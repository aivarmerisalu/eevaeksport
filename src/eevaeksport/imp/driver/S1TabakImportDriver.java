
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

public class S1TabakImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public S1TabakImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    try {
   
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
	 		    addlocks(false,false);
    	 	     makeOutput(ord); 
	 		String lineSep = System.getProperty("line.separator");
  		  		 writeConsloStr(parent.getProps().getProperty("conv.dir") + "index.csv", fileName + "," + this.getDateTimeString(Calendar.getInstance())+ ",,," + lineSep);
       			 addlocks(true,false);
			
	 	 signatures.add(signature);
      	}   
      }

	  try {
			Converter.saveObject(signatures, "signatures.vec");
		} catch (IOException e) {
			e.printStackTrace();
		}
	  
	  setNumAddedLines(counter);
  
  }

 
  
  private String fileName = null;
  
  private void addlocks(boolean delete , boolean kondit){
	 
  	String fileNam = parent.getProps().getProperty("conv.dir") + "lock"; 
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
	  

    private String getDateTimeString(Calendar cal){
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
	    
	    sb.append(cal.get(Calendar.YEAR) + " ");
	    //12.02.2025 10:01:57
	    
	    String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
	    if (hour.length() == 1){
	      sb.append("0");
	    }
	    sb.append(hour+":");
	    
	    String minu = String.valueOf(cal.get(Calendar.MINUTE));
	    if (minu.length() == 1){
	      sb.append("0");
	    }
	    sb.append(minu + ":");
	    
	    String seco = String.valueOf(cal.get(Calendar.MINUTE));
	    if (seco.length() == 1){
	      sb.append("0");
	    }
	    sb.append(seco);
	    
		    return sb.toString();
	  }
  
  private Vector splitOrder (Order order){
			  
		  Vector result = new Vector();
		  
		  Vector lines = order.getOrderLines();
		  boolean change = false;
		  Enumeration enumr = lines.elements();
		  boolean pos = false;
		  boolean neg = false;
		  while  (enumr.hasMoreElements()){
			  OrderLine lin = (OrderLine)enumr.nextElement();
			  if(lin.getKogus() > 0) {
				  pos = true;
			  }else {
				  neg = true;
			  }
		  }
		  
		  if(pos && neg) {
			  change = true;
		  }
		  
		  if (order.getDokNr() == null || order.getDokNr().trim().length() == 0){
			  order.setDokNr((Calendar.getInstance().getTimeInMillis() + "").substring(3));
		  }
		  System.out.println("Infokene " + order.getInfo() + " " + order.getDokNr());

		  Order ordorkla = new Order(order.getCompany(),order.getAgentFirstName());
		  Order ordorklabonus = new Order(order.getCompany(),order.getAgentFirstName());
		  Order ordorklaret = new Order(order.getCompany(),order.getAgentFirstName());
		  Order ordorklacha = new Order(order.getCompany(),order.getAgentFirstName());
		  
		  ordorklacha.setAgentCode(order.getAgentCode());
		  ordorklacha.setDokNr(order.getDokNr()+"C");
		  ordorklacha.setCompName(order.getShipMode());
		  ordorklacha.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordorklacha.setStatus(order.getStatus()); 
		  ordorklacha.setInfo(order.getInfo());
		  ordorklacha.setReference(order.getReference());
		  ordorklacha.setTeostaja(order.getTeostaja());
		  ordorklacha.setWarehouse(order.getWarehouse());
		  ordorklacha.setDealer(order.getDealer());
		  ordorklacha.setShipTime(order.getShipTime());
		  
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
		  ordorkla.setShipTime(order.getShipTime());
		  
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
		  ordorklabonus.setShipTime(order.getShipTime());
			 

		  ordorklaret.setAgentCode(order.getAgentCode());
		  ordorklaret.setDokNr(order.getDokNr()+"R");
		  ordorklaret.setCompName(order.getShipMode());
		  ordorklaret.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordorklaret.setStatus(order.getStatus()); 
		  ordorklaret.setInfo(order.getInfo());
		  ordorklaret.setReference(order.getReference());
		  ordorklaret.setTeostaja(order.getTeostaja());
		  ordorklaret.setWarehouse(order.getWarehouse());
		  ordorklaret.setDealer(order.getDealer());
		  ordorklaret.setShipTime(order.getShipTime());

		  
	
		  
		   lines = order.getOrderLines();
		  /*
		   * 	
      	 	 Vector lin = order.getOrderLines();
      	 	 boolean laks = false;
      	 	 for (int i = 0; i < lin.size(); i++) {
      	 		OrderLine ol = (OrderLine) lin.elementAt(i);
      	 		if(ol.getKogus() < 0 && ol.getDiscount() > 0 && (order.getStatus() == 5 || order.getStatus() == 4)){
      	 			laks = true;
      	 		}
      	 	 } */
		  
		   enumr = lines.elements();
		  while  (enumr.hasMoreElements()){
			  OrderLine lin = (OrderLine)enumr.nextElement();
			  
			  if(change) {
			    	
		    		ordorklacha.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		    		ordorklacha.setDokNr(order.getDokNr()+"C");
		    		ordorklacha.setReference(order.getReference());
			 		
		    	}  else if(lin.getDiscount() == -2) {
			    	
			    		ordorklabonus.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			    		ordorklabonus.setDokNr(order.getDokNr()+"B");
			    		ordorklabonus.setReference(order.getReference());
				 		
			    	}else if (lin.getKogus() < 0) {
			    		ordorklaret.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
				     	ordorklaret.setDokNr(order.getDokNr()+"R");
				     	ordorklaret.setReference(order.getReference());
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

		  if(ordorklaret.getOrderLines() != null && ordorklaret.getOrderLines().size()>0){
			  result.add(ordorklaret);  
		  }
		  
		  if(ordorklabonus.getOrderLines() != null && ordorklabonus.getOrderLines().size()>0){
			  result.add(ordorklabonus);  
		  }
		  
		  if(ordorklacha.getOrderLines() != null && ordorklacha.getOrderLines().size()>0){
			  result.add(ordorklacha);  
		  }
		  
		 System.out.println("Result = " +order.getDokNr()
				 + " "+ result.size());
		 //System.exit(0);
		  return result ;
	  }


  

  				    
				    

  
  private void makeOutput(Order order){
	 	StringBuffer sb = new StringBuffer();
	 	
	 	try {
	 	    String doknr = order.getDokNr();
	 	    System.out.println("doknr " + doknr);
	 	    boolean tagastus = false;
	 	    if(doknr.endsWith("R")) {
	 	    	tagastus = true;
	 	    }
	 	   boolean change = false;
	 	    if(doknr.endsWith("C")) {
	 	    	change = true;
	 	    }
	 	   boolean bonus = false;
	 	    if(doknr.endsWith("B")) {
	 	    	bonus = true;
	 	    }
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
				comm = "";
			}
			String time = order.getShipTime();
			if ( time == null ){
				time = "";
			}else{
				time = " " + time;
			}
			
	         String sep = ";";
	         String lineSep = System.getProperty("line.separator");
	         String klient = order.getCompany().replace('_', ':');;
	         
	         
	         Calendar call = Calendar.getInstance();
	         call.add(Calendar.DAY_OF_YEAR, 1);
	         if(order.isShipDateUsed()){
	        	 call = order.getShipCal();
	         }
	         
	         String rrr = (comm + time).trim();
	         if(tagastus) {
	        	 rrr = "ATG";
	         }
	         if(change) {
	        	 rrr = "APM";
	         }
	       	 fileName =   "H_" + fn + ".csv";
	       	 StringBuffer sb0 = new StringBuffer();
	       	 //369272;LT1004280:242970;12.02.2025 10:01:57;L;13.02.2025;944;;;0.00;FALSE;12.02.2025 10:40:39;;
	            sb0.append(fn + sep);
		        sb0.append( klient + sep);
		        sb0.append(getDateTimeString(Calendar.getInstance()) + sep + "L" +
		        sep + getDateString(call) + sep + agent+sep+sep+sep+"0.00"+sep +"FALSE"+sep +getDateTimeString(Calendar.getInstance())+sep+ rrr +sep);
		      
	    	        
	         String fname = (parent.getProps().getProperty("conv.dir")) + fileName;
	      		      FileOutputStream fos = new FileOutputStream(fname);
	    			        fos.write(sb0.toString().getBytes());
	    			        fos.close(); 
	      
	         
	         ArrayList arr = new ArrayList ();
	    Vector lines = order.getOrderLines();
	      for (int i = 0; i < lines.size(); i++) {
	        OrderLine ol = (OrderLine) lines.elementAt(i);
            String toode = ol.getToode();
	         if(toode.startsWith("con-")){
	        	 toode = toode.substring(4);
	         }
	         /*00000001117;5.0;-1;UNIT;1.8000;0.00;0;;
00000001292;5.0;-1;UNIT;1.2830;0.00;0;;
00000001376;6.0;-1;UNIT;4.1700;0.00;0;;*/
	        sb.append(toode + sep);
	        sb.append(ol.getKogus() + sep);
	        sb.append("-1" + sep);
	        sb.append(ol.getUnit() + sep);
	        
	        String disc = "0.00";
	        if(ol.getDiscount()> 0 && (ol.getDiscount() < 99 || arr.contains(toode)) ){
	        	disc = ol.getDiscount()+"";
	        }
	       double pr = ol.getPrice();
	       if(bonus) {
	    	   pr = 0;
	       }
	        sb.append(pr + sep);
	        sb.append(disc + sep);
	        sb.append("0" + sep+sep); //tyhi
	       
        
	         arr.add(toode);
	          sb.append(lineSep);
	 
	       counter++;
	      }
	      setNumAddedLines(counter); 
	      

   	 String fileName2 =   "B_" + fn + ".csv";
	        
      fname = (parent.getProps().getProperty("conv.dir")) + fileName2;
  		       fos = new FileOutputStream(fname);
			        fos.write(sb.toString().getBytes());
			        fos.close();
	    		
	    	

				   //   System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
	  
 
	public NumberFormat getNF() {
		return this.nf;
	}
 
  public String getDriverVersion(){
    return "S1TabakImport";
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
  private NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);
  private int counter = 0;
  private ImportPanel parent;

public String getTrouble() {
	return null;
}
}
