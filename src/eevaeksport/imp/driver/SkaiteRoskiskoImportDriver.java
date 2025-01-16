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

public class SkaiteRoskiskoImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());
    boolean testing = false;
  
  public SkaiteRoskiskoImportDriver(ImportPanel parent) {
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

  private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
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
  
  public static boolean isToday(long lll){
  	Calendar cc = Calendar.getInstance();
  	cc.set(Calendar.HOUR_OF_DAY, 1);
  	if(cc.getTime().getTime() < lll){
  		return true;
  	}else{
  		return false ;	
  	}   	
  }
  
  
  private String getProdNimi(String toode){
	  
	  String sql =  " SELECT  t.pavad FROM prekes t where t.Kodas = '" + toode + "'" ;
	  	   System.out.println(sql);
	  	   String res = "<No Name>";
	  	   try {
	  		   Connection con = getNewConnection();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs = stmt.executeQuery(sql);
	  	        
	  	        if(rs.next()){
	  	        	res = rs.getString("pavad").trim();
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
	
  
  
  public void makeOrder(Order order) throws SQLException{
	  
	  if (signatures == null){
   	   signatures = new Vector();
      }
	  
	  String signature = order.getSignature();
	
	  System.out.println("signature" + signatures.contains(signature) + "  "+ signature );
		
	  if(signatures.contains(signature)){
		  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + Converter.deCode(order.getCompany()) ;
	  }else{
		
	  
	//  Vector splitted =  splitOrder(order);
 	  //Enumeration en = splitted.elements();
 	  //while(en.hasMoreElements()){
 		//  Order ord = (Order)en.nextElement();
 			  makeOutput(order);  
	//}
 	 signatures.add(signature);
	  }
 	    
      try {
		saveObject(signatures, "signatures.vec");
	} catch (IOException e) {
		e.printStackTrace();
	}
  }
  
    
  
  HashSet twoDays = null;
  

  private HashSet getTwodays(){
	   HashSet res = new HashSet();
	  String sql =  " SELECT   part.kodas as kood FROM dbo.Partneriai AS part INNER JOIN dbo.PL_PARTNERIAI AS pl ON part.PartneriaiId = pl.PARTNERIAIID "
	  		+ "where MES_ISORINIS_VEZID  > 1 " ;
	  	   System.out.println(sql);
	  	   try {
	  		   Connection con = getNewConnection();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs = stmt.executeQuery(sql);
	  	        
	  	        while(rs.next()){
	  	        	res.add(rs.getString("kood").trim());
	  	        }
	  	        
	  	        rs.close();
	  	        stmt.close();
	  	        con.close();
	  	        
	  	   }catch(Exception e){
	  		 e.printStackTrace();  
	  	   }
	  	//   System.out.println("Twodays len " + res.size());
     return res;
  }
   
  
  private void makeOutput(Order order){
	  
	  if(twoDays == null){
		  twoDays = this.getTwodays();
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
	       
	        
	        
	        try{

		        Node un4 = doc.createElement("DateCreated");
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
		        
		        String dateString = cal.get(Calendar.YEAR) +"-"+ month +"-"+ day + "[" + hour + ":" + minute + ":" + second + "]";
		        
		        Text unVal4 = doc.createTextNode(dateString);
		        un4.appendChild(unVal4);
		        docheader.appendChild(un4);
		        
	        }catch(Exception e){
	        	
	        }
	       
	        String add = (Calendar.getInstance().getTimeInMillis()+"").substring(11);
	        String number = order.getDokNr() + add;
	        if(number == null || number.length() == 0){
	        	number = (Calendar.getInstance().getTimeInMillis()+"").substring(5);
	        }
	        
	        
     long uni = Calendar.getInstance().getTimeInMillis();
	        
	        
	        Node un2 = doc.createElement("DocumentID");
	        Text unVal2 = doc.createTextNode(number);
	        un2.appendChild(unVal2);
	        docheader.appendChild(un2);

	        Node un21 = doc.createElement("DocumentType");
	        Text unVal21 = doc.createTextNode("ORDER");
	        un21.appendChild(unVal21);
	        docheader.appendChild(un21);
		
	        
     Node header = doc.createElement("Order-Header");
	        
	        
	        Node nr = doc.createElement("OrderNumber");
	        Text nrVal = doc.createTextNode(number + "");
	        nr.appendChild(nrVal);
	        header.appendChild(nr);
	        
	        Node un3 = doc.createElement("OrderCurrency");
	        Text unVal3 = doc.createTextNode("EUR");
	        un3.appendChild(unVal3);
	        header.appendChild(un3);
	    
	        
	        Node age = doc.createElement("SalesAgent");
	        Text ageVal = doc.createTextNode(order.getAgent());
	        age.appendChild(ageVal);
	      //  header.appendChild(age);
	 
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
		   //  header.appendChild(yhiku_hind);
	        
	        
	        
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


	        
	        Node delkuupaev = doc.createElement("ExpectedDeliveryDate");
	        Calendar delcal = Calendar.getInstance();
	        delcal.setTime(order.getShipCal().getTime());
	      
	        
	        //kui on täna reede ja on pandud esmaspäevaks siis paneme homseks
	        Calendar cal2 = Calendar.getInstance();
	        if(twoDays.contains(Converter.deCode(order.getCompany()))) {
	        	 cal2.add(Calendar.DAY_OF_MONTH, 2);
	         } else {
	        	 cal2.add(Calendar.DAY_OF_MONTH, 1);
	        }
	       
	          
	          
	        if(order.isShipDateUsed()){
	        	delcal.setTimeInMillis(order.getShipCal().getTimeInMillis());
	        }else {
	        	delcal.setTimeInMillis(cal2.getTimeInMillis());
	        }
	        
	        System.out.println("DelDay " + order.isShipDateUsed());

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
	        
	        
	        
	        Node yhiku_hind2 = doc.createElement("DocumentFunctionCode");
		     Text yhiku_hindVal2 = doc.createTextNode("O");
		     yhiku_hind2.appendChild(yhiku_hindVal2);
		    header.appendChild(yhiku_hind2);

	        root.appendChild(header);


	        Node parties = doc.createElement("Order-Parties");
	        Node seller = doc.createElement("DeliveryPoint");

	        Node ise = doc.createElement("CodeBySupplier");
	        Text nimiVal = doc.createTextNode(Converter.deCode(order.getCompany()));
	        ise.appendChild(nimiVal);
	        Node ise2 = doc.createElement("ILN");
	        Text nimiVal2 = doc.createTextNode(Converter.deCode(order.getCompany()));
	        ise2.appendChild(nimiVal2);
	        
	        seller.appendChild(ise);
	        seller.appendChild(ise2);

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


	 	
	      String fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr().trim() + (uni + "").trim() +  ".xml";
	      String fileName2 = parent.getProps().getProperty("conv.dir2") + order.getDokNr().trim() + (uni + "").trim() +  ".xml";
		    
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
	     
	     
	     String toode  = Converter.deCode(line.getToode());
	    
if(line.getPartii() != null && line.getPartii().length() > 0 ){
    Node toote_kood = doc.createElement("LineComment");
    Text toote_koodVal = doc.createTextNode(line.getPartii().trim());
    toote_kood.appendChild(toote_koodVal);
    ridaitem.appendChild(toote_kood);
	
}
	     
	     Node toote_kood2 = doc.createElement("SupplierItemCode");
	     Text toote_koodVal2 = doc.createTextNode(toode);
	     toote_kood2.appendChild(toote_koodVal2);
	     ridaitem.appendChild(toote_kood2);
	     
	     
        
	     String nimi = this.getProdNimi(toode);
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
    return "SkaiteRoskiskoImport";
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


  private String    url = "jdbc:odbc:ES_Daivida";
  private ImportPanel parent;
  private boolean isAmount = false;
  private String trouble = null;

  public String getTrouble() {
  	// TODO Auto-generated method stub
  	return trouble;
  }
  
}
