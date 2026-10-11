/*
f  * Created on 31.07.2006
 */
package eevaeksport.imp.driver;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */

 import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;


 public class NaviKALImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

   public NaviKALImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
     //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
       try {
		Class.forName(databaseDriverName).newInstance();
	} catch (InstantiationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IllegalAccessException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   
   }

   HashMap custcodes = new HashMap();
   
   
   public String identifyCustomer(String id,String regnr)throws SQLException {
    String custid = null;
    Connection con = DriverManager.getConnection(url + "S1" , "", "");
    String cmd = "SELECT kl.code FROM grcompany kl WHERE U_ImpordiFiliaal = '" + id + "'";   
    PreparedStatement st2 = con.prepareStatement(cmd);
    ResultSet rs = st2.executeQuery();
 
    while (rs.next()){
    	custid =  rs.getString("code");
     }
    
    rs.close();
    st2.close();
    
    if(custid == null){
    	   String cmd2 = "SELECT kl.code FROM grcompany kl WHERE  id ='" + id + "'";   
    	    PreparedStatement st22 = con.prepareStatement(cmd2);
    	    ResultSet rs2 = st22.executeQuery();	 
    	    while (rs2.next()){
    	    	custid =  rs2.getString("code");
    	     }
    	    rs2.close();
    	    st22.close();
    }
    con.close();  
   	return custid;
   }

   public int getSalesid() throws SQLException{
     String cmd = "SELECT MAX(Nr)+1 AS id from [Orders] where Merisoft= 1  "; 
     Connection con = DriverManager.getConnection(url + "S1" , "", "");
     
     PreparedStatement st2 = con.prepareStatement(cmd);
     ResultSet rs = st2.executeQuery();
     int result = 0 ;
     while (rs.next()){
     result =  rs.getInt("id");
      }
     rs.close();
     st2.close();
     con.close();
     if(result  < 7000000){
    	 result = 7000001;
     }
     return result;
   }

   public String identifyProduct(String ean, String prodID) throws SQLException{
    return prodID;
   }

   public void makeOrder(Order order) throws SQLException{
     setOrder(order);
     System.out.println("out1");
     String salesid = this.getSalesid() + "";

     System.out.println("out2");
     addInvoiceHeader(salesid);
     
     System.out.println("out3");
     addInvoiceLines(salesid);
     

     
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
       double ale = orderLine.getDiscount();
    
       
    
       String agent = order.getAgent();
         int alex = 0;
         if(ale == -2){
        	alex = 1; 
         }
     
         String	sql = "INSERT INTO [Products] ([Krepselio Nr],[Eilutes Nr], [Prietaiso Nr],[Prekes Nr]," +
         		"    [Kiekis],[Dovana],[Nuolaidos proc.],[Kaina],[Suma],[Suma su PVM],[Data])" +
             " VALUES( " +
               salesid + "," +
               counter + ",'MS-" +
               agent + "',?," +
               kogus + "," +
               alex +"," + ale + ",0,0,0,getdate())";
System.out.println(sql);
               log.info("SALES_LINE_SQL: " + sql);
               Connection con = DriverManager.getConnection(url + "S1" , "", "");
               
               PreparedStatement st2 = con.prepareStatement(sql);
               st2.setString(1,   Converter.deCode(itemid).trim() );
               String aiai = parent.getProps().getProperty("testing");
               if(aiai != null && aiai.equals("true")){
            	   
               }else{
            	   st2.executeUpdate();   
               }
              
               setAmount(false);
               addNumLine();
               st2.close();
               
               String upd = "UPDATE [ORDERS] set [Importuota] = 0 where [Nr] = " + salesid;
               st2 = con.prepareStatement(upd);
               st2.executeUpdate();
               st2.close();
               con.close();
               
               System.out.println("Imported "+ salesid);
               
               
           
        }
   //  System.exit(0);
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
		


   private void addInvoiceHeader(String salesid) throws SQLException{
   	

   	
   	String agent = order.getAgent();
    
   	String custid = (order.getCompany());
   	System.out.println("enne " + custid );
     custid =   (String) this.custcodes.get(custid);
     if(custid == null) {
    	 custid = (order.getCompany());
     }
     System.out.println("parast " + custid );
   //	 System.exit(0);
     
   	System.out.println("agent " + agent);

   	
   
    String tekst = "";

    if (order.getEmailBody()!= null){
      // tekst = order.getEmailBody().replaceAll("\n"," ");
     }
          
    
    if(order.getInfo() != null && order.getInfo().trim().length()>0 ){
    	tekst = order.getInfo().trim().replaceAll("'", " ");
    }

    if(tekst.length() > 250){
    	tekst = tekst.substring(0, 250);
    }

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
	        if(order.getStatus() == 3){
	        	status = "3";
	        }
	        
	        if(order.getStatus() == 2){
	        	status = "2"; //return
	        }
	        if(order.getStatus() == 6){
	        	status = "6";
	        }
		
		
		
		
		System.out.println("Dealer "   + " " + order.getDealer());
		
		String smode = "";
		if(order.getShipMode() != null && order.getShipMode().trim().length()>0){
			smode = order.getShipMode();
		}

		
		
        String  sql = "INSERT INTO [Orders] ([Nr],[Prietaiso Nr],[Pirkejo Nr],[Registravimo data],[Merisoft]," +
        		" [Nuoroda],[External Document No.],[Sandelys],[Document Type],[Suma],[Suma su PVM],[Uzsakymo Nr],"
        		+ " [Uzsakymo sukurimo data],[Salesperson Code] ,[ShipMode], [Importuota]) " + //,[SalesPersoncode] [Uzsakymo sukurimo laikas]
                " VALUES ( " + salesid + ",'MS-" + agent + "','" + custid + "',cast(? as date),1,'"+ tekst+"','" +  // cast(? as date)
              ref + "','" + order.getDealer()  + "'," + status + ",0,0 ,'',cast(getdate() as date),'"+ agent + "','" + smode + "',1)"; //,'" + order.getAgent() + "'

     log.info("addInvoiceHeader2: " + sql);
     System.out.println("addInvoiceHeader2: " + sql);
     Connection con = DriverManager.getConnection(url + "S1" , "", "");
     
         PreparedStatement stmt2 = con.prepareStatement(sql);
         stmt2.setTimestamp(1, new java.sql.Timestamp(cal.getTimeInMillis()));
         String aiai = parent.getProps().getProperty("testing");
         if(aiai != null && aiai.equals("true")){
      	   
         }else{
        	 try{
        	 	 stmt2.executeUpdate();   
        	        		 
        	 }catch(Exception k){
        		   sql = "INSERT INTO [Orders] ([Nr],[Prietaiso Nr],[Pirkejo Nr],[Registravimo data],[Merisoft]," +
        	        		" [Nuoroda],[External Document No.],[Sandelys],[Document Type],[Suma],[Suma su PVM],[Uzsakymo Nr],"
        	        		+ " [Uzsakymo sukurimo data],[Salesperson Code] ,[ShipMode], [Importuota]) " + //,[SalesPersoncode]
        	                " VALUES ( " + salesid + ",'MS-" + agent + "','" + custid + "',cast(? as date),1,'"+ tekst+"','" +  // cast(? as date)
        	              ref + "','" + order.getDealer()  + "'," + status + ",0,0 ,'',cast(getdate() as date),'"+ agent + "','" + smode + "',1)"; //,'" + order.getAgent() + "'

        		 stmt2 = con.prepareStatement(sql);
                 stmt2.setTimestamp(1, new java.sql.Timestamp(cal.getTimeInMillis()));
                 stmt2.executeUpdate();
        	 }
         }
         stmt2.close();
         con.close();
         addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
   }


   public String getDriverVersion(){
     return "NaviKal";
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
   
   private boolean isAmount = false;

public String getTrouble() {
	return null;
}
  
 }

