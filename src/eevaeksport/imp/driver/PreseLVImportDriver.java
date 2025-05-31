
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class PreseLVImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(PreseLVImportDriver.class.getName());

  public PreseLVImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    } catch (Exception e) {
      parent.showError(e);
      log.error(e);
    }
  }
  
  private String	       url = "jdbc:odbc:merisoft";

  public String getImportFile(){
	   return parent.getProps().getProperty("imp.read");
	 }
  
  private Connection getNewCon() throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	  }
  
  private HashMap getVas() {
  	HashMap   tm = new HashMap();
  	
  	try {
     
  		
  		   String  cmd = "WITH LatestIssues AS (\r\n" + 
  		   		"    SELECT VALJAANNE_ID, MAX(izndat) AS LatestDate\r\n" + 
  		   		"    FROM Numuri where izndat > getdate() -400\r\n" + 
  		   		"    GROUP BY VALJAANNE_ID\r\n" + 
  		   		")\r\n" + 
  		   		"SELECT \r\n" + 
  		   		"    N.VA_NUMBER_ID,\r\n" + 
  		   		"    N.VALJAANNE_ID,\r\n" + 
  		   		"    N.VA_NUMBER,\r\n" + 
  		   		"    N.KAANEHIND,\r\n" + 
  		   		"    N.izndat AS issue_date,\r\n" + 
  		   		"    N.slegts AS Status , ean\r\n" + 
  		   		"FROM \r\n" + 
  		   		"    Numuri N\r\n" + 
  		   		"JOIN \r\n" + 
  		   		"    LatestIssues L\r\n" + 
  		   		"    ON N.VALJAANNE_ID = L.VALJAANNE_ID AND N.izndat = L.LatestDate";
  	    
  	

  	                       Connection con =  this.getNewCon();
  		                   Statement stmt = con.createStatement();		
  	                       ResultSet rs = stmt.executeQuery(cmd);

  	                      while (rs.next()) {
  	                    	   String vas = rs.getString("VA_NUMBER_ID").trim();
  	                        String kood = rs.getString("VALJAANNE_ID").trim();
  	                     
  	                          tm.put(kood,vas);  
  	                      }
  	                      
  	                      rs.close();
  	                      stmt.close();
  	                      con.close();
  	}catch(Exception e) {
  		e.printStackTrace();
  	}
      
      return tm;
  }


  public String identifyCustomer(String id,String regnr) throws SQLException {
    String acc = null;
    return acc;
  }

  
  
  public String identifyProduct(String ean, String prodID) throws SQLException{
   String prod = null;
    return prod;
  }


  
  public void makeOrder(Order order) throws SQLException{


	 	 if(numurs == null) {
	 		 numurs = this.getVas();
	 	 }
	 	 
	 counter = 0;
     makeOutput(order); 
        	   
     setNumAddedLines(counter);
  }

 
 	  private HashMap numurs = null;
 	  
 private void makeOutput(Order order){
 	StringBuffer sb = new StringBuffer();
 	 nf.setGroupingUsed(false);

 	try {
		String com = order.getInfo();
		if ( com == null ){
			com =  " ";
		}		
	
		 String doknr = order.getDokNr();
         String ls = System.getProperty("line.separator");
         String agent = order.getAgent();       
         String custID = order.getCompany();
     
             Calendar  shipDate = order.getShipCal();
         
            Calendar cal = Calendar.getInstance();
           if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
        	   cal.add(Calendar.DAY_OF_YEAR, 3);  
           }else{
        	   cal.add(Calendar.DAY_OF_YEAR, 1);  
           }
          
           if (shipDate != null) {
                 cal = shipDate;
               }
         
           String nummer = "MS" + agent + "" + (Calendar.getInstance().getTimeInMillis()+ "").substring(6);
           if(nummer.length()>20){
        	   nummer = nummer.substring(0,20);
           }
           
           if(order.getDokNr() != null && order.getDokNr().trim().length() != 0 &&
        		   !order.getDokNr().trim().equals("null")){
        	   nummer = "MS" + agent + "" + order.getDokNr();
        	   if(nummer.length()>20){
            	   nummer = nummer.substring(0,20);
               }       	   
           }
        
           
           Vector sortedLines = order.getOrderLines();
           for (int i = 0; i < sortedLines.size(); i++) {
               OrderLine line = (OrderLine)sortedLines.elementAt(i);
               double kogus = line.getKogus();
               String toode = line.getToode();
               
               if(numurs.get(toode) != null) {
            	   toode = numurs.get(toode) + ""; 
               }
                   sb.append(nummer + ";" + custID + ";" + toode + ";" + kogus + ";" + Converter.getDDpMMpYYYYpString(cal) + ";" + com + ls );
               counter++;
           }
     	    
      String fn = doknr;
      if(doknr.trim().length()== 0){
    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
      }
      
      String fileName = parent.getProps().getProperty("conv.dir") +  agent  + "." + fn  +  ".csv";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sb.toString().getBytes());
        fos.close();
       
	} catch (Exception e) {
		e.printStackTrace();
		
	log.error(e);
	}
 }
 
 
	public NumberFormat getNF() {
		return this.nf;
	}
	
	 
  public String getDriverVersion(){
    return "PreseLVImport";
  }




  
  private NumberFormat calNF = NumberFormat.getInstance();
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private int counter = 0;
  private ImportPanel parent;
  private String trouble = null;

public String getTrouble() {
	return trouble;
}
}
