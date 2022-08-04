
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
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
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

public class IceAxaUusImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public IceAxaUusImportDriver(ImportPanel parent) {
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



  public String identifyCustomer(String id,String regnr){
	  	 String sql1 = "";
	  	 if(regnr != null && regnr.trim().length()>0){
	  	 	sql1 = "SELECT accountnum FROM custtable cus inner join DirPartyTable dp with(nolock) ON cus.partY=dp.REcid  "
	  	 			+ "where ouraccountnum='" + id +   "' and orgnumber ='" + regnr + "' and dataareaid='dat'";
	  	     	
	  	 }else{
	  	 	sql1 = "SELECT accountnum FROM custtable where ouraccountnum='" + id +   "'  and dataareaid='dat'";
	  	    
	  	 }
	  	String sql3 = "SELECT accountnum FROM custtable where (ltrim(accountnum)= '" + id + "' or ouraccountnum='" + id + "') and dataareaid='dat'";
		String sql4 = "SELECT accountnum FROM custtable where accountnum= '" + id + "' and dataareaid='dat'";
		  
	  	String acc = null;
	try{
		Connection con = DriverManager.getConnection(url, "mobilejava", "mobilejava");
	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	    while(rs1.next()){
	      acc = rs1.getString("accountnum").trim();

	   }
	    System.out.println("1" +acc);
	    System.out.println(sql1);
		
		
	 rs1.close();
	 st1.close();


	   Statement st3 = con.createStatement();
	   ResultSet rs3 = st3.executeQuery(sql3);

	    while(rs3.next()){
	      acc = rs3.getString("accountnum").trim();
	   }
	    System.out.println("2" +acc);
		
	    
	 rs3.close();
	 st3.close();

	 
	 if(acc == null){
		
	 Statement st4 = con.createStatement();
	 ResultSet rs4 = st4.executeQuery(sql4);

	  while(rs4.next()){
	    acc = rs4.getString("accountnum").trim();
	 }
	  System.out.println("3" +acc);
	  System.out.println(sql4);
	  
	rs4.close();
	st4.close();
     

	 }
	 
	 con.close();

	}catch(Exception e){
		e.printStackTrace();
	}


	     return acc;
	  }

	  public String identifyProduct(String ean, String prodID){
	    String sql1 = "SELECT itemid FROM inventtable where itemid='" + prodID + "' and dataareaid='dat'";
	    String sql2 = "SELECT itemid FROM inventtable where namealias='" + ean + "' and dataareaid='dat'";

	    String prod = null;
	try{
		Connection con = DriverManager.getConnection(url, "mobilejava", "mobilejava");
	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      prod = rs1.getString("itemid").trim();
	    }

	    if(prod == null){

	      rs1 = st1.executeQuery(sql2);

	      while (rs1.next()) {
	        prod = rs1.getString("itemid").trim();
	      }
	    }
	    rs1.close();
	    st1.close();
		con.close();
	    }catch(Exception e){
		e.printStackTrace();
	    }
	    
	    return prod;
	  }
	   

  
  public void makeOrder(Order order) throws SQLException{
            counter = 0;
     		makeOutput(order);
      System.out.println("counter " + counter);
      setNumAddedLines(counter);
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
	  
  
  public TreeMap getProducts(Agent agent){
	  
		
	    
	    String cmd = "SELECT inv.recid, inv.itemid, erpt.name as nimetus,\r\n" + 
	    		"              left(ItemBuyerGroupId,4) as abix , disc.amount AS price,\r\n" + 
	    		"              NAMEALIAS AS ean ,ItemBuyerGroupId AS grupp, inv.netweight as grr, inv.minprice as minprice, uomcv.factor as kastis ,"
	    		+ "case when bomunitid = 'pk' then (Select factor from unitofmeasureconversion  u where  u.FROMUNITOFMEASURE = 5637144580 AND u.TOUNITOFMEASURE = 5637144579 and \r\n" + 
	    		"  	  u.product = inv.recid)   when   bomunitid = 'kg' then (Select factor from unitofmeasureconversion  u where  u.FROMUNITOFMEASURE = 5637144580 AND u.TOUNITOFMEASURE = 5637144577 and \r\n" + 
	    		"  	  u.product = inv.recid) else  1 end as kastis2, bomunitid as unit FROM inventtable inv  \r\n" + 
	    		"INNER JOIN    inventtablemodule mod ON mod.itemid = inv.itemid AND mod.dataareaid=inv.dataareaid\r\n" + 
	    		"LEFT JOIN     ecoresproducttranslation erpt ON inv.product = erpt.product \r\n" + 
	    		" LEFT JOIN    ecoresproduct es ON es.displayproductnumber = inv.itemid\r\n" + 
	    		"    		LEFT JOIN     unitofmeasureconversion uomcv ON uomcv.product = es.recid" + 
	    		"              AND uomcv.FROMUNITOFMEASURE = 5637144580 AND uomcv.TOUNITOFMEASURE = 5637144578\r\n" + 
	    		"INNER JOIN pricedisctable disc ON inv.itemid=disc.itemrelation AND inv.dataareaid=disc.dataareaid \r\n" + 
	    		"AND (getdate()>=disc.fromdate OR disc.fromdate=('1900-01-01 00:00:00.00')) AND (disc.todate>getdate() OR disc.todate=('1900-01-01 00:00:00.00')) \r\n" + 
	    	
	    		"WHERE  disc.dataareaid='dat' AND disc.accountrelation='Pood' AND inv.dataareaid='dat' AND mod.moduletype=2 AND balsenditem=1\r\n" + 
	    		"AND erpt.languageid = 'et'";
	          
	    	   
	    TreeMap products = new TreeMap();
	    TreeMap recids = new TreeMap();
	    try {
	      
	    	Connection con = DriverManager.getConnection(url, "mobilejava", "mobilejava");
		    Statement st1 = con.createStatement();
		    ResultSet rs = st1.executeQuery(cmd);	
	    	
	    	
	      while(rs.next()){
	        // if ( kood.length() > 2 ){
	    	  String recid = rs.getString("recid").trim();
	        String kood = rs.getString("itemid").trim();
	        Product prod = new Product(kood);
	        
	        String nimetus =  rs.getString("nimetus").trim();
	        prod.setNameSorting(kood + "" +nimetus);
	        boolean goes = true ;
	        if (nimetus.indexOf("***") != -1){
	        	goes = false;
	        }
	        prod.setName(nimetus);
	       
	        prod.setPrice(rs.getDouble("price"));
	        prod.setEAN(rs.getString("ean"));
	        prod.setGroup(parent.make(rs.getString("grupp")));
	       // double maxdisc = rs.getDouble("maxdisc");
	       double maxdisc = 0;
	      //  if(soodused.get(prod.getID()) != null){
	        //	maxdisc = Double.parseDouble(soodused.get(prod.getID()) + "");
	        	//System.out.println("sssMD:" + (int)maxdisc+ "% " +nimetus.trim());
	            
	        //}
	        
	        if(maxdisc > 0){
	        	prod.setMaxDiscount(maxdisc);
	        	prod.setStatusDiscount("1");
	        	prod.setName(nimetus.trim() + " MD:" + (int)maxdisc+ "%");
	        	System.out.println("MD:" + (int)maxdisc+ "% " +nimetus.trim());
	        }
	        
	       
	        
	        prod.setStock(0);
	        prod.setVat("20");
	    
	        double wei =rs.getDouble("grr");
	    
	        if(wei >0.999){
	        	prod.idabi = "1";
	        	//System.out.println("oh ja suur " +prod.getID() );
	            
	        }
	       
	 double oh = rs.getDouble("minprice");
	        
	        if( oh > 0) {
	         	 // prod.setMinPrice(oh);
	        // 	  prod.setPartii("Min.Hind : " + parent.getNF().format(oh) + "€");
	           }
	        
	        
	        prod.setInCases(rs.getDouble("kastis"));
	        prod.setUnit(rs.getString("unit"));
	        
	        if(goes){
	        	 products.put(kood, prod);
	        }
	      }
	      rs.close();
	      st1.close();
	      con.close();
	    
	                            
	    } catch (Exception e){
	     e.printStackTrace();
	    }

	    
	    
	    
	    return products;
	  }


  
  
 private void makeOutput(Order order){
 	StringBuffer sb = new StringBuffer();
 	 nf.setGroupingUsed(false);
 	 TreeMap tooted = getProducts(null);

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
		 String doknr = order.getDokNr();

         String ls = System.getProperty("line.separator");
         String fixagent = order.getAgent(); 
      
         String custID = order.getCompany();
          Calendar  shipDate = order.getShipCal();
           Calendar cal = Calendar.getInstance();
          
           if (shipDate != null) {
                 cal = shipDate;
               }
         
           sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + ls);
           sb.append("<E-Document>" + ls);
      
           sb.append("<Header>" + ls);
           sb.append("<DateIssued>" +  Converter.escapeXML(getDateString(Calendar.getInstance())) + "</DateIssued>" + ls);
           sb.append("<Standard>HULGI</Standard>" + ls);
              sb.append("<SenderID>BALBIINO</SenderID>" + ls);
              sb.append("<ReceiverID>BALBIINO</ReceiverID>" + ls); 	
              sb.append("<Channel>MERISOFT</Channel>" + ls); 	
         

              
           sb.append("</Header>" + ls);
           sb.append("<Document>" + ls);
           
           String type = "order";
      
           sb.append("<DocumentType>" + type + "</DocumentType> " + ls);
  
     	 sb.append("<DocumentFunction>ORIGINAL</DocumentFunction> " + ls);
           
           sb.append("<DocumentParties>" + ls);
       //    String regnr = getRegKood(custID);
         	  sb.append("<BuyerParty>" + ls);
              sb.append( "<PartyCode>" +   Converter.escapeXML((custID)) + "</PartyCode>" + ls); 
          //    sb.append( "<RegNum>" +  escape(regnr) + "</RegNum>" + ls); 
            
              sb.append("</BuyerParty>" + ls);
           
              sb.append("<OrderParty>" + ls);
              sb.append( "<PartyCode>" +   Converter.escapeXML(custID) + "</PartyCode>" + ls);
            //  sb.append( "<RegNum>" +  escape(regnr) + "</RegNum>" + ls); 
              sb.append("</OrderParty>" + ls);
           
              sb.append("<DeliveryParty>" + ls);
              sb.append("<PartyCode>" +   Converter.escapeXML(custID) + "</PartyCode>" + ls); 
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
              
           if(order.getStatus() > 5) {
        	   ref = doknr;
           }
           if(ref != null && ref.trim().length() > 0 ){
         	  sb.append("<RefInfo>" + ls);
         	   sb.append("<SourceDocument type=\"order\">" + ls);
               sb.append("    <SourceDocumentNum>" +  Converter.escapeXML(ref) + "</SourceDocumentNum>" + ls );
               sb.append("</SourceDocument>" + ls);
         	   sb.append("</RefInfo>" + ls); 
           }

           
           String subType = "tellimus";
           
        /* if(smarten){
        	   sb.append(" <CreatedBy>" + agent + "</CreatedBy>" + ls ) ;	
         	  subType = "myyk";
         	  sb.append("<DocumentName>tellimus</DocumentName>" + ls); 
        }*/
           
       String    sss = (Calendar.getInstance().getTime().getTime()+ "").substring(5) ;
           sb.append("<DocumentName>" + subType + "</DocumentName>" + ls); 
           sb.append("<DocumentNum>" +  Converter.escapeXML(agent + "-" + sss )+ "</DocumentNum>" + ls); 
           sb.append("<DateInfo>" + ls);
           sb.append("<OrderDate>" + Converter.escapeXML(getDateString(Calendar.getInstance())) + "</OrderDate>" + ls);
           String time = "T00:01:00";

           sb.append("<DeliveryDateRequested>" +   Converter.escapeXML(getDateString(cal)) + "</DeliveryDateRequested>" + ls); //aeg taha panna  T10:00:00
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
               String unit = line.getUnit() ;
  			  
    			if( tooted.get(line.getToode()) != null) {
    				Product pr = (Product)tooted.get(line.getToode());
    				unit = pr.getUnit();
    			}
               
              sb.append("         <BaseUnit>" + unit + "</BaseUnit>" + ls);
             	  sb.append("         <ItemUnitRecord>" + ls);
             	 
             			
                  sb.append("                <ItemUnit>" + unit+ "</ItemUnit>" + ls);
                  sb.append("         </ItemUnitRecord>" + ls);
                  
              // if(smarten)
                //  sb.append("          <ItemDescription>" + cleanLitLatEst(escape(getNimi(line.getToode()))) + "</ItemDescription>" + ls);               
                  sb.append("         <AmountOrdered>" +  Converter.escapeXML(getNF().format(line.getKogus())) + "</AmountOrdered>" + ls);
                
               if(line.getDiscount() > 0 && order.getStatus() < 3){
             	 // double discprice = (100d -line.getDiscount())*line.getPrice()/100d ;
               sb.append("         <ItemPrice>" +  (getNF().format(line.getPrice())) + "</ItemPrice>" + ls); 
               sb.append("         <ItemSum>" + (getNF().format(line.getPrice() * line.getKogus())) + "</ItemSum>" + ls);
               sb.append("         <Addition addCode=\"DSC\">" + ls);
               sb.append("              <AddRate>" + (getNF().format(line.getDiscount())) + "</AddRate>" + ls);
               sb.append("          </Addition>" + ls);
                
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
                   sb.append("            <InfoContent>" +  Converter.escapeXML(comm) + "</InfoContent>" + ls );
                   sb.append("       </Extension>");
                     //-<Extension extensionId="externalremarks"><InfoContent>K�esoleva arve aluseks olev n�ue on loovutatud AS-ile SEB Liising ja k�ik arvel toodud summad tuleb maksta AS-i SEB Liising arvelduskontole 10052045861006 SEB Pangas (kood 401). Kogu k�esoleva arvega seotud info tuleb teatavaks teha AS-ile SEB Liisingule.</InfoContent></Extension>
                   sb.append("   </AdditionalInfo>" + ls);	
             }
           
             
           sb.append("</Document>" + ls);
           sb.append("</E-Document>" + ls);
 
      counter = counter + cou;
    	//
      String fn = doknr;
      fn = fn.replace('/', '-');
      fn = fn.replace('\\', '-');
      if(doknr.trim().length()== 0){
    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
      }
      String fileName =  Converter.escapeXML(parent.getProps().getProperty("conv.dir") + agent)  + "." + fn  +  ".xml";
      System.out.println(fileName);
      Writer out = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream(fileName), "UTF-8"));
			try {
			    out.write(sb.toString());
			} finally {
			    out.close();
			}

			  if(comm != null){
				  Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(order.getAgent());
		    	    Converter.sendCommentEmail( comm,null,order.getDokNr(),order.getCompany(), 
		    	    		" " + getCustName(order.getCompany()) , " ",false,ag,parent);
		         }
			
	} catch (Exception e) {
		e.printStackTrace();
	}
 }
 
 private String getCustName(String code){
	 
	  String res = "noname";
	  String sql = "SELECT dp.name, cus.companychainid as payer, cus.accountnum, (SELECT SUM(amountcur) FROM custtransopen co WHERE co.duedate<getdate() \r\n" + 
	    		"                 AND co.accountnum = cus.accountnum) AS debt,  cus.linedisc ,  adr.address,''  AS visit,'' AS useean,'' AS mituladu,\r\n" + 
	    		"				  dim.DISPLAYVALUE AS agent, cus.memo AS agent2,\r\n" + 
	    		"				  cus.blocked, (select l.locator  from  LOGISTICSELECTRONICADDRESS l WITH (NOLOCK)  where l.recid = dp.primarycontactphone) as phone,\r\n" + 
	    		"				  '' AS celluralphone,  '' as kett ,  cp.name as chain ,  custclassificationid as type,\r\n" + 
	    		"				  adr.zipcode as indeks,  adr.state as region,   adr.city, adr.street,\r\n" + 
	    		"				  cus.paymtermid as pay\r\n" + 
	    		"             FROM  custtable cus with(nolock)  LEFT JOIN custgroup cp on cp.custgroup = cus.custgroup and cp.dataareaid=cus.dataareaid \r\n" + 
	    		"			 LEFT OUTER JOIN DEFAULTDIMENSIONVIEW dim ON cus.PARTITION = dim.PARTITION AND cus.DEFAULTDIMENSION = dim.DEFAULTDIMENSION AND dim.NAME = N'Agent'\r\n" + 
	    		"			 inner join DirPartyTable dp with(nolock) ON cus.partY=dp.REcid  inner join \r\n" + 
	    		"			 LOGISTICSPOSTALADDRESS adr ON adr.LOCATION = dp.PRIMARYADDRESSLOCATION and getdate() >=VALIDFROM  and  getdate() <= VALIDTO left outer JOIN  paymterm  \r\n" + 
	    		"                        pay on cus.paymtermid = pay.paymtermid  and pay.DATAAREAID  = cus.DATAAREAID \r\n" + 
	    		"                              WHERE   cus.dataareaid='dat' and  cus.accountnum = '" + code + "'  "; 
	    // 

	   System.out.println(sql);
	   try {
		
		    Connection con = DriverManager.getConnection(url, "mobilejava", "mobilejava");
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(sql);
	        
	        while(rs.next()){
	        	 res = rs.getString("name").trim();
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
 
	public NumberFormat getNF() {
		return this.nf;
	}
	


 
 
  public String getDriverVersion(){
    return "IceAxaUusImport";
  }





  
  public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  
  private NumberFormat calNF = NumberFormat.getInstance();

  
	
  private String url = "jdbc:odbc:AXAUUS";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
 
  private int counter = 0;
  private ImportPanel parent;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
