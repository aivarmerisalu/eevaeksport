package eevaeksport.driver;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalLineWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class BuitNowacoLTDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager,PictureMaker,PictureMaker2 {

  public BuitNowacoLTDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    this.lang = parent.getProps().getProperty("language");
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	}
	user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
     }
  String user =  null;
  String pass = null;
  String rcompany = null;
    
  
  
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
	  
   public String getPilotDayData(Agent agent){

	   StringBuffer sb = new StringBuffer();
	           
	   String sql2 =  " select gavejas as klient,dok_serija as seria , kitas_dok as arve , data as kuupaev,(viso_1) AS summa,a.manager as agent ,vairuotoj as juht , tran_priem as autoke FROM apyvarta a " +
                      " where month(Data) =month(current_date()) and YEAR(Data) = YEAR(current_date()) " +
					" and day(Data) = day(current_date()) " +
				    "	and operacija  not like'%Reklam%' and operacija  not like'%Pirkimas%' and " +
				    "	(tiekejas is null or tiekejas like '%idvest%' ) and a.manager = '" + agent.getID()+ "'" +
				    "	order by gavejas";
	    
	
	   
	 //  System.out.println("siin");
	     String header = " Yesterdays sales :\n\n";
	    
	     boolean first = true;
	     nf.setMinimumFractionDigits(2);
	       try {
	       	
	       	
	             Connection con2 = getNewConnection();
       	
                Statement st = con2.createStatement();
               ResultSet rs2 = st.executeQuery(sql2);
                   while (rs2.next()) {
                   	if(first){
                   		sb.append(header);
                   		first = false;
                   	}
                   	String nimi = rs2.getString("klient").trim() ;
                    String juht =  rs2.getString("juht");
                    if(juht == null){
                    	juht = "";
                    }else{
                    	juht = juht.trim();
                    }
                    
                    String auto =  rs2.getString("autoke");
                    if(auto == null){
                    	auto = "";
                    }else{
                    	auto =  " " + auto.trim();
                    }
                    
                    String seria = rs2.getString("seria");
                  
                    if(seria == null){
                    	seria = "AAA";
                    }else{
                    	seria = seria.trim();
                    }
                    
                    String arve = rs2.getString("arve");
                    
                    if(arve == null){
                    	arve = "00000000";
                    }else{
                    	arve = arve.trim();
                    }
                    
                    String kuup = rs2.getString("kuupaev");
                    if(kuup == null){
                    	kuup = "0000000000";
                    }else{
                    	kuup = kuup.trim();
                    }
                    
                    String summa =  rs2.getString("summa");
                    if(summa == null){
                    	summa = "0000000000";
                    }else{
                    	summa = summa.trim();
                    }
                    
                   
                    
                    
                   sb.append(nimi  +
                   		"\n    (" + seria + "-" + arve + ")[" + kuup.substring(0,10) + "] " +
                   		summa + "lt  Trans.:" + juht + auto
                   		+  "\n\n");
                   }

                   rs2.close();
                   st.close();
                   con2.close();
                   
                   } catch (Exception e) {
                       getParent().showError(e);
                   }
      
  
       return sb.toString();

}




   public String getSaveDebtInfo() {
	   
	   makeAction();
	   String cmd =  "   SELECT k.klient_id AS kood,k.kodas AS payer  FROM klientai k  INNER JOIN " +
		"  (select klientas, tab_nr from (select * from manager order by klientas,data desc)as man  group by klientas order by klientas,data ) " +
		"   a ON a.klientas = k.klientas and a.tab_nr >0 LEFT join marsrut ma on ma.kodas = k.marsrutas where  filialas =1 " ;

	   //kellel on blokid 

String cmd3 = " select  b.kodas AS maksja,b.klient_id as klient,sum(skola_w) AS debt FROM apyvarta a INNER JOIN klientai b ON b.klientas=a.gavejas " +
      " where a.terminas < CURRENT_DATE() and  dok_serija is not null and (kitas_dok) > 0 and "
      + "dok_serija not in ( 'KRV5', 'LKS' ,'KRV7', 'LKP1','LDS','URM') and DATE_ADD(a.data,interval 500 day) > current_date() and operacija like 'Pardav%'  group by b.kodas having sum(skola_w) != 0 " ;
		


		if (isDebug()) System.out.println(cmd);
		TreeMap custs = new TreeMap();




		StringBuffer products = new StringBuffer();
		try {

	    Connection con = getNewConnection();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(cmd3);

		while (rs.next()) {
			String maks = rs.getString("maksja");
		String kood = rs.getString("klient");
		double volg = rs.getDouble("debt");
		Customer custw = null;
		if(maks != null &&  !maks.trim().equals("ND")){
			 custw = new Customer (maks.trim());
			custw.setDebt(volg);
			if(volg>0){
				custs.put(maks.trim(),custw);
			}
		}
		if(kood != null){
			 custw = new Customer (kood.trim());
			custw.setDebt(volg);
			if(volg>0){
				custs.put(kood.trim(),custw);
			}
		}
	
		
		}

		//siin pannakse peale PLOKKID

		rs = st.executeQuery(cmd);
		while (rs.next()) {
			String kood = rs.getString("kood").trim();
			String payer = rs.getString("payer");
		    Customer cus =  (Customer)custs.get(kood);
		    
		    if(cus != null ){
		    	cus.setSalesBlockT(true);
		    }else{
		    	Customer cusnew = new Customer(kood);
		    	cusnew.setSalesBlockT(true);
		    	custs.put(kood,cusnew);
		    }
		    
		    if(payer != null && !payer.trim().equals("ND") && payer.indexOf("123715317") == -1){
		    	 cus =  (Customer)custs.get(payer.trim());
			    if(cus != null){
			    	cus.setSalesBlockT(true);
			    }else{
			    	Customer cusnew = new Customer(payer.trim());
			    	cusnew.setSalesBlockT(true);
			    	custs.put(payer.trim(),cusnew);
			    }
		    }
		 }

		rs.close();
		st.close();
		con.close();
		
		} catch (Exception e) {
		getParent().showError(e);
		}
		
		
       this.getCustomers(null);
		
		
		
		Iterator     codes = custs.values().iterator();
		while (codes.hasNext()){
		Customer cust = (Customer)codes.next();
		String bl = "0";
		if(cust.isSalesBlockT()){
			bl = "2";
			}
		if(blockedcusts.contains(cust.getID())){
			bl = "2";
		}
		if(blockedcusts.contains(cust.getPayer())){
			bl = "2";
		}
		
		 products.append(cust.getID() + ";" + getParent().getNF().format(cust.getDebt()) + ";" + bl + "\n");
		}
       
		return products.toString();

      }

   private String getCustname(String id ) {
	   String sql =  "   SELECT k.klientas  AS nimi,k.klient_id AS kood,k.kodas AS payer,k.prek_adres AS aadress ,a.tab_nr AS agent ,k.dienos as paymterm,  " +
				" filialas AS blokk, 0 AS yle,'' AS visit,kainos_nr AS type," +
				"  atstovas as kontakt , k.kl_grupe as kett , max_skola as credit, kainos_nr as hinnak ,telefonai as phone , nuolaida as maxdisc , ma.dienos as mars "
				+ ",k.kl_grupe as rcatrgory , k.miestas AS rcity , k.indeksas AS rindex , k.sritis AS rmaakond , k.salis as rcountry,k.atstovas as rkontakt "+ 
				" ,k.telefonai as rperphone, k.e_mail as remail " +
				"  FROM klientai k  INNER JOIN " +
				"  (select klientas, tab_nr from (select * from manager order by klientas,data desc)as man  group by klientas order by klientas,data ) " +
				"   a ON a.klientas = k.klientas and a.tab_nr >0 LEFT join marsrut ma on ma.kodas = k.marsrutas where  ((gavejas = 1 and e_klientas = 1) or filialas =1) and k.klient_id = " + id ;

			 //  System.out.println(sql);
			   try {
			        Connection con = getNewConnection();
			        Statement stmt = con.createStatement();
			        ResultSet rs = stmt.executeQuery(sql);
			        while(rs.next()){
			         return rs.getString("nimi").trim();
			           }
			        rs.close();
			        stmt.close();
			        con.close();
			   }catch(Exception e) {
				   e.printStackTrace();
			   }
			   return id;
   }
  
   public  boolean isSameDay(long lll){
   	Calendar cc = Calendar.getInstance();
   	Calendar cal2 = Calendar.getInstance();
   	cal2.setTimeInMillis(lll);
   
       return (cc.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
               cc.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cc.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
   
   }
   
   public String makeAction() {
	   
		  // Siin teeme Cashi ära.
			  File dir = new File(getParent().getProps().getProperty("action.read"));
			  //  System.out.println("vvv "+ driver.getImportFile());
			    File[] files = dir.listFiles();
			
			    Random randomGenerator = new Random();
			    
			      
			    HashSet loetud = new HashSet();
			    for (int i = 0; i < files.length ; i++){
			      File file = files[i];
			      if (file.isFile()){
			    	  try{
			    		
			    	  FileReader fr = new FileReader(file);
						BufferedReader br = new BufferedReader(fr);
						String line = null;
						while( (line = br.readLine()) != null){
						
						 line = line.replaceAll(";;", "; ;");
					       
					      StringTokenizer st = new StringTokenizer(line, ";");
							if (st.hasMoreElements()){
								 // res = res +  ";" + getProd().getPrice() +  ";" + price +  ";" +getDiscount() + ";" + Utils.getCal(start).getTimeInMillis() + ";" + Utils.getCal(end).getTimeInMillis() + ";"
									//	   + Utils.getCal(end).getTimeInMillis() + ";" + com ;
		      String cuids = getCustname(st.nextToken());
		      
		      String toode = st.nextToken();
		      double kogus = Double.parseDouble(st.nextToken());
		      double price = Double.parseDouble(st.nextToken());
		      double priceGiven = Double.parseDouble(st.nextToken());
		      double discount = Double.parseDouble(st.nextToken());
		    
		      boolean addDate = true;
		      Long startD = Long.parseLong(st.nextToken());
		      Long endD = Long.parseLong(st.nextToken());  
		      Long priceD = Long.parseLong(st.nextToken());
		      if(isSameDay(endD) && isSameDay(startD)) {
		    	  addDate = false; 
		      }
		      if( endD < startD+100 ) {
		    	  addDate = false; 
		      }
		      
		      Calendar algus = Calendar.getInstance();
		      algus.setTimeInMillis(startD);
		      Calendar lopp = Calendar.getInstance();
		      lopp.setTimeInMillis(endD);
		    
		      String comment = " ";
		      try{
		    	  comment = st.nextToken();
		      }catch(Exception s){
		      }
		     
		      String uniq = toode + cuids + user + (startD+"") ;
		      StringBuffer xml = new StringBuffer();
		      
		      if(!loetud.contains(uniq)){
		    	
		    	  /*<klientas> Customer
		    	  <preke> product
		    	  <kaina> price
		    	  <valiuta> Currencie
		    	  <data_nuo> date_from
		    	  <data_iki> date till
                  */
		    	  
			      xml.append("<VFPData>");
			      xml.append("<Row>");
			      xml.append("<klientas>" + cuids + "</klientas>");
  				  xml.append("<preke>" + toode+ "</preke>");
  				  xml.append("<kaina>" + priceGiven + "</kaina>");
  				  xml.append("<valiuta>EUR</valiuta>");
  				  
  				  if(addDate) {
  					  xml.append("<data_nuo>" + this.getDateString(algus) + "</data_nuo>");
  	  				  xml.append("<data_iki>" + this.getDateString(lopp) + "</data_iki>");
  				  }
  				
  				  xml.append("</Row>");
  				  xml.append("</VFPData>");
		    	  
		    	  
		    	  
		    	  try{
		    		  
		    		  String res = null;
					  
						localhost.ButentWS.wsdl.ButentWS ws = new localhost.ButentWS.wsdl.ButentWSLocator();
						localhost.ButentWS.wsdl.ButentWebServiceSoapPort port = ws.getButentWebServiceSoapPort(); 
						String login = port.login("butentws", "wsadmin");
						login = port.login("butentws", "wsadmin");
						if (login.indexOf("Already logged in") != -1) {
							//saveFile("login-" + uniq + ".txt"  ,login);
							   String process = port.process("PostNuolaid", xml.toString());
								System.out.println("process=" + process);
								res = process; 
								//login=<Error><Message>Already logged in</Message></Error>
								//process=OK
								Converter.saveFile(getParent().getProps().getProperty("action.read") +"response/process-" + uniq + ".txt"  , process + "\n" + xml.toString().replaceAll("><", ">\n<"));
							  
						}else{
							port.login("butentws", "wsadmin");
							System.out.println("login=" + login);
							System.exit(0);
						}
					
		    	  
					}catch(Exception e){
						e.printStackTrace();
					    String err = e.toString();
							
					}
		    	  
		    	  
		      loetud.add(uniq);
		      }
							}
					
							
						
						
						
						} 
						br.close();
						fr.close();
						File dest = new File(getParent().getProps().getProperty("action.read") + "done/" + file.getName());
						if(dest.exists()){
							dest.delete();
						}
						System.out.println("renamed " + file.renameTo(dest));
			    	  }catch(Exception e){
			    		  e.printStackTrace();
			    	  }
			    	  }
			      
			    }
			   // System.exit(0);
			  return null;
		   }
   
   
   private String getDateString(Calendar cal){//2012-06-05
	    StringBuffer sb = new StringBuffer();
	    sb.append(cal.get(Calendar.YEAR) + "-" );
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append( month + "-");

	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day);

	    return sb.toString();
	  }


   public void copyFile(File in, File out) throws Exception {
       FileInputStream fis  = new FileInputStream(in);
       FileOutputStream fos = new FileOutputStream(out);
       byte[] buf = new byte[1024];
       int i = 0;
       while((i=fis.read(buf))!=-1) {
         fos.write(buf, 0, i);
         }
       fis.close();
       fos.close();
       }

   
   
   public Vector getPriceMatrixData() {
	   
	   String sql = "select max(klient_id) as custid, k.kodas AS clgroup,preke AS prodid,grup_id AS prodgroup, \r\n" + 
	   		"	      kaina AS price ,procentas as disc, data_iki as paev,data_nuo as startday from\r\n" + 
	   		"	        klientai k left JOIN nuolaid ale ON ale.klientas = k.sistema left join  grupes g on g.grupe=ale.grupe 	\r\n" + 
	   		"	    	where  data_iki is null  and (procentas != 0 or kaina > 0) and (preke >0 or grup_id >0) \r\n" + 
	   		"	    group by  k.kodas,preke,grup_id,kaina,procentas,data_nuo,data_iki";
	   //see oli esialgne
	   sql = "select max(klient_id) as custid, k.kodas AS clgroup,preke AS prodid,grup_id AS prodgroup, " +
			    "  kaina AS price ,procentas as disc, data_iki as paev,data_nuo as startday " +
			    "    from  nuolaid ale inner join klientai k  ON ale.klientas=k.klientas left join  grupes g on g.grupe=ale.grupe " +	
			    "	where   data_iki is null  and (procentas != 0 or kaina > 0) and (preke >0 or grup_id >0) " +
			    " group by  k.kodas,preke,grup_id,kaina,procentas,data_nuo,data_iki";
			   
	   sql = " select max(klient_id) as custid, k.kodas AS clgroup,preke AS prodid,grup_id AS prodgroup,\r\n" + 
	   		"	   			      kaina AS price ,procentas as disc, data_iki as paev,data_nuo as startday from\r\n" + 
	   		"	   			        klientai k left JOIN nuolaid ale ON ale.klientas = k.sistema left join  grupes g on g.grupe=ale.grupe 	 \r\n" + 
	   		"	   			    	 where  data_iki is null  and (procentas != 0 or kaina > 0) and (preke >0 or grup_id >0) \r\n" + 
	   		"	   			  group by  k.kodas,preke,grup_id,kaina,procentas,data_nuo,data_iki\r\n" + 
	   		"				UNION  \r\n" + 
	   		"				  select max(klient_id) as custid, k.kodas AS clgroup,preke AS prodid,grup_id AS prodgroup, \r\n" + 
	   		"	     kaina AS price ,procentas as disc, data_iki as paev,data_nuo as startday \r\n" + 
	   		"	        from  nuolaid ale inner join klientai k  ON ale.klientas=k.klientas left join  grupes g on g.grupe=ale.grupe 	\r\n" + 
	   		"	    	where   data_iki is null  and (procentas != 0 or kaina > 0) and (preke >0 or grup_id >0) \r\n" + 
	   		"		 group by  2,3,4,5,6,7,8 ";
	   
		//pï¿½si allahindlused toodetele ja gruppidele 
	  	 String    sql1 = "  select(klient_id) AS custid,k.kodas AS clgroup,  sistema as grupp " +
	       " from   klientai k where kodas != '' " +
		"	order by  k.kodas,klient_id " ;
	  	 

Vector result = new Vector();
  try {
	  TreeMap custs = new TreeMap();
	  
	  Connection con = getNewConnection();
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql1);
while (rs.next()) {
	String id = rs.getString("custid").trim();
	String pay = rs.getString("clgroup");
	String grupp = rs.getString("grupp");
	
	
	if(grupp != null && grupp.trim().length() > 0){
		grupp = grupp.trim();
		if(custs.get(grupp)!= null){
			Vector vv = (Vector)custs.get(grupp);
			vv.add(id);
			custs.put(grupp, vv);
		  } else {
			Vector vv = new Vector();
			vv.add(id);
			custs.put(grupp, vv);
		  }
	}
	
	
	if(pay != null && pay.trim().length() > 0){
	
	if(custs.get(pay.trim())!= null){
		Vector vv = (Vector)custs.get(pay.trim());
		vv.add(id);
		custs.put(pay.trim(), vv);
	}else{
		Vector vv = new Vector();
		vv.add(id);
		custs.put(pay.trim(), vv);
	}
	
	}
}
  	
	  Calendar now = Calendar.getInstance();
	  now.add(Calendar.MONTH, -1);
	  Calendar year = Calendar.getInstance();
	  year.add(Calendar.MONTH, 12);
	         rs = stmt.executeQuery(sql);
   
	 while (rs.next()) {
      String custID = rs.getString("custid");
      String clGroup = rs.getString("clgroup");
      String prodID = rs.getString("prodid");
      String prodGroup = rs.getString("prodgroup");
      double price = rs.getDouble("price");
      double disc = rs.getDouble("disc");
     
  
      MatrixLine mline = new MatrixLine();

      if (custID != null && custID.trim().length() > 0) {
        mline.setCustID(custID.trim());
      //  mline.setProdLevel("pr");
        
        mline.setStartDate(Converter.getDDsMMMString(now.getTime()));

        mline.setEndDate(Converter.getDDsMMMString(year.getTime()));
      }
   
      if (prodID != null && prodID.trim().length() > 0) {
        mline.setProdID(prodID.trim());
      }
      if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
        mline.setProdGroup(prodGroup.trim());
        mline.setDiscGroup(prodGroup.trim());
      }

      if (price > 0.01) {
        mline.setPrice(price);

      }
  
      if (disc != 0) {
          mline.setDiscount(disc);
      }
    
      
      if( mline.getPrice() > 0 || mline.getDiscount() > 0){
          result.add(mline);    
        if(clGroup != null && clGroup.trim().length() >0 && custs.get(clGroup.trim()) != null){
        	Vector vv = (Vector)custs.get(clGroup.trim());
        	for (int i =0 ; i< vv.size();i++){
        		String aa = (String)vv.get(i);
        		MatrixLine mm = new MatrixLine();
        		mm.setCustID(aa);
        		mm.setPrice(mline.getPrice());
        		mm.setProdID(mline.getProdID());
        		mm.setStartDate(Converter.getDDsMMMString(now.getTime()));
        	    mm.setEndDate(Converter.getDDsMMMString(year.getTime()));
        		
        	    if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
              		mm.setProdGroup(mline.getProdGroup()); 
        		 }
        		 result.add(mm); 
        	}
        }
      }
    }
    rs.close();
    
    
    sql = "select ale.klientas AS clgroup,preke AS prodid,grup_id AS prodgroup, " + 
	    " kaina AS price ,procentas as disc, data_iki as paev,data_nuo as startday , datediff(data_iki ,data_nuo) as kamp " + 
	     "  from  nuolaid ale inner join klientai k  ON ale.klientas=k.sistema left join  grupes g on g.grupe=ale.grupe  " +
	    "	where ((current_date() between data_nuo and data_iki) or(current_date() > data_nuo and data_iki is null)) and ( preke >0 or grup_id >0) " +
		"	group by ale.klientas ,preke,grup_id,kaina,procentas,data_nuo,data_iki " ;


	  
    rs = stmt.executeQuery(sql);

while (rs.next()) {
String clGroup = rs.getString("clgroup");
String prodID = rs.getString("prodid");
String prodGroup = rs.getString("prodgroup");
double price = rs.getDouble("price");
double disc = rs.getDouble("disc");


MatrixLine mline = new MatrixLine();



if (prodID != null && prodID.trim().length() > 0) {
mline.setProdID(prodID.trim());
}

if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
mline.setProdGroup(prodGroup.trim());
mline.setDiscGroup(prodGroup.trim());
mline.setStartDate(Converter.getDDsMMMString(now.getTime()));

mline.setEndDate(Converter.getDDsMMMString(year.getTime()));
}

if (price > 0.01) {
mline.setPrice(price);

}

if (disc != 0) {
 mline.setDiscount(disc);
}


if( mline.getPrice() > 0 || mline.getDiscount() > 0){
 //result.add(mline);    
if(clGroup != null && clGroup.trim().length() >0 && custs.get(clGroup.trim()) != null){
	Vector vv = (Vector)custs.get(clGroup.trim());
	for (int i =0 ; i< vv.size();i++){
		String aa = (String)vv.get(i);
		MatrixLine mm = new MatrixLine();
		mm.setCustID(aa);
		mm.setPrice(mline.getPrice());
		mm.setProdID(mline.getProdID());
		 mm.setStartDate(Converter.getDDsMMMString(now.getTime()));

	        mm.setEndDate(Converter.getDDsMMMString(year.getTime()));
		 if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
     		mm.setProdGroup(mline.getProdGroup()); 
		 }
		 result.add(mm);
		// System.out.println("sisse sistema kaudu " + aa + " " + mline.getPrice() + " " +mline.getProdID());
	}	
}
}
}
rs.close();
    
    // teeme hinnakirjad valmis 
    
    for (int i = 1 ;i < 11; i++){
    	sql = "select " + (i+5) + " as pr1,preke ,prek_id as prodid, kaina_" + i + " as price from prekes where kaina_" + i + " < 321.77";
    	 con = getNewConnection();
	         stmt = con.createStatement();
	         rs = stmt.executeQuery(sql);
 while (rs.next()) {

   String clGroup = rs.getString("pr1").trim();
   String prodID = rs.getString("preke").trim();
   double price = rs.getDouble("price");
  
   MatrixLine mline = new MatrixLine(); 
     mline.setCliGroup(clGroup.trim());
     mline.setProdID(prodID);
     mline.setPrice(price);
     
     if(mline.getPrice()> 0.05 && mline.getPrice() < 1110 && !clGroup.trim().equals("11")){
    	 result.add(mline);    
    	    
     }
        }
    }
    
    //kampaaniate kysimine
    
    sql= " select max(klient_id) AS custid,k.kodas AS clgroup,preke AS prodid,grup_id AS prodgroup,  " +
	    " kaina AS price ,procentas as disc, data_iki as paev,data_nuo as startday , datediff(data_iki ,data_nuo) as kamp " +
	    "    from  nuolaid ale inner join klientai k  ON ale.klientas=k.klientas left join  grupes g on g.grupe=ale.grupe  " +
	"		where ((current_date() between data_nuo and data_iki) or(current_date() > data_nuo and data_iki is null) and ( preke >0 or grup_id >0)) " +
	"		group by  k.kodas,preke,grup_id,kaina,procentas,data_nuo,data_iki";

    rs = stmt.executeQuery(sql);
    while (rs.next()) {

      String custID = rs.getString("custid");// Converter.enCodeEst(rs.getString("custid").trim());
      String clGroup =  rs.getString("clgroup");
      String prodID = rs.getString("prodid");
      String prodGroup = rs.getString("prodgroup");     
      double price = rs.getDouble("price");
      double disc = rs.getDouble("disc");
      String date = rs.getString("paev");
      String sdate = rs.getString("startday");
      String kamp = rs.getString("kamp");
    
   
      MatrixLine mline = new MatrixLine();

      if (custID != null && custID.trim().length() > 0) {
        mline.setCustID(custID.trim());
      }
    

      if(kamp != null && kamp.trim().length() >0 ){
    	  try{
         if(Integer.parseInt(kamp.trim()) < 60){
        	 mline.setEndDate(date.substring(0,10));
        	 mline.setStartDate(sdate.substring(0,10));
        	  }  
    	  }catch(Exception e){ }  }
    	
    
       	 if (prodID != null && prodID.trim().length() > 0) {
           mline.setProdID(prodID.trim());
          }
     
       	 if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
    	  mline.setProdGroup(prodGroup.trim());
          mline.setDiscGroup(prodGroup.trim());
      }
      
 

      if (price != 0) {
           mline.setPrice(price);
         }else if (disc != 0) {
           mline.setDiscount(disc);
         }
      
      if( (mline.getPrice() > 0.01 && mline.getPrice() < 1110 )|| mline.getDiscount() > 0){
    	 
           
          
          if(clGroup != null && clGroup.trim().length() >0 && custs.get(clGroup.trim()) != null){
        	  mline.setCliGroup(clGroup.trim());
        	  mline.setCustID(null);
        	  Vector vv = (Vector)custs.get(clGroup.trim());
          	for (int i =0 ; i< vv.size();i++){
          		String aa = (String)vv.get(i);
          		MatrixLine mm = new MatrixLine();
          		mm.setCustID(aa);
          		mm.setPrice(mline.getPrice());
          		mm.setProdID(mline.getProdID());
          		 if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                		mm.setProdGroup(mline.getProdGroup()); 
          		 }
          		 result.add(mm); 
          	 }
          }
          
          result.add(mline);  
       }
    }
    
    stmt.close();
    con.close();
    }catch(Exception e){
 	 e.printStackTrace();
    }
	
   return result;

   }

   private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
   

   public void insertDefective(DefectiveWrapper wr){
 
  }
   
	public Vector getLastThreeData() {
		return null;
	}

	public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr) {
		String sql = "INSERT into dbo.supervisor values (" + "?, " // agent
																	// C(50),
				+ "?," // agentname C(200),
				+ "?," // clientid C(50),
				+ "?," // clientname C(200),
				+ "?," // prodid C(50),
				+ "?," // prodname C(200),
				+ "getdate() ," // sentdate D,
				+ " '' ," // receivedTime D,
				+ "?," // normalpr N,
				+ "?," // amoun N,
				+ "?," // grouptotal N,
				+ "?," // cattotal N,
				+ "?," // pertotcat I,
				+ "?," // percolcat I,
				+ "?," // perpalcat I,
				+ "?," // pershecat I,
				+ "?," // perrescat I,
				+ "?," // isaction L,
				+ "?," // ispartner L,
				+ "?," // isbrand L,
				+ "?," // isgroup L
				+ "?," // actionaeva N,
				+ "?," // actionbeva N,
				+ "?," // actiontota N,
				+ "?," // custeval N,
				+ "?," // custcomme C(200),
				+ "?," // comment C(200),
				+ "?," // custagent C(200),
				+ "?," // custagentname C(200),
				+ "?," // mustbe I,
				+ "?," // ideal I,
				+ "?," // resstr1 C(200),
				+ "?," // resstr2 C(200),
				+ "?," // resstr3 C(200),
				+ "?," // resint1 I,
				+ "?," // resint2 I,
				+ "?," // resint3 I,
				+ "?)"; // country L

		try {
			System.out.println("jam");
			Connection con = getNewConnection();
			PreparedStatement ps = con.prepareStatement(sql);

			for (Iterator iter = wr.getInfo().getLines().iterator(); iter
					.hasNext();) {
				eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(
						iter.next());
				ps.setString(1, wr.getInfo().getAgentID());
				ps.setString(2, Converter.deCode(wr.getInfo().getAgentName()));
				ps.setString(3, lw.getLine().getCustID());
				ps.setString(4,  Converter.deCode(lw.getLine().getCustName()));
				ps.setString(5, lw.getLine().getProdID());
				ps.setString(6,  Converter.deCode(lw.getLine().getProdName()));
				ps.setDouble(7, lw.getLine().getNormalPrice() );
				ps.setDouble(8, lw.getLine().getAmount() );
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
				ps.setDouble(20, lw.getLine().getAction1Eval());
				ps.setDouble(21, lw.getLine().getAction2Eval());
				ps.setDouble(22, lw.getLine().getActionTotalEval());
				ps.setDouble(23, lw.getLine().getCustEval());
				String cc = lw.getLine().getCustComment();
				if (cc == null) {
					cc = " ";
				}
				ps.setString(24,  Converter.deCode(cc));

				String cd = lw.getLine().getComment();
				if (cd == null) {
					cd = " ";
				}
				ps.setString(25,  Converter.deCode(cd));

				String ce = lw.getLine().getCustAgentId();
				if (ce == null) {
					ce = " ";
				}

				ps.setString(26, ce);

				String cf = lw.getLine().getCustAgentName();
				if (cf == null) {
					cf = " ";
				}
				ps.setString(27,  Converter.deCode(cf));
				ps.setInt(28, lw.getLine().getDealNr());
				ps.setInt(29, lw.getLine().getIdealNr());

				String cr1 = lw.getLine().getResStr1();
				if (cr1 == null) {
					cr1 = " ";
				}
				ps.setString(30, cr1);

				String cr2 = lw.getLine().getResStr2();
				if (cr2 == null) {
					cr2 = " ";
				}
				ps.setString(31, cr2);

				String cr3 = lw.getLine().getResStr3();
				if (cr3 == null) {
					cr3 = " ";
				}

				

				ps.setString(32, cr3);
				ps.setInt(33, lw.getLine().getResInt1());
				ps.setInt(34, lw.getLine().getResInt2());
				ps.setInt(35, lw.getLine().getResInt3());
				ps.setString(36, "et");
				ps.executeUpdate();
			}

			ps.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
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


	public String getDateStr() {
		Calendar cal = Calendar.getInstance();
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
	
	 public String getProductSortingData(){
	   	return null;
	   }

   private String ensureLen(String str, String replace){
    if (str == null) str = "";
    else str = str.trim().replace('\'', ' ').replace('\"', ' ').replace(';',' ');
    if (str.length() == 0){
      return replace;
    } else {
      return str;
    }
  }

   public ManagerData getManagerData() throws SQLException{

    String tableName = "dayreport";
    if(getParent().getProps().getProperty("report.tablename") != null && !getParent().getProps().getProperty("report.tablename").trim().equals("")){
    	tableName = getParent().getProps().getProperty("report.tablename");
    }

    String sql = " SELECT clid AS klient,sentdate AS kuupaev,docsum AS summa,linecount AS ridu," +
    		"text AS tekst,'' AS dealer,clgroup AS agentname" +
        " FROM  " + tableName +
        " WHERE sentdate>(GETDATE()-30) ORDER BY clid ";

   if (isDebug()) System.out.println(sql);
   this.con = getNewConnection();
   Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    StringBuffer sb = new StringBuffer();
    String sep = ";";

    while(rs.next()){
      String cliID = rs.getString("klient").trim();
      sb.append(cliID + sep);
      sb.append(rs.getString("kuupaev").trim() + sep);
      sb.append(getNF().format(rs.getDouble("summa")) + sep);
      sb.append(rs.getInt("ridu") + sep);
      String tekst = ensureLen(rs.getString("tekst")  ," ");
      tekst = tekst.replace('\'', ' ').replace('\"', ' ');
      tekst = tekst.replace('\n', ' ');
      String agendinimi = rs.getString("agentname");
      sb.append(tekst + sep);
        sb.append(getNF().format(0) + sep);
        sb.append(getNF().format(0) + sep);
        sb.append(ensureLen((agendinimi), " ") + sep);
        sb.append(ensureLen((" "), " ") + sep);
        sb.append(ensureLen((""), " ") + sep);
        sb.append((" ") + sep);
        sb.append((" ") + sep);
      sb.append("\n");
    }

    ManagerData md = new ManagerData();
    md.setClientVisitInfo(sb.toString());

 StringBuffer bf2 = new StringBuffer();
 Enumeration enums = klient.elements();
 while(enums.hasMoreElements()){
 	Customer cust = (Customer)enums.nextElement();
 	Iterator agen = cust.getAgents().keySet().iterator();
 	while(agen.hasNext()){
    Agent ag = 	(Agent)agents.get((String)agen.next());
   bf2.append(cust.getID() + sep);
   bf2.append(ag.getID() + sep);
   bf2.append(ag.getName() + "\n");
 }
	}

    md.setClientAgentInfo(bf2.toString());
    return md;

  }


   public void insertDistribution(DistEvalListWrapper wr){
 
   }

 
   public Vector getFixedAssortmentData() {
           Vector lines = new Vector();
        /*   boolean insertassort = true;
           
           try {
        	   String sql = "SELECT sortdate from mj_assortment where  year(sortdate) = year(getdate()) and" +
        	   		" month(sortdate) = month(getdate()) and day(sortdate) = day(getdate()) and country ='et' ";
         	 
         	    Connection cons = this.getNewConnection();
     	        Statement stmts = cons.createStatement();
     	        ResultSet rss = stmts.executeQuery(sql);
     	       
     	        if(rss.next()){
     	        	insertassort = false;
     	        }   
     	        
     	        rss.close();
     	        stmts.close();
     	        cons.close();
     	        
           }catch(Exception d){
        	   d.printStackTrace();
           }
     	
           
           try {
          	  String cmd = "  select s.[Item No_] as toode,s.[Assortment Code] + s.[Assortment Level] as grupp from " +
          	  		" [Viï¿½iunai Baltic$Item Assortment Level] s INNER JOIN [Viï¿½iunai Baltic$Item] t on t.No_= [Item No_] " +
          	  		" ORDER BY [Search Description]";
          	System.out.println(cmd);
             	 Connection con = getNewConnection();
         	        Statement stmt = con.createStatement();
         	        ResultSet rs = stmt.executeQuery(cmd);
         	        while(rs.next()){
                            String custID = " ";
                            String clGroup = " ";
                            String prodID = " ";
                            String prodGroup = " ";
                            MatrixLine mline = new MatrixLine();
                            prodID = rs.getString("toode");
                            clGroup = rs.getString("grupp");
                         

                            if (custID != null && custID.trim().length() > 0) {
                              mline.setCustID(custID.trim());
                            }
                            if (clGroup != null
                                && clGroup.trim().length() > 0) {
                              mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
                            }

                            if (prodID != null && prodID.trim().length() > 0) {
                              mline.setProdID(prodID.trim());
                            }
                            if (mline.getProdID() == null && prodGroup != null
                                && prodGroup.trim().length() > 0) {
                              mline.setProdGroup(prodGroup);
                            }
                            if(insertassort){
	                               insertSortLine(mline);   
	                           }
                            lines.add(mline);
                          }

         	       rs.close();
         	       con.close();
                         } catch (Exception e) {
                             getParent().showError(e);
                         }*/
           return lines;
   }
   
   

   private void  insertSortLine(MatrixLine mline){
		  try{
				
			      String sql = "INSERT INTO mj_assortment values (" +
			         "'" + mline.getCustID() + "'," + 
			         "getdate()," + 
			         "'" + mline.getCliGroup() + "'," + 
			         "'" + mline.getProdID() + "'," + 
			         "0," + 
			         "0,'','et')" ;
			  
			  	     Connection  con2 = this.getNewConnection();      
				     Statement stmt2 = con2.createStatement();
				     stmt2.executeUpdate(sql);
				     con2.close();
		  }catch(Exception e){
			  e.printStackTrace();
		  }		  
	  }

   public DealerProduct DealerProductsList() {

   	DealerProduct dealerProds = new DealerProduct();
    try {
    	  String cmd = "SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [Viï¿½iunai Baltic$Item Ledger Entry] l " +
          " INNER JOIN [Viï¿½iunai Baltic$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('TAL') " +
          " AND t.[Blocked]=0 AND t.[Unit Price]>0 AND len(t.[Grupï¿½s kodas])>0 " +
          " GROUP BY l.[Location Code],l.[Item No_] ORDER BY l.[Item No_] ";
       

       	 Connection con = getNewConnection();
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(cmd);
   	        while(rs.next()){
   	             String toode = null;
   	             double seis = 0;;
   	             String ladu  = null;
   	              	toode =rs.getString("kood");
   	              	seis = rs.getDouble("kogus");
   	              	ladu = rs.getString("ladu");
   	                 dealerProds.add(ladu, toode,seis+""," "," " );
   	        }

   	      rs.close();
   	      con.close();

   	    System.out.println(cmd);
    } catch (Exception e) {
        e.printStackTrace();
    }
     return dealerProds;
   }
   public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }


   private void saveFile(String fileName,String content ){
		File fil = new File( getImportFile() + "reponse/");
   	if(!fil.exists()){
   		fil.mkdir();
   	}
 try{
		PrintWriter os = new PrintWriter(new FileWriter( fileName , true));
		os.write(content);
		os.close(); 

       System.out.println("Faili salvestus " + fileName); 
 }catch(Exception r){
	  r.printStackTrace();
 }
     
        
 }
   
   public static Document loadXMLFromString(String xml) throws Exception
   {
       DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
       DocumentBuilder builder = factory.newDocumentBuilder();
       InputSource is = new InputSource(new StringReader(xml));
       return builder.parse(is);
   }
   
   
   private void makePictureCat(){
	     
	   	String  cmd2 = " SELECT t.preke AS toode  " +
	" FROM prekes t inner join grupes g on g.grupe=t.grupe inner join tipai ty on ty.tipas=t.tipas " +
	" where pard_kaina != 0 or kaina_1 < 321.77 or kaina_2 < 321.77 or kaina_3 < 321.77 or kaina_4 < 321.77 or kaina_5 < 321.77 or " +
	" kaina_6 < 321.77 or kaina_7 < 321.77 or kaina_8 < 321.77 or kaina_9  < 321.77 or kaina_10 < 321.77 ";

	    
	   	
	     	String picts = parent.getProps().getProperty("pict.dir");
	 	    String pictspda = parent.getProps().getProperty("pictpda.dir");
	 	 

	   	try {
	 	   Connection con = getNewConnection();
		
	   		Statement stmt = con.createStatement();
	   	    ResultSet	      rs = stmt.executeQuery(cmd2);


	        StringBuffer buf = new StringBuffer();
	        StringBuffer buf0 = new StringBuffer();
	        int a = 0;
	        int b = 0;
	        int c = 0;
	        
	        // leiame kataloogid ja peame meeles 
	        Vector cats = new Vector();
	        File d  = new File(picts);
	        File[] dd = d.listFiles();
	     
	        //System.out.println("file " + picts + " " + d.exists() );
		     
	        
	        for(int i = 0; i < dd.length ; i++){
	        	File t = dd[i];
	        	if(t.isDirectory()){
	        		cats.add(t.getName());
	        	}
	        }
	        
	        System.out.println("Piltide leidmine - katalooge sees " + cats.size() + " " + cats.toString());
	   	    while (rs.next()) {
	   	    	a++;
	   	    	c++;
	   	    	
	   	     String toode = rs.getString("toode").trim();  
	         toode = toode.replace('/', '_');
	         toode = toode.replace('/', '_');
	   	    	if(c == 100){
	   	    		  c = 0;
	   	    		  System.out.println("Piltide leidmine "  + a);	
	   	    		  
	   	    	}
	   	     
	              
	                 File f = new File(picts + toode+".jpg");
	                 String len = "0";
	                 if(f.exists()){
	                   len =	f.length() + "";
	                 } else if(cats.size() > 0 ){
	                	 for(int w = 0 ;w < cats.size();w++){
	                		f = new File(picts + cats.elementAt(w) + "/" + toode+".jpg");
	                		
	                		if(f.exists()){
	                		//	System.out.println("Kataloogis leitud " + picts + cats.elementAt(w) + "/" + toode+".jpg");	
	                		     len =	f.length() + "";	
	                		}                     
	                	 }
	                 }
	                 
	                 if(len.equals("0")){
	                 	//System.out.println(toode+".jpg;"  + len);
	                 	buf0.append(toode+".jpg\n");
	                 }else{
	                 	buf.append(toode+".jpg;"  + len + "\n");
	           	    	b++;
	                 }
	                 }
	   	    rs.close();
	   	    stmt.close();
	   	    con.close();
	   	
	   	    //oleasoleva kataloogi kustutamine
	   	//  System.out.println("Kataloogi kustutamine "  + a);	    
	               File f = new File("list.txt");
	               if (f.exists())f.delete();
	               
	                 try {
	           		PrintWriter os = new PrintWriter(new FileWriter(
	           				  "list.txt", true));
	           		os.write(buf.toString());
	           		os.close();
	           	
	           	  }catch (Exception ex) {
	           		ex.printStackTrace();
	           	  }
	           	//  System.out.println("buf 0 " + a  + " " +  b + " "+ buf0.toString());
	           	  File f0 = new File("list0.txt");
	           	
	           	  	
	                    if (f0.exists())f0.delete();
	                 
	                   try {
	             		PrintWriter os1 = new PrintWriter(new FileWriter(
	             				  "list0.txt", true));
	             		os1.write(buf0.toString());
	             		os1.close();
	             		//System.out.println( buf.toString());
	             	  }catch (Exception ex) {
	             		ex.printStackTrace();
	             	  }

	   //Kataloogi sisude kustutamine
	             	  
	  
	          	 
	          	 
	        	 File res2 = new File (pictspda);
	            String[] ff2 = res2.list();
	        	 for(int i = 0;ff2.length>i;i++){
	            //	System.out.println("fff " + ff2[i]);
	            	(new File ( pictspda + ff2[i])).delete(); 
	         
	            }
//	        	 Lï¿½ppes kustutamine 
	        	 
	          System.out.println("Piltide kopeerimine ");	
	          //Failide kopeerimine  
	            String[] gg = new File(picts).list();
	         	  //System.out.println("gg " + gg[0] + " " + gg.length);
	            
	             for(int i = 0;gg.length>i;i++){          	
	             	File in = new File ( picts + gg[i]); 
	             	if(in.isFile()){            	
	             	File inpda = new File ( pictspda + toUpper(gg[i])); 
	                   	Converter.shrinkAndCopyFile(in,inpda);
	             	}
	             }
	             
	             //piltide kopeerimine alam kataloogist ..
	             
	             if(cats.size() >0){
	            	 for(int h = 0;h < cats.size(); h++){
	            		
	            	   String[]  ff = new File(picts + cats.elementAt(h)+ "/").list();
	               	 
	                  
	                   for(int i = 0;i < ff.length  ;i++){          	
	                   	File in = new File ( picts + cats.elementAt(h)+ "/" + ff[i]); 
	                   	if(in.isFile()){   
	                   		
	                   	File inpda = new File ( pictspda + toUpper(ff[i])); 
	                   	Converter.shrinkAndCopyFile(in,inpda);
	                   	 }
	                   }
	                   
	            	 }
	             }
	             
	             File fff =  new File (pictspda +  "list.txt");
	       	  copyFile(f,fff);  	
	       	  File fff0 =  new File (pictspda +  "list0.txt");
	       	  copyFile(f0,fff0);  	
	   	} catch (Exception e) {
	   	    getParent().showError(e);
	   	}
	   
	}
	 
	   private String toUpper(String asi ){
		   
		   asi = asi.substring(0,asi.length()-4).toUpperCase() + asi.substring(asi.lastIndexOf("."));
		   System.out.println(asi);
		   return asi ;
	   }
	   
	

   
      private TreeMap<String,Double> stocks = new TreeMap<String,Double> ();
   private TreeMap<String,Vector> stocksdet = new TreeMap<String,Vector> ();
   
   public String getSaveStockInfo(){
	   StringBuffer products = new StringBuffer();
	 	   stocks = new TreeMap<String,Double> ();
	 		try {
	 	
	 		
	 			String cmd = "  SELECT preke , (kiekis ) as kiekis  FROM likuciai where kiekis  > 0 and sandelis in ('S6','S4','B1','S4(b)')  order by preke  ";
	 	     	 
	 			String cmd2 = "SELECT  gr.preke AS preke,SUM(gr.kiekis) AS ORDERED FROM  apyv_gr AS gr " +
     "  LEFT JOIN apyvarta AS a ON gr.apyv_id = a.apyv_id " +
     "  WHERE  a.ivestas IS NULL AND a.tiek_sand IN ('S6' , 'S4', 'B1' ,'S4(b)') " +
     "   AND gr.kiekis > 0  and gr.kiekis is not null AND a.gav_sand is null GROUP BY gr.preke  order by gr.preke";
	 			
	 	       	 Connection con = getNewConnection();
	 	 
	 	   	        Statement stmt = con.createStatement();
	 	   	        ResultSet rs = stmt.executeQuery(cmd2);
	 	   	        TreeMap tell = new TreeMap();
	 	   	        
	 	   	    
 	   	        while(rs.next()){
 	   	         tell.put(rs.getString("preke").trim(), rs.getDouble("ordered"));
       		    }
       			rs.close();
 	   	        
 	   	        rs = stmt.executeQuery(cmd);
 	   	        
	 	   	  while(rs.next()){
	 	   	        
	       		 String id =  rs.getString("preke").trim();
	       			double kogus =  rs.getDouble("kiekis");
	       			double kogusm = 0 ;
	       			if(tell.get(id) != null){
	       				kogusm = Double.parseDouble(tell.get(id) + "");
	       				tell.remove(id);
	       			}
	       			
	       			kogus = kogus - kogusm;
	       		
	       				if(stocks.get(id)!= null){
	       					stocks.put(id, stocks.get(id) + kogus);
	       				}else{
	       					stocks.put(id, kogus);
	       				}
	       			
	 	   	        }
	       			
	       		   
	      	  }catch(Exception e){
	      	   e.printStackTrace();	  
	      	  }
	      
	     Iterator itera = stocks.keySet().iterator();
	     while (itera.hasNext()){
	    	 
	     	String id = itera.next() + "";
	     	double kogust = stocks.get(id);
	     	if(kogust > 0 ){
	    	    products.append( id+  ";");
	            products.append(" ;");
	            products.append(getParent().getNF().format(stocks.get(id)) + "; "  +  "\n"); // + dealer +  "\n");	
	     	}
	     
	     }
	 		
	 	   
	     return products.toString();
	    }
   
   
   HashMap ketid = new HashMap();
  
	    
   
   public Vector getDebt(String agentCode){
        return this.debtList;
  }

   public void connectionDown(){
   }

   public void connectionUp(){
   }

           public String getDealerCustomerCSV(String agent){
            return null;
            }

           public String  getCustProdDiscounts(Agent agent){
            return null;
            }

   public TreeMap getProdGroups() throws SQLException {
     TreeMap   tm = new TreeMap();
     //setTreeGroup(true);
     String cmd = "  SELECT  tip_id AS kood,tipas AS nimi  FROM tipai order by tipas";
     	 
       	 Connection con = getNewConnection();
 
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(cmd);
   	        while(rs.next()){
   	            String id = rs.getString("kood").trim();
   	                String name = rs.getString("nimi").trim() ;
   	             tm.put(id, Converter.enCodeEst(name));
   	        }
   	        
   	      rs.close();
   	      con.close();

         return tm;
   }



   public TreeMap getProdGroupsSecond() throws SQLException {
	   String cmd = "  SELECT  grup_id AS kood,grupe AS nimi  FROM grupes order by grupe";
	   TreeMap   tm = new TreeMap();
     	 Connection con = getNewConnection();

 	        Statement stmt = con.createStatement();
 	        ResultSet rs = stmt.executeQuery(cmd);
 	        while(rs.next()){
 	            String id = rs.getString("kood").trim();
 	                String name = rs.getString("nimi").trim() ;
 	             tm.put(id, Converter.enCodeEst(name));
 	        }
 	        
 	      rs.close();
 	      con.close();

       return tm;
    }


   private String cleardots(String idx){
   	while(idx.indexOf(".")>0){
    idx = idx.substring(0,idx.indexOf("."))+idx.substring(idx.indexOf(".")+1);
   	}

	   return idx;
   }

   public TreeMap getAgents(){
	   agent1 = new TreeMap();
	   
	  // getHigher();
        String cmd = " SELECT tab_nr AS kood,CONCAT(pavarde,' ' ,vardas) AS nimi,'' AS objekt, darb_kod as dubs FROM darbuot " +
		" WHERE padalinys like '%Vady%' ";
	    if (isDebug()) System.out.println(cmd);
	    TreeMap agents = new TreeMap();
	    try {
	        Connection con = getNewConnection();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	          Agent agent = new Agent(rs.getString("kood").trim());
	          String name = rs.getString("nimi").trim();
	          agent.setObject(rs.getString("objekt"));
	          
	          agent.setName(name);
	          agent.setIsPilot(true);
	          
	          String dubs = rs.getString("dubs");
	          
	          if(dubs != null && dubs.trim().length() > 2){
	        	  dubs = dubs.trim();
	        	  if(dubs.indexOf(",") != -1 ){
	        		  StringTokenizer stag = new StringTokenizer(dubs, ",");
					    while(stag.hasMoreTokens()){  	
		                 agent.addDubbler(stag.nextToken());
		               //  System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
					    }  
	        	  }else  if(dubs.indexOf(";") != -1 ){
	        		  StringTokenizer stag = new StringTokenizer(dubs, ";");
					    while(stag.hasMoreTokens()){  	
		                 agent.addDubbler(stag.nextToken());
		               //  System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
					    }  
	        	  }else{
	        		  agent.addDubbler(dubs.trim()); 
	        		   //System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
						  
	        	  }
	        	 
	          }

				String agentAGENTS = getParent().getProps().getProperty("dub." +agent.getID());
				if(agentAGENTS != null){
    	           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
			    while(stag.hasMoreTokens()){  	
                 agent.addDubbler(stag.nextToken());
                 //System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
			    }

				}
				 agent.setLanguage("LIT");
		          agent.setCountry("lt");
		          agent.setPassWord("0000");
	             String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
	             if(email.length() > 3){
	            	 agent.setEmail(email);
	             }
	             if(email.length() > 0){
	            	 agent.setActive(true);
	             }else{
	            	 agent.setActive(false);
	             }
	        
	          agents.put(name, agent);
	          agents1.put(agent.getID(), agent);
	          agent1.put(agent.getID(), agent);
	        }

	        Agent aa = new Agent("HOUSE1");
	        aa.setName("office1");
	        agents.put("office1", aa);
            rs.close();
	        con.close();
	        
	        
	
	    } catch (Exception e){
	        getParent().showError(e);
	      }


     return agents;
   }

       public String getDistribution() {
         return null;
       }

       private TreeMap agent1 = new TreeMap();
       
       
   public TreeMap getCustomers(Agent agent){

	     TreeMap discs = new TreeMap(); 
 try {
	 Converter.getManagersReplacements(agent1, rcompany, user , pass);
	 
	 String cmd = " select  b.kodas AS maksja,b.klient_id as klient,sum(skola_w) AS summa FROM apyvarta a INNER JOIN klientai b ON b.klientas=a.gavejas " +
      " where a.terminas < CURRENT_DATE() and  dok_serija is not null and (kitas_dok) > 0 and "
      + " dok_serija not in ( 'KRV5', 'LKS' ,'KRV7' , 'LKP1' , 'LDS', 'URM') and operacija like 'Pardav%'"
      + " and DATE_ADD(a.data,interval 500 day) > current_date() and b.kodas != 'ND' group by b.kodas having sum(skola_w) != 0 " ;
	 
//System.out.println(cmd);
   Connection   cons = this.getNewConnection();
     Statement stmt1 = cons.createStatement();
     ResultSet rs1 = stmt1.executeQuery(cmd);
     while(rs1.next()){

       String id = rs1.getString("maksja");
       String kl = rs1.getString("klient");
       String summa = rs1.getString("summa");
       if(id != null && !id.trim().equals("ND")) {
    	   overdebts.put(id,summa);
       }
       if(kl != null)
       overdebts.put(kl,summa);
  }

     rs1.close();
     stmt1.close();
     cons.close();
	 
     
     
     
     
	 String cmd2 = " select  b.kodas AS maksja,b.klient_id as klient,sum(skola_w) AS summa FROM apyvarta a INNER JOIN klientai b ON b.klientas=a.gavejas " +
     "  where dok_serija is not null and (kitas_dok) > 0 and dok_serija not in ( 'KRV5', 'LKS' ,'KRV7', 'LKP1','LDS','URM') and operacija like 'Pardav%' "
     + " and DATE_ADD(a.data,interval 500 day) > current_date()  group by b.kodas having sum(skola_w) != 0 " ;
System.out.println(cmd2);
  Connection   cons2 = this.getNewConnection();
    Statement stmt12 = cons2.createStatement();
    ResultSet rs12 = stmt12.executeQuery(cmd2);
    while(rs12.next()){

      String id = rs12.getString("maksja");
      String kl = rs12.getString("klient");
      String summa = rs12.getString("summa");
      if(id != null && !id.trim().equals("ND")) {
    	  totaldebts.put(id.trim(),summa);
      }
    
      if(kl != null) {
    	  totaldebts.put(kl.trim(),summa);
      }
     
 }
    rs12.close();
    
    cmd2 = " select  b.kodas AS maksja,b.klient_id as klient,sum(skola_w) AS summa FROM apyvarta a INNER JOIN klientai b ON b.klientas=a.gavejas \r\n" + 
    		"      where dok_serija is not null and (kitas_dok) > 0 and DATE_ADD(a.data,interval 500 day) > current_date() and dok_serija not in"
    		+ " ( 'KRV5', 'LKS' ,'KRV7', 'LKP1','LDS','URM') and operacija like 'Pardav%' \r\n" + 
    		"	   group by b.klient_id having sum(skola_w) != 0 " ;
    	System.out.println(cmd2);
    	 
    	   rs12 = stmt12.executeQuery(cmd2);
    	    while(rs12.next()){

    	      String kl = rs12.getString("klient");
    	      String summa = rs12.getString("summa");
    	    
    	      if(kl != null) {
    	    	  totaldebts.put(kl.trim(),summa);
    	      }
    	     
    	 }
    	     


    
  
    rs12.close();
    stmt12.close();
    cons2.close();
     
     
     cmd = " select klient_id AS custid, procentas as disc " +
    " from  nuolaid ale inner join klientai k  ON ale.klientas=k.klientas left join  grupes g on g.grupe=ale.grupe " +		
	"	where   data_iki is null  and (procentas != 0 ) and (preke is null and grup_id is null) group by klient_id" ;

     cons = this.getNewConnection();
     stmt1 = cons.createStatement();
     rs1 = stmt1.executeQuery(cmd);
    while(rs1.next()){

     
      String kl = rs1.getString("custid");
      String dis = rs1.getString("disc");
      discs.put(kl, dis);
  
 }

    rs1.close();
    stmt1.close();
    cons.close();
   
    
     
     
       cmd = " select b.klient_id AS klient, b.kodas AS maksja,concat(dok_serija , ' ' , kitas_dok) AS number, " + 
       		"             case when a.Terminas is null then date_add(a.data,INTERVAL " + 
       		"			  (select k.dienos from klientai k where k.kodas = b.kodas and length(rtrim(k.kodas) > 3) and k.kodas is not null LIMIT 1 )\r\n" + 
       		"			   DAY) else a.Terminas end AS kuupaev, " + 
       		"            a.data as kuup,a.Terminas, b.dienos, skola_w AS summa, " + 
       		"			 DATEDIFF(case when a.Terminas is null  then date_add(a.data,INTERVAL  " + 
       		"			 (select k.dienos from klientai k where k.kodas = b.kodas and length(rtrim(k.kodas) > 3) and k.kodas is not null LIMIT 1 )  DAY) else a.Terminas end,Current_DATE()) AS due ," + 
       		"            b.kodas AS payer , (select k.dienos from klientai k where k.kodas = b.kodas and length(rtrim(k.kodas) > 3) and k.kodas is not null LIMIT 1 ) as term" + 
       		"			FROM apyvarta a INNER JOIN klientai b ON b.klientas=a.gavejas " + 
       		"         where   skola_w != 0 " + 
       		"             and (kitas_dok) > 0 and dok_serija not in ( 'KRV5', 'LKS' ,'KRV7', 'LKP1','LDS','URM') and " + 
       		"             DATE_ADD(a.data,interval 500 day) > current_date() and operacija like 'Pardav%' "; 
       
      ketid = new HashMap();
      sortimendid = new HashMap();
  //System.out.println(cmd);
       this.debtList = new Vector();
     Connection   con = getNewConnection();
        stmt1 = con.createStatement();
        rs1 = stmt1.executeQuery(cmd);
       while(rs1.next()){
        Debt debt = new Debt();
       String custID = rs1.getString("klient");
        debt.setCustID(custID.trim());
        
        String maks = rs1.getString("maksja");
        if(maks != null && maks.trim().length() >0) {
        	debt.setPayerID(maks.trim());
        }
        
try{
	 String doknr = rs1.getString("number").trim();    
     debt.setDoknr(doknr);
}catch(Exception e){}
 Date kuup = rs1.getDate("kuup");
 Date kuupm = rs1.getDate("kuupaev");
 if(kuupm != null){
	   debt.setDuedate(kuupm);
 }else{
	  debt.setDuedate(kuup);
 }
        debt.setDate(kuup);
        debt.setSum(rs1.getDouble("summa"));
        int dueint = -1 * rs1.getInt("due");
        debt.setDue(dueint + "");
        int old = 0;
        if(debtDays.get(custID) != null){
         old = Integer.parseInt((String)debtDays.get(custID));
        }
        
        if(old<dueint){
           debtDays.put(custID,dueint+"") ;
           if(dueint > 210){
        	  	  // System.out.println("siit 2 tuleb block" + custID);
                	   
        	 this.blockedcusts.add(custID);  
           }
        }
        if(debt.getDoknr() != null){
        	 this.debtList.add(debt);
        }
       
            }

       rs1.close();
       con.close();
      } catch (Exception e){
        e.printStackTrace();
      }
/// KLIENDI ANDMETE LEIDMINE

     TreeMap tm = new TreeMap();
     this.customers = new ArrayList();
  	TreeMap freeze = new TreeMap();


  	String agextra = getParent().getProps().getProperty("all.customers");

   String sql =  "    SELECT k.klientas  AS nimi,k.klient_id AS kood,k.kodas AS payer,k.prek_adres AS aadress ,a.tab_nr AS agent ,k.dienos as paymterm,  \r\n" + 
   		"	 filialas AS blokk, 0 AS yle,'' AS visit,kainos_nr AS type,\r\n" + 
   		"  atstovas as kontakt , k.kl_grupe as kett , max_skola as credit, kainos_nr as hinnak ,telefonai as phone , nuolaida as maxdisc , ma.dienos as mars \r\n" + 
   		"	,k.kl_grupe as rcatrgory , k.miestas AS rcity , k.indeksas AS rindex , k.sritis AS rmaakond , k.salis as rcountry,k.atstovas as rkontakt \r\n" + 
   		"	 ,k.telefonai as rperphone, k.e_mail as remail \r\n" + 
   		"	  FROM klientai k  INNER JOIN \r\n" + 
   		"	 ( select klientas, tab_nr from(\r\n" + 
   		"	   select klientas, tab_nr from (select * from manager order by klientas,data desc,tab_nr )as man \r\n" + 
   		"	  group by klientas,data order by klientas,data desc) as mm group by klientas ) \r\n" + 
   		"	   a ON a.klientas = k.klientas and a.tab_nr >0 LEFT join marsrut ma on ma.kodas = k.marsrutas where  (gavejas = 1 and e_klientas = 1) or filialas =1\r\n" + 
   		"	   order by k.klientas " ;

 //  System.out.println(sql);
   try {
        Connection con = getNewConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while(rs.next()){
         String name = rs.getString("nimi").trim();
           String id = rs.getString("kood").trim();
           //System.out.println(line);
           Customer customer = new Customer(id);
           customer.setCountry("Lietuva");
            String payer = null;
           if (name.equals("")) name = "<" + id + ">";
           customer.restName = name;
           customer.setName(Converter.enCodeEst(name));
           	payer = rs.getString("payer");
           
           	if(payer != null && payer.trim().length()>0){
           		if(!payer.trim().equals("ND")){
           		   customer.setPayer(payer.trim());
                   customer.setRegnr(payer.trim());	
           		}
             }
           	
           	if(discs.get(customer.getID()) != null){
           		customer.setDiscount(Double.parseDouble((String)discs.get(customer.getID())));
           	}

             String aadress = rs.getString("aadress");
             if( aadress != null && aadress.trim().length()>0){
            
               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress.trim()),"Lithuania"));
               customer.setName(customer.getName()+ " " + Converter.enCodeEst(aadress.trim()));
               customer.restStreet = aadress.trim();
             }
         	String ag = rs.getString("agent");
             customer.addAgent(new Agent(ag));
             customer.setMainAgent(ag.trim());
             customer.addAgent(new Agent("HOUSE1"));
             if ( ag != null && agents1.get( ag.trim()) != null){
                 Agent aa = (Agent) agents1.get( ag.trim());
                  Vector vec = aa.getDubbler();
                  if( vec!= null && vec.size() > 0 ){
                       Enumeration en = vec.elements();
                       while(en.hasMoreElements()){
                            String ags = (String) en.nextElement();
                         customer.addAgent( new Agent(ags));
                      }
                 }
           }
             
             if ( ag != null && agent1.get( ag.trim()) != null){
                 Agent aa = (Agent) agent1.get( ag.trim());
                  Vector vec = aa.getDubbler();
                  if( vec!= null && vec.size() > 0 ){
                       Enumeration en = vec.elements();
                       while(en.hasMoreElements()){
                            String ags = (String) en.nextElement();
                         customer.addAgent( new Agent(ags));
                      }
                 }
           }
             
             if(agextra != null){
            	 StringTokenizer st = new StringTokenizer(agextra,",");
            	 while(st.hasMoreTokens()){
            		 customer.addAgent(new Agent(st.nextToken()));
            	 }
             }
             if (ag != null && ag.trim().length()>0){
 				Agent agg = ((Agent)this.getAgents1().get(ag.trim()));
 				
 				if(agg != null && agg.getDubbler().size()>0){
 					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
 						customer.addAgent(new Agent((String)agg.getDubbler().elementAt(i)));
 					//	System.out.println(customer.getName() + customer.getAgents());
 					}
 				}
 			}
             
             String paym = rs.getString("paymterm");
             
             
             /*String allagents = rs.getString("toall");
             if (allagents.trim().equals("1")) {
               Enumeration enume = getParent().getAgentsHash().elements();
               while (enume.hasMoreElements()) {
                 Agent a = (Agent) enume.nextElement();
                 customer.addAgent(a);
               }
             }*/
             
             String blokk = rs.getString("blokk");
             if( blokk != null && blokk.trim().equals("1") && customer.getName().indexOf("Rimi") == -1){
               customer.setSalesBlockT(true);
               blockedcusts.add(customer.getID());
               if(customer.getName().indexOf("Rimi") != -1){
             	  // System.out.println("siit 3 tuleb block");
                }
               
             }

         

        /*   	String num = rs.getString("alekood");
           	if(num != null && num.trim().length()>0){
             customer.setDiscount(Double.parseDouble((num.trim())));
           	}
*/

      		String visit = rs.getString("visit");
             if(visit != null && visit.trim().length()>0){
                customer.setVisit(visit.trim());
             }

            String type = rs.getString("type");
            if(type != null && type.trim().length()>0 && !type.trim().equals("11")){
            		customer.setCustType(type.trim());
            }

           
   

            if(customer.getPayer() != null  && this.overdebts.get(customer.getPayer()) != null
            		&& !customer.getPayer().equals("ND")){
            	customer.setDebt(Double.parseDouble((String)overdebts.get(customer.getPayer())));
            }else if( this.overdebts.get(customer.getID()) != null){
            	customer.setDebt(Double.parseDouble((String)overdebts.get(customer.getID())));
            }
            	
          
            if(customer.getID().equals("17493")) {
            	System.out.println("oioioioioi " +  customer.getDebt() + " " + customer.getPayer() + " " + overdebts.get(customer.getPayer()) + " " + overdebts.get(customer.getID()));
            }
            	
   
          
           if(paym != null && paym.trim().length()>0){
        	  customer.setPaymTerm(paym);
             }

           
           
          //
           String cont = rs.getString("kontakt");
         
           String kett = rs.getString("kett");
           if(kett != null && kett.trim().length()>0){
        	   if(ketid.get(Converter.cleanLitLatEst(kett.trim())) != null){
           		Vector vv = (Vector)ketid.get(Converter.cleanLitLatEst(kett.trim()));
           		vv.add(customer.getID());
           		ketid.put(Converter.cleanLitLatEst(kett.trim()), vv);
           	}else{
           		Vector vv = new Vector();
           		vv.add(customer.getID());
           		ketid.put(Converter.cleanLitLatEst(kett.trim()), vv);
           	}
           }
           
           double credit = rs.getDouble("credit");
           if(credit > 0){
        	   customer.setCredit(credit + "");
           }
         
         
           
           if(customer.getPayer() != null && !customer.getPayer().equals("ND") && this.totaldebts.get(customer.getPayer()) != null 
        		   && customer.getName().indexOf("Rimi") == -1){
           	customer.setUsedCredit(Double.parseDouble((String)totaldebts.get(customer.getPayer())));
           	
           	
          if(credit > 0 && (credit - Double.parseDouble((String)totaldebts.get(customer.getPayer()))) < -100){
            	customer.setSalesBlockT(true);
            	blockedcusts.add(customer.getID());
            	blockedcusts.add(customer.getPayer());
            	
            }
           }else if( this.totaldebts.get(customer.getID()) != null){
           	customer.setUsedCredit(Double.parseDouble((String)totaldebts.get(customer.getID())));
           
           	if(credit > 0 && (credit - Double.parseDouble((String)totaldebts.get(customer.getID()))) < -100
            		 && customer.getName().indexOf("Rimi") == -1){
             	customer.setUsedCredit(Double.parseDouble((String)totaldebts.get(customer.getID())));
            	customer.setSalesBlockT(true);
            	blockedcusts.add(customer.getID());
            }
           }
           
           if( this.totaldebts.get(customer.getID()) != null){
        		customer.setUsedCredit(Double.parseDouble((String)totaldebts.get(customer.getID())));
           }
              
         
           String phone = rs.getString("phone");
           
           if(cont != null && cont.trim().length()>0){
               customer.setPersonalData(Converter.enCodeEst(cont.trim().replaceAll(";", ",")));   
           }
           
           if(phone != null && phone.trim().length()>0){
               customer.setPersonalData( customer.getPersonalData() + Converter.enCodeEst(phone.trim().replaceAll(";", ",")));   
           }
           
           
           double maxdisc = -1;
           try{
        	   maxdisc =  rs.getDouble("maxdisc");;
           }catch(Exception g){
        	   maxdisc = 0;
           }
           
           if(maxdisc  != 99){
        	   customer.setMaxdisc(maxdisc);
           }
           
           String mars = rs.getString("mars");
           if(mars  != null && mars.trim().length() > 0){
        	   customer.setCity("Marsrutai dienos :" + mars.trim());
        	   String asi = null;
        	   if(mars.trim().length() < 4){

            	   if(mars.contains("1")){
            		   asi = ("|  data pirmadieniais!");
            	   }
            	   
            	   if(mars.contains("2")){
            		   asi = asi + ("|  data antradieniais!");
            	   }
            	   
            	   if(mars.contains("3")){
            		   asi = asi + ("|  data treï¿½iadieniais!");
            	   }
            	   
            	   if(mars.contains("4")){
            		   asi = asi + ("|  data ketvirtadieniais!");
            	   }
            	   
            	   if(mars.contains("5")){
            		   asi = asi + ("|  data penktadieniais!");
            	   }
            	   
        	   }
        	   
        	   if (asi != null){
        		   customer.setInfo("Dï¿½mesio ï¿½iam klientui pristatymo " + asi );  
        	   }
        	   
           }
           

           String rcatrgory = rs.getString("rcatrgory");
           if(rcatrgory != null && rcatrgory.trim().length() > 0){
        	   customer.category = rcatrgory;
           }
           
           String rcity = rs.getString("rcity");
           if(rcity != null && rcity.trim().length() > 0){
        	   if(customer.restStreet != null && !customer.restStreet.toLowerCase().contains(rcity.trim().toLowerCase())){
        		 //  customer.restCity = rcity;
        	   }
           }
           
           
           String rindex = rs.getString("rindex");
           if(rindex != null && rindex.trim().length() > 0){
        	   if(customer.restStreet != null && !customer.restStreet.toLowerCase().contains(rindex.trim().toLowerCase())){
        		   customer.restZipCode = rindex;
        	   }
           }
           
           String rcountry = rs.getString("rcountry");
           if(rcountry != null && rcountry.trim().length() > 0){
        	   if(customer.restStreet != null && !customer.restStreet.toLowerCase().contains(rcountry.trim().toLowerCase())){
        		   customer.setCountry(rcountry);
        	   }
           }
           
           String atstovas = rs.getString("rkontakt");
           if(atstovas != null && atstovas.trim().length() > 0){
        	      customer.restPerson = (atstovas);
        	   
           }
           
           
           String rperphone = rs.getString("rperphone");
           if(rperphone != null && rperphone.trim().length() > 0){
        	      customer.restPerPhone = (rperphone);
        	   
           }
           
           String remail = rs.getString("remail");
           if(remail != null && remail.trim().length() > 0){
        	      customer.restPerEmail = (remail);
        	   
           }
          
          
           
            tm.put(customer.getName(), customer);
            this.customers.add(customer.getID());
           
         }

        rs.close();
        con.close();



     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }

   public TreeMap getAgents1() {
		return agents1;
	}
   

   private TreeMap agents1 = new TreeMap();
   private HashMap prodHash = new HashMap();
   
   private void getPartiid(){
	   
	   try {
			  
	    	 String cmd =  "  select preke as toode , kiekis  as kogus , sandelis as ladu ,artikulas as bb from likuciai"
	    	 		+ " where  sandelis in ('S6','S4','B1' ,'S4(b)')  and kiekis  > 0";
	    	//	  System.out.println(cmd);
	    	 
	    	 
		

	      	 Connection con = getNewConnection();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs = stmt.executeQuery(cmd);
	  	        while(rs.next()){
              String id = rs.getString("toode");
              double kogus = rs.getDouble("kogus");
              String ladu = rs.getString("ladu");
              String partii = rs.getString("bb");
            
              if(partii != null){
            	if(stocksdet.get(id)!= null){
	  	  				Vector<Toode> vv = stocksdet.get(id);
	  	  				Toode tt = new Toode(id);
	  	  				tt.setKogus(kogus);
	  	  				tt.setLadu(ladu);
	  	  				tt.setBrand(partii.trim());
	  	  				vv.add(tt);
	  	  				stocksdet.put(id, vv);
	  	  			}else{
	  	  				Vector<Toode> vv = new Vector<Toode>();
	  	  				Toode tt = new Toode(id);
	  	  				tt.setKogus(kogus);
	  	  				tt.setLadu(ladu);
	  	  				tt.setBrand(partii.trim());
	  	  				vv.add(tt);
	  	  				stocksdet.put(id, vv);
	  	  			}
                  }
	  	        }

	  	      rs.close();
	  	      con.close();
	         System.out.println(cmd);
	   
	     } catch (Exception e) {
	       e.printStackTrace();
	   }
	   
	   
   }
   
   HashSet docCodes = new HashSet();
   
   
   
   
   private HashSet getHigher() {
	   HashSet hh = new HashSet();
	   try {
		   DataFormatter formatter = new DataFormatter();
		   Workbook workbook = null;
	   File f = new File (getExcelDir() + "ProductCost.xlsx");
	   boolean xs = true;
	   if(!f.exists()) {
		   f = new File (getExcelDir() + "ProductCost.xls"); 
		   xs = false;
	   }
	   
		  FileInputStream	 ff = new FileInputStream(f);
		if(xs) {
		     workbook = new XSSFWorkbook((ff));
		}else {
			 workbook = new HSSFWorkbook((ff));
		}
		Sheet sheet = workbook.getSheetAt(0);
	      Row row = null; 
				ff.close();	
				if(sheet != null){
				  int yy = sheet.getLastRowNum();
					
					for(int i = 0 ; i < yy+1 ;i++){
						 row = sheet.getRow(i);
						if(row != null) {
							
							String ean = formatter.formatCellValue(row.getCell(0));
							String ok = formatter.formatCellValue(row.getCell(1));
							if(ok.equals("1")) {
								hh.add(ean.trim());
							}
						}}}
				
	   }catch(Exception e) {
		   e.printStackTrace();
	   }				
	   System.out.println("hh " + hh.toString());
	   return hh;
   }
   
   
   
   private HashMap<String, Double> getSuggDisc() {
	   HashMap<String, Double> hh = new HashMap<String, Double>();
	   try {
		   DataFormatter formatter = new DataFormatter();
		   Workbook workbook = null;
	   File f = new File (getExcelDir() + "RecDisc.xlsx");
	   boolean xs = true;
	   if(!f.exists()) {
		   f = new File (getExcelDir() + "RecDisc.xls"); 
		   xs = false;
	   }
	   
		  FileInputStream	 ff = new FileInputStream(f);
		if(xs) {
		     workbook = new XSSFWorkbook((ff));
		}else {
			 workbook = new HSSFWorkbook((ff));
		}
		Sheet sheet = workbook.getSheetAt(0);
	      Row row = null; 
				ff.close();	
				if(sheet != null){
				  int yy = sheet.getLastRowNum();
					
					for(int i = 1 ; i < yy+1 ;i++){
						 row = sheet.getRow(i);
						if(row != null) {
							
							String ean = formatter.formatCellValue(row.getCell(0)).trim();
							String ale = formatter.formatCellValue(row.getCell(1));
						  if(ale != null && ale.trim().length() > 0) {
							  try {
								  hh.put(ean.trim(), Double.parseDouble(ale)); 
							  }catch(Exception r) {
								  r.printStackTrace();
							  }
						  }
						}}}
				
	   }catch(Exception e) {
		   e.printStackTrace();
	   }				
	   System.out.println("REC " + hh.size());
	   return hh;
   }
   
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
     HashSet  ladutr = new HashSet();
     HashMap<String,Double>  ladub1 = new HashMap<String,Double>();
     HashMap<String,Double>  ladub1o = new HashMap<String,Double>();
     
     HashSet higherCodes = getHigher();
     
     HashMap<String,Double> suggDisc = getSuggDisc();
     
     getSaveStockInfo();
     getPartiid();
      prodHash = new HashMap();
      String inP = parent.getProps().getProperty("doc.origpath");
      String outP = parent.getProps().getProperty("doc.destination");
    
      docCodes = new HashSet();
     try {
	  
    	 String cmd =  " select * from (	SELECT  r.preke AS toode  FROM apyv_gr r " +
    		" INNER JOIN apyvarta d ON r.apyv_id= d.apyv_id and DATE_ADD(data,interval 30 day) > current_date() " +
    		" INNER JOIN klientai b ON b.klientas=d.gavejas " +
    		" where DATE_ADD(data,interval 30 day) > current_date() order by klient_id ,r.preke ,data desc ) as tab group by toode";
    		  System.out.println(cmd);
    	 
    	 
	

      	 Connection con = getNewConnection();
  	        Statement stmt = con.createStatement();
  	        ResultSet rs = stmt.executeQuery(cmd);
  	        while(rs.next()){
  	             String toode = null;
  	             double seis = 0;;
  	              	toode =rs.getString("toode");
  	              	ladutr.add(toode);
  	        }

  	      rs.close();
  	      con.close();
         System.out.println(cmd);
   
     } catch (Exception e) {
       e.printStackTrace();
   }
     try {
      	  
    	 String cmd = "    SELECT preke as toode, sum(kiekis) as seis FROM likuciai where kiekis  > 0 "
    	 		+ " and sandelis  IN ('S6' , 'S4')  group by preke order by preke  ";
    	   	  System.out.println(cmd);
    	 
    	 
	

      	 Connection con = getNewConnection();
  	        Statement stmt = con.createStatement();
  	        ResultSet rs = stmt.executeQuery(cmd);
  	        while(rs.next()){
  	             String toode =rs.getString("toode").trim();
  	             double seis = rs.getDouble("seis");
  	              ladub1o.put(toode,seis);
  	              
  	        }

  	      rs.close();
  	      con.close();
         //System.out.println("ladub1o " + ladub1o.toString());
   
     } catch (Exception e) {
       e.printStackTrace();
   }
     
     try {
   	  
    	 String cmd = "    SELECT preke as toode, sum(kiekis) as seis FROM likuciai where kiekis  > 0 "
    	 		+ " and sandelis  in ('B1' ,'S4(b)')  group by preke   ";
    	   	  System.out.println(cmd);
    	 
    	 
	

      	 Connection con = getNewConnection();
  	        Statement stmt = con.createStatement();
  	        ResultSet rs = stmt.executeQuery(cmd);
  	        while(rs.next()){
  	             String toode =rs.getString("toode").trim();
  	             double seis = rs.getDouble("seis");
  	             if(ladub1o.get(toode) == null){
  	            	 ladub1.put(toode,seis);
  	             }
  	             
  	              
  	        }
  	   //   System.out.println("ladub1 " + ladub1.toString());
  	    
  	        
  	      rs.close();
  	      con.close();
         System.out.println(cmd);
   
     } catch (Exception e) {
       e.printStackTrace();
   }
     
 	  
     Hashtable dateOut = new Hashtable();
     StringBuffer buf = new StringBuffer();
     try {
     	String cmd = " SELECT t.preke AS toode,t.prek_id as id,t.pavad AS nimi , t.pavad_3 AS nimiu , t.pard_kaina AS myygihind , kaina_1 as hind1, kaina_2 as hind2,  kaina_3 as hind3, kaina_4 as hind4, " +
 " kaina_5 as hind5, kaina_6 as hind6, kaina_7 as hind7, kaina_8 as hind8, kaina_9 as hind9, kaina_10 as hind10,grup_id AS grupp ,tip_id AS tupe ,t.tipas  " +
	"		 , t.mato_vien AS unit, t.bar_kodas AS ean,0 AS status ,t.max_nuol AS maxdiscount,  " +
	"		  t.tipas AS type, t.orig_pak AS kastis,t.pak_kiek as kastis2 ,  t.prek_neto AS kaal,21 AS vat,  pak_kiek as opt ,prek_mode as info ,t.vieta as staatus  " +
	 "    , orig_pak as origpak, kilm_salis as klimes , laik_salyg as laiksal ,"
	 + " pak.pakuote as primpak ,t.savik_3 as oma , t.didm_antk as greend, pavad_3 as brandpers, t.aprasymas as otsing " +	
	"		  FROM prekes t inner join grupes g on g.grupe=t.grupe inner join tipai ty on ty.tipas=t.tipas " +
	"     left join pr_pak pak on pak.preke = t.preke and pak.pirmine = 1   " +
	"		  where CH_meris  = '1' and (pard_kaina != 0 or kaina_1 < 321.77 or kaina_2 < 321.77 or kaina_3 < 321.77 or kaina_4 < 321.77 or kaina_5 < 321.77 or \r\n" + 
	"			 kaina_6 < 321.77 or kaina_7 < 321.77 or kaina_8 < 321.77 or kaina_9  < 321.77 or kaina_10 < 321.77)" ;
	
      System.out.println(cmd);
     	Connection con = getNewConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
        	boolean goes = false;
            String id = rs.getString("toode").trim();
            String weight ="";
            Product prod = new Product(id);
            if(suggDisc.get(id) != null) {
            	prod.suggAle = suggDisc.get(id) + "";
            	//System.out.println("suggAle " + prod.suggAle);
            }
            prod.setStock(0);
            if(stocks.get(id) != null &&  stocks.get(id) > 0){
            	prod.setStock(stocks.get(id));
            	goes = true;
            }
            
            if(stocksdet.get(id) != null){
            	Vector<Toode> vv = stocksdet.get(id);
            	for(int i = 0; i < vv.size();i++){
            	 Toode tt = vv.elementAt(i)	;
            	 if(prod.getPartii() != null && prod.getPartii().trim().length() > 0){
            		 prod.setPartii(prod.getPartii() + tt.getLadu() + ":" + tt.getKogus() + " BB:" + tt.getBrand() + "|");	
            	 }else{
            		 prod.setPartii("|" + tt.getLadu() + ":" + tt.getKogus() + " BB: " + tt.getBrand() + "|");	 
            	 }
            	
            	}
            	
            }
         	String name = rs.getString("nimi").trim() + " " + id;
         	name = name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
             double price = rs.getDouble("myygihind");
             if(price != 0 && price != 321.77 && price < 1111){
            	 prod.setPrice(price); 
             }
             
             double price1 = rs.getDouble("hind1");
             if(price1 != 0 && price1 != 321.77 && price1 < 1111 && prod.getPrice() < price1){
            	 prod.setPrice(price1); 
             }
             double price2 = rs.getDouble("hind2");
             if(price2 != 0 && price2 != 321.77 && price2 < 1111 && prod.getPrice() < price2){
            	 prod.setPrice(price2); 
             }
             double price3 = rs.getDouble("hind3");
             if(price3 != 0 && price3 != 321.77 && price3 < 1111 && prod.getPrice() < price3){
            	 prod.setPrice(price3); 
             }
             double price4 = rs.getDouble("hind4");
             if(price4 != 0 && price4 != 321.77 && price4 < 1111 && prod.getPrice() < price4){
            	 prod.setPrice(price4); 
             }
             double price5 = rs.getDouble("hind5");
             if(price5 != 0 && price5 != 321.77 && price5 < 1111 && prod.getPrice() < price5){
            	 prod.setPrice(price5); 
             }
             double price6 = rs.getDouble("hind6");
            
             double price7 = rs.getDouble("hind7");
             if(price7 != 0 && price7 != 321.77 && price7 < 1111 && prod.getPrice() < price7){
            	 prod.setPrice(price7); 
             }
             double price8 = rs.getDouble("hind8");
             if(price8 != 0 && price8 != 321.77 && price8 < 1111 && prod.getPrice() < price8){
            	 prod.setPrice(price8); 
             }
             double price9 = rs.getDouble("hind9");
             if(price9 != 0 && price9 != 321.77 && price9 < 1111 && prod.getPrice() < price9){
            	 prod.setPrice(price9); 
             }
             double price10 = rs.getDouble("hind10");
             if(price10 != 0 && price10 != 321.77 && price10 < 1111 && prod.getPrice() < price10){
            	 prod.setPrice(price10); 
             }
             
            // igaljuhul paneme HORECA hinnakui on olemas tootel
             if(price6 != 0 && price6 != 321.77 && price6 < 1111 ){
            	 prod.setPrice(price6); 
             }
             
             
             if(prod.getPrice() > 0 ){
            	 goes = true;
             }
             
             
             
             if(!ladutr.contains(prod.getID()) && stocks.get(id) == null ){
            	 goes = false;
             }
             
             if(ladub1.get(prod.getID()) != null ){
            	 //System.out.println("b1 " + prod.getID());
            	 prod.setDiffColor(true);
            	 
             }
                
             
             
              String grupp = cleardots(rs.getString("grupp"));
                String tupe = cleardots(rs.getString("tupe"));
                String typenimi =  Converter.cleanLitLatEst(cleardots(rs.getString("tipas")));
                String sortname =  typenimi.substring(0,3) + " " + name;
                prod.setGroup(tupe);
                prod.setGroupSecond(grupp);

        	//	String grupp2 = cleardots(rs.getString("grupp2"));
    			//                prod.setGroupSecond(grupp2);
                String unit = (rs.getString("unit"));
                if (unit != null && unit.trim().length()>0){
                  prod.setUnit(unit.trim());
                }
                
              String ean = (rs.getString("ean"));
                 if (ean != null && ean.trim().length()>0){
                   prod.setEAN(ean.trim());
                 }
                
                 
             	String discou = rs.getString("maxdiscount");
             	if(discou != null && discou.trim().length()>0){
                prod.setMaxDiscount(Double.parseDouble(discou));
                }

                double	cases = rs.getDouble("kastis");
                	if(cases>0){
                   prod.setInCases(cases);
                	}else {
                		 	cases = rs.getDouble("kastis2");
                     	if(cases>0){
                        prod.setInCases(cases);
                     	}
                	}
                	
                

             	weight = rs.getString("kaal");
                           if(weight != null && weight.trim().length()>0){
                             weight = weight.trim();
                             if(weight.indexOf('.')==0){
                             	weight = "0" + weight;
                             }
                               }

          	 String vat = rs.getString("vat");
                   if(vat != null && vat.trim().length()>0){
                     	prod.setVat(vat);
                   }
                  
                   
                   double opt = rs.getDouble("opt");
                   if (opt > 0){
                	   prod.setOptAmountInUnits(opt);
                   }
             String info = rs.getString("info");
                 if(info != null && info.trim().length()>0){ 
                	 if(prod.getPartii() != null && prod.getPartii().trim().length() > 0){
                		 prod.setPartii(prod.getPartii() + "Pirkimai: " +info.trim()); 
                	 }else{
                		 prod.setPartii("Pirkimai: " +info.trim()); 
                	 }
                	if(info.startsWith("1") ) {
                		prod.setDiffColor(true);
                	}
                	if(info.startsWith("2") ) {
                		prod.setDiffColor(true);
                	}
                 }
                 
                 //A on kuu disc 
                 //B kvartaalne
                 //C sellout
                 //N uus toode
                 
                 String stattus = rs.getString("staatus");
                 if(stattus != null && stattus.trim().length()>0){
                	 if(stattus.trim().toLowerCase().indexOf("a") != -1){
                		 prod.setStatusDiscount("1");
                	 }
                	 
                	 if(stattus.trim().toLowerCase().indexOf("n") != -1){
                		 prod.setStatusNew("1");
                	 }
                	 if(stattus.trim().toLowerCase().indexOf("c") != -1){
                		 prod.setStatusSellout("1");
                	 }

                	 if(stattus.trim().toLowerCase().indexOf("b") != -1){
                		 //prod.setDiffColor(true);
                		 prod.setStatusDiscount("2");
                	 }
                  }
                 
                 if(higherCodes.contains(prod.getID())){
                          prod.setStatusTop("3");
                         }
                 
                 String origp = rs.getString("origpak");
                 String klimes = rs.getString("klimes");
                 String laiksal = rs.getString("laiksal");
                 String primpak = rs.getString("primpak");
                  
                 //        Orig.pak.:
                 //        Kilmes salis:
                 //        Pirm.pak.:
                //         Laik.Sal.:

                 if (origp != null && origp.trim().length()>0){
                	 if(prod.getPartii() != null && prod.getPartii().trim().length() > 0){
                		 prod.setPartii(prod.getPartii() +"Orig.pak.:" + origp.trim() + "|");	
                	 }else{
                		 prod.setPartii("|"  +"Orig.pak.:" + origp.trim()  + "|");	 
                	 }
                 }
                 

                 if (laiksal != null && laiksal.trim().length()>0){
                	 if(prod.getPartii() != null && prod.getPartii().trim().length() > 0){
                		 prod.setPartii(prod.getPartii() +"Laik.Sal.:" + laiksal.trim() + "|");	
                	 }else{
                		 prod.setPartii("|"  +"Laik.Sal.:" + laiksal.trim()  + "|");	 
                	 }
                 }    

                 if (klimes != null && klimes.trim().length()>0){
                	 if(prod.getPartii() != null && prod.getPartii().trim().length() > 0){
                		 prod.setPartii(prod.getPartii() +"Kilmes salis:" + klimes.trim() + "|");	
                	 }else{
                		 prod.setPartii("|"  +"Kilmes salis:" + klimes.trim()  + "|");	 
                	 }
                 } 
                 

                 if (primpak != null && primpak.trim().length()>0){
                	 if(prod.getPartii() != null && prod.getPartii().trim().length() > 0){
                		 prod.setPartii(prod.getPartii() +"Pirm.pak.:" + primpak.trim() + "|");	
                	 }else{
                		 prod.setPartii("|"  +"Pirm.pak:" + primpak.trim()  + "|");	 
                	 }
                 } 
                 
                 double oma = rs.getDouble("oma");
                 
                 if(oma > 0){
                	 prod.setMargin(nf.format((100-(oma/prod.getPrice()*100))) + "");
                 }

                 double greend = rs.getDouble("greend");
                 if(greend > 0) {
                	 prod.greendisc = greend;
                 }
                 
                 
                 String brandpers = rs.getString("brandpers");
                 if(brandpers != null && brandpers.trim().length() > 0) {
                	 brandpers = Converter.enCodeEst(brandpers.trim());
                	 if(prod.getPartii() != null && prod.getPartii().trim().length() > 0){
                		 prod.setPartii(prod.getPartii()+ "|Person:" + brandpers);	
                	 }else{
                		 prod.setPartii("Person:" + brandpers);	 
                	 }	 
                 }
                 
                 String otsing = rs.getString("otsing");
                 
                 if(otsing != null && otsing.trim().length() != 0) {
                	 
                	 
                	 StringTokenizer st = new StringTokenizer(otsing,",");
                	 if(otsing.contains(",")) {
                		 while(st.hasMoreTokens()) {
                    		 prod.addBarcode(st.nextToken().trim());
                    	 } 
                	 }
                	
                	 
                  	 if(otsing.contains(";")) {
                  		 st = new StringTokenizer(otsing,";");
                  		 while(st.hasMoreTokens()) {
                    		 prod.addBarcode(st.nextToken().trim());
                    	 }
                  	 }
              
                		 
                 }
                	 
                 
                   if(goes ){
                	   prod.setName(Converter.enCodeEst(name));
                	   prod.setNameSorting(Converter.cleanLitLatEst(name));
                       tm.put(sortname, prod);
                       prodHash.put(prod.getID(), prod);  
                       String normcode = prod.getID().replace('/', '_');
                       if(!docCodes.contains(normcode)){
                    	     docCodes.add(normcode);
                       }
                   }
                }

        rs.close();
        con.close();
     System.out.println("Teen Doc kataloogi ");
     //   Converter.prepareDocCatalogue(inP, outP, docCodes, true);
        
        
     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }

   
   private String getDir(){
	      String dir = getParent().getProps().getProperty("readin");
	      if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	      return dir;
	    }

	public static void saveObject(Object obj, String file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
		oos.writeObject(obj);
		oos.close();
	}
   
   public static String deCode(String res ){

	  	return res;
	  }
	 


   public String getAssortmentStr(Agent agent){
   /* StringBuffer sb = new StringBuffer();
     String cmd = " SELECT [No_] AS toode ,[Type] AS type FROM  [Viï¿½iunai Baltic$Item] " +
     		" WHERE len([Type])>0  ORDER BY [Net Weight] , [Search Description]";
    try {
     	 Connection con = getNewConnection();
     	System.out.println(cmd);
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
        String id = rs.getString("toode");
        String type = rs.getString("type");
			//ABCD
			int i = type.trim().length();
			if(i>0){
			while (i != 0){
				sb.append(" ; ;" + type.substring(i-1,i) + ";" + id + "\n");
				i = i -1;
			 }
			}
	       }

	        rs.close();
	        con.close();
       } catch (Exception e){
      e.printStackTrace();
    }
    return sb.toString();*/
    return null;
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     HashMap kliendid = new HashMap();
     try {
    String cmd =  "  SELECT  b.klient_id AS klient,r.preke AS toode,(kiekis) AS kogus, (Data) AS lastdate, " + 
    		"    		 kaina as price FROM apyv_gr r " + 
    		"             INNER JOIN apyvarta d ON r.apyv_id= d.apyv_id and DATE_ADD(data,interval 300 day) > current_date() " + 
    		"             INNER JOIN klientai b ON b.klientas=d.gavejas " + 
    		"	         where DATE_ADD(data,interval 300 day) > current_date()  and pvm_stat ='S' " + 
    		"			order by klient, toode, lastdate desc";
    System.out.println(cmd);
   //   "klient","toode","kogus","viimane kuupï¿½ev","viimane'%%' kogus"
    Connection con = getNewConnection();
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery(cmd);
    HashSet olemas = new HashSet();
    while(rs.next()){

             String custID = rs.getString("klient").trim();
               String prodID = rs.getString("toode").trim();
                 String lastdate = " ";
               String xx = custID + "_" +prodID;
                 double  amount = rs.getDouble("kogus");
                 lastdate = rs.getString("lastdate").trim().substring(5,10);
                    Customer cust = (Customer)kliendid.get(custID);
                    if (cust == null){
                      cust = new Customer(custID);
                    }
                    
                    double price = rs.getDouble("price");
               
                    Product prod = new Product(prodID);
                    CustProd cp = new CustProd(custID, prodID,amount , 0, 0,lastdate + "_" + price + "€",price,0,0," ");

                    if(!olemas.contains(xx)) {
                    	   cust.addProduct(prod,cp,false);
                           kliendid.put(custID, cust);
                           vec.addElement(cp);	
                    }
              

                 olemas.add(xx);
           }
         rs.close();
         con.close();


           int counter = 0;
           int cou = 0;
          String oldklient = null;
          String olddate = " ";
          String oldtoode = null;

          String cmd2 =  "SELECT  b.klient_id AS klient,r.preke AS toode,r.kiekis AS kogus, data AS lastdate  FROM  apyv_gr r " +
		  " INNER JOIN apyvarta d ON r.apyv_id = d.apyv_id INNER JOIN klientai b ON b.klientas=d.gavejas WHERE  DATE_ADD(data,interval 30 day) > current_date() " +
		  " Order by b.klient_id ,r.preke,d.data desc ";
          System.out.println(cmd2);
		   //   "klient","toode","kogus","viimane kuupï¿½ev","viimane kogus"
		    Connection con2 = getNewConnection();
		    Statement stmt2 = con2.createStatement();
		    ResultSet rs2 = stmt2.executeQuery(cmd2);


  while (rs2.next()){
       String klient = rs2.getString("klient").trim();
               String toode = rs2.getString("toode").trim();
               String kogus = "0";
               String date = " ";
                  kogus = (rs2.getDouble("kogus")+0.99)+"";
                  kogus = kogus.replace('.',',');
				  if(kogus.indexOf(",") != -1){
				  	kogus=kogus.substring(0,kogus.indexOf(","));
				  }
                   date = rs2.getString("lastdate").trim().substring(0,10);

               if(cou == 0){oldklient = klient;olddate = date;oldtoode=toode;}
               if(!oldklient.equals(klient)){
                             counter = 0;
                             oldklient = klient;
                             olddate = date;
               }
               if(!toode.equals(oldtoode)){
               	counter = 0;
               }
               Customer c = (Customer)kliendid.get(klient);
               if (c != null ){

                 ArrayList al = (ArrayList)c.getProducts().get(toode);
                 if (al != null){
                   CustProd cp = (CustProd)al.get(1);
                   if (cp != null){
                	   
                    if(!olddate.equals(date)){counter++;}
                     if (counter == 0){cp.setS1(kogus );}
                     if (counter == 1){cp.setS2(kogus );}
                     if (counter == 2){cp.setS3(kogus );}
                     if (counter == 3){cp.setS4(kogus );}
                     if (counter == 4){cp.setS5(kogus );}

                     vec.add(cp);
                   }
                 }
                 oldklient = klient;
                 olddate = date;
                 oldtoode = toode;
               }
               cou++;
             }


  rs2.close();
  con2.close();

     } catch (Exception e){
       e.printStackTrace();
     }
     return vec;
   }




   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "BuitNowacoLT";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }


   public double getSalary(String agentCode){
     return 0;
   }


   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private TreeMap overdebts = new TreeMap();
   private TreeMap totaldebts = new TreeMap();
   private Vector blockedcusts = new Vector();
   private Vector debtList = new Vector();
   private HashMap sortimendid = null;
   private Vector klient = new Vector();
   private TreeMap agents = new TreeMap();
   private String url = "jdbc:odbc:my_nowaco";
   private Connection con;
   private NumberFormat NF = NumberFormat.getInstance(Locale.ENGLISH);
   private TreeMap debtDays = new TreeMap();
   private String lang = null;

   public NumberFormat getNF() {
	return NF;
}
/**
 * @param nf The nF to set.
 */
public void setNF(NumberFormat nf) {
	NF = nf;
}


  /**
   * setBulkSales
   *
   * @param agents Map
   */


TreeMap planprofit = new TreeMap();

private TreeMap makePlans(){
	   TreeMap res = new TreeMap();
	  
	   try{
		
	   Calendar cal = Calendar.getInstance();
	   int month = (cal.get(Calendar.MONTH)+1);
	   String mon = month+"";
	   if(mon.length() == 1) {
		   mon = "0" + mon;
	   }
	   int yer = cal.get(Calendar.YEAR);
	  
	   String currSheet = yer + "" + mon;
	   
	   File f = new File (getExcelDir() + "Budget.xlsx");
		  FileInputStream	 ff = new FileInputStream(f);
		XSSFWorkbook workbook = new XSSFWorkbook((ff));
		XSSFSheet sheet = workbook.getSheet(currSheet);
	XSSFRow row = null; 
				ff.close();	
				if(sheet != null){
				
					int rows = sheet.getLastRowNum()+1; 
					for(int i = 1 ; i < rows ;i++){
						 row = sheet.getRow(i);
						String agent = null;
						if(row != null) {
							
						
						try {
							agent = (row.getCell((short) 0).getStringCellValue());
					      } catch (Exception e) {
					    	  try {
					    		  if(row.getCell((short) 0) != null)
					    		agent = (int)(row.getCell((short) 0).getNumericCellValue())+ "";
					    	  }catch(Exception j) {
					    		 j.printStackTrace();
					    	  }
					    	
						  
					      }
						
						double plan = 0;
						try {
			    			plan = Double.parseDouble(row.getCell((short) 1).getNumericCellValue()+ "");
						} catch (Exception e) {
	   					 e.printStackTrace();	
	   				  	 plan = Double.parseDouble(row.getCell((short) 1).getStringCellValue());
						}
						

						double profit = 0;
						try {
							profit = Double.parseDouble(row.getCell((short) 2).getNumericCellValue()+ "");
						} catch (Exception e) {
	   					 e.printStackTrace();	
	   				profit = Double.parseDouble(row.getCell((short) 2).getStringCellValue());
						}
						
						//System.out.println(agent + " " + i + " " + plan + " " + profit);
						    res.put(agent,(int)plan);
					
							planprofit.put(agent,(int)profit);
					
						
					
						}
					}}
			   
	   }catch (Exception e){
		   e.printStackTrace();
	   }
		//System.out.println("jooksevplaan " + res.toString());
		//System.out.println("jooksevprofit " + planprofit.toString());
	//	System.exit(0);
	   return res;
}

  public void setBulkSales(Map agents) {

	 TreeMap plans = null ;
	 try {
		 plans = this.makePlans();
	 }catch(Exception e) {
		 e.printStackTrace();
	 }
	
	 int yy = Calendar.getInstance().get(Calendar.YEAR);
	     int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
	     int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
	  
  	 try {
  	 	
  		String cmd2 = "               SELECT d.manager ,sum( kiekis * (kaina -r.savik_3)) as profit FROM apyv_gr r \r\n" + 
  				"                    INNER JOIN apyvarta d ON r.apyv_id= d.apyv_id and DATE_ADD(data,interval 300 day) > current_date()  INNER JOIN klientai b ON b.klientas=d.gavejas  \r\n" + 
  				"                               where month(Data) =month(current_date()) and YEAR(Data) = YEAR(current_date()) \r\n" + 
  				"                               and day(Data) <= day(current_date()) and operacija  not like'%Reklam%' and operacija  not like'%Pirkimas%' and \r\n" + 
  				"                (d.tiekejas is null or d.tiekejas like '%idfood%' ) \r\n" + 
  				"               group  by d.manager ";
  		 
     String cmd = "select sum(viso_1) AS summa,a.manager as agent FROM apyvarta a " +
                          "   where month(Data) =month(current_date()) and YEAR(Data) = YEAR(current_date()) " +
						"	  and day(Data) <= day(current_date())" +
					"		and operacija  not like'%Reklam%' and operacija  not like'%Pirkimas%' and " +
					"		(tiekejas is null or tiekejas like '%idvest%' ) group by a.manager" +
					"		order by gavejas";
  	 	

     TreeMap<String,Integer> profiit = new TreeMap<String,Integer>();
  	        Connection con = getNewConnection();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd2);
	        
	        while(rs.next()){
	        	String mana = rs.getString("manager");
	        	if(mana != null)
	        	profiit.put(mana.trim(), rs.getInt("profit"));
	        }
             
	     // System.out.println("profiit " + profiit.toString()); 
	      
	        rs = stmt.executeQuery(cmd);
	        while(rs.next()){
                double sales = rs.getDouble("summa");
                 String agentID = rs.getString("agent");
                 if(agentID != null){
                	 if(agents.get(agentID) != null){
                 	    Agent agent = (Agent)agents.get(agentID);
                         if (agent != null){
                           agent.setMonthSales((int)(sales));
                           if(profiit.get(agentID) != null) {
                        	   agent.setMonthProfit(profiit.get(agentID) );
                           }
                           String asi = nf.format(((profiit.get(agentID)/sales *100)));
                           agent.setStart( Converter.enCodeEst(asi + "% "));
                          } 
                         
                         
                         
                         
                         //siin on 1ˇ
                   if(true) {      
                         agent.buffer = (int)(sales);

    	            	  double msales = agent.buffer;
    	        		  double psales = 0;
    	        		try{
    	        		  psales = Double.parseDouble(plans.get(agent.getID())+"");
    	        	  }catch(Exception e){
    	        		 // e.printStackTrace();
    	        	  }
    	        	  double  tehtudsalperc = msales/psales;
    	        	  double plaanitavMyykSal = d2 * msales/dd;
    	        	  double  prognosalperc = plaanitavMyykSal/psales;
    	        	  
    	        	  if(tehtudsalperc > 10){
    	        		  tehtudsalperc = 0;  
    	        	  }
    	        	  if(prognosalperc > 10){
    	        		  prognosalperc = 0;  
    	        	  }
    	        	  
    	        	  agent.buffer2 = profiit.get(agent.getID()+"");
    	        	  
    	        	  double msalesP = agent.buffer2;
	        		  double psalesP = 0;
	        		  try{
    	        		  psalesP = Double.parseDouble(this.planprofit.get(agent.getID())+"");
    	        	  }catch(Exception e){
    	        		 // e.printStackTrace();
    	        	  }
    	        	  double  tehtudsalpercP = msalesP/psalesP;
    	        	  double plaanitavMyykSalP = d2 * msalesP/dd;
    	        	  double  prognosalpercP = plaanitavMyykSalP/psalesP;
    	        	  
    	        	  if(tehtudsalpercP > 10){
    	        		  tehtudsalpercP = 0;  
    	        	  }
    	        	  if(prognosalpercP > 10){
    	        		  prognosalpercP = 0;  
    	        	  }
    	        	  
    	        	  
    	        	String asi = "Apyvartos planas: " + (int)psales + " €||"; 
    	          
    	          if(dd > 6){
    	        	  asi = asi + "Apyvartos planas atl./plan. :";
    	              asi = asi + "     " +(int)(tehtudsalperc*100) + "% / " + (int)(prognosalperc*100)  + " %||";  
    	          }
    	          
    	           
    	          asi = asi + "Pelno planas: " + (int)psalesP + " €||"; 
     	          if(dd > 6){
     	        	  asi = asi + "Pelno planas atl./plan. :";
     	              asi = asi + "     " +(int)(tehtudsalpercP*100) + "% / " + (int)(prognosalpercP*100)  + " %||";  
     	          }
     	         
     	           
     	        
    	           
    	           
    	           agent.setStart( Converter.enCodeEst(asi ));
    	           System.out.println(Converter.enCodeEst(agentID + "' " +asi  + " "  + plans.get(agent.getID())) );
                   }
    	                  	
                         
                         
                   } 
                 }
               }
	        

	        rs.close();
	        
	        
	        con.close();
      } catch (Exception e){
          e.printStackTrace();
      }

  }

  

  
  
 private void sendReExReport(){
	  
	  StringBuffer sb = new StringBuffer();
	  String filename = "Order.csv";
	  String tomail = "";
	  String sub = "Order from Bidvest Lithuania";
	 // peame tegema erinevatele saajatele objektid ku sees treemap tootenimi  + vector toode.objektiga 
	  TreeMap saajad = new TreeMap();
	  
	  Calendar cal = Calendar.getInstance();
       
 	   String ddd = Converter.getYYYYpMMpDDpString(cal.getTime());
	  File dir = new File(getImportFile() + "report/");
	    if (dir.isDirectory()){
	      File[] files = dir.listFiles();
	      for (int i = 0; i < files.length ; i++){
	        if (files[i].isFile()){
	    
	    File file = files[i];
	    
	    
	    if(file.getName().indexOf(ddd) != -1){ // nkui on tï¿½nase pï¿½eva objekt
	    
        FileReader ff;
		try {
			ff = new FileReader(file);
		
		File hj = new File(filename);
			if(hj.exists()){
				hj.delete();
			}
		BufferedReader br = new BufferedReader(ff);
		String line = null;
		while( (line = br.readLine()) != null){
            String saaja = "";
            String klient = "";
            String toode = "";
            double kogus = 0;
            String yhik = "";
            String nimi = "";
			
            StringTokenizer st = new StringTokenizer(line, ",");
			if (st.hasMoreTokens()){
				klient = st.nextToken();	
		     }

			if (st.hasMoreTokens()){
				toode = st.nextToken();	
		     }

			if (st.hasMoreTokens()){
				saaja = st.nextToken();	
		     }

			if (st.hasMoreTokens()){
				kogus = Double.parseDouble(st.nextToken());	
		     }
			
			if (st.hasMoreTokens()){
				yhik = st.nextToken();	
		     }
			
			if (st.hasMoreTokens()){
				nimi = st.nextToken();	
		     }

 //  System.out.println(nimi + " "+ yhik+ " "+kogus+ " "+saaja+ " "+toode+ " "+klient);
			
			
			if(saajad.get(saaja) != null){
				TreeMap prods = (TreeMap)saajad.get(saaja);
				if(prods.get(nimi) != null){
				 Vector vv = (Vector)prods.get(nimi); 

					Product t2 = new Product(toode);
					t2.setName(nimi);
					t2.setUnit(yhik);
					t2.setStock(kogus);
					t2.setAgentGroup(klient);
					 vv.add(t2);
					      	prods.put(nimi, vv);
					      	saajad.put(saaja, prods);
				}else{
					Product t2 = new Product(toode);
					t2.setName(nimi);
					t2.setUnit(yhik);
					t2.setStock(kogus);
					t2.setAgentGroup(klient);
					Vector pr = new Vector();
					 pr.add(t2);
					      	prods.put(nimi, pr);
					      	saajad.put(saaja, prods);
							
				}
				
			}else{
				Product t2 = new Product(toode);
				t2.setName(nimi);
				t2.setUnit(yhik);
				t2.setStock(kogus);
				t2.setAgentGroup(klient);
				TreeMap prods = new TreeMap();
				Vector pr = new Vector();
				 pr.add(t2);
				      	prods.put(nimi, pr);
				      	saajad.put(saaja, prods);
			}
  	           
	        }
	    br.close();
	    ff.close();
		} catch (Exception e) {
				e.printStackTrace();
		}
		
	        }
		
		    }
	      }
	    
	    }
	    
	  
	    // nï¿½ï¿½d on vaja fail kokku panna ja summeerides koguseid klienid toote pï¿½hiselt
	    
	 Iterator itera =   saajad.keySet().iterator();
	 while (itera.hasNext()){
		 String saaja = itera.next() + "";
		 TreeMap prods = (TreeMap)saajad.get(saaja);
	     TreeMap vahe = new TreeMap();
		 
	     // liidame kokku samatoote kogused sama kliendi juures 
	     // 
	     
		 Iterator iterap =   prods.keySet().iterator();
		 Vector<String> kliendid = new Vector<String>();
		 while (iterap.hasNext()){
			 String nimi = iterap.next() + "";
			 Vector prs = (Vector)prods.get(nimi);
			 
			 for(int i =0; i < prs.size();i++){
				 Product pp = (Product)prs.elementAt(i);
				 String klient = pp.getAgentGroup();
				 if(!kliendid.contains(klient)){
					kliendid.add(klient); 
				 }
				 if(vahe.get(pp.getID() + " " + pp.getName()) != null){
					 TreeMap klkogus = (TreeMap)vahe.get(pp.getID() + " " + pp.getName()) ;
					 if(klkogus.get(klient) != null){
                       double dd = Double.parseDouble(klkogus.get(klient) + "");
						 klkogus.put(klient, (pp.getStock() + dd) + "");
					 }else{
						 klkogus.put(klient, pp.getStock() + "");
					 }
					 vahe.put(pp.getID() + " " + pp.getName(), klkogus );
					// System.out.println(nimi + " zzz " + klient + " " + pp.getID());
						
				 }else{
					 
					 //System.out.println(nimi + " yyy " + klient + " " + pp.getID());
						
					 TreeMap klkogus = new TreeMap();
					 klkogus.put(klient, pp.getStock() + "");
					 vahe.put(pp.getID() + " " + pp.getName(), klkogus );
					 	 
				 }
				 }		 
		 }
			 
		 
		 String head = " ;" ;
		 for(String nn : kliendid){
			 head = head + nn + " ;";
		 }
		 
		 
		// System.out.println("head " + head);
		 Iterator iteras =   vahe.keySet().iterator();
 		 while (iteras.hasNext()){
 			String nimi = iteras.next() + "";
 			TreeMap kls = (TreeMap)vahe.get(nimi);
 			 sb.append(  nimi  + ";" );
 			// System.out.println("kls " + kls.toString() + " \n" );
 	 		//Kï¿½ime kï¿½ik kliendid lï¿½bi ja paneme sii styhiku kui pole kogust talle 
 			 for(String nn : kliendid){
 	 	 		// String klie = (String)iterad.next();
 				//	 System.out.println("nn " + nn + " " + kls.get(nn)  );
 					 if(kls.get(nn) != null){
 						 sb.append((kls.get(nn)+"").replace('.', ',') + ";");
 					 }else{
 						 sb.append(" ;");
 					 }
 			 }
 			 sb.append(  "\n");
 		  }
		
		tomail = parent.getProps().getProperty(saaja);
		Calendar ccc = Calendar.getInstance();
		if(ccc.get(Calendar.HOUR_OF_DAY) > 13){

		  if(sb.length() > 0 && tomail != null){
			  saveFile(filename,"sep=;\n" + head + "\n" + sb.toString());
			  sendEmail(head + "\n" + sb.toString(),sub,tomail,filename,15,false); 
		  } 
		  }
		 	 }
	    
	  
	  
		Calendar ccc = Calendar.getInstance();
		if(ccc.get(Calendar.HOUR_OF_DAY) > 13){
		
	  
	  //renameme nyyd kï¿½ik failid
	   dir = new File(getImportFile() + "report/");
	   
	   if (dir.isDirectory()){
	      File[] files = dir.listFiles();
	       for (int i = 0; i < files.length ; i++){
	         if (files[i].isFile()){
	            File file = files[i];
	            if(file.getName().indexOf(ddd) != -1){ // kui on tï¿½nase pï¿½eva fail 
	            File olemas = new File(dir + "/sent/" + file.getName());
	        try {
	           	copyFile(file,olemas);
					file.delete();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            }	
	          }
	        }   
	     }
	   }	  
     }
  

 private void sendReExReportG(){
	  
	  StringBuffer sb = new StringBuffer();
	  String filename = "Order.csv";
	  String tomail = "";
	  String sub = "Order from Bidvest Lithuania";
	 // peame tegema erinevatele saajatele objektid ku sees treemap tootenimi  + vector toode.objektiga 
	  
	  TreeMap saajad = new TreeMap();
	  Calendar cal = Calendar.getInstance();
      String ddd = Converter.getYYYYpMMpDDpString(cal.getTime());
	  
     // System.out.println("HOUR " + cal.get(Calendar.HOUR_OF_DAY) + " " + ddd);
	  
		Calendar ccc = Calendar.getInstance();
		if(ccc.get(Calendar.HOUR_OF_DAY) > 14 || ccc.get(Calendar.HOUR_OF_DAY) < 2){

      
      
	  File dir = new File(getImportFile() + "reportG/");
	    if (dir.isDirectory()){
	      File[] files = dir.listFiles();
	      for (int i = 0; i < files.length ; i++){
	        if (files[i].isFile()){
	    
	    File file = files[i];
	    
	    
	   // if(file.getName().indexOf(ddd) != -1){ // nkui on tï¿½nase pï¿½eva objekt
	    	if(true){
	    	 System.out.println("HOUR " + cal.get(Calendar.HOUR_OF_DAY) + " " + ddd + " " + file.getName());
       FileReader ff;
		try {
			ff = new FileReader(file);
		
		File hj = new File(filename);
			if(hj.exists()){
				hj.delete();
			}
		BufferedReader br = new BufferedReader(ff);
		String line = null;
		while( (line = br.readLine()) != null){
           String saaja = "";
           String klient = "";
           String toode = "";
           double kogus = 0;
           String yhik = "";
           String nimi = "";
			
           StringTokenizer st = new StringTokenizer(line, ",");
			if (st.hasMoreTokens()){
				klient = st.nextToken();	
		     }

			if (st.hasMoreTokens()){
				toode = st.nextToken();	
		     }

			if (st.hasMoreTokens()){
				saaja = st.nextToken();	
		     }

			if (st.hasMoreTokens()){
				kogus = Double.parseDouble(st.nextToken());	
		     }
			
			if (st.hasMoreTokens()){
				yhik = st.nextToken();	
		     }
			
			if (st.hasMoreTokens()){
				nimi = st.nextToken();	
		     }

 // System.out.println(nimi + " "+ yhik+ " "+kogus+ " "+saaja+ " "+toode+ " "+klient);
			
			
			if(saajad.get(saaja) != null){
				TreeMap prods = (TreeMap)saajad.get(saaja);
				if(prods.get(nimi) != null){
				 Vector vv = (Vector)prods.get(nimi); 

					Product t2 = new Product(toode);
					t2.setName(nimi);
					t2.setUnit(yhik);
					t2.setStock(kogus);
					t2.setAgentGroup(klient);
					 vv.add(t2);
					      	prods.put(nimi, vv);
					      	saajad.put(saaja, prods);
				}else{
					Product t2 = new Product(toode);
					t2.setName(nimi);
					t2.setUnit(yhik);
					t2.setStock(kogus);
					t2.setAgentGroup(klient);
					Vector pr = new Vector();
					 pr.add(t2);
					      	prods.put(nimi, pr);
					      	saajad.put(saaja, prods);
							
				}
				
			}else{
				Product t2 = new Product(toode);
				t2.setName(nimi);
				t2.setUnit(yhik);
				t2.setStock(kogus);
				t2.setAgentGroup(klient);
				TreeMap prods = new TreeMap();
				Vector pr = new Vector();
				 pr.add(t2);
				      	prods.put(nimi, pr);
				      	saajad.put(saaja, prods);
			}
 	           
	        }
	    br.close();
	    ff.close();
		} catch (Exception e) {
				e.printStackTrace();
		}
		
	        }
		
		    }
	      }
	    
	    }
	    
	   // System.out.println("aaaass " + saajad.size());
	    // nï¿½ï¿½d on vaja fail kokku panna ja summeerides koguseid klienid toote pï¿½hiselt
	    
	    
	    TreeMap kogused = new TreeMap();
	 Iterator itera =   saajad.keySet().iterator();
	 while (itera.hasNext()){
		 String saaja = itera.next() + "";
	//	 System.out.println("aaaa " + saaja);
		 TreeMap prods = (TreeMap)saajad.get(saaja);
	     TreeMap vahe = new TreeMap();
		 
	     // liidame kokku samatoote kogused sama kliendi juures 
	     // 
	     
		 Iterator iterap =   prods.keySet().iterator();
		 Vector<String> kliendid = new Vector<String>();
		 while (iterap.hasNext()){
			 String nimi = iterap.next() + "";
			 Vector prs = (Vector)prods.get(nimi);
			 
			 for(int i =0; i < prs.size();i++){
				 Product pp = (Product)prs.elementAt(i);
				 
				 
				
				 String klient = pp.getAgentGroup();
				 if(!kliendid.contains(klient)){
					kliendid.add(klient); 
				 }
				 
				 if(kogused.get(klient) != null){
					 double dd = Double.parseDouble(kogused.get(klient) + "");
					 kogused.put(klient , (dd + pp.getStock()) + "");
				 }else{
					 kogused.put(klient ,  pp.getStock() );
				 }
				 
				 if(vahe.get(pp.getID() + " " + pp.getName()) != null){
					 TreeMap klkogus = (TreeMap)vahe.get(pp.getID() + " " + pp.getName()) ;
					 if(klkogus.get(klient) != null){
                      double dd = Double.parseDouble(klkogus.get(klient) + "");
						 klkogus.put(klient, (pp.getStock() + dd) + "");
					 }else{
						 klkogus.put(klient, pp.getStock() + "");
					 }
					 vahe.put(pp.getID() + " " + pp.getName(), klkogus );
					// System.out.println(nimi + " zzz " + klient + " " + pp.getID() + " " + saaja);
						
				 }else{
					 
					 //System.out.println(nimi + " yyy " + klient + " " + pp.getID());
						
					 TreeMap klkogus = new TreeMap();
					 klkogus.put(klient, pp.getStock() + "");
					 vahe.put(pp.getID() + " " + pp.getName(), klkogus );
					 	 
				 }
				 }		 
		 }
			 
		 
		 String head = " ;" ;
		 String tald = "Viso: ;" ;
		 for(String nn : kliendid){
			 head = head + nn + " ;";
			 if(kogused.get(nn) != null){
				 tald = tald + kogused.get(nn) + " ;";
			 }
			
		 }
		 
		
		 
		 
		// System.out.println("head " + head);
		 Iterator iteras =   vahe.keySet().iterator();
		 while (iteras.hasNext()){
			String nimi = iteras.next() + "";
			TreeMap kls = (TreeMap)vahe.get(nimi);
			 sb.append(  nimi  + ";" );
			// System.out.println("kls " + kls.toString() + " \n" );
	 		//Kï¿½ime kï¿½ik kliendid lï¿½bi ja paneme sii styhiku kui pole kogust talle 
			 for(String nn : kliendid){
	 	 		// String klie = (String)iterad.next();
				//	 System.out.println("nn " + nn + " " + kls.get(nn)  );
					 if(kls.get(nn) != null){
						 sb.append((kls.get(nn)+"").replace('.', ',') + ";");
						
					 }else{
						 sb.append(" ;");
						
					 }
			 }
			 sb.append(  "\n");
		  }
		
		tomail = parent.getProps().getProperty(saaja);

		  if(sb.length() > 0 && tomail != null){
			  saveFile(filename,"sep=;\n" + head + "\n" + sb.toString()+ tald);
			  sendEmail(head + "\n" + sb.toString() + tald,sub,tomail,filename,100,false); 
		  } 
		  
		 	 }
	    
	  
	  
		
	  //renameme nyyd kï¿½ik failid
	   dir = new File(getImportFile() + "reportG/");
	   
	   if (dir.isDirectory()){
	      File[] files = dir.listFiles();
	       for (int i = 0; i < files.length ; i++){
	         if (files[i].isFile()){
	            File file = files[i];
	         //   if(file.getName().indexOf(ddd) != -1){ // kui on tï¿½nase pï¿½eva fail 
	            File olemas = new File(dir + "/sent/" + file.getName());
	        try {
	           	copyFile(file,olemas);
					file.delete();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	         //}	
	          }
	        }   
	     }
	   }	  
    }
 

 
 
 private void sendReExReportB(){
	  
	  StringBuffer sb = new StringBuffer();
	  String filename = "Order.csv";
	  String tomail = "";
	  String sub = "Order from Bidvest Lithuania";
	  TreeMap prods = new TreeMap();
	  Calendar cal = Calendar.getInstance();
	   String ddd = Converter.getYYYYpMMpDDpString(cal.getTime());
	  File dir = new File(getImportFile() + "reportB/");
	    if (dir.isDirectory()){
	      File[] files = dir.listFiles();
	      for (int i = 0; i < files.length ; i++){
	        if (files[i].isFile()){
	    File file = files[i];
	   // System.out.println("sees " + file.getName() + " " + ddd);
	    if(file.getName().indexOf(ddd) != -1){ // nkui on tï¿½nase pï¿½eva objekt
      FileReader ff;
		try {
			ff = new FileReader(file);
		File hj = new File(filename);
			if(hj.exists()){
				hj.delete();
			}
		BufferedReader br = new BufferedReader(ff);
		String line = null;
		while( (line = br.readLine()) != null){
          String saaja = "";
          String toode = "";
          double kogus = 0;
          String yhik = "";
          String nimi = "";
			
          StringTokenizer st = new StringTokenizer(line, ",");
			if (st.hasMoreTokens()){
				toode = st.nextToken();	
		     }

			if (st.hasMoreTokens()){
				saaja = st.nextToken();	// see siis see täht
		     }

			if (st.hasMoreTokens()){
				kogus = Double.parseDouble(st.nextToken());	
		     }
			
			if (st.hasMoreTokens()){
				yhik = st.nextToken();	
		     }
			
			if (st.hasMoreTokens()){
				nimi = st.nextToken();	
		     }

				if(prods.get(toode) != null){
					Toode tt = (Toode)prods.get(toode);
					tt.setKogus(tt.getKogus() + kogus);
					prods.put(toode, tt);
				}else{
					Toode tt = new Toode(toode);
					tt.setBrand(nimi);
					tt.setKogus( kogus);
					prods.put(toode, tt);
				}
			   
	        }
	    br.close();
	    ff.close();
		} catch (Exception e) {
				e.printStackTrace();
		}
		
	        }
		
		    }
	      }
	    
	    }
	    
	  
	    // nï¿½ï¿½d on vaja fail kokku panna ja summeerides koguseid klienid toote pï¿½hiselt
	    
	 Iterator itera =   prods.keySet().iterator();
	 String head = "Order;Code;Name";
	 
	 while (itera.hasNext()){
	
		 String kood = itera.next() + "";
		 Toode tt = (Toode)prods.get(kood);
		     sb.append( tt.getKogus() + ";" +  kood  + ";" + tt.getBrand() );
			 sb.append(  "\n");
		  }
		
		  tomail = parent.getProps().getProperty("B");

		  
			Calendar ccc = Calendar.getInstance();
			boolean weekend = false;
			if(ccc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || ccc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
				weekend = true;
			}
			
			if(ccc.get(Calendar.HOUR_OF_DAY) > 11){
			

		  if(sb.length() > 0 && tomail != null){
			  saveFile(filename,"sep=;\n" + head + "\n" + sb.toString());
			  sendEmail(head + "\n" + sb.toString(),sub,tomail,filename,13,false); 
		  }
		 
	  
	  
	  //renameme nyyd kï¿½ik failid
	   dir = new File(getImportFile() + "reportB/");
	    
	   
	   
	   if (dir.isDirectory()){
	      File[] files = dir.listFiles();
	       for (int i = 0; i < files.length ; i++){
	         if (files[i].isFile()){
	        	 
	            File file = files[i];
	            if(file.getName().indexOf(ddd) != -1){ // kui on tï¿½nase pï¿½eva fail 
	        	 
	            File olemas = new File(dir + "/sent/" + file.getName());
	        try {
	           	copyFile(file,olemas);
					file.delete();
				} catch (Exception e) {
					e.printStackTrace();
			     	}
	               }	
	             }
	         }
	     }		
			}  
   }
 
 
 private void sendReExReportR(){
	  
	  StringBuffer sb = new StringBuffer();
	  String filename = "Order.csv";
	  String tomail = "";
	  String sub = "Order from Bidvest Lithuania";
	  TreeMap prods = new TreeMap();
	  Calendar cal = Calendar.getInstance();
	   String ddd = Converter.getYYYYpMMpDDpString(cal.getTime());
	  File dir = new File(getImportFile() + "reportR/");
	    if (dir.isDirectory()){
	      File[] files = dir.listFiles();
	      for (int i = 0; i < files.length ; i++){
	        if (files[i].isFile()){
	    File file = files[i];
	   // System.out.println("sees " + file.getName() + " " + ddd);
	    if(file.getName().indexOf(ddd) != -1){ // nkui on tï¿½nase pï¿½eva objekt
      FileReader ff;
		try {
			ff = new FileReader(file);
		File hj = new File(filename);
			if(hj.exists()){
				hj.delete();
			}
		BufferedReader br = new BufferedReader(ff);
		String line = null;
		while( (line = br.readLine()) != null){
          String saaja = "";
          String toode = "";
          double kogus = 0;
          String yhik = "";
          String nimi = "";
			
          StringTokenizer st = new StringTokenizer(line, ",");
			if (st.hasMoreTokens()){
				toode = st.nextToken();	
		     }

			if (st.hasMoreTokens()){
				saaja = st.nextToken();	// see siis see täht
		     }

			if (st.hasMoreTokens()){
				kogus = Double.parseDouble(st.nextToken());	
		     }
			
			if (st.hasMoreTokens()){
				yhik = st.nextToken();	
		     }
			
			if (st.hasMoreTokens()){
				nimi = st.nextToken();	
		     }

				if(prods.get(toode) != null){
					Toode tt = (Toode)prods.get(toode);
					tt.setKogus(tt.getKogus() + kogus);
					prods.put(toode, tt);
				}else{
					Toode tt = new Toode(toode);
					tt.setBrand(nimi);
					tt.setKogus( kogus);
					prods.put(toode, tt);
				}
			   
	        }
	    br.close();
	    ff.close();
		} catch (Exception e) {
				e.printStackTrace();
		}
		
	        }
		
		    }
	      }
	    
	    }
	    
	  
	    // nï¿½ï¿½d on vaja fail kokku panna ja summeerides koguseid klienid toote pï¿½hiselt
	    
	 Iterator itera =   prods.keySet().iterator();
	 String head = "Order;Code;Name";
	 
	 while (itera.hasNext()){
	
		 String kood = itera.next() + "";
		 Toode tt = (Toode)prods.get(kood);
		     sb.append( tt.getKogus() + ";" +  kood  + ";" + tt.getBrand() );
			 sb.append(  "\n");
		  }
		
		  tomail = parent.getProps().getProperty("R");
			Calendar ccc = Calendar.getInstance();
			
			boolean weekend = false;
			if(ccc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || ccc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
				weekend = true;
			}
			
			if(ccc.get(Calendar.HOUR_OF_DAY) > 9){
		  if(sb.length() > 0 && tomail != null){
			  saveFile(filename,"sep=;\n" + head + "\n" + sb.toString());
			  sendEmail(head + "\n" + sb.toString(),sub,tomail,filename,11,false); 
		  }
		//renameme nyyd kï¿½ik failid
	   dir = new File(getImportFile() + "reportR/");
	   
	   if (dir.isDirectory()){
	      File[] files = dir.listFiles();
	       for (int i = 0; i < files.length ; i++){
	         if (files[i].isFile()){
	            File file = files[i];
	            if(file.getName().indexOf(ddd) != -1){ // kui on tï¿½nase pï¿½eva fail 
	            File olemas = new File(dir + "/sent/" + file.getName());
	        try {
	           	copyFile(file,olemas);
					file.delete();
				} catch (Exception e) {
					e.printStackTrace();
			     	}
	               }	
	             }
	         }
	   	
		}     }	  
   }
 
 
 
 private void sendReExReportK(){
	  
	  StringBuffer sb = new StringBuffer();
	  String filename = "Order.csv";
	  String tomail = "";
	  String sub = "Order from Bidvest Lithuania";
	  TreeMap prods = new TreeMap();
	  Calendar cal = Calendar.getInstance();
	   String ddd = Converter.getYYYYpMMpDDpString(cal.getTime());
	  File dir = new File(getImportFile() + "reportK/");
	    if (dir.isDirectory()){
	      File[] files = dir.listFiles();
	      for (int i = 0; i < files.length ; i++){
	        if (files[i].isFile()){
	    File file = files[i];
	    if(file.getName().indexOf(ddd) != -1){ // nkui on tï¿½nase pï¿½eva objekt
      FileReader ff;
		try {
			ff = new FileReader(file);
		File hj = new File(filename);
			if(hj.exists()){
				hj.delete();
			}
		BufferedReader br = new BufferedReader(ff);
		String line = null;
		while( (line = br.readLine()) != null){
          String saaja = "";
          String toode = "";
          double kogus = 0;
          String yhik = "";
          String nimi = "";
			
          StringTokenizer st = new StringTokenizer(line, ",");
			if (st.hasMoreTokens()){
				toode = st.nextToken();	
		     }

			if (st.hasMoreTokens()){
				saaja = st.nextToken();	// see siis see täht
		     }

			if (st.hasMoreTokens()){
				kogus = Double.parseDouble(st.nextToken());	
		     }
			
			if (st.hasMoreTokens()){
				yhik = st.nextToken();	
		     }
			
			if (st.hasMoreTokens()){
				nimi = st.nextToken();	
		     }

				if(prods.get(toode) != null){
					Toode tt = (Toode)prods.get(toode);
					tt.setKogus(tt.getKogus() + kogus);
					prods.put(toode, tt);
				}else{
					Toode tt = new Toode(toode);
					tt.setBrand(nimi);
					tt.setKogus( kogus);
					prods.put(toode, tt);
				}
			   
	        }
	    br.close();
	    ff.close();
		} catch (Exception e) {
				e.printStackTrace();
		}
		
	        }
		
		    }
	      }
	    
	    }
	    
	  
	    // nï¿½ï¿½d on vaja fail kokku panna ja summeerides koguseid klienid toote pï¿½hiselt
	    
	 Iterator itera =   prods.keySet().iterator();
	 String head = "Order;Code;Name";
	 
	 while (itera.hasNext()){
	
		 String kood = itera.next() + "";
		 Toode tt = (Toode)prods.get(kood);
		     sb.append( tt.getKogus() + ";" +  kood  + ";" + tt.getBrand() );
			 sb.append(  "\n");
		  }
		
		  tomail = parent.getProps().getProperty("K");

			Calendar ccc = Calendar.getInstance();
			
			boolean weekend = false;
			if(ccc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || ccc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
				weekend = true;
			}
			if(ccc.get(Calendar.HOUR_OF_DAY) > 11){// saadame siis 13.56 sheduleriga aga soov oli varemaks 
		
		  
		  if(sb.length() > 0 && tomail != null){
			  saveFile(filename,"sep=;\n" + head + "\n" + sb.toString());
			  sendEmail(head + "\n" + sb.toString(),sub,tomail,filename,13 , false); 
		  }
		 
	  
	  
	  //renameme nyyd kï¿½ik failid
	   dir = new File(getImportFile() + "reportK/");
	    
	   
	   
	   if (dir.isDirectory()){
	      File[] files = dir.listFiles();
	       for (int i = 0; i < files.length ; i++){
	         if (files[i].isFile()){
	        	 
	            File file = files[i];
	            if(file.getName().indexOf(ddd) != -1){ // kui on tï¿½nase pï¿½eva fail 
	        	 
	            File olemas = new File(dir + "/sent/" + file.getName());
	        try {
	           	copyFile(file,olemas);
					file.delete();
				} catch (Exception e) {
					e.printStackTrace();
			     	}
	               }	
	             }
	         }
       }
		     }	  
   }



 private void sendReExReport2(){
	  
	  StringBuffer sb = new StringBuffer();
	  String filename = "Order.csv";
	  String tomail = "";
	  String sub = "Order from Bidvest Lithuania";
	  TreeMap prods = new TreeMap();
	  Calendar cal = Calendar.getInstance();
	   String ddd = Converter.getYYYYpMMpDDpString(cal.getTime());
	  File dir = new File(getImportFile() + "report2/");
	    if (dir.isDirectory()){
	      File[] files = dir.listFiles();
	      for (int i = 0; i < files.length ; i++){
	        if (files[i].isFile()){
	    File file = files[i];
	   // System.out.println("sees " + file.getName() + " " + ddd);
	    if(file.getName().indexOf(ddd) != -1){ // nkui on tï¿½nase pï¿½eva objekt
       FileReader ff;
		try {
			ff = new FileReader(file);
		File hj = new File(filename);
			if(hj.exists()){
				hj.delete();
			}
		BufferedReader br = new BufferedReader(ff);
		String line = null;
		while( (line = br.readLine()) != null){
           String saaja = "";
           String toode = "";
           double kogus = 0;
           String yhik = "";
           String nimi = "";
			
           StringTokenizer st = new StringTokenizer(line, ",");
			if (st.hasMoreTokens()){
				toode = st.nextToken();	
		     }

			if (st.hasMoreTokens()){
				saaja = st.nextToken();	// see siis see täht
		     }

			if (st.hasMoreTokens()){
				kogus = Double.parseDouble(st.nextToken());	
		     }
			
			if (st.hasMoreTokens()){
				yhik = st.nextToken();	
		     }
			
			if (st.hasMoreTokens()){
				nimi = st.nextToken();	
		     }

				if(prods.get(toode) != null){
					Toode tt = (Toode)prods.get(toode);
					tt.setKogus(tt.getKogus() + kogus);
					prods.put(toode, tt);
				}else{
					Toode tt = new Toode(toode);
					tt.setBrand(nimi);
					tt.setKogus( kogus);
					prods.put(toode, tt);
				}
			   
	        }
	    br.close();
	    ff.close();
		} catch (Exception e) {
				e.printStackTrace();
		}
		
	        }
		
		    }
	      }
	    
	    }
	    
	  
	    // nï¿½ï¿½d on vaja fail kokku panna ja summeerides koguseid klienid toote pï¿½hiselt
	    
	 Iterator itera =   prods.keySet().iterator();
	 String head = "Order;Code;Name";
	 
	 while (itera.hasNext()){
	
		 String kood = itera.next() + "";
		 Toode tt = (Toode)prods.get(kood);
		     sb.append( tt.getKogus() + ";" +  kood  + ";" + tt.getBrand() );
			 sb.append(  "\n");
		  }
		
		  tomail = parent.getProps().getProperty("D");
Calendar ccc = Calendar.getInstance();
if(ccc.get(Calendar.HOUR_OF_DAY) > 13){

		  if(sb.length() > 0 && tomail != null){
			  saveFile(filename,"sep=;\n" + head + "\n" + sb.toString());
			  sendEmail(head + "\n" + sb.toString(),sub,tomail,filename,15,false); 
		  }
		
	  //renameme nyyd kï¿½ik failid
	   dir = new File(getImportFile() + "report2/");
	   
	   if (dir.isDirectory()){
	      File[] files = dir.listFiles();
	       for (int i = 0; i < files.length ; i++){
	         if (files[i].isFile()){
	        	 
	            File file = files[i];
	            if(file.getName().indexOf(ddd) != -1){ // kui on tï¿½nase pï¿½eva fail 
	        	 
	            File olemas = new File(dir + "/sent/" + file.getName());
	        try {
	           	copyFile(file,olemas);
					file.delete();
				} catch (Exception e) {
					e.printStackTrace();
			     	}
	               }	
	             }
	         }
	     }	
	   }else{
		   System.out.println("Vara veel kell 15.00 alates saadame seda");
	   }
    }
 
 


  
  private void sendEmail(String body,String subjekt,String to , String filename, int hour , boolean weekend){
		Email mail = new Email(null);
		mail.setSMTP(parent.getProps().getProperty("smtphost"));
		mail.setFrom(parent.getProps().getProperty("from"));
		  mail.setUser(parent.getProps().getProperty("smtpuser"));
          mail.setPass(parent.getProps().getProperty("smtppass"));
          mail.setPort(parent.getProps().getProperty("smtpport"));
        
		mail.setSubjectFilesTo(subjekt);
		mail.setEncoding("UTF-8");
		
		//mail.addAttach(f.getAbsolutePath());
		mail.setSendNotice(false);
		try {
			mail.setBody(body);
			if(filename != null){
				mail.addAttach(filename);
			}
		
		} catch (MessagingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Calendar cc = Calendar.getInstance();
		if(hour != 100 && cc.get(Calendar.HOUR_OF_DAY) > hour){
			to = "products@bidvest.lt";
		}
		
		if(weekend){
			to = "products@bidvest.lt,order@bidvest.lt";
		}
		
		StringTokenizer st = new StringTokenizer(to,",");
		while(st.hasMoreTokens()){
			String mai = st.nextToken();
			mail.setFilesTo(mai);
			try {
				mail.send();
				System.out.println("Saadan " + to);
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		  }	
        }
  
  

  
  
@Override
public void PictureMaker() {
	System.out.println("esimene");
	//kella kontrollid tuleb vahele panna 
	  sendReExReport();
		System.out.println("teine");
	  sendReExReport2();
		System.out.println("kolmas");
		 sendReExReportK();
			System.out.println("neljas");
			 sendReExReportB();
				System.out.println("viies");
				 sendReExReportR();
					System.out.println("kuues");
					 sendReExReportG();
		  System.exit(0);
}




@Override
public void PictureMaker2() {
	 makePictureCat();
	 System.exit(0);
}
}
