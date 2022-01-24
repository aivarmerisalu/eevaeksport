/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.CallableStatement;
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
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class SkaiteVigestaDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Discounter,FixReporter{


    public SkaiteVigestaDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
	// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();
	  
	} catch (Exception e) {
	    parent.showError(e);
	}
	  user =  getParent().getProps().getProperty("rest.user");
      pass = getParent().getProps().getProperty("rest.pass");
      rcompany = getParent().getProps().getProperty("rest.company");
      rcompanyx = getParent().getProps().getProperty("rest.companyx");
    }

    String user =  null;
    String pass = null;
    String rcompany = null;
    String rcompanyx = null;
    

    
    public String getProductSortingData(){
       	return null;
       }
	
    
    
	private Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(url, getParent().getProps()
				.getProperty("dsn.user"),
				getParent().getProps().getProperty("dsn.pass"));
	}
	
	  
	  public String getDateTimeStr(Calendar cal) {

			String month = (cal.get(Calendar.MONTH) + 1) + "";
			String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
			String hour = (cal.get(Calendar.HOUR_OF_DAY)) + "";
			String min = (cal.get(Calendar.MINUTE)) + "";
			String sec = (cal.get(Calendar.SECOND)) + "";
			
			if (month.length() == 1) {
				month = "0" + month;
			}
			if (day.length() == 1) {
				day = "0" + day;
			}
			if (hour.length() == 1) {
				hour = "0" + hour;
			}
			if (min.length() == 1) {
				min = "0" + min;
			}
			if (sec.length() == 1) {
				sec = "0" + sec;
			}
			
			return cal.get(Calendar.YEAR) + "-" + month + "-"+ day + " " + hour + ":" + min + ":" + sec;
		}
		public String getDateStr(Calendar cal) {

			String month = (cal.get(Calendar.MONTH) + 1) + "";
			String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
			if (month.length() == 1) {
				month = "0" + month;
			}
			if (day.length() == 1) {
				day = "0" + day;
			}
			return cal.get(Calendar.YEAR) + "" + month + day;
		}
	  


    /**
     *
     */
    public void connectionUp(){
      connectionUp(this.url);
    }
    public String getSaveDebtInfo() {
    	 makePictureCat();
		  System.exit(0);
       	return null;
       }
    
    private void makePictureCat(){
	     
       	 
    String cmd = "SELECT  Pr.PrekesID as tid, Pr.Kodas AS toode " +
" FROM         dbo.Prekes AS Pr INNER JOIN dbo.PL_Prekes pl on pl.prekesid=pr.prekesID LEFT OUTER JOIN " +
 "                    dbo.MVGSantykiai AS mvs ON Pr.MVId = mvs.MVId1 AND Pr.IpokavimoMVId = mvs.MVId2 AND Pr.MVGId = mvs.MVGId INNER JOIN " +
 "                    dbo.Mokesciai AS mok ON Pr.MokesciaiId = mok.MokesciaiId INNER JOIN " +
 "                    dbo.PrekiuTipai AS pt ON Pr.PrekiuTipaiId = pt.PrekiuTipaiId INNER JOIN " + 
 "                    dbo.MV ON Pr.MVId = dbo.MV.MVid INNER JOIN " +
 "                    dbo.MV AS IpokMV ON Pr.IpokavimoMVId = IpokMV.MVid LEFT OUTER JOIN " +
 "                    dbo.SkaitaMedis_Prekes AS mdp ON Pr.PrekesID = mdp.PrekesID LEFT OUTER JOIN " +
 "                    dbo.SkaitaMedis AS md ON mdp.SkaitaMedisId = md.SkaitaMedisId " +
 "      WHERE     (Pr.PrekesID > 1) AND (pt.ArSandeliuojama = 1) and Pr.Nenaudoti <> ( 1 ) and md.SkaitaMedis_ListId IN (2)" ;

        
       	
         	String picts = parent.getProps().getProperty("pict.dir");
     	    String pictspda = parent.getProps().getProperty("pictpda.dir");
     	 

       	try {
// teema kataloogi puhtaks 
            //Kataloogi sisude kustutamine
       	 File res2 = new File (pictspda);
           String[] ff2 = res2.list();
       	 for(int i = 0;ff2.length>i;i++){
           //	System.out.println("fff " + ff2[i]);
           	(new File ( pictspda + ff2[i])).delete(); 
        
           }
//       	 L�ppes kustutamine 
       	 
         System.out.println("Piltide kopeerimine ");	
         //Failide kopeerimine  
           String[] gg = new File(picts).list();
        	  //System.out.println("gg " + gg[0] + " " + gg.length);
           
            for(int i = 0;gg.length>i;i++){          	
            	File in = new File ( picts + gg[i]); 
            	if(in.isFile()){            	
            	File inpda = new File ( pictspda + toUpper(gg[i])); 
                  	Converter.shrinkAndCopyFile(in,inpda);
            	}
            }
            

       		// nyyd on �ige nimega pildid olemas ja kontrollima kas ka sobivad 
       		
       		
       		
       		
       		
       		
       		
       		
       		
       		
       		Connection con = getNewConnection();
    	
       		Statement stmt = con.createStatement();
       	    ResultSet	      rs = stmt.executeQuery(cmd);


            StringBuffer buf = new StringBuffer();
            StringBuffer buf0 = new StringBuffer();
            int a = 0;
            int b = 0;
            int c = 0;
            
            // leiame kataloogid ja peame meeles 
            File d  = new File(picts);
            File[] dd = d.listFiles();

           
            
            System.out.println("Piltide leidmine - katalooge sees ");
       	    while (rs.next()) {
       	    	a++;
       	    	c++;
       	    	
       	     String toode = rs.getString("toode").trim();  
             toode = toode.replace('/', '_');
             toode = toode.replace('/', '_');
       	    	if(c == 10){
       	    		  c = 0;
       	    		  System.out.println("Piltide leidmine "  + a);	
       	    		  
       	    	}
       	     
                  
                     File f = new File(pictspda + toode+".jpg");
                     String len = "0";
                     if(f.exists()){
                       len =	f.length() + "";
                     }
                     
                     if(len.equals("0")){
                     	//System.out.println(toode+".jpg;"  + len);
                     	buf0.append(toode+".jpg\n");
                     }else{
                     	buf.append(toode+".jpg;"  + len + "\n");
               	    	b++;
                     }
                     }
       	    rs.close();
       	    stmt.close();
       	    con.close();
       	
       	    //oleasoleva kataloogi kustutamine	    
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
               	//  System.out.println("buf 0 " + a  + " " +  b + " "+ buf0.toString());
               	  File f0 = new File("list0.txt");
               	
               	  	
                        if (f0.exists())f0.delete();
                     
                       try {
                 		PrintWriter os1 = new PrintWriter(new FileWriter(
                 				  "list0.txt", true));
                 		os1.write(buf0.toString());
                 		os1.close();
                 		//System.out.println( buf.toString());
                 	  }catch (Exception ex) {
                 		ex.printStackTrace();
                 	  }
                 
                 File fff =  new File (pictspda +  "list.txt");
           	  copyFile(f,fff);  	
           	  File fff0 =  new File (pictspda +  "list0.txt");
           	  copyFile(f0,fff0);  	
       	} catch (Exception e) {
       	    getParent().showError(e);
       	}
       
    }
     
       private String toUpper(String asi ){
    	   
    	   asi = asi.substring(0,asi.indexOf(" ")).toUpperCase() + asi.substring(asi.lastIndexOf(".")).toLowerCase();
    	//   System.out.println(asi);
    	   return asi ;
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



       
    public String getAssortmentStr(Agent agent){
    	return null;
    }

            public String getDealerCustomerCSV(String agent){
             return null;
             }
            
            
           


        public String getSaveStockInfo(){
        	  connectionUp(url);
        	

        	  String cmVILN = "SELECT   'VILN' AS LADU,   pr.kodas AS toode, sum (CONVERT(Int, ps.Likutis_PP - ps.KiekisRezervuotas)) AS stock " +
 "FROM   dbo.Sandeliai AS sand INNER JOIN " +
  "                    dbo.PrekesSandeliuose AS ps ON sand.SandeliaiId = ps.SandeliaiId INNER JOIN " +
  "                    dbo.Prekes AS pr ON ps.PrekesId = pr.PrekesID " +
 " WHERE     (sand.SandeliaiId > 1) and  (CONVERT(Int, ps.Likutis_PP - ps.KiekisRezervuotas) >0 or " +
 " CONVERT(Int, ps.Likutis_PP) >0)  and sand.Kodas in ('S','GA','GS')  " +
 " group by  pr.kodas ";
        	  
		       if (isDebug()) System.out.println(cmVILN);
		       StringBuffer products = new StringBuffer();
		       try {

		         Statement st = con.createStatement();
		         ResultSet rs = st.executeQuery(cmVILN);

		         while (rs.next()) {
		        	 String dealer = rs.getString("LADU").trim();
		           String kood = rs.getString("toode").trim();
		           double seis = rs.getDouble("stock");
		          
		           if(seis > 0 ){
		        	   
		               products.append(kood +  ";");
		               products.append( dealer +";");
		               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
		             }
		           }
		           rs.close();
		           

		        	  String cmMUU = "SELECT   'MUU' AS LADU,   pr.kodas AS toode, sum (CONVERT(Int, ps.Likutis_PP - ps.KiekisRezervuotas)) AS stock " +
		 "FROM   dbo.Sandeliai AS sand INNER JOIN " +
		  "                    dbo.PrekesSandeliuose AS ps ON sand.SandeliaiId = ps.SandeliaiId INNER JOIN " +
		  "                    dbo.Prekes AS pr ON ps.PrekesId = pr.PrekesID " +
		 " WHERE     (sand.SandeliaiId > 1) and  (CONVERT(Int, ps.Likutis_PP - ps.KiekisRezervuotas) >0 or " +
		 " CONVERT(Int, ps.Likutis_PP) >0)  and sand.Kodas in ('GA','GS')  " +
		 " group by  pr.kodas ";
		           
		        	  rs = st.executeQuery(cmMUU);

				         while (rs.next()) {
				        	 String dealer = rs.getString("LADU").trim();
				           String kood = rs.getString("toode").trim();
				           double seis = rs.getDouble("stock");
				          
				           if(seis > 0 ){
				        	  
				               products.append(kood +  ";");
				               products.append( dealer +";");
				               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
				             }
				           }
				           rs.close();
		           
		           con.close();
		       } catch (Exception e) {
		           getParent().showError(e);
		       }
		       return products.toString();
		   }
        
                                           public String getCustProdDiscounts(Agent agent){
                                            return null;
                                            }

                                       	public Vector getLastThreeData() {
                                    		// TODO Auto-generated method stub
                                    		return null;
                                    	}
        public String getDistribution() {
          return null;
        }

        public Vector getFixedAssortmentData() {
                Vector lines = new Vector();

                try {
               	  String cmd = "    select '' AS custid,ltrim(str(sut.NuolaiduSutartysId)) AS grupp ,r.prekesID AS toode from   dbo.NuolaiduInfoPrekes r  " +  
    " 	   INNER JOIN  dbo.NuolaiduAprasymai  AS apr ON r.NuolaiduAprasymaiId = apr.NuolaiduAprasymaiId   " +
    "  INNER JOIN  dbo.NuolaiduSutartys  AS sut ON r.NuolaiduAprasymaiId = sut.NuolaiduAprasymaiId and  (r.NuolaiduSutartysId = sut.NuolaiduSutartysId or  r.NuolaiduSutartysId = 1 )  " +  
   " 	  and (CONVERT (date, GETDATE()) between   cast(r.DataNuo_Int as datetime) and  cast(r.DataIki_Int as datetime) or (r.DataNuo_Int = 0 and r.DataIki_Int = 999999)) where " +
	"	  ArAsortimentas in (1,3)";
               	System.out.println(cmd);
                  	connectionUp(url);;
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

    private void connectionUp(String url){
      try {
        if (con != null){
          con.close();
        }
           con = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
           getParent().getProps().getProperty("dsn.pass"));
      } catch (Exception e) {
	    getParent().showError(e);
      }

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

    	 public TreeMap getProdGroups() throws SQLException {
    	      Connection con = this.getNewConnection();
    	      TreeMap gr = new TreeMap();
    	  this.setTreeGroup(true);

     String sql = " SELECT  CONVERT(varchar(20), md.PrekiuGrupesId) AS kood, CONVERT(varchar(50), md.Pavad) AS nimetus " +
	        " FROM  dbo.PrekiuGrupes AS md where md.Pavad <> ( '' ) and  " +
			"  CASE WHEN ISNUMERIC(Kodas) = 1 THEN CAST(Kodas AS INT) ELSE 0 END between 1100 and 1350" ;

    	    Statement st = con.createStatement();
    	    ResultSet rs = st.executeQuery(sql);

    	    while (rs.next()) {
    	    	
    	    	String kood = rs.getString("kood");
    	  	    String nimetus2 =  rs.getString("nimetus");
    	  	    nimetus2 = nimetus2.substring(nimetus2.length()-2) + nimetus2.substring(0,nimetus2.length()-2) ;
    	  	    
    	  		    gr.put( kood, Converter.enCodeEst(nimetus2.trim()));	
    	  	    
    	    }
    	  
    	    rs.close();
    	    st.close();
    	    con.close();
    	 
    	    return gr;
    	 }


    
    public TreeMap getProdGroupsSecond() throws SQLException {
 	    return new TreeMap();
 }
    
    private TreeMap agzone = new TreeMap();

    public  String getExcelDir(){
        String dir = getParent().getProps().getProperty("excel.dir");
        if(dir != null){
      	   if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";  
        }else{
      	    dir =  "excel/"; 
        }
        return dir;
      }
    
   
   
      
    public TreeMap getAgents() {
    
    	
    	
           	TreeMap agents = new TreeMap();
        try {  
    			 connectionUp(url);
        String  cmd = "select  masm.MusuAsmenysId  AS EmployeeAsId, masm.Kodas AS yksus, LTRIM(masm.Vardas + ' ' + masm.Pavarde) AS nimi " +
				 " FROM dbo.MusuAsmenys AS masm " +
				 " INNER JOIN pl_musuasmenys abi ON masm.MusuAsmenysId =abi.MusuAsmenysId where Pareigos like  '%adyb%' " ;
				


            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim() );
              String nimi = agent.getID() + " " +  rs.getString("nimi").trim();
              agent.setName(nimi);

              agent.setLanguage("LIT");
              agent.setCountry("lt");
              agent.setPassWord("0000");
            
            //  agent.needsZipPass = true;
              
              //LT     1,2,3,5,6,7,8,10,11,12,13,15,17
            //  Latvijos vadybininkai LV    4,18,19,21
              
              if(agent.getID().equals("4") || agent.getID().equals("18") ||
            		  agent.getID().equals("19") || agent.getID().equals("21")
            		  || agent.getID().equals("24") || agent.getID().equals("25") || agent.getID().equals("26")  ){
            	  agent.addProdGroupList("LV");
              }else{
               	  agent.addProdGroupList("LT");
              }
              if(agent.getID().equals("xx") ){
            	  agent.addProdGroupList("LT1");
              }
              if(agent.getID().equals("xx") ){
            	  agent.addProdGroupList("LT2");
              }
              if(agent.getID().equals("xx") ){
            	  agent.addProdGroupList("LT3");
              }
              
            if("true".equals(  getParent().getProps().getProperty("usewebpass"))) {
            	agent.needsZipPass = true;
            }
              

              agents.put(nimi, agent);
              agent1.put(agent.getID(), agent);
        }
	    rs.close();
	    stmt.close();
	    con.close();
	 

	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
        Agent agent = new Agent("HOUSE");
        String nimi = agent.getID() ;
        agent.setName(nimi);
        agent.setIsPilot(true);
        agents.put(nimi, agent);
        
	return agents;
    }


    public Vector getPriceMatrixData()  {
        Vector lines = new Vector();
    
     
   String    sql = "select Partneriai_Kodas as clgroup, Prekes_Kodas as prodid,KainaBePVM as price  from [MES_Kainos]  " ; 
		

           try {
             TreeMap prior = new TreeMap(); 
        	   
             Connection con = DriverManager.getConnection(url2, getParent().getProps().getProperty("dsn.user"),
                     getParent().getProps().getProperty("dsn.pass"));
             Statement  st = con.createStatement();
             ResultSet rs = st.executeQuery(sql);

             while (rs.next()) {

               String clGroup = rs.getString("clgroup");
               String prodID = rs.getString("prodid").trim();
               double price = rs.getDouble("price");
             
               if(clGroup.trim().length() > 0) {
            	   MatrixLine mline = new MatrixLine();
               mline.setCliGroup(clGroup.trim());

               if (prodID != null && prodID.trim().length() > 1) {
                 mline.setProdID(prodID.trim());
               }
             
               if (price != 0) {
                 mline.setPrice(price);
               }
              
                   lines.add(mline);   
             }
             }
             rs.close();
             st.close();
             con.close();
            	   
           }
           catch (SQLException ex) {
             ex.printStackTrace();
           }
           System.out.println("Maatrix suurus " + lines.size());
         return lines;
    	
       }

    

    
  
    public Vector getPriceMatrixDataold()  {
        Vector lines = new Vector();
       this.getProducts(null);
    
     
     String sqlp = "select  hh.DataNuo_Int as start from  " +
	   " NuolaiduAprasymai apr INNER JOIN dbo.NuolaiduKainininkuSudetis hh on  " +
	    " apr.NuolaiduKainininkaiId= hh.NuolaiduKainininkaiId  " +
	 	"right join dbo.NuolaiduInfoPrekiuGrupes r  on r.NuolaiduAprasymaiId = apr.NuolaiduAprasymaiId  " +
		" INNER JOIN  dbo.NuolaiduSutartys  AS sut ON r.NuolaiduAprasymaiId = sut.NuolaiduAprasymaiId  " +
	 	" where  " +
	 	" (CONVERT (date, GETDATE()) between   cast(hh.DataNuo_Int as datetime) and  cast(hh.DataIki_Int as datetime) or (hh.DataNuo_Int = 0 and hh.DataIki_Int = 999999)) " +
	" group by hh.DataNuo_Int " +
    	" union  select r.DataNuo_Int  as start from   dbo.NuolaiduInfoPrekes r   " +
     	"   INNER JOIN  dbo.NuolaiduAprasymai  AS apr ON r.NuolaiduAprasymaiId = apr.NuolaiduAprasymaiId  " + 
    	"  INNER JOIN  dbo.NuolaiduSutartys  AS sut ON r.NuolaiduAprasymaiId = sut.NuolaiduAprasymaiId and  (r.NuolaiduSutartysId = sut.NuolaiduSutartysId or  r.NuolaiduSutartysId = 1 )  " +  
    "	  and (CONVERT (date, GETDATE()) between   cast(r.DataNuo_Int as datetime) and  cast(r.DataIki_Int as datetime) or (r.DataNuo_Int = 0 and r.DataIki_Int = 999999)) group by r.DataNuo_Int " +
     "   union select  sud.DataNuo_Int as start " +
	" from KainosPrekiuKategorijoms as t INNER JOIN PrekiuKainuKategorijos as abi on t.PrekiuKainuKategorijosId= abi.PrekiuKainuKategorijosId " +
	"	 INNER JOIN NuolaiduKainininkuSudetis sud on sud.PrekiuKainuKategorijosId= abi.PrekiuKainuKategorijosId  where kaina > 0 and " +
	"	 (CONVERT (date, GETDATE()) between   cast(sud.DataNuo_Int as datetime) and  cast(sud.DataIki_Int as datetime) or (sud.DataNuo_Int = 0 and sud.DataIki_Int = 99999)) " +
  " group by sud.DataNuo_Int order by 1";
     
     
     
String    sql = " select  '' AS custid,ltrim(str(sut.NuolaiduSutartysId)) AS clgroup ,'' AS prodid,r.PrekiuGrupesId AS prodgroup,NuolaidosProc AS discount,0 AS price ,  " +
	  "  cast(hh.DataNuo_Int as datetime) as start  , cast(hh.DataIki_Int as datetime) as endday ,  hh.DataNuo_Int as  prior  from  " +
	   " NuolaiduAprasymai apr INNER JOIN dbo.NuolaiduKainininkuSudetis hh on  " +
	   "  apr.NuolaiduKainininkaiId= hh.NuolaiduKainininkaiId  " +
	 "	right join dbo.NuolaiduInfoPrekiuGrupes r  on r.NuolaiduAprasymaiId = apr.NuolaiduAprasymaiId  " +
	"	 INNER JOIN  dbo.NuolaiduSutartys  AS sut ON r.NuolaiduAprasymaiId = sut.NuolaiduAprasymaiId  " +
	 "	 where  " +
	 "	 (CONVERT (date, GETDATE()) between   cast(hh.DataNuo_Int as datetime) and  cast(hh.DataIki_Int as datetime) or (hh.DataNuo_Int = 0 and hh.DataIki_Int = 999999)) " +
	 
    "	union  " +
     "	   select '' AS custid,ltrim(str(sut.NuolaiduSutartysId)) AS clgroup ,r.prekesID AS prodid,'' AS prodgroup,NuolaidosProc AS discount,KainaBePVM AS price ,cast(r.DataNuo_Int as datetime) as start " +   
     "	   , cast(r.DataIki_Int as datetime) as endday ,   sut.DataNuo_Int as prior  from   dbo.NuolaiduInfoPrekes r   " +
     "	   INNER JOIN  dbo.NuolaiduAprasymai  AS apr ON r.NuolaiduAprasymaiId = apr.NuolaiduAprasymaiId   " +
    "	  INNER JOIN  dbo.NuolaiduSutartys  AS sut ON r.NuolaiduAprasymaiId = sut.NuolaiduAprasymaiId and  (r.NuolaiduSutartysId = sut.NuolaiduSutartysId or  r.NuolaiduSutartysId = 1 )    " +
    "	  and (CONVERT (date, GETDATE()) between   cast(r.DataNuo_Int as datetime) and  cast(r.DataIki_Int as datetime) or (r.DataNuo_Int = 0 and r.DataIki_Int = 999999)) " +
     "   union " +
	" select  '' AS custid,'K'+ ltrim(str(sud.NuolaiduKainininkaiId)) AS clgroup ,t.prekesID AS prodid,'' AS prodgroup,0 AS discount,t.Kaina AS price ,cast(sud.DataNuo_Int as datetime) as start " +
	" , cast(sud.DataIki_Int as datetime) as endday ,   sud.DataNuo_Int as prior" +
	" from KainosPrekiuKategorijoms as t INNER JOIN PrekiuKainuKategorijos as abi on t.PrekiuKainuKategorijosId= abi.PrekiuKainuKategorijosId " +
	"	 INNER JOIN NuolaiduKainininkuSudetis sud on sud.PrekiuKainuKategorijosId= abi.PrekiuKainuKategorijosId " +
	"	 where kaina > 0 and " +
	"	 (CONVERT (date, GETDATE()) between   cast(sud.DataNuo_Int as datetime) and  cast(sud.DataIki_Int as datetime) or (sud.DataNuo_Int = 0 and sud.DataIki_Int = 99999)) " +
	"  order by 8,2,3,7 " ;


Statement st = null;
           try {
             TreeMap prior = new TreeMap(); 
        	   
             Connection con = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
                     getParent().getProps().getProperty("dsn.pass"));
             st = con.createStatement();
             ResultSet rs = st.executeQuery(sqlp);
             
           int i = 1;
             while (rs.next()) {
            	 
                 String prr = rs.getString("start").trim();
                 String aa = i +"";
                 if(aa.length() == 1){
                	 aa = "00" + aa;
                 }
                 if(aa.length() == 2){
                	 aa = "0" + aa;
                 }
                 prior.put(prr, aa);
                  
              i++;    
             }
             
             rs.close();
             
             
             //Baas hinnad
             
             
             
             rs = st.executeQuery(sql);

             while (rs.next()) {

               String clGroup = rs.getString("clgroup");
               String prodID = rs.getString("prodid").trim();
               String idd = prodID;
               
               if(prcodes.get(prodID) != null){
            	   prodID = prcodes.get(prodID) + "";
               }
               String prodGroup = rs.getString("prodgroup");
               
               String discount = rs.getString("discount");
               
               double price = rs.getDouble("price");
               String prt = rs.getString("prior");
               MatrixLine mline = new MatrixLine();

               mline.setCliGroup(clGroup.trim());
               mline.setProdLevel(prt);
               if (prodID != null && prodID.trim().length() > 1) {
                 mline.setProdID(prodID.trim());
               }
               if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                 mline.setProdGroup(prodGroup.trim());
                // System.out.println("asi " + prodGroup.trim());
               }
               if(discount.trim().length()==0){
               	discount = "0";
               }
                 mline.setDiscount(Double.parseDouble(discount));

               if (Double.parseDouble(discount) == 0 && price != 0) {
                 mline.setPrice(price);

               }
               boolean goes = true ;
               if(clGroup != null && clGroup.trim().startsWith("K")){
            	   if(this.prodHash.get(idd) != null ){
            		   if(Double.parseDouble(this.prodHash.get(idd)+"") == price){
            			  goes = false; 
            		   }
            	   }
               }
               
               if(goes){
                   lines.add(mline);   
               }
               
             }

             rs.close();
             st.close();
             con.close();
     
          
            	   
           }
           catch (SQLException ex) {
             ex.printStackTrace();
           }
          // System.out.println("Maatrix suurus " + lines.size());
         return lines;
    	
       }
    private TreeMap agent1 = new TreeMap();
    
      
      public TreeMap getCustomers(Agent agent){
    	  
    	  
    	  try{
    		   Converter.getManagersReplacements(agent1, rcompany, user, pass); 
    			
    	}catch(Exception e){
    		
    	}
    	  
        String cmd = "SELECT CONVERT(Int, part.PartneriaiId) AS CustomerASId, part.regnr as regno , (select  oo.Kodas from Adresai uu " + 
				" INNER JOIN Partneriai oo  ON oo.AdresoId = uu.AdresoId  where oo.PartneriaiId=partpa.SumokaPartneriaiID  ) as pay, CONVERT(varchar(20), part.Kodas) AS kood,pl.MES_PAV_Trumpinys as nimi2, CONVERT(varchar(40), part.Pavad) AS nimi,   " +
                   "   SumokaPartneriaiID AS payer ,  rtrim(adr.Adresas)  + case when adr.Indeksas IS NULL then '' else ' ' + rtrim(adr.Indeksas)end + ' ' + " +
                   " case when adr.Miestas IS NULL then '' else ' ' + rtrim(adr.Miestas)end AS Address,part.pvmnr as vatno, " +
                   "    CONVERT(char(1), 'N') AS Blocked, CONVERT(char(1), 'Y') AS Valid, CONVERT(Int, ROUND(part.Kreditas, 0)) AS CreditLimit, CONVERT(SmallInt, msal.Terminas)  " +
                   "    AS paym,  CONVERT(nvarchar(30),  " +
                   "    adr.Miestas) AS City, CONVERT(nvarchar(40), LTRIM(ats.Vardas + ' ' + ats.Pavarde)) AS ContactName, CONVERT(nvarchar(30), ats.Pareigos) AS ContactTitle, " +  
                   "    CONVERT(varchar(50), adr.Tel1) AS CustPhone , " +
                   "   CONVERT(Money, ISNULL  ((SELECT     SUM(ROUND((op.Suma - op.ApmoketaSuma) * op.Kursas, 2) * (CASE pot.PartOperacijosTipas WHEN 1 THEN 1 ELSE - 1 END)) AS Expr1   " +
                   "            FROM         dbo.Operacijos AS op INNER JOIN  " +
                   "                                  dbo.PirkimuOpTipai AS pot ON op.OperacijosTipoId = pot.OperacijosTipoId INNER JOIN  " +
                   "                                  dbo.OperacijosPartneriai AS opp ON op.OperacijosId = opp.OperacijosId  " +
                   "            WHERE     (pot.PartOperacijosTipas IN (1, 3, 12)) AND (opp.spec = 0) and op.Data+CONVERT(SmallInt, msal.Terminas) < getdate() AND (opp.PartneriaiId = part.PartneriaiId) AND " + 
				"		 	  (op.Suma > op.ApmoketaSuma)), 0)) as debt  ,masm.kodas as agent	,  " +
				"			  (select  rtrim(uu.Adresas)  + case when uu.Indeksas IS NULL then '' else ' ' + rtrim(uu.Indeksas)end + ' ' + rtrim(uu.Miestas) from Adresai uu " +
				"INNER JOIN Partneriai oo  ON oo.AdresoId = uu.AdresoId  where oo.PartneriaiId=partpa.SumokaPartneriaiID  ) as jurid  , apr.Pavad  as aletext , sut.NuolaiduAprasymaiId  as hinnak , kain.NuolaiduKainininkaiId  as hinnak2 " +
            " FROM         dbo.Partneriai AS part INNER JOIN  " +
            "             dbo.PartneriaiPa AS partpa ON part.PartneriaiId = partpa.PartneriaiId INNER JOIN  " +
             "          dbo.MokejimoSalygos AS msal ON partpa.MokejimoSalygosID = msal.MokejimoSalygosID INNER JOIN " +  
              "      dbo.MokejimoBudai AS mbud ON partpa.MokejimoBudaiID = mbud.MokejimoBudaiID INNER JOIN  " +
               "     dbo.PartneriuKategorijos AS pkat ON partpa.PartneriuKategorijosId = pkat.PartneriuKategorijosId INNER JOIN  " +
                "     dbo.NuolaiduKainininkai AS kain ON pkat.NuolaiduKainininkaiId = kain.NuolaiduKainininkaiId INNER JOIN  " +
		 		 "     dbo.Mokesciai AS mok ON partpa.MokesciaiId = mok.MokesciaiId INNER JOIN " + 
                  "     dbo.Adresai AS adr ON part.AdresoId = adr.AdresoId INNER JOIN  " +
                  "     dbo.Atstovai AS ats ON part.AtstovaiId = ats.AtstovaiId INNER JOIN  " +
                  "     dbo.PL_PARTNERIAI AS pl ON part.PartneriaiId = pl.PARTNERIAIID INNER JOIN          " +           
                  "     dbo.MusuAsmenys AS masm ON part.MusuAsmenysId1 = masm.MusuAsmenysId INNER JOIN  " +
                  "     dbo.PL_MUSUASMENYS AS plmasm ON masm.MusuAsmenysId = plmasm.MUSUASMENYSID LEFT JOIN " + 
                  "     dbo.NuolaiduSutartys AS sut ON part.PartneriaiId = sut.PartneriaiId and " +
				  "     (getdate() between   cast(sut.DataNuo_Int as datetime) and  cast(sut.DataIki_Int as datetime) or (sut.DataNuo_Int = 0 and sut.DataIki_Int = 999999)) LEFT OUTER JOIN " +
				  "     dbo.NuolaiduAprasymai AS apr ON sut.NuolaiduAprasymaiId = apr.NuolaiduAprasymaiId LEFT OUTER JOIN  " +
                  "     dbo.PL_MUSUASMENYS AS plmasm2 ON part.MusuAsmenysId2 = plmasm2.MUSUASMENYSID CROSS JOIN  " +
                  "     dbo.FirmosAtributai AS fa  " +
     " WHERE      masm.kodas is not null and pl.MES_PAV_Trumpinys !='' ";
	
        log(cmd);

		 
		 
		 String degu = " SELECT  part.Kodas as klient , pr.Kodas as toode , pk.kiekis*pr.PK_PardKaina as summa,pr.PK_PardKaina as hind " +
 " FROM  PardavimuKunai AS pk WITH(NOLOCK) INNER JOIN Operacijos AS op   ON pk.OperacijosId = op.OperacijosId  INNER JOIN Pard_BendraInf AS pbi WITH(NOLOCK) ON op.OperacijosId = pbi.OperacijosId  " +
 " INNER JOIN  Partneriai AS part WITH(NOLOCK) ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN xPrekes AS pr  WITH(NOLOCK) ON pk.PrekesId = pr.PrekesId  "+
 " WHERE     op.OperacijosTipoId = (2000018) and year(op.Data) = year(getdate()) and month(op.Data) = month(getdate()) "+
 " order by 1,2";
		 
		    HashMap baasMap = new HashMap();
			TreeMap customers = new TreeMap();
		        HashMap custMap = new HashMap();
		        HashMap deguSumm = new HashMap();
		    	
				 try {	     
		 Connection con2 = this.getNewConnection();
	        PreparedStatement stmt = con2.prepareStatement(degu);
	        ResultSet  rs = stmt.executeQuery();

		   while (rs.next()) {
			 String klient = (rs.getString("klient").trim());
			 String toode = (rs.getString("toode").trim());
			 double kogus = rs.getDouble("summa");
			 if(deguSumm.get(klient) != null){
				 double asi = Double.parseDouble(deguSumm.get(klient)+"");
				 deguSumm.put(klient,asi + (kogus )); 
				    		 
			 }else{
				 deguSumm.put(klient,kogus ); 
				    	 
			 }
		     }
			 rs.close();
			 stmt.close();
			
			 
	//	System.out.println("Degusum " + deguSumm.toString());	 
			 
	
		
		 
		 
		 
          stmt = con2.prepareStatement(cmd);
       	      rs = stmt.executeQuery();

	   while (rs.next()) {
		   
		   
		   String id = rs.getString("CustomerASId").trim();
		   String regno = rs.getString("regno");
		   
		   String payid = rs.getString("pay").trim();
		   Customer cust = new Customer(rs.getString("kood").trim());
		   if(regno != null && regno.trim().length() > 0){
			   cust.setRegnr(regno.trim());
		   }
		 
		   if(payid != null){
				  cust.setPayer(payid);
				  cust.setGroup(payid);
	          }
		   
		   String nim = rs.getString("nimi2").replaceAll("\""," ").trim();
		   String nims = rs.getString("nimi").replaceAll("\""," ").trim();
		   
		 String nimi = Converter.enCodeEst(nim);
		 String payer = rs.getString("payer");
		 
		 if(payer != null){
			 payer = Converter.enCodeEst(payer.replaceAll("\""," ").trim());
		 }
		 
		 String addr =  Converter.enCodeEst(rs.getString("Address").replaceAll("\""," ").trim());
		 String vatno = rs.getString("vatno");
		 if(vatno != null){
			vatno = Converter.enCodeEst(vatno.replaceAll("\""," ").trim()); 
			 cust.setVatnr(vatno);
		 }

         if(deguSumm.get(cust.getID()) != null){
        	cust.setCity("DeguSuma : " + deguSumm.get(cust.getID()) + " eur");
         }
		 
		  String term = rs.getString("paym");
		 
		  
                
                if(nimi.startsWith("UAB ")){
                	nimi = (nimi.substring(4,nimi.length()) + " UAB").trim();
                }
                cust.setName(nimi);
                try{
                	String aadr = Converter.enCodeEst(addr.trim());
                	
                	if(aadr.toLowerCase().contains("latvija")){
                		aadr = Converter.adressLink(aadr, "");
                      	 cust.setCountry("Latvija");
                      	
                	}else{
                		aadr = Converter.adressLink(aadr, "Lithuania");
                   	 cust.setCountry("Lithuania");
                   		
                	}
                	cust.setAddress(aadr);
                	 int comma = addr.indexOf(",");
                	 if(comma != -1) {
                		 cust.restStreet = addr.substring(0,comma);
                		 cust.restCity = addr.substring(comma+1);
                	 }else {
                		 cust.restStreet = addr.trim(); 
                	 }
                
                	
                }catch(Exception aa){
                	
                }
               
                
                  cust.setPaymTerm(parent.make(term));
              
                double debti = rs.getDouble("debt");
               
                	cust.setDebt(debti);
                
                String agen = rs.getString("agent");
                if(agen != null){
                  cust.addAgent(new Agent(agen.trim()));
                  cust.setMainAgent(agen.trim());
                  
                  if (agen != null && agent1.get(agen.trim()) != null){
      				Agent aa = (Agent)agent1.get(agen.trim());
      			
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
                
                cust.addAgent(new Agent("HOUSE"));
              
                String aadrjur = rs.getString("jurid");
                if(aadrjur != null && aadrjur.trim().length()>0){
                  cust.setJuridics(Converter.enCodeEst(aadrjur.trim()));
                }
                
                String aletext = rs.getString("aletext");
                if(aletext != null && aletext.trim().length()>0){
                  cust.setZipcode(Converter.enCodeEst(aletext.trim()));
                }
              
               
                
                	 customers.put(Converter.cleanLitLatEst(nim+addr), cust);
                     custMap.put(cust.getID(), cust);	
                
	    }
            rs.close();
            stmt.close();
           
        
	} catch (Exception e) {
            getParent().showError(e);
        }

	return customers;
    }

    public DealerProduct getDealerProductsList() {
    	
    	
    	 DealerProduct dealerProds = new DealerProduct();    	
    	  connectionUp(url);
      	

    	  String cmVILN = "SELECT   'VILN' AS LADU,   pr.kodas AS toode, sum (CONVERT(Int, ps.Likutis_PP - ps.KiekisRezervuotas)) AS stock " +
"FROM   dbo.Sandeliai AS sand INNER JOIN " +
"                    dbo.PrekesSandeliuose AS ps ON sand.SandeliaiId = ps.SandeliaiId INNER JOIN " +
"                    dbo.Prekes AS pr ON ps.PrekesId = pr.PrekesID " +
" WHERE     (sand.SandeliaiId > 1) and  (CONVERT(Int, ps.Likutis_PP - ps.KiekisRezervuotas) >0 or " +
" CONVERT(Int, ps.Likutis_PP) >0)  and sand.Kodas in ('S','GA','GS')  " +
" group by  pr.kodas ";
    	  
	       if (isDebug()) System.out.println(cmVILN);
	       try {

	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmVILN);

	         while (rs.next()) {
	        	 String dealer = rs.getString("LADU").trim();
	           String kood = rs.getString("toode").trim();
	           double seis = rs.getDouble("stock");
	          
	           if(seis > 0 ){
	        	   dealerProds.add(dealer, kood,seis+""," "," " );
		              
	             }
	           }
	           rs.close();
	           

	        	  String cmMUU = "SELECT   'MUU' AS LADU,   pr.kodas AS toode, sum (CONVERT(Int, ps.Likutis_PP - ps.KiekisRezervuotas)) AS stock " +
	 "FROM   dbo.Sandeliai AS sand INNER JOIN " +
	  "                    dbo.PrekesSandeliuose AS ps ON sand.SandeliaiId = ps.SandeliaiId INNER JOIN " +
	  "                    dbo.Prekes AS pr ON ps.PrekesId = pr.PrekesID " +
	 " WHERE     (sand.SandeliaiId > 1) and  (CONVERT(Int, ps.Likutis_PP - ps.KiekisRezervuotas) >0 or " +
	 " CONVERT(Int, ps.Likutis_PP) >0)  and sand.Kodas in ('GA','GS')  " +
	 " group by  pr.kodas ";
	           
	        	  rs = st.executeQuery(cmMUU);

			         while (rs.next()) {
			        	 String dealer = rs.getString("LADU").trim();
			           String kood = rs.getString("toode").trim();
			           double seis = rs.getDouble("stock");
			          
			           if(seis > 0 ){
			        	   dealerProds.add(dealer, kood,seis+""," "," " );
			              }
			           }
			           rs.close();
	           
	           con.close();
	       } catch (Exception e) {
	           getParent().showError(e);
	       }

    	return dealerProds;
       }
    
  
    public TreeMap getDealers() {
        return dealers;
      }

    
    
    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = " select  CustomerCode AS klient, billcode AS number,billissuedate+paymenttime AS kuupaev, " + 
		" (case when OperacijosTipoId =64 then -1 else 1 end)*billamount-paidamount AS summa  , datediff(day,billissuedate+paymenttime ,getdate())as yle ,billissuedate as kuup ,billamount as origsum , payercode as payer from  " +
	 " (  SELECT     OrderAsID = CONVERT(varchar(20), op.OperacijosId), DeliveredOrderAsID = CONVERT(varchar(20), " + 
         "             (CASE WHEN op.OperacijosTipoId = 50 THEN op.OperacijosId ELSE NULL END)), OrderDate = op.Data, OrderDiscount = CONVERT(Real, 0),  " +
          "            BillCode = CONVERT(varchar(20), op.DokNr), BillIssueDate = op.Data, BillPaymentDate = CONVERT(DateTime, NULL), BillAmount = CONVERT(Money, op.Suma),  " +
          "            PaidAmount = CONVERT(Money, op.ApmoketaSuma), CustomerAsID = CONVERT(Int, part.PartneriaiId), EmployeeAsID = CONVERT(varchar(35), masm.MusuAsmenysId), " +
          "             Employeecode = CONVERT(varchar(35), masm.Kodas), OrderState = CONVERT(Int, NULL), OrderStateExt = CONVERT(Int, NULL), CurrencyCode = CONVERT(varchar(2), " + 
          "            op.Valiuta), DeliveryAddressNo = CONVERT(varchar(2), NULL), OrderRemark = CONVERT(varchar(35), SUBSTRING(okp.Pastaba1, 1, 35)), op.OperacijosId,  " +
          "            CustomerCode = part.Kodas, PaymentTime = CONVERT(SmallInt, msal.Terminas), PayerCode =  (select  oo.Kodas from  Partneriai oo    where oo.PartneriaiId=partpa.SumokaPartneriaiID  ) , op.OperacijosTipoId " +
          "       FROM         Operacijos AS op INNER JOIN " +
          "            Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
          "            Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
          "            PartneriaiPA AS partpa ON part.PartneriaiId = partpa.PartneriaiId INNER JOIN " +
          "            MokejimoSalygos AS msal ON partpa.MokejimoSalygosId = msal.MokejimoSalygosId INNER JOIN " +
          "            Pirk_VykdymoInf AS pvi ON pbi.Pirk_VykdymoInfId = pvi.Pirk_VykdymoInfId INNER JOIN " +
          
          "            MusuAsmenys AS masm ON pvi.MusuAsmenysId = masm.MusuAsmenysId LEFT OUTER JOIN " +
          "            OperacijuKuneliuPastabos AS okp ON op.OperacijosId = okp.OperacijosId " +
         " WHERE     0 = 1 AND op.Kilme = 35 " +
 " UNION ALL " +
" SELECT     OrderAsID = CONVERT(varchar(20), CASE WHEN op.OperacijosTipoId = 50 THEN op2.KilmeInfo ELSE NULL END), DeliveredOrderAsID = CONVERT(varchar(20),  " +
  "                    op.OperacijosId), OrderDate = op.Data, OrderDiscount = CONVERT(Real, 0), BillCode = CONVERT(varchar(20), op.DokNr), BillIssueDate = op.Data,  " +
   "                   BillPaymentDate = CONVERT(DateTime, NULL), BillAmount = CONVERT(Money, op.Suma), PaidAmount = CONVERT(Money, op.ApmoketaSuma),  " +
    "                  CustomerAsID = CONVERT(Int, part.PartneriaiId), EmployeeAsID = CONVERT(varchar(35), masm.MusuAsmenysId), Employeecode = CONVERT(varchar(35),  " +
     "                 masm.Kodas), OrderState = CONVERT(Int, NULL), OrderStateExt = CONVERT(Int, NULL), CurrencyCode = CONVERT(varchar(2), op.Valiuta),  " +
      "                DeliveryAddressNo = CONVERT(varchar(2), NULL), OrderRemark = CONVERT(varchar(35), SUBSTRING(okp.Pastaba1, 1, 35)), op.OperacijosId,  " +
       "               customercode = part.Kodas, PaymentTime = CONVERT(SmallInt, msal.Terminas), PayerCode =  (select  oo.Kodas from  Partneriai oo    where oo.PartneriaiId=partpa.SumokaPartneriaiID  ) , op.OperacijosTipoId " +
"FROM         Operacijos AS op INNER JOIN " +
 "                     Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
  "                    Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
   "                   PartneriaiPA AS partpa ON part.PartneriaiId = partpa.PartneriaiId INNER JOIN " +
    "                  MokejimoSalygos AS msal ON partpa.MokejimoSalygosId = msal.MokejimoSalygosId INNER JOIN " +
     "                 Pirk_VykdymoInf AS pvi ON pbi.Pirk_VykdymoInfId = pvi.Pirk_VykdymoInfId INNER JOIN " +
      "                MusuAsmenys AS masm ON pvi.MusuAsmenysId = masm.MusuAsmenysId LEFT OUTER JOIN " +
       "               OperacijuKuneliuPastabos AS okp ON op.OperacijosId = okp.OperacijosId INNER JOIN " +
        "              RysioOperacijos AS rop ON op.OperacijosId = rop.OperacijosId INNER JOIN " +
         "             Operacijos AS op2 ON rop.OperacijosId2 = op2.OperacijosId " +
" WHERE     op.OperacijosTipoId IN (50, 64 , 68) AND rop.RysiuTipaiId = 3 AND rop.OperacijosStatusasId = 778 ) " +
" d  where billamount-paidamount>0 and " +
 "         billissuedate > getdate()-600  order by CustomerCode,billissuedate+paymenttime";

      Connection con = this.getNewConnection();
    PreparedStatement  stmt = con.prepareStatement(sql);


      ResultSet rs = stmt.executeQuery();

      while(rs.next()){
    	  try{
    		    Debt debt = new Debt();
    	        debt.setCustID(rs.getString("klient").trim());
    	        debt.setPayerID(debt.getCustID());

            String dok = rs.getString("number");
    	        if(dok != null){
    	        	 dok = dok.trim();
    	    	        if(dok.length() >5){
    	    	        	dok = dok.substring(4);
    	    	        }
    	    	        debt.setDoknr(dok);   	    	        
    	    	        debt.setDuedate(rs.getDate("kuupaev"));
    	    	        debt.setSum(rs.getDouble("summa"));       
    	    	        debt.setDue(rs.getString("yle")); 
    	    	        debt.setDate(rs.getDate("kuup"));
    	    	        double origsum = rs.getDouble("origsum");
    	    	        if(origsum != debt.getSum()){
    	    	        	 debt.setTara(origsum);
    	    	        }
    	    	        String payer = rs.getString("payer");
    	    	        if(payer != null){
    	    	            debt.setPayerID(payer.trim());
    	     	    	}
    	    	   
    	    	        list.addElement(debt);	
    	        }
    	       
    	  }catch (Exception e){
    		  e.printStackTrace();
    	  }
    
        }
      rs.close();
      stmt.close();
      con.close();
      System.out.println("debtlist size " + list.size());
      return list;
    }
    
    
    
    

	
    
    
    
    private TreeMap prcodes = new TreeMap();
    private HashMap prodHash = new HashMap();
    
    public TreeMap getProducts(Agent agent){
    	
	TreeMap products = new TreeMap();
	
	//getSaveStockInfo();
	prcodes = new TreeMap();
	
	/*
	String  cmde = "  SELECT    CONVERT(varchar(20), sand.Kodas) AS LADU, pr.kodas AS toode, CONVERT(Int, ps.Likutis_PP) AS UnitsInStock, CONVERT(Int,  " +
            "  ps.Likutis_PP - ps.KiekisRezervuotas) AS stock " +
" FROM   dbo.Sandeliai AS sand INNER JOIN " +
"                    dbo.PrekesSandeliuose AS ps ON sand.SandeliaiId = ps.SandeliaiId INNER JOIN " +
"                   dbo.Prekes AS pr ON ps.PrekesId = pr.PrekesID " +
" WHERE     (sand.SandeliaiId > 1) and  (CONVERT(Int, ps.Likutis_PP - ps.KiekisRezervuotas) >0 or "
+ " CONVERT(Int, ps.Likutis_PP) >0)  and sand.Kodas in ('SGG') " ;
	
	  TreeMap seisud = new TreeMap();
	  TreeMap seisud2 = new TreeMap();
	   TreeMap aa = new TreeMap();
	     
	
	 try {
		 Connection con = getNewConnection();
       Statement st = con.createStatement();
         ResultSet rs = st.executeQuery(cmde);

         while (rs.next()) {
        	 String dealer = rs.getString("LADU").trim();
           String kood = rs.getString("toode").trim();
           double seis = rs.getDouble("stock");
          
           if(seis > 0 ){
        	   if(seisud.get(kood) != null){
        		   String aas = seisud.get(kood)+"";
        		   seisud.put(kood, aas + "" + dealer + "=" +seis + "|");
        	   }else{
        		   seisud.put(kood, dealer + "=" +seis + "|");
        	   }
        	   seisud2.put(kood, seis);
             }
           }
           rs.close();
           st.close();
           
	 }catch(Exception r){
		 
	 }
	 */
    String cmd = "SELECT  Pr.PrekesID as tid, Pr.Kodas AS kood,pl.MES_MES_PAV_TRUMPINYS as nimi2, Pr.Pavad AS nimetus," +
    " CONVERT(Money, tt.PK_PardKaina) AS hind, CASE WHEN pr.mvid = pr.ipokavimomvid OR " +
    " ISNULL(pr.ipokavimomvid, 0) < 2 OR  pr.MVId NOT IN (2, 62733) THEN mv.Kodas ELSE ipokmv.Kodas END AS unit , 0  AS kastis, " +
    "  CONVERT(Money, NULL) AS CostPrice, CONVERT(varchar(20), Pr.KAPKodas) AS ean, CONVERT(varchar(20),  pr.PrekiuGrupesID) AS grupp, '' AS grupp2 ,gr.kodas " +
   " ,  gr.Pavad as grnimi, mes_prior_preke as topstatus ,MES_NR2 as aggroup, pr.mase as kaal FROM  dbo.Prekes AS Pr INNER JOIN dbo.PL_Prekes pl on pl.prekesid=pr.prekesID " +
  " inner join xPrekes tt on tt.PrekesID = pr.PrekesID LEFT JOIN PrekiuGrupes gr on gr.PrekiuGrupesId= pr.PrekiuGrupesID " +
  "  LEFT OUTER JOIN dbo.MVGSantykiai AS mvs ON Pr.MVId = mvs.MVId1 AND Pr.IpokavimoMVId = mvs.MVId2 AND Pr.MVGId = mvs.MVGId INNER JOIN " +
     "  dbo.Mokesciai AS mok ON Pr.MokesciaiId = mok.MokesciaiId INNER JOIN dbo.PrekiuTipai AS pt ON Pr.PrekiuTipaiId = pt.PrekiuTipaiId INNER JOIN " +
     "  dbo.MV ON Pr.MVId = dbo.MV.MVid INNER JOIN  dbo.MV AS IpokMV ON Pr.IpokavimoMVId = IpokMV.MVid  " +
     " WHERE     (Pr.PrekesID > 1) AND (pt.ArSandeliuojama = 1) and Pr.Nenaudoti <> ( 1 ) and " +
  " CASE WHEN ISNUMERIC(gr.Kodas) = 1 THEN CAST(gr.Kodas AS INT) ELSE 0 END between 1100 and 1350 and pl.MES_MES_PAV_TRUMPINYS != '' ";  
      if (isDebug()) {
          System.out.println(cmd);
      }

      try {
        Connection con = null;
        try {
        	 con = getNewConnection();
        }catch(Exception e) {
        	System.exit(0);
        }
       
        Statement st = con.createStatement();
         
          
          ResultSet	 	  
          	      rs = st.executeQuery(cmd);
             
             while (rs.next()) {
        	  boolean goes = true;
        	  String tid = rs.getString("tid");
              String kood = rs.getString("kood").trim();
              String nimi2 = rs.getString("nimi2").trim();
              String nimetus =  rs.getString("nimetus").replaceAll("\"", " ").trim();
               nimetus = nimi2;
              Product prod = new Product(kood);
               goes = false;
             try{
              double price = rs.getDouble("hind");
           	  if(price > 0){
           	     prod.setPrice(1);
                 goes = true;
              }
             }catch(Exception d){
           	  
             }
           
             if(nimetus == null){
           	   goes = false;
             }else{
           	   nimetus = nimetus.trim().replaceAll("\n", " ") ;
             }
           	 
         
              String unit = rs.getString("unit");
              if(unit != null && unit.trim().length()>0){
              	prod.setUnit(unit.trim());
                }
try{
	 prod.setInCases(rs.getInt("kastis"));
}catch (Exception e){
	
}

              String ean = rs.getString("ean");
              if(ean != null && ean.trim().length()>0){
              	prod.setEAN(ean.trim());
              }
       
              String grupp = rs.getString("grupp");
              if(grupp != null && grupp.trim().length()>0){
              	 prod.setGroup(grupp.trim());
              }
              
             /* String grupp2 = rs.getString("grupp2");
              if(grupp2 != null && grupp2.trim().length()>0){
            
              }*/

         
             
                 prod.setName(Converter.enCodeEst(nimetus));
                 
              
             /* if(seisud.get(prod.getID()) != null){
            	  prod.setPartii(seisud.get(prod.getID()) +"");
            	 prod.setStock(Double.parseDouble(seisud2.get(prod.getID()) +""));  
              }*/
                 
                 
                  String grnimi = rs.getString("grnimi");
                 if(grnimi != null && grnimi.trim().length()>0){
                	grnimi =  grnimi.substring(grnimi.length()-2) + grnimi.substring(0,grnimi.length()-2) ;
                	prod.setNameSorting(grnimi + Converter.cleanLitLatEst(nimetus));
                 }
                 
                 //vaata tootel 7636 ja 7637
                 
                 String topstatus = rs.getString("topstatus");
                 if(topstatus != null && topstatus.trim().equals("1")){
                	prod.setStatusTop("1");
                 }
                 
                 
              prod.setStock(-1); 
              prod.setVat("21");
          
              
              String aggroup = rs.getString("aggroup");
              if(aggroup != null && aggroup.trim().length() > 0){
              	prod.setAgentGroup(aggroup.trim());
               }
              
              Double kaal = rs.getDouble("kaal");
              

              
              if(kaal != null && kaal > 0){
            	  prod.setMeasure(kaal);
               }
              
              if(prod.getUnit().equals("kg")) {
              	   prod.setMeasure(1);
                 }
              
              
              if(goes){
            	  prcodes.put(tid, kood);
         		  products.put(nimetus, prod);
                  prodHash.put(prod.getID(), prod.getPrice());  
              }
             
          }
          rs.close();
          st.close();
          con.close();
      } catch (Exception e) {
          getParent().showError(e);
      }
      System.out.println("Vigesta Tooted l�petan ");

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

 	Vector custProds = new Vector();
	
	 String sql1 = "SELECT   part.Kodas as klkood ,  OrderAsID = CONVERT(varchar(20), op.OperacijosId), DeliveredOrderAsID = CONVERT(varchar(20),  " +
     " (CASE WHEN op.OperacijosTipoId = 50 THEN op.OperacijosId ELSE NULL END)), ProductAsId = CONVERT(Int, pr.PrekesId), toode = CONVERT(varchar(35),  " +
     " pr.Kodas), Price = CONVERT(Money, pk.ppKainaBePVM / pk.Kiekis, 4), CostPrice = CONVERT(Money, NULL), kogus =  pk.Kiekis,  " +
    "  ODDiscount = CONVERT(real, 0), CurrencyCode = CONVERT(varchar(2), op.Valiuta), VAT = CONVERT(tinyInt, ISNULL(mok.TarifasProc1, 0) + ISNULL(mok.TarifasProc2, " + 
    "  0) + ISNULL(mok.TarifasProc3, 0)), OrderDetailAsID = CONVERT(varchar(20), ISNULL(ok.KilmeInfo, 'S' + CONVERT(varchar(15), pk.PardavimuKunoId))),  " +
    "  op.OperacijosId , op.Data as kuupaev " +
" FROM         Operacijos AS op WITH(NOLOCK) INNER JOIN " +
" Pard_BendraInf AS pbi WITH(NOLOCK) ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
" Partneriai AS part WITH(NOLOCK) ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
"                     PardavimuKunai AS pk  WITH(NOLOCK) ON op.OperacijosId = pk.OperacijosId INNER JOIN " +
"                     OperacijuKuneliai AS ok WITH(NOLOCK) ON pk.PardavimuKunoId = ok.OperacijosKunoId INNER JOIN " +
"                    Prekes AS pr  WITH(NOLOCK) ON pk.PrekesId = pr.PrekesId INNER JOIN " +
"                   Mokesciai AS mok WITH(NOLOCK) ON pk.MokesciaiId = mok.MokesciaiId " +
"WHERE     0 = 1 AND op.Kilme = 35 and op.Data> getdate()-150 and pk.Kiekis != 0 " +
"UNION ALL " +
"SELECT  part.Kodas as klkood ,    OrderAsID = CONVERT(varchar(20), op.OperacijosId), DeliveredOrderAsID = CONVERT(varchar(20), " + 
"                     (CASE WHEN op.OperacijosTipoId = 50 THEN op.OperacijosId ELSE NULL END)), ProductAsId = CONVERT(Int, pr.PrekesId), toode = CONVERT(varchar(35),  " +
"                     pr.Kodas), Price = CONVERT(Money, pk.ppKainaBePVM / pk.Kiekis, 4), CostPrice = CONVERT(Money, xSav.Savikaina), kogus = pk.Kiekis,  " +
"                     ODDiscount = CONVERT(real, NULL), CurrencyCode = CONVERT(varchar(2), op.Valiuta), VAT = CONVERT(tinyInt, ISNULL(mok.TarifasProc1, 0)  " +
"                     + ISNULL(mok.TarifasProc2, 0) + ISNULL(mok.TarifasProc3, 0)), OrderDetailAsID = CONVERT(varchar(20), ISNULL(ok.KilmeInfo, 'S' + CONVERT(varchar(15),  " +
"                     pk.PardavimuKunoId))), op.OperacijosId , op.Data as kuupaev " +
"FROM         PardavimuKunai AS pk WITH(NOLOCK) INNER JOIN " +
"                      Operacijos AS op ON pk.OperacijosId = op.OperacijosId INNER JOIN " +
"					   Pard_BendraInf AS pbi WITH(NOLOCK) ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
" Partneriai AS part WITH(NOLOCK) ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
"                      OperacijuKuneliai AS ok WITH(NOLOCK) ON pk.PardavimuKunoId = ok.OperacijosKunoId INNER JOIN " +
"                      Prekes AS pr ON pk.PrekesId = pr.PrekesId INNER JOIN " +
"                      Mokesciai AS mok  WITH(NOLOCK)ON pk.MokesciaiId = mok.MokesciaiId LEFT OUTER JOIN " +
"                      xPardavimuKunai_Savikaina AS xSav WITH(NOLOCK) ON pk.PardavimuKunoId = xsav.OperacijosKunoId " +
"WHERE     op.OperacijosTipoId in ( 50,2000020,2000018) and op.Data > getdate()-150 and pk.Kiekis != 0  order by 1,5,14";


	 

        if (isDebug()) {
            System.out.println(sql1);
        }

        try {
          Connection con = this.getNewConnection();
          PreparedStatement stmt = con.prepareStatement(sql1);
          ResultSet	      rs = stmt.executeQuery();

          HashMap kliendid = new HashMap();
          while (rs.next()) {
            String kliendikood = rs.getString("klkood").trim();
          
         	   String tootekood = rs.getString("toode").trim();
         	 double hind = rs.getDouble("Price");
                double kogus = rs.getDouble("kogus");
                
                Customer cust = (Customer)kliendid.get(kliendikood);
                if (cust == null){
                  cust = new Customer(kliendikood);
                }
               Product prod = new Product(tootekood);
                  CustProd cp = new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                               rs.getString("kuupaev").substring(5,10), hind, 0, 0, " ");
               	
               	    cust.addProduct(prod,cp,false);
          
               	    kliendid.put(kliendikood, cust);
                  custProds.add(cp);

            }

          rs.close();
          stmt.close();
          
          
        } catch (Exception e) {
          getParent().showError(e);
        }
       	return custProds;
    }

    public String getPilotDayData(Agent agent){
    	
        return null;

    }


    public String getNonMobileOrders(Agent agent){
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
	return 0;
    }
    


    public void setBulkSales(Map agents) {
       //if (agentCode == null) return 0;
       connectionUp(url);
       
       
         String cmd = "select  a.employeecode AS agent, sum(billamount) AS summa from  "  +
			" (  SELECT     OrderAsID = CONVERT(varchar(20), op.OperacijosId), DeliveredOrderAsID = CONVERT(varchar(20), " + 
         "             (CASE WHEN op.OperacijosTipoId = 50 THEN op.OperacijosId ELSE NULL END)), OrderDate = op.Data, OrderDiscount = CONVERT(Real, 0),  " +
          "            BillCode = CONVERT(varchar(20), op.DokNr), BillIssueDate = op.Data, BillPaymentDate = CONVERT(DateTime, NULL), BillAmount = CONVERT(Money, op.Suma),  " +
          "            PaidAmount = CONVERT(Money, op.ApmoketaSuma), CustomerAsID = CONVERT(Int, part.PartneriaiId), EmployeeAsID = CONVERT(varchar(35), masm.MusuAsmenysId), " +
          "             Employeecode = CONVERT(varchar(35), masm.Kodas), OrderState = CONVERT(Int, NULL), OrderStateExt = CONVERT(Int, NULL), CurrencyCode = CONVERT(varchar(2), " + 
          "            op.Valiuta), DeliveryAddressNo = CONVERT(varchar(2), NULL), OrderRemark = CONVERT(varchar(35), SUBSTRING(okp.Pastaba1, 1, 35)), op.OperacijosId,  " +
          "            CustomerCode = part.Kodas, PaymentTime = CONVERT(SmallInt, msal.Terminas), PayerCode = part.regnr, op.OperacijosTipoId " +
          "       FROM         Operacijos AS op INNER JOIN " +
          "            Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
          "            Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
          "            PartneriaiPA AS partpa ON part.PartneriaiId = partpa.PartneriaiId INNER JOIN " +
          "            MokejimoSalygos AS msal ON partpa.MokejimoSalygosId = msal.MokejimoSalygosId INNER JOIN " +
          "            Pirk_VykdymoInf AS pvi ON pbi.Pirk_VykdymoInfId = pvi.Pirk_VykdymoInfId INNER JOIN " +
          "            MusuAsmenys AS masm ON pvi.MusuAsmenysId = masm.MusuAsmenysId LEFT OUTER JOIN " +
          "            OperacijuKuneliuPastabos AS okp ON op.OperacijosId = okp.OperacijosId " +
         " WHERE     0 = 1 AND op.Kilme = 35 " +
 " UNION ALL " +
" SELECT     OrderAsID = CONVERT(varchar(20), CASE WHEN op.OperacijosTipoId = 50 THEN op2.KilmeInfo ELSE NULL END), DeliveredOrderAsID = CONVERT(varchar(20),  " +
  "                    op.OperacijosId), OrderDate = op.Data, OrderDiscount = CONVERT(Real, 0), BillCode = CONVERT(varchar(20), op.DokNr), BillIssueDate = op.Data,  " +
   "                   BillPaymentDate = CONVERT(DateTime, NULL), BillAmount = CONVERT(Money, op.Suma), PaidAmount = CONVERT(Money, op.ApmoketaSuma),  " +
    "                  CustomerAsID = CONVERT(Int, part.PartneriaiId), EmployeeAsID = CONVERT(varchar(35), masm.MusuAsmenysId), Employeecode = CONVERT(varchar(35),  " +
     "                 masm.Kodas), OrderState = CONVERT(Int, NULL), OrderStateExt = CONVERT(Int, NULL), CurrencyCode = CONVERT(varchar(2), op.Valiuta),  " +
      "                DeliveryAddressNo = CONVERT(varchar(2), NULL), OrderRemark = CONVERT(varchar(35), SUBSTRING(okp.Pastaba1, 1, 35)), op.OperacijosId,  " +
       "               customercode = part.Kodas, PaymentTime = CONVERT(SmallInt, msal.Terminas), PayerCode = part.regnr, op.OperacijosTipoId " +
"FROM         Operacijos AS op INNER JOIN " +
 "                     Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
  "                    Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
   "                   PartneriaiPA AS partpa ON part.PartneriaiId = partpa.PartneriaiId INNER JOIN " +
    "                  MokejimoSalygos AS msal ON partpa.MokejimoSalygosId = msal.MokejimoSalygosId INNER JOIN " +
     "                 Pirk_VykdymoInf AS pvi ON pbi.Pirk_VykdymoInfId = pvi.Pirk_VykdymoInfId INNER JOIN " +
      "                MusuAsmenys AS masm ON pvi.MusuAsmenysId = masm.MusuAsmenysId LEFT OUTER JOIN " +
       "               OperacijuKuneliuPastabos AS okp ON op.OperacijosId = okp.OperacijosId INNER JOIN " +
        "              RysioOperacijos AS rop ON op.OperacijosId = rop.OperacijosId INNER JOIN " +
         "             Operacijos AS op2 ON rop.OperacijosId2 = op2.OperacijosId " +
" WHERE     op.OperacijosTipoId IN (50, 64) AND rop.RysiuTipaiId = 3 AND rop.OperacijosStatusasId = 778 ) " +
 " d INNER JOIN (SELECT  masm.MusuAsmenysId  AS EmployeeAsId, masm.Kodas AS employeecode, " +
        		" LTRIM(masm.Vardas + ' ' + masm.Pavarde) AS nimi,masm.Kodas as ladu FROM dbo.MusuAsmenys AS masm) a  ON a.employeeasid=d.employeeasid " +
			" where MONTH( billissuedate)=MONTH(GETDATE()) AND YEAR( billissuedate)=YEAR(GETDATE()) group by a.employeecode";
         log(cmd);


         
         try {
        	 
        	 HashMap<String ,Double> grsales = new HashMap<String ,Double>();
        
             stmt = con.prepareStatement(cmd);
             //stmt.setString(1, agentCode);
     
             ResultSet rs = stmt.executeQuery();

             while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                 Agent agent = (Agent)agents.get(agentID);
                 
                    }

             rs.close();
             stmt.close();
             con.close();

         } catch (Exception e) {
             getParent().showError(e);
         }

     }





    public ManagerData getManagerData() throws SQLException{
      return null;
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
      else str = str.trim().replace('\'', ' ').replace('\"', ' ').replace(';',' ');
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

    private static double parseDouble(String s){
      if (s.length() > 0) return Double.parseDouble(s);
      else return 0;
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
       public void setDealers(TreeMap aDealers) {
        dealers = aDealers;
      }
     

    public static final String DRIVER_VERSION = "SkaiteVigesta";
    private String             url = "jdbc:odbc:skaita";
    private String             url2 = "jdbc:odbc:report";
    private Connection	       con = null;
    private Vector grouplist = new Vector();
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance(); 
    private TreeMap  dealers = new TreeMap();



	@Override
	public void FixReporter() {
		String fileName = Converter.saveDayReportReport(this.getExcelDir(), rcompany, user, pass);
		if(fileName != null) {
			Converter.sendReportEmail("Dayreport weekly report", fileName, parent);
		}
		System.exit(0);
	}
    

	
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

