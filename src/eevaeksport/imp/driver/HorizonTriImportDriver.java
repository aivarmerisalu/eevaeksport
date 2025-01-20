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
import org.json.simple.JSONObject;

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

public class HorizonTriImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public HorizonTriImportDriver(ImportPanel parent) {
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

	
	 private void loadAlco(){
		  try{
			  alco = new Vector();
				ResultSet rs1 ;
		    	Statement stmr ;
				 String sqldea = "	  select t.PK_NOM as abi, t.KODS as  toode" +
	" from TRIDLV7866.NNOM  t LEFT OUTER JOIN TRIDLV7866.NBARKODS bar ON bar.PK_NOM=t.PK_NOM AND bar.IS_MAIN = 1 " +
	" LEFT OUTER JOIN TRIDLV7866.NVIEN yh ON t.PK_VIEN=yh.PK_VIEN " +
	"  LEFT OUTER JOIN TRIDLV7866.CHD_MG1 gr ON t.PK_MG1=gr.PK_MG1 " +
	"  LEFT OUTER JOIN TRIDLV7866.NMINMAX aa ON aa.PK_NOM=t.PK_NOM and aa.PK_NOL=22013 " +
	"   LEFT OUTER JOIN [TRIDLV7866].[NRAZOT] au ON t.PK_RAZOT=au.PK_RAZOT " +
	"   LEFT OUTER JOIN [TRIDLV7866].[DVALSTS] ri ON ri.PK_VALSTS=au.PK_VALSTS " +
	"  where t.STATUSS = 0  and PAMVID = 2015  and  gr.kods != 'TM' and t.kods not like 'NIC%'and t.kods not like 'PRV%'   ";
			    		
			//	 tubakas mitte jätta .
	
			        	Connection con = this.getNewConnection(); 
			        		 stmr = con.createStatement();
			            	
			                 rs1 = stmr.executeQuery(sqldea);
			                
			                 while(rs1.next()){
			                	String toode = rs1.getString("toode").trim();
			                	alco.add(toode);
			                }
			                
			               rs1.close();
			               stmr.close();
			               con.close();
			        	}catch(Exception s){
			        		s.printStackTrace();
			        	}

			    	System.out.println("alco " + alco.size());
			    	
			    	
	  }

	private Vector  alco = null;
	private Vector  toba = null;
	
	

	 private void loadToba(){
		  try{
			  toba = new Vector();
				ResultSet rs1 ;
		    	Statement stmr ;
				 String sqldea = "	  select t.PK_NOM as abi, t.KODS as  toode" +
	" from TRIDLV7866.NNOM  t LEFT OUTER JOIN TRIDLV7866.NBARKODS bar ON bar.PK_NOM=t.PK_NOM AND bar.IS_MAIN = 1 " +
	" LEFT OUTER JOIN TRIDLV7866.NVIEN yh ON t.PK_VIEN=yh.PK_VIEN " +
	"  LEFT OUTER JOIN TRIDLV7866.CHD_MG1 gr ON t.PK_MG1=gr.PK_MG1 " +
	"  LEFT OUTER JOIN TRIDLV7866.NMINMAX aa ON aa.PK_NOM=t.PK_NOM and aa.PK_NOL=22013 " +
	"   LEFT OUTER JOIN [TRIDLV7866].[NRAZOT] au ON t.PK_RAZOT=au.PK_RAZOT " +
	"   LEFT OUTER JOIN [TRIDLV7866].[DVALSTS] ri ON ri.PK_VALSTS=au.PK_VALSTS " +
	"  where t.STATUSS = 0  and PAMVID = 2015  and (t.kods like 'NIC%' or t.kods "
	+ " like 'PRV%' or t.kods  like 'MYC%')   ";
			    		
			//	 tubakas mitte jätta .
	
			        	Connection con = this.getNewConnection(); 
			        		 stmr = con.createStatement();
			            	
			                 rs1 = stmr.executeQuery(sqldea);
			                
			                 while(rs1.next()){
			                	String toode = rs1.getString("toode").trim();
			                	toba.add(toode);
			                }
			                
			               rs1.close();
			               stmr.close();
			               con.close();
			        	}catch(Exception s){
			        		s.printStackTrace();
			        	}

			    	System.out.println("alco " + toba.size());
			    	
			    	
	  }
	
	 private Vector splitOrder (Order order){
			
		  
		  String mills = (Calendar.getInstance().getTimeInMillis()+ "").substring(10);
		  
		  
		  Vector result = new Vector();
		  if(alco == null){
			  loadAlco();
		  }
		  if(toba == null) {
			  loadToba();
		  }
		 
			  
		  
		  
	          
		  Order ord1 = new Order(order.getCompany() ,order.getAgentFirstName());

		  ord1.setAgentCode(order.getAgentCode());
		  ord1.setDokNr(order.getDokNr() + "a");
		  ord1.setInfo(order.getInfo());
		  ord1.setTeostaja((order.getTeostaja()));
		  ord1.setChanged(order.isChanged());
		  ord1.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ord1.setStatus(order.getStatus());
		  ord1.setShipTime(order.getShipTime());
		  ord1.setWarehouse("33");
		  ord1.setDealer(order.getDealer());
		  
		  Order ord2 = new Order((order.getCompany()  ),order.getAgentFirstName());
		  ord2.setAgentCode(order.getAgentCode());
		  ord2.setDokNr(order.getDokNr()+ "c");
		  ord2.setInfo(order.getInfo());
		  ord2.setTeostaja((order.getTeostaja()));
		  ord2.setChanged(order.isChanged());
		  ord2.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ord2.setStatus(order.getStatus());
		  ord2.setShipTime(order.getShipTime());
		  ord2.setWarehouse("859");
		  ord2.setDealer(order.getDealer());
		  
		  
		  Order ord3 = new Order((order.getCompany()  ),order.getAgentFirstName());
		  ord3.setAgentCode(order.getAgentCode());
		  ord3.setDokNr(order.getDokNr()+ "t");
		  ord3.setInfo(order.getInfo());
		  ord3.setTeostaja((order.getTeostaja()));
		  ord3.setChanged(order.isChanged());
		  ord3.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ord3.setStatus(order.getStatus());
		  ord3.setShipTime(order.getShipTime());
		  ord3.setWarehouse("859");
		  ord3.setDealer(order.getDealer());
		  

		 

		  
		  
		  System.out.println("Splitin Warehouse agendid " + order.getWarehouse());
		  
		  Vector lines = order.getOrderLines();
		  Enumeration enums = lines.elements();
		  while  (enums.hasMoreElements()){
			  OrderLine lin = (OrderLine)enums.nextElement();
			
			     String itemid = lin.getToode();
			     
	           if(alco.contains(itemid)){
	                 	    	
	        	   ord1.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
	                 		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
	                 	  
	                      } else  if(toba.contains(itemid)){
	                 	    	
	        	            ord3.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
	                 		     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
	                 	  
	                      }else {
	         		    	 
			    	             ord2.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			             lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			  
			     	        }
			   
			     

		  }



		  if(ord1.getOrderLines() != null && ord1.getOrderLines().size()>0){
			  result.add(ord1);  
		  }

		  if(ord2.getOrderLines() != null && ord2.getOrderLines().size()>0){
			  result.add(ord2);  
		  }

		  if(ord3.getOrderLines() != null && ord3.getOrderLines().size()>0){
			  result.add(ord3);  
		  }


		 
		 System.out.println("Result =" +order.getDokNr()
				 + " "+ result.size());
		 //System.exit(0);
		  
		 return result ;
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
    				  Vector splitted =  splitOrder(order);
			    	  Enumeration en = splitted.elements();
					  
					  while(en.hasMoreElements()){
						  Order ord = (Order)en.nextElement();
	    				  makeOutput(ord);  
	        		  }
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
	
  
  private String weburl = "https://horizonweb-cloud.visma.lv/REST_H34780000/rest/TNdmPvzSale/template/5";
  
  private String getDateNiceString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    String year =   cal.get(Calendar.YEAR) + "";
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
	    String minute = String.valueOf(cal.get(Calendar.MINUTE));
	    String second = String.valueOf(cal.get(Calendar.SECOND));
	 
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
		
		 // Credentials defaultcreds = new UsernamePasswordCredentials("rest", "Tribaltic@18");
		  
		 // Credentials defaultcreds = new UsernamePasswordCredentials("TridensLV-read", "Rhjow1PuAQLF2AO3mn@TxrbthmWTCLGT");
		  Credentials defaultcreds = new UsernamePasswordCredentials("API", "t7uhc1Y_x4Q}J~H");
		  
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
 
 private String url = "jdbc:odbc:horinew";
 
 private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	  }

 
 public String getCustId(String kood) throws SQLException {

	    Connection con = getNewConnection();
	  	
	    String cmd = "SELECT K.NOSAUK AS nimi, K.PK_KLIENTS AS id, K.KODS AS kood FROM   TRIDLV7866.DKLIENTS K " +
	  			" WHERE ltrim(K.KODS) = '" + kood + "'" ;  

	  	Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(cmd);
	    System.out.println(cmd);
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
	    

		  String cmdo = "select  as  itemid,t.NOSAUK  AS nimetus, t.M_ATT12 AS kastis,1 AS alusel,   bar.BAR_KODS AS ean ,PK_NOMGR AS grupp,yh.kods As unit,1/M_ATT13 as liter " +
		" from TRIDLV7866.NNOM  t LEFT OUTER JOIN TRIDLV7866.NBARKODS bar ON bar.PK_NOM=t.PK_NOM AND bar.IS_MAIN = 1 " +
		" LEFT OUTER JOIN TRIDLV7866.NVIEN yh ON t.PK_VIEN=yh.PK_VIEN " +
		"  where (t.PAMVID IN (2015,7713,24522,18205,12200,12201,13204,13205,15139)) AND (( ( (t.STATUSS = 0)) " +
		"   AND ( ( EXISTS (SELECT 1 FROM TRIDLV7866.NNOMSAGR NSG WHERE NSG.PK_NOM=t.PK_NOM AND NSG.PK_NOMPGR =5)))))";
	    
		  
	  	
	    String cmd = "SELECT PK_NOM AS id FROM   TRIDLV7866.NNOM t " +
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
  	
	 	try {
	 		
	 	if("true".equals(parent.getProps().getProperty("testing"))){
	 		System.out.println("Testing ");
       	 	}else{
	 	
	 		String com = "";
	 		if(order.getInfo() != null && order.getInfo().trim().length() >0){
	 			com =  order.getInfo() ;
	 		}
	 		System.out.println("Comment " + com);
			
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
		        String ccc = cukood;
		        System.out.println("ccc " + ccc);
			     
		        if(cukood.indexOf("_")!= -1){
		        	adid =  escape(cukood.substring(cukood.lastIndexOf("_")+1));
		        	ccc = cukood.substring(0,cukood.lastIndexOf("_"));
		        	cukood = escape(cukood.substring(0,cukood.lastIndexOf("_")));
		        	
			    }

		        System.out.println("ccc0 " + ccc);
		        

	            String KT = "";
				String KOMPL = "";
				String ARVE = "";
			    

			
				
				if(com.indexOf("KT:")!=-1 && com.indexOf("KM:") != -1){
				      	KT = com.substring(com.indexOf("KT:"),com.indexOf("KM:"));
				} else if(com.indexOf("KT:")!=-1 && com.indexOf("AR:") != -1){
					    KT = com.substring(com.indexOf("KT:"),com.indexOf("AR:"));
				} else if(com.indexOf("KT:")!=-1){
					    KT = com.substring(com.indexOf("KT:"),com.length());
				}

				if(com.indexOf("AR:")!=-1 && com.indexOf("KM:") != -1){  
					KOMPL = com.substring(com.indexOf("KM:"),com.indexOf("AR:")); 	
				  }else if(com.indexOf("KM:") != -1){
				  	KOMPL = com.substring(com.indexOf("KM:"),com.length()); 	
				  }    

				if(com.indexOf("AR:")!=-1 ){  
					ARVE = com.substring(com.indexOf("AR:"),com.length());
				  }

				String a3 = "";
				
				if(ARVE.length() > 3){
					a3 = ARVE;
				}
				String a2 = "";
				if(KT.length() > 3){
					a2 = KT;
				}
				
				if(KOMPL.length() > 3){
					
				}
		        
		        String ladu = "33";
		        if(order.getWarehouse() != null && order.getWarehouse().trim().length() > 0){
		        	ladu = order.getWarehouse();
		        }
		        
		           String cuid = getCustId(ccc);
		           if( Converter.deCode(order.getCompany()).contains("_4356")) {
		        	   cuid = "1699";
		           }
		           if(cuid == null){
		        	   System.exit(0);
		           }
		           System.out.println("kliendi andmed" + cukood + " " + cuid + " " + adid);
		
		           String aadr = "<NdmPvzAddition>";
		           if(adid != null){
		        	   aadr =  " <NdmPvzAddition><PK_ADRESE><href>/rest/TNdmAdr/" + adid + "</href></PK_ADRESE>";
		           }
		         
		           
		           String  order_xml = "<resource> <description>Merisoft Order</description>" +
		           "<entity xsi:schemaLocation=\"TNdmPvzSale.xsd TNdmPvzSale.xsd\" xmlns=\"TNdmPvzSale.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
	 		    "   <PK_DOKT> " +
        " <href>/rest/TdmDocType/5</href> " +
        " <title>NPMERI : Order Merisoft</title> " +
        " </PK_DOKT>" +
        " <PAMV_ID>2711</PAMV_ID> "  + 
        " <PK_IESTADE><href>/rest/TdmDIestadeBL/1</href></PK_IESTADE>" +
        " <PK_PVNK><href>/rest/TdmPvnKat/1</href></PK_PVNK>" +
        " <PK_R_ES><href>/rest/TDdmSaviRek/1123</href></PK_R_ES> " + 
        " <PK_ESPATS><href>/rest/TNdmNol/" + ladu + "</href></PK_ESPATS> " + 
        

        
        " <PK_KLIENTS><href>/rest/TDdmCustomer/" + cuid + "</href></PK_KLIENTS> "+   
        " <DAT_DOK>" + getDateNiceString(cal) + "</DAT_DOK>" + //getDateNiceString(Calendar.getInstance())
        " <DAT_PIEG>" + getDateNiceString(cal) + "</DAT_PIEG>" + 
     //   " <PK_PARD><href>/rest/TNdmPard/" + order.getAgent() + "</href></PK_PARD>" + 
        " <PK_VAL> <href>/rest/TsdmValName/3</href><title>EUR</title></PK_VAL><PAMAT>TextOnAccount</PAMAT><TIPS>1</TIPS>" +
        " <PK_PROJ><href>/rest/TdmProject/1</href><title>V : Visi projekti</title></PK_PROJ>" +
       aadr +   
        " <PVN_TIPS>0</PVN_TIPS> <PIELIKUMS>" + a3+ "</PIELIKUMS>" +
		" <PK_NDARAPR><href>/rest/TdmNDarAprBL/1</href></PK_NDARAPR></NdmPvzAddition>" ; 
      
      if(a2 != null && a2.trim().length() > 0) {
    	  order_xml = order_xml + "<dmNdocIpasBL><row>";
    	  
    	  order_xml = order_xml + "<PK_NIPAS><href>/rest/TIpasibaBL/8</href></PK_NIPAS>"
    	  		//+ "<PK_DOK><href rel=\"self\">/rest/TNdmPvzSale/277267</href></PK_DOK>"
    	  		+ " <VERTTEXT>" + a2 + "</VERTTEXT>" + 
    	  		"<NI_PAMV_ID>17511</NI_PAMV_ID><VALUE>" + a2 + "</VALUE>";
    	  
    	  order_xml = order_xml + " </row></dmNdocIpasBL>";
    	  
      }
		           
		           /*<dmNdocIpasBL>
            <row>
                <PK_NDOKIPAS>2</PK_NDOKIPAS>
                <PK_NIPAS>
                    <href>/rest/TIpasibaBL/8</href>
                </PK_NIPAS>
                <PK_DOK>
                    <href rel="self">/rest/TNdmPvzSale/277267</href>
                </PK_DOK>
                <NI_KODS>KOMENTARS</NI_KODS>
                <NI_NOSAUK>Komentārs PLG</NI_NOSAUK>
                <PK_NIPASR/>
                <DAUDZ/>
               
                <PK_KRASA/>
                <KR_NOSAUK/>
                <KR_KRASA/>
                <KR_KODS/>
                <APR_NOSAUK/>
                <VALUE>10-16:00 piegāde</VALUE>
                <DATUMS/>
                <HIPERSAITE/>
            </row>
        </dmNdocIpasBL>*/
      
      
      order_xml = order_xml + "<tblRindas>";

	     	 Vector lines = order.getOrderLines();
		      for (int i = 0; i < lines.size(); i++) {
		    	  OrderLine line = (OrderLine) lines.elementAt(i);
		         order_xml = order_xml + "<row>";
		         order_xml = order_xml +  "<RN_VEIDS>0</RN_VEIDS> ";
		         order_xml = order_xml +  "<PK_NOM>" + getProdId(line.getToode()) + "</PK_NOM>";
		          //  <!--KODS>113</KODS-->
		          // <!--BAR_KODS>4751012090010</BAR_KODS-->
		         order_xml = order_xml +  "<DAUDZ>" + line.getKogus() + "</DAUDZ>";
		         //   <TEKSTS>Rindā Piezīmes</TEKSTS>
		         //  <TEMPERAT>Rindā raksturojums</TEMPERAT>
			      order_xml = order_xml +  "<V_KODS>" + line.getUnit() + "</V_KODS>";
			      if(line.getDiscount() > 0 || line.getDiscount() < -1){
                      order_xml = order_xml +"<CENA>"+ line.getPrice() + "</CENA>";
                      order_xml = order_xml +"<ATLAIDE>"+ line.getDiscount() + "</ATLAIDE>";
			      }
			      order_xml = order_xml + "</row>";
			 
			     counter++;
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
			 	
	 		String xml = getWeb(order_xml , "DokNr:" + order.getDokNr() + " Company:" + cukood);		
	 	//	System.out.println("Tulemus " + xml);
		     Converter.saveFileInUTF8(fileName, xml);
			 	
	      
	      setNumAddedLines(counter); 
	      
	      
	       if(xml.contains("error") || xml.toLowerCase().contains("kont")) {
	    	   trouble = "NOT IMPORTED ORDER \n" + xml;
	    	   System.exit(0);
	       }
	    
	 	
	 	} catch (Exception e) {
	 		e.printStackTrace();
	 		
	 	  trouble = "NOT IMPORTED ORDER \n" + order_xml;
	 	//  System.exit(0);
	 	}

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
    return "HorizonTriImport";
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
