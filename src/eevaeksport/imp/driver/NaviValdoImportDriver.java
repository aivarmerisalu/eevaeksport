package eevaeksport.imp.driver;




import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashSet;
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

public class NaviValdoImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());
    boolean testing = false;
  
  public NaviValdoImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    if("true".equals( parent.getProps().getProperty("testing"))){
    	testing = true;
    }
  }
  
 
  
  
  private TreeMap names = null;
  private TreeMap custwares = null;

  private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));
	  }
  


  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
  
  
  
  public String identifyCustomer(String id,String regnr)  {

    String acc = null;
    	
    	try{
    String sql2 = " select k.[No_] AS klient " +
      " FROM [VOLDEMĀRS SIA$Customer] k  where k.[No_] = '" + id.trim() + "'";
    System.out.println(sql2);

    
    Statement st2 = con.createStatement();
    ResultSet rs2 = st2.executeQuery(sql2);

    while (rs2.next()) {
      acc = rs2.getString("klient").trim();

    }
    rs2.close();
    st2.close();

  	}catch(Exception e){
  		e.printStackTrace();
  	}
  	
 	
     
 	if(acc == null){
  		acc = id;
  	}
    
    System.out.println("Klient: " + acc);
    return acc;

  }

  
  public String identifyProduct(String ean, String prodID) throws SQLException{
	
  return prodID;
  
}
  

  
  private String getProdNimi(String toode){
	  
	  String sql =  " SELECT [Search Description] AS nimi "+  
	                " FROM [VOLDEMĀRS SIA$Item]  where [No_] ='" + toode + "'" ;
	  	   System.out.println(sql);
	  	   String res = "<No Name>";
	  	   try {
	  		   Connection con = getNewConnection();
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


  
  public void makeOrder(Order order) throws SQLException{

     makeOutput(order); 	    
    
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
	
	  
	 	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    
	      try {
	    	
	    	  
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.newDocument();
	        Element root = doc.createElement("Document-Order");
	        doc.appendChild(root);

double kordaja = 1;

	        boolean tagastus = false;
	   	 	 Vector lin = order.getOrderLines();
	   	 	 HashSet signas = new HashSet();
	   	 	 int nega = 0;
	   	 	 for (int i = 0; i < lin.size(); i++) {
	   	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	   	 		if(!signas.contains(ol.getToode() + " " + ol.getKogus())) {
	   	 			signas.add(ol.getToode() + " " + ol.getKogus());
	   	 		}
	   	 		if(ol.getKogus() < 0 && (order.getStatus() == 5 || order.getStatus() == 4 || order.getStatus() == 2)){
	   	 			nega++;
	   	 		}
	   	 	 }  
	   	
	   	 	
	   	 	 
	   	 	 if(nega == lin.size()){
	   		    	tagastus = true;
	   		    	kordaja =-1;
	   		    }
	   	 	
	   	 	 
	   	 	  boolean repr = false;
	   	 	 	 for (int i = 0; i < lin.size(); i++) {
	   	 	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	   	 	 		if(ol.getKogus() < 0 && ol.getDiscount() > 0.1 && (order.getStatus() == 5 || order.getStatus() == 4)){
	   	 	 			repr = true;
	   	 	 			tagastus = false;
	   	 	 		    kordaja =-1;
	   	 	 		}
	   	 
	   	 	 		
	   	 	 	 } 
	        
	        
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
	        	number = (Calendar.getInstance().getTimeInMillis()+"").substring(3);
	        }
	        
	        
	        
     long uni = Calendar.getInstance().getTimeInMillis();
     Node header = doc.createElement("Order-Header");
	        
	        
	        Node nr = doc.createElement("OrderNumber");
	        if(number.length() > 20) {
	        	number = number.substring(0,20);
	        }
	        Text nrVal = doc.createTextNode(number + "");
	        nr.appendChild(nrVal);
	        header.appendChild(nr);
	        
	        Node age = doc.createElement("SalesAgent");
	        Text ageVal = doc.createTextNode(order.getAgent());
	        age.appendChild(ageVal);
	        header.appendChild(age);
	        
			String comm = "";
	 		if(order.getInfo() != null && order.getInfo().trim().length() >0){

	 			Node com = doc.createElement("comment");
		        Text comVal = doc.createTextNode(order.getInfo());
		        com.appendChild(comVal);
		        header.appendChild(com);
		        comm = order.getInfo();
	 		}
	 		System.out.println("Comment " + comm + " " + order.getTeostaja() + " " + order.getShipMode());
	        
	 		if(order.getTeostaja() != null && order.getTeostaja().trim().length() >0 &&
	 				!order.getTeostaja().trim().startsWith("VLD")){

	 			Node coms = doc.createElement("CustomerOrderNumber");
	 			String num = order.getTeostaja();
	 			 if(num.length() > 20) {
	 	        	num = num.substring(0,20);
	 	        }
		        Text comVals = doc.createTextNode(num);
		        coms.appendChild(comVals);
		        header.appendChild(coms);
		   
	 		}

	 		if(order.getShipMode() != null && order.getShipMode().trim().length() >0){

	 			Node coms = doc.createElement("ShipMode");
		        Text comVals = doc.createTextNode(order.getShipMode());
		        coms.appendChild(comVals);
		        header.appendChild(coms);
		   
	 		}
	 		
	       

	        Node kuupaev = doc.createElement("OrderDate");
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(order.getShipCal().getTime()); // karolise erisoov 

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
	        Text delkuupaevVal = doc.createTextNode(dateString);
	        delkuupaev.appendChild(delkuupaevVal);
	        header.appendChild(delkuupaev);		        

	        boolean invoice = false;
	        Node function  = doc.createElement("DocumentFunctionCode");
	       
	        
	        System.out.println("Seisund " + repr + " " + tagastus );
	        
		     String func = "ORDER";
		      if(repr) {
		    	  func = "REPRICE";
		      }
		      
		      if(tagastus) {
		    	  func = "RETURN";
		      }
		        
	        Text functionVal = doc.createTextNode(func);
	        function.appendChild(functionVal);
	        header.appendChild(function);		   
	        root.appendChild(header);

	        String cust = order.getCompany();
	        String aadr = cust.substring(cust.indexOf("_")+1);
	        cust = cust.substring(0,cust.indexOf("_"));
	        

	        Node parties = doc.createElement("Document-Parties");
	        Node seller = doc.createElement("Receiver");

	        Node ise = doc.createElement("CodeByBuyer");
	        Text nimiVal = doc.createTextNode(Converter.deCode(cust));
	        ise.appendChild(nimiVal);
	        
	        Node isea = doc.createElement("ShipToAddress");
	        Text nimiVala = doc.createTextNode(Converter.deCode(aadr));
	        isea.appendChild(nimiVala);
	        
	        Node ise2 = doc.createElement("ILN");
	        Text nimiVal2 = doc.createTextNode(Converter.deCode(order.getCompany()));
	        ise2.appendChild(nimiVal2);
	        
	        seller.appendChild(ise);
	        seller.appendChild(isea);
	        seller.appendChild(ise2);

	        parties.appendChild(seller);
	        root.appendChild(parties);



	        //RIDADE LISAMINE
	        Node read = doc.createElement("Order-Lines");

	        int coun = 0;
            double kog = 0;
            double sum = 0;

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
	          	
	        	  kog = kog + line.getKogus();
	        	  sum = sum + (line.getKogus()* line.getPrice());
	        	  System.out.println("rida " + line.getToode() + " " + line.getKogus() + " " + line.getPrice() + " " + line.getKogus()* line.getPrice() + " " + sum);
	        	 
	        	  if(signas.contains(toode+ " " + line.getKogus())) {
	        		  coun++;
	        		  addLine(line, read, doc, coun, order, repr, kordaja); 
	        		  addNumLine(); 
	        	  }
	          	  signas.remove(toode+ " " + line.getKogus()); // topelt ridade vastu tehtud ...
	          }
	          }

	        root.appendChild(read);


if(invoice){
	
 
 Node summary = doc.createElement("Order-Summary");
 root.appendChild(summary);
	        
 
 Node tlines = doc.createElement("TotalLines");
 Text tlinesVal = doc.createTextNode(coun + "");
 tlines.appendChild(tlinesVal);
 summary.appendChild(tlines);
 
 
 Node tam = doc.createElement("TotalOrderedAmount");
 Text tamVal = doc.createTextNode(getNF().format(kog));
 tam.appendChild(tamVal);
 summary.appendChild(tam);    
 
 
 Node tnet = doc.createElement("TotalNetAmount");
 Text tnetVal = doc.createTextNode(getNF().format(sum) );
 tnet.appendChild(tnetVal);
 summary.appendChild(tnet);
 

 Node tgr = doc.createElement("TotalGrossAmount");
 Text tgrVal = doc.createTextNode(getNF().format(sum *1.21) );
 tgr.appendChild(tgrVal);
 summary.appendChild(tgr);
 
 Node ta = doc.createElement("TotalTaxAmount");
 Text taVal = doc.createTextNode(getNF().format(sum *0.21) );
 ta.appendChild(taVal);
 summary.appendChild(ta);
 
//<TotalGrossAmount>52.274</TotalGrossAmount>
// <TotalTaxAmount>7.974</TotalTaxAmount>
	        
}	        
	 	
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


   
   
   
   private void addLine(OrderLine line, Node read, Document doc,int counter ,Order order, boolean repr, double kordaja) throws SQLException{
	   

	
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
	     
	     
	     String toode  = line.getToode();
	    

	     Node toote_kood = doc.createElement("SupplierItemCode");
	     Text toote_koodVal = doc.createTextNode(toode);
	     toote_kood.appendChild(toote_koodVal);
	     ridaitem.appendChild(toote_kood);
        
	     String nimi = this.getProdNimi(toode);
	     if(nimi != null){
	    	 Text nimetusVal = doc.createTextNode(nimi.replace('`',' ').replace('´',' '));
		     Node nimetus = doc.createElement("ItemDescription");
		     nimetus.appendChild(nimetusVal);
		     ridaitem.appendChild(nimetus);
	     }

	    

	     double kogus = line.getKogus() * kordaja;
	     double price = line.getPrice();
	     double disc = line.getDiscount();
	     String unit = line.getUnit();
	     
	     
	     Node koGus = doc.createElement("OrderedQuantity");
	     
	     Text kogusVal = doc.createTextNode(getNF().format(kogus ));
	     koGus.appendChild(kogusVal);
	     ridaitem.appendChild(koGus);

	     
	     

	    
	     
	   
	     
	    
	    
	    if(disc > 0 || repr){
	    	
	     Node yhiku_hind = doc.createElement("OrderedUnitNetPrice");
	    	//price = price * (100-disc)/100;
	        Text yhiku_hindVal = doc.createTextNode(this.nf.format(price));
		     yhiku_hind.appendChild(yhiku_hindVal);
		     ridaitem.appendChild(yhiku_hind);
		     
		     if(line.getPartii() != null && line.getPartii().trim().length() >0) {
		    	    Node origDoc = doc.createElement("OriginalDocNumber");
				     Text origDocVal = doc.createTextNode(line.getPartii());
				     origDoc.appendChild(origDocVal);
				     ridaitem.appendChild(origDoc); 
		     }
	    }
	
	    if(repr){
	    	
		     Node yhiku_hind = doc.createElement("CompensationSum");
		    	double compsum = (price - (price * (100-disc)/100)) * kogus  ;
		        Text yhiku_hindVal = doc.createTextNode(this.nf.format(compsum));
			     yhiku_hind.appendChild(yhiku_hindVal);
			     ridaitem.appendChild(yhiku_hind);
		    }
		
		     
	     
	     Node yhiku_sum = doc.createElement("OrderedNetAmount");
	      Text yhiku_sumVla = doc.createTextNode(this.nf.format(price * kogus));
		     yhiku_sum.appendChild(yhiku_sumVla);
		    // ridaitem.appendChild(yhiku_sum);

	 

		     Node yhiku_vat = doc.createElement("OrderedTaxAmount");
		      Text yhiku_vatVla = doc.createTextNode(this.nf.format(price * kogus * 0.21));
			     yhiku_vat.appendChild(yhiku_vatVla);
			  //   ridaitem.appendChild(yhiku_vat);


			     Node yhiku_va = doc.createElement("TaxRate");
			      Text yhiku_vaVla = doc.createTextNode("21");
				     yhiku_va.appendChild(yhiku_vaVla);
//				     ridaitem.appendChild(yhiku_va);

				     
				     if(disc > 0 ){
					     Node yhiku_vad = doc.createElement("Discount");
					      Text yhiku_vadVla = doc.createTextNode(this.nf.format(disc));
						     yhiku_vad.appendChild(yhiku_vadVla);
						   ridaitem.appendChild(yhiku_vad);
		    		    	 
				     }
		
	     
				     
				    
				 

	     
	     
	     Node yhik = doc.createElement("UnitOfMeasure");
	     Text yhikVal = doc.createTextNode(unit);
	     yhik.appendChild(yhikVal);
	     ridaitem.appendChild(yhik);

	     rida.appendChild(ridaitem);
	   
	   }

   public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}
  


	   private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);


 
  public String getDriverVersion(){
    return "NaviIcecoImport";
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


  
  private String url = "jdbc:odbc:navi";
  private Connection con;
  private ImportPanel parent;
  private boolean isAmount = false;
  private StringBuffer troubles= new StringBuffer();

  public String getTrouble() {
  	return troubles.toString();
  }
  
}
