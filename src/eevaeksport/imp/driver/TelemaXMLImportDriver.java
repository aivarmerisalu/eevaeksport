
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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

public class TelemaXMLImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public TelemaXMLImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      
    
   
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }


  public String identifyCustomer(String id,String regnr) throws SQLException {
  	
    return null;
  }

 
  public String identifyCustomerAGENT(String id) throws SQLException {
 	
   return id;

 }
 
  


  
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
    return null;
  }

  private String getCustName(String cuid) throws SQLException{
		 String name = null;
	    	return name ;
	  }

  
  public void makeOrder(Order order) throws SQLException{
  counter = 0;
	 		 makeOutputTelema(order);
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
  
  public TreeMap getCustomers(Agent agent){
	     TreeMap tm = new TreeMap();
	   
 	 try {
 		  File f = new File (getDir() + "partner.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "E-Document"); 
	         Node dokument = getNode(root, "Document"); 
	         Node dokumentitem = getNode(dokument, "DocumentItem"); 
	         
	         for (int i = 0; i < dokumentitem.getChildNodes().getLength(); i++){
	        	 
	        	 ArrayList ags = new ArrayList();
	    		 String CustName   = null;	
	    		 String CustJurName   = null;
	    		 String VATCode   = null;
	    		 String REGCode   = null;
	    		 String OverDue = null;
	    		 String CustCode   = null;
	    		 String CustLegalAddres   = null;
	    		 String CustActualAddres   = null;
	    		 String CustPaymentTerm   = null;
	    		 String CustCreditLimit   = null;
	    	
	    		 String info = null;
	    		 
	         	  Node rida = dokumentitem.getChildNodes().item(i);
	         	
	         	  if(rida.getNodeName().equals("PartnerEntry")){
	         		  
	         		  
	         		 Customer customer = new Customer(CustCode);
	       		  	
	         		 for (int t = 0; t < rida.getChildNodes().getLength(); t++){
	         			 Node rid = rida.getChildNodes().item(t);
	         			// System.out.println("ag " + rid.getNodeName());
	         			 if(rid.getNodeName().equals("UserID")){
	         				 String ag = rid.getFirstChild().getNodeValue();
	         				 if(!ags.contains(ag)) {
	         			    	 ags.add(ag);
	         				 }
	         			 }
	         		 }
	         		  
	         		  try{
		         		 CustCode   = getNode(rida,"PartnerCode").getFirstChild().getNodeValue();
		         		 customer = new Customer(CustCode);
		         		try{
			         		 CustName   = getNode(rida,"Name").getFirstChild().getNodeValue();		
		         		}catch(Exception e){}
		         	//	System.out.println(CustName + " " );
		         		try{
		         			Node aadre = getNode(rida,"PartnerAddress");
		         			String aad1 =  "";
		         			if(getNode(aadre,"Address1") != null && getNode(aadre,"Address1").getFirstChild() != null) {
		         				aad1 = getNode(aadre,"Address1").getFirstChild().getNodeValue()+" ";
		         				customer.restStreet = aad1;
		         			}
		         			String aad2 =  "";
		         			if(getNode(aadre,"Address2") != null && getNode(aadre,"Address2").getFirstChild() != null) {
		         				aad2 = getNode(aadre,"Address2").getFirstChild().getNodeValue()+" ";
		         			}
		         			String city =  "";
		         			if(getNode(aadre,"City") != null) {
		         				city = getNode(aadre,"City").getFirstChild().getNodeValue()+" ";
		         				customer.restCity = city;
		         			}
		         			String county =  "";
		         			if(getNode(aadre,"County") != null) {
		         				county = getNode(aadre,"County").getFirstChild().getNodeValue() +" ";
		         			}
		         			String aad3 =  "";
		         			if(getNode(aadre,"PostalCode") != null && getNode(aadre,"PostalCode").getFirstChild() != null) {
		         				aad3 = getNode(aadre,"PostalCode").getFirstChild().getNodeValue();
		         				customer.setZipcode(aad3);
		         			}
		         	
		         			CustActualAddres = (aad1 +  aad2 +  city  + county  +  aad3).trim();
		         		}catch(Exception e){e.printStackTrace();}
		         	
		         		try{
		         			
		         			Node comp = getNode(rida,"CompanyInfo");
		         			
		         		
		         			if(getNode(comp,"Name") != null) {
		         				CustJurName = getNode(comp,"Name").getFirstChild().getNodeValue();
		         			}
		         			
		         			if(getNode(comp,"RegNum") != null && getNode(comp,"RegNum").getFirstChild() != null) {
		         				REGCode = getNode(comp,"RegNum").getFirstChild().getNodeValue().trim();
		         			}
		         			
		         			if(getNode(comp,"VATRegNum") != null && getNode(comp,"VATRegNum").getFirstChild() != null) {
		         				VATCode = getNode(comp,"VATRegNum").getFirstChild().getNodeValue().trim();
		         			}
		         			
		         			Node contr = getNode(comp,"ContractInfo");
		         			
		         			if(getNode(contr,"PaymentTerm") != null && getNode(contr,"PaymentTerm").getFirstChild() != null) {
		         				CustPaymentTerm = getNode(contr,"PaymentTerm").getFirstChild().getNodeValue();
		         			}
		         			
		         			if(getNode(contr,"CreditLimit") != null && getNode(contr,"CreditLimit").getFirstChild() != null) {
		         			    CustCreditLimit = getNode(contr,"CreditLimit").getFirstChild().getNodeValue();
		         			}
		         			Node bala = getNode(comp,"BalanceInfo");
		         			if(getNode(bala,"OutstandingAmount") != null) {
		         			    OverDue = getNode(bala,"OutstandingAmount").getFirstChild().getNodeValue();
		         			}
		         			
		         			Node aadre = getNode(comp,"CompanyAddress");
		         		
		         			String aad1 =  "";
		         			if(getNode(aadre,"Address1") != null && getNode(aadre,"Address1").getFirstChild() != null) {
		         				aad1 = getNode(aadre,"Address1").getFirstChild().getNodeValue()+" ";
		         			}
		         			String aad2 =  "";
		         			if(getNode(aadre,"Address2") != null && getNode(aadre,"Address2").getFirstChild() != null) {
		         				aad2 = getNode(aadre,"Address2").getFirstChild().getNodeValue()+" ";
		         			}
		         			String city =  "";
		         			if(getNode(aadre,"City") != null) {
		         				city = getNode(aadre,"City").getFirstChild().getNodeValue()+" ";
		         			}
		         			String county =  "";
		         			if(getNode(aadre,"County") != null && getNode(aadre,"County").getFirstChild() != null) {
		         				county = getNode(aadre,"County").getFirstChild().getNodeValue() +" ";
		         			}
		         			String aad3 =  "";
		         			if(getNode(aadre,"PostalCode") != null && getNode(aadre,"PostalCode").getFirstChild() != null) {
		         				aad3 = getNode(aadre,"PostalCode").getFirstChild().getNodeValue();
		         			}
		         	
		         			CustLegalAddres = (aad1 +  aad2 +  city  + county  +  aad3).trim();
		         		}catch(Exception e){e.printStackTrace();}
		         	//	System.out.println("Legal " + CustLegalAddres );
		         	
		         		
		         		 CustName = Converter.deCode(CustName);
		         		
		         		 //System.out.println(CustGUID + " " + CustName + " " + CustAgentGUID + " " + agentGUID.get(CustAgentGUID));
		         	  }catch(Exception e){
		         		  e.printStackTrace();
		         	  }
	         	
	         		
	         			  
      		  		
      		  		
      		  		
      		  		if(CustName != null){
	      		         customer.setName(Converter.enCodeEst(CustName));
	      		         
	      		       if(CustLegalAddres != null){
	      		        	customer.setJuridics(CustLegalAddres);
	      		         }
	      		         
	      		       
	      		         if(CustActualAddres != null){
	      		        	customer.setAddress(CustActualAddres);
	      		         }else {
	      		        	customer.setAddress(CustLegalAddres);
	      		         }
	      		         
	      		       if(REGCode != null){
	      		        	customer.setRegnr(REGCode);
	      		         }
	      		       
	      		     if(VATCode != null){
	      		        	customer.setVatnr(VATCode);
	      		         }
	      		    
		      		       if(CustPaymentTerm != null){
		      		        	customer.setPaymTerm(CustPaymentTerm);
		      		         }
		      		       
		      		     if(CustCreditLimit != null && !CustCreditLimit.equals("0")){
		      		        	customer.setCredit(CustCreditLimit);
		      		         }
		      		     
		      		     if(OverDue != null && !OverDue.equals("0")) {
		      		    	 customer.setDebt(Double.parseDouble(OverDue));
		      		     }
		      		     
		      		     if(info != null){
		      		    	customer.setPersonalData(info);
		      		     }
		      		     
		      		     if(ags.size() > 0) {
		      		    	 for(int u = 0; u<ags.size();u++) {
		      		    		 customer.addAgent(new Agent(ags.get(u)+""));
		      		    	 }
		      		     }
		      		     
		      		     
		      		     tm.put(customer.getID(), customer);
		      		      }
      		  	
			       }
	         	 
	         	}
	         
          in.close();
          
 	 }catch(Exception k ){
				k.printStackTrace();
			}
 	 
 	 
 	 System.out.println("kliendid " + tm.size());
 	  
 	 
      return tm;
    }
    
  
private  TreeMap<String, Double> getHinnad(){
	TreeMap<String, Double> re = 	new TreeMap<String, Double>();
	try {
		 File f = new File (getDir() + "pricat.xml");
	       // System.out.println(getDir() + "Agents.xml");
	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	     DocumentBuilder db;
		 db = dbf.newDocumentBuilder();      	
    	 FileInputStream in = getCleanInputStream(f);
    	 String handler = null;
        Document doc = db.parse(in,handler);
        Node root = getNode(doc, "E-Document"); 
        Node dokument = getNode(root, "Document"); 
        Node dokumentitem = getNode(dokument, "DocumentItem"); 
        
        for (int i = 0; i < dokumentitem.getChildNodes().getLength(); i++){
	          Node rida = dokumentitem.getChildNodes().item(i);
	    	  if(rida.getNodeName().equals("ItemPartyEntry")){
	    	        String ProdCode = null;
			        String ProdPrice = null;
				  try{
					  ProdCode   = getNode(rida,"SellerItemCode").getFirstChild().getNodeValue();
	    		  }catch(Exception e){e.printStackTrace();}
				  try{
					  ProdPrice   = getNode(rida,"ItemPartyPrice").getFirstChild().getNodeValue();
	    		  }catch(Exception e){e.printStackTrace();}
				  
				  try {
					  re.put(ProdCode, Double.parseDouble(ProdPrice));
				  }catch(Exception e) {
					  
				  }
	    	  }
        }
	   in.close();
	}catch(Exception e) {
		
	}
	return re;
}
  
  public TreeMap getProducts(Agent agent){
	     TreeMap tm = new TreeMap();
	     
	     TreeMap<String, Double> hinnad = getHinnad() ;
			try {
				 File f = new File (getDir() + "prodat.xml");
		 	       // System.out.println(getDir() + "Agents.xml");
		 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		 	     DocumentBuilder db;
		 		 db = dbf.newDocumentBuilder();      	
		     	 FileInputStream in = getCleanInputStream(f);
		     	 String handler = null;
		         Document doc = db.parse(in,handler);
		         Node root = getNode(doc, "E-Document"); 
		         Node dokument = getNode(root, "Document"); 
		         Node dokumentitem = getNode(dokument, "DocumentItem"); 
		         
		         for (int i = 0; i < dokumentitem.getChildNodes().getLength(); i++){
			     
			
			       
			    	
			    	  Node rida = dokumentitem.getChildNodes().item(i);
			    	  
			    	
			    	  
			    	  if(rida.getNodeName().equals("GroupEntry")){
			    		  
					        String ProdGroupsCode = null;
					        String ProdGroupsDescription = null;
					 
					       
			    		  try{
			    			  ProdGroupsCode   = getNode(rida,"GroupCode").getFirstChild().getNodeValue();
			    		  }catch(Exception e){e.printStackTrace();}
			    		  
			    		  try{
			    			  Node groupDescriptions = getNode(rida,"GroupDescription");
			    			  ProdGroupsDescription =  groupDescriptions.getFirstChild().getNodeValue();
				    		   
			    	    	  }catch(Exception e){e.printStackTrace();}
			    		  
			    					
			    	  	}
			    	  }
			    		  
			    in.close();
				}catch(Exception k ){
					k.printStackTrace();
				}
	 
	     
			//Product.xml

	     
			
			try {
				StringBuffer sbEAN = new StringBuffer();
				 File f = new File (getDir() + "prodat.xml");
		 	       // System.out.println(getDir() + "Agents.xml");
		 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		 	     DocumentBuilder db;
		 		 db = dbf.newDocumentBuilder();      	
		     	 FileInputStream in = getCleanInputStream(f);
		     	 String handler = null;
		         Document doc = db.parse(in,handler);
		         Node root = getNode(doc, "E-Document"); 
		         Node dokument = getNode(root, "Document"); 
		         Node dokumentitem = getNode(dokument, "DocumentItem"); 
		         
		         for (int i = 0; i < dokumentitem.getChildNodes().getLength(); i++){
			     
			    	  Node rida = dokumentitem.getChildNodes().item(i);

					   	 String ProdCode = null;      
					   	 String ProdDesc = null;   
					   	 String ProdUnit = null;  
					   	 String GroupGUID = null;
					   	 String UnitPrice = null;  
					   	 String EANKood = null; 
					   	 String incase = null;
			    	
			    	  
			    	  if(rida.getNodeName().equals("ItemEntry")){
			    	  
			    	  //System.out.println(rida.getNodeName() + " " );
			    		 /*  <PublicItem>yes</PublicItem>
			    		  <ItemUnitRecord>
			    		  <ItemUnit>KARP</ItemUnit>
			    		  <UnitNumerator>200</UnitNumerator>
			    		  <UnitDenumerator>1</UnitDenumerator>
			    		  </ItemUnitRecord>
			    		  <ItemUnitRecord>
			    		  <ItemUnit>TK</ItemUnit>
			    		  <UnitNumerator>1</UnitNumerator>
			    		  <UnitDenumerator>1</UnitDenumerator>
			    		  </ItemUnitRecord>
			    		  <BaseUnit>TK</BaseUnit>*/
			    		  try{
			    			  ProdCode   = getNode(rida,"SellerItemCode").getFirstChild().getNodeValue();
			    		  }catch(Exception e){e.printStackTrace();}
			    		  
			    		  try{
			    			  
			    			  for (int w = 0; w < rida.getChildNodes().getLength(); w++){
			    				  Node rid = rida.getChildNodes().item(w);
			    				  if(rid.getNodeName().equals("ItemUnitRecord")){
			    					 if( getNode(rid,"ItemUnit").getFirstChild().getNodeValue().equals("KARP")) {
			    						 incase = getNode(rid,"UnitNumerator").getFirstChild().getNodeValue();
			    						// System.out.println(rida.getNodeName() + " " + incase );
			    					 }
			    					 
			    				  }
			    			  
			    			  }
			    		  }catch(Exception e){e.printStackTrace();}
			    		  
			    	
			    		  try{
			    			  ProdDesc   = getNode(rida,"ItemDescription").getFirstChild().getNodeValue();
			    		  }catch(Exception e){e.printStackTrace();}
			    		  
			    		  try{
			    			  ProdUnit   = getNode(rida,"BaseUnit").getFirstChild().getNodeValue();
			    		  }catch(Exception e){e.printStackTrace();}
			    		  
			    		
			    	
			    		  try{
			    			  GroupGUID   = getNode(rida,"ItemGroupCode").getFirstChild().getNodeValue();
			    		  }catch(Exception e){e.printStackTrace();}
			    		    
			    		  
			    		  try{
			    			  EANKood   = getNode(rida,"GTIN").getFirstChild().getNodeValue();
			    		  }catch(Exception e){e.printStackTrace();}
			    		      
			        		  //GroupGUID2   = getNode(rida,"GroupSecGUID").getFirstChild().getNodeValue();
			        		  
			        		  
			        		  
			        		 //System.out.println(CustGUID + " " +CustName);
			        	  
			    		 
			    		  
			    		 Product prod = new Product(ProdCode);
			    		
			    		 
			    		 if(hinnad.get(ProdCode) != null) {
			    			 prod.setPrice(hinnad.get(ProdCode));
			    		 }
			    		 
			   			 try {
			   			if(Double.parseDouble(incase) > 1) {
			   				 prod.setInCases((int)Double.parseDouble(incase));
			   			 }
			   			 }catch(Exception j) {
			   				 
			   			 }
			   			 
			   			 if(ProdUnit.length() > 0) {   
			   				 prod.setUnit(ProdUnit);
			   			 }
			   			 if(GroupGUID != null) {   
			   				prod.setGroup(GroupGUID);
			   			 }
			   			  
			   		
			   			 if(ProdDesc.length() > 0) {
			   				 prod.setName(Converter.enCodeEst(ProdDesc ));
			   				
			   			 }
			   			 if(EANKood.length() > 0) {
			   			 prod.setEAN(EANKood);
			   			 }
			   			 try {
		   				 if(Double.parseDouble(UnitPrice) > 0) {
		   					 prod.setPrice(Double.parseDouble(UnitPrice));
		   				 }
			   			 }catch(Exception e) {
			   				 
			   			 }
		   				 
		   				prod.setVat("20");

			    	
			 			
			 				tm.put( prod.getID(), prod);
			 			 

				       }
			    	}
			    
			    in.close();
			}catch(Exception k ){
					k.printStackTrace();
				}
			return tm;

	   }

	   

  TreeMap prods = null;
  TreeMap custs = null;
  
  private void makeOutputTelema(Order order){
	  if(custs == null) {
		  custs = this.getCustomers(null);
	  }
	  if(prods == null) {
		  prods = this.getProducts(null);
	  }
	 StringBuffer sb = new StringBuffer();
	 nf.setGroupingUsed(false);

	try {
		
	  String ref = order.getTeostaja();
		if ( ref == null ){
			ref =  " ";
		}
	
		String com = order.getEmailBody();
		if ( com == null ){
			com =  " ";
		}		
		
		String ware = order.getWarehouse();
		System.out.println("ladu tellimusel " + ware);
		
		 String doknr = order.getDokNr();
      String ls = System.getProperty("line.separator");
      String fixagent = order.getAgent(); 
  
      String custID = order.getCompany();
      String ladu = null;
          Calendar  shipDate = order.getShipCal();
          
          String code = order.getWarehouse();        
          if (code != null){
            ladu = code;
          }
        
        if (ladu == null) {
          ladu = "";
        }
     
        Customer cust = null;
        if(custs.get(order.getCompany()) != null) {
        	cust = (Customer)custs.get(order.getCompany());
        }
   
    
        Calendar cal = Calendar.getInstance();
       
        if (shipDate != null) {
              cal = shipDate;
            }
      
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + ls);
        sb.append("<E-Document>" + ls);
   
        sb.append("<Header>" + ls);
        sb.append("<DateIssued>" + escape(getDateString(Calendar.getInstance())) + "</DateIssued>" + ls);
        sb.append("<SenderID>-</SenderID>" + ls);
        sb.append("<ReceiverID>-</ReceiverID>" + ls); 
        sb.append("<Charset>UTF-8</Charset>" + ls);
        sb.append("<Channel>MMT</Channel>" + ls);
        sb.append("</Header>" + ls);
        
        sb.append("<Document>" + ls);
        sb.append("<DocumentType>invoice</DocumentType>" + ls);
        sb.append("<DocumentFunction>ORIGINAL</DocumentFunction>" + ls);
        
        sb.append("<DocumentParties>" + ls);
      	   sb.append("<BuyerParty>" + ls);
           sb.append( "<PartyCode>" +  escape((custID)) + "</PartyCode>" + ls); 
           
           if(cust != null) {
           sb.append( "<Name>" + cust.getName() + "</Name>" + ls);
           if(cust.getRegnr() != null && cust.getRegnr().trim().length() > 0) {
           sb.append( "<RegNum>" +  cust.getRegnr() + "</RegNum>" + ls); 
           }
           if(cust.getVatnr() != null && cust.getVatnr().trim().length() > 0) {
           sb.append( "<VATRegNum>" +  cust.getVatnr() + "</VATRegNum>" + ls); 
           }}
           
           sb.append("<ContactData>" + ls);
           sb.append("<ActualAddress>" + ls);
           sb.append( "<Address1>" +  cust.restStreet + "</Address1>" + ls); 
           String city = "";
           if(cust.restCity != null) {
        	   city = cust.restCity;
           }
           sb.append( "<City>" +  city + "</City>" + ls); 
           sb.append( "<PostalCode>" +  cust.getZipcode() + "</PostalCode>" + ls); 
           sb.append( "<County></County>" + ls);
           sb.append( "<Country></Country>" + ls);
           sb.append( "<CountryCode></CountryCode>" + ls);
   
           sb.append("</ActualAddress>" + ls);
           sb.append("</ContactData>" + ls);
           
           
           sb.append("</BuyerParty>" + ls);
        
           sb.append("<DeliveryParty>" + ls);
           sb.append( "<PartyCode>" +  escape((custID)) + "</PartyCode>" + ls); 
           
           if(cust != null) {
           sb.append( "<Name>" + cust.getName() + "</Name>" + ls);
           if(cust.getRegnr() != null && cust.getRegnr().trim().length() > 0) {
           sb.append( "<RegNum>" +  cust.getRegnr() + "</RegNum>" + ls); 
           }
           if(cust.getVatnr() != null && cust.getVatnr().trim().length() > 0) {
           sb.append( "<VATRegNum>" +  cust.getVatnr() + "</VATRegNum>" + ls); 
           }}
           
           sb.append("<ContactData>" + ls);
           sb.append("<ActualAddress>" + ls);
           sb.append( "<Address1>" +  cust.getAddress() + "</Address1>" + ls); 
      
   
           sb.append("</ActualAddress>" + ls);
           sb.append("</ContactData>" + ls);
           
      String agent = order.getAgent();
        if(fixagent != null){
      	  agent = fixagent;
        }
        
        sb.append("<UserId>" +  agent + "</UserId>" + ls);
        sb.append("</DeliveryParty>" + ls);
        
        sb.append("<SellerParty>\r\n" + 
        		"        <PartyCode>-</PartyCode>\r\n" + 
        		"        <Name>TOP CANDY OÜ</Name>\r\n" + 
        		"        <RegNum>10355626</RegNum>\r\n" + 
        		"        <VATRegNum>EE100042007</VATRegNum>\r\n" + 
        		"        <ContactData>\r\n" + 
        		"        <PhoneNum>7362487</PhoneNum>\r\n" + 
        		"        <FaxNum>7362456</FaxNum>\r\n" + 
        		"        <EmailAddress>info@topcandy.ee</EmailAddress>\r\n" + 
        		"        <ActualAddress>\r\n" + 
        		"        <Address1>Aardla 84a</Address1>\r\n" + 
        		"        <City>Tartu</City>\r\n" + 
        		"        <PostalCode>50414</PostalCode>\r\n" + 
        		"        <County>Tartu</County>\r\n" + 
        		"        <CountryCode>EE</CountryCode>\r\n" + 
        		"        </ActualAddress>\r\n" + 
        		"        </ContactData>\r\n" + 
        		"        <AccountInfo>\r\n" + 
        		"        <AccountNum>EE142200221011371691</AccountNum>\r\n" + 
        		"        <IBAN>EE142200221011371691</IBAN>\r\n" + 
        		"        <BankName>Swedbank</BankName>\r\n" + 
        		"        </AccountInfo>\r\n" + 
        		"        </SellerParty>" + ls);
        
        
        sb.append("</DocumentParties>" + ls);
        sb.append("<DocumentInfo>" + ls);
        
       
        
        sb.append("<DocumentSubType>DEB</DocumentSubType>" + ls);
        sb.append("<DocumentName>invoice</DocumentName>" + ls );
        sb.append("<PaymentMethod>Pank</PaymentMethod>" + ls );
        sb.append("<PaymentTerm>" + cust.getPaymTerm() + "</PaymentTerm>" + ls );
       
        
        
      	  sb.append("<RefInfo>" + ls);
      	  if(ref != null && ref.trim().length() > 0){
      		
      		  
            sb.append("<SourceDocument type=\"order\">" + ls);
            sb.append("    <SourceDocumentNum>" + escape(ref) + "</SourceDocumentNum>" + ls );
            sb.append("</SourceDocument>" + ls);
      	  }
            sb.append("</RefInfo>" + ls); 

        
    String    sss = (Calendar.getInstance().getTime().getTime()+ "").substring(4) ;
    
    if(ref != null && ref.trim().length() > 0){
 	   sb.append("<DocumentNum>" + escape(ref)+ "</DocumentNum>" + ls); 
    }else {
 	   sb.append("<DocumentNum>" + escape(agent + "-" + sss )+ "</DocumentNum>" + ls);   
    }
       
        sb.append("<CreatedBy>" + escape(agent)+ "</CreatedBy>" + ls); 
        
        sb.append("<DateInfo>" + ls);
        sb.append("<InvoiceDate>" +escape(getDateString(Calendar.getInstance())) + "</InvoiceDate>" + ls);
        sb.append("<DeliveryDateActual>" +  escape(getDateString(Calendar.getInstance())) + "</DeliveryDateActual>" + ls); //aeg taha panna  T10:00:00
        sb.append("</DateInfo>" + ls);
        
        sb.append("<CreatedByContact>" + ls);
       
          if(order.getAgent().equals("M09")) {
	        sb.append("<ContactFirstName>Eda</ContactFirstName>" + ls);
	        sb.append("<ContactLastName>Raal</ContactLastName>" + ls); 
	        sb.append("<PhoneNum>56565219</PhoneNum>" + ls); 
	       } else {
        	 sb.append("<ContactFirstName>Elle</ContactFirstName>" + ls);
             sb.append("<ContactLastName>Vesinurm</ContactLastName>" + ls); 
             sb.append("<PhoneNum>56622929</PhoneNum>" + ls); 
          }
        
        sb.append("<MobileNum>+372 5062 350</MobileNum>" + ls);
        sb.append("</CreatedByContact>" + ls);
        
        sb.append("</DocumentInfo>" + ls);
       
        
        
        sb.append("<DocumentSumGroup>" + ls );
        sb.append("<Currency>EUR</Currency>" + ls);
        
        sb.append("<DocumentSum>" + order.getCalcSum()+ "</DocumentSum>" + ls );
        sb.append("<VAT><SumBeforeVAT>" + order.getCalcSum()+ "</SumBeforeVAT><VATRate>20</VATRate><VATSum>" + Converter.Round((float)(order.getCalcSum() * 0.2), 2) + "</VATSum></VAT>" + ls);
        sb.append("<TotalSum>" + Converter.Round((float) (order.getCalcSum() * 1.2 ) , 2) + "</TotalSum>" + ls);
        sb.append("</DocumentSumGroup>" + ls);  
        
     
        
        sb.append("   <DocumentItem>" + ls);


  int cou  = 0;
        
        Vector sortedLines = Converter.joinLines(order.getOrderLines());
        for (int i = 0; i < sortedLines.size(); i++) {
            OrderLine line = (OrderLine)sortedLines.elementAt(i);
            double ale = line.getDiscount();     
         
            Product prod = (Product) prods.get(line.getToode());
            
            
            String yhik = line.getUnit();
            if (ale == -1 ){
                ale = 0 ;
                }
            sb.append("      <ItemEntry>" + ls);
            sb.append("      <LineItemNum>"  + (i+1) + "</LineItemNum>" + ls);
          
           
           // <GTIN>4740072222752</GTIN> 
           // <BaseUnit>TK</BaseUnit> 
            sb.append("         <SellerItemCode>" + line.getToode() + "</SellerItemCode>" + ls);
            sb.append("         <GTIN>" + prod.getEAN() + "</GTIN>" + ls);
            sb.append("         <ItemDescription>" + prod.getName() + "</ItemDescription>" + ls);
        
           sb.append("         <ItemUnitRecord>" + ls);
           sb.append("                <ItemUnit>" + line.getUnit() + "</ItemUnit>" + ls);
           sb.append("         </ItemUnitRecord>" + ls);

           sb.append("         <BaseUnit>" + line.getUnit() + "</BaseUnit>" + ls);
          
           sb.append("         <AmountInvoiced>" + escape(getNF().format(line.getKogus())) + "</AmountInvoiced>" + ls);
           sb.append("         <ItemPrice>" + escape(getNF().format(line.getPrice())) + "</ItemPrice>" + ls);
           sb.append("         <ItemBasePrice>" + escape(getNF().format(prod.getPrice())) + "</ItemBasePrice>" + ls);
           sb.append("         <ItemSum>" + getNF().format(line.getPrice() * line.getKogus()) + "</ItemSum>" + ls);
           
               String ccc = "";
            if(line.getDiscount() > 0){
          	  double discprice = (100d -line.getDiscount())*line.getPrice()/100d ;
              sb.append("         <Addition addCode=\"DSC\"><AddRate>" +
          	   escape(getNF().format(line.getDiscount())) + "</AddRate></Addition>" + ls); 
              ccc =  "-" + nf.format(line.getDiscount()) + "% ";
            }
           
            sb.append("         <VAT vatID=\"TAX\"><SumBeforeVAT>" + getNF().format(line.getPrice() * line.getKogus()) + "</SumBeforeVAT>" + ls);
            sb.append("<VATRate>20</VATRate>" + ls);
            sb.append("<VATSum>" + getNF().format(line.getPrice() * line.getKogus() * 0.2) + "</VATSum>" + ls);
            sb.append("         </VAT>" + ls);
            

          
            if(line.getPartii() != null && line.getPartii().trim().length() > 0 ){
         	   ccc = ccc + line.getPartii();
            }
            if(ccc.length() > 0) {
                sb.append("   <AdditionalInfo>" +ls);
                sb.append("       <Extension extensionId=\"remarks\">" + ls);
                sb.append("       <InfoName>Notes</InfoName>");
                sb.append("            <InfoContent>" + Converter.escapeXML(ccc) + "</InfoContent>" + ls );
                sb.append("       </Extension>");
                sb.append("   </AdditionalInfo>" + ls);	
            }
     
            sb.append("      </ItemEntry>" + ls);
            cou++;
        }
        

        sb.append("   </DocumentItem>" + ls);
        
  	    String comm = order.getInfo();
          if(comm != null && comm.length()>100){
          	comm = comm.substring(0,100);
          }
          
          if(comm != null && comm.trim().length() > 0 ){
          	  sb.append("   <AdditionalInfo>" +ls);
                sb.append("       <Extension extensionId=\"remarks\">" + ls);
                sb.append("       <InfoName>Notes</InfoName>");
                sb.append("            <InfoContent>" + escape(comm) + "</InfoContent>" + ls );
                sb.append("       </Extension>");
                sb.append("   </AdditionalInfo>" + ls);	
          }
        
          
        sb.append("</Document>" + ls);
        sb.append("<Footer> <TotalNumDocuments>1</TotalNumDocuments></Footer>" + ls);
        
        sb.append("</E-Document>" + ls);

   counter = counter + cou;
 	//
   try{
 	  Thread.sleep(10); 
   }catch(Exception e){
 	 e.printStackTrace(); 
   }
   
   
  String  fileName = escape(parent.getProps().getProperty("conv.dirxml") + "invoice-" + agent) + "-" + counter + "." + sss  +  ".xml";
   System.out.println(fileName);
   Writer out = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream(fileName), "UTF-8"));
			try {
			    out.write(sb.toString());
			} finally {
			    out.close();
			}
			
			 log.error(" File Saved " + fileName );
			 fileName = escape(parent.getProps().getProperty("conv.dircxml") + "invoice-" + agent) + "-" + counter + "." + sss  +  ".xml";
		      System.out.println(fileName);
		       out = new BufferedWriter(new OutputStreamWriter(
					    new FileOutputStream(fileName), "UTF-8"));
					try {
					    out.write(sb.toString());
					} finally {
					    out.close();
					}
					 log.error(" copy Saved " + fileName );
	} catch (Exception e) {
		 log.error(" Output error 1 " + order.getCompany() );
		 log.error(e);
		e.printStackTrace();
	}
}

 
 
  private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    sb.append(cal.get(Calendar.YEAR) + "");
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append("-" +month + "-");

	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day);

	    return sb.toString();
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


 
	public NumberFormat getNF() {
		return this.nf;
	}
	



 
  public String getDriverVersion(){
    return "TelemaXML";
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
	// TODO Auto-generated method stub
	return null;
}
}
