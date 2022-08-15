package eevaeksport.driver;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.MilkContainer;
import eevaeksport.Product;
import eevaeksport.imp.Order;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class AxaZpienasTransaDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
  public AxaZpienasTransaDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    String databaseDriverName = "oracle.jdbc.driver.OracleDriver";
    databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    NF.setMaximumFractionDigits(2);
    NF.setMaximumFractionDigits(2);
    NF.setGroupingUsed(false);
    NF.setMinimumFractionDigits(2);
     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
    rcompan = getParent().getProps().getProperty("rest.company");
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  String rcompan = null;
  

  
  /**
   *
   */
  public void connectionUp(){
  }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	 private   TreeMap getRoutes(){
	     TreeMap tm = new TreeMap();
	     System.out.println("Routes alustan");
	   
	   try {
		   
		 String  cmd = " select r.routeid as kood , r.name as nimi from TRWayBill d  inner join trroutetable r on d.routeid = r.routeid "
		 		+ " and d.createddatetime > getdate()-2 and d.dataareaid = 'zp'  group by r.routeid, r.name"; 
		 Connection ss = this.getNewConnection();
	      Statement stmt = ss.createStatement();
		      ResultSet rs = stmt.executeQuery(cmd);
		      
		        while(rs.next()){
		        	 String id = (rs.getString("kood").trim());
			          String name = rs.getString("nimi").trim() + " " + id;
			          Customer cc = new Customer(id);
	 	         		 cc.setName(name);
			        	 tm.put(id, cc);
	 	             }
		        
		        rs.close();
		        stmt.close();
		          
	   }catch (Exception e){
      	  e.printStackTrace();
  	  }
	   System.out.println("Routesid kokku " + tm.size() );
	   return tm;
	   }  

	
  public String getSaveDebtInfo() {
	  this.getCustomers(null);
	  try{
	 	  Converter.saveRoutesJson(getRoutes());
	 	 }catch(Exception e){
	 		 e.printStackTrace();
	 	 }
	  
	  try{
 		  Converter.savePodJson(custMap);
 	 }catch(Exception e){
 		 e.printStackTrace();
 	 }
 	 
 	   sendShipitDocs(custMap);
	
	  System.exit(0);
   	return null;
   }
 
  

  private  void sendShipitDocs(TreeMap custs){
	    
	     System.out.println("Doc alustan");
	   
	    
         int cou = 0;
         Vector<Order> res = new Vector<Order>();
	     
	   try {
		
		   
		   String  cmd = "select companyref as cust ,r.routeid as route ,\r\n" + 
		   		"g.name as routeName, waybillid as waybill, h.personnelnumber as driver, transdate as kuup ,vehicle as car\r\n" + 
		   		" ,c.milksection1 as carsec1,c.milksection2 as carsec2,c.milksection3 as carsec3, trailer,t.milksection1 as trasec1,t.milksection2 as trasec2,t.milksection3 as trasec3 ,s.noteid "
		   		+ " ,g.departuretime as dstart, g.arrivaltime as dend , r.weightplanned as wp \r\n" + 
		   		" from TRRouteWaypoint r   inner join TRWayBill d on d.routeid = r.routeid \r\n" + 
		   		"										  and d.transdate > getdate()-1 and d.transdate < getdate() inner join trroutetable g on g.routeid= r.routeid \r\n" + 
		   		"		   								 inner join TRDestinationTable s ON s.destinationid = r.destinationid \r\n" + 
		   		"		   				   				  inner join HCMWORKER h on h.recid = driverrecid \r\n" + 
		   		"										  right join TRTransportTable c ON c.transportid=vehicle\r\n" + 
		   		"										  left join TRTransportTable t ON t.transportid=trailer  where d.routegroupid like 'PS%'  order by r.routeid,waybill,r.rowno"; 
			 Connection ss = this.getNewConnection();
		      Statement stmt = ss.createStatement();
			      ResultSet rs = stmt.executeQuery(cmd);
			      
			      HashSet olemas = new HashSet();
			        while(rs.next()){
			        	 
				         
				      	 String custid = rs.getString("cust").trim();
				      	 if(!olemas.contains(custid)) {
				      		 olemas.add(custid);
				      	 }
		    	    	 String  routes = rs.getString("route").trim();
		    	    	 String  routeName = rs.getString("routeName").replaceAll(","," ").replaceAll("\"", "'").trim();
		    	    	String doknr =   rs.getString("waybill").trim();
		    	    	 String driver =   rs.getString("driver").trim();
		         		 Date deldate  =  rs.getDate("kuup");
		         		 
		         		 String car =  rs.getString("car");
		         		 if(car != null && car.trim().length() > 0) {
		         			 car = car.trim();
		         		 }
		         		 int carsec1 = rs.getInt("carsec1");
		         		 int carsec2 = rs.getInt("carsec2");
		         		 int carsec3 = rs.getInt("carsec3");
		         		 
		         		 String trailer = rs.getString("trailer");
		         		 if(trailer != null && trailer.trim().length() > 0) {
		         			trailer = trailer.trim();
		         		 }
		         		 
		         		 int trasec1 = rs.getInt("trasec1");
		         		 int trasec2 = rs.getInt("trasec2");
		         		 int trasec3 = rs.getInt("trasec3");
		         		 String note = rs.getString("noteid");
		         		 long dstart  = rs.getLong("dstart") *1000L;
		         		 long dend  = rs.getLong("dend")*1000L;
		         		 double wp = rs.getDouble("wp");
		         	     if(note != null && note.length() > 1) {
		         	    	 if(note.indexOf("EKO") != -1) {
		         	    		 note = "GREEN";
		         	    	 }
		         	    	if(note.indexOf("DP") != -1) {
		         	    		 note = "YELLOW";
		         	    	 }
		         	     }else {
		         	    	 note = null;
		         	     }
		         		 
		         	    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm");
		         	  String ds =  simpleDateFormat1.format(new Date(dstart));
		         	  String de =  simpleDateFormat1.format(new Date(dend));
		         	  String dd =    simpleDateFormat1.format(new Date(dend - dstart));
		         	     
		         		 Vector<Product> lines = new Vector<Product>();
		         		 Product pr = new Product("P");
		         		 double kogus = 1;
		         		  pr.setInCases(kogus);
		         		 lines.add(pr);
		         		 Calendar ccc = Calendar.getInstance();
		         		 Date now = new Date(ccc.getTimeInMillis());
		         		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		         		
		         		if(fmt.format(deldate).equals(fmt.format(now))){
		     	         			
		         		Order shipit = new Order(custid,"autosender");
		         			shipit.prodLines = lines;
		         			shipit.docType = "COLLECTION";
		         			shipit.origId = null;
		         			shipit.completedMsisdn = null;
		         			shipit.completedDate = null;
		         			shipit.completedComment = null;
		         			shipit.deliveryDate = deldate;
		         			shipit.extDoc = doknr;
		         			shipit.route =  routes;
		         			shipit.driver = driver;
		         			shipit.car = car;
		         			shipit.carsec1 = carsec1;
		         			shipit.carsec3 = carsec3;
		         			shipit.carsec2 = carsec2;
		         			shipit.trailer = trailer;
		         			shipit.trasec1 = trasec1;
		         			shipit.trasec2 = trasec2;
		         			shipit.trasec3 = trasec3;
		         			shipit.routeName = routeName + " (" + routes + ")";
		         			shipit.colorStatus = note;
		         			if(dend > 0) {
		         				shipit.dend = de;
		         			}
		         			if(dstart > 0 ) {
		         				shipit.dstart = ds;
		         			}
		         			if(dend > 0 && dstart > 0 ) {
		         				shipit.duration = dd;
		         			}
		         			
		         			shipit.wplanned = wp;
		         			shipit.id = Calendar.getInstance().getTimeInMillis() + "";
		         		 
		         			 res.add(shipit);
			         	  }
				          
		 	             }
			        
			        rs.close();
			        stmt.close();
			        ss.close();
		   
			 Iterator itera =  custMap.keySet().iterator();
			 while(itera.hasNext()) {
				 String custid = (String)itera.next();
				 if(!olemas.contains(custid)) {
					 
					 
					 
					 Vector<Product> lines = new Vector<Product>();
	         		 Product pr = new Product("P");
	         		 double kogus = 1;
	         		  pr.setInCases(kogus);
	         		 lines.add(pr);
	         		 Calendar ccc = Calendar.getInstance();
	         		 Date now = new Date(ccc.getTimeInMillis());
	         		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
	         		
	       			
	         		Order shipit = new Order(custid,"autosender");
	         			shipit.prodLines = lines;
	         			shipit.docType = "COLLECTION";
	         			shipit.origId = null;
	         			shipit.completedMsisdn = null;
	         			shipit.completedDate = null;
	         			shipit.completedComment = null;
	         			shipit.deliveryDate = now;
	         			shipit.extDoc = "noOrder";
	         			shipit.id = Calendar.getInstance().getTimeInMillis() + "";
	         		 
	         			 res.add(shipit);
					 
				 }
			 }
				        
					  
	         	
	        System.out.println("Dokumente salvestama " + res.size() );
	        Converter.saveShipitListJson(res, custs) ;
	          
	   } catch (Exception e){
   	     e.printStackTrace();
	   }
	     System.out.println("Dokumente kokku " + cou );
	   }  


  
  
  
  public Vector getFixedAssortmentData() {
				return null;
	}

  

	  public Vector getPriceMatrixData() {
	          Vector lines = new Vector();
	          	          return lines;
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
	 
	 
	  
    String cmd = "select inventlocationid as kood , w.name as name , worker_bbs from  InventLocation w where dataareaid = 'zp' and inventlocationid like '0%'"; 
      cmd = "  select  h.personnelnumber as kood,w.name as name  from  TRWayBill d  " + 
    		"		   								 inner join HCMWORKER h on h.recid = driverrecid inner join InventLocation w on h.recid = worker_bbs " + 
    		"										  where d.routegroupid like 'PS%'  and d.transdate > getdate()-1 and d.transdate < getdate()"; 

    Properties propse = EksportFrame.loadProps(parent.getProps().getProperty("emailfile"));
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    StringBuffer sb2 = new StringBuffer();
    agnames  = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("name").trim();
          agent.setName(name + " " + agent.getID());
          agent.setIsPilot(true);
          
          agent.setLanguage("LIT");
          agent.setCountry("lt");
          agent.setPassWord("0000");
          
          String email = " ";
          
          if(propse.get(agent.getID()) != null){
        	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
          }
          
          
          if(email.length() > 3){
        	  agent.setEmail(email);
          }
          if(email.length() > 1){
        	  agent.setActive(true);
          }
          
          
          
          agents.put(name + agent.getID(), agent);
          if(propse.containsKey(agent.getID())){
        	  agnames.put(agent.getID(), name);
             // sb2.append(agent.getID() + ";" + name + "\n");  
          }
          sb2.append(agent.getID() + ";" + name + "\n");  
          
        }
    } catch (Exception e){
        getParent().showError(e);
      }
    
    if(sb2.length() > 2){
    	try{
    		 Converter.saveFileInUTF8( "agent.txt", sb2);
    	}catch (Exception e){
    	}
     }
    Agent dr  =  new Agent("driver");
    dr.setName("driver");

    agents.put("driver" ,dr);
    
    return agents;
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
  	
	
	  public static Vector upside(Vector inVecu){
		   Vector inVec = (Vector)inVecu.clone();
	        Vector sorted = new Vector(inVec.size());
	        while(inVec.size() > 0){
	        	sorted.addElement(inVec.elementAt(inVec.size()-1));
	        	inVec.removeElementAt(inVec.size()-1);
	        }
	        return sorted;
	      }
	
	//select * from TRRouteWaypoint r inner join TRWayBill d on d.routeid = r.routeid  and d.createddatetime > getdate()-1 order by r.routeid,r.rowno

	 private TreeMap custMap = new TreeMap();
	  public TreeMap getCustomers(Agent agent){
		  
		  	  
	 String cmd = "SELECT  name as name,street as nimelisa,namealias,cus.accountnum,cus.invoiceaccount AS payer,  cus.linedisc ,  address, " +
			" (select personnelnumber as kood from  DirPArtyTable w   inner join HCMWORKER h on h.person = w.recid where h.recid = cus.MAINCONTACTWORKER ) AS agent ," +
			"   pay.numofdays AS term, " +
			"(select top 1 l.locator  from  LOGISTICSELECTRONICADDRESS l  where l.privateforparty = cus.party and l.type = 1) as ph1," + 
			"			  (select top 1 l.locator  from  LOGISTICSELECTRONICADDRESS l  where l.privateforparty = cus.party and l.type = 2) as em1, " + 
			"			 (select l.locator  from  LOGISTICSELECTRONICADDRESS l  where l.recid = dp.primarycontactphone) as ph2," + 
			"			 (select l.locator  from  LOGISTICSELECTRONICADDRESS l  where l.recid = dp.primarycontactfax) as fax ," + 
			"			 (select l.locator  from  LOGISTICSELECTRONICADDRESS l  where l.recid = dp.primarycontactemail) as em2  ," +
			 " (select l.locator  from  LOGISTICSELECTRONICADDRESS l  where l.recid = dp.primarycontactphone) as driver ,  " +
	      " cus.vendgroup as grupp,cus.pricegroup as prgrupp " +
	      " ,  registrationlt_bbs as regnum	, cus.vatnum , state , county, cus.taxgroup,registrationlt_bbs as encode ,economyid_bbs as economyid,agreement_bbs as agreement " +
   " , cus.bankcentralbankpurposetext as volainfo " +
 " FROM  vendtable cus  inner join DirPartyTable dp ON cus.partY=dp.REcid  " +
" INNER JOIN LOGISTICSPOSTALADDRESS adr ON adr.LOCATION = dp.PRIMARYADDRESSLOCATION and getdate() >=VALIDFROM  and  getdate() <= VALIDTO " +
 "  left  JOIN  paymterm   pay on cus.paymtermid= pay.paymtermid " +
	" and pay.DATAAREAID = cus.DATAAREAID and  pay.dataareaid='zp'  left  JOIN  custtable  asi  on asi.vendaccount= cus.accountnum  " +
 " and asi.DATAAREAID = cus.DATAAREAID and  asi.dataareaid='zp'   WHERE   (cus.dataareaid='zp' AND cus.blocked != 2 )" +
  " union " +
 "SELECT 'P' + stationid + ' ' +dim.description as name,'' as  nimelisa,'' as namealias, stationid as accountnum,'' as  payer, '' as linedisc ,'Lithuania' as address ," +
	"(select personnelnumber as kood from  DirPArtyTable w   inner join HCMWORKER h on h.person = w.recid where h.recid = dim.responsible ) AS agent ," +
   " 0 as  term, ''  as  ph1 , ''  as  em1, ''  as  ph2, ''  as  fax, ''  as  em2 ,'' as  driver ,   '' as  grupp,'' as  prgrupp  " +
"	,  '' as  regnum	, '' as vatnum , 'Somewhere' as  state , 'County' as  county,'' as taxgroup,'' as encode ,'' as economyid,''  as agreement, '' as volainfo " +
" FROM  BBSStationTable dim " +
" where   dataareaid = 'zp'"; // OK
	 
	       String todays = "select destinationid as cust , planningcapacity as pla from TRRouteWaypoint r " + 
	       		"	       		 inner join TRWayBill d on d.routeid = r.routeid  and d.transdate > getdate()-2  group by destinationid,planningcapacity"; 
	           
	  if (isDebug()) System.out.println(cmd);
	    TreeMap customers = new TreeMap();
	  
	    HashSet today = new HashSet();
	    HashMap paak = new HashMap();
	    StringBuffer sb1 = new StringBuffer();
	    try {
	    	
	  Connection ddd = getNewConnection();
	       
	       Statement stmt = ddd.createStatement();
	       ResultSet rs = stmt.executeQuery(todays);
		    
		      while(rs.next()){
		    	  String aa = rs.getString("cust").trim();
		    	  String bb = rs.getString("pla");
		    	  if(!today.contains(aa)) {
		    		  today.add(aa);
		    	  }
		    	  String cc = "";
		    	  if(paak.get(aa) != null) {
		    		  cc = (String)paak.get(aa);
		    	  }
		    	  if(bb != null) {
		    		  paak.put(aa, (cc + bb.trim()).replaceAll(";", " ").replaceAll(":", " ").replaceAll("\\s",""));  
		    	  }
		    	
		      }
		      
		 //     System.out.println("ppp " + paak.toString());
	     
		  rs.close();
	      rs = stmt.executeQuery(cmd);
	    
	      while(rs.next()){
	        
	    	  String name = rs.getString("name").replaceAll("UAB", " ").trim();
	        String alias = rs.getString("nimelisa");
	        
	        
	        String accountnum = rs.getString("accountnum").trim();
	        Customer cust = new Customer(accountnum);
	        if(alias != null && alias.trim().length()>0 && !alias.trim().equals(name)){
	        	if(!name.trim().startsWith(alias.trim())) {
	        		name =  name + " " + alias ;
		        	  cust.restStreet = (alias.trim().replaceAll("\\r\\n|\\r|\\n", " "));	
	        	}
	        	
	        }
	        
	        name = accountnum + " " + name;
	        name = name.replaceAll("\\r\\n|\\r|\\n", " ");
	        name = name.replaceAll("'", " ").replaceAll("\"", " ").replaceAll("  ", " ");
	        cust.setName( Converter.enCodeEst(name.trim()));
	        
	        String payer = rs.getString("payer");
	        if(payer == null || payer.trim().length()== 0){
	        	cust.setPayer(cust.getID());
	        }else{
	        	cust.setPayer(payer.trim());
	        }
	       if(payer != null){
	    	   cust.setPayer(payer);
		   }
	       
	       if(paak.get(cust.getID()) != null && ((String)paak.get(cust.getID())).trim().length() > 0 ) {
	    	   cust.section = (String)paak.get(cust.getID());
	       }
	       
	        String aadr = rs.getString("address");
	        if (aadr != null) {
	          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ')),"Lithuania").replaceAll("\\r\\n|\\r|\\n", " "));
	        
	        }
	        
	        String agen = rs.getString("agent");
	      
	        if (agen != null && agen.trim().length() >0) {
	          cust.addAgent(new Agent(agen.trim()));
	          cust.setMainAgent(agen.trim());
	        }
	
	        int term = rs.getInt("term");
	        if (term > 0) {
	          cust.setPaymTerm(term + "");
	        }
	                
	        
	        String persd = "";
	        String ph1 = rs.getString("ph1");
	        if (ph1 != null && ph1.trim().length()>0) {
	          persd = "Tel:" + ph1.trim().replaceAll("\\r\\n|\\r|\\n", " ").replaceAll("\\s", "").trim() + "|";
	        }
	        
	        String em1 = rs.getString("em1");
	        if (em1 != null && em1.trim().length()>0) {
	          persd = persd  + em1.trim() + "|";
	        }

	        String ph2 = rs.getString("ph2");
	        if (ph2 != null && ph2.trim().length()>0) {
	        	if(!persd.contains(ph2.trim())) {
	        		persd = persd + "Tel:" +ph2.replaceAll("\\r\\n|\\r|\\n", " ").replaceAll("\\s", "").trim() + "|";
	        	}
	        }
	      
	        String em2 = rs.getString("em2");
	        if (em2 != null && em2.trim().length()>0) {
	        	if(!persd.contains(em2.trim())) {
	        		  persd = persd  + em2.trim() + "|";
	        	}
             }
	        
	        if(persd != null) {
	            cust.setPersonalData(persd.replaceAll("\\r\\n|\\r|\\n", " ").trim());
	  	    }
	      
	       
	        String prgrupp = rs.getString("prgrupp");
	        if (prgrupp != null && prgrupp.trim().length()>0) {
	        	cust.setCustType(prgrupp.trim());
	        	cust.setGroup(prgrupp.trim());
	        }
	        
	       // String osad = rs.getString("osad");
	        
	        String regnum = rs.getString("regnum");
	        if (regnum != null && regnum.trim().length()>0) {
	        	cust.setRegnr(regnum.trim());
	        }
	        
	        cust.setCity(Converter.enCodeEst(cust.getCity()).replaceAll("\\r\\n|\\r|\\n", " ").trim());
	         
	       // if(today.contains(cust.getID())) {
	            customers.put(name, cust);
	            custMap.put(cust.getID(), cust);
            //}
	   
     	      }
	      
	         rs.close();
             stmt.close();
                           
             }catch (Exception r){
          	   
          	   r.printStackTrace();
             }
             Customer zp = new Customer("zp");
             zp.setName("zp factory");
                customers.put("zp factory", zp);
	            custMap.put(zp.getID(), zp);

	            Customer zp2 = new Customer("ZP");
	             zp2.setName("ZP factory");
	             customers.put("ZP factory", zp2);
		            custMap.put(zp2.getID(), zp2);
		            
		            Customer zp0 = new Customer("018");
		             zp0.setName("Object 018");
		             customers.put("Object 018", zp0);
			            custMap.put(zp0.getID(), zp0);
		            
			            Customer zp8 = new Customer("8814");
			             zp8.setName("Object 8814");
			             customers.put("Object 8814", zp8);
				            custMap.put(zp8.getID(), zp8);
				            
				            Customer zp8q = new Customer("8021");
				             zp8q.setName("Object 8021");
				             customers.put("Object 8021", zp8q);
					            custMap.put(zp8q.getID(), zp8q);
					            Customer zp8q1 = new Customer("18");
					             zp8q1.setName("Object 18");
					             customers.put("Object 18", zp8q1);
						            custMap.put(zp8q1.getID(), zp8q1);
						            Customer zp8q12 = new Customer("8301");
						             zp8q12.setName("Object 8301");
						             customers.put("Object 8301", zp8q12);
							            custMap.put(zp8q12.getID(), zp8q12); 
							            
							            
							            Customer zp8q13 = new Customer("389401");
							             zp8q13.setName("Object 389401");
							             customers.put("Object 389401", zp8q13);
								            custMap.put(zp8q13.getID(), zp8q13); 

								            Customer zp8q131 = new Customer("364301");
								             zp8q131.setName("Object 364301");
								             customers.put("Object 364301", zp8q131);
									            custMap.put(zp8q131.getID(), zp8q131); 
									            
	    return customers;
	  }


  private Connection getNewConnection() throws SQLException{
        return DriverManager.getConnection(getParent().getProps().getProperty("dsn.host"), getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
      }
  


      public String getDistribution() {
        return null;
      }

     
  public TreeMap getProdGroups() throws SQLException {
  
	TreeMap   tm = new TreeMap();  
    return tm;

  }


    
  public TreeMap getProdGroupsSecond() throws SQLException {
	  Connection con = getNewConnection();
	    TreeMap   tm = new TreeMap();
		    return tm;
  }
  

  
 
	  public TreeMap getProducts(Agent agent){

		    String cmd = " SELECT mod.moduletype, inv.itemid, inv. namealias AS nimetus,  1 AS kastis,'' AS alusel, 1 AS price " +
		 "	  , '' AS ean ,inv.CATEGORYPRODUCTLINE_BBS AS grupp2,  inv.CATEGORYINVENTGROUP_BBS   AS grupp, '' as grid,mod.unitid As unit, 21 as vat , " +
		 "    0  as samm ,sortcode as sorting    FROM  inventtable inv " +
			"  left JOIN  inventtablemodule mod ON mod.itemid = inv.itemid and 'prod'=mod.DATAAREAID " +
			"   WHERE inv.dataareaid='prod' and mod.moduletype = 2   " +
			"   and inv.itemid like 'PP%'   order by inv.itemid ";
		    
		    //z p algusega
		  

		    if (isDebug()) System.out.println(cmd);
		    TreeMap products = new TreeMap();
			  HashMap prodHash = new HashMap();
		        String kood = "P";
		        Product prod = new Product(kood);
		        String nimetus = Converter.enCodeEst( "Pienas " + kood) ;
		   
		      prod.setName(( nimetus.replaceAll("'", " ").replaceAll("\"", " ").trim()).replaceAll("\n", " ").trim());
		        products.put(nimetus, prod);
		        prodHash.put(prod.getID(), prod);
		    return products;
		  }


  

  public Vector getCustomerProductsList(String agentCode){
    Vector custProds = new Vector();
       return custProds;
  }
  
 
  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


    public void setBulkSales(Map agents){
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
    return "AxaZpienasMilkMana";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  private HashMap brandplans = null; 
  

  public String getPilotDayData(Agent agent){
	      return null;
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
