
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Hashtable;
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

public class HansaStockImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(HansaStockImportDriver.class.getName());

  public static final String SEPARATOR = "\t";

  
  public HansaStockImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    prepareDateInfo();
    getProducts(null);
    try {
     
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
  
  
  private void  prepareDateInfo(){
		
	  String[] list = new File(getDir()).list();

	  for(int i = 0 ; i < list.length ; i++){
		  String name = list[i];
	// System.out.println(list[i]);
		  
		  if(name.indexOf("Kontaktid")!= -1){
			  String date = name.substring(name.indexOf("Kontaktid")+11,name.indexOf("Kontaktid")+19);
			  String ddd = (String) dates.get("Kontaktid");
			//  System.out.println(date + " " + ddd + " " + (date));
			  
			
			  if(ddd == null){
				  dates.put("Kontaktid", date); 
			  }else{
				//  System.out.println(date + " " + ddd + " " + (date) + " " + ddd.compareTo(date));
				  if(ddd.compareTo(date) <= -1){
					  dates.put("Kontaktid", date); 
				  }
			  }
		  }
		  
		  if(name.indexOf("Laoseis")!= -1){
			  String date = name.substring(name.indexOf("Laoseis")+9,name.indexOf("Laoseis")+17);
			//  System.out.println(date);
			  
			  String ddd = (String) dates.get("Laoseis");
			  if(ddd == null){
				  dates.put("Laoseis", date); 
			  }else{
				  if(ddd.compareTo(date) <= -1){
					  dates.put("Laoseis", date); 
				  }
			  }
		  }
		 
		  if(name.indexOf("Artiklid")!= -1){
			  String date = name.substring(name.indexOf("Artiklid")+10,name.indexOf("Artiklid")+18);
			//  System.out.println(date);
			  
			  String ddd = (String) dates.get("Artiklid");
			  if(ddd == null){
				  dates.put("Artiklid", date); 
			  }else{
				  if(ddd.compareTo(date) <= -1){
					  dates.put("Artiklid", date); 
				  }
			  }
		  }
		  
		  if(name.indexOf("Arved")!= -1){
			  String date = name.substring(name.indexOf("Arved")+7,name.indexOf("Arved")+15);
			//  System.out.println(date);
			  
			  String ddd = (String) dates.get("Arved");
			  if(ddd == null){
				  dates.put("Arved", date); 
			    } else {
				  if(ddd.compareTo(date) <= -1){
					  dates.put("Arved", date); 
				  }
			  }
		  }
		  
		  
		  if(name.indexOf("Hinnaalandusmaatriksid")!= -1){
			  String date = name.substring(name.indexOf("Hinnaalandusmaatriksid")+24,name.indexOf("Hinnaalandusmaatriksid")+32);
			  //System.out.println(date);
			  
			  String ddd = (String) dates.get("Hinnaalandusmaatriksid");
			  if(ddd == null){
				  dates.put("Hinnaalandusmaatriksid", date); 
			  }else{
				  if(ddd.compareTo(date) <= -1){
					  dates.put("Hinnaalandusmaatriksid", date); 
				  }
			  }
		  } 
	  }
  }
  private TreeMap prods = new TreeMap();
  private TreeMap eans = new TreeMap();
  
  
  private String getFileName(String asi) {
	   String	 filename  = null;
		 String[] list = new File(getDir()).list();
	 	for(int u = 0 ; u < list.length ; u++){
			  String fname = list[u];
		  if(fname.indexOf(asi) != -1 && fname.indexOf((String)dates.get(asi)) != -1){
			   filename =  fname;
		   }
		}
			  return filename;
  }
       

  	  private Hashtable dates = new Hashtable();
  	  
  	 
  	  public TreeMap getProducts(Agent agent){
  	     TreeMap tm = new TreeMap();
  	     try {
  	    
  	    	  BufferedReader reader = new BufferedReader(
  	     			   new InputStreamReader(
  	     	                      new FileInputStream(getDir() + getFileName("Artiklid") ), "UTF8"));
  	  
  	     String line = null;
  	     while((line = reader.readLine()) != null){
  	    	 boolean goes = false;    	
  	    
  	     		
  	     		 line = line.replaceAll("\t","; ");
  	     		//System.out.println(line);
  	     		if(line.length()>100){
  	    try{
  	    	 
  	       StringTokenizer st = new StringTokenizer(line, ";");
  	       if (st.hasMoreTokens()){
  	    	   st.nextToken();
  	    	   st.nextToken();
  	    	String sta =    st.nextToken().trim();
  	    	  if (sta.equals("1")) {
  	    		  goes = true;
  	    	   }
  	         String id = st.nextToken().trim();
  	         //System.out.println("id "  + id );
  	         Product prod = new Product(id);
  	         String out = "";
  	         String name = "";
  	         if (st.hasMoreTokens()){
  	        	 name = st.nextToken().trim();
  	          
  	        	// System.out.println("name "  + name );
  	     
  	           if (st.hasMoreTokens()){
  	           	String unit = st.nextToken().trim();
  	           	if(unit.length()>0){
  	           		prod.setUnit(unit);
  	           	}
  	          String kastis =  	st.nextToken();
  	         
  	          if(kastis != null && kastis.trim().length()>0){
  	        	  prod.setInCases(Double.parseDouble(kastis.replace(',', '.')));
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
  	                 // System.out.println("price" + prod.getPrice());

  	               	st.nextToken();
  	               	String gr = st.nextToken().trim();
  	               	if(gr.length() > 0){
  	               		prod.setGroup(gr);
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
  	           	 //|D198C21D-7057D9D0-46C25505-5B421912-DA9B79A3||1|1530|Kommikarp "RIVIERA" 125gr*12tk|KARP|12
  	            // |40121|1,35|1|MIL|||0,88442|SD||0||0,00076|0,08334|.....30.07.17|||KARP|0||4820075502492|2|
  	             String sortcode =  st.nextToken();
  	           	if(sortcode != null && sortcode.trim().length() > 0 ){
  	           		prod.setSorting(sortcode.trim());
  	           	}
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken(); 
  	           	ii =	st.nextToken();
  	           	String ean2 =	st.nextToken();
  	             	//System.out.println("ean2 " + ean2);
  	           	ii =	st.nextToken();
  	           	ii =	st.nextToken();
  	           	String grupp =	st.nextToken();
  	           
  	           	ii =	st.nextToken();
  	           
  	             	st.nextToken();
  	           	
  	          
  	 
  	           		
  	             	
  	           		name = (name).replaceAll("Pakitud", "Pk") + " " + id + " " ;
  	           		
  	           	    prod.setName(name);
  	          
  	           	   
  	           		//System.out.println("name " + prod.getName() + " " + prod.getGroup());
  	           	
  	           	
  	           	String partii = st.nextToken().trim();
  	     		//System.out.println("name " + prod.getName() + " " + prod.getGroup());
  	             
  	         	
  	           	if(ean2.trim().length()>0){
  	           		prod.setPartii(ean2.trim());
  	           	}

  	         	st.nextToken();
  	    

  	       	if(partii.trim().length()>0){
  	       		partii = partii.trim().replaceAll(";", ",");
  	       		prod.setEAN(partii.trim());
  	       	}
  	       	
  	       out =   st.nextToken();
  	     
  	           	
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
  	                  }
  	              }
  	             
  	             int o = 0; 
  	          while  (st.hasMoreTokens()) {
  	        	  o++;
  	        	 String tok = st.nextToken();
  	        	 	
  	        	 if("AA".equals(tok.trim())) {
  	        		 prod.setStatusTop("1");
  	        		 //System.out.println("aaa " + tok + " " + o);
  	        	 }
  	        	  
  	          }
  	  
  	             }
  	           }
  	         }
  	         
  	         prod.setStock(0);
  	       
  	      //   System.out.println("Out " + prod.getStock()+ " "+ prod.getName()+ " " + out);
  	        String ll = reader.readLine();
  	        ll = reader.readLine();
  	        if(ll != null && ll.trim().length() > 0 && ll.indexOf("//")!= -1){
  	        	String aaa = ll.substring(ll.indexOf("//")+2).trim() + " ";
  	        	if(prod.getPartii().trim().length() > 0){
  	        	 	prod.setPartii(prod.getPartii() + " " + aaa.replaceAll(";", ",").trim() + " ");
  	        	        		
  	        	}else{
  	        		prod.setPartii( aaa.replaceAll(";", ","));
  	        	    
  	        	}
  	        }
  	        
  	        ll = reader.readLine();
  	        if(ll != null && ll.trim().length() > 10){
  	        	String aaa = ll.trim();
  	        	if(prod.getPartii().trim().length() > 0){
  	        	 	prod.setPartii(prod.getPartii() + " " + aaa.replaceAll(";", ",").trim() + " ");
  	        	        		
  	        	}else{
  	        		prod.setPartii( aaa.replaceAll(";", ","));
  	        	    
  	        	}
  	        }
  	         
  	         	if( goes ){
  	         		
  	                 tm.put(name, prod);   
  	               
  	                 if(prod.getEAN() != null){
  	                   eans.put(prod.getEAN(), id);
  	                   }
  	               
  	                  prods.put(id,prod);
  	         	   }
  	              }
  	     		}catch(Exception r){
  	      		  r.printStackTrace();	
  	      		}
  	     	}
  	     }
  	     reader.close();
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
 	//	System.out.println("ladu tellimusel " + ware);
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
                   sb.append("          <ItemDescription>" + Converter.cleanLitLatEst(escape(nimi)) + "</ItemDescription>" + ls);               
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
       fn = fn.replace('/', '-').replace('\\', '-');
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
    return "HansaStockImport";
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
  private Connection con;
  private int counter = 0;
  private ImportPanel parent;
  private String trouble = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return trouble;
}
}
