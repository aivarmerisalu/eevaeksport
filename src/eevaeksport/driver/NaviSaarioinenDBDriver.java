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
import java.sql.Timestamp;
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
import eevaeksport.report.DefectiveLineWrapperMeri;
import eevaeksport.report.SupervisorInfoWrapperMeri;
/*
 * pop3 server mail.saarioinen.ee

mail_saarioinen_pda:D945DDEA
mail_saarioinen_aruanne:3792796A


*/
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class NaviSaarioinenDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public NaviSaarioinenDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
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
   public void insertDefectiveMeri(eevaeksport.report.DefectiveWrapperMeri wr){
	   String sql = " INSERT INTO [Mobilejava].[In_MJ_Quality]  VALUES (?,?,?,?,?,?,?,?,?,?,getdate(),?,' ',' ',0,0) ";
	    try {
	      Connection con = getNewConnection();
	        PreparedStatement ps = con.prepareStatement(sql);

	        ps.setString(1, wr.getDefective().getAgentID());
	        ps.setString(2, wr.getDefective().getAgentName());

	        for (Iterator iter = wr.getDefective().getLines().iterator(); iter.hasNext(); ) {
	          DefectiveLineWrapperMeri lw = new DefectiveLineWrapperMeri(iter.next());
	          ps.setString(3, lw.getLine().getCustID());
	          ps.setString(4, lw.getLine().getCustName());
	          ps.setString(5, lw.getLine().getProdID());
	          ps.setString(6, lw.getLine().getProdName());

	          double kogus = 0;
	          try {
	            kogus = Double.parseDouble(lw.getLine().getAmount());
	          } catch (NumberFormatException nfe){};
	          ps.setDouble(7, kogus);
	          String bb = lw.getLine().getBestBefore();
	       //   System.out.println(kogus+"bb '"+lw.getLine().getBestBefore()+"'"+bb+"'");
	          ps.setString(8, bb);
	          ps.setInt(9, lw.getLine().getDefectCode());
	          ps.setString(10, lw.getLine().getProdType());
	          ps.setString(11, lw.getLine().getDefectText());

	          ps.executeUpdate();
	        }

	        con.close();
	        ps.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	  }

   public void insertDefective(DefectiveWrapper wr){}
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){}

	 
	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	     String sql = "  INSERT into [Mobilejava].[In_MJ_Supervisor] values ("
	       + "?, "   //   agent	C(50),
	       + "?,"   //   agentname	C(200),
	       + "?,"   //   clientid	C(50),
	       + "?,"   //   clientname	C(200),
	       + "?,"   //	prodid	C(50),
	       + "?,"   //   prodname	C(200),
	       + "cast(? as date) ,"   //	sentdate	D,
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
	             eevaeksport.report.SuperVisorLineWrapperMeri lw = new   eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
	             ps.setString(1, wr.getInfo().getAgentID());
	             ps.setString(2, wr.getInfo().getAgentName());
	             ps.setString(3, lw.getLine().getCustID());
	             ps.setString(4, lw.getLine().getCustName());
	             ps.setString(5, lw.getLine().getProdID());
	             ps.setString(6, lw.getLine().getProdName());
	             ps.setTimestamp(7, new Timestamp(Long.parseLong((lw.getLine().getResStr3()))));

		       //     System.out.println( new Timestamp(Long.parseLong((lw.getLine().getResStr3()))).toLocaleString());
	             ps.setDouble(8, lw.getLine().getNormalPrice()*100);
	             ps.setDouble(9, lw.getLine().getAmount()*100);
	             ps.setDouble(10, 0);
	             ps.setDouble(11, 0);
	             ps.setInt(12, lw.getLine().getPercentageFromTotalCategory());
	             ps.setInt(13, lw.getLine().getPercentageFromColdCategory());
	             ps.setInt(14, lw.getLine().getPercentageFromPallCategory());
	             ps.setInt(15, lw.getLine().getPercentageFromShelfCategory());
	             ps.setInt(16, lw.getLine().getPercentageFromRestCategory());
	             ps.setBoolean(17, lw.getLine().isAction());
	             ps.setBoolean(18, lw.getLine().isPartner());
	             ps.setBoolean(19, lw.getLine().isBrand());
	             ps.setBoolean(20, lw.getLine().isGroup());
	             ps.setDouble(21, lw.getLine().getCategoryTotal());
	             ps.setDouble(22, lw.getLine().getPercentageFromRestCategory());
	             ps.setDouble(23, lw.getLine().getActionTotalEval()*100);
	             ps.setDouble(24, lw.getLine().getCustEval()*100);
	             String cc = lw.getLine().getCustComment();
	             if (cc == null){
	             	cc = " ";
	             }
	             ps.setString(25, cc);

	             String cd = lw.getLine().getComment();
	             if (cd == null){
	             	cd = " ";
	             }
	             ps.setString(26, cd);

	             String ce = lw.getLine().getCustAgentId();
	             if (ce == null){
	             	ce = " ";
	             }

	             ps.setString(27, ce);

	             String cf = lw.getLine().getCustAgentName();
	             if (cf == null){
	             	cf = " ";
	             }
	             ps.setString(28, cf);
	             ps.setInt(29, lw.getLine().getDealNr());
	             ps.setInt(30, lw.getLine().getIdealNr());

	             String cr1 = lw.getLine().getResStr1();
	             if (cr1 == null){
	             	cr1 = " ";
	             }
	             ps.setString(31, cr1);


	             String cr2 = lw.getLine().getResStr2();
	             if (cr2 == null){
	             	cr2 = " ";
	             }
	             ps.setString(32, cr2);


	             String cr3 = lw.getLine().getResStr3();
	             if (cr3 == null){
	             	cr3 = " ";
	             }
	             ps.setString(33, cr3);
	             ps.setInt(34, lw.getLine().getResInt1());
	             ps.setInt(35, lw.getLine().getResInt2());
	             ps.setInt(36, lw.getLine().getResInt3());
	             ps.executeUpdate();
	           }

	           ps.close();
	       } catch (SQLException e) {
	           e.printStackTrace();
	       }
	     }


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

    String tableName = "[Mobilejava].[In_MJ_Dayreport]";
    if(getParent().getProps().getProperty("report.tablename") != null && !getParent().getProps().getProperty("report.tablename").trim().equals("")){
    	tableName = getParent().getProps().getProperty("report.tablename");
    }

    String sql = " SELECT clid AS klient,sentdate AS kuupaev,docsum AS summa,linecount AS ridu," +
    		"text AS tekst,'' AS dealer,clgroup AS agentname" +
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


   public void insertDistribution(DistEvalListWrapper wr){}


   public Vector getFixedAssortmentData() {
           Vector lines = new Vector();

         /*  try {
          	  String cmd = " SELECT  [Asortimento kodas] AS grupp,[Prek�s Nr_] AS toode  FROM [Mobilejava$Preki� asortimentas] r" +
          	  		" INNER JOIN [Mobilejava$Item] t ON t.[No_]=r.[Prek�s Nr_]  ORDER BY [Search Description]";
          	System.out.println(cmd);
             	 Connection con = getNewConnection2();
         	        Statement stmt = con.createStatement();
         	        ResultSet rs = stmt.executeQuery(cmd);
         	        ResultSetMetaData md = rs.getMetaData();
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
                         }*/
           return lines;
   }



   public DealerProduct DealerProductsList() {

   	DealerProduct dealerProds = new DealerProduct();
    try {
    	  String cmd = "	  SELECT l.[Item Code]  AS kood, l.Qty AS kogus, l.[LocationCode] AS ladu " +
	      " FROM [Mobilejava].[Out_WarehouseProduct] l WHERE l.Qty != 0 " +
          "  ORDER BY l.[Item Code] ";

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
     String cmd = " SELECT  Code AS kood, Name AS nimi  FROM [Mobilejava].Out_ProductGroup where " +
        "   Name != '' order by Code";
     	  try {
       	 Connection con = getNewConnection2();
       	System.out.println(cmd);
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(cmd);
   	        while(rs.next()){
   	            String id = rs.getString("kood");
                String name = rs.getString("nimi") ;
   	             if(id != null && id.trim().length()>0) {
   	             tm.put(id.trim(), name.trim());
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
    String cmd = " SELECT  Code AS kood,Name  AS nimi  FROM [Mobilejava].Out_ItemCategory  order by Code ";
    System.out.println(cmd);
    try {
    	 Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	            String groupID = rs.getString("kood");
	                String name = rs.getString("nimi") ;
	               if(groupID != null && groupID.trim().length()>0){
	                tm.put(groupID.trim(), name.trim());               	
	               }
	           
	        }

	        rs.close();
	        con.close();
     } catch (Exception e){
       e.printStackTrace();
     }
        return tm;
  }


 

   public TreeMap getAgents(){

        String cmd = "SELECT [Code] AS kood,[Name] AS nimi,'RAPLA' AS objekt FROM " +
		 "[Mobilejava].[Out_Agent] ";
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
	          agent.setName(name);

				String agentAGENTS = getParent().getProps().getProperty("dub." +agent.getID());
				if(agentAGENTS != null){
    	           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
			    while(stag.hasMoreTokens()){  	
                 agent.addDubbler(stag.nextToken());
                 System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
			    }

				}
	          
	          agents.put(name, agent);
	          agents1.put(agent.getID(), agent);
	        }

	        Agent aa = new Agent("HOUSE1");
	        aa.setName("office1");
	        agents.put("office1", aa);
            rs.close();
	        con.close();
	        
	        
	  cmd = "SELECT k.[Code]  AS klient, Amount AS summa " +
	  " FROM [Mobilejava].[Out_Customer]  k  WHERE k.[AgentCode]  != '' AND Amount<>0 " ;
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

  
      String cmd = " select a.CustomerCode  AS klient, a.PayerCode  AS maksja,a.DocNo  AS number, a.DueDate  kuupaev, a.Amount AS summa," +
                           "  a.DaysDue AS due ,a.PayerCode AS payer FROM [Mobilejava].Out_DebtList a " ;
  System.out.println(cmd);
       this.debtList = new Vector();
       con = getNewConnection2();
       Statement stmt1 = con.createStatement();
       ResultSet rs1 = stmt1.executeQuery(cmd);
       while(rs1.next()){
        Debt debt = new Debt();
       String custID = rs1.getString("klient");
        debt.setCustID(custID);

        String doknr = rs1.getString("number");
        if(doknr.trim().length()>6){
        	doknr = doknr.substring(2,doknr.length());
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



   String sql =  "   SELECT k.Name AS nimi,k.[Code] AS kood,k.PayerCode AS payer,k.[Address] AS aadress ,k.[AgentCode] AS agent , " +
	" blocked AS blokk,k.PaymentTermsCode AS payment,k.LineDiscount AS alekood, Amount AS  yle,k.Visit ,k.Type , " +
	"   0 AS extradays ,'' AS sort,0 AS debt , 0 AS minsum ,k.ContactPerson as kontakt " +
	"  FROM [Mobilejava].Out_Customer k  " ;

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
             
             if(customer.getName().indexOf("Kontor")!= -1){
            	  customer.addAgent(new Agent("ANDRIAS")); 
             }
             if (ag != null && ag.trim().length()>0){
 				Agent agg = ((Agent)this.getAgents1().get(ag.trim()));
 				
 				if(agg != null && agg.getDubbler().size()>0){
 					System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
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
         
  
                String sql3 =  " select Code as kood , Amount as yle from Mobilejava.Out_Due " ;

		    try {
		      Connection con3 = getNewConnection2();
		      Statement stmt3 = con3.createStatement();
		      ResultSet rs3 = stmt3.executeQuery(sql3);
		      while(rs3.next()){
		      
		         double sum = rs3.getDouble("yle");
		         if(sum !=0){
		         	customer.setDebt(sum);
		         	//System.out.println("yle " + id+ " " +  sum);
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
           String cont = rs.getString("kontakt");
           if(cont != null && cont.trim().length()>0){
               customer.setPersonalData(cont.trim());   
           }
          
          // customer.setPayer(id);
            tm.put(customer.getName(), customer);
            this.customers.add(customer.getID());
           
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
      TreeMap  ladutr = new TreeMap();
     try {
   	  String cmd = " SELECT l.ItemCode  AS kood, l.Qty AS kogus, l.LocationCode  AS ladu FROM [Mobilejava].Out_Stock l  ";

      	 Connection con = getNewConnection2();
  	        Statement stmt = con.createStatement();
  	        ResultSet rs = stmt.executeQuery(cmd);
  	        while(rs.next()){
  	             String toode = null;
  	             double seis = 0;;
  	              	toode =rs.getString("kood");
  	              	seis = rs.getDouble("kogus");
  	              	ladutr.put(toode, seis + "");
  	        }

  	      rs.close();
  	      con.close();

  	    System.out.println(cmd);
   } catch (Exception e) {
       e.printStackTrace();
   }
     
     try {
     	String cmd = "	 SELECT [Code]  AS toode,Name  AS nimi,p.UnitPrice  AS myygihind, p.ProductGroupCode  AS grupp,p.ItemCategoryCode  AS grupp2 " +
		"	,p.Unit AS unit,p.EAN  AS ean,0 AS status ,'' AS maxdiscount,  " +
		"  p.Type , p.QtyPerUnit AS kastis,p.Weight  AS kaal,p.VATCode AS vat, p.VATName  AS vatdesc  " +
		"  FROM Mobilejava.Out_Product p";
      System.out.println(cmd);
     	Connection con = getNewConnection2();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode").trim();
            String weight ="";
            Product prod = new Product(id);
         	String name = rs.getString("nimi");
         	name =  "    " + name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
             double price = rs.getDouble("myygihind");
                prod.setPrice(price);
              if(ladutr.get(id) != null ){
            	  prod.setStock(new Double((String)ladutr.get(id)).doubleValue());
              }
                String grupp = rs.getString("grupp");
               if(grupp != null && grupp.trim().length() >0){
                   prod.setGroup(grupp.trim());   
               }

                String grupp2 = rs.getString("grupp2");
                if(grupp2 != null && grupp2.trim().length() >0){

                    prod.setGroupSecond(grupp2.trim());	
                }
                
                String unit = rs.getString("unit");
				 if(unit != null && unit.trim().length() >0){
					    prod.setUnit(unit.trim());
				 }
            
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
                      if (vat.equals("VAT9")){
                     	prod.setVat("9");
                     } else if (vat.equals("VAT20")){
                      	prod.setVat("20");
                      }
                   }
                   
                  
          //leedu versioonis prod.setName(weight.substring(0,weight.indexOf('.')+3) + " " + name);
            prod.setName(name);
           tm.put(prod.getName(), prod);
        }


        /*
        
        
        try {
			saveObject(dateOut,"tootedRev.has");
			  if(buf.length() > 2){
	                File f = new File(getDir() + "tootedRev.txt");
	                if (f.exists())f.delete();
	                  try {
	            		PrintWriter os = new PrintWriter(new FileWriter(
	            				getDir() + "tootedRev.txt", true));
	            		os.write(buf.toString());
	            		os.close();
	            	  }catch (IOException ex) {
	            		ex.printStackTrace();
	            	  }
	             }
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
        
        
        rs.close();
        con.close();
     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }



	public static void saveObject(Object obj, String file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
		oos.writeObject(obj);
		oos.close();
	}
   
   public static String deCode(String res ){

	  	return res;
	  }
	 


   public String getAssortmentStr(Agent agent){
  /*  StringBuffer sb = new StringBuffer();
     String cmd = " SELECT [No_] AS toode ,[Type] AS type FROM  [Mobilejava$Item] " +
     		" WHERE len([Type])>0  ORDER BY [Net Weight] , [Search Description]";
    try {
     	 Connection con = getNewConnection2();
     	System.out.println(cmd);
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        ResultSetMetaData md = rs.getMetaData();
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
    return sb.toString();*/
   	return null;
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     HashMap kliendid = new HashMap();
     try {
    String cmd =  "SELECT  r.CustomerCode AS klient,r.ItemCode AS toode,r.Qty AS kogus, r.LastDate AS lastdate, "+
	"  r.LastAmount as lastamount FROM [Mobilejava].Out_CustomerProductList r " +
    	"	 ORDER BY  CustomerCode, r.GroupName,r.ItemCode,r.LastDate";
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
                 lastdate = rs.getString("lastdate").trim().substring(5,10);
                    Customer cust = (Customer)kliendid.get(custID);
                    if (cust == null){
                      cust = new Customer(custID);
                    }
                    
                    
                    double lastamount = rs.getDouble("lastamount");
                    double percent = 0;
                    if(amount>0){
                    	percent =  amount / lastamount * 100;
                    }
                    
               
                    double open = 0;
                    if(amount == 0 && lastamount >0){
                    	open = lastamount;
                       
                    }else if (percent < 70){
                    	open = lastamount - amount ;
                    }
                    
                    
                    Product prod = new Product(prodID);
                    CustProd cp = new CustProd(custID, prodID,amount , 0, 0,lastdate,0,open,0," ");

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

          String cmd2 =  "	SELECT  r.CustomerCode AS klient,r.ItemCode AS toode,r.Qty AS kogus, r.LastDate AS lastdate, " +
	" r.LastAmount as lastamount FROM [Mobilejava].Out_CustomerProductList r where r.LastDate > GETDATE()-30 " +
    		" ORDER BY  CustomerCode, r.GroupName,r.ItemCode,r.LastDate ";
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
     return "NaviSaarioinen";
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
   private String url = "jdbc:odbc:navi";
   private String url2 = "jdbc:odbc:navi";
   private Connection con;
   private NumberFormat NF = NumberFormat.getInstance(Locale.ENGLISH);
   private TreeMap debtDays = new TreeMap();

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
  	 	String cmd = " select s.AgentCode  as agent ,s.Amount  summa from [Mobilejava].Out_AgentSales s";
  	  Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
            
                 String agentID = rs.getString("agent");
                 double sales = rs.getDouble("summa");
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
