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
import java.util.Enumeration;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;


 public class SolcraftAutoImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

   public SolcraftAutoImportDriver(ImportPanel parent) {
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
   
   private Connection getNewCon() throws SQLException{
	      return DriverManager.getConnection(url, "SYSDBA", "mustangs");
	    }

   public String identifyCustomer(String id,String regnr)throws SQLException {
    String custid = null;
   	return custid;
   }

   
   public String identifyProduct(String ean, String prodID) throws SQLException{
    return prodID;
   }

   public void makeOrder(Order order) throws SQLException{
     setOrder(order);
     
     int salesid = addInvoiceHeader(null) ;
   if(salesid != -1){
	
     addInvoiceLines(salesid +"");
    
     
     String sql4 = "update MS_ORDERHEADER set ISREADYFORIMPORT=1 where DOCID = ?";
     Connection con = this.getNewCon();
     PreparedStatement st2 = con.prepareStatement(sql4);
     st2.setString(1, salesid+"");
     try{
     	 st2.executeUpdate();
     }catch(Exception e){
    	 try{
    		 st2.executeUpdate(); 
    	 } catch(Exception ee){
    		 
    		 
    		 try{
        		 st2.executeUpdate(); 
        	 } catch(Exception er){
        		 
        	 }
    	 }
     }
     
     
     try {
    	 sql4 = "update MS_ORDERHEADER set ISREADYFORIMPORT=1 where ORDERDATE > CURRENT_TIMESTAMP - 1 and ISREADYFORIMPORT is null ";
    	 st2 = con.prepareStatement(sql4);
    	 st2.executeUpdate();
     }catch(Exception e) {
    	 e.printStackTrace();
     }
     
     st2.close();
     con.close();
     }
   }




   private void addInvoiceLines(String salesid) throws SQLException{

     Enumeration enums = order.getOrderLines().elements();
     int counter = 0;
     Connection con3 = this.getNewCon();
     while (enums.hasMoreElements()){
       counter++;
       log.info("ORDER_LINE_COUNTER:" + counter);
       OrderLine orderLine = (OrderLine)enums.nextElement();
       String itemid = orderLine.getToode();
       double kogus = orderLine.getKogus();
       double hind = orderLine.getPrice();
       double ale = orderLine.getDiscount();
    

       int changed = 0;
        	if(ale > 0){
        		changed = 1;
        	}
         
         	String sql = "INSERT INTO MS_ORDERLINES (DOCID,LINENUM,PRODID,AMOUNT,PRICE,DISCOUNT,UNIT,D_CHANGED, P_CHANGED )" +
             " VALUES( " +
               salesid + "," +
               counter + ",'" +
               itemid + "'," +
               kogus + "," +
               hind +"," + ale + ",'" + orderLine.getUnit() + "'," +changed + ",0" + ")";
System.out.println(sql);
               log.info("SALES_LINE_SQL: " + sql);
               
               PreparedStatement st2 = con3.prepareStatement(sql);
               st2.executeUpdate();
               setAmount(false);
               addNumLine();
               st2.close();
        }
     con3.close();
     }




   private int  addInvoiceHeader(String salesid) throws SQLException{
 boolean entered  = false;  	
   	String agent = order.getAgent();
   	System.out.println("agent " + agent);
   	
    
	  if(agent.equals("Marcis")) {
		  agent = "Mārcis";
	}
    String cc = order.getCompany();
    String cust = cc.substring(0, cc.indexOf("-"));
    String adr = cc.substring(cc.indexOf("-")+1);
    

	String sql = "SELECT kods FROM adreses WHERE (pkonts)='" + cust + "'";

	log.info("addInvoiceHeader: " + sql);
	Connection con = this.getNewCon();
       Statement st = con.createStatement();
       ResultSet rs = st.executeQuery(sql);
       String text = order.getInfo();
       
       String reference = order.getTeostaja();
       if(reference == null ){
    	   reference = "";
       }
       
       if (text == null){
             text = "";
              }else{
            	  if(text.length()> 240){
            		  text = text.substring(0,240);
            	  }
              }
       
String ladu = "09";
if(order.getWarehouse() != null && order.getWarehouse().trim().length() > 0){
	ladu = order.getWarehouse();
}
       
       while(rs.next()){
         String custCode = rs.getString("kods").trim();
         
 
          sql = "INSERT INTO MS_ORDERHEADER (WAREHOUSE,DOCNUMBER,ORDERDATE,DELIVERYDATE,CUSTID,ADRID" +
              "  ,SALESPERSON,COMM,REFERENCE) " +
              "VALUES ('" + ladu  + "','" + order.getDokNr()+ "', CURRENT_TIMESTAMP ,? " +  // " + salesid + "
              "," + custCode + "," + adr + ",'"   + agent + 
  			 "','" + text + "','" + reference + "' )";

     log.info("addInvoiceHeader2: " + sql);
     System.out.println("addInvoiceHeader2: " + sql);
     Connection con2 = this.getNewCon();
         PreparedStatement stmt2 = con2.prepareStatement(sql);
         stmt2.setTimestamp(1, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
         stmt2.executeUpdate();
         stmt2.close();
         con2.close();
         entered = true;
         addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
       }
       st.close();
       con.close();
       if(entered){
    	   return this.getSalesid();
    	}else{
    	   return -1;
    	   
       }
       
   }

   public int getSalesid() throws SQLException{
	     String cmd = "SELECT max (DOCID) AS id from MS_ORDERHEADER "; 
	     Connection con = this.getNewCon();
	     PreparedStatement st2 = con.prepareStatement(cmd);
	     ResultSet rs = st2.executeQuery();
	     int result = 0 ;
	     while (rs.next()){
	     result =  rs.getInt("id");
	      }
	     rs.close();
	     st2.close();
	     con.close();
	     return result;
	   }

   public String getDriverVersion(){
     return "SolcraftAuto";
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

   private String url = "jdbc:odbc:solcraft32";
   
   private ImportPanel parent;
   private Order order = null;
   private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
  
 }

