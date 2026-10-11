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
 

 public class SAFArtikWareImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SAFArtikImportDriver.class.getName());

   public SAFArtikWareImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
     //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
       con2 = DriverManager.getConnection(url + "import"
       		, "sa", "apr16ill");
       con3 = DriverManager.getConnection(url + "import"
       		, "sa", "apr16ill");
       con4 = DriverManager.getConnection(url + "saf"
       		, "sa", "apr16ill");
       con5 = DriverManager.getConnection(url + "saf"
       		, "sa", "apr16ill");
       con6 = DriverManager.getConnection(url + "import"
       		, "sa", "apr16ill");
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
            , "sa", "apr16ill");
     PreparedStatement st2 = con9.prepareStatement(sql4);
     st2.setString(1, getNewSalesid() + "");
     st2.executeUpdate();
     st2.close();
     con9.close();

   }






   private void addInvoiceLines(int salesid) throws SQLException{

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
           /*String sql3 = "SELECT MAX(tp_id)AS idx FROM tp ";
           Statement st3 = con3.createStatement();
           ResultSet rs3 = st3.executeQuery(sql3);
           while (rs3.next()) {
           tpid = rs3.getInt("idx");
           }*/

           String sql =   " SELECT pr.code AS kood,  rtrim(pr.code)  + ' ' + rtrim(pr.name) AS nimi," +
       	   " hi.price AS hind,U_ean AS EAN ,cat.idgrarticlecat AS grupp, " +
	       " pr.contentquantity AS tykke ,y.name AS yhik  "+
	       "  FROM grarticle pr RIGHT JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id RIGHT JOIN grpricelistrel hi ON " +
	       "  hi.idgrarticle=pr.id  RIGHT JOIN grarticlecat arca ON arca.id = cat.idgrarticlecat  " +
	       "  RIGHT JOIN grunit y ON y.id=pr.idgrunit    "+
	       "  WHERE hi.idgrpricelist=3 AND arca.category='Peagrupp'  AND (articletype='G' OR articletype='P')  AND pr.code='" + itemid + "'  ";
           
         log.info("SALES_LINE_SQL: " + sql);

         Statement st = con5.createStatement();
         ResultSet rs = st.executeQuery(sql);
         
         String kood= null;
         String nimi = orderLine.getTooteNimi();
               hind = 0;
         while(rs.next()){
         	kood = rs.getString("kood");  
         	if (ale != 0){
                hind = rs.getDouble("hind")*((100-ale)/100)*kogus;
              }     	
         	     	
           }
         
         if(kood == null || kood.trim().length()==0){
         	//kood = "vp puuduv artikkel";
         	itemid = "vp puuduv artikkel";
         }
         
         
         sql = "INSERT INTO ts (tp_id,kaubaID,kaubakood,kogus,hind,nimetus)" +
         " VALUES( " +
           salesid + "," +
           kood + ",'" +
           itemid + "'," +
           kogus + "," +
           hind +",'"
		   +nimi+
		   "')";

           log.info("SALES_LINE_SQL: " + sql);
           PreparedStatement st2 = con3.prepareStatement(sql);
           st2.executeUpdate();
           setAmount(false);
           addNumLine();
           st2.close();
         
         
         
        }
     }




   private void addInvoiceHeader(String salesid) throws SQLException{
   	
   	
   	System.out.println("doknr " + order.getDokNr());
   	String agent = order.getAgent();
   	
   	System.out.println("agent " +  agent);
    
   String  ladu = order.getWarehouse();
   if(ladu == null || ladu.equals(" ")){
   	ladu = "1";
   }
  
   
   Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
   
  	
    String sql = "SELECT k.id , er.code AS ag FROM grcompany k INNER JOIN eremployee er ON k.I_kliendihaldur=er.id" +
    		" WHERE LTRIM(k.code)='" + order.getCompany() + "'";

      log.info("addInvoiceHeader: " + sql);
      Statement st = con4.createStatement();
      ResultSet rs = st.executeQuery(sql);

      while(rs.next()){
        String custCode = rs.getString("id").trim();
        String agID = rs.getString("ag");
      
        if(agID == null){
        	agID = agent;
        }
  
        String name = " " ;
 		
        if(ag != null){
        	agID = agent;
        	name = ag.getName().replace('\'',' ');
        }
   
       sql = "select id from eremployee where code = '" + agID + "'";
          String agentID = " ";
          Statement st4 = con5.createStatement();
          ResultSet rs4 = st4.executeQuery(sql);

          while(rs4.next()){
            agentID = rs4.getString("id").trim();
            }

              /*   String text = order.getEmailBody();
                 		if (text != null && text.trim().length()>0){
                 			text = text.trim();
                 		}*/
       
   String tell = "";
   if (ag != null){
   	tell = "TELL";
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
                 		
                 		

          sql = "INSERT INTO tp (tellnr,kuupaev,tahtaeg,kliendiID,kliendikood" +
              "  ,sisestajaID,sisestaja,lisainfo,laoid,reference) " +
              "VALUES (' " + salesid + "',getdate(),?" +
              ",?,'" + order.getCompany().replace('\'',' ') + "','" + agentID.replace('\'',' ') + "','"  + name +
             // "','MJ')";
  			 "','MJ" + tell + text + text2 + "'," + ladu + ",'" + ref +  "')";

     log.info("addInvoiceHeader2: " + sql);
     System.out.println(sql);
         PreparedStatement stmt2 = con6.prepareStatement(sql);
         stmt2.setString(2,custCode);
        // stmt2.setTimestamp(2, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
         stmt2.setTimestamp(1, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
         stmt2.executeUpdate();
         stmt2.close();
         
         
         addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
         
         
         
      
      }

   }


   public String getDriverVersion(){
     return "SAFArtik";
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
  
   private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}

 }

