
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
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

public class SamsonasCSVImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public SamsonasCSVImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    try {
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  
  private Vector splitOrder (Order order){
	  Vector result = new Vector();
      
	 System.out.println("Enne suurus splitti" + order.getOrderLines().size());
	
	  Order ord23 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ordOTB = new Order(order.getCompany(),order.getAgentFirstName());
    
	  ord23.setAgentCode(order.getAgentCode());
	  ord23.setDokNr(order.getDokNr());
	  ord23.setInfo(order.getInfo());
	  ord23.setReference(order.getReference());
	  ord23.setTeostaja((order.getTeostaja()));
	  ord23.setChanged(order.isChanged());
	  ord23.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord23.setStatus(order.getStatus());
	  ord23.setShipTime(order.getShipTime());
	  ord23.setShipDateUsed(order.isShipDateUsed());
	  ord23.setWarehouse(order.getWarehouse());
	  ord23.setCurrency("MEAT");
	  
	

	  ordOTB.setAgentCode(order.getAgentCode());
	  ordOTB.setDokNr(order.getDokNr());
	  ordOTB.setInfo(order.getInfo());
	  ordOTB.setReference(order.getReference());
	  ordOTB.setTeostaja((order.getTeostaja()));
	  ordOTB.setChanged(order.isChanged());
	  ordOTB.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordOTB.setStatus(order.getStatus());
	  ordOTB.setShipTime(order.getShipTime());
	  ordOTB.setShipDateUsed(order.isShipDateUsed());
	  ordOTB.setWarehouse(order.getWarehouse());
	  ordOTB.setCurrency("VEGE");
	  
	
	
	//  System.out.println("Splitin Warehouse " + order.getWarehouse());
	  
	  Vector lines = order.getOrderLines();
	  Enumeration enums = lines.elements();
	  while  (enums.hasMoreElements()){
		  OrderLine lin = (OrderLine)enums.nextElement();
		
		     String itemid = lin.getToode();
		  
		    	 if(itemid != null) {
		    	
		    	 if(itemid.startsWith("OTB") ){
			    	 ordOTB.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,lin.docType);
		          }else {
		        	  ord23.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,lin.docType);
			          } 
		     } 
	  }

	  if(ord23.getOrderLines() != null && ord23.getOrderLines().size()>0){
		  result.add(ord23);  
	  }

	  if(ordOTB.getOrderLines() != null && ordOTB.getOrderLines().size()>0){
		  result.add(ordOTB);  
	  }
//	 System.out.println("Result =" +order.getDokNr()
	//		 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }


  public String identifyCustomer(String id,String regnr) throws SQLException {
    return null;
    }
  
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
     return prodID;
  }


  
  public void makeOrder(Order order) throws SQLException{

	  counter = 0;
	  makeOutput(order,true); 	    
	  setNumAddedLines(counter);
	  Vector splitted =  new Vector(); //splitOrder(order);
      
	  Enumeration en = splitted.elements();
	  while(en.hasMoreElements()){
		  Order ord = (Order)en.nextElement();
		  
	       boolean tagastus = false;
	         Vector lin = order.getOrderLines();
	         for (int i = 0; i < lin.size(); i++) {
	        	 OrderLine line = (OrderLine)lin.elementAt(i);
	        	 if(line.getKogus() < 0 ) {
	        		 tagastus = true;
	        	 }
	         }
	
	    if(ord.getCurrency().equals("MEAT")){
                  makeOutput(ord,true);  
               } else {
   				  makeOutput(ord,false);  
   			  }
          } 

  }

  
   
  private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	     sb.append((cal.get(Calendar.YEAR) + "").substring(2)+ "/");
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month + "/");
	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day);
	    return sb.toString();
	  }
	  


  
  private void makeOutput(Order order , boolean meat){
	 	StringBuffer sb = new StringBuffer();
	 	boolean minus = false;
	 	try {

	 		
		    String doknr = order.getDokNr();
		      String fn = doknr;
		      if(doknr.trim().length() == 0){
		    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
		      }
		  
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
	         
	       
	         
	         
	    Vector lines = order.getOrderLines();
	      for (int i = 0; i < lines.size(); i++) {
	    	  
	        OrderLine ol = (OrderLine) lines.elementAt(i);
	        if(ol.getKogus() < 0){
	        	minus = true;
	        }
	    	
	        String sep = ",";
	         String lineSep = System.getProperty("line.separator");
	        
	         double ale = ol.getDiscount();
	         if(ale < 0){
	        	 ale = 0;
	         }
	         double hind = ol.getPrice();
	         
	         
	         /*document(order) No
companyCode
SalespersonCode
order date
invoice(delivery) date
row in document No
productCode
quantity
*/
	         sb.append(doknr + sep );
	         sb.append(order.getCompany() + sep );
	         sb.append(agent + sep);
	         sb.append(getDateString(Calendar.getInstance()) + sep);
	         sb.append(getDateString(order.getShipCal()) + sep);
	         sb.append(counter + sep);
	         sb.append(ol.getToode() + sep);
	         sb.append(ol.getKogus() + sep);
	         sb.append(hind + sep);
	        if(ol.getDiscount() > 0) {
	        	 sb.append("1" + sep);
	        }else{
	        	 sb.append("0" + sep);
	        }
	       
	         sb.append("\"" + comm +"\"" + sep);
	         
	         
	         //  RIISIP	1MARMELAAD00	20.0000	1.5120	0.0000	05/19/2017	""	4
				//  RIISIP	1MARMELAAD04	20.0000	1.5120	0.0000	05/19/2017	""	4
		  
	          sb.append(lineSep);
	 
	       counter++;
	      }
	
	      
	      setNumAddedLines(counter); 

	    
	         	  String fileName = Converter.escapeXML(parent.getProps().getProperty("conv.dir")) +  "t-" + order.getAgent() +"."+ order.getCompany()+"." +fn  + ".csv";
			      System.out.println(fileName);		
			      if(minus){
			    	//  fileName = escape(parent.getProps().getProperty("conv.dir2")) +  "t-" + order.getAgent() +"."+ order.getCompany()+"." +fn  + ".csv";
			      }
			      FileOutputStream fos = new FileOutputStream(fileName);
			        fos.write(sb.toString().getBytes());
			        fos.close();
	    		

		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
	  
 
	public NumberFormat getNF() {
		return this.nf;
	}
	
 
  public String getDriverVersion(){
    return "SamsonasCSVImport";
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

  
public String getTrouble() {
	return null;
}
}
