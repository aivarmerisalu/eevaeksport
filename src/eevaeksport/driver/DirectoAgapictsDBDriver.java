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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ee.mobilejava.earve.GenericDOM;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.imp.Order;
import eevaeksport.util.Converter;



/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class DirectoAgapictsDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,PictureMaker,FixLoader, WebReporter{

  public DirectoAgapictsDBDriver(EksportPanel parent) {
    super(parent);
   
   
    
    this.subjectFilesTo = parent.getString("filemsg");
    company = parent.getProps().getProperty("company");
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
    rcompanyx = getParent().getProps().getProperty("rest.companyx");
    if( parent.getProps().getProperty("stock.dir") != null){
    	stockConn = parent.getProps().getProperty("stock.dir");
    }
     }
  

  private HashMap readfromMemory() {
		HashMap mem = null;  
	try {
		File f = new File("ordermemory.vec");
		if(f.exists()) {
		    mem =  (HashMap) Converter.readObject("ordermemory.vec");
		}
	} catch (ClassNotFoundException | IOException e) {
		e.printStackTrace();
	}
	//System.out.println("loading mem "  + mem );
	return mem;
  }
  
  private String stockConn = null;
  String user =  null;
  String pass = null;
  String rcompany = null;
  String rcompanyx = null;

  public TreeMap getProductsOther(){
	    TreeMap tm = new TreeMap();
	     if(stockConn != null){
	    	
	     try {
	 
	    	   FileReader fr = new FileReader(stockConn + "tooted.xml");
	           BufferedReader reader = new BufferedReader(fr);
	         String line = null;

	          while((line = reader.readLine()) != null  ){
	        	 
		        	
	        	  if((line.trim().length()) > 50){
	        		 // System.out.println(line);
	        		  int len = line.indexOf("code=");
		        	  String id   = line.substring(len + 6);
		      	      Product prod = new Product(id.substring(0,id.indexOf("\"")).trim());
		      	      len = line.indexOf("default_name=");
		      	     String name   = line.substring(len + 14);
		      	      len = line.indexOf("stock_level_total=");
		      	 	 if(len != -1){
		      	 	     String stock   = line.substring(len + 19);  
		     	          prod.setStock((int)Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
		     	         tm.put(prod.getID(), prod.getStock());  
		      	 	 }
		      	
	        	  }
	        	
	          }	    	 
	    
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	     
	     }
	     System.out.println("Stocks OTHER size " + tm.size());
	     
	     return tm;
	     
	   }
  


  
  
	@Override
	public void PictureMaker() {
		makePictureCat();
		 System.exit(0);
		
	}
	
	  private void makePictureCat(){
	  	    
	    	
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
//	         	 Lõppes kustutamine 
	         	custProds.add("suva");
	  		 TreeMap prods = this.getProducts(null);

	         StringBuffer buf = new StringBuffer();
	         Iterator enume = prods.keySet().iterator();
	    
	         while (enume.hasNext()) {
	    	    	String asi = (String)enume.next();
	    	    	Product prod = (Product)prods.get(asi);
	    	    	String toode = prod.getID().trim();  
	               if(toode.length() > 0){
	                	
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
	               }
	             }
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
	     
	  }

  
  
  private Logger log = Logger.getLogger(DirectoAgapictsDBDriver.class.getName());
  
  private String company = null;
  
  public String getSaveDebtInfo() {
	  
	 String httpsURL = "https://login.directo.ee/xmlcore/" + company + "/xmlcore.asp?get=1&what=order";
      
    System.out.println(httpsURL);
    try {
    	
   
     URL myurl = new URL(httpsURL);
     URLConnection con = (HttpsURLConnection)myurl.openConnection();
     InputStream ins = con.getInputStream();
     InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
     BufferedReader in = new BufferedReader(isr);

    String inputLine = "";
    StringBuffer inputLineBuf = new StringBuffer();
    while ((inputLine = in.readLine()) != null){
    	inputLine = Converter.enCodeEst(inputLine);
    	inputLineBuf.append(inputLine.replaceAll("<item","\n<item" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
    }

    in.close();
    
    if(inputLineBuf.length() > 2){
   
          try {
        	  File f = new File (getDir() + "orders.xml");
        	  if(f.exists())f.delete();
        	Converter.saveFileInUTF8(getDir() + "orders.xml", inputLineBuf.toString());
       	
    	  }catch (Exception ex) {
    		ex.printStackTrace();
    	  }
     }
    
    }catch(Exception e) {
    	e.printStackTrace();
    } 
	  
	  this.getOpenOrders();
   	return null;
   }
   
   public String getPilotDayData(Agent agent){
   	return null;
   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	  private java.util.Date getCalXML(String ss){
		  Calendar cc = Calendar.getInstance();
		  	
		  	cc.set(Calendar.YEAR,Integer.parseInt(ss.substring(0,4)));
		  	cc.set(Calendar.MONTH,Integer.parseInt(ss.substring(5,7))-1);
		  	cc.set(Calendar.DATE,Integer.parseInt(ss.substring(8,10)));
		     
		  	return cc.getTime() ;
		  		
	  }
	
	  
	 private String ensureLen(String in ){
		 
		 if(in.length() == 4){
			 in = "0" + in ;
		 }
		 if(in.length() == 3){
			 in = "00" + in ;
		 }
		 if(in.length() == 2){
			 in = "000" + in ;
		 }

		 if(in.length() == 1){
			 in = "0000" + in ;
		 }
		 return in;
	 }
	  
	 private Vector selverEpoodProds = null;
	 
	public Vector getPriceMatrixData() {

		//Loeme sisse tooted 
	
		
   	Vector lines = new Vector();
    
    try {
		  File f = new File (getDir() + "hinnakiri.xml");
		  System.out.println("siin matix");
	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	     DocumentBuilder db;
		db = dbf.newDocumentBuilder();      	
    	FileInputStream in = getCleanInputStream(f);
    	String handler = null;
        Document doc = db.parse(in,handler);
        Node root = getNode(doc, "transport");      
   
    	Node read = getNode(root, "priceformulas");
        for (int i = 0; i < read.getChildNodes().getLength(); i++){
        	  Node rida = read.getChildNodes().item(i);
        	  try{
        		  if(rida.getNodeName().equals("priceformula")){
        			
        			 String custID = " ";
                  String clGroup = " ";
                  String prodID = " ";
                  String datafieldvalue = " ";
                  String prodGroup = " ";
                  String start = " ";
                  String end = " ";
                  String currency = "";
                  MatrixLine mline = new MatrixLine();
                 try{
                	 
                	
                 	 try{
                 	     clGroup = ensureLen(rida.getAttributes().getNamedItem("code").getNodeValue());
                        }catch(Exception aa){}
                 	 try{
	                         prodID =  rida.getAttributes().getNamedItem("item").getNodeValue();  
                 	   }catch(Exception aa){ }
                 	 try{
                 		datafieldvalue =  rida.getAttributes().getNamedItem("datafieldvalue").getNodeValue();
                	  }catch(Exception aa){ }
                 	 try{
                          prodGroup =  rida.getAttributes().getNamedItem("class").getNodeValue();
                 	  }catch(Exception aa){ }
                 	  try{
	                             start =  rida.getAttributes().getNamedItem("start_date").getNodeValue();
	                    	  }catch(Exception aa){ }
	                    	  try{
		                             end =  rida.getAttributes().getNamedItem("end_date").getNodeValue();
		                    	  }catch(Exception aa){ }
	                    	  try{
		                             currency =  rida.getAttributes().getNamedItem("currency").getNodeValue();
		                    	  }catch(Exception aa){ }
                      
                    
                    	  
                      
                      
                      try{
                     
                    	  String mode = rida.getAttributes().getNamedItem("what").getNodeValue();
                          
                    	  double value = Double.parseDouble(rida.getAttributes().getNamedItem("discount").getNodeValue());
                     if(mode != null && mode.equals("8")){
                    	 mline.setPrice(value);
                     }else{
                         mline.setDiscount(value);
                     }
                     mline.setProdLevel(mode);
                      }catch(Exception aa){ }
                  
                      
                    
                    
                      
                      if (custID != null && custID.trim().length() > 0) {
                        mline.setCustID(custID.trim());
                      }
                      if (currency != null && currency.trim().length() > 0) {
                          mline.setCurrency(currency.trim());
                        }
                      
                      if (clGroup != null
                          && clGroup.trim().length() > 0) {
                        mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
                      }

                      if (prodID != null && prodID.trim().length() > 0) {
                        mline.setProdID(prodID.trim());
                      }
                      
                      if (mline.getProdID() == null && prodGroup.trim().length() == 0 
                          && datafieldvalue != null && datafieldvalue.trim().length() > 0) {
                        mline.setProdGroup(datafieldvalue);;
                      }   
                      
                      
                      if (mline.getProdID() == null && prodGroup != null
                          && prodGroup.trim().length() > 0) {
                        mline.setProdGroup(prodGroup);
                      }   
                      
                      if(start != " " && end != " "){
                     	 mline.setStartDate(start);
                     	 mline.setEndDate(end);
                          Date alg = getCalXML(start); 
                          Date lopp = getCalXML(end); 
                          
                          if(alg.before(new Date()) && lopp.after(new Date())){
                         	
                         	 lines.add(mline);	
                          }
                        
                      }else{
                     	// lines.add(mline);	Saadame ainult soodukad
  	                 }
                      
                     
                 }catch(Exception e){
                 	e.printStackTrace();
                 }
                 
                }
             	
        	  }catch(Exception e){
        		  e.printStackTrace();
        	  }
             
        }
        
	}catch(Exception e){
		e.printStackTrace();
	}
    
 System.out.println("siin matrix" + lines.size());
    return lines;


   }

	
   TreeMap fixEans = null;
   
   public Vector getFixedAssortmentData() {
		
		
		if(selverEpoodProds == null ) {
			this.getProducts(null);
		}
		if(this.eShopPrl == null) {
			this.getCustomers(null);
		}
		 Vector lines = new Vector();
	       TreeMap prods = this.getProducts2();
	       fixEans = new TreeMap();
	     // System.out.println("prods " + eShopPrl.toString() + " " + selverEpoodProds.toString());
	       
	       try {
	   		  File f = new File (getDir() + "hinnakiri.xml");
	   	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	   	     DocumentBuilder db;
	   		db = dbf.newDocumentBuilder();      	
	       	FileInputStream in = getCleanInputStream(f);
	       	String handler = null;
	           Document doc = db.parse(in,handler);
	           Node root = getNode(doc, "transport");      
	      
	       	Node read = getNode(root, "priceformulas");
	           for (int i = 0; i < read.getChildNodes().getLength(); i++){
	           	  Node rida = read.getChildNodes().item(i);
	           	  try{
	           		  if(rida.getNodeName().equals("priceformula")){
	           			 String clGroup = " ";
	                     String prodID = " ";
	                     String start = null;
	                     String end = null;
	                    
	                         
	                         try{
	                    	 clGroup = ensureLen(rida.getAttributes().getNamedItem("code").getNodeValue()); 
	                         prodID =  rida.getAttributes().getNamedItem("datafieldvalue").getNodeValue();
	                         
	                         try { 
	                           start =  rida.getAttributes().getNamedItem("start_date").getNodeValue();
	              	            }catch(Exception aa){ }
	                         
	              	         try{
	                           end =  rida.getAttributes().getNamedItem("end_date").getNodeValue();
	                  	        }catch(Exception aa){ }
	                         
	                      
	                        	Node nnn =  rida.getAttributes().getNamedItem("datafield");
	                        if(nnn != null && (nnn.getNodeValue().equals("ART_EAN") || nnn.getNodeValue().equals("ARTIKULA_EAN")))	{
	                        
//System.out.println("aaaa " +clGroup + " " + prodID );
	                        	Vector vv = (Vector)fixEans.get(clGroup);
	                        	if(vv == null) {
	                        			vv = new Vector();
	                        	}
	                        	vv.add(prodID);
		                        fixEans.put(clGroup, vv);

		                         if (prodID != null && prodID.trim().length() > 0 && eans.get(prodID) != null) {
		                          
		                         ArrayList arr = (ArrayList) eans.get(prodID);
		                       //  System.out.println(" " + arr.toString());
	                        	for(Object pr : arr) {
	                        		
	                        		 MatrixLine mline = new MatrixLine();
	                        		 mline.setProdID(pr+"");
	                        		// System.out.println(" " + pr + " " + clGroup);
	                        		 if (clGroup != null && clGroup.trim().length() > 0) {
	  		                           mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()+ "XX"));
	  		                         }
	                        		 
	                        		 if(selverEpoodProds.contains(pr+"") && eShopPrl.contains(mline.getCliGroup())) {
	                        			 mline.setLineinfo("TOP");
	                        		 }
	                        		 
		                        	if(start != null && end != null) {
		                        		 Date alg = getCalXML(start); 
		                                 Date lopp = getCalXML(end); 
		                           	 
		                           	  if(alg.before(new Date()) && lopp.after(new Date())){
		                                  	 lines.add(mline);	
		                                  }	
		                        	}else {
		                        		lines.add(mline);
		                        	}	
	                        	
		                         }
	                        	
	                        }
	                        	 	
	                        }
	                    
	                       
	                    }catch(Exception e){
	                    }
	                    
	                   }
	                	
	           	  }catch(Exception e){
	           		  e.printStackTrace();
	           	  }
	                
	           }
	           
	   	}catch(Exception e){
	   		e.printStackTrace();
	   	}
	     //  System.out.println("fixlines  " + lines.size() + " " + prods.size());
	       
	       return sortLines(lines,prods);
	

	}

   
   
   
 
   
   private Vector sortLines(Vector inVec,TreeMap products){
	   TreeMap asi = new TreeMap();
	        for (int i = 0 ; i < inVec.size() ; i++){
	        	
	        	  MatrixLine ol = (MatrixLine)inVec.elementAt(i);
	          asi.put(ol.getCliGroup()+ ((Product)products.get(ol.getProdID())).getName() , ol);
	         //System.out.println("Prod22 " + ol.getProdID() + " " + ol.getCliGroup()+ ((Product)products.get(ol.getProdID())).getName() );
		      
	        }
	        Vector sorted = new Vector ();
	        Iterator itera = asi.keySet().iterator();
	        while (itera.hasNext()){
	        	String key = (String)itera.next();
	        	sorted.add((MatrixLine)asi.get(key));
	        }
	       
	    //   System.out.println("sorted Size " + sorted.size() + " "+ asi.size());
	        return sorted;
	      }
	


   public String getProductSortingData(){
   	return null;
   }
   
   public DealerProduct getDealerProductsList() {
 return null;
   }

   public Vector getDebt(String agentCode){
        return this.debtList;
  }

   public void connectionDown(){
   }
   public void connectionUp(){
   }

   public String getDealerCustomerCSV(String agent){
       return null;
        }

       public String  getCustProdDiscounts(Agent agent){
                     return null;
            }
  private TreeMap   prGroups1= new TreeMap();
  private TreeMap   prGroups= new TreeMap();
  private TreeMap   prGroupsSeos= new TreeMap();
  private TreeMap   prTypes= new TreeMap();
  
  public TreeMap getProdGroups() throws SQLException {
	  return prGroups1;
  }
	    
  
   public TreeMap getProdGroups2() throws SQLException {
    
     
  	try {
  		  File f = new File (getDir() + "tootegrupid.xml");
  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  	     DocumentBuilder db;
  		db = dbf.newDocumentBuilder();      	
      	FileInputStream in = getCleanInputStream(f);
      	String handler = null;
          Document doc = db.parse(in,handler);
          Node root = getNode(doc, "transport");      
       //   System.out.println(root);
      	Node read = getNode(root, "itemclasses");
      //	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
          for (int i = 0; i < read.getChildNodes().getLength(); i++){
          	  Node rida = read.getChildNodes().item(i);
          	  try{
          		  if(rida.getNodeName().equals("itemclass")){
               		 String group  = rida.getAttributes().getNamedItem("code").getNodeValue();
               		 try{
               			 String name  = rida.getAttributes().getNamedItem("default_name").getNodeValue(); 
                   		 group = Converter.cleanLitLatEst(group).toUpperCase();
                   		  name  =  Converter.enCodeEst(name);
                   		  //paneme siis kui masterit pole 
                   		  String tase = rida.getAttributes().getNamedItem("masters").getNodeValue().toUpperCase(); 
                   	
                   		// System.out.println("tase " + tase + " " + (tase.indexOf(",") != tase.length()-1) + " " + tase.indexOf(",") +" " +tase.length());
                   		  if(tase.length() == 0) {
                   			 prGroups.put(group, name);
                   			// System.out.println("grupid " + group + " " + name + " " + tase + " " + tase2);
                   		  }else if(tase.indexOf(",") != -1) {
                   		//	System.out.println("grupid2 " + group + " " + name +  " " + tase +" " + tase.substring(0,tase.length()-1) + " " + tase2); 
                   			prGroups.put(group, name);
                   			StringTokenizer st = new StringTokenizer(tase,",");
                   			while(st.hasMoreTokens()) {
                   				prGroupsSeos.put(group, st.nextToken());
                   			}
                   			
                   			
                   		  }
                   		  
                   		  
                   		 
               		 }catch(Exception r){
               			 r.printStackTrace();
               		 }
               	 
          		  } 
                  
          	  }catch(Exception e){
          		  e.printStackTrace();
          		 // System.exit(0);
          	  }
               
          }
          
  	}catch(Exception e){
  		e.printStackTrace();
  	}
 // System.out.println(prGroupsSeos.toString());
  	
  	return prGroups;
   }

  
   
   public TreeMap getProdGroupsSecond() throws SQLException {
      return prGroups;
        }


   
  
 
	  
     
   public TreeMap getAgents(){
	   
		  

	   
     TreeMap tm = new TreeMap();
     try{
    	 
    	 if(getParent().getProps().getProperty("christCodes") != null ){
  		   christCodes = new Vector<String>();
  		   
  		  String aa =  getParent().getProps().getProperty("christCodes");
  		  
  		  StringTokenizer st = new StringTokenizer(aa,",");
  		  while(st.hasMoreTokens()) {
  			  christCodes.add(st.nextToken()) ;
  		  }
  	    	System.out.println("christCodes " + christCodes.size());
  	     }
    	// getProducts(null);	 
     }catch(Exception r){
    	 r.printStackTrace();
     }
   
     //getOpenOrders();
   //  getFixedAssortmentData();
 
     // loene võimalikud agendid sisse 
     agent1 = new TreeMap();
 	try {
 		  File f = new File (getDir() + "kliendid.xml");
 	     
 	       // System.out.println(f.getAbsolutePath());
 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	     DocumentBuilder db;
 		db = dbf.newDocumentBuilder();      	
     	FileInputStream in = getCleanInputStream(f);
     	String handler = null;
         Document doc = db.parse(in,handler);
         Node root = getNode(doc, "transport"); 
     	Node read = getNode(root, "customers");
     	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
         for (int i = 0; i < read.getChildNodes().getLength(); i++){
         	  Node rida = read.getChildNodes().item(i);
         	  try{	
         		 String salesman  = rida.getAttributes().getNamedItem("salesman").getNodeValue(); 
         		 salesman = Converter.deCode(salesman);
         		 Agent sm = new Agent (salesman);
         		 sm.setName(salesman);
         		// sm.needsZipPass = true;
                  tm.put(salesman, sm); 
                 
                  String id =  rida.getAttributes().getNamedItem("code").getNodeValue();
                  if(!parent.getEmail(salesman).equals("") &&  !custtsUsed.contains(id)){
                	  agent1.put(salesman,sm);
	         		   custtsUsed.add(id);
	         		  String debt  = rida.getAttributes().getNamedItem("balance").getNodeValue(); 
	           		
	         		   if(!debt.equals("0")){
	         			custDebtUsed.add(id);
	         			//System.out.println("balance " + debt);
	         		   }
      		      }
	         			 String price_list = ensureLen(rida.getAttributes().getNamedItem("price_list").getNodeValue());         	
		         	
		         	      if(!parent.getEmail(salesman).equals("") &&  !pricelists.contains(price_list)){
	 	         		     pricelists.add(price_list.trim()); 
	 	         		   }
		         	      price_list =  rida.getAttributes().getNamedItem("price_list_special").getNodeValue() ;         	
				         	
		         	      StringTokenizer st  = new StringTokenizer(price_list,",");
		         	      if(st.hasMoreTokens()){
                            String aa = ensureLen(st.nextToken());
                            
			         	      if(!parent.getEmail(salesman).equals("") &&  !pricelists.contains(aa)){
		 	         		     pricelists.add(aa); 
		 	         		   }
			         	      
		         	      }
	         	  
         	  }catch(Exception e){
         		//  e.printStackTrace();
         	  }
         }
         
 	}catch(Exception e){
 		e.printStackTrace();
 	}
     
    // System.out.println("Agendilist tehtud" );
 //    log.info("Agendilist tehtud");
   //  log.fine("Hinnakirju kasutusel "  + pricelists.size() +  " " + pricelists.toString() );
    // System.out.println("Aktiivseid kliente kasutusel "  + custtsUsed.size());
    // System.out.println("Võlas olevaid kliente kasutusel "  + custDebtUsed.size());
    // System.out.println("Agent1 suurus "  + agent1.size());
   
     
     Agent aaa = new Agent ("LADU");
     aaa.setName("LADu");
     tm.put("LADU", aaa);

    // setBulkSales(tm);
     
     return tm;
   }

   TreeMap agent1 = null;
   
   private void makeData(){
	       try{
       	String httpsURL = "https://login.directo.ee/xmlcore/" + company + "/xmlcore.asp?get=1&what=itemclass";
       	
  
       	
       	
       	Converter.doTrustToCertificates();
       	Converter.trustAllHttpsURLConnection();
           URL myurl = new URL(httpsURL);
           HttpURLConnection con = (HttpURLConnection)myurl.openConnection();
           InputStream ins = con.getInputStream();
           InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
           BufferedReader in = new BufferedReader(isr);

           String inputLine;
           StringBuffer inputLineBuf = new StringBuffer();
           while ((inputLine = in.readLine()) != null){
           	inputLine = Converter.enCodeEst(inputLine);
           	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\"l\"","\"L\""));
          }

           in.close();
           
           if(inputLineBuf.length() > 2){
             
                 try {
               	  File f = new File (getDir() + "tootegrupid.xml");
               	  if(f.exists())f.delete();
               	  
               	  
             	 Converter.saveFileInUTF8(getDir() + "tootegrupid.xml", inputLineBuf.toString());
           	
           	  }catch (Exception ex) {
           		ex.printStackTrace();
           	  }
            }
           
           
           
           
           
           
           //lõpeb tootegruppidega tegelemine 
           System.out.println("Tootegrupid tehtud" );
           //algab Hinnakirja faili moodustamine
           
           httpsURL = "https://login.directo.ee/xmlcore/" + company + "/xmlcore.asp?get=1&what=priceformula";
           myurl = new URL(httpsURL);
           con = (HttpsURLConnection)myurl.openConnection();
           ins = con.getInputStream();
           isr = new InputStreamReader(ins,"UTF-8");
           in = new BufferedReader(isr);

           inputLine = "";
          inputLineBuf = new StringBuffer();
          while ((inputLine = in.readLine()) != null){
          	inputLine = Converter.enCodeEst(inputLine);
          	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
          }

          in.close();
          
          if(inputLineBuf.length() > 2){
         
                try {
              	  File f = new File (getDir() + "hinnakiri.xml");
              	  if(f.exists())f.delete();
              	 Converter.saveFileInUTF8(getDir() + "hinnakiri.xml", inputLineBuf.toString());
          	
          	  }catch (Exception ex) {
          		ex.printStackTrace();
          	  }
           }
          

          //lõpeb toodetega tegelemine 

          System.out.println("Hinnakirjad tehtud" );
           
           
           
          
          httpsURL = "https://login.directo.ee/xmlcore/" + company + "/xmlcore.asp?get=1&what=supplier";
          myurl = new URL(httpsURL);
          con = (HttpsURLConnection)myurl.openConnection();
          ins = con.getInputStream();
          isr = new InputStreamReader(ins,"UTF-8");
          in = new BufferedReader(isr);

          inputLine = "";
         inputLineBuf = new StringBuffer();
         while ((inputLine = in.readLine()) != null){
         	inputLine = Converter.enCodeEst(inputLine);
         	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
         }

         in.close();
         
         if(inputLineBuf.length() > 2){
        
               try {
             	  File f = new File (getDir() + "supplier.xml");
             	  if(f.exists())f.delete();
             	 Converter.saveFileInUTF8(getDir() + "supplier.xml", inputLineBuf.toString());
         	
         	  }catch (Exception ex) {
         		ex.printStackTrace();
         	  }
          }
          
          httpsURL = "https://login.directo.ee/xmlcore/" + company + "/xmlcore.asp?get=1&what=brand";
          myurl = new URL(httpsURL);
          con = (HttpsURLConnection)myurl.openConnection();
          ins = con.getInputStream();
          isr = new InputStreamReader(ins,"UTF-8");
          in = new BufferedReader(isr);

          inputLine = "";
         inputLineBuf = new StringBuffer();
         while ((inputLine = in.readLine()) != null){
         	inputLine = Converter.enCodeEst(inputLine);
         	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
         }

         in.close();
         
         if(inputLineBuf.length() > 2){
        
               try {
             	  File f = new File (getDir() + "brands.xml");
             	  if(f.exists())f.delete();
             	 Converter.saveFileInUTF8(getDir() + "brands.xml", inputLineBuf.toString());
         	
         	  }catch (Exception ex) {
         		ex.printStackTrace();
         	  }
          }
         

         //lõpeb toodetega tegelemine 

         System.out.println("Hinnakirjad tehtud" );
          
          
        // https://login.directo.ee/xmlcore/agapics/xmlcore.asp?get=1&what=order
        	 
        	  httpsURL = "https://login.directo.ee/xmlcore/" + company + "/xmlcore.asp?get=1&what=order";
       
	    System.out.println(httpsURL);
         myurl = new URL(httpsURL);
         con = (HttpsURLConnection)myurl.openConnection();
         ins = con.getInputStream();
         isr = new InputStreamReader(ins,"UTF-8");
         in = new BufferedReader(isr);

         inputLine = "";
        inputLineBuf = new StringBuffer();
        while ((inputLine = in.readLine()) != null){
        	inputLine = Converter.enCodeEst(inputLine);
        	inputLineBuf.append(inputLine.replaceAll("<item","\n<item" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
        }

        in.close();
        
        if(inputLineBuf.length() > 2){
       
              try {
            	  File f = new File (getDir() + "orders.xml");
            	  if(f.exists())f.delete();
            	Converter.saveFileInUTF8(getDir() + "orders.xml", inputLineBuf.toString());
           	
        	  }catch (Exception ex) {
        		ex.printStackTrace();
        	  }
         }
        
          
           //algab tootede faili moodustamine
           
            httpsURL = "https://login.directo.ee/xmlcore/" + company + "/xmlcore.asp?get=1&what=item";
            if(getParent().getProps().getProperty("stockcode") != null){
            	httpsURL = httpsURL + "&stock=" +  getParent().getProps().getProperty("stockcode");
  		  }
  	    System.out.println(httpsURL);
            myurl = new URL(httpsURL);
            con = (HttpsURLConnection)myurl.openConnection();
            ins = con.getInputStream();
            isr = new InputStreamReader(ins,"UTF-8");
            in = new BufferedReader(isr);

            inputLine = "";
           inputLineBuf = new StringBuffer();
           while ((inputLine = in.readLine()) != null){
           	inputLine = Converter.enCodeEst(inputLine);
           	inputLineBuf.append(inputLine.replaceAll("<item","\n<item" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
           }

           in.close();
           
           if(inputLineBuf.length() > 2){
          
                 try {
               	  File f = new File (getDir() + "tooted.xml");
               	  if(f.exists())f.delete();
               	Converter.saveFileInUTF8(getDir() + "tooted.xml", inputLineBuf.toString());
              	
           	  }catch (Exception ex) {
           		ex.printStackTrace();
           	  }
            }
           

           //lõpeb toodetega tegelemine 

           System.out.println("Tooted tehtud" );
           //algab kliendi faili moodustamine
           
           httpsURL = "https://login.directo.ee/xmlcore/" + company + "/xmlcore.asp?get=1&what=customer";
           myurl = new URL(httpsURL);
           con = (HttpsURLConnection)myurl.openConnection();
           ins = con.getInputStream();
           isr = new InputStreamReader(ins,"UTF-8");
           in = new BufferedReader(isr);

           inputLine = "";
          inputLineBuf = new StringBuffer();
          while ((inputLine = in.readLine()) != null){
          	inputLine = Converter.enCodeEst(inputLine);
           	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ));	
          	
       //       System.out.println(inputLine);	
          }

          in.close();
          
          if(inputLineBuf.length() > 2){
             
                 try {
                    String res = "";
               	  StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
               	  while(st.hasMoreTokens()){
               		String line = st.nextToken();
               		//if(line.indexOf("\"10173\"") == -1 && line.indexOf("\"13543\"") == -1 && line.indexOf("<contact code=\"2575\"") == -1 ){
               			res = res + line + "\n";
               		//}
               	  }
               	  File f = new File (getDir() + "kliendid.xml");
               	  if(f.exists())f.delete();
               	Converter.saveFileInUTF8(getDir() + "kliendid.xml", inputLineBuf.toString());
              	
           	  }catch (Exception ex) {
           		ex.printStackTrace();
           	  }
            }
          
       }catch(Exception e){
       	e.printStackTrace();
       }
       
       

   	

       System.out.println("kliendid tehtud" );
       
       
      // System.exit(0);
	   
	   
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
        	  
        	  
       public String getDistribution() {
         return null;
       }
       
       private NumberFormat NF = NumberFormat.getInstance();
       
       Properties konverter = null;
       
       private TreeMap  getOpenOrders(){
    	   
    	    NF.setMaximumFractionDigits(4);
    	    NF.setGroupingUsed(false);
      
    	    try {
    	    	  konverter = EksportFrame.loadProps("abifailid/konverter.txt");
    	    	 // System.out.println("konverter " + konverter.toString());
    	    }catch (Exception e) {
    	    	e.printStackTrace();
    	    }
    	    TreeMap cmanas = custManas();
  		    TreeMap<String, ArrayList<Toode>> docs = new TreeMap<String, ArrayList<Toode>>();
  			TreeMap res = new TreeMap();
  			TreeMap prods = getProducts2();
  			StringBuffer oorders = new StringBuffer();
  			oorders.append(Calendar.getInstance().getTimeInMillis() + "\n");
  			
  			try {
  				 File f = new File (getDir() + "orders.xml");
  		 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  		 	     DocumentBuilder db;
  		 		db = dbf.newDocumentBuilder();      	
  		     	FileInputStream in = getCleanInputStream(f);
  		     	String handler = null;
  		         Document doc = db.parse(in,handler);
  		         Node root = getNode(doc, "transport");      
  		      //   System.out.println(root);
  		     	Node read = getNode(root, "orders");
  		     //	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
  		         for (int t = 0; t < read.getChildNodes().getLength(); t++){
  		         	  Node rida = read.getChildNodes().item(t);
  		         	  
  		         	 // System.out.println("node name " + rida.getNodeName());
  		         	 // if(rida.getNodeName().equals("order")){
  		         		
  			         try {	
  			         	  String id = rida.getAttributes().getNamedItem("ccode").getNodeValue();
  	  			    	  String arve = rida.getAttributes().getNamedItem("webid").getNodeValue();
  	  			    	  
  	  			    	  String number =  " " ;
  	  			    	  try {
  	  			    		  number = rida.getAttributes().getNamedItem("cutomer_orderno").getNodeValue();
  	  			    	  }catch(Exception e) {
  	  			    		  
  	  			    	  }
  	  			    		
  	  			    	  
  	  			    	  
  	  			    	  
  	  			    	String kuup = null;
  	  			    String kuup2 = null;
  	  			    	  try {
  	  			    	  kuup =  (rida.getAttributes().getNamedItem("webdate").getNodeValue()).substring(0,10);
 	  			    	  kuup2 =  (rida.getAttributes().getNamedItem("webdate").getNodeValue()).substring(0,10);
  	  			    	  }catch(Exception e) {
  	  			    		  
  	  			    	  }
  	  			    	boolean goes = false; 
  	  			    	
  	  			    	try {
  	  			    	String status =  (rida.getAttributes().getNamedItem("status").getNodeValue());
			    			 if(status != null && status.equals("MUUTA") && ( number.startsWith("45") || number.startsWith("46") 
			    					 || id.equals("10183") )) { // coop
			    				 goes = true;
			    			 }
  	  			    	}catch(Exception e) {
  	  			    		
  	  			    	}
  	  			    	  
 	  			    
  	  			    	Node read2 = getNode(rida, "lines");
  	  			      for (int u = 0; u < read2.getChildNodes().getLength(); u++){
  	  			      Node rida2 = read2.getChildNodes().item(u);
  	  			      String toode = null;
  	  			  try {
	  			    	   toode = rida2.getAttributes().getNamedItem("pcode").getNodeValue();
  	  			}catch(Exception e) {
			    		 
		    	  }
  	  			  
  	  			  if(toode != null) {
	  			    	  Node nnn = rida2.getAttributes().getNamedItem("description");
	  			    	  
	  			    	  String nimi = "pole nime";
	  			    	  if(nnn != null) {
	  			    		  nimi = nnn.getNodeValue();
	  			    	  }
	  			    	  double kogus =  0;
	  			    	  try {
	  			    		  kogus =  Double.parseDouble(rida2.getAttributes().getNamedItem("quantity").getNodeValue()); 
	  			    	  
	  			    	}catch(Exception e) {
				    		 
	  		    	  }
	  			    	  
	  			    	  if((toode.startsWith("UT") || toode.startsWith("UI")) && prods.get(toode) != null) {
	  			    		  toode = ((Product)prods.get(toode)).getEAN();
	  			    		  //System.out.println("muutsin koodi 00" + toode);

	  			    		  if(konverter != null && konverter.getProperty(toode) != null) {
	  			    			  toode = (String)konverter.getProperty(toode);
	  			    			//  System.out.println("muutsin koodi 22  " + toode);
	  			    		  }
	  			    	  }
	  			    	  
	  			    	  
	  			    	  Toode tt = new Toode(toode);
	  			    	  tt.setBrand(nimi);
	  			    	  tt.setKogus(kogus);
	  			    	  tt.setGr1(arve);
	  			    	  tt.setGr2(kuup2);
	  			    	  
	  			    	if(kuup != null) {
	  			    	
	  			    	  java.util.Date dd = getDate(kuup);
	  			    	  boolean ddOK = dd.after(new java.util.Date()) || isToday14(dd.getTime()) ;
	  			    	  java.util.Date dd2 = getDate(kuup2);
	  			    	  boolean dd2OK = dd2.after(new java.util.Date()) || isToday14(dd2.getTime()) ;
	  			    	  
	  			    	  if((ddOK || dd2OK ) && goes ) {
	  			    		
	  			    	  ArrayList<Toode> pr = new ArrayList<Toode>();
	  			    		if(docs.get(id+"|"+arve) != null) {
	  			    			 pr = docs.get(id+"|"+arve);
	  			    		}
	  			    		pr.add(tt);
	  			    		if(id != null && id.trim().length() > 0) {
	  			    			docs.put(id+"|"+arve, pr);
	  			    		}	
		  			    	}
	  			    	  }
  	  			        }}
  			          }catch(Exception rr) {
 			    		  rr.printStackTrace();
 			    	  }
	  			        }
  		       
  	  			      
  	  			      StringBuffer sb = new StringBuffer();
  	  			      
  	  			    Iterator itera =   docs.keySet().iterator();
  	  			    File jj = new File ("aorder");
  	  			    if(!jj.exists()) {
  	  			    	jj.mkdir();
  	  			    }
  	  			   
  	  			    FileUtils.cleanDirectory(jj);
  	  			    
  	  			    while (itera.hasNext()) {
  	  			    	StringBuffer ss = new StringBuffer();
  	  			    	String id = (String)itera.next();
  	  			    	
  	  			    	ArrayList<Toode> ar = docs.get(id);
  	  			    id = id.substring(0, id.indexOf("|"));
  	  			    	String kuup = null;
  	  			        String numb = null;
  	  			    	String text = "";
  	  			    	String agid = cmanas.get(id)+"";
  	  			    	for (int i = 0 ; i < ar.size(); i++) {
  	  			    		Toode tt = ar.get(i);
  	  			    		kuup = tt.getGr2();
  	  			    		numb = tt.getGr1();
  	  			    		text = text + "[" + NF.format(tt.getKogus()) + "] " 
  	  						       + " " + tt.getBrand().trim() + "|";
  	  			    		ss.append(id+";" + tt.getID() + ";" + tt.getKogus()+";"+numb+"\n");
  	  					}
  	  			    	
  	  			    Converter.saveFile("aorder\\order-" + agid+"-" + numb + ".txt", ss.toString());  
  	  			    	
  	  			    	 sb.append(numb + "  " + kuup + "|");
  	  					 sb.append("|" + text);
  	  			    	  
  	  				     res.put(id, sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
  	  				   	 sb = new StringBuffer();
  	  					  oorders.append(id + ";" + ar.size() + ";" + kuup + "\n" );
  	  			        }
  	  			       			           
  			     
  		         
  			    	
  			      
  				 } catch (Exception e) {
  			 		  getParent().showError(e);
  			 	 }
  			
  	   if(parent.getProps().getProperty("savefilesdir") != null) {
  		   Converter.saveFile(parent.getProps().getProperty("savefilesdir")  + "customerorders.txt", oorders.toString());   
  	   }
  	   if(parent.getProps().getProperty("savefilesafteremail") != null) {
  		   Converter.saveFile(parent.getProps().getProperty("savefilesafteremail")  + "customerorders.txt", oorders.toString());   
  	   }
       	  	
  			return res;
  		}
  		
       public static boolean isToday(long lll){
	    	Calendar cc = Calendar.getInstance();
	    	cc.set(Calendar.HOUR_OF_DAY, 1);
	    	if(cc.getTime().getTime() < lll){
	    		return true;
	    	}else{
	    		return false ;	
	    	}   	
	    }
       
       public static boolean isToday14(long lll){
	    	Calendar cc = Calendar.getInstance();
	    	cc.set(Calendar.HOUR_OF_DAY, 1);
	    	cc.add(Calendar.DAY_OF_YEAR, -4);
	    	if(cc.getTime().getTime() < lll){
	    		return true;
	    	}else{
	    		return false ;	
	    	}   	
	    }

       private TreeMap custManas() {
    	   TreeMap tm = new TreeMap();
    	   try {
 	 	File f = new File (getDir() + "kliendid.xml");
 	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	 	     DocumentBuilder db;
 	 		 db = dbf.newDocumentBuilder();      	
 	     	  FileInputStream in = getCleanInputStream(f);
 	     	 String handler = null;
 	         Document doc = db.parse(in,handler);
 	         Node root = getNode(doc, "transport");      
 	     	 Node read = getNode(root, "customers");
 	     
 	     	 for (int i = 0; i < read.getChildNodes().getLength(); i++){
 	         	  Node rida = read.getChildNodes().item(i);
 	         	  
 	         	  if(rida.getNodeName().equals("customer")){
 	         		
 		         	  try{
 		         		  String id =  rida.getAttributes().getNamedItem("code").getNodeValue();
 	         		      String salesman  = rida.getAttributes().getNamedItem("salesman").getNodeValue(); 
 	         		    tm.put(id, salesman);
 	         		
 		         	  } catch(Exception e){
 	         		
 	         		  e.printStackTrace();
 	         	  }
 	              
 	         	  }}
 	 	}catch(Exception e){
 	 		e.printStackTrace();
 	 	}
    	   return tm;
       }
       
       TreeMap custPRlists = null;
       TreeMap custMap = null;
       TreeMap custAgs = null;
       Vector eShopPrl = null;

   public TreeMap getCustomers(Agent agent){
	   TreeMap tm = new TreeMap();
	   eShopPrl = new Vector();
	   custPRlists = new TreeMap();
	   custMap = new TreeMap();
	   custAgs = new TreeMap();

	   HashMap mem = readfromMemory();
	   if(customerSales == null) {
		   this.getCustomerProductsList2();
	   }
	   if(!"true".equals(getParent().getProps().getProperty("use.olddata"))){
		   makeData();
		 }
	 
	   
	   try{
    	   Converter.getManagersReplacements(agent1, rcompanyx, user, pass, false); 
    		
    }catch(Exception e){
    	e.printStackTrace();
    }
	   
	   
	   TreeMap  oOrders = this.getOpenOrders();
	   System.out.println("kliendid a");
		
		  String country = "Estonia";
		  if(getParent().getProps().getProperty("country") != null){
			  country = getParent().getProps().getProperty("country");
		  }
	   
		 this.customers = new ArrayList();
	     this.debtList = makeDebtlist();
	     
	     
	   try {
	 		  File f = new File (getDir() + "kliendid.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "transport");      
	      //   System.out.println(root);
	     	Node read = getNode(root, "customers");
	     	Customer currCust = null;
	     	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	         for (int i = 0; i < read.getChildNodes().getLength(); i++){
	         	  Node rida = read.getChildNodes().item(i);
	         	  
	         	 // System.out.println("node name " + rida.getNodeName());
	         	  if(rida.getNodeName().equals("customer")){
	         		 String name = null;
		         	  try{
		         		   name =  rida.getAttributes().getNamedItem("name").getNodeValue();
		         		  String id =  rida.getAttributes().getNamedItem("code").getNodeValue();
		         		Customer customer = new Customer(id);
		         		  customer.restName = name;
		         	
		         		  try{
		         			  String payer = rida.getAttributes().getNamedItem("payer").getNodeValue();
			         		  
			         		  if(payer != null && payer.trim().length() > 0 ){
			         			  customer.setPayer(payer.trim());
			         		  }
	  
		         		  }catch(Exception e){
		         			  
		         		  }
		         		
		         		if(this.usedCredit.get(id) != null){
		         			customer.setUsedCredit(Double.parseDouble(usedCredit.get(id)+ ""));
		         		}
		         		
		         		if(this.dueSums.get(id) != null){
		         			customer.setDebt(Double.parseDouble(dueSums.get(id)+ ""));
		         		}
		         		
		         		 String sendaadr = ""; 
		         		try{ 
		 	         		  sendaadr =  rida.getAttributes().getNamedItem("send_address_street").getNodeValue(); 
		 	         		  customer.restStreet = sendaadr.replaceAll(";", ",");
		 	         		  name = name + " " + sendaadr;
		 	         		String maa = "";
		 	         		try{
		 	         	  maa =  rida.getAttributes().getNamedItem("send_address_country").getNodeValue().replaceAll(";", ","); 
		 	         		country = maa;
		 	         		}catch(Exception eed){
			         		 }
		 	         		
		 	         		customer.setAddress(Converter.adressLink(sendaadr, country));
		 	         	
		         		
		         		}catch(Exception ee){
		         		 }
			         	
		         		
		         		String asula = "";
		         		try{ 
		 	         		  asula =  rida.getAttributes().getNamedItem("send_address_county").getNodeValue().replaceAll(";", ","); 
		 	         		sendaadr = sendaadr + " " + asula;
		 	         		customer.restCity = asula; 
		 	         	
		 	         		String maa = "";
		 	         		try{
		 	         	  maa =  rida.getAttributes().getNamedItem("send_address_country").getNodeValue().replaceAll(";", ","); 
		 	         		country = maa;
		 	         		}catch(Exception eed){
			         		 }
		 	         		
		 	         		customer.setAddress(Converter.adressLink(sendaadr, country));
		 	         	 }catch(Exception ee){
		         		 }
		         		
		         		

		         		String regno = "";
		         		try{ 
		         			regno =  rida.getAttributes().getNamedItem("regno").getNodeValue(); 
		 	         		//customer.setRegnr(regno);
		 	         	 }catch(Exception ee){
		         		 }
                         
		         		

		         		try{ 
		         			double discount  =  Double.parseDouble(rida.getAttributes().getNamedItem("discount").getNodeValue());
		         			if(discount > 0){
		         		 		customer.setDiscount(discount);
		    		 	  }
		 	         	 }catch(Exception ee){
		         		 }
		         		
		         		  customer.setName(name);
		         		 String aadr = "";
		         		 String aadr2 = "";
		         		 try{ 
		 	         		  aadr =  rida.getAttributes().getNamedItem("address_street").getNodeValue(); 
		 	         		  aadr2 =  rida.getAttributes().getNamedItem("address_county").getNodeValue(); 
			 	         	  customer.restStreet = aadr;
			 	         	  customer.restCity = aadr2;
		 	         		  customer.setAddress(Converter.adressLink(aadr, country));
		         		 }catch(Exception ee){

			 		         		 }
		         		 
		         		try{ 
		         			country =  rida.getAttributes().getNamedItem("address_country").getNodeValue(); 
		 	         		  customer.setCountry(country);
			 	         	  customer.setAddress(Converter.adressLink(aadr, country));
		         		 }catch(Exception ee){
			 		         		 }
		         		 
		         		
		         		 try{ 
		 	         		 String yldphone =  rida.getAttributes().getNamedItem("telephone").getNodeValue();
		 	         		 if(yldphone.trim().length() > 0){
		 	            		customer.setPersonalData((yldphone).replaceAll(";", ","));
		 	            		customer.restPhone = yldphone;
		 	  		         }
		 	     		 }catch(Exception ee){
                        	 customer.setAddress(""); 
		         		 }

		         		/*  if(customerSales.get(customer.getID()) != null) {
		         			  Toode t = (Toode)customerSales.get(customer.getID())  ;
		         			  String asi = null;
	        	              if(t.getDonecur() != 0 ){
	        	            	  asi = "Jooksev kuu : " + (int)t.getDonecur() + " eur|"; 
	        	              }
	        	              
	        	            	   if(t.getDonevol() != 0 ){
	              	            	 if(asi == null){
	              	            	   asi = "Eelmine Aasta sama : " + (int)t.getDonevol() + "eur|"; 
	              	                 }else{
	              	            	   asi = asi + "Eelmine Aasta sama : " + (int)t.getDonevol() + "eur|"; 
	              	                }}
	        	            	   
                            if(asi != null) {
		         			  customer.setInfo(asi);
                            }
		         		  }*/
		         		  try{
		         			  
		         			 for (int r = 0; r < rida.getChildNodes().getLength(); r++){
		         			  Node nn = rida.getChildNodes().item(r);
		       	    		 if(nn.getNodeName().equals("contact")){
		         				 String cname =  nn.getAttributes().getNamedItem("name").getNodeValue();
		         				String ctel =  nn.getAttributes().getNamedItem("telephone").getNodeValue();
		         				customer.restPerson = cname;
		         				customer.restPerPhone = ctel;
		         				if(customer.getPersonalData().trim().length() < 1){
		         					customer.setPersonalData((cname + " " + ctel).replaceAll(";", ","));
			         			}else{
			         				String lisa = (cname + " " + ctel).replaceAll(";", ",");
			         				if(customer.getPersonalData().indexOf(ctel) != -1){
			         					lisa = (cname ).replaceAll(";", ",");
			         				}
		         				    customer.setPersonalData(customer.getPersonalData() + "|" + lisa);
		         				}
		         				String cemail =  nn.getAttributes().getNamedItem("email").getNodeValue();
		         				customer.restPerEmail = cemail;
		         				
		  	         		
		  	         		 }}
		         		  }catch(Exception r){
		         			  
		         		  }
		         		      		  
		         		  try{
			         			 String city =  rida.getAttributes().getNamedItem("send_address_county").getNodeValue().replaceAll(";", ",") ;         	
				         		  customer.setCity(city);
			         		  }catch(Exception e){
			         			 try{
				         			 String city =  rida.getAttributes().getNamedItem("address_county").getNodeValue().replaceAll(";", ",") ;         	
					         		  customer.setCity(city);
			         			}catch(Exception es){

				         			  customer.setCity(" ");	
			         			}
			         		  }
		         		 
		         		  
		         		  try{
		         			 String price_list =  ensureLen(rida.getAttributes().getNamedItem("price_list").getNodeValue());         	
			         		   customer.setGroup(Converter.cleanLitLatEst(price_list));
			         		 
			       			   customer.addFixSortCode(Converter.cleanLitLatEst(price_list+"XX")); 
			       			custPRlists.put(customer.getID(), Converter.cleanLitLatEst(price_list));
		         		  }catch(Exception e){
		         		  }
		         		  
		
		         		  try{
		         			  String	 price_list =  rida.getAttributes().getNamedItem("price_list_special").getNodeValue() ;
		         			  
		         			  StringTokenizer ss = new StringTokenizer(price_list,",");
		         			  while (ss.hasMoreTokens()){
		         				  
                                   String as = ensureLen(ss.nextToken());
			         			  
                                  if(customer.getGroup() != null && customer.getGroup().trim().length() > 0){
			         				 customer.setGroup(customer.getGroup() + "|" + Converter.deCode(as));
					         	    }else{
			         				 customer.setGroup(Converter.deCode(as));
					         	   }
					         		
                                  
		         			  }
		         			  
			         		  }catch(Exception e){
			         		  }
		         		  
		         		 if(oOrders.get(customer.getID()) != null){
			            	 customer.addToCustDataVec(customer.getID() + ";tarasaldo; ;Open Order |" 
			                  +  oOrders.get(customer.getID()));
			              }


		         		 try {
		         			 
		         		//<data code="KL_SELVER_E_POOD" content="Jah"/>
		         		 for (int r = 0; r < rida.getChildNodes().getLength(); r++){
		   	         	  Node rid = rida.getChildNodes().item(r);
		   	         	 // System.out.println("node name " + rid.getNodeName());
		   	         	  if(rid.getNodeName().equals("data")){
		   	         		 String nn = rid.getAttributes().getNamedItem("code").getNodeValue();
		   	         		if(nn.equals("KL_SELVER_E_POOD")) {
			   	         		 Node uu = rid.getAttributes().getNamedItem("content");
			   	         		  if (uu != null && uu.getNodeValue().trim().length()>0
			   	         				  && uu.getNodeValue().contains("Jah")){
				                  	  this.eShopPrl.add(custPRlists.get(customer.getID())+"XX") ;      	
				                  }
			   	         		}
		   	         		
		   	         		if(nn.equals("KL_ARVE_MEIL")) {
		   	         		 Node uu = rid.getAttributes().getNamedItem("content");
		   	         		
		   	         	      if (uu != null && uu.getNodeValue().trim().length()>0){
			                  	customer.setConfirmEmail(uu.getNodeValue().trim().replaceAll(";", ","));          	
			                  }
		   	         		}
		   	         		
		   	         	  }
		         		 }
		         		 }catch(Exception e) {
		         			 e.printStackTrace();
		         		 }
		         		  
	         		 String salesman  = rida.getAttributes().getNamedItem("salesman").getNodeValue(); 
	         		      customer.addAgent(new Agent(Converter.deCode(salesman.toUpperCase())));
	         		      customer.setMainAgent(Converter.deCode(salesman.toUpperCase()));
	         		      String sm = parent.getEmail(salesman);
	         		      
	         		     Agent aa1 = (Agent)agent1.get(salesman);
	         	      	if(aa1 != null) {
	         	      	
	         	  		Vector vec = aa1.getDubbler();
	         	  		if(vec!= null && vec.size() >0){
	         	  			Enumeration en = vec.elements();
	         	  			while(en.hasMoreElements()){
	         	  				String age = (String) en.nextElement();
	         	  				customer.addAgent(new Agent(age));
	         	  			}
	         	  		}
	         	      }
	         		      
	         	      	if(mem != null && mem.get(customer.getID()) != null) {
	         	      		String mm = (String) mem.get(customer.getID());
	         	      	//	System.out.println("mem " +  customer.getID() + " " + mem);
	         	      		if(!mm.startsWith(salesman)) {
	         	      			customer.diffBack2 = true;
	         	      			customer.setInfo("Viimase telli tegi : " + mm.substring(0,mm.indexOf("|")) + " " +
	         	      			Converter.getDDsMMMsYYYYString(new Date(Long.parseLong(mm.substring(mm.indexOf("|")+1)))));
	         	      		}
	         	      	}
	         		      
	         		    
	                  if(sm != null && sm.length() > 1){
	                	//  System.out.println(sm);
	            	      tm.put(customer.getName(), customer);
	            	      custMap.put(customer.getID(),customer);
	                      this.customers.add(customer.getID());
	                      custAgs.put(customer.getID(),salesman.toUpperCase());
	                  }
	         	
		         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	    }
	              
	         	  }
	         	  
	         
	         }
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	    
	   if(rcompany != null){
	//    	  Converter.sendCustomerJson(tm, rcompany, false, null, null);
		    }
	     Customer cc = new Customer("1777");
	     cc.setName("Ladu tagastus");
	     cc.addAgent(new Agent("LADU"));
	   tm.put("Ladu tagastus", cc);
	   
	   unse =  this.getUnsoldEans();
	   Iterator itera = unse.keySet().iterator();
	   while(itera.hasNext()) {
		   String id = (String)itera.next();
		   Vector vv = (Vector)unse.get(id);
		   if(custMap.get(id) != null) {
			   Customer ccc = (Customer)custMap.get(id);
			   String name = Converter.deCode(ccc.getName());
				if(name.startsWith("Rimi")) {
					ccc.setInfo(ccc.getInfo() + "|NB! Müümata EAN koode : " + vv.size() + "|Kasuta soovituslikku sortimentti");
				}
			   
		   }
	   }
	   
	   System.out.println("kliendid b");
		   
     return tm;
   }
   

   private boolean isOkDate(String mmddyy){
	  	long lon = 0;
	  	Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(5, 7))-1);
	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(8, 10)));
	  // System.out.println(mmddyy + " " +cal.getTime()); 
	   Date date = new Date(lon);
	   date.setTime(cal.getTimeInMillis());
		Calendar ca = Calendar.getInstance();
		 int days = 30 ;
		   if(getParent().getProps().getProperty("sortdays") != null){
			   try{
				   days = Integer.parseInt(getParent().getProps().getProperty("sortdays"));
			   }catch(Exception e){
			   }
		   }
		
		ca.add(Calendar.DAY_OF_YEAR, -days);
		
		if(cal.after(ca)){
			return true;
		}else{
			return false;
		}
		
	  }
   
   private boolean isOkDate60(String mmddyy){
	  	long lon = 0;
	  	Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(5, 7))-1);
	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(8, 10)));
	  // System.out.println(mmddyy + " " +cal.getTime()); 
	   Date date = new Date(lon);
	   date.setTime(cal.getTimeInMillis());
		Calendar ca = Calendar.getInstance();
		ca.add(Calendar.DAY_OF_YEAR, -60);
		if(cal.after(ca)){
			return true;
		}else{
			return false;
		}
		
	  }
   
   private Date getDate(String mmddyy){
	  	long lon = 0;
	  	Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(5, 7))-1);
	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(8, 10)));
	  // System.out.println(cal.getTime()); 
	   Date date = new Date(lon);
	   date.setTime(cal.getTimeInMillis());
	    return date;
		
	  }
  
   
   private boolean isDueDate(String mmddyy){
	  	
	  	Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(5, 7))-1);
	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(8, 10)));
		Calendar ca = Calendar.getInstance();
		ca.add(Calendar.DAY_OF_YEAR, -1);
		
		if(cal.before(ca)){
			return true;
		}else{
			return false;
		}
		
	  }

   private Vector getCP(String custid , String number, String date, Date ddd,HashSet check ){

	   Vector result = new Vector();
	   File cpdata = new File("cpdata/" + number+".vec");
	   try{
	   if(cpdata.exists()) {
		   Vector vec = (Vector)Converter.readObject("cpdata/" + number+".vec");
		   for(int i = 0; i < vec.size(); i++) {
			   CustProd cp = (CustProd)vec.elementAt(i);
			   if(prodCodes == null) {
				   prodCodes = new HashSet();
			   }
			   if(	 !prodCodes.contains(cp.getProdID())){
			        prodCodes.add(cp.getProdID());	
		        }
		   }
		   return vec ;
	   }else {
	   
	 
			String httpsURL = "https://login.directo.ee/xmlcore/" + company + "/xmlcore.asp?what=invoice_row&get=1&number=" + number;
		       // System.out.println("httpsURL " + httpsURL);
				URL myurl = new URL(httpsURL);
		        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		        InputStream ins = con.getInputStream();
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);
		        
		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine =Converter.enCodeEst(inputLine);
		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ));
		        	StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
		        	while (st.hasMoreTokens()){
		        		
		        		String line = st.nextToken();
		        		int ind =  line.indexOf("item=") ;
		        		String toode = "";
			        	if(ind != -1){
			        		toode = line.substring(ind + 6);
			        		toode = toode.substring(0,toode.indexOf("\""));
			        	
			        	}
		        
			        	
			        	ind =  line.indexOf("quantity=") ;
		        		double kogus = 0;
		        		String kkk = "";
			        	if(ind != -1){
			        		kkk = line.substring(ind + 10);
			        		kkk = kkk.substring(0,kkk.indexOf("\""));
			        		 kogus = Double.parseDouble(kkk);
			        	}
			        	
			        	ind =  line.indexOf("total=") ;
			        	double hind = 0;
		        		 kkk = "";
			        	if(ind != -1){
			        		kkk = line.substring(ind + 7);
			        		kkk = kkk.substring(0,kkk.indexOf("\""));
			        		
			        		 hind = Double.parseDouble(kkk)/kogus;
			        		// System.out.println("toode "  + line + " "+ toode + " " + hind);
			        	}
			        	
		        if(toode != null && toode.trim().length() > 0){
		        	
		        
		        	if(!check.contains(custid+""+toode)) {
		        		CustProd cp = new CustProd(custid, toode, kogus ,0 ,0,date.substring(5,10),hind,0,0," ");
		        		cp.checkdate = ddd;
		        		  result.add(cp);
		        		  check.add(custid+""+toode);
		        			
		        	}else {
		        		CustProd cp = new CustProd(custid, toode, kogus ,0 ,0,date.substring(5,10),hind,0,0," ");
		        		cp.checkdate = ddd;
		        		  result.add(cp);
		        	}
			       if(prodCodes == null) {
			    	   prodCodes = new HashSet();
			       }
			      
			        if(!prodCodes.contains(toode)){
				        prodCodes.add(toode);	
			        }
		        }
		         }
		        	
		        }
		        in.close();
		        Converter.saveObject(result, "cpdata/" + number + ".vec");
		     //   Converter.saveFile("cpdata/" + number + ".txt", inputLineBuf.toString());
		      //  System.out.println("buffer " + result);
	   }
	   }catch(Exception e){
		   e.printStackTrace();
		   return null;
	   }
	   return result;
   }

   HashSet prodCodes = null;
   
   
   private Hashtable getPricelists(String code ){
	   Hashtable re = new Hashtable();
	   try {
			  File f = new File (getDir() + "hinnakiri.xml");
			//  System.out.println("siin matix");
		     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		     DocumentBuilder db;
			db = dbf.newDocumentBuilder();      	
	    	FileInputStream in = getCleanInputStream(f);
	    	String handler = null;
	        Document doc = db.parse(in,handler);
	        Node root = getNode(doc, "transport");      
	   
	    	Node read = getNode(root, "priceformulas");
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	        	  Node rida = read.getChildNodes().item(i);
	        	  try{
	        		  if(rida.getNodeName().equals("priceformula")){
	        			
	        			 String custID = " ";
	                  String clGroup = " ";
	                  String prodID = " ";
	                  String prodGroup = " ";
	                  String datafieldvalue = " ";
	                  String start = " ";
	                  String end = " ";
	                 try{
	                 	 try{
	                 	     clGroup = rida.getAttributes().getNamedItem("code").getNodeValue();
	                        }catch(Exception aa){}
	                 	 try{
	                 		datafieldvalue =  rida.getAttributes().getNamedItem("datafieldvalue").getNodeValue();  
                 	   }catch(Exception aa){ }
	                 	 try{
		                         prodID =  rida.getAttributes().getNamedItem("item").getNodeValue();  
	                 	   }catch(Exception aa){ }
	                 	 try{
	                             prodGroup =  rida.getAttributes().getNamedItem("class").getNodeValue();
	                 	  }catch(Exception aa){ }
	                 	  try{
		                             start =  rida.getAttributes().getNamedItem("start_date").getNodeValue();
		                    	  }catch(Exception aa){ }
		                    	  try{
			                           end =  rida.getAttributes().getNamedItem("end_date").getNodeValue();
			                    	  }catch(Exception aa){ }
	                 	  
	                 
	                     
	                      try{
	                     
	                    	 // double value = Double.parseDouble(rida.getAttributes().getNamedItem("discount").getNodeValue());
	                    	
	                    	  if(clGroup.equals(code)){
	                    		  
	                    	  }
	 	                     
	                    	  }catch(Exception aa){
	                    		  aa.printStackTrace();
	                    	  }
                    	 
	                     
	 	                     	                 
	                 }catch(Exception e){
	                 	//e.printStackTrace();
	                 }
	                 
	                }
	             	
	        	  }catch(Exception e){
	        		  e.printStackTrace();
	        	  }
	             
	        }
	        
		}catch(Exception e){
			e.printStackTrace();
		}
	   
	//   System.out.println("re " + re.toString());
	   return re;
   }
   
   TreeMap<String,Double> eanPrices = new TreeMap<String,Double>();
   Vector<String> christCodes = null;
   
   public TreeMap getProducts(Agent agent){
	   TreeMap otherStocks = this.getProductsOther();
	   selverEpoodProds = new Vector();
	 
	   boolean addShelf = false ;
	   try {
		getProdGroups2();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	   }
	   
     TreeMap tm = new TreeMap();
     HashMap prodHash = new HashMap();
     HashSet usedGroups = new HashSet();
     
     if(custProds.size() == 0){
         prodCodes = new HashSet();
    	 getCustomerProductsList2();
     }
  TreeMap stockAlter = new TreeMap();
     if(getParent().getProps().getProperty("stockdir") != null ){
    	
    	 stockAlter = this.getProducts3();
    	//System.out.println("stockalter " + stockAlter.size());
     }

     
     
     
     Vector noGrups = new Vector();
     Vector codeEnds = new Vector();
     Vector noTexts = new Vector();
     Vector yesClasses = new Vector();
     Vector noBrands = new Vector();
     String groupIndent = null;
     String typeIndent = null;
         TreeMap brands = new TreeMap();
         boolean sortBR = false;
     
     if(getParent().getProps().getProperty("codeends") != null){
     	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("codeends"),",");
     	while (st.hasMoreElements()){
     		codeEnds.addElement(st.nextToken());
     	}
     }
     
     if(getParent().getProps().getProperty("yesclasses") != null){
      	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("yesclasses"),",");
      	while (st.hasMoreElements()){
      		yesClasses.addElement(st.nextToken());
      	}
      }
     
     if(getParent().getProps().getProperty("usedataforgroups") != null){
         groupIndent = getParent().getProps().getProperty("usedataforgroups");
      	prGroups = new TreeMap();
      }
     

     if(getParent().getProps().getProperty("usedatafortypes") != null){
         typeIndent = getParent().getProps().getProperty("usedatafortypes");
      	prTypes = new TreeMap();
      }
     
     if(getParent().getProps().getProperty("nogroups") != null){
     	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("nogroups"),",");
     	while (st.hasMoreElements()){
     		noGrups.addElement(st.nextToken());
     	}
     }
     
     
     if(getParent().getProps().getProperty("notexts") != null){
     	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("notexts"),",");
     	while (st.hasMoreElements()){
     		noTexts.addElement(st.nextToken().toLowerCase());
     	}
     }
    
     if(getParent().getProps().getProperty("addShelf") != null){
    	 addShelf = true;
      }
    
     Hashtable prices = null;
     
     if(getParent().getProps().getProperty("usepricelist") != null){
     	String pr = getParent().getProps().getProperty("usepricelist");
     	prices = getPricelists(pr);
     }
     
    
     if("true".equals(getParent().getProps().getProperty("sortbybrands"))){
     	try {
 			brands = this.getProdGroupsSecond();
 			sortBR = true;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
     
     if(getParent().getProps().getProperty("nobrands") != null){
     	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("nobrands"),",");
     	while (st.hasMoreElements()){
     		noBrands.addElement(st.nextToken());
     	}
     }
     
     try {
 
    	   FileReader fr = new FileReader(getDir() + "tooted.xml");
           BufferedReader reader = new BufferedReader(fr);
         String line = null;

         line = reader.readLine();
         
      //   System.out.println("suurus " +prodsInUse.size());
          while((line = reader.readLine()) != null  ){
        	  
        	  if((line.trim().length()) > 30 ){
        	
        	      
        	  int len = line.indexOf("code=");
        	  String id   = line.substring(len +6);
        	  id = id.substring(0,id.indexOf("\""));
      	     Product prod = new Product(id);
      	   
      	      len = line.indexOf("default_name=");
      	    String namec  = line.substring(len + 14);
      	     	 namec = namec.substring(0,namec.indexOf("\""));
      	     	 String nameb = "";
      	     	 
      if(line.indexOf("long_description=") != -1) {
    	  len = line.indexOf("long_description=");
     	     nameb  = line.substring(len + 18);
     	     	 nameb = nameb.substring(0,nameb.indexOf("\""));
      }
      	     
           	     	 
           	     	 
      	       String shelf = "";
      	    if(addShelf){
      	     len = line.indexOf("shelf=");
          	  shelf   = line.substring(len +7);
          	 shelf = shelf.substring(0,shelf.indexOf("\""));
          	 shelf = " " + shelf ;
      	    }
      	    
      	    String url = null;
      	 
       	     len = line.indexOf("pic_url=");
       	  if(len != -1){
        		  url   = line.substring(len +9);
            	 url = url.substring(0,url.indexOf("\""));
          }
           	
       	  
       	 prod.setPrice(1);
  	     
     	  
      	 len = line.indexOf("base_price=");
      	   if(len == -1){
      		 len = line.indexOf("web_price=");
      		String price   =  line.substring(len + 11);  
 	    	// System.out.println(" siin " +  prod.getName() + " " + price);
 	    	 try{

 	      	    prod.setPrice(Double.parseDouble(price.substring(0,price.indexOf("\""))) ); 
 	    	 }catch (Exception e){
 	    		prod.setPrice(1);
 	    	 }
      	   }else{
      		 String price   =  line.substring(len + 12);  
       	      prod.setPrice(Double.parseDouble(price.substring(0,price.indexOf("\""))));
       	   }
       	  
       	  
      			String add = "";
      	    len = line.indexOf("stripe_code=");
      	    if(len != -1){
      	    	 String ean   = line.substring(len + 13);
      	    	 ean = ean.substring(0,ean.indexOf("\""));
      	    	ean =  ean.replaceAll(";", ":");
      	    	if(ean.contains("&")) {
      	    		ean = ean.substring(0,ean.indexOf("&"));
      	    	}
      	    	ean =  ean.replaceAll(";", ":");
          	     prod.setEAN(ean);
          	     prod.setGroup(ean);
          	     if(prGroups1.get(ean) == null) {
          	    	prGroups1.put(ean, ean);
          	     }
          	     
          	     
          	     
          	     if(eanPrices.get(ean) == null && prod.getPrice() > 0) {
          	    	eanPrices.put(ean, prod.getPrice());
          	     }
          	     
          	     
          	   if(prod.getPrice() < 0.005 && eanPrices.get(ean) != null) {
        	    	 prod.setPrice(eanPrices.get(ean));
        	     }
          	     
          	   if("true".equals(getParent().getProps().getProperty("add.ean"))){
          		   try{
          				add = ean.substring(ean.length()-4); 
          	       }catch(Exception e ){
          			   
          		   }
          		 }
      	    }
      	   
      	
      	  namec = namec.replace("´", "'");
      	  nameb = nameb.replace("´", "'");
      	  
       	//<data code="ART_ASUKOHT" content="92.01.07"/>
   	 	 String pesa = "";
   	 	 len = line.indexOf("data code=\"ART_ASUKOHT\""); 
   	 	 boolean goes = true;
   	 	 //siin oli false
   	 	 if(len != -1){
   	 		 try {
   	 		pesa =  line.substring(len + 33); 
   	     	pesa = pesa.substring(0,pesa.indexOf("\""));
              //System.out.println("pesa " + pesa );
              goes = true;
        
   	 		 }catch (Exception e) {
   	 			 
   	 		 }
   	 	 }
   	  len = line.indexOf(   "data code=\"ART_SELVER__E_POOD\""); 
	
	 	 if(len != -1){
	 		 try {
	 		pesa =  line.substring(len + 40); 
	     	pesa = pesa.substring(0,pesa.indexOf("\""));
         if(pesa.contains("Jah")) {
              selverEpoodProds.add(prod.getID());
          }
	 		 }catch(Exception e) {
	 			 
	 		 }
  //   System.out.println("Etooted " + pesa  );
	 	 }
   	 	
   	 	 
   	 	 if(company.equals("agapics_lv")) {
   	 		 goes = true;
   	 	 }
      	  
      	  //	 prod.setName((shelf + " " + pesa + " " +Converter.enCodeEst(namec + " " + nameb) + " " + id + " " + add ).trim()); 
      	   prod.setName((Converter.enCodeEst(namec + " " + nameb) + " " + id + " " + add ).trim()); 
    		
        	  if(getParent().getProps().getProperty("vat") != null){
        		  prod.setVat(getParent().getProps().getProperty("vat"));
        	  }
      	    
      	    
      	    	
      	 
           	 len = line.indexOf("unit=");
           	 if(len != -1){
           	   String unit   =  line.substring(len + 6);  
 	          prod.setUnit(unit.substring(0,unit.indexOf("\"")));
   	   		 }
           	 
           	 String kast = "package2=";
           	 
           	 
           	len = line.indexOf(kast);
          	 if(len != -1){
          	   String incase   =  line.substring(len + 10); 
          	   try{
          		   double uh = Double.parseDouble(incase.substring(0,incase.indexOf("\"")));
          		   prod.setInCases(uh);
          		   prod.setOptAmountInUnits(uh);
          		   
          	   }catch(Exception e){
         		   e.printStackTrace();
          	   }
	         }
          	 
          	 if(url!=null){
          		 
          		 prod.setPhotoid("phurl:" +url); 
          	 }
          	 
           	 
      	 
      	if(noTexts.size() > 0){
      		for(int u = 0 ; u < noTexts.size(); u++){
      			String aa = noTexts.elementAt(u) + "";
      			if(namec.toLowerCase().contains(aa)){
      				//System.out.println("goes maha 1");
          			goes = false;	
      			}	
      		}
      	}

     	  
      	
	if(codeEnds.size() > 0){
      		for(int u = 0 ; u < codeEnds.size(); u++){
      			String aa = codeEnds.elementAt(u) + "";
      			if(id.toLowerCase().endsWith(aa.toLowerCase())){
      				//System.out.println("goes maha 2");
          			goes = false;	
      			}	
      		}
      	}
	 
      		 len = line.indexOf("class=");
      	 	 if(len != -1){
      	 	     String grupp   = line.substring(len + 7);
      	 	     grupp =   grupp.substring(0,grupp.indexOf("\"")).toUpperCase();
         	    // prod.setGroup(Converter.cleanLitLatEst(grupp));
         	   //  System.out.println("prGroupsSeos.get(grupp) " + prGroupsSeos.get(grupp) + " " + grupp + " " + prod.getID());
         	     if(prGroupsSeos.get(grupp) != null) {
         	    	 // Siin on vaja võtta ylemine grupp
         	    	  prod.setGroupSecond(Converter.cleanLitLatEst(prGroupsSeos.get(grupp)+""));
         	    	 
         	    	 // prod.setGroup(Converter.cleanLitLatEst(prGroupsSeos.get(grupp)+""));
         	     }else {
         	    	  prod.setGroupSecond(Converter.cleanLitLatEst((grupp)));
         	     }
         	     
      	 	  // siin vaja leida kas on kaylem grupp olemas
        	 if( typeIndent !=  null){
            		
              		 len = line.indexOf("<data code=\"" +typeIndent + "\"");
              		 int lll = ("<data code=\"" +typeIndent + "\"").length();
               	    if(len != -1){
               	    	 String  gr   = line.substring(len + lll + 10);
               	    	 gr = gr.substring(0,gr.indexOf("\""));
               	    	 prod.setType(gr); 
                   	 }
              	}
        	 
        	 // jõulud christmas statustop = 5
        	    
      	 	  if( groupIndent !=  null){
            		
           		 len = line.indexOf("<data code=\"" +groupIndent + "\"");
           		 int lll = ("<data code=\"" +groupIndent + "\"").length();
            	    if(len != -1){
            	    	 String  gr   = line.substring(len + lll + 10);
            	    	 
            	    	 gr = gr.substring(0,gr.indexOf("\""));
            	    	 prod.setGroup(gr);
                	   this.prGroups.put(gr, gr);  
                	 }
           		
           	}
      	 	  
      	 	 if(christCodes != null ){
      	 		 
      	 	if( christCodes.contains(grupp) ) {
      	 		prod.setStatusTop("5");
      	 		prod.setType("Christmas");
      	 	      } else {
      	 		prod.setType("Everyday");
      	 	      }
      	 	 }
      	 	  
      	 	 }

      	 	 len = line.indexOf("data code=\"GENERAL\""); 
      	 	 if(len != -1){
      	 	   String    areto =  line.substring(len + 29); 
      	 	      areto = areto.substring(0,areto.indexOf("\""));
                // System.out.println("areto " + areto );
      	 	      prod.addBarcode(areto);

      	 	 }
      	 	 
      	 	 
      	 	 String areto = "";
      	 	 len = line.indexOf("data code=\"ALK_REG\""); 
      	 	 if(len != -1){
      	 	      areto =  line.substring(len + 29); 
      	 	      areto = areto.substring(0,areto.indexOf("\""));
                // System.out.println("areto " + areto );

      	 	 }

      	 	 len = line.indexOf("data code=\"TELEMA_EXP\""); 
      	 	 if(len != -1){
      	 	     goes = true;

      	 	 }    
      	 	 
      	 	 
      	 	 len = line.indexOf("data code=\"ART_AKTIIVNE\" content=\"Ei\""); 
      	 	 if(len != -1){
      	 	     goes = false;

      	 	 }   
      	 	 
      	 	 len = line.indexOf("data code=\"STATUSS\" content=\"PAS1"); 
      	 	 if(len != -1){
      	 	     goes = false;

      	 	 }   
      	
      	 	 double mahto = 0;
      	 	 String maht = "";
      	 	 len = line.indexOf("data code=\"KOGUS\""); 
      	 	 if(len != -1){
      	 		 try{ 
      	 		 maht =  line.substring(len + 18); 
      	 	      maht = maht.substring(0,maht.indexOf("\""));
                 //  System.out.println("maht " + maht );
                  
                	   mahto = Double.parseDouble(maht.replace(',', '.'));
                   }catch(Exception e){
                	   
                   }
      	 	 }
      	 	 
      	 	 
      	 	if("true".equals(getParent().getProps().getProperty("stocktozero"))){
      	 		prod.setStock(0);
           	 }
      	 	 
      	 	 
      	 	  	 len = line.indexOf("<stocklevels>");
      	 	 if(len != -1){
      	 	     String stock   = line.substring(len + 13); 
      	 	     //System.out.println(stock.substring(0,stock.indexOf("stocklevels")));
      	 	     stock = stock.substring(0,stock.indexOf("stocklevels"));
      	 	 if( getParent().getProps().getProperty("stockcode") != null){
      	 		 
      	 		 int ladu = stock.indexOf("stock=\"" + getParent().getProps().getProperty("stockcode") + "\"");
      	 		// System.out.println(ladu);
      	 		 if(ladu != -1){
      	 			 String seis = stock.substring(ladu+5);
      	 			// System.out.println("seis " + seis);
      	 					 seis = seis.substring(seis.indexOf("level=") + 7);
      	 					 seis = seis.substring(0, seis.indexOf("\""));
      	 					 try{
      	 						prod.setStock( Double.parseDouble(seis)); 
      	 					 }catch (Exception e){
      	 						e.printStackTrace(); 
      	 					 }
         		 }
      	 	 }
     	         // prod.setStock((int)Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
     	 	 }else{
     	 		len = line.indexOf("stock_level_total=");
         	 	 if(len != -1){
         	 	     String stock   = line.substring(len + 19);  
        	          prod.setStock((int)Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
        	 	 }
         	
     	 	 }
      	 	 
      	 	 
      	 	 if(stockAlter.get(prod.getID()) != null){
      	 		 prod.setStock((Double)stockAlter.get(prod.getID()));
      	 	//  System.out.println("olemas "+ prod.getID() +" ");
        	  
      	 	 }
      	 	 
      	   if(otherStocks.get(prod.getID()) != null){
        		 try{
    				 prod.setStock((int)Double.parseDouble(otherStocks.get(prod.getID())+""));  
    	      		}catch(Exception t){
    				} 
        	   }
      	 	 
      	 	 
      	  if(line.indexOf("ARTIKKEL_KAMP") != -1){
      	    	prod.setStatusDiscount("1");
      	    }
      	    	  

      	 
      	if(!prodCodes.contains(prod.getID())){
    
      	  if(prod.getStock() > 0){
      		//  goes = true;
      	  }
      	}
      	
    
      	    if(goes ){
      	    	if(!usedGroups.contains(prod.getGroup())){
      	    		usedGroups.add(prod.getGroup());
      	    	}
      	    	
      	    	if(!usedGroups.contains(prod.getGroupSecond())){
      	    		usedGroups.add(prod.getGroupSecond());
      	    	}
      	    	
      	    	prodHash.put(prod.getID(), prod);
      	    	prod.setNameSorting(prod.getEAN() + prod.getStock() + prod.getName());
      	         tm.put(prod.getName(), prod);	
      	        }
      	      }
          }
    
     } catch (Exception e){
       e.printStackTrace();
     }
     
     //teeme puhtaks grupi mapi 
     TreeMap grups  = new TreeMap();
    Iterator itera =  prGroups.keySet().iterator();
    while (itera.hasNext()){
    	String key = itera.next()+"";
    	if(usedGroups.contains(key)){
    		grups.put(key,prGroups.get(key) );
    	}
    }
    
    prGroups = grups;
         
     return tm;
   }
   
   private TreeMap prods = null;
 
   
   
   @Override
	public void FixLoader() {
	
	   
	
		TreeMap contracts = Converter.loadFixFromExcels("excelsort" , prods, parent);
		System.out.println("Contracts " + contracts.size() + " " +  contracts.toString());
		System.exit(0);
		
	}
   
   TreeMap eans = new TreeMap();
   TreeMap prodEans = new TreeMap();
   
   public TreeMap getProducts2(){

	    TreeMap tm = new TreeMap();
	    eans = new TreeMap();
	    prodEans = new TreeMap();
	    prods = new TreeMap();
	     try {
	 
	    	   FileReader fr = new FileReader(getDir() + "tooted.xml");
	           BufferedReader reader = new BufferedReader(fr);
	            String line = null;

	          while((line = reader.readLine()) != null  ){
	        	 
		        	
	        	  if((line.trim().length()) > 50){
	        		 // System.out.println(line);
	        		  int len = line.indexOf("code=");
		        	  String id   = line.substring(len + 6);
		      	      Product prod = new Product(id.substring(0,id.indexOf("\"")).trim());
		      	      len = line.indexOf("default_name=");
		      	     String name   = line.substring(len + 14);
		      	     
		      	   String pesa = "";
		     	 	 len = line.indexOf("data code=\"ART_ASUKOHT\""); 
		     	 	 boolean goes = false;
		     	 	 if(len != -1){
		     	 		 try {
		     	 			pesa =  line.substring(len + 33); 
			     	     	pesa = pesa.substring(0,pesa.indexOf("\""));
			                //System.out.println("pesa " + pesa );
			                goes = true; 
		     	 		 }catch(Exception e) {
		     	 			 
		     	 		 }
		     	 		
		          	 }
		      	     prod.setName(Converter.deCode(name.substring(0,name.indexOf("\""))) + prod.getID());
		      	     //prod.setName( pesa + Converter.deCode(name.substring(0,name.indexOf("\""))) + prod.getID());
		      	   //  System.out.println(prod.getName());
		      	     
		      	   len = line.indexOf("stripe_code=");
		      	    if(len != -1){
		      	    	 String ean   = line.substring(len + 13);
		      	    	 ean = ean.substring(0,ean.indexOf("\""));
		      	    	ean =  ean.replaceAll(";", ":");
		      	    	if(ean.contains("&")) {
		      	    		ean = ean.substring(0,ean.indexOf("&"));
		      	    	}
		      	    	ean =  ean.replaceAll(";", ":");
		          	     prod.setEAN(ean);
		          	     ArrayList arr = (ArrayList)eans.get(ean);
		          	     if(arr == null) {
		          	    	 arr = new ArrayList();
		          	     }
		          	     arr.add(prod.getID());
		          	   eans.put(ean, arr);
		          	   prodEans.put(prod.getID(), ean);
		      	    }
		      	    
		   
		    	 	 len = line.indexOf("data code=\"ART_EAN_KONVERT\""); 
		   
		      	    if(len != -1){
		      	    	try {
		      	    	 String ean   = line.substring(len + 37);
		      	    	 ean = ean.substring(0,ean.indexOf("\""));
		      	    	ean =  ean.replaceAll(";", ":");
		      	    	if(ean.contains("&")) {
		      	    		ean = ean.substring(0,ean.indexOf("&"));
		      	    	}
		      	    	ean =  ean.replaceAll(";", ":");
		      	    	if(!ean.contains("=")) {
		      	    	    prod.setEAN(ean);
		      	    	       }
		      	    	}catch(Exception e) {
		      	    		
		      	    	}
		          	 }
		      	    
		      	     tm.put(prod.getID(), prod); 
	        	  }
	        	
	          }	    	 
	    
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	     return tm;
	     
	   }

   
   public TreeMap getProducts3(){
	   TreeMap tm = new TreeMap();
	    
	     
	     try {
	 
	    	   FileReader fr = new FileReader(getParent().getProps().getProperty("stockdir") + "tooted.xml");
	           BufferedReader reader = new BufferedReader(fr);
	         String line = null;

	          while((line = reader.readLine()) != null  ){
	        	 
		        	
	        	  if((line.trim().length()) > 50){
	        		  int len = line.indexOf("code=");
		        	  String id   = line.substring(len + 6);
		        	  id = id.substring(0,id.indexOf("\""));
		        	 // System.out.println(id);
		      	 	  	 len = line.indexOf("<stocklevels>");
		          	 	 if(len != -1){
		          	 	     String stock   = line.substring(len + 13); 
		          	 	     //System.out.println(stock.substring(0,stock.indexOf("stocklevels")));
		          	 	     stock = stock.substring(0,stock.indexOf("stocklevels"));
		          	 	 if( getParent().getProps().getProperty("stockcode") != null){
		          	 		 
		          	 		 int ladu = stock.indexOf("stock=\"" + getParent().getProps().getProperty("stockcode") + "\"");
		          	 		// System.out.println(ladu);
		          	 		 if(ladu != -1){
		          	 			 String seis = stock.substring(ladu+5);
		          	 			// System.out.println("seis " + seis);
		          	 					 seis = seis.substring(seis.indexOf("level=") + 7);
		          	 					 seis = seis.substring(0, seis.indexOf("\""));
		          	 					 try{
		          	 					tm.put(id, Double.parseDouble(seis)); 
		          	 					 }catch (Exception e){
		          	 						e.printStackTrace(); 
		          	 					 }
		             		 }
		          	 	 }
		         	         // prod.setStock((int)Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
		         	 	 }else{
		         	 		len = line.indexOf("stock_level_total=");
		             	 	 if(len != -1){
		             	 	     String stock   = line.substring(len + 19);
		             	 	     tm.put(id, Double.parseDouble(stock.substring(0,stock.indexOf("\"")))); 
		            	 	 }
		             	 }
		      	  }
	          }	    	 
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	     
	     return tm;
	     
	   }

   private String  assortment = null;
   
   private TreeMap getUnsoldEans() {
	   TreeMap res = new TreeMap();
	   // peame siis kliendi haaval tekitama puuduolevad tooted mida polemüüdud viimase 60p jooksul fix sordiga võrreldes
	   // 1. meil on vaja kliendi listi kellel on Fix sortiment olmas
	   if(custPRlists == null) {
		   this.getCustomers(null);
	   }
	  // System.out.println("custPRlists " + custPRlists.size());
	   // loeme sisse fix sortide eanid.
	   if(fixEans == null) {
		   this.getFixedAssortmentData();
	   }
	  // System.out.println("fixEans " + fixEans.size());
	   //nüüd teeme valmis müüdud toodete eanObjekti
	   if(last60Sold == null) {
		   getCustomerProductsList2();
	   }
	   TreeMap soldEans = new TreeMap();
	  // System.out.println("last60Sold " + last60Sold.size());
	   
	 
	   Iterator itera = last60Sold.keySet().iterator();
	   while (itera.hasNext()) {
		   String clid = (String)itera.next();
		   Vector vv = (Vector)last60Sold.get(clid);
	      for(int a = 0 ; a < vv.size(); a++) {
	    	  String prid = (String)vv.get(a);
	    	  
	    	  Vector aa = (Vector)soldEans.get(clid);
	    	  if( aa == null) {
	    		  aa = new Vector();
	    	  }
	    	  String ea = (String)prodEans.get(prid);
	    	  
	    	 // System.out.println("ea ja prod " + ea + " " + prid);
	    	  if(ea != null) {
	    		  if(!aa.contains(ea)) {
	    			aa.add(ea);
	    			soldEans.put(clid, aa);
	    		  }
	    		  }
	      }
	   }
	   
	//   System.out.println("soldEans " + soldEans.size() + " " + prodEans.size());
	   
	   
	   // nyyd vaatame kes mida poel ostnud
	   
	   TreeMap unsoldEans = new TreeMap();
	   
	   Iterator iterax = soldEans.keySet().iterator();
	   while(iterax.hasNext()) {
		   String cuid = (String)iterax.next();
		   String clFix =(String) custPRlists.get(cuid);
		   if(clFix != null) {
		   Vector vv = (Vector)soldEans.get(cuid);
		   Vector fv = (Vector)fixEans.get(clFix);
		   if(fv != null) {
			   for(int a = 0; a < fv.size(); a++) {
				   String prf = (String)fv.get(a);
				   if(!vv.contains(prf)) {
					 Vector us = (Vector) unsoldEans.get(cuid); 
					 if(us == null) {
						 us = new Vector();
					 }
					 us.add(prf);
					 unsoldEans.put(cuid, us);
				   }
			   }
		   }
	   }}

	   return unsoldEans;
   };
   
  private TreeMap unSoldWarning = null;
  private TreeMap unse = null;
   
   public String getAssortmentStr(Agent agent){
	   unSoldWarning = new TreeMap();
	   StringBuffer sb = new StringBuffer();
	   if(unse == null) {
		   unse =  this.getUnsoldEans();
	   }
	    
	// Nyyd on vaja igale kliendile teha sortiment mis on puudu daga ainult need mis on 
	 
	 Iterator itera = unse.keySet().iterator();
	 while(itera.hasNext()) {
		 String cuid = (String)itera.next();
		 String ag = " ";
		 if(custAgs.get(cuid) != null) {
			 ag = custAgs.get(cuid)+"";
		 }
		 Customer cc = (Customer)custMap.get(cuid);
			if(cc != null) {
				String name = Converter.deCode(cc.getName());
				if(name.startsWith("Rimi")) {
					
		 Vector vv = (Vector)unse.get(cuid);
		 for(int a = 0 ; a< vv.size() ;a++) {
			 String prodID = vv.get(a)+"";
			 ArrayList arr = (ArrayList) eans.get(prodID);
             //  System.out.println(" " + arr.toString());
			 // siin sii spanna ka kliendile kylge hoiatus 
			 if(arr != null) {
          	for(Object pr : arr) {
          		
          	     sb.append(cuid + "; ; ;" + pr + "; ; ; ; ;," + ag + ","+ "\n");
          	
               }
			 } 
		 }}}
	 }
	  // System.out.println(sb.toString().substring(0,50000));
    return sb.toString();
  }

   private Hashtable dueSums = new Hashtable();
   private Hashtable usedCredit = new Hashtable();

   public Vector makeDebtlist(){
	   //1.teeme klientide arvete nimekirja .
	   Vector debts = new Vector();
	   Enumeration enums = this.custDebtUsed.elements();
	   while(enums.hasMoreElements()){
		String custid = (String)enums.nextElement();
		
		   try{
			   String cuurl =  URLEncoder.encode(custid, "UTF-8");
				
			 	String httpsURL = "https://login.directo.ee/xmlcore/" + company + "/xmlcore.asp?what=invoice&get=1&orderer=" + cuurl;
		     //  System.out.println(httpsURL);
			 	Converter.doTrustToCertificates();
			 	URL myurl = new URL(httpsURL);
		        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		        InputStream ins = con.getInputStream();
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);

		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine = Converter.enCodeEst(inputLine);
		        //	System.out.println(inputLine);
		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\"l\"","\"L\""));
		        	StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
		        	while (st.hasMoreTokens()){
		        		
		        		String line = st.nextToken();
		        		String number = "";
			        	String date = "";
			        	String duedate = "";
			        	String volg = "";
			        	String payer = "";
		        		
			        	int ind =  line.indexOf("invoice number=") ;
		        		if(ind != -1){
			        		 number = line.substring(ind + 16);
			        		 number = number.substring(0,number.indexOf("\""));
			        	}
			        	
			        	ind =  line.indexOf("date=") ;
				        	if(ind != -1){
				        		 date = line.substring(ind + 6);
				        		 date = date.substring(0,date.indexOf("\""));
				        	}
				        		
				        	
			        	 ind =  line.indexOf("due_date=") ;
			        	if(ind != -1){
			        		 duedate = line.substring(ind + 10);
			        		duedate = duedate.substring(0,duedate.indexOf("\""));
			        	 }
			        	
			        	 ind =  line.indexOf("regno=") ;
				        	if(ind != -1){
				        		 payer = line.substring(ind + 7);
				        		 payer = payer.substring(0,payer.indexOf("\""));
				        	 }
				        	
			        	
			        	ind =  line.indexOf("balance=") ;
				        	if(ind != -1){
				        		 volg = line.substring(ind + 9);
				        		 volg = volg.substring(0,volg.indexOf("\""));
				        	 }
				        //	System.out.println(custid  + " " + number + " " + date  + " " + duedate  + " " + volg );	
				       
			        	try{
			        		 double vv = Double.parseDouble(volg);
			        		 if(vv != 0){
			        			 if(isDueDate(duedate)){
						        		     Debt dd = new Debt();
						     				  dd.setCustID(custid);
						     				  
						     				  dd.setDoknr(number);
						     				  dd.setDate(getDate(date));
						     				  dd.setDuedate(getDate(duedate));
						     				  
						     				  int ff = (int)((Calendar.getInstance().getTimeInMillis() - getDate(duedate).getTime())/86400000);
						     				  dd.setDue(ff + "");
						     				  dd.setSum(vv);
						     				//	System.out.println("aaa" + date + " " + duedate + " " + number + " " + custid + " " +ff);	
								        			  
						     				  if(dueSums.get(custid) != null){
						     					  double ss = Double.parseDouble(dueSums.get(custid) + "")+ vv ;
						     					 dueSums.put(custid, ss);    
						     				  }else{
						     					 dueSums.put(custid, volg); 
						     				  }
						     				 debts.add(dd);

									        		
						        	 }
			        			 

			     				  if(usedCredit.get(custid) != null){
			     					  double ss = Double.parseDouble(usedCredit.get(custid) + "")+ vv ;
			     					 usedCredit.put(custid, ss);    
			     				  }else{
			     					 usedCredit.put(custid, volg); 
			     				  }
			        			 
			        		 }
				        	
				        	  
			        	}catch(Exception e){
			        		//e.printStackTrace();
			        	}
				        
			        	 
		        	}           	
		        }
		      //  System.out.println(inputLineBuf);
		        in.close();
		        
		       
		   } catch (Exception a){
			 //  a.printStackTrace();
		   }
		   
	   }
	   
	     System.out.println("debts size" + debts.size());
	   
	   return debts;
   }
   private Vector custProds = new Vector();
   
   
   public Vector getCustomerProductsList(String agentCode){
	 // return new Vector();
	   return custProds;
   }
   
   private HashMap customerSales = null;
   
   public Vector getCustomerProductsList2(){
	   //1.teeme klientide arvete nimekirja .
	    custProds = new Vector();
	    customerSales = new HashMap();
	    
		   File cpdatadir = new File("cpdata");
		   if(!cpdatadir.exists()) {
			   cpdatadir.mkdir();
		   }
	    
	    HashSet check = new HashSet();
	
	   Calendar cal = Calendar.getInstance();
	   String d = cal.get(Calendar.DAY_OF_MONTH)+ "";
	   if(d.length() == 1){
		   d = "0" + d;
	   }
	   String m = (cal.get(Calendar.MONTH) +1)+ "";
	   if(m.length() == 1){
		   m = "0" + m;
	   }
	   
	   int days = 30 ;
	   if(getParent().getProps().getProperty("sortdays") != null){
		   try{
			   days = Integer.parseInt(getParent().getProps().getProperty("sortdays"));
		   }catch(Exception e){
		   }
	   }
	   String end = d + "-" +m + "-" + cal.get(Calendar.YEAR)+ "";
	   cal.add(Calendar.DATE, - days);
	   cal.set(Calendar.DAY_OF_MONTH, 1);
	   
	    d = cal.get(Calendar.DAY_OF_MONTH)+ "";
	  
	   if(d.length() == 1){
		   d = "0" + d;
	   }
	    m = (cal.get(Calendar.MONTH) +1)+ "";
	   if(m.length() == 1){
		   m = "0" + m;
	   }
		   String start =  d + "-" +m + "-" + cal.get(Calendar.YEAR)+ "";
		  // System.out.println(start + " uhuu " + end);
		   
		   Enumeration enums = this.custtsUsed.elements();
	   while(enums.hasMoreElements()){
		String custid = (String)enums.nextElement();
		   try{
			  // String httpsURL = "https://login.directo.ee/xmlcore/" + company + "/xmlcore.asp?get=1&what=itemclass";
			   Converter.doTrustToCertificates();
			   custid = Converter.deCode(custid);
			   String cuurl =  URLEncoder.encode(custid, "UTF-8");
				String httpsURL = "https://login.directo.ee/xmlcore/" + company + "/xmlcore.asp?what=invoice&get=1&orderer=" + cuurl 
				+ "&invoicedate1=" + start + "&invoicedate2=" + end;
		     //  System.out.println(httpsURL);
				URL myurl = new URL(httpsURL);
		        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		        InputStream ins = con.getInputStream();
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);

		        HashSet tehtud = new HashSet();
		        
		        TreeMap malu = new TreeMap();
		        
		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine = Converter.enCodeEst(inputLine);
		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\"l\"","\"L\""));
		        	StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
		        	while (st.hasMoreTokens()){
		        		
		        		String line = st.nextToken();
		        	//	System.out.println(" line " + line);
		        		int ind =  line.indexOf("invoice number=") ;
		        		String number = "";
			        	if(ind != -1){
			        		 number = line.substring(ind + 16);
			        		number = number.substring(0,number.indexOf("\""));
			        		// System.out.println(number);
			        	}
			        	 ind =  line.indexOf("date=") ;
			        	if(ind != -1){
			        		String date = line.substring(ind + 6);
			        		date = date.substring(0,date.indexOf("\""));
			        		// System.out.println(date + " " + number + " " + custid);	
				        		malu.put(date+custid+ number, number+"@"+date);
			        		
			        	  }		
		        	}
		        	
		        	   Iterator itera= 	malu.keySet().iterator();
				        Vector arr = new Vector();
			        	while(itera.hasNext()) {
			        		arr.add((String)itera.next());
			        	}
			        //	System.out.println("  222 "  + arr.toString() );	
		        	Vector aa = upsideVector(arr);
		      
		        	//System.out.println( "  333 " + aa.toString() );	
		        	//muutsin siin järjekorda aa ei kasuta hetkel
		        	
		        	for(int u = 0 ; aa.size() > u ;u++) {
		        		String key = (String)aa.get(u);
		        		String vahe = (String) malu.get(key);
		        		String date = vahe.substring(vahe.indexOf("@")+1);
		        		String number = vahe.substring(0, vahe.indexOf("@"));
		        	 	Calendar cal2 = Calendar.getInstance();
		        	    cal2.set(Calendar.YEAR, Integer.parseInt( date.substring(0, 4)));
		        	    cal2.set(Calendar.MONTH, Integer.parseInt(date.substring(5, 7))-1);
		        	    cal2.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(8, 10)));
		        	  // System.out.println(mmddyy + " " +cal.getTime()); 
		        	   Date ddd = new Date(0);
		        	   ddd.setTime(cal2.getTimeInMillis());
		        	
		        	
		        	 if(isOkDate(date)){
				        		
		     					Vector cps = this.getCP(Converter.deCode(custid),number,date , ddd,check);

		     					if(cps != null){
		     				if(this.isOkDate60(date)) {
		     					if(last60Sold == null) {
		     						last60Sold = new TreeMap();
		     					}
		     					Enumeration eee = cps.elements();
	     						while( eee.hasMoreElements()){
	     							CustProd cp = (CustProd) eee.nextElement();
	     							Vector v = (Vector)last60Sold.get(cp.getCustID());
	     							if(v == null) {
	     								v = new Vector();
	     							}
	     							v.add(cp.getProdID());
	     							last60Sold.put(cp.getCustID(), v);
		     					}
		     				}
		     						
		     						Enumeration eee = cps.elements();
		     						while( eee.hasMoreElements()){
		     							CustProd cp = (CustProd) eee.nextElement();
		     						//	siin siis vaja koguda kliendi müüki 
		     							//cp.checkdate
					        		//	 System.out.println(cp.checkdate.toLocaleString() + " " + (lastYearMonth(cp.checkdate))+ " " + date + "  222 " + number + " " + custid  + " " + cps);			     					
		 				        		
		     							if(thisMonth(cp.checkdate)) {

		     								Toode tt = new Toode("tt");
		     								if(customerSales.get(custid) != null) {
		     									tt = (Toode)customerSales.get(custid);
		     								}
		     								tt.setDonecur(tt.getDonecur() + cp.getLastAmount()*cp.getLastPrice());
		     								customerSales.put(custid, tt);
		     							}
                                        if(lastYearMonth(cp.checkdate)) {
                                        	Toode tt = new Toode("tt");
		     								if(customerSales.get(custid) != null) {
		     									tt = (Toode)customerSales.get(custid);
		     								}
		     								tt.setDonevol(tt.getDonevol() + cp.getLastAmount()*cp.getLastPrice());
		     								customerSales.put(custid, tt);
		     								if(custid.equals("10558")) {
		     									System.out.println("10558 "  + number + " " + cp.getProdID()+" "+  cp.getLastAmount()*cp.getLastPrice()+" "+tt.getDonevol());
		     								}

		     							}
		     							if(!tehtud.contains(cp.custID+ cp.getProdID())) {
		     							   custProds.add(cp);
		     							}
		     							tehtud.add(cp.custID+ cp.getProdID());
			     					}			     				
				        		}
		     				}
		        	}
		        	
		        	
		        }
		       
		        Converter.saveFile("data/sort"+custid+ ".txt" , inputLineBuf.toString());
		        in.close();
		        isr.close();
		        ins.close();
		        con.disconnect();
		      

		   } catch (Exception a){
		   }
	   }
	  // System.out.println("customerSales " + customerSales.size());
	   
	   return custProds;
   }
   
   private boolean thisMonth(Date dd) {
	   
	   Calendar cal = Calendar.getInstance();
	   Calendar gg = Calendar.getInstance();
	   gg.setTime(dd);
	   if(cal.get(Calendar.YEAR) == gg.get(Calendar.YEAR) && cal.get(Calendar.MONTH) == gg.get(Calendar.MONTH) ) {
		   return true;
	   }
	   
	   return false;
   }
   

   private boolean lastYearMonth(Date dd) {
	   
	   Calendar cal = Calendar.getInstance();
	   Calendar gg = Calendar.getInstance();
	   gg.setTime(dd);
	   cal.add(Calendar.YEAR, -1);
	   if(cal.get(Calendar.YEAR) == gg.get(Calendar.YEAR) && cal.get(Calendar.MONTH) == gg.get(Calendar.MONTH) ) {
		   return true;
	   }
	   
	   return false;
   }
   
   
   private TreeMap last60Sold = null;

   public  Vector upsideVector(Vector inVec){
	   Vector sorted = new Vector(inVec.size());
        while(inVec.size() > 0){
        	sorted.add(inVec.get(inVec.size()-1));
        	inVec.remove(inVec.size()-1);
        }
        return sorted;
      }
   
   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "DirectoAgapics";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
 

  public double getSalary(String agentCode){
     return 0;
   }

   private String getDir(){
     String dir = getParent().getProps().getProperty("readin");
     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
     return dir;
   }


   public static final String SEPARATOR = "|";
   public static final String DATA_FILE = "DATA.TXT";
   public static final String SALES_BILLS_FILE = "orders.log";
   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private Vector debtList = new Vector();
   private Vector fixedLines = new Vector();
   private Vector prodsInUse = new Vector();
   private Vector pricelists = new Vector();
   private Vector custtsUsed = new Vector();
   private Vector custDebtUsed = new Vector();
   

  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {

	Iterator itera =   agent1.keySet().iterator();
	while(itera.hasNext()){
	
	String agentID = (String) itera.next();
		
	  try{

			String httpsURL = "https://login.directo.ee/xmlcore/sirekar_hulgi/xmlcore.asp?what=sale&get=1&code=" + agentID;
		       // System.out.println("httpsURL " + httpsURL);
				URL myurl = new URL(httpsURL);
				Converter.doTrustToCertificates();
			    HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		        InputStream ins = con.getInputStream();
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);
		        
		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine =Converter.enCodeEst(inputLine);
		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ));
		        	StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
		        	while (st.hasMoreTokens()){
		        		String line = st.nextToken();
		        		//<sale code="EVA" total="10136.23"></sale>
		        		int ind =  line.indexOf("total=") ;
		        		String summa = "";
			        	if(ind != -1){
			        		summa = line.substring(ind + 7);
			        		summa = summa.substring(0,summa.indexOf("\""));
			                Agent agent = (Agent)agents.get(agentID);
			                 if (agent != null){
			                   agent.setMonthSales((int)Double.parseDouble(summa));
			        	  // System.out.println("agsales0 " + agentID +" "+ summa);     	
			        	}
			                // System.out.println("agsales1 " + agentID +" "+ summa);     
		        	}
		        }}  
	  }catch(Exception e){
		  e.printStackTrace();
	  }	
		
		
		}  
	  }

@Override
public String getSaveStockInfo() {

	   StringBuffer products = new StringBuffer();
  	 
      
 try{
	 Converter.doTrustToCertificates();
   String httpsURL = "https://login.directo.ee/xmlcore/" + company + "/xmlcore.asp?get=1&what=item";
    if(getParent().getProps().getProperty("stockcode") != null){
    	httpsURL = httpsURL + "&stock=" +  getParent().getProps().getProperty("stockcode");
	  }
  System.out.println(httpsURL);
  URL  myurl = new URL(httpsURL);
  HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
   InputStream ins = con.getInputStream();
   InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
   BufferedReader in = new BufferedReader(isr);

    String inputLine = "";
    StringBuffer inputLineBuf = new StringBuffer();
   while ((inputLine = in.readLine()) != null){
   	inputLine = Converter.enCodeEst(inputLine);
   	inputLineBuf.append(inputLine.replaceAll("<item","\n<item" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
   }

   in.close();
   
   if(inputLineBuf.length() > 2){
         try {
       	  File f = new File (getDir() + "tooted.xml");
       	  if(f.exists())f.delete();
       	Converter.saveFileInUTF8(getDir() + "tooted.xml", inputLineBuf.toString());
      	
   	  }catch (Exception ex) {
   		ex.printStackTrace();
   	  }
    }
   

   //lõpeb toodetega tegelemine 

   System.out.println("Tooted tehtud" );

}catch(Exception e){
	e.printStackTrace();
}
 
 
 

 try {
	 

	   FileReader fr = new FileReader(getDir() + "tooted.xml");
       BufferedReader reader = new BufferedReader(fr);
     String line = null;

     //line = reader.readLine();
     
  //   System.out.println("suurus " +prodsInUse.size());
      while((line = reader.readLine()) != null  ){
    	  
    	  if((line.trim().length()) > 40 ){
    	  int len = line.indexOf("code=");
    	  String id   = line.substring(len +6);
    	  id = id.substring(0,id.indexOf("\""));
 	 len = line.indexOf("<stocklevels>");
 	 double seisx = 0; 
if(len != -1){
    String stock   = line.substring(len + 13); 
    stock = stock.substring(0,stock.indexOf("stocklevels"));
if( getParent().getProps().getProperty("stockcode") != null){
	 int ladu = stock.indexOf("stock=\"" + getParent().getProps().getProperty("stockcode") + "\"");
	 if(ladu != -1){
		 String seis = stock.substring(ladu+5);
				 seis = seis.substring(seis.indexOf("level=") + 7);
				 seis = seis.substring(0, seis.indexOf("\""));
				 try{
					seisx = ( Double.parseDouble(seis)); 
				 }catch (Exception e){
					e.printStackTrace(); 
				 }
	 }
     }
} else {
	len = line.indexOf("stock_level_total=");
	 if(len != -1){
	     String stock   = line.substring(len + 19);  
      seisx = ((int)Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
	 }
}

if(seisx > 0) {
//	System.out.println("Seisx "+ id +" " + seisx);
 products.append(id +  "; ;");
 products.append(getParent().getNF().format(seisx) + "; "  +  "\n");
}

       	  }}
      reader.close();
    	  }catch(Exception tt){
    			 
    	  }

 
 return products.toString();
 
}





@Override
public void WebReporter() {
	
	StringBuffer sb = new StringBuffer();
	if(custMap == null) {
		prodCodes = new HashSet();
		this.getCustomers(null);
		this.getProducts(null);
	}
	TreeMap unsEans = this.getUnsoldEans();
	Calendar ccc = Calendar.getInstance();
	Calendar then = Calendar.getInstance();
	then.add(Calendar.DAY_OF_YEAR, -60);
	
	String periood = "Periood " + Converter.getDDpMMpYYYYpString(then) 
	+ " -> " + Converter.getDDpMMpYYYYpString(ccc) + "<br><br>";
	
	Iterator itera = unsEans.keySet().iterator();
	
	while(itera.hasNext()) {
		String cuid = (String)itera.next();
		Vector uns = (Vector)unsEans.get(cuid);
		Customer cc = (Customer)custMap.get(cuid);
		if(cc != null) {
			String name = Converter.deCode(cc.getName());
			if(name.startsWith("Rimi")) {
			sb.append(name + "\n");
			for (Object ob : uns) {
				sb.append(ob+ "\n");
			}
			sb.append( "\n");
			}
		}
	}
	
	//System.out.println(sb.toString());
	try {
		
	
	Email mail = new Email(null);
	   
		
    mail.setSMTP(parent.getProps().getProperty("smtphost"));
    if(parent.getProps().getProperty("smtpuser") != null)
    mail.setUser(parent.getProps().getProperty("smtpuser"));
    
    if(parent.getProps().getProperty("smtppass") != null)
    mail.setPass(parent.getProps().getProperty("smtppass"));
    
    if(parent.getProps().getProperty("smtpport") != null)
    mail.setPort(parent.getProps().getProperty("smtpport"));
      	
    mail.setSubjectFilesTo("Müümata ean koodid " + Converter.getDDpMMpYYYYpString(then) 
	+ " -> " + Converter.getDDpMMpYYYYpString(ccc)  );
 		
		mail.setFrom(parent.getProps().getProperty("from"));
		

	
		String lisa = "";
		
		mail.setEncoding("utf-8");
		mail.setBodyHtml("<br>" + periood + sb.toString().replaceAll("\n", "<br>") + "<br><br>");
		mail.setSendNotice(false);
		
	 StringTokenizer st = new StringTokenizer(parent.getProps().getProperty("unsold.email"),",");
	 while(st.hasMoreTokens()) {
	    mail.setFilesTo(st.nextToken());
		mail.send(); 
	 }
   	
	}catch(Exception e) {
		e.printStackTrace();
	}
	System.exit(0);
	
}
}
