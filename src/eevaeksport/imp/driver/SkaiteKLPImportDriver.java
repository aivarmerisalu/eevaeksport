/*
 * Created on 13.09.2007
 */
package eevaeksport.imp.driver;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */

import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Email;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;


 public class SkaiteKLPImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SkaiteEugImportDriver.class.getName());

   public SkaiteKLPImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
   
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
    }
     catch (Exception e) {
       parent.showError(e);
     }
     
     
   }

   
   public String identifyCustomer(String id,String regnr)throws SQLException {
	    String custid = null;    
	    

	    String cmd = "select PartneriaiId as kood from Partneriai where kodas ='" + id + "'" ; 
	   
	  try{
		Connection  con = this.getNewConnection();
		    PreparedStatement st2 = con.prepareStatement(cmd);
		    ResultSet rs = st2.executeQuery();
		    System.out.println(cmd);
		    while (rs.next()){
		    	custid =  rs.getString("kood");
		     }
		    rs.close();
		    st2.close();
		    con.close();
		    
		    System.out.println("custid" +custid + " " +  id.trim().indexOf(" "));
		    
	  }catch(Exception ee){
		  ee.printStackTrace();
	  }


	 	return custid;
	   }


   public String identifyCustomerType(String id)throws SQLException {
    String custid = null;    
    

    String cmd = "select Tipas3  as kood from Partneriai part INNER JOIN  " +
                      " dbo.PL_PARTNERIAI AS pl ON part.PartneriaiId = pl.PARTNERIAIID  where part.kodas ='" + id + "'" ; 
   
  try{
	Connection  con = this.getNewConnection();
	    PreparedStatement st2 = con.prepareStatement(cmd);
	    ResultSet rs = st2.executeQuery();
	    System.out.println(cmd);
	    while (rs.next()){
	    	custid =  rs.getString("kood");
	     }
	    rs.close();
	    st2.close();
	    con.close();
	    
	    System.out.println("custid" +custid + " " +  id.trim().indexOf(" "));
	    
  }catch(Exception ee){
	  ee.printStackTrace();
  }


 	return custid;
   }
   
   public String identifyAgentID(String agent) throws SQLException {
		 
		  String  sql = "SELECT  masm.MusuAsmenysId  AS EmployeeAsId  FROM dbo.MusuAsmenys AS masm " + 
		" where  masm.Kodas  = '"+ agent +"'";
		  String res = "1111";
		  Connection  con = this.getNewConnection();
		  PreparedStatement st2 = con.prepareStatement(sql);
		    ResultSet rs = st2.executeQuery();
		 
		    while (rs.next()){
		    	res =  rs.getString("EmployeeAsId").trim();
		     }
		    rs.close();
		    st2.close();
		    con.close();
		   
		  return res;
	   }
	
   public String identifyWareID(String ware) throws SQLException {
		 
	
		  String  sql = "SELECT      masm.MusuAsmenysId  AS EmployeeAsId, masm.Kodas AS yksus, " +
  		" LTRIM(masm.Vardas + ' ' + masm.Pavarde) AS nimi,abi.MOBIL_Sand  as ladu ,MPAL_Email,Padalinys as asi FROM dbo.MusuAsmenys AS masm " + 
			" INNER JOIN pl_musuasmenys abi ON masm.MusuAsmenysId =abi.MusuAsmenysId " +
			" where  masm.Kodas  = '"+ ware +"'";
		  
		  String res = "1111";
		  Connection  con = this.getNewConnection();
		  PreparedStatement st2 = con.prepareStatement(sql);
		    ResultSet rs = st2.executeQuery();
		 
		    while (rs.next()){
		    	res =  rs.getString("ladu").trim();
		     }
		    rs.close();
		    st2.close();
		    con.close();
		   
		  return res;
	   }
   
   public String identifyWareCode(String ware) throws SQLException {
		 
		
		  String  sql = "SELECT      masm.MusuAsmenysId  AS EmployeeAsId, sand.Kodas AS yksus, \r\n"
		  		+ "		 LTRIM(masm.Vardas + ' ' + masm.Pavarde) AS nimi,abi.MOBIL_Sand  as ladu ,MPAL_Email,Padalinys as asi,* FROM dbo.MusuAsmenys AS masm\r\n"
		  		+ "			 INNER JOIN pl_musuasmenys abi ON masm.MusuAsmenysId =abi.MusuAsmenysId INNER JOIN dbo.Sandeliai AS sand on sand.SandeliaiId = abi.MOBIL_Sand \r\n"
		  		+ "			 where  masm.Kodas  ='"+ ware +"'";
		  
		  String res = "1111";
		  Connection  con = this.getNewConnection();
		  PreparedStatement st2 = con.prepareStatement(sql);
		    ResultSet rs = st2.executeQuery();
		 
		    while (rs.next()){
		    	res =  rs.getString("yksus").trim();
		     }
		    rs.close();
		    st2.close();
		    con.close();
		   
		  return res;
	   }
   
   
   
   public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  

   public String identifyProduct(String ean, String prodID) throws SQLException{

	

  	String sql2 = "SELECT PrekesId AS toode FROM Prekes " +
        " where Kodas = '" + prodID + "'";

 
    
    String prod = null;
    Connection  con = this.getNewConnection();
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql2);
    
  
   
    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
    
    rs1.close();
    st1.close();
   con.close();
    return prod;
   }
   
   private  TreeMap<String,Double> getProds() throws SQLException{

		

	  	String sql2 = "SELECT Kodas AS toode , MV_Pastaba as kastis FROM Prekes  where MV_Pastaba > 0";

	 
	    
	    TreeMap<String,Double> prods = new TreeMap<String,Double>();
	    Connection  con = this.getNewConnection();
	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql2);
	   
	     while (rs1.next()) {
	      prods.put(rs1.getString("toode").trim(),rs1.getDouble("kastis"));
	     }
	    
	    rs1.close();
	    st1.close();
	    con.close();
	    
	    return prods;
	   }
	   
   
   public TreeMap getProductNames() throws SQLException{

	
  	String sql2 = "SELECT Kodas AS toode, Pavad AS nimi FROM Prekes ";

    TreeMap res = new TreeMap();
    Connection  con = this.getNewConnection();
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql2);
    
   
    while (rs1.next()) {
      res.put(rs1.getString("toode"), rs1.getString("nimi").trim());
    }
    
    rs1.close();
    st1.close();
   con.close();
    return res;
    
   }

   public void makeOrder(Order order) throws SQLException{
	   
	   
	   nf3.setGroupingUsed(false);
	   nf3.setMaximumFractionDigits(3);
	   nf3.setMinimumFractionDigits(0);
	   
     setOrder(order);
   //  String aiai = parent.getProps().getProperty("testing");
   // 	  String salesid = addInvoiceHeader(doknr,refr,order.getInfo()) + "";
     
     if(order.getCompany().equals("999999")) {
    	 makeOutput2(order); 
     }else {
    	 makeOutput(order);  
     }
     
         	 
     }
     
   private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    sb.append(cal.get(Calendar.YEAR) + "-");
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month + "-");
	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
 	    sb.append(day);
	    return sb.toString();
	  }

   private String getDateTimeString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    sb.append(cal.get(Calendar.YEAR) + "-");
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month + "-");
	//2005.09.05 00:00
	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day + " ");

	    String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
	    if (hour.length() == 1){
	      sb.append("0");
	    }
	    sb.append(hour + ":");
	   

	    String min = String.valueOf(cal.get(Calendar.MINUTE));
	    if (min.length() == 1){
	      sb.append("0");
	    }
	    sb.append(min + ":");
	    

	    String sec = String.valueOf(cal.get(Calendar.SECOND));
	    if (sec.length() == 1){
	      sb.append("0");
	    }
	    sb.append(sec );
	    
	    return sb.toString();
	  }



 
   private void makeOutput(Order order){
	 	StringBuffer sb = new StringBuffer();
	 	StringBuffer sb2 = new StringBuffer();
	    
	 	String ls = System.getProperty("line.separator");
	 	try {
	 	 
	      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + ls);
       String agent = order.getAgent();
	    
	      String userid = "1111";
	      try{
	    	  userid =  identifyAgentID(agent);
	      }catch(Exception e){
	    	  
	      }
	      
	    
	      
	      String partner = "1111";
	      try{
	    	  partner =  identifyCustomer(order.getCompany(),null);
	      }catch(Exception e){
	    	  
	      }
	      sb.append("<ImportData UserID=\"" + userid + "\">" + ls);
	 
			double sum = 0;
			NumberFormat amm = NumberFormat.getInstance(Locale.ENGLISH);
			amm.setGroupingUsed(false);
			amm.setMinimumFractionDigits(4);
	       Vector lines = order.getOrderLines();
	      for (int i = 0; i < lines.size(); i++) {
	      	OrderLine line = (OrderLine)lines.elementAt(i);
	          String toode = identifyProduct(null,line.getToode());
	          double ale = line.getDiscount();
	          if(ale == -2){
	        	  line.setPrice(0.0001);
	        	  ale = 0;
	          }
	          sum = sum + (line.getPrice() * line.getKogus());

	          double price2 = Converter.Round((float) (line.getPrice()*((100-ale)/100)), 2);
	          double price = Converter.Round((float) (line.getPrice()*((100-ale)/100) + 0.0000001), 2);
		         
	          System.out.println("Hind " + price + " " + price2 );
	          double reasum = price * line.getKogus();
	          if(line.getDiscount() == -2){
	        
	        	
	        	  NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);
	        	     nf2.setMaximumFractionDigits(6);
	        	     
	        	    double pr1 =  Converter.Round((float) (0.01/line.getKogus() + 0.0000001), 4);
	        	     
	        	    String pr = ("" +nf2.format(pr1)).replace('.', ',');
	        	 System.out.println("Hind -2 " + price  );
	        		 sb2.append("<Item Nr=\"" + (i+1) + "\" ID=\"" + toode + "\" Amount=\"" +  
	        	   			 escape(amm.format(line.getKogus()).replace('.', ','))+ "\"  Price=\"" 
	        		 		+ pr  + 
	        		 		"\" Discount=\"" + escape(nf.format(ale).replace('.', ','))+"\" Price2=\"" + pr +
	        		 		"\" Summ=\"" + "0,01" + "\" Notes=\"\" /> " + ls);
	        	    
	          }else{
	        		 sb2.append("<Item Nr=\"" + (i+1) + "\" ID=\"" + toode + "\" Amount=\"" +  
	        	   			 escape(amm.format(line.getKogus()).replace('.', ','))+ "\"  Price=\"" 
	        		 		+ escape(nf.format(line.getPrice()).replace('.', ',')) + 
	        		 		"\" Discount=\"" + escape(nf.format(ale).replace('.', ','))+"\" Price2=\"" + escape(nf.format(price).replace('.', ',')) +
	        		 		"\" Summ=\"" + escape(nf.format(reasum).replace('.', ',')) + "\" Notes=\"\" /> " + ls);
	        	            	  
	          }
	          if(sum < 0.01){
	        	  sum = 0.01;
	          }
	          
    	  addNumLine();
	      }
	      
	      String ware = identifyWareID(order.getAgent());//order.getWarehouse()
	      String typ = "50";
	      String type = this.identifyCustomerType(order.getCompany());
	      if(type != null && type.trim().length() > 0 && "KASA".equals(type.trim())){
	    	  typ = "45";
	      }

	      if(order.getAgent().startsWith("Z-")) {
	    	  typ = "53";
	      }
	      
	      String doknr = order.getDokNr();
	      if(doknr.contains(" 202") && !doknr.contains("P")) {
	    	  doknr =  doknr.substring(0 ,doknr.indexOf(" "));
	      } else if(doknr.contains(" 202") ) {
	    	  doknr = "U00" + doknr.substring(0 ,doknr.indexOf(" "));
	      }
	      
	      String reference = "";
	      if(order.getTeostaja() != null && order.getTeostaja().trim().length() > 0 ){
	    	  reference = order.getTeostaja().replace('|', ' ' ).replace('|', ' ' ).trim(); 
	    	  if(reference.contains(" 202") && !reference.contains("P")) {
		    	  reference =  reference.substring(0 ,reference.indexOf(" "));
		      }else if(reference.contains(" 202") ) {
		    	  reference = "U00" + reference.substring(0 ,reference.indexOf(" "));
		      }
	        reference = "##" + reference;
	      }
	     
	      System.out.println(doknr + " " + reference);
	      
	      sb.append("<OrderHeader ID=\""+ Calendar.getInstance().getTimeInMillis() + "\"  PartnerID=\"" + partner + "\" " +
	      		" DateFinished=\"" +  getDateTimeString(order.getShipCal()) + "\" DateDelivery=\"" +  getDateString(order.getShipCal()) +
	      		"\"" + 
	    		  "  SummTotal=\"" + (sum+"").replace('.', ',') + "\" Notes=\"" + doknr + reference + "\"" +
	    		  		" CashDocument=\"\" CashSumm=\"0\" TypeID=\""+ typ + "\" PartnerNotes=\" \"  WarehouseID=\"" + ware + "\"  >"+ ls);
	      sb.append(sb2);
	      
	      sb.append("</OrderHeader> " + ls + " </ImportData>" + ls);
	  
	      String fileName = escape(parent.getProps().getProperty("conv.dir") + order.getAgent())  + "-" + ( Calendar.getInstance().getTimeInMillis()+"").substring(4,13)   +  ".xml";
	        if (order.getAgent().toLowerCase().indexOf("z-") != -1) {
	        	 FileOutputStream fos = new FileOutputStream(fileName);
                 fos.write(sb.toString().getBytes());
                 fos.close();
	        }else {
	        	 if(order.getDokNr().toLowerCase().indexOf("-") == -1 ){ // kui pole miinust sees ja pol e z-
	   	    	  fileName = escape(parent.getProps().getProperty("conv.dir2") + order.getAgent())  + "-" + ( Calendar.getInstance().getTimeInMillis()+"").substring(4,13)   +  ".txt";
	            	  FileOutputStream fos = new FileOutputStream(fileName);
	                 fos.write(sb.toString().getBytes());
	                 fos.close();
	            }else{
	            	 FileOutputStream fos = new FileOutputStream(fileName);
	                 fos.write(sb.toString().getBytes());
	                 fos.close();
	            }
	        }
	     
	     
         //   System.out.println(fileName + " zzzz " + order.getDokNr() + " zzz" + reference);
         
   

		} catch (Exception e) {
			e.printStackTrace();
		}
	 }


   
   private void sendEmail(String body,String subjekt,String to ){
 		Email mail = new Email(null);
 		mail.setSMTP(parent.getProps().getProperty("smtphost"));
 		mail.setFrom(parent.getProps().getProperty("from"));
 		mail.setSubjectFilesTo(subjekt);
 		mail.setEncoding("UTF-8");
 		//mail.addAttach(f.getAbsolutePath());
 		mail.setSendNotice(false);
 		try {
 			mail.setBody(body);
 		} catch (MessagingException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		
 		StringTokenizer st = new StringTokenizer(to,",");
 		while(st.hasMoreTokens()){
 			String mai = st.nextToken();
 			mail.setFilesTo(mai);
 			try {
 				mail.send();
 			} catch (AddressException e) {
 				e.printStackTrace();
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			} catch (MessagingException e) {
 				e.printStackTrace();
 			}
 		}	
   }
   
   private TreeMap<String,Double> prods = null;
   
   private void makeOutput2(Order order){
	   
	   if(prods == null) {
		   try {
			prods = getProds();
		} catch (SQLException e) {
		
			e.printStackTrace();
		}
	   }
	   
	   String agent = order.getAgent();
	   try {
		 	 
	   String ware = identifyWareCode(order.getAgent());
	   
	   String from = "KAUNAS_KP";
	   if(agent.startsWith("KL")) {
		   from = "KLAIPEDA_KP";
	   }else if(agent.startsWith("AL")) {
		   from = "ALYTUS_KP";
	   }else if(agent.startsWith("KA")) {
		   from = "KAUNAS_KP";
	   }else if(agent.startsWith("PA")) {
		   from = "PANEVEZYS_KP";
	   }else if(agent.startsWith("TE")) {
		   from = "TELSIAI_KP";
	   }else if(agent.startsWith("VI")) {
		   from = "VILNIUS_KP";
	   }else if(agent.startsWith("SI")) {
		   from = "SIAULIAI_KP";
	   }else if(agent.startsWith("AN")) {
		   from = "ANYKSCIAI_KP";
	   }
	
	   
	 	StringBuffer sb = new StringBuffer();
	    
	 	String ls = System.getProperty("line.separator");
	 	String telnr = ("" + Calendar.getInstance().getTimeInMillis()).substring(5);
	 	       
			NumberFormat amm = NumberFormat.getInstance(Locale.ENGLISH);
			amm.setGroupingUsed(false);
			amm.setMinimumFractionDigits(4);
	       Vector lines = order.getOrderLines();
	
	       for (int i = 0; i < lines.size(); i++) {
	    	  
	      	  OrderLine line = (OrderLine)lines.elementAt(i);

	          sb.append(agent + "-" + telnr + ";");
	          sb.append(Converter.getYYYYpMMpDDpString(Calendar.getInstance()) + ";");
	          sb.append(telnr + ";");
	          sb.append(from + ";");
	          sb.append(ware + ";");
	          sb.append(agent + ";");
	          sb.append(line.getToode() + ";");
	          sb.append(line.getKogus() + ";");
	        
	          sb.append(line.getUnit() + ";");
	          
	          
	         if(prods != null && prods.get(line.getToode()) != null) {
	        	 sb.append(nf3.format(line.getKogus()/prods.get(line.getToode())) + ";"); 
	         }else {
	        	 sb.append(line.getKogus() + ";");
	         }
	         
	          sb.append("dėž" + ls);
	          

	          addNumLine();
	      }
	      

	      System.out.println(sb.toString());
	      
	      //System.exit(0);

	      String fileName = escape(parent.getProps().getProperty("conv.dirown") + order.getAgent())  + "-" + ( Calendar.getInstance().getTimeInMillis()+"").substring(4,13)   +  ".csv";
	        
	        	 FileOutputStream fos = new FileOutputStream(fileName);
                 fos.write(sb.toString().getBytes());
                 fos.close();
                 System.out.println(parent.getProps().getProperty(ware) + " " + ware);
                 if(parent.getProps().getProperty(from) != null) {
                	 
                	 TreeMap names = getProductNames();               	 
                	 sendEmail("Užsakymas nr: " + order.getAgent() + "-" + telnr +  "\nKlientas : " + agent 
		        			 + "\n\nKiekis     Kodas     Pavad " 
		        			 + getOrderText(order,names) + "\n", 
		        			  " Pakrovos užsakymas sandėliui " + order.getAgent() + "-" + telnr + " " + getDateString(order.getShipCal())
		        			,parent.getProps().getProperty(from)); //mail.trim()
                	 System.out.println("EMAIL SENT");
                 }
	     
                 
                 
            //System.out.println(fileName + " zzzz " + order.getDokNr() + " zzz");
         
   

		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
   
	private String getOrderText(Order order,TreeMap names) {
		String res = "\n";
		  if(prods == null) {
    		  try {
				prods = getProds();
			} catch (SQLException e) {
				
				e.printStackTrace();
			}
    	  }
		  
	//	  System.out.println("prods " + prods.size());
		
		 Iterator iter = order.getOrderLines().iterator();
	        while (iter.hasNext()){
	          OrderLine line = (OrderLine)iter.next();
	          String toode  = line.getToode();
	 	     if(toode!= null){
	 	    	 toode = toode.trim();
	 	     }else{
	 	    	 toode = "";
	 	     }
	 	     String name = line.getTooteNimi();
	 	     if(name.trim().length() == 0){
	 	    	 name = toode;
	 	     }
	 	     if(names != null && names.get(toode) != null){
	 	    	 name = names.get(toode) + "";
	 	     }
	 	     
	 	     
	          if(line.getKogus()!= 0 && !toode.equals("")){
	        	  
	        	  String kaste = "";
	        	  
	        	
	        	  
	        	  if(prods != null && prods.get(toode) != null) {
	        		  kaste =  nf3.format((line.getKogus() / prods.get(toode)));
	        	  }else {
	        		  kaste = "" +((int)line.getKogus());
	        	  }
	        	  
	           String kogus = "[" + ((int)line.getKogus())+"] / [" + kaste +"] ";
	        	  while (kogus.length() < 15) {
	        		  kogus = kogus + " ";
	        	  }
	        	  res = res + kogus + toode + "  " + name + " "  + " \n";  
	          }
	          }
System.out.println(res);
		return res;
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

 

	private Connection getNewConnection() {
		try {
			return DriverManager.getConnection(url + "skaita"
			        ,  "sa", "01234567");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
   
   private boolean repricemode = false;
   

   public String getDriverVersion(){
     return "SkaiteKLP";
   }

   private String getDate(Calendar cal){
   String yy = cal.get(Calendar.YEAR)+"";
   String mm = (cal.get(Calendar.MONTH)+1)+"";
   if(mm.length()==1){
   	mm = "0"+ mm ;
   }
   String dd = (cal.get(Calendar.DAY_OF_MONTH)) +"";
   if(dd.length()==0){
   	dd = "0" + dd;
   }
   return yy+"-"+mm+"-"+dd;
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

   private String url = "jdbc:odbc:";


   private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
   private NumberFormat nf3 = NumberFormat.getInstance(Locale.ENGLISH);
   private ImportPanel parent;
   private Order order = null;
   private boolean isAmount = false;
   private int counter = 0;
   private StringBuffer troubles= new StringBuffer();

public String getTrouble() {
	// TODO Auto-generated method stub
	return troubles.toString();
}
 }

