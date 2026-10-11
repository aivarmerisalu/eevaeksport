package eevaeksport.imp.driver;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;
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

public class ViaImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public ViaImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
 
  }

  public String identifyCustomer(String id,String regnr) throws SQLException {
     return id;
  }

  public String identifyProduct(String ean, String prodID) throws SQLException{
    String prod = null;
    return prod;
  }


 
  public void makeOrder(Order order) throws SQLException{
	      setOrder(order);
	      makeOutput(order);	 
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
	  


  
  
  public String getImportFile(){
	   return parent.getProps().getProperty("imp.read");
	 }
  
  private void makeOutput(Order order){
	 	StringBuffer sb = new StringBuffer();
	 	 nf.setGroupingUsed(false);
	 	counter = 0;
	    String number = "";
	 	try {
			String ref = order.getReference();
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
			if(!ware.equals("92")){
				ware = "10";
			}
			 String doknr = order.getDokNr();
	         if(doknr.length()>6){
	         	// doknr = doknr.substring(doknr.length()-6,doknr.length());
	         }
	                 

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
	      
	           String partnerID = "D00024";
	           if(parent.getProps().getProperty("partnerID") != null){
	        	   partnerID = parent.getProps().getProperty("partnerID");
	           }
	           sb.append("<Header>" + ls);
	           sb.append("<DateIssued>" + escape(getDateString(Calendar.getInstance())) + "</DateIssued>" + ls);
	           sb.append("<CharSet>UTF-8</CharSet>" + ls);
	           String senderID = "VIA3L";
	           if(parent.getProps().getProperty("senderID") != null){
	        	   senderID = parent.getProps().getProperty("senderID");
	           }
	           sb.append("<SenderID>" + senderID + "</SenderID>" + ls);
	           sb.append("<ReceiverID>" + partnerID + "</ReceiverID>" + ls);      
	           sb.append("</Header>" + ls);
	           
	           sb.append("<Document>" + ls);
	           
	           String type = "DESORDRSP";
	           sb.append("<DocumentType>" + type + "</DocumentType> " + ls);
	           sb.append("<DocumentFunction>original</DocumentFunction> " + ls);
	           sb.append("<DocumentParties>" + ls);
	           
	           sb.append("<SellerParty>" +ls);
	           sb.append("<PartyCode>" + partnerID + "</PartyCode>" +ls);
	           sb.append("</SellerParty>" +ls);

	         	  sb.append("<BuyerParty>" + ls);
	              sb.append( "<PartyCode>-</PartyCode>" + ls);
	              sb.append("<DeptID>" +  escape(custID) + "</DeptID>" + ls); 
	              sb.append("</BuyerParty>" + ls);
	           
	              sb.append("<DeliveryParty>" + ls);
	              sb.append("<PartyCode>-</PartyCode>" + ls); 
	              sb.append("<DeptID>" +  escape(custID) + "</DeptID>" + ls); 
	         String agent = order.getAgent();
	           if(fixagent != null){
	         	  agent = fixagent;
	           }
	       //    sb.append("<UserId>" +  agent + "</UserId>" + ls);
	           sb.append("</DeliveryParty>" + ls);
	           sb.append("</DocumentParties>" + ls);
	         
	           sb.append("<DocumentInfo>" + ls);
	           if(senderID.equals("LogPluss")){
	        	   if(ref.startsWith("2")){
	        		   ref = "1" + ref.substring(1);
	        	   }
	           }
                if(ref != null && ref.trim().length() > 0){
	         	  sb.append("<RefInfo>" + ls);
	               sb.append("<SourceDocument type=\"order\">" + ls);
	               sb.append("    <SourceDocumentNum>" + escape(ref) + "</SourceDocumentNum>" + ls );
	               sb.append("</SourceDocument>" + ls);
	               sb.append("</RefInfo>" + ls); 
	             }

	           String subType = "Despatch Order Response";
	           
	      
	           sb.append("<DocumentName>" + subType + "</DocumentName>" + ls); 
	           String inff = order.getInfo();
	           
	           if(senderID.equals("LogPluss")){
	        	   if(inff.startsWith("2")){
	        		   inff = "1" + inff.substring(1);
	        	   }
	           }
	           sb.append("<DocumentNum>" + escape(inff) + "</DocumentNum>" + ls); 
	            
	           sb.append("<RefInfo>" + ls );
	           if(senderID.equals("VIA3L")){
	        	   number = order.getDokNr().substring(2);
		           if(number.startsWith("-")){
		        	   number = number.substring(1);
		           }   
	           }else{
	        	    number =  order.getDokNr();
	           }
	            
	           sb.append("<TransportReferenceId>" + number + "</TransportReferenceId>" + ls);
	           sb.append("</RefInfo>" +ls);

	           sb.append("<DateInfo>" + ls);
	           
	           Calendar ccc = Calendar.getInstance();
	           ccc.setTimeInMillis(order.getShipCal().getTimeInMillis());
	           ccc.add(Calendar.DAY_OF_YEAR,order.getPaymTerm());
	           
	           sb.append("<IssueDate>" +escape(getDateString(order.getShipCal())) + "</IssueDate>" + ls);
	           sb.append("<DueDate>" +escape(getDateString(ccc)) + "</DueDate>" + ls);
	                   
	           sb.append("<DeliveryDateActual>" +  escape(getDateString(order.getShipCal())) + "</DeliveryDateActual>" + ls); 
	           sb.append("</DateInfo>" + ls);
	           sb.append("</DocumentInfo>" + ls);
	     
	           sb.append("<DocumentSumGroup>" + ls);
	           sb.append("<DocumentSum>" + getNF().format( order.getKogusumma()) + "</DocumentSum>" + ls);
	           sb.append("<VAT>" + ls);
	           sb.append("<SumBeforeVAT>" + getNF().format(order.getKogusumma()) + "</SumBeforeVAT>" + ls);
	           sb.append("<VATSum>" + getNF().format(order.getKmsumma())+ "</VATSum>" + ls);
	           sb.append("</VAT>" + ls);
	           sb.append("<TotalSum>" +getNF().format( (order.getKmsumma() + order.getKogusumma())) + "</TotalSum>" + ls);           
	           sb.append("<Currency>EUR</Currency>" + ls);    	  	           
	           sb.append("</DocumentSumGroup>" + ls);
	           
	           sb.append("   <DocumentItem>" + ls);

	           
	           Vector sortedLines = order.getOrderLines();
	           for (int i = 0; i < sortedLines.size(); i++) {
	               OrderLine line = (OrderLine)sortedLines.elementAt(i);
	               counter++;
	               String ean = line.getEAN();
	     
	               sb.append("      <ItemEntry>" + ls);
	               sb.append("      <LineItemNum>"  + (i+1) + "</LineItemNum>" + ls);
	               sb.append("         <ItemDescription>" + Converter.cleanLitLatEst(line.getTooteNimi()) + "</ItemDescription>" + ls);
	               sb.append("         <SellerItemCode>" + line.getToode() + "</SellerItemCode>" + ls);	               
	               sb.append("         <GTIN>" + escape(ean) + "</GTIN>" + ls);
                   sb.append("         <AmountOrdered>" + escape(getNF().format(line.getKogus())) + "</AmountOrdered>" + ls);
                   sb.append("         <AmountDespatched>" + escape(getNF().format(line.getKogus())) + "</AmountDespatched>" + ls);
	               sb.append("         <ItemPrice>" + escape(getNF().format(line.getPrice())) + "</ItemPrice>" + ls); 
	               sb.append("         <ItemSum>" + escape(getNF().format(line.getPrice()*line.getKogus())) + "</ItemSum>" + ls); 
	               sb.append("         <ItemUnitRecord>" + ls);
	               sb.append("                <ItemUnit>" + line.getUnit() + "</ItemUnit>" + ls);
	               sb.append("         </ItemUnitRecord>" + ls);	 
	               sb.append("         <BaseUnit>" + escape(line.getUnit()) + "</BaseUnit>" + ls);
	              
	               sb.append("         <RefInfo>" + ls);
	               sb.append("         <SourceDocument  type=\"order\">" + ls);
	               
                   sb.append("         <SourceDocumentNum>" + escape(inff) + "</SourceDocumentNum>" + ls);
	               sb.append("         </SourceDocument>" + ls);
	               sb.append("         </RefInfo>" + ls);
	              
	               sb.append("         <ItemReserve>" + ls );
	               sb.append("           <ItemReserveRecord>" + ls );
	               sb.append("             <ReserveAmount>" + getNF().format(line.getKogus()) + "</ReserveAmount>" + ls );
	               String kuup = line.getPartii();
	               if(kuup != null && kuup.trim().length() == 8){
	            	   //eeldame et on kuup�ev kui on 8 kohta ja lisane asjad vahele 
	            	 //  kuup = kuup.substring(0,4) + "-" + kuup.substring(4,6)+ "-" + kuup.substring(6,8);
	            	   kuup = kuup.substring(6,8) + "." + kuup.substring(4,6)+ "." + kuup.substring(0,4);
	               }
	               sb.append("             <TaxStamps>" + escape(kuup) + "</TaxStamps>" + ls );
	               sb.append("           </ItemReserveRecord>" + ls );
	               sb.append("         </ItemReserve>" + ls );
                   sb.append("      </ItemEntry>" + ls);
                   setNumAddedLines(getNumAddedLines() + 1);
	               
	               
	           }
	           

	           sb.append("   </DocumentItem>" + ls);
	           
	     	    String comm = order.getInfo();
	             if(comm != null && comm.length()>100){
	             	comm = comm.substring(0,100);
	             }
	             
	             if(comm != null && comm.trim().length() > 0 ){
	             	  sb.append("   <AdditionalInfo>" +ls);
	                   sb.append("       <Extension extensionId=\"internalremarks\">" + ls);
	                   sb.append("            <InfoContent>" + escape(comm) + "</InfoContent>" + ls );
	                   sb.append("       </Extension>");
	                     //-<Extension extensionId="externalremarks"><InfoContent>K�esoleva arve aluseks olev n�ue on loovutatud AS-ile SEB Liising ja k�ik arvel toodud summad tuleb maksta AS-i SEB Liising arvelduskontole 10052045861006 SEB Pangas (kood 401). Kogu k�esoleva arvega seotud info tuleb teatavaks teha AS-ile SEB Liisingule.</InfoContent></Extension>
	                   sb.append("   </AdditionalInfo>" + ls);	
	             }
	           
	             
	           sb.append("</Document>" + ls);
	           sb.append("</E-Document>" + ls);
	 
	    this.setNumAddedLines(counter);
	      
	    	//
	      String fn = number;
	      if(number.trim().length()== 0){
	    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
	      }
	      String fileName = escape(parent.getProps().getProperty("conv.dir") + agent)  + "." + fn  +  ".xml";
	      System.out.println(fileName);
	      
	      Converter.saveFileInUTF8(fileName, sb);
	      

		} catch (Exception e) {
			e.printStackTrace();
		}
	 }


	public NumberFormat getNF() {
		return this.nf;
	}
	
  public String getDriverVersion(){
    return "Via";
  }
  private int counter = 0;
  
  private NumberFormat calNF = NumberFormat.getInstance();

  public static final String cleanLitLatEst(String ret){
	    if (ret != null){
	      
	        ret = ret.replaceAll("�","e");
	        ret = ret.replace((char) 0x3f,' ');
	        
	        
	       // ret = ret.replaceAll("?","s");
	       // ret = ret.replaceAll("?","c");
	     // ret = ret.replace('',' ');
	        ret = ret.replaceAll("�","I");
	        ret = ret.replaceAll("�","u");
	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","z");
	        ret = ret.replaceAll("�","s");
	        ret = ret.replaceAll("�","S");
	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","I");
	        ret = ret.replaceAll("�","u");
	        ret = ret.replaceAll("�","i");
	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","Z");
	        ret = ret.replaceAll("�","E");
	        ret = ret.replaceAll("�","U");
	        ret = ret.replaceAll("�","U");
	        ret = ret.replaceAll("�","Pg.");
	        ret = ret.replaceAll("�","e");
	        
	        
	        ret = ret.replaceAll("�","Z");
	        ret = ret.replaceAll("�","A");
	        ret = ret.replaceAll("�","a");
	        ret = ret.replaceAll("�","C");

	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","E");
	        ret = ret.replaceAll("�","e");
	        ret = ret.replaceAll("�","G");
	        ret = ret.replaceAll("�","g");
	        ret = ret.replaceAll("�","I");
	        ret = ret.replaceAll("�","i");
	        ret = ret.replaceAll("�","K");
	        ret = ret.replaceAll("�","k");
	        ret = ret.replaceAll("�","L");
	        ret = ret.replaceAll("�","l");
	        ret = ret.replaceAll("�","N");
	        ret = ret.replaceAll("�","n");
	        
	        ret = ret.replaceAll("�","o");
	        ret = ret.replaceAll("�","a");
	        ret = ret.replaceAll("�","o");
	        ret = ret.replaceAll("�","u");
	       
	        ret = ret.replaceAll("�","O");
	        ret = ret.replaceAll("�","A");
	        ret = ret.replaceAll("�","O");
	        ret = ret.replaceAll("�","U");
	    }
	        
	        if(ret == null || ret.trim().length()==0){
	        return " ";
	        }else {
	        	System.out.println(ret);
	        return ret;
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



  private void setOrder(Order order){
    this.order = order;
  }

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private ImportPanel parent;
  private Order order = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
