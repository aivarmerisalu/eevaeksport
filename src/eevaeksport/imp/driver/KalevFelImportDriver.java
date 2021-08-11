

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

public class KalevFelImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public KalevFelImportDriver(ImportPanel parent) {
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
  
  
  
  
  public String identfyWare(String prodID){

	    String sql1 = " Select SC01094 AS ladu FROM SC012100 pr  where pr.SC01001='" + prodID + "'";
	  log.info("1_LINE_SQL: " + sql1);

	    String ladu = "23";

	    Statement st1;
		try {
			st1 = con.createStatement();
		
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	//ladu = rs1.getString("ladu");
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
					e.printStackTrace();
				}
		     System.out.println("Felixi agendikood k�es  ;" + ring );
			    return ring;
			  }

  
  
  public String leiaFelixiKlkood(String id){
		

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

  
  
  

  public String identifyCustomer(String id,String regnr) throws SQLException {
	  System.out.println("id " +  id + " " + regnr);
	  
	  String	sql1 = "SELECT SL01001 as agent FROM SL012100 WHERE SL01001='" + id +"'"; 
	 	
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
	    String sql = "SELECT  nextval FROM mj_numbers WHERE serial='fel'";
	    log.info("getAndUpdateRecID(): " + sql);
	      Statement st = con.createStatement();
	      ResultSet rs = st.executeQuery(sql);
	      while(rs.next()){
	       
	        //System.out.println("number:" + rs.getString("NEXTREC"));
	        nextRecID = rs.getLong("nextval");
	        log.info("NEXTRECID = " + nextRecID);
	        sql = "UPDATE mj_numbers SET nextval =" + (nextRecID+1) +
	        " WHERE serial='fel' ";
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

  
  private double getMultiplier(String toode ){
  	double res = 1;
  	
    String sql1 = " SELECT (case when SC01015!='' then cast(SC01015 as integer) else 1 end) AS res , SC01097 as aaa FROM SC010800  WHERE RTRIM(SC01001)=   '" + toode + "'";

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
  

  public void makeOrder(Order order) throws SQLException{
	   System.out.println("inff" + order.getInfo());
	            makeOutput(order);	
	            cou = 0;
        //  addListings(order);
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
	  
		 String	 sql2 = "SELECT (SELECT SL01009 from SL012100 where SL01001 =k.SL01056) AS jama "
		 		+ " FROM SL012100 k WHERE (k.SL01001)=  '" + cust+ "'";
			
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
	        if(agent2.equals("FELIX")){
				agent2 = "85";
				agent= "85";
			}
			
			System.out.println("Tuvastatud agent :" + agent + " tellimusel " + agent2 + " number " + scala );
		
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
	         Vector lin = order.getOrderLines();
	         for (int i = 0; i < lin.size(); i++) {
	        	 OrderLine line = (OrderLine)lin.elementAt(i);
	        	 if(line.getDiscount() > 0){
	        		 hasdiscount = true;
	        	 }
	             
	         }
	         
	      	
	        StringBuffer sb2 = new StringBuffer(); 
	         
	         sb2.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + ls);
		      sb2.append("<msg:Msg xsi:schemaLocation=\"http://Epicor.com/Message/2.0 http://scshost/schemas/epicor/ScalaMessage.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:msg=\"http://Epicor.com/Message/2.0\">" + ls);			      
		      sb2.append("  <msg:Hdr>" + ls);
		      sb2.append("   <msg:Sender>" + ls);
		  	if(order.getStatus() < 10 && hasdiscount){
		  		 sb2.append("     <msg:Name>MobilejavaFelix</msg:Name>" + ls);
			     
		  	}else{
		  		 sb2.append("     <msg:Name>MobilejavaFelix</msg:Name>" + ls);
			     	
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
		          
		          
		          if(comment == null || comment.trim().length() == 0 ){
		         	comment = "";
		          }else{
		         	if(comment.trim().length()>70){
		         		comment = comment.substring(0,70);	
		         	}
		         	}
		          
		          String tarnekell =  order.getShipTime();
		          

		          if(order.getStatus() == 60 || order.getStatus() == 80){
		         	 tarnekell = order.getWarehouse();
		         	 order.setWarehouse(" ");
		          }
		          
//		          System.out.println("Kella aeg:" + tarnekell +  " " + order.getWarehouse());
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
		     	 
		      
		      
		      int xmlcounter = 0;
		      
		      
	    Vector lines = order.getOrderLines();
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
				
		
				
				System.out.println("ref " + ref);
		        
		          if(ref.trim().length()>70){
		        	  ref = ref.substring(0,70);	
		         	}
		          
		            Calendar cal = Calendar.getInstance();
		            Calendar dat = Calendar.getInstance();
		    	    dat.setTimeInMillis(order.getShipCal().getTimeInMillis());
		    	
		    	    if (dat != null  && dat.after(cal) && (order.getStatus() < 10 || order.getStatus() == 98 || order.getStatus() == 80) ){
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
		
		
		    	      sb2.append("      <dta:OrdType>1</dta:OrdType>" + ls);
				      sb2.append("      <dta:WhID>SFSM1</dta:WhID>" + ls);
				      sb2.append("      <dta:OrdStatus>" + ordsta + "</dta:OrdStatus>" + ls);
				  
		    	    	if(order.getStatus() >10){
		    	    	     sb2.append("      <dta:CustPONum>" + order.getDokNr() + "</dta:CustPONum>" + ls);
		    	    	}else{
		    	    		 sb2.append("      <dta:CustPONum>" + doknr + "</dta:CustPONum>" + ls);
		    	    	}
		       
		    	    	String custo = order.getCompany();
		    	    	
		    	    	if(logistika != null){
		    		    	 System.out.println("muudame kliendikoodi " + logistika);
		    		    	custo = logistika;
		    		    }
		    	    	
		    	//  sb2.append("      <dta:OrdNum>" + doknr + "</dta:OrdNum>" + ls);
			      sb2.append("      <dta:DelivDate>" + escape(getDateString(cal)) + "</dta:DelivDate>" + ls);
			      sb2.append("      <dta:OrdDate>" + escape(getDateString(Calendar.getInstance())) + "</dta:OrdDate>" + ls);
			      sb2.append("      <dta:CustCodeOrd>" + escape(custo) + "</dta:CustCodeOrd>" + ls);	
			      sb2.append("      <dta:YourRef>" + escape(getTarne(order)) + "</dta:YourRef>" + ls);	
				   
			      
			   //   sb2.append("      <dta:AccStr>" + ls);
			    //  sb2.append("          <dta:AccDim1>" + agent + "</dta:AccDim1>" + ls);
			    //  sb2.append("      </dta:AccStr>" + ls);
			      sb2.append("      <dta:SalesmanNum>" + agent + "</dta:SalesmanNum>" + ls);
			      sb2.append("      <dta:CustCodeDeliv>" + escape(custo) + "</dta:CustCodeDeliv>" + ls);
			    
			      String ref1 = comment.trim();
			      String ref2 = null;
			      
			    	     if(ref1.length() > 35){
			    	  ref2 = ref1.substring(35);
			    	  ref1 = ref1.substring(0,35);
			      }
			      
	              sb2.append("      <dta:Remark1>" + ref1 + "</dta:Remark1>" + ls);
	              if(ref2 != null){
	            	  sb2.append("      <dta:Remark2>" + ref2 + "</dta:Remark2>" + ls);	      	      
			      }
			      sb2.append("     </dta:OrderHeader>" + ls);
			      sb2.append("     <dta:OrderLines>" + ls);
	          	
	          }
	    	  
	    	  
	    	  
	    	  
	    	  
	    	  
	    	  
	    	  
	    	  
	    	  
	    	  
	        //  System.out.println("111");
	      	OrderLine line = (OrderLine)lines.elementAt(i);
	          String toode = line.getToode();
	         try{
	        	
	          if(toode != null && order.getStatus()>1){
	         	  //kontrollime kas kampaania tootel on piisav laosaldo ja kehtiv kuup�ev, et seda otse klientidele panna
	         	  String sql = "  SELECT  SC01010 as datum, RTRIM(SC03001) AS toode    " +
	         	      "  FROM SC030800 INNER JOIN SC010800 ON SC03001= SC01001 WHERE (RTRIM(SC03001) = '" + toode + "-EE' " +
	         	      		" or RTRIM(SC03001) = '" + toode + "-EE1' or RTRIM(SC03001) = '" + toode + "-EE2')" +
	         	      				" and SC01014 != '' AND SC03003-SC03004 > " + (line.getKogus() + parent.getTellimustel(toode) 
	         	      						+ parent.getTellimustel(toode+ "-EE") + parent.getTellimustel(toode+ "-EE1")
	         	      						+ parent.getTellimustel(toode+ "-EE2"));
	         	  Statement st1 = con.createStatement();//and SC01014 != '' 
	         	  ResultSet rs1 = st1.executeQuery(sql);
	         	 //System.out.println("222");
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
	         	// System.out.println(sql +  " " + toode + " " +parent.getTellimustel(toode) + " " + datum.compareTo(this.getTana()) + datum + " " + getTana()); 
	         	  rs1.close();
	         	  st1.close();
	         	 //System.out.println("333");
	           }
	          
	         }catch(Exception e){
	        	 e.printStackTrace();
	         }
	          
	          
	         try{
	         //see blokk on selleks et Esindajatelyt tulnud tellimusel kontrollida kas on kampaania tootel piisav kogus ja kui ei siis asendada kood p�hi koodiga 	
	             if(toode != null  && (toode.endsWith("-EE") || toode.endsWith("-EE1") || toode.endsWith("-EE2") )
	            		 && order.getStatus() == 0){
	            	  
	            	  String sql = "  SELECT  SC01010 as datum, RTRIM(SC03001) AS toode    " +
	            	      "  FROM SC030800 INNER JOIN SC010800 ON SC03001= SC01001 WHERE RTRIM(SC03001) = '" + toode + "' and SC01014 != ''" +
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
	        	      "  FROM SC010800  WHERE RTRIM(SC01001) = '" + toode + "' and (SC01014 != '' or SC01115 = 'L')";
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
	         	      "  FROM SC030800 INNER JOIN SC010800 ON SC03001= SC01001 WHERE (RTRIM(SC03001) = '" + toode + "-EE' " +
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
	        	  rs1.close();
	        	  st1.close();
	        		  
	        	  }
	            	  
	              }
	             
	            }catch(Exception e){
	           	 e.printStackTrace();
	            }
	         
	     
	          double kogus = line.getKogus();         
	    //      System.out.println("888");
	          double rounder = getMultiplier(toode);
	          if(order.getStatus() == 0){
	        	  rounder = 1;
	          }
	          
	     //     System.out.println( "Teine kontroll kui peatootel pole aktiivne pdas tunnus\n" 
	       //   +  " " + rounder + " " +notInt(kogus/rounder)  ); 
	      	
	           
	          
	             String unit = line.getUnit();
	             if(unit != null && unit.equals("DPA")){ 
	  	      	   System.out.println(unit + " " + toode + " enne " + kogus + " bonus " + line.getBonus() );
	  	      	   if(line.getBonus()> 1){
	  	      		   kogus = kogus * line.getBonus();
	  	      	//	   System.out.println(unit + " " + toode + " bonus " + kogus );
	  	      	   }else{
	  	      		 //  kogus = kogus * getInCases(itemid);
	  	      		
	  	      	   }  	   
	  	      	       
	  	                }
	            
	             
	             
	             if( !notInt(kogus) && notInt(kogus/rounder)){
	            	 // kui kogus on t�isarv ja m��dud kogus pole kastisuhtes t�isarv siis ymmardame ylesse
	           	  kogus = makeRoundUp(kogus,rounder);
	           //	 System.out.println( toode + " getinCases " + kogus );
	             
	             } 
	             
	             //System.out.println(unit + " " + toode + " getinCases " + kogus );
	             
	            nf.setMaximumFractionDigits(2);
	         //   System.out.println("1000");
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
	        
	    
	     
	     if(order.getWarehouse() == null){
	    	 order.setWarehouse("SFSM1");
	     }
	    
	     
	     
	     
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
	     sb.append(addSpaces("SFSM1",6));//472 ladu 
	     sb.append(addSpaces("",47));//525
	     sb.append(addSpaces(ring,2));//525
	     sb.append(getTarne(order));//532
	     sb.append(addSpaces("",306));//525

	if(toode.trim().length() > 0){
		xmlcounter ++;
	     sb2.append("      <dta:OrderLine>" + ls);
	     sb2.append("         <dta:LineNum>" + getLineNr(cou +1) + "</dta:LineNum>" + ls);
	     sb2.append("         <dta:StockCode>" + escape(toode) + "</dta:StockCode>" + ls);
	     sb2.append("         <dta:QtyOrdered>" + nf.format(kogus) + "</dta:QtyOrdered>" + ls);
	     sb2.append("         <dta:TaxCode>01</dta:TaxCode>" + ls);
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
	     		 
	     		  if(toode.trim().length() > 0){
	     		   sb2.append("         <dta:CustDisc>" + addSpaces(nf.format(ddd),6) + "</dta:CustDisc>" + ls);			           
	               sb2.append("         <dta:UnitPrice>" + nf.format(hind) + "</dta:UnitPrice>" + ls);
	     		 }
	     		  
	     		  
	     		  System.out.println("hind ja ale " + hind + " " + ddd);
	     		 addToDB(agent,cust,toode,kogus,ddd,"EST",kogus,true);
	     }else{
	        	sb.append(addSpaces("", 6)); // discount
	     }
	     

	   		 
	   	   
	  //   sb2.append("         <dta:AccStr>" + ls);
	    // sb2.append("               <dta:AccDim1>" + agent + "</dta:AccDim1>" + ls);
	    // sb2.append("         </dta:AccStr>" + ls);
	   
	     if(toode.trim().length() > 0){
	    	  sb2.append("         <dta:TextLines>" + ls);
	    	     sb2.append("               <dta:TextLine></dta:TextLine>" + ls);
	    	     sb2.append("         </dta:TextLines>" + ls);  	     
	    	     sb2.append("      </dta:OrderLine>" + ls);
	    	    	
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
	          sb.append(addSpaces("SFSM1",6));//472 ladu 
	          sb.append(addSpaces("",47));//525
	          sb.append(addSpaces(ring,2));//525
	          sb.append(addSpaces(getTarne(order),8));//532

	             sb.append(addSpaces(" ",300));//310
	             sb.append(System.getProperty("line.separator"));
	  
	      }
	     
	      sb2.append("        </dta:OrderLines>" + ls);
	      sb2.append("      <dta:TextLines />" + ls);
	      sb2.append("    </dta:SalesOrder>" + ls);
	      sb2.append("  </msg:Dta>" + ls);
	      
	      sb2.append("  <msg:ctx>" + ls);
	      sb2.append("     <Options>" + ls);
	      sb2.append("        <IgnoreRequestedPrice>0</IgnoreRequestedPrice>" + ls);
	   	  sb2.append("     </Options>" + ls);
	      sb2.append("  </msg:ctx>" + ls);

	      
	      sb2.append("</msg:Req>" + ls);
	      sb2.append("</msg:Body>" + ls);
	      sb2.append("</msg:Msg>" + ls);
	    
	      
	 
	      
	      setNumAddedLines(cou);
	      String fn = doknr;
	      if(doknr.trim().length()== 0){
	    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
	      }
	      System.out.println("On Discountiga tellimus  " + hasdiscount);

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
	    System.out.println(fileName);
	      FileOutputStream fos = new FileOutputStream(fileName);
	      fos.write(sb.toString().getBytes());
	      fos.close();
		
	}
		  

		} catch (Exception e) {
			e.printStackTrace();
		}
	 }

  
  
  
  
  
  
 private void makeOutputold(Order order){
 	StringBuffer sb = new StringBuffer();
 	 nf.setGroupingUsed(false);
 	 
 	try {
 		 String scala = getAndUpdateNext()+ "";
 	
      
 	    String cust = order.getCompany();
 		//String hinnakiri = getHinnakiri(cust);
		String agent = this.identifyCustomerAGENT(cust);
		String agent2 = order.getAgent();
        String ring = identfyRing(cust);
        if(agent2.equals("FELIX")){
			agent2 = "85";
			agent= "85";
		}
		
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
        
         boolean house = false;
         
         if(order.getStatus()< 50 && !house && !order.getAgent().equals(parent.getProps().getProperty("defaultagent"))){
        	 System.out.println("Agent v�eti tellimusest" + order.getAgent());
        	 agent = order.getAgent();
        	 if(agent.equals("FELIX")){
     			agent2 = "85";
     			agent= "85";
     		}
         }
         
         String status = getAgentSym(agent)+ order.getWarehouse()+ "-" + this.getTarneLyh(order)+ " " +
          ( Calendar.getInstance().getTimeInMillis() + "").substring(5);
         
      	String ls = System.getProperty("line.separator");
	
       boolean  hasdiscount = false;
         Vector lin = order.getOrderLines();
         for (int i = 0; i < lin.size(); i++) {
        	 OrderLine line = (OrderLine)lin.elementAt(i);
        	 if(line.getDiscount() > 0){
        		 hasdiscount = true;
        	 }
             
         }
         
      	
        StringBuffer sb2 = new StringBuffer(); 
         
         sb2.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + ls);
	      sb2.append("<msg:Msg xsi:schemaLocation=\"http://Epicor.com/Message/2.0 http://scshost/schemas/epicor/ScalaMessage.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:msg=\"http://Epicor.com/Message/2.0\">" + ls);			      
	      sb2.append("  <msg:Hdr>" + ls);
	      sb2.append("   <msg:Sender>" + ls);
	  	if(order.getStatus() < 10 && hasdiscount){
	  		 sb2.append("     <msg:Name>MobilejavaFelix</msg:Name>" + ls);
		     
	  	}else{
	  		 sb2.append("     <msg:Name>MobilejavaFelix</msg:Name>" + ls);
		     	
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
	          
	          
	          if(comment == null || comment.trim().length() == 0 ){
	         	comment = "";
	          }else{
	         	if(comment.trim().length()>70){
	         		comment = comment.substring(0,70);	
	         	}
	         	}
	          
	          String tarnekell =  order.getShipTime();
	          

	          if(order.getStatus() == 60 || order.getStatus() == 80){
	         	 tarnekell = order.getWarehouse();
	         	 order.setWarehouse(" ");
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
	     	 
	      
	      
	      int xmlcounter = 0;
	      
	      
    Vector lines = order.getOrderLines();
      for (int i = 0; i < lines.size(); i++) {
    	  
    	  
    	  // vaikimisi staatus on 1 
    	  //Kui on kommentaar siis staatus 0 
          // Kui tarne kuup ei ole t�na + 1 va reede kui on > 3 p�eva siis 0
    	  
          if (i == 0 ){
        	
          
	          if(comment == null || comment.trim().length() == 0 ){
	         	comment = "";
	          }
	          
	      	ref = order.getDokNr();
			if ( ref == null ){
				ref =  " ";
			}
			
	
			
			System.out.println("ref " + ref);
	        
	          if(ref.trim().length()>70){
	        	  ref = ref.substring(0,70);	
	         	}
	          
	            Calendar cal = Calendar.getInstance();
	            Calendar dat = Calendar.getInstance();
	    	    dat.setTimeInMillis(order.getShipCal().getTimeInMillis());
	    	    if(dat != null){
	    	    	System.out.println("Cal Shipdate "+ dat.getTime().toLocaleString());	
	    	    }

	    	
	    	    if (dat != null && (order.getStatus() < 10 || order.getStatus() == 98 || order.getStatus() == 80) ){
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
	
	
	    	      sb2.append("      <dta:OrdType>1</dta:OrdType>" + ls);
			      sb2.append("      <dta:WhID>SFSM1</dta:WhID>" + ls);
			      sb2.append("      <dta:OrdStatus>" + ordsta + "</dta:OrdStatus>" + ls);
			  
	    	    	if(order.getStatus() >10){
	    	    	     sb2.append("      <dta:CustPONum>" + order.getDokNr() + "</dta:CustPONum>" + ls);
	    	    	}else{
	    	    		 sb2.append("      <dta:CustPONum>" + doknr + "</dta:CustPONum>" + ls);
	    	    	}
	       
	    	    	String custo = order.getCompany();
	    	    	
	    	    	
	    	    	
	    	//  sb2.append("      <dta:OrdNum>" + doknr + "</dta:OrdNum>" + ls);
		      sb2.append("      <dta:DelivDate>" + escape(getDateString(cal)) + "</dta:DelivDate>" + ls);
		      sb2.append("      <dta:OrdDate>" + escape(getDateString(Calendar.getInstance())) + "</dta:OrdDate>" + ls);
		      sb2.append("      <dta:CustCodeOrd>" + escape(custo) + "</dta:CustCodeOrd>" + ls);	
		      sb2.append("      <dta:YourRef>" + escape(getTarne(order)) + "</dta:YourRef>" + ls);	
			   
		      
		   //   sb2.append("      <dta:AccStr>" + ls);
		    //  sb2.append("          <dta:AccDim1>" + agent + "</dta:AccDim1>" + ls);
		    //  sb2.append("      </dta:AccStr>" + ls);
		      sb2.append("      <dta:SalesmanNum>" + agent + "</dta:SalesmanNum>" + ls);
		      sb2.append("      <dta:CustCodeDeliv>" + escape(custo) + "</dta:CustCodeDeliv>" + ls);
		    
		      String ref1 = comment.trim();
		      String ref2 = null;
		      
		    	     if(ref1.length() > 35){
		    	  ref2 = ref1.substring(35);
		    	  ref1 = ref1.substring(0,35);
		      }
		      
              sb2.append("      <dta:Remark1>" + ref1 + "</dta:Remark1>" + ls);
              if(ref2 != null){
            	  sb2.append("      <dta:Remark2>" + ref2 + "</dta:Remark2>" + ls);	      	      
		      }
		      sb2.append("     </dta:OrderHeader>" + ls);
		      sb2.append("     <dta:OrderLines>" + ls);
          	
          }
    	  
          System.out.println("111");
      	OrderLine line = (OrderLine)lines.elementAt(i);
          String toode = line.getToode();
           double kogus = line.getKogus();         
          System.out.println("888");
          double rounder = getMultiplier(toode);
          if(order.getStatus() == 0){
        	  rounder = 1;
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
             
             

             if(ware.equals("SFSM1") && !notInt(kogus) && notInt(kogus/rounder)){
            	 // kui kogus on t�isarv ja m��dud kogus pole kastisuhtes t�isarv siis ymmardame ylesse
           	 // kogus = makeRoundUp(kogus,rounder);
             } 
            nf.setMaximumFractionDigits(2);
         

          String nimi = getNimi(toode);
     if(nimi == null){
    	 nimi = line.getTooteNimi();
     }
        
    
     if(order.getWarehouse() == null){
    	 order.setWarehouse("SFSM1");
     }
    
     
	    if(kogus < 100000){

if(toode.trim().length() > 0){
	xmlcounter ++;
     sb2.append("      <dta:OrderLine>" + ls);
     sb2.append("         <dta:LineNum>" + getLineNr(cou +1) + "</dta:LineNum>" + ls);
     sb2.append("         <dta:StockCode>" + escape(toode) + "</dta:StockCode>" + ls);
     sb2.append("         <dta:QtyOrdered>" + nf.format(kogus) + "</dta:QtyOrdered>" + ls);
     sb2.append("         <dta:TaxCode>01</dta:TaxCode>" + ls);
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
    		    disc = (1- hindArvu/hind)* 100;
     		 
     		  if(toode.trim().length() > 0){
     		   sb2.append("         <dta:CustDisc>" + addSpaces(nf.format(ddd),6) + "</dta:CustDisc>" + ls);			           
               sb2.append("         <dta:UnitPrice>" + nf.format(hind) + "</dta:UnitPrice>" + ls);
     		 }
     		  
     		  System.out.println("hind ja ale " + hind + " " + ddd);
     }     

   
     if(toode.trim().length() > 0){
    	  sb2.append("         <dta:TextLines>" + ls);
    	     sb2.append("               <dta:TextLine></dta:TextLine>" + ls);
    	     sb2.append("         </dta:TextLines>" + ls);  	     
    	     sb2.append("      </dta:OrderLine>" + ls);
    	    	
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
      
     
      sb2.append("        </dta:OrderLines>" + ls);
      sb2.append("      <dta:TextLines />" + ls);
      sb2.append("    </dta:SalesOrder>" + ls);
      sb2.append("  </msg:Dta>" + ls);
      
      sb2.append("  <msg:ctx>" + ls);
      sb2.append("     <Options>" + ls);
      sb2.append("        <IgnoreRequestedPrice>0</IgnoreRequestedPrice>" + ls);
   	  sb2.append("     </Options>" + ls);
      sb2.append("  </msg:ctx>" + ls);

      
      sb2.append("</msg:Req>" + ls);
      sb2.append("</msg:Body>" + ls);
      sb2.append("</msg:Msg>" + ls);
    
      
 
      
      setNumAddedLines(cou);
      String fn = doknr;
      if(doknr.trim().length()== 0){
    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
      }
      System.out.println("On Discountiga tellimus  " + hasdiscount);

      
      
      
if(xmlcounter >0){
	

	    try{
	    	String fileName3 = escape(parent.getProps().getProperty("conv.dir2")  + order.getAgent())
	    	+ "." + fn   +  ".xml";
	  	FileOutputStream fos = new FileOutputStream(fileName3);
	        fos.write(sb2.toString().getBytes());
	        fos.close();
	    	
	    }catch(Exception e){
	    	e.printStackTrace();
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
    System.out.println(fileName);
      FileOutputStream fos = new FileOutputStream(fileName);
      fos.write(sb.toString().getBytes());
      fos.close();
}

	} catch (Exception e) {
		e.printStackTrace();
	}
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
	               "  FROM SC182100 INNER JOIN SL012100 ON  left(SC18002,7)= SL01056 LEFT JOIN SY240800 on SC18009=SY24002 and SY24001='BG' WHERE " + 
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
 
 
  public String getDriverVersion(){
    return "KalevFelImport";
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
        Calendar dat = Calendar.getInstance();
	    dat.setTimeInMillis(order.getShipCal().getTimeInMillis());

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
