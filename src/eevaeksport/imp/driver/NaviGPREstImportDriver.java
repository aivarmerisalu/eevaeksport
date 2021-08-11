
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

public class NaviGPREstImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(NaviGPREstImportDriver.class.getName());

  
  public NaviGPREstImportDriver(ImportPanel parent) {
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
  private String url2 = "jdbc:odbc:naviest";
  private String base = "Pernod Ricard Estonia";
  private Hashtable dates = new Hashtable();
  	  
  	private Connection getNewConnection2() throws SQLException{
  	    return DriverManager.getConnection(url2, parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));
  	  }
  	
  	 public TreeMap getProducts(Agent agent){
  	     TreeMap tm = new TreeMap();
  	        	 
  	  
  	     try {
  	     	String cmd = "  SELECT t.[No_] AS toode,t.[Search Description] AS nimi, sp.[Unit Price] AS myygihind,t.[Product Group Code] AS grupp ,t.[Item Category Code] AS grupp2  " +
  	     			" ,t.[Base Unit of Measure] AS unit,[Cross-Reference No_] AS ean,'' AS status ,0 AS maxdiscount, " +
  	     			"		 '' AS type, [Qty_ per Unit of Measure] AS kastis,  '' AS kaal, [VAT Prod_ Posting Group] AS vat "+  
  	     			"		 ,1 as goes " +
  	     			"		  FROM [" + base +"$Item] t " +
  	     			"		  LEFT JOIN  [" + base +"$Item Unit of Measure] vat ON (vat.[Item No_]=[No_] and vat.[Code]='KAST' )  " +
  	     			"		  left join  [" + base +"$Sales Price] sp on sp.[Item No_]=t.[No_] and sp.[Sales Code] ='BASE' and sp.[Starting Date] < getdate()+5  and  " +
  	     			"		   (sp.[Ending Date] > getdate()+1 or year (sp.[Ending Date]) = 1753) " +
  	     			"		   left join [" + base + "$Item Cross Reference] ref on ref.[Item No_]=t.[No_]  and [Unit of Measure]=t.[Base Unit of Measure]   " +
  	     			"		  WHERE t.[Blocked]=0     and sp.[Unit Price] > 0  ORDER BY t.[No_]";
  	     	
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
	   makeOutput(order);
	    trouble = null;
      setNumAddedLines(counter);
      
  }

  private String getDateTimeString(Calendar cal){
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
	    sb.append(day+ "T");
	    
	    String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
	    if (hour.length() == 1){
	      sb.append("0");
	    }
	    sb.append(hour+":");
	    
	    String minu = String.valueOf(cal.get(Calendar.MINUTE));
	    if (minu.length() == 1){
		      sb.append("0");
		    }
		 sb.append(minu+":00");
		   
	    return sb.toString();
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

private String getCustId(String cuid) throws SQLException{
	
	
	     	String cmd = "    Select [GLN Code] as gln from [Pernod Ricard Estonia$Customer] where No_='" + cuid + "'";
	     	
	      System.out.println(cmd);
	      
	     	Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        if(rs.next()){
	        return rs.getString("gln").trim();
	        }
	            
	return null;
}
  
  private void makeOutput(Order order){
  	StringBuffer sb = new StringBuffer();
  	 nf.setGroupingUsed(false);

  	try {
  	  int cou = 0;
 		
 				String custID = order.getCompany();
 				try{
 					custID = getCustId(Converter.deCode(order.getCompany()));
 				}catch(Exception r){
 					r.printStackTrace();
 				}
 				if(custID == null || custID.trim().length() == 0){
 					custID = order.getCompany();
 				}
 				
 		String ware = order.getWarehouse();
 		
 		System.out.println("Klient tellimusel " + custID);
 		
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
   
       

	          String agent = order.getAgent();
	            if(fixagent != null){
	          	  agent = fixagent;
	            }
        
            Calendar cal = Calendar.getInstance();
           
            if (shipDate != null) {
                  cal = shipDate;
                }
          
            sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + ls);
            sb.append("<sh:StandardBusinessDocument xmlns:sh=\"http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader\" xmlns:eanucc=\"urn:ean.ucc:2\" xmlns:str=\"http://exslt.org/strings\" xmlns:exsl=\"http://exslt.org/common\" xmlns:sh4=\"urn:ean.ucc:order:2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:order=\"urn:ean.ucc:order:2\">" + ls);
       
            
            
                    sb.append("<sh:StandardBusinessDocumentHeader>" + ls);
            		sb.append("<sh:HeaderVersion>1.0</sh:HeaderVersion>" + ls);
            		sb.append("<sh:Sender>" + ls);
            		sb.append("<sh:Identifier>3725624803400</sh:Identifier>" + ls);
            		sb.append("</sh:Sender>" + ls);
            		sb.append("<sh:Receiver>" + ls);
            		sb.append("<sh:Identifier>4747009000001</sh:Identifier>" + ls);
            		sb.append("</sh:Receiver>" + ls);
            		sb.append("<sh:DocumentIdentification>" + ls);
            		sb.append("<sh:Standard>EAN.UCC</sh:Standard>" + ls);
            		sb.append("<sh:TypeVersion>2.5</sh:TypeVersion>" + ls);
            		sb.append("<sh:InstanceIdentifier>NA</sh:InstanceIdentifier>" + ls);
            		sb.append("<sh:Type>multiShipmentOrder</sh:Type>" + ls);
            		sb.append("<sh:MultipleType>false</sh:MultipleType>" + ls);
            		sb.append("<sh:CreationDateAndTime>" +escape(getDateTimeString(Calendar.getInstance())) +"</sh:CreationDateAndTime>" + ls);
            		sb.append("</sh:DocumentIdentification>"+ ls);
            		sb.append("</sh:StandardBusinessDocumentHeader>"+ ls);
            		
            
            
            		
            sb.append("<eanucc:message>" + ls);
            		
            		sb.append("<entityIdentification>" + ls);
            		sb.append("<uniqueCreatorIdentification>NA</uniqueCreatorIdentification>" + ls);
            		sb.append("<contentOwner>" + ls);
            		sb.append("<gln>3725624803400</gln>" + ls);
            		sb.append("</contentOwner>" + ls);
            		sb.append("</entityIdentification>" + ls);		
            
            		  sb.append("<eanucc:transaction>" + ls);
                		sb.append("<entityIdentification>" + ls);
                		sb.append("<uniqueCreatorIdentification>NA</uniqueCreatorIdentification>" + ls);
                		sb.append("<contentOwner>" + ls);
                		sb.append("<gln>3725624803400</gln>" + ls);
                		sb.append("</contentOwner>" + ls);
                		sb.append("</entityIdentification>" + ls);
                		sb.append("<command>" + ls);
                		sb.append("<eanucc:documentCommand>" + ls);
                		sb.append("<documentCommandHeader type=\"ADD\">" + ls);
                		sb.append("<entityIdentification>" + ls);
                		sb.append("<uniqueCreatorIdentification>NA</uniqueCreatorIdentification>" + ls);
                		sb.append("<contentOwner>" + ls);
                		sb.append("<gln>3725624803400</gln>" + ls);
                		sb.append("</contentOwner>" + ls);
                		sb.append("</entityIdentification>" + ls);
                		sb.append("</documentCommandHeader>" + ls);
                		
	            		
						sb.append("<documentCommandOperand>" + ls);
						sb.append("<order:multiShipmentOrder documentStatus=\"ORIGINAL\" creationDateTime=\"" + escape(getDateString(Calendar.getInstance())) + "\">" + ls);
						sb.append("<orderIdentification>" + ls);
						sb.append("<uniqueCreatorIdentification>" + agent+ "-" + order.getDokNr() + "</uniqueCreatorIdentification>" + ls);
						sb.append("<contentOwner>" + ls);
						sb.append("<gln>3725624803400</gln>" + ls);
						sb.append("</contentOwner>" + ls);
						sb.append("</orderIdentification>" + ls);
						sb.append("<orderPartyInformation>" + ls);
						sb.append("<seller>" + ls);
						sb.append("<gln>4747009000001</gln>" + ls);
						sb.append("</seller>" + ls);
						sb.append("<buyer>" + ls);
						sb.append("<gln>" + custID + "</gln>" + ls);
						sb.append("</buyer>" + ls);
						sb.append("</orderPartyInformation>" + ls);
							sb.append("<orderLogisticalInformation>" + ls );
							sb.append("<shipToLogistics>" + ls );
							sb.append("<shipTo>" + ls );
							sb.append("<gln>" + custID + "</gln>" + ls );
							sb.append("<additionalPartyIdentification>" + ls );
							sb.append("<additionalPartyIdentificationValue/>" + ls );
							sb.append("<additionalPartyIdentificationType>FOR_INTERNAL_USE_1</additionalPartyIdentificationType>" + ls );
							sb.append("</additionalPartyIdentification>" + ls );
							sb.append("</shipTo>" + ls );
							sb.append("</shipToLogistics>" + ls );
							sb.append("<orderLogisticalDateGroup>" + ls );
							sb.append("<requestedDeliveryDate>" + ls );
							sb.append("<date>" + escape(getDateString(cal)) + "</date>" + ls );
							sb.append("</requestedDeliveryDate>" + ls );
							sb.append("</orderLogisticalDateGroup>" + ls );
							sb.append("</orderLogisticalInformation>" + ls );


			            Vector sortedLines = order.getOrderLines();
			            
			            for (int i = 0; i < sortedLines.size(); i++) {
			            
			                OrderLine line = (OrderLine)sortedLines.elementAt(i);
			                double ale = line.getDiscount();     
			                counter++;
			                String yhik = line.getUnit();
			                String nimi = line.getTooteNimi();
			                if (ale == -1 ){
			                    ale = 0 ;
			                    }
			            
			               String ean = null;
			                	Product prod = (Product)this.prods.get(line.getToode());
			                	if(prod!= null){
			                		 ean = prod.getEAN();	
			                		 nimi = prod.getName();
			                		 yhik = prod.getUnit();
			                	}
			              
			                if(ean == null && eans.get(line.getToode()) != null){
			                	ean = eans.get(line.getToode())+"";
			                
			                }
			                
			                
			                if(ean == null ){
			                	ean = "0000000000000";
			                }
			                
			                System.out.println("ean " + ean);
						   sb.append("<multiShipmentOrderLineItem number=\"" + ((i+1)*10) + "\">" + ls);
						   
						   
							sb.append("<requestedQuantity>" + ls);
							sb.append("<value>" + line.getKogus() + "</value>" + ls);
							sb.append("<unitOfMeasure>" + ls);
							sb.append("<measurementUnitCodeValue>" + yhik + "</measurementUnitCodeValue>" + ls);
							sb.append("</unitOfMeasure>" + ls);
							sb.append("</requestedQuantity>" + ls);
							sb.append("<tradeItemIdentification>" + ls);
							sb.append("<gtin>" + ean + "</gtin>" + ls);
							sb.append("<additionalTradeItemIdentification>" + ls);
							sb.append("<additionalTradeItemIdentificationValue>" + line.getToode() + "</additionalTradeItemIdentificationValue>" + ls);
							sb.append("<additionalTradeItemIdentificationType>SUPPLIER_ASSIGNED</additionalTradeItemIdentificationType>" + ls);
							sb.append("</additionalTradeItemIdentification>" + ls);
							sb.append("<additionalTradeItemIdentification>" + ls);
							sb.append("<additionalTradeItemIdentificationValue/>" + ls);
							sb.append("<additionalTradeItemIdentificationType>BUYER_ASSIGNED</additionalTradeItemIdentificationType>" + ls);
							sb.append("</additionalTradeItemIdentification>" + ls);
							sb.append("</tradeItemIdentification>" + ls);
							sb.append("<lineItemActionCode>ADDITION</lineItemActionCode>" + ls);
													   
													   
						   sb.append("</multiShipmentOrderLineItem>" + ls);
						   
						
			            }
			            
			            
						sb.append("</order:multiShipmentOrder>" + ls);
						sb.append("</documentCommandOperand>" + ls);
						
						
               

            
            
            
           
String com = null;

if(order.getInfo() != null && order.getInfo().trim().length() >0){
		com =  order.getInfo() ;
	}
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
			
			if(ARVE.length() > 70){
				ARVE = ARVE.substring(0,70);
			}
			if(KT.length() > 70){
				KT = KT.substring(0,70);
			}
			if(KOMPL.length() > 70){
				KOMPL = KOMPL.substring(0,70);
			}
			
			System.out.println("KT:" + KT + "KOMPL:" + KOMPL + "ARVE:" + ARVE);

            
              
            //sb.append("<documentCommandOperand>" + ls);
      		sb.append("</eanucc:documentCommand>" + ls);
      		sb.append("</command>" + ls);
      		sb.append("</eanucc:transaction>" + ls);
      		
              
              sb.append("   </eanucc:message>" + ls);
              sb.append("   </sh:StandardBusinessDocument>" + ls);

              counter = counter + cou;
     	//
       String fn = doknr;
       if(doknr.trim().length()== 0){
     	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
       }
       String fileName = escape(parent.getProps().getProperty("conv.dir") + agent)  + "." + fn  +  ".xml";
       System.out.println(fileName);
      // Converter.saveFileInUTF8(fileName, sb);
       Converter.saveFile(fileName, sb.toString());

 	} catch (Exception e) {
 		e.printStackTrace();
 	}
  }

 
	public NumberFormat getNF() {
		return this.nf;
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
