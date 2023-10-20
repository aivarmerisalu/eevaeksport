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

public class AxaKosmelitaImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public AxaKosmelitaImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    nf.setGroupingUsed(false);
  
    
    try {
      Class.forName(databaseDriverName).newInstance();
      
     
    } catch (Exception e){
       parent.showError(e);
    }
  }


  public String getImportFile(){
   return parent.getProps().getProperty("imp.read");
 }

  public String identifyCustomer(String id,String regnr) throws SQLException {
   return id;

  }

  

  public String identifyProduct(String ean, String prodID) throws SQLException{
   return prodID;
  }
  
 

 private  String salesid = null;

  private StringBuffer trouble = new StringBuffer();

  public String getTrouble() {
		// TODO Auto-generated method stub
		return trouble.toString();
	}
  
  public void makeOrder(Order order) throws SQLException{
	  boolean senddata = true;
	  
	  trouble = new StringBuffer();
	  if(topeltkontroll && order.getStatus() > 3){
		 
	  }
	  if(senddata){
		  setOrder(order);
		  //OrderHeaders
		//  OrderId     OrderAccount                                       SalesTaker                                         OrderDate               DeliveryDate            Notes                                                                                                Reference                                          Ready 
		   
           salesid = order.getDokNr();
		    addInvoiceHeader(salesid);
		    System.out.println("salesid " + salesid);
		 
	
		    addInvoiceLines(salesid);
		    String sql4 = "update  OrderHeaders set Ready=1 where OrderId = " + salesid;
		    Connection con = DriverManager.getConnection(url , parent.getProps().getProperty("dsn.userim"), parent.getProps().getProperty("dsn.passim"));
	        PreparedStatement st2 = con.prepareStatement(sql4);
		    st2.executeUpdate();
		    st2.close();
		    con.close();
		  //  System.exit(0); 
	  }
  
  //  makeOutput(order);
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
        double discount = 0;
       if(orderLine.getDiscount() > 0){
    	   discount = orderLine.getDiscount();
       }
       System.out.println("Discount "  + discount + " status " + order.getStatus() );
    
       double price =  orderLine.getPrice();
      
   
          
    	  //OrderLines  
  		//    OrderId   ItemId   Quantity    UnitId    Price   Discount 
          

  		  
          String sql =" INSERT into  OrderLines (OrderId, ItemID, Quantity, UnitId, Price, Discount )  " +
             " values("+salesid+","+itemid+","+kogus+",'" + unit + " '," + price + "," + discount+ ")";

              log.info("SALES_LINE_SQL: " + sql);
              System.out.println(sql);
              Connection con = DriverManager.getConnection(url , parent.getProps().getProperty("dsn.userim"), parent.getProps().getProperty("dsn.passim"));
              
              PreparedStatement st2 = con.prepareStatement(sql);
           
           
              st2.executeUpdate();
              addNumLine();
              st2.close();

              }
  }

  private void addInvoiceHeader(String salesid) throws SQLException{

         String agent = order.getAgent();
         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);

         if(ag == null){
         	ag = new Agent(agent);
         }
  
      
      String ref = " ";
      if(order.getInfo() != null && order.getInfo().trim().length()>0 ){
      	ref = order.getInfo().trim();
      }

      if(order.getReference() != null && order.getReference().trim().length()>0 ){
      	ref = order.getReference().trim();
      }      
     
   
       if(ref.length()>50){
     	  ref = ref.substring(0,50);
       }
  
      String comp =  order.getCompany();
     
       
      String com = "";
      if(order.getInfo() != null && order.getInfo().trim().length() >0){
      		com =  order.getInfo() ;
      	}
      
      if(com.length() > 30){
    	  com = com.substring(0,29);
      }
      
         Connection con = DriverManager.getConnection(url , parent.getProps().getProperty("dsn.userim"), parent.getProps().getProperty("dsn.passim"));
     
     String sql = " INSERT INTO OrderHeaders (  " +
            " OrderId, OrderAccount, SalesTaker, OrderDate, DeliveryDate, Notes, Reference, Ready, Activity ) " +
           " VALUES (" + salesid + " ,'" + comp +"','" + agent +"',cast(getdate() as date),cast(? as date),'" + ref + "','"
            + com.replace('\'', ' ').replace('\"', ' ') + "',0,'OLD')" ;
          log.info("addInvoiceHeader2: " + sql);
        System.out.println(sql);
        
        PreparedStatement stmt2 = con.prepareStatement(sql);
        java.util.Date dat = new java.util.Date();
       // System.out.println(dat.toString() + "  sc " + order.getShipCal().getTime().toString() + " ref " + ref);
            dat.setTime(order.getShipCal().getTimeInMillis());
        
        stmt2.setTimestamp(1, new java.sql.Timestamp(dat.getTime()));
      
        stmt2.executeUpdate();
        stmt2.close();
        con.close();
    
        addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
       // System.exit(0);
  }




  public String getDriverVersion(){
    return "KosmelitaImport";
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

  private String url = "jdbc:odbc:kosmelita";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);


  private ImportPanel parent;
  private Order order = null;
  private boolean isAmount = false;
  private int counter = 0;
  private boolean topeltkontroll = false;


}
