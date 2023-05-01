
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
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
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

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

public class NaviLivikoLITImportDriver
    extends AbstractImportDriver {


	  static Category log = Category.getRoot();

  public NaviLivikoLITImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

    try {
      Class.forName(databaseDriverName).newInstance();
      con =  DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
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


  
  private TreeMap getReturnReasons(){
	  TreeMap res = new TreeMap();
	   try {
		   
	   Statement stmt1 = con.createStatement();
 	   ResultSet  rs1 = stmt1.executeQuery("select Code as id, description as nimi from ReturnReason");
	    while(rs1.next()){
	    	String id = rs1.getString("id");
	    	String name = rs1.getString("nimi");
	    	if(name != null && name.trim().length() > 0){
	    		res.put(id.trim(), name.trim());
	    	}
	    }
	
	    rs1.close();
	    stmt1.close();
	   
       } catch(Exception r) {
		  r.printStackTrace(); 
	   }
	 //  System.out.println("res  " + res.toString());
	   return res;
  }
  
  public void makeOrder(Order order) throws SQLException{

	   if (signatures == null){
    	   signatures = new Vector();
       }
	  
	   String signature = order.getSignature();
	   
	   if(reasons == null)
	   reasons = getReturnReasons();
	   
	 // System.out.println("reasons " + reasons.size());
	   
		  if(signatures.contains(signature)){
			  this.trouble = "REPRETEATING ORDER agent :" + order.getAgent() + " customer :" 
		  + Converter.deCode(order.getCompany()) ;
			  log.error(trouble);
		  }else{
			  makeEans();
			  
			  boolean salvesta = true;
	      	 	 Vector lin = order.getOrderLines();
	      	 	 
	      	 	 boolean laks = false;
	      	 	 boolean tagastus = false;
	      	 	 for (int i = 0; i < lin.size(); i++) {
	      	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	      	 	
	      	 		if(ol.getKogus() < 0 && ol.getDiscount() > 0 && (order.getStatus() == 5 || order.getStatus() == 4)){
	      	 			laks = true;
	      	 			tagastus = true;
	      	 		}
	      	 		if(ol.getKogus() < 0 ) {
	      	 			tagastus = true;
	      	 		}
	      	 		
	      	 	 }  
	      		    if(laks){
	      		    	salvesta = false;
	      		    }		 

	      		//	System.out.println("XXXXX salvesta " + order.getStatus() + " " + salvesta + " "+ tagastus);
	      		  log.error("sisenen tellimust tegeama " + order.getCompany() + order.getOrderLines().size());
	      		    
	      		if(salvesta){
	      			this.trouble = null;
	  			  counter = 0;
	  			  makeOutput(order,tagastus);
	  			  log.error("made Output 1 " + order.getCompany() + " " + fileName);
	  		    }else{
	  		    	this.trouble = null;
	  			  counter = 0;
	  			  makeOutput2(order,tagastus);
	  			  log.error("made Output 2 " + order.getCompany() + " " + fileName);
	  		      
	  		    }
			  
			  
			  
			  
			  signatures.add(signature);
		  }

			try {
			saveObject(signatures, "signatures.vec");
			} catch (IOException e) {
			e.printStackTrace();
			} 
			  setNumAddedLines(counter); 
		      
		  }

  



public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
 
private HashMap eans = null;
private HashMap prodnames = null;
  
  private HashMap makeEans() throws SQLException{
	 
	  	
	  	if(eans == null){
	  		eans = new HashMap();
	  		prodnames = new HashMap();
	  	
	 	 String	 sql2 = " select * from barcode  ";
				
				Statement st1 = con.createStatement();
				ResultSet rs1 = st1.executeQuery(sql2);
				
				 while (rs1.next()) {
					 String id = rs1.getString("toode").trim();
					 String name = rs1.getString("nimi").trim();
					  String ean = (rs1.getString("ean"));
		                 if (ean != null &&ean.trim().length()>3){
						 eans.put(id,ean);
					 }
		                 prodnames.put(id, name);
				    
				 }
				
		rs1.close();
		st1.close();
		
	  	}
	  	
	  	System.out.println("oioi " + eans.size() );
	  	
	  	return eans ;
	  }

  private String getCustName(String cuid) throws SQLException{
		 
	
		 String name = null;
	 	 
		 String	 sql2 = " select * from Customers where kood = '" + cuid + "' ";
				
				Statement st1 = con.createStatement();
				ResultSet rs1 = st1.executeQuery(sql2);
				
				 while (rs1.next()) {
					 name = rs1.getString("nimi").trim();
				 }
				
		rs1.close();
		st1.close();
		
	  	
	  	
	  	System.out.println("oioi " + eans.size() );
	  	
	  	return name ;
	  }
  
  
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
	  
  TreeMap reasons = null;
  
 private void makeOutput(Order order , boolean tagastus){
 	
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
	//	System.out.println("ladu tellimusel " + ware + " " + order.getStatus());
		
		 String doknr = order.getDokNr();
         String ls = System.getProperty("line.separator");
         String fixagent = order.getAgent(); 
     
         String custID = order.getCompany();
        
           Calendar  shipDate = order.getShipCal();
           Calendar cal = Calendar.getInstance();
          
           if (shipDate != null) {
                 cal = shipDate;
               }
         
           
           String inff = order.getTeostaja();
           String reason = null;
       
           if (inff != null && inff.trim().length()>0 && inff.indexOf("|") !=-1){
           	reason = " " + inff.substring(inff.indexOf("|")+1, inff.indexOf("]"));
		    reason = reason.replace('|', ' ');
		    String code = reason.substring(reason.indexOf("[")+1).trim();
		    System.out.println("ret code " + code);
		    String rname = (String)reasons.get(code) ; 
		    if(rname == null) {
		    	rname ="";
		    }
		    reason = (reason.replace('[', ' ') + " " + rname).trim();
		   }
           String reasonCode = null;
           if (inff != null && inff.trim().length()>0 && inff.indexOf("[") !=-1){
        	   reasonCode =  inff.substring(inff.indexOf("[")+1, inff.indexOf("]"));
   		   }
           
           System.out.println("reason " + reason);
           
           sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + ls);
           sb.append("<E-Document>" + ls);
      
           sb.append("<Header>" + ls);
           sb.append("<DateIssued>" + escape(getDateString(Calendar.getInstance())) + "</DateIssued>" + ls);
           sb.append("<SenderID>Liviko</SenderID>" + ls);
           sb.append("<ReceiverID>" + order.getCompany() + "</ReceiverID>" + ls); 	
           sb.append("</Header>" + ls);
           sb.append("<Document>" + ls);
           
           String type = "order";
           if(tagastus) {
        	   type = "retann";
           }
       
           sb.append("<DocumentType>" + type + "</DocumentType> " + ls);
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
              
              String cuname = getCustName(custID);
              if(cuname != null) {
            	  sb.append( "<Name>" + escape(cuname)  + "</Name>" + ls); 	  
              }
            
            
         
         String agent = order.getAgent();
           if(fixagent != null){
         	  agent = fixagent;
           }
           
           sb.append("<UserId>" +  agent + "</UserId>" + ls);
           sb.append("</DeliveryParty>" + ls);
           sb.append("</DocumentParties>" + ls);
           sb.append("<DocumentInfo>" + ls);
           sb.append("<DocumentSubType>MERISOFT</DocumentSubType>" + ls);
           sb.append("<DocumentName>MERISOFT-ORDER</DocumentName>" + ls );
           String asi = "order";
   		 if(order.getStatus() == 7) {
   			asi = "delivery_note";
         }
   		 
   	  System.out.println("Tagastus " + tagastus + " " + ref);
         	  sb.append("<RefInfo>" + ls);
         	  if(ref != null && ref.trim().length() > 0){
         		  
         		
         		  if(tagastus) {
         			 if( ref.trim().indexOf("|") != -1) {
      				      ref = ref.substring(0,ref.indexOf("|"));
      			     } else if(ref.trim().indexOf(" ") != -1) {
        				  ref = ref.substring(0,ref.indexOf(" "));
        			  }
        			
         			if(ref.trim().indexOf("[") != -1) {
      				  ref = ref.substring(0,ref.indexOf("["));
      			    }
         			if(ref.trim().indexOf("[") == 0) {
        				  ref = ref.substring(0,ref.indexOf("["));
        			    }
         			  
         		  }
         		  System.out.println("Tagastus0 " + tagastus + " " + ref);
               sb.append("<SourceDocument type=\"" + asi+ "\">" + ls);
               sb.append("    <SourceDocumentNum>" + escape(ref) + "</SourceDocumentNum>" + ls );
               sb.append("</SourceDocument>" + ls);
         	  }else if(order.getStatus() == 7) {
         		 sb.append("<SourceDocument type=\"" + asi+ "\">" + ls);
         	    sb.append("</SourceDocument>" + ls);
         	  }
               sb.append("</RefInfo>" + ls); 
         	

        /* if(smarten){
        	   sb.append(" <CreatedBy>" + agent + "</CreatedBy>" + ls ) ;	
         	  subType = "myyk";
         	  sb.append("<DocumentName>tellimus</DocumentName>" + ls); 
        }*/
           
       String    sss = (Calendar.getInstance().getTime().getTime()+ "").substring(4) ;
       
       if(ref != null && ref.trim().length() > 0){
    	   sb.append("<DocumentNum>" + escape(ref)+ "</DocumentNum>" + ls); 
       }else {
    	   sb.append("<DocumentNum>" + escape(agent + "-" + sss )+ "</DocumentNum>" + ls);   
       }
          
           sb.append("<CreatedBy>" + escape(agent)+ "</CreatedBy>" + ls); 
           
           sb.append("<DateInfo>" + ls);
           sb.append("<OrderDate>" +escape(getDateString(Calendar.getInstance())) + "</OrderDate>" + ls);
           if(order.isShipDateUsed()) {
        	   sb.append("<DeliveryDateRequested>" +  escape(getDateString(cal)) + "</DeliveryDateRequested>" + ls); //aeg taha panna  T10:00:00
           }
           sb.append("</DateInfo>" + ls);
           if(order.getWarehouse() != null && order.getWarehouse().trim().length() > 0) {
        	   sb.append("<Warehouse>" +escape(order.getWarehouse().trim()) + "</Warehouse>" + ls);
           }
           
           sb.append("</DocumentInfo>" + ls);
           
           sb.append("   <DocumentItem>" + ls);
           
           Vector sortedLines = order.getOrderLines();
           for (int i = 0; i < sortedLines.size(); i++) {
               OrderLine line = (OrderLine)sortedLines.elementAt(i);
               double ale = line.getDiscount();     
               counter++;
               
               if (ale == -1 ){
                   ale = 0 ;
                   }
               sb.append("      <ItemEntry>" + ls);
               sb.append("      <LineItemNum>"  + (i+1) + "</LineItemNum>" + ls);
             
              
              // <GTIN>4740072222752</GTIN> 
              // <BaseUnit>TK</BaseUnit> 
               sb.append("         <SellerItemCode>" + line.getToode() + "</SellerItemCode>" + ls);
               try{

                   String ean = eans.get(line.getToode())+"";
                   if(ean != null && !ean.trim().equals("null")){
             	      sb.append("         <GTIN>" + ean + "</GTIN>" + ls);
                   }else {
                	   log.error(" File Saved " + "EAN-i pole " + agent + " " + order.getCompany() + " " + line.getToode() + " " + line.getKogus() );
                	   sb.append("         <GTIN>" + line.getToode() + "</GTIN>" + ls);
                   }
                        
               }catch(Exception e){
            	   
               }
               
              sb.append("         <BaseUnit>tk</BaseUnit>" + ls);
             	  sb.append("         <ItemUnitRecord>" + ls);
                  sb.append("                <ItemUnit>tk</ItemUnit>" + ls);
                  sb.append("         </ItemUnitRecord>" + ls);
                  
               if(prodnames.get(line.getToode()) != null) {
            	    sb.append("          <ItemDescription>" + (escape(prodnames.get(line.getToode())+"")) + "</ItemDescription>" + ls);               
               }
                  
                  if(tagastus) {
                	  sb.append("         <AmountToReturn>" + escape(getNF().format(line.getKogus()*-1)) + "</AmountToReturn>" + ls);
                	  
                  }else {
                	  sb.append("         <AmountOrdered>" + escape(getNF().format(line.getKogus())) + "</AmountOrdered>" + ls);
	  
                  }
                

                  String ccc = "";
               if(line.getDiscount() > 0 || tagastus){
             	 // double discprice = line.getPrice()/(100d -line.getDiscount())*100d ;
             	 sb.append("         <ItemPrice>" +
                   	   escape(getNF().format(line.getPrice())) + "</ItemPrice>" + ls); 
             	  
                 sb.append("         <Addition addCode=\"DSC\"><AddRate>" +
             	   escape(getNF().format(line.getDiscount())) + "</AddRate></Addition>" + ls); 
                // if(!tagastus)
                // ccc =  "-" + nf.format(line.getDiscount()) + "% ";
               }
               
             
               if(line.getPartii() != null && line.getPartii().trim().length() > 0 ){
            	   ccc = ccc + line.getPartii();
               }
               
          
               
               if(ccc.length() > 0) {
                   sb.append("   <AdditionalInfo>" +ls);
                   sb.append("       <Extension extensionId=\"remarks\">" + ls);
                   sb.append("       <InfoName>Notes</InfoName>");
                   sb.append("            <InfoContent>" + Converter.escapeXML(ccc) + "</InfoContent>" + ls );
                  // sb.append("            <CustomContent>" + Converter.escapeXML(reason) + "</CustomContent>" + ls );
                
                   sb.append("       </Extension>");
                   sb.append("   </AdditionalInfo>" + ls);	
               }
        
               sb.append("      </ItemEntry>" + ls);
               
           }
           

           sb.append("   </DocumentItem>" + ls);
           
     	    String comm = order.getInfo();
            
             
             if (reason != null) {
            	 if(comm == null) {
            		 comm = reason.trim();
            	 }else {
            		 comm = (comm + reason).trim();
            	 }
             	 
                }
             
             if(comm != null && comm.length()>100){
              	comm = comm.substring(0,100);
              }
             if((comm != null && comm.trim().length() > 0)  || reasonCode != null){
             	  sb.append("   <AdditionalInfo>" +ls);
                   sb.append("       <Extension extensionId=\"remarks\">" + ls);
                   sb.append("       <InfoName>Notes</InfoName>");
                   if(comm == null) {
                	   comm = "Na.";
                   }
                   sb.append("            <InfoContent>" + escape(comm) + "</InfoContent>" + ls );
                   if(reasonCode != null) {
                	   sb.append("            <CustomContent>" + Converter.escapeXML(reasonCode) + "</CustomContent>" + ls );
                   }
                   
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
      
      
       fileName = escape(parent.getProps().getProperty("conv.dir") + agent) + "-" + counter + "." + sss  +  ".xml";
      System.out.println(fileName);
      Writer out = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream(fileName), "UTF-8"));
			try {
			    out.write(sb.toString());
			} finally {
			    out.close();
			}
			
			 log.error(" File Saved " + fileName );
			 fileName = escape(parent.getProps().getProperty("conv.dirc") + agent) + "-" + counter + "." + sss  +  ".xml";
		      System.out.println(fileName);
		       out = new BufferedWriter(new OutputStreamWriter(
					    new FileOutputStream(fileName), "UTF-8"));
					try {
					    out.write(sb.toString());
					} finally {
					    out.close();
					}
					 log.error(" copy Saved " + fileName );
	} catch (Exception e) {
		 log.error(" Output error 1 " + order.getCompany() );
		 log.error(e);
		e.printStackTrace();
	}
 }
 
 
 public String convert(String s) {
	 if(s == null) {
		 return null;
	 }
	 s = s.replaceAll("[^a-zA-Z0-9äõöüÄÕÖÜ:\\s+]", "");
	  return s.trim();
	}
 
 
 private  String fileName = null;
 
 
 private void makeOutput2(Order order , boolean tagastus){
	 	StringBuffer sb = new StringBuffer();
		StringBuffer sbmiinus = new StringBuffer();
	 	 nf.setGroupingUsed(false);
  log.error("teeme ymberhindluse faile");
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
	     
	         String custID = order.getCompany();
	         Calendar  shipDate = order.getShipCal();
	             
	         String inff = order.getTeostaja();
	           String reason = null;
	           if (inff != null && inff.trim().length()>0 && inff.indexOf("|") !=-1){
	           	reason = " " + inff.substring(inff.indexOf("|")+1, inff.indexOf("]"));
			    reason = reason.replace('|', ' ');
			    String code = reason.substring(reason.indexOf("[")+1).trim();
			    System.out.println("ret code " + code);
			    String rname = (String)reasons.get(code) ; 
			    if(rname == null) {
			    	rname ="";
			    }
			    reason = (reason.replace('[', ' ') + " " + rname).trim();
			   }
	           
	           
	           String reasonCode = null;
	           if (inff != null && inff.trim().length()>0 && inff.indexOf("[") !=-1){
	        	   reasonCode =  inff.substring(inff.indexOf("[")+1, inff.indexOf("]"));
	   		   }
	           System.out.println("reason2 " + reason + " " + reasonCode);

	       
	           Calendar cal = Calendar.getInstance();
	          
	           if (shipDate != null) {
	                 cal = shipDate;
	               }
	         
	           sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + ls);
	           sb.append("<E-Document>" + ls);
	      
	           sb.append("<Header>" + ls);
	           sb.append("<DateIssued>" + escape(getDateString(Calendar.getInstance())) + "</DateIssued>" + ls);
	           sb.append("<SenderID>Liviko</SenderID>" + ls);
	           sb.append("<ReceiverID>" + order.getCompany() + "</ReceiverID>" + ls); 	
	           sb.append("</Header>" + ls);
	           sb.append("<Document>" + ls);

	           sbmiinus.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + ls);
	           sbmiinus.append("<E-Document>" + ls);
	      
	           sbmiinus.append("<Header>" + ls);
	           sbmiinus.append("<DateIssued>" + escape(getDateString(Calendar.getInstance())) + "</DateIssued>" + ls);
	           sbmiinus.append("<SenderID>Liviko</SenderID>" + ls);
	           sbmiinus.append("<ReceiverID>" + order.getCompany() + "</ReceiverID>" + ls); 	
	           sbmiinus.append("</Header>" + ls);
	           sbmiinus.append("<Document>" + ls);
	           String cuname = getCustName(custID);
	           
	           String type = "order";
	           if(tagastus) {
	        	   type = "retann";
	           }
	      
	           sb.append("<DocumentType>" + type + "</DocumentType> " + ls);
	           sb.append("<DocumentParties>" + ls);
	         	  sb.append("<BuyerParty>" + ls);
	              sb.append( "<PartyCode>" +  escape((custID)) + "</PartyCode>" + ls); 
	              sb.append("</BuyerParty>" + ls);
	              sb.append("<OrderParty>" + ls);
	              sb.append( "<PartyCode>" +  escape(custID) + "</PartyCode>" + ls);
	              sb.append("</OrderParty>" + ls);
	              sb.append("<DeliveryParty>" + ls);
	              sb.append("<PartyCode>" +  escape(custID) + "</PartyCode>" + ls);
	              if(cuname != null) {
	            	  sb.append( "<Name>" + escape(cuname)  + "</Name>" + ls); 	  
	              }
	              sbmiinus.append("<DocumentType>" + type + "</DocumentType> " + ls);
	              sbmiinus.append("<DocumentParties>" + ls);
	              sbmiinus.append("<BuyerParty>" + ls);
	              sbmiinus.append( "<PartyCode>" +  escape((custID)) + "</PartyCode>" + ls); 
	              sbmiinus.append("</BuyerParty>" + ls);
	              sbmiinus.append("<OrderParty>" + ls);
	              sbmiinus.append( "<PartyCode>" +  escape(custID) + "</PartyCode>" + ls);
	              sbmiinus.append("</OrderParty>" + ls);
	              sbmiinus.append("<DeliveryParty>" + ls);
	              sbmiinus.append("<PartyCode>" +  escape(custID) + "</PartyCode>" + ls); 
	             
	              if(cuname != null) {
	            	  sbmiinus.append( "<Name>" +escape( cuname)  + "</Name>" + ls); 	  
	              }
	         String agent = order.getAgent();
	           
	           sb.append("<UserId>" +  agent + "</UserId>" + ls);
	           sb.append("</DeliveryParty>" + ls);
	           sb.append("</DocumentParties>" + ls);
	           sb.append("<DocumentInfo>" + ls);
	           sb.append("<DocumentSubType>MERISOFT</DocumentSubType>" + ls);
	           sb.append("<DocumentName>MMT-ORDER</DocumentName>" + ls );
	           sb.append("<RefInfo>" + ls);
	           sbmiinus.append("<UserId>" +  agent + "</UserId>" + ls);
	           sbmiinus.append("</DeliveryParty>" + ls);
	           sbmiinus.append("</DocumentParties>" + ls);
	           sbmiinus.append("<DocumentInfo>" + ls);
	           sbmiinus.append("<DocumentSubType>MERISOFT</DocumentSubType>" + ls);
	           sbmiinus.append("<DocumentName>MMT-ORDER</DocumentName>" + ls );
	           sbmiinus.append("<RefInfo>" + ls);
	           
	           
	           String asi = "order";
	     		 if(order.getStatus() == 7) {
	     			asi = "delivery_note";
	           }
	           
	         	  if(ref != null && ref.trim().length() > 0){
	         		  System.out.println("Tagastus 2 " + ref);
	         		  
	         		 if( ref.trim().indexOf("|") != -1) {
       				      ref = ref.substring(0,ref.indexOf("|"));
       			     } else if(ref.trim().indexOf(" ") != -1) {
         				  ref = ref.substring(0,ref.indexOf(" "));
         			  }
         			
         			if(ref.trim().indexOf("[") != -1) {
      				  ref = ref.substring(0,ref.indexOf("["));
      			    }
         			
         		  System.out.println("Tagastus0 " + tagastus + " " + ref);
           
	         		  
	               sb.append("<SourceDocument type=\"" + asi + "\">" + ls);
	               sb.append("    <SourceDocumentNum>" + escape(ref) + "</SourceDocumentNum>" + ls );
	               sb.append("</SourceDocument>" + ls);
	               
	               sbmiinus.append("<SourceDocument type=\"" + asi + "\">" + ls);
	               sbmiinus.append("    <SourceDocumentNum>" + escape(ref) + "</SourceDocumentNum>" + ls );
	               sbmiinus.append("</SourceDocument>" + ls);
	         
	         	  } else if(order.getStatus() == 7) {
	          		 sb.append("<SourceDocument type=\"" + asi+ "\">" + ls);
	          	     sb.append("</SourceDocument>" + ls);
	          	     sbmiinus.append("<SourceDocument type=\"" + asi+ "\">" + ls);
	          	     sbmiinus.append("</SourceDocument>" + ls);
	          	  }
	         	  
	         	  
	         	  
	               sb.append("</RefInfo>" + ls); 
	               sbmiinus.append("</RefInfo>" + ls); 
	         	
	           

	           
	     
	           
	       String    sss = (Calendar.getInstance().getTime().getTime()+ "").substring(5) ;
	           sb.append("<DocumentNum>" + escape(agent + "-" + sss + "HM")+ "</DocumentNum>" + ls); 
	           sb.append("<CreatedBy>" + escape(agent)+ "</CreatedBy>" + ls); 
	           sb.append("<DateInfo>" + ls);
	           sb.append("<OrderDate>" +escape(getDateString(Calendar.getInstance())) + "</OrderDate>" + ls);
	           sb.append("<DeliveryDateRequested>" +  escape(getDateString(cal)) + "</DeliveryDateRequested>" + ls); //aeg taha panna  T10:00:00
	           sb.append("</DateInfo>" + ls);
	           sb.append("</DocumentInfo>" + ls);
	           sb.append("   <DocumentItem>" + ls);

	           sbmiinus.append("<DocumentNum>" + escape(agent + "-" + sss + "-HM")+ "</DocumentNum>" + ls); 
	           sbmiinus.append("<CreatedBy>" + escape(agent)+ "</CreatedBy>" + ls); 
	           sbmiinus.append("<DateInfo>" + ls);
	           sbmiinus.append("<OrderDate>" +escape(getDateString(Calendar.getInstance())) + "</OrderDate>" + ls);
	           sbmiinus.append("<DeliveryDateRequested>" +  escape(getDateString(cal)) + "</DeliveryDateRequested>" + ls); //aeg taha panna  T10:00:00
	           sbmiinus.append("</DateInfo>" + ls);
	           sbmiinus.append("</DocumentInfo>" + ls);
	           sbmiinus.append("   <DocumentItem>" + ls);

	           
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
	               sb.append("         <SellerItemCode>" + line.getToode() + "</SellerItemCode>" + ls);
	               sbmiinus.append("      <ItemEntry>" + ls);
	               sbmiinus.append("      <LineItemNum>"  + (i+1) + "</LineItemNum>" + ls);
	               sbmiinus.append("         <SellerItemCode>" + line.getToode() + "</SellerItemCode>" + ls);
	               if(prodnames.get(line.getToode()) != null) {
	            	    sb.append("          <ItemDescription>" + (escape(prodnames.get(line.getToode())+"")) + "</ItemDescription>" + ls); 
	            	    sbmiinus.append("          <ItemDescription>" + (escape(prodnames.get(line.getToode())+"")) + "</ItemDescription>" + ls); 
	               }
	               try{

	                   String ean = eans.get(line.getToode())+"";
	                   if(ean != null && !ean.trim().equals("null")){
	             	      sb.append("         <GTIN>" + ean + "</GTIN>" + ls);
	                   }
	                        
	               }catch(Exception e){
	            	   
	               }
	                 
	               
	               
	               double kogus = line.getKogus();
	               if(kogus > 0 ){
	            	   kogus = kogus * -1;
	               }
	                  sb.append("         <BaseUnit>tk</BaseUnit>" + ls);
	             	  sb.append("         <ItemUnitRecord>" + ls);
	                  sb.append("                <ItemUnit>tk</ItemUnit>" + ls);
	                  sb.append("         </ItemUnitRecord>" + ls);
	                  sb.append("         <AmountOrdered>" + escape(getNF().format(line.getKogus()*-1 )) + "</AmountOrdered>" + ls);
	                
	                  sbmiinus.append("         <BaseUnit>tk</BaseUnit>" + ls);
	                  sbmiinus.append("         <ItemUnitRecord>" + ls);
	                  sbmiinus.append("                <ItemUnit>tk</ItemUnit>" + ls);
	                  sbmiinus.append("         </ItemUnitRecord>" + ls);
	                  sbmiinus.append("         <AmountOrdered>" + escape(getNF().format(line.getKogus())) + "</AmountOrdered>" + ls);
	                
	               if(line.getDiscount() > 0){
	                 sb.append("         <Addition addCode=\"DSC\"><AddRate>" +
	             	  escape(getNF().format(line.getDiscount())) + "</AddRate></Addition>" + ls); 
	               }
	              
	               sb.append("      </ItemEntry>" + ls);
	               sbmiinus.append("      </ItemEntry>" + ls);
	               
	           }
	           

	           sb.append("   </DocumentItem>" + ls);
	           sbmiinus.append("   </DocumentItem>" + ls);
	           
	     	    String comm = order.getInfo();
	             if(comm != null && comm.length()>100){
	             	comm = comm.substring(0,100);
	             }
	             
	             	  sb.append("   <AdditionalInfo>" +ls);
	                   sb.append("       <Extension extensionId=\"remarks\">" + ls);
	                   sb.append("       <InfoName>Notes</InfoName>");
	                   sb.append("            <InfoContent>" + Converter.escapeXML(reason)+ "</InfoContent>" + ls );
	                   if(reason != null) {
	                	   sb.append("            <CustomContent>" + Converter.escapeXML(reasonCode) + "</CustomContent>" + ls );
	                   }
	                
	                   sb.append("       </Extension>");
	                   sb.append("   </AdditionalInfo>" + ls);	
	                   
	                   sbmiinus.append("   <AdditionalInfo>" +ls);
	                   sbmiinus.append("       <Extension extensionId=\"remarks\">" + ls);
	                   sbmiinus.append("       <InfoName>Notes</InfoName>");
	                   sbmiinus.append("            <InfoContent>" + Converter.escapeXML(reason)+ "</InfoContent>" + ls );
	                   if(reason != null) {
	                	   sbmiinus.append("            <CustomContent>" + Converter.escapeXML(reasonCode) + "</CustomContent>" + ls );
	                   }
	                
	                   sbmiinus.append("       </Extension>");
	                   sbmiinus.append("   </AdditionalInfo>" + ls);
	             
	           
	             
	           sb.append("</Document>" + ls);
	           sb.append("</E-Document>" + ls);
	           sbmiinus.append("</Document>" + ls);
	           sbmiinus.append("</E-Document>" + ls);
	 
	      counter = counter + cou;
	    	//
	      try{
	    	  Thread.sleep(10); 
	      }catch(Exception e){
	    	 e.printStackTrace(); 
	      }
	      
	      
	      String fn =( Calendar.getInstance().getTimeInMillis() + "");
	     
	      String fileName = escape(parent.getProps().getProperty("conv.dir") + agent)  +"-" + counter+ "." + fn  +  ".xml";
	      System.out.println(fileName);
	      String fileNameMin = escape(parent.getProps().getProperty("conv.dir") + agent)  +"-" + counter+  ".-" + fn  +  ".xml";
	      System.out.println(fileNameMin);
	      Writer out = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(fileName), "UTF-8"));
				try {
				    out.write(sb.toString());
				} finally {
				    out.close();
				}
				
			/* out = new BufferedWriter(new OutputStreamWriter(
					    new FileOutputStream(fileNameMin), "UTF-8"));
					try {
					    out.write(sbmiinus.toString());
					} finally {
					    out.close();
					}*/

		} catch (Exception e) {
			 log.error(" Output error 2 " + order.getCompany() );
			 log.error(e);
			e.printStackTrace();
		}
	 }
 
 
	public NumberFormat getNF() {
		return this.nf;
	}
	


 
 
  public String getDriverVersion(){
    return "NaviLivikoLITImport";
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

  
  private String url = "jdbc:odbc:navilit";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private int counter = 0;
  private ImportPanel parent;
  private String trouble = null;


public String getTrouble() {
	return trouble;
}
  


}
