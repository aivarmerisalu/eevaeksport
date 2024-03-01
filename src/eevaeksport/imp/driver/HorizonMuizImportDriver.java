package eevaeksport.imp.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
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

import eevaeksport.Agent;
import eevaeksport.Customer;
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

public class HorizonMuizImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public HorizonMuizImportDriver(ImportPanel parent) {
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

	   
	   private TreeMap getPandiPakend() throws SQLException{
		   Connection con = getNewConnection();
		   TreeMap pants = new TreeMap();
		    String sql1 = " SELECT  nn.[PK_NOM] as toode, nn.[PK_PIESKGR]as grupp ,P.PK_PIESK as kood " + 
		    		"  FROM [FTGWIN_ALUS_2021].[VLMALS2638].[NNOMPIESK] nn inner join [FTGWIN_ALUS_2021].[VLMALS2638].NPIESK P"
		    		+ "  ON P.PK_PIESKGR=NN.PK_PIESKGR";
		   
		    Statement st1 = con.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
		    while (rs1.next()) {
		    	String prod = rs1.getString("toode").trim();
		    	String kood = rs1.getString("kood").trim();
		    	pants.put(prod, kood);
		    }
		      // System.out.println(toode + " prod1 " + prod);
		    rs1.close();
		    st1.close();
		    con.close();
		    return pants;
		 }
		  
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  
  public String identifyCustomer(String id,String regnr)  {
    return null;
  }


  
  public String identifyProduct(String ean, String prodID) throws SQLException{ 	  
    return null;
}
  

	
  
  
  public void makeOrder(Order order) throws SQLException{
       counter = 0;
       
       if (signatures == null){
    	   signatures = new Vector();
       }
       		  String signature = order.getSignature();
    			  if(signatures.contains(signature)){
    				  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" 
    			  + Converter.deCode(order.getCompany()) ;
    			  }else{
    				  makeOutput(order);  
        			 // signatures.add(signature);
        			  this.trouble = null;
        		  }
    	
       try {
		saveObject(signatures, "signatures.vec");
	} catch (IOException e) {
		e.printStackTrace();
	}
  }
  
  
  public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
	
  
  private String weburl = "https://horizonweb-cloud.visma.lv/REST_H34780920/rest/TNdmPvzSale/template/303";
		  //"http://mappost:7378/rest/TNdmPvzSale/template/303";
  
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
		  System.out.println("weburl " + weburl);
		  DefaultHttpClient httpClient = new DefaultHttpClient();
		 // Credentials defaultcreds = new UsernamePasswordCredentials("restmappost", "R");
		  Credentials defaultcreds = new UsernamePasswordCredentials("merisoftrest",  parent.getProps().getProperty("wsdl.pass"));
		  
		  httpClient.getCredentialsProvider().setCredentials(
                   AuthScope.ANY, defaultcreds);
		  HttpPost postRequest = new HttpPost(weburl);
		  StringEntity input = new StringEntity(req);
		  input.setContentType("application/xml");
		  postRequest.setEntity(input);
		  response = httpClient.execute(postRequest);	  
	  }catch(Exception e){
		  trouble = "NOT IMPORTED ORDER \n" + req2;
		  e.printStackTrace();
	  }
	  return  EntityUtils.toString(response.getEntity());
  }
	  
  
 private boolean isBeforeDay(Calendar cal , Calendar ship){
	  
	  Calendar cc = Calendar.getInstance();
	  cc.setTimeInMillis(cal.getTimeInMillis());
	  cc.set(Calendar.HOUR, 1);
	  cc.set(Calendar.MINUTE, 1);
	  cc.set(Calendar.SECOND, 1);
	  cc.set(Calendar.MILLISECOND, 1);
	  
	  Calendar ss = Calendar.getInstance();
	  ss.setTimeInMillis(ship.getTimeInMillis());
	  ss.set(Calendar.HOUR, 1);
	  ss.set(Calendar.MINUTE, 1);
	  ss.set(Calendar.SECOND, 1);
	  ss.set(Calendar.MILLISECOND, 1);
	  if(cc.getTime().before(ss.getTime())){
		  return true;
	  }
	  return false;
  }
	

 private boolean isToday(Calendar cal){
	  Calendar cc = Calendar.getInstance();
	  if(cc.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)){
		  return true;
	  }
	  return false;
 }
 
 private String url = "jdbc:odbc:horizoncloud";
 
 private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	  }

 
 public Customer getCustId(String kood) throws SQLException {

	    Connection con = getNewConnection();
	  	
	    String cmd = "SELECT  K.PK_KLIENTS AS id, K.KODS AS kood, K.NOSAUK AS nimi FROM   VLMALS2638.DKLIENTS K " +
	  			" WHERE K.KODS = '" +kood+"'" ;  

	  	Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(cmd);
	    Customer liik = null;
	    if (rs.next()) {
	        liik = new Customer(rs.getString("id").trim());
	        String name = rs.getString("nimi").trim();
	        liik.setName(name);
	      }
	    rs.close();
	    st.close();
	    con.close();
	    return liik;
	}

 public String getProdId(String kood) throws SQLException {

	    Connection con = getNewConnection();
	    

		  String cmdo = "select  as  itemid,t.NOSAUK  AS nimetus, t.M_ATT12 AS kastis,1 AS alusel,   bar.BAR_KODS AS ean ,PK_NOMGR AS grupp,yh.kods As unit,1/M_ATT13 as liter " +
		" from VLMALS2638.NNOM  t LEFT OUTER JOIN VLMALS2638.NBARKODS bar ON bar.PK_NOM=t.PK_NOM AND bar.IS_MAIN = 1 " +
		" LEFT OUTER JOIN VLMALS2638.NVIEN yh ON t.PK_VIEN=yh.PK_VIEN " +
		"  where (t.PAMVID IN (2015,7713,24522,18205,12200,12201,13204,13205,15139)) AND (( ( (t.STATUSS = 0)) " +
		"   AND ( ( EXISTS (SELECT 1 FROM VLMALS2638.NNOMSAGR NSG WHERE NSG.PK_NOM=t.PK_NOM AND NSG.PK_NOMPGR =5)))))";
	    
		  
	  	
	    String cmd = "SELECT PK_NOM AS id FROM   VLMALS2638.NNOM t " +
	  			" WHERE t.KODS = '" +Converter.deCode(kood)+"'" ;  

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
  
  
  private void makeOutput(Order order){
	  Hashtable pandikas =  new Hashtable();
	
	/* // TreeMap pants = null;
	try {
		//pants = getPandiPakend();
	//	System.out.println("pants " + pants.size());
	} catch (SQLException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}*/
	 	try {
	 		
	 	
	 	
	 		String comm = "";
	 		if(order.getInfo() != null && order.getInfo().trim().length() >0){
	 			comm =  order.getInfo() ;
	 		}
	 	//	System.out.println("Comment " + comm);
			
	 		String doknr = order.getDokNr();
	 		 Calendar  shipDate = order.getShipCal();

	 		  Calendar cal = Calendar.getInstance();
	            if(isToday(shipDate)){
	            	 	if(shipDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
	   	            		shipDate.add(Calendar.DATE, 2);
	   	            	}else{
	   	            		shipDate.add(Calendar.DATE, 1);
	   	            	}
	   	             	
	            }
	         
		           if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
		        	   cal.add(Calendar.DATE , 2);
		           }else{
		        	   cal.add(Calendar.DATE , 1);
		           }
		           
		           if (shipDate != null && isBeforeDay(cal,shipDate) ) {			        	
		               cal = shipDate;
		               }        	    
		           
		        String cukood =  Converter.deCode(order.getCompany());
		        String adid = null;
		        if(cukood.indexOf("_")!= -1){
		        	adid =  escape(cukood.substring(cukood.lastIndexOf("_")+1));
		        	cukood = escape(cukood.substring(0,cukood.lastIndexOf("_")));
			    }
		           
		           Customer cuid = getCustId(cukood);
		
		           String aadr = "<NdmPvzAddition>";
		           if(adid != null){
		        	   aadr =  " <NdmPvzAddition><PK_ADRESE><href>/rest/TNdmAdr/" + adid + "</href></PK_ADRESE>";
		           }
		         
		           String agg = order.getAgent();
		           if(agg.equals("6")) {
		        	   agg = "57";
		           }
		      
		           if(agg.equals("19")) {
		        	   agg = "54";
		           }
		        
		           String  order_xml = "<resource> <description>Merisoft Order</description>" +
		           "<entity xsi:schemaLocation=\"TNdmPvzSale.xsd TNdmPvzSale.xsd\" xmlns=\"TNdmPvzSale.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
	 		    "   <PK_DOKT> " +
        " <href>/rest/TdmDocType/303</href> " +
        " <title>NPMERI : Order Merisoft</title> " +
        " </PK_DOKT>" +
        " <PAMV_ID>2711</PAMV_ID> "  + 
        " <PK_IESTADE><href>/rest/TdmDIestadeBL/1</href></PK_IESTADE>" +
       // " <PK_PVNK><href>/rest/TdmPvnKat/1</href></PK_PVNK>" +
        " <PK_R_ES><href>/rest/TDdmSaviRek/7092</href></PK_R_ES> " + 
        " <PK_KLIENTS><href>/rest/TDdmCustomer/" + cuid.getID() + "</href></PK_KLIENTS> "+   
        " <DAT_DOK>" + getDateNiceString(cal) + "</DAT_DOK>" + //getDateNiceString(Calendar.getInstance())
        " <DAT_PIEG>" + getDateNiceString(cal) + "</DAT_PIEG>" + 
        " <PK_PARD><href>/rest/TNdmPard/" + agg + "</href></PK_PARD>" + 
        " <PK_VAL> <href>/rest/TsdmValName/3</href><title>EUR</title></PK_VAL><PAMAT>TextOnAccount</PAMAT><TIPS>1</TIPS>" +
        " <PK_PROJ><href>/rest/TdmProject/1</href><title>V : Visi projekti</title></PK_PROJ>" +
       aadr +   
        " <PVN_TIPS>0</PVN_TIPS> <PIELIKUMS>" + comm+ "</PIELIKUMS>" +
		" <PK_NDARAPR><href>/rest/TdmNDarAprBL/1</href></PK_NDARAPR></NdmPvzAddition>" ; 
      
      
    /*  " <urn:soPo>" + doknr + "</urn:soPo> " +
      " <urn:soCurr>EUR</urn:soCurr> " +
      " <urn:soSlspsn>" + order.getAgent() + "</urn:soSlspsn>" ;
      */
      
      
      order_xml = order_xml + "<tblRindas>";

	     	 Vector lines = order.getOrderLines();
		      for (int i = 0; i < lines.size(); i++) {
		    	  OrderLine line = (OrderLine) lines.elementAt(i);
		    	  String prid = getProdId(line.getToode());
		         order_xml = order_xml + "<row>";
		         order_xml = order_xml +  "<RN_VEIDS>0</RN_VEIDS> ";
		         order_xml = order_xml +  "<PK_NOM>" + prid + "</PK_NOM>";
		          //  <!--KODS>113</KODS-->
		          // <!--BAR_KODS>4751012090010</BAR_KODS-->
		         order_xml = order_xml +  "<DAUDZ>" + line.getKogus() + "</DAUDZ>";
		         //   <TEKSTS>Rindā Piezīmes</TEKSTS>
		         //  <TEMPERAT>Rindā raksturojums</TEMPERAT>
			      order_xml = order_xml +  "<V_KODS>" + line.getUnit() + "</V_KODS>";
			      if(line.getDiscount() == -2) {
			    	  order_xml = order_xml +"<CENA>0.01</CENA>";
                      order_xml = order_xml +"<ATLAIDE>99.99</ATLAIDE>";
			      }else  if(line.getDiscount() > 0 || line.getDiscount() < -1){
                      order_xml = order_xml +"<CENA>"+ line.getPrice() + "</CENA>";
                      order_xml = order_xml +"<ATLAIDE>"+ line.getDiscount() + "</ATLAIDE>";
			      }
			      order_xml = order_xml + "</row>";
			 
			     counter++;
			   /*  if(pants != null) {
			    	 String pant = (String) pants.get(prid)  ; 
				     
				     if(pant != null){
			           	  if(pandikas.get(pant) == null){
			           		  pandikas.put(pant,line.getKogus()+"");
			                 } else {
			                 	pandikas.put(pant,(new Double((String)pandikas.get(pant)).doubleValue()+line.getKogus())+"");
			                 }
			              } 
			     }*/
			    
		             
	      }
		      
		      
		      
		      
		      if( pandikas.size()>0){ // kommentisin v�lja pandi asja Kalevi soovil sept 2018
		        	Enumeration enumpant = pandikas.keys();
		        	
		         while(enumpant.hasMoreElements()){
		         	String kood = (String)enumpant.nextElement();
		         	if(kood.trim().length()>0){
		             	 order_xml = order_xml + "<row>";
				         order_xml = order_xml +  "<RN_VEIDS>2</RN_VEIDS> ";
				         order_xml = order_xml +  "<PK_NOM>" + kood + "</PK_NOM>";
				         order_xml = order_xml + "</row>";
		        	}    	
		         }
		        }
		      
		      
	
		
	 	order_xml = order_xml + "</tblRindas>";
	 	order_xml = order_xml + "</entity>";
	 	order_xml = order_xml + "</resource>";
	
	 	System.out.println("output " +order_xml); 
	 	
	 		
	 	try {
	 		
	 		  String fn = doknr;
		      if(doknr.trim().length()== 0){
		    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
		      }
		      
		      String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + fn  +  ".xml";
		      System.out.println(fileName);
		       
		      Converter.saveFileInUTF8(fileName, order_xml);

		      fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-"  + fn  +  "-response.xml";
		      System.out.println(fileName);
		
		   //   trouble = "IMPORTED ORDER \n" + order_xml;
		      
		      if(parent.getProps().getProperty("smtphost") != null) {
				     Agent ag = new Agent(agg);
				      ag.setName(agg);
				       Converter.sendCommentEmail("New Order " + comm, null, doknr, cukood, cuid.getName(), null, false, ag, parent, null);
				     }
		      if("true".equals(parent.getProps().getProperty("testing"))){
			 		System.out.println("Testing ");
		    	  System.exit(0);
		      }
			 	
	 		String xml = getWeb(order_xml , "DokNr:" + order.getDokNr() + " Company:" + cukood);		
	 	//	System.out.println("Tulemus " + xml);
		     Converter.saveFileInUTF8(fileName, xml);
		     
	      
	      setNumAddedLines(counter); 
	      
	    
	 	
	 	} catch (Exception e) {
	 		e.printStackTrace();
	 		
	 	  trouble = "NOT IMPORTED ORDER \n" + order_xml;
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
    return "HorizonMuizImport";
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
	// TODO Auto-generated method stub
	return trouble;
}
  
}
