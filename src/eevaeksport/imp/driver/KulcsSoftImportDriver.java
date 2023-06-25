
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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.Customer;
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

public class KulcsSoftImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(KulcsSoftImportDriver.class.getName());

  
  public KulcsSoftImportDriver(ImportPanel parent) {
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

    } catch (Exception e) {
      parent.showError(e);
      log.error(e);
    }
  }
  private TreeMap prods = null;
  private TreeMap eans = new TreeMap();
  private String url2 = "jdbc:odbc:dziugas";
  private Hashtable dates = new Hashtable();
  	  
  	private Connection getNewConnection2() throws SQLException{
  	    return DriverManager.getConnection(url2);
  	  }
  	
  	 public TreeMap getProducts() {
     
     	TreeMap products = new TreeMap();
     	TreeMap prices = new TreeMap();
     	try {
         
 try{
      String sql = " select \"Product\", \"Price\",  \"ValidFrom\" , \"ValidTo\" from  \"PriceRulePrice\" pr INNER JOIN \"PriceRuleDetail\" d ON d.\"Id\" = pr.\"PriceRuleDetail\" \r\n" + 
      		" where \"PriceRule\" = 386 and Current_Date between \"ValidFrom\" and \"ValidTo\" and \"Price\" > 0 order by 1 " ; //where osak='LADU' 
            
      Connection  con = DriverManager.getConnection(url2);
  PreparedStatement pstm = con.prepareStatement(sql);
  ResultSet rs6 = pstm.executeQuery();

 while(rs6.next()){
 	prices.put(rs6.getString("Product").trim(),rs6.getDouble("Price")+ "");
  }


 rs6.close();
 pstm.close();
 con.close();


 System.out.println("prices " + prices.size());

 }catch(Exception r){
 r.printStackTrace();
 }
    

            String cmd = " select p.\"Id\" as toode ,p.\"Code\" as code, p.\"Name\", p.\"Barcode\" as ean,"
            		+ " p.\"MinimumQuantity\" as minam, 1 as price , p.\"Vat\" , 'db' as unit ,  p.\"StockBalance\" as stock from  \"Product\" p where \"Service\" = 0 ";
 //kood AS liik , nimetus AS nimi  FROM artryhm t
 			
 			    System.out.println(cmd);
 			
 			Connection con = DriverManager.getConnection(url2);
          Statement sst = con.createStatement();
            ResultSet  rs = sst.executeQuery(cmd);

 	    while (rs.next()) {
 		String kood = rs.getString("toode").trim();
 		
 		String name = rs.getString("Name");
 	   
 	  if (name != null && name.trim().length() > 2 ){
 		    Product prod = new Product(kood);
 		    name = name.trim();
 		    prod.setEAN(parent.make(rs.getString("ean")));
 			
 		     
     
                  if(prices.get(kood)!= null){
                 	 prod.setPrice(Double.parseDouble((String)prices.get(kood)));
                  }
                  prod.setGroup("zpienas");
                 try {
                 	 prod.setInCases(rs.getDouble("minam"));
                 }catch(Exception e) {
                 	
                 }
                
                 
                 int vat = rs.getInt("Vat");
                 if(vat == 275 ) {
                 	vat = 18;
                 }else {
                 	vat = 27;
                 }
                 prod.setVat(vat+"%");
                 
                 prod.setPrice(rs.getDouble("price"));
             
                 
                 String ean4 = "";
                 if(prod.getEAN()!= null && prod.getEAN().length() > 6 ){
                 	ean4 = prod.getEAN().substring(prod.getEAN().length()-4);
                 }
                 
                // name =  name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
                 prod.setName(Converter.enCodeEst(name + " " + ean4));
                 
                 String yh = rs.getString("unit");
                 
                 if(yh != null && yh.trim().length()>0){
                 	prod.setUnit(yh.trim());
                 }
                 Double stock = rs.getDouble("stock");
                 if(stock != null && stock > 0) {
                 	prod.setStock(stock);
                 }
             
                 products.put(prod.getID(), prod);
 		         }
 		    }
 	    
             rs.close();
             sst.close();
             con.close();
             
             
  	} catch (Exception e) {
 	   e.printStackTrace();
 	}

  	  System.out.println("tooted l�pp");
 	return products;
     }

  	   
  	 public TreeMap getCustomers() {
  		 

         System.out.println("kliendid algus");
         String cmd =
 	    "select a.\"Id\",c.\"Id\" as pid , c.\"Name\" , a.\"AddressName\" ,a.\"Zip\", a.\"Street\", a.\"City\", a.\"Number\" , c.\"PaymentMethodDelayDay\" , c.\"AppliedPriceRule\" "
 	    + ", c.\"ContactName\",c.\"ContactPhone\" ,  c.\"Email\",c.\"TaxNumber\" as vatno ,  c.\"CompanyRegNumber\" as regno from \"CustomerAddress\" a INNER JOIN \"Customer\" c ON c.\"Id\" = a.\"Customer\"" ;



 	TreeMap customers = new TreeMap();

 	try {
 		 Connection  con = DriverManager.getConnection(url2);
 	        Statement st = con.createStatement();
 	    
 	    ResultSet	      rs = st.executeQuery(cmd);

 	    while (rs.next()) {
             String kood = rs.getString("Id").trim();
             String payer = rs.getString("pid").trim();
 		Customer cust = new Customer(kood);
 		         cust.setPayer(payer);
 		         
 		String nimi = parent.make(rs.getString("Name"));
 		String nimi2 = parent.make(rs.getString("AddressName"));
 		String zipcode = parent.make(rs.getString("Zip"));
 		String street = parent.make(rs.getString("Street"));
 		String city = parent.make(rs.getString("City"));
 		String number = parent.make(rs.getString("Number"));
 		String paym = parent.make(rs.getString("PaymentMethodDelayDay"));
 		String pricegr = parent.make(rs.getString("AppliedPriceRule"));
 		String kontakt = parent.make(rs.getString("ContactName"));
 		String kontaktTel = parent.make(rs.getString("ContactPhone"));
 		String email = parent.make(rs.getString("Email"));
 		String vatno = parent.make(rs.getString("vatno"));
 		String regno =  parent.make(rs.getString("regno"));
 		String ag = "HU001";
 		         Agent aa = new Agent(ag);
 		         cust.addAgent(aa);
 		
                 cust.setName(Converter.enCodeEst(nimi) + " " + Converter.enCodeEst(nimi2));
                 
                    cust.restStreet = street+" "+ number;
                    cust.restCity = city;
                    cust.setCity(city);
                    cust.setZipcode(zipcode);
                	cust.setAddress(street+ " " + number );
                 	cust.setPersonalData(kontakt + " " + kontaktTel + "|" + email);
                 	cust.setPaymTerm(paym);
                 	cust.setGroup(pricegr);
                 	cust.setVatnr(vatno);
                 	cust.setRegnr(regno);
                 
                 customers.put(cust.getID(), cust);
 	    }
 	    rs.close();
 	    st.close();
 	    con.close();
 	    
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
 	  System.out.println("kliendid l�pp");
 	return customers;
     }




  public String identifyCustomer(String id,String regnr) throws SQLException {
    return id;
 }

 
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	
    if(prodID != null){
    	return prodID;
    }
  //  System.out.print("eans "+ eans.size() + " " + eans.get(ean));
    if(eans.get(ean) != null){
    	return eans.get(ean) + "";
    }
    return prodID;
  }


  
  
  
  public void makeOrder(Order order) throws SQLException{
	  if(this.prods == null) {
		 prods = getProducts();
	  }
	  if(customers ==  null) {
	  		 customers = this.getCustomers();
	  	 }

	  counter = 0;
      makeOutput(order);
	  setNumAddedLines(counter);
  }

  

  private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    sb.append((cal.get(Calendar.YEAR)+"")+ ".");
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month + ".");

	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day);

	    return sb.toString();
	  }

private TreeMap customers = null;
  
  
  private void makeOutput(Order order){
  	StringBuffer sb = new StringBuffer();
  	 nf.setGroupingUsed(false);
  	
  	try {
  	  int cou = 0;
  	String com = null;

  	if(order.getInfo() != null && order.getInfo().trim().length() >0){
  			com =  order.getInfo() ;
  		}
  	
 	      String doknr = order.getDokNr();
 		  String ls = ";";       
          String custID = order.getCompany();
          Customer cust = (Customer)customers.get(custID);
         
          
          String custName = cust.getName();
          Calendar  shipDate = order.getShipCal();
          Calendar cal = Calendar.getInstance();
           
            if (shipDate != null) {
                  cal = shipDate;
                }
            
            Vector sortedLines = order.getOrderLines();
            for (int i = 0; i < sortedLines.size(); i++) {
            	
            OrderLine line = (OrderLine)sortedLines.get(i);
            
            Product prod = (Product) this.prods.get(line.getToode());
            String prodName = line.getToode();
            String vatt = "vattt";
            if(prod != null) {
            	vatt = prod.getVat();
            }
             
            if(prod != null) {
            	prodName = prod.getName();
            }
           
            
            sb.append((order.getDokNr()).trim() + ls); 
            sb.append("HUF" + ls); 
            sb.append("1" + ls); 
            sb.append( custID +  ls); 
            sb.append( custName +  ls);
            sb.append( cust.getZipcode() +  ls);
            sb.append( cust.getCity() +  ls);
            sb.append( cust.getAddress() +  ls);
            sb.append( cust.getZipcode() +  ls);
            sb.append( cust.getCity() +  ls);
            sb.append( cust.getAddress() +  ls);
            sb.append(getDateString(Calendar.getInstance())  + ls);
            sb.append(getDateString(cal)  + ls);
            
            sb.append( cust.getVatnr() +  ls);
            sb.append( cust.getPaymTerm() +  ls);
            sb.append( line.getToode() +  ls);
            sb.append( prodName +  ls);
            sb.append( getNF().format(line.getKogus())+  ls);
            sb.append(line.getUnit() + ls);
            
            sb.append( getNF().format(line.getPrice())+  ls);
            sb.append(line.getPartii() + ls);
            sb.append(vatt + ls);
            sb.append(" " + ls);
            sb.append(" " + ls);
            sb.append(" " + ls);
            sb.append("RE" + ls);
            sb.append(" " + ls);
            sb.append(" " + ls);
            sb.append(" " + ls);
            sb.append("Public area " + ls);
            sb.append(" " + ls);
            sb.append(" " + ls);
            sb.append(" " + ls);
            sb.append(" " + ls);
            sb.append("House 1 " + ls);
            sb.append(" " + ls);
            sb.append("Public area " + ls);
            sb.append(" " + ls);
            sb.append(" " + ls);
            sb.append(" " + ls);
            sb.append(" " + ls);
            sb.append("House 1 " + ls);
            sb.append("0" + ls);
            sb.append("0" + ls);
            sb.append("0" + ls);
            sb.append(" " + ls);
            sb.append(" " + ls);
            sb.append(System.getProperty("line.separator"));
              
                   double ale = line.getDiscount();     
                counter++;
                String ean = line.getEAN();
              
                	
              	  double discprice = (100d -line.getDiscount())*line.getPrice()/100d ;
             
             
                
               
            
                
                
            }
            

            
           

  
       counter = counter + cou;
     	//
       String fn = doknr;
       if(doknr.trim().length()== 0){
     	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
       }
       String fileName = parent.getProps().getProperty("conv.dir") + order.getAgent()  + "." + fn  +  ".csv";
       System.out.println(fileName);
       Converter.saveFileInUTF8(fileName, sb);

 	} catch (Exception e) {
 		e.printStackTrace();
 	}
  }

 
	public NumberFormat getNF() {
		return this.nf;
	}
	
	
	  
 
 
  public String getDriverVersion(){
    return "KulcsSoftImport";
  }



  
  public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  
  private NumberFormat calNF = NumberFormat.getInstance();

 

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private int counter = 0;
  private ImportPanel parent;
  private String trouble = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return trouble;
}
}
