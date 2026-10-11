

/**
 * @author Aivar  Merisalu

 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;




import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class KalevIntrFelImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public KalevIntrFelImportDriver(ImportPanel parent) {
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
    
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  public String identfySorting (String prodID){
	  	


	    String sql1 = " Select SC01014 AS sort FROM SC012100 pr  where pr.SC01001='" + prodID + "'";
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
		
		
     
		    String sql1 = "   insert into mj_discountlines (agent,custid,sentdate,prodid,amount,discount) values " +
	 "  ('" +agentid + "','" + custid + "',getdate(),'" + toode + "'," +kogus + "," + disc + ") ";
		  
		    Statement st1;
			try {
				st1 = con.createStatement();
				st1.executeUpdate(sql1);
				st1.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		
    }
  
  
  public boolean makeRounding (String prodID){
	  	


	    String sql1 = " Select SC01097 AS sort FROM SC012100 pr  where pr.SC01001='" + prodID + "'";
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
//M14544    M14544    M14544    MAXJ10701 45283067952010010720100108                                                                                                                                                                                                                  54                                                                       34013                              Linda kirsitasku 3x55g                            6           1            5500003033J1                                                     20100108                                                                                                                                                                                                                                                                                                            


	    String sql1 = " Select SC01094 AS ladu FROM SC012100 pr  where pr.SC01001='" + prodID + "'";
	  log.info("1_LINE_SQL: " + sql1);

	    String ladu = "23";

	    Statement st1;
		try {
			st1 = con.createStatement();
		
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	ladu = rs1.getString("ladu");
	    //	System.out.println("Ladu k�es 1 ;" + ladu );
	    	if(ladu == null || ladu.trim().length() == 0){
	    		ladu = "23";
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
		     System.out.println("Felixi kliendikood k�es  ;" + ring );
			    return ring;
			  }

	  
	  
  
  
  public String identfyRing(String id){
	//M14544    M14544    M14544    MAXJ10701 45283067952010010720100108                                                                                                                                                                                                                  54                                                                       34013                              Linda kirsitasku 3x55g                            6           1            5500003033J1                                                     20100108                                                                                                                                                                                                                                                                                                            


		    String sql1 = "SELECT SL01027 AS kood FROM SL012100 where SL01001  =  '" + id + "'";	
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
	     System.out.println("Ring k�es  ;" + ring );
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
      
	  Order ord23 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ordFELIX = new Order(leiaFelixiKlkood(order.getCompany()),order.getAgentFirstName());
	  Order ordJ1 = new Order(getKKood(order.getCompany()),order.getAgentFirstName());

	     
	  ord23.setAgentCode(order.getAgentCode());
	  ord23.setDokNr(order.getDokNr());
	  ord23.setInfo(order.getInfo());
	  ord23.setTeostaja((order.getTeostaja()));
	  ord23.setChanged(order.isChanged());
	  ord23.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord23.setStatus(order.getStatus());
	  ord23.setShipTime(order.getShipTime());
	  ord23.setWarehouse(order.getWarehouse());
	  ord23.setCurrency("KALEV");
	  
	  ordFELIX.setAgentCode(order.getAgentCode());
	  ordFELIX.setDokNr(order.getDokNr());
	  ordFELIX.setInfo(order.getInfo());
	  ordFELIX.setTeostaja((order.getTeostaja()));
	  ordFELIX.setChanged(order.isChanged());
	  ordFELIX.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordFELIX.setStatus(order.getStatus());
	  ordFELIX.setShipTime(order.getShipTime());
	  ordFELIX.setWarehouse(order.getWarehouse());
	  ordFELIX.setCurrency("FELIX");
	  
	  
	  ordJ1.setAgentCode(order.getAgentCode());
	  ordJ1.setDokNr(order.getDokNr());
	  ordJ1.setInfo(order.getInfo());
	  ordJ1.setTeostaja((order.getTeostaja()));
	  ordJ1.setChanged(order.isChanged());
	  ordJ1.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordJ1.setStatus(order.getStatus());
	  ordJ1.setShipTime(order.getShipTime());
	  ordJ1.setWarehouse(order.getWarehouse());
	  ordJ1.setCurrency("KALEV");
	
	  Vector lines = order.getOrderLines();
	  Enumeration enums = lines.elements();
	  while  (enums.hasMoreElements()){
		  OrderLine lin = (OrderLine)enums.nextElement();
		
		     String itemid = lin.getToode();
		     if(itemid.startsWith("FE")){
		    	 ordFELIX.add(lin.getToode().substring(2), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
	     }else{
		    	 String ware =  identfyWare(itemid);
				    String sorting =  identfySorting(itemid);
				    System.out.println("xxx " + ware + " " + sorting);
				     ord23.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			sorting + " " + lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
				         
		     }
		   
		     

	  }





	  if(ord23.getOrderLines() != null && ord23.getOrderLines().size()>0){
		  result.add(ord23);  
	  }

	  if(ordFELIX.getOrderLines() != null && ordFELIX.getOrderLines().size()>0){
		  result.add(ordFELIX);  
	  }
	 
	  if(ordJ1.getOrderLines() != null && ordJ1.getOrderLines().size()>0){
		  result.add(ordJ1);  
	  }
	 
	 System.out.println("Result =" +order.getDokNr()
			 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }



  

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	
  	if(id.equals("stockmann")){
  		return "M10620";
  	}
  	
  	 System.out.println("Kliendi M��ramine: "  + id + " " + regnr);
  	
  	    sql1 = "SELECT SL01001 AS kood FROM SL012100 where SL01125  =  '" + id + "'";	
	
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim() ;
    }
    rs1.close();
    st1.close();
    String uus = null;
    if(id!= null &&id.trim().length() >10){
    	uus = id.trim().substring(id.trim().length()-10,id.trim().length());
    }
    
    if(uus != null && acc == null){
    	sql1 = "SELECT SL01001 AS kood FROM SL012100 where SL01125  =  '" + uus + "'";	
        System.out.println(sql1);

         st1 = con.createStatement();
         rs1 = st1.executeQuery(sql1);

        while (rs1.next()) {
          acc = rs1.getString("kood").trim() ;
        }
        rs1.close();
        st1.close();
    }
 

    
if(acc == null){
  sql1 = "SELECT SL01001 AS kood FROM SL012100 where SL01001  =  '" + id + "'";	
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
 	 String	sql1 = "SELECT SL01035 as agent FROM SL012100 WHERE SL01001='" + id +"'"; 
 	
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
 
  
  public String getPandiPakend(String toode) throws SQLException{
 	 
    String sql1 = " SELECT RTRIM(SC01150) AS pant FROM SC012100  WHERE RTRIM(SC01001)=   '" + toode.trim() + "'";
    String prod = null;
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      prod = rs1.getString("pant").trim();
    }
       System.out.println(toode + " prod1 " + prod);
    rs1.close();
    st1.close();
    return prod;
 }
 
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	
	    String prod = null;
	 String ea = ean;
	 if(ea == null){
		 ea = "XXXXXXXXXXX";
	 }
	 if (ea.length() == 0 ){
		 ea = "XXXXXXXXXX";
	 }
	    
	   String pr = prodID;
	   if(pr == null){
		   pr = "000000000";
	   }
	  
	  
    String sql2 = " SELECT RTRIM(SC01001) AS toode FROM SC012100  WHERE (RTRIM(SC01132)=   '" + ea 
                              + "' or RTRIM(SC01003) = '" + ea + "') and SC01014 != ''";

    String sql1 = "SELECT RTRIM(SC01001) AS toode FROM SC012100  WHERE RTRIM(SC01001)=  '" + pr + "' and SC01014 != ''";



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

    System.out.println(prodID + " prod1 " + prod);
    rs1.close();
    st1.close();

    return prod;
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

  
  private double getMultiplier(String toode , int status){
  	double res = 1;
  	
    String sql1 = " SELECT (case when SC01015!='' then cast(SC01015 as integer) else 1 end) AS res , SC01097 as aaa FROM SC012100  WHERE RTRIM(SC01001)=   '" + toode + "'";

			try{
				 Statement st1 = con.createStatement();
				    ResultSet rs1 = st1.executeQuery(sql1);
			
				      while (rs1.next()) {
				        res = rs1.getDouble("res");
				        int aaa = rs1.getInt("aaa");
				        if(aaa >0 ){ //&& status < 50 raneti soovil eemaldasin ja k�ik telid l�hevad
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
	  	
	    String sql1 = " SELECT (case when SC01015!='' then cast(SC01015 as integer) else 1 end) AS res FROM SC012100  WHERE RTRIM(SC01001)=   '" + toode + "'";

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
	  // System.out.println("Multiplier " + res);
	  // System.out.println(sql1);
	  	return res;
	  }

  public void makeOrder(Order order) throws SQLException{
	   System.out.println("inff" + order.getInfo());
	   Vector splitted =  splitOrder(order);
         cou = 0;
		  Enumeration en = splitted.elements();
		  while(en.hasMoreElements()){
			  Order ord = (Order)en.nextElement();
			  System.out.println("inff3 " + ord.getInfo());
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
			     
            if(ord.getCurrency().equals("KALEV")){
            	System.out.println("Kalevi Tellimus " + ord.getOrderLines().size() + " rida");
                makeOutput(ord);	
            } 
            if(ord.getCurrency().equals("FELIX")){
            	System.out.println("Felixi Tellimus " + ord.getOrderLines().size() + " rida");
                makeOutputFelix(ord);	
            }  
            
		  }
  //  addListings(order);
  }
  
  
  
  private void makeOutputFelix(Order order){
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
			
			System.out.println("Tuvastatud agent :" + agent  + " " + ref + " " + order.getReference() + " " + order.getTeostaja() );
			
			String com = order.getEmailBody();
			if ( com == null ){
				com =  " ";
			}		
			if(order.getWarehouse() == null || order.getWarehouse().trim().length() == 0){
		    	 order.setWarehouse("1"); //smarten
		     }
			
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
	          String unit = line.getUnit();
	          
	
    
	     String comment = order.getInfo();
	     
	     
	     if(comment == null || comment.trim().length() == 0 ){
	    	comment = "";
	     }else{
	    	if(comment.trim().length()>70){
	    		comment = comment.substring(0,70);	
	    	}
	     }

	
	     
	    
	     double disc = line.getDiscount();
	     
	     double hind = 0;
	     		int ddd = (int)(line.getDiscount());       	
	    			disc = ddd / (line.getKogus()) * 100;
	     System.out.println("hind ja ale " + hind + " " + ddd + " ladu : " + ware);
	     	
	     
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
	 	      System.out.println(fileName);
	 	        FileOutputStream fos = new FileOutputStream(fileName);
	 	        fos.write(sb.toString().getBytes());
	 	        fos.close();
	      }catch(Exception e){
	    	  e.printStackTrace();
	    	  String fileName = escape(parent.getProps().getProperty("conv3.dir") + agent)  + "." + fn  + order.getWarehouse()+ ".txt";
	 	      System.out.println(fileName);
	 	        FileOutputStream fos = new FileOutputStream(fileName);
	 	        fos.write(sb.toString().getBytes());
	 	        fos.close();
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
 	 String	 sql2 = "SELECT RTRIM(SC01002) AS toode FROM SC012100  WHERE RTRIM(SC01001)=  '" + aa + "'";
			
			Statement st1 = con.createStatement();
			ResultSet rs1 = st1.executeQuery(sql2);
			
			 if (rs1.next()) {
			   res = rs1.getString("toode").trim();
			 }
			
	rs1.close();
	st1.close();
  	return res ;
  }
  
  
  private boolean getAlus(String cust) throws SQLException{
	  boolean res = false;
	  
		 String	 sql2 = "SELECT (SELECT SL01009 from SL012100 where SL01001 =k.SL01056) AS jama FROM SL012100 k WHERE (k.SL01001)=  '" + cust+ "'";
			
			Statement st1 = con.createStatement();
			ResultSet rs1 = st1.executeQuery(sql2);
			
			 if (rs1.next()) {
				String aa = rs1.getString("jama");
			   if(aa != null && aa.equals("P800011")){
				   res = true;
			   }
			 }
			
	rs1.close();
	st1.close();
	
	  return res;
  }
  
  private String getAgentSym(String agent) {
	  String res = agent;
	  try{
		  String	 sql2 = "SELECT SY24003 AS nimi  FROM SY242100 WHERE SY24001='BK' and (SY24002)=  '" + agent+ "'";
			
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
  
  
  public String getHinnakiri(String custid ){


	    String sql1 = " select SL01029 as hinnakiri from SL012100   where SL01001= '" + custid + "' ";
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

  
  
 private void makeOutput(Order order){
 	StringBuffer sb = new StringBuffer();
 	 nf.setGroupingUsed(false);
 	 
 	
 	 // SIIA TEE ROUNDIN statuse j�rgi ja kampaania koguse kontroll ja koodi muutmine 
 	 
 	try {
 		 String scala = getAndUpdateNext()+ "";
 	
      
 	    String cust = order.getCompany();
 		//String hinnakiri = getHinnakiri(cust);
		String agent = this.identifyCustomerAGENT(cust);
		String agent2 = order.getAgent();
        String ring = identfyRing(cust);
		
		System.out.println("Tuvastatud agent :" + agent + " tellimusel " + agent2 );
	
		String ref = order.getReference();
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
		 String doknr = order.getDokNr();
        if(doknr == null){
        	doknr = ref;
        	if(doknr.indexOf("-") != -1){
        		doknr = doknr.substring(doknr.indexOf("-"));
        	}
        }
        
         String type = "1";
         boolean house = false;
         
         if(order.getStatus()< 50 && !house && !order.getAgent().equals(parent.getProps().getProperty("defaultagent"))){
        	 System.out.println("Agent v�eti tellimusest" + order.getAgent());
        	 agent = order.getAgent();
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
         
       
         
    Vector lines = order.getOrderLines();
      for (int i = 0; i < lines.size(); i++) {
          System.out.println("111");
      	OrderLine line = (OrderLine)lines.elementAt(i);
          String toode = line.getToode();
         try{
        	
          if(toode != null && order.getStatus()>1){
         	  //kontrollime kas kampaania tootel on piisav laosaldo ja kehtiv kuup�ev, et seda otse klientidele panna
         	  String sql = "  SELECT  SC01010 as datum, RTRIM(SC03001) AS toode    " +
         	      "  FROM SC032100 INNER JOIN SC012100 ON SC03001= SC01001 WHERE (RTRIM(SC03001) = '" + toode + "-EE' " +
         	      		" or RTRIM(SC03001) = '" + toode + "-EE1' or RTRIM(SC03001) = '" + toode + "-EE2')" +
         	      				" and SC01014 != '' AND SC03003-SC03004 > " + (line.getKogus() + parent.getTellimustel(toode) 
         	      						+ parent.getTellimustel(toode+ "-EE") + parent.getTellimustel(toode+ "-EE1")
         	      						+ parent.getTellimustel(toode+ "-EE2"));
         	  Statement st1 = con.createStatement();//and SC01014 != '' 
         	  ResultSet rs1 = st1.executeQuery(sql);
         	 System.out.println("222");
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
         	 System.out.println(sql +  " " + toode + " " +parent.getTellimustel(toode) + " " + datum.compareTo(this.getTana()) + datum + " " + getTana()); 
         	  rs1.close();
         	  st1.close();
         	 System.out.println("333");
           }
          
         }catch(Exception e){
        	 e.printStackTrace();
         }
          
          
         try{
         //see blokk on selleks et Esindajatelyt tulnud tellimusel kontrollida kas on kampaania tootel piisav kogus ja kui ei siis asendada kood p�hi koodiga 	
             if(toode != null  && (toode.endsWith("-EE") || toode.endsWith("-EE1") || toode.endsWith("-EE2") )
            		 && order.getStatus() == 0){
            	  
            	  String sql = "  SELECT  SC01010 as datum, RTRIM(SC03001) AS toode    " +
            	      "  FROM SC032100 INNER JOIN SC012100 ON SC03001= SC01001 WHERE RTRIM(SC03001) = '" + toode + "' and SC01014 != ''" +
            	      		" AND SC03003-SC03004 > " + (line.getKogus() + parent.getTellimustel(toode)
            	      				+ parent.getTellimustel(toode+ "-EE") + parent.getTellimustel(toode+ "-EE1")
     	      						+ parent.getTellimustel(toode+ "-EE2"));
            	  Statement st1 = con.createStatement();//and SC01014 != '' 
            	  ResultSet rs1 = st1.executeQuery(sql);
            	  System.out.println("444");
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
            	 System.out.println(sql + " " +  parent.getTellimustel(toode)); 
            	  rs1.close();
            	  st1.close();
            	  System.out.println("555");
              }else if(order.getStatus() == 0){
            	  //siin vaatame kampaania alguse puhul kas p�hitootel on PDA tunnus olemas kui ei siis kontrillime
            	  //kapmaania koodi olemasolu ja laoj��ki 
            	  
           	  String sql = "  SELECT  RTRIM(SC01001) AS toode    " +
        	      "  FROM SC012100  WHERE RTRIM(SC01001) = '" + toode + "' and SC01014 != '' or SC01115 = 'L'";
        	  Statement st1 = con.createStatement();//and SC01014 != '' 
        	  ResultSet rs1 = st1.executeQuery(sql);
        	  System.out.println("666");
        	  boolean ok = false;
        	  if (rs1.next()) {
                 ok = true;
        	    }
        	 System.out.println(sql); 
        	  rs1.close();
        	  st1.close();
        	  
        	  if(!ok){
        		  //Kontrollime kas kampaania toode on olemas aktiivne 
        		  sql  = "  SELECT  SC01010 as datum, RTRIM(SC03001) AS toode    " +
         	      "  FROM SC032100 INNER JOIN SC012100 ON SC03001= SC01001 WHERE (RTRIM(SC03001) = '" + toode + "-EE' " +
   	      		  " or RTRIM(SC03001) = '" + toode + "-EE1' or RTRIM(SC03001) = '" + toode + "-EE2') and SC01014 != '' AND SC03003-SC03004" +
   	      		  " > " + (line.getKogus() + parent.getTellimustel(toode) + parent.getTellimustel(toode+ "-EE") + parent.getTellimustel(toode+ "-EE1")
 	      						+ parent.getTellimustel(toode+ "-EE2"))+
   	      		  " order by SC03001 desc";
   	 
        		  st1 = con.createStatement();//and SC01014 != '' 
        		  rs1 = st1.executeQuery(sql);
               System.out.println("777");
        	  if (rs1.next()) {
                  toode = rs1.getString("toode").trim();
        	    }
        	 System.out.println( "Teine kontroll kui peatootel pole aktiivne pdas tunnus\n" +sql +  parent.getTellimustel(toode) ); 
        	  rs1.close();
        	  st1.close();
        		  
        	  }
            	  
              }
             
            }catch(Exception e){
           	 e.printStackTrace();
            }
         
     
          double kogus = line.getKogus();         
          System.out.println("888");
          double rounder = getMultiplier(toode,order.getStatus());
          if(order.getStatus() == 0){
        	  rounder = 1;
          }
          
          System.out.println("999");

             if(ware.equals("23") && makeRounding(line.getToode()) && !notInt(kogus) && notInt(kogus/rounder)){
            	 // kui kogus on t�isarv ja m��dud kogus pole kastisuhtes t�isarv siis ymmardame ylesse
           	  kogus = makeRoundUp(kogus,rounder);
             } 
          
             String unit = line.getUnit();
             if(unit != null && unit.equals("DPA")){ 
  	      	   System.out.println(unit + " " + toode + " enne " + kogus );
  	      	   if(line.getBonus()> 1){
  	      		   kogus = kogus * line.getBonus();
  	      		   System.out.println(unit + " " + toode + " bonus " + kogus );
  	      	   }else{
  	      		 //  kogus = kogus * getInCases(itemid);
  	      		   System.out.println(unit + " " + toode + " getinCases" + kogus );
  	      	   }  	   
  	      	       
  	                }
            nf.setMaximumFractionDigits(2);
            System.out.println("1000");
          if (i == 0 ){
          	if( line.getKogus() < 0){
          	//	System.out.println("kogus " + line.getKogus());
             // 	doknr = getDoknr("OR05");
              	type = "8";
          	}         	
          }

          String nimi = getNimi(toode);
     if(nimi == null){
    	 nimi = line.getTooteNimi();
     }
        
     System.out.println("1100");
     String comment = order.getInfo();
     
     
     if(comment == null || comment.trim().length() == 0 ){
    	comment = "";
     }else{
    	if(comment.trim().length()>70){
    		comment = comment.substring(0,70);	
    	}
    	}

     
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
     

     String tarnekell =  order.getShipTime();
     if(order.getStatus() == 60 || order.getStatus() == 80){
    	 tarnekell = order.getWarehouse();
    	 order.setWarehouse(" ");
     }
     if(order.getWarehouse() == null){
    	 order.setWarehouse("23");
     }
     System.out.println("Kella aeg:" + tarnekell +  " " + order.getWarehouse());
     if(tarnekell != null && tarnekell.trim().length() != 0 ){
    	 if(comment.length() != 0){
    		 comment = addSpaces(comment,35) + tarnekell; 
    	 }else{
    		 comment = addSpaces("",35) + tarnekell;
    	 }
     	}
     
     if(order.isChanged()){
    	 comment = "Muudetud!!!" + comment;
    	 if(comment.trim().length()>70){
     		comment = comment.substring(0,70);	
     	}
     }
     System.out.println("13000");
	   
	    if(kogus < 100000){
	    if(logistika != null){
	    	 System.out.println("muudame kliendikoodi " + logistika);
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
     sb.append(addSpaces(comment,70)); //lisan tyhikuid hunniku 350 kommentaar koht
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
     System.out.println("hind ja ale " + hind + " " + ddd);
     		 addToDB(agent,cust,toode,kogus,ddd,"EST",kogus,true);
     	
     }else{
        	sb.append(addSpaces("", 6)); // discount
     }
           sb.append(System.getProperty("line.separator"));
       cou++;
   	
	    }
	    System.out.println("14000");
try{
	parent.setTellimustel(toode, kogus);
	   addDBLine(cust,scala,agent,order,toode,status + doknr + comment,kogus,ring,doknr);
       System.out.println("lisaatud baasi ");
}catch (Exception e ){
	e.printStackTrace();
	log.error(e);
}
       
      }
      
//lisame aluse 
      if(getAlus(cust) ){
    	  sb.append(addSpaces(cust, 10));//
          sb.append(addSpaces(cust, 10));//11      
          sb.append(addSpaces(cust, 10));//21
          sb.append(addSpaces(status + order.getDokNr(), 20));//51
          sb.append(getTana());//59
          sb.append(getTarne(order));//68
          sb.append(addSpaces("",210)); //lisan tyhikuid hunniku 
          sb.append(addSpaces(agent, 3));//280
          sb.append(addSpaces("",70)); //lisan tyhikuid hunniku 350
          sb.append(addSpaces("P800011", 10));//360 tootekood
          sb.append(addSpaces("",25)); //lisan tyhikuid hunniku 350
          sb.append(addSpaces("Euroalus" , 25));  //375 tootenimi
          sb.append(addSpaces("",25)); //lisan tyhikuid hunniku 435
          sb.append(addSpaces("1", 12));//447    
          sb.append(type);//448
          sb.append(addSpaces("",12)); //lisan tyhikuid hunniku 470
          sb.append(addSpaces(scala,10));
          sb.append(addSpaces(order.getWarehouse(),6));//472 ladu 
          sb.append(addSpaces("",47));//525
          sb.append(addSpaces(ring,2));//525
          sb.append(addSpaces(getTarne(order),8));//532

             sb.append(addSpaces(" ",300));//310
             sb.append(System.getProperty("line.separator"));
  
      }
     
      
      setNumAddedLines(cou);
      String fn = doknr;
      if(doknr.trim().length()== 0){
    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
      }
      String fileName = escape(parent.getProps().getProperty("conv.dir") + agent)  + "." + fn  + order.getWarehouse()+ ".txt";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sb.toString().getBytes());
        fos.close();

	} catch (Exception e) {
		e.printStackTrace();
	}
 }
  
 
 public double getPrice(String id, String hinnakiri, String klient,double stPrice) throws SQLException {

	  	
	    String  sql1 = 	    
		"SELECT SC18009 as disc, SC18008 AS price " +
	               "  FROM SC182100 INNER JOIN SL012100 ON  left(SC18002,7)= SL01056 LEFT JOIN SY240100 on SC18009=SY24002 and SY24001='BG' WHERE " + 
	                " SC18001='C' AND CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011 " +
					" and SL01001 = '" + klient + "' and SUBSTRING(SC18002,11,35)='" + id + "' ";
	    
	           System.out.println(sql1);
			    double acc = 0;
			    Statement st1 = con.createStatement();
			    ResultSet rs1 = st1.executeQuery(sql1);
			    
	    if (klient != null){
		    while (rs1.next()) {
		    	double dd = rs1.getDouble("disc");
			      acc = rs1.getDouble("price") *((100-dd)*0.01);
			      System.out.println("hind ja ale " + acc + " " + dd );
			    }	
		    
		    String sql0 = " SELECT SL01002 AS nimi,SL01001 AS kood, case when  SC45005 >0 then SC45005 else 0 end AS ale" +
	        "  FROM SL012100 kl INNER JOIN SC452100    ON SL01036=SC45003  " +
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

		  
		 	
		
	     
	    
	    
	    sql1 = "  SELECT  SC39004 AS price  FROM SC392100  WHERE  SC39002='" + hinnakiri+ "' and  SC39001 = '" + id + "'"; 	    
	    System.out.println("Teine kontroll " + sql1);
	 
	     st1 = con.createStatement();
	     rs1 = st1.executeQuery(sql1);
	if (hinnakiri != null && acc == 0){
	    while (rs1.next()) {
	      acc = rs1.getDouble("price") ;
	    }	
	}

	  rs1.close();
	  st1.close();
	
	    
	    
	    
			    System.out.println("Klient: " + acc + " " + id);
			  if(acc == 0){
				  sql1 = "  SELECT  SC39004 AS price  FROM SC392100  WHERE  SC39002='00' and  SC39001 = '" + id + "'";
			  	    
				    System.out.println(sql1);
				    

				   st1 = con.createStatement();
				   rs1 = st1.executeQuery(sql1);

				    while (rs1.next()) {
				      acc = rs1.getDouble("price") ;

				    }
				    
				    rs1.close();
				    st1.close();
			  }
			    

			    System.out.println("Klient: " + acc + " " + id);
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
    Calendar dat = order.getShipCal();

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
    	 System.out.println("TARNEP�EV SHIP0  " + result + " " + dat.get(Calendar.DATE));
    	  
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
	    Calendar dat = order.getShipCal();

	    String result = null ;
	    if (dat != null){
	    	if (dat.getTime().after(cal.getTime())){
	    		//dat.setTimeInMillis(dat.getTimeInMillis() - 86400000 ); siin tekkis jama 
	    	}
       
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
  private String url = "jdbc:odbc:scalaDB";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private ImportPanel parent;
  private int cou = 0;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
