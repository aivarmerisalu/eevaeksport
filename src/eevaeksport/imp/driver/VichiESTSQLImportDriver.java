package eevaeksport.imp.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
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

public class VichiESTSQLImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public VichiESTSQLImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));
    
      //stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }

  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  
  public TreeMap getList(){
	  TreeMap companies = new TreeMap();
	    File file = new File(getImportFile() + "klient" + File.separator + "filiaal.txt");
	    try{
	      BufferedReader reader = new BufferedReader(new FileReader(file));
	      String s = null;
	      while((s = reader.readLine()) != null){
	        StringTokenizer st = new StringTokenizer(s, ";");
	        if (st.hasMoreTokens()){
	          String compID = st.nextToken();
	          if (compID.trim().equals("")){
	          compID =null;
	          }
	          String name = null;
	          if (compID!= null){
	            name = st.nextToken();
	            companies.put(compID, name);
	          }
	         }
	      }
	    } catch (IOException e){
	      parent.showError(e);
	    }
	    return companies;
	  }
  
  
  public String identifyCustomer(String id,String regnr)  {

    String acc = null;
    	
    	try{
    String sql2 = " select k.[No_] AS klient " +
      " FROM [Vičiunai Baltic$Customer] k  where k.[No_] = '" + id.trim() + "'";
    System.out.println(sql2);

    
    Statement st2 = con.createStatement();
    ResultSet rs2 = st2.executeQuery(sql2);

    while (rs2.next()) {
      acc = rs2.getString("klient").trim();

    }
    rs2.close();
    st2.close();

  	}catch(Exception e){
  		e.printStackTrace();
  	}
  	
 
  	

 	if(acc == null){
 		  TreeMap kk = getList();
 		    if(kk.get(id) != null){
 		    	acc = (String)kk.get(id) ;
 		    }
 		 		
 	}
  	
     
 	if(acc == null){
  		//acc = id;
  	}
    
    System.out.println("Klient: " + acc);
    return acc;

  }


  
  public String identifyProduct(String ean, String prodID) throws SQLException{
	
	  
    String sql2 = "SELECT [No_] AS toode  FROM [Vičiunai Baltic$Item] LEFT JOIN [Vičiunai Baltic$Item Unit of Measure]" +
    		" vat ON (vat.[Item No_]=[No_] and vat.[Code] like 'D%' ) " +
			"  WHERE [Brūkšninis kodas] ='" + ean + "' and [Blocked]=0 order by [Blocked] "; // and [Blocked]=0


    String sql1 = " SELECT [No_] AS toode FROM [Vičiunai Baltic$Item] " +
			"  WHERE [No_] = '" + prodID + "' "; // and [Blocked]=0 

    String prod = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

  	System.out.println(sql1);
    if (rs1.next()) {
    	if(ean != null && ean.trim().length()>0){
    	      prod = rs1.getString("toode").trim();	
    	}
    }
    System.out.println("peale esimest " + prod);
    
    if(prod == null || prod.trim().length() == 0  ){
    	System.out.println(sql2);
        rs1 = st1.executeQuery(sql2);
        while (rs1.next()) {

        	if(prodID != null && prodID.trim().length()>0){
                prod = rs1.getString("toode").trim();	
        	}
        }
        System.out.println("peale teist " + prod);
      }
 
    rs1.close();
    st1.close();
    
	   System.out.println("Tagastan toote:"  + prod + " prodID:" + prodID + " ean:" + ean);
    return prod;
  
}
  
  
  private String getCustName(String cuid) {
		 
	  String res = cuid;
	  try{
		  String sql1 = " select k.[Name]  AS nimi, k.[Name 2] AS nimi2 AS klient " +
			      " FROM [Vičiunai Baltic$Customer] k  where k.[No_] = '" + cuid.trim() + "'";
			
			Statement st1 = con.createStatement();
			ResultSet rs1 = st1.executeQuery(sql1);
			
			 if (rs1.next()) {
				 String name = rs1.getString("nimi").trim();
		         String name2 = rs1.getString("nimi2");
		         if(name2 != null){
		        	 name = (name + " " + name2.trim()).trim();
		         }
		         res = name;
		         
			 }
				rs1.close();
				st1.close();
	  }catch(Exception e){
		  e.printStackTrace();
	  }
		
			
	
	  return res;
  }
  
  private String getNimi(String id) {
		 
	  String res = id;
	  try{

		    String sql1 = " SELECT [Search Description] AS nimi FROM [Vičiunai Baltic$Item] " +
					"  WHERE [No_] = '" + id + "' "; // and [Blocked]=0 
			
			Statement st1 = con.createStatement();
			ResultSet rs1 = st1.executeQuery(sql1);
			
			 if (rs1.next()) {
				 String name = rs1.getString("nimi").trim();
		         res = name;
		     }
				rs1.close();
				st1.close();
	  }catch(Exception e){
		  e.printStackTrace();
	  }
		
			
	
	  return res;
  }
  
  private Vector splitOrder (Order order , boolean split){
	  Vector result = new Vector();
	  if(split){//ainult maxima tellimuse puhul lõhutakse ära 
		  
	 
	  if (order.getDokNr() == null || order.getDokNr().trim().length() == 0){
		  order.setDokNr(Calendar.getInstance().getTime() + "");
	  }
	  Order ordJahe = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ordKylm = new Order(order.getCompany(),order.getAgentFirstName());
	     
	  ordJahe.setAgentCode(order.getAgentCode());
	  ordJahe.setDokNr(order.getDokNr());
	  ordJahe.setCompName(order.getShipMode());
	  ordJahe.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordJahe.setStatus(order.getStatus());
	  
	  System.out.println("filiaal " + order.getCompName() + order.getShipMode() );
	   

	  ordKylm.setAgentCode(order.getAgentCode());
	  ordKylm.setDokNr(order.getDokNr());
	  ordKylm.setCompName(order.getShipMode());
	  ordKylm.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordKylm.setStatus(order.getStatus());

	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
		  
		     String itemid = lin.getToode();
		     try{
		    	 
    boolean kylm =  false;
		    	 String sql =" select [Pogrupio Kodas] AS kood FROM [Vičiunai Baltic$Item]  where "
		    	 		+ "[Unit Price]>0 and  [No_] ='" + itemid + "'";
		    	  Statement st1 = con.createStatement();
		    	    ResultSet rs1 = st1.executeQuery(sql);
		    	    if(rs1.next()){
		    	    	if(rs1.getString("kood").trim().startsWith("1")){
		    	    		kylm = true;
		    	    	}
		    	    }
		    	 
		    	 
		     if(!kylm){
		     	ordJahe.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		     	ordJahe.setDokNr(order.getDokNr() + ".1");
		     	ordJahe.setReference(order.getReference() + ".1");
		     //	System.out.println("ordJahe " + lin.getToode());
		     }else {
		     	ordKylm.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		     	ordKylm.setDokNr(order.getDokNr() + ".22");
		     	ordKylm.setReference(order.getReference() + ".22");
		     //	System.out.println("ordKylm " + lin.getToode());
		     	
		     }
		     
		     }catch(Exception e){
		    	 e.printStackTrace();
		     }
		  
	  }
	  
	  if(ordJahe.getOrderLines() != null && ordJahe.getOrderLines().size()>0){
		  result.add(ordJahe);  
	  }
	  if(ordKylm.getOrderLines() != null && ordKylm.getOrderLines().size()>0){
		  result.add(ordKylm);  
	  }
        }else{
		result.add(order);  
	  }
	  
	  
	 System.out.println("Result =" +order.getDokNr()
			 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }

  public void makeOrder(Order order) throws SQLException{

     counter = 0;
     boolean split = false;
     
     if(order.getStatus()==98){ //kui on maxima tellimus siis splitime /
    	 split = true;
     }
  boolean goes = true;
  
  if(order.getCompName() != null && ((order.getCompName().equals("recadv") ||
		  order.getCompName().equals("RECADV")))){
	  goes = false;
	  setNumAddedLines(order.getOrderLines().size()); 
  }

  if(goes){
 	 Vector splitted =  splitOrder(order,split);
 	  Enumeration en = splitted.elements();
 	  while(en.hasMoreElements()){
 		  Order ord = (Order)en.nextElement();
 		  makeOutput(ord);
 	  } 
  }
    
  }
  
  private void makeOutput(Order order){
	  
		 String aletext = null;
	 	StringBuffer sb = new StringBuffer();
	 	try { 	    
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
		
			
			
			 String comment = order.getInfo();
		      String comm2 = comment;
		          
		          
		          if(comment == null || comment.trim().length() == 0 ){
		         	comment = "";
		         	comm2= "";
		          }else{
		         	if(comment.trim().length()>70){
		         		comm2 = comment;	
		         	}
		         	}
		          
		         if(order.isShipDateUsed()) {
		        	 comm2 = comm2 + "<br> Tarnekuupäev valitud : " + Converter.getDDpMMpYYYYpString(order.getShipCal());
		         }
		          
	        
	         
	         String sep = ",";
	         String lineSep = System.getProperty("line.separator");
	         sb.append("tellimus" + lineSep);

	         sb.append("tellija.regnr:" + sep  + ""+ lineSep);
	         sb.append("tellija_kood:" + sep  + order.getCompany() + lineSep );
	         sb.append("filiaal:" + sep  + lineSep);
	         sb.append("aasta:" + sep + order.getShipCal().get(Calendar.YEAR) + lineSep);
	         sb.append("nr:" + sep + order.getDokNr()  + lineSep);
	         sb.append("tarnija.regnr:" + sep + lineSep);
	         sb.append("kuupaev:" + sep + getDateString(order.getShipCal()) + lineSep);
	         sb.append("tarne_kuupaev:" + sep + getDateString(order.getShipCal()) + lineSep);
	         sb.append("tarne_aadress:" + sep + lineSep);
	         sb.append("koostaja.eesnimi:" + sep + "agentname" + lineSep);
	         sb.append("koostaja.perenimi:" + sep + "agentname"  + lineSep);
	       	 sb.append("koostaja.kood:"  + sep +"" + lineSep);	         
	         sb.append("koostaja.email:" + sep + ""  + lineSep);   
	         sb.append("markused:" + sep + "|" +order.getDokNr() + lineSep);
	         String rrr = " ";
	         if(order.getDokNr() != null &&order.getDokNr().trim().length()!= 0){
	        	 rrr = order.getDokNr();
	        	 if(rrr.length() > 10){
	        		 rrr = rrr.substring(0,10);
	        	 }
	         }
	         sb.append("reference:" + sep + rrr + lineSep);
	       
	        
	         sb.append("read" + lineSep);
	       	 sb.append("toote_kood,nimetus,yhik,kogus,km,yhiku_hind,ean_kood,summa,ale" + lineSep); 
	        
	         
	    Vector lines = order.getOrderLines();
	      for (int i = 0; i < lines.size(); i++) {
	        OrderLine ol = (OrderLine) lines.elementAt(i);
	    	  sb.append(ol.getToode() + sep);
	          sb.append(ol.getTooteNimi().replace(',',' ') + sep);
	          sb.append(ol.getUnit() + sep);
	          String unit = ol.getUnit();
	          double kogus = ol.getKogus();
	          if(unit != null && unit.equals("DPA")){ 
	       	   System.out.println(unit + " " + ol.getToode() + " enne " + kogus );
	       	   if(ol.getBonus()> 1){
	       		   kogus = kogus * ol.getBonus();
	       		   System.out.println(unit + " " + ol.getToode() + " bonus " + kogus );
	       	        }
	       	      }
	          
	          sb.append(kogus + sep);
	          sb.append(sep);
	          sb.append("0" + sep);
	          String ean = "";
	          if(ean == null || ean.trim().length()==0){
	          	ean = "";
	          	}
	          sb.append(ean);
	          
	          sb.append( sep +  "0" + sep);

	          double disc = ol.getDiscount();
	          if (disc < 0){
	            disc = 0;
	          }
	          if(disc > 0) {
	              String nimi = getNimi(ol.getToode());
	              if(nimi == null){
	             	 nimi = ol.getTooteNimi();
	              }
	          
	          int ddd = (int)(disc);   
	          
	     		 if(aletext == null) {
	     			aletext = ddd + "%  [" + kogus + "] "+ ol.getToode() + " " + nimi + "\n" ;
	     		 }else {
	     			aletext = aletext + ddd + "%  [" + kogus + "] "+ ol.getToode() + " " + nimi + "\n";
	     		 }
	          }

	          sb.append(("0"));
	          sb.append(lineSep);
	 
	       counter++;
	      }
	
	      
	      setNumAddedLines(counter); 
	      
	    String doknr = order.getDokNr();
	      String fn = doknr;
	      if(doknr.trim().length()== 0){
	    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
	      }
	    
	      boolean makeout = true;
	      if(order.getStatus() == 60 && !order.getCompName().toLowerCase().equals("order")){
	    	  makeout = false;
	      }
	      System.out.println("makeout " + makeout);		
	     
	    	if(makeout){
		    	  String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + fn.replace('/','-').replace('\\','-')  +  ".csv";
			      System.out.println(fileName);		        
			      FileOutputStream fos = new FileOutputStream(fileName);
			        fos.write(sb.toString().getBytes());
			        fos.close();
	    		
	    	}

	    	if(order.getStatus() < 5) {
	    		  if(comm2 == null) {
	    			   comm2 = "Tellimus";
	    		   }
	    		if((comm2 != null || aletext != null ) ){
	    			   Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(order.getAgent());
	    			 
	    		   Converter.sendCommentEmail( comm2,aletext,doknr,order.getCompany(),  " " + getCustName(order.getCompany()) , " ",false,ag,parent,null);
	    		 }
	    	}
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
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

 
  public String getDriverVersion(){
    return "VichiESTSQL";
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


  private String getDateString(Calendar cal){
    StringBuffer sb = new StringBuffer();
    sb.append(cal.get(Calendar.YEAR));
    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
    if (month.length() == 1){
      sb.append("0");
    }
    sb.append(month);

    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    if (day.length() == 1){
      sb.append("0");
    }
    sb.append(day);

    return sb.toString();
  }
  
  private String url = "jdbc:odbc:naviest";
  private Connection con;
  private int counter = 0;
  private ImportPanel parent;
  private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
  
}
