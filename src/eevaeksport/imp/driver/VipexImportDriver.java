package eevaeksport.imp.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
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

public class VipexImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(VipexImportDriver.class.getName());

  public VipexImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    nf.setGroupingUsed(false);
    try {
      Class.forName(databaseDriverName).newInstance();
      con2 = DriverManager.getConnection(url, "pda", "pda");
    }
    catch (Exception e) {
      parent.showError(e);
    }
    yhikutolked = getTolked();
    edastuskliendid = getKliendid();

    sekrkliendid = getKliendidSekr();
    
  }
  
  private TreeMap getTolked(){
	  TreeMap res = new TreeMap();
	  
	  try {
	
		  
			File fil = new File("tolkebyroo.txt");
	if (fil.exists()){
	       System.out.println("Seon koode ... ");
			FileReader fr = new FileReader("tolkebyroo.txt");
			BufferedReader reader = new BufferedReader(fr);
			String line = null;
			while ((line = reader.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, "=");
				while(st.hasMoreTokens()){
					res.put(st.nextToken(),st.nextToken());
				}
				
	       }
	}

	 System.out.println("Tolkebyroo laadimine " + res.toString());
		  
	  }catch(Exception e){
		  e.printStackTrace();
	  }
	  
	  return res ;
  }
  private TreeMap yhikutolked = null;

  
  private Vector getKliendid(){
	  Vector res = new Vector();
	  
	  try {
	
		  
			File fil = new File("edastuskliendid.txt");
	if (fil.exists()){
			FileReader fr = new FileReader("edastuskliendid.txt");
			BufferedReader reader = new BufferedReader(fr);
			String line = null;
			while ((line = reader.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, ",");
				while(st.hasMoreTokens()){
					res.add(st.nextToken());
				}
				
	       }
	}

	 System.out.println("edastuskliendid laadimine " + res.toString());
		  
	  }catch(Exception e){
		  e.printStackTrace();
	  }
	  
	  return res ;
  }
  
  private TreeMap getKliendidSekr(){
	  TreeMap res = new TreeMap();
	  
	  try {
	
		  
			File fil = new File("sekrkliendid.txt");
	if (fil.exists()){
			FileReader fr = new FileReader("sekrkliendid.txt");
			BufferedReader reader = new BufferedReader(fr);
			String line = null;
			while ((line = reader.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, "=");
				while(st.hasMoreTokens()){
					res.put(st.nextToken(),st.nextToken());
				}
				
	       }
	}

	 System.out.println("sekrkliendid laadimine " + res.toString());
		  
	  }catch(Exception e){
		  e.printStackTrace();
	  }
	  
	  return res ;
  }
  
  
  private Vector edastuskliendid = null;

  private TreeMap sekrkliendid = null;
  
  
  
  public String identifyCustomer(String id,String regnr) throws SQLException {

	    String acc = null;
	    if(regnr != null && regnr.trim().length()>0){

	    String sql2 = " SELECT accountnum  AS kood FROM custtable where dataareaid='dat'  AND accountnum = '" + id + "'";
	    System.out.println(sql2);
	    


	    Statement st2 = con2.createStatement();
	    ResultSet rs2 = st2.executeQuery(sql2);

	    while (rs2.next()) {
	      acc = rs2.getString("kood").trim();

	    }
	    rs2.close();
	    st2.close();

	    }


	    if(regnr == null && acc == null){

	        String sql2 = " SELECT accountnum  AS kood FROM custtable where dataareaid='dat' and   accountnum = '" + id + "'";
	        System.out.println(sql2);



	        Statement st2 = con2.createStatement();
	        ResultSet rs2 = st2.executeQuery(sql2);

	        while (rs2.next()) {
	          acc = rs2.getString("kood").trim();

	        }
	        rs2.close();
	        st2.close();

	        }

	    if(regnr == null && acc == null){

	        String sql2 = " SELECT accountnum  AS kood FROM custtable where dataareaid='dat' and   namealias = '" + id + "' ";
	        System.out.println(sql2);



	        Statement st2 = con2.createStatement();
	        ResultSet rs2 = st2.executeQuery(sql2);

	        while (rs2.next()) {
	          acc = rs2.getString("kood").trim();

	        }
	        rs2.close();
	        st2.close();

	        }
	    
	  	if (acc == null){

	  	String sql1 = " SELECT accountnum  AS kood FROM custtable where dataareaid='dat' and  (REGNUM_W)= '" + id + "' AND ouraccountnum = '" + id + "'";
	    System.out.println(sql1);



	    Statement st1 = con2.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      acc = rs1.getString("kood").trim();

	    }
	    rs1.close();
	    st1.close();

	  	}
	   // System.out.println("Klient: " + acc);
	    return acc;

	  }


  public String identifyProduct(String ean, String prodID) throws SQLException{
	    String sql1 = " SELECT   bc.itemid AS kood FROM inventitembarcode bc "  +
	  " WHERE bc.dataareaid='dat' and itembarcode = '" + ean + "' " ;


	    String prod = null; 
	    Statement  st1 = con2.createStatement();
        ResultSet  rs1 = st1.executeQuery(sql1);
	      while (rs1.next()) {
	        prod = rs1.getString("kood").trim();
	      	System.out.println("prod2 " + prod );
	      }
	    rs1.close();
	    st1.close();
	    System.out.println(sql1);
	    
	   System.out.println("prod" + prod +" "+ ean+prodID);
	   if(prod == null){
	   	prod = prodID;
	   }
	    return prod;
	  }



  public void makeOrder(Order order) throws SQLException {
	  if(order.getStatus() > 2){
		  sendNotification(order);
	  }
	  makeOutput(order);
  }

  
  private void sendNotification(Order order){
	  
	  String sql = "SELECT cus.name, cus.accountnum, (case when len(cus.invoiceaccount)>0 then cus.invoiceaccount " +
	  	   " else cus.accountnum end) AS payer, " +
           "   cus.address ,salesgroup AS agent , emp.email as mail " +
           "   FROM custtable cus  INNER JOIN EMPLTABLE emp ON emp.emplid=cus.salesgroup and " +
           "cus.dataareaid=emp.dataareaid WHERE  cus.dataareaid='dat' " +
           "   AND blocked=0  and cus.accountnum = '" + order.getCompany() + "'";
	  try{
		  Statement  st1 = con2.createStatement();
	      ResultSet  rs1 = st1.executeQuery(sql);
	      System.out.println(sql);  
	      while (rs1.next()) {
		        String name  = rs1.getString("name").trim();
		        String payer = rs1.getString("payer").trim();
		        String aadr  = rs1.getString("address").trim();
		        String mail =  rs1.getString("mail");
		      	System.out.println("Klient Edi tellimusel " + name  + " " + mail + " " + payer);
		      	
		      	if(mail!= null && mail.trim().length() >0 && edastuskliendid.contains(payer)){
		        	sendEmail("Saabus tellimus :\n" + name + " " + aadr + Converter.getOrderText(order,null) + "\n"
		        			+ "Lisainfo : " + order.getInfo() + "\nTellimuse koostas : " + order.getTeostaja(), "Edi Tellimus : " + name
		        			,mail.trim()); //mail.trim()
		  		}
		      	
		      	if(mail!= null && mail.trim().length() >0 && sekrkliendid.get(payer) != null){
		        	sendEmail("Saabus tellimus :\n" + name + " " + aadr + Converter.getOrderText(order,null) + "\n"
		        			+ "Lisainfo : " + order.getInfo() + "\nTellimuse koostas : " + order.getTeostaja(), "Edi Tellimus : " + name
		        			,sekrkliendid.get(payer)+""); //mail.trim()
		  		}
		      	
		      	
		      	
		      }
		    rs1.close();
		    st1.close();
		 	  
	  }catch (Exception e){
		 e.printStackTrace(); 
	  }
	    
	    
	   
	  
	  
  }
  
  
  
  
  private void sendEmail(String body,String subjekt,String to ){
		Email mail = new Email(null);
		mail.setSMTP(parent.getProps().getProperty("smtphost"));
		mail.setFrom(parent.getProps().getProperty("from"));
		mail.setSubjectFilesTo(subjekt);
		//mail.addAttach(f.getAbsolutePath());
		mail.setSendNotice(false);
		try {
			mail.setBody(body);
		} catch (MessagingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		StringTokenizer st = new StringTokenizer(to,",");
		while(st.hasMoreTokens()){
			String mai = st.nextToken();
			mail.setFilesTo(mai);
			try {
				mail.send();
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}	
  }
  
  

  private void makeOutput(Order order){
	    
	 	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    
	      try {
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.newDocument();
	        Element root = doc.createElement("tellimus");
	        doc.appendChild(root);
	        
	        Node un3 = doc.createElement("tellija-kood");
	        Text unVal3 = doc.createTextNode(order.getCompany());
	        un3.appendChild(unVal3);
	        root.appendChild(un3);
	        
	        
	        Node ladu = doc.createElement("ladu");
	        Text laduVal = doc.createTextNode("");
	        ladu.appendChild(laduVal);
	        root.appendChild(ladu);

	        Node nr = doc.createElement("nr");
	        Text nrVal = doc.createTextNode(order.getDokNr() + "");
	        nr.appendChild(nrVal);
	        root.appendChild(nr);
	        
	        if(order.getTeostaja() != null && order.getTeostaja().trim().length() > 0){
	        	Node tellija = doc.createElement("kontakt");
		        Text tellijaVal = doc.createTextNode(order.getTeostaja() + "");
		        tellija.appendChild(tellijaVal);
		        root.appendChild(tellija);
		    }
	 	       
	        
	        if(order.getInfo() != null && order.getInfo().trim().length() > 0){
	        	String aaa = order.getInfo();
	        	if(order.getInfo().indexOf(order.getDokNr()) != -1){
	        		aaa = aaa.substring(order.getDokNr().length());
	        	}
	        	if(aaa.trim().length() > 0){

		        	Node lisainfo = doc.createElement("lisainfo");
			        Text lisainfoVal = doc.createTextNode(aaa + "");
			        lisainfo.appendChild(lisainfoVal);
			        root.appendChild(lisainfo);
			        		
	        	}
	        }

	        
	        try{

		        Node un4 = doc.createElement("tarne-kuupaev");
		        
		        Calendar cal = Calendar.getInstance();
		        cal.setTime(order.getShipCal().getTime());

		        String month = (cal.get(Calendar.MONTH) + 1) + "";
		        if (month.length() == 1) month = "0" + month;
		        String day = cal.get(Calendar.DAY_OF_MONTH) + "";
		        if (day.length() == 1) day = "0" + day;

		        String dateString = cal.get(Calendar.YEAR) +""+ month +""+ day ;
		        
		        Text unVal4 = doc.createTextNode(dateString);
		        un4.appendChild(unVal4);
		        root.appendChild(un4);
		        
	        }catch(Exception e){
	        	
	        }
	        
	        Node koostaja = doc.createElement("koostaja");
	        
	        Node isikuid = doc.createElement("isiku-id");
	        Text isikuidVal = doc.createTextNode("EDI");
	        
	        isikuid.appendChild(isikuidVal);
	        koostaja.appendChild(isikuid);
	        
	        root.appendChild(koostaja);


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
	 	
	      String fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr() + uni +  ".xml";
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

  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
	  
  
  
  private void addLine(OrderLine line, Node read, Document doc,int counter ,Order order) throws SQLException{
	   

	     Node ridaitem = doc.createElement("rida");
	     read.appendChild(ridaitem);
	     
	     
	     String toode  = line.getToode();
	     if(toode!= null){
	    	 toode = toode.trim();
	     }else{
	    	 toode = "";
	     }

     
	     Node toote_kood = doc.createElement("toote-kood");
	     Text toote_koodVal = doc.createTextNode(toode);
	     toote_kood.appendChild(toote_koodVal);
	     ridaitem.appendChild(toote_kood);

	     Node toote_ean = doc.createElement("ean");
	     Text toote_eanVal = doc.createTextNode(line.getEAN());
	     toote_ean.appendChild(toote_eanVal);
	     ridaitem.appendChild(toote_ean);

	     Text nimetusVal = doc.createTextNode(line.getTooteNimi().replace('`',' ').replace('�',' '));
	     Node nimetus = doc.createElement("nimetus");
	     nimetus.appendChild(nimetusVal);
	     ridaitem.appendChild(nimetus);
	     
	     
	
	     Node koGus = doc.createElement("kogus");
	     Text kogusVal = doc.createTextNode(getNF().format(line.getKogus()));
	     koGus.appendChild(kogusVal);
	     ridaitem.appendChild(koGus); 
	     
	     String yhike = line.getUnit();
	     if(yhike.equals("M�")){
	    	 yhike = "M2";
	     }
	     System.out.println("tolge 1 " + yhike );
	     if(yhike.trim().length() > 0 && yhikutolked != null && yhikutolked.get(yhike) != null){
	    	 yhike = yhikutolked.get(yhike) + "";
	     }
	    	 
	     System.out.println("tolge 2 " + yhike );
		    	 
	     
	     Node yhik = doc.createElement("yhik");
	     Text yhikVal = doc.createTextNode(yhike);
	     yhik.appendChild(yhikVal);
	     ridaitem.appendChild(yhik);



	     Node koGeus = doc.createElement("soodustus");
	     Text koguseVal = doc.createTextNode(getNF().format(0));
	     koGeus.appendChild(koguseVal);
	     ridaitem.appendChild(koGeus);

	     
	     addNumLine(); 
	   }


  
  
  private ImportPanel parent;

  public String getDriverVersion() {
    return "Vipex";
  }

 

  public boolean isAmount() {
    return isAmount;
  }

  /**
   *
   * @param aUseEAN - the new value for useEAN
   */
  public void setAmount(boolean aAmount) {
    isAmount = aAmount;
  }
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

  private String url = "jdbc:odbc:BMSDSN";
  private Connection con2;
  private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
/**
 * @return the nf
 */
public NumberFormat getNF() {
	return nf;
}
/**
 * @param nf the nf to set
 */
public void setNF(NumberFormat nf) {
	this.nf = nf;
}

}
