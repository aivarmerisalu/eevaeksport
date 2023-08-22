package eevaeksport.imp.driver;

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
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.Product;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;
import eevaeksport.util.FileListFilter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:l
 * @author Tarmo Tubro
 * @version 1.0
 */

public class ErplyLttImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public ErplyLttImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
  //  trouble = "CREDITPROBLEM";
    String url = parent.getProps().getProperty("erply.url");
    if(url == null){
 	   url = "https://www.erply.net/api/";
    }
 	try {
 		myurl = new URL(url);
 	} catch (MalformedURLException e) {
 		e.printStackTrace();
 	}
 	if("true".equals(parent.getProps().getProperty("topeltkontroll"))){
 		topeltkontroll = true;
 	}
 	
    username = parent.getProps().getProperty("erply.user");
    company = parent.getProps().getProperty("erply.company");
    password = parent.getProps().getProperty("erply.pass");
    apiversion = parent.getProps().getProperty("erply.apiversion");
    if(apiversion == null){
 	   apiversion = "1.0";
    }
    returntype = parent.getProps().getProperty("erply.returntype");
    if(returntype == null){
 	   returntype = "XML";
    }
    userstuff = "&version=" + apiversion + "&responseType=" + returntype + "&username=" + username 
    + "&password=" + password;
    
    
    session = getSessionId();
    while(session.indexOf("%") != -1){
 	   session = getSessionId();
    }
    customers = getCustomers(null);
    products = getProducts(null);
    
    try {
      Class.forName(databaseDriverName).newInstance();
    } catch (Exception e){
      // parent.showError(e);
    }
  }

  private String getSessionId(){

	  String data = "clientCode=" + company + "&request=verifyUser" + userstuff ;
	//  System.out.println(data);
	  
	  try {
	String res = (postData(data)).toString();
	//System.out.println(res);
	session = res.substring(res.indexOf("sessionKey") + 11 ,res.indexOf("</sessionKey"));
	//System.out.println(session);
	} catch (Exception e) {
		try{
			String res = (postData(data)).toString();
			session = res.substring(res.indexOf("sessionKey") + 11 ,res.indexOf("</sessionKey"));
		}catch(Exception d){
			d.printStackTrace();
		}
		
	}
	 
	  return session;
  }

 
  private StringBuffer postData(String data) throws Exception {
	  HttpURLConnection urlc = null;
	  StringBuffer inputLineBuf = new StringBuffer();
	  try{
	  urlc = (HttpURLConnection) myurl.openConnection();
	  try{
	  urlc.setRequestMethod( "POST" );
	  } catch (ProtocolException e) {
	    throw new Exception("Shouldn't happen: HttpURLConnection doesn't support POST??", e);
	  }
	  urlc.setDoOutput(true);
	  urlc.setDoInput(true);
	  urlc.setUseCaches(false);
	  urlc.setAllowUserInteraction(false);
	  OutputStream out = urlc.getOutputStream();
	  try {
		out.write( data.getBytes() );
	  } catch (IOException e){
	  throw new Exception("IOException while posting data", e);
	  } finally {
	  if (out != null)
	  out.close();
	  }
	  InputStream in = urlc.getInputStream();
	  try{
	  Reader reader = new InputStreamReader(in);
	    InputStreamReader isr = new InputStreamReader(in,"UTF-8");
	    BufferedReader inr = new BufferedReader(isr);
	    String inputLine;

	    while ((inputLine = inr.readLine()) != null){
	        inputLine = Converter.enCodeEst(inputLine);	// enCode(inputLine);	
	    	inputLineBuf.append(inputLine.replaceAll("/>","/>\n"));
	  }

	     inr.close();
	  
	  
	  reader.close();
	  } catch (IOException e)
	  {
	  throw new Exception("IOException while reading response", e);
	  } finally
	  {
	  if (in != null)
	  in.close();
	  }
	  } catch (IOException e)
	  {
	  throw new Exception("Connection error (is server running at " + myurl + " ?): " + e);
	  } finally
	  {
	  if (urlc != null)
	  urlc.disconnect();
	  }
	  return inputLineBuf;
	  }
	  


  

 
	
	  
  
   
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  
  
  public String identifyCustomer(String id,String regnr)  {
		if(this.ilns.get(id) != null){
			// System.out.println("klientILN olemas " + id + " " +  (String)ilns.get(id));
			return (String)ilns.get(id);
		}  
	
		// System.out.println("klientinteREG  " + id + " " + (String)custInteReg.get(regnr+id) + " " + regnr+id);
			
		if(this.custInteReg.get( regnr+id) != null){
		//	 System.out.println("klientinteREG olemas " + id + " " + (String)custInteReg.get(regnr+id));
			return (String)custInteReg.get(regnr+id);
		}
		
		if(this.custInte.get(id) != null){
		 //System.out.println("klientinte olemas " + id + " " + (String)custInte.get(id));
		return (String)custInte.get(id);
	}
	if(this.customers.get(id) != null){
		// System.out.println("klient olemas " + id);
		return id;
	}
	

    return null;
  }


  
  public String identifyProduct(String ean, String prodID) throws SQLException{ 
	 
	  
	//  System.out.println("sisse2" + ean + " " + prodID );
	  if(prodID != null && prodID.trim().length() > 0 && this.products.get(prodID) != null){
		//  System.out.println("toode olemas " + prodID);
		  Product pr = (Product)products.get(prodID);
		  return pr.getAlegrupp();
	  }
	  
	  if(prodID != null && prodID.trim().length() > 0 && this.codes.get(prodID) != null){
			//  System.out.println("toode olemas " + prodID);
			  return (String)codes.get(prodID);
			 
		  }
	  
	  
	  if(ean != null && codes != null && codes.get(ean.trim()) != null){
			 // System.out.println("ean olemas 1  " + ean);
			  return (String)codes.get(ean.trim());
		  } 
	  
	  
	  if(ean.equals("4743269002618")){
			System.out.println("olemas2 " + ean);
		}
	  
	  if(ean != null && eans != null && eans.get(ean.trim()) != null){
		 // System.out.println("ean olemas 1  " + ean);
		  return (String)eans.get(ean.trim());
	  } 
	  
	  //paneme nulli ka algusesse
	  if(ean != null && eans != null && eans.get("0" + ean.trim()) != null){
			 // System.out.println("ean olemas 1  " + ean);
			  return (String)eans.get("0" +ean.trim());
		  } 
	  
	  
	  
	  
	  if(prodID != null  && prodID.trim().length() > 0 && eans != null && eans.get(prodID) != null){
	//		  System.out.println("ean olemas 2 " + ean);
			  return (String)eans.get(prodID);
		  } 
	  while(ean != null && ean.trim().startsWith("0")){
			ean = ean.trim().substring(1);
			// System.out.println("ean olemas nulli koristajaga " + ean);
			  if(ean != null && eans != null && eans.get(ean) != null){
				 
				  return (String)eans.get(ean);
			  }  
	  }
	  
	  if(prodID != null && prodID.trim().length() > 10){
		  while(prodID != null  && prodID.trim().startsWith("0")){
			  prodID = prodID.trim().substring(1);
				// System.out.println("prodID olemas nulli koristajaga " + prodID);
				  if(ean != null && eans != null && eans.get(prodID) != null){
					  return (String)eans.get(prodID);
				  }  
		       }
		  if(ean != null && eans != null && eans.get("0" + prodID) != null){
				 // System.out.println("ean olemas 1  " + ean);
				  return (String)eans.get("0" + prodID);
			  } 
	  
	  }
	    
	  
      return "none";
  }
  
  

  public void makeOrder(Order order) throws SQLException{
         counter = 0;
       
         userstuff = "&clientCode=" + company +"&version=" + apiversion + "&responseType=" + returntype + "&sessionKey=" + session;         
		//System.exit(0);
        
        	  makeOutput(order);   
         
       
  }
  
  private String getTarne(Order order){
	    Calendar cal = Calendar.getInstance();
	    
	  //  date	eg. 2010-01-29
	    String result = null ;
	    if (order != null && order.getShipCal() != null && "true".equals(parent.getProps().getProperty("useshipdate"))){//
	    	
	    	Calendar dat = order.getShipCal();
	    	
	    	String yy = (dat.get(Calendar.YEAR))+"";

	    	  String mm = (dat.get(Calendar.MONTH)+1)+"";
	    	  if(mm.length()== 1){
	    	  	mm = "0"+mm;
	    	  }
	    	  
	    	  
	    	  String dd = (dat.get(Calendar.DATE))+"";
	    	  if(dd.length()== 1){
	    	  	dd = "0" + dd;
	    	  }
	    	  
	    	  
	    	  result = yy +"-" + mm +"-" + dd;
	    	 System.out.println("TARNEP�EV SHIP0  " + result + " " + dat.get(Calendar.DATE));
	    	  
	    }else{
	     String yy = (cal.get(Calendar.YEAR))+"";

		  String mm = (cal.get(Calendar.MONTH)+1)+"";
		  if(mm.length()== 1){
		  	mm = "0"+mm;
		  }
		  
		  int d = cal.get(Calendar.DATE);
		  String dd = d+"";
		  if(dd.length()== 1){
		  	dd = "0" + dd;
		  }
		  

		//  result = yy.substring(2,4) + mm + dd;
		  result = yy +"-" + mm +"-" + dd;
	    }
	  
	  	 System.out.println("TARNEP�EV SHIP1  " + result );
		  
	    return result;
	  }
  
  
  private boolean checkTopelt(Order order){
	  String num = order.getDokNr();
	  String lineSep = System.getProperty("line.separator");
	    
	  if(hasRepreatNums(num)){
		return true;  
	  }else{
		    writeConsloStr( "repreatnums.txt",num + "," + Calendar.getInstance().getTimeInMillis() + lineSep);  
	  }
	    
	
  
	  return false;
  }

  public boolean hasRepreatNums(String ordnum) {

	    File f = new File( "repreatnums.txt");
	    if (f.exists()){
	    try{

		      InputStreamReader is = new FileReader(f);
		      BufferedReader br = new BufferedReader(is);
		      String line = null;
		      while( ( line = br.readLine()) != null){
		      	String id = null;
		      
		        StringTokenizer st = new StringTokenizer(line, ",");
		        if (st.hasMoreTokens()){
		        	id = st.nextToken();
		           if(id.equals(ordnum)){
		        	   return true;
		           }
		        }
		    }
		      is.close();
		      br.close();
		      
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    }
	    return false;
	  }
  
	public void writeConsloStr(String path,String e) {
		
		try {
			
			File f = new File(path);
			if(!f.exists()){
				  try{
				      FileOutputStream fos = new FileOutputStream(path);
				        fos.write("".getBytes());
				        fos.close();
				
			} catch (Exception uu) {
				uu.printStackTrace();
			}
			}
			
			PrintWriter os = new PrintWriter(new FileWriter(
					path, true));
			os.write(e);
			os.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
}

	
	private void makeOutput(Order order){
	  boolean senddata = true;
	  //pane see tagasi true pealemuidu ei tule sisse infot .
	  trouble = new StringBuffer();
	  if(topeltkontroll && order.getStatus() == 98){
		  if(checkTopelt(order)){
			  trouble.append("Tellimuse number Kordub " + order.getDokNr() + " , tellimust ei Imporditud Erply-se");
			  senddata = false;
		  }
	  }
	  
	  double miinus = 1;
	  
	  if( "true".equals(parent.getProps().getProperty("returnmode"))){
		  miinus = -1;
	  }
	  
	     String aiai = parent.getProps().getProperty("testing");
	     if(aiai != null && aiai.equals("true")){
	    	 
	     }else{
	    	 try {
	 	 		String orderdata = "";
	 	 		boolean attr = false;
	 	 		//System.out.println("reference " + order.getReference());
	 	 		if(order.getReference() != null && order.getReference().trim().length() != 0){
	 	 			attr = true;
	 	 		}
	 	 		boolean kapp = false;
	 	 		Calendar cal = Calendar.getInstance();
	 	 		//System.out.println("kapp " + order.getStatus());
	 	 		
	 	 		String firma = order.getCompany();
	 	 		
	 	 		if(order.getStatus() == 100){
	 	 			kapp = true;
	 	 			cal.setTime(order.getShipCal().getTime());
	 	 			if(firma.indexOf("|") != -1 ) {
		 	 			firma = firma.substring(0,firma.indexOf("|"));
		 	 		}
	 	 		}
	 	 		
	 	 		SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
	 	 		String time = time_format.format(Calendar.getInstance().getTime());
	 	 		
	 	 	     StringBuffer inputLineBuf = new StringBuffer();
	 	     	 Vector lines = order.getOrderLines();
	 		      for (int i = 0; i < lines.size(); i++) {
	 		    	  OrderLine line = (OrderLine) lines.elementAt(i);
	 		    	  double kordaja = 1;
	 		    	   if(order.getStatus() == 80){
	 		    		   if("DPA".equals(line.getUnit())){
	 		    			   kordaja = line.getBonus();
	 		    		   }
	 		    	   }
	 		    	  if( line.getToode() != null &&  line.getToode().trim().length() != 0){
	 		    		  
	 		    		   double discount = 0;
	 		    	       if(line.getDiscount() > 0 && (order.getStatus() < 3 || order.getStatus() == 96)){
	 		    	    	   discount = line.getDiscount();
	 		    	       }
	 		    	       if(order.getStatus() == 60){
	 		    	    	   discount = 0;
	 		    	       }
	 		    	       
	 		    	  String attrib = "";
	 		    	  if(attr){
	 		    		  attrib = "&attributeName" +  (counter) + "=order_referenceNumber&attributeType"+(counter) + 
	 		    		  "=text&attributeValue" + (counter) + "=" + order.getReference();  
	 		    	  }
	 		    	  
	 		    	
	 		    	  String attrib2 = "";
	 		    	  if(kapp){
	 		    		  attrib2 = "&attributeName" +  (counter) + "=line_info&attributeType"+(counter) + 
	 		    		  "=text&attributeValue" + (counter) + "=" + line.selActCode + " aeg:" + time;  
	 		    	  }
	
	 		    	  
	 		    	  String prodID = "&productID" + (counter) + "=" + line.getToode();
	 		    	  if(line.getToode().equals("none")){
	 		    		  prodID =  "&itemName" + (counter) + "=" + (line.getTooteNimi() + " " + line.getEAN()).trim();
	 		    	  }
	 		    	  
	 		    	  orderdata=orderdata+ prodID + 
	 		    	  "&amount" + (counter) + "=" + ( miinus * line.getKogus()* kordaja) + attrib + attrib2 ;
	 		    	  if(discount > 0){
	 		    		  orderdata=orderdata+ "&discount" + (counter) + "=" + discount;
	 		    	  }
	 		    	//  + "&price" + (counter+1) + "=20";"&vatrateID" + (counter+1) + "=1&"	    	
	 	       counter++;
	 		    	  }
	 	      }
	 	 
	 		      String comment = "";
	 		        
	 		        if(order.getInfo() != null){
	 		        	comment = order.getInfo().replaceAll("\n"," ");
	 		        	comment = order.getInfo().replaceAll(","," ");
	 		        	String comm = comment.trim();
	 		        	comment = "internalNotes=" + comm + "&";

	 		 	 		if(parent.getProps().getProperty("erply.addpublic") != null){
	 		 	 			comment = comment +  "notes=" + comm + "&";
	 		 		        	  }
	 		 	 		

	 		 	 		if(parent.getProps().getProperty("erply.onlypublic") != null){
	 		 	 			comment = "notes=" + comm + "&";
	 		 		        	  }
	 		 	 		
	 		 	 		if(parent.getProps().getProperty("erply.onlypack") != null){
	 		 	 			comment = "packingUnitsDescription=" + comm + "&";
	 		 		        	  }
	 		 	 		
	 		 	 		
	 		 	 		if(comment.startsWith("r ")){
	 		 	 			comment = comment.substring(2);
	 		 	 		}
	 		        }
	 	 		String date = getTarne(null);
	 	 		String sdate = getTarne(order);
	 	 		
	 		       String custID = "customerID=" + firma;
	 		        
	 	 	try {
	 	 	  try {
	 	 		  String payerID = "";
	 	 		  String addressID = "";
	 	 		  if(firma != null && customers.get(firma) != null){
	 	 			  Customer cust = (Customer)customers.get(firma) ;
	 	 			  if(cust.getPayer() != null && cust.getPayer().trim().length() != 0){
	 	 				 payerID = "&payerID=" + cust.getPayer() +"&shipToID="+firma;
	 	 				 
	 	 				if("true".equals(parent.getProps().getProperty("erply.newAPI"))){
	 		 	 			custID = "customerID=" + cust.getPayer();
	 		 	 		}
	 	 			  }
	 	 			
	 		 	 		
	 		 	 		
	 		 	 		
	 	 			
	 		 	 		
	 		 	 		if( cust.getAnkeediTyyp() != null && cust.getAnkeediTyyp().length() > 0 &&
	 		 	 				 "true".equals(parent.getProps().getProperty("erply.addAdressID"))){
	 		 	 			addressID = "&addressID=" + cust.getAnkeediTyyp() ; 
	 		 	 		  }
	 	 		          }
	 	 		  
	 	 		 String confirm = "";
	 	 		  boolean valis = false;
	 	 		 if( "true".equals(parent.getProps().getProperty("erply.valisnoconfirm")) &&
	 	 				 order.getStatus() > 6){
	 	 			 valis = true;
	 	 		 }
	 		    
	 	 		  if( parent.getProps().getProperty("erply.confirm") != null || valis){
	 	 			 confirm = "confirmInvoice=0&"; 
	 	 		  }
	 	 	
	 	 		
	 	 		  
	 	 		String doctype = "ORDER";
	 	 		if( "true".equals(parent.getProps().getProperty("returnmode"))){
	 	 			doctype = "CREDITINVOICE&creditInvoiceType=RETURN"; 
	 	 		  }
	 	 		
	 	 		if( order.getStatus() == 1){ // on pakkumine 
	 	 			doctype = "OFFER"; 
	 	 		  }
	 	 		
	 	 		if(kapp) {
	 	 			doctype = "WAYBILL";
	 	 			//confirm = "confirmInvoice=1&"; 
	 	 		}
	 	 		
	 	 		String wareinfo = "";
	 	 		if(parent.getProps().getProperty("kapid") != null && order.getWarehouse() != null && order.getWarehouse().trim().length() > 0){
	 	 			wareinfo= "&warehouseID=" + order.getWarehouse();
	 	 		}
	 	 		
	 	 		String shipmode = "";
	 	 		
	 	 		String shm = parent.getProps().getProperty("shipmodeID");
	 	 		if(shm != null) {
	 	 			shipmode= "&deliveryTypeID=" + shm;
	 	 		  }
	 	 		
	 	 		if( order.getShipMode() != null && order.getShipMode().trim().length() > 0){
	 	 				shipmode= "&deliveryTypeID=" + order.getShipMode().trim();
	 	 		}
	 	 		
	 	 		
	 	 		String contact = "";
	 	 		if( order.getContactID() != null && order.getContactID().trim().length() > 0 ){
	 	 			String cc = order.getContactID().trim();
	 	 			if(cc.indexOf(":") != -1) {
	 	 				cc = cc.substring(0, cc.indexOf(":"));
	 	 			}
	 	 			contact= "&contactID=" + cc;
	 	 		}
	 	 		
	 	 		String delAllProds = "";
	 	 		
	 	 		if( order.fullDelyveryOnly != null && order.fullDelyveryOnly.trim().length() > 0 ){
	 	 			delAllProds = "&deliveryOnlyWhenAllItemsInStock=1" ;
	 	 		}
	 	 		
	 	 		
	 	 		
	 	 		  String dat = "request=saveSalesDocument&type=" + doctype + "&currencyCode=EUR&" + confirm + custID + addressID+payerID+
	 				"&employeeID=" + order.getAgent() + "&date=" + date + "&deliveryDate=" + sdate + "&" + comment + wareinfo + shipmode + contact + delAllProds + orderdata +  userstuff;
	 	 		  System.out.println(dat);
	 	 			
	 	 		  if(senddata){
	 	 			inputLineBuf = postData(dat);
	 	 		  }
	 	 		
	 	 		  
	 	 			
	 	 		} catch (Exception e1) {
	 	 			e1.printStackTrace();
	 	 		 
	 	 		}
	 	 	
	 	 	 
	 	 	} catch (Exception e) {
	 	 	} 
	 	      
	 	 	
	 	 	if(inputLineBuf.toString().length() < 5){
	 	  this.trouble.append("Tellimust ei imporditud " + order.getCompName() + " " + order.getDokNr() );
	 	 		// System.exit(0);
	 	 	}
	 	 	
	 		if(inputLineBuf.toString().contains("errorCode>1009")){
	 		 	  this.trouble.append("Tellimust ei imporditud 1009 viga " + order.getCompName() + " " + order.getDokNr() );
	 		 	 		// System.exit(0);
	 		 	 	}
	 	 	
	 		if(inputLineBuf.toString().contains("errorCode>1054")){
	 		 	  this.trouble.append("Tellimust ei imporditud 1054 viga " + order.getCompName() + " " + order.getDokNr() );
	 		 	 		// System.exit(0);
	 		 	 	}
	 	      setNumAddedLines(counter); 
	 	      
	 	    String doknr = order.getDokNr();
	 	      String fn = doknr;
	 	      if(doknr == null || doknr.trim().length()== 0){
	 	    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
	 	      }	    
	 	      
	 	      
	 	      String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + fn  +  ".xml";
	 	     //   System.out.println(fileName);
	 	        FileOutputStream fos = new FileOutputStream(fileName);
	 	        
	 	        fos.write(inputLineBuf.toString().getBytes());
	 	        fos.close();
	          //   System.out.println(inputLineBuf);
	 		} catch (Exception e) {
	 			e.printStackTrace();
	 		} 
	     }
	 	
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

 
  public String getDriverVersion(){
    return "ErplyLtt";
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
  
  
  private FileInputStream getCleanInputStream(File xmlFile)throws FileNotFoundException, IOException {
		FileInputStream in = new FileInputStream(xmlFile);
		int bomKount = getBOMCount(xmlFile);
		if (bomKount > 0) {
		byte[] buf = new byte[bomKount];
		in.read(buf);
		}
		return in;
		}

  private String getDir(){
	     String dir = parent.getProps().getProperty("readin");
	     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	     return dir;
	   } 
  
  public TreeMap getCustomers(Agent agent){
	     TreeMap tm = new TreeMap();
	   //  makeData();
	    this.custInte = new TreeMap();
	    this.ilns = new TreeMap();
	    this.custInteReg = new TreeMap();
		    
	   try {
		   
		   FileListFilter fil = new FileListFilter("kliendiderp","xml");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int i = 0; i < ll.length; i++) {
	    		
	    		
	    		 String name = ll[i];
	    		
	    	//	 System.out.println(name);
	 		  File f = new File (getDir() + name);
		   
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "document");      
	        
	     	Node read = getNode(root, "records");
	     	if(read != null){
	     	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	         for (int s = 0; s < read.getChildNodes().getLength(); s++){
	         	  Node rida = read.getChildNodes().item(s);
	         	  try{
	         		 if(rida.getNodeName().equals("item")){
	         			
	         		  String id = Converter.deCode( getNode(rida,"customerID").getFirstChild().getNodeValue());
	         		  Customer customer = new Customer(id);
                 
	         		  try{
	         		  String namec =   getNode(rida,"fullName").getFirstChild().getNodeValue();
	         		  customer.setName(Converter.deCode(namec));
                      }catch(Exception ee){
	                     customer.setName(id); 
                      }
	         		  try{ 
	 	         		 String aadr =   getNode(rida,"address").getFirstChild().getNodeValue();
	 	         		  customer.setAddress(Converter.deCode(aadr));
	         		 }catch(Exception ee){
		         		 customer.setAddress(""); 
	         		 }
	         		 
	         		 
	         		 
	         		 
	         		 
	         		 
	         		 
	         		 try{ 
	 	         		 Node aadr =   getNode(rida,"addresses");
	 	         		
	 	         		 for (int ss = 0; ss < aadr.getChildNodes().getLength(); ss++){
	 	         			
	 	  	         	  Node uuu = aadr.getChildNodes().item(ss);
	 	  	         		 if(uuu.getNodeName().equals("item")){
	 	  	         		  String addrID =  Converter.deCode(getNode(uuu,"addressID").getFirstChild().getNodeValue());
			 	        	 // System.out.println("aadrID " + addrID);
			 	        	 customer.setAnkeediTyyp(addrID);
		         			 }
	 	  	         	
		         		 
	         		 }
	 	  	          
	 	         		 
         		 }catch(Exception ee){
         			customer.setAnkeediTyyp("");
         		 }
	         		 
	         		 
	         		 
	         		 
	         		  try{ 
		 	         		 String intrCode =   Converter.deCode(getNode(rida,"integrationCode").getFirstChild().getNodeValue());
		 	         		  customer.setCity((intrCode));
		 	         		  this.custInte.put(intrCode, id);
		 	         		  if(intrCode.length() > 6){
		 	         			 this.custInte.put(intrCode.substring(0,6), id);
		 	         		  }
		         	
		 	         		String code =   Converter.deCode(getNode(rida,"code").getFirstChild().getNodeValue());
		 	         		  this.custInteReg.put(code + intrCode, id);
		 	         	//	System.out.println("uuuu  " + code + intrCode);
		        	        
		 	         		  
	         		  }catch(Exception ee){
			         		 customer.setCity(""); 
		         		 }
		         		  try{ 
			 	         		 String payerID =   getNode(rida,"payerID").getFirstChild().getNodeValue();
			 	         		  customer.setPayer(payerID);
			         		 }catch(Exception ee){
			         		 } 
			         		 try{
			         			Node attr = getNode(rida, "attributes");
			         			if(attr != null){
			         			
			         			//	System.out.println("ridu  " + attr.getChildNodes().getLength() + attr.toString());
				        	         for (int u = 0; u < attr.getChildNodes().getLength(); u++){
				        	        	 try{
				        	        	 	 Node item = attr.getChildNodes().item(u);
					        	      //   System.out.println("name" + item.getNodeName());
					        	        //	System.out.println("value " + getNode(item,"attributeName").getFirstChild().getNodeValue());
					        	        	
					        	            String glnkood  =  getNode(item,"attributeName").getFirstChild().getNodeValue();
					    		            if(glnkood.equals("clattr_gln")){
					    		            	glnkood  =  getNode(item,"attributeValue").getFirstChild().getNodeValue();
					    		            	 this.ilns.put(glnkood.trim(), id);
					    		            	// System.out.println("olemas " + glnkood + " " + id);
					    		            }
					        	        	
					        	        	
					        	        	
				        	        	 }catch(Exception e){
				        	        		//e.printStackTrace(); 
				        	        	 }
				        	        }
				        	         	 
				         			 
					 
				    		           
			         				
			         				
			         			}
			        	     
				 	         		
				         		   }catch(Exception ee){
					         		 
				         		   }
			         		 
	         		  try{
	         		   String salesman  =  getNode(rida,"customerManagerID").getFirstChild().getNodeValue();
    		            customer.addAgent(new Agent(salesman));
    		            
	         		   }catch(Exception ee){
		         		 
	         		   }
	         		 
	         		  try{
	  	         		String paym  =  getNode(rida,"paymentDays").getFirstChild().getNodeValue();
	         		            customer.setPaymTerm(paym);
	  	         		 }catch(Exception ee){
	  		        	 }
      		 
	         		     tm.put(customer.getID(), customer);
	                     
	         		 }
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	         
	    	  }    
	         }}
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	  //  System.out.println(custInte.toString());
      return tm;
}
  
  public TreeMap getProducts(Agent agent){

	    TreeMap tm = new TreeMap();
	    
	   codes = new TreeMap();
	   
	     try {
	    	  FileListFilter fil = new FileListFilter("tootederp","xml");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int i = 0; i < ll.length; i++) {
	    		
	    		 String name = ll[i];
	    		 //   System.out.println(name);
	 		  File f = new File (getDir() + name);
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	     	
	       Node root = getNode(doc, "document");      
	       //   System.out.println(root);
	      	Node read = getNode(root, "records");
	     // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	    
	      		if(read != null) {
	      	
	          for (int s = 0; s < read.getChildNodes().getLength(); s++){
	          	  Node rida = read.getChildNodes().item(s);
	          	  if(rida.getNodeName().equals("item")){
	         	  try{
	         		  
	         		boolean goes = true;  
	         		  
	         		String active  =  getNode(rida,"active").getFirstChild().getNodeValue();
	              	
              		if(active != null && !active.equals("1")){
              				goes = false;
              		}
	         		
	              			try{
	              				String sta  =  getNode(rida,"status").getFirstChild().getNodeValue();
	              				if(sta.equals("ACTIVE") || sta.equals("NO_LONGER_ORDERED")){
	              				}else{
	              					goes = false;
	              				}
	                        }catch(Exception e){
	              			}
	                        
	              			String code  = getNode(rida,"productID").getFirstChild().getNodeValue();
	                       
	                  
	         			  
	              		 String kood = null;
	              		 try{
	              			kood = getNode(rida,"code").getFirstChild().getNodeValue();
	              			codes.put(kood, code);
	              		 }catch(Exception e ){
	              			 kood = code;
	              		 }
	              		 
	              		Product prod = new Product(kood);
	              		
	              		
	              		 
	              		 try{
	              			
	              			String ean  =  getNode(rida,"code2").getFirstChild().getNodeValue();
	                  		if(ean != null){
	                  			prod.setEAN(ean);	
	                  			eans.put(ean.trim(), code);
	                  			
	                  		}	 
	              		 }catch(Exception r){
	              			 //r.printStackTrace();
	              		 }
	              		 
	              		 try{
		              			
		              			String ean  =  getNode(rida,"code5").getFirstChild().getNodeValue();
		                  		if(ean != null){
		                  			prod.setEAN(ean);	
		                  			eans.put(ean.trim(), code);
		                  			
		                  		}	 
		              		 }catch(Exception r){
		              			 //r.printStackTrace();
		              		 }
	              		 
	              		  if(goes){
	              		 
	              		 try{
	              			 String namec  =  getNode(rida,"name").getFirstChild().getNodeValue();
		              	    prod.setAlegrupp(code);
		              		 prod.setName( namec );
	              				 
	              		 }catch(Exception r){
	              			// r.printStackTrace();
	              		 }
	              		 
	              		
	              		
	              		String price  =  getNode(rida,"price").getFirstChild().getNodeValue();
	              		if(price != null){
	              			prod.setPrice(Double.parseDouble(price));	
	              		}
	              		
	              		String vat  =  getNode(rida,"vatrate").getFirstChild().getNodeValue();
	              		if(vat != null){
	              			prod.setVat(vat);	
	              		}
	              		
	              		
	              		String group  =  getNode(rida,"groupID").getFirstChild().getNodeValue();
	              		if(group != null){
	              			prod.setGroup(group);	
	              		}
	              		
	              		try{
	              			String unitName  =  getNode(rida,"unitName").getFirstChild().getNodeValue();
	                  		if(unitName != null){
	                  			prod.setUnit(unitName);	
	                  		}	
	              		}catch(Exception d){
	              			
	              		}
	              		
	              		
	              		
	              	
	              		try{
	              			String stock  =  getNode(getNode(getNode(rida,"warehouses"),"item"),"totalInStock").getFirstChild().getNodeValue();
	                  		if(stock != null){
	                  			prod.setStock(Double.parseDouble(stock));	
	                  		}	
	              		}catch(Exception e){
	              			
	              		}
	              		
	              		
	             		  
	                  
	         		 if(goes){
	         			  tm.put(prod.getID(), prod); 
	         		  }
	         		  }
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	          	  }     
	         }
	      	}
	    	 } 
	          
	    	 	 
	    
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	     
		 
	     return tm;
	   }


  
  private  Node getNode(Node node, String name){
	    for (int i = 0 ; i < node.getChildNodes().getLength() ; i++){
	      Node nd = node.getChildNodes().item(i);
	  //    System.out.println(nd.getNodeName());
	      if (nd.getNodeName().equals(name)){
	        return nd;
	      }
	    }
	    return null;
	  }
  
  
  private int counter = 0;
  private ImportPanel parent;
  private URL myurl = null;
  private String session = "";
  private String userstuff = "";
  private boolean isAmount = false;
  private String username = null;
  private String company = null;
  private String password = null;
  private String apiversion = null;
  private String returntype = null;
  private TreeMap products = null;
  private TreeMap codes = null;
  private TreeMap customers = null;
  private TreeMap ilns = new TreeMap();
  private TreeMap custInte = null;
  private TreeMap custInteReg = null;
  
  
  private boolean topeltkontroll = false;
  private TreeMap eans = new TreeMap();
  private StringBuffer trouble = new StringBuffer();
  

public String getTrouble() {
	// TODO Auto-generated method stub
	return trouble.toString();
}
  
}
