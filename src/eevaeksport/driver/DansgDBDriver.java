
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
//import ee.mobilejava.wrapper.SuperVisorLineWrapper;
//import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.EksportPanel;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class DansgDBDriver extends AbstractDBDriver implements Discounter, Manager,Promo, BulkUpdater{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public DansgDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
        try {
            Class.forName(databaseDriverName).newInstance();
         ddd = DriverManager.getConnection("jdbc:odbc:PRECEJ", "SYSDBA", "SYSADM");
         ggg = DriverManager.getConnection("jdbc:odbc:PRECEJ", "SYSDBA", "SYSADM");
        } catch (Exception e) {
            parent.showError(e);
        }
    }
    
    
    
    public String getSaveDebtInfo() {
    	makePictureCat();
    	System.exit(0);
    	return null;
       }

    
    
    private void makePictureCat(){
	     
       	
       	String cmd2 = "SELECT pr.preces_kods AS toode,pr.preces_nosaukums AS nimi,pg.grupas_nosaukums as grnimi,pr.realiz_cena AS myygihind," +
  		"21 AS VAT,'vnt' AS unit, IEPAKOJUMS AS tykke ,1 AS " +
  		" tykke,atlikums AS stock, gr.id_grupa || '' || gr.id_apaksgrupa AS uusgrupp,gr.id_grupa AS grupp " +
        ", pr.svitrkods AS ean ,pr.isais_nosaukums AS liik ,ag_nosaukums AS gr2nimi FROM preces  pr INNER JOIN apaksgrupas gr ON gr.id_apaksgrupa=pr.id_apaksgrupa " +
		"INNER JOIN  precu_grupas pg ON pg.id_grupa=gr.id_grupa where pr.preces_kods != ''" +
		 "  order by pr.preces_nosaukums ";

       	String cmd1 = "select pr.preces_kods as toode ,KOPU_PIELIKUMI.FAILA_NOSAUKUMS  as nimi from KOPU_PIELIKUMI " + 
       			"INNER JOIN  PRECU_KOPAS  on PRECU_KOPAS.ID_PRECU_KOPA   =   KOPU_PIELIKUMI.ID_PRECU_KOPA " + 
       			"INNER JOIN PRECES pr on " + 
       			"PR.ID_PRECU_KOPA = PRECU_KOPAS.ID_PRECU_KOPA where PRECU_KOPAS.ID_PRECU_KOPA != 0 and "
       			+ "(KOPU_PIELIKUMI.FAILA_NOSAUKUMS like '%jpg'"
       			+ " or KOPU_PIELIKUMI.FAILA_NOSAUKUMS like '%JPG' or KOPU_PIELIKUMI.FAILA_NOSAUKUMS like '%png'"
       			+ "or KOPU_PIELIKUMI.FAILA_NOSAUKUMS like '%PNG')";
       	
       	String picts = parent.getProps().getProperty("pict.dir");
        String pictspda = parent.getProps().getProperty("pictpda.dir");
     	  

       	try {
     	    Connection con = getNewCon();
    		Statement stmt = con.createStatement();
       	    ResultSet	      rs = stmt.executeQuery(cmd1);
   
       	TreeMap<String,String> pictsT = new TreeMap<String,String> ();
       	
       	while (rs.next()) {
       		pictsT.put(rs.getString("toode"), rs.getString("nimi"));
       	 }
       	    
       	    rs.close();
       	    
       	    
       	 rs = stmt.executeQuery(cmd2);

            StringBuffer buf = new StringBuffer();
            
     
            
       	    while (rs.next()) {
       	    	
       	     String toode = rs.getString("toode").trim();
             String pnimi = pictsT.get(toode);

             toode = toode.replace('/', '_');
             toode = toode.replace('/', '_');
       	    	     
                   
             File in = new File ( picts + pnimi); 
          	if(in.isFile()){    
          	 
          	File inpda = new File ( pictspda + toode+ ".jpg"); 
          	 if(!inpda.exists()){
          		Converter.shrinkAndCopyFile(in,inpda);
         	  }
          
          	
            String len = "0";
            if(inpda.exists()){
              len =	inpda.length() + "";
              } 
              buf.append(toode+".jpg;"  + len + "\n");
              
            } else {
            	in = new File ( picts + toode+".jpg"); 
            
          	if(in.isFile()){    
          	 
          	File inpda = new File ( pictspda + toode+ ".jpg"); 
          	 if(!inpda.exists()){
          		Converter.shrinkAndCopyFile(in,inpda);
         	  }
          
          	
            String len = "0";
            if(inpda.exists()){
              len =	inpda.length() + "";
              } 
              buf.append(toode+".jpg;"  + len + "\n");
            }
          	
          	
          	}
             
             
             
       	    }
                  
                     
                     
       	    rs.close();
       	    stmt.close();
       	    con.close();
       	
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
       
    }
     
     
       
    
    
	
    public Vector getLastThreeData() {
		return null;
	}
    
	public String getAssortmentStr(Agent agent){
   
    	return null;
      }

    public String getProductSortingData(){
       	return null;
       }

        public String getDealerCustomerCSV(String agent){
            return null;
            }
        public Vector getFixedAssortmentData(){
            return new Vector();
            }
        public Vector getPriceMatrixData() {
     
        	return null;
    }


                 public String  getCustProdDiscounts(Agent agent){
                         return null;
                         }


    /**
     *
     */
    public void connectionUp(){
    }

    private Connection getNewCon() throws SQLException{
      return DriverManager.getConnection(url, "SYSDBA", "SYSADM");
    }
    private Connection getNewCon2() throws SQLException{
        return DriverManager.getConnection(url2, "SYSDBA", "SYSADM");
      }

    /**
     *
     */
    public void connectionDown(){
    }
    public void connectionDown2(){
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
     setTreeGroup(true);
    TreeMap   tm = new TreeMap();
    
    String    sql = "select id_grupa as liik, GRUPAS_NOSAUKUMS as nimetus from precu_grupas";
 

    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {

    	String liik =rs.getString("liik").trim();
    	String nimi = rs.getString("nimetus").trim();
    	if(nimi.indexOf("+++")==-1){
    		if(liik.length() <3) {
    			liik = "0"+ liik ;
    		}
    		if(liik.length() <3) {
    			liik = "0"+ liik ;
    		}
    		tm.put(liik,Converter.enCodeEst(nimi));
    	}

    }
    rs.close();
    st.close();
    con.close();

    return tm;
    }

    public TreeMap getProdGroupsSecond() throws SQLException {
    	   Connection con = getNewCon();
    	     setTreeGroup(true);
    	    TreeMap   tm = new TreeMap();
    	    
    	    String    sql = "SELECT id_grupa as prim, id_apaksgrupa as liik ,ag_nosaukums AS nimetus from apaksgrupas ORDER by ag_nosaukums " ;


    	    Statement st = con.createStatement();
    	    ResultSet rs = st.executeQuery(sql);

    	    while (rs.next()) {
    	    	String prim =rs.getString("prim").trim();
    	    	
    	    	if(prim.length() <3) {
    	    		prim = "0"+ prim ;
        		}
        		if(prim.length() <3) {
        			prim = "0"+ prim ;
        		}
    	    	
    	    	String liik =rs.getString("liik").trim();
    	    	String nimi = rs.getString("nimetus").trim();
    	    	if(nimi.indexOf("+++")==-1){
    	    		tm.put(prim+liik,Converter.enCodeEst(nimi));
    	    	}

    	    }
    	    rs.close();
    	    st.close();
    	    con.close();

    	    return tm;      }

        public String getDistribution() {
          return null;
        }



    public TreeMap getAgents() {
      String  cmd = "select id_darbinieks AS kood , vards || ' ' ||uzvards as nimi from darbinieki ORDER BY id_darbinieks";
        TreeMap agents = new TreeMap();

        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("kood").trim());

              String nimi = rs.getString("nimi").trim();
              agent.setName(nimi);
              agent.setIsPilot(true);
              agents.put(nimi, agent);
            }
            rs.close();
            stmt.close();
            con.close();

        } catch (Exception e) {
            getParent().showError(e);
        }
        return agents;
    }







    public TreeMap getCustomers(Agent agent){

      String cmd = "SELECT a.klienta_nosaukums AS nimi,a.klienta_kods AS kood,a.adrese || ' ' || a.pilseta AS aadress,0 AS discount,a.nace AS visit,a.phone AS info, " +
            " a.id_darbinieks AS agent,'' AS payer , a.apmaksas_termins AS makse , '' AS grupp ,'' AS klale FROM klienti  a where " +
			" a.id_darbinieks > 0 " +
			" ORDER BY a.klienta_nosaukums";

        log(cmd);

        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        HashMap custMap2 = new HashMap();

        try {
          Connection con = getNewCon();


          Statement stmt = con.createStatement();
          Statement smt = ddd.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);
            int counter = 0;
            while (rs.next()) {
              counter++;
                String nimi = rs.getString("nimi").trim();
                String custc = rs.getString("kood");
                if(custc != null){

                Customer cust = new Customer(custc.trim());

                String aadress = rs.getString("aadress");
                if(aadress != null && aadress.trim().length()>0){
                    cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress.trim()),"Latvia"));
                    nimi = nimi + " " +aadress.trim();
                }
               // System.out.println("nimi2 " + nimi);
                cust.setName(Converter.enCodeEst(nimi));

                //String ale = rs.getString("discount");
                //if (ale != null && ale.trim().length()>0){
                 // cust.setDiscount(Double.parseDouble(ale.trim()));
               // }

                String visit=rs.getString("visit");
                if (visit!=null && visit.trim().length()>0){
                  cust.setVisit(visit.trim().toUpperCase());
                }

                String info = rs.getString("info");
                if(info != null && info.trim().length()>0){
                	info = info.replaceAll(";", "|Tel:");
                	cust.setPersonalData(Converter.enCodeEst(info.trim()));
                }

                String agentc = rs.getString("agent");
                if(agentc != null && agentc.trim().length()>0){
                    cust.addAgent(new Agent(agentc.trim()));
                 //   cust.addAgent(new Agent("14"));
                    if(agentc.equals("8888")){
                    	Enumeration enums = getParent().getAgentsHash().elements();
                        while (enums.hasMoreElements()) {
                          Agent a = (Agent) enums.nextElement();
                          cust.addAgent(a);
                        }
                    }
                }
                


         String payer = rs.getString("payer");
         if (payer != null && payer.trim().length()>0){
            cust.setPayer(payer.trim());
         }

         String makset = rs.getString("makse");
         if (makset != null && makset.trim().length()>0){
         	if(makset.trim().equals("0")){
         		makset = "-1";
         	}
            cust.setPaymTerm(makset.trim());
         }

              customers.put(cust.getName(), cust);
              custMap.put(cust.getPayer(), cust);
              custMap2.put(cust.getID(), cust);
                }
            }

            rs.close();
            stmt.close();
            con.close();

            String sql3 = "select a.klienta_kods  AS klient , SUM(summa-APMAKSATS) as summa " +
      		"  from klienta_pavadzime dok " +
		   "INNER JOIN klienti a ON a.id_klients=dok.id_klients " +
		   " where summa-APMAKSATS<>0 and dok.APMAKSAS_TERMINS<current_timestamp" +
		   " GROUP BY a.klienta_kods ORDER BY a.klienta_kods";
            sql3 = "select klienta_kods  AS klient, parads as summa from PARADI";
            System.out.println(sql3);
               ResultSet rst = smt.executeQuery(sql3);
               while(rst.next()){
               	String custom = rst.getString("klient");
            	if (custom != null){
               	Customer cc = (Customer)custMap2.get(custom.trim());

               	if (cc != null){

                   double debt = Double.parseDouble(rst.getString("summa").trim());
                   if(debt != 0){
                   	cc.setDebt(debt);

                  	}
                   }
               	}

               }



        } catch (Exception e) {
            getParent().showError(e);
        }

        return customers;
    }

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "select a.klienta_kods  AS klient , dok.BLANKAS_NUMURS as number,dok.APMAKSAS_TERMINS as kuupaev, summa-APMAKSATS as summa " +
      		"  ,current_timestamp-dok.APMAKSAS_TERMINS AS yle from klienta_pavadzime dok " +
		   "INNER JOIN klienti a ON a.id_klients=dok.id_klients " +
		   " where summa-APMAKSATS<>0 and dok.APMAKSAS_TERMINS<current_timestamp+5";

      Statement stmt = ggg.createStatement();

      ResultSet rs = stmt.executeQuery(sql);

      while(rs.next()){
        Debt debt = new Debt();
        String klient = rs.getString("klient");
        if(klient != null){

        debt.setCustID(klient.trim());
        debt.setDoknr(rs.getString("number").trim());

        debt.setDuedate(rs.getDate("kuupaev"));

        debt.setSum(Double.parseDouble(rs.getString("summa").trim()));
        debt.setDue(rs.getString("yle"));
        list.addElement(debt);
        }
      }
       rs.close();
       stmt.close();
       return list;
    }

    public TreeMap getProducts(Agent agent){

      String cmd = "SELECT pr.preces_kods AS toode,pr.preces_nosaukums AS nimi,pg.grupas_nosaukums as grnimi,pr.realiz_cena AS myygihind," +
      		"21 AS VAT,'vnt' AS unit, IEPAKOJUMS AS tykke ,1 AS " +
      		" tykke,atlikums AS stock, gr.id_apaksgrupa AS uusgrupp,gr.id_grupa AS grupp " +
            ", pr.svitrkods AS ean ,pr.isais_nosaukums AS liik ,ag_nosaukums AS gr2nimi FROM preces  pr INNER JOIN apaksgrupas gr ON gr.id_apaksgrupa=pr.id_apaksgrupa " +
			"INNER JOIN  precu_grupas pg ON pg.id_grupa=gr.id_grupa where pr.preces_kods != ''" +
			 "  order by pr.preces_nosaukums ";

        if (isDebug()) {
            System.out.println(cmd);
        }

        TreeMap products = new TreeMap();
        try {
          Connection con = getNewCon();
          Statement st = con.createStatement();
            ResultSet	      rs = st.executeQuery(cmd);
            HashMap prodHash = new HashMap();
            while (rs.next()) {
                String kood = rs.getString("toode").trim();

                String nimetus = Converter.enCodeEst(rs.getString("nimi").trim()) ;
                Product prod = new Product(kood);
                String grnimi = rs.getString("grnimi").trim() ;
                prod.setPrice(rs.getDouble("myygihind"));
             
                //prod.setPrice(prod.getPrice()/(1+vt/100));
                String unit = rs.getString("unit");
                if(unit != null && unit.trim().length()>0){
                	prod.setUnit(unit.trim());

                }

               prod.setInCases(rs.getInt("tykke"));

                prod.setStock(rs.getDouble("stock"));
                String gruppsec = rs.getString("uusgrupp");
                
                String grupp = rs.getString("grupp");
                if(grupp != null && grupp.trim().length()>0){
                String liik = grupp.trim();
                	if(liik.length() <3) {
            			liik = "0"+ liik ;
            		}
            		if(liik.length() <3) {
            			liik = "0"+ liik ;
            		}
                	prod.setGroup(liik);

                    if(gruppsec != null && gruppsec.trim().length()>0){
                    	 
                    	prod.setGroupSecond(liik+gruppsec.trim());
                    	 prod.setSorting(gruppsec.trim());

                    }
                }

              

                String ean = rs.getString("ean");
                if(ean != null && ean.trim().length()>0){
                	prod.setEAN(ean.trim());
                }

                String liik = rs.getString("liik");
                if(liik != null && liik.trim().length()>0){
                	liik = " "  + liik.trim();
                }else{
                	liik = ".";
                }
                String gr2 = rs.getString("gr2nimi");
                if(gr2 != null && gr2.trim().length()>0){
                	gr2 = gr2.trim();
                }else{
                	gr2 = "";
                }
                prod.setVat("21");

             //   prod.setName(prod.getGroup()+ " " + kood + " " +  nimetus);
                prod.setName(prod.getGroup() + liik + " "  + kood + " " +  nimetus);

                if(nimetus.indexOf("+++")== -1 && grnimi.indexOf("+++")== -1 && gr2.indexOf("+++")== -1){
                    products.put(prod.getName(), prod);
                    prodHash.put(prod.getID(), prod);
                }
            }
            rs.close();
            st.close();
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }

        return products;
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
    public Vector getCustomerProductsList(String agentCode) {

      String sql1 = "  select a.klienta_kods AS klient , pr.preces_kods AS toode, daudzums AS kogus," +
      		"  rid.CENA_REALIZ as price, dok.izrakst_datums AS kuupaev  " +
		 "  from klienta_pavadzime dok inner join kpavadz_saturs rid on rid.id_kpavadzime=dok.id_kpavadzime  " +
		 "  INNER JOIN klienti a ON a.id_klients=dok.id_klients" +
		 "  INNER JOIN preces pr ON pr.id_prece=rid.id_prece" +
		 "  INNER JOIN apaksgrupas gr ON gr.id_apaksgrupa=pr.id_apaksgrupa " +
			"INNER JOIN  precu_grupas pg ON pg.id_grupa=gr.id_grupa  where " +
		 "  dok.izrakst_datums > current_timestamp-400 and a.klienta_kods != ''  " +
		 "  GROUP BY pr.preces_kods,a.klienta_kods,rid.daudzums,rid.CENA_REALIZ,dok.izrakst_datums,pg.id_grupa,pr.isais_nosaukums " +
		 "  ORDER BY a.klienta_kods,pg.id_grupa,pr.isais_nosaukums,pr.preces_kods,dok.izrakst_datums desc ,rid.daudzums " ;


        if (isDebug()) {
            System.out.println(sql1);

        }

        Vector custProds = new Vector();

        try {
          Connection con = getNewCon();
          PreparedStatement stmt = con.prepareStatement(sql1);

          //stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
          ResultSet	      rs = stmt.executeQuery();
          
           java.util.Date lme = new java.util.Date();
           lme.setMonth(lme.getMonth() + 1);
           lme.setDate(1);
           lme.setYear(lme.getYear() -1);
           java.util.Date lms = new java.util.Date();
           lms.setDate(1);
           lms.setYear(lms.getYear() -1);
          HashMap kliendid = new HashMap();
          HashMap<String,Double> kogused = new HashMap<String,Double>();
          HashSet pandud = new HashSet ();
          while (rs.next()) {
            String kliendikood = rs.getString("klient").trim();
            String tootekood = rs.getString("toode").trim();
            double kogus = rs.getDouble("kogus");
            double price = rs.getDouble("price");
            Date dd = rs.getDate("kuupaev");
            
              CustProd cp = new CustProd(kliendikood, tootekood, kogus, 0, 0,
                                           Converter.getYYYYpMMpDDpString(dd).substring(0,5), price, 0, 0, " ");
            
           if(dd.after(lms) && dd.before(lme)  ){
        	  	if(kogused.get(kliendikood +"-"+ tootekood) != null){
            		kogused.put(kliendikood +"-"+ tootekood, kogused.get(kliendikood +"-"+ tootekood) + kogus );
                }else{
            		kogused.put(kliendikood +"-"+ tootekood, kogus );
            	  }
            }
           
          
           if(kogused.get(kliendikood + tootekood) != null){
             cp.setAbiKood("LYSM "  + kogused.get(kliendikood + tootekood));                    
           }
         
        
          
              if(!pandud.contains(  kliendikood +"-"+ tootekood)) {
            	  custProds.add(cp);
              }
              
              pandud.add(kliendikood +"-"+ tootekood);
              
          }

          rs.close();
          stmt.close();
          con.close();
        
        } catch (Exception e) {
          getParent().showError(e);
        }
        connectionDown();
        
        return custProds;
    }

    public String getPilotDayData(Agent agent){


      String sql2 =  "SELECT a.nosauk  || ' ' || a.adrese || ' ' || a.pilseta AS klient,numurs AS arve, d.created AS kuupaev," +
      		" summa9 AS summa , d.kontr " +
          " FROM dokumenti d INNER JOIN adreses a ON d.san_kods=a.kods WHERE  kontr = '" + agent.getID()+ "' AND " +
            " d.created > current_timestamp-1 AND d.tips='-'  ORDER BY d.created desc ";

      String sql3 =  "SELECT a.nosauk  || ' ' || a.adrese || ' ' || a.pilseta AS klient,numurs AS arve, d.created AS kuupaev," +
		" summa9 AS summa , d.kontr " +
    " FROM dokumenti d INNER JOIN adreses a ON d.nos_kods=a.kods WHERE  kontr = '" + agent.getID()+ "' AND " +
      " d.created > current_timestamp-1 AND d.tips='+'  ORDER BY d.created desc ";

      System.out.println("siin");
        StringBuffer sb = new StringBuffer();
        String header = " Sanemtie pasutijumi :\n\n";
        String header2 = "  Atgriezt�s pre�u pavadz�mes :\n\n";
        boolean first = true;
        boolean second = true;
          try {
            Connection con2 = getNewCon();
                   Statement st = con2.createStatement();
                  ResultSet rs2 = st.executeQuery(sql2);
                      while (rs2.next()) {
                      	if(first){
                      		sb.append(header);
                      		first = false;
                      	}

                      sb.append(rs2.getString("klient")  +
                      		"\n    (" + rs2.getString("arve").trim() + ")[" + rs2.getString("kuupaev") + "] " +
                      		getParent().getNF().format(rs2.getDouble("summa"))
                      	+" LVL "	+  "\n\n");
                      }


                     ResultSet rs3 = st.executeQuery(sql3);
                         while (rs3.next()) {
                         	if(second){
                         		sb.append(header2);
                         		second = false;
                         	}

                         sb.append(rs3.getString("klient")  +
                         		"\n    (" + rs3.getString("arve").trim() + ")[" + rs3.getString("kuupaev") + "] " +
                         		getParent().getNF().format(rs3.getDouble("summa"))
                         	+" LVL "	+  "\n\n");
                         }


                      } catch (Exception e) {
                          getParent().showError(e);
                      }
                      System.out.println(agent.getID() + sb.toString());
         if(sb.toString().trim().length()>0){

          return sb.toString();
         }else {
         	return null;
         }

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
      String cmd = "SELECT id_darbinieks AS agent, sum (summa_valuta) AS sales  FROM " +
      " klienta_pavadzime dok WHERE  extract(month from dok.izrakst_datums)= extract(month from current_timestamp) AND " +
      " extract(year from dok.izrakst_datums)= extract(year from current_timestamp)  GROUP BY id_darbinieks";
        log(cmd);

        String cmd2 = " SELECT pl.id_darbinieks AS agent,  sum(PL_SUMMA) AS planf  FROM " +
       "  PLANS pl WHERE  extract(month from MENESIS) = extract(month from current_timestamp) " +
	"	 and   extract(year from MENESIS) = extract(year from current_timestamp)  GROUP BY id_darbinieks";
        
       //gruppide myygid  
        String cmd3  = "  select dok.id_darbinieks AS agent , ag_nosaukums AS grupp, sum(summa_ar_nodokliv)  AS planf " +
		"  from klienta_pavadzime dok inner join kpavadz_saturs rid on rid.id_kpavadzime=dok.id_kpavadzime  	  " +
		"   INNER JOIN preces pr ON pr.id_prece=rid.id_prece " +
		"   INNER JOIN apaksgrupas gr ON gr.id_apaksgrupa=pr.id_apaksgrupa " +
	"		 where  extract(month from dok.izrakst_datums) = extract(month from current_timestamp) " +
     "        and   extract(year from dok.izrakst_datums) = extract(year from current_timestamp) " +
		"	 GROUP BY dok.id_darbinieks,ag_nosaukums " +
		"   ORDER BY dok.id_darbinieks,ag_nosaukums";
          log(cmd);

          
        //gruppide plaanid
          String cmd4  = "   SELECT pl.id_darbinieks AS agent, ag_nosaukums as grupp ,(PL_SUMMA) AS planf  FROM " +
          "  PLANS pl INNER JOIN apaksgrupas gr ON gr.id_apaksgrupa=pl.id_apaksgrupa " +
		  "	WHERE  extract(month from MENESIS) = extract(month from current_timestamp) " +
          "    and   extract(year from MENESIS) = extract(year from current_timestamp) and " +
		  " PL_SUMMA >0  ";
            log(cmd);
          
        /*  TABLE PLANS
          ================
           
              MENESIS                   - Monh
              ID_APAKSGRUPA      - Link to Groups   (from table APAKSGRUPAS)
              ID_DARBINIEKS         - Link to Employee   (from table DARBINIEKI)
              PL_SUMMA                - planned sum
          */ 
           

        
        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();
            HashMap asjad = new HashMap();
            ResultSet rs = stmt.executeQuery(cmd2);
             while(rs.next()) {
            	  String agentID = rs.getString("agent").trim();
            	  int plan = rs.getInt("planf");
            	  asjad.put(agentID, plan);
             }
            rs.close();
            stmt.close();
          
            stmt = con.createStatement();
            HashMap myydud = new HashMap();
            rs = stmt.executeQuery(cmd3);
             while(rs.next()) {
            	  String agentID = rs.getString("agent").trim();
            	  String grupp  = rs.getString("grupp");
            	  int plan = rs.getInt("planf");
            	  if(myydud.get(agentID) != null){
            		  TreeMap asi = (TreeMap)myydud.get(agentID);
            		  asi.put(grupp, plan + "");
            		  myydud.put(agentID, asi); 
            	  }else{
            		  TreeMap asi = new TreeMap();
            		  asi.put(grupp, plan +"");
            		  myydud.put(agentID, asi); 
            	  }
            	
             }
            rs.close();
            stmt.close();
            
            stmt = con.createStatement();
            HashMap plaanid = new HashMap();
            rs = stmt.executeQuery(cmd4);
             while(rs.next()) {
            	  String agentID = rs.getString("agent").trim();
            	  String grupp  = rs.getString("grupp");
            	  int plan = rs.getInt("planf");
            	  if(plaanid.get(agentID) != null){
            		  TreeMap asi = (TreeMap)plaanid.get(agentID);
            		  asi.put(grupp, (plan + ""));
            		  plaanid.put(agentID, asi); 
            	  }else{
            		  TreeMap asi = new TreeMap();
            		  asi.put(grupp, (plan +""));
            		  plaanid.put(agentID, asi); 
            	  }
            	
             }
            rs.close();
            stmt.close();
            
            stmt = con.createStatement();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

             rs = stmt.executeQuery(cmd);
            while(rs.next()) {
              String agentID = rs.getString("agent");
              Agent agent = (Agent)agents.get(agentID);
             
              if (agent != null){
                  agent.setMonthSales((int)rs.getDouble("sales"));
                if(asjad.get(agentID) != null){
                	agent.setMonthBudget(Integer.parseInt("" + asjad.get(agentID)));
                }
              }
            }
            rs.close();
            stmt.close();
            con.close();
            
          Iterator itera =  plaanid.keySet().iterator();
            
            while(itera.hasNext()) {               
            	String agentID = (String)itera.next();               
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                	agent.setStartText("");
                	TreeMap planas = (TreeMap)plaanid.get(agentID);
                	 Iterator itera2 =  planas.keySet().iterator();
                	 while(itera2.hasNext()) {
                		 String grupp = (String)itera2.next();
                		 String done = "€ / 0€";
                		 String pl = "" + planas.get(grupp);
                		 if(myydud.get(agentID)!= null){
                		      TreeMap mm = (TreeMap)myydud.get(agentID);
                		      if(mm.get(grupp)!= null){
                		    	  done = "€ / " +(String)mm.get(grupp) + "€";
                		      }
                		 }
                		 agent.setStartText(agent.getStartText() + grupp + " |" + pl + done + "||");
                	 }
                }
              }
            
            
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
      if (agentCode == null) return 0;
      String cmd = "SELECT SUM(OR20024) AS sales ,OR20019 AS agent FROM OR200100 WHERE MONTH(OR20015)=" +
                   "MONTH(GETDATE()) AND YEAR(OR20015)=YEAR(GETDATE()) GROUP BY OR20019";
        log(cmd);

        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            ResultSet	      rs = stmt.executeQuery(cmd);

            while(rs.next()) {
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


    public String getSaveStockInfo(){

    	  String  cmd = " SELECT artikuls AS toode,sum(atlrez) AS stock from nolik_avg('LVL', " +
             " '01.01.1999', 0, '1', '0', '0', '', '', '') group by artikuls Order by artikuls" ;

	       if (isDebug()) System.out.println(cmd);
	       StringBuffer products = new StringBuffer();
	       try {

	          Connection con = getNewCon();
	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmd);

	         while (rs.next()) {

	           String kood = rs.getString("toode").trim();
	         //  String dealer = rs.getString("LADU").trim();
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


/*	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
       String sql = "INSERT into supervisor values ("
         + "?, "   //   agent	C(50),
         + "?,"   //   agentname	C(200),
         + "?,"   //   clientid	C(50),
         + "?,"   //   clientname	C(200),
         + "?,"   //	prodid	C(50),
         + "?,"   //   prodname	C(200),
         + "current_timestamp ,"   //	sentdate	D,
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
             PreparedStatement ps = ccc.prepareStatement(sql);

             for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
               SuperVisorLineWrapper lw = new SuperVisorLineWrapper(iter.next());
               ps.setString(1, wr.getInfo().getAgentID());
               ps.setString(2, wr.getInfo().getAgentName());
               ps.setString(3, lw.getLine().getCustID());
               ps.setString(4, lw.getLine().getCustName());
               ps.setString(5, lw.getLine().getProdID());
               ps.setString(6, lw.getLine().getProdName());
               ps.setDouble(7, lw.getLine().getNormalPrice()*100);
               ps.setDouble(8, lw.getLine().getAmount()*100);
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
               int ii = lw.getLine().getResInt1();
               int res = 0;
               if(ii == 0 ){
               	res = 1;
               }
               if(ii == 1 ){
               	res = 0;
               }
               if(ii == 2 ){
               	res = -1;
               }
               ps.setInt(33, res);
               ps.setInt(34, lw.getLine().getResInt2());
               ps.setInt(35, lw.getLine().getResInt3());
               ps.executeUpdate();
             }

             ps.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
       }

*/
    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,enddate AS kuupaev,docsum AS summa,linecount AS ridu,text AS tekst " +
                   " FROM dayreport WHERE enddate>(getdate()-30) ORDER BY clid,enddate desc ";
      Connection con = getNewCon();
      Connection con2 = getNewCon();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);


      String sql2 = "SELECT  c.kood, aasta AS aastamyyk,kuu AS kuumyyk,a.nimi AS agendinimi, " +
			" contact AS kontaktisik ,phone AS telefon,right( paymentcode,2) AS maksetin, discount AS ale FROM " +
			" customers c LEFT JOIN agents a ON a.kood=c.agent LEFT JOIN Vahetabel v ON v.klient=c.kood" +
			"     WHERE c.agent !='LUKUS' and c.kood in (select klient from dbo.custprodlist2 group by klient) ORDER BY c.nimi";

      Statement st2 = con2.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs2 = st2.executeQuery(sql2);

      StringBuffer sb = new StringBuffer();
      String sep = ";";
      while(rs.next()){
        String cliID = rs.getString("klient").trim();
        sb.append(cliID + sep);
        sb.append(rs.getString("kuupaev").substring(0,10) + sep);
        sb.append(rs.getDouble("summa") + sep);
        sb.append(rs.getInt("ridu") + sep);
        String tekst = rs.getString("tekst");
        if (tekst != null && tekst.trim().length()>0){
        	tekst = tekst.trim();
        	tekst = tekst.replace('\'', ' ').replace('\"', ' ');

        } else {
        	tekst = " ";
        }
        sb.append(tekst + sep);

        boolean has = scrollTo(cliID, rs2);
        if (has){
         sb.append(ensureLen(rs2.getString("aastamyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("kuumyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
         sb.append(ensureLen(rs2.getString("kontaktisik"), " ").replace(';',',') + sep);
         sb.append(ensureLen(rs2.getString("telefon"), " ").replace(';',',') + sep);
         String makse = rs2.getString("maksetin");
         if(makse!= null && makse.trim().length()>0){
         	if(makse.trim().equals("HE")){
         		makse = "1";
         	}

         }
         sb.append(ensureLen(makse, "0") + sep);
         sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
        }

        sb.append("\n");
      }

      ManagerData md = new ManagerData();
      md.setClientVisitInfo(sb.toString());

      sql = "SELECT c.kood AS klient,c.agent AS agent,a.nimi AS agentname FROM customers c INNER JOIN agents a ON a.kood=c.agent " +
     "  WHERE c.agent != 'LUKUS' and c.kood IN (select klient from dbo.custprodlist2 group by klient) GROUP BY c.kood,c.agent,a.nimi,c.nimi ORDER BY c.nimi";

      PreparedStatement ps = con.prepareStatement(sql);
    //  ps.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
      rs = ps.executeQuery();
      StringBuffer bf2 = new StringBuffer();
      while(rs.next()){
        bf2.append(rs.getString("klient").trim() + sep);

      	String agent = rs.getString("agent").trim();
        bf2.append(agent + sep);
        String agentName = rs.getString("agentname");
        if (agentName == null) agentName = " ";
        bf2.append(agentName + sep);
        String email = parent.parseFirstEmail(parent.getEmail(agent));
        if(email == null || email.trim().length()== 0 )email = " ";
        bf2.append(email + "\n");
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

    public static final String DRIVER_VERSION = "Dansg";
    private String url = "jdbc:odbc:PRECEJ";
    private String url2 = "jdbc:odbc:VECA";
 
    private Connection ddd ;
    private Connection ggg ;
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

