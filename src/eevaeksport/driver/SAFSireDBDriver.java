package eevaeksport.driver;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
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
 * Title:        Eevaeksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class SAFSireDBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater,Discounter{
  public SAFSireDBDriver(EksportPanel parent){
    super(parent);
    if(parent.getParentFrame().isDaySales()){
         this.subjectFilesTo = getParent().getString("senddatamsg");
    } else {this.subjectFilesTo = parent.getString("filemsg");}
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      con3 = DriverManager.getConnection("jdbc:odbc:import"
            , "sa", "paranka");
      stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }

	public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr) {
		String sql = "INSERT into dbo.supervisor values (" + "?, " // agent
																	// C(50),
				+ "?," // agentname C(200),
				+ "?," // clientid C(50),
				+ "?," // clientname C(200),
				+ "?," // prodid C(50),
				+ "?," // prodname C(200),
				+ "getdate() ," // sentdate D,
				+ " '' ," // receivedTime D,
				+ "?," // normalpr N,
				+ "?," // amoun N,
				+ "?," // grouptotal N,
				+ "?," // cattotal N,
				+ "?," // pertotcat I,
				+ "?," // percolcat I,
				+ "?," // perpalcat I,
				+ "?," // pershecat I,
				+ "?," // perrescat I,
				+ "?," // isaction L,
				+ "?," // ispartner L,
				+ "?," // isbrand L,
				+ "?," // isgroup L
				+ "?," // actionaeva N,
				+ "?," // actionbeva N,
				+ "?," // actiontota N,
				+ "?," // custeval N,
				+ "?," // custcomme C(200),
				+ "?," // comment C(200),
				+ "?," // custagent C(200),
				+ "?," // custagentname C(200),
				+ "?," // mustbe I,
				+ "?," // ideal I,
				+ "?," // resstr1 C(200),
				+ "?," // resstr2 C(200),
				+ "?," // resstr3 C(200),
				+ "?," // resint1 I,
				+ "?," // resint2 I,
				+ "?)"; // resint3 I,
				//+ "?)"; // country L

		try {
			System.out.println("jam");
			Connection con = getNewConnection2();
			PreparedStatement ps = con.prepareStatement(sql);

			for (Iterator iter = wr.getInfo().getLines().iterator(); iter
					.hasNext();) {
				eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(
						iter.next());
				ps.setString(1, wr.getInfo().getAgentID());
				ps.setString(2, Converter.deCode(wr.getInfo().getAgentName()));
				ps.setString(3, lw.getLine().getCustID());
				ps.setString(4, Converter.deCode(lw.getLine().getCustName()));
				ps.setString(5, lw.getLine().getProdID());
				ps.setString(6, Converter.deCode(lw.getLine().getProdName()));
				ps.setDouble(7, lw.getLine().getNormalPrice() * 100);
				ps.setDouble(8, lw.getLine().getAmount() * 100);
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
				ps.setDouble(20, lw.getLine().getAction1Eval());
				ps.setDouble(21, lw.getLine().getAction2Eval());
				ps.setDouble(22, lw.getLine().getActionTotalEval());
				ps.setDouble(23, lw.getLine().getCustEval());
				String cc = lw.getLine().getCustComment();
				if (cc == null) {
					cc = " ";
				}
				ps.setString(24, Converter.deCode(cc));

				String cd = lw.getLine().getComment();
				if (cd == null) {
					cd = " ";
				}
				ps.setString(25, Converter.deCode(cd));

				String ce = lw.getLine().getCustAgentId();
				if (ce == null) {
					ce = " ";
				}

				ps.setString(26, ce);

				String cf = lw.getLine().getCustAgentName();
				if (cf == null) {
					cf = " ";
				}
				ps.setString(27, Converter.deCode(cf));
				ps.setInt(28, lw.getLine().getDealNr());
				ps.setInt(29, lw.getLine().getIdealNr());

				String cr1 = lw.getLine().getResStr1();
				if (cr1 == null) {
					cr1 = " ";
				}
				ps.setString(30, cr1);

				String cr2 = lw.getLine().getResStr2();
				if (cr2 == null) {
					cr2 = " ";
				}
				ps.setString(31, cr2);

				String cr3 = lw.getLine().getResStr3();
				if (cr3 == null) {
					cr3 = " ";
				}

				Calendar cal = Calendar.getInstance();
				try {
					cal.setTimeInMillis(Long.parseLong(cr3));
				} catch (Exception e) {
					System.out.println("cr3 '" + cr3 + "'");

					// e.printStackTrace();

				}

				ps.setString(32, getDateStr(cal));
				ps.setInt(33, lw.getLine().getResInt1());
				ps.setInt(34, lw.getLine().getResInt2());
				ps.setInt(35, lw.getLine().getResInt3());
				
				ps.executeUpdate();
			}

			ps.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
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

  
	  public Vector getPriceMatrixData()  {
	        Vector lines = new Vector();
	        String    sql = "  select '' AS custid, pl.idgrpricelist AS clgroup,' 'as lisa,pr.id  AS prodid,' ' AS prodgroup,pl.price AS price ,'' as begindate ,'' as enddate " +
	          "  FROM grpricelistrel pl INNER JOIN grarticle pr ON  pl.idgrarticle = pr.id where idgrpricelist > 2  " +
			"	union " +
			"	  select '' AS custid, pl.idgrcpricelist  AS clgroup, 'x' as lisa,pr.id  AS prodid,' ' AS prodgroup,pl.price AS price ,'' as begindate ,'' as enddate " +
	         "   FROM grcpricelistrel pl INNER JOIN grcpricelist u on u.id=pl.idgrcpricelist INNER JOIN grarticle pr ON  pl.idgrarticle = pr.id where " +
	         " idgrcpricelist = 22  and  getdate()>begindate and getdate()<enddate" +
				" ORDER BY 1,2 " ;
			
	        String    sql2 = "	 select k.id AS custid, ''  AS clgroup,pr.id  AS prodid,' ' AS prodgroup,pl.price AS price ,begindate ,enddate , DATEDIFF(dd, begindate, enddate) as days " +
	         "    FROM grcpricelistrel pl INNER JOIN grcpricelist u on u.id=pl.idgrcpricelist INNER JOIN " +
			"	 dbo.GRCPriceLCompany cpl  on cpl.idgrcpricelist=pl.idgrcpricelist inner join grcompany k on k.id=cpl.idgrcompany INNER JOIN grarticle pr ON  pl.idgrarticle = pr.id " +
			"	 where getdate()>begindate and getdate()<enddate and u.id != 22  ORDER BY 1,2 ";
	          Statement st = null;
	          try {
	            Connection con = getNewConnection();
	            st = con.createStatement();
	            ResultSet rs = st.executeQuery(sql);

	            while (rs.next()) {

	              String custID = rs.getString("custid").trim();
	              String clGroup = rs.getString("clgroup");
	              String lisa = rs.getString("lisa");
	              String prodID = rs.getString("prodid").trim();
	              String prodGroup = rs.getString("prodgroup");
	       
	             // String sss = rs.getString("SC18009");
	              String discount ="";
	            /*  if(discount == null){
	              	discount = sss.trim();
	              }else{
	              	discount = discount.trim().replaceAll("%"," ").trim();
	              }
				   */
	              double price = rs.getDouble("price");
	              String begin  = rs.getString("begindate");
	              String end = rs.getString("enddate");
	              MatrixLine mline = new MatrixLine();

	              if (custID != null && custID.trim().length() > 0) {
	                mline.setCustID(custID.trim());
	              }
	              if ( clGroup !=null && clGroup.trim().length() > 0) {
	                mline.setCliGroup(clGroup.trim() + lisa.trim()+ "bb");
	              }

	              if (prodID != null && prodID.trim().length() > 0) {
	                mline.setProdID(prodID.trim());
	              }
	              if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
	                mline.setProdGroup(prodGroup.trim());
	              }
	              if(discount.trim().length()==0){
	              	discount = "0";
	              }
	                mline.setDiscount(Double.parseDouble(discount));

	              if (Double.parseDouble(discount) == 0 && price != 0) {
	                mline.setPrice(price);

	              }
	              
	              if (begin != null && begin.trim().length() > 0) {
	                  mline.setStartDate(begin.trim().substring(0,10));
	                }
	              if (end != null && end.trim().length() > 0) {
	                  mline.setEndDate(end.trim().substring(0,10));
	                }
	             lines.add(mline);
	            }

	            
	          rs = st.executeQuery(sql2);

	            while (rs.next()) {

	              String custID = rs.getString("custid").trim();
	              String clGroup = rs.getString("clgroup");
	              String prodID = rs.getString("prodid").trim();
	              String prodGroup = rs.getString("prodgroup");
	       
	             // String sss = rs.getString("SC18009");
	              String discount ="";
	            /*  if(discount == null){
	              	discount = sss.trim();
	              }else{
	              	discount = discount.trim().replaceAll("%"," ").trim();
	              }
				   */
	              double price = rs.getDouble("price");
	              String begin  = rs.getString("begindate");
	              String end = rs.getString("enddate");
	              int days = rs.getInt("days");
	              MatrixLine mline = new MatrixLine();

	              if (custID != null && custID.trim().length() > 0) {
	                mline.setCustID(custID.trim());
	              }
	              if ( clGroup !=null && clGroup.trim().length() > 0) {
	                mline.setCliGroup(clGroup.trim() + "bb");
	              }

	              if (prodID != null && prodID.trim().length() > 0) {
	                mline.setProdID(prodID.trim());
	              }
	              if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
	                mline.setProdGroup(prodGroup.trim());
	              }
	              if(discount.trim().length()==0){
	              	discount = "0";
	              }
	                mline.setDiscount(Double.parseDouble(discount));

	              if (Double.parseDouble(discount) == 0 && price != 0) {
	                mline.setPrice(price);

	              }
	              
	              if (begin != null && begin.trim().length() > 0) {
	                  mline.setStartDate(begin.trim().substring(0,10));
	                }
	              if (end != null && end.trim().length() > 0) {
	                  mline.setEndDate(end.trim().substring(0,10));
	                }
	          //    if(days < 32){
	            	//  System.out.println("P�evad " + days);
	            	//  mline.setProdLevel("pr"); //m�rgime et on tegemist pika hinnaga mitte kampaania 
	            	  lines.add(mline);
	            //  }
	            
	            }
	            
	            
	            rs.close();
	            st.close();
	            connectionDown();
	          }
	          catch (SQLException ex) {
	            ex.printStackTrace();
	          }
	       System.out.println("price lines size " + lines.size());
	          return lines;
	      }



  
  public String getProductSortingData(){
	   	return null;
	   }

  
          public String getDealerCustomerCSV(String agent){
           return null;
           }

  /**
   *
   */
  public void connectionUp(){
  }
  public String getSaveDebtInfo() {
   	return null;
   }
   

  public String getAssortmentStr(Agent agent){
  	return null;
  }

      public String getSaveStockInfo(){
      return null;
      }

  	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
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

  public Vector getFixedAssortmentData() {
      Vector lines = new Vector();

      try {
     	  String cmd = "  select pr.idgrpricelist AS grupp,t.id AS toode  from grpricelistrel pr " +
				" inner join grarticle t on t.id=idgrarticle where pr.idgrpricelist > 2 order by pr.idgrpricelist,t.code ";
     	System.out.println(cmd);
        	 
    	        Statement stmt = con.createStatement();
    	        ResultSet rs = stmt.executeQuery(cmd);
    	        while(rs.next()){
                       String custID = " ";
                       String clGroup = " ";
                       String prodID = " ";
                       String prodGroup = " ";
                       MatrixLine mline = new MatrixLine();

                       clGroup = rs.getString("grupp");
                       prodID = rs.getString("toode");

                       if (custID != null && custID.trim().length() > 0) {
                         mline.setCustID(custID.trim());
                       }
                       if (clGroup != null
                           && clGroup.trim().length() > 0) {
                         mline.setCliGroup(clGroup.trim()+ "aa");
                       }

                       if (prodID != null && prodID.trim().length() > 0) {
                         mline.setProdID(prodID.trim());
                       }
                       if (mline.getProdID() == null && prodGroup != null
                           && prodGroup.trim().length() > 0) {
                         mline.setProdGroup(prodGroup);
                       }

                       lines.add(mline);
                     }

    	       rs.close();
    	      // con.close();
                    } catch (Exception e) {
                        getParent().showError(e);
                    }
      return lines;
}

  
  public TreeMap getAgents(){
    String cmd = " SELECT id AS agent, ltrim(firstname) + ' ' + ltrim(surname) AS nimetus FROM eremployee";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    HashMap aHash = new HashMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = rs.getString("nimetus").trim();
          agent.setName(name);
          agent.setObject("1");
          agent.setIsPilot(true);
          agents.put(name, agent);
          aHash.put(agent.getID(), agent);
        }
    rs.close();
    //con.close();
    
    
    
    
    String sql = " SELECT e.id AS agent,cat.code AS grupp FROM eremployee e INNER JOIN eremployeecatrel rel ON rel.ideremployee=e.id " +
   "  INNER JOIN eremployeecat cat ON cat.id = rel.ideremployeecat WHERE Category LIKE 'Tarnija%' ORDER BY e.code";

    Statement  stmt2 = con.createStatement(); 
ResultSet rs1 = stmt2.executeQuery(sql);

while (rs1.next()) {
String ag = rs1.getString("agent").trim();
String liik = rs1.getString("grupp").trim();
Agent agent = (Agent)aHash.get(ag);
if (agent != null){
 agent.addProdGroupList(liik);
// System.out.println(agent.getName()+" " + agent.hasProdGroupList(liik) +" " + liik);
}

}

    } catch (Exception e){
        getParent().showError(e);
      }
     /* String  cmd2 = "SELECT MWWHLO AS yksus ,MWWHNM AS nimetus FROM mvxjdta.MITWHL WHERE " +
		  " MWCONO='200' AND (MWWHLO = '200' OR MWWHLO = '205' OR MWWHLO = '215')";

       try {
         Connection con = getNewConnection();
           Statement stmt = con.createStatement();
           ResultSet	      rs = stmt.executeQuery(cmd2);


           while (rs.next()) {
             Dealer dealer = new Dealer(rs.getString("yksus").trim());
              dealer.setName(rs.getString("nimetus").trim());
                    addDealer(dealer);
           }
           rs.close();
           stmt.close();
           con.close();

       } catch (Exception e) {
           getParent().showError(e);
        }*/

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




  public TreeMap getCustomers(Agent agent){
    TreeMap customers = new TreeMap();

    try {
      Connection con = getNewConnection();
      Statement st = con.createStatement();

      String cmd2 = " SELECT kl.id AS kood,ltrim(kl.name) AS nimi,kl.locationaddr AS address, phone AS info,U_visit AS visit ,message AS agent, " +
           " (case when len(ltrim(kl.idsettlcompany)) >0 then kl.idsettlcompany else kl.id end) AS payer,-kl.pricecut AS ale ,idgrpricelist as grupp " +
           " FROM grcompany kl WHERE  ltrim(kl.id)>0 ORDER BY ltrim(kl.name)";

      String cmd = "  select k.id FROM grcpricelist u  INNER JOIN " +
				" dbo.GRCPriceLCompany cpl  on cpl.idgrcpricelist=u.id inner join grcompany k on k.id=cpl.idgrcompany " +
				"  where getdate()>begindate and getdate()<enddate and u.id = 22";
Vector kollane = new Vector();
      ResultSet rs = st.executeQuery(cmd);
   

      while(rs.next()){
    	  kollane.add(rs.getString("id").trim());
      }
      rs.close();
        
      
      if (isDebug()) System.out.println(cmd2);
     rs = st.executeQuery(cmd2);
      HashMap custMap = new HashMap();

      while(rs.next()){
          String accountnum = rs.getString("kood").trim();
          Customer cust = new Customer(accountnum);
          String name = rs.getString("nimi").trim();
          cust.setName(Converter.enCodeEst(name));
          String aadress1 = rs.getString("address");
          if (aadress1 != null) {aadress1 = aadress1.trim();} else {aadress1 = " ";}
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress1),"Estonia"));
          String info = rs.getString("info");
          if(info != null && info.trim().length()>0){
          	cust.setPersonalData(info.trim());
          }
          String visit = rs.getString("visit");
          if(visit != null && visit.trim().length()>0){
           cust.setVisit(visit.trim());
          }
          //String info = rs.getString("info");
          //if (info != null){cust.setInfo(info.trim());}
         // String limiit = rs.getString("limiit");
         // System.out.println(limiit);
          //if (limiit != null){cust.setCredit(info.trim());}
          String agent1 = rs.getString("agent").trim();
          if(agent1.indexOf("+")!=-1){
          	Agent aaa = new Agent(agent1.substring(0,agent1.indexOf("+")));
          	cust.addAgent(aaa);
          	if(aaa.getID().equals("26")){
          		cust.addAgent(new Agent("29"));
          	}
          	Agent aaaa = new Agent(agent1.substring(agent1.indexOf("+")+1,agent1.length()));
          	cust.addAgent(aaaa);
         	if(aaaa.getID().equals("26")){
          		cust.addAgent(new Agent("29"));
          	}
          	
          	//System.out.println("ag" + cust.getAgents().toString());
          }else{
          	Agent aaaaa = new Agent(agent1);
          	cust.addAgent(aaaaa);
         	if(aaaaa.getID().equals("26")){
          		cust.addAgent(new Agent("29"));
          	}
          }
       
          
          cust.setPayer(rs.getString("payer").trim());
          double ale  = rs.getDouble("ale");
          if (ale>0){cust.setDiscount(ale);}
         // cust.setDebt(rs.getDouble("debt"));
          int grupp = rs.getInt("grupp");
          if (grupp > 2){
        	  Vector aa = new Vector();
        	  aa.add(grupp + "aa");
        	  cust.setFixSortCode(aa);
          }
       String asi = "";
       if(kollane.contains(cust.getID())){
    	 
    	   cust.setCustType("22xbb");
     	  cust.setGroup("22xbb");
       }else{
    	   cust.setCustType(grupp + asi +"bb");
     	  cust.setGroup(grupp + asi +"bb");   
       }
        
    	 
    	  
          customers.put(name, cust);
          custMap.put(cust.getID(), cust);
    }


    String cmd3 = "SELECT d.idpartner AS maksja,d.idgrcompany AS saaja, sum(d.totalamount*exchangerate) +sum(-m.amountbase) AS SUMMA  FROM sdinvoice d " +
          " LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id WHERE docstate !='F'   GROUP BY  d.idpartner,d.idgrcompany,exchangerate ";

     if (isDebug()) System.out.println(cmd3);
        ResultSet rs1 = st.executeQuery(cmd3);
         while (rs1.next()) {
           String id = rs1.getString("saaja");
           if(id != null){
            double debt =rs1.getDouble("SUMMA");
             Customer cust = (Customer) custMap.get(id.trim());
              if (cust != null ) {
                cust.setDebt(debt);
               }
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
  private Connection getNewConnection2() throws SQLException{
	    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	  }
  
  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "select k.code AS liik ,k.name AS nimetus from grcompany k inner join grarticle" +
    		" t on t.idgrcompany=k.id  group by k.code,k.name order by k.name";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
        tm.put(rs.getString("liik").trim(),
        		Converter.enCodeEst(rs.getString("nimetus").trim()));
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}

    public TreeMap getProdGroupsSecond() throws SQLException {
        Connection con = getNewConnection();
        TreeMap   tm = new TreeMap();
        String    sql = "SELECT id AS liik , name AS nimetus FROM grarticlecat WHERE category='Tootegrupid' ORDER BY name";
        
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            tm.put(rs.getString("liik").trim(),
            		Converter.enCodeEst( rs.getString("nimetus").trim()));
        }
        rs.close();
        st.close();
        con.close();

        return tm;
  }




  public TreeMap getProducts(Agent agent){
    
    String cmd = "SELECT pr.id AS kood,pr.code,pr.name AS nimi,hi.price AS hind,u_ean  AS EAN , " +
       " pr.contentquantity AS tykke ,y.name AS yhik " +
       "  FROM grarticle pr  LEFT JOIN grpricelistrel hi ON  " +
       "  hi.idgrarticle=pr.id  AND idgrpricelist=2  " +
       "  LEFT JOIN grunit y ON y.id=pr.idgrunit  LEFT JOIN grbarcode bc ON bc.id=pr.id " +
       " WHERE  articletype='G' and hi.price >0  ORDER BY pr.name,hi.price desc";
    
    
    String cmd2 = "SELECT idgrarticle AS toode,SUM(quantity - rquantity) AS seis FROM " +
        "dcurbalancestore cur  WHERE cur.iddrstore=1 AND quantity!=0  GROUP BY idgrarticle" ;
    
    
    

    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {
      Connection con2 = getNewConnection();
      Connection con = getNewConnection();
      Hashtable groups1 = new Hashtable();
      Hashtable groups2 = new Hashtable();
      Hashtable groupsPunane = new Hashtable();
      
      String    sql = "select t.id AS toode,k.code AS liik from grcompany k inner join grarticle t on t.idgrcompany=k.id";
      
      Statement st7 = con.createStatement();
      ResultSet rs7 = st7.executeQuery(sql);

      while (rs7.next()) {
          groups1.put(rs7.getString("toode").trim(),
                 rs7.getString("liik").trim());
      }
      rs7.close();
      st7.close();
      
      
      String    sql2 = "SELECT rel.idgrarticle AS toode , rel.idgrarticlecat AS liik FROM grarticlecatrel  rel " +
		" INNER JOIN grarticlecat cat ON cat.id=rel.idgrarticlecat WHERE cat.category " +
		" like 'Tootegrupid%' ORDER BY rel.idgrarticle,rel.idgrarticlecat";
      
      Statement st6 = con.createStatement();
      ResultSet rs6 = st6.executeQuery(sql2);

      while (rs6.next()) {
          groups2.put(rs6.getString("toode").trim(),
                 rs6.getString("liik").trim());
      }
      
         sql2 = "SELECT rel.idgrarticle AS toode , rel.idgrarticlecat AS liik FROM grarticlecatrel  rel " +
		" INNER JOIN grarticlecat cat ON cat.id=rel.idgrarticlecat WHERE cat.id  = 63 " +
		" ORDER BY rel.idgrarticle,rel.idgrarticlecat";
    
      
          rs6 = st6.executeQuery(sql2);

         while (rs6.next()) {
             groupsPunane.put(rs6.getString("toode").trim(),
                    rs6.getString("liik").trim());
         }
         
      
      
      rs6.close();
      st6.close();
      
      
      Statement st2 = con2.createStatement();
      ResultSet rs2 = st2.executeQuery(cmd2);
      Hashtable tooted = new Hashtable();
     
      
        while(rs2.next()){
          String id = rs2.getString("toode").trim();
          Product toode = new Product(id);
          double sto = rs2.getDouble("seis");
        //  double ost = rs2.getDouble("ost");
          toode.setStock(sto);
          //toode.setMeasure(ost);
          tooted.put(id,toode);
        }

   
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(cmd);

      while(rs.next()){
        String kood = rs.getString("kood").trim();

        String code = rs.getString("code").trim();
        Product toode = (Product)tooted.get(kood);
        double kogus;
        double ost;
        if (toode == null) {
          kogus = 0;
          ost = 0;
        }
        else {
          kogus = round(toode.getStock(), 2);
           ost = round(toode.getMeasure(), 2);
         }

         Product prod = new Product(kood);
         String nimetus = code + " " + rs.getString("nimi").trim();
         prod.setStock(kogus);
         double hind = rs.getDouble("hind");
         prod.setPrice(hind);
         double margin = (1 - (ost / hind)) * 100;
         int margi = 0;
         try {
           margi = (int)margin;
         }

         catch (Exception e) {}
          //System.out.println("margi"+margi+"   "+margin);
     
         if (margi != 0 || margi != 100) { prod.setMargin(margi + "");
         }

         prod.setName(nimetus);
         String ean = rs.getString("ean");
         if (ean != null && ean.trim().length() > 7) {
           prod.setEAN(ean.trim());
         }

         
         
         String grupp = (String)groups1.get(prod.getID());
         if (grupp != null && grupp.trim().length() > 0) {
           prod.setGroup(grupp.trim());
         }

         String grupp2 = (String)groups2.get(prod.getID());
         if (grupp2 != null && grupp2.trim().length() > 0) {
           prod.setGroupSecond(grupp2.trim());
         }
         
         boolean oioi = true;
         String punane = (String)groupsPunane.get(prod.getID());
         if (punane != null && punane.trim().length() > 0) {
          oioi = false;
          nimetus = "##" + nimetus ;
         }
         prod.setName(Converter.enCodeEst(nimetus));
         
         prod.setInCases( (rs.getDouble("tykke")));
        String unit = rs.getString("yhik");

        if (unit != null && unit.trim().length() > 0) {
          unit = unit.trim();
          prod.setUnit(unit);
        }
        
        

        
        boolean goes = true ;
        if(oioi && prod.getStock()<1 ){
        	goes = false;
        }
       
        if(prod.getName().indexOf("M902") != -1){
            System.out.println("toode" + kood + " " + prod.getName() + " " + oioi + " " +( prod.getStock()<1 ) + " " + goes);
        	
        }

        if (goes){
      
        	  if(prod.getName().indexOf("M902") != -1){
                  System.out.println("toode" + kood + " " + nimetus + " " + oioi + " " +( prod.getStock()<1 ) + " " + goes);
              	
              }

        	
        	products.put(nimetus, prod);
        }
       
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


	public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}

  public Vector getCustomerProductsList(String agentCode){

    String sql1 = "SELECT d.idgrcompany AS klient,t.id AS toode, r.quantity AS kogus ,r.docdate as kuupaev ,r.unitprice AS lastprice " +
        "FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
        "INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=d.companyname " +
        "WHERE r.docdate>getdate()-180 AND len(ltrim(d.idgrcompany))>0 and r.quantity>0 " +
        "AND r.articletype='G' GROUP BY d.idgrcompany,t.id,r.quantity,r.docdate,r.unitprice ORDER BY d.idgrcompany,t.id,r.docdate";

    String sql2 = " SELECT d.idgrcompany AS klient,t.id AS toode,SUM(case when quantity>0 then quantity else 0 end)" +
      " AS plusskogus, SUM(case when quantity<0 then quantity else 0 end) AS minuskogus " +
      "FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
      "INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=d.companyname " +
      "WHERE r.docdate>getdate()-120 AND len(ltrim(d.idgrcompany))>0 and r.quantity>0 " +
      "AND r.articletype='G' GROUP BY t.id,d.idgrcompany ORDER BY d.idgrcompany,t.id ";

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
                 lastdate = lastdate.substring(5,10);
                double hind = Double.parseDouble(rs.getString("lastprice").trim());
                Customer cust = (Customer)kliendid.get(kliendikood);
                if (cust == null){
                  cust = new Customer(kliendikood);
                }
                Product prod = new Product(tootekood);
                cust.addProduct(prod, new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,lastdate,hind,0,0," "),false);
                kliendid.put(kliendikood, cust);
                //double pluskogus = Double.parseDouble(rs.getString("plusskogus").trim());
                //double minuskogus = Double.parseDouble(rs.getString("minuskogus").trim());
                //CustProd cp = new CustProd(kliendikood, tootekood, kogus ,pluskogus ,minuskogus);
                //custProds.add(cp);
              }

              rs.close();
              stmt.close();

              stmt = con.prepareStatement(sql2);
              rs = stmt.executeQuery();
              while(rs.next()){
                String klient = rs.getString(1).trim();
                String toode = rs.getString(2).trim();
                double pkogus = rs.getDouble(3);
                double mkogus = rs.getDouble(4);
                Customer c = (Customer)kliendid.get(klient);
                if (c != null){
                  ArrayList al = (ArrayList)c.getProducts().get(toode);
                  if (al != null){
                    CustProd cp = (CustProd)al.get(1);
                    if (cp != null){
                      cp.setMovedStockPluss(pkogus);
                      cp.setMovedStockMinus(mkogus);
                      custProds.add(cp);
                      //System.out.println(cp.getProdID());
                    }
                  }
                }
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

    String cmd =
        " SELECT idworker AS agent ,SUM(d.totalamount)/1.20  AS summa  FROM  sdinvoice d  " +
        " WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)=MONTH(getdate()) " +
        " GROUP BY idworker";

       if (isDebug()) System.out.println(cmd);
          try {
            Connection con = getNewConnection();
              Statement stmt = con.createStatement();
              ResultSet	      rs = stmt.executeQuery(cmd);
              while(rs.next()) {
                String agentID = rs.getString("agent");
                if(agentID != null){
                	 Agent agent = (Agent)agents.get(agentID.trim());
                     if (agent != null){
                       agent.setMonthSales((int)rs.getDouble("summa"));
                      // System.out.println(agent.getMonthSales());
                     } 	
                }
               
              }
              rs.close();
              stmt.close();
              con.close();
          } catch (Exception e) {
              getParent().showError(e);
           }
        }



  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


  public DealerProduct getDealerProductsList() {


       /*try {


          String  sql = " SELECT  MBITNO AS toode,MBSTQT-MBREQT AS STOCK , MBWHLO AS LADU " +
		"from mvxjdta.MITBAL l WHERE MBWHLO='200' OR MBWHLO='205' OR MBWHLO='215' ORDER BY MBITNO" ;

            if (isDebug()) System.out.println(sql);
               Connection con1 = getNewConnection();
               Statement stm = con1.createStatement();
               ResultSet rst = stm.executeQuery(sql);

                 while (rst.next()) {
                   String toode = rst.getString("toode").trim();
                   int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                   String ladu  =  rst.getString("ladu").trim();
                   dealerProds.add(ladu, toode,seis+"" );
                   //System.out.println(ladu+" "+toode+" "+seis+"");
               }
               rst.close();
               stm.close();
               con1.close();

       } catch (Exception e) {
           getParent().showError(e);
       }*/

        //return dealerProds;
        return null;
      }



      public Vector getDebt(String agentCode) throws SQLException{
       Vector list = new Vector();
       String sql = " SELECT d.idpartner AS maksja,d.sourcedocno AS arve, d.promptdate AS kuupaev, " +
        " d.totalamount*exchangerate AS SUMMA ,sum(m.amountbase ) AS minus ,-datediff(day,getdate(),d.promptdate)  AS paevi " +
        " FROM sdinvoice d " +
        " LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
        " WHERE docstate !='F'  AND d.promptdate < getdate()  " +
        "  group by  d.sourcedocno,d.idpartner,d.promptdate,d.totalamount,exchangerate  ORDER BY d.idpartner " ;

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
         double minus = rs.getDouble("minus");
         debt.setDue(rs.getString("paevi"));
        // if(duedate.getTime()<now.getTime()){
           if( summa-minus > 1 || summa-minus < -1){
              debt.setSum(summa-minus);
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
      return "SAFSire";
    }

    public String getSubjectFilesTo() {
      return this.subjectFilesTo;
    }


    public String getDaySalesData(Agent agent) {
      return null;
    }



    public String getPilotDayData(Agent agent){

        String sql2 =  " select max(kliendikood) AS klient ,max(tellnr) AS arve, max(tahtaeg) AS kuupaev , count (tellnr) as ridu " +
" from tp inner join ts on tp.tp_id=ts.tp_id  WHERE year(tahtaeg)=year(getdate()) AND month(tahtaeg)=month(getdate()) and " +
"day(tahtaeg)=day(getdate()) "+
" and sisestaja =  '" + agent.getName() + "' " +
" group by tp.tp_id order by max(sisestaja)";
	     
   
        
        System.out.println("siin");
          StringBuffer sb = new StringBuffer();
          String header = " T�nased tehingud :\n\n";
          boolean first = true;
            try {
                     Statement st = con3.createStatement();
                    ResultSet rs2 = st.executeQuery(sql2);
                        while (rs2.next()) {
                        	if(first){
                        		sb.append(header);
                        		first = false;
                        	}

                        sb.append(rs2.getString("klient")  +
                        		"\n    (" + rs2.getString("arve").trim() + ")[" + rs2.getString("kuupaev").substring(0,10) + "] " + 
                        		getParent().getNF().format(rs2.getDouble("ridu"))  
                        	+" rida "	+  "\n\n");
                        }

                        
                        
                        } catch (Exception e) {
                            getParent().showError(e);
                        }
                        System.out.println(agent.getID() + sb.toString());
           if(sb.toString().trim().length()>0){
           	
            return sb.toString();
           }else {
           	return null;
           }
    }

    public void insertDefective(DefectiveWrapper wr){

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
  private String url2 = "jdbc:odbc:report";
  private Connection con;
  private Connection con3;
  private Statement stmt;
  private String subjectFilesTo = null;
  private NumberFormat NF = NumberFormat.getInstance();
  private TreeMap  dealers = new TreeMap();
}
