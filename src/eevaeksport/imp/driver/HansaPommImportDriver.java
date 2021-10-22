
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
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

public class HansaPommImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(HansaPommImportDriver.class.getName());

  public static final String SEPARATOR = "\t";
  public static final String PRODUCT_FILE = "tooted.TXT";
  public static final String PRODUCT_PRICES_FILE = "hinnad.TXT";
  public static final String AGENT_FILE = "agent.TXT";
  public static final String CUSTOMER_FILE = "kliendid.TXT";
  public static final String CUSTOMER_AGENT_FILE = "klagent.txt";
  public static final String CUSTOMER_PRODUCT_FILE = "klienditoode.TXT";
  public static final String CUSTOMER_DEBT_FILE = "volg.txt";
  public static final String AGENT_SALES_FILE = "5_sales_agents.txt";
  
  public HansaPommImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    getProducts(null);

    try {

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


  	  private Hashtable dates = new Hashtable();
  	  
  	  public TreeMap getProducts(Agent agent){
  	     TreeMap tm = new TreeMap();
  	     StringBuffer buf = new StringBuffer();
  	     try {
  	     	
  	     //	this.getPriceMatrixData();
  	     	
  	    // 	TreeMap sto  = getStocks();
  	     //	TreeMap cost  = getCosts();
  	     	
  	     	
  	     	
  	       FileReader fr = new FileReader(getDir() + PRODUCT_FILE);
  	       BufferedReader reader = new BufferedReader(fr);
  	     String line = null;
  	     while((line = reader.readLine()) != null){
  	     	
  	     	if(line.indexOf("artikel5") != -1){
  	     		 while((line = reader.readLine()) != null && line.indexOf("ITVc2") == -1){
  	     		
  	     		 line = line.replaceAll("\t","; ");
  	     	//	System.out.println(line);
  	     		if(line.length()>100){
  	     		
  	       StringTokenizer st = new StringTokenizer(line, ";");
  	       try{
  	    	
  	       if (st.hasMoreTokens()){
  	    	  st.nextToken();
  	    	   st.nextToken();
  	    	   st.nextToken();
  	         String id = st.nextToken().trim();
  	         Product prod = new Product(id);
  	         String out = "";
  	         String name = "";
  	         if (st.hasMoreTokens()){
  	        	 name = st.nextToken().trim();
  	           prod.setName(name);
  	     
  	           if (st.hasMoreTokens()){
  	           	String unit = st.nextToken().trim();
  	           	if(unit.length()>0){
  	           		prod.setUnit(unit);
  	           	}
  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	            if (st.hasMoreTokens()){
  	            	String pr = st.nextToken().trim().replaceAll(",",".").replaceAll(" ","");
  	            	if(pr.length() == 0){
  	            		pr = "0";
  	            	}
  	                double price = Double.parseDouble(pr);
  	                if (price > 0){
  	                  prod.setPrice(price);}
  	               //   System.out.println("price" + prod.getPrice());

  	               	st.nextToken();
  	               	String gr = st.nextToken().trim();
  	               	if (gr.length()>0){
  	               		prod.setGroup(gr);
  	               	}
  	               	
  	               	
  	               	if(hinnad.get(prod.getID()) != null){
  	               	//	prod.setPrice(Double.parseDouble((String) hinnad.get(prod.getID())));
  	               	}
  	               	
  	           	String ii = "";

  	           	
  	           	
  	           	ii =	st.nextToken();
  	           	ii =st.nextToken();      
  	           	ii =st.nextToken();
  	          // 	System.out.println("ii" + ii);
  	           	String cos = ii.trim().replaceAll(",",".").replaceAll(" ","");
  	           	if(cos.length() == 0){
  	        		cos = "0";
  	        	}
  	           	 double cost = Double.parseDouble(cos);
  	           	 
  	           	 double marg = (1-cost/prod.getPrice())*100 ;
  	           	 if(marg >0){
  	           	 	 prod.setMargin((int) marg + "");
  	                //	System.out.println("price" + prod.getPrice() + " " + (int)marg + " " + cos );
  	                	 
  	           	 }
  	          
  	           	String sortcode =  st.nextToken();
  	           	if(sortcode != null && sortcode.trim().length() > 0 ){
  	           		prod.setSorting(sortcode);
  	           	}
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken(); 
  	           	ii =	st.nextToken();
  	           	String ean2 =	st.nextToken();
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken();

  	           	String ean = st.nextToken().trim();
  	           	if(ean.trim().length()>0){
  	           		prod.setEAN(ean);
  	           	}
  	         	if(ean2.trim().length()>0){
  	           		prod.setEAN(ean2.trim());
  	           	}

  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	    out=   	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	            st.nextToken();
  	          	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	           	st.nextToken();
  	          
  	             if (st.hasMoreTokens()){
  	             	String ca = st.nextToken().trim().replaceAll(",",".").replaceAll(" ","");
  	             	if(ca.trim().length()==0){
  	             		ca = "0";
  	             	}
  	                 double inCases = Double.parseDouble(ca);
  	                 if (inCases > 0){
  	                   prod.setInCases(inCases);
  	               //System.out.println("CAse" + prod.getInCases());
  	             }
  	              }
                  }
  	           }
  	         }
  	    
  	         if(prod.getName().length()>0){
  	         
  	                 tm.put(prod.getName(), prod);
  	        	     prods.put(prod.getID(), prod);
  	         }
  	            
  	       }
  	     
  	       }catch (Exception k){
  	    	  // k.printStackTrace();
  	       }
  	     		 }	
  	      		}
  	     	}
  	     }
  	     } catch (Exception e){
  	       //e.printStackTrace();
  	     }
  	 
  	     
  	     return tm;
  	   }
  	   
  	   
  private String getDir(){
    String dir = parent.getProps().getProperty("readin");
    if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
    return dir;
  }
   
  private HashMap payers = new HashMap();
 

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	
  	 System.out.println("Kliendi Määramine: " );
  	 if(regnr.equals("henkel")){
  		 return "henkel";
  	 }
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

 
  public String identifyCustomerAGENT(String id) throws SQLException {
 	 String	sql1 = "SELECT SL01035 as agent FROM SL010200 left JOIN SY240200  ON SL01036=SY24002 " +
 	 		"and SY24001 ='BG' WHERE SL01001='" + id +"'"; 
 	
   System.out.println(sql1);
   String agent = null;

   Statement st1 = con.createStatement();
   ResultSet rs1 = st1.executeQuery(sql1);

   while (rs1.next()) {
   	agent = rs1.getString("agent").trim();

   }
   rs1.close();
   st1.close();

   return agent;

 }
 
  
  
  public String getMTKood(int paevad) throws SQLException{
 	 
    String sql1 = " select SL22002 as kood from SL220200 where SL22003 =0 and SL22005 =  " + paevad + "";
    String kood = null;
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      kood = rs1.getString("kood").trim();
    }
       System.out.println(paevad + " mtkood " + kood);
    rs1.close();
    st1.close();
    return kood;
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


  
  public void makeOrder(Order order) throws SQLException{

	  counter = 0;
	   trouble = "Imoprdin tellimust " + order.getAgent() + "." + order.getDokNr() + ".txt aga SAP faili ei tekkinud ";
		
	 Order ord =  order;	
	 try{
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
	 }catch(Exception r ){
		 log.error(r);
	 }
		
   		           ord.setTeostaja(order.getTeostaja());
     			   makeOutput(ord);
	    	   
      trouble = null;
      setNumAddedLines(counter);
  }

  
  public String identfySorting (String prodID){
	  	


	    String sql1 = " Select SC01015 + SC01001 AS sort FROM SC010200 pr  where pr.SC01001='" + prodID + "'";
	  log.error("1_LINE_SQL: " + sql1);

	    String sort = "000";

	    Statement st1;
		try {
			st1 = con.createStatement();
		
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	sort = rs1.getString("sort");
	    //	System.out.println("sort käes 1 ;" + sort );
	    	if(sort == null || sort.trim().length() == 0){
	    		sort = "xxx";
	    	}
	    }

	    rs1.close();
	    st1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
// System.out.println("Sort käes 2 ;" + sort );
	    return sort;
	  }
  
  private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    sb.append((cal.get(Calendar.YEAR)+"").substring(2));
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

  public static String getDateString(java.util.Date date){
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);

    String month = (cal.get(Calendar.MONTH) + 1) + "";
    if (month.length() == 1) month = "0" + month;
    String day = cal.get(Calendar.DAY_OF_MONTH) + "";
    if (day.length() == 1) day = "0" + day;
    String dateString = day + "." + month + "." + cal.get(Calendar.YEAR);
    return dateString;
  }

  private String testLength2(int i){
    String s = String.valueOf(i);
    if (s.length() == 1) return "0" + s;
    if (s.length() == 4) return s.substring(3, 4);
    return s;
  }

  
  private String lineSep = "\n";
  
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
	            

	            if(ref != null && ref.trim().length() > 0){
	          	  sb.append("<RefInfo>" + ls);
	                sb.append("<SourceDocument type=\"order\">" + ls);
	                if(order.getStatus() > 10 && ref.trim().indexOf(" ") != -1){
	                	ref = ref.trim().substring(0,ref.trim().indexOf(" ")).trim();
	                }
	                sb.append("    <SourceDocumentNum>" + escape(ref).trim() + "</SourceDocumentNum>" + ls );
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
	        if(ref != null && ref.trim().length() > 0){
	        	sss = ref;
	        }
	        if(((OrderLine)(OrderLine)order.getOrderLines().elementAt(0)).getKogus() < 0){
	        	   sb.append(" <DocumentSubType>return</DocumentSubType>" ); 	
	        }
	        
	            sb.append("<DocumentName>" + subType + "</DocumentName>" + ls); 
	            sb.append("<DocumentNum>" + escape(sss).trim()+ "</DocumentNum>" + ls); 
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
	              	  double discprice = (100d -line.getDiscount())*line.getPrice()/100d ;
	                sb.append("         <ItemPrice>" + escape(getNF().format(discprice)) + "</ItemPrice>" + ls); 
	              
	                 
	                }
	                
	                if(order.getStatus() == 59 && line.getPrice() > 0 ){//telema variant readprice seadega 
	                	   sb.append("         <ItemPrice>" + escape(getNF().format(line.getPrice())) + "</ItemPrice>" + ls); 
	                       
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
	                    sb.append("       <Extension extensionId=\"internalremarks\">" + ls);
	                    sb.append("            <InfoContent>" + escape(comm) + "</InfoContent>" + ls );
	                    sb.append("       </Extension>");
	                      //-<Extension extensionId="externalremarks"><InfoContent>Käesoleva arve aluseks olev nõue on loovutatud AS-ile SEB Liising ja kõik arvel toodud summad tuleb maksta AS-i SEB Liising arvelduskontole 10052045861006 SEB Pangas (kood 401). Kogu käesoleva arvega seotud info tuleb teatavaks teha AS-ile SEB Liisingule.</InfoContent></Extension>
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

	 
	
		
		
		  

  
  
 private void makeOutputOld(Order order){

     StringBuffer sb = new StringBuffer();
     Calendar cal = Calendar.getInstance();
     Calendar cal2 = Calendar.getInstance();
    
try{
	 if (order.getPaymTerm() > 0) {
	        cal.add(Calendar.DAY_OF_MONTH,order.getPaymTerm());
	      }else {
	        cal.add(Calendar.DAY_OF_MONTH, 1);
	       }
}catch(Exception e){

}


    // sb.append("compnr	1\t" + lineSep);
     sb.append("format\t" + lineSep);
     sb.append("1\t44\t1\t0\t0\t" + lineSep + lineSep  );//viimane 1 on ANSI kui 0 siis UTF
     sb.append("codepage\tUTF-8" + lineSep  + lineSep);
     
     sb.append("commentstring\t" + lineSep);
     sb.append("\t" + lineSep );
     sb.append( "\n" );
     sb.append("ORVc" + lineSep);

     String kogusumma = "0";
     String kmsumma = "0";
     String km = "=";
     String laokood = "L";
     String objekt = "";
     String myygikonto = "1310";
     String valuuta = "";
     String hinnakiri = "H3";
     String muudaladu = "1";

     String addr = "";
    String indeks = "";
    String linn = "";
    String tanav = "";

    if(addr.indexOf("|") != -1){
   	 tanav = addr.substring(0,addr.indexOf("|"));
   	 linn = addr.substring(addr.indexOf("|")+1);
    }

    if(linn.indexOf("|")!= -1){
   	 indeks =linn.substring(linn.indexOf("|")+1);
   	 linn = linn.substring(0,linn.indexOf("|"));
    }



    String juraddr = order.getInfo();
    String jurindeks = "";
    String jurlinn = "";
    String jurtanav = "";
 

  
  	  laokood = "";
  	  objekt = "";
  	  hinnakiri = "AS";
  	  myygikonto = "10311";
  	  valuuta = "EUR";
  	  muudaladu = "0";

  	  String klnimi = "";
  	  String address = "";
  
//340773	06.02.2013	1836	KAUBABUSS OÜ	Ridaelamu 5-6, Maaritsa, Põlvamaa	Tele. 56 231248				
//0	2	0	0	S		18	0			0		MAR	MAR						EUR			0		0			
//0						0	0,00	30,04		6,01	36,05	0	1			15				0	36,05
//1	15,6466			4,64				15,5					54,000	0,000	2,352		36,05
  	  //1	0	0						0	0		0	0	

  	  sb.append(order.getDokNr() + "\t" + getDateString(cal2) + "\t" + order.getCompany() + 
  			  "\t" + klnimi +"\t" + address + "\t				0	2	0	0	S		18	0			0		" + order.getAgent()+ "\t"+ order.getAgent()+ "\t					EUR			0		0			0						0	0,00	30,04		6,01	36,05	0	1			15				0	36,05			1	15,6466			4,64				15,5					54,000	0,000	2,352		36,05													1	0	0						0	0		0	0	");
  	  
   /*  sb.append(order.getDokNr() + "\t" + getDateString(cal2) + "\t" + order.getCompany() +  "\t" + getDateString(cal)+ "\t"

                     + getDOM().getTellijaNimi() + "\t" + jurtanav + "\t" + jurlinn + "\t" + jurindeks + "\t\t\t0\t" + getDOM().getMaksetahtaeg() +  "\t\t0\t0\t" //viimane null on kinnitus
                     + getDOM().getMaksetahtaeg() +"\t0,000\t0\t" + hinnakiri + "\t\t\t1\t0\t\t" + objekt + "\t0\t" + myygikonto + "\t\t\t\t" +  getDOM().getAgendinimi() + "\t\t"
                       + getDateString(cal2) + "\t" + valuuta + "\t\t" + muudaladu +"\t\t\tAIVARM\t\t\t\t\t\t\t0,00\t\t0,00\t"
                       + kogusumma + "\t\t" + km + "\t" + kmsumma + "\t\t\t" + getDOM().getTellijaNimi() + "\t\t\t\t\t" + laokood + "\t\t\t\t1\t\t"
                       + "16 960 000 010 140\t\t\t0,00\t0\t\t0\t" + kmsumma  + "\t\t\t" + rate1+ "\t" + rate2 + "\t\t\t\t"
                       + "0,00\t0,00\t0\t\t\t\t\t0\t\t0\t0\t\t0\t\t\t5,000\t0,074\t0,000\t\t" + kmsumma
                       + "\t\t" + addr + "\t\t\t\t0\t\t0\t0\t\t0\t\t\t\t01:01:01\t\t\t0\t\t0\t0\t\t\t"  +  getDateString(cal2)
                       + "\t\t\t\t\t\t0\t0\t\t\t"   +  lineSep);



    */
    
     
     Iterator iter = order.getOrderLines().iterator();
     while(iter.hasNext()){
       OrderLine line = (OrderLine)iter.next();

       double kate = (line.getPrice()*line.getKogus())*(100-line.getDiscount())/100-20*line.getKogus();
System.out.println("Kate " + kate + " " + line.getPrice()*line.getKogus() + " " + line.getDiscount()  + " " + this.getNumAddedLines() );
String kmkood = "3";

String nimetus = "";
if(prods.get(line.getToode()) != null){
	nimetus = ((Product)prods.get(line.getToode())).getName();
}

       sb.append("1\t" + line.getToode()  + "\t"+ ((line.getKogus()+"").replace('.',','))+
                       "\t" + ((line.getPrice()+"").replace('.',',')) + "\t" +
                       (((line.getPrice()*line.getKogus())+"").replace('.',',')) + "\t0" //+ (line.getDiscount()+"").replace(',','.')
                               + "\t40121" + "\t"  + "\t\t"
                        + ((line.getKogus()+"").replace('.',','))
                                + "\t" +"\t" + ("") // ((kate+"").replace('.',','))
                                + "\t " + nimetus + "\t" + kmkood + "\t\t\t\t\t\t" + ((20*line.getKogus())+"").replace('.',',')
                                + "\t\t\t\t\t" + line.getUnit() + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t1,65\t\t\t\t\t\t\t0\t"
                +  lineSep);
      counter++;
     }
     //(line.getDiscount()+"").replace('.',',')  +   "\t\t"
      sb.append(lineSep);


     Calendar cal1 = Calendar.getInstance();
     String ss = (cal1.get(Calendar.SECOND)+"");
     if(ss.length()>1){
             ss = ss.substring(0,1);
     }
     String fileName = (parent.getProps().getProperty("conv.dir") + order.getAgent())  + "." + order.getDokNr()  +  ".txt";

    try{

        OutputStream os = new FileOutputStream(fileName);
                OutputStreamWriter osw = new OutputStreamWriter(os);
              
                osw.write(sb.toString());
                osw.close();
                os.close();


    }catch(Exception e){
    	e.printStackTrace();
    }
    
 }
 

 
	public NumberFormat getNF() {
		return this.nf;
	}
	
	private HashMap	hinnad = new HashMap();
	
	  
 
 
  public String getDriverVersion(){
    return "HansaPommImport";
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
	        
	        
	       // ret = ret.replaceAll("?","s");
	       // ret = ret.replaceAll("?","c");
	     // ret = ret.replace('',' ');
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
