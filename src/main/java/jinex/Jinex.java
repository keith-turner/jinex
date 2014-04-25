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

import java.io.File;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.accumulo.core.data.ByteSequence;
import org.apache.accumulo.core.data.Range;
import org.apache.commons.io.FileUtils;

import accismus.api.Column;
import accismus.api.ColumnIterator;
import accismus.api.LoaderExecutor;
import accismus.api.RowIterator;
import accismus.api.ScannerConfiguration;
import accismus.api.Snapshot;
import accismus.api.SnapshotFactory;


/**
 * 
 */
public class Jinex {

  private Properties accismusProps;
  private SnapshotFactory snapshotFactory;

  public Jinex(Properties accismusProps) {
    this.accismusProps = accismusProps;
    this.snapshotFactory = new SnapshotFactory(accismusProps);
  }

  public void indexJars(File dir) throws Exception {

    LoaderExecutor le = new LoaderExecutor(accismusProps);

    int count = 0;

    for (File jarFile : FileUtils.listFiles(dir, new String[] {"jar"}, true)) {
      JarMetadata jarMeta = new Parser().parseJar(jarFile);
      le.execute(new JarMetadataLoader(jarMeta));
      System.out.println("Queued : " + jarFile.getName());
      count++;
    }

    le.shutdown();

    System.out.println("Loaded " + count + " jars");

  }

  public void printTop(String packagePrefix, int n) throws Exception {
    Snapshot snapshot = snapshotFactory.createSnapshot();

    String depth = JarMetadataLoader.getDepth(packagePrefix);
    Range range = Range.prefix("crci:" + depth + ":" + packagePrefix);
    // Range range = Range.prefix("crci:");

    RowIterator iter = snapshot.get(new ScannerConfiguration().setRange(range));

    int count = 0;

    while (iter.hasNext() && count < n) {
      Entry<ByteSequence,ColumnIterator> rowEntries = iter.next();
      while (rowEntries.getValue().hasNext() && count < n) {
        Entry<Column,ByteSequence> col = rowEntries.getValue().next();
        // System.out.println(rowEntries.getKey() + " " + col.getKey() + " " + col.getValue());

        String[] tokens = rowEntries.getKey().toString().split(":");

        System.out.printf("%,d : %s\n", (999999999 - Integer.parseInt(tokens[3])), tokens[4]);
        count++;

      }
    }

  }
}
