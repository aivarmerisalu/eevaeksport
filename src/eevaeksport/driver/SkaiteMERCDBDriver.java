/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002fget
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class SkaiteMERCDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Discounter{


    public SkaiteMERCDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
	// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();
	    Converter conv = new Converter();
	    
	    questIDs = conv.getQuestActiveIDs(this.getExcelDir());
	   if( questIDs != null){
		   System.out.println(questIDs.toString());
	   }else {
		   System.out.println("guestID" + questIDs );
	   }
	   
	} catch (Exception e) {
	    parent.showError(e);
	}

	company = getParent().getProps().getProperty("companycode");
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  private String company = null;
	
    public String getProductSortingData(){
       	return null;
       }
	private Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(url2, getParent().getProps()
				.getProperty("dsn.user"),
				getParent().getProps().getProperty("dsn.pass"));
	}
	
	private HashMap questIDs = null;
    
    public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr) {
		String sql = "INSERT into supervisor values (" + "?, " // agent C(50),
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
				+ "?)"; // resint3 L

		try {
			System.out.println("jam");
			Connection ccc = this.getNewConnection();
			PreparedStatement ps = ccc.prepareStatement(sql);

			for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext();) {
				eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
				ps.setString(1, wr.getInfo().getAgentID());
				ps.setString(2, Converter.deCode(wr.getInfo().getAgentName()));
				ps.setString(3, lw.getLine().getCustID());
				ps.setString(4, Converter.deCode(lw.getLine().getCustName()));
				ps.setString(5, lw.getLine().getProdID());
				ps.setString(6, Converter.deCode(lw.getLine().getProdName()));
				ps.setDouble(7, lw.getLine().getNormalPrice());
				ps.setDouble(8, lw.getLine().getAmount());
				String tyyp = (String) questIDs.get(lw.getLine().getProdID());
				double type = 0;
				try{
					type = Double.parseDouble(tyyp);
				}catch(Exception e){
					
				}
				ps.setDouble(9, type);
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
				ps.setDouble(20, lw.getLine().getAction1Eval() * 100);
				ps.setDouble(21, lw.getLine().getAction2Eval() * 100);
				ps.setDouble(22, lw.getLine().getActionTotalEval() * 100);
				ps.setDouble(23, lw.getLine().getCustEval() * 100);
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
				ps.setString(32, cr3);
				ps.setInt(33, lw.getLine().getResInt1());
				ps.setInt(34, lw.getLine().getResInt2());
				ps.setInt(35, lw.getLine().getResInt3());
				ps.executeUpdate();
			}

			ps.close();
		ccc.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

    /**
     *
     */
    public void connectionUp(){
      connectionUp(this.url);
    }
    public String getSaveDebtInfo() {
       	return null;
       }
       
    public String getAssortmentStr(Agent agent){
    	return null;
    }

            public String getDealerCustomerCSV(String agent){
             return null;
             }


        public String getSaveStockInfo(){
        	  connectionUp(url);
        	  String  cmd = "  SELECT  productcode AS toode ,w.unitsinstockfree AS stock, w.warehouseasid AS ladu  " +  
	           " FROM asVwWareHouse w INNER JOIN   asVwProduct t ON w.productasid=t.productasid " +
               " WHERE unitsinstockfree !=0  ORDER BY warehouseasid" ;


		       if (isDebug()) System.out.println(cmd);
		       StringBuffer products = new StringBuffer();
		       try {

		         Statement st = con.createStatement();
		         ResultSet rs = st.executeQuery(cmd);

		         while (rs.next()) {

		           String kood = rs.getString("toode").trim();
		           double seis = rs.getDouble("stock");
		           String dealer = rs.getString("LADU").trim();
		           if(seis > 0 && !dealer.startsWith("EQB")){
		               products.append(kood +  ";");
		               products.append(dealer + ";");
		               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
		             }
		           }
		           rs.close();
		           st.close();
		           con.close();
		       } catch (Exception e) {
		           getParent().showError(e);
		       }
		       return products.toString();
		   }
        
                                           public String getCustProdDiscounts(Agent agent){
                                            return null;
                                            }

                                       	public Vector getLastThreeData() {
                                    		// TODO Auto-generated method stub
                                    		return null;
                                    	}
        public String getDistribution() {
          return null;
        }

        public Vector getFixedAssortmentData() {
                Vector lines = new Vector();

                try {
               	  String cmd = " SELECT  PriceListAsId AS grupp,t.ProductCode AS toode FROM asVwPriceListProducts r  " +
               	  		" INNER JOIN asVwProduct t ON t.ProductAsId=r.ProductAsId WHERE LevelNo is not null ORDER BY ProductName";
               	System.out.println(cmd);
                  	connectionUp(url);;
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

    private void connectionUp(String url){
      try {
        if (con != null){
          con.close();
        }
           con = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
           getParent().getProps().getProperty("dsn.pass"));
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
        String    sql = " SELECT prodgroupcode AS liik ,left(groupname,3) + '   ' + right(groupname,len(groupname)-3) AS nimetus ,(select ProdGroupCode from  asVwProdGroup gr where g.ProdGroupParentAsId= " +
                     " gr.ProdGroupAsId ) as parent from dbo.asVwProdGroup g where ProdGroupParentAsId is null " +
         " ORDER BY prodgroupcode";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String kood= rs.getString("liik").trim();
            tm.put(kood, enCode(rs.getString("nimetus").trim()));
        }
        
           sql = "  SELECT g.prodgroupcode AS liik ,g.groupname AS nimetus , " +
		" (select gr.groupname from  asVwProdGroup gr where g.ProdGroupParentAsId= gr.ProdGroupAsId ) " +
		" as parent,ProdGroupParentAsId as id from dbo.asVwProdGroup g " +
		" where g.ProdGroupParentAsId>0  ORDER BY g.prodgroupcode";
    st = con.createStatement();
    rs = st.executeQuery(sql);

    while (rs.next()) {
      String kood= rs.getString("liik").trim();
      String nimi = rs.getString("nimetus").trim() +" " + kood;
      if(kood.length()>0 ){
      	String nimeosa = rs.getString("parent");
      	if(nimeosa != null && nimeosa.length()>3){
      		nimi = nimeosa.substring(0,3) + " " + nimi;	
      	}
      	tm.put(kood,enCode(nimi));
      }
      
    }
        
        rs.close();
        st.close();
        con.close();
        this.connectionDown();
        return tm;
 }
    
    
    public TreeMap getProdGroupsSecond() throws SQLException {
    	/*connectionUp(url);
        TreeMap   tm = new TreeMap();
        String    sql = "  SELECT g.prodgroupcode AS liik ,g.groupname AS nimetus , " +
			" (select gr.groupname from  asVwProdGroup gr where g.ProdGroupParentAsId= gr.ProdGroupAsId ) " +
			" as parent,ProdGroupParentAsId as id from dbo.asVwProdGroup g " +
			" where g.ProdGroupParentAsId>0  ORDER BY g.prodgroupcode";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String kood= rs.getString("liik").trim();
          String nimi = rs.getString("nimetus").trim() +" " + kood;
          if(kood.length()>0 ){
          	String nimeosa = rs.getString("parent");
          	if(nimeosa != null && nimeosa.length()>3){
          		nimi = nimeosa.substring(0,3) + " " + nimi;	
          	}
          	tm.put(kood,enCode(nimi));
          }
          
        }
        rs.close();
        st.close();
        con.close();
        this.connectionDown();*/
        return null;
 }

    public TreeMap getAgents() {
    
        connectionUp(url);
    	TreeMap agents = new TreeMap();
        try {  
        TreeMap gr = new TreeMap();
        try{
        	 String sss = "select pricelistasid as id,t.productcode as prod from dbo.asVwPriceListProducts " +
 " pr INNER JOIN asVwProduct t ON pr.productasid=t.productasid where  productcode not like '*%' "+
   "     ORDER BY pricelistasid,productcode";
             
        	  String uus  = "SELECT   ProductCode AS gr , PriceListasId AS id FROM vMRS_PriceListsProducts  "+
           	" ORDER BY PriceListasId, ProductCode";
             
            PreparedStatement stmt2 = con.prepareStatement(uus);
     	    ResultSet	      rs2 = stmt2.executeQuery();
     	    while (rs2.next()) {
     	    	String gru = rs2.getString("gr").trim();
     	    	String id = rs2.getString("id").trim();
     	    	
     	    	Vector vec = (Vector)gr.get(id);
     	    	if(vec == null){
     	    		vec = new Vector();
     	    	}
     	    	vec.add(gru);
     	    	gr.put(id,vec);
     	    }
     	    rs2.close();
     	    stmt2.close();

        }catch(Exception e){
        e.printStackTrace();	
        }
       
        
        
        String  cmd = "SELECT  employeeasid ,employeecode AS yksus,empname AS nimi,warehouseno AS ladu , " +
        " pricelistasid AS hinnakiri FROM asVwEmployee WHERE employeecode not like 'zz%' order by empname";

        Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
        
        

            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim() );
              String nimi = agent.getID() + " " +  rs.getString("nimi").trim();
              agent.setName(nimi);
              
              agent.setObject(parent.make(rs.getString("ladu")));

              agent.setLanguage("LIT");
              agent.setCountry("lt");
              agent.setPassWord("0000");
String email = " ";
              
              if(propse.get(agent.getID()) != null){
            	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
              }
              
              
              if(email.length() > 3){
            	  agent.setEmail(email);
              }
              if(email.trim().length() > 1){
            	  agent.setActive(true);
              }
              
         
              String hk = parent.make(rs.getString("hinnakiri"));
              Vector ss = (Vector)gr.get(hk);
              if(ss != null){
              	for(int i=0;i<ss.size();i++){
              		String gg = (String)ss.get(i);
              		agent.addProdGroupList(gg);
              		if(!grouplist.contains(gg)){
                  		grouplist.addElement(gg);	
              		}
              	}
              }
              agent.setIsPilot(true);
              agents.put(nimi, agent);
         
	    }
	    
	 

	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
        if(rcompany != null){
	    	Converter.sendAgentJson(agents, rcompany, false);
	    }
        
        return agents;
    }

    public Vector getPriceMatrixData()  {
        Vector lines = new Vector();
       /*  String    sql = " SELECT SL01008 AS custid,'' AS clgroup,SC18003 + '03' AS prodid,'' AS prodgroup,SC18009, " +
         		"(case when SY24003=null then '0' else SY24003 end ) AS discount,SC18008 AS price " +
                " FROM SC180100 INNER JOIN SL010100 ON  left(SC18002,4)= SL01001 LEFT JOIN SY240100 on SC18009=SY24002 and SY24001='BG' WHERE  " +
                " SC18010 <= getdate() AND (SC18011 >= getdate() OR SC18011 = null)  and len(SL01008) >0  ";
           Statement st = null;
           try {
             connectionUp(url);
             st = con.createStatement();
             ResultSet rs = st.executeQuery(sql);

             while (rs.next()) {

               String custID = rs.getString("custid").trim();
               String clGroup = rs.getString("clgroup");
               String prodID = rs.getString("prodid").trim();
               String prodGroup = rs.getString("prodgroup");
               String sss = rs.getString("SC18009");
               String discount = rs.getString("discount");
               if(discount == null){
               	discount = sss.trim();
               }else{
               	discount = discount.trim().replaceAll("%"," ").trim();
               }
			   
               double price = rs.getDouble("price");
               MatrixLine mline = new MatrixLine();

               if (custID != null && custID.trim().length() > 0) {
                 mline.setCustID(custID.trim());
               }
               if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                 mline.setCliGroup(clGroup.trim());
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
              lines.add(mline);
             }

             rs.close();
             st.close();
             connectionDown();
           }
           catch (SQLException ex) {
             ex.printStackTrace();
           }*/
         return lines;
    	
       }

    private Vector getBlocked(){
    	Vector vec = new Vector();
    	
try{
	  String  cmd = "select RefID,max(data) as kuup  from PartneriuBlokai where data > getdate()-356 group by RefID";
   
	    HashMap vahe = new HashMap();
	  
        stmt = con.prepareStatement(cmd);
	    ResultSet rs = stmt.executeQuery();

	    while (rs.next()) {
	    	vahe.put(rs.getString("RefID").trim(), rs.getString("kuup").trim());
	    }
	    
	    System.out.println("Vahe suurus " +  vahe.size());
	    rs.close();
	    
	    
	    Iterator itera = vahe.keySet().iterator();
	    while (itera.hasNext()){
	    	String key = (String)itera.next();
            String data = (String) vahe.get(key);
            
            String sql = "select Kodas as kood from PartneriuBlokai bl   INNER JOIN Partneriai AS part ON part.PartneriaiId  = bl.RefID " + 
             " where RefID =" + key + " and data ='" + data + "' and BlokoTipas = 1";
		    stmt = con.prepareStatement(sql);
		     rs = stmt.executeQuery();

		    if (rs.next()) {
		    	vec.add(rs.getString("kood"));
		    }
		    
	
	    }
	    	    
}catch(Exception e){
	
}
          

          
    	return vec ;
    }
    
   private TreeMap getCodes(){
	   TreeMap res = new TreeMap();
	   
	   String cmd = "SELECT kl.customercode AS kood  FROM E_Partner.asVwMJCustomer kl INNER JOIN " +
       " asVwEmployee a  ON a.employeeasid=kl.employeeasid " +
       " INNER JOIN Partneriai AS part  ON part.PartneriaiId  = kl.CustomerASid " + 
       " LEFT JOIN PL_Partneriai pl ON part.PartneriaiId = pl.PartneriaiId " +
       " LEFT JOIN IvNormatyvuReiksmes ivnr ON pl.TIPAS6ID = ivnr.IvNormatyvuReiksmesId " +
       " where ivnr.Pavad !='NENAUDOJAMAS'  ORDER BY kl.customercode asc ";
	   try {
		   Connection con2 = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
		           getParent().getProps().getProperty("dsn.pass"));
	      PreparedStatement  stmt = con2.prepareStatement(cmd);
	      ResultSet	      rs = stmt.executeQuery();
	  
	    if (rs.next()) {
	    	String kood = (rs.getString("kood").replaceAll("\""," "));
	    	res.put(Converter.cleanLitLatEst(kood), kood);
	    }	
	    rs.close();
	    stmt.close();
	    con2.close();
	    
	   }catch(Exception d){
		   d.printStackTrace();
	   }
				
	   return res;
   }
    public Customer getCustomerSingle(String custid) {
        connectionUp(url);
  
        TreeMap codes = getCodes();
        
        if(codes.get(custid) != null){
        	custid = (String)codes.get(custid);
        	System.out.println("Vahetasin koodi " + custid);
        }
        
        String cmd = "SELECT kl.customercode AS kood, kl.customername  AS nimi,regno as payer,kl.address + ' ' + rtrim(kl.city) AS aadress,  " +
       " RegionName  as grupp, StoreChainAsID as jure ,ivnr.Pavad as tyyp FROM E_Partner.asVwMJCustomer kl INNER JOIN " +
       " asVwEmployee a  ON a.employeeasid=kl.employeeasid " +
       " INNER JOIN Partneriai AS part  ON part.PartneriaiId  = kl.CustomerASid " + 
       " LEFT JOIN PL_Partneriai pl ON part.PartneriaiId = pl.PartneriaiId " +
       " LEFT JOIN IvNormatyvuReiksmes ivnr ON pl.TIPAS6ID = ivnr.IvNormatyvuReiksmesId " +
       " LEFT JOIN  asVwRegion reg on reg.RegionAsId  = kl.RegionAsId " +
       " where ivnr.Pavad !='NENAUDOJAMAS' and kl.customercode = '" + custid + "' ORDER BY kl.customercode asc ";
	log(cmd);

    
	  Customer cust = null;
	try {
		Connection con2 = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
		           getParent().getProps().getProperty("dsn.pass"));
          PreparedStatement  stmt = con2.prepareStatement(cmd);
          
	    ResultSet	      rs = stmt.executeQuery();
	  
	    if (rs.next()) {
	    	String kood = (rs.getString("kood").replaceAll("\""," ").trim());
	    	String nimi = (rs.getString("nimi").replaceAll("\""," ").trim());
	    	String payer = (rs.getString("payer").replaceAll("\""," ").trim());
			String addr = (rs.getString("aadress").replaceAll("\""," ").trim());
			String region = enCode(rs.getString("grupp").replaceAll("\""," ").trim());
			String chain = enCode(rs.getString("jure").replaceAll("\""," ").trim());
		
			
		/*	custname = cc.getName();
        	chain = cc.getJuridics();
            region = cc.getGroup();
          */  
	                cust = new Customer(kood);
	                cust.setPayer(payer);
	                if(nimi.startsWith("UAB ")){
	                	nimi = (nimi.substring(4,nimi.length()) + " UAB").trim();
	                }
	                cust.setName(nimi);
	                cust.setAddress(addr);
	                cust.setGroup(region);
	                cust.setJuridics(chain);
	             	
	    }
	    rs.close();
	    stmt.close();
	    con2.close();
	 }catch(Exception aa){
     aa.printStackTrace();	
     }	

		return cust;
	}

	
    public TreeMap getCustomers(Agent agent){
        connectionUp(url);
        
        Converter conv = new Converter();
  	  HashMap kyssad =   conv.getQuestionaire(this.getExcelDir());
  
        String cmd = "SELECT kl.customername  AS nimi,kl.address as addr,regno as payer,kl.customercode AS kood,kl.address + ' ' + rtrim(kl.city) AS aadress, " +
     "   paymenttime AS term,'' AS ale, debt , " +
     "   ' ' AS visit, custphone AS info ,a.employeecode as agent, kl.PriceListAsId as grupp ,ivnr.Pavad as tyyp ," +
     "   part.Nenaudoti as aktiivne FROM E_Partner.asVwMJCustomer kl INNER JOIN " +
     "   asVwEmployee a  ON a.employeeasid=kl.employeeasid " +
     "   INNER JOIN Partneriai AS part  ON part.PartneriaiId  = kl.CustomerASid  " +
     "   LEFT JOIN PL_Partneriai pl ON part.PartneriaiId = pl.PartneriaiId" +
     "   LEFT JOIN IvNormatyvuReiksmes ivnr ON pl.TIPAS6ID = ivnr.IvNormatyvuReiksmesId" +
     "  where ivnr.Pavad !='NENAUDOJAMAS' ORDER BY kl.customercode asc";
	log(cmd);

	TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
  
	try {
		Connection con2 = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
		           getParent().getProps().getProperty("dsn.pass"));
          PreparedStatement  stmt = con2.prepareStatement(cmd);
            if (agent != null){
//              stmt.setString(1, agent.getID());
            }
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
		String nimi = enCode(rs.getString("nimi").replaceAll("\""," ").replaceAll("-", "").trim());
		String addr = enCode(rs.getString("addr").replaceAll("\""," ").trim());
		String payer = enCode(rs.getString("payer").replaceAll("\""," ").trim());
	
                Customer cust = new Customer(Converter.cleanLitLatEst(rs.getString("kood").trim()));
                conv.addQuestiosToCustomer(cust,kyssad);
                cust.setPayer(payer);
                if(nimi.startsWith("UAB ")){
                	nimi = (nimi.substring(4,nimi.length()) + " UAB").trim();
                }
                cust.setName(nimi + " " + addr);
                try{
                	String aadr = enCode(rs.getString("aadress").trim());
                	aadr = Converter.adressLink(aadr, "Lithuania");
                	 cust.setAddress(aadr);
                }catch(Exception aa){
                	
                }
               
                
                
               /* String ale = rs.getString("ale");
                if (ale != null && ale.length() > 0){
                  cust.setDiscount(Double.parseDouble(ale.trim()));
                }*/
                String term = rs.getString("term");
                cust.setPaymTerm(parent.make(term));
            
                double debti = rs.getDouble("debt");
                if(debti > 0.05 || debti < -0.05){
                	//cust.setDebt(debti);
                }
                //cust.setVisit(rs.getString("visit"));
                
                String info = rs.getString("info");
                cust.setPersonalData(enCode(parent.make(info)));
                
                String agen = rs.getString("agent");
                if(agen != null){
                  cust.addAgent(new Agent(agen.trim()));
                }
                //cust.addAgent(new Agent("HOUSE"));
                String group = rs.getString("grupp");
                if(group != null){
                  cust.setGroup(group.trim());
                }
                String tyyp = rs.getString("tyyp");
                int aktive = rs.getInt("aktiivne");
                boolean goes = true;
                if(tyyp != null && tyyp.trim().length() >0 && tyyp.trim().equals("NENAUDOJAMAS")){
                   goes = false;
                }
                if(aktive == 1){
                	goes = false;
                }
                if(goes){
                	 customers.put(cust.getName(), cust);
                     custMap.put(cust.getID(), cust);	
                }
               
	    }
            rs.close();
            stmt.close();
            


            String sql = " select kl.customercode AS klient,a.employeecode as agent from  " +
              " asVwDepCustomers si INNER JOIN asVwCustomer kl ON kl.customerasid=si.customerasid " +
              " INNER JOIN asVwEmployee a  ON a.employeeasid=si.employeeasid ";

            stmt = con2.prepareStatement(sql);

            rs = stmt.executeQuery();
            while(rs.next()){
              String id = Converter.cleanLitLatEst(rs.getString("klient").trim());
               Agent aa = new Agent(rs.getString("agent").trim());
              Customer cust = (Customer)custMap.get(id);
              if (cust != null){
                cust.addAgent(aa);
              }
            }
            rs.close();
            stmt.close();
            con2.close();
         
        
	} catch (Exception e) {
            getParent().showError(e);
        }

	return customers;
    }

    public DealerProduct getDealerProductsList() {


        DealerProduct dealerProds = new DealerProduct();
        try {


          String  sql = "  SELECT  productcode AS toode ,w.unitsinstockfree AS stock, w.warehouseasid AS ladu  " +  
		           " FROM asVwWareHouse w INNER JOIN   asVwProduct t ON w.productasid=t.productasid " +
                   " WHERE unitsinstockfree !=0  ORDER BY warehouseasid" ;

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
        
       }
    
    private String enCode(String res ){
	  	 

    	res =res.replaceAll("�","'");
    	res =res.replaceAll("�","'");
    	res =res.replaceAll("�","'");
    	
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
	    res = res.replaceAll("�","11123xx");
        res = res.replaceAll("�","11124xx");
        res = res.replaceAll("�","11125xx");
        res = res.replaceAll("�","11126xx");        
        
        res = res.replaceAll("�","11127xx");
        res = res.replaceAll("�","11128xx");
        res = res.replaceAll("�","11129xx");	
        res = res.replaceAll("�","11130xx");
        res = res.replaceAll("�","11131xx");
        res = res.replaceAll("�","11132xx");       
        res = res.replaceAll("�","11133xx");
        res = res.replaceAll("�","11134xx");        
        res = res.replaceAll("�","11135xx");
        res = res.replaceAll("�","11136xx");      
        res = res.replaceAll("�","11137xx");
        res = res.replaceAll("�","11138xx");        
        res = res.replaceAll("�","11139xx");
        res = res.replaceAll("�","11140xx");
        res = res.replaceAll("�","11141xx");
        res = res.replaceAll("�","11142xx");
        res = res.replaceAll("�","11143xx");
        res = res.replaceAll("�","11144xx");   
        res = res.replaceAll("�","11145xx");
        res = res.replaceAll("�","11146xx");      
        res = res.replaceAll("�","11147xx");
        res = res.replaceAll("�","11148xx");
        res = res.replaceAll("�","11148xx");
        
	  	return res;
	  }
    
    public TreeMap getDealers() {
        return dealers;
      }
    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
     /*
      String sql = "select  CustomerCode AS klient, billcode AS number,billissuedate+paymenttime AS kuupaev, " +
		" (case when OperacijosTipoId =64 then -1 else 1 end)*billamount-paidamount AS summa  , datediff(day,billissuedate+paymenttime ,getdate())as yle from " +
	  "	asVwOrder  d  where billamount-paidamount>0 and  " +
      "    billissuedate > getdate()-200   order by CustomerCode,billissuedate+paymenttime";

      connectionUp(url);
      stmt = con.prepareStatement(sql);


      ResultSet rs = stmt.executeQuery();

      while(rs.next()){
    	  try{
    		    Debt debt = new Debt();
    	        debt.setCustID(rs.getString("klient").trim());
    	     
    	        String dok = rs.getString("number");
    	        if(dok != null){
    	        	 dok = dok.trim();
    	    	        if(dok.length() >5){
    	    	        	dok = dok.substring(4);
    	    	        }
    	    	        debt.setDoknr(dok);   	    	        
    	    	        debt.setDuedate(rs.getDate("kuupaev"));
    	    	        debt.setSum(rs.getDouble("summa"));       
    	    	        debt.setDue(rs.getString("yle")); 	    	        
    	    	        if(debt.getSum()>0.5 || debt.getSum()<-0.5){
    	    	        //	list.addElement(debt);	
    	    	        }  
    	        }
    	       
    	  }catch (Exception e){
    		  e.printStackTrace();
    	  }
    
        }
      rs.close();
      stmt.close();*/
      System.out.println("debtlist size " + list.size());
      return list;
    }
    
    
    public static final String cleanLitLatEst(String ret){
        if (ret != null){
        
            ret = ret.replaceAll("�","e");
            ret = ret.replaceAll("�","I");
            ret = ret.replaceAll("�","u");
            ret = ret.replaceAll("�","c");
            ret = ret.replaceAll("�","z");
            ret = ret.replaceAll("�","s");
            ret = ret.replaceAll("�","S");
            ret = ret.replaceAll("�","c");
            ret = ret.replaceAll("�","I");
            ret = ret.replaceAll("�","u");
            ret = ret.replaceAll("�","i");
            ret = ret.replaceAll("�","c");
            ret = ret.replaceAll("�","Z");
            ret = ret.replaceAll("�","E");
            ret = ret.replaceAll("�","U");
            ret = ret.replaceAll("�","U");

            ret = ret.replaceAll("�","Z");
            ret = ret.replaceAll("�","A");
            ret = ret.replaceAll("�","a");
            ret = ret.replaceAll("�","C");

            ret = ret.replaceAll("�","c");
            ret = ret.replaceAll("�","E");
            ret = ret.replaceAll("�","e");
            ret = ret.replaceAll("�","G");
            ret = ret.replaceAll("�","g");
            ret = ret.replaceAll("�","I");
            ret = ret.replaceAll("�","i");
            ret = ret.replaceAll("�","K");
            ret = ret.replaceAll("�","k");
            ret = ret.replaceAll("�","L");
            ret = ret.replaceAll("�","l");
            ret = ret.replaceAll("�","N");
            ret = ret.replaceAll("�","n");
            
            ret = ret.replaceAll("�","o");
            ret = ret.replaceAll("�","a");
            ret = ret.replaceAll("�","o");
            ret = ret.replaceAll("�","u");
            
        	
        }
        if(ret == null || ret.trim().length()==0){
            return " ";
            }else {
            return ret;
            }        
          }
    
    
    private void makeXMLandCSVs(){
    	//kliendi XML tegemine
    	StringBuffer sb = new StringBuffer();
    	StringBuffer csv = new StringBuffer();
    	String date = getDateYYYMMDD(0);
    	
    	 connectionUp(url);
    	 String vaikimisi = parent.getProps().getProperty("unilever.defaultdaysback");
    	 if(vaikimisi == null){
    		 vaikimisi = "7";
    	 }
    	 

    	 String aeg = "d.data > getdate()-" + vaikimisi;
    	 String algus = parent.getProps().getProperty("unilever.startday");
    	 
    	 String lopp = parent.getProps().getProperty("unilever.endday");
    	 
    	 if(algus != null && lopp != null){
    		 aeg = "d.data >= '" + algus+ "'  and d.data <= '" + lopp + "'";
    	 }
    	 
    	   String cmd = "select kl.Customercode AS klient  , (kl.customername) AS nimi,(adr.indeksas)AS indeks, (kl.city) AS city , " +
                " address AS aadr,(PVMNr) as vat , kl.RegionasId as region ,reg.RegionName as regionname , " +
             " StoreChainAsId as chain,part.RegNr as regnr from asVwCustomer kl  " +
             "     INNER JOIN Partneriai AS part  ON part.PartneriaiId  = kl.CustomerASid  " +
             "     INNER JOIN Adresai AS adr     ON part.AdresoId = adr.AdresoId	" +
             "     INNER JOIN asVwRegion reg on reg.RegionAsId=kl.RegionasId " +
             "       ORDER BY kl.customercode asc ";
    	log(cmd);


    	try {
                stmt = con.prepareStatement(cmd);

    	    ResultSet	      rs = stmt.executeQuery();
    	    boolean custok = false;
              String ls = System.getProperty("line.separator");
    	    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
    	    sb.append("<ROOT>"+ ls);
    	    StringBuffer region = new StringBuffer();
    	    Vector reg = new Vector(); 
    	    csv.append("Customer Id;Name;Country;Region;Chain" + ls);
    	    region.append("Code;Name" +  ls);
    	    while (rs.next()) {
    	    	custok = true ;
    	    	String klid = rs.getString("klient").trim();
    	    	String nimi =  rs.getString("nimi").replaceAll("\""," ").replaceAll("'", " ").replaceAll("&", " and ").trim();
    	    	
    	    	  sb.append("     <CUSTOMER>"+ ls);
    	    	  sb.append("        <ID_KLIENT>" + klid + "</ID_KLIENT>" + ls);
    	    	  sb.append("        <ID_PLATNIK>" + klid + "</ID_PLATNIK>" + ls);
    	    	  sb.append("        <NAZWA_1>" + nimi + "</NAZWA_1>"+ ls);
    	    	  sb.append("        <KOD_POCZT>" +  parent.make(rs.getString("indeks")) + "</KOD_POCZT>"+ ls);
    	    	  sb.append("        <MIEJSCOW>" +  parent.make(rs.getString("city")) + "</MIEJSCOW>"+ ls);    	  
    	    	  sb.append("        <ADRES>" +  parent.make(rs.getString("aadr")).replaceAll("&", " ") + "</ADRES>"+ ls); 
    	    	  sb.append("        <NIP>" +  parent.make(rs.getString("vat")) + "</NIP>"+ ls);
    	    	  
    	    	  String regi = parent.make(rs.getString("region"));
    	    	  String reginame = parent.make(rs.getString("regionname"));
    	    	  String chaincode = parent.make(rs.getString("chain"));
    	    	  String regnr = parent.make(rs.getString("regnr"));

      	    	csv.append(cleanLitLatEst(klid) + ";" + cleanLitLatEst(nimi) + ";LT;" + cleanLitLatEst(regi) + ";" + cleanLitLatEst(chaincode) + ";" + regnr +  ls);
    	    	  
    	    	  if(!reg.contains(regi)){
    	    		 region.append(cleanLitLatEst(regi) + ";" + cleanLitLatEst(reginame) + ls ); 
    	    	  }
    	    	  reg.addElement(regi);
    	    	 // sb.append("        <ID_PLATNIK>0</ID_PLATNIK>"+ ls);
      	    	  sb.append("</CUSTOMER>"+ ls);              
    	    }
                rs.close();
                stmt.close();
                sb.append("</ROOT>");
                if(custok){
                	File f = new File(getOutDir() + "Customer.xml");
                    if (f.exists())f.delete();
                		PrintWriter os = new PrintWriter(new FileWriter(
                				getOutDir() + "Customer.xml", true));
                	String out = sb.toString();
                	byte[] someBytes = out.getBytes();
                	String encodingName = "UTF-8";
                	String s = new String ( someBytes, encodingName );
                		os.write(s);
                		os.close();
                		
                		File fcsv = new File(getOutCSVDir() + "Customer_" + date + ".csv");
                		 if (fcsv.exists())fcsv.delete();
                		PrintWriter osv = new PrintWriter(new FileWriter(
                				getOutCSVDir() + "Customer_" + date + ".csv", true));
                	String outv = csv.toString();
                	byte[] someBytesv = outv.getBytes();
                	String encodingNamev = "UTF-8";
                	s = new String ( someBytesv, encodingNamev );
                		osv.write(s);
                		osv.close();
                		
                	 fcsv = new File(getOutCSVDir() + "Region_" + date + ".csv");
               		 if (fcsv.exists())fcsv.delete();
               		 osv = new PrintWriter(new FileWriter(
               				getOutCSVDir() + "Region_" + date + ".csv", true));
               	 outv = region.toString();
                someBytesv = outv.getBytes();
               	encodingNamev = "UTF-8";
               	s = new String ( someBytesv, encodingNamev );
               		osv.write(s);
               		osv.close();
                		
                }
                
                
                String cmd2 = "SELECT  max(prodgroupparentasid) as grupp ,productcode AS toode,max(productbarcode) AS ean,max(productname) " +
     " AS nimi,max(retailprice/1.21) AS myygihind ,  " +
     "               max(kiekiomv) as unit  ,sum( w.UnitsInStock) as stock ,max(tt.Mase) as kaal  " +
     "              FROM asVwProduct t  INNER JOIN prekes tt ON tt.Kodas =productcode INNER JOIN  " +
     "                 asVwProdGroup g ON g.prodgroupasid=t.prodgroupasid  " +
     "                left join asVwWareHouse w ON w.productasid=t.productasid and  unitsinstockfree !=0  " +
     "              where (g.prodgroupparentasid in ('6598','6613','6815') or  prekiugrupesid in ('278571','395420')) and productcode not like '%**%'   " +
     "             group by t.productcode  order by t.productcode ";
                
                
    	log(cmd2);


                stmt = con.prepareStatement(cmd2);
                 sb = new StringBuffer();
                 csv = new StringBuffer();
                 StringBuffer laoseis = new StringBuffer();
    	   	      rs = stmt.executeQuery();
    	    boolean prodok = false;
    	    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
    	    sb.append("<ROOT>"+ ls);
    	    csv.append("Item Id NPF;Item name;Item Id;Unit;NetWeight" + ls);
    	    laoseis.append("Date;Item Id;Quantity;Unit"+ ls);
    	    while (rs.next()) {
    	    	prodok = true ;
    	    	String grupp = rs.getString("grupp").trim();
    	    	String prodid = rs.getString("toode").trim();
    	    	String ean = parent.make(rs.getString("ean"));
    	    	String name = rs.getString("nimi").replaceAll("\""," ").replaceAll("'", " ").replaceAll("\n", " ").replaceAll("&", " and ").trim();
    	    	String hind = parent.make(rs.getString("myygihind"));
    	    	String unit = parent.make(rs.getString("unit"));
    	    	String stock = parent.make(rs.getString("stock"))  ;
    	    	String kaal = parent.make(rs.getString("kaal"))  ;
    	    	
    	    	if(grupp.equals("6598")  || grupp.equals("6613")){
    	    		 sb.append("     <PRICLIST>"+ ls);
       	    	  sb.append("        <ID_PRODUKT>" + prodid + "</ID_PRODUKT>" + ls);
       	    	  sb.append("        <EAN>" +  ean + "</EAN>"+ ls);   	    	  
       	    	  sb.append("        <NAZWA>" +  name + "</NAZWA>"+ ls);
       	    	  sb.append("        <CENA1_NETTO_PP>" +  hind + "</CENA1_NETTO_PP>"+ ls);
       	    	  sb.append("        <WALUTA1>LTL</WALUTA1>"+ ls);
       	    	  sb.append("        <VAT>18</VAT>"+ ls); 
       	    	  sb.append("        <VAT_ZW>NO</VAT_ZW>"+ ls); 
       	    	  sb.append("        <JM>" +  unit + "</JM>"+ ls);
       	    	  sb.append("        <STAN_PP>" +  stock + "</STAN_PP>"+ ls);
         	    	  sb.append("     </PRICLIST>"+ ls);
         	    	
    	    	}
    	    	if(grupp.equals("7182") || grupp.equals("7202")){
    	    		

    	    		csv.append("npid;" + cleanLitLatEst(name) + ";" + cleanLitLatEst(prodid) + ";" + unit + ";" + kaal + ls );
    	    		laoseis.append(date + ";" + cleanLitLatEst(prodid) + ";" + stock + ";" + unit + ls);
    	    	}
    	    	 	
    	    }
                rs.close();
                stmt.close();
                sb.append("</ROOT>");
                if(prodok){
                	File f = new File(getOutDir() + "Pricelist.xml");
                    if (f.exists())f.delete();
                		PrintWriter os = new PrintWriter(new FileWriter(
                				getOutDir() + "Pricelist.xml", true));
                	String out = sb.toString();
                	byte[] someBytes = out.getBytes();
                	String encodingName = "UTF-8";
                	String s = new String ( someBytes, encodingName );
                		os.write(s);
                		os.close();
                		
                		
                		
                		File fcsv = new File(getOutCSVDir() + "Items_" + date + ".csv");
               		 if (fcsv.exists())fcsv.delete();
               		PrintWriter osv = new PrintWriter(new FileWriter(
               				getOutCSVDir() + "Items_" + date + ".csv", true));
               	String outv = csv.toString();
               	byte[] someBytesv = outv.getBytes();
               	String encodingNamev = "UTF-8";
               	s = new String ( someBytesv, encodingNamev );
               		osv.write(s);
               		osv.close();
                		
                		
               		

            	fcsv = new File(getOutCSVDir() + "Stock_" + date + ".csv");
           		 if (fcsv.exists())fcsv.delete();
           		osv = new PrintWriter(new FileWriter(
           				getOutCSVDir() + "Stock_" + date + ".csv", true));
           	 outv = laoseis.toString();
           	someBytesv = outv.getBytes();
           	encodingNamev = "UTF-8";
           	s = new String ( someBytesv, encodingNamev );
           		osv.write(s);
           		osv.close();
            		
               		
                }
                
           // Siin l�peb toodete tegemine     
                String cmd3 = "   select g.prodgroupparentasid  as grupp,d.OperacijosId as id, d.doknr AS doknr,(k.kodas) AS klient ,(k.pavad) AS kliendinimi " +
       		" ,(kl.RegionasId)  AS region ,(kl.StoreChainAsId)  AS chain  ,(d.data)  AS tehing, (d.data)  AS tellimus  , " +
    " productcode AS toode, r.kiekis AS kogus  ,retailprice as hhind,	  r.ccnuolaidosproc2 as ale, r.vvkainagalutinebepvm as mhind ,(kiekiomv) as unit ,k.RegNr as regnr ,employeecode as agent " +
    "  FROM Operacijos d  INNER JOIN Pard_BendraInf AS pbi  ON d.OperacijosId = pbi.OperacijosId " +
    "  INNER JOIN partneriai k ON k.PartneriaiId=pbi.PartneriaiId  " +
    "  INNER JOIN asVwCustomer kl ON kl.CustomerASId= k.PartneriaiId " +
    "  INNER JOIN PartneriaiPA AS partpa ON k.PartneriaiId = partpa.PartneriaiId " + 
    "  INNER JOIN MokejimoSalygos AS msal ON partpa.MokejimoSalygosId = msal.MokejimoSalygosId " +
    "  INNER JOIN asVwEmployee ag ON ag.employeeasid = kl.employeeasid " + 
    "  INNER JOIN PardavimuKunai  r  ON d.OperacijosId = r.OperacijosId  " +
    "  INNER JOIN prekes t ON t.prekesid =r.prekesid  INNER JOIN asVwProduct tt ON tt.productasid=r.prekesid " +
    "  INNER JOIN  asVwProdGroup g ON g.prodgroupasid=tt.prodgroupasid " +
    "  WHERE " + aeg + " and  ( g.prodgroupparentasid in ('6598','6613','6815') or   prekiugrupesid in ('278571','395420') )and OperacijosTipoId in ('50','51')  and productcode not like '%**%'  order by r.PardavimuKunoId";
 
                log(cmd3);
     stmt = con.prepareStatement(cmd3);
      sb = new StringBuffer();
      try{
          rs = stmt.executeQuery();
      }catch(Exception ee){
    	  try{
              rs = stmt.executeQuery();
          }catch(Exception ee1){
        	  rs = stmt.executeQuery();
          }
      }
	
 boolean dokok = false;
 sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
 sb.append("<ROOT>"+ ls);
 
 Vector heads = new Vector();

 csv = new StringBuffer();
 csv.append("Date;Invoice Id;Customer Id;Customer name;Country;Region;Chain;Currency;Invoice Amount;Invoice Amount MST;PayerCode;DateCollected;AgentCode" + ls);
 
 

 while (rs.next()) {
 	dokok = true ;
 	String grupp = rs.getString("grupp");
 	String id = rs.getString("id").trim();
 	String num = rs.getString("doknr").trim();
 	String klient =  parent.make(rs.getString("klient"));

 	String kliendinimi =  parent.make(rs.getString("kliendinimi"));
 	String regi = parent.make(rs.getString("region"));
 	String chain = parent.make(rs.getString("chain"));
 	String invoDate = getDateYYYMMDD( rs.getDate("tehing").getTime() );
 	
 	
 	   if(num.length()>0 && !num.equals("Q")){
 	
 	if(!heads.contains(num)){
 	
 	if(grupp.equals("6598") ||grupp.equals("6613")){
 	
 	  sb.append("     <DOCUHEAD>"+ ls);
 	  sb.append("        <ID_DOK>" + id + "</ID_DOK>" + ls);
 	  sb.append("        <KRAJ>LITHUANIA</KRAJ>"+ ls);
	  sb.append("        <ID_TYP>INVOICE</ID_TYP>"+ ls);
	  sb.append("        <NUMER>" + num + "</NUMER>"+ ls);
 	  sb.append("        <ID_KLIENT>" + klient + "</ID_KLIENT>"+ ls);   	    	  
 	  sb.append("        <DATA_WYST>" + invoDate + "</DATA_WYST>"+ ls);
 	  sb.append("        <DATA_SPRZ>" +  getDate( rs.getDate("tellimus").getTime() ) + "</DATA_SPRZ>"+ ls);
 	  sb.append("        <FORMA_ZAPL>P</FORMA_ZAPL> "+ ls);
   	  sb.append("     </DOCUHEAD>"+ ls);              
  	
 	}
 	
 	String regnr = parent.make(rs.getString("regnr"));
 	String agent = parent.make(rs.getString("agent"));
 	if(grupp.equals("7182") || grupp.equals("7202")){
 		csv.append( invoDate + ";" + num + ";" + cleanLitLatEst(klient) + ";" + cleanLitLatEst(kliendinimi) + ";LT;" + cleanLitLatEst(regi)+ ";" + cleanLitLatEst(chain) + ";LTL;0;0;" + regnr + ";" + date + ";"+ agent+ ls);
 	}
	
 	}
 	heads.addElement(num);
 	   }	
	}
     rs.close();
     stmt.close();
     sb.append("</ROOT>");
     if(dokok){
     	File f = new File(getOutDir() + "DOCUHEAD.XML");
         if (f.exists())f.delete();
     		PrintWriter os = new PrintWriter(new FileWriter(
     				getOutDir() + "DOCUHEAD.XML", true));
     	String out = sb.toString();
     	byte[] someBytes = out.getBytes();
     	String encodingName = "UTF-8";
     	String s = new String ( someBytes, encodingName );
     		os.write(s);
     		os.close();
     		
     		
     		File fcsv = new File(getOutCSVDir() + "Invoices_" + date + ".csv");
      		 if (fcsv.exists())fcsv.delete();
      		PrintWriter osv = new PrintWriter(new FileWriter(
      				getOutCSVDir() + "Invoices_" + date + ".csv", true));
      	String outv = csv.toString();
      	byte[] someBytesv = outv.getBytes();
      	String encodingNamev = "UTF-8";
      	s = new String ( someBytesv, encodingNamev );
      		osv.write(s);
      		osv.close();
       		
       		
     		
     		
     }
                
   //  Dokumendi p�ised L�pp
     
   //  Dokumendi read Algus
     
     
     String cmd4 = "   select g.prodgroupparentasid  as grupp, d.doknr AS doknr,d.OperacijosId as id,(k.kodas) AS klient ,(k.pavad) AS kliendinimi " +
		" ,(kl.RegionasId)  AS region ,(kl.StoreChainAsId)  AS chain  ,d.data  AS tehing, (d.data)  AS tellimus  , " +
	    " productcode AS toode,(case when OperacijosTipoId = ('51') then -1 else 1 end )*r.kiekis AS kogus  ,retailprice/1.21 as hhind,	  r.ccnuolaidosproc2 as ale, r.ccnuolaidosproc3 as ale2," +
	    "  r.ppKainaBePVM/r.kiekis as mhind ,(kiekiomv) as unit " +
	    "  FROM Operacijos d  INNER JOIN Pard_BendraInf AS pbi  ON d.OperacijosId = pbi.OperacijosId " +
	    "  INNER JOIN partneriai k ON k.PartneriaiId=pbi.PartneriaiId  " +
	    " INNER JOIN asVwCustomer kl ON kl.CustomerASId= k.PartneriaiId " +
	    "  INNER JOIN PartneriaiPA AS partpa ON k.PartneriaiId = partpa.PartneriaiId " + 
	    "  INNER JOIN MokejimoSalygos AS msal ON partpa.MokejimoSalygosId = msal.MokejimoSalygosId " +
	    "  INNER JOIN PardavimuKunai  r  ON d.OperacijosId = r.OperacijosId  " +
	    "  INNER JOIN prekes t ON t.prekesid =r.prekesid  INNER JOIN asVwProduct tt ON tt.productasid=r.prekesid " +
	    "  INNER JOIN  asVwProdGroup g ON g.prodgroupasid=tt.prodgroupasid " +
	    "  WHERE " + aeg + " and  ( g.prodgroupparentasid in ('6598','6613','6815')  or  prekiugrupesid in ('278571','395420') ) and productcode not like '%**%'" +
	    		"and OperacijosTipoId in ('50','51')  order by r.PardavimuKunoId ";
	 
             log(cmd4);
        stmt = con.prepareStatement(cmd4);
         sb = new StringBuffer();
         try{
             rs = stmt.executeQuery();
         }catch(Exception ee){
       	  try{
                 rs = stmt.executeQuery();
             }catch(Exception ee1){
           	  rs = stmt.executeQuery();
             }
         }
          boolean dokposok = false;
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        sb.append("<ROOT>"+ ls);
        
        csv = new StringBuffer();
        csv.append("Date;Invoice Id;Item Id;Qty;Sales Unit;Currency;Sales price;Discount Amount;Line Amount;Line Amount MST;DateCollected;SpecDiscount" + ls);
while (rs.next()) {
	dokposok = true ;

	String grupp = rs.getString("grupp");
	String num = rs.getString("doknr").trim();

	
	   if(num.length()>0 && !num.equals("Q") && heads.contains(num)){
		   String id =  rs.getString("id").trim() ;
			String invoDate = getDateYYYMMDD( rs.getDate("tehing").getTime() ); 
		 	String toode =  rs.getString("toode").trim() ;
		 	double kogus =  rs.getDouble("kogus") ;
		 	double priceh = rs.getDouble("hhind");

		 	double ale = rs.getDouble("ale");
		 	double ale2 = rs.getDouble("ale2");
			double pricem = rs.getDouble("mhind");
			String yhik = parent.make(rs.getString("unit"));
			
			if(grupp.equals("7182") || grupp.equals("7202")){
		 		csv.append(invoDate +";" + num + ";" + cleanLitLatEst(toode) + ";" + kogus + ";" + yhik + ";LTL;" + priceh + ";" + ale +
		 				";" + (priceh*kogus) + ";" + (pricem * kogus)+ ";" + date + ";" + ale2 + ls);
		 	} 
 		  
		 	if(grupp.equals("6598") ||grupp.equals("6613")){
		 		sb.append("     <DOCUPOSI>"+ ls);
		 		  sb.append("        <ID_DOK>" + id + "</ID_DOK>" + ls);
		 		  sb.append("        <ID_PRODUKT>" + toode + "</ID_PRODUKT>" + ls);
		 		  sb.append("        <ILOSC_PP>" + kogus + "</ILOSC_PP>" + ls);
		 		 
		 		  
		 		  sb.append("        <CENA1_N_B_PP>" +priceh + "</CENA1_N_B_PP>"+ ls);
		 		  sb.append("        <CENA1_N_PP>" +pricem + "</CENA1_N_PP>"+ ls);
		 		
		 		if(ale2 !=0){
		 			ale = ale2;
		 		}
		 		  sb.append("        <RABAT>"  + ale + " </RABAT>"+ ls);
		 		  sb.append("        <WALUTA1>LTL</WALUTA1>"+ ls);
		 		  sb.append("     </DOCUPOSI>"+ ls);  

		 	}
		 	
		 	
	   }
}
  rs.close();
  stmt.close();
  sb.append("</ROOT>");
  if(dokposok){
  	File f = new File(getOutDir() + "DOCUPOSI.XML");
      if (f.exists())f.delete();
  		PrintWriter os = new PrintWriter(new FileWriter(
  				getOutDir() + "DOCUPOSI.XML", true));
  	String out = sb.toString();
  	byte[] someBytes = out.getBytes();
  	String encodingName = "UTF-8";
  	String s = new String ( someBytes, encodingName );
  		os.write(s);
  		os.close();
  		
  		
  		
  		
  		File fcsv = new File(getOutCSVDir() + "InvoiceLines_" + date + ".csv");
 		 if (fcsv.exists())fcsv.delete();
 		PrintWriter osv = new PrintWriter(new FileWriter(
 				getOutCSVDir() + "InvoiceLines_" + date + ".csv", true));
 	String outv = csv.toString();
 	byte[] someBytesv = outv.getBytes();
 	String encodingNamev = "UTF-8";
 	s = new String ( someBytesv, encodingNamev );
 		osv.write(s);
 		osv.close();
  		
  		

  		
  		
  }
     
                
    	}  catch (Exception e){
    		e.printStackTrace();
    	}
    	
    	
    	System.exit(0);
    	
    }
	  private String getDate(long ll){
		  Calendar cal = Calendar.getInstance();
		  cal.setTimeInMillis(ll);
		    StringBuffer sb = new StringBuffer();
		    String year =   cal.get(Calendar.YEAR) + "";
		    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
		    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		     if (day.length() == 1){
		      day = "0" + day;
		    }
		    if (month.length() == 1){
		     month = "0" + month;
		   }
		    sb.append(year + "-" + month + "-" + day);
		    return sb.toString();
		  }
    
	  

	  private String getDateYYYMMDD(long ll){
		  Calendar cal = Calendar.getInstance();
		  if(ll >0)
		  cal.setTimeInMillis(ll);
		    StringBuffer sb = new StringBuffer();
		    String year =   cal.get(Calendar.YEAR) + "";
		    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
		    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		     if (day.length() == 1){
		      day = "0" + day;
		    }
		    if (month.length() == 1){
		     month = "0" + month;
		   }
		    sb.append(year + month + day);
		    return sb.toString();
		  }
    private String getOutDir(){
        String dir = getParent().getProps().getProperty("readout");
        if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
        return dir;
      }

    private String getOutCSVDir(){
        String dir = getParent().getProps().getProperty("readoutcsv");
        if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
        return dir;
      }

    public TreeMap getProducts(Agent agent){
    	
   
        connectionUp(url);


        String cmd = " SELECT pr.PrekiuGrupesID as asi,productcode AS toode,pr.Pavad AS nimi, " +
      "  (select ke.Pavad from  PrekiuKalbos ke where  ke.PrekesId = pr.PrekesId and KalbosId = 2 )as nimi2 , retailprice AS myygihind ,0 AS stock, " +
       "            ( case when (select ProdGroupCode from  asVwProdGroup gr where g.ProdGroupParentAsId= " +
        "              gr.ProdGroupAsId ) is null then  prodgroupcode else (select ProdGroupCode from  asVwProdGroup gr where g.ProdGroupParentAsId= " +
        "              gr.ProdGroupAsId ) end )AS grupp,productbarcode AS ean, prodgroupcode AS teineGrupp ,(select UnitsInStockFree  from asVwWareHouse w where " +
        "             w.WareHouseAsId ='EQBR' and ProductAsId=t.ProductAsId) AS sellout ,EQ_IPOK AS tykke,MPAL_MinKiekis as minamount, " +
        "            L_10000103  as prioriteet, " +
        "             ( case when ale.NuolaidosProc is not null then ale.NuolaidosProc else 0 end ) as maxdisc  ," +
        "           ( case when algr.NuolaidosProc is not null then algr.NuolaidosProc else 0 end ) as maxdisc2 ,  " +
        "            kiekiomv as unit, EQ_KIEKPALETEJE as alus FROM asVwProduct t INNER JOIN                        " +
        "             Prekes pr on pr.Kodas = t.productcode  LEFT JOIN asVwProdGroup g ON g.prodgroupasid=t.prodgroupasid  " +
        "            LEFT JOIN PL_PREKES a on a.prekesid=pr.prekesid " +
        "           LEFT JOIN NuolaiduInfoPrekes ale on ale.prekesid=pr.prekesid  and ale.NuolaiduAprasymaiId = 221990 " +
        "          LEFT JOIN NuolaiduInfoPrekiuGrupes algr on pr.PrekiuGrupesid=algr.PrekiuGrupesid  and algr.NuolaiduAprasymaiId = 221990 " +                   
 " where productcode not like '%*%' and  productcode not like '/%' order by prodgroupcode ";

	if (isDebug()) {
	    System.out.println(cmd);
	}

	TreeMap products = new TreeMap();

	try {
          Statement st = con.createStatement();
	    ResultSet	      rs = st.executeQuery(cmd);

	    while (rs.next()) {
	    	String aggrupp =  rs.getString("asi").trim();
		String kood = rs.getString("toode").trim();
		String linesep = System.getProperty("line.separator");
		String nimetus = rs.getString("nimi").trim().replaceAll("\"","").replaceAll("\n", " ").replaceAll("\r", " ").replaceAll(linesep, " ");
		String nimi2 =  rs.getString("nimi2");
		
		
	
                Product prod = new Product(kood);
                prod.setAgentGroup(aggrupp);
               
                try{
                    prod.setPrice(parseDouble(rs.getString("myygihind").trim())/1.21);	
                }catch(Exception ee){
                	prod.setPrice(0);
                }

              //  prod.setStock(parseDouble(rs.getString("stock").trim()));
              
                

                String gr = rs.getString("grupp");
                if(gr != null && gr.trim().length()>0){
                  prod.setGroup(gr.trim());
                }
                String  ean = rs.getString("ean");
                if(ean != null && ean.trim().length()>0){
                  prod.setEAN(ean.trim());
                }else{
                	ean = "";
                }
                
                if(nimi2 != null){
        			nimi2 = nimi2.trim().replaceAll("\"","").replaceAll("\n", " ").replaceAll("\r", " ").replaceAll(linesep, " ");
        			nimetus = kood +  nimi2.substring(0,1) + nimi2.substring(1,nimi2.length()).toLowerCase();
        			 prod.setName(enCode( nimi2.substring(0,1) + nimi2.substring(1,nimi2.length()).toLowerCase()));
        		}else{
        			if(nimetus.length() > 0){
        				nimetus = kood +  nimetus.substring(0,1) + nimetus.substring(1,nimetus.length()).toLowerCase();
           			 prod.setName(enCode(nimetus.substring(0,1) + nimetus.substring(1,nimetus.length()).toLowerCase() + " " +ean));
           		}else{
        			nimetus = kood ;
          			 prod.setName(enCode(nimetus.substring(0,1) + nimetus.substring(1,nimetus.length()).toLowerCase() + " " +ean));
          			
           		}
        		}
                
               
                String  gr2 = rs.getString("teineGrupp");
                if(gr2 != null && gr2.trim().length()>0 && !gr2.equals(gr)){
                  prod.setGroup(gr2.trim());
                }
             
              prod.setInCases(rs.getDouble("tykke"));
              double minkogus = rs.getDouble("minamount");
              prod.setVat("21");
              
              if(minkogus > 0){
            	  prod.setOptAmountInUnits(minkogus);
              }
         
              
              String unit = rs.getString("unit");
              if(unit!= null && unit.trim().length()>0){
            	  prod.setUnit(enCode(unit.trim()));
              }
              
              double alus = rs.getDouble("alus");
              if(alus > 0){
            	  prod.setOnPallet(alus);
              }
              
	          if( grouplist.contains(prod.getID()) || prod.getID().startsWith("240") || prod.getID().startsWith("102")){
	      		products.put(nimetus, prod);  
	          }

	    }
            rs.close();
            st.close();
            con.close();
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
        connectionUp(url);
	/*
   select "Customer Code" AS klient ,"Stock Code" AS toode,Qty AS kogus from st02 where"Salesman"='45'
AND "Invoice order date">'20021101' AND "Actual Del  Date">'00000000'
 group by "Stock Code","Customer Code",qty  order by "Customer Code","Stock Code","Invoice order date" */

     /*   SELECT  ST02001 AS klient ,ST02017 AS toode ,ST02020 AS Kogus,ST02015
         FROM ST020100 WHERE ST02015>? AND ST02016>0 AND ST02020>0 GROUP BY ST02017,ST02001, ST02020,ST02015 ORDER BY ST02001,ST02017 ,ST02015 asc
*/
//Seelega tuleb teha h�kk

        String sql = "select kl.customercode AS klient ,t.productcode AS toode, r.quantity AS kogus ,d.billissuedate  AS lastdate  " +
				"from asVwOrder d INNER JOIN asVwOrderDetails r WITH(NOLOCK) ON d.DeliveredOrderAsID = r.DeliveredOrderAsID " +
				"INNER JOIN asVwCustomer kl ON kl.customerasid=d.customerasid "+
				"INNER JOIN asVwProduct t ON t.productasid=r.productasid  " +
				" INNER JOIN  asVwProdGroup g ON g.prodgroupasid=t.prodgroupasid " +
				" WHERE billissuedate > ? GROUP BY t.productcode,kl.customercode,r.quantity,d.billissuedate ORDER BY kl.customercode asc, " +
				"t.productcode,billissuedate asc";
      


        sql = ";WITH aaa( OperacijosId, Data ) AS ( SELECT o.OperacijosId, o.Data " +
        	      " FROM Operacijos2 o2 WITH(NOLOCK) INNER JOIN Operacijos o WITH(NOLOCK) ON o2.OperacijosId = o.OperacijosId " +
        	       "                                 INNER JOIN RysioOperacijos rop WITH(NOLOCK) ON o.OperacijosId = rop.OperacijosId " +
        	     " WHERE o.OperacijosTipoId IN ( 50, 64 )AND rop.RysiuTipaiId = 3 AND rop.OperacijosStatusasId = 778 " +
        	      "  and o2.Data_Int > DATEDIFF( d, 140, GETDATE())) " +
        	" SELECT klient = part.Kodas,   toode = p.Kodas, kogus = Convert( Int, pk.Kiekis ),  lastdate = aaa.Data " +
        	 " FROM aaa INNER JOIN PardavimuKunai pk WITH(NOLOCK) ON aaa.OperacijosId = pk.OperacijosId " +
        	  "         INNER JOIN Prekes p WITH(NOLOCK) ON pk.PrekesId = p.PrekesId " +
        	   "        INNER JOIN Pard_BendraInf pbi WITH(NOLOCK) ON aaa.OperacijosId = pbi.OperacijosId " +
        	    "       INNER JOIN Partneriai part WITH(NOLOCK) ON pbi.PartneriaiId = part.PartneriaiId " +
        	" ORDER BY part.Kodas ASC,  p.Kodas, aaa.Data ASC";
        

	if (isDebug()) {
	    System.out.println(sql);
	}

	Vector custProds = new Vector();
	
          try {
            stmt = con.prepareStatement(sql);
          // stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
            if (agentCode != null){
             // stmt.setString(2, agentCode);
            }
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
              String kliendikood = Converter.cleanLitLatEst(rs.getString("klient").trim());
              String tootekood = rs.getString("toode").trim();
              CustProd cp = new CustProd(kliendikood, tootekood, Double.parseDouble(rs.getString("kogus").trim()),0 ,0,
              		rs.getString("lastdate").substring(5,10),0,0,0," ");
                    custProds.add(cp);
              
            }
            rs.close();
            stmt.close();
          } catch (Exception e) {
            getParent().showError(e);
          }
       
        connectionDown();
	return custProds;
    }

    public String getPilotDayData(Agent agent){
    	makeXMLandCSVs();
        return null;

    }


    public String getNonMobileOrders(Agent agent){
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
	return 0;
    }
    


    public void setBulkSales(Map agents) {
       //if (agentCode == null) return 0;
      /* connectionUp(url);
         String cmd = "select  a.employeecode AS agent, sum(billamount) AS summa from  "  +
			" asVwOrder  d INNER JOIN asVwEmployee a  ON a.employeeasid=d.employeeasid " +
			" where MONTH( billissuedate)=MONTH(GETDATE()) AND YEAR( billissuedate)=YEAR(GETDATE()) group by a.employeecode";
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
                      agent.setMonthSales((int)rs.getDouble("summa"));
                       }
                    }

             rs.close();
             stmt.close();

         } catch (Exception e) {
             getParent().showError(e);
         }
*/
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
        // System.out.println(ensureLen(tel, " "));
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
       public void setDealers(TreeMap aDealers) {
        dealers = aDealers;
      }

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "SkaiteMERC";
    private String             url = "jdbc:odbc:EUGESTA";
    private String             url2 = "jdbc:odbc:report";
    private Connection	       con = null;
    private Vector grouplist = new Vector();
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance(); 
    private TreeMap  dealers = new TreeMap();
    
	public void insertToImageBase(long madedate,String agent,String agentname ,String custid ,String custname ,
 		   String custaddr , String chain, String region ,String  filename  ,String filepath ,String res1 , String res2 ,double res3,String imagegroup,String country){
	  String sql = "INSERT INTO mj_imagebase values (?,'" + agent + "','" + agentname + "','" + custid + "','" + custname + "','" + custaddr + 
	  "','" + chain + "','" + region + "','" + filename + "','" + filepath + "','" + res1 + "','" + res2 + "',0,'" + imagegroup + "','" + country +"',0) ";
	  try{
		  Connection ccc = this.getNewConnection();
			PreparedStatement ps = ccc.prepareStatement(sql);
         	ps.setTimestamp(1, new java.sql.Timestamp(madedate));
			ps.executeUpdate();
			ps.close();
	        ccc.close();
	  }catch (Exception e){
        e.printStackTrace();		  
	  }
	 	}
	
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

