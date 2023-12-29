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
import eevaeksport.MatrixLine;
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
public class EevaArkeDBDriver extends AbstractDBDriver  implements BulkUpdater,Discounter,PictureMaker,FixLoader {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     
     * @see
     */
    public EevaArkeDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");

        // String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

        try {
            Class.forName(databaseDriverName).newInstance();

        } catch (Exception e) {
            parent.showError(e);
        }
        user =  getParent().getProps().getProperty("rest.user");
	    pass = getParent().getProps().getProperty("rest.pass");
	    rcompanyx = getParent().getProps().getProperty("rest.companyx");
	    rcompany = getParent().getProps().getProperty("rest.company");
          }
	  
	  String user =  null;
	  String rcompany =  null;
	  String pass = null;
	  String rcompanyx = null;


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
  

    /**
     *
     */
 

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public TreeMap getAgents() {
     
        String  cmd = "SELECT yksus,nimetus FROM firma1.yksus2 a inner join firma1.klient k on k.objekt2 = a.yksus group by yksus,nimetus";
        TreeMap agents = new TreeMap();

        try {
        	 Connection con = DriverManager.getConnection(url);
        	 Statement stmt = con.createStatement();
             ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Agent agent = new Agent(Converter.cleanLitLatEst(rs.getString("yksus").trim()));
              String name = rs.getString("nimetus").trim();
             
              
                agent.setName(name);
                agents.put(name, agent);
            }
            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }
      
        return agents;
    }

    public TreeMap getCustomers(Agent agent) {
      
        // String cmd = "SELECT k.kood, k.nimi FROM (klient k INNER JOIN omrid ON k.kood = omrid.klient INNER JOIN rid ON rid.dokumnr = omrid.tasudjrk) WHERE omrid.rid2obj='" + agentCode + "' AND (length(rid.toode) > 2) GROUP BY k.kood";
        String cmd =
        	" select k.kood,k.maksja, k.nimi,aadress,regnr, tanav , piirkond ,postiindek as inex,k.hinnak AS ale," + 
        	"        	k.objekt2  as agent , kontaktisi, telefon, email, maksetin " + 
        	"              FROM firma1.klient k  where k.objekt2 is not null ORDER BY k.nimi";
        log(cmd);

        TreeMap customers = new TreeMap();

        try {
        	 Connection con = DriverManager.getConnection(url);
        	 Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Customer cust = new Customer(rs.getString("kood").trim());
              String maksja = rs.getString("maksja");
              if(maksja != null && maksja.trim().length() > 0) {
            	  cust.setPayer(maksja.trim());
              }
            String nimi = (rs.getString("nimi").trim()).replaceAll("&", "ja");
            cust.setName((nimi));
            
            
            String street = rs.getString("tanav");
            String piirkond = rs.getString("piirkond");
            String ind = rs.getString("inex");
          
            
             String ag = rs.getString("agent").trim();
            cust.addAgent(new Agent(Converter.cleanLitLatEst(ag)));
            if(piirkond != null && piirkond.length() > 0) {
           	 cust.restCity = piirkond.trim(); 
            }else {
            	piirkond = "";
            }
            
             if(street != null && street.length() > 0) {
            	 cust.restStreet = street.trim();
            	   cust.setAddress(Converter.enCodeEst(Converter.adressLink(street.trim() + " " + piirkond.trim(),"Estonia")));
             }
           
           
            if(ind != null && ind.length() > 0) {
            	 cust.restZipCode = ind.trim();
            }
           
            
            
            
         
           
            String kontaktisi = rs.getString("kontaktisi");
            String telefon = rs.getString("telefon");
            String email = rs.getString("email");
            
            String isi = null;
            
            if(kontaktisi != null && kontaktisi.trim().length() > 0) {
            	isi = kontaktisi.trim();
            }
            
            if(telefon != null && telefon.trim().length() > 0) {
            	if(isi == null) {
            		isi="";
            	}
            	isi = isi + "| Tel:" + telefon.trim();
            }
            
            if(email != null && email.trim().length() > 0) {
            	if(isi == null) {
            		isi="";
            	}
            	isi = isi + "| " + email.trim();
            }
            
            if(isi != null) {
            	cust.setPersonalData(isi);
            }
            
            String mt = rs.getString("maksetin");
            if(mt != null && mt.trim().length() >0 ) {
            	mt = mt.trim();
            	   if(mt.equals("S")) {
                     	 mt = "-2";
                      }
                      cust.setPaymTerm(mt);
                    
            }
         
                customers.put(nimi, cust);
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
        
      
    
        
        TreeMap products = new TreeMap();

        try {
        


          String cmd = " SELECT toode, nimetus, aktsiis as tykke, myygihind, yhik ,t.liik ,ribakood   FROM firma1.toode t  inner join  firma1.artliik k on k.liik =t.liik \r\n" + 
          		"			  and k.grupp = 'L' and k.arttyyp = 'Toode' WHERE \r\n" + 
          		"             t.grupp ='L' and nimetus is not null and left(toode,1) in ('1','2','3','4','5','6') and myygihind > 0 and length(ribakood) > 0  ORDER BY toode ";

         Connection con = DriverManager.getConnection(url);
         Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(cmd);

                      while (rs.next()) {
                          String kood = rs.getString("toode").trim();
                        
                          String nimetus =  rs.getString("nimetus").trim().replace('~', '-') ;
                          String yhik = rs.getString("yhik");
                          Product prod = new Product(kood);
                          prod.setName(nimetus);
                          prod.setPrice(rs.getDouble("myygihind"));
                          prod.setInCases(rs.getDouble("tykke"));	
                          if(yhik != null) {
                        	  prod.setUnit(yhik.trim()); 
                          }
                         
                          prod.setGroup(parent.make(rs.getString("liik")));
                          prod.setEAN(parent.make(rs.getString("ribakood")));
                       	
	                      	if(eanprods == null) {
	                      		eanprods= new TreeMap();
	                      	}
	                    
	                      eanprods.put(prod.getEAN(), prod.getID());
                         // if(!prod.getGroup().startsWith("X")) {
                        	  products.put(nimetus, prod);
                        //  }
                         

                      }
                      rs.close();
                      stmt.close();
                      con.close();

        } catch (Exception e) {
            getParent().showError(e);
        }
        
        

        return products;
    }
    public TreeMap getProdGroups() throws SQLException {
    	  
    	     TreeMap   tm = new TreeMap();
    	     String    sql = " select k.liik, k.artlnimi  from firma1.artliik k where grupp = 'L' and arttyyp = 'Toode' ";
    	     
    	 	 Connection con = DriverManager.getConnection(url);
        	 Statement stmt = con.createStatement();
    	     ResultSet rs = stmt.executeQuery(sql);

    	     while (rs.next()) {
    	       String liik=rs.getString("liik").trim();
    	       String nimetus= (rs.getString("artlnimi").trim());
    	       if (liik != "" && !liik.startsWith("X")){
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
	 * agentCode + "' AND (length(omrid.klient) > 1) AND (length(rid.toode) > 2) AND omrid.tasudtyyp='G' AND omrid.reakuupaev >= " +
	 * getStartingTime() + " GROUP BY omrid.klient, rid.toode ORDER BY omrid.klient, rid.nimetus";
	 */
        String cmd =
        	    "SELECT dok.saaja,rid.toode,dok.kuupaev,max(rid.kogus) as kogus,max(rid.hind)  as hind  FROM firma1.dok INNER " +
        	  " JOIN firma1.rid ON dok.dokumnr = rid.dokumnr INNER JOIN firma1.toode on rid.toode = toode.toode INNER JOIN firma1.klient k on k.kood= dok.saaja WHERE " +
        	   "  length(trim(dok.klient)) > 1 AND length(trim(rid.toode)) > 3 " +
        	   "  AND dok.kuupaev >= current_date-30 " +
        	   "  AND  dok.doktyyp = 'G'  GROUP BY dok.saaja, rid.toode , dok.kuupaev ORDER BY dok.saaja, rid.toode,dok.kuupaev ";
    
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
        double hind = rs.getDouble("hind");

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
        String sql = "SELECT klient,saaja,tasudok AS number,tasukuup AS maksepaev,tasumata, kuupaev FROM " +
                     "firma1.dok WHERE  tasukuup<current_date+5 AND doktyyp='G' AND tasumata<>0 " +
                     "ORDER BY klient";

    	 Connection con = DriverManager.getConnection(url);
    
        PreparedStatement st = con.prepareStatement(sql);
       // st.setString(1, agentCode);
        ResultSet rs = st.executeQuery();
        while(rs.next()){
          Debt debt = new Debt();
          String saaja = rs.getString("saaja");
          if(saaja != null){
          	 debt.setCustID(saaja.trim());
               debt.setDoknr(rs.getString("number").trim());
               debt.setDuedate(rs.getDate("maksepaev"));

               debt.setSum(rs.getDouble("tasumata"));
               debt.setDate(rs.getDate("kuupaev"));
               list.addElement(debt);
          }
         
        }
        rs.close();
        st.close();
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
    public static final String DRIVER_VERSION = "EevaArke";
    private String	       url2 = "jdbc:odbc:PostgreSQL30";
    private String	       url = "jdbc:odbc:merisoft";
   
    private String	       subjectFilesTo = null;
	@Override
	public void setBulkSales(Map agents) {
		
  		String cmd =  "  select sum(doksumma)/1.22 as sales , dok.pais2obj  AS agent   from firma1.dok where (month(kuupaev))=(month(current_date)) and doktyyp='G' AND year(current_date)=year(kuupaev) " +
			  "  GROUP BY dok.pais2obj" ;
  	
  		log(cmd);
  		
  		try {
  			
  			 Connection con = DriverManager.getConnection(url);
            Statement stmt = con.createStatement();		
  		
  			
  			ResultSet	      rs = stmt.executeQuery(cmd);
  			while(rs.next()) {
  				String agentID = rs.getString("agent");
  				if(agentID != null){
  					Agent agent = (Agent)agents.get(agentID.trim());
      				if (agent != null){
      					agent.setMonthSales((int)rs.getDouble("sales"));
      				}
  				}
  				
  			}
  			rs.close();
  			stmt.close();
  			con.close();
  		} catch (Exception e) {
  			getParent().showError(e);
  		}
  		
		
	}

	@Override
	public void PictureMaker() {
		// TODO Auto-generated method stub
		
	}

	@Override
	  public Vector getPriceMatrixData()  {
        Vector lines = new Vector();
        String    sql = " SELECT mt.klient AS custid,mt.klliik AS clgroup,toode AS prodid,mt.artliik AS prodgroup,0 AS discount,mt.hind " + 
       "   AS price , algus , lopp FROM firma1.hinnamtr mt inner join firma1.hkpais p on p.hinnak = mt.hinnak where " +
       "  algus <= current_date and current_date<=lopp order by mt.klient,mt.klliik,mt.toode";
          Statement st = null;
          try {
        	  
        	 	 Connection con = DriverManager.getConnection(url);
            	 
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

              String custID = rs.getString("custid");
              String clGroup = custID;//rs.getString("clgroup");
              String prodID = rs.getString("prodid");
              String prodGroup = rs.getString("prodgroup");
              double discount = rs.getDouble("discount");
              double price = rs.getDouble("price");
              String algus = rs.getString("algus");
              String lopp = rs.getString("lopp");
              
              MatrixLine mline = new MatrixLine();

              if (lopp != null) {
					mline.setEndDate(lopp.substring(0, 10));
				}
				if (algus != null) {
					mline.setStartDate(algus.substring(0, 10));
				}
              if (custID != null && custID.trim().length() > 0) {
               // mline.setCustID(custID.trim());
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
                mline.setDiscount(discount);

              if (price != 0) {
                mline.setPrice(price);
              }
              if(price>0 || discount >0)
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

	public Vector getLastThreeData() {

		Vector result = new Vector();
		try {
		

					String sq3 = "SELECT jou.saaja as klient, jou.kuupaev  as kuup , tra.toode as toode ,sum(tra.kogus) as kogus,max(doksumma) as summa, jou.tasudok \r\n" + 
							"														as doknr  FROM firma1.rid  tra INNER JOIN firma1.dok jou ON  \r\n" + 
							"													       tra.dokumnr = jou.dokumnr \r\n" + 
							"															  WHERE  jou.kuupaev >=  current_date -14 and jou.doktyyp = 'G' and tra.toode != 'KM' \r\n" + 
							"															                GROUP BY jou.saaja,jou.tasudok,jou.kuupaev , tra.toode ORDER BY jou.saaja,jou.tasudok desc,jou.kuupaev desc , tra.toode " ;
					 Connection con = DriverManager.getConnection(url);
					Statement st3 = con.createStatement();
					ResultSet rs3 = st3.executeQuery(sq3);
					while (rs3.next()) {
						MatrixLine ml = new MatrixLine();
						ml.setCustID(rs3.getString("klient").trim());
						ml.setStartDate(rs3.getString("kuup").substring(5,10));
						ml.setProdID(rs3.getString("toode").trim());
						ml.setPrice(rs3.getDouble("kogus"));
						double summa = rs3.getDouble("summa");
						ml.setLineinfo(rs3.getString("doknr").trim() + " ("+ (int)summa + "€)" );
					    
						result.add(ml);
					}
					rs3.close();
					st3.close();
					con.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("LAstthreeData over" + result.size());

		return result;
	}
	
	
	  private TreeMap eanprods = null;
	  
	@Override
	public void FixLoader() {
		
      eanprods =  Converter.loadMore();
		  
		  if(eanprods == null) {
			  eanprods = new TreeMap();
		  }
		this.getProducts(null);
		TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
		System.out.println("Contracts " + contracts.toString());
		Converter.sendContractJson(contracts ,  rcompanyx,  user ,  pass);
		System.exit(0);
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
