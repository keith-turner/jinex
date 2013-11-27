package jinex;

import java.util.Collections;
import java.util.Map.Entry;

import jinex.PackageObserver;

import org.apache.accumulo.accismus.api.Admin;
import org.apache.accumulo.accismus.api.Column;
import org.apache.accumulo.accismus.api.ColumnIterator;
import org.apache.accumulo.accismus.api.LoaderExecutor;
import org.apache.accumulo.accismus.api.RowIterator;
import org.apache.accumulo.accismus.api.ScannerConfiguration;
import org.apache.accumulo.accismus.api.Snapshot;
import org.apache.accumulo.accismus.api.SnapshotFactory;
import org.apache.accumulo.accismus.api.config.InitializationProperties;
import org.apache.accumulo.accismus.api.config.LoaderExecutorProperties;
import org.apache.accumulo.accismus.api.test.MiniAccismus;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.core.data.ByteSequence;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.minicluster.MiniAccumuloCluster;
import org.apache.accumulo.minicluster.MiniAccumuloConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LoadTest {
  public static TemporaryFolder folder = new TemporaryFolder();
  public static MiniAccumuloCluster cluster;
  private static InitializationProperties props;
  private static MiniAccismus miniAccismus;
  private static final PasswordToken password = new PasswordToken("secret");

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    folder.create();
    MiniAccumuloConfig cfg = new MiniAccumuloConfig(folder.newFolder("miniAccumulo"), new String(password.getPassword()));
    cluster = new MiniAccumuloCluster(cfg);
    cluster.start();

    props = new InitializationProperties();
    props.setAccumuloInstance(cluster.getInstanceName());
    props.setAccumuloUser("root");
    props.setAccumuloPassword("secret");
    props.setZookeeperRoot("/accismus");
    props.setZookeepers(cluster.getZooKeepers());
    props.setAccumuloTable("data");
    props.setNumThreads(5);
    props.setObservers(Collections.singletonMap(PackageObserver.UPDATE_COLUMN, PackageObserver.class.getName()));

    Admin.initialize(props);

    miniAccismus = new MiniAccismus(props);
    miniAccismus.start();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    miniAccismus.stop();
    cluster.stop();
    folder.delete();
  }
  
  @Test
  public void test1() throws Exception {

    LoaderExecutorProperties lep = new LoaderExecutorProperties(props);
    lep.setNumThreads(3);
    lep.setQueueSize(10);
    
    LoaderExecutor le = new LoaderExecutor(lep);
    
    


    le.shutdown();
    
    miniAccismus.waitForObservers();


    SnapshotFactory snapFact = new SnapshotFactory(props);
    Snapshot snapshot = snapFact.createSnapshot();

    RowIterator iter = snapshot.get(new ScannerConfiguration().setRange(Range.prefix("prc:org:")));
    while (iter.hasNext()) {
      Entry<ByteSequence,ColumnIterator> rowEntries = iter.next();
      while (rowEntries.getValue().hasNext()) {
        Entry<Column,ByteSequence> col = rowEntries.getValue().next();
        System.out.println(rowEntries.getKey() + " " + col.getKey() + " " + col.getValue());
      }
    }
  }
}
  
