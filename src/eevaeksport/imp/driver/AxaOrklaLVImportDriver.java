
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.EksportFrame;
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

public class AxaOrklaLVImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public AxaOrklaLVImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
   
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);


  	nf.setMaximumFractionDigits(2);
	nf.setMinimumFractionDigits(2);
	nf.setGroupingUsed(false);
    
	nf2.setMaximumFractionDigits(4);
	nf2.setMinimumFractionDigits(2);
	nf2.setGroupingUsed(false);
   
	
    }
    catch (Exception e) {
      parent.showError(e);
    }
    
    loadSignatures();
  }

  
  
	private Vector signatures = new Vector();

	private void loadSignatures(){
		
try{		
		File f = new File("signatures.vec");
		Calendar ss = Calendar.getInstance();
		
		if (f.exists() && f.lastModified() < ss.getTimeInMillis() - 3600000) {// kui on yle tunni vana siis maha 
			f.delete();
			signatures = new Vector();
		} else {
			signatures = (Vector) readObject("signatures.vec");
		}

		
	} catch (Exception e) {
		signatures = new Vector();
	}

	}

		
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



  public String identifyCustomer(String id,String regnr) throws SQLException {
  return id;
	  }



  
  private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	  }

	  private Connection getNewConnection2() throws SQLException{
		    return DriverManager.getConnection(url2, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
		  }
	  
 
 
  
 
	  public String identifyProduct(String ean, String prodID) throws SQLException{
		    return prodID;
		  }


	public void writeConsloStr(String path,String e) {
		
		try {
			File f = new File(path);
			if(!f.exists()){
				  try{
				      FileOutputStream fos = new FileOutputStream(path);
				        fos.write("".getBytes());
				        fos.close();
				
			} catch (Exception uu) {
				uu.printStackTrace();
			}
			}
			PrintWriter os = new PrintWriter(new FileWriter(
					path, true));
			os.write(e);
			os.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
}

	private String trouble = null;
  
  public void makeOrder(Order order) throws SQLException{

	  counter = 0;
	  
	  if (signatures == null){
   	   signatures = new Vector();
      }
	  
	  String signature = order.getSignature();
	  if(signatures.contains(signature)){
		  this.trouble = "REPRETEATING ORDER agent :" + order.getAgent() + " customer :" 
	  + Converter.deCode(order.getCompany()) ;
	
	  } else {
		  
	 Vector vv = this.splitOrder(order);
    
	  for(int k = 0; k<vv.size();k++) {
    	   
	 	  Order ord = (Order)vv.elementAt(k);
	 		 boolean salvesta = true;
      	 	 Vector lin = ord.getOrderLines();
      	 	 boolean laks = false;
      	 	 for (int i = 0; i < lin.size(); i++) {
      	 		OrderLine ol = (OrderLine) lin.elementAt(i);
      	 		if(ol.getKogus() < 0 && ol.getDiscount() > 0 && (ord.getStatus() == 5 || ord.getStatus() == 4)){
      	 			laks = true;
      	 		}
      	 	 }  
      		    if(laks){
      		    	salvesta = false;
      		    }
      		    
     if(salvesta){
      	  		 addlocks(false,false);
    	 	     makeOutput(ord); 
	 		String lineSep = System.getProperty("line.separator");
  		  		 writeConsloStr(parent.getProps().getProperty("conv.dir") + "index.csv",lineSep + fileName + ",0,,," + lineSep);
       			 addlocks(true,false);
			 
      	 }else{
      		 
      		// teeme teise outputi 
	 	      addlocks2(false,false);
	 		  makeOutput2(ord);
      		  String lineSep = System.getProperty("line.separator");
  		  	  writeConsloStr(parent.getProps().getProperty("conv.dir3") + "index.csv",lineSep + fileName + ",0,,," + lineSep);
       		  addlocks2(true,false);
	 		  setNumAddedLines(lin.size()); 
 	    	  System.out.println("Ei Salvesta kuna neagtiivne on tell " +fileName + " " + lin.size());
      	    }
      		
	 	 signatures.add(signature);
      	}   
      }

	  try {
			saveObject(signatures, "signatures.vec");
		} catch (IOException e) {
			e.printStackTrace();
		}
	  
	  setNumAddedLines(counter);
  
  }

  public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
 
  
  private String fileName = null;
  
  private void addlocks(boolean delete , boolean kondit){
	 
  	String fileNam = parent.getProps().getProperty("conv.dir") + "lock"; 
	  if(kondit){
		  fileNam = parent.getProps().getProperty("conv.dir2") + "lock"; 
	  }
	  File lck = new File(fileNam);
  	
	  if(delete){
		 lck.delete(); 
	  }else{
		
	  if(lck.exists()){
		  System.out.println("LOCK Exists " + fileNam);
		  log.error(" LOCK Exists :" + new Date(lck.lastModified()).toGMTString());
		  System.exit(0);
	  }else {
		  try{
	      FileOutputStream fos = new FileOutputStream(fileNam);
	        fos.write("".getBytes());
	        fos.close();
	        log.error(" LOCK Created :" + new Date(lck.lastModified()).toGMTString());
			} catch (Exception e) {
				e.printStackTrace();
			}	  
		  }
	  
	  }
	  
	  }
  
  
  private void addlocks2(boolean delete , boolean kondit){
		 
	  	String fileNam = parent.getProps().getProperty("conv.dir3") + "lock"; 
		  if(kondit){
			  fileNam = parent.getProps().getProperty("conv.dir4") + "lock"; 
		  }
		  File lck = new File(fileNam);
	  	
		  if(delete){
			 lck.delete(); 
		  }else{
			
		  if(lck.exists()){
			  System.out.println("LOCK Exists " + fileNam);
			  log.error(" LOCK Exists :" + new Date(lck.lastModified()).toGMTString());
			  System.exit(0);
		  }else {
			  try{
		      FileOutputStream fos = new FileOutputStream(fileNam);
		        fos.write("".getBytes());
		        fos.close();
		        log.error(" LOCK Created :" + new Date(lck.lastModified()).toGMTString());
				} catch (Exception e) {
					e.printStackTrace();
				}	  
			  }
		  
		  }
		  
		  }
	  
  
  
  
    private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day+"." );
	    
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append( month + ".");
	    
	    sb.append(cal.get(Calendar.YEAR) + "");
		    return sb.toString();
	  }
	  
  
  
  private Vector splitOrder (Order order){
			  
		  Vector result = new Vector();

		  if (order.getDokNr() == null || order.getDokNr().trim().length() == 0){
			  order.setDokNr((Calendar.getInstance().getTimeInMillis() + "").substring(3));
		  }
		  System.out.println("Infokene " + order.getInfo() + " " + order.getDokNr());

		  Order ordorkla = new Order(order.getCompany(),order.getAgentFirstName());
		  Order ordorklabonus = new Order(order.getCompany(),order.getAgentFirstName());
		 
		  
		  
		  ordorkla.setAgentCode(order.getAgentCode());
		  ordorkla.setDokNr(order.getDokNr());
		  ordorkla.setCompName(order.getShipMode());
		  ordorkla.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordorkla.setStatus(order.getStatus()); 
		  ordorkla.setInfo(order.getInfo());
		  ordorkla.setReference(order.getReference());
		  ordorkla.setTeostaja(order.getTeostaja());
		  ordorkla.setWarehouse(order.getWarehouse());
		  ordorkla.setDealer(order.getDealer());

		  
		  ordorklabonus.setAgentCode(order.getAgentCode());
		  ordorklabonus.setDokNr(order.getDokNr()+"B");
		  ordorklabonus.setCompName(order.getShipMode());
		  ordorklabonus.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordorklabonus.setStatus(order.getStatus()); 
		  ordorklabonus.setInfo(order.getInfo());
		  ordorklabonus.setReference(order.getReference());
		  ordorklabonus.setTeostaja(order.getTeostaja());
		  ordorklabonus.setWarehouse(order.getWarehouse());
		  ordorklabonus.setDealer(order.getDealer());



		  
	
		  
		  Vector lines = order.getOrderLines();
		  
		  
		  Enumeration enumr = lines.elements();
		  while  (enumr.hasMoreElements()){
			  OrderLine lin = (OrderLine)enumr.nextElement();
			    	
			    	if(lin.getDiscount() == -2) {
			    	
			    		ordorklabonus.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			    		ordorklabonus.setDokNr(order.getDokNr());
			    		ordorklabonus.setReference(order.getReference());
				     	
			    		
			    	}else {
			    		ordorkla.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
				     	ordorkla.setDokNr(order.getDokNr());
				     	ordorkla.setReference(order.getReference());
			    	}
			  
		  }
		  
		  if(ordorkla.getOrderLines() != null && ordorkla.getOrderLines().size()>0){
			  result.add(ordorkla);  
		  }
		  
		  if(ordorklabonus.getOrderLines() != null && ordorklabonus.getOrderLines().size()>0){
			  result.add(ordorklabonus);  
		  }
	      
		  
		 System.out.println("Result =" +order.getDokNr()
				 + " "+ result.size());
		 //System.exit(0);
		  return result ;
	  }


  

  
  
  private void makeOutput2(Order order){
	 	StringBuffer sb = new StringBuffer();
	 	
	 	try {
	 	    String doknr = order.getDokNr();
		      String fn = doknr;
		      String mills = (Calendar.getInstance().getTimeInMillis()+"").substring(10);
		      if(doknr.trim().length()== 0){
		    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
		      }
		      fn = fn +"-" + mills;
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
			if(ref.endsWith("null")){
				ref = ref.substring(0, ref.length()-4);
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
	         String sep = ",";
	         String lineSep = System.getProperty("line.separator");
	         String klient = order.getCompany();
	         String dim = "olv";
	         if(klient.startsWith("con-")){
	        	 klient = klient.substring(4);
	        	 dim = "k12";
	         }
	         
	    Vector lines = order.getOrderLines();
	      for (int i = 0; i < lines.size(); i++) {
	        OrderLine ol = (OrderLine) lines.elementAt(i);
	//  "LV0197-005_NP_120216","","MPAULICANS","LV0197-005","H180100700","4.0","45.0","374054","12.02.2016","7.1","3.905","2kg",

	        String toode = ol.getToode();
	         if(toode.startsWith("con-")){
	        	 toode = toode.substring(4);
	         }
	         String disc = "0";
		        double disco = 0;
		        if(ol.getDiscount()> 0 && ol.getDiscount() < 99){
		        	disc = ol.getDiscount()+"";
		        	disco = ol.getDiscount();
		        }
		        double jaga = 1;
			     
			     String jagaja = "1";
			     
			     if(ol.getPartii() != null && ol.getPartii().indexOf("|") != -1){
			        	jagaja = (ol.getPartii().substring(ol.getPartii().indexOf("|")+1).trim());
			        }
			    
	// kui hind oli tk oma siis ei jaga midagi 
			     boolean pcprice = false;
			    
			     if(ol.getPl() == 9 ){
			    	 Product prod = getProduct(toode, true);
			    	 if(prod.getPrice() > 0) {
			    		 double jjj = Double.parseDouble(jagaja);
				    	 if(jjj > 1 && ol.getPrice()* Double.parseDouble(jagaja) * 0.6 < prod.getPrice()){
				    		 //juhul kui ol hind korda sisukogus on suuerm toote hinnast siis 
				    		 //polnud sisestatud yhiku hind 
				    		 pcprice = true;
				    	 }	 
			    	 }
			    	
			     }
			     
			   
			     
			    if( !pcprice ){
			       	jaga = Double.parseDouble(jagaja);
				}
		        
		        
		        
		    	ref = ref.trim();
				
				if(ref.indexOf(" ") > -1){
					ref = ref.substring(0,ref.indexOf(" "));
				}
		    
				  double sum = Double.parseDouble(nf.format(ol.getPrice()/jaga * (ol.getKogus() *-1) + 0.001));
                  double alesum = Double.parseDouble(nf.format(((ol.getPrice()*(1-disco/100))/jaga) * (ol.getKogus() *-1) + 0.001));
					double diff = sum-alesum;
	        //sb.append("\""+ klient+  "_MS_"+ fn + "\""+ sep);
	        sb.append("\""+ ref + "\""+ sep);
	        sb.append("\""+"\"" + sep);
	        sb.append("\""+ agent + "\"" + sep);
	        sb.append("\""+ klient + "\"" + sep);
	        sb.append("\""+toode+"\"" + sep);
	        sb.append("\""+(ol.getKogus()*-1)+"\"" + sep);
	        sb.append("\""+disc+"\"" + sep);
	        sb.append("\""+ref+"\"" + sep); // kysimärk
	        sb.append("\""+ getDateString(Calendar.getInstance())+ "\"" + sep);
	        sb.append("\""+ol.getPrice()/jaga +"\"" + sep);
	        sb.append("\""+((ol.getPrice() - (ol.getPrice()*(disco/100)))/jaga)+ "\"" + sep);
	        sb.append("\""+ol.getUnit()+"\"" + sep);
	        if(order.getWarehouse() != null && order.getWarehouse().equals("br")){
	        	  sb.append("\"GPNPA\"" + sep);
	        }else {
	        	sb.append("" + sep);
	        }
	        sb.append("\""+ diff+"\"" + sep);
          //  sb.append("\""+comm+"\"" + sep);
	        sb.append(lineSep);
	 
	       counter++;
	      }
	      
	      setNumAddedLines(counter); 
	      sendReturnEmail(order);     

   	   fileName =   "R-" + order.getAgent() +"_"+ klient +"_" +fn  + ".csv";
	      
      String fname = (parent.getProps().getProperty("conv.dir4")) + fileName;
			    		
			      if(order.getDealer().equals("orkla")){
			    	  fname = (parent.getProps().getProperty("conv.dir3")) + fileName;
                   }
			      FileOutputStream fos = new FileOutputStream(fname);
			        fos.write(sb.toString().getBytes());
			        fos.close();
	    	

		} catch (Exception e) {
			e.printStackTrace();
		}
	 }


  

	
  
	  private void sendReturnEmail(Order ord){
		  
		  String cmd = "select agentsid as agent , name as nimi from bmssa.traagentstable ag INNER JOIN bmssa.empltable emp ON ag.emplid = emp.emplid  and " +
					" ag.dataareaid =emp.dataareaid  INNER JOIN bmssa.TRAAGENTCUSTTABLE  cu ON  cu.agent = agentsid "
					+ " and cu.dataareaid =emp.dataareaid where ag.dataareaid = 'olv' and agentsid = '" + ord.getAgent() + "'" +
					" group by agentsid,name";

					  Agent agent = null;	   

					  
					  boolean orkla = true;
							 
				    try {
				    
				    	Properties props = new Properties();
						try {
							props = EksportFrame.loadProps("emailret.txt");

						} catch (Exception e) {
							e.printStackTrace();
						}
						
						Connection con = this.getNewConnection();
				    	Statement stmt = con.createStatement();
				    	ResultSet rs = stmt.executeQuery(cmd);
				        while(rs.next()){
				         agent = new Agent(rs.getString("agent").trim());
					          String name = rs.getString("nimi").trim();
					          agent.setName(name);
					          String email = parent.parseFirstEmail(props.get(agent.getID())+"");
					          if(email.length() > 3){
					        	  agent.setEmail(email);
					          }
					    }
				          rs.close();
				          stmt.close();
				          con.close();
				          
				          
				          
				          
				          Customer cust = null;
				        
			  con = this.getNewConnection();
			  stmt = con.createStatement();
		    	 cmd = "SELECT cus.name,cus.accountnum,cus.invoiceaccount AS payer, cus.linedisc , " +
		    		  	 " stbaddressdelivery as address,  email as mail ,stbcustchainid as chain ,regnumber , telex FROM bmssa.custtable cus " + 
		    			 " WHERE cus.dataareaid='olv' and cus.accountnum = '" + ord.getCompany() + "' " ;
		
		  
		
		  System.out.println(cmd);
				    	 rs = stmt.executeQuery(cmd);
				    	 while(rs.next()){
				    		  
				    		 String name = rs.getString("name").trim();
						        cust = new Customer(rs.getString("accountnum").trim());
						        cust.setName(name);
						        cust.setAddress(rs.getString("address").trim());

						        String chain = rs.getString("chain");
						        if(chain != null && chain.length() > 0){
						        	cust.setChain(chain.trim());
                                  }else{
						        	cust.setChain(" ");
						        }
						         
						        
						        
						        String regnumber = rs.getString("regnumber");
						        if(regnumber != null && regnumber.length() > 0){
						        	cust.setRegnr(regnumber.trim());
                                  }else{
						        	cust.setChain(" ");
						        }
						        String telex = rs.getString("telex");
						        if(telex != null && telex.length() > 0){
						        	cust.setCity(telex.trim());
                                  }
						        
						    }
				    	 
				    	 rs.close();
				    	 stmt.close();
				    	 con.close();
				    	 
		  String address = "aivar@mobilejava.ee" ;//agent.getEmail();
		  address =(props.get(ord.getAgent())+"");
	//	  System.out.println("määratud email " + emai + " " + props.toString());
		  if(address != null ){
			  Email email = new Email("Return info : " + cust.getName());
	          email.setFrom(parent.getProps().getProperty("from"));
	          email.setSMTP(parent.getProps().getProperty("smtphost"));
	          email.setUser(parent.getProps().getProperty("smtpuser"));
	          email.setPass(parent.getProps().getProperty("smtppass"));
	          email.setPort(parent.getProps().getProperty("smtpport"));
	          email.setSubjectFilesTo("Return info : " + cust.getName());
	          
	          email.setEncoding("iso-8859-1");
	          try {
	        	  
	        	email.setBodyHtml(getOOSEmailText(ord,cust,agent));
                email.addAttach(createExcelFile(ord,cust,agent,orkla));
	            StringTokenizer st =  new StringTokenizer(address,";");
	            String aadr = null;
	            while(st.hasMoreTokens()){
	            	 aadr = new InternetAddress(st.nextToken()).toString();
	              email.setFilesTo(aadr);
	              email.send();
	              }
	            String aadress2 =  cust.getCity();
	           //  aadress2 =  "aivar@mobilejava.ee";
	            
	            if(aadress2 != null && aadress2.trim().length() >0) {
	            	 st =  new StringTokenizer(aadress2.trim(),";");
	 	            while(st.hasMoreTokens()){
	 	            	String aadr2 = new InternetAddress(st.nextToken()).toString();
	 	            	// email.setFrom(aadr);
	 	              email.setFilesTo(aadr2);
	 	              email.send();
	 	              }	
	            }
	            
	            
			} catch (AddressException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  		  
		  }
				    } catch (Exception e) {
						e.printStackTrace();
					}
					
	  }
	  
	  
				    private String getOOSEmailText( Order ord, Customer cust ,Agent agent) {
						int i = 0;
						int j = 0;
						Vector lines = ord.getOrderLines();
						double total = 0;
						String custname = cust.getName().toString();

						String comp = cust.getName();
						if (comp == null) {
							comp = "";
						}
						
						StringBuffer sb = new StringBuffer();
						String subtext =  "SIA 'Orkla Confectionery & Snacks Latvija' \n &emsp;&emsp;&emsp;&emsp; Miera iela 22, Rīga, LV-1001, Latvija\n";
						String regno = ("&emsp;&emsp;&emsp;&emsp; Reģ.nr. 40103217882");
						if (regno != null) {
							subtext = subtext + " " + regno;
						}
						
						
						Calendar shdate = ord.getShipCal();
						Calendar cal = Calendar.getInstance();
						if (shdate != null) {
							cal.setTime(shdate.getTime());
						} else {
							cal.add(Calendar.DAY_OF_MONTH, 1);
						}
						

						sb.append("<font face=\"Arial\" size=\"2\" color=\"#E42217\"> Date "
								+ Converter.getDDpMMpYYYYpString(null) + "</font><br> ") ;
						sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\"><b>" + agent.getName() + ":</b> " + subtext + "</font>") ;
						sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\">&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;</font><br> ") ;
				        sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\"><b>Klients:</b> " + custname + "</font> ") ;
						sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\">&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;" + cust.getAddress().replaceAll("_", " ").replace('.', ' ').replace('.', ' ').replaceAll("  ", " ").trim() + "</font><br><br>") ;
																		sb.append("</font>");
						
						sb.append("<font face=\"Arial\" size=\"4\"  color=\"#382D2C\"><b>Pārcenot info</b><br><br>" );
																		
						sb.append("	<style type=\"text/css\">");
						sb.append("table.tb-table {");
						sb.append("border: 1px inset #98bf21; font-family: Arial, Helvetica, sans-serif");
						sb.append("font-size: 12px;} ");
						sb.append(".tb-table td {");
						sb.append("	padding: 4px;");
						sb.append("	margin: 3px;");
						sb.append("		border: 1px inset #FF3399;}");
						sb.append(".tb-table th {");
						sb.append("	background-color: #FFF; ");
						sb.append("color: #00000;");
						sb.append("		font-weight: bold;}");
						sb.append("</style><table class=\"tb-table\">" );
						sb.append("<tr><th>Nr.</th><th>EAN</th><th>Kodu</th><th>Apraksts</th><th>Daudzums</th>"
								+ "<th>Atlaide </th><th>Cena €</th><th>Atšķirība</th><th>Kompensācija suma</th></tr>" );
				
						
							for (; i < lines.size(); i++) {
								OrderLine ol = (OrderLine) lines.elementAt(i);

								  String toode = ol.getToode();
								  boolean ork = true;
							         if(toode.startsWith("con-")){
							        	 toode = toode.substring(4);
							        	 ork = false;
							         }
								Product prod = getProduct(toode, ork);
								System.out.println("aaa " + prod + " " + toode);
								String ean = toode ;
								String tnimi = toode;
								if(prod != null) {
									ean = prod.getEAN();
									tnimi = prod.getName();
								}
								
								
						
								
							         String disc = "0";
								        double disco = 0;
								        if(ol.getDiscount()> 0 && ol.getDiscount() < 99){
								        	disc = ol.getDiscount()+"";
								        	disco = ol.getDiscount();
								        }
						     
								     double jaga = 1;
								        if(ol.getPartii() != null && ol.getPartii().indexOf("|") != -1){
								        	jaga = Double.parseDouble(ol.getPartii().substring(ol.getPartii().indexOf("|")+1).trim());
								        }
								        double sum    = Double.parseDouble(nf2.format(ol.getPrice()/jaga + 0.001));
					                    double alesum = Double.parseDouble(nf2.format(((ol.getPrice()*(1-disco/100))/jaga) + 0.001));
										double diff   = sum - alesum;
											   total  = total + diff;
										
								        
								sb.append("<tr><td>" + (i+1) + ".</td><td>" + ean + 
											"</td><td>" + toode + 
											"</td><td>" + tnimi + 
											"</td><td>" + ol.getKogus() *-1+ 
											"</td><td>" + disc + 
											"%</td><td>" + nf2.format( sum ) + 
											"</td><td>" + nf2.format( sum - alesum ) +
											"</td><td>" + nf.format( diff * (ol.getKogus() *-1) ) +
												"</td></tr>" );
							
								j++;
							}

					
							sb.append("<tr><th></th><th>" +
									"</th><th>" + 
									"</th><th>" + 
									"</th><th>" + 
									"</th><th>" + 
									"</th><th>" +  
									"</th><td>Suma:" +
									"</td><td>" + nf.format(total) +
										" €</td></tr>" );
							
							sb.append("<tr><th></th><th>" +
									"</th><th>" + 
									"</th><th>" + 
									"</th><th>" + 
									"</th><th>" + 
									"</th><th>" +  
									"</th><td>Vat:" +
									"</td><td>" + nf.format(total*0.21) +
										" €</td></tr>" );
							
							
							sb.append("<tr><th></th><th>" +
									"</th><th>" + 
									"</th><th>" + 
									"</th><th>" + 
									"</th><th>" + 
									"</th><th>" +  
									"</th><td>Total:" +
									"</td><td>" + nf.format(total*1.21) +
										" €</td></tr>" );
						
						return sb.toString();
					}

				    
				    
				    
	  private String createExcelFile( Order ord, Customer cust ,Agent agent ,boolean orkla) {
		 NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);
		  nf2.setMaximumFractionDigits(4);
			nf2.setMinimumFractionDigits(2);
			nf2.setGroupingUsed(false);
				    	String ss = "olv";
				    	
				    	String fname = (parent.getProps().getProperty("savefilesdir")) + ss + ".xls";
				    	
				    	

						Calendar shdate = ord.getShipCal();
						Calendar cal = Calendar.getInstance();
						if (shdate != null) {
							cal.setTime(shdate.getTime());
						} else {
							cal.add(Calendar.DAY_OF_MONTH, 1);
						}
						
   try{
	   
						  FileInputStream fIPS= new FileInputStream(fname); //Read the spreadsheet that needs to be updated
					        HSSFWorkbook  wb = new HSSFWorkbook(fIPS);
					        HSSFSheet worksheet = wb.getSheetAt(0);
					    
					    	String ref = ord.getTeostaja(); //mobiilse telli ref number
							if ( ref == null ){
								ref =  "";
							}
							if(ref.endsWith("null")){
								ref = ref.substring(0, ref.length()-4);
							}
						
							ref = ref.trim();
							
							if(ref.indexOf(" ") > -1){
								ref = ref.substring(0,ref.indexOf(" "));
							}
					        
					         //Paneme dokumendi numbri paika 
					        HSSFRow row1 = worksheet.getRow(8);  //0 = row number
					        Cell c = row1.getCell(1);
					             c.setCellValue(c.getStringCellValue() + " " + ref);
					    
					           //Paneme dokumendi kuupäeva paika 
							    row1 = worksheet.getRow(13);  //0 = row number
							      c = row1.getCell(0);
							      c.setCellValue(Converter.getDDpMMpYYYYpString(Calendar.getInstance()));
							    
							      

						           //Paneme ketoi paika 
								    row1 = worksheet.getRow(4);  //0 = row number
								      c = row1.getCell(9);
								      c.setCellValue(cust.getChain());
								    

							  //Paneme vastutava persooni paika 
							    row1 = worksheet.getRow(19);  //0 = row number
								  c = row1.getCell(0);
								  String val = c.getStringCellValue();
							      c.setCellValue(val.substring(0, val.indexOf("$$")) + " " + agent.getName() + " " +
							    		  val.substring(val.indexOf("$$") +2));
									    	         
							      //Paneme kliendi nime ja aadressi paika 
								    row1 = worksheet.getRow(15);  //0 = row number
									  c = row1.getCell(0);
								      c.setCellValue(c.getStringCellValue() + " " + cust.getName() + " " + cust.getAddress() + " " + cust.getRegnr());
										    	         
						          
					    cal.add(Calendar.MONTH, 1);
				    	
				    	
						Vector lines = ord.getOrderLines();
						double total = 0;
						
	for (int i = 0; i < lines.size(); i++) {
								OrderLine ol = (OrderLine) lines.elementAt(i);

								  String toode = ol.getToode();
								  boolean ork = true;
							         if(toode.startsWith("con-")){
							        	 toode = toode.substring(4);
							        	 ork = false;
							         }
								Product prod = getProduct(toode, ork);
								
							
								
								if(prod == null) {
									prod =  new Product(toode);
									prod.setEAN(toode);
									prod.setName(toode);
									prod.setPrice(ol.getPrice());
									prod.setUnit("pc");

	                                }
									
							
								
								
								 	    	         
								
								
							         String disc = "0";
								        double disco = 0;
								        if(ol.getDiscount()> 0 && ol.getDiscount() < 99){
								        	disc = ol.getDiscount()+"";
								        	disco = ol.getDiscount();
								        }
						     // siia vahele sii loogika teha et kui on PL = 9 siis jagame muidu ei jaga 
								     double jaga = 1;
								    // System.out.println("PL trykk " + ol.getPl() + " " + ol.getToode() + " " + prod.getPrice() );
								     
								     
								     String jagaja = "1";
								     
								     if(ol.getPartii() != null && ol.getPartii().indexOf("|") != -1){
								        	jagaja = (ol.getPartii().substring(ol.getPartii().indexOf("|")+1).trim());
								        }
								    
						// kui hind oli tk oma siis ei jaga midagi 
								     boolean pcprice = false;
								     
								     if(prod.getPrice() > 0 && ol.getPl() == 9 ){
								    	 double jjj = Double.parseDouble(jagaja);
								    	 if(jjj > 1 && ol.getPrice()* Double.parseDouble(jagaja) * 0.6 < prod.getPrice()){
								    		 //juhul kui ol hind korda sisukogus on suuerm toote hinnast siis 
								    		 //polnud sisestatud yhiku hind 
								    		 pcprice = true;
								    	 }
								     }
								     
								   
								     
								    if( !pcprice ){
								       	jaga = Double.parseDouble(jagaja);
									}
								       
						   double sum = Double.parseDouble(nf.format(ol.getPrice()/jaga * (ol.getKogus() *-1) + 0.001));
	                       double alesum = Double.parseDouble(nf.format(((ol.getPrice()*(1-disco/100))/jaga) * (ol.getKogus() *-1) + 0.001));
							double diff = sum-alesum;
							total = total + diff;
								
							      row1 = worksheet.getRow(i+23);  //0 = row number
									//nimi
									c = row1.getCell(1);
									c.setCellValue(prod.getName());
								 
								  //ühik
								  c = row1.getCell(2);
							      c.setCellValue(prod.getUnit());
								
								  //kogus
								  c = row1.getCell(3);
							      c.setCellValue(nf.format((ol.getKogus() *-1)));
							     System.out.println("asi " + ol + " " + jaga + " " + nf2);
								  // tavahind 
							      c = row1.getCell(4);
							      c.setCellValue(nf2.format(ol.getPrice()/jaga));
							  
							      // summa 
							      c = row1.getCell(5);
							      c.setCellValue(sum);
								 
							      // ale 
							      c = row1.getCell(6);
							      c.setCellValue(disc);
							      
							      // aleyhele 
							      c = row1.getCell(7);
							      c.setCellValue(nf2.format(((ol.getPrice()*(1-disco/100)))/jaga));
							     

							       // alesummakokku 
							      c = row1.getCell(8);
							      c.setCellValue(alesum);
								 System.out.println(ol.getPrice()*ol.getKogus() 
										 + " " + (ol.getPrice()*(1-disco/100))/jaga*ol.getKogus() 
								 + " " + diff);
							      
							      // vahe 
							      c = row1.getCell(9);
							      c.setCellValue( nf.format(diff));
							   
							      c = row1.getCell(10);
							      c.setCellValue(Converter.getDDpMMpYYYYpString(cal));
							      
							}
	                    
	
	                System.out.println("totals " + nf.format(total) );
						      row1 = worksheet.getRow(57);  //0 = row number
								//alussumma
								c = row1.getCell(9);
								c.setCellValue(nf.format(total));
								   row1 = worksheet.getRow(58);  //0 = row number
									//km
									c = row1.getCell(9);
									c.setCellValue(nf.format(total*0.21));
				
									row1 = worksheet.getRow(59);  //0 = row number
									//km
									c = row1.getCell(9);
									c.setCellValue(nf.format(total*1.21));
				
								    fIPS.close(); //Close the InputStream
					        FileOutputStream output_file =new FileOutputStream(ss+ "-" +agent.getID() + ".xls");
					        wb.write(output_file); //write changes
					        output_file.close();  //close the stream
						
					        
					        }catch (Exception k ){
					     	   k.printStackTrace();
					        }
					     										
						return ss+ "-" +agent.getID() + ".xls";
					
				    }

				    
				    
	private Product getProduct(String prodid, boolean orkla){
		Product prod = null;
		String ork = "olv";
		if(!orkla){
			ork = "k12";
		}
		  
	    String cmd = "  SELECT disc.todate, disc.fromdate,inv.itemid,inv.itemname  AS nimetus, stbqtyinbox AS kastis,stbqtyinpallet AS alusel, disc.amount AS price " +  
		"  , bar.itembarcode AS ean ,inv.itemgroupid AS grupp,mod.unitid As unit, taxitemgroupid as vat,mod.LineDisc as alegr " +
		"  , mod.QUANTITY as opti , TPdiscount as maxdisc ,StbMinInQty as inpack,  ItemProdTypeId as status , " +
		"  pdgroupid as pgrupp , StbMaxDiscount as max2 FROM bmssa.inventtable inv  " +
		"  left JOIN bmssa.pricedisctable disc ON inv.itemid=disc.itemrelation and disc.dataareaid= inv.dataareaid  " +
		"  left JOIN bmssa.inventtablemodule mod ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid  " +
		"  left join bmssa.inventitembarcode bar ON bar.dataareaid = inv.dataareaid and bar.itemid=inv.itemid and bar.useforprinting = 1 " + 
		"  WHERE inv.dataareaid='" + ork + "'   " +
		"  and ((disc.fromdate<getdate() and disc.todate=('1900-01-01 00:00:00.00') ) or " +
		"  getdate() between disc.fromdate and disc.todate    )  " +
		"  AND moduletype=2   " +
		"  and disc.accountrelation=''  and inv.ITEMID = '" + prodid + "'  ";

	    
	    try {
	    
System.out.println(orkla +" " + cmd);
	    	  
	  	  Connection con = null;
	  	 
	  	  if(orkla){
	  		 con = this.getNewConnection();
	      }else{
	    	 con = this.getNewConnection2();
	      }
	  	  
	  	  Statement stmt =  con.createStatement();
          ResultSet rs = stmt.executeQuery(cmd);
	      while(rs.next()){


	    	  
	    	  String kood = rs.getString("itemid").trim();
	    	  System.out.println(" kood " + kood + " " + prodid);
	         prod = new Product(kood);
	        String nimetus =  rs.getString("nimetus").trim() ;
	        prod.setInCases(rs.getDouble("kastis"));
	        prod.setOnPallet(rs.getDouble("alusel"));
	        double price = rs.getDouble("price"); 
	        
	        if(price > 0){
	            prod.setPrice(price);
	        }
	        	

	        String ean = rs.getString("ean");
	      
	        if(ean != null && ean.trim().length()>0){
	          prod.setEAN(ean);
	        }
	        
	        String grupp = rs.getString("grupp");
	        String abi = grupp;
	        if(grupp!=null && grupp.trim().length()>0){
	    	    if(abi.length() > 4){
	          		 abi = abi.substring(0, 4);
	          	 }else{
	          		 if(abi.length() != 4){
	          			 abi = abi + "0";	 
	          		 }
	          		 if(abi.length() != 4){
	          			 abi = abi + "0";	 
	          		 }
	          	 }
	            
	    	    prod.setGroup(abi);  
	        	 
	     }
	        
	        String unit = rs.getString("unit");
		      
	        if(unit != null && unit.trim().length()>0){
	          prod.setUnit(unit);
	        }
	    
	      prod.setName(nimetus);

	      
	      }
	      rs.close();
	      stmt.close();
	      con.close();
	  	
		    }catch (Exception e ){
		    	e.printStackTrace();
		    }
		
		return prod ;
	}
					

  
  private void makeOutput(Order order){
	 	StringBuffer sb = new StringBuffer();
	 	
	 	try {
	 	    String doknr = order.getDokNr();
		      String fn = doknr;
		      String mills = (Calendar.getInstance().getTimeInMillis()+"").substring(10);
		      if(doknr.trim().length()== 0){
		    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
		      }
		      fn = fn +"-" + mills;
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
	         String sep = ",";
	         String lineSep = System.getProperty("line.separator");
	         String klient = order.getCompany();
	         String dim = "olv";
	         if(klient.startsWith("con-")){
	        	 klient = klient.substring(4);
	        	 dim = "k12";
	         }
	         
	         Calendar call = Calendar.getInstance();
	         call.add(Calendar.DAY_OF_YEAR, 1);
	         if(order.getDealer().equals("orkla")){
	        	 call = order.getShipCal();
	         }
	         ArrayList arr = new ArrayList ();
	    Vector lines = order.getOrderLines();
	      for (int i = 0; i < lines.size(); i++) {
	        OrderLine ol = (OrderLine) lines.elementAt(i);
	 //    _E20587,"LV2174-003","04.02.2016","P120504511","120.0000000000","0.7634000000","10","","05.02.2016","4539006126","","0"
	//     _E20587,"LV2174-003","04.02.2016","N200111100","1440.0000000000","0.2720000000","0","","05.02.2016","4539006126","",""
            String toode = ol.getToode();
	         if(toode.startsWith("con-")){
	        	 toode = toode.substring(4);
	         }
	        sb.append("MS-"+ fn + sep);
	        sb.append("\""+ klient+ "\"" + sep);
	        sb.append("\""+ getDateString(Calendar.getInstance())+ "\"" + sep);
	        sb.append("\""+toode+"\"" + sep);
	        sb.append("\""+ol.getKogus()+"\"" + sep);
	        
	        String disc = "0";
	        double disco = 0;
	        if(ol.getDiscount()> 0 && (ol.getDiscount() < 99 || arr.contains(toode)) ){
	        	disc = ol.getDiscount()+"";
	        	disco = ol.getDiscount();
	        }
	       // String price = this.getPrice(klient, toode, disco, dim) + "";
	        sb.append("\""+ol.getPrice()+"\"" + sep);
	        sb.append("\""+disc+"\"" + sep);
	        sb.append("\""+"\"" + sep); //tyhi
	        sb.append("\""+ getDateString( call)+ "\"" + sep);
	        sb.append("\""+ref+"\"" + sep);
	        sb.append("\""+ getDateString( call)+ "\"" + sep);
		       
	        if(disco == 100 || ol.getDiscount() == -2){
		        sb.append("\"1\"" + sep);
	        }else{
		        sb.append("\""+"\"" + sep);
	        }

	        sb.append("\""+comm+"\"" + sep);
	        if(order.getWarehouse() != null && order.getWarehouse().equals("br")){
	         	  sb.append("\"GPNPA\"" + sep);
	     	}
        
	     arr.add(toode);
	          sb.append(lineSep);
	 
	       counter++;
	      }
	      
	      setNumAddedLines(counter); 
	      

   	   fileName =   "o-" + order.getAgent() +"."+ order.getCompany()+"." +fn  + ".csv";
	      
String fname = (parent.getProps().getProperty("conv.dir2")) + fileName;
			    		
			      if(order.getDealer().equals("orkla")){
			    	  fname = (parent.getProps().getProperty("conv.dir")) + fileName;
  }
			      FileOutputStream fos = new FileOutputStream(fname);
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
    return "AxaOrklaLV";
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
  private String url = "jdbc:odbc:orkla";
  private String url2 = "jdbc:odbc:kondit";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);
  private int counter = 0;
  private ImportPanel parent;

public String getTrouble() {
	return null;
}
}
