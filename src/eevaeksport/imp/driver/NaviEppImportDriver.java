
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;

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

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class NaviEppImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public NaviEppImportDriver(ImportPanel parent) {
    this.parent = parent;
    //prods = getProducts();
    customers = getCustomers();
    PropertyConfigurator.configure("log4j.properties");
    nf.setGroupingUsed(false);
  }

  public String identifyCustomer(String id,String regnr) throws SQLException {
    return id;
  }

  public String identifyCustomerADDR(String id) throws SQLException {  	
    return id;
  }
  
  public String identifyCustomerAGENT(String id) throws SQLException {
 	return id;
 }
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	
    return null;
  }

  private String getDir(){
	     String dir = parent.getProps().getProperty("readin");
	     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	     return dir;
	   }
  
  
  public TreeMap getProducts(){
	     TreeMap tm = new TreeMap();
	     try { 	 
	    	 
	       FileReader fr = new FileReader(getDir() + DATA_FILE);
	       BufferedReader reader = new BufferedReader(fr);
	     String line = null;

	     while(!(line = reader.readLine()).equals("[Get Products]")){
	     }
	     line=reader.readLine();
	     while(((line = reader.readLine()).trim().length()) > 0 ){
	        line = line.replaceAll("\",\"","| ");

	       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
	       if (st.hasMoreTokens()){
	         String id = clear(st.nextToken());
	         String weight ="";
	         Product prod = new Product(id);
	         if (st.hasMoreTokens()){
	         	String name = clear(st.nextToken());
	         	name = name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
	           if (st.hasMoreTokens()){
	           st.nextToken().replace(',','.');
	            
	                     if (st.hasMoreTokens()){
	                       prod.setUnit(clear(st.nextToken()));
	                       // System.out.println("ladu"+prod.getStock());
	                       if (st.hasMoreTokens()){
	                         String ean = clear(st.nextToken());
	                         if (ean.trim().length()>0){
	                           prod.setEAN(ean);
	                         }
	                         if (st.hasMoreTokens()){
	                             clear(st.nextToken());
	                      /*   int xx = Integer.parseInt(xxx);
	                         if(xx == 1){
	                         	prod.setStatusTop("1");
	                         }else if (xx == 2){
	                           prod.setMustBe("1");
	                         }
	                         else if (xx == 3) {
	                           prod.setMustBe("1");
	                        	prod.setStatusTop("1");
	                         }
	                         else if (xx == 4) {
	                            prod.setStatusNew("1");
	                          }else if (xx == 5) {
	                            prod.setStatusSellout("1");
	                         }*/
	                         if (st.hasMoreTokens()){
	                         	String discou = clear(st.nextToken());
	                         	if(discou != null && discou.trim().length()>0){
	                         		//System.out.println(line);
	                            prod.setMaxDiscount(Double.parseDouble(discou));
	                         	}
	                         String cases = clear(st.nextToken());
	                            if (st.hasMoreTokens()){
	                             	cases = clear(st.nextToken());
	                             	if(cases != null && cases.trim().length()>0){
	                             		double inCases =Double.parseDouble(cases);
	                             		if(inCases != 0){
	                                        prod.setInCases(inCases);	
	                             		}

	                                if (st.hasMoreTokens()){
	                                	 weight = clear(st.nextToken());
	                                         if(weight != null && weight.trim().length()>0){
	                                           weight = weight.trim();
	                                   }
	                                }
	                             }
	                        }
	                     }
	                   }
	                 }
	               }
	             }
	           
	           prod.setName(name);
	           
	         }
	      
	         
	             tm.put(prod.getID(), prod);	 
	         
	         
	        }

	     }
	     } catch (IOException e){
	       e.printStackTrace();
	     }
	     return tm;
	   }
  
  public void makeOrder(Order order) throws SQLException{
 
    makeOutput(order);
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
  private Node getNode(Node rt, String s){
	    return GenericDOM.getNode(rt, s);
	  }
	  
  
  public TreeMap getCustomers(){
	     TreeMap tm = new TreeMap();
	     System.out.println("Kliendid alustan");
	    
	   
	   try {
	 		  File f = new File (getDir() + "navikliendid.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     
	 		
	 		
	 		FileInputStream in = getCleanInputStream(f);
	     	String handler = null;

	 		  Document doc = db.parse(in,handler);
	          Node root = getNode(doc, "CustomerList");      
	          System.out.println(root);
//	      	System.out.println("ridu  " + root.getChildNodes().getLength() + root.toString());
	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
	          	  Node rida = root.getChildNodes().item(i);
	         
	         	  try{
	         		  String id =  getNode(rida,"CustomerNo").getFirstChild().getNodeValue();
	         		  
	         		  
	              
	                    
	         		  
	         		  Customer customer = new Customer(id);

	         		  String name =  getNode(rida,"Name").getFirstChild().getNodeValue();

	         		   try{
	  	         		 String salesman  =  getNode(rida,"SalesPersonCode").getFirstChild().getNodeValue();
	  	         		      customer.addAgent(new Agent(deCode(salesman)));
	  	         		    
	  	         	   }catch(Exception e){
	  	         			  
	  	         		  }
	  	         	   
	  	             try{
	   	         		 int ship =  getNode(rida,"ShippingAddressList").getChildNodes().getLength();
	   	         		   if(ship >0){
	   	         			customer.setSalesBlock(true); 
	   	         		   }
	                            }catch(Exception e){
	                           	
	   	         		  }
	  	         	   
	         		  
	         		 try{
	         		  String name2 =  getNode(rida,"Name2").getFirstChild().getNodeValue();
	         		  name = name + " "+deCode(name2) ;
                    }catch(Exception e){
	         			  
	         		  }
                   
	         	
	         		 customer.setName(deCode(name + " " + id));
	         		 
	         		 String aadr =  getNode(rida,"Address").getFirstChild().getNodeValue();
	         		    
	         		
		         		   try{
			         			 String indeks =  getNode(rida,"PostCode").getFirstChild().getNodeValue();        	
				         		 aadr = aadr + "," + indeks;	  
			         		  }catch(Exception e){
			         		  }
			         		   try{
				         			 String linn =  getNode(rida,"City").getFirstChild().getNodeValue();        	
					         		 aadr = aadr + "," + linn;	  
				         		  }catch(Exception e){
				         		  }

			                      customer.setAddress(deCode(aadr));
				    
			                      
			                      

				         		     try{
					         			 String phone =  getNode(rida,"Phone").getFirstChild().getNodeValue();        	
						         		  customer.setPersonalData(deCode(phone));  	  
					         		  }catch(Exception e){
					         		  }
					         		  
	         		 try{
	         		 String paym =  getNode(rida,"PaymentTermCode").getFirstChild().getNodeValue();
	         		  customer.setPaymTerm(deCode(paym));
                      }catch(Exception e){
	         			  
	         		  }
                    	
                      
			         	   try{
				         		 String blocked  =  getNode(rida,"Blocked").getFirstChild().getNodeValue();
				         		 if(blocked.equals("1")){
				         		      customer.setSalesBlock(true); 
				         		 }
				         	   }catch(Exception e){
				         			  
				         		  }
	         	   
	         		    // customer.setConfirmEmail("terje.sepp@charlot.ee");
	         		      
	         		     tm.put(customer.getID(), customer);
	                      
	         	  }catch(Exception e){
	         		  
	         	  }
	              
	         }
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	   
	    
	 	
	 	
	 
	    System.out.println("Kliendid tehtud");
	    
  return tm;
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

	   
	   while(res.indexOf("11119xx") != -1){
		   	int aa = res.indexOf("11119xx");
		   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
		   } 

	   while(res.indexOf("11120xx") != -1){
		   	int aa = res.indexOf("11120xx");
		   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
		   } 

	   

	   while(res.indexOf("11121xx") != -1){
		   	int aa = res.indexOf("11121xx");
		   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
		   } 	   

	   while(res.indexOf("11122xx") != -1){
		   	int aa = res.indexOf("11122xx");
		   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
		   } 
	
	return res;
}

  private String clear (String ss){
	  //	System.out.println(ss);
	    String ret =   ss.replace(',','.');
	    ret =  ret.replace(',','.');
	    ret =  ret.replace(',','.');
	    ret =  ret.replace(',','.');
	    ret =  ret.replace('"',' ').trim();
	    ret =  ret.replace('"',' ').trim();
	    ret =  ret.replace('"',' ').trim();


	    ret = ret.replaceAll("�","11111xx");

	    ret = ret.replaceAll("�","11112xx");

	    ret = ret.replaceAll("�","11113xx");


	    ret = ret.replaceAll("�","11114xx");

	    ret = ret.replaceAll("�","11115xx");

	    ret = ret.replaceAll("�","11116xx");

	    ret = ret.replaceAll("�","11117xx");

	    ret = ret.replaceAll("�","11118xx");
	    ret = ret.replaceAll("�","11119xx");
	    //�mon�
	    ret = ret.replaceAll("�","11130xx");
	    ret = ret.replaceAll("�","11120xx");
	    ret = ret.replaceAll("�","11121xx");
	    ret = ret.replaceAll("�","11122xx");
	    ret = ret.replaceAll("�","11123xx");
	    ret = ret.replaceAll("�","11124xx");
	//O?
	    ret = ret.replaceAll("�","11224xx");
	    ret = ret.replaceAll("�","11225xx");
	    ret = ret.replaceAll("�","11226xx");
	    ret = ret.replaceAll("�","11227xx");
	    ret = ret.replaceAll("�","11228xx");
	    ret = ret.replaceAll("�","11229xx");
	//�
	    ret = ret.replaceAll("11224xx","�");
	    ret = ret.replaceAll("11225xx","�");
	    ret = ret.replaceAll("11226xx","�");
	    ret = ret.replaceAll("11227xx","�");
	    ret = ret.replaceAll("11228xx","�");
	    ret = ret.replaceAll("11229xx","�");
	    
	    ret = ret.replaceAll("11130xx","�");
	    ret = ret.replaceAll("11119xx","�");
	    ret = ret.replaceAll("11120xx","�");
	    ret = ret.replaceAll("11121xx","�");
	    ret = ret.replaceAll("11122xx","�");
	    ret = ret.replaceAll("11123xx","�");
	    ret = ret.replaceAll("11124xx","�");
	    ret = ret.replaceAll("11118xx","�");
	    ret = ret.replaceAll("11119xx","�");
	    ret = ret.replaceAll("11117xx","�");
	    ret = ret.replaceAll("11116xx","�");
	    ret = ret.replaceAll("11115xx","�");
	    ret = ret.replaceAll("11114xx","�");
	    ret = ret.replaceAll("11113xx","�");
	    ret = ret.replaceAll("11111xx","�");
	    ret = ret.replaceAll("11112xx","�");
	    if(ret == null || ret.trim().length()==0){
	    return " ";
	    }else {
	    return ret;
	    }
	  }

  private void makeOutput(Order order){
	    
	  String company = order.getCompany();
	 	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    
	      try {
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.newDocument();
	        Element root = doc.createElement("mmttellimus");
	        doc.appendChild(root);
	        
	        
	        Node nr = doc.createElement("nr");
	        Calendar calv = Calendar.getInstance();//order.getDokNr()
	        Text nrVal = doc.createTextNode((calv.getTimeInMillis() + "").substring(5));
	        nr.appendChild(nrVal);
	        root.appendChild(nr);

	        Node kuupaev = doc.createElement("kuupaev");
	        Calendar cal = Calendar.getInstance();
	        String month = (cal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	        String day = cal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;

	        String dateString = cal.get(Calendar.YEAR) +""+ month +""+ day;
	        Text kuupaevVal = doc.createTextNode(dateString);
	        kuupaev.appendChild(kuupaevVal);
	        root.appendChild(kuupaev);

	        /*
	        Node curr = doc.createElement("OrderCurrency");
	        Text currVal = doc.createTextNode("LTL");
	        curr.appendChild(currVal);
	        header.appendChild(curr);*/
	        
	        
	        Node mkuupaev = doc.createElement("tarne-kuupaev");
	        Calendar mcal = Calendar.getInstance();
	        try{
	        	 mcal.setTime(order.getShipCal().getTime());
	        }catch (Exception e){
	        	
	        }
	       

	         month = (mcal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	         day = mcal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;

	         String mdateString = cal.get(Calendar.YEAR)+"-" + month +"-"+ day;
	        Text mkuupaevVal = doc.createTextNode(mdateString);
	        mkuupaev.appendChild(mkuupaevVal);
	        root.appendChild(mkuupaev);
	        
	        
	        
	        
	     
	        
	        String agent = order.getAgent();
	        System.out.println(customers.get(order.getCompany())  + " fff " + order.getCompany() );
	        if(customers.get(order.getCompany()) != null){
	        Customer ccc = (Customer)customers.get(order.getCompany());
	        
	        Node delivery = doc.createElement("tellija-kood");
	      
	        if(ccc.isSalesBlock()){
	        	company = company + ":1";
	        }
	        Text deliveryVal = doc.createTextNode(company);
	        delivery.appendChild(deliveryVal);
	        root.appendChild(delivery);
	        
	        
	        
	        if (ccc.getAddress() != null && ccc.getAddress().trim().length() >0){
	        	Node deliveryadd = doc.createElement("saaja-aadress");
	  	        Text deliveryaddVal = doc.createTextNode(ccc.getAddress());
	  	        deliveryadd.appendChild(deliveryaddVal);
	  	        root.appendChild(deliveryadd);
	  	        
	  	        Node shipadd = doc.createElement("tarne-aadress");
	  	        Text shipaddVal = doc.createTextNode(ccc.getAddress());
	  	        shipadd.appendChild(shipaddVal);
	  	        root.appendChild(shipadd);
	  	    }	
	        if(agent == null || agent.trim().length()==0){
	     	   agent =  (String) ccc.getAgents().keySet().iterator().next(); 
	        }
	        }else if(order.getStatus() == 0 ){
	        	 
	  	        
	  	        Node delivery = doc.createElement("tellija-kood");
	  	      
	  	        	company = order.getCompany() + ":1";
	  	        
	  	        Text deliveryVal = doc.createTextNode(company);
	  	        delivery.appendChild(deliveryVal);
	  	        root.appendChild(delivery);
	  	        
	        }

  
	        if(agent != null){
	        	   Node koostaja = doc.createElement("koostaja");
	   	        root.appendChild(koostaja);
	   	        Node agentid = doc.createElement("isiku-id");
	   	        Text agetnVal = doc.createTextNode(agent);
	   	        agentid.appendChild(agetnVal);
	     	        koostaja.appendChild(agentid);
	   	        
	   	        	
	        }
	 
	        String comment = " ";
	        
	        if(order.getInfo() != null){
	        	comment = order.getInfo().replaceAll("\n"," ");
	        	comment = order.getInfo().replaceAll(","," ");
	        }
	    if(comment.length()>70){
	    //	comment = comment.substring(0,70);
	    }
	        if(comment.trim().length()>0){
	            Node markused = doc.createElement("markused");
		        Text markusedVal = doc.createTextNode(comment);
		        markused.appendChild(markusedVal);
		        root.appendChild(markused);
		    }
	        

	        
	        //RIDADE LISAMINE
	        Node read = doc.createElement("read");

	        int coun = 0;
	        Iterator iter = order.getOrderLines().iterator();
	        while (iter.hasNext()){
	          OrderLine line = (OrderLine)iter.next();
	          String toode  = line.getToode();
	 	     if(toode!= null){
	 	    	 toode = toode.trim();
	 	     }else{
	 	    	 toode = "";
	 	     }
	          if(line.getKogus()!= 0 && !toode.equals("")){
	          	
	        	  
	        	  coun++;
	          	addLine(line, read, doc, coun , order);
	          }
	          }

	        root.appendChild(read);

	        long uni = Calendar.getInstance().getTimeInMillis();
	 	
	      String fileName = parent.getProps().getProperty("conv.dir") + "t-mj-" + order.getAgent()+ "-"+ order.getCompany() + "-" + uni +  ".xml";
	      System.out.println(fileName);
	      OutputFormat of = new OutputFormat();
	        of.setIndenting(true);
	        of.setLineSeparator(System.getProperty("line.separator"));
	        //<?xml version="1.0" encoding="windows-1257"?>  <?xml version="1.0" encoding="windows-1257" ?>
	        of.setEncoding("windows-1257");
	        XMLSerializer ser = new XMLSerializer(of);
	        FileOutputStream fos = new FileOutputStream(fileName);
	        PrintWriter out = new PrintWriter(fos);
	        ser.setOutputCharStream(out);
	        ser.serialize(doc);
	        out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
  
 public String getImportFile(){
	    return parent.getProps().getProperty("imp.dir");
	  }
 
  public String getDriverVersion(){
    return "NaviEppImport";
  }

  private void addLine(OrderLine line, Node read, Document doc,int counter ,Order order) throws SQLException{

	  Node rida = doc.createElement("rida");
	    read.appendChild(rida);


	/*    Node linenum = doc.createElement("LineNumber");
	    Text linnum = doc.createTextNode(counter+"");
	    linenum.appendChild(linnum);
	    ridaitem.appendChild(linenum);
*/

	    Node toote_ean = doc.createElement("ean-kood");
	    Text toote_eanVal = doc.createTextNode(line.getEAN());
	    toote_ean.appendChild(toote_eanVal);
	    rida.appendChild(toote_ean);

	     
	     String toode  = line.getToode();
	     if(toode!= null){
	    	 toode = toode.trim();
	     }else{
	    	 toode = "";
	     }

	    Node toote_kood = doc.createElement("toote-kood");
	    Text toote_koodVal = doc.createTextNode(toode);
	    toote_kood.appendChild(toote_koodVal);
	    rida.appendChild(toote_kood);

   String nimetu = escape(line.getTooteNimi().replace('`',' ').replace('�',' '));
   if(prods != null && nimetu != null && nimetu.trim().length() == 0 && prods.get(toode) != null){
	   nimetu = "";// ((Product)prods.get(toode)).getName();
   }
	    Text nimetusVal = doc.createTextNode(nimetu);
	    Node nimetus = doc.createElement("nimetus");
	    nimetus.appendChild(nimetusVal);
	    rida.appendChild(nimetus);

	    
	    double kogus = line.getKogus();
	    Node koGus = doc.createElement("kogus");
	    Text kogusVal = doc.createTextNode(getNF().format(kogus));
	    koGus.appendChild(kogusVal);
	    rida.appendChild(koGus);


	/*    Node yhik = doc.createElement("yhik");
	    Text yhikVal = doc.createTextNode(line.getUnit());
	    yhik.appendChild(yhikVal);
	    rida.appendChild(yhik);
	  */  
	    Node price = doc.createElement("yhiku-hind");
	    Text priceVal = doc.createTextNode(getNF().format(0));
	    price.appendChild(priceVal);
	    rida.appendChild(price);

	    Node km = doc.createElement("km");
	    Text kmVal = doc.createTextNode(getNF().format(20));
	    km.appendChild(kmVal);
	    rida.appendChild(km);
	     
	     addNumLine(); 
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
		return nf;
	}
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private ImportPanel parent;
  public static final String DATA_FILE = "data.txt";
  private TreeMap prods = null;
  private TreeMap customers = null;
  public static final String SEPARATOR = "|";

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
