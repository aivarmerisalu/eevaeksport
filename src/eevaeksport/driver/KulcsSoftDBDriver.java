
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
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
public class KulcsSoftDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public KulcsSoftDBDriver(EksportPanel parent) {
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
    companyx = getParent().getProps().getProperty("rest.companyx");
    company = getParent().getProps().getProperty("rest.company");
      }
    
    
    String user =  null;
	  String companyx =  null;
	  String company =  null;
	
	  String pass = null;
    
    public String getSaveDebtInfo() {
       	return null;
       }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}    

    public String getSaveStockInfo(){
      
          StringBuffer products = new StringBuffer();

       String sql = "select \"Product\" as toode, \"Balance\" as seis from  \"StockAccountingItem\" a"
       		+ " INNER JOIN \"StockAccountingBalance\" s ON a.\"Id\" = s.\"StockAccountingItem\" where \"Balance\" > 0"; 
       if (isDebug()) {
           System.out.println(sql);
       }

       try {
    	Connection  con = DriverManager.getConnection(url);
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
                  products.append(rs.getString("toode").trim() + "; ; ;");
                  products.append(rs.getString("seis").trim());               
                  products.append("\n");
        }
        rs.close();
        st.close();
        con.close();
       this.connectionDown();
       
          } catch (Exception e) {
              getParent().showError(e);
          }
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

        public String getDistribution() {
          return null;
        }

        public TreeMap getProdGroups() throws SQLException {

            TreeMap   tm = new TreeMap();
            try {
     		 
     		 Connection  con = DriverManager.getConnection(url);
     	        Statement st = con.createStatement();
     	       
     	    ResultSet rs = st.executeQuery("select  \"FieldName\" as kood,ct.\"DisplayName\"  as name "
     	    		+ "from \"CustomField\" cf inner join \"CustomTable\" ct ON cf.\"ForeignTableName\" = ct.\"CustomTableName\"");

     	    while (rs.next()) {
                  
                   String kood = rs.getString("kood");
                   String nimetus = rs.getString("name");
                   try {
                   if(kood != null && nimetus != null ){
                 	  tm.put(kood.trim(), nimetus.trim());
                   }
                  
                   }catch(Exception e) {
                	   e.printStackTrace();
                   }
                
     	    }
     	    rs.close();
     	    st.close();
     	    con.close();
     	    
     	} catch (Exception e) {
     	    getParent().showError(e);
     	}
           
            
             tm.put( "zpienas", "zpienas");
            return tm;
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
   
	TreeMap agents = new TreeMap();
	Agent aa = new Agent("HUN003");
	aa.setName("Hungary 003");
	agents.put(aa.getName(), aa);
	Agent aa1 = new Agent("HUN005");
	aa1.setName("Hungary 005");
	agents.put(aa1.getName(), aa1);
	Agent aa12 = new Agent("HU000");
	aa12.setName("Hungary 000");
	agents.put(aa12.getName(), aa12);
/*
	try {
		   connectionUp();
		 Connection  con = DriverManager.getConnection(url);
	        Statement st = con.createStatement();
	       
	    ResultSet	      rs = st.executeQuery(cmd);

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String nimi = rs.getString("nimetus");
              if(nimi != null){
            	   agent.setIsPilot(true);
                   agent.setName(nimi.trim());
     		       agents.put(nimi, agent);
              }
             
           
	    }
	    rs.close();
	    st.close();
	    con.close();
	    
	} catch (Exception e) {
	    getParent().showError(e);
	}
        //connectionDown();*/
	return agents;
    }

    public TreeMap getCustomers(Agent agent) {
 

        System.out.println("kliendid algus");
       

	

	TreeMap customers = new TreeMap();
	TreeMap<String, Double> dues = new TreeMap<String, Double>();
	TreeMap<String, Double> overdues = new TreeMap<String, Double>();

	try {
		String cmd =
			    "select  \"Customer\",   sum(\"GrossValue\" - \"AcquitValue\")  as debt, sum(case when datediff(day , \"PaymentDate\" ,CURRENT_DATE ) > 0 then \"GrossValue\" - \"AcquitValue\"  else 0 end) as odebt\r\n" + 
			    " from \"CustomerStockOut\"\r\n" + 
			    " where \"VoucherType\"  = 1 and \"AcquitValue\" != \"GrossValue\"  group by 1" ;

	 Connection  con = DriverManager.getConnection(url);
        Statement st = con.createStatement();
    
    ResultSet	      rs = st.executeQuery(cmd);

    while (rs.next()) {
    	   String kood = rs.getString("Customer").trim();
    	   double dd = rs.getDouble("debt");
    	   double ddo = rs.getDouble("odebt");
    	   dues.put(kood, dd);
    	   if(ddo > 0) {
    	   overdues.put(kood, ddo);
    	   }
    }
     rs.close();
     st.close();
		
	 cmd =   "select a.\"Id\",c.\"Id\" as pid , c.\"Name\" , a.\"AddressName\" , a.\"Street\", a.\"City\", a.\"Number\" , c.\"PaymentMethodDelayDay\" , c.\"AppliedPriceRule\" "
				    + " , c.\"ContactName\",c.\"ContactPhone\" ,  c.\"Email\" from \"CustomerAddress\" a INNER JOIN \"Customer\" c ON c.\"Id\" = a.\"Customer\"" ;

		  
	         st = con.createStatement();
	   	      rs = st.executeQuery(cmd);

	    while (rs.next()) {
            String kood = rs.getString("Id").trim();
            String payer = rs.getString("pid").trim();
		Customer cust = new Customer(kood);
		         cust.setPayer(payer);
		         
		String nimi = parent.make(rs.getString("Name"));
		String nimi2 = parent.make(rs.getString("AddressName"));
		String street = parent.make(rs.getString("Street"));
		String city = parent.make(rs.getString("City"));
		String number = parent.make(rs.getString("Number"));
		String paym = parent.make(rs.getString("PaymentMethodDelayDay"));
		String pricegr = parent.make(rs.getString("AppliedPriceRule"));
		String kontakt = parent.make(rs.getString("ContactName"));
		String kontaktTel = parent.make(rs.getString("ContactPhone"));
		String email = parent.make(rs.getString("Email"));
		         
		         String ag = "HUN003";
		         Agent aa = new Agent(ag);
		         cust.addAgent(aa);
		         
		     	 String ag1 = "HUN005";
		         Agent a1 = new Agent(ag1);
		         cust.addAgent(a1);
		         
		         
		         String ag12 = "HU000";
		         Agent a12 = new Agent(ag12);
		         cust.addAgent(a12);
		         
		         if(dues.get(payer) != null) {
		        	 cust.setUsedCredit(dues.get(payer));
		         }
		         if(overdues.get(payer) != null) {
		        	 cust.setDebt(overdues.get(payer));
		         }
		
                cust.setName(Converter.enCodeEst(nimi) + " " + Converter.enCodeEst(nimi2));
                
                
               	cust.setAddress(Converter.adressLink(Converter.enCodeEst(street+ " " + number + ", " + city),"Hungary"));
                	cust.setPersonalData(Converter.enCodeEst(kontakt + " " + kontaktTel + "|" + email));
                	cust.setPaymTerm(paym);
                	cust.setGroup(pricegr);
                
                customers.put(nimi + " " + nimi2, cust);
	    }
	    rs.close();
	    st.close();
	    con.close();
	    
	} catch (Exception e) {
	    getParent().showError(e);
	}
	  System.out.println("kliendid l�pp");
	return customers;
    }
    
    
    private boolean isThere(ResultSet rs, String column){
        try{
            rs.findColumn(column);
            return true;
        } catch (SQLException sqlex){
           System.out.println("column doesn't exist {}" + column);
        }

        return false;
    }    
    

    public TreeMap getProducts(Agent agent) {
        

    	TreeMap products = new TreeMap();
    	TreeMap prices = new TreeMap();
    	try {
        
try{
     String sql = " select \"Product\", \"Price\",  \"ValidFrom\" , \"ValidTo\" from  \"PriceRulePrice\" pr INNER JOIN \"PriceRuleDetail\" d ON d.\"Id\" = pr.\"PriceRuleDetail\" \r\n" + 
     		" where \"PriceRule\" = 386 and Current_Date between \"ValidFrom\" and \"ValidTo\" and \"Price\" > 0  order by 1 " ; //where osak='LADU' 
           
     Connection  con = DriverManager.getConnection(url);
 PreparedStatement pstm = con.prepareStatement(sql);
 ResultSet rs6 = pstm.executeQuery();

while(rs6.next()){
	prices.put(rs6.getString("Product").trim(),rs6.getDouble("Price")+ "");
 }


rs6.close();
pstm.close();
con.close();


System.out.println("prices " + prices.size());

}catch(Exception r){
r.printStackTrace();
}
   
 TreeMap prodsInGroups = new TreeMap();


try{
    String cmd = " Select * from  \"Product\" p where \"Service\" = 0 and \"OutGoingProduct\" != 1 ";

          TreeMap groups = this.getProdGroups();
    Connection  con = DriverManager.getConnection(url);
PreparedStatement pstm = con.prepareStatement(cmd);
ResultSet rs6 = pstm.executeQuery();

ResultSetMetaData rsmd = rs6.getMetaData();
int columnCount = rsmd.getColumnCount();

HashSet names = new HashSet();

for (int i = 1; i <= columnCount; i++ ) {
	String nn = rsmd.getColumnName(i);
	if(groups.keySet().contains(nn)) {
          names.add(nn);
	  }
              }
System.out.println("groups 2 " + groups + " " + names.toString());
//selgitame mis väljad on olemas

if(rs6.next()){
	String kood = rs6.getString("Id").trim();
	//System.out.println("kood 3 " + kood);
	 Iterator itera =  names.iterator();
	while(itera.hasNext()) {
		String field = (String)itera.next();
		System.out.println("kood 3 " + field);
		try {
			
		
			String gr = rs6.getString(field);
			System.out.println("kood 332 " + gr);
			if(gr != null && !gr.equals("0") ) {
				prodsInGroups.put(kood, field);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}


rs6.close();
pstm.close();
con.close();


System.out.println("prices2 " + prodsInGroups);

}catch(Exception r){
r.printStackTrace();
}

           String cmd = " select p.\"Id\" as toode ,p.\"Code\" as code, p.\"Name\", p.\"Barcode\" as ean,"
           		+ " p.\"MinimumQuantity\" as minam , p.\"Vat\" , \"QuantityUnit\" as unit ,  p.\"StockBalance\" as stock from  \"Product\" p where \"Service\" = 0  and \"OutGoingProduct\" != 1 ";
//kood AS liik , nimetus AS nimi  FROM artryhm t
			if (isDebug()) {
			    System.out.println(cmd);
			}
			
			 Connection  con = DriverManager.getConnection(url);
         Statement sst = con.createStatement();
           ResultSet  rs = sst.executeQuery(cmd);

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String code = parent.make(rs.getString("code"));
		String name = rs.getString("Name");
	   
	  if (name != null && name.trim().length() > 2 ){
		    Product prod = new Product(kood);
		    name = name.trim();
		    prod.setEAN(parent.make(rs.getString("ean")));
			
		    System.out.println("name " + name);
    
                 if(prices.get(kood)!= null){
                	 prod.setPrice(Double.parseDouble((String)prices.get(kood)));
                 }
                 if(prodsInGroups.get(kood) != null) {
                	 prod.setGroup(prodsInGroups.get(kood)+"");
                 }
                try {
                	 prod.setInCases(rs.getDouble("minam"));
                }catch(Exception e) {
                	
                }
               
                
                int vat = rs.getInt("Vat");
                if(vat == 275 ) {
                	vat = 18;
                }else {
                	vat = 27;
                }
                
               // prod.setPrice(rs.getDouble("price"));
            
                
                String ean4 = "";
                if(prod.getEAN()!= null && prod.getEAN().length() > 6 ){
                	ean4 = prod.getEAN().substring(prod.getEAN().length()-4);
                }
                
               // name =  name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
                prod.setName(Converter.enCodeEst(name + " " + code + " " + ean4));
                
                String yh = rs.getString("unit");
                
                if(yh != null && yh.trim().length()>0){
                	if(yh.equals("362")) {
                		prod.setUnit("db");	
                	}else {
                		prod.setUnit("kg");	
                	}
                	
                }
                Double stock = rs.getDouble("stock");
                if(stock != null && stock > 0) {
                	prod.setStock(stock);
                }
            
                products.put(name, prod);
		         }
		    }
	    
            rs.close();
            sst.close();
            con.close();
            
            
 	} catch (Exception e) {
	    getParent().showError(e);
	}

 	  System.out.println("tooted l�pp");
	return products;
    }

    public Vector getPriceMatrixData()  {
        Vector lines = new Vector();
        String    sql = " select \"PriceRule\", \"Product\", \"Price\",  \"ValidFrom\" as algus , \"ValidTo\" as lopp , datediff (day, \"ValidFrom\", \"ValidTo\") as paevi from  \"PriceRulePrice\" pr INNER JOIN \"PriceRuleDetail\" d ON d.\"Id\" = pr.\"PriceRuleDetail\"  \r\n" + 
        		"     		 where \"PriceRule\" != 386 and Current_Date between \"ValidFrom\" and \"ValidTo\" and \"Price\" > 0 order by 1";
       
          try {
        	Connection  con = DriverManager.getConnection(url);
  	        Statement st = con.createStatement();
  	        ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

          
              String clGroup = rs.getString("PriceRule");
              String prodID = rs.getString("Product");
         double price = rs.getDouble("Price");
              
              String algus = rs.getString("algus");
              String lopp = rs.getString("lopp");
            int paevi = rs.getInt("paevi");
              
           
              MatrixLine mline = new MatrixLine();
              if(paevi < 90) {

                  if (lopp != null) {
    					mline.setEndDate(lopp.substring(0, 10));
    				}
    				if (algus != null) {
    					mline.setStartDate(algus.substring(0, 10));
    				}
    				
    			  } 
            
              if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                mline.setCliGroup(clGroup.trim());
              }

              if (prodID != null && prodID.trim().length() > 0) {
                mline.setProdID(prodID.trim());
              }
             

              if (price != 0) {
                mline.setPrice(price);
              }
              
              if( price>0 )
             lines.add(mline);
            }

            rs.close();
            st.close();
            con.close();  
          }
          catch (SQLException ex) {
          }
          System.out.println("pm lines " + lines.size());
        return lines;
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
     
        System.out.println("cp alugus");
    	String cmd =
    		    "select d.\"CustomerAddress\" as klient,r.\"Product\" as toode , r.\"Quantity\" as kogus, r.\"UnitPrice\" as hind ,\"VoucherDate\" as lastdate , \"VoucherNumber\" as lastdoc from \"CustomerDeliveryBillDetail\" r \r\n" + 
    		    " INNER JOIN \"CustomerDeliveryBill\" d  ON d.\"Id\" = r.\"CustomerDeliveryBill\"    where \"VoucherDate\" > current_date-50 order by 1,2,5 desc ";
	    
	HashSet hh = new HashSet();
	
	// Siia tee kuup�eva kontroll 
	if (isDebug()) {
	    System.out.println(cmd);
	}

	Vector custProds = new Vector();
	
	try {
		 Connection cc = DriverManager.getConnection(url);
		 Statement stm = cc.createStatement();
	     ResultSet	      rs = stm.executeQuery(cmd);

	    while (rs.next()) {
		String kliendikood = rs.getString("klient").trim();
		String tootekood = rs.getString("toode").trim();
		
		
        double kogus = Double.parseDouble(rs.getString("kogus").trim());
        double hind = Double.parseDouble(rs.getString("hind").trim());
        String kuupaev = rs.getString("lastdate");
        String doc = rs.getString("lastdoc").trim();

                if (kliendikood.length() > 1 && !hh.contains(kliendikood+"|"+ tootekood)) {
                  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,kuupaev.substring(5,10) ,hind ,0,0," ");
                  cp.setAbiKood(doc);
                  custProds.add(cp);
                }
                
                hh.add(kliendikood+"|"+ tootekood);
	    }

            rs.close();
            stm.close();
            cc.close();

	} catch (Exception e) {
	    getParent().showError(e);
	}
       
        System.out.println("cp lopp");
	return custProds;
    }

    public Vector getDebt(String agentCode) throws SQLException{
        Vector list = new Vector();
        System.out.println("debt algus");
        String sql = "select \"VoucherNumber\", \"Customer\", \"CustomerAddress\" , \"VoucherDate\" , \"PaymentDate\",  \"GrossValue\" - \"AcquitValue\"  as debt,"
        		+ " datediff(day , \"PaymentDate\" ,CURRENT_DATE ) as yle from \"CustomerStockOut\" where \"VoucherType\"  = 1 and \"AcquitValue\" != \"GrossValue\"";


                 //jee
     Connection   con = DriverManager.getConnection(url);
        Statement stmt = con.createStatement();
        ResultSet	      rs = stmt.executeQuery(sql);
  	    while(rs.next()){
          Debt debt = new Debt();
          debt.setDoknr(rs.getString("VoucherNumber").trim());
          debt.setPayerID(rs.getString("Customer").trim());
          debt.setCustID(rs.getString("CustomerAddress").trim());
          debt.setDate(rs.getDate("VoucherDate"));
          Date dd = rs.getDate("PaymentDate");
          debt.setSum(rs.getDouble("debt"));
      
         
        	  debt.setDuedate(dd);
        	  debt.setDue(rs.getString("yle"));
         
          list.addElement(debt);
        }
  	    rs.close();
  	    stmt.close();
  	    con.close();
  	    
  	    System.out.println("debt lopp");
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
        String sql = "SELECT RTRIM(nimi) AS nimi, tasudok AS number, (doksumma) AS summa FROM firma1.dok d " +
       " LEFT JOIN firma1.klient k ON  k.kood=d.saaja WHERE  pais2obj=? and kuupaev = current_date";

     StringBuffer sb = new StringBuffer();

     return sb.toString();
    }

    
    private NumberFormat NF = NumberFormat.getInstance();

    public void setBulkSales(Map agents){
/*
        
    	 String cmd =   "SELECT dok.pais2obj AS agent ,SUM(rid.hind*rid.kogus) AS sales FROM firma1.dok INNER JOIN firma1.rid ON  " +
    		        "  dok.dokumnr = rid.dokumnr INNER JOIN firma1.toode on rid.toode = toode.toode WHERE  " +
    		        "  length(trim(dok.klient)) > 1 AND length(trim(rid.toode)) > 2   AND  " +
    		        "  date_part('YEAR',dok.kuupaev)=date_part('YEAR',current_date) AND date_part('MONTH',dok.kuupaev)=date_part('MONTH',current_date)  " + 
    		        "  AND dok.doktyyp = 'G'  GROUP BY dok.pais2obj ";
          
          String cmd2 = "select objekt2 as agent,summa from firma1.eelarve where (ltrim(left(eelkuu,2))=ltrim(to_char(month(date()),'99')) or " +
          		" ltrim(left(eelkuu,2))='0'+ltrim(to_char(month(date()),'99'))) and (ltrim(right(eelkuu,4))=ltrim(to_char(year(date()),'9999')) " +
          		" or ltrim(right(eelkuu,4))='0'+ltrim(to_char(year(date()),'9999')))";
          

          try {

        	  Connection  con = DriverManager.getConnection(url);
  	    	  Statement stmt = con.createStatement();
          	ResultSet	      rs = stmt.executeQuery(cmd2);
            while(rs.next()) {
            	String agentID = rs.getString("agent").trim();
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                  agent.setMonthBudget((int)rs.getDouble("summa"));
                }}
              
            rs.close();
            stmt.close();
            con.close();
          }catch (Exception e ){
          	e.printStackTrace();
          }
       
          
          
      	            log(cmd);
          try {
        	  Connection  con = DriverManager.getConnection(url);
  	          Statement stmt = con.createStatement();
              ResultSet	      rs = stmt.executeQuery(cmd);

              while(rs.next()) {
                String agentID = rs.getString("agent");
                if(agentID != null){

                    Agent agent = (Agent)agents.get(agentID.trim());
                    if (agent != null){
                      agent.setMonthSales((int)rs.getDouble("sales"));
                      System.out.println(agent.toString() + " " + agent.getMonthSales());
                    }	
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

    public static final String DRIVER_VERSION = "KulcsSoft";
    private String	       url = "jdbc:odbc:dziugas";
    private Connection	       con = null;
    private Statement	       stmt;
    private String	       subjectFilesTo = null;
	public Vector getFixedAssortmentData() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}
	public void setNF(NumberFormat nF) {
		NF = nF;
	}
	public NumberFormat getNF() {
		return NF;
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

