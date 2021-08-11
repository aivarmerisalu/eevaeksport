package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
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

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class DunkerLATManaDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
  public DunkerLATManaDBDriver(EksportPanel parent){
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
		File f = new File(this.getDir() + "/xls/sortimendid.xls");
		
		
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
			HSSFSheet sheet = workbook.getSheet("Tooted");
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

			// System.out.println(groups.toString());

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

							//	 System.out.println( a + " " + prodID + " " +
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

		// System.exit(0);

		return result;

	}


  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
          String sql = " SELECT \"PriceGroupCode\" grupp , \"ItemCode\" as toode, \"PriceType\" as tyyp, \"Price\", " + 
      " \"StartDate\" as algus, \"EndDate\" as lopp  FROM \"PriceGroupItems\" " +
      " where \"StartDate\" <= current_date-1 and \"EndDate\" >= current_date+1 ";

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
          
  		String agentAGENTS = props.getProperty("dub." +agent.getID());
		if(agentAGENTS != null){
           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
	    while(stag.hasMoreTokens()){  	
         agent.addDubbler(stag.nextToken());
         System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
	    }

		}
		 agents1.put(agent.getID(), agent);
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
    
 makeManagerFile();
    
    if(sb2.length() > 2){
    	try{
    		 Converter.saveFileInUTF8( "agent.txt", Converter.enCodeEst(sb2.toString()));
    	}catch (Exception e){
    		
    	}}
    
    return agents;
  }
  
  
  
  private void makeManagerFile(){
	    String sql = "SELECT clid AS klient , enddate AS kuupaev , docsum AS summa , linecount AS ridu,text AS tekst," +
      " longitude,lattitude FROM dayreport WHERE enddate>(GETDATE()-30) ORDER BY clid,enddate desc ";
	    try{
	    	
	    	Connection c = getNewConnection(url2);
	    	Statement st = c.createStatement();
	    	ResultSet rs = st.executeQuery(sql);

         StringBuffer sb = new StringBuffer();
	    	String sep = ";";
	    	while(rs.next()){
	    	String cliID = rs.getString("klient").trim();
	    	sb.append(cliID + sep);
	    	sb.append(rs.getString("kuupaev").trim().substring(0, 16) + sep);
	    	sb.append(rs.getDouble("summa") + sep);
	    	sb.append(rs.getInt("ridu") + sep);
	    	String tekst = ensureLen(rs.getString("tekst")," ");
	    	tekst = tekst.replace('\'', ' ').replace('\"', ' ').replace(';', ' ');
	    	if (tekst == null) tekst = " ";
	    	sb.append(tekst + sep);
	    	String longi = rs.getString("longitude") ;
	    	String latti = rs.getString("lattitude") ;
	    	if(longi.indexOf(".0000") != -1){
	    		longi = "0";
	    	}
	    	if(latti.indexOf(".0000") != -1){
	    		latti = "0";
	    	}
	    	sb.append(latti + sep);
	    	sb.append(longi + sep);
	    
	    	sb.append("\n");
	    	}
	    	
	      Converter.saveFileInUTF8("managerdata.txt", sb);
	    
	    }catch(Exception e){
        e.printStackTrace();	    	
	    }
}
  
  private Connection getNewConnection3(String url) throws SQLException{
	    return DriverManager.getConnection(url3,"droid", "droid");
	  }
  
  private HashMap sortimendid = null;
  public TreeMap getCustomers(Agent agent){

	  sortimendid = new HashMap();
      
      File fff = new File(this.getDir() + "/xls/sortimendid.xls" );
      TreeMap groups = new TreeMap(); 
  	try{
          FileInputStream ff = new FileInputStream(fff);
  		HSSFWorkbook workbook = new HSSFWorkbook((ff));
  		HSSFSheet sheet = workbook.getSheet("Kauplused");
  		HSSFRow row = null;
  		
  		row = sheet.getRow(0);
  	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
  		
  	//	System.out.println("rownum " + row.getLastCellNum());
  		
  		
  		for(int a = 1 ; a < sheet.getLastRowNum()+1;a++){
  			try{
  			//	Loeme sisse rea
  				row = sheet.getRow(a);
  				if (row.getCell((short) 0) != null) {
  				


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
	    	    			  	 groups.put(kood,sort);
  	    			  
  	    				
  				}	   
  				}
 			}catch (Exception e){
  			//	System.out.println(row.getCell((short)a).getNumericCellValue());
  			}
  		}
  		
  		//System.out.println(groups.toString());
  
  	 }catch (Exception e){
  	//	e.printStackTrace();
  	}

  	String cmd = "SELECT sh.\"CustomerCode\" as payer , sh.\"ShipToCode\" as koht, sh.\"Description\" as name , sh.\"Address\" as addr, sh.\"City\" as linn, " +
  	" sh.\"Region\", sh.\"RouteCode\" as visit , sh.\"Contact\", sh.\"Phone\", sh.\"MinOrderAmount\" as minsum,  " +
  	"  sh.\"PriceGroupCode\" as grupp, sh.\"AssortmentGroupCode\" as assortment, sh.\"DiscountPct\" as discount, sh.\"AgentCode\" as agent, " +
  	"  sh.\"Blocked\" as blokk, sh.\"Deleted\", sh.\"TimeStamp\", sh.\"ExternalSTACode\", sh.\"IgnoreItemMinOrderQuantity\", " +
  	"  sh.\"EDIsoftSTACode\", sh.\"License\", sh.\"LicenseDate\", sh.\"CreatedTimeStamp\", " +
  	"  sh.\"ModifiedTimeStamp\",cus.\"CreditLimit\" as limit,\"Balance\" as used , cus.\"PaymentTerms\" as paym " +
       " FROM \"ShipTos\" sh LEFT JOIN \"Customers\" cus on cus.\"Code\"=sh.\"CustomerCode\" " ;
 
  	 String sql = "  select rtrim(a.[Sell-to Customer No_])+ '-' +[Ship-To Address Code] AS klient, a.[Customer No_] AS maksja,a.[External Document No_] AS number, a.[Due Date] AS maksepaev, sum(a.[Amount]) AS summa, " + 
     "     a.[Posting Date] as kuupaev , DATEDIFF(dd,a.[Due Date],GETDATE()) AS yle ,a.[Customer No_] AS payer FROM [Cust__Ledger_Entry] a  " +
      "      where  " +
"  (a.[Document Type] =2 or a.[Document Type]= 3)  AND a.[Posting Date] > getdate()-200 AND a.[Open]= 1 " +
          "   group by rtrim(a.[Sell-to Customer No_])+ '-' +[Ship-To Address Code],a.[Customer No_] ,a.[External Document No_],  " +
          "  a.[Due Date] ,a.[Posting Date], DATEDIFF(dd,a.[Due Date],GETDATE()) ,a.[Customer No_] having sum(a.[Amount])<> 0 order by  a.[Due Date]" ;



  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    TreeMap<String,Double> overDue = new TreeMap<String,Double>();
    try {
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
      	  cust.setMinsum(minsum);
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
          cust.addAgent(new Agent("HOUSE1")); 
          
          if (agen != null && agen.trim().length()>0){
				Agent agg = ((Agent)this.getAgents1().get(agen.trim()));
				
				if(agg != null && agg.getDubbler().size()>0){
					//System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
						cust.addAgent(new Agent((String)agg.getDubbler().elementAt(i)));
					//	System.out.println(cust.getName() + cust.getAgents());
					}
				}
			}        
        }
        
        if(cust.getID().equals("ERAISIK") ||cust.getID().equals("PAKKUMINE")){
        	Enumeration enume = getParent().getAgentsHash().elements();
            while (enume.hasMoreElements()) {
              Agent a = (Agent) enume.nextElement();
              cust.addAgent(a);
            }
        }
       
        int block = rs.getInt("blokk");
        if (block == 1 || block == 2) {
          cust.setSalesBlock(true);
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
       
        
        String gro = (String) groups.get(cust.getID());
        if( gro != null){
        	cust.addFixSortCode(gro);
        //	System.out.println("Settin fixsorti " + gro);
        }
        
        
    
                        customers.put(name, cust);
                        custMap.put(cust.getID(),cust);	
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
	    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsna.user"), getParent().getProps().getProperty("dsna.pass"));
	  }
      public String getDistribution() {
        return null;
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

  public TreeMap getProducts(Agent agent){

	  String cmd = "SELECT t.\"Code\" as itemid ,t.\"ExternalCode\" as code, t.\"Description\" as nimetus2, t.\"NameFull\" as nimetus, t.\"CategoryCode\" as grupp,t.\"UOM\" as unit, t.\"UnitsPerPack\" as kastis, " + 
      " t.\"VATPercent\" as vat, t.\"Barcode1\" as ean, t.\"Barcode2\", t.\"ExternalCode\", t.\"AllowedQuantities\" as optimum,  "+
      " t.\"ItemOrder\", t.\"Price\", t.\"Blocked\"    " +
      " , t.\"StandardPalletQty\" as pallet, t.\"CountryID\", t.\"ItemType\" as type, k.\"AvailToOrderQty\" as seis ," +
      " \"BlockedStatus\"  FROM \"Items\" t LEFT JOIN \"ItemQtys\" k ON k.\"ItemCode\" = t.\"Code\" where \"Blocked\" is false";
    
  
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
        }else{
        	code = "";        }
        
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimetus2").trim() + " " + code ;
     nimetus =  Converter.enCodeEst(nimetus);
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
        
      prod.setName(nimetus + add);

      
   
  
   /*   String alegr = rs.getString("alegr");
      if(alegr!=null && alegr.trim().length()>0){
       prod.setAlegrupp(alegr);
    }*/
      

       prod.setStock(rs.getDouble("seis"));   

        products.put(nimetus, prod);
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
	 int counter = 1;
	 String cmd = "SELECT h.ship_to_code as klient,r.item_code as toode, r.quantity as kogus, max(r.sales_price) as price," +
	 		"  invoice_date as kuup ,h.doc_id as dok " +   
 " FROM np_invoice_header h INNER JOIN  np_invoice_line r ON r.doc_id= h.doc_id " +
 " where invoice_date > current_date - 120 group by h.ship_to_code,r.item_code, r.quantity, " +  
 " invoice_date ,h.doc_id  order by h.ship_to_code,r.item_code,invoice_date " ;


	    if (isDebug()) System.out.println(cmd);

	    try {

	    	Statement stmt =  this.getNewConnection().createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	      counter++;
	      while(rs.next()){
	        String kliendikood = rs.getString("klient").trim();
	        String tootekood = rs.getString("toode").trim();
	        double kogus = rs.getDouble("kogus");
	        double hind = rs.getDouble("price");
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
	      if(counter< 4){
	      	try{
	        	 ResultSet rs = stmt.executeQuery(cmd);
	             counter++;
	             while(rs.next()){
	               String kliendikood = rs.getString("orderaccount").trim();
	               String tootekood = rs.getString("itemid").trim();
	               String lastdate = rs.getString("lastdate").substring(0,10);
	                        lastdate = lastdate.substring(5,10);
	               CustProd cp = new CustProd(kliendikood, tootekood, 0 ,0 ,0,lastdate,0,0,0," ");

	               cp.setRedline(rs.getString("date"));
	               custProds.add(cp);
	             }
	      	}catch (Exception ee){

	      	}

	      }
	    }
	    return custProds;
	  }





  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


    public void setBulkSales(Map agents){
  /*this.connectionUp();
    
  String cmd =" SELECT  cus.agent as agent,round(SUM(lineamount),0) AS summa " +
  " FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid and inv.ssordereraccount='DUNKER' and inv.itemid not like '9%' INNER JOIN custinvoicejour jou ON  " + 
  " sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount WHERE " +
  " inv.dataareaid='vir' AND sal.dataareaid='du2' AND sal.DIMENSION='SALE' AND cus.dataareaid='vir'  AND jou.dataareaid='du2' AND YEAR(sal.invoicedate) = YEAR(getdate())  " +
  "  AND MONTH(sal.invoicedate) = MONTH(getdate()) "+
  " GROUP BY cus.agent";


         if (isDebug()) System.out.println(cmd);
            try {
                Statement stmt =  this.getNewConnection().createStatement();
                ResultSet	      rs = stmt.executeQuery(cmd);
                while(rs.next()) {
                  String agentID = rs.getString("agent").trim();
                  Agent agent = (Agent)agents.get(agentID);
                  if (agent != null){
                    agent.setMonthSales((int)rs.getDouble("summa"));
                   // System.out.println(agent.getMonthSales());
                  }
                }
                rs.close();
                con.close();
            } catch (Exception e) {
                getParent().showError(e);
             }*/
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
  private String url = "jdbc:odbc:MobileSales_db_alco";
  private String url2 = "jdbc:odbc:droid";
  private String url3 = "jdbc:odbc:droid";
  private Connection con;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
