
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
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

public class DunkerLATImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public DunkerLATImportDriver(ImportPanel parent) {
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
    loadSignatures();
   }
   private Vector signatures = new Vector();

   
 	 public static Object readObject(String s) throws IOException,
 ClassNotFoundException, IOException {
 Object obj = null;
 File f = new File(s);
 if (f.exists()){
 ObjectInputStream ois = new ObjectInputStream(new FileInputStream(s));
 obj = ois.readObject();
 ois.close();
 }
 return obj;
 }

   private void loadSignatures(){
 		
 	  try{		
 	  		File f = new File("signatures.vec");
 	  		Calendar ss = Calendar.getInstance();
 	  		
 	  		if (f.exists() && f.lastModified() < ss.getTimeInMillis() - 3600000) {// kui on yle tunni vana siis maha 
 	  			f.delete();
 	  			signatures = new Vector();
 	  		} else {
 	  			signatures = (Vector) readObject("signatures.vec");
 	  		}

 	  		
 	  	} catch (Exception e) {
 	  		signatures = new Vector();
 	  	}

 	  	}
   

  private Connection getNewConnection(String url) throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	  }
  
  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	
  	 System.out.println("Kliendi M��ramine: " );
  	 
  	 if(id.length() > 10){
  		 id = id.substring(id.length()-10);
  	 }
  	
  	    sql1 = "SELECT SL01001 AS kood FROM SL010200 where SL01106  =  '" + id + "'";	
	
    System.out.println(sql1);
    String acc = null;

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

 
  public String identifyCustomerAGENT(String id ,String saaja ) throws SQLException {
 	 
 	String sql1 = "SELECT sh.\"AgentCode\" as agent FROM \"ShipTos\" sh LEFT JOIN \"Customers\" cus on cus.\"Code\"=sh.\"CustomerCode\" " +
 			"  where cus.\"Code\"='" + id +"' and sh.\"ShipToCode\"='" + saaja +"'";
 	
   System.out.println(sql1);
   String agent = null;

   Statement st1 = con.createStatement();
   ResultSet rs1 = st1.executeQuery(sql1);

   while (rs1.next()) {
   	agent = rs1.getString("agent").trim();

   }
   rs1.close();
   st1.close();

   return agent;

 }
 
  
  
  public String getMTKood(int paevad) throws SQLException{
 	 
    String sql1 = " select SL22002 as kood from SL220200 where SL22003 =0 and SL22005 =  " + paevad + "";
    String kood = null;
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      kood = rs1.getString("kood").trim();
    }
       System.out.println(paevad + " mtkood " + kood);
    rs1.close();
    st1.close();
    return kood;
 }
 
  
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	
    
    String sql2 = " SELECT RTRIM(SC01001) AS toode FROM SC010200  WHERE (RTRIM(SC01132)=   '" + ean.trim() 
                              + "' or RTRIM(SC01003) = '" + ean.trim() + "' or RTRIM(SC01060) = '" + ean.trim() + "') and SC01015 != ''";

    
    String sql1 ="";
        
        Statement st1 = con.createStatement();
        ResultSet rs1  = null;
        
    String prod = null;
    if(prodID != null){
    	sql1 = "SELECT RTRIM(SC01001) AS toode FROM SC010200  WHERE RTRIM(SC01001)=  '" + prodID.trim() + "' or  RTRIM(SC01001)= left ('" + prodID.trim() + "',4)";	

     rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }	
      }
   System.out.println(ean + " prod0 " + prod);
    if(prod == null || prod.trim().length() == 0){

      rs1 = st1.executeQuery(sql2);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
      }
    }

    if(prod == null && ean.trim().length() > 6){
  	  while(ean != null && ean.startsWith("0")){
	    	ean = ean.substring(1);
	    	 rs1 = st1.executeQuery( " SELECT RTRIM(SC01001) AS toode FROM SC010200  WHERE (RTRIM(SC01132)=   '" + ean.trim() 
	                  + "' or RTRIM(SC01003) = '" + ean.trim() + "' or RTRIM(SC01060) = '" + ean.trim() + "') and SC01015 != ''" );
		      if (rs1.next()) {
		        prod = rs1.getString("toode").trim();
		      }
		        
  	  }
	     
	    }
    
    System.out.println(prodID + " prod1 " + prod);
    try{
    	  rs1.close();
    	    st1.close();	
    }catch(Exception rr){
    rr.printStackTrace();	
    }
  

    return prod;
  }

  private String trouble = null;
  
  public void makeOrder(Order order) throws SQLException{

	  
	  
	   String signature = order.getSignature();
	    if(signatures == null){
	    	signatures = new Vector();
	    }
		  if(signatures.contains(signature)){
			  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
			    		  
		  }else{
	  
	  counter = 0;
	     
	     String salesid = "1";
	     
	     String sql4 = "select  max ( \"GlobalOrderID\") + 1 as salesid from \"OrderHeaders_Android\"";
		    Connection con9 = this.getNewConnection(url);
		    Statement st2 = con9.createStatement();
		    ResultSet rsd = st2.executeQuery(sql4);
		    if(rsd.next()){
		    	salesid = rsd.getInt("salesid") + "";
		    }
		    rsd.close();
		     st2.close();
		     con9.close();
	     
	     addInvoiceHeader(salesid,order);
	     addInvoiceLines(salesid,order);
	     signatures.add(signature);
	  
      setNumAddedLines(counter);
		  }
	   try {
			saveObject(signatures, "signatures.vec");
		} catch (IOException e) {
			e.printStackTrace();
		}
  }
  
  public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}


  private void addInvoiceLines(String salesid, Order order) throws SQLException{

    Enumeration enums = order.getOrderLines().elements();
     counter = 0;
    while (enums.hasMoreElements()){
      counter++;
      log.info("ORDER_LINE_COUNTER:" + counter);
      OrderLine orderLine = (OrderLine)enums.nextElement();
      String itemid = orderLine.getToode();
      double kogus = orderLine.getKogus();
      double ale = 0;
     
      if(orderLine.getDiscount() > 0){
    	  ale = orderLine.getDiscount();
      }
      
      if(kogus < 0 && ale > 0){
    	  kogus = -1 * kogus;
      }
      
      double hind = 0;
       if(orderLine.getPrice() > 0){
    	   hind = orderLine.getPrice() ;
       }
      
       String unit = orderLine.getUnit();
   
       
       String sql = "INSERT INTO \"OrderLines_Android\"( " +
     "\"GlobalOrderID\", \"LineID\", \"ItemCode\", \"Quantity\", \"Price\", \"PriceType\", " +
    "        \"DiscountPct\", \"StatusID\", \"StatusDescription\", \"UOM\", \"ExternalOrderNo\")" +
   "  VALUES (" + salesid + ", " + counter + ", '" + itemid + "', " + kogus + ", " + hind + ", 'Standard', " + 
       "    " + ale + ", 1000, '', '" + unit + "', '" + order.getAgent() + "-" + order.getDokNr() + "')";
          Connection cc = this.getNewConnection(url);

          
          log.info("SALES_LINE_SQL: " + sql);
          System.out.println(sql);
          PreparedStatement st2 = cc.prepareStatement(sql);
        //  st2.executeUpdate();
        
          //addNumLine();
          //st2.close();
          //cc.close(); 
          

       		try {

        		    cc = this.getNewConnection(url);

        		    // Start transaction
        		    cc.setAutoCommit(false);


                     st2 = cc.prepareStatement(sql);

        		    log.info("Executing insert into OrderLines_Android");

        		    st2.executeUpdate();

        		    addNumLine();

        		    // Commit transaction
        		    cc.commit();

        		} catch (Exception e) {

        		    // Rollback if something fails
        		    if (cc != null) {
        		        try {
        		            cc.rollback();
        		        } catch (Exception rollbackEx) {
        		            rollbackEx.printStackTrace();
        		        }
        		    }

        		    e.printStackTrace();

        		} finally {

        		    // Close resources
        		    if (st2 != null) {
        		        try {
        		            st2.close();
        		        } catch (Exception e) {
        		            e.printStackTrace();
        		        }
        		    }

        		    if (cc != null) {
        		        try {
        		            cc.setAutoCommit(true);
        		            cc.close();
        		        } catch (Exception e) {
        		            e.printStackTrace();
        		        }
        		    }
        		}
        
       }
    }



  

  private void addInvoiceHeader(String salesid , Order order) throws SQLException{
  	
  	
  	System.out.println("doknr " + order.getDokNr());
  	String agent = order.getAgent();
  	
  	System.out.println("agent " +  agent);
  	
  
  String  ladu = order.getWarehouse();
  if(ladu == null || ladu.equals(" ")){
  	ladu = "1";
  }
 
  
       String text = order.getEmailBody();
       System.out.println("text " + text);
  		if (text != null && text.trim().length()>0){
  			text = " " + text.trim();
  		}
  		
  		if(text == null){
  			text = "";
  		}
  		
  		String text2 = order.getInfo();
  		if (text2 != null && text2.trim().length()>0){
  			text2 = " " + text2.trim();
  		}
  		
  		if(text2 == null){
  			text2 = "";
  		}
  	
  		
  		System.out.println("info sisselugemisel " + order.getInfo());
                		
                		String ref = order.getReference();
                		if(ref == null || ref.trim().length()==0){
                			ref =  "";
                		}
             String type = "Order";  
             if(order.getStatus() == 2){
            	 type = "Return";
             }
             
             OrderLine ol = (OrderLine)order.getOrderLines().elementAt(0);
             if(ol.getKogus() < 0 && ol.getDiscount() > 0){

            	 type = "RevaluationAct";
             }
             
             if(order.getStatus() == 4){
            	 type = "RevaluationAct";
             }
                		

      String klient = order.getCompany().substring(0,order.getCompany().lastIndexOf("-"));
      String saaja = order.getCompany().substring(order.getCompany().lastIndexOf("-") + 1);
  	if(agent.indexOf("HOUSE")!=-1){
  		agent = identifyCustomerAGENT(klient,saaja);
  		if(agent == null){
  			agent = "HOUSE";
  		}
  	}
  text2 = 	text2.replaceAll("'", " ").replaceAll("\"", " ");
  if(text2.length() > 70 ){
	  
  }
      
      String sql = "INSERT INTO \"OrderHeaders_Android\"( " +
           " \"GlobalOrderID\", \"RemoteOrderID\", \"OrderType\", \"CustomerCode\", " +
           " \"ShipToCode\", \"OrderDate\", \"RequestedDeliveryDate\", \"AgentCode\", " +
           " \"OrderComments\", \"StatusID\", \"StatusDescription\", \"Imported\", " +
           " \"BackOfficeOrderID\", \"ExportedToBackOffice\", \"IsMailSend\", \"MailSend\", " +
           " \"ExternalOrderNo\")" +
   " VALUES (" + salesid + ", " + (Calendar.getInstance().getTimeInMillis() + "").substring(4) + ",'" + type +"','" + klient + "', " +
    "        '" + saaja + "', current_date, ?, '" + order.getAgent() + "', " +
     "      '" + text2 + "', 1000, '', current_timestamp, " +
      "      '', null, false, null,  " +
       "     '" + order.getAgent() + "-" + order.getDokNr() + "');";
      
    log.info("addInvoiceHeader2: " + sql);
    System.out.println(sql);
      Connection ccg = this.getNewConnection(url);
        PreparedStatement stmt2 = ccg.prepareStatement(sql);
     /*   stmt2.setTimestamp(1, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
        stmt2.executeUpdate();
        stmt2.close();
        ccg.close();
       */ 


        	try {
        	    // Start transaction
        	    ccg.setAutoCommit(false);
        	    stmt2 = ccg.prepareStatement(sql);
        	    stmt2.setTimestamp(1, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
        	     log.info("Executing OrderHeaders_Android insert");
        	    stmt2.executeUpdate();

        	    // Commit transaction
        	    ccg.commit();

        	} catch (Exception e) {

        	    // Rollback on failure
        	    if (ccg != null) {
        	        try {
        	            ccg.rollback();
        	        } catch (Exception ex) {
        	            ex.printStackTrace();
        	        }
        	    }

        	    e.printStackTrace();

        	} finally {

        	    if (stmt2 != null) {
        	        try {
        	            stmt2.close();
        	        } catch (Exception e) {
        	            e.printStackTrace();
        	        }
        	    }

        	    if (ccg != null) {
        	        try {
        	            ccg.setAutoCommit(true);
        	            ccg.close();
        	        } catch (Exception e) {
        	            e.printStackTrace();
        	        }
        	    }
        	}
        
     
    
  }

  
  
 

  
 
	public NumberFormat getNF() {
		return this.nf;
	}


 
  public String getDriverVersion(){
    return "DunkerLatImport";
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

 
  
  private String url = "jdbc:odbc:droid_db_food";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private int counter = 0;
  private ImportPanel parent;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
