package eevaeksport.imp.driver;




import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
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

public class AnkvarsFloraImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());
    boolean testing = false;
  
  public AnkvarsFloraImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    if("true".equals( parent.getProps().getProperty("testing"))){
    	testing = true;
    }
    loadSignatures();
  }
  private Vector signatures = new Vector();

	private void loadSignatures(){
	
try{		
		File f = new File("signatures.vec");
		Calendar ss = Calendar.getInstance();
		
		if (f.exists() && f.lastModified() < ss.getTimeInMillis() - 7200000) {// kui on yle tunni vana siis maha 
			f.delete();
			signatures = new Vector();
		} else {
			signatures = (Vector) readObject("signatures.vec");
		}

		
	} catch (Exception e) {
		signatures = new Vector();
	}

	}

		
	   public static Object readObject(String s) throws IOException,
     ClassNotFoundException, IOException {
   Object obj = null;
   File f = new File(s);
   if (f.exists()){
     ObjectInputStream ois = new ObjectInputStream(new FileInputStream(s));
     obj = ois.readObject();
     ois.close();
   }
   return obj;
 }


  
  private TreeMap names = null;

private Connection getNewCon() throws SQLException{
      
      return  DriverManager.getConnection("jdbc:firebirdsql://192.168.1.122:3050/C:/ANKRAVS/DB/PRECES.GDB", "SYSDBA", "masterkey");
      
    }
  

  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  
  
  public String identifyCustomer(String id,String regnr)  {

    String acc = null;
    return acc;

  }

  
  public String identifyProduct(String ean, String prodID) throws SQLException{
	
    String prod = null;
    return prod;
  
}
  
  private Product getProd(String toode){
	  
	 
	  String sql = " SELECT  pr.gnos AS nimi, pr.artikuls as ean FROM N_KLPREC  pr  where pr.isn =" + toode ;
	  	   System.out.println(sql);
	  	   Product res = new Product(toode);
	  	   res.setName("<No Name>");
	  	   try {
	  		   Connection con = getNewCon();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs = stmt.executeQuery(sql);
	  	        
	  	        if(rs.next()){
	  	        	 res.setName(rs.getString("nimi").trim());
	  	        	 res.setEAN(rs.getString("ean").trim());
	  	        }
	  	        
	  	        rs.close();
	  	        stmt.close();
	  	        con.close();
	  	 
	  	   }catch(Exception e){
	  		 e.printStackTrace();  
	  	   }
	  	   
     return res;
  }

  private String getILN(String id){
	  
		 
	  String sql = " select  kods FROM N_KL_STRV where isn =" + id ;
	  	   System.out.println(sql);
	  	   String res = "<No Name>";
	  	   try {
	  		   Connection con = getNewCon();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs = stmt.executeQuery(sql);
	  	        
	  	        if(rs.next()){
	  	        	res = rs.getString("kods").trim();
	  	        }
	  	        
	  	        rs.close();
	  	        stmt.close();
	  	        con.close();
	  	        
	  	   }catch(Exception e){
	  		 e.printStackTrace();  
	  	   }
     return res;
  }
  
  
  private String getPayer(String id){
	  
		 
	  String sql = " SELECT  pr.idkods AS nimi FROM N_KLIENT  pr  where pr.isn =" + id ;
	  	   System.out.println(sql);
	  	   String res = "<No Name>";
	  	   try {
	  		   Connection con = getNewCon();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs = stmt.executeQuery(sql);
	  	        
	  	        if(rs.next()){
	  	        	res = rs.getString("nimi").trim();
	  	        }
	  	        
	  	        rs.close();
	  	        stmt.close();
	  	        con.close();
	  	        
	  	   }catch(Exception e){
	  		 e.printStackTrace();  
	  	   }
     return res;
  }
  
  public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
	
  private int counter = 0;
  
  public void makeOrder(Order order) throws SQLException{
	  
	  if (signatures == null){
   	   signatures = new Vector();
      }
	  
	  String signature = order.getSignature();
	
	  System.out.println("signature" + signatures.contains(signature) + "  "+ signature );
		
	  if(signatures.contains(signature)){
		  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
	  }else{
		
	  
	  Vector splitted =  splitOrder(order);
 	  Enumeration en = splitted.elements();
 	  while(en.hasMoreElements()){
 		  Order ord = (Order)en.nextElement();
 			  makeOutput(ord);  
 			  counter++;
	}
 	 signatures.add(signature);
	  }
 	    
      try {
		saveObject(signatures, "signatures.vec");
	} catch (IOException e) {
		e.printStackTrace();
	}
  }
  
  
  
  private Vector splitOrder (Order order){
	  
	  Vector result = new Vector();

	  if (order.getDokNr() == null || order.getDokNr().trim().length() == 0){
		  order.setDokNr((Calendar.getInstance().getTimeInMillis() + "").substring(3));
	  }
	//  System.out.println("Infokene " + order.getInfo() + " " + order.getDokNr());

	  Order ordS = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ordS0 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ordSG = new Order(order.getCompany(),order.getAgentFirstName());
	  
	  ordS.setAgentCode(order.getAgentCode());
	  ordS.setDokNr(order.getDokNr());
	  ordS.setCompName(order.getShipMode());
	  ordS.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordS.setStatus(order.getStatus()); 
	  ordS.setInfo(order.getInfo());
	  ordS.setDealer("order");

	  ordS0.setAgentCode(order.getAgentCode());
	  ordS0.setDokNr(order.getDokNr());
	  ordS0.setCompName(order.getShipMode());
	  ordS0.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordS0.setStatus(order.getStatus());
	  ordS0.setInfo(order.getInfo());
	  ordS0.setDealer("100");

	  
	  ordSG.setAgentCode(order.getAgentCode());
	  ordSG.setDokNr(order.getDokNr());
	  ordSG.setCompName(order.getShipMode());
	  ordSG.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordSG.setStatus(order.getStatus());
	  ordSG.setInfo(order.getInfo());
	  ordSG.setDealer("gift");


	  
	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
		  
		  

		    if(lin.getDiscount() == -2){
		     	ordSG.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		    	ordSG.setDokNr(order.getDokNr().substring( 0,order.getDokNr().length()-1) + "G");
			    ordSG.setReference(order.getReference());
		     }
		
		    if(lin.getDiscount() == 0){
		    	   ordS.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     	ordS.setDokNr(order.getDokNr());
			     	ordS.setReference(order.getReference()); 
			 }
		    

		    if(lin.getDiscount() > 0){
		    	   ordS0.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     	ordS0.setDokNr(order.getDokNr().substring( 0,order.getDokNr().length()-1) + "N");
			     	ordS0.setReference(order.getReference()); 
			 }
		  
	  }
	  
	  if(ordS.getOrderLines() != null && ordS.getOrderLines().size()>0){
		  result.add(ordS);  
	  }

	  if(ordS0.getOrderLines() != null && ordS0.getOrderLines().size()>0){
		  result.add(ordS0);  
	  }

	  if(ordSG.getOrderLines() != null && ordSG.getOrderLines().size()>0){
		  result.add(ordSG);  
	  }
	  
	  
	 System.out.println("Result =" +order.getDokNr()
			 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }



  
  
  
  
    
  

  public static  String escapeXML(String s){
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
  
  
  private void makeOutput(Order order){
	  
	  if(names == null){
		 // names = this.custNames();
	  }
	  
	 	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    
	      try {
	    	
	    	  
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.newDocument();
	        Element root = doc.createElement("Document-Order");
	        doc.appendChild(root);


	  //      String filiaalStr = null;
	    //    String tarnijaKoodStr = null;

	        Node docheader = doc.createElement("Document-Header");
	        root.appendChild(docheader);
	        
	     /*   Node un2 = doc.createElement("OriginalFileName");
	        Text unVal2 = doc.createTextNode(order.getShipTime());
	        un2.appendChild(unVal2);
	        docheader.appendChild(un2);
	       
	        
	        */
	        try{

		        Node un4 = doc.createElement("DocumentReceiveDateTime");
		        Calendar cal = Calendar.getInstance();
		      
		        String month = (cal.get(Calendar.MONTH) + 1) + "";
		        if (month.length() == 1) month = "0" + month;
		        String day = cal.get(Calendar.DAY_OF_MONTH) + "";
		        if (day.length() == 1) day = "0" + day;

		        String hour = cal.get(Calendar.HOUR_OF_DAY) + "";
		        if (hour.length() == 1) hour = "0" + hour;
		        String minute = cal.get(Calendar.MINUTE) + "";
		        if (minute.length() == 1) minute = "0" + minute;
		        
		        String second = cal.get(Calendar.SECOND) + "";
		        if (second.length() == 1) second = "0" + second;
		        
		        //[ HH:MM:SS]
		        
		        String dateString = cal.get(Calendar.YEAR) +"-"+ month +"-"+ day + "T" + hour + ":" + minute + ":" + second + "";
		        
		        Text unVal4 = doc.createTextNode(dateString);
		        un4.appendChild(unVal4);
		        docheader.appendChild(un4);
		        
	        }catch(Exception e){
	        	
	        }
	        
	        
	        String type = "ORDER";
	         Node yhiku_hind = doc.createElement("DocumentType");
		     Text yhiku_hindVal = doc.createTextNode(type);
		     yhiku_hind.appendChild(yhiku_hindVal);
		     docheader.appendChild(yhiku_hind);
		     
		     
	         Node yhiku_hind2 = doc.createElement("DocumentSource");
		     Text yhiku_hindVal2 = doc.createTextNode("S");
		     yhiku_hind2.appendChild(yhiku_hindVal2);
		     docheader.appendChild(yhiku_hind2);
		
		   
		     
	        String add = (Calendar.getInstance().getTimeInMillis()+"").substring(11);
	        String number = (Calendar.getInstance().getTimeInMillis()+"").substring(4);
	        
     long uni = Calendar.getInstance().getTimeInMillis();
     Node header = doc.createElement("Order-Header");
	        
	        
	        Node nr = doc.createElement("OrderNumber");
	        Text nrVal = doc.createTextNode(number + "" + counter);
	        nr.appendChild(nrVal);
	        header.appendChild(nr);
	        

	        Node kuupaev = doc.createElement("OrderDate");
	        Calendar cal = Calendar.getInstance();
	      

	        String month = (cal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	        String day = cal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;
  String year = cal.get(Calendar.YEAR) + "";
  if(year.length() == 2){
	  year = "20" + year;
  }
	        String dateString = year +"-"+ month +"-"+ day;
	        Text kuupaevVal = doc.createTextNode(dateString);
	        kuupaev.appendChild(kuupaevVal);
	        header.appendChild(kuupaev);


	        
	        Node un3 = doc.createElement("OrderCurrency");
	        Text unVal3 = doc.createTextNode("EUR");
	        un3.appendChild(unVal3);
	        header.appendChild(un3);
	    
	        
	        Node delkuupaev = doc.createElement("ExpectedDeliveryDate");
	        Calendar delcal = Calendar.getInstance();
	        delcal.setTime(order.getShipCal().getTime());
	        
	        //kui on täna reede ja on pandud esmaspäevaks siis paneme homseks
	        Calendar cal2 = Calendar.getInstance();
	        
	          if (cal2.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY  && 
	        		  delcal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY  ) {
	            cal2.add(Calendar.DAY_OF_MONTH, 1);
	            delcal.setTimeInMillis(cal2.getTimeInMillis());
	          }
	        
	        if(order.getShipCal().getTimeInMillis() > delcal.getTimeInMillis() ){
	        	delcal.setTimeInMillis(order.getShipCal().getTimeInMillis());
	        }

	         month = (delcal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	         day = delcal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;
			   year = delcal.get(Calendar.YEAR) + "";
			  if(year.length() == 2){
				  year = "20" + year;
			  }
	         dateString = year +"-"+ month +"-"+ day;
	        Text delkuupaevVal = doc.createTextNode(dateString);
	        delkuupaev.appendChild(delkuupaevVal);
	        header.appendChild(delkuupaev);		        

	        Node age = doc.createElement("SalesAgent");
	        Text ageVal = doc.createTextNode(order.getAgent());
	        age.appendChild(ageVal);
	        header.appendChild(age);
	 
	        type = "order";
	        // kuidas edastada kingitus 
		     if("gift".equals(order.getDealer())){
		    	 type = "degu";
		     }
		     if("100".equals(order.getDealer())){
		    	 type = "zero";
		     }
	        
		     Node age2 = doc.createElement("DocType");
		        Text ageVal2 = doc.createTextNode(type);
		        age2.appendChild(ageVal2);
		        header.appendChild(age2);
		     
		   
		     Node yhiku_hind3 = doc.createElement("DocumentFunctionCode");
		     Text yhiku_hindVal3 = doc.createTextNode("O");
		     yhiku_hind3.appendChild(yhiku_hindVal3);
		     header.appendChild(yhiku_hind3);
	        
	        
	        
			String comm = "";
	 		if(order.getInfo() != null && order.getInfo().trim().length() >0){

	 			Node com = doc.createElement("comment");
		        Text comVal = doc.createTextNode(order.getInfo());
		        com.appendChild(comVal);
		      //  header.appendChild(com);
		        comm = order.getInfo();
	 		}
	 		System.out.println("Comment " + comm);
	        
	 		Node war = doc.createElement("Warehouse");
	        Text warVal = doc.createTextNode(order.getWarehouse());
	        war.appendChild(warVal);
	      //  header.appendChild(war);
	        int div = order.getCompany().indexOf("-");
             String kl = order.getCompany().substring(0,div);
	         String aad = order.getCompany().substring(div+1);
	         
	         
	        root.appendChild(header);
	        
          String payer = getPayer(kl);
          String aadre = getILN(aad);

	        Node parties = doc.createElement("Order-Parties");
	        
	        Node receiver = doc.createElement("Buyer");
	        Node ise21 = doc.createElement("ILN");
	        Text nimiVal21 = doc.createTextNode(payer);
	        ise21.appendChild(nimiVal21);
	        Node ise1 = doc.createElement("CodeByBuyer");
	        Text nimiVal1 = doc.createTextNode(payer);
	        ise1.appendChild(nimiVal1);
	        receiver.appendChild(ise21);
	        receiver.appendChild(ise1);

	        parties.appendChild(receiver);
	        
	        
	        
	        Node seller = doc.createElement("DeliveryPoint");
	       
	        Node ise2 = doc.createElement("ILN");
	        Text nimiVal2 = doc.createTextNode(aadre);
	        ise2.appendChild(nimiVal2);
	        Node ise = doc.createElement("CodeByBuyer");
	        Text nimiVal = doc.createTextNode(aadre);
	        ise.appendChild(nimiVal);
	        seller.appendChild(ise2);
	        seller.appendChild(ise);
	       

	        parties.appendChild(seller);
	        root.appendChild(parties);



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

	        Node summary = doc.createElement("Order-Summary");
	        
		      
	        Node TotalLines = doc.createElement("TotalLines");
	        Text TotalLinesVal = doc.createTextNode(coun + "");
	        TotalLines.appendChild(TotalLinesVal);
	        summary.appendChild(TotalLines);

	        Node TotalOrderedAmount = doc.createElement("TotalOrderedAmount");
	        Text TotalOrderedAmountVal = doc.createTextNode(order.getSum() + "");
	        TotalOrderedAmount.appendChild(TotalOrderedAmountVal);
	        summary.appendChild(TotalOrderedAmount);
	        
	        Node TotalNetAmount = doc.createElement("TotalNetAmount");
	        Text TotalNetAmountVal = doc.createTextNode(order.getSum() + "");
	        TotalNetAmount.appendChild(TotalNetAmountVal);
	        summary.appendChild(TotalNetAmount);
	       
	        root.appendChild(summary);
	 	
	      String fileName = parent.getProps().getProperty("conv.dir") + order.getAgent() + "." + order.getDokNr().trim()+ "-" +counter  +  ".xml";
	      String fileName2 = parent.getProps().getProperty("conv.dir2") + order.getAgent() + "." + order.getDokNr().trim()+ "-" +counter  +  ".xml";
		    
	      System.out.println(fileName);
	      OutputFormat of = new OutputFormat();
	        of.setIndenting(true);
	        of.setLineSeparator(System.getProperty("line.separator"));
	        of.setEncoding("UTF-8");
	        XMLSerializer ser = new XMLSerializer(of);
	        
	        
	        Writer out = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(fileName), "UTF-8"));
				try {
					ser.setOutputCharStream(out);
			        ser.serialize(doc);
				} finally {
				    out.close();
				}
	        
				out = new BufferedWriter(new OutputStreamWriter(
					    new FileOutputStream(fileName2), "UTF-8"));
					try {
						ser.setOutputCharStream(out);
				        ser.serialize(doc);
					} finally {
					    out.close();
					}
				
				
			} catch (Exception e) {
			e.printStackTrace();
		}
	 }


   
   
   
   private void addLine(OrderLine line, Node read, Document doc,int counter ,Order order) throws SQLException{
	   

	
	     Node rida = doc.createElement("Line");
	     read.appendChild(rida);

	     Node ridaitem = doc.createElement("Line-Item");


	     Node linenum = doc.createElement("LineNumber");
	     Text linnum = doc.createTextNode(counter+"");
	     linenum.appendChild(linnum);
	     ridaitem.appendChild(linenum);


	 /*    Node toote_ean = doc.createElement("EAN");
	     Text toote_eanVal = doc.createTextNode(line.getEAN());
	     toote_ean.appendChild(toote_eanVal);
	     ridaitem.appendChild(toote_ean);
*/
	     Product prod  = getProd(line.getToode());
	     
	     String ean  = Converter.deCode(prod.getEAN());
	    
	     
	     Node toote_kood23 = doc.createElement("EAN");
	     Text toote_koodVal23 = doc.createTextNode(ean);
	     toote_kood23.appendChild(toote_koodVal23);
	     ridaitem.appendChild(toote_kood23);

	     
	     if(line.getPartii() != null && line.getPartii().length() > 0 ){
		    Node toote_kood = doc.createElement("LineComment");
		    Text toote_koodVal = doc.createTextNode(line.getPartii().trim());
		    toote_kood.appendChild(toote_koodVal);
		    ridaitem.appendChild(toote_kood);
			
            }


	     String toode  = Converter.deCode(line.getToode());
	     Node toote_kood2 = doc.createElement("BuyerItemCode");
	     Text toote_koodVal2 = doc.createTextNode(toode);
	     toote_kood2.appendChild(toote_koodVal2);
	     ridaitem.appendChild(toote_kood2);
	     
	     
        
	     String nimi = prod.getName();
	     if(nimi != null){
	    	 Text nimetusVal = doc.createTextNode(nimi.replace('`',' ').replace('´',' '));
		     Node nimetus = doc.createElement("ItemDescription");
		     nimetus.appendChild(nimetusVal);
		     ridaitem.appendChild(nimetus);
	     }

	    

	     double kogus = line.getKogus();
	     double price = line.getPrice();
	     String unit = line.getUnit();
	     
	     Node koGus = doc.createElement("OrderedQuantity");
	     Text kogusVal = doc.createTextNode(getNF().format(kogus));
	     koGus.appendChild(kogusVal);
	     ridaitem.appendChild(koGus);

	     
	     Node koGus1 = doc.createElement("OrderedUnitPacksize");
	     Text kogusVal1 = doc.createTextNode("1.000");
	     koGus1.appendChild(kogusVal1);
	     ridaitem.appendChild(koGus1);

	     Node koGus2 = doc.createElement("SupplierUnitPacksize");
	     Text kogusVal2 = doc.createTextNode("1.000");
	     koGus2.appendChild(kogusVal2);
	     ridaitem.appendChild(koGus2);

	     
	     
	     
	     if(line.getDiscount() > 0){
		     Node yhiku_hind = doc.createElement("InvoiceUnitNetPrice");
		     Text yhiku_hindVal = doc.createTextNode("0");//this.nf.format(price)
		     yhiku_hind.appendChild(yhiku_hindVal);
		     ridaitem.appendChild(yhiku_hind);
	     }else   if("gift".equals(order.getDealer())){
	    	 Node yhiku_hind = doc.createElement("InvoiceUnitNetPrice");
		     Text yhiku_hindVal = doc.createTextNode("0.01");//this.nf.format(price)
		     yhiku_hind.appendChild(yhiku_hindVal);
		     ridaitem.appendChild(yhiku_hind);
	     }
	     
	     Node yhik = doc.createElement("UnitOfMeasure");
	     Text yhikVal = doc.createTextNode(unit);
	     yhik.appendChild(yhikVal);
	     ridaitem.appendChild(yhik);

	     rida.appendChild(ridaitem);
	     addNumLine(); 
	   }

   public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}
  


	   private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);


 
  public String getDriverVersion(){
    return "SkaiteKMImport";
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


  
  private String url = "jdbc:odbc:merisoft";
  private ImportPanel parent;
  private boolean isAmount = false;
  private String trouble = null;

  public String getTrouble() {
  	// TODO Auto-generated method stub
  	return trouble;
  }
  
}
