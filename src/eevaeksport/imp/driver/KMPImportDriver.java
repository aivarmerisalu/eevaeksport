
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;
import java.io.BufferedReader;
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

public class KMPImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public KMPImportDriver(ImportPanel parent) {
    this.parent = parent;
   // prods = getProducts();
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
	        
	         Product prod = new Product(id);
	         if (st.hasMoreTokens()){
	         	String name = clear(st.nextToken());
	         	name = name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
	           if (st.hasMoreTokens()){
	           st.nextToken().replace(',','.');
	            
	                     if (st.hasMoreTokens()){
	                       prod.setUnit(clear(st.nextToken()));
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
	    
	 	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    
	      try {
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.newDocument();
	        Element root = doc.createElement("Document-Order");
	        doc.appendChild(root);
	        
	        Node header = doc.createElement("Order-Header");
	        
	        Node nr = doc.createElement("OrderNumber");
	        Calendar calv = Calendar.getInstance();//order.getDokNr()
	        Text nrVal = doc.createTextNode((calv.getTimeInMillis() + "").substring(4));
	        nr.appendChild(nrVal);
	        header.appendChild(nr);

	        Node kuupaev = doc.createElement("OrderDate");
	        Calendar cal = Calendar.getInstance();
	        String month = (cal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	        String day = cal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;

	        String dateString = cal.get(Calendar.YEAR) +"-"+ month +"-"+ day;
	        Text kuupaevVal = doc.createTextNode(dateString);
	        kuupaev.appendChild(kuupaevVal);
	        header.appendChild(kuupaev);

	        
	        Node curr = doc.createElement("OrderCurrency");
	        Text currVal = doc.createTextNode("LTL");
	        curr.appendChild(currVal);
	        header.appendChild(curr);
	        
	        
	        Node mkuupaev = doc.createElement("ExpectedDeliveryDate");
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
	        header.appendChild(mkuupaev);
	        
	        
	        Node time = doc.createElement("ExpectedDeliveryTime");
	        Text timeVal = doc.createTextNode("00:00");
	        time.appendChild(timeVal);
	        header.appendChild(time);
	        
	        Node type = doc.createElement("DocumentFunctionCode");
	        Text typeVal = doc.createTextNode("O");
	        type.appendChild(typeVal);
	        header.appendChild(type);
	        
	        root.appendChild(header);
	        /*
- <Order-Parties>
- <Buyer>
  <ILN>9987427830001</ILN> 
  <Name>Gulbel� UAB</Name> 
- <OperatorDetails>
  <Name>DINETAWEB</Name> 
  </OperatorDetails>
  </Buyer>
*/
	        
	        Node delivery = doc.createElement("Order-Parties");
	        root.appendChild(delivery);
	        
	        Node delpoint = doc.createElement("DeliveryPoint");
	        delivery.appendChild(delpoint);
	        
	        String company = order.getCompany();
	        if(company.indexOf("#") == -1 ){
	        	company = company + "#" + company;
	        }
	        
	        
	        Node cust = doc.createElement("ILN");
	        Text custVal = doc.createTextNode(company);
	        cust.appendChild(custVal);
	        delpoint.appendChild(cust);

	        
	        Node buyer = doc.createElement("Buyer");
	        delivery.appendChild(buyer);
	        
	        
	        Node buyerag = doc.createElement("OperatorDetails");
	        Node buyeragname = doc.createElement("Name");
	        buyerag.appendChild(buyeragname);
	        
	        
	        Text buyeragVal = doc.createTextNode(order.getAgent());
	        buyeragname.appendChild(buyeragVal);
	        buyer.appendChild(buyerag);
	       


	        //RIDADE LISAMINE
	        Node read = doc.createElement("Order-Lines");

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
	 	
	      String fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr() + uni +  ".xml";
	      System.out.println(fileName);
	      OutputFormat of = new OutputFormat();
	        of.setIndenting(true);
	        of.setLineSeparator(System.getProperty("line.separator"));
	        //<?xml version="1.0" encoding="windows-1257"?>
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
    return "KMPImport";
  }

  private void addLine(OrderLine line, Node read, Document doc,int counter ,Order order) throws SQLException{

	  Node rida = doc.createElement("Line");
	    read.appendChild(rida);

	    Node ridaitem = doc.createElement("Line-Item");
        rida.appendChild(ridaitem);

	    Node linenum = doc.createElement("LineNumber");
	    Text linnum = doc.createTextNode(counter+"");
	    linenum.appendChild(linnum);
	    ridaitem.appendChild(linenum);


	    Node toote_ean = doc.createElement("EAN");
	    Text toote_eanVal = doc.createTextNode(line.getEAN());
	    toote_ean.appendChild(toote_eanVal);
	    ridaitem.appendChild(toote_ean);

	     
	     String toode  = line.getToode();
	     if(toode!= null){
	    	 toode = toode.trim();
	     }else{
	    	 toode = "";
	     }

	    Node toote_kood = doc.createElement("SupplierItemCode");
	    Text toote_koodVal = doc.createTextNode(toode);
	    toote_kood.appendChild(toote_koodVal);
	    ridaitem.appendChild(toote_kood);
	    

   String nimetu = escape(line.getTooteNimi().replace('`',' ').replace('�',' '));
   if(nimetu.trim().length() == 0 && prods.get(toode) != null){
	   nimetu = ((Product)prods.get(toode)).getName();
   }
	    Text nimetusVal = doc.createTextNode(nimetu);
	    Node nimetus = doc.createElement("ItemDescription");
	    nimetus.appendChild(nimetusVal);
	    ridaitem.appendChild(nimetus);

	    Node type = doc.createElement("ItemType");
	    Text typeVal = doc.createTextNode("CU");
	    type.appendChild(typeVal);
	    ridaitem.appendChild(type);
	    
	    double kogus = line.getKogus();
	    Node koGus = doc.createElement("OrderedQuantity");
	    Text kogusVal = doc.createTextNode(getNF().format(kogus));
	    koGus.appendChild(kogusVal);
	    ridaitem.appendChild(koGus);

	    Node size = doc.createElement("OrderedUnitPacksize");
	    Text sizeVal = doc.createTextNode(getNF().format(1));
	    size.appendChild(sizeVal);
	    ridaitem.appendChild(size);

	    Node yhik = doc.createElement("UnitOfMeasure");
	    Text yhikVal = doc.createTextNode(line.getUnit());
	    yhik.appendChild(yhikVal);
	    ridaitem.appendChild(yhik);
        double priced = 0;	    
	    if(line.getDiscount() > 0){
	        
		    Node disc = doc.createElement("LineDiscount");
		    Text discVal = doc.createTextNode(getNF().format(line.getDiscount()));
		    disc.appendChild(discVal);
		    ridaitem.appendChild(disc);
	    }
	    Node price = doc.createElement("OrderedUnitNetPrice");
	    Text priceVal = doc.createTextNode(getNF().format(priced));
	    price.appendChild(priceVal);
	    ridaitem.appendChild(price);
	    
	
	    Node km = doc.createElement("TaxRate");
	    Text kmVal = doc.createTextNode(getNF().format(21));
	    km.appendChild(kmVal);
	    ridaitem.appendChild(km);
	     
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
  public static final String SEPARATOR = "|";

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
