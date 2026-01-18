
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import org.apache.cxf.common.util.StringUtils;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

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

public class AxaNooImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public AxaNooImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
     

      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    }
    catch (Exception e) {
      parent.showError(e);
    }
    loadSignatures();
  }

  private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
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
  	
   

    return prodID;
  }
  private String trouble = null;

  

  public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
  
  
  public void makeOrder(Order order) throws SQLException{

	  counter = 0;

		
	 Order ord =  order;			  
			  java.util.Collections.sort(ord.getOrderLines(), new java.util.Comparator(){
					 
			        public boolean equals(Object obj) {
			          return false;
			        }

			       public int compare(Object o1, Object o2) {
			          OrderLine ol1 = (OrderLine)o1;
			          OrderLine ol2 = (OrderLine)o2;
			          return ol1.getTooteNimi().compareTo(ol2.getTooteNimi());
			        }
			           });
   		           ord.setTeostaja(order.getTeostaja());
   		           
   		        if(signatures == null) {
   		        	signatures = new Vector();
   		        }
   		           
   		        String signature = ord.getSignature();
   			  if(signatures.contains(signature)){
   				  this.trouble = "REPRETEATING ORDER agent :" + order.getAgent() + " customer :" 
   			  + Converter.deCode(order.getCompany()) ;
   			  }else{
   				 makeOutput(ord);
   			  signatures.add(signature);
			     
   			  }
   		           
   			 try {
					saveObject(signatures, "signatures.vec");
				} catch (IOException e) {
					e.printStackTrace();
				}
			  

      setNumAddedLines(counter);
  }

  
  
  private String getNimi(String aa) throws SQLException{
  	String res = null;
    String cmd = "       SELECT inv.itemid, inv.itemname AS nimetus FROM inventtable inv   " + 
    		     "    	 WHERE  inv.dataareaid='nlt'   and ltrim(inv.itemid) = '"  + aa + "' ";
    System.out.println("SQL " + cmd);
    		Connection con = this.getNewConnection();
			Statement st1 = con.createStatement();
			ResultSet rs1 = st1.executeQuery(cmd);
			
			 if (rs1.next()) {
			   res = rs1.getString("nimetus").trim();
			 }
			
	rs1.close();
	st1.close();
	con.close();
  	return res ;
  }
  
  
  
  
  
  private Calendar  getNextShipCal(String aa) throws SQLException{
  	Calendar res = null;
    String cmd = "       select  routeday from talrouteline l inner join talroutetable t on t.routeid=l.routeid and t.dataareaid=l.dataareaid " + 
    		" where ltrim(custaccount) = '"  + aa + "' and t.dataareaid='nlt'  order by routeday ";
    System.out.println("SQL " + cmd);
    		Connection con = this.getNewConnection();
			Statement st1 = con.createStatement();
			ResultSet rs1 = st1.executeQuery(cmd);
			
		//	Saame kõik 2 nädala päevad kätte 
			LocalDate now = new LocalDate();
			int paev = now.getDayOfWeek();
			LocalDate mon = now.withDayOfWeek(DateTimeConstants.MONDAY);
			LocalDate tue = mon.plusDays(1);
			LocalDate wed = mon.plusDays(2);
			LocalDate thu = mon.plusDays(3);
			LocalDate fri = mon.plusDays(4);
			LocalDate sat = mon.plusDays(5);
			LocalDate sun = mon.plusDays(6);
			LocalDate mon1 = mon.plusDays(7);
			LocalDate tue1 = mon.plusDays(8);
			LocalDate wed1 = mon.plusDays(9);
			LocalDate thu1 = mon.plusDays(10);
			LocalDate fri1 = mon.plusDays(11);
			LocalDate sat1 = mon.plusDays(12);
			LocalDate sun1 = mon.plusDays(13);
			
		
			LocalDate kaes = new LocalDate();
			ArrayList <LocalDate> aaa = new ArrayList<LocalDate>();
			 while (rs1.next()) {
			   int day =  rs1.getInt("routeday");
			   
			   System.out.println(day + " uuu " + paev);
			   
			   if(day == 1) {
				   aaa.add(mon1);
				   aaa.add(mon);
				   
			   }
			   if(day == 2) {
				   aaa.add(tue1);
				   aaa.add(tue);
			   }
			   if(day == 3) {
				   aaa.add(wed1);
				   aaa.add(wed);
			   }						
			   if(day == 4) {
				   aaa.add(thu1);
				   aaa.add(thu);
			   }
			   if(day == 5) {
				   aaa.add(fri1);
				   aaa.add(fri);
			   }
			   if(day == 6) {
				   aaa.add(sat1);
				   aaa.add(sat);
			   }
			   if(day == 7) {
				   aaa.add(sun1);
				   aaa.add(sun);
			   }
			 
			 }
			 
			 // aaa sees on päevad millal action toimub - nyyd onvaja leida tänasele päevale lähin neist .
			 Collections.sort(aaa);
			 Iterator<LocalDate> asd = aaa.iterator();
			 while (asd.hasNext()) {
				 LocalDate fff = asd.next();
				 if(fff.isAfter(now)) {
					 kaes = fff;
					 break;
				 }
			 }
			 
			 
	System.out.println("kaes " + kaes + " "  );
	
			 try {
				 if(kaes != null) {
						java.util.Date d = new SimpleDateFormat("yyyy-MM-dd").parse(kaes.toString());
						res = Calendar.getInstance();
						res.setTime(d);
				 }
		
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		System.out.println("päev tagasi " + kaes);
		
	rs1.close();
	st1.close();
	con.close();
  	return res ;
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
		String ref = order.getReference();
		if ( ref == null ){
			ref =  " ";
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
         if(fixagent.equals("mand")){
           //   fixagent = "m�nd";
         }
     
         String custID = order.getCompany();
         String ladu = null;
             Calendar  shipDate = Calendar.getInstance();
             shipDate.add(Calendar.DAY_OF_YEAR, 1);
             
             if( order.isShipDateUsed()) {
            	 shipDate = order.getShipCal();
             }else {
            	 shipDate = getNextShipCal(order.getCompany());
             }
            
             String shtime = "";
             if(order.getShipTime()!= null && order.getShipTime().trim().length() > 0) {
            	  shtime = "  Tarneaeg : " + order.getShipTime();
             }
           
             
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
              sb.append("<SenderID>-</SenderID>" + ls);
              sb.append("<ReceiverID>-</ReceiverID>" + ls); 	
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
          
           
           sb.append("<UserId>" +  agent + "</UserId>" + ls);
           sb.append("</DeliveryParty>" + ls);
           
       
           sb.append("</DocumentParties>" + ls);
         
           
           String agref ="";
           
           sb.append("<DocumentInfo>" + ls);
           sb.append("<DocumentSubType>MERISOFT</DocumentSubType>" + ls);
           sb.append("<DocumentName>MMT-ORDER</DocumentName>" + ls );
           sb.append("<CreatedBy>" +  agent + "</CreatedBy>" + ls);
           sb.append("<CREntryReferenceID>" + agref + "</CREntryReferenceID>" + ls);
           		

         
         	  sb.append("<RefInfo>" + ls);
         	 sb.append("<CREntryReferenceID>" + agref + "</CREntryReferenceID>" + ls);
         	  if(ref != null && ref.trim().length() > 0){
               sb.append("<SourceDocument type=\"order\">" + ls);
               sb.append("    <SourceDocumentNum>" + "" + "</SourceDocumentNum>" + ls );//escape(ref)
               sb.append("</SourceDocument>" + ls);
         	  }
               sb.append("</RefInfo>" + ls); 
         	
           
              
           
           String subType = "tellimus";
           
        /* if(smarten){
        	   sb.append(" <CreatedBy>" + agent + "</CreatedBy>" + ls ) ;	
         	  subType = "myyk";
         	  sb.append("<DocumentName>tellimus</DocumentName>" + ls); 
        }*/
           
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
                  sb.append("          <ItemDescription>" + Converter.cleanLitLatEst(escape(getNimi(line.getToode()))) + "</ItemDescription>" + ls);               
                  sb.append("         <AmountOrdered>" + escape(getNF().format(line.getKogus())) + "</AmountOrdered>" + ls);
                
               if(line.getDiscount() > 0){
             	  double discprice = line.getPrice() ;
             	 if(line.selActCode != null && line.selActCode.length() > 0) {
             		  sb.append("         <ItemPrice>" + escape(getNF().format(discprice)) + "</ItemPrice>" + ls); 
             		 sb.append("         <Addition addCode=\"DSC\"><AddContent>DISCOUNT</AddContent><AddRate>" +
                          	  escape(getNF().format(line.getDiscount())) + "</AddRate></Addition>" + ls); 
                           
             	 } else {

            		  sb.append("         <ItemPrice>" + escape(getNF().format(discprice)) + "</ItemPrice>" + ls); 
             		  sb.append("         <Addition addCode=\"DSC\"><AddContent>DISCOUNT</AddContent><AddRate>" +
                          	  escape(getNF().format(line.getDiscount())) + "</AddRate></Addition>" + ls); 
                       } 
             	 }
               
          //   System.out.println("lll " + line.selActCode + " " + line.getPartii() );
              
               if(line.selActCode != null && line.selActCode.trim().length() > 0) {
            	   sb.append("                <NLTItemBatch>" + line.selActCode  + "</NLTItemBatch>" + ls);
               }
           
     /*          sb.append("         <VAT>" + ls + "                <VATRate>" +  + "</VATRate>" + ls);
               sb.append("         </VAT>"+  ls);
               
               sb.append("         <ItemReserve>" + ls );
         
               sb.append("             <ItemReserveUnit>" + ls );
             
               
               sb.append("                <ItemUnit>" +  escape(yhik.trim()) + "</ItemUnit>" + ls );
               sb.append("                <AmountActual>" + getNF().format(line.getKogus()) + "</AmountActual>" + ls );
               sb.append("             </ItemReserveUnit>" + ls );
               sb.append("         </ItemReserve>" + ls );*/
               String ccc = "";
               
               if(line.getPartii() != null && line.getPartii().trim().length() > 0 ){
            	   ccc = line.getPartii();
               }
               
               
               sb.append("   <AdditionalInfo>" +ls);
               sb.append("       <Extension extensionId=\"remarks\">" + ls);
               sb.append("       <InfoName>Remark1</InfoName>");
               sb.append("            <InfoContent>" + convert(ccc) + "</InfoContent>" + ls );
               sb.append("       </Extension>");
         
                 //-<Extension extensionId="externalremarks"><InfoContent>K�esoleva arve aluseks olev n�ue on loovutatud AS-ile SEB Liising ja k�ik arvel toodud summad tuleb maksta AS-i SEB Liising arvelduskontole 10052045861006 SEB Pangas (kood 401). Kogu k�esoleva arvega seotud info tuleb teatavaks teha AS-ile SEB Liisingule.</InfoContent></Extension>
               sb.append("   </AdditionalInfo>" + ls);	
               
               
               sb.append("      </ItemEntry>" + ls);
     
               
               
           }
           

           sb.append("   </DocumentItem>" + ls);
           
     	    String com = convert(order.getInfo());
            
     	    
            String KT = "";
			String KOMPL = "";
			String ARVE = "";
		    
			if(com != null && com.trim().length()>0){
			if(com.indexOf("KT:")!=-1 && com.indexOf("KM:") != -1){
			      	KT = com.substring(com.indexOf("KT:"),com.indexOf("KM:"));
			} else if(com.indexOf("KT:")!=-1 && com.indexOf("AR:") != -1){
				    KT = com.substring(com.indexOf("KT:"),com.indexOf("AR:"));
			} else if(com.indexOf("KT:")!=-1){
				    KT = com.substring(com.indexOf("KT:"),com.length());
			}

			if(com.indexOf("AR:")!=-1 && com.indexOf("KM:") != -1){  
				KOMPL = com.substring(com.indexOf("KM:"),com.indexOf("AR:")); 	
			  }else if(com.indexOf("KM:") != -1){
			  	KOMPL = com.substring(com.indexOf("KM:"),com.length()); 	
			  }    

			if(com.indexOf("AR:")!=-1 ){  
				ARVE = com.substring(com.indexOf("AR:"),com.length());
			  }

			}
			
			if(ARVE.length() > 3){
				ARVE = ARVE.substring(3);
			}
			if(KT.length() > 3){
				KT = KT.substring(3);
			}
			if(KOMPL.length() > 3){
				KOMPL = KOMPL.substring(3);
			}
			
			if(ARVE.length() > 100){
				ARVE = ARVE.substring(0,100);
			}
			if(KT.length() > 100){
				KT = KT.substring(0,100);
			}
			if(KOMPL.length() > 100){
				KOMPL = KOMPL.substring(0,100);
			}
			
		
			
			System.out.println(com + "\nKT:" + KT + "KOMPL:" + KOMPL + "ARVE:" + ARVE);
     	    
             
             if(com != null && com.trim().length() > 0  || shtime.length() > 0){
             	  sb.append("   <AdditionalInfo>" +ls);
                   sb.append("       <Extension extensionId=\"remarks\">" + ls);
                   sb.append("       <InfoName>Remark1</InfoName>");
                   sb.append("            <InfoContent>" + escape(KT).trim() + "</InfoContent>" + ls );
                   sb.append("       </Extension>");
                   sb.append("       <Extension extensionId=\"remarks\">" + ls);
                   sb.append("       <InfoName>Remark2</InfoName>");
                   sb.append("            <InfoContent>" + escape(ARVE).trim() + shtime + "</InfoContent>" + ls );
                   sb.append("       </Extension>");
                   sb.append("       <Extension extensionId=\"remarks\">" + ls);
                   sb.append("       <InfoName>Remark3</InfoName>");
                   sb.append("            <InfoContent>" + escape(KOMPL).trim() + "</InfoContent>" + ls );
                   sb.append("       </Extension>");
                     //-<Extension extensionId="externalremarks"><InfoContent>K�esoleva arve aluseks olev n�ue on loovutatud AS-ile SEB Liising ja k�ik arvel toodud summad tuleb maksta AS-i SEB Liising arvelduskontole 10052045861006 SEB Pangas (kood 401). Kogu k�esoleva arvega seotud info tuleb teatavaks teha AS-ile SEB Liisingule.</InfoContent></Extension>
                   sb.append("   </AdditionalInfo>" + ls);	
             }
           
             
           sb.append("</Document>" + ls);
           sb.append("</E-Document>" + ls);
 
      counter = counter + cou;
    	//
    
      String fileName = escape(parent.getProps().getProperty("conv.dir") + agent + "-" + sss )  + "-" + custID +".xml";
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
 public String convert(String s) {
	 if(s == null) {
		 return null;
	 }
	 s = s.replaceAll("[^a-zA-Z0-9äõöüÄÕÖÜ.,:\\s+]", "");
	  return s.trim();
	}
 
	public NumberFormat getNF() {
		return this.nf;
	}
	


 
 
  public String getDriverVersion(){
    return "AxaNoo";
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

   
  private String url = "jdbc:odbc:AXA";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

  private int counter = 0;
  private ImportPanel parent;

public String getTrouble() {
	// TODO Auto-generated method stub
	return trouble;
}
}
