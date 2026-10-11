/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class SolcraftLogSolDBDriver extends AbstractDBDriver implements Discounter, Manager,Promo, BulkUpdater{

 
    public SolcraftLogSolDBDriver(EksportPanel parent) {
        super(parent);
       String hhh =  parent.getProps().getProperty("subject");
        this.subjectFilesTo = hhh + " " +parent.getString("filemsg");
        // String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
        //String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
        if(parent.getProps().getProperty("grupp") != null){
    	  	firma = parent.getProps().getProperty("grupp") ;
    	  }
        if(parent.getProps().getProperty("grupp2") != null){
    	  	firma2 = parent.getProps().getProperty("grupp2") ;
    	  }
        if(parent.getProps().getProperty("grupp3") != null){
    	  	firma3 = parent.getProps().getProperty("grupp3") ;
    	  }
        if(parent.getProps().getProperty("grupp4") != null){
    	  	firma4 = parent.getProps().getProperty("grupp4") ;
    	  }
        if(parent.getProps().getProperty("grupp5") != null){
    	  	firma5 = parent.getProps().getProperty("grupp5") ;
    	  }
        if(parent.getProps().getProperty("grupp6") != null){
    	  	firma6 = parent.getProps().getProperty("grupp6") ;
    	  }
        if(parent.getProps().getProperty("grupp7") != null){
    	  	firma7 = parent.getProps().getProperty("grupp7") ;
    	  }
        
        try {
            Class.forName(databaseDriverName).newInstance();
         ddd = DriverManager.getConnection("jdbc:odbc:finance", "SYSDBA", "master");
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
           String sql2 = "SELECT pkonts AS custid, '' AS clgroup, " +
				   "  pr.artikuls   AS prodid, " +
				"	 pr.artikuls  AS prodgroup, " +
				"	 tt.atlaide AS discount, " +
				 "    tt.spec_cena AS price ,tt.tips FROM " +
					" atlaides_saturs tt " +
					" INNER JOIN klientu_atlaides aa on aa.id= tt.id" +
					" inner join preces  pr on pr.kods= tt.kods " +
					" INNEr JOIN adreses adr on adr.kods=aa.kl_kods" +
				    " where   tt.spec_cena > 0 AND no_datuma1<=current_timestamp and current_timestamp<=lidz_datumam1 " +
				    " order by pkonts,  pr.nosauk2";
            try {


          	Statement stmt = this.getNewCon().createStatement();
          	Statement stmt2 = this.getNewCon().createStatement();
                    ResultSet rs = stmt.executeQuery(sql);

                    while (rs.next()) {

                      
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
                    
                    stmt.close();
                    System.out.println("Teen Gruppidele Matixit " +lines.size());
                    
                    
                    ResultSet rs2 = stmt2.executeQuery(sql2);

                    while (rs2.next()) {
                            String custID = rs2.getString("custid");
                            String clGroup = rs2.getString("clgroup");
                            String prodID = rs2.getString("prodid");
                            String prodGroup = rs2.getString("prodgroup");
                            double price = rs2.getDouble("price");
                            String tips = rs2.getString("tips").trim();
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

                    System.out.println("Teen kLientidele Matixit " + lines.size());

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
    String    sql = "SELECT grupa as liik ,nosauk AS nimetus from grupas  where grupa starting '" + firma+ "' or grupa starting '" + firma2+ "'  or grupa starting '" + firma3+ "'  " +
    		"or grupa starting '" + firma4+ "'  or grupa starting '" + firma5+ "' or grupa starting '" + firma6+ "' or grupa starting '" + firma7+ "' ORDER by nosauk  " ;


    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
    	String aaaa = rs.getString("liik").trim();
    	String nimetus = rs.getString("nimetus");
    		 tm.put(aaaa,  nimetus.trim());
    	
       
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
      String  cmd = "select num AS kood , nosauk || ' ' ||num as nimi from kontr  " +
      	"	where nosauk != ''  ORDER BY num";
        TreeMap agents = new TreeMap();

        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("kood").trim());

              String nimi = rs.getString("nimi").trim();
              agent.setName(nimi);
              agent.setIsPilot(true);
              if(agent.getID().startsWith(firma)){
            	    agents.put(nimi, agent);  
              }
          
            }
            rs.close();
            stmt.close();
            con.close();

        } catch (Exception e) {
            getParent().showError(e);
        }
        

        Agent hh = new Agent("HOUSE");
        hh.setName("Office");
        agents.put("HOUSE", hh);
        return agents;
    }





    public TreeMap getCustomers(Agent agent){

    	
    	String kategory="";
    	 if(parent.getProps().getProperty("kategory") != null){
    		 kategory = "and kategory ='" + parent.getProps().getProperty("kategory") +"'";
    	 }
      String cmd = "SELECT a.nosauk AS nimi,a.pkonts AS kood,a.adrese || ' ' || a.pilseta AS aadress,a.atlaide AS discount,a.uni AS visit,a.pers_amats " +
      		" || ' ' || a.talrunis AS info, " +
            " agents AS agent,AGENTS1 , AGENTS2, TELEX as AGENTS3,'' AS payer , a.kons_dienas AS makse ,kategory AS grupp ,papild_nosauk || '' || papild_nosauk2 AS klale  FROM adreses a " +
			"  WHERE  (a.tips!='A' " +
            " and a.nosauk not starting '#'  and a.nosauk not starting '*'  " + kategory +  ")  or a.pkonts = '0110000000' ORDER BY a.nosauk"; //uus klient ((or a.pkonts = '0100000'))

        log(cmd);
        
        double minordsum = 0;
        if(getParent().getProps().getProperty("minordsum") != null){
        	try{
        		minordsum = new Double(getParent().getProps().getProperty("minordsum")).doubleValue();
        	}catch (Exception e){
        		
        	}
        }
        
        
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        
        HashMap custMap2 = new HashMap();

        try {
          Connection con = getNewCon();

          Statement stmt = con.createStatement();
          Statement smt = ddd.createStatement();
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
               // System.out.println("nimi2 " + nimi);
                cust.setName(nimi);

                String ale = rs.getString("discount");
                if (ale != null && ale.trim().length()>0){
                  cust.setDiscount(Double.parseDouble(ale.trim()));
                }

                String visit=rs.getString("visit");
                if (visit!=null && visit.trim().length()>0){
                  cust.setVisit(visit.trim().toUpperCase());
                }
                
                if(minordsum > 0){
                	cust.setMinsum(minordsum);
                }

                String info = rs.getString("info");
                if(info != null && info.trim().length()>0){
                	cust.setPersonalData(info.trim());
                }

                String agentc = rs.getString("agent");
                if(agentc != null && agentc.trim().length()>0){
                    cust.addAgent(new Agent(agentc.trim()));
                    cust.addAgent(new Agent("HOUSE")); 
                }
                
                

                String agente = rs.getString("agents1");
                if(agente != null && agente.trim().length()>0){
                    cust.addAgent(new Agent(agente.trim()));
                    cust.addAgent(new Agent("HOUSE")); 
                }
                

                String agentr = rs.getString("agents2");
                if(agentr != null && agentr.trim().length()>0){
                    cust.addAgent(new Agent(agentr.trim()));
                    cust.addAgent(new Agent("HOUSE")); 
                }
                
                String agentt = rs.getString("agents3");
                if(agentt != null && agentt.trim().length()>0){
                    cust.addAgent(new Agent(agentt.trim()));
                    cust.addAgent(new Agent("HOUSE")); 
                }
                
                
                if (cust.getID().equals("0110000000")) {
                    Enumeration enums = getParent().getAgentsHash().elements();
                      while (enums.hasMoreElements()) {
                        Agent a = (Agent) enums.nextElement();
                        cust.addAgent(a);
                        }
                      }

         String payer = rs.getString("payer");
         if (payer != null && payer.trim().length()>0){
            cust.setPayer(payer.trim());
         }

         String makset = rs.getString("makse");
         if (makset != null && makset.trim().length()>0){
         	if(makset.trim().equals("0")){
         		makset = "-1";
         	}
            cust.setPaymTerm(makset.trim());
         }


         String grupp = rs.getString("grupp");
         if (grupp != null && grupp.trim().length()>0){
        
            cust.setCustType(grupp.trim());
         }

         String alegr = rs.getString("klale");
         if(alegr != null && alegr.trim().length()>0){
         	cust.setGroup(alegr.trim());

         }
         /*
         String list2 = rs.getString("list2");
         if(list2 != null && list2.trim().length()>0){
         	cust.setSupervisor2Filled(true);
           sb.append(cust.getID() + "\n");
         }
         */

              customers.put(cust.getName(), cust);
              custMap.put(cust.getPayer(), cust);
              custMap2.put(cust.getID(), cust);

             }
            }

            rs.close();
            stmt.close();
            con.close();

         /*   String sql11 = "select custid from supervisor where year(sentdate)=year(date()) and  month(sentdate)=month(date()) group by custid";
            System.out.println(sql11);
            Connection ddd = DriverManager.getConnection(url2, "SYSDBA", "");
            Statement str = ddd.createStatement();
            ResultSet rst1 = str.executeQuery(sql11);
            while(rst1.next()){
            	String custom = rst1.getString("custid");
               	Customer cc = (Customer)custMap2.get(custom.trim());
               	if (cc != null){
               	  cc.setSupervisorFilled(true);
            }
            }
*/
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

               }

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
      " current_timestamp  > dat1  and current_timestamp -180 > dat1 order by pacc, datums, fdoc";

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
    	

      String cmd = " SELECT pr.artikuls AS toode,pr.nosauk2,pr.nosauk AS nimi,pr.cena5 AS myygihind,g.nodoklis AS VAT,pr.mervien AS unit,pr.gab_kaste AS  " +
      		" tykke , 0 AS minimum,pr.mf_grupa AS uusgrupp,pr.grupa AS grupp " +
            " , pr.ecrkods AS ean ,'' AS sorting FROM preces  pr INNER JOIN grupas g ON g.grupa=pr.grupa " +
            " where act='+' and pr.nosauk2 >'' and (pr.artikuls  starting '" + firma3 + "' or pr.artikuls  starting '" + firma4 + "' " +
            		"or pr.artikuls  starting '" + firma5 + "' or pr.artikuls  starting '" + firma + "' or pr.artikuls " +
            		"  starting '" + firma6 + "' or pr.artikuls  starting '" + firma7 + "' or pr.artikuls " +
            		" starting '" + firma2 + "') " +
			" order by pr.nosauk2 ";

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
                String nimetus = "    " + kood + " " + rs.getString("nimi").trim().replaceAll("\n", " ") ;
                Product prod = new Product(kood);

                prod.setPrice(rs.getDouble("myygihind"));
             

                prod.setPrice(prod.getPrice());
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
                     prod.setSorting(grupp);
                }

     
                prod.setName(nimetus);
                
                String ean = rs.getString("ean");
                if(ean != null && ean.trim().length()>0){
                	
                	ean = ean.trim();
                	prod.setEAN(ean);
                	int len = ean.length();
                	if(len > 3){
                      	 prod.setName(Converter.enCodeEst(nimetus) + " " + ean.substring(len-4));
                	}
                }
                
                
            
            		 products.put(prod.getName(), prod);
                     prodHash.put(prod.getID(), prod);
               
            }
            rs.close();

            String filter = "";
        	  String ladu = "010111";  
        	  String grupp = "0";
      	  
      	  if(parent.getProps().getProperty("filter") != null){
      	  	filter = "where artikuls like '%" + parent.getProps().getProperty("filter") + "'";
      	  }
      	  
      	  if(parent.getProps().getProperty("warehouse") != null){
      	  	ladu = parent.getProps().getProperty("warehouse") ;
      	  }
      	  
      	  if(parent.getProps().getProperty("grupp") != null){
      	  	grupp = parent.getProps().getProperty("grupp") ;
      	  }
      	  
      	  
          	  String  sql2 = " SELECT artikuls AS toode,  sum(atlrez) AS stock from STOCK_WITH_ORDERS('LVL', " +
                   " '01.01.1999', 0, '" + ladu  +"', '" + grupp + "', '0', '', '', '') " + filter + " group by artikuls Order by artikuls" ;
            
          	  rs = st.executeQuery(sql2);
            while(rs.next()){
              String id = rs.getString("toode").trim();
              Product prod = (Product)prodHash.get(id);
              if (prod != null){
            	  double stock = rs.getDouble("stock");
            	  if(stock >0)
                prod.setStock(stock);
              }
            }
            rs.close();
            
      	  if(parent.getProps().getProperty("warehouse2") != null){
        	  	ladu = parent.getProps().getProperty("warehouse2") ;
        	  }
        	  
        	  if(parent.getProps().getProperty("grupp2") != null){
        	  	grupp = parent.getProps().getProperty("grupp2") ;
        	  }
        	  
        	  
            	  sql2 = " SELECT artikuls AS toode,  sum(atlrez) AS stock from STOCK_WITH_ORDERS('LVL', " +
                     " '01.01.1999', 0, '" + ladu  +"', '" + grupp + "', '0', '', '', '') " + filter + " group by artikuls Order by artikuls" ;
              
            	  rs = st.executeQuery(sql2);
              while(rs.next()){
                String id = rs.getString("toode").trim();
                Product prod = (Product)prodHash.get(id);
                if (prod != null){
              	  double stock = rs.getDouble("stock");
              	  if(stock >0)
                  prod.setStock(stock);
                }
              }
              rs.close();
            
        	  if(parent.getProps().getProperty("warehouse3") != null){
          	  	ladu = parent.getProps().getProperty("warehouse3") ;
          	  }
          	  
          	  if(parent.getProps().getProperty("grupp3") != null){
          	  	grupp = parent.getProps().getProperty("grupp3") ;
          	  sql2 = " SELECT artikuls AS toode,  sum(atlrez) AS stock from STOCK_WITH_ORDERS('LVL', " +
              " '01.01.1999', 0, '" + ladu  +"', '" + grupp + "', '0', '', '', '') " + filter + " group by artikuls Order by artikuls" ;
       
     	  rs = st.executeQuery(sql2);
       while(rs.next()){
         String id = rs.getString("toode").trim();
         Product prod = (Product)prodHash.get(id);
         if (prod != null){
       	  double stock = rs.getDouble("stock");
       	  if(stock >0)
           prod.setStock(stock);
         }
       }
       rs.close();
          	  	
       
       
     
          	  }
          	  
          	  if(parent.getProps().getProperty("warehouse4") != null){
           	  	ladu = parent.getProps().getProperty("warehouse4") ;
           	  }
        	  if(parent.getProps().getProperty("grupp4") != null){
            	  	grupp = parent.getProps().getProperty("grupp4") ;
            	  sql2 = " SELECT artikuls AS toode,  sum(atlrez) AS stock from STOCK_WITH_ORDERS('LVL', " +
                " '01.01.1999', 0, '" + ladu  +"', '" + grupp + "', '0', '', '', '') " + filter + " group by artikuls Order by artikuls" ;
         
       	  rs = st.executeQuery(sql2);
         while(rs.next()){
           String id = rs.getString("toode").trim();
           Product prod = (Product)prodHash.get(id);
           if (prod != null){
         	  double stock = rs.getDouble("stock");
         	  if(stock >0)
             prod.setStock(stock);
           }
         }
         rs.close();
         
         
    	  if(parent.getProps().getProperty("warehouse5") != null){
         	  	ladu = parent.getProps().getProperty("warehouse5") ;
         	  }
      	  if(parent.getProps().getProperty("grupp5") != null){
          	  	grupp = parent.getProps().getProperty("grupp5") ;
          	  sql2 = " SELECT artikuls AS toode,  sum(atlrez) AS stock from STOCK_WITH_ORDERS('LVL', " +
              " '01.01.1999', 0, '" + ladu  +"', '" + grupp + "', '0', '', '', '') " + filter + " group by artikuls Order by artikuls" ;
       
     	  rs = st.executeQuery(sql2);
       while(rs.next()){
         String id = rs.getString("toode").trim();
         Product prod = (Product)prodHash.get(id);
         if (prod != null){
       	  double stock = rs.getDouble("stock");
       	  if(stock >0)
           prod.setStock(stock);
         }
       }
       rs.close();   
      	}
      	  
      	  
      	  
      	  if(parent.getProps().getProperty("warehouse6") != null){
       	  	ladu = parent.getProps().getProperty("warehouse6") ;
       	  }
    	  if(parent.getProps().getProperty("grupp6") != null){
        	  	grupp = parent.getProps().getProperty("grupp6") ;
        	  sql2 = " SELECT artikuls AS toode,  sum(atlrez) AS stock from STOCK_WITH_ORDERS('LVL', " +
            " '01.01.1999', 0, '" + ladu  +"', '" + grupp + "', '0', '', '', '') " + filter + " group by artikuls Order by artikuls" ;
     
   	  rs = st.executeQuery(sql2);
     while(rs.next()){
       String id = rs.getString("toode").trim();
       Product prod = (Product)prodHash.get(id);
       if (prod != null){
     	  double stock = rs.getDouble("stock");
     	  if(stock >0)
         prod.setStock(stock);
       }
     }
     rs.close();   
    	}
    	  
    	  
    	  
    	  
    	  if(parent.getProps().getProperty("warehouse7") != null){
       	  	ladu = parent.getProps().getProperty("warehouse7") ;
       	  }
    	  if(parent.getProps().getProperty("grupp7") != null){
        	  	grupp = parent.getProps().getProperty("grupp7") ;
        	  sql2 = " SELECT artikuls AS toode,  sum(atlrez) AS stock from STOCK_WITH_ORDERS('LVL', " +
            " '01.01.1999', 0, '" + ladu  +"', '" + grupp + "', '0', '', '', '') " + filter + " group by artikuls Order by artikuls" ;
     
   	  rs = st.executeQuery(sql2);
     while(rs.next()){
       String id = rs.getString("toode").trim();
       Product prod = (Product)prodHash.get(id);
       if (prod != null){
     	  double stock = rs.getDouble("stock");
     	  if(stock >0)
         prod.setStock(stock);
       }
     }
     rs.close();   
    	}
      	  
      	  
      	  
      	  
      	  
            	  }   
            
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
           	if(!tootekood.startsWith("PR")){
           	    cust.addProduct(prod,cp,false);
       	}
          
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


      String sql2 =  "SELECT a.nosauk  || ' ' || a.adrese || ' ' || a.pilseta AS klient,numurs AS arve, d.created AS kuupaev," +
      		" summa9 AS summa , d.kontr " +
          " FROM dokumenti d INNER JOIN adreses a ON d.san_kods=a.kods WHERE  kontr = '" + agent.getID()+ "' AND " +
            " d.created > current_timestamp-1 AND d.tips='-'  ORDER BY d.created desc ";

      String sql3 =  "SELECT a.nosauk  || ' ' || a.adrese || ' ' || a.pilseta AS klient,numurs AS arve, d.created AS kuupaev," +
		" summa9 AS summa , d.kontr " +
    " FROM dokumenti d INNER JOIN adreses a ON d.nos_kods=a.kods WHERE  kontr = '" + agent.getID()+ "' AND " +
      " d.created > current_timestamp-1 AND d.tips='+'  ORDER BY d.created desc ";

      System.out.println("siin");
        StringBuffer sb = new StringBuffer();
        String header = " Sanemtie pasutijumi :\n\n";
        String header2 = "  Atgriezt�s pre�u pavadz�mes :\n\n";
        boolean first = true;
        boolean second = true;
          try {
            Connection con2 = getNewCon();
                   Statement st = con2.createStatement();
                  ResultSet rs2 = st.executeQuery(sql2);
                      while (rs2.next()) {
                      	if(first){
                      		sb.append(header);
                      		first = false;
                      	}

                      sb.append(rs2.getString("klient")  +
                      		"\n    (" + rs2.getString("arve").trim() + ")[" + rs2.getString("kuupaev") + "] " +
                      		getParent().getNF().format(rs2.getDouble("summa"))
                      	+" LVL "	+  "\n\n");
                      }


                     ResultSet rs3 = st.executeQuery(sql3);
                         while (rs3.next()) {
                         	if(second){
                         		sb.append(header2);
                         		second = false;
                         	}

                         sb.append(rs3.getString("klient")  +
                         		"\n    (" + rs3.getString("arve").trim() + ")[" + rs3.getString("kuupaev") + "] " +
                         		getParent().getNF().format(rs3.getDouble("summa"))
                         	+" LVL "	+  "\n\n");
                         }


                      } catch (Exception e) {
                          getParent().showError(e);
                      }
                      System.out.println(agent.getID() + sb.toString());
         if(sb.toString().trim().length()>0){

          return sb.toString();
         }else {
         	return null;
         }

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
      String cmd = "SELECT kontr AS agent, sum (summa7) AS sales  FROM  " +
"dokumenti dok WHERE  extract(month from datums)= extract(month from current_timestamp) AND " +
"extract(year from datums)= extract(year from current_timestamp) AND funk=2 and kontr !='' " +
"and apst='+' and pasut='-' and rezerv='-' AND " +
"kontr is not null GROUP BY kontr " +
"UNION ALL SELECT kontr AS agent,- sum (summa7) AS sales " +
" FROM  dokumenti dok WHERE  extract(month from datums)= extract(month from current_timestamp)" +
"  AND extract(year from datums)= extract(year from current_timestamp) AND funk=7 and kontr !='' and apst='+' and pasut='-' and rezerv='-' AND " +
"kontr is not null GROUP BY kontr ";
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



    public String getSaveStockInfo(){
  	  String filter = "";
  	  String ladu = "010111";  
  	  String grupp = "0";
	  
	  if(parent.getProps().getProperty("filter") != null){
	  	filter = "where artikuls like '%" + parent.getProps().getProperty("filter") + "'";
	  }
	  
	  if(parent.getProps().getProperty("warehouse") != null){
	  	ladu = parent.getProps().getProperty("warehouse") ;
	  }
	  
	  if(parent.getProps().getProperty("grupp") != null){
	  	grupp = parent.getProps().getProperty("grupp") ;
	  }
	  
	  
    	  String  cmd = " SELECT artikuls AS toode, '00' as LADU, sum(atlrez) AS stock from STOCK_WITH_ORDERS ('LVL', " +
             " '01.01.1999', 0, '" + ladu  +"', '" + grupp + "', '0', '', '', '') " + filter + " group by artikuls Order by artikuls" ;
    	 
    	  
	       if (isDebug()) System.out.println(cmd);
	       log(cmd);
	       StringBuffer products = new StringBuffer();
	       try {

	          Connection con = getNewCon();
	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmd);

	         while (rs.next()) {

	           String kood = rs.getString("toode").trim();
	          String dealer = rs.getString("LADU").trim();
	           double seis = rs.getDouble("stock");
	           if(seis > 0){
	               products.append(kood +  ";" + getParent().getNF().format(seis) + ";"  + dealer  +  "\n"); // + dealer +  "\n");
	             }
	           }
	           rs.close();
	           
	           
	           if(parent.getProps().getProperty("warehouse2") != null && parent.getProps().getProperty("grupp2") != null){
	        	
	           if(parent.getProps().getProperty("warehouse2") != null){
	       	  	ladu = parent.getProps().getProperty("warehouse2") ;
	       	  }
	       	  
	       	  if(parent.getProps().getProperty("grupp2") != null){
	       	  	grupp = parent.getProps().getProperty("grupp2") ;
	       	  }
	       	  
	           
	           String  cmd2 = " SELECT artikuls AS toode, '00' as LADU, sum(atlrez) AS stock from STOCK_WITH_ORDERS ('LVL', " +
	           " '01.01.1999', 0, '" + ladu  +"', '" + grupp + "', '0', '', '', '') " + filter + " group by artikuls Order by artikuls" ;

	           
		        rs = st.executeQuery(cmd2);

		         while (rs.next()) {

		           String kood = rs.getString("toode").trim();
		          String dealer = rs.getString("LADU").trim();
		           double seis = rs.getDouble("stock");
		           if(seis > 0){
		               products.append(kood +  ";" + getParent().getNF().format(seis) + ";"  + dealer  +  "\n"); // + dealer +  "\n");
		             }
		           }
		           rs.close();
	           
	           
		           
	           }
	           if(parent.getProps().getProperty("warehouse3") != null && parent.getProps().getProperty("grupp3") != null){
		        	
		           if(parent.getProps().getProperty("warehouse3") != null){
		       	  	ladu = parent.getProps().getProperty("warehouse3") ;
		       	  }
		       	  
		       	  if(parent.getProps().getProperty("grupp3") != null){
		       	  	grupp = parent.getProps().getProperty("grupp3") ;
		       	  }
		       	  
		           
		           String  cmd2 = " SELECT artikuls AS toode, '00' as LADU, sum(atlrez) AS stock from STOCK_WITH_ORDERS ('LVL', " +
		           " '01.01.1999', 0, '" + ladu  +"', '" + grupp + "', '0', '', '', '') " + filter + " group by artikuls Order by artikuls" ;

		           
			        rs = st.executeQuery(cmd2);

			         while (rs.next()) {

			           String kood = rs.getString("toode").trim();
			          String dealer = rs.getString("LADU").trim();
			           double seis = rs.getDouble("stock");
			           if(seis > 0){
			               products.append(kood +  ";" + getParent().getNF().format(seis) + ";"  + dealer  +  "\n"); // + dealer +  "\n");
			             }
			           }
			           rs.close();
		           
		           
			           
		           }
	           
	           if(parent.getProps().getProperty("warehouse4") != null && parent.getProps().getProperty("grupp4") != null){
		        	
		           if(parent.getProps().getProperty("warehouse4") != null){
		       	  	ladu = parent.getProps().getProperty("warehouse4") ;
		       	  }
		       	  
		       	  if(parent.getProps().getProperty("grupp4") != null){
		       	  	grupp = parent.getProps().getProperty("grupp4") ;
		       	  }
		       	  
		           
		           String  cmd2 = " SELECT artikuls AS toode, '00' as LADU, sum(atlrez) AS stock from STOCK_WITH_ORDERS ('LVL', " +
		           " '01.01.1999', 0, '" + ladu  +"', '" + grupp + "', '0', '', '', '') " + filter + " group by artikuls Order by artikuls" ;

		           
			        rs = st.executeQuery(cmd2);

			         while (rs.next()) {

			           String kood = rs.getString("toode").trim();
			          String dealer = rs.getString("LADU").trim();
			           double seis = rs.getDouble("stock");
			           if(seis > 0){
			               products.append(kood +  ";" + getParent().getNF().format(seis) + ";"  + dealer  +  "\n"); // + dealer +  "\n");
			             }
			           }
			           rs.close();
		           }
	           
	           
	           
	           
	           if(parent.getProps().getProperty("warehouse5") != null && parent.getProps().getProperty("grupp5") != null){
		        	
		           if(parent.getProps().getProperty("warehouse5") != null){
		       	  	ladu = parent.getProps().getProperty("warehouse5") ;
		       	  }
		       	  
		       	  if(parent.getProps().getProperty("grupp5") != null){
		       	  	grupp = parent.getProps().getProperty("grupp5") ;
		       	  }
		       	  
		           
		           String  cmd2 = " SELECT artikuls AS toode, '00' as LADU, sum(atlrez) AS stock from STOCK_WITH_ORDERS ('LVL', " +
		           " '01.01.1999', 0, '" + ladu  +"', '" + grupp + "', '0', '', '', '') " + filter + " group by artikuls Order by artikuls" ;

		           
			        rs = st.executeQuery(cmd2);

			         while (rs.next()) {

			           String kood = rs.getString("toode").trim();
			          String dealer = rs.getString("LADU").trim();
			           double seis = rs.getDouble("stock");
			           if(seis > 0){
			               products.append(kood +  ";" + getParent().getNF().format(seis) + ";"  + dealer  +  "\n"); // + dealer +  "\n");
			             }
			           }
			           rs.close();
		           
		           
			           
		           }
	           
	           
	           
	           if(parent.getProps().getProperty("warehouse6") != null && parent.getProps().getProperty("grupp6") != null){
		        	
		       	  	ladu = parent.getProps().getProperty("warehouse6") ;
		       	  	grupp = parent.getProps().getProperty("grupp6") ;
		       	  
		           
		           String  cmd2 = " SELECT artikuls AS toode, '00' as LADU, sum(atlrez) AS stock from STOCK_WITH_ORDERS ('LVL', " +
		           " '01.01.1999', 0, '" + ladu  +"', '" + grupp + "', '0', '', '', '') " + filter + " group by artikuls Order by artikuls" ;

		           
			        rs = st.executeQuery(cmd2);

			         while (rs.next()) {

			           String kood = rs.getString("toode").trim();
			          String dealer = rs.getString("LADU").trim();
			           double seis = rs.getDouble("stock");
			           if(seis > 0){
			               products.append(kood +  ";" + getParent().getNF().format(seis) + ";"  + dealer  +  "\n"); // + dealer +  "\n");
			             }
			           }
			           rs.close();
		           
		           
			           
		           }
	           
	           
	           if(parent.getProps().getProperty("warehouse7") != null && parent.getProps().getProperty("grupp7") != null){
		        	
		       	  	ladu = parent.getProps().getProperty("warehouse7") ;
		       	  	grupp = parent.getProps().getProperty("grupp7") ;
		       	  
		           
		           String  cmd2 = " SELECT artikuls AS toode, '00' as LADU, sum(atlrez) AS stock from STOCK_WITH_ORDERS ('LVL', " +
		           " '01.01.1999', 0, '" + ladu  +"', '" + grupp + "', '0', '', '', '') " + filter + " group by artikuls Order by artikuls" ;

		           
			        rs = st.executeQuery(cmd2);

			         while (rs.next()) {

			           String kood = rs.getString("toode").trim();
			          String dealer = rs.getString("LADU").trim();
			           double seis = rs.getDouble("stock");
			           if(seis > 0){
			               products.append(kood +  ";" + getParent().getNF().format(seis) + ";"  + dealer  +  "\n"); // + dealer +  "\n");
			             }
			           }
			           rs.close();
		           
		           
			           
		           }
	           
	           
	       } catch (Exception e) {
	           getParent().showError(e);
	       }
	       return products.toString();
	   }





	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){}


    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,enddate AS kuupaev,docsum AS summa,linecount AS ridu,text AS tekst " +
                   " FROM dayreport WHERE enddate>(getdate()-30) ORDER BY clid,enddate desc ";
      Connection con = getNewCon();
      Connection con2 = getNewCon();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);


      String sql2 = "SELECT  c.kood, aasta AS aastamyyk,kuu AS kuumyyk,a.nimi AS agendinimi, " +
			" contact AS kontaktisik ,phone AS telefon,right( paymentcode,2) AS maksetin, discount AS ale FROM " +
			" customers c LEFT JOIN agents a ON a.kood=c.agent LEFT JOIN Vahetabel v ON v.klient=c.kood" +
			"     WHERE c.agent !='LUKUS' and c.kood in (select klient from dbo.custprodlist2 group by klient) ORDER BY c.nimi";

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

      sql = "SELECT c.kood AS klient,c.agent AS agent,a.nimi AS agentname FROM customers c INNER JOIN agents a ON a.kood=c.agent " +
     "  WHERE c.agent != 'LUKUS' and c.kood IN (select klient from dbo.custprodlist2 group by klient) GROUP BY c.kood,c.agent,a.nimi,c.nimi ORDER BY c.nimi";

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

    public static final String DRIVER_VERSION = "SolcraftLogSol";
    private String url = "jdbc:odbc:logsol";
    private Connection ddd ;
    private String firma;
    private String firma2;
    private String firma3;
    private String firma4;
    private String firma5;
    private String firma6;
    private String firma7;
    private String             url2 = "jdbc:odbc:report";
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

