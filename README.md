jinex
=====

Example Jar indexer built on Accismus.  This example computes the most
frequently used classes for the package hierarchy.  

After cloning this repo, build with following command.  May need to install
Accismus into your local maven repo first.

```
mvn package -DskipTests
```

Copy this jar to the Accismus observer directory.

```
cp target/jinex-0.0.1-SNAPSHOT.jar $ACCISMUS_HOME/lib/observers
```

Modify `$ACCISMUS_HOME/conf/initialization.properties` and replace the observer
lines with the following:

```
accismus.worker.observer.0=prcount,update,,jinex.PackageObserver
```

Now initialize and start Accismus as outlined in its docs.  The following shows
running a simple jinex shell.  The `scan` command will indext all jar files
under `/opt/hadoop-2.2.0`.  After this finishes the observers will still be
working in the background.  You can look at
`$ACCISMUS_HOME/logs/*worker.debug.log` to check on their status.  

Running `top org 10` shows that 14,767 classes whos package start with org
referenced Object.  Then 6,658 org classes reference `java.io.IOException`.
Running `top org.apache.hadoop 10` goes deeper and shows 6,224 clases whos
package stats with `org.apache.hadoop` referenced java.lang.Object.

```
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
java -cp `cat cp.txt`:target/jinex-0.0.1-SNAPSHOT.jar jinex.Shell $ACCISMUS_HOME/conf/accismus.properties
>scan /opt/hadoop-2.2.0
Loaded 272 jars
>top org 10
14,767 : java.lang.Object
6,658 : java.io.IOException
6,025 : java.lang.String
5,500 : java.lang.StringBuilder
3,264 : java.lang.Exception
2,901 : java.util.Iterator
2,834 : java.lang.Throwable
2,554 : java.lang.Class
2,512 : java.util.Map
2,311 : java.lang.IllegalArgumentException
>top org.apache.hadoop 10
6,224 : java.lang.Object
4,058 : java.io.IOException
2,514 : java.lang.String
2,513 : java.lang.StringBuilder
1,945 : java.lang.Throwable
1,846 : org.apache.hadoop.classification.InterfaceAudience
1,394 : java.lang.Exception
1,387 : org.apache.hadoop.classification.InterfaceStability
1,286 : com.google.protobuf.InvalidProtocolBufferException
1,264 : org.apache.hadoop.conf.Configuration
```

TODO This is a complex example that needs to be cleaned up and needs some more
documentation. 


