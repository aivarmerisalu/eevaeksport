/*
 * Created on 13.09.2007
 */
package eevaeksport.imp.driver;

import java.sql.CallableStatement;

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

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;


 public class SkaitePL3ImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SkaiteEugImportDriver.class.getName());

   public SkaitePL3ImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
 //    loadPereKodirovka();
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
   
    

     }
     catch (Exception e) {
       parent.showError(e);
     }
   }

   

   
	private Connection getNewConnection() throws Exception {
		return DriverManager.getConnection(url + "EUGESTA"
	            ,  "M_Partner_1", "mpsk2002");
	}
	
  

   public String identifyCustomer(String id,String regnr)throws SQLException {
    String custid = null;
      
    String cmd = "select kodas as kood from Partneriai par INNER JOIN Adresai adr ON adr.adresoid = par.adresoid where Pastabos ='" + id + "'" ; 
   
    
    if(id != null && id.trim().length() > 19){
    	id = id.substring(0,19).trim();
    	if(id.startsWith("VERKI")){
    		id = "VERKI� G. 29, KORP";	
    	}  	
    }
  try{
	   Connection con = null;
	   try {
	   	con = this.getNewConnection();
	   } catch (Exception e) {
	   	// TODO Auto-generated catch block
	   	e.printStackTrace();
	   }
	    PreparedStatement st2 = con.prepareStatement(cmd);
	    ResultSet rs = st2.executeQuery();
	    System.out.println(cmd);
	  int cc = 0;
	    while (rs.next()){
	    	custid =  rs.getString("kood");
	    	cc++;
	     }
	    rs.close();
	    st2.close();
	    con.close();
	    
	  

	   
	    
	     cmd = "select kodas as kood from Partneriai par INNER JOIN Adresai adr ON adr.adresoid = par.adresoid where kodas ='" + id + "'" ;   
	     System.out.println(cmd);
	     Connection ccc = DriverManager.getConnection(url + "EUGESTA"  ,  "e_partner", "e_partner");
	      st2 = ccc.prepareStatement(cmd);
	      rs = st2.executeQuery();
	
	    while (rs.next()){
	    	custid =  rs.getString("kood");
	    	cc++;
	     }
	       
	    rs.close();
	    st2.close();
	    ccc.close();
	
	    
  }catch(Exception ee){
	  ee.printStackTrace();
  }

 	return custid;
   }
     
   public String identifyWare(String kood) throws SQLException {
	  String res = "VL00_LOG";
	  String sql = " SELECT s.Kodas FROM Config c INNER JOIN Config c2 ON c.ID = c2.Parent " +
	    "  INNER JOIN Sandeliai s ON c2.ItemValue_Int = s.SandeliaiId  WHERE c.ItemName = 'EQ_SVS_11'";
	  Connection ccc = DriverManager.getConnection(url + "EUGESTA"  ,  "e_partner", "e_partner");
		  PreparedStatement st2 = ccc.prepareStatement(sql);
		    ResultSet rs = st2.executeQuery();
		 
		    while (rs.next()){
		    	res =  rs.getString("kodas");
		     }
		
		    rs.close();
		    st2.close();
		    ccc.close();
	 
	  return res;
   }
   
   public int getInCases(String kood) throws SQLException {
		  String sql = "select UnitsInPack as kogus from asVwProduct where productcode = '"+ kood +"'";
		 int res = 1;
		  Connection ccc = DriverManager.getConnection(url + "EUGESTA"  ,  "e_partner", "e_partner");
		  PreparedStatement st2 = ccc.prepareStatement(sql);
		    ResultSet rs = st2.executeQuery();
 		 
		    while (rs.next()){
		    	res =  rs.getInt("kogus");
		     }
		
		    rs.close();
		    st2.close();
		    ccc.close();
		  return res;
	   }
   
 
   
  private String getFirmaByProd(String prodid){
	  String sql = "SELECT  max( k.kodas) AS kodas " +
     "   FROM Operacijos d  INNER JOIN Pard_BendraInf AS pbi  ON d.OperacijosId = pbi.OperacijosId  " +
   " INNER JOIN partneriai k ON k.PartneriaiId=pbi.PartneriaiId    " +
   " INNER JOIN PardavimuKunai  r  ON d.OperacijosId = r.OperacijosId " +
   " INNER JOIN prekes t ON t.prekesid =r.prekesid " +
    "  WHERE    t.Kodas = '"+ prodid +"' and " +
   " d.data > getdate()-10 and OperacijosTipoId in ('2000024') ";
	 		try{
	 			  Connection ccc = DriverManager.getConnection(url + "EUGESTA"  ,  "e_partner", "e_partner");
	 			  PreparedStatement st2 = ccc.prepareStatement(sql);
		 		    ResultSet rs = st2.executeQuery();
		 		    System.out.println(sql);
		 		 
		 		    if (rs.next()){
		 		    	String res  = rs.getString("kodas");
		 		    	if(res != null){
		 		    		 rs.close();
		 		 		    st2.close();
		 		 		    ccc.close();
		 		    		return res.trim();
		 		    	}
		 		    
		 		     }
		 		 
		 		    rs.close();
		 		    st2.close();
		 		    ccc.close();
	 		}catch(Exception e){
	 			e.printStackTrace();
	 		}
	 		
	  return null;
  }
  
   public String identifyCurrency(String kood) throws SQLException {
		  String sql = "SELECT valkod as kood FROM Partneriai AS part " +
 " where part.kodas='"+ kood +"'";
		  String res = "Lt";
		   Connection con = null;
		   try {
		   	con = this.getNewConnection();
		   } catch (Exception e) {
		   	// TODO Auto-generated catch block
		   	e.printStackTrace();
		   }
		  PreparedStatement st2 = con.prepareStatement(sql);
		    ResultSet rs = st2.executeQuery();
		 
		    while (rs.next()){
		    	res =  rs.getString("kood").trim();
		     }
		 
		    rs.close();
		    st2.close();
		    con.close();
		  return res;
	   }
   

   
   public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  
   public String identifyProduct(String ean, String prodID) throws SQLException{


  	String sql2 = "SELECT pr.kodas AS toode FROM Prekes pr LEFT JOIN PL_PREKES a on a.prekesid=pr.prekesid " +
        " where KAPKodas like '%" + ean + "' and pr.kodas not like '%*%' and EQ_SAVININKAS > 1";
  	String sql4 = "SELECT pr.kodas AS toode FROM Prekes pr LEFT JOIN PL_PREKES a on a.prekesid=pr.prekesid " +
    " where KAPKodas like '%" + ean + ".' and pr.kodas not like '%*%' and EQ_SAVININKAS > 1";
	String sql44 = "SELECT pr.kodas AS toode FROM Prekes pr LEFT JOIN PL_PREKES a on a.prekesid=pr.prekesid " +
    " where KAPKodas like '%" + ean + ",' and pr.kodas not like '%*%' and EQ_SAVININKAS > 1";
	String sql5 = "SELECT pr.kodas AS toode FROM Prekes pr LEFT JOIN PL_PREKES a on a.prekesid=pr.prekesid " +
    " where KAPKodas like '%" + ean + "' and pr.kodas not like '%*%' and EQ_SAVININKAS > 1";
  String sql6 = "SELECT pr.kodas AS toode  FROM Prekes pr LEFT JOIN prekiukapkodai a on a.prekesid=pr.prekesid LEFT JOIN PL_PREKES b on b.prekesid=pr.prekesid " +
   " where  a.KAPKodas = '" + ean + "' and EQ_SAVININKAS > 1";
  	
    String sql1 = "SELECT pr.kodas AS toode FROM Prekes pr LEFT JOIN PL_PREKES a on a.prekesid=pr.prekesid   " +
    " where pr.kodas = '"  + prodID + "' and EQ_SAVININKAS > 1";
   // log.info("1_LINE_SQL: " + sql1);
   // log.info("2_LINE_SQL: " + sql2);

	String sql3 = "SELECT pr.kodas AS toode FROM Prekes pr LEFT JOIN PL_PREKES a on a.prekesid=pr.prekesid " +
    " where KAPKodas = '" + prodID + "' and pr.kodas not like '%*%' and EQ_SAVININKAS > 1";

	
    String prod = null;
    Connection ccc = DriverManager.getConnection(url + "EUGESTA"  ,  "e_partner", "e_partner");
    Statement st1 = ccc.createStatement();
    ResultSet rs1 = st1.executeQuery(sql2);
    
    if(ean != null && ean.length() > 3){
   
    int counter = 0;
    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
      counter++;
    }

   
    if(counter > 1){
    	
    //	kui on rohkem kui yks siis peab olem ILMA PUNKITA KOOD MILLE J�RGI TUVASTAME 
      	 ccc = DriverManager.getConnection(url + "EUGESTA"  ,  "e_partner", "e_partner");
         st1 = ccc.createStatement();
         rs1 = st1.executeQuery(sql5);
        
         while (rs1.next()) {
           prod = rs1.getString("toode").trim();
         }

         rs1.close();
         st1.close();
         ccc.close();
         if(prod == null){
        	 //KUI POLE ILMAPUNKTITA KOODI SIIS TAGASTAME ET TOODET POLE OLEMAS
        //	 return null;
         }
         
    }
    rs1.close();
    st1.close();
    ccc.close();
   
    if(prod == null){
    	//koos punktiga toimub otsing
   	 ccc = DriverManager.getConnection(url + "EUGESTA"  ,  "e_partner", "e_partner");
     st1 = ccc.createStatement();
     rs1 = st1.executeQuery(sql4);
    
     while (rs1.next()) {
       prod = rs1.getString("toode").trim();
     }

     rs1.close();
     st1.close();
     ccc.close();
  
    }  
    

    if(prod == null){
    	//koos punktiga toimub otsing
   	 ccc = DriverManager.getConnection(url + "EUGESTA"  ,  "e_partner", "e_partner");
     st1 = ccc.createStatement();
     rs1 = st1.executeQuery(sql44);
    
     while (rs1.next()) {
       prod = rs1.getString("toode").trim();
     }

     rs1.close();
     st1.close();
     ccc.close();
  
    }  
    	//otsime abitabelist vastavust
   	 ccc = DriverManager.getConnection(url + "EUGESTA"  ,  "e_partner", "e_partner");
     st1 = ccc.createStatement();
     rs1 = st1.executeQuery(sql6);
    
     while (rs1.next()) {
       prod = rs1.getString("toode").trim();
     }

     rs1.close();
     st1.close();
     ccc.close();
  
    
    }
     
 
    counter = 0;
    if(prodID != null && (prod == null || prod.trim().length() == 0)){
    	 ccc = DriverManager.getConnection(url + "EUGESTA"  ,  "e_partner", "e_partner");
      st1 = ccc.createStatement();
      rs1 = st1.executeQuery(sql1);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
        counter++;
      }
      if(counter > 1){
    	  ccc.close();
      	return null;
      }
      rs1.close();
      st1.close();
      ccc.close();
      }
      
    counter = 0;
   
   
    if(prodID != null && (prod == null || prod.trim().length() == 0)){
    	 ccc = DriverManager.getConnection(url + "EUGESTA"  ,  "e_partner", "e_partner");
      st1 = ccc.createStatement();
      rs1 = st1.executeQuery(sql3);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
        counter++;
      }
      if(counter > 1){
    	  ccc.close();
      	return null;
      }
      rs1.close();
      st1.close();
      ccc.close();
      }
    System.out.println("prod1" + prod);
    
    return prod;
   }

   public void makeOrder(Order order) throws SQLException{
     setOrder(order);
     String refr = order.getDokNr();
    String add  =  parent.getProps().getProperty("add.ordernr");
    if(add == null){
    	add = "";
    }
    
  
   
     String doknr = add + order.getDokNr()+order.getCompany();
     if(order.getDokNr() == null){
    	 doknr =  order.getInfo()+order.getCompany();
    	  if( order.getInfo() == null){
    	    	doknr =( Calendar.getInstance().getTimeInMillis()+"" ).substring(5)+order.getCompany();
    	    }
    	 refr = "";
     }
     
     String aiai = parent.getProps().getProperty("testing");
     if(aiai != null && aiai.equals("true")){
    	 
    	  String text = order.getEmailBody();
          if (text == null){
                text = "";
                 }
          if(order.getShipMode() != null && order.getShipMode().trim().length() >0 ){
       	   text = order.getShipMode();
          }
        
   
    	  Enumeration enumr = order.getOrderLines().elements();	     
    	     while (enumr.hasMoreElements()){
    	    	this.addNumLine(); 
    	    	OrderLine orderLine = (OrderLine)enumr.nextElement(); 	 
    	    	
    	    	 String unit = orderLine.getUnit();
    	         String itemid = orderLine.getToode();
    	         double kogus = orderLine.getKogus();
    	        	
    	     	String agent = order.getAgent();
    	       	String firma = order.getTeostaja(); 
    	 //       System.out.println("Firma enne " + firma);
    	       	 firma = getFirma(firma,order.getCompany());
    	   // 	 System.out.println("Firma " + firma);
    	    	 
    	    	System.out.println("Testing "+ refr +" "+ doknr +  " " + firma + " " +  order.getCompany() + " " + itemid + " " + orderLine.getKogus()
    	    			+ " " + kogus + " hind " + orderLine.getBonus() / orderLine.getKogus() + " " + orderLine.getPrice());
    	  
    	     }
    	     
    	     
    	     
    	
    	     
    	       
     }else{
         String salesid = addInvoiceHeader(doknr,refr,order.getInfo()) + "";
         addInvoiceLines(salesid, doknr);
         finishOrder(salesid,doknr);	 
     }

   }


   private void finishOrder(String salesid,String doknr) throws SQLException{
	   
	 	
         String   // sql = "uspu_IE_PardavimaiRun  NULL," + salesid ;
       sql = " uspu_IE_ClearNeimportuoti   NULL,2," + salesid ;
              log.info("SALES_ORDER_FINISH_SQL: " + sql);
             // System.out.println("SALES_ORDER_FINISH_SQL: " + sql);
       
              Connection con = DriverManager.getConnection(url + "EUGESTA"  ,  "e_partner", "e_partner");
              CallableStatement st2 = con.prepareCall(sql);
              st2.executeUpdate(); 
              log.info("Finishing Order " + salesid + " success" );        
              st2.close();
              con.close();
          }

   private String partner = null;
   
     

   private void addInvoiceLines(String salesid,String originalnr) throws SQLException{

     Enumeration enumr = order.getOrderLines().elements();
     int counter = 0;
     
 	
     
     
     while (enumr.hasMoreElements()){
       counter++;
     //  log.info("ORDER_LINE_COUNTER:" + counter);
       OrderLine orderLine = (OrderLine)enumr.nextElement();
       String itemid = orderLine.getToode();
       //String ean = orderLine.getEAN();
       
       
       double kogus = orderLine.getKogus();
       double ale = orderLine.getDiscount();      
       String name = orderLine.getTooteNimi();
       String unit = orderLine.getUnit();
       String price = null;
       String asi = "1";
       String hindkm = "null";
       
       if(unit != null && unit.equals("DPA")){ 
    	   System.out.println(unit + " " + itemid + " enne " + kogus );
    	   if(orderLine.getBonus()> 1){
    		   kogus = kogus * orderLine.getBonus();
    		   System.out.println(unit + " " + itemid + " bonus " + kogus );
    	   }else{
    		 //  kogus = kogus * getInCases(itemid);
    		   System.out.println(unit + " " + itemid + " getinCases" + kogus );
    	   }  	   
    	       
              }
    
       if(order.getTeostaja() != null && order.getTeostaja().equals("D10001")){
    	asi = "0";  
    	price = (orderLine.getBonus()/orderLine.getKogus()) + "";
    	ale = 0;
    	if(orderLine.getPrice()>0){
    		// ale = orderLine.getPrice() + ""; 
    	}
       }
      
       if(partner != null && partner.equals("450PP04812")){
    	   asi = "0";  
       	price = (orderLine.getPrice()) + "";
       	ale = 0;
         System.out.println("hind pandud " + price); 
       }
       String ean = "null";
       if((itemid != null && itemid.trim().length()>0) || true){ //k�ik l�heb sisse true lisatud
    	
    	   name = name.replaceAll("'", "");
    	   name = name.replaceAll("\"", "");
    	   
    	 //  itemid= "";
    	   name = "";
    	   ean = orderLine.getEAN();
    	   
    	   String    sql = "declare @OpId INT \n"+
           " select @OpId = NULL \n" + 
           " exec uspi_IE_PardavimuKunai  @OpId Output ," + salesid + ",' '," +
		         "'" + itemid + "',null,null,''," + kogus + "," +
		           price + ",null,null,'" + name + "',null,null,null,"+ ean + ",null,null," + ale +
			             " ,null,null,null,null,null,null,null,null," + hindkm + "," +
			 " null,null,null,null," + asi + ",null,null,null,null,null,null,1 \n" +
			 " select @OpId " ;
    	   
    

              // log.info("SALES_LINE_SQL: " + sql);
               System.out.println("SALES_LINE_SQL: " + sql);
           
                Connection con = DriverManager.getConnection(url + "EUGESTA"  ,  "e_partner", "e_partner");
               CallableStatement st2 = con.prepareCall(sql);
              if(kogus !=0){
                  st2.executeQuery();  
                  setAmount(false);
                  addNumLine(); 
              }else{
            	  troubles.append("PRODUCT CANNOT BE RESOLVED PDF format : \n" 
 	     				 + "\t" + name +" Code:" + itemid + " amount: " + orderLine.getKogus() + " Unit: " + unit +  "\n\n");
              }
           
              // log.info("Added Line :" + salesid + " lineid:" + res +  " item:" + itemid +  " amount:" + kogus) ;
               //System.out.println("Added Line :" + salesid + " lineid:" + res + " item:" + itemid +  " amount:" + kogus) ;
                 
               st2.close();
               con.close();
           }  
     }
     
   }



 private String getFirma(String asi,String cust){
	 
	 if(asi != null && (asi.equals("Dz�kija, UAB") || asi.equals("DZU")) ){
		 return "450PP00716";
	 }
	 
	 if(asi != null && (asi.equals("AR Maistas, UAB")) ){
		 return "450PP05161";
	 }
	 
	 if(asi != null && asi.equals("Korbi Lithuania UAB")){
		 return "450PP04559";
	 }
	 
	 if(asi != null && asi.equals("D10001")){
		 return "450PP04615";
	 }
	 
	 if(cust!= null && cust.equals("450PP04615")){
		 return "450PP04615";
	 }
	 
	 if(cust!= null && cust.equals("450PP00716")){
		 return "450PP00716";
	 }
	 
	 if(cust!= null && cust.equals("450PP04559")){
		 return "450PP04559";
	 }
	 if(cust!= null && cust.equals("P450PP05161")){
		 return "P450PP05161";
	 }
	 
	 if(asi != null && asi.indexOf("Lomadis") != -1){
		 return "450PP04936";
	 }
	 
	
	 return asi ;
 }
   
   private int addInvoiceHeader(String doknr ,String refer,String info) throws SQLException{
   	
   	String agent = order.getAgent();
   	String firma = order.getTeostaja(); 
   	
    System.out.println("Firma enne " + firma);
   	 firma = getFirma(firma,order.getCompany());
	 System.out.println("Firma peale" + firma);
	 if(firma == null ){
		Enumeration enume = order.getOrderLines().elements();
		 while (enume.hasMoreElements()){
			 String prodid = ((OrderLine)enume.nextElement()).getToode();
			 if(prodid!= null && prodid.trim().length() != 0 && firma == null){
				 firma = getFirmaByProd(prodid);
			 }
		 }
		
	 }
	 System.out.println("Firma peale toodet " + firma);
	 partner = firma;
   	int result = 0;
       String text = order.getEmailBody();
       if (text == null){
             text = "";
              }
       if(order.getInfo() != null && order.getInfo().trim().length() >0 ){
    	   text = order.getInfo();
       }
     
    String ladu = identifyWare(order.getCompany());
    
 
    
    	
   		System.out.println("ladu ja agent ;" + ladu + " " + agent + " " + text + " " + order.getInfo());
    order.setAgentCode(agent);
	
    String currency = identifyCurrency(order.getCompany());   
    String tyyp = "53";
    
    if(refer.equals(text)){
    	text = "";
    }
    String opdate =  "null";
    
    Calendar cc = Calendar.getInstance();
 
    try{
    	   cc.setTimeInMillis(order.getShipCal().getTimeInMillis());
    	    
    }catch(Exception e){
    	e.printStackTrace();
    }
    
    System.out.println("shipTime '" + order.getShipCal() + "'" );
    
    opdate = "" + getDate(cc) + "";   	
    
    String sql = "declare @OpId INT \n"+
    "select @OpId = NULL \n" +
    "exec uspi_IE_PardavimuGalva @OpId Output,'" + doknr + "',110350," + tyyp + ",'" +opdate + "'," +
		
		"'" + firma + "',11,null,'" + currency + "'," +
		"'" + ladu + "','" + agent + "',null,null,null,null,null,null,null,null,null,null,35,'PL3'," +
		
		"null,null,null,null,null,null,'"+ refer+ "','" + text + "',null,null," +
		"null,null,null,null,null,null,null,null,null,null," +
		"'" + order.getCompany() + "','" +  getDate(order.getShipCal()) +  "',"
				+ "null,null,null,null,null,null,null,null,null,null,null,1	\n" +
		"select @OpId" ;


       log.info("addInvoiceHeader2: " + sql);
       //System.out.println(sql);
       Connection con = DriverManager.getConnection(url + "EUGESTA"  ,  "e_partner", "e_partner");
       CallableStatement  msProcedure = con.prepareCall( sql );
     
	 
         System.out.println("number: " + doknr);
         System.out.println("kuup�ev: " + getDate(order.getShipCal()));
         System.out.println("firma: " + order.getCompany());
         System.out.println("ladu: " + ladu);
         System.out.println("agent: " + agent + " " +  order.getAgent());
         System.out.println("text: " + text);
      ResultSet rs3 = msProcedure.executeQuery();
      if(rs3.next()){
      	result = rs3.getInt(1);
      }
      
       rs3.close();
       msProcedure.close();
       con.close();
       
       addLog("Added Order Header -> " + order.getCompany() + " given:" + result + " original;" + doknr +"\n");
         System.out.println("Added Order Header -> " + order.getCompany() + " given:" + result + " original:" + doknr );

       return result;
       
   }


   public String getDriverVersion(){
     return "SkaitePL3";
   }

   private String getDate(Calendar cal){
	   Calendar cc = Calendar.getInstance();
	   if(cc.get(Calendar.YEAR) > cal.get(Calendar.YEAR)){
		   cal = Calendar.getInstance();
	   }
   String yy = cal.get(Calendar.YEAR)+"";
   String mm = (cal.get(Calendar.MONTH)+1)+"";
   if(mm.length()==1){
   	mm = "0"+ mm ;
   }
   String dd = (cal.get(Calendar.DAY_OF_MONTH)) +"";
   if(dd.length()==0){
   	dd = "0" + dd;
   }
   return yy+"-"+mm+"-"+dd;
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
   private boolean isAmount = false;
   private int counter = 0;
   private StringBuffer troubles= new StringBuffer();

public String getTrouble() {
	// TODO Auto-generated method stub
	return troubles.toString();
}
 }

