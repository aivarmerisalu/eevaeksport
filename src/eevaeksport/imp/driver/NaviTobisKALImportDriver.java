/*
 * Created on 31.07.2006
 */
package eevaeksport.imp.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Enumeration;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;


 public class NaviTobisKALImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

   public NaviTobisKALImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
     //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
     
 

     }
     catch (Exception e) {
       parent.showError(e);
     }
   }

   
   
   public String identifyCustomer(String id,String regnr)throws SQLException {
    String custid = null;
    
   	return custid;
   }

   public int getSalesid(String conn) throws SQLException{
     String cmd = " SELECT MAX(Nr)+1 AS id from [" + conn + "$MOFF Uzsakymu krepselis] where Merisoft= 1  ";  
     Connection con2 = DriverManager.getConnection(url + "TL" , "", "");
     PreparedStatement st2 = con2.prepareStatement(cmd);
     ResultSet rs = st2.executeQuery();
     int result = 0 ;
     while (rs.next()){
     result =  rs.getInt("id");
      }
     rs.close();
     st2.close();
     con2.close();
     if(result  < 7000000){
    	 result = 7000001;
     }
     this.idid = result;
     return result;
   }

   public String identifyProduct(String ean, String prodID) throws SQLException{
      return null;
   }

   public void makeOrder(Order order) throws SQLException{
     setOrder(order);
 	String conn = "TechnoLit";

	if(order.getCompany().startsWith("TK")){
		conn = "Tobis Kaliningrad";
	}
	
	if(order.getCompany().startsWith("AA")){
		conn = "Avto Assortiment";
	}
 	
 	
	  con2 = DriverManager.getConnection(url + "TL" , "", "");
     String salesid = this.getSalesid(conn) + "";
     addInvoiceLines(salesid,conn);
     addInvoiceHeader(salesid,conn);
     con2.close();
  
   }

 private Connection con2 = null;

   private void addInvoiceLines(String salesid , String conn) throws SQLException{

     Enumeration enumr = order.getOrderLines().elements();
 	
     int counter = 0;
     while (enumr.hasMoreElements()){
       counter++;
       log.info("ORDER_LINE_COUNTER:" + counter);
       OrderLine orderLine = (OrderLine)enumr.nextElement();
       String itemid = orderLine.getToode();
       double kogus = orderLine.getKogus();
       double ale = orderLine.getDiscount();
    
       
    
       String agent = order.getAgent();
         int alex = 0;
         if(ale == 100){
        	alex = 1; 
         }
     
         String	sql = "INSERT INTO [" + conn + "$MOFF Prekiu krepselio eilute] ([Krepselio Nr],[Eilutes Nr], [Prietaiso Nr], " +
 " [Prekes Nr],Kiekis,Dovana,[Nuolaidos proc_])" +
             " VALUES( " +
               salesid + "," +
               counter + ",'MS-" +
               agent + "','" +
               Converter.deCode(itemid) + "'," +
               kogus + "," +
               alex +"," + ale + ")";
System.out.println(sql);
               log.info("SALES_LINE_SQL: " + sql);
             
               PreparedStatement st2 = con2.prepareStatement(sql);
               String aiai = parent.getProps().getProperty("testing");
               if(aiai != null && aiai.equals("true")){
            	   
               }else{
            	   st2.executeUpdate();   
               }
              
               setAmount(false);
               addNumLine();
               st2.close();
           
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
		


   private void addInvoiceHeader(String salesid, String conn) throws SQLException{
   	

   	
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
	        
	          if(change){
	        	  cal = shipDate;
	          }
	        String status = "0";
	        if(order.getStatus() == 1){
	        	status = "1";
	        }
	        
	        String date = getYYYYpMMpDD(Calendar.getInstance().getTimeInMillis());
       
		String ladu = "";
		if( custid.startsWith("TK") || custid.startsWith("AA")){
			custid = custid.substring(2);	
		}
		
        String  sql = "INSERT INTO [" + conn + "$MOFF Uzsakymu krepselis] (Nr,[Prietaiso Nr],[Pirkejo Nr],[Registravimo data], " +
" Merisoft, Nuoroda,[External Document No_],Sandelys,[Document Type]) " +
                " VALUES ( " + salesid + ",'MS-" + agent + "','" + custid + "','" +  date + "',1,'"+ unicode+"','" +  
              ref + "','" + ladu  + "'," + status + ")";

     log.info("addInvoiceHeader2: " + sql);
     System.out.println("addInvoiceHeader2: " + sql);
         PreparedStatement stmt2 = con2.prepareStatement(sql);
        // stmt2.setTimestamp(1, new java.sql.Timestamp(cal.getTimeInMillis()));
         String aiai = parent.getProps().getProperty("testing");
         if(aiai != null && aiai.equals("true")){
      	   
         }else{
        	 stmt2.executeUpdate();   
         }
         stmt2.close();
      //   con2.close();
         addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
     //  System.exit(0);
   }


   public String getDriverVersion(){
     return "NaviTobisKALImport";
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

   private String url = "jdbc:odbc:";
   
 
   private ImportPanel parent;
   private Order order = null;
   private int idid = 0;
   private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
  
 }

