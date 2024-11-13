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

import org.json.JSONArray;
import org.json.JSONObject;

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
import eevaeksport.util.ReportingService;

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

public class VichiLITSQLUUSDBDriver extends AbstractDBDriver implements
		BulkUpdater, Discounter, Manager {

	public VichiLITSQLUUSDBDriver(EksportPanel parent) {
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
		    rcompanyx = getParent().getProps().getProperty("rest.companyx");
	    }

	  

	  String user =  null;
	  String pass = null;
	  String rcompany = null;
	  String rcompanyx = null;
	

	public String getPilotDayData(Agent agent) {
		return null;
	}

	public String getSaveDebtInfo() {
		  makePictureCat();
		  System.exit(0);
		  return null;
	}

	
	 private void makePictureCat(){
		 

		 String 		 cmd2 = " select * from  MobilejavaVIK.v_VIK_GetProducts ";
		     	String picts = parent.getProps().getProperty("pict.dir");

		     	String pictspda = parent.getProps().getProperty("pictpda.dir");

		   	try {
		 	   Connection con = getNewConnectionView();
			
		   		Statement stmt = con.createStatement();
		   	    ResultSet	      rs = stmt.executeQuery(cmd2);

                HashSet olemas = new HashSet();
		        StringBuffer buf = new StringBuffer();
		        int a = 0;
		        int c = 0;
		        
		        // leiame kataloogid ja peame meeles 
		        
		        while (rs.next()) {
		   	    	a++;
		   	    	c++;
		   	     String toode = rs.getString("toode").trim();  
		         toode = toode.replace('/', '_');
		         toode = toode.replace('/', '_');
		         olemas.add(toode + ".jpg");
		         olemas.add(toode + ".JPG");
		         olemas.add(toode + ".png");
		         olemas.add(toode + ".PNG");
		   	    	if(c == 500){
		   	    		  c = 0;
		   	    		  System.out.println("Piltide leidmine "  + a);	
		   	    	}
		   	     
		   	    	     File f = new File(picts + toode+".jpg");
		                 String len = "0";
		                 if(f.exists()){
		                       len =	f.length() + "";
		                     	buf.append(toode+".jpg;"  + len + "\n");
		                  
			             } else {
			            	  f = new File(picts + toode+".png");
			                     len = "0";
				                 if(f.exists()){
				                   len =	f.length() + "";
				                   buf.append(toode+".png;"  + len + "\n");
				           	    	
				                 }  else {
				                	 f = new File(picts + toode+".PNG");
				                     if(f.exists()){
					                   len =	f.length() + "";
					                   buf.append(toode+".PNG;"  + len + "\n");
					                 }	
				                 }
		                 }
		                 
		                 }
		   	    rs.close();
		   	    stmt.close();
		   	    con.close();
		   	
		   	    //oleasoleva kataloogi kustutamine
		   	  System.out.println("Kataloogi kustutamine "  + a);	    
		               File f = new File(pictspda +  "list.txt");
		               if (f.exists())f.delete();
		               
		                 try {
		           		PrintWriter os = new PrintWriter(new FileWriter(pictspda + 
		           				  "list.txt", true));
		           		os.write(buf.toString());
		           		os.close();
		           	
		           	  }catch (Exception ex) {
		           		ex.printStackTrace();
		           	  }
		           	//  System.out.println("buf 0 " + a  + " " +  b + " "+ buf0.toString());



		   //Kataloogi sisude kustutamine
		             	  
		  
		          	 
		          	 
		        	 File res2 = new File (pictspda);
		            String[] ff2 = res2.list();
		        	 for(int i = 0;ff2.length>i;i++){
		            //	System.out.println("fff " + ff2[i]);
		            //	(new File ( pictspda + ff2[i])).delete(); 
		         
		            }
//		        	 Lõppes kustutamine 
		        	 
		          System.out.println("Piltide kopeerimine ");	
		          //Failide kopeerimine  
		            String[] gg = new File(picts).list();
		         	  //System.out.println("gg " + gg[0] + " " + gg.length);
		            
		             for(int i = 0;gg.length>i;i++){          	
		             	File in = new File ( picts + gg[i]); 
		             	if(in.isFile() && olemas.contains((gg[i]))){            	
		             	File inpda = new File ( pictspda + toUpper(gg[i])); 
		             	if(!inpda.exists()) {
		             	 	Converter.shrinkAndCopyFile(in,inpda);
		             	}
		            
		             	}
		             }
		             
		             
		           //  File fff =  new File (pictspda +  "list.txt");
		       	 // Converter.copyFile(f,fff);  	
		       	 // File fff0 =  new File (pictspda +  "list0.txt");
		       //	Converter.copyFile(f0,fff0);  	
		   	} catch (Exception e) {
		   	    getParent().showError(e);
		   	}
		   
		}
		   private String toUpper(String asi ){
			   
			 //  asi = asi.substring(0,asi.length()-4) + asi.substring(asi.lastIndexOf(".")).toLowerCase();
			   
			   return asi ;
		   }
		   


	
	
	
	  public Vector getPriceMatrixData(){
		  
		  	 String sql =" SELECT  custid, clgroup,prodid, startday, enddate, price ,discountpct as disc , salespricetype as type  from MobileJavaVIK.v_VIK_AkcijuKainos where " + 
		  	 		"		  	 	 len(clgroup) >0 or  len(custid) >0 ";

		       Vector result = new Vector();
		         try {
		          
		      	   Connection con = getNewConnectionView();
		   	        Statement stmt = con.createStatement();
		   	        ResultSet rs = stmt.executeQuery(sql);
		           while (rs.next()) {

		             String custID = Converter.enCodeEst(rs.getString("custid"));
		             String clGroup = rs.getString("clgroup");
		             String prodID = rs.getString("prodid").trim();
		             String prodGroup = null;
		            
		         
		             String sdate = rs.getString("startday");
		             String edate = rs.getString("enddate");
		             double price = rs.getDouble("price");
		             double disc = rs.getDouble("disc");
		             String type = rs.getString("type");
		             
		             MatrixLine mline = new MatrixLine();

		             if (custID != null && custID.length() > 0) {
		               mline.setCustID(custID.trim());
		             }
		             if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
		               mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
		             }

		             if(sdate != null  && !sdate.startsWith("1753")){
		            	 mline.setStartDate(sdate.substring(0,10));
		             }
		             if(edate != null && !edate.startsWith("1753")){
		            	 mline.setEndDate(edate.substring(0,10));
		             }
		             if (prodID != null && prodID.length() > 0) {
		               mline.setProdID(prodID);
		             }
		             if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
		               mline.setDiscGroup(prodGroup.trim());
		             }

		             if (price > -1) {
		               mline.setPrice(price);

		             }
		             if (disc != 0) {
		                 mline.setDiscount(disc);
		               }
		             
		             if(price == 0 && disc == 0) {
		            	 mline.setProdLevel("pr");
		             }
		             
                   if(type != null && type.trim().length() > 0) {
                	   mline.setLineinfo(type.trim());
                   }
		          //   if(mline.getPrice() > 0 || mline.getDiscount() > 0){
		                 result.add(mline);    
		            // }
		             
		           }
		           rs.close();
		           stmt.close();
		           con.close();
		         }catch(Exception e){
		        	 e.printStackTrace();
		         }
			
		    return result;
		      }
	  


	private Connection getNewConnection2() throws SQLException {
		return DriverManager.getConnection(url2, getParent().getProps()
				.getProperty("dsna.user"),
				getParent().getProps().getProperty("dsna.pass"));
	}
	
	private Connection getNewConnectionView() throws SQLException {
		String v = "jdbc:odbc:naviltview";
		if(getParent().getProps()
				.getProperty("view.url") != null) {
			v = getParent().getProps()
					.getProperty("view.url");
		}
		return DriverManager.getConnection(v, getParent().getProps()
				.getProperty("view.user"),
				getParent().getProps().getProperty("view.pass"));
	}


	public void insertDefective(DefectiveWrapper wr) {
		
	}

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr) {
		
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

	

	public ManagerData getManagerData() throws SQLException {

		
		return null;

	}

	 public void insertDistribution(DistEvalListWrapper wr){
		   
	   }

	/*
	 * private String setZero(int i){ String x = i +""; if (x.length() == 1){ x
	 * = "0" + x; } return x; }
	 */
	public Vector getFixedAssortmentData() {
		Vector lines = new Vector();

		try {

			String cmd = " SELECT a.grupp ,a.toode  from MobileJavaVIK.v_VIK_Asortimentas a inner join MobilejavaVIK.v_VIK_GetProducts"
					+ " p ON p.toode=a.toode where len(a.grupp) > 1 and a.grupp != 'MINASORT'\r\n" + 
					" order by a.grupp,p.kaal,a.toode";
            System.out.println(cmd);
            
			Connection con = this.getNewConnectionView();
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
					mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
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
			stmt.close();
			con.close();
		} catch (Exception e) {
			getParent().showError(e);
		}
		return lines;
	}

	public DealerProduct getDealerProductsList() {
		return null;
	}

	public String getSaveStockInfo() {
		StringBuffer products = new StringBuffer();


		try {
			String cmd = "Select * from MobileJavaVIK.vik_litstock";

			Connection con = getNewConnectionView();
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
			stmt.close();
			con.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

    //  System.out.println(products.toString());

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
		String cmd = " SELECT  * from MobileJavaVIK.v_VIK_GetProdGroups";
		try {
			Connection con = getNewConnectionView();
			System.out.println(cmd);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String id = rs.getString("kood");
				if (cleardots(id).equals("28")) {
					id = "5.1.";//5.01.
				}
				if (id.equals("1.10.")) {
				//	id = "1.a.";//5.01.
				}
				if (id.equals("1.11.")) {
					//id = "1.b.";//5.01.
				}
				if (id.equals("1.12.")) {
					//id = "1.c.";
				}
				if (id.equals("1.13.")) {
					//id = "1.d.";
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

				/*
				
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
				

				if (cleardots(groupID).equals("1131")) {
					groupID = "1.d.1.";
				}
				if (cleardots(groupID).equals("1132")) {
					groupID = "1.d.2.";
				}
				if (cleardots(groupID).equals("1133")) {
					groupID = "1.d.3.";
				}
				if (cleardots(groupID).equals("1134")) {
					groupID = "1.d.4.";
				}
				if (cleardots(groupID).equals("1135")) {
					groupID = "1.d.5.";
				}
				if (cleardots(groupID).equals("1136")) {
					groupID = "1.d.6.";
				}
				if (cleardots(groupID).equals("1137")) {
					groupID = "1.d.7.";
				}
				if (cleardots(groupID).equals("1138")) {
					groupID = "1.d.8.";
				}
				if (cleardots(groupID).equals("1139")) {
					groupID = "1.d.9.";
				}*/
				
				
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

		 agent1 = new TreeMap();
		
		String cmd = "SELECT [Code] AS kood,[Name] AS nimi,[Location Code] AS objekt ,left ([Location Code for MobileJava],3) AS objekt2" +
				" ,[Item subgroup filter] as grups FROM "
				+ "[Vičiūnai ir Ko$Salesperson_Purchaser] WHERE len([Location Code])>0";
		  cmd = "select * from  MobilejavaVIK.v_VIK_GetAgents ";
		if (isDebug())
			System.out.println(cmd);
		TreeMap agents = new TreeMap();
		try {
			Connection con = getNewConnectionView();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				Agent agent = new Agent(rs.getString("kood").trim());
				String name = rs.getString("nimi").trim();
				agent.setObject(rs.getString("objekt"));
				String prodGrupp = rs.getString("grups");
				StringTokenizer st = new StringTokenizer(prodGrupp,",");
				while(st.hasMoreTokens()){
					String gr = cleardots(st.nextToken()).replace('*', ' ').trim();
					agent.addProdGroupList(gr);
				}
				
				
				 agent.setLanguage("LIT");
		          agent.setCountry("lt");
		          agent.setPassWord("0000");
		          String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
		          if(email.length() > 1){
		        	  agent.setEmail(email);
		        	  agent.setActive(true);
		          }
		         
		          
		          
		  		
		          
				agent.setName(name);
				agent.setIsPilot(true);
				agents.put(name, agent);
				 agents1.put(agent.getID(), agent);
				 agent1.put(agent.getID(), agent);
			}
             rs.close();
             stmt.close();
             con.close();
             
			Agent aa = new Agent("HOUSE1");
			aa.setName("office1");
			// aa.setObject("KAU");
			agents.put("office1", aa);
			 agents1.put("office1", aa);
			 
			 Agent aw = new Agent("WARE");
		        aw.setName("WARE");
		        agents.put("WARE", aw);
		        agents1.put("WARE", aa);
		        
		        Agent aa1 = new Agent("WARE1");
		        aa1.setName("WARE1");
		        agents.put("WARE1", aa1);
		        agents1.put("WARE1", aa);

			cmd = "SELECT k.[No_] AS klient, [Balance total (LCY)]AS summa "
					+ " FROM [Vičiūnai ir Ko$Customer] k  WHERE k.[Salesperson Code] != '' AND [Balance total (LCY)]<>0 ";
			cmd ="select * from MobilejavaVIK.v_VIK_GetDebts";
			System.out.println(cmd);
			con = getNewConnectionView();
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
			stmt1.close();
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
    
	private TreeMap aad = new TreeMap();
  
	public TreeMap getCustomers(Agent agent) {
		
		
		try{
			if(rcompanyx != null)
			   Converter.getManagersReplacements(agent1, rcompanyx, user, pass, false); 
				
		}catch(Exception e){
			
		}
		
		HashSet custs = new HashSet();
	 	TreeMap freeze = new TreeMap();
	    TreeMap custAgents = new TreeMap();
	   	HashMap custagVisit = new HashMap();
	   	HashMap custagName = new HashMap();
	   	HashMap agendiTyyp = new HashMap();
	   	HashMap custVisit = new HashMap();
	    Vector kliendid = new Vector();
		try {

		
			
			
			 
		
		/*	 
			  Connection cone = this.getNewConnection2();
		    
		   	Statement stme = cone.createStatement();
		
		       ResultSet rse = stme.executeQuery(qqq);
		       while(rse.next()){
		   	   
		    		String age = rse.getString("agent").trim();
		       	    String klient = rse.getString("klient").trim();
		       	    String day = rse.getString("day").trim();
		       	 
		       	   String visit = day;
		       	   custagVisit.put(age + klient, visit);
		           
		    	   custVisit.put(klient, visit);
		       	 kliendid.add(klient);
		       	 
		       	 
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
*/
		   //   System.out.println("custAgents " + custAgents);
		    
			
			
	String ccc =  "select custcode as cust ,Code+ ' ' +name as last from MobilejavaVIK.v_VIK_FA ";
		      
  	Connection cn = getNewConnectionView();
 	Statement stcn = cn.createStatement();
	ResultSet rscn = stcn.executeQuery(ccc);
	while (rscn.next()) {
		String cid = rscn.getString("cust").trim();
		String name = rscn.getString("last");
		//String dd = rscn.getString("dd").substring(0,10);
		name = name.trim();// + " " + dd;
		if(freeze.get(cid) != null){
			Vector vv = (Vector)freeze.get(cid);
			if(!vv.contains(name)){
				
				vv.addElement(name);
				freeze.put(cid, vv);
			}
			
		}else{
			Vector vv = new Vector();
			vv.addElement(name);
			freeze.put(cid, vv);
		}
	}
	rscn.close();
	stcn.close();
	cn.close();
	
	
	   String cc2 = "select * from MobileJavaVIK.v_VIK_GetCustIlns";
	   Connection con  = getNewConnectionView();
	   Statement stmt1 = con.createStatement();
		ResultSet rs1 = stmt1.executeQuery(cc2);
		while (rs1.next()) {
			String addID = rs1.getString("klient").trim();
			String addCode = rs1.getString("klient2").trim();
		    String ilnID = rs1.getString("iln").trim();
		    aad.put(addCode, ilnID);
		   if(addCode.equals("1")) {
			aad.put(addID+"_1", ilnID);
		    }
		   aad.put(addID+"_"+addCode, ilnID);
		}
		rs1.close();
		stmt1.close();
		con.close();
		
		  	String 	cmd ="select * from MobilejavaVIK.v_VIK_GetDebtList";
			System.out.println(cmd);
			this.debtList = new Vector();
			
			cn = getNewConnectionView();
			 stmt1 = cn.createStatement();
			 rs1 = stmt1.executeQuery(cmd);
			while (rs1.next()) {
				Debt debt = new Debt();
				String custID = rs1.getString("klient");
				String kl2 = rs1.getString("kl2");
				if(kl2 != null && kl2.trim().length() > 0) {
					if(kl2.equals("1") && aad.get(custID+"_"+kl2.trim()) == null) {
						kl2 = custID + "_1";  
					   } else if(aad.get(custID+"_"+kl2.trim()) != null) {
						   kl2 = (String)aad.get(custID+"_"+kl2.trim());
					   }else if(aad.get(kl2.trim()) != null) {
						   kl2 = (String)aad.get(kl2.trim());
					   }
					custID = custID + "_" + kl2.trim();
				}
			    String payer = rs1.getString("maksja");
				debt.setCustID(custID);
				debt.setPayerID(payer);

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
			cn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// / KLIENDI ANDMETE LEIDMINE

		TreeMap<String,Double> yle = new TreeMap<String,Double>();
		   
	try {
		String sql3= "select * from MobilejavaVIK.v_VIK_GetOverdue";
		Connection con3 = getNewConnectionView();
		Statement stmt3 = con3.createStatement();
		ResultSet rs3 = stmt3.executeQuery(sql3);
		while (rs3.next()) {
            String cuid = rs3.getString("maksja").trim();
			double sum = rs3.getDouble("yle");
		
				yle.put(cuid, sum);
				// System.out.println("yle " + id+ " " + sum);
		

		}

		rs3.close();
		stmt3.close();
		con3.close();
	} catch (Exception e) {
		e.printStackTrace();
	}
		
		
		
		
		
		
		
		
		TreeMap tm = new TreeMap();
		this.customers = new ArrayList();

		String sql = "SELECT k.kood as kood1 ,k.[Ship-to Address ILN] as kood2,k.[Ship-to-address code] as kood3 , k.nimi as nimi2, k.[Ship-to address name]  as nimi3, k.aadress as aadress12 ,k.kood as payer2 ,"
				+ " k.agent as agent2 , [Delivery time till] as timex  , k.* from" + 
				"				 MobilejavaVIK.v_VIK_Customers k	" ;

		System.out.println(sql);
		
		try {
		Connection con = this.getNewConnectionView();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String id = rs.getString("kood1").trim();
				String idorig = id; 
				String id2 = rs.getString("kood2");
				String acode = rs.getString("kood3");
				
				if(id2 != null && id2.trim().length() > 0) {
					id = id + "_" + id2.trim();
				}
				String name2 = rs.getString("nimi2").trim();
				String name3 = rs.getString("nimi3");
				String aadress12 = rs.getString("aadress12");
				
				
				String name =  name2;
				if(name3 != null && name3.trim().length() > 0) {
					name = name3;
				}
				
				
				//String id = rs.getString("kood").trim();
				// System.out.println(line);
				Customer customer = new Customer(id);
				if(acode != null) {
					customer.setCity("AddrCode: " + acode.trim());
				}
				String payer = null;
				if (name.equals("")) {
					name = "<" + id + ">";
				}
				
				if (aadress12 != null && aadress12.trim().length() > 0) {
					 customer.setName(name.trim() + " " + aadress12.trim());
				} else {
				    customer.setName(name.trim());
				}
				
				payer = rs.getString("payer2");
				if (payer != null && payer.trim().length() > 0) {
					customer.setPayer(payer.trim());
				}

			
				if (aadress12 != null && aadress12.trim().length() > 0) {
					String addr = aadress12;
					customer.setAddress(Converter.enCodeEst(addr));
					customer.setName(Converter.enCodeEst(customer.getName()));
				}
            
				String ag = rs.getString("agent2").trim();
				customer.addAgent(new Agent(ag));
				customer.addAgent(new Agent("HOUSE1"));
				customer.addAgent(new Agent("WARE"));
				customer.addAgent(new Agent("WARE1"));
				
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
				

				   String timex = rs.getString("timex");

					if (timex != null && timex.trim().length() > 0) {
						customer.addCity(timex);
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
					vv.add(Converter.cleanLitLatEst(grupp.trim()));
					customer.setFixSortCode(vv);
					customer.setCustType(Converter.cleanLitLatEst(grupp.trim()));				
				}
				
				if(!asi.equals("3") && !asi.equals("4") && !asi.equals("5") && !asi.equals("6") &&  !asi.equals("7") 
						&&  customer.getGroup().trim().length() > 0){
					customer.setCustType("MINASORT");
				}
				
              if(freeze.get(customer.getID()) != null){
            	  Vector vv = (Vector) freeze.get(customer.getID());
            	  Enumeration enume = vv.elements();
            	  while (enume.hasMoreElements()){
            		  String nn = enume.nextElement()+"";
            		  if(customer.getInfo().indexOf(nn) == -1)
            		  customer.setInfo(customer.getInfo() + "|" + nn);
            	  }
              }
              
              if(freeze.get(customer.getPayer()) != null){
            	  Vector vv = (Vector) freeze.get(customer.getPayer());
            	  Enumeration enume = vv.elements();
            	  while (enume.hasMoreElements()){
            		  String nn = enume.nextElement()+"";
            		  if(customer.getInfo().indexOf(nn) == -1)
            		  customer.setInfo(customer.getInfo() + "|" + nn);
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

				
				
				if ( !customer.isNodebt()) {

				   if(yle.containsKey(idorig)) {
					   customer.setDebt(yle.get(idorig));
				   }else if(yle.containsKey(customer.getPayer())) {
					   customer.setDebt(yle.get(customer.getPayer()));
				   }
				   if(idorig.equals("A1816")) {
						System.out.println("Võlg olemas " +
								yle.containsKey(idorig) + " " + yle.containsKey(customer.getPayer())
					+ " " + customer.getDebt());
					}   
            	}
				   String cont = rs.getString("kontakt");
		           if(cont != null && cont.trim().length()>0){
		               customer.setPersonalData(Converter.enCodeEst(cont.trim()));   
		           }
		            cont = rs.getString("Ship-to Address contact");
		           if(cont != null && cont.trim().length()>0){
		               customer.setPersonalData(Converter.enCodeEst(cont.trim()));   
		           }
		           
                boolean goes = true;
		           
		           String blokk = rs.getString("Blocked");
		           if(blokk != null && blokk.trim().length()>0){
		               if(blokk.trim().equals("1")) {
		            	   goes = false;
		               }
		           }
		           
		           String alegr = rs.getString("alegr");
		           if(alegr != null && alegr.trim().length()>0){
		               customer.setGroup(alegr);   
		           }
		          
		           
		          String chain = rs.getString("chain");
		           if(chain != null && chain.trim().length()>0){
		               customer.setChain(Converter.enCodeEst(chain.trim()));   
		           }
		           
		           double CreditLimitLCY = rs.getDouble("CreditLimitLCY");
		           if(CreditLimitLCY>0){
		               customer.setCredit(CreditLimitLCY+"");   
		           }
		           
		            
		           
				// customer.setPayer(id);
				if(goes) {
					tm.put(customer.getName()+ customer.getID(), customer);
					this.customers.add(customer.getID());
					  custs.add(customer.getID());
				}
				
			}

			rs.close();
			stmt.close();
			con.close();

			
			
			
			  HashSet asses = new HashSet();
			
			

		      if(this.rcompany != null){
		    	
		    	  
		    	     //Tõmbame alla kõikmis meil olemas
		      eevaeksport.util.ReportingService servu = new ReportingService();
		  		servu.user =  user;
		  		servu.pass = pass;
		  		servu.company = rcompany;   
		  		JSONArray assets = servu.getAssets(null);
		  		servu.closeClient();
		 
		  		
		  		HashMap asscusts = new HashMap();
		  	     HashSet cuus = new HashSet();	
		  		for (int i = 0; i < assets.length(); i++) {
					 JSONObject cob = assets.getJSONObject(i);
						 //   String id = cob.get("id") + "";
						    String code = (String)cob.get("code");
							//String name = (String)cob.get("name");
							
							try{

								JSONObject cust  = (JSONObject)cob.get("customer");
								 if(cust != null){
									 String kkood = (String)cust.get("code");
									// String kname = (String)cust.get("name");
									asscusts.put(code, kkood);
									cuus.add(code);
								 }	
							}catch(Exception e){
								
							}
				 }
			
		    	  
		    	//  System.out.println("Asses size enne  " + asscusts.size());
		    	  Vector vec = new Vector(); 
		    	  String  cmd3 = "select  * from MobilejavaVIK.v_VIK_FA   ";
		    	  Connection con4 = getNewConnectionView();
		    	Statement stm = con4.createStatement();
					
		    	  ResultSet rs1 = stm.executeQuery(cmd3);
		               int counter = 0;
		                 while(rs1.next()){
		                	
		                	 JSONObject obj = new JSONObject();
		                	 
		                	  
		                        String code = rs1.getString("code").trim();
		                        obj.put("code", code);
		                        asses.add(code);
		                        try{
		                        	
		                        String name = rs1.getString("name");
		                        if(name == null){
		                        	name = code;
		                        }else{
		                        	name = name.trim();
		                        }
		                        
		                        obj.put("name", name);
		                        
		                        String ean = rs1.getString("ean");
		                        if(ean != null && ean.trim().length() > 0){
		                        	   obj.put("ean", ean);
		                        }
		                     
		                        String nfcTag = rs1.getString("nfcTag");
		                        if(nfcTag != null && nfcTag.trim().length() > 0){
		                     	   obj.put("nfcTag", nfcTag);
		                     }
		                        String factoryId = rs1.getString("factoryId");
		                        if(factoryId != null && factoryId.trim().length() > 0){
		                      	   obj.put("factoryId", factoryId);
		                          }
		                      
		                        String type = rs1.getString("type");
		                        if(type != null && type.trim().length() > 0){
		                        	type = type.trim();
		                        }else{
		                        	type = "";
		                        }
		                        
		                        String description = rs1.getString("description");
		                        if(description != null && description.trim().length() > 0){
		                      	   obj.put("description", (description + " "+type).trim());
		                      }
		                        java.sql.Date born = rs1.getDate("born");
		                        if(born != null ){
		                       	   obj.put("born", Converter.jsonDate(born));
		                       }
		                        
		                     //   java.sql.Date contract = rs1.getDate("contract");
		                        //if(contract != null ){
		                       	 //  obj.put("contract", Converter.jsonDate(contract));
		                      // }
		                    
		                        boolean goes = false;
		                        if(asses.contains(code)){
		                        	goes = true;
		                        }
		                        
		                        String ccode = rs1.getString("custcode");
		                        if(ccode != null && ccode.trim().length() > 0){
		                        	if(ccode.equals("1005")){
		                        		 obj.put("active", false);
		                        	}else{
		                        		 obj.put("active", true);
		                        	}
		                        	
		                        	if(ccode.equals("1005") || ccode.equals("1003")){
		                        		goes = true;
		                        		asses.add(code);
		                        	}
		                        	
		                      }
		                        
		                      
				                       
		                        if(goes ){  // võtsin välja et alati kirjutaks koodi yle 
		                        	//&& !cuus.contains(code)
		                        	 vec.add(obj);
		                        	counter ++;
		                        }
		                           
		                          
		                        
		                        }catch (Exception r){
		                         r.printStackTrace();    
		                        }
		                        
		                       
		                        //java.sql.Date contract = rs1.getDate("contract");
		                      }
		                     rs1.close();
		          
		    	  
		                     
		                    // System.out.println("Counter " + counter + " Asses " + asses.size());
		                     
		    	   Converter.sendAssetsJson(vec,this.user,this.pass,this.rcompanyx);     
		    	   
		                     
		                		
		             		
		             		
		    	   //siin saadame seosed ära jujul kui customer on olemas selline 
		     	   vec = new Vector(); 
		     
		 cmd3 = " select a.code,  custcode, k.[Customer ILN] as kl from MobilejavaVIK.v_VIK_FA a  left join MobilejavaVIK.v_VIK_Customers  k  on a.custcode = k.[kood]  where a.custcode != ''" ;
		     	   int cou = 0;
		              int coua = 0;
		     	  rs1 = stm.executeQuery(cmd3);
		                  while(rs1.next()){
		                 	
		                 	 JSONObject obj = new JSONObject();
		                 	 coua++;
		                 	 
		                         String code = rs1.getString("code").trim();
		                         obj.put("inventoryItemCode", code);
		                        // Date contract = rs1.getDate("contract");
		                        // obj.put("contractDate",contract.getTime() );
		                         String custcode = rs1.getString("custcode").trim();
		                         
		                         String kl = rs1.getString("kl");
		                         if(kl != null) {
		                         obj.put("customerCode", kl.trim() + "_" + custcode);
		                         
		                       
		                         if(asses.contains(code) && custs.contains(custcode) ){
		                        	  String olemas = (String)asscusts.get(code);
		                        	  if(olemas == null){
		                                  vec.add(obj);  
		                                  cou++;  
		                         	  }else if(olemas != null){
		                        		 // if(!olemas.equals(custcode)){
		                        			   vec.add(obj);  
		                                       cou++; 
		                        		  //}
		                        	  }
		                          }
		                       }}
		                  
		                  stm.close();
		                     con4.close();
		    	//   System.out.println("assets cou " +  cou + " "+ coua);
		                      
		                      // siis peaks tegema vahele kontrolli et kui on muudatus siis ainult laksad ylesse
		    	   
		    	   
		    	   
		                    Converter.sendAssetsCustomerJson(vec,this.user,this.pass,this.rcompanyx);     
		               	   
		    	   
		    	   
		    	   
		      }

			
			
			
			
			
			
			
		   //   Converter.sendCustomerJson(tm,this.rcompany, true, this.user,this.pass, true);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(tm.size() < 10) {
			System.exit(0);
		}
		return tm;
	}

	public TreeMap getProducts(Agent agent) {
		TreeMap tm = new TreeMap();
		TreeMap KAU = new TreeMap();
		TreeMap allowedstuff = new TreeMap();
		Vector topstuff = new Vector();
		TreeMap discT = new TreeMap();
		TreeMap mintime = new TreeMap();
		HashMap nullseis = new HashMap();

		try {
			
			String cmd = "Select * from MobilejavaVIK.vik_litstock";
			Connection con = getNewConnectionView();
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
//System.out.println("KAU suurus " + KAU.size());
			rs.close();
			con.close();

			String cmd7 = "select * from MobilejavaVIK.v_VIK_GetDiscount";
			con = getNewConnectionView();
			stmt = con.createStatement();
			rs = stmt.executeQuery(cmd7);
			while (rs.next()) {
				String toode = null;
				double seis = 0;
				
				toode = rs.getString("kood").trim();
				seis = rs.getDouble("disc");
				discT.put(toode, seis + "");
		
			}

			rs.close();
			con.close();
			
			String top ="select No_ as itemid , [Top Item] as asi from [Vičiūnai ir Ko$Item 2] where [Top Item] = 1 " ;

		con = getNewConnection2();
		stmt = con.createStatement();
		rs = stmt.executeQuery(top);
		while (rs.next()) {
			topstuff.add( rs.getString("itemid").trim());
					}

		rs.close();
		stmt.close();
		con.close();
			

		String top2 = "select * from  MobilejavaVIK.v_VIK_GetMinOrdTimes";
	
	con = this.getNewConnectionView();
	stmt = con.createStatement();
	rs = stmt.executeQuery(top2);
	while (rs.next()) {
		String pro = rs.getString("itemid").trim();
		mintime.put( pro,rs.getTime("asi").getTime());
		nullseis.put(pro, pro);
				}

	rs.close();
	stmt.close();
	con.close();
		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		boolean splitB = true;
		if("true".equals(parent.getProps().getProperty("nospilt"))){
			splitB = false;
		}
		

		try {
			String 	cmd = "select * from  MobilejavaVIK.v_VIK_GetProducts ";
			
			System.out.println(cmd);
			//Connection con = getNewConnection2();
			Connection con = getNewConnectionView();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String id = rs.getString("toode").trim();
				String weight = "";
				Product prod = new Product(id);
				
				String name = rs.getString("nimi");
				
				name = name.substring(0, 1)
						+ (name.substring(1, name.length())).toLowerCase();
				
				name = name.replaceAll("paniruotos", "panir. ");
				
				double price = rs.getDouble("myygihind");
				prod.setPrice(price);

				if(topstuff.contains(id)){
					prod.setStatusTop("1");
				}
				
				String gr1 = rs.getString("grupp");
			
				String grupp = cleardots(gr1);
				if (grupp.equals("208")) {
					grupp = "501";
				}
				prod.setGroup(grupp);
   
				if(allowedstuff.get(prod.getID()) != null){
					prod.setTimeToSell(Long.parseLong(""+allowedstuff.get(prod.getID())));
				}
				
				
  
				String gr2 = rs.getString("grupp2");
   			
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
					ean = ean.replaceAll(";", " ").trim();
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

				if (KAU.containsKey(id)) {
					String aaa = (String) KAU.get(id);
					prod.setPartii("Kau+KauCe"
							+ aaa.substring(0, aaa.indexOf(".")));
					prod.setStock(Double.parseDouble(aaa));
				}
				
				if(nullseis.get(prod.getID()) != null) {
					prod.setStock(-1);;
				}

				if (discT.containsKey(id)) {
					String aaa = (String) discT.get(id);
					prod.setMaxDiscount(Double.parseDouble(aaa));
					prod.setStatusDiscount("1");
				}
				double opt = rs.getDouble("optimum");
				if (opt > 0) {
					prod.setOptAmountInUnits(opt);
				}

				double min = rs.getDouble("minimum");
				if (min > 0) {
					prod.setMinAmountInUnits(min);
				}
				
				String origi = rs.getString("origi");
				if (origi != null && origi.trim().length() > 0) {
					if(prod.getPartii() != null && prod.getPartii().trim().length() > 1) {
						prod.setPartii(prod.getPartii() + "|CoO: " + origi.trim() );
					} else {
						prod.setPartii("CoO: " + origi.trim() );
					}
				}
				
				if(mintime.get(prod.getID()) != null){
					prod.setTimeToSell(Long.parseLong(mintime.get(prod.getID())+""));
				}
				prod.setName(Converter.enCodeEst(weight.substring(0, weight.indexOf('.') + 4) + " "
						+ name.replace('~', '-')) + " " + prod.getID());
				tm.put(prod.getName(), prod);
			}

			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tm;
	}
	
	
	 public TreeMap getAgents1() {
			return agents1;
		}

	private TreeMap agents1 = new TreeMap();

	public String getAssortmentStr(Agent agent) {
		StringBuffer sb = new StringBuffer();
		String cmd = " SELECT [Asortimento kodas] AS grupp,[Prekės Nr_] AS toode  FROM [Vičiūnai ir Ko$Prekių asortimentas] r " +
					 " INNER JOIN [Vičiūnai ir Ko$Item] t ON t.[No_]=r.[Prekės Nr_]  where [Asortimento kodas]= 'MINASORT' " +
                     " ORDER BY [Net Weight] , [Search Description] ";

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

			String cmd = " select * from   MobilejavaVIK.v_VIK_GetCustProdList";
			System.out.println(cmd);
			// "klient","toode","kogus","viimane kuupļæ½ev","viimane kogus"
			Connection con = getNewConnectionView();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {

				String custID = rs.getString("klient").trim();
				 String custAbi = rs.getString("abiklient").trim();
				
				 
				String prodID = rs.getString("toode").trim();
				

				double amount = 0;
				String lastdate = " ";
				amount = rs.getDouble("kogus");
				lastdate = rs.getString("lastdate").trim().substring(5, 10);
				
				String cu = custID;
				if(custAbi != null && custAbi.trim().length() > 0) {
					
					
					if(custAbi.equals("1") && aad.get(cu+"_"+custAbi.trim()) == null) {
						custAbi = custID + "_1";  
					   } else if(custAbi.equals("2") && aad.get(cu+"_"+custAbi.trim()) == null) {
						custAbi = custID + "_2";  
					   } else if(aad.get(cu+"_"+custAbi.trim()) != null) {
						 custAbi = (String)aad.get(cu+"_"+custAbi.trim());
					   } else if(aad.get(custAbi.trim()) != null) {
						 custAbi = (String)aad.get(custAbi.trim());
					   }
					
					 
					cu = custID + "_" + custAbi.trim();
				}
				Customer cust = (Customer) kliendid.get(cu);
				if (cust == null) {
					cust = new Customer(cu);
				}
				double saamatta = rs.getDouble("saamatta");
				if(saamatta > -1 && saamatta < 1){
					saamatta = 0;
				}
				Product prod = new Product(prodID);
				CustProd cp = new CustProd(cu, prodID, amount, 0, 0,
						lastdate, 0,saamatta , 0, " ");

				cust.addProduct(prod, cp,false);
				kliendid.put(cu, cust);
				vec.addElement(cp);

			}
			rs.close();
			con.close();

			int counter = 0;
			int cou = 0;
			String oldklient = null;
			String olddate = " ";
			String oldtoode = null;

			String cmd2 = "SELECT  d.[Sell-to Customer No_] AS klient,   d.[Ship-to Code]  as abiklient, r.[No_] AS toode,r.[Quantity] AS kogus, d.[Shipment Date] AS lastdate FROM [Vičiūnai ir Ko$Sales Invoice Line] r "
					+ " INNER JOIN [Vičiūnai ir Ko$Sales Invoice Header] d ON r.[Document No_]= d.[No_] "
					+ "  WHERE d.[Shipment Date]> GETDATE()-30 " +
					"  UNION ALL " +
				    "	select  d.[Sell-to Customer No_] AS klient,  d.[Ship-to Code]   as abiklient, r.[No_] AS toode,r.[Quantity] AS kogus, d.[Shipment Date] AS lastdate " + 
				    "	 FROM [Vičiūnai ir Ko$Sales Shipment Line] r INNER JOIN [Vičiūnai ir Ko$Sales Shipment Header] d ON r.[Document No_]= d.[No_] " +
				   "	  WHERE d.[Shipment Date]> GETDATE()-30 "
					+ " Order by 1,3,5 desc ";
			cmd2 = "select * from   MobilejavaVIK.v_VIK_GetCustProdList";
			System.out.println(cmd2);
			// "klient","toode","kogus","viimane kuupļæ½ev","viimane kogus"
			Connection con2 = getNewConnectionView();
			Statement stmt2 = con2.createStatement();
			ResultSet rs2 = stmt2.executeQuery(cmd2);

			while (rs2.next()) {
				String klient = rs2.getString("klient").trim();
				String custAbi = rs2.getString("abiklient");
				
				String cu = klient;
				if(custAbi != null && custAbi.trim().length() > 0) {
					

					 
					if(custAbi.equals("1")  && aad.get(cu+"_"+custAbi.trim()) == null) {
						custAbi = klient + "_1";  
					    } else if(custAbi.equals("2") && aad.get(cu+"_"+custAbi.trim()) == null) {
							custAbi = klient + "_2";  
					   }else if(aad.get(cu+"_"+custAbi.trim()) != null) {
						 custAbi = (String)aad.get(cu+"_"+custAbi.trim());
					   }else if(aad.get(custAbi.trim()) != null) {
						 custAbi = (String)aad.get(custAbi.trim());
					   }
				
					cu = klient + "_" + custAbi.trim();
				}
				
				
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
					oldklient = cu;
					olddate = date;
					oldtoode = toode;
				}
				if (!oldklient.equals(cu)) {
					counter = 0;
					oldklient = cu;
					olddate = date;
				}
				if (!toode.equals(oldtoode)) {
					counter = 0;
				}
				Customer c = (Customer) kliendid.get(cu);
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
					oldklient = cu;
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
		return "VichiLT";
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
			String cmd = " Select  summa, agent from MobilejavaVIK.v_VIK_Turnover";
			Connection con = this.getNewConnectionView();
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
