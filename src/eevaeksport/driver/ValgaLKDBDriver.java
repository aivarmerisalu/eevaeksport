/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.DefectiveLineWrapperMeri;
import eevaeksport.report.SupervisorInfoWrapperMeri;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class ValgaLKDBDriver extends AbstractDBDriver implements Manager,Promo, BulkUpdater,Discounter{



    public ValgaLKDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
        try {
            Class.forName(databaseDriverName).newInstance();

        } catch (Exception e) {
            parent.showError(e);
        }
    }

    public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		File f = new File(this.getDirXls() + "sortimendid.xls");
		try {
			
			  String sql = "SELECT sortdate from mj_assortment where  sortdate = current_date() ";
        	  boolean insertassort = true;
        	  Connection con = DriverManager.getConnection(url2);;
  	        Statement stmt = con.createStatement();
  	        
  	        ResultSet rs = stmt.executeQuery(sql);
  	        if(rs.next()){
  	        	insertassort = false;
  	        	//kontrollime kas on vaja insertida 
  	        }
  	        
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("Atria Tooted");
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

		//	 System.out.println("groups " + groups.toString());

			for (int i = 5; i <= sheet.getLastRowNum(); i++) {
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
					for (int a = 2; a < row.getLastCellNum(); a++) {

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
								 if(insertassort){
		                               insertSortLine(mline);   
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
    private String notNull(String res){
    	if(res == null){
    		return "";
    	}else{
    		return res;
    	}
    }

    public void insertSuperVisorSQL(SupervisorInfoWrapper wr){}

    public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
        String sql = "INSERT into supervisor values ("
          + "?, "   //   agent	C(50),
          + "?,"   //   agentname	C(200),
          + "?,"   //   clientid	C(50),
          + "?,"   //   clientname	C(200),
          + "?,"   //	prodid	C(50),
          + "?,"   //   prodname	C(200),agentid agentname custid custname prodid prodname
          + "now() ,"   //	sentdate	D,
         + " '' ,"   //	receivedTime	D,
          + "?,"   //normalpr	N,
          + "?,"   // amoun	N,
          + "?,"   // grouptotal	N,
          + "?,"   // cattotal	N,
          + "?,"   // pertotcat	I,sentdate normalpr amount grouptotal cattotal pertotcat
          + "?,"   // percolcat	I,
          + "?,"   // perpalcat	I,
          + "?,"   // pershecat	I,
          + "?,"   // perrescat	I,
          + "?,"   // isaction	L,
          + "?,"   // ispartner	L,
          + "?,"   // isbrand	L,
          + "?,"   // isgroup	L
          + "?,"   // actionaeva	N,percolcat perpalcat pershecat perrescat isaction ispartner isbrand isgroup actionaeva
          + "?,"   // actionbeva N,
          + "?,"   // actiontota	N,
          + "?,"   // custeval	N,
          + "?,"   // custcomme	C(200),
          + "?,"   // comment	C(200),
          + "?,"   // custagent	C(200),
          + "?,"   // custagentname	C(200),actionbeva actiontota custeval custcomme comment
          + "?,"   // mustbe	I,
          + "?,"   // ideal	I,
          + "?,"   // resstr1	C(200),
          + "?,"   // resstr2	C(200),
          + "?,"   // resstr3	C(200),
          + "?,"   // resint1	I,
          + "?,"   // resint2	I,
	      + "?)" ; //	resint3	L
        //   resstr1 resstr2 resstr3 resint1 resint2 resint3

          try {	Connection c = DriverManager.getConnection(url2);
              PreparedStatement ps = c.prepareStatement(sql);

              for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
            	  eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
                ps.setString(1, notNull(wr.getInfo().getAgentID()));
                ps.setString(2, notNull(wr.getInfo().getAgentName()));
                ps.setString(3, notNull(lw.getLine().getCustID()));
                ps.setString(4, notNull(lw.getLine().getCustName()));
                ps.setString(5,notNull( lw.getLine().getProdID()));
                ps.setString(6,notNull( lw.getLine().getProdName()));
                ps.setDouble(7, lw.getLine().getNormalPrice()*100);
                ps.setDouble(8, lw.getLine().getAmount()*100);
                ps.setDouble(9, 0);
                ps.setDouble(10, lw.getLine().getCategoryTotal());
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
                ps.setString(24, notNull(lw.getLine().getCustComment()));
                ps.setString(25, notNull(lw.getLine().getComment()));
                String ag = "";
                if(lw.getLine().getCustAgentId() != null){
                	ag = lw.getLine().getCustAgentId();
                }
                ps.setString(26,ag );
                
                ag = "";
                if(lw.getLine().getCustAgentName() != null){
                	ag = lw.getLine().getCustAgentName();
                }
                ps.setString(27, ag);
                ps.setInt(28, lw.getLine().getDealNr());
                ps.setInt(29, lw.getLine().getIdealNr());
                
                ag = "";
                if(lw.getLine().getResStr1() != null){
                	ag = lw.getLine().getResStr1();
                }
                
                ps.setString(30, ag);
                
                ag = "";
                if(lw.getLine().getResStr2() != null){
                	ag = lw.getLine().getResStr2();
                }
                
                ps.setString(31, ag);
                ag = "";
                if(lw.getLine().getResStr3() != null){
                	ag = lw.getLine().getResStr3();
                }
                ps.setString(32,ag);
                ps.setInt(33, lw.getLine().getResInt1());
                ps.setInt(34, lw.getLine().getResInt2());
                ps.setInt(35, lw.getLine().getResInt3());
                ps.executeUpdate();
     
                System.out.println( " Sisestan " + lw.getLine().getResInt3());

              }

              ps.close();
          } catch (SQLException e) {
              e.printStackTrace();
          }
        }


    public Vector getPriceMatrixData() {
      Vector lines = new Vector();
      String uus =  " select ' '  as custid,[Firma siffer] AS clgroup,[Kauba siffer] as prodid , ' ' AS prodgroup," +
      		        " [Mahahindlus] as discount, 0  AS price ,[Kehtib kuup�evast] as algus , [Kehtib kuup�evani]as lopp from TVLISA  " +
                    " where len([Kauba siffer]) > 0 and date() between [Kehtib kuup�evast] and [Kehtib kuup�evani] ";

      try {
  	
      	Statement stmt = this.getNewCon().createStatement();
                ResultSet rs = stmt.executeQuery(uus);

                while (rs.next()) {

                        String custID = rs.getString("custid");
                        String clGroup = rs.getString("clgroup");
                        String prodID = rs.getString("prodid");
                        String prodGroup = rs.getString("prodgroup");
                        double discount = rs.getDouble("discount");
                        double price = rs.getDouble("price");
                        String algus =  rs.getString("algus");
        				String lopp =  rs.getString("lopp");
                        MatrixLine mline = new MatrixLine();

                        if (custID != null && custID.trim().length() > 0) {
                                mline.setCustID(custID.trim());
                        }
                        if (mline.getCustID() == null && clGroup != null
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
                        mline.setDiscount(discount);

                        if (price != 0) {
                                mline.setPrice(price);
                        }
                      
                        
                        if(algus != null && algus.trim().length() != 0){
                        mline.setStartDate(algus.substring(0,10));	
                        }                  
        			
        				 if(lopp != null && lopp.trim().length() != 0){
                           mline.setEndDate(lopp.substring(0,10));
                         }
                        
                        
                        
                     //   lines.add(mline);
                }

        } catch (SQLException ex) {

          ex.printStackTrace();
        }


        return lines;
	//return null;
}



   public String getSaveDebtInfo() {
   	return null;
   }

   public String getProductSortingData(){
   	return null;
   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

    public String getAssortmentStr(Agent agent){
    	return null;
    }
            public String getDealerCustomerCSV(String agent){
             return null;
             }


                                                public String  getCustProdDiscounts(Agent agent){
                                                 return null;
                                                 }

        public String getSaveStockInfo(){
        	sendDistributionReport(null);
        	System.exit(0);
        return null;
        }

		
        public String getEmail(String agentCode) {
            Properties props = EksportFrame.loadProps("emails.txt");
          //  System.out.println(props);
            String     email = props.getProperty(agentCode.trim());
            //System.out.println(email + agentCode);
            if (email == null) {
                email = "";
            }

            return email;
        }			
            	
        public String parseFirstEmail(String emails) {
            String		email = "";
            StringTokenizer st =
                new StringTokenizer(emails,";");

            if (st.hasMoreTokens()) {
                email = st.nextToken();
            }

            return email;
        }
    /**
     *
     */
    public void connectionUp(){
    }

    private Connection getNewCon() throws SQLException{
        return DriverManager.getConnection(url, " ", " ");
    }
    private Connection getNewCon2() throws SQLException{
        return DriverManager.getConnection(url2, " ", " ");
      }

    /**
     *
     */
    public void connectionDown(){
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
        Connection con = getNewCon();
      TreeMap   tm = new TreeMap();
      String    sql = "SELECT siffer AS liik , [Nimetus eesti keeles] AS nimetus FROM tvkaubad WHERE " +
                     " nivoo=2 AND aktiivne ='J' AND klass='P' " ;

      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);

      while (rs.next()) {
        String liik = rs.getString("liik").trim();
        String nimi = liik + " " + rs.getString("nimetus").trim();
          tm.put(liik,
                 nimi);
      }
      rs.close();
      st.close();
      con.close();
      return tm;
      }


    public TreeMap getProdGroupsSecond() throws SQLException {

        return null;
        }

        public String getDistribution() {
          return null;
        }

    public TreeMap getAgents() {
      String  cmd = "SELECT    [Vaikimisi agent] as agent FROM tvfirmad " +
	                  " WHERE  LEN([Vaikimisi agent])>0  group BY [Vaikimisi agent]  ";
      getFixedAssortmentData();
      TreeMap agents = new TreeMap();

        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);

            Connection con2= getNewCon();
            Statement stmt2 = con2.createStatement();



            while (rs.next()) {
              Agent agent = new Agent(rs.getString("agent").trim());
              String  cmd2 = "SELECT [Nimetus Eesti keeles] AS nimetus FROM TVFIRMAD k " +
              " WHERE  siffer ='" + agent.getID() + "'";
              ResultSet	      rs2 = stmt2.executeQuery(cmd2);
             // System.out.println(agent.getID());
              if (rs2.next()){
              	String nimi = rs2.getString("nimetus").trim();
              	agent.setName(nimi);
              	// System.out.println(agent.getID() + nimi);
              }


         /*
              String agentAGENTS = getParent().getProps().getProperty(agent.getID());

              if(agentAGENTS != null){
   	         StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
  	  	       while(stag.hasMoreTokens()){
                  agent.addDubbler(stag.nextToken());
      	       }
              }*/

              
              String dubbler = "";//rs.getString("dubbler");
              if(dubbler != null && dubbler.trim().length()>0){
              	StringTokenizer st = new StringTokenizer(dubbler,",");
              	while(st.hasMoreElements()){
              		String ag = st.nextToken().trim();
              		//System.out.println(ag);
              	
              		if(!agent.getID().equals(ag)){
                  		agent.addDubbler(ag);
                  		
                  	//	System.out.println("dubbler added " + agent.getID() + " dub. " + ag);
              		}
              	}
              }
              
              
              if(agent.getName() != null){
              	agents.put(agent.getName(), agent);
                aHash.put(agent.getID(), agent);

              }
                }


            rs.close();
            stmt.close();
            con.close();

        } catch (Exception e) {
            getParent().showError(e);
        }

        Agent aa = new Agent("HOUSE");
        aa.setName("Kontor");
        agents.put(aa.getName(), aa);
        aHash.put(aa.getID(), aa);
       
	       return agents;
	    }


    private void sendDistributionReport(Calendar ccc){ //parameeter on selleks et kui on vaja saata mingi muu p�eva aruannet
  	  
  	  
  	  
  	 //=10379733,10379733
  	  //distribution.email.10379733=aivar@mobilejava.ee
  	  
    	getProducts(null);
    	getCustomers(null);
    	 
    	  //1.Fikseeritud sortimendi salvestamine baasi juhul kui tava andmetega pole tehtud 
    	 this.getFixedAssortmentData();
    	   
  	  String saajad = getParent().getProps().getProperty("distribution.reporters");
  	  
  	  StringTokenizer sto = new StringTokenizer(saajad,",");
  	  while (sto.hasMoreElements()){
  		
  	  String add = "";
  	  String regno = sto.nextToken();
  	  String toThem = getParent().getProps().getProperty("distribution.email." + regno);
  	  
  	  if(ccc != null){
  		  Calendar cal = Calendar.getInstance();
  		  long diff = cal.getTimeInMillis() -ccc.getTimeInMillis();
  		  cal.setTimeInMillis(diff);
  		  add = "-" + cal.get(Calendar.DAY_OF_YEAR);
  	  }
  	  //2. t�naste lugemiste 
  	  // M��rame t�nased kehtivad sortimendid nii suurused kui ka sisu
  	  
  	   String sql1 = "select sortgroup, count(prodid) as number from mj_assortment where " +
           " sortdate =  current_date()" + add  +
             " group by sortgroup";//t�nase sortimendi laiuse leidmine

  	   String sql2 = "select sortgroup ,prodid  from mj_assortment" +
  	   		"  where " +
           "  sortdate = current_date()" + add + " " + 
           " group by sortgroup,prodid";  // t�nase sortimendi leidmine
  	   HashMap sortLen = new HashMap();
  	   HashMap sortIse = new HashMap();
  	   HashMap tnimed = new HashMap();
  	   
  	   try{
  		   Connection con1 = this.getNewCon2();
      	   Statement st = con1.createStatement();
      	   ResultSet rs = st.executeQuery(sql1);
      	   
      	   while(rs.next()){
      		   sortLen.put(rs.getString("sortgroup").trim(),rs.getString("number").trim());
      	   }
      	   
            rs = st.executeQuery(sql2);
      	   
      	   while(rs.next()){
      		   Vector sort = new Vector();
      		   String sortim = rs.getString("sortgroup").trim();
      		   String prodid = rs.getString("prodid").trim();
      		   Product pr = ((Product) prodHash.get(prodid));
      		   
      		 //  System.out.println("toode" + prodid + "+" + pr);
      		   if(pr == null){
      			   pr = new Product(prodid);
      			   pr.setEAN(prodid);
      			   pr.setName(prodid + " nimipuudub");
      		   }
      		   String ean = pr.getEAN();
      		   if(pr.getEAN() == null)ean = "";
      		   String prname = pr.getName();
      		   if(prname == null) prname = "Tundmatu toode";
      		   String prodname = ean + " " + prname;
      		   tnimed.put(prodid, prodname);	
      		   if(sortIse.get(sortim) != null){
      			   sort = (Vector)sortIse.get(sortim);
      			   sort.add(prodid);
      		   }else{
      			   sort.add(prodid);
      			   sortIse.put(sortim,sort);  
      		   }
      		   
      	   }
      	   
      int counter = 0;	   
      	   //L�pes sortimentide sisselugemine
      	   
      	   // vaja leida mitu toodet oli m�rgitud kontrolli ajal kohale 
      	   //ja ,m��rame kes k�is mis kaupluses ja kellaaeg
      	   
      Calendar ccw = Calendar.getInstance();
      ccw.add(Calendar.DAY_OF_MONTH, -7);
String sql3 = "select prodid ,sup.custid,sup.custname, sup.sentdate as date," +
     	" rtrim(sup.agentid) as agid, rtrim(sup.agentname) as agent ,sup.resstr3 as time " +
      " from supervisor sup  " +
      " where  sup.resstr3 >'" + ccw.getTimeInMillis() + "'" + // sup.sentdate > current_date()-8 
      		"order by sup.sentdate desc";
      	   HashMap visited = new HashMap();
      	   HashMap names = new HashMap();
      	   HashMap agen = new HashMap();
      	   HashMap sorti = new HashMap();
      	   HashMap times = new HashMap();
      	   HashMap visitedcoun = new HashMap();
      	   Vector days = new Vector();
             HashMap puuduvad = new HashMap(); 
             rs = st.executeQuery(sql3);
             
           Vector kliendid = (Vector)ketid.get(regno);
           System.out.println("kliendid " + kliendid);

while(rs.next()){
	   Vector sort = new Vector();
	   String prodid = rs.getString("prodid").trim();
	
	   String custid = rs.getString("custid").trim();
	   String sortim = (String)sortimendid.get(custid);
	   String custname = rs.getString("custname").trim();
	   String date = rs.getString("date").trim().substring(0,10);
	   String agid =  rs.getString("agid").trim();
	   String agent = rs.getString("agent").trim();
	   String time = rs.getString("time");
	  // System.out.println("ahaa " + custid );
	 if(kliendid.contains(custid)){
		
	   if(time != null && time.trim().length() != 0){
		   long lll = Long.parseLong(time.trim());
		   Calendar cc = Calendar.getInstance();
		   cc.setTimeInMillis(lll);
		   time = getYYYYpMMpDDpTimeNiceString(cc.getTime());
		   date = time.substring(0,10);
		   if(!days.contains(date)){
				  days.addElement(date);
			  }
	   }else{
		   if(!days.contains(date)){
				  days.addElement(date);
			  }
		   time = date + " 00:00:00";
	   }
	   	
	   agen.put(custid, agid + " " + agent);
	   names.put(custid, custname);
	   sorti.put(custid, sortim);
	   times.put(custid+date, time);

	   if(visitedcoun.get(custid) != null){
		   Vector dddd = (Vector)visitedcoun.get(custid);
		   if(!dddd.contains(date))dddd.add(date);
		   visitedcoun.put(custid,dddd);  
	   }else{
		   Vector dddd = new Vector();
		   dddd.addElement(date);
		   visitedcoun.put(custid,dddd);  
	   }	   

	   
	   if(sortIse.get(sortim) != null){
		   
		   Vector res = (Vector)sortIse.get(sortim);
			   if(puuduvad.get(custid + date) == null){
				   counter++;
            Vector puud = (Vector)res.clone();
                 puud.removeElement(prodid);
				   puuduvad.put(custid + date,puud);    
			   }else{
				   Vector puud = (Vector) puuduvad.get(custid + date);
		           puud.removeElement(prodid);
				   puuduvad.put(custid + date,puud);   
			   }  
		  
	   }
	   if(visited.get(custid+date) != null){
		   sort = (Vector)visited.get(custid+date);
		   sort.add(prodid);
		   visited.put(custid+date,sort);  
	   }else{
		   sort.add(prodid);
		   visited.put(custid+date,sort);  
	   }	   
	   
	 }
	 } 


StringBuffer sb = new StringBuffer();

StringBuffer sbf = new StringBuffer();

Iterator ite = names.keySet().iterator();
int total = 0;
double perc = 0;
while (ite.hasNext()){

	String custid = (String)ite.next();
	
	String sortcode = (String)sorti.get(custid);
	sb.append("Klient :\t" + names.get(custid) +  "\n");
	sb.append("Kontrollis:\t" + agen.get(custid) +  "\n");
	
	sbf.append("Klient :," + names.get(custid) +  "\n");
	sbf.append("Kontrollis:," + agen.get(custid) +  "\n");
	
	
	Enumeration ed =  ((Vector)visitedcoun.get(custid)).elements();
	while(ed.hasMoreElements()){
		total++;
		String date = (String)ed.nextElement();
		sb.append("Aeg :\t\t" + times.get(custid + date).toString().substring(0,19) +  "\n");	
		sbf.append("Aeg :," + times.get(custid + date).toString().substring(0,19) +  "\n");	
	

		double sss = Double.parseDouble((String)sortLen.get(sortcode));
	
		double mmm = Double.parseDouble(((Vector)visited.get(custid+date)).size()+ "");
		double nnn = mmm/sss*100d;	
		int per = (int)(nnn);
		perc = perc + nnn;	
		sb.append("Sortimendis " + (int)sss + " / M��gil " + (int)mmm + " / " + per + "% \n\n");
		sbf.append("Sortimendis, M��gil ,% \n");
		sbf.append((int)sss + "," + (int)mmm + "," + per + "% \n\n\n\n");
	
	}
	    sb.append("\n\n");
	
	

}


//Sortimendi v�rdlus  puuduolevad tooted ja v�ljaspool olevad tooted


StringBuffer lisa = new StringBuffer();
TreeMap tootedPoed = new TreeMap();
StringBuffer lisaf = new StringBuffer();
System.out.println("puuduvad " + puuduvad.keySet());
Iterator itera = puuduvad.keySet().iterator();
boolean first = true ;
while (itera.hasNext()){
	String id = (String)itera.next();
	System.out.println(id);
	String paev = id.substring(id.length() - 10);
   String custid = id.substring(0,id.length() - 10);
   System.out.println(custid + "ee" + paev);
   if(first){
		lisa.append("Lisainfo Puuduste kohta\n");
		lisaf.append("Lisainfo Puuduste kohta\n");
		first = false;
	}
	Vector vvv = (Vector)puuduvad.get(id);
	lisa.append(names.get(custid) + " " + paev + "\nPUUDU: " + vvv.size() + " toodet\n");
	lisaf.append(names.get(custid) + "," + paev + "\nPUUDU:," + vvv.size() + "\n");
	Enumeration enu = vvv.elements();
	while(enu.hasMoreElements()){
		String prodid = (String) enu.nextElement();
		lisa.append("\t" + prodid + " " + tnimed.get(prodid) + "\n");	
		lisaf.append(prodid + "," + tnimed.get(prodid) + "\n");
		if(tootedPoed.get(prodid + " " +  tnimed.get(prodid)) != null){
			Vector kli= (Vector)tootedPoed.get(prodid + " " +  tnimed.get(prodid));
			kli.addElement(names.get(custid));
			tootedPoed.put(prodid + " " +  tnimed.get(prodid), kli);
		}else{
			Vector kli= new Vector();
			kli.addElement(names.get(custid));
			tootedPoed.put(prodid + " " +  tnimed.get(prodid), kli);	
		}
	}
	lisa.append("\n\n");
	lisaf.append("\n\n");
}
	

StringBuffer toodePood = new StringBuffer();

Iterator iteri = tootedPoed.keySet().iterator();
while (iteri.hasNext()){
	String toode = (String)iteri.next();
	toodePood.append(toode + "\n");
	
	Vector vvv = (Vector) 	tootedPoed.get(toode);
	toodePood.append("puudub " + vvv.size() + " poes\n");
	
 Enumeration enumi = 	vvv.elements();
 while (enumi.hasMoreElements()){
	 toodePood.append("\t"  + enumi.nextElement() + "\n");
 }
 toodePood.append("\n\n");
}


if(total == 0 ){
	total = 1;
}
String text = "Kontrollperiood : " + days.toString() + "\nKontrollimise kordade arv  : " + counter 
+"\nKeskmiselt Kauplustes Saadavus " + (int)perc/total + "%\n\n";
String textf = "Kontrollperiood : " + days.toString() + "\nKontrollimise kordade arv  : " + counter 
+"\nKeskmiselt Kauplustes Saadavus " + (int)perc/total + "%\n\n";

System.out.println(text + "\n" + sb.toString() + lisa.toString()+ "\n" + toodePood );


Calendar ctc = Calendar.getInstance();

String filename = "dreports/" +"DR-SLT-" + getYYYYpMMpDDpTimeNiceString(ctc.getTime()).substring(0, 10) + ".csv";  

   File f = new File(filename);
   if (f.exists())f.delete();
     try {
		PrintWriter os = new PrintWriter(new FileWriter(
				filename, true));
		os.write(textf + "\n" + sbf.toString() + "\n" + lisaf.toString());
		os.close();
	  }catch (IOException ex) {
		ex.printStackTrace();
	  }

Email email = new Email(getParent().getString("datasent"));

//email.setNoticeTo(parseSecondEmail(getEmail(agentCode)));
email.setFrom(getParent().getProps().getProperty("from"));
email.setSMTP(getParent().getProps().getProperty("smtphost"));
email.setUser(getParent().getProps().getProperty("smtpuser"));
email.setPass(getParent().getProps().getProperty("smtppass"));
email.setPort(getParent().getProps().getProperty("smtpport"));
email.setSubjectFilesTo("Distributsiooni aruanne - Atria Eesti AS klient " + regno);
email.setBody(text + "\n" + sb.toString() + "\n" + lisa.toString() + "\n"+ toodePood.toString());
email.addAttach(filename);

//iseendale saatmine
String toOwn = getParent().getProps().getProperty("distribution.ownemail");
StringTokenizer stk = new StringTokenizer(toOwn,";");

while(stk.hasMoreElements()){
	System.out.println("Saadan Emaili" + toOwn );
	email.setFilesTo(stk.nextToken());
	email.send();
	}

email.setFilesTo(toThem);
email.send();
      	   
  	   }catch(Exception e){
  		   e.printStackTrace();
  	   }
  	   
  	   
  	  }
  	  System.exit(0);
    }

    private String getYYYYpMMpDDpTimeNiceString(Date dd){
	    StringBuffer sb = new StringBuffer();
	    Calendar cal= Calendar.getInstance();
	    cal.setTime(dd);
	    
	    
	    
	    String hour =   cal.get(Calendar.HOUR_OF_DAY) + "";
	    
	    
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}

		sb.append("" + cal.get(Calendar.YEAR));
		sb.append("-" + month  + "-");
		sb.append(day +" ");
	    
	    String min = String.valueOf(cal.get(Calendar.MINUTE));
	    String sec = String.valueOf(cal.get(Calendar.SECOND));
	     if (hour.length() == 1){
	      sb.append("0");
	    }
	     sb.append(hour);
	     sb.append("_");
	    if (min.length() == 1){
	     sb.append("0");
	   }
	     sb.append(min);
	     sb.append("_");
	     if (sec.length() == 1){
	        sb.append("0");
	      }
	    sb.append(sec);
	    return sb.toString();
	  }


    
    private void  insertSortLine(MatrixLine mline){
		  try{
				
			      String sql = "INSERT INTO mj_assortment values (" +
			         "'" + mline.getCustID() + "'," + 
			         "current_date()," + 
			         "'" + mline.getCliGroup() + "'," + 
			         "'" + mline.getProdID() + "'," + 
			         "0," + 
			         "0,'')" ;
			  
			  	     Connection  con2 = DriverManager.getConnection(url2);;      
				     Statement stmt2 = con2.createStatement();
				     stmt2.executeUpdate(sql);
		  }catch(Exception e){
			  e.printStackTrace();
		  }
	
		  
	  }
    
    
	    public TreeMap getCustomers(Agent agent){
	      String cmd = "SELECT siffer AS kood, [Nimetus Eesti keeles] AS nimi,[M�rkus] as aadress,[Viivise Valem] as mt, kontaktisik AS isik," +
	      		" [Telefon, faks] As per,Kylastuspaevad as visit, " +
	                   " [Vaikimisi agent] as agent, alltyyp,firmatyyp,kauplusekett,Tellimispaevad as tarne FROM tvfirmad " +
	                   " WHERE len([Nimetus Eesti keeles])>0 and len([Vaikimisi agent]) > 0 and (aktsioon < 3 or aktsioon > 3 ) ORDER BY [Nimetus Eesti keeles]";
	      log(cmd);
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        ketid = new HashMap();
        sortimendid = new HashMap();
        this.custprodlist = this.getCustomerProductsList2(null);
        
        File fff = new File(this.getDirXls() + "sortimendid.xls" );
        TreeMap groups = new TreeMap(); 
    	try{
            FileInputStream ff = new FileInputStream(fff);
    		HSSFWorkbook workbook = new HSSFWorkbook((ff));
    		HSSFSheet sheet = workbook.getSheet("Kauplus");
    		HSSFRow row = null;
    		
    		row = sheet.getRow(0);
    	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
    		
    		System.out.println("rownum " + row.getLastCellNum());
    		
    		
    		for(int a = 1 ; a < sheet.getLastRowNum();a++){
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
    		
    		
    
    	 }catch (Exception e){
    		e.printStackTrace();
    	}
        
        
        
    //	 System.out.println("groups to string " + groups.toString());
        
        
        
        
        
        

			
		
        try {
          Connection con = getNewCon();

          Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);
            int counter = 0;
            while (rs.next()) {
              counter++;

                Customer cust = new Customer(rs.getString("kood").trim());
                String nimi = rs.getString("nimi").trim();
                if(nimi.startsWith("AS ") || nimi.startsWith("O� ") ){
                	nimi = nimi.substring(3,nimi.length());
                }
                cust.setName(nimi);
                String add = rs.getString("aadress");
                if (add != null){
                  cust.setAddress(add.trim());
                } else cust.setAddress(" ");


                String mt = rs.getString("mt");
                if (mt != null && mt.trim().length()>0){
                    cust.setPaymTerm(mt.trim());
                        }

                String isik = rs.getString("isik");
                if (isik != null && isik.trim().length()>0){
                    cust.setPersonalData(isik.trim());
                        }

                String per = rs.getString("per");
                if (per != null && per.trim().length()>0){
                    cust.setPersonalData((cust.getPersonalData() + " " + per.trim()).trim());
                        }

                String visit =  rs.getString("visit");
                    if (visit != null && visit.trim().length() != 0){
                cust.setVisit(visit.trim());
                    } else {
                    cust.setVisit(" ");
                  }

                    String ag = rs.getString("agent");
                    
                    String tyyp = rs.getString("firmatyyp");
                    if(tyyp != null  && tyyp.trim().length() > 0){
                    
                    
                    String allgr = rs.getString("alltyyp");
                    if(allgr != null  && allgr.trim().length() > 2 ){
                    //	cust.setName(allgr.substring(0,3) + " " + cust.getName());
                    }
                    
                    
                    if(tyyp.trim().equals("HORECA")){
                    	if(ag!= null && ag.trim().length()>0){
                    		if(ag.trim().length()<3){
                    			ag = ag + "  ";
                    		}
                    		//cust.setName(ag.substring(0,3) + " " + cust.getName());	
                    	}              	
                      }                
                    }
                    
                    
                    
                    
                  
				if(ag!= null && ag.trim().length()>0){
					if(tyyp!= null && tyyp.trim().equals("HORECA")){
						// siia t�sta vahele agendi lisamine
					}
					 cust.addAgent(new Agent(ag.trim()));
					 Agent aa = (Agent)aHash.get(ag.trim());
		                if (aa != null){
		                	if(aa.getDubbler()!= null && aa.getDubbler().size()>0){
		                		for(int i = 0;i<aa.getDubbler().size();i++){
		                			cust.addAgent(new Agent((String)aa.getDubbler().elementAt(i)));	
		                		}
		                	}
		                }
				}
               
                cust.addAgent(new Agent("HOUSE"));
                
    
        
                String kett = rs.getString("kauplusekett");
                if(kett != null  && kett.trim().length() > 2 ){
                	cust.setGroup(kett.trim());
                	if(ketid.get(kett.trim()) != null){
                		Vector vv = (Vector)ketid.get(kett);
                		vv.add(cust.getID());
                		ketid.put(kett.trim(), vv);
                	}else{
                		Vector vv = new Vector();
                		vv.add(cust.getID());
                		ketid.put(kett.trim(), vv);
                	}
                }
                String grp = (String)groups.get(cust.getID());
                if(grp != null){
                //	System.out.println("Siin on grupp olemas "  + grp + " " + cust.getID() );
                	cust.setGroup(grp);
                
                		sortimendid.put(cust.getID(), grp);
                	
                }
                
                String tarne = rs.getString("tarne");
                if(tarne != null  && tarne.trim().length() > 0 ){
                	cust.setCity("tarnep�ev:  " + tarne.trim() );
                	
                }
                /*String agent2 = rs.getString("internetiparool");
                if(agent2 != null && agent2.trim().length()>0){
                cust.addAgent(new Agent(agent2.trim()));
                }
            //    cust.addAgent(new Agent("MARKO"));


                String agentAGENTS = getParent().getProps().getProperty(ag);

                             if(agentAGENTS != null){
                  	         StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
                 	  	       while(stag.hasMoreTokens()){
                                 cust.addAgent(new Agent(stag.nextToken()));
                     	       }
                             }

                String payer = rs.getString("payer");
                if  (payer != null) cust.setPayer(payer.trim());*/
                customers.put(cust.getName(), cust);
                custMap.put(cust.getPayer(), cust);

            }
            rs.close();

            String sql = "SELECT max(Kuup�ev) as dateke,max([Tasumise kuup�ev]) as ma,  a.[Maksja/Saaja] AS klient,a.number,(a.kuup�ev+a.[Viivise valem]) " + 
                 "  AS kuupaev,a.[MD Summa] AS summa,SUM(tas.[Tasutud Summa])AS tasutud " +
                 "  FROM TVMDKP a LEFT JOIN TVMDKK tas on tas.[Keha pvoti]=a.[Keha voti]  WHERE " +
                 "  a.[MD summa]<>0 AND a.kuup�ev>date()-300 AND (a.kuup�ev+a.[Viivise valem])< date() " +
                 "  GROUP BY a.number,a.[Maksja/Saaja],a.[MD Summa],(a.kuup�ev+a.[Viivise valem]) " +
                 "   having 1< (a.[MD Summa] -SUM(tas.[Tasutud Summa])) or max(str([Keha pvoti])) is null " +
                 "  ORDER BY a.[Maksja/Saaja] ";


            rs = stmt.executeQuery(sql);
            while(rs.next()){
            	 java.sql.Date ddd= rs.getDate("dateke");
            	 
              String id = rs.getString("klient").trim();
              double debt = Double.parseDouble(rs.getString("summa").trim());
              double paid = 0;//
              try {
                paid = Double.parseDouble(rs.getString("tasutud").trim());             
              }
              catch (Exception e) {}
              Customer cust = (Customer) custMap.get(id);
              if (cust != null &&   (debt-paid  < 1 || -1 < debt-paid) && sendDebt(cust.getPayer())) {
               
                cust.setDebt(cust.getDebt() + debt - paid);
              }
            }
            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }

        return customers;
    }
  private boolean  sendDebt(String payer){
	  if(payer.equals("1212") ||//etk
			  payer.equals("1768") ||//rimi
			  payer.equals("5000") ||//stock
			  payer.equals("5005") ||//selver
			  payer.equals("5007") ||//abc
			  payer.equals("5023") ||//kaupmees
			  payer.equals("5044") ||//maxima
			  payer.equals("5060") ||//prisma
			  payer.equals("5065") ||//rr
			  payer.equals("5075") ||//kaubamaja
			  payer.equals("5099") ||//aldar
			  payer.equals("5132") ||//elektra
			  payer.equals("1212") ||
			  payer.equals("1212") ||
			  payer.equals("1212") ){
		  return false;
	  }

	return true;  
  }
  
	    private String getDir(){
	        String dir = getParent().getProps().getProperty("readin");
	        if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	        return dir;
	      }


	    private String getDirXls(){
	        String dir = getParent().getProps().getProperty("readxls");
	        if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	        return dir;
	      }


    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "  SELECT  max(Kuup�ev) as dateke,max([Tasumise kuup�ev]) as ma, a.[Maksja/Saaja] AS klient,a.number,(a.kuup�ev+a.[Viivise valem]) " +
                  "  AS kuupaev,a.[MD Summa] AS summa,SUM(tas.[Tasutud Summa])AS tasutud , -(a.kuup�ev+a.[Viivise valem]-date()) AS yle " +
                  " FROM TVMDKP a LEFT JOIN TVMDKK tas on tas.[Keha pvoti]=a.[Keha voti] WHERE " +
                  " a.[MD summa]<>0 AND a.kuup�ev>date()-300 AND (a.kuup�ev+a.[Viivise valem])< date() " +
                  " GROUP BY a.number,a.[Maksja/Saaja],a.[MD Summa],(a.kuup�ev+a.[Viivise valem]) " +
                   " having 1< (a.[MD Summa] -SUM(tas.[Tasutud Summa])) ORDER BY a.[Maksja/Saaja] ";
               //((a.[MD Summa])<>(tas.[Tasutud Summa]) OR a.[Keha voti]=0) AND
      Connection con = getNewCon();
      Statement stmt = con.createStatement();

      ResultSet rs = stmt.executeQuery(sql);

      while(rs.next()){
        Debt debt = new Debt();
        java.sql.Date ddd= rs.getDate("dateke");
   	 java.sql.Date mmm= rs.getDate("ma");
        debt.setCustID(rs.getString("klient").trim());
        debt.setDoknr(rs.getString("number").trim());
        double paid = 0;
        debt.setDuedate(rs.getDate("kuupaev"));

        double sum = Double.parseDouble(rs.getString("summa").trim());
        if(ddd.getYear()+1900 < 2011){
        	sum = sum /15.6466;
        }
        try {
          String Paid = rs.getString("tasutud");
          if (Paid != null) {
            paid = Double.parseDouble(Paid.trim());
            if(mmm.getYear()+1900 < 2011){
            	paid = paid /15.6466;
            } 
          }
        }
        catch (NumberFormatException e) {
        }

        debt.setSum(sum - paid);
        debt.setDue(rs.getString("yle"));
        if ((sum-paid  < 1 || -1 < sum-paid  ) && sendDebt(debt.getCustID()) ) {
          list.addElement(debt);
        }
      }
      rs.close();
      stmt.close();
      con.close();
      return list;
    }

    
    private String hasInExcel(TreeMap groups,String custid){
    	
    	Iterator it = groups.keySet().iterator();
    	while(it.hasNext()){
    		String st = (String)it.next();
    		Vector vec = ( Vector) groups.get(st);
    		if(vec != null && vec.contains(custid)){
    			return st;
    		}
    	}
    	
    	return null;
    	
    }
    
    public TreeMap getProducts(Agent agent){    	   
    	    TreeMap products = new TreeMap();
		    prodHash = new HashMap();
		 
    	    try{
    	    	String cmd = "SELECT h.[Kuni kuup]-h.[Alates kuup],h.[Alates kuup],h.[Kuni kuup], t.siffer AS toode,t.[Nimetus eesti keeles] as nimi,t.�hik as yhik ,t.[V��tkood] as EAN,t.[Tarneaeg p�evades] as maxdays, " + 
                    " h.Hind as hind   ,[2# nivoo siffer] as grupp ,ValmValjVahe as type FROM tvkaubad t , TVMHIND h  WHERE  t.aktiivne='J' and " +
                    " h.Hinnakiri = 1 and date() between h.[Alates kuup] " +
                    "  and h.[Kuni kuup] and h.Kaup=t.siffer order by t.siffer,h.[Kuni kuup]-h.[Alates kuup] desc";
    	    	Connection con = getNewCon();
    	          Statement st = con.createStatement();
    	            ResultSet	      rs = st.executeQuery(cmd);
    	        
    	            while (rs.next()) {
    	            	 String toode = (rs.getString("toode").trim());
  	            	     String name =  (rs.getString("nimi"));	            	
    	            	 String unit = (rs.getString("yhik"));
    	            	 String ean = (rs.getString("ean"));
    	            	 double hind =  rs.getDouble("hind");
    	            	 String maxdays = (rs.getString("maxdays"));
    	            	 String grupp2 = rs.getString("grupp");
    	            	 double type = rs.getDouble("type");
    	            	 Product prod = new Product(toode);
    	            
    	            	 if(grupp2 != null){
       	            	  prod.setGroup(grupp2.trim());    	            		 
    	            	 }
    	            	 

 			        	 if(maxdays != null && maxdays.trim().length()>0 && !maxdays.trim().equals("0") ){
     			        	prod.setPartii("MaxDays " + maxdays.trim());
     			        }
 			        	 String add = "";
 			        	 if(usedprods.contains(toode)){
 			        		 add = " AKT";
 			        	 }
 			       	 String tyyp = "";
 			    	String namee = "   " +grupp2 + tyyp + " " + name.trim() + add;
   			        	if(type > 0 ){
   			        		prod.setType((int)type + "");
   			        		tyyp = " T";
   			        	}
   			        
     			        prod.setName( "   " +grupp2 + tyyp + " " + name.trim() + add);

     			        prod.setPrice(hind);

     			        if(unit != null && unit.trim().length()>0){
     			        	prod.setUnit(unit.trim());
        			        }

     			        if(ean != null && ean.trim().length()>0){
     			        	prod.setEAN(ean.trim());
        			        }

     			       products.put(namee, prod);
	                   prodHash.put(prod.getID(), prod);
 			        	  
    	            }

    	} catch(Exception e) {
    	e.printStackTrace();
    	}
    	System.out.println("tooted suurus " + products.size());
        return products;
    }



    public DealerProduct getDealerProductsList() {


/*

      String cmd = "SELECT siffer AS yksus FROM TVFIRMAD k " +
                       "WHERE nivoo=2 AND klass = 'E' AND len(siffer)<3 AND l�him�rkus='LADU' AND left(nivoosiffer2,1)='L'";


         //System.out.println(cmd);


     DealerProduct dealerProds = new DealerProduct();

     try {
        Connection con = getNewCon();
        Statement st = con.createStatement();
        ResultSet	      rs = st.executeQuery(cmd);



        while (rs.next()) {
             String      yksus = rs.getString("yksus");
             String      stock = "0";
             String     sql = " SELECT LaoSiffer AS yksus,[Kaubasiffer] AS toode, SUM([SaldoPraegu]-reserveeritud)" +
                     "AS stock FROM TVLADU,TVKAUBAD WHERE siffer=kaubasiffer AND aktiivne='J' AND internetikaup=1 AND laosiffer='" + yksus + "' AND " +
                    "int(Aasta) = YEAR(date()) AND len(kaubasiffer)>0 AND len(laosiffer)<3 AND len(kaubahind)>0  "+
                                 "GROUP BY kaubasiffer,laosiffer,[Nimetus eesti keeles] ORDER BY [Nimetus eesti keeles]";


             //System.out.println(sql);
             Statement stm = con.createStatement();
             ResultSet rst = stm.executeQuery(sql);

             while (rst.next()) {
                 dealerProds.add(yksus, rst.getString("toode").trim(),rst.getString("stock")," "," " );
             }


         }
         rs.close();
         st.close();


     } catch (Exception e) {
         getParent().showError(e);
     }

     connectionDown();

     return dealerProds;
*/
    	return null;
        }
    public Vector getCustomerProductsList(String agentCode) {
    	return custprodlist;
    }
    public Vector getCustomerProductsList2(String agentCode) {

      String cmd = " SELECT r.kaup AS toode,d.ostja AS klient ,[Kauba kogus] AS kogus,[ED kuup�ev] as lastdate, " +
        " iif([ED kuup�ev]+[Tarneaeg P�evades]>date(),0,1)  as yle " +
        " FROM tvedkk r , tvedkp d  ,tvkaubad t " +
        " WHERE r.[Keha pvoti] = d.[Keha voti] and  t.siffer = r.kaup and " +
        " [ED kuup�ev]>date()-30  AND len(kaup)>0 GROUP BY kaup,ostja,[Kauba kogus],[ED kuup�ev],[Tarneaeg P�evades] " +
        " ORDER BY ostja, kaup,[ED kuup�ev] ";

        Vector vec = new Vector();
        HashMap kliendid = new HashMap();
        try {
       System.out.println(cmd);
      //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"
       Connection con = getNewCon();
       Statement stmt = con.createStatement();
       ResultSet rs = stmt.executeQuery(cmd);
       while(rs.next()){

                String custID = rs.getString("klient").trim();

                    String prodID = rs.getString("toode").trim();;
               
                    if(!usedprods.contains(prodID)){
                    	usedprods.addElement(prodID);
                    }
                    
                    double amount = 0;
                    String lastdate = " ";
                    amount = rs.getDouble("kogus");
                    lastdate = rs.getString("lastdate").trim().substring(5,10);
                    int yle = rs.getInt("yle");
                       Customer cust = (Customer)kliendid.get(custID);
                       if (cust == null){
                         cust = new Customer(custID);
                       }
                       Product prod = new Product(prodID);
                       CustProd cp = new CustProd(custID, prodID,amount , 0, 0,lastdate,0,0,0," ");
                       cp.setRedline(yle+"");

                    cust.addProduct(prod,cp,false);
                    kliendid.put(custID, cust);
                    vec.addElement(cp);

              }
            rs.close();
            con.close();


              int counter = 0;
              int cou = 0;
             String oldklient = null;
             String olddate = " ";
             String oldtoode = null;

             String cmd2 = "SELECT kaup AS toode,ostja AS klient ,[Kauba kogus] AS kogus,[ED kuup�ev] as lastdate FROM tvedkk r INNER JOIN tvedkp d ON r.[Keha pvoti] = d.[Keha voti] " +
        " WHERE [ED kuup�ev]>date()-30  AND len(kaup)>0  ORDER BY ostja, kaup ,[ED kuup�ev] desc ";


             System.out.println(cmd2);
   		   //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"
   		    Connection con2 = getNewCon();
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

                       //3114;8643
                   
                        if (counter == 0){cp.setS1(kogus );}
                        if (counter == 1){cp.setS2(kogus );}
                        if (counter == 2){cp.setS3(kogus );}
                        if (counter == 3){cp.setS4(kogus );}
                        if (counter == 4){cp.setS5(kogus );}
                        if(!olddate.equals(date)){counter++;}
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


    public String getPilotDayData(Agent agent){
      return null;
    }


    public String getNonMobileOrders(Agent agent){
      return null;
      /*
      connectionUp(url);

      String sql = "SELECT RTRIM(SL01002) AS nimi, ST02009 AS number, OR20024 AS summa FROM ST020100 " +
                   "LEFT JOIN OR200100 ON  OR20001=ST02009 LEFT JOIN SL140100 k ON ST02001=SL14001 " +
                   "LEFT JOIN SL010100 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-3 AND ST02007=? " +
                  "AND LEN(RTRIM(OR20056))=0 GROUP BY ST02009,OR20024,SL01002,SL14003";

      StringBuffer sb = new StringBuffer();

        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, agent.getID());
            String sep= System.getProperty("line.separator");
            ResultSet	      rs = stmt.executeQuery();
            sb.append(getParent().getString("name") + "  \t\t" + getParent().getString("number") +
                      "\t" + getParent().getString("sum") + sep);
            sb.append("-------------------------------------------" + sep);
            while(rs.next()){
              sb.append(rs.getString("nimi").trim() + "\t");
              sb.append(rs.getString("number").trim() + "\t");
              String sum = getNF().format(Double.parseDouble(rs.getString("summa").trim())) ;
              sb.append(sum + sep);
            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        connectionDown();
        return sb.toString();*/
    }

    public void setBulkSales(Map agents){

      String cmd = "SELECT [Vaikimisi agent] AS agent , SUM([Kauba kogus]*m��gihind*kohamaht*(1- mahahindlus/100)) AS summa  FROM tvedkk r ,tvedkp d,tvfirmad f " +
    "  WHERE r.[Keha pvoti] = d.[Keha voti] AND f.siffer=d.ostja AND month([ED kuup�ev])= month(date()) " +
    "   AND year([ED kuup�ev])= year(date()) AND len( [Vaikimisi agent])>0 GROUP BY  [Vaikimisi agent] ORDER BY  [Vaikimisi agent] " ;


      log(cmd);

        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            ResultSet	      rs = stmt.executeQuery(cmd);
            while(rs.next()) {
              String agentID = rs.getString("agent").trim();
              Agent agent = (Agent)agents.get(agentID);
              if (agent != null){
                agent.setMonthSales((int)rs.getDouble("summa"));
              //  System.out.println("bulksales " + agentID + agent.getMonthSales());
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
            return 0;
    }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
      /*String sql = "SELECT ST02020 AS  kogus, SC01071 AS sisu FROM ST020100 INNER JOIN " +
                   "SC010100 ON ST02017=SC01001 WHERE ST02007=?" +
                   " AND YEAR(ST02015)=YEAR(GETDATE()) AND MONTH(ST02015)=MONTH(GETDATE()) AND SC01012=?";
      PreparedStatement pst = con.prepareStatement(sql);
      pst.setString(1, agentCode);
      //Calendar cal = Calendar.getInstance();
      //cal.set(Calendar.DAY_OF_MONTH, 1);
      //pst.setTimestamp(2, new Timestamp(cal.getTime().getTime()));
      pst.setString(2, String.valueOf(nr));
      ResultSet rs = pst.executeQuery();
      double total = 0;
      while(rs.next()){
        double kogus = rs.getDouble("kogus");
        String sisu = rs.getString("sisu");
        if (!sisu.equals("")){
          total += kogus*Double.parseDouble(sisu);
        }
      }
      rs.close();
      pst.close();
     // System.out.println("nr:" + nr + ", total:" + total);
      return total;*/
    }


    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,docsum AS summa,linecount AS ridu,text AS tekst," +
                   "sentlong AS kuupaev FROM dayreport WHERE sentdate>(DATE()-30) ORDER BY clid";
      Connection c = DriverManager.getConnection(url2);
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(sql);


      String sql2 = "SELECT SL01001 AS kood, SL01040 AS aastamyyk,SL01041 AS kuumyyk,SY24003 AS agendinimi, SL01006 AS kontaktisik " +
                    ",SL01011 AS telefon, SL01024 AS maksetin,SL01036 AS ale" +
                    " FROM SL010100 LEFT JOIN SY240100 ON SY24002=SL01035 ORDER BY " +
                    "SL01002";
      connectionUp();
      Connection con = getNewCon();
      Statement st2 = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs2 = st2.executeQuery(sql2);

      StringBuffer sb = new StringBuffer();
      String sep = ";";
      while(rs.next()){
        String cliID = rs.getString("klient").trim();
        sb.append(cliID + sep);
        sb.append(rs.getString("kuupaev").trim() + sep);
        sb.append(rs.getDouble("summa") + sep);
        sb.append(rs.getInt("ridu") + sep);
        String tekst = rs.getString("tekst");
        tekst = tekst.replace('\'', ' ').replace('\"', ' ');
        if (tekst == null) tekst = " ";
        else tekst = tekst.trim();
        sb.append(tekst + sep);

        boolean has = scrollTo(cliID, rs2);
        if (has){
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

      sql = "SELECT OR20003 AS klient,OR20019 AS agent,OR20017 AS agentname FROM OR200100 " +
          "WHERE OR20015>? GROUP BY OR20003,OR20019,OR20017 ORDER BY OR20003";

      PreparedStatement ps = con.prepareStatement(sql);
      ps.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
      rs = ps.executeQuery();
      StringBuffer bf2 = new StringBuffer();
      while(rs.next()){
        bf2.append(rs.getString("klient").trim() + sep);
        bf2.append(rs.getString("agent").trim() + sep);
        String agentName = rs.getString("agentname");
        if (agentName == null) agentName = "";
        bf2.append(agentName + "\n");
      }

      md.setClientAgentInfo(bf2.toString());

      return md;
    }

    private boolean scrollTo(String id, ResultSet rs) throws SQLException{
      rs.beforeFirst();
      while(rs.next()){
        if (id.equals(rs.getString("kood").trim())){
          return true;
        }
      }
      return false;
    }

    private String ensureLen(String str, String replace){
      if (str == null) str = "";
      else str = str.trim();
      if (str.length() == 0){
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
    public String getStartingTime(){
    Calendar cal = Calendar.getInstance();
    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
    if (month.length() == 1) month = "0" + month;
    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    if (day.length() == 1) day = "0" + day;
    return day + month + cal.get(Calendar.YEAR);
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
    
    
    public void insertDefectiveMeri(eevaeksport.report.DefectiveWrapperMeri wr){
    	Vector reasonCH = new Vector();
    	
   	 reasonCH.add("E Etikett puudub");
       reasonCH.add("E Vale info Etiketil");
       reasonCH.add("E Triipkood puudu");
       reasonCH.add("E Realis.aeg puudub");

       reasonCH.add("K Realis.aja �letanud");
       reasonCH.add("K Hapu");
       reasonCH.add("K Alakaaluline");
       reasonCH.add("K Vaakumist v�ljas");
       reasonCH.add("K Real.aeg l�hike");		    
       reasonCH.add("K Kummis");
       reasonCH.add("K Kest katki");
       reasonCH.add("K Ebameeldiva l�hnaga");
       reasonCH.add("K Hallitanud");
       reasonCH.add("K Karp katki");
       reasonCH.add("K Pekine");
       reasonCH.add("K Tootes v��rkehad");
       reasonCH.add("K Palju Vedelikku");

       reasonCH.add("L Kaalu vahe,arvestamatta");
       reasonCH.add("L puudu,valesti komplekt.-tud");
       reasonCH.add("L Kastis kauba kogus vale");
       reasonCH.add("L Kliendini kaup ei j�udnud");
       reasonCH.add("L Valesti saadetud kaup");
       reasonCH.add("L Pakend must");
       
       reasonCH.add("M Vale hind");
       reasonCH.add("M Valesti sisestatud tell.");
       reasonCH.add("M Kampaania tagastus");
       
       String sql = "INSERT INTO MJ_QUALITY VALUES (?,?,?,?,?,?,?," +
       		"?,?,?,now(),?,?,?,?,?)";

           try {
               Connection con = getNewCon2();
                

                 for (Iterator iter = wr.getDefective().getLines().iterator(); iter.hasNext(); ) {
                 	 PreparedStatement ps = con.prepareStatement(sql);

                    ps.setString(1, wr.getDefective().getAgentID());
                    ps.setString(2, wr.getDefective().getAgentName());
                    
                 	DefectiveLineWrapperMeri lw = new DefectiveLineWrapperMeri(iter.next());

                   double kogus = 0;
                   try {
                     kogus = Double.parseDouble(lw.getLine().getAmount());
                   } catch (NumberFormatException nfe){
                   	parent.log(lw.getLine().getAmount() + "\n" +nfe.toString());
                   	}


                   double oldprice = 0;
                   try {
                   	oldprice = Double.parseDouble(lw.getLine().getBestBefore());
                   } catch (NumberFormatException nfe){};

                   double newprice = 0;
                   try {
                   	newprice = Double.parseDouble(lw.getLine().getProdName());
                   } catch (NumberFormatException nfe){};

                   String id = lw.getLine().getCustID();
                   if(id.equals("fake")){
                   	id = "VALI KLIENT";
                   }
                   
                  // System.out.println("cust name '" + id + "'");
                  
                   ps.setString(3, id);
                   
                 //  System.out.println("cust name '" + lw.getLine().getCustName() + "'");
                   String name = lw.getLine().getCustName();
                   if(name.equals("<Vali Klient>")){
                   	name = "VALI KLIENT";
                   }
                   ps.setString(4, name);
                   String idx = lw.getLine().getProdID();
                   String prid = idx.substring(0,idx.indexOf(" "));
                  //System.out.println("ID " +prid);
                  String taga = " ";
                  if(idx.indexOf("|")>-1){
                  	taga = "TAGASTUS";
                  }
                  String prna = idx.substring(idx.indexOf(" "),idx.length());
               //   System.out.println("NAME " + prna);
                   ps.setString(5, prid);
                   ps.setString(6, prna);

                   ps.setDouble(7, kogus*100);
                   String bb = lw.getLine().getBestBefore();
                  // System.out.println(kogus+"bb '"+lw.getLine().getBestBefore()+"'"+bb+"'");
                   ps.setString(8, bb);
                   
                   ps.setString(9, lw.getLine().getDefectText().substring(0,5));
                   ps.setString(10, lw.getLine().getProdType());
                   
                   ps.setString(11, getText(lw.getLine().getDefectText(),reasonCH,true));
                   ps.setString(12, taga);
                   ps.setString(13, getText(lw.getLine().getDefectText(),reasonCH,false));
                   ps.setDouble(14,oldprice*100);
                   ps.setDouble(15,newprice*100);
                   ps.executeUpdate();

           con.commit();
           ps.close();
                 }
                 } catch (SQLException e) {
           e.printStackTrace();
       }
     }

    
    public void insertDefective(DefectiveWrapper wr){}

private String getText(String xx,Vector reason,boolean text){
	Enumeration enums = reason.elements();
	while(enums.hasMoreElements()){
		String yy =  (String)enums.nextElement();
		if(text){
		  if(xx.indexOf(yy)!=-1){
			return xx.substring(0,yy.length());
		  }	
		}else{
			 if(xx.indexOf(yy)!=-1 && xx.length()>yy.length()){
				return xx.substring(yy.length(), xx.length());
			  }		
		}
	}
	return "";
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


       public TreeMap getDealers() {
           return dealers;
       }

       public void setDealers(TreeMap aDealers) {
           dealers = aDealers;
    }
       // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "ValgaLK";
    //private String             url = "jdbc:odbc:scalaDB";
    private String url = "jdbc:odbc:mox";
        //"jdbc:freetds:sqlserver://NAVI-AJALUGU/TestNavi";
    private String             url2 = "jdbc:odbc:report";
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
    private  HashMap prodHash = null;
    private TreeMap	       dealers = new TreeMap();
    private Vector custprodlist = new Vector();
    private Vector usedprods = new Vector();
    private HashMap ketid = null;
    private HashMap sortimendid = null;
    private     HashMap aHash = new HashMap();

}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

