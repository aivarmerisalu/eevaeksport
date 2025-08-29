package eevaeksport.imp.driver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
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
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class AxaBuraLatImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public AxaBuraLatImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

    nf.setMaximumFractionDigits(4);
    nf.setMinimumFractionDigits(4);
    nf.setGroupingUsed(false);
    try {
      Class.forName(databaseDriverName).newInstance();
    
      //stmt = con.createStatement();
    } catch (Exception e){
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
  
//  private double  rate = 0.702804;

  private double  rate = 1;
  
  private void sendExceptionEmail(String buf){
	  String address = parent.getProps().getProperty("exceptionemail");
	  if(address != null ){
		  Email email = new Email(parent.getEksportFrame().getString("importerror"));
         
          email.setFrom(parent.getProps().getProperty("from"));
          email.setSMTP(parent.getProps().getProperty("smtphost"));
          email.setUser(parent.getProps().getProperty("smtpuser"));
          email.setPass(parent.getProps().getProperty("smtppass"));
          email.setSubjectFilesTo(parent.getEksportFrame().getString("importerror")  );
          if(parent.getProps().getProperty("language").equals("Russian")){
            email.setEncoding("KOI8-R");
          }
          
          email.setEncoding("iso-8859-1");
          try {
        	  
        	email.setBody(buf.toString());

            StringTokenizer st =  new StringTokenizer(address,";");
            while(st.hasMoreTokens()){
            	String aadr = new InternetAddress(st.nextToken()).toString();
              email.setFilesTo(aadr);
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
  
  
  
  
  
  private void sendNotification(Order order){
	  
	  String cmd0 = " select emp.emplid as kood ,agentsid  + ' ' + del_name as nimi from  TraAgentsTable tra inner join empltable emp " + 
       "  on emp.dataareaid= tra.dataareaid and ltrim(rtrim(tra.emplid))= ltrim(rtrim(emp.emplid)) where  " +
       "   emp.dataareaid='bb' and  tra.dataareaid ='bb' and emp.emplid = '" + order.getAgent()+ "' order by agentsid desc";
		     
	  String sql1 = "  SELECT itemid AS toode ,inv.namealias as name FROM inventtable inv   " +
      " WHERE inv.dataareaid='bb'  and IsOutsourceItem = 1 ";
	   

		     
		
	  
	  String sql = " SELECT cus.name, cus.accountnum, (case when len(cus.invoiceaccount)>0 then cus.invoiceaccount " +
	  	   " else cus.accountnum end) AS payer, " +
            "  cus.address ,salesgroup AS agent , '' as mail " +
            "  FROM custtable cus   WHERE  cus.dataareaid='bb' " +
            "  AND cus.accountnum = '" + order.getCompany() + "'";
	  try{
		
	      Connection con2 = this.getCon();
		  Statement  st1 = con2.createStatement();
	     String agnimi = "";
	      ResultSet rs = st1.executeQuery(cmd0);
	          if(rs.next()){
	              String agent = rs.getString("kood").trim();
		            String nimi = rs.getString("nimi").trim();
		            agnimi = nimi;
		      }
	          rs.close();
	          st1.close();
	
		  
	          TreeMap names = new TreeMap();
	            st1 = con2.createStatement();
	 	     
	 	       rs = st1.executeQuery(sql1);
	 	          if(rs.next()){
	 	              String kood = rs.getString("toode").trim();
	 		            String nimi = rs.getString("name").trim();
	 		            names.put(kood, nimi);
	 		      }
	 	          rs.close();
	 	          st1.close();
	 	    
	          
	          
		  
		  st1 = con2.createStatement();
	      ResultSet  rs1 = st1.executeQuery(sql);
	      System.out.println(sql);  
	      if (rs1.next()) {
		        String name  = rs1.getString("name").trim();
		        String payer = rs1.getString("payer").trim();
		        String aadr  = rs1.getString("address").trim();
		        String mail =  parent.getProps().getProperty("extraorderemail");
		      	System.out.println("Klient Eraldi tellimusel " + name  + " " + mail + " " + payer);
		      	String comment = "";
		      	if(order.getInfo() != null && order.getInfo().length() > 0){
		      		comment = order.getInfo();
		      	}
		      	
		      	if(mail!= null && mail.trim().length() > 0 ){
		        	sendEmail("Pasūtījuma numurs: " + order.getDokNr() +  "\nKlients : " + name + " " + aadr +
		        			 "\nAģents: " + agnimi + "\n\nKrājuma kods Krājuma nosaukums Cena Daudzums" 
		        			 + Converter.getOrderText(order, names) + "\n"
		        			+ "Komentāri : " + comment , 
		        			"Saņemts pasūtījums par iekārtu piegādi: " + name  + " Nr.: " + order.getDokNr() + " Dat.:" +
		        			getDateString(order.getShipCal())
		        			,mail.trim()); //mail.trim()
		  		}
		      }
	      

	  /*  Pasūtījuma numurs: номер заказа
	      Klients: ANFAZ SIA Аvotu iela 76 Riga LV-1009 LV
	      Aģents: 5NF Marite Lapuhova

	      Krājuma kods Krājuma nosaukums Cena Daudzums
	      127000009 *Gāzes Balons 13.4Ltr (tara) 71.14000 12

	      Komentāri: комментарий из программы
      */
		    rs1.close();
		    st1.close();
		    con2.close();
		 	  
	  }catch (Exception e){
		 e.printStackTrace(); 
	  }
	  
  }
  
  
  
  
  private void sendEmail(String body,String subjekt,String to ){
		Email mail = new Email(null);
		mail.setSMTP(parent.getProps().getProperty("smtphost"));
		mail.setFrom(parent.getProps().getProperty("from"));
		mail.setSubjectFilesTo(subjekt);
		mail.setEncoding("UTF-8");
		//mail.addAttach(f.getAbsolutePath());
		mail.setSendNotice(false);
		try {
			mail.setBody(body);
		} catch (MessagingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		StringTokenizer st = new StringTokenizer(to,",");
		while(st.hasMoreTokens()){
			String mai = st.nextToken();
			mail.setFilesTo(mai);
			try {
				mail.send();
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}	
  }
  
  

  
  
  private Connection getCon(){
	  Connection con = null;
	  try{
		  con = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
			        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	    } catch (Exception e){
	    	 try{
	    	  con = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
				        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	    	 } catch (Exception u){
	    		 try{
	   	    	  con = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
	   				        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	   	    	 } catch (Exception x){
	   	    		 this.sendExceptionEmail(x.toString());
	   	    	 } 
	    	 }
	       parent.showError(e);
	    }
	
	return con;
	  
  }

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  public String getImportFile(){
   return parent.getProps().getProperty("imp.read");
 }

  public String identifyCustomer(String id,String regnr) throws SQLException {

    String acc = null;
    if(regnr != null && regnr.trim().length()>0){

    String sql2 = " SELECT accountnum  AS kood FROM custtable where dataareaid='bb' and  (REGNUM_W)= '" + regnr + "' AND ouraccountnum = '" + id + "'";
    System.out.println(sql2);
    

Connection con3 = getCon();
    Statement st2 = con3.createStatement();
    ResultSet rs2 = st2.executeQuery(sql2);

    while (rs2.next()) {
      acc = rs2.getString("kood").trim();

    }
    rs2.close();
    st2.close();
    con3.close();

    }


    if(regnr == null && acc == null){

        String sql2 = " SELECT accountnum  AS kood FROM custtable where dataareaid='bb' and   accountnum = '" + id + "'";
        System.out.println(sql2);

        Connection con3 = getCon();

        Statement st2 = con3.createStatement();
        ResultSet rs2 = st2.executeQuery(sql2);

        while (rs2.next()) {
          acc = rs2.getString("kood").trim();

        }
        rs2.close();
        st2.close();
   con3.close();
        }

    if(regnr == null && acc == null){

        String sql2 = " SELECT accountnum  AS kood FROM custtable where dataareaid='bb' and   namealias = '" + id + "' ";
        System.out.println(sql2);


        Connection con3 = getCon();
        Statement st2 = con3.createStatement();
        ResultSet rs2 = st2.executeQuery(sql2);

        while (rs2.next()) {
          acc = rs2.getString("kood").trim();
        }
        
        rs2.close();
        st2.close();
        con3.close();
        }
    
  	if (acc == null){

  	String sql1 = " SELECT accountnum  AS kood FROM custtable where dataareaid='bb' and  (REGNUM_W)= '" + id + "' AND ouraccountnum = '" + id + "'";
    System.out.println(sql1);


    Connection con3 = getCon();
    Statement st1 = con3.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim();

    }
    rs1.close();
    st1.close();
  con3.close();
  	}
   // System.out.println("Klient: " + acc);
    return acc;

  }

  
  
  public String getCustID(String ag){
	  String res = null;
	  try{
	
		  
		 // leiame axast  transpordi kliendi numbri  
		  String sql = "  select traaddressid as transa from custtable where dataareaid ='bb' and  accountnum ='" + ag +"'";
		    String transa ="" ;
		    Connection con7 =   DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
		              , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	      Statement st = con7.createStatement();
	      ResultSet rs = st.executeQuery(sql);
	      if(rs.next()){
	    	  transa = rs.getString("transa").trim();
	      }
	      rs.close();
	      st.close();
	      con7.close();
	      
		  
		  
		   sql = " select [ID] as cust from [E-agent].epasaule.[clients] where  status != 'D' and [BACKOFFICE_ID] ='" + transa + "'";
	Connection ccc = DriverManager.getConnection(url2 , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	       st = ccc.createStatement();
	       rs = st.executeQuery(sql);
	      while(rs.next()){
	        res = rs.getString("cust");
	      }
	      rs.close();
	      st.close();
	      ccc.close();

	  }catch(Exception e){
		  e.printStackTrace();
	   }	   
		    return res;
		  }
  
  
  private String getProdID(String prod){
	  String res = null;
	  try{
		  String sql = " select [ID] as prod from [E-agent].epasaule.[PRODUCTS] where  [BACKOFFICE_ID] ='" + prod + "'";
	Connection ccc = DriverManager.getConnection(url2 , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	      Statement st = ccc.createStatement();
	      ResultSet rs = st.executeQuery(sql);
	   //   System.out.println("Toote ID leidmine " + sql);
	      while(rs.next()){
	        res = rs.getString("prod");
	      }
	      
	      if(res == null || res.trim().length()== 0){
	    	sql = " select [ID] as prod from [E-agent].epasaule.[PRODUCTS] where  [BACKOFFICE_ID] ='B" + prod + "'";
	    	
	    		  rs = st.executeQuery(sql);
	    		  while(rs.next()){
	    		        res = rs.getString("prod");
	    		      }
	      }
	      
	      rs.close();
	      st.close();
	      ccc.close();

	  }catch(Exception e){
		  e.printStackTrace();
	   }	   
		    return res;
		  }
  
  
  
  private double getProdVol(String prod){
	  double res = 0;
	  try{
		  String sql = "  SELECT   tracrateqty AS kastis,standardpalletquantity AS alusel FROM inventtable inv   " +
       " WHERE inv.dataareaid='bb'  and itemid  ='" + prod + "'";
	    Connection ccc = DriverManager.getConnection(url + "axa" , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	      Statement st = ccc.createStatement();
	      ResultSet rs = st.executeQuery(sql);
	    //  System.out.println("Vol leidmine " + sql);
	      while(rs.next()){
	    	  double kastis = rs.getDouble("kastis");
	    	  
	        res = rs.getDouble("alusel");
	        if(res == 0){
	        	res = kastis *60;
	        }
	     
	      }
	      
	      rs.close();
	      st.close();
	      ccc.close();

	  }catch(Exception e){
		  e.printStackTrace();
	   }	   
		    return res;
		  }
  
  
  
  
  
  private String getAgentID(String ag , String custID){
	  String res = null;
	  
	  if(ag.equals("HOUSE1")){
		  ag = "729";
	  }
	  if(ag.equals("HOUSE2")){
		  ag = "816";
	  }
	  if(ag.equals("HOUSE3")){
		  ag = "527";
	  }
	  // leida vastavalt kliendile 
	  try{
		  
		  String sql3 = "select  agent from TraAgentCustTable cus inner join TraAgentsTable tra on cus.dataareaid= tra.dataareaid    " +
			"	 and cus.agent = tra.agentsid INNER JOIN EMPLTABLE e ON e.dataareaid = tra.dataareaid  and cus.dataareaid = tra.dataareaid and e.emplid=tra.emplid  " +
		"		 and tra.emplid ='" + ag + "' and custaccount = '" + custID + "' and Tsmanager = 1 " +
		"		 where cus.dataareaid = 'bb' and tra.dataareaid = 'bb' and e.dataareaid = 'bb' order by tra.dimension";
		  
		  Connection con3 = getCon();
			  
			   Statement st2 = con3.createStatement();
			      ResultSet rs2 = st2.executeQuery(sql3);
			      if(rs2.next()){
			        res = rs2.getString("agent");
			        System.out.println("Telesales  baas muutsin agendi " + res );
			      }
			      rs2.close();
			      st2.close();
			      con3.close();
			      String sql = "  select agentsid from TraAgentsTable  where emplid = '" + ag + "'";
			    	
			      if(res == null){
			    	  Connection con4 = getCon();
			    	     Statement st = con4.createStatement();
			    	     ResultSet rs = st.executeQuery(sql);
			    			 
			    			
			    			      
			    			      while(rs.next()){
			    			        res = rs.getString("agentsid");
			    			      }
			    			      rs.close();
			    			      st.close();  
			    			  con4.close();
			    			
			    				    	  
			      }
    	  
		  
		   sql = " select [ID] as agent from [E-agent].epasaule.[agents] where  status != 'D' and [BACKOFFICE_ID] ='" + res + "'";
	Connection ccc = DriverManager.getConnection(url2 , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	Statement st = ccc.createStatement();
	ResultSet  rs = st.executeQuery(sql);
	      
	      while(rs.next()){
	        res = rs.getString("agent");
	      }
	      rs.close();
	      st.close();
	      ccc.close();

	  }catch(Exception e){
		  e.printStackTrace();
	   }	   
	 // System.out.println("AGENTID EPAS " + res );
		    return res;
		  }
  
  private double getPrice(String cust , String toode ,double disc){
	  double res = 0;
	  try{
		  //leiame hinnakirja 
		  String sql = "  select pricegroup as hinnak from custtable where dataareaid ='bb' and accountnum ='" + cust +"'";
		    String hinnak = null ;
		    Connection con7 =   DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
		              , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	      Statement st = con7.createStatement();
		   		    	
		      ResultSet rs = st.executeQuery(sql);
		      if(rs.next()){
		    	  hinnak = rs.getString("hinnak");
		      }
		      rs.close();		     
		    
	      if(hinnak == null){		    	
		    	hinnak = "_1";
		    }
		  
		      st.close();
		      con7.close();
		      
	      if(hinnak != null){
		      sql = " select PDT.AccountRelation,ItemRelation = ITM.ItemID,price = Convert(Numeric(28, 12),SubString(Max(Convert(Char(1), " +
			     "    PDT.SPMPromotion) +Convert(VarChar(8), PDT.FromDate, 112) +Convert(VarChar, PDT.Amount)),10,28)) " +
		   " from InventTable ITM inner join PriceDiscTable as PDT with (nolock) on PDT.DataAreaID = 'BB' and " +
		 " PDT.ItemRelation = ITM.ItemID where	PDT.Module = 1 and	PDT.AccountCode = 1 and PDT.ItemCode = 0 and " +
			" PDT.FromDate <= CONVERT(date, GETDATE()) and (PDT.ToDate = '19000101' or PDT.ToDate >= CONVERT(date, GETDATE())) " +
		"	and PDT.CURRENCY = 'EUR' and accountrelation = '" + hinnak + "' and ITM.ItemID = '" + toode + "' group by PDT.AccountRelation,	ITM.ItemID " +
		" order by PDT.AccountRelation,ItemRelation";
			      Connection con8 =   DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
			              , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
			       st = con8.createStatement();
			       rs = st.executeQuery(sql);
			      if(rs.next()){
			        res = rs.getDouble("price");
			        //System.out.println("Hind " + res + " " + nf.format(res));
			        if(disc != 0){
			        	res = res * (100d - disc) / 100d ;	
			        }
			      //  System.out.println("Hind " + res + " " + nf.format(res));
			        
			      }
			      rs.close();
			      st.close();
			      con8.close();
	    	  
	      }

	      
	      if(res == 0 ){
	    	  
	    	  
	    	    sql = " SELECT price * " + rate + " AS price from inventtablemodule mod where mod.itemid ='" + toode + "'  and 'bb' = mod.dataareaid " +
	    	 "  AND moduletype=2 ";
	   	  Connection con8 =   DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
	   	              , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	   	       st = con8.createStatement();
	   	        rs = st.executeQuery(sql);
	   	      if(rs.next()){
	   	        res = rs.getDouble("price");
	   	     if(disc != 0){
		        	res = res * (100d - disc) / 100d ;	
		        }
	   	      }
	   	      rs.close();
	   	      st.close();
	   	      con8.close();
	      }
	      
	      
	  }catch(Exception e){
		  e.printStackTrace();
	  }
		   
		    return res;
		  }
  private String getAgent(String ag, String custID, Order ord){
	  String res = null;
	  
	  if(ag.equals("HOUSE1")){
		  ag = "729";
	  }
	  if(ag.equals("HOUSE2")){
		  ag = "816";
	  }
	  if(ag.equals("HOUSE3")){
		  ag = "527";
	  }
	  
	  System.out.println("agendi leidmine " + ag ) ;
	  try{
	  //määrame kas agent on telesales kui jah siis läheme klienditarbeks õiget koodi otsima 
	  
	  String sql3 = "select  agent from TraAgentCustTable cus inner join TraAgentsTable tra on cus.dataareaid= tra.dataareaid    " +
		"	 and cus.agent = tra.agentsid INNER JOIN EMPLTABLE e ON e.dataareaid = tra.dataareaid  and cus.dataareaid = tra.dataareaid and e.emplid=tra.emplid  " +
	"		 and tra.emplid ='" + ag + "' and custaccount = '" + custID + "' and Tsmanager = 1 " +
	"		 where cus.dataareaid = 'bb' and tra.dataareaid = 'bb' and e.dataareaid = 'bb' order by tra.dimension";
	  
	  Connection con3 = getCon();
		  
		   Statement st2 = con3.createStatement();
		      ResultSet rs2 = st2.executeQuery(sql3);
		      if(rs2.next()){
		        res = rs2.getString("agent");
		        System.out.println("Telesales muutsin agendi " + res );
		      }
		      rs2.close();
		      st2.close();
		      
		      if(res == null && (ag.equals("816") || ag.equals("729") || ag.equals("527") )){
		    	 String dimension = "T";
		    	  if(hasH(ord)){
		    		  dimension = "TH";
		    	  }
		    	  System.out.println(" probleemne case dimension " +  dimension);
		    	  String sql = " select agentsid from TraAgentsTable where dataareaid = 'bb' and emplid ='" + ag + "' and dimension = '" + dimension +"'";
				    
			      Statement st = con3.createStatement();
			      ResultSet rs = st.executeQuery(sql);
			      while(rs.next()){
			        res = rs.getString("agentsid");
			      }
			      rs.close();
			      st.close();
		      }
		      
		      
		  if(res == null){// kommentisin välja
			  String sql = " select agentsid from TraAgentsTable where dataareaid = 'bb'" +
			  		" and emplid ='" + ag + "' order by 1";
			    
		      Statement st = con3.createStatement();
		      ResultSet rs = st.executeQuery(sql);
		      while(rs.next()){
		        res = rs.getString("agentsid");
		      }
		      rs.close();
		      st.close();
  
		  }
		  con3.close();
	  }catch(Exception e){
		  e.printStackTrace();
	  }
	  System.out.println("AGENTID AXA " + res ); 
		    return res;
		  }

  
  
  private String getAgent2(String ag){
	  String res = null;
	
	  
	  System.out.println("agendi leidmine " + ag ) ;
	  try{
	  //määrame kas agent on telesales kui jah siis läheme klienditarbeks õiget koodi otsima 
	  
	
	  Connection con3 = getCon();
		  
		 
		
			  String sql = " select agentsid from TraAgentsTable where dataareaid = 'bb' and emplid ='" + ag + "'";
			    
		      Statement st = con3.createStatement();
		      ResultSet rs = st.executeQuery(sql);
		      while(rs.next()){
		        res = rs.getString("agentsid");
		      }
		      rs.close();
		      st.close();
  
		  
		  con3.close();
	  }catch(Exception e){
		  e.printStackTrace();
	  }
	  System.out.println("AGENTID AXA2 " + res ); 
		    return res;
		  }
  

  
  
  private boolean hasH(Order ord){
	  
	  
	  Vector lines = order.getOrderLines();
	   
      for (int i = 0; i < lines.size(); i++) {
    	
        OrderLine ol = (OrderLine) lines.elementAt(i);
        String prod = ol.getToode();
	  String sql = " SELECT dimension2_ as dimen FROM inventtable inv  where  inv.dataareaid='bb' and  inv.itemid = '" + prod + "'" +
	  		"  and  dimension2_ = 'H'";
	    boolean bb = false;
	    try{
	    	Connection con3 = getCon();
	    	 Statement st = con3.createStatement();
	         ResultSet rs = st.executeQuery(sql);
	         if(rs.next()){
	           bb = true;
	           System.out.println("leitud H dimensiooniga " + prod);
	         }
	         rs.close();
	         st.close();
	         con3.close();
	    }catch(Exception e ){
	    	e.printStackTrace();
	    }
     
      if(bb){
    	  return true;
      }
      }
	  
	  
	  
	  return false;
  }
  
  public String identifyProduct(String ean, String prodID) throws SQLException{
    String sql1 = "SELECT inv.itemid AS toode ,eancode FROM inventtable inv left JOIN inventtablemodule mod ON mod.itemid = inv.itemid where blocked = 0 and moduletype = 2 and  mod.dataareaid = 'bb' and inv.dataareaid='bb' and inv.telema='1'" +
    		" AND inv.eancode='" + ean + "' order by inv.itemid " ;


    String sql2 = "SELECT inv.itemid AS toode FROM inventtable inv left JOIN inventtablemodule mod ON mod.itemid = inv.itemid where blocked = 0  and moduletype = 2 and  inv.dataareaid = 'bb' and mod.dataareaid = 'bb' and inv.itemid='" + prodID + "' ";

    String prod = null;
    ResultSet rs1 = null;
    Connection con = getCon();
    Statement  st1 = con.createStatement();
  if(prodID != null){
	   
	     rs1 = st1.executeQuery(sql2);
	//	System.out.println(sql2);
	    while (rs1.next()) {
	      prod = rs1.getString("toode").trim();
	  //	System.out.println("prod1 " + prod );
	    } 
	    
  }
 

  

    if(prod == null){
      rs1 = st1.executeQuery(sql1);
      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
      //	System.out.println("prod2 " + prod );
      }
    }
    rs1.close();
    st1.close();
    con.close();
//   System.out.println("prod" + prod +" "+ ean+prodID);
   if(prod == null){
   	prod = prodID;
   }
    return prod;
  }
  
 
  private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    sb.append(cal.get(Calendar.YEAR) + "");
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month + "");

	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day);

	    return sb.toString();
	  }
	  

  private void makeOutput(Order order ,String salesid ){
	 	StringBuffer sb = new StringBuffer();
	 	  counter = 0;
	 	fileName = null;
	 	try {
	 		 
		
  	    String agent = order.getAgent();
  	  if(agent.equals("HOUSE1")){
  		agent = "729";
	  }
	  if(agent.equals("HOUSE2")){
		  agent = "816";
	  }
	  if(agent.equals("HOUSE3")){
		  agent = "527";
	  }
	  
			    
			      if(agent == null || agent.trim().length()== 0){
			    	 agent = "xxx";
			      } else{
			    	  agent = agent.trim();
			      }
			String ref = order.getTeostaja(); //mobiilse telli ref number
			if ( ref == null ){
				ref =  "";
			}
			String com = order.getEmailBody();
			if ( com == null ){
				com =  " ";
			}		
		
			String ware = order.getWarehouse();
			if ( ware == null ){
				ware = " ";
			}
		
			
			String comm = order.getInfo();
			if ( comm == null ){
				comm = " ";
			}
		String fna = salesid;
	//	salesid = salesid + getDateString(order.getShipCal()).substring(6);
			
			String reference  = order.getTeostaja();
			if ( reference != null && reference.trim().length() > 4 && reference.trim().indexOf("<") == -1 &&
					 reference.trim().indexOf("(") == -1	){
				salesid = reference.trim();
			}
	 			  
			
			//salesid = salesid + getDateString(order.getShipCal()).substring(6);
	         
	         String lineSep = System.getProperty("line.separator");
	         sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + lineSep);

	         sb.append("<order OrderId=\"" + salesid + "\">" + lineSep);
	         sb.append("<Customer>" + order.getCompany() + "</Customer> "  + lineSep );
	         sb.append("<Location>000</Location>" +  lineSep);
	         String agh = getAgent(agent,order.getCompany(),order);
	         if(agh == null){
	        	 agh = this.getAgent2(agent);
	         }
	         sb.append("<AgentId>" + agh +"</AgentId> " + lineSep);
	         sb.append("<CreationDate>" + getDateString (Calendar.getInstance())+ "</CreationDate>" +  lineSep);
	         sb.append("<TimeSpent>0</TimeSpent>" + lineSep);
	         sb.append("<DeliveryDate>" + getDateString(order.getShipCal()) +"</DeliveryDate>" + lineSep);
	         sb.append("<DeliveryType>A</DeliveryType> " + lineSep);
	         sb.append("<ORDERTYPE>mer</ORDERTYPE> " + lineSep);
	         if(comm != null && comm.trim().length() > 0){
	        	 sb.append("<Comment>" + comm + "</Comment> " + lineSep); 
	         }
	         sb.append("<details>" + lineSep);
	        // sb.append("markused:" + sep +escape(comm) + lineSep);
	        
	         curOrdSumm = 0;
	         curOrdSummVAT = 0; 
	         
	    Vector lines = order.getOrderLines();
	   
	      for (int i = 0; i < lines.size(); i++) {
	        OrderLine ol = (OrderLine) lines.elementAt(i);
	        if( ol.getKogus() !=0 && order.getStatus() != 5){ //kõik read kui pole status 5  
	      	  sb.append("<line number=\"" + (i+1) + "\">" + lineSep);
	          sb.append("<code>" + ol.getToode() +"</code>"  + lineSep);
	          sb.append("<quantity>" + ol.getKogus() +"</quantity>"  + lineSep);
	       
	          double disc = ol.getDiscount();
	          
	       
	          double price = 0;
	          
	          if(disc > 1 || disc < -1){
	        	  // price = getPrice(order.getCompany(), ol.getToode(),disc);
	        	  if(disc < -1){
	        		  price = ol.getPrice() ;//* (1 + (disc * -1 /100)); 
	        		  System.out.println("Negative price " + price + " " + disc + " " + ol.getPrice());
	        	  }else{
	        		  price = ol.getPrice();  
	        	  }
	        	   
	          }else{
	        	   price = getPrice(order.getCompany(), ol.getToode(),0); 
	          }
	       
	          if(disc == 100){
	        	  price = 0;
	          }
	          
	          curOrdSumm = curOrdSumm + price*ol.getKogus();
	          curOrdSummVAT = curOrdSummVAT + price*ol.getKogus() *1.21;
	          
	          sb.append("<price>" + nf.format(price) +"</price>"  + lineSep);
	          sb.append("</line>" + lineSep);
	         
	  	
	        }
	    
	       counter++;
	      }
	      sb.append("</details>" + lineSep);
          sb.append("</order>" );
	      
	      setNumAddedLines(counter); 

		    	  fileName =  "o-" + agent +"."+ order.getCompany()+"." + fna   + ".xml";
			      System.out.println(fileName);		
			      if(order.getWarehouse()!= null && order.getWarehouse().equals("other")){
			    	    FileOutputStream fos = new FileOutputStream(parent.getProps().getProperty("conv.dir3") + fileName);
				        fos.write(sb.toString().getBytes());
				        fos.close();
				        sendNotification(order);
			        
			      }else{
			    	    FileOutputStream fos = new FileOutputStream(parent.getProps().getProperty("conv.dir") + fileName);
				        fos.write(sb.toString().getBytes());
				        fos.close();
		    	    	  
			        
			      }
			      
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
	  
private String fileName = null;
  

	public void writeConsloStr(String path,String e) {
		
		try {
			
			File f = new File(path);
			if(!f.exists()){
				  try{
				      FileOutputStream fos = new FileOutputStream(path);
				        fos.write("".getBytes());
				        fos.close();
				
			} catch (Exception uu) {
				uu.printStackTrace();
			}
			}
			
			PrintWriter os = new PrintWriter(new FileWriter(
					path, true));
			os.write(e);
			os.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}

}

	
	
	
	 private Vector splitOrder (Order order){
		  
		  Vector result = new Vector();
	      
		  Vector other = getOther();
		  
			  Order ordBio = new Order(order.getCompany(),order.getAgentFirstName());
			  ordBio.setAgentCode(order.getAgentCode());
			  ordBio.setDokNr(order.getDokNr() + "1");
			  ordBio.setInfo(order.getInfo());
			  ordBio.setTeostaja((order.getTeostaja()));
			  ordBio.setChanged(order.isChanged());
			  ordBio.setShipCal(order.getShipCal().getTimeInMillis() +"");
			  ordBio.setStatus(order.getStatus());
			  ordBio.setShipTime(order.getShipTime());
			  ordBio.setShipMode(order.getShipMode());
			  ordBio.setPaymTerm(order.getPaymTerm());
			  ordBio.setReference(order.getReference());
				  
			  ordBio.setWarehouse("other");
		
			  Order ordNorm = new Order(order.getCompany(),order.getAgentFirstName());
			  ordNorm.setAgentCode(order.getAgentCode());
			  ordNorm.setDokNr(order.getDokNr());
			  ordNorm.setInfo(order.getInfo());
			  ordNorm.setTeostaja((order.getTeostaja()));
			  ordNorm.setChanged(order.isChanged());
			  ordNorm.setShipCal(order.getShipCal().getTimeInMillis() +"");
			  ordNorm.setStatus(order.getStatus());
			  ordNorm.setShipTime(order.getShipTime());
			  ordNorm.setShipMode(order.getShipMode());
			  ordNorm.setPaymTerm(order.getPaymTerm());
			  ordNorm.setReference(order.getReference());
			  ordNorm.setWarehouse("norm");
			
			
			  Vector lines = order.getOrderLines();
			  Enumeration enumr = lines.elements();
			
			  while  (enumr.hasMoreElements()){
				  OrderLine lin = (OrderLine)enumr.nextElement();
				  
				  if(other.contains(lin.getToode())){
					    ordBio.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);	 
			  		  
				  }else{
					    ordNorm.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);	 
						  
				  }
				
			
		
			  }


			  if(ordBio.getOrderLines() != null && ordBio.getOrderLines().size() > 0){
				  result.add(ordBio);  
			  }

			  if(ordNorm.getOrderLines() != null && ordNorm.getOrderLines().size() > 0){
				  result.add(ordNorm);  
			  }

		  
		  
		 System.out.println("Result =" +order.getDokNr()
				 + " "+ result.size());
		 
		  return result ;
	  }





	
	
  public void makeOrder(Order order) throws SQLException{
	 
	  

	   Vector splitted =  splitOrder(order);	   
        Enumeration en = splitted.elements();
        while (en.hasMoreElements()){
        	Order ord = (Order)en.nextElement();
        	  
      	  String fileNam = parent.getProps().getProperty("conv.dir") + "LOCK"; 
      	  File lck = new File(fileNam);
      	  if(lck.exists()){
      		  System.out.println("LOCK Exists");
      		  log.error(" LOCK Exists :" + new Date(lck.lastModified()).toGMTString());
      		  System.exit(0);
      	  }else {
      		  try{
      	      FileOutputStream fos = new FileOutputStream(fileNam);
      	        fos.write("".getBytes());
      	        fos.close();
      	        log.error(" LOCK Created :" + new Date(lck.lastModified()).toGMTString());
      } catch (Exception e) {
      	e.printStackTrace();
      }	  }
      	  
      	    String salesid = ord.getDokNr();
      	    if(salesid == null || salesid.trim().length() == 0){
      	    	salesid =	(Calendar.getInstance().getTimeInMillis()+"").substring(3) ;
      	    }
      		salesid =  salesid + getDateString(ord.getShipCal()).substring(6);//unikaalsuse pärast liidame päeva väärtuse otsa
                try{  		
              	  setOrder(ord);
              	boolean salvesta = true;
              	 	 Vector lin = ord.getOrderLines();
              	 	 int nega = 0;
              	 	 for (int i = 0; i < lin.size(); i++) {
              	 		OrderLine ol = (OrderLine) lin.elementAt(i);
              	 		if(ol.getKogus() < 0 && ord.getStatus() == 5){
              	 			nega++;
              	 		}
              	 		
              	 	 }  
              		    if(nega == lin.size()){
              		    	salvesta = false;
              		    }
              	  if(salvesta ){
              	
              		  makeOutput(ord , salesid);
          		  	  String lineSep = System.getProperty("line.separator");
          		  	  if(ord.getWarehouse().equals("other")){
          		  		 writeConsloStr(parent.getProps().getProperty("conv.dir3") + "index.csv",fileName + ",0,,," + lineSep);
               		  }else{
          		  		 writeConsloStr(parent.getProps().getProperty("conv.dir") + "index.csv",fileName + ",0,,," + lineSep);
               		  }
          			   
              	  }else{
              		  setNumAddedLines(lin.size()); 
      	    	  System.out.println("Ei Salvesta kuna neagtiivne on tell " +fileName);	
      	      }
              	   
          		  } catch (Exception e) {
          				e.printStackTrace();
          			}  	
          
          lck.delete();
          log.error(" LOCK Deleted :" + new Date(lck.lastModified()).toGMTString());

          
          String agentid = this.getAgentID(ord.getAgent(),ord.getCompany());
          String custid = this.getCustID(ord.getCompany());
          
         String docid = this.getAndUpdateRecID()+"";

         addInvoiceHeader(salesid,docid,agentid,custid,ord);
         addInvoiceLines(salesid,docid,custid,ord);
    	
        }
	  
	  }
  
  
  public static String getYYYYpMMpDDpTimeNiceString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}

		sb.append("" + cal.get(Calendar.YEAR));
		sb.append("." + month  + ".");
		sb.append(day +" ");
	    
	    return sb.toString();
	  }


  
  public Vector getOther(){
	  String sql = "  SELECT itemid AS toode FROM inventtable inv   " +
      " WHERE inv.dataareaid='bb'  and IsOutsourceItem = 1 ";
	   


 Vector vec = new Vector();
 try{
	  ResultSet rs1 = null;
	    Connection con3 = getCon();
	    Statement  st1 = con3.createStatement();
	      rs1 = st1.executeQuery(sql);
			  while (rs1.next()) {
		      vec.add(rs1.getString("toode").trim());
		    } 

	    rs1.close();
	    st1.close();
	    con3.close();

 }catch(Exception r){
   r.printStackTrace();	 
 }
	  	   System.out.println("Toodet mis splititakse  " + vec.size());
	   
	   
	    return vec;
	  }

  
  public String identifyProductSecond( String custID,String ean) throws SQLException{
	    String sql1 = "select sal.salesstatus,lin.itemid  as toode  from salestable sal inner join salesline lin  on lin.salesid=sal.salesid where  " + 
     " ExternalItemId  != '' and  sal.dataareaid='bb' and lin.dataareaid='bb' and sal.salesstatus=1 " +
	" and sal.custaccount='" + custID + "' and lin.ExternalItemId = '" + ean + "' " ;



	    String prod = null;
	    ResultSet rs1 = null;
	    Connection con3 = getCon();
	    Statement  st1 = con3.createStatement();
	  if(custID != null){
		   
		     rs1 = st1.executeQuery(sql1);
			//System.out.println(sql1);
		    while (rs1.next()) {
		      prod = rs1.getString("toode").trim();
		  	  // System.out.println("Teine tuvastamine prod1 " + prod );
		    } 
	  }
	 

	  
	    rs1.close();
	    st1.close();
	    con3.close();
	   System.out.println("Teine tuvastamine prod " + prod + " " + ean + " " + custID);
	   
	   
	    return prod;
	  }

  
  
private double curOrdSumm = 0;  
private double curOrdSummVAT = 0;
  private void addInvoiceLines(String salesid,String docid,String custid,Order ord){
	  curOrdSumm = 0;  
	  curOrdSummVAT = 0;  
    Enumeration enumr = ord.getOrderLines().elements();
    Vector minusLines = new Vector();
    int counter = 0;
    while (enumr.hasMoreElements()){
      counter++;
      log.info("ORDER_LINE_COUNTER:" + counter);
      OrderLine orderLine = (OrderLine)enumr.nextElement();
      String itemid = orderLine.getToode();
      double kogus = orderLine.getKogus();
  
  double disc = orderLine.getDiscount();
 
  String disctxt = "0";
  if(orderLine.getKogus() < 0){
	  disctxt = disc + "";
  }
  
  double pricestd = getPrice(ord.getCompany(), orderLine.getToode(),0); 
  double priceale = pricestd ;
  if(disc > 1){
	  priceale = orderLine.getPrice();
  }
  
  
 
  
  //"E-agent"
          
  String toode = getProdID(itemid);
  
 
          String sql =" insert into [E-agent].epasaule.[ORDEREDITEMS] " +
        "  (ORDER_ID,PRODUCT_ID,QUANTITY,MEASURE,PRODUCT_PRICE,DISCOUNT,QUANTITY_IN_BASE_UNIT,BASE_UNIT_MEASURE,PRODUCT_PRICE_W_PVN,BASIC_DISCOUNT, " +
        "  SERIES_ID,INVENTORY,DISCOUNT_EXPIRATION_DATE,DISCOUNT_CANCELED,DISCOUNT_EXPIRATION_DATE_CANCEL,STATUS,INVOICED,WAREHOUSE_ID,REAL_PRICE, " +
        "  PREF_DELIVERY_DATE,INVOICED_PRICE,INVOICED_DISCOUNT,INVOICED_DATE,TEST_QUANTITY,POSITION_NUMBER,CLIENT_INVENTORY_CHANGED,REFUSED,OFFERED, " +
        "  ITEM_MESSAGE,RETAIL_PRICE,DISCOUNT_ID,BONUS,EXTRA_FIELD_VALUES,FACING,FIXED_PRICE,CLIENTEQUIPMENT_ID,PRICELIST_ID,UNIT_TYPE,CLIENTPRODUCTPRICE_ID)" +
        "  values " +
        "  (" + docid +"," + toode +"," + kogus + "," + disctxt + "," + priceale + ",0," + kogus + ",0," + (priceale * 1.21)+",0.0,-1,0.0,NULL,NULL,NULL,'A',NULL,-1," + pricestd + ", " +
        "  NULL,0.0,0.0,NULL,NULL,6,0,0,0,NULL,0.0,-1,0,NULL,NULL,NULL,-1,26034855,0,NULL) " ;

       
              log.info("SALES_LINE_SQL: " + sql);
            //  System.out.println(sql);
             try{
            	  Connection ccc = DriverManager.getConnection(url2 , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));      	
                  PreparedStatement st2 = ccc.prepareStatement(sql);
                  if(orderLine.getKogus() < 0){
                	  minusLines.add(orderLine);
                  }
                  if(toode != null && custid != null){
                	  try{

                    	  st2.executeUpdate();   
                	  }catch(Exception e){
                		  e.printStackTrace();
                	  } 
                      
                  }
                	  
                
                  st2.close();
                  ccc.close();	 
             }catch (Exception e){
            	 e.printStackTrace();
             }
            
              }
   
  System.out.println("AAAAAA " + ord.getStatus() + " " + minusLines.size() + " " + ord.getTeostaja() + " " + ord.getInfo());
  
  String ref = ord.getTeostaja();
    if( ord.getStatus() == 5){
    	if(ref!= null && ref.indexOf("{true}") != -1){
    		makeReturnDocs(minusLines,salesid,ref);
        }else{
    	    makeReturnAvaldus(minusLines,salesid,ref);	
        }
    	
    }
    
  }
  
  private void makeReturnAvaldus(Vector minusLines, String salesid , String ref){
	  Enumeration enumr = minusLines.elements();
	  StringBuffer sb = new StringBuffer();
	  double am  = 0;
	  double sum = 0;
	    while (enumr.hasMoreElements()){
	    	 OrderLine orderLine = (OrderLine)enumr.nextElement();
	    	 am  = am + (-orderLine.getKogus() /getProdVol(orderLine.getToode()));
	    	sum = sum +( -orderLine.getKogus() * getPrice(order.getCompany(), orderLine.getToode(),0));
	    }
	    String lineSep = System.getProperty("line.separator");
	    String agid = getAgent(order.getAgent(),order.getCompany(),order);
	    
	  if (am < 0.1){
		  am = 0.1;
	  }
	  
	  String fna = salesid;
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + lineSep);

        sb.append("<GRNotices TplPartnerId=\"\">" + lineSep);
	        sb.append("<Notice RecId=\"1\">" + lineSep);
	        sb.append("<NoticeId>" + salesid + "</NoticeId>" + lineSep);
	        sb.append("<Customer>" + order.getCompany() + "</Customer>" + lineSep);
	        sb.append("<AgentId>" + agid + "</AgentId>" + lineSep);
           
         
            String comment = "";
            String rest = "";
            if(ref != null ){
            	 if( ref != null && ref.indexOf("|") != -1 ){
                 	rest = ref.substring(ref.indexOf("|") + 1);
                 	ref = ref.substring(0,ref.indexOf("|"));
                 	try{
                 		comment = rest.substring(0,rest.indexOf("|"));	
                 	}catch (Exception r){
                 		comment = rest;
                 	}
                 }else{
                 	rest =ref ;
                 	ref = "";
                 }
                 	
            }
            
            String summa = null;
            if(rest.indexOf("(") !=  -1){
            	summa = rest.substring(rest.indexOf("(") + 1,rest.indexOf(")")); 
            }
            String amount = null;
            if(rest.indexOf("<") !=  -1){
            	amount = rest.substring(rest.indexOf("<") + 1,rest.indexOf(">")); 
            }
            if(ref == null || ref.trim().length() == 0){
            	ref = salesid;
            }
            
            sb.append("<Comment>" + comment + "</Comment>" + lineSep);
    	    sb.append("<InvoiceId>" + ref + "</InvoiceId>" + lineSep);
     		sb.append("<ReturnDate>" + getDateString(order.getShipCal()) + "</ReturnDate>" + lineSep);
     		if(summa != null ) {
     			sb.append("<AmountExpected>" + summa  + "</AmountExpected>" + lineSep);
     		}else{
     			sb.append("<AmountExpected>" + sum  + "</AmountExpected>" + lineSep);
     		}
     		if(amount != null ) {
     			sb.append("<QtyExpected>" + amount  + "</QtyExpected>" + lineSep);
     		}else{
     			sb.append("<QtyExpected>" + am  + "</QtyExpected>" + lineSep);
     		}
        	
        	sb.append("</Notice>" + lineSep); 
        	sb.append("</GRNotices>" + lineSep);

	String    fileNam =  "m-" + order.getAgent() +"."+ order.getCompany()+"." + fna  + ".xml";
	      System.out.println(fileName);	
	      try{
	    	  FileOutputStream fos = new FileOutputStream(parent.getProps().getProperty("conv.dir2") + fileNam);
		        fos.write(sb.toString().getBytes());
		        fos.close();
		    
	      }catch(Exception e){
	    	  e.printStackTrace();
	      }
	     
  }

  
  
  
  private void makeReturnDocs(Vector minusLines, String salesid,String ref){
	  Enumeration enumr = minusLines.elements();
	  StringBuffer sb = new StringBuffer();
	    String lineSep = System.getProperty("line.separator");
	    String agid = getAgent(order.getAgent(),order.getCompany(),order);
	  String fna = salesid;
	  String invnr = null;
	  
	  if(ref != null && ref.trim().length() > 0 ){
		  StringTokenizer st = new StringTokenizer(ref,"|");
	        invnr = st.nextToken();
	  }
	  
      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + lineSep);

      sb.append("<order OrderId=\"" + salesid + "\">" + lineSep);
      sb.append("<Customer>" + order.getCompany() + "</Customer> "  + lineSep );
      sb.append("<Location>000</Location>" +  lineSep);
      if(agid == null){
     	 agid = this.getAgent2(order.getAgent());
      }
      sb.append("<AgentId>" + agid +"</AgentId> " + lineSep);
      sb.append("<CreationDate>" + getDateString (Calendar.getInstance())+ "</CreationDate>" +  lineSep);
      sb.append("<TimeSpent>0</TimeSpent>" + lineSep);
      sb.append("<DeliveryDate>" + getDateString(order.getShipCal()) +"</DeliveryDate>" + lineSep);
      sb.append("<DeliveryType>C</DeliveryType> " + lineSep);
      sb.append("<ORDERTYPE>mer</ORDERTYPE> " + lineSep);
      if(invnr != null && invnr.trim().length() > 0){
    	  sb.append("<invoice>" + invnr + "</invoice> " + lineSep);
    	}
      sb.append("<details>" + lineSep);
     // sb.append("markused:" + sep +escape(comm) + lineSep);
     
      
 

   for (int i = 0; i < minusLines.size(); i++) {
     OrderLine ol = (OrderLine) minusLines.elementAt(i);
     if( ol.getKogus() < 0 ){ //ainutlt negatiivsed kogused tellimusse ja ON tagastus 
   	  sb.append("<line number=\"" + (i+1) + "\">" + lineSep);
       sb.append("<code>" + ol.getToode() +"</code>"  + lineSep);
       sb.append("<quantity>" + ol.getKogus() +"</quantity>"  + lineSep);
    
       double disc = ol.getDiscount();
       
    
       double price = 0;
       
       if(disc > 1 || disc < -1){
     	  if(disc < -1){
     		  price = ol.getPrice() ;//* (1 + (disc * -1 /100)); 
     		  System.out.println("Negative price " + price + " " + disc + " " + ol.getPrice());
     	  }else{
     		  price = ol.getPrice();  
     	  }
     	   
       }else{
     	   price = getPrice(order.getCompany(), ol.getToode(),0); 
       }
    
       if(disc == 100){
     	  price = 0;
       }
       
       sb.append("<price>" + nf.format(price) +"</price>"  + lineSep);
       sb.append("</line>" + lineSep);
      
     }
 
    counter++;
   }
   sb.append("</details>" + lineSep);
   sb.append("</order>" );
   

	    	  fileName =  "m-" + order.getAgent() +"."+ order.getCompany()+"." + fna   + ".xml";
		      System.out.println(fileName);		 
		try{

		      FileOutputStream fos = new FileOutputStream(parent.getProps().getProperty("conv.dir4") + fileName);
			        fos.write(sb.toString().getBytes());
			        fos.close();

      			    writeConsloStr(parent.getProps().getProperty("conv.dir4") + "index.csv",fileName + ",0,,," + lineSep);
		}catch(Exception ff){
			ff.printStackTrace();
		}

	  
	  
	  
	  	     
  }

  private void addInvoiceHeader(String salesid, String docid,String agentid,String custid,Order ord) throws SQLException{



      
      String ref = "num:" + salesid;
      if(ord.getInfo() != null && ord.getInfo().trim().length()>0 ){
      	ref = ref + " " + order.getInfo().trim();
      }

      if(ord.getReference() != null && ord.getReference().trim().length()>0 ){
     // 	ref = order.getReference().trim();
      }
      
     

      int time = 0;
      Calendar cal = Calendar.getInstance() ;
      
      Calendar calfact = Calendar.getInstance() ;
      String algus = (cal.getTimeInMillis() + "").substring(0,6);
      if(ord.getStatus() < 4){
    	  calfact.setTimeInMillis(Long.parseLong(algus + salesid.substring(0,salesid.length()-2)));
      }
      
      
      
      time = cal.get(Calendar.HOUR_OF_DAY)*3600 + cal.get(Calendar.MINUTE)*60 + cal.get(Calendar.SECOND);
      
      String custname = "";
      if( ord.getCompName() != null){
    	  custname = ord.getCompName();
      }
      
      if (ord.getShipTime()!= null){
         custname  =  ord.getShipTime().replaceAll("\n"," ") + " " + getYYYYpMMpDDpTimeNiceString(ord.getShipCal()) + " " + custname;
        }
      System.out.println(salesid + "|"+ ord.getCompany() + "|"+ ref +"|"+docid);
     
     String sql = " insert into [E-agent].epasaule.[ORDERS] (ID,ORDERING_DATE,CLIENT_ID,DELIVERY_TYPE,DELIVERY_DATE,STATUS," +
     		"PDA_ORDER_ID,AGENT_ID,ORDERING_TIME,SPLITTED,ORDER_MESSAGE, " +
          " CLIENT_ORDER_NO,PDA_FACT_DATE,summ,summ_pvn) values(" +
          docid +",?," + custid + ",0,?,'X',getdate(),"+agentid+ ",48900,0,'" + ref +"','" + salesid+ "',?, " + curOrdSumm + ","+ curOrdSummVAT + ")" ;
        log.info("addInvoiceHeader2: " + sql);
        System.out.println(sql);
        Connection con7 =   DriverManager.getConnection(url2  , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
        
        PreparedStatement stmt2 = con7.prepareStatement(sql);
        java.util.Date dat = new java.util.Date();
       // System.out.println(dat.toString() + "  sc " + order.getShipCal().getTime().toString() + " ref " + ref);
        if(ord.getShipCal() != null){
        	 dat.setTime(ord.getShipCal().getTimeInMillis());	
        }
         
        Calendar cc = Calendar.getInstance();
        stmt2.setTimestamp(1, new java.sql.Timestamp(cc.getTimeInMillis()));
        stmt2.setTimestamp(2, new java.sql.Timestamp(dat.getTime()));
        stmt2.setTimestamp(3, new java.sql.Timestamp(calfact.getTimeInMillis()));
        if(custid != null){
        	stmt2.executeUpdate();
        }
        stmt2.close();
        con7.close();
    
        addLog("lisatud arve päis -> " + ord.getCompany() + "\n");
  }



  private int getAndUpdateRecID() throws SQLException{
    int nextRecID = 0;
    String sql = "select currentindex + 1 as asi  from [E-agent].epasaule.[tblautoindex] where tablename = 'ORDER_SEQ'";
    System.out.println("getAndUpdateRecID(): " + sql);
    Connection con7 =   DriverManager.getConnection(url2  , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      Statement st = con7.createStatement();
      ResultSet rs = st.executeQuery(sql);
      while(rs.next()){
        nextRecID = rs.getInt("asi");
      }
      rs.close();
      st.close();
      
      
      sql = "UPDATE [E-agent].epasaule.[tblautoindex] SET currentindex =" + (nextRecID) +
      " WHERE  tablename = 'ORDER_SEQ'";
      log.info("getAndUpdateRecID(): " + sql);
      Statement st2 = con7.createStatement();
      st2.executeUpdate(sql);
      st2.close();
      con7.close();

    return nextRecID;
  }


  public String getDriverVersion(){
    return "AxaBuraLatImport";
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
 // private String url2 = "jdbc:odbc:import";
  private String url2 = "jdbc:odbc:report";

  private ImportPanel parent;
  private Order order = null;
  private boolean isAmount = false;
  private int counter = 0;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
