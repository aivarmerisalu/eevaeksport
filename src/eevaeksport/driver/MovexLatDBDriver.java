package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalLineWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
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
import eevaeksport.manager.ManagerData;
import eevaeksport.report.DefectiveLineWrapperMeri;
import eevaeksport.report.EmailLogger;
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


public class MovexLatDBDriver extends AbstractDBDriver implements MustDister,Discounter,Manager,Promo,BulkUpdater,EmailLogger,PictureMaker{
  public MovexLatDBDriver(EksportPanel parent){
    super(parent);
    if(parent.getParentFrame().isDaySales()){
         this.subjectFilesTo = getParent().getString("senddatamsg");
    } else {this.subjectFilesTo = parent.getString("filemsg");}
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "oracle.jdbc.driver.OracleDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      stmt = con.createStatement();
     
    } catch (Exception e){
      parent.showError(e);
    }
    company = getParent().getProps().getProperty("companycode");
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  String company = null;

          public String getDealerCustomerCSV(String agent){
           return null;
           }

   
  /**
   *
   */

          public void setEmailLog(String logs) throws SQLException{
           
    	    String  agent = null;
    	    String  orderno = null;   
            long ordtime = 0;
            int linnum = 0;
            String cust = null;
            
    		StringTokenizer st = new StringTokenizer(logs, ";");

    		if (st.hasMoreTokens() && logs.trim().length()>0) {
    			ordtime = Long.parseLong(st.nextToken());		
    			agent = st.nextToken();
                
    		    if (st.hasMoreTokens()) {
    		    	orderno = st.nextToken();
    		    }
    		    if (st.hasMoreTokens()) {
    		    	cust = st.nextToken();
    		    }

    		    if (st.hasMoreTokens()) {
    		    	linnum  = Integer.parseInt(st.nextToken());
    		    }
    		    String sql = "INSERT INTO cesu.mj_sentorders VALUES (?,?,?,?,?)";
    		      try {
    		        Connection con = getNewConnection();
    		          PreparedStatement ps = con.prepareStatement(sql);
                    //insert into cesu.mj_sentorders values
                     //  ('20080303',      'MAN1',           'CUSTOMER'           ,'ordnum1'            ,3 )
    		          java.sql.Date ss = new Date(ordtime);
    		          ps.setString(1, getYearMonthDay(ss));
    		          ps.setString(2, agent);
    		          ps.setString(3, cust);
    		          ps.setString(4, orderno);
    		          ps.setInt(5, linnum);
    		          

    		            ps.executeUpdate();
    		          

    		          con.commit();
    		          ps.close();
    		          con.close();
    		      } catch (SQLException e) {
    		    	  con.close();
    		          e.printStackTrace();
    		      }    
    		    
    		}
    		
    		
            
            
            
            
          }

          
  public void connectionUp(){
  }

  public String getSaveDebtInfo() {

	  String  cmd = " select  PAYER from cesu.mjcustomerdata where BLOK2 > 0 group  by payer ";
	String cmd3  = " select PAYER as klient, summa as debt from cesu.mjpayerdebtdue ";
		

		if (isDebug()) System.out.println(cmd);
		TreeMap custs = new TreeMap();




		StringBuffer products = new StringBuffer();
		try {

		Connection con1 = getNewConnection();
		Statement st = con1.createStatement();
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
			String kood = rs.getString("payer").trim();
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
		if(cust.getDebt() != -1 && cust.getDebt() != -2){
			if(cust.getDebt() != -3 && cust.getID().length() > 3) {
				 products.append(cust.getID() + ";" + getParent().getNF().format(cust.getDebt()) + ";" + bl + "\n");
			}
			
		  }
		}

		return products.toString();

   }
 
  public String getAssortmentStr(Agent agent){
		StringBuffer sb = new StringBuffer();
		String cmd = " SELECT code as cfc6, item_code as mmitno, MARK as stat, item_group as grupp, item_name as nimetus, unit_volume as VOL FROM cesu.mj_must "+
       "  ORDER BY 1, 4, 3 desc, 6, 5 ";
		
 

		TreeMap tree = new TreeMap();
		try {
			Vector check = new Vector();
		
			System.out.println(cmd);
			Connection con = this.getNewConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String type = rs.getString("CFC6").trim();
				String id = rs.getString("MMITNO").trim();	
				String stat = rs.getString("STAT").trim().substring(2);	
				String sort = rs.getString("grupp");
				String sort2 = rs.getString("nimetus");
				String sort3 = rs.getString("VOL");
		
				
	  
  
			String line = " ;" + type + "; ;" + id + ";" + stat
			+ "\n";
		   
			if(stat.equals("000")){
				stat = "8";
			}
			if(stat.equals("001")){
				stat = "7";
			}
			if(stat.equals("010")){
				stat = "6";
			}
			if(stat.equals("011")){
				stat = "5";
			}
			if(stat.equals("100")){
				stat = "4";
			}
			if(stat.equals("101")){
				stat = "3";
			}
			if(stat.equals("110")){
				stat = "2";
			}
			if(stat.equals("111")){
				stat = "1";
			}
   if(sort3.length() < 6){
	   while(sort3.length()<6){
		   sort3 = sort3 + "0";
	   }
   }
			
			tree.put(type+sort+stat+"-"+sort3+sort2+id, line);
  }
			
			
			int c = 0;
			Iterator itera = tree.keySet().iterator();
			while (itera.hasNext()){
				String id = (String)itera.next();
				sb.append((String)tree.get(id));
				if(id.indexOf("10DUS")!= -1 && c < 30){
					//   System.out.println(id);
					   c++;
				}
			
			}
			
			

			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
    }




  public String getSaveStockInfo(){
   
    String cmd = "Select TOODE, STOCK, EXPI from cesu.mjstock";

      if (isDebug()) System.out.println(cmd);


      StringBuffer products = new StringBuffer();
      try {

        Connection con1 = getNewConnection();
        Statement st = con1.createStatement();
        ResultSet rs = st.executeQuery(cmd);

        while (rs.next()) {

          String kood = rs.getString("TOODE").trim();
              products.append(kood +  ";");
              products.append(" ;");
              int stock = (int) rs.getDouble("STOCK");
              if(stock >50000){
            	  stock = 50000;
              }
              String expi = rs.getString("EXPI");
              
              products.append(stock + ";");
              products.append(" ;");
              products.append(" ;");

              if(expi != null && expi.trim().length() > 0){
            	  products.append("BB :" + expi.trim() + "\n");
              }  
        
        }
          rs.close();
          st.close();
          con1.close();
      } catch (Exception e) {
          getParent().showError(e);
      }
      makeTaskFile(parent.getProps().getProperty("stockrefdir"));
      return products.toString();
  }


                                         public String  getCustProdDiscounts(Agent agent){
                                          return null;
                                          }



  /**
   *
   */
  public void connectionDown(){
  }
  
 private  TreeMap ags = null;

  public TreeMap getAgents(){
	  if (ags != null){
		  return ags;
	  }else{
		  ags = new TreeMap();
	  }
	  
    String cmd = "SELECT CTSTKY AS agent ,CTTX40 AS NIMI FROM mvxjdta.CSYTAB WHERE CTSTCO='SMCD'  AND substr(CTSTKY,0,1) != '*' ORDER BY CTSTKY";
     cmd = "select agent, NIMI  from cesu.mjsalesman";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    HashMap aHash = new HashMap();
    try {
    	Connection con = getNewConnection();
        Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = rs.getString("NIMI").trim();
          agent.setName(name);
          
          agent.setLanguage("LAT");
          agent.setCountry("lv");
          agent.setPassWord("0000");
          agent.setActive(true);
          ags.put(name, agent);
          agents.put(name, agent);
          aHash.put(agent.getID(), agent);
        }
    rs.close();
    stmt.close();
    con.close();

    } catch (Exception e){
        getParent().showError(e);
      }
 //   getAssortmentStr(null);
    return agents;
  }



      public String getDistribution() {
        return null ;
      }

      
      public void log(String s) {
		getParent().log(s);
	}


  public TreeMap getCustomers(Agent agent){
    TreeMap customers = new TreeMap();
    makeTaskFile(null);

    String horecaGrs = getParent().getProps().getProperty("horeca.sortiment.groups");
    
    try {
      Connection con = getNewConnection();
       
        
        Hashtable debts = new Hashtable();
        Hashtable sales = new Hashtable();
       Hashtable kama = new Hashtable();
       Hashtable kama1 = new Hashtable();
       Hashtable delextra = new Hashtable();
       
       Hashtable invest = new Hashtable();
       

       String cmdk = "SELECT PYNO as kood,agrnr as agrno,fvdt as startD,lvdt as endD,current_fulfill as sold,agr_end_fullfill as estimate " + 
       		", current_fulfill_coll as coloursold, agr_end_fullfill_coll as coloursesti FROM cesu.mjhorecainvest ";
       
       Statement    st = con.createStatement();
     ResultSet   rs1 = st.executeQuery(cmdk);
        StringBuffer sbinvest = new StringBuffer();
                         while(rs1.next()){
                        	 // CustomerCode: startdate; endddate;fulfilment; estimation; color (G or R)

                           String id = rs1.getString("kood");
                 if(id != null) {
                           String agrno = rs1.getString("agrno");
                           String startD = rs1.getString("startD");
                           String endD = rs1.getString("endD");
                           Double done = rs1.getDouble("sold");
                           Double estimate = rs1.getDouble("estimate");
                           if(estimate == null) {
                        	   estimate = 0.0;
                           }
                           String colsold = rs1.getString("coloursold").trim();
                           String colesti = rs1.getString("coloursesti").trim();
                            if(done != null) {
                            	sbinvest.append(id.trim()+";" + agrno +";"+startD+";"+endD+";"+done+";"+estimate+";"+colsold+ ";"+colesti + "\n");
                            	invest.put(id, agrno +";"+startD+";"+endD+";"+done+";"+estimate+";"+colsold+ ";"+colesti + "\n");
                            }
                         }
                    }
      rs1.close();
      st.close();
     // con.close();
      
       
       
       // tarne p�evade lisamine 
       String cmd31 = "SELECT ddsmcd, ddcuno as saaja , dddays as days FROM cesu.MJDELDAY ";
       Statement st1 = con.createStatement();
    if (isDebug()) System.out.println(cmd31);
        rs1 = st1.executeQuery(cmd31);
        while (rs1.next()) {
       	 String cus = rs1.getString("saaja");
          int days = Integer.parseInt(rs1.getString("days").trim());          
          delextra.put(cus, days + "");     
        } 
        rs1.close();
        st1.close();
      
       
       
        String cmd3 = "select PAYER as saaja, summa from cesu.mjpayerdebtdue ";
         st1 = con.createStatement();
     if (isDebug()) System.out.println(cmd3);
         rs1 = st1.executeQuery(cmd3);
         while (rs1.next()) {
        	 String cus = rs1.getString("saaja");
           double debt = Double.parseDouble(rs1.getString("SUMMA").trim());          
           debts.put(cus, debt + "");     
         } 
         rs1.close();
         st1.close();
        
         String sql9 = "SELECT oksmcd, uccuno as klient , name as luu, KLI || '\t' || MAKS as asi FROM cesu.MJDAILYKEG ";
         Statement st5 = con.createStatement();
         if (isDebug()) System.out.println(sql9);
            ResultSet rs5 = st5.executeQuery(sql9);
             while (rs5.next()) {
            	 try{
            	 	 String cus =  rs5.getString("klient").trim();
                     String  luu = rs5.getString("luu").trim();
                     String add =  "\t\t";
                     if (luu.length() > 12){
                    	 luu = luu.substring(0,12);
                    	 add = "\t";
                     }
                     String  asi = rs5.getString("asi").trim();
                     if(kama.get(cus) != null){
                    	 String as = (String) kama.get(cus);
                    	 kama.put(cus,luu + add + asi + "|" + as);
                     }else{
                    	  kama.put(cus, luu + add + asi);	
                     }
                    
            	 }catch (Exception e){
            		e.printStackTrace(); 
            	 }
           
             }
             rs5.close();
             st5.close();
         
        
             String sql91 = "SELECT * FROM cesu.mjhorecaequipment ";
             
             HashSet kk = new HashSet();
             Statement st51 = con.createStatement();
             if (isDebug()) System.out.println(sql91);
                ResultSet rs51 = st51.executeQuery(sql91);
                 while (rs51.next()) {
                	 try{
                	 	 String cus =  rs51.getString("kood").trim();
                         String luu = rs51.getString("equipname").trim();
                         String add =  "\t\t";
                         if (luu.length() > 20){
                        	 luu = luu.substring(0,20);
                        	 add = "\t";
                         }
                         String  asi = rs51.getString("saldo").trim();
                         if(kama1.get(cus) != null){
                        	 String as = (String) kama1.get(cus);
                        	 kama1.put(cus, as + "|" + luu + add + asi );
                         }else{
                        	  kama1.put(cus, "||Nosaukums   \t\t\tAtlikums|" + luu + add + asi );	
                         }
                	 }catch (Exception e){
                		e.printStackTrace(); 
                	 }
               
                 }
                 rs51.close();
                 st51.close();
            
             
             
             
         
         String sql7 = "select PAYER as klient, balance from cesu.mjpayerdebt";
         Statement st2 = con.createStatement();
         if (isDebug()) System.out.println(sql7);
            ResultSet rs2 = st2.executeQuery(sql7);
             while (rs2.next()) {
            	 String cus = rs2.getString("klient");
               double myyk = rs2.getDouble("balance");
               sales.put(cus, myyk+"");
             }
             rs2.close();
             st2.close();
    	


String    cmd2 = " select KLGRUPP, LICENCE, KOOD, NIMI, AADRESS1, VISIT, SQ_VISIT, LIMIIT, AGENT, PAYER, EILADU, "
		+ "DEBT, BLOK2, ROUTE_NR, ROUTE_NAME, SQ_INROUTE, GRUPA, LATITUDE, LONGTITUDE, TELENR, GNAME from cesu.mjcustomerdata  ";

	  Converter cc = new Converter();
	  HashMap kyssad =   cc.getQuestionaire(this.getExcelDir());

      if (isDebug()) System.out.println(cmd2);
       st = con.createStatement();
      ResultSet rs = st.executeQuery(cmd2);
      HashMap custMap = new HashMap();

      while(rs.next()){
    	  String klgrupp = rs.getString("klgrupp");
    	  if (klgrupp != null ){
    		  klgrupp = klgrupp.trim();
    	  }else{
    		  klgrupp = "";
    	  }
    
        String licence = rs.getString("licence");
          String accountnum = rs.getString("kood").trim();
          Customer cust = new Customer(accountnum);
          String name = rs.getString("nimi").trim();
          if("31RKi".equals(klgrupp)){
        	cust.allowGift = true;  
          }
          
          cc.addQuestiosToCustomer(cust,kyssad);  
          String aadress1 = rs.getString("aadress1");
      

          if (aadress1 != null) {aadress1 = aadress1.trim();} 
          String addr = aadress1.toLowerCase();
          cust.setName(Converter.enCodeEst(name + " " + accountnum + " " + addr));
          
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress1),"Latvia"));
          cust.setLicence(licence);
          
          String visit = rs.getString("visit");
          if(visit != null){
            cust.setVisit(visit.trim());
          }
          
          int  visitsq = rs.getInt("SQ_VISIT");
          if(visitsq > 0){
            cust.isVisitToday();
            String aaa = visitsq + "";
            if(aaa.length() == 1){
            	aaa = "00" + aaa;
            }
            if(aaa.length() == 2){
            	aaa = "0" + aaa;
            }
            cust.setName(aaa + " " + name + " " + accountnum + " " + addr);
          }
          
          String k0 = (String) kama.get(cust.getID());
           
          if(kama.get(cust.getID()) != null){
        	  if(cust.getInfo() != null && cust.getInfo().trim().length() == 0){
        		 cust.setInfo("Nosaukums    \t\tKLI\tMAKS|" + k0);       		 
        	  }else{
        		 cust.setInfo(cust.getInfo() + "|" + k0 );
        	  }
          }
          
           String k1 = (String) kama1.get(cust.getID());
          if(kama1.get(cust.getID()) != null){
        	 if(cust.getInfo() != null && cust.getInfo().trim().length() == 0){
        		 cust.setInfo( k1 );       		 
        	    } else {
        		 cust.setInfo(cust.getInfo() + "|" + k1 );
        	  }
           }
          
          
          if(delextra.get(cust.getID()) != null){
        		 cust.setDelExtraDays(Integer.parseInt(delextra.get(cust.getID())+ ""));      	  
          }
          
          
          
          double limiit = rs.getDouble("limiit");
         // System.out.println(limiit);
          if (limiit != 0){cust.setCredit(limiit+ "");}
          String agent1 = rs.getString("agent").trim();

          if(agent1.length()>3){
        	  agent1 = agent1.substring(0,3);
          }

          cust.addAgent(new Agent(agent1));


          cust.setPayer(rs.getString("payer").trim());
          
          if(invest.get(cust.getPayer()) != null) {
        	  sbinvest.append(cust.getID()+ ";" + invest.get(cust.getPayer()));
        	  cust.investstatus = true;
        	  System.out.println("Payer " + cust.getPayer() + invest.get(cust.getPayer())  );
          }
         
          if(invest.get(cust.getID()) != null) {
        	  cust.investstatus = true;
        	  System.out.println("Cust ID " + cust.getID() + invest.get(cust.getID()) );
           }
        
          
          String taraDeal = rs.getString("EILADU").trim();
          if(taraDeal != null && taraDeal.trim().length() > 0){
              cust.taraDeal = taraDeal;
              }
          
          String  blokk = rs.getString("blok2") ;

          if(!blokk.trim().equals("0")){
          cust.setSalesBlock(true);
          }

        /*  String allagents = rs.getString("toall");
            if(allagents.trim().equals("1")){
            Enumeration enums = getParent().getAgentsHash().elements();
             while (enums.hasMoreElements()) {
              Agent a =(Agent) enums.nextElement();
              cust.addAgent(a);
            }
            }*/
         // cust.setDebt(rs.getDouble("debt"));
            
            if(debts.get(cust.getPayer()) != null){
            	cust.setDebt(Double.parseDouble((String)debts.get(cust.getPayer())));
            }
            
            if(sales.get(cust.getPayer()) != null){
            double myyk = Double.parseDouble((String)sales.get(cust.getPayer()));
            cust.setUsedCredit(myyk);
            try{
           	 double limi = Double.parseDouble(cust.getCredit());
                if(limi != 0  && myyk > limi ){
               	 cust.setSalesBlock(true);
                }
            }catch(Exception e){
           	 
            }
            
        	
            }
            if(klgrupp.trim().length() > 1 ){
                	 cust.setGroup(klgrupp.trim());
                	 cust.setCity(klgrupp.trim());
                 }
            String route = rs.getString("ROUTE_NR");
            String routename = rs.getString("ROUTE_NAME");
            String routeseq = rs.getString("SQ_INROUTE");
            if(routeseq == null){
            	routeseq = "";
            }
            
            String grupa = rs.getString("GRUPA");
            String lat = rs.getString("LATITUDE");
            String lll = rs.getString("LONGTITUDE");
            String telef = rs.getString("TELENR");
            String gname = rs.getString("gname");
            
            if(gname != null && gname.trim().length() > 0){
           	 cust.category = gname;
           }
            
            if(telef != null && telef.trim().length() > 0){
            	 cust.setPersonalData("Tel: " + telef.trim());
            }
            
            if(grupa != null && grupa.trim().length() > 0){
            	cust.setCustType(grupa.trim());
            	cust.setChain(grupa.trim().replaceAll("grupa", " ").trim());
            	
            //	System.out.println(cust.getChain());
            }
            
            if(route != null && route.trim().length() > 0){
            	if(cust.getCity().trim().length() > 0){
                	cust.setCity(cust.getCity() + "| <<" + route + " " + routename + " " + routeseq);
                }else{
                	cust.setCity("<< " + route + " " + routename + " " + routeseq);
                }
            }  
            
            if(lat != null && lat.trim().length() > 0){
            	if(cust.getCity().trim().length() > 0){
            		cust.setCity(cust.getCity() + "|>< �" +lat + "," +lll + "�");
               }else{
            		cust.setCity(">< �" +lat + "," +lll + "�");
               }
            }
                  
            
          if(  horecaGrs != null && (horecaGrs.contains("," + klgrupp) || horecaGrs.contains(klgrupp + ","))) {
        	  cust.addFixSortCode("HORECAFIX");
          }
            
          customers.put(cust.getName(), cust);
          custMap.put(cust.getID(), cust);
    }

         
         st.close();
         con.close();

         if(sbinvest.toString().length() > 0) {
           	Converter.saveFile("salescontracts.txt", sbinvest.toString());
           }else {
           	Converter.saveFile("salescontracts.txt", "");
           }
           
         
         
    } catch (SQLException e){
      getParent().showError(e);
    }
    return customers;
  }

  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection("jdbc:oracle:thin:@10.10.100.97:1521:GM3REP1", getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "select liik, nimetus  from cesu.MJProductGroups1 " ;

    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
        tm.put(rs.getString("liik").trim(),
        		Converter.enCodeEst( rs.getString("nimetus").trim()));
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}

    public TreeMap getProdGroupsSecond() throws SQLException {

      Connection con = getNewConnection();
      TreeMap   tm = new TreeMap();
      String    sql = " select liik, nimetus  from cesu.MJProductGroups2 ";
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);

      while (rs.next()) {
          tm.put(rs.getString("liik").trim(),
        		  Converter.enCodeEst(rs.getString("nimetus").trim()));
      }
      rs.close();
      st.close();
      con.close();

      return tm;
  }




  public TreeMap getProducts(Agent agent){

	  prices = new Hashtable();
	  Connection con = null;
String ex = parent.getProps().getProperty("products");
    if (ex == null){

    	ex  = "";
    }

    String keg = parent.getProps().getProperty("kegs");
        if (keg == null){
        keg  = "";
        }
        
        String deflist = parent.getProps().getProperty("deflist");
        if (deflist == null){
        	deflist  = "";
        }
        
        

     
   String cmd = "SELECT * FROM CESU.MJDAILYITEMS";

   String cmd3 = "SELECT * FROM cesu.mjreklama";
   
   String cmd2 = "select * from cesu.mjstock";

    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    HashMap prodHash = new HashMap();
    try {
       con = getNewConnection();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(cmd);

      while(rs.next()){
        // if ( kood.length() > 2 ){
        String kood = rs.getString("kood").trim();
        Product prod = new Product(kood);
        String abi = rs.getString("abi").trim();
        String nimetus = abi + " " + rs.getString("nimetus").trim();
        prod.setName(Converter.enCodeEst(nimetus));

        prod.setInCases( (rs.getDouble("tykke")));
        Double hh = rs.getDouble("hind");
        prod.setPrice(hh);
        prices.put(kood, prod.getPrice() +"");
        prod.setPriceSecond(hh);
        String grupp = rs.getString("grupp") ;
         if (grupp != null && grupp.trim().length()>0) {
           prod.setGroup(grupp.trim());
         }

       /*  String pal = rs.getString("onpallet");
          if (pal != null && pal.trim().length()>0) {
          double pallet = Double.parseDouble(pal);
          prod.setOnPallet(pallet);
          }*/

       String ean = rs.getString("ean");
       if(ean != null && ean.trim().length()>7){
         prod.setEAN(ean.trim());
       }
        
       
       
         prod.setVat("21"); //kontrolli
       
       
       String unit =rs.getString("unit");

        if (unit != null && unit.trim().length()>0){
        unit = unit.trim();
        prod.setUnit(unit);
        prod.setGroupSecond(unit);
       // System.out.println("'"+unit+"'");
        if(unit.equals("1/4 PALETE") || unit.equals("1/2 PALETE") ){
      //System.out.println(nimetus);
      prod.setDiffColor(true);
      }

        }

        String id = rs.getString("id");
       // prod.setSendSeparate("100");
       // System.out.println("*" + id.trim() + "*");
            if (id.trim().equals("50") || id.trim().equals("60") ){
            prod.setSentExtra("SE");
           // prod.setSendSeparate("101");
            } else if (id.trim().equals("90")) {
              prod.setSentExtra("TA");
            }
            Double pall = rs.getDouble("palletspace");
            if(pall != null && pall > 0) {
            	  	prod.setPalSpace(1/pall);
                    prod.setOnPallet(pall);
            }
        
        if(rs.getString("prakis").equals("1")){
            prod.setDefectiveList("1");
        }
        if(deflist.trim().length() >0 && deflist.indexOf(prod.getID()) != -1){
        	prod.setDefectiveList("2");
        }
      
        
        
        String sort = rs.getString("sort");
        if(sort != null && sort.trim().length() > 0){
        	if(sort.trim().length()== 1){
        		sort = "000" +sort.trim();
        	}else if (sort.trim().length()== 2){
        		sort = "00" +sort.trim();
        	}else if (sort.trim().length()==3){
        		sort = "0" +sort.trim();
        	}
        	prod.setSorting(sort.trim()+prod.getID());
        }

        if(grupp.equals("TARA")){
        	log("TARA " + prod.getName());
        }
        
        prod.setAllowGift(false);        
        prod.setPrice( (rs.getDouble("price")));
        Integer newP = rs.getInt("statusnew");
        if(newP != null && newP == 1) {
        	prod.setStatusNew("1");
        }
        
        prodHash.put(prod.getID(), prod);
        products.put(nimetus, prod);
        
      }
      
      rs.close();
      st.close();
      con.close();

      

      con = getNewConnection();
      st = con.createStatement();
       ResultSet rs1 = st.executeQuery(cmd2);
                         while(rs1.next()){
                           String id = rs1.getString("TOODE").trim();
                           Product prod1 = (Product)prodHash.get(id);
                           double sto = rs1.getDouble("STOCK");
                           String expi = rs1.getString("EXPI");
                           if (prod1 != null){
                           prod1.setStock(sto);
                           if(expi != null && expi.trim().length() > 0){
                        	   prod1.setPartii("BB :" + expi.trim());
                           }
                           }
                         }

      rs1.close();
      st.close();
      con.close();
      
      con = getNewConnection();
      st = con.createStatement();
       rs1 = st.executeQuery(cmd3);
                        while(rs1.next()){
                          String id = rs1.getString("ITNO").trim();
                          Product prod1 = (Product)prodHash.get(id);
                          
                          if (prod1 != null){
                            prod1.setAllowGift(true);
                          System.out.println("gift allowed " + id );
                          }
                        }

     rs1.close();
     st.close();
     con.close();
      

     String cmdk = "SELECT * FROM CESU.mjkegoverdue ";
     con = getNewConnection();
     st = con.createStatement();
      rs1 = st.executeQuery(cmdk);
      StringBuffer sb = new StringBuffer();
                       while(rs1.next()){
                         String id = rs1.getString("kood").trim();
                         String name = rs1.getString("name").trim();
                         Double saldo = rs1.getDouble("saldo");
                         Double to = rs1.getDouble("turnover");
                         Double oc = rs1.getDouble("overc");
                         String col = rs1.getString("colour").trim();
                         sb.append(id+";"+name+";"+saldo+";"+to+";"+oc+";"+col + "\n");
                         
                       }

    rs1.close();
    st.close();
    con.close();
    if(sb.toString().length() > 0) {
    	Converter.saveFile("kegoverdue.txt", sb.toString());
    }else {
    	Converter.saveFile("kegoverdue.txt", "");
    }
    
     try{
   	  FileReader fr = new FileReader("markedproducts.txt");
         BufferedReader reader = new BufferedReader(fr);
       String line = null;
       while((line = reader.readLine()) != null){
       	StringTokenizer sto = new StringTokenizer(line, ";");
           if (sto.hasMoreTokens()){
           	String id= sto.nextToken();
           	sto.nextToken();
           	String status= sto.nextToken();
           	Product pr = (Product)prodHash.get(id);
           	log(line);
           	if(pr != null){
           		if (status.indexOf("a") != -1){
           			pr.setMustBe("1");
           		}
           		if (status.indexOf("b") != -1){
           			pr.setStatusTop("1");
           		}
           		if (status.indexOf("c") != -1){
           			pr.setStatusDiscount("1");
           		}
           		if (status.indexOf("d") != -1){
           			pr.setStatusNew("1");
           		}
           		if (status.indexOf("e") != -1){
           			pr.setStatusSellout("1");
           		}
           	log(pr.getStatusDiscount() + " " + pr.getStatusNew() + " " + pr.getStatusSellout() + " " + pr.getStatusTop());	
           	}
           	
           }
       }
   }catch(Exception e ){
   	//e.printStackTrace();
   	//log(e.toString());
   }
     
    } catch (Exception e){
      getParent().showError(e);
      try {
		con.close();
	} catch (SQLException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
    }

    return products;
  }
  
  
  
  public Vector getCustomerProductsList(String agentCode){

	  
String cmd =   " select toode, saaja,  kogus,  kuup from cesu.mjprodorderhist ";

    
    if (isDebug())
      System.out.println(cmd);

    Vector custProds = new Vector();
    HashMap kliendid = new HashMap();

    try {
      Connection con = getNewConnection();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(cmd);
       while (rs.next()) {
        String tootekood = rs.getString("toode").trim();
        String kliendikood = rs.getString("saaja").trim();
        double kogus = rs.getDouble("kogus");
       String kuup = rs.getString("kuup");
      Customer cust = (Customer)kliendid.get(kliendikood);
             if (cust == null){ cust = new Customer(kliendikood);}
      CustProd cp = new CustProd(kliendikood, tootekood, kogus, 0, 0,kuup,0,0,0," ");
      Product prod = new Product(tootekood);
      cust.addProduct(prod, new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,kuup,0,0,0," "),false);
      custProds.add(cp);
      kliendid.put(kliendikood, cust);


      }
      rs.close();
      st.close();
      con.close();


      String cmd7 = "SELECT oksmcd,  pmmtno as toode,uccuno as saaja, QUANT as kogus FROM cesu.mjcusttara";
      con = getNewConnection();
      Statement st2 = con.createStatement();
      ResultSet rs2 = st2.executeQuery(cmd7); 
      while(rs2.next()){
    	  
    	  String tootekood = rs2.getString("toode").trim();
          String kliendikood = rs2.getString("saaja").trim();
          double kogus = rs2.getDouble("kogus");
         String kuup = "tara";
        Customer cust = (Customer)kliendid.get(kliendikood);
               if (cust == null){ cust = new Customer(kliendikood);}
        CustProd cp = new CustProd(kliendikood, tootekood, kogus, 0, 0,kuup,0,0,0," ");
        Product prod = new Product(tootekood);
        cust.addProduct(prod, new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,kuup,0,0,0," "),false);
        custProds.add(cp);
        kliendid.put(kliendikood, cust);
  
      }
    
      rs2.close();
      st2.close();
      con.close();



      
      
      
    }
    catch (Exception e) {
      getParent().showError(e);
    }

    return custProds;

  }


  public void setBulkSales(Map agents){

    String cmd =  "Select  agent,   summacur,  summavol from cesu.mjbulksales";

      if (isDebug()) System.out.println(cmd);
          try {
            Connection con = getNewConnection();
              Statement stmt = con.createStatement();
              Calendar cal = Calendar.getInstance();
              cal.set(Calendar.DAY_OF_MONTH, 1);
              ResultSet	      rs = stmt.executeQuery(cmd);
              while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                if (agentID.length()>2){
                  if (agentID.substring(2, 3).equals("1") ||
                      agentID.substring(2, 3).equals("2")
                      || agentID.substring(2, 3).equals("3")) {
                    agentID = agentID.substring(0, 2);
                  }
                }
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                  agent.setMonthSales((int)rs.getDouble("summacur"));
                  agent.setMonthSalesVol((int)rs.getDouble("summavol"));
             //    System.out.println("agent" + agent.getName() + " " + agent.getMonthSales()+ " " + agent.getMonthSalesVol());
                }
              }
              rs.close();
              stmt.close();
              con.close();
          } catch (Exception e) {
              getParent().showError(e);
          }


  }



  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


  public DealerProduct getDealerProductsList() {
        return null;
      }



      public Vector getDebt(String agentCode) throws SQLException{
       Vector list = new Vector();
       String sql = " select  maksja,klient, arve, kuupaev, SUMMA, paevi, tahtaeg from cesu.MJINVOICEDEBT  " ;

     if (isDebug()) System.out.println(sql);
       Connection con = getNewConnection();
       stmt = con.createStatement();
       ResultSet rs = stmt.executeQuery(sql);

       while(rs.next()){
         Debt debt = new Debt();
         debt.setPayerID(rs.getString("maksja").trim());
         debt.setCustID(rs.getString("klient").trim());
         debt.setDoknr(rs.getString("arve").trim());
         Date dat = getDate(rs.getString("kuupaev").trim());
         debt.setDate(dat);
         debt.setSum(rs.getDouble("summa"));
         debt.setDue(rs.getString("paevi"));
         debt.setDuedate(getDate(rs.getString("tahtaeg").trim()));
           list.addElement(debt); 
          
       }
       rs.close();
       stmt.close();
       con.close();
       return list;
    }

    public ManagerData getManagerData() throws SQLException{
       ManagerData md = new ManagerData();
       return md;
     }


    public double getFoodSales(String agentCode) throws SQLException{
      return 0;
    }

    public double getAlcoSales(String agentCode) throws SQLException{
      return 0;
    }
    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
    }

    public String getStartingTime() {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MONTH,
              -Integer.parseInt(getParent().getProps().getProperty("months")));
      int month = cal.get(Calendar.MONTH) + 1;
      String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
      String sqlDate = cal.get(Calendar.YEAR) + month + day;
      return sqlDate;
    }

    public String getDriverVersion() {
      return "MovexLAT";
    }

    public String getSubjectFilesTo() {
      return this.subjectFilesTo;
    }


    public String getDaySalesData(Agent agent) {
      String sql = "SELECT t.MMspe2 AS grupp,SUM(UCSAAM) AS summa " +
         "FROM mvxjdta.OSBSTD d ,mvxjdta.MITMAS t WHERE t.MMITNO=d.UCITNO " +
         "AND substr(UCIVDT,0,6) = '" + getYearMonth() + "' " +
         "AND UCSMCD='" +agent.getID() + "' AND UCITTY = '01' AND M9FACI=200 GROUP BY t.MMspe2 ORDER BY t.MMspe2 ";
         System.out.println(sql);
         StringBuffer sb = new StringBuffer();

         sb.append("\n " + getParent().getString("string5") + "\n\t" + getParent().getString("string6") +  "\n\n");
         sb.append(getParent().getString("string4") +  "\n");
           NumberFormat nn = getNF();
           nn.setGroupingUsed(false);
           try {
             Connection con = getNewConnection();
             stmt = con.createStatement();
             ResultSet rs2 = stmt.executeQuery(sql);
             while (rs2.next()) {
               sb.append(" " + rs2.getString("grupp") + "\n                 " +
                         nn.format(rs2.getDouble("summa")) +
                         " EUR\n-------------------------------------\n");
             }
             rs2.close();
             con.close();
             sb.append("\n\n" + getParent().getString ("string3") + "\n\n");
             sb.append(getParent().getString("string2") + "\n\n");
              } catch (Exception E){}

      return sb.toString();
    }



    public String getPilotDayData(Agent agent){


      String sql = "SELECT t.MMspe2 AS grupp,SUM(UCSAAM) AS summa " +
          "FROM mvxjdta.OSBSTD d ,mvxjdta.MITMAS t WHERE t.MMITNO=d.UCITNO " +
          "AND substr(UCIVDT,0,6) = '" + getYearMonth() + "' "+
          "AND UCSMCD='" +agent.getID() + "' AND UCITTY = '01'  GROUP BY t.MMspe2 ORDER BY t.MMspe2 ";

      String sql2 = "SELECT t.MMITDS As nimetus ,dxdisp AS ale ,dxlimt AS tase FROM  mvxjdta.ogdmtx ah, mvxjdta.MITMAS t WHERE  t.MMITNO=ah.dxobv1 " +
          "AND substr(dxfvdt,0,6) = '" + getYearMonth() + "' AND dxdipo=1 AND dxdisy=100 " +
          "    AND dxdisp>0 GROUP BY t.MMITDS,dxdisp,dxlimt ";

      String sql3 = " SELECT d8obv1,t.MMITDS AS nimetus,d8disp AS ale,d8limt AS tase from mvxjdta.ogcmtx, mvxjdta.MITMAS t " +
          "WHERE d8disy = 100 AND d8dipo=1 AND d8cono=200 AND  d8obv1=mmitno AND substr(d8fvd1,0,6) = '" + getYearMonth() + "' " ;



      String sql4 =
          "SELECT ucivno as nr,okcunm AS nimi FROM mvxjdta.OSBSTD d ,mvxjdta.OCUSMA k " +
          " WHERE uccuno=OKCUNO AND to_char(to_date(to_char(d.UCIVDT),'YYYYMMDD')) = (to_char(sysdate)) " +
          " AND UCSMCD='" + agent.getID() + "' AND UCITTY = '01' GROUP BY ucivno,okcunm ORDER BY ucivno,okcunm";


      System.out.println(sql);
      StringBuffer sb = new StringBuffer();
       try {
         sb.append("\n " + getParent().getString("todaysbills") + "\n\n");

         Connection con4 = getNewConnection();
         stmt = con4.createStatement();
         ResultSet rs5 = stmt.executeQuery(sql4);
         while (rs5.next()) {
           sb.append(rs5.getString("nr") + " " + rs5.getString("nimi") + "\n\n");
         }
         rs5.close();
         con4.close();

         sb.append("\n\n " + getParent().getString("string5") + "\n\t" +
                   getParent().getString("string6") + "\n\n");
        sb.append(getParent().getString("string4") +  "\n");
        NumberFormat nn = getNF();
        nn.setGroupingUsed(false);

          Connection con = getNewConnection();
          stmt = con.createStatement();
          ResultSet rs2 = stmt.executeQuery(sql);
          while (rs2.next()) {

            sb.append(" " + rs2.getString("grupp") + "\n                 " +
                      nn.format(rs2.getDouble("summa")) +

                      " EUR.\n-------------------------------------\n");
          }
      //    System.out.println(sb.toString());
          con.close();
          sb.append("\n\n" + getParent().getString("string3") + "\n\n");
          sb.append(getParent().getString("string2") + "\n\n");
          Connection con2 = getNewConnection();
          String nimetus = null;
          String ale = null;
          String kogus = null;
         Statement st = con2.createStatement();
          ResultSet rs3 = st.executeQuery(sql2);
          System.out.println(sql2);
          while (rs3.next()) {
            nimetus = rs3.getString("nimetus").trim();
            ale = rs3.getString("ale");
            kogus = rs3.getString("tase").trim();
           sb.append(nimetus + "\n");
           sb.append("\t\t" + ale + " %\t" + kogus + "\n\n");
          }

        Connection con3 = getNewConnection();
        Statement stv = con3.createStatement();
        ResultSet rs4 = st.executeQuery(sql3);
        System.out.println(sql3);
        while (rs4.next()) {
          nimetus = rs4.getString("nimetus").trim();
          ale = rs4.getString("ale");
          kogus = rs4.getString("tase").trim();
          sb.append(nimetus + "\n");
          sb.append("\t\t" + ale + " %\t" + kogus + "\n\n");
        }
        st.close();
        con2.close();
        stv.close();
        con3.close();
     
      }
        catch (Exception e) {
          getParent().showError(e);
        }


        return sb.toString();
    }
    
    public void insertDefectiveMeri(eevaeksport.report.DefectiveWrapperMeri wr){
    	
    	      String sql = "INSERT INTO cesu.MJ_QUALITY VALUES (?,?,?,?,?,?,?,?,?,?,sysdate,?,' ',' ',0,0)";
    	      TreeMap toxml = new TreeMap();
    	      try {
    	        Connection con = getNewConnection();
    	          PreparedStatement ps = con.prepareStatement(sql);
    	          String agent = wr.getDefective().getAgentID();
    	          ps.setString(1, agent);
    	          ps.setString(2, wr.getDefective().getAgentName());

    	          for (Iterator iter = wr.getDefective().getLines().iterator(); iter.hasNext(); ) {
    	            DefectiveLineWrapperMeri lw = new DefectiveLineWrapperMeri(iter.next());
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
    	          //  System.out.println(kogus+"bb '"+lw.getLine().getBestBefore()+"'"+bb+"'");
    	            ps.setString(8, bb);
    	            ps.setInt(9, lw.getLine().getDefectCode());
    	            ps.setString(10, lw.getLine().getProdType());
    	            ps.setString(11, lw.getLine().getDefectText());

    	            ps.executeUpdate();
    	            if(toxml.get(lw.getLine().getCustID()) != null){
    	            	Vector vv = (Vector)toxml.get(lw.getLine().getCustID());
    	            	vv.add(lw);
    	            	toxml.put(lw.getLine().getCustID(), vv);
    	            }else{
    	            	Vector vv = new Vector();
    	            	vv.add(lw);
    	            	toxml.put(lw.getLine().getCustID(), vv);
    	            }
    	          }
    	          
    	          makeXMLMeri(toxml,agent);

    	          con.commit();
    	          ps.close();
    	          con.close();
    	      } catch (SQLException e) {
    	          e.printStackTrace();
    	      }
    	    }
  
  
    
   private void makeXML(TreeMap toxml,String agent){}


   private void makeXMLMeri(TreeMap toxml,String agent){
	  	
	      String ls = System.getProperty("line.separator");
	       Iterator itera = toxml.keySet().iterator();
	  	
	  	while (itera.hasNext()){
	  		String custid = (String)itera.next();
	  		Vector lines = (Vector)toxml.get(custid); 
	  		 Calendar cal = Calendar.getInstance();
	  	        String ordnr = (cal.getTimeInMillis()+"").substring(3);
	  	      cal.add(Calendar.DAY_OF_MONTH, 1);
	  	        
	  	        StringBuffer sb = new StringBuffer();
	          sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + ls);
	          sb.append("<Envelope xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:env=\"http://www.intentia.com/MBM_Envelope_1\" xmlns=\"http://www.intentia.com/schemas/mec\">" + ls);
	          sb.append(" <Header>" + ls);
	          sb.append("  <env:delivery>" + ls);
	          sb.append("  <env:to>" + ls);
	          sb.append("    <env:address>cesualus</env:address> " + ls);
	          sb.append("  </env:to>" + ls);
	          sb.append("  <env:from>" + ls);
	          sb.append("    <env:address>MobileJava</env:address> " + ls);
	          sb.append("  </env:from>" + ls);
	          sb.append("  </env:delivery>" + ls);
	          sb.append("  <env:manifest>" + ls);
	          sb.append("    <env:reference uri=\"#ORDERS@1\">" + ls);
	          sb.append("      <description /> " + ls);
	          sb.append("    </env:reference>" + ls);
	          sb.append("  </env:manifest>" + ls);
	          sb.append(" </Header>" + ls);
	          sb.append("<Body>" + ls);
	          sb.append("<ORDERS>" + ls);
	          sb.append("   <BGM>" + ls);
	          sb.append("      <cmp01>" + ls);
	          sb.append("        <e01_1001>707</e01_1001>" + ls);
	          sb.append("      </cmp01>" + ls);
	          sb.append("     <e01_1004>" + ordnr + "</e01_1004>" + ls);
	          sb.append("   </BGM>" + ls);
	          sb.append("      <DTM>" + ls);
	          sb.append("          <cmp01>" + ls);
	          sb.append("            <e01_2005>2</e01_2005>" + ls);
	          sb.append("            <e02_2380>" + escape(getDateString(cal)) + "</e02_2380>" + ls);
	          sb.append("            <e03_2379>101</e03_2379> " + ls);
	          sb.append("         </cmp01>" + ls);
	          sb.append("     </DTM>" + ls);
	          sb.append("     <g001>" + ls);
	          sb.append("       <RFF>" + ls);
	          sb.append("         <cmp01>" + ls);
	          sb.append("          <e01_1153>FRE</e01_1153> " + ls);
	          sb.append("          <e02_1154>K60</e02_1154>" + ls);
	          sb.append("         </cmp01>" + ls);
	          sb.append("       </RFF>" + ls);
	          sb.append("        <RFF>" + ls);
	          sb.append("         <cmp01>" + ls);
	          sb.append("          <e01_1153>PRE</e01_1153> " + ls);
	          sb.append("          <e02_1154>REKLAM�CIJA</e02_1154>" + ls); 
	          sb.append("         </cmp01>" + ls);
	          sb.append("        </RFF>" + ls);
	          sb.append("     </g001>" + ls);            
	          sb.append("   <g002>" + ls);
	          sb.append("     <NAD>" + ls);
	          sb.append("     <e01_3035>BY</e01_3035>" + ls);
	          sb.append("      <cmp01>" + ls);
	          sb.append("         <e01_3039>" + escape(custid) + "</e01_3039>");
	          sb.append("      </cmp01>" + ls);
	          sb.append("     </NAD>" + ls);
	          sb.append("   </g002>" + ls);
	      
	  		
	  		for(int i = 0; i < lines.size() ; i++){
	  			DefectiveLineWrapperMeri wr =(DefectiveLineWrapperMeri)lines.elementAt(i);
	  			 String text = wr.getLine().getDefectText();
	  			 String code = text.substring(0,3);
	  			 String bb = wr.getLine().getBestBefore();
	              sb.append("   <g025>" + ls);
	              sb.append("      <LIN>" + ls);
	              sb.append("         <e01_1082>1</e01_1082> " + ls);
	              sb.append("          <cmp01>" + ls);
	              sb.append("             <e01_7140>" + escape(wr.getLine().getProdID()) + "</e01_7140>" + ls);
	              sb.append("             <e02_7143>SA</e02_7143> " + ls);
	              sb.append("          </cmp01>" + ls);
	              sb.append("      </LIN>" + ls);
	              sb.append("      <PIA>" + ls);
	              sb.append("       <e01_4347>1</e01_4347>" + ls);
	              sb.append("        <cmp01>" + ls);
	              sb.append("           <e01_7140>" + bb + "</e01_7140>" + ls);
	              sb.append("           <e02_7143>NB</e02_7143>" + ls);
	              sb.append("        </cmp01>" + ls);
	              sb.append("    </PIA>" + ls);
	              sb.append("       <QTY>" + ls);
	              sb.append("          <cmp01>" + ls);
	              sb.append("          <e01_6063>21</e01_6063>" + ls);
	             
	              double amount = Double.parseDouble(wr.getLine().getAmount());
	              if(amount > 0){
	            	  amount = amount * -1;
	              }
	             
	              sb.append("            <e02_6060>" + (amount) + "</e02_6060> " + ls);
	              sb.append("          </cmp01>" + ls);
	              sb.append("      </QTY>" + ls);
	              sb.append("   <g028>" + ls);
	              sb.append("      <RFF>" + ls);
	              sb.append("       <cmp01>" + ls);
	              sb.append("         <e01_1153>RS</e01_1153>" + ls);
	              sb.append("        <e02_1154>" + code + "</e02_1154>" + ls);
	              sb.append("        <e03_1156></e03_1156>" + ls);
	              sb.append("      </cmp01>" + ls);
	              sb.append("     </RFF>" + ls);
	              sb.append("  </g028>" + ls);  
	              
	              sb.append("      <DTM>" + ls);
	              sb.append("          <cmp01>" + ls);
	              sb.append("            <e01_2005>2</e01_2005>" + ls);
	              sb.append("            <e02_2380>"  + escape(getDateString(cal))  +  "</e02_2380>" + ls);

	              sb.append("            <e03_2379>101</e03_2379> " + ls);
	              sb.append("         </cmp01>" + ls);
	              sb.append("     </DTM>" + ls);
	              sb.append("   </g025>" + ls);

	  		}

	          sb.append("</ORDERS>" + ls);
	          sb.append("</Body>" + ls);
	          sb.append("</Envelope>" + ls);
	          

	          try{
	        	  String fileName2 = parent.getProps().getProperty("conv.dir3")  + agent
			    	+ "." + ordnr +  ".xml";
	        	  Writer out = new BufferedWriter(new OutputStreamWriter(
	        			    new FileOutputStream(fileName2), "UTF-8"));
	        			try {
	        			    out.write(sb.toString());
	        			} finally {
	        			    out.close();
	        			}
			  
		            	
			    }catch(Exception e){
			    	e.printStackTrace();
			    }
			  
			  
	  	}	
	  }


   
    private String escape(String s){
        if (s == null) return s;
        StringBuffer sb = new StringBuffer();
        for (int i = 0 ; i < s.length() ; i++){
          char c = s.charAt(i);
          if (c == '&'){
            sb.append("&amp;");
          } else if (c == '<'){
            sb.append("&lt;");
          } else if (c == '>'){
            sb.append("&gt;");
          } else if (c == '\n'){
            sb.append(" ");
          } else {
            sb.append(c);
          }
        }
        return sb.toString();
      }



    private String getDateString(Calendar cal){
      StringBuffer sb = new StringBuffer();
      String year =   cal.get(Calendar.YEAR) + "";
      sb.append(year.substring(2,4));
      String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
      if (month.length() == 1){
        sb.append("0");
      }
      sb.append(month);

      String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
      if (day.length() == 1){
        sb.append("0");
      }
      sb.append(day);

      return sb.toString();
    }
    
    public void insertDefective(DefectiveWrapper wr){}


    public void insertDistribution(DistEvalListWrapper wr){}


	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){}

	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	        String sql = "INSERT into cesu.mj_supervisor values ("
	          + "?, "   //   agent	C(50),
	          + "?,"   //   agentname	C(200),
	          + "?,"   //   clientid	C(50),
	          + "?,"   //   clientname	C(200),
	          + "?,"   //	prodid	C(50),
	          + "?,"   //   prodname	C(200),
	          + "date() ,"   //	sentdate	D,
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

	          try {	Connection c = DriverManager.getConnection(url2);
	              PreparedStatement ps = c.prepareStatement(sql);

	              for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
	            	  eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
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
	                ps.setString(24, lw.getLine().getCustComment());
	                ps.setString(25, lw.getLine().getComment());
	                ps.setString(26, lw.getLine().getCustAgentId());
	                ps.setString(27, lw.getLine().getCustAgentName());
	                ps.setInt(28, lw.getLine().getDealNr());
	                ps.setInt(29, lw.getLine().getIdealNr());
	                ps.setString(30, lw.getLine().getResStr1());
	                ps.setString(31, lw.getLine().getResStr2());
	                ps.setString(32, lw.getLine().getResStr3());
	                ps.setInt(33, lw.getLine().getResInt1());
	                ps.setInt(34, lw.getLine().getResInt2());
	                ps.setInt(35, lw.getLine().getResInt3());
	                ps.executeUpdate();
	              }

	              ps.close();
	              c.close();
	          } catch (SQLException e) {
	              e.printStackTrace();
	          }
	        }


    private Date getDate(String kuup) {

      Calendar cal = Calendar.getInstance();
      int YEAR = Integer.parseInt(kuup.substring(0, 4));
      int MONTH = Integer.parseInt(kuup.substring(4, 6)) - 1;
      int DAY = Integer.parseInt(kuup.substring(6, 8));
      cal.set(YEAR, MONTH, DAY, 1, 2, 3);
      Date now = new Date(cal.getTimeInMillis());
      return now;
    }


    public String getStartingDateStr(){
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH,
            -(Integer.parseInt(parent.getParentFrame().getProps().getProperty("months")) + 1));
    String month = (cal.get(Calendar.MONTH)+1) +"";
    String day = (cal.get(Calendar.DAY_OF_MONTH)) +"";
         if(month.length() == 1){month = "0" + month;}
         if(day.length() == 1){day = "0" + day;}
         return cal.get(Calendar.YEAR) + "" + month + day;
     }


        private String getYearMonth() {
          Calendar cal = Calendar.getInstance();
          String month = (cal.get(Calendar.MONTH)+1) +"";
          if(month.length() == 1){month = "0" + month;}
          return cal.get(Calendar.YEAR) + "" + month;
        }

        
      private String getToday() {
        	
            Calendar cal = Calendar.getInstance();
            String month = (cal.get(Calendar.MONTH)+1) +"";
            if(month.length() == 1){month = "0" + month;}
            String day = cal.get(Calendar.DAY_OF_MONTH)+"";
            if(day.length()==1){
            	day = "0" + day;
            }
            return cal.get(Calendar.YEAR) + "" + month + "" + day;
          }
        private String getYearMonthDay(Date dd) {
        	
            Calendar cal = Calendar.getInstance();
            cal.setTime(dd);
            String month = (cal.get(Calendar.MONTH)+1) +"";
            if(month.length() == 1){month = "0" + month;}
            String day = cal.get(Calendar.DAY_OF_MONTH)+"";
            if(day.length()==1){
            	day = "0" + day;
            }
            return cal.get(Calendar.YEAR) + "" + month + "" + day;
          }


    public TreeMap getDealers() {
      return dealers;
    }

    public void setDealers(TreeMap aDealers) {
      dealers = aDealers;
    }

    public NumberFormat getNF() {
      return NF;
    }

    public void setNF(NumberFormat aNF) {
      NF = aNF;
    }


//  private String url = "jdbc:odbc:M3LVPROD";
  private Connection con;
  private Statement stmt;
  private String url2 = "jdbc:odbc:report";
  private String subjectFilesTo = null;
  private NumberFormat NF = NumberFormat.getInstance();
  private TreeMap  dealers = new TreeMap();
  
  
  private void makeTaskFile(String dir){
		 try{
			 
			  String sql2 = "select lineid ,taskid,agentid,agentnam,clid, sentdate,senttime,reminder,photoname," +
			  		"status,mestext,headertext,res1 from cesu.mj_task where sentdate > sysdate-45 order by clid,sentdate";
			  
			  
			  
		Connection con2 = this.getNewConnection();
		    Statement stt = con2.createStatement();
		ResultSet rr = stt.executeQuery(sql2);
		StringBuffer sb = new StringBuffer ();
		sb.append("custid;kell;agentname;agentcode;header;message;answerid;messageid;reminder;fotoloc;done\n");
		while (rr.next()){
			String lineid =   rr.getString("lineid");
			String taskid =   rr.getString("taskid");
			String agentid =  rr.getString("agentid");
			String agentnam = rr.getString("agentnam");
			String clid =     rr.getString("clid");	
			String sentdate = rr.getString("sentdate");
			String senttime = rr.getString("senttime");
			Date reminder =   rr.getDate("reminder");
			String photoname = rr.getString("photoname");
			String status = rr.getString("status");
			String mestext =  rr.getString("mestext");
			String headertext = rr.getString("headertext");
			String donetime = rr.getString("res1");
		     
			long lll = 0;
			boolean goes = true;
	        if(donetime != null && donetime.trim().length() > 0){
	        	try{
	        		lll = Long.parseLong(donetime.trim());
	        		if(Calendar.getInstance().getTimeInMillis() - 518400000  > lll){
	        			goes = false;
	        		}
	        	}catch(Exception e){
	        		
	        	}
	        }

			if(clid == null || clid.trim().length() == 0){
				clid = " ";
			}else{
				clid = clid.trim();
			}
			
			if(lineid == null || lineid.trim().length() == 0){
				lineid = " ";
			}else{
				lineid = lineid.trim();
			}
			
			if(taskid == null || taskid.trim().length() == 0){
				taskid = " ";
			}else{
				taskid = taskid.trim();
			}
			
			if(senttime == null || senttime.trim().length() == 0){
				senttime = " ";
			}else{
				senttime = senttime.trim();
			}
			
			
			if(photoname == null || photoname.trim().length() == 0){
				photoname = " ";
			}else{
				photoname = photoname.trim();
			}
			
			if(mestext == null || mestext.trim().length() == 0){
				mestext = " ";
			}else{
				mestext = mestext.trim();
			}
			
			
			if(headertext == null || headertext.trim().length() == 0){
				headertext = " ";
			}else{
				headertext = headertext.trim();
			}
			
			String remi = "0";
			if(reminder != null){
				remi = (reminder.getTime() + 54000000) + "";
			}
			if(goes){
				sb.append(clid + ";" + sentdate.substring(0,16) + ";" + agentnam + ";" +agentid + ";" +
						mestext.replaceAll(";", ":").replaceAll("\\r\\n|\\r|\\n", "|")	+ ";" + headertext.replaceAll(";", ":").replaceAll("\\r\\n|\\r|\\n", "|") + ";" + lineid + ";" + taskid + ";" + remi + ";" + photoname+ ";"
						  + status + "\n");
//					Header;kell;agentname;agentcode;         header                   ;message;answerid;messageid;reminder;fotoloc;done
//				10687;2013-03-31 12.30;Aigars Dzentis;AAA;teha distants v�ljapanek;testing message 1;123; ;1368474905373;IMGP5854.JPG;0 
				
			}

				

		}
		rr.close();
		stt.close();
		con2.close();


		  
		    File f = new File( "customertask.txt");
		    if (f.exists())f.delete();
		      
		    Converter.saveFileInUTF8("customertask.txt", sb);
			if( dir != null){
				 f = new File(dir + "customertask.txt");
				    if (f.exists())f.delete();
				 Converter.saveFileInUTF8(dir + "customertask.txt", sb);
			}
			  
		 }catch(Exception e){
			 e.printStackTrace();
		 }
	 }


  private void makeNormpict(Vector cats,String pictspda , String catal){
  	if(cats.size() > 0){
  		for (int e = 0; e < cats.size(); e++){
  			
  		String pictssou = catal  + (String) cats.elementAt(e) + "\\";
  //		System.out.println("cat name "  + pictssou );
  		 File res2 = new File (pictssou);
  		    String[] ff2 = res2.list();
  			 for(int i = 0; ff2.length > i ; i++){
  				 
  				File sou = new File ( pictssou + ff2[i]); 
  				//System.out.println();
  				if(ff2[i].indexOf("_") != -1 ){
  					String toode = ff2[i].substring(0,ff2[i].indexOf("_"));
  					if(prodean.get(toode) != null){
  						Vector vv = (Vector)prodean.get(toode);
  						for(int w =0;w<vv.size();w++){
  							File out = new File ( pictspda + vv.elementAt(w) + ".jpg");	
  							try {
  								copyFile(sou,out);
  							} catch (Exception u) {
  								u.printStackTrace();
  					    	}	
  						}
  							
  					}
  			   }
  		   }
  	    }
  	}else{
  		String pictssou = catal;
  		//System.out.println("cat name "  + pictssou );
  		 File res2 = new File (pictssou);
  		    String[] ff2 = res2.list();
  			 for(int i = 0; ff2.length > i ; i++){
  				 
  				File sou = new File ( pictssou + ff2[i]); 
  				//System.out.println();
  				if(ff2[i].indexOf("_") != -1 ){
  					String toode = ff2[i].substring(0,ff2[i].indexOf("_"));
  					
  					File out = new File ( pictspda + toode + ".jpg");	
						try {
							copyFile(sou,out);
						} catch (Exception u) {
							u.printStackTrace();
				    	}	
  			   }
  		   }
  	}
  	 
  }


  Hashtable prodean = new Hashtable(); 

	
  private void makePictureCatuus(){
  	    
    	
    	String pictssou = parent.getProps().getProperty("pict.dir");
  	    String pictspda = parent.getProps().getProperty("pictpda.dir");
       
  	 try {
     	  
          File f = new File(pictspda +"list.txt");
          if (f.exists())f.delete();
          //Kataloogi sisude kustutamine
      	 	 File res2 = new File (pictspda);
             String[] ff2 = res2.list();
         	 for(int i = 0;ff2.length>i;i++){
             	(new File ( pictspda + ff2[i])).delete(); 
             }
//         	 L�ppes kustutamine 
  		 
  		 TreeMap prods = this.getProducts(null);

         StringBuffer buf = new StringBuffer();
         Iterator enume = prods.keySet().iterator();
    
         while (enume.hasNext()) {
    	    	String asi = (String)enume.next();
    	    	Product prod = (Product)prods.get(asi);
    	    	String toode = prod.getEAN().trim();  
               if(toode.length() > 0){
                	
    	   	  res2 = new File (pictssou);
              ff2 = res2.list();
             
         	 for(int i = 0;ff2.length>i;i++){
             	if( ff2[i].startsWith(toode)){
               // System.out.println("pilt olemas " + ff2[i]);
            	File in = new File ( pictssou + ff2[i]); 
              	if(in.isFile()){
              	File inpda = new File ( pictspda + prod.getID()+ ".jpg"); 
                  	Converter.shrinkAndCopyFile(in,inpda,400);
            	    if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
                    		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
                    	}
                   }
                 }
               }
             }
    	 }
         try {
     		PrintWriter os = new PrintWriter(new FileWriter(pictspda + "list.txt", true));
     		os.write(buf.toString());
     		os.close();
     	  }catch (Exception ex) {
     		ex.printStackTrace();
     	  }
  	 
  	 } catch (Exception e) {
    	    getParent().showError(e);
    	}
    
  	 
  	 
  }
    

  
  
  
  
@Override
public void PictureMaker() {
    
    String cmd = "SELECT kood , ean FROM CESU.MJDAILYITEMS" ;
  		     	String picts = parent.getProps().getProperty("pict.dir");
		     	String pictspda = parent.getProps().getProperty("pictpda.dir");
		 	    String pictssource = parent.getProps().getProperty("pictsource.dir");

		   	try {
  		 	   Connection con = getNewConnection();
		   		Statement stmt = con.createStatement();
		   	    ResultSet	      rs = stmt.executeQuery(cmd);

		   	  while (rs.next()) {
		   		  String kood = rs.getString("kood").trim();
		   		 String ean = rs.getString("ean");
		   		 if(ean == null || ean.trim().length() == 0){
		   			 ean = kood;
		   		 }
		   		 if(ean != null && ean.trim().length() >0){
		   			 if(prodean.get(ean.trim()) != null){
		   				 Vector vv = (Vector)prodean.get(ean.trim());
		   				 vv.add(kood);
		   				prodean.put(ean.trim(),vv);
		   			 }else{
		   				 Vector vv = new Vector();
		   				 vv.add(kood);
		   				prodean.put(ean.trim(),vv); 
		   			 }
		   			
			   		   
		   		 }
		   		  
		   	  }
		        StringBuffer buf = new StringBuffer();
		        StringBuffer buf0 = new StringBuffer();
		        int a = 0;
		        int b = 0;
		        int c = 0;
		        
		        // leiame kataloogid ja peame meeles 
		        Vector catse = new Vector();
		        File d  = new File(picts);
		        File[] dd = d.listFiles();
		        
		        for(int i = 0; i < dd.length ; i++){
		        	File t = dd[i];
		        	if(t.isDirectory()){
		        		catse.add(t.getName());
		        	}
		        }
		       // System.out.println("Piltide leidmine - katalooge sees " + catse.size() + " " + catse.toString());
		        
		        this.makeNormpict(catse, pictssource,picts);
		        
		        rs = stmt.executeQuery(cmd);
		   	    while (rs.next()) {
		   	    	a++;
		   	    	c++;
		   	    	
		   	     String toode = rs.getString("kood").trim();  
		         toode = toode.replace('/', '_');
		         toode = toode.replace('/', '_');
		   	    	if(c == 100){
		   	    		  c = 0;
		   	    		//  System.out.println("Piltide leidmine "  + a);	
		   	    		  
		   	    	}
		   	     
		              
		                 File f = new File(pictssource + toode+".jpg");
		                 String len = "0";
		                 if(f.exists()){
		                   len =	f.length() + "";
		                 }
		                 
		                 	buf.append(toode+".jpg;"  + len + "\n");
		           	    	b++;
		                 
		                 }
		   	    rs.close();
		   	    stmt.close();
		   	    con.close();
		   	    //oleasoleva kataloogi kustutamine
		   //	  System.out.println("Kataloogi kustutamine "  + a);	    
		               
		           	     
		            
		   //Kataloogi sisude kustutamine
		             	  
		  
		          	 
		          	 
		        	 File res2 = new File (pictspda);
		            String[] ff2 = res2.list();
		        	 for(int i = 0;ff2.length>i;i++){
		            //	System.out.println("fff " + ff2[i]);
		            	(new File ( pictspda + ff2[i])).delete(); 
		         
		            }
//		        	 L�ppes kustutamine 
		        	 
		          System.out.println("Piltide kopeerimine ");	
		          //Failide kopeerimine  
		            String[] gg = new File(pictssource).list();
		         	  //System.out.println("gg " + gg[0] + " " + gg.length);
		            
		             for(int i = 0;gg.length>i;i++){          	
		             	File in = new File ( pictssource + gg[i]); 
		             	if(in.isFile() && gg[i].indexOf("umbs") == -1){            	
		             	File inpda = new File ( pictspda + gg[i]); 
		                   	copyFile(in,inpda);
		             	}
		             }
		             
		             File f = new File(pictspda + "list.txt");
		               if (f.exists())f.delete();
		               
		                 try {
		           		PrintWriter os = new PrintWriter(new FileWriter(
		           				pictspda + "list.txt", true));
		           		os.write(buf.toString());
		           		os.close();
		           	//	System.out.println("buff " + f.exists() + " " + buf.toString());
		           	  }catch (Exception ex) {
		           		ex.printStackTrace();
		           	  }
		             	
		       	 
		   	} catch (Exception e) {
		   	    getParent().showError(e);
		   	}
		   System.exit(0);

	
}

public void copyFile(File in, File out) throws Exception {
    FileInputStream fis  = new FileInputStream(in);
    FileOutputStream fos = new FileOutputStream(out);
    byte[] buf = new byte[1024];
    int i = 0;
    while((i=fis.read(buf))!=-1) {
      fos.write(buf, 0, i);
      }
    fis.close();
    fos.close();
    }

private Hashtable prices = null;


public Vector getPriceMatrixData(){

//hinnad maksja p�hised 
	String sql6 = "SELECT '' AS CUSTID, PYNO AS CLGROUP, ITNO AS PRODID, '' AS PRODGROUP, " +
	"Round((PRICE * ((100-DISCOUNT)/100)+ EXCISE),2) AS PRICE, 0 AS DISC, INFO, TODT AS PAEV, FROMDT AS STARTDAY, COMM " +
	" FROM CESU.DISCOUNTS_VIEW " +
	" WHERE CUNO IS NULL OR CUNO = PYNO " +                                              
	" ORDER BY PYNO, CUNO, ITNO" ;
	
	 //kampaaniate kysimine
    

    String sql4 = " SELECT CUNO AS CUSTID, '' AS CLGROUP, ITNO AS PRODID, '' AS PRODGROUP,  " +
    " Round((PRICE * ((100-DISCOUNT)/100)+ EXCISE),2) AS PRICE, 0 AS DISC, INFO,TODT AS PAEV, FROMDT AS STARTDAY ,COMM " +
    " FROM CESU.DISCOUNTS_VIEW " +
    " WHERE CUNO IS NOT NULL AND CUNO != PYNO  " +                                              
    " ORDER BY PYNO, CUNO, ITNO " ;
    
    
    
 // kliendip�histe hindade kysimine mis on erinevad maksja omadest 
    
    String  sql5 = "SELECT CUNO AS CUSTID, '' AS CLGROUP, ITNO AS PRODID, '' AS PRODGROUP, " +
    		"MAX((PRICE * ((100-DISCOUNT)/100)) + EXCISE) AS PRICE, 0 AS DISC, TODT AS PAEV, FROMDT AS STARTDAY , COMM " +
    		" FROM CESU.DISCOUNTS_VIEW T " +
        "WHERE CUNO IS NOT NULL AND CUNO != PYNO GROUP BY T.CUNO, T.ITNO, FROMDT, TODT, comm " +
        " ORDER BY CUNO, ITNO";
    
    

  Vector result = new Vector();
  HashSet olemas = new HashSet();
    try {
    	
 	   Connection con = getNewConnection();
	        Statement stmt = con.createStatement();
	    /*    ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {

        String custID = "";//Converter.enCodeEst(rs.getString("custid").trim());
        String clGroup = rs.getString("clgroup");
        String prodID = rs.getString("prodid").trim();
        
        String prodGroup = rs.getString("prodgroup");
       
        double price = rs.getDouble("price");
        double disc = rs.getDouble("disc");
        String info = rs.getString("info");
        String date = rs.getString("paev");
        String sdate = rs.getString("startday");

        String comm = rs.getString("COMM");
        String komment = "";
        if(comm != null && comm.trim().length() > 0){
            StringTokenizer st = new StringTokenizer(comm,"|");
                	while(st.hasMoreTokens()){
                		String token = st.nextToken();
                		if(token.trim().length()> 0 && !komment.contains(token)){
                			komment = komment + token + "|";
                        }
                }
        }
    
        MatrixLine mline = new MatrixLine();

        if (custID != null && custID.length() > 0) {
          mline.setCustID(custID);
        }
        
        if (info !=null && info.trim().length() > 0) {
            mline.setLineinfo(Converter.cleanLitLatEst(info.trim()));
          }
        
        if (komment.trim().length() > 0) {
        	if(mline.getLineinfo() != null){
        		  mline.setLineinfo(mline.getLineinfo() + "|" +Converter.cleanLitLatEst(komment.trim()));
        	 }else{
        		  mline.setLineinfo(Converter.cleanLitLatEst(komment.trim()));
            }
          }
        
        
        if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
          mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
        }

      
        if(date != null && !date.startsWith("1753")){
       	// mline.setEndDate(date.substring(0,10));
        }
        if(sdate != null  && !sdate.startsWith("1753")){
         	 if(mline.getEndDate() == null){
         	 }
          }
        if (prodID != null && prodID.length() > 0) {
          mline.setProdID(prodID);
        }
        if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
          mline.setProdGroup(prodGroup.trim());
          mline.setDiscGroup(prodGroup.trim());
        }

        if (price != 0) {
          mline.setPrice(price);

        }
        if (disc != 0) {
            mline.setDiscount(disc);

          }
      //  mline.setProdLevel("pr"); // paneme long listi 
        if( mline.getPrice() > 0 || mline.getDiscount() > 0){
        	olemas.add(clGroup.trim()+prodID.trim());
            result.add(mline);    
        }
        
      }
      rs.close();
      
     */

       ResultSet rs = stmt.executeQuery(sql4);
      while (rs.next()) {

        String custID =  Converter.enCodeEst(rs.getString("custid").trim());
        String clGroup = rs.getString("clgroup");
        String prodID = rs.getString("prodid").trim();
        
        String prodGroup = rs.getString("prodgroup");
       
        double price = rs.getDouble("price");
        double disc = rs.getDouble("disc");
        String info = rs.getString("info");
        String date = rs.getString("paev");
        String sdate = rs.getString("startday");
        String comm = rs.getString("COMM");
        String komment = "";
        if(comm != null && comm.trim().length() > 0){
            StringTokenizer st = new StringTokenizer(comm,"|");
                	while(st.hasMoreTokens()){
                		String token = st.nextToken();
                		if(token.trim().length()> 0 && !komment.contains(token)){
                			komment = komment + token + "|";
                        }
                }
        }

        
       boolean goes = false;
        if(prices.get(prodID) != null){
        	goes = true;
        	double pr = Double.parseDouble((String)prices.get(prodID));
        	if(pr == price){
        		goes = false;
        	}
        }
        MatrixLine mline = new MatrixLine();

        if (info !=null && info.trim().length() > 0) {
            mline.setLineinfo(Converter.cleanLitLatEst(info.trim()));
          }
        
        if (komment.trim().length() > 0) {
        	if(mline.getLineinfo() != null){
        		  mline.setLineinfo(mline.getLineinfo() + "|" +Converter.cleanLitLatEst(komment.trim()));
        	 }else{
        		  mline.setLineinfo(Converter.cleanLitLatEst(komment.trim()));
            }
          }
        
     
        
        if (custID != null && custID.length() > 0) {
          mline.setCustID(custID);
        }
      
        
        if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
          mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
        }

      
        if(date != null && !date.startsWith("1753")){
       	// mline.setEndDate(date);
        }
        if(sdate != null  && !sdate.startsWith("1753")){
         //	 mline.setStartDate(sdate);
         	 if(mline.getEndDate() == null){
         	//	 mline.setStartDate(null);
         	 }
          }
        if (prodID != null && prodID.length() > 0) {
          mline.setProdID(prodID);
        }
        if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
          mline.setProdGroup(prodGroup.trim());
          mline.setDiscGroup(prodGroup.trim());
        }

        if (price != 0) {
          mline.setPrice(price);
        }
        
        if (disc != 0) {
            mline.setDiscount(disc);
          }
        
        if( goes && mline.getPrice() > 0 || mline.getDiscount() > 0){
        	if(!olemas.contains(custID.trim()+prodID.trim())){
        		result.add(mline);    
                olemas.add(custID.trim()+prodID.trim());	
        	}
            
        }
      }
      
      
      
      
      
      
    rs = stmt.executeQuery(sql5);
   while (rs.next()) {

     String custID = rs.getString("custid").trim();// Converter.enCodeEst(rs.getString("custid").trim());
     String prodID = rs.getString("prodid").trim();   
     double price = rs.getDouble("price");
     String comm = rs.getString("COMM");
     String komment = "";
     if(comm != null && comm.trim().length() > 0){
         StringTokenizer st = new StringTokenizer(comm,"|");
             	while(st.hasMoreTokens()){
             		String token = st.nextToken();
             		if(token.trim().length()> 0 && !komment.contains(token)){
             			komment = komment + token + "|";
                     }
             }
     }

    
    boolean goes = true;
 
     MatrixLine mline = new MatrixLine();

     if (custID != null && custID.length() > 0) {
       mline.setCustID(custID);
     }
      
     if (prodID != null && prodID.length() > 0) {
       mline.setProdID(prodID);
     }
   
     if (price != 0) {
       mline.setPrice(price);
     }
    
     
     if (komment.trim().length() > 0) {
     	if(mline.getLineinfo() != null){
     		  mline.setLineinfo(mline.getLineinfo() + "|" +Converter.cleanLitLatEst(komment.trim()));
     	 }else{
     		  mline.setLineinfo(Converter.cleanLitLatEst(komment.trim()));
         }
       }
     
  
     
     if( goes && mline.getPrice() > 0 ){
    	 if(!olemas.contains(custID+prodID)){
     		result.add(mline);    
             olemas.add(custID+prodID);	
     	}
     }
   }
      
      
      stmt.close();
      con.close();
    }catch(Exception e){
   	 e.printStackTrace();
    }
	
return result;
 }



@Override
public Vector getFixedAssortmentData() {
	
	 Vector lines = new Vector();

    try {
   	  String cmd = " SELECT * FROM cesu.mjhorecasortment";

   	
   	System.out.println(cmd);
   	 Connection cc = getNewConnection();
  	        Statement stmt = cc.createStatement();
  	        ResultSet rs = stmt.executeQuery(cmd);
  	        while(rs.next()){
                     String custID = " ";
                     String clGroup = " ";
                     String prodID = " ";
                     String prodGroup = " ";
                     MatrixLine mline = new MatrixLine();

                     clGroup = "HORECAFIX";
                     prodID = rs.getString("ITEM");

                     if (clGroup != null
                         && clGroup.trim().length() > 0) {
                       mline.setCliGroup(clGroup.trim());
                      
                     }

                     if (prodID != null && prodID.trim().length() > 0) {
                       mline.setProdID(prodID.trim());
                     }
                                        
                     lines.add(mline);
                   }

  	       rs.close();
  	       con.close();
  	       cc.close();
                  } catch (Exception e) {
                      getParent().showError(e);
                  }
               
    return lines;
}

@Override
public String getProductSortingData() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Vector getLastThreeData() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public  Vector getCustomerProductsDistList() {
	
	String cmd =   "select client_id as saaja , product_id as toode from  CESU.MJ_DISTRIBUTION_MARKS ";

	    
	    if (isDebug())
	      System.out.println(cmd);

	    Vector custProds = new Vector();
	   
	    try {
	      Connection con = getNewConnection();
	      Statement st = con.createStatement();
	      ResultSet rs = st.executeQuery(cmd);
	      int co = 0;
	       while (rs.next()) {
	    	   String kliendikood = rs.getString("saaja").trim();
	           String tootekood = rs.getString("toode").trim();
	           if(co < 100)
	      // System.out.println( kliendikood + " " + tootekood);
	           co++;
	      CustProd cp = new CustProd(kliendikood, tootekood, 0, 0, 0," ",0,0,0," ");
	      custProds.add(cp);
            }
	      rs.close();
	      st.close();
	      con.close();
	   //   System.out.println("custProdDist suurus " + custProds.size());
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	      getParent().showError(e);
	    }
       
	    return custProds;

}


}
