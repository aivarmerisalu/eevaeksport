/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
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
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class FereSaalDBDriver extends AbstractDBDriver implements  BulkUpdater ,Discounter,CrmData{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
	
    public FereSaalDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        confirmemail = parent.getProps().getProperty("confirmemail");
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

        try {
            Class.forName(databaseDriverName).newInstance();
            con3 = DriverManager.getConnection(url3);

        } catch (Exception e) {
            parent.showError(e);
        }
    }

   public void setCrmData(String agentID,String agentName,String custID,String custName,String tekst,java.sql.Date date) throws SQLException {
    
	
	   
    }
    
    public String getProductSortingData(){
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
           	  String cmd = "SELECT te.saaja  AS klient,pr.siffer AS toode FROM osmy/os_toode pr " +
           " INNER JOIN osmy/os_tell te ON te.nr=pr.recnr INNER JOIN osmy/lm_mat a ON a.siffer =pr.siffer  WHERE  " +
           " olek in (924) AND  a.uhik not in ('------')  " +
           " GROUP BY te.saaja,pr.siffer";
           	  
           	     System.out.println(cmd);

           		connectionUp();
           	 
          	       Statement stmt = con.createStatement();
          	        ResultSet rs = stmt.executeQuery(cmd);
          	        while(rs.next()){
                             String custID = " ";
                             String prodID = " ";
                             MatrixLine mline = new MatrixLine();

                             custID = rs.getString("klient");
                             prodID = rs.getString("toode");
                             
                             if (custID != null && custID.trim().length() > 0) {
                               mline.setCustID(custID.trim());
                             }

                             if (prodID != null && prodID.trim().length() > 0) {
                               mline.setProdID(prodID.trim());
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
        	public Vector getLastThreeData() {
        		return null;
        	}     

	public String getCustProdDiscounts(Agent agent) {
		return null;
	}

	public Vector getPriceMatrixData() {
		Vector lines = new Vector();
		getHinnad(lines);
		String sql = " SELECT kood AS custid,'' AS clgroup,'' AS prodid,grupp AS alegroup,alla AS discount,0 " +
                      " AS price FROM osmy/allahind  GROUP BY custid,grupp  ORDER BY custid,grupp";

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
      String sql = "SELECT d.saaja AS klient,aa.nr AS number ,aa.vkuup AS maksepaev , " + 
         " sum(aa.summa-aa.makstud) AS tasumata ,kuup AS kuupaev, vkuup-date() AS yle from osmy/my_arve aa " +
		 "  INNER JOIN osmy/my_tell d ON d.arvenr=aa.nr  where  " +
         " aa.summa-aa.makstud !=0 and aa.vkuup <= date()  "+
         " and aa.liik not like '%S%'  group by aa.nr,d.saaja";

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

           con = DriverManager.getConnection(url);
      } catch (Exception e) {
            getParent().showError(e);
      }

    }

    public TreeMap getProdGroups() throws SQLException {

      connectionUp(url2);

      TreeMap tm = new TreeMap();

            String cmd = " select left(trim(kood),1) AS kood ,lisa as nimi from osmy/klassif  " +
           " where grupp='GT' and lisa !='' AND lisa not like '%*%' ORDER BY lisa ";
       try {
        stmt = con.prepareStatement(cmd);
        ResultSet rs = stmt.executeQuery();
       setTreeGroup(true);
        while (rs.next()) {
          String liik = rs.getString("kood").trim();
          String nimetus = rs.getString("nimi").trim();
          if (liik != "") {
            tm.put(liik, Converter.enCodeEst(nimetus.replaceAll("'", "ä").replaceAll("", "Ä").replaceAll("å", "Õ").replaceAll("", "Ö")));
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

       String cmd = "SELECT  left(kood,2) AS kood,(left(trim(kood),1)+ ' ' + right(lisa,59)) AS nimi " +
            " FROM osmy/klassif WHERE grupp ='GT' and lisa like '%*%'  ORDER BY nimi ";
       try {
         stmt = con.prepareStatement(cmd);
         ResultSet rs = stmt.executeQuery();

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
    public TreeMap getAgents() {
        connectionUp(url2);
     //   getHinnad(new Vector());
        String  cmd = "SELECT kood AS yksus,trim(enimi)+' '+ trim(pnimi)" +
                       " as nimetus, '1' AS objekt FROM osmy/isik  ORDER BY enimi";
        TreeMap agents = new TreeMap();

        try {
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();
         
            while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              agent.setObject(rs.getString("objekt").trim());
              String name = rs.getString("nimetus").trim();
              agent.setName(name);
              agents.put(name, agent);
            }
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        return agents;
    }
    
    
private void  getHinnad(Vector lines){
		
		
		String sql = "select siffer as toode , i1+i2+i3  as asi from osmy/hinnad where tunnus = 'M' and siffer not like '%*%' order by siffer";
		TreeMap res = new TreeMap();
		TreeMap vahe = new TreeMap();
	        try{
	            stmt = con.prepareStatement(sql);
	            ResultSet	      rs = stmt.executeQuery();
	       
	            while (rs.next()) {
	            	  String kood = rs.getString("toode").trim();
	                  String nimetus = rs.getString("asi").trim();
	                if(vahe.get(kood) != null){
	                	String aa = vahe.get(kood) + "";
	                	vahe.put(kood, aa + nimetus);
	                }else{
	                	vahe.put(kood, nimetus);
	                }
	            }
	            
	            rs.close();
	            stmt.close();     	
	        }catch(Exception e){
	        	e.printStackTrace();
	        }
	   
     
	    Iterator itera =    vahe.keySet().iterator();
	    
	    while (itera.hasNext()){
	    	String toode = itera.next() + "";
	    	String nimetus = vahe.get(toode) + "";
	    	StringTokenizer st = new StringTokenizer(nimetus,"|");
	    //	System.out.println("tood " + toode + " " + nimetus);
	    	int cou = 1;
            while (st.hasMoreTokens()){
          	  double aa = Double.parseDouble(st.nextToken());
          	  MatrixLine mline = new MatrixLine();
          		mline.setCliGroup(cou+ "");
				mline.setProdID(toode);
				if (aa != 0) {
					mline.setPrice(aa);
					lines.add(mline);
				}
			  	cou++;
            }
        }
	    System.out.println("Lines size " + lines.size());
	}

    
    

    public TreeMap getCustomers(Agent agent){
        connectionUp(url2);
        String cmd = "SELECT  f.kood, left(f.kood,5)+ ' ' + f.nimi AS nimetus,(trim(aadr1)+' '+trim(aadr2))" +
           " AS aadress, '0' AS ale , kleht AS agent2 ,tt.firma AS payer ,f.agent AS agent,hgrupp as hinnak FROM osmy/firma f " +
        "LEFT JOIN osmy/my_tell tt ON f.kood=tt.saaja  " +
        "WHERE   (f.agent)!=0  and keeld like'%V%' GROUP BY kood ";
        log(cmd);
        TreeMap customers = new TreeMap();
        try {
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();


            while (rs.next()) {
              String name = rs.getString("nimetus").trim();
              String accountnum = rs.getString("kood").trim();
              Customer cust = new Customer(accountnum);
                     

              cust.setName(Converter.enCodeEst(name.replaceAll("“", "õ").replaceAll("ä", "õ").replaceAll("™", "Ö").replaceAll("„", "ä").replaceAll("å", "Õ").replaceFirst("ˇ", "Ä")));                                         

              cust.setAddress(Converter.adressLink(Converter.enCodeEst(rs.getString("aadress").trim().replaceAll("'", "õ").replaceAll("", "Ä").replaceAll("å", "Õ").replaceAll("", "Ö")),"Estonia"));
              //cust.setVisit();
              if(confirmemail != null && confirmemail.trim().length()>0){
              //cust.setConfirmEmail(confirmemail);
              }
              String payer =rs.getString("payer");
              if(payer != null){
                cust.setPayer(payer.trim());
                cust.setGroup(payer.trim());
              }
              String ag = rs.getString("agent2");
              if (ag != null && ag.trim().length()>0){
              //	cust.addAgent(new Agent(ag.trim()));
              }
              String discStr = rs.getString("ale");
              if (discStr != null){
                cust.setDiscount(Double.parseDouble(discStr));
              }
              
              String hinnak = rs.getString("hinnak".trim());
              if (hinnak != null && !hinnak.equals("0")){
                cust.setCustType(hinnak);
              }
              

              cust.addAgent(new Agent("99"));
              customers.put(name, cust);
            
            }
            con.close();
          }
          catch (Exception e) {
            getParent().showError(e);
          }
          return customers;
    }

    
    
    
    public TreeMap getProducts(Agent agent){
        connectionUp(url2);

        String cmd = " SELECT a.siffer AS toode,trim(a.siffer) + ' ' + iif(right(a.uhik,5)='1---T','+++','') + a.nimi AS nimetus," +
   		"  right(left(a.uhik,4),3) AS tykke, a.mghind AS myygihind, " +
   " (kak+tulek-minek) AS kogus, left (allgrupp,1) AS grupp,left(allgrupp,2) AS grupp2 , grupp AS alegrupp,a.kassak as ean " +
   " FROM osmy/lm_mat a INNER JOIN osmy/lm_kla k ON a.grupp = k.klkood LEFT JOIN ladu/ll010000 ll " +
   " ON ll.siffer=a.siffer WHERE (a.uhik not in ('------') OR (right(ltrim(str(tarnaeg)),2)=right(str(year(date())),2) AND " +
    " left(ltrim(str(tarnaeg)),2)>iif(len(ltrim(str(month(date()))))=1,'0'+ltrim(str(month(date()))),ltrim(str(month(date()))))))" +
    " ORDER BY a.siffer " ;

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
              prod.setName(Converter.enCodeEst(nimetus.replaceAll("“", "õ").replaceAll("ä", "õ").replaceAll("™", "Ö").replaceAll("„", "ä").replaceAll("å", "Õ").replaceFirst("ˇ", "Ä")));                                  
              try{
            	  String asi = rs.getString("tykke").replaceAll("-", " ").trim();
              prod.setInCases(Double.parseDouble(asi));
            } catch (Exception e){}
              prod.setPrice(rs.getDouble("myygihind"));              
              String laoseis = rs.getString("kogus");
              if (laoseis != null){
                prod.setStock(Double.parseDouble(laoseis));
              }
              String grupp = rs.getString("grupp");
             if (grupp != null && grupp .trim().length() != 0){
               prod.setGroup(grupp.trim());
             }
             String grupp2 = rs.getString("grupp2");
                    if (grupp2 != null && grupp2.trim().length() != 0){
                      prod.setGroupSecond(grupp2.trim());
                    }

                    String grupp3 = rs.getString("alegrupp");
                           if (grupp3 != null){
                             prod.setAlegrupp(grupp3.trim());
                           }
                           
                           String ean = rs.getString("ean");
                           if (ean != null && ean.trim().length() >0){
                             prod.setEAN(ean.trim());
                           }            
              products.put(nimetus, prod);

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
        String cmd =
            "SELECT te.saaja  AS klient,pr.siffer AS toode,pr.kogus AS kogus,te.regaeg AS kuupaev FROM osmy/my_toode pr " +
            "INNER JOIN osmy/my_tell te ON te.nr=pr.recnr INNER JOIN osmy/lm_mat a ON a.siffer =pr.siffer  WHERE " +
            "regaeg >? AND olek in (904,905) AND  a.uhik not in ('------') " +
            "GROUP BY te.saaja,pr.siffer ";
        String cmd2 =
            "SELECT te.saaja  AS klient,pr.siffer AS toode,pr.kogus AS puudu ,te.regaeg AS kuupaev FROM osmy/my_toode pr " +
            "INNER JOIN osmy/my_tell te ON te.nr=pr.recnr INNER JOIN osmy/lm_mat a ON a.siffer =pr.siffer   WHERE " +
            "regaeg >date()-300 AND recnr>5000000 AND  a.uhik not in ('------') " +
            "GROUP BY te.saaja,pr.siffer ";
        if (isDebug()) {
           System.out.println(cmd);
       }

       Vector custProds = new Vector();
       HashMap kliendid = new HashMap();
       HashMap ei = null;

       try {
          stmt = con.prepareStatement(cmd);
          stmt.setDate(1, getStartingDate());
           ResultSet	      rs = stmt.executeQuery();
          
            FileReader fr = new FileReader("eisaada.txt");
            if(fr != null){
            ei = new HashMap();
            BufferedReader reader = new BufferedReader(fr);
             String line = null;
              while((line = reader.readLine()) != null){
          	    StringTokenizer st = new StringTokenizer(line, ";");
                if (st.hasMoreTokens()){
            	 ei.put(st.nextToken(),st.nextToken());
                }
              }    	
            }
          
           
           
           while (rs.next()) {
             String kliendikood = rs.getString("klient").trim();
             String tootekood = rs.getString("toode").trim();
             double kogus = Double.parseDouble(rs.getString("kogus").trim());
             String kuup = rs.getString("kuupaev");
             Customer cust = (Customer) kliendid.get(kliendikood);

             if (cust == null) {
               cust = new Customer(kliendikood);
             }

             Product prod = new Product(tootekood);
             if(ei == null){
             
             cust.addProduct(prod,
                             new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                          kuup.substring(5,kuup.length()), 0, 0,0," "),false);
             kliendid.put(kliendikood, cust);
             custProds.add(new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                          kuup.substring(5,kuup.length()), 0, 0,0," "));
         	
             }else if (getEi(kliendikood,tootekood,ei)){
                cust.addProduct(prod,
                        new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                     kuup.substring(5,kuup.length()), 0, 0,0," "),false);
        kliendid.put(kliendikood, cust);
        custProds.add(new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                     kuup.substring(5,kuup.length()), 0, 0,0," "));
             }
           }
           rs.close();
           stmt = con.prepareStatement(cmd2);
           ResultSet rs2 = stmt.executeQuery();
           while (rs2.next()) {
             String toode = rs2.getString("toode").trim();
             String klient = rs2.getString("klient").trim();
             double openAmount = rs2.getDouble("puudu");
             String kuupaev = rs2.getString("kuupaev");
             Customer c = (Customer) kliendid.get(klient);
             if (c != null) {
               ArrayList al = (ArrayList) c.getProducts().get(toode);
               if (al != null) {
                 CustProd cp = (CustProd) al.get(1);
                 if (cp != null) {
                 if(cp.getLastAmountDate().compareTo(kuupaev)<0){
                     cp.setOpenAmount(openAmount);
                   custProds.add(cp);}
                 }
               }else {
                 CustProd cpnew = new CustProd(c.getID(), toode, 0, 0, 0,
                                          kuupaev.substring(5,kuupaev.length()), 0, 0,0," ");
                  cpnew.setOpenAmount(openAmount);
                 custProds.add(cpnew);
               }
             }
           }
       } catch (Exception e) {
           getParent().showError(e);
       }
       connectionDown();
       return custProds;
    }

    private boolean getEi(String klient,String toode,HashMap ei){
    	if(ei.containsKey(klient)){
    		String eiList = (String)ei.get(klient);
    		if(eiList.indexOf(toode)>0){
    			return false;
    		}
    	}
    	return true;
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
        boolean ok = true;
		if (ok) return;
       String cmd = "SELECT SUM(summa)  AS sales , te.osmy AS agent " +
                     "FROM osmy/my_tell te  WHERE " +
                     "AND month(regaeg)=month(date()) AND year(regaeg)=year(date())" +
                     " GROUP BY te.osmy ";

       log(cmd);

         try {
           stmt = con.prepareStatement(cmd);

             Calendar cal = Calendar.getInstance();
             cal.set(Calendar.DAY_OF_MONTH, 1);

             ResultSet	      rs = stmt.executeQuery(cmd);
             while(rs.next()) {
               String agentID = rs.getString("agent").trim();
               Agent agent = (Agent)agents.get(agentID);
               if (agent != null){
                 agent.setMonthSales((int)rs.getDouble("sales"));
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
    public static final String DRIVER_VERSION = "Fere";
    private String             url = "jdbc:odbc:kiire";
    private String	       url2 = "jdbc:odbc:baas";
    private String	       url3 = "jdbc:odbc:report";
    private Connection	       con = null;
    private Connection	       con3 = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
    private String             confirmemail = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

