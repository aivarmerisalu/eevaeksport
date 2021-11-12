package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
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

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalLineWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
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

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class LabbisSkonisDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public LabbisSkonisDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
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
  
   public String getPilotDayData(Agent agent){
     return null;
   }
   public String getSaveDebtInfo() {
   	return null;
   }

   public Vector getPriceMatrixData(){
	
	  	 String    sql = " SELECT '' AS custid, KLYGDET.KOD as clgroup,PR_GR1 as prodid, GRUP as prodgroup,0 as price, " +
	  	 		"replace(FORMUL,'@rk3*','') as disc,klyg.Galioja_nuo as startday , klyg.Galioja_iki as endday " +
   "                  FROM dba.KLYGDET INNER JOIN  " +
    "                 dba.klyg ON klyg.KOD=KLYGDET.KOD  and    " +                                         
     "                                      (klyg.Galioja_nuo is null OR getdate() >= klyg.Galioja_nuo) AND " +
      "                                     (klyg.Galioja_iki is null OR getdate() <= klyg.Galioja_iki) " ;
	
     Vector result = new Vector();
       try {
    	   Connection con = getNewConnection2();
 	        Statement stmt = con.createStatement();
 	        ResultSet rs = stmt.executeQuery(sql);
         while (rs.next()) {

           String custID = Converter.enCodeEst(rs.getString("custid").trim());
           String clGroup = rs.getString("clgroup");
           String prodID = rs.getString("prodid");
           String prodGroup = rs.getString("prodgroup");
          
           double price = rs.getDouble("price");
           String disco = rs.getString("disc");
           double disc = 0;
           if(disco != null){

               if(disco.indexOf("*") != -1){
            	   disco = disco.substring(disco.indexOf("*")+1);
               }
               try{
            	   disc = (1-Double.parseDouble(disco))*100;
                   	   
               }catch(Exception r){
            	 //  System.out.println("jama " + disco);
               }   
           }
           String sdate = rs.getString("startday");
           String date = rs.getString("endday");
               
           MatrixLine mline = new MatrixLine();

           if (custID != null && custID.length() > 0) {
             mline.setCustID(custID);
           }
         
           
           if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
             mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
           }

         
           if(date != null && !date.startsWith("1753")){
          	 mline.setEndDate(date.substring(0,10));
           }
           if(sdate != null  && !sdate.startsWith("1753")){
            	 mline.setStartDate(sdate.substring(0,10));
            	 if(mline.getEndDate() == null){
            		 mline.setStartDate(null);
            	 }
             }
           if (prodID != null && prodID.length() > 0) {
             mline.setProdID(prodID.trim());
           }
           if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
             mline.setProdGroup(prodGroup.trim());
             mline.setDiscGroup(prodGroup.trim());
           }

           if (price != 0) {
             mline.setPrice(price);

           }
           if (disc != 0) {
               mline.setDiscount(disc);

             }
           if(mline.getPrice() > 0 || mline.getDiscount() > 0){
               result.add(mline);    
           }
           
         }
         rs.close();
         stmt.close();
         con.close();
       }catch(Exception e){
      	 e.printStackTrace();
       }
	
  return result;
    }


  
   private Connection getNewConnection2() throws SQLException{
    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsna.user"), getParent().getProps().getProperty("dsna.pass"));
  }

   
   public void insertDefective(DefectiveWrapper wr){
    
  }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
	  
	     }

	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	     	     }


	 public String getProductSortingData(){
	   	return null;
	   }

   public ManagerData getManagerData() throws SQLException{
     return null;
  }


   public void insertDistribution(DistEvalListWrapper wr){
   }
 
   public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		File f = new File(this.getExcelSortDir() + "Asortimento matrica.xls");
		
		
		
		try {
			
			
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheetAt(0);
			HSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(4);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 3; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

		//	 System.out.println(groups.toString());

			for (int i = 7; i < sheet.getLastRowNum(); i++) {
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
                   	
					// Siin l�heme reasisse rokkima ja vaatama kas se toode
					// l�heb kaasa
					for (int a = 3; a < row.getLastCellNum(); a++) {

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

							if (go != null && go.trim().toLowerCase().equals("x")) {
                                     if(a > 10){
                                    //	 System.out.println( a + " " + prodID + " " +
                						//		 clGroup + " " + go); 
                                     }
								 

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
									if(!sorts.contains(prodID.trim()))
									sorts.add(prodID.trim());
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


   public DealerProduct DealerProductsList() {

   	DealerProduct dealerProds = new DealerProduct();
    try {
    	  String cmd = " SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [UAB Vilandra$Item Ledger Entry] l " +
          " INNER JOIN [UAB Vilandra$Item] t ON [No_]=l.[Item No_] WHERE   t.[Blocked]=0 " + 
          " GROUP BY l.[Location Code],l.[Item No_] having sum(l.[Quantity]) != 0  ORDER BY l.[Item No_]"; 

       	 Connection con = getNewConnection2();
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(cmd);
   	        while(rs.next()){
   	             String toode = null;
   	             double seis = 0;;
   	             String ladu  = null;
   	              	toode =rs.getString("kood");
   	              	seis = rs.getDouble("kogus");
   	              	ladu = rs.getString("ladu");
   	                 dealerProds.add(ladu, toode,seis+""," "," " );
   	        }

   	      rs.close();
   	      con.close();

   	    System.out.println(cmd);
    } catch (Exception e) {
        e.printStackTrace();
    }
     return dealerProds;
   }


       public String getSaveStockInfo(){

    	   String cmd = "SELECT KOD AS kood, sum([LIKUTIS]) AS kogus,  left (PADALINYS ,1) as ladu FROM dba.[v_PrekiuLikuciaiList] where  " +
    				"	PADALINYS in ( '0BF001','0AS101','0PS119','0TS100','1BF002','1AS203','1PS210','1TS200','2BF003','2AS303','2PS308','2TS300','3BF004','3AS403','3PS404','3TS400','0AS132') " +
    	           " GROUP BY left (PADALINYS ,1), KOD having sum(LIKUTIS) != 0  ORDER BY KOD";
    	   
    		         if (isDebug()) System.out.println(cmd);
    		         StringBuffer products = new StringBuffer();
    		         try {
    		        	 Connection con = getNewConnection2();
    		           Statement st = con.createStatement();
    		           ResultSet rs = st.executeQuery(cmd);

    		           while (rs.next()) {

    		             String kood = rs.getString("kood").trim();
    		             double seis = rs.getDouble("kogus");
    		             String ladu = rs.getString("ladu").trim();
    		             if(seis < 1){
    		            	 seis = 1;
    		             }
    		             
    		             if(seis > 0){
    		                 products.append(kood +  ";");
    		                 products.append(" ;");
    		                 products.append(getParent().getNF().format(seis) + ";" + ladu + "\n");
    		            //   kogused.put(kood, seis + "");
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


   public Vector getDebt(String agentCode){
        return this.debtList;
  }

   public void connectionDown(){
   }

   public void connectionUp(){
   }
   
   
   public DealerProduct getDealerProductsList() {


       DealerProduct dealerProds = new DealerProduct();
       try {

    	   
    	   String sql = "SELECT KOD AS kood, sum([LIKUTIS]) AS kogus,  left (PADALINYS ,1) as ladu FROM dba.[v_PrekiuLikuciaiList] where  " +
			"	PADALINYS in ( '0BF001','0AS101','0PS119','0TS100','1BF002','1AS203','1PS210','1TS200','2BF003','2AS303','2PS308','2TS300','3BF004','3AS403','3PS404','3TS400','0AS132') " +
           " GROUP BY left (PADALINYS ,1), KOD having sum(LIKUTIS) != 0  ORDER BY KOD";

       
            if (isDebug()) System.out.println(sql);
             Connection con = this.getNewConnection2();
               Statement stm = con.createStatement();
               ResultSet rst = stm.executeQuery(sql);

                 while (rst.next()) {

                     String toode = rst.getString("kood").trim();
                     int seis = (int)Double.parseDouble(rst.getString("kogus").replace(',','.'));
               	  String ladu  =  rst.getString("ladu").trim();
                   
                  
                   dealerProds.add(ladu, toode,seis+""," "," " );                }
               rst.close();
               stm.close();
               con.close();

       } catch (Exception e) {
           getParent().showError(e);
       }

        return dealerProds;
       
      }
   


           public String getDealerCustomerCSV(String agent){
            return null;
            }

           public String  getCustProdDiscounts(Agent agent){
            return null;
            }

           
           public TreeMap getProdGroupsSecond() throws SQLException {
           	

        	 

             TreeMap tm = new TreeMap();

           /*     Connection con = getNewConnection2();
                String cmd = " select left(KOD,2) as liik ,max( left(PAV,24)) as nimi from dba.[songrup] group by left(KOD,2) " ;
                   try {
              Statement stmt = con.createStatement();
               ResultSet rs = stmt.executeQuery(cmd);
              setTreeGroup(true);
             // tm.put("xxxx", "Tester");
               while (rs.next()) {
               	 String liik = Converter.cleanLitLatEst(rs.getString("liik").trim());
               	 String nimetus = rs.getString("nimi").trim();
               	
               	String	 nimi = nimetus.replaceAll("Arbata", "Arb.").replaceAll("Aromatai", "Aro.")
               	.replaceAll("Tabakas", "Tab.").replaceAll("Tabako", "Tab.").replaceAll(",", " ").replaceAll("  ", " ") ;


               	 
               		  tm.put(liik.trim(), Converter.enCodeEst(nimi.trim()));  
               }
             }
             catch (Exception e) {
               getParent().showError(e);
             }
             connectionDown();
           */
             return tm;
           }

           public TreeMap getProdGroups() throws SQLException {

        	   Connection con = getNewConnection2();

             TreeMap tm = new TreeMap();

                String cmd = " select KOD as liik ,PAV as nimi from dba.[R0] " ;
                   try {
              Statement stmt = con.createStatement();
               ResultSet rs = stmt.executeQuery(cmd);
              setTreeGroup(true);
             // tm.put("xxxx", "Tester");
               while (rs.next()) {
               	 String liik = Converter.cleanLitLatEst(rs.getString("liik").trim());
               	 if(liik.length() > 3){
               		// liik = liik.substring(0,3);
               	 }
               	 String nimetus = rs.getString("nimi");
               	 if(nimetus != null && nimetus.trim().length() > 0){
               		nimetus = nimetus.trim() + " " + liik;
               	  String	 nimi = nimetus.replaceAll(",", " ").replaceAll("  ", " ") ;
	               tm.put(liik.trim(), Converter.enCodeEst(nimi.trim()));  
               }
             } 
                 	 }
             catch (Exception e) {
               getParent().showError(e);
             }
             connectionDown();
           
             return tm;

           }


             
  
   private TreeMap twoone = new TreeMap();
   private TreeMap teised = new TreeMap();
   
   public TreeMap getAgents(){
	   StringBuffer sb2 = new StringBuffer();
	    
	    String cmd = "SELECT [KOD] AS kood,[PAV] AS nimi, left(KOD,1) AS objekt FROM  " +
		"   dba.vairuot WHERE PAV not like '*%'";
        
	    if (isDebug()) System.out.println(cmd);
	    TreeMap agents = new TreeMap();
	    try {
	    	 HashMap asendajad = new HashMap();
	 	      
	 	     
              try{
            		 FileReader fr = new FileReader(this.getExcelDir() + "vacation.txt");
    	 	         BufferedReader reader = new BufferedReader(fr);
    	 	       String line = null;
    	 	        while((line = reader.readLine()) != null ){
    	 	           
    	 	        	StringTokenizer st = new StringTokenizer(line, ";");
    	 	//  System.out.println(line);
    	 	          if (st.hasMoreTokens()) {
    	 	            String ag1 =	st.nextToken();
    	 	            String man = st.nextToken().trim();
    	 	            Vector vv = new Vector();
    	 	            if(asendajad.get(ag1) != null){
    	 	           	 vv = (Vector)asendajad.get(ag1);
    	 	           	 vv.add(man);
    	 	           	 asendajad.put(ag1, vv);
    	 	            }else{
    	 	           	 vv.add(man);
    	 	           	 asendajad.put(ag1, vv); 
    	 	             }
    	 	            }
    	 	          }
    	 		 	  
              }catch(Exception e){
            	  
              }
	 	    
	 	    	
	    	
              boolean pilot = false ;
              
              if("true".equals(getParent().getProps().getProperty("pilot"))){
           	   pilot = true;
              }
	        Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	          Agent agent = new Agent(rs.getString("kood").trim());
	          String name = rs.getString("nimi").trim();
	          agent.setObject(rs.getString("objekt"));
	          agent.setName(name);
	          if(pilot){
	        	  agent.setIsPilot(true);
	          }

				String agentAGENTS = getParent().getProps().getProperty("dub." +agent.getID());
				if(agentAGENTS != null){
    	           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
			    while(stag.hasMoreTokens()){  	
                 agent.addDubbler(stag.nextToken());
              //   System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
			    }

				}
			    if(asendajad.get(agent.getID()) != null){
                	Vector vv = (Vector)asendajad.get(agent.getID());
                	Enumeration aa = vv.elements();
                	while(aa.hasMoreElements()){
                		agent.addDubbler((String)aa.nextElement());
                	}
                }
			    sb2.append(agent.getID() + ";" + name + "\n");  
	          agents.put(name, agent);
	          agents1.put(agent.getID(), agent);
	        }

	        Agent aa = new Agent("HOUSE");
	        aa.setName("office1");
	        agents.put("office1", aa);
	        
	        Agent aa2 = new Agent("WARE");
	        aa2.setName("Ware");
	        agents.put("Ware", aa2);
	        
	        
            rs.close();
            stmt.close();
	        con.close();
	        

	       
	    } catch (Exception e){
	        getParent().showError(e);
	      }
	    if(sb2.length() > 2){
	    	try{
	    		 Converter.saveFileInUTF8( "agent.txt", Converter.enCodeEst(sb2.toString()));
	    	}catch (Exception e){
	    		
	    	}
	      
	     }

     return agents;
   }

       public String getDistribution() {
         return null;
       }
  private String getVisitDay(String input){
	  String res = "";
	  
	  for (int i = 0 ; i < input.length(); i++ ){
		char a = input.charAt(i);
		if(a == 'A'){
			res = res + "E";
		}
		if(a == 'B'){
			res = res + "T";
		}
		if(a == 'C'){
			res = res + "K";
		}
		if(a == 'D'){
			res = res + "N";
		}
		if(a == 'E'){
			res = res + "R";
		}
		if(a == 'F'){
			res = res + "L";
		}
		if(a == 'G'){
			res = res + "P";
		}
		if(a == '1'){
			res = res + "2";
		}if(a == '2'){
			res = res + "1";
		}if(a == '3'){
			res = res + "4";
		}if(a == '4'){
			res = res + "8";
		}if(a == '5'){
			res = res + "16";
		}if(a == '6'){
			res = res + "32";
		}
	  }
	  
	  if(res.length() == 0){
		  res = " ";
	  }
	  return res ;
  }
  
  private HashSet sorts = new HashSet();
  
  	
  	
  private HashMap fixGroups = new HashMap();
  
       public TreeMap getCustomers(Agent agent){

    	   Converter.getManagersReplacements(this.getAgents1(), rcompanyx, user , pass);

    	   
     	   Converter cc = new Converter();
	    	  HashMap kyssad =   cc.getQuestionaire(this.getExcelDir());
	    	
	    	  HashMap volad = new HashMap();
              String sql3 =  " select Kontrahentas as klient , sum(SUMA1) AS yle FROM dba.v_DebtList_SkoluKartotekaPagalDokDebitor where " + 
				  "   SUMA1 != 0 and RegardingObjectEntity in ('Sale','TransactionJournal') " +
                 " and APMIKI < getdate()  group by Kontrahentas  ";
			    try {
			      Connection con3 = getNewConnection2();
			      Statement stmt3 = con3.createStatement();
			      ResultSet rs3 = stmt3.executeQuery(sql3);
			      while(rs3.next()){
			         String kl = rs3.getString("klient").trim();
			         double sum = rs3.getDouble("yle");
			         if(sum !=0){
			         	volad.put(kl, sum);
			         }
			      }
					rs3.close();
					stmt3.close();
					con3.close();
			      } catch (Exception e){
			         e.printStackTrace();
			       }


	    	  
	    try {

    	      String cmd = "  select Kontrahentas AS klient, Kontrahentas AS maksja,[DOK_NRBL] AS number, [DOK_DAT] AS kuupaev , APMIKI as makse, sum(SUMA1) AS summa, " + 
    	                       "    DATEDIFF(dd,[APMIKI],GETDATE()) AS due ,Kontrahentas AS payer FROM dba.v_DebtList_SkoluKartotekaPagalDokDebitor where  "+
								"	DOK_DAT > GETDATE()-200 and SUMA1 != 0 and  RegardingObjectEntity in ('Sale','TransactionJournal')   " +
							"		group by Kontrahentas,DOK_NRBL,DOK_DAT,APMIKI";

    	  System.out.println(cmd);
    	       this.debtList = new Vector();
    	      Connection conv = getNewConnection2();
    	       Statement stmt1 = conv.createStatement();
    	       ResultSet rs1 = stmt1.executeQuery(cmd);
    	       while(rs1.next()){
    	        Debt debt = new Debt();
    	       String custID = rs1.getString("klient").trim();

    	       String payer = rs1.getString("maksja").trim();
    	        debt.setCustID(custID);

    	        String doknr = rs1.getString("number");
    	        if(doknr != null && doknr.trim().length()>6){
    	        	doknr = doknr.substring(2,doknr.length());
    	        }
    	        if(doknr != null){

        	        debt.setDoknr(doknr);	
    	        }else{

        	        debt.setDoknr("NoNumber");
    	        }
    	        debt.setDate(rs1.getDate("kuupaev"));
    	        debt.setDuedate(rs1.getDate("makse"));
    	        debt.setSum(rs1.getDouble("summa"));
    	        int dueint = rs1.getInt("due");
    	        debt.setDue(dueint + "");
    	        int old = 0;
    	        if(debtDays.get(custID) != null){
    	         old = Integer.parseInt((String)debtDays.get(custID));
    	        }
    	        if(old<dueint){
    	       this.debtDays.put(custID,dueint+"") ;
    	        }
    	        this.debtList.add(debt);
    	            }

    	       rs1.close();
    	       stmt1.close();
    	       conv.close();
    	      } catch (Exception e){
    	        e.printStackTrace();
    	      }
    	/// KLIENDI ANDMETE LEIDMINE

    	     TreeMap tm = new TreeMap();
    	     this.customers = new ArrayList();

   boolean pilot = false ;
   
   if("true".equals(getParent().getProps().getProperty("pilot"))){
	   pilot = true;
   }
    	   String sql =  "  SELECT k.[PAV] AS nimi  ,k.[KOD] AS kood,k.PARENT_KOD AS payer,fulladdress AS aadress ,adres  as addr2  " + 
    	"   	,k.TN AS agent ,  '' AS blokk,  " +
    	"  MOKTERM AS payment,'' AS alekood, KREDLIM AS krediit,'' AS visit,'' AS type,  " +
    	"	   0 AS extradays ,''  AS sort,'' AS debt , 0 AS minsum ,k.TEL  as kontakt , '' as sendall,k.KLKOD as grupp " +
    	" ,k.KLKOD as alegr ,'' as alegr2	,'' as fixed ,'' as agent2 , imkod as regno ,PVMMK as vatno , " +
    	" ( select x.adres from dba.soko x where x.[KOD]=k.[KOD]) as jurid ,nordok , nor_nuo ,[StreetText] as street ,[CityText] as city " +
		" ,r.pav as chain,g.pav as type2 FROM dba.soko k " +
		" left join dba.sokogrup g on g.KOD  = k.grp_KOD " +
		" left join  dba.reg_det r on r.KOD = user_PriklausomTinklui and  tranzakc = 'PriklausomTinklui' " +
		" LEFT JOIN dba.address a  ON a.AddressId=k.AddressId WHERE k.TN != '' ";
    	   System.out.println(sql);
    	   try {
    	        Connection con = getNewConnection2();
    	        Statement stmt = con.createStatement();
    	        ResultSet rs = stmt.executeQuery(sql);
    	        while(rs.next()){
    	         String name = rs.getString("nimi").trim();
    	        
    	         name = name.replaceAll("\"", " ");
    	           String id = rs.getString("kood").trim();
    	           //System.out.println(line);
    	           Customer customer = new Customer(id);
    	            String payer = null;
    	            customer.restName = name;
    	           if (name.equals("")) name = "<" + id + ">";
    	           customer.setName(Converter.enCodeEst(name));
    	           	payer = rs.getString("payer");
    	           	if(payer != null && payer.trim().length()>0){
    	             customer.setPayer(payer.trim());
    	             }

    	           	
    	           	
    	             String aadress = rs.getString("aadress");
    	             String aadress2 = rs.getString("addr2");
    	             if( aadress != null && aadress.trim().length()>0){
    	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress.trim()),"Lithuania"));
    	               name = name + " " + Converter.enCodeEst(aadress.trim());
    	               if(!pilot)
    	              customer.setName(Converter.enCodeEst(name));
    	             }else if( aadress2 != null && aadress2.trim().length()>0){
      	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress2.trim()),"Lithuania"));
      	            // customer.setJuridics(Converter.enCodeEst(aadress2.trim()));
      	             if(!pilot)
      	               name = name + " " + Converter.enCodeEst(aadress2.trim());
      	              customer.setName(Converter.enCodeEst(name));
      	             }
    	         	String age = rs.getString("agent");
    	             
    	             if (age != null) {
    	              customer.addAgent( new Agent( age.trim()));
    	              customer.addAgent( new Agent( "WARE"));
    	              customer.setMainAgent( age.trim());
    	              if (  getAgents1().get( age.trim()) != null){
    	                           Agent aa = (Agent) getAgents1().get( age.trim());
    	                     
    	                            Vector vec = aa.getDubbler();
    	                            if( vec!= null && vec.size() >0){
    	                                 Enumeration en = vec.elements();
    	                                 while( en.hasMoreElements()){
    	                                      String ag = (String) en.nextElement();
    	                                       customer.addAgent( new Agent( ag));
    	                                }
    	                           }
    	                     }
    	             }


    	             if(rs.getString("blokk").equals("1")){
    	               customer.setSalesBlock(true);
    	             }

    	            String paym = rs.getString("payment");

    	           	String num = rs.getString("alekood");
    	           	if(num != null && num.trim().length()>0){
    	             customer.setDiscount(Double.parseDouble((num.trim())));
    	           	}
    	           	
    	           	int kred = rs.getInt("krediit");
    	           	if(kred>0){
    	             customer.setCredit(kred + "");
    	           	}
    	           	
    	
    	        	String visit = rs.getString("visit");
   	             if(visit != null && visit.trim().length()>0 && !visit.trim().toLowerCase().equals("x")){
   	                customer.setVisit(this.getVisitDay(visit.trim()));
   	             }

   	          if(customer.getPayer().equals("LTVPMARK0087") || //maxima
     	        		customer.getPayer().equals("LTPRISMA0001") || //prisma
     	        		customer.getPayer().equals("LTSANITE0005") || // sanites
     	        		customer.getPayer().equals("LTRIMLIE0001") || // rimi
     	        		customer.getPayer().equals("LTVIREGR0001") || //amber
     	        		customer.getPayer().equals("LTFREMAR0001") || // freshma
     	        		customer.getPayer().equals("LTKRISTI0005") || // kristianan
     	        		customer.getPayer().equals("LTEIGINT0001") ||  //eiginta
     	        		customer.getPayer().equals("LTLUKBAL0001") || // lukoil
     	        		customer.getPayer().equals("LTGANBEI0001") || // ganbei
     	        		customer.getPayer().equals("LTLIETSP0001") || // narvesen
     	        		customer.getPayer().equals("LTPALINK0001") || // narvesen
     	        		customer.getPayer().equals("LTNORMAZ0041") ){ // norfa
   	        	  

   	        	  
     	        	customer.setNodebt(true);
     	         }
    	      
    	           if (!customer.isNodebt()){
    	         
    	    if(volad.get(customer.getPayer()) != null){
    	    	customer.setDebt(Double.parseDouble(volad.get(customer.getPayer())+ ""));

    	    }
    	    
    	    
    	           }
    	          
    	           if(paym != null && paym.trim().length()>0){
    	        	    customer.setPaymTerm(paym);
    	             }

    	           
    	           
    	          //
    	           String cont = rs.getString("kontakt");
    	           if(cont != null && cont.trim().length()>0){
    	               customer.setPersonalData(cont.trim());   
    	           }
    	           
    	           String all = rs.getString("sendall");
    	           if("1".equals(all)){
    	        	   Enumeration enums = getParent().getAgentsHash().elements();
    	               while (enums.hasMoreElements()) {
    	                 Agent a = (Agent) enums.nextElement();
    	                 customer.addAgent(a);
    	               }
    	               customer.setMainAgent(" ");
    	           }
    	           String cat = rs.getString("grupp");
    	           if(cat != null && cat.trim().length()>0){
    	             customer.setAnkeediTyyp(cat);
    	           }
    	           
    	           cc.addQuestiosToCustomer(customer,kyssad);    	          

    	           String alegr = rs.getString("alegr");
    	           if(alegr != null && alegr.trim().length()>0){
    	        	customer.setGroup(Converter.cleanLitLatEst(alegr.trim()));   
    	           }
    	           
    	           String alegr2 = rs.getString("alegr2");
    	           if(alegr2 != null && alegr2.trim().length()>0){
    	       
    	        	if(customer.getGroup().trim().length() >0){
    	        	//customer.setCustType(Converter.cleanLitLatEst(alegr.trim()));
    	        	 	customer.setGroup(Converter.cleanLitLatEst(alegr2.trim())); 	
    	        	}else{
    	        	 	customer.setGroup(Converter.cleanLitLatEst(alegr2.trim())); 
    	        	}
    	           }
    	           
    	           
    	        	
     	          String gruppx = rs.getString("fixed");
     	         if(gruppx != null && gruppx.trim().length() != 0 ){
      	        	   Vector v = new Vector();
      	        	   v.add(gruppx.trim());
      	           	customer.setFixSortCode(v);
      	           }
     	        
 	           
 	          String regno  = rs.getString("regno");
	           if(regno != null && regno.trim().length() > 2 ){
	             customer.setRegnr(regno.trim());
	           }
	           String vatno  = rs.getString("vatno");
	           if(vatno != null && vatno.trim().length() > 2 ){
	             customer.setVatnr(vatno.trim());
	           }
	           
	           String aadrjur = rs.getString("jurid");
               if(aadrjur != null && aadrjur.trim().length()>0){
                 customer.setJuridics(Converter.enCodeEst(aadrjur.trim()));
               }
               String nordok = rs.getString("nordok");
               if(nordok != null && nordok.trim().length()>0){
            	   String norduo = rs.getString("nor_nuo");
            	   if(norduo != null){
            		   norduo = norduo.substring(0,10);
            	   }else{
            		   norduo = "";
            	   }
            	   customer.setLicence(nordok + " " + norduo);
            	   
               }
               String street = rs.getString("street");
               if(street != null && street.trim().length()>0){
            	   customer.restStreet = street.trim();
               }
               String city = rs.getString("city");
               if(city != null && city.trim().length()>0){
            	   customer.restCity = city.trim();
               }
               
               String chain = rs.getString("chain");
               if(chain != null && chain.trim().length()>0){
            	   customer.setChain(chain.trim());
               }
               

               String type = rs.getString("type2");
               if(type != null && type.trim().length()>0){
            	   customer.category = (type.trim());
               }
               
               
               customer.setCountry("Lithuania");
               
    	           if(customer.hasAgents()){
    	        	   tm.put(customer.getName(), customer);
       	            this.customers.add(customer.getID());
       	           }
    	           
    	         }

    	        rs.close();
    	        stmt.close();
    	        con.close();

    	        if(rcompany != null){
    		  //  	Converter.sendCustomerJson(tm, rcompany,false,user,pass);
    		    }

    	        Customer cust = new Customer("SKWAREHOUSE1");
   	         cust.setName("Skonis ir kvapas įrangos sandėlis");
   	        Enumeration enume = getParent().getAgentsHash().elements();
   	          while (enume.hasMoreElements()) {
   	            Agent a = (Agent) enume.nextElement();
   	           cust.addAgent(a);
   	          }
   	            tm.put(cust.getName(), cust);
	            this.customers.add(cust.getID());
   	          
   	       Customer cust2 = new Customer("SKOFFICE1");
	         cust.setName("SK centrinis ofisas");
	        enume = getParent().getAgentsHash().elements();
	          while (enume.hasMoreElements()) {
	            Agent a = (Agent) enume.nextElement();
	             cust2.addAgent(a);
	          }
	            tm.put(cust2.getName(), cust2);
 	            this.customers.add(cust2.getID());
    	        
    	         

    	       
    	        
    	        
    	     } catch (Exception e){
    	       e.printStackTrace();
    	     }
    	     return tm;
    	   }

   public TreeMap getAgents1() {
	   
		return agents1;
	}
   

   private TreeMap agents1 = new TreeMap();
   
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
      TreeMap  ladutr = new TreeMap();
      HashSet laosmyyk = new HashSet();
      addSales(laosmyyk);
      System.out.println();
     try {
   	  
      String cmd = "SELECT KOD AS kood, sum([LIKUTIS]) AS kogus FROM dba.[v_PrekiuLikuciaiList] where " +
				"	PADALINYS in ('0AS101','0AS123','0TS100','0TS114','1AS203','1PS210','1TS200','2AS303','2PS308','2TS300','3AS403','3PS404','3TS400','4XX002','0AS132') " +
	    "  GROUP BY  KOD having sum(LIKUTIS) != 0  ORDER BY KOD";

      
      
      	 Connection con = getNewConnection2();
  	        Statement stmt = con.createStatement();
  	        ResultSet rs = stmt.executeQuery(cmd);
  	        while(rs.next()){
  	             String toode = null;
  	             double seis = 0;;
  	              	toode =rs.getString("kood").trim();
  	              	seis = rs.getDouble("kogus");
  	              	ladutr.put(toode, seis + "");
  	              	if(!laosmyyk.contains(toode)){
  	              	  laosmyyk.add(toode);	
  	              	}
  	              	
  	        }

  	      rs.close();
  	    stmt.close();
  	      con.close();

  	    System.out.println(cmd);
   } catch (Exception e) {
       e.printStackTrace();
   }
   //  getFixedAssortmentData();
 
   
   
   
   TreeMap laotekstid = new TreeMap();
   
   try {
	   	  
	   String cmd = "SELECT KOD AS kood, sum([LIKUTIS]) AS kogus,  left (PADALINYS ,1) as ladu FROM dba.[v_PrekiuLikuciaiList] where  " +
		"	PADALINYS in ( '0BF001','0AS101','0AS132','0PS119','0TS100','1BF002','1AS203','1PS210','1TS200','2BF003','2AS303','2PS308','2TS300','3BF004','3AS403','3PS404','3TS400') " +
       " GROUP BY left (PADALINYS ,1), KOD having sum(LIKUTIS) != 0  ORDER BY KOD";

	      	 Connection con = getNewConnection2();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs = stmt.executeQuery(cmd);
	  	        while(rs.next()){
	  	             String toode = null;
	  	             double seis = 0;
	  	              	toode =rs.getString("kood").trim();
	  	              	seis = rs.getDouble("kogus");
	  	              String  ladu = rs.getString("ladu").trim();
	  	             if(ladu.equals("0")){
	  	            	  ladu = "vil:";
	  	              }
	  	            if(ladu.equals("1")){
	  	            	  ladu = "kau:";
	  	              }
	  	          
	  	            if(ladu.equals("2")){
  	            	  ladu = "kla:";
  	                 }
	  	            
	  	           if(ladu.equals("3")){
  	            	  ladu = "pan:";
  	                }
	  	          
	  	          if(seis != 0){
	  	        	 if(  laotekstid.get(toode) != null){
		  	        	    laotekstid.put(toode,laotekstid.get(toode) + "|" + ladu + seis + "");
		  	           }else{
		  	        	    laotekstid.put(toode, "|" + ladu +  seis + "");
		  	          }  
	  	          }
	  	              	
	  	        }

	  	      rs.close();
	  	    stmt.close();

	  	      
	  	      
	  	    cmd = "SELECT KOD AS kood, sum([LIKUTIS]) AS kogus,  left (PADALINYS ,1) as ladu FROM dba.[v_PrekiuLikuciaiList] where  " +
	  	  		"	PADALINYS in ( '0AS132') " +
	  	         " GROUP BY left (PADALINYS ,1), KOD having sum(LIKUTIS) != 0  ORDER BY KOD";

	  	  	  	          stmt = con.createStatement();
	  	  	  	          rs = stmt.executeQuery(cmd);
	  	  	  	        while(rs.next()){
	  	  	  	             String toode = null;
	  	  	  	             double seis = 0;
	  	  	  	              	toode =rs.getString("kood").trim();
	  	  	  	              	seis = rs.getDouble("kogus");
	  	  	  	              String  ladu = rs.getString("ladu").trim();
	  	  	  	            if(ladu.equals("0")){
	  	  	  	            	  ladu = "santa:";
	  	  	  	              }
	  	  	  	             
	  	  	  	          
	  	  	  	          if(seis != 0){
	  	  	  	        	 if(  laotekstid.get(toode) != null){
	  	  		  	        	    laotekstid.put(toode,laotekstid.get(toode) + "|" + ladu + seis + "");
	  	  		  	           }else{
	  	  		  	        	    laotekstid.put(toode, "|" + ladu +  seis + "");
	  	  		  	          }  
	  	  	  	          }
	  	  	  	              	
	  	  	  	        }

	  	  	  	      rs.close();
	  	  	  	    stmt.close();
	  	  	  	      con.close();
	  	      
	  	      
	  	      
	  	  //  System.out.println(cmd + " \n" + laotekstid.toString());
	  	  
	   } catch (Exception e) {
	       e.printStackTrace();
	   }
   
   
  
     try {
     	String cmd = " SELECT  distinct t.KOD AS toode,t.PAV AS nimi, t.RK3 AS myygihind,R0 AS grupp,t.GRP_KOD AS grupp2  " +
		" ,t.VNT AS unit, (case when BARKOD != '' then BARKOD else  user_EANKodasDidmenai end) AS ean,'' AS status ,0 AS maxdiscount, " +
		"	 '' AS type, PKIEKIS AS kastis,  NETO AS kaal, MOKEST AS vat  ,  RK1 as jaehind  " +
		"	 , t.R1 as goes ,t.R5 as newprod ,t.r1 ,sutart FROM dba.[son] t WHERE  " +
		"	   t.R1='DID'  ORDER BY t.[KOD]"; 
     	
      System.out.println(cmd);
      
      this.getProdGroups();
     	Connection con = getNewConnection2();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode").trim();
            Product prod = new Product(id);
         	String name = rs.getString("nimi").trim();
         	name =   Converter.firstToUpperCase(name ); //+ " " + id.substring(0,6)
             double price = rs.getDouble("myygihind");
                prod.setPrice(price);
              if(ladutr.get(id) != null ){
            	  double aa = new Double((String)ladutr.get(id)).doubleValue();
            	  if(aa < 1){
            		  prod.setStock(1);
            	  }else{
            		  prod.setStock(aa);
            	  }
            	 
              }
         
              
              if(laotekstid.get(id) != null ){
            	  prod.setPartii(laotekstid.get(id) + "");
              }
              
              String grupp = rs.getString("grupp");
                
                if(grupp != null && grupp.trim().length()>0 ){
                		 prod.setGroup(grupp.trim());
                 }
              	

                String grupp2 = rs.getString("grupp2");
                  
                  if(grupp2 != null && grupp2.trim().length()>0 ){
                	  String gr = grupp2.trim();
                	  if(gr.length()> 3){
                		 // gr = gr.substring(0, 3);
                	  }
                  		// prod.setGroup(gr);
                   }
                
                
                prod.setUnit(rs.getString("unit"));
              String ean = (rs.getString("ean"));
                 if (ean != null &&ean.trim().length()>3){
                   prod.setEAN(ean.trim());
                //   name = name + " " + ean.substring(ean.trim().length()-3);
                 }
     

             	String discou = rs.getString("maxdiscount");
             	if(discou != null && discou.trim().length()>0 && !discou.trim().equals("0")){
                prod.setMaxDiscount(Double.parseDouble(discou));
                }

                double	cases = rs.getDouble("kastis");
                	if(cases>0){
                      prod.setInCases(cases);
                	}



                 	 String vat = rs.getString("vat");
                 	 
                 	if(vat != null && vat.trim().length()>0 && !vat.trim().equals("0")){
                        prod.setVat(vat.trim());
                        }

                          double	shelf = rs.getDouble("jaehind");
                          	if(shelf>0){
                          	  prod.setPriceSecond(shelf);
                          	}
         
                  
                    String newprod = rs.getString("newprod");
                    	if(newprod != null && newprod.startsWith("NA")){
                    		prod.setStatusNew("1");
                    	}
                          	
                   String add = "";
                   if(id.length() > 5){
                	   add = id.substring(0,5);
                   }
                   
                   String christ = rs.getString("sutart");
               	if(christ != null && christ.startsWith("KALEDOS")){
               		prod.setStatusTop("5");
               	}
                 	
                 if(laosmyyk.contains(id) ){
                	 String ee = "";
                	 if (ean != null &&ean.trim().length()>3){
                         ee = " " + ean.substring(ean.trim().length()-3);
                       }
                	 prod.setName(Converter.enCodeEst(name) ); //+ ee
                     tm.put(add + name, prod); 
                 }
          
        }
  
        rs.close();
        stmt.close();
        con.close();
     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }

 
   
   private void addSales(HashSet laosmyyk) {
	   try {
		    String cmd =  "SELECT  PREKE AS toode  FROM dba.sooprp INNER JOIN   dba.spdok ON dba.sooprp.SUNIKAL = dba.spdok.UNIKAL " +
						"		 inner join dba.soko on dba.soko.KOD = GAVEJ " +
						"		   WHERE TN != '' and [DOK_DAT]> GETDATE()-180 GROUP BY PREKE ";
		    System.out.println(cmd);
		 
		    Connection con = getNewConnection2();
		    Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery(cmd);
		    while(rs.next()){

           String prodID = rs.getString("toode").trim();
           if(!laosmyyk.contains(prodID)){
        	   laosmyyk.add(prodID);              
           }
           }
		         rs.close();
		         stmt.close();
		         con.close();
	   }catch (Exception j){
		   j.printStackTrace();
	   }


	
}

   private String getExcelSortDir(){
	      String dir = getParent().getProps().getProperty("excel.dirsort");
	      if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	      return dir;
	    }
   
   

   
	public static void saveObject(Object obj, String file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
		oos.writeObject(obj);
		oos.close();
	}
   
   public static String deCode(String res ){

	  	return res;
	  }
	 


   public String getAssortmentStr(Agent agent){
    StringBuffer sb = new StringBuffer();
   /*  String cmd = " SELECT [No_] AS toode ,[Type] AS type FROM  [UAB Vilandra$Item] " +
     		" WHERE len([Type])>0  ORDER BY  [Search Description]";
    try {
     	 Connection con = getNewConnection2();
     	System.out.println(cmd);
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
        String id = rs.getString("toode");
        String type = rs.getString("type");
			//ABCD
			int i = type.trim().length();
			if(i>0){
			while (i != 0){
				sb.append(" ; ;" + type.substring(i-1,i) + ";" + id + "\n");
				i = i -1;
			 }
			}
	       }

	        rs.close();
	        stmt.close();
	        con.close();
       } catch (Exception e){
      e.printStackTrace();
    }*/
    return sb.toString();
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     HashMap kliendid = new HashMap();
     try {
    String cmd =  " SELECT  case when [GAVEJ] is null then   dba.spdok.MOK else GAVEJ end AS klient,PREKE AS toode,KIEK AS kogus, [DOK_DAT] AS lastdate, " +
					"	 0 as lastamount,SAV_S as lastprice FROM dba.sooprp INNER JOIN   dba.spdok ON dba.sooprp.SUNIKAL = dba.spdok.UNIKAL " +
				"		 inner join dba.soko on dba.soko.KOD = GAVEJ " +
				"		 INNER JOIN dba.son on dba.son.KOD= PREKE " +
				"		   WHERE TN != '' and [DOK_DAT]> GETDATE()-400 ORDER BY  case when [GAVEJ] is null then   dba.spdok.MOK else GAVEJ end, " +
				"		   dba.son.pav, PREKE,[DOK_DAT]";
    System.out.println(cmd);
   //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"
    Connection con = getNewConnection2();
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery(cmd);
    while(rs.next()){

             String custID = rs.getString("klient").trim();

                 String prodID = rs.getString("toode").trim();;

                 double amount = 0;
                 String lastdate = " ";
                 amount = rs.getDouble("kogus");
                 lastdate = rs.getString("lastdate").trim().substring(5,10);
                    Customer cust = (Customer)kliendid.get(custID);
                    if (cust == null){
                      cust = new Customer(custID);
                    }
                    
                    
                    double lastprice = rs.getDouble("lastprice");
               
                    
                    Product prod = new Product(prodID);
                    CustProd cp = new CustProd(custID, prodID,amount , 0, 0,lastdate,lastprice,0,0," ");

                 cust.addProduct(prod,cp,false);
                 kliendid.put(custID, cust);
                 vec.addElement(cp);

           }
         rs.close();
         stmt.close();
         con.close();


           int counter = 0;
           int cou = 0;
          String oldklient = null;
          String olddate = " ";
          String oldtoode = null;

          String cmd2 =  "  SELECT  case when [GAVEJ] is null then   dba.spdok.MOK else GAVEJ end AS klient,PREKE AS toode,KIEK AS kogus, [DOK_DAT] AS lastdate,  " +
						" 0 as lastamount,SAV_S as lastprice FROM dba.sooprp INNER JOIN   dba.spdok ON dba.sooprp.SUNIKAL = dba.spdok.UNIKAL  " +
						" inner join dba.soko on dba.soko.KOD = GAVEJ " +
						"   WHERE TN != '' and [DOK_DAT]> GETDATE()-30 ORDER BY  case when [GAVEJ] is null then   dba.spdok.MOK else GAVEJ end, " +
						"    PREKE,[DOK_DAT]desc ";
          System.out.println(cmd2);
		   //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"
		    Connection con2 = getNewConnection2();
		    Statement stmt2 = con2.createStatement();
		    ResultSet rs2 = stmt2.executeQuery(cmd2);


  while (rs2.next()){
       String klient = rs2.getString("klient").trim();
               String toode = rs2.getString("toode").trim();
               String kogus = "0";
               String date = " ";
                  kogus = rs2.getDouble("kogus")+"";
                  kogus = kogus.replace('.',',');
				  if(kogus.indexOf(",") != -1){
				  	kogus=kogus.substring(0,kogus.indexOf(","));
				  }
                   date = rs2.getString("lastdate").trim().substring(0,10);

               if(cou == 0){oldklient = klient;olddate = date;oldtoode=toode;}
               if(!oldklient.equals(klient)){
                             counter = 0;
                             oldklient = klient;
                             olddate = date;
               }
               if(!toode.equals(oldtoode)){
               	counter = 0;
               }
               Customer c = (Customer)kliendid.get(klient);
               if (c != null ){

                 ArrayList al = (ArrayList)c.getProducts().get(toode);
                 if (al != null){
                   CustProd cp = (CustProd)al.get(1);
                   if (cp != null){
                    if(!olddate.equals(date)){counter++;}
                     if (counter == 0){cp.setS1(kogus );}
                     if (counter == 1){cp.setS2(kogus );}
                     if (counter == 2){cp.setS3(kogus );}
                     if (counter == 3){cp.setS4(kogus );}
                     if (counter == 4){cp.setS5(kogus );}

                     vec.add(cp);
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

     } catch (Exception e){
       e.printStackTrace();
     }
     return vec;
   }




   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "LabbisSkonis";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }


   public double getSalary(String agentCode){
     return 0;
   }


   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private Vector debtList = new Vector();
   private String url = "jdbc:odbc:report";
   private String url2 = "jdbc:odbc:merisoft";
   private NumberFormat NF = NumberFormat.getInstance(Locale.ENGLISH);
   private TreeMap debtDays = new TreeMap();
 
   public NumberFormat getNF() {
	return NF;
}
/**
 * @param nf The nF to set.
 */
public void setNF(NumberFormat nf) {
	NF = nf;
}



  /**
   * setBulkSales
   *
   * @param agents Map
   */

  public void setBulkSales(Map agents) {
   

   int mm = Calendar.getInstance().get(Calendar.MONTH)+ 1;
   int yy = Calendar.getInstance().get(Calendar.YEAR);
   int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
   int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
   double planprec = dd/d2;
   
  	 try {
  		String cmd = "SELECT    soko.tn as agent,  sum(KIEK*SAV_S)as summ " +
           " FROM  dba.sooprp  INNER JOIN dba.spdok ON dba.sooprp.SUNIKAL = dba.spdok.UNIKAL "+
           " INNER JOIN dba.soko ON dba.soko.KOD = spdok.GAVEJ WHERE " +
           " spdok.POZ = '' AND spdok.OP_T = 'R' AND spdok.POZ1 = 'A' AND spdok.GRAZ = 0 AND soko.TN != ''  and Year ([DOK_DAT]) = Year(Getdate()) " +
           " AND month (DOK_DAT) = Month(Getdate()) group by soko.TN ";
  	 	
  	  Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
            
                 String agentID = rs.getString("agent");
                 double sales = rs.getDouble("summ");
                 Agent agent = (Agent)agents.get(agentID);
                 if (agent != null){
                	
                	  agent.setMonthSales((int)(sales));
                   
                /*   String asi = "Pardavimai: " + NF.format(sales) + " Lt||";
                   if(dd > 6){
                 	  asi = asi + "Plano vykdymas/prognoz� (Lt):";
                       asi = asi + "     " +NF.format(tehtudsalperc*100) + "% / " + NF.format(prognosalperc*100)  + " %||";  
                   }
                   agent.setStart( Converter.enCodeEst(asi ));*/
                   
            }
          }

	        rs.close();
	        con.close();
      } catch (Exception e){
        e.printStackTrace();
     }

  }
}
