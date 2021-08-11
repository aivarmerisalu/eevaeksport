/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;

public class KalevMercDBDriver extends AbstractDBDriver implements Manager, Promo,
		BulkUpdater, Discounter {

	public KalevMercDBDriver(EksportPanel parent) {
		super(parent);
		this.subjectFilesTo = parent.getString("filemsg");
		String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
		try {
			Class.forName(databaseDriverName).newInstance();

		} catch (Exception e) {
			parent.showError(e);
		}
		company = getParent().getProps().getProperty("companycode");
	    user =  getParent().getProps().getProperty("rest.user");
	    pass = getParent().getProps().getProperty("rest.pass");
	    rcompany = getParent().getProps().getProperty("rest.company");
	    rcompanyx = getParent().getProps().getProperty("rest.companyx");
		      
	}
	  
	  String user =  null;
	  String company =  null;
	  String pass = null;
	  String rcompanyx =  null;
	  String rcompany = null;
	 
	public String getAssortmentStr(Agent agent) {
		return null;
	}

	public String getDealerCustomerCSV(String agent) {
		return null;
	}

	public String getSaveDebtInfo() {
		
			return null;

	}

	public String getCustProdDiscounts(Agent agent) {
		return null;
	}

	/**
     *
     */
	public void connectionUp() {
	}

	private Connection getNewCon() throws SQLException {
		return DriverManager.getConnection(url, "mobilejava", "mobilejava");
	}
	
	private Connection getNewConEeva() throws SQLException {
		return DriverManager.getConnection(url3, "", "");
	}

	/**
     * 
     *
     */
	public void connectionDown() {
	}

	public String getSaveStockInfo() {
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

	public TreeMap getProdGroups() throws SQLException {
		connectionUp(url);
		TreeMap tm = new TreeMap();
		
		String sql = " SELECT SY24002 AS liik ,SY24003 AS nimetus FROM SY240100 WHERE RTRIM(SY24003)<>'' AND SY24001 = 'IF' ORDER BY SY24003";
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);
		while (rs.next()) {
		
			String kood = rs.getString("liik").trim();
			String nimi = Converter.enCodeEst(rs.getString("nimetus").trim());
			tm.put(kood, nimi + "-" + kood);
	
		}
		rs.close();
		st.close();
		con.close();
		this.connectionDown();
		try{
			getProdGroupsFelix(tm);
		}catch(Exception e){
			e.printStackTrace();
		}
	   	
				return tm;
	}
	
	
	
	
	
	

	 public void getProdGroupsFelix(TreeMap tm) throws SQLException {
			

			String    sql =
			    "SELECT liik,artlnimi AS nimetus FROM artliik WHERE grupp='L'";
			Connection con = this.getNewConEeva();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {
			    tm.put("FE" + Converter.enCodeEst(rs.getString("liik").trim()),
			    		"xFE " + Converter.enCodeEst(rs.getString("nimetus").trim()));
			}
			
			rs.close();
			st.close();
		    con.close();

		    }

	

	public String getDistribution() {
		return null;
	}

	public TreeMap getAgents() {
		String cmd = "SELECT SY24002 AS yksus,SY24003 + ' ' + SY24002 AS nimi  FROM SY240100 WHERE SY24001='BK'";
		TreeMap agents = new TreeMap();
		//getFixedAssortmentData();
		try {
			Connection con = getNewCon();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);

			Properties props = new Properties();
			try {
				props = EksportFrame.loadProps("asendajad.txt");

			} catch (Exception e) {
				e.printStackTrace();
			}

			while (rs.next()) {
				Agent agent = new Agent(rs.getString("yksus").trim());
				String nimi = rs.getString("nimi").trim();
				agent.setName(nimi);
				String agentAGENTS = props.getProperty(agent.getID());
				if (agentAGENTS != null) {
					StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
					while (stag.hasMoreTokens()) {
						agent.addDubbler(stag.nextToken());
					//	System.out.println("Lisan Duubleid " + agent.getID()
						//		+ " " + agent.getDubbler().toString());
					}
    			}
				agent.setIsPilot(true);
				
				agent.setLanguage("EST");
		          agent.setCountry("ee");
		          agent.setPassWord("0000");
		          String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
		          if(email.length() > 3){
		        	  agent.setEmail(email);
		          }
		          if(email.length() > 3){
		        	  agent.setActive(true);
		          }
				
				agents1.put(agent.getID(), agent);
				agents.put(nimi, agent);
			}
			rs.close();
			stmt.close();
			con.close();

			Converter.sendAgentJson(agents, company, true);
		} catch (Exception e) {
			getParent().showError(e);
		}

		Agent ag1 = new Agent("ZZ");
		ag1.setName("ZZ");
        agents1.put("ZZ", ag1);
		agents.put("ZZ", ag1);
	
		return agents;
	}
	
		private HashSet getFelKoodid(){
		HashSet res = new HashSet();
	    String sql1 = "SELECT viitenr FROM klient  where viitenr  !=  ''";	
		    Statement st1;
			try {
			Connection coe = this.getNewConEeva();
				st1 = coe.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
		    while (rs1.next()) {
		    	res.add(rs1.getString("viitenr").trim());
		  
		    }
		    rs1.close();
		    st1.close();
		    coe.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		return res;
	}
	
	private HashSet getSmartBlokk(){
		HashSet res = new HashSet();
		
	    String sql1 = "SELECT viitenr FROM klient  where viitenr  !=  '' and blokk1 = '1'";	
		 
		

		    Statement st1;
		    
			try {
			Connection coe = this.getNewConEeva();
				st1 = coe.createStatement();
			
		    ResultSet rs1 = st1.executeQuery(sql1);

		    while (rs1.next()) {
		    	res.add(rs1.getString("viitenr").trim());
		  
		    }

		    rs1.close();
		    st1.close();
		    coe.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		return res;
	}

	
	  private HashMap sortimendid = null;

	
	public TreeMap getCustomers(Agent agent) {
		
		HashMap saamata = new HashMap();
		if(rcompanyx != null){
			Converter.getManagersReplacements(getAgents1(), rcompanyx, user , pass);
		}
		

		
	      try{
	    	  
	    	  
	      	  String sql2 = " SELECT OR01001 AS dokumentas,OR03005 AS preke,OR03011 AS kiekis,OR03011 AS UzsKiekis,'' AS padalinys," + 
	  "  SL01001 AS klientas,OR01016 AS data, '' AS UzsDok,'Tellimus' AS tipas, OR03006 as nimi FROM  " +
       "  OR010100 INNER JOIN SL010100 ON OR01004 = SL01001 inner join   OR030100	 on OR03001=OR01001" +
       " INNER JOIN  SC010100 ON OR03005=SC01001  WHERE OR01016 > getdate() ";  
				   
		      
				Connection con = getNewConnection2();
				Statement stmt3 = con.createStatement();
				ResultSet rs = stmt3.executeQuery(sql2);
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
						String klientas = rs.getString("klientas").trim();
	                 	String tipas = rs.getString("tipas").trim();
	                 	String tnimi = rs.getString("nimi").trim();


	                 	String text = Converter.enCodeEst("[" + kiekis +  "] " + preke + " " + tnimi + " " + tipas + " " + dokumentas + "||") ;
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
				stmt3.close();
				
				
	      }catch(Exception e){
	    	  e.printStackTrace();
	      }
		
		
		
		
		
		  sortimendid = new HashMap();
	      File fff = new File(this.getExcelDir() + "/SortimentDroid.xlsx" );
	      File fffx = new File(this.getExcelDir() + "/SortimentFel.xlsx" );
		    
	      TreeMap groups = new TreeMap(); 
	      TreeMap agents = new TreeMap();
	      TreeMap visits = new TreeMap();
	      try{
	          FileInputStream ff = new FileInputStream(fff);
	  		XSSFWorkbook workbook = new XSSFWorkbook((ff));
	  		XSSFSheet sheet = workbook.getSheetAt(1);
	  		XSSFRow row = null;
	  		row = sheet.getRow(0);
	  		System.out.println(" SHIT " + sheet.getLastRowNum()+1);
	  		
	  		for(int a = 1 ; a < sheet.getLastRowNum()+1;a++){
	  			try{
	  				row = sheet.getRow(a);
	  				if (row.getCell((short) 1) != null) {
	  	    			   String kood = null;
	  	    			  if(row.getCell((short)1) != null){
		    	    			  	try{
		    	    			  	  kood = row.getCell((short)1).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			   	  kood = row.getCell((short)1).getNumericCellValue()+"";  
		    	    	    		} 
		    	    			  	 if(kood.indexOf(".")!= -1){
		    	    			  	 	 kood = (kood.substring(1,kood.indexOf(".")));	
		    	    			  	 }
		    	    			  	 String sort = null;
		    	    			  	    try{
		    	    			  		    sort = row.getCell((short)3).getStringCellValue();  
			    	    			  	 }catch (Exception e){
			    	    			  		 try{
			    	    			  			sort = row.getCell((short)3).getNumericCellValue()+"";  
					    	    	    					 
			    	    			  		}catch (Exception r){
			    	    			  			
			    	    			  		}
			    	    			   } 
			    	    			  	 
			    	    			  	 if(sort != null && sort.indexOf(".")!= -1){
			    	    			  	 	 sort = (sort.substring(0,kood.indexOf(".")));	
			    	    			  	 }
			    	    			  	 
			    	    				  if(sort != null && sort.trim().length() > 0){
			    	    					  StringTokenizer st = new StringTokenizer(sort.trim(),",");
			    	    					  String res = "";
			    	    					  while(st.hasMoreElements()){
			    	    						  res = res + "KA" + st.nextToken()+",";
			    	    					  }
			    	    					  	 groups.put(kood.trim(), Converter.cleanLitLatEst(res));
			    	    				  }
			    	    				  
			    	    				  
			    	    				  String visit = null;
			    	    			  	    try{
			    	    			  	    	visit = row.getCell((short)11).getStringCellValue();  
				    	    			  	 }catch (Exception e){
				    	    			  		//visit = row.getCell((short)3).getNumericCellValue()+"";  
				    	    	    		 } 
				    	    			  	if(visit != null && visit.trim().length() > 0){
			    	    					  	 visits.put(kood.trim(),visit.trim());
			    	    				  }
				    	    			  	

				    	    				  String ag = null;
				    	    			  	    try{
				    	    			  	    	ag = row.getCell((short)9).getStringCellValue();  
					    	    			  	 }catch (Exception e){
					    	    			  		 try{
					    	    			  			ag = ((int) row.getCell((short)9).getNumericCellValue())+"";  
							    	    	    	 }catch(Exception r){
							    	    	    		 
					    	    			  		 }
					    	    			  	 } 
				    	    			  	    if(a > 600)
					    	    			  	System.out.println("ags " + kood + " "+ ag );
		  	 
					    	    			  	if(ag != null && ag.trim().length() > 0){
				    	    					  	 agents.put(kood.trim(),ag.trim());
				    	    				     }
			    	    				  
		    	    	     }	   
	  				  }
	 			}catch (Exception e){
	  			e.printStackTrace();
	 		
	  			}
	  		}
	  		
	  		//System.out.println("visits klint " + visits.toString() );

	  		//System.out.println("agendid klint " + agents.toString() );
	  
	  	 }catch (Exception e){
	  		e.printStackTrace();
	  	}


	  	 
	  	 
	  	 
	  	 
	      try{
	          FileInputStream ff = new FileInputStream(fffx);
	  		XSSFWorkbook workbook = new XSSFWorkbook((ff));
	  		XSSFSheet sheet = workbook.getSheetAt(1);
	  		XSSFRow row = null;
	  		row = sheet.getRow(0);
	  		
	  		for(int a = 1 ; a < sheet.getLastRowNum()+1;a++){
	  			try{
	  				row = sheet.getRow(a);
	  				if (row.getCell((short) 1) != null) {
	  	    			   String kood = null;
	  	    			  if(row.getCell((short)1) != null){
		    	    			  	try{
		    	    			  	  kood = row.getCell((short)1).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			   	  kood = row.getCell((short)1).getNumericCellValue()+"";  
		    	    	    		} 
		    	    			  	 
		    	    			  	 String sort = null;
		    	    			  	    try{
		    	    			  		    sort = row.getCell((short)3).getStringCellValue();  
			    	    			  	 }catch (Exception e){
			    	    			  		 try{
			    	    			  			sort = row.getCell((short)3).getNumericCellValue()+"";  
					    	    	  		}catch (Exception r){
			    	    			  		}
			    	    			    } 
			    	    			  	 
			    	    			  	 if(sort != null && sort.indexOf(".")!= -1){
			    	    			  	 	 sort = (sort.substring(0,sort.indexOf(".")));	
			    	    			  	 }
			    	    			  	 
			    	    				  if(sort != null && sort.trim().length() > 0){
			    	    					  if(groups.get(kood) != null){
			    	    						  	 groups.put(kood.trim(),groups.get(kood.trim()) +",FE" + Converter.cleanLitLatEst(sort.trim()));
						    	    		  }else{
			    	    						  	 groups.put(kood.trim(),"FE" + Converter.cleanLitLatEst(sort.trim()));
						    	    		  }
			    	    					}
		    	    	     }	   
	  				  }
	 			} catch (Exception e){
	  			e.printStackTrace();
	 		
	  			}
	  		}
	  		
	  		//System.out.println("groups klint " + groups.toString() );

	  	
	  	 }catch (Exception e){
	  		e.printStackTrace();
	  	}
	  	 
	  	 
	  	 
	  	 
	  	 
	  	 
		
		
		connectionUp(url);
		connectionUp2(url);
		HashSet felkoodid = getFelKoodid();
		HashSet smartBlokoodid = getSmartBlokk();


		Date date = new Date();
		Calendar sygis = Calendar.getInstance();
		sygis.set(Calendar.DAY_OF_MONTH, 7); // 7 oktoober
		sygis.set(Calendar.MONTH, 8);
	

		boolean isChrist = false;
		if (date.after(sygis.getTime())) {
			isChrist = true;
		}

		Calendar talv = Calendar.getInstance();
		talv.set(Calendar.DAY_OF_MONTH, 10);
		talv.set(Calendar.MONTH, 0);
	
	
		if (date.before(talv.getTime())) {
			isChrist = true;
		}
		
		System.out.println("on J�ulud " + isChrist);
		String myy = " SELECT  SL01001 AS klient  ,SL01035 as agent "
				+ "  FROM ST030100 INNER JOIN SL010100 ON ST03001 = SL01001 WHERE ST03015> getdate()-360 and SL01035!= '' "
				+ " GROUP BY SL01001 ,SL01035 order by SL01001 ,SL01035";
		Hashtable myyk = new Hashtable();

		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery(myy);
			while (rs.next()) {
				String klient = rs.getString("klient").trim();
				String ag = rs.getString("agent").trim();
				Vector vv = new Vector();
				if (myyk.get(klient) == null) {
					vv.addElement(ag);
					myyk.put(klient, vv);
				} else {
					vv = (Vector) myyk.get(klient);
					vv.addElement(ag);
					myyk.put(klient, vv);
				}
			}
			rs.close();
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}



		String cmd = "SELECT SL01002 AS nimi,SL01001 AS kood,(SL01003 + ' ,' + SL01004 + ',' + SL01005 ) AS aadress , " +
		" (SL14004 + ' ,' + SL14005 + ',' + SL14006 ) as aadr2, SC45004 AS ale,SL01036 AS ale2 ,  "
				+ "    SL14007 AS visit,SL01035 as agent,SL22005 as paym , SL01029 as klgrupp,  "
				+ " (SELECT SL01075  from SL010100 where SL01001=kl.SL01056) AS  block , SL01075 as block2,SL01056 as payer "
				+ "   , SL01008 + ' ' + SL01011 as info , (SELECT SL01018 from SL010100 where SL01001=kl.SL01056) as limiit "
				+ "   , (SELECT SY24003 from SY240100 where SY24002=kl.SL01029 and SY24001='IL') as chain ,SL01053 as tekst FROM SL010100 kl left JOIN SC450100  "
				+ "	   ON SL01036=SC45003    LEFT OUTER JOIN SL140100 ON SL01001=SL14001 AND SL14002='00' "
				+ "     LEFT OUTER JOIN SY240100 ON SL01035=SY24002 AND SY24001='BK' "
				+ "    LEFT OUTER JOIN SL220100 ON SL01024=SL22002 AND SL22001='0' "
				+ "   WHERE len(SL01001)>4  and  SL01002 !=''  " +
						" ORDER BY SL01001";
																			

		// alumine on Kristine tehtud

		// 'PR' +
		connectionUp(url);
		String cmd3 = "SELECT SL01056 as pea   FROM SL010100 left JOIN SC450400   ON SL01036=SC45003 "
				+ "  WHERE SL01002 != '' AND SL01035 != ''  and SL01056 != '' ORDER BY SL01002";

		log(cmd);

		Vector vec = new Vector();

		try {
			st = con.createStatement();
			rs = st.executeQuery(cmd3);
			while (rs.next()) {
				vec.addElement(rs.getString("pea").trim());
			}
			rs.close();
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		log(cmd);

		TreeMap customers = new TreeMap();
		HashMap custMap = new HashMap();
		connectionUp(url);
		try {
			PreparedStatement stmt = con.prepareStatement(cmd);
			
			rs = stmt.executeQuery();
			while (rs.next()) {
				String nimi = Converter.enCodeEst(rs.getString("nimi").trim());
				Customer cust = new Customer(rs.getString("kood").trim());
				String addr = rs.getString("aadress").trim();
				String addr2 = rs.getString("aadr2");
				if(addr2 != null && addr2.length() > 6){
					
				}else {
					addr2 = addr;
				}
				if(addr.trim().length() > 6){
					cust.setAddress("." + Converter.enCodeEst(addr) + ",Estonia.");
					nimi = nimi + " " + addr2;
				}
				

				cust.setName(nimi + " " + cust.getID());

				String ale = rs.getString("ale");
				if (ale != null && ale.length() > 0) {
				//	cust.setDiscount(Double.parseDouble(ale.trim()));
				}
				
				String visit = visits.get(cust.getID()) + "";
				if (visit != null && visit.trim().length() > 0) {
					cust.setVisit(visit.trim());
				}

				String agen = agents.get(cust.getID()) + "";
				if (agen != null) {
					cust.addAgent(new Agent(agen.trim()));
				    cust.addAgent(new Agent("ZZ"));

					Agent agg = ((Agent) this.getAgents1().get(agen.trim()));

					if (agg != null && agg.getDubbler().size() > 0) {
						for (int i = 0; i < agg.getDubbler().size(); i++) {
							cust.addAgent(new Agent((String) ((Agent) this
									.getAgents1().get(agen.trim()))
									.getDubbler().elementAt(i)));
						}
					}
				}
					
				 agen = rs.getString("agent");
				if (agen != null && agen.trim().length() > 0) {
					cust.addAgent(new Agent(agen.trim()));
					//cust.setMainAgent(agen.trim());
             	}
				

				if (groups.get(cust.getID()) != null) {
					Vector vv = new Vector();
					String gr = groups.get(cust.getID())+ "";
					StringTokenizer stj = new StringTokenizer(gr,",");
					
					while(stj.hasMoreTokens()){
						vv.add(stj.nextToken());	
					}
					
					cust.setFixSortCode(vv);
				}
				


				String paym = rs.getString("paym");
				if (paym != null && paym.trim().length() > 0) {
					cust.setPaymTerm(paym.trim());
				}

				
				if (cust.getID().equals("M19999") || cust.getID().equals("M14058")) {
					Enumeration enums = getParent().getAgentsHash().elements();
					while (enums.hasMoreElements()) {
						Agent a = (Agent) enums.nextElement();
						cust.addAgent(a);
					}
				}

				cust.setGroup(rs.getString("klgrupp"));

				String block = rs.getString("block");
				if (block != null && block.trim().length() > 0) {
					if (block.trim().equals("1")) {
						cust.setSalesBlockT(true);
					}
				}

				String block2 = rs.getString("block2");
				if (block2 != null && block2.trim().length() > 0) {
					if (block2.trim().equals("1")) {
						cust.setSalesBlockT(true);
					}
				}

				String payer = rs.getString("payer");
				if (payer != null && payer.trim().length() > 0) {
					cust.setPayer(payer);
				}

				String info = rs.getString("info");
				if (info != null && info.trim().length() > 0) {
					cust.setPersonalData(Converter.enCodeEst(info.trim().replaceAll(";", ":")));
				}

				String limiit = rs.getString("limiit");
				if (limiit != null && limiit.trim().length() > 0
						&& limiit.trim().length() < 8) {
					cust.setCredit(limiit.trim());
				}
				
				String chain = rs.getString("chain");
				if (chain != null && chain.trim().length() > 0) {
					if(!chain.trim().startsWith("Hinnakiri"))
					cust.setChain(Converter.enCodeEst(chain.trim().replaceAll("&", "ja")));
				}
				
				String tekst = rs.getString("tekst");
				if (tekst != null && tekst.trim().length() > 0) {
					if(tekst.trim().startsWith("�RA KASUTA")){
				        cust.diffBack = true;
	                 	cust.setInfo(" AINULT SORTIMENDI KONTROLL - TELLIMUST TEHA EI TOHI");
	                 	cust.noOrder = true;
	    			}
				}
				

				 if(saamata.get(cust.getID()) != null){
	            	  String vv = (String) saamata.get(cust.getID());
	            	 	  cust.setInfo(cust.getInfo() + "|" + vv);
	            	}
				
				    customers.put(cust.getName(), cust);
					custMap.put(cust.getID(), cust);
				
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			getParent().showError(e);
		}

		return customers;
	}

	public Vector getDebt(String agentCode) throws SQLException {
		Vector list = new Vector();
				return list;
	}

	public Vector getPriceMatrixData() {

		String sql = "SELECT SL01001 AS custid,'' AS clgroup,SUBSTRING(SC18002,11,35) AS prodid,'' AS prodgroup,SC18009, "
				+ "	(case when SY24003=null then '0' else SY24003 end ) AS discount,SC18008 AS price ," +
						"( case when SC18011 < getdate()+100 then SC18011 else null end) as paev ,( case when SC18010 > getdate()-100 then SC18010 else null end)   as algus"
				+ "   FROM SC180100 INNER JOIN SL010100 ON  left(SC18002,7)= SL01001 LEFT JOIN SY240100 on SC18009=SY24002 and SY24001='BG' WHERE "
				+ "  SC18001='C' AND CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011   "
				+ " and SL01001 not like '%INTR%'order by SL01001,SUBSTRING(SC18002,11,35)";

		Vector result = new Vector();
		try {

			connectionUp(url);
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {

				String custID = rs.getString("custid").trim();
				String clGroup = rs.getString("clgroup");
				String prodID = rs.getString("prodid").trim();
				String prodGroup = rs.getString("prodgroup");
				String sss = rs.getString("SC18009");
				String discount = rs.getString("discount");
				if (discount == null) {
					discount = sss.trim();
				} else {
					discount = discount.trim().replaceAll("%", " ").trim();
				}

				double price = rs.getDouble("price");
				String date = rs.getString("paev");
				String algus = rs.getString("algus");

				//String lopp = rs.getString("lopp");
				MatrixLine mline = new MatrixLine();

				if (custID != null && custID.length() > 0) {
					mline.setCustID(custID);
				}
				if (mline.getCustID() == null && clGroup != null
						&& clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
				}

				if (date != null) {
					mline.setEndDate(date.substring(0, 10));
				}
				if (algus != null) {
					mline.setStartDate(algus.substring(0, 10));
				}
				if (prodID != null && prodID.length() > 0) {
					mline.setProdID(prodID);
				}
				if (mline.getProdID() == null && prodGroup != null
						&& prodGroup.trim().length() > 0) {
					mline.setProdGroup(prodGroup.trim());
				}
				if (discount.trim().length() == 0) {
					discount = "0";
				}
				double disc = 0;
				try {
					disc = Double.parseDouble(discount.replaceAll(",", "."));
				} catch (Exception e) {
					// e.printStackTrace();
				}
				mline.setDiscount(disc);

				if ( price != 0) {
					mline.setPrice(price);

				}
				
				
				if (mline.getPrice() > 0 || mline.getDiscount() > 0) {
					result.add(mline);
				}
				
				

			}

			String sql2 = "select '' AS custid ,SC39002 AS clgroup , rtrim(ltrim(SC39001))  AS prodid,' ' AS prodgroup, " + 
				"	 case when SC39006 <= getdate() AND (SC39007 >= getdate() OR SC39007 = null) then " +
				"	 SC39005 else SC39004 end AS price  , " +
				" ( case when SC39007 < getdate()+100 then SC39007 else null end) as paev ,( case when SC39006 > getdate()-100 then SC39006 else null end)   as algus " +
				"	 FROM SC390100 WHERE SC39002  not in ('00','66')   " +
				"	 and  case when SC39006 <= getdate() AND (SC39007 >= getdate() OR SC39007 = null) then " +
				"	 SC39005 else SC39004 end != 0 " +
				"	 order by SC39002,SC39001 ";
			rs = st.executeQuery(sql2);

			while (rs.next()) {

				String clGroup = rs.getString("clgroup");
				String prodID = rs.getString("prodid").trim();
				String discount = "0";

				double price = rs.getDouble("price");
				String date = rs.getString("paev");
				String algus = rs.getString("algus");
			
				MatrixLine mline = new MatrixLine();
				if (date != null) {
					mline.setEndDate(date.substring(0, 10));
				}
				if (algus != null) {
					mline.setStartDate(algus.substring(0, 10));
				}
				if (clGroup != null && clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
				}

				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}

				if (Double.parseDouble(discount) == 0 && price != 0) {
					mline.setPrice(price);
					result.add(mline);
					//kui hind on siis paneme 
				}
				
			 // Kommentisin v�lja kuniks uus hinna skeem
									// hakkab toimima
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	private TreeMap eans = null;

	public TreeMap getProducts(Agent agent) {
		connectionUp(url);
         eans = new TreeMap();
		// 'PR' +
         String cmd = "SELECT  RTRIM(t.SC01001) AS toode,(RTRIM(SC01002)+' ' "
				+ "        +rtrim(SC01003)) + ' ' +RTRIM(t.SC01001) AS nimi,SC39004 AS myygihind ,SC01015 AS tykke,SC03003-SC03004 AS stock, "
				+ " SC01115  AS grupp,SC01132 AS ean  , SC01016 as letter ,SC01014 as sorting "
				+ " , case when SC01135 = 0 then  'KG' else 'TK' end as unit  ,  SC01141 as kaal, " +
						" OptimumKTellimuseKogus as inunit ,class as type FROM SC010100 t left outer JOIN SC030100 on SC03001=t.SC01001 and SC03002='SKSM1' AND SC03003!=0 left outer JOIN SC390100 ON t.SC01001 "
				+ "  = SC39001 and SC39002='00' LEFT OUTER JOIN proov20100 yh ON yh.SC01001= t.SC01001 " +
						" LEFT OUTER JOIN class ty ON ty.MasterProductCode = t.SC01001 WHERE SC01115 = 'L' or ( LEN(LTRIM(SC01037))>0 and SC39004 > 0 and rtrim(ltrim(SC01016)) != 'S' and SC01014 != '')  ORDER BY LEFT(RTRIM(SC01037),1),t.SC01001 ";
	if (isDebug()) {
			System.out.println(cmd);
		}

		TreeMap products = new TreeMap();



		try {
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);
      while (rs.next()) {
			
				
			String kood = rs.getString("toode").trim();
				String nimetus = Converter.enCodeEst(rs.getString("nimi").trim().replaceAll("�", "s").replaceAll("�", "S"));
 
				Product prod = new Product(kood);
				prod.setEdiname(nimetus);
				prod.setPrice(parseDouble(rs.getString("myygihind").trim()));
				if(prod.getPrice() == 0){
					prod.setPrice(1);
				}
				prod.setInCases(parseDouble(rs.getString("tykke").trim()));
				prod.setStock(rs.getDouble("stock"));

				if (kood.indexOf("-EE") > -1) {
					prod.setStatusDiscount("1");
				}
				String gr = rs.getString("grupp");
				if (gr != null && gr.trim().length() > 0) {
					prod.setGroup(gr.trim());	
					if(gr.trim().equals("L")){
						gr = "XX";
					}
				}
			//	prod.setMaxDiscount(1);
				prod.setName(gr.trim() + " " + nimetus);
				gr = rs.getString("ean");
				
				if (gr != null && gr.trim().length() > 0) {
					prod.setEAN(gr.trim());
					eans.put(gr.trim(), prod.getID());
				}

				String unit = rs.getString("unit");
				if (unit != null && unit.trim().length() > 0) {
					prod.setUnit(unit.trim());
				}

				String kaal = rs.getString("kaal");
				if (kaal != null && kaal.trim().length() > 0) {
					prod.setInUnit(Double.parseDouble(kaal));
				}

				String aaa = rs.getString("inunit");
				if (aaa != null) {
					try {
						prod.setInUnit(Double.parseDouble(aaa.trim()));
					} catch (Exception e) {

					}
					//System.out.println("prod in unit " + prod.getInUnit());
				}
				
				String type = rs.getString("type");
				if (type != null && type.trim().length() > 0) {
					prod.setType2(Converter.enCodeEst(type.trim()));
					if(type.trim().equals("Teemant")){
						prod.setStatusTop("1");
					}
				}
				prod.setType("KALEV");

				prod.setVat("20");

				String sss = "SELECT top 3 SC33001 as toode , SC33015 as bb , sum( SC33005) as kogus    FROM SC330100 where SC33002 = 'SKSM1'"
						+ " and SC33005 > 0 and SC33001 = '"
						+ prod.getID()
						+ "' group by SC33015 , SC33001 order by SC33001,SC33015 desc";

				connectionUp2(url);
				Statement st2 = con2.createStatement();
				ResultSet rs2 = st2.executeQuery(sss);
				String info = " ";
				while (rs2.next()) {
					info = info + rs2.getString("bb").trim().substring(0, 10)
							+ " kog:" + rs2.getDouble("kogus") + "|";
					// prod.setPartii(info);
				}

				String vvv = " select a.MP64002 as toode,  min(a.MP64013) as kuup, "
						+ " sum(a.MP64004-a.MP64005)  as kogus FROM MP640100 as a (nolock)  "
						+ " WHERE (MP64088>=3 AND MP64088<7) and (a.MP64004-a.MP64005) > 0  "
						+ " and datepart(week,MP64013) =datepart(week,getdate()) and a.MP64002= '"
						+ prod.getID() + "' " + " GROUP BY a.MP64002 ";

				st2 = con2.createStatement();
				rs2 = st2.executeQuery(vvv);

				while (rs2.next()) {
					info = info + "Tootm:"
							+ rs2.getString("kuup").trim().substring(0, 10)
							+ " kog:" + rs2.getDouble("kogus") + "|";
					// prod.setPartii(info);
				}
				rs2.close();
				st2.close();
				String ccc = " select stockcode as toode,  bestbefore as kuup, "
					+ "  stockamount as kogus, discount as maxdisc FROM mj_salesproposal   "
					+ " WHERE  stockcode= '"+ prod.getID() + "'";

			st2 = con2.createStatement();
			rs2 = st2.executeQuery(ccc);

			if (rs2.next()) {
				
				String kuup = rs2.getString("kuup");
				double kog = 0;
				if(kuup != null){
					kog = rs2.getDouble("kogus");
					info = info + "VANAJ��K:"
					+ kuup.trim().substring(0, 10)
					+ " kog:" + kog + "|";	
				}
				
				
				double discount = rs2.getDouble("maxdisc");
				if(discount > 0){
					prod.setMaxDiscount(discount);
				}
				if(kog == 0){
					prod.setStatusDiscount("1");
				}
							
			}
			
			rs2.close();
			st2.close();
				prod.setPartii(info);
				products.put(nimetus, prod);

			}
			rs.close();
			st.close();
			
			try{
				getProductsFelix(products);
			}catch(Exception e){
				e.printStackTrace();
			}
			
		} catch (Exception e) {
			getParent().showError(e);
		}

		return products;
	}
	
	
	private void getProductsFelix(TreeMap products){


		try {

		   String cmd =
			"SELECT toode,nimetus,asukoht,kastis, myygihind, liik, plaanhind,minkogus,alusel,ribakood,ribakood3, stock1 as laoseis  FROM toode " +
	               " WHERE len(trim(toode)) > 2 AND len(trim(liik))>0  AND  myygihind > 0 AND len(trim(nimetus))>0 AND eisaada = .F. ORDER BY toode ";

		    if (isDebug()) {
			System.out.println(cmd);
		    }

		    Connection ee = this.getNewConEeva();
		    Statement st = ee.createStatement();
		    ResultSet rs = st.executeQuery(cmd);

		    while (rs.next()) {
			double  tykke = (rs.getDouble("kastis"));
			String  kood = "FE" + rs.getString("toode").trim();

			String  nimetus = rs.getString("nimetus").trim();
			String asukoht = rs.getString("asukoht").trim();
			Product prod = new Product(kood);
			prod.setName("xFE " + Converter.enCodeEst(nimetus));
			prod.setPrice(round(rs.getDouble("myygihind"), 2));
			prod.setInCases(tykke);
			prod.setType("FELIX");

	                prod.setGroup("FE" + rs.getString("liik").trim());
	                prod.setMeasure(rs.getDouble("plaanhind"));
	                prod.setMaxDiscount(rs.getDouble("minkogus"));
	                String ean = rs.getString("ribakood");
	                prod.setOnPallet(rs.getDouble("alusel"));
	                if(ean != null && ean.trim().length()>7 ){
	                prod.setEAN(ean.trim());
	                eans.put(ean.trim(), prod.getID());
	                }
	                String  status = rs.getString("ribakood3");

	                String seis = rs.getString("laoseis");
	                try{
	                    if(seis!= null && !seis.trim().equals("0")){
		                	prod.setStock(Double.parseDouble(seis.trim()));
		                }	
	                }catch(Exception r){
	                r.printStackTrace();	
	                }
	            
	                if (status!= null && status.trim().length() > 0){
	                    if (status.indexOf("F")!= -1){
	                       prod.setMustBe("1");
	                    }
	                      if (status.indexOf("D")!= -1){
	                        prod.setStatusDiscount("1");
	                     }
	                      if (status.indexOf("N")!= -1){
	                       prod.setStatusNew("1");
	                      }

	                          if (status.indexOf("O")!= -1){
	                             prod.setStatusSellout("1");
	                          }
	                }
			products.put("x"+asukoht + nimetus, prod);

			// }
		    }
	            rs.close();
                 st.close();
                 ee.close();
		} catch (Exception e) {
		    getParent().showError(e);
		}


	}
	
	
	
	
	private void connectionUp(String url) {
		try {
			if (con != null) {
				con.close();
			}
			con = DriverManager.getConnection(url, getParent().getProps()
					.getProperty("dsn.user"), getParent().getProps()
					.getProperty("dsn.pass"));
		} catch (Exception e) {
			getParent().showError(e);
		}

	}

	private void connectionUp2(String url) {
		try {
			if (con2 != null) {
				con2.close();
			}
			con2 = DriverManager.getConnection(url, getParent().getProps()
					.getProperty("dsn.user"), getParent().getProps()
					.getProperty("dsn.pass"));
		} catch (Exception e) {
			getParent().showError(e);
		}

	}

	private Connection getNewConnection2() throws SQLException {
		return DriverManager.getConnection(url, getParent().getProps()
				.getProperty("dsn.user"),
				getParent().getProps().getProperty("dsn.pass"));
	}

	
	public Vector getCustomerProductsList(String agentCode) {
		connectionUp(url);

		HashMap kliendid = new HashMap();

		Vector custProds = new Vector();

		try {

			// ESITEKS VAATAMAE MIS ON MJ-ga OUT pandud listist objekti aeg
			String sql3 = "select custid,prodid,sentdate from supervisor where comment like '%OUT%' and sentdate >getdate()-180";

			Connection con2 = getNewConnection2();
			PreparedStatement stmt2 = con2.prepareStatement(sql3);

			HashMap aeg = new HashMap();
			ResultSet rs2 = stmt2.executeQuery();
			while (rs2.next()) {
				String kliendikood = rs2.getString("custid").trim();
				String tootekood = rs2.getString("prodid").trim();
				long dd = rs2.getDate("sentdate").getTime();
				aeg.put(kliendikood + tootekood, dd + "");
			}
			
			
			
			String sql6 = "		SELECT  [KaupluseKood] as custid,[Kood] as prodid,Kogus FROM [RestorderKalev] " +
			 " where ISNULL([KaupluseKood],0) <> '0' and Tarneaeg > getdate()-10";

			
			 stmt2 = con2.prepareStatement(sql6);
          	HashMap saamatta = new HashMap();
				 rs2 = stmt2.executeQuery();
				while (rs2.next()) {
					String kliendikood = rs2.getString("custid").trim();
					String tootekood = rs2.getString("prodid").trim();
					
					saamatta.put(kliendikood + tootekood, rs2.getDouble("Kogus") + "");
				}
				
				rs2.close();

			// Siin Paneme sortimendid Sisse

			String sql2 = "SELECT  SL01001 AS klient ,ST03017 AS toode ,sum(case when ST03015 >= getdate()-30 "
					+ "  		 then ST03020 else 0 end ) as pluss,max(ST03020) AS Kogus,ST03015  as lastdate ,max(ST03014) as arvenr "
					+ "   FROM ST030100 INNER JOIN SL010100 ON ST03001 = SL01001 WHERE ST03015> ? AND ST03016>0 AND ST03020>0 and left(SL01001,2) !='M3' "
					+ "  GROUP BY SL01001,ST03017, ST03015 ORDER BY SL01001,ST03017 ,ST03015 ASC";

			if (isDebug()) {
				System.out.println(sql2);
			}

			stmt = con.prepareStatement(sql2);
			stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));

			ResultSet rs = stmt.executeQuery();
			TreeMap ttt = new TreeMap();

			while (rs.next()) {
				String kliendikood = rs.getString("klient").trim();
				String tootekood = rs.getString("toode").trim();
				double pluss = rs.getDouble("pluss");
				double kogus = Double.parseDouble(rs.getString("kogus").trim());

				Date date = rs.getDate("lastdate");
				String arvenr = rs.getString("arvenr");
				boolean ok = true;
				if (ttt.get(kliendikood + tootekood) != null) {
					String vv = (String) ttt.get(kliendikood + tootekood);
					pluss = pluss + Double.parseDouble(vv);
					ttt.put(kliendikood + tootekood, pluss + "");

				} else {
					ttt.put(kliendikood + tootekood, pluss + "");
				}
				if (ok) {
					double ei = 0;
					if (saamatta.get(kliendikood + tootekood) != null) {
						 ei = Double.parseDouble((String) saamatta.get(kliendikood + tootekood));
                       // System.out.println("Saamatta " + kliendikood + tootekood + " " + ei);
					}
					CustProd cp = new CustProd(kliendikood, tootekood, kogus,
							pluss, 0, date.toString().substring(5, 10), 0, ei,
							0, " ");
					 cp.setOpenAmount(ei);
					cp.setAbiKood(arvenr);
					custProds.add(cp);
					Product prod = new Product(tootekood);
					Customer cust = (Customer) kliendid.get(kliendikood);
					if (cust == null) {
						cust = new Customer(kliendikood);
					}
					cust.addProduct(prod, cp,false);
					kliendid.put(kliendikood, cust);

				}
			}
			rs.close();
			stmt.close();

			// Siin lisame listingud mida pole myydud aga on listingusse lisatud

			sql2 = " select custid,prodid,sentdate,prodname from supervisor where comment like '%TO THE LIST%' ";

			if (isDebug()) {
				System.out.println(sql2);
			}

			stmt = con.prepareStatement(sql2);
			// stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));

			rs = stmt.executeQuery();

			while (rs.next()) {
				String kliendikood = rs.getString("custid").trim();
				String tootekood = rs.getString("prodid").trim();
				double kogus = 0;
				Date date = rs.getDate("sentdate");

				boolean ok = true;
				if (aeg.get(kliendikood + tootekood) != null) {

					String vv = (String) aeg.get(kliendikood + tootekood);
					if (Long.parseLong(vv) > date.getTime()) {
						ok = false;
						// System.out.println("siin on sortimendi erand LISTINGUTE OMA  "
						// + kliendikood + "  " + tootekood);
					}

				}
				if (ok) {

					CustProd cp = new CustProd(kliendikood, tootekood, kogus,
							0, 0, date.toString().substring(5, 10), 0, 0, 0,
							" ");

					custProds.add(cp);
					Product prod = new Product(tootekood);
					Customer cust = (Customer) kliendid.get(kliendikood);
					if (cust == null) {
						cust = new Customer(kliendikood);
					}
					cust.addProduct(prod, cp,false);
					kliendid.put(kliendikood, cust);

				}
			}
			rs.close();
			stmt.close();

			int counter = 0;
			int cou = 0;
			String oldklient = null;
			String olddate = " ";
			String oldtoode = null;

			String cmd2 = " SELECT  SL01001 AS klient , ST03017 AS toode ,sum(ST03020) AS Kogus,ST03015  as lastdate "
					+ "  FROM ST030100 INNER JOIN SL010100 ON ST03001 = SL01001 WHERE ST03015> getdate()-180 AND ST03016>0 AND ST03020>0 "
					+ "  group by SL01001,ST03017,ST03015 "
					+ "  ORDER BY SL01001,ST03017 ,ST03015 DESC ";
			System.out.println(cmd2);
			// "klient","toode","kogus","viimane kuup�ev","viimane kogus"

			con2 = getNewConnection2();
			Statement stmt6 = con2.createStatement();
			rs2 = stmt6.executeQuery(cmd2);

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

							custProds.add(cp);
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
			getParent().showError(e);
		}

		connectionDown();
		
		try{
			getCustomerProductsListFelix(custProds);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("CustProd l�peb");
		return custProds;
	}

	
	
    public void getCustomerProductsListFelix(Vector custProds) {

    	String cmd =
    	    "SELECT k.viitenr as saaja ,rid.toode, kogus, dok.kuupaev AS lastdate FROM dok INNER " +
    	   " JOIN rid ON dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode " +
            "    INNER JOIN klient k ON dok.saaja=k.kood WHERE " +
    	    " len(trim(dok.saaja)) > 0 AND len(trim(rid.toode)) > 2   AND dok.kuupaev > date()-180 AND dok.pais2obj != '6' " + 
            " AND k.objekt2 !='' AND  dok.doktyyp = 'G' and k.viitenr != '' " +
			"  GROUP BY k.viitenr, rid.toode ORDER BY dok.kuupaev DESC, k.viitenr,toode.asukoht, toode.nimetus";

    	if (isDebug()) {
    	    System.out.println(cmd);
    	}

    	try {
         	Connection con = this.getNewConEeva();
            PreparedStatement pst = con.prepareStatement(cmd);
    	    ResultSet	      rs = pst.executeQuery();

    	    while (rs.next()) {
    		String kliendikood = rs.getString("saaja").trim();
            String tootekood = "FE" + rs.getString("toode").trim();
                    double kogus = rs.getDouble("kogus");
                    String lastDate = rs.getString("lastdate").substring(5,10);
                    CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastDate,0,0,0," ");
                    custProds.add(cp);
                    
    	    }
                rs.close();
                pst.close();
                con.close();
             
    	} catch (Exception e) {
    	    getParent().showError(e);
    	}
        }

	
	public String getPilotDayData(Agent agent) {
				return null;

	}

	public String getNonMobileOrders(Agent agent) {
		return null;
			}

	public void setBulkSales(Map agents) {
		
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

	public double getPromoSales(String agentCode, int nr) throws SQLException {
		return 0;
		
	}

	public ManagerData getManagerData() throws SQLException {
		
		return null;
	}

	

	private String ensureLen(String str, String replace) {
		if (str == null)
			str = "";
		else
			str = str.trim();
		if (str.length() == 0) {
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

	private static double parseDouble(String s) {
		if (s.length() > 0)
			return Double.parseDouble(s);
		else
			return 0;
	}

	public NumberFormat getNF() {
		return NF;
	}

	/**
	 * 
	 * @param aNF
	 *            - the new value for NF
	 */
	public void setNF(NumberFormat aNF) {
		NF = aNF;
	}

	public static final String DRIVER_VERSION = "KalevMerc";
	private String url = "jdbc:odbc:orklaDB";
	private String url2 = "jdbc:odbc:report";
	private String url3 = "jdbc:odbc:eeva";
	
	private Connection con = null;
	private Connection con2 = null;
	private String subjectFilesTo = null;
	private PreparedStatement stmt = null;

	private TreeMap agents1 = new TreeMap();
	private NumberFormat NF = NumberFormat.getInstance();

	public Vector getFixedFel (Vector result) {
		File f = new File(this.getExcelDir() + "/SortimentFel.xlsx");
		
		try {
			
			
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheetAt(0);  // toodete leht 
			XSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(3);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised kolmas rida .

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 4; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					 groups.put(a + "", "FE" + Converter.cleanLitLatEst(row.getCell((short) a)
							.getStringCellValue()).trim());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			// System.out.println(groups.toString());

			for (int i = 8; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";
              try{
            	
				if (row.getCell((short) 1) != null) {

					try {
						prodID =  (row.getCell((short) 1)
								.getNumericCellValue()) + "";
						if(prodID.indexOf("E") != -1){
							prodID = prodID.substring(0,prodID.indexOf("E") );
   				         int len  = prodID.length();
   				      prodID = prodID.substring(0,1) + prodID.substring(2,len);
  				          if(prodID.length() == 12){
  				        	prodID = prodID+"0";
  				          }
   				        }else if(prodID.indexOf(".") != -1) {
   				        	prodID = prodID.substring(0,prodID.indexOf("."));
   				        }
						
					} catch (Exception e) {
						prodID = (row.getCell((short) 1).getStringCellValue());
					}
					
					if(prodID.length() == 11 ){
						prodID = prodID + "00";
					}
					if(prodID.length() == 12 ){
						prodID = prodID + "0";
					}
					
                	prodID = (String)eans.get(prodID);
			//		System.out.println(" toode2 " + prodID);
                       if(prodID != null && !prodID.equals("0")){
					// Siin l�heme reasisse rokkima ja vaatama kas se toode
					// l�heb kaasa
					for (int a = 4; a < row.getLastCellNum(); a++) {

						String group = (String) groups.get(a + "");
						if (group != null && group.trim().length() > 0 && !group.equals("FE") ) {

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

						// System.out.println( a + " " + prodID + " " +
							//	 clGroup + " " + go);

								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
								}

								if (prodID != null
										&& prodID.trim().length() > 0 ) {
									mline.setProdID(prodID.trim());
								}
								if (mline.getProdID() == null
										&& prodGroup != null
										&& prodGroup.trim().length() > 0) {
									mline.setProdGroup(prodGroup);
								}
								 /*if(insertassort){
		                               insertSortLine(mline);   
		                           }*/
								if(clGroup != null && !clGroup.equals("FE")){
									result.add(mline);
												
								}
							}
						}
					}
					   
                       }
				}
				  
              }catch (Exception sss){
            	  sss.printStackTrace();
              }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println("fixed Suurus FEL " + result.size());
       
		return result;

	}
	
	public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		this.getProducts(null);
		File f = new File(this.getExcelDir() + "/SortimentDroid.xlsx");
		
		
		try {
			
			
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheet("Sortiment");
			XSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(3);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised kolmas rida .

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 4; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					 groups.put(a + "", "KA" + Converter.cleanLitLatEst(row.getCell((short) a)
							.getStringCellValue()));
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			// System.out.println(groups.toString());

			for (int i = 8; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";
              try{
            	
				if (row.getCell((short) 1) != null) {

					try {
						prodID =  (row.getCell((short) 1)
								.getNumericCellValue()) + "";
						if(prodID.indexOf("E") != -1){
							prodID = prodID.substring(0,prodID.indexOf("E") );
   				         int len  = prodID.length();
   				      prodID = prodID.substring(0,1) + prodID.substring(2,len);
  				          if(prodID.length() == 12){
  				        	prodID = prodID+"0";
  				          }
   				        }else if(prodID.indexOf(".") != -1) {
   				        	prodID = prodID.substring(0,prodID.indexOf("."));
   				        }
						
					} catch (Exception e) {
						prodID = (row.getCell((short) 1).getStringCellValue());
					}
					
					
					
                	prodID = (String)eans.get(prodID);
			//		System.out.println(" toode2 " + prodID);
                       if(prodID != null && !prodID.equals("0")){
					// Siin l�heme reasisse rokkima ja vaatama kas se toode
					// l�heb kaasa
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

						// System.out.println( a + " " + prodID + " " +
							//	 clGroup + " " + go);

								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
								}

								if (prodID != null
										&& prodID.trim().length() > 0 ) {
									mline.setProdID(prodID.trim());
								}
								if (mline.getProdID() == null
										&& prodGroup != null
										&& prodGroup.trim().length() > 0) {
									mline.setProdGroup(prodGroup);
								}
								 /*if(insertassort){
		                               insertSortLine(mline);   
		                           }*/
								if(!mline.getCliGroup().equals("KA"))
								result.add(mline);
							}
						}
					}
					   
                       }
				}
				  
              }catch (Exception sss){
            	  sss.printStackTrace();
              }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		 System.out.println("fixed Suurus " + result.size());
        getFixedFel(result);
		return result;

	}


	public TreeMap getAgents1() {
		return agents1;
	}

	public Vector getLastThreeData() {
		return null;
	}

	public String getProductSortingData() {
		return null;
	}

}

