package eevaeksport.imp.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
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

public class NaviKPImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public NaviKPImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    nf.setGroupingUsed(false);
    if("true".equals(parent.getProps().getProperty("topeltkontroll"))){
 		topeltkontroll = true;
 	}
    
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      //con.setAutoCommit(false);
      //con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
      con2 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
              , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      con5 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      con3 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      con4 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
              , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      con5 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      //stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }


  public String getImportFile(){
   return parent.getProps().getProperty("imp.read");
 }

  public String identifyCustomer(String id,String regnr) throws SQLException {

    String acc = null;
    if(regnr != null && regnr.trim().length()>0){

    String sql2 = "SELECT Code  AS kood FROM Mobilejava.Customer where ILNCode = '" + id + "' and  (RegNo)= '" + regnr + "'";
    System.out.println(sql2);
    


    Statement st2 = con3.createStatement();
    ResultSet rs2 = st2.executeQuery(sql2);

    while (rs2.next()) {
      acc = rs2.getString("kood").trim();

    }
    rs2.close();
    st2.close();

    }


    if(regnr == null && acc == null){

        String sql2 = " SELECT Code  AS kood FROM Mobilejava.Customer where Code = '" + id + "' ";
        System.out.println(sql2);

        Statement st2 = con3.createStatement();
        ResultSet rs2 = st2.executeQuery(sql2);

        while (rs2.next()) {
          acc = rs2.getString("kood").trim();

        }
        rs2.close();
        st2.close();

        }

    if(regnr == null && acc == null){

        String sql2 = "SELECT Code  AS kood FROM Mobilejava.Customer where ILNCode = '" + id + "' ";
        System.out.println(sql2);

        Statement st2 = con3.createStatement();
        ResultSet rs2 = st2.executeQuery(sql2);

        while (rs2.next()) {
          acc = rs2.getString("kood").trim();

        }
        rs2.close();
        st2.close();

        }
    
  	if (acc == null){

  	String sql1 = " SELECT Code  AS kood FROM Mobilejava.Customer where ILNCode = '" + id + "' ";
    System.out.println(sql1);

    Statement st1 = con3.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim();

    }
    rs1.close();
    st1.close();

  	}
   // System.out.println("Klient: " + acc);
    return acc;

  }

  
  public String getCompName(String comp) throws SQLException{

	    String sql2 = "SELECT Name  AS nimi FROM Mobilejava.Customer   WHERE  Code='" + comp + "' ";

	    String name = null;
	    ResultSet rs1 = null;
	    Statement  st1 = con.createStatement();
	  if(comp != null){
		   
		     rs1 = st1.executeQuery(sql2);
			System.out.println(sql2);
		    while (rs1.next()) {
		      name = rs1.getString("nimi").trim();
		  //	System.out.println("prod1 " + name );
		    } 
	  }

	 //  System.out.println("prod" + name + " " + comp);
	   if(name == null){
	     	name = comp;
	   }
	   
	   if(name == null || name.trim().length() == 0){
		   name = "XXX";
	   }
	    return name;
	  }

  
  
  
  public String getEAN(String prodID) throws SQLException{

	    String sql2 = "SELECT CrossReferenceNo AS toode FROM Mobilejava.Item   WHERE  Code='" + prodID + "' ";

	    String prod = null;
	    ResultSet rs1 = null;
	    Statement  st1 = con.createStatement();
	  if(prodID != null){		   
		     rs1 = st1.executeQuery(sql2);
			//System.out.println(sql2);
		    while (rs1.next()) {
		      prod = rs1.getString("toode").trim();
		  	//System.out.println("prod1 " + prod );
		    } 
	  } 
	  // System.out.println("prod" + prod + " " + prodID);
	   if(prod == null || prod.trim().length() == 0){
		   prod = "XXX";
	   }
	    return prod;
	  }


  public String getUnit(String prodID) throws SQLException{

	    String sql2 = "SELECT BaseUnitMeasure AS unit FROM Mobilejava.Item   WHERE  Code='" + prodID + "' ";

	    String prod = null;
	    ResultSet rs1 = null;
	    Statement  st1 = con.createStatement();
	  if(prodID != null){
		   
		     rs1 = st1.executeQuery(sql2);
			System.out.println(sql2);
		    while (rs1.next()) {
		      prod = rs1.getString("unit").trim();
		  //	System.out.println("prod1 " + prod );
		    } 
	  }

	  // System.out.println("prod" + prod + " " + prodID);
	   if(prod == null){
	   	prod = prodID;
	   }
	   if(prod == null || prod.trim().length() == 0){
		   prod = "XXX";
	   }
	   if(prod.length() > 6){
		   prod = prod.substring(0,5);
	   }
	    return prod;
	  }


  public String identifyProduct(String ean, String prodID) throws SQLException{
	  
	  
	  if(ean != null && ean.startsWith("0")){
		  while(ean.startsWith("0")){
			  ean= ean.substring(1);
		  }
	  }

    String sql2 = "SELECT Code AS toode FROM Mobilejava.Item   WHERE  Code='" + prodID + "' or CrossReferenceNo = '" + ean + "'";
System.out.println(sql2);
    String prod = null;
    ResultSet rs1 = null;
    Statement  st1 = con.createStatement();
    if(prodID != null && ean != null && ean.equals(prodID)){
    	prodID = null;
    }
  if(prodID != null ){
	   
	    rs1 = st1.executeQuery(sql2);
		System.out.println(sql2);
	    while (rs1.next()) {
	      prod = rs1.getString("toode").trim();
	  	 // System.out.println("prod1 " + prod );
	    } 
  }else{
	  
	  
	  sql2 = "SELECT Code AS toode FROM Mobilejava.Item   WHERE   CrossReferenceNo = '" + ean + "'";
	  rs1 = st1.executeQuery(sql2);
		System.out.println(sql2);
	    while (rs1.next()) {
	      prod = rs1.getString("toode").trim();
	  //	  System.out.println("prod1 " + prod );
	    } 
	   
	    rs1.close();
	    st1.close();
  }
 
  if(prod == null){
  	sql2 = "SELECT Code AS toode FROM Mobilejava.Item   WHERE   CrossReferenceNo = '0" + ean + "'";
  	st1 = con.createStatement(); 
  	rs1 = st1.executeQuery(sql2);
		System.out.println(sql2);
	    while (rs1.next()) {
	      prod = rs1.getString("toode").trim();
	  //	  System.out.println("prod1 " + prod );
	    }
	    rs1.close();
	    st1.close();
	  
  }
  if(prod == null){
  	sql2 = "SELECT Code AS toode FROM Mobilejava.Item   WHERE   CrossReferenceNo = '00" + ean + "'";
  	st1 = con.createStatement();  
  	rs1 = st1.executeQuery(sql2);
		System.out.println(sql2);
	    while (rs1.next()) {
	      prod = rs1.getString("toode").trim();
	  //	  System.out.println("prod1 " + prod );
	    }
	    rs1.close();
	    st1.close();    
  }
  

   System.out.println("prod xxx " + prod +" "+ ean + " " + prodID);
   if(prod == null){
   	prod = prodID;
   }
   if(prod == null || prod.trim().length() == 0){
	   prod = "XXX";
   }
    return prod;
  }
  
  private boolean checkTopelt(Order order){
	  String num = order.getDokNr();
	  String lineSep = System.getProperty("line.separator");
	  
	  if(hasRepreatNums(num)){
		return true;  
	  }else{
		    writeConsloStr( "repreatnums.txt",num + "," + Calendar.getInstance().getTimeInMillis() + lineSep);  
	  }
	    
	
  
	  return false;
  }

  public boolean hasRepreatNums(String ordnum) {

	    File f = new File( "repreatnums.txt");
	    if (f.exists()){
	    try{

		      InputStreamReader is = new FileReader(f);
		      BufferedReader br = new BufferedReader(is);
		      String line = null;
		      while( ( line = br.readLine()) != null){
		      	String id = null;
		      
		        StringTokenizer st = new StringTokenizer(line, ",");
		        if (st.hasMoreTokens()){
		        	id = st.nextToken();
		           if(id.equals(ordnum)){
		        	   return true;
		           }
		        }
		    }
		      is.close();
		      br.close();
		      
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    }
	    return false;
	  }
  
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

  private StringBuffer trouble = new StringBuffer();

  public String getTrouble() {
		// TODO Auto-generated method stub
		return trouble.toString();
	}
  
  public void makeOrder(Order order) throws SQLException{
	  boolean senddata = true;
	  
	  trouble = new StringBuffer();
	  if(topeltkontroll && order.getStatus() > 3){
		  if(checkTopelt(order)){
			//  trouble.append("Tellimuse number Kordub " + order.getDokNr() + " , tellimust ei Imporditud Navisse-se");
			  senddata = false;
		  }
	  }
	  if(senddata){
		  setOrder(order);
		    addInvoiceHeader();
		    String salesid = getAndUpdateRecID() + "";
		    addInvoiceLines(salesid);
		    String sql4 = "update  Mobilejava.SalesHeader set ReadyForImport=1 where SalesNo = " + salesid;
		   
		    PreparedStatement st2 = con5.prepareStatement(sql4);
		    st2.executeUpdate();
		    st2.close();	  
	  }
  
  //  makeOutput(order);
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

  
  
 

  private void addInvoiceLines(String salesid) throws SQLException{

    Enumeration enumr = order.getOrderLines().elements();
    int counter = 0;
    while (enumr.hasMoreElements()){
      counter++;
      log.info("ORDER_LINE_COUNTER:" + counter);
      OrderLine orderLine = (OrderLine)enumr.nextElement();
      String itemid = orderLine.getToode();
      double kogus = orderLine.getKogus();

     String unit = orderLine.getUnit();
     if(unit == null || unit.trim().length() == 0){
    	 unit = getUnit(itemid);
     }
     if(unit != null ){
    	 unit = unit.toUpperCase();
     }
     
     if(unit != null && unit.equals("KAST") && order.getStatus() == 80){ 
  	  // System.out.println(unit + " " + itemid + " enne " + kogus );
  	   if(orderLine.getBonus()> 1){
  		   kogus = kogus * orderLine.getBonus();
  		   //System.out.println(unit + " " + itemid + " bonus " + kogus );
  	          }else{
  		   //System.out.println(unit + " " + itemid + " getinCases" + kogus );
  	         }  	   
  	            
            }
     unit = getUnit(itemid); 	
      String name = orderLine.getTooteNimi();
      if(name != null){
    	  name = name.trim();
      }
      String name2 = "";
      if(name != null && name.trim().length() > 50){
    	  
    	  name2 = name.substring(49);
    	  name = name.substring(0,49);
    	 // System.out.println("a" +name + " " + name.length());
    	  if(name2.length() > 50){
    		  name2 = name2.substring(0,49);
    	  }
    	  
      }
       String ean = orderLine.getEAN();
       if(ean == null || ean.trim().length() == 0 ){
    	   ean = getEAN(itemid);
       }
  
       double discount = 0;
       if(orderLine.getDiscount() > 0 && (order.getStatus() < 3 || order.getStatus() == 96 || order.getStatus() == 59)){
    	   discount = orderLine.getDiscount();
       }
       System.out.println("Discount "  + discount + " status " + order.getStatus() );
       if(order.getStatus() == 60){
    	   discount = 0;
       }
       double price = 0;
       if(order.getStatus() == 59){
    	   price = orderLine.getPrice();
       }
          String agent = order.getAgent();
          Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
          if(ag == null){
         	ag = new Agent(agent);
         }
// Counter
          
          if(kogus < 0) {
        	  kogus = kogus * -1;
          }
          
          String sql =" INSERT into  Mobilejava.SalesLine (SalesNo, ItemCode, Description ,Description2," +
          		"Quantity ,UnitMeasureCode, ReferenceNo,DiscountPrc,VATPrc,CustomerItemCode " +
              " ,NetWeight,GrossWeight,AmountIncVAT ,Amount,UnitPrice,OrderNo ,TransferTime , SourceDocumentNo )  " +
             " values("+salesid+",'"+itemid+"',?,?,"+kogus+",'" + unit + " ','" + ean + "'" +
             		"," + discount + ",0,'',0,0,0,0," + price + ",' ',CAST('1753-01-01' AS DATE),'')";

              log.info("SALES_LINE_SQL: " + sql);
              System.out.println(sql);
              PreparedStatement st2 = con2.prepareStatement(sql);
           //   System.out.println("name st:" + order.getStatus() + " "+ name + " " +  name.length());
           
             // System.out.println("nam2e " + name2 + " " +  name2.length());
              st2.setString(1, name);
              st2.setString(2, name2);
              st2.executeUpdate();
              addNumLine();
              st2.close();

              }
  }

  private void addInvoiceHeader() throws SQLException{

         String agent = order.getAgent();
         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);

         if(ag == null){
         	ag = new Agent(agent);
         }

     String tekst = " ";

      if (order.getEmailBody()!= null){
         tekst = order.getEmailBody().replaceAll("\n"," ");
       }
            
      
      String ref = " ";
      if(order.getInfo() != null && order.getInfo().trim().length()>0 ){
      	ref = order.getInfo().trim();
      }

      if(order.getReference() != null && order.getReference().trim().length()>0 ){
      	ref = order.getReference().trim();
      }      
     
       String dokNr = order.getDokNr();
       
       try{
    	   if(ref.indexOf(dokNr) != -1){
        	   ref  = ref.substring(ref.indexOf(dokNr) + dokNr.length() );
           } 
       }catch(Exception e){
    	   
       }
      
       
       if(ref.length()>50){
     	  ref = ref.substring(0,50);
       }
      
      String type = "0";
   /*  1Telema     
     2 Edisoft
     3      MobileJava
     4 Maxima
     5 muu
     */ 

      if(order.getStatus() < 3){
    	 type = "3"; 
      }
      
      if(order.getStatus() == 80){
     	 type = "2"; 
       }
      
      if(order.getStatus() == 98){
      	 type = "4"; 
        }

      if(order.getStatus() == 60 ){
       	 type = "1"; 
        }
      
      if(order.getStatus() == 59 ){
        	 type = "6"; 
        	 //see v�ib olla ka 6 WEB
          }
      
      if(order.getStatus() == 96 ){
         type = "5"; 
          }
      
      System.out.println(type + " " + order.getCompany() +"|"+ ag.getID()+ "|"+ tekst+"|");
  //    
      String comp =  order.getCompany();
      if(comp == null){
    	  comp = "000";
      }

      String name = order.getCompName();
      if(order.getStatus() == 60 || order.getStatus() == 59 ){
    	  name = order.getShipTime();
      }
      
      if(name == null || name.trim().length() == 0){
    	  name = getCompName(order.getCompany());
      }
      String fag = ag.getID();
      if(fag.equals("PARTP")){
    	  fag = "P�RTP";
      }
      if(dokNr.length() > 20){
    	  dokNr = dokNr.substring(dokNr.length()-19);
      }
      if(name.length() > 50){
    	  name = name.substring(name.length()-49);
      }
       
      String com = "";

      if(order.getInfo() != null && order.getInfo().trim().length() >0){
      		com =  order.getInfo() ;
      	}
      
      if(com.length() > 30){
    	  com = com.substring(0,29);
      }
      
      int doctype = 0;
      try {

          if(((OrderLine)order.getOrderLines().get(0) ).getKogus() <0) {
        	  doctype = 1;
          }
      }catch(Exception e) {
    	  
      }
      
     String sql = " INSERT INTO Mobilejava.SalesHeader (  " +
            "   ImportSource, CustomerCode, OrderDate, ShipDate,ExternalDocumentNo, " +
            " SalespersonCode,  OrderDescription, ReceiveTime, ReadyForImport" +
            ",CurrencyCode, PayerCode, PayerName, ShipToCode, ShipToName," +
            " Amount, AmountIncVAT, TransferTime, OrderNo ,CustomerName,ImportError,Counter,DocumentType,Comment ) " +
           " VALUES (" + type + " ,'" + comp +"',cast(getdate() as date),cast(? as date),'" + dokNr + "'," +
            "'"+fag+"',?,getdate(),0,'','','','','',0,0," +
            		" CAST('1753-01-01' AS DATE),' ','" + name.replaceAll("'", ".") + "',0,0," + doctype + ",'" + com.replace('\'', ' ').replace('\"', ' ') + "')" ;
          log.info("addInvoiceHeader2: " + sql);
        System.out.println(sql);
        
        PreparedStatement stmt2 = con2.prepareStatement(sql);
        java.util.Date dat = new java.util.Date();
       // System.out.println(dat.toString() + "  sc " + order.getShipCal().getTime().toString() + " ref " + ref);
            dat.setTime(order.getShipCal().getTimeInMillis());
        
        stmt2.setTimestamp(1, new java.sql.Timestamp(dat.getTime()));
        stmt2.setString(2, ref);
        stmt2.executeUpdate();
        stmt2.close();
    
        addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
  }



  private long getAndUpdateRecID() throws SQLException{
    long nextRecID = 0;
    String sql = "SELECT max (SalesNo) as ID from Mobilejava.SalesHeader";
    log.info("getAndUpdateRecID(): " + sql);
      Statement st = con4.createStatement();
      ResultSet rs = st.executeQuery(sql);
      while(rs.next()){
        nextRecID = rs.getLong("ID");
      }
      rs.close();
      st.close();
     System.out.println("returning recid " + nextRecID);
    return nextRecID;
  }


  public String getDriverVersion(){
    return "NaviKP";
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

 


  public NumberFormat getNF() {
		return nf;
	}
	/**
	 * @param nf the nf to set
	 */
	public void setNF(NumberFormat nf) {
		this.nf = nf;
	}
  
  /**
  *
  * @param aUseEAN - the new value for useEAN
  */
  public void setAmount(boolean aAmount){
           isAmount= aAmount;
  }

  private String url = "jdbc:odbc:";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

  private Connection con;
  private Connection con2;
  private Connection con3;
  private Connection con4;
  private Connection con5;
  private ImportPanel parent;
  private Order order = null;
  private boolean isAmount = false;
  private int counter = 0;
  private boolean topeltkontroll = false;


}
