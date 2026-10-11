package eevaeksport.imp.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

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

public class BiovelaUusImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public BiovelaUusImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
   if(parent.getProps().getProperty("wsdl.url") != null){
	   weburl = parent.getProps().getProperty("wsdl.url");
   }
    try {
      Class.forName(databaseDriverName).newInstance();
    } catch (Exception e){
       parent.showError(e);
    }
    
    try {
    	BufferedReader in = new BufferedReader(new FileReader("vips.txt"));
		String line;
		 vips = new HashSet();
		while ((line = in.readLine()) != null) {
			vips.add(line);
		}
		in.close();
		System.out.println("Vips " + vips.toString());
      } catch (Exception e){
        // parent.showError(e);
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
  
  
  private HashSet vips = null;
  
  
  
  private Vector splitOrder (Order order){
	  Vector result = new Vector();
      
	  Order ordBio = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ordUtena = new Order((order.getCompany()),order.getAgentFirstName());
	  Order ordMilk = new Order((order.getCompany()),order.getAgentFirstName());

	     
	  ordBio.setAgentCode(order.getAgentCode());
	  ordBio.setDokNr(order.getDokNr());
	  ordBio.setInfo(order.getInfo());
	  ordBio.setTeostaja((order.getTeostaja()));
	  ordBio.setChanged(order.isChanged());
	  ordBio.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordBio.setStatus(order.getStatus());
	  ordBio.setShipTime(order.getShipTime());
	  ordBio.setWarehouse("biovela");
	     
	  
	  
	  ordUtena.setAgentCode(order.getAgentCode());
	  ordUtena.setDokNr(order.getDokNr());
	  ordUtena.setInfo(order.getInfo());
	  ordUtena.setChanged(order.isChanged());
	  ordUtena.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordUtena.setStatus(order.getStatus());
	  ordUtena.setShipTime(order.getShipTime());
	  ordUtena.setWarehouse("utena");
	
	
	  ordMilk.setAgentCode(order.getAgentCode());
	  ordMilk.setDokNr(order.getDokNr());
	  ordMilk.setInfo(order.getInfo());
	  ordMilk.setTeostaja((order.getTeostaja()));
	  ordMilk.setChanged(order.isChanged());
	  ordMilk.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordMilk.setStatus(order.getStatus());
	  ordMilk.setShipTime(order.getShipTime());
	  ordMilk.setWarehouse("milk");  
	
	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	  
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
		
		     String itemid = lin.getToode();
		    String ware = null;
			try {
				ware = identfyWare(itemid);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		     if(ware.equals("biovela")){  
		    		 ordBio.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);	 
		     	 
		         }else if(ware.equals("utena") ){
			    		ordUtena.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     
			         }  else if(ware.equals("milk") ){
				    	
				     	ordMilk.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
				     }
		     
		     

	  }





	  if(ordBio.getOrderLines() != null && ordBio.getOrderLines().size()>0){
		  result.add(ordBio);  
	  }

	  if(ordUtena.getOrderLines() != null && ordUtena.getOrderLines().size()>0){
		  result.add(ordUtena);  
	  }
	  
	  if(ordMilk.getOrderLines() != null && ordMilk.getOrderLines().size()>0){
		  result.add(ordMilk);  
	  }
	 
	 System.out.println("Result =" +order.getDokNr()
			 + " "+ result.size());

	 return result ;
  }




  
  
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  
  public String identifyCustomer(String id,String regnr)  {
	  
    return null;
  }


  
  public String identifyProduct(String ean, String prodID) throws SQLException{ 	  
    return null;
}
  
  

  
  
  private String identfyWare(String kood) throws SQLException{
 	 String res = null;
 	 if(kood.startsWith("60")){
   	  res = "utena";
     }else if(kood.startsWith("61")){
      res = "biovela";
     }else{
   	  res = "milk";
     }
 	 return res;
  }

  

  public void makeOrder(Order order) throws SQLException{
       counter = 0;
       
       if (signatures == null){
    	   signatures = new Vector();
       }
       
       String list = parent.getProps().getProperty("splitlist");
       
       boolean split = false;
       
       //kui on split listis siis splitime kui mitte siis ei spliti .
       if(list != null && list.indexOf(order.getAgent()) != -1){
    	   split = true ;
       }
       
       //kui on Vip klient siis ka splitime 
       if(vips != null && vips.contains(order.getCompany())){
    	   split = true ;
       }
       
       if(split){
    	   Vector splitted =  splitOrder(order);
    	   System.out.println("tellimusi firmadesse " + splitted.size());
             Enumeration en = splitted.elements();
    		  while(en.hasMoreElements()){
    			  Order ord = (Order)en.nextElement(); 
    			  String signature = ord.getSignature();
    			  if(signatures.contains(signature)){
    				  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
    				    		  
    			  }else{
    				  makeOutput(ord);  
        			  signatures.add(signature);
        			  this.trouble = null;
        			  
    			  }
    			   
    		  }
       }else{
    	   System.out.println("tellimus MPLG-sse ainult");
    	   String signature = order.getSignature();
    	   System.out.println("Signature " + signature + " " + signatures);
    	   if(signatures.contains(signature)){
		    
    		   this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
		    
    	   }else{
				  makeOutput(order);  
		    	   signatures.add(signature);
		    	   this.trouble = null;
    	   }
    	      
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
	
  
  
  private String weburl = "http://QAD-RH02.biovela.lt:8080/qxi/services/QdocWebService";
  
  private String getDateNiceString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    String year =   cal.get(Calendar.YEAR) + "";
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

	    String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));

	    String minute = String.valueOf(cal.get(Calendar.MINUTE));

	    String second = String.valueOf(cal.get(Calendar.SECOND));
	 
	   sb.append(  year + "-");
	    if (month.length() == 1){
		     sb.append("0");
		   }
		     sb.append(month  + "-");
	    
	    if (day.length() == 1){
	      sb.append("0");
	    }
	     sb.append(day);
	  //28.06.2010 20:00:33Y-m-d\TH:i:s n2002-05-30T09:00:00
	   
	     if (hour.length() == 1){
		      hour = "0" + hour ;
		    }
	    /* sb.append("T" +hour + ":");

	     if (minute.length() == 1){
		      sb.append("0");
		    }
	     sb.append(minute + ":");
	     if (second.length() == 1){
		      sb.append("0");
		    }
	     sb.append(second);*/
	     
	    return sb.toString();
	  }
  
  
  private String getWeb(String req, String req2){
	  Content cnt = null;
	  try{
		  System.out.println("weburl " + weburl);
		  cnt = Request.Post(weburl)
			.bodyString(req, ContentType.create("text/xml", "UTF-8"))
			.addHeader("SOAPAction", "")
		   .execute().returnContent();
	  }catch(Exception e){
		  
		  trouble = "NOT IMPORTED ORDER \n" + req2;
			 
		  e.printStackTrace();
	  }
	  return cnt.asString().replaceAll("qdoc:", "");
	 	
  }
  
  
  
  
  
  /*
    private void makeOutputOld(Order order){
    	
	 	try {
	 
	 		String comm = "";
	 		if(order.getInfo() != null && order.getInfo().trim().length() >0){
	 			comm =  order.getInfo() ;
	 		}
	 		System.out.println("Comment " + comm);
			
	 		String doknr = order.getDokNr();
	 		
	 		
	 	    String  order_xml = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"> " +
  " <soap:Header>" +
	 	   "   <MessageID xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.w3.org/2005/08/addressing\" xsi:type=\"xs:string\">480c1566-4229-42d3-8007-7aa309763650</MessageID> " +
	 		   "     <To xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance \" xmlns=\"http://www.w3.org/2005/08/addressing\" xsi:type=\"xs:string\">urn:services-qad-com:D012LT</To>" +
	 			  " <wsa:ReferenceParameters xmlns:qcom=\"urn:schemas-qad-com:xml-services:common\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" +
	 	   " <qcom:suppressResponseDetail>true</qcom:suppressResponseDetail>" +
      " </wsa:ReferenceParameters>" +
    " </soap:Header> " +
    " <soap:Body> " +
    " <maintainSalesOrder xmlns=\"urn:schemas-qad-com:xml-services\"> " +
    " <dsSessionContext xmlns=\"urn:schemas-qad-com:xml-services:common\" xmlns:qdoc=\"urn:schemas-qad-com:xml-services\">" +
      " <ttContext> " +
        " <propertyQualifier>QAD</propertyQualifier> " +
          "  <propertyName>domain</propertyName> " + 
          "  <propertyValue>D012LT</propertyValue> " +
          "  </ttContext> " +
        " <ttContext> " +
        " <propertyQualifier>QAD</propertyQualifier> " +
          " <propertyName>version</propertyName> " +
          " <propertyValue>eB2_2</propertyValue> " +
          " </ttContext> " +
        "  </dsSessionContext> " +
      " <dsSalesOrder xmlns:ns2=\"urn:schemas-qad-com:xml-services:common\"> " +
      " <salesOrder> " +
    " <operation>A</operation> " +
        " <soNbr/> " +
        " <soCust>" + order.getCompany() + "</soCust>  " +
        " <soShip>" + order.getCompany() + "</soShip>  " +
        " <soOrdDate>" + getDateNiceString(Calendar.getInstance()) + "</soOrdDate> " +
        " <soDueDate>" + getDateNiceString(order.getShipCal()) + "</soDueDate>  " +
        " <soPo>" + doknr + "</soPo> " +
        " <soSlspsn>" + order.getAgent() + "</soSlspsn>" +
        " <multSlspsn>false</multSlspsn>";
        
        
        if(comm.length() > 0){
        	if(comm.length() > 75 ){
        		comm = comm.substring(0,75);
        	}
        	 order_xml = order_xml + "<socmmts>true</socmmts><salesOrderTransComment><operation>A</operation>";
        	 order_xml = order_xml + "<cmtCmmt>" + comm + "</cmtCmmt></salesOrderTransComment>"; 
        }
	 	   


	 	
        order_xml = order_xml + " <confirm>true</confirm> " +
        " <calcFr>false</calcFr> " +
        " <dispFr>false</dispFr> " +
        " <ediAck>false</ediAck> " +
        " <soPrintSo>true</soPrintSo> " +
        " <soPrintPl>true</soPrintPl> " +
        "  <printIh>true</printIh> " +
        " <ediIh>false</ediIh> " +
        "      <soPartial>true</soPartial>" ; 
	         
            // comm;
	       
	     	 Vector lines = order.getOrderLines();
		      for (int i = 0; i < lines.size(); i++) {
		    	  OrderLine line = (OrderLine) lines.elementAt(i);
		      
		         order_xml = order_xml + "<salesOrderDetail><operation>A</operation>";
		    	  order_xml = order_xml + "<sodPart>" + line.getToode() + "</sodPart>";
			      order_xml = order_xml +"<sodQtyOrd>"+ line.getKogus() + "</sodQtyOrd>";
			      if(line.getDiscount() > 0){
                        order_xml = order_xml +"<sodListPr>"+ line.getPrice() + "</sodListPr>";
                        order_xml = order_xml +"<discount>"+ line.getDiscount() + "</discount>";
			      }
		    	 
			      order_xml = order_xml + "<sodConfirm>true</sodConfirm><sodDetailAll>false</sodDetailAll>" +
		    	  		"</salesOrderDetail>";
			 counter++;
	      }
	 
		  
		      
	 	
	 	order_xml = order_xml + "</salesOrder>";
	 	order_xml = order_xml + "</dsSalesOrder>";
	 	order_xml = order_xml + "</maintainSalesOrder>";
	 	order_xml = order_xml + "</soap:Body>";
	 	order_xml = order_xml + "</soap:Envelope>";
	    System.out.println("output " +order_xml); 
	 	
	 		
	 	try {
	 		
	 		  String fn = doknr;
		      if(doknr.trim().length()== 0){
		    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
		      }
		    
		      
		      String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + fn  +  ".xml";
		      System.out.println(fileName);
		       
		      Converter.saveFileInUTF8(fileName, order_xml);
		      
	 	
	 		String xml = getWeb(order_xml);		
	 		System.out.println("Tulemus " + xml);
	 		
	 	
       

	      
	      setNumAddedLines(counter); 
	      
	    
	      fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + fn  +  "-response.xml";
	      System.out.println(fileName);
	       
	      Converter.saveFileInUTF8(fileName, xml);
	 	} catch (Exception e) {
	 		e.printStackTrace();
	 	}

		} catch (Exception e) {
			e.printStackTrace();
		}
	 }*/
	  
  
 private boolean isBeforeDay(Calendar cal , Calendar ship){
	  
	  Calendar cc = Calendar.getInstance();
	  cc.setTimeInMillis(cal.getTimeInMillis());
	  cc.set(Calendar.HOUR, 1);
	  cc.set(Calendar.MINUTE, 1);
	  cc.set(Calendar.SECOND, 1);
	  cc.set(Calendar.MILLISECOND, 1);
	  
	  Calendar ss = Calendar.getInstance();
	  ss.setTimeInMillis(ship.getTimeInMillis());
	  ss.set(Calendar.HOUR, 1);
	  ss.set(Calendar.MINUTE, 1);
	  ss.set(Calendar.SECOND, 1);
	  ss.set(Calendar.MILLISECOND, 1);
	  //kui cal on enne kui ship
	  if(cc.getTime().before(ss.getTime())){
		  return true;
	  }
	  
	  return false;
  }
	

 private boolean isToday(Calendar cal){
	  
	  Calendar cc = Calendar.getInstance();
	  if(cc.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)){
		  return true;
	  }
	  
	  return false;
 }
 
  
  
  private void makeOutput(Order order){
  	
	 	try {
	 
	 		
	 	if("true".equals(parent.getProps().getProperty("testing"))){
	 		System.out.println("Testing ");
	 	}else{
	 	
	 		String comm = "";
	 		if(order.getInfo() != null && order.getInfo().trim().length() >0){
	 			comm =  order.getInfo() ;
	 		}
	 		System.out.println("Comment " + comm);
			
	 		String doknr = order.getDokNr();
	 		 Calendar  shipDate = order.getShipCal();

	 		  Calendar cal = Calendar.getInstance();
	            if(isToday(shipDate)){
	            	 	if(shipDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
	   	            		shipDate.add(Calendar.DATE, 2);
	   	            	}else{
	   	            		shipDate.add(Calendar.DATE, 1);
	   	            	}
	   	             	
	            }
	         
		           if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
		        	   cal.add(Calendar.DATE , 2);
		           }else{
		        	   cal.add(Calendar.DATE , 1);
		           }
		           
		           if (shipDate != null && isBeforeDay(cal,shipDate) ) {			        	
		               cal = shipDate;
		               }        
	           
	         String domain = "MPLG";
	 		if(order.getWarehouse().endsWith("biovela")){
	 			domain = "BIOV";
	 		}
	 		if(order.getWarehouse().endsWith("utena")){
	 			domain = "UTME";
	 		}  
	 	    String  order_xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:biovela:merisoft:sales-orders\"> " +
	 		  "<soapenv:Header/> " +
	 		  "<soapenv:Body>" +
	 		   "  <urn:import> " +
	 		    "    <urn:messageId>" + Calendar.getInstance().getTimeInMillis() + "</urn:messageId> " +
	 		    "    <urn:domain>" + domain + "</urn:domain> " +
	 		     "   <urn:salesOrder> " +
     
      " <urn:soCust>" + order.getCompany() + "</urn:soCust>  " +
      " <urn:soShip>" + order.getCompany() + "</urn:soShip>  " +
      " <urn:soOrdDate>" + getDateNiceString(Calendar.getInstance()) + "</urn:soOrdDate> " +
      " <urn:soDueDate>" + getDateNiceString(cal) + "</urn:soDueDate>  " +
      " <urn:soPo>" + doknr + "</urn:soPo> " +
      " <urn:soCurr>EUR</urn:soCurr> " +
      " <urn:soSlspsn>" + order.getAgent() + "</urn:soSlspsn>" ;
      
      
      if(comm.length() > 0){
    	  String comm2 = null;
      	if(comm.length() > 75 ){
      		comm = comm.substring(0,75);
      		comm2 = comm.substring(75);
      	}
      	 order_xml = order_xml + "<urn:salesOrderTransComment>" + comm + "</urn:salesOrderTransComment>";
      	 if(comm2 != null){
      		 order_xml = order_xml + "<urn:salesOrderTransComment>" + comm + "</urn:salesOrderTransComment>";
         }
      }
      
	     	 Vector lines = order.getOrderLines();
		      for (int i = 0; i < lines.size(); i++) {
		    	  OrderLine line = (OrderLine) lines.elementAt(i);
		      
		         order_xml = order_xml + "<urn:salesOrderDetail>";
		         order_xml = order_xml +  "<urn:line>" + (i+1) + "</urn:line>";
			     order_xml = order_xml +  "<urn:sodPart>" + line.getToode() + "</urn:sodPart>";
			      order_xml = order_xml +  "<urn:sodQtyOrd>" + line.getKogus() + "</urn:sodQtyOrd>";
			      order_xml = order_xml +  "<urn:sodUm>" + line.getUnit() + "</urn:sodUm>";
			      if(line.getDiscount() > 0){
                  //    order_xml = order_xml +"<urn:sodListPr>"+ line.getPrice() + "</urn:sodListPr>";
                    //  order_xml = order_xml +"<urn:discount>"+ line.getDiscount() + "</urn:discount>";
			      }
		    	 
			      order_xml = order_xml +
		    	  		"</urn:salesOrderDetail>";
			 counter++;
	      }
	 
		
	 	order_xml = order_xml + "</urn:salesOrder>";
	 	order_xml = order_xml + "</urn:import>";
	 	order_xml = order_xml + "</soapenv:Body>";
	 	order_xml = order_xml + "</soapenv:Envelope>";
	    System.out.println("output " +order_xml); 
	 	
	 		
	 	try {
	 		
	 		  String fn = doknr;
		      if(doknr.trim().length()== 0){
		    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
		      }
		      
		      String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + domain + "-" + fn  +  ".xml";
		      System.out.println(fileName);
		       
		      Converter.saveFileInUTF8(fileName, order_xml);

		      fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + domain + "-" + fn  +  "-response.xml";
		      System.out.println(fileName);
		
		   //   trouble = "IMPORTED ORDER \n" + order_xml;
			 	
	 		String xml = getWeb(order_xml , "DokNr:" + order.getDokNr() + " Company:" + order.getCompany());		
	 	//	System.out.println("Tulemus " + xml);
		     Converter.saveFileInUTF8(fileName, xml);
			 	
	      
	      setNumAddedLines(counter); 
	      
	       
	    
	 	
	 	} catch (Exception e) {
	 		e.printStackTrace();
	 		
	 	  trouble = "NOT IMPORTED ORDER \n" + order_xml;
	 	//  System.exit(0);
	 	}

	 	}
		} catch (Exception e) {
			e.printStackTrace();

		 	  trouble = "NOT IMPORTED ORDER \n" + order.getDokNr();
		 //	  System.exit(0);
		}
		
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

 
  public String getDriverVersion(){
    return "BiovelaUusImport";
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

  private String trouble = null;
  private int counter = 0;
  private ImportPanel parent;
  private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return trouble;
}
  
}
