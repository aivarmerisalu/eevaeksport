
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;




import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Email;
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

public class ScalaTobaImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public ScalaTobaImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url, "mobile","mobile");
      con2 = DriverManager.getConnection(url, "mobile","mobile");
      con3 = DriverManager.getConnection(url, "mobile","mobile");
      con4 = DriverManager.getConnection(url, "mobile","mobile");
      con5 = DriverManager.getConnection(url, "mobile","mobile");
     
      
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    }
    catch (Exception e) {
      parent.showError(e);
      System.exit(0);
    }
  }


  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	
  	 System.out.println("Kliendi M��ramine: " );
  	 
  	 if(id.length() > 10){
  		 id = id.substring(id.length()-10);
  	 }
  	
  	    sql1 = "SELECT SL01001 AS kood FROM SL010100 where SL01106  =  '" + id + "'";	
	
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

  public String identifyCustomerADDR(String id) throws SQLException {
  	 String	sql1 = "SELECT N33_ADRESAS AS aadress FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
  	 		"  trim(N08_KODAS_KS)||'-'|| N33_EIL_NR = '" + id +"'"; 
  	
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("aadress").trim();

    }
    rs1.close();
    st1.close();

    return acc;

  }
  
  public double getSacalOrdersSum(String custid) throws SQLException {
 	 String	sql1 = "SELECT  sum(OR03008*OR03011*1.2) AS summa  FROM  OR030100	INNER JOIN OR010100 on OR01001=OR03001 " +
			" WHERE    OR01003 =  '" + custid +"' GROUP BY OR01003"; 
 	
   System.out.println(sql1);
   double  sum  = 0;

   Statement st1 = con4.createStatement();
   ResultSet rs1 = st1.executeQuery(sql1);

   while (rs1.next()) {
   	sum = rs1.getDouble("summa");

   }
   rs1.close();
   st1.close();

   return sum;

 }
  public String identifyCustomerAGENT(String id) throws SQLException {
 	 String	sql1 = "SELECT SL01035 as agent FROM SL010100 left JOIN SY240100  ON SL01036=SY24002 " +
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
 
  private boolean hasCreditProblem(String custid){
	  
	
try{
	 // 1. leiame kliendi makst�htaja koodi 
	  String sql1 = " SELECT RTRIM(SL01024) AS paym FROM SL010100   WHERE RTRIM(SL01001)= '" + custid.trim() + "'";
	    String paym = null;
	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);
	    while (rs1.next()) {
	      paym = rs1.getString("paym").trim();
	    }
	       System.out.println(custid + " paym " + paym);
	    rs1.close();
	    st1.close();
	    // 0,1,2,3,4,5,6,99
	    if(paym != null && 
	    		(paym.equals("0")||
	    				paym.equals("1")||
	    				paym.equals("2")||
	    				paym.equals("3")||
	    				paym.equals("4")||
	    				paym.equals("5")||
	    				paym.equals("6")||
	    				paym.equals("99")
	    		)){
	    	return false ;
	    }
	    
	    //2. arvutame kas on yle krediidi limiidi
	    //a. leiame maksja 
	    
	    String cmd3 = "SELECT SL01056 as pea ,SL01002 as nimi, SL01012 as credit  FROM SL010100 left JOIN SC450100   ON SL01036=SC45003 " +
        "  WHERE SL01002 != '' AND SL01035 != ''  and RTRIM(SL01001)='" + custid.trim() + "' ORDER BY SL01002";
		     st1 = con.createStatement();
		     rs1 = st1.executeQuery(cmd3);
		  String pea = null;
		  String kliendinimi = null;
		  double limiit = -1;
	     while (rs1.next()) {
	       pea = rs1.getString("pea");
	       if(pea != null && pea.trim().length() == 0){
	    	   pea = custid.trim();
	       }else{
	    	   pea = pea.trim();
	       }
	       kliendinimi = rs1.getString("nimi").trim();
	       try{
	    	    limiit = Double.parseDouble(rs1.getString("credit").trim());   
	       }catch(Exception e){
	    	  // e.printStackTrace();   
	       }
	   
	      }
	     
	         System.out.println(custid + " pea " + pea + " " + limiit + " " + kliendinimi
	        		 );
	    rs1.close();
	    st1.close();
	    
	    
	    if(pea != null){
	    	//b . arvutame rp j��gi
	    	
	    	  String cmd4 =" SELECT sum(SL03014-SL03054) AS debt " +
	    	   " FROM SL030100 INNER JOIN SL010100 ON SL03001 = SL01001  WHERE  " +
	              "    NOT(SL03014=SL03054)  and SL01001='" + pea + "'  GROUP BY SL01001 ";
	  		     st1 = con.createStatement();
	  		     rs1 = st1.executeQuery(cmd4);
	  		  double volg = 0;
	  		  double scalaSum = 0;
	  		  try{
	  			scalaSum =  getSacalOrdersSum(pea);
	  		  }catch (Exception s){
	  			  
	  		  }
	  	
	  		  
	  	     while (rs1.next()) {
	  	    	volg  = volg + (rs1.getDouble("debt"));
	  	      }
	  	     
	  	  
	  	   double ylelimiidi = limiit - (volg + + currOrderSum + scalaSum);
	  	         System.out.println(custid + " volg  " + volg + " "+scalaSum + " "+ currOrderSum);
	  	        
	  	    rs1.close();
	  	    st1.close();
	  	  if(ylelimiidi < 0){
	        	 saadaKrediidiJama(custid , ylelimiidi ,limiit, kliendinimi,scalaSum ,volg);	
	        	 return true;
	         }
	  	  
	  	 
	  	  
	  	
	    }
	    
	    
	    
}catch(Exception e){
  e.printStackTrace();	
}
	
	  
	  
	  return false ;
  }
  
  private StringBuffer buf = new StringBuffer();
  
  private void  saadaKrediidiJama(String custid , double ylelimiidi ,double limiit,String kliendinimi,double scalaSum , double volg){
		  String address = parent.getProps().getProperty("creditemail");
		  String subjTX = parent.getProps().getProperty("subjtx");
		
		   buf = new StringBuffer();
		  buf.append("Kliendil " + custid + " " + kliendinimi + "\n\n Limiidi �letamine !! " +
		  		      "\n Limiit:              " + (int)limiit +
				  " EUR\n V�lg  :              " + ((int)volg) + 
				  " EUR\n Tellimustel :        " + (int)scalaSum +
				  " EUR\n Antud tellimustel :  " + (int)this.currOrderSum +
				  " EUR\n �LETAMINE :          " + (int)ylelimiidi
				  +" EUR\n\n Tellimus on karantiini kataloogis - " + getKarantiinFile() 
				  + "\n\n Tellimuse uueks impordiks peab ta t�stma samm tagasi 'orders' - kataloogi \n\nKarantiinist kustutatakse tellimus j�rgmise t��p�eva alguseks ");
		  if(address != null ){
			  Email email = new Email("Krediidi limiidi �letamine");	        
	          email.setFrom(parent.getProps().getProperty("from"));
	          email.setSMTP(parent.getProps().getProperty("smtphost"));
	          email.setType("text/html");
	       
	          String sub = "Krediidi limiidi �letamine";
	            if(subjTX != null){
	            	  sub = subjTX;
	              }
	            
	           String subj = sub + " " + kliendinimi + " File:  \"" + getKarantiinFile() + parent.currFile.getName() + "\"";
	          System.out.println(subj);
	          email.setSubjectFilesTo(subj);
	         
	          try {
	        	  
	        		String subject = email.getSubjectFilesTo();
	        		StringBuffer sb = new StringBuffer();
	        		sb.append("<HTML>\n");
	        		sb.append("<HEAD>\n");
	        		sb.append("<TITLE>\n");
	        		sb.append(subject + "\n");
	        		sb.append("</TITLE>\n");
	        		sb.append("</HEAD>\n");

	        		sb.append("<BODY>\n");
	        		sb.append("<H1>File:  <A href=\"" +   getKarantiinFile() + parent.currFile.getName() + "\">" +   getKarantiinFile() + parent.currFile.getName() +"</A>" + "</H1><BR>");
	        		//<A href="../images/forest.gif">map of the enchanted forest.</A>
	        		//<A href=" + filename + ">" + filename +"</A>
	        
	        		    sb.append(buf.toString().replaceAll("\n", "<BR>"));
	        		    sb.append("<BR>");

	        		sb.append("</BODY>\n");
	        		sb.append("</HTML>\n")    ;    	  
	        	  
	        	email.setBody( sb.toString());

	            StringTokenizer st =  new StringTokenizer(address,";");
	            while(st.hasMoreTokens()){
	            	String aadr = new InternetAddress(st.nextToken()).toString();
	              email.setFilesTo(aadr);
	              System.out.println("Setting address " + aadr);
	              email.send();
	              }
			} catch (AddressException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  
			  
		  }

  }
  
  public String getPandiPakend(String toode) throws SQLException{
 	 
    String sql1 = " SELECT RTRIM(SC01150) AS pant FROM SC010100  WHERE RTRIM(SC01001)=   '" + toode.trim() + "'";
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
 
  
  public String getMTKood(int paevad) throws SQLException{
 	 
    String sql1 = " select SL22002 as kood from SL220100 where SL22003 =0 and SL22005 =  " + paevad + "";
    String kood = null;
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      kood = rs1.getString("kood").trim();
    }
       System.out.println(paevad + " mtkood " + kood);
    rs1.close();
    st1.close();
    return kood;
 }
 
  
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	
    
    String sql2 = " SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE (RTRIM(SC01132)=   '" + ean.trim() 
                              + "' or RTRIM(SC01003) = '" + ean.trim() + "' or RTRIM(SC01060) = '" + ean.trim() + "') and SC01015 != ''";

    
    String sql1 ="";
        
        Statement st1 = con.createStatement();
        ResultSet rs1  = null;
        
    String prod = null;
    if(prodID != null){
    	sql1 = "SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01001)=  '" + prodID.trim() + "' "; //or  RTRIM(SC01001)= left ('" + prodID.trim() + "',4)";	

     rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }	
      }
   System.out.println(ean + " prod0 " + prod);
    if(prod == null || prod.trim().length() == 0){

      rs1 = st1.executeQuery(sql2);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
      }
    }

    if(prod == null && ean.trim().length() > 6){
  	  while(ean != null && ean.startsWith("0")){
	    	ean = ean.substring(1);
	    	 rs1 = st1.executeQuery( " SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE (RTRIM(SC01132)=   '" + ean.trim() 
	                  + "' or RTRIM(SC01003) = '" + ean.trim() + "' or RTRIM(SC01060) = '" + ean.trim() + "') and SC01015 != ''" );
		      if (rs1.next()) {
		        prod = rs1.getString("toode").trim();
		      }
		        
  	  }
	     
	    }
    
    System.out.println(prodID + " prod1 " + prod);
    try{
    	  rs1.close();
    	    st1.close();	
    }catch(Exception rr){
    rr.printStackTrace();	
    }
  

    return prod;
  }


  
  private double getMultiplier(String toode){
  	double res = 1;
  	
    String sql1 = " SELECT (case when SC01014!='' then cast(SC01014 as integer) else 1 end) AS res FROM SC010100  WHERE RTRIM(SC01001)=   '" + toode + "'";

			try{
				 Statement st1 = con.createStatement();
				    ResultSet rs1 = st1.executeQuery(sql1);
			
				      while (rs1.next()) {
				        res = rs1.getDouble("res");
				      }
				    
			}catch (Exception e){
				
			}

  	return res;
  }

  
  public TreeMap  getGroups(String custid){

			    String sql1 = " select Tingimus,Grupp from maksed20100 where Klient = '" + custid + "'";
			  log.info("1_LINE_SQL: " + sql1);

			    TreeMap mitu  = new TreeMap();

			    Statement st1;
				try {
					st1 = con.createStatement();
			    ResultSet rs1 = st1.executeQuery(sql1);
			    while (rs1.next()) {
			    	String ting = rs1.getString("Tingimus");
			    	String gr = rs1.getString("Grupp");
			    	if(mitu.get(ting) == null){
			    		Vector vv = new Vector();
			    		vv.addElement(gr);
			    		mitu.put(ting, vv);
			    	}else{
			    		Vector vv = (Vector) mitu.get(ting);
			    		vv.addElement(gr);
			    		mitu.put(ting, vv);
			    	}
			    	
			    }
			    rs1.close();
			    st1.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		  
			    return mitu;
			  }

  
  
  public int getMituOn(String custid){
		//M14544    M14544    M14544    MAXJ10701 45283067952010010720100108                                                                                                                                                                                                                  54                                                                       34013                              Linda kirsitasku 3x55g                            6           1            5500003033J1                                                     20100108                                                                                                                                                                                                                                                                                                            


			    String sql1 = " select (Tingimus) from maksed20100 where Klient =  '" + custid + "' group by Tingimus";
			  log.info("1_LINE_SQL: " + sql1);

			    int mitu  = 0;

			    Statement st1;
				try {
					st1 = con.createStatement();
			    ResultSet rs1 = st1.executeQuery(sql1);
			    while (rs1.next()) {
			    	mitu = mitu + 1;
			    }
			    rs1.close();
			    st1.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		  
			    return mitu;
			  }

  
  public boolean identfyMakse(String custid ,String prodID, Vector groups){
	//M14544    M14544    M14544    MAXJ10701 45283067952010010720100108                                                                                                                                                                                                                  54                                                                       34013                              Linda kirsitasku 3x55g                            6           1            5500003033J1                                                     20100108                                                                                                                                                                                                                                                                                                            


		    String sql1 = "select SC01001 ,Tingimus,Grupp from maksed20100 inner join SC010100  on SC01037 = Grupp " +
		    		"where Klient = '" + custid + "' and SC01001 = '" + prodID + "'";
		 

		  
		  
		    boolean makse  = false;

		    Statement st1;
			try {
				st1 = con.createStatement();
			
		    ResultSet rs1 = st1.executeQuery(sql1);
   boolean normal = true;
		    while (rs1.next()) {
		    	   normal = false;
		     String grupp = rs1.getString("grupp");
		     if(groups != null && groups.contains(grupp)){
		    	 return true;
		     }
		  	     
		    }
		   
		    System.out.println("1_LINE_SQL: " + sql1 + "\n normal" + normal);
		    
     if(normal && groups == null ){
    	 return true;
     }
		    rs1.close();
		    st1.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		    return makse;
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

 private double currOrderSum = 0;
  
  private Vector splitOrder (Order order){
	  currOrderSum = 0;
	  Vector result = new Vector();
      //1. Mitu maksetingimust yldse kliendil ON ??
	  int mituMT = getMituOn(order.getCompany());  
	  if(mituMT == 0){
		  order.setCompName("");
		  result.add(order);
		  Vector lines = order.getOrderLines();
		  Enumeration enumr = lines.elements();
		  String hinnak =  getHinnakiri(order.getCompany());		  
		  while  (enumr.hasMoreElements()){
			  OrderLine lin = (OrderLine)enumr.nextElement();
			     String itemid = lin.getToode();
			     try {
			    	 double hind = getPrice(itemid,hinnak,null,-1);
			    	 double  hindAle = getPrice(itemid,hinnak,order.getCompany(),hind);
			    	
					currOrderSum = currOrderSum + (hindAle * lin.getKogus());
					System.out.println("currOrderSum " + currOrderSum);
				} catch (SQLException e) {
					e.printStackTrace();
				}   
		  }			 
		  
		  return result; // kui pole siis tagasi
	  }
	  
	  
 // Vaatame kas on ilma pandud MT ridu tellimusel 	  
	  
	  Order normal = new Order(order.getCompany(),order.getAgentFirstName());
	  normal.setAgentCode(order.getAgentCode());
	  normal.setDokNr(order.getDokNr());
	  normal.setInfo(order.getInfo());
	  normal.setChanged(order.isChanged());
	  normal.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  normal.setStatus(order.getStatus());
	  normal.setWarehouse(order.getWarehouse());
	  normal.setReference(order.getReference());
	  normal.setTeostaja(order.getTeostaja());
	  
	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	  String hinnak =  getHinnakiri(order.getCompany());
	  
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
		     String itemid = lin.getToode();
		     try {
		    	 double hind = getPrice(itemid,hinnak,null,-1);
		    	 double  hindAle = getPrice(itemid,hinnak,order.getCompany(),hind);
		    	
				currOrderSum = currOrderSum + (hindAle * lin.getKogus());
				System.out.println("currOrderSum " + currOrderSum);
			} catch (SQLException e) {
				
				e.printStackTrace();
			} 
		    boolean laheb =  identfyMakse(order.getCompany(),itemid,null);
		    System.out.println("xxx " + laheb  + " " + itemid);
		     if(laheb){  
		    	 normal.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		     	System.out.println("normal " + lin.getToode());
		     }
	  }

	  if(normal.getOrderLines() != null && normal.getOrderLines().size()>0){
		  result.add(normal);  
	  }

	  //   Siin l�peb Normaalsega tegelemine 

	  
	  
	  //teeme Tree mapi kus on sees MT ja vektor grupidega mis on selle MT juures 
	  TreeMap mtgroups = getGroups(order.getCompany());
	  
	  Iterator iter = mtgroups.keySet().iterator();
	  
	  while(iter.hasNext()){
		  String mtaht = (String)iter.next();
		  Vector groups = (Vector)mtgroups.get(mtaht);
		  Order teine = new Order(order.getCompany(),order.getAgentFirstName());
		  teine.setAgentCode(order.getAgentCode());
		  teine.setReference(order.getReference());
		  teine.setCompName(mtaht); //kasuame Shipmode et edastada erinevaid MT koode 
		  teine.setDokNr(order.getDokNr());
		  teine.setInfo(order.getInfo());
		  teine.setChanged(order.isChanged());
		  teine.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  teine.setStatus(order.getStatus());
		  teine.setTeostaja(order.getTeostaja());
		  teine.setWarehouse(order.getWarehouse());
		  lines = order.getOrderLines();
		  enumr = lines.elements();
		  while  (enumr.hasMoreElements()){
			  OrderLine lin = (OrderLine)enumr.nextElement();
			     String itemid = lin.getToode();
			    boolean laheb =  identfyMakse(order.getCompany(),itemid,groups);
			    System.out.println("yyy " + laheb  + " " + itemid + " " + groups.toString());
			     if(laheb){  
			    	 teine.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     	//System.out.println("normal " + lin.getToode());
			     }
		  }		
		  
		  if(teine.getOrderLines() != null && teine.getOrderLines().size()>0){
			  System.out.println("mt " + teine.getCompName());
			  result.add(teine);  
		  }
	  }

	 
	 
	 System.out.println("Result =" +order.getDokNr()
			 + " "+ result.size());
	  return result ;
  }


  
  
  public void makeOrder(Order order) throws SQLException{

	  counter = 0;
	  
	  System.out.println("ladu enne splitti " + order.getWarehouse() + " " + order.getTeostaja());
	  Vector splitted =  splitOrder(order);
	  Enumeration en = splitted.elements();
	  while(en.hasMoreElements()){
		
	 Order ord =  sortOrder((Order)en.nextElement());			  
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
			  
			        ord.setTeostaja(order.getTeostaja());
     			   makeOutput(ord);
	    		  // addListings(ord); 
	    		   
	    		   
	  }
	  

      setNumAddedLines(counter);
  }

  
  public double getPrice(String id, String hinnakiri, String klient,double stPrice) throws SQLException {

	  	
	    String  sql1 = 	    
		"SELECT SC18009 as disc, SC18008 AS price " +
	               "  FROM SC180100 INNER JOIN SL010100 ON  left(SC18002,7)= SL01056 LEFT JOIN SY240100 on SC18009=SY24002 and SY24001='BG' WHERE " + 
	                " SC18001='C' AND CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011 " +
					" and SL01001 = '" + klient + "' and SUBSTRING(SC18002,11,35)='" + id + "' ";
	    
	           //System.out.println(sql1);
			    double acc = 0;
			    Connection con6 = DriverManager.getConnection(url, "mobile","mobile");
			    Statement st1 = con6.createStatement();
			    ResultSet rs1 = st1.executeQuery(sql1);
			    
	    if (klient != null){
		    while (rs1.next()) {
		    	double dd = 0;
		    	try{
		    	 dd = rs1.getDouble("disc");
		    	}catch(Exception ee){
		    		
		    	}
		    	
			      acc = rs1.getDouble("price") *((100-dd)*0.01);
			    //  System.out.println("hind ja ale " + acc + " " + dd );
			    }
		    rs1.close();
		    st1.close();
		    String sql0 = " SELECT SL01002 AS nimi,SL01001 AS kood, case when  SC45005 >0 then SC45005 else 0 end AS ale" +
	        "  FROM SL010100 kl INNER JOIN SC450100    ON SL01036=SC45003  " +
	       "   WHERE SL01001 ='" + klient + "' ORDER BY SL01002";
	   st1 = con6.createStatement();
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
	   // System.out.println("Teine kontroll " + sql1);
	 
	     st1 = con6.createStatement();
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
			  	    
				  //  System.out.println(sql1);
				    

				   st1 = con6.createStatement();
				   rs1 = st1.executeQuery(sql1);

				    while (rs1.next()) {
				      acc = rs1.getDouble("price") ;

				    }

	  	   		    rs1.close();
	                st1.close();

			  }
  con6.close();
			    System.out.println("Klient: " + acc + " " + id);
			    return acc;

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
  
  private String getPrGroup(String aa) throws SQLException{
	  	String res = null;
	 	 String	 sql2 = "SELECT RTRIM(SC01037) AS toode FROM SC010100  WHERE RTRIM(SC01001)=  '" + aa + "'";
				
				Statement st1 = con.createStatement();
				ResultSet rs1 = st1.executeQuery(sql2);
				
				 if (rs1.next()) {
				   res = rs1.getString("toode").trim();
				 }
				
		rs1.close();
		st1.close();
	  	return res ;
	  }
	  
  
  private void addListings (Order order) throws SQLException{

	    Enumeration enumr = order.getOrderLines().elements();
	    String grupid = parent.getProps().getProperty("listing.grupp");
	    System.out.println("listing " + grupid);
	    while (enumr.hasMoreElements()){
	      
	      OrderLine orderLine = (OrderLine)enumr.nextElement();
	      String itemid = orderLine.getToode();
	  
		 
	      String prodshort = "";
	      String grupp = "";
	      
	      String      sql2 = " select SC01011 , SC01037  from SC010100 where SC01001 = '"  + itemid+ " '";
	        Statement st = con4.createStatement();
	        ResultSet rs = st.executeQuery(sql2);
	        if (rs.next()){
	        	prodshort = rs.getString("SC01011");
	        	grupp = rs.getString("SC01037");
	        }
	        rs.close();
	        st.close();
	        
	        System.out.println("listing2 " + grupp + " " + prodshort );
	        System.out.println("listing3 " + sql2  );
	        
	           sql2 = "  select * from PXPI0100 where DateStamp > getdate ()-0.5  and ProductID='"  + itemid+ "' and CustomerID='"  + order.getCompany()+ "' ";
	        boolean eioleOlemas = true;
	       Statement  st3 = con2.createStatement();
	        ResultSet rs3 = st3.executeQuery(sql2);
	        if (rs3.next()){
	        	eioleOlemas = false;
	        	System.out.println("eioleOlemas " + eioleOlemas);
	        }
	        rs3.close();
	        st3.close();
	        
	        
	        	 if(eioleOlemas){
	        	   String      sql = "insert into PXPI0100 (CustomerID ,ProductID, DateStamp ,IsOOS , InList, SalesmanCode ,WarehouseID, Product, AvailQTY)"+
	        	      "values ( '" + order.getCompany() + "','" + itemid + "',getdate(),0,1,'" + order.getAgent() + "','10','"+ prodshort.trim() +"',1) ";

	        		              log.info("LISTING_LINE_SQL: " + sql);
	        		              try{
	        		            	  PreparedStatement st2 = con3.prepareStatement(sql);
		        		              st2.executeUpdate();	              
		        		              st2.close();  
	        		              }catch (Exception e){
	        		            	log.error(e);  
	        		              }
	        		            
	              }
	          }
	       }
	    

  
  
  private Order sortOrder (Order order){
	  
	  Order ord23 = new Order(order.getCompany(),order.getAgentFirstName());
	  ord23.setAgentCode(order.getAgentCode());
	  ord23.setDokNr(order.getDokNr());
	  ord23.setInfo(order.getInfo());
	  ord23.setCompName(order.getCompName());
	  ord23.setChanged(order.isChanged());
	  ord23.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord23.setStatus(order.getStatus());
	  ord23.setWarehouse(order.getWarehouse());
	  
	
	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
	
		     String itemid = lin.getToode();	     
		    String sorting =  identfySorting(itemid);
		    		    
		     	  ord23.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			sorting + " " + lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);

	  }

	  return ord23 ;
  }

  
 
  public String identfySorting (String prodID){
	  	


	    String sql1 = " Select SC01015 + SC01001 AS sort FROM SC010100 pr  where pr.SC01001='" + prodID + "'";
	  log.info("1_LINE_SQL: " + sql1);

	    String sort = "000";

	    Statement st1;
		try {
			st1 = con5.createStatement();
		
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
  
  
  
 private void makeOutput(Order order){
 	StringBuffer sb = new StringBuffer();
 	 nf.setGroupingUsed(false);
 	 Hashtable pandikas =  new Hashtable();
 	 String tahtkood = " ";
 	 if(order.getCompName() != null && order.getCompName().trim().length() == 2){
 		 tahtkood = order.getCompName();
 	 }
 	try {
 	  int cou = 0;
      
 	  if (order.getStatus() == 0 && order.getPaymTerm()>0){
 		 tahtkood = this.getMTKood(order.getPaymTerm()); // see on uus asi 
 	  }
 	  
 	    String cust = order.getCompany();
 	    
 	    trouble = null;
 	    if("true".equals(parent.getProps().getProperty("creditcheck")) && hasCreditProblem(cust)){
 	       System.out.println("Kliendil on krediidi probleem" + cust);
 	    	this.trouble = "CREDITPROBLEM" + buf.toString();
 	    	return;
 	    }
 	  
		String agent = this.identifyCustomerAGENT(cust);
		if(!order.getAgent().equals("111")){
			agent = agent.substring(0,2)+ order.getAgent();
		}
		
		if(order.getShipMode()!= null && order.getShipMode().trim().length() >0 && order.getStatus()!= 98 ){
			agent = order.getShipMode();
			System.out.println("LEIDSIN RINGI MIS ON TELLIMUSES " + agent);
		}
		
		System.out.println("Tuvastatud agent :" + agent );
		boolean usedev = true;
		String ref = order.getTeostaja();
		if ( ref == null ){
			ref =  " ";
		}
		String com = order.getEmailBody();
		if ( com == null ){
			com =  " ";
		}		
		String ware = order.getWarehouse();
		System.out.println("ladu tellimusel " + ware);
		if ( ware == null ){
			ware = "10";
		}
		if(!ware.equals("93")){
			ware = "10";
		}
		 String doknr = order.getDokNr();
		 
		 System.out.println("Doknr enne "  + doknr );
         if(!isNumeric(doknr)){//kui ei ole numbriline 
        	 if(order.getAgent().equals("6") || order.getAgent().equals("7") || order.getAgent().equals("9")){
        		 doknr =  "44" + this.getDoknr("OR29");
        	 }else {
        		 doknr = this.getDoknr("OR29");
        	 }       
         }
         
         if(doknr.trim().length() == 00){
        	 usedev = false;
        	 
        	 if(order.getAgent().equals("6") || order.getAgent().equals("7") || order.getAgent().equals("9")){
        		 doknr =  "44" + this.getDoknr("OR29");
        	 }else {
        		 doknr = this.getDoknr("OR29");
        	 }
        	
         }
         System.out.println("Doknr p�rast "  + doknr );
        
         String type = "1";
         
         
         
         
         
    Vector lines = order.getOrderLines();
      for (int i = 0; i < lines.size(); i++) {
        
    	 
          
          System.out.println("Use devider " + usedev);
 		
    	  
      	OrderLine line = (OrderLine)lines.elementAt(i);
          String toode = line.getToode();
          if(order.getStatus()==60){ //kui on telema tellimus/
    		/*�  String grupp= getPrGroup(toode);
    		  if(grupp != null && grupp.equals("10")){ //n�tsude puhul toimub jagamine
    	         	 usedev = true;  
    		  }else{
    			    usedev = false;
    		  }*/
    		   usedev = false; //aleksei palus jagamise maha v�tta 
          }
          
          if(order.getStatus() > 10){ //kui on e tellimus/
    		  String grupp= getPrGroup(toode);
    		  if( grupp != null && grupp.equals("10")){ //n�tsude puhul toimub jagamine
    	         	 usedev = false;  
    		  }
    		  if(order.getStatus() == 98 && grupp != null && !grupp.equals("10")){
    			  usedev = true;  
    		  }
    		 //  usedev = false; //aleksei palus jagamise maha v�tta 
          }
          
          double devider = 1;// getMultiplier(toode);//aleksei palus suitsude jagamise maha v�tta  alates 1.1.2013
          if(!usedev){
        	  devider = 1;
          }
            double disc = line.getDiscount();
            if(order.getStatus() == 60){
            	disc = 0;
            }

            nf.setMaximumFractionDigits(4);
 
          if (i == 0 ){
          	if( line.getKogus() < 0){
          		System.out.println("kogus " + line.getKogus());
             // 	doknr = getDoknr("OR05");
              	type = "8";
          	}
         	
          }
         	
     String nimi = getNimi(toode);
     if(nimi == null){
    	 nimi = line.getTooteNimi();
     }
         
     
     String pant = getPandiPakend(toode);
     
     if(pant != null && disc != 100){
    	  if(pandikas.get(pant) == null){
    		  pandikas.put(pant,line.getKogus()+"");
          } else {
          	pandikas.put(pant,(new Double((String)pandikas.get(pant)).doubleValue()+line.getKogus())+"");
          }
       }
     
     
     String unit = line.getUnit();
     double kogus = line.getKogus();
     if(unit != null && unit.equals("DPA")){ 
  	   System.out.println(unit + " " + line.getToode() + " enne " + kogus );
  	   if(line.getBonus()> 1){
  		   kogus = kogus * line.getBonus();
  		   System.out.println(unit + " " + line.getToode() + " bonus " + kogus );
  	        }
  	      }  
     if(doknr.length()>10){
    	 doknr = doknr.substring(doknr.length()-10);
     }
     
     String partii = " ";
     if(order.getStatus() == 80 && line.getPartii() != null && line.getPartii().trim().length() > 0 ){
    	 partii =  line.getPartii();
     }
      	sb.append(addSpaces(doknr, 10));
      	sb.append(type);//11
        sb.append(addSpaces(cust, 10));//21
        sb.append(addSpaces(cust, 10));//32
        sb.append(getTana());//40 t�nana kuup�ev
        sb.append(getTana());//48  tarne kuup�ev getTarne(order) Mari soovil asendasin t�nase kuup�evaga
        sb.append(addSpaces(agent, 3));//51
        sb.append(addSpaces(ware,6));//57 ladu 
        sb.append(getLineNr(cou + 1));//63 reanumber
        sb.append("000");//66
        sb.append(addSpaces(toode, 10));//76 tootekood
        sb.append(addSpaces(nimi , 25));  //101 tootenimi
        sb.append(addSpaces(" ",25));//126
        sb.append(addSpaces(((int) (kogus/devider)) + "", 22));//148 kogus
        sb.append(addSpaces(" ",32));//180 lisame tyhikui kus seda ei loeta
        sb.append(addSpaces(com,70));//250 kommentaar
        sb.append(addSpaces(" " ,4)); //254
        sb.append("512   ");//260
        sb.append(addSpaces(partii,50));//310tahtkood
        sb.append(addSpaces(tahtkood,6));//316
        sb.append(addSpaces(nf.format(disc).replace(',','.'), 6));
        

        sb.append(getDisc(disc) + "00.00");
        sb.append(addSpaces(ref, 20));
        sb.append(System.getProperty("line.separator"));
        
       cou++;
      }
//lisame pandipakendi  !!!!!!!
      if(pandikas.size()>0){
        	Enumeration enumpant = pandikas.keys();
         while(enumpant.hasMoreElements()){
         	
         	String kood = (String)enumpant.nextElement();
         	if(kood .trim().length()>0){
         	
      	double pand = new Double((String)pandikas.get(kood)).doubleValue();
      	  
            nf.setMaximumFractionDigits(4);
            nf.setGroupingUsed(false);
           
        	 sb.append(addSpaces(doknr, 10));
           	 sb.append(type);//11
             sb.append(addSpaces(cust, 10));//21
             sb.append(addSpaces(cust, 10));//32
             sb.append(getTana());//40 t�nana kuup�ev
             sb.append(getTana());//48  tarne kuup�ev getTarne(order) Mari soovil asendasin t�nase kuup�evaga
             sb.append(addSpaces(agent, 3));//51
             sb.append("10    ");//57 ladu 
             sb.append(getLineNr(cou + 1));//63 reanumber
             sb.append("000");//66
             sb.append(addSpaces(kood, 10));//76 tootekood
             sb.append(addSpaces("" , 25));  //101 tootenimi
             sb.append(addSpaces(" ",25));//126
             sb.append(addSpaces(nf.format(pand).replace(',','.').replaceAll(" ",""), 22));//148 kogus
             sb.append(addSpaces(" ",32));//180 lisame tyhikui kus seda ei loeta
             sb.append(addSpaces(com,70));//250 kommentaar
             sb.append(addSpaces(" " ,4)); //254
             sb.append("512   ");//260
             sb.append(addSpaces(" ",50));//310   
             sb.append(addSpaces(tahtkood,6));//316
             sb.append(addSpaces(" ", 6));
             sb.append("00.0000.00");
             sb.append(addSpaces(ref, 20));
             sb.append(System.getProperty("line.separator"));
          
                  
         
         cou++;
     	
         	}
        	
        }
        }
      
      
      
      counter = counter + cou;
    	
      String fn = doknr;
      if(doknr.trim().length()== 0){
    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
      }
      String fileName = escape(parent.getProps().getProperty("conv.dir") + agent)  + "." + fn  +  ".txt";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sb.toString().getBytes());
        fos.close();

	} catch (Exception e) {
		e.printStackTrace();
	}
 }
  private String getDisc(double disc){
	  String ret = "00.00";
	  if(disc >0 ){
		  ret = disc + "";
		  if(ret.indexOf(".") != -1){
			  ret = (ret + "00").substring(0,ret.indexOf(".") + 3);
		  }else{
			  ret = ret + ".00";
		  }
		  if(ret.length() < 5){
			  ret = "0" + ret;
		  }
		  
	  }
	  return ret ;
  }
  
  public static boolean isNumeric(String s){
      for (int i = 0 ; i < s.length(); i++){
        if (!isNumeric(s.charAt(i))){
          return false;
        }
      }
      return true;
    }

    public static boolean isNumeric(char ch)
    {
        return  ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4' || ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9';
    }


 private String getDoknr(String aa) throws SQLException{
 	String res = null;
	 String	sql1 = "select SY68002 as nr from SY6801XX where SY68001 = '" +aa +"'"; 
			
			Statement st1 = con.createStatement();
			ResultSet rs1 = st1.executeQuery(sql1);
			
			if (rs1.next()) {
			res = rs1.getString("nr").trim();
			
			Statement ss = con.createStatement();
			String sql2 = "UPDATE SY6801XX set SY68002=SY68002+1 where SY68001 = '" +aa +"'";
			ss.executeUpdate(sql2);
			 ss.close();
			
			}
			
			
			
	rs1.close();
	st1.close();
 	return res ;
 }
 
 
  public String getDriverVersion(){
    return "ScalaTobaImport";
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

  
  private String getLineNr(int i){
  	String res = i + "";
  	if (res.length() == 1){
  		res = "00000" + res;
  	} else if (res.length() == 2){
  		res = "0000" + res;
  	} else if (res.length() == 3){
  		res = "000" + res;
  	} else if (res.length() == 4){
  		res = "00" + res;
  	} else if (res.length() == 5){
  		res = "0" + res;
  	}
  	return res;
  }
 
   private String getTana(){
	    Calendar cal = Calendar.getInstance();

	    String result = null ;
	    	 String yy = cal.get(Calendar.YEAR) + "";
	    	  String mm = (cal.get(Calendar.MONTH)+1) + "";
	    	  if(mm.length()== 1){
	    	  	mm = "0"+mm;
	    	  }    	  
	    	  String dd = (cal.get(Calendar.DATE))+"";
	    	  if(dd.length()== 1){
	    	  	dd = "0" + dd;	  
	            }
	    	  result = yy + mm + dd;
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
  
  public String getKarantiinFile(){
	    return parent.getProps().getProperty("imp.karantiin");
	  }
  private NumberFormat calNF = NumberFormat.getInstance();


  private String url = "jdbc:odbc:scalamobile";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private Connection con2;
  private Connection con4;
  private Connection con5;
  private Connection con3;
  private int counter = 0;
  private String trouble = null;
  private ImportPanel parent;

public String getTrouble() {
	return trouble;
}
}
