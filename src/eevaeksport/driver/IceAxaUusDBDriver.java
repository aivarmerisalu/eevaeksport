package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;
import eevaeksport.util.ReportingService;



public class IceAxaUusDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,PictureMaker,FixLoader {
  public IceAxaUusDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
//String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
try {
  Class.forName(databaseDriverName).newInstance();
  con = getNewConnection();
  stmt = con.createStatement();
 } catch (Exception e){
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
   
   
   @Override
	public void FixLoader() {
	  eanprods =  Converter.loadMore();
	  
	  if(eanprods == null) {
		  eanprods = new TreeMap();
	  }
		this.getProducts(null);
		TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
	//	System.out.println("Contracts " + contracts.keySet().size() + " " + contracts.keySet().toString());
		if(contracts.size() > 0) {
			Converter.sendContractJson(contracts ,  rcompanyx,  user ,  pass);
		}
		
		
		System.exit(0);
	}
   
   
   @Override
	public void PictureMaker() {
		makePictureCat();
		 System.exit(0);
	}

   private void makePictureCat(){
 	    
   	
   	String pictssou = parent.getProps().getProperty("pict.dir");
    String pictspda = parent.getProps().getProperty("pictpda.dir");
  
 try {
  
   File f = new File(pictspda +"list.txt");
   if (f.exists())f.delete();
   //Kataloogi sisude kustutamine
 	 File res2 = new File (pictspda);
        String[] ff2 = res2.list();
    	 for(int i = 0;ff2.length>i;i++){
        	(new File ( pictspda + ff2[i])).delete(); 
        }
//        	 Lõppes kustutamine 
 		 
 		 TreeMap prods = this.getProducts(null);

        StringBuffer buf = new StringBuffer();
        Iterator enume = prods.keySet().iterator();
   
        while (enume.hasNext()) {
   	    	String asi = (String)enume.next();
   	    	Product prod = (Product)prods.get(asi);
   	    	String toode = prod.getID().trim();  
   	    
              if(toode.length() > 0){
               	
   	   	  res2 = new File (pictssou);
             ff2 = res2.list();
            
        	 for(int i = 0;ff2.length>i;i++){
            	if( ff2[i].startsWith(toode + " ")){
          // System.out.println("pilt olemas " + ff2[i]);
       	File in = new File ( pictssou + ff2[i]); 
         	if(in.isFile()){
         	File inpda = new File ( pictspda + prod.getID()+ ".jpg");
         	if(!inpda.exists()){
         		try {
             	Converter.shrinkAndCopyFile(in,inpda,500);
     	    if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
               		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
               	}
         		}catch(Exception u) {
         			
         		}
     	  	}   }
            }
          }
        }
  }
    try {
	PrintWriter os = new PrintWriter(new FileWriter(pictspda + "list.txt", true));
  		os.write(buf.toString());
  		os.close();
  	  }catch (Exception ex) {
  		ex.printStackTrace();
  	  }
 	 
 	 } catch (Exception e) {
   	    getParent().showError(e);
   	}
    
 }


   
  public Vector getFixedAssortmentData() {

		Vector result = new Vector();
		return result;

	}
  
  private Connection con2 = null;
 
	 
		public String getSaveStockInfo() {
			connectionUp();
	       String cmd = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock FROM INVENTSUM summ " +
                 "INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and summ.dataareaid=dim.dataareaid " +
                 "INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and summ.dataareaid=inv.dataareaid " +
                 "WHERE summ.DATAAREAID='dat'and dim.DATAAREAID='dat' and inv.DATAAREAID='dat' AND CLOSED = 0 AND  moduletype=0 " +
                 " AND dim.inventlocationid IN ('KM')  and AVAILPHYSICAL > 0 GROUP BY summ.itemid  ORDER BY summ.itemid ";

			if (isDebug())
				System.out.println(cmd);
			StringBuffer products = new StringBuffer();
			try {

				Statement st = con.createStatement();
				ResultSet rs = st.executeQuery(cmd);

				while (rs.next()) {

					String kood = rs.getString("toode").trim();
					double seis = rs.getDouble("stock");
					if (seis > 0) {
						products.append(kood + ";");
						products.append(" ;");
						products.append(getParent().getNF().format(seis) + "; "
								+ "\n"); // + dealer + "\n");
					}
				}
				rs.close();
				st.close();
			} catch (Exception e) {
				getParent().showError(e);
			}
			
			getPilotDayData2();
			return products.toString();
		}



	  public String getDealerCustomerCSV(String agent){
	   return null;
	   }
	
	      	public String getSaveDebtInfo() {
        		
          	  String cmd3 = "SELECT cus.accountnum as klient," +
              "(SELECT SUM(amountcur) FROM custtransopen co WHERE co.duedate<getdate()" +
              " AND co.accountnum = cus.accountnum) AS debt ,blocked as blokk , cus.paymtermid as pay " +
         " FROM custtable cus  WHERE  cus.dataareaid='dat'  and blocked !=2    ";

        		StringBuffer products = new StringBuffer();
        		try {

        			connectionUp();
        			Statement st = con.createStatement();
        			ResultSet rs = st.executeQuery(cmd3);

        			while (rs.next()) {

        				String kood = rs.getString("klient").trim();
        				double volg = rs.getDouble("debt");
        				String blokk = rs.getString("blokk");
        				String bl = "0";
        				if(blokk!= null && blokk.equals("1")){
        					bl = "1";
        				}
        				if(blokk!= null && blokk.equals("2")){
        					bl = "2";
        				}
        				
        				  String  term = rs.getString("pay");
        			         
        			         if(term != null && term.trim().length() > 0 ){
        			        	 
        			        	 if( term.trim().equals("ette")){
        			        		 bl = "3";
        			        	 }
        			        
        			         }
        				
        				
        				if (volg > 0 || bl.equals("3")) {
        					products.append(kood + ";"
        							+ getParent().getNF().format(volg) + ";" + bl
        							+ "\n");
        				}
        			}
        			
        			
        			

        			rs.close();
        		} catch (Exception e) {
        			getParent().showError(e);
        		}
        		
        		this.connectionDown();
        		return products.toString();

        	}

               
                                                  public String  getCustProdDiscounts(Agent agent){
                                                   return null;
                                                   }

                                                  public String getAssortmentStr(Agent agent){
                                                  	return null;
                                                  }
  /**
   *
   */
  public void connectionUp(){
  }

  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, "mobilejava", "mobilejava"); //"MobileJava365");
  }
  
  private Connection getNewConnection2() throws SQLException{
	    return DriverManager.getConnection(url2, "bmssa", "bmssa_pwd"); //"MobileJava365");
	  }

  public Vector getDebt(String agentCode) throws SQLException{
    Vector list = new Vector();

    String sql = "SELECT  a.accountnum AS maksja,a.orderaccount AS klient,a.invoice AS number,co.duedate AS maksepaev,co.amountcur" +
    		" AS tasumata, " +
        "co.transdate AS kuupaev FROM custtransopen co INNER JOIN custtrans a ON co.REFRECID=a.recid and co.dataareaid=a.dataareaid " +
        "WHERE  co.dataareaid='dat' AND a.dataareaid='dat' ORDER BY a.accountnum,co.transdate ";
    Connection con = this.getNewConnection();
	Statement stmt =  con.createStatement();
    ResultSet rs = stmt.executeQuery(sql);
    while(rs.next()){
      Debt debt = new Debt();
      debt.setPayerID(rs.getString("maksja").trim());
      debt.setCustID(rs.getString("klient").trim());
      String number = rs.getString("number").trim();
      if(number.trim().length()== 0){
    	  number = "nonumber";
      }
      debt.setDoknr(number);
      debt.setDuedate(rs.getDate("maksepaev"));
      debt.setSum(rs.getDouble("tasumata"));
      debt.setDate(rs.getDate("kuupaev"));
      list.addElement(debt);
    }
    rs.close();
    stmt.close();
    con.close();
    System.out.println("VÕLAD");
    return list;
  }

  

  /**
   *
   */
  public void connectionDown(){
  }

  public TreeMap getAgents(){

	  
	//  getSoodused();
	  
    String cmd = " SELECT VALUE, NAME FROM  DIMATTRIBUTEFINANCIALTAG WHERE (CATEGORY = 5637144580)";
    
  //  if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    agents1 = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("value").trim());
          String name = rs.getString("name").trim();
          agent.setName(name);
          agent.setLanguage("EST");
          agent.setCountry("et");
          agent.setPassWord("0000");
          String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
          if(email.length() > 3){
        	  agent.setEmail(email);
          }
          if(email.length() > 0){
        	  agent.setActive(true);
          }
       
          agent.setIsPilot(true);
          agents.put(name, agent);
          agents1.put(agent.getID(), agent);
        }
        
        
        Agent aa = new Agent("LADU");
        aa.setName("LADU");
        agents.put("LADU", aa);

        Agent aa2 = new Agent("ROVERTO");
        aa2.setName("ROVERTO");
        agents.put("ROVERTO", aa2);
        
        Agent aa1 = new Agent("LADU1");
        aa1.setName("LADU1");
        agents.put("LADU1", aa1);
       
        
       // Converter.sendAgentJson(agents, country, false);
            
    } catch (Exception e){
        getParent().showError(e);
      }
    
    return agents;
  }




      public String getDistribution() {
        return null;
      }



  public TreeMap getProdGroups() throws SQLException {


     TreeMap   tm = new TreeMap();
     String    sql = "SELECT ItemBuyerGroupId AS liik , ItemBuyerGroupId AS nimetus FROM  inventtable " +
                " WHERE dataareaid='dat' and ItemBuyerGroupId != '' Group by ItemBuyerGroupId";
     Connection con = this.getNewConnection();
 	Statement stmt =  con.createStatement();
 	ResultSet rs = stmt.executeQuery(sql);

     while (rs.next()) {
       String liik=rs.getString("liik").trim();
       String nimetus=rs.getString("nimetus").trim();
       if (liik != ""){
       tm.put(liik,Converter.enCodeEst(nimetus));
       }
     }
     rs.close();
     stmt.close();
     con.close();
     return tm;
 }

  
  private TreeMap getGrupid(){
      File fff = new File(this.getExcelDir() + "Sortiment.xlsx" );
      TreeMap groups = new TreeMap(); 
  	try{
          FileInputStream ff = new FileInputStream(fff);
  		XSSFWorkbook workbook = new XSSFWorkbook((ff));
  		XSSFSheet sheet = workbook.getSheetAt(1);
  		XSSFRow row = null;
  		
  		row = sheet.getRow(0);
  	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
  		
  		//System.out.println("rownum " + row.getLastCellNum());
  		
  		
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
	    	    			  	 String sort = null;
	    	    			  	try{
	    	    			  		sort = row.getCell((short)1).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			  		sort = row.getCell((short)1).getNumericCellValue()+"";  
		    	    	    		} 
		    	    			  	 if(sort.indexOf(".")!= -1){
		    	    			  	 	 sort = (sort.substring(0,kood.indexOf(".")));	
		    	    			  	 }
		    	    			  	if(groups.get(kood) != null) {
		    	    			  		 groups.put(kood,groups.get(kood) + "|" + sort);
		    	    			  	}else {
		    	    			  		 groups.put(kood,sort);
		    	    			  	}
		    	    			  	 
	    	    			  	
  	    			  
  	    				
  				}	   
  				}
 			}catch (Exception e){
  			//	System.out.println(row.getCell((short)a).getNumericCellValue());
  			}
  		}
  		
  		
  
  	 }catch (Exception e){
  		e.printStackTrace();
  	}
  	 
  	// System.out.println("grupid suurus " + groups.size() + " " + groups.toString());
      
     return groups; 
  	 
  }

  
  private TreeMap getExtraVisits(){
      File fff = new File(this.getExcelDir() + "esindajad.xls" );
      TreeMap groups = new TreeMap(); 
  	try{
          FileInputStream ff = new FileInputStream(fff);
  		HSSFWorkbook workbook = new HSSFWorkbook((ff));
  		HSSFSheet sheet = workbook.getSheetAt(0);
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
	    	    			  	 String cust = null;
	    	    			  	try{
	    	    			  		cust = row.getCell((short)2).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			  		cust = row.getCell((short)2).getNumericCellValue()+"";  
		    	    	    		} 
		    	    			  	if(cust.indexOf(".")!= -1){
		    	    			  	 	 cust = (cust.substring(0,cust.indexOf(".")));	
		    	    			  	 }
		    	    			  	 
		    	    			  	 String paev = null;
			    	    			  	try{
			    	    			  		paev = row.getCell((short)4).getStringCellValue();  
				    	    			  	 }catch (Exception e){
				    	    			  		paev = row.getCell((short)4).getNumericCellValue()+"";  
				    	    	    		} 

		    	    			  	 if(paev != null){
		    	    			  		 paev = paev.trim().replaceAll(",", "");
                                	 Agent aaa = new Agent(kood.trim());
			    	    			  	 aaa.setVisit(paev);
			    	    			  //	 System.out.println("lisad " + cust + " " + kood + " " + paev);
		    	    			  	   groups.put(cust.trim(),aaa);
		    	    			  	 }
		    	     }	   
  				}
 			}catch (Exception e){
  		
 			}
  		}
  		
  		
  
  	 }catch (Exception e){
  		e.printStackTrace();
  	}
  	 
  	 //System.out.println("lisakli suurus " + groups.size());
      
     return groups; 
  	 
  }
  

  
  
  
  public TreeMap getCustomers(Agent agent){
    String cmd = "SELECT dp.name, cus.companychainid as payer, cus.accountnum, (SELECT SUM(amountcur) FROM custtransopen co WHERE co.duedate<getdate() \r\n" + 
    		"                 AND co.accountnum = cus.accountnum) AS debt,  cus.linedisc ,  adr.address,''  AS visit,'' AS useean,'' AS mituladu,\r\n" + 
    		"				  dim.DISPLAYVALUE AS agent, cus.memo AS agent2,\r\n" + 
    		"				  cus.blocked, (select l.locator  from  LOGISTICSELECTRONICADDRESS l WITH (NOLOCK)  where l.recid = dp.primarycontactphone) as phone,\r\n" + 
    		"				  '' AS celluralphone,  '' as kett ,  cp.name as chain ,  custclassificationid as type,\r\n" + 
    		"				  adr.zipcode as indeks,  adr.state as region,   adr.city, adr.street,\r\n" + 
    		"				  cus.paymtermid as pay , d.notes" + 
    		"             FROM  custtable cus with(nolock)  LEFT JOIN custgroup cp on cp.custgroup = cus.custgroup and cp.dataareaid=cus.dataareaid \r\n" + 
    		"			 LEFT OUTER JOIN DEFAULTDIMENSIONVIEW dim ON cus.PARTITION = dim.PARTITION AND cus.DEFAULTDIMENSION = dim.DEFAULTDIMENSION AND dim.NAME = N'Agent'\r\n" + 
    		"			 inner join DirPartyTable dp with(nolock) ON cus.partY=dp.REcid  inner join \r\n" + 
    		"			 LOGISTICSPOSTALADDRESS adr ON adr.LOCATION = dp.PRIMARYADDRESSLOCATION and getdate() >=VALIDFROM  and  getdate() <= VALIDTO left outer JOIN  paymterm  \r\n" + 
    		"                        pay on cus.paymtermid = pay.paymtermid  and pay.DATAAREAID  = cus.DATAAREAID left join DocuRef  d ON cus.recid = d.refrecid and d. name like 'Leping%' \r\n" + 
    		"                              WHERE   cus.dataareaid='dat' and (blocked !=2  or cus.accountnum in ('2183','7588','3274','5755') )  "; 
    // 

    
    TreeMap extraVisits = getExtraVisits();
    HashSet custs = new HashSet();
	  
    Converter cc = new Converter();
	 // HashMap kyssad =   cc.getQuestionaire(this.getExcelDir());
	TreeMap groups = this.getGrupid();
	  sortimendid = new HashMap();
	  ketid = new HashMap();
	  HashSet asses = new HashSet();

 // if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
           //HashMap custHash = new HashMap();
    try {
       	
    	HashMap<String, ArrayList> ags = new HashMap<String, ArrayList>();
    	ResultSet rs = stmt.executeQuery(cmd);
        Statement stm = getNewConnection().createStatement();
        
    	
      while(rs.next()){
        String name = rs.getString("name").trim();
        String payer = rs.getString("payer");
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);
        if(payer != null && payer.trim().length() > 0) {
        	cust.setPayer(payer.trim());
        }
        
        cust.setName(Converter.enCodeEst(name));
        String debtStr = rs.getString("debt");
        if (debtStr != null){
          cust.setDebt(Double.parseDouble(debtStr));
        }

        String aleke = rs.getString("linedisc");
        if(aleke == null){
        aleke = "000" ;
        } else {
        aleke = aleke.trim();
        cust.setGroup(aleke);
        }
        
       
        
      //  cc.addQuestiosToCustomer(cust,kyssad);
        
        String grp = (String)groups.get(cust.getID());
        if(grp != null){
        	//System.out.println("Siin on grupp olemas "  + grp + " " + cust.getID() );
        	Vector vv = new Vector();
        	StringTokenizer st = new StringTokenizer(grp,"|");
        	while (st.hasMoreElements()){
        		String gg = st.nextToken();
        		vv.add(Converter.cleanLitLatEst(gg.trim()));
        		sortimendid.put(cust.getID(), gg.trim());
        	}
        	//cust.setFixSortCode(vv);
        //	cust.setGroup(grp);
        }
        
        String aadr = rs.getString("address");
        if (aadr != null) {
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim()),"Estonia"));
        }
        String visit = rs.getString("visit");
        if (visit != null) {
          cust.setVisit(visit.trim());
        }

        String printean = rs.getString("useean");
        if (printean.equalsIgnoreCase("T")) {
          cust.setPrintean("T");
        }
        String mituladu = rs.getString("mituladu");
               if (mituladu.equalsIgnoreCase("T")) {
               //  cust.setBlockingDealer("T");
               }
         String ag = rs.getString("agent");
         if(ag != null) {
        	
         String age = "";
         if(extraVisits.get(cust.getID()) != null){
        	 Agent aa = (Agent)extraVisits.get(cust.getID());
        	 age = aa.getID();
        	 cust.addAgent(aa); 
        	}
         
         if(!ag.equals(age)){
             cust.addAgent(new Agent(ag));  

         }

         
         
         if(cust.getID().equals("1003") || cust.getID().equals("1005") 
        		 ||cust.getID().equals("5755")|| cust.getID().equals("7588")
        		 ||cust.getID().equals("3274")|| cust.getID().equals("1217")
        		 || cust.getID().equals("2183") ||  cust.getID().equals("1950")
        		 || cust.getID().equals("2671") ||  cust.getID().equals("7445")
        		 ||  cust.getID().equals("3543") ||  cust.getID().equals("8255")
         		        		 ||ag.startsWith("121")|| ag.startsWith("142")
        		 
        		 ){
        	 cust.addAgent(new Agent("LADU"));
        	 cust.addAgent(new Agent("LADU1"));
         }
         
         
          if(cust.getID().equals("2671") || cust.getID().equals("7445")) {
        	 cust.addAgent(new Agent("ROVERTO"));
           }

    	 
         String agentAGENTS = getParent().getProps().getProperty(ag);

         if(agentAGENTS != null){
	         StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
	  	       while(stag.hasMoreTokens()){
             cust.addAgent(new Agent(stag.nextToken()));
 	       }
         }
         
         

         String ag2 = rs.getString("agent2");
         if(ag2 != null && ag2.trim().length() > 0){
        	 
        	 StringTokenizer st = new StringTokenizer(ag2,",");
        	 while(st.hasMoreTokens()){
        		String aaa = st.nextToken().replaceAll("\\r\\n|\\r|\\n", " ").trim();
        		//System.out.println("aaa " + cust.getID()+ " "+ aaa + " " + aaa.length());
        	   if(!aaa.trim().equals(age)){
                   cust.addAgent(new Agent(aaa.trim()));           	 
               }
        	   
        	   
                agentAGENTS = getParent().getProps().getProperty(aaa.trim());
               if(agentAGENTS != null){
      	         StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
      	  	       while(stag.hasMoreTokens()){
                   cust.addAgent(new Agent(stag.nextToken()));
       	       }
               } 
        	 } 
         }
         }
         String block = rs.getString("blocked");
         if(block != null && block.trim().length()>0){
         	
         	if(block.trim().equals("1")){
             	cust.setSalesBlock(true);
             //	System.out.println(" blokis " + cust.getID());
         	}
         	
         	if( block.trim().equals("2")){
             	cust.setSalesBlockT(true);
         	}
        }
    
         String phone = rs.getString("phone");
         if(phone != null && phone.trim().length()>0){
         	cust.restPhone = phone.replaceAll(";",",").trim();
          cust.setPersonalData(phone.replaceAll(";",",").trim());
        }
         
       /*  String cellularphone = rs.getString("cellularphone");
         if(cellularphone != null && cellularphone.trim().length()>0){
         	if(cust.getPersonalData().trim().length() > 0){
         		cust.setPersonalData(cust.getPersonalData() + " " + cellularphone.replaceAll(";",",").trim());
         	}else{
         		cust.setPersonalData(cellularphone.replaceAll(";",",").trim());	
         	}
        cust.restPerPhone = cellularphone.replaceAll(";",",").trim();
        }*/
         
         
         String kett = rs.getString("kett");
         if(kett != null && kett.trim().length()>0){
      	   if(ketid.get(Converter.cleanLitLatEst(kett.trim())) != null){
         		Vector vv = (Vector)ketid.get(Converter.cleanLitLatEst(kett));
         		vv.add(cust.getID());
         		ketid.put(Converter.cleanLitLatEst(kett.trim()), vv);
         	}else{
         		Vector vv = new Vector();
         		vv.add(cust.getID());
         		ketid.put(Converter.cleanLitLatEst(kett.trim()), vv);
         	}
         }
         
         
         String chain = rs.getString("chain");
         if(chain != null && chain.trim().length()>0){
        	 cust.setChain(Converter.enCodeEst(chain.trim()));
         }
         
         String type = rs.getString("type");
         if(type != null && type.trim().length()>0){
        	 cust.category = type.trim();
         }
         
         String indeks = rs.getString("indeks");
         if(indeks != null && indeks.trim().length()>0){
        	 cust.restZipCode = indeks.trim();
         }
String region = rs.getString("region");
         
         if(region != null && region.trim().length()>0){
        	 region  = region.trim() + " ";
         }else{
        	 region = "";
         }
         
         String city = rs.getString("city");
         
         if(city != null && city.trim().length()>0){
        	cust.restCity  = region +  city.trim();
         }
         
  String  street = rs.getString("street");
         
         if(street != null && street.trim().length()>0){
        	cust.restStreet  =   street.trim();
         }
         
  String  term = rs.getString("pay");
         
         if(term != null && term.trim().length() > 0 ){
        	 
        	 if( term.trim().equals("ette")){
        			cust.setSalesBlockPrepay(true);
        	 }else{
        		 try{
        			cust.setPaymTerm(Integer.parseInt(term.trim()) +""); 
        		 }catch(Exception e){
        			 
        		 }
        	 }
        
         }
         
 String  leping = rs.getString("notes");
         
         if(leping != null && leping.trim().length()>0){
        	 cust.setInfo(cust.getInfo() + "|PV: " +leping.trim() );
         }              
               
        String cmd2 = "SELECT disc.percent1 AS discount FROM pricedisctable disc  WHERE disc.accountrelation='" + aleke + "' AND " +
               " disc.dataareaid='dat' AND len(disc.itemrelation)=0  AND fromdate<=getdate() and todate >= getdate()";
        ResultSet rs1 = stm.executeQuery(cmd2);
                     if(rs1.next()){
                       double ale = rs1.getDouble("discount");
                       cust.setDiscount(ale);
                     }
                    rs1.close();
                   
                    
                    // Põhiladu 1003 .
                    
                    //Luige Kylminkus 5755
                     //Kitman 3274
                     //Mahakantud 1005
                     //Kontor 7588

                    
                    if(!cust.getID().equals("1003") && !cust.getID().equals("5755") &&
                    		!cust.getID().equals("3274") &&
                    		!cust.getID().equals("1005") &&
                    		!cust.getID().equals("7588")){
                    String cmd3 = "SELECT     TOP (100) PERCENT dbo.ASSETLENDING.ASSETID + ' ' + dbo.ASSETTABLE.NAME AS Kylmik , " +
                      " dbo.ASSETLENDING.LENDINGDATE AS kuupaev ,dbo.ASSETLENDING.ASSETID as idd FROM dbo.ASSETLENDING LEFT OUTER JOIN " +
                     " dbo.ASSETTABLE ON dbo.ASSETLENDING.ASSETID = dbo.ASSETTABLE.ASSETID " +
                    " WHERE     (dbo.ASSETLENDING.ACTUALRETURN = CONVERT(DATETIME, '1900-01-01 00:00:00', 102)) AND (dbo.ASSETLENDING.DATAAREAID = 'dat')" +
                    " and  dbo.ASSETLENDING.CUSTACCOUNT='" + accountnum + "'";
                    rs1 = stm.executeQuery(cmd3);
                    boolean first = true;
                          while(rs1.next()){
                        	  if(first){
                        		  cust.setInfo("asset:");
                        	  }
                        	  first = false;
                            
                        	  String inff = rs1.getString("Kylmik");
                            String kuup = rs1.getString("kuupaev").substring(0,10);

                      	    asses.add(rs1.getString("idd").trim());
                            
                            cust.setInfo(cust.getInfo() + "|" + inff + " " + kuup);
                          }
                         rs1.close();
                    }
                         boolean goes = true;
                         if(name.indexOf("***") != -1){
                        	 goes = false;
                         }
                         cust.setCountry("Eesti");
                    if(goes){
                        customers.put(name, cust);	
                        custs.add(accountnum);
                    }
      }

    	  
    	 
      
      
      if(this.rcompany != null){
    	
    	  
    	     //Tõmbame alla kõikmis meil olemas
      eevaeksport.util.ReportingService servu = new ReportingService();
  		servu.user =  user;
  		servu.pass = pass;
  		servu.company = rcompany;   
  		JSONArray assets = servu.getAssets(null);
  		servu.closeClient();
 
  		
  		HashMap asscusts = new HashMap();
  	     HashSet cuus = new HashSet();	
  		for (int i = 0; i < assets.length(); i++) {
			 JSONObject cob = assets.getJSONObject(i);
				 //   String id = cob.get("id") + "";
				    String code = (String)cob.get("code");
					//String name = (String)cob.get("name");
					
					try{

						JSONObject cust  = (JSONObject)cob.get("customer");
						 if(cust != null){
							 String kkood = (String)cust.get("code");
							// String kname = (String)cust.get("name");
							asscusts.put(code, kkood);
							cuus.add(code);
						 }	
					}catch(Exception e){
						
					}
		 }
	
    	  
    	  
    	  System.out.println("Asses size enne  " + asscusts.size());
    	  Vector vec = new Vector(); 
    	  String cmd3 = "select a.ASSETID as code ,b.name , '' as ean ,'' as nfcTag, a.ASSETID as factoryID ,b.make as description , " + 
    	  		" b.createddatetime as born , LENDINGDATE as contract ,a.CUSTACCOUNT as custcode " +
		    " FROM dbo.ASSETLENDING a LEFT OUTER JOIN " +
                   "  dbo.ASSETTABLE b ON a.ASSETID = b.ASSETID and a.DATAAREAID = b.DATAAREAID " +
                   "  WHERE     (a.ACTUALRETURN = CONVERT(DATETIME, '1900-01-01 00:00:00', 102)) AND (a.DATAAREAID = 'dat') " +
				   "  order by a.assetid"; // and qty_lv = 0
               ResultSet rs1 = stm.executeQuery(cmd3);
               int counter = 0;
                 while(rs1.next()){
                	
                	 JSONObject obj = new JSONObject();
                	 
                        String code = rs1.getString("code").trim();
                        obj.put("code", code);
                        asses.add(code);
                        try{
                        	
                        String name = rs1.getString("name");
                        if(name == null){
                        	name = code;
                        }else{
                        	name = name.trim();
                        }
                        
                        obj.put("name", name);
                        
                        String ean = rs1.getString("ean");
                        if(ean != null && ean.trim().length() > 0){
                        	   obj.put("ean", ean);
                        }
                     
                        String nfcTag = rs1.getString("nfcTag");
                        if(nfcTag != null && nfcTag.trim().length() > 0){
                     	   obj.put("nfcTag", nfcTag);
                     }
                        String factoryId = rs1.getString("factoryId");
                        if(factoryId != null && factoryId.trim().length() > 0){
                      	   obj.put("factoryId", factoryId);
                          }
                        String description = rs1.getString("description");
                        if(description != null && description.trim().length() > 0){
                      	   obj.put("description", description);
                      }
                        java.sql.Date born = rs1.getDate("born");
                        if(born != null ){
                       	   obj.put("born", Converter.jsonDate(born));
                       }
                        
                        java.sql.Date contract = rs1.getDate("contract");
                        if(born != null ){
                       	   obj.put("contract", Converter.jsonDate(contract));
                       }
                    
                        boolean goes = false;
                        if(asses.contains(code)){
                        	goes = true;
                        }
                        
                        String ccode = rs1.getString("custcode");
                        if(ccode != null && ccode.trim().length() > 0){
                        	if(ccode.equals("1005")){
                        		 obj.put("active", false);
                        	}else{
                        		 obj.put("active", true);
                        	}
                        	
                        	if(ccode.equals("1005") || ccode.equals("1003")){
                        		goes = true;
                        		asses.add(code);
                        	}
                        	
                      }
                     
                        if(goes && !cuus.contains(code)){
                        	 vec.add(obj);
                        	counter ++;
                        }
                           
                          
                        
                        }catch (Exception r){
                         r.printStackTrace();    
                        }
                        
                       
                        //java.sql.Date contract = rs1.getDate("contract");
                      }
                     rs1.close();
    	  
    	  
                     
                   //  System.out.println("Counter " + counter + " Asses " + asses.size() + " " + vec.size());
                     
    	   Converter.sendAssetsJson(vec,this.user,this.pass,this.rcompany);     
    	   
    	     System.out.println(" Asses saadetud  lähen tegema seoseid" + asses.size());
                		
             		
             		
    	   //siin saadame seosed ära jujul kui customer on olemas selline 
     	   vec = new Vector(); 
     	   cmd3 = "select a.ASSETID as itemcode ,b.name , '' as ean ,'' as nfcTag, a.ASSETID as factoryID ,b.make as description , " + 
      	  		" b.createddatetime as born , LENDINGDATE as contract ,a.CUSTACCOUNT as custcode " +
  		    " FROM dbo.ASSETLENDING a LEFT OUTER JOIN " +
                     "  dbo.ASSETTABLE b ON a.ASSETID = b.ASSETID and a.DATAAREAID = b.DATAAREAID " +
                     "  WHERE     (a.ACTUALRETURN = CONVERT(DATETIME, '1900-01-01 00:00:00', 102)) AND (a.DATAAREAID = 'dat')  " +
  			//	"	and  a.CUSTACCOUNT != '1005' " +
  				"	 order by a.assetid";
              int cou = 0;
              int coua = 0;
     	  rs1 = stm.executeQuery(cmd3);
                  while(rs1.next()){
                 	
                 	 JSONObject obj = new JSONObject();
                 	 coua++;
                         String code = rs1.getString("itemcode").trim();
                         obj.put("inventoryItemCode", code);
                         Date contract = rs1.getDate("contract");
                         obj.put("contractDate",contract.getTime() );
                         String custcode = rs1.getString("custcode").trim();
                         obj.put("customerCode", custcode);
                         
                         if(code.indexOf("MI.04459") != -1){
                        	 System.out.println("MI.04459 " + custcode +" "+ (asses.contains(code)) + " " +custs.contains(custcode));
                         }
                       
                         if(asses.contains(code)  ){ // && custs.contains(custcode)
                        	  String olemas = (String)asscusts.get(code);
                        	  
                        	  
                        	  if(olemas == null){
                                  vec.add(obj);  
                                  cou++;  
                         	  }else if(olemas != null){
                        		  if(!olemas.equals(custcode)){
                        			   vec.add(obj);  
                                       cou++; 
                        		  }
                        	  }
                          }
                       }
                  
                      rs1.close();
                      stm.close();
    	     
                      System.out.println("assets cou " +  cou + " "+ coua + " " + vec.size());
                      
                      // siis peaks tegema vahele kontrolli et kui on muudatus siis ainult laksad ylesse
    	   
                  //	Converter.saveFile("json" + System.getProperty("file.separator") 
        		//	+ Calendar.getInstance().getTimeInMillis()+ "vec.txt" , vec.toString());
    	   
                    Converter.sendAssetsCustomerJson(vec,this.user,this.pass,this.rcompany);     
               	   
                    System.out.println("assets saadetud " +  cou + " "+ coua);
    	   System.exit(0);
    	   //lendame välja kuna in inventari case
    	   
      }
   
    } catch (Exception e){
      getParent().showError(e);
    }
    return customers;
  }

  private HashMap prodHash = new HashMap();
  
  private void makeTexts(TreeMap recids) throws Exception{
	  
	  String    sql = "select Refrecid as kood ,  notes as asi  from  "
	  		+ " Docuref ref  " +
              " WHERE refcompanyid='dat' and TypeID ='Märkus' and name like '%koos%'";
	  Connection con =  this.getNewConnection();
  Statement stmt = con.createStatement();
   ResultSet rs = stmt.executeQuery(sql);
    StringBuffer sb = new StringBuffer();
   while (rs.next()) {
     String liik=rs.getString("kood").trim();
     if(recids.get(liik) != null){
    	 
    	 String nimetus=rs.getString("asi");
    			 if(nimetus != null){
    				 nimetus = nimetus.trim().replaceAll("\\r\\n|\\r|\\n", "|"); 
    				   sb.append(recids.get(liik) + ";" + nimetus + "\n");
    			 }
      
       	 
     }
     }
   rs.close();
   stmt.close();
   con.close();
     Converter.saveFileInUTF8("texts.txt", sb);

  }
  
  
 private TreeMap getLepingud(TreeMap recids) throws Exception{
	  TreeMap res = new TreeMap();
	  String    sql = "select Refrecid as kood ,  notes as asi  from  "
	  		+ " Docuref ref  " +
              " WHERE refcompanyid='dat' and TypeID ='Märkus' and name like '%Leping%'";
	  Connection con =  this.getNewConnection();
  Statement stmt = con.createStatement();
   ResultSet rs = stmt.executeQuery(sql);
  
   while (rs.next()) {
     String liik=rs.getString("kood").trim();
     if(recids.get(liik) != null){
    	 
    	 String nimetus=rs.getString("asi");
    			 if(nimetus != null){
    				 nimetus = nimetus.trim().replaceAll("\\r\\n|\\r|\\n", "|");
    				 res.put(recids.get(liik), nimetus);
    			 }
         }
     }
   rs.close();
   stmt.close();
   con.close();
   return res;

  }
  
  
  
  private TreeMap getSoodused(){

	  TreeMap result = new TreeMap();
			File f = new File(this.getExcelDir() + "soodukad.xlsx");
			 
			
		//	System.out.println("asoortment juurde " + insertassort);
			
			
			try {
				
				
				FileInputStream ff = new FileInputStream(f);
				XSSFWorkbook workbook = new XSSFWorkbook((ff));
				XSSFSheet sheet = workbook.getSheet("Poed");
			int a = sheet.getLastRowNum();
				// System.out.println("rownum " + row.getLastCellNum());
				for(int i = 0 ; i < a+1 ;i++){
					XSSFRow row = sheet.getRow(i);
					try {
						// System.out.println(row.getCell((short)a).getStringCellValue());
						String kood = row.getCell((short) 0).getStringCellValue();
						//4-6/4
						double  ale4 = row.getCell((short)4).getNumericCellValue();
						double  ale6 = row.getCell((short)6).getNumericCellValue();
                       double res = ((ale4-ale6)/ale4 *100);
						if(kood != null && kood.trim().length() > 0){
							result.put(kood.trim(),res);
						}
					} catch (Exception e) {
						e.printStackTrace();
						// System.out.println(row.getCell((short)a).getNumericCellValue());
					}
				}
				
				
				sheet = workbook.getSheet("Horeca");
			     a = sheet.getLastRowNum();
					// System.out.println("rownum " + row.getLastCellNum());
					for(int i = 0 ; i < a+1 ;i++){
						XSSFRow row = sheet.getRow(i);
						try {
							// System.out.println(row.getCell((short)a).getStringCellValue());
							String kood = row.getCell((short) 0).getStringCellValue();
							//4-6/4
							double  ale4 = row.getCell((short)6).getNumericCellValue();
							double  ale6 = row.getCell((short)9).getNumericCellValue();
	                       double res = ((ale4-ale6)/ale4 *100);
							if(kood != null && kood.trim().length() > 0){
								result.put(kood.trim(),res);
							}
						} catch (Exception e) {
							e.printStackTrace();
							// System.out.println(row.getCell((short)a).getNumericCellValue());
						}
					}
				
				
			 }catch(Exception d){
		       	   d.printStackTrace();
		          }
		//	System.out.println("result " + result.toString());
			return result;
  }
  
  private  void makeAllowedFile() {
		connectionUp();
     String cmd = "  Select a.itemid as toode , case when chainenterprise != '' then chainenterprise else  custaccount end as custid " + 
     		"     	  from BALITEMALLOWEDSALESREL A INNER JOIN inventtable inv on inv.itemid = a.itemid where inv.allowedsalesall = 0";

		if (isDebug())
			System.out.println(cmd);
		StringBuffer products = new StringBuffer();
		TreeMap<String,String> prods = new TreeMap<String,String>();
		try {

			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);

			while (rs.next()) {

				String kood = rs.getString("toode").trim();
			
				String custid = rs.getString("custid");
				if(custid!= null) {
				if(prods.get(kood) != null) {
						prods.put(kood, prods.get(kood) + "," + custid.trim());
				  } else {
						prods.put(kood, custid.trim());
				  }	
				}
		
				
			}
			rs.close();
			st.close();
		} catch (Exception e) {
			getParent().showError(e);
		}
		
		Iterator itera =   prods.keySet().iterator();
		while(itera.hasNext()) {
			String tt = itera.next() + "";
			products.append(tt+ ":" + prods.get(tt) + "\n");
		}
        
		Converter.saveFile("allowed.txt", products.toString());
	
	}

  
  private TreeMap getKastis() {
	   TreeMap<String,Double> res = new TreeMap<String,Double>();
	   try {
		   
		   File ff= new File("kastis.txt");
		  
		    FileInputStream is = new FileInputStream(ff);
			InputStreamReader isr = new InputStreamReader(is, "utf-8");
			BufferedReader reader = new BufferedReader(isr);
			
		    String line = null;
		 
		    try {
		        while((line = reader.readLine()) != null) {
		          StringTokenizer st = new StringTokenizer(line,";");
		           while(st.hasMoreTokens()) {
		    	     res.put(st.nextToken(), Double.parseDouble(st.nextToken()));
		           }
		        }

		    } finally {
		        reader.close();
		    }
		 
	   }catch(Exception e) {
		   e.printStackTrace();
	   }
	   
	
	   
	 System.out.println("lis suurus " + res.size());
	   
	   return res;
  }
  
  public TreeMap getProducts(Agent agent){
	  
	  TreeMap<String,Double> kastis = getKastis();
	  
	  
	  prodHash = new HashMap();
	  makeAllowedFile();
    
    String cmd = "SELECT inv.recid, inv.itemid, erpt.name as nimetus,\r\n" + 
    		"              left(ItemBuyerGroupId,4) as abix , disc.amount AS price,\r\n" + 
    		"              NAMEALIAS AS ean ,ItemBuyerGroupId AS grupp, inv.netweight as grr, inv.minprice as minprice, uomcv.factor as kastis ,"
    		+ "case when bomunitid = 'pk' then (Select factor from unitofmeasureconversion  u where  u.FROMUNITOFMEASURE = 5637144580 AND u.TOUNITOFMEASURE = 5637144579 and \r\n" + 
    		"    								  u.product = inv.recid)   when   bomunitid = 'kg' then (Select factor from unitofmeasureconversion  u where  u.FROMUNITOFMEASURE = 5637144580 AND u.TOUNITOFMEASURE = 5637144577 and \r\n" + 
    		"    								  u.product = inv.recid) else  1 end as kastis2, bomunitid as unit , pal.factor as pallet " + 
    	
    		" FROM inventtable inv  \r\n" + 
    		" INNER JOIN    inventtablemodule mod ON mod.itemid = inv.itemid AND mod.dataareaid=inv.dataareaid\r\n" + 
    		" LEFT JOIN     ecoresproducttranslation erpt ON inv.product = erpt.product \r\n" + 
    		" LEFT JOIN      ecoresproduct es ON es.displayproductnumber = inv.itemid\r\n" + 
    		"    		LEFT JOIN     unitofmeasureconversion uomcv ON uomcv.product = es.recid" + 
    		"              AND uomcv.FROMUNITOFMEASURE = 5637144580 AND uomcv.TOUNITOFMEASURE = 5637144578 LEFT JOIN     unitofmeasureconversion pal ON pal.product = es.recid\r\n" + 
    		"    		              AND pal.FROMUNITOFMEASURE = 5637144581 AND pal.TOUNITOFMEASURE = 5637144578 " + 
    		"INNER JOIN pricedisctable disc ON inv.itemid=disc.itemrelation AND inv.dataareaid=disc.dataareaid \r\n" + 
    		"AND (getdate()>=disc.fromdate OR disc.fromdate=('1900-01-01 00:00:00.00')) AND (disc.todate>getdate() OR disc.todate=('1900-01-01 00:00:00.00')) \r\n" + 
    	
    		"WHERE  disc.dataareaid='dat' AND disc.accountrelation='Pood' AND inv.dataareaid='dat' AND mod.moduletype=2 AND balsenditem=1\r\n" + 
    		"AND erpt.languageid = 'et'";
          
    String  cmd2 = "  SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock ,  inv.price as oma  FROM INVENTSUM summ " +
             " INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and summ.dataareaid=dim.dataareaid " +
             " INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and summ.dataareaid=inv.dataareaid " +
			 " AND CLOSED = 0 AND  moduletype=0 WHERE summ.DATAAREAID='dat'   and AVAILPHYSICAL > 0  AND dim.inventlocationid IN ('KM') " +
			 " GROUP BY summ.itemid,inv.price ORDER BY summ.itemid " ;
             
    String cmd3 = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock ,avg((case when PHYSICALVALUE > 0 then PHYSICALVALUE else postedvalue end)/AVAILPHYSICAL) as oma  FROM INVENTSUM summ " +
             "INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and summ.dataareaid=dim.dataareaid " +
             "INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and inv.dataareaid=summ.dataareaid " +
             "WHERE summ.DATAAREAID='dat'and dim.DATAAREAID='dat' and inv.DATAAREAID='dat' AND CLOSED = 0 AND  moduletype=0 and AVAILPHYSICAL > 0 " +
             " AND dim.inventlocationid IN ('KS') GROUP BY summ.itemid  ORDER BY summ.itemid ";

    if (isDebug()) System.out.println(cmd);
       if (isDebug()) System.out.println(cmd2);
    TreeMap products = new TreeMap();
    TreeMap recids = new TreeMap();
    try {
      
    	
    	
    	ResultSet rs = stmt.executeQuery(cmd);
      
      while(rs.next()){
        // if ( kood.length() > 2 ){
    	  String recid = rs.getString("recid").trim();
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        
        String nimetus =  rs.getString("nimetus").trim() + " " + kood;
        prod.setNameSorting(kood + "" +nimetus);
        boolean goes = true ;
        if (nimetus.indexOf("***") != -1){
        	goes = false;
        }
        prod.setName(Converter.enCodeEst(nimetus));
       
        prod.setPrice(rs.getDouble("price"));
        prod.setEAN(rs.getString("ean"));
        
        if(eanprods == null) {
     	   eanprods = new TreeMap();
        }
        eanprods.put(prod.getEAN(), prod.getID());
        
        prod.setGroup(parent.make(rs.getString("grupp")));
     
        double maxdisc = 0;
        
        if(maxdisc > 0){
        	prod.setMaxDiscount(maxdisc);
        	prod.setStatusDiscount("1");
        	prod.setName(nimetus.trim() + " MD:" + (int)maxdisc+ "%");
        	System.out.println("MD:" + (int)maxdisc+ "% " +nimetus.trim());
        }
        
       
        
        prod.setStock(0);
        prod.setVat("22");
    
        double wei =rs.getDouble("grr");
    
        if(wei >0.999){
        	prod.idabi = "1";
        	//System.out.println("oh ja suur " +prod.getID() );
            
        }
       
 double oh = rs.getDouble("minprice");
        
        if( oh > 0) {
         	 // prod.setMinPrice(oh);
         	  prod.setPartii("Min.Hind : " + parent.getNF().format(oh) + "€");
           }
        
        
        Double kast = rs.getDouble("kastis");
        Double kast2 = rs.getDouble("kastis2");
        if(kast > 0 ) {
        	 prod.setInCases(kast); 	
          } else {
        	 prod.setInCases(kast2);
        }
        
        if(prod.getID().equals("RUU054") || prod.getID().equals("RUU055") || prod.getID().equals("RUU056")) {
        	prod.setInCases(13);
        }
        
        if(prod.getID().equals("LBC020") ) {
        	prod.setInCases(14);
        }
        
        if(prod.getID().equals("KRR102") ||  prod.getID().equals("JOL101") ||  
        		prod.getID().equals("RMM132") ||  prod.getID().equals("RMM133") ||
        		prod.getID().equals("RMM142") ||
        		prod.getID().equals("RMM143") ||
        		prod.getID().equals("LBC019") ||
        		prod.getID().equals("OPP004")) {
        	prod.setInCases(10);
        }
       
        if(prod.getID().equals("OTH025") || prod.getID().equals("MSS181")
        		|| prod.getID().equals("KKS108") ) {
        	prod.setInCases(20);
        }
        
        if(prod.getID().equals("JGA301")  ) {
        	prod.setInCases(1);
        }
        
        
        if(kastis.get(prod.getID()) != null) {
        	prod.setInCases(kastis.get(prod.getID()));
        //	System.out.println("kastis " + prod.getID() + " " + prod.getInCases());
        }
        
        prod.setUnit(rs.getString("unit"));
        
        Double pallet = rs.getDouble("pallet");
        if(pallet !=null && pallet > 0) {
        	prod.setOnPallet(pallet);
        }
        
        if(goes){
        	 products.put(nimetus, prod);
        	 recids.put(recid, kood);
             prodHash.put(prod.getID(), prod);
        }
        //}
      }
      rs.close();
      ResultSet rs1 = stmt.executeQuery(cmd2);
                            while(rs1.next()){
                              String id = rs1.getString("toode").trim();
                              Product prod1 = (Product)prodHash.get(id);
                              if (prod1 != null){
                                prod1.setStock(rs1.getDouble("stock"));
                              
                           
                              }
                            }
                            
                            rs1 = stmt.executeQuery(cmd3);
                            while(rs1.next()){
                              String id = rs1.getString("toode").trim();
                              Product prod1 = (Product)prodHash.get(id);
                              if (prod1 != null){
                            	  double kog = rs1.getDouble("stock") ;
                            	  if(kog>0){
                                      prod1.setName(prod1.getName() + " |" + kog + "|");  
                            	  }
                                }
                            }

                            makeTexts(recids);
                            
    } catch (Exception e){
      getParent().showError(e);
    }

    
    
    
    return products;
  }


  public String getPilotDayData(Agent agent){


    
    String sql =  " select sum(summa) as summ	from (SELECT (SELECT sum (lineamount) from salesline s where s.dataareaid = 'dat' and  s.salesid = cus.salesid group by s.salesid) as summa\r\n" + 
    		"       		       		 FROM  SALESTABLE cus  INNER JOIN  custtable cu on cu.accountnum = cus.custaccount and cus.dataareaid=cu.dataareaid\r\n" + 
    		"       		       		LEFT OUTER JOIN DEFAULTDIMENSIONVIEW dim ON cus.PARTITION = dim.PARTITION AND cus.DEFAULTDIMENSION = dim.DEFAULTDIMENSION AND dim.NAME = N'Agent' \r\n" + 
    		"       					 WHERE cus.dataareaid='dat'  AND MONTH(cus.createddatetime) = MONTH(getdate()) AND DAY(cus.createddatetime) = DAY(getdate())  and dim.DISPLAYVALUE = '" + agent.getID()+ "' ) as tt";

       String sql2 =  "SELECT sal.salesid AS arve, sal.createddatetime AS kuupaev,sal.deliveryname AS klient  FROM  salestable sal  INNER JOIN  custtable cus on cus.accountnum = sal.custaccount and sal.dataareaid=cus.dataareaid\r\n" + 
       		"LEFT OUTER JOIN DEFAULTDIMENSIONVIEW dim ON cus.PARTITION = dim.PARTITION AND cus.DEFAULTDIMENSION = dim.DEFAULTDIMENSION AND dim.NAME = N'Agent'\r\n" + 
       		"     WHERE sal.dataareaid='dat' AND dim.DISPLAYVALUE ='" + agent.getID()+ "' AND   sal.createddatetime > cast(getdate()-1 as datetime) \r\n" + 
       		"     ORDER BY sal.createddatetime";

    StringBuffer sb = new StringBuffer();
    sb.append("\n\n\n Vormistatud tellimused:\n\n");

      try {
                  ResultSet	      rs2 = stmt.executeQuery(sql2);
                  while (rs2.next()) {
                  sb.append("(" + rs2.getString("arve").trim() + ") [" + rs2.getString("kuupaev").substring(0,10) + "] - " + rs2.getString("klient") +  "\n");
                  }
                  rs2.close();

       
          //stmt.setString(2, agent.getObject());
          ResultSet	      rs = stmt.executeQuery(sql);
//          sb.append("Nimi  \t\tLadu\t\tMüüdud\t\tSumma\n");
          sb.append("---------------------------------------------------\n");

          double totalKokku = 0d;

          while (rs.next()) {
            int ladu = rs.getInt("ladu");
            int kogus = rs.getInt("kogus");
            double hind = round(rs.getDouble("myygihind"), 2);
            double kokku = round(kogus*hind, 2);
            totalKokku = round(totalKokku + kokku, 2);

            //sb.append(rs.getString("nimetus").trim() + " " + rs.getInt("tykke")+ "\n");

            //sb.append("\t\t\t\t" + ladu + "\t\t" + kogus + "\t\t" +
              //kokku + "\n\n");
           }

         sb.append("---------------------------------------------------\n");
          sb.append("\tPäeva müük kokku: " + totalKokku + " EUR");


                  } catch (Exception e) {
                      getParent().showError(e);
                  }
System.out.println(agent.getID() +" " + sb.toString());

      return sb.toString();
  }
  TreeMap agents1 = null;
  
  private boolean isActive(String agent) {
		return  parent.getEmailProps().containsKey(agent);
	  }
	  

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  
  public String getPilotDayData2(){

	  
	  
	  Iterator itera = agents1.keySet().iterator();
			  
			  while (itera.hasNext()) {
				  
				  String agent = (String)itera.next();
			
	  if(isActive(agent)) {
		
	  
       
       String sql =  " SELECT  cus.salesid AS arve, cus.salesname AS nimi,custaccount as klient, cus.deliverydate AS kuup, POSTINGPROFILE as asi, \r\n" + 
       		"       		 (SELECT sum (lineamount) from salesline s where s.dataareaid = 'dat' and  s.salesid = cus.salesid group by s.salesid) as summa\r\n" + 
       		"       		 FROM  SALESTABLE cus  INNER JOIN  custtable cu on cu.accountnum = cus.custaccount and cus.dataareaid=cu.dataareaid\r\n" + 
       		"       		LEFT OUTER JOIN DEFAULTDIMENSIONVIEW dim ON cus.PARTITION = dim.PARTITION AND cus.DEFAULTDIMENSION = dim.DEFAULTDIMENSION AND dim.NAME = N'Agent'\r\n" + 
       		"			 WHERE cus.dataareaid='dat'  AND   cus.deliverydate >= cast(getdate() as datetime) and dim.DISPLAYVALUE = ? \r\n" + 
       		"       		     ORDER BY cus.createddatetime";
    			     
        
StringBuffer sb = new StringBuffer();
  this.connectionUp();
try {
	
	 String header = " Tänased,homsed tehingud :\n\n";
  
    boolean first = true;
    nf.setMinimumFractionDigits(2);
    nf.setMaximumFractionDigits(2);
    nf.setGroupingUsed(false);
      	
   
	Connection con = this.getNewConnection();
PreparedStatement stmt2 = con.prepareStatement(sql);
 stmt2.setString(1, agent);
 String sep= System.getProperty("line.separator");
    ResultSet  rs = stmt2.executeQuery();
 double droidsum = 0;
 double muusum = 0;
 boolean change = false;

 boolean droid = true;
 
 while(rs.next()){
	 String  id = rs.getString("arve").trim();
	 String nimi = rs.getString("nimi");
	 String kood = rs.getString("klient").trim();
     if (nimi == null){
     nimi = " ";
     } else {nimi = nimi.trim();}

 	sb.append("'" + kood + "' " + nimi + "\n");
 	sb.append(rs.getString("kuup").trim().substring(0,10));
 	sb.append("  Nr." + id+ "\t  ");
 	
	String asi = rs.getString("asi").trim();
	
 	double summa = Double.parseDouble(rs.getString("summa").trim());
 	String sum = nf.format(summa) ;
  sb.append(sum + " eur\n" + sep);
   
 
 	
     String sql3 = "select qtyordered as amount,salesunit as unit,  name from SALESLINE where dataareaid = 'dat' and salesid = '" + id + "'";
     Connection cc = this.getNewConnection();
     PreparedStatement stmt3 = cc.prepareStatement(sql3);
 	 
        ResultSet  rs2 = stmt3.executeQuery();
    
 	    while(rs2.next()){
 	    	sb.append("[" +nf.format(rs2.getDouble("amount"))+"] " 
 	       + rs2.getString("unit").trim()+ " " + rs2.getString("name").trim() + "\n");
 	    }
    
 	    rs2.close();
        stmt3.close();
        cc.close();
     	sb.append( "\n\n");
 	 	  
     
	if(asi.equals("TAVALINE")){
  		droidsum = droidsum + summa;
  	}else{
       change = true;
  		muusum = muusum + summa;
  		if(droid && droidsum > 0 ){
  			droid = false;
  			sb.append("------------------------------\n");
  			sb.append("Androidist Kokku :" + nf.format(droidsum)  + " eur\n\n");
  		}
  	}
 }
 rs.close();
 stmt2.close();
con.close(); 	 
	if(droid && droidsum > 0 ){
			droid = false;
			sb.append("------------------------------\n");
			sb.append("Androidist Kokku :" + nf.format(droidsum)  + " eur\n\n");
		}
 
 if(muusum > 0){
		sb.append("------------------------------\n");
		sb.append("E-tellimused Kokku :" + nf.format(muusum)  + " eur");
		 
 }
 
} catch (Exception e) {
 getParent().showError(e);
}

	  

connectionDown();



Converter.saveFile(parent.getProps().getProperty("stockrefdir") + "st"+ agent + "report.txt", sb.toString());
System.out.println(sb.toString());
			  }}
return "";//sb.toString();

    }
  
  
  

  public Vector getCustomerProductsList(String agentCode){
    Vector custProds = new Vector();
    //if (true) return custProds ;

   String cmd = " SELECT jou.orderaccount,tra.itemid,tra.qty AS kogus ,jou.invoicedate AS lastdate ,max( tra.lineamount) as hind " +
      " from custinvoicetrans  tra INNER JOIN custinvoicejour jou ON " +
      " tra.invoiceid=jou.invoiceid and tra.dataareaid=jou.dataareaid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid and cus.dataareaid=tra.dataareaid " + 
      " WHERE  jou.invoicedate > getdate()-90 and tra.dataareaid = 'dat'  and tra.qty != 0 GROUP BY jou.orderaccount,tra.itemid,tra.qty,jou.invoicedate  " +
      " ORDER BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty  " ;


    if (isDebug()) System.out.println(cmd);

    try {
    	Connection con = this.getNewConnection();
    	Statement stmt =  con.createStatement();
       ResultSet rs = stmt.executeQuery(cmd);
   
      while(rs.next()){
        String kliendikood = rs.getString("orderaccount").trim();
        String tootekood = rs.getString("itemid").trim();
        double kogus = rs.getDouble("kogus");
        String lastdate = rs.getString("lastdate").substring(0,10);
                 lastdate = lastdate.substring(5,10);
                 double hind = rs.getDouble("hind");
                 if(kogus != 0) {
                	 hind = hind / kogus;
                 }
            
        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,hind,0,0," ");

        custProds.add(cp);
      }
      rs.close();
      stmt.close();
      con.close();
    } catch (Exception e){
      getParent().showError(e);

    }
    
    getCustomerProductsList2(custProds);
    return custProds;
  }


  public Vector getCustomerProductsList2(Vector custProds){
	 
	   String cmd = " SELECT jou.orderaccount,tra.itemid,tra.qty AS kogus ,jou.invoicedate AS lastdate ,max( tra.lineamount/tra.qty) as hind " +
	      " from custinvoicetrans  tra INNER JOIN custinvoicejour jou ON " +
	      " tra.invoiceid=jou.invoiceid and tra.dataareaid=jou.dataareaid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid and cus.dataareaid=tra.dataareaid " + 
	      " WHERE  jou.invoicedate > getdate()-90 and tra.dataareaid = 'bal'  and tra.qty != 0 GROUP BY jou.orderaccount,tra.itemid,tra.qty,jou.invoicedate  " +
	      " ORDER BY jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate  " ;


	    if (isDebug()) System.out.println(cmd);

	    try {
	    	Connection con = this.getNewConnection2();
	    	Statement stmt =  con.createStatement();
	       ResultSet rs = stmt.executeQuery(cmd);
	   
	      while(rs.next()){
	        String kliendikood = rs.getString("orderaccount").trim();
	        String tootekood = rs.getString("itemid").trim();
	        double kogus = rs.getDouble("kogus");
	        String lastdate = rs.getString("lastdate").substring(0,10);
	                 lastdate = lastdate.substring(5,10);
	                 double hind = rs.getDouble("hind");
	            
	        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,hind,0,0," ");

	        custProds.add(cp);
	      }
	      rs.close();
	      stmt.close();
	      con.close();
	    } catch (Exception e){
	      getParent().showError(e);

	    }
	    return custProds;
	  }




  public double getSalary(String agentCode) {

     
      return 0;
    }


  public void setBulkSales(Map agents){
this.connectionUp();
    String cmd =
        " SELECT dd.displayvalue as agent, sum(lineamountmst) AS summa FROM custinvoicetrans sal \r\n" + 
        "LEFT JOIN defaultdimensionview dd ON sal.defaultdimension = dd.defaultdimension AND dd.partition = sal.partition\r\n" + 
        "LEFT JOIN dbo.dimattributefinancialtag aft ON aft.value = dd.displayvalue AND aft.partition = dd.partition\r\n" + 
        "WHERE MONTH(sal.invoicedate) = MONTH(getdate()) AND YEAR(sal.invoicedate) = YEAR(getdate()) AND sal.dataareaid= 'dat' \r\n" + 
        "AND dd.NAME = 'Agent' AND aft.category = 5637144580 and sal.itemid not like 'T%' GROUP BY dd.displayvalue";
    
    
       if (isDebug()) System.out.println(cmd);
          try {
        	  Connection con = this.getNewConnection();
          	Statement stmt =  con.createStatement();
              ResultSet	      rs = stmt.executeQuery(cmd);
              while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                  agent.setMonthSales((int)rs.getDouble("summa"));
                 // System.out.println(agent.getMonthSales());
                }
              }
              rs.close();
              stmt.close();
              con.close();
          } catch (Exception e) {
              getParent().showError(e);
           }
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
    return "IceAxaUus";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  private HashMap ketid = null;
  private HashMap sortimendid = null;
  private String url = "jdbc:odbc:AXAUUS";
  private String url2 = "jdbc:odbc:AXA_Live";
  private Connection con;
  private Statement stmt;
  private String subjectFilesTo = null;
  
  
@Override
public Vector getPriceMatrixData() {
	
	//getProducts(null);
    Vector lines = new Vector();
   String sql = " SELECT  cus.invoiceaccount as custid , (accountnum) as klient,  '' AS clgroup, "+ 
			"  (case when pd.itemcode = 0 then pd.itemrelation else ''end) AS prodid,  " +
			 "  (case when pd.itemcode = 1 then pd.itemrelation else ''end) AS prodgroup,(percent1) AS discount,(amount) " +
            " AS price ,pd.fromdate as algus , pd.todate as lopp  ,  datediff(day,pd.fromdate,pd.todate) as paevi  FROM pricedisctable pd left join custtable cus on cus.accountnum = pd.accountrelation and cus.dataareaid=pd.dataareaid where  " +
			 "   pd.dataareaid = 'dat'   and cus.dataareaid = 'dat'   " +
			 "  and  pd.relation in (4,5) and" +
			 "    ((GETDATE() between pd.FROMDATE-(case when DATEPART(DW, pd.FROMDATE) = 2 then 3 else 1 end) AND pd.TODATE) " +
	  " or(GETDATE()>=pd.FROMDATE AND year(pd.toDATE) =1900) " +
	  " or(year(pd.FROMDATE) =1900 and year(pd.toDATE) =1900) " +
	  " or( GETDATE()<=pd.TODATE+1)) " +
		"	   order by  cus.invoiceaccount,accountnum,pd.itemrelation";

    try {

    	HashSet added = new HashSet();

  	Statement stmt = this.getNewConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {

                    String custID = rs.getString("custid");
                    String klient = rs.getString("klient");
                    String clGroup = rs.getString("clgroup");
                    String prodID = rs.getString("prodid");
                    String prodGroup = rs.getString("prodgroup");
                    double discount = rs.getDouble("discount");
                    double price = rs.getDouble("price");
                    String fromdate = rs.getString("algus");
                    String todate = rs.getString("lopp");
                    int paevi = rs.getInt("paevi");
                    
                    MatrixLine mline = new MatrixLine();
                    String asi = "";

                   
                    if (klient != null && klient.trim().length() > 0) {
                        mline.setCustID(klient.trim());
                        asi = asi + klient.trim();
                     }
                    
                    if (custID != null && custID.trim().length() > 0) {
                            mline.setCustID(custID.trim());
                            asi = asi + custID.trim();
                            
                    }
                    
                    if (mline.getCustID() == null && clGroup != null
                                    && clGroup.trim().length() > 0) {
                            mline.setCliGroup(clGroup.trim());
                            mline.setDiscount(discount);
                    }

                    if (prodID != null && prodID.trim().length() > 0) {
                            mline.setProdID(prodID.trim());
                            asi = asi + prodID.trim();
                    }
                    
                    if (mline.getProdID() == null && prodGroup != null
                                    && prodGroup.trim().length() > 0) {
                            mline.setProdGroup(prodGroup);
                    }
                  
                    
                    
                    if( mline.getCustID() != null) {
                	
                    if (price > 0) {
                            mline.setPrice(price);
                    } else {
                    	
                    	if(prodID != null && prodHash.get(prodID.trim()) != null ) {
                        	Product pr = (Product) prodHash.get(prodID.trim());
                        	if(pr.getPrice() > 0) {
                            	price = pr.getPrice() * (100 - discount) / 100;
                                mline.setPrice(price);
                        	}else {

                            	mline.setDiscount(discount);
                                    		
                        	}
                        } else {
                        	mline.setDiscount(discount);	
                        }
                        
                    }
                    }
                    
                    if(paevi < 180){
                    	mline.setStartDate(fromdate.substring(0,10));
                        mline.setEndDate(todate.substring(0,10));
                    }else {
                    	mline.setProdLevel("pr");
                    }
                    
                  if(!added.contains(asi)){
                	  added.add(asi);
                	  lines.add(mline);
                    }
                    
            }
            rs.close();
            
            
            String sql2 = " select pd.accountrelation AS clgroup, " +
			  " (case when pd.itemcode = 0 then pd.itemrelation else ''end) AS prodid, " +  
			  " (case when pd.itemcode = 1 then pd.itemrelation else ''end) AS prodgroup,(percent1) AS discount, " +
			  "   pd.fromdate as algus , pd.todate as lopp  ,  datediff(day,pd.fromdate,pd.todate) as paevi from  " +
			  "   pricedisctable pd where   getdate() between pd.fromdate -3 and pd.todate  and pd.accountcode=1 and percent1 > 0 " +
			  "    and dataareaid = 'dat' order by pd.accountrelation , pd.itemrelation, pd.fromdate desc " ;
            
            rs = stmt.executeQuery(sql2);
HashSet conta = new HashSet();
            while (rs.next()) {

                    String clGroup = rs.getString("clgroup");
                    String prodID = rs.getString("prodid");
                    String prodGroup = rs.getString("prodgroup");
                    double discount = rs.getDouble("discount");
                    String fromdate = rs.getString("algus");
                    String todate = rs.getString("lopp");
                    double price = 0;
                    
                    String asi = "";
                    if(prodID != null && prodHash.get(prodID) != null) {
                    	Product pr = (Product) prodHash.get(prodID);
                    	if(pr.getPrice() > 0) {
                    	  	price = pr.getPrice() * (100 - discount) / 100;
                      }
                    }
                    int paevi = rs.getInt("paevi");
                    MatrixLine mline = new MatrixLine();
                    
                
                    if (clGroup != null && clGroup.trim().length() > 0) {
                            mline.setCliGroup(clGroup.trim());
                             asi = clGroup.trim();
                    }

                    if (prodID != null && prodID.trim().length() > 0) {
                            mline.setProdID(prodID.trim());
                            asi = asi+ "-" + prodID.trim();
                    }
                    if (mline.getProdID() == null && prodGroup != null
                                    && prodGroup.trim().length() > 0) {
                            mline.setProdGroup(prodGroup);
                    }
                    
                    if(price > 0 ) {
                    	mline.setPrice(price);
                    }else {
                    	  mline.setDiscount(discount);
                    }
                     
                   
                    if(paevi < 180){
                        
                    	mline.setStartDate(fromdate.substring(0,10));
                        mline.setEndDate(todate.substring(0,10));
                     // 	mline.setLineinfo("TOP");
                      //	mline.setProdLevel("ca");
						
                        
                    }else{
                    	
                    	mline.setProdLevel("pr");
                    
                    }
               
                    if(!added.contains(asi)){
                    	  added.add(asi);
                          lines.add(mline);	
                    }
                  	
            }
            
            
            

    } catch (SQLException ex) {
    ex.printStackTrace();
    }
    return lines;
}



@Override
public String getProductSortingData() {
	// TODO Auto-generated method stub
	return null;
}



@Override
public Vector getLastThreeData() {
	// TODO Auto-generated method stub
	return null;
}



}
