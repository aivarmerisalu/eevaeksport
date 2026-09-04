package eevaeksport.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
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

public class HorizonSmilDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter,PictureMaker {
  public HorizonSmilDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
    } catch (Exception e){
       parent.showError(e);
    }
    
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
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
  
  public void connectionUp(){
  }
  public Vector getLastThreeData() {
		String sq1 = null;
		
		Vector result = new Vector();
		// Siin loeme igale kliendile kylge viimase viie korra ostud
		try {
			Connection con = getNewConnection();
			Statement st = con.createStatement();


	    	String cmd2 = "SELECT K.NOSAUK AS nimi, K.PK_KLIENTS AS id ,A.PK_ADRESE AS aid , K.KODS AS kood,K.REG_NR as payer,K.ADRESE as address ,A.ADRESE as address2, K.PK_PARD as agent , " +
	    " K.APMTERM as paym ,K.KREDLIM as credit, K.LIC_NR as licence ,K.LIC_IZDAT as lepalg , " +
	    " K.TELEFONS as phone,K.MOBTEL as mphone ,K.EPASTS as email " +
	    " FROM   APV07062.DKLIENTS K LEFT OUTER JOIN  APV07062.NADRESES a ON A.PK_KLIENTS=K.PK_KLIENTS and a.LEG_ADR = 2 LEFT OUTER JOIN APV07062.DGRUPAS GRU ON K.PK_GRUPA = GRU.PK_GRUPA  " +
	    " WHERE (K.STATUSS <> 2 AND K.PK_DOKT IS NOT NULL AND ( NOT K.PAMV_ID IN (1810,11970))) AND " +
	    " (( ( (K.TIPS = 3)  OR  (K.TIPS = 4)  OR  (K.TIPS = 6)  OR  (K.TIPS = 8) ) AND ( (K.STATUSS = 0)  OR " +
	    "  (K.STATUSS = 1) ) AND ( (K.STATUSS = 0) ) AND ( (K.TIPS <> 8) ) AND ( (K.DBKRPAZ = 1) ) )) " +
	    " ORDER BY   K.NOSAUK ASC" ;

			//if (isDebug())
				//System.out.println(cmd2);
			ResultSet rs = st.executeQuery(cmd2);

			while (rs.next()) {

			        String abi  =  rs.getString("aid");
			        String aa = "";
			        
			        	 
			        if(abi != null){
			        	if(abi.length() > 0){
				        	aa = " and g.PK_ADRESE = " + abi.trim() ;
				        }
			        abi = 	"_" + abi.trim();
			        }else{
			        	abi = "";
			        }
			        String kood = rs.getString("kood").trim();
			        String accountnum = kood +  abi;
		        
			 sq1 = " select top 5 D.DAT_DOK as kuupaev from  APV07062.DPIRMDOK D LEFT JOIN APV07062.NDOKG G ON D.PK_DOK = G.PK_DOK  LEFT JOIN APV07062.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS where " + 
	                         " K.kods ='" + kood +  "'" + aa + " and D.DAT_DOK > getdate()-180 " +
	                         " group by K.kods,g.PK_ADRESE ,D.DAT_DOK  order by K.kods,g.PK_ADRESE , D.DAT_DOK desc";
				Connection con2 = getNewConnection();
				Statement st2 = con2.createStatement();

				ResultSet rs2 = st2.executeQuery(sq1);
				while (rs2.next()) {
					Date kuup = rs2.getDate("kuupaev");
					String sq3 = "SELECT  g.PK_ADRESE as abi, K.kods as klient,N.kods as toode ,r.DAUDZ as kogus,(D.DOK_NR) as doknr " +
	 " FROM   APV07062.NDOK r LEFT OUTER JOIN APV07062.NNOM N ON r.PK_NOM=N.PK_NOM AND r.RN_VEIDS IN (0,3,5) " +
	 " LEFT OUTER JOIN APV07062.NVIEN VIEN ON r.PK_VIEN=VIEN.PK_VIEN, APV07062.DPIRMDOK D " +
	 " LEFT JOIN APV07062.NDOKG G ON D.PK_DOK = G.PK_DOK " +
	 " LEFT JOIN APV07062.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS " + 
	 " WHERE r.PK_DOK=D.PK_DOK AND D.DAT_DOK  = ? and  K.kods ='" + kood + "'" +  aa +
	 " and r.DAUDZ != 0 and N.kods is not null ORDER BY N.NOSAUK,N.KODS  ,D.DAT_DOK ,r.DAUDZ ";
					Connection con3 = getNewConnection();
					PreparedStatement st3 = con3.prepareStatement(sq3);
					st3.setTimestamp(1, new java.sql.Timestamp(kuup.getTime()));
					ResultSet rs3 = st3.executeQuery();
					while (rs3.next()) {
						MatrixLine ml = new MatrixLine();
						ml.setCustID(accountnum);
						try{
							ml.setProdID(rs3.getString("toode").trim());
							ml.setPrice(rs3.getDouble("kogus"));
							ml.setStartDate(kuup + "");
							ml.setLineinfo(rs3.getString("doknr").trim());
							//System.out.println("L5 "  + ml.getStringForLastThree());
							
							result.add(ml);	
						}catch(Exception r ){
							r.printStackTrace();
						}
						
					}
					rs3.close();
					st3.close();
					con3.close();
				}
				rs2.close();
				st2.close();
				con2.close();

			}
		} catch (Exception e) {
			System.out.println("L5 "  + sq1);
			
			e.printStackTrace();
		}
		System.out.println("L5 "  + result.size());
		
		return result;
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
	  
	String sql = " SELECT M.PK_PIESKM , (CASE WHEN M.PK_DOKT IS NULL THEN 'VISI' ELSE 'TIPS'END) AS DOK_DAL , T.KODS AS T_KODS , T.NOSAUK AS T_NOSAUK , "+
	  " (CASE WHEN M.PK_NOMGR IS NULL THEN (CASE WHEN M.PK_NOMPGR IS NULL THEN (CASE WHEN M.PK_NOM IS NULL THEN 'VISAS' ELSE 'NOMENKLATūRA' END) ELSE 'PAPILDGRUPA' END) ELSE 'GRUPA' END) AS tooteasi "+
	   " , (CASE WHEN M.PK_NOMGR IS NULL THEN (CASE WHEN M.PK_NOMPGR IS NULL THEN N.KODS ELSE NPGR.KODS END) ELSE NGR.KODS END) AS tkood ,"+
		 "(CASE WHEN M.PK_NOMGR IS NULL THEN (CASE WHEN M.PK_NOMPGR IS NULL THEN N.NOSAUK ELSE NPGR.NOSAUK END) ELSE NGR.NOSAUK END) AS N_NOSAUK ,"+
		"  (CASE WHEN M.PK_KLGR IS NULL THEN (CASE WHEN M.PK_GRUPA IS NULL THEN (CASE WHEN M.PK_KLIENTS IS NULL THEN 'VISI' ELSE 'KLIENTS' END) ELSE 'GRUPA'END) ELSE 'PAPILDGRUPA' END) AS kliendiasi , "+
		"  (CASE WHEN M.PK_KLGR IS NULL THEN (CASE WHEN M.PK_GRUPA IS NULL THEN KL.KODS ELSE DGR.KODS END) ELSE KGR.KODS END) AS kkood , "+
	"  (CASE WHEN M.PK_KLGR IS NULL THEN (CASE WHEN M.PK_GRUPA IS NULL THEN KL.NOSAUK ELSE DGR.NOSAUK END) ELSE KGR.NOSAUK END) AS K_NOSAUK , "+
	"	   M.SVARS , M.COUNTER , R.LIKME as discount , R.SAK_DAT as algus , R.BEIG_DAT as lopp  FROM APV07062.NPIESKM M LEFT OUTER JOIN (SELECT SDOKTIP.PK_DOKT , SDOKTIPSHARED.PK_DOKTSH , SDOKTIPSHARED.KODS , SDOKTIPSHARED.NOSAUK "+ 
"	FROM APV07062.SDOKTIP , APV07062.SDOKTIPSHARED WHERE SDOKTIP.PK_DOKTSH = SDOKTIPSHARED.PK_DOKTSH) T ON M.PK_DOKT = T.PK_DOKT "+
"	LEFT OUTER JOIN APV07062.NNOMGR NGR ON M.PK_NOMGR = NGR.PK_NOMGR LEFT OUTER JOIN APV07062.NNOMPGR NPGR ON M.PK_NOMPGR = NPGR.PK_NOMPGR "+
"	LEFT OUTER JOIN APV07062.NNOM N ON M.PK_NOM = N.PK_NOM LEFT OUTER JOIN APV07062.DKLGR KGR ON M.PK_KLGR = KGR.PK_KLGR "+
"	LEFT OUTER JOIN APV07062.DGRUPAS DGR ON M.PK_GRUPA =  DGR.PK_GRUPA LEFT OUTER JOIN APV07062.DKLIENTS KL ON M.PK_KLIENTS = KL.PK_KLIENTS"+
"	LEFT OUTER JOIN (SELECT * FRoM APV07062.NPIESKR WHERE SAK_DAT <= getdate() AND BEIG_DAT >= getdate()) R ON M.PK_PIESKM = R.PK_PIESKM "
+ " where  (CASE WHEN M.PK_NOMGR IS NULL THEN (CASE WHEN M.PK_NOMPGR IS NULL THEN N.KODS ELSE NPGR.KODS END) ELSE NGR.KODS END)"
+ " is not null  and "
+ "(CASE WHEN M.PK_KLGR IS NULL THEN (CASE WHEN M.PK_GRUPA IS NULL THEN KL.KODS ELSE DGR.KODS END) ELSE KGR.KODS END) is not null  ORDER BY KL.KODS ASC  ";

	  Statement st = null;
      try {
    	  
        connectionUp();
        Connection       con = getNewConnection();
        
        st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String tasi = rs.getString("tooteasi").trim();
          String prodID = rs.getString("tkood").trim();
          String kasi = rs.getString("kliendiasi").trim();
          String custID = rs.getString("kkood").trim();
          double discount = rs.getDouble("discount");
          String algus = rs.getString("algus");
          String lopp = rs.getString("lopp");
          
          MatrixLine mline = new MatrixLine();

          if (lopp != null) {
				//mline.setEndDate(lopp.substring(0, 10));
			}
			if (algus != null) {
				//mline.setStartDate(algus.substring(0, 10));
			}
			
          if (kasi.equals("KLIENTS") && custID != null && custID.trim().length() > 0) {
            mline.setCustID(custID.trim());
          }
          
          if (!kasi.equals("KLIENTS") && custID != null && custID.trim().length() > 0) {
                mline.setCliGroup(custID.trim());
          }

          if (prodID != null && prodID.trim().length() > 0) {
           // mline.setProdID(prodID.trim());
          }
          if (tasi.indexOf("GRUPA") != -1 &&  prodID != null && prodID.trim().length() > 0) {
            mline.setProdGroup(prodID.trim());
          }
            mline.setDiscount(discount);

         lines.add(mline);
        
        }

        rs.close();
        st.close();
        this.connectionDown();
      }
      catch (SQLException ex) {
    	  ex.printStackTrace();
      }
  System.out.println("Pmatrix "+ lines.size());	
	
	  return lines ;

  }
	  
	   // jätame meelde yldised aled et saaks summeerida 
	  
	  
    
  
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
		  
	   String  cmd = "  SELECT  NN.KODS as toode , SUM(CASE WHEN V.STATUSS IN (0, 1, 3) THEN V.DAUDZ ELSE 0 END) AS stock,  " +
	"	SUM(CASE WHEN V.STATUSS IN (0, 1) THEN V.DAUDZ ELSE 0 END) AS DAUDZ, SUM(CASE WHEN V.STATUSS IN (0, 1) THEN V.DAUDZ2 ELSE 0 END) AS DAUDZ2, " + 
	"	SUM(CASE WHEN V.STATUSS IN (0, 1) THEN V.SUMMA ELSE 0 END) AS SUMMA,"
	+ " SUM(CASE WHEN V.STATUSS IN (0, 1) THEN V.SUMMA2V ELSE 0 END) AS SUMMA2V ,V.PK_NOL as ladu  " +
	"	FROM APV07062.NVOL V, APV07062.NNOM N7, APV07062.NNOMGLIM NGL7, APV07062.NNOM NN , APV07062.NVOLDAT DT WHERE N7.PK_NOM = V.PK_NOM AND N7.PK_NOMGR = NGL7.PK_CHILD " +
	"	AND  V.DAT < getdate()  AND V.PK_NOMPART = DT.PK_NOMPART AND V.PK_NOL = DT.PK_NOL AND DT.DATB >= getdate()  AND V.STATUSS IN (0,1,3) " +
	"	AND V.PK_NOM = NN.PK_NOM AND V.PK_NOL = '15' and NN.ATLUZSK=1  GROUP BY V.PK_NOL, NN.KODS " +
	"	ORDER BY V.PK_NOL, NN.KODS";


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
	               products.append(getParent().getNF().format(seis) + ";" + dealer  +  "\n"); // + dealer +  "\n");
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

  
  public DealerProduct getDealerProductsList() {
       return null;
     }

  
  
  
  public TreeMap getAgents(){
	  
	  
    String cmd = "  SELECT M.PK_PARD AS agent, M.NOSAUK AS nimi  FROM  APV07062.NPARD M  WHERE "
    		+ "M.PAMVID IN (2110) AND M.STATUSS = 1 ";
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
          agent.setName(name + " " + agent.getID());
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
    
   

  //  this.setBulkSales(agents1);
    return agents;
  }
  
  private HashMap getTaraSaldo(){
	  
  HashMap saldo = new HashMap();
  
  String sql = "SELECT    N.KODS AS N_KODS, N.NOSAUK AS N_NOSAUK, " + 
" SUM(CASE WHEN D.PAMV_ID = 2720 THEN -NDOK.DAUDZ ELSE NDOK.DAUDZ END) AS NDOK_DAUDZ, G.PK_ADRESE AS G_PK_ADRESE, A.ADRESE AS AdreseKlasifikatora,G.ADRESE AS AdreseArRoku, " + 
 " K.PK_KLIENTS AS K_PK_KLIENTS, K.KODS AS K_KODS," + 
" K.NOSAUK AS K_NOSAUK,  NDOK.PK_NOM AS NDOK_PK_NOM " + 
" FROM   APV07062.NDOK NDOK LEFT OUTER JOIN APV07062.NNOM N ON NDOK.PK_NOM=N.PK_NOM AND NDOK.RN_VEIDS IN (0,3,5) LEFT OUTER JOIN APV07062.NVIEN VIEN ON NDOK.PK_VIEN=VIEN.PK_VIEN, " + 
"  APV07062.DPIRMDOK D LEFT JOIN APV07062.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS JOIN APV07062.NDOKG G ON D.PK_DOK = G.PK_DOK  LEFT OUTER JOIN APV07062.NADRESES A ON A.PK_ADRESE=G.PK_ADRESE," + 
"  (SELECT SDOKTIP.PK_DOKT,SDOKTIP.PK_SHEMA,SDOKTIP.PK_ATSKF,SDOKTIP.DEF_ATTR,SDOKTIP.PK_RULE,SDOKTIP.DEL_GRAM,SDOKTIP.AKTIVS,SDOKTIP.CHK_DDATE,SDOKTIP.SECIBA,SDOKTIP.PK_NUM,SDOKTIP.PK_SAISDOKT, " + 
"  SDOKTIP.COPY_GRAM,SDOKTIP.PK_ATLAUJ,SDOKTIP.SELPRNF,SDOKTIP.PK_DIMSCHEM,SDOKTIP.AUTDIMK,SDOKTIP.REZBUDZ,SDOKTIPSHARED.COUNTER,SDOKTIPSHARED.PK_DOKTSH,SDOKTIPSHARED.ID,SDOKTIPSHARED.KODS,SDOKTIPSHARED.NOSAUK, " + 
"  SDOKTIPSHARED.DBKR_TIPS,SDOKTIPSHARED.CAN_GRAM " + 
"  FROM APV07062.SDOKTIP,APV07062.SDOKTIPSHARED " + 
"  WHERE SDOKTIP.PK_DOKTSH = SDOKTIPSHARED.PK_DOKTSH) T  " + 
"     WHERE (NDOK.PK_DOK=D.PK_DOK) AND (D.PK_DOKT=T.PK_DOKT) AND (( ( ( ((D.DAT_DOK >= '20000201') AND (D.DAT_DOK < (GETDATE()-1))) ) ) " + 
 " AND ( ( ( EXISTS (SELECT 1  FROM APV07062.NNOMSAGR NPGR " + 
"  WHERE NDOK.PK_NOM=NPGR.PK_NOM AND NPGR.PK_NOMPGR = 7)) ) ) AND ( ( (T.ID <> 4602) ) ) AND ( ( (T.ID <> 2713) ) ) AND ( ( (T.ID <> 2737) ) ) AND ( ( (K.DBKRPAZ = 1) ) ) )) " + 
" GROUP BY N.KODS, N.NOSAUK, K.PK_KLIENTS, K.KODS, K.NOSAUK, VIEN.KODS, NDOK.PK_NOM,G.PK_ADRESE,A.ADRESE,G.ADRESE " + 
 " ORDER BY   K_nosauk,n_kods ASC" ;
	ResultSet rs1 ;
	Statement stmr ;
	
	try{
		
    	Connection con = this.getNewConnection(); 
    		 stmr = con.createStatement();
        	
             rs1 = stmr.executeQuery(sql);
            
             while(rs1.next()){
            	 
         //N_KODS N_NOSAUK NDOK_DAUDZ  G_PK_ADRESE AdreseKlasifikatora AdreseArRoku K_PK_KLIENTS K_KODS K_NOSAUK  NDOK_PK_NOM
            	 

            	String tnimi = rs1.getString("N_NOSAUK").trim();
            	double kogus = rs1.getDouble("NDOK_DAUDZ");
            	if(kogus != 0){
            		
            		String kkood = rs1.getString("G_PK_ADRESE");
            		
            		String akood = rs1.getString("K_KODS").trim();
            		String add = "";
            		if(kogus<0) {
            			add = " !!! ";
            		}
            		
            		String rida = "[" + add + (int)kogus + add + "] " + tnimi + "|"; 
            		
            		if(kkood != null){
            			akood = akood+ "_"+ kkood.trim();
            		}
            		
                	if(saldo.get(akood) != null){
                		saldo.put(akood , saldo.get(akood) + rida);
                	}else{
                		saldo.put(akood, rida);
                	}
            			
            	}
            	
            }
            
           rs1.close();
           stmr.close();
           con.close();
    	}catch(Exception s){
    		s.printStackTrace();
    	}
	
  
  
  return saldo ;
  
  }

  public TreeMap getCustomers(Agent agent){
	  
 
    String salessql = " SELECT K.KODS as klient ,A.PK_ADRESE as aid, round(SUM(summa_pv),0) AS summa,str(datepart(YYYY,D.DAT_DOK))+' '+str(datepart(mm,D.DAT_DOK)) as kuu " +
		 "  FROM   APV07062.DPIRMDOK D LEFT OUTER JOIN APV07062.DKLIENTS N ON D.PK_ESPATS=N.PK_KLIENTS " +
		 "  LEFT JOIN APV07062.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS " +
		 "  JOIN APV07062.NDOKG G ON D.PK_DOK = G.PK_DOK " +
		 "  LEFT OUTER JOIN (SELECT PK_DOK, MAX(LAIKS) AS LAIKS FROM APV07062.SDOKEVNT GROUP BY PK_DOK) LASTEV ON LASTEV.PK_DOK = D.PK_DOK " +
		 "  LEFT OUTER JOIN APV07062.NADRESES A ON A.PK_ADRESE=G.PK_ADRESE,  APV08682.SVALUTAS V " +
		 "   WHERE D.PAMV_ID IN (2710,2720,2711,2721,2712,2786,2713,2737,2718,2728,18212,2745,2782,24501,2797,2798,2739,18086,2736,2783,2799,18091,15008,15011,2738,2792,2795,2742,2793,2796)" +
		"	AND D.PK_VAL=V.PK_VAL AND  D.DAT_DOK > getdate()-120 " +
		"	 AND D.PAMV_ID = 2711 AND  NOT (D.STATUSS = 4) and K.KODS is not null " +
		"	 GROUP BY K.KODS,A.PK_ADRESE,str(datepart(YYYY,D.DAT_DOK))+' '+str(datepart(mm,D.DAT_DOK)) " +
		"	  ORDER BY  K.KODS ASC " ;
  	


 

 String sqldebt = "	  select k.KODS AS klient,sum(D.SUMMA_APMP-D.SUMMA_PIESP) AS tasumata FROM   APV07062.DPIRMDOK  d " +
   " LEFT JOIN APV07062.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS where  "
   + "(D.SUMMA_PIESP-D.SUMMA_APMP)!=0 AND d.APM_TERM < getdate()+1  and APM_TERM > getdate()-300  AND d.STATUSS !=4" +
	" group by k.KODS ";
 
 String sqldebtall = "	  select k.KODS AS klient,sum(D.SUMMA_APMP-D.SUMMA_PIESP) AS tasumata FROM   APV07062.DPIRMDOK  d " +
		   " LEFT JOIN APV07062.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS where  (D.SUMMA_PIESP-D.SUMMA_APMP)!=0   and APM_TERM > getdate()-300"
		   + "  AND d.STATUSS !=4  group by k.KODS ";
 
 //    tal_visitationdays ,talspartnercode,

 int cou = 0;
 
 TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    Vector payers =  new Vector();
    
    HashMap<String, Double> overd = new HashMap();

    HashMap<String, Double> alld = new HashMap();
    HashMap salesres = new HashMap();
    HashMap tarares =  getTaraSaldo();
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

    	
    	 

    	String  cmd = "  SELECT\r\n" + 
    			"    K.NOSAUK AS nimi,\r\n" + 
    			"    K.PK_KLIENTS AS id,\r\n" + 
    			"    A.PK_ADRESE AS aid,\r\n" + 
    			"    K.KODS AS kood,\r\n" + 
    			"    K.REG_NR AS payer,\r\n" + 
    			"    K.ADRESE AS address,\r\n" + 
    			"    A.ADRESE AS address2,\r\n" + 
    			"    K.PK_PARD AS agent,\r\n" + 
    			"    K.APMTERM AS paym,\r\n" + 
    			"    K.KREDLIM AS credit,\r\n" + 
    			"    K.LIC_NR AS licence,\r\n" + 
    			"    K.LIC_IZDAT AS lepalg,\r\n" + 
    			"    K.TELEFONS AS phone,\r\n" + 
    			"    K.MOBTEL AS mphone,\r\n" + 
    			"    K.EPASTS AS email\r\n" + 
    			"FROM APV07062.DKLIENTS K\r\n" + 
    			"LEFT JOIN APV07062.NADRESES A\r\n" + 
    			"    ON A.PK_KLIENTS = K.PK_KLIENTS\r\n" + 
    			"    AND A.LEG_ADR = 2\r\n" + 
    			"WHERE K.STATUSS = 0\r\n" + 
    			"  AND K.PK_DOKT IS NOT NULL\r\n" + 
    			"  AND K.PAMV_ID NOT IN (1810, 11970)\r\n" + 
    			"  AND K.TIPS IN (3, 4, 6)\r\n" + 
    			"  AND K.DBKRPAZ = 1\r\n" + 
    			"ORDER BY K.NOSAUK ASC;" ;
     
    //  if (isDebug()) System.out.println(cmd);
       
    	
    	
    	Connection con = this.getNewConnection();
    	Statement stmt =con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      
      while(rs.next()){


      	cou++;
    	  String name = (rs.getString("nimi").trim());
        String abi  =  rs.getString("aid");
        
        if(abi != null){
        abi = 	"_" + abi.trim();
        }else{
        	abi = "";
        }
        String kood = rs.getString("kood").trim();
        String accountnum = kood +  abi;
        Customer cust = new Customer(accountnum);
        cust.setPayer(kood);
        
        if(salesres.get(accountnum) != null){
        	cust.setInfo("CHART Sales :|Year Month|Sales EUR|" + salesres.get(accountnum) );
        }
        
       // tarares.put(accountnum, "Tara saldo|no tara until | Armands makes it |  available");
        
        if(tarares.get(accountnum) != null){
        	cust.addToCustDataVec(accountnum+ ";tarasaldo; ;Tara Saldo |"   +  tarares.get(accountnum));
        }
        
        if(overd.get(kood) != null){
        	cust.setDebt(overd.get(kood));
        }
        
        if(alld.get(kood) != null){
        	cust.setUsedCredit(alld.get(kood));
        }
        
        cust.restName = name;
        
        name = Converter.enCodeEst(name);
       
        cust.setName(name);
         
        String payer = rs.getString("payer");
        if(payer != null && payer.trim().length() > 0){
        	//cust.setPayer(payer.trim());
        }else{
        	//cust.setPayer(accountnum);
        }
       
 
        
     //  String aadr = rs.getString("address");
       String aadr = rs.getString("address2");
       
 
        if (aadr != null) {
        	String asi = aadr.trim();
        	if(asi.toLowerCase().indexOf("latvija") != -1){
        		try{
        			asi = asi.substring(0,asi.toLowerCase().indexOf("latvija")-1).trim();
           	}catch (Exception k){
        		}
        	}
         	 name = name + " " + Converter.enCodeEst(asi.replace('\n',' '));
             cust.setAddress(Converter.adressLink(Converter.enCodeEst(asi.replace('\n',' ')),"Latvia").
          		  replaceAll("\\r\\n|\\r|\\n", " "));
        	
        	   int koma = asi.lastIndexOf(",");
               if(koma != -1){
            	   String indeks = asi.substring(koma+1);
                   
                   if (indeks != null && indeks.length() > 0){
                     cust.restZipCode = indeks.trim();
                   }
                   asi = asi.substring(0, koma);
                   koma = asi.indexOf(",");
                   if(koma != -1){

                   	String tanav = asi.substring(0,koma);
                       if (tanav != null && tanav.length() > 0){
                         cust.restStreet = tanav.trim();
                       }    	   
                       asi = asi.substring(koma+1);
                       
                     try{
                    	 String linn = asi.substring(0 ,asi.length()-1);
                         if (linn != null && linn.length() > 0){
                           cust.restCity = linn.trim();
                         } 
                     }catch(Exception k){
                    	 
                     }
                   }
               }
        }
        	

        name = name + " " + accountnum;
        cust.setName(name);
        
        String agen = rs.getString("agent");
        if (agen != null) {
        	
        	//paneme kõigile kõik kylge 
        	
      Iterator itera =  this.getAgents1().keySet().iterator();
      
      while (itera.hasNext()){
    	  Agent agg = ((Agent)this.getAgents1().get(itera.next()));
    	  cust.addAgent(agg);
      }
          cust.addAgent(new Agent(agen.trim())); 
         
          
          //callcentri tytrikud saavad kõik kätte 
          
          if (agen != null && agen.trim().length()>0){
				Agent agg = ((Agent)this.getAgents1().get(agen.trim()));
				
				
				if(agg != null && agg.getDubbler().size()>0){
					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
						cust.addAgent(new Agent((String)agg.getDubbler().elementAt(i)));
					}
				}
			}
          
        }
        
        if(cou < 200) {
            cust.addAgent(new Agent("93")); 
        }
       
       // cust.addAgent(new Agent("1")); 
      
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
        
          
        boolean laks = true;
                    if((payer == null || payer.trim().length() == 0) && payers.contains(cust.getID())){
                    	//laks = false;
                    }
                    
                    if(laks){
                        customers.put(name, cust);
                        custMap.put(cust.getID(),cust);	
                    }
      }
      
      

      String sql = "	  select k.KODS AS klient,d.DOK_NR AS number,d.DOK_NR2 AS number2 ,d.DOK_SERIJA  as seria,APM_TERM AS maksepaev,(D.SUMMA_APMP-D.SUMMA_PIESP) AS tasumata, " +
      "  DAT_DOK AS kuupaev , datediff(day,APM_TERM,getdate()) AS yle FROM   APV07062.DPIRMDOK  d  " +
       "  LEFT JOIN APV07062.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS where "
       + " (D.SUMMA_PIESP-D.SUMMA_APMP)!=0 and APM_TERM > getdate()-300    AND d.STATUSS !=4 ";
                   
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
    return DriverManager.getConnection(getParent().getProps().getProperty("dsn.url"), getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

    public String getDistribution() {
        return null;
      }


  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
     
    String sql = " SELECT  G.PK_NOMGR AS kood, G.KODS AS abi, G.NOSAUK AS nimi  FROM   APV07062.NNOMGR G  " + 
    		" LEFT OUTER JOIN APV07062.NNOMGR VGR ON G.PK_GRUPA=VGR.PK_NOMGR  WHERE G.STATUSS = 0 and VGR.PK_NOMGR = 8 ORDER BY G.NPK ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("kood").trim();
      String sort = rs.getString("abi").trim();
      String nimi = rs.getString("nimi").trim();
      if(sort.length() == 1){
    	  sort = "0" + sort;
      }
        tm.put(liik, sort + "_" + Converter.enCodeEst(nimi));
    }
   
    rs.close();
    st.close();
    con.close();
    return tm;
}

  
  public TreeMap getProdGroupsSecond() throws SQLException {
    TreeMap   tm = new TreeMap();
    return tm;
  }

  public TreeMap getAgents1() {
		return agents1;
	}

  private TreeMap agents1 = new TreeMap();


  
  public TreeMap getProducts(Agent agent){

	  String cmd = " select t.KODS as  itemid,t.NOSAUK  AS nimetus, t.M_ATT12 AS kastis,1 AS alusel,   bar.BAR_KODS AS ean ,PK_NOMGR AS grupp,yh.kods As unit,1/M_ATT13 as liter \r\n" + 
	  		"	 from APV07062.NNOM  t LEFT OUTER JOIN APV07062.NBARKODS bar ON bar.PK_NOM=t.PK_NOM AND bar.IS_MAIN = 1 \r\n" + 
	  		"	 LEFT OUTER JOIN APV07062.NVIEN yh ON t.PK_VIEN=yh.PK_VIEN \r\n" + 
	  		"	  where  t.STATUSS = 0 and bar.BAR_KODS != ''";
    
	  
    getSaveStockInfo();
    
   // if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
   
    try {
    	
        String hsql =  "  SELECT  N.KODS AS toode, CENA.CENA AS hind " +
    		 " FROM   APV07062.NCEN CENA, APV07062.NNOM N, APV07062.NCENVEID CV, APV08682.SVALUTAS VAL  WHERE  " +
    		 " (CENA.PK_NOM=N.PK_NOM) AND (CENA.PK_CENVEID=CV.PK_CENVEID) AND  " +
    		 " (CENA.PK_VAL=VAL.PK_VAL) AND (( ( (N.STATUSS = 0) ) AND ( (CV.STATUSS = 1) ) AND "
    		 + "( (CENA.PK_CENVEID = 4) ) AND ( (CENA.NO_DAT <= getdate()+1) ) AND  " +
    		 " ( (CENA.LIDZ_DAT >= getdate()-1) ) )) AND (CV.TIPS IN (0))  and VAL.KODS = 'EUR'";
 	
    	
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
    
    
    hsql =  "  SELECT   N.KODS AS toode, N.NOSAUK AS N_NOSAUK, CENA.CENA AS hind, VAL.KODS AS VAL_KODS, CV.NOSAUK AS CV_NOSAUK, '0' AS MULTISEL, N.PK_NOM AS N_PK_NOM, CV.TIPS AS CV_TIPS, CENA.PK_CEN AS CENA_PK_CEN, " + 
" CENA.COUNTER AS CENA_COUNTER ,CENA.NO_DAT,CENA.LIDZ_DAT ,CENA.PK_CENVEID " +
" FROM   APV07062.NCEN CENA, APV07062.NNOM N, APV07062.NCENVEID CV, APV08682.SVALUTAS VAL  " +
" WHERE (CENA.PK_NOM=N.PK_NOM) AND (CENA.PK_CENVEID=CV.PK_CENVEID) AND (CENA.PK_VAL=VAL.PK_VAL) AND (( ( (N.STATUSS = 0) ) " +
" AND ( (CV.STATUSS = 1) ) " +
" AND ( (CENA.PK_CENVEID = 1) or CENA.PK_CENVEID=16 ) " +
" AND ( (CENA.NO_DAT <= getdate()) ) AND ( (CENA.LIDZ_DAT >= getdate()) ) )) AND (CV.TIPS IN (0)) " +
" ORDER BY   VAL_KODS ASC";
	
    rs = stmt.executeQuery(hsql);
    while(rs.next()){
    	   String kood = rs.getString("toode").trim();
    	   double hh = rs.getDouble("hind");
    	   	hinnad2.put(kood, hh);
    }
   	rs.close();
      rs = stmt.executeQuery(cmd);
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimetus").trim()  ;
      
     nimetus =  Converter.enCodeEst(nimetus);
    if(kogused.get(kood) != null){
    	prod.setStock(Double.parseDouble((String)kogused.get(kood)));
    }else{
    	//prod.setStock(0);
    }
       prod.setInCases(rs.getDouble("kastis"));
    //   prod.setOnPallet(rs.getDouble("alusel"));
       
       if(hinnad.get(kood) != null){
    	   prod.setPrice(hinnad.get(kood));
       }
        

        String ean = rs.getString("ean");
        if(ean != null && ean.trim().length()>4){
        prod.setEAN(ean);
        }
        String grp = rs.getString("grupp");
        if(grp != null){
        	  prod.setGroup(grp.trim());  	
        }
        
        if(hinnad2.get(kood) != null){
        	if(hinnad2.get(kood) != prod.getPrice()){
        	 	   nimetus = nimetus + " !pr:" +(hinnad2.get(kood) + "€!");
        	}
        }
        
      prod.setName(nimetus + " " + kood);
      prod.setAlegrupp("1");
      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit);
        }
      double liter = rs.getDouble("liter");
      
      if (liter > 0){
    	  prod.setInUnit(liter);
      }
      
       prod.setVat("21");
    
      products.put(nimetus, prod);
      
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
	    //if (true) return custProds ;
	 int counter = 1;

	 String cmd = " SELECT  PK_ADRESE as abi, K.kods as klient,N.kods as prodid ,r.DAUDZ as qty,D.DAT_DOK AS lastdate ,max(r.CENA) as hind ,max(D.DOK_NR) as dok " +
	 " FROM   APV07062.NDOK r LEFT OUTER JOIN APV07062.NNOM N ON r.PK_NOM=N.PK_NOM AND r.RN_VEIDS IN (0,3,5)  " +
	 " LEFT OUTER JOIN APV07062.NVIEN VIEN ON r.PK_VIEN=VIEN.PK_VIEN, APV07062.DPIRMDOK D " +
	 " LEFT JOIN APV07062.NDOKG G ON D.PK_DOK = G.PK_DOK " +
	 " LEFT JOIN APV07062.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS "+
	 " WHERE r.PK_DOK=D.PK_DOK AND D.DAT_DOK > getdate()-180 AND D.PAMV_ID = 2711 and N.kods is not null " +
	 " GROUP BY K.KODS,PK_ADRESE,N.NOSAUK,N.KODS  ,D.DAT_DOK ,r.DAUDZ " +
	 " ORDER BY K.KODS,N.NOSAUK,N.KODS  ,D.DAT_DOK ,r.DAUDZ " ;


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
 
   
     String cmd ="  SELECT K.PK_PARD as agent , round(SUM(summa_pv),0) AS summa " +
		"   FROM   APV07062.DPIRMDOK D LEFT OUTER JOIN APV07062.DKLIENTS N ON D.PK_ESPATS=N.PK_KLIENTS " +
		"   LEFT JOIN APV07062.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS " +
		"   JOIN APV07062.NDOKG G ON D.PK_DOK = G.PK_DOK " +
		"   LEFT OUTER JOIN (SELECT PK_DOK, MAX(LAIKS) AS LAIKS FROM APV07062.SDOKEVNT GROUP BY PK_DOK) LASTEV ON LASTEV.PK_DOK = D.PK_DOK " +
		"   LEFT OUTER JOIN APV07062.NADRESES A ON A.PK_ADRESE=G.PK_ADRESE, APV08682.SVALUTAS V " +
		"    WHERE D.PAMV_ID IN (2710,2720,2711,2721,2712,2786,2713,2737,2718,2728,18212,2745,2782,24501,2797,2798,2739,18086,2736,2783,2799,18091,15008,15011,2738,2792,2795,2742,2793,2796) " +
		"	AND D.PK_VAL=V.PK_VAL AND  YEAR(D.DAT_DOK) = YEAR( getdate()) and  MONTH(D.DAT_DOK) = MONTH( getdate())  " +
		"	 AND D.PAMV_ID = 2711 AND  NOT (D.STATUSS = 4) and K.PK_PARD !='' " +
		"	 GROUP BY K.PK_PARD " +
		"	  ORDER BY  K.PK_PARD ASC";

      //  if (isDebug()) System.out.println(cmd);
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
    return "HorizonSmil";
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
       
  private   Vector debt = new Vector();
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();

	@Override
	public void PictureMaker() {
		makePictureCat();
		 System.exit(0);
		
	}
	
		
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
	    
	         while (enume.hasNext()) {
	    	    	String asi = (String)enume.next();
	    	    	Product prod = (Product)prods.get(asi);
	    	    	String toode = prod.getID().trim();  
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
	    

	
	
	
}
