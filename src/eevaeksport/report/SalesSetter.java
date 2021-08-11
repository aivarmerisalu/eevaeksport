package eevaeksport.report;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface SalesSetter {
  void setSales(HashMap custprods) throws SQLException;
}