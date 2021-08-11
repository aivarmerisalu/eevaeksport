package eevaeksport.imp.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.Product;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class MVtxtImportDriver extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public MVtxtImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    initConversions();
  }

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1  = "SELECT yksus AS kood FROM yksus where nimetus like  '%\"" + id + "\"%'";	
    System.out.println(sql1);
    String acc = null;
   Iterator  ite  = this.tmcustname.keySet().iterator();
   while(ite.hasNext()){
	   String name = (String)ite.next();
	   if(name.indexOf(id)>-1){
		   acc = (String) tmcustname.get(name);
	   }
   }
   

    System.out.println("Klient: " + acc);
    return acc;

  }

  public String identifyProduct(String ean, String prodID) throws SQLException{

    String prod = null;
    
    if(prodID != null && tmid.containsKey(prodID)){
    	prod = prodID;
    }
    
    if(prod == null && ean != null  && tmean.get(ean) != null){
    	prod = (String)tmean.get(ean);
    }
    System.out.println("prod1 " + prod);

    return prod;
  }

 
  public void makeOrder(Order order){
 
    setOrder(order);
    addInvoiceLines();
  }
  
  public TreeMap getCustomers() {

		TreeMap tm = new TreeMap();
		String file = CUSTOMER_FILE;
		

			try {


				FileReader fr = new FileReader(getDataDir() + file);
				BufferedReader reader = new BufferedReader(fr);
				String line = null;

			
			
				
				while ((line = reader.readLine()) != null) {
					//System.out.println(line);

					line = line.replaceAll("\"\"","\" \"");
					//System.out.println(line);
					line = line.replaceAll("\";","|");
					line = line.replaceAll(";\"","|");
					line = line.replaceAll("\"","");

					
					StringTokenizer st = new StringTokenizer(line, "|");
			//	System.out.println(line);
					
					if (st.hasMoreTokens()) {
						String id = st.nextToken().trim();
						Customer customer = new Customer(id);
						customer.setAddress(" ");

						if (st.hasMoreTokens()) {
							String payer = st.nextToken().trim();
							customer.setPayer(payer);
						}

						
						Agent age = null;
						String agen = st.nextToken().trim();
						
							age = new Agent(agen);
						

						//System.out.println(customer.getID());

						//System.out.println(age);
						if (st.hasMoreTokens()) {

							String name = st.nextToken().trim().replaceAll(";", "");

							Customer cust = (Customer) tm.get(name);
							if (cust != null) {

								//System.out.println(age.getID() + " "
									//	+ cust.getName());
								cust.addAgent(age);
								tm.put(cust.getName(), cust);

							} else {

							//System.out.println(age.getID() + "  else " );
								if (name.equals(""))
									name = "<" + id + ">";
								customer.setName(name);
								if (st.hasMoreTokens()) {
									String aadress = st.nextToken().trim().replaceAll(";", "");
									if (aadress != null) {
										customer.setAddress(aadress);
										customer.setName(customer.getName() + " " + aadress);
									}
								}

								/*if (st.hasMoreTokens()) {
									customer.setDebt(Double.parseDouble(st
											.nextToken().trim()));
								}*/

								if (st.hasMoreTokens()) {
									try{

										customer.setDiscount(Double.parseDouble(st
												.nextToken().trim()));	
									}catch(Exception ee){
										
									}
								}

								customer.addAgent(age);
							
								
								tmcustname.put(customer.getName(), customer.getID());
						          
							}
						}

					}
				}
				
                 fr.close();
                 reader.close();
		

			} catch (IOException e) {
				e.printStackTrace();
			}
	

		//System.exit(0);
		return tm;
	}
  
  private void initConversions(){
	  try{
	  this.getCustomers();
		   
		    String file = "Products.txt";
		    	try {
					File fil = new File(getDataDir() + file);
					FileReader fr = new FileReader(fil);
					BufferedReader reader = new BufferedReader(fr);
					String line = null;
					while ((line = reader.readLine()) != null) {
						
						line = line.replaceAll("\"\"","\" \"");
						line = line.replaceAll("\";","|");
						line = line.replaceAll(";\"","|");
						line = line.replaceAll("\"","");
						line = line.replaceAll(";;;","; ; ;");
						line = line.replaceAll(";;","; ;");
						line = line.replaceAll(";","|");
	
						StringTokenizer st = new StringTokenizer(line, "|");					
						if (st.hasMoreTokens()) {
							String id = st.nextToken().trim();
							Product prod = new Product(id);
							
							if (st.hasMoreTokens()) {

								
								 	prod.setName(st.nextToken().trim().replaceAll(";", ""));
								 	try{
								 	 	double inCases = Double.parseDouble(st
												.nextToken().trim());
										if (inCases > 0) 
											prod.setInCases(inCases);
									  		
								 	}catch(Exception eee){
								 		
								 	}
								 	
								 	if (st.hasMoreTokens()) {
										String gr = st.nextToken()
												.trim();
										if (gr.length() > 0) {
											prod.setGroup(gr);
										}
									}
								 	
								 	if (st.hasMoreTokens()) {
										String gr = st.nextToken()
												.trim();
										if (gr.length() > 0) {
											prod.setGroupSecond(gr);
										}
									}
								 	try{
									double price = Double.parseDouble(st
											.nextToken().trim());
									prod.setPrice(price);
							       }catch(Exception eee){
						 		
						 	        }
						 	
								
												String ean = st.nextToken().trim();
												if (ean.length() > 0) {
													prod.setEAN(ean);

												}
												

												//System.out.println(line);
												//System.out.println(prod.getEAN());
							try{
													prod.setStock(Double.parseDouble(st.nextToken().trim()));
							}catch(Exception eee){
						 		
				 	        }
							try{
								st.nextToken();
								if (st.hasMoreTokens()) {
									String unit = st
											.nextToken();
									if (unit != null
											&& unit.trim()
													.length() > 0) {
										prod.setUnit(unit
												.trim());
									}
								}
							}catch(Exception e){
								
							}
												

													if(prod.getName().trim().length()>0){
														tmid.put(prod.getID(), prod.getEAN());
														tmean.put(prod.getEAN(), prod.getID());
													}
													
												}
											}
						
						  //   System.out.println("tm size " + tm.size());
						
										}
									
								
							
						
					

					reader.close();
					fr.close();
					//fil.renameTo(new File(getMoveDir() + file));

				} catch (Exception e) {
					e.printStackTrace();
				}
		  
				

		    
		    
	  }catch (Exception e){
		  e.printStackTrace();
	  }
	   
  }



  private void addInvoiceLines(){
    try{
     StringBuffer buf = new StringBuffer();
     String timefull = System.currentTimeMillis() + "";
   
     String cust = order.getCompany();
     if (order.getDokNr()!= null && order.getDokNr().trim().length() >0){
    	 timefull = timefull.substring(10,13) +  order.getDokNr();   	 
     }
     String filename = order.getAgent()+ "." + timefull;
   
     String  agg = order.getAgent() ;
     
     Calendar cal = Calendar.getInstance();
     if(order.getShipCal() != null){
    	 cal.setTime(order.getShipCal().getTime());	 
     }
    
     String shipdate = getDateNiceString(cal);

    Enumeration enumr = order.getOrderLines().elements();
    int counter = 0;
    while (enumr.hasMoreElements()){
      counter++;
      log.info("ORDER_LINE_COUNTER:" + counter);
      OrderLine orderLine = (OrderLine)enumr.nextElement();
      String itemid = orderLine.getToode();
      double kogus = orderLine.getKogus();


          double pr = 0;       
        buf.append(cust + ";" + itemid + ";" + kogus+ ";0; ;0;0;" + timefull+ ";0;" + pr + ";0;" + agg + ";0;" + orderLine.getUnit() + ";"
        		   + shipdate + "; ; ; ;" + timefull +  System.getProperty("line.separator"));
          setNumAddedLines(getNumAddedLines() + 1);
    }

    
    
    if(buf.length() > 2){
        File f = new File(getDir() + filename +".txt");
        if (f.exists()){
        	filename = filename + "x";
        }
          try {
    		PrintWriter os = new PrintWriter(new FileWriter(
    				getDir() + filename + ".txt", true));
    		os.write(buf.toString());
    		os.close();
    	  }catch (IOException ex) {
    		ex.printStackTrace();
    	  }
     }
    
    } catch (Exception e){
       parent.showError(e);
    }
  }







  public static double getPrice(double kogus, double percent, double price){
    double value = kogus * price*((100+percent)/100);
    value = (Math.round((value*100d)))/100d;
    return value;
  }

  public String getDriverVersion(){
    return "MVtxt";
  }

  private void setOrder(Order order){
    this.order = order;
  }

  private String getDir(){
	    String dir = parent.getProps().getProperty("writeout");
	    if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	    return dir;
	  }
  private String getDataDir(){
	    String dir = parent.getProps().getProperty("readin");
	    if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	    return dir;
	  }

  
  private String getDateNiceString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    String year =   cal.get(Calendar.YEAR) + "";
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	     if (day.length() == 1){
	      sb.append("0");
	    }
	     sb.append(day);
	     sb.append(".");
	    if (month.length() == 1){
	     sb.append("0");
	   }
	     sb.append(month );
	    sb.append( "." + year);
	    return sb.toString();
	  }
	
  
  


  private HashMap tmcustname =  new HashMap();
	public static final String CUSTOMER_FILE = "customers.txt";
  private HashMap tmid =  new HashMap();
  private HashMap tmean =  new HashMap();
  
  private ImportPanel parent;
  private Order order = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}