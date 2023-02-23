package eevaeksport.driver;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONObject;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;
import eevaeksport.util.SmsSender;


/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class AxaZpienasMilkDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
  public AxaZpienasMilkDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    String databaseDriverName = "oracle.jdbc.driver.OracleDriver";
    databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    NF.setMaximumFractionDigits(2);
    NF.setGroupingUsed(false);
    NF.setMinimumFractionDigits(2);
     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
    } catch (Exception e){
       parent.showError(e);
    }
 }

  
  /**
   *
   */
  public void connectionUp(){
  }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	public static String getYYYYpMMpDDpString(Date dd){
	    StringBuffer sb = new StringBuffer();
	    Calendar cal= Calendar.getInstance();
	    cal.setTime(dd);
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}
	String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		sb.append("" + cal.get(Calendar.YEAR));
	if (month.length() == 1){month = ("0") + month;}
		sb.append("." + month  + ".");
		sb.append(day );
		
	    return sb.toString();
	  }

	
  public String getSaveDebtInfo() {
	  //Siin siis teadete saatmine farmeritele .
	  
	  
	  // 1. teeme p�ringu kellele kuhu saadame 
	  TreeMap saajad = new TreeMap();
	  try {
		  
		  String cmd = "select stationid as klient,'SMS' as type,(select top 1 l.locator  from  LOGISTICSELECTRONICADDRESS l  where  l.privateforparty = s.party) as aadress "
		  		+ " from bbsstationtable s where dataareaid = 'zp'" + 
		  		"and (select top 1 l.locator  from  LOGISTICSELECTRONICADDRESS l  where  l.privateforparty = s.party) is not null ";
	    	Connection con = this.getNewConnection();
	    	Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        
	        while(rs.next()){
		         String klient = rs.getString("klient").trim();
		         String type = rs.getString("type").trim();
		         String aadress = rs.getString("aadress").trim();
			        	
			      saajad.put(klient,type + "|" + aadress);
	        }
	        
	        rs.close();
	        stmt.close();
	        con.close();
	    	//saajad.put("1000", "SMS|+37256248034");
	    	
	    }catch(Exception r){
	    	r.printStackTrace();
	    }
	  
	  System.out.println("saajad " + saajad.size());
	  
	  
	  //2. teeme p�ringu andmetega ja panene teadet treemappi . 
	  
	  
	  
	    String   cmd = " select tt.klient as klient,  tt.stationid , cast(tt.rec as varchar)  as dok \r\n" + 
	    		"         ,(tt.kokku) as koik,  tt.kuup, tt.riebsta as fatsta ,tt.baltsta as protsta, tt.rieb as fat ,tt.balt as prot    from \r\n" + 
	    		"         ( select ost.codefrom as klient,ost.stationid,ost.typefrom, ost.purchqty as kokku ,(ost.recid) as rec ,  ost.transdate as kuup, purchqty  as purchqty,   \r\n" + 
	    		"        (Select top 1 pdsbatchattribvalue  from PdsBatchAttributes pa where pa.InventBatchId = ost.InventBatchTo  \r\n" + 
	    		"       and pa.analysisplace_bbs  = 'ZP' and pa.pdsbatchattribid = 'RP' and  vendaccount_bbs = ost.codefrom) as riebsta ,\r\n" + 
	    		"      (Select top 1  pdsbatchattribvalue  from PdsBatchAttributes pa where pa.InventBatchId = ost.InventBatchTo    \r\n" + 
	    		"       and pa.analysisplace_bbs  = 'ZP' and pa.pdsbatchattribid = 'BP' and  vendaccount_bbs = ost.codefrom) as baltsta ,\r\n" + 
	    		"       (Select top 1 pdsbatchattribvalue  from PdsBatchAttributes pa where pa.InventBatchId = ost.InventBatchFrom  \r\n" + 
	    		"       and pa.analysisplace_bbs  = 'ZP' and pa.pdsbatchattribid = 'RP') as rieb ,\r\n" + 
	    		"     (Select top 1  pdsbatchattribvalue  from PdsBatchAttributes pa where pa.InventBatchId = ost.InventBatchFrom   \r\n" + 
	    		"      and pa.analysisplace_bbs  = 'ZP' and pa.pdsbatchattribid = 'BP') as balt  from  BBsMilkJournalTrans ost inner join bbsMilkJournalTable jou\r\n" + 
	    		"		 on ost.JournalID = jou.JournalID and JournalType=1\r\n" + 
	    		"      where ost.dataareaid ='zp' and ost.transdate > getdate() -21  and purchqty  != 0  and ost.smssentdatetime < getdate()-22 \r\n" + 
	    		"		) as tt \r\n" + 
	    		"     where ISNUMERIC(tt.rieb)= 1 or   tt.typefrom = 0  order by 1,2 desc,3 ";
	    
	    
	    if (isDebug()) System.out.println(cmd);
	    TreeMap messages = new TreeMap();
	    
	    try {
	    
	    	Connection con = this.getNewConnection();
	    	Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        
	        while(rs.next()){
		       String klient = rs.getString("klient").trim();
		       String station = rs.getString("stationid");
		       boolean stati = false;
		       if(station != null && station.trim().length() > 0 && !station.trim().equals(klient)) {
		    	   klient = station;
		    	   stati = true;
		       }
		         
		       if(saajad.get(klient) != null){ // kui onolemas siis tegutseme edasi
		    	  String nimi = "noname";//rs.getString("nimi").trim();
		         String dok = rs.getString("dok").trim();
		         double kogus = rs.getDouble("koik");
		         java.sql.Date daat = rs.getDate("kuup");
		         String f = "-";
		         String p = "-";
		         String a = "-";
		         
		         String fff = rs.getString("fatsta");
		         double fatSta =  0 ;
		         try {
		         fatSta = Double.parseDouble(fff);
		        
		         double protSta = rs.getDouble("protsta");
		         double fat = rs.getDouble("fat");	
		         double prot = rs.getDouble("prot");
		         
		         if(stati) {
		        	   if(fatSta == 0) {
		        		   fatSta = fat;
		        	   }
		        	   if(protSta == 0) {
		        		   protSta = prot;
		        	   }
		        	 
			         double arvest = kogus *Converter.Round((float)(1+(fatSta-3.4)*0.178+(protSta-3)*0.267),3); 
			            a =  NF.format(arvest);
				        f =  NF.format(fatSta);
				        p =  NF.format(protSta);
		         
		         } else {
		        	 try {
			         if(fat == 0) {
			        	 fat = fatSta;
			         }
		        	
			         if(prot == 0) {
			        	 prot = protSta;
			         }
				      double arvest = kogus * Converter.Round((float)(1+(fat-3.4)*0.178+(prot-3)*0.267),3); 
				        a =  NF.format(arvest);
				        f =  NF.format(fat);
				        p =  NF.format(prot);
			         }catch(Exception e) {
			         }
		         }
		        
		         
		         
		          messages.put(dok,klient + "_" + nimi +  "|Data:" + getYYYYpMMpDDpString(daat)+":" + klient
		        		  +";Kiekis:"+ NF.format(kogus) +";R:"+ f
		          + ";B:" + p + ";PK:" + a);
		         }catch(Exception e) {
		        	 System.out.println("klient " + klient + " " + dok);
		        	 e.printStackTrace();
		         }
		         }
	        }
	        
	        rs.close();
	        stmt.close();
	        con.close();
	    	
	    }catch(Exception r){
	    	r.printStackTrace();
	    }
	//  System.out.println("messages " + messages.size());
	  
	Iterator itera =  messages.keySet().iterator();
	  
	while (itera.hasNext()){
	
		 String dok = (String)itera.next();
		 String sb = (String)messages.get(dok);
		 StringTokenizer st = new StringTokenizer(sb,"_");
         String klient = st.nextToken();
         String sisu = st.nextToken();
         st = new StringTokenizer(sisu,"|");
         String nimi = st.nextToken();
         sisu = st.nextToken();
         String inf =  (String)saajad.get(klient);
         System.out.println("inf " + inf);
         String type = inf.substring(0, inf.indexOf("|")); 
         String aadress = inf.substring(inf.indexOf("|")+1 );  
         
	   try{
		   
         if(type.equals("SMS")){
        	 String messageid = klient +"-"+ Calendar.getInstance().getTimeInMillis();
        	 SmsSender ss = new SmsSender();
        	 ss.send(messageid, aadress, sisu);
        	
         }else{
        	sendEmailMessage(sisu,dok,klient,nimi, aadress); 
         }
         //j�tame recid meelde mis saadetud .
         
         StringTokenizer st3 = new StringTokenizer(dok,",");
     	
     	if (st3.hasMoreTokens()) {
     		String hhh = st3.nextToken().trim();
            
    	    //salvestame saadetud faili ka m�llu 
    	    String loca = parent.getProps().getProperty("mess.dir");
            String fileName = loca + klient + "-" + (hhh + "").trim() +  ".txt";
            System.out.println(fileName);
            saveFileInUTF8(fileName, sb);
     	}
     		
        
        // Teeme ka update Tabelisse et teade on saadetud 
		   
        try {
    	    
        	StringTokenizer st2 = new StringTokenizer(dok,",");
        	
        	while (st2.hasMoreTokens()) {
        		String hhh = st2.nextToken().trim();
        		cmd = " update BBsMilkJournalTrans set smssentdatetime = getdate() , smstext = '" + type + "-" + sisu + "'  where dataareaid ='zp' and recid =" + hhh;
            	
    	    	Connection con = this.getNewConnection();
    	    	Statement stmt = con.createStatement();
    	        stmt.executeUpdate(cmd);
    	        stmt.close();
    	        con.close();
        	}
        	
        	
	    	
	    }catch(Exception r){
	    	r.printStackTrace();
	    }
        System.out.println("Exit " + sisu + " " + aadress);
        
		// System.exit(0);
		   
		   }catch(Exception ee){
		   ee.printStackTrace();
	    }
   	}   
       
   	return null;
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
	
  
  public boolean hasRepreatNums(String ordnum) {

	    File f = new File( "repreatnums.txt");
	    if (f.exists()){
	    try{

		      InputStreamReader is = new FileReader(f);
		      BufferedReader br = new BufferedReader(is);
		      String line = null;
		      while( ( line = br.readLine()) != null){
		      	String id = null;
		      
		        StringTokenizer st = new StringTokenizer(line, ",");
		        if (st.hasMoreTokens()){
		        	id = st.nextToken();
		           if(id.equals(ordnum)){
		        	   return true;
		           }
		        }
		    }
		      is.close();
		      br.close();
		      
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    }
	    return false;
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

  
  
  
  
  private void sendEmailMessage  (String comment,String dok,String custid, String nimi, String aadress) throws Exception{
	   	
	   		if(comment != null && comment.trim().length() >0){
	   			Email mail = new Email(null);
	   		 StringTokenizer st3 = new StringTokenizer(dok,",");
	   			String hhh = st3.nextToken().trim();
	            
	   		    //salvestame saadetud faili ka m�llu 
	   		   
		  		   
		  	  		mail.setSMTP(parent.getProps().getProperty("smtphost"));
		  	  		mail.setFilesTo(aadress);
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		mail.setSubjectFilesTo("ZPienas milkreport " + custid);
		  	  		mail.setEncoding("utf-8");
		  	  		mail.setBody("\nOrder :" +  hhh + " \nCustomer code :" + custid + 
		  	  				"\nCustomer Name :" + nimi  +  "\n" + comment);
		  	  		mail.setSendNotice(false);
		  	  		mail.send();
		  	  		System.out.println("\nOrder Comment " +  hhh + " " + custid + "\n\n" +
		  	  				  "\n >>>>>" + comment + "<<<<<");
		  				
	   		}
	   }
  

  
 
 
  
  public Vector getFixedAssortmentData() {
				return new Vector();
	}


  public Vector getPriceMatrixData() {
     return new Vector();
  }

  public String getProductSortingData(){
   	return null;
   }

  public Vector getDebt(String agentCode) throws SQLException{
     return this.debt;
   }


  public String getAssortmentStr(Agent agent){
  	return null;
  }

      public String getSaveStockInfo(){
      return null;
      }


              public String getDealerCustomerCSV(String agent){
               return null;
               }

                                                  public String  getCustProdDiscounts(Agent agent){
                                                   return null;
                                                   }


  /**
   *
   */
  public void connectionDown(){
  }

  
  public DealerProduct getDealerProductsList() {
       return null;
     }

  
  TreeMap agnames = null;
  
  public TreeMap getAgents(){
	 

    String cmd = " select stationid as kood, description as nimi   from BBSStationTable dim where description like '%ST.P%'";

    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    StringBuffer sb2 = new StringBuffer();
    agnames  = new TreeMap();
    try {
    	Connection con = this.getNewConnection();
    	Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("nimi").trim();
          agent.setName(name + " " + agent.getID());
          agent.setIsPilot(true);
          agents.put(name + agent.getID(), agent);
         
        	  agnames.put(agent.getID(), name);
            //  sb2.append(Converter.enCodeEst(agent.getID() + ";" + name + "; ; ; ;" + grupp +";" + gruppname) +"\n");  
          
        }
        rs.close();
        stmt.close();
        con.close();
    } catch (Exception e){
        getParent().showError(e);
      }
  /*  Agent aa = new Agent ("MILK");
    aa.setName("MILK");
    agents.put("MILK", aa);*/
    
    if(sb2.length() > 2){
    	try{
    		// Converter.saveFileInUTF8( "agent.txt", sb2);
    	}catch (Exception e){
    		e.printStackTrace();
    	}
      
     }
    return agents;
  }
 
  

  public static  String enCodeEst(String res ){
	  	 
	   if(res != null){
			res =res.replaceAll("„","'");
	    	res =res.replaceAll("“","'");
	    	res =res.replaceAll("”","'");
	    	
	    	
	    	res =res.replaceAll("°","11151xx");
	    	res =res.replaceAll("ø","11150xx");
	    	res =res.replaceAll("¹","11152xx");
	    	res =res.replaceAll("²","11153xx");
	    	res =res.replaceAll("³","11154xx");
	    	
	    	res =res.replaceAll("ä","11111xx");
		  	res =res.replaceAll("ö","11112xx");
			res =res.replaceAll("õ","11114xx");
			res =res.replaceAll("ü","11113xx");

			res =res.replaceAll("Ä","11115xx");
			res =res.replaceAll("Ö","11116xx");
			res =res.replaceAll("Õ","11117xx");
			res =res.replaceAll("Ü","11118xx");
			
			res =res.replaceAll("Š","11119xx");
			
			res =res.replaceAll("š","11120xx");
			res =res.replaceAll("ž","11121xx");
			res =res.replaceAll("Ž","11122xx");
		    res = res.replaceAll("Ā","11123xx");
	        res = res.replaceAll("ā","11124xx");
	        res = res.replaceAll("ą","11125xx");
	        res = res.replaceAll("Ą","11126xx");        
	        
	        res = res.replaceAll("Č","11127xx");
	        res = res.replaceAll("č","11128xx");
	        res = res.replaceAll("Ė","11129xx");	
	        res = res.replaceAll("ė","11130xx");
	        res = res.replaceAll("Ē","11131xx");
	        res = res.replaceAll("ē","11132xx");       
	        res = res.replaceAll("Ģ","11133xx");
	        res = res.replaceAll("ģ","11134xx");        
	        res = res.replaceAll("Ī","11135xx");
	        res = res.replaceAll("ī","11136xx");      
	        res = res.replaceAll("Į","11137xx");
	        res = res.replaceAll("į","11138xx");        
	        res = res.replaceAll("Ķ","11139xx");
	        res = res.replaceAll("ķ","11140xx");
	        res = res.replaceAll("Ļ","11141xx");
	        res = res.replaceAll("ļ","11142xx");
	        res = res.replaceAll("Ņ","11143xx");
	        res = res.replaceAll("ņ","11144xx");   
	        res = res.replaceAll("Ų","11145xx");
	        res = res.replaceAll("ų","11146xx");      
	        res = res.replaceAll("Ū","11147xx");
	        res = res.replaceAll("ū","11148xx");
	        res = res.replaceAll("é","11148xx");
	        
		 
	   }


		 	 
		  	return res;
		  }
		  



  public String getDateTimeStr(Calendar cal) {

		String month = (cal.get(Calendar.MONTH) + 1) + "";
		String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
		String hour = (cal.get(Calendar.HOUR_OF_DAY)) + "";
		String min = (cal.get(Calendar.MINUTE)) + "";
		String sec = (cal.get(Calendar.SECOND)) + "";
		
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (day.length() == 1) {
			day = "0" + day;
		}
		if (hour.length() == 1) {
			hour = "0" + hour;
		}
		if (min.length() == 1) {
			min = "0" + min;
		}
		if (sec.length() == 1) {
			sec = "0" + sec;
		}
		
		return cal.get(Calendar.YEAR) + "-" + month + "-"+ day + " " + hour + ":" + min + ":" + sec;
	}
	public String getDateStr(Calendar cal) {

		String month = (cal.get(Calendar.MONTH) + 1) + "";
		String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (day.length() == 1) {
			day = "0" + day;
		}
		return cal.get(Calendar.YEAR) + "" + month + day;
	}
  

  public TreeMap getCustomers(Agent agent){
	  	  
	  String cmd = "SELECT  name as name,street as nimelisa,namealias,cus.accountnum,cus.invoiceaccount AS payer,  cus.linedisc ,  address, stationid_bbs AS agent , " +
		  " pay.numofdays AS term,  (select l.locator  from  LOGISTICSELECTRONICADDRESS l  where l.recid = dp.primarycontactphone)+ ' '+ " +
		"	 (select l.locator  from  LOGISTICSELECTRONICADDRESS l  where l.recid = dp.primarycontactfax)+ ' '+  " +
		"	 (select l.locator  from  LOGISTICSELECTRONICADDRESS l  where l.recid = dp.primarycontactemail)  as info  , " +
		"	 (select l.locator  from  LOGISTICSELECTRONICADDRESS l  where l.recid = dp.primarycontactphone) as driver ,  " +
		" ( select top 1 l.locator  from  LOGISTICSELECTRONICADDRESS l  where l.privateforparty = cus.party and type = 1) as smsaadr ,  " +
	     "  cus.vendgroup as grupp,cus.pricegroup as prgrupp " +
	     "  ,  registrationlt_bbs as regnum	, cus.vatnum , state , county, cus.taxgroup,registrationlt_bbs as encode ,economyid_bbs as economyid,agreement_bbs as agreement " + 
   " , cus.bankcentralbankpurposetext as volainfo , nfccode_bbs as nfc " +
 " FROM  vendtable cus  inner join DirPartyTable dp ON cus.partY=dp.REcid  " +
 " INNER JOIN LOGISTICSPOSTALADDRESS adr ON adr.LOCATION = dp.PRIMARYADDRESSLOCATION and getdate() >=VALIDFROM  and  getdate() <= VALIDTO " +
  " left  JOIN  paymterm   pay on cus.paymtermid= pay.paymtermid " +
"	 and pay.DATAAREAID = cus.DATAAREAID and  pay.dataareaid='zp'  left  JOIN  custtable  asi  on asi.vendaccount= cus.accountnum " +
" and asi.DATAAREAID = cus.DATAAREAID and  asi.dataareaid='zp'   WHERE   (cus.dataareaid='zp' AND cus.blocked != 2 and stationid_bbs > 0 ) and cus.inactive_bbs = 0 " ;
	  
		 
		 
		 
		 String ccc = "select invoiceaccount from  vendtable where dataareaid='zp' and invoiceaccount is not null and len (invoiceaccount) > 0  group by invoiceaccount";//OK
		 
		         String sql = "  SELECT  cus.VENDACCOUNT AS klient,a.invoice AS numbe,co.duedate AS maksepaev,sum(co.amountmst) AS tasumata, " + 
	                   " co.transdate AS kuupaev , datediff(day,co.duedate ,getdate())  AS yle , max(a.bankcentralbankpurposetext) as txt FROM  custtransopen co  " +
					   " INNER JOIN  custtrans a ON co.REFRECID=a.recid and  co.DATAAREAID=a.DATAAREAID " +
	                " INNER JOIN  custtable cus on cus.accountnum =  a.ORDERACCOUNT and cus.dataareaid = 'zp' " +   
	                 "    WHERE co.dataareaid='zp' AND a.dataareaid='zp'	 and  cus.custgroup = '164PaskUk' and LEN(cus.VENDACCOUNT) > 1 " +
					 " group by a.invoice, cus.VENDACCOUNT,co.duedate, co.transdate  ORDER BY co.transdate,a.invoice, cus.VENDACCOUNT,co.duedate"; //OK
					  		
		           
		        String salessql = " SELECT  jou.orderaccount as klient,round(SUM(lineamount),0) AS summa,MONTH(sal.invoicedate) as kuu " +
			        " FROM vendinvoicetrans sal  INNER JOIN vendinvoicejour jou ON  sal.invoiceid = jou.invoiceid and  " +
			      " jou.DATAAREAID=sal.DATAAREAID WHERE " +
				    " sal.dataareaid='zp' AND sal.invoicedate > getdate() -390  GROUP BY jou.orderaccount,month(sal.invoicedate) " +
				   " ORDER BY jou.orderaccount,month(sal.invoicedate) " ;//OK
					           
		        String sss = "SELECT  accountnum, sum(case when co.duedate<getdate() then amountmst else 0 end) as asi , " +
			           		" sum(amountmst) as asi2 FROM  vendtransopen co " + 
			           	    " WHERE co.dataareaid='zp'   group by  accountnum having " + 
			           	    " sum(case when co.duedate<getdate() then amountmst else 0 end) != 0 or sum(amountmst) != 0 "; //OK
			           

        

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    TreeMap custDebt = new TreeMap();
    TreeMap totalDebt = new TreeMap();
   
    Vector payers =  new Vector();
    HashMap salesres = new HashMap();

 try {
    	Statement stmr = this.getNewConnection().createStatement();
    	
        ResultSet rs1 = stmr.executeQuery(salessql);
        while(rs1.next()){
        	String klient = rs1.getString("klient").trim();
        	
        	int summa = (int)rs1.getDouble("summa");
        	String kuu = rs1.getString("kuu");
          if(salesres.get(klient) == null){
        	  salesres.put(klient,"   " + kuu + " : " + summa + " lt" );
          }else{
        	  String asi = (String)salesres.get(klient);     	  
        	  salesres.put(klient,"   " + kuu + " : " + summa +" lt|" + asi);
          }
        }
       rs1.close();
       
  	 String order = "select ve.accountnum " + 
 	 		"    from  custtable  cus inner join Vendtable ve on ve.enterprisecode = cus.enterprisecode and cus.dataareaid = ve.dataareaid " + 
 	 		"	 left join DimensionAttributeValueSetItemView ww on ww.DimensionAttributeValueSet = cus.defaultdimension  " + 
 	 		"    where    cus.dataareaid='zp' ";
       
       HashSet orderOK = new HashSet();
       
       rs1 = stmr.executeQuery(order);
	     
	 
	        while(rs1.next()){
	        	String klient = rs1.getString("accountnum");
	        	if(klient != null && klient.trim().length() > 0) {
	        		orderOK.add(klient.trim());
	        	}
			       	
	        }
	        	//String ladu = rs1.getString("ladu").trim();
	        	
       rs1.close();
       stmr.close();    	
    	
       
       
       Statement stmu = this.getNewConnection().createStatement();
    	
         rs1 = stmu.executeQuery(sss);
        while(rs1.next()){
        	
       String ee = rs1.getString("accountnum").trim();
       double debti = rs1.getDouble("asi");
       if(debti != 0){
    	   custDebt.put(ee, debti + "");
       }
       double alld = rs1.getDouble("asi2");
       if(alld != 0){
    	   totalDebt.put(ee, alld + "");
       }
   
        }
       rs1.close();
       stmu.close();
       
    	
        stmr = this.getNewConnection().createStatement();
    	 rs1 = stmr.executeQuery(ccc);
        while(rs1.next()){
        	
       payers.add(rs1.getString("invoiceaccount"));
          
        }
       rs1.close();
       stmr.close();
    	
    	Statement stmt = this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      Statement stm = getNewConnection().createStatement();
      while(rs.next()){
        String name = rs.getString("name").replaceAll("UAB", " ").trim();
        
        
        String alias = rs.getString("nimelisa");
        if(alias != null && alias.trim().length()>0 && !alias.trim().equals(name)){
        //	if(!name.trim().startsWith(alias.trim()))
        	//name =  name + " " + alias ;
        }
        
        
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);
      
        name = name + " " + accountnum;
        name = name.replaceAll("\\r\\n|\\r|\\n", " ");
        name = name.replaceAll("'", " ").replaceAll("\"", " ").replaceAll("  ", " ");
        cust.setName( enCodeEst(name.trim()));
        
        String payer = rs.getString("payer");
        if(payer == null || payer.trim().length()== 0){
        	cust.setPayer(cust.getID());
        }else{
        	cust.setPayer(payer.trim());
        }
     
        cust.setPayer(payer);
        String debtStr = (String)custDebt.get(payer);
        if (debtStr != null){
          cust.setDebt(Double.parseDouble(debtStr));
        }
        String totalStr = (String)totalDebt.get(payer);
        if (totalStr != null){
          cust.setUsedCredit(Double.parseDouble(totalStr));
        }
        

        String aleke = rs.getString("linedisc");
        if(aleke != null && aleke.trim().length()>0){
            aleke = aleke.trim();
            cust.setGroup(aleke);
        } else {
        	aleke = "000" ;
        }
        
        if(salesres.get(accountnum) != null){
        	cust.setInfo("CHART Pardavimai :|M�nuo ir metai|Pardavimai Lt|" + salesres.get(accountnum) );
        }
        

        String aadr = rs.getString("address");
        if (aadr != null) {
          cust.setAddress(adressLink(enCodeEst(aadr.trim().replace('\n',' ')),"Lithuania").replaceAll("\\r\\n|\\r|\\n", " "));
        }
        
        String agen = rs.getString("agent");
      
        if (agen != null && agen.trim().length() >0) {
          cust.addAgent(new Agent(agen.trim()));
          cust.addAgent(new Agent("MILK"));
          cust.setMainAgent(agen.trim());
        }
        
        
        if(!orderOK.contains(accountnum)) {
        	cust.noOrder = true;
        }else {
        	cust.diffBack2 = true;
        }
        
        int term = rs.getInt("term");
        if (term > 0) {
          cust.setPaymTerm(term + "");
        }
                
        String info = rs.getString("info");
        if (info != null && info.trim().length()>0) {
          cust.setPersonalData(info.trim().replaceAll("\\r\\n|\\r|\\n", " "));
        }
        
        String smsaddr = rs.getString("smsaadr");
        if (smsaddr != null && smsaddr.trim().length()>0) {
        	smsaddr = smsaddr.trim().replaceAll("\\r\\n|\\r|\\n", " ");
        	smsaddr = smsaddr.replaceAll("\\s",""); 
        	if(smsaddr.startsWith("8")) {
        		smsaddr = "+370" + smsaddr.substring(1);
        	}
        	cust.setSMS = smsaddr;
        }
        
        
        
    String nfc = rs.getString("nfc");
    if (nfc != null && nfc.trim().length()>0) {
        cust.nfctag = nfc.trim();
     }
        
            boolean laks = true;
                    if((payer == null || payer.trim().length() == 0) && payers.contains(cust.getID())){
                    	//laks = false;
                    }
                    if(laks){
                        customers.put(cust.getID() + name, cust);
                        custMap.put(cust.getID(),cust);	
                    }
      }

      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim());
                        
                        String number = rs.getString("numbe").trim();
                        if(number != null && number.trim().length() > 0){
                            debt.setDoknr(number.replace(';',','));
                         }else{
                        	debt.setDoknr("NoNumber");
                        }
                        
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(rs.getDouble("tasumata"));
                        debt.setDate(rs.getDate("kuupaev"));
                        debt.setDue(rs.getString("yle"));
                        this.debt.addElement(debt); 	
                      }


    } catch (Exception e){
      getParent().showError(e);
    }
    return customers;
  }

  public static String adressLink( String addr ,String country){
		if(addr == null || addr.trim().length() == 0){
			return " ";
		}
		return ("." + addr.replace('.', '.').replace('.', '.')+ "," + country+ ".").replaceAll(",,", ",").replaceAll(" ", "_");
	}

  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(getParent().getProps().getProperty("dsn.host"), getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
  //

      public String getDistribution() {
        return null;
      }

     
  public TreeMap getProdGroups() throws SQLException {
    TreeMap   tm = new TreeMap();
    return tm;
}


    
  public TreeMap getProdGroupsSecond() throws SQLException {
	    TreeMap   tm = new TreeMap();
	    return tm;
  }
  
 
  

  public TreeMap getProducts(Agent agent){

	    String cmd = " SELECT mod.moduletype, inv.itemid, inv. namealias AS nimetus,  1 AS kastis,'' AS alusel, 1 AS price " +
	 "	  , '' AS ean ,inv.CATEGORYPRODUCTLINE_BBS AS grupp2,  inv.CATEGORYINVENTGROUP_BBS   AS grupp, '' as grid,mod.unitid As unit, 21 as vat , " +
	 "    0  as samm ,sortcode as sorting    FROM  inventtable inv " +
		"  left JOIN  inventtablemodule mod ON mod.itemid = inv.itemid and 'zp'=mod.DATAAREAID " +
		"   WHERE inv.dataareaid='zp' and mod.moduletype = 2   " +
		"   and inv.itemid like 'PP%'   order by inv.itemid ";
	    
	    //z p algusega
	  

	    if (isDebug()) System.out.println(cmd);
	    TreeMap products = new TreeMap();
	    try {
	           
		   Connection     ddd = getNewConnection();
	    	Statement stmt =  ddd.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	      HashMap prodHash = new HashMap();
	      if(rs.next()){
	        String kood = "P";
	        Product prod = new Product(kood);
	        String nimetus = enCodeEst( rs.getString("nimetus").trim() + " " + kood) ;
	       prod.setInCases(rs.getDouble("kastis"));
	       prod.setPrice(rs.getDouble("price"));
	       
  
	      prod.setName(( nimetus.replaceAll("'", " ").replaceAll("\"", " ").trim()).replaceAll("\n", " ").trim());

	      String unit = rs.getString("unit");
	      if(unit!=null && unit.trim().length()>0){
	       prod.setUnit(unit);
	    }
	      
	      String vat = rs.getString("vat");
	      if(vat!=null && vat.trim().length()>0){
	       prod.setVat("21");
	    }

	    
	        products.put(nimetus, prod);
	        prodHash.put(prod.getID(), prod);

	      }
	      rs.close();
	      stmt.close();
	      ddd.close();
	      
	    } catch (Exception e){
	      getParent().showError(e);
	    }

	    return products;
	  }



  public Vector getCustomerProductsList(String agentCode){
    Vector custProds = new Vector();
    HashMap custs = new HashMap();
    //if (true) return custProds ;
 int counter = 1;
 String cmd = " SELECT orderaccount as customer,tra.itemid ,tra.qty,jou.invoicedate AS lastdate, round(tra.lineamount/tra.qty,4) as lastprice " +
		    "   FROM  vendinvoicetrans  tra INNER JOIN  vendinvoicejour jou ON  " +
		    "   tra.invoiceid=  jou.invoiceid and  tra.DATAAREAID= jou.DATAAREAID " +
		    "   WHERE  jou.invoicedate > getdate()-45 AND tra.DATAAREAID = 'zp'  AND jou.DATAAREAID  = 'zp' and " +
		    "   (itemid like 'PP%') " +
		    "   GROUP BY orderaccount,tra.itemid,tra.name ,jou.invoicedate ,tra.qty,tra.lineamount  Order by orderaccount,tra.name,jou.invoicedate desc" ;
		  
   if (isDebug()) System.out.println(cmd);

    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      counter++;
      while(rs.next()){
    	
    	  
        String kliendikood = rs.getString("customer").trim();

        String tootekood = rs.getString("itemid").trim();
        if(!custs.keySet().contains(kliendikood+ tootekood)){
		           	
		       
		        double kogus = rs.getDouble("qty");
		        String lastdate = rs.getString("lastdate").substring(0,10);
		        lastdate = lastdate.substring(0,10);
		        custs.put(kliendikood + tootekood,lastdate); 
		        lastdate = lastdate.substring(5,10); 
		                 
		        CustProd cp = new CustProd(kliendikood, "P", kogus ,0 ,0,lastdate,0,0,0," ");
		        
		        custProds.add(cp);
        }
        
      }
      
     
 
      
      
    } catch (Exception e){
      getParent().showError(e);
      if(counter< 4){
      	try{
        	 ResultSet rs = stmt.executeQuery(cmd);
             counter++;
             while(rs.next()){
               String kliendikood = rs.getString("orderaccount").trim();
               String tootekood = rs.getString("itemid").trim();
               String lastdate = rs.getString("lastdate").substring(0,10);
                        lastdate = lastdate.substring(5,10);
               CustProd cp = new CustProd(kliendikood, "P", 0 ,0 ,0,lastdate,0,0,0," ");

               cp.setRedline(rs.getString("date"));
               custProds.add(cp);
             }
      	}catch (Exception ee){

      	}

      }
    }
    
    return custProds;
  }
  
 
  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


  public void setBulkSales(Map agents){
	  
	    
	      
	      
	      String cmd =" select stationid , ElectricityMeterNum as elnum, ElectricitySign as elcollect ,WaterMeterNum as wanum,WaterSign as wacollect "
	      		+ " from bbsStationTable where dataareaid = 'ZP' ";

	         if (isDebug()) System.out.println(cmd);
	            try {
	            	Connection con = this.getNewConnection();
	    	    	Statement stmt = con.createStatement();
	    	        ResultSet rs = stmt.executeQuery(cmd);
	    	        
	            
	                while(rs.next()) {
	                  String agentID = rs.getString("stationid").trim();
	                  Agent agent = (Agent)agents.get(agentID);
	                  if (agent != null){
	                  
	                   String elnum = rs.getString("elnum");
	                  
	                   Boolean elcollect = true;
	                   String eli = rs.getString("elcollect");
		                  if(eli != null && eli.trim().equals("0")) {
		                	  elcollect = false ;
		                  }
	                   
	                   String wanum = rs.getString("wanum");
	                   Boolean wacollect = true;
	                  
	                   String wai = rs.getString("wacollect");
		                  if(wai != null && wai.trim().equals("0")) {
		                	  elcollect = false ;
		                  }
	                   
	                 
	                   JSONObject js = new JSONObject();
	                   if(elnum != null && elnum.length() > 0) {
	                	  js.put("elnum", elnum.trim());
	                   }else {
	                	   js.put("elnum", "n/a");  
	                   }
	                   
	                   if(wanum != null && wanum.length() > 0) {
		                	  js.put("wanum", wanum.trim());
		                   }else {
		                	   js.put("wanum", "n/a");  
		                   }
	                   
	                   if(elcollect != null ) {
		                	  js.put("elcollect", elcollect);
		                   }

	                   if(wacollect != null ) {
		                	  js.put("wacollect", wacollect);
		                   }
	                   
	                   agent.techdata = js.toJSONString() ;
	                  }
	                }
	                rs.close();
	                stmt.close();
	                con.close();
	                
	            } catch (Exception e) {
	                getParent().showError(e);
	             }
	          }

    public double getFoodSales(String agentCode) throws SQLException{
      return 0;
    }

    public double getAlcoSales(String agentCode) throws SQLException{
      return 0;
    }

    public ManagerData getManagerData() throws SQLException{
      
      return null;
    }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
    }


  public String getStartingTime(){
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, - Integer.parseInt(getParent().getProps().getProperty("months")));
    int month = cal.get(Calendar.MONTH) + 1;
    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    String sqlDate = cal.get(Calendar.YEAR) + "-" + month + "-" + day;
    return sqlDate;
  }

  public String getDriverVersion(){
    return "AxaZpienas";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }


   public String getPilotDayData(Agent agent){

        
	 //  Paneme aruande failid kokku - igale punktile-managerile oma aruanne.
	   
	  // 1.  leiame aktiivsed punktid .
		
		Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	      
	     Enumeration enume = propse.keys();
	     while(enume.hasMoreElements()) {
	    	 String dot = (String)enume.nextElement();
	    	
	   
	   
	       String sql =  " select  ost.codefrom as klient,  ost.transdate as kuup, sum(purchqty)  as kogus , ost.milkacceptedtime as paev " + 
	       		   " from  BBsMilkJournalTrans ost where ost.dataareaid ='zp' and ost.transdate > getdate() -45 and purchqty  != 0 " + 
	       		   " and stationid = ? group by  ost.codefrom,  ost.transdate , ost.milkacceptedtime order by ost.codefrom , ost.transdate, ost.milkacceptedtime";
	    			     
	        
	StringBuffer sb = new StringBuffer();
	  this.connectionUp();
	try {
	
		
    
	Connection con = this.getNewConnection();
	PreparedStatement stmt2 = con.prepareStatement(sql);
	 stmt2.setString(1, dot);
	    ResultSet  rs = stmt2.executeQuery();
	 
	 while(rs.next()){
		 String id = rs.getString("klient").trim();
		 
		 String date = rs.getString("kuup").trim();
			
		 double kogus = rs.getDouble("kogus");
		  	 String paev = rs.getString("paev");
		  	 if(paev != null && paev.trim().length() > 0) {
		  		 paev = "(" + paev.trim() + ")";
		  	 } else {
		  		 paev = "";
		  	 }

	 	sb.append(id + ";");

	 	sb.append(  "PP;");
	 	sb.append(date.substring(0,10) + ";");
	 	String kog = NF.format(kogus); 
	 kog = 	kog.replace(',', '.');
	 	if(kog.indexOf(".") != -1) {
		 	sb.append( kog.substring(0,kog.indexOf(".") + 3) + ";" + paev + "\n");	
	 	}else {
		 	sb.append( kog + ";" + paev +  "\n");
	 	}
	 	
	   	
	 }
	 rs.close();
	 stmt2.close();
     con.close(); 	 
	

 	saveFile(parent.getProps().getProperty("savefilesdir") + "sal"+ dot + "report.txt", sb.toString());
 	
	} catch (Exception e) {
	 getParent().showError(e);
	}

	connectionDown();

	 
	 
	     }

	//System.out.println(sb.toString());
	return "";//sb.toString();

	    }

   
   public static void saveFile(String filename, String sb){
	      try{
	    	  
	    	  File f = new File(filename);
	          if (f.exists())f.delete();
	      	  Writer out = new BufferedWriter(new OutputStreamWriter(
	      			    new FileOutputStream(filename)));
	      			try {
	      			    out.write(sb.toString());
	      			} finally {
	      			    out.close();
	      			}
		            	
			    }catch(Exception e){
			    	e.printStackTrace();
			    }
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
       private String url = "jdbc:odbc:axauus";
       private String url2 = "jdbc:odbc:report";
  private Connection con;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
