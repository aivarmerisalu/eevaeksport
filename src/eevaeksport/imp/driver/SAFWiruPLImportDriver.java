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



 public class SAFWiruPLImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

   public SAFWiruPLImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
     //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
       con2 = DriverManager.getConnection(url + "importpl"
            , "aivarm", "Mobile2006");
       con3 = DriverManager.getConnection(url + "importpl"
            , "aivarm", "Mobile2006");
       con4 = DriverManager.getConnection(url + "safpl"
            , "aivarm", "Mobile2006");
       con5 = DriverManager.getConnection(url + "safpl"
            , "aivarm", "Mobile2006");
       con6 = DriverManager.getConnection(url + "importpl"
            , "aivarm", "Mobile2006");
       con8 = DriverManager.getConnection(url + "safpl"
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
    Connection con9 = DriverManager.getConnection(url + "importpl"
            , "aivarm", "Mobile2006");
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

     Enumeration enumr = order.getOrderLines().elements();
     int counter = 0;
     while (enumr.hasMoreElements()){
       counter++;
       log.info("ORDER_LINE_COUNTER:" + counter);
       OrderLine orderLine = (OrderLine)enumr.nextElement();
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
           String sql = "SELECT max(hi.price) AS hind ,  pr.id AS kood " +
       "  FROM grarticle pr RIGHT JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id RIGHT JOIN grpricelistrel hi ON " + 
       "  hi.idgrarticle=pr.id  RIGHT JOIN grarticlecat arca ON arca.id = cat.idgrarticlecat " +
       "  RIGHT JOIN grunit y ON y.id=pr.idgrunit   " +
       "  WHERE hi.idgrpricelist=1 AND (arca.category='BUDGET'  OR arca.category='PDA' ) AND (articletype='P' OR articletype='G')  AND pr.code='" + itemid + "'  group by pr.id ";
           
         log.info("SALES_LINE_SQL: " + sql);

         Statement st = con5.createStatement();
         ResultSet rs = st.executeQuery(sql);
         

         while(rs.next()){
         	if (ale != 0){
                hind = rs.getDouble("hind")*(1-ale)*kogus;
              }
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
   	
   	/*

    String itemid = ((OrderLine)order.getOrderLines().get(0)).getToode();
   String sql4 = "SELECT hi.price AS hind ,pr.id AS kood ,pr.iddrstore AS ladu FROM grarticle pr " +
       "   RIGHT JOIN grpricelistrel hi ON hi.idgrarticle=pr.id  " +
       "  WHERE hi.idgrpricelist=1  AND pr.code='" + itemid + "'";
         log.info("SALES_LINE_SQL: " + sql4);

Statement st3 = con.createStatement();
ResultSet rs4 = st3.executeQuery(sql4);
String id = "";
String code = "";

while (rs4.next()) {
  id = rs4.getString("ladu");
  if(id != null && id.trim().length()>0)
  	id = id.trim();
  if(id.equals("20")){
  	code = "6";
  }else{
  	code = "33";
  }
   
}
   	
   	String idladu = order.getDealer();
   	//siia sai juurde pandud kun on vaja nii kui nii teha
   	*/
   	
   	
   	String agent = order.getAgent();
    
   	
     String sql = "SELECT id,i_ladu FROM grcompany WHERE LTRIM(code)='" + order.getCompany() + "'";
     String idladu = order.getWarehouse();
       log.info("addInvoiceHeader: " + sql);
       Statement st = con4.createStatement();
       ResultSet rs = st.executeQuery(sql);
       String text = order.getEmailBody();
       
       if (text == null){
             text = "";
              }

       while(rs.next()){
         String custCode = rs.getString("id").trim();
         String ladu = rs.getString("i_ladu");
         if(ladu == null){
         	ladu = "";
         }
         

         if(idladu != null && !idladu.equals("0")){
         	ladu = idladu;
         }

         
         
         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
         if(agent.equals("565")){
         	
         	 String sql2 = "SELECT cat.id AS agent FROM grcompany kl INNER JOIN grcompanycatrel rel ON rel.idgrcompany=kl.id " +
         	 		"INNER JOIN grcompanycat cat  " +
        "   ON cat.id=rel.idgrcompanycat WHERE cat.category='myygimees' AND ltrim(kl.id)>0 AND LTRIM(kl.code)='" 
         + order.getCompany() + "' ORDER BY ltrim(kl.name) " ;
	
            
               Statement st2 = con8.createStatement();
               ResultSet rs2 = st2.executeQuery(sql2);
               
               while(rs2.next()){
                 	ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(rs2.getString("agent"));
                }
               }
         


          String agentid = " ";

          if (ag.getID().equals("13")) {
            agentid = "11"; // Rainer maam�gi
          } else if (ag.getID().equals("14")) {
            agentid = "9";//Aivar
          } else if (ag.getID().equals("11")) {
            agentid = "10";//Kr��t
          } else if (ag.getID().equals("12")) {
            agentid = "13";//ANU
          } else if (ag.getID().equals("9")) {
            agentid = "18";//pille kurrel
          } else if (ag.getID().equals("6")) {
            agentid = "7";//karike
          } else if (ag.getID().equals("5")) {
            agentid = "6";//pille talvere
          } else if (ag.getID().equals("13")) {
            agentid = "11";//rainer
          } else if (ag.getID().equals("4")) {
            agentid = "4";//mare
          } else if (ag.getID().equals("3")) {
            agentid = "3";//moonika 
          } else if (ag.getID().equals("2")) {
            agentid = "1";//silvar
          }else if (ag.getID().equals("8")) {
            agentid = "25";//mati pullat
          }else if (ag.getID().equals("10")) {
            agentid = "20";//indrek.pold@wiru.ee
          }else if (ag.getID().equals("17")) {
            agentid = "38";//anzelika@wiru.ee
          }else if (ag.getID().equals("19")) {
            agentid = "43";//mart.lainas@wiru.ee
          }else if (ag.getID().equals("20")) {
            agentid = "44";//mart.lainas@wiru.ee
          }
          
         



String KT = "";
String KOMPL = "";
String ARVE = "";
String com = text;
System.out.println(com);
if(com != null && com.trim().length()>0){
if(com.indexOf("KT:")!=-1 && com.indexOf("KM:") != -1){
      	KT = com.substring(com.indexOf("KT:"),com.indexOf("KM:"));
} else if(com.indexOf("KT:")!=-1 && com.indexOf("AR:") != -1){
	    KT = com.substring(com.indexOf("KT:"),com.indexOf("AR:"));
} else if(com.indexOf("KT:")!=-1){
	    KT = com.substring(com.indexOf("KT:"),com.length());
}

if(com.indexOf("AR:")!=-1 && com.indexOf("KM:") != -1){  
	KOMPL = com.substring(com.indexOf("KM:"),com.indexOf("AR:")); 	
  }else if(com.indexOf("KM:") != -1){
  	KOMPL = com.substring(com.indexOf("KM:"),com.length()); 	
  }    

if(com.indexOf("AR:")!=-1 ){  
	ARVE = com.substring(com.indexOf("AR:"),com.length());
  }

}
System.out.println("KT:" + KT + "KOMPL:" + KOMPL + "ARVE:" + ARVE);

          sql = "INSERT INTO tp (tellnr,kuupaev,tahtaeg,kliendiID,kliendikood" +
              "  ,sisestajaID,sisestaja,lisainfo,laoid,teade_klienditeenindajatele,teade_lattu,teade_arvele) " +
              "VALUES (' " + salesid + "',getdate(),?" +
              ",?," + order.getCompany().replace('\'',' ') + ",'" + agentid.replace('\'',' ') + "','"  + ag.getName().replace('\'',' ') +
             // "','MJ')";
  			 "','MJ " + text + "','" + ladu + "','" + KT +  "','" + KOMPL +  "','" + ARVE +  "')";

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

   
   public String getImportFile(){
    return "C:/Mobile Tellimusedpl/";
  }

   public String getDriverVersion(){
     return "SAFWiru";
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
   private Connection con8;
   private ImportPanel parent;
   private Order order = null;
   private int idid = 0;
   private boolean isAmount = false;
  
public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
 }

