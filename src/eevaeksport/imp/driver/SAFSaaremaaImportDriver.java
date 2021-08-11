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

import eevaeksport.Agent;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;



 public class SAFSaaremaaImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SAFArtikImportDriver.class.getName());

   public SAFSaaremaaImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
      
       con2 = DriverManager.getConnection(url + "import"
       		, "sa", "sql");
       con3 = DriverManager.getConnection(url + "import"
       		, "sa", "sql");
       con4 = DriverManager.getConnection(url + "saf"
       		, "sa", "sql");
       con5 = DriverManager.getConnection(url + "saf"
       		, "sa", "sql");
       con6 = DriverManager.getConnection(url + "import"
       		, "sa", "sql");
        
       con9 = DriverManager.getConnection(url + "saf"
            , "sa", "sql"); 
      
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
    
    String cmd = "SELECT kl.code FROM grcompany kl WHERE U_VPMarket = '" + id + "'";   
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
    //V�tame j�rgmise numbri  
   	String cmd = "SELECT MAX(tp_id)+1 AS id from tp ";   
     PreparedStatement st2 = con2.prepareStatement(cmd);
     ResultSet rs = st2.executeQuery();
     int result = 0 ;
     while (rs.next()){
     result =  rs.getInt("id");
      }
     rs.close();
     st2.close();
     
     
     String cmd2 = "SELECT MAX(tp_id) AS id from ts ";   
     PreparedStatement st3 = con2.prepareStatement(cmd2);
     ResultSet rs2 = st3.executeQuery();
     int res = 0 ;
     while (rs2.next()){
     res =  rs2.getInt("id");
      }
     rs2.close();
     st3.close();
     if(res == result){
     	result = result + 1;
     }
     
     
    // this. = result;
     return result;
   }

   
   public int getNewSalesid() throws SQLException{
    String cmd = "SELECT MAX(tp_id) AS id from tp ";   
    PreparedStatement st2 = con2.prepareStatement(cmd);
    ResultSet rs = st2.executeQuery();
    int result = 0 ;
    while (rs.next()){
    result =  rs.getInt("id");
     }
    
    return result;
  }
   
   public String identifyProduct(String ean, String prodID) throws SQLException{

  	String sql2 = "Select pr.code as toode FROM grarticle pr " +
          "  where U_ean='" + ean + "'";


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
     
     addInvoiceLines(getNewSalesid());
     String sql4 = "update tp set staatus=1 where ltrim(tp_id) = ?";
    Connection con9 = DriverManager.getConnection(url + "import"
            , "sa", "sql");
     PreparedStatement st2 = con9.prepareStatement(sql4);
     st2.setString(1, getNewSalesid() + "");
     st2.executeUpdate();
     st2.close();
     con9.close();

   }

   private void addInvoiceLines(int salesid) throws SQLException{

     Enumeration enums = order.getOrderLines().elements();
     int counter = 0;
     while (enums.hasMoreElements()){
       counter++;
       log.info("ORDER_LINE_COUNTER:" + counter);
       OrderLine orderLine = (OrderLine)enums.nextElement();
       String itemid = orderLine.getToode();
       double kogus = orderLine.getKogus();
       double ale = orderLine.getDiscount();
      
       double hind = 0;
           /*String sql3 = "SELECT MAX(tp_id)AS idx FROM tp ";
           Statement st3 = con3.createStatement();
           ResultSet rs3 = st3.executeQuery(sql3);
           while (rs3.next()) {
           tpid = rs3.getInt("idx");
           }*/

           String sql =   "    SELECT pr.id AS kood,  rtrim(pr.code)  + ' ' + rtrim(pr.name) AS nimi, hi.price AS hind " +
	     "   FROM grarticle pr RIGHT JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id RIGHT JOIN grpricelistrel hi ON  " +
        " hi.idgrarticle=pr.id RIGHT JOIN grpricelist pl ON pl.id=hi.idgrpricelist RIGHT JOIN grarticlecat arca ON  " +
		" arca.id = cat.idgrarticlecat   RIGHT JOIN grunit y ON y.id=pr.idgrunit  " +
	    "    WHERE   rtrim(pl.code)='Hinnakiri' AND (arca.category='Hinnakiri' or arca.category='suveHinnakiri') " +
	    "AND (pr.articletype='P' OR pr.articletype='G')" +
	       "  AND  pr.code='" + itemid + "'  ";
           
         log.info("SALES_LINE_SQL: " + sql);

         Statement st = con5.createStatement();
         ResultSet rs = st.executeQuery(sql);
         
         String kood= null;
               hind = 0;
         while(rs.next()){
         	kood = rs.getString("kood");  
         	if (ale != 0){
                hind = rs.getDouble("hind")*(1-ale)*kogus;
              }     	
         	     	
           }
         
         if(kood == null || kood.trim().length()==0){
         	itemid = "vp puuduv artikkel";
         }
         
         
         sql = "INSERT INTO ts (tp_id,kaubaID,kaubakood,kogus,hind)" +
         " VALUES( " +
           salesid + "," +
           kood + ",'" +
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





   private void addInvoiceHeader(String salesid) throws SQLException{


         String custCode = order.getCompany();
          String agent = order.getAgent();
          Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);

          String agentid = " ";
          
          String sql1 = "SELECT  id , code from eremployee where I_esindaja= '"+ agent + "'" ;
           
          Statement stm = con9.createStatement();
           ResultSet rr = stm.executeQuery(sql1);
           String agcode = " ";
           if(rr.next()){
             agentid = rr.getString("id"); 
             agcode = rr.getString("code"); 
           }
          
           rr.close();
          stm.close();
          
           String sql2 = "SELECT  id from grcompany where code= '"+ custCode + "'" ;
           
           stm = con9.createStatement();
           rr = stm.executeQuery(sql2);
           String id = "0";
            if(rr.next()){
            	id = rr.getString("id"); 
            }
            rr.close();
            stm.close();  
           
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
                 		
          String ware = "34";
          if(order.getWarehouse() != null && order.getWarehouse().trim().length()!= 0){
        	  ware = order.getWarehouse();
          }
          

        

          if(text.length()>125)text = text.substring(0,125);

         String sql = "INSERT INTO tp (tellnr,kuupaev,tahtaeg,kliendiID,kliendikood" +
              "  ,sisestajaID,sisestaja,lisainfo,laoid, selgitus) " +
              "VALUES (' " + salesid + "',getdate(),?" +
              "," + id + ",?,'" + agentid.replace('\'',' ') + "','"  + ag.getName().replace('\'',' ') +
             // "','MJ')";
  			 "','MJ " + text + agcode.replace('\'',' ') + "'," + ware + ",'"+ text2 + ref +  "')";

     System.out.println("addInvoiceHeader2: " + sql);
         PreparedStatement stmt2 = con6.prepareStatement(sql);
         stmt2.setTimestamp(1, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
         stmt2.setString(2,custCode);
         stmt2.executeUpdate();
         stmt2.close();
         addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
       

   }

   public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
   
   public String getDriverVersion(){
     return "SAFSaaremaa";
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
  
   private Connection con9;
   private ImportPanel parent;
   private Order order = null;
  
   private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
  
 }

