package eevaeksport.imp.driver;




import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.Email;
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

public class AgnumRinkuImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

    boolean testing = false;
  
  public AgnumRinkuImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    if("true".equals( parent.getProps().getProperty("testing"))){
    	testing = true;
    }
   // loadSignatures();
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
  
  private void loadSignatures(){
		
	  try{		
	  		File f = new File("signatures.vec");
	  		Calendar ss = Calendar.getInstance();
	  		
	  		if (f.exists() && f.lastModified() < ss.getTimeInMillis() - 3600000) {// kui on yle tunni vana siis maha 
	  			f.delete();
	  			signatures = new Vector();
	  		} else {
	  			signatures = (Vector) readObject("signatures.vec");
	  		}

	  		
	  	} catch (Exception e) {
	  		signatures = new Vector();
	  	}

	  	}
  
  

  private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	  }
  
  private Customer  getCust(String code){
	 Customer res = null;
	 
	  String kliid = code.substring(0,code.indexOf("-"));
	  String subid = code.substring(code.indexOf("-")+1);
	  String sql =
	            "SELECT  k.id_kl as klid,s.id_subkl as sid, k.kod as kood , k.pavad as nimi, s.kod as skood , s.adr as subnimi,k.adr AS aadress,' ' AS ale, ' ' AS visit ,s.id_agent   " + 
	          "  AS agent,'' as maksja , k.tlf as telefon ,k.term_p as term , k.f1 as grupp , rkod as regis , k.pozymiai as sms ,k.F3 as cuILN ,s.F2 as adrILN " +
			  " FROM kl k  left join subkl s on s.id_kl=k.id_kl where   k.deb='Y' and "
			  + "k.color!=255 and k.id_kl ='" + kliid + "'  and   s.id_subkl ='" + subid + "'";
	   System.out.println(sql);
	   try {
		   
		
		    Connection con = getNewConnection();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(sql);
	        
	        while(rs.next()){
	        	
	        	res = new Customer(rs.getString("kood").trim());
	        	res.setName(rs.getString("nimi").trim());
	        	res.setPayer(rs.getString("skood").trim());
	        	res.setRegnr(rs.getString("regis").trim());
	        	res.setSMS =(rs.getString("sms").trim());
	        	res.currency = rs.getString("cuILN").trim();
	        	res.GLNCode = rs.getString("adrILN").trim();
			    
		    }
	       
	        rs.close();
	        stmt.close();
	        con.close(); 
	   
	   }catch(Exception e){
		  e.printStackTrace(); 
	   }
	      
	        
      // System.out.println("customerName " + res);
	  
	  return res ;
  }

  public String getAgetCode(String code) {
     
      String  cmd = " SELECT kod as yksus FROM drb  where  id_drb = " + code  ;
      				  
     String res = null;

      try {
    	  
    	  Connection con = getNewConnection();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	      
          while (rs.next()) {
           res = rs.getString("yksus").trim();
          }
          rs.close();
	        stmt.close();
	        con.close(); 
      } catch (Exception e) {
         e.printStackTrace();
      }
     
      return res;
  }
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  
  public String identifyCustomer(String id,String regnr)  {
    String acc = null;
    return acc;
  }

  
  public String identifyProduct(String ean, String prodID) throws SQLException{
    return null;  
}
 
  


private Vector signatures = new Vector();

  
  public void makeOrder(Order order) throws SQLException{

	   
	  trouble = null;
	  if (signatures == null){
	   	   signatures = new Vector();
	      }
     
     boolean tagastus = false;
	 	 Vector lin = order.getOrderLines();
	 	 int nega = 0;
	 	 for (int i = 0; i < lin.size(); i++) {
	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	 		if(ol.getKogus() < 0 && (order.getStatus() == 5 || order.getStatus() == 4 || order.getStatus() == 2)){
	 			nega++;
	 		}
	 	 }  
	
	 	 if(nega == lin.size()){
		    	tagastus = true;
		    }
		    String signature = order.getSignature();
			  if(signatures.contains(signature)){
				  this.trouble = "REPRETATING ORDER agent :" +  getAgetCode(order.getAgent()) + " customer :" + order.getCompany() ;
			  }else{
				  
				  makeOutput(order);  
				  signatures.add(signature);
			  }
			   try {
					Converter.saveObject(signatures, "signatures.vec");
				} catch (IOException e) {
					e.printStackTrace();
				}
			
    
  }
  

	
  private String trouble = null;

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
  
  
  
  public static String getXMLDate(Calendar cal , boolean dot) {
	  String sep = "-";
	  if(dot){
		  sep =".";
	  }
		 String month = (cal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	      String   day = cal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;	
	      return cal.get(Calendar.YEAR)+ sep + month +sep+ day;
		 
	}
  
  
  
  
  
  
  private void makeOutput(Order order) {

		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();
			Element root = doc.createElement("Document-Order");
			doc.appendChild(root);

			Node docheader = doc.createElement("Document-Header");
			root.appendChild(docheader);

			Node un2 = doc.createElement("DocumentType");
			Text unVal2 = doc.createTextNode("ORDER");
			un2.appendChild(unVal2);
			docheader.appendChild(un2);

			Node un3 = doc.createElement("DocumentSource");
			Text unVal3 = doc.createTextNode("E");
			un3.appendChild(unVal3);
			docheader.appendChild(un3);
			try {

				long ll = Long.parseLong(order.getAgentCode());
				Node un4 = doc.createElement("DateCreated");

				Calendar cal = Calendar.getInstance();
				cal.setTime(new java.util.Date(ll));

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

				// [ HH:MM:SS]

				String dateString = cal.get(Calendar.YEAR) + "-" + month + "-" + day + "[" + hour + ":" + minute + ":"
						+ second + "]";

				Text unVal4 = doc.createTextNode(dateString);
				un4.appendChild(unVal4);
				docheader.appendChild(un4);

			} catch (Exception e) {

			}

			String number = order.getDokNr();
			if (number == null || number.length() == 0) {
				number = (Calendar.getInstance().getTimeInMillis() + "").substring(5);
			}

			long uni = Calendar.getInstance().getTimeInMillis();
			Node header = doc.createElement("Order-Header");

			Node nr = doc.createElement("OrderNumber");
			Text nrVal = doc.createTextNode("MS-" + number + "");
			nr.appendChild(nrVal);
			header.appendChild(nr);

			Node age = doc.createElement("SalesAgent");
			String agCode = getAgetCode(order.getAgent());
			Text ageVal = doc.createTextNode(agCode);
			age.appendChild(ageVal);
			header.appendChild(age);

			String comm = "";
			if (order.getInfo() != null && order.getInfo().trim().length() > 0) {

				Node com = doc.createElement("Remarks");
				Text comVal = doc.createTextNode(order.getInfo());
				com.appendChild(comVal);
				header.appendChild(com);
				comm = order.getInfo();

				Node com2 = doc.createElement("comment");
				Text comVal2 = doc.createTextNode(order.getInfo());
				com2.appendChild(comVal2);
				header.appendChild(com2);

			}
			System.out.println("Remarks " + comm);

			Node kuupaev = doc.createElement("OrderDate");
			Calendar cal = Calendar.getInstance();

			String month = (cal.get(Calendar.MONTH) + 1) + "";
			if (month.length() == 1)
				month = "0" + month;
			String day = cal.get(Calendar.DAY_OF_MONTH) + "";
			if (day.length() == 1)
				day = "0" + day;
			String year = cal.get(Calendar.YEAR) + "";
			if (year.length() == 2) {
				year = "20" + year;
			}
			String dateString = year + "-" + month + "-" + day;
			Text kuupaevVal = doc.createTextNode(dateString);
			kuupaev.appendChild(kuupaevVal);
			header.appendChild(kuupaev);

			Node curr = doc.createElement("OrderCurrency");
			Text currVal = doc.createTextNode("EUR");
			curr.appendChild(currVal);
			header.appendChild(curr);

			Node delkuupaev = doc.createElement("ExpectedDeliveryDate");
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
			dateString = year + "-" + month + "-" + day;
			Text delkuupaevVal = doc.createTextNode(dateString);
			delkuupaev.appendChild(delkuupaevVal);
			header.appendChild(delkuupaev);

			Node expectedDeliveryTime = doc.createElement("ExpectedDeliveryTime");
			Text expectedDeliveryTimeVal = doc.createTextNode("00:00");
			expectedDeliveryTime.appendChild(expectedDeliveryTimeVal);
			header.appendChild(expectedDeliveryTime);

			
			Node un34 = doc.createElement("DocumentFunctionCode");
			Text unVal34 = doc.createTextNode("O");
			un34.appendChild(unVal34);
			header.appendChild(un34);
			
			root.appendChild(header);
			
			
			

			 Customer cust = this.getCust(order.getCompany());
				String cuCode = cust.getID();
		    	String adrCode = cust.getPayer();
		    	String cuILN = cust.currency;
		    	String adrILN = cust.GLNCode;

			Node parties = doc.createElement("Document-Parties");
			Node seller = doc.createElement("DeliveryPoint");

			
			Node ise = doc.createElement("CodeBySupplier");
			Text nimiVal = doc.createTextNode(adrCode);
			ise.appendChild(nimiVal);
			Node ise2 = doc.createElement("ILN");
			Text nimiVal2 = doc.createTextNode(adrILN);
			ise2.appendChild(nimiVal2);

			seller.appendChild(ise);
			seller.appendChild(ise2);

			parties.appendChild(seller);

			Node receiver = doc.createElement("Receiver");

			Node iseR = doc.createElement("CodeBySupplier");
			Text nimiRVal = doc.createTextNode(adrCode);
			iseR.appendChild(nimiRVal);
			Node ise2R = doc.createElement("ILN");
			Text nimiVal2R = doc.createTextNode(adrILN);
			ise2R.appendChild(nimiVal2R);

			receiver.appendChild(iseR);
			receiver.appendChild(ise2R);

			parties.appendChild(receiver);

			Node buyer = doc.createElement("Buyer");

			Node iseX2 = doc.createElement("ILN");
			Text nimiXVal2 = doc.createTextNode(cuILN);
			iseX2.appendChild(nimiXVal2);

			Node iseX = doc.createElement("CodeByBuyer");
			Text nimiXVal = doc.createTextNode(cuCode);
			iseX.appendChild(nimiXVal);
			buyer.appendChild(iseX2);
			buyer.appendChild(iseX);
			parties.appendChild(buyer);
			root.appendChild(parties);

			
			
			Node partiesO = doc.createElement("Order-Parties");
			Node buyerO = doc.createElement("Buyer");

			Node iseX2O = doc.createElement("ILN");
			Text nimiXVal2O = doc.createTextNode(cuILN);
			iseX2O.appendChild(nimiXVal2O);

			Node iseXO = doc.createElement("CodeByBuyer");
			Text nimiXValO = doc.createTextNode(cuCode);
			iseXO.appendChild(nimiXValO);

			buyerO.appendChild(iseX2O);
			buyerO.appendChild(iseXO);

			partiesO.appendChild(buyerO);
			
			Node sellerO = doc.createElement("DeliveryPoint");

			
			Node iseO = doc.createElement("CodeBySupplier");
			Text nimiValO = doc.createTextNode(adrCode);
			iseO.appendChild(nimiValO);
			Node ise2O = doc.createElement("ILN");
			Text nimiVal2O = doc.createTextNode(adrILN);
			ise2O.appendChild(nimiVal2O);

			sellerO.appendChild(iseO);
			sellerO.appendChild(ise2O);

			partiesO.appendChild(sellerO);

			root.appendChild(partiesO);

			// RIDADE LISAMINE
			Node read = doc.createElement("Order-Lines");

			int coun = 0;
			Iterator iter = order.getOrderLines().iterator();
			while (iter.hasNext()) {
				OrderLine line = (OrderLine) iter.next();
				String toode = line.getToode();
				if (toode != null) {
					toode = toode.trim();
				} else {
					toode = "";
				}
				if (line.getKogus() != 0 && !toode.equals("")) {

					coun++;
					addLine(line, read, doc, coun, order);
				}
			}

			root.appendChild(read);
			
		    Node summary = doc.createElement("Order-Summary");
	        
		      
	        Node TotalLines = doc.createElement("TotalLines");
	        Text TotalLinesVal = doc.createTextNode(coun + "");
	        TotalLines.appendChild(TotalLinesVal);
	        summary.appendChild(TotalLines);

	        Node TotalOrderedAmount = doc.createElement("TotalOrderedAmount");
	        Text TotalOrderedAmountVal = doc.createTextNode(order.getCalcSum() + "");
	        TotalOrderedAmount.appendChild(TotalOrderedAmountVal);
	        summary.appendChild(TotalOrderedAmount);
	        
	        Node TotalNetAmount = doc.createElement("TotalNetAmount");
	        Text TotalNetAmountVal = doc.createTextNode(order.getCalcSum() + "");
	        TotalNetAmount.appendChild(TotalNetAmountVal);
	        summary.appendChild(TotalNetAmount);
	       
	        root.appendChild(summary);

			String fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr().trim() + (uni + "").trim()
					+ ".xml";
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
	}

	private void addLine(OrderLine line, Node read, Document doc, int counter, Order order) throws SQLException {

		 if(prods == null) {
  		   prods = getProducts();
  	   }
		
		Node rida = doc.createElement("Line");
		read.appendChild(rida);

		Node ridaitem = doc.createElement("Line-Item");

		Node linenum = doc.createElement("LineNumber");
		Text linnum = doc.createTextNode(counter + "");
		linenum.appendChild(linnum);
		ridaitem.appendChild(linenum);

		/*
		 * Node toote_ean = doc.createElement("EAN"); Text toote_eanVal =
		 * doc.createTextNode(line.getEAN());
		 * toote_ean.appendChild(toote_eanVal); ridaitem.appendChild(toote_ean);
		 */

		String toode = line.getToode();

		Node btoote_kood = doc.createElement("BuyerItemCode");
		Text btoote_koodVal = doc.createTextNode(toode);
		btoote_kood.appendChild(btoote_koodVal);
		ridaitem.appendChild(btoote_kood);

	  	  Product prod = (Product)prods.get(toode);
		Node toote_kood = doc.createElement("SupplierItemCode");
		Text toote_koodVal = doc.createTextNode(toode);
		toote_kood.appendChild(toote_koodVal);
		ridaitem.appendChild(toote_kood);
		String ean = prod.getEAN();
		if(ean != null) {
			Node ean_kood = doc.createElement("EAN");
			Text ean_koodVal = doc.createTextNode(ean);
			ean_kood.appendChild(ean_koodVal);
			ridaitem.appendChild(ean_kood);		
		}
	

		String nimi = prod.getName();
		if (nimi != null) {
			Text nimetusVal = doc.createTextNode(nimi.replace('`', ' ').replace('´', ' '));
			Node nimetus = doc.createElement("ItemDescription");
			nimetus.appendChild(nimetusVal);
			ridaitem.appendChild(nimetus);
		}

		double kogus = line.getKogus();
		double price = line.getPrice();
		double disc = line.getDiscount();
		String unit = line.getUnit();

		Node koGus = doc.createElement("OrderedQuantity");

		Text kogusVal = doc.createTextNode(getNF().format(kogus));
		koGus.appendChild(kogusVal);
		ridaitem.appendChild(koGus);

		if (disc > 0) {
			// price = price * (100-disc)/100;
			Node yhiku_hind = doc.createElement("OrderedUnitNetPrice");
			Text yhiku_hindVal = doc.createTextNode(this.nf.format(price));
			yhiku_hind.appendChild(yhiku_hindVal);
			ridaitem.appendChild(yhiku_hind);
		}

		Node yhiku_va = doc.createElement("TaxRate");
		Text yhiku_vaVla = doc.createTextNode("21");
		yhiku_va.appendChild(yhiku_vaVla);
		ridaitem.appendChild(yhiku_va);

		Node yhik = doc.createElement("UnitOfMeasure");
		Text yhikVal = doc.createTextNode(unit);
		yhik.appendChild(yhikVal);
		ridaitem.appendChild(yhik);

		rida.appendChild(ridaitem);
		addNumLine();
	}

	

  
 private void makeOutputOld(Order order , boolean ret ){
	        
	 	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	   
	      try {   Customer cust = this.getCust(order.getCompany());

	    	String cuCode = cust.getID();
	    	String adrCode = cust.getPayer();
	    	String agCode = getAgetCode(order.getAgent());
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.newDocument();
	        Element agdata = doc.createElement("AgnumData");
	        doc.appendChild(agdata);
	        Calendar cal = Calendar.getInstance();
	     
	        agdata.setAttribute("CreatedOn", getXMLDate(cal,false));
	        agdata.setAttribute("Version", "26");
	        agdata.setAttribute("CreatedByApp", "Merisoft");
	        agdata.setAttribute("CreatedByLogin", "Merisoft");
		 
	        
	        Element custs = doc.createElement("Customers");
	        custs.setAttribute("Count", "1");
	        agdata.appendChild(custs);
	        Element it1 = doc.createElement("Item");
	        custs.appendChild(it1);
	        it1.setAttribute("KOD", cuCode);
	        it1.setAttribute("PAVAD", cust.getName());
	        it1.setAttribute("RKOD", cust.getRegnr());
	        it1.setAttribute("KRD", "N");
	        it1.setAttribute("DEB", "Y");
	        it1.setAttribute("AKTYVUS", "Y");
	        it1.setAttribute("POZYMIAI", "Y");
	        
		    it1.setAttribute("DEFAULT_CURR", "EUR");
	        
	        

	        Element deps = doc.createElement("Departments");
	        deps.setAttribute("Count", "1");
	        agdata.appendChild(deps);
	        Element it2 = doc.createElement("Item");
	        deps.appendChild(it2);
	        it2.setAttribute("KOD", adrCode);
	        it2.setAttribute("KLKOD", cuCode);
	        it2.setAttribute("KLRKOD", cust.getRegnr());
	        
	        
	        Element goods = doc.createElement("Goods");
	        goods.setAttribute("Count", order.getOrderLines().size() +"");
	        agdata.appendChild(goods);
	        for(int i = 0; i < order.getOrderLines().size();i++ ) {
	        	 Element it3 = doc.createElement("Item");
	        	 it3.setAttribute("KOD", adrCode);
	 	         it3.setAttribute("KLKOD", cuCode);
	 	         it3.setAttribute("CLASS", "0");
	 	         it3.setAttribute("KNBVAL", "EUR");
	 	        goods.appendChild(it3);
	        }
	        

	        Element docs = doc.createElement("Documents");
	        docs.setAttribute("Count", "1");
	        agdata.appendChild(docs);
	        Element it3 = doc.createElement("Item");
	        docs.appendChild(it3);
	        it3.setAttribute("DATA", this.getXMLDate(cal, true));
	        it3.setAttribute("DOKNR", order.getDokNr());
	        it3.setAttribute("KL_KOD", cuCode);
	        it3.setAttribute("KL_RKOD", cust.getRegnr());
	        it3.setAttribute("SND_KOD", "ps");
	        it3.setAttribute("VAL", "EUR");
	        it3.setAttribute("KURS", "1");
	        it3.setAttribute("APM_SAL", "1");
	        it3.setAttribute("AG_KOD", agCode);
	        it3.setAttribute("SASK", "70000089560");
	        it3.setAttribute("POZYMIAI", "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
	        it3.setAttribute("M10", "Y");
	        Calendar delcal = Calendar.getInstance();
	        delcal.setTime(order.getShipCal().getTime());
	        it3.setAttribute("PR_DATA", this.getXMLDate(delcal , true));
	        
	    	if(order.getInfo() != null && order.getInfo().trim().length() >0){
				 it3.setAttribute("DOKMEMO", order.getInfo());	
			 }
	   
	        Iterator iter = order.getOrderLines().iterator();
	         sum = 0 ;
	        while (iter.hasNext()){
	          OrderLine line = (OrderLine)iter.next();
	          String toode  = line.getToode();
	 	     if(toode!= null){
	 	    	 toode = toode.trim();
	 	     }else{
	 	    	 toode = "";
	 	     }
	          if(line.getKogus()!= 0 && !toode.equals("")){
	        	  if(prods == null) {
	        		   prods = getProducts();
	        	   }
	        	  Product prod = (Product)prods.get(toode);
	        	  Element rida = doc.createElement("Row");
	        	  rida.setAttribute("KOD", prod.getID());
	        	  rida.setAttribute("BKOD", prod.getEAN());
	        	  rida.setAttribute("KIEKIS", line.getKogus()+"");
	        	  rida.setAttribute("VNT", line.getUnit()+"");
	        	  
	        	  if(line.getDiscount() > 0) {
	        		  rida.setAttribute("PARKN", line.getPrice()+"");
	        	  }
	        	  
	        	  addNumLine(); 
	        	  docs.appendChild(rida);
	          }
	          }


	     String uni = (Calendar.getInstance().getTimeInMillis()+"").substring(6);
	 	
	      String fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr().trim() + (uni + "").trim() +  ".xml";
	  
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
	 }
 
 
 
    private double sum = 0;
   private String comment = null;
   TreeMap prods = null;
   
   private void addLine(OrderLine line, Node read, Document doc,int counter ,Order order , boolean ret 
		   ,String custname) throws SQLException{
	   
  
	
   System.out.println(prods.toString());
	     Node rida = doc.createElement("Line");
	     read.appendChild(rida);

	     Node ridaitem = doc.createElement("Line-Item");


	     Node linenum = doc.createElement("LineNumber");
	     Text linnum = doc.createTextNode(counter+"");
	     linenum.appendChild(linnum);
	     ridaitem.appendChild(linenum);

	     String toode  = line.getToode();
	     Product prod = (Product)prods.get(toode);
	     
	     Node toote_ean = doc.createElement("EAN");
	     Text toote_eanVal = doc.createTextNode(prod.getEAN());
	     toote_ean.appendChild(toote_eanVal);
	     ridaitem.appendChild(toote_ean);

	     
	     
	     Node btoote_kood = doc.createElement("BuyerItemCode");
	     Text btoote_koodVal = doc.createTextNode(toode);
	     btoote_kood.appendChild(btoote_koodVal);
	     ridaitem.appendChild(btoote_kood);
        

	     
	     Node toote_kood = doc.createElement("SupplierItemCode");
	     Text toote_koodVal = doc.createTextNode(toode);
	     toote_kood.appendChild(toote_koodVal);
	     ridaitem.appendChild(toote_kood);
        
	     String nimi = prod.getName();
	     if(nimi != null){
	    	 Text nimetusVal = doc.createTextNode(nimi.replace('`',' ').replace('´',' '));
		     Node nimetus = doc.createElement("ItemDescription");
		     nimetus.appendChild(nimetusVal);
		     ridaitem.appendChild(nimetus);
		     Text nimetusValz = doc.createTextNode(nimi.replace('`',' ').replace('´',' '));
		     Node nimetusz = doc.createElement("SupplierItemDescription");
		     nimetusz.appendChild(nimetusValz);
		     ridaitem.appendChild(nimetusz);
	     }

	     //<ItemType>CU</ItemType>
	     Node ItemType = doc.createElement("ItemType");
	      Text ItemTypeVla = doc.createTextNode("CU");
	        ItemType.appendChild(ItemTypeVla);
		     ridaitem.appendChild(ItemType);
	
		     

	     double kogus = line.getKogus();
	     double price = line.getPrice();
	     double disc = line.getDiscount();
	     String unit = line.getUnit();
	     
	     
	     
	     
	     
	     Node koGus = doc.createElement("OrderedQuantity");
	     
	     Text kogusVal = doc.createTextNode(getNF().format(kogus));
	     koGus.appendChild(kogusVal);
	     ridaitem.appendChild(koGus);

 
	     Node yhik = doc.createElement("UnitOfMeasure");
	     Text yhikVal = doc.createTextNode(unit);
	     yhik.appendChild(yhikVal);
	     ridaitem.appendChild(yhik);
	     Node yhikw = doc.createElement("SupplierUnitOfMeasure");
	     Text yhikValw = doc.createTextNode(unit);
	     yhikw.appendChild(yhikValw);
	     ridaitem.appendChild(yhikw);
	     
	    
	    
	    if(disc > 0 || disc == -3){
	    	 Node yhikuale_hind = doc.createElement("SupplierUnitNetPrice");
	         Text yhikuale_hindVla = doc.createTextNode(this.nf.format(price));
	         yhikuale_hind.appendChild(yhikuale_hindVla);
		     ridaitem.appendChild(yhikuale_hind);
	    }
	     
	    sum = sum + (price * kogus);
	    
	    Node yhiku_hind = doc.createElement("OrderedUnitNetPrice");
	    Text yhiku_hindVal = doc.createTextNode(this.nf.format(price));
	     yhiku_hind.appendChild(yhiku_hindVal);
	     ridaitem.appendChild(yhiku_hind);

		    if(disc > 0 || disc == -3 ){
		    

	     Node yhiku_sum = doc.createElement("OrderedNetAmount");
	      Text yhiku_sumVla = doc.createTextNode(this.nf.format(price * kogus));
		     yhiku_sum.appendChild(yhiku_sumVla);
		     ridaitem.appendChild(yhiku_sum);

	 

		     Node yhiku_vat = doc.createElement("OrderedTaxAmount");
		      Text yhiku_vatVla = doc.createTextNode(this.nf.format(price * kogus * 0.21));
			     yhiku_vat.appendChild(yhiku_vatVla);
			     ridaitem.appendChild(yhiku_vat);
			 	
		    }

			     Node yhiku_va = doc.createElement("TaxRate");
			      Text yhiku_vaVla = doc.createTextNode("21");
				     yhiku_va.appendChild(yhiku_vaVla);
				     ridaitem.appendChild(yhiku_va);

				  
					     
					     
					  
					     if(disc > 0 || disc == -3){
						     Node yhiku_vad = doc.createElement("Discount");
						      Text yhiku_vadVla = doc.createTextNode("1");
							     yhiku_vad.appendChild(yhiku_vadVla);
						     ridaitem.appendChild(yhiku_vad);
						     String hhh = disc + "";
						     if(disc == -3){
						    	 hhh = "0";
						     }
						     Node yhiku_vada = doc.createElement("DiscountVolume");
						      Text yhiku_vadaVla = doc.createTextNode(hhh);
							     yhiku_vada.appendChild(yhiku_vadaVla);
						     ridaitem.appendChild(yhiku_vada);
						
	 
					     }else{
					    	 Node yhiku_vad = doc.createElement("Discount");
						      Text yhiku_vadVla = doc.createTextNode("0");
							     yhiku_vad.appendChild(yhiku_vadVla);
						     ridaitem.appendChild(yhiku_vad);
					     }
							    	     
		
	      
	
	     
String gg = "0";
	     
	     Node sample = doc.createElement("Sample");
	     Text sampleVal = doc.createTextNode(gg);
	     sample.appendChild(sampleVal);
	     ridaitem.appendChild(sample);
	     
	   if(line.getPartii() != null && line.getPartii().trim().length() > 0 ){
 		    Node toote_comm = doc.createElement("LineComment");
 		  
 		    Text toote_commVal = doc.createTextNode(line.getPartii().trim());
 		    toote_comm.appendChild(toote_commVal);
 		    ridaitem.appendChild(toote_comm);
 		    
 		   if(comment == null){
		    	comment = "";
		    }
 		  comment = comment + " \n\nManager : "  + getAgetCode(order.getAgent()) +"\nProduct name : " + nimi + "\nRequested price : " +line.getPrice() 
		    + " EUR\nRequested Discount : " + line.getDiscount() + "%\nOrdered amount : " +
		    + line.getKogus() + "\nComment : " +  line.getPartii().trim() ;
		}
  
	     
	     
	     
	     rida.appendChild(ridaitem);
	     addNumLine(); 
	   }
   
   
   public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}
  
   
	   private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

	   public TreeMap getProducts() {
	      

		TreeMap products = new TreeMap();

		try {
	   
	                  
	       String  cmd = "    SELECT t.kod as toode , t.pavad as nimetus , ipak as tykke,KAINA1 as  myygihind, vnt as  yhik,g.id_pgr AS grupp, '' AS grupp2 ,bk.bkod   \r\n" + 
	       		"	       		    AS ean,t.pvm as vat , t.kiekis as laoseis   FROM prk t INNER JOIN pgr g ON g.grupe=t.grupe and gr_type=1\r\n" + 
	       		"	       			INNER join RR_PRKKN h ON h. ID_PRK = t.ID_PRK and h.ID_KN = 115 and kaina1 != 0 left join BK bk on bk.id_prk=t.id_prk \r\n" + 
	       		"	       		       					where kod like '0000%' and  ID_snd = 2948   ORDER BY t.kod "; //and  g.grupe like '%pda%'
	       

	//color 8388608
	       Connection con = getNewConnection();
 	        Statement stmt = con.createStatement();
 	        
	                      ResultSet rs = stmt.executeQuery(cmd);

	                      while (rs.next()) {
	                          String kood = rs.getString("toode").trim();
	                          
	                          Product prod = new Product(kood);
	                          
	                          
	                        String nimetus =rs.getString("nimetus");
	                          prod.setName(nimetus.trim());
	                          prod.setPrice(rs.getDouble("myygihind"));
	                      
	                           String grupp = rs.getString("grupp");
	                          if(grupp != null && grupp.trim().length()>0){
	                          prod.setGroup(grupp.trim());}
	                          
	                          String grupp2 = rs.getString("grupp2");
	                          if(grupp2 != null && grupp2.trim().length()>0){
	                          prod.setGroupSecond(grupp2.trim());}

	                          
	                          prod.setInCases(rs.getDouble("tykke"));
	                          String ean = rs.getString("ean");
	                          if (ean != null && ean.trim().length()>0){
	                            prod.setEAN(ean.trim());
	                          }

	                           String vat = rs.getString("vat");
	                           if (vat != null && vat.trim().length()>0){
	                        	   prod.setVat(vat);
	                           }
	                           
	                           double stock = rs.getDouble("laoseis");
	                           if(stock>0){
	                        	   prod.setStock(stock);
	                           }
	                         
	                          
	                          
	                          products.put(prod.getID(), prod);

	                      }
	      rs.close();  
	      stmt.close();
	      con.close();
	                    

	        } catch (Exception e) {
	          
	        }
	        return products;
	    }
 
  public String getDriverVersion(){
    return "AgnumRinkuImport";
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


  private String url = "jdbc:odbc:rinku";
  
  private ImportPanel parent;
  private boolean isAmount = false;
  private StringBuffer troubles= new StringBuffer();

  public String getTrouble() {
  	// TODO Auto-generated method stub
  	return troubles.toString();
  }
  
}
