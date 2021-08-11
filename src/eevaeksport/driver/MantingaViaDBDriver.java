/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Product;


/**
 * Title:        MobileJava
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class MantingaViaDBDriver extends AbstractDBDriver implements BulkUpdater {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public MantingaViaDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");

        // String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

        try {
            Class.forName(databaseDriverName).newInstance();

        } catch (Exception e) {
            parent.showError(e);
        }
    }

    public String getSaveDebtInfo() {
    	
    	    	return null;
    	       }

    
    
   
    
    
           
    public String getSaveStockInfo(){
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

                public void setBulkSales(Map agents){
              	  connectionUp();
          		String cmd =  " SELECT sum(rid.kogus*(rid.hind))AS sales, " +
          		 " dok.pais2obj  AS agent FROM dok " +
                 " INNER JOIN rid ON dok.doktyyp+STR(dok.dokumnr,7)=rid.doktyyp+STR(rid.dokumnr,7)  INNER JOIN yksus y ON y.yksus = dok.pais2obj " +
                 "  WHERE  len(trim(dok.klient)) > 1  AND YEAR(dok.kuupaev)=YEAR(date()) " +
                 "  AND MONTH(dok.kuupaev)=MONTH(date()) AND y.klassif=2 AND (dok.doktyyp = 'G' OR dok.doktyyp = 'Y')" +
                 "  AND (rid.doktyyp ='G' or rid.doktyyp ='Y') " +
          		 "  AND dok.yksus='1' AND len(trim(rid.toode))>2  GROUP BY  dok.pais2obj" ;
          	
          		log(cmd);
          		
          		try {
          			
          			Statement stmt = con.createStatement();		
          		
          			
          			ResultSet	      rs = stmt.executeQuery(cmd);
          			while(rs.next()) {
          				String agentID = rs.getString("agent").trim();
          				Agent agent = (Agent)agents.get(agentID);
          				if (agent != null){
          					agent.setMonthSales((int)rs.getDouble("sales"));
          				}
          			}
          			rs.close();
          			stmt.close();
          			con.close();
          		} catch (Exception e) {
          			getParent().showError(e);
          		}
          		connectionDown();
          		
          	}
           
                                                
    /**
     *
     */
                public void connectionUp(){
                    if (con == null){
              	try {
                         con = DriverManager.getConnection(url,"mobilejava","mobilejava");
                         stmt = con.createStatement();
              	} catch (Exception e) {
              	    getParent().showError(e);
              	}
                    }
                  }

    /**
     *
     */
    public void connectionDown(){
        try {
           stmt.close();
           con.close();
           con = null;
        } catch (Exception e) {
            getParent().showError(e);
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
    public TreeMap getAgents() {
        connectionUp();
        String  cmd = "SELECT yksus,nimetus FROM firma1.yksus2 ";
        TreeMap agents = new TreeMap();

        try {
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              agent.setManager(true);
              String name = rs.getString("nimetus");
              if(name == null){
            	  name = "noname";
            	  agent.setName("noname");
              }else{
            	  agent.setName(name.trim());
              }
             
                agents.put(name, agent);
            }
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        Agent aa = new Agent("HOUSE");
        aa.setName("HOUSE");
        agents.put("HOUSE", aa);
        return agents;
    }

    public TreeMap getCustomers(Agent agent) {
        connectionUp();
        // String cmd = "SELECT k.kood, k.nimi FROM (klient k INNER JOIN omrid ON k.kood = omrid.klient INNER JOIN rid ON rid.dokumnr = omrid.tasudjrk) WHERE omrid.rid2obj='" + agentCode + "' AND (len(rid.toode) > 2) GROUP BY k.kood";
        String cmd =
            "SELECT k.kood, k.nimi,TRIM(k.tanav) + ' ' + trim(k.piirkond) AS aadress,' ' AS ale,k.riik AS visit,email ,k.objekt2 " +
            "AS agent,k.maksja , telefon  FROM klient k where   k.objekt2 !=''  and k.mitteakt=.F. ORDER BY k.nimi " ;

        log(cmd);

        TreeMap customers = new TreeMap(); 
        HashMap custMap2 = new HashMap();
        try {
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Customer cust = new Customer(rs.getString("kood").trim());
            String nimi = rs.getString("nimi").trim();
            cust.setName(nimi);

            String visit = rs.getString("visit");
            if (visit != null){
              cust.setVisit(visit.trim());
            }

            String aadress = rs.getString("aadress") ;
            if (aadress != null){
              cust.setAddress(aadress.trim());
            }
            cust.setDiscount(rs.getDouble("ale"));
            String email = rs.getString("email");
            if (email != null && email.trim().length()>0){
//            cust.setConfirmEmail(email.trim());

            }
            
            String ag = rs.getString("agent");
            if(ag != null && ag.trim().length() >0){
            	cust.addAgent(new Agent (ag.trim()));
            	 Agent aa = (Agent)getParent().getAgentsHash().get(ag);
            	 if(aa != null && aa.getLanguage() != null){
            		 cust.addAgent(new Agent (aa.getLanguage().trim()));
            	 }
            }
           
            
            String payer = rs.getString("maksja");
            if(payer != null && payer.trim().length() >0){
            	cust.setPayer(payer.trim());
            }
            String telefon = rs.getString("telefon");
            if(telefon != null && telefon.trim().length() >0){
            	cust.setPersonalData(telefon.trim().replaceAll(";", ":"));
            }
            
            
                customers.put(nimi, cust);
               // custMap.put(cust.getPayer(), cust);
                custMap2.put(cust.getID(), cust);
            }


            String sql = "SELECT d.klient AS klient, SUM(d.tasumata) AS summa  from dok d  WHERE  " +
              "  date()>d.tasukuup and d.doktyyp='G' AND d.tasumata <> 0 AND len(klient) > 0 GROUP BY d.klient ORDER BY d.klient";
           ResultSet   rst =  stmt.executeQuery(sql);
            while(rst.next()){
              String id = rst.getString("klient").trim();
              double debt = Double.parseDouble(rst.getString("summa").trim());
       
            Iterator iter = custMap2.values().iterator(); 
            //System.out.println("Olemas2 "  + " " + id + " " + debt);
            while (iter.hasNext()){
            	Customer cc = (Customer)iter.next();
            	// 
            	if(cc.getPayer().equals(id) || cc.getID().equals(id)){
            		cc.setDebt(debt);       		
            		//System.out.println("Olemas222 " + cc.getID() + " " + cc.getPayer() + " " + id + " " + debt);
            	}
            }
            	  
              
            }
            rst.close();
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        return customers;
    }


        public String getDistribution() {
          return null;
        }


    public TreeMap getProducts(Agent agent) {
        connectionUp();
    Hashtable dateOut = new Hashtable();

	TreeMap products = new TreeMap();

	try {
   
                  
         String  cmd = "SELECT toode, rusnimetus ,nimetus , kastis as tykke, myygihind, yhik,liik AS grupp,ribakood AS ean,markused FROM toode WHERE " +
         " (myygikood = 'K' OR  myygikood = '5%' ) AND len(trim(toode)) >2 AND (keeld2=.F. or left(toode,2)='RR')  ORDER BY toode";
       if (isDebug()) {
           	System.out.println(cmd);
           	}


          stmt = con.createStatement();
                      ResultSet rs = stmt.executeQuery(cmd);

                      while (rs.next()) {
                          String kood = rs.getString("toode").trim();
                          
                          
                          
                      	String rusnimetus =rs.getString("rusnimetus");
                        String nimetus =rs.getString("nimetus");
                        Product prod = new Product(kood);
                        if(rusnimetus != null && rusnimetus.trim().length()>0){
                        	nimetus = rusnimetus.trim();
                        }
                        
                        if(kood.startsWith("RR")){
                        	nimetus = "ZZZ " + nimetus;
                        }
                        prod.setName(nimetus.trim());
                                             
                          String sql3 = " select top 1(kuupaev),toode , kaskogus-kogus as kokku from rid r  inner join dok d on r.dokumnr=d.dokumnr " +
				             " and r.doktyyp=d.doktyyp  where kuupaev <= date()  and kaskogus!=0 and d.yksus='1' " +
				             "AND toode='"+kood+"' order by kuupaev desc " ;
		
             try{
			    PreparedStatement pstm = con.prepareStatement(sql3);
			    ResultSet rs6 = pstm.executeQuery();
			  
			   if(rs6.next()){
			    prod.setStock(rs6.getDouble("kokku"));
			    }
			}catch (Exception e){
				
			}   
            String sql4 = " select top 1(kuupaev),toode , kaskogus-kogus as kokku from rid r  inner join dok d on r.dokumnr=d.dokumnr " +
            " and r.doktyyp=d.doktyyp  where kuupaev <= date()  and kaskogus!=0 and d.yksus='2' " +
            "AND toode='"+kood+"' order by kuupaev desc " ;

try{
PreparedStatement pstm1 = con.prepareStatement(sql4);
ResultSet rs7 = pstm1.executeQuery();

if(rs7.next()){
 prod.setName(prod.getName() + "|" + (rs7.getDouble("kokku") + "|"));
 nimetus = prod.getName();
}
}catch (Exception e){

}   
		
                          prod.setPrice(rs.getDouble("myygihind"));
                          String grupp = rs.getString("grupp");

                          if(grupp != null && grupp.trim().length()>0){
                          prod.setGroup(grupp.trim());}
                          if(kood.startsWith("RR")){
                        	  prod.setGroup("RRR");
                          }
                          prod.setInCases(rs.getDouble("tykke"));
                          String ean = rs.getString("ean");
                          if (ean != null && ean.trim().length()>0){
                            prod.setEAN(ean.trim());
                          }

                          
                          if(prod.getEAN().trim().length()==0){
                         	 dateOut.put(kood.trim(),kood.trim() + "|" + nimetus.trim() + "|" + prod.getStock());
                         }else{
                         	dateOut.put(prod.getEAN(),kood.trim() + "|" + nimetus.trim() + "|" + prod.getStock());
                         }
                          
                          
                          products.put(nimetus, prod);

                      }
                      
                      
                      try {
            			saveObject(dateOut,"tootedRev.has");
            		} catch (IOException e1) {
            			// TODO Auto-generated catch block
            			e1.printStackTrace();
            		}

        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        return products;
    }
	public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}

    public TreeMap getProdGroups() throws SQLException {
   this.connectionUp();
     TreeMap   tm = new TreeMap();
     String    sql = "SELECT t.liik AS liik , l.artlnimi AS nimi FROM toode t INNER JOIN  " +
                 " ../andmed/artliik l ON l.liik=t.liik GROUP BY t.liik";
     
     ResultSet rs = stmt.executeQuery(sql);

     while (rs.next()) {
       String liik=rs.getString("liik").trim();
       String nimetus=rs.getString("nimi").trim();
       if (liik != ""){
       tm.put(liik,nimetus);
       }
     }
     rs.close();

     tm.put("RRR","Reklaam kaubad");
     
     connectionDown();
     return tm;
 }




    public Vector getCustomerProductsList(String agentCode) {
        connectionUp();
	/*
	 * String cmd = "SELECT omrid.klient,rid.toode FROM omrid INNER JOIN rid ON omrid.tasudjrk = rid.dokumnr WHERE omrid.rid2obj='" +
	 * agentCode + "' AND (len(omrid.klient) > 1) AND (len(rid.toode) > 2) AND omrid.tasudtyyp='G' AND omrid.reakuupaev >= " +
	 * getStartingTime() + " GROUP BY omrid.klient, rid.toode ORDER BY omrid.klient, rid.nimetus";
	 */
	String cmd =
	    "SELECT dok.saaja,rid.toode,dok.kuupaev,rid.kogus FROM dok INNER "
	    + "JOIN rid ON dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode WHERE "
	    + " len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 2 "
	    + "  AND dok.kuupaev >= "
	    + getStartingTime()
	    + " AND  dok.doktyyp = 'G' AND rid.doktyyp= 'G' GROUP BY dok.saaja, rid.toode ORDER BY dok.saaja, toode.toode,dok.kuupaev";

	if (isDebug()) {
	    System.out.println(cmd);
	}

	Vector custProds = new Vector();

	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String kliendikood = rs.getString("saaja").trim();
		String tootekood = rs.getString("toode").trim();
		 String kuupaev = rs.getString("kuupaev");
        double kogus = Double.parseDouble(rs.getString("kogus").trim());
     

                if (kliendikood.length() > 1) {
                  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,kuupaev.substring(5,10),0,0,0," ");
                  custProds.add(cp);
                }
	    }

            rs.close();

	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return custProds;
    }


    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      
      String sql = "SELECT saaja,tasudok AS number,tasukuup AS maksepaev,tasumata, kuupaev,(date()-tasukuup) AS yle FROM " +
                   " dok WHERE  doktyyp='G' AND tasumata<>0  " + // tasukuup<date()+10 AND
                   " ORDER BY saaja ";

      connectionUp();
      ResultSet rs = stmt.executeQuery(sql);
      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("saaja").trim());
        debt.setDoknr(rs.getString("number").trim());
        debt.setDuedate(rs.getDate("maksepaev"));

        debt.setSum(rs.getDouble("tasumata"));
        debt.setDate(rs.getDate("kuupaev"));
        debt.setDue(rs.getString("yle"));
        list.addElement(debt);
      }
      connectionDown();
      return list;
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
        connectionUp();
        double  salary = 0;

        try {

/*	String  sql = "SELECT ajut,tasu,plaan FROM yksus WHERE yksus = '" + agentCode
                      + "'";

        if (debug) {
            System.out.println(sql);
        }

            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                ajut = rs.getBoolean("ajut");
                koef = rs.getDouble("tasu");
                setBudget(rs.getDouble("plaan"));
            }

            rs.close();
*/
           /* sql = "select sum(doksumma) as eelmineaasta  from dok where (month(kuupaev))=(month(date())) and doktyyp='G' and year(date())-1=year(kuupaev)";
            //+ "< (select sum(doksumma) as seeaasta  from dok where (month(kuupaev))=(month(date())) and doktyyp='G' and year(date())=year(kuupaev))";
          if (debug) {
              System.out.println(sql);
          }

            double eelmineaasta = 0;
            double seeaasta = 0;

            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                eelmineaasta = rs.getDouble("eelmineaasta");
            }
            rs.close();


            sql = "select sum(doksumma) as seeaasta  from dok where (month(kuupaev))=(month(date())) and doktyyp='G' and year(date())=year(kuupaev)";
            if (debug) {
                System.out.println(sql);
            }

            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                seeaasta = rs.getDouble("seeaasta");
            }
            rs.close();

            if (eelmineaasta < seeaasta){
              bonus = 3000;
            }

*/	    // k�sioleva m��gimehe selle kuu m��gi leidmine
            String sql =
                "select sum(doksumma) as seekuu  from dok where (month(kuupaev))=(month(date())) and doktyyp='G' AND year(date())=year(kuupaev) AND pais2obj=?";
            if (isDebug()) {
                System.out.println(sql);
            }

            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, agentCode);

            ResultSet rs = pstmt.executeQuery();

            double seekuu = 0;

            if (rs.next()) {
              seekuu = rs.getDouble("seekuu");
              setSales(seekuu);
            }
            rs.close();
            pstmt.close();

        /*	sql = "SELECT MAX(tasukatse) as tasukatse, vahe FROM tasu WHERE myyk <= "
                      + seekuu;
                if (debug) {
                    System.out.println(sql);
                }

                rs = stmt.executeQuery(sql);

                if (rs.next()) {
                    double tasukatse = rs.getDouble("tasukatse");
                    double vahe = rs.getDouble("vahe");

                    if (ajut) {
                        salary = tasukatse+ bonus;
                    } else {
                        salary = tasukatse + vahe;
                    }
                    salary = salary*koef/100;
                }

                rs.close();
      */
        } catch (Exception e) {
            getParent().showError(e);
        }

        salary = Math.round(salary * 100d) / 100d;
        connectionDown();
        return salary;
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
        Calendar cal = Calendar.getInstance();

        // cal.add(Calendar.MONTH, -6);
        cal.add(Calendar.MONTH,
                -Integer.parseInt(getParent().getProps().getProperty("months")));

        int month = cal.get(Calendar.MONTH) + 1;
        String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        String sqlDate = "{^" + day + '-' + month + '-'
                         + cal.get(Calendar.YEAR) + '}';

        return sqlDate;
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
    public String getPilotDayData(Agent agent){
      return null;
    }


    public String getNonMobileOrders(Agent agent){
      connectionUp();

     String sql = " SELECT tasudok AS arve,kuupaev,k.nimi,doksumma AS summa FROM dok d INNER JOIN klient k ON k.kood=d.klient " +
               " WHERE kalktoode!='MobileJava' AND kuupaev > date()-3 AND pais2obj=?";


      StringBuffer sb = new StringBuffer();

        try {
            PreparedStatement st = con.prepareStatement(sql);
            st.setString(1, agent.getID());
            String sep= System.getProperty("line.separator");
            ResultSet	      rs = st.executeQuery();

            sb.append("ARVE    Nimi  \t\t\tKuup�ev\t\tSumma" + sep);
            sb.append("------------------------------------------------------" + sep);
            while(rs.next()){
              sb.append(rs.getString("arve").trim() + "\t");
              sb.append(rs.getString("nimi").trim() + "\t");
              sb.append(rs.getString("kuupaev").trim() + "\t");
              sb.append(rs.getString("summa").trim() + sep);




            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        connectionDown();
        return sb.toString();
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

    public static final String DRIVER_VERSION = "MantingaVia";
    private String	       url = "jdbc:odbc:eeva";
    private Connection	       con = null;
    private Statement	       stmt;
    private String	       subjectFilesTo = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/
