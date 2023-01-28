package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
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

public class VichiLATSQLDBDriver extends AbstractDBDriver implements
		BulkUpdater, Discounter, Manager {

	public VichiLATSQLDBDriver(EksportPanel parent) {
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
		 makePictureCat();
		  System.exit(0);
		  return null;
	}

	
	
	private void makeConvertedCat(){
		String picts = parent.getProps().getProperty("pict.dir");
     	String picts2 = parent.getProps().getProperty("pict.dir2");
     	String pictsUnion = parent.getProps().getProperty("pict.dir3");
     	
   
   	 
     	
     	String cmd2 = "SELECT t.[No_] AS toode  , (select top 1 ref.[Item No_] from Navision.dbo.[Vičiūnai ir Ko$Item Cross Reference] ref " +   
 	   "    WHERE  ref.[Cross-Reference Type No_] = 'LAT'  and ref.[Cross-Reference No_]  = t.[No_] ) as littoode " +
  "FROM [Vičiūnai-LAT$Item] t WHERE ([Blocked]=0 AND [Unit Price]>0 AND len([Grupės kodas])>0 and  len([Search Description]) != 0) " +
 	"				  or [Pogrupio kodas] like '%2.8.1%' " ;
 			
 		   	try {
 		   	    Connection con = getNewConnection2();
 				Statement stmt = con.createStatement();
 		   	    ResultSet	      rs = stmt.executeQuery(cmd2);
 		        TreeMap conver = new TreeMap();
 		        while (rs.next()) {
 		   	      String toode = rs.getString("toode").trim();  
 		          String littoode = rs.getString("littoode");
 		             toode = toode.replace('/', '_');
		             toode = toode.replace('/', '_');
		   	     if(littoode != null && littoode.trim().length()>0){
 		        	  littoode = littoode.replace('/', '_');
 				      littoode = littoode.replace('/', '_');
 				     
         	         	File in = new File ( picts + littoode+ ".jpg"); 
         	         	if(in.exists()){
         	         	File inpda = new File ( pictsUnion + toode +".jpg" ); 
         	         	if(!inpda.exists()) {
         	         		Converter.shrinkAndCopyFile(in,inpda);
         	         	}
 		             	
 		             	}
 		             }
		   	     
		   	     
		   	  File in = new File ( picts2 + toode+ ".jpg"); 
	         	if(in.exists()){
	         	File inpda = new File ( pictsUnion + toode +".jpg" );
	         	if(!inpda.exists()) {
	         		Converter.shrinkAndCopyFile(in,inpda);	
	         	}
            	
            	}
 		        }
 		   	    rs.close();
 		   	    stmt.close();
 		   	    con.close();
          	}catch(Exception r){
 		   		r.printStackTrace();
 		   	}
     	
	}

	
	
	 private void makePictureCat(){

			String cmd2 = "SELECT t.[No_] AS toode  , (select top 1 ref.[Item No_] from Navision.dbo.[Vičiūnai ir Ko$Item Cross Reference] ref " +   
	   "    WHERE  ref.[Cross-Reference Type No_] = 'LAT'  and ref.[Item No_] = t.[No_] ) as littoode " +
 "FROM [Vičiūnai-LAT$Item] t WHERE ([Blocked]=0 AND [Unit Price]>0 AND len([Grupės kodas])>0 and  len([Search Description]) != 0) " +
	"				  or [Pogrupio kodas] like '%2.8.1%' " ;
			
			makeConvertedCat();
		     	String pictsUnion = parent.getProps().getProperty("pict.dir3");
            	String pictspda = parent.getProps().getProperty("pictpda.dir");

		   	try {
		 	   Connection con = getNewConnection2();
			
		   		Statement stmt = con.createStatement();
		   	    ResultSet	      rs = stmt.executeQuery(cmd2);


		        StringBuffer buf = new StringBuffer();
		        StringBuffer buf0 = new StringBuffer();
		        int a = 0;
		        int b = 0;
		        int c = 0;
		        
		        
		        while (rs.next()) {
		   	    	a++;
		   	    	c++;
		   	    	
		   	     String toode = rs.getString("toode").trim();  
		       
		   	     toode = toode.replace('/', '_');
		         toode = toode.replace('/', '_');
		   	    	if(c == 100){
		   	    		  c = 0;
		   	    		  System.out.println("Piltide leidmine "  + a);	
		   	    		  
		   	    	}
		   	     
		              
		                 File f = new File(pictsUnion + toode+".jpg");
		                 String len = "0";
		                 if(f.exists()){
		                   len = f.length() + "";
		                 } 
		                 
		                 if(len.equals("0")){
		                 	//System.out.println(toode+".jpg;"  + len);
		                 	buf0.append(toode+".jpg\n");
		                 }else{
		                 	buf.append(toode+".jpg;"  + len + "\n");
		           	    	b++;
		                 }
		                 }
		   	    rs.close();
		   	    stmt.close();
		   	    con.close();
		   	
		   	    //oleasoleva kataloogi kustutamine
		   	  System.out.println("Kataloogi kustutamine "  + a);	    
		               File f = new File("list.txt");
		               if (f.exists())f.delete();
		                 try {
		           		PrintWriter os = new PrintWriter(new FileWriter(
		           				  "list.txt", true));
		           		os.write(buf.toString());
		           		os.close();
		           	  }catch (Exception ex) {
		           		ex.printStackTrace();
		           	  }
		           	  File f0 = new File("list0.txt");
		           	   if (f0.exists())f0.delete();
		                   try {
		             		PrintWriter os1 = new PrintWriter(new FileWriter(
		             				  "list0.txt", true));
		             		os1.write(buf0.toString());
		             		os1.close();
		             	  }catch (Exception ex) {
		             		ex.printStackTrace();
		             	  }

		   //Kataloogi sisude kustutamine
		   
		        	 File res2 = new File (pictspda);
		            String[] ff2 = res2.list();
		        	 for(int i = 0;ff2.length>i;i++){
		            	(new File ( pictspda + ff2[i])).delete(); 
		         }
	 
		          System.out.println("Piltide kopeerimine ");	
		          //Failide kopeerimine  
		            String[] gg = new File(pictsUnion).list();
		         	  //System.out.println("gg " + gg[0] + " " + gg.length);
		            
		             for(int i = 0;gg.length>i;i++){          	
		             	File in = new File ( pictsUnion + gg[i]); 
		             	if(in.isFile()){            	
		             	File inpda = new File ( pictspda + toUpper(gg[i])); 
		             	Converter.shrinkAndCopyFile(in,inpda);
		             	}
		             }
		             
		             //piltide kopeerimine alam kataloogist ..
		             
		             
		             File fff =  new File (pictspda +  "list.txt");
		       	  Converter.copyFile(f,fff);  	
		       	  File fff0 =  new File (pictspda +  "list0.txt");
		       	Converter.copyFile(f0,fff0);  	
		   	} catch (Exception e) {
		   	    getParent().showError(e);
		   	}
		   
		}
		 
		   private String toUpper(String asi ){
			   return asi ;
		   }
		   

	
	
 public Vector getPriceMatrixData(){
		  
	
	  	 String sql ="select * from v_LAT_AkcijuKainos";
	  	 
	  	 
	  	 
	       Vector result = new Vector();
	         try {
	          HashSet hset = new HashSet();
	      	   Connection con = getNewConnection2();
	   	        Statement stmt = con.createStatement();
	   	        ResultSet rs = stmt.executeQuery(sql);
	           while (rs.next()) {

	             String custID = Converter.enCodeEst(rs.getString("custid"));
	             String clGroup = rs.getString("clgroup");
	             String prodID = rs.getString("prodid");
	           //  String prodGroup = rs.getString("prodgroup");
	            
	         
	            
	             
	            
	            // double disc =  rs.getDouble("disc");
	            
	             String sdate = rs.getString("startday");
	             String edate = rs.getString("enddate");
	             double price = rs.getDouble("price");
	             
	             MatrixLine mline = new MatrixLine();
	             String check = "";
	             boolean goes = false;

	             if (custID != null && custID.length() > 0) {
	               mline.setCustID(custID);
	               check = custID;
	               goes = true;
	             }
	             
	             if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
	               mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
	               check = check + clGroup.trim();
	               goes = true;
	             }
	             
	             
	             
	            if( sdate != null  && !sdate.startsWith("2030") && !sdate.startsWith("1753") && edate != null && (!edate.startsWith("2030") && !edate.startsWith("1753"))  ){
	            	//kui on mõlemad siis paneme kuupäevad 
	            		 mline.setStartDate(sdate.substring(0,10));
		            	 mline.setEndDate(edate.substring(0,10));
		             	
	            }
	            
	             if (prodID != null && prodID.length() > 0) {
	               mline.setProdID(prodID);
	               check = check + prodID.trim();
	             }
	            
	           
	             if (price != 0) {
	               mline.setPrice(price);

	             }
	             
	             
	             if(mline.getPrice() > 0 || mline.getDiscount() > 0){
	            	 if(!hset.contains(check)){
	            		 if(goes) {
	            			 result.add(mline);
		            	      hset.add(check); 
	            		 }
	            	     
	            	 }
	              }
	           }
	           rs.close();
	           stmt.close();
	           con.close();
	         }catch(Exception e){
	        	 e.printStackTrace();
	         }
		System.out.println("Pricematrix Size "  + result.size());
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
				ps.setString(2, Converter.deCode(wr.getInfo().getAgentName()));
				ps.setString(3, lw.getLine().getCustID());
				ps.setString(4, Converter.deCode(lw.getLine().getCustName()));
				ps.setString(5, lw.getLine().getProdID());
				ps.setString(6,Converter.deCode(lw.getLine().getProdName()));
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
		
	}

	/*
	 * private String setZero(int i){ String x = i +""; if (x.length() == 1){ x
	 * = "0" + x; } return x; }
	 */
	public Vector getFixedAssortmentData() {
		Vector lines = new Vector();

		try {
		
			String cmd = " SELECT grupp ,toode from v_LAT_Asortimentas where len(grupp) > 1 order by grupp,description ";
	
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
		return null;
			}

	
	private TreeMap <String,Double> prods = null;
	
	   public String getSaveStockInfo() {
			StringBuffer products = new StringBuffer();

			prods = new TreeMap <String,Double> ();
			try {
				
				
				
				
				String  cmd = " Select kood , sum(kogus) as kogus from lat_litstock  group by kood ";
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
					prods.put(toode, seis);
				}

				rs.close();
				stmt.close();
				con.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

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
		String cmd = " SELECT  [Kodas] AS kood,[Aprašas] AS nimi  FROM [Vičiūnai-LAT$Prekių grupavimas] order by [Kodas]";
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
		String cmd = "SELECT  [Kodas] AS kood,[Aprašas] AS nimi  FROM [Vičiūnai-LAT$Prekių grupavimas] order by [Kodas]";
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
				
				if (cleardots(groupID).equals("1124")) {
					groupID = "1.c.4.";
				}
				if (cleardots(groupID).equals("1125")) {
					groupID = "1.c.5.";
				}
				if (cleardots(groupID).equals("1126")) {
					groupID = "1.c.6.";
				}
				if (cleardots(groupID).equals("1127")) {
					groupID = "1.c.7.";
				}
				if (cleardots(groupID).equals("1128")) {
					groupID = "1.c.8.";
				}
				if (cleardots(groupID).equals("1129")) {
					groupID = "1.c.9.";
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
	
	

    public TreeMap getAgents1() {
		return agents1;
	}
    private TreeMap agents1 = new TreeMap();

	public TreeMap getAgents() {

		String cmd = "SELECT [Code] AS kood,[Name] AS nimi,[Location Code] AS objekt ,left ([Location Code for MobileJava],3) AS objekt2 FROM "
				+ "[Vičiūnai-LAT$Salesperson_Purchaser] WHERE len([Location Code])>0";
		if (isDebug())
			System.out.println(cmd);
		TreeMap agents = new TreeMap();
		 agent1 = new TreeMap();
		   StringBuffer sb2 = new StringBuffer();
			
		try {

			   FileReader fr = new FileReader("vacation.txt");
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
			while (rs.next()) {
				Agent agent = new Agent(rs.getString("kood").trim());
				String name = rs.getString("nimi").trim();
				agent.setObject(rs.getString("objekt"));
				// agent.setObject("KAU");
				// agent.setObjektmain(rs.getString("objekt2"));
				agent.setName(name);
				if(name.trim().length() > 0){
	            	  sb2.append(agent.getID() + ";" + Converter.enCodeEst( name) + "\n");  	
	            }
				
				
				 agent.setLanguage("LAT");
		          agent.setCountry("lv");
		          agent.setPassWord("0000");
		          String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
		          if(email.length() > 1){
		        	  agent.setEmail(email);
		        	  agent.setActive(true);
		          }
		         
		          
		          
		  		
				
				
				agent.setIsPilot(true);
				
				 if(asendajad.get(agent.getID()) != null){
		              	Vector vv = (Vector)asendajad.get(agent.getID());
		              	Enumeration aa = vv.elements();
		              	while(aa.hasMoreElements()){
		              		agent.addDubbler((String)aa.nextElement());
		              		System.out.println(agent.getDubbler().toString());
		              	}
		              }
				
				agents.put(name, agent);
				  agents1.put(agent.getID(), agent);
				  agent1.put(agent.getID(), agent);

			}

			Agent aa = new Agent("HOUSE1");
			aa.setName("office1");
			// aa.setObject("KAU");
			agents.put("office1", aa);

			  if(sb2.length() > 2){
				  	try{
				  		 Converter.saveFileInUTF8( "agent.txt", Converter.enCodeEst(sb2.toString()));
				  	}catch (Exception e){	    		
				  	}	      
				   }
					


			
			
			cmd = "SELECT k.[No_] AS klient, [Balance total (LCY)]AS summa "
					+ " FROM [Vičiūnai-LAT$Customer] k  WHERE k.[Salesperson Code] != '' AND [Balance total (LCY)]<>0 ";
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

	private TreeMap agent1 = new TreeMap();
	public TreeMap getCustomers(Agent agent) {
		
		
		try{
			   Converter.getManagersReplacements(agent1, rcompany, user, pass); 
				
		}catch(Exception e){
			
		}
	 	TreeMap freeze = new TreeMap();
		try {

			String cmd = " select a.[Sell-to Customer No_] AS klient, a.[Customer No_] AS maksja,a.[External Document No_] AS number, a.[Due Date] AS kuupaev, sum(b.[Amount]) AS summa, "
					+ " DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payer  FROM [Vičiūnai-LAT$Cust_ Ledger Entry] a inner join "
					+ " [Vičiūnai-LAT$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] "
					+ " where a.[Document type] = 2 AND  a.[Posting Date] > getdate()-200 AND a.[Open]= 1 "
					+ // a.[Due Date] < GETDATE()+7 AND
					" group by a.[Sell-to Customer No_],a.[Customer No_] ,a.[External Document No_], "
					+ " a.[Due Date] , DATEDIFF(dd,a.[Due Date],GETDATE()) ,a.[Customer No_] having sum(b.[Amount])<> 0 ";

			try{
				String ccc = "select  [Freezer Customer] as cust, No_ + ' ' +Description   " +
				" as last ,  [Last Date Modified] as dd from [Vičiūnai-LAT$Fixed Asset]" ;

	Connection cn = getNewConnection2();
Statement stcn = cn.createStatement();
ResultSet rscn = stcn.executeQuery(ccc);
while (rscn.next()) {
	String cid = rscn.getString("cust").trim();
	String name = rscn.getString("last");
	String dd = rscn.getString("dd").substring(0,10);
	name = name.trim() + " " + dd;
	if(freeze.get(cid) != null){
		Vector vv = (Vector)freeze.get(cid);
		vv.addElement(name);
		freeze.put(cid, vv);
	}else{
		Vector vv = new Vector();
		vv.addElement(name);
		freeze.put(cid, vv);
	}
}
rscn.close();
stcn.close();
cn.close();

		
			}catch(Exception r ){
				
			}

			System.out.println(cmd);
			this.debtList = new Vector();
			Connection con = getNewConnection2();
			Statement stmt1 = con.createStatement();
			ResultSet rs1 = stmt1.executeQuery(cmd);
			while (rs1.next()) {
				Debt debt = new Debt();
				String custID = rs1.getString("klient");
				// String custID = rs1.getString("maksja");
				debt.setCustID(custID);

				String doknr = rs1.getString("number");
				if (doknr.trim().length() > 6) {
					doknr = doknr.substring(6, doknr.length());
				}
				debt.setDoknr(doknr);
				debt.setDuedate(rs1.getDate("kuupaev"));
				debt.setSum(rs1.getDouble("summa"));
				int dueint = rs1.getInt("due");
				debt.setDue(dueint + "");
				int old = 0;
				if (debtDays.get(custID) != null) {
					old = Integer.parseInt((String) debtDays.get(custID));
				}
				if (old < dueint) {
					this.debtDays.put(custID, dueint + "");
				}
				this.debtList.add(debt);
			}

			rs1.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// / KLIENDI ANDMETE LEIDMINE

		TreeMap tm = new TreeMap();
		this.customers = new ArrayList();

		String sql = " SELECT k.[Name] AS nimi,k.[No_] AS kood,k.[Bill-to Customer No_] AS payer,k.[Address 2] AS aadress ,k.[Salesperson Code] AS agent , "
				+ " [Blokuoti SF išrašymą] AS blokk, "
				+ " [Payment Terms Code] AS payment,[Line Discount %] AS alekood, [Balance total (LCY)]AS yle,[RouteCode] AS visit,[Customer type] AS type, "
				+ " [Blokavimas po pradelstų dienų] AS extradays ,k.[Asortimento kodas] AS sort,PDA_debt AS debt , 0 AS minsum ,contact +' ' + [Phone no_] as kontakt "
				+ " ,[Customer Disc_ Group]  as matrix  , k.Tinklas as chain FROM [Vičiūnai-LAT$Customer] k LEFT JOIN [Vičiūnai-LAT$Sales Line Discount] d ON d.[Sales Code] = k.[Customer Disc_ Group]   AND d.[Sales Type]=1 "
				+ " WHERE k.[Salesperson Code] != ''  AND blocked != 3 and [RouteCode]!= ''  ";

		System.out.println(sql);
		try {
			Connection con = getNewConnection2();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
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
					customer.setAddress(Converter.enCodeEst(Converter.adressLink(aadress,"Latvia")));
					customer.setName(Converter.enCodeEst(customer.getName() + " " + aadress));
				}
				
				String ag = rs.getString("agent").trim();
				
				customer.addAgent(new Agent(ag));
	             customer.addAgent(new Agent("HOUSE1"));
	             customer.setMainAgent(ag);
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
	             
	           	customer.addAgent(new Agent(ag));
					customer.addAgent(new Agent("HOUSE1"));
					
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
	           
				//System.out.println(customer.getName() + " " + customer.getAgents());

				if (rs.getString("blokk").equals("1")) {
					customer.setSalesBlock(true);
				}

				String paym = rs.getString("payment");

				if (paym != null && paym.trim().length() > 0) {
					customer.setPaymTerm(paym);
				}

				String num = rs.getString("alekood");
				if (num != null && num.trim().length() > 0) {
					customer.setDiscount(Double.parseDouble((num.trim())));
				}

				String visit = rs.getString("visit");
				if (visit != null && visit.trim().length() > 0) {
					customer.setVisit(visit.trim());
				}
				
				String type = rs.getString("type");
				String asi = "";
				if (type != null && type.trim().length() > 0) {
					//customer.setCustType(type.trim());
					asi = type.trim();
				}

				
				
				String bll = rs.getString("extradays");
				if (bll != null && bll.trim().length() > 0) {

					if (customer.getSalesBlock()) {

						int extra = Integer.parseInt(bll);
						if (debtDays.get(customer.getID()) != null
								&& customer.getPaymTerm().trim().length() > 0
								&& (extra < Integer.parseInt((String) debtDays
										.get(customer.getID())))) {
							customer.setSalesBlock(true);
						} else {
							customer.setSalesBlock(false);
						}
					}
				}
				
				String grupp = rs.getString("sort");
				if (grupp != null && grupp.trim().length() > 0) {
					Vector vv = new Vector();
					vv.add(grupp.trim());
					customer.setFixSortCode(vv);
					customer.setCustType(grupp.trim());				
				}
				
				if(!asi.equals("3") && !asi.equals("4") && !asi.equals("5") && !asi.equals("6") &&  !asi.equals("7") 
						&&  customer.getGroup().trim().length() > 0){
					//customer.setCustType("MINASORT");
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
				String yes = rs.getString("debt");

				if (yes != null && !yes.trim().equals("0")) {
					customer.setNodebt(true);
				}

				double minsum = rs.getDouble("minsum");
				if (minsum > 0) {
					customer.setMinsum(minsum);
				}

				if (!customer.isNodebt()) {

					String sql3 = "  SELECT     No_ AS kood, CASE WHEN k.[Bill-to Customer No_] = '' THEN "
							+ "(SELECT     SUM(de.[Amount (LCY)]) FROM [Vičiūnai-LAT$Cust_ Ledger Entry] c INNER JOIN  "
							+ " [Vičiūnai-LAT$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] "
							+ "  WHERE   c.[Customer No_] = k.No_) WHEN k.[Bill-to Customer No_] <> '' THEN (SELECT   SUM(de.[Amount (LCY)]) "
							+ "  FROM  [Vičiūnai-LAT$Cust_ Ledger Entry] c INNER JOIN "
							+ "   [Vičiūnai-LAT$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] "
							+ "  WHERE  c.[Customer No_] = k.[Bill-to Customer No_] AND [OPEN] = 1 AND c.[Sell-to Customer No_] = k.No_ "
							+ "	 and [Due Date] <getdate()+7    and c.[Posting Date] > getdate()- 100) "
							+ "	 END AS yle FROM [Vičiūnai-LAT$Customer] k "
							+ "	WHERE  (No_ = '"
							+ id
							+ "') AND (Blocked <> 3) AND ([Salesperson Code] <> '') ";

					try {
						Connection con3 = getNewConnection2();
						Statement stmt3 = con3.createStatement();
						ResultSet rs3 = stmt3.executeQuery(sql3);
						while (rs3.next()) {

							double sum = rs3.getDouble("yle");
							if (sum != 0) {
								customer.setDebt(sum);
								// System.out.println("yle " + id+ " " + sum);
							}

						}

						rs3.close();
						stmt3.close();
						con3.close();
					} catch (Exception e) {
						e.printStackTrace();
					}

					// customer.setUsedCredit(Double.parseDouble(used.replaceAll(",",".").replaceAll(" ","")));
				}
				   String cont = rs.getString("kontakt");
		           if(cont != null && cont.trim().length()>0){
		               customer.setPersonalData(Converter.enCodeEst(cont.trim()));   
		           }
		           
		           String pm = rs.getString("matrix");
		           if(pm != null && pm.trim().length()>0){
		               customer.setGroup(pm.trim());   
		           }
		          
		           String chain = rs.getString("chain");
		           if(chain != null && chain.trim().length()>0){
		               customer.setChain(Converter.enCodeEst(chain.trim()));   
		           }
				// customer.setPayer(id);
			
					tm.put(customer.getName(), customer);
					this.customers.add(customer.getID());
			}

			rs.close();
			con.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return tm;
	}

	public TreeMap getProducts(Agent agent) {
		TreeMap tm = new TreeMap();
		getSaveStockInfo();
		TreeMap discT = new TreeMap();

		try {
		

			String cmd7 = " select sd.[Code] as kood ,[Line Discount %] as disc from [Vičiūnai-LAT$Sales Line Discount] sd "
					+ " where getdate() between [Starting Date] and [Ending Date] +1 ";

		Connection	con = getNewConnection2();
		Statement	stmt = con.createStatement();
		ResultSet	rs = stmt.executeQuery(cmd7);
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

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		TreeMap partiid = new TreeMap();
		
	try {

		String cmd7 = " select * from lat_litstock ";
		Connection	con = getNewConnection2();
		Statement	stmt = con.createStatement();
		ResultSet	rs = stmt.executeQuery(cmd7);
		
			while (rs.next()) {
				String toode = rs.getString("kood").trim();
				double seis = rs.getDouble("kogus");
				String part = rs.getString("bestbefore").trim();
				if(partiid.get(toode) == null) {
					partiid.put(toode,"[" +seis + "] " + part + "|");
				}else {
					partiid.put(toode, partiid.get(toode) + "[" +seis + "] " + part + "|");
				}
				
			}

			rs.close();
			con.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		

		try {
			String cmd = "SELECT t.[No_] AS toode,(t.[Search Description]) AS nimi, t.[Unit Price] AS myygihind, t.[Grupės kodas] AS grupp,t.[Pogrupio kodas] AS grupp2 "
					+ "	,[Base Unit of Measure] AS unit,[Brūkšninis kodas] AS ean,[Product Status] AS status ,[Line Discount %] AS maxdiscount, "
					+ "	  t.[Type] AS type, [Qty_ per Unit of Measure] AS kastis,  [Net Weight] AS kaal, [VAT Prod_ Posting Group] AS vat, [Description] AS vatdesc , '' as type"
					+ "	  ,[Order Multiple] as optimum, [Minimum Order Quantity] as minimum  FROM [Vičiūnai-LAT$Item] t LEFT JOIN [Vičiūnai-LAT$Item Unit of Measure] vat ON (vat.[Item No_]=[No_] and vat.[Code]='IEP' ) "
					+ "	  LEFT JOIN  [Vičiūnai-LAT$Sales Line Discount] sd  on  sd.[Code]= [No_] and [Sales Type] = 1 and  [Sales Code] = 'AGENT' "
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
				if(name.length() > 3){
				
				name = name.substring(0, 1)
						+ (name.substring(1, name.length())).toLowerCase();
				double price = rs.getDouble("myygihind");
				prod.setPrice(price);

				
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
				
				if (cleardots(gr2).equals("1124")) {
					gr2 = "1.c.4.";
				}
				if (cleardots(gr2).equals("1125")) {
					gr2 = "1.c.5.";
				}
				if (cleardots(gr2).equals("1126")) {
					gr2 = "1.c.6.";
				}
				if (cleardots(gr2).equals("1127")) {
					gr2 = "1.c.7.";
				}
				if (cleardots(gr2).equals("1128")) {
					gr2 = "1.c.8.";
				}
				if (cleardots(gr2).equals("1129")) {
					gr2 = "1.c.9.";
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
			
			
			
		
			
			prod.setGroupSecond(grupp2);

				prod.setUnit(rs.getString("unit"));
				String ean = (rs.getString("ean"));
				if (ean.trim().length() > 0) {
					prod.setEAN(ean);
				}
				String xxx = rs.getString("status");
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

				if( partiid.get(prod.getID()) != null ) {
					
					prod.setPartii( partiid.get(prod.getID())+"");
				}
				
				
				
				prod.setVat("21");
				
				if (prods.get(id) != null) {
					prod.setStock(prods.get(id));
				}

				if (discT.containsKey(id)) {
					String aaa = (String) discT.get(id);
					prod.setMaxDiscount(Double.parseDouble(aaa));
					//prod.setStatusDiscount("1");
				}

				double opt = rs.getDouble("optimum");
				if (opt > 0) {
					prod.setOptAmountInUnits(opt);
				}

				double min = rs.getDouble("minimum");
				if (min > 0) {
					prod.setMinAmountInUnits(min);
				}
				
				prod.setName(Converter.enCodeEst( name ));
				prod.setNameSorting(Converter.cleanLitLatEst(name));
				tm.put(Converter.cleanLitLatEst(name) , prod);
				
				}
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
		
		String cmd = " SELECT * from v_LAT_Asortimentas";
		try {
			Connection con = getNewConnection2();
			System.out.println(cmd);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String type = rs.getString("grupp");
				String id = rs.getString("toode");	
				
						sb.append(" ; ;" + type + ";" + id
								+ "\n");
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

			
			
			String cmd = " select  d.[Sell-to Customer No_] AS klient,t.[Search Description],r.[No_] AS toode,r.[Quantity] AS kogus, d.[Shipment Date] AS lastdate ,  " +
					" r.[Užsakytas kiekis] -r.[Quantity] as saamatta , case when r.[Quantity]> 0 then r.[amount]/r.[Quantity] else 0 end  as lprice "
					+ "  FROM [Vičiūnai-LAT$Sales Line] r INNER JOIN [Vičiūnai-LAT$Sales Header] d ON r.[Document No_]= d.[No_]  INNER JOIN [Vičiūnai-LAT$Item] t ON t.[No_]=r.[No_]" +
					"  WHERE d.[Shipment Date]> GETDATE()-60  " +
					"  UNION ALL " +
				    "	select  d.[Sell-to Customer No_] AS klient,t.[Search Description],r.[No_] AS toode,r.[Quantity] AS kogus, d.[Shipment Date] AS lastdate , " + 
				    "	 r.[Užsakytas kiekis] -r.[Quantity] as saamatta ,r.[Unit Price] as lprice  FROM [Vičiūnai-LAT$Sales Shipment Line] r INNER JOIN [Vičiūnai-LAT$Sales Shipment Header] d ON r.[Document No_]= d.[No_] INNER JOIN [Vičiūnai-LAT$Item] t ON t.[No_]=r.[No_] " +
				    "	  WHERE d.[Shipment Date]> GETDATE()-60 " +
				    " order by 1,2,3,5 "; //d.[Sell-to Customer No_], t.[Search Description],r.[No_],d.[Posting Date]
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
				double lprice = rs.getDouble("lprice");
				
				Product prod = new Product(prodID);
				CustProd cp = new CustProd(custID, prodID, amount, 0, 0,
						lastdate, lprice,saamatta , 0, " ");

				cust.addProduct(prod, cp,false);
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

			String cmd2 = "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Shipment Date] AS lastdate FROM [Vičiūnai-LAT$Sales Line] r "
					+ " INNER JOIN [Vičiūnai-LAT$Sales Header] d ON r.[Document No_]= d.[No_] WHERE d.[Shipment Date]> GETDATE()-30 "
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

		} catch (Exception e) {
			e.printStackTrace();
		}
		return vec;
	}

	public String getStartingTime() {
		return null;
	}

	public String getDriverVersion() {
		return "VichiLATSQL";
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

		try {
			String cmd = " select sum(case when [Currency Factor] <> 0 then [Amount]/[Currency Factor]else [Amount] end) as summa,[Salesperson Code] AS agent "
					+ " from (select b.[Amount],a.[Currency Factor] ,a.[Salesperson Code]  from [Vičiūnai-LAT$Sales Invoice Header] a "
					+ " inner  join [Vičiūnai-LAT$Sales Invoice Line] b on a.[No_]=b.[Document No_] where Year(getdate())=Year(a.[Shipment Date]) and "
					+ " Month(getdate())=Month(a.[Shipment Date]) union all select b.[Amount],a.[Currency Factor],a.[Salesperson Code] from "
					+ " [Vičiūnai-LAT$Sales Cr_Memo Header] a inner join [Vičiūnai-LAT$Sales CR_Memo Line] b on a.[No_]=b.[Document No_] "
					+ " where Year(getdate())=Year(a.[Shipment Date]) and Month(getdate())=Month(a.[Shipment Date])) S "
					+ " group By [Salesperson Code]";
			Connection con = getNewConnection2();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				double sales = rs.getDouble("summa");
				String agentID = rs.getString("agent");
				Agent agent = (Agent) agents.get(agentID);
				if (agent != null) {
					agent.setMonthSales((int) (sales));
				}
			}

			rs.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
