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
import java.util.Enumeration;
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
public class SebaDBDriver extends AbstractDBDriver implements BulkUpdater {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public SebaDBDriver(EksportPanel parent) {
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
          		String cmd =  " select id_ag as agent,sum(suma)as sales from dok where extract(month from data)= extract(month from current_timestamp)-1 " +
             "AND extract(year from data)= extract(year from current_timestamp) and doktip='4' " +
             " GROUP BY id_ag ORDER BY id_ag" ;
          	
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
           con = DriverManager.getConnection(url);
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
        String  cmd = "SELECT id_drb as yksus,pavarde as ees,vardas as nimetus FROM drb WHERE id_pad=2";
        TreeMap agents = new TreeMap();

        try {
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              //agent.setObject(rs.getString("objekt").trim());
              String ees = rs.getString("ees").trim();
              String name = ees + " " + rs.getString("nimetus").trim();
              agent.setName(name);
                agents.put(name, agent);
            }
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        return agents;
    }

    public TreeMap getCustomers(Agent agent) {
        connectionUp();
        // String cmd = "SELECT k.kood, k.nimi FROM (klient k INNER JOIN omrid ON k.kood = omrid.klient INNER JOIN rid ON rid.dokumnr = omrid.tasudjrk) WHERE omrid.rid2obj='" + agentCode + "' AND (len(rid.toode) > 2) GROUP BY k.kood";
        String cmd =
            "SELECT  k.id_kl as klid,s.id_subkl as sid, k.kod as kood , k.pavad as nimi, s.kod as skood , s.adr as subnimi,k.adr AS aadress,' ' AS ale, ' ' AS visit ,k.id_agent   " + 
          "  AS agent,'' as maksja , k.tlf as telefon ,k.term_p as term   " +
		  " FROM kl k  left join subkl s on s.id_kl=k.id_kl where   k.deb='Y' and k.color!=255  ORDER BY k.pavad ";

        log(cmd);

        TreeMap customers = new TreeMap();
        TreeMap custMap2 = new TreeMap();
        try {
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
            	
            	String klid = rs.getString("klid").trim();
            	String paym = klid;
            	String sid= rs.getString("sid");
            	if(sid != null){
            		klid = klid + "-" + sid;
            	}
              Customer cust = new Customer(klid);
            String nimi = rs.getString("nimi").trim();
        
            cust.setPayer(paym);
            String subkod = rs.getString("skood");
            String subadr = rs.getString("subnimi");
            
            if(subkod != null){
            	nimi = nimi + " " + subkod.trim();
            }
            if(subadr != null){
            	nimi = nimi + " " + subadr.trim();
            }            
            
            nimi = nimi.replaceAll("VAISTIN�S", "Vai.");
            nimi = nimi.replaceAll("VAISTIN�", "Vai.");
            nimi = nimi.replaceAll("Kosmetik�", "Kos.");
            nimi = nimi.replaceAll("VAISTINI� CENTRAS", "Vais.Cen.");
            
            nimi = nimi.replaceAll(" UAB", "");
            nimi = nimi.replaceAll("vaistin�", "Vai.");
            nimi = nimi.replaceAll("   ", " ");
            nimi = nimi.replaceAll("  ", " ");
            
            
            cust.setName(nimi);

            
            String aadress = rs.getString("aadress") ;
            if (aadress != null){
              cust.setAddress(aadress.trim());
            }
            
            
            String visit = rs.getString("visit");
            if (visit != null){
              cust.setVisit(visit.trim());
            }
            Enumeration enums = getParent().getAgentsHash().elements();
            while (enums.hasMoreElements()) {
              Agent a = (Agent) enums.nextElement();
              cust.addAgent(a);
            }
          

            cust.setDiscount(rs.getDouble("ale"));
          /*  String email = rs.getString("email");
            if (email != null && email.trim().length()>0){
//            cust.setConfirmEmail(email.trim());

            }
            */
            String ag = rs.getString("agent");
            if(ag != null && ag.trim().length() >0){
            	cust.addAgent(new Agent (ag.trim()));
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
                custMap2.put(klid,cust);
            }

            
            String sql = " SELECT id_kl as klient,sum(skola -apmsum) as summa FROM  " +
                  "  dok WHERE  skola -apmsum !=0 and snd_kod= '1-01Centri' and current_timestamp>(data+term)  " +
                  "   group by id_kl ORDER BY id_kl ";
            
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

	TreeMap products = new TreeMap();

	try {
   
                  
       String  cmd = "SELECT t.kod as toode , t.pavad as nimetus , '' as tykke,t.kn0 as  myygihind, '' as  yhik,g.id_pgr AS grupp,bk.bkod  " +
       	"	AS ean,t.pvm as vat , t.kiekis as laoseis , t.color FROM prk t INNER JOIN pgr g ON g.grupe=t.grupe " +
	"		left join BK bk on bk.id_prk=t.id_prk " +
	"		where t.id_snd = 1   and t.color !=255 ORDER BY t.kod	"; //and  g.grupe like '%pda%'
       if (isDebug()) {
           	System.out.println(cmd);
           	}

//color 8388608
       
          stmt = con.createStatement();
                      ResultSet rs = stmt.executeQuery(cmd);

                      while (rs.next()) {
                          String kood = rs.getString("toode").trim();
                          
                          
                          
                        String nimetus =rs.getString("nimetus");
                        Product prod = new Product(kood);
                       
                        nimetus = nimetus.replaceAll("Sebamed", "SM");
                        nimetus = nimetus.replaceAll("Kleraderm", "KLE");
                        nimetus = nimetus.replaceAll("Lakme", "LAK");
                        nimetus = nimetus.replaceAll("Plauk�", "Pla.");
                        nimetus = nimetus.replaceAll("plauk�", "pla.");
                        nimetus = nimetus.replaceAll("plaukus", "pla.");
                        nimetus = nimetus.replaceAll("Plaukus", "pla.");
                        
                        nimetus = nimetus.replaceAll("Collage", "Col.");
                        nimetus = nimetus.replaceAll("da�ai", "da�.");
                        nimetus = nimetus.replaceAll("�amp�nas", "�am");
                        nimetus = nimetus.replaceAll("�amp�nas", "�am");
                        nimetus = nimetus.replaceAll("balzamas", "bal.");
                        nimetus = nimetus.replaceAll("kremas", "kr.");
                        nimetus = nimetus.replaceAll("Kremas", "Kr.");
                        nimetus = nimetus.replaceAll("Baby K�diki�", "Ba.K�");
                        nimetus = nimetus.replaceAll("Exclusiv", "Ex.");
                        nimetus = nimetus.replaceAll("losjonas", "los.");
                        nimetus = nimetus.replaceAll("emulsija", "em.");
                        nimetus = nimetus.replaceAll("Selvert", "Sel.");
                        nimetus = nimetus.replaceAll("Gloss", "Gl.");
                        nimetus = nimetus.replaceAll("ry�kinimo", "ry�.");
                        nimetus = nimetus.replaceAll("Master", "Mas.");
                        nimetus = nimetus.replaceAll("kondicionierius", "kond.");
                        nimetus = nimetus.replaceAll("Peeling", "Peel.");
                        nimetus = nimetus.replaceAll("Plakatas ", "Plak.");
                        nimetus = nimetus.replaceAll("Repair", "Rep.");
                        nimetus = nimetus.replaceAll("repair", "rep.");
                        nimetus = nimetus.replaceAll("pa�eist�", "pa�.");
                        nimetus = nimetus.replaceAll("Botanic", "Bot.");
                        nimetus = nimetus.replaceAll("Oleades", "Ole.");
                        nimetus = nimetus.replaceAll("Baby Sun Care", "BSC");
                        nimetus = nimetus.replaceAll("Teknia", "Tek");
                        nimetus = nimetus.replaceAll("Sun Care", "SC");
                        nimetus = nimetus.replaceAll("therapy", "the");
                        nimetus = nimetus.replaceAll("dezodorantas", "dezo");
                        nimetus = nimetus.replaceAll("Essential", "Esse");
                        nimetus = nimetus.replaceAll("Glicoderm", "Glic");
                        nimetus = nimetus.replaceAll("Idroderm", "Idro");
                        nimetus = nimetus.replaceAll("Neaderma", "Nead");
                        nimetus = nimetus.replaceAll("Omorfies", "Omor");
                        nimetus = nimetus.replaceAll("Purissima", "Puri");
                        nimetus = nimetus.replaceAll("Thalasso", "Thal");
                        nimetus = nimetus.replaceAll("prie�i�ros rinkinys", "pri.rin"); 
                        nimetus = nimetus.replaceAll("Oksidantas", "Oks"); 
                        nimetus = nimetus.replaceAll("Klerad", "Kle"); 
                        
                        
                        nimetus = nimetus.replaceAll("  ", " ");
                        prod.setName(nimetus.trim());
                                             
                           prod.setPrice(rs.getDouble("myygihind"));
                          String grupp = rs.getString("grupp");

                          if(grupp != null && grupp.trim().length()>0){
                          prod.setGroup(grupp.trim());}
                          prod.setInCases(rs.getDouble("tykke"));
                          String ean = rs.getString("ean");
                          if (ean != null && ean.trim().length()>0){
                            prod.setEAN(ean.trim());
                          }

                           String vat = rs.getString("vat");
                           if (vat != null && vat.trim().length()>0){
                        	   prod.setVat(vat);
                           }
                           
                           double stock = rs.getDouble("laoseis");
                           if(stock>0){
                        	   prod.setStock(stock);
                           }
                         
                          
                          
                          products.put(nimetus, prod);

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
     String    sql = "SELECT id_pgr AS liik , grupe AS nimi FROM pgr gr where grupe != ''";
     
     ResultSet rs = stmt.executeQuery(sql);

     while (rs.next()) {
       String liik=rs.getString("liik").trim();
       String nimetus=rs.getString("nimi").trim();
       if (liik != ""){
       tm.put(liik,nimetus);
       }
     }
     rs.close();
     
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
	    "select a.id_kl as saaja,a.UZS_ID_SUBKL as abi,t.kod as toode,max(a.data) as kuupaev,max(-a.kiekis) as kogus  from apv a inner join prk t on t.id_prk=a.id_prk " +
	      " where a.data>current_timestamp-200 and a.rectip='Y' and a.id_kl is not null and  a.id_kl !=-1 GROUP BY a.id_kl,a.UZS_ID_SUBKL, t.kod ORDER BY a.id_kl, t.kod";

	if (isDebug()) {
	    System.out.println(cmd);
	}

	Vector custProds = new Vector();

	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String kliendikood = rs.getString("saaja").trim();
		String abikood = rs.getString("abi").trim();
		if(!abikood.equals("-1")){
			kliendikood= kliendikood + "-" + abikood;
		}
		String tootekood = rs.getString("toode").trim();
        double kogus = Double.parseDouble(rs.getString("kogus").trim());
        String kuupaev = rs.getString("kuupaev");

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
      
      String sql = "SELECT id_kl as saaja,doknr AS number,data+ term AS maksepaev,skola -apmsum as tasumata, data as kuupaev ,((current_timestamp-data)-term)" +
      		" AS yle FROM  dok WHERE  skola -apmsum !=0 and snd_kod= '1-01Centri'  ORDER BY id_kl ";

      connectionUp();
      ResultSet rs = stmt.executeQuery(sql);
      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("saaja").trim());
        debt.setDoknr(rs.getString("number").trim());
        debt.setDuedate(rs.getDate("maksepaev"));

        debt.setSum(rs.getDouble("tasumata"));
        debt.setDate(rs.getDate("kuupaev"));
         
       debt.setDue((Calendar.getInstance().getTimeInMillis()-debt.getDuedate().getTime()) /86400000 + "");
        list.addElement(debt);
      }
    //  connectionDown();
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

   /*  String sql = " SELECT tasudok AS arve,kuupaev,k.nimi,doksumma AS summa FROM dok d INNER JOIN klient k ON k.kood=d.klient " +
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

        connectionDown();*/
        return null;
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

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "Seba";
    private String	       url = "jdbc:odbc:alfa";
    private Connection	       con = null;
    private Statement	       stmt;
    private String	       subjectFilesTo = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/
