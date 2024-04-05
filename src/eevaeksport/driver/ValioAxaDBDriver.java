package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * Title: Eeva eksport Description: Copyright: Copyright (c) 2002 Company:
 * 
 * @author Tarmo Tubro
 * @version 1.0BulkUpdater,Discounter,Manager
 */

public class ValioAxaDBDriver extends AbstractDBDriver implements Manager,
		Promo, BulkUpdater, Discounter,FixLoader ,PictureMaker{
	public ValioAxaDBDriver(EksportPanel parent) {
		super(parent);
		this.subjectFilesTo = parent.getString("filemsg");
		// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
		String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	    user =  getParent().getProps().getProperty("rest.user");
	    pass = getParent().getProps().getProperty("rest.pass");
	    rcompany = getParent().getProps().getProperty("rest.company");
	
		PropertyConfigurator.configure("log4j.properties");
		try {
			Class.forName(databaseDriverName).newInstance();
			con = getNewConnection();
			stmt = con.createStatement();
		} catch (Exception e) {
			parent.showError(e);
			
		}
	}

	
	  String user =  null;
	   String pass = null;
	   String rcompany = null;
	
	
	public void connectionUp() {
	}

	public String getSaveDebtInfo() {
		return null;
	}
	
	   private TreeMap eanprods = null;
	  
	   @Override
		public void FixLoader() {
		   
		  boolean hasFile = Converter.hasFixFromExcels("excelsort",parent);
		   if(hasFile) {
		   eanprods =  Converter.loadMore();
			  
			  if(eanprods == null) {
				  eanprods = new TreeMap();
			  }
			System.out.println("EanProds " + eanprods.toString());
			this.getProducts(null);
			
			TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
			System.out.println("Contracts " + contracts.keySet().size() + " " + contracts.keySet().toString());
			if(contracts.size() > 0) {
				Converter.sendContractJson(contracts ,  rcompany,  user ,  pass);
			}
			
		     } else {
			   System.out.println("No sortiments to read");
		   }
			System.exit(0);
		}
	

	   private StringBuffer oorders = null;
	 private TreeMap  getOpenOrders(){
			TreeMap res = new TreeMap();
			oorders = new StringBuffer();
			oorders.append(Calendar.getInstance().getTimeInMillis() + "\n");
		
			
//customerorders tee lisaks 
			   String sql2 =  "select  sal.salesid AS id, sal.custaccount AS kkood , sal.salesid as number ,sal.createddatetime AS kuup, estimate AS summa " + 
			   		"	from SalesTable sal with (nolock)\r\n" + 
			   		"	WHERE sal.dataareaid='liv' AND sal.createddatetime > getdate()-1 \r\n" + 
			   		"	ORDER BY sal.createddatetime" ;
		
		try {
		   nf.setMinimumFractionDigits(2);
		   Connection con = this.getNewConnection();
		   PreparedStatement stmt2 = con.prepareStatement(sql2);
		   ResultSet  rs = stmt2.executeQuery();


		while(rs.next()){
			StringBuffer sb = new StringBuffer();
			String id = rs.getString("id");
			 
		    String nimi = rs.getString("kkood");
		    if (nimi == null){
		    nimi = " ";
		    } else {nimi = nimi.trim();}

			//DPP00488444 (‘Pabeigts’ – 01.06.2018; merisoft).
		    sb.append(rs.getString("number").trim() + " (");
		    String kuup =(rs.getString("kuup").trim().substring(0,10)) ;
			sb.append(kuup  + ") ");
			

			double summa = Double.parseDouble(rs.getString("summa").trim());
			if(summa > 0 ){
				String sum = getNF().format(summa) ;
			    sb.append(sum + " eur KM-ga|" );	
			}else{
				 sb.append("|" );	
			}

		  	oorders.append(nimi + ";" + summa + ";" + kuup + "\n" );
		  
		    String sql3 = "select qtyordered as amount,  salesunit as unit  , name from  " +
					   " SalesLine rid where rid.dataareaid='liv' and  salesid =  '" + id + "'";
		    Connection cc = this.getNewConnection();
		    PreparedStatement stmt3 = cc.prepareStatement(sql3);
			 
		       ResultSet  rs2 = stmt3.executeQuery();
		   
			    while(rs2.next()){
			    	sb.append("[" +nf.format(rs2.getDouble("amount"))+"] " 
			       + rs2.getString("unit").replaceAll("\\r\\n|\\r|\\n", " ").trim()+ " " + rs2.getString("name").replaceAll("\\r\\n|\\r|\\n", " ").trim() + "|");
			    }
		   
			    rs2.close();
		       stmt3.close();
		       cc.close();
		       if(res.get(nimi) != null){
		    	   res.put(nimi,res.get(nimi) +" | |" + sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
		   	       
		       }else{
		    	   res.put(nimi, sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
		   	    	   
		       }
		   	
		}
		
		
		rs.close();
		stmt2.close();
		con.close(); 	 
			
		
		} catch (Exception e) {
		  getParent().showError(e);
		}

		Converter.saveFile(parent.getProps().getProperty("savefilesdir")  + "customerorders.txt", oorders.toString());
			
			return res;
		}
		


	  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

	  public static String getYYYYpMMpDDpString(Date dd){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    cal.setTime(dd);
		    
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}

			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}
			sb.append(day );
			
			sb.append("." + month  + ".");
			sb.append("" + cal.get(Calendar.YEAR));
			
		    return sb.toString();
		  }
	  
	
		
		 public TreeMap getDegu() {
				TreeMap result = new TreeMap();
				File f = new File(this.getExcelDir() + "tooteesitlused.xls");
				try {
					FileInputStream ff = new FileInputStream(f);
					HSSFWorkbook workbook = new HSSFWorkbook((ff));
					HSSFSheet sheet = workbook.getSheet("Sheet1");
					HSSFRow row = null;

					// System.out.println(groups.toString());

					for (int i = 1; i <= sheet.getLastRowNum(); i++) {
						row = sheet.getRow(i);
						String custID = "";
						try{
						if (row.getCell((short) 0) != null) {

							try {
								custID = (int) (row.getCell((short) 0)
										.getNumericCellValue()) + "";
							} catch (Exception e) {
								custID = (row.getCell((short) 0).getStringCellValue());
							}
							
		                       if(!custID.equals("0")){
		                    	
							// Siin l�heme reasisse rokkima ja vaatama kas se toode
							// l�heb kaasa
		                    		try {
							   long date = (long) row.getCell((short) 2).getNumericCellValue();
							   
							   GregorianCalendar gc = new GregorianCalendar(1900, Calendar.JANUARY, 1);
							   gc.add(Calendar.DATE,(int) date-2);
							   Calendar  dd = Calendar.getInstance();
							   dd.setTimeInMillis(gc.getTimeInMillis());
							   dd.add(Calendar.DAY_OF_YEAR, -6);
							   Calendar  bb = Calendar.getInstance();
							   bb.setTimeInMillis(gc.getTimeInMillis());
							   bb.add(Calendar.DAY_OF_YEAR, 1);
							   System.out.println(getYYYYpMMpDDpString(gc.getTime()) + " " +bb.getTime().before(new Date()) +
									   " " + getYYYYpMMpDDpString(bb.getTime())
									   + " " + dd.getTime().after(new Date()) + " " + getYYYYpMMpDDpString(dd.getTime()));
										 
						      if(new Date().after(dd.getTime()) && (new Date()).before(bb.getTime()) ){
						    	 
						    	  String text = "Degu: " + getYYYYpMMpDDpString(gc.getTime()) + " " +  row.getCell((short) 3).getStringCellValue() + " " + row.getCell((short) 4).getStringCellValue()+
						    	   "|" +  row.getCell((short) 5).getStringCellValue()  ;
						    	  try{
						    		  text = text +  "|" +  row.getCell((short) 6).getStringCellValue();
						    	  }catch(Exception ee){
						    		  
						    	  }
						    	  System.out.println(custID + " " + text);
						    	  if(result.get(custID)== null){
							    	  result.put(custID,text);  
						    	  }else{
	                               String tt = (String)result.get(custID);
							    	  result.put(custID,tt+ "|" + text); 
						    	  }
						           }
										
		                    		} catch (Exception e) {
		                    		e.printStackTrace();
		    						}
							
		                    		
							
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
	
	
	public Vector getFixedAssortmentData() {
			Vector result = new Vector();
			return result;
		}
	
		 

	public Vector getPriceMatrixData() {
		Vector lines = new Vector();
		String sql = " SELECT (case when accountcode = 0 then accountrelation else ''end) AS custid, \r\n" + 
				"			(case when accountcode = 1 then accountrelation else ''end)  AS clgroup, \r\n" + 
				"				(case when itemcode = 0 then itemrelation else ''end) AS prodid, \r\n" + 
				"				(case when itemcode = 1 then itemrelation else ''end) AS prodgroup,percent1 AS discount,amount \r\n" + 
				"				 AS price ,fromdate , todate , champaintype  as type FROM pricedisctable where \r\n" + 
				"				 fromdate<getdate() and todate>getdate() AND \r\n" + 
				"				 fromdate!=('1900-01-01 00:00:00.00') and dataareaid = 'liv' order by (case when accountcode = 0 then accountrelation else ''end),fromdate";
		try {

			Statement stmt = this.getNewConnection().createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {

				String custID = rs.getString("custid");
				String clGroup = rs.getString("clgroup");
				String prodID = rs.getString("prodid");
				String prodGroup = rs.getString("prodgroup");
				double discount = rs.getDouble("discount");
				double price = rs.getDouble("price");
				String sdate =  rs.getString("fromdate");
				String tdate =  rs.getString("todate");
				String type =   rs.getString("type");
				MatrixLine mline = new MatrixLine();

				if (custID != null && custID.trim().length() > 0) {
					mline.setCliGroup(custID.trim());
				}
				
				if (tdate != null) {
					mline.setEndDate(tdate.substring(0, 10));
				}
				
				if (sdate != null) {
					mline.setStartDate(sdate.substring(0, 10));
				}
				
				if (mline.getCustID() == null && clGroup != null
						&& clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
				}
				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}
				if (prodGroup != null && prodGroup.trim().length() > 0) {
					mline.setProdGroup(prodGroup.trim());
				}
				if (mline.getProdID() == null && prodGroup != null
						&& prodGroup.trim().length() > 0) {
					mline.setProdGroup(prodGroup);
				}
				if(discount > 0) {
				   mline.setDiscount(discount);
				} else if (price != 0) {
					mline.setPrice(price);
				}
				
				if(type != null && type.trim().length() > 0) {
					mline.setLineinfo(type);
				}
				
				lines.add(mline);
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return lines;
	}

	public String getProductSortingData() {
		return null;
	}

	public Vector getDebt(String agentCode) throws SQLException {
		return this.debt;
	}

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAssortmentStr(Agent agent) {
		return null;
	}

	public String getSaveStockInfo() {
	
		return null;
	}

	public String getDealerCustomerCSV(String agent) {
		return null;
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
		String cmd = "select emplid as kood ,pclfirstname + ' ' + pcllastname as nimi from empltable  where dataareaid='liv' and dimension = '104'";
		if (isDebug())
			System.out.println(cmd);
		TreeMap agents = new TreeMap();
		try {
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				Agent agent = new Agent(rs.getString("kood").trim());
				String name = rs.getString("nimi").trim();
				agent.setName(name);
				agents.put(name, agent);
                agents1.put(agent.getID(), agent);
			}
		} catch (Exception e) {
			getParent().showError(e);
		}
		
		 Agent aaa = new Agent("PAIG");
		 aaa.setName("Paigutaja");
		 agents.put("Paigutaja", aaa);
		 
		 
		// getFixedAssortmentData();
		return agents;
	}


	   public TreeMap getAgents1() {
		   
			return agents1;
		}
	   

	   private TreeMap agents1 = new TreeMap();
	
	public TreeMap getCustomers(Agent agent) {
		TreeMap degu = getDegu();
		
		  TreeMap openOrders = this.getOpenOrders();
		  
		  try{
	       	   Converter.getManagersReplacements(agents1, rcompany, user, pass); 
	       		
	       }catch(Exception e){
	       	e.printStackTrace();
	       }

	    	 TreeMap webmerc4856 =  Converter.getWebMercagents(rcompany, user, pass,false,"4856");
	    	 TreeMap webmerc4542 =  Converter.getWebMercagents(rcompany, user, pass,false,"4542");
	    	 TreeMap webmerc4545 =  Converter.getWebMercagents(rcompany, user, pass,false,"4545");
	    	 TreeMap webmerc4705 =  Converter.getWebMercagents(rcompany, user, pass,false,"4705");
	    	 TreeMap webmerc0061 =  Converter.getWebMercagents(rcompany, user, pass,false,"0061");
	    	 TreeMap webmerc5249 =  Converter.getWebMercagents(rcompany, user, pass,false,"5249");
	    	 TreeMap webmerc0099 =  Converter.getWebMercagents(rcompany, user, pass,false,"0099");
			 
		
		String cmd = "SELECT cus.name, cus.accountnum,(case when len(cus.invoiceaccount)=0 then cus.accountnum else cus.invoiceaccount end )AS payer,   "+
				" (SELECT sum(amountmst) FROM custtransopen co WHERE co.duedate<getdate() " +
				 "   AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else cus.invoiceaccount end ) and dataareaid ='liv') AS debt,regnum_w as regnr, cus.linedisc , " +
			     "   street+ ' '  + city as address, visitingdays AS visit , cus.salesmanager AS agent ,paymtermid AS term,  blocked AS block , custinventsort as grupp  , " +
					"  (select rtrim(cp.name) + ' ' + rtrim (cp.phone) + ' ' + rtrim(cus.phone) from contactperson cp where cp.contactpersonid=cus.contactpersonid and cp.dataareaid ='liv'   ) as kontakt " + 
				   " FROM custtable cus WHERE  cus.dataareaid='liv'  and cus.salesmanager != '' " +
			    " AND blocked != 2  order by cus.name";

		String sql = "SELECT  a.accountnum AS maksja,a.orderaccount as klient,a.invoice AS number,co.duedate AS maksepaev,\r\n" + 
				"				co.amountmst AS tasumata,co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle  FROM custtransopen co\r\n" + 
				"						 INNER JOIN custtrans a ON co.REFRECID=a.recid and a.dataareaid=co.dataareaid \r\n" + 
				"				WHERE co.dataareaid='liv' AND a.dataareaid='liv'  ORDER BY a.accountnum,co.transdate";


		if (isDebug())
			System.out.println(cmd);
		TreeMap customers = new TreeMap();
		TreeMap custMap = new TreeMap();
		Vector showSup = new Vector();
	    ketid = new HashMap();
	    sortimendid = new HashMap();
		try {
			Connection ccc =   this.getNewConnection();
			Statement stmt = ccc.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String name = rs.getString("name").trim();
				String accountnum = rs.getString("accountnum").trim();
				Customer cust = new Customer(accountnum);
				
				cust.setName(name);
				String payer = rs.getString("payer");
				if(payer != null && payer.trim().length() != 0){
					cust.setPayer(payer.trim());
					cust.setGroup(payer.trim());
					
					String debtStr = rs.getString("debt");
					
					
					if (debtStr != null) {
						
						if(!payer.trim().equals("102") 
								 && !payer.trim().equals("100")
								 && !payer.trim().equals("104")
								 && !payer.trim().equals("103")
								 && !payer.trim().equals("106")
								 && !payer.trim().equals("108") 
								 && !payer.trim().equals("107")
								 && !payer.trim().equals("101")
								 && !payer.trim().equals("109") 
								){
							    //  cust.setDebt(Double.parseDouble(debtStr));
							}
						
					}
				}
				
				String regnum = rs.getString("regnr");
				if(regnum != null && regnum.trim().length() != 0){
					if(ketid.get(regnum.trim()) != null){
	            		Vector vv = (Vector)ketid.get(regnum.trim());
	            		vv.add(cust.getID());
	            		ketid.put(regnum.trim(), vv);
	            	}else{
	            		Vector vv = new Vector();
	            		vv.add(cust.getID());
	            		ketid.put(regnum.trim(), vv);
	            	}	
				}
				

				String aleke = rs.getString("linedisc");
				if (aleke != null && aleke.trim().length() > 0) {
					aleke = aleke.trim();
					//cust.setGroup(aleke);
				} else {
					aleke = "000";
				}
				
				String inf = null;
				if(degu.containsKey(accountnum)){
					inf = (String) degu.get(accountnum);
					cust.setInfo(inf);
				}

				String aadr = rs.getString("address");
				if (aadr != null) {
					cust.setAddress(aadr.trim().replace('\n', ' '));
					// System.out.println(aleke +aadr.replace('\n',' '));
				}
				  
				  
				  if(webmerc4856.get(cust.getID()) != null) {
	            		 cust.addAgent(new Agent("4856")); 
	              }
	             if(webmerc4542.get(cust.getID()) != null) {
         		 cust.addAgent(new Agent("4542")); 
               }
	             if(webmerc4545.get(cust.getID()) != null) {
         		 cust.addAgent(new Agent("4545")); 
               }
	             if(webmerc4705.get(cust.getID()) != null) {
         		 cust.addAgent(new Agent("4705")); 
               }
	             
	             if(webmerc0061.get(cust.getID()) != null) {
         		 cust.addAgent(new Agent("0061")); 
               }
	             if(webmerc5249.get(cust.getID()) != null) {
	         		 cust.addAgent(new Agent("5249")); 
	               }
	             if(webmerc0099.get(cust.getID()) != null) {
	         		 cust.addAgent(new Agent("0099")); 
	               }
		              

 	         	String ag = rs.getString("agent").trim();
	             if (ag != null ){
        				Agent agg = ((Agent)this.getAgents1().get(ag));
      				
        				if(agg != null && agg.getDubbler().size()>0){
        					//System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
        					for (int r = 0 ; r < agg.getDubbler().size() ; r++){
        						Agent dub = new Agent((String)agg.getDubbler().elementAt(r));
        						cust.addAgent(dub);
        					}
        				}
        			}
				
				
				    
				int block = rs.getInt("block");
				if (block == 1) {
					cust.setSalesBlock(true);
				}

				String grupp = rs.getString("grupp");
				if (grupp != null && grupp.trim().length() != 0) {
					cust.setGroup(grupp);
					sortimendid.put(cust.getID(), grupp);
				}

				String kontakt = rs.getString("kontakt");
				if (kontakt != null && kontakt.trim().length() != 0) {
					cust.setPersonalData(kontakt.replaceAll(";", ",").replaceAll("  ", " "));
				}

				
				if(openOrders.get(cust.getID()) != null){
		        	cust.addToCustDataVec(cust.getID() + ";tarasaldo; ;Avatud Tellimused |"   +  openOrders.get(cust.getID()));
		        }
				
				
				customers.put(name, cust);
				custMap.put(cust.getID(), cust);
			}

			/*rs = stmt.executeQuery(sql);
			while (rs.next()) {
				Debt debt = new Debt();
				debt.setPayerID(rs.getString("maksja").trim());
				debt.setCustID(rs.getString("klient").trim());
				
				String number = rs.getString("number").trim();
				debt.setDoknr(number);
				debt.setDuedate(rs.getDate("maksepaev"));
				debt.setSum(rs.getDouble("tasumata"));
				debt.setDate(rs.getDate("kuupaev"));
				debt.setDue(rs.getString("yle"));
				if(!debt.getPayerID().equals("102") 
						&& !debt.getPayerID().equals("100") 
						 && !debt.getPayerID().equals("106")
						  && !debt.getPayerID().equals("104")
						   && !debt.getPayerID().equals("103")
						 && !debt.getPayerID().equals("108") 
						 && !debt.getPayerID().equals("107")
						 && !debt.getPayerID().equals("101")
						 && !debt.getPayerID().equals("109") 
						){
				//	this.debt.addElement(debt);	
				}
			}
*/
			
		} catch (Exception e) {
			getParent().showError(e);
		}
		
		return customers;
	}

	private Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(url, getParent().getProps()
				.getProperty("dsn.user"),
				getParent().getProps().getProperty("dsn.pass"));
	}
	
	private Connection getNewConnectionW() throws SQLException {
		return DriverManager.getConnection(urlw, getParent().getProps()
				.getProperty("dsn.user"),
				getParent().getProps().getProperty("dsn.pass"));
	}
	private Connection getNewConnectionRep() throws SQLException {
		return DriverManager.getConnection(url2, getParent().getProps()
				.getProperty("dsn.user"),
				getParent().getProps().getProperty("dsn.pass"));
	}

	public String getDistribution() {
		return null;
	}

	private TreeMap pgr1 = null;
	private TreeMap pgr2 = null;
	private HashMap g1g2 = null;
	
	public TreeMap getProdGroups() throws SQLException {
		this.setTreeGroup(true);
       return pgr1;
	}
	
	public TreeMap getProdGroupsInit() throws SQLException {
		
		pgr1 = new TreeMap();
		pgr2 = new TreeMap();
		g1g2 = new HashMap();
		
		Connection con = getNewConnection();
		TreeMap tm = new TreeMap();
		String sql = " select gr.articleclass as liik ,gr.name AS nimi , articleclassgroupid as gr2 from \r\n" + 
				"		valarticleclass gr inner join inventtable t on t.articleclasstype=gr.articleclass and t.dataareaid=gr.dataareaid\r\n" + 
				"	    WHERE t.dataareaid='liv' and gr.dataareaid='liv' \r\n" + 
				"		group by gr.articleclass,gr.name, articleclassgroupid order by  gr.name ";
		
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);


		while (rs.next()) {
			String liik = rs.getString("liik").trim();
			String nimi = rs.getString("nimi").trim();
			String gr2 = rs.getString("gr2").trim();
			try {
			String gg2 = gr2.substring(0,3)+"0";
			try {
				gg2 = gr2.substring(0, 4);
			} catch(Exception e) {
				
			}
			pgr1.put(gg2, gr2);
			pgr2.put(gg2 + liik, nimi.replaceAll("Atleet", "At. "));
			g1g2.put(liik, gg2);
			tm.put(liik, nimi);
            } catch(Exception e) {
				
			}
		}
		rs.close();
		st.close();
		con.close();

		return tm;
	}

	public TreeMap getProdGroupsSecond() throws SQLException {
		return pgr2;
	}

	public TreeMap getProducts(Agent agent) {

		prodsH = new HashSet();
		try {
			getProdGroupsInit();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		String cmdo = "select [PRODUCT ID] as toode from dbo.IBP_SHistory_2M_ed_v";
		
		
		String cmd = "SELECT inv.itemid,(inv.itemname) AS nimetus, taxpackagingqty AS kastis,    price "
				+ ", itembarcode AS ean,articleclasstype AS grupp ,unitid AS unit, 0 AS yhikukogus FROM inventtable inv "
				+ "  INNER JOIN inventtablemodule mod ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid  "
				+ "  LEFT JOIN inventitembarcode bc on bc.itemid=inv.itemid and bc.dataareaid=inv.dataareaid "
				+ "  WHERE   inv.dataareaid='liv' AND mod.dataareaid='liv' AND moduletype=2 "
				+ "  AND len(inv.itemname)>0   and price !=0 ";

		
		String cmd2 = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock FROM INVENTSUM summ "
				+ "INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid  "
				+ "INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and inv.dataareaid=summ.dataareaid "
				+ "WHERE summ.DATAAREAID='liv'and dim.DATAAREAID='liv' and inv.DATAAREAID='liv' AND CLOSED = 0 AND  moduletype=0 "
				+ "AND  rtrim(dim.inventlocationid)  IN (SELECT inventlocationid FROM INVENTDIM group by inventlocationid ) GROUP BY summ.itemid "
				+ "having SUM(AVAILPHYSICAL) !=0 ORDER BY summ.itemid ";

		if (isDebug())
			System.out.println(cmd);
		if (isDebug())
			System.out.println(cmd2);
		 
		 TreeMap products = new TreeMap();
		 prodHash = new HashMap();
		 HashSet goes = new HashSet();
		
		 try {
            
			Connection con = this.getNewConnectionW();
			Statement stmt = con.createStatement();
			ResultSet rs2 = stmt.executeQuery(cmdo);
			while (rs2.next()) {
				String id = rs2.getString("toode").trim();
				if(!goes.contains(id)) {
				goes.add(id);
				prodsH.add(id);
				}
			 }
			rs2.close();
			stmt.close();
			con.close();
			
			con = this.getNewConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			
			while (rs.next()) {
				String kood = rs.getString("itemid").trim();
				
				Product prod = new Product(kood);
				String nimetus = rs.getString("nimetus").trim();

				prod.setInCases(rs.getDouble("kastis"));
				prod.setPrice(rs.getDouble("price"));

				String ean = rs.getString("ean");
				if (ean != null && ean.trim().length() > 0) {
					prod.setEAN(ean);
					if(eanprods == null) {
	                   	eanprods =new TreeMap();
	                   }
	                   eanprods.put(ean.trim(), prod.getID());
	                   
				}

				String grupp = rs.getString("grupp");
				if (grupp != null && grupp.trim().length() > 0) {
					grupp = grupp.trim();
					if(g1g2.get(grupp) != null) {
						prod.setGroup(g1g2.get(grupp)+"");
						prod.setGroupSecond(g1g2.get(grupp) + grupp );
					}
				}

				prod.setName(nimetus);


				String unit = rs.getString("unit");
				if (unit != null && unit.trim().length() > 0) {
					prod.setUnit(unit);
				}

				// prod.setInUnit(rs.getDouble("yhikukogus"));
            if(goes.contains(prod.getID())) {
				products.put(nimetus, prod);
				prodHash.put(prod.getID(), prod);
            }

			}
			rs.close();
			
			ResultSet rs1 = stmt.executeQuery(cmd2);
			while (rs1.next()) {
				String id = rs1.getString("toode").trim();
				if(goes.contains(id)) {
				Product prod1 = (Product) prodHash.get(id);
				if (prod1 != null) {
					prod1.setStock(rs1.getDouble("stock"));
				}
				}
			}
			rs1.close();
			stmt.close();
			con.close();

		} catch (Exception e) {
			getParent().showError(e);
		}

		return products;
	}

	public Vector getCustomerProductsList(String agentCode) {
		Vector custProds = new Vector();
		HashMap custs = new HashMap();
		int counter = 1;
		String cmd = "SELECT max(itemcustomervalidity) as life ,orderaccount as customer,tra.itemid ,tra.qty,jou.invoicedate AS lastdate , "
				+ "	(case when jou.invoicedate   < getdate()-max(itemcustomervalidity) then 1 else 0 end ) as date "
				+ " FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON tra.invoiceid=jou.invoiceid and tra.dataareaid=jou.dataareaid " +
						"INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid and cus.dataareaid=tra.dataareaid  "
				+ "  WHERE  jou.invoicedate > getdate()-30 AND tra.dataareaid ='liv'  AND jou.dataareaid = 'liv'  AND cus.dataareaid = 'liv' and articleclasstype!=''  "
				+ "  GROUP BY orderaccount,tra.itemid ,jou.invoicedate ,tra.qty order BY orderaccount,tra.itemid ,jou.invoicedate desc  ";

		if (isDebug())
			System.out.println(cmd);

		try {

			Statement stmt = this.getNewConnection().createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			counter++;
			while (rs.next()) {
				double life = rs.getDouble("life");

				String kliendikood = rs.getString("customer").trim();

				String tootekood = rs.getString("itemid").trim();
				if (!custs.keySet().contains(kliendikood + tootekood)) {

					double kogus = rs.getDouble("qty");
					String lastdate = rs.getString("lastdate").substring(0, 10);
					lastdate = lastdate.substring(0, 10);
					custs.put(kliendikood + tootekood, lastdate);
					lastdate = lastdate.substring(5, 10);

					CustProd cp = new CustProd(kliendikood, tootekood, kogus,
							0, 0, lastdate, 0, 0, 0, " ");
					if (life > 0) {
						cp.setRedline(rs.getString("date"));
					}
					custProds.add(cp);
				}

			}

		} catch (Exception e) {
			getParent().showError(e);
		}
		return custProds;
	}

	public double getSalary(String agentCode) {
		setSales(0);
		return 0;
	}

	public void setBulkSales(Map agents) {
		this.connectionUp();
		String cmd = "  SELECT  cus.salesmanager as agent,round(SUM(lineamount),0) AS summa "
				+ " FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid and sal.dataareaid=inv.dataareaid  " +
						"INNER JOIN custinvoicejour jou ON "
				+ " sal.invoiceid=jou.invoiceid and jou.dataareaid=sal.dataareaid " +
						" INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount and cus.dataareaid=jou.dataareaid  "
				+ "  WHERE  inv.dataareaid='liv' AND sal.dataareaid='liv' AND cus.dataareaid='liv'AND YEAR(sal.invoicedate) = YEAR(getdate()) "
				+ " AND MONTH(sal.invoicedate) = MONTH(getdate()) "
				+ " GROUP BY cus.salesmanager";

		if (isDebug())
			System.out.println(cmd);
		try {
			Statement stmt = this.getNewConnection().createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String agentID = rs.getString("agent").trim();
				Agent agent = (Agent) agents.get(agentID);
				if (agent != null) {
					agent.setMonthSales((int) rs.getDouble("summa"));
					// System.out.println(agent.getMonthSales());
				}
			}
			rs.close();
			con.close();
		} catch (Exception e) {
			getParent().showError(e);
		}
	}

	public double getFoodSales(String agentCode) throws SQLException {
		return 0;
	}

	public double getAlcoSales(String agentCode) throws SQLException {
		return 0;
	}

	public ManagerData getManagerData() throws SQLException {
		String sql = "SELECT clid AS klient,enddate AS kuupaev ,docsum AS summa,linecount AS ridu,text AS tekst "
				+ "FROM dayreport WHERE enddate>(getdate()-30) ORDER BY clid,enddate desc";
		this.connectionUp();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);

		String sql2 = "  SELECT accountnum AS kood, 0 AS aastamyyk,0 AS kuumyyk,e.name AS agendinimi,' ' AS kontaktisik "
				+ ",k.phone AS telefon, (case when left(paymtermid,1)='N' then right(paymtermid,len(paymtermid)-1)else paymtermid end )"
				+ " AS maksetin,0 AS ale "
				+ "FROM custtable k INNER JOIN empltable e ON emplid=talsalesman "
				+ "WHERE e.dataareaid='liv' AND k.dataareaid='liv' ORDER BY accountnum";

		// siin tuleks leida kuu ja aasta myyk veel eraldi ja COTACT PERSON
		// tabeli asi ka t��le panna

		Connection con2 = getNewConnection();
		Statement st2 = con2.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);
		ResultSet rs2 = st2.executeQuery(sql2);

		StringBuffer sb = new StringBuffer();
		String sep = ";";
		while (rs.next()) {
			String cliID = rs.getString("klient").trim();
			sb.append(cliID + sep);
			sb.append(rs.getString("kuupaev").trim().substring(0, 10) + sep);
			sb.append(rs.getDouble("summa") + sep);
			sb.append(rs.getInt("ridu") + sep);
			String tekst = rs.getString("tekst");
			tekst = tekst.replace('\'', ' ').replace('\"', ' ');
			if (tekst == null)
				tekst = " ";
			else
				tekst = tekst.trim();
			sb.append(ensureLen(tekst, " ") + sep);

			boolean has = scrollTo(cliID, rs2);
			if (has) {
				sb.append(ensureLen(rs2.getString("aastamyyk"), "0") + sep);
				sb.append(ensureLen(rs2.getString("kuumyyk"), "0") + sep);
				sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
				sb.append(ensureLen(rs2.getString("kontaktisik"), " ") + sep);
				sb.append(ensureLen(rs2.getString("telefon"), " ") + sep);
				sb.append(ensureLen(
						rs2.getString("maksetin").replaceAll("SR", "1"), " ")
						+ sep);
				sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
			}

			sb.append("\n");

		}
		con.close();
		con2.close();
		ManagerData md = new ManagerData();
		md.setClientVisitInfo(sb.toString());

		sql = " SELECT k.accountnum AS klient,max(d.talsalesman) AS agent,max(e.name) AS agentname "
				+ " FROM custtable k INNER JOIN empltable e ON emplid=k.talsalesman INNER JOIN custinvoicejour d ON k.accountnum=d.orderaccount "
				+ " and k.talsalesman=d.talsalesman "
				+ " WHERE e.dataareaid='liv' AND k.dataareaid='liv' AND d.dataareaid = 'liv' AND blocked = 0 AND d.invoicedate > getdate()-30 "
				+ " GROUP BY k.accountnum  ORDER BY accountnum ";
		Connection con3 = this.getNewConnection();
		Statement ps = con3.createStatement();
		rs = ps.executeQuery(sql);
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
		con.close();
		return md;
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

	public String getStartingTime() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH,
				-Integer.parseInt(getParent().getProps().getProperty("months")));
		int month = cal.get(Calendar.MONTH) + 1;
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		String sqlDate = cal.get(Calendar.YEAR) + "-" + month + "-" + day;
		return sqlDate;
	}

	public String getDriverVersion() {
		return "ValioAxa";
	}

	public String getSubjectFilesTo() {
		return this.subjectFilesTo;
	}

	public String getPilotDayData(Agent agent) {

		String sql = "SELECT talinventstatistics1num as grupp, round(SUM(talnetweight),0) AS kogus,round(SUM(lineamount),0) AS summa "
				+ " FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid INNER JOIN custinvoicejour jou ON "
				+ " sal.invoiceid=jou.invoiceid  INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount  WHERE len(talinventstatistics1num) >0 AND "
				+ " inv.dataareaid='liv' AND sal.dataareaid='liv' AND cus.dataareaid='liv' AND cus.talsalesman='"
				+ agent.getID()
				+ "' AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate())"
				+ " GROUP BY talinventstatistics1num  ORDER BY talinventstatistics1num ";
		StringBuffer sb = new StringBuffer();

		System.out.println(sql);
		try {

			Statement stmt = this.getNewConnection().createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			sb.append("GrupiNimi     kilodes ja EUR\n");
			sb.append("---------------------------------------------------\n");

			double totalKokku = 0d;
			double totalSumma = 0d;

			while (rs.next()) {
				sb.append(rs.getString("grupp").trim() + "\n");
				int kogus = rs.getInt("kogus");
				int summa = rs.getInt("summa");
				totalSumma = round(totalSumma + summa, 2);
				totalKokku = round(totalKokku + kogus, 2);
				sb.append("\t" + kogus + "\t" + summa + " EUR\n\n");
			}

			sb.append("---------------------------------------------------\n");
			sb.append("\tKuu m��k kokku: " + totalKokku + " Kg." + totalSumma
					+ " EUR");

		} catch (Exception e) {
			getParent().showError(e);
		}

		return sb.toString();
	}

	public NumberFormat getNF() {
		return NF;
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

	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
	    
	     }

	 
	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	   
	     
	     }

	/**
	 * 
	 * @param aNF
	 *            - the new value for NF
	 */
	public void setNF(NumberFormat aNF) {
		NF = aNF;
	}

	private String url = "jdbc:odbc:axa";
	private String urlw = "jdbc:odbc:axaq";
	private String url2 = "jdbc:odbc:report";
	
    private HashMap ketid = null;
    private HashMap sortimendid = null;
	private Connection con;
    private  HashMap prodHash = null;
	private Vector debt = new Vector();
	private Statement stmt;
	private String subjectFilesTo = null;
	private NumberFormat NF = NumberFormat.getInstance();

  private HashSet prodsH = null;
	@Override
	public void PictureMaker() {
	//	this.getProducts(null);
		 
		   
		       	
		       	String picts = parent.getProps().getProperty("pict.dir");
		        String pictspda = parent.getProps().getProperty("pictpda.dir");
		     	  

		       	try {
		     	
		            StringBuffer buf = new StringBuffer();
		            
		            File d  = new File(picts);
		            for(String fname : d.list() ) {
		            
		            	 File in = new File ( picts + fname); 
		            	 String ean = fname.substring(0,fname.indexOf("."));
		            	 String ext = fname.substring(fname.indexOf(".") + 1);
		            	 
		            		if(in.isFile() ) {//&& prodsH.contains(ean)    
		                     	 
		                      	File inpda = new File ( pictspda + ean + "." + ext); 
		                      	
		                      		Converter.shrinkAndCopyFile(in,inpda);
		                     
		                      	
		                        String len = "0";
		                        if(inpda.exists()){
		                          len =	inpda.length() + "";
		                          } 
		                        
		                          buf.append(ean + "." + ext + ";"  + len + "\n");
		                        }
		                 }
		          	
		        
		       	    //oleasoleva kataloogi kustutamine
		       	    
		                   File f = new File("list.txt");
		                   if (f.exists())f.delete();
		                   
		                   try {
		               		PrintWriter os = new PrintWriter(new FileWriter(
		               				  "list.txt", true));
		               		os.write(buf.toString());
		               		os.close();
		               	   File fff =  new File (pictspda +  "list.txt");
		         	      Converter.copyFile(f,fff);  
		               	
		               	  }catch (Exception ex) {
		               		ex.printStackTrace();
		               	  }
		           	  
		       	} catch (Exception e) {
		       	    getParent().showError(e);
		       	}
		    
	    System.exit(0);
	}
	
	
}
