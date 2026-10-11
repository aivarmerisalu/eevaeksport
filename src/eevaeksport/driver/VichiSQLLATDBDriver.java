package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalLineWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
//import ee.mobilejava.wrapper.SuperVisorLineWrapper;
//import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class VichiSQLLATDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public VichiSQLLATDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    this.lang = parent.getProps().getProperty("language");
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	}
  }

   public String getPilotDayData(Agent agent){
     return null;
   }
   public String getSaveDebtInfo() {
   	return null;
   }


   public Vector getPriceMatrixData() {
   	return null;
   }

   private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
   private Connection getNewConnection2() throws SQLException{
    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsna.user"), getParent().getProps().getProperty("dsna.pass"));
  }

   public void insertDefective(DefectiveWrapper wr){
  
  }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
/*	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
	     String sql = "INSERT into dbo.supervisorlit values ("
	       + "?, "   //   agent	C(50),
	       + "?,"   //   agentname	C(200),
	       + "?,"   //   clientid	C(50),
	       + "?,"   //   clientname	C(200),
	       + "?,"   //	prodid	C(50),
	       + "?,"   //   prodname	C(200),
	       + "getdate() ,"   //	sentdate	D,
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
	       + "?,"   // isaction	L,
	       + "?,"   // ispartner	L,
	       + "?,"   // isbrand	L,
	       + "?,"   // isgroup	L
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
	        Connection con = getNewConnection();
	           PreparedStatement ps = con.prepareStatement(sql);

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
	             String cc = lw.getLine().getCustComment();
	             if (cc == null){
	             	cc = " ";
	             }
	             ps.setString(24, cc);

	             String cd = lw.getLine().getComment();
	             if (cd == null){
	             	cd = " ";
	             }
	             ps.setString(25, cd);

	             String ce = lw.getLine().getCustAgentId();
	             if (ce == null){
	             	ce = " ";
	             }

	             ps.setString(26, ce);

	             String cf = lw.getLine().getCustAgentName();
	             if (cf == null){
	             	cf = " ";
	             }
	             ps.setString(27, cf);
	             ps.setInt(28, lw.getLine().getDealNr());
	             ps.setInt(29, lw.getLine().getIdealNr());

	             String cr1 = lw.getLine().getResStr1();
	             if (cr1 == null){
	             	cr1 = " ";
	             }
	             ps.setString(30, cr1);


	             String cr2 = lw.getLine().getResStr2();
	             if (cr2 == null){
	             	cr2 = " ";
	             }
	             ps.setString(31, cr2);


	             String cr3 = lw.getLine().getResStr3();
	             if (cr3 == null){
	             	cr3 = " ";
	             }
	             ps.setString(32, cr3);
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

*/
	 public String getProductSortingData(){
	   	return null;
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

   public ManagerData getManagerData() throws SQLException{

    String tableName = "dayreport";
    if(getParent().getProps().getProperty("report.tablename") != null && !getParent().getProps().getProperty("report.tablename").trim().equals("")){
    	tableName = getParent().getProps().getProperty("report.tablename");
    }

    String sql = " SELECT clid AS klient,sentdate AS kuupaev,docsum AS summa,linecount AS ridu,text AS tekst,'' AS dealer,clgroup AS agentname" +
        " FROM  " + tableName +
        " WHERE sentdate>(GETDATE()-30) ORDER BY clid ";

   if (isDebug()) System.out.println(sql);
   this.con = getNewConnection();
   Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    StringBuffer sb = new StringBuffer();
    String sep = ";";

    while(rs.next()){
      String cliID = rs.getString("klient").trim();
      sb.append(cliID + sep);
      sb.append(rs.getString("kuupaev").trim() + sep);
      sb.append(getNF().format(rs.getDouble("summa")) + sep);
      sb.append(rs.getInt("ridu") + sep);
      String tekst = ensureLen(rs.getString("tekst")  ," ");
      tekst = tekst.replace('\'', ' ').replace('\"', ' ');
      tekst = tekst.replace('\n', ' ');
      String agendinimi = rs.getString("agentname");
      sb.append(tekst + sep);
        sb.append(getNF().format(0) + sep);
        sb.append(getNF().format(0) + sep);
        sb.append(ensureLen((agendinimi), " ") + sep);
        sb.append(ensureLen((" "), " ") + sep);
        sb.append(ensureLen((""), " ") + sep);
        sb.append((" ") + sep);
        sb.append((" ") + sep);
      sb.append("\n");
    }

    ManagerData md = new ManagerData();
    md.setClientVisitInfo(sb.toString());

 StringBuffer bf2 = new StringBuffer();
 Enumeration enums = klient.elements();
 while(enums.hasMoreElements()){
 	Customer cust = (Customer)enums.nextElement();
 	Iterator agen = cust.getAgents().keySet().iterator();
 	while(agen.hasNext()){
    Agent ag = 	(Agent)agents.get((String)agen.next());
   bf2.append(cust.getID() + sep);
   bf2.append(ag.getID() + sep);
   bf2.append(ag.getName() + "\n");
 }
	}

    md.setClientAgentInfo(bf2.toString());
    return md;

  }


   public void insertDistribution(DistEvalListWrapper wr){
   
   }

 
   public Vector getFixedAssortmentData() {
           Vector lines = new Vector();

           try {
          	  String cmd = " SELECT  [Asortimento kodas] AS grupp,[Prek�s Nr_] AS toode  FROM [Vi�i�nai ir Ko$Preki� asortimentas] r" +
          	  		" INNER JOIN [Vi�i�nai ir Ko$Item] t ON t.[No_]=r.[Prek�s Nr_]  ORDER BY [Net Weight] , [Search Description]";
          	System.out.println(cmd);
             	 Connection con = getNewConnection2();
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
                              mline.setCliGroup(clGroup.trim());
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
         	       con.close();
                         } catch (Exception e) {
                             getParent().showError(e);
                         }
           return lines;
   }



   public DealerProduct getDealerProductsList() {

   	DealerProduct dealerProds = new DealerProduct();
    try {
    	  String cmd = "SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [Vi�i�nai ir Ko$Item Ledger Entry] l " +
          " INNER JOIN [Vi�i�nai ir Ko$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('KLA','KAU','PAN','VIL','�IA') " +
          " AND t.[Blocked]=0 AND t.[Unit Price]>0 AND len(t.[Grup�s kodas])>0 " +
          " GROUP BY l.[Location Code],l.[Item No_] ORDER BY l.[Item No_] ";

       	 Connection con = getNewConnection2();
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(cmd);
   	        while(rs.next()){
   	             String toode = null;
   	             double seis = 0;;
   	             String ladu  = null;
   	              	toode =rs.getString("kood");
   	              	seis = rs.getDouble("kogus");
   	              	ladu = rs.getString("ladu");
   	                 dealerProds.add(ladu, toode,seis+""," "," " );
   	        }

   	      rs.close();
   	      con.close();

   	    System.out.println(cmd);
    } catch (Exception e) {
        e.printStackTrace();
    }
     return dealerProds;
   }


       public String getSaveStockInfo(){
       return null;
       }


   public Vector getDebt(String agentCode){
        return this.debtList;
  }

   public void connectionDown(){
   }

   public void connectionUp(){
   }

           public String getDealerCustomerCSV(String agent){
            return null;
            }

           public String  getCustProdDiscounts(Agent agent){
            return null;
            }

   public TreeMap getProdGroups() throws SQLException {
     TreeMap   tm = new TreeMap();
     setTreeGroup(true);
     String cmd = " SELECT  [Kodas] AS kood,[Apra�as] AS nimi  FROM [Vi�i�nai ir Ko$Preki� grupavimas] order by [Kodas]";
     	  try {
       	 Connection con = getNewConnection2();
       	System.out.println(cmd);
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(cmd);
   	        while(rs.next()){
   	            String id = rs.getString("kood");
   	        if(cleardots(id).equals("28")){
   	        	id = "5.1.";
   	        }
   	                String name = rs.getString("nimi") ;
   	             if((id.trim().length()== 4 ||id.trim().length()== 5 ) && id.trim().length()>0) {
   	             tm.put(cleardots(id), id.substring(0,1) +" "+ cleardots(id) + " " + name);
   	             }
   	        }

   	      rs.close();
   	      con.close();

        } catch (Exception e){
          e.printStackTrace();
        }

         return tm;
   }



   public TreeMap getProdGroupsSecond() throws SQLException {
    TreeMap   tm = new TreeMap();
    String cmd = "SELECT  [Kodas] AS kood,[Apra�as] AS nimi  FROM [Vi�i�nai ir Ko$Preki� grupavimas] order by [Kodas]";
    System.out.println(cmd);
    try {
    	 Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	            String groupID = rs.getString("kood");
	            if(cleardots(groupID).equals("281")){
	            	groupID = "5.1.1.";
	   	        }
	            if(cleardots(groupID).equals("282")){
	            	groupID = "5.1.2.";
	   	        } if(cleardots(groupID).equals("283")){
	            	groupID = "5.1.3.";
	   	        } if(cleardots(groupID).equals("284")){
	            	groupID = "5.1.4.";
	   	        } if(cleardots(groupID).equals("285")){
	            	groupID = "5.1.5.";
	   	        } if(cleardots(groupID).equals("286")){
	            	groupID = "5.1.6.";
	   	        } if(cleardots(groupID).equals("287")){
	            	groupID = "5.1.7.";
	   	        } if(cleardots(groupID).equals("288")){
	            	groupID = "5.1.8.";
	   	        } if(cleardots(groupID).equals("289")){
	            	groupID = "5.1.9.";
	   	        }
	                String name = rs.getString("nimi") ;
	                if(groupID.length()>5){
	                   tm.put(cleardots(groupID),cleardots(groupID) + " " +name);
	           }
	        }

	        rs.close();
	        con.close();
     } catch (Exception e){
       e.printStackTrace();
     }
        return tm;
  }


   private String cleardots(String idx){
   	while(idx.indexOf(".")>0){
    idx = idx.substring(0,idx.indexOf("."))+idx.substring(idx.indexOf(".")+1);
   	}

	   return idx;
   }

   public TreeMap getAgents(){

        String cmd = "SELECT [Code] AS kood,[Name] AS nimi,[Location Code] AS objekt FROM " +
		"[Vi�i�nai ir Ko$Salesperson_Purchaser] WHERE len([Location Code])>0";
	    if (isDebug()) System.out.println(cmd);
	    TreeMap agents = new TreeMap();
	    try {
	        Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	          Agent agent = new Agent(rs.getString("kood").trim());
	          String name = rs.getString("nimi").trim();
	          agent.setObject(rs.getString("objekt"));
	          agent.setIsPilot(true);
	          agent.setName(name);

				String agentAGENTS = getParent().getProps().getProperty("dub." +agent.getID());
				if(agentAGENTS != null){
    	           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
			    while(stag.hasMoreTokens()){  	
               agent.addDubbler(stag.nextToken());
			    }

				}
	          
	          agents.put(name, agent);
	          agents1.put(agent.getID(), agent);
	        }

	        Agent aa = new Agent("HOUSE1");
	        aa.setName("office1");
	        agents.put("office1", aa);

	  cmd = "SELECT k.[No_] AS klient, [Balance total (LCY)]AS summa " +
	  " FROM [Vi�i�nai ir Ko$Customer] k  WHERE k.[Salesperson Code] != '' AND [Balance total (LCY)]<>0 " ;
	  System.out.println(cmd);
	         con = getNewConnection2();
	        Statement stmt1 = con.createStatement();
	        ResultSet rs1 = stmt1.executeQuery(cmd);
	        while(rs1.next()){

	          String id = rs1.getString("klient");
	          Debt debt = new Debt ();
	         // debt.setDoknr(clear(st2.nextToken()));
	          debt.setDue(rs1.getString("summa"));
	          overdebts.put(id,debt);
	     }

            rs1.close();
	        rs.close();
	        con.close();
	    } catch (Exception e){
	        getParent().showError(e);
	      }


     return agents;
   }

       public String getDistribution() {
         return null;
       }


   public TreeMap getCustomers(Agent agent){
    try {


      String cmd = " select a.[Sell-to Customer No_] AS klient, a.[Customer No_] AS maksja,a.[External Document No_] AS number, a.[Due Date] AS kuupaev, sum(b.[Amount]) AS summa, " +
                            " DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payerFROM [Vi�i�nai ir Ko$Cust_ Ledger Entry] a inner join " +
                            " [Vi�i�nai ir Ko$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] " +
                            " where a.[Document type] = 2 AND a.[Due Date] < GETDATE()+7 AND a.[Posting Date] > getdate()-200 AND a.[Open]= 1 " +
                            " group by a.[Sell-to Customer No_],a.[Customer No_] ,a.[External Document No_], " +
                            " a.[Due Date] , DATEDIFF(dd,a.[Due Date],GETDATE()) ,a.[Customer No_] having sum(b.[Amount])<> 0 ";

  System.out.println(cmd);
       this.debtList = new Vector();
       con = getNewConnection2();
       Statement stmt1 = con.createStatement();
       ResultSet rs1 = stmt1.executeQuery(cmd);
       while(rs1.next()){
        Debt debt = new Debt();
       String custID = rs1.getString("klient");
   //     String custID = rs1.getString("maksja");
        debt.setCustID(custID);

        String doknr = rs1.getString("number");
        if(doknr.trim().length()>6){
        	doknr = doknr.substring(6,doknr.length());
        }
        debt.setDoknr(doknr);
        debt.setDuedate(rs1.getDate("kuupaev"));
        debt.setSum(rs1.getDouble("summa"));
        int dueint = rs1.getInt("due");
        debt.setDue(dueint + "");
        int old = 0;
        if(debtDays.get(custID) != null){
         old = Integer.parseInt((String)debtDays.get(custID));
        }
        if(old<dueint){
       this.debtDays.put(custID,dueint+"") ;
        }
        this.debtList.add(debt);
            }

       rs1.close();
       con.close();
      } catch (Exception e){
        e.printStackTrace();
      }
/// KLIENDI ANDMETE LEIDMINE

     TreeMap tm = new TreeMap();
     this.customers = new ArrayList();







   String sql =  " SELECT k.[Name] AS nimi,k.[No_] AS kood,k.[Bill-to Customer No_] AS payer,k.[Address] AS aadress ,k.[Salesperson Code] AS agent , " +
	" [Blokuoti SF i�ra�ym�] AS blokk, " +
	 " [Payment Terms Code] AS payment,[Line Discount %] AS alekood, [Balance total (LCY)]AS yle,[RouteCode] AS visit,[Customer type] AS type, "  +
	  " [Blokavimas po pradelst� dien�] AS extradays ,k.[Asortimento kodas] AS sort,PDA_debt AS debt , 0 AS minsum  " +
	 " FROM [Vi�i�nai ir Ko$Customer] k LEFT JOIN [Vi�i�nai ir Ko$Sales Line Discount] d ON d.[Sales Code] = k.[Customer Disc_ Group]" +
	 " WHERE k.[Salesperson Code] != ''  AND blocked != 3  " ;

   System.out.println(sql);
   try {
        Connection con = getNewConnection2();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while(rs.next()){
         String name = rs.getString("nimi").trim();
           String id = rs.getString("kood").trim();
           //System.out.println(line);
           Customer customer = new Customer(id);
            String payer = null;
           if (name.equals("")) name = "<" + id + ">";
           customer.setName(name);
           	payer = rs.getString("payer");
           	if(payer != null && payer.trim().length()>0){
             customer.setPayer(payer.trim());
             }

             String aadress = rs.getString("aadress");
             if( aadress != null && aadress.trim().length()>0){
               customer.setAddress(aadress);
               customer.setName(customer.getName()+ " " + aadress);
             }
         	String ag = rs.getString("agent");
             customer.addAgent(new Agent(ag));
             
             if (ag != null && ag.trim().length()>0){
 				Agent agg = ((Agent)this.getAgents1().get(ag.trim()));
 				//System.out.println(agg);
 				if(agg != null && agg.getDubbler().size()>0){
 					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
 						customer.addAgent(new Agent((String)agg.getDubbler().elementAt(i)));
 						System.out.println(customer.getName() + customer.getAgents());
 					}
 				}
 			}
             customer.addAgent(new Agent("HOUSE1"));

             if(rs.getString("blokk").equals("1")){
               customer.setSalesBlock(true);
             }

            String paym = rs.getString("payment");

           	String num = rs.getString("alekood");
           	if(num != null && num.trim().length()>0){
             customer.setDiscount(Double.parseDouble((num.trim())));
           	}

      

      		String visit = rs.getString("visit");
             if(visit != null && visit.trim().length()>0){
                customer.setVisit(visit.trim());
             }

            String type = rs.getString("type");
            if(type != null && type.trim().length()>0){
            customer.setCustType(type.trim());
            }

            String bll = rs.getString("extradays");
           	if(bll != null && bll.trim().length()>0){

            if (customer.getSalesBlock()){
              	 customer.setSalesBlock(false);
           	int extra = Integer.parseInt(bll);
           		if(debtDays.get(customer.getID())!= null && customer.getPaymTerm().trim().length()>0 &&
           				(extra < Integer.parseInt((String)debtDays.get(customer.getID())) )){
           	 customer.setSalesBlock(true);
           	}
          }
           	}
           String grupp = rs.getString("sort");
           if(grupp != null && grupp.trim().length()>0){
           	customer.setGroup(grupp.trim());
           }


           	String yes = rs.getString("debt");

           	if (yes != null && !yes.trim().equals("0")){
                customer.setNodebt(true);
           	}

           	
           	double minsum = rs.getDouble("minsum");
           	if(minsum>0){
           		customer.setMinsum(minsum);
           	}

           if (!customer.isNodebt()){
         
  
                String sql3 =  "  SELECT     No_ AS kood, CASE WHEN k.[Bill-to Customer No_] = '' THEN " +
                          "(SELECT     SUM(de.[Amount (LCY)]) FROM [Vi�i�nai ir Ko$Cust_ Ledger Entry] c INNER JOIN  "+
                          " [Vi�i�nai ir Ko$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] "+
                          "  WHERE   c.[Customer No_] = k.No_) WHEN k.[Bill-to Customer No_] <> '' THEN (SELECT   SUM(de.[Amount (LCY)]) "+
                          "  FROM  [Vi�i�nai ir Ko$Cust_ Ledger Entry] c INNER JOIN "+
                          "   [Vi�i�nai ir Ko$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] "+
                          "  WHERE  c.[Customer No_] = k.[Bill-to Customer No_] AND [OPEN] = 1 AND c.[Sell-to Customer No_] = k.No_ "+
						  "	 and [Due Date] <getdate()+7    and c.[Posting Date] > getdate()- 100) "+
					      "	 END AS yle FROM [Vi�i�nai ir Ko$Customer] k "+
					      "	WHERE  (No_ = '" + id + "') AND (Blocked <> 3) AND ([Salesperson Code] <> '') " ;

		    try {
		      Connection con3 = getNewConnection2();
		      Statement stmt3 = con3.createStatement();
		      ResultSet rs3 = stmt3.executeQuery(sql3);
		      while(rs3.next()){
		      
		         double sum = rs3.getDouble("yle");
		         if(sum !=0){
		         	customer.setDebt(sum);
		         	System.out.println("yle " + id+ " " +  sum);
		         }
		         
		      }

rs3.close();
stmt3.close();
con3.close();
		    } catch (Exception e){
		         e.printStackTrace();
		       }

            	

        
           	//customer.setUsedCredit(Double.parseDouble(used.replaceAll(",",".").replaceAll(" ","")));
           }
          
           if(paym != null && paym.trim().length()>0){
        	   paym = paym.replaceAll("D"," ");
               paym = paym.replaceAll("P�EVA"," ");
               paym = paym.replaceAll("P�EVA"," ");
               paym = paym.replaceAll("SR","-1").trim();
        	customer.setPaymTerm(paym);
             }

           
           
          //
           String cont = rs.getString("contact");
          customer.setPersonalData(cont);
          
          // customer.setPayer(id);
           if("Lithuanian".equals(lang) && !customer.getID().substring(0,1).equals("A")){
           	tm.put(customer.getName(), customer);
            this.customers.add(customer.getID());
           }else if("Estonian".equals(lang) && !customer.getID().substring(0,1).equals("P")){
            tm.put(customer.getName(), customer);
            this.customers.add(customer.getID());
           }
         }

        rs.close();
        con.close();



     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }

   public TreeMap getAgents1() {
		return agents1;
	}
   

   private TreeMap agents1 = new TreeMap();
   
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();

     try {
     	String cmd = "SELECT [No_] AS toode,([Search Description]) AS nimi, [Unit Price] AS myygihind, [Grup�s kodas] AS grupp,[Pogrupio kodas] AS grupp2 " +
			",[Base Unit of Measure] AS unit,[Br�k�ninis kodas] AS ean,[Product Status] AS status ,[MaxDiscount] AS maxdiscount, "+
			  "[Type] AS type, [Qty_ per Unit of Measure] AS kastis,  [Net Weight] AS kaal, [VAT Prod_ Posting Group] AS vat, [Description] AS vatdesc " +
			 " FROM [Vi�i�nai ir Ko$Item] LEFT JOIN [Vi�i�nai ir Ko$Item Unit of Measure] vat ON (vat.[Item No_]=[No_] and vat.[Code]='D��.' ) " +
			 " WHERE ([Blocked]=0 AND [Unit Price]>0 AND len([Grup�s kodas])>0 and  len([Search Description]) != 0)" +
			 " or [Pogrupio kodas] like '%2.8.1%' ORDER BY [No_] ";
      System.out.println(cmd);
     	Connection con = getNewConnection2();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode");
            String weight ="";
            Product prod = new Product(id);
         	String name = rs.getString("nimi");
         	name = name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
             double price = rs.getDouble("myygihind");
                prod.setPrice(price);

                String grupp = cleardots(rs.getString("grupp"));
                if(grupp.equals("28")){
                	grupp = "51";
                }
                prod.setGroup(grupp);

                String grupp2 = cleardots(rs.getString("grupp2"));
                if(grupp2.equals("281")){
                	grupp2 = "511";
                }
                if(grupp2.equals("281")){
                	grupp2 = "511";
                }  if(grupp2.equals("282")){
                	grupp2 = "512";
                }  if(grupp2.equals("283")){
                	grupp2 = "513";
                }  if(grupp2.equals("284")){
                	grupp2 = "514";
                }  if(grupp2.equals("285")){
                	grupp2 = "515";
                }  if(grupp2.equals("286")){
                	grupp2 = "516";
                }  if(grupp2.equals("287")){
                	grupp2 = "517";
                }  if(grupp2.equals("288")){
                	grupp2 = "518";
                }  if(grupp2.equals("289")){
                	grupp2 = "519";
                }
                prod.setGroupSecond(grupp2);

                prod.setUnit(rs.getString("unit"));
              String ean = (rs.getString("ean"));
                 if (ean.trim().length()>0){
                   prod.setEAN(ean);
                 }
                    String xxx = rs.getString("status");
                 int xx = Integer.parseInt(xxx);
                if(xx == 1){
                 	prod.setStatusTop("1");
                 } else if (xx == 2){
                   prod.setMustBe("1");
                 } else if (xx == 3) {
                   prod.setMustBe("1");
                   prod.setStatusTop("1");
                 } else if (xx == 4) {
                    prod.setStatusNew("1");
                 } else if (xx == 5) {
                    prod.setStatusSellout("1");
                 }

             	String discou = rs.getString("maxdiscount");
             	if(discou != null && discou.trim().length()>0){
                prod.setMaxDiscount(Double.parseDouble(discou));
                }

                double	cases = rs.getDouble("kastis");
                	if(cases>0){
                   prod.setInCases(cases);
                	}

             	weight = rs.getString("kaal");
                           if(weight != null && weight.trim().length()>0){
                             weight = weight.trim();
                             if(weight.indexOf('.')==0){
                             	weight = "0" + weight;
                             }

                           }

          	 String vat = rs.getString("vat");
                   if(vat != null && vat.trim().length()>0){
                     if(vat.equals("GRUPE4")){
                     	prod.setVat("18");
                     }else if (vat.equals("GRUPE2")){
                     	prod.setVat("5");
                     }
                   }
          //leedu versioonis prod.setName(weight.substring(0,weight.indexOf('.')+3) + " " + name);
          prod.setName(prod.getID() + " " + name);
           tm.put(name, prod);
        }


        rs.close();
        con.close();
     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }




   public String getAssortmentStr(Agent agent){
    StringBuffer sb = new StringBuffer();
     String cmd = " SELECT [No_] AS toode ,[Type] AS type FROM  [Vi�i�nai ir Ko$Item] " +
     		" WHERE len([Type])>0  ORDER BY [Net Weight] , [Search Description]";
    try {
     	 Connection con = getNewConnection2();
     	System.out.println(cmd);
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
        String id = rs.getString("toode");
        String type = rs.getString("type");
			//ABCD
			int i = type.trim().length();
			if(i>0){
			while (i != 0){
				sb.append(" ; ;" + type.substring(i-1,i) + ";" + id + "\n");
				i = i -1;
			 }
			}
	       }

	        rs.close();
	        con.close();
       } catch (Exception e){
      e.printStackTrace();
    }
    return sb.toString();
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     HashMap kliendid = new HashMap();
     try {
    String cmd =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Shipment Date] AS lastdate FROM [Vi�i�nai ir Ko$Sales Invoice Line] r " +
  " INNER JOIN [Vi�i�nai ir Ko$Sales Invoice Header] d ON r.[Document No_]= d.[No_] INNER JOIN [Vi�i�nai ir Ko$Item] t ON t.[No_]=r.[No_]  WHERE" +
  " d.[Shipment Date]> GETDATE()-30 ORDER BY  d.[Sell-to Customer No_],t.[Net Weight] , t.[Search Description],r.[No_],d.[Shipment Date] ";
    System.out.println(cmd);
   //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"
    Connection con = getNewConnection2();
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery(cmd);
    while(rs.next()){

             String custID = rs.getString("klient").trim();

                 String prodID = rs.getString("toode").trim();;

                 double amount = 0;
                 String lastdate = " ";
                 amount = rs.getDouble("kogus");
                 lastdate = rs.getString("lastdate").trim().substring(2,7);
                    Customer cust = (Customer)kliendid.get(custID);
                    if (cust == null){
                      cust = new Customer(custID);
                    }
                    Product prod = new Product(prodID);
                    CustProd cp = new CustProd(custID, prodID,amount , 0, 0,lastdate,0,0,0," ");

                 cust.addProduct(prod,cp,false);
                 kliendid.put(custID, cust);
                 vec.addElement(cp);

           }
         rs.close();
         con.close();


           int counter = 0;
           int cou = 0;
          String oldklient = null;
          String olddate = " ";
          String oldtoode = null;

          String cmd2 =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Shipment Date] AS lastdate FROM [Vi�i�nai ir Ko$Sales Invoice Line] r " +
		  " INNER JOIN [Vi�i�nai ir Ko$Sales Invoice Header] d ON r.[Document No_]= d.[No_] WHERE d.[Shipment Date]> GETDATE()-30 " +
		  " Order by d.[Sell-to Customer No_],r.[No_],d.[Shipment Date] desc ";
          System.out.println(cmd2);
		   //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"
		    Connection con2 = getNewConnection2();
		    Statement stmt2 = con2.createStatement();
		    ResultSet rs2 = stmt2.executeQuery(cmd2);


  while (rs2.next()){
       String klient = rs2.getString("klient").trim();
               String toode = rs2.getString("toode").trim();
               String kogus = "0";
               String date = " ";
                  kogus = rs2.getDouble("kogus")+"";
                  kogus = kogus.replace('.',',');
				  if(kogus.indexOf(",") != -1){
				  	kogus=kogus.substring(0,kogus.indexOf(","));
				  }
                   date = rs2.getString("lastdate").trim().substring(0,10);

               if(cou == 0){oldklient = klient;olddate = date;oldtoode=toode;}
               if(!oldklient.equals(klient)){
                             counter = 0;
                             oldklient = klient;
                             olddate = date;
               }
               if(!toode.equals(oldtoode)){
               	counter = 0;
               }
               Customer c = (Customer)kliendid.get(klient);
               if (c != null ){

                 ArrayList al = (ArrayList)c.getProducts().get(toode);
                 if (al != null){
                   CustProd cp = (CustProd)al.get(1);
                   if (cp != null){
                    if(!olddate.equals(date)){counter++;}
                     if (counter == 0){cp.setS1(kogus );}
                     if (counter == 1){cp.setS2(kogus );}
                     if (counter == 2){cp.setS3(kogus );}
                     if (counter == 3){cp.setS4(kogus );}
                     if (counter == 4){cp.setS5(kogus );}

                     vec.add(cp);
                   }
                 }
                 oldklient = klient;
                 olddate = date;
                 oldtoode = toode;
               }
               cou++;
             }


  rs2.close();
  con2.close();

     } catch (Exception e){
       e.printStackTrace();
     }
     return vec;
   }




   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "VichiLT";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }


   public double getSalary(String agentCode){
     return 0;
   }


   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private TreeMap overdebts = new TreeMap();
   private Vector debtList = new Vector();

   private Vector klient = new Vector();
   private TreeMap agents = new TreeMap();
   private String url = "jdbc:odbc:vicireport";
   private String url2 = "jdbc:odbc:navi";
   private Connection con;
   private NumberFormat NF = NumberFormat.getInstance(Locale.ENGLISH);
   private TreeMap debtDays = new TreeMap();
   private String lang = null;

   public NumberFormat getNF() {
	return NF;
}
/**
 * @param nf The nF to set.
 */
public void setNF(NumberFormat nf) {
	NF = nf;
}


  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {

  	 try {
  	 	String cmd = " select sum(case when [Currency Factor] <> 0 then [Amount]/[Currency Factor]else [Amount] end) as summa,[Salesperson Code] AS agent " +
        " from (select b.[Amount],a.[Currency Factor] ,a.[Salesperson Code]  from [Vi�i�nai ir Ko$Sales Invoice Header] a " +
		" inner  join [Vi�i�nai ir Ko$Sales Invoice Line] b on a.[No_]=b.[Document No_] where Year(getdate())=Year(a.[Shipment Date]) and " +
        " Month(getdate())=Month(a.[Shipment Date]) union all select b.[Amount],a.[Currency Factor],a.[Salesperson Code] from " +
		" [Vi�i�nai ir Ko$Sales Cr_Memo Header] a inner join [Vi�i�nai ir Ko$Sales CR_Memo Line] b on a.[No_]=b.[Document No_] " +
        " where Year(getdate())=Year(a.[Shipment Date]) and Month(getdate())=Month(a.[Shipment Date])) S " +
        " group By [Salesperson Code]";
  	  Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
             double sales = rs.getDouble("summa");
                 String agentID = rs.getString("agent");
                 Agent agent = (Agent)agents.get(agentID);
                 if (agent != null){
                   agent.setMonthSales((int)(sales));
            }
          }

	        rs.close();
	        con.close();
      } catch (Exception e){
        e.printStackTrace();
     }

  }
}
