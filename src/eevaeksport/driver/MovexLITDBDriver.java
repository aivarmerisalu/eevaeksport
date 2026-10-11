package eevaeksport.driver;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
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

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class MovexLITDBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater,Discounter{
  public MovexLITDBDriver(EksportPanel parent){
    super(parent);
    if(parent.getParentFrame().isDaySales()){
         this.subjectFilesTo = getParent().getString("senddatamsg");
    } else {this.subjectFilesTo = parent.getString("filemsg");}
    String databaseDriverName = "oracle.jdbc.driver.OracleDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection("jdbc:oracle:thin:@10.10.10.55:1522:MVXEE002", getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));;
      stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }

          public String getDealerCustomerCSV(String agent){
           return null;
           }

  /**
   *
   */
  public void connectionUp(){
  }

  public String getAssortmentStr(Agent agent){
   return null;
      }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

  public String getSaveDebtInfo() {
    String cmd = "SELECT OKCUNO AS klient , OKBLCD AS blokk " +
		   " FROM mvxjdta.OCUSMA k,mvxjdta.ddrpro WHERE k.OKSTAT ='20' AND k.OKCUTP='0' AND  OKCUNO =diedes(+) " +
		   " and OKBLCD=2";


		String cmd3 = "SELECT trim(ESCUNO) AS klient,round(SUM(ESCUAM),0) AS debt FROM mvxjdta.FSLEDG WHERE " +
		" ESRECO = 0  AND round(SYSDATE -to_date(to_char(ESDUDT),'YYYYMMDD'))>0 and ESCUAM >0" +
		" GROUP BY ESCUNO ORDER BY ESCUNO ";

		String cmd4 = "SELECT trim(ESCUNO) AS klient,round(SUM(ESCUAM),0) AS debt FROM mvxjdta.FSLEDG WHERE " +
		" ESRECO = 0  AND ESCUAM < 0" +
		" GROUP BY ESCUNO ORDER BY ESCUNO ";

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

		//Siin lahutatakse maha negatiivsed arve saldod
		rs = st.executeQuery(cmd4);
		while (rs.next()) {

			String kood = rs.getString("klient").trim();
			double volg = rs.getDouble("debt");

			Customer custw = (Customer)custs.get(kood);
			if(custw != null){
			custw.setDebt(custw.getDebt() + volg);
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
 // System.out.println("ehee " + products.toString() );
		return products.toString();

   }

  public String getSaveStockInfo(){

    String  cmd = "SELECT  l.MBITNO AS toode,l.MBSTQT-l.MBREQT AS STOCK , l.MBWHLO AS LADU " +
               " from mvxjdta.MITBAL l, mvxjdta.MITMAS m WHERE l.MBITNO=m.MMITNO AND l.MBWHLO='600'  " +
             " AND m.MMITTY in ('601','605','606','609') AND m.MMSTAT<'21' AND m.MMCFI2 NOT IN (1,4) ORDER BY l.MBITNO" ;

      if (isDebug()) System.out.println(cmd);


      StringBuffer products = new StringBuffer();
      try {

        Connection con1 = getNewConnection();
        Statement st = con1.createStatement();
        ResultSet rs = st.executeQuery(cmd);

        while (rs.next()) {

          String kood = rs.getString("toode").trim();
        //  String dealer = rs.getString("LADU").trim();
              products.append(kood +  ";");
              products.append(" ;");
             // products.append(getParent().getNF().format(rs.getDouble("stock")) + ";" + dealer +  "\n");
             double kogus =  rs.getDouble("stock");
             if(kogus > 0){
                 products.append((int)(kogus) +   "; ; \n"); 
             }
          }
          rs.close();
      } catch (Exception e) {
          getParent().showError(e);
      }
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

  public TreeMap getAgents(){
    String cmd = "SELECT CTSTKY AS agent ,CTTX40 AS NIMI, SubStr(ctparm,9,10) AS region  FROM mvxjdta.CSYTAB WHERE CTSTCO='SMCD'  AND substr(CTSTKY,0,1) != '*' " +
         " ORDER BY CTSTKY";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
  aHash = new HashMap();
    agents1 =  new TreeMap();
    
    String agentStr = getParent().getProps().getProperty("servicemen");

	if(agentStr != null){

	    StringTokenizer st = new StringTokenizer(agentStr, ";");
	       while(st.hasMoreTokens()){
	       	Agent agent = new Agent(st.nextToken());

			String agentAGENTS = getParent().getProps().getProperty(agent.getID());
     	    StringTokenizer stag = new StringTokenizer(agentAGENTS, ";");
		    agent.setName(stag.nextToken());
		

		       while(stag.hasMoreTokens()){
                agent.addDubbler(stag.nextToken());
		       }
				agents.put(agent.getName(), agent);
				agents1.put(agent.getID(), agent);
	       }
	}
    
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = rs.getString("NIMI").trim();
          agent.setName(name);
          agent.setIsPilot(true);
          String region = rs.getString("region");
          if(region == null){
        	  region = "";
          }
          try{/*
        	 if(false){
        	  // if(!region.equals("")){
        		
        	  String cmd2 ="SELECT regioon, toode, ale FROM (SELECT dxobv1 AS regioon, dxobv2 AS toode, dxdisp AS ale FROM mvxjdta.ogdmtx " +                                                                                                            
" WHERE dxdisy='100' AND dxdipo='4' AND dxprex=' 1' AND dxcmpn!='*' AND dxdisp!='0' AND dxobv2 between '010101' AND '189999'" +
" AND dxfvdt IN (SELECT Max(dxfvdt) FROM mvxjdta.ogdmtx WHERE dxdisy='100' AND dxdipo='4' AND dxprex=' 1')" +
" UNION  SELECT d8obv1 AS regioon, d8obv2 AS toode, d8disp AS ale FROM mvxjdta.ogcmtx "+                                                             
" WHERE d8disy='100' AND d8dipo='4' AND d8prex=' 1' AND d8disp!='0' AND d8obv2 between '010101' AND '189999' AND " +
"   d8lvdt>='" + getDateStr() + "')  WHERE regioon = '" + region.trim() + "'" ;
        	  
        	 // System.out.print(cmd2);
        	  Statement s2 = con.createStatement();
        	  ResultSet rs2 = s2.executeQuery(cmd2); 
        	  while (rs2.next()){
        		  Toode tt = new Toode(rs2.getString("toode"));
        		  tt.setKogus(rs2.getDouble("ale"));
        		//  agent.addDiscountProds(tt);
        	  }
        	  //System.out.println("Discprods " + agent.getID() + " " + agent.getDiscountProds());
        	  
        	  }*/
          }catch(Exception r){
        	  r.printStackTrace();
          }
          
          
          
          Iterator enums = agents1.keySet().iterator();
          while (enums.hasNext()) {
           Agent a =(Agent) agents1.get((String)enums.next());
         //  System.out.println( "Esimene " + a.toString());
          // System.out.println( "Teine " + agent.getID());
           if(a.getDubbler()!= null && a.getDubbler().size()>0){
        	   Enumeration en = a.getDubbler().elements();
        	   while(en.hasMoreElements()){
        		   String aa = (String)en.nextElement();
        		   if(aa.equals(agent.getID())){
            		   agent.addDubbler(a.getID());
            		  
        		   }
        	   }
           }
         }
          
      
          agents.put(name, agent);
          aHash.put(agent.getID(), agent);
        }
 
    rs.close();
    
    
    

      
    con.close();

    } catch (Exception e){
        getParent().showError(e);
      }
    
	
  

    return agents;
  }


      public String getDistribution() {

        StringBuffer sb = new StringBuffer();

        try {
          Connection con = getNewConnection();
          Statement st = con.createStatement();
          String cmd = "select mmitno as kood ,mmitds AS Nimetus , mmdim1 from mvxjdta.mitmas where MMCFI5 = 'Y'";

          if (isDebug())
          System.out.println(cmd);
          ResultSet rs = st.executeQuery(cmd);
         while(rs.next()){
           sb.append(rs.getString("kood") + ";" + rs.getString("nimetus") + "\n");
         }
         st.close();
         con.close();
       } catch (Exception e){}
         System.out.println(sb.toString());
        return sb.toString();
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
      
      


      public TreeMap getCustomers(Agent agent){
    	    TreeMap customers = new TreeMap();

    	    try {
    	    	

    	        Connection con = getNewConnection();
    	        Statement st = con.createStatement();
    	        
    	       TreeMap debts = new TreeMap();
    	       TreeMap sales = new TreeMap();
    	       TreeMap sortimendid = new TreeMap();
    	       
    	        String cmd3 = "SELECT trim(ESPYNO) AS saaja,round(SUM(ESCUAM),0) AS SUMMA FROM mvxjdta.FSLEDG WHERE " +
    	        "ESRECO = 0  AND round(SYSDATE -to_date(to_char(ESDUDT),'YYYYMMDD'))>0  " +
    	        "  GROUP BY ESPYNO ORDER BY ESPYNO  ";
    	        Statement st1 = con.createStatement();
    	     if (isDebug()) System.out.println(cmd3);
    	        ResultSet rs1 = st1.executeQuery(cmd3);
    	         while (rs1.next()) {
    	        	 String cus = rs1.getString("saaja");
    	           double debt = Double.parseDouble(rs1.getString("SUMMA").trim());          
    	           debts.put(cus, debt + "");     
    	         } 
    	         rs1.close();
    	        
    	         
    	         
    	         String sql7 = "SELECT es.ESPYNO AS klient,SUM(CASE WHEN es.ESIVDT<='" + getToday() + "' THEN es.ESCUAM ELSE 0 END) AS balance FROM MVXJDTA.FSLEDG es, MVXJDTA.OCUSMA ok " +
    	         " WHERE es.ESPYNO=ok.OKCUNO AND es.ESRECO='0' AND es.ESCONO=200 AND ok.OKCONO=200 AND es.ESDIVI=700  " +
    	         " GROUP BY es.ESPYNO ORDER BY ESPYNO ASC";
    	         Statement st2 = con.createStatement();
    	         if (isDebug()) System.out.println(sql7);
    	            ResultSet rs2 = st2.executeQuery(sql7);
    	             while (rs2.next()) {
    	            	 String cus = rs2.getString("klient");
    	               double myyk = rs2.getDouble("balance");
    	               sales.put(cus, myyk+"");
    	                 
    	                
    	             }
    	             rs2.close();
    	         
    	         
    	    	String ss = " SELECT OCCUNO AS klient , OCASCD AS sortiment   " +
    	    			"FROM mvxjdta.OASCUS  WHERE OCASCD!='600'    ORDER BY OCCUNO,OCASCD , OCLMDT ";
    	    	
    	    	 Statement st3 = con.createStatement();
    	         if (isDebug()) System.out.println(ss);
    	            ResultSet rs3 = st3.executeQuery(ss);
    	             while (rs3.next()) {
    	               String cus = rs3.getString("klient");
    	               String sort = rs3.getString("sortiment");
    	               sortimendid.put(cus, sort);
    	             }
    	             rs2.close();
    	             

    	      String cmd2 = " SELECT OKCUNO AS kood ,to_char(OKCUNM) AS nimi,(OPCUA1 || ' '  || OPCUA2 || ' ' || OPCUA3 || ' ' || OPCUA4) AS aadress1, " +
    	        "   ' ' AS aadress2, OKCFC8 AS visit, ' ' AS info ,(SELECT OKCRL2 FROM mvxjdta.OCUSMA WHERE k.OKPYNO = OKCUNO) AS limiit, OKSMCD AS agent, OKPYNO AS payer,OKWHLO AS ladu,okcfc5 AS eiladu , " +
    	        "  (SELECT MAX(OKTOIN)FROM mvxjdta.OCUSMA WHERE OKPYNO=k.OKPYNO) AS debt,didesq AS ring, dirout AS ringiabi , OKBLCD AS blok2 ,OKCFC0 AS toall " +
    	       "  FROM mvxjdta.OCUSMA k,mvxjdta.ddrpro,mvxjdta.OASCUS abi, MVXJDTA.OCUSAD  WHERE OKCUNO=OPCUNO and  k.OKSTAT ='20' AND k.OKCUTP='0' AND  OKCUNO =OCCUNO(+) AND  OKCUNO =diedes(+) " +
    	       "   ORDER BY (case when length(to_char(didesq))<2 then '00'||to_char(didesq) else (case when length(to_char(didesq))<3 " +
    		   " then '0'||to_char(didesq) else to_char(didesq) end) end)||' '|| to_char(OKCUNM) ,dirout desc";


    	      if (isDebug()) System.out.println(cmd2);
    	      ResultSet rs = st.executeQuery(cmd2);
    	      HashMap custMap = new HashMap();

    	      while(rs.next()){
    	       // String licence = rs.getString("licence");
    	          String accountnum = rs.getString("kood").trim();
    	          Customer cust = new Customer(accountnum);
    	          String name = rs.getString("nimi").trim();

    	          String aadress1 = rs.getString("aadress1");
    	         int i= 6;
    	          if(aadress1 != null && aadress1.trim().length()<i){
    	            i = aadress1.trim().length();
    	          }

    	          String addr = aadress1.substring(0,i);
    	          cust.setName(name + " " + accountnum + " " + addr);

    	          if (aadress1 != null) {aadress1 = aadress1.trim();}
    	          String aadress2 = rs.getString("aadress2");
    	          if (aadress2 != null) {aadress2 = aadress2.trim();} else {aadress2 = " ";}
    	          cust.setAddress(aadress1 + " " + aadress2);
    	        //  cust.setLicence(licence);
    	          String visit = rs.getString("visit");
    	          if(visit != null){
    	            cust.setVisit(visit.trim());
    	          }
    	          String info = rs.getString("info");
    	          if (info != null){cust.setInfo(info.trim());}
    	          double limiit = rs.getDouble("limiit");
    	         // System.out.println(limiit);
    	          if (limiit != 0){cust.setCredit(limiit+ "");}
    	          String agent1 = rs.getString("agent").trim();

    	          if(agent1.length()>3){
    	        	  agent1 = agent1.substring(0,3);
    	          }

    	          cust.addAgent(new Agent(agent1));


    	          cust.setPayer(rs.getString("payer").trim());
    	          String  blokk = rs.getString("blok2") ;

    	          if(!blokk.trim().equals("0")){
    	          cust.setSalesBlock(true);
    	          }

    	          String allagents = rs.getString("toall");
    	            if(allagents.trim().equals("1")){
    	            Enumeration enums = getParent().getAgentsHash().elements();
    	             while (enums.hasMoreElements()) {
    	              Agent a =(Agent) enums.nextElement();
    	              cust.addAgent(a);
    	            }
    	            }
    	  // cust.setDebt(rs.getDouble("debt"));
    	            
    	            if(debts.get(cust.getPayer()) != null){
    	            	cust.setDebt(Double.parseDouble((String)debts.get(cust.getPayer())));
    	            }
    	            
    	            if(sortimendid.get(cust.getID()) != null){
    	            	cust.setGroup((String)sortimendid.get(cust.getID()));
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
    	            
    	            
    	          customers.put(cust.getName(), cust);
    	          custMap.put(cust.getID(), cust);
    	    }



      
    	         
    	         st.close();
    	         con.close();


    	    } catch (SQLException e){
    	      getParent().showError(e);
    	    }
    	    return customers;
    	  }

  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection("jdbc:oracle:thin:@10.10.10.55:1522:MVXEE002", getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "SELECT MMCFI1 AS liik ,MMCFI1 AS nimetus  FROM mvxjdta.MITMAS " + 
                       " WHERE TRIM(MMCFI1)not in(' ')GROUP BY MMCFI1";
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

    public TreeMap getProdGroupsSecond() throws SQLException {

      Connection con = getNewConnection();
      TreeMap   tm = new TreeMap();
      String    sql = "SELECT '02' AS ProdGroupAsID, '02' AS ProdGroupCode, 'Buteliai' AS GroupName, NULL AS ProdGroupParentAsID FROM dual union " +
" SELECT '03' AS ProdGroupAsID, '03' AS ProdGroupCode, 'Skarda' AS GroupName, NULL AS ProdGroupParentAsID FROM dual UNION  " +
" SELECT '04' AS ProdGroupAsID, '04' AS ProdGroupCode, 'PETai' AS GroupName, NULL AS ProdGroupParentAsID FROM dual UNION  " +
" SELECT '07' AS ProdGroupAsID, '07' AS ProdGroupCode, 'Multipakai' AS GroupName, NULL AS ProdGroupParentAsID FROM dual UNION " +
" SELECT '09' AS ProdGroupAsID, '09' AS ProdGroupCode, 'KEGai' AS GroupName, NULL AS ProdGroupParentAsID FROM dual";
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);

      while (rs.next()) {
          tm.put(rs.getString("ProdGroupCode").trim(),
                 rs.getString("GroupName").trim());
      }
      rs.close();
      st.close();
      con.close();

      return tm;
  }




  public TreeMap getProducts(Agent agent){

  	
  	Hashtable stocks = new Hashtable();
  	
  	//Leiame Laoseisud
  	 try {


        String  sql = " SELECT  MBITNO AS toode,MBSTQT-MBREQT AS STOCK  from mvxjdta.MITBAL l WHERE MBWHLO='600' ORDER BY MBITNO" ;

          if (isDebug()) System.out.println(sql);
             Connection con1 = getNewConnection();
             Statement stm = con1.createStatement();
             ResultSet rst = stm.executeQuery(sql);

               while (rst.next()) {
                 String toode = rst.getString("toode").trim();
                 int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                // String ladu  =  rst.getString("ladu").trim();
                 if(seis > 0){
                     stocks.put(toode,seis+""); 
                 }
                 //System.out.println(ladu+" "+toode+" "+seis+"");
             }
             rst.close();
             stm.close();
             con1.close();

     } catch (Exception e) {
         getParent().showError(e);
     }

    // Leiame soodused 
     
     Vector soodukad = new Vector();
     
     try {


         String  sql = " SELECT toode, ale FROM (SELECT dxobv1 AS toode, dxdisp AS ale FROM mvxjdta.ogdmtx  " +               
     " WHERE dxdisy='100' AND dxdipo='1' AND dxprex=' 3' AND dxcmpn!='*' AND dxdisp!='0'  " +
     " AND dxfvdt IN (SELECT Max(dxfvdt) FROM mvxjdta.ogdmtx WHERE dxdisy='100' AND dxdipo='1' AND dxprex=' 3') " +
     " UNION SELECT d8obv1 AS toode, d8disp AS ale FROM mvxjdta.ogcmtx " +                                                         
     " WHERE d8disy='100' AND d8dipo='1' AND d8prex='3' AND d8disp!='0' AND d8lvdt>='" + getDateStr() + "' )" ;

           if (isDebug()) System.out.println(sql);
              Connection con1 = getNewConnection();
              Statement stm = con1.createStatement();
              ResultSet rst = stm.executeQuery(sql);

                while (rst.next()) {
                  String toode = rst.getString("toode").trim();
                  //soodukad.add(toode);
                  System.out.println(toode+"  kampaanias");
              }
              rst.close();
              stm.close();
              con1.close();

      } catch (Exception e) {
          getParent().showError(e);
      }

  	
    String cmd =  "SELECT MMBUAR,t.MMITNO AS kood,(case when substr(t.MMITNO,1,1)='1' " +
       "   then substr(t.MMITNO,2,6) else t.MMITNO end) AS ABI, " +
       " t.MMITDS AS nimetus,t.MMCFI2 AS tykke , '' AS maxale , " +
       "   h.ODSAPR AS hind,        t.MMCFI1 AS grupp,        " +
       "    (CASE WHEN t.MMPRGP IN ('3PL', '5PL') AND t.MMITTY = '601' THEN '02' " +
       "   WHEN t.MMPRGP IN ('3PR','5PR','5LC','568') AND t.MMITTY = '601' THEN '03' " +
       "  WHEN t.MMPRGP IN ('03P', '04P', '05P', '07P', '15P', '20P', '21P', '7PL') AND t.MMITTY = '601' THEN '04' " + 
       "  WHEN t.MMPRGP IN ('02T', '10T') AND t.MMITTY = '601' THEN '07' " +
       "  WHEN t.MMPRGP IN ('3VT','5VT', '2VT') AND t.MMITTY = '601' THEN '09' " +
        "  ELSE NULL END ) AS grupp2,     " +
        "  t.MMVOL3 AS inunit, t.MMSPE4  AS onpallet ,(case when a.MPALWT=2 then a.MPPOPN else '' end)  AS ean ,t.MMVTCP AS vat,t.mmspe3 as unit  " +
        "  , t.MMITTY  AS id , t.MMNEWE AS palletspace ,t.MMGRP1 as aaa FROM mvxjdta.MITMAS t , mvxjdta.MITPOP a,mvxjdta.OPRBAS h " +
        "    WHERE  t.MMITNO=a.MPITNO(+)  AND MPALWT(+)='2' AND t.MMITNO=h.ODITNO    AND " +
        "   t.MMITTY  in  ('601','605','606','609') AND  t.MMSTAT=20  AND  ODPRRF ='R1' AND '" + getDateStr() + "'  BETWEEN ODFVDT and ODLVDT " +
        " ORDER BY mpitno,ODSAPR,(case when a.MPALWT=2 then a.MPPOPN else '' end)desc ";


    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {
      Connection con = getNewConnection();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(cmd);

      while(rs.next()){

      	String kood = rs.getString("kood").trim();
        Product prod = new Product(kood);
        String nimetus = "    " + rs.getString("nimetus").trim();
        prod.setName(nimetus);
        if(soodukad.contains(kood)){
        	prod.setStatusDiscount("1");
        }
        prod.setInCases( (rs.getDouble("tykke")));
        prod.setPrice( (rs.getDouble("hind")));

        String grupp = rs.getString("grupp") ;
         if (grupp != null && grupp.trim().length()>0) {
           prod.setGroup(grupp.trim());
         }
         String grupp2 = rs.getString("grupp2") ;
         if (grupp2 != null && grupp2.trim().length()>0) {
           prod.setGroupSecond(grupp2.trim());
         }
         prod.setInUnit(rs.getDouble("inunit"));
         String pal = rs.getString("onpallet");
                 if (pal != null && pal.trim().length()>0) {
                // double pallet = Double.parseDouble(pal);
                // prod.setOnPallet(pallet);
                 }

       String ean = rs.getString("ean");
       if(ean != null && ean.trim().length()>7){
         prod.setEAN(ean.trim());
       }
        String unit =rs.getString("unit");

        if (unit != null && unit.trim().length()>0){
        unit = unit.trim();
        prod.setUnit(unit);
        prod.setGroupSecond(unit);
        if(unit.equals("1/4E") || unit.equals("1/2E") ){
         prod.setDiffColor(true);
         }

        }

        String id = rs.getString("id");
       // System.out.println("*" + id.trim() + "*");
            if (id.trim().equals("605") || id.trim().equals("606") ){
            prod.setSentExtra("SE");
            } else if (id.trim().equals("609")) {
              prod.setSentExtra("TA");
            }
        prod.setPalSpace(rs.getDouble("palletspace"));
        if(id.equals("605") || id.equals("606")){
        	id= "z" + id;
        }else{
        	id= "a" + id;
        }
        prod.setVat("21");
        
        String aaa = rs.getString("aaa");
        if(aaa != null && aaa.trim().length() == 2){
        	id = id + aaa.trim();
        }else{
        	id = id + "00";
        }
        
     //   nimetus= id+ nimetus;
      //  prod.setName(id + prod.getName());
        if(stocks.get(kood) != null){
        	prod.setStock(Double.parseDouble((String)stocks.get(kood)));
        }

        products.put(nimetus, prod);
        //}
      }
      rs.close();
      st.close();
      con.close();

    } catch (Exception e){
      getParent().showError(e);
    }
    
   
    return products;
  }

  public Vector getCustomerProductsList(String agentCode){

    String cmd =
        " SELECT r.OBITNO AS toode,r.OBCUNO AS saaja,(r.OBORQT) AS kogus , substr(r.OBDWDT,5,8) AS kuup " +
        " FROM mvxjdta.OOLINE r  WHERE r.OBDWDT >" + getStartingDateStr() +
        " and OBORQT>0 GROUP BY r.OBITNO , r.OBCUNO,r.OBORQT, r.OBDWDT  ORDER BY r.OBCUNO,r.OBITNO,r.OBDWDT";

  /*  String cmd1 =
        " SELECT r.OBITNO AS toode,r.OBCUNO AS saaja,SUM(r.OBORQT) AS plusskogus " +
        " FROM mvxjdta.OOLINE r  WHERE r.OBDWDT >" + getStartingDateStr() +
        " AND r.OBORQT > 0 GROUP BY r.OBITNO , r.OBCUNO,r.OBORQT ORDER BY r.OBCUNO,r.OBITNO,r.OBDWDT";

    String cmd2 =
        " SELECT r.OBITNO AS toode,r.OBCUNO AS saaja,SUM(r.OBORQT) AS miinuskogus " +
        " FROM mvxjdta.OOLINE r  WHERE r.OBDWDT >" + getStartingDateStr() +
        " AND r.OBORQT < 0 GROUP BY r.OBITNO , r.OBCUNO,r.OBORQT ORDER BY r.OBCUNO,r.OBITNO,r.OBDWDT";*/

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
        //}
      }
      rs.close();
/*      ResultSet rs1 = st.executeQuery(cmd1);
      while(rs1.next()){
        String toode = rs1.getString("toode").trim();
        String klient = rs1.getString("saaja").trim();

        double pkogus = rs1.getDouble("plusskogus");
        Customer c = (Customer)kliendid.get(klient);
        if (c != null){
          ArrayList al = (ArrayList)c.getProducts().get(toode);
          if (al != null){
            CustProd cp = (CustProd)al.get(1);
            if (cp != null){
              cp.setMovedStockPluss(pkogus);
              custProds.add(cp);
              //System.out.println(cp.toString());
            }
          }
        }
      }

      rs1.close();

    ResultSet rs2 = st.executeQuery(cmd2);
    while(rs2.next()){
    String toode = rs2.getString("toode").trim();
    String klient = rs2.getString("saaja").trim();
      double mkogus = rs2.getDouble("miinuskogus");
      Customer c = (Customer)kliendid.get(klient);
      if (c != null){
        ArrayList al = (ArrayList)c.getProducts().get(toode);
        if (al != null){
          CustProd cp = (CustProd)al.get(1);
          if (cp != null){
            cp.setMovedStockMinus(mkogus);
            custProds.add(cp);
          }
        }
      }
    }

*/
  //    rs2.close();
      st.close();
      con.close();

    }
    catch (Exception e) {
      getParent().showError(e);
    }

    return custProds;

  }


  public void setBulkSales(Map agents){

    String cmd = "  SELECT UCSMCD AS agent,Sum((case when uc.UCORTP IN ('600', '605', '615', '625', '626', '630', '635', '660')  then uc.UCSAAM else 0 end)) AS summacur,Sum((case when uc.UCORTP!=660 then uc.UCVOL3 else 0 end)) AS summavol " +
 " FROM MVXJDTA.OSBSTD uc, MVXJDTA.MITMAS mm, MVXJDTA.CSYTAB ct, MVXJDTA.OCUSMA ok  " +
 " WHERE uc.UCCONO=200 AND ok.OKCONO=200 AND ct.CTCONO=200 AND mm.MMCONO=200 AND substr(uc.UCIVDT,0,6) = '" + getYearMonth() + "' AND uc.UCITNO=mm.MMITNO AND uc.UCDIVI=600 AND ct.CTSTCO='CUCL' AND ct.CTSTKY=ok.OKCUCL AND mm.MMITTY='601' AND uc.UCIVNO>0 AND ok.OKCUNO=uc.UCCUNO AND mm.MMITGR!='149' AND uc.UCORTP IN ('600', '605', '615', '625', '626', '630', '635', '660') " + 
 " GROUP BY uc.UCSMCD ";


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
                 System.out.println("agent" + agent.getName() + " " + agent.getMonthSales()+ " " + agent.getMonthSalesVol());
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

  	/*

       DealerProduct dealerProds = new DealerProduct();
       try {


          String  sql = " SELECT  MBITNO AS toode,MBSTQT-MBREQT AS STOCK , MBWHLO AS LADU " +
		"from mvxjdta.MITBAL l WHERE MBWHLO='100' OR MBWHLO='205' OR MBWHLO='215' ORDER BY MBITNO" ;

            if (isDebug()) System.out.println(sql);
               Connection con1 = getNewConnection();
               Statement stm = con1.createStatement();
               ResultSet rst = stm.executeQuery(sql);

                 while (rst.next()) {
                   String toode = rst.getString("toode").trim();
                   int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                   String ladu  =  rst.getString("ladu").trim();
                   dealerProds.add(ladu, toode,seis+""," "," " );
                   //System.out.println(ladu+" "+toode+" "+seis+"");
               }
               rst.close();
               stm.close();
               con1.close();

       } catch (Exception e) {
           getParent().showError(e);
       }

        return dealerProds;*/
        return null;
      }



      public Vector getDebt(String agentCode) throws SQLException{
       Vector list = new Vector();
       String sql = "SELECT trim(ESPYNO) AS maksja,ESCINO AS arve, ESDUDT AS kuupaev,ESCUAM AS SUMMA," +
         " round(SYSDATE -to_date(to_char(ESDUDT),'YYYYMMDD')) AS paevi ,ESDUDT As tahtaeg " +
         "FROM mvxjdta.FSLEDG Where ESRECO=0 AND ESCONO=200 ORDER BY ESPYNO   " ;

     if (isDebug()) System.out.println(sql);
       Connection con = getNewConnection();
       stmt = con.createStatement();
       ResultSet rs = stmt.executeQuery(sql);
       Calendar cal = Calendar.getInstance();
       Date now = new Date(cal.getTimeInMillis());

       while(rs.next()){
         Debt debt = new Debt();
         debt.setCustID(rs.getString("maksja").trim());
         debt.setDoknr(rs.getString("arve").trim());
         Date duedate = getDate(rs.getString("kuupaev").trim());
         debt.setDuedate(duedate);

         debt.setSum(rs.getDouble("summa"));
         debt.setDue(rs.getString("paevi"));
         if(duedate.getTime()<now.getTime()){
           list.addElement(debt); // pane juhul kui  maksep�ev +1 on k�es kasuta tahtaeg
           // siis tuleb kokku liita YMT summad ja addida nee vastavale CUSTOMERI.Payerile
         }
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
      return "MovexLIT";
    }

    public String getSubjectFilesTo() {
      return this.subjectFilesTo;
    }


    public String getDaySalesData(Agent agent) {
      String sql = "SELECT t.MMspe2 AS grupp,SUM(UCSAAM) AS summa " +
         "FROM mvxjdta.OSBSTD d ,mvxjdta.MITMAS t WHERE t.MMITNO=d.UCITNO " +
         "AND substr(UCIVDT,0,6) = '" + getYearMonth() + "' " +
         "AND UCSMCD='" +agent.getID() + "' AND UCITTY = '601' AND M9FACI=200 GROUP BY t.MMspe2 ORDER BY t.MMspe2 ";
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
             con.close();
             sb.append("\n\n" + getParent().getString ("string3") + "\n\n");
             sb.append(getParent().getString("string2") + "\n\n");
              } catch (Exception E){}

      return sb.toString();
    }



    public String getPilotDayData(Agent agent){

    	
    	String sss = "SELECT c.oaorno AS nr,d.okcunm as nimi,a.oacuor AS kliendi_tell_nr, " +
" CASE WHEN b.evstat='15' THEN 'ERR_HEADER' WHEN b.evstat='20' THEN 'ERR_LINES_0' WHEN b.evstat='25' THEN 'ERR_IN_LINE' WHEN b.evstat='30' THEN 'ERR_CRED_BLOCK' " +
" WHEN b.evstat='90' THEN 'OK' ELSE b.evstat " +
" END AS stat1, CASE WHEN c.oaorsl='20' THEN 'ERR_LINES_0' WHEN c.oaorsl='90' THEN 'ERR_DELETED90' WHEN c.oaorsl='99' THEN 'ERR_DELETED99' ELSE 'OK' END AS stat2 " +
" FROM mvxjdta.oxhead a,mvxjdta.OXCNTR b,mvxjdta.oohead c,mvxjdta.ocusma d   " +
" WHERE  a.oacuno=d.okcuno AND a.oaorno=b.evorno AND b.evornr=c.oaorno AND " +
" a.oaordt='" + this.getDateStr() + "' and ((case when (substr(OKSMCD,3,1) not in ('1','2','3','4') OR substr(OKSMCD,3,1) is null ) then OKSMCD else " +
         " (case when (substr(OKSMCD,4,1) not in ('1','2','3','4') OR substr(OKSMCD,4,1) is null ) " +
         " then substr(OKSMCD,0,2) else substr(OKSMCD,0,3) end)end) = '" + agent.getID()+ "' or (CASE WHEN d.okresp='*USER' THEN ' ' ELSE d.okresp END) ='" + agent.getID()+ "' )ORDER BY a.oaorno";

      String sql = "SELECT t.MMspe2 AS grupp,SUM(UCSAAM) AS summa " +
          "FROM mvxjdta.OSBSTD d ,mvxjdta.MITMAS t WHERE t.MMITNO=d.UCITNO " +
          "AND substr(UCIVDT,0,6) = '" + getYearMonth() + "' "+
          "AND UCSMCD='" +agent.getID() + "' AND UCITTY = '01'  GROUP BY t.MMspe2 ORDER BY t.MMspe2 ";

  
    

      System.out.println(sql);
      StringBuffer sb = new StringBuffer();
       try {
         sb.append("\n " + getParent().getString("todaysbills") + "\n\n");

         Connection con4 = getNewConnection();
         stmt = con4.createStatement();
         ResultSet rs5 = stmt.executeQuery(sss);
         while (rs5.next()) {
           sb.append(rs5.getString("nr") + " " + rs5.getString("nimi") + "\n (stat1 "  + rs5.getString("stat1") + ") (stat2 " + rs5.getString("stat2") +  ")\n\n");
         }
         rs5.close();
         con4.close();

         System.out.println("ESIMENE TEHTUD" + sss);
       /*  sb.append("\n\n " + getParent().getString("string5") + "\n\t" +
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
          System.out.println("Teine TEHTUD");
          System.out.println(sb.toString());
          con.close();
          sb.append("\n\n" + getParent().getString("string3") + "\n\n");
          sb.append(getParent().getString("string2") + "\n\n");
          Connection con2 = getNewConnection();
          String nimetus = null;
          String ale = null;
          String kogus = null;
         Statement st = con2.createStatement();
          ResultSet rs3 = st.executeQuery(sql2);
          System.out.println("" + sql2);
          while (rs3.next()) {
            nimetus = rs3.getString("nimetus").trim();
            ale = rs3.getString("ale");
            kogus = rs3.getString("tase").trim();
           sb.append(nimetus + "\n");
           sb.append("\t\t" + ale + " %\t" + kogus + "\n\n");
          }
          System.out.println("KOLMAS TEHTUD");
        Connection con3 = getNewConnection();
        Statement stv = con3.createStatement();
        ResultSet rs4 = st.executeQuery(sql3);
        ResultSetMetaData md4 = rs4.getMetaData();
        System.out.println(sql3);
        while (rs4.next()) {
          nimetus = rs4.getString("nimetus").trim();
          ale = rs4.getString("ale");
          kogus = rs4.getString("tase").trim();
          sb.append(nimetus + "\n");
          sb.append("\t\t" + ale + " %\t" + kogus + "\n\n");
        }
        con2.close();
        st.close();
        con3.close();
        stv.close();*/
      }
        catch (Exception e) {
          getParent().showError(e);
        }


        return sb.toString();
    }

    public void insertDefective(DefectiveWrapper wr){}



    public void insertDistribution(DistEvalListWrapper wr){}


    
    public Vector getFixedAssortmentData() {
        Vector lines = new Vector();

        try {
       	  String cmd = "   SELECT oiascd AS grupp,OIITNO AS toode " +
          "  FROM mvxjdta.OASITN WHERE  oiascd !='600' " +
           " order by oiascd,OIITNO ";
       	  
       	     System.out.println(cmd);

       	 con = getNewConnection();
       	 
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
    

    private String setZero(int i){
     	String x = i +"";
     	if (x.length() == 1){
     		x = "0" + x;
     	}
     	return x;
     }
    private Date getDate(String kuup) {

      Calendar cal = Calendar.getInstance();
      int YEAR = Integer.parseInt(kuup.substring(0, 4));
      int MONTH = Integer.parseInt(kuup.substring(4, 6));
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

    
    public String getDateStr(){
        Calendar cal = Calendar.getInstance();
        String month = (cal.get(Calendar.MONTH)+1) +"";
        String day = (cal.get(Calendar.DAY_OF_MONTH)) +"";
             if(month.length() == 1){month = "0" + month;}
             if(day.length() == 1){day = "0" + day;}
             return cal.get(Calendar.YEAR) + "" + month + day;
         }
    
    public String get30AgoStr(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -30);
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

   	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){}


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


  private Connection con;
  private Statement stmt;
  private String subjectFilesTo = null;
  private TreeMap agents1 = new TreeMap();
  private NumberFormat NF = NumberFormat.getInstance();
  private TreeMap  dealers = new TreeMap();
  HashMap aHash = new HashMap();
public Vector getPriceMatrixData() {
	// TODO Auto-generated method stub
	return null;
}

public String getProductSortingData() {
	// TODO Auto-generated method stub
	return null;
}
}
