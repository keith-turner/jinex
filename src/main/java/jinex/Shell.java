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
import java.util.Properties;

import jline.ConsoleReader;

import org.apache.accumulo.accismus.api.config.AccismusProperties;

/**
 * 
 */
public class Shell {
  public static void main(String[] args) throws Exception {
    
    Properties props = new AccismusProperties(new File("accismus.properties"));
    
    ConsoleReader reader = new ConsoleReader();

    Jinex jinex = new Jinex(props);

    while (true) {
      String line = reader.readLine(">");
      if (line == null)
        break;

      final String[] tokens = line.split("\\s+");

      if (tokens[0].equals("scan") && tokens.length == 2) {
        jinex.indexJars(new File(tokens[1]));
      } else if (tokens[0].equals("top") && tokens.length == 3) {
        jinex.printTop(tokens[1], Integer.parseInt(tokens[2]));
      } else if (tokens[0].equals("quit") && tokens.length == 1) {
        break;
      }

    }
  }
}
