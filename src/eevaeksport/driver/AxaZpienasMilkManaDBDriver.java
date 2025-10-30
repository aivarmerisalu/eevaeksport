package eevaeksport.driver;


import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
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
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

public class AxaZpienasMilkManaDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
  public AxaZpienasMilkManaDBDriver(EksportPanel parent){
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

  public String getSaveDebtInfo() {
   	return null;
   }
 
  
  public Vector getFixedAssortmentData() {
				return null;
	}

  

	  public Vector getPriceMatrixData() {
	          Vector lines = new Vector();
	          
	         String sql = "  select accountrelation as custid ,'' as clgroup, itemrelation as prodid ,amount as price, quantityamountfrom as lisakogus , pd.todate ,pd.fromdate from  " +
                  "   pricedisctable pd where  pd.fromdate<getdate() and pd.todate>getdate()-1  and pd.DATAAREAID = 'zp' AND ITEMRELATION like 'PP%' " +
                  "  union    select '' as custid  ,accountrelation as clgroup , itemrelation as prodid ,amount as price, quantityamountfrom as lisakogus , pd.todate ,pd.fromdate from " +
                  "   pricedisctable pd where  pd.fromdate<getdate()   and pd.DATAAREAID = 'zp' and ACCOUNTCODE = 1 AND ITEMRELATION like 'PP%' " +
                  "  order by 1,2,5" ;
	         
	         try {
	                  Connection ss = this.getNewConnection();
	        	      Statement stmt = ss.createStatement();
	                  ResultSet rs = stmt.executeQuery(sql);

	                  while (rs.next()) {

	                          String custID = rs.getString("custid");
	                          String clGroup = rs.getString("clgroup");
	                          String prodID = rs.getString("prodid");
	                          double price = rs.getDouble("price");
	                          int lisa = rs.getInt("lisakogus");
                              Date lopp = rs.getDate("todate");
	                          Date algus = rs.getDate("fromdate");
	                          MatrixLine mline = new MatrixLine();
	                        
	                          if (custID != null && custID.trim().length() > 0) {
	                              mline.setCustID(custID.trim());
	                                }
	                          if (clGroup != null && clGroup.trim().length() > 0) {
                                  mline.setCliGroup(clGroup.trim());
                              }
	                          if (prodID != null && prodID.trim().length() > 0) {
	                                  mline.setProdID(prodID.trim());
	                          }
	                    
                               mline.setStartDate(Converter.getYYYYpMMpDDpString(algus));
                               if(lopp.toString().indexOf("1900")  != -1 ){
                            	   mline.setEndDate(Converter.getYYYYpMMpDDpString(new java.util.Date())); 
                               }else{
                            	   mline.setEndDate(Converter.getYYYYpMMpDDpString(lopp)); 
                               }
                              
                             
	                          if (price != 0) {
	                                  mline.setPrice(price);
	                          }
	                         
	                          if(lisa > 0 ){
	                        	  mline.setLineinfo("KIEKIS: " + lisa + " kg.");
	                          }
	                           
	                          lines.add(mline);
	                  }
	                  
	                  rs.close();
	                  stmt.close();
	                  ss.close();

	          } catch (SQLException ex) {
	          ex.printStackTrace();
	          }
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
	 
	 
	  
    String cmd = "select personnelnumber as kood , w.name as name from  DirPArtyTable w  "
    		+ " inner join HCMWORKER h on h.person = w.recid " +
	          " inner join vendtable c on c.MAINCONTACTWORKER=h.recid " +
		      "	  group by  w.name, personnelnumber "; // OK

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
    Agent aa = new Agent ("HOUSE1");
    aa.setName("HOUSE1");
    agents.put("HOUSE1", aa);
    

    Agent aa1 = new Agent ("10000");
    aa1.setName("10000");
    agents.put("10000", aa1);
    
    if(sb2.length() > 2){
    	try{
    		 Converter.saveFileInUTF8( "agent.txt", sb2);
    	}catch (Exception e){
    	}
     }
    

   
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
  

	private TreeMap getPuudu(){
		TreeMap result = new TreeMap(); //sisse panna klint/punkt ja valmis string
		
		// 1 leiame kaunase analyysid viimased kliendi ja kuup�eva ja toor piima koguse - arvutame ymber 
		//    klient kuup�ev ja arvutatud kogus , tee objekt MilkContainer
		// 2 leiame 
		
    	
       HashMap<String,Vector<MilkContainer>> kpAnalyysid = new HashMap<String,Vector<MilkContainer>>();
       HashMap<String,Vector<MilkContainer>> zpAnalyysid = new HashMap<String,Vector<MilkContainer>>();
  	
		// kaks p�ringut 1 ZP analyysidega ja 1 KP anal��sidega  enne teema KP 
	    
	       
	      

        String puuduSQL = "select (case when a.vendlocationtype = 0 then '' else 'P'end) || a.vendlocid as klient, 'house2' as type " +
        		" ,'txt' as present, a.PURCHQTY as kogus ,a.transdate as kuup, a.transdate as kuup2, " +
        	"	a.inventlocationid as juht ,rieb as fat , balt as valk,c.name as nimi ,b.tyrvieta as tyyp " +
         " from   QUICKPURCHJOURNALTRANS2 a inner join   analysis b on a.analysisinventtransid = b.inventtransid " + 
       " inner join  inventlocation c on c.inventlocationid= a.inventlocationid  where a.dataareaid = 'zp' " +
       "  and a.transdate > getdate()-31 and b.dataareaid = 'zp'  and c.dataareaid = 'zp'  " +
       " order by a.vendlocid , a.transdate desc ,b.tyrvieta";
     try{
    	 Connection  ddd = getNewConnection();
         Statement stmr = ddd.createStatement();
         ResultSet rs1 = stmr.executeQuery(puuduSQL);
          String aaa = "";
           while(rs1.next()){
           	String klient = rs1.getString("klient").trim();
           	int kogus = (int)rs1.getDouble("kogus");
           	Date kuup = rs1.getDate("kuup");
            
               double fat = rs1.getDouble("fat");
           	double valk = rs1.getDouble("valk");
           	String tyyp  = rs1.getString("tyyp").trim();
           	// paneme KP asjad objekti klient kuup�ev ja valk rasv
           	if(tyyp.equals("KP")){
           	    MilkContainer mk = new MilkContainer();
           	    mk.setCustid(klient);
           	    mk.setKuupaev(kuup);
           	    mk.setFatOfficial(fat);
           	    mk.setProtOfficial(valk);
           	    if(kpAnalyysid.get(klient) != null){
           	    	Vector<MilkContainer> vv = kpAnalyysid.get(klient);
           	    	vv.add(mk);
           	    	kpAnalyysid.put(klient, vv);
           	    }else{
           	    	Vector<MilkContainer> vv = new Vector<MilkContainer>() ;
           	    	vv.add(mk);
           	    	kpAnalyysid.put(klient, vv);
           	    }
           	    
           	}else{
           	//zp analyysid ja kogused eraldi objekti 
           		MilkContainer mk = new MilkContainer();
             	    mk.setCustid(klient);
             	    mk.setKuupaev(kuup);
             	    mk.setFatOwn(fat);
             	    mk.setProtOwn(valk);
             	    mk.setMilkamount(kogus);
             	    
             	    if(zpAnalyysid.get(klient) != null){
             	    	Vector<MilkContainer> vv = zpAnalyysid.get(klient) ;
             	    	vv.add(mk);
             	    	zpAnalyysid.put(klient, vv);
             	    }else{
             	    	Vector<MilkContainer> vv = new Vector<MilkContainer>();
             	    	vv.add(mk);
             	    	zpAnalyysid.put(klient, vv);
             	    }
           	 	 }
              }
          rs1.close();
          stmr.close();
          
  		 
     } catch (Exception t){
    	 t.printStackTrace();
     }
     
    
     
     //N��d on meil 2 objekti koos zp ja Kp andmetega ja vaja lisada igale
     // zp elemendile vastavad Kaunase analyysid et saaks t�idetud 
    Iterator itera =  kpAnalyysid.keySet().iterator();
    while(itera.hasNext()){
    	String klient = (String)itera.next();
    	Vector<MilkContainer> vv = kpAnalyysid.get(klient);
    	vv = upside(vv);
    	for(int y = 0;y<vv.size();y++){
    		MilkContainer mk = vv.elementAt(y);
    		if(zpAnalyysid.get(klient) != null){
    			Vector<MilkContainer> vk = zpAnalyysid.get(klient);
    	    	for(int z = 0;z<vk.size();z++){
    	    		MilkContainer m = vk.elementAt(z);
    	    		if(mk.getKuupaev().before(m.getKuupaev())){
    	    			m.setFatOfficial(mk.getFatOfficial());
                     	m.setProtOfficial(mk.getProtOfficial());
                     //	System.out.println("Lisasin kaunase " + m.getKuupaev() + " " + mk.getKuupaev() );
    	    		}
    	    	}
    		}
    	}
    }
    
    
    
    HashMap jooksev = new HashMap();  // jooksev kuu 
    HashMap paeva30 = new HashMap(); // viimased 30 p�eva      
    HashMap dekaad = new HashMap();  // esimesed 15 p�eva 
    HashMap paevad = new HashMap();
	
    
   itera =  zpAnalyysid.keySet().iterator();
    while(itera.hasNext()){
    String klient = (String)itera.next();
    if(zpAnalyysid.get(klient) != null){
		Vector<MilkContainer> vk = zpAnalyysid.get(klient);
    	for(int z = 0;z<vk.size();z++){
    		MilkContainer m = vk.elementAt(z);
    	
    		
    		if(isCurrentM(m.getKuupaev())){
    			if( jooksev.get(m.getCustid()) != null){
    				Toode tt =(Toode)jooksev.get(m.getCustid());
        			tt.setKogus(tt.getKogus() + m.getMilkamount());
        			tt.setKastis(tt.getKastis() + m.getVaheRBA());
        			 jooksev.put(m.getCustid(),tt);
    			}else{
    				Toode tt = new Toode("id");
        			tt.setKogus(m.getMilkamount());
        			tt.setKastis(m.getVaheRBA());
        			 jooksev.put(m.getCustid(),tt);	
    			}
    		}
    			
    			if(isDekad(m.getKuupaev())){
        			if( dekaad.get(m.getCustid()) != null){
        				Toode tt =(Toode)dekaad.get(m.getCustid());
            			tt.setKogus(tt.getKogus() + m.getMilkamount());
            			tt.setKastis(tt.getKastis() + m.getVaheRBA());
            			dekaad.put(m.getCustid(),tt);
        			}else{
        				Toode tt = new Toode("id");
            			tt.setKogus(m.getMilkamount());
            			tt.setKastis(m.getVaheRBA());
            			dekaad.put(m.getCustid(),tt);	
        			}
    		}
            
    		
        			if( paeva30.get(m.getCustid()) != null){
        				Toode tt =(Toode)paeva30.get(m.getCustid());
            			tt.setKogus(tt.getKogus() + m.getMilkamount());
            			tt.setKastis(tt.getKastis() + m.getVaheRBA());
            			paeva30.put(m.getCustid(),tt);
        			}else{
        				Toode tt = new Toode("id");
            			tt.setKogus(m.getMilkamount());
            			tt.setKastis(m.getVaheRBA());
            			paeva30.put(m.getCustid(),tt);	
        			}
    		
    			
    			if( paevad.get(m.getCustid()) != null){
    				String tt =(String)paevad.get(m.getCustid());
    				paevad.put(m.getCustid(),tt + "|" + m.getKuupaev() + "    " + (int)m.getMilkamount() + " kg. " + m.getVahe() );
    			}else{
    				paevad.put(m.getCustid(), m.getKuupaev() + "   " + (int)m.getMilkamount() + " kg. " + m.getVahe() );	
    			}
            }	
        }
    }    		

    NumberFormat nf = NumberFormat.getInstance();
       
 //   System.out.println("Sidu " + paeva30);
    nf.setMinimumFractionDigits(2);
    nf.setMaximumFractionDigits(2);
    nf.setGroupingUsed(false);
  itera = paeva30.keySet().iterator();
   while (itera.hasNext()){
	   String klient = itera.next() + "";
	   String asi = "";
	   if(jooksev.get(klient) != null){
			Toode tt =(Toode)jooksev.get(klient); 
			asi =        "Current " + (int)(tt.getKogus()) + " kg. " + nf.format(tt.getKastis()) + "|"; 
			
	   }
	   if(dekaad.get(klient) != null){
			Toode tt =(Toode)dekaad.get(klient); 
			asi = asi +  "Dekad   " + (int)(tt.getKogus()) + " kg. " + nf.format(tt.getKastis()) + "|"; 
	   }
	   if(paeva30.get(klient) != null){
			Toode tt =(Toode)paeva30.get(klient); 
			asi = asi +  "30 dien " + (int)(tt.getKogus()) + " kg. " + nf.format(tt.getKastis()) + "| |"; 
	   }
	   if(paevad.get(klient) != null){
			
			asi = asi + paevad.get(klient) + "|"; 
	   }
	   result.put(klient, klient + ";txt;house3;" + asi);
   }
    
		return result;
	}
	
	
	private boolean isCurrentM(Date dd){
	     Date t = new Date();	 
		 if (t.getMonth() == dd.getMonth() && dd.getYear() == t.getYear()){
			 return true ;
		 }
	     
		return false;
	}
	
	private boolean isDekad(Date dd){
	     Date t = new Date();	 
		 if (t.getMonth() == dd.getMonth() && dd.getYear() == t.getYear()&& dd.getDate() < 16){
			 return true ;
		 }
	     
		return false;
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
	
	

	  public TreeMap getCustomers(Agent agent){
		  
		  String inP = parent.getProps().getProperty("doc.origpath");
	      String outP = parent.getProps().getProperty("doc.destination");
	      HashSet codes = new HashSet();
		  	  
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
   " , cus.bankcentralbankpurposetext as volainfo , cus.Enterprisecode as bday " +
 " FROM  vendtable cus  inner join DirPartyTable dp ON cus.partY=dp.REcid  " +
" INNER JOIN LOGISTICSPOSTALADDRESS adr ON adr.LOCATION = dp.PRIMARYADDRESSLOCATION and getdate() >=VALIDFROM  and  getdate() <= VALIDTO " +
 "  left  JOIN  paymterm   pay on cus.paymtermid= pay.paymtermid " +
	" and pay.DATAAREAID = cus.DATAAREAID and  pay.dataareaid='zp'  left  JOIN  custtable  asi  on asi.vendaccount= cus.accountnum  " +
 " and asi.DATAAREAID = cus.DATAAREAID and  asi.dataareaid='zp'   WHERE   cus.dataareaid='zp' AND cus.blocked != 2   and stationid_bbs > 0" +
  " union " +
 "SELECT 'P' + stationid + ' ' +dim.description as name,'' as  nimelisa,'' as namealias,'P' + stationid as accountnum,'' as  payer, '' as linedisc ,'Lithuania' as address ," +
	"(select personnelnumber as kood from  DirPArtyTable w   inner join HCMWORKER h on h.person = w.recid where h.recid = dim.responsible ) AS agent ," +
   " 0 as  term, ''  as  ph1 , ''  as  em1, ''  as  ph2, ''  as  fax, ''  as  em2 ,'' as  driver ,   '' as  grupp,'' as  prgrupp  " +
"	,  '' as  regnum	, '' as vatnum , 'Somewhere' as  state , 'County' as  county,'' as taxgroup,'' as encode ,'' as economyid,''  as agreement, '' as volainfo,'' as bday " +
" FROM  BBSStationTable dim " +
" where  description  like '%ST.P.%' 	and dataareaid = 'zp'"; // OK
	 
	 String order = "select ve.accountnum as cust " + 
	 		"    from  custtable  cus inner join Vendtable ve on ve.enterprisecode = cus.enterprisecode and cus.dataareaid = ve.dataareaid " + 
	 		"	 left join DimensionAttributeValueSetItemView ww on ww.DimensionAttributeValueSet = cus.defaultdimension  " + 
	 		"    where    cus.dataareaid='zp' ";
	 
	 
	 String ccc = "select invoiceaccount from  vendtable where dataareaid='zp' and invoiceaccount is not null and len (invoiceaccount) > 0  group by invoiceaccount";//OK
	 
	         String sql = "  SELECT  cus.VENDACCOUNT AS klient,a.invoice AS numbe,co.duedate AS maksepaev,sum(co.amountmst) AS tasumata, " + 
                   " co.transdate AS kuupaev , datediff(day,co.duedate ,getdate())  AS yle , max(a.bankcentralbankpurposetext) as txt FROM  custtransopen co  " +
				   " INNER JOIN  custtrans a ON co.REFRECID=a.recid and  co.DATAAREAID=a.DATAAREAID " +
                " INNER JOIN  custtable cus on cus.accountnum =  a.ORDERACCOUNT and cus.dataareaid = 'zp' " +   
                 "    WHERE co.dataareaid='zp' AND a.dataareaid='zp'	 and  cus.custgroup = '164PaskUk' and LEN(cus.VENDACCOUNT) > 1 " +
				 " group by a.invoice, cus.VENDACCOUNT,co.duedate, co.transdate  ORDER BY co.transdate,a.invoice, cus.VENDACCOUNT,co.duedate"; //OK
				  		
	           
	           String salessql = "select x01_punktas as ladu ,X02_knygelesnr as klient ,X03_data as kuupaev , X08_kiekisba as kogus ,  " +
               "  round (X10_bazinesuma/X08_kiekisba,2) as hind,X04_riebproc as fat ,X05_baltproc as protein, x23_sl as sl, x24_BBU as bbu,"
               + " x25_INH as inha, x26_H2O as h2o " +
               "  from  BBSAnalysisGBI where dataareaid = 'zp' and X03_data > getdate()-360 and X08_kiekisba > 0 and X10_bazinesuma > 0 order by 1,2,3 desc " ;//OK
	            
	         
	           String salessqlLadu = "select x01_punktas as ladu  ,X03_data as kuupaev , sum(X08_kiekisba) as kogus , " +
               " round(avg(X10_bazinesuma/X08_kiekisba),2) as hind,round(avg(X04_riebproc),2) as fat ,round(avg(X05_baltproc),2) as protein " +
               " from  BBSAnalysisGBI where dataareaid = 'zp' and X03_data > getdate()-360  "+
               "  and X08_kiekisba > 0 and X10_bazinesuma > 0 group by x01_punktas, X03_data order by 1,2,3 desc"; //OK
	           
	        
	           String sss = "SELECT  accountnum, sum(case when co.duedate<getdate() then amountmst else 0 end) as asi , " +
	           		" sum(amountmst) as asi2 FROM  vendtransopen co " + 
	           	    " WHERE co.dataareaid='zp'   group by  accountnum having " + 
	           	    " sum(case when co.duedate<getdate() then amountmst else 0 end) != 0 or sum(amountmst) != 0 "; //OK
	           
	           String house = "  select accountnum as klient , ltrim(str(inactive_bbs)) + '- Aktyvus|' + ltrim(str(daily_bbs)) + '- Eksperimentininkas|' + " + 
				" ltrim(str(cowscount_bbs)) + '- Karvi11146xx skai11128xxius|' + ltrim(str(farmer_bbs)) + '- Stacionarus/ukininkas|'  " +
				 "  as info from VendTable where DATAAREAID = 'zp'"; // OK
	         
	       
	           
	           String  house2 = "SELECT 'P' + dim.inventlocationid as klient  , 'house' as type  ,'txt' as present " +
	         "  	,  str(SUM(AVAILPHYSICAL))+' - '+ summ.itemid + ' ' + pr.namealias AS info FROM  INVENTSUM summ " +   
             "   INNER JOIN  inventtable pr on pr.itemid=summ.itemid and   pr.DATAAREAID ='prod'  " +
             "   INNER JOIN  INVENTDIM dim ON  summ.inventdimid= dim.inventdimid  " +
             "    INNER JOIN  inventtablemodule inv ON inv.itemid=summ.itemid  " +
             "    WHERE  summ.DATAAREAID ='zp'  AND CLOSED = 0 AND  moduletype=0 " +
             "     and dim.inventlocationid not like'%PROD%'  and summ.itemid  like 'M%' " +
             "    GROUP BY dim.inventlocationid,summ.itemid,pr.namealias  ORDER BY dim.inventlocationid,pr.namealias,summ.itemid ";
               //OK      
	           
	  if (isDebug()) System.out.println(cmd);
	    TreeMap customers = new TreeMap();
	    TreeMap custMap = new TreeMap();
	    TreeMap custDebt = new TreeMap();
	    TreeMap taraDebt = new TreeMap();
	    TreeMap totalDebt = new TreeMap();
	    TreeMap custKlp = new TreeMap();
	    TreeMap houseDat = new TreeMap();
	    TreeMap puuduDat = new TreeMap();
	    TreeMap puuduTotDat = new TreeMap();//getPuudu();
	    Vector payers =  new Vector();
	    HashMap salesres = new HashMap();
	    HashMap labres = new HashMap();
	    HashMap labrest = new HashMap();

	    HashMap labres2 = new HashMap();
	    HashMap labres3 = new HashMap();
	    StringBuffer sb1 = new StringBuffer();
	    try {
	    	
	  Connection ddd = getNewConnection();
	       Statement stmr = ddd.createStatement();
	       ResultSet  rs1 = stmr.executeQuery(salessql);
	        while(rs1.next()){
	        	String ladu = rs1.getString("ladu").trim();
	        	String klient = rs1.getString("klient").trim();
	        	String kuu = rs1.getString("kuupaev").substring(0,10);
	        	int kogus = (int)rs1.getDouble("kogus");
	        	double hind = rs1.getDouble("hind");
	        	double fat = rs1.getDouble("fat");
	        	double valk = rs1.getDouble("protein");
	        	double sl = rs1.getDouble("sl");
	        	double bbu = rs1.getDouble("bbu");
	        	String inh = rs1.getString("inha");
	        	double h20 = rs1.getDouble("h2o");
	        	
		          if(labrest.get(klient) == null){
		        	  labrest.put(klient, kuu + " :| sl: " + (int)sl + "  bbu:" + (int)bbu + "  inh: " +inh + "  H2O: " + (int)h20 );
		          }else{
		        	  String asi = (String)labrest.get(klient);     	  
		        	  labrest.put(klient,  asi + " |" + kuu + " :| sl:  " + (int)sl + "  bbu:" + (int)bbu + "  inh: " +inh + "  H2O: " + (int)h20  );
		          }
	        
	          if(salesres.get(klient) == null){
	        	  salesres.put(klient,"   " + kuu + " : " + kogus + "," + hind + " " );
	          }else{
	        	  String asi = (String)salesres.get(klient);     	  
	        	  salesres.put(klient,"   " + kuu + " : " + kogus + "," + hind + " |" + asi);
	          }
	          
	          if(labres.get(klient) == null){
	        	  labres.put(klient,"   " + kuu + " : " + fat + "," + valk + " " );
	          }else{
	        	  String asi = (String)labres.get(klient);     	  
	        	  labres.put(klient,"   " + kuu + " : " + fat + "," + valk + " |" + asi);
	          }
	        }
	       rs1.close();
	       rs1 = stmr.executeQuery(salessqlLadu);
	        while(rs1.next()){
	        	String klient = "P" +rs1.getString("ladu").trim();
	        	
	        	String kuu = rs1.getString("kuupaev").substring(0,10);
	        	int kogus = (int)rs1.getDouble("kogus");
	        	double hind = rs1.getDouble("hind");
	        	double fat = rs1.getDouble("fat");
	        	double valk = rs1.getDouble("protein");
	        
	          if(salesres.get(klient) == null){
	        	  salesres.put(klient,"   " + kuu + " : " + kogus + "," + hind + " " );
	          }else{
	        	  String asi = (String)salesres.get(klient);     	  
	        	  salesres.put(klient,"   " + kuu + " : " + kogus + "," + hind + " |" + asi);
	          }
	          
	          if(labres.get(klient) == null){
	        	  labres.put(klient,"   " + kuu + " : " + fat + "," + valk + " " );
	          }else{
	        	  String asi = (String)labres.get(klient);     	  
	        	  labres.put(klient,"   " + kuu + " : " + fat + "," + valk + " |" + asi);
	          }
	        }
	       rs1.close();
	        
	       
	       String lab2sql = "select  orderaccount as klient , iv. proddate as kuup, " +
				 " (Select top 1 pdsbatchattribvalue from PdsBatchAttributes pa where pa.InventBatchId = iv.InventBatchId and pa.analysisplace_bbs  = 'ZP' and pa.pdsbatchattribid = 'RP') as fat , " +
				 " (Select top 1 pdsbatchattribvalue  from PdsBatchAttributes pa where pa.InventBatchId = iv.InventBatchId and pa.analysisplace_bbs  = 'ZP' and pa.pdsbatchattribid = 'BP' ) as protein " + 
				" from vendinvoicetrans  tra INNER JOIN  vendinvoicejour jou ON   tra.invoiceid=  jou.invoiceid and  tra.DATAAREAID= jou.DATAAREAID	" +
	" inner JOIN Inventdim dim ON tra.InventDimId = dim.InventDimId and 'prod' = dim.DATAAREAID	  inner JOIN  InventBatch iv on dim.InventBatchId = iv.InventBatchId and iv.DATAAREAID = tra.DATAAREAID " +
	" where  iv.itemid = 'P'  and iv.proddate > getdate()-30  order by 1,2 desc,3 desc " ; //OK
	   	               
	   	   
	     rs1 = stmr.executeQuery(lab2sql);
	     String oklient = null;
	     int cou = 0;
	        while(rs1.next()){
	        	//String ladu = rs1.getString("ladu").trim();
	        	String klient = rs1.getString("klient").trim();
	        	boolean goes = true ;
	        	if(oklient == null){
	        		oklient = klient;
	        	}
	        	
	        	if(!oklient.equals(klient)){
	        		cou = 0;
	        		goes = true;
	        	}
	          if(cou == 10 ){
	        		goes  = false;
	        	}
	        	cou++;
	        	String kuu = rs1.getString("kuup").substring(0,10);
	        	String uu = rs1.getString("fat");
	        	if(uu != null && uu.trim().length() > 0){
	        	
	        	double fat = Double.parseDouble(uu);
	        	double valk = rs1.getDouble("protein");
	       
	          if(goes && (fat > 0 || valk > 0)){
	        	  if(labres2.get(klient) == null){
		        	  labres2.put(klient,"   " + kuu + " : " + fat + "," + valk + " " );
		          }else{
		        	  String asi = (String)labres2.get(klient);     	  
		        	  labres2.put(klient,"   " + kuu + " : " + fat + "," + valk + " |" + asi);
		          }  
	          }
	      	
	        	}
	          
	        }
	       rs1.close();
	     //  stmr.close();
	    
	       
	       String lab2ladu = "  select 'P' + tt.klient as klient ,  tt.kuup,   " +
	     " round(sum( cast (purchqty as DECIMAL(9,2)) * cast (rieb as DECIMAL(9,2))/3.4) *3.4/sum( cast (purchqty as DECIMAL(9,2))),2) as fat , " +
		"	 round((sum(  cast (purchqty as DECIMAL(9,2)) *(1+(cast (rieb as DECIMAL(9,2))-3.4)*0.178+(cast (balt as DECIMAL(9,2))-3)*0.267)) " +
	    "    -sum( cast (purchqty as DECIMAL(9,2)))*(0.178*(sum(  cast (purchqty as DECIMAL(9,2)) *cast (rieb as DECIMAL(9,2))/3.4) *3.4/sum(  cast (purchqty as DECIMAL(9,2))))-0.4062 " +
	    "     ))/(sum(  cast (purchqty as DECIMAL(9,2)))*0.267),2)  as prot " +
	    " from  ( select ost.stationid as klient,  ost.transdate as kuup, purchqty  as purchqty," +
	  	" (Select top 1 pdsbatchattribvalue  from PdsBatchAttributes pa where pa.InventBatchId = ost.InventBatchTo " +
	"		   and pa.analysisplace_bbs  = 'ZP' and pa.pdsbatchattribid = 'RP')" +
	"		 as rieb ,(Select top 1  pdsbatchattribvalue  from PdsBatchAttributes pa where pa.InventBatchId = ost.InventBatchTo " +
	"		   and pa.analysisplace_bbs  = 'ZP' and pa.pdsbatchattribid = 'BP')" +
"			  as balt   from  BBsMilkJournalTrans ost where ost.dataareaid ='zp' and ost.transdate > getdate() -15 " +
"	         and purchqty  != 0 and len(ost.stationid) > 0) as tt where ISNUMERIC(tt.rieb)= 1" +
"			 group by tt.klient,tt.kuup order by 1,2 desc,3  ";       //OK
	       	       
	       
	       rs1 = stmr.executeQuery(lab2ladu);
		      oklient = null;
		      cou = 0;
		        while(rs1.next()){
		        	//String ladu = rs1.getString("ladu").trim();
		        	String klient = rs1.getString("klient").trim();
		        	boolean goes = true ;
		        	if(oklient == null){
		        		oklient = klient;
		        	}
		        	
		        	if(!oklient.equals(klient)){
		        		cou = 0;
		        		goes = true;
		        	}
		          if(cou == 10 ){
		        		goes  = false;
		        	}
		        	cou++;
		        	String kuu = rs1.getString("kuup").substring(0,10);
		        	double fat = rs1.getDouble("fat");
		        	double valk = rs1.getDouble("prot");
		       
		          if(goes){
		        	  if(labres3.get(klient) == null){
			        	  labres3.put(klient,"   " + kuu + " : " + fat + "," + valk + " " );
			          }else{
			        	  String asi = (String)labres3.get(klient);     	  
			        	  labres3.put(klient,"   " + kuu + " : " + fat + "," + valk + " |" + asi);
			          }  
		        	  
		        	//  System.out.println("   " + kuu + " : " + fat + "," + valk + " " + klient);
		          }
		          
		        }
		       rs1.close();
		       
		       HashSet orderOK = new HashSet();
		       
		       rs1 = stmr.executeQuery(order);
			      oklient = null;
			      cou = 0;
			        while(rs1.next()){
			        	String klient = rs1.getString("cust");
			        	if(klient != null && klient.trim().length() > 0) {
			        		orderOK.add(klient.trim());
			        	}
					       	
			        }
			        	System.out.println("orderOK " + orderOK.size());
		       
		       
		       
		       stmr.close();
		    
		    
	       
	      

	        String puuduSQL = "select (case when a.vendlocationtype = 0 then '' else 'P'end) || a.vendlocid as klient, 'house2' as type  ,'txt' as present, a.PURCHQTY as kogus ,a.transdate as kuup, " +
	        	"	a.inventlocationid as juht ,rieb as fat , balt as valk,c.name as nimi ,b.tyrvieta as tyyp " +
             " from   QUICKPURCHJOURNALTRANS2 a inner join   analysis b on a.analysisinventtransid = b.inventtransid " + 
           " inner join  inventlocation c on c.inventlocationid= a.inventlocationid  where a.dataareaid = 'zp' " +
           "  and a.transdate > getdate()-30 and b.dataareaid = 'zp'  and c.dataareaid = 'zp'  " +
           " order by a.vendlocid , a.transdate desc ,b.tyrvieta";
	        
	        puuduSQL = " select  tt.klient as klient , 'house2' as type  ,'txt' as present,purchqty as kogus , tt.kuup as kuup , '' as juht2 ,  " +
	"     round(sum( cast (purchqty as DECIMAL(9,2)) * cast (rieb as DECIMAL(9,2))/3.4) *3.4/sum( cast (purchqty as DECIMAL(9,2))),2) as fat , " +
	"	 round((sum(  cast (purchqty as DECIMAL(9,2)) *(1+(cast (rieb as DECIMAL(9,2))-3.4)*0.178+(cast (case when balt = '' then '3.33' else balt end as DECIMAL(9,2))-3)*0.267)) " +
	"        -sum( cast (purchqty as DECIMAL(9,2)))*(0.178*(sum(  cast (purchqty as DECIMAL(9,2)) " +
 "			*cast (rieb as DECIMAL(9,2))/3.4) *3.4/sum(  cast (purchqty as DECIMAL(9,2))))-0.4062 " +
"       ))/(sum(  cast (purchqty as DECIMAL(9,2)))*0.267),2)  as valk , max(juht) as nimi , '' as tyyp " + 
"	     from  (  select (case when ost.typefrom = 1 then '' else '' end ) + " +
"		  ost.codefrom as klient,  ost.transdate as kuup, purchqty  as purchqty, " +
"	  	 (Select top 1 pdsbatchattribvalue  from PdsBatchAttributes pa where pa.InventBatchId = ost.InventBatchTo " +
"	   and pa.analysisplace_bbs  = 'ZP' and pa.pdsbatchattribid = 'RP') " +
"		 as rieb ,(Select top 1  pdsbatchattribvalue  from PdsBatchAttributes pa where pa.InventBatchId = ost.InventBatchTo " +
"			   and pa.analysisplace_bbs  = 'ZP' and pa.pdsbatchattribid = 'BP') " +
"			  as balt, ( select name from inventlocation where inventlocationid = ost.inventlocationidto)  as juht  " +
"			  from  BBsMilkJournalTrans ost where ost.dataareaid ='zp' and ost.transdate > getdate() -15 and purchqty  != 0 ) as tt where ISNUMERIC(tt.rieb)= 1 " +
"			 group by tt.klient,tt.kuup,tt.purchqty order by 1,2 desc,3";
	       
	       stmr = ddd.createStatement();
	       rs1 = stmr.executeQuery(puuduSQL);


	       while(rs1.next()){
	        	String klient = rs1.getString("klient").trim();
	        	String type = rs1.getString("type").trim();
	        	String present = rs1.getString("present").trim();
	        	int kogus = (int)rs1.getDouble("kogus");
	         	String kuu = rs1.getString("kuup").substring(5,10);
	         
	            double fat = rs1.getDouble("fat");
	        	double valk = rs1.getDouble("valk");
	        	String juht = rs1.getString("nimi").trim();
	        	String tyyp  = rs1.getString("tyyp").trim();
	        	
	        		String content = " |" +kuu + " " + juht.replaceAll("Vair.", " ").replaceAll("vair.", " ").trim()
	        		+ "|   (" + kogus + "kg.) [ R:" + fat +  " B:" + valk + " ]|"  ; 

		        	if(puuduDat.get(klient) != null){
		        		String asi = puuduDat.get(klient) + "";
		        		puuduDat.put(klient, asi + "|"  + content);
		        		puuduDat.put("P"+klient, asi + "|"  + content);
		        	}else{
		        		puuduDat.put(klient, klient + ";" + type + ";" + present + ";" + content);	
		        		puuduDat.put("P"+klient, "P"+klient + ";" + type + ";" + present + ";" + content);	
			        	}
		    	        }
	        
	        System.out.println("house 2 " + puuduDat.size());
	       rs1.close();
	       stmr.close();
	       
	    
	       
	    
	       stmr = ddd.createStatement();
	       rs1 = stmr.executeQuery(house);
	        while(rs1.next()){
	        	String klient = rs1.getString("klient").trim();
	            String content = rs1.getString("info").trim();
	            if(labrest.get(klient) != null) {
	            	content = content + "|" +labrest.get(klient);
	            }
	        		houseDat.put(klient, klient + ";house;txt;" + content);	
	        		
	         }
	       rs1.close();
	       stmr.close(); 
	       
	       stmr = ddd.createStatement();
	       rs1 = stmr.executeQuery(house2);
	        while(rs1.next()){
	        	String klient = rs1.getString("klient").trim();
	            String content = rs1.getString("info").trim();
	        	houseDat.put(klient, klient + ";house;txt;" + content);	
	        }
	       rs1.close();
	       stmr.close();
	       
	       Statement stmt = ddd.createStatement();
	       ResultSet rs = stmt.executeQuery(salessql);
	       TreeMap<String,Double> laenud =  new TreeMap<String,Double>();
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
             if(laenud.get(debt.getCustID())!= null){
            	 double asi = laenud.get(debt.getCustID());
            	 laenud.put(debt.getCustID(), debt.getSum() + asi);
             }else{

            	 laenud.put(debt.getCustID(), debt.getSum());
             }
             
           }
           rs.close();
           stmt.close();
           ddd.close();
           
	       
           ddd = getNewConnection();
	       Statement stmu = ddd.createStatement();
	    	
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
	       ddd.close();
	       
	       ddd = getNewConnection();
	        stmr = ddd.createStatement();
	    	 rs1 = stmr.executeQuery(ccc);
	        while(rs1.next()){
	        	
	       payers.add(rs1.getString("invoiceaccount"));
	          
	        }
	       rs1.close();
	       stmr.close();
             ddd.close();
	       
	       ddd = getNewConnection();
	    	
	    	 stmt = ddd.createStatement();
	         rs = stmt.executeQuery(cmd);
	    
	      while(rs.next()){
	        String name = rs.getString("name").replaceAll("UAB", " ").trim();
	        
	        
	        String alias = rs.getString("nimelisa");
	        if(alias != null && alias.trim().length()>0 && !alias.trim().equals(name)){
	        	if(!name.trim().startsWith(alias.trim()))
	        	name =  name + " " + alias ;
	        }
	        
	        
	        String accountnum = rs.getString("accountnum").trim();
	        codes.add(accountnum);
	        Customer cust = new Customer(accountnum);
	      
	        name = name + " " + accountnum;
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
	       
	       String debtStr = null;
	        try{
	        	  debtStr = (String)custDebt.get(payer);
	  	        if (debtStr != null){
	  	          cust.setDebt(Double.parseDouble(debtStr));
	  	        }
	  	        String totalStr = (String)totalDebt.get(payer);
	  	        if (totalStr != null){
	  	          cust.setUsedCredit(Double.parseDouble(totalStr));
	  	        }
	        }catch(Exception e){
	        	//e.printStackTrace();
	        }
	      
	   if (laenud.get(accountnum) != null){
		   cust.setDebt(laenud.get(accountnum));
	   }
	        String aleke = rs.getString("linedisc");
	        if(aleke != null && aleke.trim().length()>0){
	            aleke = aleke.trim();
	            cust.setGroup(aleke);
	        } else {
	            cust.setGroup(accountnum.substring(1));
	        	aleke = "000" ;
	        }
	        
	        if(salesres.get(accountnum) != null){
	        	cust.addToCustDataVec(cust.getID()+ ";sales;chart;" + "CHART SUPIRKIMAS :|Menuo ir dekada|Pirkti KG|Kaina|Kg|Eur.|0|0|" + salesres.get(accountnum));
	        }
	        
	        if(labres.get(accountnum) != null){
	        	cust.addToCustDataVec(cust.getID()+ ";lab;chart;" + "CHART LABTEST KA :|Menuo ir dekada|R %|Ba %|%|%|0.8|0.4|" + labres.get(accountnum));
	        }
	        
	        if(labres2.get(accountnum) != null){
	        	cust.addToCustDataVec(cust.getID()+ ";lab2;chart;" + "CHART LABTEST ZP:|Data|R %|Ba %|%|%|0.8|0.4|" + labres2.get(accountnum));
	        }
	        
	        if(labres3.get(accountnum) != null){
	        	cust.addToCustDataVec(cust.getID()+ ";lab2;chart;" + "CHART LABTEST ZP:|Data|R %|Ba %|%|%|0.4|0.2|" + labres3.get(accountnum));
	        }

	        if(!orderOK.contains(accountnum)) {
	        	cust.noOrder = true;
	        }else {
	        	cust.diffBack2 = true;
	        }
	        
	        String aadr = rs.getString("address");
	        if (aadr != null) {
	          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ')),"Lithuania").replaceAll("\\r\\n|\\r|\\n", " "));
	        }
	        
	        String agen = rs.getString("agent");
	      
	        if (agen != null && agen.trim().length() >0) {
	          cust.addAgent(new Agent(agen.trim()));
	          cust.setMainAgent(agen.trim());
	          
	          cust.addAgent(new Agent("11959"));
	          cust.addAgent(new Agent("13462"));
	          cust.addAgent(new Agent("10000"));

	          cust.addAgent(new Agent("13720"));
	          
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
	        
	        String fax = rs.getString("fax");
		        if (fax != null && fax.trim().length()>0) {
		      //    persd = persd  + fax.trim() + "|";
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
	        
	        if(houseDat.get(cust.getID()) != null){
	        	cust.addToCustDataVec(houseDat.get(cust.getID()) + "");
	        }
	    
	        if(puuduDat.get(cust.getID()) != null){
	        	cust.addToCustDataVec(puuduDat.get(cust.getID()) + "");
	        }
	        
	        if(puuduTotDat.get(cust.getID()) != null){
	        	cust.addToCustDataVec(puuduTotDat.get(cust.getID()) + "");
	        }
	        
	        String driver = rs.getString("driver");
	       
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
	        
	        
	        if (driver != null && driver.trim().length()>0) {
		         // cust.setPersonalData(driver.replaceAll("\\r\\n|\\r|\\n", " ").trim());
		          cust.setCity(cust.getCity() +"Vairuotojas :"+driver.trim() + "|");
		        }
	        String vatnum = rs.getString("vatnum");
	        if (vatnum != null && vatnum.trim().length()>0) {
	        	cust.setCity(cust.getCity() +"PVM kodas :"+vatnum.trim().replaceAll("LT", "LT'") + "'|");
	        }
	        
	        String state = rs.getString("state");
	        if (state != null && state.trim().length()>0) {
	        	cust.setCity(cust.getCity() +"Apskrities kodas : "+state.trim() + "|");
	        }
	        
	        String county = rs.getString("county");
	        if (county != null && county.trim().length()>0) {
	        	cust.setCity(cust.getCity() + "Rajono kodas : " + county.trim() + "|");
	        }
	      
	        
	        String taxgroup = rs.getString("taxgroup");
	        if (taxgroup != null && taxgroup.trim().length()>0) {
	        	cust.setCity(cust.getCity() + Converter.enCodeEst("PVM grupes :") + taxgroup.trim() + "|");
	        }
	        
	        String economyid = rs.getString("economyid");
	        if (economyid != null && economyid.trim().length()>0) {
	        	cust.setCity(cust.getCity() + Converter.enCodeEst("�kio kodas :'") + economyid.trim() + "'|");
	        }
	        String agreement = rs.getString("agreement");
	        if (agreement != null && agreement.trim().length()>0) {
	        	cust.setCity(cust.getCity() + "Sutartis :'" + agreement.trim().replace('.', '-')
	        			.replace('.', '-').replaceAll("     ", "' '").replaceAll("    ", "' '").replaceAll("   ", "' '") + "'|");
	        }
	        String volainfo = rs.getString("volainfo");
	        if (volainfo != null && volainfo.trim().length()>0 && debtStr != null ) {
	        	cust.setCity(cust.getCity() + "Paskolos informacija :'" + volainfo.trim().replace('.', '-')
	        			.replace('.', '-') + "'|");
	        //	cust.setDebt(-1);
	        }
	        
	        
	        String bday = rs.getString("bday");
	        if (bday != null && bday.trim().length() > 0 &&  Converter.isPersonalcode(bday.trim()) ) {
	        	cust.setRegnr(bday.trim());
	        }
	        
	        
	        cust.setCity(Converter.enCodeEst(cust.getCity()).replaceAll("\\r\\n|\\r|\\n", " ").trim());
	        
	     	 
	                    boolean laks = true;
	                    if((payer == null || payer.trim().length() == 0) && payers.contains(cust.getID())){
	                    	//laks = false;
	                    }
	                    if(laks){
	                        customers.put(name, cust);
	                        custMap.put(cust.getID(),cust);	
	                    }
	      }
	      
	      Customer nn = new Customer("naujas");
	      nn.setName("  naujas ūkininkas");
	      nn.setAddress("somewhere");;
	      Enumeration enume = getParent().getAgentsHash().elements();
          while (enume.hasMoreElements()) {
            Agent a = (Agent) enume.nextElement();
              nn.addAgent(a);
          }
	      
	      customers.put("  naujas ūkininkas", nn);
          custMap.put("naujas",nn);	
	      
             rs.close();
             stmt.close();
            
       
             
             
             
             String fff = " select custaccount as klient,name as nimi ,  \r\n" + 
             		"   case when birthyear  > 1930 then CONVERT(date, ( CAST(birthyear AS varchar) +'-'+ CAST(birthmonth AS varchar)  +'-'+ CAST(birthday AS varchar) ), 126) else null end \r\n" + 
             		"     as synna , (select l.locator  from  LOGISTICSELECTRONICADDRESS l  where l.recid = dp.primarycontactemail)  as email, \r\n" + 
             		"   (select l.locator  from  LOGISTICSELECTRONICADDRESS l  where l.recid = dp.primarycontactphone)  as phone , ve.Enterprisecode as bday \r\n" + 
             		"   from  Contactperson co inner join DirPartyTable dp ON co.partY=dp.REcid \r\n" + 
             		"    right JOIN custtable ve on ve.accountnum = custaccount and ve.dataareaid= co.dataareaid\r\n" + 
             		"    where co.dataareaid='zp' "; //OK
            

             try{
                 StringBuffer sb2 = new StringBuffer();
          	   Statement stm = ddd.createStatement();
          	   rs1 = stm.executeQuery(fff);
                 while(rs1.next()){
              	   String klient = rs1.getString("klient").trim();
              	   String nimi = rs1.getString("nimi").trim();
              	   java.sql.Date synna = rs1.getDate("synna");
              	  String email = parent.make(rs1.getString("email"));
              	  String phone = parent.make(rs1.getString("phone"));
              	  long lll = 0;
              	  if(synna != null){
              		  lll = synna.getTime();
              	  }
                  sb2.append(klient+";"+ nimi + ";" + lll + "; ;" + phone  + ";" + email +  "\n");
                 }
                rs1.close();  
                stm.close();
          
                if(sb2.length() > 2){
                   		 Converter.saveFileInUTF8( "custcontacts.txt", sb2);
                   	}
              
                ddd.close();
                
             }catch (Exception r){
          	   
          	   r.printStackTrace();
             }
             
           // Converter.prepareDocCatalogue(inP,outP,codes,true);

	    } catch (Exception e){
	      getParent().showError(e);
	    }
	    return customers;
	  }


  private Connection getNewConnection() throws SQLException{
        return DriverManager.getConnection(getParent().getProps().getProperty("dsn.host"), getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
      }
  
  private Connection getNewConnection2() throws SQLException{
	    return DriverManager.getConnection(getParent().getProps().getProperty("dsn.host2"), getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	  }
  
  private Connection getNewConnection3() throws SQLException{
	    return DriverManager.getConnection(getParent().getProps().getProperty("dsn.host"), getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	  }


      public String getDistribution() {
        return null;
      }

     
  public TreeMap getProdGroups() throws SQLException {

   
    TreeMap   tm = new TreeMap();
   /* Connection con = getNewConnection();
    
    String    sql = "   SELECT code as liik,name as nimi FROM  itemanagroup  where dataareaid ='zp'  ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
      //  tm.put(liik,  Converter.enCodeEst(rs.getString("nimi").trim()));
    }
    rs.close();
    st.close();
    con.close();*/
  
    return tm;
}


    
  public TreeMap getProdGroupsSecond() throws SQLException {
	  Connection con = getNewConnection();
	    TreeMap   tm = new TreeMap();
		    return tm;
  }
  

  private TreeMap  getExcelProducts() {
	  TreeMap res = new TreeMap();
	  try{
		  
	 File file = new File(this.getExcelDir() + "Milk_product.xlsx");
			 
	       FileInputStream	 ff = new FileInputStream(file);
	       Workbook workbook =  workbook = new XSSFWorkbook(ff); 
			 Sheet sheet = workbook.getSheetAt(0); 
			    	  
						
			   
			 DataFormatter formatter = new DataFormatter();
		
			ff.close();	
			
			
			
			if(sheet != null){
				for (int aa = 7; aa < sheet.getLastRowNum()+1 ; aa++) {
				
			    Row row = sheet.getRow(aa);
				String kood = formatter.formatCellValue(row.getCell(2));
				String nimi = Converter.enCodeEst(formatter.formatCellValue(row.getCell(3)));
				String yhik = formatter.formatCellValue(row.getCell(5));
				String ean  = formatter.formatCellValue(row.getCell(1));
			
				
			
				
			if (kood != null && kood.trim().length() > 0) {
					Product pr = new Product(kood.trim());
					pr.setName(nimi.trim());
					double pri = 1;
					try {
						pri = Double.parseDouble(formatter.formatCellValue(row.getCell(6)).replace(',', '.'));
					}catch(Exception k) {
						
					}
					System.out.println(pri + " "+ kood);
					pr.setPrice(pri);
					pr.setUnit(yhik.trim());
					res.put(nimi.trim(), pr);
				  }
				 }
			 } 
				
			 }catch(Exception e) {
				  e.printStackTrace();
			  }
				
	  return res ;
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
		    products = getExcelProducts();
		    
		    try {
		           
			   Connection     ddd = getNewConnection();
		    	Statement stmt =  ddd.createStatement();
		      ResultSet rs = stmt.executeQuery(cmd);
		      HashMap prodHash = new HashMap();
		      while(rs.next()){
		        String kood = rs.getString("itemid").trim();
		        Product prod = new Product(kood);
		        String nimetus = Converter.enCodeEst( rs.getString("nimetus").trim() + " " + kood) ;
		   
		  
		        prod.setInCases(rs.getDouble("kastis"));
		       // prod.setOnPallet(rs.getDouble("alusel"));
		        prod.setPrice(rs.getDouble("price"));
		        prod.setType("ZP");
		        String ean = rs.getString("ean");
		        if(ean!=null && ean.trim().length()>0){
		        prod.setEAN(ean);
		      }
		String add = "";
		      String grupp = rs.getString("grupp");
		       if(grupp!=null && grupp.trim().length()>0){
		        prod.setGroup(grupp.trim());
		      //  add = grupp.trim().substring(0,2);
		     }
		       String grid = rs.getString("grid");
		       if(grid!=null && grid.trim().length()>0){
		           grid = grid.trim();
		           prod.setGroupSecond(grid);
		        }else{
		        	grid = "";
		        }
		    //  String grupp2 = rs.getString("grupp2");
		      // if(grupp2!=null && grupp2.trim().length()>0){
		       // prod.setGroupSecond(grupp2.trim());
		   
		     //}
		       
		      prod.setName((add +  " " + nimetus.replaceAll("'", " ").replaceAll("\"", " ").trim()).replaceAll("\n", " ").trim());

		      String unit = rs.getString("unit");
		      if(unit!=null && unit.trim().length()>0){
		       prod.setUnit(unit);
		    }
		      
		      String vat = rs.getString("vat");
		      if(vat!=null && vat.trim().length()>0){
		       prod.setVat("21");
		    }

		      int  samm = rs.getInt("samm");
		      if(samm>0){
		      // prod.setOptAmountInUnits(samm);
		    }
		      
		      String top = rs.getString("sorting");
		      if(top != null && top.trim().length()>0 && top.trim().equals("TOP")){
		       prod.setStatusTop("1");
		    }
		        products.put(grid + nimetus, prod);
		       
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
    	  
	     Connection  ddd = getNewConnection();
    	Statement stmt =  ddd.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      counter++;
      while(rs.next()){
    	
    	  
        String kliendikood = rs.getString("customer").trim();

        String tootekood = rs.getString("itemid").trim();
        if(!custs.keySet().contains(kliendikood+ tootekood)){
		           	
		       
		        double kogus = rs.getDouble("qty");
		        String lastdate = rs.getString("lastdate").substring(0,10);
		        double lastprice = rs.getDouble("lastprice");
		        custs.put(kliendikood + tootekood,lastdate); 
		        lastdate = lastdate.substring(5,10); 
		                 
		        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,lastprice,0,0," ");
		        
		        custProds.add(cp);
        }
        
      }
      rs.close();
      stmt.close();
      ddd.close();
 
      
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
               double lastprice = rs.getDouble("lastprice");
                        lastdate = lastdate.substring(5,10);
               CustProd cp = new CustProd(kliendikood, tootekood, 0 ,0 ,0,lastdate,lastprice,0,0," ");

             
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
    	
    	TreeMap troubles = new TreeMap();
   String sql = " select  orderaccount as klient ,namealias as name , iv. proddate as kuup , " +
				" (Select top 1 pdsbatchattribvalue from PdsBatchAttributes pa where pa.InventBatchId = iv.InventBatchId and pa.analysisplace_bbs  = 'ZP' and pa.pdsbatchattribid = 'RP') as fat , " +
				" (Select top 1 pdsbatchattribvalue  from PdsBatchAttributes pa where pa.InventBatchId = iv.InventBatchId and pa.analysisplace_bbs  = 'ZP' and pa.pdsbatchattribid = 'BP' ) as protein , "+
			    "  (select personnelnumber as kood from  DirPArtyTable w   inner join HCMWORKER h on h.person = w.recid where h.recid = cus.MAINCONTACTWORKER ) AS agent " +
				"  from vendinvoicetrans  tra INNER JOIN  vendinvoicejour jou ON   tra.invoiceid=  jou.invoiceid and  tra.DATAAREAID= jou.DATAAREAID	" +
				"  INNER JOIN vendtable cus on cus.accountnum = orderaccount and cus.dataareaid = 'zp' " +
				"   inner join DirPartyTable dp ON cus.partY=dp.REcid  " +
	            " inner JOIN Inventdim dim ON tra.InventDimId = dim.InventDimId and 'prod' = dim.DATAAREAID	  inner JOIN  InventBatch iv on dim.InventBatchId = iv.InventBatchId and iv.DATAAREAID = tra.DATAAREAID " +
	            " where  iv.itemid = 'P'  and iv.proddate > getdate()-30  order by 1,2 desc,3 desc"; // OK 
   try{
	   
 
   Connection ddd = getNewConnection();
	Statement stmr = ddd.createStatement();
	
   
   ResultSet rs1 = stmr.executeQuery(sql);
   String oklient = null;
   double ofat = 0;
   double oprot = 0;
   int cou = 0;
      while(rs1.next()){
      	
      	String klient = rs1.getString("klient").trim();
    	String name = rs1.getString("name").trim();
      	boolean goes = true ;
      	if(oklient == null){
      		oklient = klient;
      	}
      	
      	if(!oklient.equals(klient)){
      		cou = 0;
      		goes = true;
      	}
        if(cou == 2 ){
      		goes  = false;
      	}
      	cou++;
      	String kuu = rs1.getString("kuup").substring(0,10);
      	String fff = rs1.getString("fat");
      	double fat = 0;
      	double valk = 0;
      	try{
      		fat = Double.parseDouble(fff.trim());
      		valk = rs1.getDouble("protein");
      	}catch(Exception e){
      		
      	}
      	
      	String ag = " ";
      	try{
      	 	ag = rs1.getString("agent").trim(); 		
      	}catch(Exception e){
      		
      	}
     
     
        if(goes && fat > 0){
        if(ofat == 0){
        	ofat = fat;
        }else{
        	
        	if(oklient.equals(klient) && Math.abs(ofat-fat) > 0.8){
        		if(troubles.get(ag) != null){
        			String asi = troubles.get(ag) + "";
        			troubles.put(klient,asi + Converter.enCodeEst( name + " " + kuu +  " FAT :" + ofat + "% -> " + fat + "%||"));
        		}else{
        			troubles.put(klient,  Converter.enCodeEst(name +" " + kuu +  " FAT :" + ofat + "% -> " + fat + "%||"));
        		}
        	}
          ofat = fat;
        }
        
        if(oprot == 0){
        	oprot = valk;
        }else{
        	if(oklient.equals(klient) && Math.abs(oprot-valk) > 0.4){
        		if(troubles.get(ag) != null){
        			String asi = troubles.get(ag) + "";
        			troubles.put(ag,asi + Converter.enCodeEst( name + " " +kuu + " PROTE :" + oprot + "% -> " + valk + "%||"));
        		}else{
        			troubles.put(ag, Converter.enCodeEst( name + " " +kuu +  " PROTE :" + oprot + "% -> " + valk + "%||"));
        		}
        	}
        	oprot = valk;
        }
        }
    }
      
      
      rs1.close();
      stmr.close();
      ddd.close();
      
      
      
      
      
      
      String sql2 = "  select 'P' +  tt.klient ,  tt.kuup, max( tt.nimi ) as name,  " +
	     " round(sum( cast (purchqty as DECIMAL(9,2)) * cast (rieb as DECIMAL(9,2))/3.4) *3.4/sum( cast (purchqty as DECIMAL(9,2))),2) as fat , "+
		"	 round((sum(  cast (purchqty as DECIMAL(9,2)) *(1+(cast (rieb as DECIMAL(9,2))-3.4)*0.178+(cast (case when balt = '' then '3.33' else balt end as DECIMAL(9,2))-3)*0.267)) " +
	     "   -sum( cast (purchqty as DECIMAL(9,2)))*(0.178*(sum(  cast (purchqty as DECIMAL(9,2)) *cast (rieb as DECIMAL(9,2))/3.4) *3.4/sum(  cast (purchqty as DECIMAL(9,2))))-0.4062  " +
	      "   ))/(sum(  cast (purchqty as DECIMAL(9,2)))*0.267),2)  as prot , "+
			" 	(select top 1 personnelnumber as kood from  DirPArtyTable w   inner join HCMWORKER h on h.person = w.recid " +
			"	Inner JOIN BBSStationTable dim on h.recid = dim.responsible where dim.stationid = tt.klient ) AS agent " +
	    " from  ( select ost.codefrom as klient,  ost.transdate as kuup, " +
		" (Select top 1  description  from BBSStationTable aaa where aaa.stationid=ost.stationid ) as nimi ,purchqty  as purchqty, " +
	  	" (Select top 1 pdsbatchattribvalue  from PdsBatchAttributes pa where pa.InventBatchId = ost.InventBatchTo  " +
		"	   and pa.analysisplace_bbs  = 'ZP' and pa.pdsbatchattribid = 'RP') " +
		"	 as rieb ,(Select top 1  pdsbatchattribvalue  from PdsBatchAttributes pa where pa.InventBatchId = ost.InventBatchTo " +
		"	   and pa.analysisplace_bbs  = 'ZP' and pa.pdsbatchattribid = 'BP') " +
		"	  as balt   from  BBsMilkJournalTrans ost where ost.dataareaid ='zp' and ost.transdate > getdate() -15 " +
	     "    and purchqty  != 0 and ost.typefrom = 0 ) as tt where ISNUMERIC(tt.rieb)= 1  " +
			" group by tt.klient,tt.kuup order by 1,2 desc,3 ";
      //OK aga ei tule punkte vaid v��rlaod 
      	   
       
          ddd = getNewConnection();
      	 stmr = ddd.createStatement();
      	
         
          rs1 = stmr.executeQuery(sql);
          oklient = null;
         ofat = 0;
          oprot = 0;
          cou = 0;
            while(rs1.next()){
            	
            	String klient = rs1.getString("klient").trim();
          	String name = rs1.getString("name").trim();
            	boolean goes = true ;
            	if(oklient == null){
            		oklient = klient;
            	}
            	
            	if(!oklient.equals(klient)){
            		cou = 0;
            		goes = true;
            	}
              if(cou == 2 ){
            		goes  = false;
            	}
            	cou++;
            	String kuu = rs1.getString("kuup").substring(0,10);
            	String fff = rs1.getString("fat");
              	double fat = 0;
              	double valk = 0;
              	try{
              		fat = Double.parseDouble(fff.trim());
              		valk = rs1.getDouble("protein");
              	}catch(Exception e){
              		
              	}
              	
              	String ag = " ";
              	try{
              	 	ag = rs1.getString("agent").trim(); 		
              	}catch(Exception e){
              		
              	}
                  
              if(goes && fat > 0){
              if(ofat == 0){
              	ofat = fat;
              }else{
              	
              	if(oklient.equals(klient) && Math.abs(ofat-fat) > 0.4){
              		if(troubles.get(ag) != null){
              			String asi = troubles.get(ag) + "";
              			troubles.put(klient,asi + Converter.enCodeEst( name + " " + kuu +  " FAT :" + ofat + "% -> " + fat + "%||"));
              		}else{
              			troubles.put(klient,  Converter.enCodeEst(name +" " + kuu +  " FAT :" + ofat + "% -> " + fat + "%||"));
              		}
              	}
                ofat = fat;
              }
              
              if(oprot == 0){
              	oprot = valk;
              }else{
              	if(oklient.equals(klient) && Math.abs(oprot-valk) > 0.2){
              		if(troubles.get(ag) != null){
              			String asi = troubles.get(ag) + "";
              			troubles.put(ag,asi + Converter.enCodeEst( name + " " +kuu + " PROTE :" + oprot + "% -> " + valk + "%||"));
              		}else{
              			troubles.put(ag, Converter.enCodeEst( name + " " +kuu +  " PROTE :" + oprot + "% -> " + valk + "%||"));
              		}
              	}
              	oprot = valk;
              }
              }
          }
            
            
            rs1.close();
            stmr.close();
            ddd.close();
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      Iterator enume = troubles.keySet().iterator();
      while (enume.hasNext()){
    	  String key = enume.next()+"";
    	  Agent agent = (Agent)agents.get(key);
    	  if(agent != null){
    		  agent.setStartText("PROBLEMS!!!\n" +troubles.get(key)+ "");
    	  }
      }
      
  //    System.out.println("probleemid " + troubles.toString());
   }catch (Exception r ){
	 r.printStackTrace();  
   }  
    }
    
    
    private double getKoef(double fat,double prot){
    
    	double res = 1;
         res = 1+(prot-3.4)*0.178+(fat-3)*0.267;	
    	
    	return res;
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
