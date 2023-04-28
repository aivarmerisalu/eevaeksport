

/**
 * @author Aivar  Merisalu

 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;




import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.Email;
import eevaeksport.driver.Toode;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:prisma
 * 
 * @version 1.0
 */

public class KalevImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public KalevImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url, "mobiejava","mobilejava");
   
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
      loadSignatures();
      
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  
	private Vector signatures = new Vector();

  private void loadSignatures(){
		
	  try{		
	  		File f = new File("signatures.vec");
	  		Calendar ss = Calendar.getInstance();
	  		
	  		if (f.exists() && f.lastModified() < ss.getTimeInMillis() - 3600000) {// kui on yle tunni vana siis maha 
	  			f.delete();
	  			signatures = new Vector();
	  		} else {
	  			signatures = (Vector) readObject("signatures.vec");
	  		}

	  		
	  	} catch (Exception e) {
	  		signatures = new Vector();
	  	}

	  	}

	  		
	  	   public static Object readObject(String s) throws IOException,
	       ClassNotFoundException, IOException {
	     Object obj = null;
	     File f = new File(s);
	     if (f.exists()){
	       ObjectInputStream ois = new ObjectInputStream(new FileInputStream(s));
	       obj = ois.readObject();
	       ois.close();
	     }
	     return obj;
	   }


  
  public String identfySorting (String prodID){
	  	
	    String sql1 = " Select SC01014 AS sort FROM SC010100 pr  where pr.SC01001='" + prodID + "'";
	  log.info("1_LINE_SQL: " + sql1);

	    String sort = "000";

	    Statement st1;
		try {
			st1 = con.createStatement();
		
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	sort = rs1.getString("sort");
	    //	System.out.println("sort k�es 1 ;" + sort );
	    	if(sort == null || sort.trim().length() == 0){
	    		sort = "xxx";
	    	}
	    }

	    rs1.close();
	    st1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
  // System.out.println("Sort k�es 2 ;" + sort );
	    return sort;
	  }
  
	private void  addToDB(String agentid,String custid,String toode,double kogus,double disc,String country,double alekogus,boolean zall){
		String ccc = " select stockcode as toode,  bestbefore as kuup, "
				+ "  stockamount as kogus, discount as maxdisc FROM mj_salesproposal   "
				+ " WHERE  stockcode= '"+ toode + "' and bestbefore is not null ";
		
     String sellfast = "0";
		
		
		
	
		  
		    Statement st1;
			try {
				st1 = con.createStatement();
				
				ResultSet rs1 = st1.executeQuery(ccc);
				if(rs1.next()){
					sellfast = "1";
				}
				
				
			    String sql1 = "   insert into mj_discountlines (agent,custid,sentdate,prodid,amount,discount,salefast) values " +
			    		 "  ('" +agentid + "','" + custid + "',getdate(),'" + toode + "'," +kogus + "," + disc 
			    		 		+ ",'" + sellfast + "') ";
			
				st1.executeUpdate(sql1);
				st1.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		
    }
  
	
private HashSet getXXX(){
	  	
  HashSet res = new HashSet();

	    String sql1 = " Select SC01001 AS pda  FROM SC010100 pr  where SC01014 = 'XXX' ";

		try {
	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      res.add(rs1.getString("pda").trim());
	    }

	    rs1.close();
	    st1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	    return res;
	  }


  
  
  public boolean makeRoundingO (String prodID){
	  	


	    String sql1 = " Select SC01097 AS sort FROM SC010100 pr  where pr.SC01001='" + prodID + "'";
	  log.info("1_LINE_SQL: " + sql1);

	    boolean sort = true;

	    Statement st1;
		try {
			st1 = con.createStatement();
		
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	if( rs1.getString("sort").trim().equals("1")){//kui seal on 0 -st erinev v��rtus
	    		//sort = false; // kommentisin v�lja sest peaks t��tama ilma selleta kah
	    	}
	    }

	    rs1.close();
	    st1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
// System.out.println("Sort k�es 2 ;" + sort );
	    return sort;
	  }


  
  
  public String identfyWare(String prodID){


	    String sql1 = " Select SC01094 AS ladu FROM SC010100 pr  where pr.SC01001='" + prodID + "'";
	  log.info("1_LINE_SQL: " + sql1);

	    String ladu = "SKSM1";

	    Statement st1;
		try {
			st1 = con.createStatement();
		
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	//ladu = rs1.getString("ladu");
	    //	System.out.println("Ladu k�es 1 ;" + ladu );
	    	if(ladu == null || ladu.trim().length() == 0){
	    		ladu = "SKSM1";
	    	}
	    }

	    rs1.close();
	    st1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
   //  System.out.println("Ladu k�es 2 ;" + ladu );
	    return ladu;
	  }


  
  public String leiaFelixiAGkood(String id){

			 String sql1 = "SELECT objekt2  FROM klient  where kood  =  '" + id + "'";	
			  log.info("1_LINE_SQL: " + sql1);
			    String ring = id;

			    Statement st1;
			    
				try {
				Connection coe = this.getNewConEeva();
					st1 = coe.createStatement();
				
			    ResultSet rs1 = st1.executeQuery(sql1);

			    while (rs1.next()) {
			    	ring = rs1.getString("kood").trim();
			  
			    }

			    rs1.close();
			    st1.close();
			    coe.close();
				} catch (SQLException e) {
				//	e.printStackTrace();
				}
		  //   System.out.println("Felixi agendikood k�es  ;" + ring );
			    return ring;
			  }

  
  
  public String leiaFelixiKlkood(String id){
		//M14544    M14544    M14544    MAXJ10701 45283067952010010720100108                                                                                                                                                                                                                  54                                                                       34013                              Linda kirsitasku 3x55g                            6           1            5500003033J1                                                     20100108                                                                                                                                                                                                                                                                                                            


			    String sql1 = "SELECT kood  FROM klient  where viitenr  =  '" + id + "'";	
			  log.info("1_LINE_SQL: " + sql1);
			    String ring = id;

			    Statement st1;
			    
				try {
				Connection coe = this.getNewConEeva();
					st1 = coe.createStatement();
				
			    ResultSet rs1 = st1.executeQuery(sql1);

			    while (rs1.next()) {
			    	ring = rs1.getString("kood").trim();
			  
			    }

			    rs1.close();
			    st1.close();
			    coe.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		    // System.out.println("Felixi kliendikood k�es  ;" + ring );
			    return ring;
			  }

  public String leiaFelixiUUSKlkood(String id){
		//M14544    M14544    M14544    MAXJ10701 45283067952010010720100108                                                                                                                                                                                                                  54                                                                       34013                              Linda kirsitasku 3x55g                            6           1            5500003033J1                                                     20100108                                                                                                                                                                                                                                                                                                            


			    String sql1 = "SELECT kood14  FROM klient  where viitenr  =  '" + id + "'";	
			  log.info("1_LINE_SQL: " + sql1);
			    String ring = id;

			    Statement st1;
			    
				try {
				Connection coe = this.getNewConEeva();
					st1 = coe.createStatement();
				
			    ResultSet rs1 = st1.executeQuery(sql1);

			    while (rs1.next()) {
			    	ring = rs1.getString("kood14").trim();
			  
			    }

			    rs1.close();
			    st1.close();
			    coe.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		 //    System.out.println("Felixi kliendikood k�es  ;" + ring );
			    return ring;
			  }

	 
	  
  
  
  public String identfyRing(String id){
	//M14544    M14544    M14544    MAXJ10701 45283067952010010720100108                                                                                                                                                                                                                  54                                                                       34013                              Linda kirsitasku 3x55g                            6           1            5500003033J1                                                     20100108                                                                                                                                                                                                                                                                                                            


		    String sql1 = "SELECT SL01027 AS kood FROM SL010100 where SL01001  =  '" + id + "'";	
		  log.info("1_LINE_SQL: " + sql1);
		    String ring = "";

		    Statement st1;
			try {
				st1 = con.createStatement();
			
		    ResultSet rs1 = st1.executeQuery(sql1);

		    while (rs1.next()) {
		    	ring = rs1.getString("kood").trim();
		  
		    }

		    rs1.close();
		    st1.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	  //   System.out.println("Ring k�es  ;" + ring );
		    return ring;
		  }

  
  
  
  private String getKKood(String intr){
	  String res = intr;
	  if(intr != null){
		  if(intr.equals("INTR504")){
			  res = "K504";
		  }
		  
		  //J�HVIKOMMIPOOD
		   
		   
		  if(intr.equals("INTR501")){
			  res = "K501";
		  }
		
		//  KURESSAARE  KOMMIPOOD
		   
		  
		  if(intr.equals("INTR506")){
			  res = "K506";
		  }
		 // NUKUTEATRI KOMMIPOOD
		   
		  if(intr.equals("INTR513")){
			  res = "K513";
		  }
		
		//  N�MME KOMMIPOOD
		   
		  if(intr.equals("INTR510")){
			  res = "K510";
		  }
		 // PAIDE KOMMIPOOD
		   
		  if(intr.equals("INTR512")){
			  res = "K512";
		  }
		 // P�RNU KOMMIPOOD
		   
		  if(intr.equals("INTR502")){
			  res = "K502";
		  }

//		  P�LVA KOMMIPOOD
		   
		  if(intr.equals("INTR509")){
			  res = "K509";
		  }
		
		//  RAKVERE KOMMIPOOD
		   
		  if(intr.equals("INTR517")){
			  res = "K517";
		  }
		//  ROTERMANNI KOMMIPOOD
		   
		  
		  if(intr.equals("INTR503")){
			  res = "K503";
		  }
		 // VALGA KOMMIPOOD
		   
		  
		  if(intr.equals("INTR511")){
			  res = "K511";
		  }
		 // V�RUKOMMIPOOD
		   
		   
		  if(intr.equals("INTR507")){
			  res = "K507";
		  }	 // TARTU KOMMIPOOD
		   
		  if(intr.equals("INTR515")){
			  res = "K515";
		  }
		
		//  KIVI�LI KOMMIPOOD
		   
		  
		  if(intr.equals("INTR508")){
			  res = "K508";
		  }
		  //J�RITEHASEPOOD
		   
		   
		  if(intr.equals("INTR516")){
			  res = "K516";
		  }
		  //VILJANDI KOMMIPOOD
	  
	  }
	 	    
	  return res;
  }
  
  private Vector splitOrder (Order order){
	  Vector result = new Vector();
      
	 System.out.println("Enne suurus splitti" + order.getOrderLines().size());
	  HashSet xxxprods = getXXX();
	  Order ord23 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ordJ1 = new Order((order.getCompany()),order.getAgentFirstName());
      Order ordOTB = new Order(leiaFelixiKlkood(order.getCompany()),order.getAgentFirstName());
    
	  ord23.setAgentCode(order.getAgentCode());
	  ord23.setDokNr(order.getDokNr());
	  ord23.setInfo(order.getInfo());
	  ord23.setReference(order.getReference());
	  ord23.setTeostaja((order.getTeostaja()));
	  ord23.setChanged(order.isChanged());
	  ord23.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord23.setStatus(order.getStatus());
	  ord23.setShipTime(order.getShipTime());
	  ord23.setShipDateUsed(order.isShipDateUsed());
	  ord23.setWarehouse(order.getWarehouse());
	  ord23.setCurrency("KALEV");
	  
	

	  ordOTB.setAgentCode(order.getAgentCode());
	  ordOTB.setDokNr(order.getDokNr());
	  ordOTB.setInfo(order.getInfo());
	  ordOTB.setReference(order.getReference());
	  ordOTB.setTeostaja((order.getTeostaja()));
	  ordOTB.setChanged(order.isChanged());
	  ordOTB.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordOTB.setStatus(order.getStatus());
	  ordOTB.setShipTime(order.getShipTime());
	  ordOTB.setShipDateUsed(order.isShipDateUsed());
	  ordOTB.setWarehouse(order.getWarehouse());
	  ordOTB.setCurrency("OTB");
	  
	  ordJ1.setAgentCode(order.getAgentCode());
	  ordJ1.setDokNr(order.getDokNr());
	  ordJ1.setInfo(order.getInfo());
	  ordJ1.setReference(order.getReference());
	  ordJ1.setTeostaja((order.getTeostaja()));
	  ordJ1.setChanged(order.isChanged());
	  ordJ1.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordJ1.setStatus(order.getStatus());
	  ordJ1.setShipDateUsed(order.isShipDateUsed());
	  ordJ1.setShipTime(order.getShipTime());
	  ordJ1.setWarehouse(order.getWarehouse());
	  ordJ1.setCurrency("EBAST");
	
	//  System.out.println("Splitin Warehouse " + order.getWarehouse());
	  
	  Vector lines = order.getOrderLines();
	  Enumeration enums = lines.elements();
	  while  (enums.hasMoreElements()){
		  OrderLine lin = (OrderLine)enums.nextElement();
		
		     String itemid = lin.getToode();
		     if(eipandid.contains(itemid)){
		    	// kui on pandi jama siis ei lisa tellimusele aga numeraatori keerame edasi 
		    	 cou++;
		     }else{
		    	 if(itemid != null) {
		    	
		    	 if(itemid.startsWith("OTB") ){
			    	 ordOTB.add(lin.getToode().substring(3), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,lin.docType);
		          }else if(itemid.startsWith("FE")){
				    	 ordOTB.add(lin.getToode().substring(2), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,lin.docType);
			          }else{
			        	  String sorting =  identfySorting(itemid);
			        	  if((order.getCompany().startsWith("INTR")  || order.getCompany().equals("M12320") || order.getCompany().equals("M19031")) && xxxprods.contains(itemid)) {
			        		  ordJ1.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
						     			sorting + " " + lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,lin.docType); 
			        	  }else {
			        		  ord23.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
						     			sorting + " " + lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,lin.docType);  
			        	  }
						   
			        	     	 
				     }	 
		     } }
	  }


	  if(ord23.getOrderLines() != null && ord23.getOrderLines().size()>0){
		  result.add(ord23);  
	  }

	 
	  if(ordJ1.getOrderLines() != null && ordJ1.getOrderLines().size()>0){
		  result.add(ordJ1);  
	  }
	 

	  if(ordOTB.getOrderLines() != null && ordOTB.getOrderLines().size()>0){
		  result.add(ordOTB);  
	  }
//	 System.out.println("Result =" +order.getDokNr()
	//		 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }



  

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	
  	
  	if( id != null && id.equals("stockmann")){
  		return "M10620";
  	}
  	if(regnr != null && regnr.equals("laima")){
  		return "M20240";
  	}
  	
  	 System.out.println("Kliendi M��ramine: "  + id + " " + regnr);
  	
  	    sql1 = "SELECT SL01001 AS kood FROM SL010100 where SL01125  =  '" + id + "'";	
	
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim() ;
    }
    rs1.close();
    st1.close();
    
    
    if(acc == null){
    	  sql1 = "SELECT SL01001 AS kood FROM SL010100 where SL01001  =  '" + id + "'";	
    	    System.out.println(sql1);
    	     st1 = con.createStatement();
    	     rs1 = st1.executeQuery(sql1);

    	    while (rs1.next()) {
    	      acc = rs1.getString("kood").trim() ;
    	    }
    	    
    	    rs1.close();
    	    st1.close();   
    		
    	}    
    
    String uus = null;
    if(id!= null && id.trim().length() >10){
    	uus = id.trim().substring(id.trim().length()-10,id.trim().length());
    }
    
    if(uus != null && acc == null){
    	sql1 = "SELECT SL01001 AS kood FROM SL010100 where SL01125  =  '" + uus + "'";	
       System.out.println(sql1);

         st1 = con.createStatement();
         rs1 = st1.executeQuery(sql1);

        while (rs1.next()) {
          acc = rs1.getString("kood").trim() ;
        }
        rs1.close();
        st1.close();
    }
 

    

    
    System.out.println("Klient: " + acc + " " + id);
    return acc;

  }

  
  
  public String identifyCustomerAGENT(String id) throws SQLException {
 	 String	sql1 = "SELECT SL01035 as agent FROM SL010100 WHERE SL01001='" + id +"'"; 
 	
  // System.out.println(sql1);
   String agent = null;

   Statement st1 = con.createStatement();
   ResultSet rs1 = st1.executeQuery(sql1);

   while (rs1.next()) {
   	agent = rs1.getString("agent").trim();

   }
   rs1.close();
   st1.close();

   return agent;

 }

  public String identifyCustomerPayer(String id) throws SQLException {
	 	 String	sql1 = "SELECT SL01056 as payer FROM SL010100 WHERE SL01001='" + id +"'"; 
	 	
	  // System.out.println(sql1);
	   String agent = null;

	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   while (rs1.next()) {
	   	agent = rs1.getString("payer").trim();

	   }
	   rs1.close();
	   st1.close();

	   return agent;

	 }

  
  private String getPandiPakend(String toode) throws SQLException{
 	 
    String sql1 = " SELECT RTRIM(SC01150) AS pant FROM SC010100  WHERE RTRIM(SC01001)=   '" + toode.trim() + "'";
    String prod = null;
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      prod = rs1.getString("pant").trim();
    }
      // System.out.println(toode + " prod1 " + prod);
    rs1.close();
    st1.close();
    return prod;
 }
  
  
  
  
  private HashSet eipandid = new HashSet();
 
  private TreeMap twoeans = null;
  
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	

	  
	    String prod = null;
	 String ea = ean;
	 if(ea == null){
		 ea = "XXXXXXXXXXX";
	 }
	 if (ea.length() == 0 ){
		 ea = "XXXXXXXXXX";
	 }
	 
	 if(ea != null && (ea.equals("2050000039577")  || ea.equals("0299600004468")  || ea.trim().equals("100004005")  || ea.equals("2050000039560") || ea.equals("100009811")
			 || ea.equals("0299600004482") || ea.equals("530000004024") || ea.equals("100004021") || ea.equals("0299600004499"))){  
		 return "NOPANT";
	 }
	    
	   String pr = prodID;
	   if(pr == null){
		   pr = "000000000";
	   }
	  
	  
	   
    String sql2 = " SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE (RTRIM(SC01132)=   '" + ea 
                              + "' or RTRIM(SC01003) = '" + ea + "') and SC01014 != '' and SC01001 not like '%-D' order by len(SC01001)  ";

    String sql1 = "SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01001)=  '" + pr + "' and SC01014 != ''";

  


    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
   

    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
     }
 //  System.out.println(ean + " prod0 " + prod);
   
   
   
    if(prod == null && ean != null){

      rs1 = st1.executeQuery(sql2);
      if (rs1.next()) {
        prod = rs1.getString("toode").trim();
      }
    }
    
    if(prod == null && ean != null && ean.trim().length() > 1 && !ean.contains("XXXX")){
    	
    	  String sqlea = "SELECT  nimetus , toode FROM toode  WHERE TRIM(ribakood)=  '" + ean + "' ";

    	Connection coe = this.getNewConEeva();
    	
    	 Statement st2 = coe.createStatement();
    	    ResultSet rs2 = st2.executeQuery(sqlea);
    	 rs2 = st2.executeQuery(sqlea);
         if (rs2.next()) {
        	 String nimetus = rs2.getString("nimetus").trim();
        	 String kood = rs2.getString("toode").trim();
        	 String pre = "FE" ;
 			if(kood.startsWith("K0") && nimetus.indexOf("OTB") !=-1){
 				pre = "OTB";
 			}
           prod = pre + kood ;
          
         }
         rs2.close();
         st2.close();
         coe.close();
    }

  //  System.out.println(prodID + " prod1 " + prod);
    rs1.close();
    st1.close();

    return prod;
  }

  
  
  public String identifyProduct3(String ean, String prodID) throws SQLException{
	  	

	  
	    String prod = null;
	 String ea = ean;
	 if(ea == null){
		 ea = "XXXXXXXXXX";
	 }
	 if (ea.length() == 0 ){
		 ea = "XXXXXXXXXX";
	 }
	 
	 if(ea != null && (ea.equals("2050000039577")  || ea.equals("0299600004468")  || ea.trim().equals("100004005")  || ea.equals("2050000039560") || ea.equals("100009811")
			 || ea.equals("0299600004482") || ea.equals("530000004024") || ea.equals("100004021") || ea.equals("0299600004499"))){  
		 return "NOPANT";
	 }
	    
	   String pr = prodID;
	   if(pr == null){
		   pr = "000000000";
	   }
	  
	  if(ea.length() == 12) {
		  ea = ea + "0";
	  }
	  
	  
	   
 

  String sql1 = "SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE  SC01014 != ''  and RTRIM(SC01001)=  '" + pr + "' ";




  Statement st1 = con.createStatement();
  ResultSet rs1 = st1.executeQuery(sql1);
 

  while (rs1.next()) {
    prod = rs1.getString("toode").trim();
   }
  System.out.println(ea + " prod0 " + prod);
  if(ea != null) {
	  ea = ea.trim();
  }
  if(ea.length() > 13) {
	  ea = ea.substring(0,13);
  }
 
  String sql2 = " SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE (RTRIM(SC01132)=   '" + ea 
  + "' or RTRIM(SC01003) = '" + ea + "')   and SC01014 != ''   order by len(SC01001)  ";
  if(prod == null && ean != null){
	
    rs1 = st1.executeQuery(sql2);
    if (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
  }
  
  if(prod == null && ean != null){
  	
  	  String sqlea = "SELECT  nimetus , toode FROM toode  WHERE TRIM(ribakood)=  '" + ean + "' ";

  	Connection coe = this.getNewConEeva();
  	
  	 Statement st2 = coe.createStatement();
  	    ResultSet rs2 = st2.executeQuery(sqlea);
  	 rs2 = st2.executeQuery(sqlea);
       if (rs2.next()) {
      	 String nimetus = rs2.getString("nimetus").trim();
      	 String kood = rs2.getString("toode").trim();
      	 String pre = "FE" ;
			if(kood.startsWith("K0") && nimetus.indexOf("OTB") !=-1){
				pre = "OTB";
			}
         prod = pre + kood ;
        
       }
       rs2.close();
       st2.close();
       coe.close();
  }

  System.out.println(prodID + "  "  + ean + " prod1 " + prod + " " + sql2);
  rs1.close();
  st1.close();

  return prod;
} 
  
  public String identifyProduct2(String ean, String prodID) throws SQLException{
	  	

	  
	    String prod = null;
	 String ea = ean;
	 if(ea == null){
		 ea = "XXXXXXXXXX";
	 }
	 if (ea.length() == 0 ){
		 ea = "XXXXXXXXXX";
	 }
	 
	 if(ea != null && (ea.equals("2050000039577")  || ea.equals("0299600004468")  || ea.trim().equals("100004005")  || ea.equals("2050000039560") || ea.equals("100009811")
			 || ea.equals("0299600004482") || ea.equals("530000004024") || ea.equals("100004021") || ea.equals("0299600004499"))){  
		 return "NOPANT";
	 }
	    
	   String pr = prodID;
	   if(pr == null){
		   pr = "000000000";
	   }
	  
	   if(ea.length() == 12) {
			  ea = ea + "0";
		  }
	   
  String sql2 = " SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE (RTRIM(SC01132)=   '" + ea 
                            + "' or RTRIM(SC01003) = '" + ea + "')  order by len(SC01001)  ";

  String sql1 = "SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01001)=  '" + pr + "' ";




  Statement st1 = con.createStatement();
  ResultSet rs1 = st1.executeQuery(sql1);
 

  while (rs1.next()) {
    prod = rs1.getString("toode").trim();
   }
//  System.out.println(ean + " prod0 " + prod);
 
 
 
  if(prod == null && ean != null){

    rs1 = st1.executeQuery(sql2);
    if (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
  }
  
  if(prod == null && ean != null){
  	
  	  String sqlea = "SELECT  nimetus , toode FROM toode  WHERE TRIM(ribakood)=  '" + ean + "' ";

  	Connection coe = this.getNewConEeva();
  	
  	 Statement st2 = coe.createStatement();
  	    ResultSet rs2 = st2.executeQuery(sqlea);
  	 rs2 = st2.executeQuery(sqlea);
       if (rs2.next()) {
      	 String nimetus = rs2.getString("nimetus").trim();
      	 String kood = rs2.getString("toode").trim();
      	 String pre = "FE" ;
			if(kood.startsWith("K0") && nimetus.indexOf("OTB") !=-1){
				pre = "OTB";
			}
         prod = pre + kood ;
        
       }
       rs2.close();
       st2.close();
       coe.close();
  }

//  System.out.println(prodID + " prod1 " + prod);
  rs1.close();
  st1.close();

  return prod;
}
  
  
  private TreeMap getTwoEans() {
	TreeMap res = new TreeMap();
	
	try{
		
		
		String sq = "SELECT SC01132 AS ean   FROM SC010100 t " +
					"	 left outer JOIN SC030100 on SC03001=t.SC01001 and SC03002='SKSM1' AND SC03003!=0 left outer JOIN SC390100 ON t.SC01001 "+ 
				 " = SC39001 and SC39002='00' LEFT OUTER JOIN proov20100 yh ON yh.SC01001= t.SC01001 " +
					"	 LEFT OUTER JOIN class ty ON ty.MasterProductCode = t.SC01001 WHERE (SC01115 = 'L' or " +
					"	  ( LEN(LTRIM(SC01037))>0 and SC39004 > 0 and rtrim(ltrim(SC01016)) != 'S' and SC01014 != '' and SC01014 != 'XXX') ) " +
					"	  and ( LEN(LTRIM(SC01132)))>0 " +
					"	  group by t.SC01132 having  count (SC01132) > 1";
	
		    String sql = " SELECT  RTRIM(t.SC01001) AS toode, SC01015 AS tykke , SC01132 AS ean   FROM SC010100 t "+ 
					" left outer JOIN SC030100 on SC03001=t.SC01001 and SC03002='SKSM1' AND SC03003!=0 left outer JOIN SC390100 ON t.SC01001 "+ 
				    " = SC39001 and SC39002='00' LEFT OUTER JOIN proov20100 yh ON yh.SC01001= t.SC01001 " +
					" LEFT OUTER JOIN class ty ON ty.MasterProductCode = t.SC01001 WHERE (SC01115 = 'L' or " +
					" ( LEN(LTRIM(SC01037))>0 and SC39004 > 0 and rtrim(ltrim(SC01016)) != 'S' and SC01014 != '' and SC01014 != 'XXX') ) "+
					"  and ( LEN(LTRIM(SC01132)))>0 and ( LEN(LTRIM(SC01015)))>0  ORDER BY SC01015 ,LEFT(RTRIM(SC01037),1),t.SC01001 ";
		   
		    log.info("getAndUpdateRecID(): " + sql);
		      Statement st = con.createStatement();
		      ResultSet rs = st.executeQuery(sq);
		     
		      HashSet eans = new HashSet();
		      
		      while(rs.next()){
			      eans.add( rs.getString("ean").trim());
			     }
		      
		      rs.close();
		      
		      rs = st.executeQuery(sql);
		      while(rs.next()){
		       String toode= rs.getString("toode").trim();
		       String kogus = rs.getString("tykke");
		      String ean= rs.getString("ean").trim();
		      if(eans.contains(ean)){
		    	  Toode t = new Toode(toode);
		    	  t.setKastis(Integer.parseInt(kogus.trim()));
		    	  ArrayList arr = new ArrayList();
		    	  if(res.get(ean) != null){
		    		  arr = (ArrayList)res.get(ean);
		    	  }
		    	  arr.add(t);
		    	  res.put(ean, arr);
		        }
		       
		       
		       }
		      rs.close();
		      st.close();
		      
	}catch(Exception e){
		e.printStackTrace();
	}

		    return res;
		  }

	  


private long getAndUpdateNext() throws SQLException{
	    long nextRecID = 0;
	    String sql = "SELECT  nextval FROM mj_numbers WHERE serial='ord'";
	    log.info("getAndUpdateRecID(): " + sql);
	      Statement st = con.createStatement();
	      ResultSet rs = st.executeQuery(sql);
	      while(rs.next()){
	       
	        //System.out.println("number:" + rs.getString("NEXTREC"));
	        nextRecID = rs.getLong("nextval");
	        log.info("NEXTRECID = " + nextRecID);
	        sql = "UPDATE mj_numbers SET nextval =" + (nextRecID+1) +
	        " WHERE serial='ord' ";
	        log.info("getAndUpdateRecID(): " + sql);
	        Statement st2 = con.createStatement();
	        int i = st2.executeUpdate(sql);
	        st2.close();
	        log.info("UPDATED ROWS = " + i);
	      }
	      rs.close();
	      st.close();

	    return nextRecID;
	  }

  
  private boolean isExportCustomer(String  cust )throws SQLException{
	    boolean result = false;
	    String sql = "SELECT SL01001 AS kood FROM SL010100 where SL01001  =  '" + cust + "'  ";
	    log.info("getAndUpdateRecID(): " + sql);
	      Statement st = con.createStatement();
	      ResultSet rs = st.executeQuery(sql);
	      if(rs.next()){
	        result = true;
	      }
	      
	     
	      rs.close();
	      st.close();

	    return result;
	  }
  
  private boolean isPrisma(String  cust )throws SQLException{
	    boolean result = false;
	    String sql = "SELECT SL01056 AS kood FROM SL010100 where SL01001  =  '" + cust + "'  ";
	    log.info("getAndUpdateRecID(): " + sql);
	      Statement st = con.createStatement();
	      ResultSet rs = st.executeQuery(sql);
	      if(rs.next()){
	    	  String aa = rs.getString("kood").trim();
	    	  if(aa.equals("M13598")){
	    		  result = true; 
	    	  }
	        
	      }
	      
	     
	      rs.close();
	      st.close();

	    return result;
	  }
  
  private boolean isRealik(String  cust )throws SQLException{
	    boolean result = false;
	    String sql = "SELECT SL01056 AS kood FROM SL010100 where SL01001  =  '" + cust + "'  ";
	    log.info("getAndUpdateRecID(): " + sql);
	      Statement st = con.createStatement();
	      ResultSet rs = st.executeQuery(sql);
	      if(rs.next()){
	    	  String aa = rs.getString("kood").trim();
	    	  if(aa.equals("M16554")){
	    		  result = true; 
	    	  }
	        
	      }
	      
	     
	      rs.close();
	      st.close();

	    return result;
	  }

  
  private double getMultiplier(String toode ){
  	double res = 1;
  	
    String sql1 = " SELECT (case when SC01015!='' then cast(SC01015 as integer) else 1 end) AS res "
    		+ ", SC01097 as aaa FROM SC010100  WHERE RTRIM(SC01001)=   '" + toode + "'";

			try{
				 Statement st1 = con.createStatement();
				    ResultSet rs1 = st1.executeQuery(sql1);
			
				      while (rs1.next()) {
				        res = rs1.getDouble("res");
				        int aaa = rs1.getInt("aaa");
				        if(aaa >0 ){ // raneti soovil eemaldasin ja k�ik telid l�hevad
				        	res = aaa;
				        }
				      }

				      rs1.close();
				    st1.close();
			}catch (Exception e){
				
			}
  // System.out.println("Multiplier " + res);
  // System.out.println(sql1);
  	return res;
  }
  
  private double getInCase(String toode){
	  	double res = 1;
	  	
	    String sql1 = " SELECT (case when SC01015!='' then cast(SC01015 as integer) else 1 end) AS res FROM SC010100  WHERE RTRIM(SC01001)=   '" + toode + "'";
		try{
					 Statement st1 = con.createStatement();
					    ResultSet rs1 = st1.executeQuery(sql1);
				
					      while (rs1.next()) {
					        res = rs1.getDouble("res");
					      
					      }
					      rs1.close();
					    st1.close();
				}catch (Exception e){
					
				}
	    	return res;
	  }
  private HashMap excludeProds = null;
  
  
  
  private HashMap getExclude( ){
	  excludeProds = new HashMap();
	  
	  
	  	
	    String sql1 = "SELECT [Peakliendikood] as payer ,[Tarnekliendikood] as saaja,[Tootekood]  as toode " +   
  " FROM mj_PDAclose where getdate() between Alates and Kuni";

				try{
					 Statement st1 = con.createStatement();
					    ResultSet rs1 = st1.executeQuery(sql1);
				
					      while (rs1.next()) {
					    	  String payer = rs1.getString("payer");
					    	  String saaja = rs1.getString("saaja");
					    	  String toode = rs1.getString("toode");
					        
					      if (toode != null){
					    	  if(payer != null){
					    		  Vector vv = (Vector)excludeProds.get(payer.trim());
					    		  if(vv == null){
					    			  vv = new Vector();
					    		  }
					    		  vv.add(toode);
					    		  excludeProds.put(payer.trim(), vv);
					    	  }
					    	  
					    	  if(saaja != null){
					    		  Vector vv = (Vector)excludeProds.get(saaja.trim());
					    		  if(vv == null){
					    			  vv = new Vector();
					    		  }
					    		  vv.add(toode);
					    		  excludeProds.put(saaja.trim(), vv);
					    	  }
					    	  
					      }
					        
					      }

					      rs1.close();
					    st1.close();
				}catch (Exception e){
					
				}
	  // System.out.println("excludeProds " + excludeProds.toString());
	  // System.out.println(sql1);
	  	return excludeProds;
	  }
	  

  public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
  private HashSet pdaProds = null;

  public void makeOrder(Order order) throws SQLException{
	  
	  this.trouble =  null;
	   String aaa = parent.getProps().getProperty("cust.deposits");
	   if(aaa != null){
		
	   StringTokenizer pa = new StringTokenizer(aaa,",");
		 
		  while(pa.hasMoreTokens()){
			  
			  eipandid.add(pa.nextToken());
		  }
		   
	   }
	   
	   if(pdaProds == null) {
		   String sql1 = "SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE  SC01014 != ''";

			  
		   pdaProds = new HashSet();

		    Statement st1 = con.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
		   
		  
		    while (rs1.next()) {
		    	pdaProds.add(rs1.getString("toode").trim());
		     }
		   
		    rs1.close();
		    st1.close();
	   }
	   
	    excludeProds = getExclude();
	    cou = 0;
	   Vector splitted =  splitOrder(order);
        
		  Enumeration en = splitted.elements();
		  while(en.hasMoreElements()){
			  Order ord = (Order)en.nextElement();
			  
		       boolean tagastus = false;
		         Vector lin = order.getOrderLines();
		         for (int i = 0; i < lin.size(); i++) {
		        	 OrderLine line = (OrderLine)lin.elementAt(i);
		        	 if(line.getKogus() < 0 ) {
		        		 tagastus = true;
		        	 }
		             
		         }
			  
			//  System.out.println("inff3 " + ord.getInfo() + Converter.getDDpMMpYYYYpString(ord.getShipCal()));
		         if(!tagastus) {
			        	 java.util.Collections.sort(ord.getOrderLines(), new java.util.Comparator(){
						        public boolean equals(Object obj) {
		          return false;
		        }
		        public int compare(Object o1, Object o2) {
		          OrderLine ol1 = (OrderLine)o1;
		          OrderLine ol2 = (OrderLine)o2;
		          return ol1.getTooteNimi().compareTo(ol2.getTooteNimi());
		        }
		      });
			         }
			 
			     
			  
			  
			  
            if(ord.getCurrency().equals("KALEV")){
            	System.out.println("Kalevi Tellimus " + ord.getOrderLines().size() + " rida");
            	   if(signatures == null) {
      		        	signatures = new Vector();
      		        }
      		           
      		        String signature = ord.getSignature();
      			  if(signatures.contains(signature)){
      				  this.trouble = "REPRETEATING ORDER agent :" + order.getAgent() + " customer :" 
      			  + Converter.deCode(order.getCompany()) ;
      			  }else{

                      makeOutput(ord,false);  
                      signatures.add(signature);
     			     
       			  }
       		           
       			 try {
    					saveObject(signatures, "signatures.vec");
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    			  
            } 
          
            if(ord.getCurrency().equals("EBAST")){
            	System.out.println("Poe Ebastandartne Tellimus " + ord.getOrderLines().size() + " rida");
            	makeOutput(ord , true);	
            } 
            
            /*if(ord.getCurrency().equals("FELIXUUS")){
            	System.out.println("FelixiUUS Tellimus " + ord.getOrderLines().size() + " rida");
                makeOutputFelixUUS(ord);	
            } */ 

            if(ord.getCurrency().equals("OTB")){
            	System.out.println("OTB Tellimus " + ord.getOrderLines().size() + " rida");
                makeOutputOTB(ord);	
            }  
            
		  }
  //  addListings(order);
  }
  
  
  
 
  
  private void makeOutputOTB(Order order){
	 	StringBuffer sb = new StringBuffer();
	 	 nf.setGroupingUsed(false);

	 	try {
	 	 
	 	    String cust = order.getCompany();
	 		String agent = order.getAgent();
			
		
			String ref = order.getReference();
			if ( ref == null ){
				ref =  " ";
			}

			String teo = order.getTeostaja();
			if ( teo != null && teo.trim().length() > 0 ){
				ref =  teo.replace('|', ' ').replace('|', ' ').trim();
			}
			agent = leiaFelixiAGkood(cust);
			//System.out.println("Tuvastatud agent OTB :" + agent  + " " + ref + " " + order.getReference() + " " + order.getTeostaja() );
			
			String com = order.getEmailBody();
			if ( com == null ){
				com =  " ";
			}		
			
			
			order.setWarehouse("5"); //smarten
		     
			
			//System.out.println("Laokene OTB " + order.getWarehouse() + " " + cust + " ");
			
			String ware = order.getWarehouse();
			if ( ware == null ){
				ware = " ";
			}
			 String doknr = order.getDokNr();
	        if(doknr == null){
	        	doknr = ref;
	        }
	      String  time = (System.currentTimeMillis() + "").substring(5,12);
	          
	    Vector lines = order.getOrderLines();
	      for (int i = 0; i < lines.size(); i++) {
	          
	      	OrderLine line = (OrderLine)lines.elementAt(i);
	          String toode = line.getToode();
	          double kogus = line.getKogus();         
	
	     String comment = order.getInfo();
	     
	     
	     if(comment == null || comment.trim().length() == 0 ){
	    	comment = "";
	     }else{
	    	if(comment.trim().length()>70){
	    		comment = comment.substring(0,70);	
	    	}
	     }

	
	     
	    
	     double disc = line.getDiscount();
	     
	
	     
 if(kogus < 100000){
		 sb.append(cust + ";" + toode + ";" + kogus + ";0;" + ware + ";0;" +  disc + ";" 
				 + order.getShipCal().getTime().getTime() + ";0;" +line.getPrice() + ";"
				 + order.getPaymTerm() + ";" + agent + ";0;" + line.getUnit() + ";" +
				 getDateDDpMMpYYYYStr(order.getShipCal(),0) + "; " + com + ";" + ware + "; ;" +
				 time + "; ; ;" + ref + ";" + order.getOrderLines().size());//
	           sb.append(System.getProperty("line.separator"));
	       cou++;
	   	    }
	  }
	      
	      setNumAddedLines(cou);
	
	      String fn = doknr;
	      if(doknr.trim().length()== 0){
	    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
	      }
	      try{
	    	   String fileName = escape(parent.getProps().getProperty("conv2.dir") + agent)  + "." + fn  + order.getWarehouse()+ ".txt";
	 	    //  System.out.println(fileName);
	 	        FileOutputStream fos = new FileOutputStream(fileName);
	 	        fos.write(sb.toString().getBytes());
	 	        fos.close();
	      }catch(Exception e){
	    	  e.printStackTrace();
	    	  String fileName = escape(parent.getProps().getProperty("conv3.dir") + agent)  + "." + fn  + order.getWarehouse()+ ".txt";
	 	     //System.out.println(fileName);
	 	        FileOutputStream fos = new FileOutputStream(fileName);
	 	        fos.write(sb.toString().getBytes());
	 	        fos.close();
	      }
	      
	      if(order.getStatus() < 5) {
	    	  Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(order.getAgent());
			   sendCommentEmail("OTB KAUBAD " + com ,null,doknr,order.getCompany(),  " " + getCustName(order.getCompany()) , " ",false,ag,parent);
			  
	      }
	    

		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
	  

  
	public static String getDateDDpMMpYYYYStr(Calendar cc,int add){
		StringBuffer sb = new StringBuffer();
		Calendar cal = Calendar.getInstance();
		if(cc == null){
			cc = Calendar.getInstance();
		}
		cal.setTimeInMillis(cc.getTimeInMillis());
		
		cal.add(Calendar.DAY_OF_YEAR,add);
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}
		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}
		sb.append(day +".");
		sb.append(month + ".");		
		sb.append("" + cal.get(Calendar.YEAR));
	
		return sb.toString();
	}
  

  private String getNimi(String aa) throws SQLException{
  	String res = null;
 	 String	 sql2 = "SELECT RTRIM(SC01002) AS toode FROM SC010100  WHERE RTRIM(SC01001)=  '" + aa + "'";
			
			Statement st1 = con.createStatement();
			ResultSet rs1 = st1.executeQuery(sql2);
			
			 if (rs1.next()) {
			   res = rs1.getString("toode").trim();
			 }
			
	rs1.close();
	st1.close();
  	return res ;
  }
  
  
  
  private String getCustName(String cuid) {
		 
	  String res = cuid;
	  try{
		  String sql1 = "SELECT SL01002 AS nimi FROM SL010100 where SL01001  =  '" + cuid + "'";
			
			Statement st1 = con.createStatement();
			ResultSet rs1 = st1.executeQuery(sql1);
			
			 if (rs1.next()) {
				String aa = rs1.getString("nimi").trim();
				   res = aa ;
			 }
				rs1.close();
				st1.close();
	  }catch(Exception e){
		  e.printStackTrace();
	  }
		
			
	
	  return res;
  }
  
  
  private String getAgentSym(String agent) {
	  String res = agent;
	  try{
		  String	 sql2 = "SELECT SY24003 AS nimi  FROM SY240100 WHERE SY24001='BK' and (SY24002)=  '" + agent+ "'";
			
			Statement st1 = con.createStatement();
			ResultSet rs1 = st1.executeQuery(sql2);
			
			 if (rs1.next()) {
				String aa = rs1.getString("nimi").trim();
			 
				   res = aa.substring(0,1) +(aa.substring(aa.indexOf(' '),aa.indexOf(' ')+2)).trim() ;
			   
			 }
			 

				rs1.close();
				st1.close();
	  }catch(Exception e){
		  e.printStackTrace();
	  }
		
			
	
	  return res;
  }
  
 
  private void addDBLine(String cust,String doknum,String agent,Order order,String toode,String comment,double kogus,String ring,String number) throws SQLException{
	  
	  String sss = "SELECT order_number from mj_orderbuffer where order_number = '" + doknum + "' and  product = '" + toode + "' and amount = " + kogus;
	  boolean go = true;
	  PreparedStatement st = con.prepareStatement(sss);
	   ResultSet rs = st.executeQuery();
	   if(rs.next()){
		   go = false;
	   }
	   rs.close();
      st.close();
	  
		 String	 sql = "INSERT INTO mj_orderbuffer (order_date,shipping_date,order_number,agent,customer,product,amount,comment,order_number_shop) values (" +
		 		" getdate(),? ,'" + doknum + "','" + agent+ "','" + cust + "','" + toode + "'," + kogus + ",'" + comment + "','" + number +"')";
			
		     st = con.prepareStatement(sql);
		    st.setTimestamp(1, new Timestamp(order.getShipCal().getTimeInMillis()));
		    if(go && kogus != 0){
			    st.executeUpdate();	
		    }
	        st.close();
	}
  
  
  private boolean notInt(double sisse){
  	try{
  	String res = (sisse+"").substring(0,(sisse+"").indexOf('.'));
  	int hhh = Integer.parseInt(res);
  	if(sisse-hhh>0){
  		return true;
  	}else{
  		return false;	
  	}
  	}catch (Exception e){
  		e.printStackTrace();
  	
  	}
  	return true;
  }
  
  
  
  
  public String getFelixUUSToode(String id){
		//M14544    M14544    M14544    MAXJ10701 45283067952010010720100108                                                                                                                                                                                                                  54                                                                       34013                              Linda kirsitasku 3x55g                            6           1            5500003033J1                                                     20100108                                                                                                                                                                                                                                                                                                            


			    String sql1 = "SELECT kood14  FROM toode  where toode  =  '" + id + "'";	
			  log.info("1_LINE_SQL: " + sql1);
			    String ring = id;

			    Statement st1;
			    
				try {
				Connection coe = this.getNewConEeva();
					st1 = coe.createStatement();
				
			    ResultSet rs1 = st1.executeQuery(sql1);

			    while (rs1.next()) {
			    	ring = rs1.getString("kood14").trim();
			  
			    }

			    rs1.close();
			    st1.close();
			    coe.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		  //   System.out.println("Felixi kliendikood k�es  ;" + ring );
			    return ring;
			  }

  
  public String getHinnakiri(String custid ){


	    String sql1 = " select SL01029 as hinnakiri from SL010100   where SL01001= '" + custid + "' ";
	  log.info("1_LINE_SQL: " + sql1);

	    String result = null;

	 
	    Statement st1;
		try {
			st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);
	    if (rs1.next()) {
	    	result = rs1.getString("hinnakiri");
	    	if(result != null && result.trim().length() != 0){
	    		result = result.trim();
	    	}
	    }
	    rs1.close();
	    st1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	    return result;
	  }

  private boolean isSelected(Calendar ccc){
	  Calendar cal = Calendar.getInstance();
	  if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
		  cal.add(Calendar.DATE, 1);
		  if(cal.get(Calendar.DATE) != ccc.get(Calendar.DATE) ){
			  cal.add(Calendar.DATE, 2);
			  if(cal.get(Calendar.DATE) != ccc.get(Calendar.DATE) ){
				  return true;
				}
			 }
	    
	  }else{
		  cal.add(Calendar.DATE, 1);
		  if(cal.get(Calendar.DATE) != ccc.get(Calendar.DATE) ){
			  return true;
		  }
	  }
	  
	  return false;
  }
  
  boolean export = false;
  
  private HashSet felProds = null;
  
  
 private void makeOutput(Order order , boolean ebast){
 	StringBuffer sb = new StringBuffer();
 	 nf.setGroupingUsed(false);
	 Hashtable pandikas =  new Hashtable();
	 String aletext = null;
	 
	 boolean sendcomm = false;
 	
 	 // SIIA TEE ROUNDIN statuse j�rgi ja kampaania koguse kontroll ja koodi muutmine 
 	 
	  if(twoeans == null){
		  twoeans = getTwoEans();
	  }

	 
	 
 	try {
 		
 		if(felProds == null ){
 			felProds = getFelProds();
 		}
 		
 		 String scala = getAndUpdateNext()+ "";
 	      export = isExportCustomer(order.getCompany());
      boolean isPrisma = isPrisma(order.getCompany());
      boolean isRealik = isRealik(order.getCompany());
      StringBuffer prismaProds = new StringBuffer();
      StringBuffer realikProds = new StringBuffer();
      
    //  System.out.println("isPrisma" + isPrisma);
      
 	    String cust = order.getCompany();
 		//String hinnakiri = getHinnakiri(cust);
		String agent = this.identifyCustomerAGENT(cust);
		String agent2 = order.getAgent();
        String ring = identfyRing(cust);
        if(agent2.equals("FELIX")){
			agent2 = "85";
			agent= "85";
		}
		
		//System.out.println("Tuvastatud agent :" + agent + " tellimusel " + agent2 );
	
		String ref = order.getTeostaja();
		if ( ref == null ){
			ref =  " ";
		}
		String com = order.getEmailBody();
		if ( com == null ){
			com =  " ";
		}		
		String ware = order.getWarehouse();
		if ( ware == null ){
			ware = " ";
		}
		
		// System.out.println("inff" + order.getInfo() + " " + ref + " " + com + " "  );
		
		
		String warex = "SKSM1";
		if((order.getWarehouse() != null && order.getWarehouse().equals("23")) || ebast){
			warex = "23";
		}
		
		if(order.getStatus() == 77 && order.getWarehouse() != null && order.getWarehouse().length() > 1) {
			warex = order.getWarehouse();
		}
		
		 String doknr = order.getDokNr();
        if(doknr == null){
        	doknr = ref;
        	if(doknr.indexOf("-") != -1){
        		doknr = doknr.substring(doknr.indexOf("-"));
        	}
        }
        
        if(order.getStatus() < 11 && ref.trim().length() > 4){
        	doknr = ref.trim();
        }
        
         String type = "1";
         boolean house = false;
         
         if(order.getStatus()< 50 && !house && !order.getAgent().equals(parent.getProps().getProperty("defaultagent"))){
        	// System.out.println("Agent v�eti tellimusest" + order.getAgent());
        	 agent = order.getAgent();
        	 if(agent.equals("FELIX")){
     			agent2 = "85";
     			agent= "85";
     		}
         }
         
         String status = getAgentSym(agent)+ order.getWarehouse()+ "-" + this.getTarneLyh(order)+ " " +
          ( Calendar.getInstance().getTimeInMillis() + "").substring(5);
         if(order.getStatus() == 98){     	 
        	 status = "MAX" + order.getWarehouse() + this.getTarneLyh(order) + " " ;
         }
      
         if(order.getCompany().indexOf("INTR") != -1 || inList(order.getCompany())){     	 
        	 status = "SYS." + order.getWarehouse() + this.getTarneLyh(order) + " " +
             ( Calendar.getInstance().getTimeInMillis() + "").substring(5);
         }
         
         if(order.getStatus() == 96 || order.getStatus() == 60  || order.getStatus() == 80){  	 
        	 status = "AAA23"  + this.getTarneLyh(order) + " " ;
         }
         
      	String ls = System.getProperty("line.separator");
	
      	 String logistika = null;
         if(order.getTeostaja() != null && order.getTeostaja().trim().length()>0 ){
        	 logistika = order.getTeostaja();
        	 
        	 if(logistika.equals("AFES")){
        		 logistika = "M14822";
        		
        	 }else  if(logistika.equals("EP")){
            	 logistika = "M13405";
            	
        	 }else  if(logistika.equals("AS Smarten Logistics")){
            	 logistika = "M15537";
            	 agent = "OO";
        	 }else{
        		 logistika = null;
        	 }
         }
       boolean  hasdiscount = false;
       boolean tagastus = false;
         Vector lin = order.getOrderLines();
         for (int i = 0; i < lin.size(); i++) {
        	 OrderLine line = (OrderLine)lin.elementAt(i);
        	 if(line.getDiscount() > 0){
        		 hasdiscount = true;
        	 }
        	 if(line.getKogus() < 0 ) {
        		 tagastus = true;
        	 }
             
         }
         
      	
        StringBuffer sb2 = new StringBuffer(); 
         
         sb2.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + ls);
	      sb2.append("<msg:Msg xsi:schemaLocation=\"http://Epicor.com/Message/2.0 http://scshost/schemas/epicor/ScalaMessage.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:msg=\"http://Epicor.com/Message/2.0\">" + ls);			      
	      sb2.append("  <msg:Hdr>" + ls);
	      sb2.append("   <msg:Sender>" + ls);
	  	if(order.getStatus() < 10 && hasdiscount){
	  		 sb2.append("     <msg:Name>MobilejavaAndroid</msg:Name>" + ls);
		     
	  	}else{
	  		 sb2.append("     <msg:Name>Mobilejava</msg:Name>" + ls);
		     	
	  	}
	      sb2.append("     <msg:Subname>Tellimus</msg:Subname>" + ls);
	      sb2.append("   </msg:Sender>" + ls);
	      sb2.append("    <msg:Ctrl>" + ls);
	      sb2.append("      <msg:MsgID>" + Calendar.getInstance().getTime().getTime() + "</msg:MsgID>" + ls);
	      sb2.append("    </msg:Ctrl>" + ls);
	      sb2.append("   </msg:Hdr>" + ls);
	      sb2.append("   <msg:Body>" + ls);
	      sb2.append("      <msg:Req msg-type=\"Sales Order\" action=\"Process\">" + ls);
	      sb2.append("    <msg:Dta>" + ls);
	      sb2.append("    <dta:SalesOrder xsi:schemaLocation=\"http://www.scala.net/SalesOrder/1.1 http://scshost/schemas/Scala/1.1/SalesOrder.xsd\" xmlns:msg=\"http://Epicor.com/InternalMessage/1.1\" xmlns:dta=\"http://www.scala.net/SalesOrder/1.1\">" + ls);		      
	      sb2.append("     <dta:OrderHeader>" + ls);
	    
       
     

	      String comment = order.getInfo();
	      String comm2 = comment;
	      String tarne = "";
	          
	          
	          if(comment == null || comment.trim().length() == 0 ){
	         	comment = "";
	         	comm2= "";
	          }else{
	         	if(comment.trim().length()>70){
	         		comm2 = comment;
	         		comment = comment.substring(0,70);	
	         		}
	         	sendcomm = true;
	         	}
	          
	         if(order.isShipDateUsed()) {
	        	  tarne = "<br> Tarnekuupäev valitud : " + Converter.getDDpMMpYYYYpString(order.getShipCal());
	         }
	          
	          String tarnekell =  order.getShipTime();
	          

	          if(order.getStatus() == 60 || order.getStatus() == 80){
	         	 tarnekell = order.getWarehouse();
	         	 order.setWarehouse(" ");
	          }
	          
	        //  System.out.println("Kella aeg:" + tarnekell +  " " + order.getWarehouse());
	          if(tarnekell != null && tarnekell.trim().length() != 0 ){
	         	 if(comment.length() != 0){
	         		 comment = addSpaces(comment,35) + tarnekell; 
	         	 }else{
	         		 comment = addSpaces("",35) + tarnekell;
	         	 }
	         	 //sendcomm = true;
	          	}
	          if(order.isChanged()){
	         	 comment = "Muudetud!!!" + comment;
	         	 if(comment.trim().length()>70){
	          		comment = comment.substring(0,70);	
	          	}
	          }
	     	 
	      
	          comment = Converter.escapeXML(comment);
	          
	      int xmlcounter = 0;
	      
	   //   System.out.println("SEES lines size " + order.getOrderLines().size());
    Vector lines = order.getOrderLines();
    String ref1= null;
      for (int i = 0; i < lines.size(); i++) {
    	  
    	  
    	  // vaikimisi staatus on 1 
    	  //Kui on kommentaar siis staatus 0 
          // Kui tarne kuup ei ole t�na + 1 va reede kui on > 3 p�eva siis 0
    	  
          if (i == 0 ){
        	
          	
            
          
	          if(order.getPaymTerm() == -5){ //ettemaks
	        		type = "1";
	              	status = "0";
	          } 
          
	          if(comment == null || comment.trim().length() == 0 ){
	         	comment = "";
	          }
	          
	      	ref = order.getDokNr();
			if ( ref == null ){
				ref =  " ";
			}
			
	
			
			//System.out.println("ref " + ref);
	        
	          if(ref.trim().length()>70){
	        	  ref = ref.substring(0,70);	
	         	}
	          
	            Calendar cal = Calendar.getInstance();
	            Calendar dat = Calendar.getInstance();
	    	    dat.setTimeInMillis(order.getShipCal().getTimeInMillis());
	    	   

	    	
	    	    if (dat != null && dat.after(cal) && (order.getStatus() < 10 || order.getStatus() == 98 || order.getStatus() == 80) ){
	    	    	if(dat.get(Calendar.DAY_OF_YEAR)< Calendar.getInstance().get(Calendar.DAY_OF_YEAR) &&
	    	    			dat.get(Calendar.YEAR)== Calendar.getInstance().get(Calendar.YEAR)){
	    	    		dat.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
	    	    	}
	    	    	cal.setTimeInMillis(dat.getTimeInMillis());
	    	    }
	String ordsta = "1";
	if(ref != null && ref.trim().length() >0 || isSelected(cal) ){
		ordsta = "0";
	}
	
	
	if(tagastus) {
		ordsta = "1";
	}
	
	if(ebast) {
		ordsta = "2";
	}
	    	      sb2.append("      <dta:OrdType>1</dta:OrdType>" + ls);
			      sb2.append("      <dta:WhID>" + warex + "</dta:WhID>" + ls);
			      sb2.append("      <dta:OrdStatus>" + ordsta + "</dta:OrdStatus>" + ls);
			  
	    	    	if(order.getStatus() >10){
	    	    	     sb2.append("      <dta:CustPONum>" + order.getDokNr() + "</dta:CustPONum>" + ls);
	    	    	}else{
	    	    		 sb2.append("      <dta:CustPONum>" + doknr + "</dta:CustPONum>" + ls);
	    	    	}
	       
	    	    	String custo = order.getCompany();
	    	    	
	    	    	if(logistika != null){
	    		    	custo = logistika;
	    		    }
	    	    	
	    	//  sb2.append("      <dta:OrdNum>" + doknr + "</dta:OrdNum>" + ls);
		      sb2.append("      <dta:DelivDate>" +  escape(getTarneXML(order)) + "</dta:DelivDate>" + ls);
		      sb2.append("      <dta:OrdDate>" + escape(getDateString(Calendar.getInstance())) + "</dta:OrdDate>" + ls);
		      sb2.append("      <dta:CustCodeOrd>" + escape(custo) + "</dta:CustCodeOrd>" + ls);	
		      sb2.append("      <dta:YourRef>" + escape(getTarne(order)) + "</dta:YourRef>" + ls);	
			   
		      
		   //   sb2.append("      <dta:AccStr>" + ls);
		    //  sb2.append("          <dta:AccDim1>"escape(getTarne(order)) + agent + "</dta:AccDim1>" + ls);
		    //  sb2.append("      </dta:AccStr>" + ls);
		      String agg = agent;
		      if(agg.startsWith("INTR") || agg.equals("M12320")  || agg.equals("M19031")  ) {
		    	  agg = "";
		      }
		      sb2.append("      <dta:SalesmanNum>" + agg + "</dta:SalesmanNum>" + ls);
		      sb2.append("      <dta:CustCodeDeliv>" + escape(custo) + "</dta:CustCodeDeliv>" + ls);
		    
		       ref1 =Converter.escapeXML(comment.trim());
		      
		      Calendar ccc = Calendar.getInstance();
		      
		      ccc.setTime(order.getShipCal().getTime());
		     
		      
		      if(ccc.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY){
				  ccc.add(Calendar.DATE, -3);
			  }else{
				  ccc.add(Calendar.DATE, -1);
			  }
		      
		      
		      ref1 = "SM-" + escape(getDateStringPool(ccc)) +  " " + ref1;
		      String ref2 = null;
		      
		    	     if(ref1.length() > 34){
		    	  ref2 = ref1.substring(34);
		    	  ref1 = ref1.substring(0,34);
		    	  if(ref2.length() > 34){
		    	    	 ref2 = ref2.substring(0,34);
		    	     }
		      }
		      
		    	     if(tagastus) {
		    	    	    sb2.append("      <dta:Remark1>" + order.getDokNr() + " kreedit</dta:Remark1>" + ls);
		            	    sb2.append("      <dta:Remark2>" + order.getDokNr() + " kreedit</dta:Remark2>" + ls);	      	      
				     } else {
		    	     	    sb2.append("      <dta:Remark1>" + escape(ref1) + "</dta:Remark1>" + ls);
		                  
		    	     	    if(ref2 != null){
		            	      sb2.append("      <dta:Remark2>" + escape(ref2) + "</dta:Remark2>" + ls);	      	      
				            }
		            }
				    	     
		      sb2.append("     </dta:OrderHeader>" + ls);
		      sb2.append("     <dta:OrderLines>" + ls);
          	
          }
    	  
    	  
    	  
    	  
    	  
    	  
    	  
    	  
    	  
    	  
    
      	OrderLine line = (OrderLine)lines.elementAt(i);
          String toode = line.getToode();
          
      
          String pant = getPandiPakend(toode);
          
          
         try{
        	
          if(toode != null && order.getStatus()>1){
         	  //kontrollime kas kampaania tootel on piisav laosaldo ja kehtiv kuup�ev, et seda otse klientidele panna
         	  String sql = "  SELECT  SC01010 as datum, RTRIM(SC03001) AS toode    " +
         	      "  FROM SC030100 INNER JOIN SC010100 ON SC03001= SC01001 WHERE (RTRIM(SC03001) = '" + toode + "-EE' " +
         	      		" or RTRIM(SC03001) = '" + toode + "-EE1' or RTRIM(SC03001) = '" + toode + "-EE2')" +
         	      				" and SC01014 != '' and SC01001 not like '%-D' AND SC03003-SC03004 > " + (line.getKogus() + parent.getTellimustel(toode) 
         	      						+ parent.getTellimustel(toode+ "-EE") + parent.getTellimustel(toode+ "-EE1")
         	      						+ parent.getTellimustel(toode+ "-EE2"));
         	  Statement st1 = con.createStatement();//and SC01014 != '' 
         	  ResultSet rs1 = st1.executeQuery(sql);
         	
         	  String datum = "";
         	  while (rs1.next()) {
         		  boolean goes = true;
         		  datum = rs1.getString("datum");
         		
         		  if(datum != null && datum.trim().length() > 0 && datum.compareTo(this.getTana()) < 0){
         			  goes = false;
         		  }      
         		  if(datum != null && getTana().equals(datum)){
         			//  goes = false; hetkel ei toimi 
         		  }
         		  if(goes){
             	      toode = rs1.getString("toode").trim();   
         		  }
         	    }
         //	 System.out.println(sql +  " " + toode + " " +parent.getTellimustel(toode) + " " + datum.compareTo(this.getTana()) + datum + " " + getTana()); 
         	  rs1.close();
         	  st1.close();
         //	 System.out.println("333");
           }
          
         }catch(Exception e){
        	 e.printStackTrace();
         }
          
          
         try{
         //see blokk on selleks et Esindajatelyt tulnud tellimusel kontrollida kas on kampaania tootel piisav kogus ja kui ei siis asendada kood p�hi koodiga 	
             if(toode != null  && (toode.endsWith("-EE") || toode.endsWith("-EE1") || toode.endsWith("-EE2") )
            		 && order.getStatus() == 0){
            	  
            	  String sql = "  SELECT  SC01010 as datum, RTRIM(SC03001) AS toode    " +
            	      "  FROM SC030100 INNER JOIN SC010100 ON SC03001= SC01001 WHERE RTRIM(SC03001) = '" + toode + "' and SC01014 != ''" +
            	      		" AND SC03003-SC03004 > " + (line.getKogus() + parent.getTellimustel(toode)
            	      				+ parent.getTellimustel(toode+ "-EE") + parent.getTellimustel(toode+ "-EE1")
     	      						+ parent.getTellimustel(toode+ "-EE2"));
            	  Statement st1 = con.createStatement();//and SC01014 != '' 
            	  ResultSet rs1 = st1.executeQuery(sql);
            	//  System.out.println("444");
            	  if (rs1.next()) {
            		  /* boolean goes = true;
            		  String datum = rs1.getString("datum");
            		
            		  if(datum != null && datum.trim().length() > 0 && datum.compareTo(this.getTana()) < 0){
            			  goes = false;
            		  }
            		  if(goes){*/
                	      toode = rs1.getString("toode").trim();   
            		 // }   	       	      
            	    }else{
            	    	if(toode.endsWith("-EE")){
                	    	toode = toode.substring(0,toode.length()-3);	
            	    	}else if(toode.endsWith("-EE1")){
                	    	toode = toode.substring(0,toode.length()-4);	
            	    	}else if(toode.endsWith("-EE2")){
                	    	toode = toode.substring(0,toode.length()-4);	
            	    	}
            	    	
            	    }
            	// System.out.println(sql + " " +  parent.getTellimustel(toode)); 
            	  rs1.close();
            	  st1.close();
            	//  System.out.println("555");
              }else if(order.getStatus() == 0){
            	  //siin vaatame kampaania alguse puhul kas p�hitootel on PDA tunnus olemas kui ei siis kontrillime
            	  //kapmaania koodi olemasolu ja laoj��ki 
            	  
           	  String sql = "  SELECT  RTRIM(SC01001) AS toode    " +
        	      "  FROM SC010100  WHERE RTRIM(SC01001) = '" + toode + "' and (SC01014 != '' or SC01115 = 'L')";
        	  Statement st1 = con.createStatement();
        	  ResultSet rs1 = st1.executeQuery(sql);
        //	  System.out.println("666");
        	  boolean ok = false;
        	  if (rs1.next()) {
                 ok = true;
        	    }
        	// System.out.println(sql); 
        	  rs1.close();
        	  st1.close();
        	  
        	  if(!ok){
        		  //Kontrollime kas kampaania toode on olemas aktiivne 
        		  sql  = "  SELECT  SC01010 as datum, RTRIM(SC03001) AS toode    " +
         	      "  FROM SC030100 INNER JOIN SC010100 ON SC03001= SC01001 WHERE (RTRIM(SC03001) = '" + toode + "-EE' " +
   	      		  " or RTRIM(SC03001) = '" + toode + "-EE1' or RTRIM(SC03001) = '" + toode + "-EE2') and SC01014 != '' AND SC03003-SC03004" +
   	      		  " > " + (line.getKogus() + parent.getTellimustel(toode) + parent.getTellimustel(toode+ "-EE") + parent.getTellimustel(toode+ "-EE1")
 	      						+ parent.getTellimustel(toode+ "-EE2"))+
   	      		  " order by SC03001 desc";
   	 
        		  st1 = con.createStatement();//and SC01014 != '' 
        		  rs1 = st1.executeQuery(sql);
           //    System.out.println("777");
        	  if (rs1.next()) {
                  toode = rs1.getString("toode").trim();
        	    }
        	  rs1.close();
        	  st1.close();
        		  
        	  }
            	  
              }
             
            }catch(Exception e){
           	 e.printStackTrace();
            }

         
         //kontrollime kas on tegemist kauba aluse myygiga edi teillimuste puhul va prismad 
         
         if(order.getStatus() > 1 && !isPrisma){
   String ea = line.getEAN();
      	   if(ea != null && twoeans != null && twoeans.get(ea) != null){
      		 ArrayList all = (ArrayList)twoeans.get(ea);
      		  Toode t1 = (Toode) all.get(0);
      		Toode t2 = (Toode) all.get(1);
      		Toode t3 = t1;
      		
      		if(t2.getKastis() > t1.getKastis()){
      			t3 = t2;
      		}
      		if(!notInt(line.getKogus()/t3.getKastis())){
      			//System.out.println("Vahetasin toote koodi Myygialus " + toode + "-> " + t3.getID());
      	      //	toode = t3.getID();
      		}
      		  
      	   }
         }
         
         
     
          double kogus = line.getKogus();         
    //      System.out.println("888");
          double rounder = getMultiplier(toode);
          if(order.getStatus() == 0 || order.getStatus() == 77){
        	  rounder = 1;
          }
          
     //     System.out.println( "Teine kontroll kui peatootel pole aktiivne pdas tunnus\n" 
       //   +  " " + rounder + " " +notInt(kogus/rounder)  ); 
      	
       
          
             String unit = line.getUnit();
             if(unit != null && unit.equals("DPA")){ 
  	      	//   System.out.println(unit + " " + toode + " enne " + kogus + " bonus " + line.getBonus() );
  	      	   if(line.getBonus()> 1 && !line.getToode().equals("08043-3") && !line.getToode().equals("687228") && 
  	      			   !line.getToode().equals("135897")){
  	      		   kogus = kogus * line.getBonus();
  	      	//	   System.out.println(unit + " " + toode + " bonus " + kogus );
  	      	   }else{
  	      		 //  kogus = kogus * getInCases(itemid);
  	      		
  	      	   }  	   
  	      	       
  	                }
           //  System.out.println(unit + " " + toode + " " + kogus + " " + order.getStatus() );
             if(order.getStatus() > 5  && unit != null && unit.equals("plokk")){ 
            	 kogus = kogus * getInCase(toode);
                 	 
             }
            // System.out.println(unit + " 222 " + toode + " " + kogus + " " + order.getStatus() + " " + order.getCompany() ); 
             if(line.getToode().equals("340235")) {
            	 kogus = kogus /2;  //mingi laima kommi case on see..
             }
             
             if(order.getStatus() ==  98 || order.getCompany().equals("M17307")) { // maxima 
            	 if(line.getToode().equals("07398")) {
                	 kogus = kogus *5;  //mingi laima kommi case on see..
                 }
            	 if(line.getToode().equals("07399")) {
                	 kogus = kogus *5;  //mingi laima kommi case on see..
                 }
            	 if(line.getToode().equals("07421")) {
                	 kogus = kogus *5;  //mingi laima kommi case on see..
                 }
            	 if(line.getToode().equals("02322-1")) {
                	 kogus = kogus *3;  //mingi laima kommi case on see..
                 }
            	 if(line.getToode().equals("02774")) {
                	 kogus = kogus *5;  //mingi laima kommi case on see..
                 }
            	 if(line.getToode().equals("02562")) {
                	 kogus = kogus *5;  //mingi laima kommi case on see..
                 }
            	 if(line.getToode().equals("02561")) {
                	 kogus = kogus *5;  //mingi laima kommi case on see..
                 }
            	 if(line.getToode().equals("02770")) {
                	 kogus = kogus *5;  //mingi laima kommi case on see..
                 }
            	 if(line.getToode().equals("02563")) {
                	 kogus = kogus *5;  //mingi laima kommi case on see..
                 }
            	 if(line.getToode().equals("02727")) {
                	 kogus = kogus *5;  //mingi laima kommi case on see..
                 }
            	 if(line.getToode().equals("02802")) {
                	 kogus = kogus *3;  //mingi laima kommi case on see..
                 }

            	 if(line.getToode().equals("02424")) {
                	 kogus = kogus /2;  //mingi laima kommi case on see..
                 }
            	 if(line.getToode().equals("02425")) {
                	 kogus = kogus /2;  //mingi laima kommi case on see..
                 }
             }
             
             if(order.getStatus() > 1  && (order.getCompany().equals("M10098") || order.getCompany().equals("M16533"))) { // kui on coop siis koguseid muudame

            	 if(line.getToode().equals("688867")) {
                	 kogus = kogus / 5;  // coop case 
                	 }

            	 if(line.getToode().equals("135897")) {
                	 kogus = kogus / 5;  // coop case 
                	 }
            	 if(line.getToode().equals("08043-3")) {
                	 kogus = kogus / 3;  // coop case 
                	 }
            	 
            	 if(line.getToode().equals("02424")) {
                	 kogus = kogus /2;  //mingi laima kommi case on see..
                 }
	        	 
             }
             
             
             if(order.getStatus() > 1  && (order.getCompany().equals("M13600") || order.getCompany().equals("M10024"))) {
            	 // kui on rimi ja mingi kypsis siis koguseid muudame

            	 if(line.getToode().equals("135897") || line.getToode().equals("688867") ) {
                	// kogus = kogus / 5; // Kylli palvel maas 10.2019 ,20.04.2020 tagasi pandud , 06.2020 uuesti maha 
                	 }
            	// 688867 kondents. ja 135897 š
            	 if(line.getToode().equals("05149-1")) {
                	  line.setToode("05149");
                	  kogus = kogus / 14;
                	 }

	        	 
             }
             
            // System.out.println(unit + " 222 " + toode + " " + kogus + " " + order.getStatus() + " " + order.getCompany() ); 
             
             if( !notInt(kogus) && notInt(kogus/rounder)){
            	 // kui kogus on t�isarv ja m��dud kogus pole kastisuhtes t�isarv siis ymmardame ylesse
            	 // aga ainult Kalevi puhul felixit ei ymmarda 
            	 
            	//if(!felProds.contains(toode)){
            		  kogus = makeRoundUp(kogus,rounder);
            	//}
            	 
           	
           //	 System.out.println( toode + " getinCases " + kogus );
             
             } 
             

             if(pant != null){
           	  if(pandikas.get(pant) == null){
           		  pandikas.put(pant,kogus+"");
                 } else {
                 	pandikas.put(pant,(new Double((String)pandikas.get(pant)).doubleValue()+kogus)+"");
                 }
              }
             
             
           //  System.out.println(unit + " " + toode + " getinCases " + kogus  + " " + rounder);
             
            nf.setMaximumFractionDigits(2);
         //   System.out.println("1000");
          if (i == 0 ){
          	if( line.getKogus() < 0){
          	//	System.out.println("kogus " + line.getKogus());
             // 	doknr = getDoknr("OR05");
              	type = "8";
          	}         	
          }

          String nimi =  getNimi(toode);
     if(nimi == null){
    	 nimi = line.getTooteNimi();
     }
     if(!nimi.contains(toode)) {
      nimi = toode + " " + nimi;
     }
        
     if(isPrisma && parent.getProps().getProperty("prismaPalletProds") != null 
   		  && parent.getProps().getProperty("prismaPalletProds").contains(toode+",")) {
   	  prismaProds.append("[" + (int)line.getKogus() + "] " + line.getToode() + " " + nimi + "\n");
     }
     if(isRealik && parent.getProps().getProperty("realiPalletProds") != null 
      		  && parent.getProps().getProperty("realiPalletProds").contains(toode+",")) {
      	  realikProds.append("[" + (int)line.getKogus() + "] " + line.getToode() + " " + nimi + "\n");
        }
     
     
     if(order.getWarehouse() == null){
    	 order.setWarehouse("SKSM1");
     }
    
     
     boolean allowed = isAllowed(cust,toode,order.getStatus());
     
	    if(kogus < 100000){
	    if(logistika != null){
	    	// System.out.println("muudame kliendikoodi " + logistika);
	    	cust = logistika;
	    }
     sb.append(addSpaces(cust, 10));//
     sb.append(addSpaces(cust, 10));//11      
     sb.append(addSpaces(cust, 10));//21
     sb.append(addSpaces(status + doknr, 20));//51
     sb.append(getTana());//59
     sb.append(getTarne(order));//68
     sb.append(addSpaces("",210)); //lisan tyhikuid hunniku 
     sb.append(addSpaces(agent, 3));//280
     sb.append(addSpaces(ref1,70)); //lisan tyhikuid hunniku 350 kommentaar koht
     sb.append(addSpaces(toode, 10));//360 tootekood
     sb.append(addSpaces("",25)); //lisan tyhikuid hunniku 350
     sb.append(addSpaces(nimi , 25));  //375 tootenimi
     sb.append(addSpaces("",25)); //lisan tyhikuid hunniku 435
     sb.append(addSpaces(nf.format(kogus).replace(',','.').replaceAll(" ",""), 12));//447    
     sb.append(type);//448
     sb.append(addSpaces("",12)); //lisan tyhikuid hunniku 470
     sb.append(addSpaces(scala,10));
     sb.append(addSpaces(order.getWarehouse(),6));//472 ladu 
     sb.append(addSpaces("",47));//525
     sb.append(addSpaces(ring,2));//525
     sb.append(getTarne(order));//532
     sb.append(addSpaces("",306));//525

     System.out.println("Toode ja allowed " + toode + " " + allowed);
     
if(toode.trim().length() > 0 && allowed){
	xmlcounter ++;
     sb2.append("      <dta:OrderLine>" + ls);
     sb2.append("         <dta:LineNum>" + getLineNr(cou +1) + "</dta:LineNum>" + ls);
     sb2.append("         <dta:StockCode>" + escape(toode) + "</dta:StockCode>" + ls);
  
     sb2.append("         <dta:QtyOrdered>" + nf.format(kogus) + "</dta:QtyOrdered>" + ls);
     if(line.docType != null) {
    	 sb2.append("         <dta:PurchLineNum>" + line.docType + "</dta:PurchLineNum>" + ls);
         	 
     }
     
     
     if(!export){
    	   sb2.append("         <dta:TaxCode>01</dta:TaxCode>" + ls);
     }
  
}  	
if(!allowed){
	trouble = trouble +  "\nTellimuselt eemaldatti toode " + nimi;
}
     
     
     double disc = line.getDiscount();
     if(order.getStatus() == 60){
     	disc = 0;
     }
     double hind = 0;
     double hindArvu = 0;
     double hindAle = 0;
     if(disc > 0){
     		int ddd = (int)(line.getDiscount());       	
    			disc = ddd / (line.getKogus()) * 100;
    		    hind = getPrice(toode,"00",null,-1); //hinnakiri
    		   // hindAle = getPrice(toode,hinnakiri,cust,hind);
    		    if(hindAle != 0 ){
    		     hindArvu = hindAle * (100-disc)/100;
    		    }else{
    		     hindArvu = hind * (100-disc)/100;
    		    }
    		   
    		 //   hindArvu = hindArvu *(100 - custDisc)/100; // kommentisin v�lja kuna pole vaja 
    		    disc = (1- hindArvu/hind)* 100;
     				 
     		// siia uus arvutus teha zallahi alusel 
     		//sb.append(addSpaces(nf.format(disc), 6));
    		  sb.append(addSpaces((hind )+ "", 20)); // discount
     		  sb.append(addSpaces((ddd )+ "", 6)); // discount	
     		 
     		  if(toode.trim().length() > 0 && allowed){
     		   sb2.append("         <dta:CustDisc>" + addSpaces(nf.format(ddd),6) + "</dta:CustDisc>" + ls);			           
               sb2.append("         <dta:UnitPrice>" + nf.format(hind) + "</dta:UnitPrice>" + ls);
     		 }
     		  
     		  
     		 // System.out.println("hind ja ale " + hind + " " + ddd);
     		 addToDB(agent,cust,toode,kogus,ddd,"EST",kogus,true);
     		 
     		 if(aletext == null) {
     			aletext = ddd + "%  [" + kogus + "] "+ toode + " " + nimi + "\n" ;
     		 }else {
     			aletext = aletext + ddd + "%  [" + kogus + "] "+ toode + " " + nimi + "\n";
     		 }
     		//sendcomm = true;
     }else{
        	sb.append(addSpaces("", 6)); // discount
     }
     

   		 
   	   
  //   sb2.append("         <dta:AccStr>" + ls);
    // sb2.append("               <dta:AccDim1>" + agent + "</dta:AccDim1>" + ls);
    // sb2.append("         </dta:AccStr>" + ls);
   
     if(toode.trim().length() > 0 && allowed){
    	  sb2.append("         <dta:TextLines>" + ls);
    	     sb2.append("               <dta:TextLine></dta:TextLine>" + ls);
    	     sb2.append("         </dta:TextLines>" + ls);  	     
    	     sb2.append("      </dta:OrderLine>" + ls);
    	    	
     }
     
           sb.append(System.getProperty("line.separator"));
       cou++;
   	
	    }
	   // System.out.println("pandikas" + pandikas.size());
try{
	parent.setTellimustel(toode, kogus);
	   addDBLine(cust,scala,agent,order,toode,status + doknr + comment,kogus,ring,doknr);
    //   System.out.println("lisaatud baasi ");
}catch (Exception e ){
	e.printStackTrace();
	log.error(e);
}
       
      }
      
//lisame  
      if(false && pandikas.size()>0){ // kommentisin v�lja pandi asja Kalevi soovil sept 2018
        	Enumeration enumpant = pandikas.keys();
        	int aaa = 2;
         while(enumpant.hasMoreElements()){
         	
         	String kood = (String)enumpant.nextElement();
         	if(kood .trim().length()>0){
             	double pand = new Double((String)pandikas.get(kood)).doubleValue();
            xmlcounter ++;
            sb2.append("      <dta:OrderLine>" + ls);
            sb2.append("         <dta:LineNum>" + getLineNr(cou +aaa) + "</dta:LineNum>" + ls);
            sb2.append("         <dta:StockCode>" + escape(kood) + "</dta:StockCode>" + ls);
            sb2.append("         <dta:QtyOrdered>" + nf.format(pand) + "</dta:QtyOrdered>" + ls);
            if(!export){
           	   sb2.append("         <dta:TaxCode>01</dta:TaxCode>" + ls);
            }
            sb2.append("      </dta:OrderLine>" + ls);

            sb.append(addSpaces(cust, 10));//
            sb.append(addSpaces(cust, 10));//11      
            sb.append(addSpaces(cust, 10));//21
            sb.append(addSpaces(status + doknr, 20));//51
            sb.append(getTana());//59
            sb.append(getTarne(order));//68
            sb.append(addSpaces("",210)); //lisan tyhikuid hunniku 
            sb.append(addSpaces(agent, 3));//280
            sb.append(addSpaces(comment,70)); //lisan tyhikuid hunniku 350 kommentaar koht
            sb.append(addSpaces(kood, 10));//360 tootekood
            sb.append(addSpaces("",25)); //lisan tyhikuid hunniku 350
            sb.append(addSpaces("pandipakend" , 25));  //375 tootenimi
            sb.append(addSpaces("",25)); //lisan tyhikuid hunniku 435
            sb.append(addSpaces(nf.format(pand).replace(',','.').replaceAll(" ",""), 12));//447    
            sb.append(type);//448
            sb.append(addSpaces("",12)); //lisan tyhikuid hunniku 470
            sb.append(addSpaces(scala,10));
            sb.append(addSpaces(order.getWarehouse(),6));//472 ladu 
            sb.append(addSpaces("",47));//525
            sb.append(addSpaces(ring,2));//525
            sb.append(getTarne(order));//532
            sb.append(addSpaces("",306));//525
            
            sb.append(System.getProperty("line.separator"));
            
            aaa++;
        	}    	
         }
        }
     
      sb2.append("        </dta:OrderLines>" + ls);
      sb2.append("      <dta:TextLines />" + ls);
      sb2.append("    </dta:SalesOrder>" + ls);
      sb2.append("  </msg:Dta>" + ls);
      
      sb2.append("  <msg:ctx>" + ls);
      sb2.append("     <Options>" + ls);
      if(tagastus) {
    	  sb2.append("        <AllowNegativeQuantity>1</AllowNegativeQuantity>" + ls);  
      }
      sb2.append("        <IgnoreRequestedPrice>0</IgnoreRequestedPrice>" + ls);
   	  sb2.append("     </Options>" + ls);
      sb2.append("  </msg:ctx>" + ls);

      
      sb2.append("</msg:Req>" + ls);
      sb2.append("</msg:Body>" + ls);
      sb2.append("</msg:Msg>" + ls);
    
      
 
      
      setNumAddedLines(cou);
      String fn = doknr;
      fn = fn.replace('|', '-');
      fn = fn.replace('|', '-');
      if(doknr.trim().length()== 0){
    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
      }
      
      fn = fn + "-" + ( Calendar.getInstance().getTimeInMillis() + "").substring(9);
      
   //   System.out.println("On Discountiga tellimus  " + hasdiscount);

if(xmlcounter >0){
	if(order.getStatus() < 10 && hasdiscount){

	    try{
	    	String fileName3 = escape(parent.getProps().getProperty("conv.dirdroid")  + order.getAgent())
	    	+ "." + fn   +  ".xml";
	  	FileOutputStream fos = new FileOutputStream(fileName3);
	        fos.write(sb2.toString().getBytes());
	        fos.close();
	    	
	    }catch(Exception e){
	    	e.printStackTrace();
	    }	 	
	
	    String fileName = escape(parent.getProps().getProperty("conv.dir") + agent)  + "." + fn  + order.getWarehouse()+ ".txt";
	    System.out.println(fileName);
	      FileOutputStream fos = new FileOutputStream(fileName);
	      fos.write(sb.toString().getBytes());
	      fos.close();
	
	
	}else{

	    try{
	    	String fileName3 = escape(parent.getProps().getProperty("conv.dir2")  + order.getAgent())
	    	+ "." + fn   +  ".xml";
	  	FileOutputStream fos = new FileOutputStream(fileName3);
	        fos.write(sb2.toString().getBytes());
	        fos.close();
	    	
	    }catch(Exception e){
	    	e.printStackTrace();
	    }	 
	}

    try{
    	String fileName3 = escape(parent.getProps().getProperty("conv.dirhis")  + order.getAgent())
    	+ "." + fn   +  ".xml";
  	FileOutputStream fos = new FileOutputStream(fileName3);
        fos.write(sb2.toString().getBytes());
        fos.close();
    	
    }catch(Exception e){
    	e.printStackTrace();
    }	 
}

if("true".equals(parent.getProps().getProperty("make.oldfile"))){
    String fileName = escape(parent.getProps().getProperty("conv.dir") + agent)  + "." + fn  + order.getWarehouse()+ ".txt";
      FileOutputStream fos = new FileOutputStream(fileName);
      fos.write(sb.toString().getBytes());
      fos.close();
}

System.out.println("Saadame kommentaari " + sendcomm + " " + comm2 +" "+ aletext);

if(order.getStatus() < 5 && sendcomm) {
	  if(comm2 == null) {
		   comm2 = "Tellimus";
	   }
	  
	if((comm2 != null || aletext != null ) ){
		   Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(order.getAgent());
		 
	   sendCommentEmail( comm2+tarne ,aletext,doknr,order.getCompany(),  " " + getCustName(order.getCompany()) , " ",false,ag,parent);
	 }
}


if(prismaProds.length() > 0) {
	   sendPrismaEmail(prismaProds.toString(),doknr,order.getCompany() + " " + getCustName(order.getCompany()),parent);
		
}

if(realikProds.length() > 0) {
	   sendRealikEmail(realikProds.toString(),doknr,order.getCompany() + " " + getCustName(order.getCompany()),parent);
		
}
	  
	} catch (Exception e) {
		e.printStackTrace();
	}
 }
  
  
	private void sendCommentEmail(String comment1 ,String comment2,String doknr,String custid, String nimi , String chain, boolean price, Agent ag ,ImportPanel parent){
	   	try{
	   		
	  
	   		String comment = null ;
	   		if(comment1 != null && comment1.trim().length() > 0) {
	   			comment = "Comment : " + comment1 + "\n\n" ;
	   		}
	   		
	   		if(comment2 != null && comment2.trim().length() > 0) {
	   			if(comment == null) {
	   				comment = "";
	   			}
	   			
	   			comment = comment + "Price info : " + comment2 + "\n" ;
	   		}
	   		
	   		if(comment != null && comment.trim().length() >0){
	   			
	   			//System.out.println(comment);
	   			Email mail = new Email(null);
		  		   
	   			
	                mail.setSMTP(parent.getProps().getProperty("smtphost"));
	                if(parent.getProps().getProperty("smtpuser") != null)
	                mail.setUser(parent.getProps().getProperty("smtpuser"));
	                
	                if(parent.getProps().getProperty("smtppass") != null)
	                mail.setPass(parent.getProps().getProperty("smtppass"));
	                
	                if(parent.getProps().getProperty("smtpport") != null)
	                mail.setPort(parent.getProps().getProperty("smtpport"));
	                
		  	  		mail.setFilesTo(parent.getProps().getProperty("operatoremail"));
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		String agent = "";
		  	  	if(ag != null) {
		  	  		agent = ag.getName() ;
		  	  	}
		  	  		mail.setSubjectFilesTo("Order info : "+ agent + " " + custid + " " + nimi);
		  	  		String lisa = "";
		  	  		
		  	  		mail.setEncoding("utf-8");
		  	  		
		  	  		
		  	  		mail.setBodyHtml( "Customer code : " + custid +  "<br>Customer Name : " + nimi + "<br>" +
		  	  			    "Manager : " + agent + "<br><br>" + comment.replaceAll("\n", "<br>") + lisa  + "<br><br>");
		  	  		//mail.setBody("\nOrder :" +  doknr + " \nCustomer code :" + custid +  "\nCustomer Name :" + nimi + "\n" +
		  	  			//    "Manager :" + agent + "\n" +  "\n" + lisa + comment + "\n\n.");
		  	  		mail.setSendNotice(false);
		  	  		
		  	  		mail.send();
		  	  		System.out.println("\nOrder Comment " +  doknr + " " + lisa + custid + " "+ nimi + " " + agent + "\n\n" +
		  	  				  "\n >>>>>" + comment + "<<<<<");
		  				
	   		}
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	   }
 
	
	private void sendPrismaEmail(String comment1 ,String doknr,String custidName , ImportPanel parent){
	   	try{
	   		
	  
	   		String comment = null ;
	   		if(comment1 != null && comment1.trim().length() > 0) {
	   			comment =   comment1 + "\n\n" ;
	   		}
	   		
	   	
	   		
	   		if(comment != null && comment.trim().length() >0){
	   			
	   			//System.out.println(comment);
	   			Email mail = new Email(null);
		  		   
	   			
	                mail.setSMTP(parent.getProps().getProperty("smtphost"));
	                if(parent.getProps().getProperty("smtpuser") != null)
	                mail.setUser(parent.getProps().getProperty("smtpuser"));
	                
	                if(parent.getProps().getProperty("smtppass") != null)
	                mail.setPass(parent.getProps().getProperty("smtppass"));
	                
	                if(parent.getProps().getProperty("smtpport") != null)
	                mail.setPort(parent.getProps().getProperty("smtpport"));
	                
		  	  		mail.setFilesTo(parent.getProps().getProperty("prismaemail"));
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		String agent = "";
		  	  
		  	  		mail.setSubjectFilesTo("Prisma Aluse teema : " + custidName + " " + doknr );
		  	  		String lisa = "";
		  	  		
		  	  		mail.setEncoding("utf-8");
		  	  		
		  	  		
		  	  		mail.setBodyHtml( "Klient : " + custidName +  "<br>Tellimuse nr: " + doknr+  "<br><br>"+ comment.replaceAll("\n", "<br>") + lisa  + "<br><br>");
		  	  		//mail.setBody("\nOrder :" +  doknr + " \nCustomer code :" + custid +  "\nCustomer Name :" + nimi + "\n" +
		  	  			//    "Manager :" + agent + "\n" +  "\n" + lisa + comment + "\n\n.");
		  	  		mail.setSendNotice(false);
		  	  		
		  	  		mail.send();
		  	  		
		  				
	   		}
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	   }
	private void sendRealikEmail(String comment1 ,String doknr,String custidName , ImportPanel parent){
	   	try{
	   		
	  
	   		String comment = null ;
	   		if(comment1 != null && comment1.trim().length() > 0) {
	   			comment =   comment1 + "\n\n" ;
	   		}
	   		
	   	
	   		
	   		if(comment != null && comment.trim().length() >0){
	   			
	   			//System.out.println(comment);
	   			Email mail = new Email(null);
		  		   
	   			
	                mail.setSMTP(parent.getProps().getProperty("smtphost"));
	                if(parent.getProps().getProperty("smtpuser") != null)
	                mail.setUser(parent.getProps().getProperty("smtpuser"));
	                
	                if(parent.getProps().getProperty("smtppass") != null)
	                mail.setPass(parent.getProps().getProperty("smtppass"));
	                
	                if(parent.getProps().getProperty("smtpport") != null)
	                mail.setPort(parent.getProps().getProperty("smtpport"));
	                
		  	  		mail.setFilesTo(parent.getProps().getProperty("realiemail"));
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  	
		  	  
		  	  		mail.setSubjectFilesTo("Realiseerimiskeskuse toodete teema : " + custidName + " " + doknr );
		  	  		String lisa = "";
		  	  		
		  	  		mail.setEncoding("utf-8");
		  	  		
		  	  		
		  	  		mail.setBodyHtml( "Klient : " + custidName +  "<br>Tellimuse nr: " + doknr+  "<br><br>"+ comment.replaceAll("\n", "<br>") + lisa  + "<br><br>");
		  	  		//mail.setBody("\nOrder :" +  doknr + " \nCustomer code :" + custid +  "\nCustomer Name :" + nimi + "\n" +
		  	  			//    "Manager :" + agent + "\n" +  "\n" + lisa + comment + "\n\n.");
		  	  		mail.setSendNotice(false);
		  	  		
		  	  		mail.send();
		  	  		
		  				
	   		}
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	   }
 
 private HashSet getFelProds() {
	 String cmd = "SELECT  RTRIM(t.SC01001) AS toode, SC01164 as kate FROM SC010100 t  where SC01164 = 'FOODS'" ;
		
  HashSet res = new HashSet();


		try {
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);	
		
			 while (rs.next()) {
			    	String kood = rs.getString("toode").trim();
					String nimetus = rs.getString("kate").trim();

					res.add(kood);
			    }
			 
			 rs.close();
			 st.close();
			 
		}catch(Exception e){
			e.printStackTrace();
		}
			
   
			
		//System.out.println("Fel Prods size " + res.size());		
			
	return res;
}

private boolean isAllowed(String cust, String toode, int status) throws SQLException {
	String pay =  this.identifyCustomerPayer(cust);
	if(excludeProds != null){
		if(excludeProds.get(cust) != null){
			Vector vv = (Vector)excludeProds.get(cust);
			if(vv.contains(toode)){
				return false;
			}
		}else if(excludeProds.get(pay) != null){
			Vector vv = (Vector)excludeProds.get(pay);
			if(vv.contains(toode)){
				return false;
			}
		}
	}
	
	
	if(status != 77) {
		//kontrollime kas on pda tunnus - kui ei siis maha V.A. nende exceli vorm.
		System.out.println("uuring allow " + pdaProds.size() + " " + toode+ " " + pdaProds.contains(toode));
		 if(!pdaProds.contains(toode)) {
			 return false;
		 }
	}
	
	
	
	return true;
}

private String getDateStringPool(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	
	  
	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day);
	    sb.append(".");
		    
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month);
	    return sb.toString();
	  }
 
 private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    sb.append(cal.get(Calendar.YEAR));
	    sb.append("-");
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month);
	    sb.append("-");
	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day);
	    return sb.toString();
	  }

 private String getLineNr(int i){
	  	String res = i + "";
	  	if (res.length() == 1){
	  		res = "0000" + res + "0";
	  	} else if (res.length() == 2){
	  		res = "000" + res + "0";
	  	} else if (res.length() == 3){
	  		res = "00" + res + "0";
	  	} else if (res.length() == 4){
	  		res = "0" + res + "0";
	  	} else if (res.length() == 5){
	  		res = "" + res + "0";
	  	}
	  	return res;
	  }

 
 
 public double getPrice(String id, String hinnakiri, String klient,double stPrice) throws SQLException {

	  	
	    String  sql1 = 	    
		"SELECT SC18009 as disc, SC18008 AS price " +
	               "  FROM SC180100 INNER JOIN SL010100 ON  left(SC18002,7)= SL01056 LEFT JOIN SY240100 on SC18009=SY24002 and SY24001='BG' WHERE " + 
	                " SC18001='C' AND CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011 " +
					" and SL01001 = '" + klient + "' and SUBSTRING(SC18002,11,35)='" + id + "' ";
	    
	         //  System.out.println(sql1);
			    double acc = 0;
			    Statement st1 = con.createStatement();
			    ResultSet rs1 = st1.executeQuery(sql1);
			    
	    if (klient != null){
		    while (rs1.next()) {
		    	double dd = rs1.getDouble("disc");
			      acc = rs1.getDouble("price") *((100-dd)*0.01);
			   //   System.out.println("hind ja ale " + acc + " " + dd );
			    }	
		    
		    String sql0 = " SELECT SL01002 AS nimi,SL01001 AS kood, case when  SC45005 >0 then SC45005 else 0 end AS ale" +
	        "  FROM SL010100 kl INNER JOIN SC450100    ON SL01036=SC45003  " +
	       "   WHERE SL01001 ='" + klient + "' ORDER BY SL01002";
	   st1 = con.createStatement();
	   rs1 = st1.executeQuery(sql0);
	   double ale = -1;
	   double alehind = 0;
	  if (rs1.next()) {
	  	ale = rs1.getDouble("ale") ;
	  	
	  }
	  rs1.close();
	  st1.close();
	  
	  
	  
	  if(ale != -1 ){
		  alehind = stPrice * (100d-ale)/100d;
		  if(acc == 0){
			  acc = alehind;
		  }else if(acc > alehind){
			  acc = alehind;
		   }
	      }	    
	     }

		  
		 	
		
	     
	    
	    
	    sql1 = "  SELECT  SC39004 AS price  FROM SC390100  WHERE  SC39002='" + hinnakiri+ "' and  SC39001 = '" + id + "'"; 	    
	//    System.out.println("Teine kontroll " + sql1);
	 
	     st1 = con.createStatement();
	     rs1 = st1.executeQuery(sql1);
	if (hinnakiri != null && acc == 0){
	    while (rs1.next()) {
	      acc = rs1.getDouble("price") ;
	    }	
	}

	  rs1.close();
	  st1.close();
	
	    
	    
	    
			  //  System.out.println("Klient: " + acc + " " + id);
			  if(acc == 0){
				  sql1 = "  SELECT  SC39004 AS price  FROM SC390100  WHERE  SC39002='00' and  SC39001 = '" + id + "'";
			  	    
				   // System.out.println(sql1);
				    

				   st1 = con.createStatement();
				   rs1 = st1.executeQuery(sql1);

				    while (rs1.next()) {
				      acc = rs1.getDouble("price") ;

				    }
				    
				    rs1.close();
				    st1.close();
			  }
			    

			  //  System.out.println("Klient: " + acc + " " + id);
			    return acc;

			  }



 private double makeRoundUp(double kogus,double rounder){

	 double ii = Math.round(kogus/rounder + 0.5);	 
	 return rounder * ii;
 }
 
 private boolean inList(String intr){
	  
	  if(intr.equals("K504")){
		 return true;
	  }
	  if(intr.equals("M12320")){
			 return true;
		  }
	  if(intr.equals("M19031")){
			 return true;
		  }
	  
	  
	  if(intr.equals("K501")){
			 return true;
		  }
	  if(intr.equals("K511")){
				 return true;
		  } 
	  if(intr.equals("K503")){
				 return true;
		  } 
	  if(intr.equals("K517")){
				 return true;
		  } 
	  if(intr.equals("K509")){
				 return true;
		  } 
	  if(intr.equals("K502")){
				 return true;
		  } 
	  if(intr.equals("K512")){
				 return true;
		  } 
	  if(intr.equals("K510")){
			 return true;  
	  }
		   if(intr.equals("K513")){
				 return true;
		  }
		  if(intr.equals("K506")){
				 return true;
		  }
		  if(intr.equals("K507")){
				 return true;
		  }
		  if(intr.equals("K515")){
				 return true;
		  }
		  if(intr.equals("K508")){
				 return true;
		  }
		  if(intr.equals("K516")){
				 return true;
		  }
	
	 return false;
 }
 
  public String getDriverVersion(){
    return "KalevImport";
  }




  private String escape(String s){
    if (s == null) return s;
    StringBuffer sb = new StringBuffer();
    for (int i = 0 ; i < s.length() ; i++){
      char c = s.charAt(i);
      if (c == '&'){
        sb.append("&amp;");
      } else if (c == '<'){
        sb.append("&lt;");
      } else if (c == '>'){
        sb.append("&gt;");
      } else if (c == '\n'){
        sb.append(" ");
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  


  private String getTarne(Order order){
    Calendar cal = Calendar.getInstance();
    Calendar dat = Calendar.getInstance();
    dat.setTimeInMillis(order.getShipCal().getTimeInMillis());

    String result = null ;
    if (dat != null){
    	 String yy = (dat.get(Calendar.YEAR))+"";

    	  String mm = (dat.get(Calendar.MONTH)+1)+"";
    	  if(mm.length()== 1){
    	  	mm = "0"+mm;
    	  }
    	  
    	  
    	  String dd = (dat.get(Calendar.DATE))+"";
    	  if(dd.length()== 1){
    	  	dd = "0" + dd;
    	  }
    	  
    	  
    	  result = yy + mm + dd;
    	// System.out.println("TARNEP�EV SHIP0  " + result + " " + dat.get(Calendar.DATE));
    	  
    }else{
     String yy = (cal.get(Calendar.YEAR))+"";

	  String mm = (cal.get(Calendar.MONTH)+1)+"";
	  if(mm.length()== 1){
	  	mm = "0"+mm;
	  }
	  
	  int d = cal.get(Calendar.DATE);
	  String dd = d+"";
	  if(dd.length()== 1){
	  	dd = "0" + dd;
	  }
	  

	//  result = yy.substring(2,4) + mm + dd;
	  result = yy + mm + dd;
    }
    return result;
  }
  
  private String getTarneLyh(Order order){
	    Calendar cal = Calendar.getInstance();
	    Calendar dat = Calendar.getInstance();
	    dat.setTimeInMillis(order.getShipCal().getTimeInMillis());

	    String result = null ;
	    if (dat != null){
	    	if (dat.getTime().after(cal.getTime())){
	    		//dat.setTimeInMillis(dat.getTimeInMillis() - 86400000 ); siin tekkis jama 
	    	}
	    	  dat.add(Calendar.DATE, -1);
       
	    	  String mm = (dat.get(Calendar.MONTH)+1)+"";
	    	  if(mm.length()== 1){
	    	  	mm = "0"+mm;
	    	  }
	    	  
	    	  
	    	  String dd = (dat.get(Calendar.DATE))+"";
	    	  if(dd.length()== 1){
	    	  	dd = "0" + dd;
	    	  }
	    	  
	    	  
	    	  result = dd + mm ;
	    	 // System.out.println("TARNEP�EV SHIP " + result + " " + dat.toGMTString());
	    	  
	    }else{
	     

		  String mm = (cal.get(Calendar.MONTH)+1)+"";
		  if(mm.length()== 1){
		  	mm = "0"+mm;
		  }
		  
		  int d = cal.get(Calendar.DATE);
		  String dd = d+"";
		  if(dd.length()== 1){
		  	dd = "0" + dd;
		  }
		   result = dd + mm;
		//  System.out.println("TARNEP�EV " + result + " " + dat.toGMTString());
	    }
	    return result;
	  }

  
  private String getTarneXML(Order order){
	    Calendar cal = Calendar.getInstance();
	    Calendar dat = Calendar.getInstance();
	    dat.setTimeInMillis(order.getShipCal().getTimeInMillis());

	    String result = null ;
	    if (dat != null && dat.after(cal)){
	    	 String yy = (dat.get(Calendar.YEAR))+"";

	    	  String mm = (dat.get(Calendar.MONTH)+1)+"";
	    	  if(mm.length()== 1){
	    	  	mm = "0"+mm;
	    	  }
	    	  
	    	  
	    	  String dd = (dat.get(Calendar.DATE))+"";
	    	  if(dd.length()== 1){
	    	  	dd = "0" + dd;
	    	  }
	    	  
	    	  
	    	  result = yy + "-" + mm + "-" + dd;

	    	  //System.out.println("TARNEP�EV SHIP0  " + result + " " + dat.get(Calendar.DATE) + " " );
	    	  
	    }else{
	     String yy = (cal.get(Calendar.YEAR))+"";

		  String mm = (cal.get(Calendar.MONTH)+1)+"";
		  if(mm.length()== 1){
		  	mm = "0"+mm;
		  }
		  
		  int d = cal.get(Calendar.DATE);
		  String dd = d+"";
		  if(dd.length()== 1){
		  	dd = "0" + dd;
		  }
		  

		//  result = yy.substring(2,4) + mm + dd;
		  result = yy + "-" + mm + "-" + dd;
	    }
	    return result;
	  }

  
  
  private String getTana(){
	    Calendar cal = Calendar.getInstance();

	    String result = null ;
	    	 String yy = cal.get(Calendar.YEAR)+"";
	    	  String mm = (cal.get(Calendar.MONTH) +1 )+"";
	    	  if(mm.length()== 1){
	    	  	mm = "0"+mm;
	    	  }
	    	  
	    	  
	    	  String dd = cal.get(Calendar.DAY_OF_MONTH)+"";
	    	  if(dd.length()== 1){
	    	  	dd = "0" + dd;	  
	            }
	    	  result = yy + mm + dd;
			 // System.out.println("T�NAP�EV " + result + " " + dat.toGMTString());
	    return result;
	  }
  
  public static final String addSpaces(String s, int totalLen){
	  if(s== null){
		  return "";
	  }
    int len = s.length();
    StringBuffer sb = new StringBuffer(s);
    for (int i=len; i < totalLen ; i++){
      sb.append(' ');
    }
    if( len > totalLen){
    	return s.substring(0,totalLen);
    }
    return sb.toString();
  }
	private Connection getNewConEeva() throws SQLException {
		return DriverManager.getConnection(url3, "", "");
	}
  
  public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  
  private NumberFormat calNF = NumberFormat.getInstance();

	private String url3 = "jdbc:odbc:eeva";
  private String url = "jdbc:odbc:orklaDB";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private ImportPanel parent;
  private int cou = 0;
  private String trouble = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return trouble;
}
}
