/*
 * Created on 9.11.2007
 */
package eevaeksport.imp.driver;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.imp.SheetPrinterPrike;

	/**
	 * Title:        Eeva eksport
	 * Description:
	 * Copyright:    Copyright (c) 2002
	 * Company:
	 * @author Tarmo Tubro
	 * @version 1.0
	 */

	public class ScalaPrikeEstCCImportDriver
	    extends AbstractImportDriver {

	  static Category log = Category.getInstance(IceImportDriver.class.getName());

	  public ScalaPrikeEstCCImportDriver(ImportPanel parent) {
	    this.parent = parent;
	    loadPrinted();
	    managePrinted();
	    PropertyConfigurator.configure("log4j.properties");
	    
	    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	    try {
	      Class.forName(databaseDriverName).newInstance();
	      con = DriverManager.getConnection(url,"mobilejava","mobilejava" );
	      con2 = DriverManager.getConnection(url,"mobilejava","mobilejava" );
	     // updateCustcodes();
	      calNF.setMaximumFractionDigits(0);
	      calNF.setMaximumIntegerDigits(2);
	      calNF.setMinimumIntegerDigits(2);
	      calNF.setGroupingUsed(false);
	      nf2.setMaximumFractionDigits(2);
	      nf.setMaximumFractionDigits(4);
	      nf.setGroupingUsed(false);
	      nf2.setGroupingUsed(false);
	    }
	    catch (Exception e) {
	      parent.showError(e);
	    }
	  }
	  
	  
	  public void loadPrinted() {
			File f = new File(getDataDir() + "printed.vec");
			ObjectInputStream ois;
			if (!f.exists()) {
				f = new File(getDataDir() + "printed.vec");
			}
			if (f.exists()) {
				try {
					ois = new ObjectInputStream(new FileInputStream(f));
					printed  = (Vector) ois.readObject();
					ois.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		public static void saveObject(Object obj, String file) throws IOException {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(obj);
			oos.close();
		}
	  
	  public void savePrinted() {
		  try {
			saveObject(printed,getDataDir() + "printed.vec");
		} catch (IOException e) {
			e.printStackTrace();
		}
		}

	  public void managePrinted() {
		  if(printed.size() > 10000){
			  for(int i = 0 ; i<1000; i++){
				  printed.remove(0);
			  }
			  try {
					saveObject(printed,getDataDir() + "printed.vec");
				} catch (IOException e) {
					e.printStackTrace();
				}
		  }	 
		}
	  

	  
	  
	  
	
	  public String identifyCustomer(String id,String regnr) throws SQLException {
	  	    return null;
	  }

	 
	  public String identifyProduct(String ean, String prodID) throws SQLException{
	    return null;
	  }

	 
		

	  public void makeOrder(Order order) throws SQLException{
			    setOrder(order);

			    if("true".equals(parent.getProps().getProperty("reprintolddoc"))){
			    	  makePrinting(order);	
				  }else{
					  if(!printed.contains(order.getDokNr())){
						  makePrinting(order);	
						  printed.addElement(order.getDokNr());
						  savePrinted();
					  }else{
						  log.error("Korduv printimine : " + order.getDokNr());
						  this.setNumAddedLines(order.getOrderLines().size());
					  }
					
				  }
			    
	  }

	 private Vector sort(Vector vvv){
		 Vector sorted = new Vector();
		// System.out.println("SORTING............");
		 TreeMap tt = new TreeMap();
		 Enumeration enume = vvv.elements();
		 while (enume.hasMoreElements()){
			OrderLine ol = (OrderLine)enume.nextElement();
			if(ol.getUnit().indexOf("|") != -1){
				String koht = ol.getUnit().substring(ol.getUnit().indexOf("|")+1);
				if(koht== null || koht.trim().length()== 0){
					koht = " ";
				}
				tt.put(koht, koht);
			} 	
		 }
		 System.out.println("tt" + tt.toString());
		 Iterator ite = tt.keySet().iterator();
		 while(ite.hasNext()){
			 String koht = (String)ite.next();
			 Enumeration enumex = vvv.elements();
			 while (enumex.hasMoreElements()){
				 OrderLine ol = (OrderLine)enumex.nextElement();
				 String kohtx = ol.getUnit().substring(ol.getUnit().indexOf("|")+1);
				
				 if(kohtx.equals(koht)){
					 sorted.add(ol);
					
				 }else if(kohtx.trim().length()== 0 && koht.equals(" ")){
					 sorted.add(ol);
					
				 }
			 }
				
		 }
		// System.out.println("SORTING " + vvv.size() + " " + sorted.size());
		 return sorted;
	 }
		  
 private void makePrinting(Order order){
	  
		  PrinterJob job = PrinterJob.getPrinterJob();
		  SheetPrinterPrike pr = new SheetPrinterPrike();
		
		  String name = order.getCompName();
		  pr.setAddInt(10);
		 // pr.setAddtop(300);
		  pr.setNumber(order.getDokNr());
		
		  pr.setAadress( order.getTeostaja());
		  pr.setNimi(name);
		  if(order.getShipTime().indexOf("|") != -1){
			  pr.setKuupaev(order.getShipTime().substring(0,order.getShipTime().indexOf("|")));  
		  }else{
			  pr.setKuupaev(order.getShipTime());
		  }
		 
		  pr.setTarnepaev(getTarne(order));
		  pr.setAgendinimi(order.getWarehouse());
		  pr.setMarkused(order.getInfo());
		  pr.setRing(order.getShipMode());
		  System.out.println("Kogusumma " + order.getShipTime() );
		  
		  if(order.getShipTime().indexOf("|") != -1){
			  pr.setTarneHalve(order.getShipTime().substring(order.getShipTime().indexOf("|") + 1));
		  }
		 
		  Vector sortedLines = sort(order.getOrderLines());
		  pr.setLines(sortedLines);
		  
		  job.setPrintable(pr);
			
		  boolean autoprinting = false;
		  if("true".equals(parent.getProps().getProperty("autoprinting"))){
			  autoprinting = true;
		  }else{
			  autoprinting = job.printDialog();
		  }
    
		  if (autoprinting) {
		    try {
		         job.print();
		    } catch (PrinterException e) {
		    	e.printStackTrace();
		    }
		  }
		  this.setNumAddedLines(order.getOrderLines().size());
	  }  
 
	  
	 

			  
	 public String getDriverVersion(){
	    return "ScalaPrikeEstCCImport";
	  }

	  private void setOrder(Order order){

	  }


	  

	  private String getToday(){
	    Calendar cal = Calendar.getInstance();
	    String result = calNF.format(cal.get(Calendar.YEAR)) +
	                    calNF.format(cal.get(Calendar.MONTH) + 1) +
	                  calNF.format(cal.get(Calendar.DAY_OF_MONTH));
	    return result;
	  }

	  private String getTarne(Order order){
	    Calendar cal = Calendar.getInstance();
	    Calendar dat = order.getShipCal();

	    String result = null ;
	   
	    
	    	 String yy = (cal.get(Calendar.YEAR))+"";

	    	  String mm = (cal.get(Calendar.MONTH)+1)+"";
	    	  if(mm.length()== 1){
	    	  	mm = "0"+mm;
	    	  }
	    	  
	    	  
	    	  String dd = (cal.get(Calendar.DAY_OF_MONTH))+"";
	    	  if(dd.length()== 1){
	    	  	dd = "0" + dd;
	    	  }
	    	  
		  result = yy.substring(2,4) + mm + dd;
		  System.out.println("P�EV " + result + " " + dat.getTime().toString());
	    
	    return result;
	  }
	  

	  
	  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
	  
	  public String getDataDir(){
		    return parent.getProps().getProperty("datadir");
		  }
		  
	  private NumberFormat calNF = NumberFormat.getInstance();


	  private String url = "jdbc:odbc:scalaDB";
	  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
	  private NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);
	  private Connection con;
	  private TreeMap zalMap = new TreeMap();
	  private Connection con2;
	  private ImportPanel parent;
	  private Vector printed = new Vector();

	public String getTrouble() {
		// TODO Auto-generated method stub
		return null;
	}
	}


