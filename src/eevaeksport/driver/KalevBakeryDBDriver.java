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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
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
public class KalevBakeryDBDriver extends AbstractDBDriver implements Manager,Promo, BulkUpdater,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public KalevBakeryDBDriver(EksportPanel parent) {
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
                String cmd = "SELECT SL01001 as klient FROM SL010100 where SL01075 >0";


        		String cmd3 = "SELECT SL01001 AS klient,SUM(SL03013-SL03053) AS debt FROM SL030300 INNER JOIN SL010300 ON SL03001 = SL01056" +
        				" or SL03001 = SL01001  WHERE  " +
                        " NOT(SL03013=SL03053) AND SL03006<=getdate()  GROUP BY SL01001 ";


        		if (isDebug()) System.out.println(cmd);
        		TreeMap custs = new TreeMap();




        		StringBuffer products = new StringBuffer();
        		try {

        	     this.connectionUp(url);
        		Statement st = con.createStatement();
        		ResultSet rs = st.executeQuery(cmd3);

        		while (rs.next()) {

        		String kood = rs.getString("klient").trim();
        		double volg = rs.getDouble("debt");
        		Customer custw = new Customer (kood);
        		custw.setDebt(volg);
        		if(volg>0){
        			custs.put(kood,custw);
        		}
        		}

        		//siin pannakse peale PLOKKID

        		rs = st.executeQuery(cmd);
        		while (rs.next()) {
        			String kood = rs.getString("klient").trim();
        		    Customer cus =  (Customer)custs.get(kood);
        		    if(cus != null){
        		    	cus.setSalesBlock(true);
        		    }else{
        		    	Customer cusnew = new Customer(kood);
        		    	cusnew.setSalesBlock(true);
        		    	custs.put(kood,cusnew);
        		    }
        		 }

        		rs.close();
        		} catch (Exception e) {
        		getParent().showError(e);
        		}
        		Iterator     codes = custs.values().iterator();
        		while (codes.hasNext()){
        		Customer cust = (Customer)codes.next();
        		String bl = "0";
        		if(cust.isSalesBlock()){
        			bl = "1";
        			}
        		 products.append(cust.getID() + ";" + getParent().getNF().format(cust.getDebt()) + ";" + bl + "\n");
        		}
                 this.connectionDown();
        		return products.toString();

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
    	// 0. tee tabel 
    	// 1. eevast peame v�lja lugema
    	// 2. loeme t�naes p�eva info tabelist
    	
  	  connectionUp(url); 
  	  String  cmd = "  SELECT  RTRIM(SC03001) AS toode , '23' AS LADU ,SC03003-SC03004 AS stock   " +
	      "  FROM SC030300  WHERE SC03002='23' AND SC03003!=0 ORDER BY SC03001" ;


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
	           st.close();
	       } catch (Exception e) {
	           getParent().showError(e);
	       }
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
            String    sql = " SELECT SY24002 AS liik ,SY24003 AS nimetus FROM SY240300 WHERE RTRIM(SY24003)<>'' AND SY24001 = 'IF' ORDER BY SY24003";
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
        String  cmd = "SELECT SY24002 AS yksus,SY24003 AS nimi  FROM SY240300 WHERE SY24001='BK'";
	TreeMap agents = new TreeMap();

	try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
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
	
	Agent aa = new Agent ("HOUSE1");
	aa.setName("PagariPlikad1");
	agents.put("PagariPlikad1", aa);
	
	Agent ab = new Agent ("HOUSE2");
	ab.setName("PagariPlikad2");
	agents.put("PagariPlikad2", ab);
	
	Agent ac = new Agent ("HOUSE3");
	ac.setName("PagariPlikad3");
	agents.put("PagariPlikad3", ac);
	
	
	return agents;
    }
    
    public void insertDefective(DefectiveWrapper wr){
        String sql = "INSERT INTO mj_quality VALUES (?,?,?,?,?,?,?,?,?,?,getdate(),?,' ',' ',0,0)";
        try {
           connectionUp(url);
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, wr.getDefective().getAgentID());
            ps.setString(2, wr.getDefective().getAgentName());

            for (Iterator iter = wr.getDefective().getLines().iterator(); iter.hasNext(); ) {
              DefectiveLineWrapper lw = new DefectiveLineWrapper(iter.next());
              ps.setString(3, lw.getLine().getCustID());
              ps.setString(4, lw.getLine().getCustName());
              ps.setString(5, lw.getLine().getProdID());
              ps.setString(6, lw.getLine().getProdName());

              double kogus = 0;
              try {
                kogus = Double.parseDouble(lw.getLine().getAmount());
              } catch (NumberFormatException nfe){};
              ps.setDouble(7, kogus);
              String bb = lw.getLine().getBestBefore();
              System.out.println(kogus+"bb '"+lw.getLine().getBestBefore()+"'"+bb+"'");
              ps.setString(8, bb);
              ps.setInt(9, lw.getLine().getDefectCode());
              ps.setString(10, lw.getLine().getProdType());
              ps.setString(11, lw.getLine().getDefectText());

              ps.executeUpdate();
            }

            con.commit();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
      }


    

    public TreeMap getCustomers(Agent agent){
        connectionUp(url);
        connectionUp2(url);
        
        Date date = new Date();
       
     
        Calendar c2 = Calendar.getInstance(); //sygis
        c2.set(Calendar.DATE, 1);
        c2.set(Calendar.MONTH, 8);      
   
        Calendar c1 = Calendar.getInstance();//talv
        c1.set(Calendar.DATE, 10);
        c1.set(Calendar.MONTH, 0);
        c1.set(Calendar.YEAR, c1.get(Calendar.YEAR) + 1);
  
        boolean isChrist = true;
        if(date.after(c2.getTime()) && date.before(c1.getTime())){
        	isChrist = true;
        }
        
        
        String cmd = "SELECT SL01002 AS nimi,SL01001 AS kood,(SL01003 + ' (' + SL01004 + ')') AS aadress, SC45004 AS ale,SL01036 AS ale2 , " +
         "  SL14007 AS visit,SL01035 as agent,SL22005 as paym , SL01029 as klgrupp, SL01075 as block ,SL01056 as payer" +
         " , SL01008 + ' ' + SL01011 as info , (SELECT SL01018 from SL010300 where SL01001=kl.SL01056) as limiit " +
         " FROM SL010300 kl left JOIN SC450300  " +
		 "  ON SL01036=SC45003    LEFT OUTER JOIN SL140300 ON SL01001=SL14001 AND SL14002='00'" +
         " LEFT OUTER JOIN SY240300 ON SL01035=SY24002 AND SY24001='BK'" +
         " LEFT OUTER JOIN SL220300 ON SL01024=SL22002 AND SL22001='0' " +
         " WHERE len(SL01001)>4  and  SL01002 !='' ORDER BY SL01002";//WHERE ( SL01002 != ''  AND SL01129 != '') or SL01129 = '0100000' 
        
        //alumine on Kristine tehtud

        
        //'PR' +
        connectionUp(url);
        String cmd3 = "SELECT SL01056 as pea   FROM SL010300 left JOIN SC450400   ON SL01036=SC45003 " +
                  "  WHERE SL01002 != '' AND SL01035 != ''  and SL01056 != '' ORDER BY SL01002";
	
      log(cmd);

	Vector vec = new Vector();

   Statement st = null;
   ResultSet	      rs = null;
	try {   
		st = con.createStatement();
	      rs = st.executeQuery(cmd3);
	    while(rs.next()){
	    	vec.addElement(rs.getString("pea").trim());
	    }
	    rs.close();
	    st.close();
	}catch (Exception e){
		e.printStackTrace();
	}

        
        
	log(cmd);

	TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        connectionUp(url);
	try {
           PreparedStatement stmt = con.prepareStatement(cmd);
            if (agent != null){
//              stmt.setString(1, agent.getID());
            }
	   	      rs = stmt.executeQuery();

	    while (rs.next()) {
		String nimi = rs.getString("nimi").trim();
                Customer cust = new Customer(rs.getString("kood").trim());
                cust.setAddress(rs.getString("aadress").trim());
                  nimi = nimi + " " + cust.getID();//  + " " + cust.getAddress() v�tsin v�lja Angelika soovil
                
                cust.setName(nimi);
                
                
                String ale = rs.getString("ale");
                if (ale != null && ale.length() > 0){
                 // cust.setDiscount(Double.parseDouble(ale.trim()));
                }
                String ale2 = rs.getString("ale2");
                if (ale2 != null && ale2.trim().equals("04")){
                	//cust.setDiscount(17);
                	
                }
                String visit = rs.getString("visit");
                if(visit != null && visit.trim().length()>0){
                 //   cust.setVisit(visit.trim());	
                }
                
                String agen = rs.getString("agent");
                if(agen != null){
                  cust.addAgent(new Agent(agen.trim()));
                }
                
                String paym = rs.getString("paym");
                if(paym != null && paym.trim().length()>0){
                	cust.setPaymTerm(paym.trim());
                }
                cust.addAgent(new Agent("HOUSE1"));
                cust.addAgent(new Agent("HOUSE2"));
                cust.addAgent(new Agent("HOUSE3"));
              
                
               
             
                cust.setGroup(rs.getString("klgrupp"));
                
                String block  =  rs.getString("block");
                if(block != null && block.trim().length()>0){
                	if(block.trim().equals("1")){
                		cust.setSalesBlockT(true);
                	}
                	
                }
                String payer = rs.getString("payer");
                if(payer != null && payer.trim().length()>0){
                	cust.setPayer(payer);              	
                }

               
                
                
                String info = rs.getString("info");
                if(info != null && info.trim().length()>0){
                	cust.setPersonalData(info.trim().replaceAll(";", ":"));              	
                }
                String limiit = rs.getString("limiit");
                if(limiit != null && limiit.trim().length()>0){
                	cust.setCredit(limiit.trim());              	
                }
                
          
                
                
                
                
                //otsime v�lasumma igale kauplusele 
                
    /*            String sql6 = "SELECT SL01001 AS id,SUM(SL03013-SL03053) AS debt FROM SL030300 INNER JOIN SL010300 ON SL03001 = SL01001  WHERE  " +
                " NOT(SL03013=SL03053) AND SL03006<=getdate() and SL01001='" + cust.getPayer()+ "'  GROUP BY SL01001 ";

   Statement stm = con2.createStatement();

   ResultSet rs3 = stm.executeQuery(sql6);
   while(rs3.next()){    
     double debt = Double.parseDouble(rs3.getString("debt").trim());
     //  cust.setDebt(cust.getDebt() + debt);
   }
   
   rs3.close();
   stm.close();

                
   if(cust.getDebt()<0.02 && cust.getDebt() > 0 ){
   	   cust.setDebt(0);
     }           
      */     
   
   String sql7 = "SELECT SL01001 AS id,SUM(SL03013-SL03053) AS debt FROM SL030300 INNER JOIN SL010300 ON SL03001 = SL01001  WHERE  " +
   " NOT(SL03013=SL03053) and SL01001='" + cust.getPayer()+ "'  GROUP BY SL01001 ";

			Statement stmr = con2.createStatement();
			
			ResultSet rs4 = stmr.executeQuery(sql7);
			while(rs4.next()){    
			double xxx = Double.parseDouble(rs4.getString("debt").trim());
			cust.setUsedCredit(xxx);
			}
			
			rs4.close();
			stmr.close();
       boolean goes = true ;
			
			if(cust.getID().subSequence(0,2).equals("M3") && isChrist){
			//	goes = false;
			}
			    
   if(goes){

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
        String sql = "SELECT SL01001 AS klient, SL03002 AS number,SL03006 AS kuupaev,SL03013-SL03053 AS summa , -datediff(day ,getdate(),SL03006) as yle " +
        		"FROM SL030300 INNER JOIN SL010300 ON SL03001 = SL01001  WHERE NOT(SL03013=SL03053) AND " +
                     "LEN(LTRIM(SL03013))>0 AND SL03006<GETDATE()+5 ";
        sql += " ORDER BY SL01001";

        connectionUp(url);
        stmt = con.prepareStatement(sql);


        ResultSet rs = stmt.executeQuery();

        while(rs.next()){
          Debt debt = new Debt();
          debt.setCustID(rs.getString("klient").trim());
          debt.setDoknr(rs.getString("number").trim());

          debt.setDuedate(rs.getDate("kuupaev"));

          debt.setSum(Double.parseDouble(rs.getString("summa").trim()));
          debt.setDue(rs.getString("yle"));
          if(debt.getSum()>0.04 || debt.getSum()<-0.04){
          	list.addElement(debt);	
          }
          }
        rs.close();
        stmt.close();
        return list;
      }

    
    public Vector getPriceMatrixData()  {  
    	
    	   String    sql = "SELECT SL01001 AS custid,'' AS clgroup,SUBSTRING(SC18002,11,35) AS prodid,'' AS prodgroup,SC18009,  " +
          "   (case when SY24003=null then '0' else SY24003 end ) AS discount,SC18008 AS price ,( case when SC18011 < getdate()+100 then SC18011 else null end) as paev " +
          "   FROM SC180300 INNER JOIN SL010300 ON  left(SC18002,7)= SL01001  LEFT JOIN SC010300 on SC01001=SUBSTRING(SC18002,11,35) LEFT JOIN SY240300 on SC18009=SY24002 and SY24001='BG' WHERE  " +
          "   SC18001='C' AND CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011   and len(SL01008) >0  and SC01014 != '' and  SC01115 in ('Q')  " +
          "   order by SL01001,SUBSTRING(SC18002,11,35)";

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
                 String date = rs.getString("paev");
                 MatrixLine mline = new MatrixLine();

                 if (custID != null && custID.length() > 0) {
                   mline.setCustID(custID);
                 }
                 if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                   mline.setCliGroup(clGroup.trim());
                 }

                 
                 if(date != null){
                	 mline.setEndDate(date.substring(0,10));
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
              "  case when SC39006 <= getdate() AND (SC39007 >= getdate() OR SC39007 = null) then  " +
              "  SC39005 else SC39004 end AS price  " +
              "  FROM SC390300 INNER JOIN SC010300 on SC01001=SC39001 WHERE SC39002 != '00'  and SC01014 != '' and  SC01115 in ('Q')  order by SC39002,SC39001 ";
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

            	 
            	 
            	 
             }
    	
        return result;
      }

    public TreeMap getProducts(Agent agent){
        connectionUp(url);

//'PR' +
        String cmd ="SELECT  RTRIM(SC01001) AS toode,(case when SC01094 =''then 'YYY' else SC01094 end) + '     ' + (RTRIM(SC01001)+'     ('+  " +
     "   SC01015    + ')    ' +    +rtrim(SC01002))  AS nimi,SC39004 AS myygihind ,SC01015 AS tykke,0 AS stock,   " +
   " SC01115  AS grupp,SC01132 AS ean  , SC01016 as letter ,SC01014 as sorting " +
   "  , case when SC01135 = 0 then  'KG' else 'TK' end as unit  FROM SC010300  left outer JOIN SC390300 ON SC01001 " + 
   "  = SC39001 and SC39002='00'  WHERE   LEN(LTRIM(SC01037))>0 and SC39004 > 0 and rtrim(ltrim(SC01016)) != 'S'  " +
   " and  SC01115 in ('Q')  and right (SC01001,2) != '-1'ORDER BY LEFT(RTRIM(SC01037),1),(case when SC01094 =''then 'YYY' else SC01094 end),SC01002 " ;
	
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
              //  prod.setStock(rs.getDouble("stock"));

                if(kood.indexOf("-EE") > -1 ){
                  prod.setStatusDiscount("1");
                }
                String gr = rs.getString("grupp");
                if(gr != null && gr.trim().length()>0){
                  prod.setGroup(gr.trim());
                }
                prod.setName("      " + nimetus );
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
            
          String sql2 = "SELECT  SL01001 AS klient ,ST03017 AS toode ,ST03020 AS Kogus,ST03015  as lastdate ,max(ST03014) as arvenr , " +
      "(case when ST03015+cast(SC01093 as int )>getdate() then 0 else 1 end) AS date " +
     "           FROM ST030300 INNER JOIN SL010300 ON ST03001 = SL01001  INNER JOIN SC010300 ON ST03017 = SC01001 WHERE ST03015> getdate()-30  " +
     " AND ST03016>0 AND ST03020>0  and SC01014 != '' and  SC01115 in ('Q')  " +
      "         GROUP BY ST03017,SL01001, ST03020,ST03015,SC01093 ORDER BY SL01001,ST03017 ,ST03015  ";

					if (isDebug()) {
					System.out.println(sql2);
					}
          
          
          stmt = con.prepareStatement(sql2);
        //  stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
          
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
                  cp.setRedline(rs.getString("date"));
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

  String cmd2 =  " SELECT  SL01001 AS klient ,ST03017 AS toode ,ST03020 AS Kogus,ST03015  as lastdate ,max(ST03014) as arvenr " +
          "  FROM ST030300 INNER JOIN SL010300 ON ST03001 = SL01001  INNER JOIN SC010300 ON ST03017 = SC01001 WHERE ST03015> GETDATE()-60 AND ST03016>0 AND ST03020>0  and SC01014 != '' " +
           " and  SC01115 in ('Q') " +
          " GROUP BY ST03017,SL01001, ST03020,ST03015 ORDER BY SL01001,ST03017 ,ST03015 ASC ";
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
      String cmd = "SELECT OR20019 AS agent, SUM(OR20024) AS sales  FROM OR200300 WHERE MONTH(OR20015)=" +
                   "MONTH(GETDATE()) AND YEAR(OR20015)=YEAR(GETDATE()) GROUP BY OR20019";
        log(cmd);

        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            ResultSet	      rs = stmt.executeQuery(cmd);
            while(rs.next()) {
              String agentID = rs.getString("agent");
              Agent agent = (Agent)agents.get(agentID);
              if (agent != null){
                int sales = (int)rs.getDouble("sales");
                agent.setMonthSales(sales);
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
     * @param agentCode
     *
     * @return
     *
     * @see
     */
    public double getSalary(String agentCode) {
      if (agentCode == null) return 0;
      String cmd = "SELECT SUM(OR20024) AS sales ,OR20019 AS agent FROM OR200300 WHERE MONTH(OR20015)=" +
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
      System.out.println("nr:" + nr + ", total:" + total);
      return total;*/
    }


    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,docsum AS summa,linecount AS ridu,text AS tekst," +
                   "sentlong AS kuupaev FROM dayreport WHERE sentdate>(DATE()-30) ORDER BY clid";
      Connection c = DriverManager.getConnection(url2);
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(sql);


      String sql2 = "SELECT SL01001 AS kood, SL01040 AS aastamyyk,SL01041 AS kuumyyk,SY24003 AS agendinimi, SL01006 AS kontaktisik " +
                    ",SL01011 AS telefon, SL01024 AS maksetin,SL01036 AS ale" +
                    " FROM SL010100 LEFT JOIN SY240100 ON SY24002=SL01035 ORDER BY " +
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

      sql = "SELECT OR20003 AS klient,OR20019 AS agent,OR20017 AS agentname FROM OR200100 " +
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

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "KalevBakery";
    private String             url = "jdbc:odbc:scalaDB";
    //private String url = "jdbc:freetds:sqlserver://scala/scalaDB";
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

