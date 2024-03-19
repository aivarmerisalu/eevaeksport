package eevaeksport.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
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

public class KosmelitaDBDriver extends AbstractDBDriver implements Manager, Promo,
		BulkUpdater, Discounter, PictureMaker {
	public KosmelitaDBDriver(EksportPanel parent) {
		super(parent);
		if (parent.getParentFrame().isDaySales()) {
			this.subjectFilesTo = getParent().getString("senddatamsg");
		} else {
			this.subjectFilesTo = parent.getString("filemsg");
		}
		String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
		try {
			Class.forName(databaseDriverName).newInstance();
			con = DriverManager.getConnection(url, "PDA", getParent().getProps().getProperty("dsn.pass"));
			stmt = con.createStatement();
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
	
	  @Override
		public void PictureMaker() {
			makePictureCat();
			 System.exit(0);
			
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
//		         	 Lõppes kustutamine 
		  		 
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
		               System.out.println("pilt olemas " + ff2[i]);
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
		  
	
	public String getDealerCustomerCSV(String agent) {
		return null;
	}

	public Vector getLastThreeData() {

		Vector result = new Vector();
		// Siin loeme igale kliendile kylge viimase kolem korra ostud
	/*	try {

			Connection con = getNewConnection();
			Statement st = con.createStatement();

			String cmd2 = "select * from kliendid_baas";

			if (isDebug())
				System.out.println(cmd2);
			ResultSet rs = st.executeQuery(cmd2);

			while (rs.next()) {
				String accountnum = rs.getString("kood").trim();
				
			//	paneme tuleviku siia ette 
				String sqlt = "select ItemNumber as toode ,OrderedAmount as kogus , RequestedDeliveryDate as kuup ,Tarne "
						+ " from futureOrders_view  where CustomerCode = '"
						+ accountnum + "'  order by toode";

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
					ml.setStartDate(rs3.getString("kuup").substring(4).replaceAll("-", "")
							+ "-" + Converter.cleanLitLatEst(rs3.getString("Tarne")));
					ml.setCliGroup("green");
					result.add(ml);
				}
				rs3.close();
				st3.close();
				String asi ="5";
				if(hasTom){
					asi = "4";
				}

				String sq1 = "select top "+asi+" kuupaev from  tempkaive3viimast where klient ='"
						+ accountnum
						+ "' group by klient ,kuupaev  order by klient,kuupaev desc";
				Connection con2 = getNewConnection();
				Statement st2 = con2.createStatement();

				ResultSet rs2 = st2.executeQuery(sq1);
				while (rs2.next()) {
					String kuup = rs2.getInt("kuupaev")+ "";
					String sq3 = "select toode , kogus from  tempkaive3viimast where klient ='"
							+ accountnum
							+ "' and kuupaev ="
							+ kuup
							+ " order by toode";
					  con3 = getNewConnection();
					 st3 = con3.createStatement();
					 rs3 = st3.executeQuery(sq3);
					while (rs3.next()) {
						MatrixLine ml = new MatrixLine();
						ml.setCustID(accountnum);
						ml.setProdID(rs3.getString("toode").trim());
						ml.setPrice(rs3.getDouble("kogus"));
						ml.setStartDate(kuup.substring(4));
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
*/
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

	
	
	public String getSaveDebtInfo() {
		return null;
	}

	public String getSaveStockInfo() {

		String cmd = "laosaldo";

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
		    	e.printStackTrace();
		    }
		try {
			Connection con1 = getNewConnection();
			Statement stmt = con1.createStatement();
		

			ResultSet rs = stmt.executeQuery("Select * from Agents");
			while (rs.next()) {
				String kood = rs.getString("Code").trim();
				System.out.println("kood " + kood);
				Agent agent = new Agent(kood);
				String name = rs.getString("Name");
				if(name == null) {
					name = kood;
				}else {
					name = name.trim();
				}
				agent.setName(name + " " + kood);
				agent.setLanguage("LIT");
				agent.setCountry("lt");
				//agent.setPassWord("0000");
             

				agents.put(agent.getName(), agent);
				aHash.put(agent.getID(), agent);
				agent1.put(agent.getID(), agent);
			}
			rs.close();
			stmt.close();
			con1.close();

		} catch (Exception e) {
			getParent().showError(e);
		}
		
		System.out.println("p�mmutan " + rcompany);
		 if(rcompany != null){
		    	Converter.sendAgentJson(agents, rcompany, false);
		    }
		 
		return agents;
	}

	public String getDistribution() {
		return null;
	}
	private TreeMap agent1 = new TreeMap();
	
	
	
	public TreeMap getCustomers(Agent agent) {
		TreeMap customers = new TreeMap();
		try{
			if(rcompanyx != null)
			   Converter.getManagersReplacements(agent1, rcompanyx, user, pass); 
				
		}catch(Exception e){
			
		}
		

		custProds = new Vector();
		HashMap kliendid = new HashMap();

	
TreeMap groups = new TreeMap();

		
		try {
			Connection con = getNewConnection();
			Statement st = con.createStatement();

			String cmd2 = "select * from Customers where CustName is not null";

			if (isDebug())
				System.out.println(cmd2);
			ResultSet rs = st.executeQuery(cmd2);
			HashMap custMap = new HashMap();

			while (rs.next()) {

				
				String accountnum = rs.getString("Custcode").trim();
				
				Customer cust = new Customer(accountnum);
				String name = rs.getString("CustName").trim();		
				String payer = rs.getString("PayerCode");
				if(payer != null && payer.trim().length() >0) {
					cust.setPayer(payer.trim());
				}
				
			
				cust.setName(Converter.enCodeEst(name));
				String group = rs.getString("CustGroup");
				if(group != null && group.trim().length() > 0) {
					cust.setGroup(group);
				}
			
				
				String aadress1 = rs.getString("Address");
				if (aadress1 != null) {
					aadress1 = aadress1.trim();
				} else {
					aadress1 = " ";
				}
				String aadress2 = rs.getString("City");
				if (aadress2 != null) {
					aadress2 = aadress2.trim();
				} else {
					aadress2 = " ";
				}
				cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress1 + " " + aadress2),"Lithuania"));

			
						
				Double debt = rs.getDouble("DebtDue");
				if(debt != null && debt != 0) {
					cust.setDebt(debt);
				}
				
			
				String ag = rs.getString("SalespersonCode");
				
				if (ag != null) {
                   ag = ag.trim();
					cust.addAgent(new Agent(ag));
				   if (ag != null && agent1.get(ag) != null){
						Agent aa = (Agent)agent1.get(ag);
					
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

				
				//(	Custcode       DebtDue  SalespersonCode    Contact 
			       // Phone    PaymentTerm  CreditLimit UsedCredit   CustDiscount Blocked     

				String inff = rs.getString("Contact");
				if (inff != null && inff.trim().length() > 0) {
					cust.setPersonalData(inff.trim());
				}
				 String info2 = rs.getString("phone");
			        if (info2 != null && info2.trim().length()>0) {
			        	if(cust.getPersonalData() != null && cust.getPersonalData().trim().length() > 2) {
                   	     cust.setPersonalData(cust.getPersonalData() +  "|Tel:" + info2.trim().replaceAll(";", ",").replaceAll("\\r\\n|\\r|\\n", " "));
                   	}else {
                   	     cust.setPersonalData( "Tel:" + info2.trim().replaceAll(";", ",").replaceAll("\\r\\n|\\r|\\n", " "));
                   	}
			         
			        }
				
                 
				String paym = rs.getString("PaymentTerm");
				if (paym != null && paym.trim().length() > 0) {
					cust.setPaymTerm(paym.trim());
				}
				
			
				
				String info = rs.getString("CreditLimit");
				if (info != null ) {
					cust.setCredit(info.trim());
				}

				
				Double used = rs.getDouble("UsedCredit");
				if (used != null && used > 0 ) {
					cust.setUsedCredit(used);
				}

				
				String blokk = rs.getString("Blocked");

				if (blokk != null && blokk.trim().equals("0")) {
					cust.setSalesBlock(true);
				}
			
				
				customers.put(name, cust);
				custMap.put(cust.getID(), cust);
			}

		} catch (SQLException e) {
			getParent().showError(e);
		}
		
		 if(rcompany != null){
		    	Converter.sendCustomerJson(customers, rcompany,false,user,pass,true);
		    }
		return customers;
	}

	private Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(url, getParent().getProps()
				.getProperty("dsn.user"),
				getParent().getProps().getProperty("dsn.pass"));
	}

	public TreeMap getProdGroups() throws SQLException {
	//	GroupCode   ParentGroupCode Description  
		Connection con = getNewConnection();
		TreeMap tm = new TreeMap();
		String sql = "select * from ProductGroups";
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);

		while (rs.next()) {
			String kood = rs.getString("GroupCode");
			String nimi = rs.getString("Description");
			if(nimi != null) {
				tm.put(kood.trim(), Converter.enCodeEst(nimi.trim()));
			}
			
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

	TreeMap<String,Double> prices = new TreeMap<String, Double>();
	
	public TreeMap getProducts(Agent agent) {

		Hashtable stocks = new Hashtable();

		String cmd = "select * from Products where Blocked != '1'";
		
		if (isDebug())
			System.out.println(cmd);
		TreeMap products = new TreeMap();
		try {
			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);

			while (rs.next()) {

				String kood = rs.getString("UniqueCode").trim();
				String abikood = rs.getString("ItemCode").trim();
				
				Product prod = new Product(kood);
				String nimetus = rs.getString("Description").trim() + " " + abikood;
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
				//UniqueCode  ItemCode     Description          
				//InCase      GroupCode  GroupSecCode UnitPrice  
				
				String unit = rs.getString("Unit");

				if (unit != null && unit.trim().length() > 0) {
					unit = unit.trim();
					prod.setUnit(unit);

				} else {
					prod.setUnit("vnt");
				}
				
				
				prod.setInCases((rs.getDouble("InCase")));
				

				String grupp = rs.getString("GroupCode");
				if (grupp != null && grupp.trim().length() > 0) {
					prod.setGroup(grupp.trim());
				}
				String grupp2 = rs.getString("GroupSecCode");
				if (grupp2 != null && grupp2.trim().length() > 0) {
					prod.setGroupSecond(grupp2.trim());
				}
				prod.setVat("21");
				Double hind = rs.getDouble("UnitPrice");
				if(hind != null && hind > 0) {
					prod.setPrice(hind);
					prices.put(kood, hind);
				}else {
					prod.setPrice(1);
				}
				prod.setName(nimetus);
			
				 
              //   Inventory                LongTermQty              TesterQty                ReservedQty 	
				
				
				String ean = rs.getString("EAN");
				if (ean != null && ean.trim().length() > 7) {
					prod.setEAN(ean.trim());
				}

			  Double stock = rs.getDouble("Inventory");
			  if(stock > 0) {
				  prod.setStock(stock);
			  }

				if (stocks.get(kood) != null) {
					prod.setStock(Double.parseDouble((String) stocks.get(kood)));
				}

				String newI = rs.getString("NewItem");
				if (newI != null && newI.trim().equals("1")) {
					//prod.setStatusNew("1");
					prod.setStatusTop("1");
				}
				
				String itemLevel  = rs.getString("ItemLevel");
				if (itemLevel != null) {
					prod.setName(itemLevel.trim() + " " +prod.getName());;
				}
				
				String vat  = rs.getString("VatCode");
				if (vat != null && vat.trim().length() > 1) {
					prod.setVat(vat.trim());
				}
				
				
				products.put(prod.getName(), prod);
				// }
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
	     
			/*
			 * String cmd = "SELECT omrid.klient,rid.toode FROM omrid INNER JOIN rid ON omrid.tasudjrk = rid.dokumnr WHERE omrid.rid2obj='" +
			 * agentCode + "' AND (len(omrid.klient) > 1) AND (len(rid.toode) > 2) AND omrid.tasudtyyp='G' AND omrid.reakuupaev >= " +
			 * getStartingTime() + " GROUP BY omrid.klient, rid.toode ORDER BY omrid.klient, rid.nimetus";
			 */
		 
		//CustomerProducts
			//CustomerCode               ProductCode Qty         LastDate                InvoiceNumber           LastPrice                

			String cmd = "Select * from CustomerProducts ";

			if (isDebug()) {
			    System.out.println(cmd);
			}

			Vector custProds = new Vector();

			try {
				Connection con = getNewConnection();
				Statement st = con.createStatement();
				ResultSet rs = st.executeQuery(cmd);

			    while (rs.next()) {
				String kliendikood = rs.getString("CustomerCode").trim();
				String tootekood = rs.getString("ProductCode").trim();
				   double kogus = Double.parseDouble(rs.getString("Qty").trim());
				 String kuupaev = rs.getString("LastDate");
				 String doknr = rs.getString("InvoiceNumber");
		     
		        double hind = Double.parseDouble(rs.getString("LastPrice").trim());
		     

		                if (kliendikood.length() > 1 && kuupaev != null) {
		                  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,kuupaev.substring(5,10),hind,0,0," ");
		                  cp.setAbiKood(doknr);
		                  custProds.add(cp);
		                }
			    }

		            rs.close();
		            st.close();
		            con.close();

			} catch (Exception e) {
			    getParent().showError(e);
			}
		       
			return custProds;
		    }

	   public void setBulkSales(Map agents){
       
   		String cmd =  " SELECT * from Agents" ;
   	
   	
   		
   		try {
   			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);
	
   		
   			while(rs.next()) {
   				String agentID = rs.getString("Code").trim();
   				Agent agent = (Agent)agents.get(agentID);
   				if (agent != null){
   					agent.setMonthSales((int)rs.getDouble("SalesCurrency"));
   				}
   			}
   			rs.close();
   			st.close();
   			con.close();
   			
   		} catch (Exception e) {
   			getParent().showError(e);
   		}
   		connectionDown();
   		
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
		String sql = "select * from OpenInvoices";
//DocumentNumber                    DocumentDate PayDate    OpenAmount     CustomerCode        PayerCode  
		

		if (isDebug())
			System.out.println(sql);
		Connection con = getNewConnection();

		stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		Calendar cal = Calendar.getInstance();
		Date now = new Date(cal.getTimeInMillis());

		while (rs.next()) {
			Debt debt = new Debt();
		
			debt.setDoknr(rs.getString("DocumentNumber").trim());
			Date date = getDate(rs.getString("DocumentDate").trim());
			debt.setDate(date);
			String payd = rs.getString("PayDate");
			if(payd != null) {
				Date duedate = getDate(payd.trim());
				debt.setDuedate(duedate);	
			}
			
			debt.setSum(rs.getDouble("OpenAmount"));
            debt.setCustID(rs.getString("CustomerCode").trim());
             String payer = rs.getString("PayerCode");
             if(payer != null) {
            	 debt.setPayerID(payer.trim());
             }
		
		//	debt.setDue(rs.getString("paevi"));
			
			if(payd != null) {
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
		return "Leibur";
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

		return null;
	}

	public void insertDefective(DefectiveWrapper wr) {}

	public void insertDistribution(DistEvalListWrapper wr) {}

	public Vector getFixedAssortmentData() {
		Vector lines = new Vector();

		/*try {
			String cmd = " select PR_grupp as clGroup ,ProductCode as TooteKood from [MobileJava].[dbo].[Tootegrp]";

			System.out.println(cmd);

			con = getNewConnection();

			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String custID = " ";
				String clGroup = " ";
				String prodID = " ";
				String prodGroup = " ";
				MatrixLine mline = new MatrixLine();

				clGroup = "ss" + rs.getString("clGroup");
				prodID = rs.getString("TooteKood");

				if (custID != null && custID.trim().length() > 0) {
					mline.setCustID(custID.trim());
				}
				if (clGroup != null && clGroup.trim().length() > 0) {
					mline.setCliGroup(Converter.enCodeEst(clGroup.trim()));
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
		//System.out.println("Mlines " + lines);
		
*/
		return lines;
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

	private String url = "jdbc:odbc:kosmelita";
	private Connection con;
	private Vector custProds = null;
	private Statement stmt;
	private String subjectFilesTo = null;
	private TreeMap agents1 = new TreeMap();
	private NumberFormat NF = NumberFormat.getInstance();
	private TreeMap dealers = new TreeMap();
	HashMap aHash = new HashMap();

	public Vector getPriceMatrixData() {

		String cmd = "select * from PriceConditions";
//ProductCode ProductGroupCode                                   Discount                                           Price       StartDay   EndDay     
		Vector lines = new Vector();
		TreeMap products = new TreeMap();
		try {

			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);

			while (rs.next()) {

				String kood = rs.getString("ProductCode").trim();
			
				String hind = rs.getDouble("Price") + "";

				products.put(kood, hind);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	
		String sql = "select * from PriceConditions";
		Vector check = new Vector();

		try {
			con = getNewConnection();

			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {

			/*	[CustomerCode]
					      ,[CustomerGroup]
					      ,[PayerCode]
					      ,[ProductCode]
					      ,[ProductGroupCode]
					      ,[Discount]
					      ,[Price]
					      ,[StartDay]
					      ,[EndDay
				*/
				
				String klient = rs.getString("CustomerCode");
				String clGroup = rs.getString("CustomerGroup");
				String prodID = rs.getString("ProductCode");
				double discount = rs.getDouble("Discount");
				double price = rs.getDouble("Price");
				String algDate = rs.getString("StartDay");
						if(algDate != null) {
						algDate = algDate.substring(0, 10);
						}
						
				String endDate = rs.getString("EndDay");
			
				if(endDate != null) {
					endDate = endDate.substring(0, 10);
					}
				boolean goes = true;
			
              if(prices.get(prodID) != null && price == prices.get(prodID)) {
            	  goes = false;
              }

				MatrixLine mline = new MatrixLine();

				//mline.setCustID(klient);
				
	
					mline.setCliGroup(clGroup.trim());
				

				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}

				if (endDate != null && endDate.trim().length() > 0) {
					mline.setEndDate(endDate);
				}
				if (algDate != null && algDate.trim().length() > 0) {
					mline.setStartDate(algDate);
				}

				//mline.setDiscount(discount);

				if (price != 0) {
					mline.setPrice(price);
				}
				if (!check.contains(clGroup.trim() + prodID.trim()) && goes) {
					lines.add(mline);
				}

				check.addElement(clGroup.trim() + prodID.trim());
			}

			rs.close();
			this.connectionDown();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		//System.out.println("matrix " + lines.size());
		
		return lines;
	}

	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}
}
