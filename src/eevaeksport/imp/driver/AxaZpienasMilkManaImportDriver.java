package eevaeksport.imp.driver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
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

import eevaeksport.Agent;
import eevaeksport.Email;
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

public class AxaZpienasMilkManaImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public AxaZpienasMilkManaImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "oracle.jdbc.driver.OracleDriver";
    databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection( parent.getProps().getProperty("dsn.host")
        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      con2 = DriverManager.getConnection(parent.getProps().getProperty("dsn.host")
              , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      con5 = DriverManager.getConnection(parent.getProps().getProperty("dsn.host")
        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      
    } catch (Exception e){
       log.error(e);
       this.sendExceptionEmail(e.toString());
       System.exit(0);
    }
  }

  
  
  
  private void sendExceptionEmail(String buf){
	  String address = parent.getProps().getProperty("exceptionemail");
	  if(address != null ){
		  Email email = new Email(parent.getEksportFrame().getString("importerror"));
         
          email.setFrom(parent.getProps().getProperty("from"));
          email.setSMTP(parent.getProps().getProperty("smtphost"));
          email.setUser(parent.getProps().getProperty("smtpuser"));
          email.setPass(parent.getProps().getProperty("smtppass"));
          email.setSubjectFilesTo(parent.getEksportFrame().getString("importerror")  );
          if(parent.getProps().getProperty("language").equals("Russian")){
            email.setEncoding("KOI8-R");
          }
          
          
          email.setEncoding("iso-8859-1");
          try {
        	  
        	email.setBody(buf.toString());

            StringTokenizer st =  new StringTokenizer(address,";");
            while(st.hasMoreTokens()){
            	String aadr = new InternetAddress(st.nextToken()).toString();
              email.setFilesTo(aadr);
              email.send();
              }
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  		  
	  }
  }
  
  
  


  private Connection getNewConnectionSkaita() throws SQLException{
	    return DriverManager.getConnection("jdbc:odbc:skaita", "sa", "01234567");
	  }

  
  public String getImportFile(){
   return parent.getProps().getProperty("imp.read");
 }

  public String identifyCustomer(String id,String regnr) throws SQLException {

    String acc = null;
    if(regnr != null && regnr.trim().length()>0){

    String sql2 = " SELECT accountnum  AS kood FROM  custtable where dataareaid='zp' and  (vatnum)= '" + regnr + "' AND accountnum = '" + id + "'";
    System.out.println(sql2);
    


    Statement st2 = con5.createStatement();
    ResultSet rs2 = st2.executeQuery(sql2);

    while (rs2.next()) {
      acc = rs2.getString("kood").trim();

    }
    rs2.close();
    st2.close();

    }


    if(regnr == null && acc == null){

        String sql2 = " SELECT accountnum  AS kood FROM  custtable where dataareaid='zp' and   accountnum = '" + id + "'";
        System.out.println(sql2);



        Statement st2 = con5.createStatement();
        ResultSet rs2 = st2.executeQuery(sql2);

        while (rs2.next()) {
          acc = rs2.getString("kood").trim();

        }
        rs2.close();
        st2.close();

        }

    if(regnr == null && acc == null){

        String sql2 = " SELECT accountnum  AS kood FROM  custtable where dataareaid='zp' and   namealias = '" + id + "' ";
        System.out.println(sql2);



        Statement st2 = con5.createStatement();
        ResultSet rs2 = st2.executeQuery(sql2);

        while (rs2.next()) {
          acc = rs2.getString("kood").trim();
        }
        
        rs2.close();
        st2.close();

        }
    
  	if (acc == null){

  	String sql1 = " SELECT accountnum  AS kood FROM  custtable where dataareaid='zp' and  (vatnum)= '" + id + "' AND accountnum = '" + id + "'";
    System.out.println(sql1);



    Statement st1 = con5.createStatement();
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
    String sql1 = "SELECT inv.itemid AS toode ,eancode FROM  inventtable inv left JOIN  inventtablemodule mod ON mod.itemid = inv.itemid where blocked = 0 and moduletype = 2 and  mod.dataareaid = 'zp' and inv.dataareaid='zp' " +
    		" AND inv.eancode='" + ean + "' order by inv.itemid " ;


    String sql2 = "SELECT inv.itemid AS toode FROM  inventtable inv left JOIN  inventtablemodule mod ON mod.itemid = inv.itemid where blocked = 0  and moduletype = 2 and  inv.dataareaid = 'zp' and mod.dataareaid = 'zp' and inv.itemid='" + prodID + "' ";

    String prod = null;
    ResultSet rs1 = null;
    Statement  st1 = con.createStatement();
  if(prodID != null){
	   
	     rs1 = st1.executeQuery(sql2);
		System.out.println(sql2);
	    while (rs1.next()) {
	      prod = rs1.getString("toode").trim();
	  	System.out.println("prod1 " + prod );
	    } 
	    rs1.close();
  }
 

  

    if(prod == null){
      rs1 = st1.executeQuery(sql1);
      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
      	System.out.println("prod2 " + prod );
      }
    }
    rs1.close();
    st1.close();
   System.out.println("prod" + prod +" "+ ean+prodID);
   if(prod == null){
   	prod = prodID;
   }
    return prod;
  }

  
  private Vector splitOrder (Order order){
	  Vector result = new Vector();
      
	  Order ordZpi = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ordKlp = new Order(order.getCompany(),order.getAgentFirstName());
   long ship = order.getShipCal().getTimeInMillis() - 10800000 ;
	     
	  ordZpi.setAgentCode(order.getAgentCode());
	  ordZpi.setDokNr(order.getDokNr());
	  ordZpi.setInfo(order.getInfo());
	  ordZpi.setTeostaja((order.getTeostaja()));
	  ordZpi.setChanged(order.isChanged());
	  ordZpi.setShipCal(ship +"");
	  ordZpi.setStatus(order.getStatus());
	  ordZpi.setShipTime(order.getShipTime());
	  ordZpi.setWarehouse("zp");
	     
	  ordKlp.setAgentCode(order.getAgentCode());
	  ordKlp.setDokNr(order.getDokNr());
	  ordKlp.setInfo(order.getInfo());
	  ordKlp.setTeostaja(order.getTeostaja());
	  ordKlp.setChanged(order.isChanged());
	  ordKlp.setShipCal( ship +"");
	  ordKlp.setStatus(order.getStatus());
	  ordKlp.setShipTime(order.getShipTime());
	  ordKlp.setWarehouse("klp");
	  
	     


	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	
	  
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
		
		     String itemid = lin.getToode();
		    String ware = "";
		    if(itemid.startsWith("KLP-")){
		    	ware = "klp";
		    	itemid = itemid.substring(4);
		    }else{
		    	ware = "zp";
		    }
		    System.out.println("xxx " + ware  + " " + itemid);
		    
		     if(ware.equals("zp")){  
		    	 ordZpi.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		    	     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);	 
			     } else {
				    ordKlp.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null); 
			    	 }
		     
		     

	  }





	  if(ordZpi.getOrderLines() != null && ordZpi.getOrderLines().size()>0){
		  result.add(ordZpi);  
	  }
	  if(ordKlp.getOrderLines() != null && ordKlp.getOrderLines().size()>0){
		  result.add(ordKlp);  
	  }
	 	 
	 System.out.println("Result =" +order.getDokNr()
			 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }





  public void makeOrder(Order order) throws SQLException{
	 
    setOrder(order);
    
    Vector splitted =  splitOrder(order);
	   System.out.println("tellimusi firmadesse " + splitted.size());
     boolean testing = false;
     if( "true".equals(parent.getProps().getProperty("testing"))){
    	 testing = true;
     }
      Enumeration en = splitted.elements();
      if(testing){
    	  while(en.hasMoreElements()){
			  Order ord = (Order)en.nextElement(); 
			  if(ord.getWarehouse().equals("zp")){
			    System.out.println(ord.getWarehouse() + " " +
			    		ord.getOrderLines().size() + " " +ord.getCompany() + " " + ord.getAgent());  
			  }
			 
			  if(ord.getWarehouse().equals("klp")){
				  Enumeration enumr = ord.getOrderLines().elements();	     
		    	     while (enumr.hasMoreElements()){
		    	    	 
		    	    	OrderLine orderLine = (OrderLine)enumr.nextElement(); 	 
		    	         String itemid = orderLine.getToode();
		    	         double kogus = orderLine.getKogus();
		    	      
		    	    	System.out.println(order.getCompany() + " "  
		    	    			+ " " + itemid + " " + orderLine.getKogus() + " " + kogus);
		    	     } 
		    	     String comp = getKPcustCode(ord.getCompany());
			    	  if (comp != null){
			    		  makeKLPOrder(ord,comp);  
			    	  }
		    	     
			  }
		  } 
      }else{
    	  while(en.hasMoreElements()){
			  Order ord = (Order)en.nextElement(); 
			 
			  if(ord.getWarehouse().equals("zp")){
				  String salesid =  (Calendar.getInstance().getTimeInMillis()+"").substring(3) ;
				    addInvoiceHeader(salesid);
				    addInvoiceLines(salesid);  
			  }
			 
			  if(ord.getWarehouse().equals("klp")){
				  String comp = getKPcustCode(ord.getCompany());
		    	  if (comp != null){
		    		  makeKLPOrder(ord,comp);  
		    	  }
				   
			  }
		  }  
      }
		 	  
  
  //  String sql4 = "update talsorderheader set ready=1 where ltrim(orderid) = ?";
  //  PreparedStatement st2 = con6.prepareStatement(sql4);
  //  st2.setString(1, salesid.trim());
  //  st2.executeUpdate();

  }
  
  
  
  
  
	public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}
	
	  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  
  

	  

	   private void makeKLPOrder(Order order , String comp){
		    
		   kogusumma = 0;
		   maksusumma = 0;
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
		        
			

		        
		        
		        
		        
	     long uni = Calendar.getInstance().getTimeInMillis();
	     Node header = doc.createElement("Order-Header");
		        
		        
		        Node nr = doc.createElement("OrderNumber");
		        Text nrVal = doc.createTextNode(order.getDokNr() + "");
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

		        root.appendChild(header);


		        Node parties = doc.createElement("Document-Parties");



		        Node seller = doc.createElement("Receiver");
		      

		        Node ise = doc.createElement("CodeByBuyer");
		        Text nimiVal = doc.createTextNode(comp);
		        ise.appendChild(nimiVal);
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


		 	
		      String fileName = parent.getProps().getProperty("conv.dirKLP") + order.getDokNr().trim() + (uni + "").trim() +  ".xml";
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
		     
		     
		     String toode  = line.getToode();
		     if(toode!= null){
		    	 toode = toode.trim().substring(4);
		     }else{
		    	 toode = " ";
		     }

		     Node toote_kood = doc.createElement("SupplierItemCode");
		     Text toote_koodVal = doc.createTextNode(toode);
		     toote_kood.appendChild(toote_koodVal);
		     ridaitem.appendChild(toote_kood);


		     Text nimetusVal = doc.createTextNode(line.getTooteNimi().replace('`',' ').replace('�',' '));
		     Node nimetus = doc.createElement("ItemDescription");
		     nimetus.appendChild(nimetusVal);
		     ridaitem.appendChild(nimetus);


		     double kogus = line.getKogus();
		     double price = line.getPrice();
		     String unit = line.getUnit();
		     
		     Node koGus = doc.createElement("OrderedQuantity");
		     
		     Text kogusVal = doc.createTextNode(getNF().format(kogus));
		     koGus.appendChild(kogusVal);
		     ridaitem.appendChild(koGus);

		  /*   Node yhiku_hind = doc.createElement("InvoiceUnitNetPrice");
		     
		     Text yhiku_hindVal = doc.createTextNode(this.nf.format(price));
		     yhiku_hind.appendChild(yhiku_hindVal);
		     ridaitem.appendChild(yhiku_hind);
*/

		     Node yhik = doc.createElement("UnitOfMeasure");
		     Text yhikVal = doc.createTextNode(unit);
		     yhik.appendChild(yhikVal);
		     ridaitem.appendChild(yhik);

		     rida.appendChild(ridaitem);
		     addNumLine(); 
		   }


	  
	  
	  
  
   
  public static String getYYYYpMMpDDpTimeNiceString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}

		sb.append("" + cal.get(Calendar.YEAR));
		sb.append("." + month  + ".");
		sb.append(day +" ");
	    
	    return sb.toString();
	  }

 	
  
  public String getPayer( String custID) throws SQLException{
	    String sql1 = "select invoiceaccount  as payer  from  custtable  where    dataareaid='zp'  and accountnum='" + custID + "'";
	    String prod = null;
	    ResultSet rs1 = null;
	    Statement  st1 = con.createStatement();
	  if(custID != null){
		   
		     rs1 = st1.executeQuery(sql1);
			System.out.println(sql1);
		    while (rs1.next()) {
		      prod = rs1.getString("payer").trim();	  	  
		    } 
	  }
  
	    rs1.close();
	    st1.close();
	      System.out.println("Maksja tuvastamine  " + prod  + " " + custID);
	   	    return prod;
	  }
  
  public String getKPcustCode( String custID) throws SQLException{
	  
	    String sql1 = "select accountnum as klp ,ExtCodeValue  from  custtable a ,  ExtCodeValueTable b " +
	    " where ExtCodeValue = '" + custID + "' and  a.dataareaid='klp' and  b.dataareaid='klp'"
	    		+ " and a.RecId= b.ExtCodeRelationRecId  and  b.ExtCodeId = 'ZP' and b.extCodeTableTableId=77";


	    sql1 = "Select Kodas  from Partneriai p LEFT OUTER JOIN dbo.PL_PARTNERIAI a ON P.PartneriaiId = a.PARTNERIAIID " +
          " where a.Axapta_Kod = '" + custID + "' ";
	              
	    String prod = null;
	    ResultSet rs1 = null;
	    
	    Connection con2 = this.getNewConnectionSkaita();
	    Statement  st1 = con2.createStatement();
	 
	    if(custID != null){
		     rs1 = st1.executeQuery(sql1);
			System.out.println(sql1);
		    while (rs1.next()) {
		      prod = rs1.getString("Kodas").trim();	  	  
		    } 
	     }
  
	    rs1.close();
	    st1.close();
	    con2.close();
	    
	      System.out.println("oioi KPL Kliendi tuvastamine  " + prod  + " " + custID);
	   	
	    return prod;
	   	 
	  }

  
  

  private void addInvoiceLines(String salesid) throws SQLException{

    Enumeration enumr = order.getOrderLines().elements();
    int counter = 0;
    while (enumr.hasMoreElements()){
      counter++;
     // log.info("ORDER_LINE_COUNTER:" + counter);
      OrderLine orderLine = (OrderLine)enumr.nextElement();
      String itemid = orderLine.getToode();
      double kogus = orderLine.getKogus();

      

      String sql0 = "SELECT inv.itemid,(inv.namealias) AS nimetus,STANDARDINVENTSIZEID AS unit ,STANDARDCONFIGID as conf  FROM  inventtable inv " +
              "  INNER JOIN  inventtablemodule mod ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid "
              + " WHERE  mod.dataareaid='zp' " +
              "  AND inv.dataareaid='zp' AND moduletype=2  " +
              "   AND (inv.namealias) is not null AND inv.itemid ='" + itemid + "' ";
      
   // log.info("SALES_LINE_SQL: " + sql0);
    System.out.println(sql0);
    Statement st = con5.createStatement();
    ResultSet rs = st.executeQuery(sql0);
   boolean olemas = false;
    String unit = " ";
    String name = "";
    String conf = "";
    while(rs.next()){
    	
        name = rs.getString("nimetus"); 
    	unit = rs.getString("unit");
    	conf = rs.getString("conf");


      if (unit !=null && unit.trim().length()>0){
      	unit = unit.trim();
      	olemas = true;
      }else {
      	unit = " ";
      }
    }
    rs.close();
    st.close();
    
  if(orderLine.getUnit().trim().length()>0){
   // unit = orderLine.getUnit();
  }
  if(!olemas){
  	itemid = orderLine.getTooteNimi();
  	name = orderLine.getEAN() + " " + orderLine.getTooteNimi();
  	if(itemid.length()>20){
  		itemid = itemid.substring(0,19);
  	}
  }
          String agent = order.getAgent();
          Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
          if(ag == null){
         	ag = new Agent(agent);
         }
     
          String sql =" INSERT into   BBSImportedSalesLine (LINENUM,DATAAREAID,RECVERSION,RECID," +
          		"ITEMNAMEEXT,ITEMID,QTY,DLVDATE," +
          		"ORDERID,SUITEITEMID,SUITEQTY,SIZEID,BARCODE , PARTITION , CONFIGURATIONID )  " +// DEPARTMEN,
             " values( " +counter + ",'zp',1," +getAndUpdateRecID() + 
            ",'" + name.replaceAll("'", " ") + "','"+itemid+"',"+kogus+",cast(? as date)," +//'" + order.getAgent() +
            "'" + salesid + "',' ',0,'" + unit + "',' ', 5637144576, '"+ conf +"')";

              log.info("SALES_LINE_SQL: " + sql);
              System.out.println(sql);
              PreparedStatement st2 = con2.prepareStatement(sql);
              java.util.Date dat = new java.util.Date();
              long ship = order.getShipCal().getTimeInMillis() - 10800000 ;
                dat.setTime(ship);
                dat.setSeconds(0);
                dat.setMinutes(0);
                dat.setHours(0);
               st2.setTimestamp(1, new java.sql.Timestamp(dat.getTime()));
              st2.executeUpdate();
              addNumLine();
              st2.close();

              }
  }

  private void addInvoiceHeader(String salesid) throws SQLException{

    long recID = getAndUpdateRecID();
 
         String agent = order.getAgent();
         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);

         if(ag == null){
         	ag = new Agent(agent);
         }

     String tekst = " ";

      if (order.getEmailBody()!= null){
         tekst = order.getEmailBody().replaceAll("\n"," ");
       }
      

      if (order.getShipTime()!= null){
         tekst =  order.getShipTime().replaceAll("\n"," ") + "  Day :" + getYYYYpMMpDDpTimeNiceString(order.getShipCal());
       }
      
      
      String ref = " ";
      if(order.getInfo() != null && order.getInfo().trim().length()>0 ){
      	ref = order.getInfo().trim();
      }

      if(order.getReference() != null && order.getReference().trim().length()>0 ){
      	ref = order.getReference().trim();
      }
      
      String sql1 = " select cus.invoiceaccount  as payer ,cus.ROUTECODE_TR as transa,"
      		+ "ww.DisplayValue  as dimension1,cus.INVENTLOCATION as loca " +
      		" from  custtable  cus left join DimensionAttributeValueSetItemView ww on ww.DimensionAttributeValueSet = cus.defaultdimension  " +
      		" where    cus.dataareaid='zp' and cus.accountnum='" + order.getCompany() + "'";
      
       sql1 = "select cus.invoiceaccount  as payer ,cus.ROUTECODE_TR as transa,ww.DisplayValue  as dimension1,"
       		+ "cus.INVENTLOCATION as loca , cus.accountnum as cust \r\n" + 
       		"      		 from  custtable  cus inner join Vendtable ve on ve.accountnum = cus.milksupplier_bbs and cus.dataareaid = ve.dataareaid \r\n" + 
       		"			 left join DimensionAttributeValueSetItemView ww on ww.DimensionAttributeValueSet = cus.defaultdimension  \r\n" + 
       		"      		 where    cus.dataareaid='zp' and ve.accountnum = '" + order.getCompany() + "'";
      
	    String payer  = "";
	    String transa  = "";
	    String dimension1  = "";
	    String newcustcode = order.getCompany();
	    
	    String loca  = " ";
	    ResultSet rs1 = null;
	    Statement  st1 = con.createStatement();
	     
		     rs1 = st1.executeQuery(sql1);
			System.out.println(sql1);
		    while (rs1.next()) {
		      payer = rs1.getString("payer").trim();
		      transa = rs1.getString("transa").trim();
		      dimension1 = rs1.getString("dimension1").trim();
		      loca = rs1.getString("loca").trim();
		      newcustcode = rs1.getString("cust").trim();
		  
		    } 
	    if(loca == null){
	    	loca = " ";
	    }

	    rs1.close();
	    st1.close();
      

    //Select   
      
      Calendar cal = Calendar.getInstance() ;        
      
  if(transa == null || transa.equals("")){
	  transa = " ";  }
  if(payer == null || payer.equals("")){

	  payer = order.getCompany();  }
  if(loca == null || loca.equals("")){
	  loca = " ";  }
          
  
  
  String sql = " INSERT INTO  BBSImportedSalesOrder (  " +
            " DLVDATE ,CUSTCODEEXT ,CUSTACCOUNT ,CUSTNAMEEXT ," +
            "ROUTECODE_TR , DIVISION  ,INVENTLOCATIONID ,CUSTINVOICEACCOUNT ," +
            "ORDERID ,CONFIRMED ,FILENAME ,SALESID  ,CREATEDDATETIME ,USERID ,DATAAREAID ,RECVERSION ,RECID"
            + " ,ORDERDATE, IMPORTDATE ,IMPORTTIME) " +
            " VALUES (cast(? as date),'" + order.getAgent() +"','" + newcustcode +"'," +
            "SUBSTRING(isnull(( select namealias from  custtable c inner join DirPartyTable dp ON c.partY=dp.REcid  where  dataareaid='zp'"
            + "  and accountnum='" + newcustcode + "'),'noname '),0,20),'" + 
            transa + "','" + dimension1 + "','" + loca + "','"+payer+ "','" +
            salesid  +"',0,' ','',getdate(), 'is','zp',1, " + recID +",cast(getdate() as date),cast(getdate() as date), 12345)" ;
     //   log.info("addInvoiceHeader2: " + sql); TO_DATE(sysdate,'YYYY-MM-DD HH24:MI:SS')
        System.out.println(sql);
        try{
        	 PreparedStatement stmt2 = con2.prepareStatement(sql);
             java.util.Date dat = new java.util.Date();
             long ship = order.getShipCal().getTimeInMillis() - 10800000 ;
            System.out.println(dat.toString() + "  sc " + order.getShipCal().getTime().toString() + " ref " + ref);
              dat.setTime(ship);
              dat.setSeconds(0);
              dat.setMinutes(0);
              dat.setHours(0);
               
             stmt2.setTimestamp(1, new java.sql.Timestamp(dat.getTime()));
             stmt2.executeUpdate();
             stmt2.close();
         
             addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
 	
        }catch (Exception r){
        	r.printStackTrace();
        	 this.sendExceptionEmail(r.toString());
        	System.exit(0);
        }
         }

  public static void saveProps(Properties props, String fileName){
	    try {
	      FileOutputStream out =
	          new FileOutputStream(fileName);
	      props.store(out, "");
	      out.close();
	    } catch (IOException e) {
	     
	    }
	  }


  public static Properties loadProps(String fileName){
    Properties props = new Properties();
    try {
      FileInputStream in = new FileInputStream(fileName);
      props.load(in);
      in.close();
    } catch (IOException e){
       // showError(e);
      }
    return props;
  }
  
  private long getAndUpdateRecID() throws SQLException{
    long nextRecID = 0;
     Properties props = loadProps("numerator.properties"); 
       if(props == null){
    	   props = new Properties();
       }
     String asi = (String) props.get("nextval");
     if(asi == null){
    	 nextRecID = 1;
     }else{
    	 nextRecID = Long.parseLong(asi) + 1;
    	 
     }
     props.put("nextval", nextRecID  + "");
     saveProps(props,"numerator.properties");
    return nextRecID;
  }


  public String getDriverVersion(){
    return "AxaZpienasImport";
  }

  private void setOrder(Order order){
    this.order = order;
  }

  private void addLog(String line){
    parent.addLog(line);
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
  private double maksusumma = 0;
  private double kogusumma = 0;
  private Connection con;
  private Connection con2;
  private Connection con5;
  private ImportPanel parent;
  private Order order = null;
  private boolean isAmount = false;
  private int counter = 0;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
