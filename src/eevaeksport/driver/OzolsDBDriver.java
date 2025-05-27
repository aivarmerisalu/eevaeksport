/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class OzolsDBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater,Discounter
{
	

	
	public OzolsDBDriver(EksportPanel parent) {
		super(parent);
		this.subjectFilesTo = parent.getString("filemsg");
		// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
		String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
		
		try {
			Class.forName(databaseDriverName).newInstance();
			
		} catch (Exception e) {
			parent.showError(e);
		}
	
		 if(getParent().getProps().getProperty("dns.name") != null){
			 url = "jdbc:odbc:" + getParent().getProps().getProperty("dns.name") ;
			 url2 = "jdbc:odbc:" + getParent().getProps().getProperty("dns.name") ;
		 }
		
		
		 		
	    }

	
	public String getSaveDebtInfo() {
	   	return null;
	   }
	   
	public String getAssortmentStr(Agent agent){
		return null;
	}
	
	public String getCustProdDiscounts(Agent agent){
		return null;
	}
	
	public String getDealerCustomerCSV(String agent){
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
	          + "?,"   //   prodname	C(200),agentid agentname custid custname prodid prodname
	          + "getdate() ,"   //	sentdate	D,
	          + " '' ,"   //	receivedTime	D,
	          + "?,"   //normalpr	N,
	          + "?,"   // amoun	N,
	          + "?,"   // grouptotal	N,
	          + "?,"   // cattotal	N,
	          + "?,"   // pertotcat	I,sentdate normalpr amount grouptotal cattotal pertotcat
	          + "?,"   // percolcat	I,
	          + "?,"   // perpalcat	I,
	          + "?,"   // pershecat	I,
	          + "?,"   // perrescat	I,
	          + "?,"   // isaction	L,
	          + "?,"   // ispartner	L,
	          + "?,"   // isbrand	L,
	          + "?,"   // isgroup	L
	          + "?,"   // actionaeva	N,percolcat perpalcat pershecat perrescat isaction ispartner isbrand isgroup actionaeva
	          + "?,"   // actionbeva N,
	          + "?,"   // actiontota	N,
	          + "?,"   // custeval	N,
	          + "?,"   // custcomme	C(200),
	          + "?,"   // comment	C(200),
	          + "?,"   // custagent	C(200),
	          + "?,"   // custagentname	C(200),actionbeva actiontota custeval custcomme comment
	          + "?,"   // mustbe	I,
	          + "?,"   // ideal	I,
	          + "?,"   // resstr1	C(200),
	          + "?,"   // resstr2	C(200),
	          + "?,"   // resstr3	C(200),
	          + "?,"   // resint1	I,
	          + "?,"   // resint2	I,
		      + "?)" ; //	resint3	L
	        //   resstr1 resstr2 resstr3 resint1 resint2 resint3

	          try {	Connection c = DriverManager.getConnection(url2,"mobile","mobile123");
	              PreparedStatement ps = c.prepareStatement(sql);

	              for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
	            	  eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
	                ps.setString(1, wr.getInfo().getAgentID());
	                ps.setString(2, Converter.deCode(wr.getInfo().getAgentName()));
	                ps.setString(3, lw.getLine().getCustID());
	                ps.setString(4, Converter.deCode(lw.getLine().getCustName()));
	                ps.setString(5, lw.getLine().getProdID());
	                ps.setString(6, Converter.deCode(lw.getLine().getProdName()));
	                ps.setDouble(7, lw.getLine().getNormalPrice()*100);
	                ps.setDouble(8, lw.getLine().getAmount()*100);
	                ps.setDouble(9, 0);
	                ps.setDouble(10, 0);
	                ps.setInt(11, lw.getLine().getPercentageFromTotalCategory());
	                ps.setInt(12, lw.getLine().getPercentageFromColdCategory());
	                ps.setInt(13, lw.getLine().getPercentageFromPallCategory());
	                ps.setInt(14, lw.getLine().getPercentageFromShelfCategory());
	                ps.setInt(15, lw.getLine().getPercentageFromRestCategory());
	                ps.setBoolean(16, lw.getLine().isAction());
	                ps.setBoolean(17, lw.getLine().isPartner());
	                ps.setBoolean(18, lw.getLine().isBrand());
	                ps.setBoolean(19, lw.getLine().isGroup());
	                ps.setDouble(20, lw.getLine().getAction1Eval()*100);
	                ps.setDouble(21, lw.getLine().getAction2Eval()*100);
	                ps.setDouble(22, lw.getLine().getActionTotalEval()*100);
	                ps.setDouble(23, lw.getLine().getCustEval()*100);
	                ps.setString(24, lw.getLine().getCustComment());
	                String asi1 = lw.getLine().getComment();
	                if( asi1 == null){
	                	asi1 = " ";
	                 }
	                ps.setString(25, asi1);
	                
	                String asi2 = lw.getLine().getCustAgentId();
	                if( asi2 == null){
	                	asi2 = " ";
	                 }
	                ps.setString(26, asi2);
	                
	                

	                String asi3 = lw.getLine().getCustAgentName();
	                if( asi3 == null){
	                	asi3 = " ";
	                 }
	                ps.setString(27, asi3);
	                ps.setInt(28, lw.getLine().getDealNr());
	                ps.setInt(29, lw.getLine().getIdealNr());
	                
	                String asi4 =  lw.getLine().getResStr1();
	                if( asi4 == null){
	                	asi4 = " ";
	                 }
	                ps.setString(30,asi4);
	                
	                
	                String asi5 = lw.getLine().getResStr2();
	                if( asi5 == null){
	                	asi5 = " ";
	                 }
	                ps.setString(31, asi5);
	                
	                
	                String asi6 = lw.getLine().getResStr3();
	                if( asi6 == null){
	                	asi6 = " ";
	                 }
	                ps.setString(32, asi6);
	                ps.setInt(33, lw.getLine().getResInt1());
	                ps.setInt(34, lw.getLine().getResInt2());
	                ps.setInt(35, lw.getLine().getResInt3());
	                ps.executeUpdate();
	              }

	              ps.close();
	          } catch (SQLException e) {
	              e.printStackTrace();
	          }
	        }


	public String getSaveStockInfo(){
		
	    String  cmd = " SET DATEFORMAT dmy " +
	    " exec atsk_n_setfltr @nSkfId = 771, @nRcl = 1, @lNotIncTrn = 0, @lNShowNotRest = 0, @lNShowBSumRest = 0, @lNShowBDzRest = 0, @lNotShNTurnov = 0, @cGrpCol = '', @cGrpTbl = '', " +
	    "@dDat_b = '" + getDateStringYYYYmMMmDD(new Date())+ "'";
	    	        
		
		if (isDebug()) {
			System.out.println(cmd);
		}
		
		StringBuffer products = new StringBuffer();
		try {
			Statement st = con.createStatement();
			ResultSet	      rs = st.executeQuery(cmd);
			while (rs.next()) {
				
				int seis = (int)Double.parseDouble(rs.getString("batl_daudz").replace(',','.'));
	              
				String toode = rs.getString("nrs_id").trim();
				 String kood  =  rs.getString("nns_id").trim();
            	
				products.append(toode +  ";");
				products.append(" ;");
				products.append(getParent().getNF().format(seis));
				products.append("; ;");
				products.append(kood +  "\n");
			}
			rs.close();
		} catch (Exception e) {
			getParent().showError(e);
		}
		return products.toString();
	}
	
	
	
	  public void setBulkSales(Map agents) {
	       //if (agentCode == null) return 0;
	       connectionUp(url);
	         String cmd = "select rrd.cps_id_m as agent ,sum(netosumma) as sales  from rrd " +
	         		" INNER JOIN rrc on rrd.rrd_id=rrc.rrd_id where sct_id=20 group by rrd.cps_id_m";
	         
	         log(cmd);

	         try {
	             stmt = con.prepareStatement(cmd);
	             //stmt.setString(1, agentCode);

	             Calendar cal = Calendar.getInstance();
	             cal.set(Calendar.DAY_OF_MONTH, 1);

	             ResultSet	      rs = stmt.executeQuery();

	             while(rs.next()) {
	                String agentID = rs.getString("agent").trim();
	                 Agent agent = (Agent)agents.get(agentID);
	                 if (agent != null){
	                   agent.setMonthSales((int)rs.getDouble("sales"));
	                       }
	                    }

	             rs.close();
	             stmt.close();

	         } catch (Exception e) {
	             getParent().showError(e);
	         }

	     }
	
	/**
	 *
	 */
	public void connectionUp(){
		connectionUp(this.url);
	}
	
	
	public String getDistribution() {
	return null;
	}
	
	
	
	private void connectionUp(String url){
		try {
			if (con != null){
			//	con.close();
			}
			con = DriverManager.getConnection(url, getParent().getProps().getProperty("dns.user"),
					getParent().getProps().getProperty("dns.pass"));
		} catch (Exception e) {
			getParent().showError(e);
		}
		
	}
	
	/**
	 *
	 */
	public void connectionDown(){
		try {
			con.close();
			con = null;
		} catch (Exception e) {
			getParent().showError(e);
		}
	}
	
	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	
	public TreeMap getProdGroups() throws SQLException {
		 connectionUp(url);
	    TreeMap   tm = new TreeMap();
	    String    sql = "select npg_id as liik,nos as nimi from npg " ;

	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	        tm.put(rs.getString("liik").trim(),
	        		Converter.enCodeEst(rs.getString("nimi").trim()));
	    }
	    rs.close();
	    st.close();
	    connectionDown();
	    return tm;
	}
	TreeMap agents = new TreeMap();
	
	
	public TreeMap getAgents() {
		connectionUp(url);
		String  cmd = "select cps_id as yksus , nos as nimi  , nns_id as ladu from cps where s_stra = 1 ";
		 agents = new TreeMap();
		 StringBuffer sb2 = new StringBuffer();
		try {
			
			String cmd2 = "SELECT  cps.cps_id_m AS agent   From cps left join cpa on cpa.dok_id=cps.cps_id  " +
   " where (s_pirc = 1 or s_stra = 1 ) and cps.cps_id_m <> 0 group by cps.cps_id_m";
			
			stmt = con.prepareStatement(cmd2);
			ResultSet	      rs = stmt.executeQuery();
			Vector ags = new Vector();
			while (rs.next()) {
			  ags.add(rs.getString("agent").trim());	
			}
			rs.close();
			stmt.close();
			
			stmt = con.prepareStatement(cmd);
				      rs = stmt.executeQuery();
			
			while (rs.next()) {
				Agent agent = new Agent(rs.getString("yksus").trim());
				String nimi = rs.getString("nimi").trim();
				String ladu = rs.getString("ladu").trim();
				agent.setName(nimi +" " + ladu);
				agent.setIsPilot(true);
				if(!ladu.equals("0")){
					agent.setObject(ladu);
				}
				if(!ags.contains(agent.getID())){
					agent.setSednAllClients(true);	
				}
				
                System.out.println(agent.getID() + " " + agent.isSednAllClients());
				 sb2.append(agent.getID() + ";" + nimi + " " + ladu + "\n");
				agents.put(nimi, agent);
			}
		} catch (Exception e) {
			getParent().showError(e);
		}
		  
        if(sb2.length() > 2){
	    	try{
	    		 Converter.saveFileInUTF8( "agent.txt", Converter.enCodeEst(sb2.toString()));
	    	}catch (Exception e){
	    		
	    	}
	      
	     }
		//   connectionDown();
		return agents;
	}
	
	

	 
	public TreeMap getCustomers(Agent agent){
		connectionUp(url);
		String cmd = " SELECT  rtrim(cps.nos) AS nimi,ltrim(str(cps.cps_id)) as kood0, ltrim(str(cpa_id)) AS kood, cps.kods as code , rtrim(iela) + ' '+ rtrim( pils) + ' ' +  rtrim(pi) AS aadress,0 AS ale, " + 
   "  '' AS visit , not_used  AS blokk,  cps.cps_id AS payer,nns_id AS ware  , cpa.cps_id_m AS agent ,cps.cps_id_m as agent2 " +
   "  ,kredits as limiit, cpg.cpu_id as fixed " +
   "   From cps left join cpa on cpa.dok_id=cps.cps_id " + 
   " left outer join cpg on cps.cps_id = cpg.cps_id and cpg.cpu_id > 100 " +
   " where s_pirc = 1 or s_stra = 1";
		log(cmd);
		
		TreeMap customers = new TreeMap();
		HashMap custMap = new HashMap();
		HashMap comments = new HashMap();
		
		try {
			
			stmt = con.prepareStatement(cmd);
			ResultSet	rs = stmt.executeQuery();
			while (rs.next()) {
				String nimi = rs.getString("nimi");
				if(nimi != null){
				String kk0 = rs.getString("kood0").trim();
				String kk = rs.getString("kood");
				if(kk != null){
					kk = "-" + kk.trim();
				}
					
				Customer cust = new Customer(kk0);
				cust.addFixSortCode(kk0);
				cust.setName(Converter.enCodeEst(nimi.replace(',',' ') + " " + rs.getString("code").trim()));
				String aadr = rs.getString("aadress");
				if(aadr != null){
					cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace(',',' ')),"Latvia"));
				}
				//String ale = rs.getString("ale");
				//if (ale != null && ale.trim().length() > 0){
					//ale.replace('%',' ');
				//	cust.setDiscount(Double.parseDouble(ale.trim()));
				//}
				//String dist = rs.getString("distribution") ;
				//cust.setVisit(rs.getString("visit").trim());
				
				
				cust.setShowmarketinfo(true);
				//(case when SL01075='0' AND SL01060='0' then 0 else 1 end) AS blokk
				String  blokk = rs.getString("blokk") ;
				if(blokk.trim().equals("1")){
					cust.setSalesBlockT(true);
				}
				
				String payer = rs.getString("payer");
				if(payer != null && payer.trim().length() > 0){
					cust.setPayer(payer.trim());
				}
				
				String ware =  rs.getString("ware");
				if(ware != null && ware.trim().length() > 0 && !ware.trim().equals("0")){			
					cust.setWare(ware.trim());
				}
				
				
				String ag = rs.getString("agent");
				if(ag != null && ag.trim().length() > 0){	
					cust.addAgent(new Agent (ag.trim()));
					cust.setMainAgent(ag.trim());
				}
				
				
				String ag2 = rs.getString("agent2");
				if(ag2 != null && ag2.trim().length() > 0){	
					cust.addAgent(new Agent (ag2.trim()));
					cust.setMainAgent(ag2.trim());
				}
				
				
				String fixed = rs.getString("fixed");
				if(fixed != null && fixed.trim().length() > 0){	
					cust.addFixSortCode(fixed.trim());
				}
				
				
				if(cust.hasAgents()){
				
				 if("true".equals(getParent().getProps().getProperty("all.customers"))){
	         			Enumeration enume = getParent().getAgentsHash().elements();
	         				while(enume.hasMoreElements()){
	         					Agent a = (Agent)enume.nextElement();
	         					cust.addAgent(a);
	         				}
	         		} else if(getParent().getProps().getProperty("all.customers") != null){
	         			String ags = getParent().getProps().getProperty("all.customers");
	         			StringTokenizer st = new StringTokenizer(ags ,",");
	         			while (st.hasMoreElements()){
	         				Agent aa = new Agent(st.nextToken());
	         				cust.addAgent(aa);
	         			}
	         		} else {
	         			Enumeration enume = getParent().getAgentsHash().elements();
	     				while(enume.hasMoreElements()){
	     					Agent a = (Agent)enume.nextElement();
	     					if(a.isSednAllClients()){
	     						cust.addAgent(a);
	     					}
	     				}
	         		}
				} 
				/*String limiit = rs.getString("limiit");
	             if(limiit != null && limiit.trim().length()>0 && limiit.trim().length()<8){
	               	cust.setCredit(limiit.trim());              	
	             }*/
	                
	            
				if(comments.get(cust.getID()) != null){
					cust.setInfo(Converter.enCodeEst(((String)comments.get(cust.getID())).replaceAll("\n", " ")));
				}
				
			//	if(used.get(cust.getPayer()) != null){
				//	cust.setUsedCredit(used.get(cust.getPayer()));
			//	}
				
				
				
				Customer custo = (Customer)custMap.get(cust.getPayer());
						if(custo != null){
							cust.setDebt(custo.getDebt());
						}
						
				
						
						
				customers.put(cust.getName(), cust);
				custMap.put(cust.getID(), cust);
				
				}
				}
			rs.close();
			stmt.close();
			
			
			
			
		} catch (Exception e) {
			getParent().showError(e);
		}
		
		return customers;
	}
	
	
	  public static final String addSpaces(String s, int totalLen){
		    int len = s.length();
		    StringBuffer sb = new StringBuffer(s);
		    for (int i=len; i < totalLen ; i++){
		      sb.append(' ');
		    }
		    if( len > totalLen){
		    	return s.substring(0,totalLen);
		    }
		    return sb.toString();
		  }
		  
	  
	
	public Vector getDebt(String agentCode) throws SQLException{
		Vector list = new Vector();
		String sql = " SET DATEFORMAT dmy " +
                      " exec atsk_n_setfltr @nSkfId = 540, @cTblV = 'rrd', @dDat_uz = '" + getDateStringYYYYmMMmDD(new Date())+ "'";
		
		connectionUp(url);
		System.out.println(sql);
		stmt = con.prepareStatement(sql);
	
		ResultSet rs = stmt.executeQuery();
		
		while(rs.next()){
			Debt debt = new Debt();
			debt.setDoknr(rs.getString("dok").trim().replace(';','.'));		
			debt.setDuedate(rs.getDate("dat"));
			debt.setSum(Double.parseDouble(rs.getString("doksumma").trim()));
			//debt.setDue(rs.getString("yle"));
			String kl = rs.getString("cps_id");
			debt.setCustID(kl.trim());
			list.addElement(debt);
		}
		rs.close();
		stmt.close();
		return list;
	}
	
	  
	   static public String getDateStringYYYYmMMmDD(Date dat){
		   Calendar cal = Calendar.getInstance();
		   cal.setTime(dat);
		    StringBuffer sb = new StringBuffer();
		    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		    if (day.length() == 1){
		      sb.append("0");
		    }
		    sb.append(day + ".");
		    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
		    if (month.length() == 1){
		      sb.append("0");
		    }
		    sb.append(month + ".");

		   
		    sb.append(cal.get(Calendar.YEAR) );
		  

		    return sb.toString();
		  }
		   
	
	
	  public DealerProduct getDealerProductsList() {


	       DealerProduct dealerProds = new DealerProduct();
	       try {


	         String  sql = " SET DATEFORMAT dmy " +
" exec atsk_n_setfltr @nSkfId = 771, @nRcl = 1, @lNotIncTrn = 0, @lNShowNotRest = 0, @lNShowBSumRest = 0, @lNShowBDzRest = 0, " +
"@lNotShNTurnov = 0, @cGrpCol = '', @cGrpTbl = '', @dDat_b = '" + getDateStringYYYYmMMmDD(new Date())+ "'";

	            if (isDebug()) System.out.println(sql);
	            connectionUp(url);
	               
	               Statement stm = con.createStatement();
	               ResultSet rst = stm.executeQuery(sql);

	                 while (rst.next()) {
	                	    int seis = (int)Double.parseDouble(rst.getString("batl_daudz").replace(',','.'));
	     	                	
	                	 String toode = rst.getString("nrs_id").trim();
	                	 String ladu  =  rst.getString("nns_id").trim();
	                  
	                   dealerProds.add(ladu, toode,seis+""," "," " );
	                   //System.out.println(ladu+" "+toode+" "+seis+"");
	               }
	               rst.close();
	               stm.close();
	               con.close();

	       } catch (Exception e) {
	           getParent().showError(e);
	       }

	        return dealerProds;
	       // return null;
	      }


	
	
	public TreeMap getProducts(Agent agent){
		connectionUp(url);

		
		String hh = "select nrs_id as kood , cena as hind from NCS where ncv_id = 1 order by nrs_id ,dat desc";
		String cmd = "select  nrs.nrs_id AS toode,nrs.nos AS nimi, " +
" 0 AS myygihind, 0 AS tykke, 0 AS maxale,nrs.svkods as ean, nrs.npg_id AS grupp ,ctx.nod as vat  from nrs  inner join npg on npg.npg_id= nrs.npg_id " +
" left join ctx on ctx.ctx_id=npg.ctx_id where nrs.sct_id=20";
		
		if (isDebug()) {
			System.out.println(cmd);
		}
		TreeMap<String,Double> hinnad = new TreeMap<String,Double>();
		TreeMap products = new TreeMap();
		
		try {
			Statement st = con.createStatement();
			ResultSet	      rs = st.executeQuery(hh);
			
			while (rs.next()) {
				String kood = rs.getString("kood").trim();
				double hind = rs.getDouble("hind");
				hinnad.put(kood, hind);
			}
			rs.close();
				st.close();
			
			 st = con.createStatement();
				      rs = st.executeQuery(cmd);
			
			while (rs.next()) {
				String kood = rs.getString("toode").trim();
				String nimetus = rs.getString("nimi").trim();
				
				nimetus.replace('"',' ');
				Product prod = new Product(kood);
				prod.setName(Converter.enCodeEst(nimetus));
				prod.setPrice(parseDouble(rs.getString("myygihind").trim()));
				prod.setInCases(parseDouble(rs.getString("tykke").trim()));
				if(hinnad.get(kood) != null){
					prod.setPrice(hinnad.get(kood) );
				}
				
				//prod.setStock(parseDouble(rs.getString("stock").trim()));
				try{

					prod.setMaxDiscount(parseDouble(rs.getString("maxale").trim().replace(',','.')));
					//System.out.println(prod.getMaxDiscount());
				}catch (Exception e){
					
				}
				String ean = rs.getString("ean").trim();
				String grupp = rs.getString("grupp").trim();
				prod.setGroup(grupp);
				if (ean.length()>0){
					prod.setEAN(ean);
				}
				prod.setVat(rs.getString("vat"));
				products.put(nimetus, prod);
				
			}
			rs.close();
			st.close();
		} catch (Exception e) {
			getParent().showError(e);
		}
		
		return products;
	}
	
	/**
	 * Method declaration
	 *
	 *
	 * @param agentCode
	 *
	 * @return
	 *
	 * @see
	 */
	public Vector getCustomerProductsList(String agentCode){
		connectionUp(url);
		String cmd =
			// + '-' + ltrim(str(cpa_id_sv)) oli saaja juures
			" select ltrim(str(rrd.cps_id)) as saaja ,nrs_id as toode ,daudz as kogus ,datums as kuup ,cena as hind  from rrd  " +
			" INNER JOIN rrc on rrd.rrd_id=rrc.rrd_id where sct_id=20 " +
            " GROUP BY datums, rrd.cps_id , cpa_id_sv,nrs_id,daudz,cena,rrc.nos  ORDER BY rrd.cps_id,rrc.nos" ;
		
		
		if (isDebug())
			System.out.println(cmd);
		
		Vector custProds = new Vector();
		HashMap kliendid = new HashMap();
		
		try {
			
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);
			while (rs.next()) {
				String kliendikood = rs.getString("saaja").trim();
				String tootekood = rs.getString("toode").trim();
				double kogus = rs.getDouble("kogus");
				String kuup = rs.getDate("kuup").toString().substring(5,10);
				double hind = rs.getDouble("hind");
				Customer cust = (Customer)kliendid.get(kliendikood);
				if (cust == null){ cust = new Customer(kliendikood);}
				CustProd cp = new CustProd(kliendikood, tootekood, kogus, 0, 0,kuup,hind,0,0," ");
				Product prod = new Product(tootekood);
				cust.addProduct(prod, new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,kuup,hind,0,0," "),false);
				custProds.add(cp);
				kliendid.put(kliendikood, cust);
				//}
			}
			rs.close();
			st.close();
			con.close();
			
		}
		catch (Exception e) {
			getParent().showError(e);
		}
		
		return custProds;
		
	}
	
	
	
	 public String getPilotDayData(Agent agent){
	
	    return null;

	  }

	
	public String getNonMobileOrders(Agent agent){
		
			return null;
	}
	
	public double getSalary(String agentCode) {
		return 0;
	}
	
	
	public double getPromoSales(String agentCode, int nr) throws SQLException{		
		return 0;
	}
	
	
	public ManagerData getManagerData() throws SQLException{
		return null;
	}
	

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public String getStartingTime() {
		return null;
	}
	
	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public String getDriverVersion() {
		return DRIVER_VERSION;
	}
	
	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public String getSubjectFilesTo() {
		return this.subjectFilesTo;
	}
	
	/**
	 * Method declaration
	 *
	 *
	 * @param s
	 *
	 * @see
	 */
	public void log(String s) {
		getParent().log(s);
	}
	
	private static double parseDouble(String s){
		if (s.length() > 0) return Double.parseDouble(s);
		else return 0;
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
	
	// private String url = "jdbc:DBF:///c://work/avallone/dbf";
	public static final String DRIVER_VERSION = "Ozols";
	private String             url = "jdbc:odbc:ozols";
	private String             url2 = "jdbc:odbc:ozols";
	private Connection	       con = null;

	private PreparedStatement  stmt = null;
	private String	       subjectFilesTo = null;
	private NumberFormat NF = NumberFormat.getInstance();


	@Override
	public Vector getPriceMatrixData() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Vector getFixedAssortmentData() {
		
		  Vector lines = new Vector();
		
		  String cmd = " SELECT nrs_id as toode , cps_id as klient ,* FROM dbo._cdf_get_nrs_ids_by_cps()";


			        	 try {
			        		 
			        		 
			        		Statement  stmt = con.createStatement();
			                 ResultSet	      rs = stmt.executeQuery(cmd);
			                 while(rs.next()) {
			             
		                       String custID = " ";
		                       String prodID = " ";
		                       MatrixLine mline = new MatrixLine();
		                       
		                       prodID = rs.getString("toode").trim();
		                       custID =  rs.getString("klient").trim();
		                    
		                       if (custID != null
		                           && custID.trim().length() > 0) {
		                        // mline.setCliGroup(custID.trim());
		                         mline.setCustID(custID);
		                       }

		                       if (prodID != null && prodID.trim().length() > 0) {
		                         mline.setProdID(prodID.trim());
		                       }
		                       lines.add(mline);
			                 }
			                 rs.close();
			                 stmt.close();
			                // con.close();
			             } catch (Exception e) {
			                 getParent().showError(e);
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



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

