
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


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
import java.util.Vector;

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

public class AxaLitAgraImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public AxaLitAgraImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url, "mobile","AivAr.314");
      con2 = DriverManager.getConnection(url, "mobile","AivAr.314");
      con3 = DriverManager.getConnection(url, "mobile","AivAr.314");
      con4 = DriverManager.getConnection(url, "mobile","AivAr.314");
   
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  private Vector getWares(Order ord ){
	  Vector res = new Vector();
	  
	  Vector lines = ord.getOrderLines();
	  Enumeration enumr = lines.elements();
	 
	  
	  while  (enumr.hasMoreElements()){
		  OrderLine ol = (OrderLine)enumr.nextElement();
		  String ware = ol.getDealer();
		  if(ware == null || ware.trim().length() == 0){
			  if(!res.contains("NODEALER")){
				  res.add("NODEALER");  
			  }
		  }else{
			  if(!res.contains(ware)){
				  res.add(ware);  
			  }
		  }
	  }
	  
	 System.out.println("getWQare " + res .toString());
	  return res ;
  }
  
  private Vector splitOrder (Order order){
	  
	  Vector result = new Vector();
      
	  Vector wares = getWares(order);
	  for (int i = 0; i<wares.size();i++){
		  String dealer = (String)wares.elementAt(i);
		  Order ordBio = new Order(order.getCompany(),order.getAgentFirstName());
		  ordBio.setAgentCode(order.getAgentCode());
		  ordBio.setDokNr(order.getDokNr());
		  ordBio.setInfo(order.getInfo());
		  ordBio.setTeostaja((order.getTeostaja()));
		  ordBio.setChanged(order.isChanged());
		  ordBio.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordBio.setStatus(order.getStatus());
		  ordBio.setShipTime(order.getShipTime());
		  ordBio.setShipMode(order.getShipMode());
		  ordBio.setPaymTerm(order.getPaymTerm());
		  ordBio.setDealer(dealer);
		
		
		  Vector lines = order.getOrderLines();
		  Enumeration enumr = lines.elements();
		
		  System.out.println("sees diiler " +  dealer);
		  while  (enumr.hasMoreElements()){
			  OrderLine lin = (OrderLine)enumr.nextElement();
			
			     String dea = lin.getDealer();
			     if(dea == null || dea.trim().length() == 0){
			    	 dea = "NODEALER";
			     }
		  if(dea.equals(dealer)){
			    ordBio.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),dea,null,null);	 
	  
		  }
	
		  }


		  if(ordBio.getOrderLines() != null && ordBio.getOrderLines().size()>0){
			  result.add(ordBio);  
		  }

	  }
	  
	 System.out.println("Result =" +order.getDokNr()
			 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }




  

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	
    return null;

  }

  
  public String getTerm(int num ){
	 String res = "";
	  	
	  String   sql1 = "select kood from (select (ROW_NUMBER() OVER (ORDER BY paymtermid))+1 AS Row ,paymtermid as kood  , " +
           		       "   description  as nimi from MJ_getPaymTerm where dataareaid='ltb'   ) d where row = " + num + "  ";	
	
 System.out.println(sql1);
 
try{
	 Statement st1 = con.createStatement();
	 ResultSet rs1 = st1.executeQuery(sql1);

	 while (rs1.next()) {
	   res = rs1.getString("kood").trim() ;

	 }
	 rs1.close();
	 st1.close();

}catch(Exception e){

}
	 
	 return res;
  }
  
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
    return prodID;
  }
  
  
  private Order order = null;
  private void setOrder(Order order){
	    this.order = order;
	  }

  
  public void makeOrder(Order order) throws SQLException{
	  

	   setOrder(order);
	 
	   Vector splitted =  splitOrder(order);	   
         Enumeration en = splitted.elements();
         
         if("true".equals(parent.getProps().getProperty("testing"))){
        	 while(en.hasMoreElements()){
   			  Order ord = (Order)en.nextElement();
   			   System.out.println("dealer "  + ord.getDealer() + " " + ord.getOrderLines().size());
   		  } 
          }else{
        	  while(en.hasMoreElements()){
    			  Order ord = (Order)en.nextElement();
    			   String salesid =  (Calendar.getInstance().getTimeInMillis()+"").substring(3) ;
    			    addInvoiceHeader(salesid , ord);
    			    addInvoiceLines(salesid ,ord);
    			   String sql4 = "update mj_orderheader set ISREADYFORAXA=1 where dataareaid='110'   ";
    			    PreparedStatement st2 = con2.prepareStatement(sql4);
    			  // st2.setString(1, salesid.trim());
    			    st2.executeUpdate();
    		  }   
          }
		   
  }

  
  
  private void addInvoiceLines(String salesid,Order ord) throws SQLException{

	    Enumeration enumr = ord.getOrderLines().elements();
	    int counter = 0;
	    while (enumr.hasMoreElements()){
	      counter++;
	      log.info("ORDER_LINE_COUNTER:" + counter);
	      OrderLine orderLine = (OrderLine)enumr.nextElement();
	      String itemid = orderLine.getToode();
	      double kogus = orderLine.getKogus();

	      
	      String sql0 = "SELECT itemid, nimetus,unit  FROM MJ_getProducts where  itemid ='" + itemid + "' and dataareaid='110'   ";
	      
	    log.info("SALES_LINE_SQL: " + sql0);
	    System.out.println(sql0);
	    Statement st = con4.createStatement();
	    ResultSet rs = st.executeQuery(sql0);
	   boolean olemas = false;
	    String unit = " ";
	    String name = "";
	    while(rs.next()){
	       name = rs.getString("nimetus"); 
	    	unit = rs.getString("unit");

	      if (unit !=null && unit.trim().length()>0){
	      	unit = unit.trim();
	      	olemas = true;
	      }else {
	      	unit = " ";
	      }
	    }
	  if(orderLine.getUnit().trim().length()>0){
	    unit = orderLine.getUnit();
	  }
	  System.out.println("olemas " + olemas);
	  if(!olemas){
	  	itemid = orderLine.getTooteNimi();
	  	name = orderLine.getEAN() + " " + orderLine.getTooteNimi();
	  	if(itemid.length()>20){
	  		itemid = itemid.substring(0,19);
	  	}
	  }
	          
   double price = 0;
   double discount = 0;
   if(orderLine.getDiscount() != 0){
	   price = orderLine.getPrice();
   }
	          
	          
	          String sql =" INSERT into  mj_orderlines (DOCID, LINENUM,PRODID,PRODNAME, QUANTITY, UNIT ,PRICE,DISCOUNT,DATAAREAID )  " +
	   " values('"+salesid+"'," + counter + ",'"+itemid+"','" + name + "',"+kogus+",'" + unit + "'," + price +"," + discount+",'110')";

	              log.info("SALES_LINE_SQL: " + sql);
	              System.out.println(sql);
	              PreparedStatement st2 = con2.prepareStatement(sql);
	              st2.executeUpdate();
	              addNumLine();
	              st2.close();

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


  
  private void addInvoiceHeader(String salesid, Order ord ) throws SQLException{

    

         String agent = ord.getAgent();
         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);

         if(ag == null){
         	ag = new Agent(agent);
         }

     String tekst = ord.getInfo();
     String mode = ord.getShipMode();
     if(mode == null){
    	 mode = "";
     }
      String ref = "";

      if(ord.getReference() != null && ord.getReference().trim().length()>0 ){
      	ref = ord.getReference().trim();
      }

      String custname = "";
      if( ord.getCompName() != null){
    	  custname = ord.getCompName();
      }
      
      if (ord.getShipTime()!= null){
         custname  =  ord.getShipTime().replaceAll("\n"," ") + " " + getYYYYpMMpDDpTimeNiceString(ord.getShipCal()) + " " + custname;
        }
      String cust =  ord.getCompany();
      String aadrID = "";
      if(cust.indexOf("_") != -1){
    	  cust = cust.substring(0,ord.getCompany().indexOf("_"));
    	  aadrID = ord.getCompany().substring(ord.getCompany().indexOf("_")+1);
      }
      
 String dea = ord.getDealer();
 if(dea.equals("NODEALER")){
	 dea = "";
	 }
 
 String term = getTerm(ord.getPaymTerm());
      
      System.out.println(salesid + "|"+ ord.getCompany() +"|"+ ag.getID()+ "|"+ tekst);
     String sql = " INSERT INTO mj_orderheader (  " +
            " DOCID,DOCNUMBER,REFERENCE,CUSTID,SALESPERSON,WAREHOUSE,COMMENT,ORDERDATE,DELIVERYDATE,ISREADYFORAXA, DATAAREAID,addressid,shipway,paymterm) " +
            " VALUES ('"+salesid+"','" + ord.getDokNr() + "','" + ref + "','"
            + cust+"','"+ag.getID()+"','" + dea +"','"+tekst+"',?,?,0,'110','" + aadrID + "','" + mode + "','" + term + "')" ;
        log.info("addInvoiceHeader2: " + sql);
        System.out.println(sql);
        
        // escape(cust.substring(0,cust.indexOf("-")))
        PreparedStatement stmt2 = con2.prepareStatement(sql);
        java.util.Date dat = new java.util.Date();
       // System.out.println(dat.toString() + "  sc " + order.getShipCal().getTime().toString() + " ref " + ref);
            dat.setTime(ord.getShipCal().getTimeInMillis());
        Calendar cc = Calendar.getInstance();
        stmt2.setTimestamp(1, new java.sql.Timestamp(cc.getTimeInMillis()));
        stmt2.setTimestamp(2, new java.sql.Timestamp(dat.getTime()));
        stmt2.executeUpdate();
        stmt2.close();
    
        addLog("lisatud arve p�is -> " + ord.getCompany() + "\n");
  }



  
  private void addLog(String line){
	    parent.addLog(line);
	  }
 	  


 
	public NumberFormat getNF() {
		return this.nf;
	}
	



 
  public String getDriverVersion(){
    return "AxaLitAgraImport";
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

  public static final String cleanLitLatEst(String ret){
	    if (ret != null){
	      
	        ret = ret.replaceAll("�","e");
	        ret = ret.replace((char) 0x3f,' ');
	        
	        
	       // ret = ret.replaceAll("?","s");
	       // ret = ret.replaceAll("?","c");
	     // ret = ret.replace('',' ');
	        ret = ret.replaceAll("�","I");
	        ret = ret.replaceAll("�","u");
	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","z");
	        ret = ret.replaceAll("�","s");
	        ret = ret.replaceAll("�","S");
	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","I");
	        ret = ret.replaceAll("�","u");
	        ret = ret.replaceAll("�","i");
	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","Z");
	        ret = ret.replaceAll("�","E");
	        ret = ret.replaceAll("�","U");
	        ret = ret.replaceAll("�","U");
	        ret = ret.replaceAll("�","Pg.");
	        
	        ret = ret.replaceAll("�","Z");
	        ret = ret.replaceAll("�","A");
	        ret = ret.replaceAll("�","a");
	        ret = ret.replaceAll("�","C");

	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","E");
	        ret = ret.replaceAll("�","e");
	        ret = ret.replaceAll("�","G");
	        ret = ret.replaceAll("�","g");
	        ret = ret.replaceAll("�","I");
	        ret = ret.replaceAll("�","i");
	        ret = ret.replaceAll("�","K");
	        ret = ret.replaceAll("�","k");
	        ret = ret.replaceAll("�","L");
	        ret = ret.replaceAll("�","l");
	        ret = ret.replaceAll("�","N");
	        ret = ret.replaceAll("�","n");
	        
	        ret = ret.replaceAll("�","o");
	        ret = ret.replaceAll("�","a");
	        ret = ret.replaceAll("�","o");
	        ret = ret.replaceAll("�","u");
	       
	        ret = ret.replaceAll("�","O");
	        ret = ret.replaceAll("�","A");
	        ret = ret.replaceAll("�","O");
	        ret = ret.replaceAll("�","U");
	    }
	        
	        if(ret == null || ret.trim().length()==0){
	        return " ";
	        }else {
	        	System.out.println(ret);
	        return ret;
	        }        
	      }
  
  private String url = "jdbc:odbc:AXA";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private Connection con2;
  private Connection con4;
  private Connection con3;
  private ImportPanel parent;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
