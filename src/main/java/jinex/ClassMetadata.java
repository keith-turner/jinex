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

import java.util.Set;

/**
 * Data class that encapsulates information about a java class file
 */
public class ClassMetadata {

  private String hash;
  private String name;
  private Set<String> deps;

  public ClassMetadata(String hash, String name, Set<String> deps) {
    this.hash = hash;
    this.name = name;
    this.deps = deps;
  }

  public String getHash() {
    return hash;
  }

  public String getName() {
    return name;
  }

  public Set<String> getClassDependencies() {
    return deps;
  }
}
