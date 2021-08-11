
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.driver;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
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
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;


public class SAFMaagDBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater,Discounter,FixLoader{
  public SAFMaagDBDriver(EksportPanel parent){
    super(parent);
    if(parent.getParentFrame().isDaySales()){
         this.subjectFilesTo = getParent().getString("senddatamsg");
    } else {this.subjectFilesTo = parent.getString("filemsg");}
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();

  //    getFixedAssortmentData();
    } catch (Exception e){
       parent.showError(e);
      // System.exit(0);
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
	
  
  public String getProductSortingData(){
   	return null;
   }
  
      public String getDealerCustomerCSV(String agent){
       return null;
       }

      public Vector getFixedAssortmentData() {
    	  Vector lines = new Vector();
   
          try {
        	  String sql = "SELECT DATEADD(dd, 0, DATEDIFF(dd, 0, GETDATE())) from mj_assortment where DATEADD(dd, 0, DATEDIFF(dd, 0, sortdate)) = DATEADD(dd, 0, DATEDIFF(dd, 0, GETDATE())) ";
        	  boolean insertassort = true;
        	  
         	  String cmd = "  select pr.id as grupp ,t.code as toode, arca.name as tgrupp from grpricelist pr INNER JOIN grpricelistrel rel ON pr.id =rel.idgrpricelist  " +
			" INNER JOIN grarticle t on t.id = rel.idgrarticle  inner JOIN grarticleCatrel cat ON cat.idgrarticle=t.id inner JOIN  " +
			"   grarticlecat arca ON arca.id = cat.idgrarticlecat     where pr.code like '%sortiment%'  and arca.category = 'tooterühm' " +
			"  and ((rel.enddate>getdate() OR rel.enddate=('1899-12-30 00:00:00.00') ) and  (rel.begindate<=getdate() OR rel.begindate=('1899-12-30 00:00:00.00') ) ) " +
		    " order by pr.id,t.code ";
         	  
         	System.out.println(cmd);
            	 Connection con2 = getNewConnection(url2);
        	        Statement stmt2 = con2.createStatement();
        	        
        	        ResultSet rs2 = stmt2.executeQuery(sql);
        	        if(rs2.next()){
        	        	insertassort = false;
        	        	//kontrollime kas on vaja insertida 
        	        }
        	        rs2.close();
        	        stmt2.close();
        	        con2.close();
        	        Connection con = getNewConnection();
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
                           prodGroup = rs.getString("tgrupp");
                           
                           if (custID != null && custID.trim().length() > 0) {
                             mline.setCustID(custID.trim());
                           }
                           if (clGroup != null
                               && clGroup.trim().length() > 0) {
                             mline.setCliGroup("S" + clGroup.trim());
                           }

                           if (prodID != null && prodID.trim().length() > 0) {
                             mline.setProdID(prodID.trim());
                           }
                           if (mline.getProdID() == null && prodGroup != null
                               && prodGroup.trim().length() > 0) {
                          //   mline.setProdGroup(prodGroup);
                           }

                           lines.add(mline);
                           if(insertassort){

                               insertSortLine(mline , prodGroup);   
                           }
                         }

        	       rs.close();
        	       con.close();
                        } catch (Exception e) {
                            getParent().showError(e);
                        }
          return lines;
  }
      
	  public void connectionUp(){
	  }

	  
	  private void	addToDB(String paev,String custid,String custname,String sort,String prodid,String prodname) {
		  
		  try{
	  
			 if(prodname.length() >50){
				 prodname = prodname.substring(0,50); 
			 }
	      String sql = "INSERT INTO oosmissing values (" +
	         "'" + paev + "'," + 
	         "'" + custid + "'," + 
	         "''," + 
	         "''," + 
	         "'" + custname + "'," + 
	         "'" + prodid + "'," + 
	         "'" + prodname + "')" ;
	  
	      
	      /*madedate datetime not NULL,
	
     custid varchar(20) not NULL,
	 chain varchar(50) not NULL,
	 agent varchar(10) not NULL,
	  agentname varchar(500)NOT NULL,
	    prodid varchar(50) NOT NULL,
		 prodname varchar(50) NOT NULL*/
	  	     Connection  con2 = getNewConnection(url2);      
		     Statement stmt2 = con2.createStatement();
		  //   System.out.println(sql);
		     stmt2.executeUpdate(sql);
		     stmt2.close();
		     con2.close();
  }catch(Exception e){
	
	  e.printStackTrace();
  }
	  }  
	  private void  insertSortLine(MatrixLine mline ,String tootegrupp ){
		  try{
				
			      String sql = "INSERT INTO mj_assortment values (" +
			         "'" + mline.getCustID() + "'," + 
			         "getdate()," + 
			         "'" + mline.getCliGroup() + "'," + 
			         "'" + mline.getProdID() + "'," + 
			         "0," + 
			         "0,'" + tootegrupp + "')" ;
			  
			  	     Connection  con2 = getNewConnection(url2);      
				     Statement stmt2 = con2.createStatement();
				     stmt2.executeUpdate(sql);
				     stmt2.close();
				     con2.close();
		  }catch(Exception e){
			  e.printStackTrace();
		  }
	
		  
	  }
	  
	  public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
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
	         	Connection ccc = this.getNewConnection(url2);
	             PreparedStatement ps = ccc.prepareStatement(sql);

	             for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
	               SuperVisorLineWrapper lw = new SuperVisorLineWrapper(iter.next());
	               ps.setString(1, wr.getInfo().getAgentID());
	               ps.setString(2, wr.getInfo().getAgentName());
	               ps.setString(3, lw.getLine().getCustID());
	               ps.setString(4, lw.getLine().getCustName());
	               ps.setString(5, lw.getLine().getProdID());
	               ps.setString(6, lw.getLine().getProdName());
	               ps.setDouble(7, lw.getLine().getNormalPrice());
	               ps.setDouble(8, lw.getLine().getAmount());
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
	             ccc.close();
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
	         	Connection ccc = this.getNewConnection(url2);
	             PreparedStatement ps = ccc.prepareStatement(sql);

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
	               ps.setString(30, Converter.deCode(cr1));


	               String cr2 = lw.getLine().getResStr2();
	               if (cr2 == null){
	               	cr2 = " ";
	               }
	               ps.setString(31, Converter.deCode(cr2));


	               String cr3 = lw.getLine().getResStr3();
	               if (cr3 == null){
	               	cr3 = " ";
	               }
	               ps.setString(32, Converter.deCode(cr3));
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


	  
	  public Vector getPriceMatrixData()  {
        Vector lines = new Vector();
        String    sql = "select '' AS custid, pl.idgrpricelist AS clgroup,pr.code  AS prodid,' ' AS prodgroup,pl.price AS price ,'' as begindate ,'' as enddate " +
           "  FROM grpricelistrel pl INNER JOIN grarticle pr ON  pl.idgrarticle = pr.id where pr.code not like '%arh%'  ORDER BY 1,2 " ;
		
        String    sql2 = "	 select k.id AS custid, ''  AS clgroup,pr.code  AS prodid,' ' AS prodgroup,pl.price AS price ,begindate ,enddate , DATEDIFF(dd, begindate, enddate) as days " +
         "    FROM grcpricelistrel pl INNER JOIN grcpricelist u on u.id=pl.idgrcpricelist INNER JOIN " +
		"	 dbo.GRCPriceLCompany cpl  on cpl.idgrcpricelist=pl.idgrcpricelist inner join grcompany k on k.id=cpl.idgrcompany INNER JOIN grarticle pr ON  pl.idgrarticle = pr.id " +
		"	 where getdate()>begindate and getdate()<enddate  ORDER BY 1,2";
          Statement st = null;
          try {
            Connection con = getNewConnection();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

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
              MatrixLine mline = new MatrixLine();

              if (custID != null && custID.trim().length() > 0) {
                mline.setCustID(custID.trim());
              }
              if ( clGroup !=null && clGroup.trim().length() > 0) {
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
              
              if (begin != null && begin.trim().length() > 0) {
                  mline.setStartDate(begin.trim().substring(0,10));
                }
              if (end != null && end.trim().length() > 0) {
                  mline.setEndDate(end.trim().substring(0,10));
                }
              if(days > 32){
            	  System.out.println("P�evad " + days);
            	  mline.setProdLevel("pr"); //m�rgime et on tegemist pika hinnaga mitte kampaania 
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
        return lines;
      }


	  
	  public String getAssortmentStr(Agent agent){
	  	return null;
	  }

		public Vector getLastThreeData() {

			Vector result = new Vector();
			int cou = 0;
			try {
				Connection con = getNewConnection();
				Statement st = con.createStatement();

				String cmd2 = "SELECT kl.code AS kood FROM grcompany kl WHERE  ltrim(kl.id)>0 ORDER BY ltrim(kl.name)";


				if (isDebug())
					System.out.println(cmd2);
				ResultSet rs = st.executeQuery(cmd2);

				while (rs.next()) {

					String accountnum = rs.getString("kood").trim();

					String sq1 = "SELECT top 5 d.docdate as kuupaev  FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice  INNER JOIN grcompany k ON k.name=d.companyname " + 
				    " WHERE d.docdate>getdate()-30 AND len(ltrim(d.idgrcompany))>0  and k.code = '" + accountnum +"'" +
					" GROUP BY k.code,d.docdate ORDER BY d.docdate desc";
					Connection con2 = getNewConnection();
					Statement st2 = con2.createStatement();

					ResultSet rs2 = st2.executeQuery(sq1);
					while (rs2.next()) {
						String kuup = rs2.getString("kuupaev");
						String sq3 = "SELECT t.code AS toode, r.quantity AS kogus " +
				 "  FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
				 "  INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=d.companyname " +  
				 "   WHERE d.docdate='" + kuup + "' and k.code = '" + accountnum + "'  AND (r.articletype='P' OR r.articletype='G') " +
				 "   ORDER BY t.code,r.docdate ";
						if(cou == 0){
							System.out.println(sq3);
							cou++;
						}
						Connection con3 = getNewConnection();
						Statement st3 = con3.createStatement();
						ResultSet rs3 = st3.executeQuery(sq3);
						while (rs3.next()) {
							MatrixLine ml = new MatrixLine();
							ml.setCustID(accountnum);
							ml.setProdID(rs3.getString("toode").trim());
							ml.setPrice(rs3.getDouble("kogus"));
							ml.setStartDate(kuup.substring(0,10));
							result.add(ml);
						}
						rs3.close();
						st3.close();
						con3.close();
					}
					rs2.close();
					st2.close();
					con2.close();
				   }
			} catch (Exception e) {
				e.printStackTrace();
			}

			return result;
		}

	
		
		public String getSaveStockInfo(){

      	  String  cmd = "SELECT pr.code AS toode,SUM(quantity) AS seis FROM dcurbalancestore ss  " +
    		 "INNER JOIN grarticle pr ON idgrarticle = pr.id WHERE ss.iddrstore in (2,5) AND quantity >0 GROUP BY  pr.code " ;


		       if (isDebug()) System.out.println(cmd);
		       StringBuffer products = new StringBuffer();
		       try {
		       	Connection con = getNewConnection();
		         Statement st = con.createStatement();
		         ResultSet rs = st.executeQuery(cmd);

		         while (rs.next()) {

		           String kood = rs.getString("toode").trim();
		           double seis = rs.getDouble("seis");
		           if(seis > 0){
		               products.append(kood +  ";");
		               products.append(" ;");
		               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
		             }
		           }
		           rs.close();
		       } catch (Exception e) {
		           getParent().showError(e);
		       }
		       return products.toString();
		   }
      public String getSaveDebtInfo() {
    	  
    	  sendDistributionReport(null,false);
       	return null;
       }
       
      private void sendDistributionReport(Calendar ccc , boolean day ){ //parameeter on selleks et kui on vaja saata mingi muu p�eva aruannet
    	     	  
    	  String saajad = getParent().getProps().getProperty("distribution.reporters");
    	  
    	  StringTokenizer sto = new StringTokenizer(saajad,",");
    	  while (sto.hasMoreElements()){
    		
    	  String add = "";
    	  String regno = sto.nextToken();
    	  String toThem = getParent().getProps().getProperty("distribution.email." + regno);
    	  
    	  if(ccc != null){
    		  Calendar cal = Calendar.getInstance();
    		  long diff = cal.getTimeInMillis() -ccc.getTimeInMillis();
    		  cal.setTimeInMillis(diff);
    		  add = "-" + cal.get(Calendar.DAY_OF_YEAR);
    	  }
    	  
    	 this.getFixedAssortmentData();
    	
    	   String sql1 = "select sortgroup, count(prodid) as number from [filee_mj].[dbo].[mj_assortment] where " +
             " DATEADD(dd, 0, DATEDIFF(dd, 0, sortdate)) = DATEADD(dd, 0, DATEDIFF(dd, 0, GETDATE()" + add + ")) " +
               " group by sortgroup";//t�nase sortimendi laiuse leidmine

    	   String sql2 = "select sortgroup ,prodid ,U_unikood +' ' + t.name as prodname from [filee_mj].[dbo].[mj_assortment]" +
    	   		" inner join [filee].[dbo].[grarticle] t on t.code =prodid where " +
             " DATEADD(dd, 0, DATEDIFF(dd, 0, sortdate)) = DATEADD(dd, 0, DATEDIFF(dd, 0, GETDATE()" + add + ")) " + 
             " group by sortgroup,prodid,t.name,U_unikood";  // t�nase sortimendi leidmine
    	   HashMap sortLen = new HashMap();
    	   HashMap sortIse = new HashMap();
    	   HashMap tnimed = new HashMap();
    	   
    	   try{
    		   Connection con1 = this.getNewConnection();
        	   Statement st = con1.createStatement();
        	   ResultSet rs = st.executeQuery(sql1);
        	   
        	   while(rs.next()){
        		   sortLen.put(rs.getString("sortgroup").trim(),rs.getString("number").trim());
        	   }
        	   
              rs = st.executeQuery(sql2);
        	   
        	   while(rs.next()){
        		   Vector sort = new Vector();
        		   String sortim = rs.getString("sortgroup").trim();
        		   String prodid = rs.getString("prodid").trim();
        		   String prodname = rs.getString("prodname").trim();
        		   tnimed.put(prodid, prodname);	
        		   if(sortIse.get(sortim) != null){
        			   sort = (Vector)sortIse.get(sortim);
        			   sort.add(prodid);
        		   }else{
        			   sort.add(prodid);
        			   sortIse.put(sortim,sort);  
        		   }
        		   
        	   }
        	   
        int counter = 0;	   
        	   //L�pes sortimentide sisselugemine
        	   
        	   // vaja leida mitu toodet oli m�rgitud kontrolli ajal kohale 
        	   //ja ,m��rame kes k�is mis kaupluses ja kellaaeg
        String nu = "8";
        if(day){
        	nu = "1";
        	Calendar cc = Calendar.getInstance();
        	if(cc.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY){
        		nu = "3";
        	}
        }
        	   
  String sql3 = "select prodid,kl.I_Sortiment as sortgroup ,sup.custid,sup.custname,DATEADD(dd, 0, DATEDIFF(dd, 0, sup.sentdate)) as date," +
       	" rtrim(sup.agentid) +' ' + rtrim(sup.agentname) as agent ,sup.resstr3 as time,sup.resint3  as status" +
        " from [filee_mj].[dbo].[supervisor] sup inner join [filee].[dbo].[grcompany] kl on kl.code=sup.custid " +
        " where prodid is not null and kl.I_Sortiment is not null and regno like '%" + regno + "%' and DATEADD(dd, 0, DATEDIFF(dd, 0, sup.sentdate)) > getdate()-" + nu +
        		" order by DATEADD(dd, 0, DATEDIFF(dd, 0, sup.sentdate)) desc";
        	   HashMap visited = new HashMap();
        	   HashMap names = new HashMap();
        	   HashMap agen = new HashMap();
        	   HashMap sorti = new HashMap();
        	   TreeMap tootedPoed = new TreeMap();
        	   HashMap times = new HashMap();
        	   HashMap visitedcoun = new HashMap();
        	   Vector days = new Vector();
               HashMap puuduva = new HashMap(); 
               HashMap olemas = new HashMap();
               HashMap korras = new HashMap(); 
               rs = st.executeQuery(sql3);
   System.out.println(sql3);
  while(rs.next()){
	   Vector sort = new Vector();
	   String prodid = rs.getString("prodid").trim();
	   String sortim = rs.getString("sortgroup").trim();
	   String custid = rs.getString("custid").trim();
	   String custname = rs.getString("custname").trim();
	   String date = rs.getString("date").trim().substring(0,10);
	   String agent = rs.getString("agent").trim();
	   String time = rs.getString("time");
	   int status = rs.getInt("status");
	   if(time != null && time.trim().length() != 0){
		   long lll = Long.parseLong(time.trim());
		   Calendar cc = Calendar.getInstance();
		   cc.setTimeInMillis(lll);
		   time = getYYYYpMMpDDpTimeNiceString(cc.getTime());
		   date = time.substring(0,10);
		   if(!days.contains(date)){
				  days.addElement(date);
			  }
	   }else{
		   if(!days.contains(date)){
				  days.addElement(date);
			  }
		   time = date + " 00:00:00";
	   }
	   	
	   agen.put(custid, agent);
	   names.put(custid, custname);
	   sorti.put(custid, sortim);
	   times.put(custid+date, time);

	   if(visitedcoun.get(custid) != null){
		   Vector dddd = (Vector)visitedcoun.get(custid);
		   if(!dddd.contains(date))dddd.add(date);
		   visitedcoun.put(custid,dddd);  
	   }else{
		   Vector dddd = new Vector();
		   dddd.addElement(date);
		   visitedcoun.put(custid,dddd);  
	   }	   

	   
	   if(sortIse.get(sortim) != null){
		   
		   Vector res = (Vector)sortIse.get(sortim);
		   if(status == 999){
			   if(puuduva.get(custid + date) == null){
				   counter++;
              Vector puud = (Vector)res.clone();
           //   System.out.println("Kloonin " + puud.size() + " " + custid );
                   puud.removeElement(prodid);
				   puuduva.put(custid + date,puud);    
			   }else{
				   Vector puud = (Vector) puuduva.get(custid + date);
		           puud.removeElement(prodid);
				   puuduva.put(custid + date,puud);   
			   } 
		   }
		   if(status == 888){
			   if(olemas.get(custid + date) == null){
				   counter++;
              Vector puud = (Vector)res.clone();
           //   System.out.println("Kloonin " + puud.size() + " " + custid );
                   puud.removeElement(prodid);
                   olemas.put(custid + date,puud);    
			   }else{
				   Vector puud = (Vector) olemas.get(custid + date);
		           puud.removeElement(prodid);
		           olemas.put(custid + date,puud);   
			   } 
		   }
		   
		   if(status == 777){
			   if(korras.get(custid + date) == null){
				   counter++;
              Vector puud = (Vector)res.clone();
                   puud.removeElement(prodid);
                   korras.put(custid + date,puud);    
			   }else{
				   Vector puud = (Vector) korras.get(custid + date);
		           puud.removeElement(prodid);
		           korras.put(custid + date,puud);   
			   } 
		   }
			   
		  
	   }
	   if(visited.get(custid+date) != null){
		   sort = (Vector)visited.get(custid+date);
		   sort.add(prodid);
		   visited.put(custid+date,sort);  
	   }else{
		   sort.add(prodid);
		   visited.put(custid+date,sort);  
	   }	   
  } 
  
  
StringBuffer sb = new StringBuffer();

StringBuffer sbf = new StringBuffer();

Iterator ite = names.keySet().iterator();
int total = 0;
double perc = 0;
while (ite.hasNext()){

	String custid = (String)ite.next();
	
	String sortcode = (String)sorti.get(custid);
	sb.append("Klient :\t" + names.get(custid) +  "\n");
	sb.append("Kontrollis:\t" + agen.get(custid) +  "\n");
	
	sbf.append("Klient :," + names.get(custid) +  "\n");
	sbf.append("Kontrollis:," + agen.get(custid) +  "\n");
	
	
	Enumeration ed =  ((Vector)visitedcoun.get(custid)).elements();
	while(ed.hasMoreElements()){
		total++;
		String date = (String)ed.nextElement();
		sb.append("Aeg :\t\t" + times.get(custid + date).toString().substring(0,19) +  "\n");	
		sbf.append("Aeg :," + times.get(custid + date).toString().substring(0,19) +  "\n");	
	

		double sss = Double.parseDouble((String)sortLen.get(sortcode));
	
		double mmm = Double.parseDouble(((Vector)visited.get(custid+date)).size()+ "");
		mmm = sss - mmm;
		if(korras.get(custid+date) != null){
			mmm = sss;
		}
		//keerame vastupidi m�rgivad mid n�evad m��gil 
		double nnn = mmm/sss*100d;	
		int per = (int)(nnn);
		perc = perc + nnn;	
		sb.append("Sortimendis " + (int)sss + " / M��gil " + (int)mmm + " / " + per + "% \n\n");
		sbf.append("Sortimendis, M��gil ,% \n");
		sbf.append((int)sss + "," + (int)mmm + "," + per + "% \n\n\n\n");
	
	}
	    sb.append("\n\n");
	
	

}


// Sortimendi v�rdlus  puuduolevad tooted ja v�ljaspool olevad tooted


StringBuffer lisa = new StringBuffer();

StringBuffer lisaf = new StringBuffer();
//System.out.println("puuduvad " + puuduvad.keySet());
Iterator itera = olemas.keySet().iterator();
boolean first = true ;
while (itera.hasNext()){
	String id = (String)itera.next();
	//System.out.println(id);
	String paev = id.substring(id.length() - 10);
     String custid = id.substring(0,id.length() - 10);
    // System.out.println(custid + "ee" + paev);
     if(first){
		lisa.append("Lisainfo Puuduste kohta\n");
		lisaf.append("Lisainfo Puuduste kohta\n");
		first = false;
	}
	Vector vvv = (Vector)olemas.get(id);
	int size = vvv.size();
	
	int suurus = ((Vector)sortIse.get((String)sorti.get(custid))).size();
	if(korras.get(id)!= null){
		size = suurus;
	}
	lisa.append(names.get(custid) + " " + paev + "\nPUUDU: " + (suurus - size) + " toodet\n");
	lisaf.append(names.get(custid) + "," + paev + "\nPUUDU:," + (suurus - size) + "\n");
	
	Vector olla = (Vector)((Vector)sortIse.get((String)sorti.get(custid))).clone();
//	System.out.println("olla " +  olla.size()+ " vvv " + vvv.size());
	for (int u= 0 ; u < vvv.size(); u ++){
		 olla.remove((String)vvv.elementAt(u) );
	 }
	
	if(korras.get(id)!= null){
		olla.removeAllElements();
	}
	//System.out.println("olla2 " +  olla.size()+ " vvv " + vvv.size());
	 
	Enumeration enu = olla.elements();
	while(enu.hasMoreElements()){
		String prodid = (String) enu.nextElement();
		lisa.append("\t" + prodid + " " + tnimed.get(prodid) + "\n");
		lisaf.append(prodid + "," + tnimed.get(prodid) + "\n");
		
		addToDB(paev,custid,(String)names.get(custid),(String)sorti.get(custid),prodid,(String)tnimed.get(prodid));
		
		if(tootedPoed.get(prodid + " " +  tnimed.get(prodid)) != null){
			Vector kli= (Vector)tootedPoed.get(prodid + " " +  tnimed.get(prodid));
			kli.addElement(names.get(custid));
			tootedPoed.put(prodid + " " +  tnimed.get(prodid), kli);
		}else{
			Vector kli= new Vector();
			kli.addElement(names.get(custid));
			tootedPoed.put(prodid + " " +  tnimed.get(prodid), kli);	
		}
		
	}
	lisa.append("\n\n");
	lisaf.append("\n\n");
}
	


StringBuffer toodePood = new StringBuffer();

Iterator iteri = tootedPoed.keySet().iterator();
while (iteri.hasNext()){
	String toode = (String)iteri.next();
	toodePood.append(toode + "\n");
	
	Vector vvv = (Vector) 	tootedPoed.get(toode);
	toodePood.append("puudub " + vvv.size() + " poes\n");
	
 Enumeration enumi = 	vvv.elements();
 while (enumi.hasMoreElements()){
	 toodePood.append("\t"  + enumi.nextElement() + "\n");
 }
 toodePood.append("\n\n");
}


if(total == 0){
	total = 1;
}
String text = "Kontrollperiood : " + days.toString() + "\nKontrollimise kordade arv  : " + counter 
+"\nKeskmiselt Kauplustes Saadavus " + (int)perc/total + "%\n\n";
String textf = "Kontrollperiood : " + days.toString() + "\nKontrollimise kordade arv  : " + counter 
+"\nKeskmiselt Kauplustes Saadavus " + (int)perc/total + "%\n\n";

  //System.out.println(text + "\n" + sb.toString() + lisa.toString());
  
  
  Calendar ctc = Calendar.getInstance();
  
 String filename = "dreports/" +"DR-RRLT-" + getYYYYpMMpDDpTimeNiceString(ctc.getTime()).substring(0, 10) + ".csv";  
  
     File f = new File(filename);
     if (f.exists())f.delete();
       try {
 		PrintWriter os = new PrintWriter(new FileWriter(
 				filename, true));
 		os.write(textf + "\n" + sbf.toString() + "\n" + lisaf.toString());
 		os.close();
 	  }catch (IOException ex) {
 		ex.printStackTrace();
 	  }
  
  Email email = new Email(getParent().getString("datasent"));
 
//  email.setNoticeTo(parseSecondEmail(getEmail(agentCode)));
  email.setFrom(getParent().getProps().getProperty("from"));
  email.setSMTP(getParent().getProps().getProperty("smtphost"));
  email.setUser(getParent().getProps().getProperty("smtpuser"));
  email.setPass(getParent().getProps().getProperty("smtppass"));
  email.setPort(getParent().getProps().getProperty("smtpport"));
  email.setSubjectFilesTo("Distributsiooni aruanne - Filee Lihat��stuse AS klient " + regno);
  email.setBody(text + "\n" + Converter.deCode(sb.toString()) + "\n" + Converter.deCode(lisa.toString()) + "\n" + Converter.deCode(toodePood.toString()));
  email.addAttach(filename);
  System.out.println("Saadan Emaili");
  //iseendale saatmine
  String toOwn = getParent().getProps().getProperty("distribution.ownemail");
 if(!day){
	  StringTokenizer stt = new StringTokenizer(toOwn,",");
	  while(stt.hasMoreElements()){
		  email.setFilesTo(stt.nextToken());
		  email.send();  
	  }	  
	  email.setFilesTo(toThem);
	  email.send();
 }

        	   
    	   }catch(Exception e){
    		   e.printStackTrace();
    	   }
    	   
    	   
    	  }
    	  if(!day){
    		  System.exit(0);
    	      	  
    	  }
    	}
      
 	  public String  getCustProdDiscounts(Agent agent){
	  return null;
	  }
	
 	  
 	  
 	  private String getYYYYpMMpDDpTimeNiceString(Date dd){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    cal.setTime(dd);
		    
		    
		    
		    String hour =   cal.get(Calendar.HOUR_OF_DAY) + "";
		    
		    
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}

			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}

			sb.append("" + cal.get(Calendar.YEAR));
			sb.append("-" + month  + "-");
			sb.append(day +" ");
		    
		    String min = String.valueOf(cal.get(Calendar.MINUTE));
		    String sec = String.valueOf(cal.get(Calendar.SECOND));
		     if (hour.length() == 1){
		      sb.append("0");
		    }
		     sb.append(hour);
		     sb.append("_");
		    if (min.length() == 1){
		     sb.append("0");
		   }
		     sb.append(min);
		     sb.append("_");
		     if (sec.length() == 1){
		        sb.append("0");
		      }
		    sb.append(sec);
		    return sb.toString();
		  }
	

 	  
 	    public String getPilotDayData(Agent agent){

 	        
 	       String sql =  " select d.id,case when substring(description,0,3)!='MJ' then 1 else 0 end as abi ,(d.companyname) AS nimi,(d.docdate) AS kuup ,(d.docno) AS number , (d.totalamount) as summa ,k.i_agent " +
 	    		    "  FROM sdorder d INNER JOIN grcompany k on k.id = d.idgrcompany " +
 	    			"	    WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)>=MONTH(getdate())"
 	    			+ " AND (day(d.docdate)= day(getdate()) or day(d.docdate)= day(getdate()+1)) and k.i_agent = ? "+ 
					"		order by (d.companyname)";
 	    			     
 	        
 	StringBuffer sb = new StringBuffer();
 	  this.connectionUp();
 	try {
 		
 		 String header = " T�nased,homsed tehingud :\n\n";
         String header1 = " Jooksva kuu m��k : ";
         boolean first = true;
         nf.setMinimumFractionDigits(2);
           	
        
 		Connection con = this.getNewConnection();
 	PreparedStatement stmt2 = con.prepareStatement(sql);
 	 stmt2.setString(1, agent.getID());
 	 String sep= System.getProperty("line.separator");
 	    ResultSet  rs = stmt2.executeQuery();
 	 double droidsum = 0;
 	 double muusum = 0;
 	 boolean change = false;

 	 boolean droid = true;
 	 
 	 while(rs.next()){
 		 int id = rs.getInt("id");
 		 int abi = rs.getInt("abi");
 		  	 
 	     String nimi = rs.getString("nimi");
 	     if (nimi == null){
 	     nimi = " ";
 	     } else {nimi = nimi.trim();}

 	 	sb.append(nimi + "\n");
 	 	sb.append(rs.getString("kuup").trim().substring(0,10));
 	 	sb.append("  Nr." + rs.getString("number").trim() + "\t");
 	 	double summa = Double.parseDouble(rs.getString("summa").trim());
 	 
 	 	
 	 	String sum = getNF().format(summa) ;
 	     sb.append(sum + " eur\n" + sep);
 	   
 	     String sql3 = "select quantity as amount, case when idgrunit = 6 then 'kg' else 'tk' end as unit  , name from sdorderrow where idsdorder = " + id;
 	     Connection cc = this.getNewConnection();
 	     PreparedStatement stmt3 = cc.prepareStatement(sql3);
 	 	 
 	        ResultSet  rs2 = stmt3.executeQuery();
 	    
 	 	    while(rs2.next()){
 	 	    	sb.append("[" +nf.format(rs2.getDouble("amount"))+"] " 
 	 	       + rs2.getString("unit").trim()+ " " + rs2.getString("name").trim() + "\n");
 	 	    }
 	    
 	 	    rs2.close();
 	        stmt3.close();
 	        cc.close();
 	     	sb.append( "\n\n");
 	 	 	  
 	     
 		if(abi == 0){
 	  		droidsum = droidsum + summa;
 	  	}else{
 	       change = true;
 	  		muusum = muusum + summa;
 	  		if(droid && droidsum > 0 ){
 	  			droid = false;
 	  			sb.append("------------------------------\n");
 	  			sb.append("Androidist Kokku :" + getNF().format(droidsum)  + "eur\n\n");
 	  		}
 	  	}
 	 }
 	 rs.close();
 	 stmt2.close();
     con.close(); 	 
 		if(droid && droidsum > 0 ){
 				droid = false;
 				sb.append("------------------------------\n");
 				sb.append("Androidist Kokku :" + getNF().format(droidsum)  + "eur\n\n");
 			}
 	 
 	 if(muusum > 0){
 			sb.append("------------------------------\n");
 			sb.append("E-tellimused Kokku :" + getNF().format(muusum)  + "eur");
 			 
 	 }
 	 
 	} catch (Exception e) {
 	 getParent().showError(e);
 	}

 	connectionDown();



 	Converter.saveFile(parent.getProps().getProperty("stockrefdir") + "st"+ agent.getID() + "report.txt", sb.toString());
 	//System.out.println(sb.toString());
 	return "";//sb.toString();

 	    }
 	  
 	  
 	  
 	   public String getPilotDayData2(Agent agent){

 	  	

        StringBuffer sb = new StringBuffer();
        
 	   	
 	    String cmd = "SELECT  w.code AS agent ,SUM(r.amount)  AS summa " +
		    " FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
		    " INNER JOIN grcompany cus  ON cus.id=d.idgrcompany " +
		    " INNER JOIN eremployee w ON w.id= cus.I_kliendihaldur " + 
		    " INNER JOIN grarticle t ON t.id= r.idgrarticle " +
		    " WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)=MONTH(getdate()) AND r.idgrunit!=1 " +
		    " AND (r.articletype='P' OR r.articletype='G') and  w.code='" + agent.getID()+ "' GROUP BY w.code";

	       
	            
        String sql2 =  "select max(d.companyname) AS klient ,max(d.docno) AS arve, max(d.docdate) AS kuupaev , max(d.totalamount*exchangerate ) as summa " +
     " FROM sdorder d INNER JOIN eremployee w ON w.id=d.ideremployee " +
		   "  WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)=MONTH(getdate()) AND DAY(d.docdate)=DAY(getdate()) " +
			" and substring(description,0,3)!='MJ'  and  w.code = '" + agent.getID()+ "'  GROUP BY d.docno";
	     
   
        
        System.out.println("siin");
          String header = " T�nased tehingud :\n\n";
          String header1 = " Jooksva kuu m��k : ";
          boolean first = true;
          nf.setMinimumFractionDigits(2);
            try {
            	
            	
            	
            	
            	Connection con = getNewConnection();
	              Statement stmt = con.createStatement();
	              ResultSet	      rs = stmt.executeQuery(cmd);
	              if(rs.next()) {
	                sb.append(header1 + nf.format(rs.getDouble("summa"))+  " EUR\n\n");
	                
	              }
	              rs.close();
	              stmt.close();
	              con.close();
	         
            	
            	
            	
            	
	              Connection con2 = getNewConnection();
            	
                     Statement st = con2.createStatement();
                    ResultSet rs2 = st.executeQuery(sql2);
                        while (rs2.next()) {
                        	if(first){
                        		sb.append(header);
                        		first = false;
                        	}
                        	String nimi = rs2.getString("klient").trim() ;
                      
                      
                        sb.append(nimi  +
                        		"\n    (" + rs2.getString("arve").trim() + ")[" + rs2.getString("kuupaev").substring(0,10) + "] " +
                        		rs2.getString("summa") + "EUR summa "
                        		+  "\n\n");
                        }

                        rs2.close();
                        st.close();
                        con2.close();
                        
                        } catch (Exception e) {
                            getParent().showError(e);
                        }
                     //   System.out.println(agent.getID() + sb.toString());
           
       
            return sb.toString();

    }

	  public void connectionDown(){
	  }

  
	
	  
    public TreeMap getAgents(){
  	
    String cmd = "select i_agent AS agent,"
    		+ " (select firstname + ' ' + surname from eremployee e where e.id = i_agent) AS nimetus "
    		+ " from grcompany cat where i_agent !='' group by i_agent" ;
  
    if (isDebug()) System.out.println(cmd);
  
   // String dubbler = getParent().getProps().getProperty("dubbler");
    try {
        Connection  con = getNewConnection();     
        Statement stmt = con.createStatement();
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
    stmt.close();
    con.close();

    } catch (Exception e){
        getParent().showError(e);
      }
  /*  Agent aa = new Agent("565");
    aa.setName("HALJALA");
    aa.setObject("607");
    agents.put(aa.getName(), aa);
*/
    Agent hh = new Agent("HOUSE");
    hh.setIsPilot(true);
    hh.setName("Office");
    Agent hh1 = new Agent("HOUSE1");
    hh1.setIsPilot(true);
    hh1.setName("Office");
    agents.put("HOUSE", hh);
    agents.put("HOUSE1", hh1);
 
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
    
	 if(rcompany != null){
		Converter.getManagersReplacements(aHash, rcompany, user , pass);
	 }
  
    try {
    	  String cmd = " select cat.idgrcompany as id , name as kett from grcompanyCatrel cat " +
		" INNER JOIN   grcompanycat arca ON arca.id = cat.idgrcompanycat where arca.category ='K' " ;
    	
    	Connection con2 = getNewConnection();
        Statement st2 = con2.createStatement();
        HashMap ketid = new HashMap();   
        ResultSet rs = st2.executeQuery(cmd);
            while(rs.next()){
        	ketid.put(rs.getString("id"), Converter.enCodeEst("kett"));
        }
    	rs.close();
    	st2.close();
    	con2.close();
    	
      Connection con = getNewConnection();
      Statement st = con.createStatement();
     //  makeCurrentDistribution();
 	   Converter cc = new Converter();
 	  HashMap kyssad =   cc.getQuestionaire(this.getExcelDir());

  
      String cmd2 = " SELECT id , kl.code AS kood,ltrim(kl.name) AS nimi,(select name from grcompany where id=kl.idsettlcompany)as mnimi,kl.locationaddr AS address,U_Kylastusgraafik AS visit ,creditlimit AS limiit,kl.i_agent AS agent, " +
          " (case when len(ltrim(kl.idsettlcompany)) >0 then kl.idsettlcompany else kl.id end) AS payer,-kl.pricecut AS ale ,phone,i_ladu  AS ladu  " +
          ",idgrpricelist as grupp ,i_sortiment as sort ,case when message like '%@$$%' then 1 else 0 end as block  "
    
          + "FROM grcompany kl  WHERE  ltrim(kl.id)>0  and kl.i_agent is not null ORDER BY ltrim(kl.name)";
  

      
      if (isDebug()) System.out.println(cmd2);
      rs = st.executeQuery(cmd2);
      HashMap custMap = new HashMap();

      while(rs.next()){
    	  
    	  String id  = rs.getString("id").trim();
          String accountnum = rs.getString("kood").trim();
          Customer cust = new Customer(accountnum);
          String name = rs.getString("nimi").trim();
          String mnimi = rs.getString("mnimi");
          
          if(mnimi != null && mnimi.trim().length() >0 && name.endsWith(mnimi.trim())){
        	  name = mnimi.trim() + " " + name.substring(0,name.indexOf(mnimi.trim())).trim();
        	  
          }
          if(ketid.get(id) !=null){
        	  cust.setChain((String)ketid.get(id));
          }
          cust.setName(Converter.enCodeEst(name));
          String aadress1 = rs.getString("address");
          if (aadress1 != null) {aadress1 = aadress1.trim();} else {aadress1 = " ";}
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress1),"Estonia"));
          String visit = rs.getString("visit");
          if(visit != null && visit.trim().length()>0){
            cust.setVisit(visit.trim());
          }
       
         String limiit = rs.getString("limiit");
         // System.out.println(limiit);
          if (limiit != null && limiit.trim().length()>0){
          	String lim = limiit.substring(0,limiit.indexOf(".")).trim();
          	if (lim.length()>0){
          		cust.setCredit(lim);		
          	 }
          	}
          String ag = rs.getString("agent").trim();
          cust.addAgent(new Agent(ag));
          cust.addAgent(new Agent("HOUSE"));
          cust.addAgent(new Agent("HOUSE1"));
          
          Agent aa = (Agent)aHash.get(ag);
          //System.out.println("agents" +aa);
          if (aa != null){
           // System.out.println("agents" +aa.getDubbler().toString());
          	if(aa.getDubbler()!= null && aa.getDubbler().size()>0){
          	//	System.out.println("agents" +aa.getDubbler().toString());
          		for(int i = 0;i<aa.getDubbler().size();i++){
          			cust.addAgent(new Agent((String)aa.getDubbler().elementAt(i)));	
          			//System.out.println("agent lisatude" + cust.getAgents());
          		}
              		
          	}
          }
        
             
          
          
          cust.setPayer(rs.getString("payer").trim());
         // double ale  = rs.getDouble("ale");
         // if (ale>0){cust.setDiscount(ale);}
         // cust.setDebt(rs.getDouble("debt"));
          

         // String info = rs.getString("info");
          //if (info != null && info.trim().length()>0){cust.setInfo(info.trim());}
          
      
          
          String phone = rs.getString("phone");
          if(phone != null && phone.trim().length()>0){
          	cust.setPersonalData("tel. " +phone.trim());
          }
          
          

          String ladu = rs.getString("ladu");
          if(ladu != null && ladu.trim().length()>0){
          	cust.setWare(ladu.trim());
          }
          
         String grupp = rs.getString("grupp");
          if(grupp != null && grupp.trim().length()>0){
          	cust.setGroup(grupp.trim());
          }
          
          String sort = rs.getString("sort");
          if(sort != null && sort.trim().length()>0){
          	cust.addFixSortCode("S" + sort.trim());
          }
          String plokk = rs.getString("block");
          if(plokk != null && plokk.trim().length()>0 && plokk.equals("1")){
          	cust.setSalesBlockT(true);
          }
          
          cc.addQuestiosToCustomer(cust,kyssad);
          
          customers.put(name, cust);
          custMap.put(cust.getID(), cust);
    }


      String cmd3 = "SELECT d.idpartner AS maksja, " +
		 "d.totalamount  - sum(case when m.amountbase is null then 0 else m.amountbase end ) AS summa FROM sdinvoice d " +
		 "INNER JOIN grcompany k on k.id = d.idgrcompany " +
		 "LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
		 "WHERE docstate !='F'  AND d.promptdate < getdate()  group by d.sourcedocno,d.idpartner, " +
		 "d.promptdate,d.totalamount  HAVING d.totalamount  - sum(case when m.amountbase is null then 0 else m.amountbase end ) != 0 ORDER BY d.idpartner";
		
	     if (isDebug()) System.out.println(cmd3);
	        ResultSet rs1 = st.executeQuery(cmd3);
	         while (rs1.next()) {
	           String id = rs1.getString("maksja").trim();
	           double debt =rs1.getDouble("summa");
	          
	           Iterator iter = custMap.values().iterator();
	           while(iter.hasNext()){
	          Customer cust = (Customer)iter.next();   	
	           	if(cust.getPayer().equals(id)){
	                cust.setDebt(cust.getDebt()+ debt);
	            //     System.out.println(id + "  " + debt);	
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
  
  private Connection getNewConnection(String url) throws SQLException{
	    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	  }
	  
  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "  SELECT id AS liik , left(code,3)+ ' ' + name AS nimetus  FROM grarticlecat " +
    		       "  WHERE category='tooterühm' or category='suveHinnakiri' or category='jouluHinnakiri' ORDER BY code,name  ";
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
        TreeMap   tm = new TreeMap();
    return tm;
  }

    



  public TreeMap getProducts(Agent agent){
	  eanprods= new TreeMap();

    String cmd = "SELECT pr.code AS kood, rtrim(pr.name) + ' ' +  rtrim(pr.code)  AS nimi,  " +
    	"	hi.price AS hind,U_unikood AS EAN ,cat.idgrarticlecat AS grupp, " +
        " pr.contentquantity AS tykke ,y.name AS yhik ,u_komp_ruum as type   " +
        " FROM grarticle pr RIGHT JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id RIGHT JOIN grpricelistrel hi ON " +
        " hi.idgrarticle=pr.id RIGHT JOIN grpricelist pl ON pl.id=hi.idgrpricelist RIGHT JOIN  " +
		" grarticlecat arca ON arca.id = cat.idgrarticlecat  " +
        " RIGHT JOIN grunit y ON y.id=pr.idgrunit  " +
        " WHERE rtrim(pl.code)='1'  and arca.category = 'tooterühm'  and  U_Merisoft = 1  and pr.code not like 'arh%' " + // and pr.name not like 'EI TO%'
		" order by pr.code ";
    
    String cmd2 = " SELECT pr.code AS toode,SUM(quantity) AS seis FROM dcurbalancestore ss  " +
    		" INNER JOIN grarticle pr ON idgrarticle = pr.id WHERE ss.iddrstore in (2,5) AND quantity >0 GROUP BY  pr.code " ;

    StringBuffer top = new StringBuffer();
    
    String cmd3 = " select pr.code as toode, Ltrim(kir.name) +  " +
" case when U_Parimenne > getdate()-400 then ' P:' + ltrim(str(day(U_Parimenne)))+'.'+ltrim(str(month(U_Parimenne)))+  " +
" '.'+substring(ltrim(str(year(U_Parimenne))),3,4) else '' end + " +
" ' S:' as info , round(jaak.quantity,2) as jaak from DCurDRStockReserve jaak inner join " +
"    	 DRStockReserve kir on kir.id=jaak.iddrstockreserve inner " +
"    		join  grarticle pr on pr.id=jaak.idgrarticle order by pr.code " ;
    //siin on k�sa kas k�ik laod liidame kokku 
    
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {
      Connection con2 = getNewConnection();
      Statement st2 = con2.createStatement();
      ResultSet rs2 = st2.executeQuery(cmd2);
      Hashtable tooted = new Hashtable();
        while(rs2.next()){
          String id = rs2.getString("toode").trim();
          Product toode = new Product(id);
          double sto = rs2.getDouble("seis");
          toode.setStock(sto);
          //toode.setMeasure(ost);
          tooted.put(id,toode);
        }
        
        
        
        ResultSet rs3 = st2.executeQuery(cmd3);
        Hashtable tooteinfo = new Hashtable();
          while(rs3.next()){
            String id = rs3.getString("toode").trim();
            Product toode = new Product(id);
        String info = rs3.getString("info");
        String kog = rs3.getString("jaak");
        if(kog.indexOf(".")!=-1){
        	kog = kog.substring(0,kog.indexOf("."));
        }
           if(info != null && info.trim().length()>0){
           	if(tooteinfo.containsKey(id)){
           	String partii = ((Product)tooteinfo.get(id)).getPartii() + "|" +info.trim() + kog;	
           	toode.setPartii(partii);
           	}else{
           	  toode.setPartii(info.trim()); 		
           	}
            
           
            tooteinfo.put(id,toode);
           }
         
          }
        
        
       // System.out.println(tooted.toString() + tooted.size());

      Connection con = getNewConnection();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(cmd);

      while(rs.next()){
        String kood = rs.getString("kood").trim();
        Product toode = (Product)tooted.get(kood);
        double kogus;
    
        if (toode == null) {
          kogus = 0;      
        } else {
          kogus = round(toode.getStock(), 2);
         // System.out.println(kogus + "  " + kood);
         }

        Product info = (Product) tooteinfo.get(kood);
       
        
         Product prod = new Product(kood);
         if(info != null){
     	   prod.setPartii(info.getPartii());
     }
         String nimetus = rs.getString("nimi").trim().replaceAll("Rannarootsi", "RR");
         prod.setStock(kogus);
        // System.out.println(prod.getStock() + "  " + kood);
         double hind = rs.getDouble("hind");
         prod.setPrice(hind);
         prod.setName(Converter.enCodeEst(nimetus));
         String ean = rs.getString("ean");
         if (ean != null && ean.trim().length() > 5) {
           prod.setEAN(ean.trim());
           eanprods.put(ean.trim(), prod.getID());
         }

         String grupp = rs.getString("grupp");
         if (grupp != null && grupp.trim().length() > 0) {
           prod.setGroup(grupp.trim());
           if(grupp.trim().equals("770") || grupp.trim().equals("771")){
        	   prod.setType(Converter.enCodeEst("Rannam�isa"));  
           }else{
        	   prod.setType("Rannarootsi");
           }
         }
         
 

         prod.setInCases( (rs.getDouble("tykke")));
        String unit = rs.getString("yhik");
        if (unit != null && unit.trim().length() > 0) {
          prod.setUnit(unit.trim());
        }
        
       
        
        
        products.put(nimetus, prod);
      }
      rs.close();
      st.close();
      con.close();
      
      if(top.length() > 2){
        File f = new File(  "supervisor.txt");
        if (f.exists())f.delete();
          try {
    		PrintWriter os = new PrintWriter(new FileWriter(
    				 "supervisor.txt", true));
    		os.write(top.toString());
    		os.close();
    	  }catch (IOException ex) {
    		ex.printStackTrace();
    	  }
     }

    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }


  public Vector getCustomerProductsList(String agentCode){
  	

    String sql1 = 	"SELECT k.code AS klient,t.code AS toode, r.quantity AS kogus ,r.docdate as kuupaev ,r.unitprice AS lastprice  , " +
    "  t.U_SailAeg as aeg, (case when r.docdate+t.U_sailAeg >getdate() then 0 else 1 end) as days " +
  "  FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc  " +
  " INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=abi.companyname  " +
  "  WHERE r.docdate>getdate()-30 AND len(ltrim(abi.idgrcompany))>0  " +
  "  AND (r.articletype='P' OR r.articletype='G') GROUP BY k.code,t.code,r.quantity,r.docdate,r.unitprice ,t.U_SailAeg " +
"	ORDER BY k.code,t.code,r.docdate " ; 
    
    
    String sql2 = " SELECT k.code AS klient,t.code AS toode,SUM(case when quantity>0 then quantity else 0 end) AS plusskogus, " +
    		"SUM(case when quantity<0 then quantity else 0 end) AS minuskogus " +
      "FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
      "INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=abi.companyname " +
      "WHERE r.docdate>getdate()-30 AND len(ltrim(abi.idgrcompany))>0 " +
      "AND (r.articletype='P' OR r.articletype='G') GROUP BY t.code,k.code ORDER BY t.code,k.code ";

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
                
                double aeg = rs.getDouble("aeg");
               
                Customer cust = (Customer)kliendid.get(kliendikood);
                if (cust == null){
                  cust = new Customer(kliendikood);
                }
                Product prod = new Product(tootekood);
                
                CustProd cp =  new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,lastdate,hind,0,0," ");
                
                if (aeg >0){
                  	 cp.setRedline(rs.getString("days"));
                  }
                
                cust.addProduct(prod,cp,false);
                
                kliendid.put(kliendikood, cust);
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

            

	           int counter = 0;
	           int cou = 0;
	          String oldklient = null;
	          String olddate = " ";

	          String cmd2 =  "SELECT k.code AS klient,t.code AS toode, r.quantity AS kogus ,r.docdate as kuupaev  " +
  "  FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc  " +
  " INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=d.companyname  " +
  "  WHERE r.docdate>getdate()-30 AND len(ltrim(d.idgrcompany))>0  " +
  "  AND (r.articletype='P' OR r.articletype='G') " +
"	ORDER BY k.code,r.docdate desc,t.code "; // klient,kuupaev desc,toode
	          System.out.println(cmd2);
			   //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"
			    Connection con2 = getNewConnection();
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
	                   date = rs2.getString("kuupaev").trim().substring(0,10);

	               if(cou == 0){oldklient = klient;olddate = date;}
	               if(!oldklient.equals(klient)){
	                             counter = 0;
	                             oldklient = klient;
	                             olddate = date;
	               }
	               
	               Customer c = (Customer)kliendid.get(klient);
	               if (c != null ){

	                 ArrayList al = (ArrayList)c.getProducts().get(toode);
	                 if(!olddate.toString().equals(date.toString())){
	                  	 counter++;
	                  	 }
	                 
	                 if (al != null){
	                   CustProd cp = (CustProd)al.get(1);
	                   if (cp != null){
	                     if (counter == 0){cp.setS1(kogus );}
	                     if (counter == 1){cp.setS2(kogus );}
	                     if (counter == 2){cp.setS3(kogus );}
	                     if (counter == 3){cp.setS4(kogus );}
	                     if (counter == 4){cp.setS5(kogus );}

	                     custProds.add(cp);
	                   }
	                 }
	                 oldklient = klient;
	                 olddate = date;
	               }
	               cou++;
	             }


            
            
            
            } catch (Exception e) {
              getParent().showError(e);
            }
            connectionDown();
            return custProds;
  }

  
 


  
  public void setBulkSales(Map agents){

    String cmd = "	SELECT  d.IDWorker  AS agent ,SUM(r.amount)  AS summa ,sum(case when t.idgrunit = 6  then r.quantity else  t.netmass*r.quantity end) as kg  " +
		 "    FROM gdocument d INNER JOIN sdinvoicerow r ON d.id=r.idsdinvoice  " +
		 "   INNER JOIN grcompany cus  ON cus.id=d.idgrcompany	 " +
		 "   INNER JOIN grarticle t ON t.id= r.idgrarticle  " +
		 "   WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)=MONTH(getdate()) " +
		 "   AND (r.articletype='P' OR r.articletype='G') and d.IDWorker is not null GROUP BY d.IDWorker";

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
                  agent.setKgSales((int)rs.getDouble("kg"));
                 // agent.setMonthSalesVol((int)rs.getDouble("summavol"));
                 // System.out.println(agent.getMonthSales());
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

 return null;
      }



  public Vector getDebt(String agentCode) throws SQLException{
    Vector list = new Vector();
    String sql = "SELECT kl.code AS maksja,d.sourcedocno AS arve, d.promptdate AS kuupaev, " +
    "  d.totalamount - sum(case when m.amountbase is null then 0 else m.amountbase end ) AS summa ,-datediff(day,getdate(),d.promptdate)  AS paevi " +
    "  FROM sdinvoice d INNER JOIN grcompany kl on kl.id=d.idgrcompany " +
    " LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
    "  WHERE docstate !='F'  AND d.promptdate <= getdate() " +
    "   group by  d.sourcedocno,kl.code,d.promptdate,d.totalamount  HAVING d.totalamount - sum(case when m.amountbase is null then 0 else m.amountbase end ) != 0  ORDER BY kl.code " ;

  if (isDebug()) System.out.println(sql);
  Connection  con = getNewConnection();     
  Statement stmt = con.createStatement();
  
    ResultSet rs = stmt.executeQuery(sql);

    while(rs.next()){
      Debt debt = new Debt();
      debt.setCustID(rs.getString("maksja").trim());
      debt.setDoknr(rs.getString("arve").trim());
      debt.setDuedate(rs.getDate("kuupaev"));
      
      double summa = rs.getDouble("summa");
      debt.setDue(rs.getString("paevi"));
      
           debt.setSum(summa);
        list.addElement(debt);
      
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
      return "SAFMaag";
    }

    public String getSubjectFilesTo() {
      return this.subjectFilesTo;
    }


    public String getDaySalesData(Agent agent) {
      return null;
    }




    public void insertDefective(DefectiveWrapper wr){
            String sql = "INSERT INTO dbo.MJ_QUALITY VALUES (?,?,?,?,?,?,?,?,?,?,getdate(),?,' ',' ',0,0)";
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
              //    System.out.println(kogus+"bb '"+lw.getLine().getBestBefore()+"'"+bb+"'");
                  ps.setString(8, bb);
                  ps.setInt(9, lw.getLine().getDefectCode());
                  ps.setString(10, lw.getLine().getProdType());
                  ps.setString(11, lw.getLine().getDefectText());

                  ps.executeUpdate();
                }

                con.commit();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
  
  private Connection con2;
  private TreeMap agents = new TreeMap();
  private TreeMap aHash = new TreeMap();

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  
  private String subjectFilesTo = null;
  private NumberFormat NF = NumberFormat.getInstance();
  private TreeMap  dealers = new TreeMap();
}
