/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.parser.JSONParser;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.ClientBlock;
import eevaeksport.report.DefectiveLineWrapperMeri;
import eevaeksport.report.SalesSetter;
import eevaeksport.util.Converter;

public class KalevDBDriver extends AbstractDBDriver implements Manager, Promo,
		BulkUpdater, Discounter,SalesSetter,ClientBlock,PictureMaker ,FixLoader{

	public KalevDBDriver(EksportPanel parent) {
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
	  String rcompanyx =  null;
	  String pass = null;
	  String rcompany = null;
	  private TreeMap eanprods = null;
	  
	  
	  private TreeMap  getOpenOrders(){
			TreeMap res = new TreeMap();
			oorders = new StringBuffer();
			oorders.append(Calendar.getInstance().getTimeInMillis() + "\n");

			   String sql2 =  " select  OR01001 AS id,case when OR01072 != '' then 1 else 0 end as abi, case when OR01004='+' then OR01003 else OR01004 end  AS kkood , \r\n" + 
			   		"			   			 OR01021 as number ,''  as status,OR01015 AS kuup, OR01024 AS summa from OR010100 sal\r\n" + 
			   		"			   			WHERE OR01015 > getdate()-2  and (case when OR01004='+' then OR01003 else OR01004 end) not like ('INTR%') and OR01024 > 0\r\n" + 
			   		"			   			ORDER BY (case when OR01004='+' then OR01003 else OR01004 end ),OR01015 desc " ;
			   
		 
	
		try {
		   nf.setMinimumFractionDigits(2);
	       
		   Connection con = this.getNewCon();
		   Connection cc = this.getNewCon();
		   PreparedStatement stmt2 = con.prepareStatement(sql2);
		   ResultSet  rs = stmt2.executeQuery();

HashSet ids = new HashSet();
int u = 0;
String oldcust = null;
		while(rs.next()){
			StringBuffer sb = new StringBuffer();
			String id = rs.getString("id").trim();
			
			if(!ids.contains(id)) {
			ids.add(id);
			
			u++;
			
			
			 int abi = Integer.parseInt(rs.getString("abi"));
			 String text = "edi";
			 if(abi == 1){
				 text = "merisoft";
			 }
		    String nimi = rs.getString("kkood");
		    if (nimi == null){
		    nimi = " ";
		    } else {nimi = nimi.trim();}
		    
		    boolean goes = true;
		    
		    if(oldcust == null) {
		    	oldcust = nimi;		   
		    	}
		    
		    if(u > 3 && oldcust.equals(nimi)) {
				goes = false;  
				//System.out.println("ei lähe " + nimi);
			}
		    
		    
		    if(!nimi.equals(oldcust)) {
		    	oldcust = nimi;
		    	u = 0;
		    	goes = true;
		    	//System.out.println("muutus " + nimi);
		    }
         
		    if(goes) {

			//DPP00488444 (‘Pabeigts’ – 01.06.2018; merisoft).
		    	String number = rs.getString("number") ;
		    	if(number == null) {
		    		number = id;
		    	}else {
		    		number = number.trim();
		    	}
		    sb.append(number + " (");
		    
		    String kuup = rs.getString("kuup").trim().substring(0,10);
			sb.append(kuup + " " + text + ") ");
			

			double summa = Double.parseDouble(rs.getString("summa").trim());
			if(summa > 0 ){
				String sum = getNF().format(summa) ;
			    sb.append(sum + " eur KM-ta|" );	
			}else{
				 sb.append("|" );	
			}
			oorders.append(nimi + ";" + summa + ";" + kuup + "\n" );
		  
		    String sql3 = "select OR03011 as amount,  'tk.' as unit  , OR03006 as name from OR030100 where OR03001 = '" + id + "'";
		  
		    PreparedStatement stmt3 = cc.prepareStatement(sql3);
			 
		       ResultSet  rs2 = stmt3.executeQuery();
		   
			    while(rs2.next()){
			    	sb.append("[" +nf.format(rs2.getDouble("amount"))+"] " 
			       + rs2.getString("unit").replaceAll("\\r\\n|\\r|\\n", " ").trim()+ " " + rs2.getString("name").replaceAll("\\r\\n|\\r|\\n", " ").trim() + "|");
			    }
		   
			    rs2.close();
		       stmt3.close();
		       
		       if(res.get(nimi) != null){
		    	   res.put(nimi,res.get(nimi) +" | |" + sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
		   	       
		       }else{
		    	   res.put(nimi, sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
		   	    	   
		       }
		   	
		   }
		}
			
		}
		
	
		
		cc.close();
		rs.close();
		stmt2.close();
		con.close(); 	 
		

		
		
		
		
		
		} catch (Exception e) {
		  getParent().showError(e);
		}

System.out.println("res OO suurus " + res.size());

Converter.saveFile(parent.getProps().getProperty("stockrefdir")  + "customerorders.txt", oorders.toString());
			return res;
		}
		
	   private StringBuffer oorders = null;

	  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
	  
	
	  
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
				Converter.sendContractJson(contracts ,  rcompanyx,  user ,  pass);
			}
			
			
			System.exit(0);
		}

	public String getAssortmentStr(Agent agent) {
		return null;
	}

	public String getDealerCustomerCSV(String agent) {
		return null;
	}

	public String getSaveDebtInfo() {
		
		String cmd3 = "SELECT SL01001 AS klient,SL01002,SUM(SL03013-SL03053) AS debt ,(SELECT SL01075  from SL010100 where SL01001=kl.SL01056) as blokk ," +
				"SL01075 as blokk2 FROM SL030100 INNER JOIN SL010100 kl ON SL03001 = SL01056  or SL03001 = SL01001  WHERE  " +
				" NOT(SL03013=SL03053) AND SL03006<=getdate()  GROUP BY SL01001,SL01002,SL01056,SL01075 ORDER BY SL01002 ";

		StringBuffer products = new StringBuffer();
		try {

			this.connectionUp(url);
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd3);

			while (rs.next()) {

				String kood = rs.getString("klient").trim();
				double volg = rs.getDouble("debt");
				String blokk = rs.getString("blokk");
				String blokk2 = rs.getString("blokk2");
				String bl = "0";
				if(blokk!= null && blokk.equals("1")){
					bl = "2";
				}
				if(blokk2!= null && blokk2.equals("1")){
					bl = "2";
				}
				if (volg > 0) {
					products.append(kood + ";"
							+ getParent().getNF().format(volg) + ";" + bl
							+ "\n");
				}
			}
			
			
			

			rs.close();
		} catch (Exception e) {
			getParent().showError(e);
		}
		
		this.connectionDown();
		return products.toString();

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
	
	
	  
	  public HashMap getSmartenData(){
		HashMap hash = new HashMap();
		
			try {
		
		final URL urlst;

		String urlStr = "http://mail.smarten.ee:8080/mobilejava/felka/stockref.txt";
		
		

		final File  v  = new File("stocksmarten.txt");
		    
		int read = 0;
           if(urlStr != null){             
				urlst = new URL(urlStr);
				//System.out.println( urlst + v.getAbsolutePath());
			URLConnection con = urlst.openConnection();
			InputStream is = con.getInputStream();
			OutputStream os = new FileOutputStream(v);
			int i;
			while ( (i = is.read()) != -1) {
				read++;
				os.write(i);
			}
			is.close();
			os.close();
		
			

				BufferedReader  in = new BufferedReader(new FileReader(v));
				String	    line;
			
				while ((line = in.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(line, ";");
					
					String prodID = "";
					double amount = 0;
					System.out.println("id " + line);
					if (st.hasMoreTokens()) {
						prodID = st.nextToken();
					}
					st.nextToken();
				
					if (st.hasMoreTokens()) {
						amount = new Double(st.nextToken()).doubleValue();
					}
					hash.put(prodID,amount + "" );
						
				}}
               }catch(Exception e){
				e.printStackTrace();
			}
  	
		  return hash;
	  }

	  
	  
	  private HashMap getSmartenCodes(){
		  HashMap res = new HashMap();

				try {
					  String cmd =
							"SELECT toode, kood1 as sma  FROM toode  WHERE len(trim(kood1)) > 2 ";

						    if (isDebug()) {
							System.out.println(cmd);
						    }

						    Connection ee = this.getNewConEeva();
						    Statement st = ee.createStatement();
						    ResultSet rs = st.executeQuery(cmd);

						    while (rs.next()) {
								String  kood = "FE" + rs.getString("toode").trim();
								String  sma = (rs.getString("sma"));
								
								res.put(kood, sma.trim() );
							    	
						    }
	                       rs.close();
		                   st.close();
		                   ee.close();
				}catch(Exception r){
					
				}

				
				return res ;
				 
	  }
	  
	  
	public String getSaveStockInfo() {
		connectionUp(url);

      StringBuffer products = new StringBuffer();
      TreeMap part = new TreeMap();
		try {
			
			String ccc = " select stockcode as toode,  bestbefore as kuup, " + 
					"  stockamount as kogus, discount as maxdisc FROM mj_salesproposal where stockcode != '' ";
					
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(ccc);
			while (rs.next()) {
				String toode = rs.getString("toode").trim();
				String kuup = rs.getString("kuup");
				double kog = rs.getDouble("kogus");
				double discount = rs.getDouble("maxdisc");
				
				
				if(discount > 0){
					if(kuup != null){
						String aa =  Converter.enCodeEst("VANAJÄÄK:") + kuup.trim().substring(0, 10)+ " kog:" + kog + "| ale:" + (int)discount + "%";	
						
						if(part.get(toode) != null) {
							part.put(toode, part.get(toode) + "|" + aa);
						} else {
							part.put(toode, aa);
						}
						
					}
				}
			}
				
				
			rs.close();
			
			
			String cmd = "  SELECT  RTRIM(SC03001) AS toode , 'SKSM1' AS LADU ,SC03003 AS stock   " //-SC03004
					+ "  FROM SC030100 (nolock) WHERE SC03002='SKSM1' AND SC03003!=0 ORDER BY SC03001";
			
				
		rs = st.executeQuery(cmd);
			

			while (rs.next()) {

				String kood = rs.getString("toode").trim();
				double seis = rs.getDouble("stock");
				String dd = " ";
				if(part.get(kood) != null) {
					dd = part.get(kood)+"";
				}
				if (seis > 0) {
					products.append(kood + ";");
					products.append(" ;");
					products.append(getParent().getNF().format(seis) + "; ; ;" + dd	+ "\n");
				}
			}
			rs.close();
			st.close();
		

			HashMap smart =  getSmartenData();
			HashMap smartKodes = getSmartenCodes();
			Iterator itera = smartKodes.keySet().iterator();
			while (itera.hasNext()){
				String toode  = (String)itera.next();
				String sm = (String)smartKodes.get(toode);
				if(smart.get(sm) != null){
				//	products.append(toode + ";");
					//products.append(" ;");
					//products.append(smart.get(sm) + "; "
						//	+ "\n");
				}
			}
		
		
		} catch (Exception e) {
			getParent().showError(e);
		}
		
		   getOpenOrders();
		return products.toString();
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
	
	
	
	
	
	 public void makeWarehouse() throws SQLException {
		
		 String  sql = "SELECT yksus,(iif(len(trim(yksus)) = 1 , '0'+trim(yksus), trim(yksus)) + ' ' + nimetus) as nimi," +
		 		" 2 AS objekt, klassif ,agentcode FROM yksus where klassif = 1 and agentcode = '' and yksus != '1' order by agentcode";
		
		
		Connection con = this.getNewConEeva();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);
StringBuffer sb = new StringBuffer();
		
		while (rs.next()) {
		    sb.append(rs.getString("yksus").trim() + ";" + rs.getString("nimi").trim() + "\n");
		}
		
		rs.close();
		st.close();
	    con.close();
	    Converter.saveFileInUTF8("warehouse.txt", sb);

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
					System.out.println("asendajad " + agent + " " + agent.getDubbler());
					}

				}
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
			       
				
				agent.setIsPilot(true);
				agents1.put(agent.getID(), agent);
				agents.put(nimi, agent);
			}
			rs.close();
			stmt.close();
			con.close();

		} catch (Exception e) {
			getParent().showError(e);
		}
		  // Converter.sendAgentJson(agents, company, false);
		     
		 if(rcompany != null){
		    	Converter.sendAgentJson(agents, rcompany, false);
		    }

		Agent ag1 = new Agent("FELIX");
		ag1.setName("FELIX");
		agents1.put("FELIX", ag1);
		agents.put("FELIX", ag1);
	
		//pane tellile 85

		return agents;
	}
	
	 public void insertDefectiveMeri(eevaeksport.report.DefectiveWrapperMeri wr){
		 String sql = "INSERT INTO mj_quality VALUES (?,?,?,?,?,?,?,?,?,?,getdate(),?,' ',' ',0,0)";
			try {
				connectionUp(url);
				PreparedStatement ps = con.prepareStatement(sql);

				ps.setString(1, wr.getDefective().getAgentID());
				ps.setString(2, wr.getDefective().getAgentName());

				for (Iterator iter = wr.getDefective().getLines().iterator(); iter
						.hasNext();) {
					DefectiveLineWrapperMeri lw = new DefectiveLineWrapperMeri(iter.next());
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
	public void insertDefective(DefectiveWrapper wr) {}

	private HashSet getFelKoodid(){
		HashSet res = new HashSet();
		
	    String sql1 = "SELECT viitenr,kood5 FROM klient  where viitenr != '' or kood5 != ''";	
		 
		

		    Statement st1;
		    
			try {
			Connection coe = this.getNewConEeva();
				st1 = coe.createStatement();
			
		    ResultSet rs1 = st1.executeQuery(sql1);

		    while (rs1.next()) {
		    	String vv = rs1.getString("viitenr");
		    	if(vv != null && vv.trim().length() > 0){
		    		res.add(vv.trim());
		  		}
		    	
		    	vv = rs1.getString("kood5");
		    	if(vv != null && vv.trim().length() > 0){
		    		res.add(vv.trim());
		  		}
		    
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

	
	private String getVisitDay(String aaa){
		String res = aaa;
		
		res = res.replace('2', '5');
		res = res.replace('1', '2');
		res = res.replace('5', '1');
		
		return res;
	}
	
	private HashMap sortimendid = null;
	
	public TreeMap getCustomers(Agent agent) {
		if(rcompanyx != null){
			Converter.getManagersReplacements(getAgents1(), rcompanyx, user , pass);
		}
		
		  TreeMap openOrders = this.getOpenOrders();
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
	  	
	  		// teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
	  	// 	System.out.println("rownum " + row.getLastCellNum());
	  		
	  		
	  		for(int a = 1 ; a < sheet.getLastRowNum()+3;a++){
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
					    	    	    	  }catch(Exception k){
			    	    			  			k.printStackTrace(); 
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
					    	    			  		//visit = row.getCell((short)3).getNumericCellValue()+"";  
					    	    	    		 } 
					    	    			  	if(ag != null && ag.trim().length() > 0){
				    	    					  	 agents.put(kood.trim(),ag.trim());
				    	    				  }
			    	    				  
		    	    	     }	   
	  				  }
	 			}catch (Exception e){
	  			e.printStackTrace();
	 				//System.out.println(row.getCell((short)a).getNumericCellValue());
	  			}
	  		}

		//	System.out.println("M17154 " + groups.get("M17154")+ " " + sheet.getLastRowNum() );
	  
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
			    	    					  
			    	    					  
			    	    					  
			    	    				 	
				    	    					  StringTokenizer st = new StringTokenizer(sort.trim(),",");
				    	    					  String res = "";
				    	    					  while(st.hasMoreElements()){
				    	    						  res = res + "FE" + st.nextToken()+",";
				    	    					  }
				    	    					  	
				    	    				  
			    	    					  
			    	    					  
			    	    					  
			    	    					  
			    	    					  if(groups.get(kood.trim()) != null){
			    	    						  	 groups.put(kood.trim(),groups.get(kood.trim()) +"," + Converter.cleanLitLatEst(res.trim()));
						    	    		  }else{
			    	    						  	 groups.put(kood.trim(),Converter.cleanLitLatEst(res));
						    	    		  }
			    	    					}
		    	    	     }	   
	  				  }
	 			} catch (Exception e){
	  			//e.printStackTrace();
	 		
	  			}
	  		}
	  		
	  	//	System.out.println("groups klint " + groups.toString() );

	  	
	  	 }catch (Exception e){
	  		e.printStackTrace();
	  	}
	  	 


		
		
		connectionUp(url);
		connectionUp2(url);
		try{
			makeWarehouse();	
		}catch(Exception e){			
		}
		
		HashSet felkoodid = getFelKoodid();
		HashSet smartBlokoodid = getSmartBlokk();

		
		
		Hashtable miinusyks = new Hashtable();
		Hashtable miinuskaks = new Hashtable();
		Hashtable jooksev = new Hashtable();
		String sql2 = "SELECT SL01001 AS klient, "
				+ " sum (case when year(DATEADD (month , -1 , getdate()))=year(OR20015) and month(DATEADD (month , -1 , getdate()))=month(OR20015) "
				+ " then OR20024 else 0 end)  AS yks, "
				+ " sum (case when year(DATEADD (month , -2 , getdate()))=year(OR20015) and month(DATEADD (month , -2 , getdate()))=month(OR20015) "
				+ " then OR20024 else 0 end)  AS kaks, "
				+ " sum (case when year(getdate())=year(OR20015) and month( getdate())=month(OR20015) "
				+ " then OR20024 else 0 end)  AS jooksev "
				+ " FROM   OR200100 INNER JOIN SL010100 ON OR20004 = SL01001 WHERE OR20015 > getdate()-93  group by SL01001";

		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery(sql2);
			while (rs.next()) {
				String klient = rs.getString("klient").trim();
				double yks = rs.getDouble("yks");
				double kaks = rs.getDouble("kaks");
				double jooks = rs.getDouble("jooksev");
				if (jooks != 0) {
					jooksev.put(klient, (int) jooks + "");
				}
				if (yks != 0) {
					miinusyks.put(klient, (int) yks + "");
				}
				if (kaks != 0) {
					miinuskaks.put(klient, (int) kaks + "");
				}
			}
			rs.close();
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

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
		" (SL14004 + ' ,' + SL14005 + ',' + SL14006 ) as aadr2,SL01003 as tanav,SL01004 as linn,SL01005 as regioon,SL01083 as indeks,"
		+ " SC45004 AS ale,SL01036 AS ale2 ,  "
				+ "    SL14007 AS visit,SL01035 as agent,SL22005 as paym , SL01029 as klgrupp,  "
				+ " (SELECT SL01075  from SL010100 where SL01001=kl.SL01056) AS  block , SL01075 as block2,SL01056 as payer "
				+ "   , SL01008 + ' ' + SL01011 as info , (SELECT SL01037  from SL010100 where SL01001=kl.SL01056) as limiit , kl.SL01037  as limiit2 "
				+ "  , (SELECT SY24003 from SY240100 where SY24002=kl.SL01029 and SY24001='IL') as chain ,SL01053 as tekst "
				+ " ,cc.class as tase FROM SL010100 kl left JOIN SC450100  "
				+ "	   ON SL01036=SC45003    LEFT OUTER JOIN SL140100 ON SL01001=SL14001 AND SL14002='00' "
				+ "     LEFT OUTER JOIN SY240100 ON SL01035=SY24002 AND SY24001='BK' "
				+ "    LEFT OUTER JOIN SL220100 ON SL01024=SL22002 AND SL22001='0' "
				+ "    LEFT OUTER JOIN customerclass cc ON MasterProductCode=SL01001  "
				+ "   WHERE len(SL01001)>4  and  SL01002 !='' and rtrim(SL01012)+rtrim(SL01053)!='1VANA'" +
						" and rtrim(SL01012)+rtrim(SL01053)!='1SULETUD' ORDER BY SL01001";
																			

		
		/*
SELECT  [MasterProductCode] as klient 
      ,[class] as taseblock
      
  FROM [scalaDB].[dbo].[customerclass]*/
		
		// alumine on Kristine tehtud

		// 'PR' +
		connectionUp(url);
		String cmd3 = "SELECT SL01056 as pea   FROM SL010100 left JOIN SC450100   ON SL01036=SC45003 "
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
			if (agent != null) {
				// stmt.setString(1, agent.getID());
			}
			rs = stmt.executeQuery();
			String header = " |Läbimyygi Aruanne :| | ";
			while (rs.next()) {
				String nimi = Converter.enCodeEst(rs.getString("nimi").trim().replaceAll("bussipeatus", "pea").replaceAll("hansu", "h."));
				Customer cust = new Customer(rs.getString("kood").trim());
				cust.restName = nimi;
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
				
			
				String tanav = rs.getString("tanav");
                if (tanav != null && tanav.length() > 0){
                  cust.restStreet = tanav.trim();
                }
                String linn = rs.getString("linn");
                if (linn != null && linn.length() > 0){
                  cust.restCity = linn.trim();
                }
                String regioon = rs.getString("regioon");
                if (regioon != null && regioon.length() > 0){
                	cust.restCity = (cust.restCity + " " + regioon.trim()).trim();
                }

                String indeks = rs.getString("indeks");
                if (indeks != null && indeks.length() > 0){
                  cust.restZipCode = indeks.trim();
                }

               if(openOrders.get(cust.getID()) != null){
                	cust.addToCustDataVec(cust.getID() + ";tarasaldo; ;Avatud Tellimused |"   +  openOrders.get(cust.getID()));
                }
               
				

				cust.setName(nimi + " " + cust.getID());

				String ale = rs.getString("ale");
				if (ale != null && ale.length() > 0) {
					cust.setDiscount(Double.parseDouble(ale.trim()));
				}
				String ale2 = rs.getString("ale2");
				if (ale2 != null && ale2.trim().equals("04")) {
					// cust.setDiscount(17);

				}
				String visit = rs.getString("visit");
				if (visit != null && visit.trim().length() > 0) {
					cust.setVisit(getVisitDay(visit.trim()));
				}

				String agen = rs.getString("agent");
				if (agen != null) {
					cust.addAgent(new Agent(agen.trim()));
					cust.addAgent(new Agent("HOUSE1"));
					Agent agg = ((Agent) this.getAgents1().get(agen.trim()));

					if (agg != null && agg.getDubbler().size() > 0) {
						Vector vvv = agg.getDubbler(); 
						for (int i = 0; i < vvv.size(); i++) {
							cust.addAgent(new Agent((String)vvv.elementAt(i)));
						}
					}
				}
				
				
				String custinfo = null;

				if (miinuskaks.get(cust.getID()) != null) {
					custinfo =  "Üleeelmine kuu: "
							+ miinuskaks.get(cust.getID()) + "|";
				}

				if (miinusyks.get(cust.getID()) != null) {
					if (custinfo == null) {
						custinfo = "Eelmine kuu: "
								+ miinusyks.get(cust.getID()) + "|";
					} else {
						custinfo = custinfo + "Eelmine kuu: "
								+ miinusyks.get(cust.getID()) + "|";
					}

				}

				if (jooksev.get(cust.getID()) != null) {
					if (custinfo == null) {
						custinfo = "Jooksev kuu: " + jooksev.get(cust.getID());
					} else {
						custinfo = custinfo + "Jooksev kuu: "
								+ jooksev.get(cust.getID());
					}

				}

				if (custinfo != null) {
					cust.setInfo(Converter.enCodeEst(header + custinfo));
				}

				
				if(felkoodid.size() >0 && !felkoodid.contains(cust.getID())){
					cust.setInfo(" OTB TOOTEID EI SAA MÜÜA - KOOD PUUDUB||" + cust.getInfo());
				}else 				if(smartBlokoodid.size() >0 && smartBlokoodid.contains(cust.getID())){
					cust.setInfo(" Smartenis On MÜÜk blokeeritud VÕLGNEVUS||" + cust.getInfo());
				}
				boolean goes = true;

			
				
				if (groups.get(cust.getID()) != null) {
					Vector vv = new Vector();
					String gr = groups.get(cust.getID())+ "";
					StringTokenizer stj = new StringTokenizer(gr,",");
					
					while(stj.hasMoreTokens()){
						vv.add(stj.nextToken());	
					}
					
					cust.setFixSortCode(vv);
				}
				
				
				
				if (cust.getID().substring(0, 2).equals("M3") && !isChrist) {
					goes = false;
				}

				if(cust.getID().equals("M15194") || cust.getID().equals("M15369")){
					cust.addAgent(new Agent("FELIX"));	
					goes = true;
				}
				
				if (myyk.get(cust.getID()) != null) {
					Vector ags = (Vector) myyk.get(cust.getID());
					for (int i = 0; i < ags.size(); i++) {
						String aa = (String) ags.elementAt(i);
						cust.addAgent(new Agent(aa));
					
					}
				}

				String paym = rs.getString("paym");
				if (paym != null && paym.trim().length() > 0) {
					cust.setPaymTerm(paym.trim());
				}
			
				if (cust.getID().equals("M19999") || cust.getID().equals("M14058") 
						|| cust.getID().equals("M17235") || cust.getID().equals("M17244") 
						|| cust.getID().equals("M17243") 
						|| cust.getID().equals("M17499") 
						|| cust.getID().equals("M17467") ) {
					 

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
				String limiit2 = rs.getString("limiit2");
				
				if (limiit != null && limiit.trim().length() > 1
						&& limiit.trim().length() < 8) {
					cust.setCredit(limiit.trim());
				}else if (limiit2 != null && limiit2.trim().length() > 1
						&& limiit2.trim().length() < 8) {
					cust.setCredit(limiit2.trim());
				}
				
				String chain = rs.getString("chain");
				if (chain != null && chain.trim().length() > 0) {
					if(!chain.trim().startsWith("Hinnakiri"))
					cust.setChain(Converter.enCodeEst(chain.trim().replaceAll("&", "ja")));
				}

				String tekst = rs.getString("tekst");
				if (tekst != null && tekst.trim().length() > 0) {
					if(tekst.trim().startsWith("ÄRA KASUTA")){
				        cust.diffBack = true;
	                 	cust.setInfo(" AINULT SORTIMENDI KONTROLL - TELLIMUST TEHA EI TOHI");
	    			    cust.noOrder = true;
					}
				}
				
				String tase = rs.getString("tase");
				if (tase != null && tase.trim().length() > 0) {
					cust.category = (tase.trim());
				}
				
				// otsime v�lasumma igale kauplusele

				String sql6 = "SELECT SL01001 AS id,SUM(SL03013-SL03053) AS debt FROM SL030100 INNER JOIN SL010100 ON SL03001 = SL01001  WHERE  "
						+ " NOT(SL03013=SL03053) AND SL03006<=getdate() and SL01001='"
						+ cust.getPayer() + "'  GROUP BY SL01001 ";

				Statement stm = con2.createStatement();

				ResultSet rs3 = stm.executeQuery(sql6);
				while (rs3.next()) {
					double debt = Double.parseDouble(rs3.getString("debt")
							.trim());
					cust.setDebt(cust.getDebt() + debt);
				}

				rs3.close();
				stm.close();

				if (cust.getDebt() < 0.02 && cust.getDebt() > 0) {
					cust.setDebt(0);
				}

				String sql7 = "SELECT SL01001 AS id,SUM(SL03013-SL03053) AS debt FROM SL030100 INNER JOIN SL010100 ON SL03001 = SL01001  WHERE  "
						+ " NOT(SL03013=SL03053) and SL01001='"
						+ cust.getPayer() + "'  GROUP BY SL01001 ";

				Statement stmr = con2.createStatement();

				ResultSet rs4 = stmr.executeQuery(sql7);
				while (rs4.next()) {
					double xxx = Double.parseDouble(rs4.getString("debt")
							.trim());
					cust.setUsedCredit(xxx);
				}

				rs4.close();
				stmr.close();

				if (goes) {

					customers.put(cust.getName(), cust);
					custMap.put(cust.getID(), cust);
				}
			}
			rs.close();
			stmt.close();
			
			
 			Customer custo = new Customer("KALEV");

			custo.setName("x Kalevi Kontor");
			custo.setPayer("KALEV");
			custo.setAddress("P�rguv�lja tee 6");
			Enumeration enums = getParent().getAgentsHash().elements();
         	  while (enums.hasMoreElements()) {
         	    Agent a = (Agent) enums.nextElement();
         	    custo.addAgent(a);
         	   }
         	 custo.setSalesBlockT(true);
         	 customers.put(custo.getName(), custo);

         	 
  			Customer custox = new Customer("SMARTEN");

			custox.setName("x Smarteni ladu");
			custox.setPayer("SMARTEN");
			custox.setAddress("P�llupealse 1, lennujaama taga");
            custox.setSalesBlockT(true);
			 enums = getParent().getAgentsHash().elements();
         	  while (enums.hasMoreElements()) {
         	    Agent a = (Agent) enums.nextElement();
         	    custox.addAgent(a);
         	   }
         	 customers.put(custox.getName(), custox);

         	 
         	 
		} catch (Exception e) {
			getParent().showError(e);
		}

		 if(rcompany != null){
		//	 Converter.sendCustomerJson(customers, company, true,user,pass);
		    }
		
		
		return customers;
	}

	public Vector getDebt(String agentCode) throws SQLException {
		Vector list = new Vector();
		String sql = "SELECT SL01001 AS klient, SL03002 AS number,SL03006 AS kuupaev,(SL03013-SL03053) AS summa , -datediff(day ,getdate(),SL03006) as yle "
				+ "FROM SL030100 INNER JOIN SL010100 ON SL03001 = SL01001  WHERE NOT(SL03013=SL03053) AND "
				+ "LEN(LTRIM(SL03013))>0 AND SL03006<GETDATE()+5 ";
		sql += " ORDER BY SL01001";

		connectionUp(url);
		stmt = con.prepareStatement(sql);

		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			Debt debt = new Debt();
			debt.setCustID(rs.getString("klient").trim());
			try{
				debt.setDoknr(rs.getString("number").trim());
				debt.setDuedate(rs.getDate("kuupaev"));
				debt.setSum(Double.parseDouble(rs.getString("summa").trim()));
				debt.setDue(rs.getString("yle"));
				if (debt.getSum() > 0.04 || debt.getSum() < -0.04) {
					list.addElement(debt);
				}	
			}catch(Exception e){
				
			}
			
		}
		rs.close();
		stmt.close();
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
					if(mline.getCustID().equals("M15260")){
						//System.out.println("mline " + mline.getCustID() + " " + mline.getPrice());
					}
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

	public TreeMap getProducts(Agent agent) {
		connectionUp(url);
		eans = new TreeMap();
		// 'PR' +

		
		TreeMap<String,Integer> levels = new TreeMap<String,Integer>();

		try {
			
			String cmd = "select ScalaKood, TKaal from [mj_planotooted]";
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);
      while (rs.next()) {
    	  String kood = rs.getString("ScalaKood").trim();
    	  int kaal = rs.getInt("TKaal");
    	  levels.put(kood, kaal);
      }
      
      rs.close();
      st.close();
      
		}catch(Exception e) {
			e.printStackTrace();
		}
		
      
      
			

		
		String cmd = "SELECT  RTRIM(t.SC01001) AS toode,(RTRIM(SC01002)+' ' "
				+ "        +rtrim(SC01003)) + ' ' +RTRIM(t.SC01001) AS nimi,SC39004 AS myygihind ,"
				+ "SC01015 AS tykke,SC03003 AS stock, " //-SC03004
				+ " SC01115  AS grupp,SC01132 AS ean  , SC01016 as letter ,SC01014 as sorting "
				+ " , case when SC01135 = 0 then  'KG' else 'TK' end as unit  ,  SC01141 as kaal, " +
						" OptimumKTellimuseKogus as inunit ,SC01107 as type , SC01164 as kate FROM SC010100 t left outer JOIN SC030100 on SC03001=t.SC01001 and SC03002='SKSM1' AND SC03003!=0 left outer JOIN SC390100 ON t.SC01001 "
				+ "  = SC39001 and SC39002='00' LEFT OUTER JOIN proov20100 yh ON yh.SC01001= t.SC01001 " +
						" LEFT OUTER JOIN class ty ON ty.MasterProductCode = t.SC01001 WHERE   (( RTRIM(t.SC01001) like 'X0%' or  RTRIM(t.SC01001) like 'Y0%' or RTRIM(t.SC01001) like 'R0%') and SC01115 = 'K') or SC01115 = 'L' or"
						+ "  ( LEN(LTRIM(SC01037))>0 and SC39004 > 0 and rtrim(ltrim(SC01016)) != 'S' and SC01014 != '' and SC01014 != 'XXX')  ORDER BY LEFT(RTRIM(SC01037),1),t.SC01001 ";
		
		if (isDebug()) {
			System.out.println(cmd);
		}


		
		TreeMap products = new TreeMap();

		try {
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);
      while (rs.next()) {
			
				
			String kood = rs.getString("toode").trim();
				String nimetus = Converter.enCodeEst(rs.getString("nimi").trim());
 
				Product prod = new Product(kood);
				prod.setEdiname(nimetus);
				prod.setPrice(parseDouble(rs.getString("myygihind").trim()));
				if(prod.getPrice() == 0){
					prod.setPrice(1);
				}
				try{
					prod.setInCases(parseDouble(rs.getString("tykke").trim()));
						
				}catch(Exception e){
					e.printStackTrace();
				}
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
				
				boolean goes = true;
				String sort = rs.getString("sorting");
				if (sort == null || sort.trim().length() == 0) {
					goes = false ;
				}

				if (goes && gr != null && gr.trim().length() > 0) {
					prod.setEAN(gr.trim());
					if(eans.get(gr.trim()) == null){
						eans.put(gr.trim(), prod.getID());
					}
					
					 if(eanprods == null) {
	                	 	eanprods = new TreeMap();
	                   }
	                   eanprods.put(gr.trim(), prod.getID());
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
					if(type.trim().equals("Teemant") || type.trim().equals("A")){
					//	prod.setStatusTop("1");
					}
				}
				String kate = rs.getString("kate");
				if (kate != null && kate.trim().length() > 0) {
					if(kate.trim().equals("CS")){
						prod.setType("KALEV");
					}else{
						prod.setType("FELIX");
					}
					
				}
				
				
				if(levels.get(prod.getID()) != null) {
					prod.points = levels.get(prod.getID());
					prod.setStatusTop("1");
				}
				
				
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
			double discount = 0;
			
			while (rs2.next()) {
				String kuup = rs2.getString("kuup");
				double kog = rs2.getDouble("kogus");
				
				
				
				
				discount = rs2.getDouble("maxdisc");
				if(discount > 0){
					
					if(prod.getMaxDiscount() < discount) {
						prod.setMaxDiscount(discount);	
						prod.setDiffColor(true);
					}
				
					if(kuup != null){
						info = info + "VANAJÄÄK:"
								+ kuup.trim().substring(0, 10)
								+ " kog:" + kog + "| ale:" + (int)discount + "%";		
					}
				}
				
				
				
				if(kog == 0){
					prod.setStatusDiscount("1");
				}
			}
			
			     rs2.close();
			     st2.close();
				prod.setPartii(Converter.enCodeEst(info));
				
				if(goes){
					products.put(nimetus, prod);
				}
			
			}
			rs.close();
			st.close();
			
		
			
			
			try{
				getProductsEeva(products, levels);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			try{
				 String  sql = "select aaadress, planokaal from [mj_planotabel] ";
				 connectionUp(url);
				 st = con.createStatement();
				 rs = st.executeQuery(sql);
			StringBuffer sb = new StringBuffer();
					
					while (rs.next()) {
					    sb.append(rs.getString("aaadress").trim() + ";" + rs.getString("planokaal").trim() + "\n");
					}
					
					rs.close();
					st.close();
				    //con.close();
				    Converter.saveFileInUTF8("displaypoints.txt", sb);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			
			// Converter.sendProductJson(products, company);
		} catch (Exception e) {
			getParent().showError(e);
		}

	
		return products;
	}
	private String remSpace(String s){
		 if (s == null) return s;
		    StringBuffer sb = new StringBuffer();
		    for (int i = 0 ; i < s.length() ; i++){
		      char c = s.charAt(i);
		      if (c != ' '){
		        sb.append(c);
		      } 
		    }
		    return sb.toString();
	}
	
	private void getProductsEeva(TreeMap products, TreeMap<String,Integer> levels){


		try {
			
			
			

			
		   String cmd =
			"SELECT toode,nimetus,asukoht,kastis, myygihind, liik, plaanhind,minkogus,alusel,ribakood,ribakood3, stock1 as laoseis, kood14 as felix FROM toode " +
	               " WHERE len(trim(toode)) > 2 AND len(trim(liik))>0  AND  myygihind > 0 AND len(trim(nimetus))>0 AND eisaada = .F. ORDER BY toode ";

		    if (isDebug()) {
			System.out.println(cmd);
		    }

		    Connection ee = this.getNewConEeva();
		    Statement st = ee.createStatement();
		    ResultSet rs = st.executeQuery(cmd);

		    while (rs.next()) {
				String  kood =  rs.getString("toode").trim();
                String  nimetus = rs.getString("nimetus").trim();
				String asukoht = rs.getString("asukoht").trim();
		    	
		    	double  tykke = (rs.getDouble("kastis"));
			String pre = "FE" ;
			if(kood.startsWith("K0") && nimetus.indexOf("OTB") !=-1){
				pre = "OTB";
			}
             kood = pre + kood;
			Product prod = new Product(kood);
			prod.setName("x" + pre + " " + Converter.enCodeEst(nimetus));
			prod.setPrice(round(rs.getDouble("myygihind"), 2));
			prod.setInCases(tykke);
			prod.setType("OTB");
			
			
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

	                if (status!= null && status.trim().length() > 0){
	                	 if (status.indexOf("T")!= -1){
		                       // prod.setStatusTop("1");
		                      }
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

	                if(levels.get(prod.getID()) != null) {
	                	prod.points = levels.get(prod.getID());
	                	prod.setStatusTop("1");
	                }
	                
	    			String puhas = rs.getString("felix");
	    			String ccc = " select stockcode as toode,  bestbefore as kuup, "
	    					+ "  stockamount as kogus, discount as maxdisc FROM mj_salesproposal   "
	    					+ " WHERE  stockcode= '"+ puhas + "'";
	    //     System.out.println(ccc);
	    			Statement st2 = con2.createStatement();
	    			 ResultSet rs2 = st2.executeQuery(ccc);

	    			if (rs2.next()) {
	    				
	    				String kuup = rs2.getString("kuup");
	    				double kog = 0;
	    				if(kuup != null){
	    					kog = rs2.getDouble("kogus");
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
	
	
	
	
    private String getOutDir(){
        String dir = getParent().getProps().getProperty("readout");
        if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
        return dir;
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

		
			//teema objekti kus on sees tarnimata kaubad 
			String sql6 = "		SELECT  [KaupluseKood] as custid,[Kood] as prodid,Kogus FROM [RestorderKalev] " +
 " where ISNULL([KaupluseKood],0) <> '0' and Tarneaeg > getdate()-10";
			HashMap saamatta = new HashMap();
			
			
			try {
				
			
			PreparedStatement stmt2 = con2.prepareStatement(sql6);
            ResultSet rs2 = stmt2.executeQuery();
			
            while (rs2.next()) {
				String kliendikood = rs2.getString("custid").trim();
				String tootekood = rs2.getString("prodid").trim();
				saamatta.put(kliendikood + tootekood, rs2.getDouble("Kogus") + "");
			}
			
			rs2.close();
			stmt2.close();
			
			}catch(Exception e){
				e.printStackTrace();
			}

			
			String sql2 = "SELECT  SL01001 AS klient ,ST03017 AS toode ,sum(case when ST03015 >= getdate()-30 " +
					  	"	 then ST03020 else 0 end ) as pluss,max(ST03020) AS Kogus,ST03015  as lastdate ,max(ST03014) as arvenr "+
					 "  FROM ST030100 INNER JOIN SL010100 ON ST03001 = SL01001 WHERE ST03015> ? AND ST03016>0 AND ST03020>0 and left(SL01001,2) !='M3'"+
					  "  GROUP BY SL01001,ST03017, ST03015 "+
					  " union "+
					"	SELECT  SL01001 AS klient ,ST03017 AS toode ,sum(case when ST03015 >= getdate()-30 "+
					 " 		 then ST03020 else 0 end ) as pluss,max(ST03020) AS Kogus,ST03015  as lastdate ,max(ST03014) as arvenr "+
					 "  FROM [SQL].[scalaDB].dbo.[ST030800]  INNER JOIN [SQL].[scalaDB].dbo.[SL010800]  ON ST03001 = SL01001 "
					 + "WHERE ST03015> ? AND ST03016>0 AND ST03020>0 and left(SL01001,2) !='M3' "+
					"      GROUP BY SL01001,ST03017, ST03015 "+
					" ORDER BY 1,2 ,3 ASC";

			if (isDebug()) {
				System.out.println(sql2);
			}

			stmt = con.prepareStatement(sql2);
			stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
			stmt.setTimestamp(2, new Timestamp(getStartingDate().getTime()));

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
                     //   System.out.println("Saamatta " + kliendikood + tootekood + " " + ei);
					}

					CustProd cp = new CustProd(kliendikood, tootekood, kogus,
							pluss, 0, date.toString().substring(5, 10), 0, ei,
							0, " ");
					cp.setAbiKood(arvenr);
					 cp.setOpenAmount(ei);
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
			ResultSet rs2 = stmt6.executeQuery(cmd2);

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
		String sql = "SELECT RTRIM(SL01002) AS nimi,OR01001  AS number,OR01015 AS kuupaev, OR01024 AS summa  FROM  " +
                           "    OR010100 INNER JOIN SL010100 ON OR01004 = SL01001 WHERE OR01015 >GETDATE()-1 and OR01019  = ? " + 
                           "    ";

		StringBuffer sb = new StringBuffer();
		this.connectionUp(url);
		try {
			stmt = con.prepareStatement(sql);
			stmt.setString(1, agent.getID());
			String sep = System.getProperty("line.separator");
			ResultSet rs = stmt.executeQuery();
			String header = getParent().getString("name") + "  \t\t"
					+ getParent().getString("number") + "\t"
					+ getParent().getString("sum") + sep
					+ "-------------------------------------------" + sep;
			boolean heade = true;
			while (rs.next()) {
				if (heade) {
					sb.append(header);
					heade = false;
				}

				String nimi = rs.getString("nimi");
				if (nimi == null) {
					nimi = " ";
				} else {
					nimi = nimi.trim();
				}

				sb.append(nimi + "\n");
				sb.append(rs.getString("number").trim() + "\t");
				String sum = getNF().format(
						Double.parseDouble(rs.getString("summa").trim()));
				sb.append(sum + "\n" + sep);
			}
		} catch (Exception e) {
			getParent().showError(e);
		}

		connectionDown();

	//	System.out.println(sb.toString());
		return sb.toString();

	}

	public String getNonMobileOrders(Agent agent) {
		return null;
		
	}

	public void setBulkSales(Map agents) {
		String cmd = "SELECT ST03007 as agent,sum(ST03021*ST03020*(1-ST03022/100)) as sales " +
				"FROM ST030100 WHERE MONTH(ST03015)= MONTH(GETDATE()) AND YEAR(ST03015)=YEAR(GETDATE()) GROUP BY ST03007";
		log(cmd);

		try {
			Connection con = getNewCon();
			Statement stmt = con.createStatement();

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_MONTH, 1);

			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String agentID = rs.getString("agent");
				Agent agent = (Agent) agents.get(agentID);
				if (agent != null) {
					int sales = (int) rs.getDouble("sales");
					agent.setMonthSales(sales);
				}
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			getParent().showError(e);
		}

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
		if (agentCode == null)
			return 0;
		String cmd = "SELECT SUM(OR20024) AS sales ,OR20019 AS agent FROM OR200100 WHERE MONTH(OR20015)="
				+ "MONTH(GETDATE()) AND YEAR(OR20015)=YEAR(GETDATE()) GROUP BY OR20019";
		log(cmd);

		try {
			Connection con = getNewCon();
			Statement stmt = con.createStatement();

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_MONTH, 1);

			ResultSet rs = stmt.executeQuery(cmd);

			while (rs.next()) {
				setSales(rs.getDouble("sales"));
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			getParent().showError(e);
		}

		return 0;
	}

	public double getPromoSales(String agentCode, int nr) throws SQLException {
		return 0;
		/*
		 * String sql =
		 * "SELECT ST02020 AS  kogus, SC01071 AS sisu FROM ST020100 INNER JOIN "
		 * + "SC010100 ON ST02017=SC01001 WHERE ST02007=?" +
		 * " AND YEAR(ST02015)=YEAR(GETDATE()) AND MONTH(ST02015)=MONTH(GETDATE()) AND SC01012=?"
		 * ; PreparedStatement pst = con.prepareStatement(sql); pst.setString(1,
		 * agentCode); //Calendar cal = Calendar.getInstance();
		 * //cal.set(Calendar.DAY_OF_MONTH, 1); //pst.setTimestamp(2, new
		 * Timestamp(cal.getTime().getTime())); pst.setString(2,
		 * String.valueOf(nr)); ResultSet rs = pst.executeQuery(); double total
		 * = 0; while(rs.next()){ double kogus = rs.getDouble("kogus"); String
		 * sisu = rs.getString("sisu"); if (!sisu.equals("")){ total +=
		 * kogus*Double.parseDouble(sisu); } } rs.close(); pst.close();
		 * System.out.println("nr:" + nr + ", total:" + total); return total;
		 */
	}

	public ManagerData getManagerData() throws SQLException {
		String sql = "SELECT clid AS klient,docsum AS summa,linecount AS ridu,text AS tekst,"
				+ "sentlong AS kuupaev FROM dayreport WHERE sentdate>(DATE()-30) ORDER BY clid";
		Connection c = DriverManager.getConnection(url2);
		Statement st = c.createStatement();
		ResultSet rs = st.executeQuery(sql);

		String sql2 = "SELECT SL01001 AS kood, SL01040 AS aastamyyk,SL01041 AS kuumyyk,SY24003 AS agendinimi, SL01006 AS kontaktisik "
				+ ",SL01011 AS telefon, SL01024 AS maksetin,SL01036 AS ale"
				+ " FROM SL010100 LEFT JOIN SY240100 ON SY24002=SL01035 ORDER BY "
				+ "SL01002";
		connectionUp();
		Connection con = getNewCon();
		Statement st2 = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);
		ResultSet rs2 = st2.executeQuery(sql2);

		StringBuffer sb = new StringBuffer();
		String sep = ";";
		while (rs.next()) {
			String cliID = rs.getString("klient").trim();
			sb.append(cliID + sep);
			sb.append(rs.getString("kuupaev").trim() + sep);
			sb.append(rs.getDouble("summa") + sep);
			sb.append(rs.getInt("ridu") + sep);
			String tekst = rs.getString("tekst");
			tekst = tekst.replace('\'', ' ').replace('\"', ' ');
			if (tekst == null)
				tekst = " ";
			else
				tekst = tekst.trim();
			sb.append(tekst + sep);

			boolean has = scrollTo(cliID, rs2);
			if (has) {
				sb.append(ensureLen(rs2.getString("aastamyyk"), "0") + sep);
				sb.append(ensureLen(rs2.getString("kuumyyk"), "0") + sep);
				sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
				sb.append(ensureLen(rs2.getString("kontaktisik"), " ") + sep);
				sb.append(ensureLen(rs2.getString("telefon"), " ") + sep);
				sb.append(ensureLen(rs2.getString("maksetin"), " ") + sep);
				sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
			}

			sb.append("\n");
		}

		ManagerData md = new ManagerData();
		md.setClientVisitInfo(sb.toString());

		sql = "SELECT OR20003 AS klient,OR20019 AS agent,OR20017 AS agentname FROM OR200100 "
				+ "WHERE OR20015>? GROUP BY OR20003,OR20019,OR20017 ORDER BY OR20003";

		PreparedStatement ps = con.prepareStatement(sql);
		ps.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
		rs = ps.executeQuery();
		StringBuffer bf2 = new StringBuffer();
		while (rs.next()) {
			bf2.append(rs.getString("klient").trim() + sep);
			bf2.append(rs.getString("agent").trim() + sep);
			String agentName = rs.getString("agentname");
			if (agentName == null)
				agentName = "";
			bf2.append(agentName + "\n");
		}

		md.setClientAgentInfo(bf2.toString());

		return md;
	}

	private boolean scrollTo(String id, ResultSet rs) throws SQLException {
		rs.beforeFirst();
		while (rs.next()) {
			if (id.equals(rs.getString("kood").trim())) {
				return true;
			}
		}
		return false;
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

	// private String url = "jdbc:DBF:///c://work/avallone/dbf";
	public static final String DRIVER_VERSION = "Kalev";
	private String url = "jdbc:odbc:orklaDB";
	// private String url = "jdbc:freetds:sqlserver://scala/scalaDB";
	private String url2 = "jdbc:odbc:report";
	private String url3 = "jdbc:odbc:eeva";
	
	private Connection con = null;
	private Connection con2 = null;
	private String subjectFilesTo = null;
	private PreparedStatement stmt = null;
	private TreeMap eans = null;
	private TreeMap agents1 = new TreeMap();
	private NumberFormat NF = NumberFormat.getInstance();
	

	public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		this.getProducts(null);
		File f = new File(this.getExcelDir() + "/SortimentDroid.xlsx");
		
		//HashSet teemantid = getTeemant();
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
					 groups.put(a + "", "KA" +Converter.cleanLitLatEst(row.getCell((short) a)
							.getStringCellValue()).trim());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

		//	 System.out.println(groups.toString());

			for (int i = 8; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";

				if (row != null && row.getCell((short) 1) != null) {

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
									try{
										go = (row.getCell((short) a)
												.getStringCellValue());	
									}catch(Exception n){
										
									}
								
								}

							}

							if (go != null && (go.equals("1") || go.equals("2"))) {
								
										
						

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
									
									//mline.setLineinfo("CENTRAL");
									
									//if(teemantid.contains(prodID.trim())){
										//mline.setLineinfo("TOP");
									//}
								}
								
								if (mline.getProdID() == null
										&& prodGroup != null
										&& prodGroup.trim().length() > 0) {
									mline.setProdGroup(prodGroup);
								}
								
								
									
								 
								if(!mline.getCliGroup().equals("KA"))
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
		 getFixedFel(result,null);
		 System.out.println("fixed Suurus baas " + result.size());
		
		return result;

	}


	
	public Vector getFixedFel (Vector result, HashSet teemantid) {
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
					String ssss = prodID;
					
					prodID = (String)eans.get(prodID);
                	
                	//System.out.println(" toode2 " + prodID);
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

							if (go != null && (go.equals("1") || go.equals("2"))) {

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
                                //   mline.setLineinfo("CENTRAL");
									
								//	if(teemantid.contains(prodID.trim())){
									//	mline.setLineinfo("TOP");
									//}
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

		 System.out.println("fixed Suurus FEL " + result.size());
       
		return result;

	}
	
	
	
	
	private HashSet getTeemant() {
		HashSet res = new HashSet();
		String sql = "select SC01001 as toode from orklaDB.dbo.SC010100 "
				+ "where  (SC01107 = 'teemant' or SC01107 = 'A') ";
try{
	
      Connection con = getNewCon();
		Statement ps = con.createStatement();
		ResultSet rs = ps.executeQuery(sql);
		while (rs.next()) {
			res.add(rs.getString("toode").trim());
		}
		rs.close();
		ps.close();
		con.close();
//	System.out.println("Teemantid " + res.size());
		
		
		
		   String cmd =
					"SELECT toode, ribakood3 FROM toode  WHERE len(trim(toode)) > 2  AND len(trim(ribakood3)) > 0 AND eisaada = .F.  ORDER BY toode ";

				    if (isDebug()) {
					  System.out.println(cmd);
				    }

				    Connection ee = this.getNewConEeva();
				    Statement st = ee.createStatement();
				    rs = st.executeQuery(cmd);

				    while (rs.next()) {
				    	String  kood =  "FE" + rs.getString("toode").trim();
		                String  nimetus = rs.getString("ribakood3").trim();
						if(nimetus.indexOf("T") != -1) {
							 res.add(kood);
						}
		               
				    }
				    rs.close();
				    st.close();
				    ee.close();
						
}catch(Exception e){
	e.printStackTrace();
}
	
		return res;

	}

	public TreeMap getAgents1() {
		return agents1;
	}

		public Vector getLastThreeData() {

			Vector result = new Vector();
			try {
				   
	
	  
		        Connection con3 = getNewCon();
				
				
				

						String sq3 = "select * from (\r\n" + 
								"select case when d.OR20004='+' then d.OR20003 else d.OR20004 end AS klient  ,d.OR20015 AS kuup ,r.OR21005 as toode,sum(r.OR21012) as kogus, max(r.OR21008)  as hind, max(d.OR20024) as summa  from OR210100 r INNER JOIN OR200100 d ON r.OR21001=d.OR20001   \r\n" + 
								"									 WHERE   d.OR20024 > 0 AND d.OR20015 >=(select top 1 kuupaev  from ( 	SELECT top 5 jou2.OR20015 as kuupaev \r\n" + 
								"														FROM  OR200100 jou2  where ltrim(case when jou2.OR20004='+' then jou2.OR20003 else jou2.OR20004 end) = ltrim(case when d.OR20004='+' then d.OR20003 else d.OR20004 end)  \r\n" + 
								"													 and jou2.OR20015 > getdate()-30   and jou2.OR20024 > 0 group by (case when jou2.OR20004='+' then jou2.OR20003 else jou2.OR20004 end) , jou2.OR20015 order by jou2.OR20015 desc ) \r\n" + 
								"														  as dd order by kuupaev) and (case when d.OR20004='+' then d.OR20003 else d.OR20004 end) not like 'INTR%'  and r.OR21005 not like 'PP%'\r\n" + 
								"														 GROUP BY case when d.OR20004='+' then d.OR20003 else d.OR20004 end,d.OR20015 ,r.OR21005 \r\n" + 
								"						union all\r\n" + 
								"		select case when d.OR01004='+' then d.OR01003 else d.OR01004 end AS klient  ,d.OR01015 AS kuup ,r.OR03005 as toode,sum(r.OR03011) as kogus, max(r.OR03008)  as hind, max(d.OR01024) as summa  from OR030100 r INNER JOIN OR010100 d ON r.OR03001=d.OR01001   \r\n" + 
								"									 WHERE   d.OR01024 > 0 AND d.OR01015 >=(select top 1 kuupaev  from ( 	SELECT top 2 jou2.OR01015 as kuupaev \r\n" + 
								"														FROM  OR010100 jou2  where ltrim(case when jou2.OR01004='+' then jou2.OR01003 else jou2.OR01004 end) = ltrim(case when d.OR01004='+' then d.OR01003 else d.OR01004 end)  \r\n" + 
								"													 and jou2.OR01015 > getdate()-2   and jou2.OR01024 > 0 group by (case when jou2.OR01004='+' then jou2.OR01003 else jou2.OR01004 end) , jou2.OR01015 order by jou2.OR01015 desc ) \r\n" + 
								"														  as dd order by kuupaev) and (case when d.OR01004='+' then d.OR01003 else d.OR01004 end) not like 'INTR%' and r.OR03005 not like 'PP%'\r\n" + 
								"														 GROUP BY case when d.OR01004='+' then d.OR01003 else d.OR01004 end,d.OR01015 ,r.OR03005) as ll \r\n" + 
								"														  ORDER BY 1,2 desc ,3" ;
						
					 
						Statement st3 = con3.createStatement();
						ResultSet rs3 = st3.executeQuery(sq3);
						while (rs3.next()) {
							MatrixLine ml = new MatrixLine();
							ml.setCustID(rs3.getString("klient").trim());
							String kuup = rs3.getString("kuup");
							ml.setStartDate(kuup.substring(5,10));
							String toode = rs3.getString("toode");
							if(toode != null) {
								ml.setProdID(toode.trim());
								ml.setPrice(rs3.getDouble("kogus"));
								ml.discount = rs3.getDouble("hind");
								double summa = rs3.getDouble("summa");
								ml.setLineinfo(kuup.substring(0,10)+ " ("+ (int)summa + "€)" );
							    
								result.add(ml);
							}
							
						}
						rs3.close();
						st3.close();
						con3.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
System.out.println("lastthree läbi" + result.size());
			return result;
		}
	

	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSales(HashMap custprods) throws SQLException {
		 System.out.println("TEEME SALESREPORTIT");
	      Iterator iter = custprods.keySet().iterator();
	      String dlr = (String)iter.next();
	      String sqly =" INSERT INTO rid VALUES('T',99999, '', ?, ?, ?,?,'K','',0,'',?,?,'', " +
	           "?,MONTH(date()-20),YEAR(date()-20),0,0,? ,'','','',0,0,'','','',0,0,.F.,.F.,0,'',0)" ;

	      
	      String sql = "INSERT INTO  feliximyygid values(" +
	      		" ? ," + // salesdate datetime ,
	      		" ? ," + // agentid nvarchar(10) ,
	      		" ? ," + //agentname nvarchar(100) ,
	      		" ? ," + //smacustid nvarchar(20) ,
	      		" ? ," + // felcustid nvarchar(20) ,
	      		" ? ," + // custname nvarchar(200) ,
	      		" ? ," + // prodid nvarchar(20) ,
	      		" ? ," + // prodname nvarchar(200) ,
	      		" ? ," + //barcode nvarchar(20) ,  
	      		" ? ," + // smaprodid nvarchar(20) ,
	      		" ? ," + // felixprodid nvarchar(20) ,
	      		" ? ," + // qty numeric(18,7) ,
	      		" ? )" ; //price numeric(18,7) "
	      
	       String sql1 = "SELECT kood , viitenr as koodfel,objekt2 as agent, y.nimetus as agentname, nimi as custname FROM KLIENT k left join  " +
           " yksus y on y.yksus =objekt2 and y.klassif = 2 WHERE kood1 = ? OR smartsaaja= ?";
	       
	       
	       String sql2 = "SELECT toode,nimetus as prodname,ribakood as barcode,kood14 as felprodid FROM toode WHERE kood1 = ?  OR ribakood= ?";
	   	Connection con = getNewConnection2();
	       PreparedStatement pst = con.prepareStatement(sql);
	       iter = custprods.keySet().iterator();
	       while (iter.hasNext()) {
	         String dealer = (String) iter.next();
	         ArrayList al = (ArrayList) custprods.get(dealer);

	         for (int i = 0; i < al.size(); i++) {

	           String owncustid = "MUUKLIENT";
	           String felcustid = "FELKLIENT";
	           String ownprodid = "11111111";
	           String agent = "000";
	           String agentname = "noname";
	           String custname = "noname";

	           CustProd p = (CustProd) al.get(i);
	           String custid = p.getCustID();
	           String prodid = p.getProdID();
	           java.sql.Date daatum = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
	           if(p.getReportmonth() != null){
	           	daatum = p.getReportmonth();
	           }

	           Connection cc = getNewConEeva();
	           PreparedStatement pst1 = cc.prepareStatement(sql1);
	           pst1.setString(1, custid);
	           pst1.setString(2, custid);
	           ResultSet rs = pst1.executeQuery();
	           while (rs.next()) {
	             owncustid = rs.getString("kood").trim();
	             felcustid = rs.getString("koodfel").trim();
	             agent = rs.getString("agent").trim();
	             String nn = rs.getString("agentname");
	             if(nn != null){
	            	  agentname = nn.trim();
	             }
	             nn = rs.getString("custname");
	             if(nn != null){
	            	 custname = nn.trim();
	             }
	            
	           }
	           rs.close();
	           pst1.close();
            
	           String prodname = "noname";
	        String    barcode = "0000000000000";
	        String    smaprodid = " ";
	        String    felprodid = "nocode";
	           PreparedStatement pst2 = cc.prepareStatement(sql2);
	           pst2.setString(1, prodid);
	           pst2.setString(2, prodid);
	           ResultSet rs2 = pst2.executeQuery();
	           while (rs2.next()) {
	             ownprodid = rs2.getString("toode").trim();
	             prodname = rs2.getString("prodname").trim();
	             barcode = rs2.getString("barcode").trim();
	             felprodid = rs2.getString("felprodid").trim();
          }
	           rs2.close();
	           pst2.close();
	           cc.close();
	           
	  
      		
	           
	           pst.setTimestamp(1, new Timestamp(daatum.getTime()));
	           pst.setString(2, agent);
	           pst.setString(3, agentname);
	           pst.setString(4, custid);
	           pst.setString(5, felcustid);
	           pst.setString(6, custname);
	           pst.setString(7, prodid);
	           pst.setString(8, prodname);
	           pst.setString(9, barcode);
	           pst.setString(10, smaprodid);
	           pst.setString(11, felprodid);
	           pst.setDouble(12, p.getMovedStockPluss());
	           pst.setDouble(13, p.getLastPrice());
	           pst.executeUpdate();
	           
	          
	           
	         }
	       }

	       pst.close();
	       con.close();
	}

	@Override
	public void blockClients(HashMap codes) throws SQLException {
			
	}

	@Override
	public void PictureMaker() {
		// teeme info toomise tabelisse kampaania hinnad 
	 	Calendar viim5 = Calendar.getInstance();
	 	viim5.add(Calendar.DAY_OF_YEAR, -5);
	 	Calendar tana = Calendar.getInstance();
	 	tana.add(Calendar.DAY_OF_YEAR, 1);
	 	NumberFormat NF2 = NumberFormat.getInstance();
	 	NF2.setMinimumFractionDigits(2);
	 	NF2.setMaximumFractionDigits(2);
	 	
	 	
	
		String filename =  Converter.getSVData(viim5.getTime(), tana.getTime(), rcompanyx, user, pass,false,"PRODUCT",null,"product");
		ArrayList<Toode> ttt = getListingdata(filename);
		
		
			       try {
			    	// nüüd teeme test objekti et koos signatuuridega et kas see rida juba pole olemas 
			    	   
			    	 String sql = "select ltrim(Kliendikood) +''+ ltrim(Tootekood) +''+ ltrim(CAST(round(KampaaniaHind,2) AS varchar))"
			    	 		+ " as signa from mj_kampaaniahinnad where Created > getdate()-6";
					      
			  		 HashSet signas = new HashSet();
			    	   
			    		Connection con = getNewCon();
			 			Statement ps = con.createStatement();
			 			ResultSet rs = ps.executeQuery(sql);
			 			while (rs.next()) {
			 				String ss = rs.getString("signa").trim();
			 				signas.add(ss.substring(0, ss.length()-2));
			 			 }
			 			rs.close();
			 			ps.close();
			 			
			 		 
			 			// nyyd vaatame kas selliste sigadega on meil objetis asju ja teeme uue ArraListi neile keda tabelis veel pole
			 			ArrayList<Toode> toDB = new ArrayList<Toode>();
			 			for(Toode tt: ttt) {
			 				
			 				String signa = tt.getGr2() + tt.getID() + NF2.format(tt.getKogus()).replaceAll(",","."); 
			 				System.out.println("aaa " + signa + " " + signas);
			 				if(!signas.contains(signa)) {
			 					toDB.add(tt);
			 				}
			 				
			 			}
			 			
			 			System.out.println("toDB " + toDB.size());
			 			//Lisame siis baas ja ennem leiame ka ID 
			 			
			 			for(Toode tt : toDB) {
			 				
			 				// uurime mis on tema ID baasis .
			 				
			 				sql = " SELECT CampaignID as idd ,InvoiceCustomer as payer FROM orklaDB.dbo.PXCampaignViewClickView0100 " + 
			 						" where CampaignStatusText != 'Suletud' and "
			 						+ "InvoiceCustomer = (select SL01056 from SL010100 where SL01001 = '" + tt.getGr2()+ "') "
			 								+ "and CampaignRowStockCode ='" + tt.getID() + "'";
						      System.out.println(sql);
					     ps = con.createStatement();
					 	 rs = ps.executeQuery(sql);
					 	
					 	 String id = null;
					 	 String payer = null;
					 	 while (rs.next()) {
					 				id = rs.getInt("idd")+ "";
					 				payer = rs.getString("payer");
					 			}
					 			rs.close();
					 			ps.close();
					 			
					 			 System.out.println("id " + id + " " + payer);
					 			
			 				if(id != null) {
			 				//	Created  Datadate Maksjakood  Kliendikood Agendikood  KampaaniaID Tootekood Tavahind KampaaniaHind  KliendileheHind Kommentaar 
			 					sql = "INSERT INTO mj_kampaaniahinnad VALUES (getdate(),?,'" + payer + "','" + tt.getGr2() +"','" + tt.getBrand() + "',"
			 							+ id +",'" + tt.getID() + "'," + tt.getDonecur()+ "," + tt.getKogus()+",0,?)";
			 					PreparedStatement psa = con.prepareStatement(sql);
			 					psa.setTimestamp(1,new  java.sql.Timestamp(Long.parseLong(tt.getLadu())));
			 					psa.setString(2, tt.comment);
			 				    psa.executeUpdate();
			 					con.commit();
			 					psa.close();
			 				}
			 			}
			 			con.close();
			       }catch(Exception k ) {
			    	   k.printStackTrace();
			       }
	
		
		System.exit(0);
	}
	
	private ArrayList getListingdata(String filename) {
		ArrayList res = new ArrayList();
		try {
			
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(filename));
	         org.json.simple.JSONArray conList = (org.json.simple.JSONArray) obj;
	         
	         Iterator<org.json.simple.JSONObject> iterat = ((List) conList).iterator();
	         
	         while (iterat.hasNext()) {
	        	 org.json.simple.JSONObject cob = iterat.next();
	        	 String type = (String)cob.get("resstr2");
	        	 if(type != null ) {
	        		String customer = (String)cob.get("custid"); 
	        		String product = (String)cob.get("prodid"); 
	        		String agentid = (String)cob.get("agentid"); 
	        		String comment = (String)cob.get("comment"); 
	        		String sentdate  = (Long)cob.get("sentdate")+""; 
	        		Double hind = null; 
	        		try {
	        			hind = (Double)cob.get("normalpr");
	        		}catch(Exception e ) {
	        			
	        		}
	        		
	        		Double khind = null;
	        		
	        		try {
	        			khind = (Double)cob.get("actiontota");
	        		}catch(Exception e ) {
	        			
	        		}
	        		//kui on kampaania hind pandud 
	        		if(khind != null && khind > 0 ) {
	        			
	        		
	        		Toode tt = new Toode(product);
	        		tt.setGr2(customer);
	        		if(agentid != null) {
	        			tt.setBrand(agentid);
	        		}
	        		tt.comment = comment;
	        		
	        		if(hind != null && hind > 0 ) {
	        			tt.setDonecur(hind);
	        		}
	        		
	        		tt.setKogus(khind);
	        		try {
	        			tt.setGr1((String)cob.get("prodname")); 
	        		}catch(Exception k) {
	        			
	        		}
	        		
	        		//tt.setGr2(type);
	        		tt.setLadu(sentdate);
	        		
	        		res.add(tt);
	        	 }
	        	 } 
	         }
	         
		}catch(Exception e) {
			e.printStackTrace();
		}
	
        	
		return res;
	}
	
	
}

/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

