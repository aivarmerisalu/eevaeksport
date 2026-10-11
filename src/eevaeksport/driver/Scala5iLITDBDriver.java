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
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
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
import eevaeksport.manager.ManagerData;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class Scala5iLITDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Promo,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public Scala5iLITDBDriver(EksportPanel parent) {
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
    public String getProductSortingData(){
       	return null;
       }
    /**
     *
     */
    public void connectionUp(){
      connectionUp(this.url);
    }
    public String getSaveDebtInfo() {
       	return null;
       }
       
    public String getAssortmentStr(Agent agent){
    	return null;
    }

            public String getDealerCustomerCSV(String agent){
             return null;
             }
        	public Vector getLastThreeData() {
        		// TODO Auto-generated method stub
        		return null;
        	}

        public String getSaveStockInfo(){
        	  connectionUp(url);
        	  String  cmd = "  SELECT  SC03001 AS toode , '01' AS LADU ,SC03003 AS stock   " +
		      "  FROM SC030300  WHERE SC03002='01' AND SC03003!=0 ORDER BY SC03001" ;


		       if (isDebug()) System.out.println(cmd);
		       StringBuffer products = new StringBuffer();
		       try {

		         Statement st = con.createStatement();
		         ResultSet rs = st.executeQuery(cmd);

		         while (rs.next()) {

		           String kood = rs.getString("toode").trim();
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
        


                                           public String getCustProdDiscounts(Agent agent){
                                            return null;
                                            }


        public String getDistribution() {
          return null;
        }

        public Vector getFixedAssortmentData() {
          return null;
        }


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

    /**
     *
     */
    public void connectionDown(){
        try {
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

    public TreeMap getProdGroups() throws SQLException {
    	connectionUp(url);
        TreeMap   tm = new TreeMap();
        String    sql = " SELECT SY24002 AS liik ,SY24003 AS nimetus FROM SY240300 WHERE RTRIM(SY24003)<>'' AND SY24001 = 'IB' ORDER BY SY24003";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String kood= rs.getString("liik").trim();
            tm.put(kood,
                   rs.getString("nimetus").trim() +" " + kood);
        }
        rs.close();
        st.close();
        con.close();
        this.connectionDown();
        return tm;
 }

    public TreeMap getAgents() {
        connectionUp(url);
        String  cmd = "SELECT SY24002 AS yksus,SY24003 AS nimi FROM SY240300 WHERE SY24001='BK'";
	TreeMap agents = new TreeMap();

	try {
            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String nimi = rs.getString("nimi").trim();
              agent.setName(nimi);
              agent.setObject("01");
              agents.put(nimi, agent);
	    }
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return agents;
    }

    public Vector getPriceMatrixData()  {
         Vector lines = new Vector();
         String    sql = " SELECT left(SC18002,4) AS custid,'' AS clgroup,SC18003 AS prodid,'' AS prodgroup,SC18009, " +
         		"(case when SY24003=null then '0' else SY24003 end ) AS discount,SC18008 AS price " +
                " FROM SC180300  LEFT JOIN SY240300 on SC18009=SY24002 and SY24001='BG' WHERE  " +
                " SC18010 <= getdate() AND (SC18011 >= getdate() OR SC18011 = null)  ";
           Statement st = null;
           try {
             connectionUp(url);
             st = con.createStatement();
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
               }
               if(discount.trim().length()==0){
               	discount = "0";
               }
                 mline.setDiscount(Double.parseDouble(discount));

               if (Double.parseDouble(discount) == 0 && price != 0) {
                 mline.setPrice(price);

               }
              lines.add(mline);
             }

             rs.close();
             st.close();
             connectionDown();
           }
           catch (SQLException ex) {
             ex.printStackTrace();
           }
         return lines;
       }






    public TreeMap getCustomers(Agent agent){
        connectionUp(url);
        String cmd = "SELECT SL01002 AS nimi,SL01001 AS kood,(SL01003 + ' (' + SL01004 + ')') AS aadress, SC45004 AS ale,SL01036 AS ale2 , " +
        "    SL01009 AS visit, ' ' AS info ,SL01035 as agent , SL01029 as klgrupp , SL01056 as alegrupp ,(case when SL01060=1 or SL01075=1 " +
        "   then 1 else 0 end) as blokk , SL01056 as payer  FROM SL010300 left JOIN SC450300   " +
		"   ON SL01036=SC45003 and SC45002='00' WHERE SL01002 != '' ORDER BY SL01002 ";
	log(cmd);

	TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();

	try {
            stmt = con.prepareStatement(cmd);
            if (agent != null){
//              stmt.setString(1, agent.getID());
            }
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
		String nimi = rs.getString("nimi").trim();
                Customer cust = new Customer(rs.getString("kood").trim());
                nimi = nimi + " " + cust.getID();
                cust.setName(nimi);
                cust.setAddress(rs.getString("aadress").trim());
                String ale = rs.getString("ale");
                if (ale != null && ale.length() > 0){
                  ale = ale.replaceAll("%"," ");
                   ale = ale.replace('%',' ');
                 // System.out.println(ale);
                  cust.setDiscount(Double.parseDouble(ale.trim()));
                }
                
                String ale2 = rs.getString("ale2");
                System.out.println(cust.getName() + ale2);
                if (ale2 != null  && ale2.length() > 0){
                 ale2 = ale2.replaceAll("%"," ");
                  ale2 =  ale2.replace('%',' ');
                  System.out.println(cust.getName() + ale2);
                  cust.setDiscount(Double.parseDouble(ale2.trim()));
                }
                
                cust.setVisit(rs.getString("visit").trim());
               // cust.setInfo(rs.getString("info").trim());
                String agen = rs.getString("agent");
                if(agen != null){
                  cust.addAgent(new Agent(agen.trim()));
                }
          
                
                //lisame k�igile k�ik agendid 
                Enumeration enums = getParent().getAgentsHash().elements();
                while (enums.hasMoreElements()) {
                  Agent a = (Agent) enums.nextElement();
                  cust.addAgent(a);
                }
                
                customers.put(cust.getName(), cust);
                custMap.put(cust.getID(), cust);
	    }
            rs.close();
            stmt.close();


            String sql = "SELECT SL03001 AS id,SUM(SL03013-SL03053) AS debt FROM SL030300 WHERE  " +
                         " NOT(SL03013=SL03053) AND SL03006<=getdate() GROUP BY SL03001 ";
            

            stmt = con.prepareStatement(sql);

            rs = stmt.executeQuery();
            while(rs.next()){
              String id = rs.getString("id").trim();
              double debt = Double.parseDouble(rs.getString("debt").trim());
              Customer cust = (Customer)custMap.get(id);
              if (cust != null && debtShow(id)){
                cust.setDebt(cust.getDebt() + debt);
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
      String sql = "SELECT SL03001 AS klient, SL03002 AS number,SL03006 AS kuupaev,SL03013-SL03053 AS summa FROM SL030300 WHERE NOT(SL03013=SL03053) AND " +
                   "LEN(LTRIM(SL03013))>0 AND SL03006<GETDATE()+5 ";
      if (agentCode != null){
        sql += " AND SL03041=?";
      }
      sql += " ORDER BY SL03001";

      connectionUp(url);
      stmt = con.prepareStatement(sql);

      if (agentCode != null){
        stmt.setString(1, agentCode);
      }

      ResultSet rs = stmt.executeQuery();

      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        debt.setPayerID(debt.getCustID());
        debt.setDoknr(rs.getString("number").trim());

        debt.setDuedate(rs.getDate("kuupaev"));

        debt.setSum(Double.parseDouble(rs.getString("summa").trim()));
        if(debt.getSum()>0.04 || debt.getSum()<-0.04){
        	if(debtShow(debt.getCustID())) {
        		list.addElement(debt);	
        	}
        	
        }
        }
      rs.close();
      stmt.close();
      return list;
    }

    private boolean debtShow(String custid) {
    	boolean res = true;
    	if(getParent().getProps().getProperty("nodebtlist").contains(custid+",")) {
    		return false;
    	}
    	return res;
    }
    
    
    public TreeMap getProducts(Agent agent){
        connectionUp(url);


        String cmd = "SELECT RTRIM(SC01001) AS toode,LEFT(RTRIM(SC01037),1)+' '+RTRIM(SC01002)+' ' " +
                  " +SC01003 AS nimi,SC01004 AS myygihind ,0 AS tykke,SC03003 AS stock, 0 AS maxale,'' AS pandikas ,  " +
                  "  case when SC03003>0 and SC01031 = 1 then 1 else 0 end AS out ,SC01037 AS grupp,SC01132 AS ean  FROM SC010300 " +
                  "   INNER JOIN SC030300 ON SC01001=SC03001 WHERE SC03002='00'  AND LEN(LTRIM(SC01037))>0 ORDER BY LEFT(RTRIM(SC01037),1),SC01001 ";

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
                prod.setName("      " +nimetus);
                prod.setPrice(parseDouble(rs.getString("myygihind").trim()));

                prod.setInCases(parseDouble(rs.getString("tykke").trim()));
                prod.setStock(parseDouble(rs.getString("stock").trim()));
                prod.setMaxDiscount(parseDouble(rs.getString("maxale").trim()));
                

                String pant = rs.getString("pandikas");
                if(pant != null && pant.trim().length()>0){
                  prod.setPandPakendiKood(pant.trim());
                }
                String out = rs.getString("out");
                if(out != null && out.trim().equals("1")){
                  prod.setStatusSellout("1");
                }
                String gr = rs.getString("grupp");
                if(gr != null && gr.trim().length()>0){
                  prod.setGroup(gr.trim());
                }
                 gr = rs.getString("ean");
                if(gr != null && gr.trim().length()>0){
                  prod.setEAN(gr.trim());
                }
                
                
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
    public Vector getCustomerProductsList(String agentCode) {
        connectionUp(url);
	/*
   select "Customer Code" AS klient ,"Stock Code" AS toode,Qty AS kogus from st02 where"Salesman"='45'
AND "Invoice order date">'20021101' AND "Actual Del  Date">'00000000'
 group by "Stock Code","Customer Code",qty  order by "Customer Code","Stock Code","Invoice order date" */

     /*   SELECT  ST02001 AS klient ,ST02017 AS toode ,ST02020 AS Kogus,ST02015
         FROM ST020100 WHERE ST02015>? AND ST02016>0 AND ST02020>0 GROUP BY ST02017,ST02001, ST02020,ST02015 ORDER BY ST02001,ST02017 ,ST02015 asc
*/
//Seelega tuleb teha h�kk

        String sql = "SELECT OR20003 AS klient , OR21005 AS toode, 0 AS kogus FROM OR200100 INNER JOIN " +
                     "OR210300 ON OR20001=OR21001 WHERE OR20015>? AND LEN(OR20019)>0";

        String sql2 = "SELECT  ST02001 AS klient ,ST02017 AS toode ,ST02020 AS Kogus,ST02015 " +
                     "FROM ST020300 WHERE ST02015>? AND ST02016>0 AND ST02020>0 AND LEN(ST02007)>0";

        if (agentCode != null){
        //  sql += " AND OR20019=?";
        //  sql2 += " AND ST02007=?";

          sql += "";
          sql2 += "";

        }
        sql += " GROUP BY OR21005,OR20003 ORDER BY OR21005,OR20003  asc";
        sql2 += " GROUP BY ST02017,ST02001, ST02020,ST02015 ORDER BY ST02001,ST02017 ,ST02015 asc";

	if (isDebug()) {
	    System.out.println(sql);
	}

	Vector custProds = new Vector();
        String[] sqls = new String[]{sql2, sql};
        for (int i = 0 ; i < sqls.length ; i++){
          try {
            stmt = con.prepareStatement(sqls[i]);
            stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
            if (agentCode != null){
             // stmt.setString(2, agentCode);
            }
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
              String kliendikood = rs.getString("klient").trim();
              String tootekood = rs.getString("toode").trim();
              CustProd cp = new CustProd(kliendikood, tootekood, Double.parseDouble(rs.getString("kogus").trim()),0 ,0," ",0,0,0," ");
              if (i == 0){
                custProds.add(cp);
              } else if(!custProds.contains(cp)){
                custProds.add(cp);
              }
            }
            rs.close();
            stmt.close();
          } catch (Exception e) {
            getParent().showError(e);
          }
        }
        connectionDown();
	return custProds;
    }

    public String getPilotDayData(Agent agent){
      return null;
    }


    public String getNonMobileOrders(Agent agent){
      connectionUp(url);

      /*String sql = "SELECT \"Order value\" AS summa,(RTRIM(\"sl01002\")+' '+(\"Customer name\")) AS Nimi," +
                  " i.\"order Number\" AS number FROM st02 i INNER JOIN or20 o ON " +
                   " o.\"Order Number\"=i.\"Order Number\" INNER JOIN sl14 k ON " +
                   " i.\"Customer Code\"=k.\"Customer Code\" INNER JOIN sl01 l ON \"sl01001\"= " +
                   " k.\"Customer Code\" WHERE \"actual del  date\"=? AND  length(\"Shipment Mark\")=0 ";
      */
      String sql = "SELECT RTRIM(SL01002) AS nimi, ST02009 AS number, OR20024 AS summa FROM ST020300 " +
                   "LEFT JOIN OR200300 ON  OR20001=ST02009 LEFT JOIN SL140300 k ON ST02001=SL14001 " +
                   "LEFT JOIN SL010300 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-3 AND ST02007=? " +
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
                String nimi = rs.getString("nimi");
                if (nimi == null){
                nimi = " ";
                } else {nimi = nimi.trim();}

            	sb.append(nimi + "\t");
              sb.append(rs.getString("number").trim() + "\t");
              String sum = getNF().format(Double.parseDouble(rs.getString("summa").trim())) ;
              sb.append(sum + sep);
            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        connectionDown();
        return sb.toString();
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
      connectionUp(url);
        String cmd = "SELECT SUM(OR20024) AS sales FROM OR200300 WHERE MONTH(OR20015)=" +
                     "MONTH(GETDATE()) AND YEAR(OR20015)=YEAR(GETDATE()) AND OR20019=?";
        log(cmd);

        try {
            stmt = con.prepareStatement(cmd);
            stmt.setString(1, agentCode);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            ResultSet	      rs = stmt.executeQuery();

            while(rs.next()) {
              setSales(rs.getDouble("sales"));
            }
            rs.close();
            stmt.close();

        } catch (Exception e) {
            getParent().showError(e);
        }

	return 0;
    }


    public void setBulkSales(Map agents) {
       //if (agentCode == null) return 0;
       connectionUp(url);
         String cmd = "SELECT OR20019 AS agent,SUM(OR20024) AS sales FROM OR200300 WHERE MONTH(OR20015)=" +
                      "MONTH(GETDATE()) AND YEAR(OR20015)=YEAR(GETDATE()) GROUP BY OR20019";
         log(cmd);

         try {
             stmt = con.prepareStatement(cmd);
             //stmt.setString(1, agentCode);

             Calendar cal = Calendar.getInstance();
             cal.set(Calendar.DAY_OF_MONTH, 1);

             ResultSet	      rs = stmt.executeQuery();

             while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                 Agent agent = (Agent)agents.get(agentID);
                 if (agent != null){
                   agent.setMonthSales((int)rs.getDouble("sales"));
                       }
                    }

             rs.close();
             stmt.close();

         } catch (Exception e) {
             getParent().showError(e);
         }

     }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      String sql = "SELECT ST02020 AS  kogus, SC01071 AS sisu FROM ST020300 INNER JOIN " +
                   "SC010300 ON ST02017=SC01001 WHERE ST02007=?" +
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
      System.out.println("nr:" + nr + ", total:" + total);
      return total;
    }


    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,docsum AS summa,linecount AS ridu,text AS tekst," +
                   "sentlong AS kuupaev FROM dayreport WHERE sentdate>(DATE()-30) ORDER BY clid";
      Connection c = DriverManager.getConnection(url2);
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(sql);


      String sql2 = "SELECT SL01001 AS kood, SL01040 AS aastamyyk,SL01041 AS kuumyyk,SY24003 AS agendinimi, SL01006 AS kontaktisik " +
                    ",SL01011 AS telefon, SL01024 AS maksetin,SL01036 AS ale" +
                    " FROM SL010300 LEFT JOIN SY240100 ON SY24002=SL01035 ORDER BY " +
                    "SL01002";
      connectionUp();
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
        String tekst = ensureLen(rs.getString("tekst")," ");
        tekst = tekst.replace('\'', ' ').replace('\"', ' ').replace(';', ' ');
        if (tekst == null) tekst = " ";

        sb.append(tekst + sep);

        boolean has = scrollTo(cliID, rs2);
        if (has){
         sb.append(ensureLen(rs2.getString("aastamyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("kuumyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
         sb.append(ensureLen(rs2.getString("kontaktisik"), " ") + sep);
         String tel =  rs2.getString("telefon") ;
         tel.replace('\'', ' ').replace('\"', ' ').replace(';',' ');
         sb.append(ensureLen(tel, " ") + sep);
         System.out.println(ensureLen(tel, " "));
         sb.append(ensureLen(rs2.getString("maksetin"), " ") + sep);
         sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
        }

        sb.append("\n");
      }

      ManagerData md = new ManagerData();
      md.setClientVisitInfo(sb.toString());

      sql = "SELECT OR20003 AS klient,OR20019 AS agent,OR20017 AS agentname FROM OR200300 " +
          "WHERE OR20015>? GROUP BY OR20003,OR20019,OR20017 ORDER BY OR20003";

      PreparedStatement ps = con.prepareStatement(sql);
      ps.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
      rs = ps.executeQuery();
      StringBuffer bf2 = new StringBuffer();
      while(rs.next()){
        bf2.append(rs.getString("klient").trim() + sep);
        bf2.append(rs.getString("agent").trim() + sep);
        String agentName = rs.getString("agentname");
         if (agentName != null) agentName = agentName.trim();
        if (agentName == null) agentName = " ";
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
      else str = str.trim().replace('\'', ' ').replace('\"', ' ').replace(';',' ');
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

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "Scala5iLIT";
    private String             url = "jdbc:odbc:scalaDB";
    private String             url2 = "jdbc:odbc:reportlit";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/