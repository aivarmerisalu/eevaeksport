package eevaeksport.imp.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONObject;

import eevaeksport.EksportFrame;
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

public class HorizonKonigImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public HorizonKonigImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
   if(parent.getProps().getProperty("wsdl.url") != null){
	   weburl = parent.getProps().getProperty("wsdl.url");
   }
    try {
      Class.forName(databaseDriverName).newInstance();
    } catch (Exception e){
       parent.showError(e);
    }
    
      loadSignatures();
  }

  
  


  private Calendar  getNextShipCal(String cust) throws SQLException{
	  	Calendar res = null;
	 		//	Saame kõik 2 nädala päevad kätte 
	  	
	  	
	  	
	  	String cmd = "SELECT K.NOSAUK AS nimi, K.PK_KLIENTS AS id , a.KONTAKTP  as tarne \r\n" + 
	  			"	  		   FROM   APV04761.DKLIENTS K LEFT OUTER JOIN  APV04761.NADRESES a ON A.PK_KLIENTS=K.PK_KLIENTS and a.LEG_ADR = 2 " + 
	  			"	  		     WHERE  K.PK_KLIENTS='" + cust + "' "  ;
	  		     
	  		      
	  		       
	  		    	
	  		    	
	  		    	Connection con = this.getNewConnection();
	  		    	Statement stmt =con.createStatement();
	  		      ResultSet rs = stmt.executeQuery(cmd);
	  		      String days = null;
	  		      if(rs.next()){
	  		    	
		  		        days= rs.getString("tarne");
		  		        if(days != null && days.trim().length() > 0) {
		  		        	days = days.trim();
		  		        }else {
		  		        	days = null;
		  		        }
	  		      }
	  		      rs.close();
	  		      stmt.close();
	  		      con.close();
	  		      
	  	
	  		      
	  		      if(days != null) {
	  		    	  
	  		     
	  	
				LocalDate now = new LocalDate();
			
				LocalDate mon = now.withDayOfWeek(DateTimeConstants.MONDAY);
				LocalDate tue = mon.plusDays(1);
				LocalDate wed = mon.plusDays(2);
				LocalDate thu = mon.plusDays(3);
				LocalDate fri = mon.plusDays(4);
				LocalDate sat = mon.plusDays(5);
				LocalDate sun = mon.plusDays(6);
				LocalDate mon1 = mon.plusDays(7);
				LocalDate tue1 = mon.plusDays(8);
				LocalDate wed1 = mon.plusDays(9);
				LocalDate thu1 = mon.plusDays(10);
				LocalDate fri1 = mon.plusDays(11);
				LocalDate sat1 = mon.plusDays(12);
				LocalDate sun1 = mon.plusDays(13);
				
			LocalDate kaes = new LocalDate();
			ArrayList <LocalDate> aaa = new ArrayList<LocalDate>();
				 
				 StringTokenizer st = new StringTokenizer(days,",");  
				 while(st.hasMoreTokens()) {
					 
					int day = Integer.parseInt(st.nextToken());
				//   System.out.println(day + " uuu " + paev);
				   
				   if(day == 1) {
					   aaa.add(mon1);
					   aaa.add(mon);
					   
				   }
				   if(day == 2) {
					   aaa.add(tue1);
					   aaa.add(tue);
				   }
				   if(day == 3) {
					   aaa.add(wed1);
					   aaa.add(wed);
				   }						
				   if(day == 4) {
					   aaa.add(thu1);
					   aaa.add(thu);
				   }
				   if(day == 5) {
					   aaa.add(fri1);
					   aaa.add(fri);
				   }
				   if(day == 6) {
					   aaa.add(sat1);
					   aaa.add(sat);
				   }
				   if(day == 7) {
					   aaa.add(sun1);
					   aaa.add(sun);
				   }
				   
				 }
				 
				 // aaa sees on päevad millal action toimub - nyyd onvaja leida tänasele päevale lähin neist .
				 Collections.sort(aaa);
				 Iterator<LocalDate> asd = aaa.iterator();
				 while (asd.hasNext()) {
					 LocalDate fff = asd.next();
					 if(fff.isAfter(now)) {
						 kaes = fff;
						 break;
					 }
				 }
				 
				 
	//	System.out.println("kaes " + kaes + " "  );
		
				 try {
					 if(kaes != null) {
							java.util.Date d = new SimpleDateFormat("yyyy-MM-dd").parse(kaes.toString());
							res = Calendar.getInstance();
							res.setTime(d);
					 }
			
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
	  		    }
			//System.out.println("päev tagasi " + kaes);
			
	  	return res ;
	  }
	  

  private TreeMap getPandiPakend() {
	   TreeMap pants = new TreeMap();
		FileInputStream is = null;
		try {
			is = new FileInputStream("deposit.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(is, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(isr);
		
		
	    String         line = null;
	   
	    String         ls = System.getProperty("line.separator");

	  
	        try {
				while((line = reader.readLine()) != null) {
					int vord = line.indexOf("=");
					
					if(vord > -1) {
						String key =line.substring(0,vord) ;
					String val = line.substring(vord+1);
					
						pants.put(key,val);
					}
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

	  
	    return pants;
	 }
  
  
  private TreeMap getPandiPakendHind() throws SQLException{
	   Connection con = getNewConnection();
	   TreeMap pants = new TreeMap();
	    String sql1 = " SELECT PK_PIESK as PK_NOM, KODS as kood, LIKME AS price " + 
	    		"FROM APV04761.NPIESK " + 
	    		"WHERE KODS LIKE N'DEP%'" ;
	   
	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);
	    while (rs1.next()) {
	    	String koodnom = rs1.getString("PK_NOM").trim();
	      	String kood = rs1.getString("kood").trim();
	    	double hind = rs1.getDouble("price");
	  
	    	pants.put(kood, hind);
	    	pants.put(kood+"nom", koodnom);
	    }
	      // System.out.println(toode + " prod1 " + prod);
	    rs1.close();
	    st1.close();
	    con.close();
	    return pants;
	 }
	  
 
  
  
  private void loadWares(){
	  try{
		  defDealers = new TreeMap();
			ResultSet rs1 ;
	    	Statement stmr ;
			 String sqldea = "	  select  s.PK_KLIENTS as klient ,"
			 		+ " PK_KLGR as dealer from APV04761.DKLSAGR s  where PK_KLGR in ('267','268','269')";
		    		
		//	 267	zKD-D	Daugavpils Noliktava
		//	 268	zKD-L	Liepaja Noliktava
		//	 269	zKD-R	Riga Noliktava
//			 53268	RĪGAS NOLIKTAVA (Galvena)	N01
//				53269	Daugavpils noliktava	N02
//				53270	Liepājas noliktava	N03

			 
		        	Connection con = this.getNewConnection(); 
		        		 stmr = con.createStatement();
		            	
		                 rs1 = stmr.executeQuery(sqldea);
		                
		                 while(rs1.next()){
		                	String klient = rs1.getString("klient").trim();
		                	String dealer = rs1.getString("dealer").trim();
		                	if(dealer.equals("267")){
		                		dealer = "53269";
		                	}
		                	if(dealer.equals("268")){
		                		dealer = "53270";
		                	}
		                	if(dealer.equals("269")){
		                		dealer = "53268";
		                	}
		                	

		                	
		                	defDealers.put(klient, dealer);
		                }
		                
		               rs1.close();
		               stmr.close();
		               con.close();
		        	}catch(Exception s){
		        		s.printStackTrace();
		        	}

		    //	System.out.println("defDealers " + defDealers.size());
		    	
		    	
  }

  
  private void loadOsak(){
	  try{
		  defOsak = new TreeMap();
			ResultSet rs1 ;
	    	Statement stmr ;
			 String sqldea = "select t.kods as toode ,a.kods as osak from APV04761.NKSORTIMR r " +
	  "	inner join APV04761.NKSORTIM a on a.PK_KSORTIM= r.PK_KSORTIM INNER JOIN APV04761.NNOM t on t.pk_nom=r.pk_nom where a.kods !=  'M' order by r.pk_nom" ; 
		    	
		//	 267	zKD-D	Daugavpils Noliktava
		//	 268	zKD-L	Liepaja Noliktava
		//	 269	zKD-R	Riga Noliktava
//			 53268	RĪGAS NOLIKTAVA (Galvena)	N01
//				53269	Daugavpils noliktava	N02
//				53270	Liepājas noliktava	N03

			 
		        	Connection con = this.getNewConnection(); 
		        		 stmr = con.createStatement();
		            	
		                 rs1 = stmr.executeQuery(sqldea);
		                
		                 while(rs1.next()){
		                	String kood = rs1.getString("toode").trim();
		                	String osakond = rs1.getString("osak").trim();

		                	
		                	defOsak.put(kood, osakond);
		                }
		                
		               rs1.close();
		               stmr.close();
		               con.close();
		        	}catch(Exception s){
		        		s.printStackTrace();
		        	}

		    	//System.out.println("dfOsak " + defOsak.size());
		    	
		    	
  }

  private HashSet<String> makeBLK(){
	  
	  HashSet<String> res =  new HashSet<String>();
	  
	  try{
		
			ResultSet rs1 ;
	    	Statement stmr ;
			 String sqldea = " 	 select PK_KLIENTS as klient, bridin  from APV04761.DKLIENTS s where bridin like '%BLK%' " ; 
		    	
		        	Connection con = this.getNewConnection(); 
		        		 stmr = con.createStatement();
		                 rs1 = stmr.executeQuery(sqldea);
		                 
		                 while(rs1.next()){
		                    res.add(rs1.getString("klient").trim());
		                   }
		                
		               rs1.close();
		               stmr.close();
		               con.close();
		        	}catch(Exception s){
		        		s.printStackTrace();
		        	}

		    //	System.out.println("hrccustomers " + res.size());
		   return res ; 	
		    	
  }


  private HashSet<String> make261(){
	  
	  HashSet<String> res =  new HashSet<String>();
	  
	  try{
		
			ResultSet rs1 ;
	    	Statement stmr ;
			 String sqldea = " 	select PK_KLIENTS as klient from APV04761.DKLIENTS s where PK_APMV = 1 " ; 
		    	
		        	Connection con = this.getNewConnection(); 
		        		 stmr = con.createStatement();
		                 rs1 = stmr.executeQuery(sqldea);
		                 
		                 while(rs1.next()){
		                    res.add(rs1.getString("klient").trim());
		                   }
		                
		               rs1.close();
		               stmr.close();
		               con.close();
		        	}catch(Exception s){
		        		s.printStackTrace();
		        	}

		    	System.out.println("261customers " + res.size());
		   return res ; 	
		    	
  }

    
  
  private ArrayList<String> makeHRC(){
	  
	  ArrayList<String> res =  new ArrayList<String>();
	  
	  try{
		
			ResultSet rs1 ;
	    	Statement stmr ;
			 String sqldea = " 	  select  s.PK_KLIENTS as klient from APV04761.DKLSAGR s  where PK_KLGR = 145 " ; 
		    	
		        	Connection con = this.getNewConnection(); 
		        		 stmr = con.createStatement();
		                 rs1 = stmr.executeQuery(sqldea);
		                 
		                 while(rs1.next()){
		                    res.add(rs1.getString("klient").trim());
		                   }
		                
		               rs1.close();
		               stmr.close();
		               con.close();
		        	}catch(Exception s){
		        		s.printStackTrace();
		        	}

		    //	System.out.println("hrccustomers " + res.size());
		   return res ; 	
		    	
  }

  
  

  private void getKont(){
	  try{

		  kontOK = new TreeMap();
		  kontNO = new TreeMap();
		  kontNOD = new TreeMap();
		  defAgents = new TreeMap();
			
		    ResultSet rs1 ;
	    	Statement stmr ;
		    String sqldea = "  SELECT A.PK_KLIENTS AS kid, A.PK_ADRESE AS aid , A.PK_PARD as agent , POST_ADR as asi " +
	"	      FROM   APV04761.DKLIENTS K LEFT OUTER JOIN  APV04761.NADRESES a ON A.PK_KLIENTS=K.PK_KLIENTS and a.LEG_ADR = 2 " + 
	"	 LEFT OUTER JOIN APV04761.DGRUPAS GRU ON K.PK_GRUPA = GRU.PK_GRUPA  " +
	 "    WHERE (K.STATUSS <> 2 AND K.PK_DOKT IS NOT NULL AND ( NOT K.PAMV_ID IN (1810,11970))) AND " +
	 "    (( ( (K.TIPS = 3)  OR  (K.TIPS = 4)  OR  (K.TIPS = 6)  OR  (K.TIPS = 8) ) AND ( (K.STATUSS = 0)  OR " +
	 "     (K.STATUSS = 1) ) AND ( (K.STATUSS = 0) ) AND ( (K.TIPS <> 8) ) AND ( (K.DBKRPAZ = 1) ) ))  and K.KODS is not null and A.PK_KLIENTS is not null " +
	 "    ORDER BY   K.NOSAUK ASC ";
		
			 
		        	Connection con = this.getNewConnection(); 
		        		 stmr = con.createStatement();
		                 rs1 = stmr.executeQuery(sqldea);
		                
		                 while(rs1.next()){
		                	String kid = rs1.getString("kid").trim();
		                	String aid = rs1.getString("aid").trim();
		                	String agent = rs1.getString("agent");
		                	int asi = rs1.getInt("asi");
		                	
		                	if(asi == 1){
		                		kontOK.put(kid, aid);	
		                	}else{
		                		if(agent != null && agent.trim().equals("91")) { // 
		                			kontNOD.put(kid, aid);		
		                		} else {
		                			kontNO.put(kid, aid);	
		                		}
		                			
		                	}
		                	
		                	if(agent != null && agent.trim().length() > 0) {
		                		defAgents.put(kid, agent.trim());
		                		defAgents.put(aid, agent.trim());
				                
		                	}
		                }
		                
		               rs1.close();
		               stmr.close();
		               con.close();
		        	}catch(Exception s){
		        		s.printStackTrace();
		        	}

		   // 	System.out.println("defDealers " + kontOK.size());
		    	
		    	
  }

  
  
  TreeMap defDealers = null;
  TreeMap defOsak = null;
  TreeMap defAgents = null;


  
  public  String getExcelDir(){
      String dir = parent.getProps().getProperty("excel.dir");
      if(dir != null){
    	   if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";  
      }else{
    	    dir =  "excel/"; 
      }
   
      
      return dir;
    }
  
  
    private TreeMap kontOK = null;
    private Properties propsVEGE = null;
    private TreeMap kontNO = null;
    private TreeMap kontNOD = null;
  
    
    
  private Vector splitOrder (Order order){
	
	
	
		
	  String mills = (Calendar.getInstance().getTimeInMillis()+ "").substring(10);
	  
	  
	  Vector result = new Vector();
	  if(defDealers == null){
		  loadWares();
	  }
	  
	  if(kontOK == null){
		  getKont();
		loadOsak();
		//System.out.println("aadresite suurused " + kontOK.size() + " " + kontNO.size() + " " + kontNOD.size());
	  }
	  
	

	 
	  
	  
		  if(order.getDealer() == null || order.getDealer().trim().length() == 0){
			  order.setDealer("53268");
		  }
		  
		  System.out.println("defDealers " + defDealers.get(order.getCompany()));
		  
		  if(order.getStatus() > 6 && defDealers != null && defDealers.get(order.getCompany()) != null){
			  order.setWarehouse(""+defDealers.get(order.getCompany()));
			  order.setDealer(""+defDealers.get(order.getCompany())+"");
		   }else if(order.getStatus() > 6){
			   order.setWarehouse("53268");
				 order.setDealer("53268");
				 this.trouble = " Nav noliktava in Horizon SPLIT NO HRC";
		   }
		
          String cuKont = kontOK.get(order.getCompany())+"";
          
          String cuNot = kontNO.get(order.getCompany())+"";
          String cuNotD = kontNOD.get(order.getCompany())+"";
		  
          if((cuKont == null || cuKont.length() == 0 ) &&
        		  cuNot != null && cuNot.length() > 0  ){
        	  cuKont = cuNot; 
          }
          
          if((cuNot == null || cuNot.length() == 0 ) &&
        		  cuKont != null && cuKont.length() > 0  ){
        	   cuNot = cuKont; 
          }
          
          
       //   System.out.println("CuNot " + order.getCompany() + " " + cuNot + " " + cuKont);
          
          Order ordc = new Order(order.getCompany()+ "_" + cuNot ,order.getAgentFirstName());

	  
	  String tahtc = "c";
	  ordc.setAgentCode(order.getAgentCode());
	  ordc.setDokNr(order.getDokNr() + tahtc + mills);
	  ordc.setInfo(order.getInfo());
	  ordc.setTeostaja((order.getTeostaja()));
	  ordc.setChanged(order.isChanged());
	  ordc.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordc.setStatus(order.getStatus());
	  ordc.setShipDateUsed(order.isShipDateUsed());
	  ordc.setShipTime(order.getShipTime());
	  ordc.setWarehouse(order.getWarehouse());
	  ordc.setDealer(order.getDealer());
	  ordc.setPaymTerm(order.getPaymTerm());
	  ordc.setCurrency(getType(order.getDealer(),1,order)); // p mitte
	  
	  
 Order ordcFI = new Order(order.getCompany()+ "_" + cuKont ,order.getAgentFirstName());

	  
	  String tahtcFI = "cf";
	  ordcFI.setAgentCode(order.getAgentCode());
	  ordcFI.setDokNr(order.getDokNr() + tahtcFI + mills);
	  ordcFI.setInfo(order.getInfo());
	  ordcFI.setTeostaja((order.getTeostaja()));
	  ordcFI.setChanged(order.isChanged());
	  ordcFI.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordcFI.setStatus(order.getStatus());
	  ordcFI.setShipDateUsed(order.isShipDateUsed());
	  ordcFI.setShipTime(order.getShipTime());
	  ordcFI.setWarehouse(order.getWarehouse());
	  ordcFI.setDealer(order.getDealer());
	  ordcFI.setPaymTerm(order.getPaymTerm());
	  ordcFI.setCurrency(getType(order.getDealer(),1,order)); // p mitte
	  
	  
	  if(cuNotD == null) {
		  cuNotD = cuNot;
	  }
	  
 Order ordcD = new Order(order.getCompany()+ "_" + cuNotD ,order.getAgentFirstName());

	  
	  String tahtcD = "cd";
	  ordcD.setAgentCode(order.getAgentCode());
	  ordcD.setDokNr(order.getDokNr() + tahtcD + mills);
	  ordcD.setInfo(order.getInfo());
	  ordcD.setTeostaja((order.getTeostaja()));
	  ordcD.setChanged(order.isChanged());
	  ordcD.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordcD.setStatus(order.getStatus());
	  ordcD.setShipDateUsed(order.isShipDateUsed());
	  ordcD.setShipTime(order.getShipTime());
	  String eridealer = getERI(order.getDealer());
	 
	  ordcD.setWarehouse(eridealer);
	  ordcD.setDealer(eridealer);
	  ordcD.setPaymTerm(order.getPaymTerm());
	  ordcD.setCurrency(getType(order.getDealer(),20,ordcD)); // p mitte
	//  System.out.println("eridealer00 " + order.getDealer() + " " + eridealer + " " + ordcD.getCurrency());
	 
	  
	  Order ord1 = new Order(order.getCompany()+ "_" + cuNot ,order.getAgentFirstName());
  
	  String taht1 = "p";
	  ord1.setAgentCode(order.getAgentCode());
	  ord1.setDokNr(order.getDokNr() + taht1 + mills);
	  ord1.setInfo(order.getInfo());
	  ord1.setTeostaja((order.getTeostaja()));
	  ord1.setChanged(order.isChanged());
	  ord1.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord1.setStatus(order.getStatus());
	  ord1.setShipDateUsed(order.isShipDateUsed());
	  ord1.setShipTime(order.getShipTime());
	  ord1.setWarehouse(order.getWarehouse());
	  ord1.setDealer(order.getDealer());
	  ord1.setPaymTerm(order.getPaymTerm());
	  ord1.setCurrency(getType(order.getDealer(),1,order)); // p mitte
	 
	  Order ord2 = new Order((order.getCompany() + "_" + cuNot ),order.getAgentFirstName());
	  ord2.setAgentCode(order.getAgentCode());
	  ord2.setDokNr(order.getDokNr()+ "m"+ mills);
	  ord2.setInfo(order.getInfo());
	  ord2.setShipDateUsed(order.isShipDateUsed());
	  ord2.setTeostaja((order.getTeostaja()));
	  ord2.setChanged(order.isChanged());
	  ord2.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord2.setStatus(order.getStatus());
	  ord2.setShipTime(order.getShipTime());
	  ord2.setWarehouse(order.getWarehouse());
	  ord2.setPaymTerm(order.getPaymTerm());
	  ord2.setDealer(order.getDealer());
	  ord2.setCurrency(getType(order.getDealer(),2,order)); // M
		

	  Order ord3 = new Order((order.getCompany() + "_" + cuNot ),order.getAgentFirstName());
	  ord3.setAgentCode(order.getAgentCode());
	  ord3.setDokNr(order.getDokNr()+ "n"+ mills);
	  ord3.setInfo(order.getInfo());
	  ord3.setTeostaja((order.getTeostaja()));
	  ord3.setChanged(order.isChanged());
	  ord3.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord3.setStatus(order.getStatus());
	  ord3.setShipTime(order.getShipTime());
	  ord3.setWarehouse(order.getWarehouse());
	  ord3.setShipDateUsed(order.isShipDateUsed());
	  ord3.setDealer(order.getDealer());
	  ord3.setPaymTerm(order.getPaymTerm());
	  ord3.setCurrency(getType(order.getDealer(),3,order)); // RM N

	  
	  Order ord4 = new Order((order.getCompany() + "_" + cuKont),order.getAgentFirstName());
	  String taht4 = "f";
	  ord4.setAgentCode(order.getAgentCode());
	  ord4.setDokNr(order.getDokNr()+ taht4+ mills);
	  ord4.setInfo(order.getInfo());
	  ord4.setPaymTerm(order.getPaymTerm());
	  ord4.setTeostaja((order.getTeostaja()));
	  ord4.setChanged(order.isChanged());
	  ord4.setShipDateUsed(order.isShipDateUsed());
	  ord4.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord4.setStatus(order.getStatus());
	  ord4.setShipTime(order.getShipTime());
	  ord4.setWarehouse(order.getWarehouse());
	  ord4.setDealer(order.getDealer());
	  ord4.setCurrency(getType(order.getDealer(),4,order));
		 
	 
	  String taht5 = "h";	  

      Order ord5 = new Order((order.getCompany()+ "_" + cuNot),order.getAgentFirstName());
	  ord5.setAgentCode(order.getAgentCode());
	  ord5.setDokNr(order.getDokNr()+ taht5+ mills);
	  ord5.setInfo(order.getInfo());
	  ord5.setPaymTerm(order.getPaymTerm());
	  ord5.setTeostaja((order.getTeostaja()));
	  ord5.setChanged(order.isChanged());
	  ord5.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord5.setStatus(order.getStatus());
	  ord5.setShipTime(order.getShipTime());
	  ord5.setWarehouse(order.getWarehouse());
	  ord5.setShipDateUsed(order.isShipDateUsed());
	  ord5.setDealer(order.getDealer());
	  ord5.setCurrency(getType(order.getDealer(),5,order)); // täht H
		 

      Order ord6 = new Order((order.getCompany()+ "_" + cuKont),order.getAgentFirstName());
	  String taht6 = "x"; 
	  ord6.setAgentCode(order.getAgentCode());
	  ord6.setDokNr(order.getDokNr() + taht6+ mills);
	  ord6.setInfo(order.getInfo());
	  ord6.setTeostaja((order.getTeostaja()));
	  ord6.setPaymTerm(order.getPaymTerm());
	  ord6.setChanged(order.isChanged());
	  ord6.setShipDateUsed(order.isShipDateUsed());
	  ord6.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord6.setStatus(order.getStatus());
	  ord6.setShipTime(order.getShipTime());
	  ord6.setWarehouse(order.getWarehouse());
	  ord6.setDealer(order.getDealer());
	  ord6.setCurrency(getType(order.getDealer(),6,order)); // täht X
		 


      Order ord7 = new Order((order.getCompany() + "_" + cuKont),order.getAgentFirstName());
	  String taht7 = "b";
	  ord7.setAgentCode(order.getAgentCode());
	  ord7.setDokNr(order.getDokNr() + taht7+ mills);
	  ord7.setInfo(order.getInfo());
	  ord7.setTeostaja((order.getTeostaja()));
	  ord7.setChanged(order.isChanged());
	  ord7.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord7.setStatus(order.getStatus());
	  ord7.setShipDateUsed(order.isShipDateUsed());
	  ord7.setShipTime(order.getShipTime());
	  ord7.setPaymTerm(order.getPaymTerm());
	  ord7.setWarehouse(order.getWarehouse());
	  ord7.setDealer(order.getDealer());
	  ord7.setCurrency(getType(order.getDealer(),7,order)); // täht B
	  
		/*  53268	RĪGAS NOLIKTAVA (Galvena)	N01
	  53269	Daugavpils noliktava	N02
	  53270	Liepājas noliktava	N03
	  53271	Atdzesetas GAĻAS noliktava BAJARDS	N04
	  53272	Atdzesetas ZIVIS noliktava ROTANS	N05
*/  
	  Order ord8 = new Order((order.getCompany() + "_" + cuKont),order.getAgentFirstName());
	  String taht8 = "u";
	 
	  ord8.setAgentCode(order.getAgentCode());
	  ord8.setDokNr(order.getDokNr() + taht8+ mills);
	  ord8.setInfo(order.getInfo());
	  ord8.setTeostaja((order.getTeostaja()));
	  ord8.setChanged(order.isChanged());
	  ord8.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord8.setStatus(order.getStatus());
	  ord8.setShipTime(order.getShipTime());
	  ord8.setShipDateUsed(order.isShipDateUsed());
	  ord8.setPaymTerm(order.getPaymTerm());
	  ord8.setWarehouse(order.getWarehouse());
	  ord8.setDealer(order.getDealer());
	  ord8.setCurrency(getType(order.getDealer(),8,order)); // P tähega

	  
	  
	  Order ord9 = new Order((order.getCompany()+ "_" + cuKont),order.getAgentFirstName());
	  String taht9 = "g"; 
	  ord9.setAgentCode(order.getAgentCode());
	  ord9.setDokNr(order.getDokNr() + taht9 + mills);
	  ord9.setInfo(order.getInfo());
	  ord9.setTeostaja((order.getTeostaja()));
	  ord9.setChanged(order.isChanged());
	  ord9.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord9.setStatus(order.getStatus());
	  ord9.setShipTime(order.getShipTime());
	  ord9.setPaymTerm(order.getPaymTerm());
	  ord9.setShipDateUsed(order.isShipDateUsed());
	  ord9.setWarehouse(order.getWarehouse());
	  ord9.setDealer("85349");
	  ord9.setCurrency(getType(order.getDealer(),9,order)); // G tähega
	  


	  Order ord10 = new Order((order.getCompany()+ "_" + cuKont),order.getAgentFirstName()); // J tähega tellimus 
	  String taht10 = "j";
	  ord10.setAgentCode(order.getAgentCode());
	  ord10.setDokNr(order.getDokNr() + taht10 + mills);
	  ord10.setInfo(order.getInfo());
	  ord10.setTeostaja((order.getTeostaja()));
	  ord10.setChanged(order.isChanged());
	  ord10.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord10.setStatus(order.getStatus());
	  ord10.setShipTime(order.getShipTime());
	  ord10.setWarehouse(order.getWarehouse());
	  ord10.setPaymTerm(order.getPaymTerm());
	  ord10.setShipDateUsed(order.isShipDateUsed());
	  ord10.setDealer("53272");
	  ord10.setCurrency(getType(order.getDealer(),10,order));
	  
	 
	  
	  
	  Order ord10o = new Order((order.getCompany()+ "_" + cuKont),order.getAgentFirstName()); // O tähega tellimus 
	  String taht10o = "o";
	  ord10o.setAgentCode(order.getAgentCode());
	  ord10o.setDokNr(order.getDokNr() + taht10o + mills);
	  ord10o.setInfo(order.getInfo());
	  ord10o.setTeostaja((order.getTeostaja()));
	  ord10o.setChanged(order.isChanged());
	  ord10o.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord10o.setStatus(order.getStatus());
	  ord10o.setShipTime(order.getShipTime());
	  ord10o.setWarehouse(order.getWarehouse());
	  ord10o.setPaymTerm(order.getPaymTerm());
	  ord10o.setDealer(order.getDealer());
	  ord10o.setCurrency(getType(order.getDealer(),11,order));
	  ord10o.setShipDateUsed(order.isShipDateUsed());
	 
	  String lisaV = cuKont;
	  if(propsVEGE.containsKey(order.getCompany())) {
		  lisaV = propsVEGE.getProperty(order.getCompany());
	  }
	  
	  Order ord10d = new Order((order.getCompany()+ "_" + lisaV),order.getAgentFirstName()); // D tähega tellimus 
	  String taht1d = "d";
	  ord10d.setAgentCode(order.getAgentCode());
	  ord10d.setDokNr(order.getDokNr() + taht1d + mills);
	  ord10d.setInfo(order.getInfo());
	  ord10d.setTeostaja((order.getTeostaja()));
	  ord10d.setChanged(order.isChanged());
	  ord10d.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord10d.setStatus(order.getStatus());
	  ord10d.setShipTime(order.getShipTime());
	  ord10d.setPaymTerm(order.getPaymTerm());
	  ord10d.setWarehouse(order.getWarehouse());
	  ord10d.setShipDateUsed(order.isShipDateUsed());
	  ord10d.setDealer("81161");
	  ord10d.setCurrency(getType(order.getDealer(),12,order));
	  
	  Order ord10pp = new Order((order.getCompany()+ "_" + lisaV),order.getAgentFirstName()); // D tähega tellimus 
	  String taht1pp = "pp";
	  ord10pp.setAgentCode(order.getAgentCode());
	  ord10pp.setDokNr(order.getDokNr() + taht1pp + mills);
	  ord10pp.setInfo(order.getInfo());
	  ord10pp.setTeostaja((order.getTeostaja()));
	  ord10pp.setChanged(order.isChanged());
	  ord10pp.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord10pp.setStatus(order.getStatus());
	  ord10pp.setShipTime(order.getShipTime());
	  ord10pp.setPaymTerm(order.getPaymTerm());
	  ord10pp.setWarehouse(order.getWarehouse());
	  ord10pp.setShipDateUsed(order.isShipDateUsed());
	  ord10pp.setDealer(order.getDealer());
	  ord10pp.setCurrency(getType(order.getDealer(),1,order)); // p mitte
	  
	  Order ord10fp = new Order((order.getCompany() + "_" + lisaV),order.getAgentFirstName());
	  String taht4p = "fp";
	  ord10fp.setAgentCode(order.getAgentCode());
	  ord10fp.setDokNr(order.getDokNr()+ taht4p+ mills);
	  ord10fp.setInfo(order.getInfo());
	  ord10fp.setPaymTerm(order.getPaymTerm());
	  ord10fp.setTeostaja((order.getTeostaja()));
	  ord10fp.setChanged(order.isChanged());
	  ord10fp.setShipDateUsed(order.isShipDateUsed());
	  ord10fp.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord10fp.setStatus(order.getStatus());
	  ord10fp.setShipTime(order.getShipTime());
	  ord10fp.setWarehouse(order.getWarehouse());
	  ord10fp.setDealer(order.getDealer());
	  ord10fp.setCurrency(getType(order.getDealer(),4,order));
	  
	  
	  Order ord10r = new Order((order.getCompany()+ "_" + cuKont),order.getAgentFirstName()); // D tähega tellimus 
	  String taht1r = "r";
	  ord10r.setAgentCode(order.getAgentCode());
	  ord10r.setDokNr(order.getDokNr() + taht1r + mills);
	  ord10r.setInfo(order.getInfo());
	  ord10r.setTeostaja((order.getTeostaja()));
	  ord10r.setChanged(order.isChanged());
	  ord10r.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord10r.setStatus(order.getStatus());
	  ord10r.setPaymTerm(order.getPaymTerm());
	  ord10r.setShipTime(order.getShipTime());
	  ord10r.setWarehouse(order.getWarehouse());
	  ord10r.setShipDateUsed(order.isShipDateUsed());
	  
	  
	  Order ord92 = new Order((order.getCompany()+ "_" + cuKont),order.getAgentFirstName());
	  String taht92 = "z"; 
	  ord92.setAgentCode(order.getAgentCode());
	  ord92.setDokNr(order.getDokNr() + taht92 + mills);
	  ord92.setInfo(order.getInfo());
	  ord92.setTeostaja((order.getTeostaja()));
	  ord92.setChanged(order.isChanged());
	  ord92.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord92.setStatus(order.getStatus());
	  ord92.setShipTime(order.getShipTime());
	  ord92.setPaymTerm(order.getPaymTerm());
	  ord92.setShipDateUsed(order.isShipDateUsed());
	  ord92.setWarehouse(order.getWarehouse());
	  ord92.setDealer("82450");
	  ord92.setCurrency("NPARMP"); // G tähega
	  
	  String dea = "53277";
	  if(order.getDealer().equals("53269")) {
		  dea = "81208";
	  }else if(order.getDealer().equals("53270")) {
		  dea = "81209";
	  }
	  
	  ord10r.setDealer(dea);
	  ord10r.setCurrency(getType(order.getDealer(),13,order));
	  
	  
	  
	  
	  //System.out.println("Splitin Warehouse agendidY " + order.getWarehouse());
	  
	  Vector lines = order.getOrderLines();
	  Enumeration enums = lines.elements();
	  while  (enums.hasMoreElements()){
		  OrderLine lin = (OrderLine)enums.nextElement();
		
		     String itemid = lin.getToode();
		    System.out.println("ppcheck " + lin.getToode() + " " + lin.getKogus() + " " + order.getPaymTerm());
         if(order.getPaymTerm() == -1 ){
        	 
        	 if(itemid.startsWith("FP") && propsVEGE.containsKey(order.getCompany()) ){
             	ord10fp.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
   		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
             	
             
             }else if(itemid.startsWith("F") || itemid.startsWith("I") ){
                	ordcFI.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
   		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
                } else if(itemid.startsWith("D") ){
                	ordcD.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
      		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
                }else if(itemid.startsWith("PP") && propsVEGE.containsKey(order.getCompany()) ){
                	ord10pp.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
      		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
                	
                
                } else  if(itemid.startsWith("Z") ){
          	    	 
          	    	 ord92.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
          		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
          	  
                    }else if(itemid.startsWith("P") ){
                    boolean FF = true;
          	    	 if(defOsak.get(lin.getToode()) != null && defOsak.get(lin.getToode()).equals("NF")){
          	    		 FF = false;
          	    	 }
          	    	 if(FF){
          	    		ordcFI.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
          		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
                        
          	    	 }else {
          	    		ordc.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
       		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
          	    	 }
          	    	 
          	    		 
          	    	
                	
                } else {
                	ordc.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
    		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
                }
     	    	 
                
                
         
   	   
   	    	 
                
     	  
               } else if(itemid.startsWith("J") ){
     	    	 
     	    	 ord10.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
     		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
     	  
               } else if(itemid.startsWith("RM") ){
         	    	 
         	    	 ord10r.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
         		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
         	         // System.out.println("ord10rm " + lin.getToode() + " " + lin.getKogus());
              
                 } else if(itemid.startsWith("D") ){
       	    	 
       	    	 ord10d.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
       		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
       	        //  System.out.println("ord10d " + lin.getToode() + " " + lin.getKogus());
           } else  if(itemid.startsWith("Z") ){
          	    	 
          	    	 ord92.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
          		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
          	  
                    
               }else if(itemid.startsWith("G") ){
         	    	 
         	    	 ord9.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
         		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
         	  
                    }else if(itemid.startsWith("O") ){
           	    	 
                    	 ord10o.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
              		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
           	  
                     }else if(itemid.startsWith("PP") && propsVEGE.containsKey(order.getCompany()) ){
                     	ord10pp.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
         		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
                   	
                   	// System.out.println("ord10pp " + lin.getToode() + " " + lin.getKogus());
                   }else if(itemid.startsWith("P") || itemid.startsWith("A") ){
            	    	 boolean FF = true;
            	    	 if(defOsak.get(lin.getToode()) != null && defOsak.get(lin.getToode()).equals("NF")){
            	    		 FF = false;
            	    	 }
            	    	 if(FF){
            	    		 ord8.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
            		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
            	   
            	    	 }else{
            	    		 ord1.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
            		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
            	   
            	    	 }
            	    	
            	    	
                      }else if(itemid.startsWith("B")){
              	    	 
              	    	 ord7.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
              		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
              	  
                      }else if(itemid.startsWith("X") ){
                 	    	 
                 	    	 ord6.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
                 		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
                 	  
                      }else if(itemid.startsWith("H") ){
                 	    	//paneme P mitte kontaktsega kaasa  
                 	    	 ord1.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
                 		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
                 	  
                      }else if(itemid.startsWith("FP") && propsVEGE.containsKey(order.getCompany()) ){
                       	ord10fp.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
          		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
                    	
                    
                      }else if(itemid.startsWith("F") || itemid.startsWith("I") ){
             	    	 
	    	                ord4.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			    lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
	  
                      }else if(itemid.startsWith("N")){
		    	 
		    	             ord3.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			             lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		  
		     	      }else if(itemid.startsWith("M") ){
         
		     	    	  ord2.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			             lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		     	        }
		   
		     

	  }



	  if(ord1.getOrderLines() != null && ord1.getOrderLines().size()>0){
		  result.add(ord1);  
	  }
	  if(ordc.getOrderLines() != null && ordc.getOrderLines().size()>0){
		  result.add(ordc);  
	  }
	  if(ordcFI.getOrderLines() != null && ordcFI.getOrderLines().size()>0){
		  result.add(ordcFI);  
	  }
	  if(ordcD.getOrderLines() != null && ordcD.getOrderLines().size()>0){
		  result.add(ordcD);  
	  }
	  if(ord92.getOrderLines() != null && ord92.getOrderLines().size()>0){
		  result.add(ord92);  
	  }
	  if(ord2.getOrderLines() != null && ord2.getOrderLines().size()>0){
		  result.add(ord2);  
	  }
	  if(ord10fp.getOrderLines() != null && ord10fp.getOrderLines().size()>0){
		  result.add(ord10fp);  
	  }
	  
	  
	  if(ord3.getOrderLines() != null && ord3.getOrderLines().size()>0){
		  result.add(ord3);  
	  }
	  
	  if(ord10pp.getOrderLines() != null && ord10pp.getOrderLines().size()>0){
		  result.add(ord10pp);  
	  }
	  
	  

	  if(ord4.getOrderLines() != null && ord4.getOrderLines().size()>0){
		  result.add(ord4);  
	  }

	  if(ord5.getOrderLines() != null && ord5.getOrderLines().size()>0){
		  result.add(ord5);  
	  }

	  if(ord6.getOrderLines() != null && ord6.getOrderLines().size()>0){
		  result.add(ord6);  
	  }

	  if(ord7.getOrderLines() != null && ord7.getOrderLines().size()>0){
		  result.add(ord7);  
	  }
	  
	  if(ord8.getOrderLines() != null && ord8.getOrderLines().size()>0){
		  result.add(ord8);  
	  }
	  if(ord9.getOrderLines() != null && ord9.getOrderLines().size()>0){
		  result.add(ord9);  
	  }
	  if(ord10.getOrderLines() != null && ord10.getOrderLines().size()>0){
		  result.add(ord10);  
	  }
	  if(ord10o.getOrderLines() != null && ord10o.getOrderLines().size()>0){
		  result.add(ord10o);  
	  }
	  if(ord10d.getOrderLines() != null && ord10d.getOrderLines().size()>0){
		  result.add(ord10d);  
	  }
	  if(ord10r.getOrderLines() != null && ord10r.getOrderLines().size()>0){
		  result.add(ord10r);  
	  }
	  
	 //System.exit(0);
	  
	 return result ;
  }

  
  
private String getERI(String ware) {

	if(ware.equals("53268") || ware.equals("22013")){ // n01 riga 
		
		return "81161";
	}
	if(ware.equals("53269") || ware.equals("46801")){ // n02 daugavpils 
		
		return "81734";
	}
	if(ware.equals("53270") || ware.equals("45112")){ // n03 liepaja
		return "81735";
	}
	return null;
}


private Vector splitOrderByAmNOTINUSE (Order order, int num){
Vector result = new Vector();

Order ord9 = new Order(order.getCompany(),order.getAgentFirstName());

ord9.setAgentCode(order.getAgentCode());
ord9.setDokNr(order.getDokNr());
ord9.setInfo(order.getInfo());
ord9.setTeostaja((order.getTeostaja()));
ord9.setChanged(order.isChanged());
ord9.setShipCal(order.getShipCal().getTimeInMillis() +"");
ord9.setStatus(order.getStatus());
ord9.setShipTime(order.getShipTime());
ord9.setPaymTerm(order.getPaymTerm());
ord9.setShipDateUsed(order.isShipDateUsed());
ord9.setWarehouse(order.getWarehouse());
ord9.setDealer(order.getDealer());
ord9.setCurrency(order.getCurrency()); // G tähega

Order ord2 = new Order((order.getCompany() ),order.getAgentFirstName());
ord2.setAgentCode(order.getAgentCode());
ord2.setDokNr(order.getDokNr() + "+");
ord2.setInfo(order.getInfo());
ord2.setTeostaja((order.getTeostaja()));
ord2.setShipDateUsed(order.isShipDateUsed());
ord2.setChanged(order.isChanged());
ord2.setShipCal(order.getShipCal().getTimeInMillis() +"");
ord2.setStatus(order.getStatus());
ord2.setShipTime(order.getShipTime());
ord2.setPaymTerm(order.getPaymTerm());
ord2.setWarehouse(order.getWarehouse());
ord2.setDealer(order.getDealer());
ord2.setCurrency(order.getCurrency()); // M
	


//System.out.println("Splitin Warehouse agendid " + order.getWarehouse());

Vector lines = order.getOrderLines();
Enumeration enums = lines.elements();
int coun = 1;
while  (enums.hasMoreElements()){
OrderLine lin = (OrderLine)enums.nextElement();

   if(coun < num+1){
   	 ord9.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
	     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
 
     } else {
  	 
		 ord2.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
   			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
	
      }
   coun++;

}


if(ord2.getOrderLines() != null && ord2.getOrderLines().size()>0){
result.add(ord2);  
}


if(ord9.getOrderLines() != null && ord9.getOrderLines().size()>0){
result.add(ord9);  
}



//System.exit(0);
return result ;
}


private Vector splitOrderHRC (Order order){
	
	  
	  String mills = (Calendar.getInstance().getTimeInMillis()+ "").substring(10);
	  
	  
	  Vector result = new Vector();
	  if(defDealers == null){
		  loadWares();
	  }
	  
	  if(kontOK == null){
		getKont();
		loadOsak();
		//System.out.println("aadresite suurused " + kontOK.size() + " " + kontNO.size());
	  }
	  
	
	  
	  
		  if(order.getDealer() == null || order.getDealer().trim().length() == 0){
			  order.setDealer("53268");
		  }
		System.out.println("ladu " + defDealers.get(order.getCompany()) + " " + order.getCompany() );
		  
		  if(order.getStatus() > 6 && defDealers != null && defDealers.get(order.getCompany()) != null){
			  order.setWarehouse(""+defDealers.get(order.getCompany()));
			  order.setDealer(""+defDealers.get(order.getCompany())+"");
		   }else if(order.getStatus() > 6){
			   order.setWarehouse("53268");
				 order.setDealer("53268");
				 this.trouble = " Nav noliktava in Horizon HRC ";
		   }
		
          String cuKont = kontOK.get(order.getCompany())+"";
         
          
         System.out.println("CuKont " + order.getWarehouse());
          
          
 

          Order ord9 = new Order((order.getCompany()+ "_" + cuKont),order.getAgentFirstName());
    	  String taht9 = "g"; 
    	  ord9.setAgentCode(order.getAgentCode());
    	  ord9.setDokNr(order.getDokNr() + taht9 + mills);
    	  ord9.setInfo(order.getInfo());
    	  ord9.setTeostaja((order.getTeostaja()));
    	  ord9.setChanged(order.isChanged());
    	  ord9.setShipCal(order.getShipCal().getTimeInMillis() +"");
    	  ord9.setStatus(order.getStatus());
    	  ord9.setShipTime(order.getShipTime());
    	  ord9.setPaymTerm(order.getPaymTerm());
    	  ord9.setShipDateUsed(order.isShipDateUsed());
    	  ord9.setWarehouse(order.getWarehouse());
    	  ord9.setDealer("85349");
    	  ord9.setCurrency("NDM33"); // G tähega
          
    	  Order ord2 = new Order((order.getCompany() + "_" + cuKont ),order.getAgentFirstName());
    	  ord2.setAgentCode(order.getAgentCode());
    	  ord2.setDokNr(order.getDokNr()+ "m"+ mills);
    	  ord2.setInfo(order.getInfo());
    	  ord2.setTeostaja((order.getTeostaja()));
    	  ord2.setShipDateUsed(order.isShipDateUsed());
    	  ord2.setChanged(order.isChanged());
    	  ord2.setShipCal(order.getShipCal().getTimeInMillis() +"");
    	  ord2.setStatus(order.getStatus());
    	  ord2.setShipTime(order.getShipTime());
    	  ord2.setPaymTerm(order.getPaymTerm());
    	  ord2.setWarehouse(order.getWarehouse());
    	  ord2.setDealer(order.getDealer());
    	  ord2.setCurrency(getTypeHRC(order.getDealer(),1,order)); // M
    		
    	  Order ord92 = new Order((order.getCompany()+ "_" + cuKont),order.getAgentFirstName());
    	  String taht92 = "z"; 
    	  ord92.setAgentCode(order.getAgentCode());
    	  ord92.setDokNr(order.getDokNr() + taht92 + mills);
    	  ord92.setInfo(order.getInfo());
    	  ord92.setTeostaja((order.getTeostaja()));
    	  ord92.setChanged(order.isChanged());
    	  ord92.setShipCal(order.getShipCal().getTimeInMillis() +"");
    	  ord92.setStatus(order.getStatus());
    	  ord92.setShipTime(order.getShipTime());
    	  ord92.setPaymTerm(order.getPaymTerm());
    	  ord92.setShipDateUsed(order.isShipDateUsed());
    	  ord92.setWarehouse(order.getWarehouse());
    	  ord92.setDealer("82450");
    	  ord92.setCurrency("NPARMP"); // G tähega
	  
	//  System.out.println("Splitin Warehouse agendid " + order.getWarehouse());
	  
	  Vector lines = order.getOrderLines();
	  Enumeration enums = lines.elements();
	  while  (enums.hasMoreElements()){
		  OrderLine lin = (OrderLine)enums.nextElement();
		
		     String itemid = lin.getToode();
		     if(itemid.startsWith("G") ){
     	    	 
     	    	 ord9.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
     		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
     	  
               } else  if(itemid.startsWith("Z") ){
       	    	 
       	    	 ord92.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
       		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
       	  
                 } else {
		    	 
	    		 ord2.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
	    	
                }
            

	  }

	
	  if(ord2.getOrderLines() != null && ord2.getOrderLines().size()>0){
		  result.add(ord2);  
	  }
	  if(ord92.getOrderLines() != null && ord92.getOrderLines().size()>0){
		  result.add(ord92);  
	  }

	  if(ord9.getOrderLines() != null && ord9.getOrderLines().size()>0){
		  result.add(ord9);  
	  }
	  
	  

		 //System.exit(0);
	  
	 return result ;
  }
  


private String getType(String ware , int num , Order ord ){
	String res = "";
	
//	 53268	RĪGAS NOLIKTAVA (Galvena)	N01
//		53269	Daugavpils noliktava	N02
//		53270	Liepājas noliktava	N03
//J - всегда склад N05 и тип документа NDM2G - всегда склад N04 и тип документа NDM3
	
//	System.out.println("type "+ ware + " "+ num + " " );
	
	
	
/*	kui on sull siis tagasi nii
	Riga  NDM
	Liepaja NDM1
	Daugavpils NDMD2
*/
	
	
	
	
	if(ord.getPaymTerm() == -1 || (this.h261custs != null && this.h261custs.contains(ord.getCompany()))) {
		
		//CASHi juhtumid
		
	//	Correct is -for Liepaja - standard order document (NDDDL), cash order document (NDDDSL) and warehouse (81735)
		//For Daugavpils - standard order document (NDDDD), cash order document (NDDDSD) and warehouse (81734)
		if(ware.equals("53268") || ware.equals("22013")){ // n01 riga 
			
			if(num == 20) { 
				return "NDDDS";
			}
			return "NDM";
		}
		if(ware.equals("53269") || ware.equals("46801")){ // n02 daugavpils 
			if(num == 20) { 
				return "NDDDSD";
			}
			
			return "NDMD2";
		}
		if(ware.equals("53270") || ware.equals("45112")){ // n03 liepaja
			if(num == 20) { 
				return "NDDDSL";
			}
			return "NDM1";
		}
	}

		if(num == 10){ // J
			return "NDM2";
		}
		
		if(num == 9 ){ // G
			return "NDM33";
		}
	
		if(num == 12 ){ 
			res = "NDDD";
		}
	
	if(ware.equals("53268") || ware.equals("22013")){ // n01 riga 
		if(num == 8){ // p
			res = "NPARK2K";
		}
		if(num == 1){
			res = "NPARK2K";
		}
		if(num == 7){ // B
			res = "NPARKS";
		}
		if(num == 2){
			res = "NPARKMM"; //;"MEDR";
		}
		if(num == 3){
			res = "NPARK2K";
		}
		if(num == 4){
			res = "NPARH";
		}
		if(num == 5){
			res = "NPARK2K";
		}
		if(num == 6){
			res = "NPARK2K";
		}
		
		if(num == 11 ){ 
			res = "NPARKSO";
		}
		if(num == 12 ){ 
			res = "NDDD";
		}
		if(num == 13 ){ 
			res = "NPAVR";
				}
		if(num == 20 ){ 
			res = "NDDD";
				}
	}
//	 53268	RĪGAS NOLIKTAVA (Galvena)	N01
//		53269	Daugavpils noliktava	N02
//		53270	Liepājas noliktava	N03

	
	if(ware.equals("53269") || ware.equals("46801") ){ // n02 daugavpils 
		if(num == 8){ // p
			res = "NPAD";
		}
		if(num == 7){ // B
			res = "NPAD2";
		}
		if(num == 1){
			res = "NPAD";
		}
		if(num == 2){
			res = "NPADMM";//"MEDD";
		}
		if(num == 3){
			res = "NPAD";
		}
		if(num == 4){
			res = "NPAD3";
		}
		if(num == 5){
			res = "NPAD";
		}
		if(num == 6){
			res = "NPAD";
		}
		
		if(num == 11){ 
			res = "NPAD2O";
		}
		if(num == 12 ){ 
			res = "NDDDD";
		}
		if(num == 13 ){ 
			res = "NPAVD";
				}
		
		if(num == 20 ){ 
			res = "NDDDD";
				}
	}
	
//	 53268	RĪGAS NOLIKTAVA (Galvena)	N01
//		53269	Daugavpils noliktava	N02
//		53270	Liepājas noliktava	N03


	if(ware.equals("53270") || ware.equals("45112")){ // n03 liepaja
		
		if(num == 8){ // P
			res = "NPARE";
		}
		if(num == 7){ // B
			res = "NPARE2";
		}
		if(num == 1){
			res = "NPARE";
		}
		if(num == 2){
			res = "NPARMM"; //"NPARE3";
		}
		if(num == 3){
			res = "NPARE";
		}
		if(num == 4){
			res = "NPARE4";
		}
		if(num == 5){
			res = "NPARE";
		}
		if(num == 6){
			res = "NPARE";
		}
		
		if(num == 11 ){ 
			res = "NPARE2O";
		}
		if(num == 12 ){ 
			res = "NDDD";
		}
		if(num == 13 ){ 
			res = "NPAVL";
				}
		if(num == 20 ){ 
			res = "NDDDL";
				}
	}
	
	

	
	
	
	//System.out.println("ware and typs " + res + " " + num + " " + ware);

	return res ;
}
  
  
  
private String getTypeHRC(String ware , int num , Order ord ){
	String res = "";
	
//	 53268	RĪGAS NOLIKTAVA (Galvena)	N01
//		53269	Daugavpils noliktava	N02
//		53270	Liepājas noliktava	N03
//J - всегда склад N05 и тип документа NDM2G - всегда склад N04 и тип документа NDM3
	
	//System.out.println("typeHRC "+ ware + " "+ num + " " );
	
	
	if(ord.getPaymTerm() == -1) {
		
		//CASHi juhtumid
		
	//	Correct is -for Liepaja - standard order document (NDDDL), cash order document (NDDDSL) and warehouse (81735)
		//For Daugavpils - standard order document (NDDDD), cash order document (NDDDSD) and warehouse (81734)
		if(ware.equals("53268") || ware.equals("22013")){ // n01 riga 
			
			if(num == 20) { 
				return "NDDDS";
			}
			return "NDM";
		}
		if(ware.equals("53269") || ware.equals("46801")){ // n02 daugavpils 
			if(num == 20) { 
				return "NDDDSD";
			}
			
			return "NDMD2";
		}
		if(ware.equals("53270") || ware.equals("45112")){ // n03 liepaja
			if(num == 20) { 
				return "NDDDSL";
			}
			return "NDM1";
		}
	}
	

		if(num == 10){ // J
			return "NDM2";
		}
		
		if(num == 9 ){ // G
			return "NDM33";
		}
	
		
		if(num == 12 ){  // D
			res = "NDDD";
		}
	
	if(ware.equals("53268") || ware.equals("22013")){ // n01 riga 
		
		if(num == 1){
			res = "NPARK2KHRC";
		}
		if(num == 2){
			res = "NPARKMM";//"MEDR";
		}
		if(num == 4){
			res = "NPARH";
		}
		if(num == 5){
			res = "NPARKS";
		}
		

			if(num == 11 ){ 
				res = "NPARKSO";
					}
			if(num == 13 ){ 
				res = "NPAVR";
					}
			
			if(num == 20 ){  // D
				res = "NDDD";
			}
		
	}
//	 53268	RĪGAS NOLIKTAVA (Galvena)	N01
//		53269	Daugavpils noliktava	N02
//		53270	Liepājas noliktava	N03

	
	if(ware.equals("53269") || ware.equals("46801") ){ // n02 daugavpils 
		if(num == 1){
			res = "NPADHRC";
		}
		
		if(num == 2){
			res = "MEDD";
		}

		if(num == 4){
			res = "NPAD3";
		}
		if(num == 5){
			res = "NPAD2";
		}

       if(num == 11){ 
			res = "NPAD2O";
		}
		
       if(num == 13 ){ 
			res = "NPAVD";
				}
       
       if(num == 20 ){  // D
			res = "NDDDD";
		}
	}
	
//	 53268	RĪGAS NOLIKTAVA (Galvena)	N01
//		53269	Daugavpils noliktava	N02
//		53270	Liepājas noliktava	N03


	if(ware.equals("53270") || ware.equals("45112")){ // n03 liepaja
		
		if(num == 1){
			res = "NPAREHRC";
		}

		if(num == 2){
			res = "NPARE3";
		}
		
		if(num == 4){
			res = "NPARE4";
		}
		if(num == 5){
			res = "NPARE2";
		}
		if(num == 11 ){ 
			res = "NPARE2O";
		}
		
		if(num == 13 ){ 
			res = "NPAVL";
				}
		if(num == 20 ){  // D
			res = "NDDDL";
		}
	}
	
	//System.out.println("ware and typs " + res + " " + num + " " + ware);

	return res ;
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

  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  

  public String identifyCustomer(String id,String regnr) throws SQLException {
	  	String sql1 = null;
	  
	  //	 System.out.println("Kliendi Määramine: "  + id + " " + regnr);
	  	
	  	    sql1 = " select PK_KLIENTS as kood from APV04761.NADRESES where LIC_NR= '" + id + "'";	
	  	   Connection con = getNewConnection();
	 //   System.out.println(sql1);
	    String acc = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      acc = rs1.getString("kood").trim() ;
	    }
	    rs1.close();
	    st1.close();
	    
	    if(acc == null){
	    	 sql1 ="  select PK_KLIENTS as kood  from APV04761.DKLIENTS k where  PAP_KODS= '" + id + "'";	
		 //     System.out.println(sql1);
		  
		     st1 = con.createStatement();
		     rs1 = st1.executeQuery(sql1);

		    while (rs1.next()) {
		    //  acc = rs1.getString("kood").trim() ;
		    }
	    }
	    

        con.close();
	    
	  //  System.out.println("Klient: " + acc + " " + id);
	    return acc;

	  }

  
  public HashSet lvNotCustomer() throws SQLException {
	  	String sql1 = null;
	  
	  //	 System.out.println("Kliendi Määramine: "  + id + " " + regnr);
	  	
	  	    sql1 = "  select PK_KLIENTS as kood  from APV04761.DKLIENTS k where  k.pk_valsts != 51 ";	
	  	   Connection con = getNewConnection();
	 //   System.out.println(sql1);
	  	 HashSet res = new HashSet();
	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      res.add( rs1.getString("kood").trim()) ;
	    }
	    rs1.close();
	    st1.close();
	
	    

      con.close();
	    
	  //  System.out.println("Klient: " + acc + " " + id);
	    return res;

	  }

  
  public String identifyProduct(String ean, String prodID) throws SQLException{
	  	
	    String prod = null;
	
	  
	  
  String sql2 = " select t.PK_NOM, t.KODS as  toode,t.NOSAUK  AS nimetus, bar.BAR_KODS AS ean  " +
	" from APV04761.NNOM  t inner JOIN APV04761.NBARKODS bar ON bar.PK_NOM=t.PK_NOM AND bar.IS_MAIN = 1 " +
	" where bar.BAR_KODS = '" + ean + "'";
   
 Connection con =  getNewConnection();
  Statement st1 = con.createStatement();
  ResultSet rs1 = st1.executeQuery(sql2);

  while (rs1.next()) {
    prod = rs1.getString("toode").trim();
  }
 

  //System.out.println(prodID + " prod1 " + prod + " " + ean );
 
  if(prod == null){
	  sql2 = " select t.PK_NOM, t.KODS as  toode  " +
				" from APV04761.NNOM t " +
				" where t.KODS = '" + prodID + "'";
	  rs1 = st1.executeQuery(sql2);

	  while (rs1.next()) {
	    prod = rs1.getString("toode").trim();
	  }
  }
  
  
  rs1.close();
  st1.close();
  con.close();

  return prod;
}


	
  private ArrayList<String> hrccusts = null; 
  private HashSet<String> h261custs = null;
  private HashSet<String> blkcusts = null; 
  
	private TreeMap delDays = null;	 
	
	private HashSet nlvKlients = null;
  
  public void makeOrder(Order order) throws SQLException{
       counter = 0;
       
       if(delDays == null) {
    	 //  delDays =  makeCustomerNextDeliveryData();
       }
      
       propsVEGE = new Properties();
     		try {
     			propsVEGE = EksportFrame.loadProps("vegeaddress.txt");
     		} catch (Exception e) {
     			e.printStackTrace();
     		}
       
     		
     		
     		//System.out.println("Vege Address size " + propsVEGE.size());
       if (signatures == null){
    	   signatures = new Vector();
       }
       		  String signature = order.getSignature();
    			  if(signatures.contains(signature)){
    				  this.trouble = "REPRETEATING ORDER agent :" + order.getAgent() + " customer :" 
    			  + Converter.deCode(order.getCompany()) ;
    			  }else{
    				  this.trouble = null;
    				  
    				  if( hrccusts == null){
    					  hrccusts =  makeHRC(); 
    				  }
    	
    				  if( blkcusts == null){
    					  blkcusts =  makeBLK(); 
    					  
    				  }

    				  if(blkcusts.contains(order.getCompany())) {
    					  order.setDokNr("BLK-" + order.getDokNr());
    				  }
    				  if( h261custs == null){
    					  h261custs =  make261(); 
    				  }
    				  
    				  boolean hrccustomer = false;
    				  
    				  if(hrccusts.contains(order.getCompany())){
    					  hrccustomer = true;
    				  }
    				  
    				  if(nlvKlients == null) {
    					  nlvKlients = lvNotCustomer();
    				  }
    				  
    				  System.out.println(order.getCompany() + " " + nlvKlients.contains(order.getCompany()));
    				if(nlvKlients.contains(order.getCompany())) {
    					order.setCurrency("NPARK2");
    					
    					 //makeOutput(order,order,hrccustomer);  
    				
					        makeOutput(order,order,hrccustomer);
   			    	  
    				}else {
    				
    				  
    				  if(!hrccustomer){
    					  System.out.println("OffTrade" + order.getStatus());
    					  if(order.getStatus() == 34) {
    						  order.setCurrency("NPAIK9");
    						  order.setWarehouse("53268");
    						  
    						  makeOutput(order,order, hrccustomer); 
    					  }else {
    					  Vector splitted =  splitOrder(order);
    			    	  Enumeration en = splitted.elements();
    					  
    					  while(en.hasMoreElements()){
    						  Order ord = (Order)en.nextElement();
    	    				 makeOutput(ord,order, hrccustomer);  
    	    				  
    	    				/*  Vector splitted2 =  this.splitOrderByAm(ord, 38);
        			    	  Enumeration en2 = splitted2.elements();
        			    	  while(en2.hasMoreElements()){
        			    		  
        						  Order ord2 = (Order)en2.nextElement();
        						  
        						  
    					        makeOutput(ord2,order,hrccustomer);
        			    	  }*/
    	        		  }
    					  }
    					  
    				  }else{
    					  System.out.println("OnTrade" + order.getStatus());
    					  
    					  if(order.getStatus() == 34) {
    						  order.setCurrency("NPAIK9");
    						  order.setWarehouse("53268");
    						  
    						  makeOutput(order,order, hrccustomer); 
    					  }else {
    					  Vector splitted =  splitOrderHRC(order);
    			    	  Enumeration en = splitted.elements();
    			    	  while(en.hasMoreElements()){
    			    		  
    						  Order ord = (Order)en.nextElement();
    						  makeOutput(ord,order,hrccustomer);
    						 
    					  }}
    				  }
    				}
    					signatures.add(signature);
        		  }
    	
       try {
		saveObject(signatures, "signatures.vec");
	} catch (IOException e) {
		e.printStackTrace();
	}
       setNumAddedLines(counter); 
	    
       
  }
  

 
  public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
   
  
  private String weburl = "http://konigsql:3128/rest/TNdmPvzSale/template/303";
  
  private String getDateNiceString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    String year =   cal.get(Calendar.YEAR) + "";
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
	 
	   sb.append(  year + "-");
	    if (month.length() == 1){
		     sb.append("0");
		   }
		     sb.append(month  + "-");
	    
	    if (day.length() == 1){
	      sb.append("0");
	    }
	     sb.append(day);
	  //28.06.2010 20:00:33Y-m-d\TH:i:s n2002-05-30T09:00:00
	   
	     if (hour.length() == 1){
		      hour = "0" + hour ;
		    }
	    /* sb.append("T" +hour + ":");

	     if (minute.length() == 1){
		      sb.append("0");
		    }
	     sb.append(minute + ":");
	     if (second.length() == 1){
		      sb.append("0");
		    }
	     sb.append(second);*/
	     
	    return sb.toString();
	  }
  
  
  private String getWeb(String req, String req2) throws ParseException, IOException{
	  HttpResponse response = null;
	  try{
		//  System.out.println("weburl " + weburl);
		  DefaultHttpClient httpClient = new DefaultHttpClient();
		//  Credentials defaultcreds = new UsernamePasswordCredentials("testa", "asdf"); // "evgeney", "5697110" 
		  Credentials defaultcreds = new UsernamePasswordCredentials("EXCELL", "5697110"); // "evgeney", "5697110" 
			 
		  httpClient.getCredentialsProvider().setCredentials(
                   AuthScope.ANY, defaultcreds);
		  HttpPost postRequest = new HttpPost(weburl + "TNdmPvzSale");
		  StringEntity input = new StringEntity(req);
		  input.setContentType("application/json");
		  postRequest.setEntity(input);
		  response = httpClient.execute(postRequest);	  
	  }catch(Exception e){
		  trouble = "NOT IMPORTED ORDER \n" + req2;
		  e.printStackTrace();
	  }
	  return  EntityUtils.toString(response.getEntity());
  }
	  
  
	
 private String url = "jdbc:odbc:horizon2";
 
 private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	  }

 
 public String getCustId(String kood) throws SQLException {

	    Connection con = getNewConnection();
	  	
	    String cmd = "SELECT K.NOSAUK AS nimi, K.PK_KLIENTS AS id, K.KODS AS kood FROM   APV04761.DKLIENTS K " +
	  			" WHERE K.KODS = '" +kood+"'" ;  

	  	Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(cmd);
	    String liik = null;
	    if (rs.next()) {
	      liik = rs.getString("id").trim();
	      }
	    rs.close();
	    st.close();
	    con.close();
	    return liik;
	}

 public String getProdId(String kood) throws SQLException {

	    Connection con = getNewConnection();
	    
	  	
	    String cmd = "SELECT PK_NOM AS id FROM   APV04761.NNOM t " +
	  			" WHERE t.KODS = '" +kood+"'" ;  

	  	Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(cmd);
	    String liik = null;
	    if (rs.next()) {
	      liik = rs.getString("id").trim();
	      }
	    rs.close();
	    st.close();
	    con.close();
	    return liik;
	}
  

 private void makeOutput(Order order, Order orig, boolean isHoreca){
	 
	 Hashtable pandikas =  new Hashtable();
	
	 TreeMap pantHinnad = null;
	
	 TreeMap	pants = getPandiPakend();
		//	System.out.println("pants " + pants.size());
	
		try {
			pantHinnad = getPandiPakendHind();
			//System.out.println("panthinnad " + pantHinnad.size());
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		

		  if( h261custs == null){
			  h261custs =  make261(); 
		  }
	
	 	try {
	 		
	 	
	 	
	 		String comm = "";
	 		if(order.getInfo() != null && order.getInfo().trim().length() >0){
	 			comm =  order.getInfo() ;
	 		}
	 		
	            
		           
		        String cukood =  Converter.deCode(order.getCompany());
		        String adid = null;
		        System.out.println("Comment " + comm + " " + cukood);
				
		        if(cukood.indexOf("_")!= -1){
		        	adid =  escape(cukood.substring(cukood.indexOf("_")+1));
		        	cukood = escape(cukood.substring(0,cukood.indexOf("_")));
			    }
		        
		        if(adid != null && adid.indexOf("_")!= -1){
		        	adid =  escape(adid.substring(0,adid.indexOf("_")));
			    }
		         
		        boolean tsekikas = false;
		   	 if(this.h261custs != null && this.h261custs.contains(cukood) ) {
		   		 tsekikas = true;
		   	 }
		   	 
		 		 Calendar  shipDate = order.getShipCal();
		 		 if(order.getStatus() < 5) {
		 			 
		 			 if( order.isShipDateUsed() ) {
		            	 shipDate = order.getShipCal();
		             } else {
		            	 shipDate = getNextShipCal(cukood);
		             }
		 			 
		 		  } else {
		 			  
		 			 if(hrccusts.contains(orig.getCompany())){
		 				shipDate = order.getShipCal();
		 			   } else {
		 				shipDate = getNextShipCal(cukood);
		 			 }
		 		
		 		 }
		 		
		 		 
		 		//   System.out.println("shipdate horeca: " + hrccusts.contains(orig.getCompany()) + " " + order.getStatus());
					
		 		 
		 		  if(shipDate == null) {
		 			 shipDate = order.getShipCal();
		 		  }
		        
		           String doknr = order.getDokNr();
				      String fn = doknr;
				      if(doknr.trim().length()== 0){
				    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
				      }
				  
				   
				   
				   String agent = order.getAgent();
				   if(agent.indexOf("SUP") != -1 ) {
					   agent = (String)this.defAgents.get(cukood);
				   }
				  
				   
				   
				   String ref = order.getTeostaja(); //mobiilse telli ref number
					if ( ref == null ){
						ref =  "";
					}
				 //  System.out.println("refnr " + ref);
					 String ref2 = ref;
					 if(isHoreca) {
						
						 
						 boolean hasFrozen = false;
						 Vector ll = order.getOrderLines();
						 for (int i = 0; i < ll.size(); i++) {
					    	  OrderLine line = (OrderLine) ll.elementAt(i);
					    	  String prodid = line.getToode();
					    	  if(prodid.startsWith("I") || prodid.startsWith("F")) {
					    		  hasFrozen = true;
					    		  ref2 = "+FF";
					    		  break;
					    	  }
						 }
						 
						 
						 if(hasFrozen) {
						 boolean isOnlyFrozen = true;
						 ll = order.getOrderLines();
						 for (int i = 0; i < ll.size(); i++) {
					    	  OrderLine line = (OrderLine) ll.elementAt(i);
					    	  String prodid = line.getToode();
					    	  if(!prodid.startsWith("I") && !prodid.startsWith("F")) {
					    		  isOnlyFrozen = false;
					    		  break;
					    	  }
					    	  
						 }
						 
						 if(isOnlyFrozen) {
							 ref2 = "FF"; 
						 } 
					 }
						 ll = order.getOrderLines();
						 for (int i = 0; i < ll.size(); i++) {
					    	  OrderLine line = (OrderLine) ll.elementAt(i);
					    	  String prodid = line.getToode();
					    	  if(prodid.startsWith("M") ) {
					    		  ref2 = ref2 + "M";
					    		  break;
					    	  }
					    	  
						 } 
					 }
					 
					 if(isHoreca) {
					 if(defDealers == null){
						  loadWares();
					  }
					 
					 
					 
					  if(order.getDealer() == null || order.getDealer().trim().length() == 0){
						  order.setDealer("53268");
					  }
					  


					 

						 System.out.println("dealer1 " + order.getDealer() + " " + defDealers.get(cukood) + " " + cukood);
					  
					  if(order.getStatus() > 6 && defDealers != null && defDealers.get(cukood) != null){
						  order.setWarehouse(""+defDealers.get(cukood));
						  order.setDealer(""+defDealers.get(cukood)+"");
					   }else if(order.getStatus() > 6){
						   order.setWarehouse("53268");
							 order.setDealer("53268");
							 this.trouble = " Nav noliktava in Horizon OUTPUT";
					   }
					 // System.out.println("dealer " + defDealers.size() + " " + aaa + " " + defDealers.get(aaa));
					 // order.setCurrency(getType(order.getDealer(),1,order));
					 }
				   
					 System.out.println("dealer2 " + order.getDealer());
					 
					 
					 String ladu = "53268";
				   
				   String dealer = order.getDealer();
				   if(dealer != null && dealer.trim().length() > 0){
					   ladu = dealer.trim();
					  
				   }
				   System.out.println("dealer2 " + ladu);
					 
		           
		           JSONObject resource  = new JSONObject();
		           JSONObject sisu  = new JSONObject();
		           resource.put("resource", sisu);
		           sisu.put("docType", order.getCurrency());
		           
		           JSONObject entity  = new JSONObject();
		           sisu.put("entity" , entity );
		           
		           JSONObject href1  = new JSONObject();
		           href1.put("href", "/rest/TNdmNol/" + ladu);
		           entity.put("PK_ESPATS", href1);
		           
		           JSONObject href2  = new JSONObject();
		           href2.put("href", "/rest/TDdmCustomer/" + cukood);
		           entity.put("PK_KLIENTS", href2);
		           
		           
if(tsekikas) {
	System.out.println("Tsekikas JAH");
		           JSONObject href3  = new JSONObject();
		           href3.put("href", "/rest/TNdmApmveids/1");
		           entity.put("PK_APMV", href3);
		           
}else {
	System.out.println("Tsekikas EIII");
}
		           
		           entity.put("DAT_DOK",getDateNiceString(shipDate)); // Calendar.getInstance()
		           entity.put("DOK_NR" , doknr );
		           entity.put("DOK_NR2", ref );
		           
		           if(agent != null) {
		        	   JSONObject href4  = new JSONObject();
			           href4.put("href", "/rest/TNdmPard/" + agent);
			           entity.put("PK_PARD", href4);  
		           }
		         
		          
		           entity.put("PAP_NR", ref2 );
		           
		           JSONObject href5  = new JSONObject();
		           href5.put("DAT_PIEG",getDateNiceString(shipDate) );
		           if(adid != null && !adid.equals("null")){
		        	   JSONObject href3  = new JSONObject();
			           href3.put("href", "/rest/TNdmAdr/" + adid);
			           href5.put("PK_ADRESE", href3);
			       }
		           
		           entity.put("NdmPvzAddition", href5);
		          
		           
		           JSONObject href6  = new JSONObject();
		           JSONArray arr1 =  new JSONArray();
		           JSONObject pamat  = new JSONObject();
		           pamat.put("PAMAT", comm);
		           arr1.put(pamat);
		           href6.put("row",arr1 );
		           entity.put("qryPamat",href6);
			         
	
		           
		           JSONObject linex  = new JSONObject(); 
		           entity.put("tblRindas",linex );
		           JSONArray row  = new JSONArray();
		           linex.put("row", row);
		           
		           


	     	 Vector lines = order.getOrderLines();
	     	 if(isHoreca) {
	     		// System.out.println("OLines 1 " + lines.toString());
	        	 java.util.Collections.sort(lines, new java.util.Comparator(){
				        public boolean equals(Object obj) {
					       return false;
					     }
					     public int compare(Object o1, Object o2) {
					       OrderLine ol1 = (OrderLine)o1;
					       OrderLine ol2 = (OrderLine)o2;
					       return ol1.getToode().compareTo(ol2.getToode());
					     }
					   });
						        	 
						        	 
	        	// System.out.println("OLines 2 " + lines.toString());
	     	 }
		      for (int i = 0; i < lines.size(); i++) {
		    	  OrderLine line = (OrderLine) lines.elementAt(i);
		    	  JSONObject lin  = new JSONObject(); 
		    	  row.put(lin);
		    	  String prid = getProdId(line.getToode());
		    	  lin.put("RN_VEIDS", 0);
		    	  lin.put("PK_NOM", prid );
		    	  double kogus = line.getKogus();
		    	  if(kogus  > 100000){
		    		  kogus = 99999;
		    	  }
		    	  lin.put("DAUDZ", kogus);
		    	  lin.put("V_KODS", line.getUnit());
		    	  //  <!--KODS>113</KODS-->
		          // <!--BAR_KODS>4751012090010</BAR_KODS-->
		         //   <TEKSTS>RindÄ PiezÄ«mes</TEKSTS>
		         //  <TEMPERAT>RindÄ raksturojums</TEMPERAT>
		    	  if(line.getPartii() != null && line.getPartii().trim().length() > 0){
		    		  lin.put("TEMPERAT", line.getPartii().trim());
		    	  }
		    	  if(order.getStatus() < 6){
		    	      if(line.getDiscount() > 0 || line.getDiscount() < -1){
				    	  lin.put("CENA", line.getPrice());
				    	  lin.put("ATLAIDE", line.getDiscount());
				    	  lin.put("TEMPERAT",  line.getDiscount() + " %");
	                 }
		          }
			      
		    	  if(pants != null) {
				    	 String pant = (String) pants.get(line.getToode())  ; 
					     
					     if(pant != null){
				           	  if(pandikas.get(pant) == null){
				           		  pandikas.put(pant,line.getKogus()+"");
				                 } else {
				                 	pandikas.put(pant,(new Double((String)pandikas.get(pant)).doubleValue()+line.getKogus())+"");
				                 }
				              } 
				     }
			      counter++;
			      }
		      
		      if( pandikas.size()>0){ 
		        	Enumeration enumpant = pandikas.keys();
		        	
		         while(enumpant.hasMoreElements()){
		         	String kood = (String)enumpant.nextElement();
		         	if(kood.trim().length()>0){
		         		 JSONObject lin  = new JSONObject(); 
				    	  row.put(lin);
				    	  String prid = (String)pantHinnad.get(kood+"nom");
				    	  lin.put("RN_VEIDS", 2);
				    	  lin.put("PK_NOM", prid );
				    	  double kogus = Double.parseDouble(pandikas.get(kood)+"");
				    	  double hind = Double.parseDouble(pantHinnad.get(kood)+"");
				    	  lin.put("DAUDZ", hind);
				    	  lin.put("V_KODS", "gab");
				    	  //  <!--KODS>113</KODS-->
				          // <!--BAR_KODS>4751012090010</BAR_KODS-->
				         //   <TEKSTS>RindÄ PiezÄ«mes</TEKSTS>
				         //  <TEMPERAT>RindÄ raksturojums</TEMPERAT>
				    	
				    	      
						    	  lin.put("CENA", kogus);
						    	 
			                 
				          }
		        	}    	
		         }
		        
	
	// 	System.out.println("output' " + resource.toString()); 
	 	
	 		
	 	try {
	 		if("true".equals(parent.getProps().getProperty("testing"))){
		 	//	System.out.println("Testing ");
		 	   String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + fn + "-" + order.getCurrency() +  ".json";
			     // System.out.println(fileName);
			       
			      Converter.saveFileInUTF8(fileName, resource.toString());

			 	}else{
	    	 	
	 		
		      String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + fn + "-" + order.getCurrency() +  ".json";
		     // System.out.println(fileName);
		       
		      Converter.saveFileInUTF8(fileName, resource.toString());

		      fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-"  + fn  + "-" + order.getCurrency()+  "-response.xml";
		     //System.out.println(fileName + "\n" +  resource.toString());
		     
		
		   //   trouble = "IMPORTED ORDER \n" + order_xml;
			 	
	 		String xml = getWeb(resource.toString() , "DokNr:" + order.getDokNr() + " Company:" + cukood);		
	 		//System.out.println("Tulemus " + xml.contains("error") +" "+ xml);
		     Converter.saveFileInUTF8(fileName, xml);
		     if(xml.contains("error")){
		    	//	System.out.println("Tulemus " + xml.contains("error") );
		    	 trouble = "NOT IMPORTED ORDER \n" + xml;
		     }
		     	
	      
		 	
	    	 	}
	    
	 	
	 	} catch (Exception e) {
	 		e.printStackTrace();
	 		
	 	  trouble = "NOT IMPORTED ORDER \n" + resource;
	 	//  System.exit(0);
	 	}
		      
	 	
		} catch (Exception e) {
			e.printStackTrace();

		 	  trouble = "NOT IMPORTED ORDER \n" + order.getDokNr();
		 //	  System.exit(0);
		}
		
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

 
  public String getDriverVersion(){
    return "HorizonKonig18Import";
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

  private String trouble = null;
  private int counter = 0;
  private ImportPanel parent;
  private boolean isAmount = false;

public String getTrouble() {
	return trouble;
}
  
}
