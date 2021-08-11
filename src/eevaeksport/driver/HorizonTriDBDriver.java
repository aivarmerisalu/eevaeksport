package eevaeksport.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
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
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class HorizonTriDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter,PictureMaker {
  public HorizonTriDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
    } catch (Exception e){
       parent.showError(e);
    }
    
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
    nodebt = new Vector();
   String nd = getParent().getProps().getProperty("nodebt.list");
   if(nd != null){
	   StringTokenizer st = new StringTokenizer(nd,",");
	   while(st.hasMoreTokens()){
		   nodebt.add(st.nextToken());
	   }
   }
    
  }

  String user =  null;
  String pass = null;
  String rcompany = null;
  Vector nodebt = null;
  HashSet usedProds = null;
  public void connectionUp(){
  }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

  public String getSaveDebtInfo() {
   	return null;
   }


  public Vector getFixedAssortmentData() {
      Vector lines = new Vector();
      return lines;
}

  public Vector getPriceMatrixData() {
	  Vector lines = new Vector();
	  
	String sql = " SELECT  N.KODS as toode,[CENA] as hind ,[NO_DAT] as algus ,[LIDZ_DAT] as lopp , " +
   " k.KODS as klient,H.[PK_GRUPA] as clgrupp,datediff(day,[NO_DAT] , [LIDZ_DAT]) as paevi " +
    " FROM [APV06161].[NCEN]  H  inner join  APV06161.DKLIENTS K ON H.PK_KLIENTS=K.PK_KLIENTS  "
    + "inner join  APV06161.NNOM n on n.PK_NOM= h.PK_NOM where GETDATE()"
    + " between NO_DAT and LIDZ_DAT and year(LIDZ_DAT) != 3000";
	  Statement st = null;
      try {
    	 
        connectionUp();
        Connection       con = getNewConnection();
        
        st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String prodID = rs.getString("toode").trim();
          double price = rs.getDouble("hind");
          String algus = rs.getString("algus");
          String lopp = rs.getString("lopp");
        
          String custID = rs.getString("klient");//.trim();
          String clgr = rs.getString("clgrupp");//.trim();
          int paevi = rs.getInt("paevi");
          
          
          MatrixLine mline = new MatrixLine();

     
			
          if (custID != null && custID.trim().length() > 0 && !custID.trim().equals("1")) {
            mline.setCustID(custID.trim());
          }else if ( clgr != null && clgr.trim().length() > 0) {
                mline.setCliGroup(clgr.trim());
          }

          if (prodID != null && prodID.trim().length() > 0) {
            mline.setProdID(prodID.trim());
          }
      
          if(paevi < 125){
	
          if (lopp != null) {
     				mline.setEndDate(lopp.substring(0, 10));
     			}
     	  if (algus != null) {
     				mline.setStartDate(algus.substring(0, 10));
     			}
     	  
     	  }
     	  
     	  
          mline.setPrice(price);

          if(mline.getCustID() != null || mline.getCliGroup() != null){
              lines.add(mline);
          }
     	
        }

        
        
        
        
  	  // võtame välja need mis on aktiivsed - kui on Klient ja kaup konkreetselt 
        String sql2 = "   SELECT  N.KODS AS prodid , '' as custid ,  CENA.PK_CENVEID as clgroup, " +
  "    0 AS discount  ,  CENA.CENA AS price , CENA.NO_DAT as algus , CENA.LIDZ_DAT  as lopp , " +
	"	 datediff( day,CENA.NO_DAT , CENA.LIDZ_DAT)  as paevi  " +
  "		  FROM   APV06161.NCEN CENA, APV06161.NNOM N, APV06161.NCENVEID CV, APV01454.SVALUTAS VAL  WHERE " +
  "		  CENA.PK_NOM=N.PK_NOM AND CENA.PK_CENVEID=CV.PK_CENVEID AND  " +
  "		  CENA.PK_VAL=VAL.PK_VAL AND N.STATUSS = 0 AND CV.STATUSS = 1 AND " + 
  "		 CENA.NO_DAT <= getdate()+1  AND  " +
  "		  CENA.LIDZ_DAT >= getdate()-1   AND CV.TIPS IN (0)  and VAL.KODS = 'EUR' " +
	"		   and  CENA.PK_CENVEID not in ( 4,18 ) order by  N.KODS";

       
   
                   st = con.createStatement();
                   rs = st.executeQuery(sql2);


              	                  while (rs.next()) {
              	                	  String prodID = rs.getString("prodid");
              	                          String clgroup = rs.getString("clgroup");
              	                          double price = rs.getDouble("price");
              	                      
              	                        //  String algus = rs.getString("algus");
              	                        //  String lopp = rs.getString("lopp");
              	                        //  int paevi = rs.getInt("paevi");
              	                          MatrixLine mline = new MatrixLine();
              	                          
              	                    //KLIENDIGRUPP JA TOOTEGRUPP
              	                          if (clgroup != null && clgroup.trim().length() > 0) {
              	                                  mline.setCliGroup(clgroup.trim());
              	                          }
              	                       
              	                          if (prodID != null && prodID.trim().length() > 0) {
              	                                  mline.setProdID(prodID.trim());
              	                          }
              	                     

              	                          if (price != 0) {
              	                                  mline.setPrice(price);
              	                          }
              	                          
              	                         
              	                          
              	                          
              	                           if(mline.getPrice() > 0){
              	                        	   lines.add(mline);
              	                           }
              	                         
              	                  }


        
        
        rs.close();
        st.close();
        this.connectionDown();
      }
      catch (SQLException ex) {
      }
  System.out.println("Pmatrix "+ lines.size());	
	
	  return lines ;

  }
	  
	   // jätame meelde yldised aled et saaks summeerida 
	  
	  
  public Vector getPriceMatrixDataold() {
	  
	   // jätame meelde yldised aled et saaks summeerida 
	  
	  Vector lines = new Vector();
      
	  // võtame välja need mis on aktiivsed - kui on Klient ja kaup konkreetselt 
          String sql = "   SELECT  N.KODS AS prodid , '' as custid ,  CENA.PK_CENVEID as clgroup, " +
    "    0 AS discount  ,  CENA.CENA AS price , CENA.NO_DAT as algus , CENA.LIDZ_DAT  as lopp , " +
	"	 datediff( day,CENA.NO_DAT , CENA.LIDZ_DAT)  as paevi  " +
    "		  FROM   APV06161.NCEN CENA, APV06161.NNOM N, APV06161.NCENVEID CV, APV01454.SVALUTAS VAL  WHERE " +
    "		  CENA.PK_NOM=N.PK_NOM AND CENA.PK_CENVEID=CV.PK_CENVEID AND  " +
    "		  CENA.PK_VAL=VAL.PK_VAL AND N.STATUSS = 0 AND CV.STATUSS = 1 AND " + 
    "		 CENA.NO_DAT <= getdate()+1  AND  " +
    "		  CENA.LIDZ_DAT >= getdate()-1   AND CV.TIPS IN (0)  and VAL.KODS = 'EUR' " +
	"		   and  CENA.PK_CENVEID != 117 order by  N.KODS";

try{        
      Connection       con = getNewConnection();
           Statement stmt = con.createStatement();
                  ResultSet rs = stmt.executeQuery(sql);


                	                  while (rs.next()) {
                	                	  String prodID = rs.getString("prodid");
                	                          String clgroup = rs.getString("clgroup");
                	                          double price = rs.getDouble("price");
                	                      
                	                        //  String algus = rs.getString("algus");
                	                        //  String lopp = rs.getString("lopp");
                	                        //  int paevi = rs.getInt("paevi");
                	                          MatrixLine mline = new MatrixLine();
                	                          
                	                    //KLIENDIGRUPP JA TOOTEGRUPP
                	                          if (clgroup != null && clgroup.trim().length() > 0) {
                	                                  mline.setCliGroup(clgroup.trim());
                	                          }
                	                       
                	                          if (prodID != null && prodID.trim().length() > 0) {
                	                                  mline.setProdID(prodID.trim());
                	                          }
                	                     

                	                          if (price != 0) {
                	                                  mline.setPrice(price);
                	                          }
                	                          
                	                         
                	                          
                	                          
                	                           if(mline.getPrice() > 0){
                	                        	   lines.add(mline);
                	                           }
                	                         
                	                  }

                                    rs.close();                  
                  stmt.close();
                  con.close();

          } catch (SQLException ex) {
          ex.printStackTrace();
          }
	  
	  System.out.println("PriceLines " + lines.size());
          return lines;
  }

  
  
  
  
    
  
  public String getProductSortingData(){
   	return null;
   }

  public Vector getDebt(String agentCode) throws SQLException{
     return this.debt;
   }


  public String getAssortmentStr(Agent agent){
  	return null;
  }
  
  private TreeMap kogused = null;



 
 
 public String getSaveStockInfo(){
	  
	   kogused = new TreeMap();
		  
	   String    cmd = "  SELECT  N.KODS AS toode,  N.NOSAUK AS stocka, VIEN.KODS AS VIEN_KODS,  VOL.DAUDZ  AS stock, " +
" VOL.DAUDZ2 AS VOL_DAUDZ2,  ROUND(VOL.SUMMA, 6) AS VOL_SUMMA,  NOL.KODS AS NOL_KODS,  NOL.NOSAUK AS NOL_NOSAUK, " +
" '0' AS MULTISEL, N.COUNTER AS N_COUNTER, " +
" N.PK_NOM AS N_PK_NOM, NOL.PK_KLIENTS AS ladu  , VOL.PK_NOL FROM   APV06161.NNOM N LEFT OUTER JOIN (SELECT V.PK_NOL, V.PK_NOM ," + 
" SUM(V.DAUDZ) AS DAUDZ, SUM(V.DAUDZ2) AS DAUDZ2, " +
" SUM(V.SUMMA) AS SUMMA,SUM(CASE ISNULL(NN.M_ATT12,0) WHEN 0 THEN CAST(NULL AS FLOAT) ELSE V.DAUDZ / NN.M_ATT12 END) AS DAUDZ12, " +
" SUM(CASE ISNULL(NN.M_ATT13,0) WHEN 0 THEN CAST(NULL AS FLOAT) ELSE V.DAUDZ / NN.M_ATT13 END) AS DAUDZ13 FROM APV06161.NVOL V, APV06161.NNOM NN , " +
" APV06161.NVOLDAT DT WHERE  V.DAT < GETDATE()  AND V.PK_NOMPART = DT.PK_NOMPART AND V.PK_NOL = DT.PK_NOL AND DT.DATB >= GETDATE() " +
"  AND V.STATUSS IN (0,1)  AND V.PK_NOM = NN.PK_NOM AND NN.ATLUZSK=1 GROUP BY V.PK_NOL,V.PK_NOM) VOL ON N.PK_NOM = VOL.PK_NOM " +
"   LEFT OUTER JOIN APV06161.DKLIENTS NOL ON VOL.PK_NOL = NOL.PK_KLIENTS LEFT OUTER JOIN APV06161.NVIEN VIEN ON N.PK_VIEN=VIEN.PK_VIEN " +
"   WHERE ( ( VOL.DAUDZ <> 0  OR  VOL.DAUDZ2 <> 0  OR  ROUND(VOL.SUMMA, 6) <> 0)  ) AND (N.ATLUZSK = 1)  AND ( VOL.PK_NOL = 33  OR " +
"     VOL.PK_NOL = 859    )" +
"    GROUP BY N.KODS,N.NOSAUK,VIEN.KODS,VOL.DAUDZ,VOL.DAUDZ2,ROUND(VOL.SUMMA, 6),NOL.KODS,NOL.NOSAUK,N.COUNTER,N.PK_NOM,NOL.PK_KLIENTS,VOL.PK_NOL " +
"     ORDER BY   N.KODS ASC" ;

	       if (isDebug()) System.out.println(cmd);
	       StringBuffer products = new StringBuffer();
	       try {
             
	    	 Connection  con = getNewConnection();
	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmd);

	         while (rs.next()) {

	           String kood = rs.getString("toode").trim();
	           double seis = rs.getDouble("stock");
	           String dealer = rs.getString("ladu");
	           if(seis > 0){
	               products.append(kood +  ";");
	               products.append(" ;");
	               products.append(getParent().getNF().format(seis) + "; \n"); // + dealer +  "\n");
	             kogused.put(kood, seis + "");
	           }
	         
	         }
	         
	           rs.close();
	       } catch (Exception e) {
	           getParent().showError(e);
	       }
	       
	       return products.toString();
	   }




              public String getDealerCustomerCSV(String agent){
               return null;
               }

                                                  public String  getCustProdDiscounts(Agent agent){
                                                   return null;
                                                   }


  /**
   *
   */
  public void connectionDown(){
  }

  
  
  public TreeMap getDealers() {
     return null;
	    }
  

  public DealerProduct getDealerProductsList() {
	  return null;
 
 }


	@Override
	public void PictureMaker() {
		makePictureCat();
		 System.exit(0);
		
	}
  
  
  public TreeMap getAgents(){
	  
	  
    String cmd = "    SELECT M.PK_REISS AS agent, M.NOSAUK AS nimi   FROM  [APV06161].[NREISS] M  ";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
   
  // this.getPriceMatrixData();
    try {
    
    	Properties props = new Properties();
		try {
			props = EksportFrame.loadProps("asendajad.txt");

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Connection con = this.getNewConnection();
    	Statement stmt = con.createStatement();
    	ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = rs.getString("nimi").trim();
   
          //   String info = rs.getString("info");
         // if(info != null && info.contains("#")){
        	//  agent.setObject(info.substring(info.indexOf("#")+1, info.indexOf("#")+4));
          //}
          
          
          agent.setName(name + " " + agent.getID());
          //System.out.println(name + " " + agent.getID() + " " + agent.getObject());
          agent.setIsPilot(true);
          
          agent.setLanguage("LAT");
          agent.setCountry("lv");
          agent.setPassWord("0000");
          String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
          if(email.length() > 3){
        	  agent.setEmail(email);
          }
         
          
          agent.setActive(true);
  		String agentAGENTS = props.getProperty("dub." +agent.getID());
		if(agentAGENTS != null){
           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
	    while(stag.hasMoreTokens()){  	
         agent.addDubbler(stag.nextToken());
       //  System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
	       }
         }
		
		 agents1.put(agent.getID(), agent);
          agents.put(name+ " " + agent.getID(), agent);
        }
        rs.close();
        stmt.close();
        con.close();
    } catch (Exception e){
        getParent().showError(e);
      }
  /*  Agent aa = new Agent ("HOUSE1");
    aa.setName("HOUSE1");
    agents.put("HOUSE1", aa);
    */
    
    if(rcompany != null){
    	//Converter.sendAgentJson(agents, rcompany, false);
    }

  //  this.setBulkSales(agents1);
    return agents;
  }

  public TreeMap getCustomers(Agent agent){
	  
 
    String salessql = " SELECT K.KODS as klient ,A.PK_ADRESE as aid, round(SUM(summa_pv),0) AS summa,str(datepart(YYYY,D.DAT_DOK))+' '+str(datepart(mm,D.DAT_DOK)) as kuu " +
		 "  FROM   APV06161.DPIRMDOK D LEFT OUTER JOIN APV06161.DKLIENTS N ON D.PK_ESPATS=N.PK_KLIENTS " +
		 "  LEFT JOIN APV06161.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS " +
		 "  JOIN APV06161.NDOKG G ON D.PK_DOK = G.PK_DOK " +
		 "  LEFT OUTER JOIN (SELECT PK_DOK, MAX(LAIKS) AS LAIKS FROM APV06161.SDOKEVNT GROUP BY PK_DOK) LASTEV ON LASTEV.PK_DOK = D.PK_DOK " +
		 "  LEFT OUTER JOIN APV06161.NADRESES A ON A.PK_ADRESE=G.PK_ADRESE, APV01454.SVALUTAS V " +
		 "   WHERE D.PAMV_ID IN (2710,2720,2711,2721,2712,2786,2713,2737,2718,2728,18212,2745,2782,24501,2797,2798,2739,18086,2736,2783,2799,18091,15008,15011,2738,2792,2795,2742,2793,2796)" +
		"	AND D.PK_VAL=V.PK_VAL AND  D.DAT_DOK > getdate()-120 " +
		"	 AND D.PAMV_ID = 2711 AND  NOT (D.STATUSS = 4)  and K.KODS is not null   " +
		"	 GROUP BY K.KODS,A.PK_ADRESE,str(datepart(YYYY,D.DAT_DOK))+' '+str(datepart(mm,D.DAT_DOK)) " +
		"	  ORDER BY  K.KODS ASC " ;
 

 String sqldebt = "	  select k.KODS AS klient,sum(D.SUMMA_APMP-D.SUMMA_PIESP) AS tasumata FROM   APV06161.DPIRMDOK  d " +
   " LEFT JOIN APV06161.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS where  (D.SUMMA_PIESP-D.SUMMA_APMP)!=0 AND d.APM_TERM < getdate()+1 " +
	" group by k.KODS ";
 
 String sqldebtall = "	  select k.KODS AS klient,sum(D.SUMMA_APMP-D.SUMMA_PIESP) AS tasumata FROM   APV06161.DPIRMDOK  d " +
		   " LEFT JOIN APV06161.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS where  (D.SUMMA_PIESP-D.SUMMA_APMP)!=0  " +
			" group by k.KODS ";
 
 //    tal_visitationdays ,talspartnercode,

 try{
	   Converter.getManagersReplacements(agents1, rcompany, user, pass); 
}catch(Exception e){

}
 
 
 
 TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    Vector payers =  new Vector();
    
    HashMap<String, Double> overd = new HashMap();
    HashMap<String, Double> alld = new HashMap();
    HashMap salesres = new HashMap();
    	ResultSet rs1 ;
    	Statement stmr ;
    	

    	try{
    		
        	Connection con = this.getNewConnection(); 
        		 stmr = con.createStatement();
            	 rs1 = stmr.executeQuery(sqldebt);
                
            	 while(rs1.next()){
                	String klient = rs1.getString("klient").trim();
                	double summa = rs1.getDouble("tasumata");
                	overd.put(klient, summa);
                }
                
               rs1.close();
               stmr.close();
               con.close();
        	}catch(Exception s){
        		s.printStackTrace();
        	}
    	
    	
    	try{
    		
        	Connection con = this.getNewConnection(); 
        		 stmr = con.createStatement();
            	 rs1 = stmr.executeQuery(sqldebtall);
                
                 while(rs1.next()){
                	String klient = rs1.getString("klient").trim();
                	double summa = rs1.getDouble("tasumata");
                	alld.put(klient, summa);
                }
                
               rs1.close();
               stmr.close();
               con.close();
        	}catch(Exception s){
        		s.printStackTrace();
        	}
    		
    	
    	try{
    		
    	Connection con = this.getNewConnection(); 
    		 stmr = con.createStatement();
        	
             rs1 = stmr.executeQuery(salessql);
            while(rs1.next()){
            	String klient = rs1.getString("klient").trim() ;
            	String abi =  rs1.getString("aid");
            	
            	if(abi != null){
                    abi = 	"_" + abi.trim();
                    }else{
                    	abi = "";
                    }
            	
            	klient = klient + abi;
            	int summa = (int)rs1.getDouble("summa");
            	
            	String aeg = rs1.getString("kuu").trim();
            	String kuu = aeg.substring(0, 4).trim() ;
            	if(kuu.length() == 1){
            		kuu = "0" + kuu;
            	}
            	String aasta = aeg.substring(4).trim();
            	if(aasta.length() == 1){
            		aasta = "0" + aasta;
            	}
              if(salesres.get(klient) == null){
            	  salesres.put(klient,"   " + aasta + " : " + summa + " €" );
              }else{
            	  String asi = (String)salesres.get(klient);     	  
            	  salesres.put(klient,"   " + aasta + " : " + summa +" €|" + asi);
              }
            }
           rs1.close();
           stmr.close();
           con.close();
    	}catch(Exception s){
    		s.printStackTrace();
    	}
    
    
    
    
    try {

    	
    	 

    	String cmd = "SELECT K.NOSAUK AS nimi, K.PK_KLIENTS AS id ,A.PK_ADRESE AS aid , K.KODS AS kood,"
    			+ "K.REG_NR as payer,K.ADRESE as address ,A.ADRESE as address2 ,k.PIEZIMES as address3, A.PK_REISS as agent , " +
    " K.APMTERM as paym ,K.KREDLIM as credit, K.LIC_NR as licence ,K.LIC_IZDAT as lepalg , " +
    " K.TELEFONS as phone,K.MOBTEL as mphone ,K.EPASTS as email ,k.PK_CENVEID as hinnak  ,gru.nosauk as chain ,kkat.nosauk as kate " +
    " , K.PK_GRUPA as alegru , K.BRIDIN as infoke FROM   APV06161.DKLIENTS K LEFT OUTER JOIN  APV06161.NADRESES a ON A.PK_KLIENTS=K.PK_KLIENTS  " // and a.LEG_ADR = 2
    + " LEFT OUTER JOIN APV06161.DGRUPAS GRU ON K.PK_GRUPA = GRU.PK_GRUPA  " +
    " LEFT OUTER JOIN APV06161.DKLKAT KKAT  ON K.PK_KLKAT = KKAT.PK_KLKAT " +
    " WHERE (K.STATUSS <> 2 AND K.PK_DOKT IS NOT NULL AND ( NOT K.PAMV_ID IN (1810,11970))) AND " +
    " (( ( (K.TIPS = 3)  OR  (K.TIPS = 4)  OR  (K.TIPS = 6)  OR  (K.TIPS = 8) ) AND ( (K.STATUSS = 0)  OR " +
    "  (K.STATUSS = 1) ) AND ( (K.STATUSS = 0) ) AND ( (K.TIPS <> 8) ) AND ( (K.DBKRPAZ = 1) ) ))  and K.KODS is not null   " +
    " ORDER BY   K.NOSAUK ASC " ;
     
      if (isDebug()) System.out.println(cmd);
       
    	
    	
    	Connection con = this.getNewConnection();
    	Statement stmt =con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      
      while(rs.next()){
        String name = (rs.getString("nimi").trim());
        String abi  =  rs.getString("aid");
        
        if(abi != null){
        abi = 	"_" + abi.trim();
        }else{
        	abi = "";
        }
        String kood = rs.getString("kood").trim();
        String accountnum = kood +  abi;//+  abi;
        Customer cust = new Customer(accountnum);
        cust.setPayer(kood);
       
        if(salesres.get(accountnum) != null){
        	cust.setInfo("CHART Sales :|Year Month|Sales EUR|" + salesres.get(accountnum) );
        }
        
        if(overd.get(kood) != null){
        	cust.setDebt(overd.get(kood));
       // 	System.out.println("oioi" + kood);
        }
        if(alld.get(kood) != null){
        	cust.setUsedCredit(alld.get(kood));
        }
        cust.restName = name;
        
        name = Converter.enCodeEst(name);
       
        cust.setName(name + " " + accountnum);
         
        String payer = rs.getString("payer");
        if(payer != null && payer.trim().length() > 0){
        	//cust.setPayer(payer.trim());
        }else{
        	//cust.setPayer(accountnum);
        }
       
 
        
       String aadr = rs.getString("address2");
       
 
        if (aadr != null) {
        	String asi = aadr.trim();
        	if(asi.toLowerCase().indexOf("latvija") != -1){
        		try{
        			asi = asi.substring(0,asi.toLowerCase().indexOf("latvija")-1).trim();
           	}catch (Exception k){
        		}
        	}
        	
        	String addrx = Converter.enCodeEst(asi.replace('\n',' ').replaceAll("\\r\\n|\\r|\\n", " "));
         	 name = name + " " + addrx;
             cust.setAddress(Converter.adressLink(addrx,"Latvia"));
             cust.restStreet = addrx;
             cust.setCountry("Latvia");
        	
        	   int koma = asi.lastIndexOf(",");
               if(koma != -1){
            	   String indeks = asi.substring(koma+1);
                   
                   if (indeks != null && indeks.length() > 0){
                    // cust.restZipCode = indeks.trim();
                   }
                   asi = asi.substring(0, koma);
                   koma = asi.indexOf(",");
                   if(koma != -1){

                   	String tanav = asi.substring(0,koma);
                       if (tanav != null && tanav.length() > 0){
                        // cust.restStreet = tanav.trim();
                       }    	   
                       asi = asi.substring(koma+1);
                       
                     try{
                    	 String linn = asi.substring(0 ,asi.length()-1);
                         if (linn != null && linn.length() > 0){
                          // cust.restCity = linn.trim();
                         } 
                     }catch(Exception k){
                    	 
                     }
                   }
               }
        }
        	

        name = name + " " + accountnum;
        cust.setName(name);
    
        String aag = rs.getString("agent");
        if(aag != null && aag.trim().length() >0 ){
        	cust.addAgent(new Agent(aag.trim()));
            //  System.out.println("lisan agenti " + aag + " " + cust.getName());
            
        	 if (agents1.get(aag.trim()) != null){
 				Agent aa = (Agent)agents1.get(aag.trim());
 				Vector vec = aa.getDubbler();
 				if(vec!= null && vec.size() >0){
 					Enumeration en = vec.elements();
 					while(en.hasMoreElements()){
 						String ag = (String) en.nextElement();
 						cust.addAgent(new Agent(ag));
 					}
 				}
 			}
        	
        }
    

        String term = rs.getString("paym");
        if(term != null && term.trim().length() >0){
        	cust.setPaymTerm(term);	
        }
        

        double  credit = rs.getDouble("credit");
        if(credit != 0){
     	   cust.setCredit(((int)credit)+"");
        }
          
        
       
        String licence = rs.getString("licence");
        if (licence != null && licence.trim().length()>0) {
        		cust.setLicence(licence.trim());
        }
        
        String info = rs.getString("phone");
        if (info != null && info.trim().length()> 2) {
           if (info.trim().endsWith(",")){
        	   info = info.substring(0,info.length()-1);
           }
           if (info.trim().startsWith(",")){
        	   info = info.substring(1);
           }
        	cust.setPersonalData("Tel:"+ info.trim());
        }
        
        String mphone = rs.getString("mphone");
        if (mphone != null && mphone.trim().length()>0) {
        	if(cust.getPersonalData() != null && cust.getPersonalData().trim().length() > 0 ){
        		 cust.setPersonalData(cust.getPersonalData()+"|" + "Tel:"+ mphone.trim());
        	}else{
        		 cust.setPersonalData( "Tel:"+ mphone.trim());
        	}
        }
        
        
        String confmail = rs.getString("email");
        if (confmail != null && confmail.trim().length()>0) {
        	if(cust.getPersonalData() != null && cust.getPersonalData().trim().length() > 0 ){
        		 cust.setPersonalData(cust.getPersonalData()+"|" + confmail.trim());
        	}else{
        		 cust.setPersonalData(confmail.trim());
        	}
        }
        
        int  hinnak = rs.getInt("hinnak");
        if ( hinnak  >0 && hinnak != 4) {
        	cust.setGroup(hinnak + "");
        }
        String chain = rs.getString("chain");
        if (chain != null && chain.trim().length()>0) {
        	cust.setChain(chain.trim());
        }
        
        String kate = rs.getString("kate");
        if (kate != null && kate.trim().length()>0) {
        	cust.category = (kate.trim());
        }
          
        String alegru = rs.getString("alegru");
        if (alegru != null && alegru.trim().length()>0) {
        	if(cust.getGroup()!= null && cust.getGroup().trim().length() > 0){
        		cust.setGroup(cust.getGroup().trim() +"|"+alegru.trim());
            }else{
        		cust.setGroup(alegru.trim());
            }
        	
        	}
        	
        String infoke = rs.getString("infoke");
        if (infoke != null && infoke.trim().length()>0) {
        	cust.setInfo(Converter.enCodeEst(infoke.replaceAll("\\r\\n|\\r|\\n", " ").trim()));
        }
        
        boolean laks = true;
                    if((payer == null || payer.trim().length() == 0) && payers.contains(cust.getID())){
                    //	laks = false;
                    }
                    
                    if(laks){
                        customers.put(name+abi, cust);
                        custMap.put(cust.getID(),cust);	
                    }
      }
      
      

      String sql = "	  select k.KODS AS klient,d.DOK_NR AS number,d.DOK_NR2 AS number2 ,d.DOK_SERIJA  as seria,APM_TERM AS maksepaev,(D.SUMMA_APMP-D.SUMMA_PIESP) AS tasumata, " +
      "  DAT_DOK AS kuupaev , datediff(day,APM_TERM,getdate()) AS yle FROM   APV06161.DPIRMDOK  d  " +
       "  LEFT JOIN APV06161.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS where  (D.SUMMA_PIESP-D.SUMMA_APMP)!=0 and APM_TERM > getdate()-300  ";
                   
                rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        String saaja = rs.getString("klient").trim();
                        debt.setCustID(saaja);
                        debt.setPayerID(saaja);
                        
                        String number = rs.getString("number").trim();

                        String number2 = rs.getString("number2");
                        String seria = rs.getString("seria");
                        if(number2 != null && seria != null && seria.trim().length() > 0){
                        	number = seria + number2.trim();
                        }
                        debt.setDoknr(number);
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(rs.getDouble("tasumata"));
                        debt.setDate(rs.getDate("kuupaev"));
                        debt.setDue(rs.getString("yle"));
                        boolean addDebt = true;
                        if(nodebt.contains(saaja)){
                        	addDebt = false;
                        }
                 
                        if(addDebt){
                            this.debt.addElement(debt);
    	                        	
                        }
                      }

           rs.close();
           stmt.close();
           con.close();
    } catch (Exception e){
      getParent().showError(e);
    }
    
    return customers;
  }


  
  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

    public String getDistribution() {
        return null;
      }

 private HashSet usedGr = null;
  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
     
    String    sql = " SELECT  G.PK_NOMGR AS kood, G.KODS AS abi, G.NOSAUK AS nimi FROM   APV06161.NNOMGR G  " +
	"  LEFT OUTER JOIN APV06161.NNOMGR VGR ON G.PK_GRUPA=VGR.PK_NOMGR  WHERE G.STATUSS = 0 ORDER BY G.NPK ";
    sql = " select pk_nomkat as kood , nosauk as nimi  from APV06161.LGNOMKAT  ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("kood").trim();
      String nimi = rs.getString("nimi").trim();
     
      if(usedGr.contains( liik)){
    	  tm.put(liik, Converter.enCodeEst(nimi));  
      }
       
    }
   
    rs.close();
    st.close();
    con.close();
    return tm;
}

  
  public TreeMap getProdGroupsSecond() throws SQLException {
	    Connection con = getNewConnection();
	    TreeMap   tm = new TreeMap();
	     
	    String    sql = " select pk_nomkat as kood , nosauk as nimi  from APV06161.LGNOMKAT  ";
	    
	      sql = " select KODS as kood, NOSAUK as nimi from APV06161.CHD_MG1";
	      sql = " select KODS as kood, NOSAUK as nimi from APV06161.DVALSTS ";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	      String liik = rs.getString("kood").trim();
	      String nimi = rs.getString("nimi").trim();
	     
	      if(usedGr.contains( liik)){
	    	  tm.put(liik, Converter.enCodeEst(nimi));  
	      }
	       
	    }
	   
	    rs.close();
	    st.close();
	    con.close();
	    return tm;}

  public TreeMap getAgents1() {
		return agents1;
	}

  private TreeMap agents1 = new TreeMap();

 HashMap eans = null;
  
  public TreeMap getProducts(Agent agent){
	  eans = new HashMap();
	  usedProds = new HashSet();
	  usedGr  = new HashSet();
	  
	/*  String grout = "307,845,757,924,1,309";
	  if( getParent().getProps().getProperty("groups.out") != null){
		  grout = getParent().getProps().getProperty("groups.out");
	  }*/
	  
	  String cmd = "select t.PK_NOM as abi, t.KODS as  itemid,t.NOSAUK  AS nimetus, t.M_ATT12 AS kastis,M_ATT13 AS alusel, " + 
	  	" bar.BAR_KODS AS ean ,PK_NOMKAT as grupp,gr.kods as gruppuus, PK_NOMGR AS grupp2, ri.KODS as riik, " +
	  	"	yh.kods As unit, 0 as liter , MINPARD as minmax ,  PIEG_SOLIS as opt ," +
	"		(case when gr.kods = 'TM' then 'Chemistry' else 'Alcohol' end) as type , t.filename as weblink" + 
	" from APV06161.NNOM  t LEFT OUTER JOIN APV06161.NBARKODS bar ON bar.PK_NOM=t.PK_NOM AND bar.IS_MAIN = 1 " +
	" LEFT OUTER JOIN APV06161.NVIEN yh ON t.PK_VIEN=yh.PK_VIEN " +
	"  LEFT OUTER JOIN APV06161.CHD_MG1 gr ON t.PK_MG1=gr.PK_MG1 " +
	"  LEFT OUTER JOIN APV06161.NMINMAX aa ON aa.PK_NOM=t.PK_NOM and aa.PK_NOL=22013 " +
	"   LEFT OUTER JOIN [APV06161].[NRAZOT] au ON t.PK_RAZOT=au.PK_RAZOT " +
	"   LEFT OUTER JOIN [APV06161].[DVALSTS] ri ON ri.PK_VALSTS=au.PK_VALSTS " +
	"  where t.STATUSS = 0  and PAMVID = 2015  ";
	    
	//  and t.PK_NOMGR not in (" + grout + ")
    getSaveStockInfo();
    
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
   
    try {
    	
        String hsql =  "   SELECT  N.KODS AS toode, CENA.CENA AS hind  " +
    		"  FROM   APV06161.NCEN CENA, APV06161.NNOM N, APV06161.NCENVEID CV, APV01454.SVALUTAS VAL  WHERE " + 
    		"  CENA.PK_NOM=N.PK_NOM AND CENA.PK_CENVEID=CV.PK_CENVEID AND  " +
    		"  CENA.PK_VAL=VAL.PK_VAL AND N.STATUSS = 0 AND CV.STATUSS = 1 AND " + 
    		" CENA.NO_DAT <= getdate()+1  AND  " +
    		"  CENA.LIDZ_DAT >= getdate()-1   AND CV.TIPS IN (0)  and VAL.KODS = 'EUR' " +
			"   and CENA.PK_CENVEID = 4 order by  N.KODS ";
 	
    	
    Connection con = this.getNewConnection();
   	Statement stmt =  con.createStatement();
    ResultSet   rs = stmt.executeQuery(hsql);
  HashMap<String, Double> hinnad = new HashMap<String, Double>();
  
  HashMap<String, Double> hinnad2 = new HashMap<String, Double>();
   
    while(rs.next()){
    	   String kood = rs.getString("toode").trim();
    	   double hh = rs.getDouble("hind");
    	   	hinnad.put(kood, hh);
    }
    rs.close();
    
    
    hsql =  "    SELECT   N.KODS AS toode, N.NOSAUK AS N_NOSAUK, CENA.CENA AS hind, VAL.KODS AS VAL_KODS, CV.NOSAUK AS CV_NOSAUK, '0' AS MULTISEL, N.PK_NOM AS N_PK_NOM, CV.TIPS AS CV_TIPS, CENA.PK_CEN AS CENA_PK_CEN, " + 
 " CENA.COUNTER AS CENA_COUNTER ,CENA.NO_DAT,CENA.LIDZ_DAT ,CENA.PK_CENVEID " +
 " FROM   APV06161.NCEN CENA, APV06161.NNOM N, APV06161.NCENVEID CV, APV01454.SVALUTAS VAL  " +
 " WHERE (CENA.PK_NOM=N.PK_NOM) AND (CENA.PK_CENVEID=CV.PK_CENVEID) AND (CENA.PK_VAL=VAL.PK_VAL) AND (( ( (N.STATUSS = 0) ) " +
 " AND ( (CV.STATUSS = 1) ) " +
 " AND  CENA.PK_CENVEID=4 " + //horeca 54 ja 64 on baasid
 " AND ( (CENA.NO_DAT <= getdate()) ) AND ( (CENA.LIDZ_DAT >= getdate()) ) )) AND (CV.TIPS IN (0)) and VAL.KODS = 'EUR' " +
 " ORDER BY   CV_NOSAUK ASC";
	
    rs = stmt.executeQuery(hsql);
    while(rs.next()){
    	   String kood = rs.getString("toode").trim();
    	   double hh = rs.getDouble("hind");
    	   	hinnad2.put(kood, hh);
    }
   	rs.close();
   	
   	
   	
	String eee = " SELECT  N.KODS AS toode,  N.NOSAUK AS stocka, VIEN.KODS AS VIEN_KODS,  VOL.DAUDZ  AS stock, " +
 " VOL.DAUDZ2 AS VOL_DAUDZ2,  ROUND(VOL.SUMMA, 6) AS VOL_SUMMA,  NOL.KODS AS NOL_KODS,  NOL.NOSAUK AS NOL_NOSAUK, " +
" '0' AS MULTISEL, N.COUNTER AS N_COUNTER, " +
" N.PK_NOM AS N_PK_NOM, NOL.PK_KLIENTS AS ladu  , VOL.PK_NOL FROM   APV06161.NNOM N LEFT OUTER JOIN (SELECT V.PK_NOL, V.PK_NOM , " +
" SUM(V.DAUDZ) AS DAUDZ, SUM(V.DAUDZ2) AS DAUDZ2, " +
" SUM(V.SUMMA) AS SUMMA,SUM(CASE ISNULL(NN.M_ATT12,0) WHEN 0 THEN CAST(NULL AS FLOAT) ELSE V.DAUDZ / NN.M_ATT12 END) AS DAUDZ12, " +
" SUM(CASE ISNULL(NN.M_ATT13,0) WHEN 0 THEN CAST(NULL AS FLOAT) ELSE V.DAUDZ / NN.M_ATT13 END) AS DAUDZ13 FROM APV06161.NVOL V, APV06161.NNOM NN ," +
" APV06161.NVOLDAT DT WHERE  V.DAT < GETDATE()  AND V.PK_NOMPART = DT.PK_NOMPART AND V.PK_NOL = DT.PK_NOL AND DT.DATB >= GETDATE() " +
"  AND V.STATUSS IN (0,1)  AND V.PK_NOM = NN.PK_NOM AND NN.ATLUZSK=1 GROUP BY V.PK_NOL,V.PK_NOM) VOL ON N.PK_NOM = VOL.PK_NOM " +
"   LEFT OUTER JOIN APV06161.DKLIENTS NOL ON VOL.PK_NOL = NOL.PK_KLIENTS LEFT OUTER JOIN APV06161.NVIEN VIEN ON N.PK_VIEN=VIEN.PK_VIEN " + 
"   WHERE ( ( VOL.DAUDZ <> 0  OR  VOL.DAUDZ2 <> 0  OR  ROUND(VOL.SUMMA, 6) <> 0)  ) AND (N.ATLUZSK = 1)  AND ( VOL.PK_NOL = 42   )" +
"    GROUP BY N.KODS,N.NOSAUK,VIEN.KODS,VOL.DAUDZ,VOL.DAUDZ2,ROUND(VOL.SUMMA, 6),NOL.KODS,NOL.NOSAUK,N.COUNTER,N.PK_NOM,NOL.PK_KLIENTS,VOL.PK_NOL " + 
 "    ORDER BY   N.KODS ASC ";
	  
   	TreeMap<String,Integer> kogused2 = new TreeMap<String,Integer>();
   	
	
    rs = stmt.executeQuery(eee);
    while(rs.next()){
    	   String kood = rs.getString("toode").trim();
    	   int hh = rs.getInt("stock");
    	   	kogused2.put(kood, hh);
    }
   	rs.close();
   	
   	
   	
   	
   	
      rs = stmt.executeQuery(cmd);
      while(rs.next()){
    	  String abi = rs.getString("abi").trim();
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimetus").trim()  ;
      
     nimetus =  Converter.enCodeEst(nimetus);
    if(kogused.get(kood) != null){
   	  prod.setStock(Double.parseDouble((String)kogused.get(kood)));
    }else{
       prod.setStock(0);
    }
    
    if(kogused2.get(kood) != null && kogused2.get(kood)> 0){
     	  prod.setPartii("Act:" + kogused2.get(kood));
      }
    
    double kast = rs.getDouble("kastis");
    if(kast > 0){
    	 prod.setInCases(kast);
    }
       
    double alusel = rs.getDouble("alusel");
    if(alusel > 0){
    	 prod.setOnPallet(alusel);
    	 }
     
       
       if(hinnad.get(kood) != null){
    	   prod.setPrice(hinnad.get(kood));
       }
        

        String ean = rs.getString("ean");
        String addx = "";
        if(ean != null && ean.trim().length()>4){
        prod.setEAN(ean.trim());
        eans.put(ean.trim(), prod.getID());
        addx = ean.trim().substring(ean.trim().length() -4);
        }
        String grp = rs.getString("grupp");
        if(grp != null){
        	  prod.setGroup(grp.trim());  
        	  if(!usedGr.contains(grp.trim())){
            	  usedGr.add(grp.trim());  
        	  }
        }
        
     /*String grpuus = rs.getString("gruppuus");
        
        if(grpuus != null){
      //	 prod.setGroupSecond(grpuus.trim());  
      	  if(!usedGr.contains(grpuus.trim())){
        //  	  usedGr.add(grpuus.trim());  
      	  }
      }
      */
        
        
        
        String riik = rs.getString("riik");
        if(riik != null){
        	  prod.setGroupSecond(riik.trim());  
        	  if(!usedGr.contains(riik.trim())){
            	  usedGr.add(riik.trim());  
        	  }
        }
       
        
        
        nimetus = nimetus.replaceAll("konfektes", "konf.");
    
      prod.setName(nimetus + " " + addx);
    //  prod.setAlegrupp("1");
      
      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit);
        }
    
      
      double minmax = rs.getDouble("minmax");
      if(minmax > 0){
    	  prod.setOptAmountInUnits(minmax);
      }
      
      
      String type = rs.getString("type");
      if(type!=null && type.trim().length()>0){
    		  prod.setType(type.trim());
    	  	}
      
      String weblink = rs.getString("weblink");
      if(weblink!=null && weblink.trim().length()>0){
    	  
         prod.weblink = weblink.trim();
    	
    	  	}
      
      
      boolean goes = true ;
       prod.setVat("21");
       
    if(goes){
    	 products.put(nimetus+ " " + kood, prod);
         usedProds.add(prod.getID());
    }
     
      }
      rs.close();
      stmt.close();
      con.close();
    } catch (Exception e){
      getParent().showError(e);
    }
	
    
    return products;
  }

 



	    

  public Vector getCustomerProductsList(String agentCode){
	    Vector custProds = new Vector();

	    int counter = 1;

	 String cmd = " SELECT  PK_ADRESE as abi, K.kods as klient,N.kods as prodid ,r.DAUDZ as qty,D.DAT_DOK AS lastdate ,max(r.CENA) as hind ,max(D.DOK_NR) as dok " + 
	"  FROM   APV06161.NDOK r LEFT OUTER JOIN APV06161.NNOM N ON r.PK_NOM=N.PK_NOM AND r.RN_VEIDS IN (0,3,5)  " +
	"  LEFT OUTER JOIN APV06161.NVIEN VIEN ON r.PK_VIEN=VIEN.PK_VIEN, APV06161.DPIRMDOK D " +
	"  LEFT JOIN APV06161.NDOKG G ON D.PK_DOK = G.PK_DOK " +
	"  LEFT JOIN APV06161.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS " +
	"  WHERE r.PK_DOK=D.PK_DOK AND D.DAT_DOK > getdate()-180 AND D.PAMV_ID = 2711 and N.kods is not null " +
	"  GROUP BY K.KODS,PK_ADRESE,N.NOSAUK,N.KODS  ,D.DAT_DOK ,r.DAUDZ " + 
	"  ORDER BY K.KODS,N.NOSAUK,N.KODS  ,D.DAT_DOK ,r.DAUDZ " ;


	//    if (isDebug()) System.out.println(cmd);

	    try {
      Connection con = this.getNewConnection();
	    Statement stmt =  con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	      counter++;
	      while(rs.next()){
	    	  String abi =  rs.getString("abi");
        	
        	if(abi != null){
                abi = 	"_" + abi.trim();
                }else{
                	abi = "";
                }
	        String kliendikood = rs.getString("klient").trim() + abi;
	        String tootekood = rs.getString("prodid").trim();
	        double kogus = rs.getDouble("qty");
	  
	        String lastdate = rs.getString("lastdate").substring(0,10);
	                 lastdate = lastdate.substring(5,10);
	                 double hind = rs.getDouble("hind");
	     	        String bill = rs.getString("dok");          
	        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,hind,0,0," ");
	        cp.setAbiKood(bill);
	        custProds.add(cp);

	      }
	      rs.close();
	      stmt.close();
	      con.close();
	    } catch (Exception e){
	      getParent().showError(e);
	      if(counter< 4){
	      	try{
	      		Connection con = this.getNewConnection();
	    	    Statement stmt =  con.createStatement();
	        	 ResultSet rs = stmt.executeQuery(cmd);
	             counter++;
	             while(rs.next()){
	               String kliendikood = rs.getString("orderaccount").trim();
	               String tootekood = rs.getString("itemid").trim();
	               String lastdate = rs.getString("lastdate").substring(0,10);
	                        lastdate = lastdate.substring(5,10);
	               CustProd cp = new CustProd(kliendikood, tootekood, 0 ,0 ,0,lastdate,0,0,0," ");

	               cp.setRedline(rs.getString("date"));
	               custProds.add(cp);
	             }
	             rs.close();
	             stmt.close();
	             con.close();
	             
	      	}catch (Exception ee){

	      	}

	      }
	    }
	    return custProds;
	  }





  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }

  public void setBulkSales(Map agents){
 
   
     String cmd =" SELECT A.PK_REISS as agent , round(SUM(summa_pv),0) AS summa " +
		 "  FROM   APV06161.DPIRMDOK D LEFT OUTER JOIN APV06161.DKLIENTS N ON D.PK_ESPATS=N.PK_KLIENTS " +
		 "  LEFT JOIN APV06161.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS " +
		 "  JOIN APV06161.NDOKG G ON D.PK_DOK = G.PK_DOK " +
		 "  LEFT OUTER JOIN (SELECT PK_DOK, MAX(LAIKS) AS LAIKS FROM APV06161.SDOKEVNT GROUP BY PK_DOK) LASTEV ON LASTEV.PK_DOK = D.PK_DOK " + 
		 "  LEFT OUTER JOIN APV06161.NADRESES A ON A.PK_ADRESE=G.PK_ADRESE, APV01454.SVALUTAS V " +
		 "   WHERE D.PK_VAL=V.PK_VAL AND  YEAR(D.DAT_DOK) = YEAR( getdate()) and  MONTH(D.DAT_DOK) = MONTH( getdate()) " +
		" AND D.PAMV_ID = 2711 AND  NOT (D.STATUSS = 4) and A.PK_REISS !='' " +
		"	 GROUP BY  A.PK_REISS  ORDER BY   A.PK_REISS ASC";

        if (isDebug()) System.out.println(cmd);
           try {
        	   Connection con =  this.getNewConnection();
               Statement stmt = con.createStatement();
               ResultSet	      rs = stmt.executeQuery(cmd);
               while(rs.next()) {
                 String agentID = rs.getString("agent").trim();
                 Agent agent = (Agent)agents.get(agentID);
                 if (agent != null){
                   agent.setMonthSales((int)rs.getDouble("summa"));
                 }
               }
               rs.close();
               con.close();
           } catch (Exception e) {
               getParent().showError(e);
            }
         }

   
    
    
    public double getFoodSales(String agentCode) throws SQLException{
      return 0;
    }

    public double getAlcoSales(String agentCode) throws SQLException{
      return 0;
    }

    public ManagerData getManagerData() throws SQLException{
      
      return null;
    }


    private TreeMap brandplans = null; 

    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
    }


  public String getStartingTime(){
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, - Integer.parseInt(getParent().getProps().getProperty("months")));
    int month = cal.get(Calendar.MONTH) + 1;
    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    String sqlDate = cal.get(Calendar.YEAR) + "-" + month + "-" + day;
    return sqlDate;
  }

  public String getDriverVersion(){
    return "HorizonTri";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent){



	       String sql2 =  "SELECT sal.orderid AS arve, sal.orderdate AS kuupaev,cus.name AS klient, count(rid.itemid) AS summa  " +
	    " FROM perinttelematable sal INNER JOIN custtable cus ON sal.CUSTACCOUNT=cus.accountnum  " +
	    " INNER JOIN perinttelemaline rid ON sal.orderid=rid.orderid  WHERE sal.dataareaid='dun' AND cus.dataareaid='dun'  AND rid.dataareaid='dun' AND cus.salesrep ='" + agent.getID() + "' AND " +
	    " sal.ORDERDATE > getdate()-0.8  and referenceno = '' GROUP BY sal.orderid, sal.orderdate , cus.name ORDER BY sal.orderdate";

	    StringBuffer sb = new StringBuffer();
	    sb.append("\n\n\n Vormistatud tellimused:\n\n");

	      try {
	    	  Connection con = this.getNewConnection();
	  	    Statement stmt =  con.createStatement();
	                  ResultSet	      rs2 = stmt.executeQuery(sql2);
	                  while (rs2.next()) {
	                  sb.append("(" + rs2.getString("arve").trim() + ") [" + rs2.getString("kuupaev").substring(0,10) + "] - " + rs2.getString("klient") + "  Ridu: " + rs2.getDouble("summa") + "\n");
	                  }
                       rs2.close();
                       stmt.close();
                       con.close();
	                  
	                  } catch (Exception e) {
	                      getParent().showError(e);
	                  }


	      return sb.toString();
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
  private String url = "jdbc:odbc:horizon";
  private   Vector debt = new Vector();
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();

	
    
    
    
   
    
	
	  private void makePictureCat(){
	  	    
	    	
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
//	         	 Lõppes kustutamine 
	  		 
	  		 TreeMap prods = this.getProducts(null);

	         StringBuffer buf = new StringBuffer();
	         Iterator enume = prods.keySet().iterator();
	    
	         res2 = new File (pictssou);
             ff2 = res2.list();
          //   System.out.println(ff2.toString());
	         
	         while (enume.hasNext()) {
	    	    	String asi = (String)enume.next();
	    	    	Product prod = (Product)prods.get(asi);
	    	    	String toode = prod.getID().trim();  
	    	    	String ean = prod.getEAN().trim(); 
	               if(toode.length() > 0){
	                	
	    	   	  res2 = new File (pictssou);
	              ff2 = res2.list();
	             
	         	 for(int i = 0;ff2.length>i;i++){
	             	if( ff2[i].startsWith(ean+".")){
	               // System.out.println("pilt olemas " + ff2[i]);
	            	File in = new File ( pictssou + ff2[i]); 
	              	if(in.isFile()){
	              	File inpda = new File ( pictspda + prod.getID()+ ".jpg");
	              	if(!inpda.exists()){
	                  	Converter.shrinkAndCopyFile(in,inpda,400);
            	    if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
	                    		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
	                    	}
            	  	}   }
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
  

	
	
	
}
