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


 public class SAFWiruImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

   public SAFWiruImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
     //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
     
       con2 = DriverManager.getConnection(url + "import"
            , "sa", "Viruolu1");
       con3 = DriverManager.getConnection(url + "import"
    		   , "sa", "Viruolu1");
       con4 = DriverManager.getConnection(url + "saf"
    		   , "sa", "Viruolu1");
       con5 = DriverManager.getConnection(url + "saf"
    		   , "sa", "Viruolu1");
       con6 = DriverManager.getConnection(url + "import"
    		   , "sa", "Viruolu1");
       con8 = DriverManager.getConnection(url + "saf"
    		   , "sa", "Viruolu1");  
      
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

	   

	    String sql1 = "Select pr.code AS toode FROM grarticle pr " + 
	          " where pr.code='" + prodID + "'";
	   
  	String sql2 = "Select pr.code as toode FROM grarticle pr " +
          "  where U_ean_teg='" + ean + "' order by pr.code ";
	String sql4 = "Select pr.code as toode FROM grarticle pr " +
	          "  where U_ean_int='" + ean + "' order by pr.code ";


    log.info("1_LINE_SQL: " + sql1);
    log.info("2_LINE_SQL: " + sql2);

    String prod = null;
  Connection con5 = DriverManager.getConnection(url + "saf"
		   , "sa", "Viruolu1");
    Statement st1 = con5.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
//System.out.println("prod0" + prod);
    rs1.close();
    st1.close();
    
    if(prod == null || prod.trim().length() == 0){
      st1 = con5.createStatement();
      rs1 = st1.executeQuery(sql2);

      if (rs1.next()) {
        prod = rs1.getString("toode").trim();
      }

      rs1.close();
      st1.close();
    }
   
    
    if(prod == null || prod.trim().length() == 0){
        st1 = con5.createStatement();
        rs1 = st1.executeQuery(sql4);

        if (rs1.next()) {
          prod = rs1.getString("toode").trim();
        }
        rs1.close();
        st1.close();
    }
    
    con5.close();
    
    System.out.println("prod1xxx " + prod);
    

    return prod;
   }

   public void makeOrder(Order order) throws SQLException{
     setOrder(order);
     String salesid = getSalesid() + "";
     addInvoiceHeader(salesid);
     addInvoiceLines(salesid);
     String sql4 = "update tp set staatus=1 where ltrim(tellnr) = ?";
   
     
     Connection con9 = DriverManager.getConnection(url + "import"
            , "sa", "Viruolu1");
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
       "  WHERE hi.idgrpricelist=23 AND (arca.category='BUDGET'  OR arca.category='PDA' ) AND (articletype='P' OR articletype='G')  AND pr.code='" + itemid + "'  group by pr.id ";
           
         log.info("SALES_LINE_SQL: " + sql);

         Statement st = con5.createStatement();
         ResultSet rs = st.executeQuery(sql);
         

         while(rs.next()){
         	if (ale != 0){
                hind = rs.getDouble("hind")*(100-ale)/100*kogus;
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
   	

   	
   	String agent = order.getAgent();
    
   	
     String sql = "SELECT id,i_ladu FROM grcompany WHERE LTRIM(code)='" + order.getCompany() + "'";
     String idladu = order.getWarehouse();
       log.info("addInvoiceHeader: " + sql);
       
       Connection ccc = DriverManager.getConnection(url + "saf"
    		   , "sa", "Viruolu1");
       Statement st = ccc.createStatement();
       ResultSet rs = st.executeQuery(sql);
       String text = order.getInfo();
       String reference = order.getTeostaja();
       if(order.getStatus() > 10){
    	   reference = order.getDokNr();
       }
       if(reference == null ){
    	   reference = "";
       }
       
       if(reference.length() >49){
    	   reference = reference.substring(0,49);
       }
       System.out.println("reference " + reference);
       if (text == null){
             text = "";
              }

       while(rs.next()){
         String custCode = rs.getString("id").trim();
         String ladu = rs.getString("i_ladu");
         if(ladu == null){
         	ladu = "";
         }
         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
         
         	
         	 String sql2 = "SELECT cat.id AS agent FROM grcompany kl INNER JOIN grcompanycatrel rel ON rel.idgrcompany=kl.id " +
         	 		"INNER JOIN grcompanycat cat  " +
        "   ON cat.id=rel.idgrcompanycat WHERE cat.category='myygimees' AND ltrim(kl.id)>0 AND LTRIM(kl.code)='" 
         + order.getCompany() + "' ORDER BY ltrim(kl.name) " ;
	
            
               Statement st2 = con8.createStatement();
               ResultSet rs2 = st2.executeQuery(sql2);
               
               while(rs2.next()){
                 	ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(rs2.getString("agent"));
                }
               
         

               
          String agentid = " ";
               if(ag == null){
            	   agentid =   "576";
          } else if (ag.getID().equals("4")) {
            agentid = "822"; // 13 algava koodiga on teostajad...
          } else if (ag.getID().equals("3")) {
            agentid = "798";
          } else if (ag.getID().equals("10")) {
            agentid = "569";
          } else if (ag.getID().equals("9")) {
            agentid = "566";
          } else if (ag.getID().equals("8")) {
            agentid = "567";
          } else if (ag.getID().equals("6")) {
            agentid = "805";
          } else if (ag.getID().equals("7")) {
            agentid = "797";
          } else if (ag.getID().equals("11")) {
            agentid = "814";
          } else if (ag.getID().equals("565")) {
            agentid = "565";
          } else if (ag.getID().equals("15")) {
            agentid = "844";
          } else if (ag.getID().equals("16")) {
            agentid = "850";
          }else if (ag.getID().equals("20")) {//mare
            agentid = "855";
          }else if (ag.getID().equals("22")) {//karike
            agentid = "969";
          }else if (ag.getID().equals("21")) {//pillet
            agentid = "861";
          }else if (ag.getID().equals("19")) {//moonika
            agentid = "859";
          }else if (ag.getID().equals("18")) {//silvar
            agentid = "858";
          }else if (ag.getID().equals("24")) {//anzelika
            agentid = "873";
          }else if (ag.getID().equals("26")) {//Mart
            agentid = "901";
          }else if (ag.getID().equals("27")) {//Imre
            agentid = "913";
          }else if (ag.getID().equals("31")) {//Helen
            agentid = "938";
          }else if (ag.getID().equals("HOUSE")) {//HMaja
            agentid = "938";
          }else if (ag.getID().equals("34")) {//HMaja
            agentid = "970";
          }else if (ag.getID().equals("35")) {//HMaja
            agentid = "960";
          }else if (ag.getID().equals("38")) {//Ando
            agentid = "995";
          }else if (ag.getID().equals("39")) {//Peeter
            agentid = "997";
          }else if (ag.getID().equals("40")) {//marko
            agentid = "998";
          }else if (ag.getID().equals("52")) {//kalle br�gel
            agentid = "1041";
          }else if (ag.getID().equals("53")) {//indrek Karp
            agentid = "1049";
          } else if (ag.getID().equals("54")) {//Anu Sepp
              agentid = "1063";
            } else if (ag.getID().equals("55")) {//Rain Lahe
              agentid = "1064";
            } else if (ag.getID().equals("56")) {//Rain Lahe
              agentid = "1069";
            }else if (ag.getID().equals("59")) {//Evo Sepp
              agentid = "1095";
            }else if (ag.getID().equals("63")) {//Rasmus
              agentid = "1125";
            }else if (ag.getID().equals("65")) {//Diana Palusaar
              agentid = "1126";
            }else if (ag.getID().equals("67")) {//Diana Palusaar
                agentid = "1178";
             }else if (ag.getID().equals("68")) {//Rainer Preemet
                  agentid = "1179";
             }else if (ag.getID().equals("72")) {//Mare Kreisberg 
                 agentid = "1209";
             }else if (ag.getID().equals("77")) {//egon 
             agentid = "1222"; // siin k�ib ID mitte kood 
              }else if (ag.getID().equals("78")) {//egon 
             agentid = "1226 "; // siin k�ib ID mitte kood 
              }
     
          
     
     
          

if(idladu != null){
	ladu = idladu;
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
System.out.println("KT:" + KT + "KOMPL:" + KOMPL + "ARVE:" + ARVE );


String agname = "noname";
if(ag != null){
	agname = ag.getName();
}
          sql = "INSERT INTO tp (tellnr,kuupaev,tahtaeg,kliendiID,kliendikood" +
              "  ,sisestajaID,sisestaja,lisainfo,laoid,teade_klienditeenindajatele,teade_lattu,teade_arvele,u_ostjatellimus) " +
              "VALUES (' " + salesid + "',getdate(),?" +
              ",?," + order.getCompany().replace('\'',' ') + ",'" + agentid.replace('\'',' ') + "','"  + agname.replace('\'',' ') +
             // "','MJ')";
  			 "','MJ " + text + "','" + ladu + "','" + KT +  "','" + KOMPL +  "','" + ARVE +  "', '" + reference + "')";

     log.info("addInvoiceHeader2: " + sql);
     System.out.println("addInvoiceHeader2: " + sql);
         PreparedStatement stmt2 = con6.prepareStatement(sql);
         stmt2.setString(2,custCode);
        // stmt2.setTimestamp(2, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
         stmt2.setTimestamp(1, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
         stmt2.executeUpdate();
         stmt2.close();
         addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
       }
       rs.close();
       st.close();
       ccc.close();

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

