
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONObject;

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
      fileName = parent.getProps().getProperty("conv.dir") + getYYYYpMMpDDpString(new Date())   +  ".csv";
    } catch (Exception e) {
      parent.showError(e);
      log.error(e);
    }
  }
  
  
  public static String getYYYYpMMpDDpString(Date dd){
	    StringBuffer sb = new StringBuffer();
	    Calendar cal= Calendar.getInstance();
	    cal.setTime(dd);
	    
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}
		sb.append("" + cal.get(Calendar.YEAR));
		sb.append("-" + month  + "-");
		sb.append(day );
		
		
		String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
		if (hour.length() == 1) {hour = "0" + hour;}
	
		String min = String.valueOf(cal.get(Calendar.MINUTE));
		if (min.length() == 1) {min = "0" + min;}

		sb.append(" " + hour  + "_" + min);
		
	    return sb.toString();
	  }
  
  private  String fileName = null;
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
 		String code = rs.getString("code");
 		
 		String name = rs.getString("Name");
 	   
 	  if (name != null && name.trim().length() > 2 ){
 		    Product prod = new Product(kood);
 		    name = name.trim();
 		    prod.setEAN(parent.make(rs.getString("ean")));
 		    if(code != null) {
 			prod.setAgentGroup(code);
 		    }
     
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
             
           
                // name =  name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
                 prod.setName(name);
                 
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

  	 
  	 private String m(String x) {
  		 if(x == null || x.toString().length() == 0 ) {
  			 return " ";
  		 }else {
  			 return x;
  		 }
  	 }
  	   
  	 public TreeMap getCustomers() {
  		 

         System.out.println("kliendid algus");
         String cmd =
 	    "select a.\"Id\", c.\"Code\" as code1 ,c.\"Id\" as pid , c.\"Name\" , a.\"AddressName\" "
 	    + ",a.\"Zip\", a.\"Street\", a.\"City\",a.\"PublicDomain\", a.\"Number\" , a.\"Building\" , a.\"Staircase\" , a.\"Floor\" , a.\"Door\" "
 	   + ",c.\"CentralZip\", c.\"CentralStreet\", c.\"CentralCity\", c.\"CentralPublicDomain\", c.\"CentralNumber\" , c.\"CentralBuilding\" , c.\"CentralStaircase\" , c.\"CentralFloor\" , c.\"CentralDoor\", "
 	    + "c.\"PaymentMethodDelayDay\" , c.\"AppliedPriceRule\" "
 	    + ", c.\"ContactName\",c.\"ContactPhone\" ,  c.\"Email\",c.\"TaxNumber\" as vatno ,  c.\"CompanyRegNumber\" as regno"
 	    + " from \"CustomerAddress\" a INNER JOIN \"Customer\" c ON c.\"Id\" = a.\"Customer\"" ;

         
        // Zip  City Street    Building , Staircase  Floor Door  ,  CentralAddress  CentralCountry CentralZip , CentralCity CentralStreet 
         // CentralNumber CentralBuilding CentralStaircase , CentralFloor  , CentralDoor  Bank  ObligatoryPaymentTerms DateOfFiling 



 	TreeMap customers = new TreeMap();

 	try {
 		 Connection  con = DriverManager.getConnection(url2);
 	        Statement st = con.createStatement();
 	    
 	    ResultSet	      rs = st.executeQuery(cmd);

 	    while (rs.next()) {
             String kood = rs.getString("Id").trim();
             String code = rs.getString("code1");
             String payer = rs.getString("pid").trim();
 		Customer cust = new Customer(kood);
 		         cust.setPayer(payer);
 		         if(code != null) {
 		         cust.setMainAgent(code.trim());
 		         }
 		        JSONObject json = new JSONObject();
 		 		
 		 		
 		String nimi = parent.make(rs.getString("Name"));
 		String nimi2 = parent.make(rs.getString("AddressName"));
 		String zipcode = parent.make(rs.getString("Zip"));
 		String street = parent.make(rs.getString("Street"));
 		String city = parent.make(rs.getString("City"));
 		String publicDomain = parent.make(rs.getString("PublicDomain"));
 		
 		String number = parent.make(rs.getString("Number"));
 		String building = parent.make(rs.getString("Building"));
 		String staircase = parent.make(rs.getString("Staircase"));
 		String floor = parent.make(rs.getString("Floor"));
 		String door = parent.make(rs.getString("Door"));
 		
 		
 		
 		String zipcodeC = parent.make(rs.getString("CentralZip"));
 		String streetC = parent.make(rs.getString("CentralStreet"));
 		String cityC = parent.make(rs.getString("CentralCity"));
 		String publicDomainC = parent.make(rs.getString("CentralPublicDomain"));
 		
 		String numberC = parent.make(rs.getString("CentralNumber"));
 		String buildingC = parent.make(rs.getString("CentralBuilding"));
 		String staircaseC = parent.make(rs.getString("CentralStaircase"));
 		String floorC = parent.make(rs.getString("CentralFloor"));
 		String doorC = parent.make(rs.getString("CentralDoor"));
 		
 		
 		String paym = parent.make(rs.getString("PaymentMethodDelayDay"));
 		String pricegr = parent.make(rs.getString("AppliedPriceRule"));
 		String kontakt = parent.make(rs.getString("ContactName"));
 		String kontaktTel = parent.make(rs.getString("ContactPhone"));
 		String email = parent.make(rs.getString("Email"));
 		String vatno = parent.make(rs.getString("vatno"));
 		String regno =  parent.make(rs.getString("regno"));
 		String ag = "HU001";
 		
 		json.put("street", m(street));
 		json.put("number", m(number));
 		json.put("city", m(city));
 		json.put("building", m(building));
 		json.put("staircase", m(staircase));
 		json.put("floor", m(floor));
 		json.put("door", m(door));
 		json.put("buildingC", m(buildingC));
 		json.put("staircaseC",m( staircaseC));
 		json.put("floorC", m(floorC));
 		json.put("doorC", m(doorC));
 		json.put("numberC",m(numberC));
 		json.put("cityC", m(cityC));
 		json.put("streetC", m(streetC));
 		json.put("zipcodeC", m(zipcodeC));
 		json.put("country","Magyarország");
 		json.put("countryC","Magyarország");
 		json.put("zipcode", m(zipcode));
 		
 	
 		String pd = "út";
 		if(publicDomain.equals("2711")) {
 			pd = "utca";
 		}
 		if(publicDomain.equals("2738")) {
 			pd = "sétány";
 		}
 		if(publicDomain.equals("2714")) {
 			pd = "körút";
 		}
 		if(publicDomain.equals("2735")) {
 			pd = "park";
 		}
 		
 		json.put("publicDomain", m(pd));
 		
 		
 		pd = "út";
 		if(publicDomainC.equals("2711")) {
 			pd = "utca";
 		}
 		if(publicDomainC.equals("2738")) {
 			pd = "sétány";
 		}
 		if(publicDomainC.equals("2714")) {
 			pd = "körút";
 		}
 		if(publicDomainC.equals("2735")) {
 			pd = "park";
 		}
 		json.put("publicDomainC", m(pd));
 	
 		
 		         Agent aa = new Agent(ag);
 		         cust.addAgent(aa);
 		
                 cust.setName((nimi) );
                 
                    cust.restStreet = street;
                    cust.restCity = city;
                    cust.setCity(city);
                    cust.setZipcode(zipcode);
                	cust.setAddress(street);
                	cust.json = json;
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
          JSONObject json = cust.json;
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
            sb.append( cust.getMainAgent() +  ls); 
            sb.append( custName +  ls);
            sb.append(json.get("zipcodeC")  + ls);
            sb.append(json.get("cityC")  + ls);
            sb.append(json.get("streetC")  + ls);
            sb.append(json.get("zipcode")  + ls);
            sb.append(json.get("city")  + ls);
            sb.append(json.get("street")  + ls);
            sb.append(getDateString(Calendar.getInstance())  + ls);
            sb.append(getDateString(cal)  + ls);
            
            sb.append( cust.getVatnr() +  ls);
            sb.append( "átutalás" +  ls);
            sb.append( prod.getAgentGroup()+  ls);
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
            sb.append("" + ls);
            sb.append("" + ls);
            
      /*  	json.put("building", building);
     		json.put("staircase", staircase);
     		json.put("floor", floor);
     		json.put("door", door);
     		json.put("buildingC", buildingC);
     		json.put("staircaseC", staircaseC);
     		json.put("floorC", floorC);
     		json.put("doorC", doorC);
     		json.put("numberC", numberC);
     		json.put("cityC", cityC);
     		json.put("streetC", streetC);
     		json.put("zipcodeC", zipcodeC);*/
           
        
            sb.append(json.get("countryC") + ls); // Buyer address - Country
            sb.append(json.get("publicDomainC") + ls); //Customer address - Public area
            sb.append(json.get("doorC") + ls); //Customer address - Door
            sb.append(json.get("buildingC") + ls);//Customer address - Building
            sb.append(json.get("floorC") + ls);// Customer address - Floor
            sb.append(json.get("staircaseC") + ls);//Customer address - Staircase
            sb.append(json.get("numberC")  + ls);//Customer address - House number
            sb.append(json.get("country") + ls); // Shipping address - Country
            sb.append(json.get("publicDomain") + ls);// Delivery address - Public area
            sb.append(json.get("door") + ls);// Shipping address - Door
            sb.append(json.get("building") + ls);//Delivery address - Building
            sb.append(json.get("floor") + ls);// Delivery address - Floor
            sb.append(json.get("staircase") + ls);// Delivery address - Staircase
            sb.append(json.get("number")  + ls);// Delivery address - House number
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
    
      
       System.out.println(fileName);
      // Converter.saveFileInANSI(fileName, sb.toString());
       
       FileUtils.writeStringToFile(new File(fileName),sb.toString() , true);
 	} catch (Exception e) {
 		e.printStackTrace();
 	}
  }

 
	public NumberFormat getNF() {
		return this.nf;
	}
	
	
	public static void saveFileInANSI(String filename, String sb ){
	      try{
	    	  
	    
	      	  Writer out = new BufferedWriter(new OutputStreamWriter(
	      			    new FileOutputStream(filename), "ISO-8859-2"));
	      			try {
	      			    out.write(sb.toString());
	      			} finally {
	      			    out.close();
	      			}
		            	
			    }catch(Exception e){
			    	e.printStackTrace();
			    }
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
