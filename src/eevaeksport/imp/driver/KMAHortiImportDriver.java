
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

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

public class KMAHortiImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(KMAHortiImportDriver.class.getName());

  
  public KMAHortiImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
   //   con = DriverManager.getConnection(url, "mobile","mobile");

      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);

    } catch (Exception e) {
      parent.showError(e);
      log.error(e);
    }
    loadSignatures();
    }
    private Vector signatures = new Vector();

    
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
  		
  		if (f.exists() && f.lastModified() < ss.getTimeInMillis() - 9200000) {// kui on yle tunni vana siis maha 
  			f.delete();
  			signatures = new Vector();
  		} else {
  			signatures = (Vector) readObject("signatures.vec");
  		}

  		
  	} catch (Exception e) {
  		signatures = new Vector();
  	}

  	}
  private TreeMap prods = new TreeMap();
  private String url2 = "jdbc:odbc:kma";


  	   
 

  public String identifyCustomer(String id,String regnr) throws SQLException {
    return id;
 }

  
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	return prodID;
  }

 
  
  private Vector splitOrder (Order order){
	  
	  Vector result = new Vector();

	  if (order.getDokNr() == null || order.getDokNr().trim().length() == 0){
		  order.setDokNr((Calendar.getInstance().getTimeInMillis() + "").substring(3));
	  }
	//  System.out.println("Infokene " + order.getInfo() + " " + order.getDokNr());

	  Order ordS = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ordS0 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ordSG = new Order(order.getCompany(),order.getAgentFirstName());
	  
	  ordS.setAgentCode(order.getAgentCode());
	  ordS.setDokNr(order.getDokNr());
	  ordS.setCompName(order.getShipMode());
	  ordS.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordS.setStatus(order.getStatus()); 
	  ordS.setInfo(order.getInfo());
	  ordS.setWarehouse(order.getWarehouse());
	  ordS.setDealer("order");

	  ordS0.setAgentCode(order.getAgentCode());
	  ordS0.setDokNr(order.getDokNr());
	  ordS0.setCompName(order.getShipMode());
	  ordS0.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordS0.setStatus(order.getStatus());
	  ordS0.setInfo(order.getInfo());
	  ordS0.setWarehouse(order.getWarehouse());
	  ordS0.setDealer("100");

	  
	  ordSG.setAgentCode(order.getAgentCode());
	  ordSG.setDokNr(order.getDokNr());
	  ordSG.setCompName(order.getShipMode());
	  ordSG.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordSG.setStatus(order.getStatus());
	  ordSG.setWarehouse(order.getWarehouse());
		
	  ordSG.setInfo(order.getInfo());
	  ordSG.setDealer("gift");


	  
	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
		  
		  

		    if(lin.getDiscount() == -2){
		     	ordSG.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		    	ordSG.setDokNr(order.getDokNr().substring( 0,order.getDokNr().length()-1) + "G");
			    ordSG.setReference(order.getReference());
		     }
		
		    if(lin.getDiscount() == 0){
		    	   ordS.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     	ordS.setDokNr(order.getDokNr());
			     	ordS.setReference(order.getReference()); 
			 }
		    

		    if(lin.getDiscount() > 0){
		    	   ordS0.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     	ordS0.setDokNr(order.getDokNr().substring( 0,order.getDokNr().length()-1) + "N");
			     	ordS0.setReference(order.getReference()); 
			 }
		  
	  }
	  
	  if(ordS.getOrderLines() != null && ordS.getOrderLines().size()>0){
		  result.add(ordS);  
	  }

	  if(ordS0.getOrderLines() != null && ordS0.getOrderLines().size()>0){
		  result.add(ordS0);  
	  }

	  if(ordSG.getOrderLines() != null && ordSG.getOrderLines().size()>0){
		  result.add(ordSG);  
	  }
	  
	  
	 System.out.println("Result =" +order.getDokNr()
			 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }



  
  
  
  
  public void makeOrder(Order order) throws SQLException{

	  
	  if (signatures == null){
   	   signatures = new Vector();
      }
	  counter = 0;
	  
	  String signature = order.getSignature();
	
	  System.out.println("signature" + signatures.contains(signature) + "  "+ signature );
		
	  if(signatures.contains(signature)){
		  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
	  } else {
		
 	  makeOutput(order);  
 	  signatures.add(signature);
 	  
	  }
 	    
      try {
		saveObject(signatures, "signatures.vec");
	} catch (IOException e) {
		e.printStackTrace();
	}
	  
	  
          trouble = null;
  }

  public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}

  private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    sb.append((cal.get(Calendar.YEAR)+"")+ "-");
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

  
  
  private void makeOutput(Order order){
  	StringBuffer sb = new StringBuffer();
  	 nf.setGroupingUsed(false);

  	try {
  	  int cou = 0;
 		
 				
 		String ware = order.getWarehouse();
 	//	System.out.println("ladu tellimusel " + ware);
 	
 		 String doknr = order.getDokNr();
    
         String subType = "order";
    

          String ls = System.getProperty("line.separator");
          String custID = order.getCompany();
     
          if(order.getStatus() > 5 && order.getDealer() != null && order.getDealer().trim().length() > 0 ){
        	  custID = order.getDealer();
          }
           
          String ladu = null;
          Calendar  shipDate = order.getShipCal();
              
          String code = order.getWarehouse();        
              if (code != null){
                ladu = code;
              }
            
            if (ladu == null) {
              ladu = "";
            }
   
       

       	 boolean laks = false;
      	 boolean tagastus = false;
       	 int nega = 0;
       	Vector lin = order.getOrderLines();
  	 	 for (int i = 0; i < lin.size(); i++) {
  	 		OrderLine ol = (OrderLine) lin.elementAt(i);
  	 		//System.out.println("XXXXX orderStatus " + ord.getStatus());
  	 		if(ol.getKogus() < 0 && ol.getDiscount() > 0 && 
  	 				(order.getStatus() == 5 || order.getStatus() == 4)){
  	 			nega++;
  	 			laks = true;
  	 		}
  	 	 }  
  		
  	 	  if(nega == lin.size()){
		    	tagastus = true;
		    }
            
        
            Calendar cal = Calendar.getInstance();
           
            if (shipDate != null) {
                  cal = shipDate;
                }
          
            sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + ls);
            sb.append("<E-Document>" + ls);
       
            sb.append("<Header>" + ls);
            sb.append("<DateIssued>" + escape(getDateString(Calendar.getInstance())) + "</DateIssued>" + ls);
         //   sb.append("<Test>no</Test>" + ls);
           // sb.append("<SenderID>02</SenderID>" + ls);
           // sb.append("<ReceiverID>2473</ReceiverID>" + ls); 	
               
            sb.append("</Header>" + ls);
            sb.append("<Document>" + ls);
            
            String type = "order";
            
            double kordaja = 1;

            if( order.getStatus() == 1){ // on pakkumine 
   	 			type = "offer";
   	 			subType = "offer";
   	 		  }
            if( order.getStatus() == 2){ // on pakkumine 
   	 			type = "return";
   	 			subType = "return";
   	 		kordaja = -1;
   	 		  }
       
            
            if( order.getStatus() == 4){ // on pakkumine 
   	 			type = "reprice";
   	 			subType = "reprice";
   	 		    kordaja = -1;
   	 		  }
       
            if(order.getWarehouse() != null && order.getWarehouse().equals("OMA")) {
            	type = "carsales";
   	 			subType = "carsales";
   	 		if( order.getStatus() == 2){
   	 		type = "carreturn";
	 			subType = "carreturn";
   	 		}
            }
            
            
            sb.append("<DocumentType>" + type + "</DocumentType> " + ls);
   
      //	 sb.append("<DocumentFunction>original</DocumentFunction> " + ls);
            
            sb.append("<DocumentParties>" + ls);
            //String regnr = getRegKood(custID);
          	  sb.append("<BuyerParty>" + ls);
               sb.append( "<PartyCode>" +  escape((custID)) + "</PartyCode>" + ls); 
              // sb.append( "<RegNum>" +  escape(regnr) + "</RegNum>" + ls); 
             
               sb.append("</BuyerParty>" + ls);
            
               sb.append("<OrderParty>" + ls);
               sb.append( "<PartyCode>" +  escape(custID) + "</PartyCode>" + ls);
            //   sb.append( "<RegNum>" +  escape(regnr) + "</RegNum>" + ls); 
               sb.append("</OrderParty>" + ls);
            
               sb.append("<DeliveryParty>" + ls);
               sb.append("<PartyCode>" +  escape(custID) + "</PartyCode>" + ls); 
             //  sb.append( "<RegNum>" +  escape(regnr) + "</RegNum>" + ls); 
          String agent = order.getAgent();
         if( order.getAgentCode() != null && !agent.equals(order.getAgentCode())){
        	 agent =  order.getAgentCode();
         }
            
            sb.append("<UserId>" +  agent + "</UserId>" + ls);
            sb.append("</DeliveryParty>" + ls);
            
            
            sb.append("<SellerParty>" +ls);
            sb.append("<PartyCode>-</PartyCode>" +ls);
            sb.append("</SellerParty>" +ls);
            
            sb.append("</DocumentParties>" + ls);
          
            
            sb.append("<DocumentInfo>" + ls);

            sb.append("<DocumentType>" + subType + "</DocumentType>" + ls); 
            sb.append("<DocumentSubType>MERISOFT</DocumentSubType>" + ls );
            sb.append("<DocumentName>MERISOFT-ORDER</DocumentName>" + ls );
            String ref = order.getTeostaja(); //mobiilse telli ref number
			if ( ref == null ){
				ref =  "";
			}
			if(ref.endsWith("null")){
				ref = ref.substring(0, ref.length()-4);
			}

            if(ref != null && ref.trim().length() > 0){
          	  sb.append("<RefInfo>" + ls);
                sb.append("<SourceDocument type=\"order\">" + ls);
               
                sb.append("    <SourceDocumentNum>" + escape(ref).trim() + "</SourceDocumentNum>" + ls );
                sb.append("</SourceDocument>" + ls);
                sb.append("</RefInfo>" + ls); 
            }

            if(order.getShipMode() != null && order.getShipMode().trim().length() > 0 ){
            	 sb.append("<DocumentShipMode>" + escape(order.getShipMode()).trim()+ "</DocumentShipMode>" + ls); 
                  	
            }
            String add = (Calendar.getInstance().getTimeInMillis() + "").substring(11);
            
            sb.append("<DocumentNum>MRS" + escape(order.getDokNr()).trim() + add + "</DocumentNum>" + ls); 
            sb.append("<DateInfo>" + ls);
            sb.append("<OrderDate>" +escape(getDateString(Calendar.getInstance())) + "</OrderDate>" + ls);
           
            sb.append("<DeliveryDateRequested>" +  escape(getDateString(cal)) + "</DeliveryDateRequested>" + ls); //aeg taha panna  T10:00:00
            
            String time = "";
           if(order.getShipTime()!= null && order.getShipTime().trim().length() > 0){
            	time = order.getShipTime();
            }
            if(time != null  && time.trim().length() > 0){ 
          
            	      sb.append("   <AdditionalInfo>" +ls);
      	  		      sb.append("       <Extension extensionId=\"DeliveryTime\">" + ls);
            	      sb.append("            <InfoName>Time</InfoName>" + ls );
                      sb.append("            <InfoContent>" + time.trim() + "</InfoContent>" + ls );
                      sb.append("       </Extension>" +ls);
                      sb.append("   </AdditionalInfo>" + ls);
            }
            
            
            sb.append("</DateInfo>" + ls);
            sb.append("</DocumentInfo>" + ls);
            sb.append("   <DocumentItem>" + ls);
            
            Vector sortedLines = order.getOrderLines();
            for (int i = 0; i < sortedLines.size(); i++) {
                OrderLine line = (OrderLine)sortedLines.elementAt(i);
                double ale = line.getDiscount();     
                counter++;
                String ean = line.getEAN();
                if (ale == -1 ){
                    ale = 0 ;
                    }
                sb.append("      <ItemEntry>" + ls);
                sb.append("      <LineItemNum>"  + (i+1) + "</LineItemNum>" + ls);
              
               
               // <GTIN>4740072222752</GTIN> 
               // <BaseUnit>TK</BaseUnit> 
                sb.append("         <SellerItemCode>" + line.getToode() + "</SellerItemCode>" + ls);
                String nimi = "";
                if( ean == null || ean.length() < 6 ) {
                	Product prod = (Product)this.prods.get(line.getToode());
                	if(prod!= null){
                		 ean = prod.getEAN();	
                		 nimi = prod.getName();
                	}
                	
                  }
                if(order.getStatus() > 4){
                	Product prod = (Product)this.prods.get(line.getToode());
                	if(prod!= null){
                		 nimi = prod.getName();
                	}
                		
                }
                
                if(ean != null){
                	sb.append("         <GTIN>" + escape(ean) + "</GTIN>" + ls);
                }
                
                   sb.append("         <ItemUnitRecord>" + ls);
                   sb.append("                <ItemUnit>" + line.getUnit() + "</ItemUnit>" + ls);
                   sb.append("         </ItemUnitRecord>" + ls);
                   
               // if(smarten)
                   sb.append("          <ItemDescription>" + Converter.cleanLitLatEst(escape(nimi)) + "</ItemDescription>" + ls);               
                   sb.append("         <AmountOrdered>" + escape(getNF().format(line.getKogus()* kordaja)) + "</AmountOrdered>" + ls);
                 
                if(line.getDiscount() > 0){
              
             double discprice = (100d -line.getDiscount())*line.getPrice()/100d ;
                sb.append("         <ItemPrice>" + escape(getNF().format(line.getPrice())) + "</ItemPrice>" + ls); 
                sb.append("         <ItemSum>" + escape(getNF().format(line.getPrice() * line.getKogus()* kordaja)) + "</ItemSum>" + ls);
                sb.append("         <Addition addCode=\"DSC\">" + ls);
                sb.append("              <AddRate>" + escape(getNF().format(line.getDiscount())) + "</AddRate>" + ls);
                sb.append("          </Addition>" + ls);
                }
                
               //((ol.getPrice() - (ol.getPrice()*(disco/100)))/jaga)
                sb.append("      </ItemEntry>" + ls);
      
                
                
            }
            

            sb.append("   </DocumentItem>" + ls);
            
           
String com = null;


if(order.getInfo() != null && order.getInfo().trim().length() >0){
		com =  order.getInfo() ;
		com = stripNonValidXMLCharacters(com);
	}
           



			
boolean addh = false;
              StringBuffer ccc = new StringBuffer();
              if(com != null  && com.trim().length() > 0){
            	  addh = true;
            	  if(com.length() > 70){
            		  com = com.substring(0,70);
            	  }
              	      ccc.append("       <Extension>" + ls);
                      ccc.append("            <InfoContent>" + com + "</InfoContent>" + ls );
                      ccc.append("       </Extension>" +ls);
              }
            
              
            
              
              if(addh){
            	  sb.append("   <AdditionalInfo>" +ls);
            	  sb.append(ccc.toString());
            	  sb.append("   </AdditionalInfo>" + ls);	
              }
              

              
            sb.append("</Document>" + ls);
            sb.append("</E-Document>" + ls);
  
      
       String fn = doknr;
       if(doknr.trim().length()== 0){
     	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
       }
       String fileName = escape(parent.getProps().getProperty("conv.dir") + agent)  + "." + fn  +  ".xml";
       System.out.println(fileName);
       Converter.saveFileInUTF8(fileName, sb);

       setNumAddedLines(counter); 
       
 	} catch (Exception e) {
 		e.printStackTrace();
 	}
  }

 
	public NumberFormat getNF() {
		return this.nf;
	}
	
	
	  
 
 
  public String getDriverVersion(){
    return "KMAHortiImport";
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


  public static String stripNonValidXMLCharacters(String in) {
      StringBuffer out = new StringBuffer(); // Used to hold the output.
      char current; // Used to reference the current character.

      if (in == null || ("".equals(in))) return ""; // vacancy test.
      for (int i = 0; i < in.length(); i++) {
          current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
          if ((current == 0x9) ||
              (current == 0xA) ||
              (current == 0xD) ||
              ((current >= 0x20) && (current <= 0xD7FF)) ||
              ((current >= 0xE000) && (current <= 0xFFFD)) ||
              ((current >= 0x10000) && (current <= 0x10FFFF)))
              out.append(current);
      }
      return out.toString();
  }   
  
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
 
  private int counter = 0;
  private ImportPanel parent;
  private String trouble = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return trouble;
}
}
