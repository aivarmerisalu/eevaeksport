/*
 * Created on 13.09.2007
 */
package eevaeksport.imp.driver;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;


 public class SkaiteEugImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SkaiteEugImportDriver.class.getName());

   public SkaiteEugImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
   //  loadPereKodirovka();
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
   
     }
     catch (Exception e) {
       parent.showError(e);
     }
   }

   


   private Hashtable kodirovka = null;
   
   public String identifyCustomer(String id,String regnr)throws SQLException {
    String custid = null;
 		

    if(regnr != null && regnr.equals("1111111")){
    	return    "240VL21448";
    }
    if(id != null && id.equals("DR021")){
    	return    "240�1495";
    }
    
    if(id != null && id.trim().length() > 19){
    	id = id.substring(0,19).trim();
    	if(id.startsWith("VERKI")){
    		id = "VERKI� G. 29, KORP";	
    	}  	
    	if(id.startsWith("Pramon") && id.indexOf("iau") != -1){
    		id = "Pramones g. 6, Sia";	
    	}  
    	
    	if(id.startsWith("Pramon") && id.indexOf("g.31") != -1){
    		id = "Pramones g.31";	
    	} 
    }else  if(id != null && id.startsWith("Pramon")){
    	if( id.indexOf("g.31") != -1){
    		id = "Pramones g.31";	
    	} 
    }
 
    
    

    

    String cmd = "select kodas as kood from Partneriai par INNER JOIN Adresai adr ON "
    		+ "adr.adresoid = par.adresoid where Pastabos ='" + id + "'" ; 
   
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
	 
	    if(custid == null && id.length() > 10){
	    	  String cmd4 = "select kodas as kood from Partneriai par INNER JOIN Adresai adr ON adr.adresoid = par.adresoid where Pastabos like '%" + id.substring(0,7) + "%'" ; 
	    	    
	  	    st2 = con.prepareStatement(cmd4);
	  	     rs = st2.executeQuery();
	  	    System.out.println(cmd4);
	  	     cc = 0;
	  	    while (rs.next()){
	  	    	custid =  rs.getString("kood");
	  	    	cc++;
	  	     }
	  	    rs.close();
	  	    st2.close();
	  	    
	  	  if(custid == null){
	  		  String cmd5 = "select kodas as kood from Partneriai par INNER JOIN Adresai adr ON adr.adresoid = par.adresoid where Pastabos like '" + id.substring(0,6) + "%'" ; 
	    	    
		  	    st2 = con.prepareStatement(cmd5);
		  	     rs = st2.executeQuery();
		  	    System.out.println(cmd5);
		  	     cc = 0;
		  	    while (rs.next()){
		  	    	custid =  rs.getString("kood");
		  	    	cc++;
		  	     }
		  	    rs.close();
		  	    st2.close();
		  	    
	  	  }
	  	  
	  	
	  	    	
	    }
	    
	    
	    System.out.println("custid" +custid + " " +  id.trim().indexOf(" "));

	    if(custid ==  null && id.trim().indexOf(" ") != -1){	    
	    String cut = id.substring(0,id.indexOf(" "));
	   
	    
	     cmd = "select kodas as kood from Partneriai par INNER JOIN Adresai adr ON adr.adresoid = par.adresoid where Pastabos ='PV" + cut + "'" ;   
	     System.out.println(cmd);
	    st2 = con.prepareStatement(cmd);
	     rs = st2.executeQuery();
	 System.out.println("IKI cut " + cut);
	    while (rs.next()){
	    	custid =  rs.getString("kood");
	    	cc++;
	     }
	       
	    rs.close();
	    st2.close();
	  
	    
	    }
	    
	    
	  	 if(custid == null){
	  		  String cmd5 = "select kodas as kood from Partneriai par INNER JOIN Adresai adr ON adr.adresoid = par.adresoid where kodas = '" + id + "'" ; 
	    	    
		  	    st2 = con.prepareStatement(cmd5);
		  	     rs = st2.executeQuery();
		  	    System.out.println(cmd5);
		  	     cc = 0;
		  	    while (rs.next()){
		  	    	custid =  rs.getString("kood");
		  	    	cc++;
		  	     }
		  	    rs.close();
		  	    st2.close();
		  	    
	  	  }
	    con.close();
	    if(cc > 1){
	    	return null;
	    }
	    
  }catch(Exception ee){
	  ee.printStackTrace();
  }


 	return custid;
   }
     
   public String identifyWare(String kood) throws SQLException {
	  String sql = "select warehouseno from asVwCustomer where rtrim(customercode) =  '"+ kood +"'";
	  String res = "EQ";
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
	    	res =  rs.getString("warehouseno").trim();
	     }
	    rs.close();
	    st2.close();
	    con.close();
	    if(res.equals("VL00")){
	    	res = "EQ";
	    }
	    
	  return res;
   }
   
   public int getInCases(String kood) throws SQLException {
		  String sql = "select UnitsInPack as kogus from asVwProduct where productcode = '"+ kood +"'";
		 int res = 1;   Connection con = null;
			try {
				con = this.getNewConnection();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  PreparedStatement st2 = con.prepareStatement(sql);
		    ResultSet rs = st2.executeQuery();
 		 
		    while (rs.next()){
		    	res =  rs.getInt("kogus");
		     }
		
		    rs.close();
		    st2.close();
		    con.close();
		  return res;
	   }
   
   public String identifyRegion(String kood) throws SQLException {
		  String sql = "SELECT CONVERT( varchar(20), ivr_reg.IvNormatyvuReiksmesId ) as kood FROM Partneriai AS part " +
    "  INNER JOIN PL_Partneriai               AS pl      ON part.PartneriaiId = pl.PartneriaiId " +
    "    INNER JOIN IvNormatyvuReiksmes     AS ivr_reg ON pl.RegionasId = ivr_reg.IvNormatyvuReiksmesId " +
    " where part.kodas='"+ kood +"'";
		  String res = "154";
		   Connection con = null;
			try {
				con = this.getNewConnection();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
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
   
   public String identifyAgent(String ware) throws SQLException {
		  String res = "F128";
		   if(ware.equals("KA02") || ware.equals("141")) {
			   return "K425";
		   }
		   
		   if(ware.equals("KL01") || ware.equals("140")) {
			   return "L288";
		   }
	
		   /*
	       V956  Vilnius  154
	       K425  Kaunas    141
	       S567  Siauliai  142
	       P450  Panevezys  143
	       A628  Alytus    155
	       L288  Klaipeda 140 
	       */
 
		   
		   if(ware.equals("PA04") || ware.equals("143")) {
			   return "P450";
		   }
		   
		   if(ware.equals("�A03") || ware.equals("142")) {
			   return "S567";
		   }
		   
		   if(ware.equals("EQ") || ware.equals("154")) {
			   return "V956";
		   }
		   
		   if(ware.equals("EQBR") || ware.equals("154")) {
			   return "V956";
		   }
		 
		   if( ware.equals("155")) {
			   return "A628";
		   }
		  return res;
	   }
   
   public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  
   public String identifyProduct(String ean, String prodID) throws SQLException{


	  if( ean!=null){
		     if(ean.equals("59918257")){
		    	 ean = "7613032190842";
		     }
			   
		     if(ean.equals("5998710967612")){
		    	 ean = "7613032194680";
		     }
	 
		     if(ean.equals("5998710967193")){
		    	 ean = "5998710967070";
		     }		  
	  }

  	String sql2 = "SELECT pr.kodas AS toode FROM Prekes  pr LEFT JOIN PL_PREKES a on a.prekesid=pr.prekesid " +
        " where KAPKodas like '%" + ean + "' and kodas not like '%*%' and EQ_SAVININKAS = 1 and pr.Nenaudoti  = 0";
  	String sql4 = "SELECT pr.kodas AS toode FROM Prekes pr LEFT JOIN PL_PREKES a on a.prekesid=pr.prekesid " +
    " where KAPKodas like '%" + ean + ".' and pr.kodas not like '%*%' and EQ_SAVININKAS = 1 and pr.Nenaudoti  = 0";
	String sql5 = "SELECT pr.kodas AS toode FROM Prekes pr LEFT JOIN PL_PREKES a on a.prekesid=pr.prekesid " +
    " where KAPKodas like '%" + ean + "' and pr.kodas not like '%*%' and EQ_SAVININKAS = 1 and pr.Nenaudoti  = 0";

  	
    String sql1 = "SELECT kodas AS toode FROM Prekes " +
    " where kodas = '"  + prodID + "'";
   // log.info("1_LINE_SQL: " + sql1);
   // log.info("2_LINE_SQL: " + sql2);

	String sql3 = "SELECT kodas AS toode FROM Prekes " +
    " where KAPKodas = '" + prodID + "' and kodas not like '%*%' and Nenaudoti  = 0 ";

	
    String prod = null;
    Connection ccc = DriverManager.getConnection(url + "eugesta"  ,  "e_partner", "e_partner");
    Statement st1 = ccc.createStatement();
    ResultSet rs1 = st1.executeQuery(sql2);
    
    if(ean != null && ean.length() > 4){
   
    int counter = 0;
    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
      counter++;
    }

   
    if(counter > 1){
    	
    //	kui on rohkem kui yks siis peab olem ILMA PUNKITA KOOD MILLE J�RGI TUVASTAME 
      	 ccc = DriverManager.getConnection(url + "eugesta"  ,  "e_partner", "e_partner");
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
   	 ccc = DriverManager.getConnection(url + "eugesta"  ,  "e_partner", "e_partner");
     st1 = ccc.createStatement();
     rs1 = st1.executeQuery(sql4);
    
     while (rs1.next()) {
       prod = rs1.getString("toode").trim();
     }

     rs1.close();
     st1.close();
     ccc.close();
  
    }  
    }
     
 
    counter = 0;
    if(prodID != null && (prod == null || prod.trim().length() == 0)){
    	 ccc = DriverManager.getConnection(url + "eugesta"  ,  "e_partner", "e_partner");
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
    	 ccc = DriverManager.getConnection(url + "eugesta"  ,  "e_partner", "e_partner");
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
    
    if ("true".equals(parent.getProps().getProperty("no.prodcheck"))){
    	if ("true".equals(parent.getProps().getProperty("no.prodcheckatall"))){
    		if(ean != null && ean.trim().length() > 4){
        		return ean;
        	}	
    	}else{
    		if(prod == null && ean != null && ean.trim().length() > 4){
        		return ean;
        	}	
    	}
    	
    	
    }
    
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
    	  Enumeration enumr = order.getOrderLines().elements();	     
    	     while (enumr.hasMoreElements()){
    	    	this.addNumLine(); 
    	    	OrderLine orderLine = (OrderLine)enumr.nextElement(); 	 
    	    	
    	    	 String unit = orderLine.getUnit();
    	         String itemid = orderLine.getToode();
    	         double kogus = orderLine.getKogus();
    	      
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
    	         int convert = 1;
    	         
    	         
    	         if(order.getStatus()>78 && order.getStatus() != 94 && order.getStatus() != 82 && order.getStatus() != 92){

    	    	     if(itemid.equals("156203")){
    	    	          convert = 28;	// ean = "7613032190842";156203
    	    	     }
    	    		   
    	    	     if(itemid.equals("156212") && order.getStatus() != 80 ){
    	    	    	//  convert = 28; //ean = "7613032194680";
    	    	     }
    	     
    	    	     if(itemid.equals("156220")){
    	    	    	  convert = 25;// ean = "5998710967070";
    	    	     }
    	    	     kogus =  kogus / convert;
    	         
    	         	System.out.println("kogus" + kogus);
    	         	if(convert != 1){
    	         		kogus = Math.round(kogus + 0.49);
    	         	}
    	         
    	         	System.out.println("kogusrr" + kogus);
    	            
    	         }    	
    	    	System.out.println(order.getCompany() + " " + itemid + " " + orderLine.getKogus() + " " + kogus);
    	    	System.out.println("on T�na " + isToday(order.getShipCal()));
    	    	if(isToday(order.getShipCal())){
    	    		Calendar cc = Calendar.getInstance();
    	    		if(cc.get(Calendar.DAY_OF_WEEK)== Calendar.FRIDAY){
    	    	          cc.add(Calendar.DATE, 3);	
    	    	          order.setShipCal(cc.getTimeInMillis() + "");
    	    		}
    	    	}
    	    	System.out.println("Panin asja " + new Date(order.getShipCal().getTimeInMillis()).toGMTString());
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
          	String region = identifyRegion(order.getCompany());
              Connection con = null;
			try {
				con = getNewConnection();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
              CallableStatement st2 = con.prepareCall(sql);
              st2.executeUpdate(); 
              log.info("Finishing Order " + salesid + " success" );        
              st2.close();
              con.close();
          }

   
   
       public String  getKodirovka(String ean,float status){
    	
    	   if ("true".equals(parent.getProps().getProperty("no.prodcheck"))){
    		   return ean;
    	   }else{
    		   return ean; 
    	   }
    	   /*
    	   String itemid = null;
    	   System.out.println("ean sisenedes " + ean + " status " + status);
    	   if(this.kodirovka != null && this.kodirovka.size() != 0 && ean != null && ean.trim().length() != 0){
    		   if(status == 92){//rivona
    			 Vector vec = (Vector)kodirovka.get("rivona");
    			 if(vec != null){
    				Enumeration en =  vec.elements();
    				while(en.hasMoreElements()){
    					String ttt = (String)en.nextElement();
    					if(ean.equals(ttt.substring(0, ean.length()))){
    						System.out.print("muudan toote koodi rivona " + itemid + "+" );
    						itemid = ttt.substring(ean.length()+1);
    						System.out.println(itemid);
    					}					
    				}
    			 }
    		   }
    		   
    		   if(this.kodirovka != null && this.kodirovka.size() != 0 && ean != null && ean.trim().length() != 0){
        		   if(status == 83){//assortii
        			   itemid = ean;
        			/* Vector vec = (Vector)kodirovka.get("assorti");
        			 if(vec != null){
        				Enumeration en =  vec.elements();
        				while(en.hasMoreElements()){
        					String ttt = (String)en.nextElement();
        					if(ean.equals(ttt.substring(0, ean.length()))){
        						System.out.print("muudan toote koodi assorti " + itemid + "+" );
        						itemid = ttt.substring(ean.length()+1);
        						System.out.println(itemid);
        					}					
        				}
        			 }
        			   }
        		   }
    		   
    		   if(status == 93 || status == 4779026910001f){//iki
      			 Vector vec = (Vector)kodirovka.get("iki");
      			 if(vec != null){
      				Enumeration en =  vec.elements();
      				while(en.hasMoreElements()){
      					String ttt = (String)en.nextElement();
      					if(ean.equals(ttt.substring(0, ean.length()))){
      						System.out.print("muudan toote koodi iki " + itemid + "+" + ttt + "+" + ean.length()+ "+" );
      						itemid = ttt.substring(ean.length()+1);
      						System.out.println(itemid);
      					}					
      				}
      			 }
      		   }
    		   if(status == 97 ){//lukoil
        			 Vector vec = (Vector)kodirovka.get("lukoil");
        			 if(vec != null){
        				Enumeration en =  vec.elements();
        				while(en.hasMoreElements()){
        					String ttt = (String)en.nextElement();
        					if(ean.equals(ttt.substring(0, ean.length()))){
        						System.out.print("muudan toote koodi lukoil " + itemid + "+" + ttt + "+" + ean.length()+ "+" );
        						itemid = ttt.substring(ean.length()+1);
        						System.out.println(itemid);
        						if(itemid.indexOf("#") != -1){
        							itemid = itemid.substring(itemid.indexOf("#") + 1 );
        						}
        					}					
        				}
        			 }
        		   }
    		   if(status == 89 || status == 9982348352403f){//kilmine
        			 Vector vec = (Vector)kodirovka.get("kilmine");
        			 if(vec != null){
        				Enumeration en =  vec.elements();
        				while(en.hasMoreElements()){
        					String ttt = (String)en.nextElement();
        					if(ean.equals(ttt.substring(0, ean.length()))){
        						System.out.print("muudan toote koodi kilmine " + itemid + "+" + ttt + "+" + ean.length()+ "+" );
        						itemid = ttt.substring(ean.length()+1);
        						System.out.println(itemid);
        					}					
        				}
        			 }
        		   }
    		   

    		   if(status == 89 || status == 9981237153176f){//rimi
        			 Vector vec = (Vector)kodirovka.get("rimi");
        			 if(vec != null ){
        				Enumeration en =  vec.elements();
        				while(en.hasMoreElements()){
        					String ttt = (String)en.nextElement();
        					System.out.println("Rimi " + ttt);
        					if(ean.equals(ttt.substring(0, ean.length()))){
        						System.out.print("muudan toote koodi rimi " + itemid + "+" );
        						itemid = ttt.substring(ean.length()+1);
        						System.out.println(itemid);
        					}	
        					if(itemid == null){
        						while (ean.endsWith("0")){
        							ean = ean.substring(0,ean.length()-1);
        						}
        						if(ean.equals(ttt.substring(0, ean.length()))){
            						System.out.print("muudan toote koodi rimi " + itemid + "+" );
            						itemid = ttt.substring(ean.length()+1);
            						System.out.println(itemid);
            					}	
        					}
        					if(itemid != null && itemid.indexOf("#")!=-1){
        						itemid = itemid.substring(itemid.indexOf("#") + 1);
        					}
        				}
        			 }
        		   }
    		   

    		   if(status == 89 || status == 9987427830001f){//Gulbele
        			 Vector vec = (Vector)kodirovka.get("gulbele");
        			 if(vec != null ){
        				Enumeration en =  vec.elements();
        				while(en.hasMoreElements()){
        					String ttt = (String)en.nextElement();
        					System.out.println("gulcheck '" + ttt + "' '" + ean);
        					if(ttt.length() >= ean.length() && ean.equals(ttt.substring(0, ean.length()))){
        						System.out.print("muudan toote koodi gulbele " + itemid + "+" );
        						itemid = ttt.substring(ean.length()+1);
        						if(itemid.startsWith("#")){
        							itemid = itemid.substring(1);
        						}
        						System.out.println(itemid);
        					}					
        				}
        			 }
        		   }
    		   
    		   if(status == 89 || status == 4747001002003f){//Prisma
      			 Vector vec = (Vector)kodirovka.get("prisma");
      			 if(vec != null ){
      				Enumeration en =  vec.elements();
      				while(en.hasMoreElements()){
      					String ttt = (String)en.nextElement();
      					if(ean.equals(ttt.substring(0, ean.length()))){
      						System.out.print("muudan toote koodi prisma " + itemid + "+" );
      						itemid = ttt.substring(ean.length()+1);
      						System.out.println(itemid);
      					}					
      				}
      			 }
      		   }
    		   if(status == 88 ){//KPM
        			 Vector vec = (Vector)kodirovka.get("kpm");
        			 if(vec != null ){
        				Enumeration en =  vec.elements();
        				while(en.hasMoreElements()){
        					String ttt = (String)en.nextElement();
        					if(ean.equals(ttt.substring(0, ean.length()))){
        						System.out.print("muudan toote koodi KPM " + itemid + "+" );
        						itemid = ttt.substring(ean.length()+1);
        						System.out.println(itemid);
        					}					
        				}
        			 }
        		   }
    		   
    		   if(status == 90){//drogas
      			 Vector vec = (Vector)kodirovka.get("drogas");
      			 if(vec != null){
      				Enumeration en =  vec.elements();
      				while(en.hasMoreElements()){
      					String ttt = (String)en.nextElement();
      					if(ean.equals(ttt.substring(0, ean.length()))){
      						System.out.print("muudan toote koodi drogas " + itemid + "+" );
      						itemid = ttt.substring(ean.length()+1);
      						System.out.println(itemid);
      					}					
      				}
      			 }
      		   }
    		   
    		   
    		   if(status == 94 || status == 98 ){//maxima xls ja txt
        			 Vector vec = (Vector)kodirovka.get("maxima");
        			 if(vec != null){
        				Enumeration en =  vec.elements();
        				while(en.hasMoreElements()){
        					String ttt = (String)en.nextElement();
        					
        					if(ttt.length()>=ean.length() && ean.equals(ttt.substring(0, ean.length()))){
        						System.out.print("muudan toote koodi maxima " + itemid + "+" );
        						itemid = ttt.substring(ean.length()+1);
        						System.out.println(itemid);
        					}					
        				 }
        			  }
        		   }   
    	        } 	   
    	   if(itemid != null && itemid.startsWith("#")){
    		   itemid = itemid.substring(1);
    	   }
    	   return itemid ;*/
    	   }
    

   private void addInvoiceLines(String salesid,String originalnr) throws SQLException{

     Enumeration enumr = order.getOrderLines().elements();
     int counter = 0;
     
 	
     
     
     while (enumr.hasMoreElements()){
       counter++;
     //  log.info("ORDER_LINE_COUNTER:" + counter);
       OrderLine orderLine = (OrderLine)enumr.nextElement();
       String itemid = orderLine.getToode();
       if(itemid.indexOf("#") != -1){
    	   itemid = itemid.substring(itemid.indexOf("#")+1);
       }
       //String ean = orderLine.getEAN();
       
       
       double kogus = orderLine.getKogus();
       double ale = orderLine.getDiscount();      
       String name = orderLine.getTooteNimi();
       String unit = orderLine.getUnit();
      
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
       int convert = 1;
       
       
       if(order.getStatus()>78 && order.getStatus() != 94  && order.getStatus() != 82 && order.getStatus() != 92){

  	     if(itemid.equals("156203")){
  	          convert = 28;	// ean = "7613032190842";156203
  	     }
  		   
   
  	     if(itemid.equals("156220")){
  	    	  convert = 25;// ean = "5998710967070";
  	     }
  	     kogus =  kogus / convert;
  	     
       
       	System.out.println("kogus" + kogus);
       	if(convert != 1){
       		kogus = Math.round(kogus + 0.49);	
       	}
       
       	System.out.println("kogusrr" + kogus);
          
       }
     String ean = "null";
     
       if ("true".equals(parent.getProps().getProperty("no.prodcheck")) || 
    		   (order.getStatus() > 2 && order.getStatus() != 82)){
    	   itemid= "";
    	   name = "";
    	   ean = orderLine.getEAN();
       }
       
       if((itemid != null && itemid.trim().length()>0) || "true".equals(parent.getProps().getProperty("no.prodcheck"))
    		   || order.getStatus() > 2){
    	
    	   name = name.replaceAll("'", "");
    	   name = name.replaceAll("\"", "");
    	   
          String    sql = "declare @OpId INT \n"+
                         " select @OpId = NULL \n" + 
		                 " exec uspi_IE_PardavimuKunai  @OpId Output ," + salesid + ",' '," +
	          		         "'" + itemid + "',null,null,''," + kogus + "," +
          				 " null,null,null,'" + name + "',null,null,null,'" + ean +"',null," + ale +
          				 " ,null,null,null,null,null,null,null,null,null,null," +
          				 " null,null,null,null,1,null,null,null,null,null,null,1 \n" + //5 null juurde
          				 " select @OpId " ;
          


              // log.info("SALES_LINE_SQL: " + sql);
               System.out.println("SALES_LINE_SQL: " + sql);
        //   	String region = identifyRegion(order.getCompany());
               Connection con = null;
			try {
				con = getNewConnection();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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





   
   private boolean isToday(Calendar cal){
	   Calendar cc = Calendar.getInstance();
	   if(cal.get(Calendar.YEAR) == cc.get(Calendar.YEAR)){
		   if(cal.get(Calendar.MONTH) == cc.get(Calendar.MONTH)){
			   if(cal.get(Calendar.DAY_OF_MONTH) == cc.get(Calendar.DAY_OF_MONTH)){
				   return true;
			   }  
		   } 
	   }
	   return false;
   }
   
   
   private int addInvoiceHeader(String doknr ,String refer,String info) throws SQLException{
   	
   	String agent = order.getAgent();
   	
   	int result = 0;
       String text = order.getEmailBody();
       if (text == null){
             text = "";
              }
  Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
       
    String ladu = "VL00";
 
    
    	
    	if(isToday(order.getShipCal())){
    		Calendar cc = Calendar.getInstance();
    		if(cc.get(Calendar.DAY_OF_WEEK)== Calendar.FRIDAY){
    	          cc.add(Calendar.DATE, 3);	
    	          order.setShipCal(cc.getTimeInMillis() + "");
    		}
    	}
    	
    	String region = identifyRegion(order.getCompany());
    	
   // if(agent == null || agent.trim().length()== 0){
    	if(order.getStatus()>2){
       		agent = this.identifyAgent(region);	
    	}
   	//}
   		
   		System.out.println("ladu ja agent ;" + ladu + " " + agent);
    order.setAgentCode(agent);
	
    String currency = identifyCurrency(order.getCompany());
    
    
    String tyyp = "53";
    String param = "35";
    if(order.getStatus() == 1){
    	System.out.println("Return Document");
    	tyyp = "50";
    }
    
    if(order.getStatus() == 2){
    	System.out.println("Re Pricing Document");
    	tyyp = "123";
    }
    if(order.getStatus() == 79){
    	System.out.println("Edisoft Recadv");
    	tyyp = "2000032";
    	 param = "145";
    }
    String sql = "declare @OpId INT \n"+
    "select @OpId = NULL \n" +
    "exec uspi_IE_PardavimuGalva @OpId Output,'" + doknr + "',110350," + tyyp + ",'" + getDate(order.getShipCal()) + "'," +
		
		"'" + order.getCompany() + "',11,null,'" + currency + "'," +
		"'" + ladu + "','" + agent + "',null,null,null,null,null,null,null,null,null,null," + param + ",'X'," +
		
		"null,null,null,null,'" + text + "',null,'"+ refer.replaceAll("\"", " ").replaceAll("'", " ").trim()+ "',null,null,null," +
		"null,null,null,null,null,null,null,null,null,null," +
		"null,null,null,null,null,null,null,null,null,null,null,null,null,null,1	\n" +
		"select @OpId" ;



       log.info("addInvoiceHeader2: " + sql);
       //System.out.println(sql);
       Connection con = null;
	try {
		con = getNewConnection();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
       CallableStatement  msProcedure = con.prepareCall( sql );
     
	 
         System.out.println("number: " + doknr);
         System.out.println("kuup�ev: " + getDate(order.getShipCal()));
         System.out.println("firma: " + order.getCompany());
         System.out.println("ladu: " + ladu);
         System.out.println("agent: " + agent + " " +  order.getAgent());
         System.out.println("text: " + text);
       //  System.exit(0);
         //stmt2.setTimestamp(2, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
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
     return "SkaiteEug";
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
	private Connection getNewConnection() throws Exception {
		return DriverManager.getConnection(url + "eugesta"
	            ,  "M_Partner_1", "mpsk2002");
	}
	
   


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

