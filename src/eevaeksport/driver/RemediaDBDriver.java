
package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
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

public class RemediaDBDriver
    extends AbstractDBDriver implements BulkUpdater,Discounter {
  public RemediaDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url);
      con2 = DriverManager.getConnection(url);
      Properties connInfo = new Properties();
  	  connInfo.put("user", "liides-remedia");
  	  connInfo.put("password", "remedia123");
  	  connInfo.put("charSet", "Cp1257");
     // conlp = DriverManager.getConnection(url2,connInfo);
      stmt = con.createStatement();
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  

  	public static String changeEnc(String s)
  	 {
  	  byte[] bb;
  	  try { bb = s.getBytes("Cp1251"); }
  	  catch( java.io.UnsupportedEncodingException e ) { return s; }
  	  char[] cb = new char[bb.length];
  	  for(int i=0; i < bb.length; i++)
  	    {
  	     cb[i] = (char)( bb[i] & 0x00FF );
  	    }
  	  return new String(cb);
  	 }

  

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
    Statement stmt1 = con2.createStatement();
    String sql = " SELECT id AS liik, descr AS nimetus FROM SC322 WHERE isfolder=1 ORDER BY descr ";
    try {
      ResultSet rs = stmt1.executeQuery(sql);
      while (rs.next()) {
        tm.put(rs.getString("liik").trim(),
              Converter.enCodeEst( rs.getString("nimetus").trim()));
      }
      rs.close();
    }
    catch (Exception e) {
      getParent().showError(e);
    }

    return tm;
  }
  
  
  public TreeMap getProdGroupsSecond() throws SQLException {
    TreeMap tm = new TreeMap();
    Statement stmt1 = con2.createStatement();
    String sql = " SELECT sp2872 AS liik, sp2872 AS nimetus FROM SC322 WHERE isfolder=2 and  ltrim(rtrim(SP2807)) ='2EO' " +
			" group by  sp2872 ORDER BY sp2872 ";
    try {
      ResultSet rs = stmt1.executeQuery(sql);
      while (rs.next()) {
        tm.put(rs.getString("liik").trim(),
               rs.getString("nimetus").trim());
      }
      rs.close();
    }
    catch (Exception e) {
      getParent().showError(e);
    }

    return tm;
  }

 

  public String getSaveStockInfo() {
	  StringBuffer products = new StringBuffer();
		
	  File ff = new File(getParent().getProps().getProperty("stockfileloc"));
	  if(ff.exists()){
		
	  //stockfileloc=C\:/1C_Remedia/agora/download/RemediaLaoseis.csv
		
		try {

			  BufferedReader br = new BufferedReader(new FileReader(getParent().getProps().getProperty("stockfileloc")));
			    String line = null;
			    br.readLine();
			    br.readLine();
			    while((line = br.readLine()) != null){
			      StringTokenizer st = new StringTokenizer(line, ";");
			       if(st.hasMoreTokens()){
                	   String kood = st.nextToken();
                	   if(st.hasMoreTokens()){
                           try{
                        	   double seis = Double.parseDouble(st.nextToken());
                 				if (seis > 0) {
                 					products.append(kood + ";");
                 					products.append(" ;");
                 					products.append(getParent().getNF().format(seis) + "; "
                 							+ "\n"); // + dealer + "\n");
                 					stockslp.put(kood, seis+ "");
                 				}   
                           }catch(Exception u){
                        	   
                           }
              					    
                	   }  
                   }
			  }
			 br.close();
		} catch (Exception e) {
			getParent().showError(e);
		}
		  
	  }
	  System.out.println("Stocks " + products.toString());
		return products.toString();
  }

  /**
   *
   */
  public void connectionDown() {
  }

  public TreeMap getAgents() {
    String cmd = "SELECT id,trim(descr)+' '+ trim(code) as nimi FROM SC2132 order by descr";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
      while (rs.next()) {
        Agent agent = new Agent(rs.getString("id").trim());
        String name = rs.getString("nimi").trim();
        agent.setName(name);
        agents.put(name, agent);
      }
      rs.close();
    }
    catch (Exception e) {
      getParent().showError(e);
    }

    return agents;
  }

 

  public TreeMap getCustomers(Agent agent) {
	  
      File fff = new File(this.getExcelDir() + "/sortimendid.xls" );
      TreeMap fgroups = new TreeMap(); 
  	try{
          FileInputStream ff = new FileInputStream(fff);
  		HSSFWorkbook workbook = new HSSFWorkbook((ff));
  		HSSFSheet sheet = workbook.getSheet("Kauplused");
  		HSSFRow row = null;
  		
  		row = sheet.getRow(0);
  	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
  		
  	//	System.out.println("rownum " + row.getLastCellNum());
  		
  		
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
	    	    			  	// fgroups.put(kood,sort);
  	    			  
  	    				
  				}	   
  				}
 			}catch (Exception e){
  			//	System.out.println(row.getCell((short)a).getNumericCellValue());
  			}
  		}
  		
  		//System.out.println(groups.toString());
  
  	 }catch (Exception e){
  		e.printStackTrace();
  	}

	  
	  
	 String groups = getParent().getProps().getProperty("groups");
	 String groups2 = getParent().getProps().getProperty("groups2");
  	String gsql = "select id from sc69 where isfolder = 1  and (rtrim(ltrim(code)) in (" + groups + ") or " +
  			" rtrim(ltrim(code)) in (" + groups2 + "))";
    
    TreeMap customers = new TreeMap();
    HashMap set = new HashMap();
    Vector vvv = new Vector();
    
  	try {
	    	
       pstmt = con.prepareStatement(gsql);   
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
         vvv.add(rs.getString("id").trim());	
        }
        rs.close();	  
	  
  	String cmd = "   SELECT trim(parentid) as id ,kl.code AS kood,iif (rtrim(ltrim(parentid))='2KO' ,rtrim(kl.descr) + ' REMEDIA' , " +
    	"	kl.descr) AS nimi,rtrim(ltrim(sp72)) AS address,rtrim(ltrim(sp2951)) AS address1, " +
    	"	SP3370 AS visit,sp82 AS ale  ,'' AS blokk ,'' AS toall,'' AS payer,SP3123 AS agent , sp78 AS makse , sp73 AS phone ,sp3774 AS limiit  " +
        "    FROM sc69 kl WHERE ltrim(rtrim(kl.descr)) != ''  and  " +
		"     ltrim(rtrim(SP3123)) != '' ORDER BY kl.descr ASC  ";
  
    if (isDebug()) System.out.println(cmd);
    	
      pstmt = con.prepareStatement(cmd);
       rs = pstmt.executeQuery();
      while (rs.next()) {
   String id = rs.getString("id").trim();
   if(vvv.contains(id)){
	
    	  String name = rs.getString("nimi").trim();
        name = enCode((name).replaceAll("\\r\\n|\\r|\\n", " "));
        String accountnum = rs.getString("kood").trim();
        name = name + " " + accountnum;
        agent = new Agent(rs.getString("agent").trim());
        Customer cust = new Customer(accountnum);
        int makse = rs.getInt("makse");
        if(makse!= 0){
        	cust.setPaymTerm(makse+"");
        }
        String phone = rs.getString("phone");
        if(phone!= null & phone.trim().length()>0){
            cust.setPersonalData("Tel. " +phone.trim());
          }
        
        String limiit = rs.getString("limiit");
        if(limiit!= null & limiit.trim().length()>0){
        	try{
        		int lim = Integer.parseInt(limiit);
        		if(lim > 0){
                    cust.setCredit(lim + "");	
        		}
        		}catch(Exception e){
    			}
        	
          }
        cust.setName(name);
        cust.addAgent(agent);
        
          cust.addAgent(new Agent("2F"));
          cust.addAgent(new Agent("2E"));
       if(agent.getID().equals("27")){
    	   cust.addAgent(new Agent("1U"));
       }
        
       if(agent.getID().equals("1U")){
    	   cust.addAgent(new Agent("27"));
       }
        
        
        String aadress = rs.getString("address").trim();
        if(aadress != null && aadress.trim().length()>0){
        	cust.setAddress(Converter.adressLink(enCode((aadress.trim().replaceAll("\\r\\n|\\r|\\n", " "))),"Estonia"));
        }
        String aadress1 = rs.getString("address1").trim();
        if(aadress1 != null && aadress1.trim().length()>0){
        	cust.setAddress(Converter.adressLink(enCode((aadress1.trim().replaceAll("\\r\\n|\\r|\\n", " "))),"Estonia"));
        }
        
        String gro = (String) fgroups.get(cust.getID());
        if( gro != null && gro.trim().length() > 0){
        	cust.addFixSortCode(gro);
        	System.out.println("Settin fixsorti " + gro);
        }
        
        
        String visit =rs.getString("visit");
        if(visit!= null & visit.trim().length()>0){
          cust.setVisit(visit.trim().replaceAll(" ", ""));
        }
        
        double ale = rs.getDouble("ale");
        if (ale != 0 ) {
        	if(ale <0){
        		ale = ale *-1;
        	}
          cust.setDiscount(ale);
        }
        
        boolean blokk = rs.getBoolean("blokk");
        if (blokk) {
          cust.setSalesBlock(blokk);
        }

        String allagents = rs.getString("toall");
        if (allagents.trim().equals("1")|| cust.getID().equals("3726")) {
          Enumeration enums = getParent().getAgentsHash().elements();
          while (enums.hasMoreElements()) {
            Agent a = (Agent) enums.nextElement();
            cust.addAgent(a);
          }
        }

        Enumeration enums = getParent().getAgentsHash().elements();
        while (enums.hasMoreElements()) {
          Agent a = (Agent) enums.nextElement();
          if(a.isSednAllClients()){
            cust.addAgent(a);
         }
        }

        String payer = rs.getString("payer");
        if (payer != null && payer.trim().length() > 0) {
          String sql1 = "SELECT code FROM sc69  " +
              " WHERE  ltrim(rtrim(id)) = '" + payer.trim() + "'";
          Statement st = con2.createStatement();
          ResultSet rs2 = st.executeQuery(sql1);
          if (rs2.next()) {
           // cust.setPayer(rs2.getString("code").trim());
          }
        
          rs2.close();
          st.close();
        }
        
        cust.setPayer(cust.getID());
        customers.put(name, cust);
        set.put(cust.getID(), cust);
        
   }            
        
      }

      rs.close();
      pstmt.close();
      this.debts = new Vector();

      String sql = "SELECT d.iddoc,sp542,kk.code AS klid,right(rtrim(d.sp3562),9) AS doknr,d.sp3563 AS maksepaev,date()-sp3563 AS yle, " +
		   " sp3588 / iif(year(sp3563-sp3564)=2010,15.6466,1) AS summa,sum(sp3037)AS makstud , sum(sp3720) as makstud2 ,sum(sp1997) as makstud3 ," +
		   " sp3563-sp3564 as kuup FROM  DH3556 d 	" +
           "  INNER JOIN sc69 kk ON ltrim(rtrim(kk.id))=ltrim(right(rtrim(ltrim(sp3558)),4)) " +
	       " left join DT537 ON  ltrim(rtrim(d.iddoc))=ltrim(right(rtrim(ltrim(sp542)),4))" +
		   "	 left join DH3693 ON  ltrim(rtrim(d.iddoc))=ltrim(right(rtrim(ltrim(sp3698)),4))" +
		   "	 left join DH1973 ON  ltrim(rtrim(d.iddoc))=ltrim(right(rtrim(ltrim(sp1978)),4))" +
		   "	   where  sp3563 > date()-200 	" +
	       "   group by d.sp3562,d.iddoc having sum(sp3037) is null or sp3588/ iif(year(sp3563-sp3564)=2010,15.6466,1) -sum(sp3037) >1 order by kk.code, d.sp3562";

      
      
  
      pstmt = con.prepareStatement(sql);
      if (isDebug()) System.out.println(changeEnc(sql));
      rs = pstmt.executeQuery();

      
      Calendar today = Calendar.getInstance();
      today.set(Calendar.HOUR_OF_DAY, 1);

      while (rs.next()) {
        Debt debt = new Debt();
        String klid = rs.getString("klid").trim();
        if (debt.getCustID() == null) {
          debt.setCustID(klid);
        }

        debt.setDoknr(rs.getString("doknr").trim());
        Date dat = rs.getDate("maksepaev");
        if (dat != null) {
          debt.setDuedate(dat);
        }
        double summa = rs.getDouble("summa");
        double makstud = rs.getDouble("makstud");
        double makstud2 = rs.getDouble("makstud2");
        double makstud3 = rs.getDouble("makstud3");
        debt.setSum(summa-makstud-makstud2-makstud3);
        String yle = rs.getString("yle");

        if(yle != null && yle.trim().length()>0){
          debt.setDue(yle);
        }

        Date kuup = rs.getDate("kuup");
        if (kuup != null) {
            debt.setDate(kuup);
          }
        
        
        Iterator iter = set.values().iterator();
        while (iter.hasNext()) {
          Customer cust = (Customer) iter.next();
          if (cust != null && (cust.getPayer().equals(klid) || cust.getID().equals(klid))) {
            java.util.Date maksep = debt.getDuedate();
            
            if (maksep != null && maksep.getTime() < today.getTimeInMillis()) {
            	if(debt.getSum()>0.5 || debt.getSum()<-0.5){
            		cust.setDebt(cust.getDebt() + debt.getSum());	
            	}
               }
            if(debt.getSum()>0.5 || debt.getSum()<-0.5){
          		cust.setUsedCredit(cust.getUsedCredit() + debt.getSum());	
          	  }
          }
         
        }
		if(debt.getSum()>0.5 || debt.getSum()<-0.5){
		    debts.addElement(debt);
		    
		}
        }

      
      String sql2 = "SELECT kk.code AS klid,abi.docno AS doknr,iif(sp2921>date()-1000,sp2921,abi.date) AS maksepaev,  " +
    	"	date()-iif(sp2921>date()-1000,sp2921,abi.date) AS yle,  " +
		"    sp2938/iif(year(sp2921-sp2922)=2010,15.6466,1) AS summa,sum(sp3037)AS makstud  ,sum(sp3720) as makstud2 ,sum(sp1997) as makstud3  " +
		"    FROM  DH2914 d INNER JOIN 1SJOURN abi ON abi.iddoc=d.iddoc AND abi.idjournal like '%29Q%' " +
        " INNER JOIN sc69 kk ON ltrim(rtrim(kk.id))=ltrim(right(rtrim(ltrim(sp2916)),4))  " +
		"    left join DT537 ON  ltrim(rtrim(abi.iddoc))=ltrim(right(rtrim(ltrim(sp542)),4))  " +
	"		left join DH3693 ON  ltrim(rtrim(abi.iddoc))=ltrim(right(rtrim(ltrim(sp3698)),4))" +
	"		 left join DH1973 ON  ltrim(rtrim(abi.iddoc))=ltrim(right(rtrim(ltrim(sp1978)),4))" +
	"	     where   abi.date >date()-200 group by abi.docno  having sum(sp3037) is null or sp2938/iif(year(sp2921-sp2922)=2010,15.6466,1) -sum(sp3037) >1 " +
	"	  order by kk.code, abi.docno";
      
      pstmt = con.prepareStatement(sql2);
      if (isDebug()) System.out.println(changeEnc(sql2));
      rs = pstmt.executeQuery();

      
       today = Calendar.getInstance();
      today.set(Calendar.HOUR_OF_DAY, 1);

      while (rs.next()) {
        Debt debt = new Debt();
        String klid = rs.getString("klid").trim();
        if (debt.getCustID() == null) {
          debt.setCustID(klid);
        }

        debt.setDoknr(rs.getString("doknr").trim());
        Date dat = rs.getDate("maksepaev");
        if (dat != null) {
          debt.setDuedate(dat);
        }
        double summa = rs.getDouble("summa");
        double makstud = rs.getDouble("makstud");
        double makstud2 = rs.getDouble("makstud2");
        double makstud3 = rs.getDouble("makstud3");
        debt.setSum(summa-makstud-makstud2-makstud3);
        String yle = rs.getString("yle");

        if(yle != null && yle.trim().length()>0){
          debt.setDue(yle);
        }

        Iterator iter = set.values().iterator();
        while (iter.hasNext()) {
          Customer cust = (Customer) iter.next();
          if (cust != null && (cust.getPayer().equals(klid) || cust.getID().equals(klid))) {
            java.util.Date maksep = debt.getDuedate();
            if (maksep != null && maksep.getTime() < today.getTimeInMillis()) {
            	if(debt.getSum()>0.5 || debt.getSum()<-0.5){
            		cust.setDebt(cust.getDebt() + debt.getSum());
            		
            	}
               }
          }
        }
		if(debt.getSum()>0.5 || debt.getSum()<-0.5){
		    debts.addElement(debt);
		    
		}
        }

      
      

        rs.close();
        pstmt.close();


    }
    catch (Exception e) {
      getParent().showError(e);
    }
    return customers;
  }

  public Vector getDebt(String agentCode) throws SQLException {
    return this.debts;
  }

  public TreeMap getProducts(Agent agent) {

	 
  	
    TreeMap products = new TreeMap();
    try {
 

    	getSaveStockInfo();
    	
      String cmd = " SELECT pr.parentid,pr.code AS kood,pr.descr AS nimi,SP325 AS hind , y.descr AS yhik  " +
        " ,parentid AS grupp ,SP1877 AS ean , sp2872 AS gr2, SP3371 as kastis ,sp2904 AS reg, sp2872 AS maht " + 
        " ,sp2871 AS alko  FROM SC322 pr INNER JOIN SC85 y ON y.id = pr.sp619 " +
        " WHERE  isfolder=2  AND (parentid in ( " +
	    " SELECT id FROM SC322 WHERE isfolder=1 and  ltrim(rtrim(parentid)) in ('5','PC')) or ltrim(rtrim(parentid)) in ('5','PC') )" +
	    " and (ltrim(rtrim(SP2807)) ='2EO' or  ltrim(rtrim(SP2807)) ='25S' or  ltrim(rtrim(SP2807)) ='2XV' or ltrim(rtrim(SP2807)) ='2EO' ) and " +
	    " pr.descr not like '% EKS'  and pr.descr not like '% PL'  ORDER BY pr.descr"; 
			//and pr.descr not like '% AM'
      if (isDebug()) System.out.println(cmd);
      stmt = con.createStatement();

      ResultSet rs = stmt.executeQuery(cmd);
      while (rs.next()) {
        // if ( kood.length() > 2 ){
      //  int ok = rs.getInt("ok");
      
        String kood = rs.getString("kood").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimi").trim();
       
        if( stockslp.get(kood) != null){
        	prod.setStock(Double.parseDouble((String)stockslp.get(kood)));
        }
        prod.setName(enCode((nimetus)));
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
        
        String grupp2 = rs.getString("gr2");
        if (grupp2 != null && grupp2.trim().length() > 0) {
          prod.setGroupSecond(grupp2.trim());
        }

  
        double kastis = rs.getDouble("kastis");
        double maht = rs.getDouble("maht");
        if(maht > 0 ){
        	prod.setInUnit(maht);
        }
        
        String areto = rs.getString("reg");
        if(areto == null ){
        	areto = "";
        }
        areto = areto.trim();
        

     //   System.out.println(grupinimi);
        
      //  rs1.close();
        
        
        if(kastis > 0 ){
        	prod.setInCases(kastis);	
        }
     
        
      if(prod.getPrice()!= 0){
     	products.put(nimetus, prod);
       }
      
      //lisame toote LP-sse
      /* 	double nkaal = 0;
	double bkaal = 0;
	//double maht = rs.getDouble("maht");
	double alusel = 0;
	//double protsent  = rs.getDouble("alko");
	String pandikood = "" ;
	String maa = "";
	String ekn = "";
	String tootjaKood = "";
	String tootjaNimi = "";
 
   try{
    String ins = " insert into [LPK - Kaup]([Andmevahetuse tunnus], [LPK nr] " +
    "	, Nr, Kirjeldus, Ribakood, [Pohimootuhiku tahis], Netokaal, Brutokaal, Maht, Kastis, Alusel, Aktiivne, [Kanguse protsent] " +
	", [Seotud pandikauba nr], [Paritolumaa tahis], [EKN kood], [Areto nr], [Tootja tahis], [Tootja nimetus] " +
	", [Aktsepteerimise viga], [Aktsepteerimise veateade]" +
	", [KM maar],[Kaubagrupi tahis], [Kaubagrupi nimetus]) VALUES (1,'2680','" + prod.getID()+ "',dbo.Rus2Est('" + enCode((prod.getName().trim())) + "'),'" + prod.getEAN() + "','" + prod.getUnit()+ "',"
	 + nkaal + "," + bkaal + "," + maht + "," + prod.getInCases() + "," + alusel + "," + 1  + "," + protsent + ",'" + pandikood + "','" +
	 maa + "','" + ekn + "','" + areto + "','" + tootjaKood + "',dbo.Rus2Est('" + enCode((tootjaNimi)) + "'), 0, '',20,dbo.Rus2Est('"
	 + prod.getGroup() + "'),dbo.Rus2Est('"+ enCode(grupinimi.trim())	+"'))";
     
      
     // System.out.println(ins);
   	Statement st3 = conlp.createStatement();
    st3.executeUpdate(changeEnc(ins));
   // st3.close();
   }catch (Exception e){
 //  	e.printStackTrace();
   	
    String ins = " update [LPK - Kaup]set [Kaubagrupi tahis] = dbo.Rus2Est('" + prod.getGroup().trim() + "')" +
    		", [Kaubagrupi nimetus]= dbo.Rus2Est('" + enCode(grupinimi.trim()) + "'),[Areto nr] = '" + areto+ "' where  [LPK nr] = '2680' and Nr= '" + prod.getID() + "' " ;
   
      
     // System.out.println(ins);
   	Statement st3 = conlp.createStatement();
    st3.executeUpdate(ins);
    //st3.close();
   
   }*/
      
        
      }
      rs.close();
      //stmt.close();

    
    
    

    
    } catch (Exception e) {
        getParent().showError(e);
      }
    
    return products;
  }
  private String enCode(String res ){
  	 
    res = res.replaceAll("\n", " ");
    String jj = System.getProperties().getProperty("line.separator");
    res = res.replaceAll(jj, " ");
  	res =res.replaceAll("�","11111xx");
  	res =res.replaceAll("�","11112xx");
	res =res.replaceAll("�","11114xx");
	res =res.replaceAll("�","11113xx");

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
  
  


  public String getDistribution() {
    return null;
  }

  public Vector getCustomerProductsList(String agentCode) {
  	Vector custProds = new Vector();

    String cmd = "SELECT tt.code AS toode,kk.code AS klient,r.SP2932 AS kogus,date AS lastdate FROM  DH2914 d " +
	   " INNER JOIN 1SJOURN abi ON abi.iddoc=d.iddoc AND abi.idjournal like '%29Q%' " +
	   " INNER JOIN DT2914 r ON r.iddoc=d.iddoc" +
        " INNER JOIN  SC322 tt ON ltrim(rtrim(tt.id))= ltrim(right(rtrim(ltrim(sp2928)),4)) " +
        " INNER JOIN sc69 kk ON ltrim(rtrim(kk.id))=ltrim(right(rtrim(ltrim(sp2916)),4))	 " +
		"  WHERE   date > ? " +
        "  GROUP BY kk.code, tt.descr ORDER BY kk.code, tt.descr ";
   String cmd2 = "  SELECT tt.code AS toode,kk.code AS klient,r.SP3582 AS kogus,date AS lastdate FROM  DH3556 d  " +
  " INNER JOIN 1SJOURN abi ON abi.iddoc=d.iddoc " +
  " INNER JOIN DT3556 r ON r.iddoc=d.iddoc" +
  " LEFT JOIN  SC322 tt ON ltrim(rtrim(tt.id))= ltrim(right(rtrim(ltrim(SP3578)),4)) " +
  "  LEFT JOIN sc69 kk ON ltrim(rtrim(kk.id))=ltrim(right(rtrim(ltrim(sp3558)),4))	 " +
"	  WHERE   date > ?" +
"     GROUP BY kk.code, tt.descr ORDER BY kk.code, tt.descr" ;
    if (isDebug()) System.out.println(cmd);
    
    try {
    	Connection con = DriverManager.getConnection(url);
     PreparedStatement pstmt = con.prepareStatement(cmd);
     // pstmt.setString(1, agentCode);

      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MONTH,
              -Integer.parseInt(getParent().getProps().getProperty("months")));
      pstmt.setDate(1, new java.sql.Date(cal.getTime().getTime()));

      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        String kliendikood = rs.getString("klient").trim();
        String tootekood = rs.getString("toode").trim();
        double kogus = rs.getDouble("kogus");
     //   double plusskogus = rs.getDouble("plusskogus");
      //  double miinuskogus = rs.getDouble("miinuskogus");
        String lastDate = rs.getString("lastdate").substring(5, 10);

        CustProd cp = new CustProd(kliendikood, tootekood, kogus, 0,0, lastDate, 0, 0, 0, " ");
        custProds.add(cp);
      }
      
      pstmt = con.prepareStatement(cmd2);
      pstmt.setDate(1, new java.sql.Date(cal.getTime().getTime()));
      rs = pstmt.executeQuery();
      while (rs.next()) {
    	  try{
    		  String kliendikood = rs.getString("klient").trim();
    	        String tootekood = rs.getString("toode").trim();
    	        double kogus = rs.getDouble("kogus");
    	     //   double plusskogus = rs.getDouble("plusskogus");
    	      //  double miinuskogus = rs.getDouble("miinuskogus");
    	        String lastDate = rs.getString("lastdate").substring(5, 10);

    	        CustProd cp = new CustProd(kliendikood, tootekood, kogus, 0,0, lastDate, 0, 0, 0, " ");
    	        custProds.add(cp);  
    	  }catch(Exception ee){
    		  
    	  }
       
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
    /*connectionUp();*/
    double sales = 0;
    String cmd = " SELECT (abi.sp269) AS sales,  ltrim(rtrim(sp640)) AS agent  FROM dt272 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc  " +
        " INNER JOIN dh272 abi ON abi.iddoc=info.iddoc INNER JOIN sc113 tt ON tt.id=sp264  " +
        " WHERE len(ltrim(pais.ismark))=0 AND YEAR(date)=YEAR(date()) AND MONTH(date) = MONTH(DATE()) " +
        " GROUP BY  sp640 ";

    try {
      pstmt = con.prepareStatement(cmd);
      pstmt.setString(1, agentCode);
      ResultSet rs = pstmt.executeQuery();

      while (rs.next()) {
        sales = sales + rs.getDouble("sales");
      }
    //  setSales(sales);
    }
    catch (Exception e) {
      getParent().showError(e);
    }

    return 0;
  }

  public DealerProduct getDealerProductsList() {
  
    return null;
  }

  public String getStartingTime() {
    return null;
  }

  public String getDriverVersion() {
    return "Remedia";
  }

  public String getSubjectFilesTo() {
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent) {
    return null;
  }


  public void setBulkSales(Map agents){

    String cmd = " SELECT sum(sp2936) AS sales,  ltrim(rtrim(sp2373)) AS agent  FROM  DH2914 d " +
	   " INNER JOIN 1SJOURN abi ON abi.iddoc=d.iddoc AND abi.idjournal like '%29Q%' " +
        " INNER JOIN sc69 kk ON ltrim(rtrim(kk.id))=ltrim(right(rtrim(ltrim(sp2916)),4)) " +	 
		"  WHERE   YEAR(date)=YEAR(date()) AND MONTH(date) = MONTH(DATE()) GROUP BY sp2373 ";

       try {
           Calendar cal = Calendar.getInstance();
           cal.set(Calendar.DAY_OF_MONTH, 1);

           ResultSet	      rs = stmt.executeQuery(cmd);
           while(rs.next()) {
             String agentID = rs.getString("agent").trim();
             Agent agent = (Agent)agents.get(agentID);
             if (agent != null){
               agent.setMonthSales((int)rs.getDouble("sales"));
             }
           }
           rs.close();
           con.close();
       } catch (Exception e) {
           getParent().showError(e);
       }

   }


  public TreeMap getDealers() {
    return null;
  }

  private String url = "jdbc:odbc:baas";
 // private String url2 = "jdbc:odbc:RemediaLP";
  private Connection con;
  private Connection con2;
 // private Connection conlp;
  private HashMap stockslp = new HashMap();
  private Statement stmt;
  private PreparedStatement pstmt;
  private String subjectFilesTo = null;
  private Vector debts = null;



@Override
public Vector getPriceMatrixData(){

	
 	 String    sql = "  SELECT [Sales Code] AS custid,'' AS clgroup,[Item No_] AS prodid,'' AS prodgroup, " +
       "  [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val from" +
       "   [Tobis$Sales Price] where [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or" +
        "           year ([Ending Date]) = 1753)  and [Sales Type] = 0    " ;

	 

  Vector result = new Vector();
/*    try {
     
	        Statement stmt = con2.createStatement();
	        ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {

        String custID = Converter.enCodeEst(rs.getString("custid").trim());
        String clGroup = rs.getString("clgroup");
        String prodID = rs.getString("prodid").trim();
        String prodGroup = rs.getString("prodgroup");
       
        double price = rs.getDouble("price");
        double disc = rs.getDouble("disc");
        String date = rs.getString("paev");
        String sdate = rs.getString("startday");
        String curr = rs.getString("val");
        MatrixLine mline = new MatrixLine();

        if (custID != null && custID.length() > 0) {
          mline.setCustID(custID);
        }
        
        if (curr != null && curr.length() > 0) {
            mline.setCurrency(curr.trim());
          }
        
        if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
          mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
        }

        if(sdate != null  && !sdate.startsWith("1753")){
       	 mline.setStartDate(sdate.substring(0,10));
        }
        if(date != null && !date.startsWith("1753")){
       	 mline.setEndDate(date.substring(0,10));
        }
        if (prodID != null && prodID.length() > 0) {
          mline.setProdID(prodID);
        }
        if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
          mline.setProdGroup(Converter.cleanLitLatEst(prodGroup.trim()));
          mline.setDiscGroup(Converter.cleanLitLatEst(prodGroup.trim()));
        }

        if (price != 0) {
          mline.setPrice(price);

        }
        if (disc != 0) {
            mline.setDiscount(disc);

          }
        if(mline.getPrice() > 0 || mline.getDiscount() > 0){
            result.add(mline);    
        }
        
      }
      rs.close();
      stmt.close();
      con.close();
    }catch(Exception e){
   	 e.printStackTrace();
    }
*/
return result;
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




private String getDir() {
		String dir = getParent().getProps().getProperty("readin");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}

@Override
public Vector getFixedAssortmentData() {
	Vector result = new Vector();
	File f = new File(this.getExcelDir() + "sortimendid.xls");
	
	try {
		
		
		FileInputStream ff = new FileInputStream(f);
		HSSFWorkbook workbook = new HSSFWorkbook((ff));
		HSSFSheet sheet = workbook.getSheet("Tooted");
		HSSFRow row = null;
		TreeMap groups = new TreeMap();
		row = sheet.getRow(3);
		// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

		// System.out.println("rownum " + row.getLastCellNum());
		for (int a = 2; a < row.getLastCellNum(); a++) {
			try {
				// System.out.println(row.getCell((short)a).getStringCellValue());
				groups.put(a + "", row.getCell((short) a)
						.getStringCellValue());
			} catch (Exception e) {
				// System.out.println(row.getCell((short)a).getNumericCellValue());
			}
		}

		// System.out.println(groups.toString());

		for (int i = 5; i < sheet.getLastRowNum(); i++) {
			row = sheet.getRow(i);
			String prodID = "";

			if (row.getCell((short) 0) != null) {

				try {
					prodID = (int) (row.getCell((short) 0)
							.getNumericCellValue()) + "";
				} catch (Exception e) {
					prodID = (row.getCell((short) 0).getStringCellValue());
				}
				
                   if(!prodID.equals("0")){
                	
				// Siin l�heme reasisse rokkima ja vaatama kas se toode
				// l�heb kaasa
				for (int a = 4; a < row.getLastCellNum(); a++) {

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

						if (go != null && go.equals("1")) {

						//	 System.out.println( a + " " + prodID + " " +
							// clGroup + " " + go);

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
								mline.setProdID(prodID.trim());
							}
							if (mline.getProdID() == null
									&& prodGroup != null
									&& prodGroup.trim().length() > 0) {
								mline.setProdGroup(prodGroup);
							}
				
							result.add(mline);
						}
					}
				}
				   
                   }
			}
		}
	} catch (Exception e) {
		System.out.println("Sortimendi faili pole ");
	}

	// System.exit(0);

	return result;

}

}
