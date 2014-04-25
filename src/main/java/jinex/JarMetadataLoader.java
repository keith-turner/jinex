/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jinex;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.accumulo.core.data.ByteSequence;
import org.apache.accumulo.core.data.Range;
import org.apache.commons.collections.map.DefaultedMap;

import accismus.api.Column;
import accismus.api.ColumnIterator;
import accismus.api.Loader;
import accismus.api.RowIterator;
import accismus.api.ScannerConfiguration;
import accismus.api.Transaction;


/**
 * 
 */
public class JarMetadataLoader implements Loader {

  private JarMetadata jarInfo;

  public JarMetadataLoader(JarMetadata cinfo) {
    this.jarInfo = cinfo;
  }

  static String getParent(String classname){
    int idx = classname.lastIndexOf('.');
    if(idx == -1)
      return null;
    
    return classname.substring(0, idx);
  }
  
  static String getDepth(String classname){
    return String.format("%03d", classname.split("\\.").length);
  }
  
  static String getCount(int count){
    return String.format("%09d", 999999999 - count);
  }
  
  public void load(Transaction tx) throws Exception {

    String jarRow = "jar:" + jarInfo.getHash();

    if (tx.get(jarRow, new Column("count", "classes")) == null) {
      // have never seen this jar before
      tx.set(jarRow, new Column("count", "classes"), jarInfo.getClasses().size() + "");

      Map<String, Integer> pkgRefCounts = new DefaultedMap(new Integer(0));
      
      for (ClassMetadata classInfo : jarInfo.getClasses()) {
        String classRow = "class:" + classInfo.getName();

        if (tx.get(classRow, new Column(classInfo.getHash(), "deps")) == null) {
          // have never seen this class before, so add its dependencies
          tx.set(classRow, new Column(classInfo.getHash(), "deps"), classInfo.getClassDependencies().size() + "");

          Set<String> deps = new HashSet<String>(classInfo.getClassDependencies());
          
          //remove existing deps
          RowIterator depIter = tx.get(new ScannerConfiguration().setRange(Range.exact(classRow, "deps")));
          if(depIter.hasNext()){
            ColumnIterator colIter = depIter.next().getValue();
            while(colIter.hasNext()){
              String existingDep = colIter.next().getKey().getQualifier().toString();
              deps.remove(existingDep);
            }
          }
          
          String packageName = getParent(classInfo.getName());
          
          //new dependencies for this class
          for(String dep : deps){
            tx.set(classRow, new Column("deps", dep), "");
            if(packageName != null){
              String pkgRefCountRow = "prc:"+packageName+":"+dep;
              int pkgRefCount = pkgRefCounts.get(pkgRefCountRow);
              pkgRefCounts.put(pkgRefCountRow, pkgRefCount+1);
            }
          }
 
        }

      }

      for(Entry<String,Integer> entry : pkgRefCounts.entrySet()){
        String pkgRefCountRow = entry.getKey();
        ByteSequence currCountBS = tx.get(pkgRefCountRow, new Column("prcount","update"));
        int pkgRefCount = entry.getValue(); 
        if(currCountBS != null)
          pkgRefCount += Integer.parseInt(currCountBS.toString());
        tx.set(pkgRefCountRow, new Column("prcount","update"), pkgRefCount+"");
      }
    }

    // is this jar name known?
    if (tx.get(jarRow, new Column("names", jarInfo.getName())) == null) {
      tx.set(jarRow, new Column("names", jarInfo.getName()), "");
    }

  }
}
