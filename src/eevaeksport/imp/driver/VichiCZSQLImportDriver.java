/*
' * Created on 31.07.2006
 */
package eevaeksport.imp.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;


 public class VichiCZSQLImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

   public VichiCZSQLImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
        }  catch (Exception e) {
       parent.showError(e);
     }
     loadSignatures();
   }
   
 	private Vector signatures = new Vector();

 	private void loadSignatures(){
 		
 try{		
 		File f = new File("signatures.vec");
 		Calendar ss = Calendar.getInstance();
 		
 		if (f.exists() && f.lastModified() < ss.getTimeInMillis() - 3600000) {// kui on yle tunni vana siis maha 
 			f.delete();
 			signatures = new Vector();
 		} else {
 			signatures = (Vector) readObject("signatures.vec");
 		}

 		
 	} catch (Exception e) {
 		signatures = new Vector();
 	}

 	}

 	public static void saveObject(Object obj, String file) throws IOException {
 		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
 		oos.writeObject(obj);
 		oos.close();
 	}
 	
   
 		
 	   public static Object readObject(String s) throws IOException,
      ClassNotFoundException, IOException {
    Object obj = null;
    File f = new File(s);
    if (f.exists()){
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(s));
      obj = ois.readObject();
      ois.close();
    }
    return obj;
  }



   
   
   public String identifyCustomer(String id,String regnr)throws SQLException {
   	return null;
   }
   
   public String identifyProduct(String ean, String prodID) throws SQLException{
	      return null;
	   }

   public int getSalesid() throws SQLException{
     String cmd = " SELECT MAX([Entry ID])+1 AS id from [Viciunai CZ$EDI Header]  ";  
     Connection con2 = DriverManager.getConnection(url, parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));
     PreparedStatement st2 = con2.prepareStatement(cmd);
     ResultSet rs = st2.executeQuery();
     int result = 0 ;
     while (rs.next()){
     result =  rs.getInt("id");
      }
     rs.close();
     st2.close();
     con2.close();
     return result;
   }

   private StringBuffer troubles= new StringBuffer();


   public String getTrouble() {
   	// TODO Auto-generated method stub
   	return troubles.toString();
   }
   
   public void makeOrder(Order order) throws SQLException{
     setOrder(order);

     if (signatures == null){
  	   signatures = new Vector();
     }
     
     
     String signature = order.getSignature();
	  if(signatures.contains(signature)){
		  this.troubles.append( "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany()) ;
		    		  
	  }else{
		    String salesid = this.getSalesid() + "";
		    try {
		    	  addInvoiceHeader(salesid);
		    }catch(Exception e) {
		    	e.printStackTrace();
		    	log.error(e);
		    	System.exit(0);
		    }
		    
		    // addInvoiceHeader(salesid);
		     addInvoiceLines(salesid);
		  
		  	  
	  }

	   try {
			saveObject(signatures, "signatures.vec");
		} catch (IOException e) {
			e.printStackTrace();
		}
	
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
       double ale = 0;
       
       if(orderLine.getDiscount() > 0) {
    	   ale = orderLine.getDiscount();
       }
       double price = 0;
       
       if(orderLine.getPrice() > 0) {
    	   price = orderLine.getPrice();
       }
       
    
       
  
         String	sql = "INSERT INTO [Viciunai CZ$EDI Lines] ([Entry ID]  ,[Line No_]  ,[Document Type], [EAN]  ,[Cust_ Item No_],[Ordered Qty_] ,[UOM]"
         		+ "  ,[Order Unit Net Price] ,[Lot No_]  ,[Valid Date]  ,[Discount])" +
             " VALUES( "
             + " " +
               salesid + "," +
               counter + ",0,'','" +
               Converter.deCode(itemid) + "'," +
               kogus + ",'" + orderLine.getUnit() +"'," + price + " ,'',CAST(? AS date)" +
                "," + ale + ")";
         
               System.out.println(sql);
               
               log.info("SALES_LINE_SQL: " + sql);
               Connection con2 = DriverManager.getConnection(url, parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));
               PreparedStatement st2 = con2.prepareStatement(sql);
               st2.setTimestamp(1, new java.sql.Timestamp(0));
                st2.executeUpdate();   
             
               setAmount(false);
               addNumLine();
               st2.close();
               con2.close();
           
        }
   //  System.exit(0);
     }
   
   public static String getYYYYpMMpDD(long dd){
	    StringBuffer sb = new StringBuffer();
	    Calendar cal= Calendar.getInstance();
	    cal.setTimeInMillis(dd);
	 
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}

		sb.append("" + cal.get(Calendar.YEAR));
		sb.append("-" + month  + "-");
		sb.append(day );
	    
	    return sb.toString();
	  }


   private boolean isBeforeOneDay(Calendar cal , Calendar ship){
		  
		  Calendar cc = Calendar.getInstance();
		  cc.setTimeInMillis(cal.getTimeInMillis());
		  cc.set(Calendar.HOUR, 2);
		  cc.set(Calendar.MINUTE, 1);
		  cc.set(Calendar.SECOND, 1);
		  cc.set(Calendar.MILLISECOND, 1);
		  cc.add(Calendar.DAY_OF_YEAR, 1);
		  
		  Calendar ss = Calendar.getInstance();
		  ss.setTimeInMillis(ship.getTimeInMillis());
		  ss.set(Calendar.HOUR, 1);
		  ss.set(Calendar.MINUTE, 1);
		  ss.set(Calendar.SECOND, 1);
		  ss.set(Calendar.MILLISECOND, 1);
		  
		  if(cc.getTime().before(ss.getTime())){
			  return true;
		  }
		  
		  return false;
	  }
		
private String getILN(String cuid) {
	
String res = "";
   
   String sql =  "SELECT  adr.[EDI ILN] AS lisa  FROM [Viciunai CZ$Customer] k  \r\n" + 
   		"   					  RIGHT JOIN [Viciunai CZ$Ship-to Address] adr ON adr.[Customer No_]  = k.[No_]  \r\n" + 
   		"   					    WHERE k.[No_] +'_'+ adr.[Code] = '" + cuid + "'" ;

	   System.out.println(sql);
	   try {
		   Connection con2 = DriverManager.getConnection(url, parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));
	        Statement stmt = con2.createStatement();
	        ResultSet rs = stmt.executeQuery(sql);
	        while(rs.next()){
	         res = rs.getString("lisa").trim();
	        }
	       
	        rs.close();
	        stmt.close();
	        con2.close();
	       
	   } catch(Exception e) {
	        e.printStackTrace();	
        }
   return res ;
}

   private void addInvoiceHeader(String salesid) throws SQLException{
   	

   	
   	String agent = order.getAgent();
   	String custid = (order.getCompany());
   	System.out.println("agent " + agent);

   	
   
    String tekst = "";

    if (order.getEmailBody()!= null){
      // tekst = order.getEmailBody().replaceAll("\n"," ");
     }
          
    
    if(order.getInfo() != null && order.getInfo().trim().length()>0 ){
    	tekst = order.getInfo().trim();
    }

    if(tekst.length() > 250){
    	tekst = tekst.substring(0, 250);
    }
    String unicode = tekst;
    /*try {
		 unicode = new String(tekst.getBytes(), "cp1251");
	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	}*/

		String reference  = order.getTeostaja();
		
		String ref = "";
		if ( reference != null && reference.trim().length() > 4 ){
		  ref = reference.trim().toUpperCase();
		}
		
	     Calendar cal = Calendar.getInstance();
	     boolean change = false;
	     Calendar  shipDate = order.getShipCal();
         if(shipDate != null && cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY &&
         		shipDate.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY && 
         		shipDate.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY){
        	 change = true;
         }
         
         if(shipDate != null && isBeforeOneDay( cal,shipDate ) ){
         	 change = true;         	
          }
	        
	          //if(change){
	        	  cal = shipDate;
	          //}
	        
	        String iln = getILN(custid);
		String custid1= custid;
		String custid2 = "";
	   int ind = custid.indexOf("_");
	   
		if(ind != -1 ) {
			custid1 = custid.substring(0,ind);
			custid2 = custid.substring(ind+1);
		}
		
        String  sql = "INSERT INTO [Viciunai CZ$EDI Header] ( [Entry ID]  ,[Document Type] ,[Order No_]  ,[Order Date]  ,"
        		+ "[EDD] ,[Sender ILN]  ,[Receiver ILN]  ,[Buyer ILN] ,[Seller ILN]  ,[Delivery ILN]  ,[Document No_] "
        		+ ",[External Document No_] ,[Document Function Code] ,[Exported],[Processed] ,[Export Date_Time]  ,[Import Date_Time]  ,[Order Currency]"
        		+ " ,[Your Reference] ,[Invoice No_]  ,[Comment]  ,[CustomerCode] ,[SalesManager]) " +
                " VALUES ( " + salesid + ",0,'" + order.getDokNr() + "',CAST(getdate() AS date),"
                		+ " CAST(? AS date) ,'','','123456789','','" + iln + "','','','O',0,0,getdate(),getdate(),'','','','"+ unicode +"','" + custid1 + "','" + agent  + "')";

     log.info("addInvoiceHeader2: " + sql);
     System.out.println("addInvoiceHeader2: " + sql);
     Connection con2 = DriverManager.getConnection(url, parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));
         PreparedStatement stmt2 = con2.prepareStatement(sql);
        stmt2.setTimestamp(1, new java.sql.Timestamp(cal.getTimeInMillis()));
    
        	 stmt2.executeUpdate();   
        
         stmt2.close();
         con2.close();
         addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
     //  System.exit(0);
   }


   public String getDriverVersion(){
     return "ViciCZSQLImport";
   }

   public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
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

   private String url = "jdbc:odbc:navicz";
   
 
   private ImportPanel parent;
   private Order order = null;
 
   private boolean isAmount = false;


  
 }

