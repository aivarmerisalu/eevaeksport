
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
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

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

public class MantingaCSVImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public MantingaCSVImportDriver(ImportPanel parent) {
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
  
  
  public List<String[]> readCSVData(String fname) {
      String csvFile = getDir() + fname; // Adjust getDir() accordingly
      CSVParser parser = new CSVParserBuilder().withSeparator(',').build();
      List<String[]> data = new ArrayList<>();

      try (CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile))
   		   
              .withCSVParser(parser)
              .build()) {
          String[] nextLine = reader.readNext();// esimest rida ei loe
          while ((nextLine = reader.readNext()) != null) {
              data.add(nextLine); // Process each line without loading everything at once
          }
      } catch (IOException | CsvException e) {
          e.printStackTrace();
      }
      return data;
  }
  
  public TreeMap getCustomers(){


 TreeMap tm = new TreeMap();

 try {
     for (String[] row : readCSVData("Merisoft_MasterData_Customers.csv")) {
    	 int cou = 0;
    	 String id = null;
    	 String name = null;
    	 String name2 = null;
    	 String address = null;
    	 String contact = null;
    	 String payer = null;
    	 String group = null;
    	 String group2 = null;
    	 String chain = null;
    	 String visit = null;
    	 String age = null;
    	 String age2 = null;
    	 Integer block = null;
    	 Integer payerblock = null;
    	 Integer term = null;
    	 
         for (String cell : row) {

        	 cell = cell.replaceAll("\"", " ").trim();
        	 
        	 if(cou == 0) {
                 name = cell.trim();
                 name = name.replaceAll("'", " ").trim();
                 name = name.replaceAll("  ", " ");
                 name2 = Converter.cleanLitLatEst(name);
             }
        	 
        	 if(cou == 1) {
                 id = cell.trim().replace(':', '_');
             }

        	 if(cou == 2) {
                 payer = cell.trim();
             }
        	 
        	 
        	 if(cou == 6) {
                 group = cell.trim();
             }
        	 
        	 if(cou == 7) {
                 address = cell.trim();
             }
        	 
        	 if(cou == 8) {
                 visit = cell.trim();
             }

        	 if(cou == 9) {
                 age = cell.trim();
             }
        	 
        	 if(cou == 10) {
                 age2 = cell.trim();
             }

        	 if(cou == 11) {
                 term = Integer.parseInt(cell.trim());
             }
        	 
        	 if(cou == 12) {
                 block = Integer.parseInt(cell.trim());
             }

        	 if(cou == 13) {
                 payerblock = Integer.parseInt(cell.trim());
             }
         	// ,"agent","teles","term","block","payerblock","info","grupp","chain","maxallow","ette","cuinfo","minam"
        	 
        	 if(cou == 14) {
                 contact = cell.trim();
             }
        	 
        	 if(cou == 15) {
                 group2 = cell.trim();
             }
        	 
        	 if(cou == 16) {
                 chain = cell.trim();
             }


        	 if(cou == 17) {
               //  group = cell.trim();
             }

        
        	 
             cou++;
         }
         Customer  cust = new Customer(id);
         cust.setName( Converter.enCodeEst(name).trim() + " " + Converter.enCodeEst(address));
         cust.setAddress( Converter.adressLink(Converter.enCodeEst(address), ""));
         cust.restStreet = address;
         cust.restPerson = contact;
         
         if(block == 1) {
        	 cust.setSalesBlock(true);
         }
         
         if(payerblock == 1) {
        	 cust.setSalesBlock(true);
         }
         
         cust.setPersonalData( Converter.enCodeEst(contact));
         String setg = "";
         if(group != null && group.trim().length() > 0 ) {
        	 cust.setGroup(group);
             setg = group+ "|";
             
         }
         if(group2 != null && group2.trim().length() > 0 ) {
         	  cust.setGroup(setg + group2);
          }
         
         if(age != null && age.trim().length() > 0 ) {
        	 cust.addAgent(new Agent(age));
    	 }
         if(age2 != null && age2.trim().length() > 0 ) {
        	 cust.addAgent(new Agent(age2));
    	 }
          
         if(chain != null && chain.trim().length() > 0 ) {
        	  cust.setChain(chain);
         }
         if(payer != null && payer.trim().length() > 0 ) {
       	  cust.setPayer(payer);
        }
         
         if(visit != null && visit.trim().length() > 0 ) {
          	  cust.setVisit(visit);
           }
        
         if(term != null && term > 0) {
         	 cust.setPaymTerm(term+"");
         }
       
         
         tm.put(cust.getID(), cust);	
         
     }
 } catch (Exception e) {
     e.printStackTrace();
 }
 System.out.println("cust " + tm.toString());
 return tm;
}



  private TreeMap custs = null;
  
  
  public void makeOrder(Order order) throws SQLException{

	  counter = 0;
	  
	  if(!order.getCompany().equals("OTHERSTUFF")){
		 // Vector splitted =  splitOrder(order);
	 	 // Enumeration en = splitted.elements();
	 	 try {
			makeEans();
		} catch (Exception e) {
			e.printStackTrace();
		}
	 	 if(custs == null) {
	 		 custs = getCustomers();
	 	 }
	 	 
	 		  Order ord = order;
	 		 
	 		 	 Vector lin = ord.getOrderLines();
	      	 	 
	      	 	 boolean tagastus = false;
	      	 	 for (int i = 0; i < lin.size(); i++) {
	      	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	      	 		if(ol.getKogus() < 0 && ol.getDiscount() > 0 && (ord.getStatus() == 5 || ord.getStatus() == 4)){
	      	 			tagastus = true;
	      	 		}
	      	 		if(ol.getKogus() < 0 ) {
	      	 			tagastus = true;
	      	 		}
	      	 	 }  	
	 			  
	  		 	  boolean repr = false;
	  		 	 	 for (int i = 0; i < lin.size(); i++) {
	  		 	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	  		 	 		if(ol.getKogus() < 0 && ol.getDiscount() > 0.1 && (order.getStatus() == 5 || order.getStatus() == 4)){
	  		 	 			repr = true;
	  		 	 			tagastus = false;
	  		 	 		}
	  		 	 	 } 
	      		    System.out.println("comp " + ord.getCompany());
	 		 makeOutputTelema(ord,tagastus, repr);
	    
	    
	  }else{
		  counter = order.getOrderLines().size();
	  }
		
	 
    //  makeOutput(order);
      setNumAddedLines(counter);
  }
  
    private HashMap eans = null;
    private HashMap prodnames = null;
    
    private void makeEans() throws Exception{
  	 
  	  	if(eans == null){
  	  		eans = new HashMap();
  	  		prodnames = new HashMap();
  	  	
  	      for (String[] row : readCSVData("Merisoft_MasterData_Products.csv")) {
       	   
  	    	   int cou = 0;
  	         String id = null;
  	      	 String name = null;
  	      	 String ean = null;

  	     	   for (String cell : row) {

  	          	 cell = cell.replaceAll("\"", " ").trim();
  	         	 if(cou == 0) {
  	                   id = cell.trim();
  	             }
  	         	 if(cou == 1) {
  	                name = cell.trim().replaceAll("'", " ").trim().replaceAll("  ", " ");
  	             }
  	         	 if(cou == 5) {
  	                ean = cell.trim();
  	             }
  	              cou++;
  	          }
  	        
  	     	   if(ean != null) {
  	     		   eans.put(id, ean);
  	     	     }
  	     	     prodnames.put(id, name);
  	           }
  	     	 }
 		   
  	  	  //  	System.out.println("oioi " + eans.size() + " " + prodnames.size());
  	  	
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
      
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + ls);
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
          
            if (ale == -1 ){
                ale = 0 ;
                }
            sb.append("      <ItemEntry>" + ls);
            sb.append("      <LineItemNum>"  + (i+1) + "</LineItemNum>" + ls);
          
           
            String toode = line.getToode() ;
            
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

             counter++; 
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

 	//
   try{
 	  Thread.sleep(10); 
   }catch(Exception e){
 	 e.printStackTrace(); 
   }
   
   
  String  fileName = escape(parent.getProps().getProperty("conv.dirxml") + agent) + "-" + counter + "." + sss  +  ".xml";
  
  System.out.println(fileName + " " + System.getProperty("file.encoding"));
   
           Converter.saveFileInUTF8(fileName, sb.toString());
   		   //log.error(" File Saved " + fileName );
   		 
			 fileName = escape(parent.getProps().getProperty("conv.dircxml") + agent) + "-" + counter + "." + sss  +  ".xml";
			 Converter.saveFileInUTF8(fileName, sb.toString());
			//log.error(" copy Saved " + fileName );
					 
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
