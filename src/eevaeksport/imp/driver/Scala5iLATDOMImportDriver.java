/*
 * Created on 9.11.2007
 */
package eevaeksport.imp.driver;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */

	import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

	public class Scala5iLATDOMImportDriver
	    extends AbstractImportDriver {

	  static Category log = Category.getInstance(IceImportDriver.class.getName());

	  public Scala5iLATDOMImportDriver(ImportPanel parent) {
	    this.parent = parent;
	    PropertyConfigurator.configure("log4j.properties");
	   
	    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	    try {
	      Class.forName(databaseDriverName).newInstance();
	      con = DriverManager.getConnection(url,"pda","pda" );
	     // updateCustcodes();
	      calNF.setMaximumFractionDigits(0);
	      calNF.setMaximumIntegerDigits(2);
	      calNF.setMinimumIntegerDigits(2);
	      calNF.setGroupingUsed(false);
	      nf2.setMaximumFractionDigits(2);
	      nf.setMaximumFractionDigits(4);
	      nf.setGroupingUsed(false);
	      nf2.setGroupingUsed(false);
	    }
	    catch (Exception e) {
	      parent.showError(e);
	    }
	    
	    if(parent.getProps().getProperty("rate") != null){
	    	 try {
	    	    rate = Double.parseDouble(parent.getProps().getProperty("rate"));
	    	    } catch (Exception e){
	    	       parent.showError(e);
	    	    }
	    }
	  }
	  
	  private double  rate = 0.702804;

	  public String identifyCustomer(String id,String regnr) throws SQLException {
	  	String sql1 = null;
	  	
	  	 System.out.println("Kliendi M��ramine: " );
	  	
	  	    sql1 = "SELECT SL01001 AS kood FROM SL010200 where SL01001  =  '" + id + "'";	
		
	    System.out.println(sql1);
	    String acc = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      acc = rs1.getString("kood").trim() ;

	    }
	    rs1.close();
	    st1.close();

	    System.out.println("Klient: " + acc + " " + id);
	    return acc;

	  }

	
	  
	  public String identifyCustomerAGENT(String id) throws SQLException {
	 	 String	sql1 = "SELECT SL01035 as agent FROM SL010200 left JOIN SY240200  ON SL01036=SY24002 " +
	 	 		"and SY24001 ='BG' WHERE SL01001='" + id +"'"; 
	 	
	   System.out.println(sql1);
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
	 	 
	    String sql1 = " SELECT RTRIM(SC01150) AS pant FROM SC010200  WHERE RTRIM(SC01001)=   '" + toode.trim() + "'";
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
	  	
	    
	    String sql1 = " SELECT RTRIM(SC01001) AS toode FROM SC010200  WHERE RTRIM(SC01132)=   '" + ean.trim() + "'";

	    String sql2 = "SELECT RTRIM(SC01001) AS toode FROM SC010200  WHERE RTRIM(SC01001)=  '" + prodID.trim() + "'";

	    String prod = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      prod = rs1.getString("toode").trim();
	    }
	   System.out.println(ean + " prod0 " + prod);
	    if(prod == null){

	      rs1 = st1.executeQuery(sql2);

	      while (rs1.next()) {
	        prod = rs1.getString("toode").trim();
	      }
	    }

	    System.out.println(prodID + " prod1 " + prod);
	    rs1.close();
	    st1.close();

	    return prod;
	  }

	  
	  private String getTerm(int term){
	  	String res = "";
	  	 String sql1 = " SELECT SL22002 AS term FROM SL220200  WHERE SL22005=" + term + "";


	 			try{
	 				 Statement st1 = con.createStatement();
	 				    ResultSet rs1 = st1.executeQuery(sql1);
	 			
	 				      while (rs1.next()) {
	 				        res = rs1.getString("term");
	 				      }
	 			          rs1.close();
	 				      st1.close();
	 			
	 				      
	 			}catch (Exception e){
	 				
	 			}
	 System.out.println( "Makset�htaja koodileidmine " + res  + " " + term);
	 if(term == 0){
	 	return "";
	 }else{
		return res;	
	 }
	  }
	  
	  private double getRealDiscount(String toode,double price,String cust){
	  	double res = 0;
	  	// Baashinnakirja hinna leidmine 
	    String sql1 = " select case when SC39006 <= getdate() AND (SC39007 >= getdate() OR SC39007 = null) then " +
         " SC39005 else SC39004 end AS price  " +
         " FROM SC390200 WHERE  SC39002='00'   and SC39001 = '" + toode + "'";

	    double  pr = 0;
				try{
					 Statement st1 = con.createStatement();
					    ResultSet rs1 = st1.executeQuery(sql1);
				
					      while (rs1.next()) {
					        pr = rs1.getDouble("price");
					      }
					    
				}catch (Exception e){
					
				}
				
			
				// Personaalse hinnakirja  hinna leidmine 
			    String sql2 = " select case when SC39006 <= getdate() AND (SC39007 >= getdate() OR SC39007 = null) then " + 
          " SC39005 else SC39004 end AS price  ,SC39002,SL01129 " +
          " FROM SC390200 INNER JOIN SL010200 ON SL01029=SC39002 WHERE SC39001 == '" + toode + "'" + 
         " and  SL01129='" + cust + "'" ;

			    double  pro = 0;
						try{
							 Statement st2 = con.createStatement();
							    ResultSet rs2 = st2.executeQuery(sql2);
						
							      while (rs2.next()) {
							        pro = rs2.getDouble("price");
							      }
							    
						}catch (Exception e){
							
						}

	  
	if(pr != 0 && price != 0){
		res =  1- (price/pr);
	}
	
	if(pro != 0 && price != 0){
		res =  1- (price/pro);
	}
	
	System.out.println( "Ale arvutus " + pr + " prod00 " + "  " +   pro  + " prodoma " + price + " ale " + res*100);
	  	return res*100;
	  }

	  public void makeOrder(Order order) throws SQLException{
	    setOrder(order);
	    makeOutput(order);
	  }
	  public static boolean isNumeric(char ch)
	    {
	        return ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4' || ch == '.' || ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9';
	    }


	   private String removeLetters(String s){
		   String res = "";
		      for (int i = 0 ; i < s.length(); i++){
		        if (isNumeric(s.charAt(i))){
		          res = res + s.charAt(i) ;
		        }
		      }
		      return res;
		    }
	  
	 private void makeOutput(Order order){
	 	StringBuffer sb = new StringBuffer();

		 boolean duplicate = false;
	 	 
	 	 
	 	try {
	 	  int cou = 0;

	      
	 	    String cust = order.getCompany();
			String agent = this.identifyCustomerAGENT(cust);
			
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
	    
			
	         
	         
	         String type = "2";
	         
	    Vector lines = order.getOrderLines();
	      for (int i = 0; i < lines.size(); i++) {
	      	
	          
	      	OrderLine line = (OrderLine)lines.elementAt(i);
	          String toode = line.getToode();
	          String batch = line.getPartii();
	          double price = line.getPrice();
	          double kogus = line.getKogus();
	            double discount = getRealDiscount(toode,price,cust);
	 
	          if (i == 0 ){
	          	if( kogus < 0){
	          		System.out.println("Miinus arve kogus on  " + line.getKogus());
	             	doknr = removeLetters(doknr);
	              	type = "1";
	          	}
	          	if(cust.startsWith("INTR")){
	          //		doknr = getDoknr("OR06");
	          		duplicate = true;
	          	}
	         	
	          }
	          String paymterm = getTerm(order.getPaymTerm());
	          
	          
	             
	          double disc = line.getDiscount();
	          if(disc == 0){
	          	discount = 0;
	          }
	          
	          if(disc == 100){
	          	discount = 100;
	          }
	      
	         System.out.println("linedisc:" + disc + " arvutuslik disc:" + discount);
//	329900    22413                2  1 1  07092545      329900                              2  4230                        2       18.579     ;TAGASTUS;SAATELEHT NR. 20;
	        
		
	         
	      	 sb.append(addSpaces(doknr, 10));
	      	sb.append(type);//11
	        sb.append(addSpaces(cust, 10));//22
	        sb.append("          ");//32
	        sb.append("2");//33
	        sb.append(addSpaces(paymterm,2));//35
	        sb.append("1 ");//37
	        sb.append("1  ");//40
	        sb.append(getTarne(order));//46
	        sb.append(addSpaces(agent, 3));//49
	        sb.append("     ");//54
	        sb.append(addSpaces(doknr, 6));//60
	        sb.append("      ");////66
	        sb.append("      ");//72
	        sb.append(addSpaces("12", 6));//78 Warehouse Order
	        sb.append("      ");//84
	        sb.append("      ");//90
	        sb.append(addSpaces(String.valueOf(cou + 1), 3));//93
	        sb.append(addSpaces(toode, 20));//113
	        sb.append("        ");//121
	        sb.append(addSpaces(nf.format(line.getKogus()), 8));//129
	        sb.append(addSpaces(nf.format(discount).replace(',','.'), 6));//135
	        sb.append("      ");//141 
	        sb.append(addSpaces("12", 8));//146
	        sb.append(addSpaces(com,70));//181
	        sb.append(addSpaces(batch,70));//181
	        sb.append(addSpaces(nf2.format((price)).replace(',','.'), 20));	
	        sb.append(addSpaces(nf.format(disc).replace(',','.'), 6));
	        sb.append(System.getProperty("line.separator"));
	        
	       cou++;
	      }
	    	
	      setNumAddedLines(cou);
	  
          if(!duplicate){
          	 
            String fileName = escape(parent.getProps().getProperty("conv.dir") + agent)  + "." + doknr   +  ".txt";
   	     
   	      System.out.println(fileName);
   	        FileOutputStream fos = new FileOutputStream(fileName);
   	        fos.write(sb.toString().getBytes());
   	        fos.close();
          	
          }else {
          	String fileName2 = escape(parent.getProps().getProperty("conv.dir2") + agent)  + "." + doknr   +  ".txt";
          	FileOutputStream fos = new FileOutputStream(fileName2);
	        fos.write(sb.toString().getBytes());
	        fos.close();
          }
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
	  
	  public String getDriverVersion(){
	    return "Scala5iLATJUNImport";
	  }

	  private void setOrder(Order order){
	 
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
	    Calendar dat =  Calendar.getInstance();
	    
	    if(order.getShipCal()!=null){
	    	dat.setTime(order.getShipCal().getTime());
	    }

	    String result = null ;
	    	 String yy = (dat.get(Calendar.YEAR))+"";

	    	  String mm = (dat.get(Calendar.MONTH)+1)+"";
	    	  if(mm.length()== 1){
	    	  	mm = "0"+mm;
	    	  }
	    	  
	    	  String dd = (dat.get(Calendar.DAY_OF_MONTH))+"";
	    	  if(dd.length()== 1){
	    	  	dd = "0" + dd;
	    	  }
	    	  
	    	  result = yy.substring(2,4) + mm + dd;
	    	  System.out.println("P�EV SHIP " + result + " " + dat.toString());
    return result;
	  }
	  
	  
	  public static final String addSpaces(String s, int totalLen){
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
	  
	  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
	  
	  private NumberFormat calNF = NumberFormat.getInstance();


	  private String url = "jdbc:odbc:scalaDB";
	  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
	  private NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);
	  private Connection con;
	  private ImportPanel parent;

	public String getTrouble() {
		// TODO Auto-generated method stub
		return null;
	}
	}
