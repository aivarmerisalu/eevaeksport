
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
public class EevaProBeauteDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public EevaProBeauteDBDriver(EksportPanel parent) {
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
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}    

    public String getSaveStockInfo(){
      
          StringBuffer products = new StringBuffer();

       String sql = "select s.toode , s.kogus  as asi from  " +
              " firma2.seisud s  where s.osak ='LADU'";
       if (isDebug()) {
           System.out.println(sql);
       }

       try {
    	   connectionUp();
        ResultSet rs = con.createStatement().executeQuery(sql);
        while (rs.next()) {

       
                  products.append(rs.getString("toode").trim() + "; ; ;");
                  products.append(rs.getString("asi").trim());               
                  products.append("\n");
        }
        rs.close();
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

    /**
     *
     */
    public void connectionUp(){
      if (con == null){
	try {
           con = DriverManager.getConnection(url,"postgres","eeta8joma");
           stmt = con.createStatement();
	} catch (Exception e) {
	    getParent().showError(e);
	    System.exit(0);
	}
      }
    }

        public String getDistribution() {
          return null;
        }

    public TreeMap getProdGroups() throws SQLException {
    	connectionUp();

    	System.out.println("tooegrupid algus");
        TreeMap   tm = new TreeMap();
        String    sql = "SELECT liik AS liik , artlnimi AS nimi  FROM firma2.artliik t order by artlnimi";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String liik=rs.getString("liik").trim();
          String nimetus=rs.getString("nimi").trim();
          if (liik != ""){
          tm.put(liik,Converter.enCodeEst( nimetus + " " + liik));
          }
        }
        rs.close();
        st.close();

System.out.println("tooegrupoid l�pp");
        return tm;
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
	String  cmd = "SELECT yksus,nimetus FROM firma2.yksus2";
	TreeMap agents = new TreeMap();

	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String nimi = rs.getString("nimetus").trim();
              agent.setIsPilot(true);
              agent.setName(nimi);
		agents.put(nimi, agent);
	    }
	    
	    
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return agents;
    }

    public TreeMap getCustomers(Agent agent) {
        connectionUp();

        System.out.println("kliendid algus");
        String cmd =
	    " SELECT k.kood, k.nimi,trim(tanav)+ ' '+piirkond AS aadress,trim(kontaktisi) +' ' +trim (telefon)  AS kontakt,  email," +
	  "  k.maksetin AS makse,max(protsent) as ale, " +
	   " 	 SUM(iif(date()>d.tasukuup,d.tasumata,0)) AS tasumata,0 AS limit, " +
	   "    0 as contrend, '' AS visit,max(k.maksja) as maksja,max(k.objekt2) AS agent, max(k.objekt7) AS abiagent ,max(liik) as liik  FROM firma2.klient k LEFT JOIN firma2.dok d " + 
	   " ON k.kood=d.saaja and  d.doktyyp='G' " +
	"	 left join firma2.hkpais h on h.hinnak=k.hinnak " + 
	" GROUP BY k.kood, k.nimi order by k.kood ";


	log(cmd);

	TreeMap customers = new TreeMap();

	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
            String kood = rs.getString("kood").trim();
		Customer cust = new Customer(kood);
		String nimi = rs.getString("nimi").trim();
                cust.setName(Converter.enCodeEst(nimi));
                cust.setDebt(rs.getDouble("tasumata"));
                cust.setVisit(rs.getString("visit"));
                	cust.setAddress(Converter.adressLink(Converter.enCodeEst(parent.make(rs.getString("aadress"))),"Estonia"));
                	cust.setPersonalData(Converter.enCodeEst(parent.make(rs.getString("kontakt")) + " " + parent.make(rs.getString("email"))));
                	
                	cust.setPaymTerm(parent.make(rs.getString("makse")));
                    cust.setDiscount(rs.getDouble("ale"));
                
                
                
                String limit = rs.getString("limit");
                if(limit.trim().length()>0){
                 //cust.setCredit(limit.trim());	
                }
             
                String over = rs.getString("contrend");
                if(over.trim().length()>0){
                // cust.setCredit(over);	
                }
                
                String payer = rs.getString("maksja");
                if(payer != null && payer.trim().length()>1){
                  cust.setPayer(payer.trim());
                } else {cust.setPayer(kood);}
                
                String ag = rs.getString("agent");
                if (ag != null && ag.trim().length()>0){
                	cust.addAgent(new Agent(ag.trim()));
                }
                String ag2 = rs.getString("abiagent");
                if (ag2 != null && ag2.trim().length()>0){
                	cust.addAgent(new Agent(ag2.trim()));
                }
                String liik = rs.getString("liik");
                if (liik != null && liik.trim().length()>0){
                	cust.setGroup(liik.trim());
                }
                
                customers.put(nimi, cust);
	    }
	} catch (Exception e) {
	    getParent().showError(e);
	}
	  System.out.println("kliendid l�pp");
	return customers;
    }

    public TreeMap getProducts(Agent agent) {
        connectionUp();

    	TreeMap products = new TreeMap();
    	TreeMap stocks = new TreeMap();
    	try {
        
    		
            String sql = "select toode,kogus as kokku from firma2.seisud   where osak='LADU'  " ;
           
           this.connectionUp(); 
 PreparedStatement pstm = con.prepareStatement(sql);
 ResultSet rs6 = pstm.executeQuery();

while(rs6.next()){
  stocks.put(rs6.getString("toode").trim(),rs6.getDouble("kokku")+ "");
 }


rs6.close();
pstm.close();


System.out.println("stocks " + stocks.size());
StringBuffer buf = new StringBuffer();

          int counter =0 ;
           String cmd = " SELECT toode.toode, toode.nimetus, toode.tykke, toode.myygihind ,toode.norm,ribakood AS ean, " +
           	"    yhik,liik AS liik FROM firma2.toode  toode  WHERE  length(trim(toode.toode))>2 AND grupp='L' " +
           	"   and saadakaalu = false and length(trim(toode.nimetus))>2 " +
            "   ORDER BY toode.nimetus  ";
//kood AS liik , nimetus AS nimi  FROM artryhm t
			if (isDebug()) {
			    System.out.println(cmd);
			}
         Statement sst = con.createStatement();
           ResultSet  rs = sst.executeQuery(cmd);

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String name = rs.getString("nimetus").trim();
		
		// if ( kood.length() > 2 ){
		Product prod = new Product(kood);
		        name = "    " + name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
                prod.setName(Converter.enCodeEst(name + " " + kood));
    
                 if(stocks.get(kood)!= null){
                	 prod.setStock(Double.parseDouble((String)stocks.get(kood)));
                 }
                      
                          
                          
                prod.setInCases(rs.getDouble("tykke"));
                prod.setPrice(rs.getDouble("myygihind"));
                prod.setEAN(parent.make(rs.getString("ean")));
                String yh = rs.getString("yhik");
                
                if(yh != null && yh.trim().length()>0){
                	prod.setUnit(yh.trim());
                }
                
                prod.setGroup(parent.make(rs.getString("liik")));
                if(prod.getStock() >0){
                	  buf.append(kood + ";" + name +  ";" + prod.getStock() + "\n");	
                }
              
                	products.put(name, prod);
		         }
            rs.close();

            if(buf.length() > 2){
                File f = new File(  "mgshop/products.txt");
                if (f.exists())f.delete();
                  try {
            		PrintWriter os = new PrintWriter(new FileWriter(
            				 "mgshop/products.txt", true));
            		os.write(buf.toString());
            		os.close();
            	  }catch (IOException ex) {
            		ex.printStackTrace();
            	  }
             }
            
            
 	} catch (Exception e) {
	    getParent().showError(e);
	}

 	  System.out.println("tooted l�pp");
	return products;
    }

    public Vector getPriceMatrixData()  {
        Vector lines = new Vector();
        String    sql = " SELECT mt.klient AS custid,mt.klliik AS clgroup,toode AS prodid,mt.artliik AS prodgroup,p.protsent AS discount,mt.hind  " + 
         " AS price FROM firma2.hinnamtr mt inner join firma2.hkpais p on p.hinnak = mt.hinnak where " +
         "algus <= current_date and current_date<=lopp order by mt.klient,mt.klliik,mt.toode";
          Statement st = null;
          try {
            connectionUp();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

              String custID = rs.getString("custid");
              String clGroup = rs.getString("clgroup");
              String prodID = rs.getString("prodid");
              String prodGroup = rs.getString("prodgroup");
              double discount = rs.getDouble("discount");
              double price = rs.getDouble("price");
              MatrixLine mline = new MatrixLine();

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
                mline.setDiscount(discount);

              if (price != 0) {
                mline.setPrice(price);
              }
              if(price>0 || discount >0)
             lines.add(mline);
            }

            rs.close();
            st.close();
            this.connectionDown();
          }
          catch (SQLException ex) {
          }
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
        connectionUp();
        System.out.println("cp alugus");
    	String cmd =
    		    "SELECT dok.klient,rid.toode,dok.kuupaev,max(rid.kogus) as kogus,max(rid.hind)  as hind FROM firma2.dok INNER " +
    		    "JOIN firma2.rid ON dok.dokumnr = rid.dokumnr INNER JOIN firma2.toode on rid.toode = toode.toode WHERE " +
    		    " length(trim(dok.klient)) > 1 AND length(trim(rid.toode)) > 2  " +
    		    "  AND dok.kuupaev >= current_date -180"
	    + " AND  dok.doktyyp = 'T'     " +
    		    " GROUP BY dok.klient, rid.toode,dok.kuupaev  ORDER BY dok.klient,rid.toode,dok.kuupaev ";
	    
	
	
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
		 String kuupaev = rs.getString("kuupaev");
        double kogus = Double.parseDouble(rs.getString("kogus").trim());
     

                if (kliendikood.length() > 1) {
                  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,kuupaev.substring(5,10),0,0,0," ");
                  custProds.add(cp);
                }
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
        String sql = "SELECT klient,tasudok AS number,tasukuup AS maksepaev,tasumata, kuupaev ,tasukuup-date() as yle FROM " +
                     " firma2.dok WHERE   doktyyp='G' AND tasumata<>0 " +
                     "ORDER BY klient";


                 //jee
        con = DriverManager.getConnection(url);
        Statement stmt = con.createStatement();
        ResultSet	      rs = stmt.executeQuery(sql);
  	    while(rs.next()){
          Debt debt = new Debt();
          debt.setCustID(rs.getString("klient").trim());
          debt.setDoknr(rs.getString("number").trim());
          Date dd = rs.getDate("maksepaev");
         
         
          
          debt.setSum(rs.getDouble("tasumata"));
          debt.setDate(rs.getDate("kuupaev"));
          if(dd == null){
        	  debt.setDuedate(new Date(Calendar.getInstance().getTimeInMillis()));
        	  debt.setDue("0");
              
          }else{
        	  debt.setDuedate(dd);
        	  debt.setDue(rs.getString("yle"));
                   	  
          }
          
          
          list.addElement(debt);
        }
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
        connectionUp();
	double  salary = 0;
        String  sql = "SELECT dok.pais2obj AS agent ,SUM(dok.doksumma/1.20) AS sales FROM dok  WHERE " +
        "  YEAR(dok.kuupaev)=YEAR(date()) AND MONTH(dok.kuupaev)=MONTH(date())  " +
        "  AND dok.doktyyp = 'G' and dok.pais2obj=? GROUP BY dok.pais2obj" ;
                      

        
	if (isDebug()) {
	    System.out.println(sql);
	}

      try {
        PreparedStatement st = con.prepareStatement(sql);
        st.setString(1, agentCode);
        ResultSet rs = st.executeQuery();
        while(rs.next()){
          salary = rs.getDouble("sales");
        }
        rs.close();
        st.close();
        
        String cmd2 = "select objekt2 as agent,summa from eelarve where (ltrim(left(eelkuu,2))=ltrim(str(month(date()))) or" +
        		" ltrim(left(eelkuu,2))='0'+ltrim(str(month(date())))) and objekt2 = ? and (ltrim(right(eelkuu,4))=ltrim(str(year(date())))" +
        		" or ltrim(right(eelkuu,4))='0'+ltrim(str(year(date()))))";

        st = con.prepareStatement(cmd2);
        st.setString(1, agentCode);
        rs = st.executeQuery();
        while(rs.next()){
         this.setBudget(rs.getDouble("eelarve"));
        }

        rs.close();
        st.close();

      } catch (SQLException e){
        getParent().showError(e);
      }

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
        String sql = "SELECT RTRIM(nimi) AS nimi, tasudok AS number, (doksumma) AS summa FROM firma2.dok d " +
       " LEFT JOIN firma2.klient k ON  k.kood=d.saaja WHERE  pais2obj=? and kuupaev = current_date";

StringBuffer sb = new StringBuffer();
  this.connectionUp();
try {
PreparedStatement stmt1 = con.prepareStatement(sql);
 stmt1.setString(1, agent.getID());
 String sep= System.getProperty("line.separator");
 ResultSet	      rs = stmt1.executeQuery();
 String header = getParent().getString("name") + "  \t\t" + getParent().getString("number") +
 "\t" + getParent().getString("sum") + sep + "-------------------------------------------" + sep;
 boolean heade = true;
 while(rs.next()){
 	if (heade){
 		sb.append(header);
 		heade = false;
 	}
 	
     String nimi = rs.getString("nimi");
     if (nimi == null){
     nimi = " ";
     } else {nimi = nimi.trim();}

 	sb.append(nimi + "\n");
   sb.append(rs.getString("number").trim() + "\t");
   String sum = getNF().format(Double.parseDouble(rs.getString("summa").trim())) ;
   sb.append(sum + "\n" + sep);
 }
} catch (Exception e) {
 getParent().showError(e);
}

connectionDown();

System.out.println(sb.toString());
return sb.toString();

    }
    private NumberFormat NF = NumberFormat.getInstance();

    public void setBulkSales(Map agents){

        
    	 String cmd =   "SELECT dok.pais2obj AS agent ,SUM(rid.hind*rid.kogus) AS sales FROM firma2.dok INNER JOIN firma2.rid ON  " +
    		        "  dok.dokumnr = rid.dokumnr INNER JOIN firma2.toode on rid.toode = toode.toode WHERE  " +
    		        "  length(trim(dok.klient)) > 1 AND length(trim(rid.toode)) > 2   AND  " +
    		        "  date_part('YEAR',dok.kuupaev)=date_part('YEAR',current_date) AND date_part('MONTH',dok.kuupaev)=date_part('MONTH',current_date)  " + 
    		        "  AND dok.doktyyp = 'G'  GROUP BY dok.pais2obj ";
          
          String cmd2 = "select objekt2 as agent,summa from firma2.eelarve where (ltrim(left(eelkuu,2))=ltrim(to_char(month(date()),'99')) or " +
          		" ltrim(left(eelkuu,2))='0'+ltrim(to_char(month(date()),'99'))) and (ltrim(right(eelkuu,4))=ltrim(to_char(year(date()),'9999')) " +
          		" or ltrim(right(eelkuu,4))='0'+ltrim(to_char(year(date()),'9999')))";
          
          connectionUp();
          try {
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
            
          }catch (Exception e ){
          	e.printStackTrace();
          }
       
          
          
      	            log(cmd);
      	          connectionUp();
          try {
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

    public static final String DRIVER_VERSION = "EevaProBeaute";
    private String	       url = "jdbc:odbc:eeva";
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
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

