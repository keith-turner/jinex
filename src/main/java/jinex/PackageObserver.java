package jinex;

import org.apache.accumulo.core.data.ByteSequence;

import accismus.api.Column;
import accismus.api.Observer;
import accismus.api.Transaction;
import accismus.api.exceptions.AlreadySetException;

public class PackageObserver implements Observer{

  public static final Column TOTAL_COLUMN = new Column("prcount", "total");
  public static final Column UPDATE_COLUMN = new Column("prcount", "update");

  public void process(Transaction tx, ByteSequence row, Column col) throws Exception {
    if (!col.equals(UPDATE_COLUMN))
      throw new IllegalArgumentException("Unexpecred column "+col);
    
    int upCount = Integer.parseInt(tx.get(row, col).toString());
    
    String[] tokens = row.toString().split(":");
    String pkg = tokens[1];
    String dep = tokens[2];

    ByteSequence total = tx.get(row, TOTAL_COLUMN);
    int totalCount = 0;
    if(total != null){
      totalCount += Integer.parseInt(total.toString());
    }
    
    tx.set(row.toString(), TOTAL_COLUMN, (upCount + totalCount) + "");

    tx.delete(row.toString(), UPDATE_COLUMN);

    String parent = JarMetadataLoader.getParent(pkg);
    if(parent != null){
      String pkgRefCountRow = tokens[0] + ":" + parent + ":" + dep;
      //TODO duplicate code
      ByteSequence currCountBS = tx.get(pkgRefCountRow, UPDATE_COLUMN);
      int pkgRefCount = upCount; 
      if(currCountBS != null)
        pkgRefCount += Integer.parseInt(currCountBS.toString());
      tx.set(pkgRefCountRow, UPDATE_COLUMN, pkgRefCount + "");
    }
    

    //update inverted count index
    String depth = JarMetadataLoader.getDepth(pkg);
    //crci:<depth>:<from prefix>:<count>:<to class>
    if(total != null){
      //remove old index entry
      JarMetadataLoader.getCount(totalCount);
      String oldInverseCountRow = "crci:"+depth+":"+pkg+":"+JarMetadataLoader.getCount(totalCount)+":"+dep;
      tx.delete(oldInverseCountRow, new Column("foo","bar"));
    }
    
    //add new index entry
    String newInverseCountRow = "crci:"+depth+":"+pkg+":"+JarMetadataLoader.getCount(upCount+totalCount)+":"+dep;
    try {
      tx.set(newInverseCountRow, new Column("foo", "bar"), "");
    } catch (AlreadySetException ase) {
      System.out.printf("upCount %d total %d\n", upCount, totalCount);
      throw ase;
    }
  }
  
}
