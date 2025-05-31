/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.InventoryLine;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.util.Converter;


/**
 * Title:        MobileJava
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class PreseLVDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

    /**
     * Constructor declarationgetcustomerpr
     * 
     *
     *
     * @param parent
     *
     * @see
     */
    public PreseLVDBDriver(EksportPanel parent) {
        super(parent);
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
        try {
            Class.forName(databaseDriverName).newInstance();
        } catch (Exception e) {
            parent.showError(e);
        }
        
        user =  getParent().getProps().getProperty("rest.user");
        pass = getParent().getProps().getProperty("rest.pass");
        rcompany = getParent().getProps().getProperty("rest.companyx");
    
    }
    
      String user =  null;
      String pass = null;
      String rcompany = null;

      private Hashtable custCodes = null;
      
      private Connection getNewCon() throws SQLException{
    	    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
    	  }
      
      public Vector getPriceMatrixData()  {
          Vector lines = new Vector();
          String    sql = " SELECT mt.klient AS custid,mt.klliik AS clgroup,toode AS prodid,mt.artliik AS prodgroup,mt.hinnak AS discount,mt.hind " + 
          		"           AS price ,lopp-algus as paevi, algus , lopp  FROM hinnamtr mt inner join hkpais p on p.hinnak = mt.hinnak where " + 
          		"           algus <= date() and ( date()<=lopp or year(lopp) = 0 ) order by mt.klient,mt.klliik,mt.toode ";
            Statement st = null;
            try {
          	  Connection con = this.getNewCon();
              st = con.createStatement();
              ResultSet rs = st.executeQuery(sql);

              while (rs.next()) {

                String custID = rs.getString("custid");
                String clGroup = rs.getString("clgroup");
                String prodID = rs.getString("prodid");
                String prodGroup = rs.getString("prodgroup");
                String discount = rs.getString("discount");
                double price = rs.getDouble("price");
                int paevi = rs.getInt("paevi");
                String algus = rs.getString("algus");
                String lopp = rs.getString("lopp");
                
                MatrixLine mline = new MatrixLine();
       
             if(paevi < 90) {

                if (lopp != null) {
  					mline.setEndDate(lopp.substring(0, 10));
  				}
  				if (algus != null) {
  					mline.setStartDate(algus.substring(0, 10));
  				}
  				
  			  } 

                if (custID != null && custID.trim().length() > 0) {
                  mline.setCustID(custID.trim());
                }
                if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                  mline.setCliGroup(clGroup.trim());
                }

                if (prodID != null && prodID.trim().length() > 0) {
                  mline.setProdID(prodID.trim());
                }
                if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                  mline.setProdGroup(prodGroup.trim());
                  price = 0;
                }
                if(discount!= null && !discount.trim().equals("HI") && !discount.trim().equals("K4") && !discount.trim().equals("K5")) {
                	try {
                	   	   mline.setDiscount(Double.parseDouble(discount.trim()));
               	            		
                	}catch(Exception e) {
                		e.printStackTrace();
                	}
                }
               
                if (price != 0) {
                  mline.setPrice(price);
                }
                if(price>0 || (!discount.equals("HI") && !discount.equals("K4") && !discount.equals("K5")))
               lines.add(mline);
              }

              rs.close();
              st.close();
              con.close();
            }
            catch (SQLException ex) {
            }
            
                      return lines;
        }
      
      
      
    public String getSaveDebtInfo() {
    	 	
    	    	return null;
    	       }

    
    
  
    
    
    
           
    public String getSaveStockInfo(){
 
  		 
    	 return null ;
    	
    	
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
             /* 	
          		String cmd =  " SELECT sum(rid.kogus*(rid.hind))AS sales, " +
          		 " dok.pais2obj  AS agent FROM dok " + //k.objekt2
                 " INNER JOIN rid ON dok.doktyyp+STR(dok.dokumnr,7)=rid.doktyyp+STR(rid.dokumnr,7)  INNER JOIN yksus y ON y.yksus = dok.pais2obj " +
                 " WHERE  len(trim(dok.klient)) > 1  AND YEAR(dok.kuupaev)=YEAR(date()) " + // INNER JOIN klient k on k.kood = dok.saaja 
                 "  AND MONTH(dok.kuupaev)=MONTH(date()) AND y.klassif=2 AND (dok.doktyyp = 'G' OR dok.doktyyp = 'Y')" +
                 "  AND (rid.doktyyp ='G' or rid.doktyyp ='Y') " +
          		 "  AND dok.yksus in ('1','3') AND len(trim(rid.toode))>2  GROUP BY  dok.pais2obj" ; //k.objekt2
          	
          		log(cmd);
          		
          		try {
          			

    				Connection con =  this.getNewCon();
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
          		*/
          	}
           
     
    public TreeMap getAgents() {
       
        agent1 = new TreeMap();
        String  cmd = " select Parzinis as Agent from kioski where nimi not like 'Sl%' and Parzinis is not null group by Parzinis";
        TreeMap agents = new TreeMap();

	
        try {

			Connection con =  this.getNewCon();
  			Statement stmt = con.createStatement();		
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
            	String kood = rs.getString("Agent").trim();
              Agent agent = new Agent(kood);
              String name = kood;
              agent.setName(name);
              agent.setLanguage("LAT");
              agent.setCountry("lv");
              agents.put(kood, agent);
            }
            rs.close();
            stmt.close();
            con.close();
            
        } catch (Exception e) {
            getParent().showError(e);
        }
       
        
        return agents;
    }

    private TreeMap agent1 = new TreeMap();
    
    public TreeMap getCustomers(Agent agent) {
       
    	
        custCodes = new Hashtable();
        try{
     	   Converter.getManagersReplacements(agent1, rcompany, user, pass, false); 
     		
     }catch(Exception e){
     	e.printStackTrace();
     }
        // String cmd = "SELECT k.kood, k.nimi FROM (klient k INNER JOIN omrid ON k.kood = omrid.klient INNER JOIN rid ON rid.dokumnr = omrid.tasudjrk) WHERE omrid.rid2obj='" + agentCode + "' AND (len(rid.toode) > 2) GROUP BY k.kood";
        String cmd =
            "SELECT kioski.NIMI AS nimi, kioski.MYYGIPUNKT_ID AS kood, kioski.adrese AS aadress, kioski.MP_TYYP_KD AS kett, kioski.Parzinis AS agent, "
            + "kioski.Rajons AS piirkond, kioski.Pilseta AS City FROM kioski\r\n" + 
            "WHERE (((kioski.NIMI) Not Like 'Sl%') AND ((kioski.Parzinis) Not Like 'MENED%')) " ;

        log(cmd);

        TreeMap customers = new TreeMap();
        HashMap custMap2 = new HashMap();
        try {
			Connection con =  this.getNewCon();
  			Statement stmt = con.createStatement();		

            ResultSet	      rs = stmt.executeQuery(cmd);
           
            while (rs.next()) {
              String nimi = rs.getString("nimi").trim();
              Customer cust = new Customer(rs.getString("kood").trim());
               cust.setName(Converter.enCodeEst(nimi));

        

            String aadress = rs.getString("aadress") ;
            
            
            String kett = rs.getString("kett");
            if(kett != null && kett.trim().length() >0){
            	cust.setChain(kett.trim());
            }
            
            
            String ag = rs.getString("agent");
            if(ag != null && ag.trim().length() >0){
            	
            cust.addAgent(new Agent (ag.trim()));
           	 Agent aa = (Agent)getParent().getAgentsHash().get(ag);
           	 if(aa != null && aa.getLanguage() != null){
           		 cust.addAgent(new Agent (aa.getLanguage().trim()));
           	 }
           	 
            	
            	Agent aa1 = (Agent)agent1.get(ag.trim());
            	if(aa1 != null) {
            	
    			Vector vec = aa1.getDubbler();
				if(vec!= null && vec.size() >0){
					Enumeration en = vec.elements();
					while(en.hasMoreElements()){
						String age = (String) en.nextElement();
						cust.addAgent(new Agent(age));
					}
				}
            }
        	}
            String piirkond = rs.getString("piirkond");
           
            String linn = rs.getString("city");
            if(linn != null && linn.trim().length() >0){
            	cust.restCity = linn.trim();
            }
            
            if (aadress != null){
            	String aadr = aadress.trim();
            	 if(piirkond != null && piirkond.trim().length() >0){
            		 aadr = aadr + ", " +piirkond.trim();
                 }
            	 cust.restStreet = aadr.trim();
            	aadr = "." + aadr + ",Latvia.";
              cust.setAddress(Converter.enCodeEst(aadr));
             }
         	
            
            /*   String payer = rs.getString("maksja");
            if(payer != null && payer.trim().length() >0){
            	cust.setPayer(payer.trim());
            }
            String telefon = rs.getString("telefon");
            if(telefon != null && telefon.trim().length() >0){
            	cust.setPersonalData(Converter.enCodeEst("Tel:" + telefon.trim().replaceAll(";", ":")));
            }
            
         
       
            String kontakt = rs.getString("kontakt");
            if(kontakt != null && kontakt.trim().length() >0){
            	if(cust.getPersonalData() != null && cust.getPersonalData().length() > 0){
            		cust.setPersonalData(Converter.enCodeEst(kontakt.trim().replaceAll(";", ":") + "|"+ cust.getPersonalData()   ));
            	}else{
            		cust.setPersonalData(Converter.enCodeEst(kontakt.trim().replaceAll(";", ":") ));	
            	}
            	
            }*/
            
            
                customers.put(nimi, cust);
                custMap2.put(cust.getID(), cust);
            }

            rs.close();
            

      /*      String sql = "SELECT d.klient AS klient, SUM(d.tasumata) AS summa  from dok d  WHERE  " +
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
            rst.close();*/
            stmt.close();
            con.close();
            
        } catch (Exception e) {
            getParent().showError(e);
        }
        return customers;
    }

    


        public String getDistribution() {
          return null;
        }

        
        
        

    public TreeMap getProducts(Agent agent) {
       
   

	TreeMap products = new TreeMap();
	TreeMap<String, Double> hinnad = new TreeMap<String, Double>();

	TreeMap<String, String> eans = new TreeMap<String, String>();
	
	
	try {
   
		
		   String  cmd = "WITH LatestIssues AS (\r\n" + 
		   		"    SELECT VALJAANNE_ID, MAX(izndat) AS LatestDate\r\n" + 
		   		"    FROM Numuri where izndat > getdate() -400\r\n" + 
		   		"    GROUP BY VALJAANNE_ID\r\n" + 
		   		")\r\n" + 
		   		"SELECT \r\n" + 
		   		"    N.VA_NUMBER_ID,\r\n" + 
		   		"    N.VALJAANNE_ID,\r\n" + 
		   		"    N.VA_NUMBER,\r\n" + 
		   		"    N.KAANEHIND,\r\n" + 
		   		"    N.izndat AS issue_date,\r\n" + 
		   		"    N.slegts AS Status , EAN \r\n" + 
		   		"FROM \r\n" + 
		   		"    Numuri N\r\n" + 
		   		"JOIN \r\n" + 
		   		"    LatestIssues L\r\n" + 
		   		"    ON N.VALJAANNE_ID = L.VALJAANNE_ID AND N.izndat = L.LatestDate";
	    
	

	                       Connection con =  this.getNewCon();
		                   Statement stmt = con.createStatement();		
	                       ResultSet rs = stmt.executeQuery(cmd);

	                      while (rs.next()) {
	                        String kood = rs.getString("VALJAANNE_ID").trim();
	                        Double hind = rs.getDouble("kaanehind");
	                        String ean = rs.getString("EAN");
                            if(ean != null && ean.trim().length() > 0) {
                            	eans.put(kood, ean.trim());
                            }
	                        hinnad.put(kood, hind);  
	                      }
	                      
	                      rs.close();
	                      stmt.close();
	                      con.close();
	}catch(Exception e) {
		e.printStackTrace();
	}
		
		
	try {            
        // String cmd = "SELECT toode, nimetus, yhik, myygihind FROM toode WHERE (len(trim(toode)) > 3) AND (trim(toode.grupp) = 'L')";
         String  cmd = "SELECT IZDEVUMI.VALJAANNE_ID as toode, IZDEVUMI.NIMI as nimetus, Periodi.NIMI AS Period, Tipi.Tips As Type, IZDEVUMI.grupa as grupp, Valoda as keel \r\n" + 
         		"FROM (IZDEVUMI INNER JOIN Tipi ON IZDEVUMI.VA_TYYP_KD = Tipi.VA_TYYP_KD) INNER JOIN Periodi ON IZDEVUMI.VA_PERIOOD_KD = Periodi.VA_PERIOOD_KD\r\n" + 
         		"WHERE (((IZDEVUMI.NIMI) Not Like 'Sl%'))";
       if (isDebug()) {
           	System.out.println(cmd);
           	}

                       Connection con =  this.getNewCon();
	                   Statement stmt = con.createStatement();		
                       ResultSet rs = stmt.executeQuery(cmd);

                      while (rs.next()) {
                        String kood = rs.getString("toode").trim();
                        String nimetus = rs.getString("nimetus");
                        String period = Converter.enCodeEst(rs.getString("Period"));
                        String type = Converter.enCodeEst(rs.getString("type"));
                        String grupp = Converter.enCodeEst(rs.getString("grupp"));
                        String keel = Converter.enCodeEst(rs.getString("keel"));
                       // String ean = rs.getString("ean");
                  
                        Product prod = new Product(kood);
                        if(hinnad.get(kood) != null) {
                        	prod.setPrice(hinnad.get(kood));
                        }
                        
                        if(eans.get(kood) != null) {
                        	prod.setEAN(eans.get(kood));
                        }
                        
                        String partii = "";
                        if(period != null && period.trim().length() > 0) {
                        	partii = period;
                        }
                        if(type != null && type.trim().length() > 0) {
                        	partii = partii + " " + type;
                        }
                        if(keel != null && keel.trim().length() > 0) {
                        	partii = partii + " " + keel;
                        }
                        
                        if(partii.length() > 0) {
                        	prod.setPartii(partii.trim());
                        }
                        
                          prod.setName(Converter.enCodeEst(nimetus.trim()));
                          
                          if(grupp != null && grupp.trim().length()>0){
                            prod.setGroup(grupp.trim());
                          }
                        
                       
       
          				  prod.setVat("21");
          			     
                          products.put(prod.getName(), prod);

                      }
                      rs.close();
                      stmt.close();
                      con.close();
                      
              

        } catch (Exception e) {
            getParent().showError(e);
        }
        System.out.println("prod done");
       
        return products;
    }
	

    public TreeMap getProdGroups() throws SQLException {
     TreeMap   tm = new TreeMap();
     String    sql = "select kods, grupa from grupas";
		Connection con =  this.getNewCon();
			Statement stmt = con.createStatement();		

     ResultSet rs = stmt.executeQuery(sql);

     while (rs.next()) {
       String liik=Converter.enCodeEst(rs.getString("kods").trim());
       String nimetus=Converter.enCodeEst(rs.getString("grupa").trim());
       if (liik != ""){
       tm.put(liik,nimetus);
       }
     }
     rs.close();
     stmt.close();
     con.close();

     
     
     return tm;
 }
    
    private HashMap getVas() {
    	HashMap   tm = new HashMap();
    	
    	try {
       
    		
    		   String  cmd = "WITH LatestIssues AS (\r\n" + 
    		   		"    SELECT VALJAANNE_ID, MAX(izndat) AS LatestDate\r\n" + 
    		   		"    FROM Numuri where izndat > getdate() -400\r\n" + 
    		   		"    GROUP BY VALJAANNE_ID\r\n" + 
    		   		")\r\n" + 
    		   		"SELECT \r\n" + 
    		   		"    N.VA_NUMBER_ID,\r\n" + 
    		   		"    N.VALJAANNE_ID,\r\n" + 
    		   		"    N.VA_NUMBER,\r\n" + 
    		   		"    N.KAANEHIND,\r\n" + 
    		   		"    N.izndat AS issue_date,\r\n" + 
    		   		"    N.slegts AS Status\r\n" + 
    		   		"FROM \r\n" + 
    		   		"    Numuri N\r\n" + 
    		   		"JOIN \r\n" + 
    		   		"    LatestIssues L\r\n" + 
    		   		"    ON N.VALJAANNE_ID = L.VALJAANNE_ID AND N.izndat = L.LatestDate";
    	    
    	

    	                       Connection con =  this.getNewCon();
    		                   Statement stmt = con.createStatement();		
    	                       ResultSet rs = stmt.executeQuery(cmd);

    	                      while (rs.next()) {
    	                    	   String vas = rs.getString("VA_NUMBER_ID").trim();
    	                        String kood = rs.getString("VALJAANNE_ID").trim();
    	                     
    	                          tm.put(vas, kood);  
    	                      }
    	                      
    	                      rs.close();
    	                      stmt.close();
    	                      con.close();
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
        
        return tm;
    }



    public Vector getCustomerProductsList(String agentCode) {
       
	String cmd =  "			  WITH LatestRows AS (\r\n" + 
			"    SELECT \r\n" + 
			"        TPID,\r\n" + 
			"        numid,\r\n" + 
			"        MAX(Datums) AS LatestDate\r\n" + 
			"    FROM Datafin\r\n" + 
			"    WHERE Datums >= GETDATE() - 300 and nosut > 0  \r\n" + 
			"    GROUP BY TPID, numid\r\n" + 
			")\r\n" + 
			"SELECT \r\n" + 
			"    D.TPID AS saaja,\r\n" + 
			"    D.numid AS toode,\r\n" + 
			"    D.Datums AS kuupaev,\r\n" + 
			"    D.atgr AS kogus,\r\n" + 
			"    D.nosut AS kogus1,\r\n" + 
			"    D.cena AS hind,\r\n" + 
			"    D.PVZnum AS dok\r\n" + 
			"FROM \r\n" + 
			"    Datafin D\r\n" + 
			"JOIN \r\n" + 
			"    LatestRows L\r\n" + 
			"    ON D.TPID = L.TPID \r\n" + 
			"       AND D.numid = L.numid \r\n" + 
			"       AND D.Datums = L.LatestDate;";

	if (isDebug()) {
	    System.out.println(cmd);
	}
     HashMap vas = getVas();
     HashSet<String> olemas = new HashSet<String>();
	Vector custProds = new Vector();

	try {
		
		Connection con =  this.getNewCon();
		Statement stmt = con.createStatement();		
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String kliendikood = rs.getString("saaja").trim();
		String tootekood = rs.getString("toode").trim();
		if(vas.get(tootekood) != null) {
			tootekood =  vas.get(tootekood)+"";
		}
		 String kuupaev = rs.getString("kuupaev");
        double kogus = Double.parseDouble(rs.getString("kogus1").trim());
        double hind = Double.parseDouble(rs.getString("hind").trim());
   	     String dok = rs.getString("dok");
     

                if (kliendikood.length() > 1) {
                	String signa = kliendikood+"_"+tootekood;
                	if(!olemas.contains(signa)) {
                	
                  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,kuupaev.substring(5,10),hind,0,0," ");
                  cp.setAbiKood(dok.trim());
                  custProds.add(cp);
                	}
                	olemas.add(signa);
                }
	    }

            rs.close();
            stmt.close();
            con.close();

	} catch (Exception e) {
	    getParent().showError(e);
	}
	return custProds;
    }


    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      
    /*  String sql = "SELECT saaja,tasudok AS number,tasukuup AS maksepaev,tasumata, kuupaev,(date()-tasukuup) AS yle FROM " +
                   " dok WHERE  doktyyp='G' AND tasumata<>0  " + // tasukuup<date()+10 AND
                   " ORDER BY saaja ";
		Connection con =  this.getNewCon();
			Statement stmt = con.createStatement();		

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
    rs.close();
    stmt.close();
    con.close();
    */
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
      
        return 0;
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
        return "Mobilejava";
    }
    public String getPilotDayData(Agent agent){
      return null;
    }


    public String getNonMobileOrders(Agent agent){
      
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

    public static final String DRIVER_VERSION = "PreseLV";
    private String	       url = "jdbc:odbc:merisoft";
   
	@Override
	public Vector getFixedAssortmentData() {
		return null;
	}



	@Override
	public String getProductSortingData() {
		return null;
	}



	@Override
	public Vector getLastThreeData() {
		return null;
	}

	@Override
	public void connectionUp() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectionDown() {
		// TODO Auto-generated method stub
		
	}
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/
