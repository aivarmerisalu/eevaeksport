/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DistEvalLineWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Dealer;
import eevaeksport.DealerProduct;
import eevaeksport.EksportPanel;
import eevaeksport.PlansBonuses;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.ClientBlock;
import eevaeksport.report.SalesSetter;
import eevaeksport.report.StockSetter;
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
public class VSuusDBDriver extends AbstractDBDriver implements Discounter, Manager, ClientBlock, StockSetter,SalesSetter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public VSuusDBDriver(EksportPanel parent) {
      super(parent);
        if(parent.getProps().getProperty("language").equals("Russian")){
        this.nimetu = "rusnimetus";
        }


        if(parent.getProps().getProperty("planbonusmeasure") != null &&
           parent.getProps().getProperty("planbonusmeasure").equals("money") ){
        this.kordaja = "plaanhind";
        }

        this.subjectFilesTo = parent.getString("filemsg");
        if (parent.getProps().getProperty("dburl") != null &&
                         parent.getProps().getProperty("dburl").trim().length()>0 ){
                       this.url = parent.getProps().getProperty("dburl").trim();
                     }
        if (parent.getProps().getProperty("report.dburl") != null &&
                        parent.getProps().getProperty("report.dburl").trim().length()>0 ){
                       this.url2=parent.getProps().getProperty("report.dburl").trim();

                     }

	// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();
	    con3 = DriverManager.getConnection(url3);
	} catch (Exception e) {
	    parent.showError(e);
	}
    }

    public String getProductSortingData(){
       	return null;
       }

    public void setCrmData(String agentID,String agentName,String custID,String custName,String tekst,java.sql.Date date) throws SQLException {
        
    	
 	   
    }
    
        public String getSaveStockInfo(){
        return null;
        }
        public Vector getPriceMatrixData()  {
           
        return null;
        }
    	public Vector getLastThreeData() {
    		// TODO Auto-generated method stub
    		return null;
    	}
        public String getAssortmentStr(Agent agent){
        	return null;
        }

        public Vector getFixedAssortmentData() {
            return null;
          }
        public String getSaveDebtInfo() {
           	return null;
           }
           

    /**
     *
     */
    public void connectionUp() {

	if (con == null) {
	    try {
		con = DriverManager.getConnection(url);
		stmt = con.createStatement();
	    } catch (Exception e) {
		getParent().showError(e);
	    }
	}
    }

    /**
     *
     */
    public void connectionDown() {
	try {
	    stmt.close();
	    con.close();

	    con = null;
	} catch (Exception e) {
	    getParent().showError(e);
	}
    }
    public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
        String sql = "INSERT into supervisor values ("
          + "?, "   //   agent	C(50),
          + "?,"   //   agentname	C(200),
          + "?,"   //   clientid	C(50),
          + "?,"   //   clientname	C(200),
          + "?,"   //	prodid	C(50),
          + "?,"   //   prodname	C(200),agentid agentname custid custname prodid prodname
          + "date() ,"   //	sentdate	D,
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

          try {	Connection c = DriverManager.getConnection(url2);
              PreparedStatement ps = c.prepareStatement(sql);

              for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
                SuperVisorLineWrapper lw = new SuperVisorLineWrapper(iter.next());
                ps.setString(1, wr.getInfo().getAgentID());
                ps.setString(2, wr.getInfo().getAgentName());
                ps.setString(3, lw.getLine().getCustID());
                ps.setString(4, lw.getLine().getCustName());
                ps.setString(5, lw.getLine().getProdID());
                ps.setString(6, lw.getLine().getProdName());
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
                ps.setString(25, lw.getLine().getComment());
                ps.setString(26, lw.getLine().getCustAgentId());
                ps.setString(27, lw.getLine().getCustAgentName());
                ps.setInt(28, lw.getLine().getDealNr());
                ps.setInt(29, lw.getLine().getIdealNr());
                ps.setString(30, lw.getLine().getResStr1());
                ps.setString(31, lw.getLine().getResStr2());
                ps.setString(32, lw.getLine().getResStr3());
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
          + "date() ,"   //	sentdate	D,
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

          try {	Connection c = DriverManager.getConnection(url2);
              PreparedStatement ps = c.prepareStatement(sql);

              for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
                eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
                ps.setString(1, wr.getInfo().getAgentID());
                ps.setString(2, wr.getInfo().getAgentName());
                ps.setString(3, lw.getLine().getCustID());
                ps.setString(4, lw.getLine().getCustName());
                ps.setString(5, lw.getLine().getProdID());
                ps.setString(6, lw.getLine().getProdName());
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
                ps.setString(25, lw.getLine().getComment());
                ps.setString(26, lw.getLine().getCustAgentId());
                ps.setString(27, lw.getLine().getCustAgentName());
                ps.setInt(28, lw.getLine().getDealNr());
                ps.setInt(29, lw.getLine().getIdealNr());
                ps.setString(30, lw.getLine().getResStr1());
                ps.setString(31, lw.getLine().getResStr2());
                ps.setString(32, lw.getLine().getResStr3());
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

                                       public String getCustProdDiscounts(Agent agent){
                                        return null;
                                        }





             public String getDistribution() {
      connectionUp();
      StringBuffer sb = new StringBuffer();

      try {
        String cmd = "SELECT toode,left (artlnimi,3)+ ' ' + nimetus AS nimi FROM toode t INNER JOIN  ../andmed/artliik l on" +
        		" t.liik=l.liik  WHERE pangateen=.T. and l.grupp='L' ORDER BY artlnimi, nimetus";


        PreparedStatement st = con.prepareStatement(cmd);
        ResultSet rs = st.executeQuery();

                 while (rs.next()) {

          sb.append(rs.getString("toode").trim() + ";" + rs.getString("nimi").trim() + "\n");
        }
        st.close();

      } catch (Exception e) {}
      System.out.println(sb.toString());
      return sb.toString();

        }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public TreeMap getAgents() {
	connectionUp();

	/*try {
	    getPlansBonuses("2");
	} catch (SQLException e) {
	    e.printStackTrace();
	}*/

	// String  cmd = "SELECT yksus,nimetus,objekt FROM yksus WHERE klassif=2";
	String  cmd = "SELECT yksus,nimetus, 2 AS objekt, klassif ,agentcode FROM yksus order by nimetus";
	TreeMap agents = new TreeMap();

	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		int klassif = rs.getInt("klassif");
                String agentstr = rs.getString("agentcode").trim();

		if (klassif == 2) {
		    Agent agent = new Agent(rs.getString("yksus").trim());

		    agent.setObject(rs.getString("objekt").trim());

		    String name = rs.getString("nimetus").trim();

		    agent.setName(name);
                    agent.setIsPilot(true);
		    agents.put(name, agent);
		} else if (klassif == 1 && agentstr.length() == 0) {
		    Dealer dealer = new Dealer(rs.getString("yksus").trim());

		    dealer.setName(Converter.enCodeEst(rs.getString("nimetus").trim()));
		    addDealer(dealer);
		}
	    }
	} catch (Exception e) {
	    getParent().showError(e);
	}

	connectionDown();

	return agents;
    }

    public Vector getDebt(String agentCode){
      return null;
    }

    public void blockClients(HashMap clients) throws SQLException{
      connectionUp();
      Iterator iter = clients.keySet().iterator();
      String dlr = (String)iter.next();
      String sql = "UPDATE klient SET blokk" + dlr + "='',debtdate" + dlr + "='' ";
      Statement st = con.createStatement();
      st.executeUpdate(sql);
      st.close();

      sql = "UPDATE klient SET blokk" + dlr + "=?,debtdate" + dlr + "=? WHERE kood" + dlr + "=?";
      PreparedStatement pst = con.prepareStatement(sql);
      iter = clients.keySet().iterator();
      while(iter.hasNext()){
        String dealer = (String)iter.next();
        pst.setString(1, dealer);
        pst.setString(2, getStartingTime());
        ArrayList al = (ArrayList)clients.get(dealer);
        Iterator alIter = al.iterator();
        while(alIter.hasNext()){
          pst.setString(3, (String)alIter.next());
          pst.executeUpdate();
        }
      }
      pst.close();
    }






    public void setSales(HashMap custprods) throws SQLException{
      connectionUp();
      Iterator iter = custprods.keySet().iterator();
      String dlr = (String)iter.next();
      String sql =" INSERT INTO rid VALUES('T',99999, '', ?, ?, ?,?,'K','',0,'',?,?,'', " +
           "?,MONTH(date()-20),YEAR(date()-20),0,0,? ,'','','',0,0,'','','',0,0,.F.,.F.,0,'',0)" ;

       String sql1 = "SELECT kood FROM KLIENT WHERE kood" + dlr + "= ? OR smartsaaja= ?";
       String sql2 = "SELECT toode FROM toode WHERE kood" + dlr + "= ?  OR ribakood= ?";

       PreparedStatement pst = con.prepareStatement(sql);
       iter = custprods.keySet().iterator();
       while (iter.hasNext()) {
         String dealer = (String) iter.next();
         ArrayList al = (ArrayList) custprods.get(dealer);

         for (int i = 0; i < al.size(); i++) {

           String owncustid = "MUUKLIENT";
           String ownprodid = "11111111";

           CustProd p = (CustProd) al.get(i);
           String custid = p.getCustID();
           String prodid = p.getProdID();
           Date daatum = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
           if(p.getReportmonth() != null){
           	daatum = p.getReportmonth();
           }

           PreparedStatement pst1 = con.prepareStatement(sql1);
           pst1.setString(1, custid);
           pst1.setString(2, custid);
           ResultSet rs = pst1.executeQuery();
           while (rs.next()) {
             owncustid = rs.getString("kood").trim();
           }

           PreparedStatement pst2 = con.prepareStatement(sql2);
           pst2.setString(1, prodid);
           pst2.setString(2, prodid);
           ResultSet rs2 = pst2.executeQuery();
           while (rs2.next()) {
             ownprodid = rs2.getString("toode").trim();
           }
           pst.setDouble(1, p.getLastPrice());
           pst.setDouble(2, p.getMovedStockPluss());
           pst.setString(3, ownprodid);
           pst.setString(4, prodid);
           pst.setString(5, custid);
           pst.setString(6, dealer);
           pst.setDate(7, daatum);
           pst.setString(8, owncustid);
           pst.executeUpdate();
         }
       }

       pst.close();
    }





    public void setStocks(HashMap products) throws SQLException{
      connectionUp();
      Iterator iter = products.keySet().iterator();
      String dlr = (String)iter.next();
      String sql = "UPDATE toode SET stock" + dlr + "=0,stockdat" + dlr + "='' ";
      Statement st = con.createStatement();
      st.executeUpdate(sql);
      st.close();

      sql = "UPDATE toode SET stock" + dlr + "=?,stockdat" + dlr + "=? WHERE trim(kood" + dlr + ")=?";
      PreparedStatement pst = con.prepareStatement(sql);
      iter = products.keySet().iterator();
      while(iter.hasNext()){
        String dealer = (String)iter.next();
        ArrayList al = (ArrayList)products.get(dealer);

        for (int i = 0 ; i < al.size() ; i++){
          Product p = (Product)al.get(i);
          pst.setDouble(1, p.getStock());
          pst.setString(2, getStartingTime());
          pst.setString(3, p.getID());
          pst.executeUpdate();
        }
      }
      pst.close();
    }

    /**
     * Method declaration
     *
     *
     * @param agent
     *
     * @return
     *
     * @see
     */
    public TreeMap getCustomers(Agent agent) {
	
    	//Converter.convertCoopSortimenttoMobitrekFormat(null);
    	connectionUp();
	
	String cmd =
	    "SELECT k.kood, k.nimi, 0 AS tasumata,visit,dealer, trim(tanav)+ ' ' +trim(riik)+ ' ' +trim(piirkond)+ ' ' +trim(telefon) AS Address,liik,info , " +
           "blokk1,blokk2,blokk3,blokk4,blokk5,  " +
           "blokk6,blokk7,blokk8,blokk9,blokk10,hinnak,k.email,k.turg,k.distributs AS kuniksdist,maksetin AS ting FROM klient k "
	    + "LEFT JOIN yksus y ON y.yksus=k.dealer";
       if (agent != null){
         cmd += " WHERE objekt2=? OR parool=? ";
       }
       cmd += " GROUP BY k.kood, k.nimi order by k.nimi";

	log(cmd);

	TreeMap customers = new TreeMap();

	try {
	    PreparedStatement st = con.prepareStatement(cmd);

            if (agent != null){
              st.setString(1, agent.getID());
              st.setString(2, agent.getID());
            }

	    ResultSet rs = st.executeQuery();

	    while (rs.next()) {
		Customer cust = new Customer(rs.getString("kood").trim());
		String   nimi = rs.getString("nimi").trim();

		cust.setName(Converter.enCodeEst(nimi));
		cust.setDebt(rs.getDouble("tasumata"));
		cust.setVisit(rs.getString("visit").trim());
                String aadr = rs.getString("address");
                if(aadr != null){
		cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim()),"Estonia"));
                }
                String dealerID = rs.getString("dealer").trim();
                cust.setGroup(Converter.enCodeEst(rs.getString("liik").trim()));
                String info = rs.getString("info") ;
                if(info != null){
                  if(!info.trim().equals(""))
                cust.setInfo(Converter.enCodeEst(info.trim()));
                }

                if (!dealerID.equals("")) {
                  Dealer dealer = (Dealer) getDealers().get(dealerID);

                  if (dealer != null) {
                    cust.setDealer(dealer);
                  }
                }


                String block = " ";
                String block1 = rs.getString("blokk1");
                String block2 = rs.getString("blokk2");
                String block3 = rs.getString("blokk3");
                 String block4 = rs.getString("blokk4");
                  String block5 = rs.getString("blokk5");
                   String block6 = rs.getString("blokk6");
                    String block7 = rs.getString("blokk7");
                     String block8 = rs.getString("blokk8");
                      String block9 = rs.getString("blokk9");
                      String block10 = rs.getString("blokk10");

              if(block1 != null && block1.trim().length()>0){
              block = block1.trim() ;
              }
              if(block2 != null && block2.trim().length()>0){
             block = block + block2.trim()+ "|" ;
           }
           if (block3 != null && block3.trim().length()>0) {
             block = block + block3.trim() + "|";
           }
           if (block4 != null && block4.trim().length()>0) {
             block = block + block4.trim() + "|";
           }
           if (block5 != null && block5.trim().length()>0) {
             block = block + block5.trim() + "|";
           }
           if (block6 != null && block6.trim().length()>0) {
             block = block + block6.trim() + "|";
           }
           if (block7 != null && block7.trim().length()>0) {
             block = block + block7.trim() + "|";
           }
           if (block8 != null && block8.trim().length()>0) {
             block = block + block8.trim() + "|";
           }
           if (block9 != null && block9.trim().length()>0) {
             block = block + block9.trim() + "|";
           }
           if (block10 != null && block10.trim().length()>0) {
             block = block + block10.trim() ;
           }
            cust.setBlockingDealer(block.trim());
            String ale = rs.getString("hinnak");
            if (ale != null && !ale.trim().equals("")){
            cust.setFixDisc(Double.parseDouble(ale));
            }
             String email = rs.getString("email");
              if (email != null && !email.trim().equals("")){
                cust.setConfirmEmail(email.trim());
              }
              boolean showmarket = rs.getBoolean("turg");
                 if(showmarket){
                   if(agent != null ){
                     agent.setShowmarketinfoall(true);
                   }
                 cust.setShowmarketinfo(true);
                 }

            Date upto = rs.getDate("kuniksdist");
            String tanga = rs.getString("ting");
            if(tanga != null && tanga.trim().length()>0){
              if(tanga.trim().equals("KS")){
                tanga="-1";
              }
              cust.setPaymTerm(tanga.trim());
            }
            Calendar cal = Calendar.getInstance() ;
            cal.setTime(upto);
            Calendar cal2 = cal;
            cal = Calendar.getInstance();

            if( cal2.getTimeInMillis() > cal.getTimeInMillis()){
            cust.setDistUpTo("1");
            System.out.println("Distributsiooni" + cust.getName());
            }
            
            String sql3 = "select sentdate ,prodname from supervisor where custid ='" + cust.getID() + "' and sentdate>date()-10";
            Statement stt = con3.createStatement();
            ResultSet rr = stt.executeQuery(sql3);
            String inf = "";
            while (rr.next()){
            	if (inf.equals("") ){
            		inf = "!!!Out Of Stock!!!!|";
            	}
            	inf = inf + rr.getString("sentdate") + "|\t "+ rr.getString("prodname").trim() + "|";
            }
            if(inf != null){
                cust.setInfo(inf);	
            }
            
		 customers.put(nimi, cust);
	    }

	      String sql2 = "select clid AS kood,agentnam AS agent ,sentdate as kuup , mestext " +
  		"AS mess from mj_crm where sentdate > date()-60 order by clid,sentdate";
  Statement stt = con3.createStatement();
  ResultSet rr = stt.executeQuery(sql2);
  StringBuffer sb = new StringBuffer ();
  while (rr.next()){
  	sb.append(rr.getString("kood").trim() + ";" + rr.getString("kuup").trim() + " " + rr.getString("agent").trim()
  			+ "|" + Converter.enCodeEst(rr.getString("mess").trim()) + "\n");
  }
  
  if(sb.length() > 2){
      File f = new File(getDir() + "customercrm.txt");
      if (f.exists())f.delete();
        try {
  		PrintWriter os = new PrintWriter(new FileWriter(
  				getDir() + "customercrm.txt", true));
  		os.write(sb.toString());
  		os.close();
  	  }catch (IOException ex) {
  		ex.printStackTrace();
  	  }
   }
	   
  
  
  
  
	} catch (Exception e) {
	    getParent().showError(e);
	}

	return customers;
    }

    

    private String getDir(){
        String dir = getParent().getProps().getProperty("readin");
        if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
        return dir;
      }
    /**
     * Method declaration
     *
     *
     * @param agent
     *
     * @return
     *
     * @see
     */
    public TreeMap getProducts(Agent agent) {
	connectionUp();

	TreeMap products = new TreeMap();



	try {

	   String cmd =
		"SELECT toode," + nimetu +",kastis, myygihind, liik, plaanhind,minkogus,alusel,ribakood,ribakood3  FROM toode " +
               " WHERE len(trim(toode)) > 2 AND len(trim(liik))>0 AND len(trim("+nimetu+"))>0 AND eisaada = .F. ORDER BY toode ";

	    if (isDebug()) {
		System.out.println(cmd);
	    }

	    ResultSet rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		double  tykke = (rs.getDouble("kastis"));
		String  kood = rs.getString("toode").trim();

		String  nimetus = rs.getString(nimetu).trim();
		Product prod = new Product(kood);
		prod.setName(Converter.enCodeEst(nimetus));
		prod.setPrice(round(rs.getDouble("myygihind"), 2));
		prod.setInCases(tykke);
                prod.setGroup(rs.getString("liik").trim());
                prod.setMeasure(rs.getDouble("plaanhind"));
                prod.setMaxDiscount(rs.getDouble("minkogus"));
                String ean = rs.getString("ribakood");
                prod.setOnPallet(rs.getDouble("alusel"));
                if(ean != null && ean.trim().length()>7 ){
                prod.setEAN(ean.trim());
                }
                prod.setVat("20");
                String  status = rs.getString("ribakood3");

                if (status!= null && status.trim().length() > 0){
                    if (status.indexOf("F")!= -1){
                       prod.setMustBe("1");
                    }
                      if (status.indexOf("D")!= -1){
                        prod.setStatusDiscount("1");
                     }
                      if (status.indexOf("N")!= -1){
                       prod.setStatusNew("1");
                      }

                          if (status.indexOf("O")!= -1){
                             prod.setStatusSellout("1");
                          }
                }
		products.put(nimetus, prod);

		// }
	    }
            rs.close();

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
    public Vector getCustomerProductsList(String agentCode) {
	connectionUp();

	String cmd =
	    "SELECT dok.saaja,rid.toode, kogus, sum(iif(dok.kuupaev>date()-30,(kogus),0)) AS pkogus,dok.kuupaev AS lastdate FROM dok INNER "
	    + "JOIN rid ON dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode " +
           " INNER JOIN klient k ON dok.saaja=k.kood WHERE "
	    + " len(trim(dok.saaja)) > 0 AND len(trim(rid.toode)) > 2  AND dok.kuupaev >= ?";
        if (agentCode != null){
          cmd += " AND (dok.pais2obj = ? OR k.parool=? )";
        }
        cmd += " AND  dok.doktyyp = 'G' GROUP BY dok.saaja, rid.toode ORDER BY dok.kuupaev DESC, dok.saaja, toode.nimetus";

	if (isDebug()) {
	    System.out.println(cmd);
	}

        String sql ="SELECT dok.saaja,rid.toode, sum(kogus) AS valjakogus FROM  dok " +
            "INNER JOIN  rid ON dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode INNER JOIN  klient k ON dok.saaja=k.kood " +
            " WHERE  len(trim(dok.saaja)) > 0 AND len(trim(rid.toode)) > 2 AND dok.kuupaev >date()-30 ";
           if (agentCode != null){
             sql +=" AND (dok.pais2obj = ? OR k.parool=? )" ;
           }
            sql +=" AND dok.doktyyp = 'G' GROUP BY dok.saaja, rid.toode ORDER BY dok.kuupaev DESC, dok.saaja, toode.nimetus";

	Vector custProds = new Vector();
        HashMap kliendid = new HashMap();

	try {
          PreparedStatement pst = con.prepareStatement(cmd);
          pst.setDate(1, getStartingDate());
          if (agentCode != null){
            pst.setString(2, agentCode);
            pst.setString(3, agentCode);
          }

	    ResultSet	      rs = pst.executeQuery();

	    while (rs.next()) {
		String kliendikood = rs.getString("saaja").trim();
                Customer cust = (Customer) kliendid.get(kliendikood);
		String tootekood = rs.getString("toode").trim();
                double kogus = rs.getDouble("kogus");
                double plkogus = rs.getDouble("pkogus");
                String lastDate = rs.getString("lastdate");
                CustProd cp = new CustProd(kliendikood, tootekood, kogus ,plkogus ,0,lastDate,0,0,0," ");
                custProds.add(cp);
                 kliendid.put(kliendikood, cust);
	    }
            rs.close();
            pst.close();

            PreparedStatement pst3 = con.prepareStatement(sql);
           if (agentCode != null){
             pst3.setString(1, agentCode);
             pst3.setString(2, agentCode);
           }
            ResultSet rs1 = pst3.executeQuery();
            while (rs1.next()) {
              String klient = rs1.getString("saaja").trim();
              String toode = rs1.getString("toode").trim();
              double pkogus = rs1.getDouble("valjakogus");
             // System.out.println(toode+"pkogus"+pkogus);

              Customer c = (Customer) kliendid.get(klient);
              if (c != null) {
                //System.out.println("klient"+c.getName());
                ArrayList al = (ArrayList) c.getProducts().get(toode);
                if (al != null) {
                  CustProd cp = (CustProd) al.get(1);
                  if (cp != null) {
                    cp.setMovedStockPluss(pkogus);
                    custProds.add(cp);
                    //System.out.println("CP trykk"+cp.toString());
                  }
                }
              }
            }
            rs1.close();
	} catch (Exception e) {
	    getParent().showError(e);
	}

	connectionDown();

	return custProds;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */

                public String getDealerCustomerCSV(String agent){
                 StringBuffer sb = new StringBuffer();
                 connectionUp();

                     String cmd = "SELECT yksus FROM yksus WHERE klassif=1";

                     if (isDebug()) {
                         System.out.println(cmd);
                     }


                     try {
                         ResultSet rs = stmt.executeQuery(cmd);

                         while (rs.next()) {
                             String       yksus = rs.getString("yksus").trim();
                             String    sql = "SELECT kood,blokk" + yksus + " AS plokk,kood" + yksus + " AS deacode ,debtdate" + yksus + " as daatum FROM klient WHERE kood" + yksus
                                             + "<>'' AND objekt2='" + agent + "' ORDER BY nimi";
                             Statement stm = con.createStatement();
                             ResultSet rst = stm.executeQuery(sql);

                             while (rst.next()) {
                               String klient = rst.getString("kood").trim();
                               String deacode = rst.getString("deacode");
                               String plokk = rst.getString("plokk");
                               String daatum = rst.getString("daatum");

                               if (deacode != null) {
                                 deacode = deacode.trim();
                                 if (deacode.length()==0){
                                  deacode = " ";
                                  }

                               }

                               if (plokk != null) {
                                 plokk = plokk.trim();
                                 if (!plokk.equals("1")) {
                                         plokk = "0";
                                       }
                               }

                               if (daatum != null) {
                                 daatum = daatum.trim();
                                 if (daatum.length()==0){
                                 daatum = " ";
                                 }
                               }



                               sb.append(yksus + ";" + klient + ";" + deacode + ";" + plokk + ";" +
                                         daatum + "\n");
                             }

                             rst.close();
                             stm.close();
                           }

                         rs.close();
                     } catch (Exception e) {
                         getParent().showError(e);
                     }
                     connectionDown();
                  return sb.toString();
                 }


    public DealerProduct getDealerProductsList() {
	connectionUp();

	String cmd = "SELECT yksus FROM yksus WHERE klassif=1";

	if (isDebug()) {
	    System.out.println(cmd);
	}

	DealerProduct dealerProds = new DealerProduct();

	try {
	    ResultSet rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String       yksus = rs.getString("yksus").trim();
                String        stock = "0";
		String    sql = "SELECT toode,stock" + yksus + " AS ladu,kood" + yksus + " AS deacode FROM toode WHERE kood" + yksus
				+ "<>'' ORDER BY nimetus";
		Statement stm = con.createStatement();
		ResultSet rst = stm.executeQuery(sql);

		while (rst.next()) {
                  String toode = rst.getString("toode").trim();
                  String ladu = rst.getString("ladu") ;
                  String deacode = rst.getString("deacode").trim();
                  if (!ladu.trim().equals("")){
                  stock = ladu ;
                  if (stock.equals("0")){
                    stock = " " ;
                  }
                  }
		    dealerProds.add(yksus, toode,stock," ",deacode);
		}

		rst.close();
		stm.close();
	    }

	    rs.close();
	} catch (Exception e) {
	    getParent().showError(e);
	}

	connectionDown();

	return dealerProds;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws SQLException
     *
     * @see
     */
    public TreeMap getProdGroups() throws SQLException {
	connectionUp();

	TreeMap   tm = new TreeMap();
	String    sql =
	    "SELECT liik,artlnimi AS nimetus FROM ../andmed/artliik WHERE grupp='L'";
	Statement st = con.createStatement();
	ResultSet rs = st.executeQuery(sql);

	while (rs.next()) {
	    tm.put(rs.getString("liik").trim(),
	    		Converter.enCodeEst(rs.getString("nimetus").trim()));
	}

	rs.close();
	st.close();
	connectionDown();

	return tm;
    }

    /**
     * Method declaration
     *
     *
     * @param agent
     *
     * @return
     *
     * @throws SQLException
     *
     * @see
     */
    public PlansBonuses getPlansBonuses(String agent) throws SQLException {
	connectionUp();

	PlansBonuses pb = new PlansBonuses();
	String       sql =
	    "SELECT liik FROM ../andmed/artliik WHERE grupp='L' ";
	Statement    st = con.createStatement();
	ResultSet    rs = st.executeQuery(sql);
	String       sqlSelectList = "";
	int	     liik = -1;
	int	     counter = 0;

	while (rs.next()) {
	    counter++;
            liik = rs.getInt("liik");
            pb.add(liik, 0, 0, 0, 0);
	    if (!sqlSelectList.equals("")) {
		sqlSelectList = sqlSelectList + ",";
	    }

	    sqlSelectList = sqlSelectList + "plaan" + liik + ",boonus" + liik;
	}

        sql =
            "SELECT SUM(kogus*" + kordaja + ") AS plaandone,SUM(baasostu) AS bonusdone,"
            + sqlSelectList + ", a.liik FROM dok d "
            + "INNER JOIN  rid r ON r.dokumnr=d.dokumnr LEFT JOIN toode t "
            + "ON t.toode = r.toode INNER JOIN klient k ON k.kood=d.klient LEFT JOIN ../andmed/artliik a ON a.liik=t.liik LEFT JOIN yksus y "
            + "ON y.yksus=d.pais2obj  WHERE klassif=2 AND k.objekt2=? AND a.liik IN (SELECT liik FROM ../andmed/artliik WHERE grupp='L') AND "
            + "year(date())=year(kuupaev) AND month(date())=month(kuupaev) AND r.doktyyp='G' AND k.outbox=.F. AND d.doktyyp='G'   AND d.yksus IN (SELECT yksus FROM yksus WHERE klassif=1 AND block=.F.) "
	    + "GROUP BY a.liik";

	PreparedStatement pst = con.prepareStatement(sql);

	pst.setString(1, agent);

	rs = pst.executeQuery();

	while (rs.next()) {
          int index = rs.getInt("liik");
          pb.add(index, rs.getDouble("plaan" + index),
		       rs.getDouble("boonus" + index), rs.getDouble("plaandone"), rs.getDouble("bonusdone"));
	}

	//System.out.println(counter + ". " + sql);

	return pb;
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
	connectionUp();

	double  salary = 0;

	try {
	    String sql =
                "SELECT SUM(t.myygihind*r.kogus*(1-iif(hk.jrk!=0,hk.jrk,0)/100)) AS sales FROM rid r INNER JOIN dok d ON d.dokumnr=r.dokumnr " +
                "INNER JOIN toode t ON t.toode=r.toode INNER JOIN klient k ON k.kood=d.klient INNER JOIN yksus y ON y.yksus=d.yksus " +
                "LEFT JOIN hkpais hk ON hk.hinnak=k.hinnak  WHERE k.objekt2 =? " +
                "AND year(date())=year(kuupaev) AND len(trim(y.agentcode))=0 AND r.doktyyp='G' AND month(date())=month(kuupaev)" ;

	    if (isDebug()) {
		System.out.println(sql);
	    }

	    PreparedStatement pstmt = con.prepareStatement(sql);

	    pstmt.setString(1, agentCode);

	    ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
              setSales(rs.getDouble("sales"));
            }


	    rs.close();
	    pstmt.close();
	} catch (Exception e) {
	    getParent().showError(e);
	}

	salary = Math.round(salary * 100d) / 100d;

	return salary;
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
     * @param pilot
     *
     * @return
     *
     * @see
     */

    public String getNonMobileOrders(Agent agent){
    	connectionUp();
        StringBuffer sb = new StringBuffer();
        sb.append(
              "Jooksva kuu plaani t�itmise aruanne\n\n KliendiLIIK       T�IDETUD\n");
        try {
         String sql =
          	"SELECT round(SUM(kogus*myygihind),2) AS plaandone ,a.nimetus FROM dok d  INNER JOIN  rid r ON r.dokumnr=d.dokumnr LEFT JOIN toode t " +
            "ON t.toode = r.toode INNER JOIN klient k ON k.kood=d.klient LEFT JOIN ../andmed/klliik a ON a.liik=k.liik LEFT JOIN yksus y " +
            "ON y.yksus=d.pais2obj  WHERE klassif=2 AND d.pais2obj= ?  AND d.doktyyp='G' AND a.liik IN (SELECT liik FROM ../andmed/klliik ) AND " +
            "year(date())=year(kuupaev) AND month(date())=month(kuupaev) AND d.yksus IN (select yksus from yksus where klassif=1 and block=.F.)AND k.outbox=.F. " +
            "GROUP BY a.liik";

          PreparedStatement pst = con.prepareStatement(sql);
           // System.out.println(agent.getID());
          pst.setString(1, agent.getID());

        ResultSet  rs = pst.executeQuery();
          while (rs.next()) {
            sb.append(rs.getString("nimetus") + "\n\t\t " +
                      rs.getDouble("plaandone") + "\n\n");
          }

        } catch (Exception e){}

       System.out.println(sb.toString());

        return sb.toString();


      }


 /*   public String getPilotDayData(Agent agent){
    connectionUp();
    StringBuffer sb = new StringBuffer();
    
    String sql =
        "SELECT liik FROM ../andmed/artliik WHERE grupp='L' ";
    try {
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);

      while (rs.next()) {
    	  sb.append(b)
      }
    
    
    
    
  /* sb.append(
          "Eelmise kuu plaani t�itmise aruanne\n\n LIIK     PLAAN    T�IDETUD\n");
    String sql =
        "SELECT liik FROM ../andmed/artliik WHERE grupp='L' ";
    try {
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);
      String sqlSelectList = "";
      int liik = -1;
      int counter = 0;

      while (rs.next()) {
        counter++;

        liik = rs.getInt("liik");
        //  pb.add(liik, 0, 0, 0, 0);
        if (!sqlSelectList.equals("")) {
          sqlSelectList = sqlSelectList + ",";
        }

        sqlSelectList = sqlSelectList + "plaan" + liik + ",boonus" + liik;
      }

      sql =
          "SELECT round(SUM(kogus*myygihind),2) AS plaandone,SUM(baasostu) AS bonusdone,"
          + sqlSelectList + ", a.liik ,a.artlnimi FROM dok d "
          + "INNER JOIN  rid r ON r.dokumnr=d.dokumnr LEFT JOIN toode t "
          + "ON t.toode = r.toode INNER JOIN klient k ON k.kood=d.klient LEFT JOIN ../andmed/artliik a ON a.liik=t.liik LEFT JOIN yksus y "
          + "ON y.yksus=d.pais2obj  WHERE klassif=2 AND d.pais2obj=?  AND d.doktyyp='G' AND a.liik IN (SELECT liik FROM ../andmed/artliik WHERE grupp='L') AND "
          +
          "year(date()-20)=year(kuupaev) AND month(date()-20)=month(kuupaev) AND d.yksus IN (select yksus from yksus where klassif=1 and block=.F.)AND k.outbox=.F. "
          + "GROUP BY a.liik";

      PreparedStatement pst = con.prepareStatement(sql);
       // System.out.println(agent.getID());
      pst.setString(1, agent.getID());

      rs = pst.executeQuery();


      while (rs.next()) {
       // System.out.println(sqlSelectList);
        int index = rs.getInt("liik");
        sb.append(rs.getString("artlnimi") + "\n\t " +
                  rs.getDouble("plaan" + index) + "   " +
                  rs.getDouble("plaandone") + "\n");
      }
    } catch (Exception e){}

   System.out.println(sb.toString());

    return sb.toString();

  }*/

    

    public String getPilotDayData(Agent agent){

    	String lastMonthVisit = parent.getProps().getProperty("lastreport");
    	long last = System.currentTimeMillis();
    	if(lastMonthVisit != null){
    		last = new Long(lastMonthVisit).longValue();
    	}
    	System.out.println(last + " " + new Date(last) + " " + agent.getID());
      String sql = "  SELECT k.kood,k.nimi ,sv.prodname,cr.sentdate,amoun/100 AS kogus,pertotcat AS kamp , perpalcat AS hinnang," +
      		" percolcat AS protsent,comment,mestext " +
            " FROM mj_crm cr inner join klient k on k.kood = cr.clid left join supervisor sv on k.kood= sv.custid  WHERE   cr.sentdate=? " +
            "  AND rtrim(objekt2) = ? ORDER BY custname ";


      StringBuffer sb = new StringBuffer();
      String header = "\n\n SUPERVISORI t�helepanekud:";
//System.out.println(header);
      boolean first = true;
      boolean groups = true;
      boolean groups2 = true;
      boolean cl = true;
      boolean c2 = true;
      try {
         connectionUp();

                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setDate(1, new Date(last));
                stmt.setString(2, agent.getID());
                ResultSet rs2 = stmt.executeQuery();
                String oldname =  "";
                    while (rs2.next()) {
                    	 String custID = rs2.getString("kood");
                    	if(first){
                    		String date = rs2.getString("sentdate");
                    		sb.append(header + " " + date + "\n\n");
                    		first = false;
                    	}
                    	String name = rs2.getString("nimi").trim();
                    	//System.out.println("nnn"+name);
                        if(!name.equals(oldname)){
                        	cl = true;
                        	c2 =true;
                        	groups2=true;
                        }
                        //System.out.println("yyy"+oldname);
                        if(cl){
                        	sb.append("\n\n " +name + "\n");
                        	String comment2 = rs2.getString("mestext");
                            if(comment2 != null && comment2.trim().length()>0){
                            	sb.append("\tKliendi Kommentaar: " + comment2.trim() + "\n");
                            }
                            cl = false;
                        }

                        if(c2){

                            String sql2 =  " SELECT custname, prodname,resint1,comment,custcomme " +
        		            " FROM supervisor WHERE  sentdate >= ? AND custid = ? ORDER BY custname,resint1";

                          PreparedStatement stmt2 = con.prepareStatement(sql2);
                          stmt2.setDate(1, new Date(last));
                          stmt2.setString(2, custID);
                          ResultSet rs3 = stmt2.executeQuery();



                      	  groups = true;
                      	  boolean f1 = true;
                      	  boolean f2 = true;
                      	  while (rs3.next()) {
                            	if(groups){
                            		sb.append("\n   PUUDUVAD TOOTED & OUTofSTOCK \n");
                            		groups = false;
                            	}

                            String res = rs3.getString("resint1");


                            if(res != null && !res.trim().equals("2") && f1){
                            	//sb.append("  Lisada Sortimentti :\n");
                            	f1 = false;
                            }
                            if(res != null && res.trim().equals("2") && f2){
                            	sb.append("  O.O.S. : \n" );
                            	f2 = false;
                            }
                            sb.append("\t" + rs3.getString("prodname").trim()+"\n");

                          String   comment = rs3.getString("comment");
                            if(comment != null && comment.trim().length()>0){
                            	sb.append("\tKommentaar: " + comment.trim() + "\n");
                            }
                          }
                      	  c2 = false;
                         }
                        //System.out.println("gr2 " +groups2);
                        if(groups2){
                    		//sb.append("\n   TOOTEGRUPID \n");
                    		groups2 = false;
                    	}



               //  sb.append("\t" + rs2.getString("prodname").trim() + "\tVP Hinnang : " + rs2.getString("hinnang") +" \n");
                  //  sb.append("\t" + rs2.getString("prodname").trim() +" \n");
                    String comment = rs2.getString("comment");
                    if(comment != null && comment.trim().length()>0){
                    	sb.append("\tKommentaar: " + comment.trim() + "\n");

                    }




                    oldname = name;

             }
        }catch (Exception e){
        	e.printStackTrace();
        }
      // System.out.println(sb.toString());
        if(sb.toString().trim().length()>0){
        return sb.toString();
       }else {
       	return null;
       }

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

    /**
     *
     * @return the TreeMap value of dealers.
     */
    public TreeMap getDealers() {
	return dealers;
    }


    /**
     *
     * @param aDealers - the new value for dealers
     */
    public void setDealers(TreeMap aDealers) {
	dealers = aDealers;
    }

    /**
     * Method declaration
     *
     *
     * @param dealer
     *
     * @see
     */
    private void addDealer(Dealer dealer) {
	getDealers().put(dealer.getID(), dealer);
    }

    public String getStartingTime(){
      Calendar cal = Calendar.getInstance();
      String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
      if (month.length() == 1) month = "0" + month;
      String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
      if (day.length() == 1) day = "0" + day;
      return day + month + cal.get(Calendar.YEAR);
    }

    public ManagerData getManagerData() throws SQLException{
      String sql = "  SELECT clid AS klient,docsum AS summa,linecount AS ridu,text AS tekst,enddate AS kuupaev,y.nimetus AS dealer " +
     "   FROM dayreport r INNER JOIN dok d ON d.klient=clid INNER JOIN yksus y ON y.yksus=d.yksus " +
     "   WHERE enddate <date()+1 and enddate > DATE()-30 AND d.kuupaev=enddate AND d.doktyyp='G'  AND y.klassif = 1 ORDER BY clid,enddate desc ";
      
      
      
      Connection c = DriverManager.getConnection(url2);
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(sql);

      String sql2 = "SELECT kood AS kood ,nimi AS nimi,maksetin AS maksetin,hinnaale AS ale," +
                   " y.nimetus AS agendinimi , kontaktisi AS kontaktisik , telefon AS telefon " +
                    "FROM klient k LEFT JOIN yksus y ON y.yksus=k.objekt2 WHERE klassif=2";
      connectionUp();
      Statement st2 = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs2 = st2.executeQuery(sql2);

      String sql3 = "SELECT SUM(kogus*myygihind) AS kuumyyk,d.klient FROM rid r INNER JOIN dok d ON d.dokumnr=r.dokumnr INNER JOIN toode t ON r.toode=t.toode " +
                    "WHERE MONTH(d.kuupaev)=MONTH(DATE()) AND YEAR(d.kuupaev)=YEAR(DATE()) AND d.doktyyp='G'  GROUP BY d.klient";
      Statement st3 = con.createStatement();
      ResultSet rs3 = st3.executeQuery(sql3);
      HashMap hm = new HashMap();
      while (rs3.next()){
        hm.put(rs3.getString("klient").trim(), new Double(rs3.getDouble("kuumyyk")));
      }
      rs3.close();
      st3.close();

      String sql4 = "SELECT SUM(kogus*myygihind) AS aastamyyk,d.klient FROM rid r INNER JOIN dok d ON d.dokumnr=r.dokumnr INNER JOIN toode t ON r.toode=t.toode " +
                    "WHERE YEAR(d.kuupaev)=YEAR(DATE()) AND d.doktyyp='G' GROUP BY d.klient";
      Statement st4 = con.createStatement();
      ResultSet rs4 = st4.executeQuery(sql4);
      HashMap hm2 = new HashMap();
      while (rs4.next()){
        hm2.put(rs4.getString("klient").trim(), new Double(rs4.getDouble("aastamyyk")));
      }
      rs4.close();
      st4.close();

      StringBuffer sb = new StringBuffer();
      String sep = ";";
      while(rs.next()){
        String cliID = rs.getString("klient").trim();
        sb.append(cliID + sep);
        sb.append(rs.getString("kuupaev").trim() + sep);
        sb.append(rs.getDouble("summa") + sep);
        sb.append(rs.getInt("ridu") + sep);
        String tekst = ensureLen(rs.getString("tekst")  ," ");
        tekst = tekst.replace('\'', ' ').replace('\"', ' ');

        sb.append(tekst + sep);

        boolean has = scrollTo(cliID, rs2);
        if (has){
          Double aastamyyk = (Double)hm2.get(cliID);
          double am = 0;
          if (aastamyyk != null) am = aastamyyk.doubleValue();
          sb.append(am + sep);

          Double kuumyyk = (Double)hm.get(cliID);
          double km = 0;
          if (kuumyyk != null) km = kuumyyk.doubleValue();

          sb.append(km + sep);
          sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
          sb.append(ensureLen(rs2.getString("kontaktisik"), " ") + sep);
          sb.append(ensureLen(rs2.getString("telefon"), " ") + sep);
          sb.append(ensureLen(rs2.getString("maksetin"), "0") + sep);
          sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
          sb.append("Dealer: " + rs.getString("dealer").trim() + sep);
        }

        sb.append("\n");
      }

            ManagerData md = new ManagerData();
            md.setClientVisitInfo(sb.toString());

            sql = "SELECT kood AS klient,objekt2 AS agent,y.nimetus AS agentname  from klient k inner join yksus y on y.yksus=objekt2 " +
				" where klassif =2  GROUP BY kood Order by kood";

    PreparedStatement ps = con.prepareStatement(sql);
   // ps.setDate(1, new Date(getStartingDate().getTime()));
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


    public void insertDistribution(DistEvalListWrapper wr){
        String sql = "INSERT into distribution values ("
        + "?,"   //   agent	C(50),
        + "?,"   //   agentname	C(200),
        + "?,"   //   clientid	C(50),
        + "?,"   //   clientname	C(200),
        + "?,"   //	prodid	C(50),
        + "?,"   //   prodname	C(200),
        + "date() ,"   //	sentdate	D,
        + "?,"   //	evalcode	C(50),
        + "?,"   //	bestbefore	C(50),
        + "?,"   //   ownproduct	L,
        + "?,"   //	evalname	C(200),
        + "?,"   //	variation	I,
        + "?,"   //	package1	I,
        + "?,"   //	presence	L,
        + "?,"   //	StockDisp	N,
        + "?,"   //	StockWare	N,
        + "?,"   //	SpacePros	I,
        + "?,"   //	FaceAmBef	I,
        + "?,"   //	FaceAmAft	I,
        + "?,"   //	PlanoEval	I,
        + "?,"   //	PalAmount	I,
        + "?,"   //	action	L,
        + "?,"   //	dominance	I,
        + "?,"   //	FirstInFl	I,
        + "?,"   //	PlacPres	I,
        + "?,"   //	NormalPr	N,
        + "?,"   //	ActionPr	N,
        + "?,"   //	comment	C(200),	reserv1	C(200),	reserv2	C(200),
        + "?,"
        + "?,"
   	 + "?,"   //	resint1	I,resint2	I,
   	 + "?,"
        + "?,"   //	resdou1	N,	resdou2	N,
        + "?,"
        + "?,"   //   a2	I,
        + "?,"   //   a3	I,
        + "?,"   //	a4	I,
        + "?,"   //	vobbler	I,
        + "?,"   //	pricetag	I,
        + "?,"   //	numberof11	I,
        + "?,"   //	numberof12	I,
        + "?,"   //	numberof21	I,
        + "?,"   //	numberof22	I,
        + "?,"   //	planocomm	C(200),
        + "?,"   //	colldtime	C(12),
        + "?)" ; //	isbrand	L

        try {
        	connectionUp();
            PreparedStatement ps = con.prepareStatement(sql);


            for (Iterator iter = wr.getDistEval().getLines().iterator(); iter.hasNext(); ) {
              DistEvalLineWrapper lw = new DistEvalLineWrapper(iter.next());
          

              ps.setString(1, wr.getDistEval().getAgentID());
              ps.setString(2, wr.getDistEval().getAgentName());
              ps.setString(3, lw.getLine().getCustID());
              ps.setString(4, lw.getLine().getCustName());
              ps.setString(5, lw.getLine().getProdID());
              ps.setString(6, lw.getLine().getProdName());
              ps.setString(7, lw.getLine().getEvalCode());
              ps.setString(8, lw.getLine().getBestBefore());
              ps.setBoolean(9, lw.getLine().isOwnProduct());
              ps.setString(10, lw.getLine().getEvalName());
              ps.setInt(11, lw.getLine().getVariation());
              ps.setInt(12, lw.getLine().getPackage1());
              ps.setBoolean(13, lw.getLine().isPresence());
              ps.setDouble(14, lw.getLine().getStockAtDisplay());//   + "?,"   //	StockDisp	N,
              ps.setDouble(15, lw.getLine().getStockAtWarehouse());// + "?,"   //	StockWare	N,
              ps.setInt(16, lw.getLine().getSpaceProsentage());//  + "?,"   //	SpacePros	I,
              ps.setInt(17, lw.getLine().getFaceAmountBefore());//  + "?,"   //	FaceAmBef	I,
              ps.setInt(18, lw.getLine().getFaceAmountAfter());// + "?,"   //	FaceAmAft	I,
              ps.setInt(19, lw.getLine().getPalnoeval());//  + "?,"   //	PlanoEval	I,
              ps.setInt(20, lw.getLine().getPalAmount());//  + "?,"   //	PalAmount	I,
              ps.setBoolean(21, lw.getLine().isAction());//  + "?,"   //	action	L,
   		      ps.setInt(22, lw.getLine().getDominance());// + "?,"   //	dominance	I,
   		      ps.setInt(23, lw.getLine().getFirstinflow());// + "?,"   //	FirstInFl	I,
   		      ps.setInt(24, lw.getLine().getPlacespresent());// + "?,"   //	PlacPres	I,
   		      ps.setDouble(25, lw.getLine().getNormalPrice()*100);  // + "?,"   //	NormalPr	N,
   		      ps.setDouble(26, lw.getLine().getPromoPrice()*100);//+ "?,"   //	ActionPr	N,
   		      ps.setString(27, lw.getLine().getComment());//   + "?,"   //	comment	C(200),	reserv1	C(200),	reserv2	C(200),
   		      ps.setString(28, lw.getLine().getReserv1());
   		      ps.setString(29, lw.getLine().getReserv2());
   		      ps.setInt(30, lw.getLine().getResint1());// + "?,"   //	resint1	I,resint2	I,
                ps.setInt(31, lw.getLine().getResint2());
                ps.setDouble(32, lw.getLine().getResdouble1()*100);// + "?,"   //	resdou1	N,	resdou2	N,
                ps.setDouble(33, lw.getLine().getResdouble2()*100);
   		      ps.setInt(34, lw.getLine().getA2());//  + "?,"   //   a2	I,
   		      ps.setInt(35, lw.getLine().getA3());//  + "?,"   //   a3	I,
   		      ps.setInt(36, lw.getLine().getA4());//  + "?,"   //	a4	I,
   		      ps.setInt(37, lw.getLine().getVobbler());// + "?,"   //	vobbler	I,
   		      ps.setInt(38, lw.getLine().getPricetag());// + "?,"   //	pricetag	I,
   		      ps.setInt(39, lw.getLine().getNumberOF11());// + "?,"   //	numberof11	I,
   		      ps.setInt(40, lw.getLine().getNumberOF12());// + "?,"   //	numberof12	I,
   		      ps.setInt(41, lw.getLine().getNumberOF21());// + "?,"   //	numberof21	I,
   		      ps.setInt(42, lw.getLine().getNumberOF22());// + "?,"   //	numberof22	I,
   		      ps.setString(43, lw.getLine().getPlanoComment());//  + "?,"   //	planocomm	C(200),
   		     String time = parent.getTimeHHpMMpSSStr(lw.getLine().getDate().getTime());
   			  ps.setString(44, time);//  + "?,"   //	colldtime	C(12),
                ps.setBoolean(45, lw.getLine().isBrand());// + "?)" ; //	isbrand	L

              ps.executeUpdate();
            }

            //con.commit();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private boolean scrollTo(String id, ResultSet rs) throws SQLException{
      rs.beforeFirst();
      while(rs.next()){
        if (id.equals(rs.getString("kood").trim())){
          return true;
        }
      }
      return false;
    }

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "VSuus";

    private String	       url = "jdbc:odbc:eeva";
    private String             url2 = "jdbc:odbc:report";
    private String             nimetu = "nimetus";
    private Connection	       con3 = null;
    private Connection	       con = null;

    private String	       url3 = "jdbc:odbc:report";
    private String             kordaja = "myygihind";
    private Statement	       stmt;
    private String	       subjectFilesTo = null;
    private TreeMap	       dealers = new TreeMap();
}



/*--- formatting done in "Sun Java Convention" style on 10-07-2002 ---*/

