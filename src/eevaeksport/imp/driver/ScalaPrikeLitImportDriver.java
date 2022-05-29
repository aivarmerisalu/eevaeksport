/*
 * Created on 9.11.2007
 */
package eevaeksport.imp.driver;

import static org.junit.Assert.assertNotNull;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Email;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;

	/**
	 * Title:        Eeva eksport
	 * Description:
	 * Copyright:    Copyright (c) 2002
	 * Company:
	 * @author Tarmo Tubro
	 * @version 1.0
	 */

	public class ScalaPrikeLitImportDriver
	    extends AbstractImportDriver {

	  static Category log = Category.getInstance(IceImportDriver.class.getName());

	  public ScalaPrikeLitImportDriver(ImportPanel parent) {
	    this.parent = parent;
	    PropertyConfigurator.configure("log4j.properties");
	   
	    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	    try {
	      Class.forName(databaseDriverName).newInstance();
	      if(parent.getEksportFrame().getProps().getProperty("db.url") != null) {
		     	 url = parent.getEksportFrame().getProps().getProperty("db.url");//"jdbc:odbc:scalaDB";	
		       }
	    
	      
	     // updateCustcodes();
	      calNF.setMaximumFractionDigits(0);
	      calNF.setMaximumIntegerDigits(2);
	      calNF.setMinimumIntegerDigits(2);
	      calNF.setGroupingUsed(false);
	      nf2.setMaximumFractionDigits(1);
	      nf.setMaximumFractionDigits(4);
	      nf.setGroupingUsed(false);
	      nf2.setGroupingUsed(false);
	      
	    }
	    catch (Exception e) {
	      parent.showError(e);
	    }
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

	  
	  public String getEan(String prodID){
		  	

		    String sql1 = " Select SC01132 AS ean FROM SC010500 pr  where pr.SC01001='" + prodID + "'";
		  log.info("1_LINE_SQL: " + sql1);

		    String grupp = null;

		    Statement st1;
			try {
				 Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
					
				st1 = con.createStatement();
			
		    ResultSet rs1 = st1.executeQuery(sql1);

		    while (rs1.next()) {
		    	grupp = rs1.getString("ean");
		    }

		    rs1.close();
		    st1.close();
		    con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	//    System.out.println("EAN k�es 2 ;" + grupp );
		    return grupp;
		  }
	  
	  private HashSet zeros = null;
	  
	  public String identfyGroup(String prodID){
		  	


		    String sql1 = " Select SC01037 AS grupp FROM SC010500 pr  where pr.SC01001='" + prodID + "'";
		  log.info("1_LINE_SQL: " + sql1);

		    String grupp = "MUU";

		    Statement st1;
			try {
				 Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
					
				st1 = con.createStatement();
			
		    ResultSet rs1 = st1.executeQuery(sql1);

		    while (rs1.next()) {
		    	grupp = rs1.getString("grupp");
		    	if(grupp == null || grupp.trim().length() == 0){
		    		grupp = "MUU";
		    	}
		    }

		    rs1.close();
		    st1.close();
		    con.close();
		    
			} catch (SQLException e) {
				e.printStackTrace();
			}
	  //  System.out.println("Grupp k�es 2 ;" + grupp  + " " + prodID);
		    return grupp;
		  }
	  
	
	private void  addToDB(String agentid,String custid,String toode,double kogus,double disc,String country,double alekogus,boolean zall){
		
		String zallah = "";
		if(zall){
			kogus = alekogus;
			zallah = "zal";
			
		}
     
		    String sql1 = "   insert into discountlines" + zallah + " (agentid,custid,sentdate,prodid,amount,discount,country) values " +
	 "  ('" +agentid + "','" + custid + "',getdate(),'" + toode + "'," +kogus + "," + disc + ",'" + country + "') ";
		  
		    Statement st1;
			try {
				 Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
					
				st1 = con.createStatement();
				st1.executeUpdate(sql1);
				st1.close();
				con.close();
		}catch (Exception e){
			
		}
		
		
    }
	
	private HashSet zeroStatus() {
		HashSet res = new HashSet();
		
		try {
			
		
        String sql6 = "SELECT SL01001 as klient, SUM( case when SL03006<=getdate() then SL03013-SL03053 else 0 end ) AS debtover,\r\n" + 
        		"        		SUM(SL03013-SL03053) AS debt , max(datediff(dd, SL03006,getdate())) as days\r\n" + 
        		"        		 FROM SL030500 INNER JOIN SL010500 ON SL03001 = SL01001  WHERE \r\n" + 
        		"        NOT(SL03013=SL03053)  GROUP BY SL01001 having SUM(SL03013-SL03053) > 0  and   max(datediff(dd, SL03006,getdate())) >29";
        Connection  con2 = DriverManager.getConnection(url,"iscalaerp","Search2016" );
Statement stm = con2.createStatement();
ResultSet rs3 = stm.executeQuery(sql6);

while(rs3.next()){ 
	String klient = rs3.getString("klient");
    double debt = Double.parseDouble(rs3.getString("debt").trim());

    if (rs3.getInt("days") > 29 && debt > 50){
       res.add(klient.trim());
     }
}

rs3.close();
stm.close();
con2.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
System.out.println(res.toString());
return res;
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
		
	  
	  public String getMakse(String custid , String grupp){


				    String sql1 = " select Tingimus from maksed20500 m  inner join SL010500 k on k.SL01056=m.SL01001 where k.SL01001= '" + custid + "' and Grupp = '" + grupp + "'";
				  log.info("1_LINE_SQL: " + sql1);

				    String result = null;

				    Statement st1;
					try {
						 Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
							
						st1 = con.createStatement();
				    ResultSet rs1 = st1.executeQuery(sql1);
				    if (rs1.next()) {
				    	result = rs1.getString("Tingimus");
				    }
				    rs1.close();
				    st1.close();
				    con.close();
				    
					} catch (SQLException e) {
						e.printStackTrace();
					}
			   //     System.out.println("Makset�htaja kood "  + result + " " + custid + " " + grupp);
				    return result;

	  
	  
					
}
	
	  
	  private String getPayer(String custid){
			String payer = null;
		  try{
			String sqlp = "Select SL01056  as maksja from SL010500 where SL01001 = '" + custid + "'";
			 Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
				
			Statement st1 = con.createStatement();
		     ResultSet  rs1 = st1.executeQuery(sqlp);			
			if (rs1.next()) {
			     payer = rs1.getString("maksja");
			    // System.out.println("maksja " + payer);
			   
	          }else {
	        	  payer = "ALL";
	          }
			
		    rs1.close();
		    st1.close();
		    con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	  
		    return payer;
		  }
	  
	  
		  
	  private boolean getEttemaks(String custid){


		    String sql1 = " select (select SL01099 from SL010500 where k.SL01056=SL01001 )as ette from  SL010500 k  where k.SL01001= '" + custid + "' ";
		     log.info("1_LINE_SQL: " + sql1);

		    String result = null;

		    Statement st1;
			try {
				 Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
					
				st1 = con.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
		    if (rs1.next()) {
		    	result = rs1.getString("ette");
		    	if(result != null && result.toLowerCase().indexOf("ettem") != -1){
		    		return true;
		    	}
		    }
		    rs1.close();
		    st1.close();
		    con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	  
		    return false;
		  }

	  public String getHinnakiri(String custid ){


		    String sql1 = " select SL01029 as hinnakiri from SL010500   where SL01001= '" + custid + "' ";
		  log.info("1_LINE_SQL: " + sql1);

		    String result = null;

		    Statement st1;
			try {
				 Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
					
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
		    con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	  
		    return result;
		  }



	  
	  
	 

	
	  public String identifyCustomer(String id,String regnr) throws SQLException {
	 	    return id;
	  }

	  public double getStdPrice(String id) throws SQLException {

		  	
    String  sql1 = 	    
	" SELECT  SC39004 AS price  FROM SC010500 left outer JOIN SC030500 on SC03001=SC01001 and SC01002='01'  " +  //AND SC01003!=0
	" left outer JOIN SC390500 ON SC01001 = SC39001 and SC39002='00'  WHERE  " +
	" SC39004 > 0 and SC01001 = '" + id + "' ";
    
           System.out.println(sql1);
		    double acc = 0;
		    Connection cccd = DriverManager.getConnection(url,"iscalaerp","Search2016" );
		    Statement st1 = cccd.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
		  	    
			    System.out.println(sql1);

			    if (rs1.next()) {
			      acc = rs1.getDouble("price") ;
			    }
			    

		  
		    rs1.close();
		    st1.close();
		    cccd.close();

		    System.out.println("Hind: " + acc + " " + id);
		    return acc;

		  }

	  public double getPrice(String id, String hinnakiri, String klient,double stPrice) throws SQLException {

		  	
		    String  sql1 = 
		    "SELECT SCP6009 as disc, SCP6008 AS price \r\n" + 
			"		                 FROM SCP60500 INNER JOIN SL010500 ON  SCP6002= SL01056 LEFT JOIN SY240500 on SCP6009=SY24002 and SY24001='BG' WHERE \r\n" + 
			"		               getdate() BETWEEN  SCP6010 AND SCP6011  " +
						" and SL01001 = '" + klient + "' and SCP6002='" + id + "' ";
		    
		           System.out.println(sql1);
				    double acc = 0;
				    Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
					
				    Statement st1 = con.createStatement();
				    ResultSet rs1 = st1.executeQuery(sql1);
				    
		    if (klient != null){
			    while (rs1.next()) {
			    	double dd = rs1.getDouble("disc");
				      acc = rs1.getDouble("price") *((100-dd)*0.01);
				      System.out.println("hind ja ale " + acc + " " + dd );
				    }	
			    
			    String sql0 = " SELECT SL01002 AS nimi,SL01001 AS kood, case when  SC45005 >0 then SC45005 else 0 end AS ale" +
		        "  FROM SL010500 kl INNER JOIN SC450500    ON SL01036=SC45003  " +
		       "   WHERE SL01001 = (select SL01056 from SL010500 where SL01001 = '" + klient +"') ORDER BY SL01002";
		   st1 = con.createStatement();
		   rs1 = st1.executeQuery(sql0);
		   double ale = -1;
		   double alehind = 0;
		  if (rs1.next()) {
		  	ale = rs1.getDouble("ale") ;
		  	
		  }
		  if(ale != -1 ){
			  alehind = stPrice * (100d-ale)/100d;
			  if(acc == 0){
				  acc = alehind;
			  }else if(acc > alehind){
				  acc = alehind;
			   }
		      }	    
		     }

			  
			 	
			
		     
		    
		    
		    sql1 = "  SELECT  SC39004 AS price  FROM SC390500  WHERE  SC39002='" + hinnakiri+ "' and  SC39001 = '" + id + "'"; 	    
		    System.out.println("Teine kontroll " + sql1);
		 
		     st1 = con.createStatement();
		     rs1 = st1.executeQuery(sql1);
		if (hinnakiri != null && acc == 0){
		    while (rs1.next()) {
		      acc = rs1.getDouble("price") ;
		    }	
		}


		    
		    
		    
				    System.out.println("Klient: " + acc + " " + id);
				  if(acc == 0){
					  sql1 = "  SELECT  SC39004 AS price  FROM SC390500  WHERE  SC39002='00' and  SC39001 = '" + id + "'";
				  	    
					    System.out.println(sql1);
					    

					   st1 = con.createStatement();
					   rs1 = st1.executeQuery(sql1);

					    while (rs1.next()) {
					      acc = rs1.getDouble("price") ;

					    }
					    

				  }
				  	   		    rs1.close();
				    st1.close();
				    con.close();

				    System.out.println("Klient: " + acc + " " + id);
				    return acc;

				  }

	  
	  public String identifyCustomerAGENT(String id) throws SQLException {
	 	 String	sql1 = "SELECT SL01035 as agent FROM SL010500 left JOIN SY240500  ON SL01036=SY24002 " +
	 	 		"and SY24001 ='BG' WHERE SL01001='" + id +"'"; 
	 	
	   System.out.println(sql1);
	   String agent = null;
	   Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
		
	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   while (rs1.next()) {
	   	agent = rs1.getString("agent").trim();

	   }
	   rs1.close();
	   st1.close();
	   con.close();
	   
       if(agent.equals("MUU")){
    	//   agent = "   ";
       }
	   return agent;

	 }
	
	  
	  public double getCustDiscount(String id) throws SQLException {
		
		 	 
		     String sql1 = "SELECT SC45005 AS ale FROM SL010500 kl inner JOIN SC450500 ON SL01036=SC45003  " +
		       "   WHERE  SL01001='" + id +"'"; 
		   System.out.println(sql1);
		   double res = 0;
		   Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
			
		   Statement st1 = con.createStatement();
		   ResultSet rs1 = st1.executeQuery(sql1);

		   if (rs1.next()) {
		   	res = rs1.getDouble("ale");

		   }
		   rs1.close();
		   st1.close();
		   con.close();
	     
		   return res;

		 }
	  
	  public String getNimi(String id) throws SQLException {
	 	 String	sql1 = "SELECT SL01002 as agent FROM SL010500  WHERE SL01001='" + id +"'"; 
	 	
	   System.out.println(sql1);
	   String agent = null;
	   Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
		
	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   while (rs1.next()) {
	   	agent = rs1.getString("agent").trim();
       }
	 
	   rs1.close();
	   st1.close();
     con.close();
	   return agent;

	 }
	  
	  
	  
	  public String getPhone(String id) throws SQLException {
	 	 String	sql1 = "SELECT SL01011 as agent FROM SL010500  WHERE SL01001='" + id +"'"; 
	 	
	   System.out.println(sql1);
	   String agent = null;
	   Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
		
	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   while (rs1.next()) {
	   	agent = rs1.getString("agent").trim();
       }
	 
	   rs1.close();
	   st1.close();
     con.close();
	   return agent;

	 }
	  
	  public String getAddress(String id) throws SQLException {
	 	 String	sql1 = "SELECT (SL01003 + ' (' + SL01004 + ')')  as agent FROM SL010500  WHERE SL01001='" + id +"'"; 
	 	
	   System.out.println(sql1);
	   String agent = null;
	   Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
		
	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   while (rs1.next()) {
	   	agent = rs1.getString("agent").trim();

	   }
	   rs1.close();
	   st1.close();
	   con.close();
     
	   return agent;

	 }
	
	
	  
	  public String getDemaLadu(String toode, double kogus) throws SQLException{
		 	 
		    String sql1 = " select SC03002 as ladu from  SC030200  " + 
         "  WHERE RTRIM(SC03001)=   '" + toode.trim() + "' and  SC03003-SC03004 > " + kogus + " and SC03002 in ('01') AND SC03003!=0";
		    String ladu = "02";
		    Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
			
		    Statement st1 = con.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
		    if (rs1.next()) {
		    	ladu = rs1.getString("ladu").trim();
		    }
		       System.out.println(toode + " ladu1 " + ladu);
		    rs1.close();
		    st1.close();
		    con.close();
		    
		    return ladu;
		 }
	  
	 
	 	  
	  public String getPandiPakend(String toode) throws SQLException{
	 	 
	    String sql1 = " SELECT RTRIM(SC01150) AS pant FROM SC010500  WHERE RTRIM(SC01001)=   '" + toode.trim() + "'";
	    String prod = null;
	    Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
		
	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);
	    while (rs1.next()) {
	      prod = rs1.getString("pant").trim();
	    }
	       System.out.println(toode + " prod1 " + prod);
	    rs1.close();
	    st1.close();
	    con.close();
	    
	    return prod;
	 }
	  
	  public double getPandiKordaja(String toode) throws SQLException{
		 	 
		    String sql1 = " SELECT RTRIM(SC01019) AS pant FROM SC010500  WHERE RTRIM(SC01001)=   '" + toode.trim() + "'";
		    double prod = 1;
		    Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
			
		    Statement st1 = con.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
		    if (rs1.next()) {
		    	try{
		    		 prod = Double.parseDouble(rs1.getString("pant"));
		    	}catch(Exception e){
		    		//e.printStackTrace();
		    	}
		     
		    }
		       System.out.println(toode + " prod1 " + prod);
		    rs1.close();
		    st1.close();
		    con.close();
		    return prod;
		 }
		   
	  
	  public String identifyProduct(String ean, String prodID) throws SQLException{
	    return prodID;
	  }
			

	  public void makeOrder(Order order) throws SQLException{
		
		  if(zeros == null) {
			 zeros =  zeroStatus() ;
		  }
		  setOrder(order);
		    makeOutput(order);
	/*	  Vector splitted =  splitOrder(order);
      if("true".equals(parent.getProps().getProperty("testing"))){
    	  System.out.println("comp " + order.getCompany() + " " + sendKinnitus(order.getCompany()) );
    	  if(sendKinnitus(order.getCompany())){
	        	sendKinnitusEmail(order.getCompany(),getNimi(order.getCompany()),order.getOrderLines(), this.identifyCustomerAGENT(order.getCompany()) ,
		    			  order.getInfo(), getAddress(order.getCompany()));
	        }
	         System.exit(0);
          }
		  Enumeration en = splitted.elements();
		  while(en.hasMoreElements()){
			  Order ord = (Order)en.nextElement();
			  if(ord.getOrderLines().size() > 0){
				    setOrder(ord);
				    makeOutput(ord);
				}
		     }*/
	     }

	
	  
	  private String getDoknr(String aa) throws SQLException{
	  	String res = null;
	 	 String	sql1 = "select SY68002 as nr from SY6805XX where SY68001 = '" + aa + "'"; 
	 	 Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
			
				Statement st1 = con.createStatement();
				ResultSet rs1 = st1.executeQuery(sql1);
				
				if (rs1.next()) {
				res = rs1.getString("nr").trim();
				
				Statement ss = con.createStatement();
				String sql2 = "UPDATE SY6805XX set SY68002=SY68002+1 where SY68001 = '" +aa +"'";
				 ss.executeUpdate(sql2);
				 ss.close();
				
				}
				
				while(res.length() < 10){
					res = "0" + res;
				}
				
		rs1.close();
		st1.close();
		con.close();
	  	return res ;
	  }
	 
	  private void sendTagastusEmail(String custid, String nimi ,Vector lines ,String agent, String comment,  String aadress ){
	    	try{
	    		
	    		if(lines.size() > 0){
	    			
	    			String text = "";
	    			
	    			for (int i = 0; i < lines.size(); i++) {
	    		     	OrderLine line = (OrderLine)lines.elementAt(i);
	    		     	text = text + "<br><b><font size=\"5\"> [" + (int)(-1*line.getKogus()) + "] </b></font>  " + this.getProdNimi(line.getToode());
	    		    }
	    			
	    			Email mail = new Email(null);
	 	  		    mail.setSMTP(parent.getProps().getProperty("smtphost"));
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		mail.setType("text/html");
		  	  		mail.setSubjectFilesTo("Tagastuse soov  "  + custid + " " + nimi);
		  	  		String maksja = this.getPayer(custid);
		  	  		mail.setBody("<h><br><font size=\"6\">Return request</font></b>&nbsp;&nbsp salesperson : " + agent  + " <br><br><b><font size=\"5\">" + custid +  " " + nimi 
		  	  				+ "  " + aadress + " </font></b><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Tel : " + getPhone(custid)+"<br><br><hr>" + text
		  	  				+ " <br><br>Extrainfo : " + comment + "<br><br><br><br>Payer Customer :" + maksja + " " + this.getNimi(maksja) + "</h1>" );
    
		  	  		//"Maksja  nimi ja kood ka  kommentaar "
                    
		  	  		mail.setSendNotice(false);
		  
		  	  		String toemail = parent.getProps().getProperty("returnemail");
		  	  	StringTokenizer st = new StringTokenizer(toemail,";") ;
		  	   
		  	  	while (st.hasMoreTokens()){
		  	  	 mail.setFilesTo(st.nextToken());
		  	  	 mail.send();
		  	  	}
	  	  		
		  	  		
		  	  		System.out.println("\nReturn request " + agent  + custid +  " " + nimi + "\n\n" + text);
		  				
	    		}
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	    }  
	  
    private void sendCommentEmail(String comment,String doknr,String custid, String nimi){
    	try{
    		
    		if(comment != null && comment.trim().length() >0){
    			Email mail = new Email(null);
 	  		   
	  	  		mail.setSMTP(parent.getProps().getProperty("smtphost"));
	  	  		mail.setFilesTo(parent.getProps().getProperty("operatoremail"));
	  	  		mail.setFrom(parent.getProps().getProperty("from"));
	  	  		mail.setSubjectFilesTo("Order comment " + doknr + " " + custid);
	  	  		
	  	  		mail.setBody("\nOrder comment " +  doknr + " " + custid +  " " + nimi + "\n\n" +
	  	  				  "\n >>>>>" + comment + "<<<<<"
	  	  				);
	  	  		mail.setSendNotice(false);
	  	  		mail.send();
	  	  		System.out.println("\nOrder comment " +  doknr + " " + custid + "\n\n" +
	  	  				  "\n >>>>>" + comment + "<<<<<");
	  				
    		}
	  	
	  			}catch(Exception e){
	  			e.printStackTrace();	
	  			}
	  	
    }
	
 

    TreeMap asendused = null;
    
    
    private TreeMap getAsendused() {
 	    
 	    TreeMap res = new TreeMap();
 	        String cmd =" select SC01001 as toode, ActiveProdCode as asendus from PRODDATA0500	where ActiveProdCode != '' " ;
 		
 	     
 		      System.out.println(cmd);
 		   

 	      

 		try {
 			 Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
 			
 	          Statement st = con.createStatement();
 		    ResultSet	      rs = st.executeQuery(cmd);

 		    while (rs.next()) {
 			String kood = rs.getString("toode").trim();
 			String asendus = rs.getString("asendus").trim();
 			res.put(kood, asendus);
 			}
 		    rs.close();
 		    st.close();
 		    con.close();
 		    
 		}catch(Exception e) {
 			e.printStackTrace();
 		}
 	   return res ;
    }
    

 
	  private void makeOutput(Order order){
		  
		 	StringBuffer sb = new StringBuffer();
		 	Hashtable pandikas =  new Hashtable();
		 	String ls = System.getProperty("line.separator");
		    StringBuffer sb2 = new StringBuffer();
		    if(asendused == null) {
	        	asendused = getAsendused();
	        }
		   
		 	   
		 	try {
		 	  int cou = 0;
		
		 	 int ccc = 1;
		 	    String cust = order.getCompany();
				String agent = this.identifyCustomerAGENT(cust);
				double custdiscount = this.getCustDiscount(cust);
				if(order.getStatus() > 2 && agent.equals("MUU") 
						 ){
					agent = order.getAgent();
				}
				String hinnakiri = getHinnakiri(cust);
				
				double jae = 0;
		      	if(hinnakiri.equals("01")){
		      		jae = 1;
		      	}
				String com = order.getEmailBody();
				if ( com == null ){
					com =  " ";
				}		
				String ware = "01";
				
				if (order.getWarehouse() != null && order.getWarehouse().trim().length() > 2){
					ware = order.getWarehouse();
				}
			
				System.out.println("warehouse " + ware);
				  String reference = null; 
				if(order.getTeostaja() != null && order.getTeostaja().trim().length() > 0) {
					reference =  order.getTeostaja();
				}
				  
				 String doknr = "";

		    
				 if(order.getPaymTerm() == -2){
		        	  doknr = getDoknr("OR09");
		          }else if( 0 > ((OrderLine)order.getOrderLines().elementAt(0)).getKogus() ) {
		        	  doknr = getDoknr("OR02");
		          }else{
		        	  doknr = getDoknr("OR21");
		          }
				 
				 String shipmode = "0";
				 if(order.getShipMode() != null && order.getShipMode().trim().equals("1")){
					 shipmode = "1";
				 }
				 
				 boolean demagoogia  = false;
				
		         String mtaht = null;
		        String status = "1";
		         
		         String type = "1";
		        
		         String payer = order.getCompany();
		         if(payer.length() > 5) {
		        	 payer = payer.substring(1,5);
		         }
		      
		        if(zeros.contains(payer)) {
		        	status = "0";
		        }
		        System.out.println("payer " + payer + " " + status);
		         
		         
		         sb2.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + ls);
			      sb2.append("<msg:Msg xsi:schemaLocation=\"http://Epicor.com/Message/2.0 http://scshost/schemas/epicor/ScalaMessage.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:msg=\"http://Epicor.com/Message/2.0\">" + ls);			      
			      sb2.append("  <msg:Hdr>" + ls);
			      sb2.append("   <msg:Sender>" + ls);
			      sb2.append("     <msg:Name>MobilejavaLT</msg:Name>" + ls);
			      sb2.append("     <msg:Subname>ImportModule</msg:Subname>" + ls);
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
			    
			      String ref = "";
		         
	         
		         
		    Vector lines = order.getOrderLines();
		    boolean hasZallah = false;
		  	
		    for (int i = 0; i < lines.size(); i++) {
		     	OrderLine line = (OrderLine)lines.elementAt(i);
		     //	System.out.println("Line PL " +  line.getPl());
		     	if((line.getDiscount() > 0 && line.getPl() == 99) ){
		         // hasZallah = true; 		
		     	}
		    }
		    if(order.getStatus() == 60){
		    	 hasZallah = false;
		    }
		    int i = 0;
		    boolean tagastus = false;
		    
		   // boolean otsikoodi = otsiKoodi(order.getCompany());
		    
		    for (i = 0; i < lines.size(); i++) {
		      	OrderLine line = (OrderLine)lines.elementAt(i);
		          String toode = line.getToode();
		          if (asendused.get(toode) != null) {
		          	  toode = (String)asendused.get(toode);
		            }
		          
		          // otsime ean uuesti ja sama piires vaatan laosaldosid - ja valin vanima tootekoodi veini ja kanget eraldi ei k�sitleta
		          // SC01116 tooteloomise kuup�ev ja 
		        
		          
		          if (i == 0 ){
		        		try{  
		        			String abitoode = toode;
		        			if(abitoode == null){
		        				abitoode = ((OrderLine)lines.elementAt(lines.size()-1)).getToode();
		        			}
		        	  String grupp = identfyGroup(abitoode);
		       
		        	  mtaht = getMakse(order.getCompany(),grupp);
                       }catch(Exception e){
		            	e.printStackTrace();	
		            	}
		          	if( line.getKogus() < 0){
		     //     		System.out.println("kogus " + line.getKogus());
		              	type = "1";
		              	status = "0";
		              	tagastus = true;
		           	    doknr = getDoknr("OR22");
		           	    
				    
		          	}
		          	
		          	
		            String comment = order.getInfo();
		            
		       //     System.out.println("Kommentaari saatmine " + order.getStatus() + " " + comment);
		            if(order.getStatus() < 10 && !tagastus){
		            	try{
		                	sendCommentEmail(comment, doknr, order.getCompany(),getNimi(order.getCompany()));
		        		            		
		            	}catch(Exception e){
		            		e.printStackTrace();
		            	}
		           }
		            
		            
			     //     System.out.println("Payment " + order.getPaymTerm());
			          if(order.getPaymTerm() == -2){
			        	  comment = "Real";
			        	//  doknr = getDoknr("OR09");
			        	  mtaht = "91";
			          }
			          if(order.getPaymTerm() == -5){ //ettemaks
			        		type = "1";
			              	status = "0";
			          } 
		            try{
			          if(this.getEttemaks(order.getCompany())){
			        		type = "1";
			              	status = "0";
			          }
			          
		               }catch(Exception e){
		            	e.printStackTrace();	
		            	}
		          
			          if(comment == null || comment.trim().length() == 0 ){
			         	comment = "";
			          }
			          
			      	ref = order.getDokNr();
					if ( ref == null ){
						ref =  " ";
					}
					String asik = ref;
					if(order.isChanged()){
						ref =  "T" + ref ;
						
					}
			         
			
					if(comment.indexOf(asik) == -1 ){
				          ref = ref + " " + comment;	
					}else if(ref.trim().length() < 2 || order.getStatus() < 3){
						if(order.isChanged()){
							ref = "T " +comment;
						}else{
							ref = comment;
						}
						
					}
					
			        
			          if(ref.trim().length()>70){
			        	  ref = ref.substring(0,70);	
			         	}
			          
			            Calendar cal = Calendar.getInstance();
			    	    Calendar dat = order.getShipCal();
			    	 //   if(dat != null){
			    	   // 	System.out.println("Cal Shipdate "+ dat.getTime().toLocaleString());	
			    	    //}

			    	
			    	    if (dat != null && (order.getStatus() < 10 || order.getStatus() == 98 || order.getStatus() == 80) ){
			    	    	if(dat.get(Calendar.DAY_OF_YEAR)< Calendar.getInstance().get(Calendar.DAY_OF_YEAR)){
			    	    		dat.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
			    	    	}
			    	    	cal.setTimeInMillis(dat.getTimeInMillis());
			    	    }
			
			    	      sb2.append("      <dta:OrdType>1</dta:OrdType>" + ls);
					      sb2.append("      <dta:WhID>"+ware+"</dta:WhID>" + ls);
					      sb2.append("      <dta:OrdStatus>" + status + "</dta:OrdStatus>" + ls);
					  
			    	    	if(order.getStatus() >10){
			    	    	     sb2.append("      <dta:CustPONum>" +  Converter.escapeXML(order.getDokNr()) + "</dta:CustPONum>" + ls);
			    	    	}else{
			    	    		if(reference != null) {
			    	    			if(reference.length() > 20) {
			    	    				reference = reference.substring(0,20);
			    	    			}
			    	    			 sb2.append("      <dta:CustPONum>" +  Converter.escapeXML(reference) + "</dta:CustPONum>" + ls);
			    	    		}else {
			    	    			
			    	    			if(doknr.length() > 20) {
			    	    				doknr = doknr.substring(0,20);
			    	    			}
			    	    			 sb2.append("      <dta:CustPONum>" +  Converter.escapeXML(doknr) + "</dta:CustPONum>" + ls);
			    	    		}
			    	    				
			    	    		
			    	    	}
			    	  sb2.append("      <dta:OrdNum>" +  Converter.escapeXML(doknr) + "</dta:OrdNum>" + ls);
						System.out.println("datestuff " + order.isShipDateUsed() + " " + (order.getShipCal() != null));

			    	  
			    	 /* if(!isSameDay(cal,Calendar.getInstance())){
			    		   sb2.append("      <dta:DelivDate>" + escape(getDateString(cal)) + "</dta:DelivDate>" + ls);
			    		   sb2.append("      <dta:WayDeliv>0</dta:WayDeliv>" + ls);
					  }else{*/
						  if( order.isShipDateUsed() && order.getShipCal() != null ){
							   sb2.append("      <dta:DelivDate>" + escape(getDateString(order.getShipCal())) + "</dta:DelivDate>" + ls);
				    		   sb2.append("      <dta:WayDeliv>1</dta:WayDeliv>" + ls);
						  }else{
				    		   sb2.append("      <dta:WayDeliv>0</dta:WayDeliv>" + ls);  
						  }
					  //}
			    	  
			    	  sb2.append("      <dta:TermDeliv>" + shipmode + "</dta:TermDeliv>" + ls);
					  sb2.append("      <dta:OrdDate>" + escape(getDateString(Calendar.getInstance())) + "</dta:OrdDate>" + ls);
				      sb2.append("      <dta:CustCodeOrd>" + escape(order.getCompany()) + "</dta:CustCodeOrd>" + ls);	

						 String time = order.getShipTime();
					      if(time != null && time.trim().length() > 0){
					    	  sb2.append("      <dta:ShipMark>" + time.trim() + "</dta:ShipMark>" + ls);	
					      }

					     if(hasZallah){
				    	 // sb2.append("      <dta:OrdDisc>" + nf.format(custDisc) + "</dta:OrdDisc>" + ls);	
						}
				    
				      sb2.append("      <dta:AccStr>" + ls);
				      sb2.append("          <dta:AccDim1>" + agent + "</dta:AccDim1>" + ls);
				      sb2.append("      </dta:AccStr>" + ls);
				      sb2.append("      <dta:SalesmanNum>" + agent + "</dta:SalesmanNum>" + ls);
				      sb2.append("      <dta:CustCodeDeliv>" + escape(order.getCompany()) + "</dta:CustCodeDeliv>" + ls);
				      if(mtaht != null){
					      sb2.append("      <dta:TermPay>" + mtaht + "</dta:TermPay>" + ls);    	  
				      }		
				      
				      String ref1 = ref.trim();
				      String ref2 = null;
				     
				      
				      if(ware!= null && ware.trim().length() >0){
				    	  ref2 = escape(getDateString(cal)) + "T"+ ware;
						   
				      }
				    	     if(ref1.length() > 35){
				    	  ref2 = ref1.substring(35);
				    	  ref1 = ref1.substring(0,35);
				    	 
				      }
				      
		              sb2.append("      <dta:Remark1>" +  Converter.escapeXML(ref1) + "</dta:Remark1>" + ls);
		              if(ref2 != null){
		            	  sb2.append("      <dta:Remark2>" +  Converter.escapeXML(ref2) + "</dta:Remark2>" + ls);	      	      
				      }
				      sb2.append("     </dta:OrderHeader>" + ls);
				      sb2.append("     <dta:OrderLines>" + ls);
		          	
		          }
		          
		          
		          if(toode.trim().length() != 0){
		          if(toode.startsWith("XX")){
		        	  toode = toode.substring(2);
		        	  demagoogia = true;
		          }
		          String pant = getPandiPakend(toode);
		          double pandikordaja = getPandiKordaja(toode);
		          if(pant != null ){
		        	  
		        	  double aaa = line.getKogus()*pandikordaja;
		         	  if(pandikas.get(pant) == null){
		         		  pandikas.put(pant,aaa+"");
		               } else {
		               	pandikas.put(pant,(new Double((String)pandikas.get(pant)).doubleValue()+(aaa))+"");
		               }
		         	  
		         //	 System.out.println("Kordaja " + pandikordaja + "  " + pandikas);
		            }
		          
		       
		          
		        
		          
		            double disc = line.getDiscount();
		            if(order.getStatus() == 60){
		            	disc = 0;
		            }
		             int kogus = (int)line.getKogus();
		             
		             
		             		      String	ladu = "01";
		  
		      	if(demagoogia){
		      		agent ="REK";
		      		ladu = getDemaLadu(toode,kogus);
		      	}
		      	
		      	
		        String zallahtext = null;
		        double hind = 0;
		        double hindstd = 0;
		    	hindstd = getStdPrice(toode);
		        double hindArvu = 0;
		        double hindAle = 0;
		        
		        if(disc >0){
		        	//if((line.getDiscount() > 0 && line.getDiscount() < 3) || (line.getDiscount() > 0 && line.getPl() == 99)){
		        	if(false){	
		        	int ddd = (int)(line.getDiscount()*10);       	
		       			disc = ddd / (line.getKogus()) * 100;
		       		
		       		    hind = getPrice(toode,hinnakiri,null,-1);
		       		    hindAle = getPrice(toode,hinnakiri,cust,hind);
		       		    if(hindAle != 0 ){
		       		     hindArvu = hindAle * (100-disc)/100;
		       		    }else{
		       		     hindArvu = hind * (100-disc)/100;
		       		    }
		       		   
		       		 //   hindArvu = hindArvu *(100 - custDisc)/100; // kommentisin v�lja kuna pole vaja 
		       		    disc = (1- hindArvu/hind)* 100;
		        		zallahtext = "Product been sold " + (int)(line.getKogus()-ddd) + "+" + ddd + " discount";
		        		System.out.println("Zallah Disc " + (disc) + " kogus" + kogus + 
		        				" " + zallahtext + " hind arvu " + hindArvu
		        				 );
		        		// siia uus arvutus teha zallahi alusel 
		        		//sb.append(addSpaces(nf.format(disc), 6));
		        		disc = disc-jae;
		        		sb.append(addSpaces((disc)+ "", 6)); // discount	
		        		 addToDB(agent,cust,toode,kogus,disc,"LIT",kogus,true);
		        	}else{
		        		disc = disc-jae;
			           	sb.append(addSpaces(nf.format(disc), 6)); // discount	
		        	}
		        }else{
		           	sb.append(addSpaces("", 6)); // discount
		        }
		    
		        
		       
		       
		        if(disc > 2){
		        addToDB(agent,cust,toode,kogus,disc,"LIT",0,false);
		        }else{
		        }
		        
		        ccc++;
		        
		          sb2.append("      <dta:OrderLine>" + ls);
		          sb2.append("         <dta:LineNum>" + getLineNr(ccc) + "</dta:LineNum>" + ls);
		          sb2.append("         <dta:StockCode>" + escape(toode) + "</dta:StockCode>" + ls);
		          sb2.append("         <dta:QtyOrdered>" + nf.format(kogus) + "</dta:QtyOrdered>" + ls);
			      	if(hind > 0){
			         // sb2.append("         <dta:UnitPrice>" + nf.format(hind) + "</dta:UnitPrice>" + ls);
			      	}
			      
			    	   if(disc > 1 && !tagastus){
			    		   if(zallahtext != null){
			    			    sb2.append("         <dta:CustDisc>" + addSpaces(nf.format(disc),6) + "</dta:CustDisc>" + ls);
			    			    sb2.append("         <dta:UnitPrice>" + nf.format(hindstd) + "</dta:UnitPrice>" + ls);
			    		   }else{
			    			   if(custdiscount > 0){
					                sb2.append("         <dta:CustDisc>" + addSpaces(nf2.format(disc),6) + "</dta:CustDisc>" + ls);			           
					                sb2.append("         <dta:UnitPrice>" + nf.format(hindstd) + "</dta:UnitPrice>" + ls);
					             } else {
					            	 sb2.append("         <dta:CustDisc>" + addSpaces(nf2.format(disc),6) + "</dta:CustDisc>" + ls);			           
						             sb2.append("         <dta:UnitPrice>" + nf.format(hindstd) + "</dta:UnitPrice>" + ls);
					             }   
			    		     }
			    		 }
			    	   
		          sb2.append("         <dta:AccStr>" + ls);
			      sb2.append("               <dta:AccDim1>" + agent + "</dta:AccDim1>" + ls);
			      sb2.append("         </dta:AccStr>" + ls);
			      sb2.append("         <dta:TextLines>" + ls);
			      if(zallahtext != null){
			      sb2.append("               <dta:TextLine>" + zallahtext + "</dta:TextLine>" + ls);
			        }else{
			      sb2.append("               <dta:TextLine></dta:TextLine>" + ls);
			        }
			      sb2.append("         </dta:TextLines>" + ls);
			      
                  sb2.append("      </dta:OrderLine>" + ls);
		        
                  if(pant != null && pant.trim().length() > 0){
                	  
                	 ccc++;
                	     sb2.append("      <dta:OrderLine>" + ls);
				          sb2.append("         <dta:LineNum>" + getLineNr(ccc) + "</dta:LineNum>" + ls);
				          sb2.append("         <dta:StockCode>" + escape(pant) + "</dta:StockCode>" + ls);
				          sb2.append("         <dta:QtyOrdered>" + nf.format(kogus*pandikordaja).replace(',','.').replaceAll(" ","") + "</dta:QtyOrdered>" + ls);
				          sb2.append("         <dta:AccStr>" + ls);
					      sb2.append("               <dta:AccDim1>" + agent + "</dta:AccDim1>" + ls);
					      sb2.append("         </dta:AccStr>" + ls);
					      sb2.append("      </dta:OrderLine>" + ls);
					      
                  }
                  
		       cou++;
		      }
		 	}
              nf.setMaximumFractionDigits(3);
              nf.setGroupingUsed(false);
              

		      
		    //lisame pandipakendi  !!!!!!!
            
		          if(false && pandikas.size()>0){
		            	Enumeration enumpant = pandikas.keys();
		             while(enumpant.hasMoreElements()){
		             	
		             	String kood = (String)enumpant.nextElement();
		             	if(kood .trim().length()>0){
		             	i++;
		          	double pand = new Double((String)pandikas.get(kood)).doubleValue();
		          	  

				      	sb.append(addSpaces(doknr, 10));
				      	sb.append(type);//11
				        sb.append(addSpaces(cust, 10));//22
				        sb.append(getToday());//46 siia pane dokkuup
				        sb.append(getTarne(order));//46
				        sb.append(addSpaces(agent, 3));//49	      
				        sb.append(addSpaces(kood, 20));//113
				        sb.append(addSpaces(nf.format(pand).replace(',','.').replaceAll(" ",""), 8));//129
				        sb.append(addSpaces(" ",70));//181
				        sb.append("01");// Laokood 
				        sb.append(addSpaces("",200));//181
				           
		                 sb.append(System.getProperty("line.separator"));
		              
		                 
				          sb2.append("      <dta:OrderLine>" + ls);
				          sb2.append("         <dta:LineNum>" + getLineNr(i+1) + "</dta:LineNum>" + ls);
				          sb2.append("         <dta:StockCode>" + escape(kood) + "</dta:StockCode>" + ls);
				          sb2.append("         <dta:QtyOrdered>" + nf.format(pand).replace(',','.').replaceAll(" ","") + "</dta:QtyOrdered>" + ls);
				          sb2.append("         <dta:AccStr>" + ls);
					      sb2.append("               <dta:AccDim1>" + agent + "</dta:AccDim1>" + ls);
					      sb2.append("         </dta:AccStr>" + ls);
					      sb2.append("      </dta:OrderLine>" + ls);
		             
		         	
		             	}
		            	
		            }
		            }
		      
		      
		      
		      
		    	
		      setNumAddedLines(getNumAddedLines() + cou);
		      String add = agent;
		      if(add.equals("   ")){
		    	  add = "MUU";
		      }
	        
	          
	          sb2.append("        </dta:OrderLines>" + ls);
		      sb2.append("      <dta:TextLines />" + ls);
		      sb2.append("    </dta:SalesOrder>" + ls);
		      sb2.append("  </msg:Dta>" + ls);
		      sb2.append("</msg:Req>" + ls);
		      sb2.append("</msg:Body>" + ls);
		      sb2.append("</msg:Msg>" + ls);
		    
		      
		      if(true){//!tagastus
		    	  if(ccc > 1){
		    		
		    	  try{
				    	String fileName2 = escape(parent.getProps().getProperty("conv.dir3")  + order.getAgent())
				    	+ "." + doknr   +  ".xml";
			          	FileOutputStream fos = new FileOutputStream(fileName2);
				        fos.write(sb2.toString().getBytes());
				        fos.close();
			            	
				    }catch(Exception e){
				    	e.printStackTrace();
				    }
				  
				    try{
				    	String fileName3 = escape(parent.getProps().getProperty("conv.dir4")  + order.getAgent())
				    	+ "." + doknr   +  ".xml";
			          	FileOutputStream fos = new FileOutputStream(fileName3);
				        fos.write(sb2.toString().getBytes());
				        fos.close();
			            	
				    }catch(Exception e){
				    	e.printStackTrace();
				    }
				    
				 //   if(sendKinnitus(order.getCompany())){
			        //	sendKinnitusEmail(order.getCompany(),getNimi(order.getCompany()),lines, this.identifyCustomerAGENT(cust) ,
				    	//		  order.getInfo(), getAddress(order.getCompany()));
			       // }
				    
		    	  }
		      }else{
		    	  this.sendTagastusEmail( order.getCompany(),getNimi(order.getCompany()),lines, order.getAgent() ,
		    			  order.getInfo(), getAddress(order.getCompany()));
		      }
		      
		  
		  
	          
	          
			} catch (Exception e) {
				e.printStackTrace();
			}
		 }
		  

			  
	 public String getDriverVersion(){
	    return "ScalaPrikeLitImport";
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

	  

	  private String getToday(){
	    Calendar cal = Calendar.getInstance();
	    String result = calNF.format(cal.get(Calendar.YEAR)) +
	                    calNF.format(cal.get(Calendar.MONTH) + 1) +
	                  calNF.format(cal.get(Calendar.DAY_OF_MONTH));
	    return result;
	  }

	  private String getTarne(Order order){
	    Calendar cal = Calendar.getInstance();
	    Calendar dat = order.getShipCal();

	    String result = null ;
	    if (dat != null && order.getStatus() < 10){
	    	if(dat.get(Calendar.DAY_OF_YEAR)< Calendar.getInstance().get(Calendar.DAY_OF_YEAR)){
	    		dat.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
	    	}
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
	    	  
	    }else{
	    
	    	 String yy = (cal.get(Calendar.YEAR))+"";

	    	  String mm = (cal.get(Calendar.MONTH)+1)+"";
	    	  if(mm.length()== 1){
	    	  	mm = "0"+mm;
	    	  }
	    	  
	    	  
	    	  String dd = (cal.get(Calendar.DAY_OF_MONTH))+"";
	    	  if(dd.length()== 1){
	    	  	dd = "0" + dd;
	    	  }
	    	  
		  result = yy.substring(2,4) + mm + dd;
		  System.out.println("P�EV " + result + " " + dat.getTime().toString());
	    }
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
	  
	  
	  public String getProdNimi(String id) throws SQLException {
		
		  String sql1 = " Select ( RTRIM(SC01002) +rtrim(SC01003))  AS nimi FROM SC010500 pr  where pr.SC01001='" + id + "'";
		 	
		   System.out.println(sql1);
		   String agent = null;
		 Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
		   Statement st1 = con.createStatement();
		   ResultSet rs1 = st1.executeQuery(sql1);

		   while (rs1.next()) {
		   	agent = rs1.getString("nimi").trim();

		   }
		   rs1.close();
		   st1.close();
		   con.close();
	     
		   return agent;

		 }
	  
	  
	  
	  public String getCustNimi(String id) throws SQLException {
		
		  String sql1 = " Select ( RTRIM(SC01002) +rtrim(SC01003))  AS nimi FROM SC010500 pr  where pr.SC01001='" + id + "'";
		 	
		   System.out.println(sql1);
		   String agent = null;
		   Connection  con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
			
		   Statement st1 = con.createStatement();
		   ResultSet rs1 = st1.executeQuery(sql1);

		   while (rs1.next()) {
		   	agent = rs1.getString("nimi").trim();

		   }
		   rs1.close();
		   st1.close();
	     con.close();
		   return agent;

		 }

	  private String url = "jdbc:odbc:scalaDB";
	  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
	  private NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);
	  private TreeMap zalMap = new TreeMap();
	 
	  private ImportPanel parent;

	public String getTrouble() {
		// TODO Auto-generated method stub
		return null;
	}
	}
