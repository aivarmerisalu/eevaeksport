package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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



public class IceAxaHRCDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {
  public IceAxaHRCDBDriver(EksportPanel parent){
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
      }
   
   String user =  null;
   String pass = null;
   String rcompany = null; 
   
  

		  public Vector getFixedAssortmentData() {
					return null;
		
			}



	 
		public String getSaveStockInfo() {
			connectionUp();
	       String cmd = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock FROM INVENTSUM summ " +
                 "INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and summ.dataareaid=dim.dataareaid " +
                 "INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and summ.dataareaid=inv.dataareaid " +
                 "WHERE summ.DATAAREAID='bal'and dim.DATAAREAID='bal' and inv.DATAAREAID='bal' AND CLOSED = 0 AND  moduletype=0 " +
                 " AND dim.inventlocationid IN ('KM')  GROUP BY summ.itemid  ORDER BY summ.itemid ";

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
			return products.toString();
		}



              public String getDealerCustomerCSV(String agent){
               return null;
               }

          	public String getSaveDebtInfo() {
        		
          	  String cmd3 = "SELECT cus.accountnum as klient," +
              "(SELECT SUM(amountcur) FROM custtransopen co WHERE co.duedate<getdate()" +
              " AND co.accountnum = cus.accountnum) AS debt ,blocked as blokk , cus.paymtermid as pay " +
         " FROM custtable cus  WHERE  cus.dataareaid='bal'  and closedcustomer=0  and len(cus.dimension2_) >0  ";

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
    return DriverManager.getConnection(url, "bmssa", "bmssa_pwd");
  }

  public Vector getDebt(String agentCode) throws SQLException{
    Vector list = new Vector();

    String sql = "SELECT  a.accountnum AS klient,a.invoice AS number,co.duedate AS maksepaev,co.amountcur" +
    		" AS tasumata, " +
        "co.transdate AS kuupaev FROM custtransopen co INNER JOIN custtrans a ON co.REFRECID=a.recid and co.dataareaid=a.dataareaid " +
        "WHERE  co.dataareaid='bal' AND a.dataareaid='bal' ORDER BY a.accountnum,co.transdate ";
    Connection con = this.getNewConnection();
	Statement stmt =  con.createStatement();
    ResultSet rs = stmt.executeQuery(sql);
    while(rs.next()){
      Debt debt = new Debt();
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
	  
   // String cmd = "SELECT num, description FROM dimensions  WHERE dataareaid='bal' " +
     //      " AND dimensioncode=1   ";
    
    String cmd = "SELECT  cus.accountnum, cus.name" +
         " FROM custtable cus  LEFT JOIN custgroup cp on cp.custgroup = cus.custgroup and cp.dataareaid=cus.dataareaid " +
			" WHERE  cus.dataareaid='bal' and cp.dataareaid='bal' and (closedcustomer=0  or cus.accountnum in ('2183','7588','3274','5755') )   "; // and len(cus.dimension2_) >0

 
    
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("accountnum").trim());
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
        }
        
        

       
        
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
                " WHERE dataareaid='bal' and ItemBuyerGroupId != '' Group by ItemBuyerGroupId";
     Connection con = this.getNewConnection();
 	Statement stmt =  con.createStatement();
 	ResultSet rs = stmt.executeQuery(sql);

     while (rs.next()) {
       String liik=rs.getString("liik").trim();
       String nimetus=rs.getString("nimetus").trim();
       if (liik != ""){
       tm.put(liik,nimetus);
       }
     }
     rs.close();
     stmt.close();
     con.close();
     return tm;
 }

  
     
  
  public TreeMap getCustomers(Agent agent){
    String cmd = "SELECT cus.name, cus.accountnum, " +
               "  (SELECT SUM(amountcur) FROM custtransopen co WHERE co.duedate<getdate() " +
               "   AND co.accountnum = cus.accountnum) AS debt, cus.linedisc , " +
               "  cus.address, cus.DayOfVisit AS visit ,cus.PilotEAN AS useean ,cus.Pilotdepot AS mituladu,cus.dimension2_ as agent, " + 
               "  cus.bankcentralbankpurposetext as agent2 ,blocked ,phone , cellularphone , regnum_w as kett ,cp.name as chain , "
               + " custclassificationid as type , zipcode as indeks , state as region , city ,street ,cus.paymtermid as pay " +
            " FROM custtable cus  LEFT JOIN custgroup cp on cp.custgroup = cus.custgroup and cp.dataareaid=cus.dataareaid " +
			" WHERE  cus.dataareaid='bal' and cp.dataareaid='bal' and (closedcustomer=0  or cus.accountnum in ('2183','7588','3274','5755') )   "; // and len(cus.dimension2_) >0

    
    HashSet custs = new HashSet();
	  
   
	  sortimendid = new HashMap();
	  ketid = new HashMap();
	  HashSet asses = new HashSet();

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
           //HashMap custHash = new HashMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
      Statement stm = getNewConnection().createStatement();
      while(rs.next()){
        String name = rs.getString("name").trim();
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);
        
        cust.setName(name);
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
        
       
        
        String aadr = rs.getString("address");
        if (aadr != null) {
          cust.setAddress(aadr.trim());
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
         

         
         if(ag != null && ag.trim().length() > 0) {
    	  cust.setMainAgent(ag.trim()); 
          }
            
         
         cust.addAgent(new Agent(accountnum));


    	 
         String agentAGENTS = getParent().getProps().getProperty(ag);

         if(agentAGENTS != null){
	         StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
	  	       while(stag.hasMoreTokens()){
             cust.addAgent(new Agent(stag.nextToken()));
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
         
         String cellularphone = rs.getString("cellularphone");
         if(cellularphone != null && cellularphone.trim().length()>0){
         	if(cust.getPersonalData().trim().length() > 0){
         		cust.setPersonalData(cust.getPersonalData() + " " + cellularphone.replaceAll(";",",").trim());
         	}else{
         		cust.setPersonalData(cellularphone.replaceAll(";",",").trim());	
         	}
        cust.restPerPhone = cellularphone.replaceAll(";",",").trim();
        }
         
         
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
         
                     
               
        String cmd2 = "SELECT disc.percent1 AS discount FROM pricedisctable disc  WHERE disc.accountrelation='" + aleke + "' AND " +
               " disc.dataareaid='bal' AND len(disc.itemrelation)=0  AND fromdate<=getdate() and todate >= getdate()";
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
                    " WHERE     (dbo.ASSETLENDING.ACTUALRETURN = CONVERT(DATETIME, '1900-01-01 00:00:00', 102)) AND (dbo.ASSETLENDING.DATAAREAID = 'bal')" +
                    " and  dbo.ASSETLENDING.BALCUSTACCOUNT='" + accountnum + "'";
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
    	  		" b.createddate as born , LENDINGDATE as contract ,a.BALCUSTACCOUNT as custcode " +
		    " FROM dbo.ASSETLENDING a LEFT OUTER JOIN " +
                   "  dbo.ASSETTABLE b ON a.ASSETID = b.ASSETID and a.DATAAREAID = b.DATAAREAID " +
                   "  WHERE     (a.ACTUALRETURN = CONVERT(DATETIME, '1900-01-01 00:00:00', 102)) AND (a.DATAAREAID = 'bal') " +
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
    	  
    	  
                     
                     System.out.println("Counter " + counter + " Asses " + asses.size() + " " + vec.size());
                     
    	   Converter.sendAssetsJson(vec,this.user,this.pass,this.rcompany);     
    	   
    	     System.out.println(" Asses saadetud  lähen tegema seoseid" + asses.size());
                		
             		
             		
    	   //siin saadame seosed ära jujul kui customer on olemas selline 
     	   vec = new Vector(); 
     	   cmd3 = "select a.ASSETID as itemcode ,b.name , '' as ean ,'' as nfcTag, a.ASSETID as factoryID ,b.make as description , " + 
      	  		" b.createddate as born , LENDINGDATE as contract ,a.BALCUSTACCOUNT as custcode " +
  		    " FROM dbo.ASSETLENDING a LEFT OUTER JOIN " +
                     "  dbo.ASSETTABLE b ON a.ASSETID = b.ASSETID and a.DATAAREAID = b.DATAAREAID " +
                     "  WHERE     (a.ACTUALRETURN = CONVERT(DATETIME, '1900-01-01 00:00:00', 102)) AND (a.DATAAREAID = 'bal')  " +
  			//	"	and  a.BALCUSTACCOUNT != '1005' " +
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
                         
                         if(code.indexOf("1104090041") != -1){
                        //	 System.out.println("1104090041 " + custcode +" "+ (asses.contains(code)) + " " +custs.contains(custcode));
                         }
                       
                         if(asses.contains(code) && custs.contains(custcode) ){
                        	  String olemas = (String)asscusts.get(code);
                        	  if(olemas == null){
                                  vec.add(obj);  
                                  cou++;  
                         	  }else if(olemas != null){
                        		 // if(!olemas.equals(custcode)){
                        			   vec.add(obj);  
                                       cou++; 
                        		  //}
                        	  }
                          }
                       }
                  
                      rs1.close();
                      stm.close();
    	   System.out.println("assets cou " +  cou + " "+ coua + " " + vec.size());
                      
                      // siis peaks tegema vahele kontrolli et kui on muudatus siis ainult laksad ylesse
    	   
    	   
    	   
                    Converter.sendAssetsCustomerJson(vec,this.user,this.pass,this.rcompany);     
               	   
                    System.out.println("assets saadetud " +  cou + " "+ coua);
    	   
    	   
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
              " WHERE refcompanyid='bal' and TypeID ='Märkus' and name like '%koos%'";
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
  
  
  public TreeMap getProducts(Agent agent){
	  
	 // TreeMap soodused = getSoodused();
	  
	  prodHash = new HashMap();
    String obj =  "";
    if("true".equals(parent.getProps().getProperty("tartuladu"))){
    	 obj = "AND dim.inventlocationid IN ('NK') ";    	
    }else{
    	 obj = "AND dim.inventlocationid IN ('KM') ";
	
    }
    
    
    String obj2 =  "";
    if(!"true".equals(parent.getProps().getProperty("tartuladu"))){
    	 obj2 = "AND inventlocationid not IN ('NK') ";    	
    }
    
    String cmd = "SELECT inv.recid,inv.itemid, left(ItemBuyerGroupId,4) as abix , inv.itemname AS nimetus,inv.itemname" +
    		" as kontroll, inv.itemqtyinabox AS kastis,  disc.amount AS price " +
               ", NAMEALIAS AS ean ,ItemBuyerGroupId AS grupp ,MINAVERAGESETTLE as maxdisc, inv.netweight as grr FROM inventtable inv  " +
                 "INNER JOIN inventtablemodule mod ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid" +
                 " INNER JOIN inventitemgroup gr ON inv.itemgroupid= gr.itemgroupid and inv.dataareaid=gr.dataareaid " +
                 " INNER JOIN pricedisctable disc ON inv.itemid=disc.itemrelation and inv.dataareaid=disc.dataareaid " +
                 "and (getdate()>=disc.fromdate OR disc.fromdate=('1900-01-01 00:00:00.00') )   and (disc.todate>getdate() OR disc.todate=('1900-01-01 00:00:00.00') ) " +
                 " WHERE  disc.dataareaid='bal' AND disc.accountrelation='Pood' AND inv.dataareaid='bal'AND gr.dataareaid='bal' AND mod.dataareaid='bal' AND moduletype=2  " +
                 " AND balsenditem=1 " + obj2 ;
             String cmd2 = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock , "
             		+ " inv.price as oma  FROM INVENTSUM summ " +
                 "INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and summ.dataareaid=dim.dataareaid " +
                 "INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and summ.dataareaid=inv.dataareaid " +
                 "WHERE summ.DATAAREAID='bal'and dim.DATAAREAID='bal' and inv.DATAAREAID='bal' AND CLOSED = 0 AND  moduletype=1 and AVAILPHYSICAL != 0  " +
                 "" + obj + " GROUP BY summ.itemid,inv.price ORDER BY summ.itemid ";

              cmd2 = "  SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock ,  inv.price as oma  FROM INVENTSUM summ " +
             " INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and summ.dataareaid=dim.dataareaid " +
             " INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and summ.dataareaid=inv.dataareaid " +
			 " AND CLOSED = 0 AND  moduletype=0 WHERE summ.DATAAREAID='bal'   and AVAILPHYSICAL != 0  " +
			 "" + obj + " GROUP BY summ.itemid,inv.price ORDER BY summ.itemid " ;
             
             String cmd3 = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock ,avg((case when PHYSICALVALUE > 0 then PHYSICALVALUE else postedvalue end)/AVAILPHYSICAL) as oma  FROM INVENTSUM summ " +
             "INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and summ.dataareaid=dim.dataareaid " +
             "INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and inv.dataareaid=summ.dataareaid " +
             "WHERE summ.DATAAREAID='bal'and dim.DATAAREAID='bal' and inv.DATAAREAID='bal' AND CLOSED = 0 AND  moduletype=0 and AVAILPHYSICAL != 0 " +
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
        
        String nimetus =  rs.getString("nimetus").trim();
        prod.setNameSorting(kood + "" +nimetus);
        boolean goes = true ;
        if (nimetus.indexOf("***") != -1){
        	goes = false;
        }
        prod.setName(nimetus);
        prod.setInCases(rs.getDouble("kastis"));
        prod.setPrice(rs.getDouble("price"));
        prod.setEAN(rs.getString("ean"));
        prod.setGroup(parent.make(rs.getString("grupp")));
       // double maxdisc = rs.getDouble("maxdisc");
       double maxdisc = 0;
      //  if(soodused.get(prod.getID()) != null){
        //	maxdisc = Double.parseDouble(soodused.get(prod.getID()) + "");
        	//System.out.println("sssMD:" + (int)maxdisc+ "% " +nimetus.trim());
            
        //}
        
        if(maxdisc > 0){
        	prod.setMaxDiscount(maxdisc);
        	prod.setStatusDiscount("1");
        	prod.setName(nimetus.trim() + " MD:" + (int)maxdisc+ "%");
        	System.out.println("MD:" + (int)maxdisc+ "% " +nimetus.trim());
        }
        
        prod.setStock(0);
        prod.setVat("20");
    
        double wei =rs.getDouble("grr");
    
        if(wei >0.999){
        	prod.idabi = "1";
        	//System.out.println("oh ja suur " +prod.getID() );
            
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
                              
                              double oh = rs1.getDouble("oma");
                              boolean goes = false;
                              if(prod1.getID().startsWith("JKN") || prod1.getID().startsWith("JOB") ){
                            	  oh = oh * 1.33;
                            	  goes = true;
                              }else if(prod1.idabi != null && prod1.idabi.equals("1")){
                            	  oh = oh * 1.22;
                            	  goes = true;
                              }
                              
                           //   System.out.println("oh " +prod1.getName() + " " + prod1.idabi +" "+ oh);
                              
                             // kui pole kampaania staatuses 
                              if(goes && oh > 0){
                            	  //prod1.setMinPrice(oh);
                            	   
                            	  prod1.setPartii("Min.Hind : " + parent.getNF().format(oh) + "€");
                              }
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
      return null;
  }



  public Vector getCustomerProductsList(String agentCode){
    Vector custProds = new Vector();
    //if (true) return custProds ;

   String cmd = " SELECT jou.orderaccount,tra.itemid,tra.qty AS kogus ,jou.invoicedate AS lastdate ,max( tra.lineamount/tra.qty) as hind " +
      " from custinvoicetrans  tra INNER JOIN custinvoicejour jou ON " +
      " tra.invoiceid=jou.invoiceid and tra.dataareaid=jou.dataareaid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid and cus.dataareaid=tra.dataareaid " + 
      " WHERE  jou.invoicedate > getdate()-90 and tra.dataareaid = 'bal'  and tra.qty != 0 GROUP BY jou.orderaccount,tra.itemid,tra.qty,jou.invoicedate  " +
      " ORDER BY jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate  " ;


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


    String cmd =  "  SELECT jou.orderaccount as agent ,sum(lineamountmst) AS summa from  custinvoicetrans sal INNER JOIN custinvoicejour jou ON " + 
        "         jou.invoiceid=sal.invoiceid and jou.dataareaid=sal.dataareaid  WHERE " + 
        "	   MONTH(sal.invoicedate) = MONTH(getdate()) AND YEAR(sal.invoicedate) = YEAR(getdate()) " + 
        "        AND sal.dataareaid='bal' group by jou.orderaccount " ;
    
    
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
    return "IceAxaHRC";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  private HashMap ketid = null;
  private HashMap sortimendid = null;
  private String url = "jdbc:odbc:AXA_Live";
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
			 "   pd.dataareaid = 'bal'   and cus.dataareaid = 'bal'   " +
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
                    
                    if(paevi < 70){
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
			  "    and dataareaid = 'bal' order by pd.accountrelation , pd.itemrelation " ;
            
            rs = stmt.executeQuery(sql2);

            while (rs.next()) {

                    String clGroup = rs.getString("clgroup");
                    String prodID = rs.getString("prodid");
                    String prodGroup = rs.getString("prodgroup");
                    double discount = rs.getDouble("discount");
                    String fromdate = rs.getString("algus");
                    String todate = rs.getString("lopp");
                    double price = 0;
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
                    }

                    if (prodID != null && prodID.trim().length() > 0) {
                            mline.setProdID(prodID.trim());
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
                     
                   
                    if(paevi < 70){
                        
                    	mline.setStartDate(fromdate.substring(0,10));
                        mline.setEndDate(todate.substring(0,10));
                     // 	mline.setLineinfo("TOP");
                      //	mline.setProdLevel("ca");
						
                        
                    }else{
                    	
                    	mline.setProdLevel("pr");
                    
                    }
               
                  
                    lines.add(mline);
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
