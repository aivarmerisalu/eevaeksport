/*
 * Created on 17.02.2007
 */
package eevaeksport.driver;

import java.sql.SQLException;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
public interface SalesPlan {
	  void setSalesPlan(String agentID,String yearMonth,String custID,int plan,java.sql.Date date) throws SQLException;
	}