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

public class NaviNematekasImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());
    boolean testing = false;
  
  public NaviNematekasImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
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
      " FROM [" + base + "$Customer] k  where k.[No_] = '" + id.trim() + "'";
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
	
	  
    String sql1 = "SELECT [No_] AS toode  FROM [" + base + "$Item] LEFT JOIN [Viï¿½iunai Baltic$Item Unit of Measure]" +
    		" vat ON (vat.[Item No_]=[No_] and vat.[Code]='' ) " +
			"  WHERE [] ='" + ean + "'  order by [Blocked] "; // and [Blocked]=0


    String sql2 = " SELECT [No_] AS toode FROM [" + base + "$Item] " +
			"  WHERE [No_] = '" + prodID + "' "; // and [Blocked]=0 

    String prod = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

  	System.out.println(sql1);
    if (rs1.next()) {
    	if(ean.trim().length()>0){
    	      prod = rs1.getString("toode").trim();	
    	}
    }
    System.out.println("peale esimest " + prod);
    
    if(prod == null || prod.trim().length() == 0  ){
    	System.out.println(sql2);
        rs1 = st1.executeQuery(sql2);
        while (rs1.next()) {

        	if(prodID != null && prodID.trim().length()>0){
                prod = rs1.getString("toode").trim();	
        	}
        }
        System.out.println("peale teist " + prod);
      }
 
    rs1.close();
    st1.close();
    
	   System.out.println("Tagastan toote:"  + prod + " prodID:" + prodID + " ean:" + ean);
    return prod;
  
}
  
  private String base = "ŽŪB Nematekas";

  private String getProdEAN(String toode , String unit ){
	  
	  String sql =  " SELECT ref.[barcode] AS ean FROM  [ŽŪB Nematekas$Item Marking] ref where "
	  		+ " Code like '%VISI PIRK%' and [Unit Measure Code]='" + unit + "'" +
	  			"	and [Item No_] ='" + toode + "'" ;
	  	   System.out.println(sql);
	  	   String res = null;
	  	   try {
	  		   Connection con = getNewConnection();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs = null;
	  	        if(unit != null && unit.trim().length() > 1){
	  	        	rs = stmt.executeQuery(sql);
	  	        	if(rs.next()){
		  	        	res = rs.getString("ean").trim();
		  	        }	
	  	        }
	  	        
	  	      System.out.println("res1 " + res+ " " +toode + " "+ unit);
	  	        if(res == null){
	  	        	
	  	        	sql =  " SELECT ref.[barcode] AS ean FROM  [ŽŪB Nematekas$Item Marking] ref where "
	  	      	  		+ " Code like '%VISI PIRK%' and Code not like '%1+1%' and [Item No_] ='" + toode + "'" ;
	  	        	
	  	        	rs = stmt.executeQuery(sql);
	  	        	 if(rs.next()){
	 	  	        	res = rs.getString("ean").trim();
	 	  	        }
	  	        }
	  	        

		  	      System.out.println("res2 " + res+ " " +toode);
	  	        if(res == null){
	  	        	String asi = toode ;
	  	        	if (asi.length() > 10 ){
	  	        		asi = asi.substring(0, 10);
	  	        	}
	  	        	
	  	        	sql =  " SELECT ref.[barcode] AS ean FROM [" + base +"$Item] t "
	  	      	  		+ " left join [" + base + "$Item Marking] ref on ref.[Item No_]=t.[No_] "
	  	      	  				+ " and ref.Code like '%VISI PIRK%'  where t.[No_] like '" + asi + "%'" ;
	  	      	  	 
	  	        	rs = stmt.executeQuery(sql);
	  	        	 if(rs.next()){
	 	  	        	res = rs.getString("ean").trim();
	 	  	        }
	  	        }
	  	        

		  	      System.out.println("res3 " + res+ " " +toode);
	  	        rs.close();
	  	        stmt.close();
	  	        con.close();
	  	        
	  	   }catch(Exception e){
	  		 e.printStackTrace();  
	  	   }
     return res;
  }

  
  
  private String getProdNimi(String toode){
	  
	  String sql =  " SELECT [Search Description] AS nimi "+  
	                " FROM [" + base +"$Item]  where [No_] ='" + toode + "'" ;
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

	  if (signatures == null){
   	   signatures = new Vector();
      }
	  
	  
	  String signature = order.getSignature();
	  if(signatures.contains(signature)){
		  this.troubles.append("REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() );
		    		  
	  }else{
		  makeOutput(order);  
		  signatures.add(signature);
		  this.troubles =  new StringBuffer();
		  
	  }
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
	        
	        Calendar caa = Calendar.getInstance();
	        String idd  = (caa.getTimeInMillis()+ "");
	        idd = idd.substring(3);
	        Node un2 = doc.createElement("DocumentID");
	        Text unVal2 = doc.createTextNode(idd);
	        un2.appendChild(unVal2);
	        docheader.appendChild(un2);
	       
	        /*
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
	        String mills = Calendar.getInstance().getTimeInMillis()+"";
	        if(number == null || number.length() == 0){
	        	number = (mills+"").substring(5);
	        }else{
	        	number = number + (mills+"").substring(11);
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
	        
			String comm = "";
	 		if(order.getInfo() != null && order.getInfo().trim().length() >0){

	 			Node com = doc.createElement("comment");
		        Text comVal = doc.createTextNode(order.getInfo());
		        com.appendChild(comVal);
		        header.appendChild(com);
		        comm = order.getInfo();
	 		}
	 		
	 	//	System.out.println("Comment " + comm + " " + order.getWarehouse());
	        
	 		if(order.getWarehouse() != null && order.getWarehouse().indexOf("S03") != -1 ){
	 			Node war = doc.createElement("Warehouse");
		        Text warVal = doc.createTextNode(order.getWarehouse());
		        war.appendChild(warVal);
		        header.appendChild(war);
	 		}
	 		
	      

	        Node kuupaev = doc.createElement("OrderDate");
	        Calendar cal = Calendar.getInstance();
	      //  cal.setTime(order.getShipCal().getTime()); // karolise erisoov 

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


	        
		        String func = "ORDER";
		       
	        Text functionVal = doc.createTextNode(func);
	        function.appendChild(functionVal);
	        header.appendChild(function);		   
	        root.appendChild(header);

	        
	        

	        Node parties = doc.createElement("Document-Parties");
	        
	        
	        Node seller2 = doc.createElement("Receiver");

	        Node ise22 = doc.createElement("CodeByBuyer");
	        Text nimiVal22 = doc.createTextNode(Converter.deCode(order.getCompany()));
	        ise22.appendChild(nimiVal22);
	        Node ise2 = doc.createElement("ILN");
	        Text nimiVal2 = doc.createTextNode(Converter.deCode(order.getCompany()));
	        ise2.appendChild(nimiVal2);
	        
	        seller2.appendChild(ise22);
	        seller2.appendChild(ise2);

	        
	        Node seller = doc.createElement("DeliveryPoint");

	        Node ise = doc.createElement("CodeBySupplier");
	        Text nimiVal = doc.createTextNode(Converter.deCode(order.getCompany()));
	        ise.appendChild(nimiVal);
	       
	        
	        seller.appendChild(ise);
	        
	        
	        parties.appendChild(seller2);
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
	        	  coun++;
	          	addLine(line, read, doc, coun, order, invoice);
	          }
	          }

	        root.appendChild(read);


	
 
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
 Text tnetVal = doc.createTextNode("0.00");
 tnet.appendChild(tnetVal);
 summary.appendChild(tnet);
 

 Node tgr = doc.createElement("TotalGrossAmount");
 Text tgrVal = doc.createTextNode(getNF().format(sum *1.21) );
 tgr.appendChild(tgrVal);
 //summary.appendChild(tgr);
 
 Node ta = doc.createElement("TotalTaxAmount");
 Text taVal = doc.createTextNode(getNF().format(sum *0.21) );
 ta.appendChild(taVal);
// summary.appendChild(ta);
 
//<TotalGrossAmount>52.274</TotalGrossAmount>
// <TotalTaxAmount>7.974</TotalTaxAmount>
	 	
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


   
   
   
   private void addLine(OrderLine line, Node read, Document doc,int counter ,Order order, boolean invoice) throws SQLException{
	   

	
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
	     
	     
	     String toode  = Converter.deCode(line.getToode());
	   
	   try{
	    	toode = getProdEAN(toode , line.selActCode);
		}catch(Exception e){
	    	e.printStackTrace();
	    }
	     
	     if(toode == null){
	    	 toode = Converter.deCode(line.getToode());
	     }

	     Node tooteS_kood = doc.createElement("SupplierItemCode");
	     Text tooteS_koodVal = doc.createTextNode(toode);
	     tooteS_kood.appendChild(tooteS_koodVal);
	     ridaitem.appendChild(tooteS_kood);
	     
	     toode  = Converter.deCode(line.getToode());
	     Node toote_kood = doc.createElement("BuyerItemCode");
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

	    

	     double kogus = line.getKogus();
	     double price = line.getPrice();
	     double disc = line.getDiscount();
	     String unit = line.getUnit();
	     
	     Node koGus = doc.createElement("OrderedQuantity");
	     
	     Text kogusVal = doc.createTextNode(getNF().format(kogus));
	     koGus.appendChild(kogusVal);
	     ridaitem.appendChild(koGus);


	     
	   
	     
	    
	    
	    if(disc > 0 || invoice){
	    	
	     Node yhiku_hind = doc.createElement("OrderedUnitNetPrice");
	    	//price = price * (100-disc)/100;
	        Text yhiku_hindVal = doc.createTextNode(this.nf.format(price));
		     yhiku_hind.appendChild(yhiku_hindVal);
		     ridaitem.appendChild(yhiku_hind);
	    }
	
	     
	     Node yhiku_sum = doc.createElement("OrderedUnitPacksize");
	      Text yhiku_sumVla = doc.createTextNode("1.000");
		     yhiku_sum.appendChild(yhiku_sumVla);
		     ridaitem.appendChild(yhiku_sum);

	 

		     Node yhiku_vat = doc.createElement("SupplierUnitPacksize");
		      Text yhiku_vatVla = doc.createTextNode("1.000");
			     yhiku_vat.appendChild(yhiku_vatVla);
			     ridaitem.appendChild(yhiku_vat);


			     Node yhiku_va = doc.createElement("TaxRate");
			      Text yhiku_vaVla = doc.createTextNode("0.00");
				     yhiku_va.appendChild(yhiku_vaVla);
				     ridaitem.appendChild(yhiku_va);

				     


				     
				     if(disc > 0 && !invoice){
					     Node yhiku_vad = doc.createElement("Discount");
					      Text yhiku_vadVla = doc.createTextNode(this.nf.format(disc));
						     yhiku_vad.appendChild(yhiku_vadVla);
						//     ridaitem.appendChild(yhiku_vad);
		    		    	 
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
    return "NaviNematekasImport";
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
