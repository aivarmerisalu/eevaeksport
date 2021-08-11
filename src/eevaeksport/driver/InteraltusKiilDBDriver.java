/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Product;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class InteraltusKiilDBDriver extends AbstractDBDriver  implements BulkUpdater {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public InteraltusKiilDBDriver(EksportPanel parent) {
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
       
    public String getAssortmentStr(Agent agent){
    	return null;
    }
        public String getSaveStockInfo(){
        return null;
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
        String  cmd = "SELECT yksus,nimetus,objekt FROM firma1.yksus2";
        TreeMap agents = new TreeMap();

        try {
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String name = rs.getString("nimetus").trim();
              agent.setObject(rs.getString("objekt").trim());
              
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
        // String cmd = "SELECT k.kood, k.nimi FROM (klient k INNER JOIN omrid ON k.kood = omrid.klient INNER JOIN rid ON rid.dokumnr = omrid.tasudjrk) WHERE omrid.rid2obj='" + agentCode + "' AND (length(rid.toode) > 2) GROUP BY k.kood";
        String cmd =
        	" SELECT k.kood,k.maksja, k.nimi,aadress,regnr, tanav + ' '+ piirkond as addr,postiindek as inex,k.hinnaale AS ale,k.visit," +
        	" k.tarne as abi,k.objekt2  as agent ,k.infomail as partner  "+
            "  FROM firma1.klient k ORDER BY k.nimi";
        log(cmd);

        TreeMap customers = new TreeMap();

        try {
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Customer cust = new Customer(rs.getString("kood").trim());
            String nimi = (rs.getString("nimi").trim()).replaceAll("&", "ja");
            cust.setAddress(rs.getString("addr").trim());
            cust.setRegnr(rs.getString("regnr").trim());
            cust.setZipcode(rs.getString("inex").trim());
            cust.setName((nimi));
          
            cust.setAddress(" ");
                customers.put(nimi, cust);
            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        try {
    		Converter.makeTelemaFiles(this.getOutDir(), null, null, customers, "-");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        
        return customers;
    }



        public String getDistribution() {
          return null;
        }


    public TreeMap getProducts(Agent agent) {
        connectionUp();
      
    
        
        TreeMap products = new TreeMap();

        try {
        


          // String cmd = "SELECT toode, nimetus, yhik, myygihind FROM toode WHERE (length(trim(toode)) > 3) AND (trim(toode.grupp) = 'L')";
          String cmd = "SELECT toode, nimetus, aktsiis as tykke, myygihind, yhik ,liik ,ribakood  FROM firma1.toode WHERE " +
            " length(trim(toode)) >3 and grupp ='L' and left(toode,1) !='.' ORDER BY toode";


          stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery(cmd);

                      while (rs.next()) {
                          String kood = rs.getString("toode").trim();
                        
                          String nimetus =  rs.getString("nimetus").trim() + " " + rs.getString("yhik").trim();
                          Product prod = new Product(kood);
                          prod.setName((nimetus));
                          prod.setPrice(rs.getDouble("myygihind"));
                          prod.setInCases(rs.getDouble("tykke"));
                          prod.setUnit("pdl");
                          prod.setGroup(parent.make(rs.getString("liik")));
                          prod.setEAN(parent.make(rs.getString("ribakood")));
                          products.put(nimetus, prod);

                      }

        } catch (Exception e) {
            getParent().showError(e);
        }
        
        
        try {
    		Converter.makeTelemaFiles(this.getOutDir(), products, this.getProdGroups(), null, "-");
    	} catch (SQLException e) {
    		
    		e.printStackTrace();
    	}

        return products;
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
    public TreeMap getProdGroups() throws SQLException {
    	   this.connectionUp();
    	     TreeMap   tm = new TreeMap();
    	     String    sql = "select k.liik, k.artlnimi from firma1.artliik k where grupp = 'L'";
    	     
    	     ResultSet rs = stmt.executeQuery(sql);

    	     while (rs.next()) {
    	       String liik=rs.getString("liik").trim();
    	       String nimetus= (rs.getString("artlnimi").trim());
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
	 * agentCode + "' AND (length(omrid.klient) > 1) AND (length(rid.toode) > 2) AND omrid.tasudtyyp='G' AND omrid.reakuupaev >= " +
	 * getStartingTime() + " GROUP BY omrid.klient, rid.toode ORDER BY omrid.klient, rid.nimetus";
	 */
	String cmd =
	    "SELECT dok.saaja,rid.toode,dok.kuupaev,rid.kogus FROM firma1.dok INNER "
	    + "JOIN firma1.rid ON dok.dokumnr = rid.dokumnr INNER JOIN firma1.toode on rid.toode = toode.toode "
	    + "INNER JOIN firma1.klient k on k.kood= dok.saaja WHERE "
	    + " length(trim(dok.klient)) > 1 AND length(trim(rid.toode)) > 3 "
	    + " AND (dok.pais2obj = '" + agentCode + "' or k.tarne ='" + agentCode + "') AND dok.kuupaev >= current_date-100 "
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
      String sql = "SELECT klient,tasudok AS number,tasukuup AS maksepaev,tasumata, kuupaev FROM " +
                   "firma1.dok WHERE pais2obj = ? AND tasukuup<current_date+5 AND doktyyp='G' AND tasumata<>0 " +
                   "ORDER BY klient";

      connectionUp();
      PreparedStatement st = con.prepareStatement(sql);
      st.setString(1, agentCode);
      ResultSet rs = st.executeQuery();
      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        debt.setDoknr(rs.getString("number").trim());
        debt.setDuedate(rs.getDate("maksepaev"));

        debt.setSum(rs.getDouble("tasumata"));
        debt.setDate(rs.getDate("kuupaev"));
        list.addElement(debt);
      }
      st.close();
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
           /* sql = "select sum(doksumma) as eelmineaasta  from dok where (month(kuupaev))=(month(current_date)) and doktyyp='G' and year(current_date)-1=year(kuupaev)";
            //+ "< (select sum(doksumma) as seeaasta  from dok where (month(kuupaev))=(month(current_date)) and doktyyp='G' and year(current_date)=year(kuupaev))";
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


            sql = "select sum(doksumma) as seeaasta  from dok where (month(kuupaev))=(month(current_date)) and doktyyp='G' and year(current_date)=year(kuupaev)";
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
                "select sum(doksumma)/1.20 as seekuu  from firma1.dok where (month(kuupaev))=(month(current_date)) and doktyyp='G' AND year(current_date)=year(kuupaev) AND pais2obj=?";
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
            String sql2 =
                "select sum(doksumma)/1.20 as seekuu  from firma1.dok where (month(kuupaev))=(month(current_date)) and yksus='3' and doktyyp='G' AND year(current_date)=year(kuupaev) AND pais2obj=?";
            if (isDebug()) {
              System.out.println(sql2);
            }

            PreparedStatement pstmt2 = con.prepareStatement(sql2);
            pstmt2.setString(1, agentCode);

            ResultSet rs2 = pstmt2.executeQuery();

            double seekuuladu3 = 0;

            if (rs2.next()) {
              seekuuladu3 = rs2.getDouble("seekuu");
              setSales3(seekuuladu3);
            }
            rs2.close();
            pstmt2.close();

            String sql1 =
                "select sum(doksumma)/1.20 as seekuu  from firma1.dok where (month(kuupaev))=(month(current_date)) and yksus='4' and doktyyp='G' AND year(current_date)=year(kuupaev) AND pais2obj=?";
            if (isDebug()) {
              System.out.println(sql1);
            }

            PreparedStatement pstmt1 = con.prepareStatement(sql1);
            pstmt1.setString(1, agentCode);

            ResultSet rs1 = pstmt1.executeQuery();

            double seekuuladu2 = 0;

            if (rs1.next()) {
              seekuuladu2 = rs1.getDouble("seekuu");
              setSales4(seekuuladu2);
            }
            rs1.close();
            pstmt1.close();


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
    public static final String DRIVER_VERSION = "Interaltus";
    private String	       url = "jdbc:odbc:eevakiil";
    private Connection	       con = null;
    private Statement	       stmt;
    private String	       subjectFilesTo = null;
	@Override
	public void setBulkSales(Map agents) {
		// TODO Auto-generated method stub
		
	}
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/
