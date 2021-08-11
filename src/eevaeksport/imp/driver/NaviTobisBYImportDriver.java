/*
f  * Created on 31.07.2006
 */
package eevaeksport.imp.driver;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */

 import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;


 public class NaviTobisBYImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

   public NaviTobisBYImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
     //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
     
       con2 = DriverManager.getConnection(url + "NAV40"
            , "", "");
       con3 = DriverManager.getConnection(url + "NAV40"
            , "", "");
       con4 = DriverManager.getConnection(url + "NAV40"
            , "", "");
       con5 = DriverManager.getConnection(url + "NAV40"
            , "", "");
       con6 = DriverManager.getConnection(url + "NAV40"
            , "", "");

     
             
     String sql =  "  SELECT k.[No_] AS kood " +
	 " FROM [Amazis 4_0$Customer] k " +
	 " WHERE k.[Salesperson Code] != ''  AND Blocked != 3 " ;

System.out.println(sql);


     Statement stmt = con2.createStatement();
     ResultSet rs = stmt.executeQuery(sql);
     while(rs.next()){
    		String kood = rs.getString("kood").trim(); 
    		String asi = Converter.enCodeEst(kood);
    		custcodes.put(asi, kood);
     }
     rs.close();
     stmt.close();
    
     }
     catch (Exception e) {
       parent.showError(e);
     }
   }

   HashMap custcodes = new HashMap();
   
   
   public String identifyCustomer(String id,String regnr)throws SQLException {
    String custid = null;
    
    String cmd = "SELECT kl.code FROM grcompany kl WHERE U_ImpordiFiliaal = '" + id + "'";   
    PreparedStatement st2 = con4.prepareStatement(cmd);
    ResultSet rs = st2.executeQuery();
 
    while (rs.next()){
    	custid =  rs.getString("code");
     }
    
    rs.close();
    st2.close();
    
    if(custid == null){
    	   String cmd2 = "SELECT kl.code FROM grcompany kl WHERE  id ='" + id + "'";   
    	    PreparedStatement st22 = con4.prepareStatement(cmd2);
    	    ResultSet rs2 = st22.executeQuery();	 
    	    while (rs2.next()){
    	    	custid =  rs2.getString("code");
    	     }
    	    rs2.close();
    	    st22.close();
    }
    
   	return custid;
   }

   public int getSalesid() throws SQLException{
     String cmd = "SELECT MAX(Nr)+1 AS id from [Amazis 4_0$MOFF Uzsakymu krepselis] where Merisoft= 1  ";   
     PreparedStatement st2 = con2.prepareStatement(cmd);
     ResultSet rs = st2.executeQuery();
     int result = 0 ;
     while (rs.next()){
     result =  rs.getInt("id");
      }
     rs.close();
     st2.close();
     if(result  < 7000000){
    	 result = 7000001;
     }
     this.idid = result;
     return result;
   }

   public String identifyProduct(String ean, String prodID) throws SQLException{

  	String sql2 = "select pr.code as toode  FROM grarticle pr inner join grbarcode bc " +
  			" on bc.idgrarticle = pr.id where bc.code ='" + ean + "'";


    String sql1 = "Select pr.code AS toode FROM grarticle pr " + 
          " where pr.code='" + prodID + "'";
    log.info("1_LINE_SQL: " + sql1);
    log.info("2_LINE_SQL: " + sql2);

    String prod = null;
Connection ccc = DriverManager.getConnection(url + "saf"
        , "mobile", "mobile");
    Statement st1 = ccc.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
    
    rs1.close();
    st1.close();
//System.out.println("prod0" + prod);
    //rs1.close();
   // st1.close();
    
    if(prod == null || prod.trim().length() == 0){
      st1 = ccc.createStatement();
      rs1 = st1.executeQuery(sql2);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
      }
    }

    //System.out.println("prod1" + prod);
    rs1.close();
    st1.close();
   ccc.close();
    return prod;
   }

   public void makeOrder(Order order) throws SQLException{
     setOrder(order);
     System.out.println("out1");
     String salesid = this.getSalesid() + "";

     System.out.println("out2");
     addInvoiceHeader(salesid);

     System.out.println("out3");
     addInvoiceLines(salesid);
   }



   private void addInvoiceLines(String salesid) throws SQLException{

     Enumeration enumr = order.getOrderLines().elements();
     int counter = 0;
     while (enumr.hasMoreElements()){
       counter++;
       log.info("ORDER_LINE_COUNTER:" + counter);
       OrderLine orderLine = (OrderLine)enumr.nextElement();
       String itemid = orderLine.getToode();
       double kogus = orderLine.getKogus();
       double ale = orderLine.getDiscount();
    
       
    
       String agent = order.getAgent();
         int alex = 0;
         if(ale == -2){
        	alex = 1; 
         }
     
         String	sql = "INSERT INTO [Amazis 4_0$MOFF Prekiu krepselio eilute] ([Krepselio Nr],[Eilutes Nr], [Prietaiso Nr],[Prekes Nr]," +
         		"    [Kiekis],[Dovana],[Nuolaidos proc_])" +
             " VALUES( " +
               salesid + "," +
               counter + ",'MS-" +
               agent + "',?," +
               kogus + "," +
               alex +"," + ale + ")";
System.out.println(sql);
               log.info("SALES_LINE_SQL: " + sql);
               PreparedStatement st2 = con3.prepareStatement(sql);
               st2.setString(1,   Converter.deCode(itemid).trim() );
               String aiai = parent.getProps().getProperty("testing");
               if(aiai != null && aiai.equals("true")){
            	   
               }else{
            	   st2.executeUpdate();   
               }
              
               setAmount(false);
               addNumLine();
               st2.close();
           
        }
   //  System.exit(0);
     }

   private boolean isBeforeOneDay(Calendar cal , Calendar ship){
		  
		  Calendar cc = Calendar.getInstance();
		  cc.setTimeInMillis(cal.getTimeInMillis());
		  cc.set(Calendar.HOUR, 2);
		  cc.set(Calendar.MINUTE, 1);
		  cc.set(Calendar.SECOND, 1);
		  cc.set(Calendar.MILLISECOND, 1);
		  cc.add(Calendar.DAY_OF_YEAR, 1);
		  
		  Calendar ss = Calendar.getInstance();
		  ss.setTimeInMillis(ship.getTimeInMillis());
		  ss.set(Calendar.HOUR, 1);
		  ss.set(Calendar.MINUTE, 1);
		  ss.set(Calendar.SECOND, 1);
		  ss.set(Calendar.MILLISECOND, 1);
		  
		  if(cc.getTime().before(ss.getTime())){
			  return true;
		  }
		  
		  return false;
	  }
		


   private void addInvoiceHeader(String salesid) throws SQLException{
   	

   	
   	String agent = order.getAgent();
    
   	String custid = (order.getCompany());
   	System.out.println("enne " + custid );
     custid =   (String) this.custcodes.get(custid);
     if(custid == null) {
    	 custid = (order.getCompany());
     }
     System.out.println("parast " + custid );
   //	 System.exit(0);
     
   	System.out.println("agent " + agent);

   	
   
    String tekst = "";

    if (order.getEmailBody()!= null){
      // tekst = order.getEmailBody().replaceAll("\n"," ");
     }
          
    
    if(order.getInfo() != null && order.getInfo().trim().length()>0 ){
    	tekst = order.getInfo().trim();
    }

    if(tekst.length() > 250){
    	tekst = tekst.substring(0, 250);
    }

		String reference  = order.getTeostaja();
		
		String ref = "";
		if ( reference != null && reference.trim().length() > 4 ){
		  ref = reference.trim().toUpperCase();
		}
		
	     Calendar cal = Calendar.getInstance();
	     boolean change = false;
	     Calendar  shipDate = order.getShipCal();
         if(shipDate != null && cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY &&
         		shipDate.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY && 
         		shipDate.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY){
        	 change = true;
         }
         
         if(shipDate != null && isBeforeOneDay( cal,shipDate ) ){
         	 change = true;         	
          }
	        
	          if(change){
	        	  cal = shipDate;
	          }
	        String status = "0";
	        if(order.getStatus() == 1){
	        	status = "1";
	        }
	        if(order.getStatus() == 3){
	        	status = "3";
	        }
	        
	        if(order.getStatus() == 2){
	        	status = "2"; //return
	        }
	        if(order.getStatus() == 6){
	        	status = "6";
	        }
		String ladu = "";
		
		if(order.getWarehouse() != null && order.getWarehouse().trim().length() > 0){
			ladu = order.getWarehouse().trim();
		}
		String smode = "";
		if(order.getShipMode() != null && order.getShipMode().trim().length()>0){
			smode = order.getShipMode();
		}

        String  sql = "INSERT INTO [Amazis 4_0$MOFF Uzsakymu krepselis] ([Nr],[Prietaiso Nr],[Pirkejo Nr],[Registravimo data],[Merisoft]," +
        		" [Nuoroda],[External Document No_],[Sandelys],[Document Type],[ShipMode],[SalesPersoncode]) " +
                " VALUES ( " + salesid + ",'MS-" + agent + "','" + custid + "',cast(? as date),1,'"+ tekst+"','" +  
              ref + "','" + ladu  + "'," + status + ",'" + smode + "','" + order.getAgent() + "')";

     log.info("addInvoiceHeader2: " + sql);
     System.out.println("addInvoiceHeader2: " + sql);
         PreparedStatement stmt2 = con6.prepareStatement(sql);
         stmt2.setTimestamp(1, new java.sql.Timestamp(cal.getTimeInMillis()));
         String aiai = parent.getProps().getProperty("testing");
         if(aiai != null && aiai.equals("true")){
      	   
         }else{
        	 stmt2.executeUpdate();   
         }
         stmt2.close();
         addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
     //  System.exit(0);
   }


   public String getDriverVersion(){
     return "NaviTobis";
   }

   public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
 
   
   private void setOrder(Order order){
     this.order = order;
   }

   private void addLog(String line){
     parent.addLog(line);
   }
   public boolean isAmount(){
           return isAmount;
   }

   /**
   *
   * @param aUseEAN - the new value for useEAN
   */
   public void setAmount(boolean aAmount){
            isAmount= aAmount;
   }

   private String url = "jdbc:odbc:";
   
   private Connection con6;
   private Connection con2;
   private Connection con3;
   private Connection con4;
   private Connection con5;
   private ImportPanel parent;
   private Order order = null;
   private int idid = 0;
   private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
  
 }

