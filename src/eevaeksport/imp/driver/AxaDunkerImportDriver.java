
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
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

public class AxaDunkerImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public AxaDunkerImportDriver(ImportPanel parent) {
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
    Connection con = DriverManager.getConnection(url, "mobile","mobile");
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim() ;

    }
    rs1.close();
    st1.close();
    con.close();

    System.out.println("Klient: " + acc + " " + id);
    return acc;

  }

 
  public String identifyCustomerAGENT(String id) throws SQLException {
 	 String	sql1 = "SELECT SL01035 as agent FROM SL010200 left JOIN SY240200  ON SL01036=SY24002 " +
 	 		"and SY24001 ='BG' WHERE SL01001='" + id +"'"; 
 	
   System.out.println(sql1);
   String agent = null;
   Connection con = DriverManager.getConnection(url, "mobile","mobile");
   Statement st1 = con.createStatement();
   ResultSet rs1 = st1.executeQuery(sql1);

   while (rs1.next()) {
   	agent = rs1.getString("agent").trim();

   }
   rs1.close();
   st1.close();
   con.close();

   return agent;

 }
 
  

  
  public String getMTKood(int paevad) throws SQLException{
 	 
    String sql1 = " select SL22002 as kood from SL220200 where SL22003 =0 and SL22005 =  " + paevad + "";
    String kood = null;
    Connection con = DriverManager.getConnection(url, "mobile","mobile");
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      kood = rs1.getString("kood").trim();
    }
       System.out.println(paevad + " mtkood " + kood);
    rs1.close();
    st1.close();
    con.close();
    return kood;
 }
 
  
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	
    
    String sql2 = " SELECT RTRIM(SC01001) AS toode FROM SC010200  WHERE (RTRIM(SC01132)=   '" + ean.trim() 
                              + "' or RTRIM(SC01003) = '" + ean.trim() + "' or RTRIM(SC01060) = '" + ean.trim() + "') and SC01015 != ''";

    
    String sql1 ="";
    Connection con = DriverManager.getConnection(url, "mobile","mobile");
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
    	    con.close();
    }catch(Exception rr){
    rr.printStackTrace();	
    }
  

    return prod;
  }

  private String getCustName(String cuid) throws SQLException{
		 
		
		 String name = null;
	 	 
		    String sql2 = "SELECT cus.name as nimi FROM custtable cus " +
		            " WHERE cus.dataareaid='vir' and cus.accountnum = '" + cuid + "' ";
		    Connection con = DriverManager.getConnection(url, "mobile","mobile");
				Statement st1 = con.createStatement();
				ResultSet rs1 = st1.executeQuery(sql2);
				
				 while (rs1.next()) {
					 name = rs1.getString("nimi").trim();
				 }
				
		rs1.close();
		st1.close();
		con.close();
	  	
	  	
	  	System.out.println("oioi " + eans.size() );
	  	
	  	return name ;
	  }

  
  public void makeOrder(Order order) throws SQLException{

	  counter = 0;
	  
	  if(!order.getCompany().equals("OTHERSTUFF")){
		  Vector splitted =  splitOrder(order);
	 	  Enumeration en = splitted.elements();
	 	 makeEans();
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
  
  
  private HashMap eans = null;
  private HashMap prodnames = null;
    
    private HashMap makeEans() throws SQLException{
  	 
  	  	
  	  	if(eans == null){
  	  		eans = new HashMap();
  	  		prodnames = new HashMap();
  	  	
  	 	 String	 sql2 = "SELECT inv.itemid as toode, inv.itemname +' '+RTRIM(CAST(CAST(inv.DUNALCOHOLPERCENT AS decimal(15,1))"
  	 	 		+ " AS char(5)))+'%' AS nimetus , bar.itembarcode AS ean  FROM inventtable inv  " +
  	 		  " left JOIN pricedisctable disc ON inv.itemid=disc.itemrelation and disc.dataareaid= inv.dataareaid " +
  	 		  " left JOIN inventtablemodule mod ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid "+
  	 		  " left join inventitembarcode bar ON bar.dataareaid = inv.dataareaid and bar.itemid=inv.itemid and bar.useforinput  = 1 " +
  	 		  "  LEFT JOIN UNITCONVERT u ON inv.ITEMID=u.ITEMID AND u.DATAAREAID=inv.dataareaid  AND u.TOUNIT='kast'" +
  	 		  " WHERE disc.dataareaid='vir' and inv.dataareaid='vir' and inv.ssOrdererAccount='DUNKER' and u.dataareaid='vir'   AND mod.dataareaid='vir'  " +
  	 		  " and (disc.todate>getdate()-1 OR (disc.fromdate<=getdate() AND disc.todate=('1900-01-01 00:00:00.00') ) )  "+
  	 		  " AND moduletype=2 and blocked = 0  and inv.itemgroupid not in ('91','94') "+
  	 		  "  and disc.accountrelation = ''";
  	   Connection con = DriverManager.getConnection(url, "mobile","mobile");
  				Statement st1 = con.createStatement();
  				ResultSet rs1 = st1.executeQuery(sql2);
  				
  				 while (rs1.next()) {
  					 String id = rs1.getString("toode").trim();
  					 String name = rs1.getString("nimetus").trim();
  					  String ean = (rs1.getString("ean"));
  		                 if (ean != null &&ean.trim().length()>3){
  						 eans.put(id,ean);
  					 }
  		                 prodnames.put(id, name);
  				    
  				 }
  				
  		rs1.close();
  		st1.close();
  		con.close();
  	  	}
  	  	
  	  	System.out.println("oioi " + eans.size() );
  	  	
  	  	return eans ;
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
            if(line.getDiscount() > 0){
          	  double discprice = (100d -line.getDiscount())*line.getPrice()/100d ;
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

 
  public String identfySorting (String prodID){
	  	


	    String sql1 = " Select SC01015 + SC01001 AS sort FROM SC010200 pr  where pr.SC01001='" + prodID + "'";
	  log.info("1_LINE_SQL: " + sql1);

	    String sort = "000";

	 
	    Statement st1;
		try {
			   Connection con = DriverManager.getConnection(url, "mobile","mobile");
			st1 = con.createStatement();
		
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	sort = rs1.getString("sort");
	    //	System.out.println("sort k�es 1 ;" + sort );
	    	if(sort == null || sort.trim().length() == 0){
	    		sort = "xxx";
	    	}
	    }

	    rs1.close();
	    st1.close();
	    con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
// System.out.println("Sort k�es 2 ;" + sort );
	    return sort;
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
	    	  sb.append(ol.getToode() + sep);
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

	    
	      boolean makeout = true;
	      if(order.getStatus() == 60 && !order.getCompName().equals("order")){
	    //	  makeout = false;
	      }
	     
	    	if(makeout){
		    	  String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + order.getAgent() +"."+ order.getCompany()+"." +fn  + ".csv";
			      System.out.println(fileName);		
			      if(minus){
			    	  fileName = escape(parent.getProps().getProperty("conv.dir2")) +  "t-" + order.getAgent() +"."+ order.getCompany()+"." +fn  + ".csv";
			      }
			      FileOutputStream fos = new FileOutputStream(fileName);
			        fos.write(sb.toString().getBytes());
			        fos.close();
	    		
	    	}

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
  
  private String url = "jdbc:odbc:AXA";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

  private int counter = 0;
  private ImportPanel parent;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
