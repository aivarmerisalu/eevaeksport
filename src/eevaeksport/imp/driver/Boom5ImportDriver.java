
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
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

import eevaeksport.Email;
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

public class Boom5ImportDriver
    extends AbstractImportDriver {


	  static Category log = Category.getRoot();

  public Boom5ImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

    try {
      Class.forName(databaseDriverName).newInstance();
      con =  DriverManager.getConnection(url, "SYSDBA", "kuu2pool");
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    } catch (Exception e) {
      parent.showError(e);
    }
    
    loadSignatures();
    
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
  

  public String identifyCustomer(String id,String regnr) throws SQLException {
    return id;
  }

 


 
  public String identifyProduct(String ean, String prodID) throws SQLException{
    return null;
  }


  
  public void makeOrder(Order order) throws SQLException{

	   if (signatures == null){
    	   signatures = new Vector();
       }
	  
	   String signature = order.getSignature();
	   
	   
	   
		  if(signatures.contains(signature)){
			  this.trouble = "REPRETEATING ORDER agent :" + order.getAgent() + " customer :" 
		  + Converter.deCode(order.getCompany()) ;
			  log.error(trouble);
		  }else{
			  
	      			//System.out.println("XXXXX salvesta " + ord.getStatus() + " " + salvesta);
	      		  log.error("sisenen tellimust tegeama " + order.getCompany() + order.getOrderLines().size());
	      		    
	      			this.trouble = null;
	  			  counter = 0;
	  			  makeOutput(order);
	  			  log.error("made Output 1 " + order.getCompany() + " " + fileName);


			  signatures.add(signature);
		  }
		  
		   setNumAddedLines(counter); 
		   System.out.println("counter " + counter  + " " + this.getNumAddedLines());
			
		   try {
			saveObject(signatures, "signatures.vec");
			} catch (IOException e) {
			e.printStackTrace();
			} 
			
		      
		  }

  



public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
 
private HashMap eans = null;
  
  private String getEan(String aa) throws SQLException{
	 
	  	
	  	if(eans == null){
	  		eans = new HashMap();
	  	
	 	 String	 sql2 = " SELECT  t.artno AS toode, trim(t.artname) || ' ' || trim(t.artcode)   AS nimetus,u.salesprice AS myygihind ,u.unitname AS unit, " +
		 " t.info AS tykke,b.barcode AS ean,g.metagroupno||''||g.groupno AS grupp2,g.metagroupno AS grupp ,l.volume AS stock " +
		 " FROM artmain t,unit u ,barart b,artgrp g,fifo l WHERE t.artno=u.artno AND t.artno=b.artno AND t.groupno=g.groupno " +
		 " AND l.stockno = 5 AND t.artno=l.artno AND t.passive='F' ORDER BY t.artname ,b.isdefault";
				
				Statement st1 = con.createStatement();
				ResultSet rs1 = st1.executeQuery(sql2);
				
				 while (rs1.next()) {
					 String id = rs1.getString("toode").trim();
					 String joon = rs1.getString("ean");
					 if(joon != null && joon.trim().length() > 0){
						 eans.put(id,joon);
					 }
				    
				 }
				
		rs1.close();
		st1.close();
		
	  	}
	  	
	  	System.out.println("oioi " + eans.size() + " " + eans.get(aa)+"");
	  	
	  	return eans.get(aa)+"" ;
	  }

  
private HashMap names = null;
private HashMap cnames = null;  

private String getCustName(String aa) throws SQLException{
	 
  	
  	if(cnames == null){
  		cnames = new HashMap();
  	
  		String cmd = "SELECT custcode AS kood, custname AS nimi, address AS aadress,  " +
  				"   sellerno AS agent  , CONTACTPERSON as info ,  PHONE , EMAIL , PAYPERIOD as term ,ownerno as payer "
  				+ " FROM cumain WHERE (sellerno)>0  AND passive='F' AND  typeno=1 ORDER BY custname ";
			
			Statement st1 = con.createStatement();
			ResultSet rs1 = st1.executeQuery(cmd);
			
			 while (rs1.next()) {
				 String id = rs1.getString("kood").trim();
				 String joon = rs1.getString("nimi");
				 if(joon != null && joon.trim().length() > 0){
					 cnames.put(id,joon.trim());
				 }
			    
			 }
			
	rs1.close();
	st1.close();
	
  	}
  	
  	System.out.println("oioi " + cnames.size() + " " + cnames.get(aa)+"");
  	
  	return cnames.get(aa)+"" ;
  }

  private String getProdName(String aa) throws SQLException{
	 
	  	
	  	if(names == null){
	  		names = new HashMap();
	  	
	 	 String	 sql2 = " SELECT  t.artno AS toode, trim(t.artname)  AS nimetus,u.salesprice AS myygihind ,u.unitname AS unit, " +
		 " t.info AS tykke,b.barcode AS ean,g.metagroupno||''||g.groupno AS grupp2,g.metagroupno AS grupp ,l.volume AS stock " +
		 " FROM artmain t,unit u ,barart b,artgrp g,fifo l WHERE t.artno=u.artno AND t.artno=b.artno AND t.groupno=g.groupno " +
		 " AND l.stockno = 5 AND t.artno=l.artno AND t.passive='F' ORDER BY t.artname ,b.isdefault";
				
				Statement st1 = con.createStatement();
				ResultSet rs1 = st1.executeQuery(sql2);
				
				 while (rs1.next()) {
					 String id = rs1.getString("toode").trim();
					 String joon = rs1.getString("nimetus");
					 if(joon != null && joon.trim().length() > 0){
						 names.put(id,joon);
					 }
				    
				 }
				
		rs1.close();
		st1.close();
		
	  	}
	  	
	  	System.out.println("oioi " + names.size() + " " + names.get(aa)+"");
	  	
	  	return names.get(aa)+"" ;
	  }

  

  
  
 private void makeOutput(Order order){
	  
	  String comm = "";
	  String tarne = "";
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
	       
	        
	        Node un3 = doc.createElement("Description");
	        Text unVal3 = doc.createTextNode(order.getWarehouse());
	        un3.appendChild(unVal3);
	        docheader.appendChild(un3);*/
	        try{

		        long ll = Long.parseLong(order.getAgentCode());
		        Node un4 = doc.createElement("DateCreated");
		        
		        Calendar cal = Calendar.getInstance();
		        cal.setTime(new java.util.Date(ll));

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
		        
		        String dateString = cal.get(Calendar.YEAR) +"-"+ month +"-"+ day + "[" + hour + ":" + minute + ":" + second + "]";
		        
		        Text unVal4 = doc.createTextNode(dateString);
		        un4.appendChild(unVal4);
		        docheader.appendChild(un4);
		        
	        }catch(Exception e){
	        	
	        }
	        
		

	        String number = order.getDokNr();
	        if(number == null || number.length() == 0){
	        	number = (Calendar.getInstance().getTimeInMillis()+"").substring(5);
	        }
	        
	        
	        
     long uni = Calendar.getInstance().getTimeInMillis();
     Node header = doc.createElement("Order-Header");
	        
	        
	        Node nr = doc.createElement("OrderNumber");
	        Text nrVal = doc.createTextNode(number + "");
	        nr.appendChild(nrVal);
	        header.appendChild(nr);
	        
	        Node age = doc.createElement("SalesAgent");
	        Text ageVal = doc.createTextNode(order.getAgent());
	        age.appendChild(ageVal);
	        header.appendChild(age);


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
	        
	        
	      Node func = doc.createElement("DocumentFunctionCode");
	        Text funcVal = doc.createTextNode("9");
	        func.appendChild(funcVal);
	        header.appendChild(func);
	        
	        
   
		  Node curr = doc.createElement("OrderCurrency");
		   Text currVal = doc.createTextNode("EUR");
		   curr.appendChild(currVal);
		   header.appendChild(curr);
		   
			        
	        Node delkuupaev = doc.createElement("ExpectedDeliveryDate");
	        Calendar delcal = Calendar.getInstance();
	        delcal.setTime(order.getShipCal().getTime());

	         month = (delcal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	         day = delcal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;
   year = delcal.get(Calendar.YEAR) + "";
  if(year.length() == 2){
	  year = "20" + year;
  }
	         dateString = year +"-"+ month +"-"+ day;
	         tarne = dateString;
	        Text delkuupaevVal = doc.createTextNode(dateString);
	        delkuupaev.appendChild(delkuupaevVal);
	        header.appendChild(delkuupaev);	
	        
	    
	 
	        String type = "order";
	        // kuidas edastada kingitus 
		     if("gift".equals(order.getDealer())){
		    	 type = "degu";
		     }
		     if("100".equals(order.getDealer())){
		    	 type = "zero";
		     }
	        
		     Node yhiku_hind = doc.createElement("DocType");
		     Text yhiku_hindVal = doc.createTextNode(type);
		     yhiku_hind.appendChild(yhiku_hindVal);
		     header.appendChild(yhiku_hind);
	        
	        
	        
			
	 		if(order.getInfo() != null && order.getInfo().trim().length() >0){

	 			Node com = doc.createElement("comment");
		        Text comVal = doc.createTextNode(order.getInfo());
		        com.appendChild(comVal);
		        header.appendChild(com);
		        comm = order.getInfo();
	 		}
	 		System.out.println("Comment " + comm);

	        

	        root.appendChild(header);


	        Node parties = doc.createElement("Document-Parties");
	        Node seller = doc.createElement("Receiver");
	        Node ise = doc.createElement("CodeByBuyer");
	        Text nimiVal = doc.createTextNode("gilden");
	        ise.appendChild(nimiVal);
	        Node ise2 = doc.createElement("ILN");
	        Text nimiVal2 = doc.createTextNode("gilden");
	        ise2.appendChild(nimiVal2);
	        seller.appendChild(ise);
	        seller.appendChild(ise2);
            parties.appendChild(seller);
            
            Node sender = doc.createElement("Sender");
	        Node iseS = doc.createElement("UtilizationRegisterNumber");
	        Text nimiValS = doc.createTextNode(order.getCompany());
	        iseS.appendChild(nimiValS);
	        Node ise2S = doc.createElement("ILN");
	        Text nimiVal2S = doc.createTextNode("");
	        ise2S.appendChild(nimiVal2S);
	        sender.appendChild(iseS);
	        sender.appendChild(ise2S);
            parties.appendChild(sender);
            
	        root.appendChild(parties);
            
	        
            
            
            Node parties2 = doc.createElement("Order-Parties");
	        
            
            
            Node buyer = doc.createElement("Buyer");
	        Node iseB = doc.createElement("UtilizationRegisterNumber");
	        Text nimiValB = doc.createTextNode(order.getCompany());
	        iseB.appendChild(nimiValB);
	        Node ise2B = doc.createElement("ILN");
	        Text nimiVal2B = doc.createTextNode("");
	        ise2B.appendChild(nimiVal2B);
	        buyer.appendChild(iseB);
	        buyer.appendChild(ise2B);
            parties2.appendChild(buyer);
            
            
            Node seller3 = doc.createElement("Seller");
	        Node ise3 = doc.createElement("CodeByBuyer");
	        Text nimiVal3 = doc.createTextNode("Gilden");
	        ise3.appendChild(nimiVal3);
	        Node ise23 = doc.createElement("ILN");
	        Text nimiVal23 = doc.createTextNode("Gilden");
	        ise23.appendChild(nimiVal23);
	        seller3.appendChild(ise3);
	        seller3.appendChild(ise23);
            parties2.appendChild(seller3);
            
            Node delivery = doc.createElement("DeliveryPoint");
	        Node iseD = doc.createElement("UtilizationRegisterNumber");
	        Text nimiValD = doc.createTextNode(order.getCompany());
	        iseD.appendChild(nimiValD);
	        Node ise2D = doc.createElement("ILN");
	        Text nimiVal2D = doc.createTextNode("");
	        ise2D.appendChild(nimiVal2D);
	        delivery.appendChild(iseD);
	        delivery.appendChild(ise2D);
            parties2.appendChild(delivery);
            
            
	        root.appendChild(parties2);
	        
	 



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
            counter = coun ;
	        root.appendChild(read);

	        
	        
	        Node summary = doc.createElement("Order-Summary");
	       
	        Node sumO = doc.createElement("TotalLines");
	        Text sumValO = doc.createTextNode(coun + "");
	        sumO.appendChild(sumValO);
	        
	        Node sumO2 = doc.createElement("TotalOrderedAmount");
	        Text sumValO2 = doc.createTextNode(order.getCalcSum() + "");
	        sumO2.appendChild(sumValO2);
	        
	        summary.appendChild(sumO);
	        summary.appendChild(sumO2);
            
            root.appendChild(summary);
	        

	 	
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
	      
	      
	   
	        
	    	    sendCommentEmail( order , comm , tarne,fileName);
	         
	 }

 
 
 private void sendCommentEmail(Order order, String comment , String tarne, String filename){
	   	try{
	   		
	   		
	   		String sisu = "";
	   		
	   		
	   	  Iterator iter = order.getOrderLines().iterator();
	        while (iter.hasNext()){
	        	   OrderLine line = (OrderLine)iter.next();
	 	         
	 	          sisu = sisu + "[ " + line.getKogus() + " ] " + this.getEan(line.getToode()) 
	 	          + " " + this.getProdName(line.getToode()) + "\n";
	        }
	       
	   		if(comment != null && comment.length() > 0){
	   			comment = "Kommentaar :" + comment + "\n";
	   		}
	   		if(tarne != null && tarne.length() > 0){
	   			tarne = "Tarnekuupäev :" + tarne + "\n";
	   		}
	   		
	   			
	   			Email mail = new Email(null);
		  		   
	   			
		  	  		mail.setSMTP(parent.getProps().getProperty("smtphost"));
		  	  		mail.setFilesTo(parent.getProps().getProperty("operatoremail"));
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		mail.setSubjectFilesTo("Tellimus esindajalt  " + order.getAgent());
		  	  		mail.setEncoding("utf-8");
		  	  		mail.addAttach(filename);
		  	  		mail.setBody("Kliendikood :" + order.getCompany()
		  	  				+  "\nKliendi nimi :" + getCustName(order.getCompany()) + "\n" +
		  	  				 comment +
		  	  				 tarne+
		  	  			     "\n" + sisu );
		  	  		mail.setSendNotice(false);
		  	  		mail.send();
		  	  		
		  	
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	   }


   
   
   
   private void addLine(OrderLine line, Node read, Document doc,int counte ,Order order) throws SQLException{
	   

	
	     Node rida = doc.createElement("Line");
	     read.appendChild(rida);

	     Node ridaitem = doc.createElement("Line-Item");


	     Node linenum = doc.createElement("LineNumber");
	     Text linnum = doc.createTextNode(counte+"");
	     linenum.appendChild(linnum);
	     ridaitem.appendChild(linenum);

     String ean = getEan(line.getToode());
     if(ean != null){
    	  Node toote_ean = doc.createElement("EAN");
 	     Text toote_eanVal = doc.createTextNode(ean);
 	     toote_ean.appendChild(toote_eanVal);
 	     ridaitem.appendChild(toote_ean); 
     }
	   

	     
	     
	     String toode  = line.getToode();
	    
	     
	     
	     Node toote_kood = doc.createElement("SellerItemCode");
	     Text toote_koodVal = doc.createTextNode(toode);
	     toote_kood.appendChild(toote_koodVal);
	     ridaitem.appendChild(toote_kood);
        
	     String nimi = this.getProdName(toode);
	     if(nimi != null){
	    	 Text nimetusVal = doc.createTextNode(nimi.replace('`',' ').replace('�',' '));
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

	     
	     

	    
	     
	    Node yhiku_hind = doc.createElement("OrderedUnitNetPrice");
	     
	    
	    
	    if(disc > 0 ){
	    	//price = price * (100-disc)/100;
	    }
	     Text yhiku_hindVal = doc.createTextNode(this.nf.format(price));
	     yhiku_hind.appendChild(yhiku_hindVal);
	     ridaitem.appendChild(yhiku_hind);
	     
	     Node yhiku_sum = doc.createElement("OrderedNetAmount");
	      Text yhiku_sumVla = doc.createTextNode(this.nf.format(price * kogus));
		     yhiku_sum.appendChild(yhiku_sumVla);
		     ridaitem.appendChild(yhiku_sum);

	 

		     Node yhiku_vat = doc.createElement("OrderedTaxAmount");
		      Text yhiku_vatVla = doc.createTextNode(this.nf.format(price * kogus * 0.2));
			     yhiku_vat.appendChild(yhiku_vatVla);
			     ridaitem.appendChild(yhiku_vat);


			     Node yhiku_va = doc.createElement("TaxRate");
			      Text yhiku_vaVla = doc.createTextNode("20");
				     yhiku_va.appendChild(yhiku_vaVla);
				     ridaitem.appendChild(yhiku_va);

				     Node yhiku_vad = doc.createElement("Discount");
				      Text yhiku_vadVla = doc.createTextNode("0.0000");
					     yhiku_vad.appendChild(yhiku_vadVla);
					     ridaitem.appendChild(yhiku_vad);
	    
	     
	     
	     
	     
	     Node yhik = doc.createElement("UnitOfMeasure");
	     Text yhikVal = doc.createTextNode(unit);
	     yhik.appendChild(yhikVal);
	     ridaitem.appendChild(yhik);

	     rida.appendChild(ridaitem);
	
	   }

 
 
  private  String fileName = null;
 
 
 
  public NumberFormat getNF() {
		return this.nf;
	}
	
 
  public String getDriverVersion(){
    return "Boom5Import";
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

  
  private String url = "jdbc:odbc:boom";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private int counter = 0;
  private ImportPanel parent;
  private String trouble = null;


public String getTrouble() {
	return trouble;
}
  


}
