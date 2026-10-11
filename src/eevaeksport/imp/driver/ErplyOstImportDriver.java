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
import java.util.Calendar;
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

public class ErplyOstImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public ErplyOstImportDriver(ImportPanel parent) {
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
 	try {
		Converter.doTrustToCertificates();
	} catch (Exception e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
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
    
    warehouses = this.getWarehouses();
    try {
      Class.forName(databaseDriverName).newInstance();
    } catch (Exception e){
       parent.showError(e);
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
	        inputLine = enCode(inputLine);	// enCode(inputLine);	
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
	  
  private String enCode(String res ){

	  	res =res.replaceAll("�","11111xx");
	  	res =res.replaceAll("�","11112xx");
		res =res.replaceAll("�","11114xx");
		res =res.replaceAll("�","11114xx");
		res =res.replaceAll("�","11113xx");
		res =res.replaceAll("�","e");
		res =res.replaceAll("�"," "); 
		res = res.replaceAll("�"," ");
		res = res.replaceAll("�"," ");
		res = res.replaceAll("�"," ");
		res =res.replaceAll("�","'");
		res = res.replaceAll("�", "EUR");
		res = res.replaceAll("�", "Nael");
		res = res.replaceAll("�", " ");
		res =res.replaceAll("�","'");
		res =res.replaceAll("�"," ");
	
		res =res.replaceAll("�","11115xx");
		res =res.replaceAll("�","11116xx");
		res =res.replaceAll("�","11117xx");
		res =res.replaceAll("�","11118xx");
		
		res =res.replaceAll("�","11119xx");
		res =res.replaceAll("�","11120xx");
		res =res.replaceAll("�","11121xx");
		res =res.replaceAll("�","11122xx");
	  	return res;
	  }
	  
  
  

  
  public static String deCode(String res ){
	   
	   while(res.indexOf("11111xx") != -1){
	   	int aa = res.indexOf("11111xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   }
	   while(res.indexOf("11112xx") != -1){
	   	
	   	int aa = res.indexOf("11112xx");
	   //	System.out.println(aa + " " +res);
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11113xx") != -1){
	   	int aa = res.indexOf("11113xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11114xx") != -1){
	   	int aa = res.indexOf("11114xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11115xx") != -1){
	   	int aa = res.indexOf("11115xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11116xx") != -1){
	   	int aa = res.indexOf("11116xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11117xx") != -1){
	   	int aa = res.indexOf("11117xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11118xx") != -1){
	   	int aa = res.indexOf("11118xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 

	 
	return res;
}
  

 
	
	  
  
   
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  
  
  public String identifyCustomer(String id,String regnr)  {
	// System.out.println("regnos " + regnr );
		if(this.ilns.get(id) != null){
			 System.out.println("klientILN olemas " + id + " " +  (String)ilns.get(id));
			return (String)ilns.get(id);
		}  
	if(this.custInte.get(id) != null){
		 System.out.println("klientinte olemas " + id + " " + (String)custInte.get(id));
		return (String)custInte.get(id);
	}
	if(this.customers.get(id) != null){
		 System.out.println("klient olemas " + id);
		return id;
	}
	if(this.regnos.get(regnr) != null){
		 System.out.println("klient olemas " + regnr + " " +  id + " " + regnos.get(regnr) );
		return regnos.get(regnr) + "";
	}
	
	

    return null;
  }


  
  public String identifyProduct(String ean, String prodID) throws SQLException{ 
	  System.out.println("sisse  " + ean + " " + prodID );
	  if(prodID != null && this.products.get(prodID) != null){
		 System.out.println("toode olemas " + prodID);
		  Product pr = (Product)products.get(prodID);
		  return pr.getAlegrupp();
	  }
  System.out.println("suurus" + eans.size() + " " + eans.get(ean));
	  if(ean != null && eans != null && eans.get(ean.trim()) != null){
	    System.out.println("ean olemas " + ean);
		  return (String)eans.get(ean.trim());
	  } 
	  
	  if(prodID != null && eans != null && eans.get(prodID) != null){
			  System.out.println("ean olemas " + ean);
			  return (String)eans.get(prodID);
		  } 
	  while(ean != null && ean.startsWith("0")){
			ean = ean.substring(1);
			  if(ean != null && eans != null && eans.get(ean) != null){
				  System.out.println("ean olemas nulli koristajaga " + ean);
				  return (String)eans.get(ean);
			  }  
	  }
	  
      return null;
  }
  
  

  public void makeOrder(Order order) throws SQLException{
         counter = 0;
       
         userstuff = "&clientCode=" + company +"&version=" + apiversion + "&responseType=" + returntype + "&sessionKey=" + session;         
		//System.exit(0);
         makeOutput(order);   
  }
  
  private String getTarne(Order order){
	    Calendar cal = Calendar.getInstance();
	    Calendar dat = order.getShipCal();
	  //  date	eg. 2010-01-29
	    String result = null ;
	    if (dat != null){//dat != null
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
	 
	  trouble = new StringBuffer();
	
	  
	     String aiai = parent.getProps().getProperty("testing");
	     if(aiai != null && aiai.equals("true")){
	    	 
	     }else{
	    	 try {
	 	 		String orderdata = "";
	 	 		boolean attr = false;
	 	 		if(order.getReference() != null && order.getReference().trim().length() != 0){
	 	 			attr = true;
	 	 		}

	 	 	     StringBuffer inputLineBuf = new StringBuffer();
	 	     	 Vector lines = order.getOrderLines();
	 		      for (int i = 0; i < lines.size(); i++) {
	 		    	  OrderLine line = (OrderLine) lines.elementAt(i);
	 		    	 
	 		    	  if( line.getToode() != null &&  line.getToode().trim().length() != 0){
	 		    		  
	 		    		   double kordaja = 1;
	 		    		   try{
	 		    			//  System.out.println(line.getToode() + " " + jagajad.toString());
	 		    			  double pr = Double.parseDouble((this.jagajad.get(line.getToode())+"").replace(',', '.')) ;
	 		    			//  System.out.println(line.getToode() + " bbb  " + pr);
	 		    			  kordaja = pr;
	 		    		   }catch(Exception e){
	 		    			   //e.printStackTrace();
	 		    		   }
	 		    		   double price = line.getPrice()/ kordaja;
	 		    	       
	 		    	  String attrib = "";
	 		    	  if(attr){
	 		    		  attrib = "&attributeName" +  (counter) + "=order_referenceNumber&attributeType"+(counter) + 
	 		    		  "=text&attributeValue" + (counter) + "=" + order.getReference();  
	 		    		  
	 		    	  }
	 		    	  
	 		    	  orderdata=orderdata+ "&productID" + (counter) + "=" + line.getToode() + 
	 		    	  "&amount" + (counter) + "=" +  (line.getKogus()*kordaja) + attrib ;
	 		    	 
	 		    		  orderdata=orderdata+ "&price" + (counter) + "=" + price;
	 		    		  orderdata=orderdata+ "&vatrateID" + (counter) + "=0" ;
	 		    		  
	 		    		 
	 		    
	 		    		  counter++;
	 		    	  }
	 	      }
	 	 
	 		      String comment = "";
	 		        
	 		        if(order.getInfo() != null){
	 		        	comment = order.getInfo().replaceAll("\n"," ");
	 		        	comment = order.getInfo().replaceAll(","," ");
	 		        	comment = "internalNotes=" + comment.trim() + "&";
	 		        }
	 	 		String date = getTarne(order);
	 	 		
	 		       
	 		        
	 	 	try {
	 	 	  try {
	 	 		  String payerID = "";
	 	 		  if(customers.get(order.getCompany()) != null){
	 	 			  Customer cust = (Customer)customers.get(order.getCompany()) ;
	 	 			  if(cust.getPayer() != null && cust.getPayer().trim().length() != 0){
	 	 				 payerID = "&payerID=" + cust.getPayer();
	 	 			  }
	 	 			
	 	 		  }
	 	 		  String confirm = "";
	 	 		  if(parent.getProps().getProperty("erply.confirm") != null){
	 	 			 confirm = "confirmInvoice=0&"; 
	 	 		  }
	 	 		
	 	 		
	 	 		  
	 	 		  String dat = "request=savePurchaseDocument&type=PRCINVOICE&currencyCode=EUR&no=" 
	 	 			  + order.getDokNr() + "&warehouseID=" + warehouses.get(Converter.cleanLitLatEst(order.getShipMode())) + "&" + confirm + "supplierID=" + order.getCompany()+
	 				 "&date=" + date + "&paymentDays=" + order.getPaymTerm()+ "&" + comment  + orderdata + userstuff;
	 	 		  System.out.println( Converter.cleanLitLatEst(order.getShipMode()) + " " +dat);
	 	 			
	 	 		
	 	 			inputLineBuf = postData(dat);
	 	 		
	 	 			
	 	 		} catch (Exception e1) {
	 	 			e1.printStackTrace();
	 	 		}
	 	 	
	 	 	 
	 	 	} catch (Exception e) {
	 	 	} 
	 	      
	 	      setNumAddedLines(counter); 
	 	      
	 	    String doknr = order.getDokNr();
	 	      String fn = doknr;
	 	      if(doknr.trim().length()== 0){
	 	    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
	 	      }	      
	 	      String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + fn  +  ".xml";
	 	     //   System.out.println(fileName);
	 	        FileOutputStream fos = new FileOutputStream(fileName);
	 	        
	 	        fos.write(inputLineBuf.toString().getBytes());
	 	        fos.close();
	            System.out.println(inputLineBuf);
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
    return "ErplyOst";
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
	    
	   try {
		   
		   FileListFilter fil = new FileListFilter("kliendiderp","xml");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int i = 0; i < ll.length; i++) {
	    		
	    		
	    		 String name = ll[i];
	    		
	    		 System.out.println(name);
	 		  File f = new File (getDir() + name);
		   
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "document");      
	        
	     	Node read = getNode(root, "records");
	     	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	         for (int s = 0; s < read.getChildNodes().getLength(); s++){
	         	  Node rida = read.getChildNodes().item(s);
	         	  try{
	         		 if(rida.getNodeName().equals("item")){
	         			
	         		  String id = deCode( getNode(rida,"supplierID").getFirstChild().getNodeValue());
	         		  Customer customer = new Customer(id);
                 
	         		  try{
	         		  String namec =   getNode(rida,"fullName").getFirstChild().getNodeValue();
	         		  customer.setName(deCode(namec));
                      }catch(Exception ee){
	                     customer.setName(id); 
                      }
                      
                      try{
    	         		  String namec =   getNode(rida,"code").getFirstChild().getNodeValue();
    	         		  customer.setRegnr(deCode(namec));
    	         		  regnos.put(namec, id);
                          }catch(Exception ee){
    	                     customer.setName(id); 
                          }
	         		  try{ 
	 	         		 String aadr =   getNode(rida,"address").getFirstChild().getNodeValue();
	 	         		  customer.setAddress(deCode(aadr));
	         		 }catch(Exception ee){
		         		 customer.setAddress(""); 
	         		 }
	         		 
	         		 try{ 
	 	         		 String gln =   getNode(rida,"GLN").getFirstChild().getNodeValue();
	 	         		 this.ilns.put(gln.trim(), id);  
	         		 }catch(Exception ee){
		         		 
	         		 }
	         		  try{ 
		 	         		 String intrCode =   deCode(getNode(rida,"integrationCode").getFirstChild().getNodeValue());
		 	         		  customer.setCity((intrCode));
		 	         		  this.custInte.put(intrCode, id);
		 	         		  if(intrCode.length() > 6){
		 	         			 this.custInte.put(intrCode.substring(0,6), id);
		 	         		  }
		         		 }catch(Exception ee){
			         		 customer.setCity(""); 
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
			         		 
	         	
	         		     tm.put(customer.getID(), customer);
	                     
	         		 }
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	         
	    	  }    
	         }
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	  //  System.out.println(custInte.toString());
      return tm;
}
  
  public TreeMap getProducts(Agent agent){

	    TreeMap tm = new TreeMap();
	    
	   jagajad = new TreeMap();
	   
	     try {
	    	  FileListFilter fil = new FileListFilter("tootederp","xml");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int i = 0; i < ll.length; i++) {
	    		
	    		 String name = ll[i];
	    		   // System.out.println(name);
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
	          for (int s = 0; s < read.getChildNodes().getLength(); s++){
	          	  Node rida = read.getChildNodes().item(s);
	          	  if(rida.getNodeName().equals("item")){
	         	  try{
	         		  
	         		  
	         		 
	              		 String code  = getNode(rida,"productID").getFirstChild().getNodeValue();
	              		 
	              		 String kood = null;
	              		 try{
	              			kood = getNode(rida,"code").getFirstChild().getNodeValue();
	              		 }catch(Exception e ){
	              			 kood = code;
	              		 }
	              		 
	              		Product prod = new Product(kood);
	              		
	              		String active  =  getNode(rida,"active").getFirstChild().getNodeValue();
	              		boolean goes = true;
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
	              		 
	              		 try{
	              			 String namec  =  getNode(rida,"name").getFirstChild().getNodeValue();
		              	    prod.setAlegrupp(code);
		              		 prod.setName( namec );
	              			String ean  =  getNode(rida,"code2").getFirstChild().getNodeValue();
	                  		if(ean != null){
	                  			prod.setEAN(ean);	
	                  			if(goes){
	                  				eans.put(ean.trim(), code);	
	                  			}
	                  			
	                  		}	 
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
	              		
	              		
	              		 try{
			         			Node attr = getNode(rida, "attributes");
			         			if(attr != null){
			         			
			         				//System.out.println("ridu  " + attr.getChildNodes().getLength() + attr.toString());
				        	         for (int u = 0; u < attr.getChildNodes().getLength(); u++){
				        	        	 try{
				        	        	 	 Node item = attr.getChildNodes().item(u);
					        	        // System.out.println("name" + item.getNodeName());
					        	        //	System.out.println("value " + getNode(item,"attributeName").getFirstChild().getNodeValue());
					        	        	
					        	            String jagaja  =  getNode(item,"attributeName").getFirstChild().getNodeValue();
					    		            if(jagaja.equals("order_qty")){
					    		            	jagaja  =  getNode(item,"attributeValue").getFirstChild().getNodeValue();
					    		           
					    		            jagajad.put(code,jagaja.trim());
					    		            	// System.out.println("olemas " + prod.getID() + " " + prod.getInUnit());
					    		            }
					        	        	
					        	        	
					        	        	
				        	        	 }catch(Exception e){
				        	        		//e.printStackTrace(); 
				        	        	 }
				        	        }
				     			}
			        	     
				 	         		
				         		   }catch(Exception ee){
					         		 ee.printStackTrace();
				         		   }
			         		   
	              		
	              		
	              		
	              		
	                  
	         		 if(goes){
	         			  tm.put(prod.getID(), prod); 
	         		  }
	                      
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	          	  }     
	         }
	    
	          
	          
	    	 }	 
	    
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	     
		 
	     return tm;
	   }

  
  

  public TreeMap getWarehouses(){

	    TreeMap tm = new TreeMap();
	    
	   
	   
	     try {
	    	  FileListFilter fil = new FileListFilter("laoderp","xml");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int i = 0; i < ll.length; i++) {
	    		
	    		 String name = ll[i];
	    		   // System.out.println(name);
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
	          for (int s = 0; s < read.getChildNodes().getLength(); s++){
	          	  Node rida = read.getChildNodes().item(s);
	          	  if(rida.getNodeName().equals("item")){
	         	  try{
	         		  
	         		  
	         		 
	              		 String code  = getNode(rida,"warehouseID").getFirstChild().getNodeValue();
	              		 
	              		 String kood = null;
	              		 try{
	              			kood = Converter.deCode(getNode(rida,"code").getFirstChild().getNodeValue());
	              		 }catch(Exception e ){
	              			
	              		 }
	              		 
	              	
	              		
	              		
	              	 if(kood != null){
	         			  tm.put(Converter.cleanLitLatEst(kood), code); 
	         		  }
	                      
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	          	  }     
	         }
	    
	          
	          
	    	 }	 
	    
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	     
		 System.out.println("Laod " + tm.toString());
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
  private TreeMap jagajad = null;
  private TreeMap customers = null;
  private TreeMap warehouses = null;
  private TreeMap ilns = new TreeMap();
  private TreeMap custInte = null;
  private TreeMap regnos = new TreeMap();
  private TreeMap eans = new TreeMap();
  private StringBuffer trouble = new StringBuffer();
  

public String getTrouble() {
	// TODO Auto-generated method stub
	return trouble.toString();
}
  
}
