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
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.MatrixLine;
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

public class KosmelitaImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public KosmelitaImportDriver(ImportPanel parent) {
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
		    Connection con = DriverManager.getConnection(url , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	        PreparedStatement st2 = con.prepareStatement(sql4);
		    st2.executeUpdate();
		    st2.close();
		    con.close();
		  //  System.exit(0); 
	  }
  
  //  makeOutput(order);
  }

  
 

  private void addInvoiceLines(String salesid) throws SQLException{

	  
	  if(this.sPrices == null) {
		  sPrices = this.getPrices();
	  }
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
       double sprice = -1;
       if (sPrices.get(itemid) != null) {
    	   sprice = sPrices.get(itemid);
       }
       double cprice = getPriceFromMatrix(order.getCompany(),itemid,sprice);
       double mprice = -1;
       if(discount > 0) {
    	   mprice = price;
       }
    	  //OrderLines  
  		//    OrderId   ItemId   Quantity    UnitId    Price   Discount 
          

  		  
          String sql =" INSERT into  OrderLines (OrderId, ItemID, Quantity, UnitId, Price, Discount"
          		+ ", StandardPrice, CustomerPrice, ManualPrice)  " +
             " values("+salesid+","+itemid+","+kogus+",'" + unit + " '," + price + "," + discount+
             "," + sprice + "," +cprice+ "," + mprice+ ")";

              log.info("SALES_LINE_SQL: " + sql);
              System.out.println(sql);
              Connection con = DriverManager.getConnection(url , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
              
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
      
         Connection con = DriverManager.getConnection(url , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
     
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

  
  private HashMap<String , Double>  sPrices = null;
  
  private HashMap<String , Double> getPrices() {
	  HashMap<String , Double> res = new HashMap<String , Double>();
	  String sql = "SELECT inv.itemid,  disc.amount AS price   FROM  inventtable inv inner JOIN  pricedisctable disc ON inv.itemid = disc.itemrelation  and \r\n" + 
	  		"    		     disc.amount != 0  and (disc.todate>getdate()  or   YEAR(disc.todate) =1900)   and disc.currency = 'EUR' and disc.dataareaid='kosm' \r\n" + 
	  		"    		       left JOIN  inventtablemodule mod ON mod.itemid = inv.itemid and inv.DATAAREAID =mod.DATAAREAID   \r\n" + 
	  		"    		   left join DimensionAttributeValueSetItemView ww on ww.DimensionAttributeValueSet = inv.defaultdimension  \r\n" + 
	  		"    				 and  dimensionattribute = 5637145336   WHERE   moduletype=2  and  inv.dataareaid = 'kosm'";
	  try {

      Connection con = DriverManager.getConnection(url2 , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
     Statement stmt = con.createStatement();
     ResultSet rs = stmt.executeQuery(sql);

     while (rs.next()) {
             String itemid = rs.getString("itemid").trim();
             Double price = rs.getDouble("price");
             res.put(itemid, price);
     }
     
       rs.close();
       stmt.close();
       con.close();
	  }catch(Exception e) {
		  e.printStackTrace();
	  }
     
	  return res;
  }

  private double getPriceFromMatrix(String cust, String prodID , double pri) {
          double res = -1;
         String sql = " SELECT (case when  cus.invoiceaccount = '' then cus.accountnum else cus.invoiceaccount end) as custid, (accountnum) as klient,  '' AS clgroup, " + 
				 "  (case when pd.itemcode = 0 then pd.itemrelation else ''end) AS prodid, " +  
				 "  (case when pd.itemcode = 1 then pd.itemrelation else ''end) AS prodgroup,max(percent1) AS discount,max(amount)  " +
                 "  AS price FROM pricedisctable pd " +
                 "   left join custtable cus on cus.accountnum = pd.accountrelation  " +
                 "    and cus.DATAAREAID=pd.DATAAREAID where  " +
				 "  cus.DATAAREAID = 'kosm'   and pd.DATAAREAID = 'kosm'   " +
				 "  and  pd.fromdate<getdate() and pd.todate>getdate()" +
				 "  and  pd.relation in (4,5) and (case when pd.itemcode = 0 then pd.itemrelation else ''end) =' " + prodID + "'" +
				 " and ((case when  cus.invoiceaccount = '' then cus.accountnum else cus.invoiceaccount end) = '" + cust + "'"  +
				 "  group by (case when  cus.invoiceaccount = '' then cus.accountnum else cus.invoiceaccount end) " +
                 "  ,cus.accountnum, pd.itemrelation,pd.itemcode  ";

          try {
System.out.println(sql);
                   Connection con = DriverManager.getConnection(url2 , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
        	      Statement stmt = con.createStatement();
                  ResultSet rs = stmt.executeQuery(sql);

                  while (rs.next()) {

                          double price = rs.getDouble("price");
                          if (price != 0) {
                                  return(price);
                          }
                     }
                    rs.close();
                  
                  
                  String sql2 = "  select c.accountnum AS clgroup,  " +
				  " (case when pd.itemcode = 0 then pd.itemrelation else ''end) AS prodid, " +
				  " (case when pd.itemcode = 1 then pd.itemrelation else ''end) AS prodgroup,(percent1) AS discount, " +
				  "   pd.fromdate as algus , pd.todate as lopp from  " +
				  "  pricedisctable pd  inner join custtable c on c.linedisc =  pd.accountrelation where  c.accountnum = '"+cust+"'"
				  		+ "and pd.fromdate<getdate()+3 and pd.todate>=getdate() and pd.accountcode=1 and percent1 > 0 and pd.dataareaid ='kosm' " +
				  "   order by pd.accountrelation , pd.itemrelation" ;
                  
                  rs = stmt.executeQuery(sql2);

                  while (rs.next()) {

                         
                          double discount = rs.getDouble("discount");
                         System.out.println("disc " + discount);
                       if(discount > 0) {
                    	  // return pri*(100-discount) /100;
                       }
                        
                   
                  }
                  
                  
                  rs.close();
                  stmt.close();
                  con.close();

          } catch (SQLException ex) {
          ex.printStackTrace();
          }
          return res;
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
  private String url2 = "jdbc:odbc:kosmelitaaxa";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);


  private ImportPanel parent;
  private Order order = null;
  private boolean isAmount = false;
  private int counter = 0;
  private boolean topeltkontroll = false;


}
