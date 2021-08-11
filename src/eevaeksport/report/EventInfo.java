package eevaeksport.report;

import java.sql.SQLException;
import java.util.Vector;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface EventInfo {
  void eventInfo(Vector lines) throws SQLException;
}