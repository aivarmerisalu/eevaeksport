package eevaeksport.driver;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

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
import imageUtil.ImageLoader;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class DunkerLATDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter,PictureMaker,FixLoader {
  public DunkerLATDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
   
    
  }

  String user =  null;
  String pass = null;
  String rcompany = null;

  
  private TreeMap eanprods = null;
	
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
				Converter.sendContractJson(contracts ,  rcompany,  user ,  pass);
			}
			
			
			System.exit(0);
		}
	

	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	     String sql = "INSERT into supervisor values ("
	       + "?, "   //   agent	C(50),
	       + "?,"   //   agentname	C(200),
	       + "?,"   //   clientid	C(50),
	       + "?,"   //   clientname	C(200),
	       + "?,"   //	prodid	C(50),
	       + "?,"   //   prodname	C(200),
	       + "getdate() ,"   //	sentdate	D,
	       + " '' ,"   //	receivedTime	D,
	       + "?,"   //normalpr	N,
	       + "?,"   // amoun	N,
	       + "?,"   // grouptotal	N,
	       + "?,"   // cattotal	N,
	       + "?,"   // pertotcat	I,
	       + "?,"   // percolcat	I,
	       + "?,"   // perpalcat	I,
	       + "?,"   // pershecat	I,
	       + "?,"   // perrescat	I,
	       + "?,"   // isaction	L,
	       + "?,"   // ispartner	L,
	       + "?,"   // isbrand	L,
	       + "?,"   // isgroup	L
	       + "?,"   // actionaeva	N,
	       + "?,"   // actionbeva N,
	       + "?,"   // actiontota	N,
	       + "?,"   // custeval	N,
	       + "?,"   // custcomme	C(200),
	       + "?,"   // comment	C(200),
	       + "?,"   // custagent	C(200),
	       + "?,"   // custagentname	C(200),
	       + "?,"   // mustbe	I,
	       + "?,"   // ideal	I,
	       + "?,"   // resstr1	C(200),
	       + "?,"   // resstr2	C(200),
	       + "?,"   // resstr3	C(200),
	       + "?,"   // resint1	I,
	       + "?,"   // resint2	I,
		      + "?)" ; //	resint3	L

	       try {
	       	System.out.println("jam");
	        Connection con = getNewConnection(url2);
	           PreparedStatement ps = con.prepareStatement(sql);

	           for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
	        	   eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
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
	             ps.setDouble(20, lw.getLine().getAction1Eval()*100);
	             ps.setDouble(21, lw.getLine().getAction2Eval()*100);
	             ps.setDouble(22, lw.getLine().getActionTotalEval()*100);
	             ps.setDouble(23, lw.getLine().getCustEval()*100);
	             String cc = lw.getLine().getCustComment();
	             if (cc == null){
	             	cc = " ";
	             }
	             ps.setString(24, cc);

	             String cd = lw.getLine().getComment();
	             if (cd == null){
	             	cd = " ";
	             }
	             ps.setString(25, cd);

	             String ce = lw.getLine().getCustAgentId();
	             if (ce == null){
	             	ce = " ";
	             }

	             ps.setString(26, ce);

	             String cf = lw.getLine().getCustAgentName();
	             if (cf == null){
	             	cf = " ";
	             }
	             ps.setString(27, cf);
	             ps.setInt(28, lw.getLine().getDealNr());
	             ps.setInt(29, lw.getLine().getIdealNr());

	             String cr1 = lw.getLine().getResStr1();
	             if (cr1 == null){
	             	cr1 = " ";
	             }
	             ps.setString(30, cr1);


	             String cr2 = lw.getLine().getResStr2();
	             if (cr2 == null){
	             	cr2 = " ";
	             }
	             ps.setString(31, cr2);


	             String cr3 = lw.getLine().getResStr3();
	             if (cr3 == null){
	             	cr3 = " ";
	             }
	             ps.setString(32, cr3);
	             ps.setInt(33, lw.getLine().getResInt1());
	             ps.setInt(34, lw.getLine().getResInt2());
	             ps.setInt(35, 111); //111 on eesti andmed
	             ps.executeUpdate();
	           }

	           ps.close();
	           con.close();
	       } catch (SQLException e) {
	           e.printStackTrace();
	       }
	     }

  /**
   *
   */
  public void connectionUp(){
  }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

  public String getSaveDebtInfo() {
   	return null;
   }

  private String getDir() {
		String dir = getParent().getProps().getProperty("readin");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}

	public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		this.getProducts(null);
		File f = new File(this.getDir() + "/xls/sortiments.xls");
		
		
		try {
			
			/*
			  String sql = "SELECT sortdate from mj_assortment where  " + tananepaev;
        	  boolean insertassort = true;
        	  Connection con = DriverManager.getConnection(url2,"aivarm", "mobilejava");
  	        Statement stmt = con.createStatement();
  	        
  	        ResultSet rs = stmt.executeQuery(sql);
  	        if(rs.next()){
  	        	insertassort = false;
  	        	//kontrollime kas on vaja insertida 
  	        }
  	        System.out.println("Sisestame sortimenti " + insertassort );
			*/
			
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("Products");
			HSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(3);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 3; a < row.getLastCellNum(); a++) {
				try {
				//	 System.out.println(row.getCell((short)a).getStringCellValue());
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			// System.out.println(groups.toString());

			for (int i = 5; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";

				if (row.getCell((short) 0) != null) {

					try {
						prodID = (int) (row.getCell((short) 1)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID = (row.getCell((short) 1).getStringCellValue());
					}
					
                       if(!prodID.equals("0")){
                    	
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

				//		 System.out.println( a + " " + prodID + " " +
					//			 clGroup + " " + go);

								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(clGroup.trim());
								}

								if (prodID != null
										&& prodID.trim().length() > 0 && ids.get(prodID.trim()) != null) {
									mline.setProdID("" + ids.get(prodID.trim()));
								}
								if (mline.getProdID() == null
										&& prodGroup != null
										&& prodGroup.trim().length() > 0) {
									mline.setProdGroup(prodGroup);
								}
								 /*if(insertassort){
		                               insertSortLine(mline);   
		                           }*/
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

		 System.out.println("fixed Suurus ");

		return result;

	}


  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
          String sql = " SELECT \"PriceGroupCode\" grupp , \"ItemCode\" as toode, \"PriceType\" as tyyp, \"Price\", " + 
      " \"StartDate\" as algus, \"EndDate\" as lopp  FROM \"PriceGroupItems\" " +
      " where \"StartDate\" <= current_date+1 and \"EndDate\" >= current_date+1 ";

          try {

             Connection cc = getNewConnection();
        	Statement stmt = cc.createStatement();
                  ResultSet rs = stmt.executeQuery(sql);

                  while (rs.next()) {
                	  String clGroup = rs.getString("grupp");
                	  String prodID = rs.getString("toode");
                	  String tyyp = rs.getString("tyyp");
                          double price = rs.getDouble("price");
                          String algus = rs.getString("algus");
                          String lopp = rs.getString("lopp");
                          MatrixLine mline = new MatrixLine();
                    
                          if (clGroup != null && clGroup.trim().length() > 0) {
                                  mline.setCliGroup(clGroup.trim());
                          }
                       
                          if (prodID != null && prodID.trim().length() > 0) {
                                  mline.setProdID(prodID.trim());
                          }

                          if (price != 0) {
                                  mline.setPrice(price);
                          }
                          
                          if(tyyp.equals("Promo")){
                        	  if (algus != null && algus.trim().length() > 0) {
                                  mline.setStartDate(algus.trim().substring(0,10));
                          }
                          
                          if (lopp != null && lopp.trim().length() > 0) {
                                  mline.setEndDate(lopp.trim().substring(0,10));
                          } 
                          }
                          lines.add(mline);
                          
                          if("LATS".equals(clGroup) && "ITA01973".equals(prodID)) {
                        	  System.out.println("olemas " + mline.getStringForFixedAssortment());
                          }
                     }
                  rs.close();
                  stmt.close();
                   cc.close();
          } catch (SQLException ex) {
          ex.printStackTrace();
          }
          return lines;
  }

  public String getProductSortingData(){
   	return null;
   }

  public Vector getDebt(String agentCode) throws SQLException{
     return this.debt;
   }


  public String getAssortmentStr(Agent agent){
  	return null;
  }
  

 public String getSaveStockInfo(){
	  

	  
String  cmd = " SELECT \"ItemCode\" as toode ,  \"AvailToOrderQty\" s stock FROM \"ItemQtys\" where \"AvailToOrderQty\" > 0  ";


	  

       if (isDebug()) System.out.println(cmd);
       StringBuffer products = new StringBuffer();
       try {

         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery(cmd);

         while (rs.next()) {

           String kood = rs.getString("toode").trim();
           double seis = rs.getDouble("stock");
           if(seis > 0){
               products.append(kood +  ";");
               products.append(" ;");
               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
         
           }
           }
           rs.close();
       } catch (Exception e) {
           getParent().showError(e);
       }
       return products.toString();
   }



              public String getDealerCustomerCSV(String agent){
               return null;
               }

                                                  public String  getCustProdDiscounts(Agent agent){
                                                   return null;
                                                   }


  /**
   *
   */
  public void connectionDown(){
  }

  
  public DealerProduct getDealerProductsList() {

     DealerProduct dealerProds = new DealerProduct();
            return dealerProds;
     }

  
  private String ensureLen(String str, String replace){
	     if (str == null) str = "";
	     else str = str.trim().replace('\'', ' ').replace('\"', ' ').replace(';',' ');
	     if (str.length() == 0){
	       return replace;
	     } else {
	       return str;
	     }
	   }
	   
  
  public TreeMap getAgents(){
    String cmd = "SELECT \"Code\" as  kood, \"Surname\" || ' ' || \"Name\"  as nimi FROM \"Agents\"";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    
	 StringBuffer sb2 = new StringBuffer();
    try {
    
    	Properties props = new Properties();
		try {
			props = EksportFrame.loadProps("asendajad.txt");

		} catch (Exception e) {
			e.printStackTrace();
		}
	Connection 	cc = this.getNewConnection();
	Statement st = cc.createStatement();
    	ResultSet rs = st.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("nimi");
          if(name != null){
        	  agent.setName(name);
          }else{
        	  agent.setName(agent.getID());
        	  name = agent.getID();
          }
          
          
          agent.setIsPilot(true);
          agent.setHasEmail(parent.parseFirstEmail(parent.getEmail(agent.getID())).length() > 0);
          
          agent.setLanguage("LAT");
          agent.setCountry("lv");
          agent.setPassWord("0000");
          String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
          if(email.length() > 3){
        	  agent.setEmail(email);
          }
         
          
          agent.setActive(true);
          
          String agentAGENTS = props.getProperty("dub." +agent.getID());
		if(agentAGENTS != null){
           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
	    while(stag.hasMoreTokens()){  	
         agent.addDubbler(stag.nextToken());
         System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
	    }

		}
		 agents1.put(agent.getID(), agent);
		  agent1.put(agent.getID(), agent);
          agents.put(name, agent);
          sb2.append(agent.getID() + ";" + name + "\n");  
        }
        rs.close();
        st.close();
        cc.close();
    } catch (Exception e){
        getParent().showError(e);
      }
    Agent aa = new Agent ("HOUSE1");
    aa.setName("HOUSE1");
    agents.put("HOUSE1", aa);
 
    
    if(sb2.length() > 2){
    	try{
    		 Converter.saveFileInUTF8( "agent.txt", Converter.enCodeEst(sb2.toString()));
    	}catch (Exception e){
    		
    	}
      
     }
    return agents;
  }
  private TreeMap agent1 = new TreeMap();
  
  private HashMap sortimendid = null;
  public TreeMap getCustomers(Agent agent){
      debt = new Vector();
	  sortimendid = new HashMap();
      
 

  	String cmd = "SELECT sh.\"CustomerCode\" as payer , sh.\"ShipToCode\" as koht, sh.\"Description\" as name , sh.\"Address\" as addr, sh.\"City\" as linn, " +
  	" sh.\"Region\", sh.\"RouteCode\" as visit , sh.\"Contact\", sh.\"Phone\", sh.\"MinOrderAmount\" as minsum,  " +
  	"  sh.\"PriceGroupCode\" as grupp, sh.\"AssortmentGroupCode\" as assortment, sh.\"DiscountPct\" as discount, sh.\"AgentCode\" as agent, sh.\"AgentCodeAdditional\" as agent2," +
  	"  sh.\"Blocked\" as blokk, sh.\"Deleted\", sh.\"TimeStamp\", sh.\"ExternalSTACode\", sh.\"IgnoreItemMinOrderQuantity\", " +
  	"  sh.\"EDIsoftSTACode\", sh.\"License\", sh.\"LicenseDate\",  " +
  	"  cus.\"CreditLimit\" as limit,\"Balance\" as used , cus.\"PaymentTerms\" as paym " +
       " FROM \"ShipTos\" sh LEFT JOIN \"Customers\" cus on cus.\"Code\"=sh.\"CustomerCode\" " ;
 
 
 String sql = "  select rtrim(a.[Sell-to Customer No_])+ '-' +[Ship-To Address Code] AS klient, a.[Customer No_] AS maksja,a.[External Document No_] AS number, a.[Due Date] AS maksepaev, sum(a.[Remaining Amt_ (LCY)]) AS summa, " + 
                           "     a.[Posting Date] as kuupaev , DATEDIFF(dd,a.[Due Date],GETDATE()) AS yle ,a.[Customer No_] AS payer FROM [Cust__Ledger_Entry] a  " +
                            "      where  " +
           "  (a.[Document Type] =2 or a.[Document Type]= 3)  AND a.[Posting Date] > getdate()-200 AND a.[Open]= 1 " +
                                "   group by rtrim(a.[Sell-to Customer No_])+ '-' +[Ship-To Address Code],a.[Customer No_] ,a.[External Document No_],  " +
                                "  a.[Due Date] ,a.[Posting Date], DATEDIFF(dd,a.[Due Date],GETDATE()) ,a.[Customer No_] having sum(a.[Remaining Amt_ (LCY)])<> 0 order by  a.[Due Date]" ;

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    TreeMap<String,Double> overDue = new TreeMap<String,Double>();
    try {
    	
    	 Converter.getManagersReplacements(agent1, rcompany, user , pass);
    	Connection cc = this.getNewConnection3(url3);
    	Statement stmt = cc.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
    
      while(rs.next()){
        Debt deb = new Debt();
        String klient = rs.getString("klient").trim();
        String maksja = klient.substring(0,klient.lastIndexOf("-"));
        if(klient.endsWith("-")){
        	klient = maksja;
        }
        deb.setCustID(klient);
        String number = rs.getString("number").trim();
        deb.setDoknr(number);
        deb.setDuedate(rs.getDate("maksepaev"));
        deb.setSum(rs.getDouble("summa"));
        deb.setDate(rs.getDate("kuupaev"));
        int yle = rs.getInt("yle");
        deb.setDue(yle + "");
        if(yle > 0){
        	if(overDue.get(maksja) != null){
        		overDue.put(maksja, overDue.get(maksja)+ deb.getSum());
        	}else{
        		overDue.put(maksja,  deb.getSum());
        	}
        }
        this.debt.addElement(deb);
      }
      rs.close();
      stmt.close();
      
      cc = this.getNewConnection();
      stmt = cc.createStatement();
      rs = stmt.executeQuery(cmd);
    
      while(rs.next()){
        
    	  String payer = rs.getString("payer").trim();
    	  String id = rs.getString("koht").trim();
    	  String accountnum = payer + "-" + id;
          
    	  String name = Converter.enCodeEst(rs.getString("name").trim());
        
        Customer cust = new Customer(accountnum);
        if(overDue.get(payer) != null){
        	cust.setDebt(overDue.get(payer));
        }
        
        
       
        if(payer != null && payer.trim().length() > 0){
        	cust.setPayer(payer.trim());
        }else{
        	cust.setPayer(accountnum);
        }
 
        String aadr = rs.getString("addr") ;
        String city = rs.getString("linn");
        
        if (aadr != null) {
        	  if(city != null){
              	aadr = aadr.trim() + "," + city.trim(); 
              }	
          cust.setAddress(Converter.enCodeEst(Converter.adressLink(aadr.trim(),"Latvia")));
        }
        
        name = name + " " + Converter.enCodeEst(aadr) + " " + accountnum;
        cust.setName(name);
     
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
        
        String info = rs.getString("Contact");
        String phone = rs.getString("Phone");
        if (info != null && info.trim().length()>0) {
        	if(phone != null){
        		info = info.trim() + " " + phone.trim();
        	}
          cust.setPersonalData(info.trim());
        }
        
        double minsum = rs.getDouble("minsum");
        if(minsum > 0){
      	//  cust.setMinsum(minsum);
        }
     
        String grupp = rs.getString("grupp");
        if (grupp != null && grupp.trim().length()>0) {
          cust.setGroup(grupp.trim());
        }
   
        double discount = rs.getDouble("discount");
          if(discount > 0){
        	  cust.setDiscount(discount);
          }
       
        String agen = rs.getString("agent");
        if (agen != null) {
          cust.addAgent(new Agent(agen.trim()));   
          cust.setMainAgent(agen.trim());
          if(agents1.get(agen.trim()) != null){
        	  Agent aa = (Agent)agents1.get(agen.trim());
        	  if(aa.isHasE()){
        		  cust.addAgent(new Agent("HOUSE1")); 
        	  }
        	   
          }
          
          if ( agen != null && agent1.get( agen.trim()) != null){
              Agent aa = (Agent) agent1.get( agen.trim());
               Vector vec = aa.getDubbler();
               if( vec!= null && vec.size() > 0 ){
                    Enumeration en = vec.elements();
                    while(en.hasMoreElements()){
                         String ags = (String) en.nextElement();
                      cust.addAgent( new Agent(ags));
                   }
              }
        }
            
          
          if (agen != null && agen.trim().length()>0){
				Agent agg = ((Agent)this.getAgents1().get(agen.trim()));
				
				if(agg != null && agg.getDubbler().size()>0){
					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
						cust.addAgent(new Agent((String)agg.getDubbler().elementAt(i)));
					}
				}
			}        
        }
     
        String agen2 = rs.getString("agent2");
        if (agen2 != null) {
        	 cust.addAgent(new Agent(agen2.trim()));   
        }
       boolean goes = true;
        int block = rs.getInt("blokk");
        if (block == 1 || block == 2) {
          cust.setSalesBlock(true);
          goes = false;
        }
        
        
      /*  String debtStr = rs.getString("debt");
        if (debtStr != null){
          cust.setDebt((int)Double.parseDouble(debtStr));
        }*/
        
        
        String limitStr = rs.getString("limit");
        if (limitStr != null){
          cust.setCredit((int)Double.parseDouble(limitStr) + "");
        }
        
        String usedStr = rs.getString("used");
        if (usedStr != null){
          cust.setUsedCredit((int)Double.parseDouble(usedStr));
        }
   
             
        String term = rs.getString("paym");
        if(term != null && term.trim().length() >0){
        	cust.setPaymTerm(term);	
        }
       
         
        
    if(goes) {
    	   customers.put(name, cust);
           custMap.put(cust.getID(),cust);	
       }
                     
                    }
     
             
              rs.close();
              stmt.close();
              cc.close();
    } catch (Exception e){
      getParent().showError(e);
    }
    return customers;
  }



  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

  private Connection getNewConnection(String url) throws SQLException{
	    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	  }
      public String getDistribution() {
        return null;
      }
      private Connection getNewConnection3(String url) throws SQLException{
  	    return DriverManager.getConnection(url3,"droid", "droid");
  	  }

  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "   SELECT \"Code\" as liik , \"Description\" as nimi  FROM \"ItemCategories\" where \"Blocked\" is false  ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik,  Converter.enCodeEst(rs.getString("nimi").trim()));
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}


  public TreeMap getProdGroupsSecond() throws SQLException {

    //Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
  /*  String    sql = " SELECT talROUTETYPEID AS liik, talROUTETYPEID AS nimetus FROM inventtable WHERE dataareaid='tal' AND talROUTETYPEID!='' GROUP BY talROUTETYPEID";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik, rs.getString("nimetus").trim());
    }
    rs.close();
    st.close();
    con.close();
*/
    return tm;
  }
  public TreeMap getAgents1() {
		return agents1;
	}

  private TreeMap agents1 = new TreeMap();
  private TreeMap ids = new TreeMap();

  public TreeMap getProducts(Agent agent){

	  String cmd = "SELECT t.\"Code\" as itemid ,t.\"ExternalCode\" as code, t.\"Description\" as nimetus2, t.\"NameFull\" as nimetus, t.\"CategoryCode\" as grupp,t.\"UOM\" as unit, t.\"UnitsPerPack\" as kastis, " + 
      " t.\"VATPercent\" as vat, t.\"Barcode1\" as ean, t.\"Barcode2\", t.\"ExternalCode\", t.\"AllowedQuantities\" as optimum,  "+
      " t.\"ItemOrder\", t.\"Price\", t.\"Blocked\"    " +
      " , t.\"StandardPalletQty\" as pallet, t.\"CountryID\", t.\"ItemType\" as type, k.\"AvailToOrderQty\" as seis ," +
      " \"BlockedStatus\"  FROM \"Items\" t LEFT JOIN \"ItemQtys\" k ON k.\"ItemCode\" = t.\"Code\" where \"Blocked\" is false and t.\"ExternalCode\" != ''";
	  ids = new TreeMap();
  
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        String code = rs.getString("code");
        if(code != null){
        	code = code.trim();
        	 ids.put(code, kood);
           }else{
        	code = "";        
        }
        
       
        
        Product prod = new Product(kood);
        String nimetus2 = Converter.enCodeEst(rs.getString("nimetus2").trim() + " " + code) ;
        String nimetus =(rs.getString("nimetus")) ;
        if(nimetus != null && nimetus.trim().length()> 1 ) {
        	 prod.setPartii( Converter.enCodeEst(nimetus.trim()));
        }
       ; // tahavad nähe infos pikka nime
        
     String grupp = rs.getString("grupp");
     if(grupp!=null && grupp.trim().length()>0){
      prod.setGroup(grupp.trim());
   }
     String unit = rs.getString("unit");
     if(unit!=null && unit.trim().length()>0){
      prod.setUnit(unit);
   }
   
    
     prod.setInCases(rs.getDouble("kastis"));
     String vat = rs.getString("vat");
     if(vat!=null && vat.trim().length()>0){
      prod.setVat(vat);
   }


     String ean = rs.getString("ean");
     String add = "";
     if(ean != null && ean.trim().length()>0){
     prod.setEAN(ean);
     
     if(eanprods == null) {
  	   eanprods = new TreeMap();
     }
     eanprods.put(ean.trim(), prod.getID());
     
     int ll  = ean.trim().length();
     add = " @" +  ean.trim().substring(ll-4);
    }
     
     String optimum = rs.getString("optimum");
     if(optimum != null && optimum.trim().length()>0){
    	 prod.setOptAmountInUnits(Double.parseDouble(optimum.trim()));
     }
   
        prod.setPrice(rs.getDouble("price"));
        prod.setOnPallet(rs.getDouble("pallet"));
   
        String type = rs.getString("type");
        if(type != null && type.trim().length()>0){
       	 prod.setType(type.trim());
        }
        
      prod.setName(nimetus2 + add);

      
   
  
   /*   String alegr = rs.getString("alegr");
      if(alegr!=null && alegr.trim().length()>0){
       prod.setAlegrupp(alegr);
    }*/
      

       prod.setStock(rs.getDouble("seis"));   

        products.put(nimetus2, prod);
        prodHash.put(prod.getID(), prod);

      }
      rs.close();
      
    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }




  public Vector getCustomerProductsList(String agentCode){
	    Vector custProds = new Vector();
	    //if (true) return custProds ;

	 String cmd =  "SELECT   rtrim(d.[Sell-to Customer No_])+'-'+d.[Ship-to Code] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, r.[Vat Base Amount] as price ,d.[Posting Date] AS kuup,r.[No_] as dok " +
	  " FROM [Sales_Shipment_Line] r " +
	  "INNER JOIN [Sales_Shipment_Header] d ON r.[Document No_]= d.[No_] INNER JOIN [Item] t ON t.[No_]=r.[No_]  WHERE " +
	 " d.[Posting Date]> GETDATE()-120 ORDER BY  d.[Sell-to Customer No_],d.[Ship-to Code], t.[Search_Description],r.[No_],d.[Posting Date]";
	    if (isDebug()) System.out.println(cmd);

	    try {

	    	Statement stmt =  this.getNewConnection3(url3).createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	   
	      while(rs.next()){
	        String kliendikood = rs.getString("klient").trim();
	        String tootekood = rs.getString("toode").trim();
	        double kogus = rs.getDouble("kogus");
	        double hind = rs.getDouble("price");
	        if(kogus != 0){
	        	hind = hind /kogus;
	        }
	        String lastdate = rs.getString("kuup").substring(0,10);
	                 lastdate = lastdate.substring(5,10);
	                 
	     	        String bill = rs.getString("dok");          
	        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,hind,0,0," ");
	        cp.setAbiKood(bill);

	        //cp.setRedline(rs.getString("date"));
	        custProds.add(cp);
	      }
	    } catch (Exception e){
	      getParent().showError(e);
	    }
	    return custProds;
	  }





  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


  public void setBulkSales(Map agents) {

	  	 try {
	  	 	
	  	 	
	  	   String cmd = "select [Salesperson Code] as agent ,sum([Sales (LCY)]) as summa from [Cust__Ledger_Entry]  " +
	        " where Month(getdate())=Month([Posting Date]) and  " +
	              " Year(getdate())=Year([Posting Date])  group by [Salesperson Code]";
	  	  Connection con = getNewConnection3(url3);
		        Statement stmt = con.createStatement();
		        ResultSet rs = stmt.executeQuery(cmd);
		        while(rs.next()){
	            
	                 String agentID = rs.getString("agent");
	                 double sales = rs.getDouble("summa");
	                 Agent agent = (Agent)agents.get(agentID);
	                 if (agent != null){
	                   agent.setMonthSales((int)(sales));
	            }
	          }

		        rs.close();
		        stmt.close();
		        con.close();
	      } catch (Exception e){
	        e.printStackTrace();
	     }
     }

    public double getFoodSales(String agentCode) throws SQLException{
      return 0;
    }

    public double getAlcoSales(String agentCode) throws SQLException{
      return 0;
    }

    public ManagerData getManagerData() throws SQLException{
      
      return null;
    }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
    }


  public String getStartingTime(){
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, - Integer.parseInt(getParent().getProps().getProperty("months")));
    int month = cal.get(Calendar.MONTH) + 1;
    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    String sqlDate = cal.get(Calendar.YEAR) + "-" + month + "-" + day;
    return sqlDate;
  }

  public String getDriverVersion(){
    return "DunkerLAT";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent){



	       String sql2 =  "SELECT sal.orderid AS arve, sal.orderdate AS kuupaev,cus.name AS klient, count(rid.itemid) AS summa  " +
	    " FROM perinttelematable sal INNER JOIN custtable cus ON sal.CUSTACCOUNT=cus.accountnum  " +
	    " INNER JOIN perinttelemaline rid ON sal.orderid=rid.orderid  WHERE sal.dataareaid='dun' AND cus.dataareaid='dun'  AND rid.dataareaid='dun' AND cus.salesrep ='" + agent.getID() + "' AND " +
	    " sal.ORDERDATE > getdate()-0.8  and referenceno = '' GROUP BY sal.orderid, sal.orderdate , cus.name ORDER BY sal.orderdate";

	    StringBuffer sb = new StringBuffer();
	    sb.append("\n\n\n Vormistatud tellimused:\n\n");

	      try {
	                  ResultSet	      rs2 = stmt.executeQuery(sql2);
	                  while (rs2.next()) {
	                  sb.append("(" + rs2.getString("arve").trim() + ") [" + rs2.getString("kuupaev").substring(0,10) + "] - " + rs2.getString("klient") + "  Ridu: " + rs2.getDouble("summa") + "\n");
	                  }

	        //stmt = con.prepareStatement(sql);
	        //stmt.sets(1, agent.getObject());
	          //stmt.setString(2, agent.getObject());
	                  /* 	    String sql =" SELECT  (SELECT SUM(PHYSICALINVENT)FROM INVENTSUM summ INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid WHERE summ.DATAAREAID='bal' and dim.DATAAREAID='bal'" +
	"AND CLOSED = 0 AND dim.inventlocationid ='" + agent.getObject() + "' " +
	"AND summ.itemid=inv.itemid GROUP BY summ.itemid) AS ladu,SUM(sal.qty) AS kogus,avg(sal.lineamountmst/sal.qty) AS myygihind, " +
	"inv.itemname AS nimetus, inv.itemqtyinabox AS tykke " +
	"FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid  WHERE " +
	"inv.dataareaid='bal' AND sal.dimension='" + agent.getID() + "' AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate()) AND DAY(sal.invoicedate) = DAY(getdate()) " +
	"GROUP BY inv.itemname ,inv.itemqtyinabox,inv.itemid ORDER BY inv.itemname ";
   ResultSet	      rs = stmt.executeQuery(sql);
	          ResultSetMetaData md = rs.getMetaData();
	          sb.append("Nimi  \t\tLadu\t\tM��dud\t\tSumma\n");
	          sb.append("---------------------------------------------------\n");

	          double totalKokku = 0d;

	          while (rs.next()) {
	            int ladu = rs.getInt("ladu");
	            int kogus = rs.getInt("kogus");
	            double hind = round(rs.getDouble("myygihind"), 2);
	            double kokku = round(kogus*hind, 2);
	            totalKokku = round(totalKokku + kokku, 2);

	            sb.append(rs.getString("nimetus").trim() + " " + rs.getInt("tykke")+ "\n");

	            sb.append("\t\t\t\t" + ladu + "\t\t" + kogus + "\t\t" +
	              kokku + "\n\n");
	           }

	          sb.append("---------------------------------------------------\n");
	          sb.append("\tP�eva m��k kokku: " + totalKokku + " EUR");

*/
	                  } catch (Exception e) {
	                      getParent().showError(e);
	                  }


	      return sb.toString();
	  }





       public NumberFormat getNF(){
         return NF;
       }






       /**
        *
        * @param aNF - the new value for NF
        */
       public void setNF(NumberFormat aNF){
         NF = aNF;
    }
  private String url = "jdbc:odbc:droid_db_food";
  private String url2 = "jdbc:odbc:report";
  private String url3 = "jdbc:odbc:droid";
  private Connection con;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
    
    
    public void shrinkAndCopyFile (String in, String out) throws Exception {

        try {
        	BufferedImage inb = ImageIO.read(new File(in));
        	BufferedImage newImage = new BufferedImage(inb.getWidth(), inb.getHeight(), BufferedImage.TYPE_INT_RGB);
        	Graphics2D g = newImage.createGraphics();
        	g.drawImage(inb, 0, 0, null);
        	g.dispose();
        	 File outputfile = new File(out);
        	 ImageIO.write(newImage, "jpg", outputfile);
        	
          ImageLoader.fromFile(out).getResizedToWidth(300).writeToJPG(new File(out), 0.7f);//.soften(0.95f)
        }
        catch (Exception ioe) {
        	ioe.printStackTrace();
        }
          }

    
    
    private void makeNormpict(String pictspda,String pictssou ){
    	  File res2 = new File (pictspda);
    	  
    	  Vector cats = new Vector();
    	  File[] dd = res2.listFiles();
          for(int i = 0; i < dd.length ; i++){
          	File t = dd[i];
          	if(t.isDirectory()){
          		cats.add(t.getName());
          	}
          }
    	  
    	  
          for(int u = 0; u < cats.size(); u++){
        	  String cat = cats.elementAt(u)+"";
        	  File res3 = new File (pictspda + "/" +cat);
          
          
    	  
          String[] ff2 = res3.list();
      //    System.out.println("ff2 suur " + ff2.length);
      	 for(int i = 0; ff2.length > i ; i++){
      		if(ff2[i].indexOf("_") != -1 ){
      	  		try {
      	  		//	System.out.println("min " + pictspda  + "/" +cat +"/"+ ff2[i] + " | " + ff2[i].substring(0,ff2[i].indexOf("_")) + ".jpg");
      				shrinkAndCopyFile(pictspda +cat +"/" + ff2[i],pictssou + ff2[i].substring(0,ff2[i].indexOf("_")) + ".jpg");
      			} catch (Exception e) {
      				e.printStackTrace();
      			}	
      		}
      		
      	 }
          }
      }
    private void makePictureCat(){
  	    
    	
    	String pictssou = parent.getProps().getProperty("pict.dir");
  	    String pictspda = parent.getProps().getProperty("pictpda.dir");
  	    String picts  = parent.getProps().getProperty("pictpda.source");
  	 makeNormpict(pictssou,picts);
       
  	 try {
    	TreeMap prods = this.getProducts(null);

         StringBuffer buf = new StringBuffer();
         StringBuffer buf0 = new StringBuffer();
         int c = 0;
         Iterator enume = prods.keySet().iterator();
    //     System.out.println("Piltide leidmine ");
    	    while (enume.hasNext()) {
    	    	String asi = (String)enume.next();
    	    	Product prod = (Product)prods.get(asi);
    	    	
    	    	c++;
    	    	if(c == 100){
    	    		  c =0;
    	    //		  System.out.println("Piltide leidmine "  + a);	
    	    	}
    	     
                  String toode = prod.getID().trim();  
                  toode = toode.replace('/', '_');
                  File f = new File(picts + toode+".jpg");
                  String len = "0";
                  if(f.exists()){
                    len =	f.length() + "";
                  }
                  if(len.equals("0")){
                  	//System.out.println(toode+".jpg;"  + len);
                  	buf0.append(toode+".jpg\n");
                  }else{
                  	buf.append(toode+".jpg;"  + len + "\n");
            	    	
                  }
                  }
    	
    	
    	    //oleasoleva kataloogi kustutamine
    	//  System.out.println("Kataloogi kustutamine "  + a);	    
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
            	//  System.out.println("buf 0 " + a  + " " +  b + " "+ buf0.toString());
            	  File f0 = new File("list0.txt");
            	
            	  	
                     if (f0.exists())f0.delete();
                  
                    try {
              		PrintWriter os1 = new PrintWriter(new FileWriter(
              				  "list0.txt", true));
              		os1.write(buf0.toString());
              		os1.close();
              		//System.out.println( buf.toString());
              	  }catch (Exception ex) {
              		ex.printStackTrace();
              	  }

    //Kataloogi sisude kustutamine
              	  
   
           	 
           	 
         	 File res2 = new File (pictspda);
             String[] ff2 = res2.list();
         	 for(int i = 0;ff2.length>i;i++){
             //	System.out.println("fff " + ff2[i]);
             	(new File ( pictspda + ff2[i])).delete(); 
          
             }
//         	 L�ppes kustutamine 
         	 
       //    System.out.println("Piltide kopeerimine ");	
           //Failide kopeerimine  
             String[] gg = new File(picts).list();
          	  //System.out.println("gg " + gg[0] + " " + gg.length);
             
              for(int i = 0;gg.length>i;i++){
              	
              	File in = new File ( picts + gg[i]); 
              	if(in.isFile()){
              	
              	File inpda = new File ( pictspda + gg[i]); 
             
                    	Converter.copyFile(in,inpda);
              	}
              }
              File fff =  new File (pictspda +  "list.txt");
              Converter.copyFile(f,fff);  	
        	  File fff0 =  new File (pictspda +  "list0.txt");
        	  Converter.copyFile(f0,fff0);  	
    	} catch (Exception e) {
    	    getParent().showError(e);
    	}
    
  }
    
    
	@Override
	public void PictureMaker() {
		makePictureCat();
		  System.exit(0);
		
	}
}
