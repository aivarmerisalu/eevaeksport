
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
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

public class BalmerkCSVImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public BalmerkCSVImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    try {
   
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }


  public String identifyCustomer(String id,String regnr) throws SQLException {
    return null;
  }

 
  public String identifyCustomerAGENT(String id) throws SQLException {
   return null;
 }
 
  
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
    return prodID;
  }


  private String getDir(){
	     String dir = parent.getProps().getProperty("readin");
	     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	     return dir;
	   }
  
  public TreeMap getCustomers(){
	     TreeMap tm = new TreeMap();
	     try {
	     FileReader  fr = new FileReader(getDir() + "D365GetCustomers.csv");
	     BufferedReader  reader = new BufferedReader(fr);
	     
	     String  line=reader.readLine();
	      
	     while((line = reader.readLine()) != null){
	    	 
	    	 line = line.replaceAll(","," | ");
	        StringTokenizer st = new StringTokenizer(line, "|");
	       if (st.hasMoreTokens()){
	    	// name,nimelisa,namealias,accountnum,payer,ssordereraccount,debt,used,linedisc,address,visit,agent,term,block,payerblock,phone,info,grupp,confmail,abiagent,abipaev,credit,chain

	    	   String name = st.nextToken().trim();
	    	   if(name.startsWith("\"")) {
	        	   name = name + " " + st.nextToken().trim();
	        	   if(!name.endsWith("\"")) {
	        		   name = name + " " + st.nextToken().trim();
	            	}
	        	   if(!name.endsWith("\"")) {
	        		   name = name + " " + st.nextToken().trim();
	               }   
	        	   if(!name.endsWith("\"")) {
	        		   name = name + " " + st.nextToken().trim();
	               }   
	        	   name = name.replaceAll("\"", " ").trim();
	           }
	    	   try {
	    	   String name2 = st.nextToken().trim();
	    	   String nameAlias = st.nextToken().trim();
	           String id = st.nextToken().trim();
	           String payer = st.nextToken().trim();
	    	   String firma = st.nextToken().trim();
	    	   
	    	   if(firma.equals("DUNKER")) {
	           String deptdue = st.nextToken().trim();
	           String usedcredit = st.nextToken().trim();
	           String custdisc = st.nextToken().trim();
	           
	           if(custdisc.startsWith("\"")) {
	        	   custdisc = custdisc + " " + st.nextToken().trim();
	        	   custdisc = custdisc.replaceAll("\"", " ").trim();
	           }
	           

	           
	           String address = st.nextToken().trim();
	        //   System.out.println(address);
	           if(address.startsWith("\"")) {
	        	   address = address + " " + st.nextToken().trim();
	        	   if(!address.endsWith("\"")) {
	            	   address = address + " " + st.nextToken().trim();
	            	}
	        	   
	        	   if(!address.endsWith("\"")) {
	            	   address = address + " " + st.nextToken().trim();
	               }   

	        	   if(!address.endsWith("\"")) {
	            	   address = address + " " + st.nextToken().trim();
	               }   
	        	   address = address.replaceAll("\"", " ").trim();
	             //  System.out.println("address " + address);
	           }
	        //   Aardemaa OÜ,,Aardemaa OÜ,AARDE,,DUNKER,,,3,Kastani 1 90508 Haapsalu,,UUSSAAR,21,0,0,475 5075,,AARDEMAA,,,,8784.7,
	//name,nimelisa,namealias,accountnum,payer,ssordereraccount,debt,used,linedisc,address,visit,agent,
	           //term,block,payerblock,phone,info,grupp,confmail,abiagent,abipaev,credit,chain.

	           
	           String visit = st.nextToken().trim();
	           String agentcode = st.nextToken().trim();
	           String payterm = st.nextToken().trim();
	           String block = st.nextToken().trim();
	           String blockPayer = st.nextToken().trim();

	           String contactphone = st.nextToken().trim();

	           String contactname = st.nextToken().trim();
	           String grupp = st.nextToken().trim();
	           
	           String confmail = st.nextToken().trim();
	           String abiagent = st.nextToken().trim();
	           String abipaev = st.nextToken().trim();
	           String crlimit = st.nextToken().trim();
	           String chain = st.nextToken().trim();
	           
	if(id.equals("AARDE")) {
//		System.out.println("grupp " +  grupp);
	}
	          
	  
	           Customer customer = new Customer(id);
	           if(grupp.length() > 0) {
	            customer.setGroup(grupp);
	           }
	           
	           if(chain.length() > 0) {
	               customer.setChain(chain);
	              }
	        	 customer.setAddress(Converter.enCodeEst(Converter.adressLink(address,"Eesti")));           
	           
	           
	           customer.setName(Converter.enCodeEst(name) + " " + id );
	           
		       	double dept = 0;
		       	try{
		       		dept = Double.parseDouble(deptdue);
		       		customer.setDebt(dept);
		       	}catch(Exception e ){
		       	}
	           
	           if(agentcode.length() > 0) {
	        	   customer.addAgent(new Agent(agentcode));
	           }
	           
	           
	           String cont = "";
	           if(contactname.length() > 0) {
	        	   cont=contactname;
	           }

	           if(contactphone.length() > 0) {
	        	   cont=cont + " Tel:" + contactphone.trim();
	           }
	           
	           if (cont.length()>0){    	
	              customer.setPersonalData(Converter.enCodeEst(cont.trim()));
	         	}
	           

	         if(payer.length() > 0){
	             customer.setPayer(payer);  
	            
	           
	         }
	         
	             
	    	double limit = 0;
	    	try{
	    		limit = Double.parseDouble(crlimit);
	    		if(limit > 0) {
	    			customer.setCredit(limit + "");	
	      		  
	    		}
	    	
	    	}catch(Exception e ){
	    	}
	    	
	       
	        
	        if(payterm.length() > 0) {
	        	customer.setPaymTerm(payterm);
	        }
	   
	        if(custdisc.trim().length() > 0) {
	        	try {
	        	customer.setDiscount(Double.parseDouble(custdisc));
	        	}catch(Exception e){
	        		
	        	}
	        }
	        
	        if(block != null && block.trim().equals("B")) {
	        	customer.setSalesBlockT(true);
	        }
	        
		     tm.put(Converter.cleanLitLatEst( id), customer);
	    
	        }
	       
	       }catch(Exception e) {
	    	 //  e.printStackTrace();
	       }
	     }
	     }
	     
	     reader.close();
	     fr.close();
	    	
	     } catch (IOException e){
	       e.printStackTrace();
	     }
	     return tm;
	   }

  private TreeMap custs = null;
  
  
  public void makeOrder(Order order) throws SQLException{

	  counter = 0;
	  
	  if(!order.getCompany().equals("OTHERSTUFF")){
		  Vector splitted =  splitOrder(order);
	 	  Enumeration en = splitted.elements();
	 	 try {
			makeEans();
		} catch (Exception e) {
			e.printStackTrace();
		}
	 	 if(custs == null) {
	 		 custs = getCustomers();
	 	 }
	 	 
	 	  while(en.hasMoreElements()){
	 		  Order ord = (Order)en.nextElement();
	 		 makeOutput(ord); 	 
	 		 
	 		  boolean salvesta = true;
	      	 	 Vector lin = ord.getOrderLines();
	      	 	 
	      	 	 boolean laks = false;
	      	 	 boolean tagastus = false;
	      	 	 for (int i = 0; i < lin.size(); i++) {
	      	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	      	 	
	      	 		if(ol.getKogus() < 0 && ol.getDiscount() > 0 && (ord.getStatus() == 5 || ord.getStatus() == 4)){
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
	 		 
	      		    
	      		  
	  		 	  boolean repr = false;
	  		 	 	 for (int i = 0; i < lin.size(); i++) {
	  		 	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	  		 	 		if(ol.getKogus() < 0 && ol.getDiscount() > 0.1 && (order.getStatus() == 5 || order.getStatus() == 4)){
	  		 	 			repr = true;
	  		 	 			tagastus = false;
	  		 	 		}
	  		 	 	 } 
	      		    
	 		 makeOutputTelema(ord,tagastus, repr);
	    }
	    
	  }else{
		  counter = order.getOrderLines().size();
	  }
		
	 
    //  makeOutput(order);
      setNumAddedLines(counter);
  }
  
   private HashMap vanaUusKood = null;
    private HashMap eans = null;
    private HashMap prodnames = null;
    
    private void makeEans() throws Exception{
  	 
    	if(vanaUusKood == null) {
    		vanaUusKood = new HashMap();
    		
    		 FileReader fr = new FileReader(getDir() + "VanaUusKood.csv");
     		 BufferedReader reader = new BufferedReader(fr);

      	     String line = null;
             reader.readLine();
             while(( line = reader.readLine()) != null ){
             line = line.replaceAll(","," |");
             
             line = line.replaceAll("\"\"", "''");
          
            StringTokenizer st = new StringTokenizer(line, "|");

            if (st.hasMoreTokens()){
         	   String vana = st.nextToken().trim();
         	   String uus = st.nextToken().trim();
         	   vanaUusKood.put(vana, uus);
            }
            
             }
             reader.close();
    		
    	}
    	
  	  	
  	  	if(eans == null){
  	  		eans = new HashMap();
  	  		prodnames = new HashMap();
  	  	
  	     String line = null;
          
 		 FileReader fr = new FileReader(getDir() + "D365GetProducts.csv");
 		 BufferedReader reader = new BufferedReader(fr);

         reader.readLine();
         while(( line = reader.readLine()) != null ){
         line = line.replaceAll(","," |");
         
         line = line.replaceAll("\"\"", "''");
      
        StringTokenizer st = new StringTokenizer(line, "|");

        if (st.hasMoreTokens()){
     	   String id = st.nextToken().trim();
     	   String description = st.nextToken().trim();
            if(description.startsWith("\"") && !description.contains("''") ) {
         	   description = description + " " + st.nextToken().trim();
         	   if(!description.endsWith("\"")) {
         		   description = description + " " + st.nextToken().trim();
             	}
         	   if(!description.endsWith("\"")) {
         		   description = description + " " + st.nextToken().trim();
                }   
         	   if(!description.endsWith("\"")) {
         		   description = description + " " + st.nextToken().trim();
                }   
         	   description = description.replaceAll("\"", " ").trim();
            }
     	   //itemid,nimetus,kastis,alusel,price,ean,grupp,unit,vat,alegr,tarne_kp
            String incase = st.nextToken().trim();
            String onPallet = st.nextToken().trim();
     	   String unitprice = st.nextToken().trim();
     	   String ean = st.nextToken().trim();
     	   String groupcode = st.nextToken().trim();
            String unit = st.nextToken().trim();
     	   String vat = st.nextToken().trim();
     	   String alegr = st.nextToken().trim();
		
     	   if(ean != null) {
     		   eans.put(id, ean);
     	     }
     	     prodnames.put(id, description);
 			 }
 		   }
         reader.close();
  	  	}

  	  	System.out.println("oioi " + eans.size() + " " + prodnames.size() + " " + vanaUusKood.size());
  	  	
  	  }
  

    
  
  private void makeOutputTelema(Order order , boolean tagastus, boolean ymber){	
	 StringBuffer sb = new StringBuffer();
	 nf.setGroupingUsed(false);

	try {
		
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
		
		 String doknr = order.getDokNr();
      String ls = System.getProperty("line.separator");
      String fixagent = order.getAgent(); 
  
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
        sb.append("<SenderID>Balmerk</SenderID>" + ls);
        sb.append("<ReceiverID>" + order.getCompany() + "</ReceiverID>" + ls); 	
        sb.append("</Header>" + ls);
        sb.append("<Document>" + ls);
        
        String type = "order";
        if(tagastus) {
     	   type = "retann";
        }
        if(ymber) {
      	   type = "reprice";
         }
   
        sb.append("<DocumentType>" + type + "</DocumentType> " + ls);
        sb.append("<DocumentParties>" + ls);
    //    String regnr = getRegKood(custID);
      	   sb.append("<BuyerParty>" + ls);
           sb.append( "<PartyCode>" +  escape((custID)) + "</PartyCode>" + ls); 
           sb.append("</BuyerParty>" + ls);
        
           sb.append("<OrderParty>" + ls);
           sb.append( "<PartyCode>" +  escape(custID) + "</PartyCode>" + ls);
           sb.append("</OrderParty>" + ls);
        
           sb.append("<DeliveryParty>" + ls);
           sb.append("<PartyCode>" +  escape(custID) + "</PartyCode>" + ls); 
           try {
           String cuname = ((Customer)custs.get(custID)).getName();
           if(cuname != null) {
         	  sb.append( "<Name>" + escape(cuname)  + "</Name>" + ls); 	  
           }
           }catch(Exception e) {
        	   e.printStackTrace();
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
               
      	  sb.append("<RefInfo>" + ls);
      	  if(ref != null && ref.trim().length() > 0){
      		  if(tagastus) {
      			  if(ref.trim().indexOf(" ") != -1) {
      				  ref = ref.substring(0,ref.indexOf(" "));
      			  }
      		  }
      		  
            sb.append("<SourceDocument type=\"order\">" + ls);
            sb.append("    <SourceDocumentNum>" + escape(ref) + "</SourceDocumentNum>" + ls );
            sb.append("</SourceDocument>" + ls);
      	  }
            sb.append("</RefInfo>" + ls); 

    String    sss = (Calendar.getInstance().getTime().getTime()+ "").substring(4) ;
    
    if(ref != null && ref.trim().length() > 0){
 	   sb.append("<DocumentNum>" + escape(ref)+ "</DocumentNum>" + ls); 
    } else {
 	   sb.append("<DocumentNum>" + escape(agent + "-" + sss )+ "</DocumentNum>" + ls);   
    }
       
        sb.append("<CreatedBy>" + escape(agent)+ "</CreatedBy>" + ls); 
        
        sb.append("<DateInfo>" + ls);
        sb.append("<OrderDate>" +escape(getDateString(Calendar.getInstance())) + "</OrderDate>" + ls);
        sb.append("<DeliveryDateRequested>" +  escape(getDateString(cal)) + "</DeliveryDateRequested>" + ls); //aeg taha panna  T10:00:00
        sb.append("</DateInfo>" + ls);
        sb.append("</DocumentInfo>" + ls);
        sb.append("   <DocumentItem>" + ls);

        
        Vector sortedLines = Converter.joinLines(order.getOrderLines());
        for (int i = 0; i < sortedLines.size(); i++) {
            OrderLine line = (OrderLine)sortedLines.elementAt(i);
            double ale = line.getDiscount();     
          //  counter++;
            
            String yhik = line.getUnit();
            if (ale == -1 ){
                ale = 0 ;
                }
            sb.append("      <ItemEntry>" + ls);
            sb.append("      <LineItemNum>"  + (i+1) + "</LineItemNum>" + ls);
          
           
            String toode = line.getToode() ;
            if(this.vanaUusKood.get(toode) != null) {
	        	toode = (String)vanaUusKood.get(toode); 
	        }
           // <GTIN>4740072222752</GTIN> 
           // <BaseUnit>TK</BaseUnit> 
            sb.append("         <SellerItemCode>" + toode+ "</SellerItemCode>" + ls);
            try{

                String ean = eans.get(toode)+"";
                if(ean != null && !ean.trim().equals("null")){
          	      sb.append("         <GTIN>" + ean + "</GTIN>" + ls);
                }else {
             	   log.error(" File Saved " + "EAN-i pole " + agent + " " + order.getCompany() + " " + toode + " " + line.getKogus() );
             	   sb.append("         <GTIN>" + toode + "</GTIN>" + ls);
                }
                     
            }catch(Exception e){
         	   
            }
            
           sb.append("         <BaseUnit>tk</BaseUnit>" + ls);
          	  sb.append("         <ItemUnitRecord>" + ls);
               sb.append("                <ItemUnit>tk</ItemUnit>" + ls);
               sb.append("         </ItemUnitRecord>" + ls);
               
            if(prodnames.get(toode) != null) {
         	    sb.append("          <ItemDescription>" + (escape(prodnames.get(toode)+"")) + "</ItemDescription>" + ls);               
            }
               
               if(tagastus) {
             	  sb.append("         <AmountToReturn>" + escape(getNF().format(line.getKogus()*-1)) + "</AmountToReturn>" + ls);
             	  
               }else {
             	  sb.append("         <AmountOrdered>" + escape(getNF().format(line.getKogus())) + "</AmountOrdered>" + ls);
	  
               }
             

               String ccc = "";
            if(line.getDiscount() > 0){
              sb.append("         <Addition addCode=\"DSC\"><AddRate>" +
          	   escape(getNF().format(line.getDiscount())) + "</AddRate></Addition>" + ls); 
              ccc =  "-" + nf.format(line.getDiscount()) + "% ";
            }
           
          
            if(line.getPartii() != null && line.getPartii().trim().length() > 0 ){
         	   ccc = ccc + line.getPartii();
            }
            if(ccc.length() > 0) {
                sb.append("   <AdditionalInfo>" +ls);
                sb.append("       <Extension extensionId=\"remarks\">" + ls);
                sb.append("       <InfoName>Notes</InfoName>");
                sb.append("            <InfoContent>" + Converter.escapeXML(ccc) + "</InfoContent>" + ls );
                sb.append("       </Extension>");
                sb.append("   </AdditionalInfo>" + ls);	
            }
     
            sb.append("      </ItemEntry>" + ls);
            
        }
        

        sb.append("   </DocumentItem>" + ls);
        
  	    String comm = order.getInfo();
        /*  if(comm != null && comm.length()>100){
          	comm = comm.substring(0,100);
          }*/
          
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

 //  counter = counter + cou;
 	//
   try{
 	  Thread.sleep(10); 
   }catch(Exception e){
 	 e.printStackTrace(); 
   }
   
   
  String  fileName = escape(parent.getProps().getProperty("conv.dirxml") + agent) + "-" + counter + "." + sss  +  ".xml";
   System.out.println(fileName);
   Writer out = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream(fileName), "UTF-8"));
			try {
			    out.write(sb.toString());
			} finally {
			    out.close();
			}
			
			 log.error(" File Saved " + fileName );
			 fileName = escape(parent.getProps().getProperty("conv.dircxml") + agent) + "-" + counter + "." + sss  +  ".xml";
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

 
  
  private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    sb.append(cal.get(Calendar.YEAR) + "");
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month + "");

	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day);

	    return sb.toString();
	  }
	  
  
  
  private Vector splitOrder (Order order){
			  
		  Vector result = new Vector();

		  if (order.getDokNr() == null || order.getDokNr().trim().length() == 0){
			  order.setDokNr((Calendar.getInstance().getTimeInMillis() + "").substring(3));
		  }
		  System.out.println("Infokene " + order.getInfo() + " " + order.getDokNr());

		  Order ordS2 = new Order(order.getCompany(),order.getAgentFirstName());
		  Order ordS4 = new Order(order.getCompany(),order.getAgentFirstName());
		  
		  ordS2.setAgentCode(order.getAgentCode());
		  ordS2.setDokNr(order.getDokNr());
		  ordS2.setCompName(order.getShipMode());
		  ordS2.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordS2.setStatus(order.getStatus()); 
		  ordS2.setInfo(order.getInfo());
		  ordS2.setDealer("order");

		  ordS4.setAgentCode(order.getAgentCode());
		  ordS4.setDokNr(order.getDokNr());
		  ordS4.setCompName(order.getShipMode());
		  ordS4.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordS4.setStatus(order.getStatus());
		  ordS4.setInfo(order.getInfo());
		  ordS4.setDealer("tester");

		  
		  Vector lines = order.getOrderLines();
		  Enumeration enumr = lines.elements();
		  while  (enumr.hasMoreElements()){
			  OrderLine lin = (OrderLine)enumr.nextElement();
			  
			  double status = lin.getDiscount();

			    if(status > -2){
			     	ordS2.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     	ordS2.setDokNr(order.getDokNr());
			     	ordS2.setReference(order.getReference());
			     }
			
			    if(status < -1){
			    	 ordS4.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
				     	ordS4.setDokNr(order.getDokNr().substring( 0,order.getDokNr().length()-1) + "9");
				     	ordS4.setReference(order.getReference()); 
				     	ordS2.setInfo(" ");
			     }
			  
		  }
		  
		  if(ordS2.getOrderLines() != null && ordS2.getOrderLines().size()>0){
			  result.add(ordS2);  
		  }

		  if(ordS4.getOrderLines() != null && ordS4.getOrderLines().size()>0){
			  result.add(ordS4);  
		  }
	      
		  
		  
		 System.out.println("Result =" +order.getDokNr()
				 + " "+ result.size());
		 //System.exit(0);
		  return result ;
	  }


  
  
  private void makeOutput(Order order){
	 	StringBuffer sb = new StringBuffer();
	 	boolean minus = false;
	 	try {
	 	  String extra = "";
		      

		    Vector linesy = order.getOrderLines();
		      for (int i = 0; i < linesy.size(); i++) {
		    	    OrderLine ol = (OrderLine) linesy.elementAt(i);
		 		 if(ol.getPartii() != null && ol.getPartii().trim().length() > 0){
		        	  extra  = extra + "("+ ol.getToode() + " - " +ol.getPartii() + ")  ";
		          }  
		      }
		    	
	 		
		    String doknr = order.getDokNr();
		      String fn = doknr;
		      if(doknr.trim().length()== 0){
		    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
		      }
		      String dd = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + "";
		      
			    String agent = order.getAgent();
			    
			      if(agent == null || agent.trim().length()== 0){
			    	 agent = "xxx";
			      } else{
			    	  agent = agent.trim();
			      }
			String ref = order.getTeostaja(); //mobiilse telli ref number
			if ( ref == null ){
				ref =  "";
			}
			String com = order.getEmailBody();
			if ( com == null ){
				com =  " ";
			}		
		
			String ware = order.getWarehouse();
			if ( ware == null ){
				ware = " ";
			}
		
			
			String comm = order.getInfo();
			if ( comm == null ){
				comm = " ";
			}
			comm = comm + " " + extra;
	         
	        String ootele = "0";
	        try{
	        	if(order.getShipMode() != null  && order.getShipMode().equals("-3")){
	 	        	ootele = "1";
	 	        }	
	        }catch(Exception e){
	        	
	        }
	       
	        
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
		 	 
		 	 
		 	  boolean repr = false;
		 	 	 for (int i = 0; i < lin.size(); i++) {
		 	 		OrderLine ol = (OrderLine) lin.elementAt(i);
		 	 		if(ol.getKogus() < 0 && ol.getDiscount() > 0.1 && (order.getStatus() == 5 || order.getStatus() == 4)){
		 	 			repr = true;
		 	 			tagastus = false;
		 	 		}
		 	 	 } 
	        
	         
	         String sep = ",";
	         String lineSep = System.getProperty("line.separator");
	         String type = "tellimus";
	         
	         if( repr ) {
	        	 type = "ymberhindlus";
	         }
	         if( tagastus ) {
	        	 type = "tagastus";
	         }
	         
	         sb.append(type + lineSep);

	         sb.append("tellija.regnr:" + sep  + ""+ lineSep);
	         sb.append("tellija_kood:" + sep  + order.getCompany() + lineSep );
	         sb.append("filiaal:" + sep  + order.getCompany() + lineSep);
	         sb.append("aasta:" + sep + order.getShipCal().get(Calendar.YEAR) + lineSep);
	         sb.append("nr:" + sep + "MJ"+ agent + dd + fn + lineSep);
	         sb.append("tarnija.regnr:" + sep + lineSep);
	         sb.append("kuupaev:" + sep + getDateString(order.getShipCal()) + lineSep);
	         sb.append("tarne_kuupaev:" + sep + getDateString(order.getShipCal()) + lineSep);
	         sb.append("tarne_aadress:" + sep + lineSep);
	         sb.append("koostaja.eesnimi:" + sep + "agentname" + lineSep);
	         sb.append("koostaja.perenimi:" + sep + "agentname"  + lineSep);
	       	 sb.append("koostaja.kood:"  + sep +"" + lineSep);	         
	         sb.append("koostaja.email:" + sep + ""  + lineSep); 
	         sb.append("tellija_viide:" + sep + ref  + lineSep);   
	         sb.append("markused:" + sep +escape(comm) + lineSep);
	         sb.append("ootele:" + sep +escape(ootele) + lineSep);
	         
	       
	        
	         sb.append("read" + lineSep);
	       	 sb.append("toote_kood,nimetus,yhik,kogus,km,yhiku_hind,ean_kood,summa" + lineSep); 
	        
	         
	    Vector lines =  Converter.joinLines(order.getOrderLines());
	      for (int i = 0; i < lines.size(); i++) {
	    	  
	        OrderLine ol = (OrderLine) lines.elementAt(i);
	        if(ol.getKogus() < 0){
	        	minus = true;
	        }
	        String toode = ol.getToode();
	        if(this.vanaUusKood.get(toode) != null) {
	        	toode = (String)vanaUusKood.get(toode); 
	        }
	    	  sb.append(toode + sep);
	          sb.append(ol.getTooteNimi().replace(',',' ') + sep);
	          sb.append(ol.getUnit() + sep);
	          sb.append(ol.getKogus() + sep);
	          sb.append(sep);
	          sb.append("0" + sep);
	          String ean = "";
	          if(ean == null || ean.trim().length()==0){
	          	ean = "";
	          	}
	          sb.append(ean);
	          
	          sb.append( sep +  "0" );

	        
	          String text = "";
	          
	          if(ol.getPartii() != null && ol.getPartii().trim().length() > 0){
	        	  text = ol.getPartii();
	          }
	          
	          sb.append( sep +  text );
	          
	          if(repr) {
	        	  double disc = ol.getDiscount();
		          if (disc < 0){
		            disc = 0;
		          }

	        	  sb.append( sep +  disc );
	          }
 
	          sb.append(lineSep);
	 
	       counter++;
	      }
	
	      
	      setNumAddedLines(counter); 

	    
	     
	        	  String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + order.getAgent() +"."+ order.getCompany()+"." +fn  + ".csv";
			      System.out.println(fileName);		
			      if(minus){
			    	  fileName = escape(parent.getProps().getProperty("conv.dir2")) +  "t-" + order.getAgent() +"."+ order.getCompany()+"." +fn  + ".csv";
			      }
			      FileOutputStream fos = new FileOutputStream(fileName);
			        fos.write(sb.toString().getBytes());
			        fos.close();
	    		
	    
		} catch (Exception e) {
			e.printStackTrace();
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


	public NumberFormat getNF() {
		return this.nf;
	}
	
 
  public String getDriverVersion(){
    return "AxaDunkerImport";
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
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private int counter = 0;
  private ImportPanel parent;

public String getTrouble() {
	return null;
}
}
