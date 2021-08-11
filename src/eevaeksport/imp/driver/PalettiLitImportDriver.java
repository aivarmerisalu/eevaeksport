
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
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

public class PalettiLitImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(PalettiLitImportDriver.class.getName());

  
  public PalettiLitImportDriver(ImportPanel parent) {
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
	   
	   private HashMap<String,Double> stocks = null;
	    private String             url = "jdbc:odbc:PalettiSync32";
	    
	   public void getSaveStockInfo(){
	        
		 String  cmd = " select VERTYBESKODAS as toode,sum(kiekis)-isnull((select sum(kiekisapskvnt) from linum where " +
                   "    linum.periodono='XX' and linum.vertybeskodas=likoi.vertybeskodas and linum.objektokodas=likoi.objektokodas ),0) "+
              " as stock  from likoi where  objektokodas='0110' and saugojimovieta is not null group by VERTYBESKODAS,objektokodas "+
                     "  having stock>0 order by VERTYBESKODAS ";
		  
	  	  cmd = "select toode,stock from (select VERTYBESKODAS as toode,sum(kiekis)-isnull((select sum(kiekisapskvnt) from linum where\r\n" + 
	  	  		"                       linum.periodono='XX' and linum.vertybeskodas=likoi.vertybeskodas and linum.objektokodas=likoi.objektokodas ),0)\r\n" + 
	  	  		"               as stock  from likoi where  objektokodas='0110' and isnull(saugojimovieta,'')<>'' group by VERTYBESKODAS,objektokodas\r\n" + 
	  	  		"                       having stock>0 order by VERTYBESKODAS) AA" + 
	  	  		" UNION select kodas as toode,999 as stock from usrPrekiuRinkiniai\r\n" + 
	  	  		"";

		          System.out.println(cmd);

		           stocks = new HashMap<String,Double>();

		         try {
		         Connection con =  DriverManager.getConnection(url,"","");
		           Statement st = con.createStatement();
		           ResultSet rs = st.executeQuery(cmd);

		           while (rs.next()) {

		             String kood = rs.getString("toode").trim();
		             double seis = rs.getDouble("stock");
		             if(seis > 0){
		            	 stocks.put(kood, seis);
		               }
		             }
		             rs.close();
		             st.close();
		             con.close();
		         } catch (Exception e) {
		             e.printStackTrace();
		         }
		     }


	   TreeMap groupProducts = new TreeMap();
	   
	   public TreeMap getProducts(Agent agent){
	    	getSaveStockInfo();
	    	System.out.println("stocks suurus " + stocks.size());
	  
	    String cmd = "select toode,nimetus,tykke,myygihind,ean,unit,vat,kogus,grupp,grupp2 from (" + 
		   		"SELECT KODAS AS toode,PAVADINIMAS  AS nimetus,1  AS tykke, 0 AS myygihind, " + 
		   		"   BARKODAS AS ean, APSKVNTKODAS as unit, PVMPROCENTAS as vat,  " + 
		   		"   0 AS kogus,(select parentKodas from gruko g where g.kodas = VERTYBGRUPESKODAS) AS grupp, VERTYBGRUPESKODAS AS grupp2 " + 
		   		"    FROM verko a  where SASKPREKODAS = '2040' and BARKODAS != '') BB " + 
		   		"union select kodas as toode,pavadinimas as nimetus,1 as tykke,0 as myygihind,'9999999999999' as ean,'RINK' as unit,21 as vat, "
		   		+ " 0 as kogus,'Rinkiniai' as grupp,'Rinkiniai2' as  grupp2 " + 
		   		"from usrprekiurinkiniai " ;
		     
	   //siia juurde siis tingimus et välja jätta soovimatud kaubad 
	     
	            System.out.println(cmd);
	        TreeMap products = new TreeMap();
	     	groupProducts = new TreeMap();
		       
	        
	        try {
	        Connection con =  DriverManager.getConnection(url,"","");
		    
	           PreparedStatement st = con.prepareStatement(cmd);
	            ResultSet	      rs = st.executeQuery();

	            
	            while (rs.next()) {
	              String kood = rs.getString("toode").trim();
	              Product prod = new Product(kood);
	              String nimetus = rs.getString("nimetus").trim();
	             
	              String ean = rs.getString("ean");
	              if(ean != null && ean.trim().length()>0){
	            	      prod.setEAN(ean.trim());	
	              }else{
	            	  prod.setEAN("0000000000"); 
	              }
	       
	              prod.setName(kood);
	              prod.addBarcode(kood);
	              
	              if(stocks.get(prod.getID()) != null){
	            	  prod.setStock((Double)stocks.get(prod.getID())); 
	            	}
	              
	              String grupp = rs.getString("grupp");
	             if (grupp != null && grupp.trim().length()>0){
	            	 prod.setGroup(this.ensure6(grupp.trim()));
	             }else{
	            	 prod.setGroup("000000");
	             }
	             String grupp2 = rs.getString("grupp2");
	             if (grupp2 != null && grupp2.trim().length()>0){
	               prod.setGroupSecond(prod.getGroup()+ "." + grupp2.trim());
	             }
	             
	             boolean goes = true;
	            
	             if(prod.getStock() < 0){
	            	 goes = false;
	             }
	             products.put(prod.getID(), prod);
	             
	             if(goes){
	                 if(groupProducts.get(prod.getEAN()) == null){
	                	 Map sisu = new HashMap();
	                	 sisu.put(kood, prod.getStock());
	                	 groupProducts.put(prod.getEAN(), sortByValue(sisu));
	                 }else{
	                	 Map sisu = (HashMap)groupProducts.get(prod.getEAN());
	                	 sisu.put(kood, prod.getStock());
	                	 groupProducts.put(prod.getEAN(), sortByValue(sisu));
	                 }
	             }

	            
	            }
	             rs.close();
	             st.close();
	             con.close();
	        } catch (Exception e) {
	           e.printStackTrace();
	        }

	        System.out.println("products " + products.size());
	       // System.out.println("productGroups " + groupProducts.toString());
	        return products;
	    }
	   
	   
	   private static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
		    List<Entry<K, V>> list = new LinkedList<Entry<K, V>>(map.entrySet());
		    Collections.sort(list, new Comparator<Object>() {
		        @SuppressWarnings("unchecked")
		        public int compare(Object o1, Object o2) {
		            return ((Comparable<V>) ((Map.Entry<K, V>) (o2)).getValue()).compareTo(((Map.Entry<K, V>) (o1)).getValue());
		        }
		    });

		    Map<K, V> result = new LinkedHashMap<K, V>();
		    for (Iterator<Entry<K, V>> it = list.iterator(); it.hasNext();) {
		        Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
		        result.put(entry.getKey(), entry.getValue());
		    }

		    return result;
		}
	   
	   private String ensure6 (String asi){
	    	if(asi != null){
	    		if(asi.length() > 6 ){
	    			asi = asi.substring(0,6);
	    		}else if (asi.length() == 0){
	    			asi =  "000000";
	    		}else if (asi.length() == 1){
	    			asi = asi + "00000";
	    		}else if (asi.length() == 2){
	    			asi = asi + "0000";
	    		}
	    		else if (asi.length() == 3){
	    			asi = asi + "000";
	    		}
	    		else if (asi.length() == 4){
	    			asi = asi + "00";
	    		}else if (asi.length() == 5){
	    			asi = asi + "0";
	    		}
	    	}else{
	    		asi =  "000000";	
	    	}
	    	
	    	return asi ;
	    }
	    

	   
	   
  	private void loadSignatures(){
  	
  try{		
  		File f = new File("signatures.vec");
  		Calendar ss = Calendar.getInstance();
  		
  		if (f.exists() && f.lastModified() < ss.getTimeInMillis() - 7200000) {// kui on yle tunni vana siis maha 
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



  public String identifyCustomer(String id,String regnr) throws SQLException {
    return id;
 }

 
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	return prodID;
  }


  private 	TreeMap   products =  null;
  
    
  
  public void makeOrder(Order order) throws SQLException{

	  
	  if (signatures == null){
   	   signatures = new Vector();
      }
	  counter = 0;
	  
	  String signature = order.getSignature();
	
	  System.out.println("signature" + signatures.contains(signature) + "  "+ signature );
		
	  if(signatures.contains(signature)){
		  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
	  }else{
		
	  if(products == null){
		  products =  getProducts(null);
	  }
	  
	/*  Vector splitted =  splitOrder(order);
 	  Enumeration en = splitted.elements();
 	  while(en.hasMoreElements()){
 		  Order ord = (Order)en.nextElement();
 			  makeOutput(ord);  
	}*/
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

  
  private Vector analyse(Vector lines){
	  
	  if(	stocks == null ){
			getSaveStockInfo();
	  }
	  Vector linex = new Vector();
	  HashSet olemas = new HashSet();
	  
	  
	  
	  for(int i = 0 ; i < lines.size(); i++){
		  OrderLine ol = (OrderLine)lines.elementAt(i);
		  olemas.add(ol.getToode());
	  }
		  
	  for(int i = 0 ; i < lines.size(); i++){
		  OrderLine ol = (OrderLine)lines.elementAt(i);
		  double am = ol.getKogus();
		  String pr = ol.getToode();
		//  System.out.println("pr " + pr + " st. " + stocks);
		  
		  double laos =  0;
		  try{
			 laos =  stocks.get(pr);
		  }catch(Exception e){
			  
		  }
		  
		  
		  if(laos > am){
			  linex.add(ol);
			  //muudame bubbris saadavat kogust ka 
			  stocks.put(pr, laos - am);
		  }else{
			  if(laos != 0){
				
				// muudame olemasoleva rea koguse ära selleks mis on laos ja jäägi paneme  
					
				  ol.setKogus(laos);
				  stocks.put(pr, 0.0);
				  linex.add(ol);
				  
				  
				  // määrame uue toote ja teeme jäägiga lisa rea 
				  OrderLine oo = new OrderLine ();
				 Product mas = (Product)products.get(pr);
				 String group = mas.getEAN();
				 if(groupProducts.get(group) != null){
					Map vv = (Map)groupProducts.get(group);
					Iterator keys = vv.keySet().iterator();
					while (keys.hasNext()){
						String pro = (String)keys.next();
						if(!olemas.contains(pro)){
						
						
						double jääk = stocks.get(pro);
					   if(jääk > am-laos){
						   oo.setKogus(am-laos);
						   oo.setToode(pro);
						   oo.setUnit("VNT");
						   linex.add(oo);
						   olemas.add(pro);
						   System.out.println("Lisan osalise toote  " + pr + "->" + pro + " laos " + jääk + " " + am  );
						   stocks.put(pro,jääk - (am-laos) );
						   break;
					   }
					}
						
					}
					
				 }
						 
			  }else{
				//kui laoseis on 0 siis otsime uue toote 
				  
				  
					 Product mas = (Product)products.get(pr);
					 String group = mas.getEAN();
					 if(groupProducts.get(group) != null){
						Map vv = (Map)groupProducts.get(group);
						Iterator keys = vv.keySet().iterator();
						while (keys.hasNext()){
							String pro = (String)keys.next();
							if(!olemas.contains(pro)){
								
							
						   double jääk = stocks.get(pro);
						   if(jääk > am){
							   ol.setKogus(am);
							   ol.setToode(pro);
							   
							   linex.add(ol);
							   olemas.add(pro);
							   stocks.put(pro,jääk - (am) );
								
							   System.out.println("Asendasin toote  " + pr + "->" + pro + " laos " + jääk + " " + am  );
							   break;
						   }
						}
						}
					 }
			  }
		  }
		  
	  }
	  
	  return linex;
  }
  
  
  private void makeOutput(Order order){
  	StringBuffer sb = new StringBuffer();
  	 nf.setGroupingUsed(false);

  	try {
 		
 				
 		String ware = order.getWarehouse();
 		System.out.println("ladu tellimusel " + ware);
 	
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
            

            if( order.getStatus() == 1){ // on pakkumine 
   	 			type = "offer";
   	 			subType = "offer";
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
            }else{
            	 sb.append("<DocumentShipMode>MANAGER</DocumentShipMode>" + ls); 
                      	
            }
            String add = (Calendar.getInstance().getTimeInMillis() + "").substring(11);
            
            sb.append("<DocumentNum>MRS" + escape(order.getDokNr()).trim() + add + "</DocumentNum>" + ls); 
            sb.append("<DateInfo>" + ls);
            sb.append("<OrderDate>" +escape(getDateString(Calendar.getInstance())) + "</OrderDate>" + ls);
           
            if(order.getShipMode() != null && order.getShipMode().trim().length() > 0 ){
            	//  sb.append("<DeliveryWay>" + order.getShipMode() + "</DeliveryWay>" + ls);
            }
            
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
            
            Vector sortedLines = analyse(order.getOrderLines());
            System.out.println("Enne ja pärast ridu " + order.getOrderLines().size() + " " +  sortedLines.size() );
           
            
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
                if(order.getStatus() > 3){
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
                   sb.append("         <AmountOrdered>" + escape(getNF().format(line.getKogus())) + "</AmountOrdered>" + ls);
                 
                if(line.getDiscount() > 0){
              
             double discprice = (100d -line.getDiscount())*line.getPrice()/100d ;
                sb.append("         <ItemPrice>" + escape(getNF().format(line.getPrice())) + "</ItemPrice>" + ls); 
                sb.append("         <ItemSum>" + escape(getNF().format(line.getPrice() * line.getKogus())) + "</ItemSum>" + ls);
                sb.append("         <Addition addCode=\"DSC\">" + ls);
                sb.append("              <AddRate>" + escape(getNF().format(line.getDiscount())) + "</AddRate>" + ls);
                sb.append("          </Addition>" + ls);
                }
                
               
                sb.append("      </ItemEntry>" + ls);
      
                
                
            }
            
            if(counter != order.getOrderLines().size() ){
            	counter = order.getOrderLines().size() ;
            }

            sb.append("   </DocumentItem>" + ls);
            
           
String com = null;

if(order.getInfo() != null && order.getInfo().trim().length() >0){
		com =  order.getInfo() ;
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
    return "PalettiLitImport";
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
 
  private int counter = 0;
  private ImportPanel parent;
  private String trouble = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return trouble;
}
}
