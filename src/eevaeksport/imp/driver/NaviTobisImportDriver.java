/*
f  * Created on 31.07.2006
 */
package eevaeksport.imp.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */

 import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;


 public class NaviTobisImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

   public NaviTobisImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
     //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
     
       con2 = DriverManager.getConnection(url + "NAV40"
            , "", "");
       con3 = DriverManager.getConnection(url + "NAV40"
            , "", "");
       con4 = DriverManager.getConnection(url + "NAV40"
            , "", "");

       con6 = DriverManager.getConnection(url + "NAV40"
            , "", "");

     
             
     String sql =  "  SELECT k.[No_] AS kood " +
	 " FROM [2015_TOBIS$Customer] k " +
	 " WHERE k.[Salesperson Code] != ''  AND Blocked != 3 " ;

System.out.println(sql);


     Statement stmt = con2.createStatement();
     ResultSet rs = stmt.executeQuery(sql);
     while(rs.next()){
    		String kood = rs.getString("kood").trim(); 
    		String asi = Converter.enCodeEst(kood);
    		custcodes.put(asi, kood);
     }
     rs.close();
     stmt.close();
    
     }
     catch (Exception e) {
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
  

   HashMap custcodes = new HashMap();
   
   
   public String identifyCustomer(String id,String regnr)throws SQLException {
    String custid = null;
    
    String cmd = "SELECT kl.code FROM grcompany kl WHERE U_ImpordiFiliaal = '" + id + "'";   
    PreparedStatement st2 = con4.prepareStatement(cmd);
    ResultSet rs = st2.executeQuery();
 
    while (rs.next()){
    	custid =  rs.getString("code");
     }
    
    rs.close();
    st2.close();
    
    if(custid == null){
    	   String cmd2 = "SELECT kl.code FROM grcompany kl WHERE  id ='" + id + "'";   
    	    PreparedStatement st22 = con4.prepareStatement(cmd2);
    	    ResultSet rs2 = st22.executeQuery();	 
    	    while (rs2.next()){
    	    	custid =  rs2.getString("code");
    	     }
    	    rs2.close();
    	    st22.close();
    }
    
   	return custid;
   }

   public int getSalesid() throws SQLException{
     String cmd = "SELECT MAX(Nr)+1 AS id from [2015_Tobis$MOFF Uzsakymu krepselis] where Merisoft= 1  ";   
     PreparedStatement st2 = con2.prepareStatement(cmd);
     ResultSet rs = st2.executeQuery();
     int result = 0 ;
     while (rs.next()){
     result =  rs.getInt("id");
      }
     rs.close();
     st2.close();
     if(result  < 7000000){
    	 result = 7000001;
     }
    
     return result;
   }

   public String identifyProduct(String ean, String prodID) throws SQLException{

  	String sql2 = "select pr.code as toode  FROM grarticle pr inner join grbarcode bc " +
  			" on bc.idgrarticle = pr.id where bc.code ='" + ean + "'";


    String sql1 = "Select pr.code AS toode FROM grarticle pr " + 
          " where pr.code='" + prodID + "'";
    log.info("1_LINE_SQL: " + sql1);
    log.info("2_LINE_SQL: " + sql2);

    String prod = null;
Connection ccc = DriverManager.getConnection(url + "saf"
        , "mobile", "mobile");
    Statement st1 = ccc.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
    
    rs1.close();
    st1.close();
//System.out.println("prod0" + prod);
    //rs1.close();
   // st1.close();
    
    if(prod == null || prod.trim().length() == 0){
      st1 = ccc.createStatement();
      rs1 = st1.executeQuery(sql2);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
      }
    }

    //System.out.println("prod1" + prod);
    rs1.close();
    st1.close();
   ccc.close();
    return prod;
   }
   
   private String trouble = null;
   public String getTrouble() {
		// TODO Auto-generated method stub
		return trouble;
	}

   public void makeOrder(Order order) throws SQLException{
	   
	   if (signatures == null){
    	   signatures = new Vector();
       }
	   
	   
	   String signature = order.getSignature();
		  if(signatures.contains(signature)){
			  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
			    		  
		  }else{
			   
			     setOrder(order);
			     System.out.println("out1");
			     String salesid = this.getSalesid() + "";

			     System.out.println("out2");
			     addInvoiceHeader(salesid);

			     System.out.println("out3");
			     addInvoiceLines(salesid);
			     signatures.add(signature);
				  this.trouble = null;			  
			  
		  }
		  
		  
		     try {
		 		saveObject(signatures, "signatures.vec");
		 	} catch (IOException e) {
		 		e.printStackTrace();
		 	}

   }

   
   public static void saveObject(Object obj, String file) throws IOException {
 		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
 		oos.writeObject(obj);
 		oos.close();
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
    
       String ccc = "";
       if(orderLine.selActCode != null && orderLine.selActCode.trim().length() > 0) {
    	   ccc = orderLine.selActCode;
       }
    
       String agent = order.getAgent();
         int alex = 0;
         if(ale == -2){
        	alex = 1; 
         }
     
         String	sql = "INSERT INTO [2015_Tobis$MOFF Prekiu krepselio eilute] ([Krepselio Nr],[Eilutes Nr], [Prietaiso Nr],[Prekes Nr]," +
         		"    [Kiekis],[Dovana],[Nuolaidos proc_],[Unit Of Measure Code],[Ret_ From ILE No_],[End Customer Discount %],"
         		+ "[End Customer price],[Uzs_Eil_Nr_],[KS prekes Nr],[Suma],[Suma su PVM],[Aprasas],[Kaina],[Campaign No],"
         		+ "[Item_code2],[Item_code15],[Item_code5],[Item_code3],[Item_code16],[can_be_equal],[RetFromInvoiceNo],[RetFromInvoiceDate],[Unrecognized])" +
             " VALUES( " +
               salesid + "," +
               counter + ",'MS-" +
               agent + "',?," +
               kogus + "," +
               alex +"," + ale + ",'',0,0,0,0,'',0,0,'',0,'" + ccc + "','','','','','',0,'',getdate(),0)";
System.out.println(sql);
               log.info("SALES_LINE_SQL: " + sql);
               PreparedStatement st2 = con3.prepareStatement(sql);
               st2.setString(1,   Converter.deCode(itemid).trim() );
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

   

  private String returnMode (Order ord){
	  String res = "2";
		     Enumeration enumr = order.getOrderLines().elements();
	  
	     while (enumr.hasMoreElements()){
	    	  OrderLine orderLine = (OrderLine)enumr.nextElement();
		       double kogus = orderLine.getKogus();
		       if(kogus > 0){
		    	   return "0";
		       }
	     }
	 
	  return res ;
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
    	tekst = order.getInfo().trim();
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
	        	status = returnMode(order); //return
	        }
	        
	        if(order.getStatus() == 6){
	        	status = "6";
	        }
		String ladu = "CS";
		
		String smode = "";
		if(order.getShipMode() != null && order.getShipMode().trim().length()>0){
			smode = order.getShipMode();
		}

        String  sql = "INSERT INTO [2015_Tobis$MOFF Uzsakymu krepselis] ([Nr],[Prietaiso Nr],[Pirkejo Nr],[Registravimo data],[Merisoft]," +
        		" [Nuoroda],[External Document No_],[Sandelys],[Document Type],[ShipMode],[SalesPersoncode],[Order source],[Order Date],[Shipment Date],[Invoice To Payer],[Suma]"
        		+ " ,[Suma su PVM], [Importuota], [Uzsakymo Nr], [Uzsakymo Sukurimo data],[Uzsakymo Sukurimo laikas],[Naujas],[WEB B2B],[End Cust_ Due Date],[Payment Method Code],"
        		+ " [Prices include VAT],[Your Reference],[Coupon Code],[Coupon Amount],[ShippingCost],[Direct Delivery via Payer],"
        		+ "[Payer No_],[End_Cust_ Paym_Method],[Ship-To Name],[Ship-To Name 2],[Ship-To Address],\r\n" + 
        		"[Ship-To Address 2],[Ship-To City],[Ship-To Post Code],[Ship-To Country],[Ship-To VAT Reg_ No_],\r\n" + 
        		"[Ship-To Phone],[Ship-To e-mail],[Bill-To Name],[Bill-To Name 2],[Bill-To Address],\r\n" + 
        		"[Bill-To Address 2],[Bill-To City],[Bill-To Post Code],[Bill-To Country],[Bill-To Reg_ No_],\r\n" + 
        		"[Bill-To VAT Reg_ No_],[Reply-To email],[Ship-To Info Modified],[Payer Info Modified],[StockMgt Order],[DeliveryDate],[Priority],[Customer No_],[Locker Owner], [Locker Code], [Prices From File]) " +
                " VALUES ( " + salesid + ",'MS-" + agent + "','" + custid + "',cast(? as date),1,'"+ tekst.replaceAll("'", " ")+"','" +  
              ref + "','" + ladu  + "'," + status + ",'" + smode + "','" + order.getAgent() + "','MOB',(CAST('1753-01-01 00:00:00.000' AS DateTime)),"
              		+ "(CAST('1753-01-01 00:00:00.000' AS DateTime)),0,0,0,'','',(CAST('1753-01-01 00:00:00.000' AS DateTime)),(CAST('1753-01-01 00:00:00.000' AS DateTime)),'',0,(CAST('1753-01-01 00:00:00.000' AS DateTime)),'',1,'','',0,0,0,"
              		+ " '','','','','','','','','','','','','','','','','','','','','','',0,0,0,cast(? as date),0,'','','',0)";

     log.info("addInvoiceHeader2: " + sql);
     System.out.println("addInvoiceHeader2: " + sql);
         PreparedStatement stmt2 = con6.prepareStatement(sql);
         stmt2.setTimestamp(1, new java.sql.Timestamp(cal.getTimeInMillis()));
         stmt2.setTimestamp(2, new java.sql.Timestamp(cal.getTimeInMillis()));

         String aiai = parent.getProps().getProperty("testing");
         if(aiai != null && aiai.equals("true")){
      	   
         }else{
        	 stmt2.executeUpdate();   
         }
         stmt2.close();
         addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
     //  System.exit(0);
   }


   public String getDriverVersion(){
     return "NaviTobis";
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
   
   private Connection con6;
   private Connection con2;
   private Connection con3;
   private Connection con4;
   private ImportPanel parent;
   private Order order = null;
 
   private boolean isAmount = false;


  
 }

