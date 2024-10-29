/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
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
public class SkaiteAGDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Discounter{


    public SkaiteAGDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
	// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();
	    
	   
	   url = "jdbc:odbc:EUGESTA";
		 if( getParent().getProps().getProperty("odbc") != null){
			 url = "jdbc:odbc:" + getParent().getProps().getProperty("odbc");
		 }
	    
	   
	} catch (Exception e) {
	    parent.showError(e);
	}
	user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  
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
    	
Converter conv = new Converter();
	    
	    questIDs = conv.getQuestActiveIDs(this.getExcelDir());
	 
	   
    	
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

            
        private void loadMaster(){
        	   String	   fileName = "test/custmaster.txt";
        	   File f = new File(fileName);
        	   if(f.exists()){
        		   
        	  try{
        		  BufferedReader br = new BufferedReader(new FileReader(fileName));
      	  
            String	   line = null;
      	    String	   custID = null;
            String	   custName = null;
      	    String     custAddr = null;
      	    String     chain  = null;
      	    String     payer  = null;
      	    String     city  = null;
      	    String     region  = null;
      	   
      	/*dbo.mj_customer_est 
             custid varchar(20) ,
             custname varchar(200) ,
             aadress varchar(100) ,
             region varchar(200) ,
             chain varchar(200) ,
             cityvarchar varchar(100) ,
             
             sb.append(cust.getID() + ";" + nimi2 + ";" + addr  + ";" + cust.getPayer() 
            		 + ";" + city + ";" + region + ";" + chain + "\n");
             */
      	    java.sql.Date     date =  null;
      	    while ((line = br.readLine()) != null) {
      	    	System.out.println(line);
      		StringTokenizer st = new StringTokenizer(line, ";");
      		while (st.hasMoreTokens()) {
      			
      			custID = st.nextToken();
      			custName = st.nextToken();
      			custAddr = st.nextToken();
   		        payer = st.nextToken();
   		        city = st.nextToken();
   		        region = st.nextToken();
   		        chain = st.nextToken();
   		
   		        String sql = "INSERT INTO mj_customer_est values (?,?,?,?,?,?)";
   		        
   		     try{
   			  Connection ccc = this.getNewConnection();
   				PreparedStatement ps = ccc.prepareStatement(sql);
   	         	ps.setString(1,custID);
   	     	    ps.setString(2,custName);
   	 	        ps.setString(3,custAddr);
   	 	        ps.setString(4,city);
   	 		    ps.setString(5,region);
   	 		    ps.setString(6,chain);
   				ps.executeUpdate();
   				ps.close();
   		        ccc.close();
   		    }catch (Exception e){
   	          e.printStackTrace();		  
   		    }
      		}
      	    }
      	    br.close();
	  
        	  }catch(Exception e){
                e.printStackTrace();      		  
        	  }
        	  
        	  
       	          	   }
        	   
        	   if(f.exists()){f.delete();}
        	   
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
		           if(seis > 0 && dealer.equals("EQ")){
		               products.append(kood +  ";");
		               products.append(dealer + ";");
		               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
		             }
		           }
		           rs.close();
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
               	  String vana = " SELECT  PriceListAsId AS grupp,t.ProductCode AS toode FROM asVwPriceListProducts r  " +
               	  		" INNER JOIN asVwProduct t ON t.ProductAsId=r.ProductAsId WHERE LevelNo is not null ORDER BY ProductName";
               	String cmd = "SELECT  PriceListasId AS grupp, ProductCode AS toode FROM vMRS_PriceListsProducts  "+
               	" ORDER BY PriceListasId, ProductCode";
               	  
               	  
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
    
    	if(parent.getParentFrame().agents == false){
    		parent.getParentFrame().agents = true;
    	System.out.println("parent.getParentFrame().agents true");
        connectionUp(url);
    	TreeMap agents = new TreeMap();
        try {  
        TreeMap gr = new TreeMap();
        try{
        	parent.agentsBuffer = new TreeMap();
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
       
        Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
        
        String  cmd = "SELECT  employeeasid ,employeecode AS yksus,empname AS nimi,warehouseno AS ladu , " +
        " pricelistasid AS hinnakiri FROM asVwEmployee WHERE employeecode not like 'zz%' order by empname";


        

            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim() );
              String nimi = agent.getID() + " " +  rs.getString("nimi").trim();
              agent.setName(nimi);
              
              agent.setObject(parent.make(rs.getString("ladu")));
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
              if(email.length() > 3){
            	  agent.setActive(true);
              }
              
              agents.put(nimi, agent);
              parent.agentsBuffer.put(nimi, agent);
	    }
	    rs.close();
	    stmt.close();
	    con.close();
	 

	} catch (Exception e) {
	    getParent().showError(e);
	}
        if(rcompany != null){
	    	//Converter.sendAgentJson(agents, rcompany, false);
	    }
        connectionDown();
        return agents;	
    	}else{
	   return	parent.agentsBuffer;
	}
    

    
    }
 
    
    public Vector getPriceMatrixData()  {
        Vector lines = new Vector();
    /*     String    sql = " SELECT SL01008 AS custid,'' AS clgroup,SC18003 + '03' AS prodid,'' AS prodgroup,SC18009, " +
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
           }
      */   return lines;
    	
       }

    private Vector getBlocked(){
    	
    	Vector vec = new Vector();
    	
try{
	  String  cmd = "select RefID,max(data) as kuup  from PartneriuBlokai where data > getdate()-356 group by RefID";
   this.connectionUp(url);
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
		    rs.close();
		    stmt.close();
		    con.close();
	
	    }
	    	    
}catch(Exception e){
	
}
          

          
    	return vec ;
    }
    
    
    TreeMap codes = null;
    
    private TreeMap getCodes(){
    	if(parent.custCodes == null){
    	
 	   parent.custCodes = new TreeMap();
 	   
 	   String cmd = "SELECT kl.customercode AS kood  FROM E_Partner.asVwMJCustomer kl  " +
        " INNER JOIN Partneriai AS part  ON part.PartneriaiId  = kl.CustomerASid " + 
        " LEFT JOIN PL_Partneriai pl ON part.PartneriaiId = pl.PartneriaiId " +
        " LEFT JOIN IvNormatyvuReiksmes ivnr ON pl.TIPAS6ID = ivnr.IvNormatyvuReiksmesId " +
        " where ivnr.Pavad !='NENAUDOJAMAS'  ORDER BY kl.customercode asc ";
 	   try {
 		   Connection con2 = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
 		           getParent().getProps().getProperty("dsn.pass"));
 	      PreparedStatement  stmt = con2.prepareStatement(cmd);
 	      ResultSet	      rs = stmt.executeQuery();
 	  
 	    while (rs.next()) {
 	    	
 	    	String kood = (rs.getString("kood").replaceAll("\""," "));
 	    	parent.custCodes.put(Converter.cleanLitLatEst(kood), kood);
 	    }	
 	    rs.close();
 	    stmt.close();
 	    con2.close();
 	    
 	   }catch(Exception d){
 		   d.printStackTrace();
 	   }
 		
    	}		
 	   return parent.custCodes;
    }
    
    public Customer getCustomerSingle(String custid) {
        connectionUp(url);
        TreeMap codes = getCodes();
       // System.out.println("Vahetasin koodi 0 " + custid + " " + codes.size());

        if(custid == null){
        	return null;
        }
        if(codes != null && codes.get(custid) != null){
        	custid = (String)codes.get(custid);
        	System.out.println("Vahetasin koodi 1 " + custid);
        	
        }else if(codes != null){
        	String sisse = custid.substring(0,3) + custid.substring(4);
        Iterator itera = 	codes.keySet().iterator();
        while (itera.hasNext()){
        	String bb = itera.next()+"";
        	String aa = codes.get(bb)+ "";
        	try{
        		String valja = aa.substring(0,3) + aa.substring(4);
            	if(valja.equals(sisse)){
            		custid = aa;
            		System.out.println("BINGO Vahetasin koodi 2 " + valja + " " + sisse);
            		//break;
            	}
            //	System.out.println("BINGO Vahetasin koodi 2 " + valja + " " + sisse);
        	}catch (Exception e){
        		
        	}
        	
        }
        }
        System.out.println("Vahetasin koodi 3 " + custid);
        
        
        String cmd = "SELECT kl.customercode AS kood, kl.customername  AS nimi,regno as payer,kl.address + ' ' + rtrim(kl.city) AS aadress,  " +
       " RegionName  as grupp, StoreChainAsID as jure ,ivnr.Pavad as tyyp, 'LT' as country FROM E_Partner.asVwMJCustomer kl INNER JOIN " +
       " asVwEmployee a  ON a.employeeasid=kl.employeeasid " +
       " INNER JOIN Partneriai AS part  ON part.PartneriaiId  = kl.CustomerASid " + 
       " LEFT JOIN PL_Partneriai pl ON part.PartneriaiId = pl.PartneriaiId " +
       " LEFT JOIN IvNormatyvuReiksmes ivnr ON pl.TIPAS6ID = ivnr.IvNormatyvuReiksmesId " +
       " LEFT JOIN  asVwRegion reg on reg.RegionAsId  = kl.RegionAsId " +
       " where ivnr.Pavad !='NENAUDOJAMAS' and kl.customercode = '" + custid + "'  " +
       " UNION " +
       " select custID AS kood, custName  AS nimi, '' as payer,  aadress, " +
       " region  as grupp, chain as jure ,chain as tyyp, 'EE' as country from   mj_customer_est where custID = '" + custid + "'" +
       " ORDER BY 1 asc";
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
			String country = enCode(rs.getString("country").replaceAll("\""," ").trim());
		
			
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
	                cust.setCity(country);
	             	
	    }
	    rs.close();
	    stmt.close();
	    con.close();
	 }catch(Exception aa){
     aa.printStackTrace();	
     }	

		return cust;
	}
    
    private String makeCall(String day ,String add){
  	  String res = " ";
  	 // System.out.println("dd" + day);
  	  if(day.length()==1){
  		  if(day.equals("1")){
  			  res = "E" + add;
  		  }
  		  if(day.equals("2")){
  			  res = "T" + add;
  		  }
  		  if(day.equals("3")){
  			  res = "K"+ add;
  		  }
  		  if(day.equals("4")){
  			  res = "N"+ add;
  		  }
  		  if(day.equals("5")){
  			  res = "R"+ add;
  		  }
  		  if(day.equals("6")){
  			  res = "L"+ add;
  		  }
  		  if(day.equals("7")){
  			  res = "P"+ add;
  		  }
  	  }else{
  		for(int i = 0; i <  day.length();i++){
  			if(isNumeric(day.charAt(i))){
  				  if((day.charAt(i)+"").equals("1")){
  					  res = res + "E" + add;
  				  }
  				  if((day.charAt(i)+"").equals("2")){
  					  res =res + "T" + add;
  				  }
  				  if((day.charAt(i)+"").equals("3")){
  					  res =res + "K" + add;
  				  }
  				  if((day.charAt(i)+"").equals("4")){
  					  res =res + "N" + add;
  				  }
  				  if((day.charAt(i)+"").equals("5")){
  					  res =res + "R" + add;
  				  }
  				  if((day.charAt(i)+"").equals("6")){
  					  res =res + "L" + add;
  				  }
  				  if((day.charAt(i)+"").equals("7")){
  					  res =res + "P" + add;
  				  }
  			}
  		}
  	  }
  	    
  	  
  	  return res ;
  }



    public static boolean isNumeric(String s){
        for (int i = 0 ; i < s.length(); i++){
          if (!isNumeric(s.charAt(i))){
            return false;
          }
        }
        return true;
      }

      public static boolean isNumeric(char ch)
      {
          return ch == '-' || ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4' || ch == '.' || ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9';
      }
    
    
  	private String getDir() {
		String dir = getParent().getProps().getProperty("readin");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}


    
    public TreeMap getDegu() {
		TreeMap result = new TreeMap();
		File f = new File(this.getDir() + "CC.xls");
		try {
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("Bendras");
			HSSFRow row = null;

			// System.out.println(groups.toString());

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String custID = "";
				try{
				if (row.getCell((short) 0) != null) {

					try {
						custID = (int) (row.getCell((short) 0).getNumericCellValue()) + "";
					} catch (Exception e) {
						custID = (row.getCell((short) 0).getStringCellValue());
					}
					
                       if(!custID.equals("0")){
                    	Customer cust = new Customer(custID);
					
                    		try {
					   String agent = (row.getCell((short) 5).getStringCellValue());
					   cust.setCity(agent);
					 
								
                    		} catch (Exception e) {
                    		e.printStackTrace();
    						}
                    		String callday = "";
                    		try {
     						   callday = (row.getCell((short) 7).getStringCellValue());
     						   if(callday.length() <  4){
     							 callday = makeCall(callday.trim() ,"");   
     						   }
     									
     	                    		} catch (Exception e) {
     	                    			callday = (int) (row.getCell((short) 7).getNumericCellValue()) + "";
     	                    			 if(callday.length() < 4){
     		     							  callday = makeCall(callday.trim(),"");   
     		     						   }
     	    						}
                    		
                    		String mainday = "";
                    		int mainseq = 0;
                    		String callday1 = "";
                    		try {
     						   callday1 = (row.getCell((short) 8).getStringCellValue());
     						   if(callday1.length() <  4 && callday1.length() != 0){
     							   mainday = makeCall(callday1.trim() ,"");
     							   mainseq = 4;  
     						   }
     									
     	                    		} catch (Exception e) {
     	                    			callday1 = (int) (row.getCell((short) 8).getNumericCellValue()) + "";
     	                    			 if(callday1.length() < 4 && callday1.length() != 0){
     	                    				   mainday = makeCall(callday1.trim() ,"");
     		     							   mainseq = 4;    
     		     						   }
     	    						}
     	                    		String callday2 = "";
     	                    		
     	                    		try {
     	     						   callday2 = (row.getCell((short) 9).getStringCellValue());
     	     						   if(callday2.length() <  4 && callday2.length() != 0){
     	     							   if(mainday.length() == 0){
     	     								 mainday = makeCall(callday2.trim() ,"");
	    	     						    }
     	     							   mainseq = mainseq + 8;   
     	     						   }
     	     									
     	     	                    		} catch (Exception e) {
     	     	                    			callday2 = (int) (row.getCell((short) 9).getNumericCellValue()) + "";
     	     	                    			 if(callday2.length() < 4 && callday2.length() != 0){
     	     	                    				 if(mainday.length() == 0){
     		     	     								 mainday = makeCall(callday2.trim() ,"");
     			    	     						    }
     		    	     							   mainseq = mainseq + 8; 
     		    	     							   }
     	     	    						}
     	                    		
     	                    		
     	     	                    		
     	     	                    		String callday3 = "";
    	     	                    		
    	     	                    		try {
    	     	     						   callday3 = (row.getCell((short) 10).getStringCellValue());
    	     	     						   if(callday3.length() <  4 && callday3.length() != 0){
    	     	     							 if(mainday.length() == 0){
    	     	     								 mainday = makeCall(callday3.trim() ,"");
    		    	     						    }
  	    	     							   mainseq = mainseq + 16;    
    	     	     						   }
    	     	     									
    	     	     	                    		} catch (Exception e) {
    	     	     	                    			callday3 = (int) (row.getCell((short) 10).getNumericCellValue()) + "";
    	     	     	                    			 if(callday3.length() < 4 && callday3.length() != 0){
    	     	     	                    				 if(mainday.length() == 0){
    	     		     	     								 mainday = makeCall(callday3.trim() ,"");
    	     			    	     						    }
    	     	     	                    				 mainseq = mainseq + 16;    
    	  	  	    	     							   }
    	     	     	    						}
     	     	                    		
    	     	     	                    		String callday4 = "";
    	    	     	                    		
    	    	     	                    		try {
		    	    	     	     						   callday4 = (row.getCell((short) 11).getStringCellValue());
		    	    	     	     						   if(callday4.length() <  4 && callday4.length() != 0){
		    	    	     	     							 if(mainday.length() == 0){
		    	    	     	     								 mainday = makeCall(callday4.trim() ,"");
		    	    		    	     						    }
		    	    	     	     							 mainseq = mainseq + 32;     }
		    	    	     	     									
    	    	     	     	                    		} catch (Exception e) {
    	    	     	     	                    			callday4 = (int) (row.getCell((short) 11).getNumericCellValue()) + "";
    	    	     	     	                    			 if(callday4.length() < 4 && callday4.length() != 0){
    	    	     	     	                    				 if(mainday.length() == 0){
    	    	     		     	     								 mainday = makeCall(callday4.trim() ,"");
    	    	     			    	     						    }
    	    	     	     	                    				 mainseq = mainseq + 32;       }
    	    	     	     	    						}
    	    	     	     	                    		
    	    	     	     	                    		
    	    	     	     	                    		
    	    	     	     	           	 String calld = "";
    	    	     	     	                    	 if(mainseq == 20){
    	    	     	     	                    		 mainseq = 2;
    	    	     	     	                    	 }
    	    	     	     	                    	 if(mainseq == 40){
    	    	     	     	                    		 mainseq = 1;
    	    	     	     	                    	 }	 
    	    	     	     	               if(mainday.length()!= 1){
    	    	     	     	                	  for(int o = 0;o < mainday.length(); o++){
    	    	     	     	                		  calld = calld +  mainday + "" + mainseq;
    	    	     	     	                	  }
    	    	     	     	                		  
    	    	     	     	                  }else {
    	    	     	     	                	 calld =  mainday + "" + mainseq;
    	    	     	     	                  }
     	                    		
     	                    		 if(callday.trim().length() != 0){
     	                    			 calld = callday;
     	                    		 }
     	                    		 
     	                    		 if(calld.length() != 0){
     	                    			 cust.setCallDay(calld);
     	                    			// System.out.println(cust.getID() + " z" + mainday + "z" +mainseq +   "y " + calld);
     	                    		 }
     	                    		 
     	                    		 String name = " ";
	     	                    		try {
	     	                    			name = (row.getCell((short) 15).getStringCellValue());
	     	     						  if(name.trim().length() != 0){
	     	     							  cust.setPersonalData(name);
	     	     						  }
	     	     									
	     	     	                    		} catch (Exception e) {
	     	     	                    			name = (int) (row.getCell((short) 15).getNumericCellValue()) + "";
	     	     	                    			if(name.trim().length() != 0){
	     		     	     							  cust.setPersonalData(name);
	     		     	     						  }
	     	     	    						}
     	                    		 
     	                    		 String phone = " ";
     	                    		try {
     	     						  phone = (row.getCell((short) 13).getStringCellValue());
     	     						  if(phone.trim().length() != 0){
     	     							  cust.setPersonalData((cust.getPersonalData() + " " + phone).trim());
     	     						  }
     	     									
     	     	                    		} catch (Exception e) {
     	     	                    			phone = (int) (row.getCell((short) 13).getNumericCellValue()) + "";
     	     	                    			if(phone.trim().length() != 0){
     	     	                    				 cust.setPersonalData((cust.getPersonalData() + " " + phone).trim());
     		     	     						  }
     	     	    						}
     	     	                    		
     	     	        	//System.out.println(cust.getID() + " xx " + cust.getCallDay());
     	     	         result.put(custID, cust);
				}
                   
				}
				
				
				}catch(Exception eee){
					eee.printStackTrace();
				}
				}
		} catch (Exception e) {
			e.printStackTrace();
		}

	//	 System.exit(0);

		return result;

	}
    

    public TreeMap getCustomers(Agent agent){
    	
    	
Converter conv = new Converter();
	    
	  
	   
        connectionUp(url);
        this.loadMaster();
        TreeMap ccd = null;
    
  	  HashMap kyssad =   conv.getQuestionaire(this.getExcelDir());
  	  
  	  TreeMap minsums = loadMinsums();
  	  //
  	  
  	  System.out.println("minsums " + minsums.toString());
        Vector blocked = getBlocked();
        
        String cmd = "SELECT kl.customername  AS nimi,kl.address as addr,regno as payer,kl.customercode AS kood, kl.address as tanav,kl.city as linn, reg.RegionName as regioon,Indeksas as indeks," +
        "	 kl.address + ' ' + rtrim(kl.city) AS aadress, " +
       " paymenttime AS term,'' AS ale, debt , " +
       " ' ' AS visit, custphone AS info ,a.employeecode as agent, kl.PriceListAsId as grupp ,ivnr.Pavad as tyyp ," +
       " part.Nenaudoti as aktiivne , (case when CategoryAsID  in ('10','03','05','11','12','20','21') then 100 else 150 end)" +
	"	 as minsum , CategoryAsID as msid FROM E_Partner.asVwMJCustomer kl left JOIN " +
     "   asVwEmployee a  ON a.employeeasid=kl.employeeasid " +
     "   INNER JOIN Partneriai AS part  ON part.PartneriaiId  = kl.CustomerASid  " +
	"	LEFT JOIN Adresai AS adr     ON part.AdresoId = adr.AdresoId " +
	"	LEFT JOIN asVwRegion reg on reg.RegionAsId=kl.RegionasId " +
    "    LEFT JOIN PL_Partneriai pl ON part.PartneriaiId = pl.PartneriaiId " +
    "    LEFT JOIN IvNormatyvuReiksmes ivnr ON pl.TIPAS6ID = ivnr.IvNormatyvuReiksmesId " +
    "   where ivnr.Pavad !='NENAUDOJAMAS' ORDER BY kl.customercode asc";
	log(cmd);

	TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
  
	try {
		Connection con2 = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
		           getParent().getProps().getProperty("dsn.pass"));
          PreparedStatement  stmt = con2.prepareStatement(cmd);
          ResultSet	           rs = stmt.executeQuery();

	    String minam = getParent().getProps().getProperty("minsumma");
	    double minsum = 0;
	    if(minam != null){
	    	try{
	    		minsum = Double.parseDouble(minam);
	    	 }catch(Exception r){
	    	 }
	      }
	    
	    while (rs.next()) {
		String nimi = enCode(rs.getString("nimi").replaceAll("\""," ").trim());
		String addr = enCode(rs.getString("addr").replaceAll("\""," ").trim());
		String payer = enCode(rs.getString("payer").replaceAll("\""," ").trim());
	
                Customer cust = new Customer(rs.getString("kood").trim());
                if(minsum > 0){
                	cust.setMinsum(minsum);
                }
                
                String tanav = rs.getString("tanav");
                if (tanav != null && tanav.length() > 0){
                  cust.restStreet = tanav.trim();
                }
                String linn = rs.getString("linn");
                if (linn != null && linn.length() > 0){
                  cust.restCity = linn.trim();
                }
                String regioon = rs.getString("regioon");
                if (regioon != null && regioon.length() > 0){
                	cust.restCity = (cust.restCity + " " + regioon.trim()).trim();
                }

                String indeks = rs.getString("indeks");
                if (indeks != null && indeks.length() > 0){
                  cust.restZipCode = indeks.trim();
                }

                cust.setCountry("Lithuania");
                
                conv.addQuestiosToCustomer(cust,kyssad);
                cust.setPayer(payer);
                if(nimi.startsWith("UAB ")){
                	nimi = (nimi.substring(4,nimi.length()) + " UAB").trim();
                }
                cust.setName(nimi + " " + addr);
                cust.restName = nimi;
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
                if(blocked.contains(cust.getID())){
                	cust.setSalesBlock(true);
                }
                double debti = rs.getDouble("debt");
               
                	cust.setDebt(debti);
                	
                
                	 if(ccd != null && ccd.get(cust.getID())!= null ){
                     	Customer ccc = (Customer) ccd.get(cust.getID());
                     
                     	if(ccc.getCallDay() != null && ccc.getCallDay().length() != 0){
                     		cust.setVisit(ccc.getCallDay());// lisame kylastus päeva. 
                     	}
                     	
                     }
                	
                
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
                
                minsum = rs.getDouble("minsum");
                
                String category = rs.getString("msid");
                if(category != null && category.trim().length() >0 ){
                	category = category.trim();
                	if(minsums != null && minsums.get(category) != null){
                		try{
                			minsum = Double.parseDouble(minsums.get(category)+"");	
                		}catch(Exception e){
                		e.printStackTrace();	
                		}
                	}
                }
                
                cust.setMinsum(minsum);
                
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
              String id = rs.getString("klient").trim();
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

	 if(rcompany != null){
         	//Converter.sendCustomerJson(customers, company,true,user,pass);
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
	  	 

  	return Converter.enCodeEst(res);
	  }
    
    public TreeMap getDealers() {
        return dealers;
      }
    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "select  CustomerCode AS klient, PayerCode as payer, billcode AS number,billissuedate+paymenttime AS kuupaev, " +
		" (case when OperacijosTipoId =64 then -1 else 1 end)*billamount-paidamount AS summa  , datediff(day,billissuedate+paymenttime" +
		" ,getdate())as yle from " +
	  "	asVwOrder  d  where billamount-paidamount>0 and  " +
      "    billissuedate > getdate()-600  order by CustomerCode,billissuedate+paymenttime";

      connectionUp(url);
      stmt = con.prepareStatement(sql);


      ResultSet rs = stmt.executeQuery();

      while(rs.next()){
    	  try{
    		    Debt debt = new Debt();
    	        debt.setCustID(rs.getString("klient").trim());
    	        debt.setPayerID(rs.getString("payer").trim());
    	     
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
    	    	        list.addElement(debt);	
    	        }
    	       
    	  }catch (Exception e){
    		  e.printStackTrace();
    	  }
    
        }
      rs.close();
      stmt.close();
      con.close();
      System.out.println("debtlist size " + list.size());
      return list;
    }
    
    
    public static final String cleanLitLatEst(String ret){
        if (ret != null){
        Converter.cleanLitLatEst(ret);        	
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
       	    	  sb.append("        <WALUTA1>EUR</WALUTA1>"+ ls);
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
                
           // Siin lï¿½peb toodete tegemine     
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
 		csv.append( invoDate + ";" + num + ";" + cleanLitLatEst(klient) + ";" + cleanLitLatEst(kliendinimi) + ";LT;" + cleanLitLatEst(regi)+ ";" + cleanLitLatEst(chain) + ";EUR;0;0;" + regnr + ";" + date + ";"+ agent+ ls);
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
                
   //  Dokumendi pï¿½ised Lï¿½pp
     
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
		 		csv.append(invoDate +";" + num + ";" + cleanLitLatEst(toode) + ";" + kogus + ";" + yhik + ";EUR;" + priceh + ";" + ale +
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
		 		  sb.append("        <WALUTA1>EUR</WALUTA1>"+ ls);
		 		  sb.append("     </DOCUPOSI>"+ ls);  

		 	}
		 	
		 	
	   }
}
  rs.close();
  stmt.close();
  con.close();
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

    private TreeMap<String, String> loadMinsums() {
  		TreeMap<String, String> res = new TreeMap<String, String>();
  		
  		try {
  			FileReader reader = new FileReader("minsums.txt");
  			BufferedReader br = new BufferedReader(reader);
  			String line;
  		
  			while ( (line = br.readLine()) != null) {
  				StringTokenizer st = new StringTokenizer(line, ";");
  				if (st.hasMoreTokens()) {
  					String modeID = st.nextToken();
  					if (st.hasMoreTokens()) {
  						String modeName = st.nextToken();
  							res.put(modeID, modeName);
  					}
  				}
  			}
  			br.close();
  			reader.close();
  		}
  		catch (Exception e) {
  		}
  		
  		return res;
  	}
    
    private TreeMap loadSimis() {
		TreeMap res = new TreeMap();
		
		try {
			FileReader reader = new FileReader("prodanalogs.txt");
			BufferedReader br = new BufferedReader(reader);
			String line;
		
			while ( (line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, ";");
				if (st.hasMoreTokens()) {
					String modeID = st.nextToken();
					if (st.hasMoreTokens()) {
						String modeName = st.nextToken();
							res.put(modeID, modeName);
					}
				}
			}
			br.close();
			reader.close();
		}
		catch (Exception e) {
		}
		
		return res;
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
      " where productcode not like '%*%' and  productcode not like '/%' and pr.Nenaudoti  = 0 order by prodgroupcode ";

	if (isDebug()) {
	    System.out.println(cmd);
	}

	TreeMap products = new TreeMap();
    TreeMap simis = loadSimis();
    
    
	try {
          Statement st = con.createStatement();
	    ResultSet	      rs = st.executeQuery(cmd);

	    while (rs.next()) {
	    	String aggrupp =  rs.getString("asi").trim();
		String kood = rs.getString("toode").trim();
		String linesep = System.getProperty("line.separator");
		String nimetus = rs.getString("nimi").trim().replaceAll("\"","").replaceAll("\n", " ").replaceAll("\r", " ").replaceAll(linesep, " ");
		String nimi2 =  rs.getString("nimi2");
		if(nimi2 != null){
			nimi2 = nimi2.trim().replaceAll("\"","").replaceAll("\n", " ").replaceAll("\r", " ").replaceAll(linesep, " ");
			nimetus = kood +  nimi2.substring(0,1) + nimi2.substring(1,nimi2.length()).toLowerCase();
		}else{
			try{
				nimetus = kood +  nimetus.substring(0,1) + nimetus.substring(1,nimetus.length()).toLowerCase();		
			}catch(Exception r){
				r.printStackTrace();
			}
		
		}
		
	
                Product prod = new Product(kood);
                prod.setAgentGroup(aggrupp);
                
                if(simis.get(kood) != null){
                	prod.setTypeSimi(simis.get(kood) + "");
                }
               
                try{
                    prod.setPrice(parseDouble(rs.getString("myygihind").trim())/1.21);	
                }catch(Exception ee){
                	prod.setPrice(0);
                }

              

                String gr = rs.getString("grupp");
                if(gr != null && gr.trim().length()>0){
                  prod.setGroup(gr.trim());
                }
                String  ean = rs.getString("ean");
                if(ean != null && ean.trim().length()>0){
                  prod.setEAN(ean.trim());
                }
                prod.setName(enCode(nimetus));
                String  gr2 = rs.getString("teineGrupp");
                if(gr2 != null && gr2.trim().length()>0 && !gr2.equals(gr)){
                  prod.setGroup(gr2.trim());
                }
               try{ 
	                double  sellout = rs.getDouble("sellout");
	                if(sellout > 1){
	                 // prod.setStatusSellout("1");
	                  prod.setName(prod.getName() +  " @BR"  + sellout + "@");
	                }
                
	          } catch (Exception e) {
		       getParent().showError(e);
		      }
                
              prod.setInCases(rs.getDouble("tykke"));
              double minkogus = rs.getDouble("minamount");
              prod.setVat("21");
              
            
              int prior = rs.getInt("prioriteet");
              
              if(prior != 0){
            	  prod.setType(prior + "");
            	  if(prior == 2){
            		  prod.setType("1" + prior );
            	  }
            	  if(prior == 3){
            		  prod.setType("12" + prior );
            	  }
            	  if(prior == 4){
            		  prod.setType("123" + prior );
            		  prod.setDiffColor(true);
            	  }
            
              }
             
              double max = rs.getDouble("maxdisc");
              double max2 = rs.getDouble("maxdisc2");
            
                  
              
              if(max > 0){
                  prod.setMaxDiscount(max);
                  if(max > 1){	                 
 	                  prod.setName(prod.getName() +  " MD"  + (int)max + "%");
 	                }
              }else{
            	  if(prod.getName().indexOf("@BR") != -1){
            		  prod.setMaxDiscount(0); //miinus yhega keelame koheselt kõigile toodetele allahindluse andmise  
            	  }else{
            		  prod.setMaxDiscount(-1); //miinus yhega keelame koheselt kõigile toodetele allahindluse andmise 
            	  }        	
              }
              
              
              if(max2 > 0){
            	 // prod.setMaxDiscount(max2);  
              }
              
              String unit = rs.getString("unit");
              if(unit!= null && unit.trim().length()>0){
            	  prod.setUnit(enCode(unit.trim()));
              }
              
              if(minkogus > 0 && prod.getUnit().indexOf("pak.") == -1){
              	  prod.setOptAmountInUnits(minkogus);
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
	
        String sql = "select kl.customercode AS klient ,t.productcode AS toode, r.quantity AS kogus ,d.billissuedate  AS lastdate  " +
				"from asVwOrder d  WITH(NOLOCK) INNER JOIN asVwOrderDetails r WITH(NOLOCK) ON d.DeliveredOrderAsID = r.DeliveredOrderAsID " +
				"INNER JOIN asVwCustomer kl  WITH(NOLOCK) ON kl.customerasid=d.customerasid "+
				"INNER JOIN asVwProduct t  WITH(NOLOCK) ON t.productasid=r.productasid  " +
				" INNER JOIN  asVwProdGroup g   WITH(NOLOCK) ON g.prodgroupasid=t.prodgroupasid " +
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
              String kliendikood = rs.getString("klient").trim();
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
       connectionUp(url);
         String cmd = "select  a.employeecode AS agent, sum(billamount/1.21) AS summa , max(BPlanas) as planb, max(APlanas) as plana from  "  +
			" asVwOrder  d INNER JOIN asVwEmployee a  ON a.employeeasid=d.employeeasid " +
			" where MONTH( billissuedate)=MONTH(GETDATE()) AND YEAR( billissuedate)=YEAR(GETDATE()) group by a.employeecode";
         log(cmd);

         
         String grmyyk  = "select  sum(r.ppKainaBePVM * d.Kursas) as grmyyk,  employeecode as agent " +
      "  FROM Operacijos d WITH(NOLOCK) INNER JOIN Pard_BendraInf pbi WITH(NOLOCK) ON d.OperacijosId = pbi.OperacijosId " +
                                                                               " INNER JOIN asVwCustomer kl WITH(NOLOCK) ON kl.CustomerASId= pbi.PartneriaiId "+
                                                                               " INNER JOIN PardavimuKunai r WITH(NOLOCK) ON d.OperacijosId = r.OperacijosId " +
                                                                               " INNER JOIN prekes t WITH(NOLOCK) ON t.prekesid = r.prekesid "+
                                                                              " inner join PrekiuGrupes pg WITH(NOLOCK) ON t.PrekiuGrupesId = pg.PrekiuGrupesID " +
                                                                             " INNER JOIN asVwEmployee e WITH(NOLOCK) on e.employeeasid=kl.employeeasid "+
    " WHERE d.OperacijosTipoId in (50, 45, 68, 164, 2000014) and  MONTH( d.data)=MONTH(GETDATE()) AND YEAR( d.data)=YEAR(GETDATE()) " +
   " and (pg.Kodas in ('1','2','8','14','141',' 18','20','24','26','27','28','31','311','35','36', " +
    "     '395','45','46','47','50','51','53','60','61','635','71','76','77','853','854')) " +
     "       group by employeecode ";
         
         System.out.println("Esimene algas");	  
         
         
         
         try {
        	 
        	 HashMap<String ,Double> grsales = new HashMap<String ,Double>();
        	 stmt = con.prepareStatement(grmyyk);
        	  try {
        		  ResultSet	      rs = stmt.executeQuery();

                  while(rs.next()) {
                 	 double grsal = rs.getDouble("grmyyk");
                 	 String agentID = rs.getString("agent").trim();
                 	 grsales.put(agentID, grsal);
                  }
                    rs.close();
	  
        	  }catch(Exception e){
        		  stmt = con.prepareStatement(grmyyk);
        		  ResultSet	      rs = stmt.executeQuery();

                  while(rs.next()) {
                 	 double grsal = rs.getDouble("grmyyk");
                 	 String agentID = rs.getString("agent").trim();
                 	 grsales.put(agentID, grsal);
                  }
                    rs.close();
        	  }
        	  
        System.out.println("Esimene möödas");	  
        	                 
        	 
             stmt = con.prepareStatement(cmd);
             //stmt.setString(1, agentCode);
             int mm = Calendar.getInstance().get(Calendar.MONTH)+ 1;
             int yy = Calendar.getInstance().get(Calendar.YEAR);
             int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
             int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
             double planprec = dd/d2;
             Calendar cal = Calendar.getInstance();
             cal.set(Calendar.DAY_OF_MONTH, 1);

             ResultSet rs = stmt.executeQuery();

             while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                 Agent agent = (Agent)agents.get(agentID);
                   if (agent != null){
                	 int  msales = (int)rs.getDouble("summa");
                	 int  asales = 0;
                	 if(grsales.get(agentID)!= null){
                		  asales = (int)grsales.get(agentID).doubleValue();
                	 }
                	 
                      agent.setMonthSales(msales);
                      double planb = rs.getDouble("planb");
                      double plana = rs.getDouble("plana");
                      //siia siis plaani protsendid ja tekstid teha 
                      String asi = "Pardavimai: " + msales + " €||";
                      
                      asi = asi +  "Pardavimai A : " + asales + " €||";
                     
                      double  tehtudsalperc = msales/planb;
                      double  tehtudsalAperc = asales/plana;
                	 
                	  
                	  double plaanitavMyykSal = d2 * msales/dd;
                	  double  prognosalperc = plaanitavMyykSal/planb;
                	  double plaanitavAMyykSal = d2 * asales/dd;
                	  double  prognosalAperc = plaanitavAMyykSal/plana;
                	 
                	      asi = asi + "Pardavimų planas: " + (int)planb + " €||";
                    	  asi = asi + "Plano vykdymas/prognozė (€):";
                          asi = asi + "     " +NF.format(tehtudsalperc*100) + "% / " + NF.format(prognosalperc*100)  + " %||";  
                      
                          asi = asi + "Pardavimų  A planas: " + (int)plana + " €||";
                          asi = asi + "Plano A vykdymas/prognozė (€):";
                          asi = asi + "     " +NF.format(tehtudsalAperc*100) + "% / " + NF.format(prognosalAperc*100)  + " %||";  
                      
                    
                          agent.setStartText(asi);
                      
                       }
                    }

             rs.close();
             stmt.close();
             con.close();
             System.out.println("teine möödas");	  
             
         } catch (Exception e) {
             getParent().showError(e);
         }

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
      rs.close();
      ps.close();
      con.close();

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
    public static final String DRIVER_VERSION = "SkaiteAG";
    private String             url = "jdbc:odbc:EUGESTA";
    private String             url2 = "jdbc:odbc:report";
    private Connection	       con = null;
    private Vector grouplist = new Vector();
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance(); 
    private TreeMap  dealers = new TreeMap();
    
	public void insertToImageBase(long madedate,String agent,String agentname ,String custid ,String custname ,
 		   String custaddr , String chain, String region ,String  filename  ,String filepath ,String res1 , String res2 ,double res3,
 		   String imagegroup,String country,String imagech){
		TreeMap hh = loadPH();
		
		if(imagech != null && hh != null && hh.get(imagech) != null){
			imagech = hh.get(imagech) + "";
		}
TreeMap hh2 = loadPH2();
		System.out.println("hh2 " + hh2.toString());
		if(country.equals("EE") && imagech != null && hh2 != null && hh2.get(imagech) != null){
			imagech = hh2.get(imagech) + "";
		}
		
	  String sql = "INSERT INTO mj_imagebase values (?,'" + agent + "','" + agentname + "','" + custid + "','" + custname + "','" + custaddr + 
	  "','" + chain + "','" + Converter.deCode(region) + "','" + filename + "','" + filepath + "','" + res1 + "','" + res2 + "',0,'" + imagegroup + "','" + country +"','"+imagech + "',0,'') ";
	 
	 
	  try{
		  Connection ccc = this.getNewConnection();
		  System.out.println(sql);
			PreparedStatement ps = ccc.prepareStatement(sql);
         	ps.setTimestamp(1, new java.sql.Timestamp(madedate));
			ps.executeUpdate();
			ps.close();
	        ccc.close();
	    }catch (Exception e){
          e.printStackTrace();		  
	    }
	 	}
	private TreeMap loadPH() {
		TreeMap res = new TreeMap();
		
		try {
			FileReader reader = new FileReader("photogroups.txt");
			BufferedReader br = new BufferedReader(reader);
			String line;
		
			while ( (line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, ";");
				if (st.hasMoreTokens()) {
					String modeID = st.nextToken();
					if (st.hasMoreTokens()) {
						String modeName = st.nextToken();
						
						if (st.hasMoreTokens()) {
							String type = st.nextToken();
							res.put(modeID, modeName);
						}
					}
					
				}
			}
			br.close();
			reader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	private TreeMap loadPH2() {
		TreeMap res = new TreeMap();
		
		try {
			FileReader reader = new FileReader("photogroups2.txt");
			BufferedReader br = new BufferedReader(reader);
			String line;
		
			while ( (line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, ";");
				if (st.hasMoreTokens()) {
					String modeID = st.nextToken();
					if (st.hasMoreTokens()) {
						String modeName = st.nextToken();
						
						if (st.hasMoreTokens()) {
							String type = st.nextToken();
							res.put(modeID, modeName);
						}
					}
					
				}
			}
			br.close();
			reader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
}


