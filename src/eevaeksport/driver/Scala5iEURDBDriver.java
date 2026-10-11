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
public class Scala5iEURDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Promo,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public Scala5iEURDBDriver(EksportPanel parent) {
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
		      "  FROM SC03E100  WHERE SC03002='01' AND SC03003!=0 ORDER BY SC03001" ;


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
    	try{
    		connectionUp(url);
    	}catch(Exception e ){
    		connectionUp(url);
    	}
    
        TreeMap   tm = new TreeMap();
        String    sql = " SELECT SY24002 AS liik ,SY24003 AS nimetus FROM SY24E100 WHERE RTRIM(SY24003)<>'' AND SY24001 = 'IB' ORDER BY SY24003";
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
        String  cmd = "SELECT SY24002 AS yksus,SY24003 AS nimi FROM SY24E100 WHERE SY24001='BK'";
	TreeMap agents = new TreeMap();

	try {
            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String nimi = rs.getString("nimi").trim();
              agent.setName(nimi);
              agent.setObject("01");
              agent.setIsPilot(true);
              agents.put(nimi, agent);
	    }
	    Agent aaa = new Agent("HOUSE");
	    aaa.setName("HOUSE");
	    aaa.setObject("01");
        aaa.setIsPilot(true);
	    agents.put("HOUSE", aaa);
	    
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return agents;
    }

    public Vector getPriceMatrixData()  {
         Vector lines = new Vector();
         connectionUp(url);
         String cmd = "SELECT SL01008 as pea   FROM SL01E100 left JOIN SC45E100   ON SL01036=SC45003 " +
                    " WHERE SL01002 != '' AND SL01035 != '' AND SL01008 != '' and SL01056 != '' ORDER BY SL01002";
	
       log(cmd);

	Vector vec = new Vector();

    Statement st = null;
    ResultSet	      rs = null;
	try {   
		st = con.createStatement();
	      rs = st.executeQuery(cmd);
	    while(rs.next()){
	    	vec.addElement(rs.getString("pea").trim());
	    }
	    rs.close();
	    st.close();
	}catch (Exception e){
		e.printStackTrace();
	}
	    System.out.println("vecsize " + vec.size());
	// vektoris on klindid kellel on peaklient m�rgitud ja neile ei pane kaasa allahindlust
	
         String    sql = "SELECT SL01008 AS custid,'' AS clgroup,SUBSTRING(SC18002,11,35) + '03' AS prodid,'' AS prodgroup,SC18009, " +
         "	(case when SY24003=null then '0' else SY24003 end ) AS discount,SC18008 AS price  " +
         "   FROM SC18E100 INNER JOIN SL01E100 ON  left(SC18002,4)= SL01001 LEFT JOIN SY24E100 on SC18009=SY24002 and SY24001='BG' WHERE " +
          "  SC18001='C' AND CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011   and len(SL01008) >0  " +
          " order by SL01008,SUBSTRING(SC18002,11,35)";

         
           try {
            
             st = con.createStatement();
              rs = st.executeQuery(sql);

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
               if(!vec.contains(custID.trim())){
                   lines.add(mline);   
               }
             }

             // siisn l�hevad ainult need kilendid kellel pole peaklienti
             
             rs.close();
             
             
             String sqq = "       SELECT  LEFT(SC18002,10) AS clgroup,SUBSTRING(SC18002,11,35) + '03' AS prodid, SC18008 AS price  " +
      "   FROM SC18E100 WHERE  SC18001='C' AND " +
      "   CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011 " +
      "   and LEFT(SC18002,10) like '%P%'";
             /*      SELECT  LEFT(SC18002,10) AS clgroup,SUBSTRING(SC18002,11,35) + '03' AS prodid, SC18008 AS price 
             FROM SC180100 WHERE  SC18001='C' AND
             CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011
             and LEFT(SC18002,10) like '%P%'*/
             
             rs = st.executeQuery(sqq);

             while (rs.next()) {

                 String clGroup = rs.getString("clgroup").trim();
                 String prodID = rs.getString("prodid").trim();
   	   
                 double price = rs.getDouble("price");
                 MatrixLine mline = new MatrixLine();
       
                 if (clGroup != null && clGroup.trim().length() > 0) {
                     mline.setDiscGroup(clGroup.trim());
                   }
                 
                 if (prodID != null && prodID.trim().length() > 0) {
                   mline.setProdID(prodID.trim());
                 }

                 if (price != 0) {
                     mline.setPrice(price);
                 }
                
                 
                     lines.add(mline);   
                 
             }
             
             //Siin panime �ra PEAKLIENDI SOODUKAD ja kasutame seal pricergroupi
             
             
             
             String sql2 = "select '' AS custid ,SC39002 AS clgroup , rtrim(ltrim(SC39001)) + '03'  AS prodid,' ' AS prodgroup, " +
             " case when SC39006 <= getdate() AND (SC39007 >= getdate() OR SC39007 = null) then " +
             " SC39005 else SC39004 end AS price  " +
             " FROM SC39E100 WHERE SC39002 != '00' order by SC39002,SC39001 ";
             rs = st.executeQuery(sql2);
/*
select '' AS custid ,SC39002 AS clgroup , rtrim(ltrim(SC39001)) + '03'  AS prodid,' ' AS prodgroup, 
              
 
case when CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN SC39006 and SC39007 then 
              SC39005 else SC39004 end AS price  
              FROM SC390100 WHERE SC39002 != '00' order by SC39002,SC39001 */
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
              lines.add(mline);  // Kommentisin v�lja kuniks uus hinna skeem hakkab toimima
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
        String cmd = "SELECT SL01002 AS nimi,SL01008 AS kood,(SL01003 + ' (' + SL01004 + ')') AS aadress, SC45004 AS ale,SL01036 AS ale2 ," +
          "  SL01009 AS visit, ' ' AS info ,SL01035 as agent , SL01029 as klgrupp , SL01056 as alegrupp ,(case when SL01060=1 or SL01075=1" +
          " then 1 else 0 end) as blokk , SL01056 as payer  FROM SL01E100 left JOIN SC45E100  " +
		  " ON SL01036=SC45003 and SC45002='00' WHERE SL01002 != '' AND SL01035 != '' AND SL01008 != '' ORDER BY SL01002";
	
        log(cmd);

	TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        HashMap pead = new HashMap();
        HashMap peaaled = new HashMap();
        Vector custvec = new Vector();
        String sql1 = " select SL01001,SL01029, SC45004 as ale" +
        		" FROM SL01E100 left JOIN SC45E100  " +
		  " ON SL01036=SC45003 and SC45002='00' where SL01001 like '%P%'";

        
        
	try {
		
		    stmt = con.prepareStatement(sql1);		
		    ResultSet	      rs = stmt.executeQuery();
		    while (rs.next()) {
		    	String klient = rs.getString("SL01001").trim();
		    	pead.put(klient,rs.getString("SL01029").trim());
		        String ale = rs.getString("ale");
                if (ale != null && ale.length() > 0){
                  peaaled.put(klient,ale);
                }
                
           
		    }
		    
		    
            stmt = con.prepareStatement(cmd);       
	        rs = stmt.executeQuery();
	    
	    while (rs.next()) {
		String nimi = rs.getString("nimi").trim().toUpperCase();
                Customer cust = new Customer(rs.getString("kood").trim());
                cust.setName(nimi);
                cust.setAddress(rs.getString("aadress").trim());
                String ale = rs.getString("ale");
                if (ale != null && ale.length() > 0){
                  cust.setDiscount(Double.parseDouble(ale.trim()));
                }
                String ale2 = rs.getString("ale2");
                if (ale2 != null && ale2.trim().equals("04")){
                	cust.setDiscount(17);              	
                }
                cust.setVisit(rs.getString("visit").trim());
                String agen = rs.getString("agent");
                if(agen != null){
                  cust.addAgent(new Agent(agen.trim()));
                }
                cust.addAgent(new Agent("HOUSE"));
                
                if (cust.getID().equals("GPRdegu")) {
                    Enumeration enums = getParent().getAgentsHash().elements();
                      while (enums.hasMoreElements()) {
                        Agent a = (Agent) enums.nextElement();
                        cust.addAgent(a);
                        }
                      }
                
                String klgrupp = rs.getString("klgrupp");
                cust.setGroup(parent.make(klgrupp));
              
                String alegrupp = rs.getString("alegrupp");
                cust.setCustType(parent.make(alegrupp));
                
                String pricelist  = (String)pead.get(cust.getCustType().trim());
                if(pricelist != null){
                	cust.setGroup(parent.make(pricelist));
                	//System.out.println("panen juurde " + pricelist + " " +cust.getID());
                }
                
                
                String discp = (String)peaaled.get(cust.getCustType().trim());
                if(discp != null){
                	double peadisc  = Double.parseDouble(discp);
                        if(peadisc != 0){    
                        	cust.setDiscount(peadisc);
                        }else{
                        	cust.setDiscount(0);
                        }
                        	
                }
                
                String blokk = rs.getString("blokk");
                if(blokk != null && blokk.trim().equals("1")){
                	cust.setSalesBlockT(true);
                }
                
                String payer = rs.getString("payer");
                if(payer != null && payer.trim().length()>0){
                	cust.setPayer(payer.trim());
                }
                
                customers.put(cust.getName(), cust);
                custMap.put(cust.getID(), cust);
                custvec.addElement(cust);
	    }
            rs.close();
            stmt.close();


            //Siin paneme paika yksik klientide v�lad yle MT.Via koodi j�rgi  
            
            String sql = "SELECT SL01008 AS id,SUM(SL03013-SL03053) AS debt FROM SL03E100 INNER JOIN SL01E100 ON SL03001 = SL01001  WHERE  " +
                         " NOT(SL03013=SL03053) AND SL03006<=getdate() and SL01056='' GROUP BY SL01008 ";

            stmt = con.prepareStatement(sql);

            rs = stmt.executeQuery();
            while(rs.next()){
              String id = rs.getString("id").trim();
              double debt = Double.parseDouble(rs.getString("debt").trim());
              Customer cust = (Customer)custMap.get(id);
              if (cust != null){
                cust.setDebt(cust.getDebt() + debt);
              }
            }
            rs.close();
            stmt.close();

            // n��d paneme paika peakliendi kaudu v�lad 
            
            String sql2 = "SELECT SL01056 AS id,SUM(SL03013-SL03053) AS debt FROM SL03E100 INNER JOIN SL01E100 ON SL03001 = SL01001 " +
                        "WHERE NOT(SL03013=SL03053) AND SL03006<=getdate() and SL01056!='' GROUP BY SL01056 ";

stmt = con.prepareStatement(sql2);

rs = stmt.executeQuery();
while(rs.next()){
 String id = rs.getString("id").trim();
 double debt = Double.parseDouble(rs.getString("debt").trim());
 
 if(debt >0){
	 
	 Enumeration  it = custvec.elements();
	 while(it.hasMoreElements()){

		 Customer cust = (Customer)it.nextElement();
		 if (cust.getPayer().equals(id)){
		   cust.setDebt(debt);
		 }		 
	 }

	 
 }
}


String sql3 = "SELECT SL03001 AS id,sum(SL03013-SL03053) AS debt FROM SL03E100  " +
                   "     WHERE NOT(SL03013=SL03053) AND SL03006<=getdate() and SL03001 like '%P%' group by SL03001 " +
                   "order by SL03001";

stmt = con.prepareStatement(sql3);

rs = stmt.executeQuery();
while(rs.next()){
 String id = rs.getString("id").trim();
 double debt = Double.parseDouble(rs.getString("debt").trim());
 
 if(debt >0){
	 
	 Enumeration  it = custvec.elements();
	 while(it.hasMoreElements()){

		 Customer cust = (Customer)it.nextElement();
		 if (cust.getPayer().equals(id)){
		 // System.out.println(cust.getPayer() + " " + debt);
			 cust.setDebt(debt);
		 }		 
	 }

	 
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
      String sql = "SELECT SL01008 AS klient, SL03002 AS number,SL03006 AS kuupaev,SL03013-SL03053 AS summa " +
      		"FROM SL03E100 INNER JOIN SL01E100 ON SL03001 = SL01001  WHERE NOT(SL03013=SL03053) AND " +
                   "LEN(LTRIM(SL03013))>0 AND SL03006<GETDATE()+5 ";
      sql += " ORDER BY SL01008";

      connectionUp(url);
      stmt = con.prepareStatement(sql);


      ResultSet rs = stmt.executeQuery();

      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        debt.setDoknr(rs.getString("number").trim());
        debt.setDuedate(rs.getDate("kuupaev"));
        debt.setSum(Double.parseDouble(rs.getString("summa").trim()));
        if(debt.getSum() > 0.04 || debt.getSum() < -0.04){
        	list.addElement(debt);	
        }
        }
      rs.close();
      stmt.close();
      return list;
    }

    public TreeMap getProducts(Agent agent){
        connectionUp(url);


        String cmd = "SELECT RTRIM(SC01001)+'03' AS toode,(LEFT(RTRIM(SC01037),1)+' '+RTRIM(SC01002)+' '" +
                   "+SC01003) AS nimi,SC01004 AS myygihind ,SC01010 AS tykke,SC03003 AS stock, SC01011 AS maxale,SC01150 AS pandikas , " +
                   " case when SC03003>0 and SC01031 = 1 then 1 else 0 end AS out ,SC01037 AS grupp,SC01132 AS ean FROM SC01E100 " +
                     "INNER JOIN SC03E100 ON SC01001=SC03001 WHERE SC03002='01'  AND LEN(LTRIM(SC01037))>0 ORDER BY LEFT(RTRIM(SC01037),1),SC01001";

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
                try{
                	 prod.setInCases(parseDouble(rs.getString("tykke").trim()));
                }catch(Exception e){
                	
                }
               
                prod.setStock(0);
            //   prod.setMaxDiscount(17);
                

                String pant = rs.getString("pandikas");
                if(pant != null && pant.trim().length()>0){
                  prod.setPandPakendiKood(pant.trim());
                }
                String out = rs.getString("out");
                if(out != null && out.trim().equals("1")){
                //  prod.setStatusSellout("1");
                }
                String gr = rs.getString("grupp");
                if(gr != null && gr.trim().length()>0){
                  prod.setGroup(gr.trim());
                }
                 gr = rs.getString("ean");
                if(gr != null && gr.trim().length()>0){
                  prod.setEAN(gr.trim());
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

        String sql = "SELECT SL01008 AS klient , OR21005 +'03' AS toode, (OR21011) AS kogus ,OR20015  AS lastdate FROM OR20E100 INNER JOIN  " +
                    " OR21E100 ON OR20001=OR21001 INNER JOIN SL01E100 ON OR20003 = SL01001 WHERE OR20015>? AND LEN(SL01008)>0  " +
                   " GROUP BY OR21005,SL01008,OR21011,OR20015 ORDER BY SL01008 asc,OR21005,OR20015 DESC ";
        String sql2 = "SELECT  SL01008 AS klient ,ST02017+'03' AS toode ,ST02020 AS Kogus,ST02015  as lastdate " +
                       " FROM ST02E100 INNER JOIN SL01E100 ON ST02001 = SL01001 WHERE ST02015>? AND ST02016>0 AND ST02020>0 AND LEN(SL01008)>0 " +
                      " GROUP BY ST02017,SL01008, ST02020,ST02015 ORDER BY SL01008,ST02017 ,ST02015 DESC";

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
              CustProd cp = new CustProd(kliendikood, tootekood, Double.parseDouble(rs.getString("kogus").trim()),0 ,0,
              		rs.getString("lastdate").substring(5,10),0,0,0," ");
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
        String sql = "SELECT RTRIM(SL01002) AS nimi, ST02009 AS number, sum(ST02040) AS summa FROM ST02E100  " +
        " LEFT JOIN OR20E100 ON  OR20001=ST02009 LEFT JOIN SL14E100 k ON ST02001=SL14001  " +
        " LEFT JOIN SL01E100 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-1 AND ST02007=? " +
        " GROUP BY ST02009,SL01002";

StringBuffer sb = new StringBuffer();
  this.connectionUp();
try {
 stmt = con.prepareStatement(sql);
 stmt.setString(1, agent.getID());
 String sep= System.getProperty("line.separator");
 ResultSet	      rs = stmt.executeQuery();
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


    public String getNonMobileOrders(Agent agent){
      connectionUp(url);

      /*String sql = "SELECT \"Order value\" AS summa,(RTRIM(\"sl01002\")+' '+(\"Customer name\")) AS Nimi," +
                  " i.\"order Number\" AS number FROM st02 i INNER JOIN or20 o ON " +
                   " o.\"Order Number\"=i.\"Order Number\" INNER JOIN sl14 k ON " +
                   " i.\"Customer Code\"=k.\"Customer Code\" INNER JOIN sl01 l ON \"sl01001\"= " +
                   " k.\"Customer Code\" WHERE \"actual del  date\"=? AND  length(\"Shipment Mark\")=0 ";
      */
      String sql = "SELECT RTRIM(SL01002) AS nimi, ST02009 AS number, sum(ST02040) AS summa FROM ST02E100  " +
        " LEFT JOIN OR20E100 ON  OR20001=ST02009 LEFT JOIN SL14E100 k ON ST02001=SL14001  " +
        " LEFT JOIN SL01E100 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-1 AND ST02007=? " +
        " GROUP BY ST02009,SL01002";

      StringBuffer sb = new StringBuffer();

        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, agent.getID());
            String sep= System.getProperty("line.separator");
            ResultSet	      rs = stmt.executeQuery();
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
              sb.append(sum + sep +  sep);
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
        String cmd = "SELECT SUM(OR20024) AS sales FROM OR20E100 WHERE MONTH(OR20015)=" +
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
         String cmd = "SELECT OR20019 AS agent,SUM(OR20024) AS sales FROM OR20E100 WHERE MONTH(OR20015)=" +
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
      String sql = "SELECT ST02020 AS  kogus, SC01071 AS sisu FROM ST02E100 INNER JOIN " +
                   "SC01E100 ON ST02017=SC01001 WHERE ST02007=?" +
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
      //System.out.println("nr:" + nr + ", total:" + total);
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
                    " FROM SL01E100 LEFT JOIN SY24E100 ON SY24002=SL01035 ORDER BY " +
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
        // System.out.println(ensureLen(tel, " "));
         sb.append(ensureLen(rs2.getString("maksetin"), " ") + sep);
         sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
        }

        sb.append("\n");
      }

      ManagerData md = new ManagerData();
      md.setClientVisitInfo(sb.toString());

      sql = "SELECT OR20003 AS klient,OR20019 AS agent,OR20017 AS agentname FROM OR20E100 " +
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
    public static final String DRIVER_VERSION = "Scala5iEUR";
    private String             url = "jdbc:odbc:scalaDB";
    private String             url2 = "jdbc:odbc:reportEST";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

