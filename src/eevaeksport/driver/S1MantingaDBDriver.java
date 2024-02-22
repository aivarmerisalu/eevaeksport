package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Product;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class S1MantingaDBDriver
    extends AbstractDBDriver implements BulkUpdater,Discounter {
  public S1MantingaDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
   
    }
    catch (Exception e) {
      parent.showError(e);
    }
    
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
  }
  
  
  String user =  null;
  String pass = null;
  String rcompany = null;

  public String getCustProdDiscounts(Agent agent) {
    return null;
  }

  public String getAssortmentStr(Agent agent){
  	return null;
  }
  public String getDealerCustomerCSV(String agent) {
    return null;
  }
  public String getSaveDebtInfo() {
   	return null;
   }
   
  /**
   *
   */
  public void connectionUp() {
  }

  public TreeMap getProdGroups() throws SQLException {
    TreeMap tm = new TreeMap();
    Connection con2 = DriverManager.getConnection(url);
    Statement stmt1 = con2.createStatement();
    String sql = "SELECT id AS liik, descr AS nimetus FROM SC157 WHERE isfolder=1 ORDER BY descr ";
    try {
      ResultSet rs = stmt1.executeQuery(sql);
      while (rs.next()) {
        tm.put(rs.getString("liik").trim(),
        		Converter.enCodeEst(rs.getString("nimetus").replaceAll("Sasaldeti ", " ").trim()));
      }
      rs.close();
      stmt1.close();
      con2.close();
    }
    catch (Exception e) {
      getParent().showError(e);
    }

    return tm;
  }


  public String getSaveStockInfo() {
    return null;
  }

  /**
   *
   */
  public void connectionDown() {
  }

  public TreeMap getAgents() {
    String cmd = "SELECT id,descr FROM SC232 ";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    TreeMap agents1 = new TreeMap();
    try {
    	
    	Connection con = DriverManager.getConnection(url);
    	Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      while (rs.next()) {
        Agent agent = new Agent(rs.getString("id").trim());
        String name = rs.getString("descr").trim();
        agent.setName(name);
        agents.put(name, agent);
        agents1.put(agent.getID(), agent);
      }
      rs.close();
      stmt.close();
      con.close();
    }
    catch (Exception e) {
      getParent().showError(e);
    }
    
    return agents;
  }

  public void setDealers(TreeMap aDealers) {
   
  }
  
  
  public TreeMap getAgents1() {
	   
		return agents1;
	}
   

   private TreeMap agents1 = new TreeMap();

  public TreeMap getCustomers(Agent agent) {
	  
	  
	  try{
		   Converter.getManagersReplacements(agents1, rcompany, user, pass); 
	}catch(Exception e){

	}
	  
	  

      String salessql = " SELECT  jou.orderaccount as klient,( ltrim( iif when month(sal.invoicedate) <10 then  '0' + ltrim(str(month(sal.invoicedate))) else ltrim(str(month(sal.invoicedate))) end) ) as kuu, " +
   "  round(SUM(lineamount),0) AS summa " +
   "  FROM custinvoicetrans sal with(nolock) INNER JOIN custinvoicejour jou with(nolock) ON  " +
   " sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid   " +
	" WHERE sal.dataareaid='ct'   AND jou.dataareaid='ct' AND sal.invoicedate > getdate()-200 " +
   " GROUP BY jou.orderaccount ,(YEAR(sal.invoicedate)),(month(sal.invoicedate))  order by   jou.orderaccount ,YEAR(sal.invoicedate), month(sal.invoicedate) ";
	 
      
     salessql = " SELECT kl.code as klient,  ltrim(str(year(date)))+( iif( month(date) <10 ,  '0' + ltrim(str(month(date))) , ltrim(str(month(date)))  ) ) as kuu\r\n" + 
     		"     		    		 ,sum(info.sp620) AS summa  FROM dt626 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc \r\n" + 
     		"     		      		         INNER JOIN dh626 abi ON abi.iddoc=info.iddoc  inner join sc138 kl on rtrim(ltrim(kl.id)) =  rtrim(ltrim(SP599)) \r\n" + 
     		"     		      		         WHERE len(ltrim(pais.ismark))=0 AND  date > date()-150 \r\n" + 
     		"     		      		         GROUP BY 1,2 order by 1,2";
      
    String cmd = " SELECT kl.code AS kood,kl.descr AS nimi,rtrim(ltrim(sp134)) AS address,'' AS visit,0 AS ale  ,'' AS blokk ,0 AS toall,kl.parentid AS payer,kl.sp132 AS agent " + 
    		"    		        FROM sc138 kl left JOIN 1sconst c ON c.objid=kl.id  left join 1SBLOB b on b.objid=kl.id    where ltrim(rtrim(kl.sp132)) != '0' and block not like '%closed%' ";
    
    if (isDebug()) System.out.println(cmd);

    TreeMap customers = new TreeMap();
    HashMap set = new HashMap();
    HashMap salesres = new HashMap();
    
    TreeMap<String,Toode> deld =  getDeliveryDates();
    try {
    
    	
    	Connection con = DriverManager.getConnection(url);
    	Statement stmr = con.createStatement();
    	
        ResultSet rs1 = stmr.executeQuery(salessql);
        while(rs1.next()){
        	String klient = rs1.getString("klient").trim();
        	String kuu = rs1.getString("kuu").trim();
        	int summa = (int)rs1.getDouble("summa");
          if(salesres.get(klient) == null){
        	  salesres.put(klient,"   " + kuu + " : " + summa + " " );
          }else{
        	  String asi = (String)salesres.get(klient);     	  
        	  salesres.put(klient,"   " + kuu + " : " + summa +" |" + asi);
          }
        }
       rs1.close();
       stmr.close();
    	
    	
    
    	
     PreparedStatement   pstmt = con.prepareStatement(cmd);
    //  pstmt.setString(1, agent.getID());
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        String name = rs.getString("nimi").trim();
        String accountnum = rs.getString("kood").trim();
        agent = new Agent(rs.getString("agent").trim());
        Customer cust = new Customer(accountnum);
        cust.setName(enCodeEst(name));
        cust.addAgent(agent);
        
        Agent agg = ((Agent)this.getAgents1().get(agent.getID()));
			
			if(agg != null && agg.getDubbler().size()>0){
				for (int r = 0 ; r < agg.getDubbler().size() ; r++){
					Agent dub = new Agent((String)agg.getDubbler().elementAt(r));
					cust.addAgent(dub);
				}
			}
		
        
        
        cust.setAddress(Converter.adressLink(enCodeEst(rs.getString("address").trim()),"Latvia"));
        String visit =rs.getString("visit");
        if(visit!= null & visit.trim().length()>0){
          cust.setVisit(visit.trim());
        }
        String ale = rs.getString("ale");
        if (ale != null && ale.trim().length() > 0) {
          cust.setDiscount(Double.parseDouble(ale));
        }
        boolean blokk = rs.getBoolean("blokk");
        if (blokk) {
          cust.setSalesBlock(blokk);
        }

        if(salesres.get(accountnum) != null){
        	cust.setInfo(Converter.enCodeEst("CHART Sales :|Month and year|sales |" )+ salesres.get(accountnum) );
        }
        String allagents = rs.getString("toall");
        if (allagents.trim().equals("1")) {
          Enumeration enume = getParent().getAgentsHash().elements();
          while (enume.hasMoreElements()) {
            Agent a = (Agent) enume.nextElement();
            cust.addAgent(a);
          }
        }

        Enumeration enume = getParent().getAgentsHash().elements();
        while (enume.hasMoreElements()) {
          Agent a = (Agent) enume.nextElement();
          if(a.isSednAllClients()){
            cust.addAgent(a);
         //   System.out.println("added" + a.getName());
          }
        }


        String payer = rs.getString("payer");
        if (payer != null && payer.trim().length() > 0) {
          String sql1 = "SELECT code FROM sc138  " +
              " WHERE  ltrim(rtrim(id)) = '" + payer.trim() + "'";
          Statement st = con.createStatement();
          ResultSet rs2 = st.executeQuery(sql1);
          while (rs2.next()) {
            cust.setPayer(rs2.getString("code").trim());
          }
          rs2.close();
          st.close();
        }

        
        if(deld.get(cust.getID())!= null) {
        	Toode tt = deld.get(cust.getID());
        	String kk = null;
        	if(tt.getGr1() != null) {
        		kk ="Termo :"+ tt.getGr1();
        	}
        	
        	if(tt.getGr2() != null) {
        		if(kk == null) {
        			kk = tt.getGr2();
        		}else {
        			kk = kk + "|Froze :" + tt.getGr2();
        		}
        		
        	}
        	cust.setCity(kk);
        	
        }
        
        customers.put(name, cust);
        set.put(cust.getID(), cust);
      }

      rs.close();
      pstmt.close();
      con.close();
      
      
      Customer off = new Customer("office");
      off.setName("Ofis Maskavas");
      off.noOrder = true;

      Enumeration enume = getParent().getAgentsHash().elements();
      while (enume.hasMoreElements()) {
        Agent a = (Agent) enume.nextElement();
        off.addAgent(a);
      }
      
      customers.put("Ofis Maskavas", off);
      set.put("office", off);
      
      
      
      
      Customer nou1 = new Customer("nuol1");
      nou1.setName("Noliktava BLS");
      nou1.noOrder = true;
      enume = getParent().getAgentsHash().elements();
      while (enume.hasMoreElements()) {
        Agent a = (Agent) enume.nextElement();
        nou1.addAgent(a);
      }
      customers.put("Noliktava BLS", nou1);
      set.put("nuol1", nou1);
      
      
      Customer nou2 = new Customer("nuol2");
      nou2.setName("Noliktava Lublanas");
      nou2.noOrder = true;
      enume = getParent().getAgentsHash().elements();
      while (enume.hasMoreElements()) {
        Agent a = (Agent) enume.nextElement();
        nou2.addAgent(a);
      }
      customers.put("Noliktava Lublanas", nou2);
      set.put("nuol2", nou2);
      

      String     sql = "SELECT ltrim(a.code) as klid, k.code AS pyid,d.docno AS doknr,sp909 AS maksepaev,sum(sp171) AS debt,date()-sp909 AS yle " + 
    	    	 " 	, date as kuup FROM RG169 alg " +
    	         " left JOIN 1sjourn d ON  ltrim(RIGHT(alg.sp405,7))= rtrim(ltrim(d.iddoc)) left JOIN dh410 info ON info.iddoc=d.iddoc  " +
    	         "   left JOIN  sc46 k ON k.id = sp413  left join SC3724 a on k.id=a.parentext and sp3810 = a.id " +
    	         " WHERE  MONTH(date())=MONTH(alg.period) AND YEAR(date())=YEAR(alg.period)  "+
    	         "   AND k.descr!='' AND (sp171<-0.05 OR sp171>0.05) and sp2671 =(('     1   '))  ORDER BY k.descr,sp909 GROUP BY d.docno " ;
     
  	Connection con2 = DriverManager.getConnection(url2);
      pstmt = con2.prepareStatement(sql);
      if (isDebug()) System.out.println(sql);
      rs = pstmt.executeQuery();

      this.debts = new Vector();
      Calendar today = Calendar.getInstance();
      today.set(Calendar.HOUR_OF_DAY, 1);

      while (rs.next()) {
        Debt debt = new Debt();
        String rece = rs.getString("rece");
        String klid = rs.getString("klid").trim();
        debt.setPayerID(klid);
        if (rece != null && rece.trim().length() > 0) {
          String sql1 = "SELECT code FROM sc138  " +
              " WHERE  ltrim(rtrim(id)) = '" + rece.trim() + "'";
          Statement st = con2.createStatement();
          ResultSet rs2 = st.executeQuery(sql1);
          while (rs2.next()) {
           // debt.setCustID(rs2.getString("code").trim());
          }
          rs2.close();
          st.close();
        }
        if (debt.getCustID() == null) {
          debt.setCustID(klid);
        }

        debt.setDoknr(rs.getString("doknr").trim());
        Date dat = rs.getDate("maksepaev");
        if (dat != null) {
          debt.setDuedate(dat);
        }
        debt.setSum(rs.getDouble("debt"));
        String yle = rs.getString("yle");

        if(yle != null && yle.trim().length()>0){
          debt.setDue(yle);
        }

        Iterator iter = set.values().iterator();
        while (iter.hasNext()) {
          Customer cust = (Customer) iter.next();
          if (cust != null && cust.getPayer().equals(klid)) {
            java.util.Date maksep = debt.getDuedate();
            if (maksep != null && maksep.getTime() < today.getTimeInMillis()) {
              cust.setDebt(cust.getDebt() + debt.getSum());
            }
          }
        }

          debts.addElement(debt);
        }


        rs.close();
        pstmt.close();
        con2.close();
       

    }
    catch (Exception e) {
      getParent().showError(e);
    }
    return customers;
  }

  
  private TreeMap<String,Toode> getPrices() {
	  TreeMap<String,Toode> res = new TreeMap<String,Toode>();
	  
	  File f = new File(this.getExcelDir() + "cenulapa.xlsx");
		try {
			
		
			
			
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheetAt(0);
			XSSFRow row = null;
			for (int i = 12; i <= sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				

				try {
					
					String kood = null ;
					try {
						kood = row.getCell((short) 0).getStringCellValue().trim();
					}catch(Exception e) {
						kood = ((int)row.getCell((short) 0).getNumericCellValue()) +"";
					}
					
					if(kood != null && kood.length() > 3 && kood.length() < 13) {
						 System.out.println(kood);	
						Toode tt = new Toode(kood);
						
						  try {
								String nimi = row.getCell((short) 1).getStringCellValue().trim();
								  tt.setGr1(nimi);
							   }catch(Exception e1) {
								 e1.printStackTrace();
							   }
						
						try {
							double hind = row.getCell((short) 4).getNumericCellValue();
							tt.setDonecur(hind);
						 }catch(Exception e1) {
							e1.printStackTrace();
						 }
						
						
                        try {
                        	double kastis = row.getCell((short) 3).getNumericCellValue();
    						tt.setDonevol(kastis);
						 }catch(Exception e1) {
							 try {
								 double kastis = Double.parseDouble(row.getCell((short) 3).getStringCellValue());
		    						tt.setDonevol(kastis);
							 }catch(Exception k) {
								 
							 }
							
							
						 }
						
                        
			   	     try {
						String lisa = row.getCell((short) 5).getStringCellValue().trim();
						  tt.setBrand(lisa);
					   }catch(Exception e1) {
						// e1.printStackTrace();
					   }
			   	     
			   	   try {
						String uus = row.getCell((short) 6).getStringCellValue().trim();
						
						if(uus != null && uus.length() > 0) {
							  tt.setKogus(1);
											
						}
					   }catch(Exception e1) {
					//	 e1.printStackTrace();
					   }
			   	     
						res.put(kood, tt);	
					}
					
				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	  
		}catch(Exception e) {
			e.printStackTrace();
		}
	  
	  return res;
  }
  
  
  private TreeMap<String,Toode> getDeliveryDates() {
	  TreeMap<String,Toode> res = new TreeMap<String,Toode>();
	  
	  File f = new File(this.getExcelDir() + "Delivery.xlsx");
		try {
			
		
			
			
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheetAt(0);
			XSSFRow row = null;
			for (int i = 12; i <= sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				

				try {
					
					String kood = null ;
					try {
						kood = row.getCell((short) 0).getStringCellValue().trim();
					}catch(Exception e) {
						kood = ((int)row.getCell((short) 0).getNumericCellValue()) +"";
					}
					
					if(kood != null && kood.length() > 2 ) {
						// System.out.println(kood);	
						Toode tt = new Toode(kood);
						 try {
								String jahe = row.getCell((short) 7).getStringCellValue().trim();
								  tt.setGr1(jahe);
							   }catch(Exception e1) {
								   String jahe = ((int)row.getCell((short) 7).getNumericCellValue()) + "";
									  tt.setGr1(jahe);
								   
								
							   }
						
						  try {
								String kylm = row.getCell((short) 8).getStringCellValue().trim();
								  tt.setGr2(kylm);
							   }catch(Exception e1) {
								   try {
								   String jahe = ((int)row.getCell((short) 8).getNumericCellValue()) + "";
									  tt.setGr2(jahe);
								   }catch(Exception e12) {}
								   
							   }
						
						res.put(kood, tt);	
					}
					
				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	  
		}catch(Exception e) {
			e.printStackTrace();
		}
	  
	  return res;
  }
  
  
  public Vector getDebt(String agentCode) throws SQLException {
    return this.debts;
  }

  public TreeMap getProducts(Agent agent) {
    TreeMap<String, Toode> hinnad =  getPrices();
    TreeMap products = new TreeMap();
    Vector active = new Vector();

    String sql = "    SELECT pr.code AS toode, sum(sp1938) AS seis,sp1938 AS kastis  FROM SC157 pr inner JOIN RG1940 alg ON alg.sp1935=pr.id " + 
    		"        WHERE MONTH(date())=MONTH(alg.period) AND YEAR(date())=YEAR(alg.period) order by pr.code group by pr.code";
    try {
    	
    	Connection con = DriverManager.getConnection(url);
    	
    	   
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      Hashtable tooted = new Hashtable();
      while (rs.next()) {
        String id = rs.getString("toode").trim();
        Toode toode = (Toode) tooted.get(id);
      if (toode == null) {
          toode = new Toode(id);
          tooted.put(id, toode);
        }
        toode.setKogus(toode.getKogus() + rs.getDouble("seis"));
        toode.setKastis(1); //rs.getDouble("kastis")
      }
      rs.close();
        stmt.close();
      
        String cmd = "  SELECT '' as ok,pr.id,pr.code AS kood,pr.descr AS nimi,0 AS hind,abi.sp96 AS EAN , yh.descr AS yhik  \r\n" + 
        		"        		 ,parentid AS grupp ,SP94 as kaal, * FROM SC157 pr   left join sc99 abi on rtrim(ltrim(abi.id))=rtrim(ltrim(pr.SP147)) left JOIN SC160 yh ON yh.id=abi.SP93 \r\n" + 
        		"				 where  parentid in (select id from SC157 where ltrim(rtrim(parentid)) = '1' )";

      if (isDebug()) System.out.println(cmd);
      stmt = con.createStatement();

      rs = stmt.executeQuery(cmd);
      while (rs.next()) {
  
        String kood = rs.getString("kood").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimi").trim();
        prod.setName(Converter.enCodeEst(nimetus));
        prod.setPrice(rs.getDouble("hind"));
        String ean = rs.getString("EAN");
        if (ean != null && ean.trim().length() > 0) {
          prod.setEAN(ean.trim());
        }
        String yhik = rs.getString("yhik");
        if (yhik != null && yhik.trim().length() > 0) {
          prod.setUnit(yhik.trim());
        }
        String grupp = rs.getString("grupp");
        if (grupp != null && grupp.trim().length() > 0) {
          prod.setGroup(grupp.trim());
        }
        double kaal = rs.getDouble("kaal");
        if (kaal  > 0) {
          prod.setMeasure(kaal);
        }

        
        boolean goes = false;
        Toode toode = (Toode) tooted.get(kood);
        double kogus = -1;
       
        if (toode != null) {
          kogus = round(toode.getKogus(), 2);
        }

        if(hinnad.get(kood) != null) {
        	goes = true;
        	
        	Toode tt = hinnad.get(kood);
          	prod.setPrice(tt.getDonecur());
        	prod.setInCases(tt.getDonevol());
        	if(tt.getKogus() == 1 ) {
        		prod.setStatusNew("1");
        	}
        	if(tt.getBrand() != null) {
        		prod.setPartii(tt.getBrand());
        	}
        	if(tt.getGr1() != null) {
        		prod.setName(Converter.enCodeEst(tt.getGr1()));
        		nimetus = tt.getGr1();
        	}
        }
        
        
        if(kood.equals("51B228")) {
        	System.out.println("oioi  51B228 " + goes );
        }
        prod.setStock(kogus);
        prod.setVat("21");

        if (goes){
          products.put(nimetus, prod);
          active.addElement(prod.getID());
        } 

      }
      rs.close();
      stmt.close();
      con.close();
    }
    catch (Exception e) {
      getParent().showError(e);
    }
    
	
	/*String  cmd = "select pr.code AS toode,li.code as kood,pr.descr AS nimi,c.value AS hind,li.descr as actnimetus " +
		"	from sc113 pr left join  SC506 hi on hi.parentext=pr.id  LEFT JOIN 1sconst c ON (c.objid) = (hi.id) inner join SC504 li " +
	"		on li.id = sp508  inner join Dh1070 ae on c.docid=ae.iddoc WHERE ltrim(c.id)='E5'  " +
"			and li.descr like '%Extra%'  and date <= date()  order by pr.code,li.descr,date desc ";
	
    StringBuffer buf = new StringBuffer();
	try {
		Statement stmt = con.createStatement();
		ResultSet	      rs = stmt.executeQuery(cmd);
	

		 Vector check = new Vector();
           while(rs.next()){
        	        double hind = rs.getDouble("hind");
                  	String toodex = rs.getString("toode").trim();
                  	String koodx = rs.getString("kood").trim();
                 if(hind>0 && active.contains(toodex)){
                  	if(!check.contains(toodex+koodx)){
                          buf.append(toodex+ ";" + koodx
                          		+ ";" + rs.getString("actnimetus").trim()+ ";" + hind);
                          buf.append("\n");	
                  	}
        	      }
        	    check.add(toodex+koodx);
              }		
	}catch (Exception e ){
		e.printStackTrace();
	}
		
	System.out.println(buf.toString());
	if(buf.length() > 2){
        File f = new File("prodactions.txt");
        if (f.exists())f.delete();
          try {
    		PrintWriter os = new PrintWriter(new FileWriter(
    				 "prodactions.txt", true));
    		os.write(buf.toString());
    		os.close();
    	  }catch (IOException ex) {
    		ex.printStackTrace();
    	  }
     }else{
        File f = new File("prodactions.txt");
        if (f.exists())f.delete();
          try {
    		PrintWriter os = new PrintWriter(new FileWriter(
    				 "prodactions.txt", true));
    		os.write("");
    		os.close();
    	  }catch (IOException ex) {
    		ex.printStackTrace();
    	  }
     }*/
    
    
    return products;
  }

  public String getDistribution() {
    return null;
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
		    	res =res.replaceAll("hhhhh","11154xx");
		    	
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
				res =res.replaceAll("¿","11121xx");
				res =res.replaceAll("Ž","11122xx");
			    res = res.replaceAll("¡","11123xx");
		        res = res.replaceAll("¢","11124xx");
		        res = res.replaceAll("ą","11125xx");
		        res = res.replaceAll("Ą","11126xx");        
		        
		        res = res.replaceAll("Č","11127xx");
		        res = res.replaceAll("¥","11128xx");
		        res = res.replaceAll("Ė","11129xx");	
		        res = res.replaceAll("ė","11130xx");
		        res = res.replaceAll("Ē","11131xx");
		        res = res.replaceAll("§","11132xx");       
		        res = res.replaceAll("Ģ","11133xx");
		        res = res.replaceAll("ģ","11134xx");        
		        res = res.replaceAll("¬","11135xx");
		        res = res.replaceAll("®","11136xx");      
		        res = res.replaceAll("Į","11137xx");
		        res = res.replaceAll("į","11138xx");        
		        res = res.replaceAll("Ķ","11139xx");
		        res = res.replaceAll("ķ","11140xx");
		        res = res.replaceAll("Ļ","11141xx");
		        res = res.replaceAll("ļ","11142xx");
		        res = res.replaceAll("Ņ","11143xx");
		        res = res.replaceAll("³","11144xx");   
		        res = res.replaceAll("Ų","11145xx");
		        res = res.replaceAll("ų","11146xx");      
		        res = res.replaceAll("¼","11147xx");
		        res = res.replaceAll("½","11148xx");
		        res = res.replaceAll("é","11148xx");
		        
			 
		   }


			 	 
			  	return res;
			  }
  
  public Vector getCustomerProductsList(String agentCode) {

    String cmd = " 	SELECT tt.code AS toode,kk.code AS klient,sp616 AS kogus,date AS lastdate,  SUM(iif(sp616>0,sp616,0)) AS Plusskogus," + 
    		"    		SUM(iif(sp616<0,sp616,0)) AS miinuskogus , docno as abi , info.sp620/sp616 as lprice " + 
    		"         FROM dt626 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc INNER JOIN dh626 abi ON abi.iddoc=info.iddoc INNER JOIN SC157 tt ON tt.id=sp615 " + 
    		"         INNER JOIN sc138 kk ON kk.id=sp599  WHERE ltrim(rtrim(kk.sp132)) != '' AND date > date()-120  GROUP BY sp599, tt.descr ORDER BY sp599, tt.descr ";

    if (isDebug()) System.out.println(cmd);
    Vector custProds = new Vector();
    try {
    	Connection con = DriverManager.getConnection(url);
     PreparedStatement pstmt = con.prepareStatement(cmd);
     // pstmt.setString(1, agentCode);

      //Calendar cal = Calendar.getInstance();
      //cal.add(Calendar.MONTH,
           //   -Integer.parseInt(getParent().getProps().getProperty("months")));
    //  pstmt.setDate(1, new java.sql.Date(cal.getTime().getTime()));

      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        String kliendikood = rs.getString("klient").trim();
        String tootekood = rs.getString("toode").trim();
        double kogus = rs.getDouble("kogus");
        double plusskogus = rs.getDouble("plusskogus");
        double miinuskogus = rs.getDouble("miinuskogus");
        String lastDate = rs.getString("lastdate").substring(5, 10);
        String abi  = rs.getString("abi").trim();
        double lprice = rs.getDouble("lprice");

        try{
     //   	kogus = Double.parseDouble(abi.substring(9));
        }catch(Exception e){
        	System.out.println(abi.substring(9));
        	e.printStackTrace();
        }
        CustProd cp = new CustProd(kliendikood, tootekood, kogus, plusskogus,
                                   miinuskogus, lastDate, lprice, 0, 0, " ");
        cp.setAbiKood(abi);
        custProds.add(cp);
      }
      rs.close();
      pstmt.close();
      con.close();
    }
    catch (Exception e) {
      getParent().showError(e);
    }
    return custProds;
  }

  public double getSalary(String agentCode) {
    return 0;
  }

  public DealerProduct getDealerProductsList() {
    return null;
  }

  public String getStartingTime() {
    return null;
  }

  public String getDriverVersion() {
    return "S1Mantinga";
  }

  public String getSubjectFilesTo() {
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent) {
    return null;
  }


  public void setBulkSales(Map agents){

    String cmd = " SELECT sum(info.sp620) AS sales,  ltrim(rtrim(kl.sp132)) AS agent  FROM dt626 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc  " + 
    		"         INNER JOIN dh626 abi ON abi.iddoc=info.iddoc  inner join sc138 kl on rtrim(ltrim(kl.id)) =  rtrim(ltrim(SP599))" + 
    		"         WHERE len(ltrim(pais.ismark))=0 AND YEAR(date)=YEAR(date()) AND MONTH(date) = MONTH(DATE()) " +
    		"         GROUP BY  kl.sp132 ";

       try {
           Calendar cal = Calendar.getInstance();
           cal.set(Calendar.DAY_OF_MONTH, 1);
       	Connection con = DriverManager.getConnection(url);
    	Statement stmt = con.createStatement();
        
           ResultSet	      rs = stmt.executeQuery(cmd);
           while(rs.next()) {
             String agentID = rs.getString("agent").trim();
             Agent agent = (Agent)agents.get(agentID);
             if (agent != null){
               agent.setMonthSales((int)rs.getDouble("sales"));
             }
           }
           rs.close();
           stmt.close();
           con.close();
       } catch (Exception e) {
           getParent().showError(e);
       }

   }


  public TreeMap getDealers() {
    return null;
  }

  private String url = "jdbc:odbc:mantinga";
  private String url2 = "jdbc:odbc:finance";
	
 
  private String subjectFilesTo = null;
  private Vector debts = null;
@Override
public Vector getPriceMatrixData() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Vector getFixedAssortmentData() {
	// TODO Auto-generated method stub
	return null;
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
