
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
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
import java.util.Locale;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

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

public class MoxLihaImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public MoxLihaImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url, "root", "er0te");

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
  	String sql1 = null;
  	
  	 System.out.println("Kliendi M��ramine: " );
  	 
  	 if(id.length() > 10){
  		 id = id.substring(id.length()-10);
  	 }
  	
  	    sql1 = "SELECT SL01001 AS kood FROM SL010200 where SL01106  =  '" + id + "'";	
	
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim() ;

    }
    rs1.close();
    st1.close();

    System.out.println("Klient: " + acc + " " + id);
    return acc;

  }

 


 
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	
    
    String sql2 = " SELECT RTRIM(SC01001) AS toode FROM SC010200  WHERE (RTRIM(SC01132)=   '" + ean.trim() 
                              + "' or RTRIM(SC01003) = '" + ean.trim() + "' or RTRIM(SC01060) = '" + ean.trim() + "') and SC01015 != ''";

    
    String sql1 ="";
        
        Statement st1 = con.createStatement();
        ResultSet rs1  = null;
        
    String prod = null;
    if(prodID != null){
    	sql1 = "SELECT RTRIM(SC01001) AS toode FROM SC010200  WHERE RTRIM(SC01001)=  '" + prodID.trim() + "' or  RTRIM(SC01001)= left ('" + prodID.trim() + "',4)";	

     rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }	
      }
   System.out.println(ean + " prod0 " + prod);
    if(prod == null || prod.trim().length() == 0){

      rs1 = st1.executeQuery(sql2);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
      }
    }

    if(prod == null && ean.trim().length() > 6){
  	  while(ean != null && ean.startsWith("0")){
	    	ean = ean.substring(1);
	    	 rs1 = st1.executeQuery( " SELECT RTRIM(SC01001) AS toode FROM SC010200  WHERE (RTRIM(SC01132)=   '" + ean.trim() 
	                  + "' or RTRIM(SC01003) = '" + ean.trim() + "' or RTRIM(SC01060) = '" + ean.trim() + "') and SC01015 != ''" );
		      if (rs1.next()) {
		        prod = rs1.getString("toode").trim();
		      }
		        
  	  }
	     
	    }
    
    System.out.println(prodID + " prod1 " + prod);
    try{
    	  rs1.close();
    	    st1.close();	
    }catch(Exception rr){
    rr.printStackTrace();	
    }
  

    return prod;
  }


  private Vector signatures = new Vector();
  private String trouble = null;
  
  public void makeOrder(Order order) throws SQLException{

	  counter = 0;

		   
     			   
     			  String signature = order.getSignature();
     			    if(signatures == null){
     			    	signatures = new Vector();
     			    }
     			    
     				  if(signatures.contains(signature)){
     					  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
     					    		  
     				  }else{
     					 
     					          makeOutput(order);
     					   		  signatures.add(signature);
     				     
     				   try {
     						saveObject(signatures, "signatures.vec");
     					} catch (IOException e) {
     						e.printStackTrace();
     					}
     				  
     				  }
     			   
     			   
     			   
     			   
     			   
	    	   

      setNumAddedLines(counter);
  }


     				 public static void saveObject(Object obj, String file) throws IOException {
     					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
     					oos.writeObject(obj);
     					oos.close();
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
	  
  
  
 private void makeOutput(Order order){
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
		if(!ware.equals("92")){
			ware = "10";
		}
		 String doknr = order.getDokNr();
          

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
           sb.append("<Standard>HULGI</Standard>" + ls);
              sb.append("<SenderID>ELIHA</SenderID>" + ls);
              sb.append("<ReceiverID>ELIHA</ReceiverID>" + ls); 	
              sb.append("<Channel>MERISOFT</Channel>" + ls); 	
         

              
           sb.append("</Header>" + ls);
           sb.append("<Document>" + ls);
           
           String type = "order";
      
           sb.append("<DocumentType>" + type + "</DocumentType> " + ls);
  
     	 sb.append("<DocumentFunction>ORIGINAL</DocumentFunction> " + ls);
           
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
           if(agent.equals("mand")){
            //   agent = "m�nd";
          }
           
           sb.append("<UserId>" +  agent + "</UserId>" + ls);
           sb.append("</DeliveryParty>" + ls);
           
       
           sb.append("</DocumentParties>" + ls);
         
           
           
           sb.append("<DocumentInfo>" + ls);
           sb.append("<DocumentSubType>MERISOFT</DocumentSubType>" + ls);
           sb.append("<DocumentName>MERISOFT-ORDER</DocumentName>" + ls );
           		

         
         	  sb.append("<RefInfo>" + ls);
         	  if(ref != null && ref.trim().length() > 0){
               sb.append("<SourceDocument type=\"order\">" + ls);
               sb.append("    <SourceDocumentNum>" + escape(ref) + "</SourceDocumentNum>" + ls );
               sb.append("</SourceDocument>" + ls);
         	  }
               sb.append("</RefInfo>" + ls); 
         	
           

           
           String subType = "tellimus";
           
           
       String    sss = (Calendar.getInstance().getTime().getTime()+ "").substring(5) ;
           sb.append("<DocumentName>" + subType + "</DocumentName>" + ls); 
           sb.append("<DocumentNum>" + escape(agent + "-" + sss )+ "</DocumentNum>" + ls); 
           sb.append("<DateInfo>" + ls);
           sb.append("<OrderDate>" +escape(getDateString(Calendar.getInstance())) + "</OrderDate>" + ls);
           String time = "T00:01:00";

           sb.append("<DeliveryDateRequested>" +  escape(getDateString(cal)) + "</DeliveryDateRequested>" + ls); //aeg taha panna  T10:00:00
           //  seal l�pus T10:00:00
           // p�hiveo puhul T01:00:00
           sb.append("</DateInfo>" + ls);
           sb.append("</DocumentInfo>" + ls);
           
           sb.append("<DocumentSumGroup>" + ls );
           sb.append("<Currency>EUR</Currency>" + ls);
           sb.append("<DocumentSum>10.81</DocumentSum>" + ls );
           sb.append("<VAT><SumBeforeVAT>10.81</SumBeforeVAT><VATRate>20</VATRate><VATSum>2.162</VATSum></VAT>" + ls);
           sb.append("<TotalSum>12.972</TotalSum>" + ls);
           sb.append("</DocumentSumGroup>" + ls);    	  
           sb.append("   <DocumentItem>" + ls);



           
           Vector sortedLines = order.getOrderLines();
           for (int i = 0; i < sortedLines.size(); i++) {
               OrderLine line = (OrderLine)sortedLines.elementAt(i);
               double ale = line.getDiscount();     
               counter++;
               String ean = line.getEAN();
               String yhik = line.getUnit();
               if (ale == -1 ){
                   ale = 0 ;
                   }
               sb.append("      <ItemEntry>" + ls);
               sb.append("      <LineItemNum>"  + (i+1) + "</LineItemNum>" + ls);
             
              
              // <GTIN>4740072222752</GTIN> 
              // <BaseUnit>TK</BaseUnit> 
               sb.append("         <SellerItemCode>" + line.getToode() + "</SellerItemCode>" + ls);
               if( ean==null || ean.length() < 6 ) {
              //    ean = getEan(line.getToode());
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
               sb.append("         <ItemPrice>" + escape(getNF().format(discprice)) + "</ItemPrice>" + ls); 
             
                
               }
              
           
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
                   sb.append("       <InfoName>Remark1</InfoName>");
                   sb.append("            <InfoContent>" + escape(comm) + "</InfoContent>" + ls );
                   sb.append("       </Extension>");
                     //-<Extension extensionId="externalremarks"><InfoContent>K�esoleva arve aluseks olev n�ue on loovutatud AS-ile SEB Liising ja k�ik arvel toodud summad tuleb maksta AS-i SEB Liising arvelduskontole 10052045861006 SEB Pangas (kood 401). Kogu k�esoleva arvega seotud info tuleb teatavaks teha AS-ile SEB Liisingule.</InfoContent></Extension>
                   sb.append("   </AdditionalInfo>" + ls);	
             }
           
             
           sb.append("</Document>" + ls);
           sb.append("</E-Document>" + ls);
 
      counter = counter + cou;
    	//
      String fn = doknr;
      if(doknr.trim().length()== 0){
    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
      }
      String fileName = escape(parent.getProps().getProperty("conv.dir") + agent)  + "." + fn  +  ".xml";
      System.out.println(fileName);
      Writer out = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream(fileName), "UTF-8"));
			try {
			    out.write(sb.toString());
			} finally {
			    out.close();
			}

	} catch (Exception e) {
		e.printStackTrace();
	}
 }
 
 
	public NumberFormat getNF() {
		return this.nf;
	}
	


 
 
  public String getDriverVersion(){
    return "ScalaSaleImport";
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
  
  private String url = "jdbc:odbc:moxuus";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private int counter = 0;
  private ImportPanel parent;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
