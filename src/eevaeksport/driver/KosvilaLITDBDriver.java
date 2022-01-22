/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
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
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class KosvilaLITDBDriver extends AbstractDBDriver implements  BulkUpdater ,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public KosvilaLITDBDriver(EksportPanel parent) {
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
            public String getDealerCustomerCSV(String agent) {
		return null;
	}
            public String getSaveDebtInfo() {
               	return null;
               }
               
        public Vector getFixedAssortmentData() {
          return null;
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
				System.out.println(discGroup);
			}

			rs.close();
			st.close();
			con.close();
		} catch (SQLException ex) {
		}
		return lines;
	}

	 public String getProductSortingData(){
	   	return null;
	   }

		public Vector getLastThreeData() {
			// TODO Auto-generated method stub
			return null;
		}
	

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
     String sql = "SELECT I44_KODAS_KS AS klient,I44_DOK_NR AS number ,max(I44_DATA_MOK) AS maksepaev , " +
         " sum(i44_suma_db-i44_suma_cr) AS tasumata ,max(I44_DATA_DOK) AS kuupaev, max(I44_DATA_MOK-getdate()) AS yle FROM i44_skol,n08_klij WHERE " +
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
        String number = rs.getString("number").trim();
        if(number.length() > 8){
        	number = number.substring(number.length()-8);
        }
        debt.setDoknr(number);
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

           con = DriverManager.getConnection(url,"dba","sql");
      } catch (Exception e) {
            getParent().showError(e);
      }

    }

    
    
    public TreeMap getProdGroups() throws SQLException {

      connectionUp(url2);

      TreeMap tm = new TreeMap();

            String cmd = " SELECT trim(N17_KODAS_KS)+trim(N17_KODAS_LS_1) AS kood ,trim(N08_PAV)+' '+trim(N17_KODAS_LS_1) AS nimi " +
			" FROM n17_prod a INNER JOIN N32_PABC ON N32_KODAS_PS=N17_KODAS_PS " +
			" INNER JOIN n08_klij ON N08_KODAS_KS=N17_KODAS_KS "+
			" WHERE N32_TIPAS='A' AND N17_INTERNET = 1 " +
			" GROUP BY N17_KODAS_KS,N08_PAV,N17_KODAS_LS_1 ORDER BY N17_KODAS_KS,N08_PAV";
       try {
        stmt = con.prepareStatement(cmd);
        ResultSet rs = stmt.executeQuery();
      // setTreeGroup(true);
        while (rs.next()) {
          String liik = rs.getString("kood").trim();
          String nimetus =  Converter.enCodeEst(rs.getString("nimi").trim());
          nimetus = nimetus.replaceAll("Przedsiebiorstwo produkcyjno", "Pre. pro.");
          nimetus = nimetus.replaceAll("COSMETICS DYSTRYBUCJA Sp.z.o.o.", "Cosm.Distr.");
          nimetus = nimetus.replaceAll(" Handlowa ''JOANNA'' Joanna Skibinska", " ");
          nimetus = nimetus.replaceAll("11119xxukos, 11120xxepe11128xxiai, kempin11130xxs", "11119xxu.,11120xxepe.,kemp.");
          nimetus = nimetus.replaceAll("Grupa Kolastyna S.A. w upadlosci ukladowej", "Grupa Kolastyna");
          nimetus = nimetus.replaceAll("Laboratori Di Chimica Applicata S. R.L.", "Laboratori Di");
          nimetus = nimetus.replaceAll("Grupa Kolastyna S.A. w upadlosci ukladowej", "Grupa Kolastyna");
         
          if (liik != "") {
            tm.put(liik,nimetus);
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

              String cmd = " SELECT  N19_KODAS_GS AS kood ,N19_PAV  AS nimi  FROM  n19_pgr  where "
              		+ "N19_LYGIS = 2 and N19_RUSIS = 1 ORDER BY N19_KODAS_GS";
         try {
          stmt = con.prepareStatement(cmd);
          ResultSet rs = stmt.executeQuery();
        // setTreeGroup(true);
          while (rs.next()) {
            String liik = rs.getString("kood").trim();
            String nimetus = rs.getString("nimi");
            if (liik != "") {
            	if(nimetus == null || nimetus.trim().length() == 0) {
            		nimetus = liik;
            	}
              tm.put(liik, Converter.enCodeEst(nimetus.trim()));
            }
          }
        }
        catch (Exception e) {
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
        	if(con != null){

                con.close();
                con = null;	
        	}
        } catch (Exception e) {
            getParent().showError(e);
        }
    }
    public DealerProduct getDealerProductsList() {


        DealerProduct dealerProds = new DealerProduct();
        try {

          String  sql = " SELECT  I17_KODAS_PS AS toode, I17_KIEKIS-I17_ATIDUOTA AS STOCK , I17_KODAS_IS AS LADU  " +
            "   from i17_vpro where I17_KODAS_IS in  ('01','02','03') and I17_KIEKIS > 0  ORDER BY I17_KODAS_PS" ;

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
    public TreeMap getAgents() {
        connectionUp(url2);
        String  cmd = "SELECT N15_KODAS_MS AS kood ,N15_PAV AS nimi,N15_TEVAS AS objekt  FROM  n15_men ORDER by N15_PAV";
        TreeMap agents = new TreeMap();

        try {
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("kood").trim());
              String name = rs.getString("nimi").trim();
              String ob = rs.getString("objekt");
            //  if(ob != null && ob.trim().length()>0){
              	
              /*	if(ob.trim().equals("SIAULIAI")){
              		ob = "03";
              	}else if(ob.trim().equals("KAUNAS")){
              		ob = "02";
              	}if(ob.trim().equals("VILNIUS")){
              		ob = "01";
              	}*/
            	  ob = "01"; 
                agent.setObject(ob.trim());
             // }
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
        connectionUp(url2);
        String cmd = "SELECT  trim(N08_KODAS_KS) AS kood1, (case when (N33_kodas_ak) is null then '1' else trim((N33_kodas_ak)) end) AS kood," +
        		" trim(N08_PAV) + ' ' + trim(N33_kodas_ak)AS nimetus, "+
				" N33_ADRESAS AS aadress, (case when N08_NUOL_GR ='JURA' then '0' else N08_NUOL_GR end)  AS ale , " +
				" ' ' AS visit ,N08_KODAS_KS AS payer , " +
				" (CASE WHEN LENGTH(TRIM(N33_SHIP_VIA))=0 or N33_SHIP_VIA IS NULL THEN  N08_KODAS_MS_P ELSE N33_SHIP_VIA END) AS agent , "  +
				" (case when N08_KODAS_TS_P in ('REAL','SEKAN','ISANKSTINIS') then '0' else N08_KODAS_TS_P end) as paymterm " +
				"  FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS  order by N08_PAV";
        log(cmd);
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        try {
            stmt = con.prepareStatement(cmd);
            //stmt.setInt(1, Integer.parseInt(agent.getID()));
            //stmt.setDate(2, getStartingDate());
            ResultSet	      rs = stmt.executeQuery();


            while (rs.next()) {
              String accountnum = rs.getString("kood1").trim();
              String acc = rs.getString("kood");
              if (acc != null && acc.trim().length()>0){
              	accountnum = accountnum + "-" + acc.trim();
              }
              String name = rs.getString("nimetus").trim() + accountnum;
              Customer cust = new Customer(accountnum);
               cust.setName(Converter.enCodeEst(name));
              String aadr = rs.getString("aadress");
              if(aadr != null && aadr.trim().length()>0){
                cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim()),"Lihtuania"));
              }

              String discStr = rs.getString("ale");
              if (discStr != null){
            	  try{
            		  cust.setDiscount(Double.parseDouble(discStr));  
            	  }catch(Exception e){
            		  
            	  }
               
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
              cust.addAgent(new Agent(ag));
              if(ag.equals("043")){
            	  cust.addAgent(new Agent("005"));
              }
              if(ag.equals("044")){
            	  cust.addAgent(new Agent("014"));
              }
              if(ag.equals("045")){
            	  cust.addAgent(new Agent("021"));
              }
              cust.setPaymTerm(rs.getInt("paymterm")+"");
             //System.out.println(cust.getID() +" " + cust.getAgents().toString());
              customers.put(name, cust);
              custMap.put(cust.getPayer(), cust);
            }

            String sql =
                "SELECT I44_KODAS_KS AS klient, sum(i44_suma_db-i44_suma_cr) AS summa  FROM i44_skol,n08_klij WHERE " +
               " i44_skol.i44_kodas_ks=n08_klij.n08_kodas_ks and " +
               " exists(select i44_kodas_ks,i44_dok_nr,sum(i44_suma_db-i44_suma_cr) as suma from i44_skol i44 where i44.i44_kodas_ks=i44_skol.i44_kodas_ks " +
               " and i44.i44_dok_nr=i44_skol.i44_dok_nr and i44_tipas<>11 and i44.i44_data_dok<=getdate()group by i44.i44_kodas_ks,i44.i44_dok_nr "+
                 " having sum(i44.i44_suma_db-i44.i44_suma_cr)<>0) group by I44_KODAS_KS order by I44_KODAS_KS ";

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

    public TreeMap getProducts(Agent agent){
        connectionUp(url2);
        
        
        String grc = "SELECT  N19_KODAS_GS AS kood ,N19_TEVAS  AS nimi   FROM  n19_pgr  where N19_LYGIS not in  (1,2) and N19_RUSIS = 1 ORDER BY N19_KODAS_GS";
  
        TreeMap groups = new TreeMap();
        

        try {
            stmt = con.prepareStatement(grc);
            ResultSet	      rs = stmt.executeQuery();
            while (rs.next()) {
              
              String kood = rs.getString("kood").trim();
              String nimi = rs.getString("nimi").trim();
              groups.put(kood, nimi);
            
            } 
            rs.close();
            stmt.close();
            
        }catch(Exception e) {
            	
            }
            
   String cmd = " SELECT N17_KODAS_PS AS toode,trim(N17_PAV) + ' ' + N17_PAVU AS nimetus,N27_KIEKIS_PAK  AS tykke, N32_KAINA1 AS myygihind," +
   		"N17_KOD_T AS ean, "+
   " 0 AS kogus, trim(N17_KODAS_KS)+trim(N17_KODAS_LS_1) AS grupp, N17_KODAS_GS AS grupp2 ,N17_MEN_PAV AS minkogus ,N17_KODAS_LS_4 AS status , N17_MAX_NUOL as max " +
   " FROM n17_prod a INNER JOIN N32_PABC ON N32_KODAS_PS=N17_KODAS_PS LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps " +
   "	 WHERE N32_TIPAS='A' "+
   "   AND N17_INTERNET = 1 AND N17_KODAS_PS NOT IN   (select L02_KODAS_FIL from L02_FILD where L02_KODAS_FS='01')";
   
        if (isDebug()) {
            System.out.println(cmd);
        }
        TreeMap products = new TreeMap();

        try {
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();
            while (rs.next()) {
              String kood = rs.getString("toode").trim();
              Product prod = new Product(kood);
              String nimetus = rs.getString("nimetus").trim();
              prod.setName(Converter.enCodeEst(nimetus));
              try{
              prod.setInCases(rs.getDouble("tykke"));
            } catch (Exception e){}
              prod.setPrice(rs.getDouble("myygihind"));
             // String price = rs.getString("iii");

              String ean = rs.getString("ean");
              if(ean != null && ean.trim().length()>0){
              	prod.setEAN(ean.trim());
              }
//              String price = ("3.000|15.000|0.000");
   /*           if(price.indexOf("|")!=-1 ){
                price = price.substring(price.indexOf("|")+1,price.trim().length());
                price = price.substring(0,price.indexOf("|"));
               // System.out.println("price" + price);
              prod.setPrice(Double.parseDouble(price));
}*/
              String laoseis = rs.getString("kogus");
              if (laoseis != null && laoseis.trim().length()>0){
                prod.setStock(Double.parseDouble(laoseis));
              }
              String grupp = rs.getString("grupp");
             if (grupp != null && grupp.trim().length()>0){
               prod.setGroup(grupp.trim());
             }
             String grupp2 = rs.getString("grupp2");
                    if (grupp2 != null && grupp2.trim().length()>0){
                    	grupp2 = grupp2.trim();
                    	String gr2 = grupp2;
                    	if(groups.get(grupp2) != null) {
                    		gr2 = (String)groups.get(grupp2);
                    		if(groups.get(gr2) != null) {
                        		gr2 = (String)groups.get(gr2);
                        		if(groups.get(gr2) != null) {
                            		gr2 = (String)groups.get(gr2);
                            	}
                        	}
                    	}
                    	
                      prod.setGroupSecond(gr2);
                    }
               String minam = rs.getString("minkogus");
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
               }
               
               
               String stat = rs.getString("status");
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
               double max = rs.getDouble("max");
               if(max > 0){
               	if(max < 1){
               		max = 2;
               	}
               	prod.setMaxDiscount(max);
               }
               prod.setVat("21");
               
               products.put(nimetus, prod);

                // }
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
        
        String cmd =" SELECT trim(I06_KODAS_KS)  AS klient,(case when (i06_kodas_ak) is null then '1' else trim((i06_kodas_ak)) end) as kood," +
        		"I07_KODAS AS toode,I07_KIEKIS AS kogus, " +
				"I06_DOK_DATA AS kuupaev FROM i07_pard p" +
				"  INNER JOIN i06_parh ON p.I07_KODAS_PO=I06_KODAS_PO "+
				" WHERE I06_DOK_DATA > ? "+
				" GROUP BY I06_KODAS_KS,(case when (i06_kodas_ak) is null then '1' else trim((i06_kodas_ak)) end),I07_KODAS,I06_DOK_DATA,I07_KIEKIS";
    
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

    public static final String DRIVER_VERSION = "KosvilaLIT";
    private String             url = "jdbc:odbc:kosvila";
    private String	       url2 = "jdbc:odbc:kosvila";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

