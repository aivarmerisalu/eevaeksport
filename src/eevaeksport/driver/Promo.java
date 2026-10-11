package eevaeksport.driver;

import java.sql.SQLException;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface Promo {

  double getPromoSales(String agentCode, int nr) throws SQLException;

}