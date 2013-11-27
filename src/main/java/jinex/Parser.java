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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;

/**
 * parses class files in a jar file to determine their dependencies
 */
public class Parser {

  static class DependencyEmitter extends EmptyVisitor {

    private JavaClass javaClass;
    TreeSet<String> deps = new TreeSet<String>();

    public DependencyEmitter(JavaClass javaClass) {
      this.javaClass = javaClass;
    }

    @Override
    public void visitConstantClass(ConstantClass obj) {
      ConstantPool cp = javaClass.getConstantPool();
      String ref = obj.getBytes(cp).replace("/", ".");
      if (ref.startsWith("[L"))
        ref = ref.substring(2, ref.length() - 1);
      if (!ref.startsWith("["))
        deps.add(ref);
    }
  }

  JarMetadata parseJar(final File jarFile) throws IOException, NoSuchAlgorithmException {
    List<ClassMetadata> list = new ArrayList<ClassMetadata>();

    final JarFile jfile = new JarFile(jarFile);
    Enumeration<JarEntry> entries = jfile.entries();

    while (entries.hasMoreElements()) {
      final JarEntry entry = entries.nextElement();
      if (entry.isDirectory())
        continue;

      if (entry.getName().endsWith(".class")) {

        JavaClass jclass = new ClassParser(jfile.getInputStream(entry), entry.getName()).parse();
        DependencyEmitter visitor = new DependencyEmitter(jclass);
        DescendingVisitor classWalker = new DescendingVisitor(jclass, visitor);
        classWalker.visit();

        HashCode hash = ByteStreams.hash(new InputSupplier<InputStream>() {
          public InputStream getInput() throws IOException {
            return jfile.getInputStream(entry);
          }
        }, Hashing.sha1());

        visitor.deps.remove(jclass.getClassName());

        ClassMetadata classInfo = new ClassMetadata(hash.toString(), jclass.getClassName(), visitor.deps);
        list.add(classInfo);
      }

    }

    HashCode jarHash = ByteStreams.hash(new InputSupplier<InputStream>() {
      public InputStream getInput() throws IOException {
        return new FileInputStream(jarFile);
      }
    }, Hashing.sha1());

    return new JarMetadata(jarFile.getName(), jarHash.toString(), list);

  }
}
