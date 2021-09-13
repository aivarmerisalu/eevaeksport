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
import java.sql.SQLException;
import java.sql.Statement;
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

public class AxaZpienasMilkImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public AxaZpienasMilkImportDriver(ImportPanel parent) {
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




  public void makeOrder(Order order) throws SQLException{
	 
 
    makeMilkOrder(order);  
    updateCustNFC();
  }

  
	public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}
	  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
	
private void updateCustNFC(){

	
	    File dir = new File(getImportFileNFC());
        File[] files = dir.listFiles();
	
        for (int i = 0; i < files.length ; i++){
	      boolean error = false;
	      File file = files[i];

	      if (file.isFile()){
	        String fileName = file.getAbsolutePath();
    
	        try {  
	        FileReader ff = new FileReader(file);
	        BufferedReader in = new BufferedReader(ff);
	        String line = in.readLine();
	        StringTokenizer st = new StringTokenizer(line, ";");
	        String klient = st.nextToken();
	        String nfc = st.nextToken();
	        in.close();
	        ff.close();
	        

	       	String  sql2 = "update vendtable set nfccode_bbs='" + nfc + "' where accountnum = '" + klient + "'" ;
            System.out.println(sql2);
              
            Statement st2 = con5.createStatement();
                st2.executeUpdate(sql2);
                st2.close();
                
    }catch(Exception e){
    	e.printStackTrace();
    	error = true;
    }
    
    
    if( !error && file.exists()){
    	try {
			Converter.copyFile(file, new File(dir + "/done/" + file.getName()) );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	file.delete();
    	System.out.println("File deleted " + fileName);
    }
  
		    }
	      }
	
}
	 

private String turn(String aa) {
	   String res = "";
	   res = aa.substring(6, 10) + "." + aa.substring(3, 5) + "." + aa.substring(0, 2);
	   return res;
}


	   private void makeMilkOrder(Order order){
		    
		 //  loeme sama punkti faili ja kui on olemas siis lisame rea juurde
		   
		  	try{
		    	  StringBuffer sb = new StringBuffer();
		  		  Calendar cal = Calendar.getInstance();
		  		   cal.setTime(new java.util.Date(order.getShipCal().getTimeInMillis()));
			        String month = (cal.get(Calendar.MONTH) + 1) + "";
			        if (month.length() == 1) month = "0" + month;
			        String day = cal.get(Calendar.DAY_OF_MONTH) + "";
			        if (day.length() == 1) day = "0" + day;
			        
			        
			    String dateString = cal.get(Calendar.YEAR) +"."+ month +"."+ day ;		         
		        
			    
			    String emb = turn(order.getEmailBody());
			    System.out.println("Kuupaäe " + emb);
			    
			    dateString = emb;
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
		        String morning = "R";
		        
		       

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
		 	 
		 	     
		 	    if(line.selActCode != null &&  line.selActCode.equals("9")) {
		        	morning = "V";
		        }  
		 	    System.out.println("line.selActCode " + line.selActCode + " " + morning);
		 	     
		 	     if(line.getPartii() != null && line.getPartii().trim().length() > 0){
		 	    	 if(line.getPartii().equals("22")){
		 	    		 type = "EK"; 
		 	    	 }
		 	    	if(line.getPartii().equals("33")){
		 	    		 type = "DZ"; 
		 	    	 }
		 	     }
		 	     
		 	     if((int)line.getPrice() == 222){
		 	    	 days = "2";
		 	     }
		 	     if((int)line.getPrice() == 332){
		 	    	 days = "3";
		 	     }
		 	     
		 	     double temp = 0;
		 	     if(line.getDiscount() > 0) {
		 	    	 temp = line.getDiscount();
		 	     }
		 	   
		 	     
		 	     
		        
		 	    if(line.getKogus()!= 0 ){
                  
		        sb.append(dateString + ";" + order.getAgent() + ";" + order.getCompany() + ";P;" +
		        type + ";" + days + ";" + line.getKogus() + ";" + photo + ";" + signa
		           + ";" + comment +  ";" + morning + ";" + temp + "\n");  //";" + tagastus +
		        	  
		        	  coun++;
		          }
		        }

		        
		        
		       String fileName = null;

            long uni = Calendar.getInstance().getTimeInMillis();
            
               String loca = parent.getProps().getProperty("conv.dir");
              
               fileName = loca + order.getAgent()+ "-" + order.getCompany() + "-" + (uni + "").trim() +  ".csv";
 		      
               if(refe){
            	   loca = parent.getProps().getProperty("conv2.dir");

            	   
    		 	   fileName = loca + order.getAgent()+ "-" + order.getCompany() + "-" + (uni + "").trim() +  ".csv";
    		 	   System.out.println("tagastus" + fileName);
    		 	  writeToFile(sb.toString(),fileName);
               }else {
            	   File ff = new File (loca);
            	   FileNameFilter filter = new FileNameFilter();
            	     filter.setAgent(order.getAgent());
            	  
            	     
            	     File[] aa = ff.listFiles(filter);
            	   if(aa.length >0) {
            		   for(File f : aa) {

                			   writeToFile(sb.toString(),f.getPath());
                			   System.out.println("vana fail" + fileName);   
            			   
        	           }
            	   }else {
            	 	   fileName = loca + order.getAgent()+ "-" + order.getCompany() + "-" + (uni + "").trim() +  ".csv";
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
	    	cc.set(Calendar.HOUR_OF_DAY, 0);
	    	if(cc.getTime().getTime() < lll){
	    		return true;
	    	  } else {
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
		
		    @Override
		
		    public boolean accept(File directory, String fileName) {
		    //	System.out.println("is Today " + directory.toString() + " " + directory.lastModified() +" " +isToday(directory.lastModified()));
		    	  if(isToday(new File(directory.toString() +"\\"+ fileName).lastModified())) {
		    		  if ( fileName.startsWith(agent) ) {
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
