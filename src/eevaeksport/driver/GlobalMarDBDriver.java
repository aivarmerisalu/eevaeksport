
package eevaeksport.driver;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

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

public class GlobalMarDBDriver  extends AbstractDBDriver implements BulkUpdater,Discounter {
  public GlobalMarDBDriver(EksportPanel parent) {
  	
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url);
      stmt = con.createStatement();
    }
    catch (Exception e) {
      parent.showError(e);
    }
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
    nodebt = new Vector();
   String nd = getParent().getProps().getProperty("nodebt.list");
   if(nd != null){
	   StringTokenizer st = new StringTokenizer(nd,",");
	   while(st.hasMoreTokens()){
		   nodebt.add(st.nextToken());
	   }
   }
    
  }

  String user =  null;
  String pass = null;
  String rcompany = null;
  Vector nodebt = null;
  
  
  private Connection getCon() throws SQLException{
	  return  DriverManager.getConnection(url);
  }

  public String getCustProdDiscounts(Agent agent) {
    return null;
  }

  public String getDealerCustomerCSV(String agent) {
    return null;
  }
  public String getSaveDebtInfo() {
   	return null;
   }
   
  public String getAssortmentStr(Agent agent){
  	return null;
  }
  
  
  public void connectionUp() {
  }

  public TreeMap getProdGroups() throws SQLException {
    TreeMap tm = new TreeMap();
    String sql = " SELECT  pr.id as liik, pr.code AS kood, pr.descr AS nimetus  FROM sc33 pr " + 
              "  WHERE  parentid like '%2P%' ORDER BY pr.descr ";
    try {
    	Connection con = DriverManager.getConnection(url);
    Statement	stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
           while (rs.next()) {
        	   String liik = rs.getString("liik").trim();
        	   String code = rs.getString("kood").trim();
        tm.put(liik,
        		Converter.enCodeEst(rs.getString("nimetus").trim()) + " " + code.substring(code.length()-2));
         }
           rs.close();
           stmt.close();
           con.close();
    }
    catch (Exception e) {
      getParent().showError(e);
    }

    return tm;
  }
  
  private String clearCyrr(String in) {
	  in = in.replaceAll("Ь", "Ü");
	  in = in.replaceAll("ь", "ü");
	  in = in.replaceAll("х", "õ");
	  in = in.replaceAll("д", "ä");
	  in = in.replaceAll("Х", "Õ");
	  
	  return in;
  }
  
  private HashMap abigr = null;
  
  public TreeMap getProdGroupsSecond() throws SQLException {
	  abigr = new HashMap();
	    TreeMap tm = new TreeMap();
	    String sql = "select b.code as liik ,b.descr as nimetus from SC5234 a  inner join SC5196 b on a.sp5231=b.id \r\n" + 
	    		"      		 inner join sc33 pr on pr.id =a.parentext  where ltrim(rtrim(b.descr)) like '%PDA%'  group by b.code";
	    try {
	    	Connection con = DriverManager.getConnection(url);
	    Statement	stmt = con.createStatement();
	    
	        ResultSet rs = stmt.executeQuery(sql);
	           while (rs.next()) {
	        	   String liik = rs.getString("liik").trim();
	        	   String nimetus = rs.getString("nimetus").trim();
		               tm.put(liik,Converter.enCodeEst(nimetus));
             }
	           
	           rs.close();
	           stmt.close();
	           con.close();
	    }
	    catch (Exception e) {
	      getParent().showError(e);
	    }

	    return tm;    }

  public String getSaveStockInfo(){
	  
	  String cmd = "select t.code AS toode,sum(sp102) AS kogus from rg99 l INNER JOIN  sc33 t on l.sp101=t.id AND RTRIM(LTRIM(SP3603))='1'  " +
 " LEFT JOIN sc131 ks ON ks.parentext = t.id and sp3748 like'%2%'  where year(period) = year(date()) and month(period) = month(date()) " +
	"	        ORDER BY T.DESCR group by t.code having 0< sum(sp102)";
	 
	         if (isDebug()) System.out.println(cmd);
	         StringBuffer products = new StringBuffer();
	         try {

	           Statement st = con.createStatement();
	           ResultSet rs = st.executeQuery(cmd);

	           while (rs.next()) {
                 String kood = rs.getString("toode").trim();
	             double seis = rs.getDouble("kogus");
	            // String ladu = rs.getString("ladu").trim();
	           
	             if(seis > 0){
	          	     	 products.append(kood +  ";");
	                     products.append(" ;");
	                     products.append(getParent().getNF().format(seis) + "; " +  "\n"); // + dealer +  "\n");
	              }
	             }
	             rs.close();
	         } catch (Exception e) {
	             getParent().showError(e);
	         }
	      
	         return products.toString();
	     }


  /**
   *
   */
  public void connectionDown() {
  }

  public String getPilotDayData(Agent agent){

  	 String sql2 =  "SELECT kl.code AS klient,kl.descr AS nimi,docno AS  arve, sum(info.sp428) AS summa , date() As kuupaev " +
			" FROM dt410 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc " +
          " INNER JOIN dh410 abi ON abi.iddoc=info.iddoc INNER JOIN sc46 kl on kl.id=sp413 INNER JOIN SC5230 b ON b.parentext=kl.id " +
          " WHERE   (sp416) = date()   AND sp5228 like '%" + agent.getID() + "%'  group by docno ORDER BY docno ";
    
      StringBuffer sb = new StringBuffer();
      sb.append("\n\n\nTäna Vormistatud tehingud:\n\n");
        this.getCustomers(null);
        try {

        	Connection con = DriverManager.getConnection(url);
                 Statement st = con.createStatement();
               
                ResultSet rs2 = st.executeQuery(sql2);

                    while (rs2.next()) {
                    sb.append("(" + rs2.getString("arve").trim() + ") [" + rs2.getString("kuupaev").substring(0,10) + "] - " + 
                    		rs2.getString("nimi")+ "  Summa: " + rs2.getDouble("summa") + "\n");
                    }
                    
                    } catch (Exception e) {
                        getParent().showError(e);
                    }
                    if(sb.length()== 0){
                    	return null;
                    }else{
                        return sb.toString();
                                         	
                    }
   
  }

  
  public TreeMap getAgents() {
    String cmd = "select id,code,descr  from SC5200  where parentext =  '    1M   '";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try{
    	Connection con = DriverManager.getConnection(url);
    	   Statement	stmt = con.createStatement();
           ResultSet rs = stmt.executeQuery(cmd);
      while (rs.next()) {
        	String id = rs.getString("id").trim();
        	String code = rs.getString("code").trim();
        	String name = rs.getString("descr").trim() + " " + id ;
        	
            Agent agent = new Agent(id);
            agent.setIsPilot(true);
            agent.setCode(code);
            agent.setName(name);
            
            agent.setLanguage("EST");
            agent.setCountry("ee");
            agent.setPassWord("0000");
            String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
            if(email.length() > 1){
          	  agent.setEmail(email);
          	agent.setActive(true);
            }
           
            
            
           String agentAGENTS = getParent().getProps().getProperty(agent.getID());
            if(agentAGENTS != null){
 	         StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
	  	       while(stag.hasMoreTokens()){
                agent.addDubbler(stag.nextToken());
    	       }
            }
            
            
            agents.put(name , agent);
        }
      rs.close();
      stmt.close();
      con.close();
    }catch(Exception e){
    	e.printStackTrace();
    }
    if(rcompany != null){
    	Converter.sendAgentJson(agents, rcompany, false);
    }
    
   return agents;
  }

  
  
  public void setDealers(TreeMap aDealers) {
    dealers = aDealers;
  }

 private TreeMap getKomments(){
	 TreeMap res = new TreeMap();
		try {
	  File f = new File( "Sortiment/lepingud.csv");
	  System.out.println(f.getAbsolutePath());
	    if (f.exists()){
	    	
		 InputStreamReader is = new FileReader(f);	
	      BufferedReader br = new BufferedReader(is);
	      String line = null;
	      while( ( line = br.readLine()) != null){
	    	  line = line.replaceAll("\",\"", "@");
	    	  line = line.replaceAll("\"", " ").trim();
	      	String id = null;
	      	String name = null;
	        StringTokenizer st = new StringTokenizer(line, "@");
	        if (st.hasMoreTokens()){
	        	id = st.nextToken().trim();
	        	 if (st.hasMoreTokens()){
	              id = id + "_" + st.nextToken().trim();
	               if (st.hasMoreTokens()){
	            	   name = st.nextToken(); 
	               }
	          res.put(id,name);
	        }
	      }
	    }
	      is.close();
	      br.close();
	    }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	// System.out.println("asi  "+ res.toString());
	 return res;
 }
 
 
 
 private TreeMap getKontaktid(){
	 String sql3 = " select trim(kl.code) + '_' + ltrim(a.code) as klient ,rtrim(sp6189) + ' ' + rtrim(kon.descr) + ' ' + rtrim(sp6191) + ' ' + sp6192  as kontakt from SC6223 al" +
		" inner join SC6194 kon on sp6220 = kon.id " +
" INNER JOIN  SC3724 a on a.id=al.parentext INNER JOIN sc46 kl ON kl.id=a.parentext";
	 
	 TreeMap res = new TreeMap();
	 try{
		 Connection con = DriverManager.getConnection(url);
		 Statement stmt = con.createStatement();
         ResultSet  rs = stmt.executeQuery(sql3);
		System.out.println(sql3);
		  while (rs.next()) {
			  String klient = rs.getString("klient").trim();
		      String kontakt = rs.getString("kontakt");
		      if(kontakt != null && kontakt.trim().length()>0){
		      	kontakt = kontakt.replaceAll("\\r\\n|\\r|\\n", " ").trim();
		    	  if(res.get(klient) == null){
		    		  res.put(klient, kontakt);
		    	  }else{
		    		  String asi = (String)res.get(klient);
		    		  res.put(klient,asi + "|" + kontakt);
		    	  }
		      }
		  }
		
		 rs.close();
		 stmt.close();
		 con.close();
		 
	 }catch(Exception e){
		 e.printStackTrace();
	 }
		 
	 System.out.println();
 return res ;	 
		 
 }
 
 

  public TreeMap getCustomers(Agent agent) {

	  TreeMap kommentaarid = getKomments();
	  TreeMap kontaktid = getKontaktid();
  	String cmd = "SELECT  trim(kl.code) + '_' + ltrim(a.code) AS kood ,trim(kl.code) as kood2,rtrim(kl.descr) + ' ' " +
  		"	+ a.sp6354 AS nimi,a.sp6088 AS address ,sp2884 AS paym ,ag.id AS agent , rtrim(sp6189) + ' ' + rtrim(kon.descr) AS kontakt " +
	   " , sp3784 as leping, sp5943 as phone ,sp5214 as email , e.value as limit ,ag2.descr as type  FROM  SC3724 a INNER JOIN sc46 kl ON kl.id=a.parentext " +
	   " INNER JOIN SC6536 agab ON agab.parentext=a.id " +
	   " INNER JOIN SC5200 ag ON ag.id=agab.sp6538 and ag.parentext =  '    1M   '		" +
	 "  INNER JOIN SC6536 agab2 ON agab2.parentext=a.id  " +
	  "  LEFT JOIN SC5200 ag2 ON ag2.id=agab2.sp6538 and ag2.parentext = '    2Z   ' " +
       " LEFT JOIN 1sblob ko ON  "+
      " ko.objid=kl.id AND ko.fieldid =  ' 2X5 '   left join SC6194 kon ON kon.id=sp6087 " +
    " left join 1sconst c ON c.objid=kl.id and trim(c.id)like '%P3%' " +
	 " left join 1sconst e ON e.objid=kl.id and trim(e.id)like '%29T%' "+
      "  WHERE kl.isfolder=2  and (ltrim(rtrim(kl.parentid)) ='1' or ltrim(rtrim(kl.parentid)) ='5') "+
       " and ltrim(rtrim(parentid))='1'  ORDER BY kl.descr , e.date  " ;
  	
  	

    if (isDebug()) System.out.println(cmd);

    TreeMap customers = new TreeMap();
    HashMap set = new HashMap();
    HashMap setused = new HashMap();
    try {
    	
    	  String     sql = "SELECT ltrim(a.code) as klid, k.code AS pyid,d.docno AS doknr,sp909 AS maksepaev,sum(sp171) AS debt,date()-sp909 AS yle " + 
    	 " 	, date as kuup FROM RG169 alg " +
         " left JOIN 1sjourn d ON  ltrim(RIGHT(alg.sp405,7))= rtrim(ltrim(d.iddoc)) left JOIN dh410 info ON info.iddoc=d.iddoc  " +
         "   left JOIN  sc46 k ON k.id = sp413  left join SC3724 a on k.id=a.parentext and sp3810 = a.id " +
         " WHERE  MONTH(date())=MONTH(alg.period) AND YEAR(date())=YEAR(alg.period)  "+
         "   AND k.descr!='' AND (sp171<-0.05 OR sp171>0.05) and sp2671 =(('     1   '))  ORDER BY k.descr,sp909 GROUP BY d.docno " ;
    	
         Connection con = DriverManager.getConnection(url);
    	     stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(sql);

          if (isDebug()) System.out.println(sql);

          this.debts = new Vector();
          Calendar today = Calendar.getInstance();
          today.set(Calendar.HOUR_OF_DAY, 1);

          while (rs.next()) {
            Debt debt = new Debt();
            String kl =rs.getString("klid");
            String py =rs.getString("pyid").trim();
            
            debt.setPayerID(py);
            debt.setCustID(py);
            if(kl != null){
            	   debt.setCustID(py + "_" +kl.trim());   	
            }
         
            debt.setDoknr(rs.getString("doknr").trim());
            debt.setDuedate(rs.getDate("maksepaev"));
            debt.setSum(rs.getDouble("debt"));
            debt.setDue(rs.getString("yle"));
            debt.setDate(rs.getDate("kuup"));
              java.util.Date maksep = debt.getDuedate();             
              if (maksep.getTime() < today.getTimeInMillis()) {
            	  String paYer = (String)set.get(debt.getPayerID());
            	  if(paYer == null ){
            		  set.put(debt.getPayerID(), debt.getSum() + "");   	  
            	  }else{
            		  set.put(debt.getPayerID(),(Double.parseDouble(paYer) +  debt.getSum()) + ""); 
            	  }
              }
              
              String paYer = (String)setused.get(debt.getPayerID());
        	  if(paYer == null ){
        		  setused.put(debt.getPayerID(), debt.getSum() + "");   	  
        	  }else{
        		  setused.put(debt.getPayerID(),(Double.parseDouble(paYer) +  debt.getSum()) + ""); 
        	  }

            debts.addElement(debt);
            
          }
          rs.close();
    	
    

            String sortsql = "SELECT  trim(kl.code) + '_' + ltrim(a.code) AS kood  , sp3784 as leping ,ag.id as sort "
            		+ "FROM  SC3724 a INNER JOIN sc46 kl ON kl.id=a.parentext " +
	   " INNER JOIN SC6536 agab ON agab.parentext=a.id " +
	   " INNER JOIN SC5200 ag ON ag.id=agab.sp6538 and ag.parentext like '%S%'	" +
       " WHERE kl.isfolder=2  and (ltrim(rtrim(kl.parentid)) ='1' or ltrim(rtrim(kl.parentid)) ='5') " +
       " and ltrim(rtrim(parentid))='1'  ORDER BY kl.descr";
            
        TreeMap sorts = new TreeMap();
        rs = stmt.executeQuery(sortsql);
            while (rs.next()) {
                String accountnum = rs.getString("kood").trim();
                String sort = rs.getString("sort").trim();
                sorts.put(accountnum, sort);
            }
                
        
                
                

        	  	stmt = con.createStatement();
                rs = stmt.executeQuery(cmd);
            while (rs.next()) {
        String name = rs.getString("nimi").trim();
        
        String accountnum = rs.getString("kood").trim();
        String payer = rs.getString("kood2").trim();
        Customer cust = new Customer(accountnum);
        cust.setPayer(payer);
        cust.restName = name;
        name = clearCyrr(name) +  " " +accountnum;
        cust.setName(Converter.enCodeEst((name)));
     
        String agen = rs.getString("agent").trim();
        cust.addAgent(new Agent(agen));
        if(!agen.equals("4JR")){
        	cust.addAgent(new Agent("4JR"));
        	cust.addAgent(new Agent("60B"));
        	cust.addAgent(new Agent("5TV"));
        	cust.addAgent(new Agent("4JT"));
        }
         
       
if(cust.getID().equals("0000290_1"))
{
	cust.addAgent(new Agent("4JR"));
	cust.addAgent(new Agent("60B"));
	cust.addAgent(new Agent("5TV"));
	cust.addAgent(new Agent("4JT"));
	cust.addAgent(new Agent("79E"));
	cust.addAgent(new Agent("4JQ"));
	cust.addAgent(new Agent("7O3"));
	cust.addAgent(new Agent("80G"));
	cust.addAgent(new Agent("80F"));
	

}
	
        
        String agentAGENTS = getParent().getProps().getProperty(agen);

        if(agentAGENTS != null){
	         StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
  	       while(stag.hasMoreTokens()){
            cust.addAgent(new Agent(stag.nextToken()));
	       }
        }
     
     
        
         String deb =(String) set.get(payer);
         if(deb != null ){
        	 cust.setDebt(Double.parseDouble(deb));
         }
         

         String used =(String) setused.get(payer);
         if(used != null ){
        	 cust.setUsedCredit(Double.parseDouble(used));
         }
         
         String aadr = clearCyrr(rs.getString("address").trim());
        cust.setAddress(Converter.adressLink(Converter.enCodeEst((aadr)),"Estonia"));
        if(aadr.indexOf(",") != -1) {
        	
        	cust.restStreet = aadr.substring(0,aadr.indexOf(","));
            aadr = aadr.substring(aadr.indexOf(",")+1);
            
            if(aadr.indexOf(",") != -1) {
        		cust.restCity = aadr.substring(aadr.lastIndexOf(",")+1);
            	aadr = aadr.substring(0, aadr.indexOf(","));
           }
            if(aadr.length() > 0) {
            	cust.restZipCode = aadr;
            }
        	
        }
        System.out.println("aadr "+ cust.getID() + " " + cust.restStreet + "|"+ cust.restCity + "|"+ cust.restZipCode);
        
        if(kommentaarid.get(accountnum) != null){
        	String inff = (String)kommentaarid.get(accountnum);
        	if(inff.length() > 300){
        		inff = inff.substring(0,300);
        	}
        	cust.setInfo(inff);
        }
        
        if(kontaktid.get(accountnum) != null){
        	String inff = (String)kontaktid.get(accountnum);
        	if(inff.length() > 200){
        		inff = inff.substring(0,200);
        	}
        	//cust.setPersonalData(inff);
        }
        
    //    cust.setVisit(rs.getString("visit").trim());
       String days = rs.getString("paym");
        if(days != null && days.trim().length()>0){
        	cust.setPaymTerm(days.trim().substring(0,days.trim().length()-1));        	
        }
        
        
        
        String leping = rs.getString("leping");
        if(leping != null && leping.trim().length() > 0){
        	cust.setGroup(leping.trim());
        	
        }
        
        if(sorts.get(cust.getID()) != null){
        	cust.addFixSortCode(sorts.get(cust.getID())+"");
        }
        
        String info = rs.getString("phone");
        if (info != null && info.trim().length()> 2) {
           if (info.trim().endsWith(",")){
        	   info = info.substring(0,info.length()-1);
           }
           if (info.trim().startsWith(",")){
        	   info = info.substring(1);
           }
        	cust.setPersonalData("Tel:"+ info.trim());
        }
        
        

        String confmail = rs.getString("email");
        if (confmail != null && confmail.trim().length()>0) {
        	if(cust.getPersonalData() != null && cust.getPersonalData().trim().length() > 0 ){
        		 cust.setPersonalData(cust.getPersonalData()+"|" + confmail.trim());
        	}else{
        		 cust.setPersonalData(confmail.trim());
        	}
        }
        
        String limit  = rs.getString("limit").trim();
        try{
        	double dd = Double.parseDouble(limit);
        	if(dd > 0){
        		cust.setCredit(dd +"");
        	}
        }catch(Exception e){
        	e.printStackTrace();
        }
      
        String typ  = rs.getString("type");
  if(typ != null) {
	  typ = typ.trim();
	  cust.category = typ;
  }

        
        
        customers.put(name, cust);
      }

      rs.close();
      stmt.close();
      con.close();


    }
    catch (Exception e) {
      getParent().showError(e);
    }
    
    
    
    if(rcompany != null){
     //	Converter.sendCustomerJson(customers, rcompany,false,user,pass);
     }
    
    return customers;
  }

  public Vector getDebt(String agentCode) throws SQLException {
    return this.debts;
  }

  public TreeMap getProducts(Agent agent) {

    TreeMap products = new TreeMap();
    
   
    String sql = " select t.code AS toode,t.descr, ks.sp134 AS kastis,(sp102) AS kogus from rg99 l INNER JOIN  sc33 t on l.sp101=t.id AND RTRIM(LTRIM(SP3603))='1' " +
	      " LEFT JOIN sc131 ks ON ks.parentext = t.id and sp3748 like'%2%'  where year(period) = year(date()) and month(period) = month(date())" +
	      "  ORDER BY T.DESCR  "; //and RTRIM(LTRIM(sp100))='2'
  
    try {
    	 getProdGroupsSecond();

    	Connection con = DriverManager.getConnection(url);
    Statement  stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      Hashtable tooted = new Hashtable();
      while (rs.next()) {
        String id = rs.getString("toode").trim();
        Toode toode = (Toode) tooted.get(id);
        if (toode == null) {
          toode = new Toode(id);
          tooted.put(id, toode);
        }
        toode.setKogus(toode.getKogus() + rs.getDouble("kogus"));
        toode.setKastis(rs.getDouble("kastis"));
     
      }
      rs.close();
      
      
      sql = "select sp1131 as toode , sum(sp2655) / sum(sp1133) as omah  from RG1130 s " +
 " inner join sc33 t on t.id = s.sp1131 where sp2655 > 0 and year(period) = year(date()) and month(period) = month(date()) " +
 " group by sp1131 having sum(sp1133)> 0 ";
      
    rs = stmt.executeQuery(sql);
   Hashtable ohd = new Hashtable();
   while (rs.next()) {
     String id = rs.getString("toode").trim();
     double oh = rs.getDouble("omah");
     ohd.put(id, oh);
   }
   rs.close();
 
 
      //oli aktsiisi lao seisude oma..
      String sql2 = " select t.code AS toode,t.descr, '' AS kastis,(sp102) AS kogus from rg99 l INNER JOIN  sc33 t on l.sp101=t.id AND RTRIM(LTRIM(SP3603))='1' " +
      " where year(period) = year(date()) and month(period) = month(date()) and RTRIM(LTRIM(sp100))='1' ORDER BY T.DESCR "; //and rtrim(sp3603)like '%0' and sp100 like '%1'
      
      stmt = con.createStatement();
      ResultSet rs2 = stmt.executeQuery(sql2);
      Hashtable tooted2 = new Hashtable();
      while (rs2.next()) {
        String id = rs2.getString("toode").trim();
        Toode toode = (Toode) tooted2.get(id);
        if (toode == null) {
          toode = new Toode(id);
          tooted2.put(id, toode);
        }
        toode.setKogus(toode.getKogus() + rs2.getDouble("kogus"));
      }
      rs2.close();
      HashMap gr22 = new HashMap();
      
      String sss = "select pr.id as toode , b.code as gr  from SC5234 a  inner join SC5196 b on a.sp5231=b.id "
      		+ " inner join sc33 pr on pr.id =a.parentext  where ltrim(rtrim(b.descr)) like '%PDA%'  group by pr.code";
      stmt = con.createStatement();
       rs2 = stmt.executeQuery(sss);
      HashSet lubatud  = new HashSet();
      while (rs2.next()) {
    	  String kood = rs2.getString("toode").trim();
    	  String gr = rs2.getString("gr").trim();
    	  gr22.put(kood, gr);
    	  if(!lubatud.contains(kood)){
    		  lubatud.add(kood);
    	  }
      }
      rs2.close();
      
      
  		  sss = "select hi.parentext as id, c.value as hind from SC3772"
  		  		+ "  hi inner join  1sconst c ON (c.objid) = (hi.id)"
  		  		+ " where ltrim(rtrim(SP3787)) like '%48%' " + 
  			" ORDER BY hi.parentext,date " ;
      stmt = con.createStatement();
  	       rs2 = stmt.executeQuery(sss);
  	      HashMap minhind  = new HashMap();
  	      while (rs2.next()) {
  	    	  String kood = rs2.getString("id").trim();
  	    	  String hind = rs2.getString("hind").trim();
  	    	minhind.put(kood, hind);
  	      }
  	      rs2.close();
  	      
  	      
  	    sss = "select hi.parentext as id, c.value as hind from SC3772"
  		  		+ "  hi inner join  1sconst c ON (c.objid) = (hi.id)"
  		  		+ " where ltrim(rtrim(SP3787)) like '%47%' " + 
  			" ORDER BY hi.parentext,date " ;
      stmt = con.createStatement();
  	       rs2 = stmt.executeQuery(sss);
  	      HashMap soodhind  = new HashMap();
  	      while (rs2.next()) {
  	    	  String kood = rs2.getString("id").trim();
  	    	  String hind = rs2.getString("hind").trim();
  	    	soodhind.put(kood, hind);
  	      }
  	      rs2.close();
  			
  			
  			
      String cmd = " SELECT  c.id,(date),pr.id,pr.code AS kood,pr.descr AS nimi ,nim.descr as nimi2,c.value AS hind,sp3559 AS EAN , yh.descr AS yhik "
      		+ " ,pr.parentid AS grupp ,sp3752 AS abikood    FROM sc33 pr LEFT JOIN SC5973 nim ON nim.parentext=pr.id and  trim(sp5974) like '%2%'  " + 
      		"       LEFT JOIN sc131 yh ON yh.id=sp3753 LEFT JOIN  SC3772 hi on hi.parentext=pr.id and (ltrim(rtrim(hi.sp3787))='3' ) LEFT JOIN " + 
      		"       1sconst c ON (c.objid) = (hi.id) " + 
      		"        WHERE isfolder=2 AND ltrim(c.value)!='0.000' AND trim(c.id) like '%2WV%' " + 
      		"        ORDER BY pr.descr,date";

      if (isDebug()) System.out.println(cmd);
      stmt = con.createStatement();

      rs = stmt.executeQuery(cmd);
      while (rs.next()) {
    	  String idd = rs.getString("id").trim();
    	  boolean goes = false;
    	  if(lubatud.contains(idd)){
    		  goes = true;
    	  }
        String kood = rs.getString("kood").trim();
        Product prod = new Product(kood);
        
        
        
        
        String nimetus = rs.getString("nimi").trim();

        String nimetus2 = rs.getString("nimi2");
        if(nimetus2 != null && nimetus2.trim().length() > 0) {
        	nimetus = nimetus2.trim();
        }
       
        double price = rs.getDouble("hind"); 
        if(price == 0 ){
          	price = 1;
        }
     	 prod.setPrice(price);
        String ean = rs.getString("EAN");
        if (ean != null && ean.trim().length() > 0) {
        	if(ean.startsWith("-")){
        		ean = ean.substring(1);
        	}
          prod.setEAN(ean.trim());
        }
        String yhi = rs.getString("yhik");
        if(yhi != null && yhi.trim().length()>0){
        	prod.setUnit(yhi.trim());
        }else{
        	yhi = "";
        }
        prod.setName(Converter.enCodeEst(nimetus) + " " + yhi.trim() + " " + kood.trim());
        
        String grupp = rs.getString("grupp");
        if (grupp != null && grupp.trim().length() > 0) {
        	String gr = grupp.trim();
        	
        	if(abigr.get(gr) != null && !((String)abigr.get(gr)).trim().equals("2P")){
        		String gr2 = (String)abigr.get(gr);
        		 if(abigr.get(gr2) != null && !((String)abigr.get(gr2)).trim().equals("2P")){
        			prod.setGroup((abigr.get(gr2) + "").trim());
                  }else{
        			prod.setGroup(gr2.trim());
                  }
        		// prod.setGroupSecond(gr);
        	}else{
        		prod.setGroup(gr);
        	}
            
        	
         }
        
        

    	if(gr22.get(idd) != null) {
    		 prod.setGroupSecond(gr22.get(idd) +"");
    	}
        
    	if(soodhind.get(idd) != null){
        	try{
             
              		prod.setPartii("Sood. hind: " + soodhind.get(idd));
              	
            }catch(Exception e){
        	// e.printStackTrace();	
        	}
        }
    	
        if(minhind.get(idd) != null){
        	try{
              	prod.setMinPrice(Double.parseDouble(minhind.get(idd)+""));
              	
              	if(prod.getPartii() != null && prod.getPartii().trim().length() > 1) {
              		prod.setPartii( prod.getPartii() + "|Min. hind: " + minhind.get(idd));
              	 } else {
              		prod.setPartii("Min. hind: " + minhind.get(idd));
              	}
              	
            }catch(Exception e){
        	// e.printStackTrace();	
        	}
        }
        
        if(ohd.get(idd) != null && (prod.getGroup().equals("789")
        		|| prod.getGroupSecond().equals("789"))){
        	 
             	try{
                //   	prod.setMinPrice(Double.parseDouble(ohd.get(idd)+"")*1.1);
                 }catch(Exception e){
             	
                 }
             
        }
        
        if(ohd.get(idd) != null && (prod.getGroup().equals("W") || prod.getGroupSecond().equals("W") ||
        		prod.getGroup().equals("4DZ") || prod.getGroupSecond().equals("4DZ") ||
        		prod.getGroup().equals("1XK") || prod.getGroupSecond().equals("1XK") ||
        		prod.getGroup().equals("20M") || prod.getGroupSecond().equals("20M"))){
       	 /*W;Fresh fruits and vegetables 32;2
4DZ;Fresh fruits and vegetables 90;2
1XK;Fresh vegetables, fruits 04;2
20M;Fresh vegetables, fruits 14;2*/
         	try{
              // 	prod.setMinPrice(Double.parseDouble(ohd.get(idd)+"")*1.2);
             }catch(Exception e){
         	}
         
    }
        
        
        
        Toode toode = (Toode) tooted2.get(kood);
        double kogus;
        if (toode == null) {
          kogus = 0;
        }
        else {
          kogus = round(toode.getKogus(), 2);
        }
        if(kogus > 0){
        	prod.setName(prod.getName() + "|" + kogus + "|"); //aktsiisilao seisu lisamine
        }
        
        
         toode = (Toode) tooted.get(kood);
     
        double kastis;
        if (toode == null) {
          //System.out.println(kood);
          kogus = 0;
          kastis = 0;
        }
        else {
          kogus = round(toode.getKogus(), 2);
          kastis = round(toode.getKastis(), 2);
        }

         prod.setStock(kogus);
         prod.setInCases(kastis);
     if(goes){
    	    products.put(nimetus, prod); 
     }
      }
      
      rs.close();
      stmt.close();
      con.close();
    }
    catch (Exception e) {
      getParent().showError(e);
    }
    return products;
  }

  public String getDistribution() {
    return null;
  }

  public Vector getCustomerProductsList(String agentCode) {

	    String cmd = "SELECT tt.code AS toode,kk.code AS klient,a.code AS kood,info.sp424 AS kogus,sp416 AS lastdate, " +
	       " SUM(iif(info.sp424>0,info.sp424,0)) AS Plusskogus,SUM(iif(info.sp424<0,info.sp424,0)) AS miinuskogus,sp426 as lprice " +
	       "  FROM dt410 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc " +
	       "  INNER JOIN dh410 abi ON abi.iddoc=info.iddoc INNER JOIN sc33 tt ON tt.id=SP423 " +
		   "	 INNER JOIN SC3724 a ON  a.id=sp3802 "+
	       "    INNER JOIN sc46 kk ON kk.id=sp413  WHERE  sp416 > ?  GROUP BY kk.code,sp3802, tt.code ORDER BY kk.code,sp3802, tt.code" ;
	    if (isDebug()) System.out.println(cmd);
	    Vector custProds = new Vector();
	    try {
	    	
	    	Connection cc = getCon();
	     PreparedStatement pstmt = cc.prepareStatement(cmd);

	      Calendar cal = Calendar.getInstance();
	      cal.add(Calendar.MONTH,
	              -Integer.parseInt(getParent().getProps().getProperty("months")));
	      pstmt.setDate(1, new java.sql.Date(cal.getTime().getTime()));

	      ResultSet rs = pstmt.executeQuery();
	      while (rs.next()) {
	        String kliendikood = rs.getString("klient").trim()+"_"+rs.getString("kood").trim();
	        String tootekood = rs.getString("toode").trim();
	        double kogus = rs.getDouble("kogus");
	        double plusskogus = rs.getDouble("plusskogus");
	        double miinuskogus = rs.getDouble("miinuskogus");
	        String lastDate = rs.getString("lastdate");
	        double lprice = rs.getDouble("lprice");

	        CustProd cp = new CustProd(kliendikood, tootekood, kogus, plusskogus,
	                                   miinuskogus, lastDate, lprice, 0, 0, " ");
	        custProds.add(cp);
	      }
	      rs.close();
	      pstmt.close();
	      cc.close();
	    }
	    catch (Exception e) {
	      getParent().showError(e);
	    }
	    return custProds;
	  }


  public double getSalary(String agentCode) {
    /*connectionUp();*/
    double sales = 0;
    String cmd = "SELECT SUM(info.sp428) AS sales,sp5228 AS agent  FROM dt410 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc " +
       "  INNER JOIN dh410 abi ON abi.iddoc=info.iddoc INNER JOIN sc46 kl on kl.id=sp413 INNER JOIN SC5230 b ON b.parentext=kl.id " +
       "  WHERE  YEAR(sp416)=YEAR(date()) AND MONTH(sp416) = MONTH(DATE()) group by sp5228 ";

    try {
      pstmt = con.prepareStatement(cmd);
      pstmt.setString(1, agentCode);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        sales = rs.getDouble("sales");
        setSales(sales);
      }
    }
    catch (Exception e) {
      getParent().showError(e);
    }

    return 0;
  }
 
  public void setBulkSales(Map agents){

 
    
    
   String  cmd =   "  SELECT sum(info.sp428) AS summa, ag.id as agent   FROM dt410 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc  " + 
    		"    		     INNER JOIN dh410 abi ON abi.iddoc=info.iddoc INNER JOIN sc46 kl on kl.id=sp413 " + 
    		"    			 INNER join SC3724 a  ON kl.id=a.parentext and ltrim(rtrim(a.code)) = '1' " + 
    		"    			  INNER JOIN SC6536 agab ON agab.parentext=a.id " + 
    		"    		   INNER JOIN SC5200 ag ON ag.id=agab.sp6538 and ag.parentext =  '    1M   '		" + 
    		"    		      WHERE  YEAR(sp416)=YEAR(date()) AND MONTH(sp416) = MONTH(DATE()) " + 
    		"    			   group by ag.id " + 
    		" union " + 
    		" select sum(SP453)*-1 AS summa, ag.id as agent   from dh434  INNER JOIN sc46 kl on kl.id=SP437" + 
    		" INNER join SC3724 a  ON kl.id=a.parentext and ltrim(rtrim(a.code)) = '1'" + 
    		" INNER JOIN SC6536 agab ON agab.parentext=a.id  " + 
    		" INNER JOIN SC5200 ag ON ag.id=agab.sp6538 and ag.parentext =  '    1M   ' " + 
    		" where ltrim(rtrim(sp446)) = '3J' and YEAR(SP440)=YEAR(date()) AND MONTH(SP440) = MONTH(DATE()) " + 
    		" group by ag.id  ";
    
    
    
       if (isDebug()) System.out.println(cmd);
          try {
        	  Connection con = DriverManager.getConnection(url);
              Statement stmt = con.createStatement();
              ResultSet	      rs = stmt.executeQuery(cmd);
              while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                   agent.setMonthSales((int)rs.getDouble("summa"));
                }
              }
              rs.close();
              stmt.close();
              con.close();
          } catch (Exception e) {
              getParent().showError(e);
           }
        }


  
  public DealerProduct getDealerProductsList() {

  /*  try {

      String sql = " SELECT t.code AS toode,SUM(sp411) AS STOCK , l.code AS LADU from rg405 s INNER JOIN sc55 l ON l.id=s.sp418 " +
          " inner join  sc84 t on sp408=t.id WHERE MONTH(period)=MONTH(date()) AND YEAR(period)=YEAR(date()) GROUP BY t.code,l.code ORDER BY l.code ";

      if (isDebug()) System.out.println(sql);

      Statement stm = con.createStatement();
      ResultSet rst = stm.executeQuery(sql);

      while (rst.next()) {
        String toode = rst.getString("toode").trim();
        int seis = (int) Double.parseDouble(rst.getString("stock").replace(',',
            '.'));
        String ladu = rst.getString("ladu").trim();

        dealerProds.add(ladu, toode, seis + "", " ", " ");

        //System.out.println(ladu+" "+toode+" "+seis+"");
      }
      rst.close();

    }
    catch (Exception e) {
      getParent().showError(e);
    }

    return dealerProds;
*/
    return null;
    }

  public String getStartingTime() {
    return null;
  }

  public String getDriverVersion() {
    return "GlobalMar";
  }

  public String getSubjectFilesTo() {
    return this.subjectFilesTo;
  }


  public TreeMap getDealers() {
    return dealers;
  }

  private String url = "jdbc:odbc:globalmar";
  private Connection con;
  private Statement stmt;
  private PreparedStatement pstmt;
  private String subjectFilesTo = null;
  private Vector debts = null;
  private TreeMap dealers = new TreeMap();

	  public Vector getFixedAssortmentData() {
	        Vector lines = new Vector();
	       
String cmd = " select pr.code as toode, SP5232 as sortcode  from SC5234 a  inner join SC5196 b on a.sp5231=b.id "
		+ " inner join sc33 pr on pr.id =a.parentext  where ltrim(rtrim(SP5231)) ='S'  order by SP5232,pr.code";

//alternatiivne sortiment 
//select pr.code as toode, b.id as sortcode  from SC5234 a  inner join SC5196 b on a.sp5231=b.id inner join sc33 pr 
// on pr.id =a.parentext  where b.descr like '%PDA%' group by a.parentext

	        	 try {
	        	HashSet olemas = new HashSet();	 
	        		Connection con = DriverManager.getConnection(url);
	                Statement  stmt = con.createStatement();
	                 ResultSet	      rs = stmt.executeQuery(cmd);
	                 while(rs.next()) {
	             
                       String clGroup = " ";
                       String prodID = " ";
                       MatrixLine mline = new MatrixLine();
                       
                       prodID = rs.getString("toode").trim();
                       clGroup =  rs.getString("sortcode").trim();
                    
                       if (clGroup != null
                           && clGroup.trim().length() > 0) {
                         mline.setCliGroup(clGroup.trim());
                       }

                       if (prodID != null && prodID.trim().length() > 0) {
                         mline.setProdID(prodID.trim());
                       }
                                          
                        if(!olemas.contains(prodID+ "-" + clGroup)) {
                        	lines.add(mline);
                        }
                        
                       olemas.add(prodID+ "-" + clGroup);
	                   
	                 }
	                 rs.close();
	                 stmt.close();
	                 con.close();
	             } catch (Exception e) {
	                 getParent().showError(e);
	              }
	           
	        	            
	            
	        return lines;
	}


public Vector getLastThreeData() {
	// TODO Auto-generated method stub
	return null;
}


 public Vector getPriceMatrixData() {
        Vector lines = new Vector();
       String sql = " SELECT   pr.code AS prodid,sp3787 as custgr ,c.value AS price  FROM sc33 pr  " +  
      " LEFT JOIN sc131 yh ON yh.id=sp3757 LEFT JOIN  SC3772 hi on hi.parentext=pr.id  LEFT JOIN " +
      " 1sconst c ON (c.objid) = (hi.id)  WHERE isfolder=2 AND ltrim(c.value)!='0.000' AND trim(c.id) like '%2WV%' " +
      "	ORDER BY pr.descr,date";
   TreeMap vahe = new TreeMap();
        try {

        	Connection con = DriverManager.getConnection(url);
            	Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
              	  String prodID = rs.getString("prodid");
                        String custGR = rs.getString("custgr");
                        double price = rs.getDouble("price");
                    
                      //  String algus = rs.getString("algus");
                       // String lopp = rs.getString("lopp");
                        MatrixLine mline = new MatrixLine();
                        
                  
                        if (custGR != null && custGR.trim().length() > 0) {
                                mline.setCliGroup(custGR.trim());
                        }
                     
                        if (prodID != null && prodID.trim().length() > 0) {
                                mline.setProdID(prodID.trim());
                        }
                   

                        if (price != 0) {
                                mline.setPrice(price);
                        }
                        
                        /*if (algus != null && algus.trim().length() > 0) {
                            mline.setStartDate(algus.trim().substring(0,10));
                    }
                        if (lopp != null && lopp.trim().length() > 0) {
                            mline.setEndDate(lopp.trim().substring(0,10));
                    } */
                        
                        vahe.put(custGR.trim()+ "-" + prodID.trim(), mline);
                        
                        
                }
                rs.close();
                stmt.close();
                con.close();
                
        } catch (SQLException ex) {
        ex.printStackTrace();
        }
  
        Iterator itera = vahe.keySet().iterator();
        while(itera.hasNext()){
        	 lines.add(vahe.get(itera.next()));
        }
        
       

        
        
        return lines;
}

public String getProductSortingData() {
	return null;
}
}

