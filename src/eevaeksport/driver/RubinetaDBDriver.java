package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalLineWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
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
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class RubinetaDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public RubinetaDBDriver(EksportPanel parent) {
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

   public Vector getPriceMatrixData(){
	
	  	 String    sql = "select '' AS custid,[Sales Code] AS clgroup,d.[Item No_] AS prodid,'' AS prodgroup,  " +
	       " d.[Unit Price] AS price ,0 as disc, d.[Starting Date] as paev,d.[Ending Date] as startday ,d.[Currency Code] as currency " +
	        " from [NAVISION].[dbo].[Rubineta, UAB$Sales Price] d " +
	       " where d.[Starting Date] < getdate()-5  and (d.[Ending Date] > getdate()+1 or " +
	       " year (d.[Ending Date]) = 1753)  order by  [Sales Code] ,d.[Item No_]" ;
	                      
		
		 
	 

     Vector result = new Vector();
       try {
        
    	   Connection con = getNewConnection2();
 	        Statement stmt = con.createStatement();
 	        ResultSet rs = stmt.executeQuery(sql);
         while (rs.next()) {

           String custID = Converter.enCodeEst(rs.getString("custid").trim());
           String clGroup = rs.getString("clgroup");
           String prodID = rs.getString("prodid").trim();
           String prodGroup = rs.getString("prodgroup");
          
           double price = rs.getDouble("price");
           double disc = rs.getDouble("disc");
           String date = rs.getString("paev");
           String sdate = rs.getString("startday");
           String currency = rs.getString("currency");
        
           MatrixLine mline = new MatrixLine();

           if (custID != null && custID.length() > 0) {
             mline.setCustID(custID);
           }
         
           
           if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
             mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
           }

         
           if(date != null && !date.startsWith("1753")){
          	 mline.setEndDate(date.substring(0,10));
           }
           if(sdate != null  && !sdate.startsWith("1753")){
            	 mline.setStartDate(sdate.substring(0,10));
            	 if(mline.getEndDate() == null){
            		 mline.setStartDate(null);
            	 }
             }
           if (prodID != null && prodID.length() > 0) {
             mline.setProdID(prodID);
           }
           if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
             mline.setProdGroup(prodGroup.trim());
             mline.setDiscGroup(prodGroup.trim());
            
           }
           mline.setCurrency(currency);
           if (price != 0) {
             mline.setPrice(price);

           }
           if (disc != 0) {
               mline.setDiscount(disc);

             }
           if(mline.getPrice() > 0 || mline.getDiscount() > 0){
               result.add(mline);    
           }
           
         }
         rs.close();
         stmt.close();
         con.close();
       }catch(Exception e){
      	 e.printStackTrace();
       }
	
  return result;
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

      
        ps.close();
        con.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }
  }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
	  
	     }

	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	     String sql = "INSERT into mj_supervisor_lit values ("
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

   public ManagerData getManagerData() throws SQLException{
     return null;
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
         con.close();
     } catch (SQLException e) {
         e.printStackTrace();
     }
   }

 
   public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		File f = new File(this.getExcelSortDir() + "Asortimento matrica.xls");
		
		
		
		try {
			
			
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheetAt(0);
			HSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(4);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 3; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

		//	 System.out.println(groups.toString());

			for (int i = 7; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";

				if (row.getCell((short) 1) != null) {

					try {
						prodID = (int) (row.getCell((short) 1)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID = (row.getCell((short) 1).getStringCellValue());
					}
					
                      if(!prodID.equals("0")){
                   	
					// Siin l�heme reasisse rokkima ja vaatama kas se toode
					// l�heb kaasa
					for (int a = 3; a < row.getLastCellNum(); a++) {

						String group = (String) groups.get(a + "");
						if (group != null && group.trim().length() > 0) {

							String custID = " ";
							String clGroup = " ";
							String prodGroup = " ";

							String go = null;
							clGroup = group;

							MatrixLine mline = new MatrixLine();
							// Vaatame kas selles veerus on sellel tootel pandud
							// mingi t�his
							if (row.getCell((short) a) != null) {

								try {
									go = (int) (row.getCell((short) a)
											.getNumericCellValue()) + "";
								} catch (Exception e) {
									go = (row.getCell((short) a)
											.getStringCellValue());
								}

							}

							if (go != null && go.trim().toLowerCase().equals("x")) {
                                     if(a > 10){
                                    //	 System.out.println( a + " " + prodID + " " +
                						//		 clGroup + " " + go); 
                                     }
								 

								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(clGroup.trim());
								}

								if (prodID != null
										&& prodID.trim().length() > 0) {
									mline.setProdID(prodID.trim());
									if(!sorts.contains(prodID.trim()))
									sorts.add(prodID.trim());
								}
								if (mline.getProdID() == null
										&& prodGroup != null
										&& prodGroup.trim().length() > 0) {
									mline.setProdGroup(prodGroup);
								}
							
								result.add(mline);
							}
						}
					}
					   
                      }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}


   public DealerProduct DealerProductsList() {

   	DealerProduct dealerProds = new DealerProduct();
    try {
    	  String cmd = " SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [NAVISION].[dbo].[Rubineta, UAB$Item Ledger Entry] l " +
          " INNER JOIN [NAVISION].[dbo].[Rubineta, UAB$Item] t ON [No_]=l.[Item No_] WHERE   t.[Blocked]=0 " + 
          " GROUP BY l.[Location Code],l.[Item No_] having sum(l.[Quantity]) != 0  ORDER BY l.[Item No_]"; 

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

 	  	  
    		  String  cmd = "  SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [NAVISION].[dbo].[Rubineta, UAB$Item Ledger Entry] l " +
          " INNER JOIN [NAVISION].[dbo].[Rubineta, UAB$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('SANDELIS') and t.[Blocked]=0 " + 
          " GROUP BY l.[Location Code],l.[Item No_] having sum(l.[Quantity]) != 0  ORDER BY l.[Item No_] ";

    		         if (isDebug()) System.out.println(cmd);
    		         StringBuffer products = new StringBuffer();
    		         try {
    		        	 Connection con = getNewConnection2();
    		           Statement st = con.createStatement();
    		           ResultSet rs = st.executeQuery(cmd);

    		           while (rs.next()) {

    		             String kood = rs.getString("kood").trim();
    		             double seis = rs.getDouble("kogus");
    		             if(seis > 0){
    		                 products.append(kood +  ";");
    		                 products.append(" ;");
    		                 products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
    		            //   kogused.put(kood, seis + "");
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
           	

        	   Connection con = getNewConnection2();

             TreeMap tm = new TreeMap();

                String cmd = 
                         "  SELECT  [Item Category Code] as esimene,pg.[Code] AS kood ,(pg.[Description]) AS nimi ,(cat.[Description]) as catname " +
                         "  FROM [NAVISION].[dbo].[Rubineta, UAB$Product Group] pg inner join [NAVISION].[dbo].[Rubineta, UAB$Item Category] cat on cat.[Code]=pg.[Item Category Code] " + 
                         "  where pg.[Item Category Code] !=''   order by 1,2" ;
                   try {
              Statement stmt = con.createStatement();
               ResultSet rs = stmt.executeQuery(cmd);
              setTreeGroup(true);
             // tm.put("xxxx", "Tester");
               while (rs.next()) {
               	String esimene =Converter.cleanLitLatEst(rs.getString("esimene"));
               	 String liik = Converter.cleanLitLatEst(rs.getString("kood").trim());
               	 String nimetus = rs.getString("nimi");
               	 String catname = rs.getString("catname");
               	 boolean fir = false;
               	 if(esimene != null && esimene.trim().length()>0){
               		twoone.put(liik, esimene.trim()) ;
                  		fir = true;
                  	}
                
                 String nimetus2 = "";
              
               	  if (fir){
               		  nimetus2 =  nimetus.trim();
               		  nimetus =   catname.trim();             		
               	  }
               	                
               		
                 if (liik != "") {
               	  if (fir){
               		  tm.put(esimene.trim(), Converter.enCodeEst(nimetus.trim()));  
               		    teised.put(esimene.trim() + "|" +liik.trim(), Converter.enCodeEst(nimetus2.trim())); 
               	  }else{
               		  tm.put(liik.trim(), Converter.enCodeEst(nimetus.trim()));  
               	  }                
                 }
               }
             }
             catch (Exception e) {
               getParent().showError(e);
             }
             System.out.println("tootegrupid1");
             connectionDown();
             TreeMap res = new TreeMap();
             TreeMap res2 = new TreeMap();
             Iterator enume = tm.keySet().iterator();
             int i = 1;
             while (enume.hasNext()){
           	  String aa = i + "";
           	  if(aa.length() == 1 ){
           		  aa = "00" + aa;
           	  }
           	  if(aa.length() == 2 ){
           		  aa = "0" + aa;
           	  }
           	  String kood = (String)enume.next();
           	  String nimi = (String)tm.get(kood);
           	  
           	  Iterator  enume2 = teised.keySet().iterator();
                 while (enume2.hasNext()){
              	   String ko =(String) enume2.next();
              	   if(ko.startsWith(kood + "|")){
              		   res2.put(aa + ko.substring(ko.indexOf("|")+1), teised.get(ko));
              		 koodJaGrupp.put( ko.substring(ko.indexOf("|")+1),aa);
              	   }
                 }
           	  
           	  koodJaGrupp.put( kood,aa);
           	  res.put(aa, nimi);
           	  i++;
             }  
           	  
           
             teised = res2;
             
         //    System.out.println("grupid " + res.toString());
          //   System.out.println("grupid2 " + res2.toString());
             
             return res;
           }

           private TreeMap koodJaGrupp = new TreeMap(); 
           public TreeMap getProdGroupsSecond() throws SQLException {
                    
               return teised;
             }


           
           
           
           
  
   private TreeMap twoone = new TreeMap();
   private TreeMap teised = new TreeMap();
   
   public TreeMap getAgents(){
	   StringBuffer sb2 = new StringBuffer();
	    
	    String cmd = "SELECT [Code] AS kood,[Name] AS nimi,'' AS objekt FROM " + 
		"  [NAVISION].[dbo].[Rubineta, UAB$Salesperson_Purchaser] ";
        
	    if (isDebug()) System.out.println(cmd);
	    TreeMap agents = new TreeMap();
	    try {
	    	

	 	    	 FileReader fr = new FileReader(this.getExcelDir() + "vacation.txt");
	 	         BufferedReader reader = new BufferedReader(fr);
	 	       String line = null;
	 	      HashMap asendajad = new HashMap();
	 	      
	 	        while((line = reader.readLine()) != null ){
	 	           
	 	        	StringTokenizer st = new StringTokenizer(line, ";");
	 	//  System.out.println(line);
	 	          if (st.hasMoreTokens()) {
	 	            String ag1 =	st.nextToken();
	 	            String man = st.nextToken().trim();
	 	            Vector vv = new Vector();
	 	            if(asendajad.get(ag1) != null){
	 	           	 vv = (Vector)asendajad.get(ag1);
	 	           	 vv.add(man);
	 	           	 asendajad.put(ag1, vv);
	 	            }else{
	 	           	 vv.add(man);
	 	           	 asendajad.put(ag1, vv); 
	 	             }
	 	            }
	 	          }
	 		 
	 	    	
	    	
	    	
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
              //   System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
			    }

				}
			    if(asendajad.get(agent.getID()) != null){
                	Vector vv = (Vector)asendajad.get(agent.getID());
                	Enumeration aa = vv.elements();
                	while(aa.hasMoreElements()){
                		agent.addDubbler((String)aa.nextElement());
                	}
                }
			    sb2.append(agent.getID() + ";" + name + "\n");  
	          agents.put(name, agent);
	          agents1.put(agent.getID(), agent);
	        }

	        Agent aa = new Agent("HOUSE1");
	        aa.setName("office1");
	        agents.put("office1", aa);
            rs.close();
            stmt.close();
	        con.close();
	        
	        
	/*  cmd = "SELECT k.[No_] AS klient, [Balance total (LCY)]AS summa " +
	  " FROM [NAVISION].[dbo].[Rubineta, UAB$Customer] k  WHERE k.[Salesperson Code] != '' AND [Balance total (LCY)]<>0 " ;
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
*/
            
	       
	        con.close();
	    } catch (Exception e){
	        getParent().showError(e);
	      }
	    if(sb2.length() > 2){
	    	try{
	    		 Converter.saveFileInUTF8( "agent.txt", Converter.enCodeEst(sb2.toString()));
	    	}catch (Exception e){
	    		
	    	}
	      
	     }

     return agents;
   }

       public String getDistribution() {
         return null;
       }
  private String getVisitDay(String input){
	  String res = "";
	  
	  for (int i = 0 ; i < input.length(); i++ ){
		char a = input.charAt(i);
		if(a == 'A'){
			res = res + "E";
		}
		if(a == 'B'){
			res = res + "T";
		}
		if(a == 'C'){
			res = res + "K";
		}
		if(a == 'D'){
			res = res + "N";
		}
		if(a == 'E'){
			res = res + "R";
		}
		if(a == 'F'){
			res = res + "L";
		}
		if(a == 'G'){
			res = res + "P";
		}
		if(a == '1'){
			res = res + "2";
		}if(a == '2'){
			res = res + "1";
		}if(a == '3'){
			res = res + "4";
		}if(a == '4'){
			res = res + "8";
		}if(a == '5'){
			res = res + "16";
		}if(a == '6'){
			res = res + "32";
		}
	  }
	  
	  if(res.length() == 0){
		  res = " ";
	  }
	  return res ;
  }
  
  private HashSet sorts = new HashSet();
  
  	
  
  private HashMap fixGroups = new HashMap();
  
       public TreeMap getCustomers(Agent agent){

     	   Converter cc = new Converter();
	    	  HashMap kyssad =   cc.getQuestionaire(this.getExcelDir());
	    	

	    	  
	    try {

    	      String cmd = " select a.[Sell-to Customer No_] AS klient, a.[Customer No_] AS maksja,a.[External Document No_] AS number, a.[Due Date] AS kuupaev, sum(b.[Amount]) AS summa," +
    	                            " DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payer FROM [NAVISION].[dbo].[Rubineta, UAB$Cust_ Ledger Entry] a inner join " +
    	                            " [NAVISION].[dbo].[Rubineta, UAB$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] " +
    	                            " where " +
    								"  (a.[Document type] = 2 or a.[Document type]= 3)  AND a.[Posting Date] > getdate()-200 AND a.[Open]= 1 " +
    	                            " and  a.[Customer No_] not in ('101937200','114549101','815282300','3587403220','348585703','3477848375','3437652300','2444685200','2371531250', " +
									" '230335177','117449707') group by a.[Sell-to Customer No_],a.[Customer No_] ,a.[External Document No_], " +
    	                            " a.[Due Date] , DATEDIFF(dd,a.[Due Date],GETDATE()) ,a.[Customer No_] having sum(b.[Amount])<> 0 order by  a.[Due Date]";

    	  System.out.println(cmd);
    	       this.debtList = new Vector();
    	      Connection conv = getNewConnection2();
    	       Statement stmt1 = conv.createStatement();
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
    	       stmt1.close();
    	       conv.close();
    	      } catch (Exception e){
    	        e.printStackTrace();
    	      }
    	/// KLIENDI ANDMETE LEIDMINE

    	     TreeMap tm = new TreeMap();
    	     this.customers = new ArrayList();

 String sql = "SELECT k.[Name] AS nimi , k.City as addr2 ,k.[No_] AS kood,k.[Bill-to Customer No_] AS payer,k.[Address] + ' ' + k.[City] AS aadress  " +
    	  "	,k.[Salesperson Code] AS agent ,  k.blocked AS blokk,  " +
    		"  [Payment Terms Code] AS payment,'' AS alekood, [Credit Limit (LCY)] AS krediit,'' AS visit,'' AS type,  " +
    		"   0 AS extradays ,[Customer Price Group]  AS sort,'' AS debt , 0 AS minsum ,'' as kontakt , 0 as sendall,'' as grupp " +
    	" ,[Customer Price Group] as alegr ,k.[Customer Disc_ Group] as alegr2	,'' as fixed ,'' as agent2 FROM [NAVISION].[dbo].[Rubineta, UAB$Customer] k " + 
    	"	  WHERE k.[Salesperson Code] != ''  AND blocked != 3 " ;
    	   
    	   System.out.println(sql);
    	   try {
    	        Connection con = getNewConnection2();
    	        Statement stmt = con.createStatement();
    	        ResultSet rs = stmt.executeQuery(sql);
    	        while(rs.next()){
    	         String name = rs.getString("nimi").trim();
    	         String addr2 = rs.getString("addr2");
    	         if(addr2 != null){
    	        	
    	         }
    	         name = name.replaceAll("\"", " ");
    	           String id = rs.getString("kood").trim();
    	           //System.out.println(line);
    	           Customer customer = new Customer(id);
    	            String payer = null;
    	           if (name.equals("")) name = "<" + id + ">";
    	           customer.setName(Converter.enCodeEst(name));
    	           	payer = rs.getString("payer");
    	           	if(payer != null && payer.trim().length()>0){
    	             customer.setPayer(payer.trim());
    	             }

    	           
    	           	
    	             String aadress = rs.getString("aadress");
    	             if( aadress != null && aadress.trim().length()>0){
    	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),"Lithuania"));
    	               name = name + " " + Converter.enCodeEst(aadress);
    	              customer.setName(Converter.enCodeEst(name));
    	             }
    	         	String ag = rs.getString("agent");
    	             customer.addAgent(new Agent(ag));
    	             customer.setMainAgent(ag.trim());
    	             
    	             if (ag != null ){
	           				Agent agg = ((Agent)this.getAgents1().get(ag));
	         				
	           				if(agg != null && agg.getDubbler().size()>0){
	           					//System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
	           					for (int r = 0 ; r < agg.getDubbler().size() ; r++){
	           						Agent dub = new Agent((String)agg.getDubbler().elementAt(r));
	           						customer.addAgent(dub);
	           					}
	           				}
	           			}
    	             if (ag != null && ag.equals("MITU")){
    	 			          Enumeration enums = getParent().getAgentsHash().elements();
    	 			          while (enums.hasMoreElements()) {
    	 			            Agent a = (Agent) enums.nextElement();
    	 			            customer.addAgent(a);
    	 			          }

    	 			}

    	             if(rs.getString("blokk").equals("1")){
    	               customer.setSalesBlock(true);
    	             }

    	            String paym = rs.getString("payment");

    	           	String num = rs.getString("alekood");
    	           	if(num != null && num.trim().length()>0){
    	             customer.setDiscount(Double.parseDouble((num.trim())));
    	           	}
    	           	
    	           	int kred = rs.getInt("krediit");
    	           	if(kred>0){
    	             customer.setCredit(kred + "");
    	           	}
    	           	
    	
    	        	String visit = rs.getString("visit");
   	             if(visit != null && visit.trim().length()>0 && !visit.trim().toLowerCase().equals("x")){
   	                customer.setVisit(this.getVisitDay(visit.trim()));
   	             }

   	        
    	      
    	           if (!customer.isNodebt()){
    	         
    	  
    	                String sql3 =  "  SELECT     No_ AS kood, CASE WHEN k.[Bill-to Customer No_] = '' THEN " +
    	                         " (SELECT     SUM(de.[Amount (LCY)]) FROM [NAVISION].[dbo].[Rubineta, UAB$Cust_ Ledger Entry] c INNER JOIN  " +
    	                         "  [NAVISION].[dbo].[Rubineta, UAB$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] " +
    	                         "   WHERE   c.[Customer No_] = k.No_) WHEN k.[Bill-to Customer No_] <> '' THEN (SELECT   SUM(de.[Amount (LCY)]) " +
    	                         "   FROM  [NAVISION].[dbo].[Rubineta, UAB$Cust_ Ledger Entry] c INNER JOIN " +
    	                         "    [NAVISION].[dbo].[Rubineta, UAB$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] " +
    	                         "   WHERE  c.[Customer No_] = k.[Bill-to Customer No_] AND [OPEN] = 1 AND c.[Sell-to Customer No_] = k.No_ " +
    							 " 	 and [Due Date] <getdate()+7    and c.[Posting Date] > getdate()- 100) " +
    						     " 	 END AS yle FROM [NAVISION].[dbo].[Rubineta, UAB$Customer] k "+
    						      "	WHERE  (No_ = '" + id + "') AND (Blocked <> 3) AND ([Salesperson Code] <> '') " ;

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
    	           
    	           String all = rs.getString("sendall");
    	           if("1".equals(all)){
    	        	   Enumeration enums = getParent().getAgentsHash().elements();
    	               while (enums.hasMoreElements()) {
    	                 Agent a = (Agent) enums.nextElement();
    	                 customer.addAgent(a);
    	               }
    	               customer.setMainAgent(" ");
    	           }
    	           String cat = rs.getString("grupp");
    	           if(cat != null && cat.trim().length()>0){
    	             customer.setAnkeediTyyp(cat);
    	           }
    	           
    	           cc.addQuestiosToCustomer(customer,kyssad);    	          

    	           String alegr = rs.getString("alegr");
    	           if(alegr != null && alegr.trim().length()>0){
    	        	customer.setGroup(Converter.cleanLitLatEst(alegr.trim()));   
    	           }
    	           
    	           String alegr2 = rs.getString("alegr2");
    	           if(alegr2 != null && alegr2.trim().length()>0){
    	       
    	        	if(customer.getGroup().trim().length() >0){
    	        	//customer.setCustType(Converter.cleanLitLatEst(alegr.trim()));
    	        	 	customer.setGroup(Converter.cleanLitLatEst(alegr2.trim())); 	
    	        	}else{
    	        	 	customer.setGroup(Converter.cleanLitLatEst(alegr2.trim())); 
    	        	}
    	           }
    	           
    	           
    	        	
     	          String gruppx = rs.getString("fixed");
     	         if(gruppx != null && gruppx.trim().length() != 0 ){
      	        	   Vector v = new Vector();
      	        	   v.add(gruppx.trim());
      	           	customer.setFixSortCode(v);
      	           }
     	        String agent2  = rs.getString("agent2");
 	           if(agent2 != null && agent2.trim().length() > 2 ){
 	             customer.addAgent(new Agent(agent2.substring(0, 2)));
 	           }
    	           
    	            tm.put(customer.getName(), customer);
    	            this.customers.add(customer.getID());
    	           
    	         }

    	        rs.close();
    	        stmt.close();
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
   	  String cmd = " SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [NAVISION].[dbo].[Rubineta, UAB$Item Ledger Entry] l " +
          " INNER JOIN [NAVISION].[dbo].[Rubineta, UAB$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('SANDELIS') and t.[Blocked]=0 " + 
          " GROUP BY l.[Location Code],l.[Item No_] having sum(l.[Quantity]) != 0  ORDER BY l.[Item No_] ";
  

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
  	    stmt.close();
  	      con.close();

  	    System.out.println(cmd);
   } catch (Exception e) {
       e.printStackTrace();
   }
   //  getFixedAssortmentData();
 
  
     try {
    	 String cmd =  "     SELECT t.[No_] AS toode,t.[Description]  + ' '+ t.[Description 2]AS nimi, sp.[Unit Price] AS myygihind,t.[Product Group Code] AS grupp, '' AS gruppx,t.[Item Category Code] AS grupp2 " + 
		 ",t.[Base Unit of Measure] AS unit,Barkodas AS ean,'' AS status ,0 AS maxdiscount,  " +
		 "'' AS type, [Qty_ per Unit of Measure] AS kastis,  '' AS kaal, [VAT Prod_ Posting Group] AS vat  ,  0 as jaehind  " +
		 ",1 as goes " +
		 " FROM [NAVISION].[dbo].[Rubineta, UAB$Item] t LEFT JOIN [NAVISION].[dbo].[Rubineta, UAB$Item Unit of Measure] vat ON (vat.[Item No_]=[No_] and vat.[Code]='D��' )  " +
		 " left join  [NAVISION].[dbo].[Rubineta, UAB$Sales Price] sp on sp.[Item No_]=t.[No_] and sp.[Sales Code] ='LIETUVA' and sp.[Starting Date] < getdate()+5  and " +
		 "  (sp.[Ending Date] > getdate()+1 or year (sp.[Ending Date]) = 1753) " +   
		 " WHERE t.[Blocked]=0  and  len(t.[Search Description]) != 0 and sp.[Unit Price] != 0   ORDER BY t.[No_]  " ;

     	
      System.out.println(cmd);
      
      this.getProdGroups();
     	Connection con = getNewConnection2();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode").trim();
            Product prod = new Product(id);
         	String name = rs.getString("nimi").trim();
         	name =   Converter.firstToUpperCase(name) + " " +id;
             double price = rs.getDouble("myygihind");
                prod.setPrice(price);
              if(ladutr.get(id) != null ){
            	  prod.setStock(new Double((String)ladutr.get(id)).doubleValue());
              }
                String grupp = Converter.enCodeEst(rs.getString("grupp"));
                if(grupp == null || grupp.trim().length() == 0 ){
                	grupp = Converter.enCodeEst(rs.getString("gruppx"));
                }
                //asendame brandiga kui tootel pole gruppi 
              if(grupp != null && grupp.trim().length()>0 ){
                	
            	 // System.out.println("twoone "  + grupp + " "+ twoone.get(grupp.trim()) + " " + koodJaGrupp.get(grupp.trim()) );
                	if(twoone.get(grupp.trim()) != null){
                		String ad = "";
                		if(  koodJaGrupp.get(grupp.trim()) != null){
                    		ad = (String)koodJaGrupp.get(grupp.trim());
                    		 prod.setGroup(ad);
                    		 prod.setGroupSecond(ad + grupp.trim());
                    	}else{
                    		 prod.setGroup((String) twoone.get(grupp.trim()));
                    		 prod.setGroupSecond( grupp.trim());
                    	}
                	}else{
                		 prod.setGroup(grupp.trim());
                	}
                 	 //prod.setGroup(grupp.trim());
                	
                //	System.out.println("twoone "  + grupp + " "+ twoone.get(grupp.trim()) + " " + koodJaGrupp.get(grupp.trim()) );
                 }
          

        		
    			
                prod.setUnit(rs.getString("unit"));
              String ean = (rs.getString("ean"));
                 if (ean != null &&ean.trim().length()>3){
                   prod.setEAN(ean.trim());
                //   name = name + " " + ean.substring(ean.trim().length()-3);
                 }
                    String xxx = rs.getString("status");
     try{
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
     }catch(Exception e){
    	 
     }
              

             	String discou = rs.getString("maxdiscount");
             	if(discou != null && discou.trim().length()>0){
                prod.setMaxDiscount(Double.parseDouble(discou));
                }

                double	cases = rs.getDouble("kastis");
                	if(cases>0){
                      prod.setInCases(cases);
                	}



                 	 String vat = rs.getString("vat");
                 	 double vati = 0;
                          if(vat != null && vat.trim().length()>0){
                            if(vat.equals("PVM21")){
                            	prod.setVat("21");
                            	vati = 21;
                            }else if (vat.equals("GRUPE2")){
                            	prod.setVat("5");
                            	vati = 5;
                            } else if (vat.equals("GRUPE20")){
                             	prod.setVat("20");
                             	vati = 20;
                             }
                          }

                          double	shelf = rs.getDouble("jaehind");
                          	if(shelf>0){
                          	  prod.setPriceSecond(shelf * ((100 + vati)/100));
                          	}
         
                  
         
                          	
                   String add = "";
                   if(id.length() > 5){
                	   add = id.substring(0,5);
                   }
                   
                   boolean goes = true;
                   double	goe = rs.getDouble("goes");
                 	if(goe==0 && !sorts.contains(id) && prod.getStock() < 1){
                 	 goes = false;
                 	}
                 	
                 if(goes ){
                	 String ee = "";
                	 if (ean != null &&ean.trim().length()>3){
                         ee = " " + ean.substring(ean.trim().length()-3);
                       }
                	 prod.setName(Converter.enCodeEst(name) + ee);
                     tm.put(add + name, prod); 
                 }
          
        }
  
        rs.close();
        stmt.close();
        con.close();
     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }

 
   
   private String getExcelSortDir(){
	      String dir = getParent().getProps().getProperty("excel.dirsort");
	      if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	      return dir;
	    }
   
   
   private String getExcelPlanDir(){
	      String dir = getParent().getProps().getProperty("excel.dirplan");
	      if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	      return dir;
	    }
   
   private String getPlanoDir(){
	      String dir = getParent().getProps().getProperty("plano.dir");
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
   /*  String cmd = " SELECT [No_] AS toode ,[Type] AS type FROM  [NAVISION].[dbo].[Rubineta, UAB$Item] " +
     		" WHERE len([Type])>0  ORDER BY  [Search Description]";
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
	        stmt.close();
	        con.close();
       } catch (Exception e){
      e.printStackTrace();
    }*/
    return sb.toString();
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     HashMap kliendid = new HashMap();
     try {
    String cmd =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate,0 as lastamount" +
    		" FROM [NAVISION].[dbo].[Rubineta, UAB$Sales Shipment Line] r " +
    "  INNER JOIN [NAVISION].[dbo].[Rubineta, UAB$Sales Shipment Header] d ON r.[Document No_]= d.[No_] INNER JOIN [NAVISION].[dbo].[Rubineta, UAB$Item] t ON t.[No_]=r.[No_]  WHERE " +
    "  d.[Posting Date]> GETDATE()-300 ORDER BY  d.[Sell-to Customer No_], t.[Search Description],r.[No_],d.[Posting Date]";
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
         stmt.close();
         con.close();


           int counter = 0;
           int cou = 0;
          String oldklient = null;
          String olddate = " ";
          String oldtoode = null;

          String cmd2 =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate FROM [NAVISION].[dbo].[Rubineta, UAB$Sales Invoice Line] r " +
		  " INNER JOIN [NAVISION].[dbo].[Rubineta, UAB$Sales Invoice Header] d ON r.[Document No_]= d.[No_] WHERE d.[Posting Date]> GETDATE()-30 " +
		  " Order by d.[Sell-to Customer No_],r.[No_],d.[Posting Date] desc ";
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
     return "NaviVilandra";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }


   public double getSalary(String agentCode){
     return 0;
   }


   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private Vector debtList = new Vector();
   private String url = "jdbc:odbc:report";
   private String url2 = "jdbc:odbc:navi";
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

   

   int mm = Calendar.getInstance().get(Calendar.MONTH)+ 1;
   int yy = Calendar.getInstance().get(Calendar.YEAR);
   int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
   int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
   double planprec = dd/d2;
   
  	 try {
  		String cmd = "   select x.agent ,sum(summa) as summ from( " +
  		" SELECT cust.[Salesperson Code] AS agent, Sum(sl.[quantity (base)] * CASE " +
  		" WHEN slh.[currency code] = '' THEN sl.[unit price] ELSE sl.[unit price] / slh.[currency factor] END) AS summa " +
  		" FROM [NAVISION].[dbo].[Rubineta, UAB$sales invoice line] sl LEFT JOIN [NAVISION].[dbo].[Rubineta, UAB$sales invoice header]slh " +
  		" ON sl.[document no_] = slh.[no_] left join [NAVISION].[dbo].[Rubineta, UAB$Customer] cust" +
  		" on cust.[Salesperson Code] = slh.[Salesperson Code] WHERE sl.type != 0 " +
  		" AND Year (sl.[posting date]) = Year(Getdate()) AND Month (sl.[posting date]) = month(Getdate()) " +
  		" GROUP BY cust.[salesperson code] UNION " +
  		" SELECT cust.[Salesperson Code] AS agent, Sum(-sl.[quantity (base)] * CASE " +
  		" WHEN slh.[currency code] = '' THEN sl.[unit price] ELSE " +
  		" sl.[unit price] / slh.[currency factor] END) AS summa FROM [NAVISION].[dbo].[Rubineta, UAB$sales cr_memo line] sl " +
  		" LEFT JOIN [NAVISION].[dbo].[Rubineta, UAB$sales cr_memo header]slh ON sl.[document no_] = slh.[no_] " +
  		" left join [NAVISION].[dbo].[Rubineta, UAB$Customer] cust on cust.[Salesperson Code] = slh.[Salesperson Code]" +
  		" WHERE sl.type != 0 AND Year (sl.[posting date]) = Year(Getdate()) AND month (sl.[posting date]) = Month(Getdate()) " +
  		" GROUP BY cust.[salesperson code] " +
  		"   ) as x  group by x.agent order by 1";
  	 	
  	  Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
            
                 String agentID = rs.getString("agent");
                 double sales = rs.getDouble("summ");
                 Agent agent = (Agent)agents.get(agentID);
                 if (agent != null){
                	 double plan = sales;
                	 try{
                		// plan = Double.parseDouble((String)plans.get(agent));
                	 }catch(Exception e){
                		 
                	 }
                	 double  tehtudsalperc = sales/plan;
                	 
                	  double plaanitavMyykSal = d2 * sales/dd;
                	  
                	  double  prognosalperc = plaanitavMyykSal/plan;
                	 
                     agent.setMonthSales((int)(sales));
                   
                   String asi = "Pardavimai: " + NF.format(sales) + " Lt||";
                   if(dd > 6){
                 	  asi = asi + "Plano vykdymas/prognoz� (Lt):";
                       asi = asi + "     " +NF.format(tehtudsalperc*100) + "% / " + NF.format(prognosalperc*100)  + " %||";  
                   }
                   agent.setStart( Converter.enCodeEst(asi ));
                   
            }
          }

	        rs.close();
	        con.close();
      } catch (Exception e){
        e.printStackTrace();
     }

  }
}
