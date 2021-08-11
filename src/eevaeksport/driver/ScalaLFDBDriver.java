/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
public class ScalaLFDBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater,Discounter{
	

	
	public ScalaLFDBDriver(EksportPanel parent) {
		super(parent);
		this.subjectFilesTo = parent.getString("filemsg");
		// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
		String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
		
		try {
			Class.forName(databaseDriverName).newInstance();
			
		} catch (Exception e) {
			parent.showError(e);
		}
		 		
	    }

	
	   public Vector getPriceMatrixData()  {
	    	
	    	
      	   String    sql = "  SELECT SL01001 AS custid,'' AS clgroup,SUBSTRING(SC18002,11,35) AS prodid,'' AS prodgroup,SC18009, " + 
      	 " SC18008 AS price ,( case when SC18011 < getdate()+190 then SC18011 else null end) as lpaev , " +
		 " ( case when SC18011 < getdate()+190 then SC18010 else null end) as spaev " +
		 "  FROM SC180100 INNER JOIN SL010100 ON  left(SC18002,7)= SL01001 LEFT JOIN SY240100 on SC18009=SY24002 and SY24001='BG' WHERE " + 
         " SC18001='C' AND CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011    and SC18009 !='0' " +
         "     order by SL01001,SUBSTRING(SC18002,11,35)";

             Vector result = new Vector();
               try {
                
              	 connectionUp(url);
               Statement  st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql);

                 while (rs.next()) {

                   String custID = rs.getString("custid").trim();
                   String clGroup = rs.getString("clgroup");
                   String prodID = rs.getString("prodid").trim();
                   String prodGroup = rs.getString("prodgroup");           
                   String discount = rs.getString("SC18009");
                   double price = rs.getDouble("price");
                   String ldate = rs.getString("lpaev");
                   String sdate = rs.getString("spaev");
                   MatrixLine mline = new MatrixLine();

                   if (custID != null && custID.length() > 0) {
                	   mline.setCliGroup(custID.trim());
                	   mline.setDiscGroup(custID.trim());
                   }
                   if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                    // mline.setCliGroup(clGroup.trim());
                   }

                   
                   if(ldate != null){
                  	 mline.setEndDate(ldate.substring(0,10));
                   }
                   if(sdate != null){
                    	 mline.setStartDate(sdate.substring(0,10));
                     }
                   if (prodID != null && prodID.length() > 0) {
                     mline.setProdID(prodID);
                   }
                   if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                     mline.setProdGroup(prodGroup.trim());
                   }
                   if(discount.trim().length()==0){
                   	discount = "0";
                   }
                   double disc = 0; 
                   try{
                  	disc = Double.parseDouble(discount.replaceAll(",", "."));
                   }catch(Exception e){
                  	 e.printStackTrace();
                   }
                  // mline.setDiscount(disc);

                   
                   if(disc != 0){
                	   price = price*(100-disc)/100;
                	   
                   }
                   if (price != 0) {
                     mline.setPrice(price);
                   }
                   if(mline.getPrice() > 0 ){
                       result.add(mline);    
                   }
                   
                 }

                 
                 
                 // toote gruppide lisamine 
                 
                 
                 
                 
                 
                 String sql2 = "select '' AS custid ,SC39002 AS clgroup , rtrim(ltrim(SC39001))  AS prodid,' ' AS prodgroup, " +
                " case when SC39006 <= getdate() AND (SC39007 >= getdate() OR SC39007 = null) then  " +
                "  SC39005 else SC39004 end AS price   " +
                "  FROM SC390100 where (SC39004 > 0 or SC39005 > 0) and SC39002 not in ('00')  order by SC39002,SC39001 ";
                 
                 rs = st.executeQuery(sql2);

                 while (rs.next()) {

                   String clGroup = rs.getString("clgroup").trim();
                   String prodID = rs.getString("prodid").trim();
                   String discount = "0";       
     			   
                   double price = rs.getDouble("price");
                   MatrixLine mline = new MatrixLine();
                 
                   if (clGroup !=null && clGroup.trim().length() > 0) {
                     mline.setCliGroup(clGroup.trim());
                   }

                   if (prodID != null && prodID.trim().length() > 0) {
                     mline.setProdID(prodID.trim());
                   }
       
                   if (Double.parseDouble(discount) == 0 && price != 0) {
                     mline.setPrice(price);

                   }
                  result.add(mline);  // Kommentisin v�lja kuniks uus hinna skeem hakkab toimima
                 }
                 
                 String sql4 = "Select  SC08002 as custid,SC08003 as prodgr,SC08004 as discount  from SC080100 where" +
                 		"  CONVERT(char(8), getdate(),112)  BETWEEN  SC08005 AND SC08006 ";
                 rs = st.executeQuery(sql4);

                 while (rs.next()) {

                   String custID = rs.getString("custid").trim();
                   String prodGR = rs.getString("prodgr").trim();
                   double discount = 0;
                   try{
                	   
                   }catch(Exception e){
                	   discount = rs.getDouble("discount");       
         			   
                   }
                 
                   
                
                   MatrixLine mline = new MatrixLine();
                 
                   if (custID !=null && custID.trim().length() > 0) {
                     mline.setCliGroup(custID.trim());
                   }

                   if (prodGR != null && prodGR.trim().length() > 0) {
                     mline.setProdGroup(prodGR.trim());
                   }
       
                   if (discount != 0) {
                     mline.setDiscount(discount);
                   }
                  result.add(mline);  // Kommentisin v�lja kuniks uus hinna skeem hakkab toimima
                 }
                 
                 
            
                 
                 
      	
                 
                 
               }catch(Exception e){
              	 e.printStackTrace();
               }
      	
          return result;
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
	
	
	   public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
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
	                SuperVisorLineWrapper lw = new SuperVisorLineWrapper(iter.next());
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

	   
	   private TreeMap stocks = null;

	public String getSaveStockInfo(){
		String cmd = " SELECT  SC03001 AS kaup,SC03003 AS stock,SC03002 AS stockcode FROM SC030100 " +
		" WHERE  SC03001!='' AND SC03002!='' AND SC03003>1 ORDER BY  SC03003 ";
		
		if (isDebug()) {
			System.out.println(cmd);
		}
		 stocks = new TreeMap();
		StringBuffer products = new StringBuffer();
		try {
			Statement st = con.createStatement();
			ResultSet	      rs = st.executeQuery(cmd);
			while (rs.next()) {
				
				String kood = rs.getString("kaup").trim();
				double sto = rs.getDouble("stock");
				stocks.put(kood, sto + "");
				products.append(kood +  ";");
				products.append(" ;");
				products.append(getParent().getNF().format(sto));
				products.append("; ;");
				products.append(rs.getString("stockcode") +  "\n");
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
	         String cmd = "SELECT OR20019 AS agent,SUM(OR20024) AS sales FROM OR200100 WHERE MONTH(OR20015)=" +
	                      "MONTH(GETDATE()) AND YEAR(OR20015)=YEAR(GETDATE()) GROUP BY OR20019";
	         
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
		
		StringBuffer sb = new StringBuffer();
		
		try {
			connectionUp(url);
			Statement st = con.createStatement();
			String cmd =   "SELECT RTRIM(SC01001) AS kood,(RTRIM(SC01002)+' '+SC01003) AS nimetus " +
			"FROM SC010100 WHERE RTRIM(SC01018)='J�' ";
			
			
			if (isDebug())
				System.out.println(cmd);
			ResultSet rs = st.executeQuery(cmd);
			while(rs.next()){
				sb.append(rs.getString("kood") + ";" + rs.getString("nimetus") + "\n");
			}
			st.close();
			connectionDown();
		} catch (Exception e){}
		return sb.toString();
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
	    String    sql = " SELECT (case when LTRIM(substring (SC01039,7,4))='2060' then '2070' else substring (SC01039,7,4) end) AS liik , GL03003 AS nimi " +
		"	FROM SC010100 INNER JOIN SC030100 ON rtrim(ltrim(SC01001))=(SC03001) " +
		"	 inner join GL030113  gr on  rtrim(ltrim(GL03002))=rtrim(ltrim(substring (SC01039,7,4))) " + 
		"	  WHERE SC03002='01' AND SC01004 > 0 AND SC01019 = 'JA' GROUP BY substring (SC01039,7,4),GL03003 order by GL03003  " ;

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
	
	
	
	public TreeMap getAgents() {
		connectionUp(url);
		String  cmd = "SELECT SY24002 AS yksus,SY24003 AS nimi FROM SY240100 WHERE SY24001='BK' AND SY24002<'300'";
		TreeMap agents = new TreeMap();
		
		try {
			stmt = con.prepareStatement(cmd);
			ResultSet	      rs = stmt.executeQuery();
			
			while (rs.next()) {
				Agent agent = new Agent(rs.getString("yksus").trim());
				String nimi = rs.getString("nimi").trim();
				agent.setName(nimi);
				agent.setIsPilot(true);

				if (agent.getID().equals("102")) {
					agent.setObject("04");
				}
			
				if (agent.getID().equals("103") || agent.getID().equals("207")  || agent.getID().equals("106")) {
					agent.setObject("09");
				}
				
				if (agent.getID().equals("105")) {
					agent.setObject("02");
				}
				if (agent.getID().equals("108")) {
					agent.setObject("02");
				}
			
				
				if( agent.getID().equals("203") || agent.getID().equals("104") ||
						agent.getID().equals("204") || agent.getID().equals("205") ){
					agent.setObject("B001");
				}
				//agent.setShowmarketinfoall(true);
				agents.put(nimi, agent);
			}
		} catch (Exception e) {
			getParent().showError(e);
		}
		//   connectionDown();
		return agents;
	}
	
	

	 
	public TreeMap getCustomers(Agent agent){
		connectionUp(url);
		  Converter cc = new Converter();
		 HashMap kyssad =   cc.getQuestionaire(this.getExcelDir());
		String cmd = "SELECT ( ltrim(SL01002) + ' ' + SL14004) AS nimi,SL01001 AS kood,ltrim(SL01003) +' '+SL14005 AS aadress," +
				" (case when SY24002 = null then '0' " +
    " else SY24002 end) AS ale, " +
	"	    SL01076 AS distribution , SL01083 AS visit ,(case when SL01060=1 or SL01075=1 then 1 else 0 end)  AS blokk," +
    " (Select (case when SL01060=1 or SL01075=1 then 1 else 0 end) from SL010100 where SL01001 = k.SL01056 ) as blokk2  ,SL01035," +
    "	(case when len(SL01056)=0 then SL01001 else SL01056 end ) AS payer,SL01098 AS ware ,SL01026 AS auto , SL01035 AS agent " +
	"  ,(SELECT SL01037 from SL010100 where SL01001=k.SL01056) as limiit," +
	" (case when k.SL01010 like 'AKT%' and  k.SL01060 = '2' then 'MANUALI BLOKETS' else '' end) as blinfo ,SL01056 as klgrupp FROM SL010100 k LEFT JOIN SY240100 ON SL01036=SY24002 LEFT JOIN  SL140100 abi ON SL01001=SL14001 " +
	"  WHERE SL01002 != ''  and SL01010 != 'NEAK'  ORDER BY SL01002,SY24001";
		log(cmd);
		TreeMap customers = new TreeMap();
		HashMap custMap = new HashMap();
		HashMap comments = new HashMap();
		
		try {
			
			
			String sql = "Select comment_item as id ,convert(varchar,comment_date,104)+' '+comment_text as comm from intra.dbo.comments comm where comment_table='SL01' and comment_id=(select MAX(comment_id)" +
					" from intra.dbo.comments lastcomm where lastcomm.comment_private=0 and lastcomm.comment_item=comm.comment_item)  ";
			
			stmt = con.prepareStatement(sql);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				String id = rs.getString("id").trim();
				
			String comm = rs.getString("comm").trim();
			
				comments.put(id,comm);
			}
			rs.close();
			stmt.close();
			
			
			
			 sql = "Select comment_item as id ,convert(varchar,comment_date,104)+' '+comment_text as comm from intra.dbo.comments comm where comment_table='SL01' and comment_id=(select MAX(comment_id)" +
			" from intra.dbo.comments lastcomm where lastcomm.comment_private=0 and lastcomm.comment_item=comm.comment_item)  ";
	
	stmt = con.prepareStatement(sql);
	
	 rs = stmt.executeQuery();
	while(rs.next()){
		String id = rs.getString("id").trim();
		
	String comm = rs.getString("comm").trim();
	
		comments.put(id,comm);
	}
	rs.close();
	stmt.close();
	
	HashMap<String,Double> used = new HashMap<String,Double>();
	   String sql7 = "SELECT SL01001 AS id,SUM(SL03013-SL03053) AS debt FROM SL030100 INNER JOIN SL010100 ON SL03001 = SL01001  WHERE  " +
	   " NOT(SL03013=SL03053)  and  " +
	   " SL03001 not in (326860,326861,324640,324871 ,326865, 326866,325209,326903 ,322577  ,316433 ,323503    " +
	    " ,313419,321221,323503 ,320971 ,312780,324644,324139,324102,324127 ,310282 ,323991 ,322786 ,323256 )" +
	    " group by SL01001" ;

				Statement stmr = con.createStatement();
				
				ResultSet rs4 = stmr.executeQuery(sql7);
				while(rs4.next()){  

				 used.put(rs4.getString("id").trim(), rs4.getDouble("debt"));
				}
				
				rs4.close();
				stmr.close();
	
			
			
		 sql = "SELECT (case when len(SL01056)=0 then SL03001 else SL01056 end ) AS id,SUM(SL03013-SL03053) AS debt FROM SL030100 " +
			" INNER JOIN SL010100 ON SL03001=SL01001 WHERE  " +
			" NOT(SL03013=SL03053) AND SL03006<=getdate() AND " +
			" SL03001 not in (326860,326861,324640,324871 ,326865, 326866,325209,326903 ,322577  ,316433 ,323503    " +
		    " ,313419,321221,323503 ,320971 ,312780,324644,324139,324102,324127 ,310282 ,323991 ,322786 ,323256 )" +
	        " GROUP BY (case when len(SL01056)=0 then SL03001 else SL01056 end )";
			
			stmt = con.prepareStatement(sql);
			
		    rs = stmt.executeQuery();
			while(rs.next()){
				String id = rs.getString("id").trim();
				double debt = Double.parseDouble(rs.getString("debt").trim());
				Customer cust = (Customer)custMap.get(id);
				if(cust == null){
					cust = new Customer(id);
				}
				cust.setDebt(cust.getDebt()+debt);
				custMap.put(id,cust);
			}
			rs.close();
			stmt.close();
			
			
			
		
			
			
			Connection con2 = DriverManager.getConnection(url2,"mobile","mobile123");
			
			stmt = con.prepareStatement(cmd);
			rs = stmt.executeQuery();
			while (rs.next()) {
				String nimi = rs.getString("nimi");
				if(nimi != null){
				
				Customer cust = new Customer(rs.getString("kood").trim());
				cust.setName(Converter.enCodeEst(nimi.replace(',',' ')));
				cust.setAddress(Converter.adressLink(Converter.enCodeEst(rs.getString("aadress").trim().replace(',',' ')),"Latvia"));
				//String ale = rs.getString("ale");
				//if (ale != null && ale.trim().length() > 0){
					//ale.replace('%',' ');
				//	cust.setDiscount(Double.parseDouble(ale.trim()));
				//}
				String dist = rs.getString("distribution") ;
				cust.setVisit(rs.getString("visit").trim());
				
				if (dist != null && dist.trim().equals("1")){
					cust.setDistUpTo(dist);
				}
				cust.setShowmarketinfo(true);
				//(case when SL01075='0' AND SL01060='0' then 0 else 1 end) AS blokk
				String  blokk = rs.getString("blokk") ;
				if(blokk.trim().equals("1")){
					cust.setSalesBlock(true);
				}
				String  blokk2 = rs.getString("blokk2") ;
				if(blokk2 != null && blokk2.trim().equals("1")){
					cust.setSalesBlock(true);
					//System.out.println("Blokk 2 " + cust.getName());
				}
				
				 cc.addQuestiosToCustomer(cust,kyssad);
				String payer = rs.getString("payer");
				if(payer != null && payer.trim().length() > 0){
					cust.setPayer(payer.trim());
				}
				
				String ware =  rs.getString("ware");
				if(ware != null && ware.trim().length() > 0){			
					cust.setWare(addSpaces(ware.trim(),4));
				}
				String car =  rs.getString("auto");
				if(car != null && car.trim().length() > 0){			
					cust.setWare(cust.getWare()+car.trim());
				}
				
				
				String ag = rs.getString("agent");
				if(ag != null && ag.trim().length() > 0){			
					cust.addAgent(new Agent (ag));
				}
				
				String limiit = rs.getString("limiit");
	             if(limiit != null && limiit.trim().length()>0 && limiit.trim().length()<8){
	               	cust.setCredit(limiit.trim());              	
	             }
	                
	             String blinfo = rs.getString("blinfo");
	             if(blinfo != null && blinfo.trim().length()>0){
	               	cust.setCity(blinfo.trim());              	
	             }

	                String type  =  rs.getString("klgrupp");
	                
	                if(type != null && type.trim().length()>0){
	                	  cust.setCustType(type);
	                }
	                
				
				if(comments.get(cust.getID()) != null){
					cust.setInfo(Converter.enCodeEst(((String)comments.get(cust.getID())).replaceAll("\n", " ")));
				}
				
				if(used.get(cust.getPayer()) != null){
					cust.setUsedCredit(used.get(cust.getPayer()));
				}
				
				
				
				Customer custo = (Customer)custMap.get(cust.getPayer());
						if(custo != null){
							cust.setDebt(custo.getDebt());
						}
						
				
						
						
				customers.put(cust.getName(), cust);
				custMap.put(cust.getID(), cust);
				
				}
				con2.close();
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
		String sql = "SELECT SL03087 as kl,SL03001 AS klient,  ltrim (SL03024)+  SL03002   AS number,SL03006 AS kuupaev,(SL03013-SL03053) AS summa, datediff(day,SL03006,getdate()) " +
			"	AS yle,SL01002 FROM SL030100 INNER JOIN SL010100 on SL01001=SL03001 WHERE NOT(SL03013=SL03053) AND " +
	"	LEN(LTRIM(SL03013))>0 AND SL03006<GETDATE() and SL03001 not in (326860,326861,324640,324871 ,326865, 326866,325209,326903,322577,316433,323503  " +
    ",313419,321221,323503 ,320971 ,312780,324644,324139,324102,324127 ,310282 ,323991 ,322786 ,323256 " +
    " )  ORDER BY SL03087";
		
		connectionUp(url);
		stmt = con.prepareStatement(sql);
	
		ResultSet rs = stmt.executeQuery();
		
		while(rs.next()){
			Debt debt = new Debt();
			String kl = rs.getString("kl");
			if(kl == null || kl.trim().length() > 0){
				kl = rs.getString("klient");
			}
			debt.setCustID(kl.trim());
			debt.setDoknr(rs.getString("number").trim().replace(';','.'));		
			debt.setDuedate(rs.getDate("kuupaev"));
			debt.setSum(Double.parseDouble(rs.getString("summa").trim()));
			debt.setDue(rs.getString("yle"));
			list.addElement(debt);
		}
		rs.close();
		stmt.close();
		return list;
	}
	
	
	
	
	  public DealerProduct getDealerProductsList() {


	       DealerProduct dealerProds = new DealerProduct();
	       try {


	         String  sql = " SELECT SC03001 as toode,SC03003 AS stock ,SC03002 as ladu " +
	         		"from SC030100 where SC03003 !=0  order by SC03001" ;

	            if (isDebug()) System.out.println(sql);
	            connectionUp(url);
	               
	               Statement stm = con.createStatement();
	               ResultSet rst = stm.executeQuery(sql);

	                 while (rst.next()) {
	                   String toode = rst.getString("toode").trim();
	                   int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
	                   String ladu  =  rst.getString("ladu").trim();
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
		String cmd =
"SELECT RTRIM(SC01001) AS toode,(case when substring(SC01039,7,4)='2060' then '2070' else " +
" substring(SC01039,7,4) end)+' '+RTRIM(SC01002)+' '+SC01003 AS nimi, SC01004 AS myygihind," +
" SC01071 AS tykke,SC01093 AS maxale,SC01132 as ean," +
" (case when substring(SC01039,7,4)='2060' then '2070' else substring(SC01039,7,4) end) AS grupp " +
" FROM SC010100 WHERE SC01004>0 AND SC01019='JA'" +
" ORDER BY (case when substring(SC01039,7,4)='2060' then '2070' else substring(SC01039,7,4) end)";
		
		if (isDebug()) {
			System.out.println(cmd);
		}
		getSaveStockInfo();
		TreeMap products = new TreeMap();
		
		try {
			Statement st = con.createStatement();
			ResultSet	      rs = st.executeQuery(cmd);
			
			while (rs.next()) {
				String kood = rs.getString("toode").trim();
				String nimetus = rs.getString("nimi").trim();
				
				nimetus.replace('"',' ');
				Product prod = new Product(kood);
				prod.setName(Converter.enCodeEst(nimetus));
				prod.setPrice(parseDouble(rs.getString("myygihind").trim()));
				prod.setInCases(parseDouble(rs.getString("tykke").trim()));
				
				if(stocks.get(kood) != null){
					prod.setStock(parseDouble(stocks.get(kood)+""));
				}
				
				
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
				prod.setVat("21");
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
			
			" SELECT OR20003 AS saaja , OR21005 AS toode, OR21011 AS kogus ,OR20015 AS kuup FROM OR200100 " +
			" INNER JOIN OR210100 ON OR20001=OR21001 INNER JOIN  SL010100 ON SL01001=OR20003 " +
			"INNER JOIN  SC010100 ON OR21005=SC01001 INNER JOIN SC030100 ON SC01001=SC03001 " +
			" WHERE OR20015>getdate()-120  AND LEN(OR20019)>0  " +
			" GROUP BY OR21005,OR20003,OR21011,OR20015 , (LEFT(LTRIM(SC01039),4)+' '+RTRIM(SC01002)+' '+SC01003)" +
			" ORDER BY (LEFT(LTRIM(SC01039),4)+' '+RTRIM(SC01002)+' '+SC01003), OR21005,OR20003,OR20015 asc " ;
		
		
		String cmd1 =
			"SELECT OR20003 AS saaja , OR21005 AS toode, SUM(OR21011) AS plusskogus FROM OR200100 " +
			"INNER JOIN OR210100 ON OR20001=OR21001 INNER JOIN  SL010100 ON SL01001=OR20003  " +
			"INNER JOIN  SC010100 ON OR21005=SC01001 INNER JOIN SC030100 ON SC01001=SC03001 " +
			"WHERE OR20015>getdate()-30  AND LEN(OR20019)>0 AND OR21011>0 " +
			"GROUP BY OR21005,OR20003,(LEFT(LTRIM(SC01039),4)+' '+RTRIM(SC01002)+' '+SC01003) ORDER BY (LEFT(LTRIM(SC01039),4)+' '+RTRIM(SC01002)+' '+SC01003), OR21005,OR20003 asc ";
		
		
		String cmd2 =
			"SELECT OR20003 AS saaja , OR21005 AS toode, SUM(OR21011) AS miinuskogus FROM OR200100 " +
			"INNER JOIN OR210100 ON OR20001=OR21001  INNER JOIN  SL010100 ON SL01001=OR20003 " +
			" INNER JOIN  SC010100 ON OR21005=SC01001 INNER JOIN SC030100 ON SC01001=SC03001" +
			" WHERE OR20015>getdate()-30  AND LEN(OR20019)>0 AND OR21011<0 " +
			"GROUP BY OR21005,OR20003,(LEFT(LTRIM(SC01039),4)+' '+RTRIM(SC01002)+' '+SC01003) ORDER BY (LEFT(LTRIM(SC01039),4)+' '+RTRIM(SC01002)+' '+SC01003),OR21005,OR20003 asc ";
		
		
		if (isDebug())
			System.out.println(cmd);
		if (isDebug())
			System.out.println(cmd1);
		if (isDebug())
			System.out.println(cmd2);
		
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
				Customer cust = (Customer)kliendid.get(kliendikood);
				if (cust == null){ cust = new Customer(kliendikood);}
				CustProd cp = new CustProd(kliendikood, tootekood, kogus, 0, 0,kuup,0,0,0," ");
				Product prod = new Product(tootekood);
				cust.addProduct(prod, new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,kuup,0,0,0," "),false);
				custProds.add(cp);
				kliendid.put(kliendikood, cust);
				//}
			}
			rs.close();
			ResultSet rs1 = st.executeQuery(cmd1);
			while(rs1.next()){
				String klient = rs1.getString("saaja").trim();
				String toode = rs1.getString("toode").trim();
				double pkogus = rs1.getDouble("plusskogus");
				Customer c = (Customer)kliendid.get(klient);
				if (c != null){
					ArrayList al = (ArrayList)c.getProducts().get(toode);
					if (al != null){
						CustProd cp = (CustProd)al.get(1);
						if (cp != null){
							cp.setMovedStockPluss(pkogus);
							custProds.add(cp);
							//System.out.println(cp.toString());
						}
					}
				}
			}
			rs1.close();
			
			ResultSet rs2 = st.executeQuery(cmd2);
			while(rs2.next()){
				String klient = rs2.getString("saaja").trim();
				String toode = rs2.getString("toode").trim();
				double mkogus = rs2.getDouble("miinuskogus");
				Customer c = (Customer)kliendid.get(klient);
				if (c != null){
					ArrayList al = (ArrayList)c.getProducts().get(toode);
					if (al != null){
						CustProd cp = (CustProd)al.get(1);
						if (cp != null){
							cp.setMovedStockMinus(mkogus);
							custProds.add(cp);
						}
					}
				}
			}
			
			
			rs2.close();
			st.close();
			con.close();
			
		}
		catch (Exception e) {
			getParent().showError(e);
		}
		
		return custProds;
		
	}
	
	
	
	 public String getPilotDayData(Agent agent){
	    connectionUp();
	    StringBuffer sb = new StringBuffer();

	    sb.append( agent.getName() + "\n");
	    sb.append( "Sales By BudgetGroups\n\n Group     Plan    Done\n\n");
	  
	    try {
	      TreeMap groups = new TreeMap();
	   groups.put("Snack Kopa"," 100000<=OR21005 AND OR21005<=299999");
	   TreeMap gr = this.getProdGroups();
	   Iterator ite = gr.keySet().iterator();
	   while( ite.hasNext()) {
		   String liik = (String)ite.next();
		   String value = (String)gr.get(liik);
		   groups.put("    " + value," ltrim(SC01039) =  '" + liik + "'" );   
	   }
	   
	   
	   
	   groups.put("Leaf Kopa"," 430000<=OR21005 AND OR21005<=439999");
	  // groups.put("Okartes"," 455000<=OR21005 AND OR21005<=455999");
	   Iterator iter = groups.keySet().iterator();
	      while( iter.hasNext()) {
	   String name = (String)iter.next();
	   String value = (String)groups.get(name);
	   
	      String sql =  "SELECT   sum(OR21008*OR21011*(100-OR21010)/100) AS summa FROM OR200100 " +
			" INNER JOIN OR210100 ON OR20001=OR21001 INNER JOIN  SL010100 ON SL01001=OR20003 " +
			"INNER JOIN SC010100 ON SC01001=OR21005 " + 
			" WHERE YEAR(OR20015)=YEAR(getdate()) AND MONTH(OR20015)=MONTH(getdate()) " +
			" AND LEN(OR20019)>0 AND SL01035=? AND " + value ;
	      System.out.println(sql + agent.getID());
	      connectionUp();
	      PreparedStatement pst = con.prepareStatement(sql);
	    
	      pst.setString(1, agent.getID());

	     ResultSet rs = pst.executeQuery();
          while (rs.next()) {
	        sb.append( name + "\n\t " +
	                   "         " +
	                  rs.getDouble("summa") + "\n");
	       }
	      }
	     } catch (Exception e){
	        e.printStackTrace();	
	    }
	    
	//System.out.println(sb.toString());

	    return sb.toString();

	  }

	
	public String getNonMobileOrders(Agent agent){
		connectionUp(url);
		
		/*String sql = "SELECT \"Order value\" AS summa,(RTRIM(\"sl01002\")+' '+(\"Customer name\")) AS Nimi," +
		 " i.\"order Number\" AS number FROM st02 i INNER JOIN or20 o ON " +
		 " o.\"Order Number\"=i.\"Order Number\" INNER JOIN sl14 k ON " +
		 " i.\"Customer Code\"=k.\"Customer Code\" INNER JOIN sl01 l ON \"sl01001\"= " +
		 " k.\"Customer Code\" WHERE \"actual del  date\"=? AND  length(\"Shipment Mark\")=0 ";
		 
		String sql = "SELECT ST02016 AS kuupaev,RTRIM(SL01002)+ ' ' + RTRIM(SL14004) AS nimi, ST02009 AS number, OR20024 AS summa  FROM ST020100 " +
		" LEFT JOIN OR200100 ON  OR20001=ST02009 LEFT JOIN SL140100 k ON ST02001=SL14001 " +
		" LEFT JOIN SL010100 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-3 AND ST02007=? " +
		" AND LEN(RTRIM(OR20056))=0 GROUP BY ST02009,OR20024,SL01002,SL14004,SL14003,ST02016 " +
		" ORDER BY ST02016 desc,ST02009,OR20024,SL01002,SL14004,SL14003";
		
		StringBuffer sb = new StringBuffer();
		try {
			stmt = con.prepareStatement(sql);
			stmt.setString(1, agent.getID());
			String sep = System.getProperty("line.separator");
			ResultSet rs = stmt.executeQuery();
			boolean first = true;
			while(rs.next()){
				if(first){
					sb.append(getParent().getString("name") + "  \t\t" + getParent().getString("number") +
							"\t" + getParent().getString("sum") + sep);
					sb.append("-------------------------------------------" + sep);
								first = false;		
				}
                Date kuup = rs.getDate("kuupaev");
    		
				
				sb.append(rs.getString("nimi").trim() + "\n");
				sb.append( "Datu: " + kuup.toString().substring(0,12)  + " " + rs.getString("number").trim() + "\t");
				String sum = getNF().format(Double.parseDouble(rs.getString("summa").trim())) ;
				sb.append(sum + sep);
			}
		} catch (Exception e) {
			getParent().showError(e);
		}
		
		connectionDown();
		return sb.toString();*/
		return null;
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
	public double getSalary(String agentCode) {
		if (agentCode == null) return 0;
		connectionUp(url);
		String cmd = "SELECT SUM(OR20024) AS sales FROM OR200100 WHERE MONTH(OR20015)=" +
		"MONTH(GETDATE()) AND YEAR(OR20015)=YEAR(GETDATE()) AND OR20019=?";
		log(cmd);
		
		try {
			stmt = con.prepareStatement(cmd);
			stmt.setString(1, agentCode);
			
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_MONTH, 1);
			
			ResultSet	      rs = stmt.executeQuery();
			
			while(rs.next()) {
				setSales(rs.getDouble("sales"));
			}
			rs.close();
			stmt.close();
			
		} catch (Exception e) {
			getParent().showError(e);
		}
		
		return 0;
	}
	
	
	public double getPromoSales(String agentCode, int nr) throws SQLException{
		String sql = "SELECT ST02020 AS  kogus, SC01071 AS sisu FROM ST020100 INNER JOIN " +
		"SC010100 ON ST02017=SC01001 WHERE ST02007=?" +
		" AND YEAR(ST02015)=YEAR(GETDATE()) AND MONTH(ST02015)=MONTH(GETDATE()) AND SC01012=?";
		PreparedStatement pst = con.prepareStatement(sql);
		pst.setString(1, agentCode);
		//Calendar cal = Calendar.getInstance();
		//cal.set(Calendar.DAY_OF_MONTH, 1);
		//pst.setTimestamp(2, new Timestamp(cal.getTime().getTime()));
		pst.setString(2, String.valueOf(nr));
		ResultSet rs = pst.executeQuery();
		double total = 0;
		while(rs.next()){
			double kogus = rs.getDouble("kogus");
			String sisu = rs.getString("sisu");
			if (!sisu.equals("")){
				total += kogus*Double.parseDouble(sisu);
			}
		}
		rs.close();
		pst.close();
		//System.out.println("nr:" + nr + ", total:" + total);
		return total;
	}
	
	
	public ManagerData getManagerData() throws SQLException{
		String sql = "SELECT clid AS klient,docsum AS summa,linecount AS ridu,text AS tekst," +
		"sentlong AS kuupaev FROM dayreport WHERE sentdate>(DATE()-30) ORDER BY clid";
		Connection c = DriverManager.getConnection(url2);
		Statement st = c.createStatement();
		ResultSet rs = st.executeQuery(sql);
		
		
		String sql2 = "SELECT SL01001 AS kood, SL01040 AS aastamyyk,SL01041 AS kuumyyk,SY24003 AS agendinimi, SL01006 AS kontaktisik " +
		",SL01011 AS telefon, SL01024 AS maksetin,SL01036 AS ale" +
		" FROM SL010100 LEFT JOIN SY240100 ON SY24002=SL01035 ORDER BY " +
		"SL01002";
		connectionUp();
		Statement st2 = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs2 = st2.executeQuery(sql2);
		
		StringBuffer sb = new StringBuffer();
		String sep = ";";
		while(rs.next()){
			String cliID = rs.getString("klient").trim();
			sb.append(cliID + sep);
			sb.append(rs.getString("kuupaev").trim() + sep);
			sb.append(rs.getDouble("summa") + sep);
			sb.append(rs.getInt("ridu") + sep);
			String tekst = ensureLen(rs.getString("tekst")," ");
			tekst = tekst.replace('\'', ' ').replace('\"', ' ').replace(';', ' ');
			if (tekst == null) tekst = " ";
			
			sb.append(tekst + sep);
			
			boolean has = scrollTo(cliID, rs2);
			if (has){
				sb.append(ensureLen(rs2.getString("aastamyyk"), "0") + sep);
				sb.append(ensureLen(rs2.getString("kuumyyk"), "0") + sep);
				sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
				sb.append(ensureLen(rs2.getString("kontaktisik"), " ") + sep);
				String tel =  rs2.getString("telefon") ;
				tel.replace('\'', ' ').replace('\"', ' ').replace(';',' ');
				sb.append(ensureLen(tel, " ") + sep);
			//	System.out.println(ensureLen(tel, " "));
				sb.append(ensureLen(rs2.getString("maksetin"), " ") + sep);
				sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
			}
			
			sb.append("\n");
		}
		
		ManagerData md = new ManagerData();
		md.setClientVisitInfo(sb.toString());
		
		sql = "SELECT OR20003 AS klient,OR20019 AS agent,OR20017 AS agentname FROM OR200100 " +
		"WHERE OR20015>? GROUP BY OR20003,OR20019,OR20017 ORDER BY OR20003";
		
		PreparedStatement ps = con.prepareStatement(sql);
		ps.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
		rs = ps.executeQuery();
		StringBuffer bf2 = new StringBuffer();
		while(rs.next()){
			bf2.append(rs.getString("klient").trim() + sep);
			bf2.append(rs.getString("agent").trim() + sep);
			String agentName = rs.getString("agentname");
			if (agentName != null) agentName = agentName.trim();
			if (agentName == null) agentName = " ";
			bf2.append(agentName + "\n");
		}
		
		md.setClientAgentInfo(bf2.toString());
		
		return md;
	}
	
	private boolean scrollTo(String id, ResultSet rs) throws SQLException{
		rs.beforeFirst();
		while(rs.next()){
			if (id.equals(rs.getString("kood").trim())){
				return true;
			}
		}
		return false;
	}
	
	private String ensureLen(String str, String replace){
		if (str == null) str = "";
		else str = str.trim().replace('\'', ' ').replace('\"', ' ').replace(';',' ');
		if (str.length() == 0){
			return replace;
		} else {
			return str;
			
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
	public static final String DRIVER_VERSION = "ScalaLF";
	private String             url = "jdbc:odbc:scalaDB";
	private String             url2 = "jdbc:odbc:report";
	private Connection	       con = null;

	private PreparedStatement  stmt = null;
	private String	       subjectFilesTo = null;
	private NumberFormat NF = NumberFormat.getInstance();

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



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

