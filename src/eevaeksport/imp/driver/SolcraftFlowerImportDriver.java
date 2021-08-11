package eevaeksport.imp.driver;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */

 import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.standard.PrinterName;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageable;

import eevaeksport.Email;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;


 public class SolcraftFlowerImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

   public SolcraftFlowerImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
     //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
     
     }
     catch (Exception e) {
       parent.showError(e);
     }
   
     testing = "true".equals(parent.getProps().getProperty("testing"));
  
   }
   
   boolean testing = false ;
   
   private Connection getNewCon() throws SQLException{
	      return DriverManager.getConnection(url, "SYSDBA", "masterkey");
	    }

   public String identifyCustomer(String id,String regnr)throws SQLException {
    String custid = null;
   	return custid;
   }

   
   public String identifyProduct(String ean, String prodID) throws SQLException{
    return prodID;
   }

   public void makeOrder(Order order) throws SQLException{
     setOrder(order);
     linetxt = "";
     custname = null;
     custcode = null;
     
     int salesid = addInvoiceHeader(null) ;
   if(salesid != -1){
	
    String comment =  addInvoiceLines(salesid +"");
    
     System.out.println("Comment " + comment);
     String sql4 = "update MS_ORDERHEADER set ISREADYFORIMPORT=1 where DOCID = ?";
     Connection con = this.getNewCon();
     PreparedStatement st2 = con.prepareStatement(sql4);
     st2.setString(1, salesid+"");
     try{
     	 st2.executeUpdate();
     }catch(Exception e){
    	 try{
    		 st2.executeUpdate(); 
    	 } catch(Exception ee){
    		 st2.executeUpdate(); 
    	 }
     }
     st2.close();
     con.close();
     try{
    
    	 if(comment.length() > 2){
    		 sendCommentEmail(comment ,custname,order.getAgent());
    	 }
                    		
        	}catch(Exception e){
        		e.printStackTrace();
        	}
  			
     }
   
 
	    printOrder( order);
    
   
   }

   
   
   private void sendCommentEmail(String comment, String nimi, 
			  String agent ){
	  	try{
	  		
	  		
	  		String email = (parent.getEksportFrame().getEksportPanel().parseSecondEmail(
	  				parent.getEksportFrame().getEksportPanel().getEmail(agent)));
	  			Email mail = new Email(null);
		  		   
		  	  	mail.setSMTP(parent.getProps().getProperty("smtphost"));
	            mail.setUser(parent.getProps().getProperty("smtpuser"));
	            mail.setPass(parent.getProps().getProperty("smtppass"));
	            mail.setPort(parent.getProps().getProperty("smtpport"));
	            
		  	  		mail.setFilesTo(email);
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		mail.setSubjectFilesTo(  agent + " Problematic Items" );
			  	  	
		  	    		mail.setBody("\n" + nimi + " " + agent  +
		  	  				  "\n " + comment  
		  	  				);	
		  	  		
		  	  	System.out.println("\n" + nimi + " " + agent + "\n "+  "\n" +
	  	  				  "\n >>>>>" + comment + "<<<<<\n\n  " 
		  				);
		  	  	
		  	  		mail.setSendNotice(false);
		  	  		mail.send();
		  	  		
		  				

	  	
               	  	}catch(Exception e){
		  		    	e.printStackTrace();	
		  			}
		  	
	  }

   
   private void printOrder(Order ord){
	
	   
	  
	  String html = Converter.cleanLitLatEst(getOOSEmailText(ord));
	   
	  String filename = parent.getProps().getProperty("watcher") + "PRINT1-"
			   + Calendar.getInstance().getTimeInMillis() + ".html";
	   Converter.saveFile(filename, html);
	   try {
		   sendToPrinter(filename,html );
		} catch (Exception e) {
			e.printStackTrace();
		}  
	   
	   File f = new File(filename);
		  if(f.exists()){
			  File uus = new File(parent.getProps().getProperty("watcher.rename") + File.separator  + f.getName());
			  
			  if(uus.exists()){
				  uus.delete();
			  }
			  f.renameTo(uus);
			//  f.delete();
			  System.out.println("Renamed " + uus.getAbsolutePath());
		  }
   }
   

	private String getOOSEmailText( Order ord) {
		int i = 0;
		int j = 0;
		 String text = order.getInfo();
		  
		   if(text != null && text.trim().length() > 0){
			   text = text.trim();
		   }else{
			   text = "";	
			   }
		
		StringBuffer sb = new StringBuffer();
	    sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\"><b>Klients:</b> " + custname + "</font> ") ;
	    sb.append("<font face=\"Arial\" size=\"2\" color=\"#000000\">&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;" + custAadr + " </font>") ;
		sb.append("</font>");
	    sb.append("<font face=\"Arial\" size=\"2\" color=\"#E42217\"><span style=\"float: right\">  Data "
				+ Converter.getDDpMMpYYYYpString(ord.getShipCal()) + "</span></font><br> ") ;
	    	
	
		sb.append("<font face=\"Arial\" size=\"2\" color=\"#000000\"><span style=\"float: right\"> <b>" 
				+ ("Agents ") + ":</b> " + agentname  + " " +  ord.getAgent()+ "</span></font>") ; ;
   
		
		sb.append("<font face=\"Arial\" size=\"4\"  color=\"#382D2C\"><b> Kommentars : " + text + " </b><br><br>" );
														
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
		sb.append("<tr><th>Nr.</th><th>Code</th><th>Prece</th><th>Skaits</th><th></th></tr>" );

		
			for (; i < ord.getOrderLines().size(); i++) {
				OrderLine line = (OrderLine) ord.getOrderLines().elementAt(i);
		
					sb.append("<tr><td>" + (i+1) + ".</td><td>" + line.getToode() + 
							"</td><td><div style=\"min-width: 250px\" >" + line.getTooteNimi() + "</div></td>"
									+ "<td><div style=\"min-width: 150px\" >" + line.getKogus() + "</div></td>"
											+ "<td><div style=\"min-width: 200px\" ></div></td></tr>" );
				j++;
			}
		return sb.toString();
	}


   

public void sendToPrinter(String fileName, String text) {
    //Convert to 
    
    String fileNameWithPath = fileName.substring(0,fileName.length()-5)+".pdf";
    
	try {

    
  ProcessBuilder pb = new ProcessBuilder("wkhtmltopdf.exe", fileName, fileNameWithPath);
  Process process = pb.start();

  BufferedReader errStreamReader = new BufferedReader(new  InputStreamReader(process.getErrorStream())); 
  //not "process.getInputStream()" 
  String line = errStreamReader.readLine(); 
  while(line != null) 
  { 
      System.out.println(line); //or whatever else
      line = errStreamReader.readLine(); 
  }
    
  
    } catch (Exception x) {
        x.printStackTrace();
    }
    
    
    
    try {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        PageFormat pageFormat = new PageFormat();
        pageFormat.setOrientation(PageFormat.PORTRAIT);
        Paper paper = new Paper(); 
        paper.setSize(595, 842);
        paper.setImageableArea(0, 0, 595, 842);
        pageFormat.setPaper(paper);
        

        AttributeSet aset = new HashAttributeSet();
        aset.add(new PrinterName(parent.getProps().getProperty("printer.name"), null));
        PrintService[] pservices = 
            PrintServiceLookup.lookupPrintServices(null, aset);
        
        PDDocument doc = PDDocument.load(new File(fileNameWithPath));
       PrintService printService = pservices[0];
        printJob.setPrintService(printService);
        printJob.setPrintable(new PDPageable(doc), pageFormat);

       // if (printJob.printDialog()) {
            printJob.print();
       // }
        doc.close();
        
        File f = new File(fileNameWithPath);
		  if(f.exists()){
			  File uus = new File(parent.getProps().getProperty("watcher.rename") + File.separator  + f.getName());
			  if(uus.exists()){
				  uus.delete();
			  }
			  f.renameTo(uus);
			  System.out.println("Renamed " + uus.getAbsolutePath());
		  }

    } catch (Exception x) {
        x.printStackTrace();
    }

}
   

   private String  addInvoiceLines(String salesid) throws SQLException{
   String res = "";
     Enumeration enums = order.getOrderLines().elements();
     int counter = 0;
     Connection con3 = this.getNewCon();
     while (enums.hasMoreElements()){
       counter++;
       log.info("ORDER_LINE_COUNTER:" + counter);
       OrderLine orderLine = (OrderLine)enums.nextElement();
       String itemid = orderLine.getToode();
       double kogus = orderLine.getKogus();
       double hind = orderLine.getPrice();
       double ale = orderLine.getDiscount();
       String prodname = getProdName(itemid);   
       orderLine.setTooteNimi(prodname);

       int changed = 0;
        	if(ale > 0){
        		changed = 1;
        	}
         
         	String sql = "INSERT INTO MS_ORDERLINES (DOCID,LINENUM,PRODID,AMOUNT,PRICE,DISCOUNT,UNIT,D_CHANGED, P_CHANGED )" +
             " VALUES( " +
               salesid + "," +
               counter + ",'" +
               itemid + "'," +
               kogus + "," +
               hind +"," + ale + ",'" + orderLine.getUnit() + "'," +changed + ",0" + ")";
                System.out.println(sql);
               log.info("SALES_LINE_SQL: " + sql);
               
               PreparedStatement st2 = con3.prepareStatement(sql);
               st2.executeUpdate();
               setAmount(false);
               addNumLine();
               st2.close();
               linetxt = linetxt + "\r\n[ " +  orderLine.getKogus() + " ] "  + orderLine.getToode() + " " + prodname ; 
        
               String  cmd = " select  sum(s.atlikums) as stock  " +
            	 		"  from fwm_atlikumi s INNER JOIN PRECES t on s.preces_kods=t.kods  "
            	 		+ " where t.artikuls = '" + orderLine.getToode() + "' group by t.artikuls having  sum(s.atlikums)>0 " ;
               
               Statement st = con3.createStatement();
               ResultSet rs = st.executeQuery(cmd);
               while(rs.next()){
            	   double stock  = rs.getDouble("stock");
            	   if(stock != 0 && stock < kogus){
            		   res = res + "[ " + kogus + " -> " + stock + " ] " + prodname + " \n"; 
            	   }
               }
                  
     }
     con3.close();
     
     System.out.println("emaili text " + res);
     
     return res;
     }


String custname = null;
String custcode = null;
String custAadr = null;
String agentname = null;
String linetxt = "";

   private int  addInvoiceHeader(String salesid) throws SQLException{
 boolean entered  = false;  	
   	String agent = order.getAgent();
   	System.out.println("agent " + agent);
    String cc = order.getCompany();
    String cust = cc.substring(0, cc.indexOf("_"));
    String adr = cc.substring(cc.indexOf("_")+1);
   
    String func = "";
    String oper = "";
    String aoper = "";
    
    if(order.getPaymTerm() > 0){
    	int res = order.getPaymTerm(); 
    	if(res == 1){
    		 func = "16";
       	     oper = "KR2";
   	         aoper = "SKR";
    	}
    	if(res == 2){
   		 func = "2";
      	     oper = "KRP";
  	         aoper = "PRP";
   	         }
    	if(res == 3){
   		 func = "2";
      	     oper = "KRP";
  	         aoper = "SKR";
   	}
    	if(res == 4){
      		 func = "2";
         	     oper = "KRP";
     	}
    	if(res == 5){
      		 func = "11";
         	     oper = "NNZ";
     	}
    }
    System.out.println("lisad " + func + " " + oper + " " + aoper);

	String sql = "SELECT a.kods , a.nosauk , a.adrese ,b.nosauk as nimi  FROM adreses a left join kontr  b "
			+ "on b.num=a.agents WHERE (pkonts)='" + cust + "'";

	log.info("addInvoiceHeader: " + sql);
	Connection con = this.getNewCon();
       Statement st = con.createStatement();
       ResultSet rs = st.executeQuery(sql);
       String text = order.getInfo();
       
       String reference = order.getTeostaja();
       if(reference == null ){
    	   reference = "";
       }
       
       if (text == null){
             text = "";
              }else{
            	  if(text.length()> 240){
            		  text = text.substring(0,240);
            	  }
              }
       
String ladu = "001";
if(order.getWarehouse() != null && order.getWarehouse().trim().length() > 0){
	//ladu = order.getWarehouse();
}
       
       while(rs.next()){
         String custCode = rs.getString("kods").trim();
         String custName = rs.getString("nosauk").trim();
         String custAadr = rs.getString("adrese").trim();
         this.custcode = custCode;
         this.custname = custName;
         this.custAadr = custAadr;
         this.agentname = rs.getString("nimi");
         Calendar cal = Calendar.getInstance();
         String today  = Converter.getYYYYmMMmDDmString(cal.getTime());
 
         if(testing){
        	 text = "AIVAR TESTING DO NOT SEND FLOWERS - DELETE IT ";
         }
         
          sql = "INSERT INTO MS_ORDERHEADER (WAREHOUSE,DOCNUMBER,ORDERDATE,DELIVERYDATE,CUSTID,ADRID" +
              "  ,SALESPERSON,COMM,REFERENCE,FUNC,OPER,AOPER) " +
              "VALUES ('" + ladu  + "','" + order.getDokNr()+ "', '" + today + "' ,? " +  // " + salesid + "
              "," + custCode + "," + adr + ",'"   + agent + // " + adr + " nulli asemel 
  			 "','" + text + "','" + reference + "','" + func + "','" + oper + "','" +aoper +"')";

     log.info("addInvoiceHeader2: " + sql);
     System.out.println("addInvoiceHeader2: " + sql);
     Connection con2 = this.getNewCon();
         PreparedStatement stmt2 = con2.prepareStatement(sql);
         stmt2.setTimestamp(1, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
         stmt2.executeUpdate();
         stmt2.close();
         con2.close();
         entered = true;
         addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
       }
       st.close();
       con.close();
       if(entered){
    	   return this.getSalesid();
    	}else{
    	   return -1;
    	   
       }
       
   }
   

   public String getProdName(String prodid) throws SQLException{
	     
	     String cmd = "SELECT pr.artikuls AS toode,pr.nosauk AS nimi " +
	 			" FROM  PRECES pr  WHERE  pr.artikuls = '" + prodid+ "'";
	     System.out.println(cmd);
	     Connection con = this.getNewCon();
	     PreparedStatement st2 = con.prepareStatement(cmd);
	     ResultSet rs = st2.executeQuery();
	     String result = null ;
	      while (rs.next()){
	       result =  rs.getString("nimi");
	      }
	     rs.close();
	     st2.close();
	     con.close();
	     return result;
	   }

   public int getSalesid() throws SQLException{
	     String cmd = "SELECT max (DOCID) AS id from MS_ORDERHEADER "; 
	     Connection con = this.getNewCon();
	     PreparedStatement st2 = con.prepareStatement(cmd);
	     ResultSet rs = st2.executeQuery();
	     int result = 0 ;
	     while (rs.next()){
	     result =  rs.getInt("id");
	      }
	     rs.close();
	     st2.close();
	     con.close();
	     return result;
	   }

   public String getDriverVersion(){
     return "SolcraftFlower";
   }

   public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
 
   
   private void setOrder(Order order){
     this.order = order;
   }

   private void addLog(String line){
     parent.addLog(line);
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

   private String url = "jdbc:odbc:solcraft32";
   
   private ImportPanel parent;
   private Order order = null;
   private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
  
 }

