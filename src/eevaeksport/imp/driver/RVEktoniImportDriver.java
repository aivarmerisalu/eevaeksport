
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class RVEktoniImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public RVEktoniImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }


  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	
  	 System.out.println("Kliendi M��ramine: " );
  	 
  	 if(id.length() > 10){
  		 id = id.substring(id.length()-10);
  	 }
  	
  	    sql1 = "SELECT SL01001 AS kood FROM SL010200 where SL01106  =  '" + id + "'";	
  	  sql1 = "SELECT  f.kood FROM osmy/firma f   WHERE  f.nimi like '%" + id + "' " ;
    System.out.println(sql1);
    String acc = null;
    connectionUp(url);
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim() ;

    }
    rs1.close();
    st1.close();

    System.out.println("Klient: " + acc + " " + id);
    return acc;

  }

 
 
 
  
 
  
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
     return prodID;
  }


  
  public void makeOrder(Order order) throws SQLException{

	  counter = 0;
	  makeOutput(order); 	    
	  setNumAddedLines(counter);

  }

  private String getNimi(String aa) throws SQLException{
  	String res = null;
 	 String	 sql2 = "SELECT RTRIM(SC01002) AS toode FROM SC010200  WHERE RTRIM(SC01001)=  '" + aa + "'";
			
			Statement st1 = con.createStatement();
			ResultSet rs1 = st1.executeQuery(sql2);
			
			 if (rs1.next()) {
			   res = rs1.getString("toode").trim();
			 }
			
	rs1.close();
	st1.close();
  	return res ;
  }
  
  private String getEan(String aa) throws SQLException{
	  	String res = null;
	 	 String	 sql2 = "SELECT RTRIM(SC01132) AS toode FROM SC010200  WHERE RTRIM(SC01001)=  '" + aa + "'";
				
				Statement st1 = con.createStatement();
				ResultSet rs1 = st1.executeQuery(sql2);
				
				 if (rs1.next()) {
				   res = rs1.getString("toode").trim();
				 }
				
		rs1.close();
		st1.close();
	  	return res ;
	  }
   
  private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	 
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month + "/");

	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day+ "/");
	    sb.append(cal.get(Calendar.YEAR) + "");
	    return sb.toString();
	  }
	  


  
  private void makeOutput(Order order){
	 	StringBuffer sb = new StringBuffer();
	 	boolean minus = false;
	 	try {

	 		
		    String doknr = order.getDokNr();
		      String fn = doknr;
		      if(doknr.trim().length()== 0){
		    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
		      }
		      String dd = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + "";
		      
			    String agent = order.getAgent();
			    
			      if(agent == null || agent.trim().length()== 0){
			    	 agent = "xxx";
			      } else{
			    	  agent = agent.trim();
			      }
			String ref = order.getTeostaja(); //mobiilse telli ref number
			if ( ref == null ){
				ref =  "";
			}
			String com = order.getEmailBody();
			if ( com == null ){
				com =  " ";
			}		
		
			String ware = order.getWarehouse();
			if ( ware == null ){
				ware = " ";
			}
		
			
			String comm = order.getInfo();
			if ( comm == null ){
				comm = " ";
			}
	         
	       
	         
	         
	    Vector lines = order.getOrderLines();
	      for (int i = 0; i < lines.size(); i++) {
	    	  
	        OrderLine ol = (OrderLine) lines.elementAt(i);
	        if(ol.getKogus() < 0){
	        	minus = true;
	        }
	    	
	        String sep = "\t";
	         String lineSep = System.getProperty("line.separator");
	        
	         double ale = ol.getDiscount();
	         if(ale < 0){
	        	 ale = 0;
	         }
	         double hind = ol.getPrice();
	         
	         sb.append(order.getCompany() + sep );
	         sb.append(ol.getToode() + sep);
	         sb.append(ol.getKogus() + sep);
	         sb.append(hind + sep);
	         sb.append(ale + sep);
	         sb.append(getDateString(order.getShipCal()) + sep);
	         sb.append("\"" + comm +"\"" + sep);
	         sb.append(agent + sep);
	         
	         
	         //  RIISIP	1MARMELAAD00	20.0000	1.5120	0.0000	05/19/2017	""	4
				//  RIISIP	1MARMELAAD04	20.0000	1.5120	0.0000	05/19/2017	""	4
		  
	          sb.append(lineSep);
	 
	       counter++;
	      }
	
	      
	      setNumAddedLines(counter); 

	    
	         	  String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + order.getAgent() +"."+ order.getCompany()+"." +fn  + ".txt";
			      System.out.println(fileName);		
			      if(minus){
			    	//  fileName = escape(parent.getProps().getProperty("conv.dir2")) +  "t-" + order.getAgent() +"."+ order.getCompany()+"." +fn  + ".csv";
			      }
			      FileOutputStream fos = new FileOutputStream(fileName);
			        fos.write(sb.toString().getBytes());
			        fos.close();
	    		

		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
	  

private String escape(String s){
  if (s == null) return s;
  StringBuffer sb = new StringBuffer();
  for (int i = 0 ; i < s.length() ; i++){
    char c = s.charAt(i);
    if (c == '&'){
      sb.append("&amp;");
    } else if (c == '<'){
      sb.append("&lt;");
    } else if (c == '>'){
      sb.append("&gt;");
    } else if (c == '\n'){
      sb.append(" ");
    } else {
      sb.append(c);
    }
  }
  return sb.toString();
}


 
	public NumberFormat getNF() {
		return this.nf;
	}
	



 
  public String getDriverVersion(){
    return "RVEktoniImport";
  }




  public static final String addSpaces(String s, int totalLen){
    int len = s.length();
    StringBuffer sb = new StringBuffer(s);
    for (int i=len; i < totalLen ; i++){
      sb.append(' ');
    }
    if( len > totalLen){
    	return s.substring(0,totalLen);
    }
    return sb.toString();
  }
  
  public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  
  private NumberFormat calNF = NumberFormat.getInstance();

  public static final String cleanLitLatEst(String ret){
	    if (ret != null){
	      
	        ret = ret.replaceAll("�","e");
	        ret = ret.replace((char) 0x3f,' ');
	        
	        
	       // ret = ret.replaceAll("?","s");
	       // ret = ret.replaceAll("?","c");
	     // ret = ret.replace('',' ');
	        ret = ret.replaceAll("�","I");
	        ret = ret.replaceAll("�","u");
	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","z");
	        ret = ret.replaceAll("�","s");
	        ret = ret.replaceAll("�","S");
	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","I");
	        ret = ret.replaceAll("�","u");
	        ret = ret.replaceAll("�","i");
	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","Z");
	        ret = ret.replaceAll("�","E");
	        ret = ret.replaceAll("�","U");
	        ret = ret.replaceAll("�","U");
	        ret = ret.replaceAll("�","Pg.");
	        
	        ret = ret.replaceAll("�","Z");
	        ret = ret.replaceAll("�","A");
	        ret = ret.replaceAll("�","a");
	        ret = ret.replaceAll("�","C");

	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","E");
	        ret = ret.replaceAll("�","e");
	        ret = ret.replaceAll("�","G");
	        ret = ret.replaceAll("�","g");
	        ret = ret.replaceAll("�","I");
	        ret = ret.replaceAll("�","i");
	        ret = ret.replaceAll("�","K");
	        ret = ret.replaceAll("�","k");
	        ret = ret.replaceAll("�","L");
	        ret = ret.replaceAll("�","l");
	        ret = ret.replaceAll("�","N");
	        ret = ret.replaceAll("�","n");
	        
	        ret = ret.replaceAll("�","o");
	        ret = ret.replaceAll("�","a");
	        ret = ret.replaceAll("�","o");
	        ret = ret.replaceAll("�","u");
	       
	        ret = ret.replaceAll("�","O");
	        ret = ret.replaceAll("�","A");
	        ret = ret.replaceAll("�","O");
	        ret = ret.replaceAll("�","U");
	    }
	        
	        if(ret == null || ret.trim().length()==0){
	        return " ";
	        }else {
	        	System.out.println(ret);
	        return ret;
	        }        
	      }

  private void connectionUp(String url){
      try {
        if (con != null){
          con.close();
        }
           con = DriverManager.getConnection(url);
      } catch (Exception e) {
           e.printStackTrace();
      }

    }

  
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
 
  private int counter = 0;
  private ImportPanel parent;
  private String             url = "jdbc:odbc:Divinum";
  private Connection	       con = null;

  
public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
