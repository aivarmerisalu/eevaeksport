package eevaeksport.driver;

import java.sql.SQLException;

import eevaeksport.manager.ManagerData;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface Manager {
  ManagerData getManagerData() throws SQLException;
}