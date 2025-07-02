/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import eevaeksport.Agent;
import eevaeksport.Contract;
import eevaeksport.ContractLine;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubr
 * @version 1.0
 */
public class RakvereDBDriver extends AbstractDBDriver implements Discounter, Manager,Promo, BulkUpdater,FixLoader{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
	
	
    public RakvereDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        // String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
        //String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
        try {
            Class.forName(databaseDriverName).newInstance();
         ccc = DriverManager.getConnection("jdbc:odbc:scalaDB", "mobileJava", "javajava");
        } catch (Exception e) {
            parent.showError(e);
        }
        user =  getParent().getProps().getProperty("rest.user");
        pass = getParent().getProps().getProperty("rest.pass");
        rcompany = getParent().getProps().getProperty("rest.company");
        rcompanyx = getParent().getProps().getProperty("rest.companyx");
             }
          
          String user =  null;
          String pass = null;
          String rcompany = null; 
          String rcompanyx = null;
          private TreeMap eanprods = null;
          
          
    public String getSaveDebtInfo() {
       	return null;
       }
    

    private HashMap sortimendid = null;
    
    
    
    public String getAssortmentStr(Agent agent){
        StringBuffer sb = new StringBuffer();
         String cmd = "SELECT toode,tahtsus AS type  FROM products  WHERE tahtsus  = 1 or tahtsus = 3 or tahtsus = 2 ORDER BY tahtsus desc,nimi ";
        try {
          Connection con = getNewCon();
         	System.out.println(cmd);
    	        Statement stmt = con.createStatement();
    	        ResultSet rs = stmt.executeQuery(cmd);
    	        while(rs.next()){
            String id = rs.getString("toode");
    			//	sb.append(" ; ;A;" + id + "\n");
    			 }

    	        rs.close();
    	        con.close();
           } catch (Exception e){
          e.printStackTrace();
        }
        return sb.toString();
      }

    public String getSaveStockInfo(){
      // 	sendDistributionReport(null);
       	System.exit(0);
        return null;
    }
    private HashMap prodHash = new HashMap();
    HashMap ketid = new HashMap();
    
    private void sendDistributionReport(Calendar ccc){ //parameeter on selleks et kui on vaja saata mingi muu p�eva aruannet
 	  	  
 	  	  
 	    	getProducts(null);
 	    	getCustomers(null);
 	    	 
 	    	  //1.Fikseeritud sortimendi salvestamine baasi juhul kui tava andmetega pole tehtud 
 	    	 this.getFixedAssortmentData();
 	    	   
 	  	  String saajad = getParent().getProps().getProperty("distribution.reporters");
 	  	  
 	  	  StringTokenizer sto = new StringTokenizer(saajad,",");
 	  	  while (sto.hasMoreElements()){
 	  		
 	  	  String add = "";
 	  	  String regno = sto.nextToken();
 	  	  String toThem = getParent().getProps().getProperty("distribution.email." + regno);
 	  	  
 	  	  if(ccc != null){
 	  		  Calendar cal = Calendar.getInstance();
 	  		  long diff = cal.getTimeInMillis() -ccc.getTimeInMillis();
 	  		  cal.setTimeInMillis(diff);
 	  		  add = "-" + cal.get(Calendar.DAY_OF_YEAR);
 	  	  }
 	  	  //2. t�naste lugemiste 
 	  	  // M��rame t�nased kehtivad sortimendid nii suurused kui ka sisu
 	  	  
 	  	   String sql1 = "select sortgroup, count(prodid) as number from mj_assortment where  " +
 	         "  year(sortdate) =  year (getdate()" + add + ") " +
 	           "   and month(sortdate) =  month (getdate()" + add + ")" + 
 	             "		and day(sortdate) =  day (getdate()" + add + ") and res3 = 'EE' group by sortgroup";
 	  	   
 	  	   String sql2 = "select sortgroup ,prodid  from mj_assortment " +
 	  	   	"	where year(sortdate) =  year (getdate()" + add + " " +
 	        "  ) and month(sortdate) =  month (getdate()" + add + ") " + 
 			"  and day(sortdate) =  day (getdate()" + add + ") and res3 = 'EE' group by sortgroup,prodid";  // t�nase sortimendi leidmine
 	  	 
 	  	   
 	  	   HashMap sortLen = new HashMap();
 	  	   HashMap sortIse = new HashMap();
 	  	   HashMap tnimed = new HashMap();
 	  	   
 	  	   try{
 	  		   Connection con1 = getNewConnection(url);
 	      	   Statement st = con1.createStatement();
 	      	   ResultSet rs = st.executeQuery(sql1);
 	      	   
 	      	   while(rs.next()){
 	      		   sortLen.put(Converter.cleanLitLatEst(rs.getString("sortgroup").trim()),rs.getString("number").trim());
 	      	   }
 	      	   
 	            rs = st.executeQuery(sql2);
 	      	   
 	      	   while(rs.next()){
 	      		   Vector sort = new Vector();
 	      		   String sortim = Converter.cleanLitLatEst(rs.getString("sortgroup").trim());
 	      		   String prodid = rs.getString("prodid").trim();
 	      		   Product pr = ((Product) prodHash.get(prodid));
 	      		   
 	      		 //  System.out.println("toode" + prodid + "+" + pr);
 	      		   if(pr == null){
 	      			   pr = new Product(prodid);
 	      			   pr.setEAN(prodid);
 	      			   pr.setName(prodid + " name missing");
 	      		   }
 	      		   String ean = pr.getEAN();
 	      		   if(pr.getEAN() == null)ean = "";
 	      		   String prname = pr.getName();
 	      		   if(prname == null) prname = "Unknown Product";
 	      		   String prodname = ean + " " + prname;
 	      		   tnimed.put(prodid, prodname);	
 	      		   if(sortIse.get(sortim) != null){
 	      			   sort = (Vector)sortIse.get(sortim);
 	      			   sort.add(prodid);
 	      		   }else{
 	      			   sort.add(prodid);
 	      			   sortIse.put(sortim,sort);  
 	      		   }
 	      		   
 	      	   }
 	      	   
 	      int counter = 0;	   
 	      	   //L�pes sortimentide sisselugemine
 	      	   
 	      	   // vaja leida mitu toodet oli m�rgitud kontrolli ajal kohale 
 	      	   //ja ,m��rame kes k�is mis kaupluses ja kellaaeg
 	      	   
 	      Calendar ccw = Calendar.getInstance();
 	      ccw.add(Calendar.DAY_OF_MONTH, -7);
 	String sql3 = "select prodid ,sup.custid,sup.custname, sup.sentdate as date," +
 	     	" rtrim(sup.agentid) as agid, rtrim(sup.agentname) as agent ,sup.resstr3 as time " +
 	      " from supervisor sup  " +
 	      " where sup.resstr2 = 'EE' and  sup.sentdate > getdate()-14 " +// sup.resstr3 >'" + ccw.getTimeInMillis() + "'" + //
 	      		"order by sup.sentdate desc";
 	      	   HashMap visited = new HashMap();
 	      	   HashMap names = new HashMap();
 	      	   HashMap agen = new HashMap();
 	      	   HashMap sorti = new HashMap();
 	      	   HashMap times = new HashMap();
 	      	   HashMap visitedcoun = new HashMap();
 	      	   Vector days = new Vector();
 	             HashMap puuduvad = new HashMap(); 
 	             rs = st.executeQuery(sql3);
 	             
 	           Vector kliendid = (Vector)ketid.get(regno);
 	          // System.out.println("kliendid " + kliendid + " " + regno );

 	while(rs.next()){
 		   Vector sort = new Vector();
 		   String prodid = rs.getString("prodid").trim();
 		
 		   String custid = rs.getString("custid").trim();
 		   String sortim = (String)sortimendid.get(custid);
 		   String custname = rs.getString("custname").trim();
 		   String date = rs.getString("date").trim().substring(0,10);
 		   String agid =  rs.getString("agid").trim();
 		   String agent = rs.getString("agent").trim();
 		   String time = rs.getString("time");
 		
 		   
 		 if(kliendid.contains(custid)){
 			
 		   if(time != null && time.trim().length() != 0){
 			   long lll = Long.parseLong(time.trim());
 			   Calendar cc = Calendar.getInstance();
 			   cc.setTimeInMillis(lll);
 			   time = getYYYYpMMpDDpTimeNiceString(cc.getTime());
 			   date = time.substring(0,10);
 			   if(!days.contains(date)){
 					  days.addElement(date);
 				  }
 		   }else{
 			   if(!days.contains(date)){
 					  days.addElement(date);
 				  }
 			   time = date + " 00:00:00";
 		   }
 		   	
 		   agen.put(custid, agid + " " + agent);
 		   names.put(custid, custname);
 		   sorti.put(custid, sortim);
 		   times.put(custid+date, time);

 		   if(visitedcoun.get(custid) != null){
 			   Vector dddd = (Vector)visitedcoun.get(custid);
 			   if(!dddd.contains(date))dddd.add(date);
 			   visitedcoun.put(custid,dddd);  
 		   }else{
 			   Vector dddd = new Vector();
 			   dddd.addElement(date);
 			   visitedcoun.put(custid,dddd);  
 		   }	   

 		//Loeme kokku mis olid pudu - m�rgivad puuduvaid tooteid    
 		   if(sortIse.get(sortim) != null){
 			  
 			   if(puuduvad.get(custid + date) == null){
 					   counter++;
 	            Vector puud = new Vector();
 	                 puud.addElement(prodid);
 					   puuduvad.put(custid + date,puud);    
 				   }else{
 					   Vector puud = (Vector) puuduvad.get(custid + date);
 			           puud.addElement(prodid);
 					   puuduvad.put(custid + date,puud);   
 				   }  
 			  
 		   }
 		   if(visited.get(custid+date) != null){
 			   sort = (Vector)visited.get(custid+date);
 			   sort.add(prodid);
 			   visited.put(custid+date,sort);  
 		   }else{
 			   sort.add(prodid);
 			   visited.put(custid+date,sort);  
 		   }	   
 		   
 		 }
 		 } 


 	StringBuffer sb = new StringBuffer();

 	StringBuffer sbf = new StringBuffer();

 	Iterator ite = names.keySet().iterator();
 	int total = 0;
 	double perc = 0;
 	while (ite.hasNext()){

 		String custid = (String)ite.next();
 		
 		String sortcode = (String)sorti.get(custid);
 		sb.append("Customer :\t" + names.get(custid) +  "\n");
 		sb.append("Checked by:\t" + agen.get(custid) +  "\n");
 		
 		sbf.append("Customer :," + names.get(custid) +  "\n");
 		sbf.append("Checked by:," + agen.get(custid) +  "\n");
 		
 		
 		Enumeration ed =  ((Vector)visitedcoun.get(custid)).elements();
 		while(ed.hasMoreElements()){
 			total++;
 			String date = (String)ed.nextElement();
 			sb.append("Time :\t\t" + times.get(custid + date).toString().substring(0,19) +  "\n");	
 			sbf.append("Time :," + times.get(custid + date).toString().substring(0,19) +  "\n");	
 		//System.out.println(sortcode + " " +sortLen);

 			double sss = Double.parseDouble((String)sortLen.get(sortcode));
 			
 			double mmm = Double.parseDouble(((Vector)visited.get(custid+date)).size()+ "");
 			       mmm = sss - mmm;
 		  // kui m�rgivad OOS siis on myygil vahe .
 			       
 			double nnn = mmm/sss*100d;	
 			int per = (int)(nnn);
 			perc = perc + nnn;	
 			sb.append("InSortiment " + (int)sss + " / Available " + (int)mmm + " / " + per + "% \n\n");
 			sbf.append("InSortiment, Available ,% \n");
 			sbf.append((int)sss + "," + (int)mmm + "," + per + "% \n\n\n\n");
 		
 		}
 		    sb.append("\n\n");
 		
 		

 	}


 	//Sortimendi v�rdlus  puuduolevad tooted ja v�ljaspool olevad tooted


 	StringBuffer lisa = new StringBuffer();
 	TreeMap tootedPoed = new TreeMap();
 	StringBuffer lisaf = new StringBuffer();
// 	System.out.println("puuduvad " + puuduvad.keySet());
 	Iterator itera = puuduvad.keySet().iterator();
 	boolean first = true ;
 	while (itera.hasNext()){
 		String id = (String)itera.next();
 	//	System.out.println(id);
 		String paev = id.substring(id.length() - 10);
 	   String custid = id.substring(0,id.length() - 10);
 	//   System.out.println(custid + "ee" + paev);
 	   if(first){
 			lisa.append("Extrainfo MISSING Items\n");
 			lisaf.append("Extrainfo MISSING Items\n");
 			first = false;
 		}
 		Vector vvv = (Vector)puuduvad.get(id);
 		int suurus = ((Vector)sortIse.get((String)sortimendid.get(custid))).size();
 		lisa.append(names.get(custid) + " " + paev + "\nMISSING: " + ( vvv.size()) + " products\n");
 		lisaf.append(names.get(custid) + "," + paev + "\nMISSING:," + ( vvv.size()) + "\n");
 		Enumeration enu = vvv.elements();
 		while(enu.hasMoreElements()){
 			String prodid = (String) enu.nextElement();
 			lisa.append("\t" + prodid + " " + tnimed.get(prodid) + "\n");	
 			lisaf.append(prodid + "," + tnimed.get(prodid) + "\n");
 			if(tootedPoed.get(prodid + " " +  tnimed.get(prodid)) != null){
 				Vector kli= (Vector)tootedPoed.get(prodid + " " +  tnimed.get(prodid));
 				kli.addElement(names.get(custid));
 				tootedPoed.put(prodid + " " +  tnimed.get(prodid), kli);
 			}else{
 				Vector kli= new Vector();
 				kli.addElement(names.get(custid));
 				tootedPoed.put(prodid + " " +  tnimed.get(prodid), kli);	
 			}
 		}
 		lisa.append("\n\n");
 		lisaf.append("\n\n");
 	}
 		

 	StringBuffer toodePood = new StringBuffer();

 	Iterator iteri = tootedPoed.keySet().iterator();
 	while (iteri.hasNext()){
 		String toode = (String)iteri.next();
 		toodePood.append(toode + "\n");
 		
 		Vector vvv = (Vector) 	tootedPoed.get(toode);
 		toodePood.append("missing " + vvv.size() + " in shop\n");
 		
 	 Enumeration enumi = 	vvv.elements();
 	 while (enumi.hasMoreElements()){
 		 toodePood.append("\t"  + enumi.nextElement() + "\n");
 	 }
 	 toodePood.append("\n\n");
 	}


 	if(total == 0 ){
 		total = 1;
 	}
 	String text = "CheckPeriod : " + days.toString() + "\nNumber of Checks  : " + counter 
 	+"\nAverage availability " + (int)perc/total + "%\n\n";
 	String textf = "CheckPeriod : " + days.toString() + "\nNumber of Checks  : " + counter 
 	+"\nAverage availability " + (int)perc/total + "%\n\n";

 //	System.out.println(text + "\n" + sb.toString() + lisa.toString()+ "\n" + toodePood );


 	Calendar ctc = Calendar.getInstance();

 	String filename = "dreports/" +"DR-RLK-" + getYYYYpMMpDDpTimeNiceString(ctc.getTime()).substring(0, 10) + ".csv";  

 	   File f = new File(filename);
 	   if (f.exists())f.delete();
 	     try {
 			PrintWriter os = new PrintWriter(new FileWriter(
 					filename, true));
 			os.write(textf + "\n" + sbf.toString() + "\n" + lisaf.toString());
 			os.close();
 		  }catch (IOException ex) {
 			ex.printStackTrace();
 		  }

 	Email email = new Email(getParent().getString("datasent"));

 	//email.setNoticeTo(parseSecondEmail(getEmail(agentCode)));
 	email.setFrom(getParent().getProps().getProperty("from"));
 	email.setSMTP(getParent().getProps().getProperty("smtphost"));
 	email.setUser(getParent().getProps().getProperty("smtpuser"));
 	email.setPass(getParent().getProps().getProperty("smtppass"));
 	email.setPort(getParent().getProps().getProperty("smtpport"));
 	email.setSubjectFilesTo("Distribution report - Rakvere Lihat��stus AS customer " + regno);
 	email.setBody(Converter.deCode(text + "\n" + sb.toString() + "\n" + lisa.toString() + "\n"+ toodePood.toString()));
 	email.addAttach(filename);
 	System.out.println("Saadan Emaili");
 	//iseendale saatmine
 	String toOwn = getParent().getProps().getProperty("distribution.ownemail");
 	
 if(sb.length() >10){
 	email.setFilesTo(toOwn);
 	email.send();
 	email.setFilesTo(toThem);
 	email.send();
 }
 	
 	      	   
 	  	   }catch(Exception e){
 	  		   e.printStackTrace();
 	  	   }
 	  	   
 	  	   
 	  	  }
 	  	  System.exit(0);
 	    }

 	    private String getYYYYpMMpDDpTimeNiceString(java.util.Date dd){
 		    StringBuffer sb = new StringBuffer();
 		    Calendar cal= Calendar.getInstance();
 		    cal.setTime(dd);
 		    
 		    
 		    
 		    String hour =   cal.get(Calendar.HOUR_OF_DAY) + "";
 		    
 		    
 			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
 			if (day.length() == 1) {day = "0" + day;}

 			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
 			if (month.length() == 1){month = ("0") + month;}

 			sb.append("" + cal.get(Calendar.YEAR));
 			sb.append("-" + month  + "-");
 			sb.append(day +" ");
 		    
 		    String min = String.valueOf(cal.get(Calendar.MINUTE));
 		    String sec = String.valueOf(cal.get(Calendar.SECOND));
 		     if (hour.length() == 1){
 		      sb.append("0");
 		    }
 		     sb.append(hour);
 		     sb.append("_");
 		    if (min.length() == 1){
 		     sb.append("0");
 		   }
 		     sb.append(min);
 		     sb.append("_");
 		     if (sec.length() == 1){
 		        sb.append("0");
 		      }
 		    sb.append(sec);
 		    return sb.toString();
 		  }

    
  	   private   void getProdGroupsMilk(TreeMap groups){
	    	File f = new File(this.getExcelDir() + "tootedmilk.xlsx");
			
			try {
				
				FileInputStream ff = new FileInputStream(f);
				XSSFWorkbook workbook = new XSSFWorkbook((ff));
				XSSFSheet sheet = workbook.getSheet("Tooted");
				XSSFRow row = null;
	  	        DataFormatter formatter = new DataFormatter();

	  			for (int a = 1; a <  sheet.getLastRowNum()+1; a++) {
	  				row = sheet.getRow(a);
					try {
						
						String grupp = formatter.formatCellValue(row.getCell((short) 6)).trim();
						String gname = formatter.formatCellValue(row.getCell((short) 7)).trim();
						groups.put(grupp, Converter.enCodeEst(gname));
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
	  			
			}catch(Exception e ) {
				e.printStackTrace();
			}
	    
	   }   		
 	   
 	   private   void getProductsMilk(TreeMap prods){
 	    	File f = new File(this.getExcelDir() + "tootedmilk.xlsx");
 			
 			try {
 				
 				FileInputStream ff = new FileInputStream(f);
 				XSSFWorkbook workbook = new XSSFWorkbook((ff));
 				XSSFSheet sheet = workbook.getSheet("Tooted");
 				XSSFRow row = null;
 	  	        DataFormatter formatter = new DataFormatter();

 	  			for (int a = 1; a <  sheet.getLastRowNum()+1; a++) {
 	  				row = sheet.getRow(a);
 					try {
 						
 						String kood = formatter.formatCellValue(row.getCell((short) 0)).trim();
 						String nimi = formatter.formatCellValue(row.getCell((short) 2)).trim();
 						String ean = formatter.formatCellValue(row.getCell((short) 3));
 						String unit = "tk.";
 						Double kastis = 1.0 ;
 						try {
 						kastis = Double.parseDouble(formatter.formatCellValue(row.getCell((short) 5)).trim());
 						}catch (Exception r) {
 							
 						}
 						String grupp = formatter.formatCellValue(row.getCell((short) 6)).trim();
 						String gname = formatter.formatCellValue(row.getCell((short) 7)).trim();
 						
 						Double kaal = Double.parseDouble(formatter.formatCellValue(row.getCell((short) 8)).trim());
 						if(nimi.length() > 3) {
 						Product prod = new Product(kood);
 						prod.setUnit(unit);
 						prod.setName(" NM " + nimi);
 						if(ean != null && ean.trim().length() > 4) {
 							prod.setEAN(ean.trim());
 	 						 eanprods.put(ean.trim(), prod.getID());
 	 							
 						}
 						prod.setInCases(kastis);
 						prod.setGroup(grupp);
 						prod.setGroupname(gname);
 						prod.setInUnit(kaal);
 						prod.setVat("24");
 						prod.setType("PIIM");
 						prods.put(" NM " + nimi, prod);
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					
 				}
 	  			
 	  		//	System.exit(0);
 			}catch(Exception e ) {
 				e.printStackTrace();
 			}
 	    
 	   }  
 	   	    
 	    private   TreeMap getVisits(){
 		     TreeMap tm = new TreeMap();
 		     System.out.println("Visiidid alustan");
 		     int a = 0;
 		   try {
 				  File f = new File (getExcelDir() + "VisitSystem.xls");
 					
 				  FileInputStream	 ff = new FileInputStream(f);
 				
 						HSSFWorkbook workbook = new HSSFWorkbook(ff); 
 						HSSFSheet sheet = workbook.getSheetAt(0); 
 						ff.close();	
 						if(sheet != null){
 							for(int i = 0 ; i < sheet.getLastRowNum();i++){
 								HSSFRow row = sheet.getRow(i);
 								//System.out.println(row);
 								if(row == null && a == 0){
 									a = i;
 								}else{
 									a = sheet.getLastRowNum();
 								}
 							}
 							
 							Calendar cal = Calendar.getInstance();
 							int week = cal.get(Calendar.WEEK_OF_YEAR);
 							//taandame 
 							
 							 
 							 if (week >4){
 								 while (week > 4){
 									 week = week -4;
 								 }
 							 }
 							System.out.println("W2 " +  week);
 							
 							for(int i = 1 ; i < a;i++){
 								
 								HSSFRow row = sheet.getRow(i);
 								String custid = null;
 								try {
 									custid = (int) (row.getCell((short) 0)
 											.getNumericCellValue()) + "";
 								} catch (Exception e) {
 									try{
 										custid = (row.getCell((short) 0).getStringCellValue());
 			 										
 									}catch(Exception k ){
 										
 									}
 								}
 								if(custid != null){
 									String paev = getPaev(row , week);
 	 								if(paev != null && paev.trim().length() > 0){
 	 									tm.put(custid, paev);
 	 	   						}	
 	 								//System.out.println("P�ev " + a + " " + i +" " + custid + " *" + paev + " " + week);
 								}
 							
 								//if(paev.equals("K"))
 							
 								
 							}
 						}
 		         		
 	            	  }catch (Exception e){
 	                   e.printStackTrace();	
 		         	  }
 	       
 		   return tm;
 		   }  
 	      
 	    private String getPaev(HSSFRow row , int week){
 	 	   String paev = "";
 	 	   int alg = 4;
 	 	   String w1 = "";
 				if(getVal(row,alg).equals("1")){
 					w1 = "E";
 				}
 				if(getVal(row,alg+1).equals("1")){
 					w1 = w1 + "T";
 				}
 				if(getVal(row,alg+2).equals("1")){
 					w1 = w1+ "K";
 				}
 				
 				if(getVal(row,alg+3).equals("1")){
 					w1 = w1+ "N";
 				}
 				if(getVal(row,alg+4).equals("1")){
 					w1 = w1+ "R";
 				}
 				
 				String w12 = "";
 				if(getVal(row,alg+5).equals("1")){
 					w12 = "E";
 				}
 				if(getVal(row,alg+6).equals("1")){
 					w12 = w12 + "T";
 				}
 				if(getVal(row,alg+7).equals("1")){
 					w12 = w12+ "K";
 				}
 				
 				if(getVal(row,alg+8).equals("1")){
 					w12 = w12+ "N";
 				}
 				if(getVal(row,alg+9).equals("1")){
 					w12 = w12+ "R";
 				}
 				
 				String w13 = "";
 				if(getVal(row,alg+10).equals("1")){
 					w13 = "E";
 				}
 				if(getVal(row,alg+11).equals("1")){
 					w13 = w13 + "T";
 				}
 				if(getVal(row,alg+12).equals("1")){
 					w13 = w13+ "K";
 				}
 				
 				if(getVal(row,alg+13).equals("1")){
 					w13 = w13+ "N";
 				}
 				if(getVal(row,alg+14).equals("1")){
 					w13 = w13+ "R";
 				}
 				
 				String w14 = "";
 				if(getVal(row,alg+15).equals("1")){
 					w14 = "E";
 				}
 				if(getVal(row,alg+16).equals("1")){
 					w14 = w14 + "T";
 				}
 				if(getVal(row,alg+17).equals("1")){
 					w14 = w14+ "K";
 				}
 				
 				if(getVal(row,alg+18).equals("1")){
 					w14 = w14+ "N";
 				}
 				if(getVal(row,alg+19).equals("1")){
 					w14 = w14+ "R";
 				}
 				
 				if(getVal(row,alg+21).equals("1")){
 					return  "KV1-3";
 				}
 				if(getVal(row,alg+22).equals("1")){
 					return  "KV2-4";
 				}
 				
 				//peame vaatama n�dala numbri 
 				if(week == 1 ){
 					return w1;
 				}
 				if(week == 2 ){
 					return w12;
 				}
 				if(week == 3 ){
 					return w13;
 				}
 				if(week == 4 ){
 					return w14;
 				}
 				
 	 	   return paev;
 	    }
 	    
 	    private String getVal(HSSFRow row,int u ){
 	 	   String asi = "";
 	 	   if(row.getCell(u) != null ){
 	 		   try {
 						asi = (int) (row.getCell((short) u).getNumericCellValue()) + "";
 					} catch (Exception e) {
 						asi = (row.getCell((short) u).getStringCellValue());
 					}
 	 		 }
 	 	  return asi ; 
 	    }
 	 
 	    
 	    
    

    

        public String getProductSortingData(){
           	return null;
           }

    	public Vector getLastThreeData() {
    		// TODO Auto-generated method stub
    		return null;
    	}
        
        public String getDealerCustomerCSV(String agent){
            return null;
            }
        
        private Connection getNewConnection(String url) throws SQLException{
            return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
          }

        
        
        private void  insertSortLine(MatrixLine mline  ){
        	  try{
        		  if(mline.getCliGroup() != null && mline.getProdID() != null){
        			  String sql = "INSERT INTO mj_assortment values (" +
     		         "'" + mline.getCustID() + "'," + 
     		         "getdate()," + 
     		         "'" + mline.getCliGroup() + "'," + 
     		         "'" + mline.getProdID() + "'," + 
     		         "0," + 
     		         "0,'EE')" ;
     		  
     		  	     Connection  con2 = getNewConnection(url);      
     			     Statement stmt2 = con2.createStatement();
     			     stmt2.executeUpdate(sql);
     			     stmt2.close();
     			     con2.close(); 
        		  }
        		     
        	  }catch(Exception e){
        		  e.printStackTrace();
        	  }

        	  
        }

     
        private HashMap<String, String> getProdNamesExcel() {
        	HashMap<String, String> res = new HashMap<String, String>();
        	File f = new File(this.getExcelDir() + "sortiments.xls");
			
    		try {
    			
    			FileInputStream ff = new FileInputStream(f);
    			HSSFWorkbook workbook = new HSSFWorkbook((ff));
    			HSSFSheet sheet = workbook.getSheet("Assortment");
    			HSSFRow row = null;

    		    DataFormatter formatter = new DataFormatter();

      			for (int a = 4; a <  sheet.getLastRowNum()+1; a++) {
      				row = sheet.getRow(a);
    				try {
    					// System.out.println(a + " " +formatter.formatCellValue(row.getCell((short) 0)));
    					
    					String kood = formatter.formatCellValue(row.getCell((short) 0)).trim();
    					String nimi = formatter.formatCellValue(row.getCell((short) 1)).trim();
         				
    					res.put(kood,nimi);
    				} catch (Exception e) {
    					 System.out.println(row.getCell((short)a).getNumericCellValue());
    				}
    				
    			}
    		}catch(Exception e ) {
    			e.printStackTrace();
    		}
        	return res;
        }
        
        public Vector getFixedAssortmentData() {
    	return null;
        	/*Vector result = new Vector();
    		this.getProducts(null);
    		File f = new File(this.getExcelDir() + "sortiments.xls");
    				
    		try {
    			
    			FileInputStream ff = new FileInputStream(f);
    			HSSFWorkbook workbook = new HSSFWorkbook((ff));
    			HSSFSheet sheet = workbook.getSheet("Assortment");
    			HSSFRow row = null;
    			
    			HSSFSheet sheetKood = workbook.getSheet("Code");
    			
    			HSSFSheet sheetKoodSort = workbook.getSheet("CodeSorts");
    			
    			TreeMap omakoodid = new TreeMap();

    			TreeMap omagroups = new TreeMap();
    			
    			TreeMap omaTootegroups = new TreeMap();
    			
    			row = sheetKood.getRow(0);
    			// M��RAME �RA MIS V�LJAS ASUVAD TOOTEKOODIDE Sortimendi t�hised.

    			// System.out.println("rownum " + row.getLastCellNum());
    			for (int a = 1; a < row.getLastCellNum(); a++) {
    				try {
    					// System.out.println(row.getCell((short)a).getStringCellValue());
    					omagroups.put(a + "", row.getCell((short) a)
    							.getStringCellValue());
    				} catch (Exception e) {
    					// System.out.println(row.getCell((short)a).getNumericCellValue());
    				}
    			}

    		//	 System.out.println("omagroups " + omagroups.toString());

    			
    			//mis kood on sortimedi koodid ja tootekoodide sortimendi t�hised kokku 
    			for (int i = 0; i < sheetKoodSort.getLastRowNum() + 1; i++) {
    				row = sheetKoodSort.getRow(i);
    				String sortkood = "";
    				if (row.getCell((short) 0) != null) {
                      try {
    						sortkood = (int) (row.getCell((short) 0)
    								.getNumericCellValue()) + "";
    					} catch (Exception e) {
    						sortkood = (row.getCell((short) 0).getStringCellValue());
    					}
    			}
    				String omakood = "";

    				if (row.getCell((short) 1) != null) {

    					try {
    						omakood = (int) (row.getCell((short) 1)
    								.getNumericCellValue()) + "";
    					} catch (Exception e) {
    						omakood = (row.getCell((short) 1).getStringCellValue());
    					}
    					//System.out.println("omakood" + omakood + " " + sortkood);

    			}
    				
    				if(!omakood.equals("") && !sortkood.equals("")){
    					
    						omaTootegroups.put(sortkood, omakood);
    					//	omaTootegroups.put(sortkood, i+"");
    				}
    			}
    				
    	//		 System.out.println("omaTootegroups " + omaTootegroups.toString());
    			
    			TreeMap tulemusKood = new TreeMap();

    			for (int i = 1; i < sheetKood.getLastRowNum(); i++) {
    				row = sheetKood.getRow(i);
    				
    				String prodID = "";
                   if (row.getCell((short) 0) != null) {
                     try {
    						prodID = (int) (row.getCell((short) 0)
    								.getNumericCellValue()) + "";
    					} catch (Exception e) {
    						prodID = (row.getCell((short) 0).getStringCellValue());
    					}
    								}
    				

    				String prodID1 = "";
                   if (row.getCell((short) 1) != null) {
                    	try {
    						prodID1 = (int) (row.getCell((short) 1)
    								.getNumericCellValue()) + "";
    					} catch (Exception e) {
    						prodID1 = (row.getCell((short) 1).getStringCellValue());
    					}
    			     }

                   if(prodID.trim().length() > 0 && prodID1.trim().length() > 0){
                	   String key = omagroups.get("1") + "";
                	   if(tulemusKood.get(key) != null){
                		   TreeMap t1 = (TreeMap) tulemusKood.get(key);
                		   t1.put(prodID, key + ":" + prodID1);
                		   tulemusKood.put(key, t1);
                	   }else{
                		   TreeMap t1 = new TreeMap();
                		   t1.put(prodID,key + ":" +  prodID1);
                		   tulemusKood.put(key, t1);
                	    }
                              
                   }
    				
                   
    				String prodID2 = "";
                   if (row.getCell((short) 2) != null) {
                     try {
    						prodID2 = (int) (row.getCell((short) 2)
    								.getNumericCellValue()) + "";
    					} catch (Exception e) {
    						prodID2 = (row.getCell((short) 2).getStringCellValue());
    					}
    			       }
    				

                   if(prodID.trim().length() > 0 && prodID2.trim().length() > 0){
                 	  
                	   String key = omagroups.get("2") + "";
                	   if(tulemusKood.get(key) != null){
                		   TreeMap t1 = (TreeMap) tulemusKood.get(key);
                		   t1.put(prodID,key + ":" +  prodID2);
                		   tulemusKood.put(key, t1);
                	   }else{
                		   TreeMap t1 = new TreeMap();
                		   t1.put(prodID,key + ":" +  prodID2);
                		   tulemusKood.put(key, t1);
                	    }
                   }
                   
                   
    				String prodID3 = "";
                   if (row.getCell((short) 3) != null) {
                  	try {
    						prodID3 = (int) (row.getCell((short) 3)
    								.getNumericCellValue()) + "";
    					} catch (Exception e) {
    						prodID3 = (row.getCell((short) 3).getStringCellValue());
    					}
    			    }
    				

                   if(prodID.trim().length() > 0 && prodID3.trim().length() > 0){
                 	  
                	   String key = omagroups.get("3") + "";
                	   if(tulemusKood.get(key) != null){
                		   TreeMap t1 = (TreeMap) tulemusKood.get(key);
                		   t1.put(prodID,key + ":" +  prodID3);
                		   tulemusKood.put(key, t1);
                	   }else{
                		   TreeMap t1 = new TreeMap();
                		   t1.put(prodID, key + ":" + prodID3);
                		   tulemusKood.put(key, t1);
                	    }
                   }
                   
    				String prodID4 = "";
                   if (row.getCell((short) 4) != null) {
                 	try {
    						prodID4 = (int) (row.getCell((short) 4)
    								.getNumericCellValue()) + "";
    					} catch (Exception e) {
    						prodID4 = (row.getCell((short) 4).getStringCellValue());
    					}
    			    }
                   


                   if(prodID.trim().length() > 0 && prodID4.trim().length() > 0){
                 	  
                	   String key = omagroups.get("4") + "";
                	   if(tulemusKood.get(key) != null){
                		   TreeMap t1 = (TreeMap) tulemusKood.get(key);
                		   t1.put(prodID,key + ":" +  prodID4);
                		   tulemusKood.put(key, t1);
                	   }else{
                		   TreeMap t1 = new TreeMap();
                		   t1.put(prodID, key + ":" + prodID4);
                		   tulemusKood.put(key, t1);
                	    }
                   }
    			}
    			
    		//	System.out.println("TulemusKood " + tulemusKood.toString());
    			
    			
    			
    			TreeMap groups = new TreeMap();
    			row = sheet.getRow(2);
    			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.
                 
    			for (int a = 2; a < row.getLastCellNum(); a++) {
    				try {
    					groups.put(a + "", row.getCell((short) a)
    							.getStringCellValue());
    				} catch (Exception e) {
    				}
    			}

    			// System.out.println("groups" + groups.toString());

    			for (int d = 6; d < sheet.getLastRowNum(); d++) {
    				row = sheet.getRow(d);
    				String prodIDe = "";

    				if (row.getCell((short) 0) != null) {

    					try {
    						prodIDe = (int) (row.getCell((short) 0)
    								.getNumericCellValue()) + "";
    					} catch (Exception e) {
    						prodIDe = (row.getCell((short) 0).getStringCellValue());
    					}
    					
                          if(!prodIDe.equals("0")){
                       	
    					// Siin l�heme reasisse rokkima ja vaatama kas se toode
    					// l�heb kaasa
    					for (int a = 2; a < row.getLastCellNum(); a++) {

    						String group = (String) groups.get(a + "");
    						if (group != null && group.trim().length() > 0) {

    							String custID = " ";
    							String clGroup = " ";
    							String prodGroup = " ";
    							

    							String go = null;
    							clGroup = group;

    							MatrixLine mline = new MatrixLine();
    							// Vaatame kas selles veerus on sellel tootel pandud
    							// mingi t�his
    							if (row.getCell((short) a) != null) {

    								try {
    									go = (int) (row.getCell((short) a)
    											.getNumericCellValue()) + "";
    								} catch (Exception e) {
    									go = (row.getCell((short) a)
    											.getStringCellValue());
    								}

    							}

    							if (go != null && go.equals("1")) {

    						//	 System.out.println( a + " " + prodID + " " +
    							//	 clGroup + " " + go);

    								if (custID != null
    										&& custID.trim().length() > 0) {
    									mline.setCustID(custID.trim());
    								}
    								if (clGroup != null
    										&& clGroup.trim().length() > 0) {
    									mline.setCliGroup(clGroup.trim());
    									
    								}

    								if (prodIDe != null	&& prodIDe.trim().length() > 0 ) {
    										mline.setProdID(prodIDe.trim());	
    									}
    								
    								}
    								if (mline.getProdID() == null
    										&& prodGroup != null
    										&& prodGroup.trim().length() > 0) {
    									mline.setProdGroup(prodGroup);
    								}
    								

    								if (clGroup != null && prodIDe != null
    									&& clGroup.trim().length() > 0 && prodIDe.trim().length() > 0 ) {
                                       if(omaTootegroups.get(clGroup.trim()) != null){
                                    	   String ass = (String)(omaTootegroups.get(clGroup.trim()));
                                    	   if(tulemusKood.get(ass) != null){
                                    		  
                                    		     TreeMap tr = (TreeMap)tulemusKood.get(ass);
                                    		     if(tr.get(prodIDe) != null){
                                    		    	 mline.setLineinfo("" +tr.get(prodIDe));
                                    		 //   	 System.out.println(clGroup.trim() + " " + omaTootegroups.get(clGroup.trim())
                                    		   // 			 + " " + tr.get(prodIDe) + " " + prodIDe);
                                               	  
                                    		     }
                                    			
                                    	   }
                                       }
    									//tulemusKood.get()
    								}
    											
    								// if(insertassort){
    										//  insertSortLine(mline);  
    									// }
    								
    								//System.out.println("Milne " + mline.toString());
    								result.add(mline);
    							}
    						}
    					}
    					   
                          }
    				}
    			
    			
    		} catch (Exception e) {
    			e.printStackTrace();
    		}

    		return result;*/

    	}

        public Vector getPriceMatrixData(){
        	 String    sql = "SELECT Kliendikood AS custid,'' AS clgroup,Tootekood AS prodid,'' AS prodgroup, " +
            " Sooduhind AS price ,[kampaania lõpp] as paev  "+
            "   FROM soodukampaaniad inner join customers c on c.kood=Kliendikood where getdate() BETWEEN  [kampaania algus]-5 AND [kampaania lõpp]  " +
            "   and visit !='' order by Kliendikood,Tootekood";

             Vector result = new Vector();
               try {
                
            	   Connection con = getNewCon();
         	        Statement stmt = con.createStatement();
         	        ResultSet rs = stmt.executeQuery(sql);
                 while (rs.next()) {

                   String custID = rs.getString("custid").trim();
                   String clGroup = rs.getString("clgroup");
                   String prodID = rs.getString("prodid").trim();
                   String prodGroup = rs.getString("prodgroup");
                  
                   double price = rs.getDouble("price");
                   String date = rs.getString("paev");
                   MatrixLine mline = new MatrixLine();

                   if (custID != null && custID.length() > 0) {
                     mline.setCustID(custID);
                   }
                   if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                     mline.setCliGroup(clGroup.trim());
                   }
    
                   if(date != null){
                  	 mline.setEndDate(date.substring(0,10));
                   }
                   if (prodID != null && prodID.length() > 0) {
                     mline.setProdID(prodID);
                   }
                   if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                     mline.setProdGroup(prodGroup.trim());
                   }

                   if (price != 0) {
                     mline.setPrice(price);

                   }
                   if(mline.getPrice() > 0 || mline.getDiscount() > 0){
                       result.add(mline);    
                   }
                   
                 }
               }catch(Exception e){
              	 e.printStackTrace();
               }
      	
          return result;
            }

                                                    public String  getCustProdDiscounts(Agent agent){
                                                     return null;
                                                     }


    /**
     *
     */
    public void connectionUp(){
    }

    private Connection getNewCon() throws SQLException{
      return DriverManager.getConnection(url, "mobilejava", "javajava");
    }

    /**
     *
     */
    public void connectionDown(){
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */

    public TreeMap getProdGroups() throws SQLException {
      Connection con = getNewCon();
    TreeMap   tm = new TreeMap();
    String    sql = "SELECT [Code] as liik , [Description] as nimetus FROM [SalesData].[dbo].[SC2Subgroups]" ;

    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
    	String liik = rs.getString("liik").trim();
    	String nimi = rs.getString("nimetus");
    	
    	if (nimi == null || nimi.trim().length() == 0){
    		nimi = liik ;
    	}
    	
        tm.put(liik ,Converter.enCodeEst(nimi));
    }
    rs.close();
    st.close();
    con.close();

    getProdGroupsMilk(tm);
    
    return tm;
    }


        public String getDistribution() {
          return null;
        }



    public TreeMap getAgents() {
      String  cmd = "SELECT kood,nimi FROM agents ORDER BY nimi ";
        TreeMap agents = new TreeMap();
      
        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);


            while (rs.next()) {
              Agent agent = new Agent(rs.getString("kood").trim());

              String nimi = rs.getString("nimi").trim();
              agent.setName(nimi);
              agent.setIsPilot(true);
              agents.put(nimi, agent);
            }
            rs.close();
            stmt.close();
            con.close();

        } catch (Exception e) {
            getParent().showError(e);
        }
        return agents;
    }
    
    
    private HashMap getSortGroups() {
    	HashMap groups = new HashMap();
    	
try {
        File fff = new File(this.getExcelDir() + "sortiments.xls" );
        FileInputStream ff = new FileInputStream(fff);
		HSSFWorkbook workbook = new HSSFWorkbook((ff));
		HSSFSheet sheet = workbook.getSheet("customer");
		HSSFRow row = null;
		
		row = sheet.getRow(0);
	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
		
	//	System.out.println("rownum " + row.getLastCellNum());
		
		
		for(int a = 1 ; a < sheet.getLastRowNum()+1;a++){
			try{
			//	Loeme sisse rea
				row = sheet.getRow(a);
				if (row.getCell((short) 0) != null) {
				


	    			   String kood = null;
	    			  if(row.getCell((short)0) != null){
	    	    			  	try{
	    	    			  	  kood = row.getCell((short)0).getStringCellValue();  
	    	    			  	 }catch (Exception e){
	    	    			   	  kood = row.getCell((short)0).getNumericCellValue()+"";  
	    	    	    		} 
	    	    			  	 if(kood.indexOf(".")!= -1){
	    	    			  	 	 kood = (kood.substring(0,kood.indexOf(".")));	
	    	    			  	 }
	    	    			  //	 System.out.println("kood" + kood);
	    	    			  	 if(kood.trim().length() > 6){
	    	    			  		 kood = kood.trim().substring(0,6);
	    	    			  	 }
	    	    			  	 String sort = null;
	    	    			  	try{
	    	    			  		sort = row.getCell((short)1).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			  		sort = row.getCell((short)1).getNumericCellValue()+"";  
		    	    	    		} 
		    	    			  	 if(sort.indexOf(".")!= -1){
		    	    			  	 	 sort = (sort.substring(0,kood.indexOf(".")));	
		    	    			  	 }
	    	    			  	 groups.put(kood.trim(),sort.trim());
	    	    			   sortimendid.put(kood.trim(),sort.trim());
	    			  
	    				
				}	   
				}
			}catch (Exception e){
			}
		}
     }catch (Exception e){
	}
		

System.out.println(groups.toString());


    	return groups;
    }

    public TreeMap getCustomers(Agent agent){

    	sortimendid = new HashMap();
    	ketid = new HashMap();
    	
    	 TreeMap visits = getVisits();
        
        HashMap groups = getSortGroups();
        
    	
      String  cmd = "SELECT nimi,kood,aadress,discount,visit,info,agent,payer,'' as listcode,block, Chain as kett FROM customers k " +
        " WHERE agent != 'LUKUS' ORDER BY nimi ";

        log(cmd);

        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();

        try {
          Connection con = getNewCon();
          Connection con2 = getNewCon();
          Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);
            int counter = 0;
            while (rs.next()) {
              counter++;
                String nimi = rs.getString("nimi").trim();
                Customer cust = new Customer(rs.getString("kood").trim());
                cust.setName(Converter.enCodeEst(nimi) + " " + cust.getID());
                cust.restName = nimi;
                String aadress = rs.getString("aadress");
                if(aadress != null && aadress.trim().length()>0){
                    cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress.trim()),"Estonia"));
                    cust.restStreet = aadress.trim();
                    cust.setCountry("Eesti");
                }
                cust.setCustType("A");
                String ale = rs.getString("discount");
                if (ale != null){
                  cust.setDiscount(Double.parseDouble(ale.trim()));
                }

                String info = rs.getString("info");

                if(info != null && info.trim().length()>0){
                	cust.setInfo(info.trim());
                }

                String agentc = rs.getString("agent").trim();
                cust.addAgent(new Agent(agentc));

                
                if(visits.get(cust.getID()) != null){
       			 String visit = "" + visits.get(cust.getID());
       				 cust.setVisit(visit); 
       		     }
                
                
                if(agentc.equals("AP05")){
                	cust.addAgent(new Agent("AP06"));
                	cust.addAgent(new Agent("AP07"));
                      }
                
         String payer = rs.getString("payer");
         if (payer != null && payer.trim().length()>0){
           cust.setPayer(payer.trim());
         }
         String grupp = rs.getString("listcode");
         if (grupp != null && grupp.trim().length()>0){
      //   cust.setGroup(grupp.trim());
         }
         
         String sss = "select listcode ,listtype from fixlists  where clientcode= '" + cust.getID()+ "'";
         Statement stmt2 = con2.createStatement();
         ResultSet	      rs2 = stmt2.executeQuery(sss);
       
         while (rs2.next()) {
        	 String res = rs2.getString("listcode").trim() + "_" + rs2.getString("listtype").trim();
        	
        	 if(cust.getGroup().trim().length() == 0){
        		 cust.setGroup(res);  
        	 }else if(!cust.getGroup().equals(res)){
        		 cust.setGroup(cust.getGroup()+  "|" + res);
        	 }
        	 
         }
         rs2.close();
         stmt2.close();
         
         int block = rs.getInt("block");
         if(block == 1){
        	 //cust.setSalesBlock(true);
         }
       //  System.out.println("grupp" + grupp + cust.getName());
         String gro = (String) groups.get(cust.getID());
         if( gro != null){
         	cust.addFixSortCode(gro.trim());
         	//cust.level = gro.trim();
         }

         String kett =  rs.getString("kett");

         
         try{
 			
             if(kett != null && kett.trim().length()>0){
           	  
           		//  System.out.println(kett + " " + cust.getID());
           	  String ktt = Converter.cleanLitLatEst(kett.trim());
           	  cust.setChain(ktt);
          	   if(ketid.get(ktt) != null){
             		Vector vv = (Vector)ketid.get(ktt);
             		vv.add(cust.getID());
             		ketid.put(ktt , vv);
             	}else{
             		Vector vv = new Vector();
             		vv.add(cust.getID());
             		ketid.put(ktt , vv);
             	}
          	   
             }
            	  
 		  }catch(Exception e){
 			e.printStackTrace();  
 		  }
         
         customers.put(cust.getName(), cust);
                custMap.put(cust.getPayer(), cust);

            }
          //  System.out.println(ketid.toString());
            rs.close();

            String sql = "SELECT klient, summa FROM [customer due] ";

            rs = stmt.executeQuery(sql);
            while(rs.next()){
              String id = rs.getString("klient").trim();
              double debt = Double.parseDouble(rs.getString("summa").trim());
              Customer cust = (Customer)custMap.get(id);
              if (cust != null){
              //  cust.setDebt(cust.getDebt() + debt);
              }
            }



        	TreeMap abi = new TreeMap();
        	//Loeme sisse sv ja toote baasi vastavus koodid
         

             sql = " SELECT custid AS  klient, max(sentdate) AS sent,agentname  AS checker  , prodid AS toode " +
                   " FROM supervisor where resint1=0 and sentdate>getdate()-30 GROUP BY custid,agentname,prodid ";

            rs = stmt.executeQuery(sql);
            while(rs.next()){
              String id = rs.getString("klient").trim();
              Customer cust = (Customer)custMap.get(id);
              if (cust != null){
              	String lastd = rs.getString("sent").substring(0,10) + " "
				  + rs.getString("checker").trim();
              	String toode = rs.getString("toode").trim();
              	//System.out.println("toode" + toode + "id" + abi.containsKey(toode));
              	if(abi.containsKey(toode)){
              		Vector vec =(Vector)abi.get(toode);
              	cust.setLastCheckDate(lastd);
              	//System.out.println("vec" + toode +vec);
              	Enumeration en = vec.elements();
              	while(en.hasMoreElements()){
              		cust.addLastCheckMissingProds((String)en.nextElement());
              	}

              	}
              }
            }
            rs.close();
            stmt.close();
            con.close();
            con2.close();

        } catch (Exception e) {
            getParent().showError(e);
        }
        
        return customers;
    }

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "SELECT klient,number,kuupaev,summa,yle FROM [customer debt]";

      Connection con = getNewCon();
      Statement stmt = con.createStatement();

      ResultSet rs = stmt.executeQuery(sql);

      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        debt.setDoknr(rs.getString("number").trim());

        debt.setDuedate(rs.getDate("kuupaev"));

        debt.setSum(Double.parseDouble(rs.getString("summa").trim()));
        debt.setDue(rs.getString("yle"));
        list.addElement(debt);
      }
      rs.close();
      stmt.close();
      con.close();
      return list;
    }

    public TreeMap getProducts(Agent agent){
    	 
      String cmd = "SELECT toode, nimi, myygihind, unit, tykke, SC2Subgroup AS uusgrupp,grupp ,tahtsus AS xx,  ean  FROM products ";

        if (isDebug()) {
            System.out.println(cmd);
        }

        HashMap<String, String> names =  getProdNamesExcel();
     //   System.out.println("names " + names.size());
       
        TreeMap products = new TreeMap();

        if(eanprods == null) {
     	   eanprods = new TreeMap();
        }
        
        try {
          Connection con = getNewCon();
          Statement st = con.createStatement();
            ResultSet	      rs = st.executeQuery(cmd);
             prodHash = new HashMap();
            while (rs.next()) {
                String kood = rs.getString("toode").trim();
                String nimetus = rs.getString("nimi").trim();
                Product prod = new Product(kood);
               // System.out.println("kood " + kood + " " + names.get(kood));
                if(names.get(kood) != null) {
                	nimetus = names.get(kood);
                }

                prod.setPrice(parseDouble(rs.getString("myygihind").trim()));
                prod.setUnit(rs.getString("unit").trim() );
                prod.setInCases(parseDouble(rs.getString("tykke").trim()));
                String grupp = rs.getString("uusgrupp").trim();
                prod.setGroup(grupp);
                prod.setVat("24");
                
                prod.setType("LIHA");

                prod.setName(Converter.enCodeEst(nimetus  + " " + kood));
                String ean = rs.getString("ean");
                if(ean != null && ean.trim().length()>0){
                	prod.setEAN(ean.trim());

                    eanprods.put(prod.getEAN(), prod.getID());
                }
                
                
                
                products.put(nimetus  + " " + kood, prod);
                prodHash.put(prod.getID(), prod);
            }
            rs.close();

            String sql2 =   "SELECT toode,stock FROM [tooted laos]" ;
            rs = st.executeQuery(sql2);
            while(rs.next()){
              String id = rs.getString("toode").trim();
              Product prod = (Product)prodHash.get(id);
              if (prod != null){
                prod.setStock(rs.getDouble("stock"));
              }
            }
            rs.close();
            st.close();
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }

        this.getProductsMilk(products);
        
        return products;
    }

    /**
     * Method declaration
     *
     *
     * @param agentCode
     *
     * @return
     *
     * @see
     */
    public Vector getCustomerProductsList(String agentCode) {

      String sql1 = "SELECT cp.klient,cp.toode,cp.kogus,cp.kuupaev, " +
        " (case when cp.kuupaev+pr.maxdays>getdate() then 0 else 1 end) AS date " +
          "FROM custprodlist cp INNER JOIN products pr ON pr.toode=cp.toode GROUP BY cp.toode,cp.klient,cp.kogus,cp.kuupaev,pr.maxdays ORDER BY cp.klient,cp.toode,cp.kuupaev,pr.maxdays ";

      String sql2 = "SELECT klient,toode,SUM(case when kogus>0 then kogus else  0 end) AS plusskogus" +
                    ",SUM(case when kogus<0 then kogus else 0 end) AS minuskogus FROM custprodlist " +
                    "GROUP BY toode,klient ORDER BY klient,toode";

                      String sql3 = "SELECT klient,toode,ltrim(rtrim(str(round(kogus,0)))) AS kog,kuupaev " +
                          "FROM custprodlist  ORDER BY klient,kuupaev desc,toode";

        if (isDebug()) {
            System.out.println(sql1);

        }

        Vector custProds = new Vector();

        try {
          Connection con = getNewCon();
          PreparedStatement stmt = con.prepareStatement(sql1);

          //stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
          ResultSet	      rs = stmt.executeQuery();

          HashMap kliendid = new HashMap();
          while (rs.next()) {
            String kliendikood = rs.getString("klient").trim();
            String tootekood = rs.getString("toode").trim();
            double kogus = Double.parseDouble(rs.getString("kogus").trim());
            Customer cust = (Customer)kliendid.get(kliendikood);
            if (cust == null){
              cust = new Customer(kliendikood);
            }
            Product prod = new Product(tootekood);
            if (kogus != 0){
              CustProd cp = new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                           " ", 0, 0, 0, " ");

              cp.setRedline(rs.getString("date"));
              cust.addProduct(prod,cp,false);
              kliendid.put(kliendikood, cust);
            }
            //double pluskogus = Double.parseDouble(rs.getString("plusskogus").trim());
            //double minuskogus = Double.parseDouble(rs.getString("minuskogus").trim());
            //CustProd cp = new CustProd(kliendikood, tootekood, kogus ,pluskogus ,minuskogus);
            //custProds.add(cp);
          }

          rs.close();
          stmt.close();

          stmt = con.prepareStatement(sql2);
          rs = stmt.executeQuery();
          while(rs.next()){
            String klient = rs.getString(1).trim();
            String toode = rs.getString(2).trim();
            double pkogus = rs.getDouble(3);
            double mkogus = rs.getDouble(4);
            Customer c = (Customer)kliendid.get(klient);
            if (c != null){
              ArrayList al = (ArrayList)c.getProducts().get(toode);
              if (al != null){
                CustProd cp = (CustProd)al.get(1);
                if (cp != null){
                  if(pkogus>0){
                    cp.setMovedStockPluss(pkogus);
                  }
                  if(mkogus>0){
                    cp.setMovedStockMinus(mkogus);
                  }
                  custProds.add(cp);
                }
              }
            }
          }

          rs.close();
          stmt.close();

          stmt = con.prepareStatement(sql3);
          rs = stmt.executeQuery();
           int counter = 0;
           int cou = 0;
          String oldklient = null;

          Date olddate = null;
           while(rs.next()){
            String klient = rs.getString("klient").trim();
            String toode = rs.getString("toode").trim();
            String kogus = rs.getString("kog");
            Date date = rs.getDate("kuupaev");
            if(cou == 0){oldklient = klient;olddate = date;}
            if(!oldklient.equals(klient)){
            counter = 0;
                          oldklient = klient;
                          olddate = date;
            }
            Customer c = (Customer)kliendid.get(klient);
            if (c != null ){

              ArrayList al = (ArrayList)c.getProducts().get(toode);
              if (al != null){
                CustProd cp = (CustProd)al.get(1);
                if (cp != null){
                 if(!olddate.toString().equals(date.toString())){counter++;}
                  if (counter == 0){cp.setS1(kogus );}
                  if (counter == 1){cp.setS2(kogus );}
                  if (counter == 2){cp.setS3(kogus );}
                  if (counter == 3){cp.setS4(kogus );}
                  if (counter == 4){cp.setS5(kogus );}

                  custProds.add(cp);
                }
              }
              oldklient = klient;
              olddate = date;
            }
            cou++;
          }

          rs.close();
          stmt.close();




          con.close();
        } catch (Exception e) {
          getParent().showError(e);
        }
        connectionDown();
        return custProds;
    }

    public String getPilotDayData(Agent agent){
       // System.out.println("agendiplaanid t�itmine " + agent.getID());
        String sss = " select agentname as agent , [plaani kood] as plkood , " +
        		"[plaan ise] as plaan , t�itmine as myyk   from agendiplaanit�itmine where agentname ='" + agent.getID() + "'"  ;
        StringBuffer sb = new StringBuffer();
        try{
        	Connection con = getNewCon();
            boolean first = true;
             PreparedStatement st = con.prepareStatement(sss);
           
           ResultSet rs2 = st.executeQuery();
         
          sb.append("\n\n\n");
           
                            while (rs2.next()) {
                           	 if (first ){
                                    first = false; 
                                    sb.append("EELARVE T�ITMINE \n\nnimi\teelarve\ttehtud\n");
                           	 }
                                    sb.append( rs2.getString("plkood").trim() + "\t"
                           		  + rs2.getString("plaan") + "\t" + rs2.getString("myyk") + "\n");
                            }
                          rs2.close();
                          
        }catch(Exception e){
        	e.printStackTrace();
        }
     //   System.out.println(sb.toString());
        return sb.toString();
    }


    public String getNonMobileOrders(Agent agent){
      return null;
      /*
      connectionUp(url);

      String sql = "SELECT RTRIM(SL01002) AS nimi, ST02009 AS number, OR20024 AS summa FROM ST020100 " +
                   "LEFT JOIN OR200100 ON  OR20001=ST02009 LEFT JOIN SL140100 k ON ST02001=SL14001 " +
                   "LEFT JOIN SL010100 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-3 AND ST02007=? " +
                  "AND LEN(RTRIM(OR20056))=0 GROUP BY ST02009,OR20024,SL01002,SL14003";

      StringBuffer sb = new StringBuffer();

        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, agent.getID());
            String sep= System.getProperty("line.separator");
            ResultSet	      rs = stmt.executeQuery();
            sb.append(getParent().getString("name") + "  \t\t" + getParent().getString("number") +
                      "\t" + getParent().getString("sum") + sep);
            sb.append("-------------------------------------------" + sep);
            while(rs.next()){
              sb.append(rs.getString("nimi").trim() + "\t");
              sb.append(rs.getString("number").trim() + "\t");
              String sum = getNF().format(Double.parseDouble(rs.getString("summa").trim())) ;
              sb.append(sum + sep);
            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        connectionDown();
        return sb.toString();*/
    }

    public void setBulkSales(Map agents){
      //System.out.println("bulkSales");
      String cmd = "SELECT agent,sales ,involume FROM agentsales WHERE LEN(agent)>0";
        log(cmd);

        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            ResultSet	      rs = stmt.executeQuery(cmd);
            while(rs.next()) {
              String agentID = rs.getString("agent");
              Agent agent = (Agent)agents.get(agentID);
              if (agent != null){
                agent.setMonthSales((int)rs.getDouble("sales"));
                agent.setKgSales((int)rs.getDouble("involume"));
               
              }
            }
            rs.close();
            
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }

    }

    /**
     * Method declaration
     *
     *
     * @param agentCode
     *
     * @return
     *
     * @see
     */
    public double getSalary(String agentCode) {
      if (agentCode == null) return 0;
      String cmd = "SELECT SUM(OR20024) AS sales ,OR20019 AS agent FROM OR200100 WHERE MONTH(OR20015)=" +
                   "MONTH(GETDATE()) AND YEAR(OR20015)=YEAR(GETDATE()) GROUP BY OR20019";
        log(cmd);

        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            ResultSet	      rs = stmt.executeQuery(cmd);

            while(rs.next()) {
              setSales(rs.getDouble("sales"));
            }
            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }

        return 0;
    }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
      /*String sql = "SELECT ST02020 AS  kogus, SC01071 AS sisu FROM ST020100 INNER JOIN " +
                   "SC010100 ON ST02017=SC01001 WHERE ST02007=?" +
                   " AND YEAR(ST02015)=YEAR(GETDATE()) AND MONTH(ST02015)=MONTH(GETDATE()) AND SC01012=?";
      PreparedStatement pst = con.prepareStatement(sql);
      pst.setString(1, agentCode);
      //Calendar cal = Calendar.getInstance();
      //cal.set(Calendar.DAY_OF_MONTH, 1);
      //pst.setTimestamp(2, new Timestamp(cal.getTime().getTime()));
      pst.setString(2, String.valueOf(nr));
      ResultSet rs = pst.executeQuery();
      double total = 0;
      while(rs.next()){
        double kogus = rs.getDouble("kogus");
        String sisu = rs.getString("sisu");
        if (!sisu.equals("")){
          total += kogus*Double.parseDouble(sisu);
        }
      }
      rs.close();
      pst.close();
     // System.out.println("nr:" + nr + ", total:" + total);
      return total;*/
    }

	
	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	       String sql = "INSERT into supervisor values ("
	         + "?, "   //   agent	C(50),
	         + "?,"   //   agentname	C(200),
	         + "?,"   //   clientid	C(50),
	         + "?,"   //   clientname	C(200),
	         + "?,"   //	prodid	C(50),
	         + "?,"   //   prodname	C(200),
	         + "getdate() ,"   //	sentdate	D,
	         + " '' ,"   //	receivedTime	D,
	         + "?,"   //normalpr	N,
	         + "?,"   // amoun	N,
	         + "?,"   // grouptotal	N,
	         + "?,"   // cattotal	N,
	         + "?,"   // pertotcat	I,
	         + "?,"   // percolcat	I,
	         + "?,"   // perpalcat	I,
	         + "?,"   // pershecat	I,
	         + "?,"   // perrescat	I,
	         + "?,"   // isaction	L,
	         + "?,"   // ispartner	L,
	         + "?,"   // isbrand	L,
	         + "?,"   // isgroup	L
	         + "?,"   // actionaeva	N,
	         + "?,"   // actionbeva N,
	         + "?,"   // actiontota	N,
	         + "?,"   // custeval	N,
	         + "?,"   // custcomme	C(200),
	         + "?,"   // comment	C(200),
	         + "?,"   // custagent	C(200),
	         + "?,"   // custagentname	C(200),
	         + "?,"   // mustbe	I,
	         + "?,"   // ideal	I,
	         + "?,"   // resstr1	C(200),
	         + "?,"   // resstr2	C(200),
	         + "?,"   // resstr3	C(200),
	         + "?,"   // resint1	I,
	         + "?,"   // resint2	I,
		      + "?)" ; //	resint3	L

	         try {
	         	System.out.println("jam");
	         	Connection  ccc = DriverManager.getConnection("jdbc:odbc:scalaDB", "mobilejava", "javajava");
	             PreparedStatement ps = ccc.prepareStatement(sql);

	             for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
	               eevaeksport.report.SuperVisorLineWrapperMeri lw = new  eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
	               ps.setString(1, wr.getInfo().getAgentID());
	               ps.setString(2, Converter.deCode( wr.getInfo().getAgentName()));
	               ps.setString(3, lw.getLine().getCustID());
	               ps.setString(4, Converter.deCode( lw.getLine().getCustName()));
	               ps.setString(5, lw.getLine().getProdID());
	               ps.setString(6, Converter.deCode(lw.getLine().getProdName()));
	               ps.setDouble(7, lw.getLine().getNormalPrice()*100);
	               ps.setDouble(8, lw.getLine().getAmount()*100);
	               ps.setDouble(9, 0);
	               ps.setDouble(10, 0);
	               ps.setInt(11, lw.getLine().getPercentageFromTotalCategory());
	               ps.setInt(12, lw.getLine().getPercentageFromColdCategory());
	               ps.setInt(13, lw.getLine().getPercentageFromPallCategory());
	               ps.setInt(14, lw.getLine().getPercentageFromShelfCategory());
	               ps.setInt(15, lw.getLine().getPercentageFromRestCategory());
	               ps.setBoolean(16, lw.getLine().isAction());
	               ps.setBoolean(17, lw.getLine().isPartner());
	               ps.setBoolean(18, lw.getLine().isBrand());
	               ps.setBoolean(19, lw.getLine().isGroup());
	               ps.setDouble(20, lw.getLine().getAction1Eval()*100);
	               ps.setDouble(21, lw.getLine().getAction2Eval()*100);
	               ps.setDouble(22, lw.getLine().getActionTotalEval()*100);
	               ps.setDouble(23, lw.getLine().getCustEval()*100);
	               String cc = Converter.deCode(lw.getLine().getCustComment());
	               if (cc == null){
	               	cc = " ";
	               }
	               ps.setString(24, cc);

	               String cd = lw.getLine().getComment();
	               if (cd == null){
	               	cd = " ";
	               }
	               ps.setString(25, Converter.deCode(cd));

	               String ce = lw.getLine().getCustAgentId();
	               if (ce == null){
	               	ce = " ";
	               }

	               ps.setString(26, ce);

	               String cf = lw.getLine().getCustAgentName();
	               if (cf == null){
	               	cf = " ";
	               }
	               ps.setString(27, Converter.deCode(cf));
	               ps.setInt(28, lw.getLine().getDealNr());
	               ps.setInt(29, lw.getLine().getIdealNr());

	               String cr1 = lw.getLine().getResStr1();
	               if (cr1 == null){
	               	cr1 = " ";
	               }
	               ps.setString(30, cr1);


	               String cr2 = lw.getLine().getResStr2();
	               if (cr2 == null){
	               	cr2 = " ";
	               }
	               ps.setString(31, "EE");


	               String cr3 = lw.getLine().getResStr3();
	               if (cr3 == null){
	               	cr3 = " ";
	               }
	               ps.setString(32, cr3);
	               ps.setInt(33, lw.getLine().getResInt1());
	               ps.setInt(34, lw.getLine().getResInt2());
	               ps.setInt(35, lw.getLine().getResInt3());
	               ps.executeUpdate();
	             }

	             ps.close();
	             ccc.close();
	         } catch (SQLException e) {
	             e.printStackTrace();
	         }
	       }



    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,enddate AS kuupaev,docsum AS summa,linecount AS ridu,text AS tekst " +
                   " FROM dayreport WHERE enddate>(getdate()-30) ORDER BY clid,enddate desc ";
      Connection con = getNewCon();
      Connection con2 = getNewCon();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);


      String sql2 = "SELECT  c.kood, aasta AS aastamyyk,kuu AS kuumyyk,a.nimi AS agendinimi, " +
			" contact AS kontaktisik ,phone AS telefon,right( paymentcode,2) AS maksetin, discount AS ale FROM " +
			" customers c LEFT JOIN agents a ON a.kood=c.agent LEFT JOIN Vahetabel v ON v.klient=c.kood" +
			"     WHERE c.agent !='LUKUS' and c.kood in (select klient from dbo.custprodlist2 group by klient) ORDER BY c.nimi";

      Statement st2 = con2.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs2 = st2.executeQuery(sql2);

      StringBuffer sb = new StringBuffer();
      String sep = ";";
      while(rs.next()){
        String cliID = rs.getString("klient").trim();
        sb.append(cliID + sep);
        sb.append(rs.getString("kuupaev").substring(0,10) + sep);
        sb.append(rs.getDouble("summa") + sep);
        sb.append(rs.getInt("ridu") + sep);
        String tekst = rs.getString("tekst");
        if (tekst != null && tekst.trim().length()>0){
        	tekst = tekst.trim();
        	tekst = tekst.replace('\'', ' ').replace('\"', ' ');

        } else {
        	tekst = " ";
        }
        sb.append(tekst + sep);

        boolean has = scrollTo(cliID, rs2);
        if (has){
         sb.append(ensureLen(rs2.getString("aastamyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("kuumyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
         sb.append(ensureLen(rs2.getString("kontaktisik"), " ").replace(';',',') + sep);
         sb.append(ensureLen(rs2.getString("telefon"), " ").replace(';',',') + sep);
         String makse = rs2.getString("maksetin");
         if(makse!= null && makse.trim().length()>0){
         	if(makse.trim().equals("HE")){
         		makse = "1";
         	}

         }
         sb.append(ensureLen(makse, "0") + sep);
         sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
        }

        sb.append("\n");
      }

      ManagerData md = new ManagerData();
      md.setClientVisitInfo(sb.toString());

      sql = "SELECT c.kood AS klient,c.agent AS agent,a.nimi AS agentname FROM customers c INNER JOIN agents a ON a.kood=c.agent " +
     "  WHERE c.agent != 'LUKUS' and c.kood IN (select klient from dbo.custprodlist2 group by klient) GROUP BY c.kood,c.agent,a.nimi,c.nimi ORDER BY c.nimi";

      PreparedStatement ps = con.prepareStatement(sql);
    //  ps.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
      rs = ps.executeQuery();
      StringBuffer bf2 = new StringBuffer();
      while(rs.next()){
        bf2.append(rs.getString("klient").trim() + sep);

      	String agent = rs.getString("agent").trim();
        bf2.append(agent + sep);
        String agentName = rs.getString("agentname");
        if (agentName == null) agentName = " ";
        bf2.append(agentName + sep);
        String email = parent.parseFirstEmail(parent.getEmail(agent));
        if(email == null || email.trim().length()== 0 )email = " ";
        bf2.append(email + "\n");
      }

      md.setClientAgentInfo(bf2.toString());
      return md;
    }


    private boolean scrollTo(String id, ResultSet rs) throws SQLException{
      rs.beforeFirst();
      while(rs.next()){
        if (id.equals(rs.getString("kood").trim())){
          return true;
        }
      }
      return false;
    }

    private String ensureLen(String str, String replace){
      if (str == null) str = "";
      else str = str.trim();
      if (str.length() == 0){
        return replace;
      } else {
        return str;
      }
    }
    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getStartingTime() {
      return null;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getDriverVersion() {
        return DRIVER_VERSION;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getSubjectFilesTo() {
        return this.subjectFilesTo;
    }

    /**
     * Method declaration
     *
     *
     * @param s
     *
     * @see
     */
    public void log(String s) {
        getParent().log(s);
    }

    private static double parseDouble(String s){
      if (s.length() > 0) return Double.parseDouble(s);
      else return 0;
    }

       public NumberFormat getNF(){
         return NF;
       }

       /**
        *
        * @param aNF - the new value for NF
        */
       public void setNF(NumberFormat aNF){
         NF = aNF;
    }

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "Rakvere";
    //private String             url = "jdbc:odbc:scalaDB";
    private String url = "jdbc:odbc:scalaDB";
    private Connection ccc ;
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();

    private TreeMap getFixSortimentContracts() {
    	TreeMap res = new TreeMap();
    	File f = new File(this.getExcelDir() + "sortiments.xls");
    	//teeme seda kõike juhul kui faili oli muudetud.
    	String lasttime = null;
		try {
			boolean readdata = true;
	    	if(f.exists()) {
	          lasttime =  f.lastModified() + "";
			 String olemasolev = null;
			try {
				File ff = new File("lastfixed.txt");
				olemasolev = Converter.readFile(ff, "utf-8", null).substring(0,13);
				readdata = !lasttime.equals(olemasolev);
			} catch (IOException e1) {
				//e1.printStackTrace();
			}
			}
			
		if(readdata) {	
			
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("Assortment");
			HSSFRow row = null;
			
			HSSFSheet sheetKood = workbook.getSheet("Code");
			
			HSSFSheet sheetKoodSort = workbook.getSheet("CodeSorts");
			
			TreeMap omakoodid = new TreeMap();

			TreeMap omagroups = new TreeMap();
			
			TreeMap omaTootegroups = new TreeMap();
			
			row = sheetKood.getRow(0);
			// M��RAME �RA MIS V�LJAS ASUVAD TOOTEKOODIDE Sortimendi t�hised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 1; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					omagroups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

		//	 System.out.println("omagroups " + omagroups.toString());

			
			//mis kood on sortimedi koodid ja tootekoodide sortimendi t�hised kokku 
			for (int i = 0; i < sheetKoodSort.getLastRowNum() + 1; i++) {
				row = sheetKoodSort.getRow(i);
				String sortkood = "";
				if (row.getCell((short) 0) != null) {
                  try {
						sortkood = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						sortkood = (row.getCell((short) 0).getStringCellValue());
					}
			}
				String omakood = "";

				if (row.getCell((short) 1) != null) {

					try {
						omakood = (int) (row.getCell((short) 1)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						omakood = (row.getCell((short) 1).getStringCellValue());
					}
					//System.out.println("omakood" + omakood + " " + sortkood);

			}
				
				if(!omakood.equals("") && !sortkood.equals("")){
					
						omaTootegroups.put(sortkood, omakood);
					//	omaTootegroups.put(sortkood, i+"");
				}
			}
				
	//		 System.out.println("omaTootegroups " + omaTootegroups.toString());
			
			TreeMap tulemusKood = new TreeMap();

			for (int i = 1; i < sheetKood.getLastRowNum(); i++) {
				row = sheetKood.getRow(i);
				
				String prodID = "";
               if (row.getCell((short) 0) != null) {
                 try {
						prodID = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID = (row.getCell((short) 0).getStringCellValue());
					}
								}
				

				String prodID1 = "";
               if (row.getCell((short) 1) != null) {
                	try {
						prodID1 = (int) (row.getCell((short) 1)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID1 = (row.getCell((short) 1).getStringCellValue());
					}
			     }

               if(prodID.trim().length() > 0 && prodID1.trim().length() > 0){
            	   String key = omagroups.get("1") + "";
            	   if(tulemusKood.get(key) != null){
            		   TreeMap t1 = (TreeMap) tulemusKood.get(key);
            		   t1.put(prodID, key + ":" + prodID1);
            		   tulemusKood.put(key, t1);
            	   }else{
            		   TreeMap t1 = new TreeMap();
            		   t1.put(prodID,key + ":" +  prodID1);
            		   tulemusKood.put(key, t1);
            	    }
                          
               }
				
               
				String prodID2 = "";
               if (row.getCell((short) 2) != null) {
                 try {
						prodID2 = (int) (row.getCell((short) 2)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID2 = (row.getCell((short) 2).getStringCellValue());
					}
			       }
				

               if(prodID.trim().length() > 0 && prodID2.trim().length() > 0){
             	  
            	   String key = omagroups.get("2") + "";
            	   if(tulemusKood.get(key) != null){
            		   TreeMap t1 = (TreeMap) tulemusKood.get(key);
            		   t1.put(prodID,key + ":" +  prodID2);
            		   tulemusKood.put(key, t1);
            	   }else{
            		   TreeMap t1 = new TreeMap();
            		   t1.put(prodID,key + ":" +  prodID2);
            		   tulemusKood.put(key, t1);
            	    }
               }
               
               
				String prodID3 = "";
               if (row.getCell((short) 3) != null) {
              	try {
						prodID3 = (int) (row.getCell((short) 3)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID3 = (row.getCell((short) 3).getStringCellValue());
					}
			    }
				

               if(prodID.trim().length() > 0 && prodID3.trim().length() > 0){
             	  
            	   String key = omagroups.get("3") + "";
            	   if(tulemusKood.get(key) != null){
            		   TreeMap t1 = (TreeMap) tulemusKood.get(key);
            		   t1.put(prodID,key + ":" +  prodID3);
            		   tulemusKood.put(key, t1);
            	   }else{
            		   TreeMap t1 = new TreeMap();
            		   t1.put(prodID, key + ":" + prodID3);
            		   tulemusKood.put(key, t1);
            	    }
               }
               
				String prodID4 = "";
               if (row.getCell((short) 4) != null) {
             	try {
						prodID4 = (int) (row.getCell((short) 4)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID4 = (row.getCell((short) 4).getStringCellValue());
					}
			    }
               


               if(prodID.trim().length() > 0 && prodID4.trim().length() > 0){
             	  
            	   String key = omagroups.get("4") + "";
            	   if(tulemusKood.get(key) != null){
            		   TreeMap t1 = (TreeMap) tulemusKood.get(key);
            		   t1.put(prodID,key + ":" +  prodID4);
            		   tulemusKood.put(key, t1);
            	   }else{
            		   TreeMap t1 = new TreeMap();
            		   t1.put(prodID, key + ":" + prodID4);
            		   tulemusKood.put(key, t1);
            	    }
               }
			}
			
		//	System.out.println("TulemusKood " + tulemusKood.toString());
			
			
			
			TreeMap groups = new TreeMap();
			row = sheet.getRow(2);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.
             
			for (int a = 2; a < row.getLastCellNum(); a++) {
				try {
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
				}
			}

			// System.out.println("groups" + groups.toString());

			for (int d = 6; d < sheet.getLastRowNum(); d++) {
				row = sheet.getRow(d);
				String prodIDe = "";

				if (row.getCell((short) 0) != null) {

					try {
						prodIDe = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodIDe = (row.getCell((short) 0).getStringCellValue());
					}
					
                      if(!prodIDe.equals("0")){
                   	
					// Siin l�heme reasisse rokkima ja vaatama kas se toode
					// l�heb kaasa
					for (int a = 2; a < row.getLastCellNum(); a++) {

						String group = (String) groups.get(a + "");
						if (group != null && group.trim().length() > 0) {

							String custID = " ";
							String clGroup = " ";
							String prodGroup = " ";
							

							String go = null;
							clGroup = group;

							if (row.getCell((short) a) != null) {

								try {
									go = (int) (row.getCell((short) a)
											.getNumericCellValue()) + "";
								} catch (Exception e) {
									go = (row.getCell((short) a)
											.getStringCellValue());
								}

							}

							if (go != null && go.equals("1")) {

						//	 System.out.println( a + " " + prodID + " " +
							//	 clGroup + " " + go);
                              
								
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									

								
								  String pood =  clGroup;
			                         Contract con = new Contract(pood);
									
			                        	// System.out.println("Sees " +a + " " + pood + " " + prodIDe + " " + pood);
			                        
										if(res.get(pood) != null){
			            	                 con = (Contract)res.get(pood);
			            	                } 
										  ContractLine ll = new ContractLine(prodIDe);
			            	               if(con.lines == null){
			            		                 con.lines = new Vector();
			            	                  }
			            	              con.lines.add(ll);
			            	               res.put(pood, con);
			            	              
								 		
							  }
							}
						  }
					    }
					  }
				   }
			    }
			if(lasttime != null) {
			  Converter.saveFile("lastfixed.txt", lasttime);
			}
			} else {
				System.out.println("Sortimendi fail on sama mis enne ja ei lae midagi.");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    	return res;
    }


	@Override
	public void FixLoader() {
		  
		  if(eanprods == null) {
			  eanprods = new TreeMap();
		  }
			this.getProducts(null);
		
			
			TreeMap contractsMilk = getFixSortimentContracts();
			//	System.out.println("Contracts " + contracts.keySet().size() + " " + contracts.keySet().toString());
				if(contractsMilk.size() > 0) {
					Converter.sendContractJson(contractsMilk , rcompanyx,  user ,  pass);
				}
			
			
			
			TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
			System.out.println("Contracts " + contracts.keySet().size() + " " + contracts.keySet().toString());
			if(contracts.size() > 0) {
				Converter.sendContractJson(contracts , rcompanyx,  user ,  pass);
			}

			System.exit(0);

			}
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

