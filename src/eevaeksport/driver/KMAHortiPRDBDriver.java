package eevaeksport.driver;

import java.io.BufferedWriter;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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

import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Dealer;
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

public class KMAHortiPRDBDriver extends AbstractDBDriver implements Manager, Promo,
		BulkUpdater, Discounter {
	public KMAHortiPRDBDriver(EksportPanel parent) {
		super(parent);
		if (parent.getParentFrame().isDaySales()) {
			this.subjectFilesTo = getParent().getString("senddatamsg");
		} else {
			this.subjectFilesTo = parent.getString("filemsg");
		}
		String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
		try {
			Class.forName(databaseDriverName).newInstance();
			con = DriverManager.getConnection(url, "merisoft", "Aivar_Horticom297Q5");
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
	//	System.out.println("L5 "  + result.size());
		
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
		return null;
	}

	public String getSaveStockInfo() {

		String cmd = " select Siffer as toode, Kogus as stock , Kasutajakood as ladu from AgendiLaoseis " + 
				"      		union select Siffer as toode , Hetkeseis as stock , 'PEA' as ladu from Kaubad";

	//	if (isDebug())
		//	System.out.println(cmd);

		StringBuffer products = new StringBuffer();
		try {

			Connection con1 = getNewConnection();
			Statement st = con1.createStatement();
			ResultSet rs = st.executeQuery(cmd);

			while (rs.next()) {

				String kood = rs.getString("toode").trim();
				double kogus = rs.getDouble("stock");
				String dealer = rs.getString("ladu").trim();
          if(kogus > 0){

				products.append(kood + ";");
				products.append(" ;");
				products.append(getParent().getNF().format(kogus) );
				if(!dealer.equals("PEA")) {
					products.append( ";"  + dealer + ";" ) ; 
				}
				products.append("\n");
				
			}
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
		
		 Properties propse =  new Properties();
		    try{
		    	propse =  EksportFrame.loadProps("emails.txt");
		  	  
		    }catch(Exception e){
		    }
		    
		try {

			
			Connection con = DriverManager.getConnection(url, "merisoft", "Aivar_Horticom297Q5");
		Statement	stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(" select kasutajakood , nimi from Agendid");
			while (rs.next()) {
				String kood = rs.getString("kasutajakood").trim();
				Agent agent = new Agent(kood);
				String name = rs.getString("nimi").trim();
				agent.setName(name);
				agent.setLanguage("EST");
				agent.setCountry("et");
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
              	activeAgents.add(kood);
                }
				
                agents1.put(agent.getID(), agent);
				agents.put(name, agent);
			}
			rs.close();
			stmt.close();
			con.close();

		} catch (Exception e) {
			getParent().showError(e);
		}
		
		return agents;
	}

	
	private TreeMap makeCustomerDeliveryFile(){
		TreeMap res = new TreeMap();
		try{

	      
	String sql3 = " select kliendikood as klient ,TarnePäevad as paev from Kliendid where TarnePäevad != ''";

	Connection con = this.getNewConnection();
		Statement stmt = con.createStatement();

	ResultSet rs3 = stmt.executeQuery(sql3);
	while(rs3.next()){
	String klient = rs3.getString("klient").trim();
	String days = rs3.getString("paev").trim();
	String ress = "";
	if(days.contains("Mo")) {
		ress = ress + "E";
	}
	if(days.contains("Tu")) {
		ress = ress + "T";
	}
	if(days.contains("We")) {
		ress = ress + "K";
	}
	if(days.contains("Th")) {
		ress = ress + "N";
	}
	if(days.contains("Fr")) {
		ress = ress + "R";
	}
	if(days.contains("Sa")) {
		ress = ress + "L";
	}
	if(days.contains("Su")) {
		ress = ress + "P";
	}
	
			res.put(klient, ress);
	}
	rs3.close();
	stmt.close();
	con.close();
	 
	  }catch (Exception e){
		  e.printStackTrace();
	    //getParent().showError(e);
	  }
	  return res;
	}
	public String getDistribution() {
		return null;
	}
	
	TreeMap customers  = null;
	
	public TreeMap getCustomers(Agent agent) {
		
		 TreeMap deldays =  makeCustomerDeliveryFile();
		 Converter.getManagersReplacements(agents1, rcompany, user , pass);
			     
	    	// System.out.println("groups to string " + groups.toString());
	        		
		if(customers != null) {
			return customers;
		}else {
			
		
	customers = new TreeMap();
		
		  String cmd = "select * from HinnakirjaKliendid";
		
		//if (isDebug())
			//System.out.println(cmd);

		HashMap kliendid = new HashMap();
        HashMap hinnakud = new HashMap();
		
		try {
			
			Connection con = getNewConnection();
			Statement st = con.createStatement();
			String sql3 = "select * from Transpordiliigid";
		
			
	    	ResultSet  rs = st.executeQuery(sql3);
	    	StringBuffer sb1 = new StringBuffer();
	        while(rs.next()){
	        	String kood = Converter.enCodeEst(rs.getString("Ident").trim());
	        	String name = Converter.enCodeEst(rs.getString("Kirjeldus").trim());
	        	sb1.append(kood + ";" + name + "\n");
	        }
	        rs.close();
	        try{
	        
	       	  Writer out = new BufferedWriter(new OutputStreamWriter(
	  			    new FileOutputStream("shipmode.txt"), "UTF-8"));
	  			try {
	  			    out.write(sb1.toString());
	  			} finally {
	  			    out.close();
	  			}
	        }catch(Exception r){
	        	
	        }
			
			
			
			
			
			 rs = st.executeQuery(cmd);
			int cc = 0;
			while (rs.next()) {
				cc++;
				String hinnak = rs.getString("HK_ID").trim();
				String klient = rs.getString("klient").trim();
				String vv = (String)hinnakud.get(klient);
				if(vv == null){
					vv = hinnak;
				}else{
					vv = vv + "|" + hinnak;
				}
				hinnakud.put(klient, vv);
				
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

			String cmd2 = "select * from Kliendid where agent != '' or kliendikood = 'UUS_KLIENT' ";
//kliendikood nimi Aadress  lisainfo Agent
			

			//if (isDebug())
				//System.out.println(cmd2);
			ResultSet rs = st.executeQuery(cmd2);
			HashMap custMap = new HashMap();

			while (rs.next()) {

				
				String accountnum = rs.getString("kliendikood").trim();
				Customer cust = new Customer(accountnum);

				if(hinnakud.get(accountnum) != null){
					cust.setGroup(hinnakud.get(accountnum)+"");
				}
				String name = rs.getString("nimi").trim();
				cust.setName(Converter.enCodeEst(name) + " " + accountnum);

				
				String aadress1 = rs.getString("aadress");
				if (aadress1 != null) {
					aadress1 = aadress1.trim();
				} else {
					aadress1 = " ";
				}
				
				cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress1),"Eesti"));
				cust.restStreet = (aadress1).trim();
				
				
				if (kliendid.get(cust.getID()) != null) {
					String sss = ((String) kliendid.get(cust.getID()));
					sss = sss.substring(0, sss.indexOf("|"));
					cust.setSold(Double.parseDouble(sss));
				}

				if(deldays.get(cust.getID()) != null) {
					cust.setDelDays(deldays.get(cust.getID())+"");
				}
                
				
				String info = rs.getString("lisainfo");
				if (info != null) {
					//cust.setInfo(info.trim());
				}
				
				boolean goes = false;
				String salesman = rs.getString("agent");
				 if ( salesman != null && agents1.get( salesman.trim()) != null){
	                      Agent aa = (Agent) agents1.get( salesman.trim());
	                      
	                      if(aa.isActive()) {
	                    	  Vector vec = aa.getDubbler();
		                       if( vec!= null && vec.size() > 0 ){
		                            Enumeration en = vec.elements();
		                            while(en.hasMoreElements()){
		                                 String ag = (String) en.nextElement();
		                              cust.addAgent( new Agent( ag));
		                           }
		                      } 
		                       goes = true;
	                      }
	                      
	                }
			
				 if ( salesman != null){
					 cust.addAgent(new Agent(salesman.trim()));
		    	 }
				 
					String salesman2 = rs.getString("agent2");
					
					
					if ( salesman2 != null && agents1.get( salesman2.trim()) != null){
	                      Agent aa = (Agent) agents1.get( salesman2.trim());
	                      if(aa.isActive()) {
	                       Vector vec = aa.getDubbler();
	                       if( vec!= null && vec.size() > 0 ){
	                            Enumeration en = vec.elements();
	                            while(en.hasMoreElements()){
	                                 String ag = (String) en.nextElement();
	                              cust.addAgent( new Agent( ag));
	                           }
	                      }
	                       goes = true;
	                       }
	                }
					
					 if ( salesman2 != null){
						 cust.addAgent(new Agent(salesman2.trim()));
			    	 }
				
				 if(cust.getID().equals("UUS_KLIENT")){
                	Enumeration enume = getParent().getAgentsHash().elements();
                    while (enume.hasMoreElements()) {
                      Agent a = (Agent) enume.nextElement();
                      cust.addAgent(a);
                    }
                }
                
                
                String mt = rs.getString("Maksetähtaeg");
                if(mt != null){
                	cust.setPaymTerm(mt.trim());
                }

				String blokk = rs.getString("blokk");

				if (!blokk.trim().equals("0")) {
					cust.setSalesBlock(true);
				}
				
                

				String orderType = rs.getString("Type");
				if(orderType != null && orderType.trim().length() > 0){
					cust.category = orderType.trim();
				}

				
				String tel = rs.getString("telefon");
				String mail = rs.getString("email");
				
				String inff = null;
				
				if(tel != null && tel.trim().length() > 0){
					inff = tel.trim().replaceAll(";", ",");
				}
				
				if(mail != null && mail.trim().length() > 0){
					if(inff == null){
						inff =  "Tel:" + tel.trim().replaceAll(";", ",");
						
					}else{
						inff = inff + " , Tel:" +tel.trim().replaceAll(";", ",");
									
					}
				}
				if (inff != null && inff.trim().length() > 0) {
					cust.setPersonalData(inff.trim());
				}

				double kokku = rs.getDouble("kliendivõlg");
				if(kokku != 0 ){
					cust.setUsedCredit(kokku);
				}
				
				double limiit = rs.getDouble("limiit");
				if(limiit > 0 ){
					cust.setCredit(limiit+"");
				}

				double volg = rs.getDouble("MakstaSumma");
				if(volg != 0 ){
					cust.setDebt(volg);
				}
				
				
				String payer = rs.getString("Maksjaklient");
				if (payer != null && payer.trim().length() > 0) {
					cust.setPayer(payer.trim());
				}
				String sorti = rs.getString("SortimentID");
				if (sorti != null && sorti.trim().length() > 0) {
					cust.addFixSortCode(sorti.trim());
				}
				
				if(hinnakud.get(accountnum) != null){
					StringTokenizer sta = new StringTokenizer(hinnakud.get(accountnum)+"","|");
					while(sta.hasMoreTokens()) {
						cust.addFixSortCode(sta.nextToken().trim());
					}
 					
				}
				if(goes) {
					customers.put(name + " " + accountnum, cust);
					custMap.put(cust.getID(), cust);
				}
				
			
			}

		

		} catch (Exception e) {
			getParent().showError(e);
		}
		}
		
		System.out.println("kliendid " + customers.size());
		
		return customers;
	}

	private Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(url, "merisoft", "Aivar_Horticom297Q5");
	}

	public TreeMap getProdGroups() throws SQLException {

		Connection con = getNewConnection();
		TreeMap tm = new TreeMap();
		String sql = "select * from KauGrupp1_Tase1";
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);

		while (rs.next()) {
			tm.put(rs.getString("kood").trim(), Converter.enCodeEst(rs.getString("Kirjeldus").trim()));
		}
		rs.close();
		st.close();
		con.close();

		return tm;
	}

	public TreeMap getProdGroupsSecond() throws SQLException {
		Connection con = getNewConnection();
		TreeMap tm = new TreeMap();
		String sql = "select * from KauGrupp1_Tase2";
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);

		while (rs.next()) {
			String kood = rs.getString("kood").trim();
			String pea = 	rs.getString("PeagrupiKood").trim();
			tm.put(pea+"."+kood, Converter.enCodeEst(rs.getString("Kirjeldus").trim()));
		}
		rs.close();
		st.close();
		con.close();

		return tm;
	}

	
	private HashSet activeProds() {
		
		HashSet res = new HashSet();
		
		//Teeme hinnalirjade listi ja klientide listi 
		Iterator itera = customers.keySet().iterator();
		HashSet custs = new HashSet();
		while (itera.hasNext()) {
			String key = itera.next()+"";
			Customer cc = (Customer)customers.get(key) ;
			custs.add(cc.getID());
			
		}
		

		System.out.println("custs " + custs.size());
		
		
		   String cmd = " select klident, gp.Siffer , Kogus , ArveKpv , arvenr from CustomerProductsProff gp "
		   		+ "INNER JOIN Kaubad k on k.Siffer = gp.Siffer  order by gp.klident ,k.Nimetus " ;

		//   and blocked = 0 
		//    if (isDebug()) System.out.println(cmd);

		    try {
		    	Connection con = this.getNewConnection();
		    	Statement stmt =  con.createStatement();
		      ResultSet rs = stmt.executeQuery(cmd);
		    
		      while(rs.next()){
		        String kliendikood = rs.getString("klident").trim();
		        if(custs.contains(kliendikood)) {
		        	   String tootekood = rs.getString("Siffer").trim();
				         res.add(tootekood);
		        }
		     
		      }
		      rs.close();
		      stmt.close();
		      con.close();
		    } catch (Exception e){
		      getParent().showError(e);
		 
		      	try{
		      		Connection con = this.getNewConnection();
			    	Statement stmt =  con.createStatement();
		        	 ResultSet rs = stmt.executeQuery(cmd);
		            
		             while(rs.next()){
		               String kliendikood = rs.getString("orderaccount").trim();
		               if(custs.contains(kliendikood)) {
			        	   String tootekood = rs.getString("Siffer").trim();
					         res.add(tootekood);
			           }

		             }
		             rs.close();
		             stmt.close();
		             con.close();
		             
		      	}catch (Exception ee){

		      	}
		    }

		
		
		

		
		
		return res ;
	}
	
	
	public TreeMap getProducts(Agent agent) {
		

		String cmd = " select Siffer,nimetus,hind,Ühik,ÜhikutKastis,ean,YlemGrupp"
				+ ",AlamGrupp,Hetkeseis,PildiLink,SoovituslikJaehind,Hooaeg,tähtsusgrupp from KaubadProff"
				+ " union all select  Siffer,nimetus,hind,Ühik,ÜhikutKastis,ean,YlemGrupp " + 
			    ",AlamGrupp,Hetkeseis,PildiLink,SoovituslikJaehind,Hooaeg,tähtsusgrupp  from Kaubad  ";
		// Siffer   Nimetus Hind Ühik  ÜhikutKastis   EAN   
		//YlemGrupp AlamGrupp Hetkeseis   MinHind  Kaal  Brutokaal  Pildilink   

		
		 HashSet active =  activeProds();
		 
		 System.out.println("proff " +  active.size() );
		
	//	if (isDebug())
		//	System.out.println(cmd);
		TreeMap products = new TreeMap();
		try {
			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);

			while (rs.next()) {

				String kood = rs.getString("Siffer").trim();
				Product prod = new Product(kood);
				String nimetus = rs.getString("nimetus");
				
				if(nimetus != null) {
							nimetus = nimetus.trim();
						
						
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
				prod.setPrice((rs.getDouble("hind")));

				String unit = rs.getString("Ühik");

				if (unit != null && unit.trim().length() > 0) {
					unit = unit.trim();
					prod.setUnit(unit);

				}

				
				prod.setInCases((rs.getDouble("ÜhikutKastis")));
				prod.setVat("20");

				
				 
					// Siffer   Nimetus Hind Ühik  ÜhikutKastis   EAN   
					//YlemGrupp AlamGrupp Hetkeseis   MinHind  Kaal  Brutokaal  Pildilink   


				String ean = rs.getString("ean");
				String add = "";
				if (ean != null && ean.trim().length() > 7) {
					prod.setEAN(ean.trim());
					add = ean.substring(ean.length()-4); 
				}

				String grupp = rs.getString("YlemGrupp");
				if (grupp != null && grupp.trim().length() > 0) {
					prod.setGroup(grupp.trim());
				}
				
				String grupp2 = rs.getString("AlamGrupp");
				if (grupp2 != null && grupp2.trim().length() > 0) {
					
					prod.setGroupSecond(prod.getGroup()+ "." + grupp2.trim());
				}

              double sto = rs.getDouble("Hetkeseis");
				if (sto > 0) {
					prod.setStock(sto);
				}else{
					prod.setStock(0);
				}

				prod.setName( Converter.enCodeEst(nimetus + " " +  add).trim());

				String weblink = rs.getString("PildiLink");
				if (weblink != null && weblink.trim().length() > 0) {
					//http://pda.kma.ee/horticom/images/kaubad/tellikma/
				//	prod.weblink = (weblink.trim().substring(weblink.trim().lastIndexOf("/")+1));
					prod.setPhotoid(weblink.trim().substring(weblink.trim().lastIndexOf("/")+1));
				}
				
				double  jaehind = rs.getDouble("SoovituslikJaehind");
				if (jaehind > 0) {
					prod.setPriceSecond(jaehind  * 1.20);
				}
				
				String hooaeg = rs.getString("Hooaeg");
				if (hooaeg != null && hooaeg.trim().length() > 0) {
					hooaeg = hooaeg.trim();
					if(hooaeg.indexOf(" ") > -1){
						hooaeg = hooaeg.substring(0,hooaeg.indexOf(" "));
					}
					prod.setType(hooaeg.trim());
				}
				boolean goes = false;
				String staatus = rs.getString("tähtsusgrupp");
				if (staatus != null && staatus.trim().length() > 0) {
					staatus = staatus.trim();
					if(staatus.equals("Lõpumüük")) {
						if(prod.getStock() > 0) {
							prod.setStatusSellout("1");
						//	System.out.println("OIOI "+ prod.getName());
							goes = true;
						}
					}
				}
				
				if(active.contains(prod.getID())) {
					goes = true;
				//	System.out.println("OIOI "+ prod.getName());
				}
				
				
				if(goes) {
					products.put(prod.getName(), prod);
					
				}
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



	  public Vector getCustomerProductsList(String agentCode){
		    Vector custProds = new Vector();

		 int counter = 1;
		   String cmd = " select klident, gp.Siffer , Kogus , ArveKpv , arvenr from CustomerProductsProff gp "
		   		+ "INNER JOIN Kaubad k on k.Siffer = gp.Siffer  order by gp.klident ,k.Nimetus " ;

		//   and blocked = 0 
		 //   if (isDebug()) System.out.println(cmd);

		    try {
		    	Connection con = this.getNewConnection();
		    	Statement stmt =  con.createStatement();
		      ResultSet rs = stmt.executeQuery(cmd);
		      counter++;
		      while(rs.next()){
		        String kliendikood = rs.getString("klident").trim();
		        String tootekood = rs.getString("Siffer").trim();
		        double kogus = rs.getDouble("Kogus");
		        String lastdate = rs.getString("ArveKpv").substring(0,10);
		                 lastdate = lastdate.substring(5,10);
		               //  double hind = rs.getDouble("hind");
		     	        String bill = rs.getString("arvenr");  
		     	        
		        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,0,0,0," ");
		        cp.setAbiKood(bill);

		        custProds.add(cp);
		      }
		      rs.close();
		      stmt.close();
		      con.close();
		    } catch (Exception e){
		      getParent().showError(e);
		      if(counter< 4){
		      	try{
		      		Connection con = this.getNewConnection();
			    	Statement stmt =  con.createStatement();
		        	 ResultSet rs = stmt.executeQuery(cmd);
		             counter++;
		             while(rs.next()){
		               String kliendikood = rs.getString("orderaccount").trim();
		               String tootekood = rs.getString("itemid").trim();
		               double kogus = rs.getDouble("qty");
		         	  
		               String lastdate = rs.getString("lastdate").substring(0,10);
		                        lastdate = lastdate.substring(5,10);
		                        double hind = rs.getDouble("hind");
		    	     	        String bill = rs.getString("dok");
		    	     	       CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,hind,0,0," ");
		    	   	        cp.setAbiKood(bill);

		               custProds.add(cp);
		             }
		             rs.close();
		             stmt.close();
		             con.close();
		             
		      	}catch (Exception ee){

		      	}

		      }
		    }
		    
		    return custProds;
		  }



	 public void setBulkSales(Map agents){
	      //System.out.println("bulkSales");
	      String cmd = "SELECT * FROM ManagerSales ";
	    

	        try {
	          Connection con = this.getNewConnection();
	            Statement stmt = con.createStatement();

	            Calendar cal = Calendar.getInstance();
	            cal.set(Calendar.DAY_OF_MONTH, 1);

	            ResultSet	      rs = stmt.executeQuery(cmd);
	            while(rs.next()) {
	              String agentID = rs.getString("agent");
	              if(agentID != null){
	            	  Agent agent = (Agent)agents.get(agentID.trim());
		              if (agent != null){
		                agent.setMonthSales((int)rs.getDouble("KuuMüük"));
		            //    System.out.println("kuumyyk " + agent.toString() + " " + agent.getMonthSales());
		              }  
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

	  
	public Vector getDebt(String agentCode) throws SQLException {
		Vector list = new Vector();
		String sql = "select *, datediff(dy,Maksepäev,getdate() ) as OverdueDays from OpenInvoices";
//arvenr     SaajaKood  Maksjakood Kuupäev  Maksepäev     Jääksummas
		//if (isDebug())
			//System.out.println(sql);
		Connection con = getNewConnection();

		stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		Calendar cal = Calendar.getInstance();
		Date now = new Date(cal.getTimeInMillis());

		while (rs.next()) {
			Debt debt = new Debt();
			debt.setDoknr(rs.getString("arvenr").trim());
			debt.setCustID(rs.getString("SaajaKood").trim());
			debt.setPayerID(rs.getString("Maksjakood").trim());
	
			Date date = getDate(rs.getString("Kuupäev").trim());
			Date duedate = getDate(rs.getString("Maksepäev").trim());
			debt.setDuedate(duedate);
			debt.setDate(date);
			debt.setSum(rs.getDouble("Jääksummas"));
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
		return "KMAHorti";
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
	  Vector lines = new Vector();

    try {
   	  String cmd = " select  SortimentID as grupp,Siffer as toode  from Sortimendid ";
   	  cmd = "select r.HK_ID as grupp , Siffer as toode  " + 
   	  							 " from HinnakirjaKaubad  r INNER JOIN Hinnakirjad h on h.HK_ID = r.HK_ID " + 
   	  							 " where Lõpp > getdate() order by  r.HK_ID ,Siffer ";
   	  boolean insertassort = false;
    	
   	  

   	
   //	System.out.println(cmd);
   	 Connection cc = getNewConnection();
  	        Statement stmt = cc.createStatement();
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

                   
                     
                     if(insertassort){
       //               insertSortLine(mline , prodGroup);   
                     }
                     lines.add(mline);
                   }

  	       rs.close();
  	       con.close();
  	       cc.close();
                  } catch (Exception e) {
                      getParent().showError(e);
                  }
               
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

	

	  public TreeMap getDealers() {

	     
		  return null;
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
	private String url = "jdbc:odbc:kma";
	private Connection con;
	
	private Statement stmt;
	private String subjectFilesTo = null;
	private TreeMap agents1 = new TreeMap();
	private NumberFormat NF = NumberFormat.getInstance();
	private TreeMap dealers = new TreeMap();
	HashMap aHash = new HashMap();

	HashSet activeAgents = new HashSet();
	public Vector getPriceMatrixData() {

		
		Vector lines = new Vector();
		Vector check = new Vector();
		TreeMap products = new TreeMap();
		
		try {
			String cmd = " select r.HK_ID as PListCode , Siffer as ItemCode , hind as SalesPrice "
					+ " from HinnakirjaKaubad  r INNER JOIN Hinnakirjad h on h.HK_ID = r.HK_ID "
					+ " and Kampaania = 0 and Lõpp > getdate() order by PListCode,ItemCode ";
		//	System.out.println(cmd);
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
			}		
		  rs.close();
		  st.close();
		  con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}


		try {

			String sql = "select r.HK_ID as kett ,algus , Lõpp, Siffer as TooteKood , "
					+ " hind as SalesPrice from HinnakirjaKaubad  r INNER JOIN Hinnakirjad h on h.HK_ID = r.HK_ID "
					+ " and Kampaania = 1 and Lõpp > getdate() and algus is not null order by kett,tootekood ";

			Connection con = getNewConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {

				String clGroup = rs.getString("Kett");
				String stDate = rs.getString("algus").substring(0, 10);
				String endDate = rs.getString("Lõpp").substring(0, 10);
				String prodID = rs.getString("TooteKood");
				double price = rs.getDouble("SalesPrice");


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
				
			//	mline.setDiscount(discount);

				if (price != 0) {
					mline.setPrice(price);
				}
				if (!check.contains(clGroup.trim() + prodID.trim())) {
					lines.add(mline);

			//	System.out.println("hinnak bvälja " + mline.toString());
				}

				check.addElement(clGroup.trim() + prodID.trim());
			}

			rs.close();
			stmt.close();
			con.close();
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return lines;
	}

	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}
}
