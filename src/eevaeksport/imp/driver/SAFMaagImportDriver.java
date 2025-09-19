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


 public class SAFMaagImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

   public SAFMaagImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
     //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
     
     
       String ur = parent.getProps().getProperty("dsn.url");
       if(ur != null) {
    	  // urlP = ur;
       }
      
    
     }
     catch (Exception e) {
       parent.showError(e);
     }
   }

   private String urlP = "SAFTEST";
   
   
   public String identifyCustomer(String id,String regnr)throws SQLException {
    String custid = null;
    
    String cmd = "SELECT kl.code FROM grcompany kl WHERE U_ImpordiFiliaal = '" + id + "'";   
    Connection con4 = DriverManager.getConnection(url + urlP
            , "mobile", "mobile");
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
    con4.close();
    
   	return custid;
   }

   public int getSalesid() throws SQLException{
     String cmd = "SELECT max (id) AS id from SDO where TS > getdate()-0.1 and  lisainfo like 'MJ%' ";   
     Connection con2 = DriverManager.getConnection(url + "import"
             , "mobile", "mobile");
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
Connection ccc = DriverManager.getConnection(url + urlP
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
     String salesid = "";//getSalesid() + "";
    salesid  = addInvoiceHeader(null) + "";
     addInvoiceLines(salesid);
     String sql4 = "update SDO set staatus=1 where ltrim(doknr) = ?";
   
     
     Connection con9 = DriverManager.getConnection(url + "import"
            , "mobile", "mobile");
     PreparedStatement st2 = con9.prepareStatement(sql4);
     st2.setString(1, salesid.trim());
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



   private void addInvoiceLines(String salesid) throws SQLException{

     Enumeration enums = order.getOrderLines().elements();
     int counter = 0;
     while (enums.hasMoreElements()){
       counter++;
       log.info("ORDER_LINE_COUNTER:" + counter);
       OrderLine orderLine = (OrderLine)enums.nextElement();
       String itemid = orderLine.getToode();
       double kogus = orderLine.getKogus();
       double ale = orderLine.getDiscount();
    
           
           String sql = " SELECT max(hi.price) AS hind  ,pr.id AS kood" +
        " FROM grarticle pr  " +
		"  RIGHT JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id RIGHT JOIN grpricelistrel hi ON " +
        " hi.idgrarticle=pr.id  RIGHT JOIN grarticlecat arca ON arca.id = cat.idgrarticlecat " +
        " RIGHT JOIN grunit y ON y.id=pr.idgrunit   " +
        " WHERE hi.idgrpricelist=1  AND (articletype='G' OR articletype='P')  AND pr.code='" + itemid + "'  group by pr.id ";
           
         log.info("SALES_LINE_SQL: " + sql);
         Connection con5 = DriverManager.getConnection(url + urlP
                 , "mobile", "mobile");
         Statement st = con5.createStatement();
         ResultSet rs = st.executeQuery(sql);
         
  double hind = 0;
         while(rs.next()){
         	if (ale != 0){
                  hind = rs.getDouble("hind")*(100-ale)/100*kogus;
              }else {
            	  hind = 0;
              }
         	sql = "INSERT INTO SDORow (idsdo,kauba_kood,kogus,hind,allapro,SAFkaubaID)" +
             " VALUES( " +
               idid + "," +
               itemid + "," +
               kogus + "," +
               hind +"," + ale + "," +   rs.getString("kood") + ")";
System.out.println(sql);
               log.info("SALES_LINE_SQL: " + sql);
               Connection con3 = DriverManager.getConnection(url + "import"
                       , "mobile", "mobile");
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
     }




   private int  addInvoiceHeader(String salesid) throws SQLException{
   	
   	String agent = order.getAgent();
   	System.out.println("agent " + agent);

	String sql = "SELECT id FROM grcompany WHERE LTRIM(code)='" + order.getCompany() + "'";

	log.info("addInvoiceHeader: " + sql);
	Connection con4 = DriverManager.getConnection(url + urlP
            , "mobile", "mobile");
       Statement st = con4.createStatement();
       ResultSet rs = st.executeQuery(sql);
       String text = order.getEmailBody();
       String reference = order.getReference();
       if(reference == null ){
    	   reference = "";
       }
       
       if (text == null){
             text = "";
              }

       
       while(rs.next()){
            
          agent = "telema";

          sql = "INSERT INTO SDO (doknr,dok_kuup,tarne_kuup,firma_kood" +
              "  ,sisestaja,lisainfo) " +
              "VALUES ((SELECT MAX(id)+1  from SDO), cast(getdate() as date) ,cast(? as date)" +  // " + salesid + "
              ",'" + order.getCompany().replace('\'',' ') + "','"  + agent + 
  			 "','MJ " + text + "' )";

     log.info("addInvoiceHeader2: " + sql);
     System.out.println("addInvoiceHeader2: " + sql);
     Connection con6 = DriverManager.getConnection(url + "import"
             , "mobile", "mobile");
         PreparedStatement stmt2 = con6.prepareStatement(sql);
        // stmt2.setString(1,custCode);
         stmt2.setTimestamp(1, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
         //stmt2.setTimestamp(1, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
         stmt2.executeUpdate();
         stmt2.close();
         con6.close();
         addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
       }
       rs.close();
       st.close();
       con4.close();
  return this.getSalesid();
   }


   public String getDriverVersion(){
     return "SAFMaag";
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

