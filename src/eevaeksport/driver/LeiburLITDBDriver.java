package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
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
import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
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
 * Title: Eeva eksport Description: Copyright: Copyright (c) 2002 Company:
 * 
 * @author Tarmo Tubro
 * @version 1.0
 */

public class LeiburLITDBDriver extends AbstractDBDriver implements Manager, Promo,
		BulkUpdater, Discounter {
	public LeiburLITDBDriver(EksportPanel parent) {
		super(parent);
		if (parent.getParentFrame().isDaySales()) {
			this.subjectFilesTo = getParent().getString("senddatamsg");
		} else {
			this.subjectFilesTo = parent.getString("filemsg");
		}
		String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
		try {
			Class.forName(databaseDriverName).newInstance();
			con = DriverManager.getConnection(url, "merisoft", getParent().getProps().getProperty("dsn.pass"));
			stmt = con.createStatement();
		} catch (Exception e) {
			parent.showError(e);
		}

		  user =  getParent().getProps().getProperty("rest.user");
		    pass = getParent().getProps().getProperty("rest.pass");
		    rcompanyx = getParent().getProps().getProperty("rest.companyx");
		  
	}

	 String user =  null;
	  String pass = null;
	  String rcompany = null;
	  String rcompanyx = null;
	


	public String getDealerCustomerCSV(String agent) {
		return null;
	}

	public Vector getLastThreeData() {

		Vector result = new Vector();
		// Siin loeme igale kliendile kylge viimase kolem korra ostud
		try {

			Connection con = getNewConnection();
			Statement st = con.createStatement();

			String cmd2 = "select * from CustomerData";

			if (isDebug())
				System.out.println(cmd2);
			ResultSet rs = st.executeQuery(cmd2);

			while (rs.next()) {

				String accountnum = rs.getString("CustomerID").trim();

				
				//	paneme tuleviku siia ette 
				String sqlt = "select ItemNumber as toode ,OrderedAmount as kogus , RequestedDeliveryDate as kuup ,Tarne "
						+ " from futureOrders_view  where CustomerCode = '"
						+ accountnum + "'  order by toode,RequestedDeliveryDate desc";

				Connection con3 = getNewConnection();
				Statement st3 = con3.createStatement();
				ResultSet rs3 = st3.executeQuery(sqlt);
				boolean hasTom = false;
				while (rs3.next()) {
					hasTom = true;
					MatrixLine ml = new MatrixLine();
					ml.setCustID(accountnum);
					ml.setProdID(rs3.getString("toode").trim());
					ml.setPrice(rs3.getDouble("kogus"));
					String kuup = rs3.getString("kuup");
					ml.setStartDate(kuup.substring(4,8));
					if(getDate(kuup).after(Calendar.getInstance().getTime())) {
						ml.setCliGroup("green");
					}
					result.add(ml);
				}
				rs3.close();
				st3.close();
				String asi ="5";
				if(hasTom){
					asi = "4";
				}
				
				
				
				String sq1 = "select top " + asi + " DeliveryDate from  Last3DaysSales where CustomerID ='" + accountnum + "' " +
  " group by CustomerID ,DeliveryDate  order by CustomerID ,DeliveryDate desc";
				Connection con2 = getNewConnection();
				Statement st2 = con2.createStatement();

				ResultSet rs2 = st2.executeQuery(sq1);
				while (rs2.next()) {
					int kuup = rs2.getInt("DeliveryDate");
					String sq3 = "select ProductID as toode , Quantity  as kogus from  Last3DaysSales where CustomerID ='"
							+ accountnum
							+ "' and DeliveryDate ="
							+ kuup
							+ " order by ProductID";
					 con3 = getNewConnection();
					 st3 = con3.createStatement();
					 rs3 = st3.executeQuery(sq3);
					while (rs3.next()) {
						MatrixLine ml = new MatrixLine();
						ml.setCustID(accountnum);
						ml.setProdID(rs3.getString("toode").trim());
						ml.setPrice(rs3.getDouble("kogus"));
						ml.setStartDate((kuup+"").substring(4,8));
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

	/**
   *
   */
	public void connectionUp() {
	}

	public String getAssortmentStr(Agent agent) {
		return null;
	}

	public void insertSuperVisorSQL(SupervisorInfoWrapper wr) {
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
				+ "?)"; // resint3 L

		try {
			System.out.println("jam");
			Connection con = getNewConnection();
			PreparedStatement ps = con.prepareStatement(sql);

			for (Iterator iter = wr.getInfo().getLines().iterator(); iter
					.hasNext();) {
				SuperVisorLineWrapper lw = new SuperVisorLineWrapper(
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
				//	System.out.println("cr3 '" + cr3 + "'");

					// e.printStackTrace();

				}

				ps.setString(32, getDateStr(cal));
				ps.setInt(33, lw.getLine().getResInt1());
				ps.setInt(34, lw.getLine().getResInt2());
				ps.setInt(35, lw.getLine().getResInt3());
				ps.executeUpdate();
				/*
				 * Connection cco = this.getNewConnection(); Statement st =
				 * cco.createStatement(); // st.executeQuery("exportsari");
				 * st.close(); cco.close();
				 */
			}

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
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
				+ "?)"; // resint3 L

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
					//System.out.println("cr3 '" + cr3 + "'");

					// e.printStackTrace();

				}

				ps.setString(32, getDateStr(cal));
				ps.setInt(33, lw.getLine().getResInt1());
				ps.setInt(34, lw.getLine().getResInt2());
				ps.setInt(35, lw.getLine().getResInt3());
			
				
				  Connection cco = this.getNewConnection(); 
				  Statement st =  cco.createStatement(); 
				  st.executeUpdate("delete from supervisor where prodid = '" + lw.getLine().getProdID() + 
						  "' and custid = '" + lw.getLine().getCustID()+ "' and ispartner = 1");
				   
				  st.close(); 
				  cco.close();
				  
					ps.executeUpdate();
				  
				 
			}

			ps.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	 public String getSaveDebtInfo() {
		  makePictureCat();
		  System.exit(0);
	   	return null;
	   }
	 
	 
	 private void makePictureCat(){
		    
		  	
		  	String pictssou = parent.getProps().getProperty("pict.dir");
			    String pictspda = parent.getProps().getProperty("pictpda.dir");
		     
			 try {
		 	  
		      File f = new File(pictspda +"list.txt");
		      if (f.exists())f.delete();
		      //Kataloogi sisude kustutamine
		  	 	 File res2 = new File (pictspda);
		           String[] ff2 = res2.list();
		       	 for(int i = 0;ff2.length>i;i++){
		           	(new File ( pictspda + ff2[i])).delete(); 
		           }
//		       	 Lõppes kustutamine 
				 
				 TreeMap prods = this.getProducts(null);

		       StringBuffer buf = new StringBuffer();
		       Iterator enume = prods.keySet().iterator();
		  
		       while (enume.hasNext()) {
		  	    	String asi = (String)enume.next();
		  	    	Product prod = (Product)prods.get(asi);
		  	    	String toode = prod.getID().trim();  
		  	    
		             if(toode.length() > 0){
		              	
		  	   	  res2 = new File (pictssou);
		            ff2 = res2.list();
		           
		       	 for(int i = 0;ff2.length>i;i++){
		           	if( ff2[i].startsWith(toode + ".")){
		             // System.out.println("pilt olemas " + ff2[i]);
		          	File in = new File ( pictssou + ff2[i]); 
		            	if(in.isFile()){
		            	File inpda = new File ( pictspda + prod.getID()+ ".jpg");
		            	if(!inpda.exists()){
		                	Converter.shrinkAndCopyFile(in,inpda,400);
		        	    if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
		                  		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
		                  	}
		        	  	}   }
		               }
		             }
		           }
		     }
		       try {
		 		PrintWriter os = new PrintWriter(new FileWriter(pictspda + "list.txt", true));
		 		os.write(buf.toString());
		 		os.close();
		 	  }catch (Exception ex) {
		 		ex.printStackTrace();
		 	  }
			 
			 } catch (Exception e) {
		  	    getParent().showError(e);
		  	}
		   
		}

	public String getSaveStockInfo() {

		String cmd = "WarehouseBalance_proc";

		if (isDebug())
			System.out.println(cmd);

		StringBuffer products = new StringBuffer();
		try {

			Connection con1 = getNewConnection();
			Statement st = con1.createStatement();
			ResultSet rs = st.executeQuery(cmd);

			while (rs.next()) {

				String kood = rs.getString("ProductID").trim();
				double kogus = rs.getDouble("StockBalance");
				String dealer = rs.getString("WarehouseID").trim();

				products.append(kood + ";");
				products.append(" ;");
				products.append(getParent().getNF().format(kogus) + ";"
						+ dealer + "\n");
				// products.append(getParent().getNF().format(rs.getDouble("stock"))
				// + "; ; \n");
			}
			rs.close();
		} catch (Exception e) {
			getParent().showError(e);
		}
		return products.toString();
	}

	public String getCustProdDiscounts(Agent agent) {
		return null;
	}

	/**
   *
   */
	public void connectionDown() {
	}

	public TreeMap getAgents() {

		TreeMap agents = new TreeMap();
		aHash = new HashMap();
		agents1 = new TreeMap();
		  agent1 = new TreeMap();
		 Properties propse =  new Properties();
		    try{
		    	propse =  EksportFrame.loadProps("emails.txt");
		  	  
		    }catch(Exception e){
		    }
		    
		 StringBuffer sb2 = new StringBuffer();
		try {
			con = DriverManager.getConnection(url, "merisoft", getParent().getProps().getProperty("dsn.pass"));
			Statement stmt = con.createStatement();
			ResultSet rs2 = stmt.executeQuery("SalesRepReplacements_proc");
			while (rs2.next()) {
				String id = rs2.getString("SalesRep");
				String asendaja = rs2.getString("Replacement");
				agents1.put(id, asendaja);
			}

			rs2.close();

			ResultSet rs = stmt.executeQuery("SalesRep_proc");
			while (rs.next()) {
				String kood = rs.getString("SalesRep").trim();
				Agent agent = new Agent(kood);
				String name = rs.getString("SalesRepName").trim();
				agent.setName(name);
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
                if(email.length() > 1){
              	  agent.setActive(true);
                }
				
				
				Iterator enums = agents1.keySet().iterator();
				while (enums.hasNext()) {
					String a = (String) enums.next();

					if (a.equals(agent.getID())) {
						agent.addDubbler((String) agents1.get(a));

					}
				}

				String agentAGENTS = getParent().getProps().getProperty("dub." +agent.getID());
				if(agentAGENTS != null){
    	           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
			    while(stag.hasMoreTokens()){  	
                 agent.addDubbler(stag.nextToken());
                 System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
			    }
                }
			    
				sb2.append(agent.getID() + ";" + name + "\n");  
				agents.put(name, agent);
				aHash.put(agent.getID(), agent);
				agent1.put(agent.getID(), agent);
			}
			rs.close();
			stmt.close();
			con.close();

		} catch (Exception e) {
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

private HashMap visitDays = new HashMap();
private HashMap visitWeeks = new HashMap();
private HashMap phones = new HashMap();
	
	 public TreeMap getDegu() {
			TreeMap result = new TreeMap();
			File f = new File(this.getExcelDir() + "Routes.xls");
			try {
				FileInputStream ff = new FileInputStream(f);
				HSSFWorkbook workbook = new HSSFWorkbook((ff));
				HSSFSheet sheet = workbook.getSheet("Route");
				HSSFRow row = null;

				// System.out.println(groups.toString());

				for (int i = 1; i <= sheet.getLastRowNum(); i++) {
					row = sheet.getRow(i);
					String custID = "";
					String day  = "";
					String week = "";
					String phoneno = "";
					try{
					if (row.getCell(0) != null) {

						try {
							custID = (int) (row.getCell(3).getNumericCellValue()) + "";
						} catch (Exception e) {
							custID = (row.getCell(3).getStringCellValue());
						}
						
						try {
							day = (int) (row.getCell(0).getNumericCellValue()) + "";
						} catch (Exception e) {
							day = (row.getCell(0).getStringCellValue());
						}
						
	               
		                 try {
		                   week = (row.getCell(1).getStringCellValue());	     						 		
		     	         } catch (Exception e) {
		     	          	week = (int) (row.getCell(1).getNumericCellValue()) + "";
		     	         }

			             try {
			               phoneno = (row.getCell(7).getStringCellValue());	     						 		
			     	     } catch (Exception e) {
			     	      	 phoneno = (int) (row.getCell(7).getNumericCellValue()) + "";
			     	     } 
			     	          
	                //   System.out.println(custID + " " + day + " "+ week + " "+ phoneno);
	                   
	                   if(custID.length() > 0){
	                	  
	                	  // visitDays.put(custID, getVisitDay(day + week));
	                	   phones.put(custID, phoneno);
	                	   
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


	
	

	 private TreeMap agent1 = new TreeMap();
	  
	public TreeMap getCustomers(Agent agent) {
		TreeMap customers = new TreeMap();
	//	getDegu();
		
		try{
			   Converter.getManagersReplacements(agent1, rcompanyx, user, pass); 
				
		}catch(Exception e){
			
		}
		

		HashSet outlist = new HashSet();
		
	//	Converter conv = new Converter();
	    	//  HashMap kyssad =   conv.getQuestionaire(this.getExcelDir());
		String cmd = "    select CustomerID as klient,ProductID as toode,max(DeliveryDate)AS kuupaev,max(InvoicedQuantity) AS arvkogus, sum (InvoicedQuantity) as pluss " +
				" from CustomerProductSales group by  CustomerID,ProductID ORDER BY CustomerID,ProductID ";

		
		
		String cmd5 = " select CustomerID as klient , Balance as saldo  from crates ";
		

		if (isDebug())
			System.out.println(cmd);
        HashMap taara = new HashMap();
		custProds = new Vector();
	//	HashMap kliendid = new HashMap();

		try {
			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd5);
			
			while (rs.next()) {
				taara.put(rs.getString("klient").trim(),rs.getString("saldo"));	
			}
			
			rs.close();
			st.close();
			
			 st = con.createStatement();
			 rs = st.executeQuery(cmd);
			
			int cc = 0;
			while (rs.next()) {
				cc++;
				String kliendikood = rs.getString("klient").trim();
				String tootekood = rs.getString("toode").trim();
				String kuup = rs.getString("kuupaev");
				double kogus = rs.getDouble("arvkogus");
				double pluss = rs.getDouble("pluss");
				double fixpr = 0;
				if(outlist.contains(kliendikood + "|" + tootekood)){
					fixpr = -1;
				}

				CustProd cp = new CustProd(kliendikood, tootekood, kogus,
						pluss, 0, kuup, 0, 0, fixpr, " ");
				custProds.add(cp);

			}
			rs.close();
			st.close();
			con.close();

		} catch (Exception e) {
			getParent().showError(e);
		}

	/*	try {

			String cmd2 = "select * from CustomerSales";
			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd2);

			while (rs.next()) {

				String kliendikood = rs.getString("CustomerID").trim();
				double rsumma = rs.getDouble("SalesAmount");
				double kilod = rs.getDouble("SalesKG");

				kliendid.put(kliendikood, rsumma + "|" + kilod);

			}
			rs.close();
			st.close();
			con.close();

		} catch (Exception e) {
			getParent().showError(e);
		}*/

		try {
			
			Connection con = getNewConnection();
			Statement st = con.createStatement();	
			String cmd3 = "Debts_proc ";
  HashMap debts = new HashMap();
			if (isDebug())
				System.out.println(cmd3);
			ResultSet rs1 = st.executeQuery(cmd3);
			while (rs1.next()) {
				String id = rs1.getString("CustomerID").trim();
				double debt = Double.parseDouble(rs1.getString("DebtAmount").trim());				
				debts.put(id, debt + "");					
				}
			rs1.close();
			
			
			String cmd2 = "select * from CustomerData";

			if (isDebug())
				System.out.println(cmd2);
			ResultSet rs = st.executeQuery(cmd2);
			HashMap custMap = new HashMap();

			while (rs.next()) {

				String klgrupp = rs.getString("CustomerGroup");
				if (klgrupp != null) {
					klgrupp = klgrupp.trim();
				} else {
					klgrupp = "";
				}
				String accountnum = rs.getString("CustomerID").trim();
				Customer cust = new Customer(accountnum);

				String name = Converter.enCodeEst(rs.getString("CustomerName").trim());
				cust.restName =   new String((name).getBytes("Windows-1252"), "Windows-1257");
				 
				
				String aadress1 = rs.getString("Address1");
				if (aadress1 != null) {
					aadress1 = aadress1.trim();
				} else {
					aadress1 = " ";
				}
				String aadress2 = rs.getString("Address2");
				if (aadress2 != null) {
					aadress2 = aadress2.trim();
				} else {
					aadress2 = " ";
				}
				cust.setName(Converter.enCodeEst(name + " " +aadress1 + " " + aadress2));
				cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress1 + " " + aadress2),"Lithuania"));
				cust.restStreet = 
						new String(((aadress1 + " " + aadress2).trim()).getBytes("Windows-1252"), "Windows-1257");
				if (visitDays.get(cust.getID()) != null) {
				//	cust.setVisit(visitDays.get(cust.getID()) + "");
				}
				
			/*	if (kliendid.get(cust.getID()) != null) {
					String sss = ((String) kliendid.get(cust.getID()));
					sss = sss.substring(0, sss.indexOf("|"));
					//cust.setSold(Double.parseDouble(sss));
				}*/
				String info = rs.getString("Info");
				if (info != null) {
					cust.setInfo(info.trim());
				}
				
				if (phones.get(cust.getID()) != null) {
					cust.setPersonalData(phones.get(cust.getID()) + "");
				}
				
				String ag = rs.getString("Agent").trim();
			//	cust.addAgent(new Agent("130"));
				if (ag != null) {

					cust.addAgent(new Agent(ag));
					cust.setMainAgent(ag);
				
					Agent agg = ((Agent) this.aHash.get(ag.trim()));
					if(agg != null){
						cust.setCity("Ag: " +agg.getName());
					}
					if (agg != null && agg.getDubbler().size() > 0) {
						for (int i = 0; i < agg.getDubbler().size(); i++) {
							cust.addAgent(new Agent(
									(String) ((Agent) this.aHash.get(ag.trim()))
											.getDubbler().elementAt(i)));
						}
					}
					
					   if (ag != null && agent1.get(ag.trim()) != null){
							Agent aa = (Agent)agent1.get(ag.trim());
						
							Vector vec = aa.getDubbler();
							if(vec!= null && vec.size() >0){
								Enumeration en = vec.elements();
								while(en.hasMoreElements()){
									String age = (String) en.nextElement();
									cust.addAgent(new Agent(age));
								}
							}
						}
						


				}

				
				if(taara.get(cust.getID()) != null){
					String saldo = Converter.enCodeEst("Kliento d��i� skola: '" + (String)taara.get(cust.getID()));
					//System.out.println(saldo);
					if(cust.getCity() != null && cust.getCity().trim().length() > 3){
						cust.setCity(cust.getCity() + "|" + saldo);
					}else{
						cust.setCity(saldo);
					}
				}
				
				cust.setPayer(rs.getString("Payer").trim());


				String blokk = rs.getString("Block2");

				if (!blokk.trim().equals("0")) {
					cust.setSalesBlock(true);
				}
				String kett = rs.getString("Chain").trim();
				
				String ketinimi = rs.getString("ChainName");
				
				if (ketinimi != null && ketinimi.trim().length() > 0) {
					ketinimi = ketinimi.trim();
					cust.setAnkeediTyyp(ketinimi);
					cust.setChain(ketinimi);
				/*	if (!ketinimi.equals("OTHERS")
							&& !ketinimi.equals("ETK")) {
						String ke = ketinimi.substring(0, 2);
						if(ketinimi.length()>2){
							ke =  ketinimi.substring(0, 3);
						}
						name = ke + " " + name;
						cust.setName(name);
					}

					if (ketinimi.equals("ETK")) {
						name = maksjanimi + " " + name;
						cust.setName(name);
					}*/
					
					if(ketinimi.equals("IKI") || ketinimi.equals("NORFA") || ketinimi.equals("MAXIMA") || ketinimi.equals("RIMI") ){
						cust.setNodebt(true);
					}

				}

				String kett2 = rs.getString("Chain2");
				if (kett2 != null && kett2.trim().length() > 0) {
					//cust.setGroup(kett2.trim());
				}

			

				String jelp = rs.getString("Replacement");
				if (jelp != null && jelp.trim().length() > 0) {
					cust.addAgent(new Agent(jelp.trim()));
					cust.setMainAgent(jelp.trim());
				}


				String paymterm = rs.getString("PaymentTerms");
				if (paymterm != null && paymterm.trim().length() > 0) {
					if(paymterm.trim().length() == 3){
						cust.setPaymTerm(paymterm.substring(1));
					}else{
						cust.setPaymTerm(paymterm);
					}
					
				}
				// ja kui on vaja kuvada siis paneme peale all olev on
				// ajam��rang

				if(!cust.isNodebt() && debts.get(cust.getID()) != null  ){
					cust.setDebt( Double.parseDouble((String)debts.get(cust.getID())));
				}else if(!cust.isNodebt() && debts.get(cust.getPayer()) != null  ){
					cust.setDebt( Double.parseDouble((String)debts.get(cust.getPayer())));
				}
				
				if(cust.hasAgents()){
					cust.setCountry("Lithuania");
				    customers.put(name + " " +aadress1, cust);
					custMap.put(cust.getID(), cust);
				}
				
				
			}


			Customer cc = new Customer("YYYYY");
			cc.setName("Unknown Customer");
			cc.setSalesBlockT(true);

			Enumeration enums = getParent().getAgentsHash().elements();
			while (enums.hasMoreElements()) {
				Agent a = (Agent) enums.nextElement();
				cc.addAgent(a);
			}

			customers.put("Unknown Customer", cc);
			custMap.put(cc.getID(), cc);


		} catch (SQLException e) {
			getParent().showError(e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   
		return customers;
	}
	
	


	private Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(url, getParent().getProps()
				.getProperty("dsn.user"),
				getParent().getProps().getProperty("dsn.pass"));
	}

	public TreeMap getProdGroups() throws SQLException {

		Connection con = getNewConnection();
		TreeMap tm = new TreeMap();
		String sql = "ProductGroups_proc";
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);

		while (rs.next()) {
			tm.put(rs.getString("ProductGroupID").trim(), Converter.enCodeEst(rs.getString("ProductGroup").trim()));
		}
		rs.close();
		st.close();
		con.close();
		return tm;
	}
	
	public TreeMap getProdGroupsSecond() throws SQLException {
		TreeMap tm = new TreeMap();
		return tm;
	}

	public TreeMap getProducts(Agent agent) {

	

		// Leiame soodused

		String cmd = "select * from ProductData";

		if (isDebug())
			System.out.println(cmd);
		TreeMap products = new TreeMap();
		try {
			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);

			while (rs.next()) {

				String kood = rs.getString("ProductID").trim();
				Product prod = new Product(kood);
				String nimetus = rs.getString("ProductName").trim();
				try {
					if (nimetus.indexOf("(") != -1) {
						nimetus = nimetus.replace('(', '<');
					}
				} catch (Exception ew) {

				}
				try {
					if (nimetus.indexOf(")") != -1) {
						nimetus = nimetus.replace(')', '>');
					}
				} catch (Exception ew) {

				}

				prod.setInCases((rs.getDouble("AltUnitQty")));
				prod.setPrice((rs.getDouble("Price")));

				String grupp = rs.getString("ProductGroup");
				if (grupp != null && grupp.trim().length() > 0) {
					prod.setGroup(grupp.trim());
				}

				String into = rs.getString("InUnit");
				String lisa = "";
				if (into == null || into.trim().length() == 0) {
					//prod.setInUnit(prod.getInCases());

				} else if (into.trim().equals("1") || into.trim().equals("4")) {
					//prod.setInUnit(prod.getInCases() / 2);
					lisa = " " + (int) prod.getInCases() + "/"
							+ (int) prod.getInCases() / 2;
				} else if (into.trim().equals("3") || into.trim().equals("5")) {
					//prod.setInUnit(prod.getInCases() / 4);
					lisa = " " + (int) prod.getInCases() + "/"
							+ (int) prod.getInCases() / 2;
				}

				prod.setName(Converter.enCodeEst( prod.getGroup() + "-" + prod.getID() + " " + nimetus + lisa));
			  prod.restName =  new String((nimetus + lisa).getBytes("Windows-1252"), "Windows-1257");
			 // System.out.println("aaaa " + prod.restName);
				prod.setVat("21");
   
				String ean = rs.getString("EAN");
				if (ean != null && ean.trim().length() > 7) {
					prod.setEAN(ean.trim());
					lisa = lisa + ean.trim().substring(ean.trim().length()-3);
				}

				String unit = rs.getString("BasicUnit");

				if (unit != null && unit.trim().length() > 0) {
					prod.setUnit(unit.trim());
				}

		

				products.put(prod.getGroup() + " " + nimetus + lisa, prod);
			
			}
			rs.close();
			st.close();
			con.close();

		} catch (Exception e) {
			getParent().showError(e);
		}

		return products;
	}

	public Vector getCustomerProductsList(String agentCode) {
		return custProds;

	}

	public void setBulkSales(Map agents) {

		/*String cmd = "select * from SalesRepSales";

		if (isDebug())
			System.out.println(cmd);
		try {
			Connection con = getNewConnection();
			Statement stmt = con.createStatement();
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_MONTH, 1);
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String agentID = rs.getString("SalesRep").trim();
				
				Agent agent = (Agent) agents.get(agentID);
				if (agent != null) {
					agent.setMonthSales((int) rs.getDouble("SalesAmount"));
					agent.setMonthSalesVol((int) rs.getDouble("SalesKG"));
					System.out.println("agent" + agent.getName() + " "
							+ agent.getMonthSales() + " "
							+ agent.getMonthSalesVol());
				}
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			getParent().showError(e);
		}*/

	}

	public double getSalary(String agentCode) {
		setSales(0);
		return 0;
	}

	public DealerProduct getDealerProductsList() {

		/*
		 * 
		 * DealerProduct dealerProds = new DealerProduct(); try {
		 * 
		 * 
		 * String sql =
		 * " SELECT  MBITNO AS toode,MBSTQT-MBREQT AS STOCK , MBWHLO AS LADU " +
		 * "from mvxjdta.MITBAL l WHERE MBWHLO='100' OR MBWHLO='205' OR MBWHLO='215' ORDER BY MBITNO"
		 * ;
		 * 
		 * if (isDebug()) System.out.println(sql); Connection con1 =
		 * getNewConnection(); Statement stm = con1.createStatement(); ResultSet
		 * rst = stm.executeQuery(sql);
		 * 
		 * while (rst.next()) { String toode = rst.getString("toode").trim();
		 * int seis =
		 * (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
		 * String ladu = rst.getString("ladu").trim(); dealerProds.add(ladu,
		 * toode,seis+""," "," " );
		 * //System.out.println(ladu+" "+toode+" "+seis+""); } rst.close();
		 * stm.close(); con1.close();
		 * 
		 * } catch (Exception e) { getParent().showError(e); }
		 * 
		 * return dealerProds;
		 */
		return null;
	}

	public Vector getDebt(String agentCode) throws SQLException {
		Vector list = new Vector();
		String sql = "DebtList_proc";

		if (isDebug())
			System.out.println(sql);
		Connection con = getNewConnection();

		stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		Calendar cal = Calendar.getInstance();
		Date now = new Date(cal.getTimeInMillis());

		while (rs.next()) {
			Debt debt = new Debt();
			debt.setPayerID(rs.getString("PayerID").trim());
			
			debt.setDoknr(rs.getString("InvoiceNumber").trim());
			Date duedate = getDate(rs.getString("DueDate").trim());
			debt.setDuedate(duedate);

			debt.setSum(rs.getDouble("InvoicedAmount"));
			debt.setDue(rs.getString("DaysOverdue"));
			
			
			debt.setCustID(rs.getString("ShopID").trim());
			Date date = getDate(rs.getString("InvoiceDate").trim());
			debt.setDate(date);
			
			
			// lisada 5 p�eva
			
			//if (duedate.getTime() < (now.getTime()) + 432000000) {
				list.addElement(debt);
			//}
			
		}
		rs.close();
		stmt.close();
		con.close();
		return list;
	}

	public ManagerData getManagerData() throws SQLException {
		ManagerData md = new ManagerData();
		return md;
	}

	public double getFoodSales(String agentCode) throws SQLException {
		return 0;
	}

	public double getAlcoSales(String agentCode) throws SQLException {
		return 0;
	}

	public double getPromoSales(String agentCode, int nr) throws SQLException {
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
		return "LeiburLIT";
	}

	public String getSubjectFilesTo() {
		return this.subjectFilesTo;
	}

	public String getDaySalesData(Agent agent) {
		String sql = "SELECT t.MMspe2 AS grupp,SUM(UCSAAM) AS summa "
				+ "FROM mvxjdta.OSBSTD d ,mvxjdta.MITMAS t WHERE t.MMITNO=d.UCITNO "
				+ "AND substr(UCIVDT,0,6) = '"
				+ getYearMonth()
				+ "' "
				+ "AND UCSMCD='"
				+ agent.getID()
				+ "' AND UCITTY = '01' AND M9FACI=200 GROUP BY t.MMspe2 ORDER BY t.MMspe2 ";
		System.out.println(sql);
		StringBuffer sb = new StringBuffer();

		sb.append("\n " + getParent().getString("string5") + "\n\t"
				+ getParent().getString("string6") + "\n\n");
		sb.append(getParent().getString("string4") + "\n");
		NumberFormat nn = getNF();
		nn.setGroupingUsed(false);
		try {
			Connection con = getNewConnection();
			stmt = con.createStatement();
			ResultSet rs2 = stmt.executeQuery(sql);
			while (rs2.next()) {
				sb.append(" " + rs2.getString("grupp") + "\n                 "
						+ nn.format(rs2.getDouble("summa"))
						+ " EUR\n-------------------------------------\n");
			}
			con.close();
			sb.append("\n\n" + getParent().getString("string3") + "\n\n");
			sb.append(getParent().getString("string2") + "\n\n");
		} catch (Exception E) {
		}

		return sb.toString();
	}

	public String getPilotDayData(Agent agent) {

		String sql = "SELECT t.MMspe2 AS grupp,SUM(UCSAAM) AS summa "
				+ "FROM mvxjdta.OSBSTD d ,mvxjdta.MITMAS t WHERE t.MMITNO=d.UCITNO "
				+ "AND substr(UCIVDT,0,6) = '" + getYearMonth() + "' "
				+ "AND UCSMCD='" + agent.getID()
				+ "' AND UCITTY = '01'  GROUP BY t.MMspe2 ORDER BY t.MMspe2 ";

		String sql2 = "SELECT t.MMITDS As nimetus ,dxdisp AS ale ,dxlimt AS tase FROM  mvxjdta.ogdmtx ah, mvxjdta.MITMAS t WHERE  t.MMITNO=ah.dxobv1 "
				+ "AND substr(dxfvdt,0,6) = '"
				+ getYearMonth()
				+ "' AND dxdipo=1 AND dxdisy=100 "
				+ "    AND dxdisp>0 GROUP BY t.MMITDS,dxdisp,dxlimt ";

		String sql3 = " SELECT d8obv1,t.MMITDS AS nimetus,d8disp AS ale,d8limt AS tase from mvxjdta.ogcmtx, mvxjdta.MITMAS t "
				+ "WHERE d8disy = 100 AND d8dipo=1 AND d8cono=200 AND  d8obv1=mmitno AND substr(d8fvd1,0,6) = '"
				+ getYearMonth() + "' ";

		String sql4 = "SELECT ucivno as nr,okcunm AS nimi FROM mvxjdta.OSBSTD d ,mvxjdta.OCUSMA k "
				+ " WHERE uccuno=OKCUNO AND to_char(to_date(to_char(d.UCIVDT),'YYYYMMDD')) = (to_char(sysdate)) "
				+ " AND UCSMCD='"
				+ agent.getID()
				+ "' AND UCITTY = '01' GROUP BY ucivno,okcunm ORDER BY ucivno,okcunm";

		System.out.println(sql);
		StringBuffer sb = new StringBuffer();
		try {
			sb.append("\n " + getParent().getString("todaysbills") + "\n\n");

			Connection con4 = getNewConnection();
			stmt = con4.createStatement();
			ResultSet rs5 = stmt.executeQuery(sql4);
			while (rs5.next()) {
				sb.append(rs5.getString("nr") + " " + rs5.getString("nimi")
						+ "\n\n");
			}
			rs5.close();
			con4.close();

			sb.append("\n\n " + getParent().getString("string5") + "\n\t"
					+ getParent().getString("string6") + "\n\n");
			sb.append(getParent().getString("string4") + "\n");
			NumberFormat nn = getNF();
			nn.setGroupingUsed(false);

			Connection con = getNewConnection();
			stmt = con.createStatement();
			ResultSet rs2 = stmt.executeQuery(sql);
			while (rs2.next()) {

				sb.append(" " + rs2.getString("grupp") + "\n                 "
						+ nn.format(rs2.getDouble("summa")) +

						" EUR.\n-------------------------------------\n");
			}
			//System.out.println(sb.toString());
			con.close();
			sb.append("\n\n" + getParent().getString("string3") + "\n\n");
			sb.append(getParent().getString("string2") + "\n\n");
			Connection con2 = getNewConnection();
			String nimetus = null;
			String ale = null;
			String kogus = null;
			Statement st = con2.createStatement();
			ResultSet rs3 = st.executeQuery(sql2);
			System.out.println(sql2);
			while (rs3.next()) {
				nimetus = rs3.getString("nimetus").trim();
				ale = rs3.getString("ale");
				kogus = rs3.getString("tase").trim();
				sb.append(nimetus + "\n");
				sb.append("\t\t" + ale + " %\t" + kogus + "\n\n");
			}

			Connection con3 = getNewConnection();
			Statement stv = con3.createStatement();
			ResultSet rs4 = st.executeQuery(sql3);
			System.out.println(sql3);
			while (rs4.next()) {
				nimetus = rs4.getString("nimetus").trim();
				ale = rs4.getString("ale");
				kogus = rs4.getString("tase").trim();
				sb.append(nimetus + "\n");
				sb.append("\t\t" + ale + " %\t" + kogus + "\n\n");
			}
			con2.close();
			st.close();
			con3.close();
			stv.close();
		} catch (Exception e) {
			getParent().showError(e);
		}

		return sb.toString();
	}

	public void insertDefective(DefectiveWrapper wr) {
		String sql = "INSERT INTO tartu.MJ_QUALITY VALUES (?,?,?,?,?,?,?,?,?,?,sysdate,?,' ',' ',0,0)";
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
			//	System.out.println(kogus + "bb '"
				//		+ lw.getLine().getBestBefore() + "'" + bb + "'");
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

	public void insertDistribution(DistEvalListWrapper wr) {
		String sql = "INSERT into tartu.mj_distribution values (" + "?," // agent
																			// C(50),
				+ "?," // agentname C(200),
				+ "?," // clientid C(50),
				+ "?," // clientname C(200),
				+ "?," // prodid C(50),
				+ "?," // prodname C(200),
				+ "date() ," // sentdate D,
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
				String time = setZero(lw.getLine().getDate().getHours()) + "."
						+ setZero(lw.getLine().getDate().getMinutes()) + "."
						+ setZero(lw.getLine().getDate().getSeconds());
				ps.setString(44, time);// + "?," // colldtime C(12),
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


	private String setZero(int i) {
		String x = i + "";
		if (x.length() == 1) {
			x = "0" + x;
		}
		return x;
	}

	private Date getDate(String kuup) {

		Calendar cal = Calendar.getInstance();
		int YEAR = Integer.parseInt(kuup.substring(0, 4));
		int MONTH = Integer.parseInt(kuup.substring(4, 6))-1;
		int DAY = Integer.parseInt(kuup.substring(6, 8));
		cal.set(YEAR, MONTH, DAY, 1, 2, 3);
		Date now = new Date(cal.getTimeInMillis());
		return now;
	}

	public String getStartingDateStr() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH,
				-(Integer.parseInt(parent.getParentFrame().getProps()
						.getProperty("months")) + 1));
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

	public String get30AgoStr() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -30);
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

	private String getYearMonth() {
		Calendar cal = Calendar.getInstance();
		String month = (cal.get(Calendar.MONTH) + 1) + "";
		if (month.length() == 1) {
			month = "0" + month;
		}
		return cal.get(Calendar.YEAR) + "" + month;
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

	// private String url = "jdbc:oracle:thin:@10.10.10.55:1521:MVXEE001";
	private String url = "jdbc:odbc:mj_vd";
	private Connection con;
	private Vector custProds = null;
	private Statement stmt;
	private String subjectFilesTo = null;
	private TreeMap agents1 = new TreeMap();
	private NumberFormat NF = NumberFormat.getInstance();
	private TreeMap dealers = new TreeMap();
	HashMap aHash = new HashMap();

	public Vector getPriceMatrixData() {

		String cmd = "select * from ProductData";

		if (isDebug())
			System.out.println(cmd);
		TreeMap products = new TreeMap();
		try {

			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);

			while (rs.next()) {

				String kood = rs.getString("ProductID").trim();
				String hind = rs.getDouble("Price") + "";

				products.put(kood, hind);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Vector lines = new Vector();
		String sql = "CustPromotions_proc";
		Vector check = new Vector();

		try {
			con = getNewConnection();

			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				//PayerID    ShopID     ProductID       StartDate  EndDate    Price       Discount 
				String clGroup = rs.getString("PayerID");
				String custID = rs.getString("ShopID");
				String prodID = rs.getString("ProductID");
				String startDate = rs.getString("StartDate").substring(0, 10);
				String endDate = rs.getString("EndDate").substring(0, 10);
				double price = rs.getDouble("Price");
				double discount = rs.getDouble("Discount");
				

				if (products.get(prodID.trim()) != null) {
					try {

						if (price == 0) {
							price = Double.parseDouble((String) products
									.get(prodID.trim()))
									* (100 - discount)
									/ 100;
							discount = 0;
						} else {
						
						}

						// System.out.println("arvutan hinda " + prodID.trim() +
						// " " + price + " " +
						// (String)products.get(prodID.trim()));
					} catch (Exception a) {
						a.printStackTrace();
					}
				}

				MatrixLine mline = new MatrixLine();

				if (custID != null
						&& custID.trim().length() > 0) {
//					mline.setCustID(custID.trim());
				}
				
				if (mline.getCustID() == null && clGroup != null
						&& clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
				}

				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}

				if (endDate != null && endDate.trim().length() > 0) {
					mline.setEndDate(endDate);
				}
				if (startDate != null && startDate.trim().length() > 0) {
					mline.setStartDate(startDate);
				}

			//	mline.setDiscount(discount);

				if (price != 0) {
					mline.setPrice(price);
				}
				if (!check.contains(clGroup.trim() + prodID.trim()) && clGroup.trim().length() > 0) {
					lines.add(mline);
				}

				check.addElement(clGroup.trim() + prodID.trim());
			}

			rs.close();
			this.connectionDown();
		} catch (SQLException ex) {
		}
		return lines;
	}

	  public Vector getFixedAssortmentData() {
			Vector result = new Vector();
			/*File f = new File(this.getExcelDir() + "AssortmentVD.xls");
			
			
			
			try {
				
				
				FileInputStream ff = new FileInputStream(f);
				HSSFWorkbook workbook = new HSSFWorkbook((ff));
				HSSFSheet sheet = workbook.getSheetAt(0);
				HSSFRow row = null;
				TreeMap groups = new TreeMap();
				row = sheet.getRow(3);
				// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

				// System.out.println("rownum " + row.getLastCellNum());
				for (int a = 2; a < row.getLastCellNum(); a++) {
					try {
						// System.out.println(row.getCell((short)a).getStringCellValue());
						groups.put(a + "", row.getCell((short) a)
								.getStringCellValue());
					} catch (Exception e) {
						// System.out.println(row.getCell((short)a).getNumericCellValue());
					}
				}

			//	 System.out.println(groups.toString());

				for (int i = 5; i < sheet.getLastRowNum(); i++) {
					row = sheet.getRow(i);
					String prodID = "";

					if (row.getCell((short) 0) != null) {

						try {
							prodID = (int) (row.getCell((short) 0)
									.getNumericCellValue()) + "";
						} catch (Exception e) {
							prodID = (row.getCell((short) 0).getStringCellValue());
						}
						
	                      if(!prodID.equals("0")){
	                   	
						// Siin l�heme reasisse rokkima ja vaatama kas se toode
						// l�heb kaasa
						for (int a = 2; a < row.getLastCellNum(); a++) {

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

								if (go != null && go.equals("1")) {

									 System.out.println( a + " " + prodID + " " +
									 clGroup + " " + go);

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

			// System.exit(0);
*/
			return result;

		}

	
	
	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}
}
