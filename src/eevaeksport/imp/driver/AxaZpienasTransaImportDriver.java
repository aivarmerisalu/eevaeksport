package eevaeksport.imp.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Email;
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

public class AxaZpienasTransaImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public AxaZpienasTransaImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "oracle.jdbc.driver.OracleDriver";
    databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
    
      con5 = DriverManager.getConnection(parent.getProps().getProperty("dsn.host")
        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      
    } catch (Exception e){
       log.error(e);
       this.sendExceptionEmail(e.toString());
       System.exit(0);
    }
  }

  
  
  
  private void sendExceptionEmail(String buf){
	  String address = parent.getProps().getProperty("exceptionemail");
	  if(address != null ){
		  Email email = new Email(parent.getEksportFrame().getString("importerror"));
         
          email.setFrom(parent.getProps().getProperty("from"));
          email.setSMTP(parent.getProps().getProperty("smtphost"));
          email.setUser(parent.getProps().getProperty("smtpuser"));
          email.setPass(parent.getProps().getProperty("smtppass"));
          email.setSubjectFilesTo(parent.getEksportFrame().getString("importerror")  );
          if(parent.getProps().getProperty("language").equals("Russian")){
            email.setEncoding("KOI8-R");
          }
          
          
          email.setEncoding("iso-8859-1");
          try {
        	  
        	email.setBody(buf.toString());

            StringTokenizer st =  new StringTokenizer(address,";");
            while(st.hasMoreTokens()){
            	String aadr = new InternetAddress(st.nextToken()).toString();
              email.setFilesTo(aadr);
              email.send();
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
  }
  
  public String getImportFile(){
   return parent.getProps().getProperty("imp.read");
   }
  public String getImportFileNFC(){
	   return parent.getProps().getProperty("nfc.read");
	   }

  public String identifyCustomer(String id,String regnr) throws SQLException {
    String acc = null;
    return acc;
  }

  public String identifyProduct(String ean, String prodID) throws SQLException{
    return null;
  }


  
  private String getVendType(String company) {
	  String res = "0";
	  

	    String sql2 = " select companytype  as kood from TRDestinationTable where companyref= '" + company + "'";
	   
   try {
	    Statement st2 = con5.createStatement();
	    ResultSet rs2 = st2.executeQuery(sql2);

	    while (rs2.next()) {
	      res  = rs2.getInt("kood")+"";
	      if(res.equals("1")) {
	    	  res = "0";
	      }else {
	    	  res = "1";
	      }
	      
	    }

	    rs2.close();
	    st2.close();
   }catch(Exception j) {
	j.printStackTrace();
   }

     
	  return res;
  }

  
  private String getStation(String company) {
	  String res = company;
	  

	    String sql2 = " SELECT   stationid_bbs AS kood  FROM  vendtable where dataareaid='zp' and  accountnum =  '" + company + "'";
	   
   try {
	    Statement st2 = con5.createStatement();
	    ResultSet rs2 = st2.executeQuery(sql2);

	    while (rs2.next()) {
	      res  = rs2.getInt("kood")+"";
	    }

	    rs2.close();
	    st2.close();
   }catch(Exception j) {
	j.printStackTrace();
   }

     
	  return res;
  }


  public void makeOrder(Order order) throws SQLException{
	 
 
    makeMilkTransportOrder(order);  
  //  updateCustNFC();
  }

  
	public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}
	  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
	
	   private void makeMilkTransportOrder(Order order){
		   String extDoc = " ";
		   String desttype = " ";
		   String driverStock = order.getAgent();
		   String trip = "1";
		  	try{
		    	  StringBuffer sb = new StringBuffer();
		  		  Calendar cal = order.getShipCal();
		  	    Calendar tod = Calendar.getInstance();
		  	    if(tod.getTime().before(cal.getTime())) {
		  	    	cal.setTime(tod.getTime());
		  	    }
		  	        String month = (cal.get(Calendar.MONTH) + 1) + "";
			        if (month.length() == 1) month = "0" + month;
			        String day = cal.get(Calendar.DAY_OF_MONTH) + "";
			        if (day.length() == 1) day = "0" + day;
			        
			        
			        
			    String dateString = cal.get(Calendar.YEAR) +"."+ month +"."+ day ;	
			    String dateString2 = cal.get(Calendar.YEAR) +""+ month +""+ day ;	
		        
			    
		        boolean refe = false;
		        String tagastus = " ";
		        if(order.getTeostaja() != null && order.getTeostaja().trim().length() > 0){
		        	tagastus = order.getTeostaja(); 
		        	if(tagastus.contains("(")){
		        		tagastus = tagastus.substring(tagastus.indexOf("(")+1, tagastus.indexOf(")"));
		        	}
		        	 refe = true ;
		        }
		        
		       
		        String comment = " ";
		        if(order.getInfo()!= null && order.getInfo().trim().length() > 0){
		        	comment = order.getInfo();
		        }
		        
		       
                String signa = " ";
		        if(order.getSignaname() != null && order.getSignaname().trim().length() > 0){
		        	signa = order.getSignaname();
		        }
                
               
		        String photo = " ";
		        if(order.getPhotoname() != null && order.getPhotoname().trim().length() > 0){
		        	photo = order.getPhotoname();
		        }
		        
		        
		        String type = "PA";
		        String days = "1";

		        int coun = 0;
		     
		     Iterator iter = order.getOrderLines().iterator();
		    
		       while (iter.hasNext()){
		        OrderLine line = (OrderLine)iter.next();
		        String toode  = line.getToode();
		 	     if(toode!= null){
		 	    	 toode = toode.trim();
		 	     }else{
		 	    	 toode = "";
		 	     }
		 	 
		 	   
		 	     
		 	     if(line.getPartii() != null && line.getPartii().trim().length() > 0){
		 	    	 if(line.getPartii().equals("22")){
		 	    		 type = "A2"; 
		 	    	 }
		 	    	if(line.getPartii().equals("33")){
		 	    		 type = "DZ"; 
		 	    	 }
		 	     }
		 	     
		 	     if((int)line.getPrice() == 222){
		 	    	 days = "2";
		 	     }
		 	     if((int)line.getPrice() == 333){
		 	    	 days = "3";
		 	     }
		 	     
		 	     
		 	    String section = "1";
			     
			   String tripkl = "1";
			   String scan = " ";
			   String koef = "1";
			   String smell = " ";
			   String clean = " ";
			   String sample = " ";
			  
			   System.out.println("UNIT " + line.getUnit());
		 	   if(line.getUnit() != null ) {
		 		  if(line.getUnit().indexOf("ss:") != -1) {
		 			   section = line.getUnit().substring(line.getUnit().indexOf("ss:")+3, line.getUnit().indexOf("ss:") + 4);
		 		   }
		 		   if(line.getUnit().indexOf("ti:") != -1) {
		 			  trip = line.getUnit().substring(line.getUnit().indexOf("ti:")+3, line.getUnit().indexOf("ti:") + 4); 
		 		   }
		 		  if(line.getUnit().indexOf("tk:") != -1) {
		 			 tripkl = line.getUnit().substring(line.getUnit().indexOf("tk:")+3, line.getUnit().indexOf("tk:") + 4); 
		 		   }
		 		  if(line.getUnit().indexOf("sc:") != -1) {
		 			  scan = line.getUnit().substring(line.getUnit().indexOf("sc:")+3, line.getUnit().indexOf("sc:") + 11); 
		 		   }
		 		  
		 		  //"ko:" + koef + "sm:" + smell + "cl:" + clean + "sa:" + sample
		 		  if(line.getUnit().indexOf("ko:") != -1) {
		 			  koef = line.getUnit().substring(line.getUnit().indexOf("ko:")+3, line.getUnit().indexOf("sm:")); 
		 		   }
		 		  if(line.getUnit().indexOf("sm:") != -1) {
		 			  smell = line.getUnit().substring(line.getUnit().indexOf("sm:")+3, line.getUnit().indexOf("cl:")); 
		 		   }
		 		  if(line.getUnit().indexOf("cl:") != -1) {
		 			  clean = line.getUnit().substring(line.getUnit().indexOf("cl:")+3, line.getUnit().indexOf("sa:")); 
		 		   }
		 		  
		 		  if(line.getUnit().indexOf("sa:") != -1) {
		 			  sample = line.getUnit().substring(line.getUnit().indexOf("sa:")+3, line.getUnit().indexOf("ed:")); 
		 		   }
		 		 if(line.getUnit().indexOf("ed:") != -1) {
		 			  extDoc = line.getUnit().substring(line.getUnit().indexOf("ed:")+3); 
		 		   }
		 	   }
		 	     
		 	     String company = order.getCompany();
		 	    //  waybill = " ";
		 	     if(company.indexOf("--") != -1) {
		 	    	// waybill = company.substring(company.indexOf("--") + 2, company.length());
		 	    	 company = company.substring(0, company.indexOf("--"));
		 	     }
		 	     
		 	  
		 	     while(driverStock.length() < 6) {
		 	    	driverStock = "0" + driverStock;
		 	     }
		        
		 	    if(line.getKogus()!= 0 ){
		 	    	
		 	    	double temp = line.getDiscount();
		 	    	
		 	    	String hour = order.getShipCal().get(Calendar.HOUR_OF_DAY) + "" ;
		 	    	if(hour.length() == 1) {
		 	    		hour = "0" + hour;
		 	    	}
		 	    	String minutes = order.getShipCal().get(Calendar.MINUTE) + "" ;
		 	    	if(minutes.length() == 1) {
		 	    		minutes = "0" + minutes;
		 	    	}
		 	    	
		 	    	String time = hour + ":"+ minutes;
		 	    	
		 	    	String vendType = this.getVendType(company);
		 	    	
		 	    	String station = getStation(company);
		 	    	
		 	    	 desttype = " ";
		 	    	String cycle = " ";
		 	    	String tripFrom = "1";
		 	    	String sectionFrom = " ";
		 	    	
		 	    	
		 	    //	3.desttype	место из – esli Vendor , togda  nicego
		 	      //  esli stac  tocka  togda  0
		 	//esli driver to iz barkoda седьмой символ

		 	    System.out.println("Scan " + scan);	
		 	    	if(scan != null && scan.indexOf("000000")== -1) {
		 	    		
		 	    		if(scan.startsWith("1")) {
		 	    			desttype = scan.substring(6,7);
		 	    			tripFrom = scan.substring(7,8);
		 	    		}
		 	    		
		 	    		if(scan.startsWith("7")) {
		 	    			desttype = "0";
		 	    		}
		 	    		
		 	    		
		 	    	}else {
		 	    		tripFrom = tripkl;
		 	    		if(vendType.equals("1")) {
		 	    			desttype = "0";
		 	    		}
		 	    	}
		 	    	//4.	цикл из - последний (восьмой) символ из barкода поставщика
                  
		        sb.append(dateString + ";" + extDoc + ";" + extDoc.substring(3) + ";" + time + ";P;" +  type + ";ZP;"+ vendType + ";" +  company + ";" +
		        
		        desttype + ";" + tripFrom  +  ";" + driverStock + ";" +  section + ";" + trip +  ";"+ Converter.Round((float)line.getKogus(), 1) + ";" + days + ";" + station +";" + signa + ";" + photo
		           + ";" + comment + ";" + temp + ";" +  ";" +  ";" + ";" + ";;;;;;TIE\n"); 
		        	  
		
		//        Data +“;“+ путевой лист.+“;“+ грузовoй лист.+ “;“+ начало сбора +“;“+ Item +“;“+ тип молока + “;“+ место расследования + “;“+ тип (поставщик(0), склад(1))+ “;“+ код из +“;“+ место из +“;“+
		  //      цикл из + “;“+ код то+ “;“+ место то +“;“+ цикл то +“;“+ Количество кг.+“;“+ Количество дней.+“;“+ код точки +“;“+подпись+“;“+ Фото +“;“+Комментарий+“;“+Температура+“;“+ 

		    //    PH+ “;“+ кислотность +“;“+INH1 +“;“+ INH +“;“+ Количество образцов +“;“+ Плохой чувственный показатель + “;“+ Группа чистоты +“;“+ Исследование выполнил(кодID) +“;“+ Исследование подтвердил (кодID)

		      //  +“;“+Признак (лаборатория или нет)

		        	  coun++;
		          }
		        }

		        
		        
		       String fileName = null;

            long uni = Calendar.getInstance().getTimeInMillis();
            
               String loca = parent.getProps().getProperty("conv.dir");
              
               fileName = loca + order.getAgent()+ "-" + order.getCompany() + "-" + (uni + "").trim() +  ".csv";
               
               
               //date_ путевой лист_ код из_ Признак.csv
               fileName =  loca + dateString2 +  "_" + extDoc + "_" +  driverStock + "_" + trip +  "_TIE.csv";
 		      
               if(refe){
            	   loca = parent.getProps().getProperty("conv2.dir");

            	   
    		 	   fileName = loca + order.getAgent()+ "-" + order.getCompany() + "-" + (uni + "").trim() + "_" + trip +   ".csv";
    		 	   System.out.println("tagastus" + fileName);
    		 	  writeToFile(sb.toString(),fileName);
               }else {
            	   File ff = new File (loca);
            	   FileNameFilter filter = new FileNameFilter();
            	     filter.setAgent(extDoc); //order.getAgent() ja meetod tee ka ymner strrt in peale
            	  
            	     
            	     File[] aa = ff.listFiles(filter);
            	   if(aa.length >0) {
            		   for(File f : aa) {

                			   writeToFile(sb.toString(),f.getPath());
                			   System.out.println("vana fail" + fileName);   
            			   
        	           }
            	   }else {
            	 	  // fileName = loca + order.getAgent()+ "-" + order.getCompany() + "-" + (uni + "").trim() +  ".csv";
            	          fileName =  loca + dateString2 +  "_" + extDoc + "_" +  driverStock +  "_" + trip +  "_TIE.csv";
        		 	   System.out.println("uus fail" + fileName);
        		 	  writeToFile(sb.toString(),fileName);
            	   }
               }

		       setNumAddedLines(coun); 

			} catch (Exception e) {
				e.printStackTrace();
			}
		 }

	   

	   private boolean isToday(long lll){
	    	Calendar cc = Calendar.getInstance();
	    	cc.set(Calendar.HOUR_OF_DAY, 1);
	    	if(cc.getTime().getTime() < lll){
	    		return true;
	    	}else{
	    		return false ;	
	    	}   	
	    }
	   
		public static void saveFileInUTF8(String filename, StringBuffer sb){
			saveFileInUTF8(filename,sb.toString());
		}
		
		public static void saveFileInUTF8(String filename, String sb){
		      try{
		    	  
		    	  File f = new File(filename);
		          if (f.exists())f.delete();
		      	  Writer out = new BufferedWriter(new OutputStreamWriter(
		      			    new FileOutputStream(filename), "UTF-8"));
		      			try {
		      			    out.write(sb.toString());
		      			} finally {
		      			    out.close();
		      			}
			            	
				    }catch(Exception e){
				    	e.printStackTrace();
				    }
			}
	   
	   
	   public static void writeToFile(String keyString,String textFilePAth) throws IOException {
		    // For output to file
		    File a = new File(textFilePAth);

		    if (!a.exists()) {
		        a.createNewFile();
		    }
		    FileWriter fw = new FileWriter(a.getAbsoluteFile(), true);
		    BufferedWriter bw = new BufferedWriter(fw);
		    bw.append(keyString);
		    bw.newLine();
		    bw.close();
		}
	   
	   
	   private class FileNameFilter  implements FilenameFilter {
			
		     String agent = null;
		     String trip = null;
		
		    @Override
		
		    public boolean accept(File directory, String fileName) {
		    	System.out.println("is Today " + directory.toString() + " " + directory.lastModified() +" " +isToday(directory.lastModified()));
		    	  if(isToday(new File(directory.toString() +"\\"+ fileName).lastModified())) {
		    		  if ( fileName.contains(agent) && fileName.contains("_"+ trip + "_")) {
				          return true;
				        }	  
		    	  }
		               return false;
		    }

			public String getAgent() {
				return agent;
			}

			public void setAgent(String agent) {
				this.agent = agent;
			}
			public void setTrip(String trip) {
				this.trip = trip;
			}
}
 	
  
  public String getDriverVersion(){
    return "AxaZpienasMilkImport";
  }

  

  public boolean isAmount(){
          return isAmount;
  }

  /**
  *
  * @param aUseEAN - the new value for useEAN
  */
  public void setAmount(boolean aAmount){
           isAmount= aAmount;
  }

  private Connection con5;
  private ImportPanel parent;

  private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
