package eevaeksport.driver;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
import java.util.HashMap;

public interface Outputter {
  public HashMap getOutPutHash() ;
  public void readStockToDb();
}
