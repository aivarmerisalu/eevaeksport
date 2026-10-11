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


 public class SAFWiruBillImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

   public SAFWiruBillImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
     //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
       con = DriverManager.getConnection(url + "via"
                                         , "aivarm", "Mobile2006");
       con2 = DriverManager.getConnection(url + "via"
            , "aivarm", "Mobile2006");
       con3 = DriverManager.getConnection(url + "via"
            , "aivarm", "Mobile2006");
       con4 = DriverManager.getConnection(url + "saf"
            , "aivarm", "Mobile2006"); //
       con5 = DriverManager.getConnection(url + "saf"
            , "aivarm", "Mobile2006");
       con6 = DriverManager.getConnection(url + "via"
            , "aivarm", "Mobile2006");
    
 /*
       POP3Reader pop3 = new POP3Reader(parent.getEksportFrame().getString(
           "moborder"));
       pop3.setPop3host(parent.getProps().getProperty("pop3.host"));
       pop3.setUser(parent.getProps().getProperty("pop3.user"));
       pop3.setPassword(parent.getProps().getProperty("pop3.password"));
       pop3.setSaveDir(getImportFile());
       pop3.readFiles(); 
           
       */

     }
     catch (Exception e) {
       parent.showError(e);
     }
   }

   public String getImportFile(){
  	String ret = parent.getProps().getProperty("imp.read");
  	if (ret == null){
  		ret = "C:/Mobile Tellimused/";
  	}
    return ret;
  }
   
   public String identifyCustomer(String id,String regnr)throws SQLException {
    String custid = null;
    
    String cmd = "SELECT kl.code FROM grcompany kl WHERE U_viakoods = '" + id + "'";   
    PreparedStatement st2 = con4.prepareStatement(cmd);
    ResultSet rs = st2.executeQuery();
 
    while (rs.next()){
    	custid =  rs.getString("code");
     }
    
   	return custid;
   }

   public int getSalesid() throws SQLException{
     String cmd = "SELECT MAX(id) AS id from sl ";   
     PreparedStatement st2 = con2.prepareStatement(cmd);
     ResultSet rs = st2.executeQuery();
     int result = 0 ;
     while (rs.next()){
     result =  rs.getInt("id");
      }
     return result;
   }

   public String identifyProduct(String ean, String prodID) throws SQLException{

  	String sql1 = "Select pr.code as toode FROM grarticle pr " +
          "  where U_ean_teg='" + ean + "'";


    String sql2 = "Select pr.code AS toode FROM grarticle pr " + 
          " where pr.code='" + prodID + "'";
    log.info("1_LINE_SQL: " + sql1);
    log.info("2_LINE_SQL: " + sql2);

    String prod = null;

    Statement st1 = con5.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
//System.out.println("prod0" + prod);
    //rs1.close();
   // st1.close();
    
    if(prod == null){
      st1 = con5.createStatement();
      rs1 = st1.executeQuery(sql2);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
      }
    }

    //System.out.println("prod1" + prod);
    rs1.close();
    st1.close();

    return prod;
   }

   public void makeOrder(Order order) throws SQLException{
     setOrder(order);
     String salesid = getSalesid() + "";
     addInvoiceHeader(salesid);
     addInvoiceLines(salesid);
     String sql4 = "update sl set staatus=1 where ltrim(doknr) = ?";
     PreparedStatement st2 = con.prepareStatement(sql4);
     st2.setString(1, order.getDokNr());
     st2.executeUpdate();

   }






   private void addInvoiceLines(String salesid) throws SQLException{

     Enumeration enums = order.getOrderLines().elements();
     int counter = 0;
     while (enums.hasMoreElements()){
       counter++;
       log.info("ORDER_LINE_COUNTER:" + counter);
       OrderLine orderLine = (OrderLine)enums.nextElement();
       String itemid = orderLine.getToode();
       double kogus = orderLine.getKogus();
       double hind = orderLine.getPrice();
     
         	String sql = "INSERT INTO  SLRida (slid,kood,nimi,yhik,kogus,kmprots,hind,summa)" +
             " VALUES( " +
               getSalesid() + ",'" +
               itemid + "','" +
               orderLine.getTooteNimi()+ "','" +
               orderLine.getUnit() + "'," +
               kogus + "," +
               order.getKm() + "," +
               hind  + "," +
			   kogus*hind+")";

               log.info("SALES_LINE_SQL: " + sql);
               PreparedStatement st2 = con3.prepareStatement(sql);
               st2.executeUpdate();
               setAmount(false);
               addNumLine();
               st2.close();
           }
        }
     




   private void addInvoiceHeader(String salesid) throws SQLException{
    	
       String text = order.getEmailBody();
       
       if (text == null){
             text = "";
              }
       
       double kogukas = order.getKogusumma()+ order.getKmsumma()+ order.getYmmardus(); 

          String sql = "INSERT INTO sl (doknr,kuup,ts,makseaeg,klient,lisainfo" +
              "  ,summa,kmsumma,kokku,ymardus) " +
              "VALUES ('" + order.getDokNr() + "',?,getdate()" +
              ",?," + order.getCompany().replace('\'',' ') + ",?," + order.getKogusumma() + ","  + order.getKmsumma() +
  			 "," + kogukas + "," + order.getYmmardus()
			 + ")";

     log.info("addInvoiceHeader2: " + sql);
     System.out.println("addInvoiceHeader2: " + sql);
         PreparedStatement stmt2 = con6.prepareStatement(sql);
         //stmt2.setString(3,custCode);
         stmt2.setTimestamp(2, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
         stmt2.setTimestamp(1, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
         stmt2.setString(3,text);
         stmt2.executeUpdate();
         stmt2.close();
         addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
       

   }


   public String getDriverVersion(){
     return "SAFWiruBill";
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
   private Connection con;
   private Connection con6;
   private Connection con2;
   private Connection con3;
   private Connection con4;
   private Connection con5;
   private ImportPanel parent;
   private Order order = null;

   private boolean isAmount = false;
  

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
 }

