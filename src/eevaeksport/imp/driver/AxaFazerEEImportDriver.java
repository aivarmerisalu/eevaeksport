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
import java.util.Date;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

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

public class AxaFazerEEImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

    boolean testing = false;
  
  public AxaFazerEEImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    if("true".equals( parent.getProps().getProperty("testing"))){
    	testing = true;
    }
    loadSignatures();
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
  
  
  private TreeMap names = null;
  private TreeMap custwares = null;

  private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	  }
  
  private String custName(String code){
	  String res = "noname";
	  String sql =  "  SELECT accountnum ,cus.name as nimi FROM custtable cus WHERE  "
	  		+ " cus.dataareaid in ('eee') and ltrim(accountnum) ='" + code + "'" ;

	   System.out.println(sql);
	   try {
		
		    Connection con = getNewConnection();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(sql);
	        
	        while(rs.next()){
	        	 res = rs.getString("nimi").trim();
		    }
	       
	        rs.close();
	        stmt.close();
	        con.close(); 
	   
	   }catch(Exception e){
		  e.printStackTrace(); 
	   }
	      
	        
       System.out.println("customerName " + res);
	  
	  return res ;
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
  
  private HashSet makeShoko() {
	  HashSet res = new HashSet();
	  String sql =  " SELECT  t.ITEMID as name FROM inventtable  t where  dataareaid ='eee' and  t.itemgroupid = 'SALD'" ;
 	  
 	   
 	   try {
 		   Connection con = getNewConnection();
 	        Statement stmt = con.createStatement();
 	        ResultSet rs = stmt.executeQuery(sql);
 	        
 	        while(rs.next()){
 	        	res.add(rs.getString("name").trim());
 	        }
 	        
 	        rs.close();
 	        stmt.close();
 	        con.close();
 	        
 	   }catch(Exception e){
 		 e.printStackTrace();  
 	   }
	  return res;
  }
  
private String getProdNimi(String toode){
	  
	  String sql =  " SELECT  t.itemname as name FROM inventtable  t where  dataareaid ='eee' and t.ITEMID =  '" + toode + "'" ;
	  	   System.out.println(sql);
	  	   String res = "<No Name>";
	  	   try {
	  		   Connection con = getNewConnection();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs = stmt.executeQuery(sql);
	  	        
	  	        if(rs.next()){
	  	        	res = rs.getString("name").trim();
	  	        }
	  	        
	  	        rs.close();
	  	        stmt.close();
	  	        con.close();
	  	        
	  	   }catch(Exception e){
	  		 e.printStackTrace();  
	  	   }
     return res;
  }

private String getEan(String toode){
	  
	  String sql =  " SELECT bar.itembarcode as name FROM inventitembarcode bar  WHERE  bar.itemid = '" + toode + "'"
	  		+ " and bar.barcodesetupid ='STD'  and useforprinting = 1  and dataareaid = 'eee' ";
	  	   System.out.println(sql);
	  	   String res = "00000000000";
	  	   try {
	  		   Connection con = getNewConnection();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs = stmt.executeQuery(sql);
	  	        
	  	        if(rs.next()){
	  	        	res = rs.getString("name").trim();
	  	        }
	  	        
	  	        rs.close();
	  	        stmt.close();
	  	        con.close();
	  	        
	  	   }catch(Exception e){
	  		 e.printStackTrace();  
	  	   }
   return res;
}


private Vector signatures = new Vector();

  
  public void makeOrder(Order order) throws SQLException{

	   
	  trouble = null;
	  if (signatures == null){
	   	   signatures = new Vector();
	      }
     counter = 0;
     
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
				  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
			  }else{
				  
				  
				  Vector splitted =  splitOrder(order);
			 	  Enumeration en = splitted.elements();
			 	  while(en.hasMoreElements()){
			 		  Order ord = (Order)en.nextElement();
			 			  makeOutput(ord,tagastus);  
				    }
			      		
			     
			  
				 // makeOutput(order,tagastus);  
				 // signatures.add(signature);
			  }
			   try {
					saveObject(signatures, "signatures.vec");
				} catch (IOException e) {
					e.printStackTrace();
				}
			
    
  }
  
  HashSet shoko = null;
  
 private Vector splitOrder (Order order){
	  

	 if(shoko == null) {
		 shoko = makeShoko();
	 }
	 
	 
	 
	 Vector result = new Vector();

	  if (order.getDokNr() == null || order.getDokNr().trim().length() == 0){
		  order.setDokNr((Calendar.getInstance().getTimeInMillis() + "").substring(3));
	  }
	 System.out.println("Shoko " + shoko.size() );

	  Order ordS0 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ordSG = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ordBR = new Order(order.getCompany(),order.getAgentFirstName());
		 
	  ordS0.setAgentCode(order.getAgentCode());
	  ordS0.setDokNr(order.getDokNr());
	  ordS0.setCompName(order.getShipMode());
	  
	  // siin teha muudatus et paneks +1 
	  
	  
	    Calendar cal = Calendar.getInstance();
	      cal.add(Calendar.DAY_OF_MONTH, 1);
	      
	  if(order.isShipDateUsed()) {
		  ordS0.setShipCal(order.getShipCal().getTimeInMillis() +"");  
	  }else {
		  ordS0.setShipCal(cal.getTimeInMillis() +"");
	  }
	 
	  ordS0.setStatus(order.getStatus());
	  ordS0.setInfo(order.getInfo());
	  ordS0.setPaymTerm(order.getPaymTerm());
	  ordS0.setDealer("leib");
	  ordS0.setTeostaja(order.getTeostaja());
		
	  
	  ordSG.setAgentCode(order.getAgentCode());
	  ordSG.setDokNr(order.getDokNr());
	  ordSG.setCompName(order.getShipMode());
	  ordSG.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordSG.setStatus(order.getStatus());
	  ordSG.setInfo(order.getInfo());
	  ordSG.setPaymTerm(order.getPaymTerm());
	  ordSG.setDealer("soko");
	  ordSG.setTeostaja(order.getTeostaja());
	  
	  ordBR.setAgentCode(order.getAgentCode());
	  ordBR.setDokNr(order.getDokNr());
	  ordBR.setCompName(order.getShipMode());
	  ordBR.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordBR.setStatus(order.getStatus());
	  ordBR.setInfo(order.getInfo());
	  ordBR.setPaymTerm(order.getPaymTerm());
	  ordBR.setDealer("gift");
	  ordBR.setTeostaja(order.getTeostaja());

	  
	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
		  System.out.println("Line " + lin.getToode() + " " + shoko.contains(lin.getToode()));
		  
		    if(shoko.contains(lin.getToode())){
		     	ordSG.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,lin.docType);
		    	ordSG.setDokNr(order.getDokNr().substring( 0,order.getDokNr().length()-1) + "S");
			    ordSG.setReference(order.getReference());
		     }else if(lin.getDiscount() == -2){
		    	   ordBR.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,lin.docType);
			     	ordBR.setDokNr(order.getDokNr().substring( 0,order.getDokNr().length()-1) + "G");
			     	ordBR.setReference(order.getReference());
			     	
			 } else {
		    	   ordS0.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,lin.docType);
			     	ordS0.setDokNr(order.getDokNr());
			     	ordS0.setReference(order.getReference()); 
			 }
		  
	  }
	  

	  if(ordS0.getOrderLines() != null && ordS0.getOrderLines().size()>0){
		  result.add(ordS0);  
	  }

	  if(ordSG.getOrderLines() != null && ordSG.getOrderLines().size()>0){
		  result.add(ordSG);  
	  }
	  if(ordBR.getOrderLines() != null && ordBR.getOrderLines().size()>0){
		  result.add(ordBR);  
	  }
	  
	 System.out.println("Result =" +order.getDokNr()
			 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }



  
  public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
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
  
  
  private void makeOutput(Order order , boolean ret){
	 	StringBuffer sb = new StringBuffer();
	 	 nf.setGroupingUsed(false);

	 	try {
	 	  int cou = 0;
			String ref = order.getTeostaja();
			if ( ref == null ){
				ref =  " ";
			}
			String com = order.getEmailBody();
			if ( com == null ){
				com =  " ";
			}		
			String ware = order.getWarehouse();
			System.out.println("ladu tellimusel " + ware);
			if ( ware == null ){
				ware = "10";
			}
			
			 String doknr = order.getDokNr();
	         if(doknr.length()>6){
	         	// doknr = doknr.substring(doknr.length()-6,doknr.length());
	         }
	                 

	         String ls = System.getProperty("line.separator");
	         String fixagent = order.getAgent(); 
	         if(fixagent.equals("mand")){
	           //   fixagent = "m�nd";
	         }
	     
	         String custID = order.getCompany();
	         String ladu = null;
	             Calendar  shipDate = order.getShipCal();
	             
	             String code = order.getWarehouse();        
	             if (code != null){
	               ladu = code;
	             }
	           
	           if (ladu == null) {
	             ladu = "";
	           }
	  
	      

	       
	           Calendar cal = Calendar.getInstance();
	          
	           if (shipDate != null) {
	                 cal = shipDate;
	               }
	         
	           sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + ls);
	           sb.append("<E-Document>" + ls);
	      
	           sb.append("<Header>" + ls);
	           sb.append("<DateIssued>" + escape(getDateString(Calendar.getInstance())) + "</DateIssued>" + ls);
	           sb.append("<SenderGLN>4740103000007</SenderGLN>" + ls);
	           sb.append("<ReceiverID>" + order.getCompany() + "</ReceiverID>" + ls); 	
	           sb.append("</Header>" + ls);
	           sb.append("<Document>" + ls);
	           
	           String type = "order";
	      
	           sb.append("<DocumentType>" + type + "</DocumentType> " + ls);
	           sb.append("<DocumentFunction>original</DocumentFunction> " + ls);
	           sb.append("<DocumentParties>" + ls);
	       //    String regnr = getRegKood(custID);
	         	  sb.append("<BuyerParty>" + ls);
	              sb.append( "<PartyCode>" +  escape((custID)) + "</PartyCode>" + ls); 
	          //    sb.append( "<RegNum>" +  escape(regnr) + "</RegNum>" + ls); 
	              sb.append("</BuyerParty>" + ls);
	           
	              sb.append("<OrderParty>" + ls);
	              sb.append( "<PartyCode>" +  escape(custID) + "</PartyCode>" + ls);
	            //  sb.append( "<RegNum>" +  escape(regnr) + "</RegNum>" + ls); 
	              sb.append("</OrderParty>" + ls);
	           
	              sb.append("<DeliveryParty>" + ls);
	              sb.append("<PartyCode>" +  escape(custID) + "</PartyCode>" + ls); 
	             // sb.append( "<RegNum>" +  escape(regnr) + "</RegNum>" + ls); 
	         String agent = order.getAgent();
	           if(fixagent != null){
	         	  agent = fixagent;
	           }
	           
	           if(agent.equals("Laane-Eest")){
	               agent = "Lääne-Eest";
	            }
	           if(agent.equals("Louna-Eest")){
	               agent = "Lõuna-Eest";
	            }

	           
	           sb.append("<UserId>" +  agent + "</UserId>" + ls);
	           sb.append("</DeliveryParty>" + ls);
	           sb.append("</DocumentParties>" + ls);
	           sb.append("<DocumentInfo>" + ls);
	           sb.append("<DocumentSubType>MERISOFT</DocumentSubType>" + ls);
	           sb.append("<DocumentName>SALEMODULEORDER</DocumentName>" + ls );
	                  
	         	  sb.append("<RefInfo>" + ls);
	         	  if(ref != null && ref.trim().length() > 0){
	               sb.append("<SourceDocument type=\"order\">" + ls);
	               sb.append("    <SourceDocumentNum>" + escape(ref) + "</SourceDocumentNum>" + ls );
	               sb.append("</SourceDocument>" + ls);
	         	  }
	               sb.append("</RefInfo>" + ls); 
	         	
	           

	            Date dd = Calendar.getInstance().getTime();
	            Calendar cal1 = Calendar.getInstance();
		    

		        String month = (cal1.get(Calendar.MONTH) + 1) + "";
		        if (month.length() == 1) month = "0" + month;
		        String day = cal1.get(Calendar.DAY_OF_MONTH) + "";
		        if (day.length() == 1) day = "0" + day;
		        String hour = cal1.get(Calendar.HOUR_OF_DAY) + "";
		        if (hour.length() == 1) hour = "0" + hour;
		        String minute = cal1.get(Calendar.MINUTE) + "";
		        if (minute.length() == 1) minute = "0" + minute;
		        String second = cal1.get(Calendar.SECOND) + "";
		        if (second.length() == 1) second = "0" + second;
		        
		      
		        
		        String dateString = day +"-"+ month +"-"+ hour + ":" + minute;
		        
		        
	       String    sss = dateString +  (dd.getTime()+ "").substring(9) ;
	       String numa = agent.substring(0,3);
	       if (agent.contains("Tallinn 1")) {
	    	   numa = numa + "1";
	       }
	       if (agent.contains("Tallinn 2")) {
	    	   numa = numa + "2";
	       }
	       
	           sb.append("<DocumentNum>" + escape(numa + "-" + sss )+ "</DocumentNum>" + ls); 
	           sb.append("<CreatedBy>" + escape(agent)+ "</CreatedBy>" + ls); 
	           
	           sb.append("<DateInfo>" + ls);
	           sb.append("<OrderDate>" +escape(getDateString(Calendar.getInstance())) + "</OrderDate>" + ls);
	           sb.append("<DeliveryDateRequested>" +  escape(getDateString(cal)) + "</DeliveryDateRequested>" + ls); //aeg taha panna  T10:00:00
	           sb.append("</DateInfo>" + ls);
	           sb.append("</DocumentInfo>" + ls);
	           
	          // sb.append("<DocumentSumGroup>" + ls );
	          // sb.append("<Currency>EUR</Currency>" + ls);
	           
	          // sb.append("<DocumentSum>10.81</DocumentSum>" + ls );
	           //sb.append("<VAT><SumBeforeVAT>10.81</SumBeforeVAT><VATRate>20</VATRate><VATSum>2.162</VATSum></VAT>" + ls);
	        //   sb.append("<TotalSum>12.972</TotalSum>" + ls);
	          // sb.append("</DocumentSumGroup>" + ls);    	  
	           sb.append("   <DocumentItem>" + ls);



	           
	           Vector sortedLines = order.getOrderLines();
	           for (int i = 0; i < sortedLines.size(); i++) {
	               OrderLine line = (OrderLine)sortedLines.elementAt(i);
	               double ale = line.getDiscount();     
	               counter++;
	               
	               String yhik = line.getUnit();
	               if (ale == -1 ){
	                   ale = 0 ;
	                   }
	               sb.append("      <ItemEntry>" + ls);
	               sb.append("      <LineItemNum>"  + (i+1) + "</LineItemNum>" + ls);
	             
	              
	              // <GTIN>4740072222752</GTIN> 
	              // <BaseUnit>TK</BaseUnit> 
	               sb.append("         <SellerItemCode>" + line.getToode() + "</SellerItemCode>" + ls);
	               try{

	                   String ean = getEan(line.getToode());
	                   if(ean != null && !ean.trim().equals("null")){
	             	      sb.append("         <GTIN>" + ean + "</GTIN>" + ls);
	                   }
	                        
	               }catch(Exception e){
	            	   
	               }
	               
	              sb.append("         <BaseUnit>tk</BaseUnit>" + ls);
	             	  sb.append("         <ItemUnitRecord>" + ls);
	                  sb.append("                <ItemUnit>tk</ItemUnit>" + ls);
	                  sb.append("         </ItemUnitRecord>" + ls);
	                  
	              // if(smarten)
	                //  sb.append("          <ItemDescription>" + cleanLitLatEst(escape(getNimi(line.getToode()))) + "</ItemDescription>" + ls);               
	                  sb.append("         <AmountOrdered>" + escape(getNF().format(line.getKogus())) + "</AmountOrdered>" + ls);
	                
	               if(line.getDiscount() > 0){
	             	  double discprice = (100d -line.getDiscount())*line.getPrice()/100d ;
	               sb.append("         <Addition addCode=\"DSC\"><AddRate>" +
	             	  escape(getNF().format(line.getDiscount())) + "</AddRate></Addition>" + ls); 
	             
	               
	               }
	               addNumLine(); 
	           
	     /*          sb.append("         <VAT>" + ls + "                <VATRate>" +  + "</VATRate>" + ls);
	               sb.append("         </VAT>"+  ls);
	               
	               sb.append("         <ItemReserve>" + ls );
	         
	               sb.append("             <ItemReserveUnit>" + ls );
	             
	               
	               sb.append("                <ItemUnit>" +  escape(yhik.trim()) + "</ItemUnit>" + ls );
	               sb.append("                <AmountActual>" + getNF().format(line.getKogus()) + "</AmountActual>" + ls );
	               sb.append("             </ItemReserveUnit>" + ls );
	               sb.append("         </ItemReserve>" + ls );*/
	               sb.append("      </ItemEntry>" + ls);
	     
	               
	               
	           }
	           

	           sb.append("   </DocumentItem>" + ls);
	           
	     	    String comm = order.getInfo();
	             if(comm != null && comm.length()>100){
	             	comm = comm.substring(0,100);
	             }
	             
	             if(comm != null && comm.trim().length() > 0 ){
	             	  sb.append("   <AdditionalInfo>" +ls);
	                   sb.append("       <Extension extensionId=\"remarks\">" + ls);
	                   sb.append("       <InfoName>Notes</InfoName>");
	                   sb.append("            <InfoContent>" + escape(comm) + "</InfoContent>" + ls );
	                   sb.append("       </Extension>");
	                     //-<Extension extensionId="externalremarks"><InfoContent>K�esoleva arve aluseks olev n�ue on loovutatud AS-ile SEB Liising ja k�ik arvel toodud summad tuleb maksta AS-i SEB Liising arvelduskontole 10052045861006 SEB Pangas (kood 401). Kogu k�esoleva arvega seotud info tuleb teatavaks teha AS-ile SEB Liisingule.</InfoContent></Extension>
	                   sb.append("   </AdditionalInfo>" + ls);	
	             }
	           
	             
	           sb.append("</Document>" + ls);
	           sb.append("</E-Document>" + ls);
	 
	      counter = counter + cou;
	    	//
	      try{
	    	  Thread.sleep(10); 
	      }catch(Exception e){
	    	 e.printStackTrace(); 
	      }
	      
	      String fn =( Calendar.getInstance().getTimeInMillis() + "");
	      
	       fileName = escape(parent.getProps().getProperty("conv.dir") + agent) + "-" + counter + "." + fn  +  ".xml";
	      System.out.println(fileName);
	      Writer out = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(fileName), "UTF-8"));
				try {
				    out.write(sb.toString());
				} finally {
				    out.close();
				}
				
				 log.error(" File Saved " + fileName );
				/* fileName = escape(parent.getProps().getProperty("conv.dirc") + agent) + "-" + counter + "." + fn  +  ".xml";
			      System.out.println(fileName);
			       out = new BufferedWriter(new OutputStreamWriter(
						    new FileOutputStream(fileName), "UTF-8"));
						try {
						    out.write(sb.toString());
						} finally {
						    out.close();
						}
						 log.error(" copy Saved " + fileName );*/
		} catch (Exception e) {
			 log.error(" Output error 1 " + order.getCompany() );
			 log.error(e);
			e.printStackTrace();
		}
	 }
private String fileName = null;

private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    sb.append(cal.get(Calendar.YEAR) + "-");
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month + "-");

	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day);

	    return sb.toString();
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

  
  
 private void makeOutputEdis(Order order , boolean ret ){
	        
	 boolean gift = (order.getDealer() != null && order.getDealer().equals("gift"));
	 boolean brakis =  (order.getDealer() != null && order.getDealer().equals("GPP-VT-BR"));
	 System.out.println("gift or brakis " + gift  + " " + brakis );
	 	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    String reason = "100";
	      try {
	    	
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.newDocument();
	        Element root = doc.createElement("Document-Order");
	        doc.appendChild(root);

	  //      String filiaalStr = null;
	    //    String tarnijaKoodStr = null;

	        Node docheader = doc.createElement("Document-Header");
	        root.appendChild(docheader);
	        
	        Node un2 = doc.createElement("DocumentType");
	        Text unVal2 = doc.createTextNode("ORDER");
	        if(order.getDealer().equals("gift")) {
	        	unVal2 = doc.createTextNode("COMPENSATION");
	        }
	        	
	        	
	        un2.appendChild(unVal2);
	        docheader.appendChild(un2);
	       
	        
	        Node un3 = doc.createElement("DocumentSource");
	        Text unVal3 = doc.createTextNode("S");
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
	        
		

	        String number = order.getDokNr();
	       
	        
	        if(number == null || number.length() == 0){
	        	number = (Calendar.getInstance().getTimeInMillis()+"");
	        }else{
	        	 String add = (Calendar.getInstance().getTimeInMillis()+"").substring(10);
	        	 number = number+ add;
	        }
	        
	        
	        
     long uni = Calendar.getInstance().getTimeInMillis();
     Node header = doc.createElement("Order-Header");
	        
	        
	        Node nr = doc.createElement("OrderNumber");
	        Text nrVal = doc.createTextNode("MS-" +  number + "");
	        nr.appendChild(nrVal);
	        header.appendChild(nr);
	        
	        Node age = doc.createElement("SalesAgent");
	        Text ageVal = doc.createTextNode(order.getAgent());
	        age.appendChild(ageVal);
	        header.appendChild(age);
	        
	        

	        
	       // "Adl PaymTerm Code '90D'"
	    /*    System.out.println("Paym term " + order.getPaymTerm());
	        if(order.getPaymTerm() == -2){
	        	
	        	Node pt = doc.createElement("PaymentTerm");
		        Text ptVal = doc.createTextNode("90D");
		        pt.appendChild(ptVal);
		        header.appendChild(pt);
		        
		        Node pl = doc.createElement("OrderType");
		        Text plVal = doc.createTextNode("realizatsija");
		        pl.appendChild(plVal);
		        header.appendChild(pl);
		    
	        
	        }*/
	        
	        
	      //System.out.println("is Gift Sampli jaoks " + gift + " " + ((OrderLine)order.getOrderLines().get(0)).docType); 
	        
	     /*   if(gift){
	        	String samtype = ((OrderLine)order.getOrderLines().get(0)).docType;
	        	if(samtype == null || samtype.trim().length() == 0 || samtype.trim().equals("0")){
	        
	        	}
	        	Node samType = doc.createElement("SampleType");
		        Text samTypeVal = doc.createTextNode(samtype);
		        samType.appendChild(samTypeVal);
		        header.appendChild(samType);	
	        }
	        
	      /*  
	        
         if(order.getPaymTerm() > 29){
	        	
	        	Node pt = doc.createElement("PaymentTerm");
		        Text ptVal = doc.createTextNode(order.getPaymTerm() + "D");
		        pt.appendChild(ptVal);
		        header.appendChild(pt);
		      
	        }
	        */
			String comm = "";
System.out.println("info " + order.getInfo());
System.out.println("teostaja " + order.getTeostaja());
			if(order.getInfo() != null && order.getInfo().trim().length() >0){
			   comm = order.getInfo();	
			}
	 	/*	if(ret){
                String inff = order.getTeostaja();
                if (inff != null && inff.trim().length()>0 && inff.indexOf("[") !=-1){
                	reason = inff.substring(inff.indexOf("[")+1, inff.indexOf("]"));
    		    
                }
	 			
		       
	 			Node WebOrderRegion = doc.createElement("WebOrderRegion");
		        Text WebOrderRegionVal = doc.createTextNode("-1");
		        WebOrderRegion.appendChild(WebOrderRegionVal);
		        header.appendChild(WebOrderRegion);
	 			 
		        
		        Node WebOrderNote = doc.createElement("WebOrderNote");
		        Text WebOrderNoteVal = doc.createTextNode(comm);
		        WebOrderNote.appendChild(WebOrderNoteVal);
		        header.appendChild(WebOrderNote);
		        
		        Node WebOrderDeliveryType = doc.createElement("WebOrderDeliveryType");
		        Text WebOrderDeliveryTypeVal = doc.createTextNode("C");
		        WebOrderNote.appendChild(WebOrderDeliveryTypeVal);
		        header.appendChild(WebOrderDeliveryType);
	 			 
	 		 }*/
		        
		      //  <Remarks>preču atgriešana. jāaizbrauc ar loģistiku pakaļ!</Remarks>
		       // <Note1>preču atgriešana. jāaizbrauc ar loģistiku pakaļ!</Note1>
		       // <WebOrderRegion>-1</WebOrderRegion>
		       // <WebOrderNote>preču atgriešana. jāaizbrauc ar loģistiku pakaļ!</WebOrderNote>
		       // <WebOrderDeliveryType>C</WebOrderDeliveryType>
		        
		        
		        
		        
	 		System.out.println("Remarks " + comm);
	        
	        
	      

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

 /*  if(brakis){
	   Node ware = doc.createElement("Warehouse");
	   Text wareVal = doc.createTextNode(order.getDealer());
	   ware.appendChild(wareVal);
	   header.appendChild(ware); 
   }*/
   
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


	        Node expectedDeliveryTime = doc.createElement("ExpectedDeliveryTime");
	        Text expectedDeliveryTimeVal = doc.createTextNode("00:00");
	        expectedDeliveryTime.appendChild(expectedDeliveryTimeVal);
	        header.appendChild(expectedDeliveryTime);

	        
	        Node func = doc.createElement("DocumentFunctionCode");
	        Text funcVal = doc.createTextNode("O");
	        func.appendChild(funcVal);
	        header.appendChild(func);
	        

 			Node com = doc.createElement("Remarks");
	        Text comVal = doc.createTextNode(comm);
	        com.appendChild(comVal);
	        header.appendChild(com);
	       
	        
	        
 		
 		 
		
		Node WebOrderRegion = doc.createElement("WebOrderRegion");
        Text WebOrderRegionVal = doc.createTextNode("-1");
        WebOrderRegion.appendChild(WebOrderRegionVal);
        header.appendChild(WebOrderRegion);
			 
        
        Node WebOrderNote = doc.createElement("WebOrderNote");
        Text WebOrderNoteVal = doc.createTextNode("null");
        WebOrderNote.appendChild(WebOrderNoteVal);
        header.appendChild(WebOrderNote);
        
        Node WebOrderDeliveryType = doc.createElement("WebOrderDeliveryType");
        Text WebOrderDeliveryTypeVal = doc.createTextNode("L");
        WebOrderDeliveryType.appendChild(WebOrderDeliveryTypeVal);
        header.appendChild(WebOrderDeliveryType);
 		 

	        
	        comment = null;   
	        
	        root.appendChild(header);

String custname = this.custName(order.getCompany());
Node dparties = doc.createElement("Document-Parties");
root.appendChild(dparties);

	        Node parties = doc.createElement("Order-Parties");
	        Node seller = doc.createElement("DeliveryPoint");

	        Node ise = doc.createElement("CodeBySupplier");
	        Text nimiVal = doc.createTextNode(order.getCompany());
	        ise.appendChild(nimiVal);
	        
	        Node iseName = doc.createElement("Name");
	        Text iseNameVal = doc.createTextNode(custname);
	        iseName.appendChild(iseNameVal);
	        
	   
	        
	        Node ise2 = doc.createElement("ILN");
	        Text nimiVal2 = doc.createTextNode(order.getCompany());
	        ise2.appendChild(nimiVal2);
	        
	        seller.appendChild(ise2);
	        seller.appendChild(iseName);
	        
	        seller.appendChild(ise);
	     

	        
	        
	        Node sell = doc.createElement("Seller");
            Node sellX = doc.createElement("Name");
	        Text sellXVal = doc.createTextNode("Fazer Latvija SIA");
	        sellX.appendChild(sellXVal);
	     
	        Node iseX22 = doc.createElement("ILN");
	        Text nimiXVal22 = doc.createTextNode("4750212000003");
	        iseX22.appendChild(nimiXVal22);
	        
	        sell.appendChild(sellX);
	        sell.appendChild(iseX22);
	        
	        Node buyer = doc.createElement("Buyer");
            Node iseX2 = doc.createElement("Name");
	        Text nimiXVal2 = doc.createTextNode(custname);
	        iseX2.appendChild(nimiXVal2);
	        
	        Node iseX = doc.createElement("CodeByBuyer");
	        Text nimiXVal = doc.createTextNode(order.getCompany());
	        iseX.appendChild(nimiXVal);
	        buyer.appendChild(iseX2);
	        buyer.appendChild(iseX);

	        parties.appendChild(buyer);
	        parties.appendChild(sell);
	        parties.appendChild(seller);

	        
	        root.appendChild(parties);



	        //RIDADE LISAMINE
	        Node read = doc.createElement("Order-Lines");

	        int coun = 0;
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
	          	
	        	  
	        	  coun++;
	        	  addLine(line, read, doc, coun , order, ret , reason , custname,gift);
	          }
	          }

	        root.appendChild(read);

	        
	        Node summary = doc.createElement("Order-Summary");
	        
	      
	        Node TotalLines = doc.createElement("TotalLines");
	        Text TotalLinesVal = doc.createTextNode(coun + "");
	        TotalLines.appendChild(TotalLinesVal);
	        summary.appendChild(TotalLines);

	        Node TotalOrderedAmount = doc.createElement("TotalOrderedAmount");
	        Text TotalOrderedAmountVal = doc.createTextNode(sum + "");
	        TotalOrderedAmount.appendChild(TotalOrderedAmountVal);
	        summary.appendChild(TotalOrderedAmount);
	        
	        Node TotalNetAmount = doc.createElement("TotalNetAmount");
	        Text TotalNetAmountVal = doc.createTextNode((sum * 1.2) + "");
	        TotalNetAmount.appendChild(TotalNetAmountVal);
	        summary.appendChild(TotalNetAmount);
	       
	        root.appendChild(summary);
	 	
	      String fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr().trim() + (uni + "").trim() +  ".xml";
	      
	      if(gift) {
	    	  fileName = parent.getProps().getProperty("conv2.dir") + order.getDokNr().trim() + (uni + "").trim() +  ".xml"; 
	      }
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
	      
	        if(gift){
	        //	sendConfirmEmail( comment,order, custname );
	         }
	         
		
	      } catch (Exception e) {
			e.printStackTrace();
		}
	 }
 
    private double sum = 0;
   private String comment = null;
   
   private void addLine(OrderLine line, Node read, Document doc,int counter ,Order order , boolean ret 
		   ,String reason,String custname, boolean gift) throws SQLException{
	   

	
	     Node rida = doc.createElement("Line");
	     read.appendChild(rida);

	     Node ridaitem = doc.createElement("Line-Item");


	     Node linenum = doc.createElement("LineNumber");
	     Text linnum = doc.createTextNode(counter+"");
	     linenum.appendChild(linnum);
	     ridaitem.appendChild(linenum);

	     String toode  = line.getToode();
	     
	     Node toote_ean = doc.createElement("EAN");
	     Text toote_eanVal = doc.createTextNode(getEan(toode));
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
        
	     String nimi = this.getProdNimi(toode);
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
	     if(gift){
	    	 gg = "1";  	
	     }
	     Node sample = doc.createElement("Sample");
	     Text sampleVal = doc.createTextNode(gg);
	     sample.appendChild(sampleVal);
	     ridaitem.appendChild(sample);
	     
	     if (ret){
	    	 System.out.println("Partii" + line.getPartii()+" " + reason);
	    	 Node ReturnReason = doc.createElement("ReturnReason");
		     Text ReturnReasonVal = doc.createTextNode(reason);
		     ReturnReason.appendChild(ReturnReasonVal);
		     ridaitem.appendChild(ReturnReason);
		    
if(line.getPartii() !=null && line.getPartii().length() >0){
String text = line.getPartii();
if(text.indexOf("\"")!=-1){
	text = text.substring(text.indexOf("\""));
	Node ExciseStamp = doc.createElement("ExciseStamp");
    Text ExciseStampVal = doc.createTextNode(text.replace('"', ' ').replace('"', ' ').trim());
    ExciseStamp.appendChild(ExciseStampVal);
    ridaitem.appendChild(ExciseStamp);
}
		     
		 	
		 }		    
	 
	     }else  if(line.getPartii() != null && line.getPartii().trim().length() > 0 ){
 		    Node toote_comm = doc.createElement("LineComment");
 		   if(gift){
 			  toote_comm = doc.createElement("Samples_comment"); 
 		   }
 		    Text toote_commVal = doc.createTextNode(line.getPartii().trim());
 		    toote_comm.appendChild(toote_commVal);
 		    ridaitem.appendChild(toote_comm);
 		    
 		   if(comment == null){
		    	comment = "";
		    }
 		  comment = comment + " \n\nManager : "  +order.getAgent() +"\nProduct name : " + nimi + "\nRequested price : " +line.getPrice() 
		    + " EUR\nRequested Discount : " + line.getDiscount() + "%\nOrdered amount : " +
		    + line.getKogus() + "\nComment : " +  line.getPartii().trim() ;
		}
  
	     
	     
	     
	     rida.appendChild(ridaitem);
	     addNumLine(); 
	   }
   private void sendCommentEmail(String comment,String doknr,String custid, String nimi , String chain){
	   	try{
	   		
	   		if(comment != null && comment.trim().length() >0){
	   			
	   			System.out.println(comment);
	   			Email mail = new Email(null);
		  		   
	   			
		  	  		mail.setSMTP(parent.getProps().getProperty("smtphost"));
		  	  		mail.setFilesTo(parent.getProps().getProperty("operatoremail"));
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		mail.setSubjectFilesTo("Price request " + custid);
		  	  		mail.setEncoding("utf-8");
		  	  		mail.setBody("\nOrder :" +  doknr + " \nCustomer code :" + custid +  "\nCustomer Name :" + nimi + "\n" +
		  	  			  "Customer Chain :" + chain + "\n" +  "\n" + comment);
		  	  		mail.setSendNotice(false);
		  	  		mail.send();
		  	  		System.out.println("\nOrder Comment " +  doknr + " " + custid + "\n\n" +
		  	  				  "\n >>>>>" + comment + "<<<<<");
		  				
	   		}
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	   }
   
   private void sendConfirmEmail(String comment, Order ord , String cuname ){
	   	try{
	   		
	   		
	   			
	   			System.out.println(comment);
	   			Email mail = new Email(null);
		  		   String sub = "Compensation request " + ord.getCompany() + " " + cuname;
		  	
		            mail.setSMTP(parent.getProps().getProperty("smtphost"));
		            mail.setUser(parent.getProps().getProperty("smtpuser"));
		            mail.setPass(parent.getProps().getProperty("smtppass"));
		            mail.setPort(parent.getProps().getProperty("smtpport"));
		  	  		mail.setFilesTo("aivar@mobilejava.ee");//pasutijumi@fazer.com
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		mail.setSubjectFilesTo(sub + ord.getCompany()+ " " + cuname);
		  	  		mail.setEncoding("utf-8");
		  	    	mail.setBodyHtml(getOOSEmailText(ord,comment, ord.getAgent() , cuname ));
		  	  		mail.setSendNotice(false);
		  	  		mail.send();
		  	  
		  	  		System.out.println("Request Sent");
		  	
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	   }

   public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}
  
   private String getOOSEmailText( Order ord, String comment ,String agent, String custn) {
 		int i = 0;
 		int j = 0;
 		Vector lines = ord.getOrderLines();
 		double total = 0;
 		String custname = custn;

 		String comp = custn;
 		if (comp == null) {
 			comp = "";
 		}
 		
 		String name = "";
 		String aadr = "";
 		String agname = "";
 		
 		if(custname.indexOf("|") != -1) {
 			StringTokenizer st = new StringTokenizer(custname,"|");
 			name = st.nextToken();
 			aadr = st.nextToken();
 			agname = st.nextToken();
 		}

 		
 		String axat = "";
 		
 		StringBuffer sb = new StringBuffer();
 		String subtext = (axat +  " Fazer LV ' \n &emsp;&emsp;&emsp;&emsp; \n").trim();
 		String regno = ("&emsp;&emsp;&emsp;&emsp; <br>" + agname);
 		
 			subtext = subtext + " " + regno;
 		
 		
 		
 		Calendar shdate = ord.getShipCal();
 		Calendar cal = Calendar.getInstance();
 		if (shdate != null) {
 			cal.setTime(shdate.getTime());
 		} else {
 			cal.add(Calendar.DAY_OF_MONTH, 1);
 		}
 		
 		
 		
 		sb.append("<font face=\"Arial\" size=\"2\" color=\"#E42217\"> Date "
 				+ Converter.getDDpMMpYYYYpString(null) + "</font><br> ") ;
 		sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\"><b></b> " + subtext + "</font>") ;
 		sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\">&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;</font><br> ") ;
        sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\"><b>Customer:</b> " + name +"<br>" + aadr +"<br>" +  "</font> ") ;
 		sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\">&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;" + "</font><br><br>") ;
 														sb.append("</font>");
 		
 		sb.append("<font face=\"Arial\" size=\"4\"  color=\"#382D2C\"><b>Compensation request " + axat + "</b><br><br>" );
 														
 		sb.append("	<style type=\"text/css\">");
 		sb.append("table.tb-table {");
 		sb.append("border: 1px inset #98bf21; font-family: Arial, Helvetica, sans-serif");
 		sb.append("font-size: 12px;} ");
 		sb.append(".tb-table td {");
 		sb.append("	padding: 4px;");
 		sb.append("	margin: 3px;");
 		sb.append("		border: 1px inset #FF3399;}");
 		sb.append(".tb-table th {");
 		sb.append("	background-color: #FFF; ");
 		sb.append("color: #00000;");
 		sb.append("		font-weight: bold;}");
 		sb.append("</style><table class=\"tb-table\">" );
 		sb.append("<tr><th>Nr.</th><th>EAN</th><th>Code</th><th>Description</th><th>Amount Cs</th>"
 				+ "</tr>" );

 		
 			for (; i < lines.size(); i++) {
 				OrderLine ol = (OrderLine) lines.elementAt(i);

 				  String toode = ol.getToode();
 				
 				Product prod = getProduct(toode);
 				System.out.println("aaa " + prod + " " + toode);
 				String ean = prod.getEAN();
 				if (ean == null) {
 					ean = prod.getID();
 				}
 		
 				
 			         String disc = "0";
 				        double disco = 0;
 				        if(ol.getDiscount()> 0 && ol.getDiscount() < 99){
 				        	disc = ol.getDiscount()+"";
 				        	disco = ol.getDiscount();
 				        }
 		     
 				     double jaga = 1;
 				        if(ol.getPartii() != null && ol.getPartii().indexOf("|") != -1){
 				        	jaga = Double.parseDouble(ol.getPartii().substring(ol.getPartii().indexOf("|")+1).trim());
 				        }
 				        double sum    = Double.parseDouble(nf2.format(ol.getPrice()/jaga + 0.001));
 	                    
 							   total  = total + ol.getKogus();
 						
 				        
 				sb.append("<tr><td>" + (i+1) + ".</td><td>" + ean + 
 							"</td><td>" + prod.getID() + 
 							"</td><td>" + prod.getName() + 
 							"</td><td>" + ol.getKogus() + 
 							"</td></tr>" );
 			
 				j++;
 			}

 			
 	
 			sb.append("<tr><th></th><th>" +
 					"</th><th>" + 
 					
 				 
 					"</th><td>Total units:" +
 					"</td><td>" + nf.format(total) +
 						" </td></tr>" );
 			
 			sb.append("<tr><th></th><th>" +
 					"</th><th>" + 
 					"</th><th>" + 
 					"</th><th>" + 
 					"</th><th>" + 
 					"</th><th>" +  
 					"</th></tr>" );
 		
 		  System.out.println(sb.toString());
 		return sb.toString();
 	}

    
   private NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);
   
private Product getProduct(String prodid){
Product prod = null;


String cmd = "  SELECT inv.itemid, inv.itemname AS nimetus, ERPOLDITEMID as oldcode , inv.taxpackagingqty AS kastis,inv.standardpalletquantity AS alusel, disc.amount AS price \r\n" + 
		"    		 , bar.itembarcode AS ean  ,bar2.itembarcode as ean2,inv.ITEMSUBGROUPID AS grupp,inv.SPIBRANDID AS grupp2, origcountryregionid as coun,mod.unitid As unit, '21' as vat,mod.LineDisc as alegr ,inv.alcstrengthgroupid as gruppale  \r\n" + 
		"    		  , ITEMGROUPID as type ,lowestqty as mini , multipleqty as opti ,NEWITEM as uus ,sellout as out , topitem as tops , topoffitem as tops2, giftbox as gift \r\n" + 
		"    		 ,  itemfreetextannotation  as annot , SPIDONTCONTROLMINAMOUNTMS as nocount  FROM inventtable inv \r\n" + 
		"    		  left join InventItemSalesSetup s on s.itemid = inv.itemid and s.dataareaid=inv.dataareaid left join pricedisctable as disc on inv.itemid=disc.itemrelation and disc.dataareaid=inv.dataareaid \r\n" + 
		"    		  and disc.module in ( 0 , 1 )  and disc.relation=4  and disc.itemcode=0 and disc.accountcode=1 and accountrelation = 'DPD'  \r\n" + 
		"    		 and (((disc.todate > getdate() or year( disc.todate ) = 1900) and   (disc.fromdate < getdate()+1 or year( disc.fromdate ) = 1900 )) or \r\n" + 
		"    		  ((disc.fromdate < getdate()+1 or year( disc.todate ) = 1900 ) and year( disc.todate ) = 1900))  \r\n" + 
		"    		  left join inventitembarcode bar   on bar.dataareaid = inv.dataareaid  and  bar.itemid = inv.itemid  \r\n" + 
		"    		  and bar.barcodesetupid ='EANBottle' and len(bar.itembarcode)>8  and bar.useforprinting = 1\r\n" + 
		"			  left join inventitembarcode bar2   on bar2.dataareaid = inv.dataareaid  and  bar2.itemid = inv.itemid  \r\n" + 
		"    		  and bar2.barcodesetupid ='EANCase' \r\n" + 
		"			  left JOIN inventtablemodule mod ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid  \r\n" + 
		"    		  WHERE  inv.dataareaid='ct'    AND mod.dataareaid='ct'  \r\n" + 
		"    		  AND moduletype=2 and active = 1 and inv.ITEMID = '" + prodid + "' \r\n" + 
		"    		  order by inv.itemid,  disc.module , disc.todate desc ";


cmd = "   SELECT inv.itemid,(inv.itemname) AS nimetus, qtyinbox AS kastis,  mod.price AS price, itembarcode AS ean,itemgroupid AS grupp ,\r\n" + 
		" astuppersegment AS grupp2,mod.unitid AS unit \r\n" + 
		"    		                FROM inventtable inv \r\n" + 
		"    		                 INNER JOIN inventtablemodule mod ON mod.itemid = inv.itemid  and  mod.dataareaid= inv.dataareaid\r\n" + 
		"    		                       LEFT JOIN inventitembarcode bar ON bar.itemid = inv.itemid  and  bar.dataareaid= inv.dataareaid\r\n" + 
		"    							    and bar.barcodesetupid='STD' AND bar.dataareaid='eee'  \r\n" + 
		"    		                         WHERE   inv.dataareaid='eee' AND mod.dataareaid='eee'   AND moduletype=2  AND ASTITEMFORSALE = 1 \r\n" + 
		"    		                            AND len(inv.itemname)>0    and inv.ITEMID = '" + prodid + "' " +  
		"    		                                order by inv.itemid "  ;

try {


Connection con = this.getNewConnection();

Statement stmt =  con.createStatement();
ResultSet rs = stmt.executeQuery(cmd);
while(rs.next()){



String kood = rs.getString("itemid").trim();
System.out.println(" kood " + kood + " " + prodid);
prod = new Product(kood);
String nimetus =  rs.getString("nimetus").trim() ;
prod.setInCases(rs.getDouble("kastis"));

double price = rs.getDouble("price"); 

if(price > 0){
prod.setPrice(price);
}


String ean = rs.getString("ean");

if(ean != null && ean.trim().length()>0){
prod.setEAN(ean);
}

String grupp = rs.getString("grupp");
String abi = grupp;
if(grupp!=null && grupp.trim().length()>0){
if(abi.length() > 4){
		 abi = abi.substring(0, 4);
	 }else{
		 if(abi.length() != 4){
			 abi = abi + "0";	 
		 }
		 if(abi.length() != 4){
			 abi = abi + "0";	 
		 }
	 }

prod.setGroup(abi);  

}

String unit = rs.getString("unit");

if(unit != null && unit.trim().length()>0){
prod.setUnit(unit);
}

prod.setName(nimetus);


}
rs.close();
stmt.close();
con.close();

}catch (Exception e ){
e.printStackTrace();
}

return prod ;
}


	   private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);


 
  public String getDriverVersion(){
    return "AxaFazerLV";
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


  private String url = "jdbc:odbc:axa";
  private Connection con;
  private int counter = 0;
  private ImportPanel parent;
  private boolean isAmount = false;
  private StringBuffer troubles= new StringBuffer();

  public String getTrouble() {
  	// TODO Auto-generated method stub
  	return troubles.toString();
  }
  
}
