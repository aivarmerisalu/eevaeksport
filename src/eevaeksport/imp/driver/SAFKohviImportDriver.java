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
import java.util.Calendar;
import java.util.Enumeration;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;


 public class SAFKohviImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

   public SAFKohviImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
     //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
       con2 = DriverManager.getConnection(url + "import"
            , "mobile", "mob7kp");
       con3 = DriverManager.getConnection(url + "import"
            , "mobile", "mob7kp");
       con4 = DriverManager.getConnection(url + "saf"
            , "mobile", "mob7kp");
       con5 = DriverManager.getConnection(url + "saf"
            , "mobile", "mob7kp");
       con6 = DriverManager.getConnection(url + "import"
            , "mobile", "mob7kp");
      
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

   public String identifyCustomer(String id,String regnr)throws SQLException {
    String custid = null;
    
    String cmd = "SELECT kl.code FROM grcompany kl WHERE U_impfiliaal = '" + id + "'";   
    PreparedStatement st2 = con4.prepareStatement(cmd);
    ResultSet rs = st2.executeQuery();
 
    while (rs.next()){
    	custid =  rs.getString("code");
     }
    if(custid == null){
    	   String cmd2 = "SELECT kl.code FROM grcompany kl WHERE  id ='" + id + "'";   
    	    PreparedStatement st22 = con4.prepareStatement(cmd2);
    	    ResultSet rs2 = st22.executeQuery();	 
    	    while (rs2.next()){
    	    	custid =  rs2.getString("code");
    	     }
    }
    
   	return custid;
   }

   public int getSalesid() throws SQLException{
     String cmd = "SELECT MAX(tp_id)+1 AS id from tp ";   
     PreparedStatement st2 = con2.prepareStatement(cmd);
     ResultSet rs = st2.executeQuery();
     int result = 0 ;
     while (rs.next()){
     result =  rs.getInt("id");
      }
     this.idid = result;
     return result;
   }

   public String identifyProduct(String ean, String prodID) throws SQLException{

  	String sql2 = "Select pr.code as toode FROM grarticle pr " +
          "  where U_ean_teg='" + ean + "'";


    String sql1 = "Select pr.code AS toode FROM grarticle pr " + 
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
    
    if(prod == null || prod.trim().length() == 0){
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
     String sql4 = "update tp set staatus=1 where ltrim(tellnr) = ?";
    Connection con9 = DriverManager.getConnection(url + "import"
            , "mobile", "mob7kp");
     PreparedStatement st2 = con9.prepareStatement(sql4);
     st2.setString(1, salesid.trim());
     st2.executeUpdate();
     st2.close();
     con9.close();

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
      
       double hind = 0;

           
           String sql = "SELECT  pr.id AS kood " +         	
         " FROM grarticle pr left JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id left JOIN grpricelistrel hi ON " +
         " hi.idgrarticle=pr.id  left JOIN grarticlecat arca ON arca.id = cat.idgrarticlecat " +
         " left JOIN grunit y ON y.id=pr.idgrunit where  pr.code='" + itemid + "' group by pr.id ";
           
         log.info("SALES_LINE_SQL: " + sql);

         Statement st = con5.createStatement();
         ResultSet rs = st.executeQuery(sql);
         

         while(rs.next()){
         
         	sql = "INSERT INTO ts (tp_id,kaubaID,kaubakood,kogus,hind)" +
             " VALUES( " +
               idid + "," +
               rs.getString("kood") + ",'" +
               itemid + "'," +
               kogus + "," +
               hind +")";

               log.info("SALES_LINE_SQL: " + sql);
               PreparedStatement st2 = con3.prepareStatement(sql);
               st2.executeUpdate();
               setAmount(false);
               addNumLine();
               st2.close();
           }
        }
     }


   private void addInvoiceHeader(String salesid) throws SQLException{
   	
   	
   	String agent = order.getAgent();
       	
     String sql = "SELECT id FROM grcompany WHERE LTRIM(code)='" + order.getCompany() + "'";
  
       log.info("addInvoiceHeader: " + sql);
       Statement st = con4.createStatement();
       ResultSet rs = st.executeQuery(sql);
       String text = order.getEmailBody();
       
       if (text == null){
             text = "";
              }

       String ladu = "";
       int laduid = 0;
       String teostaja = "";
       while(rs.next()){
         String custCode = rs.getString("id").trim();
         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
   
         
          String agentid = " ";
          if (ag.getID().equals("19")) {
           // agentid = "00014"; // 13 algava koodiga on teostajad...
            agentid = "19"; // 13 algava koodiga on teostajad...
            ladu = "150";
            laduid = 150;
            teostaja = "19";
            order.setShipCal(Calendar.getInstance().getTimeInMillis() + "");
          } else if (ag.getID().equals("28")) {
            //agentid = "00023";
        	  agentid = "28"; // 13 algava koodiga on teostajad...
        	  ladu = "150";
            laduid = 150;
            teostaja = "28";
            order.setShipCal(Calendar.getInstance().getTimeInMillis() + "");
          } else if (ag.getID().equals("13")) {
            //agentid = "00023";
        	  agentid = "13"; // 13 algava koodiga on teostajad...
        	  ladu = "150";
            laduid = 150;
            teostaja = "13";

            order.setShipCal(Calendar.getInstance().getTimeInMillis() + "");
          } else if (ag.getID().equals("HOUSE")) {
            //agentid = "00023";
        	  agentid = "25"; // 13 algava koodiga on teostajad...
        	  ladu = "150";
            laduid = 150;
            teostaja = "25";

            order.setShipCal(Calendar.getInstance().getTimeInMillis() + "");
          }
          
          
     


          
          if(ag.getID().endsWith("19") || ag.getID().endsWith("28")){
              sql = "INSERT INTO tp (tellnr,kuupaev,tahtaeg,kliendiID,kliendikood" +
              "  ,sisestajaID,sisestaja,lisainfo,laoid,laokood) " +
              "VALUES (' " + salesid + "',getdate(),?" +
              ",?," + order.getCompany().replace('\'',' ') + ",'" + teostaja.replace('\'',' ') + "','"  + ag.getName().replace('\'',' ') +
             // "','MJ')";
  			 "','MJ " + text + "'," + laduid +", '" + ladu+ "')";
        	  
          }else{
              sql = "INSERT INTO tp (tellnr,kuupaev,tahtaeg,kliendiID,kliendikood" +
              "  ,sisestajaID,sisestaja,lisainfo) " +
              "VALUES (' " + salesid + "',getdate(),?" +
              ",?," + order.getCompany().replace('\'',' ') + ",'" + agentid.replace('\'',' ') + "','"  + ag.getName().replace('\'',' ') +
             // "','MJ')";
  			 "','MJ " + text + "')";

          }


     log.info("addInvoiceHeader2: " + sql);
         PreparedStatement stmt2 = con6.prepareStatement(sql);
         stmt2.setString(2,custCode);
        // stmt2.setTimestamp(2, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
         stmt2.setTimestamp(1, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
         stmt2.executeUpdate();
         stmt2.close();
         addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
       }
       st.close();

   }


   public String getDriverVersion(){
     return "SAFKohvi";
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

