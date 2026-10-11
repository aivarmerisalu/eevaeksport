/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.report.ReportLine;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class KruzasLATDBDriver extends AbstractDBDriver implements  BulkUpdater,SalesPlan ,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public KruzasLATDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

        try {
            Class.forName(databaseDriverName).newInstance();

        } catch (Exception e) {
            parent.showError(e);
        }
    }

   public void setSalesPlan(String agentID,String yearMonth,String custID,int plan,java.sql.Date date){
	 
   	
int mm = Integer.parseInt(yearMonth.substring(5,6));
int yyyy = Integer.parseInt(yearMonth.substring(0,4));
Calendar cal = Calendar.getInstance();
cal.set(Calendar.DATE,1);
cal.set(Calendar.MONTH,mm-1);
cal.set(Calendar.HOUR,0);
//System.out.println(cal.getTime().toLocaleString());

     try {
     	String plaan =" select b10_kodas_ks AS klient, b10_pozymis AS plaan from b10_darbotvarke where  b10_tipas = 2 " +
     			" and  month(b10_data)= " + mm + " and  " + yyyy + "=year(b10_data)  AND b10_kodas_ks = '" + custID + "'" ;
        
    	System.out.println(plaan);
       	 connectionUp();
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(plaan);
   	        while(rs.next()){
   	        	String kill = "delete from b10_darbotvarke where b10_kodas_ks = '" + custID +
				"' and " + mm + " = month(b10_data) and " + yyyy + "=year(b10_data)";
   	        	
   	        	Statement stmt2 = con.createStatement();
   	        	stmt2.executeUpdate(kill);
   	        	String aa = "Deleted line " + custID + " " + agentID + " " + yearMonth + " " + rs.getDouble("plaan");
                this.log(aa);
   	        	System.out.println(aa);
   	        }
   	     String add = "insert  b10_darbotvarke  values('" + agentID + "',now(),'" + custID + "',2," + plan + ",0,null,0,now(),now())";
        	Statement stmt3 = con.createStatement();
        	stmt3.executeUpdate(add);
   	        
        	String aa = "Added line " + custID + " " + agentID + " " + yearMonth + " " + plan;
            this.log(aa);
	        	System.out.println(aa);
        }catch (Exception e){
     	e.printStackTrace();
       }
   }
    
   public void setAlternativeReport(ReportLine rr) {
   	System.out.println("AlternativeSisse");
   
    try {
     	connectionUp();    
     	
     	String upstr = "";
     	String min = "UPKV0000  ";
     	String comm = "";
     	if(rr.getLineSum()<1){
     	comm = rr.getText();

     	}
     	
     	upstr = min + comm;
   	     String add = "insert  b10_darbotvarke  values('" + rr.getAgentCode() + "',now(),'" + rr.getClientCode() + "',1,'" + upstr + "',0,null,0,now(),now())";
        	Statement stmt3 = con.createStatement();
        	stmt3.executeUpdate(add);
   	        
        	String aa = "Added line " + rr.getAgentCode() + " " + rr.getClientCode() + " " + upstr ;
            this.log(aa);
	        	System.out.println(aa);
        }catch (Exception e){
     	e.printStackTrace();
       }
   
   }
   
   public String getProductSortingData(){
   	return null;
   }
   
   public String getProductSortingData(Vector done){
   	StringBuffer sb = new StringBuffer();
   
   	
   	return sb.toString();
   }
   
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
   
    public String getAssortmentStr(Agent agent){
    	return null;
    }
            public String getDealerCustomerCSV(String agent) {
		return null;
	}
            public String getSaveDebtInfo() {
               	return null;
               }
               
        public Vector getFixedAssortmentData() {
        	 Vector lines = new Vector();

             try {
            	  String cmd = " SELECT v22_kodas_as AS grupp, v22_kodas_ps AS toode FROM V22_ASORT_PR " +
            	  		"order by v22_kodas_ps, v22_kodas_as ";
            	System.out.println(cmd);
               	 connectionUp();
           	        Statement stmt = con.createStatement();
           	        ResultSet rs = stmt.executeQuery(cmd);
           	        while(rs.next()){
                              String custID = " ";
                              String clGroup = " ";
                              String prodID = " ";
                              String prodGroup = " ";
                              MatrixLine mline = new MatrixLine();
                                   
                              clGroup = rs.getString("grupp");
                              prodID = rs.getString("toode");

                              if (custID != null && custID.trim().length() > 0) {
                                mline.setCustID(custID.trim());
                              }
                              if (clGroup != null
                                  && clGroup.trim().length() > 0) {
                                mline.setCliGroup(clGroup.trim());
                              }

                              if (prodID != null && prodID.trim().length() > 0) {
                                mline.setProdID(prodID.trim());
                              }
                              if (mline.getProdID() == null && prodGroup != null
                                  && prodGroup.trim().length() > 0) {
                                mline.setProdGroup(prodGroup);
                              }

                              lines.add(mline);
                            }

           	       rs.close();
           	       con.close();
                           } catch (Exception e) {
                               getParent().showError(e);
                           }
             return lines;
        }

	public String getCustProdDiscounts(Agent agent) {
		return null;
	}

	public Vector getPriceMatrixData() {
		Vector lines = new Vector();

		String sql = " SELECT kood AS custid,'' AS clgroup,'' AS prodid,grupp AS alegroup,alla AS discount,0 " +
                      " AS price FROM osmy/allahind ";

		Statement st = null;
		try {
			connectionUp();
			st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {

				String custID = rs.getString("custid");
				String clGroup = rs.getString("clgroup");
				String prodID = rs.getString("prodid");
				String discGroup = rs.getString("alegroup");
				double discount = rs.getDouble("discount");
				double price = rs.getDouble("price");
				MatrixLine mline = new MatrixLine();

				if (custID != null && custID.trim().length() > 0) {
					mline.setCustID(custID.trim());
				}
				if (mline.getCustID() == null && clGroup != null
						&& clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
				}

				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}
				if (mline.getProdID() == null && discGroup != null
						&& discGroup.trim().length() > 0) {
					mline.setDiscGroup(discGroup);
				}
				mline.setDiscount(discount);

				if (price != 0) {
					mline.setPrice(price);
				}
				lines.add(mline);
			//	System.out.println(discGroup);
			}

			rs.close();
			st.close();
			con.close();
		} catch (SQLException ex) {
		}
		return lines;
	}



	

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
     String sql = "SELECT I44_KODAS_KS AS klient,I44_DOK_NR AS number ,max(I44_DATA_MOK) AS maksepaev , " +
         " sum(i44_suma_db-i44_suma_cr) AS tasumata ,max(I44_DATA_DOK) AS kuupaev, max(I44_DATA_MOK-getdate()) AS yle FROM i44_skol,n08_klij WHERE " +
         " i44_skol.i44_kodas_ks=n08_klij.n08_kodas_ks and " +
         " exists(select i44_kodas_ks,i44_dok_nr,sum(i44_suma_db-i44_suma_cr) as suma from i44_skol i44 where i44.i44_kodas_ks=i44_skol.i44_kodas_ks " +
         " and i44.i44_dok_nr=i44_skol.i44_dok_nr and i44_tipas<>11 and i44.i44_data_dok<=getdate()group by i44.i44_kodas_ks,i44.i44_dok_nr " +
        " having sum(i44.i44_suma_db-i44.i44_suma_cr)<>0) group by I44_DOK_NR,I44_KODAS_KS order by I44_KODAS_KS,i44_dok_nr ";
     System.out.println("volanimekiri");
      connectionUp();
      PreparedStatement st = con.prepareStatement(sql);
      //st.setString(1, agentCode);
      ResultSet rs = st.executeQuery();
      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        debt.setDoknr(rs.getString("number").trim());
        debt.setDuedate(rs.getDate("maksepaev"));
        debt.setSum(rs.getDouble("tasumata"));
        debt.setDate(rs.getDate("kuupaev"));
        debt.setDue(rs.getString("yle"));
        list.addElement(debt);
      }
      st.close();
      return list;
    }


    /**
	 *
	 */
    public void connectionUp(){
      connectionUp(this.url);
    }




        public String getSaveStockInfo(){
        return null;
        }




        public String getDistribution() {
          return null;
        }


    private void connectionUp(String url){
      try {
        if (con != null){
          con.close();
        }

           con = DriverManager.getConnection(url,"DBA","sql");
      } catch (Exception e) {
            getParent().showError(e);
      }

    }

    
    
    public TreeMap getProdGroups() throws SQLException {
    	

      connectionUp(url2);

      TreeMap tm = new TreeMap();

            String cmd = "  SELECT e_prgr(N17_KODAS_MS, 1, 2) AS kood " +
			"  FROM n17_prod a 		" + 
			" GROUP BY e_prgr(N17_KODAS_MS, 1, 2) ORDER BY e_prgr(N17_KODAS_MS, 1, 2)";
       try {
        stmt = con.prepareStatement(cmd);
        ResultSet rs = stmt.executeQuery();
       setTreeGroup(true);
      // tm.put("xxxx", "Tester");
        while (rs.next()) {
          String liik = rs.getString("kood").trim();
          String nimetus = liik.substring(liik.indexOf("|")+1,liik.length());
          liik = liik.substring(0,liik.indexOf("|"));
          if (liik != "") {
            tm.put(liik.trim(), nimetus.trim());
          }
        }
      }
      catch (Exception e) {
        getParent().showError(e);
      }
      System.out.println("tootegrupid1");
      connectionDown();
      return tm;
    }


    public TreeMap getProdGroupsSecond() throws SQLException {
        connectionUp(url2);
        TreeMap tm = new TreeMap();
        System.out.println("tootegrupid2");
 
          

            String cmd = "  SELECT e_prgr(N17_KODAS_MS, 2, 2) AS kood " +
			"  FROM n17_prod a 		" + 
			" GROUP BY e_prgr(N17_KODAS_MS, 2, 2) ORDER BY e_prgr(N17_KODAS_MS, 2, 2)";
       try {
        stmt = con.prepareStatement(cmd);
        ResultSet rs = stmt.executeQuery();
       setTreeGroup(true);
       tm.put("xxxx", "Tester");
        while (rs.next()) {
          String liik = rs.getString("kood").trim();
          String nimetus = liik.substring(liik.indexOf("|")+1,liik.length());
          liik = liik.substring(0,liik.indexOf("|"));
          if (liik != "") {
            tm.put(liik.trim(), nimetus.trim());
          //  tm.put("xxxx"+liik.trim(), "Tester "+nimetus.trim());
          }
         }
        } catch (Exception e) {
          getParent().showError(e);
        }
        connectionDown();
        return tm;
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
    public DealerProduct getDealerProductsList() {

    	/*
        DealerProduct dealerProds = new DealerProduct();
        try {

          String  sql = " SELECT  I17_KODAS_PS AS toode, sum(I17_KIEKIS) AS STOCK , '01' AS LADU  " +
               " from i17_vpro  where I17_KODAS_IS in ('00000001','T0000002') " +
			   " GROUP BY I17_KODAS_PS" +
			    " ORDER BY I17_KODAS_PS" ;

             if (isDebug()) System.out.println(sql);
             connectionUp(url2);
                Statement stm = con.createStatement();
                ResultSet rst = stm.executeQuery(sql);

                  while (rst.next()) {
                    String toode = rst.getString("toode").trim();
                    int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                    String ladu  =  rst.getString("ladu").trim();
                    dealerProds.add(ladu, toode,seis+""," "," " );
                }
                rst.close();
                stm.close();

        } catch (Exception e) {
            getParent().showError(e);
        }
*/
        // return dealerProds;
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
    public TreeMap getAgents() {
        connectionUp();
        String  cmd = "SELECT N15_KODAS_MS AS kood ,N15_PAV AS nimi  FROM  n15_men " +
              " WHERE length (rtrim(N15_KODAS_MS))=2 ORDER by N15_PAV";
        TreeMap agents = new TreeMap();

        try {
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("kood").trim());
              String name = rs.getString("nimi").trim();
        
              agent.setName(name);
              agents.put(name, agent);
            }
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        return agents;
    }

    public TreeMap getCustomers(Agent agent){

        TreeMap customers = new TreeMap();
        connectionUp(url2);     
        try {
        String sql2 = " Select v21_kodas_ks AS klient  ,v21_kodas_as AS grupp from V21_ASORT_KL order by v21_kodas_ks";
        
        TreeMap custGroups = new TreeMap();
        stmt = con.prepareStatement(sql2);
        //stmt.setInt(1, Integer.parseInt(agent.getID()));
        //stmt.setDate(2, getStartingDate());
        ResultSet	      rsr = stmt.executeQuery();

        while (rsr.next()) {
        	String klient  = rsr.getString("klient").trim(); 
        String olemas = (String)custGroups.get(klient);
        if(olemas != null){
        	custGroups.put(klient,olemas + "|" + rsr.getString("grupp").trim());
        }else{
        	custGroups.put(klient, rsr.getString("grupp").trim());
        }
        	
        }
        
        
        /*
          SELECT  I06_KODAS_KS AS agent , SUM(case when I06_OP_TIP = '52' then -I06_SUMA else 0 end) + 
                     SUM(case when I06_OP_TIP = '51' then I06_SUMA else 0 end)  AS summa  FROM i06_parh  WHERE 
                     month(I06_OP_DATA)=month(getdate()) AND year(I06_OP_DATA)=year(getdate()) and I06_PERKELTA='2'
                      and I06_R_DATE <= getdate() 
                     GROUP BY  I06_KODAS_KS  
                     */
        
      String myydyd = "SELECT  I06_KODAS_KS AS klient , SUM(case when I06_OP_TIP = '52' then -I06_SUMA else 0 end) +  " +
                   "  SUM(case when I06_OP_TIP = '51' then I06_SUMA else 0 end)  AS summa  FROM i06_parh  WHERE " +
                   "  month(I06_OP_DATA)=month(getdate()) AND year(I06_OP_DATA)=year(getdate()) and I06_PERKELTA='2' "+
                   "   and I06_R_DATE <= getdate() " +
                   "  GROUP BY  I06_KODAS_KS " ;
	   //String myydyd= "select  kodas_ks as klient ,pard_sum  - pard_gr_sum AS summa  from  i_men_apyv  where yyyymm =" +
	   	//	" DateFormat(dateadd(month, 0, getdate()), 'yyyymm') " ;
 
	   TreeMap custSold = new TreeMap();
	   
	   
	   
	    stmt = con.prepareStatement(myydyd);
        ResultSet	      rst = stmt.executeQuery();

        while (rst.next()) {
        	String klient  = rst.getString("klient").trim(); 
        	double summa = rst.getDouble("summa");
        	custSold.put(klient, summa + "");
        }
	   
	   
	   
	   
	   
	//   String last3 = " select  kodas_ks as klient ,sum(pard_sum  - pard_gr_sum)/3 AS summa  from  i_men_apyv  where  yyyymm >" +
	  // 		" DateFormat(dateadd(month, -4, getdate()), 'yyyymm') " +
//	"	and yyyymm < DateFormat(dateadd(month, 0, getdate()), 'yyyymm')  GROUP BY  kodas_ks";
	
        /*
        SELECT  I06_KODAS_KS AS agent , SUM(case when I06_OP_TIP = '52' then -I06_SUMA else 0 end) + 
        SUM(case when I06_OP_TIP = '51' then I06_SUMA else 0 end)  AS summa  FROM i06_parh  WHERE 
        month(I06_OP_DATA)=month(getdate()) AND year(I06_OP_DATA)=year(getdate()) and I06_PERKELTA='2'
         and I06_R_DATE <= getdate() 
        GROUP BY  I06_KODAS_KS 
        
         */
        
        String last3 = "  SELECT  I06_KODAS_KS AS klient , (SUM(case when I06_OP_TIP = '52' then -I06_SUMA else 0 end) +  " + 
                   "  SUM(case when I06_OP_TIP = '51' then I06_SUMA else 0 end))  AS summa  FROM i06_parh  WHERE  " +
                   "   I06_PERKELTA='2'  and I06_R_DATE > dateadd(month, -4, getdate()) " +
                   " and I06_R_DATE < dateadd(month, 0, getdate()) " +
                   "  GROUP BY  I06_KODAS_KS " ;
        
	   TreeMap lastThree = new TreeMap();

	    stmt = con.prepareStatement(last3);
       ResultSet	      rsu = stmt.executeQuery();


       while (rsu.next()) {
       	String klient  = rsu.getString("klient").trim(); 
       	double summa = rsu.getDouble("summa");
       	if(summa>0){
       		lastThree.put(klient, (summa/3) + "");
       	}
       }
   	String aadress ="SELECT  trim(N33_KODAS_KS) AS klient,   N33_ADRESAS AS aadress  FROM  N33_KBAN  where    length (rtrim(N33_ADRESAS))>0" ;
    
	   TreeMap custAadr = new TreeMap();
	   
	   
	    stmt = con.prepareStatement(aadress);
        ResultSet	      rsa = stmt.executeQuery();

        while (rsa.next()) {
        	String klient  = rsa.getString("klient").trim(); 
        	String aadres = rsa.getString("aadress").trim();
        		custAadr.put(klient,aadres);
        	
        }
	   
		String plaan =" select b10_kodas_ks AS klient, b10_pozymis AS plaan from b10_darbotvarke where  b10_tipas = 2 and month(getdate())" +
				" = month(b10_data) and year(getdate()) = year(b10_data)" ;
        
		   TreeMap custPlan = new TreeMap();
		   
		   
		    stmt = con.prepareStatement(plaan);
	        ResultSet	      rsp = stmt.executeQuery();

	        while (rsp.next()) {
	        	String klient  = rsp.getString("klient").trim(); 
	        	double plan = rsp.getDouble("plaan");
	        	if(plan>0){
	        		custPlan.put(klient, plan + "");
	        	}
	        }
        
        
        
        
        String cmd = "SELECT  trim(N08_KODAS_KS) AS kood1, " +
        		 " trim(N08_PAV) + ' ' AS nimetus, " +
				  " N33_ADRESAS AS aadress  ,N08_KODAS_KS AS payer , " +
				 " N08_KODAS_MS_P  AS agent ," +
				 " (case when N08_KODAS_TS_P in ('REAL','SEKAN') then '0' else N08_KODAS_TS_P end) as paymterm ," +
				"rtrim( N08_TEL) +' '+ (N08_MOB_TEL) AS info ,N08_KODAS_LS_7 AS visit  " +
				"  FROM n08_klij , N33_KBAN " +
				 "  where  N33_KODAS_KS =* N08_KODAS_KS and ((length (rtrim(N08_KODAS_MS_P))=2 AND N08_POZ_DATE !=1 " +			  
				 " and left(N08_KODAS_KS,3) in ('SIA')) or  trim(N08_KODAS_KS) ='2000535') order by N08_PAV ";
        
      
        
        log(cmd);
        

        HashMap custMap = new HashMap();
        System.out.println("kliendid");
       
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();


            while (rs.next()) {
              String accountnum = rs.getString("kood1").trim();
              String name = rs.getString("nimetus").trim();
              Customer cust = new Customer(accountnum);
              if (cust.getID().equals("2000535")) {
                Enumeration enums = getParent().getAgentsHash().elements();
                  while (enums.hasMoreElements()) {
                    Agent a = (Agent) enums.nextElement();
                    cust.addAgent(a);
                    }
                  }
              if(custGroups.get(accountnum) != null){
              	cust.setGroup((String)custGroups.get(accountnum));
              //	System.out.println(cust.getGroup());
              }
              
              if(custPlan.get(accountnum) != null){
              	cust.setBudget(new Double((String)custPlan.get(accountnum)).doubleValue());
              	custPlan.remove(accountnum);
              //	System.out.println(cust.getGroup());
              }
              if(custSold.get(accountnum) != null){
              	cust.setSold(new Double((String)custSold.get(accountnum)).doubleValue());
              //	System.out.println(cust.getGroup());
              }
              if(custAadr.get(accountnum) != null){
              	cust.setAddress((String)custAadr.get(accountnum));
              //	System.out.println(cust.getGroup());
              }
              

              if(lastThree.get(accountnum) != null){
              	cust.setAverage(new Double((String)lastThree.get(accountnum)).doubleValue());
              //	System.out.println(cust.getGroup());
              }
              
              
              cust.setName(name + " " + accountnum.substring(accountnum.length()-3,accountnum.length()));
    
           /*   String discStr = rs.getString("ale");
              if (discStr != null){
               // cust.setDiscount(Double.parseDouble(discStr));
              }*/
              
              String visit = rs.getString("visit");
              if (visit != null && visit.trim().length()>0 && visit.trim().startsWith("VZ")){
                cust.setVisit(visit.trim().substring(2,visit.trim().length()));
              }
              
              
               
              String payer =rs.getString("payer");
              if(payer != null && payer.trim().length()> 0){
                cust.setPayer(payer.trim());
              }

              cust.addAgent(new Agent(rs.getString("agent").trim()));
              cust.addAgent(new Agent("VP"));
             // cust.setPaymTerm(rs.getInt("paymterm")+"");
           //  System.out.println(cust.getID() +" " + cust.getAgents().toString());
              
              cust.setPersonalData(parent.make(rs.getString("info")));
              
              customers.put(cust.getName(), cust);
              custMap.put(cust.getPayer(), cust);
            }
            String sql =
                "SELECT I44_KODAS_KS AS klient, sum(i44_suma_db-i44_suma_cr) AS summa  FROM i44_skol,n08_klij WHERE " +
               " i44_skol.i44_kodas_ks=n08_klij.n08_kodas_ks and " +
               " exists(select i44_kodas_ks,i44_dok_nr,sum(i44_suma_db-i44_suma_cr) as suma from i44_skol i44 where i44.i44_kodas_ks=i44_skol.i44_kodas_ks " +
               " and i44.i44_dok_nr=i44_skol.i44_dok_nr and i44_tipas<>11 and i44.i44_data_dok<=getdate() group by i44.i44_kodas_ks,i44.i44_dok_nr "+
                 " having sum(i44.i44_suma_db-i44.i44_suma_cr)<>0) group by I44_KODAS_KS order by I44_KODAS_KS ";
            System.out.println("debitory");
            stmt = con.prepareStatement(sql);
             //stmt.setInt(1, Integer.parseInt(agent.getID()));
             //stmt.setDate(2, getStartingDate());
             ResultSet	      rs1 = stmt.executeQuery();

            while (rs1.next()) {
              String id = rs1.getString("klient").trim();
              double debt = Double.parseDouble(rs1.getString("summa").trim());

              Customer cust = (Customer) custMap.get(id);
              if (cust != null) {
                cust.setDebt(cust.getDebt() + debt );
              }
            }
            rs1.close();
            stmt.close();
            con.close();
          }
          catch (Exception e) {
            getParent().showError(e);
          }

          return customers;
    }

    

    private String getDir(){
        String dir = getParent().getProps().getProperty("readin");
        if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
        return dir;
      }
    
    
    
    public TreeMap getProducts(Agent agent){
    	
    	

        TreeMap products = new TreeMap();
    	
    	  try {
         
    	   String  sql = "   SELECT  I17_KODAS_PS AS toode, sum(I17_KIEKIS) AS STOCK ,max(I17_P_PAR_K) AS price ,max(I17_kodas_us_a) AS yhik " +
           " from i17_vpro  where I17_KODAS_IS in ('00000001') AND ((I17_KIEKIS) >0 OR (I17_P_PAR_K)>0) " +
			"    GROUP BY I17_KODAS_PS   ORDER BY I17_KODAS_PS" ;
    	   //('00000001','T0000002')
           System.out.println("laoseisud");
         if (isDebug()) System.out.println(sql);
         connectionUp(url2);
         TreeMap tooted = new TreeMap();
            Statement stm = con.createStatement();
            ResultSet rst = stm.executeQuery(sql);

              while (rst.next()) {
                String toode = rst.getString("toode").trim();
                int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                double price = rst.getDouble("price");
                String yhik = rst.getString("yhik");
               	 Toode tt = new Toode(toode);
                    tt.setKogus(seis);
                    tt.setKastis(price);                  
                    tt.setLadu(yhik);
                    tooted.put(toode,tt);	
               
               
            }
            rst.close();
            stm.close();
  Vector sellers = new Vector();
  
  
  String  sql2 = " SELECT I07_KODAS AS toode FROM i07_pard " +
			" INNER JOIN i06_parh ON I07_KODAS_PO=I06_KODAS_PO " +
		    " WHERE I06_DOK_DATA > getdate()-100 AND I06_DOK_DATA <getdate()+3 AND length(rtrim(I06_KODAS_MS)) =2 AND left(I07_KODAS,1)!='T' " +
		    " GROUP BY I07_KODAS " ;

if (isDebug()) System.out.println(sql2);
connectionUp(url2);
   Statement stm1 = con.createStatement();
   ResultSet rst1 = stm1.executeQuery(sql2);

     while (rst1.next()) {
       String toode = rst1.getString("toode").trim();    
           sellers.add(toode);	
      }    
   
   rst1.close();
   stm1.close();
    	
    	

   System.out.println("MyydudSortiment");  	
    	
        connectionUp(url2);
        TreeMap testers = new TreeMap();
        
        String cmd3 = "SELECT N17_KODAS_PS AS toode,trim(N17_PAV) AS nimetus " +
     "  FROM n17_prod a where " +
     "	 N17_INSTR_POZ != 1 and N17_INSTR_POZ != 1  and N17_POZ_DATE !=1  " +
      " and (left (trim(N17_KODAS_PS),1)='T' or  " +
    	"	( left (trim(N17_PAV),1)='T' and left (trim(N17_KODAS_PS),1)!='T' )) order by N17_KODAS_PS desc";
  
        stmt = con.prepareStatement(cmd3);
        ResultSet	      rs5 = stmt.executeQuery();
        
        
        while (rs5.next()) {
        	String kood = rs5.getString("toode");
        	String nimetus = rs5.getString("nimetus");
        	if(nimetus != null && nimetus.trim().length()>0){
        		if(nimetus.trim().indexOf(" ") !=-1){
        			testers.put(nimetus.substring(1,nimetus.trim().indexOf(" ")),kood.trim());
        		}
        	}
        }
        
        
        
        
        
        
   String cmd = " SELECT N17_KODAS_PS AS toode,trim(N17_PAV) AS nimetus2,trim(N17_PAVU) AS nimetus,s27_kiek_pak  AS tykke, N32_KAINA1 AS myygihind, " +
   " 0 AS kogus, e_prgr(N17_KODAS_MS, 1, 2) AS grupp, e_prgr(N17_KODAS_MS, 2, 2) AS grupp2 ,N17_MEN_PAV AS minkogus , " +
   " N17_KODAS_LS_4 AS status , N17_MAX_NUOL as max, " +
   " case when N17_DATA_NAUJA>getdate() AND N17_AR_NAUJA=1 then 1 else 0 end AS uus,N17_BROK_POZ AS busted ," +
   "case when left(N17_KODAS_PS,1)  in ('1','2','3','4','5','6','7','8','9','0') then 'P' else'T' end AS sendsep " +
   " FROM n17_prod a , N32_PABC  ,s27_pakh where  trim(N17_KODAS_PS)*=trim(s27_addusr) AND  N17_KODAS_PS *= N32_KODAS_PS "  +
   "	and N17_INSTR_POZ != 1 and N17_INSTR_POZ != 1  and N17_POZ_DATE !=1 and left (N17_KODAS_PS,1)!='T' " +
   "and  left (N17_PAV,1) != 'T' order by N17_KODAS_PS desc ";
       
   if (isDebug()) {
            System.out.println(cmd);
        }
        System.out.println("tooted");
        System.out.println("listing " + sellers.size());
        System.out.println("laos " + tooted.size());
      
        Vector done = new Vector(); 
        
       
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();
            
            
            while (rs.next()) {
              String kood = rs.getString("toode").trim();
              Product prod = new Product(kood);
             // prod.setEAN(kood);
              double kogus = 0;
              double pr = 0;
              String yhik = null;
              Toode t = (Toode) tooted.get(kood);
             
              if(t != null){
              	kogus = t.getKogus();
              	pr = t.getKastis();
              	yhik = t.getLadu();
              	//tooted.remove(kood);
              }
              prod.setStock(0);
              if(kogus!= 0){
              	prod.setStock(kogus);
              }
              
              if(pr!= 0){
              	prod.setPrice(pr);
              }
              
              if(yhik != null && yhik.trim().length()>0){
              	prod.setUnit(yhik.trim());
              }
              
              String nimetus = rs.getString("nimetus").trim();
              prod.setName(nimetus);
          //  System.out.println(nimetus);
              if(nimetus.indexOf(" ") != -1){
              String test = 	(String) testers.get(nimetus.substring(0,nimetus.indexOf(" ")));
              if(test != null){
              	 prod.setPartii(test);
                // System.out.println(test + " " +prod.getName());
              	 //testers.remove(test);
              }
              	 }
             
              
              try{
              prod.setInCases(rs.getDouble("tykke"));
            } catch (Exception e){}
            if(prod.getPrice() > 0){
            }else{
            	prod.setPrice(rs.getDouble("myygihind"));     	
            }
           
             // String price = rs.getString("iii");
              /* 
              String ean = rs.getString("ean");
              if(ean != null && ean.trim().length()>0){
              	prod.setEAN(ean.trim());
              }
//              String price = ("3.000|15.000|0.000");
             if(price.indexOf("|")!=-1 ){
                price = price.substring(price.indexOf("|")+1,price.trim().length());
                price = price.substring(0,price.indexOf("|"));
               // System.out.println("price" + price);
              prod.setPrice(Double.parseDouble(price));
}*/
              String grupp = rs.getString("grupp");
             
             String grupp2 = rs.getString("grupp2");
                    
               String minam = rs.getString("minkogus");
               double res = 0;
               if (minam != null && minam.trim().length()>0){
               	try{
               	if(minam.indexOf("/")!=-1){
               		res = Double.parseDouble(minam.trim().substring(0,minam.indexOf("/")));
               	}else{
               		res = Double.parseDouble(minam.trim());
               	}
               	}catch (Exception e){
               		e.printStackTrace();
               	}
               	prod.setInCases(res);
               }
               
               
               int stat = rs.getInt("uus");
               	if(stat == 1){
               		//prod.setStatusNew("1");
               		prod.setName("NEW!! " + prod.getName());
               	}
               	
               	int busted = rs.getInt("busted");
               	if(busted == 1 && stat == 1){
               		prod.setName("OK!! " + prod.getName());
               	}
               
                if(grupp != null && grupp.trim().length()>0 ){
                 	 prod.setGroup(grupp.trim().substring(0,grupp.trim().indexOf("|")).trim());
                 }
                
                if (grupp2 != null && grupp2.trim().length()>0 ){
                    prod.setGroupSecond(grupp2.trim().substring(0,grupp2.trim().indexOf("|")).trim());
                   }
               	
               	//	String sends = rs.getString("sendsep").trim();
               // prod.setSendSeparate(sends);
               	/* 
                if (grupp != null && grupp.trim().length()>0 && sends.equals("T")){
                    prod.setGroup("xxxx");
                  }else if(grupp != null && grupp.trim().length()>0 ){
                  	 prod.setGroup(grupp.trim().substring(0,grupp.trim().indexOf("|")).trim());
                  }
                  if (grupp2 != null && grupp2.trim().length()>0 && sends.equals("T")){
                     prod.setGroupSecond("xxxx"+grupp2.trim().substring(0,grupp2.trim().indexOf("|")).trim());
                   }else if (grupp2 != null && grupp2.trim().length()>0 ){
                    prod.setGroupSecond(grupp2.trim().substring(0,grupp2.trim().indexOf("|")).trim());
                   }
               
               double max = rs.getDouble("max");
               if(max > 0){
               	if(max < 1){
               		max = 2;
               	}
               	prod.setMaxDiscount(max);
               }*/
               if(kogus > 1 || sellers.contains(kood) || stat == 1 ){
               String idd = prod.getID();
               if(idd.length()>8){
               	idd = idd.substring(idd.length()-8,idd.length());
               }
               
               prod.setVat("21");
               	prod.setName("  " + idd +" " + prod.getName());
               products.put(nimetus, prod);
               done.addElement(prod.getID());
                //sellers.remove(kood);
               }
                // }
            }
              
           
            
            String buf =   getProductSortingData(done);
       	 System.out.println("Sorteeritud Tootek ALGUS");
              
              if(buf.length() > 2){                      	
                  File f = new File(getDir() + "prodsort.txt");
                  if (f.exists())f.delete();
                    try {
              		PrintWriter os = new PrintWriter(new FileWriter(
              				getDir() + "prodsort.txt", true));
              		 System.out.println("Sorteeritud Tootek OK !!!!");
              		os.write(buf);
              		os.close();
              	  }catch (IOException ex) {
              		ex.printStackTrace();
              	  }	
               }  
           
            
            
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
        connectionUp(url2);
        System.out.println("klienditooted");
        String cmd ="  SELECT trim(I06_KODAS_KS)  AS klient, " +
        	" I07_KODAS AS toode,I07_KIEKIS AS kogus, " +
		    " I06_DOK_DATA AS kuupaev FROM i07_pard " +
			" INNER JOIN i06_parh ON I07_KODAS_PO=I06_KODAS_PO " +
		    " WHERE I06_DOK_DATA > ?  AND I06_DOK_DATA <getdate()+3 AND length(rtrim(I06_KODAS_MS)) =2 AND left(I06_KODAS_KS,1)!='T' " +
		    " GROUP BY I06_KODAS_KS,I07_KODAS,I06_DOK_DATA,I07_KIEKIS";
    
        
        if (isDebug()) {
           System.out.println(cmd);
       }

       Vector custProds = new Vector();
       HashMap kliendid = new HashMap();


       try {
          stmt = con.prepareStatement(cmd);
          stmt.setDate(1, getStartingDate());
           ResultSet	      rs = stmt.executeQuery();
          
           while (rs.next()) {
             String kliendikood = rs.getString("klient").trim();
             String tootekood = rs.getString("toode").trim();
             if(tootekood.length()>0){
             
             double kogus = Double.parseDouble(rs.getString("kogus").trim());
             String kuup = rs.getString("kuupaev").substring(5,10).trim();
             Customer cust = (Customer) kliendid.get(kliendikood);

             if (cust == null) {
               cust = new Customer(kliendikood);
             }

             Product prod = new Product(tootekood);
             cust.addProduct(prod,
                             new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                          kuup, 0, 0,0," "),false);
             kliendid.put(kliendikood, cust);
             custProds.add(new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                          kuup, 0, 0,0," "));
           }	
           }
           rs.close();
           } catch (Exception e) {
           getParent().showError(e);
       }
       connectionDown();
       return custProds;
    }

    public String getPilotDayData(Agent agent){
      return null;
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
      connectionUp(url2);
        double sales = 0;
        String cmd = "SELECT SUM(summa)  AS salary " +
                     "FROM osmy/my_tell te  WHERE te.osmy = ? " +
                     "AND month(regaeg)=month(date()) AND year(regaeg)=year(date())";
        log(cmd);

        try {
            stmt = con.prepareStatement(cmd);
            stmt.setInt(1, Integer.parseInt(agentCode));
            ResultSet	      rs = stmt.executeQuery();

            if(rs.next()) {
              sales = rs.getDouble("salary");
            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        setSales(sales);
        return 0;
    }




    public void setBulkSales(Map agents){
    	 connectionUp(url2);
       String cmd = " SELECT  I06_KODAS_MS AS agent , SUM(case when I06_OP_TIP = '52' then -I06_SUMA else 0 end) + " +
                     "SUM(case when I06_OP_TIP = '51' then I06_SUMA else 0 end)  AS pluss  FROM i06_parh  WHERE " +
                     "month(I06_OP_DATA)=month(getdate()) AND year(I06_OP_DATA)=year(getdate()) and I06_PERKELTA='2'" +
                     " and I06_R_DATE <= getdate() " +
                     "GROUP BY  I06_KODAS_MS";

       log(cmd);
       System.out.println("myygid");
         try {
           stmt = con.prepareStatement(cmd);

             ResultSet	      rs = stmt.executeQuery();
             while(rs.next()) {
               String agentID = rs.getString("agent").trim();
               Agent agent = (Agent)agents.get(agentID);
               if (agent != null){
                 agent.setMonthSales((int)rs.getDouble("pluss"));
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
    public static final String DRIVER_VERSION = "KruzasLAT";
    private String         url = "jdbc:odbc:Ncd_GAMA";
    private String	       url2 = "jdbc:odbc:Ncd_GAMA";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

