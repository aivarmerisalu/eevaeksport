/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DefectiveWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class KalevIntrFelDBDriver extends AbstractDBDriver implements Manager,Promo, BulkUpdater,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public KalevIntrFelDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	}
    }

    public String getAssortmentStr(Agent agent){
    	return null;
    }
            public String getDealerCustomerCSV(String agent){
             return null;
             }
            public String getSaveDebtInfo() {
               
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
      return DriverManager.getConnection(url, "mobilejava", "mobilejava");
    }

    /**
     * 
     *
     */
    public void connectionDown(){
    }


    public String getSaveStockInfo(){
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

        public TreeMap getProdGroups() throws SQLException {
        	connectionUp(url);
            TreeMap   tm = new TreeMap();
            String    sql = " SELECT SY24002 AS liik ,SY24003 AS nimetus FROM SY242100 WHERE RTRIM(SY24003)<>'' AND SY24001 = 'IF' ORDER BY SY24003";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
              String kood=  rs.getString("liik").trim();
                tm.put(kood,
                       rs.getString("nimetus").trim() +" " + kood);
            }
            rs.close();
            st.close();
            con.close();
            this.connectionDown();
            return tm;
     }



        public String getDistribution() {
          return null;
        }



    public TreeMap getAgents() {
        String  cmd = "select SL01001 as kood , SL01002 As nimi from SL012100 where SL01001 like '%INTR5%' ";
	TreeMap agents = new TreeMap();

	try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("kood").trim());
              String nimi = rs.getString("nimi").trim();
              agent.setName(nimi);

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
    
    public void insertDefective(DefectiveWrapper wr){

      }


    

    public TreeMap getCustomers(Agent agent){
        connectionUp(url);
        connectionUp2(url);
        String cmd = "SELECT SL01002 AS nimi,SL01001 AS kood,(SL01003 + ' (' + SL01004 + ')') AS aadress, SC45004 AS ale,SL01036 AS ale2 , " +
         "  ' ' AS visit, ' ' AS info ,SL01035 as agent,SL22005 as paym , SL01029 as klgrupp, SL01075 as block ,SL01056 as payer ,SL14007 as delday " +
         "FROM SL012100 left JOIN SC452100  " +
		 "  ON SL01036=SC45003    LEFT OUTER JOIN SL142100 ON SL01001=SL14001 AND SL14002='00'" +
         " LEFT OUTER JOIN SY242100 ON SL01035=SY24002 AND SY24001='BK'" +
         " LEFT OUTER JOIN SL222100 ON SL01024=SL22002 AND SL22001='0'  " +
         " WHERE len(SL01001)>4 and ( SL01001 like '%INTR5%' or SL01001 like '%INTR9%' or SL01001 like '%M10217%' or SL01001 like '%M12320%') ORDER BY SL01002";
       
        
        
        
	log(cmd);

	TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        connectionUp(url);
	try {
           PreparedStatement stmt = con.prepareStatement(cmd);

	   	    ResultSet  rs = stmt.executeQuery();

	    while (rs.next()) {
		String nimi = rs.getString("nimi").trim();
                Customer cust = new Customer(rs.getString("kood").trim());
                cust.setAddress(rs.getString("aadress").trim());
                  nimi = nimi + " " + cust.getAddress() + " " + cust.getID();
                
                cust.setName(nimi);
         
                  cust.addAgent(new Agent(cust.getID()));
                  cust.addAgent(new Agent("INTR"));
                
                String paym = rs.getString("paym");
                if(paym != null && paym.trim().length()>0){
                	cust.setPaymTerm(paym.trim());
                }
             
             
                String payer = rs.getString("payer");
                if(payer != null && payer.trim().length()>0){
                	cust.setPayer(payer);              	
                }

                String delday = rs.getString("delday");
                if(delday != null && delday.trim().length()>0){
                	cust.setPersonalData(delday.trim().replaceAll(";", ":"));              	
                }
          
        if(!nimi.startsWith("*")){
        	  customers.put(cust.getName(), cust);
              custMap.put(cust.getID(), cust);
        }
              
	    }
            rs.close();
            stmt.close();



        } catch (Exception e) {
            getParent().showError(e);
        }

        
	return customers;
    }


    public Vector getDebt(String agentCode) throws SQLException{
        Vector list = new Vector();
        return list;
      }

    
    public Vector getPriceMatrixData()  {  
    	
    	   String    sql = "SELECT SL01001 AS custid,'' AS clgroup,SUBSTRING(SC18002,11,35) AS prodid,'' AS prodgroup,SC18009, " +
           "	(case when SY24003=null then '0' else SY24003 end ) AS discount,SC18008 AS price  " +
           "   FROM SC182100 INNER JOIN SL012100 ON  left(SC18002,7)= SL01001 LEFT JOIN SY242100 on SC18009=SY24002 and SY24001='BG' WHERE " +
            "  SC18001='C' AND CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011   and len(SL01008) >0  " +
            " order by SL01001,SUBSTRING(SC18002,11,35)";

           Vector result = new Vector();
             try {
              
            	 connectionUp(url);
             Statement  st = con.createStatement();
               ResultSet rs = st.executeQuery(sql);

               while (rs.next()) {

                 String custID = rs.getString("custid").trim();
                 String clGroup = rs.getString("clgroup");
                 String prodID = rs.getString("prodid").trim();
                 String prodGroup = rs.getString("prodgroup");
                 String sss = rs.getString("SC18009");
                 String discount = rs.getString("discount");
                 if(discount == null){
                 	discount = sss.trim();
                 }else{
                 	discount = discount.trim().replaceAll("%"," ").trim();
                 }
   			   
                 double price = rs.getDouble("price");
                 MatrixLine mline = new MatrixLine();

                 if (custID != null && custID.length() > 0) {
                   mline.setCustID(custID);
                 }
                 if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                   mline.setCliGroup(clGroup.trim());
                 }

                 if (prodID != null && prodID.length() > 0) {
                   mline.setProdID(prodID);
                 }
                 if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                   mline.setProdGroup(prodGroup.trim());
                 }
                 if(discount.trim().length()==0){
                 	discount = "0";
                 }
                 double disc = 0; 
                 try{
                	disc = Double.parseDouble(discount.replaceAll(",", "."));
                 }catch(Exception e){
                	// e.printStackTrace();
                 }
                 mline.setDiscount(disc);

                 if (disc == 0 && price != 0) {
                   mline.setPrice(price);

                 }
                 if(mline.getPrice() > 0 || mline.getDiscount() > 0){
                     result.add(mline);    
                 }
                 
               }

               
               String sql2 = "select '' AS custid ,SC39002 AS clgroup , rtrim(ltrim(SC39001))  AS prodid,' ' AS prodgroup, " +
               " case when SC39006 <= getdate() AND (SC39007 >= getdate() OR SC39007 = null) then " +
               " SC39005 else SC39004 end AS price  " +
               " FROM SC392100 WHERE SC39002 != '00' order by SC39002,SC39001 ";
               rs = st.executeQuery(sql2);

               while (rs.next()) {

                 String clGroup = rs.getString("clgroup");
                 String prodID = rs.getString("prodid").trim();
                 String discount = "0";       
   			   
                 double price = rs.getDouble("price");
                 MatrixLine mline = new MatrixLine();
               
                 if (clGroup !=null && clGroup.trim().length() > 0) {
                   mline.setCliGroup(clGroup.trim());
                 }

                 if (prodID != null && prodID.trim().length() > 0) {
                   mline.setProdID(prodID.trim());
                 }
     
                 if (Double.parseDouble(discount) == 0 && price != 0) {
                   mline.setPrice(price);

                 }
                result.add(mline);  // Kommentisin v�lja kuniks uus hinna skeem hakkab toimima
               }
               
               
               
               
             }catch(Exception e){
            	 e.printStackTrace();
             }
    	
        return result;
      }

    public TreeMap getProducts(Agent agent){
        connectionUp(url);

        String cmd ="SELECT  RTRIM(SC01001) AS toode,(RTRIM(SC01002)+'      ' " +
            "    +rtrim(SC01003)) + '     ' +RTRIM(SC01001) AS nimi,SC39004 AS myygihind ,SC01015 AS tykke,SC03003-SC03004 AS stock, " +  
    " SC01115  AS grupp,SC01132 AS ean  , SC01016 as letter ,SC01014 as sorting " +
    " , case when SC01135 = 0 or SC01001 = '08060-6,25' then  'KG' else 'TK' end as unit  FROM SC012100 left outer JOIN SC032100 on SC03001=SC01001 and SC03002='01' AND SC03003!=0 left outer JOIN SC392100 ON SC01001 " +
    "  = SC39001 and SC39002='00'  WHERE   LEN(LTRIM(SC01037))>0 and SC39004 > 0 and rtrim(ltrim(SC01016)) != 'S' and SC01014 != ''  ORDER BY LEFT(RTRIM(SC01037),1),SC01001 " ;
	if (isDebug()) {
	    System.out.println(cmd);
	}

	TreeMap products = new TreeMap();

	try {
          Statement st = con.createStatement();
	    ResultSet	      rs = st.executeQuery(cmd);

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String nimetus = rs.getString("nimi").trim();

                Product prod = new Product(kood);
             
                prod.setPrice(parseDouble(rs.getString("myygihind").trim()));

                prod.setInCases(parseDouble(rs.getString("tykke").trim()));
               prod.setStock(rs.getDouble("stock"));

                if(kood.indexOf("-EE") > -1 ){
                  prod.setStatusDiscount("1");
                }
                String gr = rs.getString("grupp");
                if(gr != null && gr.trim().length()>0){
                  prod.setGroup(gr.trim());
                }
                prod.setName("      " + gr.trim() + " " + nimetus );
                 gr = rs.getString("ean");
                if(gr != null && gr.trim().length()>0){
                  prod.setEAN(gr.trim());
                }
                
                String unit = rs.getString("unit");
                if(unit != null && unit.trim().length()>0){
                    prod.setUnit(unit.trim());
                  }
           
              prod.setVat("20"); 
            
  		        products.put(nimetus, prod);  

	    }
            rs.close();
            st.close();
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
    private void connectionUp(String url){
        try {
          if (con != null){
            con.close();
          }
             con = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
             getParent().getProps().getProperty("dsn.pass"));
        } catch (Exception e) {
  	    getParent().showError(e);
        }

      }
    
    private void connectionUp2(String url){
        try {
          if (con2 != null){
            con2.close();
          }
             con2 = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
             getParent().getProps().getProperty("dsn.pass"));
        } catch (Exception e) {
  	    getParent().showError(e);
        }

      }
    
    private Connection getNewConnection2() throws SQLException{
        return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
                getParent().getProps().getProperty("dsn.pass"));
      }
    
    
    public Vector getCustomerProductsList(String agentCode) {
        connectionUp(url);

  
	  HashMap kliendid = new HashMap();
	Vector custProds = new Vector();
      
          try {
        	  

  
      
    //  Siin Paneme sortimendid Sisse 
            
          String sql2 = "SELECT  SL01001 AS klient ,ST03017 AS toode ,ST03020 AS Kogus,ST03015  as lastdate ,max(ST03014) as arvenr " +
          "  FROM ST032100 INNER JOIN SL012100 ON ST03001 = SL01001 WHERE ST03015> ? AND ST03016>0 AND ST03020>0 " +
          " GROUP BY ST03017,SL01001, ST03020,ST03015 ORDER BY SL01001,ST03017 ,ST03015 ASC";

					if (isDebug()) {
					System.out.println(sql2);
					}
          
          
          stmt = con.prepareStatement(sql2);
          stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
          
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
              String kliendikood = rs.getString("klient").trim();
              String tootekood = rs.getString("toode").trim();
              double kogus = Double.parseDouble(rs.getString("kogus").trim());
              Date date = rs.getDate("lastdate");
              String arvenr = rs.getString("arvenr");
         
                  CustProd cp = new CustProd(kliendikood, tootekood, kogus,0 ,0,
                  		date.toString().substring(5,10),0,0,0," ");
                  cp.setAbiKood(arvenr);
                    custProds.add(cp);  
                    Product prod = new Product(tootekood);
                    Customer cust = (Customer)kliendid.get(kliendikood);
                    if (cust == null){
                        cust = new Customer(kliendikood);
                      }
                    cust.addProduct(prod,cp,false);
                      kliendid.put(kliendikood, cust);
                     
              
            }
            rs.close();
            stmt.close();

            
        
           
        
            
            
            
            
   
            
          
            

   int counter = 0;
   int cou = 0;
   String oldklient = null;
   String olddate = " ";
   String oldtoode = null;

  String cmd2 =  " SELECT  SL01001 AS klient , ST03017 AS toode ,sum(ST03020) AS Kogus,ST03015  as lastdate " + 
                     "  FROM ST032100 INNER JOIN SL012100 ON ST03001 = SL01001 WHERE ST03015> getdate()-180 AND ST03016>0 AND ST03020>0 " +				 
					 "  group by SL01001,ST03017,ST03015 " +
                     "  ORDER BY SL01001,ST03017 ,ST03015 DESC ";
  System.out.println(cmd2);
   //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"

               con2 = getNewConnection2();
  Statement stmt6 = con2.createStatement();
   ResultSet  rs2 = stmt6.executeQuery(cmd2);


while (rs2.next()){
String klient = rs2.getString("klient").trim();
       String toode = rs2.getString("toode").trim();
       String kogus = "0";
       String date = " ";
          kogus = rs2.getDouble("kogus")+"";
          kogus = kogus.replace('.',',');
		  if(kogus.indexOf(",") != -1){
		  	kogus=kogus.substring(0,kogus.indexOf(","));
		  }
           date = rs2.getString("lastdate").trim().substring(0,10);

       if(cou == 0){oldklient = klient;olddate = date;oldtoode=toode;}
       if(!oldklient.equals(klient)){
                     counter = 0;
                     oldklient = klient;
                     olddate = date;
       }
       if(!toode.equals(oldtoode)){
       	counter = 0;
       }
       Customer c = (Customer)kliendid.get(klient);
       if (c != null ){

         ArrayList al = (ArrayList)c.getProducts().get(toode);
         if (al != null){
           CustProd cp = (CustProd)al.get(1);
           if (cp != null){
            if(!olddate.equals(date)){counter++;}
             if (counter == 0){cp.setS1(kogus );}
             if (counter == 1){cp.setS2(kogus );}
             if (counter == 2){cp.setS3(kogus );}
             if (counter == 3){cp.setS4(kogus );}
             if (counter == 4){cp.setS5(kogus );}

             custProds.add(cp);
           }
         }
         oldklient = klient;
         olddate = date;
         oldtoode = toode;
       }
       cou++;
     }


rs2.close();
con2.close();
    
            
          } catch (Exception e) {
            getParent().showError(e);
          }
        
          
          
          
          
          
        connectionDown();
        System.out.println("CustProd l�peb");
	return custProds;
    }

    public String getPilotDayData(Agent agent){
      return null;
    }


    public String getNonMobileOrders(Agent agent){
      return null;
    }

    public void setBulkSales(Map agents){

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


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
    }


    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,docsum AS summa,linecount AS ridu,text AS tekst," +
                   "sentlong AS kuupaev FROM dayreport WHERE sentdate>(DATE()-30) ORDER BY clid";
      Connection c = DriverManager.getConnection(url2);
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(sql);


      String sql2 = "SELECT SL01001 AS kood, SL01040 AS aastamyyk,SL01041 AS kuumyyk,SY24003 AS agendinimi, SL01006 AS kontaktisik " +
                    ",SL01011 AS telefon, SL01024 AS maksetin,SL01036 AS ale" +
                    " FROM SL012100 LEFT JOIN SY242100 ON SY24002=SL01035 ORDER BY " +
                    "SL01002";
      connectionUp();
      Connection con = getNewCon();
      Statement st2 = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs2 = st2.executeQuery(sql2);

      StringBuffer sb = new StringBuffer();
      String sep = ";";
      while(rs.next()){
        String cliID = rs.getString("klient").trim();
        sb.append(cliID + sep);
        sb.append(rs.getString("kuupaev").trim() + sep);
        sb.append(rs.getDouble("summa") + sep);
        sb.append(rs.getInt("ridu") + sep);
        String tekst = rs.getString("tekst");
        tekst = tekst.replace('\'', ' ').replace('\"', ' ');
        if (tekst == null) tekst = " ";
        else tekst = tekst.trim();
        sb.append(tekst + sep);

        boolean has = scrollTo(cliID, rs2);
        if (has){
         sb.append(ensureLen(rs2.getString("aastamyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("kuumyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
         sb.append(ensureLen(rs2.getString("kontaktisik"), " ") + sep);
         sb.append(ensureLen(rs2.getString("telefon"), " ") + sep);
         sb.append(ensureLen(rs2.getString("maksetin"), " ") + sep);
         sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
        }

        sb.append("\n");
      }

      ManagerData md = new ManagerData();
      md.setClientVisitInfo(sb.toString());

      sql = "SELECT OR20003 AS klient,OR20019 AS agent,OR20017 AS agentname FROM OR202100 " +
          "WHERE OR20015>? GROUP BY OR20003,OR20019,OR20017 ORDER BY OR20003";

      PreparedStatement ps = con.prepareStatement(sql);
      ps.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
      rs = ps.executeQuery();
      StringBuffer bf2 = new StringBuffer();
      while(rs.next()){
        bf2.append(rs.getString("klient").trim() + sep);
        bf2.append(rs.getString("agent").trim() + sep);
        String agentName = rs.getString("agentname");
        if (agentName == null) agentName = "";
        bf2.append(agentName + "\n");
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

    private static double parseDouble(String s){
      if (s.length() > 0) return Double.parseDouble(s);
      else return 0;
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

    public static final String DRIVER_VERSION = "KalevIntr";
    private String             url = "jdbc:odbc:scalaDB";
    private String             url2 = "jdbc:odbc:report";
   
    private Connection	       con = null;
    private Connection	       con2 = null;
    private String	       subjectFilesTo = null;
    private PreparedStatement  stmt = null;
    private NumberFormat NF = NumberFormat.getInstance();
	public Vector getFixedAssortmentData() {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

