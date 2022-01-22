
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

import org.apache.geronimo.mail.util.Base64;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
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

public class LabbisSkonisImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());
   boolean testing = false;
  
  public LabbisSkonisImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url, "sa","l@bb1s");
      con2 = DriverManager.getConnection(url, "sa","l@bb1s");
      con3 = DriverManager.getConnection(url, "sa","l@bb1s");
      con4 = DriverManager.getConnection(url, "sa","l@bb1s");
   
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
      if("true".equals( parent.getProps().getProperty("testing"))){
      	testing = true;
      }
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  private String orderware = null;
  
  
  private Vector splitOrder (Order order){
	  Vector result = new Vector();
	  
      if(order.getWarehouse() != null && order.getWarehouse().trim().length() > 1 && order.getAgent().startsWith("0")) {
    	  orderware = order.getWarehouse();
      } else {
    	  orderware = null;
      }
	  
	  Order l0BF001 = new Order(order.getCompany(),order.getAgentFirstName());

	  l0BF001.setAgentCode(order.getAgentCode());
	  l0BF001.setDokNr("1" + order.getDokNr());
	  l0BF001.setInfo(order.getInfo());
	  l0BF001.setTeostaja((order.getTeostaja()));
	  l0BF001.setChanged(order.isChanged());
	  l0BF001.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l0BF001.setStatus(order.getStatus());
	  l0BF001.setShipTime(order.getShipTime());
	  l0BF001.setWarehouse("0BF001");
	     

	  
	  
	  Order l0AS101 = new Order((order.getCompany()),order.getAgentFirstName());
	  l0AS101.setAgentCode(order.getAgentCode());
	  l0AS101.setDokNr("2" + order.getDokNr());
	  l0AS101.setInfo(order.getInfo());
	  l0AS101.setChanged(order.isChanged());
	  l0AS101.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l0AS101.setStatus(order.getStatus());
	  l0AS101.setShipTime(order.getShipTime());
	  l0AS101.setWarehouse("0AS101");
	
	  Order l0PS119 = new Order((order.getCompany()),order.getAgentFirstName());

	  l0PS119.setAgentCode(order.getAgentCode());
	  l0PS119.setDokNr("3" + order.getDokNr());
	  l0PS119.setInfo(order.getInfo());
	  l0PS119.setTeostaja((order.getTeostaja()));
	  l0PS119.setChanged(order.isChanged());
	  l0PS119.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l0PS119.setStatus(order.getStatus());
	  l0PS119.setShipTime(order.getShipTime());
	  l0PS119.setWarehouse("0PS119");  
	
	  

	  Order l0TS100 = new Order(order.getCompany(),order.getAgentFirstName());
	  l0TS100.setAgentCode(order.getAgentCode());
	  l0TS100.setDokNr("4" + order.getDokNr());
	  l0TS100.setInfo(order.getInfo());
	  l0TS100.setTeostaja((order.getTeostaja()));
	  l0TS100.setChanged(order.isChanged());
	  l0TS100.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l0TS100.setStatus(order.getStatus());
	  l0TS100.setShipTime(order.getShipTime());
	  l0TS100.setWarehouse("0TS100");  
	
	  Order l1BF002 = new Order((order.getCompany()),order.getAgentFirstName());
	  l1BF002.setAgentCode(order.getAgentCode());
	  l1BF002.setDokNr("5" + order.getDokNr());
	  l1BF002.setInfo(order.getInfo());
	  l1BF002.setTeostaja((order.getTeostaja()));
	  l1BF002.setChanged(order.isChanged());
	  l1BF002.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l1BF002.setStatus(order.getStatus());
	  l1BF002.setShipTime(order.getShipTime());
	  l1BF002.setWarehouse("1BF002");  
	
	  Order l1AS203 = new Order((order.getCompany()),order.getAgentFirstName());
	  l1AS203.setAgentCode(order.getAgentCode());
	  l1AS203.setDokNr("6" + order.getDokNr());
	  l1AS203.setInfo(order.getInfo());
	  l1AS203.setTeostaja((order.getTeostaja()));
	  l1AS203.setChanged(order.isChanged());
	  l1AS203.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l1AS203.setStatus(order.getStatus());
	  l1AS203.setShipTime(order.getShipTime());
	  l1AS203.setWarehouse("l1AS203");

	  Order l1PS210 = new Order(order.getCompany(),order.getAgentFirstName());
	  l1PS210.setAgentCode(order.getAgentCode());
	  l1PS210.setDokNr("7" + order.getDokNr());
	  l1PS210.setInfo(order.getInfo());
	  l1PS210.setTeostaja((order.getTeostaja()));
	  l1PS210.setChanged(order.isChanged());
	  l1PS210.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l1PS210.setStatus(order.getStatus());
	  l1PS210.setShipTime(order.getShipTime());
	  l1PS210.setWarehouse("1PS210");
	  
	  Order l1TS200 = new Order((order.getCompany()),order.getAgentFirstName());
	  l1TS200.setAgentCode(order.getAgentCode());
	  l1TS200.setDokNr("8" + order.getDokNr());
	  l1TS200.setInfo(order.getInfo());
	  l1TS200.setTeostaja((order.getTeostaja()));
	  l1TS200.setChanged(order.isChanged());
	  l1TS200.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l1TS200.setStatus(order.getStatus());
	  l1TS200.setShipTime(order.getShipTime());
	  l1TS200.setWarehouse("1TS200");
	  
	  Order l2BF003 = new Order((order.getCompany()),order.getAgentFirstName());
	  l2BF003.setAgentCode(order.getAgentCode());
	  l2BF003.setDokNr("9" + order.getDokNr());
	  l2BF003.setInfo(order.getInfo());
	  l2BF003.setTeostaja((order.getTeostaja()));
	  l2BF003.setChanged(order.isChanged());
	  l2BF003.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l2BF003.setStatus(order.getStatus());
	  l2BF003.setShipTime(order.getShipTime());
	  l2BF003.setWarehouse("2BF003");
	  

	  Order l2AS303 = new Order(order.getCompany(),order.getAgentFirstName());
	  l2AS303.setAgentCode(order.getAgentCode());
	  l2AS303.setDokNr("10" + order.getDokNr());
	  l2AS303.setInfo(order.getInfo());
	  l2AS303.setTeostaja((order.getTeostaja()));
	  l2AS303.setChanged(order.isChanged());
	  l2AS303.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l2AS303.setStatus(order.getStatus());
	  l2AS303.setShipTime(order.getShipTime());
	  l2AS303.setWarehouse("2AS303");
	  
	  
	  Order l2PS308 = new Order((order.getCompany()),order.getAgentFirstName());
	  l2PS308.setAgentCode(order.getAgentCode());
	  l2PS308.setDokNr("11" + order.getDokNr());
	  l2PS308.setInfo(order.getInfo());
	  l2PS308.setTeostaja((order.getTeostaja()));
	  l2PS308.setChanged(order.isChanged());
	  l2PS308.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l2PS308.setStatus(order.getStatus());
	  l2PS308.setShipTime(order.getShipTime());
	  l2PS308.setWarehouse("2PS308");

	  Order l2TS300 = new Order((order.getCompany()),order.getAgentFirstName());
	  l2TS300.setAgentCode(order.getAgentCode());
	  l2TS300.setDokNr("12" + order.getDokNr());
	  l2TS300.setInfo(order.getInfo());
	  l2TS300.setTeostaja((order.getTeostaja()));
	  l2TS300.setChanged(order.isChanged());
	  l2TS300.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l2TS300.setStatus(order.getStatus());
	  l2TS300.setShipTime(order.getShipTime());
	  l2TS300.setWarehouse("2TS300");
	  
	  Order l3BF004 = new Order(order.getCompany(),order.getAgentFirstName());
	  l3BF004.setAgentCode(order.getAgentCode());
	  l3BF004.setDokNr("13" + order.getDokNr());
	  l3BF004.setInfo(order.getInfo());
	  l3BF004.setTeostaja((order.getTeostaja()));
	  l3BF004.setChanged(order.isChanged());
	  l3BF004.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l3BF004.setStatus(order.getStatus());
	  l3BF004.setShipTime(order.getShipTime());
	  l3BF004.setWarehouse("3BF004");
	  
	  Order l3AS403 = new Order((order.getCompany()),order.getAgentFirstName());
	  l3AS403.setAgentCode(order.getAgentCode());
	  l3AS403.setDokNr("14" + order.getDokNr());
	  l3AS403.setInfo(order.getInfo());
	  l3AS403.setTeostaja((order.getTeostaja()));
	  l3AS403.setChanged(order.isChanged());
	  l3AS403.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l3AS403.setStatus(order.getStatus());
	  l3AS403.setShipTime(order.getShipTime());
	  l3AS403.setWarehouse("3AS403");
	  
	  Order l3PS404 = new Order((order.getCompany()),order.getAgentFirstName());
	  l3PS404.setAgentCode(order.getAgentCode());
	  l3PS404.setDokNr("15" + order.getDokNr());
	  l3PS404.setInfo(order.getInfo());
	  l3PS404.setTeostaja((order.getTeostaja()));
	  l3PS404.setChanged(order.isChanged());
	  l3PS404.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l3PS404.setStatus(order.getStatus());
	  l3PS404.setShipTime(order.getShipTime());
	  l3PS404.setWarehouse("3PS404");
	  
	  Order l3TS400 = new Order((order.getCompany()),order.getAgentFirstName());
	  l3TS400.setAgentCode(order.getAgentCode());
	  l3TS400.setDokNr("16" + order.getDokNr());
	  l3TS400.setInfo(order.getInfo());
	  l3TS400.setTeostaja((order.getTeostaja()));
	  l3TS400.setChanged(order.isChanged());
	  l3TS400.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l3TS400.setStatus(order.getStatus());
	  l3TS400.setShipTime(order.getShipTime());
	  l3TS400.setWarehouse("3TS400"); 
	  
	  
	  
	  Order l4XX002 = new Order((order.getCompany()),order.getAgentFirstName());
	  l4XX002.setAgentCode(order.getAgentCode());
	  l4XX002.setDokNr("17" + order.getDokNr());
	  l4XX002.setInfo(order.getInfo());
	  l4XX002.setTeostaja((order.getTeostaja()));
	  l4XX002.setChanged(order.isChanged());
	  l4XX002.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l4XX002.setStatus(order.getStatus());
	  l4XX002.setShipTime(order.getShipTime());
	  l4XX002.setWarehouse("4XX002"); 
	  
	 

	  
	  Order l0AS132 = new Order(order.getCompany(),order.getAgentFirstName());

	  l0AS132.setAgentCode(order.getAgentCode());
	  l0AS132.setDokNr("18" + order.getDokNr());
	  l0AS132.setInfo(order.getInfo());
	  l0AS132.setTeostaja((order.getTeostaja()));
	  l0AS132.setChanged(order.isChanged());
	  l0AS132.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l0AS132.setStatus(order.getStatus());
	  l0AS132.setShipTime(order.getShipTime());
	  l0AS132.setWarehouse("0AS132");
	  
	
	  Order l0AS131 = new Order(order.getCompany(),order.getAgentFirstName());

	  l0AS131.setAgentCode(order.getAgentCode());
	  l0AS131.setDokNr("19" + order.getDokNr());
	  l0AS131.setInfo(order.getInfo());
	  l0AS131.setTeostaja((order.getTeostaja()));
	  l0AS131.setChanged(order.isChanged());
	  l0AS131.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  l0AS131.setStatus(order.getStatus());
	  l0AS131.setShipTime(order.getShipTime());
	  l0AS131.setWarehouse("0AS131");
	  
	  
	  
	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	  
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
		
		    String ware = "";
			try {
				ware = identifyWarehouse(lin.getToode(),order.getAgentCode());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(orderware != null) {
				ware = orderware;
			}
			if(ware == null){
				ware = "";
			}
			 if(ware.equals("4XX002")){  
		    	 l4XX002.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);	 
		     	 }else  if(ware.equals("0BF001")){  
		    	 l0BF001.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);	 
		     	 }else if(ware.equals("0AS101") ){
		        	 l0AS101.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			      }  else if(ware.equals("0PS119") ){
				    	l0PS119.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
				     } else if(ware.equals("0TS100") ){
					    	l0TS100.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
					     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
					     } else if(ware.equals("1BF002") ){
					    	 l1BF002.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
						     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
						     } else if(ware.equals("1AS203") ){
							    	l1AS203.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
							     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
							     } else if(ware.equals("1PS210") ){
								    	l1PS210.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
								     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
								     } else if(ware.equals("1TS200") ){
								    	 l1TS200.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
									     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
									     } else if(ware.equals("2BF003") ){
									    	 l2BF003.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
										     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
										     }else if(ware.equals("2AS303") ){
									    	 l2AS303.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
										     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
										     }else if(ware.equals("2PS308") ){
										    	 l2PS308.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
										     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
										     }else if(ware.equals("2TS300") ){
									    	 l2TS300.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
										     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
										     }else if(ware.equals("3BF004") ){
									    	 l3BF004.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
										     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
										     }else if(ware.equals("3AS403") ){
									    	 l3AS403.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
										     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
										     }else if(ware.equals("3PS404") ){
									    	 l3PS404.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
										     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
										     }else if(ware.equals("3TS400") ){
									    	 l3TS400.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
										     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
										     }   else if(ware.equals("0AS132") ){
										    	 l0AS132.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
											     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
											     }
										     else if(ware.equals("0AS131") ){
										    	 l0AS131.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
											     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
											     }
		     
		     

	  }


	  if(l0AS132.getOrderLines() != null && l0AS132.getOrderLines().size()>0){
		  result.add(l0AS132);  
	  }
	  if(l0AS131.getOrderLines() != null && l0AS131.getOrderLines().size()>0){
		  result.add(l0AS131);  
	  }
	  

	  if(l0BF001.getOrderLines() != null && l0BF001.getOrderLines().size()>0){
		  result.add(l0BF001);  
	  }

	  if(l0AS101.getOrderLines() != null && l0AS101.getOrderLines().size()>0){
		  result.add(l0AS101);  
	  }
	  
	  if(l0PS119.getOrderLines() != null && l0PS119.getOrderLines().size()>0){
		  result.add(l0PS119);  
	  }
	  
	  if(l4XX002.getOrderLines() != null && l4XX002.getOrderLines().size()>0){
		  result.add(l4XX002);  
	  }
	  

	  if(l2TS300.getOrderLines() != null && l2TS300.getOrderLines().size()>0){
		  result.add(l2TS300);  
	  }
	  if(l2PS308.getOrderLines() != null && l2PS308.getOrderLines().size()>0){
		  result.add(l2PS308);  
	  }
	  if(l2AS303.getOrderLines() != null && l2AS303.getOrderLines().size()>0){
		  result.add(l2AS303);  
	  }
	  if(l2BF003.getOrderLines() != null && l2BF003.getOrderLines().size()>0){
		  result.add(l2BF003);  
	  }
	  if(l1TS200.getOrderLines() != null && l1TS200.getOrderLines().size()>0){
		  result.add(l1TS200);  
	  }
	  if(l1PS210.getOrderLines() != null && l1PS210.getOrderLines().size()>0){
		  result.add(l1PS210);  
	  }
	  if(l1AS203.getOrderLines() != null && l1AS203.getOrderLines().size()>0){
		  result.add(l1AS203);  
	  }

	  if(l1BF002.getOrderLines() != null && l1BF002.getOrderLines().size()>0){
		  result.add(l1BF002);  
	  }

	  if(l0TS100.getOrderLines() != null && l0TS100.getOrderLines().size()>0){
		  result.add(l0TS100);  
	  }


	  
	  

	  if(l3AS403.getOrderLines() != null && l3AS403.getOrderLines().size()>0){
		  result.add(l3AS403);  
	  }
	  if(l3BF004.getOrderLines() != null && l3BF004.getOrderLines().size()>0){
		  result.add(l3BF004);  
	  }
	  if(l3PS404.getOrderLines() != null && l3PS404.getOrderLines().size()>0){
		  result.add(l3PS404);  
	  }
	  if(l3TS400.getOrderLines() != null && l3TS400.getOrderLines().size()>0){
		  result.add(l3TS400);  
	  }
	  
	 System.out.println("Result =" +order.getDokNr()
			 + " "+ result.size());

	 return result ;
  }


  
 
  private String getWeb(String req){
	    String user = "global";
		String pass = "1bbd886460827015e5d605ed44252251"; //"04d248f4452ed6ec977b7ccc1fa49c5d";//04d248f4452ed6ec977b7ccc1fa49c5d
		if(parent.getProps().getProperty("soap.pass") != null) {
			pass = parent.getProps().getProperty("soap.pass");
		}
		
		String authStr = user + ":" + pass;
	
		String authEncoded = Base64.encode(authStr.getBytes()).toString();
	  System.out.println("authStr " + authStr +" "+ authEncoded );
	  Content cnt = null;
	  try{
		  String server =  "http://localhost/Apskaita.skonis/WS/Skonis.svc";
		  cnt = Request.Post(server)
			.bodyString(req, ContentType.create("text/xml", "UTF-8"))
					.addHeader("Authorization", "Basic " + authEncoded)
				.addHeader("SOAPAction", "http://tempuri.org/IService/ss_Pardavimas")
			.execute().returnContent();
	  }catch(Exception e){
		  e.printStackTrace();
		  return e.getMessage();
	  }
	  return cnt.asString().replaceAll("qdoc:", "");
	 	
  }

  

  public String identifyWarehouse(String prod,String agent) throws SQLException {
	  	String sql1 = null;
	  	
	  	 System.out.println("Lao M��ramine: " );
	  	 
	  	//toote grupi m��ramine
	  	    sql1 = "SELECT left(t.GRP_KOD,2) AS grupp  FROM dba.[son] t WHERE  t.KOD = '" + prod + "'";	
		
	    System.out.println(sql1);
	    String ladu = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);
       String grupp = "";
	    while (rs1.next()) {
	      grupp = rs1.getString("grupp").trim() ;
	    }
	  if(grupp.length() > 0){
		  grupp = grupp.substring(0,1); 
	  }
	  
	 // LTSPAMAL004
       if(grupp.equals("T")){// toode on tubakas
    	   if(agent.substring(0,1).equals("0")){ //vilniuse agent
    		   ladu =    "0TS100";
    	   }
    	   if(agent.substring(0,1).equals("1")){ //Kaunase agent
    		   ladu =    "1TS200";
    	   }
    	   if(agent.substring(0,1).equals("2")){ //Klaipeda agent
    		   ladu =    "2TS300";
    	   }
    	   if(agent.substring(0,1).equals("3")){ //Panevezys agent
    		   ladu =    "3TS400";
    	   }
    	   
       }
	    
       

       if(grupp.equals("A")){// toode on tee
    	   if(agent.substring(0,1).equals("0")){ //vilniuse agent
    		   ladu =    "0AS101";
    	   }
    	   if(agent.substring(0,1).equals("1")){ //Kaunase agent
    		   ladu =    "1AS203";
    	   }
    	   if(agent.substring(0,1).equals("2")){ //Klaipeda agent
    		   ladu =    "2AS303";
    	   }
    	   if(agent.substring(0,1).equals("3")){ //Panevezys agent
    		   ladu =    "3AS403";
    	   }	   
       }
       
       

       if(grupp.equals("P")){// toode on aroomid
    	   if(agent.substring(0,1).equals("0")){ //vilniuse agent
    		   ladu =    "0PS119";
    	   }
    	   if(agent.substring(0,1).equals("1")){ //Kaunase agent
    		   ladu =    "1PS210";
    	   }
    	   if(agent.substring(0,1).equals("2")){ //Klaipeda agent
    		   ladu =    "2PS308";
    	   }
    	   if(agent.substring(0,1).equals("3")){ //Panevezys agent
    		   ladu =    "3PS404";
    	   }	   
       }
       
       

       if(grupp.equals("M")){// toode on aroomid
    	   if(agent.substring(0,1).equals("0")){ //vilniuse agent
    		   ladu =    "4XX002";
    	   }
    	   if(agent.substring(0,1).equals("1")){ //Kaunase agent
    		   ladu =    "1AS203";
    	   }
    	   if(agent.substring(0,1).equals("2")){ //Klaipeda agent
    		   ladu =    "2AS303";
    	   }
    	   if(agent.substring(0,1).equals("3")){ //Panevezys agent
    		   ladu =    "3AS403";
    	   }	   
       }
       
	    rs1.close();
	    st1.close();

	    //kui on vilniuse agent ja jõulu toode siis saadame jõulu lattu. 
	    
	    if(agent.substring(0,1).equals("0")){ //vilniuse agent
	    	  sql1 = "SELECT left(t.GRP_KOD,2) AS grupp  FROM dba.[son] t WHERE  t.KOD = '" + prod + "' and sutart = 'KALEDOS'";	
	  		
	  	    System.out.println(sql1);

	  	     st1 = con.createStatement();
	  	     rs1 = st1.executeQuery(sql1);
	         
	  	    if (rs1.next()) {
	  	      ladu = "0AS132";
	  	    System.out.println("Opaa Leitud ladu: " + ladu + " "  + prod + " " + agent);
	  	    }
	  	    rs1.close();
		    st1.close();
 	   }
	    
	    
	    return ladu;

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
  
  
  private Order order = null;
  private void setOrder(Order order){
	    this.order = order;
	  }

  
  public void makeOrder(Order order) throws SQLException{
	  

	  
    		
	  counter = 0;
	//  makeOutput(order);
   
    		   Vector splitted =  splitOrder(order);
    		   System.out.println("tellimusi ladudesse " + splitted.size());
    	         Enumeration en = splitted.elements();
    			  while(en.hasMoreElements()){
    				  Order ord = (Order)en.nextElement(); 
    				  makeOutput(ord); 
    			  }
    		
    		
    		
  }

  private  int counter = 0;
 private void makeOutput(Order order ){
	  
	  
	 	StringBuffer sb = new StringBuffer();
	 	try { 	    
			String ref = order.getReference();
			if ( ref == null ){
				ref =  " ";
			}
			
			String com = order.getInfo();
			if ( com == null ){
				com =  " ";
			}		
		
			String ware = order.getWarehouse();
			if ( ware == null ){
				ware = " ";
			}
	         
    
	         String rrr = " ";
	         if(order.getDokNr() != null &&order.getDokNr().trim().length()!= 0){
	        	 rrr = order.getDokNr();
	        	 if(rrr.length() > 10){
	        		 rrr = rrr.substring(0,10);
	        	 }
	         }
	        
	         String agent =  order.getAgent();
             
	      
	         Calendar cal = Calendar.getInstance();
	           if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
	        	   cal.add(Calendar.DAY_OF_YEAR, 3);  
	           }else{
	        	   cal.add(Calendar.DAY_OF_YEAR, 1);  
	           }
	           
	          
	           if (order.getShipCal() != null) {
	                 cal = order.getShipCal();
	               }
	         
	         
	        sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\" " +
	        		"xmlns:lab=\"http://schemas.datacontract.org/2004/07/Labbis.Apskaita.Extensions.SK.WCF.ss_Pardavimas\"> "+
     " <soapenv:Header/> "+
      " <soapenv:Body>"+
      "   <tem:ss_Pardavimas>"+
      "     <tem:username>global</tem:username>"+
      "    <tem:password>1bbd886460827015e5d605ed44252251</tem:password>");
	        
	        String number = order.getDokNr();
              int sees = 0;
              sb.append("<tem:pardavimas>");
	          sb.append("<lab:APM_IKI>" + this.getDateString(cal)+ "</lab:APM_IKI>");
	          sb.append("<lab:DOKNR1>" + number + "</lab:DOKNR1>");
	    	  sb.append("<lab:MOK>" + order.getCompany() + "</lab:MOK>");
	    	  sb.append("<lab:Prekes>");
	    	  String ladu = "";
	    	  
	        Vector lines = order.getOrderLines();
	      for (int i = 0; i < lines.size(); i++) {
	    	  OrderLine ol = (OrderLine) lines.elementAt(i);
        	  String unit = ol.getUnit();
	          double kogus = ol.getKogus();
	       double disc = ol.getDiscount();
	          if(disc == -2){
	        	 disc = 100; 
	          }
	          
	          if (disc < 0){
	            disc = 0;
	          }
	          
               double price = ol.getPrice();
               DecimalFormat df2 = new DecimalFormat("####.##");
               price =  Double.valueOf(df2.format(price).replace(',', '.'));
               
               if(disc == 100){
            	   price = 0;
               }
        
               sees = sees + 1;
	    	  
            
               sb.append("<lab:Preke>");
               sb.append("<lab:ATSAK>" + agent + "</lab:ATSAK>");
               sb.append("<lab:IVYKDYMO_DATA>" + this.getDateString(cal) + "</lab:IVYKDYMO_DATA>");
 	          
               
               sb.append("<lab:KIEK>" + kogus + "</lab:KIEK>");
	        	if(disc > 0){
		    	       sb.append("<lab:NUOL_PROC>" + disc + "</lab:NUOL_PROC>");
		    	    }else{
		    	        sb.append("<lab:NUOL_PROC>0</lab:NUOL_PROC>");
				    	    	
		    	    }
                 if(com.trim().length() > 2){
		        	    sb.append("<lab:PAST>" + com + "</lab:PAST>");
				  }else{
					  sb.append("<lab:PAST> </lab:PAST>");
						 	  
				  }
			       sb.append("<lab:PREKE>"+ ol.getToode() + "</lab:PREKE>");
		    	   
		    	   
		    	   sb.append("</lab:Preke>");
		 	    	  
		    	   
		       ladu = identifyWarehouse(ol.getToode(),agent);
		       if(ladu == null){
		    	   ladu = "";
		       }
		       if(orderware != null) {
					ladu = orderware;
				}
counter++;
}
	      String fn = number;
	      if(number.trim().length()== 0){
	    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
	      }
	      sb.append("</lab:Prekes><lab:SAUGO>" + ladu  + "</lab:SAUGO></tem:pardavimas>");	
	    
	          
	      sb.append("</tem:ss_Pardavimas></soapenv:Body></soapenv:Envelope>");
	      if (testing){
	    	  System.out.println(sb.toString());
	    	 String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + fn  +  "-response.xml";
		      System.out.println(fileName);
		      Converter.saveFileInUTF8(fileName, sb.toString());
		 	
	      }else{
	    	  System.out.println( "sees:" + sees + " " + sb.toString());
	   if(sees > 0 ){
		
	   
	   
	 	try {
	 		
	 		  
		      
		      String fileName = (parent.getProps().getProperty("conv.dir")) +  "t-" + fn  +  ".xml";
		      System.out.println(fileName);
		       
		      Converter.saveFileInUTF8(escape(fileName), sb.toString());
		      
	 	
	 		String xml = getWeb(sb.toString());		
	 		System.out.println("Tulemus " + xml);
	 		
      setNumAddedLines(counter); 
	      fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + fn  +  "-response.xml";
	      System.out.println(fileName);
	      Converter.saveFileInUTF8(fileName, xml);
	 	} catch (Exception e) {
	 		e.printStackTrace();
	 	}
     }
	      }
	      setNumAddedLines(counter); 
	   

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

 private String getDateString(Calendar cal){//2012-06-05
   StringBuffer sb = new StringBuffer();
   sb.append(cal.get(Calendar.YEAR) + "-" );
   String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
   if (month.length() == 1){
     sb.append("0");
   }
   sb.append( month + "-");

   String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
   if (day.length() == 1){
     sb.append("0");
   }
   sb.append(day);

   return sb.toString();
 }
 

  
  
  private void addInvoiceLines(String salesid,Order ord) throws SQLException{

	    Enumeration enumr = ord.getOrderLines().elements();
	    int counter = 0;
	    while (enumr.hasMoreElements()){
	      counter++;
	      log.info("ORDER_LINE_COUNTER:" + counter);
	      OrderLine orderLine = (OrderLine)enumr.nextElement();
	      String itemid = orderLine.getToode();
	      double kogus = orderLine.getKogus();

	      String sql0 = "  SELECT t.KOD AS toode, t.PAV AS nimetus " +  
			" ,t.VNT AS unit FROM dba.[son] t   WHERE   t.KOD='" + itemid + "'  "; 

	      
	    log.info("SALES_LINE_SQL: " + sql0);
	    System.out.println(sql0);
	    Statement st = con4.createStatement();
	    ResultSet rs = st.executeQuery(sql0);
	   boolean olemas = false;
	    String unit = " ";
	    String name = "";
	    while(rs.next()){
	       name = rs.getString("nimetus"); 
	    	unit = rs.getString("unit");

	      if (unit !=null && unit.trim().length()>0){
	      	unit = unit.trim();
	      	olemas = true;
	      }else {
	      	unit = " ";
	      }
	    }
	  if(orderLine.getUnit().trim().length()>0){
	    unit = orderLine.getUnit();
	  }
	  System.out.println("olemas " + olemas);
	  if(!olemas){
	  	itemid = orderLine.getTooteNimi();
	  	name = orderLine.getEAN() + " " + orderLine.getTooteNimi();
	  	if(itemid.length()>20){
	  		itemid = itemid.substring(0,19);
	  	}
	  }
	          
   double price = -1;
   double discount = -1;
   if(orderLine.getDiscount() != 0){
	   price = orderLine.getPrice();
   }
	          
	          
	          String sql =" INSERT into  ms_orderlines (DOCID, LINENUM,PRODID,PRODNAME, AMOUNT, UNIT ,PRICE,DISCOUNT )  " +
	   " values('"+salesid+"'," + counter + ",'"+itemid+"','" + name + "',"+kogus+",'" + unit + "'," + price +"," + discount+")";

	              log.info("SALES_LINE_SQL: " + sql);
	              System.out.println(sql);
	              PreparedStatement st2 = con2.prepareStatement(sql);
	              st2.executeUpdate();
	              addNumLine();
	              st2.close();

	              }
	  }

  

  

  public static String getYYYYpMMpDDpTimeNiceString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}

		sb.append("" + cal.get(Calendar.YEAR));
		sb.append("." + month  + ".");
		sb.append(day +" ");
	    
	    return sb.toString();
	  }


  
  private void addInvoiceHeader(String salesid, Order ord ) throws SQLException{

    

         String agent = ord.getAgent();
         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);

         if(ag == null){
         	ag = new Agent(agent);
         }

     String tekst = ord.getInfo();
     if(tekst == null){
    	 tekst = "";
     }
     String mode = ord.getShipMode();
     if(mode == null){
    	 mode = "";
     }
      String ref = "";

      if(ord.getReference() != null && ord.getReference().trim().length()>0 ){
      	ref = ord.getReference().trim();
      }

      String custname = "";
      if( ord.getCompName() != null){
    	  custname = ord.getCompName();
      }
      
      if (ord.getShipTime()!= null){
         custname  =  ord.getShipTime().replaceAll("\n"," ") + " " + getYYYYpMMpDDpTimeNiceString(ord.getShipCal()) + " " + custname;
        }
      String cust =  ord.getCompany();
     
      
 String dea = ord.getDealer();
 if(dea.equals("NODEALER")){
	 dea = "";
	 }
      
      System.out.println(salesid + "|"+ ord.getCompany() +"|"+ ag.getID()+ "|"+ tekst);
     String sql = " INSERT INTO ms_orderheader (  " +
            " DOCID,DOCNUMBER,REFERENCE,CUSTID,SALESPERSON,WAREHOUSE,COMMENT,ORDERDATE,DELIVERYDATE,ISREADYFORLABBIS) " +
            " VALUES ('"+salesid+"','" + ord.getDokNr() + "','" + ref + "','"
            + cust+"','"+ag.getID()+"','" + dea +"','"+tekst+"',?,?,0)" ;
        log.info("addInvoiceHeader2: " + sql);
        System.out.println(sql);
        
        // escape(cust.substring(0,cust.indexOf("-")))
        PreparedStatement stmt2 = con2.prepareStatement(sql);
        java.util.Date dat = new java.util.Date();
       // System.out.println(dat.toString() + "  sc " + order.getShipCal().getTime().toString() + " ref " + ref);
            dat.setTime(ord.getShipCal().getTimeInMillis());
        Calendar cc = Calendar.getInstance();
        stmt2.setTimestamp(1, new java.sql.Timestamp(cc.getTimeInMillis()));
        stmt2.setTimestamp(2, new java.sql.Timestamp(dat.getTime()));
        stmt2.executeUpdate();
        stmt2.close();
    
        addLog("lisatud arve p�is -> " + ord.getCompany() + "\n");
  }



  
  private void addLog(String line){
	    parent.addLog(line);
	  }
 	  


 
	public NumberFormat getNF() {
		return this.nf;
	}
	



 
  public String getDriverVersion(){
    return "LabbisSkonisImport";
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
  
  private String url = "jdbc:odbc:merisoft";
  private String url2 = "jdbc:odbc:import";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private Connection con2;
  private Connection con4;
  private Connection con3;
  private ImportPanel parent;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
