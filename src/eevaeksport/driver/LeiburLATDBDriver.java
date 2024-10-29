package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
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

public class LeiburLATDBDriver extends AbstractDBDriver implements Manager, Promo,
		BulkUpdater, Discounter {
	public LeiburLATDBDriver(EksportPanel parent) {
		super(parent);
		if (parent.getParentFrame().isDaySales()) {
			this.subjectFilesTo = getParent().getString("senddatamsg");
		} else {
			this.subjectFilesTo = parent.getString("filemsg");
		}
		String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
		try {
			Class.forName(databaseDriverName).newInstance();
			con = DriverManager.getConnection(url, "mjuser", ("usEr4Mj"));
			stmt = con.createStatement();
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
	
	
	public String getDealerCustomerCSV(String agent) {
		return null;
	}

	public Vector getLastThreeData() {

		Vector result = new Vector();
		// Siin loeme igale kliendile kylge viimase kolem korra ostud
		try {

			Connection con = getNewConnection();
			Statement st = con.createStatement();

			String cmd2 = "select * from Customer where blok2 =0 and len(agent)>1 and hasRoute = 1 "
					+ " and statuscode = 20 ";

			if (isDebug())
				System.out.println(cmd2);
			ResultSet rs = st.executeQuery(cmd2);

			while (rs.next()) {

				String accountnum = rs.getString("kood").trim();

				String sq1 = "select top 5 kuupaev from  SalesGeneral where klient ='"
						+ accountnum
						+ "' group by klient ,kuupaev  order by klient,kuupaev desc";
				Connection con2 = getNewConnection();
				Statement st2 = con2.createStatement();

				ResultSet rs2 = st2.executeQuery(sq1);
				while (rs2.next()) {
					Date kuup = rs2.getDate("kuupaev");
					String sq3 = "select toode , kogus from  SalesGeneral where klient ='"
							+ accountnum
							+ "' and kuupaev =?  order by toode";
					Connection con3 = getNewConnection();
					PreparedStatement st3 = con3.prepareStatement(sq3);
					st3.setTimestamp(1, new java.sql.Timestamp(kuup.getTime()));
					ResultSet rs3 = st3.executeQuery();
					while (rs3.next()) {
						MatrixLine ml = new MatrixLine();
						ml.setCustID(accountnum);
						ml.setProdID(rs3.getString("toode").trim());
						ml.setPrice(rs3.getDouble("kogus"));
						ml.setStartDate(kuup + "");
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
		System.out.println("L5 "  + result.size());
		
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
		}

	public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr) {
		}

	

	   public String getSaveDebtInfo() {
		   String cmd =  "   SELECT  kood, payer  FROM Customer  where statuscode in (90)" ;

		   
		   String cmd3 =  "  select c.payer, c.kood,(select overduesum from Customer  b where  b.kood = c.payer) as debt " +
					" from Customer c where c.statuscode in (90)  and (select overduesum from Customer  b where  b.kood = c.payer) != 0" ;


			if (isDebug()) System.out.println(cmd);
			TreeMap custs = new TreeMap();




			StringBuffer products = new StringBuffer();
			try {

		    Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd3);

			while (rs.next()) {
				String maks = rs.getString("payer");
			String kood = rs.getString("kood");
			double volg = rs.getDouble("debt");
			Customer custw = null;
			if(maks != null){
				 custw = new Customer (maks.trim());
				custw.setDebt(volg);
				if(volg>0){
					custs.put(maks.trim(),custw);
				}
			}
			if(kood != null){
				 custw = new Customer (kood.trim());
				custw.setDebt(volg);
				if(volg>0){
					custs.put(kood.trim(),custw);
				}
			}
		
			
			}

			//siin pannakse peale PLOKKID

			rs = st.executeQuery(cmd);
			while (rs.next()) {
				String kood = rs.getString("kood").trim();
				String payer = rs.getString("payer");
			    Customer cus =  (Customer)custs.get(kood);
			    if(cus != null ){
			    	cus.setSalesBlockT(true);
			    }else{
			    	Customer cusnew = new Customer(kood);
			    	cusnew.setSalesBlockT(true);
			    	custs.put(kood,cusnew);
			    }
			    if(payer != null){
			    	 cus =  (Customer)custs.get(payer.trim());
				    if(cus != null){
				    	cus.setSalesBlockT(true);
				    }else{
				    	Customer cusnew = new Customer(payer.trim());
				    	cusnew.setSalesBlockT(true);
				    	custs.put(payer.trim(),cusnew);
				    }
			    }
			 }

			rs.close();
			st.close();
			con.close();
			
			} catch (Exception e) {
			getParent().showError(e);
			}
			
			
			
			Iterator     codes = custs.values().iterator();
			while (codes.hasNext()){
			Customer cust = (Customer)codes.next();
			String bl = "0";
			if(cust.isSalesBlockT()){
				bl = "2";
				}

             products.append(cust.getID() + ";" + getParent().getNF().format(cust.getDebt()) + ";" + bl + "\n");
			}
	       
			return products.toString();

	      }

	public String getSaveStockInfo() {

		String cmd = "select * from StockBalance";

		if (isDebug())
			System.out.println(cmd);

		StringBuffer products = new StringBuffer();
		try {

			Connection con1 = getNewConnection();
			Statement st = con1.createStatement();
			ResultSet rs = st.executeQuery(cmd);

			while (rs.next()) {

				String kood = rs.getString("toode").trim();
				double kogus = rs.getDouble("stock");
				String dealer = rs.getString("LADU").trim();

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
		    
		try {

			
			Connection con = DriverManager.getConnection(url, "mjuser", ("usEr4Mj"));
		Statement	stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(" select * from SalesRep");
			while (rs.next()) {
				String kood = rs.getString("Haldur").trim();
				Agent agent = new Agent(kood);
				String name = rs.getString("Nimi").trim();
				agent.setName(name);
				agent.setLanguage("LAT");
				agent.setCountry("lv");
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
						StringTokenizer st = new StringTokenizer((String) agents1.get(a),",");
						while (st.hasMoreTokens()){
							agent.addDubbler(st.nextToken());	
						}
									}
				}

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
		
		return agents;
	}

	public String getDistribution() {
		return null;
	}
	
	
	private void otpQtys(String optqtys){
		  
		  
		  String rou = " select ItemCode, ObjectID, QtyLow  from OrderQtyCtrl where ObjectID != '100'  order by ItemCode,ObjectID,OPriority " ;
	    	 StringBuffer sb = new StringBuffer();
	    	
	    	 System.out.println(rou);
	    	 
	    	 sb.append(optqtys);
	    	 
	    	 
	    	 try {
	    		 
	    	 	Connection con = getNewConnection();
	 			Statement st = con.createStatement();
	 			
	 			
	 			ResultSet rs = st.executeQuery(rou);
	 			while (rs.next()) {
	 				String kood = rs.getString("ItemCode").trim();
	 				String muu =  rs.getString("ObjectID").trim();
	 				String kog =  rs.getString("QtyLow").trim();
	 				sb.append(kood+";"+muu+";"+kog+"\n");
	 			}
	 			rs.close();
	 			st.close();
	 			con.close();
	 			  if(sb.length() > 2){
					  	try{
					  		 Converter.saveFileInUTF8( "optamounts.txt", Converter.enCodeEst(sb.toString()));
					  	}catch (Exception e){	    		
					  	}	      
					   }
			    
			} catch (Exception e) {
				getParent().showError(e);
			}
	 			
	}
	private TreeMap agent1 = new TreeMap();

	public TreeMap getCustomers(Agent agent) {
		
		
		try{
			   Converter.getManagersReplacements(agent1, rcompany, user, pass, false); 
				
		}catch(Exception e){
			
		}
		
		 File fff = new File(this.getExcelDir() + "sortiments.xlsx" );
	        TreeMap groups = new TreeMap(); 
	    	try{
	            FileInputStream ff = new FileInputStream(fff);
	    		XSSFWorkbook workbook = new XSSFWorkbook((ff));
	    		XSSFSheet sheet = workbook.getSheetAt(1);
	    		XSSFRow row = sheet.getRow(0);
	    	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
	    		
	    		
	    		for(int a = 1 ; a < sheet.getLastRowNum();a++){
	    			try{
	    			//	Loeme sisse rea
	    				row = sheet.getRow(a);
	    				if (row != null && row.getCell((short) 0) != null) {
	    				


	    	    			   String kood = null;
	    	    			  if(row.getCell((short)0) != null){
		    	    			  	try{
		    	    			  	  kood = row.getCell((short)0).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			   	  kood = row.getCell((short)0).getNumericCellValue()+"";  
		    	    	    		} 
		    	    			  	 if(kood.indexOf(".")!= -1){
		    	    			  	 	 kood = (kood.substring(0,kood.indexOf(".")));	
		    	    			  	 }

		    	    			  	 String sort = null;
		    	    			  	try{
		    	    			  		sort = row.getCell((short)1).getStringCellValue();  
			    	    			  	 }catch (Exception e){
			    	    			  		sort = row.getCell((short)1).getNumericCellValue()+"";  
			    	    	    		} 
			    	    			  	 if(sort.indexOf(".")!= -1){
			    	    			  	 	 sort = (sort.substring(0,kood.indexOf(".")));	
			    	    			  	 }
			    	    			  	 sort = sort.replace(',', '|').replace(',', '|').replace(',', '|').replace(',', '|');
			    	    			  	 
			    	    				String gg = sort.trim();
			    	 					if(gg.length() < 4){
			    	 						gg = gg + "x";
			    	 					}
			    	 					if(gg.length() < 4){
			    	 						gg = gg + "x";
			    	 					}
		    	    			  	 groups.put(kood,gg);
	    	    			  
	    	    				
	    				}	   
	    				}
	   			}catch (Exception e){
	    				//System.out.println(row.getCell((short)a).getNumericCellValue());
	    				e.printStackTrace();
	    			}
	    		}
	    		
	    		
	    
	    	 }catch (Exception e){
	    		e.printStackTrace();
	    	}
	        
	        
	    	 
	        
	    	// System.out.println("groups to string " + groups.toString());
	        
	    	 String rou = "select CustCode, DelivDOW, l.RouteID, r.RouteName, DRDEPDOW from RoutePSEL l "
	    	 		+ "inner join RouteList r  on l.RouteID = r.ROuteID order by CustCode, DelivDOW desc " ;
	    	 TreeMap routes = new TreeMap();
	    	 
	    	 try {
	    		 
	    	 
	 			Connection con = getNewConnection();
	 			Statement st = con.createStatement();
	 			ResultSet rs = st.executeQuery(rou);
	 			
	 			while (rs.next()) {
	 				String kliendikood = rs.getString("CustCode").trim();
	 				String inf = "'" + rs.getString("DelivDOW").trim() + "'";
	 				String rid  =   rs.getString("RouteID").trim();
	 				String rname = rs.getString("RouteName").trim();
	 				String yld = rs.getString("DRDEPDOW").trim();
	 				String aaa = "('" + rid + "' - " + yld + ") " + rname ;
	 				if(routes.get(kliendikood) != null){
	 					String ddd = routes.get(kliendikood)+"";
	 					if(ddd.contains(aaa)){
	 						
	 						int ind = ddd.indexOf("|");
	 						
	 						if(ind != -1 ){
	 							int in = ddd.indexOf(aaa);
	 							//juhul kui route on pärast | tähte siis paneme peal eseda tähte kui mitte siis algusesse
	 							if(in > ind){
	 								// on peale
	 								ddd= ddd.substring(0,ind +1) + inf+"-"+ddd.substring(ind +1);
	 							}else{
	 								ddd = inf +"-"+ ddd;
	 							}
	 							
	 							
	 						}else{
	 							ddd = inf +"-"+ ddd;	
	 						}
	 						
	 						
	 					}else{
	 						ddd = ddd + "|" + inf +" "+ aaa;
	 					}
	 					
	 					routes.put(kliendikood, ddd);
	 					//7	010	LUDZA
	 					//6	079	KARSAVA
	 					//5	079	KARSAVA

	 					
	 				}else{
	 					routes.put(kliendikood, inf + " " + aaa);
	 				}
	    	 		
	 			}
	 		rs.close();
	 		st.close();
	 		con.close();
	 	
              }catch(Exception r){
	    		r.printStackTrace(); 
	    	 }
		
		
		
		TreeMap customers = new TreeMap();
		
		  String cmd = "select klient,toode,max(kuupaev)AS kuupaev,max(kogus) AS arvkogus, sum (kogus) as pluss ,(arve) as doknr " +
				" from SalesGeneral group by  klient,toode,arve ORDER BY klient,toode,arve ";
		
		if (isDebug())
			System.out.println(cmd);

		custProds = new Vector();
		HashMap kliendid = new HashMap();

		try {
			Connection con = getNewConnection();
			Statement st = con.createStatement();
			
			//st.executeQuery("delete from zz_tbl_Tempkaive3viimast");
		//	st.executeQuery("insert into zz_tbl_Tempkaive3viimast select * from zz_Tempkaive3viimast ");
			
			
			
			ResultSet rs = st.executeQuery(cmd);
			int cc = 0;
			while (rs.next()) {
				cc++;
				String kliendikood = rs.getString("klient").trim();
				String tootekood = rs.getString("toode").trim();
				String kuup = rs.getString("kuupaev");
				double kogus = rs.getDouble("arvkogus");
				double pluss = rs.getDouble("pluss");
				String doknr = rs.getString("doknr").trim();

				CustProd cp = new CustProd(kliendikood, tootekood, kogus,
						pluss, 0, kuup, 0, 0, 0, " ");
				if(doknr.trim().length() > 0){
					cp.setAbiKood(doknr);
				}
				
				custProds.add(cp);

			}
			rs.close();
			st.close();
			con.close();

		} catch (Exception e) {
			getParent().showError(e);
		}

		try {

			String cmd2 = "select * from SalesByCust";
			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd2);

			while (rs.next()) {

				String kliendikood = rs.getString("klient").trim();
				double kilod = rs.getDouble("kilod");

				double rsumma = rs.getDouble("summa");
				
				kliendid.put(kliendikood, rsumma + "|" + kilod);

			}
			rs.close();
			st.close();
			con.close();

		} catch (Exception e) {
			getParent().showError(e);
		}

		
		try {
			Connection con = getNewConnection();
			Statement st = con.createStatement();

			String cmd2 = "select c.*, (select overduesum from Customer  b where  b.kood = c.payer) as overf " +
					" from Customer c where c.statuscode in (20,10,90)    "; //and IsContacted = 1

			if (isDebug())
				System.out.println(cmd2);
			ResultSet rs = st.executeQuery(cmd2);
			HashMap custMap = new HashMap();

			while (rs.next()) {

				String klgrupp = rs.getString("klgrupp");
				if (klgrupp != null) {
					klgrupp = klgrupp.trim();
				} else {
					klgrupp = "";
				}
				String accountnum = rs.getString("kood").trim();
				Customer cust = new Customer(accountnum);

				
				String name = rs.getString("nimi").trim();

String statusCode = rs.getString("statusCode");
				
				if(statusCode != null && statusCode.trim().length() > 0 &&
						statusCode.trim().equals("90")){
					//cust.setSalesBlockT(true);
				}
				
				String aadress1 = rs.getString("aadress1");
				if (aadress1 != null) {
					aadress1 = aadress1.trim();
				} else {
					aadress1 = " ";
				}
				String aadress2 = rs.getString("aadress2");
				if (aadress2 != null) {
					aadress2 = aadress2.trim();
				} else {
					aadress2 = " ";
				}
				if(routes.get(accountnum) != null){
					cust.addToCustDataVec(cust.getID()+ ";routeinfo; ;" + routes.get(accountnum));
				}

				cust.setName(Converter.enCodeEst(name) + " " + accountnum + " " + Converter.enCodeEst(aadress1));

				cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress1 + " " + aadress2),"Latvia"));
				if(aadress1.indexOf(",") != -1 ){
					cust.restStreet = (aadress1.substring(0,aadress1.indexOf(","))).trim();	
					cust.restCity = (aadress1.substring(aadress1.indexOf(",")+1)).trim();
					
				}else{
					cust.restStreet = (aadress1).trim();
						
				}
				cust.setCountry("Latvia");
				String visit = rs.getString("visit");
				if (visit != null) {
					cust.setVisit(visit.trim());
				}
				if (kliendid.get(cust.getID()) != null) {
					String sss = ((String) kliendid.get(cust.getID()));
					sss = sss.substring(0, sss.indexOf("|"));
					cust.setSold(Double.parseDouble(sss));
				}
				String info = rs.getString("info");
				if (info != null) {
					cust.setInfo(info.trim());
				}
				String ag = rs.getString("agent").trim();
                cust.addAgent(new Agent(ag));
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
				


				cust.setPayer(rs.getString("payer").trim());

				String maksjanimi = rs.getString("payername");
				if (maksjanimi != null && maksjanimi.trim().length() > 0) {
					maksjanimi = maksjanimi.trim();
				}

				String blokk = rs.getString("blok2");

				if (!blokk.trim().equals("0")) {
					cust.setSalesBlock(true);
				}
				
				String kett = rs.getString("kett").trim();
				cust.setChain(kett);
				
				
				String ketinimi = rs.getString("ketinimi");

				String kett2 = rs.getString("kett2");

				String sortiment = (String)groups.get(accountnum);
				if (sortiment != null && sortiment.trim().length() > 0) {
					 cust.addFixSortCode(sortiment.trim());
				}

				
				String PListCode = rs.getString("PListCode");

				
				String jelp = rs.getString("asendus");
				if (jelp != null && jelp.trim().length() > 0) {
					cust.addAgent(new Agent(jelp.trim()));
				}

				String inff = rs.getString("personalinfo");
				if (inff != null && inff.trim().length() > 0) {
					cust.setPersonalData(inff.trim());
				}
				String city = rs.getString("Asula");
				if (city != null && city.trim().length() > 0) {
					//cust.restCity =(city.trim());
					if(cust.restStreet.contains(city.trim())){
					//	cust.restStreet = cust.restStreet.replaceAll(city.trim(), " ").trim();
					}
				}

				
				String noprice = rs.getString("HidePrices");
				
				String orderType = rs.getString("OrderType");
				if(orderType != null && orderType.trim().length() > 0){
					//cust.setCity("Type :" + orderType.trim());
					cust.setCustType(orderType.trim());
				}
				
				String overf = rs.getString("overf");
				

				//if (noprice.trim().equals("0")) {
					if(kett != null){
						cust.setGroup(kett.trim());
					}
					
					if ( PListCode != null && PListCode.trim().length() > 0 ) {
						if(cust.getGroup()!= null && cust.getGroup().trim().length() > 1){
							 cust.setGroup(cust.getGroup() + "|" + PListCode.trim());
						}else{
							 cust.setGroup(PListCode.trim());
						}
					}
					if (overf != null && overf.trim().length() > 0) {
						try{
							cust.setDebt(Double.parseDouble(overf.trim()));
						}catch(Exception e){
						}
					}
				//}else{
					//System.out.println(cust.getName());
						
				//}


				
				
				customers.put(name + " " + accountnum, cust);
				custMap.put(cust.getID(), cust);
			}

		


		} catch (Exception e) {
			getParent().showError(e);
		}
		
		//System.out.println("kliendid " + customers.size());
		
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
		String sql = "select * from ProductGrp";
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);

		while (rs.next()) {
			tm.put(rs.getString("tootegrupp").trim(), Converter.enCodeEst(rs.getString("grupinimi").trim()));
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
		
		
		String optqtys = "";


		Hashtable stocks = new Hashtable();

		// Leiame Laoseisud
		try {

			String sql = " select * from StockBalance";

			if (isDebug())
				System.out.println(sql);
			Connection con1 = getNewConnection();
			Statement stm = con1.createStatement();
			ResultSet rst = stm.executeQuery(sql);

			while (rst.next()) {
				String toode = rst.getString("toode").trim();
				int seis = (int) Double.parseDouble(rst.getString("stock")
						.replace(',', '.'));
				String ladu = rst.getString("ladu").trim();
				if (ladu.equals("100") && seis > 0) {
					stocks.put(toode, seis + "");
				}

			}
			rst.close();
			stm.close();
			con1.close();

		} catch (Exception e) {
			getParent().showError(e);
		}

		// Leiame soodused

		String cmd = "select * from Product where IsActive = 1";

		if (isDebug())
			System.out.println(cmd);
		TreeMap products = new TreeMap();
		try {
			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);

			while (rs.next()) {

				String kood = rs.getString("kood").trim();
				Product prod = new Product(kood);
				String nimetus = rs.getString("nimetus").trim();
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

				prod.setInCases((rs.getDouble("tykke")));
				prod.setPrice((rs.getDouble("hind")));
				prod.setVat("21");

				
				 optqtys = optqtys + prod.getID()+";103;1"+ "\n";
				 if(prod.getInCases() > 0){
					// optqtys = optqtys + prod.getID()+";100;"+((int)prod.getInCases())+ "\n";
				 }
				 

				
				String grupp = rs.getString("grupp");
				if (grupp != null && grupp.trim().length() > 0) {
					prod.setGroup(grupp.trim());
				}

				String into = rs.getString("inunit");
				String lisa = "";
				if (into == null || into.trim().length() == 0) {
					prod.setInUnit(prod.getInCases());

				} else if (into.trim().equals("1") || into.trim().equals("4")) {
					prod.setInUnit(prod.getInCases() / 2);
					lisa = " " + (int) prod.getInCases() + "/"
							+ (int) prod.getInCases() / 2;
				} else if (into.trim().equals("3") || into.trim().equals("5")) {
					prod.setInUnit(prod.getInCases() / 4);
					lisa = " " + (int) prod.getInCases() + "/"
							+ (int) prod.getInCases() / 2;
				}

				prod.setName( Converter.enCodeEst(nimetus + lisa));
			

				String ean = rs.getString("ean");
				if (ean != null && ean.trim().length() > 7) {
					prod.setEAN(ean.trim());
				}

				String unit = rs.getString("unit");

				if (unit != null && unit.trim().length() > 0) {
					unit = unit.trim();
					prod.setUnit(unit);

				}

				if (stocks.get(kood) != null) {
					prod.setStock(Double.parseDouble((String) stocks.get(kood)));
				}

				products.put(prod.getName(), prod);
				// }
			}
			rs.close();
			st.close();
			con.close();
			
			otpQtys(optqtys);
			

		} catch (Exception e) {
			getParent().showError(e);
		}

		return products;
	}

	public Vector getCustomerProductsList(String agentCode) {
		return custProds;

	}

	public void setBulkSales(Map agents) {
		  
	      int mm = Calendar.getInstance().get(Calendar.MONTH)+ 1;
	      int yy = Calendar.getInstance().get(Calendar.YEAR);
	      int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
	      int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
	     
	      
   TreeMap<String,Toode> eesmark = new TreeMap<String,Toode>();
   TreeMap<String,Toode> eile = new TreeMap<String,Toode>();
		
		try {
			Connection con = getNewConnection();
			Statement stmt = con.createStatement();
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_MONTH, 1);
		
		
	  String   cmd3 = "select haldurid,kg,summa from SalesYesterday";
try{
	//stmt = con.createStatement();
ResultSet rs2 = stmt.executeQuery(cmd3);

while (rs2.next()) {
	String agentID = rs2.getString("haldurid").trim();
	double doneYkg = rs2.getDouble("kg");
	double doneYsum = rs2.getDouble("summa");
	Toode tt = new Toode("");
	tt.setDonecur(doneYsum);
	tt.setDonevol(doneYkg);
	eile.put(agentID, tt);	
}

 rs2.close();
 stmt.close();	
 }catch (Exception e){
   e.printStackTrace();
 }
	//System.out.println("Eile "  + eile);	
		
String cmd = "select Haldur ,sum(Kilod) as kil , sum(Summa) as summ from SalesByRep  group by Haldur";
				
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			
			while (rs.next()) {
				String agentID = rs.getString("haldur").trim();
				
				Agent agent = (Agent) agents.get(agentID);
				if (agent != null) {
					agent.setKgSales((int) rs.getDouble("kil"));
					agent.setMonthSales((int) rs.getDouble("summ"));
						}
					}
			
			
			Iterator itera = agents.keySet().iterator();
			while (itera.hasNext()){
				String agentID = (String) itera.next();
				
				
				Agent agent = (Agent) agents.get(agentID);
				
				
				
				if(eesmark.get(agentID) != null){
					 Toode tt = eesmark.get(agentID);
					 
					 double psales = 0;
               	  
               	  try{
               		  psales = tt.getDonecur();
               	  }catch(Exception e){
               		  
               	  }
                     
               	  double  tehtudsalperc = agent.getMonthSales()/psales;
               	  double plaanitavMyykSal = d2 * agent.getMonthSales()/dd;
               	  double  prognosalperc = plaanitavMyykSal/psales;
               	  String asi = "";
                     if(dd > 2){
                   	  asi = asi + "Plan/prognoze ():";
                         asi = asi + "     " +NF.format(tehtudsalperc*100) + "% / " + NF.format(prognosalperc*100)  + " %||";  
                     }
                   
                     asi = asi + "Target : " +(int)psales + " ||";
					          agent.setStart(asi);
				          }	
				
				
			
				
				
				if(eile.get(agentID) != null){
					 Toode tt = eile.get(agentID);
					 
					 double psales = 0;
					 double pvol = 0;
	           	  
	           	  try{
	           		  psales = tt.getDonecur();
	           	  }catch(Exception e){
	           		  
	           	  }
	                 
	           	  try{
	           		  pvol = tt.getDonevol();
	           	  }catch(Exception e){
	           		  
	           	  }
	           	  String asi = "";
	                
	               	  asi = asi + "Vakar : " +NF.format(psales) + " € " + NF.format(pvol)  + " kg ||";  
	               
	                        agent.setStart(agent.getStart() + asi);
	                        System.out.println(agent.getStart());
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
		String sql = "select * from OverdueInvoices d inner join Customer c on c.Kood=d.Custcode where HidePrices = 0";

		if (isDebug())
			System.out.println(sql);
		Connection con = getNewConnection();

		stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		Calendar cal = Calendar.getInstance();
		Date now = new Date(cal.getTimeInMillis());

		while (rs.next()) {
			Debt debt = new Debt();
			debt.setCustID(rs.getString("PayerCode").trim());
			debt.setDoknr(rs.getString("InvoiceNo").trim());
			Date date = getDate(rs.getString("InvoiceDate").trim());
			Date duedate = getDate(rs.getString("DueDate").trim());
			debt.setDuedate(duedate);
			debt.setDate(date);
			debt.setSum(rs.getDouble("DebtAmt"));
			debt.setDue(rs.getString("OverdueDays"));
			if (duedate.getTime() < now.getTime()) {
				list.addElement(debt);
			}
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
		return "LeiburLat";
	}

	public String getSubjectFilesTo() {
		return this.subjectFilesTo;
	}

	public String getDaySalesData(Agent agent) {
		return null;
	}

	public String getPilotDayData(Agent agent) {
		return null;
	}

	public void insertDefective(DefectiveWrapper wr) {
		}

	public void insertDistribution(DistEvalListWrapper wr) {
		}


    public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		File f = new File(this.getExcelDir() + "sortiments.xlsx");
		try {
			
			
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheetAt(0);
			XSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(3);
			// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD Sortimendi tähised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 3; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					String gg = row.getCell((short) a)
							.getStringCellValue().trim();
					if(gg.length() < 4){
						gg = gg + "x";
					}
					if(gg.length() < 4){
						gg = gg + "x";
					}
					groups.put(a + "", gg);
				} catch (Exception e) {
					 System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			 System.out.println("groups " + groups.size() + " " + groups.toString());

			for (int i = 4; i <= sheet.getLastRowNum(); i++) {
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
                    	
					// Siin läheme reasisse rokkima ja vaatama kas se toode
					// läheb kaasa
					for (int a = 4; a < row.getLastCellNum(); a++) {

						String group = (String) groups.get(a + "");
						if (group != null && group.trim().length() > 0) {

							String custID = " ";
							String clGroup = " ";
							String prodGroup = " ";

							String go = null;
							clGroup = group;

							MatrixLine mline = new MatrixLine();
							// Vaatame kas selles veerus on sellel tootel pandud
							// mingi tähis
							if (row.getCell((short) a) != null) {

								try {
									go = (int) (row.getCell((short) a)
											.getNumericCellValue()) + "";
								} catch (Exception e) {
									go = (row.getCell((short) a)
											.getStringCellValue());
								}

							}

							if (go != null && ( go.toLowerCase().equals("1") || go.length() > 3)) {

							// System.out.println( a + " " + prodID + " " +
								// clGroup + " " + go);

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

		return result;

	}


	private Date getDate(String kuup) {

		Calendar cal = Calendar.getInstance();
		int YEAR = Integer.parseInt(kuup.substring(0, 4));
		int MONTH = Integer.parseInt(kuup.substring(5, 7))-1;
		int DAY = Integer.parseInt(kuup.substring(8, 10));
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
	private String url = "jdbc:odbc:hm";
	private Connection con;
	private Vector custProds = null;
	private Statement stmt;
	private String subjectFilesTo = null;
	private TreeMap agents1 = new TreeMap();
	private NumberFormat NF = NumberFormat.getInstance();
	private TreeMap dealers = new TreeMap();
	HashMap aHash = new HashMap();

	public Vector getPriceMatrixData() {

		
		Vector lines = new Vector();
		Vector check = new Vector();
		TreeMap products = new TreeMap();
		
		try {
			String cmd = "select * from PriceList";
			System.out.println(cmd);
			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);
			
			while (rs.next()) {

				String clGroup = rs.getString("PListCode");
				String prodID = rs.getString("ItemCode");
				double price = rs.getDouble("SalesPrice");


				MatrixLine mline = new MatrixLine();

				if (mline.getCustID() == null && clGroup != null
						&& clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
				}

				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}



				if (price != 0) {
					mline.setPrice(price);
				}
				if (!check.contains(clGroup.trim() + prodID.trim())) {
					lines.add(mline);
				}

				check.addElement(clGroup.trim() + prodID.trim());
			}		} catch (Exception e) {
			e.printStackTrace();
		}


		try {

			String sql = "select * from PromoPrice";

			con = getNewConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {

				String clGroup = rs.getString("Kett");
				String stDate = rs.getString("AlgusPaev").substring(0, 10);
				String endDate = rs.getString("LoppPaev").substring(0, 10);
				String prodID = rs.getString("TooteKood");
             	double discount = rs.getDouble("Allahindlus");
				double price = rs.getDouble("Hind");


				MatrixLine mline = new MatrixLine();

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

				if (stDate != null && stDate.trim().length() > 0) {
					mline.setStartDate(stDate);
				}
				
				mline.setDiscount(discount);

				if (price != 0) {
					mline.setPrice(price);
				}
				if (!check.contains(clGroup.trim() +" "+ prodID.trim())) {
					lines.add(mline);
				}

				check.addElement(clGroup.trim() +" "+ prodID.trim());
			}

			rs.close();
			this.connectionDown();
		} catch (SQLException ex) {
		}
		return lines;
	}

	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}
}
