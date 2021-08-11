package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalLineWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * 
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class NaviKPDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager,FixLoader {

  public NaviKPDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     NF.setMaximumFractionDigits(2);
	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	}
	  
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
     }
  String user =  null;
  String pass = null;
  String rcompany = null;

  private TreeMap eanprods = null;
	
	 @Override
		public void FixLoader() {
		  eanprods =  Converter.loadMore();
		  
		  if(eanprods == null) {
			  eanprods = new TreeMap();
		  }
			this.getProducts(null);
			TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
			System.out.println("Contracts " + contracts.keySet().size() + " " + contracts.keySet().toString());
			if(contracts.size() > 0) {
				Converter.sendContractJson(contracts ,  rcompany,  user ,  pass);
			}
			
			
			System.exit(0);
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
    String sql = "INSERT INTO dbo.MJ_QUALITYLIT VALUES (?,?,?,?,?,?,?,?,?,?,getdate(),?,' ',' ',0,0)";
    try {
      Connection con = getNewConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, wr.getDefective().getAgentID());
        ps.setString(2, wr.getDefective().getAgentName());

        for (Iterator iter = wr.getDefective().getLines().iterator(); iter.hasNext(); ) {
          DefectiveLineWrapper lw = new DefectiveLineWrapper(iter.next());
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
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}


	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	     String sql = "INSERT into supervisor values ("
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
	        	   eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
	             ps.setString(1, wr.getInfo().getAgentID());
	             ps.setString(2, Converter.deCode(wr.getInfo().getAgentName()));
	             ps.setString(3, lw.getLine().getCustID());
	             ps.setString(4, Converter.deCode(lw.getLine().getCustName()));
	             ps.setString(5, lw.getLine().getProdID());
	             ps.setString(6, Converter.deCode(lw.getLine().getProdName()));
	             ps.setDouble(7, lw.getLine().getNormalPrice());
	             ps.setDouble(8, lw.getLine().getAmount());
	             ps.setDouble(9, lw.getLine().getGroupTotal());
	             ps.setDouble(10, lw.getLine().getCategoryTotal());
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
	             String cc = Converter.deCode(lw.getLine().getCustComment());
	             if (cc == null){
	             	cc = " ";
	             }
	             ps.setString(24, cc);

	             String cd = Converter.deCode(lw.getLine().getComment());
	             if (cd == null){
	             	cd = " ";
	             }
	             ps.setString(25, cd);

	             String ce = lw.getLine().getCustAgentId();
	             if (ce == null){
	             	ce = " ";
	             }

	             ps.setString(26, ce);

	             String cf = Converter.deCode(lw.getLine().getCustAgentName());
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
	             ps.setInt(35, lw.getLine().getResInt3()); //111 on eesti andmed
	             ps.executeUpdate();
	           }

	           ps.close();
	           con.close();
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

    String tableName = "dayreport";
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


   public void insertDistribution(DistEvalListWrapper wr){
     String sql = "INSERT into dbo.distributionlit values ("
     + "?,"   //   agent	C(50),
     + "?,"   //   agentname	C(200),
     + "?,"   //   clientid	C(50),
     + "?,"   //   clientname	C(200),
     + "?,"   //	prodid	C(50),
     + "?,"   //   prodname	C(200),
     + "getdate() ,"   //	sentdate	D,
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
     	Connection con = getNewConnection();
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
		      ps.setDouble(25, lw.getLine().getNormalPrice());  // + "?,"   //	NormalPr	N,
		      ps.setDouble(26, lw.getLine().getPromoPrice());//+ "?,"   //	ActionPr	N,
		      ps.setString(27, lw.getLine().getComment());//   + "?,"   //	comment	C(200),	reserv1	C(200),	reserv2	C(200),
		      ps.setString(28, lw.getLine().getReserv1());
		      ps.setString(29, lw.getLine().getReserv2());
		      ps.setInt(30, lw.getLine().getResint1());// + "?,"   //	resint1	I,resint2	I,
             ps.setInt(31, lw.getLine().getResint2());
             ps.setDouble(32, lw.getLine().getResdouble1());// + "?,"   //	resdou1	N,	resdou2	N,
             ps.setDouble(33, lw.getLine().getResdouble2());
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

 
   public Vector getFixedAssortmentData() {
           Vector lines = new Vector();
/*
           try {
          	  String cmd = " SELECT  [Asortimento kodas] AS grupp,[Prek�s Nr_] AS toode  FROM [CRONUS Navision Express$Preki� asortimentas] r" +
          	  		" INNER JOIN [CRONUS Navision Express$Item] t ON t.[No_]=r.[Prek�s Nr_]  ORDER BY [Search Description]";
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
  */         return lines;
   }



   public DealerProduct DealerProductsList() {
     return null;
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
     String cmd = " SELECT  [Code] AS kood,Name AS nimi FROM Mobilejava.ItemCategory order by [Name]";
     	  try {
       	 Connection con = getNewConnection2();
       	System.out.println(cmd);
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(cmd);
   	        while(rs.next()){
   	            String id = rs.getString("kood").trim();
   	            String name = rs.getString("nimi") ;
   	            if(name != null && name.trim().length() != 0){
      	             tm.put(id, Converter.enCodeEst(name.trim()));      	             	
   	            }
   	        }
   	     tm.put("MUU", "MUU");
   	      rs.close();
   	      con.close();

        } catch (Exception e){
          e.printStackTrace();
        }

         return tm;
   }



   public TreeMap getProdGroupsSecond() throws SQLException {
    TreeMap   tm = new TreeMap();
    String cmd = "SELECT  [Code] AS kood,Name AS nimi FROM Mobilejava.ProductGroup";
    System.out.println(cmd);
    try {
    	 Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	            String groupID = rs.getString("kood");           
	            String name = rs.getString("nimi") ;
	            if(name != null && name.trim().length() != 0){
     	             tm.put(groupID,  Converter.enCodeEst(name.trim()));      	             	
  	            }
	           
	        }
            tm.put("MUU", "MUU");
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

        String cmd = "SELECT Code AS kood,Name AS nimi FROM " + 
		" Mobilejava.Salesperson";
        
        Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
        
        
	    if (isDebug()) System.out.println(cmd);
	    TreeMap agents = new TreeMap();
	    try {
	        Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	          Agent agent = new Agent(Converter.cleanLitLatEst(rs.getString("kood").trim()));
	          String name = rs.getString("nimi").trim();
	        //  agent.setObject(rs.getString("objekt"));
	          agent.setName(name);

	            FileReader fr2 = new FileReader(getDir() + "aggroups.txt");
	            BufferedReader reader2 = new BufferedReader(fr2);
	          String line2 = null;

	          while((line2 = reader2.readLine())!= null ){
	              StringTokenizer st2 = new StringTokenizer(line2, ";");
	           
	               String group = (st2.nextToken());
	               
	               while(st2.hasMoreTokens()){
	               	String ag = st2.nextToken();
	               	if(ag.equals(agent.getID())){
	               	//   System.out.println(ag + " " +group);
	               	 	agent.addProdGroupList(group); 		
	               	}
	              }
	          }
	          

	          agent.setLanguage("EST");
	          agent.setCountry("ee");
	          agent.setPassWord("0000");
	          String email = " ";
	          
	          if(propse.get(agent.getID()) != null){
	        	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
	          }
	          
	          if(email.length() > 3){
	        	  agent.setEmail(email);
	          }
	        
	          if(email.length() > 0){
	        	  agent.setActive(true);
	          }
	        
	        
	          
	       //   System.out.println("GRUPID "  +agent.getID() + " " + agent.getProdGroupList().toString());
			    
				String agentAGENTS = getParent().getProps().getProperty("dub." +agent.getID());
				if(agentAGENTS != null){
    	           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
			    while(stag.hasMoreTokens()){  	
                 agent.addDubbler(stag.nextToken());
              //   System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
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
	        
	        
	  cmd = "select PayerCode as kood ,sum(Amount) as yle  from Mobilejava.OpenInvoices where DueDate > getdate() group by PayerCode" ;
	  System.out.println(cmd);
	         con = getNewConnection2();
	        Statement stmt1 = con.createStatement();
	        ResultSet rs1 = stmt1.executeQuery(cmd);
	        while(rs1.next()){

	          String id = rs1.getString("kood");
	          Debt debt = new Debt ();
	         // debt.setDoknr(clear(st2.nextToken()));
	          debt.setDue(rs1.getString("yle"));
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

   
      String cmd = "select CustomerCode AS klient, PayerCode AS maksja,DocumentNo AS number, DueDate AS kuupaev, Amount AS summa,  " +
       " DATEDIFF(dd,DueDate,GETDATE()) AS due,PayerCode AS payer FROM Mobilejava.OpenInvoices where DueDate < getdate()-5 " +
	"	order by CustomerCode,DueDate " ;
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







   String sql =  " SELECT Name AS nimi,Code AS kood,PayerCode as payer ,Address+' '+City AS aadress ,Address as tanav,City,SalespersonCode AS agent , " +
	" WebPassword AS agent2,Blocked AS blokk, PaymentTermsCode AS payment,0 AS alekood, Balance AS yle,VisitDay AS visit,'' AS type, " +
	 "  0 AS extradays,'' AS sort,Balance AS debt , 0 AS minsum ,Contact +' ' + Phone as kontakt  " +
	 " FROM Mobilejava.Customer" ;

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
           customer.setName( Converter.enCodeEst(name));
           customer.restName = name;
           	payer = rs.getString("payer");
           	if(payer != null && payer.trim().length()>0){
             customer.setPayer(payer.trim());
             }

             String aadress = rs.getString("aadress");
             if( aadress != null && aadress.trim().length()>0){
               customer.setAddress( Converter.adressLink(Converter.enCodeEst(aadress),"Estonia"));
               customer.setName(customer.getName()+ " " + Converter.enCodeEst(aadress));
             }
            aadress = rs.getString("tanav");
             if( aadress != null && aadress.trim().length()>0){
            	 customer.restStreet = aadress.trim();
             }
             aadress = rs.getString("city");
             if( aadress != null && aadress.trim().length()>0){
            	 customer.restCity = aadress.trim();
            	 customer.setCountry("Estonia");
             }
             
         	String ag = Converter.cleanLitLatEst(rs.getString("agent"));
             customer.addAgent(new Agent(ag));
             
             String ag2 = rs.getString("agent2");
             
             if (ag2 != null && ag2.trim().length()>0){
            	 customer.addAgent(new Agent(Converter.cleanLitLatEst(ag2))); 
             }
             if (ag != null && ag.trim().length()>0){
 				Agent agg = ((Agent)this.getAgents1().get(ag.trim()));
 				
 				if(agg != null && agg.getDubbler().size()>0){
 					System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
 					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
 						customer.addAgent(new Agent(Converter.cleanLitLatEst((String)agg.getDubbler().elementAt(i))));
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
         
  
                String sql3 =  " select PayerCode as kood ,sum(Amount) as yle  " +
                		" from Mobilejava.OpenInvoices where DueDate > getdate() and PayerCode = '" +
                		customer.getPayer() + "' group by PayerCode " ;


		    try {
		      Connection con3 = getNewConnection2();
		      Statement stmt3 = con3.createStatement();
		      ResultSet rs3 = stmt3.executeQuery(sql3);
		      while(rs3.next()){
		      
		         double sum = rs3.getDouble("yle");
		         if(sum !=0){
		         	customer.setDebt(sum);
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
               customer.setPersonalData( Converter.enCodeEst(cont.trim()));   
           }
          
           this.customers.add(customer.getID());
           tm.put(customer.getName(), customer);
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
     	String cmd = " SELECT [Code] AS toode,ltrim(ItemCategoryCode) + ' ' +  ltrim(ProductGroupCode) + ' ' +  ltrim(PrintoutType) + ' '  + ltrim(PrintoutOption)  as nimi" +
       "  , SalesUnitVolume , lower(ltrim( SalesVolumeUnitMeasure)) as nimi3 " +
       " , UnitPrice AS myygihind, ItemCategoryCode AS grupp," +
       "  case when ItemCategoryCode= 'MUU' then 'MUU' else ProductGroupCode end AS grupp2" +  
	   "	,BaseUnitMeasure AS unit,CrossReferenceNo AS ean,'' AS status ,0 AS maxdiscount, " +
	  "	  '' AS type, [QtyPerUnitMeasure] AS kastis,  '' AS kaal, VATPrc AS vat, QtyAvailable as kogus,ItemDimensionName as tyyp  " +
	  "	   FROM Mobilejava.Item where Description != '' and Blocked != 1  and ForPDA = 1";
      System.out.println(cmd);
 
     	Connection con = getNewConnection2();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode").trim();
            Product prod = new Product(id);
         	String name = rs.getString("nimi");
         	name = name.trim();
         	name = name.replaceAll("  ", " ");
         	String sisu = " " + this.getNF().format(rs.getDouble("SalesUnitVolume")) +  rs.getString("nimi3");
         	try{
         		name =  "    " + name.substring(0,1) + (name.substring(1,name.length())).toLowerCase() + sisu + " " + id;
                		
         	}catch(Exception ll){
         		
         	}
            double price = rs.getDouble("myygihind");
                prod.setPrice(price);
              
                String grupp = cleardots(rs.getString("grupp"));
                prod.setGroup(parent.make(grupp));
                String grupp2 = cleardots(rs.getString("grupp2"));
                prod.setGroupSecond(parent.make(grupp2));
 
                String unit = rs.getString("unit");
                if (unit != null && unit.trim().length()>0){
                	prod.setUnit(unit.trim());                   	
                }
                
              String ean = (rs.getString("ean"));
                 if (ean.trim().length()>0){
                   prod.setEAN(ean);
                   eanprods.put(ean.trim(), prod.getID());
                 }
      
                double	cases = rs.getDouble("kastis");
                	if(cases>0){
                      prod.setInCases(cases);
                	}

          	 String vat = "" + rs.getInt("vat");
                   	prod.setVat(vat);
                      
                    double	kogus = rs.getDouble("kogus");
                	if(kogus>0){
                      prod.setStock(kogus);
                	}else{
                		prod.setStock(0);
                	}
                   
                	String tyyp = rs.getString("tyyp");
                	if(tyyp != null && tyyp.trim().length() >0){
                		prod.setType(tyyp.trim());
                	}
                	
            prod.setName( Converter.enCodeEst(name));
            tm.put(prod.getName(), prod);
        }


        
        
        
       /* try {
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
		}
        */
        
        rs.close();
        con.close();
     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }

   
   private String getDir(){
	      String dir = getParent().getProps().getProperty("readin");
	      if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	      return dir;
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
    StringBuffer sb = new StringBuffer();
    return sb.toString();
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     HashMap kliendid = new HashMap();
     try {
    String cmd =  "select CustomerCode AS klient,ItemCode AS toode,Quantity AS kogus, LastDate AS lastdate " +
      " from Mobilejava.CustomerProducts where ItemCode != '' order by CustomerCode,ItemCode,LastDate desc" ;
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

          String cmd2 =  " select CustomerCode AS klient,ItemCode AS toode,Quantity AS kogus, LastDate AS lastdate " +
 " from Mobilejava.CustomerProducts where ItemCode != '' order by CustomerCode,ItemCode,LastDate desc ";
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
     return "NaviKP";
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
   private String url = "jdbc:odbc:report";
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
  	 	String cmd = "select Agent as agent ,SalesAmount as summa from Mobilejava.MonthSales";
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
