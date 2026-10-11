package eevaeksport.imp.driver;


import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
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

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;


public class ScalaLFImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(ScalaLFImportDriver.class.getName());

  
  public ScalaLFImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
                                        , parent.getProps().getProperty("dns.user"),
                                        parent.getProps().getProperty("dns.pass"));

      con2 = DriverManager.getConnection(url +
                                         parent.getProps().getProperty("dsn")

                                         , parent.getProps().getProperty("dns.user"),
                                         parent.getProps().getProperty("dns.pass"));

    


      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
      nf2.setMaximumFractionDigits(2);
      nf.setMaximumFractionDigits(4);
      nf.setGroupingUsed(false);
      nf2.setGroupingUsed(false);

    }
    catch (Exception e) {
      parent.showError(e);
    }
    
    if(parent.getProps().getProperty("rate") != null){
    	 try {
    	    rate = Double.parseDouble(parent.getProps().getProperty("rate"));
    	    } catch (Exception e){
    	       parent.showError(e);
    	    }
    }
  }
  
  private double  rate = 0.702804;

  public String identifyCustomer(String id,String regnr) throws SQLException {
	  String sql = "SELECT SL01001 from SL010100 where  SL01125 = '" + id + "'";	  
	  String result = null;
	  Statement st = con2.createStatement();
      ResultSet rs = st.executeQuery(sql);
      while(rs.next()){
    	  result = rs.getString("SL01001").trim();
      }
    return result;
  }

  public String identifyProduct(String ean, String prodID) throws SQLException{
	  System.out.println("ean " + ean + "   prodID " + prodID );
	  	//SELECT SC01001 AS toode,SC01071 AS tykke,SC01132 as ean FROM SC010100 where SC01132
	    if(prodID == null){
	    	prodID = "xxxxxx";
	    }
	    String sql1 = "SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01001)=  '" + prodID.trim() + "'";

	    String prod = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      prod = rs1.getString("toode").trim();
	    }
	    
		   System.out.println(ean + " prod0 " + prod);
		    if(prod == null && ean != null){
			    String sql2 = " SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01132)=   '" + ean.trim() + "'";
		      rs1 = st1.executeQuery(sql2);
		      while (rs1.next()) {
		        prod = rs1.getString("toode").trim();
		      }
		    }
		    
	    if(prod == null && prodID.trim().length()>0){
		    String sql2 = " SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01132)=   '" + prodID.trim() + "'";
	      rs1 = st1.executeQuery(sql2);

	      while (rs1.next()) {
	        prod = rs1.getString("toode").trim();
	      }
	    }

	    
	    


	    System.out.println(prodID + " prod1 " + prod);
	    rs1.close();
	    st1.close();

	    return prod;
	  }
  
  public double getInCase(String prodID) throws SQLException{
	  	//SELECT SC01001 AS toode,SC01071 AS tykke,SC01132 as ean FROM SC010100 where SC01132
	    
	    String sql1 = "SELECT SC01071 FROM SC010100  WHERE RTRIM(SC01001)=  '" + prodID.trim() + "'";

	    double  incase = 1;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	incase = rs1.getDouble("SC01071");
	    }
	  
	    System.out.println(prodID + " incase " + incase);
	    rs1.close();
	    st1.close();

	    return incase;
	  }
  


  private Vector splitOrder (Order order){
	  
	  Vector result = new Vector();
	  Order ord600 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ord700 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ord500 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ord400 = new Order(order.getCompany(),order.getAgentFirstName());
	     
	  ord600.setAgentCode(order.getAgentCode());
	  ord600.setDokNr(order.getDokNr() + "01");
	  ord600.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord600.setWarehouse(order.getWarehouse());
	  
	  ord400.setAgentCode(order.getAgentCode());
	  ord400.setDokNr(order.getDokNr() + "03");
	  ord400.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord400.setWarehouse(order.getWarehouse());

	  ord500.setAgentCode(order.getAgentCode());
	  ord500.setDokNr(order.getDokNr());
	  ord500.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord500.setWarehouse(order.getWarehouse());

	  ord700.setAgentCode(order.getAgentCode());
	  ord700.setDokNr(order.getDokNr() + "02");
	  ord700.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord700.setWarehouse(order.getWarehouse());
	  
	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	  int linecount = 0;
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
		  
		     System.out.println("Splitting " + lin.getToode() + " " + lin.getTooteNimi());
		     try{
		    	 
		    
		     if(linecount < 25){
		     	
		     	ord500.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		     	System.out.println("500 " + lin.getToode());
		     }else if(linecount < 50){
		     
		     	ord600.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		     	System.out.println("600 " + lin.getToode());
		     }else if(linecount < 50){
		    	
			     ord700.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     		lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     System.out.println("700 " + lin.getToode());
		     }else {
		    	
			     ord400.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     		lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     System.out.println("400 " + lin.getToode());
		     }
		      linecount++;
		     }catch(Exception e){
		    	 e.printStackTrace();
		     }
		  
	  }
	  

	  
	  if(ord600.getOrderLines() != null && ord600.getOrderLines().size()>0){
		  result.add(ord600);  
	  }
	  if(ord500.getOrderLines() != null && ord500.getOrderLines().size()>0){
		  result.add(ord500);  
	  }
	  if(ord700.getOrderLines() != null && ord700.getOrderLines().size()>0){
		  result.add(ord700);  
	  }
	  if(ord400.getOrderLines() != null && ord400.getOrderLines().size()>0){
		  result.add(ord400);  
	  }
	 System.out.println("Result =" +order.getDokNr()
			 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }

  

  public void makeOrder(Order order) throws SQLException{
   
	  Vector splitted =  splitOrder(order);
	  Enumeration en = splitted.elements();
	
	  while(en.hasMoreElements()){
		  Order ord = (Order)en.nextElement();	
	
	      makeOutput(ord);  
	  }
	  setNumAddedLines(cou);

  }

  
  
  private void makeOutput(Order order){
	 	StringBuffer sb = new StringBuffer();
	 	 
	 	 cou = 0;

	 	 
	 	try {
	 	

	      
	 	    String cust = order.getCompany();
			String agent = this.identifyCustomerAGENT(cust);
			
			String ref = order.getReference();
			if ( ref == null ){
				ref =  " ";
			}
			String com = order.getEmailBody();
			if ( com == null ){
				com =  " ";
			}		
			String ware = this.identifyWare(cust);
			if ( ware == null ){
				ware = " ";
			}
			String ordnr = order.getDokNr();
			boolean valis = false ;
			 if(ordnr.startsWith("45")){
				 ordnr = Calendar.getInstance().getTimeInMillis() + "";
				 if(ordnr.length()>9){
						ordnr = ordnr.substring(ordnr.length()-9 ,ordnr.length()) ;
					}
				 valis = true ;
			 }
			
			
			 String doknr = "1" + agent + ordnr;
			 if(doknr.length()>10){
				 doknr = doknr.substring(0,10);
			 }
			 if(valis){
				 doknr = "1" + ordnr;
			 }
	          
		
	    Vector lines = order.getOrderLines();
	      for (int i = 0; i < lines.size(); i++) {
	      	
	          
	      	OrderLine line = (OrderLine)lines.elementAt(i);
	          String toode = line.getToode();
	         double kogus = line.getKogus(); // kommentisin v�lja et ei/getInCase(toode);
	         double incase = getInCase(toode);
	         double kaste = kogus /incase;
	         
	         System.out.println("kogus enne " + kogus);

	       int aa = (int)(kaste+0.5);
	       if(aa != kaste){
	    	   kogus = aa * incase;
	       }
	         //siin tuleb koguseks ymmardatud aga testida tuleb seda kohta 
	       
	       
	         kogus = Math.round(kogus);
	        
	         System.out.println("kogus p�rast " + kogus + " '" + addSpaces(ware,6) + "'" );
	          if(kogus != 0 && toode.trim().length() > 0){
	        	
	          sb.append(addSpaces(cust, 10));//10
	          sb.append(addSpaces(toode, 35));//45
	          sb.append(addSpaces(nf.format(kogus), 12));  
	          sb.append(addSpaces(" ", 6));
	          sb.append(addSpaces(getTarne(order),8));//47
	          sb.append(addSpaces(doknr, 10));//154
	          
	          sb.append(addSpaces(ordnr, 35));//154
	          sb.append(addSpaces(agent,3));//58

	//    324558    431115                             1                 2008102945186840204518684020                         102    1
	 //   310463    431111                             2                         1102007039                                   10201101
	          sb.append(addSpaces(ware,6));//58
	          sb.append("1");//58 see lisatakse et tellimuse tyyp oleks 1 ei l�he laost maha
	          sb.append(addSpaces(order.getDokNr(),10));
	          sb.append(System.getProperty("line.separator"));
	    
	           cou++;
	       
	          }
	      }
	    	
	    
        	 
          String fileName = escape(parent.getProps().getProperty("conv.dir") + agent)  + "." + doknr   
          +  "." + (Calendar.getInstance().getTimeInMillis() + "" ).substring(7)+   ".txt";
 	     
 	      System.out.println(fileName);
 	        FileOutputStream fos = new FileOutputStream(fileName);
 	        fos.write(sb.toString().getBytes());
 	        fos.close();
        	
        
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
	 
	  
  private String getTarne(Order order){
	    Calendar dat = order.getShipCal();
	    
	    String mon ="";
	  	String day = "";
	  	String year = "";

	    if(dat != null){
	  	 mon =(dat.get(Calendar.MONTH) + 1 )+"";
	  	 day =  (dat.get(Calendar.DAY_OF_MONTH)) + "";
	  	 year = (dat.get(Calendar.YEAR))+"";
	  	
	       if(dat != null){	 
	    	  if(mon.length()==1){
	    	  	mon= "0"+ mon;    	  
	    	  	}
	    	  if(day.length()==1){
	    	  	day= "0"+ day;    	  
	    	  	}
	       }
	   }
	    
	    return year+mon+day;
	    
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
	  
  
  
  public String getShippingTime(Order order){
    Calendar cal = order.getShipCal();
    int month = cal.get(Calendar.MONTH) + 1;
    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    String sqlDate = cal.get(Calendar.YEAR) + "-" + month + "-" + day;
    return sqlDate;
  }



  public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  
  public String identifyWare(String id) throws SQLException {
	 	 String	sql1 = "SELECT SL01098 AS ware ,SL01026 AS auto FROM SL010100 WHERE SL01001='" + id + "'"; 
	 	
	 	 
	 	 
	   System.out.println(sql1);
	   String agent = null;

	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   while (rs1.next()) {
	   	agent = addSpaces(rs1.getString("ware").trim(),4);
	   	agent = agent + rs1.getString("auto").trim();

	   }
	   rs1.close();
	   st1.close();

	   return agent;

	 }
  public String identifyCustomerAGENT(String id) throws SQLException {
	 	 String	sql1 = "SELECT SL01035 as agent FROM SL010100 WHERE SL01001='" + id + "'"; 
	 	
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




  public String getDriverVersion(){
    return "ScalaLF";
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
  private Connection con;
  private Connection con2;
  private   int cou = 0;
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);
  private NumberFormat calNF = NumberFormat.getInstance();
  private ImportPanel parent;
  private boolean isAmount = false;
 
public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
