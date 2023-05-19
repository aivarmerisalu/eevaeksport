/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.routines.EAN13CheckDigit;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class RivileAmberDBDriver extends AbstractDBDriver implements  BulkUpdater,Discounter,PictureMaker{


	
    public RivileAmberDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

        try {
            Class.forName(databaseDriverName).newInstance();

        } catch (Exception e) {
            parent.showError(e);
        }
    }

  
   
   
 
 
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
   
	  public void insertSuperVisorSQL(SupervisorInfoWrapper wr){}


	  public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	        String sql = "INSERT into dbo.supervisor values ("
	          + "?, "   //   agent	C(50),
	          + "?,"   //   agentname	C(200),
	          + "?,"   //   clientid	C(50),
	          + "?,"   //   clientname	C(200),
	          + "?,"   //	prodid	C(50),
	          + "?,"   //   prodname	C(200),
	        //  ps.setTimestamp(7, new Timestamp(Long.parseLong((lw.getLine().getResStr3()))));
	          + "getdate() ,"   //	sentdate	D,
	          + " '' ,"   //	receivedTime	D,
	          + "?,"   //normalpr	N,
	          + "?,"   // amoun	N,
	          + "?,"   // grouptotal	N,
	          + "?,"   // cattotal	N,
	          + "?,"   // pertotcat	I,
	          + "?,"   // percolcat	I,
	          + "?,"   // perpalcat	I,
	          + "?,"   // pershecat	I,
	          + "?,"   // perrescat	I,
	          + "?,"
	          + "0,"
	          + "0,"
	          + "0,"
	          + "?,"   // actionaeva	N,
	          + "?,"   // actionbeva N,
	          + "?,"   // actiontota	N,
	          + "?,"   // custeval	N,
	          + "?,"   // custcomme	C(200),
	          + "?,"   // comment	C(200),
	          + "?,"   // custagent	C(200),
	          + "?,"   // custagentname	C(200),
	          + "?,"   // mustbe	I,
	          + "?,"   // ideal	I,
	          + "?,"   // resstr1	C(200),
	          + "?,"   // resstr2	C(200),
	          + "?,"   // resstr3	C(200),
	          + "?,"   // resint1	I,
	          + "?,"   // resint2	I,
	 	      + "?)" ; //	resint4	L

	          try {
	          	System.out.println("jam");
	          	connectionUp();
	              PreparedStatement ps = con.prepareStatement(sql);

	              for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
	                eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
	                ps.setString(1, wr.getInfo().getAgentID());
	                ps.setString(2, wr.getInfo().getAgentName());
	                ps.setString(3, lw.getLine().getCustID());
	                ps.setString(4, lw.getLine().getCustName());
	                ps.setString(5, lw.getLine().getProdID());
	             
	                ps.setString(6, lw.getLine().getProdName());
	                ps.setDouble(7, lw.getLine().getNormalPrice());
	                ps.setDouble(8, lw.getLine().getAmount());
	                ps.setDouble(9, 0);
	                ps.setDouble(10, 0);
	                ps.setInt(11, lw.getLine().getPercentageFromTotalCategory());
	                ps.setInt(12, lw.getLine().getPercentageFromColdCategory());
	                ps.setInt(13, lw.getLine().getPercentageFromPallCategory());
	                ps.setInt(14, lw.getLine().getPercentageFromShelfCategory());
	                ps.setInt(15, lw.getLine().getPercentageFromRestCategory());
	                ps.setBoolean(16, lw.getLine().isAction());
	                ps.setDouble(17, lw.getLine().getAction1Eval()*100);
	                ps.setDouble(18, lw.getLine().getAction2Eval()*100);
	                ps.setDouble(19, lw.getLine().getActionTotalEval()*100);
	                ps.setDouble(20, lw.getLine().getCustEval()*100);
	                String cc = lw.getLine().getCustComment();
	                if (cc == null){
	                	cc = " ";
	                }
	                ps.setString(21, cc);

	                String cd = lw.getLine().getComment();
	                if (cd == null){
	                	cd = " ";
	                }
	                ps.setString(22, cd);

	                String ce = lw.getLine().getCustAgentId();
	                if (ce == null){
	                	ce = " ";
	                }

	                ps.setString(23, ce);

	                String cf = lw.getLine().getCustAgentName();
	                if (cf == null){
	                	cf = " ";
	                }
	                ps.setString(24, cf);
	                ps.setInt(25, lw.getLine().getDealNr());
	                ps.setInt(26, lw.getLine().getIdealNr());

	                String cr1 = lw.getLine().getResStr1();
	                if (cr1 == null){
	                	cr1 = " ";
	                }
	                ps.setString(27, cr1);


	                String cr2 = lw.getLine().getResStr2();
	                if (cr2 == null){
	                	cr2 = " ";
	                }
	                ps.setString(28, cr2);


	                String cr3 = lw.getLine().getResStr3();
	                if (cr3 == null){
	                	cr3 = " ";
	                }
	                ps.setString(29, cr3);
	                ps.setInt(30, lw.getLine().getResInt1());
	                ps.setInt(31, lw.getLine().getResInt2());
	                ps.setInt(32, lw.getLine().getResInt3());
	                ps.executeUpdate();
	              }

	              ps.close();
	              this.connectionDown();	              
	          } catch (SQLException e) {
	              e.printStackTrace();
	          }
	        }




   
   
   public String getProductSortingData(){
   	return null;
   }
   
   public String getProductSortingData(Vector done){
   	StringBuffer sb = new StringBuffer();
   	System.out.println("SorditudTooted");
   	String id =  " SELECT N17_KODAS_PS AS prodid  " +
   " FROM n17_prod a ,  where  N17_INSTR_POZ != 1 and N17_INSTR_POZ != 1  and N17_POZ_DATE !=1 and left (N17_KODAS_PS,1)!='T' " +
   " and  left (N17_PAV,1) != 'T' order by N17_KODAS_PS ";

	String price =  " SELECT N17_KODAS_PS AS prodid ,(N32_KAINA1) as price " +
	   " FROM n17_prod a , N32_PABC  where   N17_KODAS_PS = N32_KODAS_PS and N17_INSTR_POZ != 1 " +
	   " and N17_POZ_DATE !=1 and left (N17_KODAS_PS,1)!='T' " +
	   " and  left (N17_PAV,1) != 'T' and N32_KAINA1 >0  AND N32_TIPAS = 'I' " +
	   " order by N17_KODAS_PS,N32_G_DATE desc,(N32_KAINA1)desc";
   	
	

	Statement st = null;
		try {
		connectionUp();
		st = con.createStatement();
		ResultSet rs = st.executeQuery(price);
		TreeMap sort = new TreeMap();
		while (rs.next()) {
		String prodID = rs.getString("prodid");
		String pri = rs.getString("price");
		if(!sort.containsValue(prodID)){
			sort.put(pri+prodID,prodID);
		}
		}
		
		Iterator iter = sort.values().iterator();
		while(iter.hasNext()){
			String gg = (String)iter.next();
			if(done.contains(gg)){
				//System.out.println(gg);		
				sb.append("pr;" + gg + "\n");
			}	
		}
		rs = st.executeQuery(id);
		
		while (rs.next()) {
		String prodID = rs.getString("prodid");
		if(done.contains(prodID)){
			sb.append("id;" + prodID + "\n");	
		}
		}
		
	//	System.out.println(sb.toString());
		
		/*rs = st.executeQuery(ean);
		while (rs.next()) {
		String prodID = rs.getString("prodid");
		if(done.contains(prodID)){
		
		sb.append("ean;" + prodID + "\n");
		}}
		
		rs = st.executeQuery(name);
		while (rs.next()) {
		String prodID = rs.getString("prodid");
		if(done.contains(prodID)){
		sb.append("name;" + prodID + "\n");
		}}
		
		
		rs = st.executeQuery(gr2);
		while (rs.next()) {
		String prodID = rs.getString("prodid");
		if(done.contains(prodID)){
		sb.append("gr2;" + prodID + "\n");
		}}
		rs = st.executeQuery(gr1);
		while (rs.next()) {
		String prodID = rs.getString("prodid");
		if(done.contains(prodID)){
		sb.append("gr1;" + prodID + "\n");
		}}
		
		
		*
		**/
		

		
		
		
		
		rs.close();
		st.close();
		con.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
   	
   	return sb.toString();
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
            	  String cmd = " select L01_KODAS_FS as grupp,l02_kodas_fil as toode from L01_FILH h , L02_FILD d ,n17_prod p where " +
            	  		" h.l01_kodas_fs=d.l02_kodas_fs  and L01_TIPAS = 1 " +
" and N17_KODAS_PS=l02_kodas_fil and h.l01_kodas_fs in (select n33_ship_via from " +
" N33_KBAN group by n33_ship_via ) order by L01_KODAS_FS ,N17_PAV ";
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

		String sql = " SELECT '' AS custid,kodas_gs AS clgroup,pr_kodas AS prodid,'' AS alegroup,0 AS discount,akc_kaina " +
                      " AS price ,akc_data_nuo  as fdate, akc_data_iki  as tdate FROM akc_kainos ";
		
		/*pr_kodas                             CHAR(12)      – SKU Code
kodas_gs                             CHAR(12)      – Group Code
kaina                                     NUMERIC(12,4)  - Base Price
akc_kaina                            NUMERIC(12,4)  - Action Price (all added up), NULL if no action
akc_data_nuo                   DATETIME             - Action Begin Date. NULL if actin is no time-limited
akc_data_iki                       DATETIME             - Action End Date. NULL if actin is no time-limited
*/

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
				  String algus = rs.getString("fdate");
                  String lopp = rs.getString("tdate");
                  
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
				
				   if (algus != null && algus.trim().length() > 0) {
                       mline.setStartDate(algus.trim().substring(0,10));
               }
                   if (lopp != null && lopp.trim().length() > 0) {
                       mline.setEndDate(lopp.trim().substring(0,10));
               }  
                   
			//	lines.add(mline);
				//System.out.println(discGroup);
			}

			rs.close();
			
			
			
			
			String sql2 = " SELECT kodas_ks AS custid,pr_kodas AS prodid,klij_kaina AS price FROM klij_kainos ";
			/*                kodas_ks                             CHAR(12)      – Client Code
                pr_kodas                             CHAR(12)      – SKU Code
                klij_kaina                             NUMERIC(12,4)  - Final Special Price (all added up)
*/
			
			 rs = st.executeQuery(sql2);

			while (rs.next()) {

				String custID = rs.getString("custid");
				String prodID = rs.getString("prodid");
				double price = rs.getDouble("price");
                  
				MatrixLine mline = new MatrixLine();

				if (custID != null && custID.trim().length() > 0) {
					mline.setCustID(custID.trim());
				}

				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}

				if (price != 0) {
					mline.setPrice(price);
				}
                   
				//lines.add(mline);
				//System.out.println(discGroup);
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
		         " sum(i44_suma_db-i44_suma_cr) AS tasumata ,max(I44_DATA_DOK) AS kuupaev, max(datediff(d,I44_DATA_MOK,getdate())) AS yle FROM i44_skol,n08_klij WHERE " +
		         " i44_skol.i44_kodas_ks=n08_klij.n08_kodas_ks and " +
		         " exists(select i44_kodas_ks,i44_dok_nr,sum(i44_suma_db-i44_suma_cr) as suma from i44_skol i44 where i44.i44_kodas_ks=i44_skol.i44_kodas_ks " +
		         " and i44.i44_dok_nr=i44_skol.i44_dok_nr and i44_tipas<>11 and i44.i44_data_dok<=getdate() group by i44.i44_kodas_ks,i44.i44_dok_nr " +
		        " having sum(i44.i44_suma_db-i44.i44_suma_cr)<>0) group by I44_DOK_NR,I44_KODAS_KS order by I44_KODAS_KS,i44_dok_nr ";

		      connectionUp();
		      PreparedStatement st = con.prepareStatement(sql);
		      //st.setString(1, agentCode);
		      ResultSet rs = st.executeQuery();
		      while(rs.next()){
		        Debt debt = new Debt();
		        debt.setCustID(rs.getString("klient").trim());
		        debt.setPayerID(debt.getCustID());
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

        String  cmd = "    SELECT  I17_KODAS_PS AS toode, sum(I17_KIEKIS-I17_REZERVAS- I17_ATIDUOTA) AS STOCK " +

      "  from i17_vpro  where I17_KODAS_IS in ('1','2') AND ((I17_KIEKIS) >0 OR (I17_P_PAR_K)>0)  " +

                                 "  GROUP BY I17_KODAS_PS   ORDER BY I17_KODAS_PS" ;
      

        cmd = "SELECT toode, STOCK " + 
        		"  FROM AMBER_EUR.dbo.vwStock" ;

           if (isDebug()) System.out.println(cmd);


           StringBuffer products = new StringBuffer();
           try {

           connectionUp();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(cmd);

             while (rs.next()) {

               String kood = rs.getString("toode").trim();
               double seis = rs.getDouble("stock");
               
                   products.append(kood +  ";");
                   products.append(" ;");
                   products.append(getParent().getNF().format(seis) + "; "  +  "\n"); 
                 
               }
               rs.close();
           } catch (Exception e) {
               getParent().showError(e);
           }
           return products.toString();
       }





        public String getDistribution() {
          return null;
        }


    private void connectionUp(String url){
      try {
        if (con != null){
          con.close();
        }

           con = DriverManager.getConnection(url,"mobjava","mobjava");
      } catch (Exception e) {
            getParent().showError(e);
      }

    }

    
    
    public TreeMap getProdGroups() throws SQLException {

  	  connectionUp(url2);

        TreeMap tm = new TreeMap();

              String cmd = " SELECT  N19_KODAS_GS AS kood ,rtrim( N19_KODAS_GS) + ' ' + rTRIM(N19_PAV)   AS nimi " +
          "   FROM  n19_pgr WHERE  N19_LYGIS = 1 and N19_RUSIS=1 ORDER BY N19_KODAS_GS";
         try {
          stmt = con.prepareStatement(cmd);
      
          
          ResultSet rs = stmt.executeQuery();
        // setTreeGroup(true);
          while (rs.next()) {
            String liik = rs.getString("kood").trim();
            String nimetus = rs.getString("nimi").trim();

            
            if (liik != "") {
              tm.put(liik, Converter.enCodeEst(nimetus));
              
            }
          }
        }
        catch (Exception e) {
          getParent().showError(e);
        }
        connectionDown();
        
        return tm;
      }


    public TreeMap getProdGroupsSecond() throws SQLException {
       connectionUp(url2);
        TreeMap tm = new TreeMap();
        System.out.println("tootegrupid2");
 
          

            String cmd = "   select L01_KODAS_FS as kood , L01_PAV as nimi from l01_filh where L01_TIPAS=1 and left(L01_PAV,3) = 'BR:'";
       try {
        stmt = con.prepareStatement(cmd);
        ResultSet rs = stmt.executeQuery();
       setTreeGroup(true);
       tm.put("xxxx", "Kitos");
        while (rs.next()) {
          String liik = rs.getString("kood").trim();
          String nimetus = rs.getString("nimi").trim();
        
          if (liik != "" && nimetus != "") {
            tm.put(liik.trim(),Converter.enCodeEst(nimetus.trim()));
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
              " ORDER by N15_PAV";
        TreeMap agents = new TreeMap();

        try {
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();
        
            while (rs.next()) {
              Agent agent = new Agent(rs.getString("kood").trim());
              String name = rs.getString("nimi").trim() + " " + agent.getID();
        
              agent.setName(name);
              agent.setIsPilot(true);
              agents.put(name, agent);
            }
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        Agent aa= new Agent("HOUSE");
        aa.setIsPilot(true);
        aa.setName("HOUSE");
        agents.put("HOUSE", aa);
        return agents;
    }

    
    public TreeMap getCustomers(Agent agent){
        connectionUp(url2);
        String cmd = " SELECT   ltrim(N08_KODAS_KS) AS kood1, (case when (N33_KODAS_AK) is null then '1' else ltrim(str(N33_KODAS_AK)) end) AS kood, " +
        		" left (N08_KODAS_GS,5) as horeca, " +
        		" rtrim(N08_PAV) + ' ' + (case when (N33_KODAS_AK) is null then '1' else ltrim(str(N33_KODAS_AK))end)  AS nimetus, " +
				" rtrim(N33_ADRESAS)   AS aadress, 0  AS ale , " +
				"   N33_SHIP_FOB AS visit ,N08_KODAS_KS AS payer , " +
				 " (CASE WHEN LEN(LTRIM(N33_KODAS_MS))=0 or N33_SHIP_VIA IS NULL THEN  N08_KODAS_MS_P ELSE " +
                 "  N33_KODAS_MS END) AS agent , n33_ship_via " +
				 " AS grupp ,  dbo.pap(L12_LAUKAS,3,'') as custType " +
				 " FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS " +
                 " LEFT JOIN L12_PAP2 ON L12_KODAS1 =N08_KODAS_KS and L12_KODAS_NR='KSN33' and L12_KODAS2=N33_EIL_NR " +
                " order by N08_PAV ";
        
        cmd = "SELECT ltrim(N08_KODAS_KS) AS kood1, \r\n" + 
        		"(case when (N33_KODAS_AK) is null then '1' else ltrim(N33_KODAS_AK) end) AS kood, \r\n" + 
        		"left (N08_KODAS_GS,5) as horeca,\r\n" + 
        		"rtrim(N08_PAV) + ' ' + (case when (N33_KODAS_AK) is null then '1' else ltrim(N33_KODAS_AK)end)  AS nimetus, \r\n" + 
        		"rtrim(N33_ADRESAS)   AS aadress, 0  AS ale , N33_SHIP_FOB AS visit ,N08_KODAS_KS AS payer , \r\n" + 
        		"(CASE WHEN LEN(LTRIM(N33_KODAS_MS))=0 or N33_SHIP_VIA IS NULL THEN  N08_KODAS_MS_P ELSE N33_KODAS_MS END) AS agent ,\r\n" + 
        		"n33_ship_via AS grupp ,  dbo.pap(L12_LAUKAS,3,'') as custType \r\n" + 
        		"FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS \r\n" + 
        		"               LEFT JOIN L12_PAP2 ON L12_KODAS1 =N08_KODAS_KS and L12_KODAS_NR='KSN33' and L12_KODAS2=N33_EIL_NR \r\n" + 
        		"order by N08_PAV ";

        log(cmd);
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        try {
        	
        	String maksql = "select a.i04_kodas_ks as klient, a.i04_dok_nr as makse, a.i04_op_data as data, a.i04_suma as summa " +
            " from i04_ath as a where a.i04_kodas_ch in (select top 3 b.i04_kodas_ch from i04_ath as b " +
            " where i04_perkelta = 2 and i04_op_rusis = 1 and year(getdate()) - year(b.i04_op_data) <= 2 " +
           " and a.i04_kodas_ks = b.i04_kodas_ks order by i04_op_data desc) " +
           " order by a.i04_kodas_ks, a.i04_op_data ";
        	HashMap makstud = new HashMap();
            stmt = con.prepareStatement(maksql);
            ResultSet	      rs = stmt.executeQuery();
           

            while (rs.next()) {
            	String kood = rs.getString("klient").trim();
            	String info = rs.getString("makse").trim() + " " + rs.getString("data").substring(0,10) + " " +rs.getDouble("summa") + "lt." ;
            	if(makstud.get(kood) != null){
            		String asi = (String)makstud.get(kood) + "|" + info ;
            	 	makstud.put(kood, asi);                   
            	}else{
            	 	makstud.put(kood, info);
            	}
            }
            rs.close();
        	
        	
        	
        	
            stmt = con.prepareStatement(cmd);
            	      rs = stmt.executeQuery();
           

            while (rs.next()) {
            	
              String accountnum = rs.getString("kood1").trim();
              String acc = rs.getString("kood");
              if (acc != null && acc.trim().length()>0){
              	accountnum = accountnum + "-" + acc.trim();
              }
              String xxx = "";
      
              String name = rs.getString("nimetus");
              if(name == null ) name = "";
              Customer cust = new Customer(accountnum);
             
            
              String aadr = rs.getString("aadress");
              String aad = "";
              if(aadr != null && aadr.trim().length()>0){
                cust.setAddress(Converter.enCodeEst(Converter.adressLink(aadr.trim(),"Lithuania")));
                aad = aadr.trim();
              }
              cust.setName(Converter.enCodeEst((xxx+name.trim() + " " + aad)));

              if (cust.getID().equals("9999999")) {
                  Enumeration enums = getParent().getAgentsHash().elements();
                    while (enums.hasMoreElements()) {
                      Agent a = (Agent) enums.nextElement();
                      cust.addAgent(a);
                      }
                    }
              
             
              
              String discStr = rs.getString("ale");
              if (discStr != null){
                cust.setDiscount(Double.parseDouble(discStr));
              }
              
              String visit = rs.getString("visit");
              if (visit != null && visit.trim().length()>0){
                cust.setVisit(visit.trim());
              }
              
              
              String payer =rs.getString("payer");
              if(payer != null && payer.trim().length()> 0){
                cust.setPayer(payer.trim());
              }

              if(makstud.get(cust.getPayer()) != null){
            	  cust.setInfo((String) makstud.get(cust.getPayer()));
              }
              if(makstud.get(cust.getID()) != null){
            	  cust.setInfo((String) makstud.get(cust.getID()));
              }
              cust.addAgent(new Agent(rs.getString("agent").trim()));
                
                
                String grupp = rs.getString("grupp");
                if(grupp != null && grupp.trim().length()>0){
                	cust.setGroup(grupp.trim());
                	cust.addFixSortCode(grupp.trim());
                }

                String custType = rs.getString("custType");
                if(custType != null && custType.trim().length()>0){
                	cust.setCustType(custType.trim());
                	//System.out.println(cust.getID() +" " + cust.getCustType());
                    
                }
                
              customers.put(name, cust);
              if(custMap.get(cust.getPayer()) != null){
            	  Vector cc = (Vector)custMap.get(cust.getPayer());
            	  cc.addElement(cust);
            	  custMap.put(cust.getPayer(), cc);
              }else{
            	  Vector ss = new Vector ();
            	  ss.addElement(cust);
                  custMap.put(cust.getPayer(),ss );  
              }
            }

            String sql =
                "SELECT I44_KODAS_KS AS klient, sum(i44_suma_db-i44_suma_cr) AS summa , max(datediff(d,I44_DATA_MOK,getdate())) AS yle FROM i44_skol,n08_klij WHERE " +
               " i44_skol.i44_kodas_ks=n08_klij.n08_kodas_ks and " +
               " exists(select i44_kodas_ks,i44_dok_nr,sum(i44_suma_db-i44_suma_cr) as suma from i44_skol i44 where i44.i44_kodas_ks=i44_skol.i44_kodas_ks " +
               " and i44.i44_dok_nr=i44_skol.i44_dok_nr and i44_tipas<>11 and i44.i44_data_dok<=getdate()group by i44.i44_kodas_ks,i44.i44_dok_nr "+
                 " having sum(i44.i44_suma_db-i44.i44_suma_cr)<>0) group by I44_KODAS_KS order by I44_KODAS_KS ";

            stmt = con.prepareStatement(sql);
             ResultSet	      rs1 = stmt.executeQuery();

            while (rs1.next()) {
              String id = rs1.getString("klient").trim();
              double debt = Double.parseDouble(rs1.getString("summa").trim());
              int yle = rs1.getInt("yle");

              Vector custv = (Vector) custMap.get(id);
              if (custv != null) {
            	  
            	  for (int i = 0;i<custv.size();i++){
            		  Customer cust = (Customer)custv.elementAt(i);
            		  cust.setDebt(cust.getDebt() + debt );
                      if(yle < 15 ){
                      	cust.setName("L " + cust.getName());
                      }else if(yle <= 15 && yle < 90){
                      	cust.setName("M " + cust.getName());
                      }else if (yle >= 90){
                      	cust.setName("X " + cust.getName());
                      }
            	  }
            	   
                
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


    
    
    

    public TreeMap getProducts(Agent agent){
        connectionUp(url2);

        TreeMap products = new TreeMap();
        try {
        	
        	
        	HashMap gruppmap = new HashMap();

        	
        	
        	  String  sql = " select L01_KODAS_FS as kood, L02_KODAS_FIL as toode from l02_fild inner join l01_filh on L01_KODAS_FS=L02_KODAS_FS where L01_TIPAS=1 and left(L01_PAV,3) = 'BR:' " +
				" order by  L01_KODAS_FS,L02_KODAS_FIL" ;
       	   //('00000001','T0000002')
              System.out.println("teen GruppMap-i");
            if (isDebug()) System.out.println(sql);
            connectionUp(url2);
               Statement stm = con.createStatement();
               ResultSet rst = stm.executeQuery(sql);

                 while (rst.next()) {
                   String kood = rst.getString("kood").trim();
                    String toode  = rst.getString("toode").trim();
                	  gruppmap.put(toode, kood);
                               
               }
               rst.close();
               stm.close();
           	
           	

        	
        	
        	
        	
        	
        	
     	     sql = "   SELECT  I17_KODAS_PS AS toode, sum(I17_KIEKIS-I17_REZERVAS - I17_ATIDUOTA) AS STOCK " +

       " from i17_vpro  where I17_KODAS_IS in ('1','2') AND ((I17_KIEKIS) >0 OR (I17_P_PAR_K)>0)"  +

                                 "  GROUP BY I17_KODAS_PS   ORDER BY I17_KODAS_PS" ;
     	     
     	     
     	    sql = " SELECT toode, STOCK " + 
     	    		"  FROM AMBER_EUR.dbo.vwStock" ;
     	     		
    	   //('00000001','T0000002')
           System.out.println("laoseisud");
         if (isDebug()) System.out.println(sql);
         connectionUp(url2);
         TreeMap tootedladu = new TreeMap();
            stm = con.createStatement();
            rst = stm.executeQuery(sql);

              while (rst.next()) {
                String toode = rst.getString("toode").trim();
                int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                    tootedladu.put(toode,seis+ "");	
               
               
            }
            rst.close();
            stm.close();
        	
        	
        	
        	
        	
        	
        	
        	
 	     sql = "select I33_KODAS_PS AS toode, max(I33_KAINA) AS hind from I33_PKAI GROUP BY I33_KODAS_PS order by I33_KODAS_PS " ;
       System.out.println("hind");
     if (isDebug()) System.out.println(sql);
     connectionUp(url2);
     TreeMap tooted = new TreeMap();
         stm = con.createStatement();
         rst = stm.executeQuery(sql);

          while (rst.next()) {
            String toode = rst.getString("toode").trim();
            double hind = rst.getDouble("hind");
        
           	 Toode tt = new Toode(toode);
            
                tt.setKastis(hind);
                tooted.put(toode,tt);	
           
           
        }
        rst.close();
        stm.close();
        

        String cmd = "SELECT N17_KODAS_PS AS toode,rtrim(N17_PAV)  AS nimetus, " +

              "  N37_BAR_KODAS AS ean,N17_KODAS_KS1 AS ean2 , x.n27_kiekis_pak AS kastis,  tevas as productGroup, N17_MEN_PAV AS minkogus " +  

             " ,N17_KODAS_LS_4 AS status5 , N17_MAX_NUOL as maxi ,N17_KODAS_US as yhik ,N37_BRUTTO As kaal ,N17_KODAS_LS_5 as status" +

             " , N17_KREPS_POZ as fixcase " +

              " FROM n17_prod a INNER JOIN Top_Groups g on N17_KODAS_GS = KODAS_GS  " +

               "  LEFT JOIN  n37_pmat ON n37_kodas_ps=n17_kodas_ps  and N37_KODAS_US = N17_KODAS_US " +

              " LEFT JOIN  (select n27_kodas_ps, n27_kodas_us, n27_kiekis_pak from n27_pak where n27_kodas_us_pak = 'MIN_PAK') as x " +

        " on  x.n27_kodas_ps = n17_kodas_ps and x.n27_kodas_us = n17_kodas_us " +

             "  where n17_tipas = 1 and n17_kodas_ps not in (SELECT d.l02_kodas_fil FROM L01_FILH h, L02_FILD d " +

           " where h.l01_kodas_fs=d.l02_kodas_fs and h.L01_kodas_fs in ('NA', 'PRIVAT') and h.L01_tipas = 1) and " +

           "  ( N17_POZ_DATE =0 or (N17_POZ_DATE =1 and N17_END_DATE > getdate())) and  len(N17_KODAS_PS)>4 and  N17_TEMPER_POZ =0 and " +

          "  N17_KODAS_GS in (select kodas_gs from Top_Groups) order by N17_KODAS_PS";


  /*   String cmd7 = "SELECT N17_KODAS_PS AS toode,rtrim(N17_PAV)  AS nimetus,  " +
        "   N37_BAR_KODAS AS ean,N17_KOD_T1 AS ean2 , 0 AS kastis,  tevas as productGroup, N17_MEN_PAV AS minkogus   " +
        " ,N17_KODAS_LS_4 AS status , N17_MAX_NUOL as maxi ,N17_KODAS_US as yhik ,N37_NETTO As kaal  " +
        "  FROM n17_prod a INNER JOIN Top_Groups g on N17_KODAS_GS = KODAS_GS  " +
        "    LEFT JOIN  n37_pmat ON n37_kodas_ps=n17_kodas_ps  and N37_KODAS_US = N17_KODAS_US   " +
        "  where n17_tipas = 1 and n17_kodas_ps not in (SELECT d.l02_kodas_fil FROM L01_FILH h, L02_FILD d " +
       " where h.l01_kodas_fs=d.l02_kodas_fs and h.L01_kodas_fs = 'NA' and h.L01_tipas = 1) and " +
       " ( N17_POZ_DATE =0 or (N17_POZ_DATE =1 and N17_END_DATE > getdate())) and  len(N17_KODAS_PS)>4 and  N17_TEMPER_POZ =0 and " +
      " N17_KODAS_GS in (select kodas_gs from Top_Groups) order by N17_KODAS_PS";
        */
        
 /*  String cmd1 = "  SELECT N17_KODAS_PS AS toode,rtrim(N17_PAV)  AS nimetus,N37_KIEKIS_PAK  AS tykke, 0 AS myygihind,  "+
   	"	N37_BAR_KODAS AS ean,N17_KOD_T1 AS ean2 , "+
    "  N37_TAROS_SVORIS AS kogus,  left(N17_KODAS_GS,6) AS grupp2, N17_KODAS_GS AS grupp , N19_TEVAS as par , "+
	"   (select N19_TEVAS from n19_pgr where N19_KODAS_GS= g.N19_TEVAS) as par2,N17_MEN_PAV AS minkogus  "+
    "  ,N17_KODAS_LS_4 AS status , N17_MAX_NUOL as maxi ,N17_KODAS_US as yhik ,N37_NETTO As kaal "+
    "   FROM n17_prod a INNER JOIN n19_pgr g on N17_KODAS_GS = N19_KODAS_GS and N19_POZ_DATE =0 "+
     "  LEFT JOIN  n37_pmat ON n37_kodas_ps=n17_kodas_ps  and N37_KODAS_US = N17_KODAS_US  "+
    " where( N17_POZ_DATE =0 or (N17_POZ_DATE =1 and N17_END_DATE > getdate())) and  len(N17_KODAS_PS)>4 and  N17_TEMPER_POZ =0 "+
    "   order by N17_KODAS_PS";*/
        if (isDebug()) {
            System.out.println(cmd);
        }

      
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();
          
            
            
            while (rs.next()) {
              String kood = rs.getString("toode").trim();
              Product prod = new Product(kood);
              String nimetus =  rs.getString("nimetus").replaceAll("\""," ").trim();
             

              String ean = rs.getString("ean");
              if(ean != null && ean.trim().length()>0 && !ean.trim().equals("000000000000")){
              	prod.setEAN(ean.trim());
              
              try{
            	  EAN13CheckDigit ee = new EAN13CheckDigit();
                  prod.setEAN(ean.trim() + ee.calculate(ean.trim()));
              }catch(Exception e){
            	  
              }
              }
              String ean2 = rs.getString("ean2");
              if(ean2 != null && ean2.trim().length()>0 && !ean2.trim().equals("000000000000")){
            	//	prod.setEAN(ean2.trim());  
              }
              
              prod.setVat("21");
//              String price = ("3.000|15.000|0.000");
               if(tootedladu.get(prod.getID())!= null){
                   prod.setStock(Double.parseDouble((String)tootedladu.get(prod.getID())));
               }
              String kastis = rs.getString("kastis");
              if (kastis != null && kastis.trim().length()>0){
                prod.setInCases(Double.parseDouble(kastis));
              }
              
              String vv = (String)gruppmap.get(prod.getID());
              
                    if (vv != null ){
                      prod.setGroupSecond(vv.trim());
                    }
                    
                    String grupp = rs.getString("productGroup");
                    if (grupp != null && grupp.trim().length()>0){
                      prod.setGroup(grupp.trim());
                    }
                    
                 
                    String aa = prod.getGroupSecond() + " ";
                    
                    if(aa!= null && aa.length()>2){
                    	aa = aa.substring(0, 3) + " ";
                    }else {
                    	aa = "    ";                    }
                    
                    if(prod.getEAN() != null && prod.getEAN().trim().length() != 0 ){
                    	nimetus = nimetus + " " + prod.getEAN().substring(prod.getEAN().length()-4);                   	
                    }
                    prod.setName(Converter.enCodeEst( aa + nimetus ));
    /*           String minam = rs.getString("minkogus");
               double res = 0;
               if (minam != null && minam.trim().length()>0){
               	try{
               	if(minam.indexOf("/")!=-1){
               		res = Double.parseDouble(minam.trim().substring(0,minam.indexOf("/")));
               	}else if(minam.indexOf("*")!=-1){
               		res = Double.parseDouble(minam.trim().substring(0,minam.indexOf("*")));
               	}else{
               		res = Double.parseDouble(minam.trim());
               	}
               	}catch (Exception e){
               		e.printStackTrace();
               	}
               	prod.setInCases(res);
               }*/
               
                  	String yhik = rs.getString("yhik");

                    if(yhik != null && yhik.trim().length()>0){
                    	prod.setUnit(yhik.trim());
                    }
                    
                    
                  	double kaal = rs.getDouble("kaal");

                    if(kaal >0){
                    	prod.setMeasure(kaal);
                    }
                    
                    String status = rs.getString("status") ;
                    
                    if(status != null){
                    	if(status.trim().equals("NEW")){
                    		prod.setStatusNew("1");
                    	}
                    	if(status.trim().equals("OUT")){
                    		prod.setStatusSellout("1");
                    	}
                    	if(status.trim().equals("TOP")){
                    		prod.setStatusTop("1");
                    	}
                    	if(status.trim().equals("DISCOUNT")){
                    		prod.setStatusDiscount("1");
                    	}
                    	if(status.trim().equals("FOCUS")){
                    		prod.setMustBe("1");
                    	}
                    	
                    }
                    
                    
               Toode t = (Toode) tooted.get(kood);

               
                  if(t != null){
                  	double kogus = t.getKogus();
                  	 if(kogus!= 0){
                      	//prod.setStock(kogus);
                      }
                  	 
                  double	pr = t.getKastis(); 
                  	if(pr!= 0){
                      	prod.setPrice(pr);
                    }
                
                  	//tooted.remove(kood);
                  }
               
               
                  /*String stat = rs.getString("status");
               if (stat != null && stat.trim().length()>0){
               	if(stat.trim().equals("1")){
               		prod.setStatusNew("1");
               	}
               	if(stat.trim().equals("2")){
               		prod.setStatusDiscount("1");
               	}
               	if(stat.trim().equals("3")){
               		prod.setStatusSellout("1");
               	}
               }
               double max = rs.getDouble("maxi");
               if(max > 0){
               	if(max < 1){
               		max = 2;
               	}
               	prod.setMaxDiscount(max);
               }*/
                  
                  String fixcase = parent.make(rs.getString("fixcase"));
                  if(fixcase.trim().length() !=0 && fixcase.trim().equals("1") ){
                	//  System.out.println("fixcase " + prod.getName() +  " kastis" + prod.getInCases() );
                	  if(prod.getInCases() != 0){
                		  prod.setOptAmountInUnits(prod.getInCases());
                    	  //prod.setMinAmountInUnits(prod.getInCases());
                    	    
                	  }
                	  //System.out.println("fixcase " + prod.getName() +  " kastis" + prod.getOptAmountInUnits() ); 
                  }
                  
                  if(prod.getPrice() >0 && prod.getName().indexOf("****") == -1 ){
                      products.put(prod.getName(), prod);       	  
                  }

                // }
            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        return products;
    }

    public static String deCode(String res ){

      	return res;
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
        
        String cmd ="SELECT ltrim(I06_KODAS_KS)  AS klient,(case when (N33_kodas_ak) is null then '1'  " +
  "   else ltrim(N33_kodas_ak) end) as kood, " +
  " I07_KODAS AS toode,I07_KIEKIS AS kogus,  I06_DOK_DATA AS kuupaev " +
  " FROM i07_pard " +
  " INNER JOIN i06_parh ON I07_KODAS_PO=I06_KODAS_PO " +
  " left JOIN N33_KBAN ON   N33_KODAS_KS = I06_KODAS_KS AND I06_kodas_ak=N33_kodas_ak " +
  " WHERE I06_DOK_DATA > ? " +
  "  GROUP BY I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ltrim(N33_kodas_ak) end), " +
  " I07_KODAS,I06_DOK_DATA,I07_KIEKIS order by klient,kood,toode,kuupaev desc " ;


        if (isDebug()) {
           System.out.println(cmd);
       }

       Vector custProds = new Vector();
       HashMap kliendid = new HashMap();
       HashSet done = new HashSet();


       try {
          stmt = con.prepareStatement(cmd);
          stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
           ResultSet	      rs = stmt.executeQuery();

           while (rs.next()) {
             String kliendikood = rs.getString("klient").trim();
             String kood = rs.getString("kood");
             if (kood != null && kood.trim().length()> 0){
             	kliendikood = kliendikood +  "-" + kood.trim();
             }
             
             String tootekood = rs.getString("toode").trim();
             double kogus = Double.parseDouble(rs.getString("kogus").trim());
             String kuup = rs.getString("kuupaev").substring(5,10).trim();
         
            if(!done.contains(kliendikood +"|"+ tootekood)) {
             custProds.add(new CustProd(kliendikood, tootekood, kogus, 0, 0,
                                          kuup, 0, 0,0," "));
             done.add( kliendikood +"|"+  tootekood);
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

 	   	

        StringBuffer sb = new StringBuffer();
        try {
        	
        	
 	   	
       File dir = new File(getParent().getProps().getProperty("pop3.dir"));
      
       if(dir.exists() && dir.isDirectory()){
    	   String [] files = dir.list();  
    	   if(files.length>0){
    		   for (int i = 0;i<files.length;i++){
        		  
        		   if (files[i].toLowerCase().indexOf(agent.getID().toLowerCase())!= -1 && files[i].toLowerCase().endsWith("log")){
        			 //  System.out.println(files[i]);  
        			   File f = new File(getParent().getProps().getProperty("pop3.dir") + (files[i].substring(0,files[i].indexOf(".")) + ".LOG").toUpperCase());
        			   //System.out.println(f.getAbsolutePath());  
        			   if(f.exists()){
        				   FileReader fr = new FileReader(f.getAbsoluteFile());
        				  
        				   BufferedReader reader = new BufferedReader(fr);
        			        String line = " ";
        			        
        			      while( ((line = reader.readLine())!= null )){
        			    	//  System.out.println(line); 
        			    	  if(line.trim().startsWith("Sufor")){
        			    		  sb.append(line.substring(line.indexOf("Klientas")+9) + "\n");
        			    	  }
        			    	  if(line.trim().startsWith("*Klaida")){
        			    		  sb.append("   " +line.substring(line.indexOf("Klaida")+8) + "\n");
        			    	  }
        			    	  
        			      }
        				   
        				   
        				   
        				   
        			   }
        			   
        		   }     		   
    		   }
    	   }
       }
        
                        
                        } catch (Exception e) {
                            getParent().showError(e);
                        }
                      //  System.out.println(agent.getID() + sb.toString());
           if(sb.toString().trim().length()>0){
        	  // return null;
            return sb.toString();
           }else {
           	return null;
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

    
    private void makePictureCat(){


  	  String cmd = " SELECT N17_KODAS_PS AS toode,ltrim(N17_PAV)  AS nimetus,dbo.papn(L11_LAUKAS , 8, 'c')  AS tykke,dbo.papn(L11_LAUKAS , 9, 'c')  AS alusel, 0 AS myygihind," +
  		   		"N17_KODAS_PS AS ean, "+
  		   " 0 AS kogus,n17_kodas_ls_5 AS grupp, N17_KODAS_GS AS grupp2 ,N17_MEN_PAV AS minkogus ,N17_KODAS_LS_4 AS status , N17_MAX_NUOL as maks," +
  		   " case when N17_DATA_NAUJA>getdate() AND N17_AR_NAUJA=1 then 1 else 0 end AS uus " +
  		   " FROM n17_prod a  LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps left join L11_PAP1 b on b.L11_KODAS_NR = 'PSN17' and N17_KODAS_PS = L11_KODAS1 where  N17_POZ_DATE !=1" +
  		   " " ;


        String picts = parent.getProps().getProperty("pict.dir");
       	String pictspda = parent.getProps().getProperty("pictpda.dir");

       	HashSet added = new HashSet();

   	   //Kataloogi sisude kustutamine
   	        File res2 = new File (pictspda);
   	            String[] ff2 = res2.list();
   	        	 for(int i = 0;ff2.length>i;i++){
   	            	//(new File ( pictspda + ff2[i])).delete(); 
   	            }
       	
       	
	   	try {
	   		connectionUp();
	   		Statement stmt = con.createStatement();
	   	    ResultSet	      rs = stmt.executeQuery(cmd);


	        StringBuffer buf = new StringBuffer();
	      
	        int a = 0;
	        int c = 0;
	        
	        // leiame kataloogid ja peame meeles 
	        
	        while (rs.next()) {
	   	    	a++;
	   	    	c++;
	   	     String toode = rs.getString("toode").trim();  
	         toode = toode.replace('/', '_');
	         toode = toode.replace('/', '_');
	        
	         String ean = rs.getString("ean");
           if(ean != null && ean.trim().length()>0){
         	  try{
         		  EAN13CheckDigit ee = new EAN13CheckDigit();
         		  String ean2 = ean.trim() + ee.calculate(ean.trim());
                  ean = ean2;
         	  }catch(Exception y){
        	  }
           }
    
      	 
        System.out.println("Piltide kopeerimine ");	
      
       	  //System.out.println("gg " + gg[0] + " " + gg.length);
                 	
           	File in = new File ( picts + toode + ".jpg"); 
           	if(in.isFile()){            	
           	File inpda = new File ( pictspda + toode + ".jpg" ); 
           	if(!inpda.exists()) {
           	 	Converter.shrinkAndCopyFile(in,inpda);
           	}
          
           	}
           
           
	   	    	if(c == 100){
	   	    		  c = 0;
	   	    		//  System.out.println("Piltide leidmine "  + a);	
	   	   		}
	   	           
	                 File f = new File(pictspda + toode+".jpg");
	                 String len = "0";
	                 if(f.exists()){
	                   len =	f.length() + "";
	                   if(!added.contains(toode))
	                   buf.append(toode+".jpg;"  + len + "\n");
	                   added.add(toode);
	           	     } 
	       
	                 
	                 }
	   	    rs.close();
	   	    stmt.close();
	   	    con.close();
	   	
	      
	               File f = new File("list.txt");
	               if (f.exists())f.delete();
	               
	                 try {
	           		PrintWriter os = new PrintWriter(new FileWriter(
	           				  "list.txt", true));
	           		os.write(buf.toString());
	           		os.close();
	           	
	           	  }catch (Exception ex) {
	           		ex.printStackTrace();
	           	  }
	     	             
	             File fff =  new File (pictspda +  "list.txt");
	       	  Converter.copyFile(f,fff);  	
	     	
	   	} catch (Exception e) {
	   	    getParent().showError(e);
	   	}
	   
	}


    
    
    
    
    
    public void log(String s) {
        getParent().log(s);
    }

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "RivileAmber";
    private String         url = "jdbc:odbc:amber";
    private String	       url2 = "jdbc:odbc:amber";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
	@Override
	public void PictureMaker() {
		  makePictureCat();
 		  System.exit(0);
		
	}
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

