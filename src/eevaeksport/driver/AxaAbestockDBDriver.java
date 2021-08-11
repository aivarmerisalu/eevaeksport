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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
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
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class AxaAbestockDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
  public AxaAbestockDBDriver(EksportPanel parent){
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
    rcompany = getParent().getProps().getProperty("rest.company");
     }
  String user =  null;
  String pass = null;
  String rcompany = null;

  
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
			// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD Sortimendi tähised.

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
//System.out.println("rida " + i);
				if ( row.getCell((short) 0) != null) {

					try {
						prodID = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID = (row.getCell((short) 0).getStringCellValue());
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
									/*if(clGroup.trim().startsWith("RIM") && prodID != null
											&& rimi.get(prodID.trim()) != null){
										mline.setLineinfo("RimiKood:" + rimi.get(prodID.trim()));
									
									}*/
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

		 System.out.println("fix suurus " + result.size());

		return result;

	}

	private HashSet payers = null;

  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
          
      
          String sql = "    select  *,   datediff( day,DEFAULTAGREEMENTLINEEFFECTIVEDATE, DEFAULTAGREEMENTLINEEXPIRATIONDATE )    as paevi from AbestockMS_Myygilepingud  ";
  
          try {
        	  Connection con = this.getNewConnection();
        	Statement stmt = con.createStatement();
                  ResultSet rs = stmt.executeQuery(sql);

                  while (rs.next()) {
                	 
                          String custID = rs.getString("CUSTACCOUNT");
                          
                          
                          String prodID = rs.getString("itemid").trim();
                          String inventsizeid = rs.getString("inventsizeid");
                          String inventcolorid = rs.getString("inventcolorid");
                          
                          if(inventsizeid != null && inventsizeid.trim().length() > 0){
                        	  prodID = prodID + ":" + inventsizeid;
                          }
                          if(inventcolorid != null && inventcolorid.trim().length() > 0){
                        	  prodID = prodID + ":" + inventcolorid;
                          }
                          
                         String tootekood = (String) this.siffer_ID .get(prodID);
                          
                       //  System.out.println("sees " + tootekood + " " + prodID );
                         
                          double price = rs.getDouble("priceperunit");
                          
                          double ale = rs.getDouble("LINEDISCOUNTAMOUNT");
                      
                          String algus = rs.getString("DEFAULTAGREEMENTLINEEFFECTIVEDATE");
                          String lopp = rs.getString("DEFAULTAGREEMENTLINEEXPIRATIONDATE");
                          int paevi = rs.getInt("paevi");
                          MatrixLine mline = new MatrixLine();
                          
                    
                          if (custID != null && custID.trim().length() > 0) {
                        	//  System.out.println("sees2 " + custID + " " + payers.contains(custID.trim()) );
                        	  if(payers.contains(custID.trim())){
                        		  
                        		
                        	      mline.setCliGroup(custID.trim()); 
                        	  }else{
                        	      mline.setCustID(custID.trim()); 
                        	  }
                            
                          }
                       
                          if (tootekood != null && tootekood.trim().length() > 0) {
                                  mline.setProdID(tootekood.trim());
                          }
                     

                          if (price != 0) {
                                  mline.setPrice(price);
                          }else if( ale > 0){
                        	     mline.setDiscount(ale);
                          }
                          
                          if (algus != null && algus.trim().length() > 0) {
                              mline.setStartDate(algus.trim().substring(0,10));
                      }
                          if (lopp != null && lopp.trim().length() > 0) {
                              mline.setEndDate(lopp.trim().substring(0,10));
                      }  
                          
                        /*  if(paevi < 50){
                        	  
                        	   if (algus != null && algus.trim().length() > 0) {
                                   mline.setStartDate(algus.trim().substring(0,10));
                           }
                               if (lopp != null && lopp.trim().length() > 0) {
                                   mline.setEndDate(lopp.trim().substring(0,10));
                           }  	  
                         
                          }else{
                        	  mline.setProdLevel("pr");
                          }*/
                       
                          lines.add(mline);
                  }
                  
                  rs.close();
                  stmt.close();
                  con.close();

          

          } catch (SQLException ex) {
          ex.printStackTrace();
          }
          
          System.out.println("Price Lines Suurus "  + lines.size());
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
  
  private TreeMap kogused = null;

 public String getSaveStockInfo(){
	  
kogused = new TreeMap();
	  
String  cmd = " select  *  from AbestockMS_Laoseis where Hetkeseis > 0 ";


	  

       if (isDebug()) System.out.println(cmd);
       StringBuffer products = new StringBuffer();
       try {
    	   Connection con = this.getNewConnection();
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery(cmd);

         while (rs.next()) {
        	 
        	 
        	    double seis = rs.getDouble("Hetkeseis");
           String kood = rs.getString("KaubaID").trim();
           if(seis > 0){
               products.append(kood +  ";");
               products.append(" ;");
               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
             kogused.put(kood, seis + "");
           }
           }
           rs.close();
           st.close();
           con.close();
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
      return null;
     }

  
  
  
  public TreeMap getAgents(){
    String cmd = "select agent , tarnija  from AbestockMS_AgendiTarnija";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    TreeMap agentTarne = new TreeMap();
    
    try{
    	Connection con = this.getNewConnection();
  	  
        Statement st = con.createStatement();
    	ResultSet rs = st.executeQuery(cmd);
        while(rs.next()){
          String agent = rs.getString("agent").trim();
          String name = rs.getString("tarnija").trim();
          
          ArrayList aa = new ArrayList();
          if(agentTarne.get(agent) != null){
        	  aa = (ArrayList)agentTarne.get(agent);
          }
          aa.add(name);
          agentTarne.put(agent, aa);
          
        }
        rs.close();
        st.close();
        con.close();
        
    }catch(Exception e){
    	e.printStackTrace();
    }
    
    
    try {

    	   cmd = "select *  from AbestockMS_Agendid";
    	   		
		Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    Connection con = this.getNewConnection();
	  
        Statement st = con.createStatement();
    	ResultSet rs = st.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("groupid").trim());
          String name = rs.getString("name").trim();
          agent.setName(name);
          agent.setIsPilot(true);
          
          agent.setLanguage("EST");
          agent.setCountry("ee");
          agent.setPassWord("0000");
          String email = " ";
          
          if(propse.get(agent.getID()) != null){
        	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
          }
          
          if(email.length() > 3){
        	  agent.setEmail(email);
          }
        
          if(email.length() > 0){
        	  agent.setActive(true);
          }
        
          
          if(agentTarne.get(agent.getID())  != null){
        	  agent.prodGroupList = (ArrayList)agentTarne.get(agent.getID());
        	  
          }
        
		  agents1.put(agent.getID(), agent);
          agents.put(name, agent);
        }
        rs.close();
        st.close();
        con.close();
        
    } catch (Exception e){
        getParent().showError(e);
      }
    
    return agents;
  }
  
  
  private HashMap sortimendid = null;
 
  public TreeMap getCustomers(Agent agent){

	 
    Calendar now = Calendar.getInstance();
    TreeMap openOrders = new TreeMap();
    TreeMap klagent = new TreeMap();
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    payers =  new HashSet();
    
    try {
    	 Connection con = this.getNewConnection();
   
    	Statement stmr = con.createStatement();
    	
        ResultSet rs1 = stmr.executeQuery("select * from AbestockMS_AgentKlient");
        while(rs1.next()){
        	String ag = rs1.getString("Agendikood");
        	String kl = rs1.getString("Kliendikood");
        	Vector vv = new Vector();
       if(klagent.get(kl) != null){
    	    vv = (Vector)klagent.get(kl);
       }
         vv.add(ag);
         klagent.put(kl,vv);
        }
  
        rs1.close();
        stmr.close();
        
        

    	 stmr = con.createStatement();
    	
         rs1 = stmr.executeQuery("select * from AbestockMS_TodaysOrderProducts ");
        if(name_ID == null){
        	this.MakeName_ID();
        }
        if(siffer_ID == null){
        	this.MakeSiffer_ID();
        }
         while(rs1.next()){
       
        	
        	String kl = rs1.getString("custaccount");
        	String siffer = rs1.getString("Siffer");
        	double kogus = rs1.getDouble("kogus");
        	

	        if(siffer != null && siffer.endsWith("::")){
	        	siffer = siffer.substring(0, siffer.length()-2);
	        }
	        if(siffer != null && siffer.endsWith(":")){
	        	siffer = siffer.substring(0, siffer.length()-1);
	        }
	        
	       String tootekood = (String)siffer_ID.get(siffer) ;
	    
	      
	       if(tootekood != null){
	    	   
	    	   String name = (String)name_ID.get(tootekood); 
	        	
	        	String linetext = "[" +nf.format(kogus)+"] " + name +"|";
	        	String text = linetext;
	        	
			       if(openOrders.get(kl) != null){
			    	    text  = (String)openOrders.get(kl) + linetext;
			       }
			       //  System.out.println("tttt " + text);
			         openOrders.put(kl,text);
	    	   
	       }
        	
        	
		         
		        }
		  
        rs1.close();
        stmr.close();
        
        
    	
    	Statement stmt = con.createStatement();
    	String cmd = "select  * from AbestockMS_Kliendid" ;
      ResultSet rs = stmt.executeQuery(cmd);
      Statement stm = getNewConnection().createStatement();
      while(rs.next()){
        String name = Converter.enCodeEst(rs.getString("name").trim());
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);
        name = name + " " + accountnum;
        cust.setName(name);
        String payer = rs.getString("payer");
        if(payer != null && payer.trim().length() > 0){
        	cust.setPayer(payer.trim());
        	if(!payers.contains(payer.trim())){
        		payers.add(payer.trim());
        	}
        	
        }else{
        	cust.setPayer(accountnum);
        }
       
 
        String debtStr = rs.getString("debt");
        if (debtStr != null){
          cust.setDebt((int)Double.parseDouble(debtStr));
        }
        
        String usedStr = rs.getString("used");
        if (usedStr != null){
          cust.setUsedCredit((int)Double.parseDouble(usedStr));
        }
        
        String street = rs.getString("STREET");
        String zipcode = rs.getString("ZIPCODE");
        String city = rs.getString("CITY");

        if(street != null && street.trim().length() > 0){
        	
        	int count = street.length() - street.replace(".", "").length();
        	if(count > 1){
        		StringTokenizer st = new StringTokenizer(street,",");{
        			if(st.hasMoreTokens()){
        				cust.restCity = st.nextToken();
        			}
        			if(st.hasMoreTokens()){
        				cust.restStreet = st.nextToken();
        			}
        			if(st.hasMoreTokens()){
        				cust.restStreet =  cust.restStreet + " " + st.nextToken();
        			}
        		}
        		
        	}else{
        		
        		if(city != null && city.trim().length() > 0){
        			street = street + " " + city.trim();
        		}
        		
        		if(zipcode != null && zipcode.trim().length() > 0){
        			street = street + " " + zipcode.trim();
        		}
        	}
        	
        	cust.setAddress(Converter.enCodeEst(street.trim().replace('\n',',')));
        }
      
        
        Vector agen = (Vector)klagent.get(cust.getID());
        if (agen != null) {
        	for(int x = 0; x < agen.size(); x++) {
        		String aa = (String)agen.elementAt(x);
        		   cust.addAgent(new Agent(aa));  
        	}
        }
        
     
        String Tarnepaev = rs.getString("Tarnepaev");
        if(Tarnepaev != null && Tarnepaev.trim().length() > 0){
        	cust.setCity("Tarnepäev : " + Tarnepaev);	
        }

        
        if(openOrders.get(cust.getID()) != null){
        	cust.addToCustDataVec(cust.getID() + ";tarasaldo; ;Avatud tellimus |"   +  openOrders.get(cust.getID()));
        }
        
       double  credit = rs.getDouble("credit");
       if(credit != 0){
    	   cust.setCredit(((int)credit)+"");
       }
         
       java.sql.Timestamp bb = rs.getTimestamp("CONTRACTTODATE");
       cust.setContractOver(bb.getTime()+"");
       
       
       String term = rs.getString("PAYMTERMID");
       if(term != null && term.trim().length() > 0){
        	cust.setPaymTerm(term);	
        }
        
        bb = rs.getTimestamp("ContractStopDate");
       // System.out.println("BB aasta " + bb.getYear());
        if(bb.getYear() > 10  && bb.getTime() < now.getTimeInMillis()){
        	cust.setSalesBlock(true);	
        }
       
        
        
        
        // name accountnum   payer    debt used STREET ZIPCODE   
        //CITY block   payerblock  Tarnepaev  credit CONTRACTTODATE  PAYMTERMID ContractStopDate    

        
                        customers.put(name, cust);
                        custMap.put(cust.getID(),cust);	
      }
      rs.close();
      
      String sql = "select * from AbestockMS_GetOpenInvoice ";

      
      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim());
                        debt.setPayerID(debt.getCustID());
                        String number = rs.getString("number").trim();
                        debt.setDoknr(number);
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(rs.getDouble("tasumata"));
                        debt.setDate(rs.getDate("kuupaev"));
                        debt.setDue(rs.getString("yle"));
                        this.debt.addElement(debt);
                      }
     rs.close();
     stmt.close();
     con.close();

    } catch (Exception e){
      getParent().showError(e);
    }
        
    return customers;
  }



  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }


      public String getDistribution() {
        return null;
      }


  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "   select * from AbestockMS_Kaubagrupid where nimi  != '' ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("kood").trim();
      String nimi = rs.getString("nimi").trim();
      nimi = nimi.replaceAll("Digitaalsed", "Dig");
      nimi = nimi.replaceAll("Elvital", "Elv");
      nimi = nimi.replaceAll("Fructis", "Fru");
      nimi = nimi.replaceAll("Hõõglambid /", "Hõõ");
      nimi = nimi.replaceAll("Hamba", "Ham");
      nimi = nimi.replaceAll("Jalatsihooldusvahendid", "Jal hooldusvahendid");
      nimi = nimi.replaceAll("Juukse", "Juu ");
      nimi = nimi.replaceAll("Kiirnuudlid", "Kinu");
      nimi = nimi.replaceAll("Klassikalised", "Kel");
      nimi = nimi.replaceAll("Laste", "Las");
      nimi = nimi.replaceAll("Loomade", "Loo");
      nimi = nimi.replaceAll("Meeste", "Mee");
      nimi = nimi.replaceAll("Moment", "Mom");
      nimi = nimi.replaceAll("Naiste", "Nai");
      nimi = nimi.replaceAll("Patareid /", "Pat");
      nimi = nimi.replaceAll("Patareid", "Pat");
      nimi = nimi.replaceAll("Puhastustarbed/", "Puh ");
      nimi = nimi.replaceAll("Spetsiaaltoidud", "Spe");
      nimi = nimi.replaceAll("Tualett", "Tua ");
      nimi = nimi.replaceAll("Vitaminiseeritud", "Vit");
  
      
        tm.put(liik,  Converter.enCodeEst(nimi));
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}


  public TreeMap getProdGroupsSecond() throws SQLException {

	  Connection con = getNewConnection();
	    TreeMap   tm = new TreeMap();
	    String cmd = "  select  Abestock_Tarnija  from AbestockMS_Kaup1  union " +
	    		" select Abestock_Tarnija from AbestockMS_Kaup2  group by Abestock_Tarnija";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(cmd);
	    while (rs.next()) {
	      String liik = rs.getString("Abestock_Tarnija");
	      if( liik != null && liik.trim().length() > 0 ){
	    	     tm.put(liik,  Converter.enCodeEst(liik));
	      }
	     }
	    rs.close();
	    st.close();
	    con.close();

	    return tm;
  }
  public TreeMap getAgents1() {
		return agents1;
	}

  private TreeMap agents1 = new TreeMap();

  public TreeMap getProducts(Agent agent){

	  String cmd = "  select Kaup_ID ,HindEEK, Grupp1, JoonKood, Nimetus, VYS, Liik, Varjatud, Siffer, Abestock_Tarnija, Mintellimus, Hetkeseis, Kauba_PeaKood, WebShop, Brand, MMD ,KasteAlusel, Tarnija" + 
		" from AbestockMS_Kaup1 where HindEEK > -1 union " +
		" select Kaup_ID ,HindEEK, Grupp1, JoonKood, Nimetus, VYS, Liik, Varjatud, Siffer, Abestock_Tarnija, Mintellimus, Hetkeseis, Kauba_PeaKood, WebShop, Brand, MMD ,KasteAlusel, Tarnija " +
		" from AbestockMS_Kaup2 where HindEEK > -1 ";
	  
	  MakeSiffer_ID();
    HashSet sold = getCustomerProductsListUU();
    
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {
    	 Connection con = this.getNewConnection();
       
    	Statement stmt =  con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("Kaup_ID").trim();
        Product prod = new Product(kood);
        Double dd = rs.getDouble("HindEEK");
        if(dd != null ){
        	prod.setPrice(dd);
            	
        }else{
        	prod.setPrice(1);
        }
        
        String grupp = rs.getString("Grupp1");
        if(grupp!=null && grupp.trim().length()>0){
         prod.setGroup(grupp.trim());
        }
        
        String ean = rs.getString("JoonKood");
        String add = "";
        if(ean != null && ean.trim().length()>0){
        prod.setEAN(ean);
        
        prod.setPhotoid(ean.replaceAll(" ", "-")+ ".jpg");
        
        int ll  = ean.trim().length();
        if(ll > 4){
        	  add = " " +  ean.trim().substring(ll-4);
        }
         }

        
        String nimetus = rs.getString("Nimetus").trim()  ;
     nimetus =  Converter.enCodeEst(nimetus);
    
     
     prod.setInCases(rs.getDouble("VYS"));
     
      String Siffer = rs.getString("Siffer");
       
     

      String unit = "tk";
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit);
    }
      
      String vat = "20";
      if(vat!=null && vat.trim().length()>0){
       prod.setVat("20");
    }
  
     
      
     String grupp2 = rs.getString("Abestock_Tarnija");
      if(grupp2 != null && grupp2.trim().length()>0){
             prod.setGroupSecond(grupp2.trim());
          }

      
      double stock = rs.getDouble("Hetkeseis");
      if (stock > 0){
     	 prod.setStock(stock);
      }else{
    	  prod.setStock(0);
      }
     	
          
      String Brand = rs.getString("Brand");
      if(Brand != null && Brand.trim().length()>0){
             prod.setType(Brand.trim());
          }
     
      double KasteAlusel = rs.getDouble("KasteAlusel");
      if (KasteAlusel > 0 && prod.getInCases() > 0){
     	 prod.setOnPallet((int)(KasteAlusel * prod.getInCases()));
      }
      
      String Tarnija = rs.getString("Tarnija");
      if(Tarnija != null && Tarnija.trim().length()>0){
             prod.setAgentGroup(Tarnija.trim());
          }
      
      prod.setNameSorting(nimetus);
     
      if(prod.getGroupSecond() != null && (prod.getGroup().equals("219") ||
    		  prod.getGroup().equals("222") ||
    		  prod.getGroup().equals("221") ||
    		  prod.getGroup().equals("223") ||
    		  prod.getGroup().equals("227") ||
    		  prod.getGroup().equals("230") ||
    		  prod.getGroup().equals("260") ||
    		  prod.getGroup().equals("285") ||
    		  prod.getGroup().equals("296") ||
    		  prod.getGroup().equals("342") ||
    		  prod.getGroup().equals("345") ||
    		  prod.getGroup().equals("429") ||
    		  prod.getGroup().equals("477") ||
    		  prod.getGroup().equals("488") ||
    		  prod.getGroup().equals("534") ||
    		  prod.getGroup().equals("535") ||
       		  prod.getGroup().equals("542") ||
       		  prod.getGroup().equals("708") ||
       		  prod.getGroup().equals("805") ||   		  
       		  prod.getGroup().equals("807") ||
       		  prod.getGroup().equals("809") ||
       		  prod.getGroup().equals("810") ||
       		  prod.getGroup().equals("812") ||
       		  prod.getGroup().equals("1005") ||
       		 
       		 
       		    		  prod.getGroup().equals("402")) ){
    	  if( Siffer!= null){
    		  nimetus = Siffer.trim() + " " + nimetus + add +"{" +Siffer.trim().length()+"}";
    	  }else{
    		  nimetus = nimetus + " " + Siffer.trim() + add;
    	  }
    	
      } else if( Siffer!= null && Siffer.trim().length() > 0 ){
    	  
          nimetus = nimetus + " " + Siffer.trim() + add;
      }
      
      prod.setName(nimetus);
      
      if( prod.getStock() > 0 || sold.contains(prod.getID()) ){
			 products.put(nimetus, prod);
		     prodHash.put(prod.getID(), prod);
		}
		       
        
      }

      rs.close();
      stmt.close();
      con.close();
      
    } catch (Exception e){
      getParent().showError(e);
    }
    
    return products;
  }



  public Vector getCustomerProductsList(String agentCode){
	    return custProds;
	  }
  
 private Vector custProds = null;
 
 private HashMap siffer_ID = null;
 
 private void MakeSiffer_ID(){
	 siffer_ID = new HashMap();

	    try {
	    	String cmd = "select Kaup_ID , Siffer from AbestockMS_Kaup1  union " +
            " select Kaup_ID , Siffer from AbestockMS_Kaup2  ";
       Connection con = this.getNewConnection();
	      Statement stmt =  con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	      
	      while(rs.next()){
	        String Kaup_ID = rs.getString("Kaup_ID");
	        String Siffer = rs.getString("Siffer");
	        if(Kaup_ID != null && Siffer != null){
	            siffer_ID.put( Siffer.trim() , Kaup_ID.trim());
	        }
	      }
	      rs.close();
	      stmt.close();
	      con.close();
	    } catch (Exception e){
	      getParent().showError(e);
	    
	    }
	 
 }
 
 HashMap name_ID = null;

 private void MakeName_ID(){
	 name_ID = new HashMap();

	    try {
	    	String cmd = "select Kaup_ID , Nimetus from AbestockMS_Kaup1  union " +
            " select Kaup_ID , Siffer from AbestockMS_Kaup2  ";
       Connection con = this.getNewConnection();
	      Statement stmt =  con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	      
	      while(rs.next()){
	        String Kaup_ID = rs.getString("Kaup_ID");
	        String name = rs.getString("Nimetus");
	        if(Kaup_ID != null && name != null){
	            name_ID.put( Kaup_ID.trim(),name.trim() );
	        }
	      }
	      rs.close();
	      stmt.close();
	      con.close();
	    } catch (Exception e){
	      getParent().showError(e);
	    
	    }
	 
 }
 
  public HashSet getCustomerProductsListUU(){
	     custProds = new Vector();
	     
	     
	     //1. teeme valmis tarnimata kauba objekti  
	     HashMap puudu = new HashMap();
	     
	     try {
		    	String cmd = "select * from AbestockMS_Tarnekindlus ";
	          Connection con = this.getNewConnection();
		      Statement stmt =  con.createStatement();
		      ResultSet rs = stmt.executeQuery(cmd);
		      
		      while(rs.next()){
		    	  
		        String kliendikood = rs.getString("klient").trim();
		        String siffer = rs.getString("Siffer").trim();
		        
		        if(siffer != null && siffer.endsWith("::")){
		        	siffer = siffer.substring(0, siffer.length()-2);
		        }
		        if(siffer != null && siffer.endsWith(":")){
		        	siffer = siffer.substring(0, siffer.length()-1);
		        }
		        
		       String tootekood = (String)siffer_ID.get(siffer) ;
		      
		       if(tootekood != null){
		        double kogus = rs.getDouble("Tarnimata");
		       
		         HashMap sisu = (HashMap) puudu.get(kliendikood);
		         if(sisu == null){
		        	 sisu = new HashMap();
		         }
                  sisu.put(tootekood, kogus+"");
		         puudu.put(kliendikood, sisu);
		       }
		      }
		      rs.close();
		      stmt.close();
		      con.close();
		      
		      System.out.println("puudu " + puudu.size());
		    } catch (Exception e){
		      getParent().showError(e);
		    
		    }
	     
	     
	     
	     
	     
	     
	     
	     HashSet res = new HashSet();
	
	     String cmd = "select * from  AbestockMS_CustomerProducts";
     

	    if (isDebug()) System.out.println(cmd);

	    try {
	    	
          Connection con = this.getNewConnection();
	      Statement stmt =  con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	      
	      while(rs.next()){
	        String kliendikood = rs.getString("orderaccount").trim();
	        String siffer = rs.getString("Siffer").trim();
	        
	        if(siffer != null && siffer.endsWith("::")){
	        	siffer = siffer.substring(0, siffer.length()-2);
	        }
	        
	       String tootekood = (String)siffer_ID.get(siffer) ;
	      
	       if(tootekood != null){
	    	   
	    
	        if(!res.contains(tootekood)){
	        	res.add(tootekood);
	        }
	        double kogus = rs.getDouble("qty");
	  
	        
	        double saamatta = 0;
	        if(puudu.get(kliendikood) != null){
	        	HashMap ttt = (HashMap)puudu.get(kliendikood);
	        	if(ttt != null && ttt.get(tootekood) != null){
	        		saamatta = Double.parseDouble(ttt.get(tootekood) +"");
	        	}
	        }
	        
	        String lastdate = rs.getString("lastdate").substring(0,10);
	                 lastdate = lastdate.substring(5,10);
	                 double hind = rs.getDouble("hind");
	     	        String bill = rs.getString("dok");          
	        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,hind,saamatta,0," ");
	        cp.setAbiKood(bill);

	        
	        custProds.add(cp);
	       }
	      }
	      rs.close();
	      stmt.close();
	      con.close();
	    } catch (Exception e){
	      getParent().showError(e);
	    
	    }
	    
	 //   System.out.println("CustProd suurus " + res.size() + " " + res.contains("637152389"));
	    
	    
	    return res;
	  
  }





  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


    public void setBulkSales(Map agents){
  this.connectionUp();
    
  String tarnijad = "select agent,Tarnija,summa from AbestockMS_TarnijaKuuMyyk order by agent,Tarnija ";
  if (isDebug()) System.out.println(tarnijad);
  try {
  	 Connection con = this.getNewConnection();
      Statement stmt =  con.createStatement();
      ResultSet	  rs = stmt.executeQuery(tarnijad);
      while(rs.next()) {
      	
        String agentID = rs.getString("agent");
        String Tarnija = rs.getString("Tarnija");
        int summa = (int)rs.getDouble("summa");
        		
        
        if (agentID != null){
        Agent agent = (Agent)agents.get(agentID.trim());
        agent.setStart(agent.getStart() +  Converter.enCodeEst(Tarnija + "|" + nf.format(summa) + " €|" ));
        }
      }
      rs.close();
      stmt.close();
      con.close();
  } catch (Exception e) {
      getParent().showError(e);
   }
  
  String cmd ="select * from AbestockMS_AgendiKuuMyyk";
                                  

 
         if (isDebug()) System.out.println(cmd);
            try {
            	 Connection con = this.getNewConnection();
                Statement stmt =  con.createStatement();
                ResultSet	  rs = stmt.executeQuery(cmd);
                while(rs.next()) {
                	
                  String agentID = rs.getString("agent");
                  if (agentID != null){
                  Agent agent = (Agent)agents.get(agentID.trim());
                    agent.setMonthSales((int)rs.getDouble("summa"));
                  }
                }
                rs.close();
                stmt.close();
                con.close();
            } catch (Exception e) {
                getParent().showError(e);
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
    return "AxaAbestock";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  
  public String getPilotDayData(Agent agent){

	   String sql2 =  " select name , Kuupaev , ArveNr , summa  from AbestockMS_TodaysInvoicelist where agent = '" + agent.getID() + "'" ;
	   
StringBuffer sb = new StringBuffer();
this.connectionUp();
try {
	 nf.setMinimumFractionDigits(2);
 Connection con = this.getNewConnection();
PreparedStatement stmt2 = con.prepareStatement(sql2);
//stmt2.setString(1, agent.getID());
String sep= System.getProperty("line.separator");
  ResultSet  rs = stmt2.executeQuery();

double muusum = 0;
boolean droid = true;
// agent      Kliendikood     name        ArveNr      Kuupaev  summa  
while(rs.next()){

   String nimi = rs.getString("name");
   if (nimi == null){ nimi = " "; } else {nimi = nimi.trim();}

	sb.append(nimi + "\n");
	sb.append(rs.getString("Kuupaev").trim().substring(0,10));
	sb.append("  Nr." + rs.getString("ArveNr").trim() + "\t");
	double summa = Double.parseDouble(rs.getString("summa").trim());
	muusum = muusum + summa;
	String sum = getNF().format(summa) ;
   sb.append(sum + " eur\n" + sep);
 
  
}

sb.append("------------------------------\n");
sb.append(" Kokku :" + getNF().format(muusum)  + " eur\n\n");

rs.close();
stmt2.close();
con.close(); 	 
	


} catch (Exception e) {
 getParent().showError(e);
}

connectionDown();

Converter.saveFile(parent.getProps().getProperty("stockrefdir") + "st"+ agent.getID() + "report.txt", sb.toString());
return "";
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
       
  private String url = "jdbc:odbc:AXA_ABES";
  private Connection con;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
