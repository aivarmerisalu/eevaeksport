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


 public class SAFNurmiImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

   public SAFNurmiImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
     //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
     
       con2 = DriverManager.getConnection(url + "import"
            , "mobilejava", "!mobile2010");
       con3 = DriverManager.getConnection(url + "import"
            , "mobilejava", "!mobile2010");
       con4 = DriverManager.getConnection(url + "saf"
            , "mobilejava", "!mobile2010");
       con5 = DriverManager.getConnection(url + "saf"
            , "mobilejava", "!mobile2010");
       con6 = DriverManager.getConnection(url + "import"
            , "mobilejava", "!mobile2010");

     }
     catch (Exception e) {
       parent.showError(e);
     }
   }

   public String identifyCustomer(String id,String regnr)throws SQLException {
    String custid = null;
    
    String cmd = "SELECT kl.code FROM grcompany kl WHERE U_ImpordiFiliaal = '" + id + "'";   
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

  	String sql2 = "select pr.code as toode  FROM grarticle pr inner join grbarcode bc " +
  			" on bc.idgrarticle = pr.id where bc.code ='" + ean + "'";


    String sql1 = "Select pr.code AS toode FROM grarticle pr " + 
          " where pr.code='" + prodID + "'";
    log.info("1_LINE_SQL: " + sql1);
    log.info("2_LINE_SQL: " + sql2);

    String prod = null;
Connection ccc = DriverManager.getConnection(url + "saf"
        , "mobilejava", "!mobile2010");
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
     String salesid = getSalesid() + "";
     addInvoiceHeader(salesid);
     addInvoiceLines(salesid);
     String sql4 = "update tp set staatus=1 where ltrim(tellnr) = ?";
   
     
     Connection con9 = DriverManager.getConnection(url + "import"
            , "mobilejava", "!mobile2010");
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




   public String getLadu(int kood) throws SQLException{
	     String cmd = "SELECT code  from drstore  where id = " + kood + "";   
	     PreparedStatement st2 = con4.prepareStatement(cmd);
	     ResultSet rs = st2.executeQuery();
	     String result = "" ;
	     while (rs.next()){
	     result =  rs.getString("code");
	      }
	      rs.close();
	      st2.close();
	     return result;
	   }
   
   
   public boolean isTarvik(String kood) throws SQLException{
	   
	     String cmd = "SELECT pr.code AS toode,cc.category FROM  grarticle pr    " +
    	     "  INNER JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id INNER JOIN grarticlecat cc on cc.id =cat.idgrarticlecat   " +		
    		 " WHERE cc.category in ( 'TARVIKUD') and pr.code = '" + kood + "'";   
	     PreparedStatement st2 = con4.prepareStatement(cmd);
	     ResultSet rs = st2.executeQuery();
	     boolean res = false;
	     while (rs.next()){
	    	 res = true;
	      }
	     rs.close();
	     st2.close();
	  
	     return res;
	   }
 
   
   public int getSortLadu( String toode ,double kogus ) throws SQLException{
	   	   
       /*
        * Kui tuleb Tarvik - tallinn saku  kui ei siis hulgi kui ei ole siis saku 
        *                     */
	   
	   String cmd2 = " SELECT cc.category as cat  FROM dcurbalancestore ss  " +
    		" INNER JOIN grarticle pr ON idgrarticle = pr.id    " +
    	    "   INNER JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id INNER JOIN grarticlecat cc on cc.id =cat.idgrarticlecat  " +
			"  RIGHT JOIN grpricelistrel hi ON hi.idgrarticle=pr.id   " +
    		"  WHERE ss.iddrstore in (32) AND quantity >0 and hi.idgrpricelist in (7,39,40,9,13,16,17,18)    " +
    		" and  cc.category in ( 'TARVIKUD') and pr.code = '" + toode + "' and quantity >="+ kogus ;   
	     PreparedStatement st2 = con4.prepareStatement(cmd2);
	     ResultSet rs = st2.executeQuery();
	     int result = 12 ;
	     
	     while (rs.next()){
	    	
	        result =  32;
	      }
	  
	     rs.close();
	     st2.close();
	     
	     
	     if(result == 12 ){ // kui ei olnud saku laos siis otsi hulgilaos    
	   cmd2 = " SELECT cc.category as cat  FROM dcurbalancestore ss  " +
 		" INNER JOIN grarticle pr ON idgrarticle = pr.id    " +
 	    "   INNER JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id INNER JOIN grarticlecat cc on cc.id =cat.idgrarticlecat  " +
			"  RIGHT JOIN grpricelistrel hi ON hi.idgrarticle=pr.id   " +
 		"  WHERE ss.iddrstore in (1) AND quantity >0 and hi.idgrpricelist in (7,39,40,9,13,16,17,18)    " +
 		" and  cc.category in ( 'TARVIKUD') and pr.code = '" + toode + "' and quantity >="+ kogus ;   
	
		
	   st2 = con4.prepareStatement(cmd2);
	      
	   rs = st2.executeQuery();
	  
	     
	     if (rs.next()){    	 
	          result =  1;
	      }else{
	    	  result = 32;
	      }
	  
	     rs.close();
	     st2.close();
	     
	     
	  }
	     System.out.println("Lao otsing " + result );
	     
	     return result;
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
       int laoid = 12;//vaikimisi market ladu
       String laokood = "market";
       if(order.getWarehouse()!=null && order.getWarehouse().trim().length() > 0 ){
    	   laoid = Integer.parseInt(order.getWarehouse());
    	   
    	   System.out.println("laoid tellimuses " + laoid);
    	   if(laoid == 1){
    		   laoid = 12;
    	   }
    	   laokood = getLadu(laoid);
       }
       
       if (laoid == 12 && isTarvik(itemid)){
    	   System.out.println("on tarvik ");
    	   laoid = getSortLadu(itemid ,kogus);
    	   laokood =getLadu(laoid);
       }
       String agent = order.getAgent();
       
       if( agent.equals("HOUSE")){
    	   laoid= 1;
    	   laokood =getLadu(laoid);
       }
       
       System.out.println("laoid " + laoid);
       double hind = 0;
           /*String sql3 = "SELECT MAX(tp_id)AS idx FROM tp ";
           Statement st3 = con3.createStatement();
           ResultSet rs3 = st3.executeQuery(sql3);
           while (rs3.next()) {
           tpid = rs3.getInt("idx");
           }*/
       
       /*
        * Kui tuleb Tarvik - tallinn saku  kui ei siis hulgi kui ei ole siis saku 
        *                     */

          
           
           String sql = " SELECT max(hi.price) AS hind  ,pr.id AS kood" +
        " FROM grarticle pr inner join grbarcode bc on bc.idgrarticle = pr.id and bc.idgrunit=pr.idgrunit " +
		"  RIGHT JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id RIGHT JOIN grpricelistrel hi ON " +
        " hi.idgrarticle=pr.id  RIGHT JOIN grarticlecat arca ON arca.id = cat.idgrarticlecat " +
        " RIGHT JOIN grunit y ON y.id=pr.idgrunit   " +
        " WHERE hi.idgrpricelist=4  AND (articletype='G' OR articletype='P')  AND pr.code='" + itemid + "'  group by pr.id ";
           
         log.info("SALES_LINE_SQL: " + sql);

         Statement st = con5.createStatement();
         ResultSet rs = st.executeQuery(sql);
         

         while(rs.next()){
         	if (ale != 0){
                  hind = rs.getDouble("hind")*(100-ale)/100*kogus;
              }else {
            	  hind = 0;
              }
         	sql = "INSERT INTO ts (tp_id,kaubaID,kaubakood,kogus,hind,laoid,laokood,allapro)" +
             " VALUES( " +
               idid + "," +
               rs.getString("kood") + ",'" +
               itemid + "'," +
               kogus + "," +
               hind +"," +laoid + ",'" + laokood +  "'," + ale + ")";
System.out.println(sql);
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
    
   	
   	System.out.println("agent " + agent);

    int teostaja = 0;
    
    if(agent.equals("TALL1")){
  	  teostaja = 78  ;//
    }

    if(agent.equals("TALL2")){
  	  teostaja = 200  ;//
    }
    

    if(agent.equals("TARTU")){
  	  teostaja = 73  ;//
    }

    if(agent.equals("HOUSE")){
  	  teostaja = 24  ;//
    }
    
	System.out.println("teostaja " + teostaja);
   	
   	
     String sql = "SELECT id FROM grcompany WHERE LTRIM(code)='" + order.getCompany() + "'";
    // String idladu = order.getWarehouse();
       log.info("addInvoiceHeader: " + sql);
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
         String custCode = rs.getString("id").trim();
            
          

          sql = "INSERT INTO tp (tellnr,kuupaev,tahtaeg,kliendiID,kliendikood" +
              "  ,sisestajaID,sisestaja,lisainfo,teostaja_id) " +
              "VALUES (' " + salesid + "',getdate(),getdate()" +
              ",?,'" + order.getCompany().replace('\'',' ') + "','','"  + //" + agentid.replace('\'',' ') + "  + ag.getName().replace('\'',' ')
  			 "','MJ " + text + "'," + teostaja  +" )";

     log.info("addInvoiceHeader2: " + sql);
     System.out.println("addInvoiceHeader2: " + sql);
         PreparedStatement stmt2 = con6.prepareStatement(sql);
         stmt2.setString(1,custCode);
        // stmt2.setTimestamp(2, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
         //stmt2.setTimestamp(1, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
         stmt2.executeUpdate();
         stmt2.close();
         addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
       }
       st.close();

   }


   public String getDriverVersion(){
     return "SAFNurmi";
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

