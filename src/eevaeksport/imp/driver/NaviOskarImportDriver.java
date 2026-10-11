
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
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

public class NaviOskarImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(NaviOskarImportDriver.class.getName());

  
  public NaviOskarImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    getProducts(null);
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
  }
  private TreeMap prods = new TreeMap();
  private TreeMap eans = new TreeMap();
  private String url2 = "jdbc:odbc:navi";
  private Hashtable dates = new Hashtable();
  	  
  	private Connection getNewConnection2() throws SQLException{
  	    return DriverManager.getConnection(url2, parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));
  	  }
  	
  	 public TreeMap getProducts(Agent agent){
  	     TreeMap tm = new TreeMap();
  	        	 
  	  
  	     try {
  	     	String cmd = "  SELECT t.[No_] AS toode,t.[Search Description] AS nimi, t.[Unit Price] AS myygihind,t.[Product Group Code] AS grupp ,t.[Item Category Code] AS grupp2 " +  
  			"	 ,t.[Base Unit of Measure] AS unit,[Cross-Reference No_] AS ean,'' AS status ,0 AS maxdiscount, " +
  			"	 '' AS type, [Qty_ per Unit of Measure] AS kastis,  '' AS kaal, [VAT Prod_ Posting Group] AS vat  " +
  			"	 ,1 as goes " +
  			"	  FROM [Oskar LT$Item] t LEFT JOIN [Oskar LT$Item Unit of Measure] vat ON (vat.[Item No_]=[No_] and vat.[Code]='KAST' ) " + 
  			"	  left join  [Oskar LT$Sales Price] sp on sp.[Item No_]=t.[No_] and sp.[Sales Code] ='00' and sp.[Starting Date] < getdate()+5  and " +
  			"	   (sp.[Ending Date] > getdate()+1 or year (sp.[Ending Date]) = 1753) " +
  			"	   left join [Oskar LT$Item Cross Reference] ref on ref.[Item No_]=t.[No_] and [Cross-Reference Type]=3  " +
  			"	  WHERE t.[Blocked]=0  and  len(t.[Search Description]) != 0  and t.[Unit Price] >0   	  ORDER BY t.[No_]"; 
  	     	
  	      System.out.println(cmd);
  	      
  	     	Connection con = getNewConnection2();
  	        Statement stmt = con.createStatement();
  	        ResultSet rs = stmt.executeQuery(cmd);
  	        while(rs.next()){
  	            String id = rs.getString("toode").trim();
  	            Product prod = new Product(id);
  	         	String name = rs.getString("nimi").trim();
  	         	name =   Converter.firstToUpperCase(name) + " " +id;
  	             double price = rs.getDouble("myygihind");
  	                prod.setPrice(price);
  	                String grupp = Converter.enCodeEst(rs.getString("grupp"));
  	                
  	                //asendame brandiga kui tootel pole gruppi 
  	              if(grupp != null && grupp.trim().length()>0 ){
  	               prod.setGroup(grupp.trim());  
  	              }
  	          
  	                prod.setUnit(rs.getString("unit"));
  	              String ean = (rs.getString("ean"));
  	                 if (ean != null &&ean.trim().length()>3){
  	                   prod.setEAN(ean.trim());
  	                      eans.put(prod.getID(), ean.trim());
  	                 }
  	        
  	                 double	cases = rs.getDouble("kastis");
  	                	if(cases>0){
  	                      prod.setInCases(cases);
  	                	}



  	                          	
  	                   String add = "";
  	                   if(id.length() > 5){
  	                	   add = id.substring(0,5);
  	                   }
  	                	 prod.setName(Converter.enCodeEst(name) );
  	                     tm.put(add + name, prod); 
  	        }
  	  
  	        rs.close();
  	        stmt.close();
  	        con.close();
  	     } catch (Exception e){
  	       e.printStackTrace();
  	     }
  	     return tm;
  	   }


  	   
 

  public String identifyCustomer(String id,String regnr) throws SQLException {
    return id;
 }

 
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	
    if(prodID != null){
    	return prodID;
    }
  //  System.out.print("eans "+ eans.size() + " " + eans.get(ean));
    if(eans.get(ean) != null){
    	return eans.get(ean) + "";
    }
    return prodID;
  }


  
  
  
  public void makeOrder(Order order) throws SQLException{

	  counter = 0;
	   trouble = "Imoprdin tellimust " + order.getAgent() + "." + order.getDokNr() + ".txt aga HANSA faili ei tekkinud ";
		
	 //Order ord =  order;	
	/* try{
		  java.util.Collections.sort(ord.getOrderLines(), new java.util.Comparator(){
				 
		        public boolean equals(Object obj) {
		          return false;
		        }

		       public int compare(Object o1, Object o2) {
		          OrderLine ol1 = (OrderLine)o1;
		          OrderLine ol2 = (OrderLine)o2;
		          return ol1.getToode().compareTo(ol2.getToode());
		        }
		           }); 
	 }catch(Exception r ){
		 log.error(r);
	 }*/
		
   		        //   ord.setTeostaja(order.getTeostaja());
     			   makeOutput(order);
	    	   
        trouble = null;
      setNumAddedLines(counter);
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

 /* public static String getDateString(java.util.Date date){
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);

    String month = (cal.get(Calendar.MONTH) + 1) + "";
    if (month.length() == 1) month = "0" + month;
    String day = cal.get(Calendar.DAY_OF_MONTH) + "";
    if (day.length() == 1) day = "0" + day;
    String dateString = day + "." + month + "." + cal.get(Calendar.YEAR);
    return dateString;
  }*/

  

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
   
       

        
            Calendar cal = Calendar.getInstance();
           
            if (shipDate != null) {
                  cal = shipDate;
                }
          
            sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + ls);
            sb.append("<E-Document>" + ls);
       
            sb.append("<Header>" + ls);
            sb.append("<DateIssued>" + escape(getDateString(Calendar.getInstance())) + "</DateIssued>" + ls);
            sb.append("<Test>no</Test>" + ls);
            sb.append("<SenderID>02</SenderID>" + ls);
            sb.append("<ReceiverID>2473</ReceiverID>" + ls); 	
               
            sb.append("</Header>" + ls);
            sb.append("<Document>" + ls);
            
            String type = "order";
       
            sb.append("<DocumentType>" + type + "</DocumentType> " + ls);
   
      	 sb.append("<DocumentFunction>original</DocumentFunction> " + ls);
            
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
            if(fixagent != null){
          	  agent = fixagent;
            }
            sb.append("<UserId>" +  agent + "</UserId>" + ls);
            sb.append("</DeliveryParty>" + ls);
            
            
            sb.append("<SellerParty>" +ls);
            sb.append("<PartyCode>-</PartyCode>" +ls);
            sb.append("</SellerParty>" +ls);
            
            sb.append("</DocumentParties>" + ls);
          
            
            sb.append("<DocumentInfo>" + ls);
            sb.append("<DocumentSubType>MMT</DocumentSubType>" + ls );
            sb.append("<DocumentName>MMT-ORDER</DocumentName>" + ls );


          /*  if(ref != null && ref.trim().length() > 0){
          	  sb.append("<RefInfo>" + ls);
                sb.append("<SourceDocument type=\"order\">" + ls);
               
                sb.append("    <SourceDocumentNum>" + escape(ref).trim() + "</SourceDocumentNum>" + ls );
                sb.append("</SourceDocument>" + ls);
                sb.append("</RefInfo>" + ls); 
            }*/

            
            String subType = "tellimus";
            
         /* if(smarten){
         	   sb.append(" <CreatedBy>" + agent + "</CreatedBy>" + ls ) ;	
          	  subType = "myyk";
          	  sb.append("<DocumentName>tellimus</DocumentName>" + ls); 
         }*/
            
        String    sss = (Calendar.getInstance().getTime().getTime()+ "").substring(5) ;
       // if(ref != null && ref.trim().length() > 0){
        	//sss = ref;
        //}
        if(((OrderLine)(OrderLine)order.getOrderLines().elementAt(0)).getKogus() < 0){
        	   sb.append(" <DocumentSubType>return</DocumentSubType>" ); 	
        }
        
            sb.append("<DocumentName>" + subType + "</DocumentName>" + ls); 
            sb.append("<DocumentNum>MOB" + escape(order.getDokNr()).trim()+ "</DocumentNum>" + ls); 
            sb.append("<DateInfo>" + ls);
            sb.append("<OrderDate>" +escape(getDateString(Calendar.getInstance())) + "</OrderDate>" + ls);
            String time = "T00:01:00";

            sb.append("<DeliveryDateRequested>" +  escape(getDateString(cal)) + "</DeliveryDateRequested>" + ls); //aeg taha panna  T10:00:00
            //  seal lõpus T10:00:00
            // põhiveo puhul T01:00:00
            sb.append("</DateInfo>" + ls);
            sb.append("</DocumentInfo>" + ls);
           // if(smarten){
             //   sb.append("<DocumentSumGroup>" + ls + "<Currency>EUR</Currency>" + ls + "</DocumentSumGroup>" + ls);    	  
            //}
            
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
                String nimi = "";
                if( ean == null || ean.length() < 6 ) {
                	Product prod = (Product)this.prods.get(line.getToode());
                	if(prod!= null){
                		 ean = prod.getEAN();	
                		 nimi = prod.getName();
                	}
                	
                  }
                if(order.getStatus() > 3){
                	Product prod = (Product)this.prods.get(line.getToode());
                	if(prod!= null){
                		 nimi = prod.getName();
                	}
                		
                }
                
                sb.append("         <GTIN>" + escape(ean) + "</GTIN>" + ls);
              	  sb.append("         <ItemUnitRecord>" + ls);
                   sb.append("                <ItemUnit>tk</ItemUnit>" + ls);
                   sb.append("         </ItemUnitRecord>" + ls);
                   
               // if(smarten)
                   sb.append("          <ItemDescription>" + cleanLitLatEst(escape(nimi)) + "</ItemDescription>" + ls);               
                   sb.append("         <AmountOrdered>" + escape(getNF().format(line.getKogus())) + "</AmountOrdered>" + ls);
                 
                if(line.getDiscount() > 0){
                	/* <ItemPrice>30.6</ItemPrice> <!—sisestati käsitsi
                               <ItemBasePrice>34</ItemBasePrice> <!—baashind hinnakirja järgi
                               <ItemSum>459</ItemSum>
                               <Addition addCode="DSC"> <!--peab oleama „DSC“, muidu NAV ignoreerib seda. 
                                 <AddRate>10</AddRate> <!-- see tuleb arvutada 34*15 = 510 ja 30.6 *15 = 459, 459/510=0,9 ehk allahindluse % = 10
                                 <!-- rea põhine allahindluse protsent -->
                               </Addition>
*/
                	
              	  double discprice = (100d -line.getDiscount())*line.getPrice()/100d ;
                sb.append("         <ItemPrice>" + escape(getNF().format(line.getPrice())) + "</ItemPrice>" + ls); 
                sb.append("         <ItemSum>" + escape(getNF().format(line.getPrice() * line.getKogus())) + "</ItemSum>" + ls);

                sb.append("         <Addition addCode=\"DSC\">" + ls);
                sb.append("              <AddRate>" + escape(getNF().format(line.getDiscount())) + "</AddRate>" + ls);
                  sb.append("          </Addition>" + ls);
                /*         <ItemPrice>30.6</ItemPrice> <!—sisestati käsitsi
                               <ItemBasePrice>34</ItemBasePrice> <!—baashind hinnakirja järgi
                               <ItemSum>459</ItemSum>
                               <Addition addCode="DSC"> <!--peab oleama „DSC“, muidu NAV ignoreerib seda. 
                                 <AddRate>10</AddRate> <!-- see tuleb arvutada 34*15 = 510 ja 30.6 *15 = 459, 459/510=0,9 ehk allahindluse % = 10
                                 <!-- rea põhine allahindluse protsent -->
                               </Addition>
*/
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
            
           
String com = null;

if(order.getInfo() != null && order.getInfo().trim().length() >0){
		com =  order.getInfo() ;
	}
           
			

              
              if(com != null  && com.trim().length() > 0){
            	  if(com.length() > 70){
            		  com = com.substring(0,70);
            	  }
              	      sb.append("   <AdditionalInfo>" +ls);
                      sb.append("       <Extension>" + ls);
                      sb.append("            <InfoContent>" + com + "</InfoContent>" + ls );
                      sb.append("       </Extension>");
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
       Converter.saveFileInUTF8(fileName, sb);

 	} catch (Exception e) {
 		e.printStackTrace();
 	}
  }

 
	public NumberFormat getNF() {
		return this.nf;
	}
	
	
	  
 
 
  public String getDriverVersion(){
    return "NaviOskarImport";
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
	      
	        ret = ret.replaceAll("ė","e");
	        ret = ret.replace((char) 0x3f,' ');
	        
	        ret = ret.replaceAll("Į","I");
	        ret = ret.replaceAll("ų","u");
	        ret = ret.replaceAll("č","c");
	        ret = ret.replaceAll("ž","z");
	        ret = ret.replaceAll("š","s");
	        ret = ret.replaceAll("Š","S");
	        ret = ret.replaceAll("č","c");
	        ret = ret.replaceAll("Į","I");
	        ret = ret.replaceAll("ū","u");
	        ret = ret.replaceAll("į","i");
	        ret = ret.replaceAll("č","c");
	        ret = ret.replaceAll("Ž","Z");
	        ret = ret.replaceAll("Ė","E");
	        ret = ret.replaceAll("Ų","U");
	        ret = ret.replaceAll("Ū","U");
	        ret = ret.replaceAll("§","Pg.");
	        
	        ret = ret.replaceAll("Ž","Z");
	        ret = ret.replaceAll("Ā","A");
	        ret = ret.replaceAll("ā","a");
	        ret = ret.replaceAll("Č","C");

	        ret = ret.replaceAll("č","c");
	        ret = ret.replaceAll("Ē","E");
	        ret = ret.replaceAll("ē","e");
	        ret = ret.replaceAll("Ģ","G");
	        ret = ret.replaceAll("ģ","g");
	        ret = ret.replaceAll("Ī","I");
	        ret = ret.replaceAll("ī","i");
	        ret = ret.replaceAll("Ķ","K");
	        ret = ret.replaceAll("ķ","k");
	        ret = ret.replaceAll("Ļ","L");
	        ret = ret.replaceAll("ļ","l");
	        ret = ret.replaceAll("Ņ","N");
	        ret = ret.replaceAll("ņ","n");
	        
	        ret = ret.replaceAll("õ","o");
	        ret = ret.replaceAll("ä","a");
	        ret = ret.replaceAll("ö","o");
	        ret = ret.replaceAll("ü","u");
	       
	        ret = ret.replaceAll("Õ","O");
	        ret = ret.replaceAll("Ä","A");
	        ret = ret.replaceAll("Ö","O");
	        ret = ret.replaceAll("Ü","U");
	    }
	        
	        if(ret == null || ret.trim().length()==0){
	        return " ";
	        }else {
	        	System.out.println(ret);
	        return ret;
	        }        
	      }

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private int counter = 0;
  private ImportPanel parent;
  private String trouble = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return trouble;
}
}
