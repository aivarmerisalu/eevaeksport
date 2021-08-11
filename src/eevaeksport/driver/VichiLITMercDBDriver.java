package eevaeksport.driver;

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
import java.util.Properties;
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
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author unascribed
 * @version 1.0
 */

public class VichiLITMercDBDriver extends AbstractDBDriver implements
		BulkUpdater, Discounter, Manager {

	public VichiLITMercDBDriver(EksportPanel parent) {
		super(parent);
		this.subjectFilesTo = parent.getString("filemsg");
		this.lang = parent.getProps().getProperty("language");
		String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

		try {
			Class.forName(databaseDriverName).newInstance();

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

	public String getPilotDayData(Agent agent) {
		return null;
	}

	public String getSaveDebtInfo() {
		return null;
	}

	   public Vector getPriceMatrixData(){
		   
		  
		   String sql = "select  '' as custid,a.[Pirkėjų nuolaidos grupė] as clgroup,a.[Item No_] AS prodid,a.[Starting Date] as startday, " +  
           " a.[End Date] as enddate,a.[Unit Price] as price  , a.[Discount, Pct] as ale," +
           " case when year([End Date] - [Starting Date]) = 1900  and month([End Date] - [Starting Date])<3 then 1 else 0 end as kuu " +
           " , (case when [Starting Date] - getdate() > 3 then 1 else 0 end ) as tulevik  FROM Navision.dbo.[Vičiūnai ir Ko$Akcijų kainos] a WHERE  " +
           " [Starting Date] > getdate()-30  and ([End Date] > getdate()+1 or " +   
           "    year ([End Date]) = 1753)  and a.[Pirkėjų nuolaidos grupė] != '' and a.[Sales Price Type] = 'AKC' order by 2,3,4 desc";

		   
		   // sql = "select  * from v_VIK_AkcijuKainos";


		       Vector result = new Vector();
		       HashSet hh = new HashSet();
		         try {
		          
		      	   Connection con = getNewConnection2();
		   	        Statement stmt = con.createStatement();
		   	        ResultSet rs = stmt.executeQuery(sql);
		           while (rs.next()) {
		        	   String clGroup = rs.getString("clgroup");  
		        	   String prodID = rs.getString("prodid").trim();
		          
		           
			         String sdate = rs.getString("startday");
		             String edate = rs.getString("enddate");
		             double price = rs.getDouble("price");
		             double disc = rs.getDouble("ale");
		             int kuu = rs.getInt("kuu");
		             int tulevik = rs.getInt("tulevik");
		             MatrixLine mline = new MatrixLine();

		             if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
		               mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
		             }

		           
		           
		             if (prodID != null && prodID.length() > 0) {
		               mline.setProdID(prodID);
		             }
		         
	                	  if(sdate != null  && !sdate.startsWith("1753")){
	     	            	 mline.setStartDate(sdate.substring(0,10));
	     	             }
	                	  if(edate != null && !edate.startsWith("1753")){
	     	            	 mline.setEndDate(edate.substring(0,10));
	     	             }
	                 if(tulevik == 1){
	                	 mline.setProdLevel("pr");
	                 }

		             if (price != 0) {
		               mline.setPrice(1);
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

	   
	private Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(url, getParent().getProps()
				.getProperty("dsn.user"),
				getParent().getProps().getProperty("dsn.pass"));
	}

	private Connection getNewConnection2() throws SQLException {
		return DriverManager.getConnection(url2, getParent().getProps()
				.getProperty("dsna.user"),
				getParent().getProps().getProperty("dsna.pass"));
	}

	public void insertDefective(DefectiveWrapper wr) {
		String sql = "INSERT INTO dbo.MJ_QUALITYLIT VALUES (?,?,?,?,?,?,?,?,?,?,getdate(),?,' ',' ',0,0)";
		try {
			Connection con = getNewConnection();
			PreparedStatement ps = con.prepareStatement(sql);

			ps.setString(1, wr.getDefective().getAgentID());
			ps.setString(2, wr.getDefective().getAgentName());

			for (Iterator iter = wr.getDefective().getLines().iterator(); iter
					.hasNext();) {
				DefectiveLineWrapper lw = new DefectiveLineWrapper(iter.next());
				ps.setString(3, lw.getLine().getCustID());
				ps.setString(4, lw.getLine().getCustName());
				ps.setString(5, lw.getLine().getProdID());
				ps.setString(6, lw.getLine().getProdName());

				double kogus = 0;
				try {
					kogus = Double.parseDouble(lw.getLine().getAmount());
				} catch (NumberFormatException nfe) {
				}
				;
				ps.setDouble(7, kogus);
				String bb = lw.getLine().getBestBefore();
				// System.out.println(kogus+"bb '"+lw.getLine().getBestBefore()+"'"+bb+"'");
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
				+ "?," // resint3 I,
				+ "?)"; // country L

		try {
			System.out.println("jam");
			Connection con = getNewConnection();
			PreparedStatement ps = con.prepareStatement(sql);

			for (Iterator iter = wr.getInfo().getLines().iterator(); iter
					.hasNext();) {
				eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(
						iter.next());
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
				if (cc == null) {
					cc = " ";
				}
				ps.setString(24, cc);

				String cd = lw.getLine().getComment();
				if (cd == null) {
					cd = " ";
				}
				ps.setString(25, cd);

				String ce = lw.getLine().getCustAgentId();
				if (ce == null) {
					ce = " ";
				}

				ps.setString(26, ce);

				String cf = lw.getLine().getCustAgentName();
				if (cf == null) {
					cf = " ";
				}
				ps.setString(27, cf);
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
				ps.setString(36, "lt");
				ps.executeUpdate();
			}

			ps.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public String getDateStr() {
		Calendar cal = Calendar.getInstance();
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

	
	public String getProductSortingData() {
		return null;
	}

	private String ensureLen(String str, String replace) {
		if (str == null)
			str = "";
		else
			str = str.trim().replace('\'', ' ').replace('\"', ' ')
					.replace(';', ' ');
		if (str.length() == 0) {
			return replace;
		} else {
			return str;
		}
	}

	public ManagerData getManagerData() throws SQLException {

		String tableName = "dayreport";
		if (getParent().getProps().getProperty("report.tablename") != null
				&& !getParent().getProps().getProperty("report.tablename")
						.trim().equals("")) {
			tableName = getParent().getProps().getProperty("report.tablename");
		}

		String sql = " SELECT clid AS klient,sentdate AS kuupaev,docsum AS summa,linecount AS ridu,text AS tekst,'' AS dealer,clgroup AS agentname"
				+ " FROM  "
				+ tableName
				+ " WHERE sentdate>(GETDATE()-30) ORDER BY clid ";

		if (isDebug())
			System.out.println(sql);
		this.con = getNewConnection();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);

		StringBuffer sb = new StringBuffer();
		String sep = ";";

		while (rs.next()) {
			String cliID = rs.getString("klient").trim();
			sb.append(cliID + sep);
			sb.append(rs.getString("kuupaev").trim() + sep);
			sb.append(getNF().format(rs.getDouble("summa")) + sep);
			sb.append(rs.getInt("ridu") + sep);
			String tekst = ensureLen(rs.getString("tekst"), " ");
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
		while (enums.hasMoreElements()) {
			Customer cust = (Customer) enums.nextElement();
			Iterator agen = cust.getAgents().keySet().iterator();
			while (agen.hasNext()) {
				Agent ag = (Agent) agents.get((String) agen.next());
				bf2.append(cust.getID() + sep);
				bf2.append(ag.getID() + sep);
				bf2.append(ag.getName() + "\n");
			}
		}

		md.setClientAgentInfo(bf2.toString());
		return md;

	}

	public void insertDistribution(DistEvalListWrapper wr) {
		String sql = "INSERT into dbo.distributionlit values (" + "?," // agent
																		// C(50),
				+ "?," // agentname C(200),
				+ "?," // clientid C(50),
				+ "?," // clientname C(200),
				+ "?," // prodid C(50),
				+ "?," // prodname C(200),
				+ "getdate() ," // sentdate D,
				+ "?," // evalcode C(50),
				+ "?," // bestbefore C(50),
				+ "?," // ownproduct L,
				+ "?," // evalname C(200),
				+ "?," // variation I,
				+ "?," // package1 I,
				+ "?," // presence L,
				+ "?," // StockDisp N,
				+ "?," // StockWare N,
				+ "?," // SpacePros I,
				+ "?," // FaceAmBef I,
				+ "?," // FaceAmAft I,
				+ "?," // PlanoEval I,
				+ "?," // PalAmount I,
				+ "?," // action L,
				+ "?," // dominance I,
				+ "?," // FirstInFl I,
				+ "?," // PlacPres I,
				+ "?," // NormalPr N,
				+ "?," // ActionPr N,
				+ "?," // comment C(200), reserv1 C(200), reserv2 C(200),
				+ "?," + "?," + "?," // resint1 I,resint2 I,
				+ "?," + "?," // resdou1 N, resdou2 N,
				+ "?," + "?," // a2 I,
				+ "?," // a3 I,
				+ "?," // a4 I,
				+ "?," // vobbler I,
				+ "?," // pricetag I,
				+ "?," // numberof11 I,
				+ "?," // numberof12 I,
				+ "?," // numberof21 I,
				+ "?," // numberof22 I,
				+ "?," // planocomm C(200),
				+ "?," // colldtime C(12),
				+ "?)"; // isbrand L

		try {
			Connection con = getNewConnection();
			PreparedStatement ps = con.prepareStatement(sql);

			for (Iterator iter = wr.getDistEval().getLines().iterator(); iter
					.hasNext();) {
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
				ps.setDouble(14, lw.getLine().getStockAtDisplay());// + "?," //
																	// StockDisp
																	// N,
				ps.setDouble(15, lw.getLine().getStockAtWarehouse());// + "?,"
																		// //
																		// StockWare
																		// N,
				ps.setInt(16, lw.getLine().getSpaceProsentage());// + "?," //
																	// SpacePros
																	// I,
				ps.setInt(17, lw.getLine().getFaceAmountBefore());// + "?," //
																	// FaceAmBef
																	// I,
				ps.setInt(18, lw.getLine().getFaceAmountAfter());// + "?," //
																	// FaceAmAft
																	// I,
				ps.setInt(19, lw.getLine().getPalnoeval());// + "?," //
															// PlanoEval I,
				ps.setInt(20, lw.getLine().getPalAmount());// + "?," //
															// PalAmount I,
				ps.setBoolean(21, lw.getLine().isAction());// + "?," // action
															// L,
				ps.setInt(22, lw.getLine().getDominance());// + "?," //
															// dominance I,
				ps.setInt(23, lw.getLine().getFirstinflow());// + "?," //
																// FirstInFl I,
				ps.setInt(24, lw.getLine().getPlacespresent());// + "?," //
																// PlacPres I,
				ps.setDouble(25, lw.getLine().getNormalPrice()); // + "?," //
																	// NormalPr
																	// N,
				ps.setDouble(26, lw.getLine().getPromoPrice());// + "?," //
																// ActionPr N,
				ps.setString(27, lw.getLine().getComment());// + "?," // comment
															// C(200), reserv1
															// C(200), reserv2
															// C(200),
				ps.setString(28, lw.getLine().getReserv1());
				ps.setString(29, lw.getLine().getReserv2());
				ps.setInt(30, lw.getLine().getResint1());// + "?," // resint1
															// I,resint2 I,
				ps.setInt(31, lw.getLine().getResint2());
				ps.setDouble(32, lw.getLine().getResdouble1());// + "?," //
																// resdou1 N,
																// resdou2 N,
				ps.setDouble(33, lw.getLine().getResdouble2());
				ps.setInt(34, lw.getLine().getA2());// + "?," // a2 I,
				ps.setInt(35, lw.getLine().getA3());// + "?," // a3 I,
				ps.setInt(36, lw.getLine().getA4());// + "?," // a4 I,
				ps.setInt(37, lw.getLine().getVobbler());// + "?," // vobbler I,
				ps.setInt(38, lw.getLine().getPricetag());// + "?," // pricetag
															// I,
				ps.setInt(39, lw.getLine().getNumberOF11());// + "?," //
															// numberof11 I,
				ps.setInt(40, lw.getLine().getNumberOF12());// + "?," //
															// numberof12 I,
				ps.setInt(41, lw.getLine().getNumberOF21());// + "?," //
															// numberof21 I,
				ps.setInt(42, lw.getLine().getNumberOF22());// + "?," //
															// numberof22 I,
				ps.setString(43, lw.getLine().getPlanoComment());// + "?," //
																	// planocomm
																	// C(200),
				// String time = setZero(lw.getLine().getDate().getHours()) +
				// "."+setZero(lw.getLine().getDate().getMinutes())+"."+setZero(lw.getLine().getDate().getSeconds());
				ps.setString(44, "00.00");// + "?," // colldtime C(12),
				ps.setBoolean(45, lw.getLine().isBrand());// + "?)" ; // isbrand
															// L

				ps.executeUpdate();
			}

			// con.commit();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * private String setZero(int i){ String x = i +""; if (x.length() == 1){ x
	 * = "0" + x; } return x; }
	 */
	public Vector getFixedAssortmentData() {
		Vector lines = new Vector();

		
		  boolean insertassort = true;
          
          try {
        	  String sql = "SELECT sortdate from mj_assortment where  year(sortdate) = year(getdate()) and" +
  	   		" month(sortdate) = month(getdate()) and day(sortdate) = day(getdate()) and country ='lt' ";
        	 
        	    Connection cons = this.getNewConnection();
    	        Statement stmts = cons.createStatement();
    	        ResultSet rss = stmts.executeQuery(sql);
    	        if(rss.next()){
    	        	insertassort = false;
    	        }   
    	        
    	        rss.close();
    	        stmts.close();
    	        cons.close();
    	        
          }catch(Exception d){
       	//   d.printStackTrace();
          }
    	
		
		try {
			String cmd = " select s.[Item No_] as toode,s.[Assortment Code] + s.[Assortment Level] as sortcode from" +
					" [Vičiūnai ir Ko$Item Assortment Level] s INNER JOIN [Vičiūnai ir Ko$Item] t on t.No_=[Item No_] " +
					" ORDER BY [Net Weight] , [Search Description]";
			cmd = "select * from v_VIK_Asortimentas";
			
			System.out.println(cmd);
			Connection con = getNewConnection2();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
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
				if (clGroup != null && clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
				}

				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}
				if (mline.getProdID() == null && prodGroup != null
						&& prodGroup.trim().length() > 0) {
					mline.setProdGroup(prodGroup);
				}
			    if(insertassort){
                    insertSortLine(mline);   
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

	   private void  insertSortLine(MatrixLine mline){
			  try{
					
				      String sql = "INSERT INTO mj_assortment values (" +
				         "'" + mline.getCustID() + "'," + 
				         "getdate()," + 
				         "'" + mline.getCliGroup() + "'," + 
				         "'" + mline.getProdID() + "'," + 
				         "0," + 
				         "0,'','lt')" ;
				  
				  	     Connection  con2 = this.getNewConnection();      
					     Statement stmt2 = con2.createStatement();
					     stmt2.executeUpdate(sql);
					     con2.close();
			  }catch(Exception e){
				  e.printStackTrace();
			  }		  
		  }
	
	public DealerProduct getDealerProductsList() {
		return null;

		/*
		 * DealerProduct dealerProds = new DealerProduct();
		 * 
		 * StringBuffer bufKau = new StringBuffer();
		 * 
		 * TreeMap KLA = new TreeMap(); TreeMap KAU = new TreeMap(); TreeMap
		 * KAU_CEN = new TreeMap(); TreeMap PAN = new TreeMap(); TreeMap VIL =
		 * new TreeMap(); TreeMap SIA = new TreeMap();
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * try {
		 * 
		 * 
		 * 
		 * String cmd0 =
		 * "select sum(Quantity) as kogus , r.[Location Code] as asukoht,r.No_ as toode  "
		 * + " FROM [Vičiūnai ir Ko$Sales Line] r " +
		 * "    INNER JOIN [Vičiūnai ir Ko$Sales Header] d ON r.[Document No_]= d.[No_] where "
		 * +
		 * "  status=1 and r.[Location Code] in ('KLA','KAU','PAN','VIL','ŠIA','KAU_CEN') group by r.[Location Code],r.[No_] "
		 * + "order by r.[Location Code],r.[No_]" ;
		 * 
		 * con = getNewConnection2(); stmt = con.createStatement(); ResultSet rs
		 * = stmt.executeQuery(cmd0); while(rs.next()){ String toode = null;
		 * double seis = 0;; String ladu = null; seis = rs.getDouble("kogus");
		 * ladu = rs.getString("asukoht").trim(); toode
		 * =rs.getString("toode").trim();
		 * 
		 * if(ladu != null && ladu.equals("PAN") && seis > 0){
		 * 
		 * PAN.put(toode,seis+"");
		 * 
		 * 
		 * } if(ladu != null && ladu.equals("ŠIA") && seis > 0){
		 * 
		 * SIA.put(toode,seis+"");
		 * 
		 * }
		 * 
		 * if(ladu != null && ladu.equals("KAU") && seis > 0){
		 * 
		 * KAU.put(toode,seis+"");
		 * 
		 * } if(ladu != null && ladu.equals("KLA") && seis > 0){
		 * 
		 * KLA.put(toode,seis+""); }
		 * 
		 * if(ladu != null && ladu.equals("VIL") && seis > 0){
		 * 
		 * VIL.put(toode,seis+"");
		 * 
		 * } if(ladu != null && ladu.equals("KAU_CEN") && seis > 0){
		 * 
		 * KAU_CEN.put(toode,seis+"");
		 * 
		 * } }
		 * 
		 * rs.close(); con.close();
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * String cmd =
		 * "SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [Vičiūnai ir Ko$Item Ledger Entry] l "
		 * +
		 * " INNER JOIN [Vičiūnai ir Ko$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('KLA','KAU','PAN','VIL','ŠIA','KAU_CEN') "
		 * +
		 * " AND t.[Blocked]=0 AND t.[Unit Price]>0 AND len(t.[Grupės kodas])>0 "
		 * + " GROUP BY l.[Location Code],l.[Item No_] ORDER BY l.[Item No_] ";
		 * 
		 * Connection con = getNewConnection2(); Statement stmt =
		 * con.createStatement(); rs = stmt.executeQuery(cmd); while(rs.next()){
		 * String toode = null; double seis = 0;; String ladu = null; toode
		 * =rs.getString("kood").trim(); seis = rs.getDouble("kogus"); ladu =
		 * rs.getString("ladu").trim(); try{ if(ladu.equals("KAU") &&
		 * KAU.get(toode)!= null){ seis = seis -
		 * Double.parseDouble((String)KAU.get(toode)); }
		 * if(ladu.equals("KAU_CEN") && KAU_CEN.get(toode)!= null){ seis = seis
		 * - Double.parseDouble((String)KAU_CEN.get(toode)); }
		 * if(ladu.equals("VIL") && VIL.get(toode)!= null){ seis = seis -
		 * Double.parseDouble((String)VIL.get(toode)); } if(ladu.equals("PAN")
		 * && PAN.get(toode)!= null){ seis = seis -
		 * Double.parseDouble((String)PAN.get(toode)); } if(ladu.equals("ŠIA")
		 * && SIA.get(toode)!= null){ seis = seis -
		 * Double.parseDouble((String)SIA.get(toode)); } if(ladu.equals("KLA")
		 * && KLA.get(toode)!= null){ seis = seis -
		 * Double.parseDouble((String)KLA.get(toode)); } }catch (Exception e){
		 * e.printStackTrace(); }
		 * 
		 * 
		 * if(seis > 0 ){ dealerProds.add(ladu, toode,seis+""," "," " ); }else{
		 * dealerProds.add(ladu, toode,"0"," "," " ); }
		 * 
		 * if(ladu.equals("KAU")){ bufKau.append(toode + ";" + seis + "\n" ); }
		 * 
		 * }
		 * 
		 * rs.close(); con.close();
		 * 
		 * /* if(bufKau.length()>0){ String custfile = "kaustock.txt" ; File f =
		 * new File(getOutDir() + custfile); if (f.exists())f.delete(); try {
		 * PrintWriter os = new PrintWriter(new FileWriter( getOutDir() +
		 * custfile, true)); os.write(bufKau.toString()); os.close(); }catch
		 * (IOException ex) { ex.printStackTrace(); } }
		 * 
		 * System.out.println(cmd); } catch (Exception e) { e.printStackTrace();
		 * } return dealerProds;
		 */
	}

	public String getSaveStockInfo() {
		StringBuffer products = new StringBuffer();


		try {
			String cmd = "SELECT l.[Item No_] AS kood, CAST( sum(l.[Remaining Quantity]) "+
   "  -(select (case when count(*)>0 Then sum([Outstanding Quantity]) else 0 end ) from Navision.dbo.[Vičiūnai ir Ko$Sales Line] u "+
  "  where u.No_= l.[Item No_] and u.[Location Code] in ('BFE')) "+
   " AS DECIMAL(12,2)) AS kogus FROM Navision.dbo.[Vičiūnai ir Ko$Item Ledger Entry] l  "+
    "    INNER JOIN Navision.dbo.[Vičiūnai ir Ko$Item] t ON [No_]=l.[Item No_]  "+
    "    WHERE  l.[Location Code] in ('BFE') AND l.[Open] = 1 " +
   "        AND t.[Blocked]=0 AND t.[Unit Price]>0 AND len(t.[Grupės kodas])>0 " +
        " GROUP BY l.[Item No_] ORDER BY l.[Item No_]";

			Connection con = getNewConnection2();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String toode = null;
				double seis = 0;
				
				toode = rs.getString("kood").trim();
				seis = rs.getDouble("kogus");
				products.append(toode + "; ;");
				products.append((seis + "").substring(0, (seis + "").indexOf("."))
						+ "; ; ; \n");
			
			}
			

			rs.close();
			con.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

      System.out.println(products.toString());

		return products.toString();
	}

	public Vector getDebt(String agentCode) {
		return this.debtList;
	}

	public void connectionDown() {
	}

	public void connectionUp() {
	}

	public String getDealerCustomerCSV(String agent) {
		return null;
	}

	public String getCustProdDiscounts(Agent agent) {
		return null;
	}

	public TreeMap getProdGroups() throws SQLException {
		TreeMap tm = new TreeMap();
		setTreeGroup(true);
		String cmd = " SELECT  [Kodas] AS kood,[Aprašas] AS nimi  FROM [Vičiūnai ir Ko$Prekių grupavimas] order by [Kodas]";
		try {
			Connection con = getNewConnection2();
			System.out.println(cmd);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String id = rs.getString("kood");
				if (cleardots(id).equals("28")) {
					id = "5.1.";//5.01.
				}
				if (id.equals("1.10.")) {
					id = "1.a.";//5.01.
				}
				if (id.equals("1.11.")) {
					id = "1.b.";//5.01.
				}
				if (id.equals("1.12.")) {
					id = "1.c.";//5.01.
				}
				if (id.equals("1.13.")) {
					id = "1.d.";//5.01.
				}
				String name = rs.getString("nimi");
				if ((id.trim().length() == 4 || id.trim().length() == 5)
						&& id.trim().length() > 0) {
					tm.put(cleardots(id), Converter.enCodeEst(id.substring(0, 1) + " "
							+ cleardots(id) + " " + name));
				}
			}

			rs.close();
			con.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return tm;
	}

	public TreeMap getProdGroupsSecond() throws SQLException {
		TreeMap tm = new TreeMap();
		String cmd = "SELECT  [Kodas] AS kood,[Aprašas] AS nimi  FROM [Vičiūnai ir Ko$Prekių grupavimas] order by [Kodas]";
		System.out.println(cmd);
		try {
			Connection con = getNewConnection2();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String groupID = rs.getString("kood");
/*				if (cleardots(groupID).equals("20801")) {
					groupID = "5.01.01.";
				}
				if (cleardots(groupID).equals("20802")) {
					groupID = "5.01.02.";
				}
				if (cleardots(groupID).equals("20803")) {
					groupID = "5.01.03.";
				}
				if (cleardots(groupID).equals("20804")) {
					groupID = "5.01.04.";
				}
				if (cleardots(groupID).equals("20805")) {
					groupID = "5.01.05.";
				}
				if (cleardots(groupID).equals("20806")) {
					groupID = "5.01.06.";
				}
				if (cleardots(groupID).equals("20807")) {
					groupID = "5.01.07.";
				}
				if (cleardots(groupID).equals("20808")) {
					groupID = "5.01.08.";
				}
				if (cleardots(groupID).equals("20809")) {
					groupID = "5.01.09.";
				}*/
				if (cleardots(groupID).equals("281")) {
					groupID = "5.1.1.";
				}
				if (cleardots(groupID).equals("282")) {
					groupID = "5.1.2.";
				}
				if (cleardots(groupID).equals("283")) {
					groupID = "5.1.3.";
				}
				if (cleardots(groupID).equals("284")) {
					groupID = "5.1.4.";
				}
				if (cleardots(groupID).equals("285")) {
					groupID = "5.1.5.";
				}
				if (cleardots(groupID).equals("286")) {
					groupID = "5.1.6.";
				}
				if (cleardots(groupID).equals("287")) {
					groupID = "5.1.7.";
				}
				if (cleardots(groupID).equals("288")) {
					groupID = "5.1.8.";
				}
				if (cleardots(groupID).equals("289")) {
					groupID = "5.1.9.";
				}

				
				
				if (cleardots(groupID).equals("1101")) {
					groupID = "1.a.1.";
				}
				if (cleardots(groupID).equals("1102")) {
					groupID = "1.a.2.";
				}
				if (cleardots(groupID).equals("1103")) {
					groupID = "1.a.3.";
				}
				
				
				if (cleardots(groupID).equals("1111")) {
					groupID = "1.b.1.";
				}
				if (cleardots(groupID).equals("1112")) {
					groupID = "1.b.2.";
				}
				if (cleardots(groupID).equals("1113")) {
					groupID = "1.b.3.";
				}
				
				
				
				if (cleardots(groupID).equals("1121")) {
					groupID = "1.c.1.";
				}
				if (cleardots(groupID).equals("1122")) {
					groupID = "1.c.2.";
				}
				if (cleardots(groupID).equals("1123")) {
					groupID = "1.c.3.";
				}
				
				
				String name = rs.getString("nimi");
				if (groupID.length() > 5) {
					tm.put(cleardots(groupID),Converter.enCodeEst(cleardots(groupID) + " " + name));
				}
			}

			rs.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tm;
	}

	private String cleardots(String idx) {
		while (idx.indexOf(".") > 0) {
			idx = idx.substring(0, idx.indexOf("."))
					+ idx.substring(idx.indexOf(".") + 1);
		}

		return idx;
	}

	public TreeMap getAgents() {

		String cmd = "SELECT [Code] AS kood,[Name] AS nimi,[Location Code] AS objekt ,left ([Location Code for MobileJava],3) AS objekt2 FROM "
				+ "[Vičiūnai ir Ko$Salesperson_Purchaser] WHERE len([Location Code])>0";
		if (isDebug())
			System.out.println(cmd);
		StringBuffer sb2 = new StringBuffer();
		  Properties props = EksportFrame.loadProps(parent.getProps().getProperty("emailfile"));
		  
		TreeMap agents = new TreeMap();
		try {
			Connection con = getNewConnection2();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				Agent agent = new Agent(rs.getString("kood").trim());
				String name = rs.getString("nimi").trim();
				agent.setObject(rs.getString("objekt"));
				agent.setName(name);
				agent.setIsPilot(true);
				agents.put(name, agent);
				 agents1.put(agent.getID(), agent);
				 agent1.put(agent.getID(), agent);
				  if(props.containsKey(agent.getID())){
		        	  sb2.append(agent.getID() + ";" + name + "\n");  
		          }
			}

			 if(sb2.length() > 2){
			    	try{
			    		 Converter.saveFileInUTF8( "agent.txt", sb2);
			    	}catch (Exception e){
			    		
			    	}
			      
			     }
			
			Agent aa = new Agent("HOUSE1");
			aa.setName("office1");
			// aa.setObject("KAU");
			agents.put("office1", aa);

			cmd = "SELECT k.[No_] AS klient, [Balance total (LCY)]AS summa "
					+ " FROM [Vičiūnai ir Ko$Customer] k  WHERE k.[Salesperson Code] != '' AND [Balance total (LCY)]<>0 ";
			System.out.println(cmd);
			con = getNewConnection2();
			Statement stmt1 = con.createStatement();
			ResultSet rs1 = stmt1.executeQuery(cmd);
			while (rs1.next()) {

				String id = rs1.getString("klient");
				Debt debt = new Debt();
				// debt.setDoknr(clear(st2.nextToken()));
				debt.setDue(rs1.getString("summa"));
				overdebts.put(id, debt);
			}

			rs1.close();
			rs.close();
			con.close();
		} catch (Exception e) {
			getParent().showError(e);
		}

		return agents;
	}

	public String getDistribution() {
		return null;
	}

	 public TreeMap getAgents1() {
			return agents1;
		}

	private TreeMap agents1 = new TreeMap();
	private TreeMap agent1 = new TreeMap();
	
	public TreeMap getCustomers(Agent agent) {
		
		try{
			   Converter.getManagersReplacements(agent1, rcompany, user, pass); 
				
		}catch(Exception e){
			
		}
		
		TreeMap freeze = new TreeMap();
	    TreeMap custAgents = new TreeMap();
	   	HashMap custagVisit = new HashMap();
	   	HashMap custagName = new HashMap();
	   	HashMap agendiTyyp = new HashMap();
	   	HashMap custVisit = new HashMap();

	  	HashMap saamata = new HashMap();
		TreeMap tm = new TreeMap();
		this.customers = new ArrayList();
		Converter conv = new Converter();
    	  HashMap kyssad =   conv.getQuestionaire(this.getExcelDir());
    	  String qqq = "select Agent as agent , Customer  as klient , [Route Code] as day from " +
	 		"[Vičiūnai ir Ko$Route sequence]	 ";
  	  
  	  String sql2 = "SELECT d.[Document No_] AS dokumentas,d.No_ AS preke,d.Quantity AS kiekis,d.[Užsakytas kiekis] AS UzsKiekis,d.[Shortcut Dimension 1 Code] AS padalinys, " +
" c.[Sell-To Customer No_] AS klientas,c.[Posting Date] AS data,d.[Document No_] AS UzsDok,'Uzsakymas' AS tipas,d.Description as nimi" +
" FROM [Vičiūnai ir Ko$Sales Line] d  INNER JOIN [Vičiūnai ir Ko$Sales Header] c ON d.[Document No_] = c.No_ WHERE (d.Type = 2) AND (c.Status = 1) AND " +
" (c.[External Document No_] <> '') AND (c.[Posting Date] > GETDATE()-1) AND (c.[Posting Date] < GETDATE()-0) " +
" UNION ALL " +
" SELECT b.[Document No_] AS dokumentas, b.No_ AS preke,b.Quantity AS kiekis,(select MAX([Užsakytas kiekis]) from [Vičiūnai ir Ko$Sales Shipment Line]  " +
"  WHERE [Order No_] = a.[Order No_] AND No_ = b.No_ AND Quantity = b.Quantity) AS UzsKiekis,b.[Shortcut Dimension 1 Code] AS padalinys, " +
" a.[Sell-To Customer No_] AS klientas,a.[Posting Date] AS data, a.[Order No_] AS UzsDok,'SF' AS tipas ,b.Description as nimi from [Vičiūnai ir Ko$Sales Invoice Line] b " +
" INNER JOIN [Vičiūnai ir Ko$Sales Invoice Header] a  ON b.[Document No_] = a.No_  WHERE (b.Type=2) AND  (a.[Posting Date] > GETDATE()-1) AND " +
"  (a.[Posting Date] < GETDATE()-0)";
  	  
  	  
		String sql = " SELECT k.[Name] AS nimi,k.[No_] AS kood,k.[Bill-to Customer No_] AS payer,k.[Address] AS aadress ," +
				"k.[Logisto kodas] AS agent , [Blokuoti SF išrašymą] AS blokk, " +
				" [Payment Terms Code] AS payment,[Customer Disc_ Group] AS alekood, [Balance total (LCY)]AS yle,[RouteCode] AS visit,[Customer type] AS type, " +
				" [Blokavimas po pradelstų dienų] AS extradays ,k.[Asortimento kodas]+k.[Assortment Level] AS sort,PDA_debt AS debt , 0 AS minsum ,k.contact +' ' + k.[Phone no_] as kontakt, " +
				" res.Name as keskus , res.[Location Code] as regioon , k.Tinklas as chain"+
				" FROM [Vičiūnai ir Ko$Customer] k LEFT JOIN [Vičiūnai ir Ko$Sales Line Discount] d ON d.[Sales Code] = k.[Customer Disc_ Group]   AND d.[Sales Type]=1 " +
				"  LEFT JOIN [Vičiūnai ir Ko$Responsibility Center] res on res.code = k.[Responsibility Center]" +
				" WHERE k.[Logisto kodas] != ''  AND blocked != 3 " ;
				  

		
		System.out.println(sql);
		try {
			
			  Connection cone = this.getNewConnection2();			    
			   	Statement stme = cone.createStatement();
			    System.out.println(qqq);
			       ResultSet rse = stme.executeQuery(qqq);
			   
			       while(rse.next()){
			   	   
			    		String age = rse.getString("agent").trim();
			       	    String klient = rse.getString("klient").trim();
			       	    String day = rse.getString("day");
			       	  //  String period = rse.getString("periodtype");
			       	   // String name = rse.getString("nam");
			       	   // String tyyp = rse.getString("tyyp");
			      
			       	  /*  if(tyyp != null && tyyp.trim().equals("1")){
			       	 	 agendiTyyp.put(age, tyyp);
			       	    }*/
			       	    String old = (String)custagVisit.get(age + klient);
			       	 
			       	   String visit = day;
			       	   custagVisit.put(age + klient, visit);
			         //  custagName.put(age + klient, name);
			           
			    	   custVisit.put(klient, visit);
			       	
			       	 
			       	 
			       if(custAgents.get(klient) != null){
			    	   Vector vv = (Vector)custAgents.get(klient) ;
			    	   vv.add(age);
			    	   custAgents.put(klient, vv);
			    	  
			       }else{
			    	   Vector vv = new Vector() ;
			    	   vv.add(age);
			    	   custAgents.put(klient, vv);
			       }
			    	
			       }
			      rse.close();
			      stme.close();
			      cone.close();
		} catch (Exception e ){
			e.printStackTrace();
		}
			      
			      try{
			    	  
			      
			      
			Connection con = getNewConnection2();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next()) {
				try{
					String dokumentas = rs.getString("dokumentas").trim();
					String preke = rs.getString("preke").trim();
					String kiekis = rs.getString("kiekis").trim();
					if(kiekis.indexOf(".") != -1){
						kiekis = kiekis.substring(0,kiekis.indexOf("."));
					}
					String uzsKiekis = rs.getString("UzsKiekis").trim();
					if(uzsKiekis.indexOf(".") != -1){
						uzsKiekis = uzsKiekis.substring(0,uzsKiekis.indexOf("."));
					}
					String padalinys = rs.getString("padalinys").trim();
                 	String klientas = rs.getString("klientas").trim();
                 	String data = rs.getString("data").trim();
                 	String uzsDok = rs.getString("UzsDok").trim();
                 	String tipas = rs.getString("tipas").trim();
                 	String tnimi = rs.getString("nimi").trim();


                 	String text = Converter.enCodeEst("u(" + uzsKiekis + ") k[" + kiekis +  "] " + preke + " " + tnimi + " " + tipas + " " + dokumentas + "||") ;
					if(saamata.get(klientas) != null){
						String asi = (String)saamata.get(klientas) ;
						saamata.put(klientas,asi + text);	
					}else{
						saamata.put(klientas, text);	
					}
					
				}catch (Exception e){
					
				}
			}
			rs.close();
			stmt.close();
			
			
			
			
			
			 stmt = con.createStatement();
			 rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String name = rs.getString("nimi").trim();
				String id = rs.getString("kood").trim();
				// System.out.println(line);
				Customer customer = new Customer(id);
				String payer = null;
				if (name.equals(""))
					name = "<" + id + ">";
				customer.setName(name);
				payer = rs.getString("payer");
				if (payer != null && payer.trim().length() > 0) {
					customer.setPayer(payer.trim());
				}

				String aadress = rs.getString("aadress");

				if (aadress != null && aadress.trim().length() > 0) {
					customer.setAddress(Converter.enCodeEst(aadress));
					customer.setName(Converter.enCodeEst(customer.getName() + " " + aadress));
				}

				String ag = rs.getString("agent").trim();
				customer.setMainAgent(ag);
				customer.addAgent(new Agent(ag));
				
				if (ag != null && agent1.get(ag.trim()) != null){
					Agent aa = (Agent)agent1.get(ag.trim());
				
					Vector vec = aa.getDubbler();
					if(vec!= null && vec.size() >0){
						Enumeration en = vec.elements();
						while(en.hasMoreElements()){
							String age = (String) en.nextElement();
							customer.addAgent(new Agent(age));
						}
					}
				}
				

				   if(custAgents.get(customer.getID()) != null){
			        	Vector vv = (Vector) custAgents.get(customer.getID());
			        	for(int i = 0; i < vv.size();i++){
			        		
			        		Agent aa = new Agent((String) vv.elementAt(i));
			        		String visi = (String)custagVisit.get(aa.getID() + customer.getID());
			        		if(visi != null && visi.trim().length() > 0){
			        			aa.setVisit(visi);	
			        		}
			        		
			        		customer.addAgent(aa);	
			        		 
			        		  if (aa != null ){
			      				Agent agg = ((Agent)this.getAgents1().get(aa.getID()));
			      				
			      				if(agg != null && agg.getDubbler().size()>0){
			      					//System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
			      					for (int r = 0 ; r < agg.getDubbler().size() ; r++){
			      						Agent dub = new Agent((String)agg.getDubbler().elementAt(r));
			      						if(custVisit.get(customer.getID()) != null){
			      							dub.setVisit(custVisit.get(customer.getID()) + "");
			      						}
			      						customer.addAgent(dub);
			      					//	System.out.println(cust.getName() + cust.getAgents());
			      					}
			      				}
			      			}
			        		
			        	}
			        	
			        }
				
				
                
				if (rs.getString("blokk").equals("1")) {
					customer.setSalesBlock(true);
				}

				String paym = rs.getString("payment");

				if (paym != null && paym.trim().length() > 0) {
					customer.setPaymTerm(paym);
				}

				String num = rs.getString("alekood");
				if (num != null && num.trim().length() > 0) {
					customer.setGroup(num.trim());
				}

				String visit = rs.getString("visit");
				if (visit != null && visit.trim().length() > 0) {
					customer.setVisit(visit.trim());
				}

				String type = rs.getString("type");
				String asi = "";
				if (type != null && type.trim().length() > 0) {
				
					asi = type.trim();
				}

	
				String grupp = rs.getString("sort");
				if (grupp != null && grupp.trim().length() > 0) {
					Vector vv = new Vector ();
					vv.add(grupp.trim());
					customer.setFixSortCode(vv);
					
				}
				
				if(!asi.equals("3") && !asi.equals("4") && !asi.equals("5") && !asi.equals("6") &&  !asi.equals("7") 
						&&  customer.getGroup().trim().length() > 0){
					customer.setCustType("MINASORT");
				}
				
			
				
				
              if(freeze.get(customer.getID()) != null){
            	  Vector vv = (Vector) freeze.get(customer.getID());
            	  Enumeration enume = vv.elements();
            	  while (enume.hasMoreElements()){
            		  customer.setInfo(customer.getInfo() + "|" + enume.nextElement());
            	  }
              }
              
              if(freeze.get(customer.getPayer()) != null){
            	  Vector vv = (Vector) freeze.get(customer.getPayer());
            	  Enumeration enume = vv.elements();
            	  while (enume.hasMoreElements()){
            		  customer.setInfo(customer.getInfo() + "|" + enume.nextElement());
            	  }
              }
              
              if(saamata.get(customer.getID()) != null){
            	  String vv = (String) saamata.get(customer.getID());
            	 	  customer.setInfo(customer.getInfo() + "|" + vv);
            	}
         
         
              conv.addQuestiosToCustomer(customer,kyssad);
				   String cont = rs.getString("kontakt");
		           if(cont != null && cont.trim().length()>0){
		               customer.setPersonalData(Converter.enCodeEst(cont.trim()));   
		           }
		           String keskus = rs.getString("keskus");
		        
		          
		           
		           if(keskus != null && keskus.trim().length()>0){
		               customer.setCity("Area : " + Converter.enCodeEst(keskus.trim()));   
		           }
		           
		           String regioon = rs.getString("regioon");
		           if(regioon != null && regioon.trim().length()>0){  
		               Enumeration enume = getParent().getAgentsHash().elements();
			           while (enume.hasMoreElements()) {
			             Agent a = (Agent) enume.nextElement();
			             if(a.getObject()!= null && a.getObject().equals(regioon.trim())){
			            	// customer.addAgent(a);
					     }
			           }
		           }
		           
		           String chain = rs.getString("chain");
		           if(chain != null && chain.trim().length()>0){
		               customer.setChain(Converter.enCodeEst(chain.trim()));   
		           }
		           
				// customer.setPayer(id);
				if ("Lithuanian".equals(lang)
						&& !customer.getID().substring(0, 1).equals("A")) {
					tm.put(customer.getName(), customer);
					this.customers.add(customer.getID());
				} else if ("Estonian".equals(lang)
						&& !customer.getID().substring(0, 1).equals("P")) {
					tm.put(customer.getName(), customer);
					this.customers.add(customer.getID());
				}
			}

			rs.close();
			con.close();
			
			
			//  Converter.sendCustomerJson(tm,this.rcompany, true, this.user,this.pass, true);
				

		} catch (Exception e) {
			e.printStackTrace();
		}
		return tm;
	}

	
	
	
	public TreeMap getProducts(Agent agent) {
		TreeMap tm = new TreeMap();
		TreeMap KAU = new TreeMap();

		TreeMap discT = new TreeMap();

		try {
			String cmd = "SELECT l.[Item No_] AS kood, CAST( sum(l.[Remaining Quantity]) " +
 " -(select (case when count(*)>0 Then sum([Outstanding Quantity]) else 0 end) from Navision.dbo.[Vičiūnai ir Ko$Sales Line] u " +
   " inner join Navision.dbo.[Vičiūnai ir Ko$Sales Header] h on h.No_= u.[Document No_]  " +
  "  where u.No_= l.[Item No_] and h.[Status]=1 and u.[Location Code] in ('KAU_CEN','KLA','KAU','PAN','VIL','ŠIA')) " +
 " AS DECIMAL(12,2)) AS kogus  FROM Navision.dbo.[Vičiūnai ir Ko$Item Ledger Entry] l  " +
   "      INNER JOIN Navision.dbo.[Vičiūnai ir Ko$Item] t ON [No_]=l.[Item No_]  " +
    "     WHERE  l.[Location Code] in ('BFE') " +
     "    AND l.[Open] = 1 AND t.[Blocked]=0 AND t.[Unit Price]>0 AND len(t.[Grupės kodas])>0 " +
      "   GROUP BY l.[Item No_] having  CAST( sum(l.[Remaining Quantity]) " +
 " -(select (case when count(*)>0 Then sum([Outstanding Quantity]) else 0 end) from Navision.dbo.[Vičiūnai ir Ko$Sales Line] u " +
   " inner join Navision.dbo.[Vičiūnai ir Ko$Sales Header] h on h.No_= u.[Document No_]  " +
  "  where u.No_= l.[Item No_] and h.[Status]=1 and u.[Location Code] in ('BFE')) " +
 " AS DECIMAL(12,2)) is not null ";

			Connection con = getNewConnection2();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String toode = null;
				double seis = 0;
				;
				toode = rs.getString("kood").trim();
				seis = rs.getDouble("kogus");
				KAU.put(toode, seis + "");

			}
System.out.println("KAU suurus " + KAU.size());
			rs.close();
			con.close();

			String cmd7 = " select sd.[Code] as kood ,[Line Discount %] as disc from [Vičiūnai ir Ko$Sales Line Discount] sd "
					+ " where getdate() between [Starting Date] and [Ending Date] +1 ";

			con = getNewConnection2();
			stmt = con.createStatement();
			rs = stmt.executeQuery(cmd7);
			while (rs.next()) {
				String toode = null;
				double seis = 0;
				;
				toode = rs.getString("kood").trim();
				seis = rs.getDouble("disc");
				discT.put(toode, seis + "");

			}

			rs.close();
			con.close();

			String cmd0 = "select sum([Outstanding Quantity]) as kogus ,r.No_ as toode  "
					+ " FROM [Vičiūnai ir Ko$Sales Line] r "
					+ "    INNER JOIN [Vičiūnai ir Ko$Sales Header] d ON r.[Document No_]= d.[No_] where "
					+ "  r.[Location Code] in ('KAU','KAU_CEN') group by r.[No_] "
					+ // status=1 and
					"order by r.[No_]";

			con = getNewConnection2();
			stmt = con.createStatement();
			rs = stmt.executeQuery(cmd0);
			while (rs.next()) {
				String toode = null;
				double seis = 0;
				seis = rs.getDouble("kogus");
				toode = rs.getString("toode").trim();
				if (KAU.containsKey(toode)) {
					double seiss = Double.parseDouble((String) KAU.get(toode))
							- seis;
					if (seiss > 0) {
						KAU.put(toode, seiss + "");
					}
				}

			}

			rs.close();
			con.close();

			System.out.println("Kau" + KAU.size());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			String cmd = "SELECT t.[No_] AS toode,(t.[Search Description]) AS nimi, t.[Unit Price] AS myygihind, t.[Grupės kodas] AS grupp,t.[Pogrupio kodas] AS grupp2 "
					+ "	,[Base Unit of Measure] AS unit,[Brūkšninis kodas] AS ean,[Product Status] AS status ,[Line Discount %] AS maxdiscount, "
					+ "	  t.[Type] AS type, [Qty_ per Unit of Measure] AS kastis,  [Net Weight] AS kaal, [VAT Prod_ Posting Group] AS vat, [Description] AS vatdesc , '' as type"
					+ "	  FROM [Vičiūnai ir Ko$Item] t LEFT JOIN [Vičiūnai ir Ko$Item Unit of Measure] vat ON (vat.[Item No_]=[No_] and vat.[Code]='DĖŽ.' ) "
					+ "	  LEFT JOIN  [Vičiūnai ir Ko$Sales Line Discount] sd  on  sd.[Code]= [No_] and [Sales Type] = 1 and  [Sales Code] = 'AGENT' "
					+ " and  [Starting Date]<=getdate()  and   [Ending Date]>=getdate()   "
					+ "	  WHERE ([Blocked]=0 AND [Unit Price]>0 AND len([Grupės kodas])>0 and  len([Search Description]) != 0) "
					+ "	  or [Pogrupio kodas] like '%2.8.1%' ORDER BY [No_] "; //'%2.08.01%'
			System.out.println(cmd);
			Connection con = getNewConnection2();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String id = rs.getString("toode").trim();
				String weight = "";
				Product prod = new Product(id);
				String name = rs.getString("nimi");
				name = name.substring(0, 1)
						+ (name.substring(1, name.length())).toLowerCase();
				double price = rs.getDouble("myygihind");
				prod.setPrice(1);

				
				String gr1 = rs.getString("grupp");
				if (gr1.equals("1.10.")) {
					gr1 = "1.a.";//5.01.
				}
				if (gr1.equals("1.11.")) {
					gr1 = "1.b.";//5.01.
				}
				if (gr1.equals("1.12.")) {
					gr1 = "1.c.";//5.01.
				}
				if (gr1.equals("1.13.")) {
					gr1 = "1.d.";//5.01.
				}
			
				String grupp = cleardots(gr1);
				if (grupp.equals("208")) {
					grupp = "501";
				}
				prod.setGroup(grupp);

				String split = prod.getGroup().substring(0, 1);
				if (split.equals("1")) {
					prod.setSendSeparate("1");
				} else {
					prod.setSendSeparate("2");
				}
  
				String gr2 = rs.getString("grupp2");
   
				
				if (cleardots(gr2).equals("1101")) {
					gr2 = "1.a.1.";
				}
				if (cleardots(gr2).equals("1102")) {
					gr2 = "1.a.2.";
				}
				if (cleardots(gr2).equals("1103")) {
					gr2 = "1.a.3.";
				}
				
				
				if (cleardots(gr2).equals("1111")) {
					gr2 = "1.b.1.";
				}
				if (cleardots(gr2).equals("1112")) {
					gr2 = "1.b.2.";
				}
				if (cleardots(gr2).equals("1113")) {
					gr2 = "1.b.3.";
				}
				
				
				
				if (cleardots(gr2).equals("1121")) {
					gr2 = "1.c.1.";
				}
				if (cleardots(gr2).equals("1122")) {
					gr2 = "1.c.2.";
				}
				if (cleardots(gr2).equals("1123")) {
					gr2 = "1.c.3.";
				}
				
				String grupp2 = cleardots(gr2);
			
				
			if (grupp2.equals("281")) {
				grupp2 = "511";
			}
			if (grupp2.equals("282")) {
				grupp2 = "512";
			}
			if (grupp2.equals("283")) {
				grupp2 = "513";
			}
			if (grupp2.equals("284")) {
				grupp2 = "514";
			}
			if (grupp2.equals("285")) {
				grupp2 = "515";
			}
			if (grupp2.equals("286")) {
				grupp2 = "516";
			}
			if (grupp2.equals("287")) {
				grupp2 = "517";
			}
			if (grupp2.equals("288")) {
				grupp2 = "518";
			}
			if (grupp2.equals("289")) {
				grupp2 = "519";
			}
			
			
			
		
			
			
			
				/*if (grupp2.equals("20801")) {
					grupp2 = "50101";
				}
				if (grupp2.equals("20801")) {
					grupp2 = "50101";
				}
				if (grupp2.equals("20802")) {
					grupp2 = "50102";
				}
				if (grupp2.equals("20803")) {
					grupp2 = "50103";
				}
				if (grupp2.equals("20804")) {
					grupp2 = "50104";
				}
				if (grupp2.equals("20805")) {
					grupp2 = "50105";
				}
				if (grupp2.equals("20806")) {
					grupp2 = "50106";
				}
				if (grupp2.equals("20807")) {
					grupp2 = "50107";
				}
				if (grupp2.equals("20808")) {
					grupp2 = "50108";
				}
				if (grupp2.equals("20809")) {
					grupp2 = "50109";
				}*/
				prod.setGroupSecond(grupp2);

				prod.setUnit(rs.getString("unit"));
				String ean = (rs.getString("ean"));
				if (ean.trim().length() > 0) {
					prod.setEAN(ean);
					try{
						String add = ean.substring(ean.length()-6);
					if(prod.getUnit().toLowerCase().indexOf("kg") != -1){
						add = ean.substring(2);
						if(add.length()>6){
							add = add.substring(0,6);
						}
					}
						name = name + " " + add;	
					}catch(Exception e){
						
					}
					
				}
			/*	String xxx = rs.getString("status");
				int xx = Integer.parseInt(xxx);
				if (xx == 1) {
					prod.setStatusTop("1");
				} else if (xx == 2) {
					prod.setMustBe("1");
				} else if (xx == 3) {
					prod.setMustBe("1");
					prod.setStatusTop("1");
				} else if (xx == 4) {
					prod.setStatusNew("1");
				} else if (xx == 5) {
					prod.setStatusSellout("1");
				}*/

				String discou = rs.getString("maxdiscount");
				if (discou != null && discou.trim().length() > 0) {

				}

				double cases = rs.getDouble("kastis");
				if (cases > 0) {
					prod.setInCases(cases);
				}

				weight = rs.getString("kaal");
				if (weight != null && weight.trim().length() > 0) {
					weight = weight.trim();
					if (weight.indexOf('.') == 0) {
						weight = "0" + weight;
					}

				}

				String vat = rs.getString("vat");
				if (vat != null && vat.trim().length() > 0) {
					if (vat.equals("GRUPE4")) {
						prod.setVat("18");
					} else if (vat.equals("GRUPE2")) {
						prod.setVat("5");
					} else if (vat.equals("GRUPE19")) {
						prod.setVat("19");
					} else if (vat.equals("GRUPE1")) {
						prod.setVat("0");
					} else if (vat.equals("GRUPE21")) {
						prod.setVat("21");
					}
				}

				/*if (KAU.containsKey(id)) {
					String aaa = (String) KAU.get(id);
					prod.setPartii("Kau+KauCe"
							+ aaa.substring(0, aaa.indexOf(".")));
				 //	prod.setStock(Double.parseDouble(aaa));
				}*/

				if (discT.containsKey(id)) {
					String aaa = (String) discT.get(id);
					prod.setMaxDiscount(Double.parseDouble(aaa));
				//	prod.setStatusDiscount("1");
				}

				prod.setName(Converter.enCodeEst(weight.substring(0, weight.indexOf('.') + 3) + " "
						+ name));
				tm.put(prod.getName(), prod);
			}

			rs.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tm;
	}

	public String getAssortmentStr(Agent agent) {
		StringBuffer sb = new StringBuffer();
		String cmd = " SELECT [Asortimento kodas] AS grupp,[Prekės Nr_] AS toode  FROM [Vičiūnai ir Ko$Prekių asortimentas] r " +
					"  INNER JOIN [Vičiūnai ir Ko$Item] t ON t.[No_]=r.[Prekės Nr_]  where [Asortimento kodas]= 'MINASORT' " +
                    "  group by [Asortimento kodas] , [Prekės Nr_] , [Net Weight] ,[Search Description] " +
					"  ORDER BY [Net Weight] , [Search Description] ";
		try {
			Connection con = getNewConnection2();
			System.out.println(cmd);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String type = rs.getString("grupp");
				String id = rs.getString("toode");			
				// ABCD
				int i = type.trim().length();
				if (i > 0) {
					while (i != 0) {
						sb.append(" ; ;" + type + ";" + id
								+ "\n");
						i = i - 1;
					}
				}
			}

			rs.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public Vector getCustomerProductsList(String agentCode) {
		Vector vec = new Vector();
		HashMap kliendid = new HashMap();
		try {
			
			
			String cmd = "select  a.[Sell-to Customer No_] AS klient,b.[No_] AS toode,b.[Quantity] AS kogus, a.[Posting Date] AS lastdate ,   " +
                 "  b.[Užsakytas kiekis] -b.[Quantity] as saamatta FROM [Vičiūnai ir Ko$Sales Line] b INNER JOIN [Vičiūnai ir Ko$Sales Header] a ON b.[Document No_]= a.[No_]   " +
                  "              WHERE (a.[Posting Date]>= GETDATE()-8) and ((b.type=2) and (a.status=1)) and (b.[Užsakytas kiekis] >b.[Quantity]) " +
                   "             UNION ALL  " +
                    "          select  c.[Sell-to Customer No_] AS klient,d.[No_] AS toode,d.[Quantity] AS kogus, c.[Posting Date] AS lastdate , " +
                     "          d.[Užsakytas kiekis] -d.[Quantity] as saamatta FROM [Vičiūnai ir Ko$Sales Shipment Line] d INNER JOIN [Vičiūnai ir Ko$Sales Shipment Header] c ON d.[Document No_]= c.[No_] " +
                      "       WHERE (c.[Posting Date]> GETDATE()-8) and (d.type=2) and (d.[Užsakytas kiekis] >d.[Quantity])";	

			
			System.out.println(cmd);
			// "klient","toode","kogus","viimane kuupļæ½ev","viimane kogus"
			Connection con = getNewConnection2();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {

				String custID = rs.getString("klient").trim();

				String prodID = rs.getString("toode").trim();
				;

				double amount = 0;
				String lastdate = " ";
				amount = rs.getDouble("kogus");
				lastdate = rs.getString("lastdate").trim().substring(5, 10);
				Customer cust = (Customer) kliendid.get(custID);
				if (cust == null) {
					cust = new Customer(custID);
				}
				double saamatta = rs.getDouble("saamatta");
				if(saamatta > -1 && saamatta < 1){
					saamatta = 0;
				}
				Product prod = new Product(prodID);
				CustProd cp = new CustProd(custID, prodID, amount, 0, 0,
						lastdate, 0,saamatta , 0, " ");

				cust.addProduct(prod, cp,false);
				kliendid.put(custID, cust);
				vec.addElement(cp);

			}
			rs.close();
			con.close();
/*
			int counter = 0;
			int cou = 0;
			String oldklient = null;
			String olddate = " ";
			String oldtoode = null;

			String cmd2 = "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Shipment Date] AS lastdate FROM [Vičiūnai ir Ko$Sales Invoice Line] r "
					+ " INNER JOIN [Vičiūnai ir Ko$Sales Invoice Header] d ON r.[Document No_]= d.[No_] WHERE d.[Shipment Date]> GETDATE()-30 "
					+ " Order by d.[Sell-to Customer No_],r.[No_],d.[Shipment Date] desc ";
			System.out.println(cmd2);
			// "klient","toode","kogus","viimane kuupļæ½ev","viimane kogus"
			Connection con2 = getNewConnection2();
			Statement stmt2 = con2.createStatement();
			ResultSet rs2 = stmt2.executeQuery(cmd2);

			while (rs2.next()) {
				String klient = rs2.getString("klient").trim();
				String toode = rs2.getString("toode").trim();
				String kogus = "0";
				String date = " ";
				kogus = rs2.getDouble("kogus") + "";
				kogus = kogus.replace('.', ',');
				if (kogus.indexOf(",") != -1) {
					kogus = kogus.substring(0, kogus.indexOf(","));
				}
				date = rs2.getString("lastdate").trim().substring(0, 10);

				if (cou == 0) {
					oldklient = klient;
					olddate = date;
					oldtoode = toode;
				}
				if (!oldklient.equals(klient)) {
					counter = 0;
					oldklient = klient;
					olddate = date;
				}
				if (!toode.equals(oldtoode)) {
					counter = 0;
				}
				Customer c = (Customer) kliendid.get(klient);
				if (c != null) {

					ArrayList al = (ArrayList) c.getProducts().get(toode);
					if (al != null) {
						CustProd cp = (CustProd) al.get(1);
						if (cp != null) {
							if (!olddate.equals(date)) {
								counter++;
							}
							if (counter == 0) {
								cp.setS1(kogus);
							}
							if (counter == 1) {
								cp.setS2(kogus);
							}
							if (counter == 2) {
								cp.setS3(kogus);
							}
							if (counter == 3) {
								cp.setS4(kogus);
							}
							if (counter == 4) {
								cp.setS5(kogus);
							}

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
*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		return vec;
	}

	public String getStartingTime() {
		return null;
	}

	public String getDriverVersion() {
		return "VichiLITMerc";
	}

	public String getSubjectFilesTo() {
		return this.subjectFilesTo;
	}

	public double getSalary(String agentCode) {
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
	 * @param nf
	 *            The nF to set.
	 */
	public void setNF(NumberFormat nf) {
		NF = nf;
	}

	/**
	 * setBulkSales
	 * 
	 * @param agents
	 *            Map
	 */
	public void setBulkSales(Map agents) {

	}
}
