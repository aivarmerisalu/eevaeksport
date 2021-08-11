/*
 * Created on 20.08.2006
 */
package eevaeksport.driver;

import java.sql.Connection;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034
 */


import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
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
public class SolManDBDriver extends AbstractDBDriver implements Discounter, Manager,Promo, BulkUpdater{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public SolManDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        // String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
        //String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
        try {
            Class.forName(databaseDriverName).newInstance();
         ddd = DriverManager.getConnection("jdbc:odbc:Solcraft Live Finanses", "SYSDBA", "");
        } catch (Exception e) {
            parent.showError(e);
        }
    }
    public String getSaveDebtInfo() {
       	return null;
       }
    public String getProductSortingData(){
       	return null;
       }
    public String getAssortmentStr(Agent agent){
      /*  StringBuffer sb = new StringBuffer();
         String cmd = "SELECT toode,tahtsus AS type  FROM products  WHERE tahtsus  = 1 or tahtsus = 3 or tahtsus = 2 ORDER BY tahtsus desc,nimi ";
        try {
          Connection con = getNewCon();
         	System.out.println(cmd);
    	        Statement stmt = con.createStatement();
    	        ResultSet rs = stmt.executeQuery(cmd);
    	        ResultSetMetaData md = rs.getMetaData();
    	        while(rs.next()){
            String id = rs.getString("toode");
    				sb.append(" ; ;A;" + id + "\n");
    			 }

    	        rs.close();
    	        con.close();
           } catch (Exception e){
          e.printStackTrace();
        }*/
        //return sb.toString();
    	return null;
      }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
    
        public String getSaveStockInfo(){
        return null;
        }


        public String getDealerCustomerCSV(String agent){
            return null;
            }
        public Vector getFixedAssortmentData(){
            return new Vector();
            }

        	 public Vector getPriceMatrixData() {
                Vector lines = new Vector();
                String sql = " SELECT ' ' AS custid, ale.nosaukums || '' || ale.papild_nosauk2 AS clgroup, " +
    				    " pr.artikuls   AS prodid, " +
    					" pr.artikuls  AS prodgroup, " +
    					" tt.atlaide AS discount, " +
    				    " tt.spec_cena AS price ,tips FROM atlaizhu_grupas ale  " +
    					" inner join atlaizhu_grupas_saturs abi on ale.id=abi.atlgrupas_id " +
    				    " inner join atlaides_saturs tt on tt.id=abi.id " +
    					" inner join preces  pr on pr.kods= tt.kods " +
    				    " where   tt.spec_cena > 0 AND no_datuma1<=current_timestamp and current_timestamp<=lidz_datumam1" +
    				    " order by  ale.nosaukums , ale.papild_nosauk2, pr.nosauk2 ";

                try {


              	Statement stmt = this.getNewCon().createStatement();
                        ResultSet rs = stmt.executeQuery(sql);

                        while (rs.next()) {
     //   System.out.println("wwww");
                                String custID = rs.getString("custid");
                                String clGroup = rs.getString("clgroup");
                                String prodID = rs.getString("prodid");
                                String prodGroup = rs.getString("prodgroup");
                                double price = rs.getDouble("price");
                                String tips = rs.getString("tips").trim();
                                MatrixLine mline = new MatrixLine();

                                if (custID != null && custID.trim().length() > 0) {
                                        mline.setCustID(custID.trim());
                                }
                                if (mline.getCustID() == null && clGroup != null
                                                && clGroup.trim().length() > 0) {
                                        mline.setCliGroup(clGroup.trim());
                                }

                                if (prodID != null && prodID.trim().length() > 0 && tips.equals("3")) {
                                        mline.setProdID(prodID.trim());
                                }
                                if (mline.getProdID() == null && prodGroup != null
                                                && prodGroup.trim().length() > 0 && tips.equals("2")) {
                                        mline.setProdGroup(prodGroup);
                                }
                               // mline.setDiscount(discount);

                                if (price != 0) {
                                        mline.setPrice(price);
                                }
                                lines.add(mline);
                        }

                } catch (SQLException ex) {
                ex.printStackTrace();
                }
                return lines;
            	//return null;
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
      return DriverManager.getConnection(url, "SYSDBA", "");
    }

    /**
     *
     */
    public void connectionDown(){
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
      Connection con = getNewCon();
    TreeMap   tm = new TreeMap();
    String    sql = "SELECT grupa as liik ,nosauk AS nimetus from grupas ORDER by nosauk  " ;


    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
        tm.put(rs.getString("liik").trim(),
               rs.getString("nimetus").trim());
    }
    rs.close();
    st.close();
    con.close();

    return tm;
    }


        public String getDistribution() {
          return null;
        }



    public TreeMap getAgents() {

		TreeMap agents = new TreeMap();

		String agentStr = getParent().getProps().getProperty("supervisors");

		if(agentStr != null){

		    StringTokenizer st = new StringTokenizer(agentStr, ",");
		       while(st.hasMoreTokens()){
		       	Agent agent = new Agent(st.nextToken());

				String agentAGENTS = getParent().getProps().getProperty(agent.getID());
         	    StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
			    agent.setName(stag.nextToken());
				agent.setManager(true);

			       while(stag.hasMoreTokens()){
                    agent.addDubbler(stag.nextToken());
			       }
					agents.put(agent.getName(), agent);
					agents1.put(agent.getID(), agent);
		       }
		}
		return agents;

    }





    public TreeMap getCustomers(Agent agent){

      String cmd = "SELECT a.nosauk AS nimi,a.pkonts AS kood,a.adrese || ' ' || a.pilseta AS aadress,a.atlaide AS discount," +
      		"a.uni AS visit,a.pers_amats " +
      		" || ' ' || a.talrunis AS info, " +
            " ag.num AS agent,'' AS payer , a.kons_dienas AS makse ,kategory AS grupp ,ag.nosauk AS agname FROM adreses a " +
			" LEFT JOIN kontr ag ON ag.piezimes=a.persona WHERE  (a.tips!='A' " +
            " and a.nosauk not starting '#'  and a.nosauk not starting '*' AND ag.piezimes != '') or a.pkonts = '0100000' ORDER BY a.nosauk";

    log(cmd);

        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        HashMap custMap2 = new HashMap();

        try {
          Connection con = getNewCon();

          Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);
            int counter = 0;
            while (rs.next()) {
              counter++;
                String nimi = rs.getString("nimi").trim();
                if(!nimi.endsWith("#")){

                      //   System.out.println("nimi1 " + nimi);
                     	nimi = nimi.replaceAll("Z/S","");
                     	nimi = nimi.replaceAll(" SIA","");
                     	nimi = nimi.replaceAll("AIZKRAUKLES","AIZ.");
                     	nimi = nimi.replaceAll("NAFTA ","NA. ");
                     	nimi = nimi.replaceAll("SAUL�TE","");
                     	nimi = nimi.replaceAll("''","\"");
                     	nimi = nimi.replaceAll("A/S","");
                     	nimi = nimi.replaceAll("VIDZEMES ","VID ");
                     	nimi = nimi.replaceAll(" DUS","");
                     	nimi = nimi.replaceAll("VESEKS Sabiedr�ba ar Ierobe�otu Atbild�bu","VESEKS SIA");
                     	nimi = nimi.replaceAll("TIRDZNIEC�BAS","TIRD.");
                     	nimi = nimi.replaceAll("SENTOR FARM APTIEKAS","SENTOR APT.");
                     	nimi = nimi.replaceAll("SAULES APTIEKU P�RVALDE","SAULES APT.");
                     	nimi = nimi.replaceAll("KOMERCIJA","KOM");
                      	nimi = nimi.replaceAll("LATVIA ","LAT ");
                      	nimi = nimi.replaceAll("R�GAS","R�.");
                      	nimi = nimi.replaceAll("R�GA","R�");
                      	nimi = nimi.replaceAll("R�ZEKNES","R�Z");
                      	nimi = nimi.replaceAll("PRESES APVIEN�BA","PRESES APVI.");
                      	nimi = nimi.replaceAll("CENTRS ","CEN ");
                      	nimi = nimi.replaceAll("PARTNERI","PAR");
                      	nimi = nimi.replaceAll(" BALTIJA","");
                      	nimi = nimi.replaceAll("SERVISS","SER");
                      	nimi = nimi.replaceAll("APTIEKA","APT");
                      	nimi = nimi.replaceAll("P�RTIKA","P�R");
                      	nimi = nimi.replaceAll("LATVIJAS ","LAT ");
                      	nimi = nimi.replaceAll("JELGAVAS","JEL.");
                      	nimi = nimi.replaceAll("PHARMA","PH");

                     	nimi = nimi.replaceAll("DOUBLE COFFEE","DouCof");
                     	nimi = nimi.replaceAll("AIZKRAUKLES SABIEDR�BA","AIZKRAUKLES SAB.");


                Customer cust = new Customer(rs.getString("kood").trim());

                String aadress = rs.getString("aadress");
                if(aadress != null && aadress.trim().length()>0){
                    cust.setAddress(aadress.trim());
                    nimi = nimi + " " +aadress.trim()+ " " + cust.getID();
                }

                cust.setName(nimi);
            //    cust.setCustType("A");
                String ale = rs.getString("discount");
                if (ale != null && ale.trim().length()>0){
                  cust.setDiscount(Double.parseDouble(ale.trim()));
                }

                String visit=rs.getString("visit");
                if (visit!=null && visit.trim().length()>0){
                  cust.setVisit(visit.trim());
                }
                String info = rs.getString("info");
                if(info != null && info.trim().length()>0){
                	cust.setPersonalData(info.trim());
                }

                String ag = rs.getString("agent");
                if(ag!=null){

                	cust.addAgent(new Agent(ag));
					Agent agg = ((Agent)this.getAgents1().get(ag.trim()));
					if(agg != null && agg.getDubbler().size()>0){
						for (int i = 0 ; i < agg.getDubbler().size() ; i++){
							cust.addAgent(new Agent((String)((Agent)this.getAgents1().get(ag.trim())).getDubbler().elementAt(i)));
						}
					}

                }

         String payer = rs.getString("payer");
         if (payer != null && payer.trim().length()>0){
            cust.setPayer(payer.trim());
         }

         String makset = rs.getString("makse");
         if (makset != null && makset.trim().length()>0){
            cust.setPaymTerm(makset.trim());
         }

         String grupp = rs.getString("grupp");
         if (grupp != null && grupp.trim().length()>0){
         	if(grupp.trim().equals("01")){
         		grupp = "A";
         	}
        	if(grupp.trim().equals("02")){
         		grupp = "B";
         	}
        	if(grupp.trim().equals("03") || grupp.trim().equals("04") ||
        			grupp.trim().equals("05")  || grupp.trim().equals("08")

					 || grupp.trim().equals("14")){
         		grupp = "C";
         	}
        	if(grupp.trim().equals("06")){
         		grupp = "D";

        	}

        	if(grupp.trim().equals("09") || grupp.trim().equals("10")){
         		grupp = "E";
         	}
            cust.setCustType(grupp.trim());
         }
         String agname = rs.getString("agname");
         if(agname != null && agname.trim().length()>0){
         	cust.setPersonalData(agname.trim()+ " " + cust.getPersonalData());

         }

                customers.put(cust.getName(), cust);
                custMap.put(cust.getPayer(), cust);
                custMap2.put(cust.getID(), cust);
                }




            }

            rs.close();
            stmt.close();
            con.close();
/*
            String sql3 = "Select RPACC AS klient,result_code AS staatus,rsaldo AS summa from m_sld_saldolist (cast('today' as date)) ";
            System.out.println(sql3);
               ResultSet rst = smt.executeQuery(sql3);
               while(rst.next()){
               	String custom = rst.getString("klient");
               	Customer cc = (Customer)custMap2.get(custom.trim());
               	if (cc != null){
               	int status = rst.getInt("staatus");
               	 if(status==2){
               	  cc.setSalesBlock(true);
               	 }
                   double debt = Double.parseDouble(rst.getString("summa").trim());
                   if(debt != 0){
                   	cc.setDebt(debt);

                   }
               	}

               }*/

        } catch (Exception e) {
            getParent().showError(e);
        }

        return customers;
    }

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "select pacc AS klient , fdoc as number,dat1 as kuupaev, fdoc as number, sv as summa" +
      		"  ,current_timestamp-dat1 AS yle " +
      " from dokument where sv is not null and acc1 starting '2310' AND  " +
      " current_timestamp + 5 > dat1 order by pacc, datums, fdoc";

      Statement stmt = ddd.createStatement();

      ResultSet rs = stmt.executeQuery(sql);

      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        debt.setDoknr(rs.getString("number").trim());

        debt.setDuedate(rs.getDate("kuupaev"));

        debt.setSum(Double.parseDouble(rs.getString("summa").trim()));
        debt.setDue(rs.getString("yle"));
        list.addElement(debt);
      }
       rs.close();
       stmt.close();
       return list;
    }

    public TreeMap getProducts(Agent agent){

        String cmd = "SELECT pr.artikuls AS toode,pr.nosauk,pr.nosauk2 AS nimi,pr.cena1 AS myygihind,g.nodoklis AS VAT,pr.mervien AS unit,pr.svars2 AS " +
        		" tykke,svars AS minimum,pr.mf_grupa AS uusgrupp,pr.grupa AS grupp " +
             " , pr.ecrkods AS ean ,'' AS sorting FROM preces  pr INNER JOIN grupas g ON g.grupa=pr.grupa " +
              " where (act='+' and pr.nosauk2 >'') OR  (g.grupa=50 and pr.nosauk2 >'') " +
  			" order by pr.nosauk2";

          if (isDebug()) {
              System.out.println(cmd);
          }

          TreeMap products = new TreeMap();
          try {
            Connection con = getNewCon();
            Statement st = con.createStatement();
              ResultSet	      rs = st.executeQuery(cmd);
              HashMap prodHash = new HashMap();
              while (rs.next()) {
                  String kood = rs.getString("toode").trim();
                  String abi = kood;
                  if(abi.indexOf(".")!= -1){
                  	abi = abi.substring(0,abi.indexOf("."));
                  }
                  if (abi.length()== 3){
                  	abi = "00" + abi;
                  }
                  if (abi.length()== 4){
                  	abi =  abi + "0";
                  }
                  String nimetus = rs.getString("nimi").trim() ;
                  Product prod = new Product(kood);

                  prod.setPrice(rs.getDouble("myygihind"));
                  String vat = rs.getString("VAT");
                  double vt = 21;
                  try{
                  	if(vat!=null && vat.trim().length()>0){
                      	vt = Double.parseDouble(vat.trim());
                      }
                  }catch (Exception e){
                  }

                  prod.setPrice(prod.getPrice()/(1+vt/100));
                  String unit = rs.getString("unit");
                  if(unit != null && unit.trim().length()>0){
                  	prod.setUnit(unit.trim());

                  }

                  prod.setInCases(rs.getInt("tykke"));
                  double minim = rs.getDouble("minimum");
                  if (minim > 0){
                      prod.setOptAmountInUnits(minim);
                  }
                  prod.setStock(0);
                  String gruppsec = rs.getString("uusgrupp");
                  if(gruppsec != null && gruppsec.trim().length()>0){
                  	 prod.setGroup(gruppsec.trim());

                  }
                  String grupp = rs.getString("grupp");
                  if(grupp != null && grupp.trim().length()>0){

                  	 prod.setGroup(grupp.trim());

                  	 if(prod.getGroup().equals("050")){
                  	 	nimetus = "x " + nimetus;
                  	 }
                      if(grupp.trim().length()< 4){
                      	grupp = grupp.trim() + "0";
                      }
                      prod.setSorting(grupp);
                  }

                  /*int xx = rs.getInt("xx");
                  if(xx == 2){
          //            nimetus = "NT " + nimetus;
                      prod.setMustBe("1");
                  }
                  if(xx == 1){
                  	prod.setStatusTop("1");
        //              nimetus = "TOP " + nimetus;
                  }
                  if(xx == 3){
                  	prod.setMustBe("1");
                  	prod.setStatusTop("1");
                  //	nimetus = "N+T " + nimetus;
                  }*/

                  //prod.setName(abi.substring(0,3) + " " + nimetus);
                  if(nimetus.startsWith("MF")){
                  	nimetus = " " + nimetus;
                  }
                  prod.setName(nimetus);

                  String ean = rs.getString("ean");
                  if(ean != null && ean.trim().length()>0){
                  	prod.setEAN(ean.trim());
                  }

                  products.put(nimetus, prod);
                  prodHash.put(prod.getID(), prod);
              }
              rs.close();

              String sql2 =   "SELECT artikuls AS toode,sum(atlrez) AS stock from nolik_avg('LVL', " +
                  " '01.01.1999', 0, '1', '0', '0', '', '', '') group by artikuls Order by artikuls" ;
              rs = st.executeQuery(sql2);
              while(rs.next()){
                String id = rs.getString("toode").trim();
                Product prod = (Product)prodHash.get(id);
                if (prod != null){
                  prod.setStock(rs.getDouble("stock"));
                }
              }
              rs.close();
              st.close();
              con.close();
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

        String sql1 = " select a.pkonts AS klient , pr.artikuls AS toode, daudzums AS kogus, datums AS kuupaev  " +
  		  " from dokumenti dok inner join dok_spec rid on rid.dok_kods=dok.kods  " +
  		  " INNER JOIN adreses a ON a.kods=dok.san_kods " +
  		  " INNER JOIN preces pr ON pr.kods=rid.preces_kods where " +
  		  " datums > current_timestamp-120 AND funk=2 " +
  		  " GROUP BY pr.artikuls,a.pkonts,daudzums,datums " +
  		  " ORDER BY a.pkonts,pr.artikuls,datums,daudzums " ;



          if (isDebug()) {
              System.out.println(sql1);

          }

          Vector custProds = new Vector();

          try {
            Connection con = getNewCon();
            PreparedStatement stmt = con.prepareStatement(sql1);

            //stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
            ResultSet	      rs = stmt.executeQuery();

            HashMap kliendid = new HashMap();
            while (rs.next()) {
              String kliendikood = rs.getString("klient").trim();
              String tootekood = rs.getString("toode").trim();
              double kogus = rs.getDouble("kogus");
              Customer cust = (Customer)kliendid.get(kliendikood);
              if (cust == null){
                cust = new Customer(kliendikood);
              }
             Product prod = new Product(tootekood);
                CustProd cp = new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                             rs.getString("kuupaev").substring(5,10), 0, 0, 0, " ");
                cust.addProduct(prod,cp,false);
                kliendid.put(kliendikood, cust);
                custProds.add(cp);
            }

            rs.close();
            stmt.close();
            con.close();
          } catch (Exception e) {
            getParent().showError(e);
          }
          connectionDown();
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
      //System.out.println("bulkSales");
      String cmd = "SELECT kontr AS agent, sum (summa7) AS sales  FROM  dokumenti dok " +
      " WHERE  extract(month from datums)= extract(month from current_timestamp) " +
	  " AND extract(year from datums)= extract(year from current_timestamp) " +
      " AND funk=2 and kontr !='' AND kontr is not null GROUP BY kontr " +
	  " UNION ALL SELECT kontr AS agent,- sum (summa7) AS sales  FROM  dokumenti dok " +
      " WHERE  extract(month from datums)= extract(month from current_timestamp) " +
	  "  AND extract(year from datums)= extract(year from current_timestamp) " +
      "  AND funk=7 and kontr !='' AND kontr is not null " +
	  "  GROUP BY kontr  ";
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
                agent.setMonthSales((int)rs.getDouble("sales"));
                //System.out.println(agent.toString() + " " + agent.getMonthSales());
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
      String cmd = "SELECT SUM(OR20024) AS sales ,OR20019 AS agent FROM OR200100 WHERE MONTH(OR20015)=" +
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
     // System.out.println("nr:" + nr + ", total:" + total);
      return total;*/
    }

/*	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
       String sql = "INSERT into supervisor values ("
         + "?, "   //   agent	C(50),
         + "?,"   //   agentname	C(200),
         + "?,"   //   clientid	C(50),
         + "?,"   //   clientname	C(200),
         + "?,"   //	prodid	C(50),
         + "?,"   //   prodname	C(200),
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
         + "?,"   // isaction	L,
         + "?,"   // ispartner	L,
         + "?,"   // isbrand	L,
         + "?,"   // isgroup	L
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
	      + "?)" ; //	resint3	L

         try {
         	System.out.println("jam");
             PreparedStatement ps = ccc.prepareStatement(sql);

             for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
               SuperVisorLineWrapper lw = new SuperVisorLineWrapper(iter.next());
               ps.setString(1, wr.getInfo().getAgentID());
               ps.setString(2, wr.getInfo().getAgentName());
               ps.setString(3, lw.getLine().getCustID());
               ps.setString(4, lw.getLine().getCustName());
               ps.setString(5, lw.getLine().getProdID());
               ps.setString(6, lw.getLine().getProdName());
               ps.setDouble(7, lw.getLine().getNormalPrice()*100);
               ps.setDouble(8, lw.getLine().getAmount()*100);
               ps.setDouble(9, 0);
               ps.setDouble(10, 0);
               ps.setInt(11, lw.getLine().getPercentageFromTotalCategory());
               ps.setInt(12, lw.getLine().getPercentageFromColdCategory());
               ps.setInt(13, lw.getLine().getPercentageFromPallCategory());
               ps.setInt(14, lw.getLine().getPercentageFromShelfCategory());
               ps.setInt(15, lw.getLine().getPercentageFromRestCategory());
               ps.setBoolean(16, lw.getLine().isAction());
               ps.setBoolean(17, lw.getLine().isPartner());
               ps.setBoolean(18, lw.getLine().isBrand());
               ps.setBoolean(19, lw.getLine().isGroup());
               ps.setDouble(20, lw.getLine().getAction1Eval()*100);
               ps.setDouble(21, lw.getLine().getAction2Eval()*100);
               ps.setDouble(22, lw.getLine().getActionTotalEval()*100);
               ps.setDouble(23, lw.getLine().getCustEval()*100);
               String cc = lw.getLine().getCustComment();
               if (cc == null){
               	cc = " ";
               }
               ps.setString(24, cc);

               String cd = lw.getLine().getComment();
               if (cd == null){
               	cd = " ";
               }
               ps.setString(25, cd);

               String ce = lw.getLine().getCustAgentId();
               if (ce == null){
               	ce = " ";
               }

               ps.setString(26, ce);

               String cf = lw.getLine().getCustAgentName();
               if (cf == null){
               	cf = " ";
               }
               ps.setString(27, cf);
               ps.setInt(28, lw.getLine().getDealNr());
               ps.setInt(29, lw.getLine().getIdealNr());

               String cr1 = lw.getLine().getResStr1();
               if (cr1 == null){
               	cr1 = " ";
               }
               ps.setString(30, cr1);


               String cr2 = lw.getLine().getResStr2();
               if (cr2 == null){
               	cr2 = " ";
               }
               ps.setString(31, cr2);


               String cr3 = lw.getLine().getResStr3();
               if (cr3 == null){
               	cr3 = " ";
               }
               ps.setString(32, cr3);
               ps.setInt(33, lw.getLine().getResInt1());
               ps.setInt(34, lw.getLine().getResInt2());
               ps.setInt(35, lw.getLine().getResInt3());
               ps.executeUpdate();
             }

             ps.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
       }
*/

    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,enddate AS kuupaev,docsum AS summa,linecount AS ridu,text AS tekst " +
                   " FROM dayreport WHERE enddate>(current_timestamp-30) ORDER BY clid,enddate desc ";
      Connection con = getNewCon();
      Connection con2 = getNewCon();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);


      String sql2 = "SELECT  c.pkonts AS kood, 0 AS aastamyyk,0 AS kuumyyk,c.ag_nos AS agendinimi, " +
			" pers_amats AS kontaktisik ,talrunis AS telefon,kons_dienas AS maksetin, atlaide AS ale FROM " +
			" adreses c WHERE agents != '' AND persona != '' AND tips!='A'  and nosauk not starting '#'  and nosauk not starting '*' ORDER BY nosauk";

      Statement st2 = con2.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs2 = st2.executeQuery(sql2);

      StringBuffer sb = new StringBuffer();
      String sep = ";";
      while(rs.next()){
        String cliID = rs.getString("klient").trim();
        sb.append(cliID + sep);
        sb.append(rs.getString("kuupaev").substring(0,10) + sep);
        sb.append(rs.getDouble("summa") + sep);
        sb.append(rs.getInt("ridu") + sep);
        String tekst = rs.getString("tekst");
        if (tekst != null && tekst.trim().length()>0){
        	tekst = tekst.trim();
        	tekst = tekst.replace('\'', ' ').replace('\"', ' ');
        } else {
        	tekst = " ";
        }
        sb.append(tekst + sep);

        boolean has = scrollTo(cliID, rs2);
        if (has){
         sb.append(ensureLen(rs2.getString("aastamyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("kuumyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
         sb.append(ensureLen(rs2.getString("kontaktisik"), " ").replace(';',',') + sep);
         sb.append(ensureLen(rs2.getString("telefon"), " ").replace(';',',') + sep);
         String makse = rs2.getString("maksetin");
         if(makse!= null && makse.trim().length()>0){
         	if(makse.trim().equals("HE")){
         		makse = "1";
         	}

         }
         sb.append(ensureLen(makse, "0") + sep);
         sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
        }

        sb.append("\n");
      }

      ManagerData md = new ManagerData();
      md.setClientVisitInfo(sb.toString());

      sql = "SELECT c.pkonts AS klient,c.agents AS agent,c.ag_nos AS agentname FROM adreses c " +
      " WHERE agents != '' AND persona != '' AND tips!='A'  and nosauk not starting '#'  and nosauk not starting '*' ORDER BY nosauk";

      PreparedStatement ps = con.prepareStatement(sql);
    //  ps.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
      rs = ps.executeQuery();
      StringBuffer bf2 = new StringBuffer();
      while(rs.next()){
        bf2.append(rs.getString("klient").trim() + sep);

      	String agent = rs.getString("agent").trim();
        bf2.append(agent + sep);
        String agentName = rs.getString("agentname");
        if (agentName == null) agentName = " ";
        bf2.append(agentName + sep);
        String email = parent.parseFirstEmail(parent.getEmail(agent));
        if(email == null || email.trim().length()== 0 )email = " ";
        bf2.append(email + "\n");
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
       public TreeMap getAgents1() {
		return agents1;
	}
	/**
	 * @param agents1 The agents1 to set.
	 */
	public void setAgents1(TreeMap agents1) {
		this.agents1 = agents1;
	}


    public static final String DRIVER_VERSION = "SolMan";
    private String url = "jdbc:odbc:Solcraft on server";
    private Connection ddd ;
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
	private TreeMap agents1 = new TreeMap();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

