package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ee.mobilejava.earve.GenericDOM;
import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class TelemaXMLDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager,PictureMaker {

  public TelemaXMLDBDriver(EksportPanel parent) {
	super(parent);
	this.subjectFilesTo = parent.getString("filemsg");

	     user =  getParent().getProps().getProperty("rest.user");
	     pass = getParent().getProps().getProperty("rest.pass");
	     rcompany = getParent().getProps().getProperty("rest.companyx");
	     }
  
  
	  String user =  null;
	  String pass = null;
	  String rcompany = null;
  private Hashtable dates = new Hashtable();
   public String getPilotDayData(Agent agent){
     return null;
   }
   
   public String getSaveDebtInfo() {
			StringBuffer products = new StringBuffer();
		return products.toString();
   }
   
    private Vector prlists = null;
   
   private HashMap plistGUID() {
	   HashMap hm = new HashMap();
	   
	 	try {
	 		  File f = new File (getDir() + "Prices.xml");
	 	     
	 	       // System.out.println(getDir() + "Agents.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "PricesList"); 
	     	
	     	//System.out.println("ridu  " + root.getChildNodes().getLength() + root.toString());
	     	
	         for (int i = 0; i < root.getChildNodes().getLength(); i++){
	         	  Node rida = root.getChildNodes().item(i);
	         	 if(rida.getNodeName().equals("Product")){
	         		 
	         	Node pricesNode = getNode(rida, "Prices"); 
	         	for (int j = 0; j < pricesNode.getChildNodes().getLength(); j++){
	         		Node pricetypeNode = pricesNode.getChildNodes().item(j);
	         		if(pricetypeNode.getNodeName().equals("PriceType")) {
	         			String priceTypeGUID   = getNode(pricetypeNode,"PriceTypeGUID").getFirstChild().getNodeValue();
	         			String priceTypeCode   = getNode(pricetypeNode,"PriceTypeCode").getFirstChild().getNodeValue();
	         			hm.put(priceTypeGUID, priceTypeCode);
	         		}
	         	}
	         	  
	         	  
	         }
	         	 }
	         
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	   
	   
	   return hm;
   }
   
   
   public Vector getPriceMatrixData(){
	   
	   Vector result = new Vector();
	  
	   
	   return result;
   }



   public ManagerData getManagerData() throws SQLException{
	   return null;
   }
   public String getProductSortingData(){
   	return null;
   }

 
   public DealerProduct getDealerProductsList() {
       return null;
   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
   
  

	  public String getSaveStockInfo(){
	    	  return null;
	     }


   public Vector getDebt(String agentCode){
	    return null;
  }


   public String getDealerCustomerCSV(String agent){
    return null;
    }

   public String  getCustProdDiscounts(Agent agent){
    return null;
    }

   public TreeMap getProdGroups() throws SQLException {

	          return prodGroups;
   }
   
   
   private TreeMap prodGroups = null;
   
   
   public TreeMap getProdGroupsSecond() throws SQLException {
	   TreeMap   tm = new TreeMap();
	   return tm;
   }

   

	  private Node getElement(Node rt, String s){
	    return GenericDOM.getNode(rt, s);
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

   private HashMap <String, String> agentGUID = null;
   
   public TreeMap getAgents(){
	   
	     TreeMap tm = new TreeMap();
	  
	 	try {
	 		  File f = new File (getDir() + "partner.xml");
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
	     	
	     	//System.out.println("ridu  " + root.getChildNodes().getLength() + root.toString());
	     	
	         for (int i = 0; i < dokumentitem.getChildNodes().getLength(); i++){
	         	  Node rida = dokumentitem.getChildNodes().item(i);
	         	
	         	 if(rida.getNodeName().equals("PartnerEntry")){
	         	  try{
	         		  
	         		//  System.out.println(rida.getNodeName() + " " );
	         		 String salesman   = getNode(rida,"UserID").getFirstChild().getNodeValue();
	         		 String salesmanN   = getNode(rida,"UserID").getFirstChild().getNodeValue();
	         		 salesman = Converter.deCode(salesman);
	         		 Agent sm = new Agent (salesman);
	         		 sm.setIsPilot(true);
	         		 sm.setName(salesmanN);
	                 tm.put(salesman, sm);
	                 agents1.put(sm.getID(), sm);

	                 
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	         }
	         	 }
	         
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	     return tm;
	   }




       public String getDistribution() {
         return null;
       }
       
       
       
       
       private Date getDate(String mmddyy){
    	  	Calendar cal = Calendar.getInstance();
    	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(6, 10)));
    	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(3, 5))-1);
    	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(0, 2)));
    	    return new Date(cal.getTimeInMillis());
    	  }
      

       private TreeMap agents1 = new TreeMap();
       public TreeMap getAgents1() {
    	return agents1;
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
		         		 
		         		try{
			         		 CustName   = getNode(rida,"Name").getFirstChild().getNodeValue();		
		         		}catch(Exception e){}
		         	//	System.out.println(CustName + " " );
		         		try{
		         			Node aadre = getNode(rida,"PartnerAddress");
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
		         			if(getNode(aadre,"County") != null) {
		         				county = getNode(aadre,"County").getFirstChild().getNodeValue() +" ";
		         			}
		         			String aad3 =  "";
		         			if(getNode(aadre,"PostalCode") != null && getNode(aadre,"PostalCode").getFirstChild() != null) {
		         				aad3 = getNode(aadre,"PostalCode").getFirstChild().getNodeValue();
		         			}
		         	
		         			CustActualAddres = (aad1 +  aad2 +  city  + county  +  aad3).trim();
		         		}catch(Exception e){e.printStackTrace();}
		         	
		         		try{
		         			
		         			Node comp = getNode(rida,"CompanyInfo");
		         			
		         		
		         			if(getNode(comp,"Name") != null) {
		         				CustJurName = getNode(comp,"Name").getFirstChild().getNodeValue();
		         			}
		         			
		         			if(getNode(comp,"RegNum") != null && getNode(comp,"RegNum").getFirstChild() != null) {
		         				REGCode = getNode(comp,"RegNum").getFirstChild().getNodeValue();
		         			}
		         			
		         			if(getNode(comp,"VATRegNum") != null && getNode(comp,"VATRegNum").getFirstChild() != null) {
		         				VATCode = getNode(comp,"VATRegNum").getFirstChild().getNodeValue();
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
		         		System.out.println("Legal " + CustLegalAddres );
		         	
		         		
		         		 CustName = Converter.deCode(CustName);
		         		
		         		 //System.out.println(CustGUID + " " + CustName + " " + CustAgentGUID + " " + agentGUID.get(CustAgentGUID));
		         	  }catch(Exception e){
		         		  e.printStackTrace();
		         	  }
	         	
	         		
	         			  
         		  		
         		  		Customer customer = new Customer(CustCode);
         		  	
         		  		
         		  		if(CustName != null){
	      		         customer.setName(Converter.enCodeEst(CustName));
	      		         
	      		       if(CustLegalAddres != null){
	      		        	customer.setJuridics(Converter.enCodeEst(Converter.adressLink(CustLegalAddres,"Eesti")));
	      		         }
	      		         
	      		       
	      		         if(CustActualAddres != null){
	      		        	customer.setAddress(Converter.enCodeEst(Converter.adressLink(CustActualAddres,"Eesti")));
	      		         }else {
	      		        	customer.setAddress(Converter.enCodeEst(Converter.adressLink(CustLegalAddres,"Eesti")));
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
		      		     
		      		     
		      		     tm.put(customer.getName(), customer);
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
       
       
private HashMap prodsGUID = new HashMap();

   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
     prodGroups = new TreeMap();
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
		    		  
		    					        		// System.out.println("ProdGroups: " + ProdGroupsCode + " " + ProdGroupsDescription);
		        	  
		    		  
		    			  //rodGroupsParentGrGUID   = getNode(rida,"ParentGrGUID").getFirstChild().getNodeValue();
		    			  prodGroups.put(ProdGroupsCode, ProdGroupsDescription);
		    		
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
	   				 
	   				prod.setVat("21");

		    	
		 			
		 				tm.put(Converter.cleanLitLatEst(ProdDesc + prod.getUnit() + prod.getID()), prod);
		 			 

			       }
		    	}
		    
		    in.close();
		}catch(Exception k ){
				k.printStackTrace();
			}
		return tm;

   }

   
   
   
   public String getAssortmentStr(Agent agent){
    StringBuffer sb = new StringBuffer();

    return sb.toString();
  }


   public Vector getCustomerProductsList(String agentCode){
	   Vector custProds = new Vector();


	    return custProds;
	  }
   


   public String getDriverVersion(){
     return "S1XMLRadex";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
	
	private String clear (String ss){
			return ss;
	}
   
   
   public double getSalary(String agentCode){

     return 0;
   }

   private String getDir(){
     String dir = getParent().getProps().getProperty("readin");
     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
     return dir;
   }


   private String subjectFilesTo = null;
   private TreeMap<String,Double> overdebts = new TreeMap<String,Double>();
   private TreeMap discounts = new TreeMap();
   private Vector debtList = new Vector();
   private Vector klient = new Vector();
   private TreeMap agents = new TreeMap();
   private NumberFormat NF = NumberFormat.getInstance(Locale.ENGLISH);

   public NumberFormat getNF() {
	return NF;
}
/**
 * @param nf The nF to set.
 */
public void setNF(NumberFormat nf) {
	NF = nf;
}


  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {
	  
	   
	 
	  
  }

  
  
public Vector getFixedAssortmentData() {	
	//  Vector result = new Vector();
	  return prlists;
     /* try {
       
     	 FileReader fr = new FileReader(getDir() + "PriceMatrix.csv");
          BufferedReader reader = new BufferedReader(fr);
        String line = null;
        reader.readLine();
         while((line = reader.readLine()) != null ){

              line = line.replaceAll(";"," | ");
         	StringTokenizer st = new StringTokenizer(line, "|");

           if (st.hasMoreTokens()) {
         	//System.out.println( line );
//00012;;00012;0019D;;;7.6;;
              st.nextToken();
              st.nextToken();
              
              String clGroup = st.nextToken().trim();
              String prodID = st.nextToken().trim();
              st.nextToken();
              st.nextToken();
              double price = Double.parseDouble(st.nextToken().trim());
              String sdate =  st.nextToken().trim();
                 String edate = st.nextToken().trim();
                 String act = st.nextToken().trim();
            
          MatrixLine mline = new MatrixLine();
          if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
            mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
          }
          
          if(act.equals("Akc")) {
        	  
         
try {
	if(sdate != null  && !sdate.startsWith("1753") && sdate.trim().length() > 0 ){
    	 mline.setStartDate(sdate.substring(0,8));
     }
     if(edate != null && !edate.startsWith("1753") && sdate.trim().length() > 0 ){
    	 mline.setEndDate(edate.substring(0,8));
     }
}catch(Exception e) {
	
}
          }  
          
          
          if (prodID != null && prodID.length() > 0) {
            mline.setProdID(prodID);
          }
         

              result.add(mline);    
            
          }
         }
         
         reader.close();
         fr.close();
      }catch(Exception e){
      e.printStackTrace();
      }
	
    // System.out.println("fixed " + result.toString());
 return result;*/
}


@Override
public String getStartingTime() {
	// TODO Auto-generated method stub
	return null;
}
@Override
public void connectionUp() {
	// TODO Auto-generated method stub
	
}
@Override
public void connectionDown() {
	// TODO Auto-generated method stub
	
}
@Override
public void PictureMaker() {
	String pictssou = parent.getProps().getProperty("pict.dir");
    String pictspda = parent.getProps().getProperty("pictpda.dir");
   
	 try {
	  
    File f = new File(pictspda +"list.txt");
    if (f.exists())f.delete();
    //Kataloogi sisude kustutamine
	 	 File res2 = new File (pictspda);
         String[] ff2 = res2.list();
     	 for(int i = 0;ff2.length>i;i++){
         	(new File ( pictspda + ff2[i])).delete(); 
         }
//     	 Lõppes kustutamine 
		 
		 TreeMap prods = this.getProducts(null);

     StringBuffer buf = new StringBuffer();
     Iterator enume = prods.keySet().iterator();

     while (enume.hasNext()) {
	    	String asi = (String)enume.next();
	    	Product prod = (Product)prods.get(asi);
	    	if(prod.artikkel != null) {
	    	String toode = prod.artikkel.trim(); 
	    	
	    	//System.out.println("pildid" + toode);
           
            	
	   	  res2 = new File (pictssou);
          ff2 = res2.list();
         
     	 for(int i = 0;ff2.length>i;i++){
         	if( ff2[i].startsWith(toode)){
           // System.out.println("pilt olemas " + ff2[i]);
        	File in = new File ( pictssou + ff2[i]); 
          	if(in.isFile()){
          	File inpda = new File ( pictspda + prod.getID()+ ".jpg");
          	if(!inpda.exists()){
              	Converter.shrinkAndCopyFile(in,inpda,400);
      	    if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
                		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
                	}
      	  	   }
             }
           }
         }}
   }
     try {
		PrintWriter os = new PrintWriter(new FileWriter(pictspda + "list.txt", true));
		os.write(buf.toString());
		os.close();
	  }catch (Exception ex) {
		ex.printStackTrace();
	  }
	 
	 } catch (Exception e) {
	    getParent().showError(e);
	}
 System.exit(0);
}




}
