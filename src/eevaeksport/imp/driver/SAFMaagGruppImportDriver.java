/*
 * Created on 31.07.2006
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
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.StringTokenizer;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Email;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;


 public class SAFMaagGruppImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

   public SAFMaagGruppImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
     //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
     
      
   
     }
     catch (Exception e) {
       parent.showError(e);
     }
   }

   public String identifyCustomer(String id,String regnr)throws SQLException {
    String custid = null;
    Connection  con = DriverManager.getConnection(url + "safgr"
            , "mobile", "mobilemaag");     
   
    String cmd = "SELECT kl.code FROM grcompany kl WHERE U_ImpordiFiliaal = '" + id + "'";   
    PreparedStatement st2 = con.prepareStatement(cmd);
    ResultSet rs = st2.executeQuery();
 
    while (rs.next()){
    	custid =  rs.getString("code");
     }
    rs.close();
    st2.close();
    if(custid == null){
    	   String cmd2 = "SELECT kl.code FROM grcompany kl WHERE  id ='" + id + "'";   
    	    PreparedStatement st22 = con.prepareStatement(cmd2);
    	    ResultSet rs2 = st22.executeQuery();	 
    	    while (rs2.next()){
    	    	custid =  rs2.getString("code");
    	     }
    	    rs2.close();
    	    st22.close();
    }
    con.close();
    
   	return custid;
   }

   public int getSalesid() throws SQLException{
	   String cmd = "SELECT MAX(id) AS id from SDO  where  filename like '%MJ%'"; 
	   DriverManager.getConnection(url + "safgr"
	            , "mobile", "mobilemaag");
	  Connection con2 =  DriverManager.getConnection(url + "importgr"
	            , "mobile", "mobilemaag");
     PreparedStatement st2 = con2.prepareStatement(cmd);
     ResultSet rs = st2.executeQuery();
     int result = 0 ;
     while (rs.next()){
     result =  rs.getInt("id");
      }
     rs.close();
     st2.close();
     con2.close();
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
     
    String salesid  = addInvoiceHeader(null) + "";
     addInvoiceLines(salesid);
     

   }




   private void addInvoiceLines(String salesid) throws SQLException{

     Enumeration enumt = order.getOrderLines().elements();
     int counter = 0;
     while (enumt.hasMoreElements()){
       counter++;
       log.info("ORDER_LINE_COUNTER:" + counter);
       OrderLine orderLine = (OrderLine)enumt.nextElement();
       String itemid = orderLine.getToode();
       double kogus = orderLine.getKogus();
       double ale = orderLine.getDiscount();
       double hind = 0;

       String sql = "  SELECT pr.id AS kood FROM grarticle pr  WHERE   pr.code='" + itemid + "'  ";
 
           
         log.info("SALES_LINE_SQL: " + sql);
       System.out.println(sql);
       Connection con5 = DriverManager.getConnection(url + "safgr"
               , "mobile", "mobilemaag");
      
         Statement st = con5.createStatement();
         ResultSet rs = st.executeQuery(sql);

         if(rs.next()){
         	if (ale != 0){
                //  hind = rs.getDouble("hind")*(100-ale)/100*kogus;
         		  hind = orderLine.getPrice();
              }else {
            	  hind = 0;
              }
         	
         	sql = "INSERT INTO SDORow (idsdo,kauba_kood,kogus,hind,allapro,SAFkaubaID)" +
            " VALUES( " +
              salesid + ",'" +
              itemid + "'," +
              kogus + "," +
              hind +"," + ale + "," +   rs.getString("kood") + ")";
         	
         	
       System.out.println(sql);
       
            log.info("SALES_LINE_SQL: " + sql);
            Connection  con3 = DriverManager.getConnection(url + "importgr"
                    , "mobile", "mobilemaag");
             
               PreparedStatement st2 = con3.prepareStatement(sql);
               st2.executeUpdate();
               setAmount(false);
               addNumLine();
               st2.close();
               con3.close();
           }
         
         rs.close();
         st.close();
         con5.close();
        }
     
     
     String sql4 = "update SDO set staatus=1 where filename like '%MJ%' and ts > getdate() - 1";
     
     
     if(!"true".equals(parent.getProps().getProperty("testing"))){
    	 Connection con9 = DriverManager.getConnection(url + "importgr"
    	            , "mobile", "mobilemaag");
    	     PreparedStatement st2 = con9.prepareStatement(sql4);
    	     try{
    	     	 st2.executeUpdate();
    	     }catch(Exception e){
    	    	 try{
    	    		 st2.executeUpdate(); 
    	    	 } catch(Exception ee){
    	    		 st2.executeUpdate(); 
    	    	 }
    	     	
    	     }
    	    
    	     st2.close();
    	     con9.close(); 
     }
   
     
     
     
     }

   

   
   private int addInvoiceHeader(String salesid) throws SQLException{
	   	
boolean send = false;
boolean send2 = false;
	   	
	   	String agent = order.getAgent();	   	
	   	System.out.println("agent " + agent);
  	
	     String sql = "SELECT code , name FROM grcompany WHERE id=" + order.getCompany() + "";
	    // String idladu = order.getWarehouse();
	       log.info("addInvoiceHeader: " + sql);
	       Connection  con = DriverManager.getConnection(url + "safgr"
	               , "mobile", "mobilemaag");     
	      
	       Statement st = con.createStatement();
	       ResultSet rs = st.executeQuery(sql);
	       String text = order.getInfo();
	       String time = order.getShipTime();
	       String reference = order.getReference();
	       if(reference == null ){
	    	   reference = "";
	       }
	       
	       if (text == null){
	             text = "";
	              }
       if(time != null && time.trim().length() > 0){
    	   text = (text +  " "+ time).trim() ;
       }
       
       
       String KT = "";
       String KOMPL = "";
       String ARVE = "";
       String com = text;
       System.out.println(com);
       if(com != null && com.trim().length()>0){
       if(com.indexOf("KT:")!=-1 && com.indexOf("KM:") != -1){
             	KT = com.substring(com.indexOf("KT:") +3 ,com.indexOf("KM:"));
       } else if(com.indexOf("KT:")!=-1 && com.indexOf("AR:") != -1){
       	    KT = com.substring(com.indexOf("KT:")+3,com.indexOf("AR:"));
       } else if(com.indexOf("KT:")!=-1){
       	    KT = com.substring(com.indexOf("KT:")+3,com.length());
       }

       if(com.indexOf("AR:")!=-1 && com.indexOf("KM:") != -1){  
       	KOMPL = com.substring(com.indexOf("KM:" )+3,com.indexOf("AR:")); 	
         }else if(com.indexOf("KM:") != -1){
         	KOMPL = com.substring(com.indexOf("KM:" )+3,com.length()); 	
         }    

       if(com.indexOf("AR:")!=-1 ){  
       	ARVE = com.substring(com.indexOf("AR:" )+3,com.length());
         }

       }
       System.out.println("KT:" + KT + "KOMPL:" + KOMPL + "ARVE:" + ARVE );

       
       
	   String veoring = order.getShipMode();
	   if(veoring != null && veoring.trim().length() > 0 ){
		   veoring = veoring.trim();
		   if(veoring.contains("|")) {
			   veoring = veoring.substring(0,veoring.indexOf("|"));
		   }
		   try{
			   int aa = Integer.parseInt(veoring);
		   }catch(Exception e){
			   veoring = veoring.substring(1);
		   }
	   }else{
		   veoring = "";
	   }
	   
	   String custCode = "";
       String custName = "";
       
	       while(rs.next()){
	          custCode = rs.getString("code").trim();
	          custName = rs.getString("name").trim();
	          
	          
	          sql = "INSERT INTO SDO (doknr,dok_kuup  ,tehingu_kuup ,tarne_kuup,firma_kood" + //
              "  ,sisestaja,lisainfo,staatus,veoring,filename,selgitus_laole) " +
              "VALUES ((SELECT MAX(id)+1  from SDO where filename like '%MJ%' ), cast(? as date) ,cast(? as date),cast(? as date)" +  // ,cast(? as date)
              ",'" + custCode + "','"  + agent + 
  			 "','" + KOMPL + " " +  ARVE + "' ,0,'" + veoring + "','MJ', '" + KT  + "')";

	     System.out.println("addInvoiceHeader2: " + sql);
	     Connection con6 = DriverManager.getConnection(url + "importgr"
	             , "mobile", "mobilemaag");

	         PreparedStatement stmt2 = con6.prepareStatement(sql);
	         
	         stmt2.setTimestamp(1,new Timestamp(order.getShipCal().getTimeInMillis()));
	         stmt2.setTimestamp(2,new Timestamp(order.getShipCal().getTimeInMillis()));
	         stmt2.setTimestamp(3,new Timestamp(order.getShipCal().getTimeInMillis()));
	    
	         stmt2.executeUpdate();
	         stmt2.close();
	         con6.close();
	         addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
	       }
	       rs.close();
	       st.close();
	       con.close();
	       
	       int aaa = this.getSalesid();
	       Enumeration enumt = order.getOrderLines().elements();
	       while (enumt.hasMoreElements()){
	    	   OrderLine orderLine = (OrderLine)enumt.nextElement();
		         String itemid = orderLine.getToode();
		       if(itemid.startsWith("6") || itemid.startsWith("07") || itemid.startsWith("015") || itemid.startsWith("013")) {
		    	   System.out.println("Saadame mailipaele toode "  + itemid);
		    	   send = true;
		       }
	       }
	       
	       System.out.println("Saadame mailipaele "  + send);
	       if(send){
	    	    sendCommentEmail(text,order.getDokNr(),custCode,custName, Converter.getYYYYpMMpDDpString(order.getShipCal().getTime()));	    		    	   
	       }
	       if(veoring.equals("305")){
	    	   sendShipmentEmail(text,order.getDokNr(),custCode,custName, "TLN KIIRABI",parent.getProps().getProperty("shipmodeemail"));	    		    	   
	       }
	       
	       if(veoring.equals("467")){
	    	   sendShipmentEmail(text,order.getDokNr(),custCode,custName, "TRT KIIRABI",parent.getProps().getProperty("shipmodeemailtartu"));	    		    	   
	       }
	       
	       return aaa;
	       
	   }
   

   private void sendShipmentEmail(String comment,String doknr,String custid, String nimi,String tarne, String mmm){
	  
	   if(mmm != null) {
	   try{
	   				Email mail = new Email(null);
		  		   
		  	  		mail.setSMTP(parent.getProps().getProperty("smtphost"));
		  	  		
		  	  	
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		mail.setSubjectFilesTo("Saabus tellimus MJ-" + doknr + " kliendilt: " + custid);
		  	  		
		  	  		mail.setBody("\nSaabus tellimus MJ-" +  doknr + " kliendilt " + custid + " VEORINGIGA: " + tarne 
		  	  				);
		  	  		mail.setSendNotice(false);
		  	  		StringTokenizer st = new StringTokenizer(mmm,";");
		  	  		
		  	  		while (st.hasMoreElements()){
		  	  		  mail.setFilesTo(st.nextToken());
		  	  	      mail.send();
		  	  		}
		  	  		
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
	   	}
	   }
	  
   private void sendCommentEmail(String comment,String doknr,String custid, String nimi,String tarne){
   	try{
   		
   	if(parent.getProps().getProperty("operatoremail") != null) {
   			Email mail = new Email(null);
	  		   
	  	  		mail.setSMTP(parent.getProps().getProperty("smtphost"));
	  	  		mail.setFilesTo(parent.getProps().getProperty("operatoremail"));
	  	  		mail.setFrom(parent.getProps().getProperty("from"));
	  	  		mail.setSubjectFilesTo("Saabus tellimus MJ-" + doknr + " kliendilt: " + custid);
	  	  		mail.setBody("\nSaabus tellimus MJ-" +  doknr + " kliendilt " + custid + " Tarnekuupäevaga: " + tarne 
	  	  				);
	  	  		mail.setSendNotice(false);
	  	  		mail.send();
   	}
	  			}catch(Exception e){
	  			e.printStackTrace();	
	  			}
	  	
   }


   public String getDriverVersion(){
     return "SAFMaagGrupp";
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
   
  
   private ImportPanel parent;
   private Order order = null;
   private int idid = 0;
   private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
  
 }

