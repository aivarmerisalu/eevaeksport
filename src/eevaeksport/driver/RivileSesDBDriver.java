/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
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
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class RivileSesDBDriver extends AbstractDBDriver implements  BulkUpdater,Discounter{


	
    public RivileSesDBDriver(EksportPanel parent) {
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



 public static final String cleanLitLatEst(String ret){
	    if (ret != null){
	    
	        ret = ret.replace('�','e');
	        ret = ret.replace('�','I');
	        ret = ret.replace('�','u');
	        ret = ret.replace('�','c');
	        ret = ret.replace('�','z');
	        ret = ret.replace('�','s');
	        ret = ret.replace('�','S');
	        ret = ret.replace('�','c');
	        ret = ret.replace('�','I');
	        ret = ret.replace('�','u');
	        ret = ret.replace('�','i');
	        ret = ret.replace('�','c');
	        ret = ret.replace('�','Z');
	        ret = ret.replace('�','E');
	        ret = ret.replace('�','U');
	        ret = ret.replace('�','U');

	        ret = ret.replace('�','Z');
	        ret = ret.replace('�','A');
	        ret = ret.replace('�','a');
	        ret = ret.replace('�','C');

	        ret = ret.replace('�','c');
	        ret = ret.replace('�','E');
	        ret = ret.replace('�','e');
	        ret = ret.replace('�','G');
	        ret = ret.replace('�','g');
	        ret = ret.replace('�','I');
	        ret = ret.replace('�','i');
	        ret = ret.replace('�','K');
	        ret = ret.replace('�','k');
	        ret = ret.replace('�','L');
	        ret = ret.replace('�','l');
	        ret = ret.replace('�','N');
	        ret = ret.replace('�','n');
	        
	        ret = ret.replace('�','o');
	        ret = ret.replace('�','a');
	        ret = ret.replace('�','o');
	        ret = ret.replace('�','u');
	        
	    	
	    }
	        
	        if(ret == null || ret.trim().length()==0){
	        return " ";
	        }else {
	        return ret;
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

		String sql = " SELECT N31_KODAS_NS AS grupp ,N31_NUOL_PROC as ale  " +
                 "   FROM N31_NUOD p   order by N31_KODAS_NS";

		
		Statement st = null;
		try {
			connectionUp();
			st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {

				String gr = rs.getString("grupp");
				
				String clGroup = gr.substring(0,3).trim();
				
				String alegr = Converter.cleanLitLatEst(gr.substring(3).trim());
				
				double ale = rs.getDouble("ale");
				MatrixLine mline = new MatrixLine();
				
				if (clGroup != null
						&& clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
				    
					mline.setDiscGroup(alegr);
					
					if (ale != 0) {
						mline.setDiscount(ale);
						lines.add(mline);
					}
				}
	}
		
			String sql2 = "SELECT n32_tipas as clgroup ,max(n32_g_date),n32_kodas_ps as toode ,(n32_kaina1) as price " +
	  		" FROM dbo.N32_PABC " +
	  		" where n32_g_date < getdate()   group by  n32_kodas_ps,n32_tipas,n32_kaina1 order by n32_tipas ,n32_kodas_ps";
				
			
			 rs = st.executeQuery(sql2);

			while (rs.next()) {
				String clGroup =rs.getString("clgroup").trim();
				String prodid = Converter.enCodeEst(rs.getString("toode").trim());
				
				double hind = rs.getDouble("price");
				MatrixLine mline = new MatrixLine();
					    
					    mline.setCliGroup(clGroup.trim());
				        mline.setProdID(prodid);
						mline.setPrice(hind);
							 lines.add(mline);
							 System.out.println("line " + mline.toString());
					}
	
			rs.close();
			st.close();
			
			String sql3 = " SELECT N51_KODAS_KS AS grupp,N52_KODAS AS toode ,N52_KAINA as price " +
	         " FROM N51_SUTH p INNER JOIN N52_SUTD r ON N51_KODAS_KT=N52_KODAS_KT where N52_POZ_KAINA =1 and N52_KAINA != 0 " +
	                  " AND getdate() between N52_BEG_DATE and N52_END_DATE order by N51_KODAS_KS,N52_KODAS";
			
			st = con.createStatement();
			 rs = st.executeQuery(sql3);

			while (rs.next()) {

				String clGroup = rs.getString("grupp");
				String prodID = Converter.enCodeEst(rs.getString("toode"));
			
				double price = rs.getDouble("price");
				MatrixLine mline = new MatrixLine();

				
				if (mline.getCustID() == null && clGroup != null
						&& clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
					mline.setStartDate(Converter.getYYYYpMMpDDpString(new Date()));
				}

				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}
			
				if (price != 0) {
					mline.setPrice(price);
				}
				lines.add(mline);
			}
			
			
		} catch (SQLException ex) {
		}
		System.out.println("matrix Lines " + lines.size());
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
		        debt.setDoknr(rs.getString("number").trim());
		        debt.setDuedate(rs.getDate("maksepaev"));
		        debt.setPayerID(debt.getCustID());
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

        String  cmd = "    SELECT  I17_KODAS_PS AS toode, sum(I17_KIEKIS--I17_REZERVAS) AS STOCK " +

      "  from i17_vpro  where I17_KODAS_IS in ('1','2') AND ((I17_KIEKIS) >0 OR (I17_P_PAR_K)>0)  " +

                                 "  GROUP BY I17_KODAS_PS   ORDER BY I17_KODAS_PS" ;
      


           if (isDebug()) System.out.println(cmd);


           StringBuffer products = new StringBuffer();
           try {

           connectionUp();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(cmd);

             while (rs.next()) {

               String kood = rs.getString("toode").trim();
               double seis = rs.getDouble("stock");
               if(seis > 0){
                   products.append(kood +  ";");
                   products.append(" ;");
                   products.append(getParent().getNF().format(seis) + "; "  +  "\n"); 
                 }
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

           con = DriverManager.getConnection(url,"sa","Manager00");
      } catch (Exception e) {
            getParent().showError(e);
      }

    }

    
    
    public TreeMap getProdGroups() throws SQLException {

  	  connectionUp(url2);

        TreeMap tm = new TreeMap();

              String cmd = "  SELECT  N19_KODAS_GS AS kood ,rtrim( N19_KODAS_GS) + ' ' + rTRIM(N19_PAV)   AS nimi " +
         " FROM  n19_pgr WHERE  N19_LYGIS = 3 ORDER BY N19_KODAS_GS";
         try {
          stmt = con.prepareStatement(cmd);
      
          
          ResultSet rs = stmt.executeQuery();
        // setTreeGroup(true);
          while (rs.next()) {
            String liik = rs.getString("kood").trim();
            String nimetus = Converter.enCodeEst(rs.getString("nimi").trim());

            
            if (liik != "") {
              tm.put(liik, nimetus);
              
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
        return new TreeMap();
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

    	
        DealerProduct dealerProds = new DealerProduct();
        try {

          String  sql = "  select toode ,sum(kog) as stock,ladu,max(part) as real from (	" +
			"	SELECT I17_KODAS_PS AS toode, (I17_KIEKIS) AS kog , I17_KODAS_IS AS LADU ,(N06_PAV) as part " +
            "    from i17_vpro LEFT OUTER JOIN N06_OBJ AS b ON I17_KODAS_OS = N06_KODAS_OS  " +
			 "    where (I17_KIEKIS) != 0 ) as aa group by ladu,toode  ORDER BY ladu,toode ";
				 
				

             if (isDebug()) System.out.println(sql);
             connectionUp(url2);
                Statement stm = con.createStatement();
                ResultSet rst = stm.executeQuery(sql);

                  while (rst.next()) {
                    String toode = rst.getString("toode").trim();
                    int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                    String ladu  =  rst.getString("ladu").trim();
                    String real  =  rst.getString("real");
                    if(real != null){
                    	real =  real.trim().replaceAll(";", ",");
                    }else{
                    	real = " ";
                    }
                   
                    dealerProds.add(ladu, toode,seis+"",real," " );
                }
                rst.close();
                stm.close();

        } catch (Exception e) {
            getParent().showError(e);
        }

         return dealerProds;
        // return null;
       }


    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    TreeMap agent1 = new TreeMap();
    public TreeMap getAgents() {
        connectionUp();
        String  cmd = "SELECT N15_KODAS_MS AS kood ,rtrim(N15_PAV) +' (' + rtrim(N15_tel) + ')' AS nimi  FROM  n15_men ORDER by N15_PAV";
        TreeMap agents = new TreeMap();

        try {
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();
        
            while (rs.next()) {
              Agent agent = new Agent(rs.getString("kood").trim());
              String name = rs.getString("nimi").trim() + " " + agent.getID();
        
              agent.setName(name);
              agent.setIsPilot(true);
              
              if(agent.getCode() != null && agent.getCode().equals("VLADAS")){
            	  agent.setObject("AUTO002");
              }else if(agent.getCode() != null && agent.getCode().equals("VYTAUTAS")){
            	  agent.setObject("AUTO003");
              }else{
            	  agent.setObject("AUTO001");
              }
            	  
             
              agents.put(name, agent);
              agent1.put(agent.getID(), agent);
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
        String cmd = "SELECT (N08_KODAS_KS) AS kood1, (case when (N33_kodas_ak) is null  then '1' else ((N33_kodas_ak)) end) AS kood, " +  
        		"  (N08_PAV) as nimi,n33_pav as nimi2, (N33_kodas_ak) AS nimetus, " +
				" N08_ADR AS aadress , n33_adresas as aadress2 ,  rtrim(N08_NUOL_GR) AS ale , " + 
				" ' ' AS visit ,N08_KODAS_KS AS payer ,  " +
				" n08_kodas_ms_p AS agent ,   " +
				" case when N08_KODAS_TS_P in ('VASARA') then '14' else '30' end " +
			 " as paymterm ,n08_mob_tel as phone ,N08_TEL as phone2 ,N08_PVM_KODAS as vatno,N08_IM_KODAS as regno, N08_KAINYNAS as hinnad " +
				" FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS where " +
				 "  ltrim(N08_KODAS_KS ) != '' and N08_RUSIS in  (1,3)  " +
				 "  order by N08_PAV";

        log(cmd);
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        try {
        	
        	
        	
        	
        	
            stmt = con.prepareStatement(cmd);
            	    ResultSet  rs = stmt.executeQuery();
           

            while (rs.next()) {
            	
              String accountnum = rs.getString("kood1").trim();
              String acc = rs.getString("kood");
              if (acc != null && acc.trim().length()>0){
              	accountnum = accountnum + "-" + acc.trim();
              }
           
      
              String name = rs.getString("nimi");
              
              if(name == null ) name = "";
              Customer cust = new Customer(Converter.enCodeEst(accountnum));
             
  String name2 = rs.getString("nimi2");
              
              if(name2 == null ){
            	  name2 = "";
              }else{
            	  name2 = name2.trim();
              }
              
              
              String aadrjur = rs.getString("aadress");
              if(aadrjur != null && aadrjur.trim().length()>0){
                cust.setJuridics(Converter.enCodeEst(aadrjur.trim()));
              }
              
              String aadr = rs.getString("aadress2");
              String add = accountnum;
              if(aadr != null && aadr.trim().length()>0){
            	  add = Converter.adressLink(Converter.enCodeEst(aadr.trim() + " " + name2),"Lithuania");
                cust.setAddress(add);
                
              }
             
              cust.setName(Converter.enCodeEst(name.trim()) + " " + Converter.enCodeEst(add.trim()) );

                  Enumeration enums = getParent().getAgentsHash().elements();
                    while (enums.hasMoreElements()) {
                      Agent a = (Agent) enums.nextElement();
                      cust.addAgent(a);
                      }
             
              
              String discStr = rs.getString("ale");
              if (discStr != null && discStr.trim().length() > 0){
               cust.setGroup(discStr);
              }
              
              String visit = rs.getString("visit");
              if (visit != null && visit.trim().length()>0){
                cust.setVisit(visit.trim());
              }
              
              
              String payer =rs.getString("payer");
              if(payer != null && payer.trim().length()> 0){
                cust.setPayer(payer.trim());
              }
String ag = rs.getString("agent").trim();
	
	if(agent1.get(ag)!= null ) {
	cust.setInfo(Converter.enCodeEst("Aptarnaujantis vadybininkas: " + 
				((Agent)agent1.get(ag)).getName()));
	}
              
              cust.addAgent(new Agent(ag));
                
              
              String paym = rs.getString("paymterm");
              if(paym != null && paym.trim().length() >0){
            	  cust.setPaymTerm(paym.trim());  
              }
             
              String phone = rs.getString("phone");
              if(phone != null && phone.trim().length() > 0){
            	  cust.setPersonalData("TelGSM : " + phone.trim());
              }
              
              
              String phone2 = rs.getString("phone2");
              if(phone2 != null && phone2.trim().length() > 0){
            	  if(cust.getPersonalData() != null && cust.getPersonalData().trim().length() >0 ){
            		  cust.setPersonalData(cust.getPersonalData() + "|Tel: " + phone2.trim());
            	  }else{
            		  cust.setPersonalData("Tel: " + phone2.trim());
            	  }
            	 
              }
                
              
              String vatno = rs.getString("vatno");
              if(vatno != null && vatno.trim().length() > 0){
            	  cust.setVatnr( vatno.trim());
              }
              
              
              String regno = rs.getString("regno");
              if(regno != null && regno.trim().length() > 0){
            	  cust.setRegnr(regno.trim());
              }
              
          
              
              String hinnak = rs.getString("hinnad");
              if(hinnak != null && hinnak.trim().length() > 0){
            	 // if(!hinnak.trim().equals("A")){
            		//cust.setGroup(hinnak.trim());  
            		cust.setCustType(hinnak.trim());  
            	  //}
            	  
              }
              
              
              customers.put(cust.getName(), cust);
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
            	  
            	  for (int i = 0;i < custv.size();i++){
            		  Customer cust = (Customer)custv.elementAt(i);
            		  cust.setDebt(cust.getDebt() + debt );
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

        	
        	
        	  String  sql = " select L01_KODAS_FS as kood, L02_KODAS_FIL as toode from l02_fild inner join l01_filh on L01_KODAS_FS=L02_KODAS_FS where "
        	  		+ " L01_TIPAS=1 and left(L01_PAV,3) = 'BR:' " +
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
           	
           	

        	
        	
        	
        	
        	
        	
     	     sql = "     SELECT  I17_KODAS_PS AS toode, sum(I17_KIEKIS) AS STOCK , case when I17_KODAS_IS= '01' then 'main:' else 'car:' end AS LADU  " +
              "  from i17_vpro where I17_KODAS_IS in ('AUTO001','AUTO002' ,'AUTO003','01') GROUP BY I17_KODAS_IS ,I17_KODAS_PS HAVING " +
              "  sum(I17_KIEKIS) != 0 ORDER BY I17_KODAS_PS " ;
			
    	
           System.out.println("laoseisud");
         if (isDebug()) System.out.println(sql);
         connectionUp(url2);
         TreeMap tootedladu = new TreeMap();
            stm = con.createStatement();
            rst = stm.executeQuery(sql);

              while (rst.next()) {
                String toode = rst.getString("toode").trim();
                int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                String ladu = rst.getString("ladu").trim();
                if(tootedladu.get(toode) == null){
                	  tootedladu.put(toode,ladu + " " + seis+ " ");	
                }else{
                	String asi = (String )  tootedladu.get(toode);	
                	  tootedladu.put(toode,asi + ladu + " " + seis);	
                }
                  
                  }
            rst.close();
            stm.close();
        	
        	
        	
        	sql = "SELECT  I07_KODAS AS toode  FROM i07_pard INNER JOIN i06_parh ON I07_KODAS_PO=I06_KODAS_PO " +
     " left JOIN N33_KBAN ON   N33_KODAS_KS = I06_KODAS_KS AND I06_kodas_ak=N33_kodas_ak " +
     " WHERE I06_DOK_DATA > getdate()-90  GROUP BY I07_KODAS union SELECT  I17_KODAS_PS AS toode " +
      "  from i17_vpro  GROUP BY I17_KODAS_PS HAVING sum(I17_KIEKIS) != 0";
        	  connectionUp(url2);	
        	 stm = con.createStatement();
             rst = stm.executeQuery(sql);
       HashSet aktiivsed = new HashSet();
              while (rst.next()) {
                String toode = rst.getString("toode").trim();
               aktiivsed.add(toode);
            }
            rst.close();
            stm.close();
            

        	
        	
 	     sql = "select I33_KODAS_PS AS toode, max(I33_KAINA) AS hind from I33_PKAI GROUP BY I33_KODAS_PS order by I33_KODAS_PS " ;
       System.out.println("hind");
     if (isDebug()) System.out.println(sql);
   
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
        

        String cmd = "SELECT N17_KODAS_PS AS toode,(N17_PAV)  AS nimetus,N27_KIEKIS_PAK  AS tykke, 0 AS myygihind,  "+
   	"	N40_BAR_KODAS AS ean, " +
   " 0 AS kogus,n17_kodas_ks AS grupp, N17_KODAS_GS AS grupp2 , N17_NUOL_GR AS alegrupp  ,N17_MEN_PAV AS minkogus ,N17_KODAS_LS_4 AS status , N17_MAX_NUOL as maks, "+
   " case when N17_DATA_NAUJA>getdate() AND N17_AR_NAUJA=1 then 1 else 0 end AS uus "+
   " FROM n17_prod a LEFT JOIN  n40_abar  ON n40_kodas_ps=n17_kodas_ps   LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps where  N17_POZ_DATE !=1 order by N17_KODAS_PS ";


        if (isDebug()) {
            System.out.println(cmd);
        }
 
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();
 
            while (rs.next()) {
            	
            	boolean goes = false;
              String kood = rs.getString("toode").trim();
              Product prod = new Product(kood);
              String nimetus =  rs.getString("nimetus").replaceAll("\""," ").trim();
             if(nimetus.toLowerCase().indexOf("akcija") != -1 || nimetus.toLowerCase().indexOf("naujiena") != -1 ){
            	 goes = true;
             }
                prod.setVat("21");

              if(tootedladu.get(prod.getID())!= null){
                   prod.setOldName((String)tootedladu.get(prod.getID()));
               }
           
              
              
               String ean = rs.getString("ean");
              if (ean != null && ean.trim().length()>0){
                prod.setEAN(ean.trim());
              }
              
                    
                    String grupp = rs.getString("grupp2");
                    if (grupp != null && grupp.trim().length()>0){
                      prod.setGroup(grupp.trim());
                    }
                    
                    String alegrupp = rs.getString("alegrupp");
                    if (alegrupp != null && alegrupp.trim().length()>0){
                      prod.setAlegrupp(Converter.cleanLitLatEst(alegrupp.trim()));
                    }
                    
                    
                    String aa = prod.getGroup() + " ";
                    
                    if(aa!= null && aa.trim().length()>2){
                    	aa = aa.substring(0, 2) + " ";
                    }
                    
                   
                    prod.setName(Converter.enCodeEst( nimetus));
                 	String yhik = "vnt";

                    if(yhik != null && yhik.trim().length()>0){
                    	prod.setUnit(yhik.trim());
                    }
                    
               Toode t = (Toode) tooted.get(kood);

               
                  if(t != null){
                  	double kogus = t.getKogus();
                  	 if(kogus!= 0){
                      	//prod.h(kogus);
                      }
                  	 
                  double	pr = t.getKastis(); 
                  	if(pr!= 0){
                      	prod.setPrice(pr);
                    }
                
                  }
         
                 
                  
                  if(prod.getPrice() >0 && ( aktiivsed.contains(prod.getID()) || goes) ){
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
        
        String cmd =" SELECT (I06_KODAS_KS)  AS klient,(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end) as kood," +
		"I07_KODAS AS toode,I07_KIEKIS AS kogus, " +
		"I06_DOK_DATA AS kuupaev FROM i07_pard p" +
		"  INNER JOIN i06_parh ON p.I07_KODAS_PO=I06_KODAS_PO "+
		" left JOIN N33_KBAN ON N33_KODAS_KS = I06_KODAS_KS AND I06_ADR2=N33_ADRESAS " +
		" WHERE I06_DOK_DATA > ? "+
		" GROUP BY I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end),I07_KODAS,I06_DOK_DATA,I07_KIEKIS";



        if (isDebug()) {
           System.out.println(cmd);
       }

       Vector custProds = new Vector();
       HashMap kliendid = new HashMap();


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
        			   System.out.println(files[i]);  
        			   File f = new File(getParent().getProps().getProperty("pop3.dir") + (files[i].substring(0,files[i].indexOf(".")) + ".LOG").toUpperCase());
        			   System.out.println(f.getAbsolutePath());  
        			   if(f.exists()){
        				   FileReader fr = new FileReader(f.getAbsoluteFile());
        				  
        				   BufferedReader reader = new BufferedReader(fr);
        			        String line = " ";
        			        
        			      while( ((line = reader.readLine())!= null )){
        			    	  System.out.println(line); 
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
                        System.out.println(agent.getID() + sb.toString());
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
    public static final String DRIVER_VERSION = "RivileSes";
    private String         url = "jdbc:odbc:rivileses";
    private String	       url2 = "jdbc:odbc:rivileses";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

