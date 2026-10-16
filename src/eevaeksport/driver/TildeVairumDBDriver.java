/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
public class TildeVairumDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,WebReporter {

    /**
     * Constructor declarationgetcustomerpr
     * 
     *
     *
     * @param parent
     *
     * @see
     */
    public TildeVairumDBDriver(EksportPanel parent) {
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
        rcompany = getParent().getProps().getProperty("rest.companyx");
        url = "jdbc:odbc:" + getParent().getProps().getProperty("dsn.url");;
    
       }
    
      String user =  null;
      String pass = null;
      String rcompany = null;

   
      
      public Vector getPriceMatrixData()  {
          Vector lines = new Vector();
          String    sql = " SELECT mt.klient AS custid,mt.klliik AS clgroup,toode AS prodid,mt.artliik AS prodgroup,mt.hinnak AS discount,mt.hind " + 
          		"           AS price ,lopp-algus as paevi, algus , lopp  FROM hinnamtr mt inner join hkpais p on p.hinnak = mt.hinnak where " + 
          		"           algus <= date() and ( date()<=lopp or year(lopp) = 0 ) order by mt.klient,mt.klliik,mt.toode ";
      
            try {
            	Connection con = DriverManager.getConnection(url);
                Statement st = con.createStatement();
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

    
    

    
    
    HashMap<String,Double> seisud = null;
           
    public String getSaveStockInfo(){
    	
    	seisud = new HashMap<String,Double>();

  	  String cmVILN = " select  ProductID as toode, ConfirmedRemain as stock from productremain ";
  	  
	       if (isDebug()) System.out.println(cmVILN);
	       StringBuffer products = new StringBuffer();
	       try {
             Connection con = DriverManager.getConnection(url);
	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmVILN);

	         while (rs.next()) {
	           String kood = rs.getString("toode").trim();
	           double seis = rs.getDouble("stock");
	          
	           if(seis > 0 ){
	        	   products.append(kood +  ";");
	               products.append( " ;");//dealer
	               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
	               seisud.put(kood, seis);
	             }
	           }
	           rs.close();
	           st.close();
	           con.close();
	       }catch(Exception e) {e.printStackTrace();}
	       return products.toString();
    	
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
              
          		String cmd =  " SELECT sum(rid.kogus*(rid.hind))AS sales, " +
          		 " dok.pais2obj  AS agent FROM dok " + //k.objekt2
                 " INNER JOIN rid ON dok.doktyyp+STR(dok.dokumnr,7)=rid.doktyyp+STR(rid.dokumnr,7)  INNER JOIN yksus y ON y.yksus = dok.pais2obj " +
                 " WHERE  len(trim(dok.klient)) > 1  AND YEAR(dok.kuupaev)=YEAR(date()) " + // INNER JOIN klient k on k.kood = dok.saaja 
                 "  AND MONTH(dok.kuupaev)=MONTH(date()) AND y.klassif=2 AND (dok.doktyyp = 'G' OR dok.doktyyp = 'Y')" +
                 "  AND (rid.doktyyp ='G' or rid.doktyyp ='Y') " +
          		 "  AND dok.yksus in ('1','3') AND len(trim(rid.toode))>2  GROUP BY  dok.pais2obj" ; //k.objekt2
          	
          		log(cmd);
          		
          		try {
          			Connection con = DriverManager.getConnection(url);
          			Statement  stmt = con.createStatement();		
          			ResultSet  rs = stmt.executeQuery(cmd);
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
        agent1 = new TreeMap();
        String  cmd = "select  TypeId as agent , TypeName as name from PartnerType";
        TreeMap agents = new TreeMap();

	    Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	
        try {
        	Connection con = DriverManager.getConnection(url);
        	Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(cmd);
            while (rs.next()) {
              Agent agent = new Agent(rs.getString("agent").trim());
              agent.setManager(true);
              String name = rs.getString("name").trim();
              
              agent.setName(name);
              agent.setLanguage("LAT");
              agent.setCountry("lv");
              agent.setPassWord("0000");
            
                	  agent.setActive(true);
                	  agent1.put(agent.getID(), agent);
                	  agents.put(name, agent);
                  }
             
            rs.close();
            stmt.close();
            con.close();
         }catch (Exception e) {
            getParent().showError(e);
         }
      
   
        return agents;
    }

    private TreeMap agent1 = new TreeMap();
    
    public TreeMap getCustomers(Agent agent) {
     
    	
        
        try{
     	   Converter.getManagersReplacements(agent1, rcompany, user, pass, false); 
     		
     }catch(Exception e){
     	e.printStackTrace();
     }


        TreeMap customers = new TreeMap();
        HashMap<String,Double> debtH = new HashMap<String,Double>();
        try {
        	
        
            String sql = "select  PartnerID as klient, Total as summa from dbo.vm_PartnerDebtSum";        	
        	 Connection  con = DriverManager.getConnection(url);
             Statement   stmt = con.createStatement();
            

            ResultSet   rst =  stmt.executeQuery(sql);
             while(rst.next()){
               String id = rst.getString("klient").trim();
               double debt = Double.parseDouble(rst.getString("summa").trim());
               debtH.put(id, debt);
             }
             rst.close();

             
             
             String cmd =  "select a.PartnerID as klient, a.addressID as aadrid,Partnername as nimi,address, city, postalcode, partnertypeid as agent,phone, email"
                 + " from partneraddress a inner join partner p on p.PartnerID = a.PartnerID where partnertypeid > 0 " ;

             log(cmd);
             
             ResultSet   rs = stmt.executeQuery(cmd);
             
            while (rs.next()) {
            	String klid = rs.getString("klient").trim();
            	String adid = rs.getString("aadrid").trim();
              Customer cust = new Customer(klid+"_"+adid);
            String nimi = rs.getString("nimi").trim();
            String aadr = rs.getString("address");
            String linn = rs.getString("city");
            String zip = rs.getString("postalcode");
            String ag = rs.getString("agent");
            String email = rs.getString("email");
            
            String ph2 = "";
            if(zip != null && zip.contains("(")) {
            	ph2 = zip.substring(zip.indexOf("(") + 1);
            	if(ph2.contains(")")) {
            		ph2 = ph2.substring(0,ph2.indexOf(""));
            	}
            	zip = zip.substring(0,zip.indexOf("(")).trim();

                cust.restZipCode = zip;
            }else {
            	zip = "";
            }
            
            String persD = "";
            if(ph2.trim().length() > 0) {
            	persD = "Tel: " + ph2 + "|";
            }
            if(email != null && email.trim().length() > 0) {
            	persD  = persD + email ;
            }
            
            if(persD.trim().length() > 0) {
            	cust.setPersonalData(persD);
            }
            
            if(linn != null) {
            	linn = linn.trim();
               cust.restCity = linn;
            } else {
            	linn = "";
            }
            
            if(aadr != null) {
            	aadr = aadr.trim();
               cust.restStreet = aadr;
            } else {
            	aadr = "";
            }
            
        	cust.setPayer(klid);
            cust.setName(Converter.enCodeEst(nimi + " " + aadr + " " + linn ).trim());


            if (aadr != null){
            	aadr = Converter.adressLink((aadr + " " + linn + " " + zip).trim(), "Latvija");
              cust.setAddress(Converter.enCodeEst(aadr));
            }
                        
            if(ag != null && ag.trim().length() >0){
              cust.addAgent(new Agent (ag.trim()));
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
            
            if(debtH.get(klid) != null) {
            	cust.setDebt(debtH.get(klid));
            }
            
            
                customers.put(cust.getName(), cust);
            }
            rs.close();
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
       
    getSaveStockInfo();
    //seisud
	TreeMap products = new TreeMap();
	
	try {
   
	              
         String  cmd = " select  ProductID as idd , ProductCode as kood , ProductBarCode as ean,ProductName as nimi, u.ProductUnitName as unit,ProductTypeID as grupp " + 
         		"   from product p left join productunit u on u.productunitid=p.productunitid ";
         
       if (isDebug()) {
           	System.out.println(cmd);
           	}
        
                      Connection con = DriverManager.getConnection(url);
                      Statement stmt = con.createStatement();
                      ResultSet rs = stmt.executeQuery(cmd);

                      while (rs.next()) {
                          String idd = rs.getString("idd").trim();
                          String kood = rs.getString("kood").trim();
                          String ean = rs.getString("ean");
                          String nimetus = rs.getString("nimi").trim();
                          String unit = rs.getString("unit");
                          String grupp = rs.getString("grupp");
                          
                          
                          Product prod = new Product(idd);
                           prod.setName(Converter.enCodeEst(nimetus.trim()) + " " + kood);
                       
                          if(grupp != null && grupp.trim().length()>0){
                              prod.setGroup(grupp.trim());
                           }
                          
                          if (ean != null && ean.trim().length()>0){
                            prod.setEAN(ean.trim());
                          }
                          
                           if (unit != null && unit.trim().length()>0){
                              prod.setUnit(unit.trim());
                            }
                        
                           if(this.seisud.get(idd) != null) {
                        	 prod.setStock(seisud.get(idd));
                           }
                        
                        
                           
          				  prod.setVat("21");
          		
                          products.put(nimetus + kood, prod);

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
    
    private HashMap prods = null;
    

    public TreeMap getProdGroups() throws SQLException {
 
     TreeMap   tm = new TreeMap();
     String    sql = "  select  ProductTypeID as liik ,ProductTypeName as nimi from producttype";
     Connection con = DriverManager.getConnection(url);
     Statement stmt = con.createStatement();
     ResultSet rs = stmt.executeQuery(sql);

     while (rs.next()) {
       String liik=Converter.enCodeEst(rs.getString("liik").trim());
       String nimetus=Converter.enCodeEst(rs.getString("nimi").trim());
       if (liik != ""){
       tm.put(liik,nimetus);
       }
     }
     rs.close();
     stmt.close();
     con.close();

   
     return tm;
 }




    public Vector getCustomerProductsList(String agentCode) {
       
	/*
	 * String cmd = "SELECT omrid.klient,rid.toode FROM omrid INNER JOIN rid ON omrid.tasudjrk = rid.dokumnr WHERE omrid.rid2obj='" +
	 * agentCode + "' AND (len(omrid.klient) > 1) AND (len(rid.toode) > 2) AND omrid.tasudtyyp='G' AND omrid.reakuupaev >= " +
	 * getStartingTime() + " GROUP BY omrid.klient, rid.toode ORDER BY omrid.klient, rid.nimetus";
	 */
	String cmd =
	    "SELECT dok.saaja,rid.toode,dok.kuupaev,rid.kogus, rid.hind FROM dok INNER \r\n" + 
	    "	   JOIN rid ON dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode WHERE \r\n" + 
	    "	     len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 2 \r\n" + 
	    "	      AND dok.kuupaev >= date() -180 \r\n" + 
	    "	     AND  dok.doktyyp = 'G' AND rid.doktyyp= 'G' and rid.toode not in ('001','002','003','004') GROUP BY dok.saaja, rid.toode ORDER BY dok.saaja, toode.toode,dok.kuupaev";

	if (isDebug()) {
	    System.out.println(cmd);
	}

	Vector custProds = new Vector();

	try {
	    Connection con = DriverManager.getConnection(url);
        Statement stmt = con.createStatement();
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String kliendikood = rs.getString("saaja").trim();
		String tootekood = rs.getString("toode").trim();
		 String kuupaev = rs.getString("kuupaev");
        double kogus = Double.parseDouble(rs.getString("kogus").trim());
        double hind = Double.parseDouble(rs.getString("hind").trim());
     

                if (kliendikood.length() > 1) {
                  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,kuupaev.substring(5,10),hind,0,0," ");
                  custProds.add(cp);
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
      
      String sql = "SELECT saaja,tasudok AS number,tasukuup AS maksepaev,tasumata, kuupaev,(date()-tasukuup) AS yle FROM " +
                   " dok WHERE  doktyyp='G' AND tasumata<>0  " + // tasukuup<date()+10 AND
                   " ORDER BY saaja ";

      Connection con = DriverManager.getConnection(url);
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
        	Connection con = DriverManager.getConnection(url);
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
            rs.close();
            st.close();
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }

    
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

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "TildeVairum";
    private String url = null;
    private String subjectFilesTo = null;



	@Override
	public Vector getFixedAssortmentData() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void WebReporter() {
	 
	
			
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
