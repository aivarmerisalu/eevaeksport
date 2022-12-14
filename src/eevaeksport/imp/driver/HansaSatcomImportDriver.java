
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
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

public class HansaSatcomImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(HansaSatcomImportDriver.class.getName());

  public static final String SEPARATOR = "\t";
  public static final String PRODUCT_FILE = "tooted.TXT";
  public static final String PRODUCT_PRICES_FILE = "hinnad.TXT";
  public static final String AGENT_FILE = "agent.TXT";
  public static final String CUSTOMER_FILE = "kliendid.TXT";
  public static final String CUSTOMER_AGENT_FILE = "klagent.txt";
  public static final String CUSTOMER_PRODUCT_FILE = "klienditoode.TXT";
  public static final String CUSTOMER_DEBT_FILE = "volg.txt";
  public static final String AGENT_SALES_FILE = "5_sales_agents.txt";
  private TreeMap custs = null;
  
  public HansaSatcomImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    getProducts(null);
   custs =  getCustomers();
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


  	  private Hashtable dates = new Hashtable();
  	  
  	 
  	   public TreeMap getProducts(Agent agent){
  	     TreeMap tm = new TreeMap();
  	 
  	     StringBuffer buf = new StringBuffer();
  	     try {
  	     	
  	       FileReader fr = new FileReader(getDir() + PRODUCT_FILE);
  	       BufferedReader reader = new BufferedReader(fr);
  	     String line = null;
  	     while((line = reader.readLine()) != null){
  	    	 boolean goes = true;    	
  	     	if(line.indexOf("artikel5") != -1){
  	     		 while((line = reader.readLine()) != null && line.indexOf("ITVc2") == -1){
  	     			 line = line.replaceAll(";"," ");
  	     		 line = line.replaceAll("\t","; ");
  	     		if(line.length()>100){
  	     		//	System.out.println(line);
  	     	     		
  	       StringTokenizer st = new StringTokenizer(line, ";");
  	       if (st.hasMoreTokens()){
  	    	   st.nextToken().trim();
  	    	   st.nextToken().trim();
  	    	   st.nextToken().trim();
  	         String id = st.nextToken().trim();
  	         Product prod = new Product(id);
  	       //  System.out.println("id " + id );
  	         String name = "";
  	         if (st.hasMoreTokens()){
  	        	 name = st.nextToken().trim();
  	     
  	           if (st.hasMoreTokens()){
  	           	String unit = st.nextToken().trim();
  	           	if(unit.length()>0){
  	           		prod.setUnit(unit);
  	           	}
  	          String kastis =  	st.nextToken();
  	          if(kastis != null && kastis.trim().length()>0){
  	        	//  prod.setInCases(Double.parseDouble(kastis.replaceAll(" ","").replace(',', '.')));
  	          }
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
  	               	String grupp = st.nextToken().trim();
  	               	if (grupp.length()>0){
  	               		prod.setGroup(grupp);
  	               	}
  	               	
  	               	
  	           	String ii = "";
 	
  	           	ii = st.nextToken();
  	           	ii = st.nextToken();      
  	           	ii = st.nextToken();
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
  	           		//prod.setSorting(sortcode.trim());
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
  	           
  	           	ii = st.nextToken();
  	             	st.nextToken();
  	       
  	             	
  	           	if(grupp.trim().length()>0){
  	           		prod.setGroup(grupp.trim());
  	           		
  	           	
  	              
  	           	}
  	          prod.setNameSorting(name);
           	    prod.setName(name);
  	           	
  	           	String partii = st.nextToken().trim();
  	           
  	         	
  	           	if(ean2.trim().length()>0){
  	           		prod.setEAN(ean2.trim());
  	           		System.out.println("ssssr " + prod.getEAN());
  	           	}

  	         	st.nextToken();
  	    

  	       	if(partii.trim().length()>0){
  	       		prod.setEAN(partii.trim());
  	       	  if(partii.trim().length() > 4){
  	      		//prod.setName(prod.getName() + " " + partii.trim().substring(partii.trim().length()-4)); 
  	      	  }
  	       		
  	       	}
  	       	
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
  	             

  	           //	System.out.println("CAse" + st.nextToken());
  	             }
  	           }
  	         }
  	         
  	       
  	       
  	        
  	         
  	         	
  	                 tm.put(prod.getGroup() +" "+  id +" "+ name, prod); 
  	                 if(prod.getEAN() != null){
  	                   eans.put(prod.getEAN(), id);
  	                	   
  	                   }
  	                 prods.put(id,prod);
  	            
  	                 
  	         	}
  	            
  	       }
  	     		 }	
  	      		}
  	     	}
  	     
  	     } catch (IOException e){
  	       e.printStackTrace();
  	     }

  	     
  	     return tm;
  	   }
  private String getDir(){
    String dir = parent.getProps().getProperty("readin");
    if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
    return dir;
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
		
	 Order ord =  order;	
	 try{
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
	 }
		
   		           ord.setTeostaja(order.getTeostaja());
     			   makeOutput(ord);
	    	   
      trouble = null;
      setNumAddedLines(counter);
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
                 } else if(order.getStatus() == 59 && line.getPrice() > 0 ){//telema variant readprice seadega 
                	   sb.append("         <ItemPrice>" + escape(getNF().format(line.getPrice())) + "</ItemPrice>" + ls); 
                       
                  } else {
              	 // sb.append("         <ItemPrice>" + escape(getNF().format(line.getPrice())) + "</ItemPrice>" + ls); 
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
       fn = fn.replace('/', '-').replace('\\', '-');
       String fileName = escape(parent.getProps().getProperty("conv.dir") + agent)  + "." + fn  +  ".xml";
       System.out.println(fileName);
       Converter.saveFileInUTF8(fileName, sb);
       
       
       if( order.getStatus() < 5){
    	   String info  = "Tellimusel Ridu : " + order.getOrderLines().size() + "\nTarnekuupäev : " + Converter.getDDpMMpYYYYpString(cal);
    	   
		   Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(order.getAgent());
	    Converter.sendCommentEmail( comm,info
	    		,order.getDokNr(),order.getCompany(),  " " + getCustName(order.getCompany()) , " ",false,ag,parent);
	 }

 	} catch (Exception e) {
 		e.printStackTrace();
 	}
  }

 
	public NumberFormat getNF() {
		return this.nf;
	}
	
	
	  private String getCustName(String id) {
		  String res = null;
		  if(custs.get(id) != null) {
			  res = ((Customer)custs.get(id)).getName();
		  }
		  return res ;
	  }

	   public TreeMap getCustomers(){
	  

	     TreeMap tm = new TreeMap();
	     StringBuffer buf = new StringBuffer ();
	 
	     try {
	     	
	       InputStream ins = new FileInputStream(getDir() + CUSTOMER_FILE);
	       InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
	       BufferedReader reader = new BufferedReader(isr);
	  
	     String line = null;
	     while((line = reader.readLine()) != null){
	     	
	     	
	     	if(line.indexOf("cu5") != -1){
	     		//reader.readLine();
	  while((line = reader.readLine()) != null){
	  	line = line.replaceAll("\t","| ");
	  	line = line.replaceAll(";", ":");
	 	  StringTokenizer st = new StringTokenizer(line, "|");
	 try{
		
	 	  if (st.hasMoreTokens()){
	    	   st.nextToken().trim();
	    	   st.nextToken().trim();
	    	   st.nextToken().trim();
	    	   
	    	   
	         String id = st.nextToken().trim();
	         Customer customer = new Customer(id); 

	         customer.setAddress(" ");
	         if (st.hasMoreTokens()){
	           String name = st.nextToken().trim();
	           if (name.equals("")) name = "<" + id + ">";      
	            name = name + " " +  id ;
	          String cinfo =  st.nextToken() + " " + st.nextToken() ;
	         
	          if(cinfo.trim().length()>0){
	        	  customer.setPersonalData((Converter.enCodeEst(cinfo)));
	          }
	          // System.out.println(cinfo);
	           if (st.hasMoreTokens()){
	             String aadress = st.nextToken().trim();
	             String lisa = st.nextToken().trim();
	            
	               if( aadress != null){
	            	   if( lisa != null){
	            		   aadress = lisa + " " + aadress;
	                   }          
	               customer.setAddress(Converter.enCodeEst(Converter.adressLink(aadress, "Estonia")));
	             }
	           }
	           if (st.hasMoreTokens()){
	          }

	           
	           name = (name);
	             customer.setName(Converter.enCodeEst(name));  

	             
	             st.nextToken();
	             
	             
	             
	           st.nextToken();
	          
	        String  aadress =  st.nextToken().trim();
	           if( aadress != null && aadress.trim().length() > 0){
	             customer.setInfo(aadress);
	           }
	      
	      if (st.hasMoreTokens()){
	       String lisa = st.nextToken().trim();
	       if( lisa != null){
	        }
	     
	     }	           
	      st.nextToken();
	      //     st.nextToken();
	   //10202	A-Selver AS			P�rnu mnt 238	Tallinn 11624	Kadaka Selver		Kadaka tee 56A	Tallinn 12915		6673400	6673414	SELVE		45	0	0		1			KADSELVER	1			0	GAJAN				1	Kadaka Selver				075		0	09.02.2007					0									0				0	0			0	0	0	19.07.2004	0											0	0	0				0	0																					0	0	1									102068		0	0				0		0			
	        
	           if (st.hasMoreTokens()){
	           	String info = st.nextToken().trim();
	
	            }
	           st.nextToken();
	           st.nextToken();
	           st.nextToken();
	          
	          
	           if (st.hasMoreTokens()){
	           	String paym = st.nextToken().trim();
				if(paym.length()>0 && paym.trim().length()>0){
					
				//	if(ting.get(paym.trim()) != null ){
					//	customer.setPaymTerm((String)ting.get(paym.trim()));	
				//	}else{
						customer.setPaymTerm(paym.trim());
				//	}
					
			           	
				}
	             }

	           st.nextToken();
	           st.nextToken();
	           st.nextToken();
	           st.nextToken();
	           st.nextToken();
	           st.nextToken();
	        String hinnak =    st.nextToken();
	        if(hinnak != null && hinnak.trim().length() > 0){
	        	customer.setGroup(hinnak.trim());	
	        }
	 
	           st.nextToken();
	           st.nextToken();
	           st.nextToken();
	           st.nextToken();

	           if (st.hasMoreTokens()){
	        	   String agg = st.nextToken().trim();
	        	 //  System.out.println("agg" + agg);
	        	   if(agg.length()>0){
	                   customer.addAgent(new Agent(agg));
	                   customer.setCity(agg);
	                   customer.addAgent(new Agent("HOUSE"));
	                   customer.setMainAgent(agg);
	                   if(agg.equals("KRISTJAN")){
	                	   customer.addAgent(new Agent("HARRY_O"));
	                	   
	                   }
	                
	                }
	        	   
	        	   
	          }

	           
	           if (st.hasMoreTokens()){
	           	String cred = st.nextToken().trim();
	           	if(cred.length()>0 && cred.trim().length()>0){
	           	//	customer.setCredit(cred.replaceAll(" ", "").replaceAll(",", "."));	
	           	}          
	           }
	           
	           /*

	           if (st.hasMoreTokens()){
	            customer.setVisit(st.nextToken().trim());
	            System.out.println(customer.getVisit());
	          }*/
	           
	           String tok = "";
	           String tok2 = "";
	           String pricelist = "";
	           String delivery = "";
	           for(int i = 0 ;i<86;i++){
	           String	toke = st.nextToken();
	          
	           if(i==9){
	             tok = toke.trim();	
	           	}
	           	if(i==7){
	                tok2 = toke.trim();	
	                	}
	        	if(i==6){
	                pricelist = toke.trim();	
	                	}
	        	if(i==41){
	                delivery = toke.trim();	
	                	}
	           }
	        
	           if(delivery.trim().length()>0){
	        	 //  customer.setAddress(delivery.trim());
	           }
	              
	        	   
	           if(pricelist.trim().length()>0){
	              	customer.setGroup(pricelist);
	              }
	           
	           if(tok2.trim().length()>0){
	           	if(tok2.trim().length()>2){
	           		tok2 = tok2.substring(0,2)+ "." + tok2.substring(2,tok2.length());
	          	}
	           }
	        
	           //	System.out.println("Ale " + customer.getName() + " " +customer.getDiscount() + " " + tok);
	          
	         
	          
	          
	         }

	         tm.put(id, customer);
	         
	       }
	 	 
	 	 }catch(Exception r){
	 		// r.printStackTrace();
	 	 }
	     	
	    	}
	       }
	     }
	     reader.close();
	     

	     } catch (IOException e){
	       e.printStackTrace();
	     } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     
	 
	     
	 
	     
	     return tm;
	   }
 
 
  public String getDriverVersion(){
    return "HansaSatcomImport";
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
