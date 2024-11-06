
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONObject;

import com.amazonaws.services.s3.model.S3DataSource.Utils;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.Debt;
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

public class SamsonasCSVImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public SamsonasCSVImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    try {
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  
  private Vector splitOrder (Order order){
	  Vector result = new Vector();
      if(prods == null) {
    	  this.getProducts();
      }
      if(customers == null) {
    	  this.getCustomers();
      }
	// System.out.println("Enne suurus splitti" + order.getOrderLines().size());
	
	  Order ord23 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ordOTB = new Order(order.getCompany(),order.getAgentFirstName());
    
	  ord23.setAgentCode(order.getAgentCode());
	  ord23.setDokNr(order.getDokNr());
	  ord23.setInfo(order.getInfo());
	  ord23.setReference(order.getReference());
	  ord23.setTeostaja((order.getTeostaja()));
	  ord23.setChanged(order.isChanged());
	  ord23.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord23.setStatus(order.getStatus());
	  ord23.setShipTime(order.getShipTime());
	  ord23.setShipDateUsed(order.isShipDateUsed());
	  ord23.setWarehouse(order.getWarehouse());
	  ord23.setCurrency("MEAT");
	  
	

	  ordOTB.setAgentCode(order.getAgentCode());
	  ordOTB.setDokNr(order.getDokNr());
	  ordOTB.setInfo(order.getInfo());
	  ordOTB.setReference(order.getReference());
	  ordOTB.setTeostaja((order.getTeostaja()));
	  ordOTB.setChanged(order.isChanged());
	  ordOTB.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordOTB.setStatus(order.getStatus());
	  ordOTB.setShipTime(order.getShipTime());
	  ordOTB.setShipDateUsed(order.isShipDateUsed());
	  ordOTB.setWarehouse(order.getWarehouse());
	  ordOTB.setCurrency("VEGE");
	  
	
	
	//  System.out.println("Splitin Warehouse " + order.getWarehouse());
	  
	  Vector lines = order.getOrderLines();
	  Enumeration enums = lines.elements();
	  while  (enums.hasMoreElements()){
		  OrderLine lin = (OrderLine)enums.nextElement();
		
		     String itemid = lin.getToode();
		  
		    	// if(itemid != null && prods.get(itemid) != null) {
		    	  
		      //    Product pr = (Product)  prods.get(itemid);
		    		// Võtame maha hetkel spliti.
		    	//  if(pr.getGroup().toLowerCase().startsWith("me") ){
			    	 ordOTB.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,lin.docType);
		          //    }
		    	 
		    	  /*if(pr.getGroup().toLowerCase().startsWith("ki") ){
		        	ord23.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     		 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,lin.docType);
			          }*/ 
		    // } 
	  }

	  if(ord23.getOrderLines() != null && ord23.getOrderLines().size()>0){
		  result.add(ord23);  
	  }

	  if(ordOTB.getOrderLines() != null && ordOTB.getOrderLines().size()>0){
		  result.add(ordOTB);  
	  }
//	 System.out.println("Result =" +order.getDokNr()
	//		 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }


  public String identifyCustomer(String id,String regnr) throws SQLException {
    return null;
    }
  
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
     return prodID;
    }


  
  public void makeOrder(Order order) throws SQLException{

	  counter = 0;
	//  makeOutput(order,true); 	    
	
	  Vector splitted =  splitOrder(order);
      
	  Enumeration en = splitted.elements();
	  while(en.hasMoreElements()){
		  Order ord = (Order)en.nextElement();
		  
	   /*    boolean tagastus = false;
	         Vector lin = order.getOrderLines();
	         for (int i = 0; i < lin.size(); i++) {
	        	 OrderLine line = (OrderLine)lin.elementAt(i);
	        	 if(line.getKogus() < 0 ) {
	        		 tagastus = true;
	        	 }
	         }*/
	
	    if(ord.getCurrency().equals("MEAT")){
                  makeOutput(ord,true);  
               } else {
   				  makeOutput(ord,false);  
   			  }
          } 
	  
	  setNumAddedLines(counter);
  }

  
  public void getCustomers(){
	  customers = new TreeMap();
	     
	     try {
	     FileReader fr = new FileReader(parent.getProps().getProperty("readin")  + "Customer.csv");
	     BufferedReader reader = new BufferedReader(fr);
	     String line = null;
	     //Custcode	 CustName	PayerCode	Address	City	Post Code	DebtDue	SalespersonCode	Contact	PhoneNo	PaymentTerm	CreditLimit	UsedCredit	CustDiscount	Blocked

	       line=reader.readLine();
	     while((line = reader.readLine()) != null){
	    	 
	    	 line = line.replaceAll(";"," |");
	        StringTokenizer st = new StringTokenizer(line, "|");
	       if (st.hasMoreTokens()){

	           String id = st.nextToken().trim();
	           String name = st.nextToken().trim();
	           String payer = st.nextToken().trim();
	           String city = st.nextToken().trim();
	           String address = st.nextToken().trim();
	           String zipcode = st.nextToken().trim();
	           String deptdue = st.nextToken().trim();
	           String agentcode = st.nextToken().trim();
	           String contactname = st.nextToken().trim();
	           String contactphone = st.nextToken().trim();
	           String payterm = st.nextToken().trim();
	           String crlimit = st.nextToken().trim();
	           String usedcredit = st.nextToken().trim();

	           Customer customer = new Customer(id);
	           customer.setName(Converter.enCodeEst(name));
	           
	            
	           if( address.length() > 0){
	        	   if(city.length() > 0) {
	        		   address = address + " " + city;
	            	   if(zipcode.length() > 0) {
	            		   address = address + " " + zipcode;
	            	   }
	        	   }
	             customer.setAddress(Converter.enCodeEst(Converter.adressLink(address,"Lithuania")));           
	           }
	           
		       	double dept = 0;
		       	try{
		       		dept = Double.parseDouble(deptdue);
		       		customer.setDebt(dept);
		       	}catch(Exception e ){
		       	}
	           
	           if(agentcode.length() > 0) {
	        	   customer.addAgent(new Agent(agentcode));
	        	  customers.put(customer.getID(), agentcode);
	           }
	
	       }
	     }
	     
	     reader.close();
	     fr.close();
	    	
	     } catch (IOException e){
	       e.printStackTrace();
	     }
	   ;
	   }
  
  
  private TreeMap prods = null;
  private TreeMap customers = null;
  
  public void getProducts(){
	     prods = new TreeMap();
	     	     try {
	         String line = null;
	         
			 FileReader fr = new FileReader(parent.getProps().getProperty("readin") + "Product.csv");
			 BufferedReader reader = new BufferedReader(fr);

	        reader.readLine();
	        while(( line = reader.readLine()) != null ){
	        line = line.replaceAll(";"," |");
	     
	       StringTokenizer st = new StringTokenizer(line, "|");
	      //Code		Description		Unit	incase	GroupCode	GroupSecCode	UnitPrice	EAN	Inventory	Pallet	Blocked

	       if (st.hasMoreTokens()){
	    	   String id = st.nextToken().trim();
	    	   String description = st.nextToken().trim();
	    	   String unit = st.nextToken().trim();
	    	   String incase = st.nextToken().trim();
	    	   String groupcode = st.nextToken().trim();
	    	   String groupcodesec = st.nextToken().trim();
	    	   String unitprice = st.nextToken().trim();
	    	   String ean = st.nextToken().trim();
	    	   String inventory = st.nextToken().trim();

	    	   Product prod = new Product(id);
	    	   //System.out.println(description);

				 if(Double.parseDouble(inventory) > 1) {
					 prod.setStock(Double.parseDouble(inventory));
				 }
				 
				 if(Double.parseDouble(incase) > 1) {
					 prod.setInCases(Double.parseDouble(incase));
				 }
				 
				 if(unit.length() > 0) {
					 prod.setUnit(unit);
				 }
				 
				 if(groupcode.length() > 0) {
					 prod.setGroup(groupcode);
				 }
				 
				 if(description.length() > 0) {
					 prod.setName(description + " "+ id);
				 
		
					 if(Double.parseDouble(unitprice) > 0) {
						 prod.setPrice(Double.parseDouble(unitprice));
					 }
					 
					 if(ean.length() > 0) {
						 prod.setEAN(ean);
					 }
					 
					 prod.setVat("21");
					 prods.put(prod.getID(), prod);
				 
				 }
				 
				 
				 

	         }
	       }
	        
	        reader.close();
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	     
	     
	   }

   
  private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	     sb.append((cal.get(Calendar.YEAR) + "").substring(2)+ "/");
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month + "/");
	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day);
	    return sb.toString();
	  }
	  
  
  private String getTimeString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	   
	    String hh = String.valueOf(cal.get(Calendar.HOUR));
	    if (hh.length() == 1){
	      sb.append("0");
	    }
	    sb.append(hh + ":");
	    String min = String.valueOf(cal.get(Calendar.MINUTE));
	    if (min.length() == 1){
	      sb.append("0");
	    }
	    sb.append(min);
	    return sb.toString();
	  }

  private String getDateString2(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	     sb.append((cal.get(Calendar.YEAR) + "").substring(2)+ "");
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month + "");
	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day);
	    return sb.toString();
	  }
	  


  
  
  private void makeOutput(Order order , boolean meat){
	 	StringBuffer sb = new StringBuffer();
	 	boolean minus = false;
	 	JSONObject json = new JSONObject();
	 	
	 	try {

	 		
		    String doknr = order.getDokNr();
		      String fn = doknr;
		      if(doknr.trim().length() == 0){
		    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
		      }
		  
		      String agent = order.getAgent();
		      if(customers != null  && customers.get(order.getCompany()) != null) {
		    	agent = (String) customers.get(order.getCompany());
		      }
			    
			      if(agent == null || agent.trim().length()== 0){
			    	 agent = "xxx";
			      } else{
			    	  agent = agent.trim();
			      }
			      
			String ref = order.getTeostaja(); //mobiilse telli ref number
			if ( ref == null ){
				ref =  "";
			}
			String com = order.getEmailBody();
			if ( com == null ){
				com =  " ";
			}		
		
			String ware = order.getWarehouse();
			if ( ware == null ){
				ware = " ";
			}
		
			
			String comm = order.getInfo();
			if ( comm == null ){
				comm = " ";
			}
			
			
			  Calendar cal = Calendar.getInstance();
		         if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
		        	  cal.add(Calendar.DAY_OF_YEAR, 3);
		         }else {
		        	  cal.add(Calendar.DAY_OF_YEAR, 1);
		         }
		       
		         String shipd = getDateString(cal);
		         
		         if( order.isShipDateUsed()){
	   	    	  shipd = getDateString(order.getShipCal());
	   	    	  cal = order.getShipCal();
	   	         }
	         
			json.put("SamsKey", "EE_S@msonasK3taviskese#Raugtas###135DaugD@ugKartu");
			json.put("Action", "EE_SetOrder");
			json.put("OrderNumber", doknr);
			json.put("OrderDate", Converter.getYYYY_MM_DDpString(Calendar.getInstance()));
			json.put("OrderTime", getTimeString(Calendar.getInstance()));
			json.put("DeliveryDate", Converter.getYYYY_MM_DDpString(cal));
			json.put( "CustomerNo", order.getCompany());
			json.put( "CustomerGLN", "");
            json.put("ManagerNo", agent);
            json.put("Comment", comm);
            json.put("DEV", 1);
            
            JSONObject data = new JSONObject();
	         
	         
	    Vector lines = order.getOrderLines();
	      for (int i = 0; i < lines.size(); i++) {
	    	  JSONObject line = new JSONObject();
	        OrderLine ol = (OrderLine) lines.elementAt(i);
	        if(ol.getKogus() < 0){
	        	minus = true;
	        }
	    	
	        String sep = "|";
	         String lineSep = System.getProperty("line.separator");
	        
	         double ale = ol.getDiscount();
	         if(ale < 0){
	        	 ale = 0;
	         }
	         double hind = ol.getPrice();
	       
	         sb.append(doknr + sep );
	         sb.append(order.getCompany() + sep );
	         sb.append(agent + sep);
	         sb.append(getDateString(Calendar.getInstance()) + sep);
	         sb.append(shipd + sep);
	         sb.append(counter + sep);
	         sb.append(ol.getToode() + sep);
	         sb.append(ol.getKogus() + sep);
	         sb.append(hind + sep);
	         line.put("ItemNo", ol.getToode());
	         line.put("Quantity", ol.getKogus());
	         line.put("UnitPrice", hind);
	        if(ol.getDiscount() > 0) {
	        	 sb.append("1" + sep);
	        	 line.put("Discount", 1);
	         } else {
	        	 sb.append("0" + sep);
	        	 line.put("Discount", 0);
	        }
	       
	         sb.append("\"" + comm +"\"" + sep);
	         sb.append( getTimeString(Calendar.getInstance()) + sep);
	        
	         
	         line.put("Comment", ol.getPartii());
	       

	         //  RIISIP	1MARMELAAD00	20.0000	1.5120	0.0000	05/19/2017	""	4
				//  RIISIP	1MARMELAAD04	20.0000	1.5120	0.0000	05/19/2017	""	4
		  
	          sb.append(lineSep);
	       data.put(counter+"", line );
	       counter++;
	      }
	      json.put("Data",data);
	      
	      setNumAddedLines(counter); 

	      String vahe = "meat/";
	      String respd = "response/";
	      if(meat) {
	    	  vahe = "vege/";
	      }
	      
	     
	    
	         	  String fileName = Converter.escapeXML(parent.getProps().getProperty("conv.dir"))+ vahe +  fn +"_" + order.getCompany() + "_" + getDateString2(Calendar.getInstance()) + ".csv";
			      System.out.println(fileName);		
			      if(minus){
			    	//  fileName = escape(parent.getProps().getProperty("conv.dir2")) +  "t-" + order.getAgent() +"."+ order.getCompany()+"." +fn  + ".csv";
			      }
			
			      try {
			    		String resp = sendOrderAPI(json);
			    		Converter.saveFile(parent.getProps().getProperty("imp.read")+  respd + "response-"+  fn +"_" + order.getCompany() + "_" + getDateString2(Calendar.getInstance()) + ".json", resp);
			    		//System.exit(0); 
			    		if(resp.length() < 40 && !resp.contains("\"Result\":\"ok\"")) {
			    			System.exit(0);
			    		}
			    	}catch(Exception e) {
			    		log.error(e);
			    		System.exit(0);
			    	e.printStackTrace();	
			    	}
			      
			      if(sb.toString().length() > 1) {
			    	  FileOutputStream fos = new FileOutputStream(fileName);
				        fos.write(sb.toString().getBytes());
				        fos.close(); 
			      }else {
			    	  System.exit(0);
			      }
			      
	    	

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
			System.exit(0);
		}
	 }
	  
  
  private String sendOrderAPI(JSONObject json) throws Exception  {
	  HttpClient cli =  HttpClients.createDefault();
	
		String uri = "https://tab.samsonas.lt:443/bcapi";
		
	  HttpPost httpPost = new HttpPost(uri);
		
			 httpPost.setEntity(new StringEntity(json.toJSONString(),"utf-8"));
		        httpPost.setHeader("Accept", "application/json");
		        httpPost.setHeader("Content-type", "application/json;charset=utf-8");
		 HttpResponse resp = cli.execute(httpPost);
		 if (resp.getStatusLine().getStatusCode() != 200) throw new Exception("Login failed! " );
		 
			return ( EntityUtils.toString(resp.getEntity()));
  }
 
	public NumberFormat getNF() {
		return this.nf;
	}
	
 
  public String getDriverVersion(){
    return "SamsonasCSVImport";
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
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private int counter = 0;
  private ImportPanel parent;

  
public String getTrouble() {
	return null;
}
}
