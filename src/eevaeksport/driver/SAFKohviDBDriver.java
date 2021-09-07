
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.driver;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;


public class SAFKohviDBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater{
  public SAFKohviDBDriver(EksportPanel parent){
    super(parent);
    if(parent.getParentFrame().isDaySales()){
         this.subjectFilesTo = getParent().getString("senddatamsg");
    } else {this.subjectFilesTo = parent.getString("filemsg");}
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      con3 = getNewConnection2();
      stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }

      public String getDealerCustomerCSV(String agent){
       return null;
       }

	  public void connectionUp(){
	  }
	  
	  public String getAssortmentStr(Agent agent){
	  	return null;
	  }

      public String getSaveStockInfo(){
      return null;
      }
      public String getSaveDebtInfo() {
       	return null;
       }
       
 	  public String  getCustProdDiscounts(Agent agent){
	  return null;
	  }
	
	
	  public void connectionDown(){
	  }

  
    public TreeMap getAgents(){
  	
    String cmd = "SELECT id AS agent, firstname + ' ' +surname AS nimetus, code as abikood  " + 
      "  FROM  eremployee order by firstname";
    
    if (isDebug()) System.out.println(cmd);
   
    HashMap aHash = new HashMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = rs.getString("nimetus").trim();
          agent.setName(name);
          agent.setIsPilot(true);
          agents.put(name, agent);
          aHash.put(agent.getID(), agent);
        }
    rs.close();
    //con.close();
    
    Agent aa = new Agent("HOUSE");
    aa.setName("HOUSE");
    aa.setIsPilot(true);
    agents.put("HOUSE", aa);
    aHash.put(aa.getID(), aa);

    } catch (Exception e){
        getParent().showError(e);
      }
    updateCalls();
    return agents;
  }



      public String getDistribution() {
/*
        StringBuffer sb = new StringBuffer();

        try {
          Connection con = getNewConnection();
          Statement st = con.createStatement();
          String cmd = "select mmitno as kood ,mmitds AS Nimetus , mmdim1 from mvxjdta.mitmas where MMCFI5 = 'Y'";

          if (isDebug())
          System.out.println(cmd);
          ResultSet rs = st.executeQuery(cmd);
         while(rs.next()){
           sb.append(rs.getString("kood") + ";" + rs.getString("nimetus") + "\n");
         }
         st.close();
         con.close();
       } catch (Exception e){}
         System.out.println(sb.toString());
        return sb.toString();*/
    return null;
      }


      public void insertSuperVisorSQL(SupervisorInfoWrapper wr){}

      public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
          String sql = "INSERT into supervisor values ("
            + "?, "   //   agent	C(50),
            + "?,"   //   agentname	C(200),
            + "?,"   //   clientid	C(50),
            + "?,"   //   clientname	C(200),
            + "?,"   //	prodid	C(50),
            + "?,"   //   prodname	C(200),
            + "? ,"   //	sentdate	D,getdate()
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
                PreparedStatement ps = con.prepareStatement(sql);

                for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
                	 eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
                  ps.setString(1, wr.getInfo().getAgentID());
                  ps.setString(2, wr.getInfo().getAgentName());
                  ps.setString(3, lw.getLine().getCustID());
                  ps.setString(4, lw.getLine().getCustName());
                  ps.setString(5, lw.getLine().getProdID());
                  ps.setString(6, lw.getLine().getProdName());
                  ps.setTimestamp(7, new java.sql.Timestamp(Long.parseLong(lw.getLine().getResStr3())));
                  
                  ps.setDouble(8, lw.getLine().getNormalPrice());
                  ps.setDouble(9, lw.getLine().getAmount());
                  lw.getLine().getDate();
                  ps.setDouble(10, 0);
                  ps.setDouble(11, 0);
                  ps.setInt(12, lw.getLine().getPercentageFromTotalCategory());
                  ps.setInt(13, lw.getLine().getPercentageFromColdCategory());
                  ps.setInt(14, lw.getLine().getPercentageFromPallCategory());
                  ps.setInt(15, lw.getLine().getPercentageFromShelfCategory());
                  ps.setInt(16, lw.getLine().getPercentageFromRestCategory());
                  ps.setDouble(17, lw.getLine().getAction1Eval()*100);
                  ps.setDouble(18, lw.getLine().getAction2Eval()*100);
                  ps.setDouble(19, lw.getLine().getActionTotalEval()*100);
                  ps.setDouble(20, lw.getLine().getCustEval()*100);
                  String cc = lw.getLine().getCustComment();
                  if (cc == null){
                  	cc = " ";
                  }
                  ps.setString(21, cc);

                  String cd = lw.getLine().getComment();
                  if (cd == null){
                  	cd = " ";
                  }
                  ps.setString(22, cd);

                  String ce = lw.getLine().getCustAgentId();
                  if (ce == null){
                  	ce = " ";
                  }

                  ps.setString(23, ce);

                  String cf = lw.getLine().getCustAgentName();
                  if (cf == null){
                  	cf = " ";
                  }
                  ps.setString(24, cf);
                  ps.setInt(25, lw.getLine().getDealNr());
                  ps.setInt(26, lw.getLine().getIdealNr());

                  String cr1 = lw.getLine().getResStr1();
                  if (cr1 == null){
                  	cr1 = " ";
                  }
                  ps.setString(27, cr1);


                  String cr2 = lw.getLine().getResStr2();
                  if (cr2 == null){
                  	cr2 = " ";
                  }
                  ps.setString(28, cr2);


                  String cr3 = lw.getLine().getResStr3();
                  if (cr3 == null){
                  	cr3 = " ";
                  }
                  ps.setString(29, cr3);
                  ps.setInt(30, lw.getLine().getResInt1());
                  ps.setInt(31, lw.getLine().getResInt2());
                  ps.setInt(32, lw.getLine().getResInt3());
                  ps.executeUpdate();
                }

                ps.close();
                
                
                
                
              
                 
            } catch (SQLException e) {
                e.printStackTrace();
            }
          }

        
    
      
      public void updateCalls(){
    	  try{
    		  String kutsed = "select custid ,sentdate from supervisor where prodname='V�ljakutse' and resstr1 ='' and sentdate> getdate()-6";
              
              Statement sty = con.createStatement();
              ResultSet rsy = sty.executeQuery(kutsed);
              HashMap kuts = new HashMap();
              while(rsy.next()){
            	  kuts.put(rsy.getString("custid").trim(),rsy.getTimestamp("sentdate").getTime()+ "");
              
              }
              rsy.close();
              sty.close();
              
              
              String vastus =  "select custid ,max(sentdate) as dd from supervisor where prodname!='V�ljakutse'  and sentdate> getdate()-6 group by custid";
              
              sty = con.createStatement();
              rsy = sty.executeQuery(vastus);
              
              while(rsy.next()){
            	  String klientID = rsy.getString("custid").trim();
            	
            
            	  if(kuts.containsKey(klientID)){
            		  long lopp = rsy.getTimestamp("dd").getTime();
            		  long algus = Long.parseLong((String)kuts.get(klientID));
            		long vahe = lopp-algus;//-720000
                	  String update = "update supervisor set resstr1 = '"  + getTimeStr(vahe) + "' where   custid = '" + klientID + "' and prodname='V�ljakutse' and resstr1 = '' and sentdate > getdate()-6 " ;
                        System.out.println(vahe + " " + update );
                      Connection cony = getNewConnection();
                      Statement stx = cony.createStatement();
                      
                      stx.executeUpdate(update);
                      stx.close();
                      cony.close();
                     
            	  }  	  
              }
    	  }catch (Exception e){
    		  e.printStackTrace();
    	  }
         
          
      }
      
      
      private String  getTimeStr(long xxx){
    	  if(xxx < 0){
    		  return "------";
    	  }
    	  Calendar c = Calendar.getInstance();
    	 
    	  c.setTimeInMillis(xxx);
    	  
    	  //System.out.println("long " + xxx +  " " + c.toString());
    	  
    	  String paev = "";
    	  if(c.get(Calendar.DATE)>1){
    		  paev = c.get(Calendar.DATE)-1 + "p ";
    	  }
    	  String tund = "";
    	  if(c.get(Calendar.HOUR)>0){
    		  tund = c.get(Calendar.HOUR)-2 + "t ";
    		  if ((c.get(Calendar.HOUR)+ "").length()==1){
    			  tund = "0" + (c.get(Calendar.HOUR)-2) + "h ";
    		  }else{
    			  tund = (c.get(Calendar.HOUR)-2) + "h ";
    		  }
    	  }
    	  String minut = "";
    	  if(c.get(Calendar.MINUTE)>0){
    		  minut = c.get(Calendar.MINUTE) + "t ";
    		  if ((c.get(Calendar.MINUTE)+ "").length()==1){
    			  minut = "0" + c.get(Calendar.MINUTE) + "m";
    		  }else{
    			  minut = c.get(Calendar.MINUTE) + "m";
    		  }
    	  }
    	  System.out.println("trykk " + paev+tund+minut);
    	  return paev+tund+minut;
      }
      
      private TreeMap getComments(){
      	TreeMap result = null;
      	try {

         	 Connection con = getNewConnection();
         	 
            Statement st = con.createStatement();
          
            
            String sss = "select k.code,c.description  from grcompany k inner join grcomment  c on c.idgrcompany=k.id where c.description like '%v�ljakutse%'";
  	      
  	     
  	    ResultSet rs = st.executeQuery(sss);
  	      while(rs.next()){
  	      	if(result == null)result = new TreeMap();
  	    	
  	      	String klient = rs.getString("code").trim();
  	      	String muu = rs.getString("description").trim();
  	      
  	      	if(result.containsKey(klient)){
  	      		String ss= (String)result.get(klient);
  	      		result.put(klient,ss + "|" +muu);	
  	      	  }else{
  	      		result.put(klient,muu);	
  	      	}
  	      	
  	      }
  	      rs.close();
  	      st.close();
            
            
            String cmd = "select agentname AS ag,sentdate as dt,resstr2 ,(custcomme),custid from supervisor " +
            		" where sentdate > getdate()-500 and resstr2 !='' order by sentdate desc ";

            st = con.createStatement();
	       rs = st.executeQuery(cmd);
	      while(rs.next()){
	      	if(result == null)result = new TreeMap();
	      	String muu = rs.getString("ag").trim() + "|" + rs.getString("dt").trim().substring(0,16)+ "|" 
			+ rs.getString("resstr2").trim();

	      	String klient = rs.getString("custid").trim();
	      	if(result.containsKey(klient)){
	      		String ss= (String)result.get(klient);
	      		result.put(klient,ss + "||" +muu);	
	      	}else{
	      		result.put(klient,muu);	
	      	}
	      	
	      }
	      rs.close();
	      st.close();
	      
	      
	      
	  //    System.out.println("result " +result);
            
      	}catch(Exception e){
      		e.printStackTrace();
      	}
         return result;
      }
      
  public TreeMap getCustomers(Agent agent){
    TreeMap customers = new TreeMap();

    try {
      Connection con = getNewConnection();
      Connection cony = getNewConnection();
      Connection con2 = getNewConnection();
      Statement st = con.createStatement();
  TreeMap comments = getComments();
  Vector horeca = new Vector();
  Vector weekly = new Vector();
  Vector quarter2000 = new Vector();
  //Keda peab korra aastas kui on horeca grupp.
  
  String sqly = "select custid,kl.name from supervisor inner join grcompany kl on kl.code = custid inner join " +
   "  grcompanycatrel rel on rel.idgrcompany=kl.id " +
   " where rel.idgrcompanycat = 25  AND sentdate < getdate()-330 group by custid,kl.name";
  
  Statement sty = con.createStatement();
  ResultSet rsy = sty.executeQuery(sqly);
  
  while(rsy.next()){
  	horeca.add(rsy.getString("custid"));
  	System.out.println(" Aasta plaani sees HORECA klient " + rsy.getString("name"));
  }
  rsy.close();
  sty.close();
  
//Keda peab kylastama iga 5 n�dala tagant vanad lepingud
  
  String sqlw = "select kl.code,U_nadal,name from grcompany kl INNER JOIN grcomment com ON com.idgrcompany=kl.id " +
      " Where idgrcommenttype = 1 and commentdate<'2007-06-01 00:00:00.000' and len(U_nadal) >0";
  
  Statement stw = con.createStatement();
  ResultSet rsw = stw.executeQuery(sqlw);
  
  while(rsw.next()){
  	
  	String custid = rsw.getString("code");
  	boolean kylastatud = false;
  	boolean vajaoleks = false;
  	
  	
  	//vaatame kas viimase kahe n�dala jooksul on seal k�idud
				  	String sqlc = "select custid from supervisor where custid='" + custid + "' and sentdate>getdate()-15 group by custid";
				  	 Statement stc = cony.createStatement();
				  	  ResultSet rsc = stc.executeQuery(sqlc);
				  	  
				  	  if(rsc.next()){
				  	  	kylastatud = true;
				  	  }
				  	  rsc.close();
				  	  stc.close();
				  	  
  	  int week = 0;
  	  //kas on meie n�dal
  	 
  	  try{
  	  	week = rsw.getInt("U_nadal");
  	  }catch(Exception e ){}
  	 int currentWeek = 0;
  	 String aa = "";
  	  if(week >0){
  	  			   
		          Calendar cal = Calendar.getInstance();
		           currentWeek  = cal.get(Calendar.WEEK_OF_YEAR);
		          int re = week - currentWeek;
		        
		          aa = (Double.parseDouble(re+"")/5) + "" ;
		          if (aa.indexOf(".")>-1){
		          	 aa = aa.substring(aa.indexOf("."),aa.length());
		          }
		          
		          if(Double.parseDouble(aa)== 0){	          	
		          	vajaoleks = true;
			      }
  	      }
  	    	String name = rsw.getString("name");
  	if(!kylastatud && vajaoleks){
  		weekly.add(custid);	
  		System.out.println("5 n�dala plaani sees vana klient " + name + " " + currentWeek + " " + week + " " + aa);
  	  }
  	
  //	System.out.println("XXX5 n�dala plaani sees vana klient " + name + " " + currentWeek + " " + week + " " + aa);
  	}
  
 // System.exit(0);
  
  //Keda peab kylastama peale teist kuud 2000 tassi v�i korra kvartalis aga uued kliendid 
  
  // Vaatame ainult uusi kliente 
  String sqlr = "select kl.code,max(name) AS name from grcompany kl INNER JOIN grcomment com ON com.idgrcompany=kl.id " +
  " Where idgrcommenttype = 1 and commentdate>'2007-06-01 00:00:00.000' and " +
  " commentdate>getdate()-60 and len(U_nadal) >0 group by code ";
	
	Statement str = con.createStatement();
	ResultSet rsr = str.executeQuery(sqlr);
	
	while(rsr.next()){
		String custid = rsr.getString("code");
		boolean kylastatud =  false;
		//vaatame kas viimase kvartli jooksul on k�idud jooksul on seal k�idud
	  	String sqlc = "select custid from supervisor where custid='" + custid + "' and sentdate>getdate()-92 group by custid";
	  	System.out.println(sqlc);
	  	Statement stc = cony.createStatement();
	  	  ResultSet rsc = stc.executeQuery(sqlc);
	  	  
	  	  if(rsc.next()){
	  	  	kylastatud = true;
	  	  }
	  	  
	    	  
		  rsc.close();
		  stc.close();
	  	  //vaatame kas on 2000 tassi t�is orinteeruvalt
		  	String sql2 = "select custid from supervisor where custid='" + custid + "' and sentdate>getdate()+92 group by custid";
		  	 Statement st2 = con2.createStatement();
		  	  ResultSet rs2 = st2.executeQuery(sql2);
		  	  
		  	  if(rs2.next()){
		  	  	//joodud = true;
		  	  }		  	  
		    	  
			  rs2.close();
			  st2.close();
		  
	  	  
	  	  
	  	  if(!kylastatud){
			quarter2000.add(custid);	
			System.out.println("kvartal v�i 2000 Tassi t�is  " + rsr.getString("name"));
		  }	
	  	  
	   }
	
	
  
  System.out.println("Kvartal " + quarter2000.toString());

  System.out.println("5 n�dalat " + weekly.toString());

  System.out.println("Horeca " + horeca.toString());
  
 // System.exit(0);
      String cmd2 = "SELECT u_tehnik as tehnik, fax AS asuk, u_isikukood AS asukoht, co.description as masin, kl.code AS kood,ltrim(fax) + ' ' + ltrim(kl.name)  AS nimi,kl.locationaddr AS address,'' AS visit ,creditlimit AS limiit, " + 
           "(case when len(ltrim(kl.idsettlcompany)) >0 then kl.idsettlcompany else kl.id end) AS payer,-kl.pricecut AS ale ,phone ,u_nadal ,u_hooldusvalp AS valp  ,u_teavitusemail as mail " +
           " FROM grcompany kl  inner join grcomment co on co.idgrcompany = kl.id and co.idgrcommenttype=7  and co.description is not null  " +
           " order by kl.name";
      
      

      if (isDebug()) System.out.println(cmd2);
      ResultSet rs = st.executeQuery(cmd2);
      HashMap custMap = new HashMap();

      while(rs.next()){
      	String tehnik = rs.getString("tehnik");
    	
    	
    	String asukoht = rs.getString("asukoht");
        
      	String vin = rs.getString("masin").trim();
      	if(asukoht != null && asukoht.trim().length()>0){
      		asukoht = " " + asukoht.trim() +  " ";
      	}else{
      		asukoht = "";
      	}
      	
      	if(tehnik != null && tehnik.trim().length()>0){
      		tehnik = " " + tehnik.trim() +  " ";
      	}else{
      		tehnik = "";
      	}
          String accountnum = rs.getString("kood").trim();
        	
          Customer cust = new Customer(accountnum);
          String name = rs.getString("nimi").trim();
          
          if(comments != null){
        	  String comment = (String)comments.get(accountnum);          	
              if(comment != null){
              	cust.setInfo(comment);
              }
                	  
          }   
          
          String aadress1 = rs.getString("address");
          if (aadress1 != null) {aadress1 = aadress1.trim();} else {aadress1 = " ";}
          cust.setAddress(aadress1);
          name =  name + asukoht + " " + aadress1.trim() + " " + vin;
          cust.setName(name.replaceAll("\n", " "));
          String visit = rs.getString("visit");
          if(visit != null){
          }
         String limiit = rs.getString("limiit");
          if (limiit != null && limiit.trim().length()>0){
          	String lim = limiit.substring(0,limiit.indexOf(".")).trim();
          	if (lim.length()>0){
          		cust.setCredit(lim);		
          	}
          	}

          Iterator iter = this.agents.values().iterator();
          while (iter.hasNext()){
            Agent a = (Agent) iter.next();      
          cust.addAgent(a);
          }
          Agent aa = new Agent("HOUSE");
          cust.addAgent(aa);
    
          cust.setPayer(rs.getString("payer").trim());
          double ale  = rs.getDouble("ale");
          if (ale>0){cust.setDiscount(ale);}
          String phone = rs.getString("phone");
          if(phone != null && phone.trim().length()>0){
          	cust.setPersonalData("tel. " +phone.trim());
          }
          if(weekly.contains(cust.getID()) || horeca.contains(cust.getID())
          		|| quarter2000.contains(cust.getID())){
          //	cust.setVisitToday(true);
          }
     
          String mail = rs.getString("mail");
          if(mail != null && mail.trim().length()>0){
        	  if(mail.indexOf(";")>0){
        		  mail = mail.substring(0,mail.indexOf(";"));
        	  }
        	  cust.setConfirmEmail(mail.trim().replaceAll(";", ""));
          }
          
          customers.put(name, cust);
          custMap.put(cust.getID(), cust);
    }


    String cmd3 = "SELECT d.idpartner AS maksja,k.code AS saaja , " +
" d.totalamount - sum(m.amount) AS minus " +
" FROM sdinvoice d " +
" INNER JOIN grcompany k on k.id = d.idgrcompany " +
" LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
" WHERE docstate !='F' AND exchangerate=1 AND d.promptdate < getdate() -3 group by d.sourcedocno,d.idpartner," +
"k.code,d.promptdate,d.totalamount ORDER " +
" BY d.promptdate,d.idpartner";

     if (isDebug()) System.out.println(cmd3);
        ResultSet rs1 = st.executeQuery(cmd3);
         while (rs1.next()) {
           String id = rs1.getString("saaja").trim();
           double debt =rs1.getDouble("minus");
          // System.out.println(id + "  " + debt);
           Customer cust = (Customer) custMap.get(id);
           if (cust != null && debt != 0) {
             cust.setDebt(cust.getDebt()+ debt);
            //  System.out.println(id + "  " + debt);
           }
         }

         st.close();
         con.close();


    } catch (SQLException e){
      getParent().showError(e);
    }
    return customers;
  }

  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = " SELECT id AS liik , name AS nimetus FROM grarticlecat  ORDER BY name ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
        tm.put(rs.getString("liik").trim(),
               rs.getString("nimetus").trim());
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}

    public TreeMap getProdGroupsSecond() throws SQLException {
      return null;
  }

    



  public TreeMap getProducts(Agent agent){

    String cmd = " SELECT  pr.id,y.name AS yhik ,pr.code AS kood,  rtrim(pr.code) + ' ' + rtrim(y.name) +  ' ' + rtrim(pr.name)   AS nimi, " +
    "	hi.price AS hind,'' AS EAN ,cat.idgrarticlecat AS grupp,  " +
    "     pr.contentquantity AS tykke " +
    "     FROM grarticle pr LEFT JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id LEFT JOIN grpricelistrel hi ON  " +
    "     hi.idgrarticle=pr.id  LEFT JOIN grarticlecat arca ON arca.id = cat.idgrarticlecat  " +
    "     LEFT JOIN grunit y ON y.id=pr.idgrunit    " +
    "     WHERE  (articletype='G' OR articletype='P' OR articletype='S') and idgrpricelist = 15 and " +
    "     (cat.idgrarticlecat in (29,45,33,8,2,22,1,24,3,4,31,23,17,5,32,7,21,6) or pr.code in ('002026','002036','002037'" +
    "  ,'002038','002039','002040')) order by pr.code " ; // and left(pr.code,3) in ('005','006','007','008','009') ";
    
   // 002036, 002037, 002038, 002039 002040

    
    String cmd2 = "SELECT pr.code AS toode,SUM(quantity) AS seis FROM dcurbalancestore ss " +
    		" INNER JOIN grarticle pr ON idgrarticle = pr.id WHERE ss.iddrstore!=6 AND quantity!=0 " +
	        " GROUP BY  pr.code " ;

    //siin on k�sa kas k�ik laod liidame kokku 
    
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {
      Connection con2 = getNewConnection();
      Statement st2 = con2.createStatement();
      ResultSet rs2 = st2.executeQuery(cmd2);
      Hashtable tooted = new Hashtable();
        while(rs2.next()){
          String id = rs2.getString("toode");
          Product toode = new Product(id);
          double sto = rs2.getDouble("seis");
          toode.setStock(sto);
          //toode.setMeasure(ost);
          tooted.put(id,toode);
        }

      Connection con = getNewConnection();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(cmd);

      while(rs.next()){
    	  
    	  String unit = rs.getString("yhik");

        String kood = rs.getString("kood").trim();

         Product prod = new Product(kood);

         if (unit != null && unit.trim().length() > 0) {
           unit = unit.trim();
           prod.setUnit(unit);
         }
         String nimetus = rs.getString("nimi").trim();
         //prod.setStock(kogus);
         double hind = rs.getDouble("hind");
         prod.setPrice(hind);
         prod.setName("    " + nimetus);
         String ean = rs.getString("ean");
         if (ean != null && ean.trim().length() > 7) {
           prod.setEAN(ean.trim());
         }

         String grupp = rs.getString("grupp");
         if (grupp != null && grupp.trim().length() > 0) {
           prod.setGroup(grupp.trim());
         }
         
      

         prod.setInCases( (rs.getDouble("tykke")));
      
        products.put(nimetus, prod);
        //}
      }
      rs.close();
      st.close();
      con.close();

    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }



  public Vector getCustomerProductsList(String agentCode){

    String sql1 = 	"SELECT k.code AS klient,t.code AS toode, r.quantity AS kogus ,r.docdate as kuupaev ,r.unitprice AS lastprice  " +
   " FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc "+
   " INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=d.companyname " +
   " WHERE r.docdate>getdate()-1590 AND len(ltrim(d.idgrcompany))>0 " +
   " AND (r.articletype='P' OR r.articletype='G') GROUP BY k.code,t.code,r.quantity,r.docdate,r.unitprice ORDER BY k.code,t.code,r.docdate";


           if (isDebug()) {
                System.out.println(sql1);
            }

            Vector custProds = new Vector();

            try {
              Connection con = getNewConnection();
              PreparedStatement stmt = con.prepareStatement(sql1);

              //stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
              ResultSet	      rs = stmt.executeQuery();

              HashMap kliendid = new HashMap();
              while (rs.next()) {
                String kliendikood = rs.getString("klient").trim();
                String tootekood = rs.getString("toode").trim();
                double kogus = Double.parseDouble(rs.getString("kogus").trim());
                String lastdate = rs.getString("kuupaev").substring(0,10);
                // lastdate = lastdate.substring(5,10);
                double hind = Double.parseDouble(rs.getString("lastprice").trim());
                Customer cust = (Customer)kliendid.get(kliendikood);
                if (cust == null){
                  cust = new Customer(kliendikood);
                }
                Product prod = new Product(tootekood);
                CustProd cp = new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,lastdate,hind,0,0," ");
                cust.addProduct(prod,cp,false );
                kliendid.put(kliendikood, cust);
                custProds.add(cp);
              }

              rs.close();
              stmt.close();

           
              con.close();
            } catch (Exception e) {
              getParent().showError(e);
            }
            connectionDown();
            return custProds;
  }

  public void setBulkSales(Map agents){
/*
    String cmd = "SELECT  cat.id AS agent ,SUM(r.amount)  AS summa, SUM(t.u_maht*r.quantity)AS summavol " +
    " FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
   " INNER JOIN eremployee w ON w.id= d.idworker  " +
   " INNER JOIN grcompanycat cat  ON cat.code=w.code " +
   " INNER JOIN grarticle t ON t.id= r.idgrarticle" +
   " WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)=MONTH(getdate()) AND r.idgrunit!=1 " +
   " AND (r.articletype='P' OR r.articletype='G') GROUP BY cat.id";

       if (isDebug()) System.out.println(cmd);
          try {
            Connection con = getNewConnection();
              Statement stmt = con.createStatement();
              ResultSet	      rs = stmt.executeQuery(cmd);
              while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                  agent.setMonthSales((int)rs.getDouble("summa"));
                  agent.setMonthSalesVol((int)rs.getDouble("summavol"));
                 // System.out.println(agent.getMonthSales());
                }
              }
              rs.close();
              stmt.close();
              con.close();
          } catch (Exception e) {
              getParent().showError(e);
           }
  */
              }



  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


  public DealerProduct getDealerProductsList() {

/*
       DealerProduct dealerProds = new DealerProduct();
       try {


          String  sql = " SELECT pr.code AS toode,(quantity) AS stock , ss.iddrstore AS ladu FROM dcurbalancestore ss " + 
    		" INNER JOIN grarticle pr ON idgrarticle = pr.id WHERE ss.iddrstore in ('601','600','607') AND quantity!=0 " +
	        " order BY  pr.code" ;

            if (isDebug()) System.out.println(sql);
               Connection con1 = getNewConnection();
               Statement stm = con1.createStatement();
               ResultSet rst = stm.executeQuery(sql);

                 while (rst.next()) {
                   String toode = rst.getString("toode").trim();
                   int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                   String ladu  =  rst.getString("ladu").trim();
                  	double amo = dealerProds.getDealerProductStock(ladu,toode);
                   if(amo>0){
                   	dealerProds.remDealerProduct(ladu,toode);
                    dealerProds.add(ladu, toode,(amo+seis)+"" ,"","");
                   }else{
                    dealerProds.add(ladu, toode,seis+"" ,"","");     	
                   }
                  }
               rst.close();
               stm.close();
               con1.close();

       } catch (Exception e) {
           getParent().showError(e);
       }
*/
       // return dealerProds;
        return null;
      }



      public Vector getDebt(String agentCode) throws SQLException{
       Vector list = new Vector();
       String sql = " SELECT d.idpartner AS maksja,d.sourcedocno AS arve, d.promptdate AS kuupaev, " +
        " d.totalamount AS SUMMA ,sum(m.amount) AS minus ,-datediff(day,getdate(),d.promptdate)  AS paevi " +
        " FROM sdinvoice d " +
        " LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
        " WHERE docstate !='F' AND exchangerate=1 AND d.promptdate < getdate() +5 " +
        "  group by  d.sourcedocno,d.idpartner,d.promptdate,d.totalamount ORDER BY d.idpartner " ;

     if (isDebug()) System.out.println(sql);
       Connection con = getNewConnection();
       stmt = con.createStatement();
       ResultSet rs = stmt.executeQuery(sql);

       while(rs.next()){
         Debt debt = new Debt();
         debt.setCustID(rs.getString("maksja").trim());
         debt.setDoknr(rs.getString("arve").trim());
         debt.setDuedate(rs.getDate("kuupaev"));
         
         double summa = rs.getDouble("summa");
         double minus = -rs.getDouble("minus");
         debt.setDue(rs.getString("paevi"));
         
         if( summa+minus > 1 || summa+minus < -1){
              debt.setSum(summa+minus);
           list.addElement(debt);
         }
         
       }
       rs.close();
       stmt.close();
       con.close();
       return list;
    }

    public ManagerData getManagerData() throws SQLException{
       ManagerData md = new ManagerData();
       return md;
     }


    public double getFoodSales(String agentCode) throws SQLException{
      return 0;
    }

    public double getAlcoSales(String agentCode) throws SQLException{
      return 0;
    }
    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
    }

    public String getStartingTime() {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MONTH,
              -Integer.parseInt(getParent().getProps().getProperty("months")));
      int month = cal.get(Calendar.MONTH) + 1;
      String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
      String sqlDate = cal.get(Calendar.YEAR) + month + day;
      return sqlDate;
    }

    public String getDriverVersion() {
      return "SAFKohvi";
    }

    public String getSubjectFilesTo() {
      return this.subjectFilesTo;
    }


    public String getDaySalesData(Agent agent) {
      return null;
    }



	   public String getPilotDayData(Agent agent){

	 	   	
     
	        StringBuffer sb = new StringBuffer();
	 	   	
	 	   	
		       if(!agent.getID().equals("HOUSE")){
		    	
		            
	        String sql2 =  "select max(kliendikood) AS klient ,max(tellnr) AS arve, max(kuupaev) AS kuupaev , count (tellnr) as ridu   "+
	" from tp inner join ts on tp.tp_id=ts.tp_id  WHERE year(kuupaev)=year(getdate()) AND month(kuupaev)=month(getdate()) and  " +
  "	day(kuupaev)=day(getdate())  and sisestajaID = " + agent.getID() + " " +
	" group by tp.tp_id order by max(sisestajaID) ";
		     
	   
	        
	        System.out.println("siin " + sql2);
	          String header = " T�nased tehingud :\n\n";
	          boolean first = true;
	          nf.setMinimumFractionDigits(2);
	            try {
	            	
	                     Statement st = con3.createStatement();
	                    ResultSet rs2 = st.executeQuery(sql2);
	                        while (rs2.next()) {
	                        	if(first){
	                        		sb.append(header);
	                        		first = false;
	                        	}
	                        	String klient = rs2.getString("klient").trim() ;
	                        	String sql3 = " SELECT kl.code AS kood,ltrim(kl.name) AS nimi " +
						            "   FROM grcompany kl " +
						            "  WHERE kl.code= '" + klient + "'";
	                        	Connection con4 = this.getNewConnection();
	                      Statement st3 = con4.createStatement();
	                      ResultSet rs3 = st3.executeQuery(sql3);
	                      String nimi = "";
	                      if (rs3.next()){
	                      	nimi = rs3.getString("nimi").trim();
	                        	
	                      }
	                      rs3.close();
	                      st3.close();
	                      con4.close();
	                      
	                        sb.append(nimi  +
	                        		"\n    (" + rs2.getString("arve").trim() + ")[" + rs2.getString("kuupaev").substring(0,10) + "] " + 
	                        		getParent().getNF().format(rs2.getDouble("ridu"))  
	                        	+" rida "	+  "\n\n");
	                        }

	                        
	                        
	                        } catch (Exception e) {
	                            getParent().showError(e);
	                        }
	                        System.out.println(agent.getID() + sb.toString());
	                        
		       }
	           if(sb.toString().trim().length()>0){
	           	
	            return sb.toString();
	           }else {
	           	return null;
	           }
	    }

    public void insertDefective(DefectiveWrapper wr){}

    private Connection getNewConnection2() throws SQLException{
        return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
      }

    public void insertDistribution(DistEvalListWrapper wr){
    }

 

    public TreeMap getDealers() {
      return dealers;
    }

    public void setDealers(TreeMap aDealers) {
      dealers = aDealers;
    }

    public NumberFormat getNF() {
      return NF;
    }

    public void setNF(NumberFormat aNF) {
      NF = aNF;
    }


  private String url = "jdbc:odbc:SAF";
  private String url2 = "jdbc:odbc:import";
  private Connection con3;
  private Connection con;
  private Statement stmt;
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private  TreeMap agents = new TreeMap();
  private String subjectFilesTo = null;
  private NumberFormat NF = NumberFormat.getInstance();
  private TreeMap  dealers = new TreeMap();
}
