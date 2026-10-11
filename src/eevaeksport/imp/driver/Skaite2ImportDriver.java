/*
 * Created on 13.09.2007
 */
package eevaeksport.imp.driver;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */

 import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
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

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;


 public class Skaite2ImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(Skaite2ImportDriver.class.getName());

   public Skaite2ImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
     nf.setGroupingUsed(false);
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
    
     }
     catch (Exception e) {
       parent.showError(e);
     }
   }

   public String identifyCustomer(String id,String regnr)throws SQLException {
    String custid = null;

    if(regnr != null && regnr.equals("nestle")){
    	return    "450PP00578";
    }

    if(regnr != null && regnr.equals("npfoods")){
    	return    "450PP02552";
    }
    
    if(regnr != null && regnr.equals("alita")){
    	return    "450PP03747";
    }
    
    if(regnr != null && regnr.equals("henkel")){
    	return    "450PP06344";
    }
    
    if(regnr != null && regnr.equals("colgate")){
    	return    "450PP00081";
    }
    
    if(regnr != null && regnr.equals("felix")){
    	return    "450PP00139";
    }
    
    if(regnr != null && regnr.equals("prike")){
    	return    "450PP03964";
    }
    String cmd = "select kodas as kood from Partneriai par INNER JOIN Adresai adr ON adr.adresoid = par.adresoid where Pastabos ='" + id + "'" ;   
   Connection con = null;
try {
	con = this.getNewConnection();
} catch (Exception e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
    PreparedStatement st2 = con.prepareStatement(cmd);
    ResultSet rs = st2.executeQuery();
  int cc = 0;
    while (rs.next()){
    	custid =  rs.getString("kood");
    	cc++;
     }
    rs.close();
    st2.close();
    con.close();
    
    if(cc > 1){
    	return null;
    }

 	return custid;
   }
   
   
   public String identifyWare(String kood) throws SQLException {
	  String sql = "select warehouseno from asVwCustomer where rtrim(customercode) =  '"+ kood +"'";
	  String res = "EQ";
	   Connection con = null;
	   try {
	   	con = this.getNewConnection();
	   } catch (Exception e) {
	   	// TODO Auto-generated catch block
	   	e.printStackTrace();
	   }
	  PreparedStatement st2 = con.prepareStatement(sql);
	    ResultSet rs = st2.executeQuery();
	 
	    while (rs.next()){
	    	res =  rs.getString("warehouseno").trim();
	     }
	    rs.close();
	    st2.close();
	    con.close();
	    if(res.equals("VL00")){
	    	res = "EQ";
	    }
	    
	  return res;
   }
   

   public int getInCases(String kood) throws SQLException {
		  String sql = " select  EQ_kiekdezeje as kogus from pl_prekes tt inner join prekes t on t.PrekesID=tt.PREKESID where " +
                  " Kodas ='"+ kood +"'";
		 int res = 1;
		   Connection con = null;
		   try {
		   	con = this.getNewConnection();
		   } catch (Exception e) {
		   	// TODO Auto-generated catch block
		   	e.printStackTrace();
		   }
		  PreparedStatement st2 = con.prepareStatement(sql);
		    ResultSet rs = st2.executeQuery();
 		 
		    while (rs.next()){
		    	try{
		    		res =  rs.getInt("kogus");	
		    	}catch(Exception e){
		    		
		    	}
		    
		     }
		
		    rs.close();
		    st2.close();
		    con.close();
		  return res;
	   }
   
   
   public String getUnit(String kood) throws SQLException {
		  String sql = " select mv.Kodas as unit from Prekes p inner join MV mv on p.MVId = mv.MVid where p.Kodas ='"+ kood +"'";
		 String res = null;
		   Connection con = null;
		   try {
		   	con = this.getNewConnection();
		   } catch (Exception e) {
		   	// TODO Auto-generated catch block
		   	e.printStackTrace();
		   }
		  PreparedStatement st2 = con.prepareStatement(sql);
		    ResultSet rs = st2.executeQuery();
		 
		    while (rs.next()){
		    	res =  rs.getString("unit");
		     }
		
		    rs.close();
		    st2.close();
		    con.close();
		  return res;
	   }
   
   
   public String identifyRegion(String kood) throws SQLException {
		  String sql = "SELECT CONVERT( varchar(20), ivr_reg.IvNormatyvuReiksmesId ) as kood FROM Partneriai AS part " +
    "  INNER JOIN PL_Partneriai               AS pl      ON part.PartneriaiId = pl.PartneriaiId " +
    "    INNER JOIN IvNormatyvuReiksmes     AS ivr_reg ON pl.RegionasId = ivr_reg.IvNormatyvuReiksmesId " +
    " where part.kodas='"+ kood +"'";
		  String res = "154";
		   Connection con = null;
		   try {
		   	con = this.getNewConnection();
		   } catch (Exception e) {
		   	// TODO Auto-generated catch block
		   	e.printStackTrace();
		   }
		  PreparedStatement st2 = con.prepareStatement(sql);
		    ResultSet rs = st2.executeQuery();
		 
		    while (rs.next()){
		    	res =  rs.getString("kood").trim();
		     }
		 
		    rs.close();
		    st2.close();
		    con.close();
		  return res;
	   }
  

   public String identifyAgent(String ware) throws SQLException {
		  String res = "F128";
		   if(ware.equals("KA02") || ware.equals("141")) {
			   return "K425";
		   }
		   
		   if(ware.equals("KL01") || ware.equals("140")) {
			   return "L288";
		   }
	
		   /*
	       V956  Vilnius  154
	       K425  Kaunas    141
	       S567  Siauliai  142
	       P450  Panevezys  143
	       A628  Alytus    155
	       L288  Klaipeda 140 
	       */
 
		   
		   if(ware.equals("PA04") || ware.equals("143")) {
			   return "P450";
		   }
		   
		   if(ware.equals("�A03") || ware.equals("142")) {
			   return "S567";
		   }
		   
		   if(ware.equals("EQ") || ware.equals("154")) {
			   return "V956";
		   }
		   
		   if(ware.equals("EQBR") || ware.equals("154")) {
			   return "V956";
		   }
		 
		   if( ware.equals("155")) {
			   return "A628";
		   }
		  return res;
	   }
   
   public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  
   

   public String identifyProduct(String ean, String prodID) throws SQLException{

	  if( ean!=null){
		     if(ean.equals("59918257")){
		    	 ean = "7613032190842";
		     }
			   
		     if(ean.equals("5998710967612")){
		    	 ean = "7613032194680";
		     }
	 
		     if(ean.equals("5998710967193")){
		    	 ean = "5998710967070";
		     }		  
	  }

	  String prodf = prodID;
	  String firma = null;
	  if( prodID != null && prodID.indexOf("|") != -1){
		  prodf = prodID.substring(0,prodID.indexOf("|"));
		  firma = prodID.substring(prodID.indexOf("|")+1);
	  }
	  
	   
  	String sql2 = "SELECT kodas AS toode FROM Prekes " +
        " where KAPKodas like '%" + ean + "%' and kodas not like '%*%'";

  	
    String sql1 = "SELECT kodas AS toode FROM Prekes " +
    " where kodas = '"  + prodID + "'";
   // log.info("1_LINE_SQL: " + sql1);
   // log.info("2_LINE_SQL: " + sql2);

	String sql3 = "SELECT kodas AS toode FROM Prekes " +
    " where KAPKodas = '" + prodID + "' and kodas not like '%*%'";

	String sql4 = "select p.kodas as toode from PrekiuTiekejai t INNER JOIN Prekes p on p.PrekesID=t.PrekesID where" +
			" PartneriaiId =" + firma + " and PrekesKodas = '" + prodf + "'";
    
    String prod = null;
    Connection con = null;
    try {
    	con = this.getNewConnection();
    } catch (Exception e) {
    	// TODO Auto-generated catch block
    	e.printStackTrace();
    }
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql2);
    
    if(ean != null){
   
    int counter = 0;
    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
      counter++;
    }

    if(counter > 1){
    	return null;
    }
	
    }
    
    counter = 0;
    if(prodID != null && (prod == null || prod.trim().length() == 0)){
    	    con = null;
    	   try {
    	   	con = this.getNewConnection();
    	   } catch (Exception e) {
    	   	// TODO Auto-generated catch block
    	   	e.printStackTrace();
    	   }
      st1 = con.createStatement();
      rs1 = st1.executeQuery(sql1);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
        counter++;
      }
      rs1.close();
      st1.close();
      con.close();
      if(counter > 1){
      	return null;
      }
      }
      
    counter = 0;
    if(prodID != null && (prod == null || prod.trim().length() == 0)){
    	  
    	   try {
    	   	con = this.getNewConnection();
    	   } catch (Exception e) {
    	   	// TODO Auto-generated catch block
    	   	e.printStackTrace();
    	   }
      st1 = con.createStatement();
      rs1 = st1.executeQuery(sql3);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
        counter++;
      }
      rs1.close();
      st1.close();
      con.close();
      if(counter > 1){
      	return null;
      }
      }   
    System.out.println("prod1" + prod);
    
    counter = 0;
    if(prodID != null && (prod == null || prod.trim().length() == 0) && firma != null){
    	 System.out.println(sql4);
    	    con = null;
    	   try {
    	   	con = this.getNewConnection();
    	   } catch (Exception e) {
    	   	// TODO Auto-generated catch block
    	   	e.printStackTrace();
    	   }
      st1 = con.createStatement();
      rs1 = st1.executeQuery(sql4);
Vector res = new Vector();
      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
        if(!res.contains(prod)){
            res.add(prod);	
        }
      }
      rs1.close();
      st1.close();
      con.close();
      if(res.size() > 1){
      	return null;
      }
      }
    System.out.println("prod2" + prod + sql4);


    return prod;
   }

   public void makeOrder(Order order) throws SQLException{   
     makeOutput(order);
   }

   private void makeOutput(Order order){
	    
	   kogusumma = 0;
	   maksusumma = 0;
	 	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    
	      try {
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.newDocument();
	        Element root = doc.createElement("Document-Invoice");
	        doc.appendChild(root);


	  //      String filiaalStr = null;
	    //    String tarnijaKoodStr = null;

	        Node docheader = doc.createElement("Document-Header");
	        root.appendChild(docheader);
	        
	        Node un2 = doc.createElement("OriginalFileName");
	        Text unVal2 = doc.createTextNode(order.getShipTime());
	        un2.appendChild(unVal2);
	        docheader.appendChild(un2);
	        
	        Node un3 = doc.createElement("Description");
	        Text unVal3 = doc.createTextNode(order.getWarehouse());
	        un3.appendChild(unVal3);
	        docheader.appendChild(un3);
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
     Node header = doc.createElement("Invoice-Header");
	        
	        Node un = doc.createElement("InvoiceUniqId");
	        Text unVal = doc.createTextNode(uni + "");
	        un.appendChild(unVal);
	        header.appendChild(un);
	        
	        Node nr = doc.createElement("InvoiceNumber");
	        Text nrVal = doc.createTextNode(order.getDokNr() + "");
	        nr.appendChild(nrVal);
	        header.appendChild(nr);

	        Node kuupaev = doc.createElement("InvoiceDate");
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(order.getShipCal().getTime());

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

	        Node curr = doc.createElement("InvoiceCurrency");
	        Text currVal = doc.createTextNode(order.getDealer() + "");
	        curr.appendChild(currVal);
	        header.appendChild(curr);

	        String stat = "1";
	        
	        if(order.getCompany().equals( "450PP00578") || order.getCompany().equals( "450PP02552")
	        		 || order.getCompany().equals( "450PP00139")
                      || order.getCompany().equals( "450PP03747")){//nestle v�i npfoods alita v�i felix
	        	stat = "0";
	        }
	        
	        
	        
	        Node impStat = doc.createElement("InvoiceImportStatus");
	        Text impStatVal = doc.createTextNode(stat);
	        impStat.appendChild(impStatVal);
	        header.appendChild(impStat);
	        
	        Node invTax = doc.createElement("InvoiceTaxStatus");
	        Text invTaxVal = doc.createTextNode(stat);
	        invTax.appendChild(invTaxVal);
	        header.appendChild(invTax);

	/*        Node mkuupaev = doc.createElement("InvoicePaymentDueDate");
	        Calendar mcal = Calendar.getInstance();
	        mcal.setTime(getDOM().getMaksepaev());

	         month = (mcal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	         day = mcal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;

	         String mdateString = cal.get(Calendar.YEAR)+"-" + month +"-"+ day;
	        Text mkuupaevVal = doc.createTextNode(mdateString);
	        mkuupaev.appendChild(mkuupaevVal);
	        header.appendChild(mkuupaev);
*/


	        root.appendChild(header);


	        Node parties = doc.createElement("Invoice-Parties");



	    /*    Node buyer = doc.createElement("Buyer");

	        Node iln = doc.createElement("ILN");
	        Text ilnVal = doc.createTextNode(getDOM().getFiliaal());
	        iln.appendChild(ilnVal);
	        buyer.appendChild(iln);
	      String telkood =  getDOM().getTellijaKood();
	      if(telkood != null){
	    	   Node code_by = doc.createElement("CodeBySeller");
	           Text code_byVal = doc.createTextNode(telkood);
	           code_by.appendChild(code_byVal);
	           buyer.appendChild(code_by);
	      }

	        parties.appendChild(buyer);



	        Node payer = doc.createElement("Payer");
	        Node piln = doc.createElement("ILN");
	        Text pilnVal = doc.createTextNode(getDOM().getFiliaal());
	        piln.appendChild(pilnVal);
	        payer.appendChild(piln);
	        if(telkood != null){
	     	   Node code_by = doc.createElement("CodeBySeller");
	            Text code_byVal = doc.createTextNode(telkood);
	            code_by.appendChild(code_byVal);
	            payer.appendChild(code_by);
	       }
	        parties.appendChild(payer);

*/

	        Node seller = doc.createElement("Seller");
	      

	        Node ise = doc.createElement("CodeByBuyer");
	        Text nimiVal = doc.createTextNode(order.getCompany());
	        ise.appendChild(nimiVal);
	        seller.appendChild(ise);

	        parties.appendChild(seller);
	        root.appendChild(parties);



	        //RIDADE LISAMINE
	        Node read = doc.createElement("Invoice-Lines");

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


	        Node summary = doc.createElement("Invoice-Summary");


	        Node TotalLines = doc.createElement("TotalLines");
	        Text TotalLinesVal = doc.createTextNode(coun + "");
	        TotalLines.appendChild(TotalLinesVal);
	        summary.appendChild(TotalLines);


	        Node TotalNetAmount = doc.createElement("TotalNetAmount");
	        Text TotalNetAmountVal = doc.createTextNode(getNF().format(kogusumma ));
	        TotalNetAmount.appendChild(TotalNetAmountVal);
	        summary.appendChild(TotalNetAmount);


	        Node TotalTaxAmount = doc.createElement("TotalTaxAmount");
	        Text TotalTaxAmountVal = doc.createTextNode(getNF().format(maksusumma));
	        TotalTaxAmount.appendChild(TotalTaxAmountVal);
	        summary.appendChild(TotalTaxAmount);

	        Node TotalGrossAmount = doc.createElement("TotalGrossAmount");
	        Text TotalGrossAmountVal = doc.createTextNode(getNF().format(maksusumma + kogusumma));
	        TotalGrossAmount.appendChild(TotalGrossAmountVal);
	        summary.appendChild(TotalGrossAmount);

	        root.appendChild(summary);


	        Node taxsummary = doc.createElement("Tax-Summary");
	          Node taxSummaryLine = doc.createElement("Tax-Summary-Line");

	             Node TaxRate = doc.createElement("TaxRate");
	             String taxr = "ES21";
	             if(order.getCompany().equals("450PP00578") || order.getCompany().equals( "450PP02552")
	            		 || order.getCompany().equals( "450PP00139") || order.getCompany().equals( "450PP03747")   ){
	            	 //nestle v�i npfoods alita v�i felix
	            	 taxr = "0";
	             }
	             
		         Text TaxRateVal = doc.createTextNode(taxr);//kaibemaks eduardase kirja p�hjal 
		         TaxRate.appendChild(TaxRateVal);
		         taxSummaryLine.appendChild(TaxRate);

		         
		         Node TaxAmount = doc.createElement("TaxAmount");
		         Text TaxAmountVal = doc.createTextNode(getNF().format(0));//maksusumma eduardase kirja p�hjal 

		         TaxAmount.appendChild(TaxAmountVal);
		         taxSummaryLine.appendChild(TaxAmount);


		         Node TaxableAmount = doc.createElement("TaxableAmount");
		         Text TaxableAmountVal =  doc.createTextNode(getNF().format(0)); //kogusumma eduardase kirja p�hjal 
		         TaxableAmount.appendChild(TaxableAmountVal);
		         taxSummaryLine.appendChild(TaxableAmount);




		         taxsummary.appendChild(taxSummaryLine);
	        summary.appendChild(taxsummary);
	 	
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

   public NumberFormat getNF(){
 	    return this.nf;
 	  }
   
   
   
   private void addLine(OrderLine line, Node read, Document doc,int counter ,Order order) throws SQLException{
	   

	
	     Node rida = doc.createElement("Line");
	     read.appendChild(rida);

	     Node ridaitem = doc.createElement("Line-Item");


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
	    	 toode = " ";
	     }

	     Node toote_kood = doc.createElement("BuyerItemCode");
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
	     
	     if(unit!= null && line.getUnit().equals("CASE")){
	    	 int incase = this.getInCases(line.getToode());
	    	 if(incase > 0){
	    		 kogus = kogus*incase;
	    		 price = price/incase;
	    		 unit = getUnit(line.getToode());
	    		 if(unit == null){
	    			 unit = "vnt."; 
	    		 }	    		 
	    	 }
	     }
	     
	     Node koGus = doc.createElement("InvoiceQuantity");
	     
	     Text kogusVal = doc.createTextNode(getNF().format(kogus));
	     koGus.appendChild(kogusVal);
	     ridaitem.appendChild(koGus);

	     Node yhiku_hind = doc.createElement("InvoiceUnitNetPrice");
	     
	     Text yhiku_hindVal = doc.createTextNode(this.nf.format(price));
	     yhiku_hind.appendChild(yhiku_hindVal);
	     ridaitem.appendChild(yhiku_hind);


	     Node yhik = doc.createElement("UnitOfMeasure");
	     Text yhikVal = doc.createTextNode(unit);
	     yhik.appendChild(yhikVal);
	     ridaitem.appendChild(yhik);



	    String vat =  getNF().format(line.getBonus());
	     

	     Node km = doc.createElement("TaxRate");
	     
	     String taxr = "ES21";
         if(order.getCompany().equals("450PP00578") || order.getCompany().equals( "450PP02552")  || order.getCompany().equals( "450PP03747")
        		 || order.getCompany().equals( "450PP00139")){ //nestle v�i npfoods alita v�i felix
        	 taxr = vat;
         }
	     Text kmVal = doc.createTextNode(taxr);
	     km.appendChild(kmVal);
	     ridaitem.appendChild(km);
	   

	     String vatam =  this.nf.format(kogus * price * (line.getBonus()/100));
	     maksusumma = maksusumma + (kogus * price * (line.getBonus()/100));

	     Node reakm_summa = doc.createElement("TaxAmount");
	     Text reakm_summat = doc.createTextNode(vatam);
	     reakm_summa.appendChild(reakm_summat);
	     ridaitem.appendChild(reakm_summa);


	     Node reanet_summa = doc.createElement("NetAmount");
	     Text reanet_summat = doc.createTextNode(this.nf.format(kogus*price));
	     reanet_summa.appendChild(reanet_summat);
	     ridaitem.appendChild(reanet_summa);
	     
	     kogusumma = kogusumma + (kogus * price);

	     Node bestbefore = doc.createElement("BestBefore");
	     Text best_before = doc.createTextNode(line.getPartii());
	     bestbefore.appendChild(best_before);
	     ridaitem.appendChild(bestbefore);

	     rida.appendChild(ridaitem);
	     addNumLine(); 
	   }


 
   public String getDriverVersion(){
     return "Skaite2";
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

   private String url = "jdbc:odbc:";
  
   private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
   private double maksusumma = 0;
   private double kogusumma = 0;

   
	private Connection getNewConnection() throws Exception {
		return DriverManager.getConnection(url + "EUGESTA"
	            ,  "M_Partner_1", "mpsk2002");
	}
	
   


   private ImportPanel parent;

   private boolean isAmount = false;
   private int counter = 0;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
 }

