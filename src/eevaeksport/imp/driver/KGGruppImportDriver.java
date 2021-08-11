
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.Product;
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

public class KGGruppImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(KGGruppImportDriver.class.getName());

  public KGGruppImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");


    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
   //   con = DriverManager.getConnection(url, "mobile","mobile");

      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
      shipaddr = readCustAddr();
      nf.setGroupingUsed(false);
      nf.setMinimumFractionDigits(4);
      nf.setMaximumFractionDigits(4);
      
    } catch (Exception e) {
      parent.showError(e);
      log.error(e);
    }
  }
  private TreeMap prods = new TreeMap();
  private Hashtable<String,String> shipaddr = null;

  	   
  	   
  private String getDir(){
    String dir = parent.getProps().getProperty("readin");
    if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
    return dir;
  }
  
  
  
  private HashMap payers = new HashMap();
  private TreeMap custnames = new TreeMap();
  
 
  public String identifyCustomer(String id,String regnr) throws SQLException {
  	
  	 System.out.println("Kliendi Määramine: "  + id + " " + regnr );
  	     String acc = null;
     Iterator itera = custnames.keySet().iterator();
     while (itera.hasNext()){
    	 String name = (String)itera.next();
    	 if(name.endsWith(id)){
    		 acc = (String)custnames.get(name);
    		 System.out.println("Klient leitud : " + name + " " + id );
    	 }
     }


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
 
  
  
  public String getMTKood(int paevad) throws SQLException{
 	 
    String sql1 = " select SL22002 as kood from SL220200 where SL22003 =0 and SL22005 =  " + paevad + "";
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
 
  TreeMap eanprods = new TreeMap();
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
   String prod = null;
     System.out.println("Toode  : " + ean + " " + prodID + " " + eanprods.size() );
     Iterator itera = eanprods.keySet().iterator();
     while (itera.hasNext()){
    	 String eansees = (String)itera.next();
    	 if(eansees.equals(ean)){
    		 prod = (String)eanprods.get(eansees);
    		 System.out.println("Toode leitud : " + eansees + " " + prod );
    	 }
     }

    return prod;
  }


  
  public void makeOrder(Order order) throws SQLException{

	  counter = 0;
	   trouble = "Imoprdin tellimust " + order.getAgent() + "." + order.getDokNr() + ".txt aga SAP faili ei tekkinud ";
		
	 Order ord =  order;	
	 try{
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
	 }catch(Exception r ){
		 log.error(r);
	 }
		
   		           ord.setTeostaja(order.getTeostaja());
     			   makeOutput(ord);
	    	   
      trouble = null;
      setNumAddedLines(counter);
  }

  private String getNimi(String aa) throws SQLException{
  	String res = "noname";
   if(prods.get(aa) != null){
	   res = ((Product)prods.get(aa)).getName();
   }
  	return res ;
  }
  
  private String getEan(String aa) throws SQLException{
	  	String res = "0000000000000";
	  	 if(prods.get(aa) != null){
	  	   res = ((Product)prods.get(aa)).getEAN();
	     }
	  	return res ;
	  }
  
  public String identfySorting (String prodID){
	  	


	    String sql1 = " Select SC01015 + SC01001 AS sort FROM SC010200 pr  where pr.SC01001='" + prodID + "'";
	  log.error("1_LINE_SQL: " + sql1);

	    String sort = "000";

	    Statement st1;
		try {
			st1 = con.createStatement();
		
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	sort = rs1.getString("sort");
	    //	System.out.println("sort käes 1 ;" + sort );
	    	if(sort == null || sort.trim().length() == 0){
	    		sort = "xxx";
	    	}
	    }

	    rs1.close();
	    st1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
// System.out.println("Sort käes 2 ;" + sort );
	    return sort;
	  }
  
	  private String getDateString(Calendar cal){
		    StringBuffer sb = new StringBuffer();
		    sb.append((cal.get(Calendar.YEAR)+"-"));
		    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
		    if (month.length() == 1){
		      sb.append("0");
		    }
		    sb.append(month + "-");

		    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		    if (day.length() == 1){
		      sb.append("0");
		    }
		    sb.append(day + " ");
		    
		    sb.append((cal.get(Calendar.HOUR_OF_DAY)+":"));
		    String min = String.valueOf(cal.get(Calendar.MINUTE));
		    if (min.length() == 1){
		      sb.append("0");
		    }
		    sb.append(min + ":");
		    String sek = String.valueOf(cal.get(Calendar.SECOND));
		    if (sek.length() == 1){
		      sb.append("0");
		    }
		    sb.append(sek);
	//2013-08-23 13:55:34
		    return sb.toString();
		  }
	
	  private Hashtable<String,String>  readCustAddr(){
   	   Hashtable<String,String> hh = new Hashtable<String,String>();

   	     try {
   	       FileReader fr = new FileReader(getDir() + "CustomerAddress_ID.csv");
   	       BufferedReader reader = new BufferedReader(fr);
   	     String line = null;
   	     reader.readLine();
   	      while((line = reader.readLine()) != null ){

   	           line = line.replaceAll(";"," |");
   	      	StringTokenizer st = new StringTokenizer(line, "|");
   	      	System.out.println(line);
//Contact_ID;Customer_ID;ContactName;ContactName2;ContactName3;Contact_Mobile;
 	//ContactEmail;ContactPhone;ContactFax;ContactNote;ContactN
   	        if (st.hasMoreTokens()) {
   	        
   	           st.nextToken().trim(); 
   	          String addrid = st.nextToken().trim();
   	          String name = st.nextToken().trim(); 
   	          String name2 = st.nextToken().trim(); 
   	         
   	        	  hh.put(addrid, name+";"+name2);
   	            }
   	          }
   	       
   	       reader.close();
   	       fr.close();

   	     } catch (Exception e){
   	       e.printStackTrace();
   	     }
   	     System.out.println(hh.toString());
   	     
   	   return hh ;
      }
      

  private String getTimeString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	   
	    return sb.toString();
	  }
	  
  
 private void makeOutput(Order order){
 	StringBuffer sb = new StringBuffer();
 	 nf.setGroupingUsed(false);


 	try {
 	  int cou = 0;
		String ref = order.getReference();
		if ( ref == null ){
			ref =  " ";
		}
		String com = order.getInfo();
		if ( com == null ){
			com =  " ";
		}		
	
		 String doknr = order.getDokNr();
       

         String ls = System.getProperty("line.separator");
         String fixagent = order.getAgent();       
     
       
         String ladu = null;
             Calendar  shipDate = order.getShipCal();
             
             String code = order.getWarehouse();        
             if (code != null){
               ladu = code;
             }
           
           if (ladu == null) {
             ladu = "";
           }
  
           String agent = order.getAgent();
           
           if(fixagent != null){
         	  agent = fixagent;
           }
    
           Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
           if(ag != null){
          	int aa = ag.getName().indexOf(" ");
        	  agent = ag.getName().substring(0,aa).trim();
          }
         agent = agent.toLowerCase();
         agent = Converter.cleanLitLatEst(agent);
           Calendar cal = Calendar.getInstance();
           if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
        	   cal.add(Calendar.DAY_OF_YEAR, 3);  
           }else{
        	   cal.add(Calendar.DAY_OF_YEAR, 1);  
           }
           
          
           if (shipDate != null) {
                 cal = shipDate;
               }
         
           sb.append("OrderExt_ID;Order_ID;IsTemplate;Active;Customer_ID;Wholesaler_ID;User_ID;OrderType;DateCreated;DeliveryDate;OrderCusID;OrderTotal;OrderTotalD;OrderTotalR;Order_CL1;Order_CL2;Order_CL3;Order_CL4;Order_CL5;Order_CL6;OrderTReserved;OrderLReserved;OrderNReserved;OrderHeaderNote;Row_ID;Product_ID;OrderedQTY;OrderUnit;UnitPrice;VATRate;VATSum;RowTotal;RowTotalR;ClassNumber;OrderRowType;ReservedTxt;ReservedNumber;ReservedInteger;ReservedCL1;ReservedCL2;" +
           		"ReservedCL3;ReservedCL4;ReservedCL5;ReservedCL6;" +
           		"BatchID;OrderRowNote;PriceUnit;OrderedQTYInEaches;RecAmount;" +
           		"RecVAT;RecStatus;Discount;ShipToAddress1;ShipToAddress2;ShipToCity;ShipToZip;ShipToDistrict;" +
           		"ShipToState;ShipToCountry" + ls);

  String klient = order.getCompany();
  String saaja = "";
  String shipto =";";
  if(klient.indexOf("-") != -1){
	  saaja = klient.substring(klient.indexOf("-")+1);
	  klient = klient.substring(0,klient.indexOf("-"));
	  if(shipaddr.get(saaja) != null){
		  shipto = shipaddr.get(saaja);  
	  }	  
  }

        
           Vector sortedLines = order.getOrderLines();
           for (int i = 0; i < sortedLines.size(); i++) {
               OrderLine line = (OrderLine)sortedLines.elementAt(i);
               double ale = line.getDiscount();     
               counter++;
               String ean = line.getEAN();
               String yhik = line.getUnit();
               if (ale == -1 ){
                   ale = 0 ;
                   }
               
               
               if( ean==null || ean.length() < 6 ) {
                   ean = getEan(line.getToode());
                  }
               double price = line.getPrice();
               if(line.getDiscount() > 0){
            	   price = price /((100-line.getDiscount())/100);
               }
             
               
               // OrderExt_ID;Order_ID;IsTemplate;Active;Customer_ID;Wholesaler_ID;User_ID;OrderType;
                //DateCreated;DeliveryDate;OrderCusID;OrderTotal;OrderTotalD;OrderTotalR;Order_CL1;Order_CL2;
                //Order_CL3;Order_CL4;Order_CL5;Order_CL6;OrderTReserved;OrderLReserved;OrderNReserved;
                //OrderHeaderNote;Row_ID;Product_ID;OrderedQTY;OrderUnit;UnitPrice;VATRate;VATSum;RowTotal;
                //RowTotalR;ClassNumber;OrderRowType;ReservedTxt;ReservedNumber;ReservedInteger;ReservedCL1;
                //ReservedCL2;ReservedCL3;ReservedCL4;ReservedCL5;ReservedCL6;BatchID;OrderRowNote;PriceUnit;
                //OrderedQTYInEaches;RecAmount;RecVAT;RecStatus;Discount;ShipToAddress1;ShipToAddress2;ShipToCity;
                //ShipToZip;ShipToDistrict;ShipToState;ShipToCountry
                
               // ;1203691;0;1;LJUVIGA0;@1MALF;5MDAP_MAR;Vizitas;2013-08-23 13:14:27;2013-08-26 00:00:00;;
                //896000.0000;1084160.0000;0.0000;ITR;FAKT1;;;;;2013-09-30;;;;1;mmCHEF50_M;11.000;vnt;
                //0.0000;21.00;140910.0000;671000.0000;0.0000;;TANN;;;;;;;;;;;;T;550.000;671.000000000;
                //140.910000000;1;"!	1	Manual discount	1220.000	61000.0000
                //";AKADEMIJOS miestelis PILĖNŲ g.1, KAUNAS,;AKADEMIJOS miestelis PILĖNŲ g.;KAUNAS;;;;LT
     
               
               
               //OrderExt_ID;Order_ID;IsTemplate;Active;Customer_ID;Wholesaler_ID;User_ID;OrderType;
               //DateCreated;DeliveryDate;OrderCusID;OrderTotal;OrderTotalD;OrderTotalR;Order_CL1;
               //Order_CL2;Order_CL3;Order_CL4;Order_CL5;Order_CL6;OrderTReserved;OrderLReserved;
               //OrderNReserved;OrderHeaderNote;Row_ID;Product_ID;OrderedQTY;OrderUnit;UnitPrice;
               //VATRate;VATSum;RowTotal;RowTotalR;ClassNumber;OrderRowType;ReservedTxt;ReservedNumber;
               //ReservedInteger;ReservedCL1;ReservedCL2;ReservedCL3;ReservedCL4;ReservedCL5;ReservedCL6;
               //BatchID;OrderRowNote;OrderedQTYInEaches

        //;1517963;0;1;RIMON_;@VP;KLAI;;2013-08-23 13:55:34;2013-08-24 00:00:00;;385.1750;422.4350;421.5000
               //;;;;;;;;;;;1;5727341;2.000;VNT;4.4000;0.0000.00;0;8.0000;8.8000;;TAN;;;;;;;;;;;;2.000      
               
               String whole = "@VP";
               String user = order.getAgent();;
               String orderTotal = getNF().format(order.getSum()) + "";
               String orderTotalD = getNF().format(order.getSum()) + "";
               String orderTotalR = getNF().format(order.getSum()) + "";
               String linesum = getNF().format((price * line.getKogus())) + "";
               String orderRowType = "TAN";
             	
                       
               System.out.println("klent ja saaja " + klient + " " + saaja  + " " + shipto);
             		     sb.append(";"+ doknr + ";0;1;" + klient + ";" + whole + ";" + user +";;"+
             		     getDateString(Calendar.getInstance()) + ";" + getDateString(cal) + ";;" +
             		     orderTotal +";"+ orderTotalD +";"+ orderTotalR +";;;;;;;;;;;"+ (i+1) + ";" + line.getToode()
             		     + ";" + getNF().format(line.getKogus()) + ";" + yhik +";"+ getNF().format(price) +";0.00;0.000;"
             		     + linesum + ";" + linesum + ";;" + orderRowType + ";;;;;;;;;;;;" + yhik + ";" + getNF().format(line.getKogus()) 
             		     + ";;;1;\"!Manualdiscountsomething\";" + shipto + ";;;;;LT" + ls );
           }
                
             
   counter = counter + cou;
    	//
      String fn = doknr;
      if(doknr.trim().length()== 0){
    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
      }
   
      String fileName = (parent.getProps().getProperty("conv.dir")  + agent)
      + "_"+ fn + "_" + this.getDateString(Calendar.getInstance()).replaceAll(" ", "_").replaceAll(":", "_")  +  ".csv";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sb.toString().getBytes());
        fos.close();
        File f = new File(fileName);
        if(f.exists()){
        	log.error(doknr  + " tellimus failis " + fileName);
        }else{
        	log.error(doknr  + " tellimust POLE failis " + fileName);
        }
       
	} catch (Exception e) {
		e.printStackTrace();
		
	log.error(e);
	}
 }
 
 
	public NumberFormat getNF() {
		return this.nf;
	}
	
	
 
 
  public String getDriverVersion(){
    return "KGGruppImport";
  }

  
  public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  
  private NumberFormat calNF = NumberFormat.getInstance();


  
  private String url = "jdbc:odbc:scalaDB";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private int counter = 0;
  private ImportPanel parent;
  private String trouble = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return trouble;
}
}
