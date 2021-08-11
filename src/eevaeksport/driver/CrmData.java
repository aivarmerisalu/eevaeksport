/*
 * Created on 17.02.2007
 */
package eevaeksport.driver;

import java.sql.SQLException;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
public interface CrmData {
	  void setCrmData(String agentID,String agentName,String custID,
			  String custName,String tekst,java.sql.Date date) throws SQLException;
	}