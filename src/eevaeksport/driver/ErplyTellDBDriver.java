package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;
import eevaeksport.util.FileListFilter;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ErplyTellDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public ErplyTellDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
   String url = getParent().getProps().getProperty("erply.url");
   ownGLN = getParent().getProps().getProperty("owngln");
   if(url == null){
	   url = "https://www.erply.net/api/";
   }
	try {
		myurl = new URL(url);
	} catch (MalformedURLException e) {
		e.printStackTrace();
	}
	
   username = getParent().getProps().getProperty("erply.user");	
   company = getParent().getProps().getProperty("erply.company");
   password = getParent().getProps().getProperty("erply.pass");
   apiversion = getParent().getProps().getProperty("erply.apiversion");
   if(apiversion == null){
	   apiversion = "1.0";
   }
   returntype = getParent().getProps().getProperty("erply.returntype");
   if(returntype == null){
	   returntype = "XML";
   }
    userstuff = "&version=" + apiversion + "&responseType=" + returntype + "&username=" + username 
      + "&password=" + password;
   
   session = getSessionId();
	System.out.println(session);
   while(session.indexOf("%") != -1){
	   session = getSessionId();
   }
    userstuff = "&clientCode=" + company +"&version=" + apiversion + "&responseType=" + returntype + "&sessionKey=" + session;
    System.out.println(session + userstuff ) ;
   
  }
    
  public String getSaveDebtInfo() {
   	return null;
   }
  public static String getYYYYpMMpDDpString(Date dd){
	    StringBuffer sb = new StringBuffer();
	    Calendar cal= Calendar.getInstance();
	    cal.setTime(dd);
	    
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}
		sb.append(day );
		
		sb.append("." + month  + ".");
		sb.append("" + cal.get(Calendar.YEAR));
		
	    return sb.toString();
	  }







  

  
  private String getSessionId(){

	  String data = "clientCode=" + company + "&request=verifyUser" + userstuff ;
	//  System.out.println(data);
	  
	  try {
	String res = (postData(data)).toString();
	System.out.println(res);
	session = res.substring(res.indexOf("sessionKey") + 11 ,res.indexOf("</sessionKey"));
	//System.out.println(session);
	} catch (Exception e) {
		e.printStackTrace();
	}
	 
	  return session;
  }
  
  
  private StringBuffer postData(String data) throws Exception {
  HttpURLConnection urlc = null;
  Converter.doTrustToCertificates();
  StringBuffer inputLineBuf = new StringBuffer();
  try{
  urlc = (HttpURLConnection) myurl.openConnection();
  try{
  urlc.setRequestMethod( "POST" );
  } catch (ProtocolException e) {
    throw new Exception("Shouldn't happen: HttpURLConnection doesn't support POST??", e);
  }
  urlc.setDoOutput(true);
  urlc.setDoInput(true);
  urlc.setUseCaches(false);
  urlc.setAllowUserInteraction(false);
  OutputStream out = urlc.getOutputStream();
  try {
	out.write( data.getBytes() );
  } catch (IOException e){
  throw new Exception("IOException while posting data", e);
  } finally {
  if (out != null)
  out.close();
  }
  InputStream in = urlc.getInputStream();
  try{
  Reader reader = new InputStreamReader(in);
    InputStreamReader isr = new InputStreamReader(in,"UTF-8");
    BufferedReader inr = new BufferedReader(isr);
    String inputLine;

    while ((inputLine = inr.readLine()) != null){
        inputLine = enCode(inputLine);	// enCode(inputLine);	
    	inputLineBuf.append(inputLine.replaceAll("/>","/>\n").replace((char)0x1c, ' '));
  }

     inr.close();
  
  
  reader.close();
  } catch (IOException e)
  {
  throw new Exception("IOException while reading response", e);
  } finally
  {
  if (in != null)
  in.close();
  }
  } catch (IOException e)
  {
  throw new Exception("Connection error (is server running at " + myurl + " ?): " + e);
  } finally
  {
  if (urlc != null)
  urlc.disconnect();
  }
  return inputLineBuf;
  }
  
  
  private StringBuffer getData(String data) throws Exception {
     	String httpsURL = myurl + "?" + data;
        URL myurl1 = new URL(httpsURL);
        HttpsURLConnection con = (HttpsURLConnection)myurl1.openConnection();
        InputStream ins = con.getInputStream();
        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
        BufferedReader in = new BufferedReader(isr);

        String inputLine;
        StringBuffer inputLineBuf = new StringBuffer();
        while ((inputLine = in.readLine()) != null){
        	inputLine = this.enCode(inputLine);
        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\"l\"","\"L\""));
       }
        return inputLineBuf;
	  }
	  
	  
  
  
   public String getPilotDayData(Agent agent){
   	return null;
   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
   public Vector getPriceMatrixData() {

   	Vector lines = new Vector();
   	/*
   	try {
    FileReader fr = new FileReader(getDir() + DATA_FILE);
    BufferedReader reader = new BufferedReader(fr);
  String line = null;

  while(!(line = reader.readLine()).equals("\"[Get DiscountMatrix]\"")){
  }
  line = reader.readLine();
    System.out.println("line" +line);
  while(((line = reader.readLine()).trim().length()) > 3 ){
  	
    line = line.replaceAll("\",\"", "|");
    line = line.replaceAll("\",", "|");
    line = line.replaceAll("\""," ");
 
    StringTokenizer st = new StringTokenizer(line, SEPARATOR);
    double price = 0;
	String custID = null;
	String clGroup = null;
	String prodID = null;
	String discGroup = null;
	double discount = 0;
		if(st.hasMoreTokens()){
			custID = cleardots(st.nextToken());
			
		}
				if(st.hasMoreTokens()){
		 clGroup = cleardots(st.nextToken());	
			}
			if(st.hasMoreTokens()){
				 prodID = cleardots(st.nextToken());	
			}
			if(st.hasMoreTokens()){
				discGroup = cleardots(st.nextToken());	
			}
			
			if(st.hasMoreTokens()){
				 discount = Double.parseDouble(cleardots(st.nextToken()).replaceAll(",","."));	
			}
			
		//	System.out.println("'" + custID + "'" + clGroup + "'" + prodID + "'" + discGroup + "'" + discount) ;
			if(st.hasMoreTokens()){
				price = Double.parseDouble(cleardots(st.nextToken()));	
			}
			MatrixLine mline = new MatrixLine();

			if (custID != null && custID.trim().length() > 0) {
				mline.setCustID(custID.trim());
			}
			if (mline.getCustID() == null && clGroup != null
					&& clGroup.trim().length() > 0) {
				mline.setCliGroup(clGroup.trim());
			}

			if (prodID != null && prodID.trim().length() > 0) {
				mline.setProdID(prodID.trim());
			}
			if (mline.getProdID() == null && discGroup != null
					&& discGroup.trim().length() > 0) {
				mline.setDiscGroup(discGroup);
			}
			mline.setDiscount(discount);

			if (price != 0) {
				mline.setPrice(price);
			}
			lines.add(mline);
		}

	  

	} catch (Exception ex) {
		ex.printStackTrace();
	}*/
	return lines;

   }

   private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection("jdbc:odbc:report", "mobile", "mobile");
	  }
   
   
   
   private TreeMap getGrupid(){
	      File fff = new File(this.getExcelDir() + "Sortiment.xls" );
	      TreeMap groups = new TreeMap(); 
	  	try{
	          FileInputStream ff = new FileInputStream(fff);
	  		HSSFWorkbook workbook = new HSSFWorkbook((ff));
	  		HSSFSheet sheet = workbook.getSheetAt(1);
	  		HSSFRow row = null;
	  		
	  		row = sheet.getRow(0);
	  	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
	  		
	  		//System.out.println("rownum " + row.getLastCellNum());
	  		
	  		
	  		for(int a = 1 ; a < sheet.getLastRowNum();a++){
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
		    	    			  	 String sort = null;
		    	    			  	try{
		    	    			  		sort = row.getCell((short)1).getStringCellValue();  
			    	    			  	 }catch (Exception e){
			    	    			  		sort = row.getCell((short)1).getNumericCellValue()+"";  
			    	    	    		} 
			    	    			  	 if(sort.indexOf(".")!= -1){
			    	    			  	 	 sort = (sort.substring(0,kood.indexOf(".")));	
			    	    			  	 }
			    	    			  	 sort = sort.replace(',', '|').replace(',', '|').replace(',', '|').replace(',', '|');
		    	    			  	 groups.put(kood,sort);
	  	    			  
	  	    				
	  				}	   
	  				}
	 			}catch (Exception e){
	  			//	System.out.println(row.getCell((short)a).getNumericCellValue());
	  			}
	  		}
	  		
	  		
	  
	  	 }catch (Exception e){
	  		e.printStackTrace();
	  	}
	  	 
	  	 System.out.println("grupid suurus " + groups.size() );
	      
	     return groups; 
	  	 
	  }

   
   
   public void sendDistributionReport(Calendar ccc){ //parameeter on selleks et kui on vaja saata mingi muu p�eva aruannet
	  	  
	  	  boolean oosmarge = true;
	  	  if("true".equals(getParent().getProps().getProperty("distribution.insmarking"))){
	  		oosmarge = false; 
	  	  }
	  	  
	  	  
	  	 //=10379733,10379733
	  	  //distribution.email.10379733=aivar@mobilejava.ee
	  	  
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
	             "		and day(sortdate) =  day (getdate()" + add + ")  group by sortgroup";
	  	   
	  	   String sql2 = "select sortgroup ,prodid  from mj_assortment " +
	  	   	"	where year(sortdate) =  year (getdate()" + add + " " +
	        "  ) and month(sortdate) =  month (getdate()" + add + ") " + 
			"  and day(sortdate) =  day (getdate()" + add + ")  group by sortgroup,prodid";  // t�nase sortimendi leidmine
	  	 
	  	   
	  	   HashMap sortLen = new HashMap();
	  	   HashMap sortIse = new HashMap();
	  	   HashMap tnimed = new HashMap();
	  	   
	  	   try{
	  		   Connection con1 = this.getNewConnection();
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
	      			   pr.setName(prodid + " nimipuudub");
	      		   }
	      		   String ean = pr.getEAN();
	      		   if(pr.getEAN() == null)ean = "";
	      		   String prname = pr.getName();
	      		   if(prname == null) prname = "Tundmatu toode";
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
	     	" rtrim(sup.agentid) as agid, rtrim(sup.agentname) as agent ,sup.resstr3 as time ,resint3 as oosins  " +
	      " from supervisor sup  " +
	      " where   sup.sentdate > getdate()-7 and resint3 = 888 " +// mis on myygil sup.resstr3 >'" + ccw.getTimeInMillis() + "'" + //
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
	          // System.out.println("kliendid " + kliendid);

	while(rs.next()){
		   Vector sort = new Vector();
		   String prodid = rs.getString("prodid").trim();
		
		   String custid = rs.getString("custid").trim();
		   String sortim = (String)sortimendid.get(custid);
		   String custname = Converter.deCode(rs.getString("custname").trim());
		   String date = rs.getString("date").trim().substring(0,10);
		   String agid =  rs.getString("agid").trim();
		   String agent = Converter.deCode(rs.getString("agent").trim());
		   String time = rs.getString("time");
		  // System.out.println("ahaa " + custid );
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
			  
				 Vector res = (Vector)sortIse.get(sortim);
				 if(oosmarge){
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
				 }else{
					 
					   if(puuduvad.get(custid + date) == null){
						   counter++;
		              Vector puud = (Vector)res.clone();
		           //   System.out.println("Kloonin " + puud.size() + " " + custid );
		                   puud.removeElement(prodid);
						   puuduvad.put(custid + date,puud);    
					   }else{
						   Vector puud = (Vector) puuduvad.get(custid + date);
				           puud.removeElement(prodid);
						   puuduvad.put(custid + date,puud);   
					   }
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
		sb.append("Klient :\t" + names.get(custid) +  "\n");
		sb.append("Kontrollis:\t" + agen.get(custid) +  "\n");
		
		sbf.append("Klient :," + names.get(custid) +  "\n");
		sbf.append("Kontrollis:," + agen.get(custid) +  "\n");
		
		
		Enumeration ed =  ((Vector)visitedcoun.get(custid)).elements();
		while(ed.hasMoreElements()){
			total++;
			String date = (String)ed.nextElement();
			sb.append("Aeg :\t\t" + times.get(custid + date).toString().substring(0,19) +  "\n");	
			sbf.append("Aeg :," + times.get(custid + date).toString().substring(0,19) +  "\n");	
		

			double sss = Double.parseDouble((String)sortLen.get(sortcode));
			
			double mmm = Double.parseDouble(((Vector)visited.get(custid+date)).size()+ "");
			
		//	System.out.println("OOS " + sss + " " + mmm);
			if(oosmarge){
				mmm = sss - mmm;
							
			}
		  // kui m�rgivad OOS siis on myygil vahe .
			       
			double nnn = mmm/sss*100d;	
			int per = (int)(nnn);
			perc = perc + nnn;	
			sb.append("Sortimendis " + (int)sss + " / M��gil " + (int)mmm + " / " + per + "% \n\n");
			sbf.append("Sortimendis, M��gil ,% \n");
			sbf.append((int)sss + "," + (int)mmm + "," + per + "% \n\n\n\n");
		}

		sb.append("\n\n");

	}


	//Sortimendi v�rdlus  puuduolevad tooted ja v�ljaspool olevad tooted


	StringBuffer lisa = new StringBuffer();
	TreeMap tootedPoed = new TreeMap();
	StringBuffer lisaf = new StringBuffer();
	//System.out.println("puuduvad " + puuduvad.keySet());
	Iterator itera = puuduvad.keySet().iterator();
	boolean first = true ;
	while (itera.hasNext()){
		String id = (String)itera.next();
		System.out.println(id);
		String paev = id.substring(id.length() - 10);
	   String custid = id.substring(0,id.length() - 10);
	   System.out.println(custid + "ee" + paev);
	   if(first){
			lisa.append("Lisainfo Puuduste kohta\n");
			lisaf.append("Lisainfo Puuduste kohta\n");
			first = false;
		}
		Vector vvv = (Vector)puuduvad.get(id);
		lisa.append(names.get(custid) + " " + paev + "\nPUUDU: " + vvv.size() + " toodet\n");
		lisaf.append(names.get(custid) + "," + paev + "\nPUUDU:," + vvv.size() + "\n");
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
		toodePood.append("puudub " + vvv.size() + " poes\n");
		
	 Enumeration enumi = 	vvv.elements();
	 while (enumi.hasMoreElements()){
		 toodePood.append("\t"  + enumi.nextElement() + "\n");
	 }
	 toodePood.append("\n\n");
	}


	if(total == 0 ){
		total = 1;
	}
	String text = "Kontrollperiood : " + days.toString() + "\nKontrollimise kordade arv  : " + counter 
	+"\nKeskmiselt Kauplustes Saadavus " + (int)perc/total + "%\n\n";
	String textf = "Kontrollperiood : " + days.toString() + "\nKontrollimise kordade arv  : " + counter 
	+"\nKeskmiselt Kauplustes Saadavus " + (int)perc/total + "%\n\n";

	System.out.println(text + "\n" + sb.toString() + lisa.toString()+ "\n" + toodePood );


	Calendar ctc = Calendar.getInstance();

	String filename = "dreports/" +"DR-Erply-" + getYYYYpMMpDDpTimeNiceString(ctc.getTime()).substring(0, 10) + ".csv";  

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
	email.setSubjectFilesTo("Distributsiooni aruanne - Ltt AS klient " + regno);
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

  
   private HashMap prodHash = new HashMap();
   
	    private String getYYYYpMMpDDpTimeNiceString(Date dd){
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


   private Connection con2 = null;
   
   Hashtable prodCodes = null;
   
  
  
   
   
   public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		File f = new File(this.getExcelDir() + "Sortiment.xls");
		
		 if( prodCodes == null){
			 this.getProducts(null);
		 }
		
		  boolean insertassort = true;
         
         try {
       	  con2 = this.getNewConnection();  
      	   String sql = "SELECT sortdate from mj_assortment where  year(sortdate) = year(getdate()) and" +
      	   		" month(sortdate) = month(getdate()) and day(sortdate) = day(getdate()) ";
       	 
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
		
	//	System.out.println("asoortment juurde " + insertassort);
		
		
		try {
			
			
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheetAt(0);
			HSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(1);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 3; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			// System.out.println(groups.toString());

			for (int i = 2; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";

				if (row.getCell((short) 1) != null) {

					try {
						
						prodID = (row.getCell((short) 0).getStringCellValue());
					} catch (Exception e) {
						prodID = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					}
					
                   if(!prodID.equals("")){
                	
					// Siin l�heme reasisse rokkima ja vaatama kas se toode
					// l�heb kaasa
					for (int a = 3; a < row.getLastCellNum(); a++) {

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

							if (go != null && go.trim().toLowerCase().equals("1")) {
                                  if(a > 10){
                                 //	 System.out.println( a + " " + prodID + " " +
             						//		 clGroup + " " + go); 
                                  }
								 

								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(clGroup.trim());
								}

								if (prodID != null
										&& prodID.trim().length() > 0) {
									if(prodCodes != null){
										String prodX = (String)prodCodes.get(prodID.trim());
										if(prodX != null){
											mline.setProdID(prodX.trim());		
										}else{
									//		System.out.println("mingi jama toodet pole " + prodID );
										}
										
										
									}
								}
								
								if (mline.getProdID() == null
										&& prodGroup != null
										&& prodGroup.trim().length() > 0) {
									mline.setProdGroup(prodGroup);
								}
								  if(insertassort){
									  try{
										  insertSortLine(mline); 
									  }catch(Exception s){
										  
									  }
								         
		                           }
								result.add(mline);
							}
						}
					}
					   
                   }
				}
			}
			if(con2 != null)
			con2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.exit(0);
   System.out.println("fixed size " + result.size());
		return result;

	}

   
   
   public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr ){
	     String sql = "INSERT into dbo.supervisor values ("
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
	          this.connectionUp();
	          con2 = this.getNewConnection(); 
	          
	          PreparedStatement ps = con2.prepareStatement(sql);

	           
	           for (Iterator it = wr.getInfo().getLines().iterator(); it.hasNext(); ) {
	        	   eevaeksport.report.SuperVisorLineWrapperMeri lw = new  eevaeksport.report.SuperVisorLineWrapperMeri(it.next());
	             ps.setString(1, wr.getInfo().getAgentID());
	             String agName =  wr.getInfo().getAgentName();
	             if(agName == null){
	            	 agName = (String)this.agents.get(wr.getInfo().getAgentID());
	             }
	             ps.setString(2, agName);
	             ps.setString(3, lw.getLine().getCustID());
	             ps.setString(4, lw.getLine().getCustName());
	             ps.setString(5, lw.getLine().getProdID());
	             ps.setString(6, lw.getLine().getProdName());
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
	             String cc = lw.getLine().getCustComment();
	             if (cc == null){
	             	cc = " ";
	             }
	             ps.setString(24, cc);

	             String cd = lw.getLine().getComment();
	             if (cd == null){
	             	cd = " ";
	             }
	             ps.setString(25, cd);

	             String ce = lw.getLine().getCustAgentId();
	             if (ce == null){
	             	ce = " ";
	             }

	             ps.setString(26, ce);

	             String cf = lw.getLine().getCustAgentName();
	             if (cf == null){
	             	cf = " ";
	             }
	             ps.setString(27, cf);
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
	             ps.setString(31, cr2);


	             String cr3 = lw.getLine().getResStr3();
	             if (cr3 == null){
	             	cr3 = " ";
	             }
	             ps.setString(32, cr3);
	             ps.setInt(33, lw.getLine().getResInt1());
	             ps.setInt(34, lw.getLine().getResInt2());
	             ps.setInt(35, lw.getLine().getResInt3());
	             ps.executeUpdate();
	             System.out.println("lopp real");
	           }

	           ps.close();
	           con2.close();
	       } catch (SQLException e) {
	           e.printStackTrace();
	       }
	       this.connectionDown();
	     }


   
   
   private void  insertSortLine(MatrixLine mline){
		  try{
				
			      String sql = "INSERT INTO mj_assortment values (" +
			         "'" + mline.getCustID() + "'," + 
			         "getdate()," + 
			         "'" + mline.getCliGroup() + "'," + 
			         "'" + mline.getProdID() + "'," + 
			         "0," + 
			         "0,'')" ;
			  
			  	        
				     Statement stmt2 = con2.createStatement();
				     stmt2.executeUpdate(sql);
				     
				    // System.out.println(sql);
		  }catch(Exception e){
			 // e.printStackTrace();
		  }		  
	  }

   
   
   public Vector getFixedAssortmentData1() {
       Vector lines = new Vector();
       
       try {
   		  File f = new File (getDir() + "hinnakiri.xml");
   	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
   	     DocumentBuilder db;
   		db = dbf.newDocumentBuilder();      	
       	FileInputStream in = getCleanInputStream(f);
       	String handler = null;
           Document doc = db.parse(in,handler);
           Node root = getNode(doc, "transport");      
      
       	Node read = getNode(root, "priceformulas");
           for (int i = 0; i < read.getChildNodes().getLength(); i++){
           	  Node rida = read.getChildNodes().item(i);
           	  try{
           		  if(rida.getNodeName().equals("priceformula")){
           			 String custID = " ";
                     String clGroup = " ";
                     String prodID = " ";
                     String prodGroup = " ";
                     MatrixLine mline = new MatrixLine();
                    try{
                    	 clGroup = rida.getAttributes().getNamedItem("code").getNodeValue(); 
                         prodID =  rida.getAttributes().getNamedItem("item").getNodeValue(); 
                         if (custID != null && custID.trim().length() > 0) {
                           mline.setCustID(custID.trim());
                         }
                         if (clGroup != null && clGroup.trim().length() > 0) {
                           mline.setCliGroup(clGroup.trim());
                         }

                         if (prodID != null && prodID.trim().length() > 0) {
                           mline.setProdID(prodID.trim());
                         }
                         
                         if (mline.getProdID() == null && prodGroup != null
                             && prodGroup.trim().length() > 0) {
                           mline.setProdGroup(prodGroup);
                         }

                        
                        	 
                        	if(!prodsInUse.contains(prodID.trim())){
                           	  this.prodsInUse.add(prodID.trim());	
                        	}
                        	 
                            lines.add(mline);	
                          
                    
                       
                    }catch(Exception e){
                    }
                    
                   }
                	
           	  }catch(Exception e){
           		  e.printStackTrace();
           	  }
                
           }
           
   	}catch(Exception e){
   		e.printStackTrace();
   	}
  //     System.out.println(prodsInUse.size() );
       
       return lines;
 }
   
   private Vector sortLines(Vector inVec,TreeMap products){
	        Vector vec = new Vector();
	       // System.out.println("Prod22 " + products.size());
	        for (int i = 0 ; i < inVec.size() ; i++){
	          vec.addElement(inVec.elementAt(i));
	        }
	        Vector sorted = new Vector(vec.size());
	        while(vec.size() > 0){
	          MatrixLine smallest = null;
	          Product smProd = null;
	          for (Enumeration enumr = vec.elements(); enumr.hasMoreElements() ; ){
	        	  MatrixLine ol = (MatrixLine)enumr.nextElement();
	            Product prod = (Product)products.get(ol.getProdID());
           //   System.out.println("Prod " + prod + "  " + ol.getProdID());
	            if ( (smallest == null && smProd == null )
	            		|| ( prod.getName().compareTo(smProd.getName()) < 0 )) {
	              smallest = ol;
	              smProd = (Product)products.get(smallest.getProdID());
	            }
	          }
	          vec.removeElement(smallest);
	          sorted.addElement(smallest);
	        }
	        return sorted;
	      }
	


   public String getProductSortingData(){
   	return null;
   }
   
   public DealerProduct getDealerProductsList() {
 return null;
   }


 
       public String getSaveStockInfo(){
    	 	sendDistributionReport(null);
    	   	System.exit(0);
    	   	return null;
       }


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

   public TreeMap getProdGroups()  {
     TreeMap   tm = new TreeMap();
     groupList = new TreeMap();
  	try {
  		  File f = new File (getDir() + "tootegrupiderp.xml");
  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  	     DocumentBuilder db;
  		db = dbf.newDocumentBuilder();      	
      	FileInputStream in = getCleanInputStream(f);
      	String handler = null;
          Document doc = db.parse(in,handler);
      	
        Node root = getNode(doc, "document");      
         // System.out.println(root);
       	Node read = getNode(root, "records");
      // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
           for (int i = 0; i < read.getChildNodes().getLength(); i++){
           	  Node rida = read.getChildNodes().item(i);
           	  if(rida.getNodeName().equals("item")){
          	  try{
          		 
               		 String group  = getNode(rida,"productGroupID").getFirstChild().getNodeValue();
               		 String name  =  getNode(rida,"name").getFirstChild().getNodeValue();
               		 group = deCode(group);
               		  name  =  deCode(name);
                        tm.put(group,Converter.enCodeEst(name));  
                        groupList.put(group, group);
                    	Node alamad = getNode(rida, "subGroups");
                    	if(alamad != null){
                            for (int u = 0; u < alamad.getChildNodes().getLength(); u++){
                           	  Node alam = alamad.getChildNodes().item(u);
                           	 if(alam.getNodeName().equals("item")){
                           		 String agroup  = getNode(alam,"productGroupID").getFirstChild().getNodeValue();
                           		  String aname  =  getNode(alam,"name").getFirstChild().getNodeValue();
                           		 agroup = deCode(agroup);
                           		  aname  =  deCode(aname);
                                    tm.put(group + "." + agroup, Converter.enCodeEst(aname)); 
                                    groupList.put(agroup, group + "." + agroup);
                                    
                                    
                                    Node alamad2 = getNode(alam, "subGroups");
                                //     	System.out.println("ridu2  " + read.getChildNodes().getLength() + read.toString());
                                	if(alamad2 != null){
                                        for (int w = 0; w < alamad2.getChildNodes().getLength(); w++){
                                       	  Node alam2 = alamad2.getChildNodes().item(w);
                                       	 if(alam2.getNodeName().equals("item")){
                                       		String a2group  = getNode(alam2,"productGroupID").getFirstChild().getNodeValue();
                                     		  String a2name  =  getNode(alam2,"name").getFirstChild().getNodeValue();
                                     		 a2group = deCode(a2group);
                                     		  a2name  =  deCode(a2name);
                                              tm.put(group + "." + agroup + "." + a2group, Converter.enCodeEst(a2name)); 
                                              groupList.put(a2group, group + "." + agroup + "." + a2group);
                                              
                                              Node alamad3 = getNode(alamad2, "subGroups");
                                        //   	System.out.println("ridu2  " + read.getChildNodes().getLength() + read.toString());
                                      	if(alamad3 != null){
                                              for (int y = 0; y < alamad3.getChildNodes().getLength(); y++){
                                             	  Node alam3 = alamad3.getChildNodes().item(y);
                                             	 if(alam3.getNodeName().equals("item")){
                                             		String a3group  = getNode(alam3,"productGroupID").getFirstChild().getNodeValue();
                                           		  String a3name  =  getNode(alam3,"name").getFirstChild().getNodeValue();
                                           		 a3group = deCode(a3group);
                                           		  a3name  =  deCode(a3name);
                                                    tm.put(group + "." + agroup + "." + a2group + "." + a3group, Converter.enCodeEst(a3name)); 
                                                    groupList.put(a3group, group + "." + agroup + "." + a2group +"." + a3group);
                                                  } 
                                                } 
                                        	  }
                                            } 
                                      	 } 
                                  	 }
                                 }
                            }	
                    	}
                             	
                  
          	  }catch(Exception e){
          		  e.printStackTrace();
          		 // System.exit(0);
          	  }
           	  }     
          }
          
  	}catch(Exception e){
  		e.printStackTrace();
  	}
  
  //	System.out.println( "grlist " + groupList.toString());
 //	System.out.println("tm " + tm.toString());
         return tm;
   }

   public TreeMap getProdGroupsSecond() throws SQLException {
      return null;
        }



   private String enCode(String res ){
	  	 

	  	res =res.replaceAll("�","11111xx");
	  	res =res.replaceAll("�","11112xx");
		res =res.replaceAll("�","11114xx");
		res =res.replaceAll("�","11114xx");
		res =res.replaceAll("�","11113xx");
		res =res.replaceAll("�","e");
		res =res.replaceAll("�"," "); 
		
		res =res.replaceAll("�"," ");
		res =res.replaceAll("�"," ");
		res = res.replaceAll("�"," ");
		res = res.replaceAll("�", " ");
		res =res.replaceAll("�"," ");
		res =res.replaceAll("�"," ");
		res =res.replaceAll("�"," ");
		res =res.replaceAll("�","e");
		res =res.replaceAll("�","E");
		res =res.replaceAll("�"," ");
		

		res =res.replaceAll("�"," ");
		res =res.replaceAll("�"," ");
		res =res.replaceAll("�"," ");
		
		
		res = res.replaceAll("�", "EUR");
		res = res.replaceAll("�", "Nael");
	
		res =res.replaceAll("�","11115xx");
		res =res.replaceAll("�","11116xx");
		res =res.replaceAll("�","11117xx");
		res =res.replaceAll("�","11118xx");
		
		res =res.replaceAll("�","11119xx");
		res =res.replaceAll("�","11120xx");
		res =res.replaceAll("�","11121xx");
		res =res.replaceAll("�","11122xx");

	 	 
	  	return res;
	  }
	  
   
   public static String deCode(String res ){
	   
	   while(res.indexOf("11111xx") != -1){
	   	int aa = res.indexOf("11111xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   }
	   while(res.indexOf("11112xx") != -1){
	   	
	   	int aa = res.indexOf("11112xx");
	   //	System.out.println(aa + " " +res);
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11113xx") != -1){
	   	int aa = res.indexOf("11113xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11114xx") != -1){
	   	int aa = res.indexOf("11114xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11115xx") != -1){
	   	int aa = res.indexOf("11115xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11116xx") != -1){
	   	int aa = res.indexOf("11116xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11117xx") != -1){
	   	int aa = res.indexOf("11117xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11118xx") != -1){
	   	int aa = res.indexOf("11118xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 

	 
 	return res;
 }
   TreeMap agents = null;
   
   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
     this.getFixedAssortmentData();
agents = new TreeMap();
   //  makeData();
   //  getCustomerProductsList(null);
//this.getFixedAssortmentData();
getProdGroups();
     StringBuffer inputLineBuf = new StringBuffer();
	try {
		inputLineBuf = postData("request=getEmployees&recordsOnPage=100" + userstuff);
	} catch (Exception e1) {
		e1.printStackTrace();
	}
     if(inputLineBuf.length() > 2){
         
         try {
       	  File f = new File (getDir() + "tootajad.xml");
       	  if(f.exists())f.delete();
   		PrintWriter os = new PrintWriter(new FileWriter(
   				getDir() + "tootajad.xml", true));
   		os.write(inputLineBuf.toString());
   		os.close();
   	  }catch (IOException ex) {
   		ex.printStackTrace();
   	  }
    }
   
     // loene v�imalikud agendid sisse 
   
 	try {
 		  File f = new File (getDir() + "tootajad.xml");
 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	     DocumentBuilder db;
 		db = dbf.newDocumentBuilder();      	
     	FileInputStream in = getCleanInputStream(f);
     	String handler = null;
         Document doc = db.parse(in,handler);
         Node root = getNode(doc, "document");      
      //   System.out.println(root);
     	Node read = getNode(root, "records");
    // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
         for (int i = 0; i < read.getChildNodes().getLength(); i++){
         	  Node rida = read.getChildNodes().item(i);
         	  if(rida.getNodeName().equals("item")){

             	  try{
             		  
             		//	System.out.println("rida  " + rida.getNodeName() );
             		// getNode(getNode(getNode(root, "Order-Parties"), "Buyer"),"CodeBySupplier"));
             		 String salesman  =  getNode(rida, "employeeID").getFirstChild().getNodeValue(); 
             		
             		 Agent sm = new Agent (salesman);
             		 String name  = deCode(getNode(rida, "employeeName").getFirstChild().getNodeValue());        		
             		 sm.setName(name);
                     tm.put(name, sm); 
                     //getNode(rida,"item")
                     agents.put(salesman, name);	 
             	  }catch(Exception e){
             		  e.printStackTrace();
             	  }  
         	  }
              
         }
         
 	}catch(Exception e){
 		e.printStackTrace();
 	}
     
     System.out.println("Agendilist tehtud" );

//     System.out.println("Hinnakirju kasutusel "  + pricelists.size() +  " " + pricelists.toString() );

  //   System.out.println("Aktiivseid kliente kasutusel "  + custtsUsed.size());

    // fixedLines = getFixedAssortmentData1();
            
     
     Agent sm = new Agent ("HOUSE");
		       		
		 sm.setName("OFFICE");
     tm.put("HOUSE", sm); 
   //  setBulkSales(tm);
     return tm;
   }

   
   private StringBuffer removeTags(String input , String tag){
	   StringBuffer res  = new StringBuffer();
	   int len = tag.length();
	   while (input.indexOf("<" + tag + ">") != -1){
		   int a = input.indexOf("<" + tag + ">");
		   res.append(input.substring(0,a));
		   input = input.substring(a + len + 1);
		  // System.out.println("vahe " + input.substring(0,100));
		   if(input.indexOf("</" +tag + ">") != -1){
			   input = input.substring(input.indexOf("</" +tag + ">") + len + 3);
		   }
		   //System.out.println("vahe2 " + input.substring(0,100));
	   }
	   
	  res.append(input);
	   return res ;
   }
   
private String getMonth(){
	Calendar cal = Calendar.getInstance();
	String res = cal.get(Calendar.YEAR)+ "-";
	String mm = (cal.get(Calendar.MONTH )+1) +"";
	if(mm.length() == 1 )
		mm = "0" + mm;
	
	return res + mm + "-01";
}
   
   private void makeData(){
	   

	     
       try{
           StringBuffer inputLineBuf = new StringBuffer();
       	try {
    		inputLineBuf = postData("request=getProductGroups" + userstuff);
    	} catch (Exception e1) {
    		e1.printStackTrace();
    	}
           if(inputLineBuf.length() > 2){          
                 try {
               	  File f = new File (getDir() + "tootegrupiderp.xml");
               	  if(f.exists())f.delete();
           	
				 Converter.saveFileInUTF8(getDir() + "tootegrupiderp.xml", inputLineBuf.toString());
           	  }catch (Exception ex) {
           		ex.printStackTrace();
           	  }
            }
           
           //l�peb tootegruppidega tegelemine 
          // System.out.println("Tootegrupid tehtud" );
           System.out.println("Teen Dokyumendi listi" );   
           
           FileListFilter fil = new FileListFilter("doklist","xml");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int i = 0; i < ll.length; i++) {
	    		 String name = ll[i];
		    		// System.out.println(name);
		 		  File f = new File (getDir() + name);
	           if(f.exists()){
	        	   f.delete();
	           }
	    	 }
	    		
	    		
	    
           
         String  data = "request=getSalesDocuments&recordsOnPage=100&dateFrom=" + getMonth() + "&type=INVWAYBILL"
				+ userstuff;// &dateFrom=2010-12-01
		// System.out.println(data);

		inputLineBuf = postData(data);
		// System.out.println(inputLineBuf.toString());

		int tsykkel = 0;
		if (inputLineBuf.indexOf("recordsTotal") != -1) {
			int lines = inputLineBuf.indexOf("recordsTotal");
			int end = inputLineBuf.indexOf("recordsTotal", lines + 12);
			// System.out.println(inputLineBuf);
			try {
				tsykkel = Integer.parseInt(inputLineBuf.substring(
						lines + 13, end - 2));
			} catch (Exception e) {

			}
		}
		if (inputLineBuf.length() > 2) {
			try {
				File f = new File(getDir() + "doklist1.xml");
				if (f.exists())
					f.delete();
				 String c = (char)0x19 + "";
				 Converter.saveFileInUTF8(getDir() + "doklist1.xml", inputLineBuf.toString().replace((char)0x19, ' ')
						 .replace((char)0x19, ' ').replace((char)0x19, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' '));
		       
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (tsykkel > 100) {
			int counter = 2;
			for (; tsykkel > 100; tsykkel = tsykkel - 100) {
				try {
					inputLineBuf = postData("request=getSalesDocuments&recordsOnPage=100&type=INVWAYBILL&dateFrom=" + getMonth() + "&pageNo="
							+ counter + userstuff);
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				if (inputLineBuf.length() > 2) {
					try {
						File f = new File(getDir() + "doklist" + counter
								+ ".xml");
						if (f.exists())
							f.delete();
						
						 Converter.saveFileInUTF8(getDir() + "doklist" + counter + ".xml", inputLineBuf.toString().replace((char)0x19, ' ')
								 .replace((char)0x19, ' ').replace((char)0x19, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' '));
					
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				counter++;
			}

		}
           
           
           
           
           System.out.println("Laod alustan" );
       	try {
    		inputLineBuf = postData("request=getWarehouses" + userstuff);
    	} catch (Exception e1) {
    		e1.printStackTrace();
    	}
           if(inputLineBuf.length() > 2){          
                 try {
               	  File f = new File (getDir() + "laoderp.xml");
               	  if(f.exists())f.delete();
               	 Converter.saveFileInUTF8(getDir() + "laoderp.xml", inputLineBuf.toString());
           	
           	  }catch (Exception ex) {
           		ex.printStackTrace();
           	  }
            }
          
           
           
           
           
           
           System.out.println("kliendisortimendid alustan" );
           
           try {
       		inputLineBuf = postData("request=getReportSalesRows" + userstuff);
       	 if(inputLineBuf.indexOf("<errorCode>") != -1 ){
       		inputLineBuf = postData("request=getReportSalesRows" + userstuff);
         }
       	if(inputLineBuf.indexOf("<errorCode>") != -1 ){
       		inputLineBuf = postData("request=getReportSalesRows" + userstuff);
         }
       		
       	} catch (Exception e1) {
       		e1.printStackTrace();
       	}
              if(inputLineBuf.length() > 2){          
                    try {
                  	  File f = new File (getDir() + "klienditoodeErp.xml");
                  	  if(f.exists())f.delete();
                  	  Converter.saveFileInUTF8(getDir() + "klienditoodeErp.xml", inputLineBuf.toString());
              	 }catch (Exception ex) {
              		ex.printStackTrace();
              	  }
               }
           
           System.out.println("kliendisortimendid tehtud" );
           //algab Hinnakirja faili moodustamine
           
           System.out.println("Algab toodete moodustamine" );
            tsykkel = 0;
           
           inputLineBuf = new StringBuffer();
           boolean cut = true;
           if("true".equals(getParent().getProps().getProperty("nocut"))){        	   
        	   cut = false;
           }
          String laoID = "2";
          if(getParent().getProps().getProperty("laoid") != null){        	   
       	   laoID = getParent().getProps().getProperty("laoid");
          }
        	
       	try {
    		inputLineBuf = postData("request=getProducts&recordsOnPage=100&getPackageInfo=1&getContainerInfo=1&getStockInfo=1&warehouseID="+ laoID + userstuff); //&getStockInfo=1
    		  if(cut){
    				inputLineBuf = removeTags(inputLineBuf.toString(),"manufacturerName");
    	    		inputLineBuf = removeTags(inputLineBuf.toString(),"brandName");
    	    		inputLineBuf = removeTags(inputLineBuf.toString(),"packages");
    	    		inputLineBuf = removeTags(inputLineBuf.toString(),"description");
    	    		inputLineBuf = removeTags(inputLineBuf.toString(),"longdesc");	  
    		  }
    		  
    		  
    	
       	} catch (Exception e1) {
    		e1.printStackTrace();
    	}
        
           
         if(inputLineBuf.indexOf("recordsTotal") != -1 ){
        	 int lines = inputLineBuf.indexOf("recordsTotal");
        	 int end = inputLineBuf.indexOf("recordsTotal",lines+12);
        	
        	 try{
        		 tsykkel = Integer.parseInt(inputLineBuf.substring(lines+13,end-2));
        	
        		 //kuion olemas siis vanad failid maha.
        		 if(tsykkel != 0){
        			 fil = new FileListFilter("tootederp","xml");
         	    	 ll = new File (getDir()).list(fil);
         	    	 for (int i = 0; i < ll.length; i++) {
         	    		 String name = ll[i];
         		    		// System.out.println(name);
         		 		  File f = new File (getDir() + name);
         	           if(f.exists()){
         	        	   f.delete();
         	           }
         	    	 }	 
        		 }
        		 
        	 }catch(Exception e){
        		 
        	 }
         }
        // System.out.println("Tooted Tsykkel " + tsykkel );
           if(inputLineBuf.length() > 2){        
               try {
             	  File f = new File (getDir() + "tootederp1.xml");
             	  if(f.exists())f.delete();
         		 Converter.saveFileInUTF8(getDir() + "tootederp1.xml", inputLineBuf.toString());
         	  }catch (Exception ex) {
         		ex.printStackTrace();
         	  }
          }
           
           if(tsykkel > 100 ){
        	  int counter = 2;
        	   for(;tsykkel > 100;tsykkel = tsykkel-100){
        		   try {
        	    		inputLineBuf = postData("request=getProducts&recordsOnPage=100&getPackageInfo=1&getContainerInfo=1&getStockInfo=1&warehouseID=" + laoID + "&pageNo=" + counter + userstuff); //&getStockInfo=1
        	    		 if(cut){
        	    				inputLineBuf = removeTags(inputLineBuf.toString(),"manufacturerName");
                	    		inputLineBuf = removeTags(inputLineBuf.toString(),"brandName");
                	    		inputLineBuf = removeTags(inputLineBuf.toString(),"packages");
                	    		inputLineBuf = removeTags(inputLineBuf.toString(),"description");
                	    		inputLineBuf = removeTags(inputLineBuf.toString(),"longdesc");	 
        	    		 }
        	    	
        		   } catch (Exception e1) {
        	    		e1.printStackTrace();
        	    	}  
        	    	if(inputLineBuf.length() > 2){        
        	               try {
        	             	  File f = new File (getDir() + "tootederp" + counter + ".xml");
        	             	  if(f.exists())f.delete();
        	         
        	       	    	 Converter.saveFileInUTF8(getDir() + "tootederp" + counter + ".xml", inputLineBuf.toString());
        	               }catch (Exception ex) {
        	         		ex.printStackTrace();
        	         	  }
        	          }

        	    	counter++;
        	   }
        	   
           }
         
           
           System.out.println("l�petatud toodete moodustamine" );
           System.out.println("algab klientide moodustamine" ); 
           inputLineBuf = new StringBuffer();
           
        
           
           try {
        	 //  System.out.println("request=getCustomers&recordsOnPage=100&responseMode=detail" + userstuff);
       	
       		inputLineBuf = getData("request=getCustomers&recordsOnPage=100&responseMode=detail" + userstuff);
       	
       
       	   } catch (Exception e1) {
       		e1.printStackTrace();
       	   }
       
       	   
       	   tsykkel = 0;
        if(inputLineBuf.indexOf("recordsTotal") != -1 ){
       	 int lines = inputLineBuf.indexOf("recordsTotal");
       	 int end = inputLineBuf.indexOf("recordsTotal",lines+12);
       	
       	 try{
       		 tsykkel = Integer.parseInt(inputLineBuf.substring(lines+13,end-2));
       	//kui �nnestub siis kustutame maha
       		 if(tsykkel != 0){
       			fil = new FileListFilter("kliendiderp","xml");
    	    	 ll = new File (getDir()).list(fil);
    	    	 for (int i = 0; i < ll.length; i++) {
    	    		 String name = ll[i];
    		    		// System.out.println(name);
    		 		  File f = new File (getDir() + name);
    	           if(f.exists()){
    	        	   f.delete();
    	           }
    	    	 } 
    	    	   if(inputLineBuf.length() > 2){
    	        	   try {
    	              	  File f = new File (getDir() + "kliendiderp1.xml");
    	              	  if(f.exists())f.delete();
    	          	
    	          		 Converter.saveFileInUTF8(getDir() + "kliendiderp1.xml", inputLineBuf.toString());
         	            
    	          	  }catch (Exception ex) {
    	          		ex.printStackTrace();
    	          	  }
    	            }
    	    	 
       		 }
        		
       		 
       	 }catch(Exception e){
       		 
       	 }
       	 //System.out.println("Klientide tsykkel " + tsykkel);
       	 
        }
        
         
           
           if(tsykkel > 100 ){
         	  int counter = 2;
         	   for(;tsykkel > 100;tsykkel = tsykkel-100){
         		  try {
         			//  System.out.println("request=getCustomers&recordsOnPage=100&responseMode=detail&pageNo=" + counter + userstuff);
         	       		inputLineBuf = getData("request=getCustomers&recordsOnPage=100&responseMode=detail&pageNo=" + counter + userstuff);
         	       	   } catch (Exception e1) {
         	       		e1.printStackTrace();
         	       	   }
         	        
         	    	if(inputLineBuf.length() > 2){        
         	               try {
         	             	  File f = new File (getDir() + "kliendiderp" + counter + ".xml");
         	             	  if(f.exists())f.delete();
         	         	
       	          		 Converter.saveFileInUTF8(getDir() + "kliendiderp" + counter + ".xml", inputLineBuf.toString());
            	        
         	         	  }catch (Exception ex) {
         	         		ex.printStackTrace();
         	         	  }
         	          }

         	    	counter++;
         	   }
         	   
            }
           
           
           
           System.out.println("l�petatud klientide moodustamine" );

       
       }catch(Exception e){
       	e.printStackTrace();
       }

	   
   }

   private  Node getNode(Node node, String name){
	    for (int i = 0 ; i < node.getChildNodes().getLength() ; i++){
	      Node nd = node.getChildNodes().item(i);
	  //    System.out.println(nd.getNodeName());
	      if (nd.getNodeName().equals(name)){
	        return nd;
	      }
	    }
	    return null;
	  }

        	  private FileInputStream getCleanInputStream(File xmlFile)throws FileNotFoundException, IOException {
        		FileInputStream in = new FileInputStream(xmlFile);
        		int bomKount = getBOMCount(xmlFile);
        		if (bomKount > 0) {
        		byte[] buf = new byte[bomKount];
        		in.read(buf);
        		}
        		return in;
        		}

        	  
        	  private int getBOMCount(File xmlFile) throws FileNotFoundException,
        	  IOException {
        	  DataInputStream din = new DataInputStream(new FileInputStream(xmlFile));
        	  int bomKount = 0;
        	  byte b = din.readByte();
        	  while (b < 0) {
        	  bomKount++;
        	  b = din.readByte();
        	  }
        	  din.close();
        	//   System.out.println("Skipping BOM, " + bomKount + " bytes");
        	  return bomKount;
        	  } 
        	  
        	  
       public String getDistribution() {
         return null;
       }


   public TreeMap getCustomers(Agent agent){
	     TreeMap tm = new TreeMap();
	    makeData();
	    
	     TreeMap groups = this.getGrupid();
	     sortimendid = new HashMap();
		  ketid = new HashMap();
	     
	     this.customers = new ArrayList();
	   
	   try {
		   
		   FileListFilter fil = new FileListFilter("kliendiderp","xml");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int i = 0; i < ll.length; i++) {
	    		
	    		
	    		 String name = ll[i];
	    		// System.out.println(name);
	 		  File f = new File (getDir() + name);
		   
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "document");      
	     	Node read = getNode(root, "records");
	     	 for (int s = 0; s < read.getChildNodes().getLength(); s++){
	         	  Node rida = read.getChildNodes().item(s);
	         	  try{
	         		 if(rida.getNodeName().equals("item")){
	         			
	         		  String id =  deCode(getNode(rida,"customerID").getFirstChild().getNodeValue());
	         		  Customer customer = new Customer(id);
	         		  try{
	         			 String kett = deCode(getNode(rida,"code").getFirstChild().getNodeValue());;
		                 if(kett != null && kett.trim().length()>0){
		              	   if(ketid.get(Converter.cleanLitLatEst(kett.trim())) != null){
		                 		Vector vv = (Vector)ketid.get(Converter.cleanLitLatEst(kett));
		                 		vv.add(customer.getID());
		                 		ketid.put(Converter.cleanLitLatEst(kett.trim()), vv);
		                 	}else{
		                 		Vector vv = new Vector();
		                 		vv.add(customer.getID());
		                 		ketid.put(Converter.cleanLitLatEst(kett.trim()), vv);
		                 	}
		                 }
	                    	  
	         		  }catch(Exception e){
	         			//e.printStackTrace();  
	         		  }
	         		
	         		  try{
	         		  String namec =   deCode(getNode(rida,"fullName").getFirstChild().getNodeValue());
	         		  customer.setName(Converter.enCodeEst(namec));
                         }catch(Exception ee){
	                     customer.setName(id); 
                         }
                         
                         String grp = (String)groups.get(id);
    	                 if(grp != null){
    	                 	//System.out.println("Siin on grupp olemas "  + grp + " " + cust.getID() );
    	                 	Vector vv = new Vector();
    	                 	StringTokenizer st = new StringTokenizer(grp,"|");
    	                 	while (st.hasMoreElements()){
    	                 		String gg = st.nextToken();
    	                 		vv.add(gg);
    	                 		sortimendid.put(customer.getID(), gg);
    	                 	}
    	                 	customer.setFixSortCode(vv);
    	                 //	System.out.println(customer.getName() + " " + vv.toString());
    	                 //	cust.setGroup(grp);
    	                 }
    	                
                         
	         		  try{ 
	 	         		 String aadr =   getNode(rida,"address").getFirstChild().getNodeValue();
	 	         		  customer.setAddress(Converter.adressLink(Converter.enCodeEst(deCode(aadr)),"Estonia"));
	         		 }catch(Exception ee){
		         		 customer.setAddress(""); 
	         		 }
	         		  try{
	         		   String salesman  = deCode( getNode(rida,"customerManagerID").getFirstChild().getNodeValue());
       		            customer.addAgent(new Agent(salesman));
       		            this.custtsUsed.add(id);
	         		   }catch(Exception ee){
		         		 
	         		   }
	         		  customer.addAgent(new Agent("HOUSE"));
	         		 if("true".equals(getParent().getProps().getProperty("all.customers"))){
	         			Enumeration enume = getParent().getAgentsHash().elements();
	         				while(enume.hasMoreElements()){
	         					Agent a = (Agent)enume.nextElement();
	         					customer.addAgent(a);
	         				//	System.out.println("Cust " + a.getName());
	         				}
	         			 
	         		 }
	         		 
	         		 if(getParent().getProps().getProperty("all.customers.list") != null){
	         			 String ags = getParent().getProps().getProperty("all.customers.list");
	         			 
		         			StringTokenizer st = new StringTokenizer(ags,",");
		         			while (st.hasMoreTokens()){
		         				Agent a = new Agent(st.nextToken());
		         					customer.addAgent(a);
		         			}
		         			
		         		 }
	         		  
	         		  try{
	  	         		String paym  =  getNode(rida,"paymentDays").getFirstChild().getNodeValue();
	         		            customer.setPaymTerm(paym);
	  	         		 }catch(Exception ee){
	  		        	 }
	  	         	  try{
		  	         		String payer  =  getNode(rida,"payerID").getFirstChild().getNodeValue();
		         		            customer.setPayer(payer);
		  	         		 }catch(Exception ee){
		  		        	 }
	         		 
	         		     tm.put(customer.getName(), customer);
	                     this.customers.add(customer.getID());
	                     
	         		 }
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	         
	    	  }    
	         }
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	    
     return tm;
   }

 

 
   
   public TreeMap getProducts(Agent agent){

    TreeMap tm = new TreeMap();
       prodCodes = new Hashtable();
    Vector noGrups = new Vector();
    Vector noBrands = new Vector();
        prodHash = new HashMap();
    
    if(getParent().getProps().getProperty("nogrups") != null){
    	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("nogrups"),",");
    	while (st.hasMoreElements()){
    		noGrups.addElement(st.nextToken());
    	}
    }
   
    if(getParent().getProps().getProperty("nobrands") != null){
    	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("nobrands"),",");
    	while (st.hasMoreElements()){
    		noBrands.addElement(st.nextToken());
    	}
    }
   
     try {
    	  FileListFilter fil = new FileListFilter("tootederp","xml");
    	 String[] ll = new File (getDir()).list(fil);
    	 for (int i = 0; i < ll.length; i++) {
    		
    		 String name = ll[i];
 		  File f = new File (getDir() + name);
 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	     DocumentBuilder db;
 		db = dbf.newDocumentBuilder();      	
     	FileInputStream in = getCleanInputStream(f);
     	String handler = null;
         Document doc = db.parse(in,handler);
     	
       Node root = getNode(doc, "document");      
       //   System.out.println(root);
      	Node read = getNode(root, "records");
     // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
          for (int s = 0; s < read.getChildNodes().getLength(); s++){
          	  Node rida = read.getChildNodes().item(s);
          	  if(rida.getNodeName().equals("item")){
         	  try{
         		  
         		  
         		 
              		 String code  = deCode(getNode(rida,"productID").getFirstChild().getNodeValue());
              		 Product prod = new Product(code);
              		 boolean goes = true ;
              		 String kood = "";
              		 try{
              			 kood = deCode(getNode(rida,"code").getFirstChild().getNodeValue()); 
              			prodCodes.put(kood.trim(), code);
              		 }catch (Exception e){
              			 
              		 }
              	
              		 try{
              		 String namec  =  (getNode(rida,"name").getFirstChild().getNodeValue());
              		 prod.setName(Converter.enCodeEst(namec) + " " + kood + " " + code);
                      }catch(Exception r){
              			// r.printStackTrace();
              			 prod.setName("noname " + code);
              		 }
              		 try{
              			String ean  =  getNode(rida,"code2").getFirstChild().getNodeValue();
                  		if(ean != null){
                  			prod.setEAN(ean);	
                  		}	 
              		 }catch(Exception r){
              			 
              		 }
              		
              		
              		String price  =  getNode(rida,"price").getFirstChild().getNodeValue();
              		if(price != null){
              			prod.setPrice(Double.parseDouble(price));	
              		}
              		
              		String vat  =  getNode(rida,"vatrate").getFirstChild().getNodeValue();
              		if(vat != null){
              			prod.setVat(vat);	
              		}
              		
              		
              		String group  =  getNode(rida,"groupID").getFirstChild().getNodeValue();
              		if(group != null){
              			String ghh = (String)groupList.get(group);
              			//System.out.println("ghh " + ghh );
              			if(ghh != null){
              				prod.setGroup(ghh);	
                  			if(noGrups.contains(group)){
                  				goes = false;
                  			}	
              			}
              			
              		}
              		

              		String brand  =  getNode(rida,"brandID").getFirstChild().getNodeValue();
              		if(brand != null){
              		if(noBrands.contains(brand.trim())){
              			//System.out.println(prod.getName());
                  				goes = false;
                  			}	
              		}
              		
              		
             
              		
              		try{
              			String unitName  =  getNode(rida,"unitName").getFirstChild().getNodeValue();
                  		if(unitName != null){
                  			prod.setUnit(deCode(unitName));	
                  		}	
              		}catch(Exception d){
              			
              		}
              		
              		
              	 
              		
              		String active  =  getNode(rida,"active").getFirstChild().getNodeValue();
              	
              		if(active != null && !active.equals("1")){
              				goes = false;
              		}
              	
              		try{
              			String stock  =  getNode(getNode(getNode(rida,"warehouses"),"item"),"totalInStock").getFirstChild().getNodeValue();
                  		if(stock != null){
                  			prod.setStock(Double.parseDouble(stock));	
                  		}	
              		}catch(Exception e){
              			
              		}
              		
              		try{
              			String kastis  =  getNode(getNode(getNode(rida,"packages"),"kast"),"amount").getFirstChild().getNodeValue();
                  		if(kastis != null){
                  			prod.setInCases(Double.parseDouble(kastis));	
                  		}	
              		}catch(Exception e){
              			
              		}
             		  
                  
         		 if(goes){
         			 String sorting = prod.getID();
         			if(getParent().getProps().getProperty("sorting") != null){
         				if(getParent().getProps().getProperty("sorting").equals("nimi")){
         					sorting = prod.getName();
         				}
         				if(getParent().getProps().getProperty("sorting").equals("gruppnimi")){
         					sorting = prod.getGroup() + prod.getName();
         				}
         				if(getParent().getProps().getProperty("sorting").equals("gruppkood")){
         					sorting = prod.getGroup() + prod.getID();
         				}
         			}
         			  tm.put(sorting , prod); 
         			  prodHash.put(prod.getID(), prod);
         		  }
                      
         	  }catch(Exception e){
         		  e.printStackTrace();
         	  }
          	  }     
         }
    
          
          
    	 }	 
    
     } catch (Exception e){
       e.printStackTrace();
     }
     
     try {
    	// getOutDir();
 		//Converter.makeTelemaFiles(this.getOutDir(), tm, this.getProdGroups(), null, ownGLN);
 	} catch (Exception e) {
 		
 		e.printStackTrace();
 	}
	 
     return tm;
   }

   private String ownGLN = null;
   private String getOutDir(){
       String dir = getParent().getProps().getProperty("readout");
       if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
       return dir;
     }

   
   public String getAssortmentStr(Agent agent){
    return null;
  }

   
   private String getKlient(String kli){
	   String klient = null ;
	   return klient ;
   }

   public Vector getCustomerProductsList(String agentCode){
	   Vector custProds = new Vector();

		try {
	  		  File f = new File (getDir() + "klienditoodeErp.xml");
	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	        Node root = getNode(doc, "document");      
	       	Node read = getNode(root, "records");
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	           	  Node rida = read.getChildNodes().item(i);
	           	  if(rida.getNodeName().equals("CSV")){
	          	  String csv = rida.getFirstChild().getNodeValue();
	          	  csv = csv.replaceAll("\"\"", "|");
	          
	          	  StringTokenizer st = new StringTokenizer(csv,"|");
	          	  st.nextToken();
	          	  while (st.hasMoreElements()){
	          		  String line = st.nextToken();
	          		 line = line.replaceAll("\";\"", "|");
	          		 line = line.replaceAll("\"", " ").trim();
	          		
	      //    		System.out.println("line  " + line);
	          	//	line  6276|8|Aurusti eGo Sigma|2013-06-29|6.6667|8|1
	          		 StringTokenizer stv = new StringTokenizer(line,"|");
	          		  while (stv.hasMoreElements()){
	          			  String klient = (String)docCust.get(stv.nextToken().trim());
	          			  if (klient != null){
	          				 String toode =  " "; 
	 	          			
		          			 String kuupaev = " ";
		          			 double kog = 1;
		          			 try{
		          				  toode = stv.nextToken().trim();
			          			   stv.nextToken().trim();//String nimi =
		          			   kuupaev = stv.nextToken().trim().substring(5);
		          			   double lastpr = 0;
		          			   try{
		          				   lastpr = Double.parseDouble( stv.nextToken().trim());
		          			   }catch(Exception e){
		          				   
		          			   }
		          			  //String hindb =
		          			   stv.nextToken().trim();//String hindn =
		          			  String kogus = stv.nextToken().trim();
		          			 kog = Double.parseDouble(kogus); 
		          			 
		          			  
			          		    CustProd cp = new CustProd(klient, toode,kog,0,0,kuupaev,lastpr,0,0," ");
			          		  custProds.add(cp);
	  
	          			  		 }catch(Exception e){
	          			 }  } } } }     
	          }
	          
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	  
	  	   System.out.println("Cp size " + custProds.size());
	   return custProds;
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "ErplyLtt";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
 

  public double getSalary(String agentCode){
     return 0;
   }

   private String getDir(){
     String dir = getParent().getProps().getProperty("readin");
     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
     return dir;
   }
   
   private HashMap ketid = null;
   private HashMap sortimendid = null;
   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private Vector debtList = new Vector();
   private TreeMap groupList = new TreeMap();
  
   private Vector fixedLines = new Vector();
   private Vector prodsInUse = new Vector();
   private Vector pricelists = new Vector();
   private Vector custtsUsed = new Vector();
   private String session = "";
 
   private String username = null;
   private String company = null;
   private String password = null;
   private String apiversion = null;
   private String returntype = null;
   private String userstuff = null;
     private URL myurl = null;

  /**
   * setBulkSales
   *
   * @param agents Map
   */
     
     private Hashtable docCust = null;
     
  public void setBulkSales(Map agents) {
	  TreeMap myygid = new TreeMap();
	  docCust = new Hashtable();
      FileListFilter fil = new FileListFilter("doklist","xml");
 	 String[] ll = new File (getDir()).list(fil);
 	 for (int u = 0; u < ll.length; u++) {
 		 String name = ll[u];
	    		System.out.println(name);
	 		  File f = new File (getDir() + name);
	 		  
	 		 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  	     try{
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	        
	        Node root = getNode(doc, "document");      
	        //   System.out.println(root);
	       	Node read = getNode(root, "records");
	      // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	           for (int i = 0; i < read.getChildNodes().getLength(); i++){
	           	  Node rida = read.getChildNodes().item(i);
	           	  if(rida.getNodeName().equals("item")){
	          	  try{
				
				String agent = getNode(rida, "employeeID").getFirstChild().getNodeValue();
				double total = Double.parseDouble(getNode(rida, "total").getFirstChild().getNodeValue());
		        if(myygid.get(agent) != null){
		        	double hetk = Double.parseDouble((String)myygid.get(agent));
		        	myygid.put(agent,(total+ hetk)+"");
		        }else{
		        	myygid.put(agent,total+"");
		        }
		        docCust.put(getNode(rida, "id").getFirstChild().getNodeValue(), 
		        		getNode(rida, "clientID").getFirstChild().getNodeValue());
		          }catch(Exception e){
	          		  e.printStackTrace();
	          	  }
	           	}     
	          }
	           
	           
         	  }catch(Exception e){
         		  e.printStackTrace();
         		
         	  }
         	//  System.out.println("myygid" + myygid.size());
 	         
	 		  Iterator itera = myygid.keySet().iterator();
	 		  while(itera.hasNext()){
	 			  String code = (String)itera.next();
	 			 Agent agent = (Agent)agents.get(code);
	             if (agent != null){
	            	   agent.setMonthSales((int) Double.parseDouble((String)myygid.get(code))); 
	             }
	             	
	             	
	           
	 		  }
         	  
         	
	 		  
	 		  
       
 	 }
	  
	  
  }
}
