/*
 * Created on 9.11.2007
 */
package eevaeksport.imp.driver;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */

    import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

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

	public class ViaLaimaImportDriver
	    extends AbstractImportDriver {

	  static Category log = Category.getInstance(IceImportDriver.class.getName());

	  public ViaLaimaImportDriver(ImportPanel parent) {
	    this.parent = parent;
	    PropertyConfigurator.configure("log4j.properties");
	   
	    try {
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
	  
	  

	  
	  
	  private String getDateString(Calendar cal){
				StringBuffer sb = new StringBuffer();
				String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
				if (day.length() == 1) {day = "0" + day;}

				String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
				if (month.length() == 1){month = ("0") + month;}

				sb.append("" + cal.get(Calendar.YEAR));
				sb.append("" + month);
				sb.append(day +"");
				return sb.toString();
			
		  }
		
	  
	  	  
	  	     
	
	  public String identifyCustomer(String id,String regnr) throws SQLException {
	  	
	    return id;

	  }

	  	  
	 
	  public String identifyProduct(String ean, String prodID) throws SQLException{
	    return prodID;
	  }

	  public void makeOrder(Order order) throws SQLException{
			    setOrder(order);
			    try {
					makeOutput(order);
				} catch (IOException e) {
					e.printStackTrace();
				}
	  }

	  private String escapeCSV(String s){
		    s.replaceAll("\"", "\"\"");
		    s.replaceAll("\n"," ");
		    s.replaceAll(System.getProperty("line.separator")," ");
		    return "\"" + s + "\"";
		  }
	   
	  private void makeOutput(Order order) throws IOException{
		 	StringBuffer sb = new StringBuffer();
		    String lineSep = System.getProperty("line.separator");

		        String saaja = order.getCompany();
		        if(saaja.length()==0){
		        	  throw new IOException("Kliendil " + order.getCompName() + " on VIA3L Saaja kood Sisestamatta!!!!!!!!!");
		      }
		    	
		      char sep = ',';

		      String tell = order.getReference();
		      if(tell == null){
		    	  tell = "";
		      }
		      
	
		      
		      
     String filiaal = parent.getProps().getProperty("koostaja.filiaal");	
     String comm = order.getShipMode();
     if(comm != null){
    	 comm = comm.replaceAll(System.getProperty("line.separator"), " ").replaceAll("\n", " "); 
     }else {
    	 comm = "";
     }
   
     String agent = parent.getProps().getProperty("koostaja.agent");
     String agentname = parent.getProps().getProperty("koostaja.agentname");
     String email = parent.getProps().getProperty("koostaja.email");
     
	  sb.append("\"pakkumine\"" + sep);
      sb.append(sep);
      sb.append("\""+ (filiaal) + "\"" + sep);
      sb.append(sep); 	      
      sb.append("\"" +  tell + "\"" + sep);
      sb.append("\"" +(order.getDokNr())+ "\"" + sep);    
      sb.append("\"" + order.getCompany() + "\"" + sep + "\"" + "z" + order.getPaymTerm() + "\"" + sep);
      sb.append("\"" + getDateString(Calendar.getInstance()) + "\"" + sep);
      sb.append("\"" + getDateString(order.getShipCal()) + "\"" + sep);
      sb.append("\"" +  tell  + "\"" + sep);
      sb.append("\"\"" + sep);
      sb.append("\"\"" + sep);
      sb.append("\"" + agentname + "\"" + sep);
      sb.append("\"\"" + sep);
      sb.append("\"" + email + "\"" + sep);
      sb.append("\"\"" + sep);
      sb.append("\"" + agent + "\"" + sep);   
      sb.append("\"" + comm + "\"" + sep);
      sb.append("\"\"" + sep);
  
      sb.append(lineSep);
		    

		      Iterator iter = order.getOrderLines().iterator();
		      int counter = 0;
		      while(iter.hasNext()){
		        counter++;
		        OrderLine ol = (OrderLine)iter.next();
		        
		        
		        double hind = ol.getBonus()/ol.getKogus() / (1-(ol.getPrice()/100));
		        String disc = "";
		        if(ol.getPrice()>0){
		        	disc = ol.getPrice() + "";
		        }
		        	
		        sb.append("\"LIN\"" + sep);
		          sb.append("\""  +  (ol.getToode()+filiaal)  + "\""  + sep);
		          sb.append("\""  + (ol.getTooteNimi().replaceAll("\"","'")) + "\""  + sep);
		          sb.append("\"" + (ol.getUnit()) + "\"" + sep);
		          sb.append(ol.getKogus());	          
		          sb.append(sep);
		          sb.append(sep); 
		          sb.append(hind + "" +sep + "" + sep + "\"" );		         
		          sb.append( ol.getBonus()+ ""); // reasumma
		          sb.append("\"" + sep);
		          sb.append("" + sep);
		          sb.append(disc+ "" + sep);
		          sb.append("" +sep);
		          sb.append("" +sep);
		         
		          sb.append(lineSep);
		      }


		      sb.append("\"FTR\"" + sep);
		      sb.append(counter + lineSep);
		          
		  this.setNumAddedLines(counter);  

		      String fileName = parent.getProps().getProperty("conv.dir") + "p-" + filiaal + "-" +  parent.getProps().getProperty("firma").trim() + "-" + order.getDokNr() +
		                      ".csv";
		  
		    try{
			      FileOutputStream fos = new FileOutputStream(fileName);
			      fos.write(sb.toString().getBytes());
			      fos.close();        	
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
		  
	  
		 }
		  

			  
	 public String getDriverVersion(){
	    return "ViaLaimaImport";
	  }

	  private void setOrder(Order order){

	  }



	  private String getYear(Calendar cal){
	 
	    String result = calNF.format(cal.get(Calendar.YEAR)) ;
	    return result;
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
	  private ImportPanel parent;

	public String getTrouble() {
		// TODO Auto-generated method stub
		return null;
	}
	}
