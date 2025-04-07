package eevaeksport.imp.driver;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import ee.mobilejava.earve.GenericDOM;
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

public class S1XMLMomLVImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public S1XMLMomLVImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    loadSignatures();
    }
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
  
  public String identifyCustomer(String id,String regnr)  {

    String acc = null;
    return acc;

  }



  
  public String identifyProduct(String ean, String prodID) throws SQLException{
    return prodID;
  }


  private Vector splitOrder (Order order){
	  
	  Vector result = new Vector();

	  if (order.getDokNr() == null || order.getDokNr().trim().length() == 0){
		  order.setDokNr((Calendar.getInstance().getTimeInMillis() + "").substring(3));
	  }
	  System.out.println("Infokene " + order.getInfo() + " " + order.getDokNr());

	  Order ordorkla = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ordorklabonus = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ordorklaret = new Order(order.getCompany(),order.getAgentFirstName());
	  
	  
	  
	  ordorkla.setAgentCode(order.getAgentCode());
	  ordorkla.setDokNr(order.getDokNr());
	  ordorkla.setCompName(order.getShipMode());
	  ordorkla.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordorkla.setShipTime(order.getShipTime());
	  ordorkla.setStatus(order.getStatus()); 
	  ordorkla.setInfo(order.getInfo());
	  ordorkla.setReference(order.getReference());
	  ordorkla.setTeostaja(order.getTeostaja());
	  ordorkla.setWarehouse(order.getWarehouse());
	  ordorkla.setDealer(order.getDealer());

	  
	  ordorklabonus.setAgentCode(order.getAgentCode());
	  ordorklabonus.setDokNr(order.getDokNr()+"B");
	  ordorklabonus.setCompName(order.getShipMode());
	  ordorklabonus.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordorklabonus.setStatus(order.getStatus()); 
	  ordorklabonus.setShipTime(order.getShipTime());
	  ordorklabonus.setInfo(order.getInfo());
	  ordorklabonus.setReference(order.getReference());
	  ordorklabonus.setTeostaja(order.getTeostaja());
	  ordorklabonus.setWarehouse(order.getWarehouse());
	  ordorklabonus.setDealer(order.getDealer());

	  ordorklaret.setAgentCode(order.getAgentCode());
	  ordorklaret.setDokNr(order.getDokNr()+"R");
	  ordorklaret.setCompName(order.getShipMode());
	  ordorklaret.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordorklaret.setStatus(order.getStatus()); 
	  ordorklaret.setShipTime(order.getShipTime());
	  ordorklaret.setInfo(order.getInfo());
	  ordorklaret.setReference(order.getReference());
	  ordorklaret.setTeostaja(order.getTeostaja());
	  ordorklaret.setWarehouse(order.getWarehouse());
	  ordorklaret.setDealer(order.getDealer());

	  
	  Vector lines = order.getOrderLines();
	  
	  Enumeration enumr = lines.elements();
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
		    	
		    	if(lin.getDiscount() == -2) {
		    		ordorklabonus.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		    		ordorklabonus.setDokNr(order.getDokNr()+"B");
		    		ordorklabonus.setReference(order.getReference());
		    	}else if (lin.getKogus() < 0) {
		    		ordorklaret.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     	ordorklaret.setDokNr(order.getDokNr()+"R");
			     	ordorklaret.setReference(order.getReference());
		    	}else {
		    		ordorkla.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     	ordorkla.setDokNr(order.getDokNr());
			     	ordorkla.setReference(order.getReference());
		    	}
	  }
	  
	  if(ordorkla.getOrderLines() != null && ordorkla.getOrderLines().size()>0){
		  result.add(ordorkla);  
	  }

	  if(ordorklaret.getOrderLines() != null && ordorklaret.getOrderLines().size()>0){
		  result.add(ordorklaret);  
	  }
	  if(ordorklabonus.getOrderLines() != null && ordorklabonus.getOrderLines().size()>0){
		  result.add(ordorklabonus);  
	  }
      
	  
	 System.out.println("Result = " +order.getDokNr()
			 + " "+ result.size());
	
	 return result ;
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
			signatures = (Vector) Converter.readObject("signatures.vec");
		}

		
	} catch (Exception e) {
		signatures = new Vector();
	}

	}

  
  
     public String getTrouble() {
		return trouble;
	   }

	 private String trouble = null;
	
	
    public void makeOrder(Order order) throws SQLException{

	  counter = 0;
	  
	  if (signatures == null){
   	   signatures = new Vector();
      }
	  
	  String signature = order.getSignature();
	  if(signatures.contains(signature)){
		  this.trouble = "REPRETEATING ORDER agent :" + order.getAgent() + " customer :" 
	  + Converter.deCode(order.getCompany()) ;
	
	  } else {
		  
	 Vector vv = this.splitOrder(order);
    
	  for(int k = 0; k<vv.size();k++) {
     	  Order ord = (Order)vv.elementAt(k);
	 		    
    	 	     makeOutput(ord); 
	 	  		
	 	 signatures.add(signature);
      	}   
      }

	  try {
			Converter.saveObject(signatures, "signatures.vec");
		} catch (IOException e) {
			e.printStackTrace();
		}
	  
	  setNumAddedLines(counter);
  
  }


  
  
  private Node getNode(Node rt, String s){
	    return GenericDOM.getNode(rt, s);
	  }
  
	  private FileInputStream getCleanInputStream(File xmlFile)throws FileNotFoundException, IOException {
		FileInputStream in = new FileInputStream(xmlFile);
		int bomKount = getBOMCount(xmlFile);
		if (bomKount > 0) {
		byte[] buf = new byte[bomKount];
		in.read(buf);
		}
		return in;
		}
	  
	  
	private int getBOMCount(File xmlFile) throws FileNotFoundException,
  IOException {
  DataInputStream din = new DataInputStream(new FileInputStream(xmlFile));
  int bomKount = 0;
  byte b = din.readByte();
  while (b < 0) {
  bomKount++;
  b = din.readByte();
  }
  din.close();
//   System.out.println("Skipping BOM, " + bomKount + " bytes");
  return bomKount;
  } 
  
  private String getDir(){
	     String dir = parent.getProps().getProperty("readin");
	     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	     return dir;
	   }
  
  
  
  
  
  
  
  public void getAgents(){
	   
	     agentGUID = new HashMap<String, String>();
	     
	     TreeMap tm = new TreeMap();
	     
	 	try {
	 		  File f = new File (getDir() + "Agents.xml");
	 	     
	 	       // System.out.println(getDir() + "Agents.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "AgentsList"); 
	     	
	     	//System.out.println("ridu  " + root.getChildNodes().getLength() + root.toString());
	     	
	         for (int i = 0; i < root.getChildNodes().getLength(); i++){
	         	  Node rida = root.getChildNodes().item(i);
	         	  //System.out.println(rida.getNodeName() + " " );
	         	 if(rida.getNodeName().equals("Agent")){
	         	  try{
	         		 String salesman   = getNode(rida,"Code").getFirstChild().getNodeValue();
	         		 String salesmanN   = getNode(rida,"Name").getFirstChild().getNodeValue();
	         		 salesman = Converter.deCode(salesman);
	         		 Agent sm = new Agent (salesman);
	         		 sm.setName(salesmanN);
	                 tm.put(salesman, sm); 
	               //System.out.println(salesman + " " + sm);
	                 String id =  getNode(rida,"GUID").getFirstChild().getNodeValue();
	                 agentGUID.put(salesman, id);
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	         }
	         	 }
	         
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	   }
  
  
  
  
  
  
  Hashtable custAgents = null;
  private HashMap <String, String> agentGUID = null;
  private int counter = 0;

  public void getCustomers(){
	     customerstm = new TreeMap();
	     custAgents = new Hashtable();
	
		
 	 
 	 try {
	 		  File f = new File (getDir() + "Clients.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "CustomersList"); 
	         
	         for (int i = 0; i < root.getChildNodes().getLength(); i++){
	         	  Node rida = root.getChildNodes().item(i);
	
	         	  if(rida.getNodeName().equals("Customer")){
	         		
	         		 String CustGUID   = null;
	         		 String CustName   = null;		
	         		 String CustCode   = null;
	         		  
	         		  try{
		         		 CustGUID   = getNode(rida,"CustomerGUID").getFirstChild().getNodeValue();
		         		 
		         		try {
		         			CustName   = getNode(rida,"CustomerName").getFirstChild().getNodeValue();		
		         			}catch(Exception k) {}
		         		
		         		 CustCode   = getNode(rida,"CustomerCode").getFirstChild().getNodeValue();
		         		 CustName = Converter.deCode(CustName);
		         		
		         		 //System.out.println(CustGUID + " " +CustName);
		         	  }catch(Exception e){
		         		  e.printStackTrace();
		         	  }
	         	  
      		  	Node addressNode = getNode(rida, "DeliveryAddressList");
      		  	if(addressNode != null){
	      			for (int j = 0; j < addressNode.getChildNodes().getLength(); j++){

		         		  Customer customer = new Customer(CustGUID);
		      		         
		      		         customer.setName(Converter.enCodeEst(CustName));
		      		         
	      				Node ridaAddress = addressNode.getChildNodes().item(j);
	      				
	      				if(ridaAddress.getNodeName().equals("DeliveryAddress")){
	      					 String DAddressGUID   = null;
	    	         		 String DAddressCode   = null;
	    	         		 String DAddress   = null;
	    	         		
	      				
	      				try {
	      					DAddressGUID   = getNode(ridaAddress,"AddressGUID").getFirstChild().getNodeValue();
	      				}catch(Exception k) {}
	      				
	      				try {
	      					DAddressCode   = getNode(ridaAddress,"AddressCode").getFirstChild().getNodeValue();
	      				}catch(Exception k) {}
	      				
	      				try {
	      					DAddress   = getNode(ridaAddress,"Address").getFirstChild().getNodeValue();
	      				}catch(Exception k) {}
	      				
	      		       
	      		         if(DAddressGUID != null ) {
	      		        	 customer.setPayer(DAddressGUID);
	      		         }
	      		         customer.setName(Converter.enCodeEst(CustName) + "_" + DAddressCode);
	      		         customer.setAddress(Converter.enCodeEst(Converter.adressLink(DAddress,"Latvia")));
	      		         customerstm.put(CustCode + "_" + DAddressCode, customer); 
	      				}
	      			}
	      			
      		  	} else {

	         		  Customer customer = new Customer(CustGUID);
	      		         customer.setName(Converter.enCodeEst(CustName));
      		  	 customerstm.put(CustCode + "_0" , customer); 
      		  	}
			       }
	         	 
	         	}
	         
	      in.close();
          
 	 }catch(Exception k ){
				k.printStackTrace();
			}
 	 
 	}
  
  
  TreeMap customerstm = null;
  
  
  
  
  private Hashtable prodNames = null;
  TreeMap prodtm = null;
  

  public void getProducts(){
    prodtm = new TreeMap();
        String ProdGUID=null;
	    String ProdCode=null;   
		
		try {
			  File f = new File (getDir() + "Product.xml");
		     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		     DocumentBuilder db;
			db = dbf.newDocumentBuilder();      	
			FileInputStream in = getCleanInputStream(f);
			String handler = null;
		    Document doc = db.parse(in,handler);
		    Node root = getNode(doc, "ProductsList"); 
		    
		    for (int i = 0; i < root.getChildNodes().getLength(); i++){
		    	  Node rida = root.getChildNodes().item(i);

		    	  if(rida.getNodeName().equals("Product")){
		    		  try{
		    			  
if(getNode(rida,"Code") != null && getNode(rida,"Code").getFirstChild() != null) {
		        		  ProdCode   = getNode(rida,"Code").getFirstChild().getNodeValue();
		        		  ProdGUID   = getNode(rida,"GUID").getFirstChild().getNodeValue();
						prodtm.put(ProdCode, ProdGUID);
}

		    		  }catch(Exception e){
		        		  e.printStackTrace();
		        	  }
			       }
		    	}
		    in.close();
		}catch(Exception k ){
				k.printStackTrace();
		}
  }
  
  
  
  
  HashMap names = null;
  HashMap ilns = null;
  HashMap ilnsC = null;

	private void makeOutput(Order order) {

	  if(customerstm == null) {
		  getCustomers();
	  }
	  if(agentGUID == null) {
		  getAgents();
	  }
	  
	  if(prodtm == null) {
		  getProducts();
	  }
	  
	  

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();

			Node docheader = doc.createElement("Order");
			doc.appendChild(docheader);
			
			String number = order.getDokNr();
			if (number == null || number.length() == 0) {
				number = (Calendar.getInstance().getTimeInMillis() + "").substring(5);
			}
			
			Node un2 = doc.createElement("Number");
			Text unVal2 = doc.createTextNode(number);
			un2.appendChild(unVal2);
			docheader.appendChild(un2);

			try {

				Calendar cal = Calendar.getInstance();

				String month = (cal.get(Calendar.MONTH) + 1) + "";
				if (month.length() == 1)
					month = "0" + month;
				String day = cal.get(Calendar.DAY_OF_MONTH) + "";
				if (day.length() == 1)
					day = "0" + day;
				String hour = cal.get(Calendar.HOUR_OF_DAY) + "";
				if (hour.length() == 1)
					hour = "0" + hour;
				String minute = cal.get(Calendar.MINUTE) + "";
				if (minute.length() == 1)
					minute = "0" + minute;

				String second = cal.get(Calendar.SECOND) + "";
				if (second.length() == 1)
					second = "0" + second;
				String year = cal.get(Calendar.YEAR) + "";
				if (year.length() == 2) {
					year = "20" + year;
				}
				String dateString = cal.get(Calendar.YEAR) + "-" + month + "-" + day + "T" + hour + ":" + minute + ":"
						+ second;
				
				Node un4 = doc.createElement("Date");
				Text unVal4 = doc.createTextNode(dateString);
				un4.appendChild(unVal4);
				docheader.appendChild(un4);
				
				Calendar delcal = Calendar.getInstance();
				delcal.setTime(order.getShipCal().getTime());

				month = (delcal.get(Calendar.MONTH) + 1) + "";
				if (month.length() == 1)
					month = "0" + month;
				day = delcal.get(Calendar.DAY_OF_MONTH) + "";
				if (day.length() == 1)
					day = "0" + day;
				year = delcal.get(Calendar.YEAR) + "";
				if (year.length() == 2) {
					year = "20" + year;
				}
				dateString = year + "-" + month + "-" + day + "T00:00:00";
				
				Node un5 = doc.createElement("DeliveryDate");
				Text unVal5 = doc.createTextNode(dateString);
				un5.appendChild(unVal5);
				docheader.appendChild(un5);
				

				Node age = doc.createElement("AgentGUID");
				Text ageVal = doc.createTextNode(agentGUID.get(order.getAgentCode()));
				age.appendChild(ageVal);
				docheader.appendChild(age);

				
				/*Node pricetypeGUID = doc.createElement("PriceTypeGUID");
				Text pricetypeGUIDVal = doc.createTextNode("hardcode-f2f8-11e8-8430-42358369fa142");
				pricetypeGUID.appendChild(pricetypeGUIDVal);
				docheader.appendChild(pricetypeGUID);*/
				
				/*Node sum = doc.createElement("Sum");
				Text sumVal = doc.createTextNode("");
				sum.appendChild(sumVal);
				docheader.appendChild(sum);*/
				
				Node currency = doc.createElement("Currency");
				Text currencyVal = doc.createTextNode("EUR");
				currency.appendChild(currencyVal);
				docheader.appendChild(currency);
				
				String shipTime = "";

				if(order.getShipTime() != null && order.getShipTime().trim().length() > 0 ) {
					shipTime = " Pieg.Laiks: " + order.getShipTime();
				}
				
				if(order.getInfo() != null && order.getInfo().trim().length() > 0 ) {
					Node comment = doc.createElement("Comment");
					Text commentVal = doc.createTextNode(order.getInfo() + shipTime);
					comment.appendChild(commentVal);
					docheader.appendChild(comment);
				} else if (shipTime.length() > 0) {
					Node comment = doc.createElement("Comment");
					Text commentVal = doc.createTextNode(shipTime);
					comment.appendChild(commentVal);
					docheader.appendChild(comment);
				}
				
				//if(order.getShipMode().equals("-3")) {
					// tellitud vedu endale.
				//}
				
				Customer cust = ((Customer)customerstm.get( order.getCompany()));
				System.out.println("asi " + order.getCompany() + " " + cust + " " + cust.getPayer());
				
				Node customer = doc.createElement("Customer");
				Node custGUID = doc.createElement("CustomerGUID");
				Text custGUIDVal = doc.createTextNode(cust.getID()+"");
				custGUID.appendChild(custGUIDVal);
				customer.appendChild(custGUID);
				docheader.appendChild(customer);
				
				Node custName = doc.createElement("Name");
				Text custNameVal = doc.createTextNode(Converter.deCode(cust.getName()));
				custName.appendChild(custNameVal);
				customer.appendChild(custName);
				
				Node custRegNr = doc.createElement("RegNumber");
				Text custRegNrVal = doc.createTextNode(cust.getRegnr());
				custRegNr.appendChild(custRegNrVal);
				customer.appendChild(custRegNr);
				
				Node custDelAddr = doc.createElement("DeliveryAddress");
				Text custDelAddrVal = doc.createTextNode(Converter.deCode(cust.getAddress()));
				custDelAddr.appendChild(custDelAddrVal);
				customer.appendChild(custDelAddr);
				

				Node DelAddrGUID = doc.createElement("DeliveryAddressGUID");
				Text DelAddrGUIDVal = doc.createTextNode(cust.getPayer()+"");
				DelAddrGUID.appendChild(DelAddrGUIDVal);
				customer.appendChild(DelAddrGUID);


				Node products = doc.createElement("Products");
				docheader.appendChild(products);
				
				for(int x=0; x<order.getOrderLines().size(); x++) {
					OrderLine ll = (OrderLine) order.getOrderLines().get(x);
					Node line = doc.createElement("Line");
					products.appendChild(line);
					
					Node ProdNrNode = doc.createElement("Nr");
					Text ProdNr = doc.createTextNode((x + 1) + "");
					
					Node ProdGUIDNode = doc.createElement("ProductGUID");
					Text ProdGUID = doc.createTextNode(prodtm.get(ll.getToode())+"");
					
					Node ProdCodeNode = doc.createElement("ProductCode");
					Text ProdCode = doc.createTextNode(ll.getToode());
					
					Node ProdQtyNode = doc.createElement("Qty");
					Text ProdQty = doc.createTextNode(ll.getKogus()+"");
					
					
					
					Node ProdVatNode = doc.createElement("VatPercent");
					Text ProdVat = doc.createTextNode("21");
					

					
					ProdNrNode.appendChild(ProdNr);
					ProdGUIDNode.appendChild(ProdGUID);
					ProdCodeNode.appendChild(ProdCode);
					ProdQtyNode.appendChild(ProdQty);
					
					ProdVatNode.appendChild(ProdVat);
					//ProdSumNode.appendChild(ProdSum);
					
					line.appendChild(ProdNrNode);
					line.appendChild(ProdGUIDNode);
					line.appendChild(ProdCodeNode);
					line.appendChild(ProdQtyNode);
					if(ll.getDiscount()>0) {
						Node ProdPriceNode = doc.createElement("Discount");
						Text ProdPrice = doc.createTextNode(ll.getDiscount()+"");
						
						ProdPriceNode.appendChild(ProdPrice);
						line.appendChild(ProdPriceNode);
					}
					line.appendChild(ProdVatNode);
					//line.appendChild(ProdSumNode);
					counter++;
					addNumLine();
				}
				
				
				
				
				
				
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			

			String fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr().trim() + ".xml";
			if(order.getStatus() == 1) {
				fileName = parent.getProps().getProperty("conv.diroffer") + order.getDokNr().trim() + ".xml";
			}
			System.out.println(fileName);
			OutputFormat of = new OutputFormat();
			of.setIndenting(true);
			of.setLineSeparator(System.getProperty("line.separator"));
			of.setEncoding("ISO-8859-1");
			XMLSerializer ser = new XMLSerializer(of);
			FileOutputStream fos = new FileOutputStream(fileName);
			PrintWriter out = new PrintWriter(fos);
			ser.setOutputCharStream(out);
			ser.serialize(doc);
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.exit(0);
	}


	public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}

	private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);


  public String getDriverVersion(){
    return "S1XMLRadexImport";
  }

  
  public boolean isAmount(){
          return isAmount;
  }

  public void setAmount(boolean aAmount){
           isAmount= aAmount;
  }


  private ImportPanel parent;
  private boolean isAmount = false;


}
