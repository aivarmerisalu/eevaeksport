package eevaeksport.imp.driver;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;


public class OzolsImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(OzolsImportDriver.class.getName());

  
  public OzolsImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
    	
    	 if(parent.getProps().getProperty("dns.name") != null){
			 url = "jdbc:odbc:" + parent.getProps().getProperty("dns.name") ;
		 }
		
    	
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url , parent.getProps().getProperty("dns.user"),
                                        parent.getProps().getProperty("dns.pass"));

      con2 = DriverManager.getConnection(url , parent.getProps().getProperty("dns.user"),
                                         parent.getProps().getProperty("dns.pass"));

     


    }
    catch (Exception e) {
      parent.showError(e);
    }
 
  }
  

  public String identifyCustomer(String id,String regnr) throws SQLException {
	  String result = null;
	return result;
  }

  public String identifyProduct(String ean, String prodID) throws SQLException{
		    return null;
	  }
  

  

  
	  public void makeOrder(Order order) throws SQLException{
		  

		 
		   
	         if("true".equals(parent.getProps().getProperty("testing"))){
	        	
	   			   System.out.println("dealer "  + order.getDealer() + " " + order.getOrderLines().size());
	   		  
	          }else{
	        	 
	        	  String salesid = ""; 
	        	  if(order.getAgent().equals(order.getCompany())){
	        		  
	    			    addMoveHeader( order);
	    			    String sql4 = "SELECT MAX (DOCID) AS DOK from mj_moveheader  ";
	    			    Statement st1 = con.createStatement();
	    				 ResultSet rs1 = st1.executeQuery(sql4);

	    				 while (rs1.next()) {
	    					 salesid = rs1.getString("DOK").trim() ;

	    				 }
	    				 rs1.close();
	    				 st1.close();

	    			    
	    			    addMoveLines(salesid ,order);
	    			  sql4 = "update mj_moveheader set ISREADYFOR=1 where DOCNUMBER='" + order.getDokNr() + "' ";
	    			    PreparedStatement st2 = con2.prepareStatement(sql4);
	    			  // st2.setString(1, salesid.trim());
	    			    st2.executeUpdate();
	        	  }else{
	        		 
	    			    addInvoiceHeader( order);
	    			    String sql4 = "SELECT  (DOCID) AS DOK from mj_orderheader where"
	    			    		+ " DOCNUMBER='" + order.getDokNr() + "' and ORDERDATE  > getdate()-2  ";
	    			    Statement st1 = con.createStatement();
	    				 ResultSet rs1 = st1.executeQuery(sql4);

	    				 while (rs1.next()) {
	    					 salesid = rs1.getString("DOK").trim() ;

	    				 }
	    				 rs1.close();
	    				 st1.close();

	    			    
	    			    addInvoiceLines(salesid ,order);
	    			  sql4 = "update mj_orderheader set ISREADYFOR=1 where DOCNUMBER='" + order.getDokNr() + "' ";
	    			    PreparedStatement st2 = con2.prepareStatement(sql4);
	    			  // st2.setString(1, salesid.trim());
	    			    st2.executeUpdate(); 
	        	  }
	    			 
	    			  
	    		  }   
	          
	  }


 

	  
	  private void addInvoiceLines(String salesid,Order ord) throws SQLException{

		    Enumeration enumr = ord.getOrderLines().elements();
		    int counter = 0;
		    while (enumr.hasMoreElements()){
		      counter++;
		      log.info("ORDER_LINE_COUNTER:" + counter);
		      OrderLine orderLine = (OrderLine)enumr.nextElement();
		      String itemid = orderLine.getToode();
		      double kogus = orderLine.getKogus();

		   
		String    unit = orderLine.getUnit();
		  
		String  	name =  orderLine.getTooteNimi();
		          
	   double price = 0;
	   double discount = 0;
	   if(orderLine.getDiscount() != 0){
		   price = orderLine.getPrice();
	   }
	   
	  // DOCID ;LINENUM ;PRODID ;PRODNAME ;AMOUNT ;UNIT ;PRICE ;DISCOUNT ;
		          String sql =" INSERT into  mj_orderlines (DOCID, LINENUM,PRODID,PRODNAME, AMOUNT, UNIT ,PRICE,DISCOUNT )  " +
		   " values("+salesid+"," + counter + ","+itemid+",'" + name + "',"+kogus+",'" + unit + "'," + price +"," + discount+")";

		              log.info("SALES_LINE_SQL: " + sql);
		              System.out.println(sql);
		              PreparedStatement st2 = con2.prepareStatement(sql);
		              st2.executeUpdate();
		              addNumLine();
		              st2.close();

		              }
		  }

	  

 

	  
	  private void addMoveLines(String salesid,Order ord) throws SQLException{

		    Enumeration enumr = ord.getOrderLines().elements();
		    int counter = 0;
		    while (enumr.hasMoreElements()){
		      counter++;
		      log.info("ORDER_LINE_COUNTER:" + counter);
		      OrderLine orderLine = (OrderLine)enumr.nextElement();
		      String itemid = orderLine.getToode();
		      double kogus = orderLine.getKogus();

		   
		String    unit = orderLine.getUnit();
		  
		String  	name =  orderLine.getTooteNimi();
		          
	   double price = 0;
	   double discount = 0;
	   if(orderLine.getDiscount() != 0){
		   price = orderLine.getPrice();
	   }
	   
	  // DOCID ;LINENUM ;PRODID ;PRODNAME ;AMOUNT ;UNIT ;PRICE ;DISCOUNT ;
		          String sql =" INSERT into  mj_movelines (DOCID, LINENUM,PRODID,PRODNAME, AMOUNT, UNIT ,PRICE,DISCOUNT )  " +
		   " values("+salesid+"," + counter + ","+itemid+",'" + name + "',"+kogus+",'" + unit + "'," + price +"," + discount+")";

		              log.info("SALES_LINE_SQL: " + sql);
		              System.out.println(sql);
		              PreparedStatement st2 = con2.prepareStatement(sql);
		              st2.executeUpdate();
		              addNumLine();
		              st2.close();

		              }
		  }

	  

	  

	  
	  private void addInvoiceHeader( Order ord ) throws SQLException{

	    

	         String agent = ord.getAgent();
	         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);

	         if(ag == null){
	         	ag = new Agent(agent);
	         }

	     String tekst = ord.getInfo();
	     if(tekst == null){
	    	 tekst = "";
	     }
	     String mode = ord.getShipMode();
	     if(mode == null){
	    	 mode = "";
	     }
	      String ref = "";

	      if(ord.getReference() != null && ord.getReference().trim().length()>0 ){
	      	ref = ord.getReference().trim();
	      }

	     String cust =  ord.getCompany();
	      String aadrID = "";
	      if(cust.indexOf("-") != -1){
	    	  cust = cust.substring(0,ord.getCompany().indexOf("-"));
	    	  aadrID = ord.getCompany().substring(ord.getCompany().indexOf("-")+1);
	      }
	      
	 String dea = ord.getDealer();
		 dea = ag.getObject();
	
	      System.out.println( ord.getCompany() +"|"+ ag.getID()+ "|"+ tekst);
	     String sql = " INSERT INTO dbo.mj_orderheader (  " +
	            " DOCNUMBER,REFERENCE,CUSTID,SALESPERSON,WAREHOUSE,COMMENT,ORDERDATE,DELIVERYDATE,ISREADYFOR ) " + //,ADDRID " +
	            " VALUES ('" + ord.getDokNr() + "','" + ref + "',"
	            + cust+","+ag.getID()+"," + dea +",'"+tekst+"',?,?,0)" ;//,'" + aadrID + "'
	        System.out.println(sql);
	        
	        PreparedStatement stmt2 = con2.prepareStatement(sql);
	        java.util.Date dat = new java.util.Date();
	            dat.setTime(ord.getShipCal().getTimeInMillis());
	        Calendar cc = Calendar.getInstance();
	        stmt2.setTimestamp(1, new java.sql.Timestamp(cc.getTimeInMillis()));
	        stmt2.setTimestamp(2, new java.sql.Timestamp(dat.getTime()));
	        stmt2.executeUpdate();
	        stmt2.close();
	    
	        addLog("lisatud arve p�is -> " + ord.getCompany() + "\n");
	  }

	  
	  private void addMoveHeader( Order ord ) throws SQLException{

		    

	         String agent = ord.getAgent();
	         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);

	         if(ag == null){
	         	ag = new Agent(agent);
	         }

	     String tekst = ord.getInfo();
	     if(tekst == null){
	    	 tekst = "";
	     }
	     String mode = ord.getShipMode();
	     if(mode == null){
	    	 mode = "";
	     }
	      String ref = "";

	      if(ord.getReference() != null && ord.getReference().trim().length()>0 ){
	      	ref = ord.getReference().trim();
	      }

	     String cust =  ord.getCompany();
	      String aadrID = "";
	      if(cust.indexOf("-") != -1){
	    	  cust = cust.substring(0,ord.getCompany().indexOf("-"));
	    	  aadrID = ord.getCompany().substring(ord.getCompany().indexOf("-")+1);
	      }
	      
	 String dea = ord.getDealer();
		 dea = ag.getObject();
	
	      System.out.println( ord.getCompany() +"|"+ ag.getID()+ "|"+ tekst);
	     String sql = " INSERT INTO dbo.mj_moveheader (  " +
	            " DOCNUMBER,REFERENCE,CUSTID,SALESPERSON,WAREHOUSE,COMMENT,ORDERDATE,DELIVERYDATE,ISREADYFOR ) " + //,ADDRID " +
	            " VALUES ('" + ord.getDokNr() + "','" + ref + "',"
	            + cust+","+ag.getID()+"," + dea +",'"+tekst+"',?,?,0)" ;//,'" + aadrID + "'
	        System.out.println(sql);
	        
	        PreparedStatement stmt2 = con2.prepareStatement(sql);
	        java.util.Date dat = new java.util.Date();
	            dat.setTime(ord.getShipCal().getTimeInMillis());
	        Calendar cc = Calendar.getInstance();
	        stmt2.setTimestamp(1, new java.sql.Timestamp(cc.getTimeInMillis()));
	        stmt2.setTimestamp(2, new java.sql.Timestamp(dat.getTime()));
	        stmt2.executeUpdate();
	        stmt2.close();
	    
	        addLog("lisatud arve p�is -> " + ord.getCompany() + "\n");
	  }

	  
	  private void addLog(String line){
		    parent.addLog(line);
		  }
	 	  


	  
	  
  

  public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  


  public String getDriverVersion(){
    return "OzolsImport";
  }




  public boolean isAmount(){
          return isAmount;
  }


  public void setAmount(boolean aAmount){
           isAmount= aAmount;
  }

  private String url = "jdbc:odbc:ozols";
  private Connection con;
  private Connection con2;
  private   int cou = 0;
  private ImportPanel parent;
  private boolean isAmount = false;
 
public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
