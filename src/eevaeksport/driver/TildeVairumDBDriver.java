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
import java.util.HashSet;
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
              
          		String cmd =  "  select  k.partnertypeid as agent , max(AmountFinal) as sales " + 
          				"	 from storedocline r INNER JOIN storedoc d ON d.StoreDOcID = r.StoreDOcID and d.PartnerID != ''\r\n" + 
          				"	 INNER JOIN Partner k ON d.PartnerID = k.PartnerID\r\n" + 
          				"	 where  year(d.DocDate) = YEAR(getdate()) and MONTH(d.DocDate) = MONTH(getdate()) and d.StoreDocTypeID = 3 and d.PartnerID != '' and k.partnertypeid != ''\r\n" + 
          				"	 GROUP BY k.partnertypeid" ; 
          	
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
     	//   Converter.getManagersReplacements(agent1, rcompany, user, pass, false); 
     	 } catch(Exception e){
     	  //e.printStackTrace();
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
            	//cust.setDebt(debtH.get(klid));
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
       
    getCustomerProductsListAction();
    getSaveStockInfo();
    //seisud
	TreeMap products = new TreeMap();
	
	try {
   
	              
         String  cmd = " \r\n" + 
         		"					 select  p.ProductID as idd , p.ProductCode as kood , p.ProductBarCode as ean,ProductName as nimi, u.ProductUnitName as unit,p.ProductTypeID as grupp , h.Price\r\n" + 
         		"         		   from product p left join productunit u on u.productunitid=p.productunitid left join ProductPrice h on h.ProductID = p.ProductID   and h.TypeID = 18  ";
         
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
                          Double hind = rs.getDouble("price");
                          
                          
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
                        
                           boolean goes = false;
                           
                           if(this.seisud.get(idd) != null) {
                        	 prod.setStock(seisud.get(idd));
                        	 goes = true;
                           }
                           if(prodsSold.contains(idd)) {
                        	   goes = true;
                           }
                            prod.setVat("21");
                            if(hind != null && hind > 0) {
                            	prod.setPrice(hind);
                            }
                          
          		        if(goes) {
                           products.put(nimetus + kood, prod);
          		        }
          		        
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
       String liik = Converter.enCodeEst(rs.getString("liik").trim());
       String nimetus = rs.getString("nimi").trim();
       if (liik != ""){
       tm.put(liik, Converter.cleanLitLatEst(nimetus.substring(0,2)) + "_" + Converter.enCodeEst(nimetus));
        }
     }
     rs.close();
     stmt.close();
     con.close();

   
     return tm;
 }


   private Vector custProds = null;
   private HashSet prodsSold = null;
   public Vector getCustomerProductsList(String agentCode) {
      	return custProds;
   }

    public Vector getCustomerProductsListAction() {
    	if(custProds == null) {
    		custProds = new Vector();	
    	}
    	prodsSold = new HashSet();
      
	String cmd =" select d.PartnerID as saaja, a.AddressID as aadr ,ProductID as toode, d.DocDate as kuupaev, Quantity as kogus,AmountFinal/Quantity as hind, DocNoSerial+DocNo as doc\r\n" + 
			"				 from storedocline r\r\n" + 
			"				  INNER JOIN storedoc d ON d.StoreDOcID = r. StoreDOcID and d.PartnerID != ''\r\n" + 
			"				  INNER JOIN PartnerAddress a on a.PartnerID = d.PartnerID and d.PartnerAddressGLNCode = a.GLNCode where d.DocDate >= getdate()-90 and d.StoreDocTypeID = 3\r\n" + 
			"				  GROUP BY d.PartnerID, a.AddressID ,ProductID, d.DocDate, Quantity ,AmountFinal, DocNoSerial+DocNo  ORDER BY d.PartnerID, a.AddressID ,ProductID, d.DocDate " ;
	if (isDebug()) {
	    System.out.println(cmd);
	}
  
	Vector custPro = new Vector();

	try {
	    Connection con = DriverManager.getConnection(url);
        Statement stmt = con.createStatement();
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String kliendikood = rs.getString("saaja").trim();
		String aadr = rs.getString("aadr").trim();
		String tootekood = rs.getString("toode").trim();
		 String kuupaev = rs.getString("kuupaev");
        double kogus = Double.parseDouble(rs.getString("kogus").trim());
        double hind = Double.parseDouble(rs.getString("hind").trim());
        String doknr = rs.getString("doc").trim();
		
                  CustProd cp = new CustProd(kliendikood + "_" + aadr, tootekood, kogus ,0 ,0,kuupaev.substring(5,10),hind,0,0," ");
                  cp.setAbiKood(doknr);
                  custProds.add(cp);
                  if(!prodsSold.contains(tootekood)) {
                	  prodsSold.add(tootekood);
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
      
      String sql = "select PartnerID as saaja, FinancialDocNo as number,FinancialdocDate as kuupaev, DisbursementTerm as maksepaev, NotDisburseAmount as tasumata,  \r\n" + 
      		"	  daycount*-1 as yle from v_DebtAnalysisY where NotDisburseAmount != 0 and PartnerID != ''  order by PartnerID,FinancialDocNo ";

      Connection con = DriverManager.getConnection(url);
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      while(rs.next()){
        Debt debt = new Debt();
        debt.setPayerID(rs.getString("saaja").trim());
        debt.setDoknr(rs.getString("number").trim());
        debt.setDate(rs.getDate("kuupaev"));
        debt.setDuedate(rs.getDate("maksepaev"));
        debt.setSum(rs.getDouble("tasumata"));
        debt.setDue(rs.getString("yle"));
        list.addElement(debt);
      }
      rs.close();
      stmt.close();
      con.close();
   
      return list;
    }

    public double getSalary(String agentCode) {
        return 0;
    }

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

    public String getDriverVersion() {
        return DRIVER_VERSION;
    }

    public String getSubjectFilesTo() {
        return this.subjectFilesTo;
    }
    public String getPilotDayData(Agent agent){
      return null;
    }


    public String getNonMobileOrders(Agent agent){
      return null;
    }

    public void log(String s) {
        getParent().log(s);
    }

    public static final String DRIVER_VERSION = "TildeVairum";
    private String url = null;
    private String subjectFilesTo = null;

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
	public void WebReporter() {
			
	}
	

	@Override
	public void connectionUp() {
		
	}


	@Override
	public void connectionDown() {
		
	}
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/
