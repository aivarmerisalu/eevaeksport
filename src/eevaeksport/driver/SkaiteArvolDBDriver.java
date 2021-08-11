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
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
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
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002fget
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class SkaiteArvolDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Discounter{


    public SkaiteArvolDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
	// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();
	  
	} catch (Exception e) {
	    parent.showError(e);
	}
    }
    
    public String getProductSortingData(){
       	return null;
       }
	
    
    
	private Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(url, getParent().getProps()
				.getProperty("dsn.user"),
				getParent().getProps().getProperty("dsn.pass"));
	}
	
	  private Connection getNewConnection3() throws SQLException{
		    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
		  }

	

	  public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr) {
			String sql = "INSERT into dbo.supervisor values (" + "?, " // agent
																		// C(50),
					+ "?," // agentname C(200),
					+ "?," // clientid C(50),
					+ "?," // clientname C(200),
					+ "?," // prodid C(50),
					+ "?," // prodname C(200),
					+ "getdate() ," // sentdate D,
					+ " '' ," // receivedTime D,
					+ "?," // normalpr N,
					+ "?," // amoun N,
					+ "?," // grouptotal N,
					+ "?," // cattotal N,
					+ "?," // pertotcat I,
					+ "?," // percolcat I,
					+ "?," // perpalcat I,
					+ "?," // pershecat I,
					+ "?," // perrescat I,
					+ "?," // isaction L,
					+ "?," // ispartner L,
					+ "?," // isbrand L,
					+ "?," // isgroup L
					+ "?," // actionaeva N,
					+ "?," // actionbeva N,
					+ "?," // actiontota N,
					+ "?," // custeval N,
					+ "?," // custcomme C(200),
					+ "?," // comment C(200),
					+ "?," // custagent C(200),
					+ "?," // custagentname C(200),
					+ "?," // mustbe I,
					+ "?," // ideal I,
					+ "?," // resstr1 C(200),
					+ "?," // resstr2 C(200),
					+ "?," // resstr3 C(200),
					+ "?," // resint1 I,
					+ "?," // resint2 I,
					+ "?)"; // resint3 I,
					//+ "?)"; // country L

			try {
				System.out.println("jamxxx");
				Connection con = getNewConnection3();
				PreparedStatement ps = con.prepareStatement(sql);
				
				
	        
				for (Iterator iter = wr.getInfo().getLines().iterator(); iter
						.hasNext();) {
					
					eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(
							iter.next());
					ps.setString(1, wr.getInfo().getAgentID());
					ps.setString(2, Converter.deCode(wr.getInfo().getAgentName()));
					ps.setString(3, lw.getLine().getCustID());
					ps.setString(4, Converter.deCode(lw.getLine().getCustName()));
					ps.setString(5, lw.getLine().getProdID());
					ps.setString(6, Converter.deCode(lw.getLine().getProdName()));
					ps.setDouble(7, lw.getLine().getNormalPrice() * 100);
					ps.setDouble(8, lw.getLine().getAmount() * 100);
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
					ps.setDouble(20, lw.getLine().getAction1Eval());
					ps.setDouble(21, lw.getLine().getAction2Eval());
					ps.setDouble(22, lw.getLine().getActionTotalEval());
					ps.setDouble(23, lw.getLine().getCustEval());
					String cc = lw.getLine().getCustComment();
					if (cc == null) {
						cc = " ";
					}
					ps.setString(24, Converter.deCode(cc));

					String cd = lw.getLine().getComment();
					if (cd == null) {
						cd = " ";
					}
					ps.setString(25, Converter.deCode(cd));

					String ce = lw.getLine().getCustAgentId();
					if (ce == null) {
						ce = " ";
					}

					ps.setString(26, ce);

					String cf = lw.getLine().getCustAgentName();
					if (cf == null) {
						cf = " ";
					}
					ps.setString(27, Converter.deCode(cf));
					ps.setInt(28, lw.getLine().getDealNr());
					ps.setInt(29, lw.getLine().getIdealNr());

					String cr1 = lw.getLine().getResStr1();
					if (cr1 == null) {
						cr1 = " ";
					}
					
					ps.setString(30, cr1);

					String cr2 = lw.getLine().getResStr2();
					if (cr2 == null) {
						cr2 = " ";
					}
					ps.setString(31, cr2);

					String cr3 = lw.getLine().getResStr3();
					if (cr3 == null) {
						cr3 = " ";
					}

					Calendar cal = Calendar.getInstance();
					try {
						cal.setTimeInMillis(Long.parseLong(cr3));
					} catch (Exception e) {
						System.out.println("cr3 '" + cr3 + "'");

						// e.printStackTrace();

					}

					ps.setString(32, getDateStr(cal));
					ps.setInt(33, lw.getLine().getResInt1());
					ps.setInt(34, lw.getLine().getResInt2());
					ps.setInt(35, lw.getLine().getResInt3());
				//	System.out.println("ps asse");
					
					
					
					int leng = lw.getLine().getCustID().length();
					String sb =  wr.getInfo().getAgentID() + ";;"
					 + lw.getLine().getCustID()+  ";" +Converter.deCode(lw.getLine().getCustName().substring(0,lw.getLine().getCustName().length() - leng)) +
					   ";"  ;	
				     sb = sb + getDateTimeStr(cal) + ";" + cr2 + ";" + cc + ";" + cd; 
				     sb = sb + ";" + lw.getLine().getAction1Eval() + ";" + lw.getLine().getAction2Eval();//long,latt
					System.out.println("ps asse" + sb);
					
					if(cr1.equals("INVSCAN")){
						makeCSV(sb , cr2 + "-"+ cal.get(Calendar.MILLISECOND));
					}else{
						ps.executeUpdate();	
					}
					
				}

				ps.close();
				con.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	  private void makeCSV(String sb, String scan){
		  StringBuffer sb2 = new StringBuffer();
		  
		  sb2.append("agent;customerIDAXA;customerIDSKAITA;customerName;timestamp;scanned;other;comment;longitude;lattitude\n");
		  sb2.append(sb.replaceAll("\\r\\n|\\r|\\n", " "));
		  
		  String path = "klpscan\\";
		  if(getParent().getProps().getProperty("scan.dir") != null){
			  path = getParent().getProps().getProperty("scan.dir");
		  }
		  if(sb2.length() > 2){
			
				  try{
			    		 Converter.saveFileInUTF8(path + scan + ".csv", sb2);
			    	}catch (Exception e){
			    		
			    	}  
			   }
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
    	   System.out.println(asi);
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
/*
                try {
               	  String cmd = " SELECT  PriceListAsId AS grupp,t.ProductCode AS toode FROM asVwPriceListProducts r  " +
               	  		" INNER JOIN asVwProduct t ON t.ProductAsId=r.ProductAsId WHERE LevelNo is not null ORDER BY ProductName";
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
                              }*/
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

     String sql = "SELECT  CONVERT(varchar(20), md.SkaitaMedisId) AS kood, CONVERT(varchar(50), md.Pavad) AS nimetus,"
     		+ " CONVERT(varchar(20), " +
	               "      CASE md2.SkaitaMedisId WHEN 1 THEN NULL ELSE md2.SkaitaMedisId END) AS ProdGroupParentAsId,"
	               + " CONVERT(varchar(15), md.Kodas) AS ProdGroupCode  " +
	 " FROM         dbo.SkaitaMedis_List AS mdl INNER JOIN  " +
	  "                    dbo.SkaitaMedis AS md ON mdl.SkaitaMedis_ListId = md.SkaitaMedis_ListId  " +
		"				  INNER JOIN   dbo.SkaitaMedis AS md2 ON md.TevasId = md2.SkaitaMedisId  "+
	" WHERE     (mdl.SysVardas = 'PREKES_STD') AND (md.SkaitaMedisId > 1) and  md.TevasID =1  order by md.TevasID" ;

    	    Statement st = con.createStatement();
    	    ResultSet rs = st.executeQuery(sql);

    	    while (rs.next()) {
    	    	
    	    	String kood = rs.getString("kood");
    	  	    String nimetus2 =  rs.getString("nimetus");
    	  	    if(nimetus2.indexOf("-") != -1){
    	  	    	nimetus2 = nimetus2.substring(nimetus2.indexOf("-")+1).trim();
    	  	    }
    	  	  if(nimetus2.indexOf("-") != -1){
  	  	    	nimetus2 = nimetus2.substring(nimetus2.indexOf("-")+1).trim();
  	  	     }
    	  		    gr.put( kood, Converter.enCodeEst(nimetus2.trim()));	
    	  	    
    	    }
    	  
    	    rs.close();
    	    st.close();
    	    con.close();
    	 
    	    return gr;
    	 }


    
    public TreeMap getProdGroupsSecond() throws SQLException {
    	 Connection con = this.getNewConnection();
	      TreeMap gr = new TreeMap();
	  
    TreeMap aa = new TreeMap();
	      String sql1 = "SELECT  CONVERT(varchar(20), md.SkaitaMedisId) AS kood, CONVERT(varchar(50), md.Pavad) AS nimetus, " +
     		" CONVERT(varchar(20),     CASE md2.SkaitaMedisId WHEN 1 THEN NULL ELSE md2.SkaitaMedisId END) AS ProdGroupParentAsId, " +
	        "        CONVERT(varchar(15), md.Kodas) AS ProdGroupCode  " +
	        "   FROM dbo.SkaitaMedis_List AS mdl INNER JOIN  " +
	        "             dbo.SkaitaMedis AS md ON mdl.SkaitaMedis_ListId = md.SkaitaMedis_ListId  " +
		"				  INNER JOIN   dbo.SkaitaMedis AS md2 ON md.TevasId = md2.SkaitaMedisId " +
         "     WHERE     (mdl.SysVardas = 'PREKES_STD') AND (md.SkaitaMedisId > 1) ";
	      

		    Statement st = con.createStatement();
		    ResultSet rs = st.executeQuery(sql1);

		    while (rs.next()) {
		    	String kood = rs.getString("kood");
		  String kood2 = rs.getString("ProdGroupParentAsId");
		  	
		  		 aa.put( kood.trim(),kood2);	
		  	
		  	}
		    rs.close();
		    st.close();

	      
	      
	      
	      
	    String    sql = "	SELECT     CONVERT(varchar(20), md.SkaitaMedisId) AS kood, CONVERT(varchar(50),"
	    		+ " md.Pavad) AS nimetus, CONVERT(varchar(20), " +
	                   "   CASE md2.SkaitaMedisId WHEN 1 THEN NULL ELSE md2.SkaitaMedisId END) AS ProdGroupParentAsId, "
	                   + "CONVERT(varchar(15), md.TevasID) AS ProdGroupCode " +
	 " FROM         dbo.SkaitaMedis_List AS mdl INNER JOIN  " +
	                     " dbo.SkaitaMedis AS md ON mdl.SkaitaMedis_ListId = md.SkaitaMedis_ListId INNER JOIN " +
	                  "    dbo.SkaitaMedis AS md2 ON md.TevasId = md2.SkaitaMedisId " +
	" WHERE     (mdl.SysVardas = 'PREKES_STD') AND (md.SkaitaMedisId > 1) and md.TevasID > 1 " ;


	     st = con.createStatement();
	     rs = st.executeQuery(sql);

	    while (rs.next()) {
	    	String kood = rs.getString("kood");
	  	String nimetus2 =  rs.getString("nimetus");
	  	
		String kood2 = rs.getString("ProdGroupCode");
	  	
	  	
	  	if(nimetus2.indexOf("-") != -1){
  	    	nimetus2 = nimetus2.substring(nimetus2.indexOf("-")+1).trim();
  	    }
  	  if(nimetus2.indexOf("-") != -1){
	    	nimetus2 = nimetus2.substring(nimetus2.indexOf("-")+1).trim();
	     }
  	  
  	  //Siin asendame ylema grupiga kui pon olemas
  	  if(aa.get(kood2.trim()) != null){
  		  kood2 = aa.get(kood2.trim()) + "";
  	  }
  	
  	  if(aa.get(kood2.trim()) != null){
  		  kood2 = aa.get(kood2.trim()) + "";
  	  }
  	  
	  		 gr.put( kood2.trim() +kood.trim(), Converter.enCodeEst(nimetus2.trim()) + " " +kood);	
	  	
	  	}
	    rs.close();
	    st.close();
	    con.close();
	 
	    return gr;
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
    
   private TreeMap logistics = null;
   
   
   
      
    public TreeMap getAgents() {
    
    	
    	String vilnManas = getParent().getProps().getProperty("viln.managers");
    	
           	TreeMap agents = new TreeMap();
        try {  
    			 connectionUp(url);
        String  cmd = "select  masm.MusuAsmenysId  AS EmployeeAsId, masm.Kodas AS yksus, LTRIM(masm.Vardas + ' ' + masm.Pavarde) AS nimi " +
				 " FROM dbo.MusuAsmenys AS masm " +
				 " INNER JOIN pl_musuasmenys abi ON masm.MusuAsmenysId =abi.MusuAsmenysId where Pareigos like  '%Parda%' " ;
				


            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim() );
              String nimi = agent.getID() + " " +  rs.getString("nimi").trim();
              agent.setName(nimi);
              agent.setIsPilot(true);
              if(vilnManas.contains(agent.getID())){
            	  agent.setObject("VILN");
              }else{
            	  agent.setObject("MUU");
              }
              agents.put(nimi, agent);
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
       TreeMap hinnad =  getKPHinnad(); //saame baas hinnad mida kasa ei pane 
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
        	   
        	   connectionUp(url);
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
            	   if(hinnad.get(idd) != null ){
            		   if(Double.parseDouble(hinnad.get(idd)+"") == price){
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
     
             
             if(baashk != null && baashk.size() > 0){
            	
             for(int r = 0; r<baashk.size();r++){
            	 
         String asi = (baashk.elementAt(r) + "");
       		  con = getNewConnection();
       		  CallableStatement  msProcedure = con.prepareCall( "uspv_Kainininkas '" + 
       				  asi.substring(1) + "', 'eur' ,0" );
       		    ResultSet rs3 = msProcedure.executeQuery();
       	      while(rs3.next()){
       	    	  String toode = rs3.getString("prekesid");
       	    	  String hind = rs3.getString("kaina");
       	    //  	System.out.println( "Toode " +  toode + " hind " + hind);
       	      	MatrixLine mm = new MatrixLine();
       	      	mm.setProdID(toode);
       	      	mm.setCliGroup(asi);
       	      	mm.setPrice(Double.parseDouble(hind));
       	      	mm.setProdLevel("0");
       	      }
       	       rs3.close();
       	       msProcedure.close();
       	    con.close();
       	  
             }
            	 
               	 
              
             }
            	   
             
             
             
             
             connectionDown();
           }
           catch (SQLException ex) {
             ex.printStackTrace();
           }
           System.out.println("Maatrix suurus " + lines.size());
         return lines;
    	
       }
private Vector baashk = null;
      
      public TreeMap getCustomers(Agent agent){
    	  
        String cmd = "SELECT CONVERT(Int, part.PartneriaiId) AS CustomerASId, CONVERT(varchar(20), part.Kodas) AS kood, CONVERT(varchar(40), part.Pavad) AS nimi,   " +
                   "   part.regnr AS payer ,  rtrim(adr.Adresas)  + case when adr.Indeksas IS NULL then '' else ' ' + rtrim(adr.Indeksas)end + ' ' + " +
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
            "                     dbo.PartneriaiPa AS partpa ON part.PartneriaiId = partpa.PartneriaiId INNER JOIN  " +
             "               dbo.MokejimoSalygos AS msal ON partpa.MokejimoSalygosID = msal.MokejimoSalygosID INNER JOIN " +  
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
     " WHERE      masm.kodas is not null ";
	
        log(cmd);

        String lepingud = "select kl.PartneriaiId as klient , kl.NuolaiduSutartysId  as leping from dbo.NuolaiduSutarciuPartneriai kl inner join  " +
           " dbo.NuolaiduSutartys AS sut ON kl.NuolaiduSutartysId = sut.NuolaiduSutartysId and " +
			"	   (CONVERT (date, GETDATE()) between   cast(sut.DataNuo_Int as datetime) and  cast(sut.DataIki_Int as datetime) or (sut.DataNuo_Int = 0 and sut.DataIki_Int = 999999))";
        HashMap lepMap = new HashMap();
        HashMap baasMap = new HashMap();
	TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        baashk = new Vector();
	try {
		
		Connection con2 = this.getNewConnection();
        PreparedStatement  stmt = con2.prepareStatement(lepingud);
        ResultSet	      rs = stmt.executeQuery();

	   while (rs.next()) {
		 String klient = (rs.getString("klient").trim()); 
		 String leping = (rs.getString("leping").trim());
		 if(lepMap.get(klient) != null){
			 lepMap.put(klient, lepMap.get(klient) +"|"+leping); 
		 }else{
			 lepMap.put(klient, leping); 
		 }
		
	   }
		 rs.close();
		 stmt.close();
		
		 
		 String sqlb = "SELECT Partneriai.PartneriaiId as klient,(SELECT  Nuolaidukainininkusudetis.PrekiuKainuKategorijosId AS hk FROM " +
   "{oj{oj{oj  dbo.NuolaiduKainininkuSudetis Nuolaidukainininkusudetis    LEFT OUTER JOIN dbo.NuolaiduKainininkai Nuolaidukainininkai " +
   "  ON  Nuolaidukainininkusudetis.NuolaiduKainininkaiId = Nuolaidukainininkai.NuolaiduKainininkaiId} " +
   " LEFT OUTER JOIN dbo.PrekiuKainuKategorijos Prekiukainukategorijos  " +
   "ON  Nuolaidukainininkusudetis.PrekiuKainuKategorijosId = Prekiukainukategorijos.PrekiuKainuKategorijosId}  " +
   " LEFT OUTER JOIN dbo.PartneriuKategorijos Partneriukategorijos " +
   "ON  Nuolaidukainininkusudetis.NuolaiduKainininkaiId = Partneriukategorijos.NuolaiduKainininkaiId} " +
" WHERE  Prekiukainukategorijos.Neaktyvi <> ( 1 ) " +
 "  AND  Nuolaidukainininkusudetis.DataIki_Int = ( 999999 ) and Nuolaidukainininkusudetis.PrekiuKainuKategorijosId  != 93776 and Partneriukategorijos.PartneriuKategorijosId = Pa.PartneriuKategorijosId )" +
 " AS hk FROM   {oj{oj  dbo.Partneriai Partneriai    LEFT OUTER JOIN dbo.PartneriaiPa Pa " +
 "    ON  Partneriai.PartneriaiId = pa.PartneriaiId} " +
 "   LEFT OUTER JOIN dbo.PL_PARTNERIAI Pl_partneriai " +
 "  ON  Partneriai.PartneriaiId = Pl_partneriai.PARTNERIAIID} " +
" WHERE   Pa.PartneriuKategorijosId !=1 order by Partneriai.PartneriaiId";
		 //baas hinnakirjade leidmine
		
		 
		 
		 stmt = con2.prepareStatement(sqlb);
	             rs = stmt.executeQuery();

		   while (rs.next()) {
			 String klient = (rs.getString("klient").trim()); 
			 String hk = rs.getString("hk");
			 if(hk != null && hk.trim().length() > 0){
				 baasMap.put(klient, "B" + hk.trim()); 
				 if(baashk!= null && !baashk.contains("B" + hk.trim())){
					 baashk.add("B" + hk.trim());
				 } 
			 }
				}
			 rs.close();
			 stmt.close();
			
		 
		 
		 
		 
          stmt = con2.prepareStatement(cmd);
       	      rs = stmt.executeQuery();

	   while (rs.next()) {
		   
		   
		   String id = rs.getString("CustomerASId").trim();
		   Customer cust = new Customer(rs.getString("kood").trim());
		   String nim = rs.getString("nimi").replaceAll("\""," ").trim();
		   
		 String nimi = Converter.enCodeEst(nim);
		 String payer = Converter.enCodeEst(rs.getString("payer").replaceAll("\""," ").trim());
		 String addr =  Converter.enCodeEst(rs.getString("Address").replaceAll("\""," ").trim());
		 String vatno = rs.getString("vatno");
		 if(vatno != null){
			vatno = Converter.enCodeEst(vatno.replaceAll("\""," ").trim()); 
			 cust.setVatnr(vatno);
		 }
		 
		 
		  String term = rs.getString("paym");
                cust.setPayer(payer);
                cust.setRegnr(payer);
                
                if(nimi.startsWith("UAB ")){
                	nimi = (nimi.substring(4,nimi.length()) + " UAB").trim();
                }
                cust.setName(nimi + " " + addr);
                try{
                	String aadr = Converter.enCodeEst(addr.trim());
                	aadr = Converter.adressLink(aadr, "Lithuania");
                	 cust.setAddress(aadr);
                }catch(Exception aa){
                	
                }
               
                
                
               /* String ale = rs.getString("ale");
                if (ale != null && ale.length() > 0){
                  cust.setDiscount(Double.parseDouble(ale.trim()));
                }*/
              
                cust.setPaymTerm(parent.make(term));
              
                double debti = rs.getDouble("debt");
               
                	cust.setDebt(debti);
                //cust.setVisit(rs.getString("visit"));
                
                //String info = rs.getString("info");
               // cust.setPersonalData(enCode(parent.make(info)));
                
                String agen = rs.getString("agent");
                if(agen != null){
                  cust.addAgent(new Agent(agen.trim()));
                  cust.setMainAgent(agen.trim());
                }
                boolean goes = true;
                
                
                cust.addAgent(new Agent("HOUSE"));
               /* String group = rs.getString("grupp");
                if(group != null){
                  cust.setGroup(group.trim());
                }
                String tyyp = rs.getString("tyyp");
                int aktive = rs.getInt("aktiivne");
               
                if(tyyp != null && tyyp.trim().length() >0 && tyyp.trim().equals("NENAUDOJAMAS")){
                   goes = false;
                }
                if(aktive == 1){
                	goes = false;
                }*/
                String aadrjur = rs.getString("jurid");
                if(aadrjur != null && aadrjur.trim().length()>0){
                  cust.setJuridics(Converter.enCodeEst(aadrjur.trim()));
                }
                
                String aletext = rs.getString("aletext");
                if(aletext != null && aletext.trim().length()>0){
                  cust.setZipcode(Converter.enCodeEst(aletext.trim()));
                }
                String hinnak = rs.getString("hinnak");
                if(hinnak != null && hinnak.trim().length()>0){
                  cust.setGroup(hinnak.trim());
                }
                
                String hinnak2 = rs.getString("hinnak2");
                if(hinnak2 != null && !hinnak2.trim().equals("1")){
                	if(cust.getGroup().trim().length() > 0 ){
                		 cust.setGroup(cust.getGroup() + "|K" + hinnak2.trim());
                	}else{
                		 cust.setGroup("K" + hinnak2.trim());
                	}
                 
                }
                 if(lepMap.get(id) != null){
                	 if(cust.getGroup().trim().length() > 0 ){
                		 cust.setGroup(cust.getGroup() + "|" +lepMap.get(id));
                	 }else{
                		 cust.setGroup("" +lepMap.get(id)); 
                	 }
                 }
                 
                 if(baasMap.get(id) != null){
                	 if(cust.getGroup().trim().length() > 0 ){
                		 cust.setGroup(cust.getGroup() + "|" +baasMap.get(id));
                	 }else{
                		 cust.setGroup("" +baasMap.get(id)); 
                	 }
                 }
                
                if(goes){
                	 customers.put(Converter.cleanLitLatEst(nim+addr), cust);
                     custMap.put(cust.getID(), cust);	
                }
               
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
		" (case when OperacijosTipoId =64 then -1 else 1 end)*billamount-paidamount AS summa  , datediff(day,billissuedate+paymenttime ,getdate())as yle ,billissuedate as kuup from  " +
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
" d  where billamount-paidamount>0 and " +
 "         billissuedate > getdate()-600  order by CustomerCode,billissuedate+paymenttime";

      Connection con = this.getNewConnection();
    PreparedStatement  stmt = con.prepareStatement(sql);


      ResultSet rs = stmt.executeQuery();

      while(rs.next()){
    	  try{
    		    Debt debt = new Debt();
    	        debt.setCustID(rs.getString("klient").trim());
    	     
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
    
    
    
    

	
    
    
    
    private TreeMap getKPHinnad(){
  	  TreeMap res = new TreeMap();
  	  String  hk = "79001";
  	  
  	 
  	  
  	  try{
  		  
  		  String baas = "(select PrekiuKainuKategorijosID as hk from NuolaiduKainininkuSudetis " +
  		  		"where nuolaidukainininkaiid= 1 and DataIki_Int = 999999)";
  		  
  		  Connection con = this.getNewConnection();
  	    PreparedStatement  stmt = con.prepareStatement(baas);


  	      ResultSet rs = stmt.executeQuery();

  	      while(rs.next()){
  	    	hk = rs.getInt("hk") + "";  
  	      }
  	      rs.close();
  	      stmt.close();
  	      con.close();
  		  
  		  String sql = "uspv_Kainininkas '" + hk + "', 'eur' ,0";
  		  con = getNewConnection();
  		  CallableStatement  msProcedure = con.prepareCall( sql );
  		     
  			
  	       //  System.exit(0);
  	         //stmt2.setTimestamp(2, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
  	      ResultSet rs3 = msProcedure.executeQuery();
  	      while(rs3.next()){
  	    	  String toode = rs3.getString("prekesid");
  	    	  String hind = rs3.getString("kaina");
  	    //  	System.out.println( "Toode " +  toode + " hind " + hind);
  	      	res.put(toode, hind);
  	      }
  	       rs3.close();
  	       msProcedure.close();
  	       con.close();
  	  }catch (Exception e){
  		 e.printStackTrace();
  	  }
  	  
  	  return res;
    }

    private TreeMap prcodes = new TreeMap();
    
    
    public TreeMap getProducts(Agent agent){
    	
	TreeMap products = new TreeMap();
	TreeMap hinnad = getKPHinnad();
	//getSaveStockInfo();
	prcodes = new TreeMap();
	
	
	String  cmde = "  SELECT    CONVERT(varchar(20), sand.Kodas) AS LADU, pr.kodas AS toode, CONVERT(Int, ps.Likutis_PP) AS UnitsInStock, CONVERT(Int,  " +
            "  ps.Likutis_PP - ps.KiekisRezervuotas) AS stock " +
" FROM   dbo.Sandeliai AS sand INNER JOIN " +
"                    dbo.PrekesSandeliuose AS ps ON sand.SandeliaiId = ps.SandeliaiId INNER JOIN " +
"                   dbo.Prekes AS pr ON ps.PrekesId = pr.PrekesID " +
" WHERE     (sand.SandeliaiId > 1) and  (CONVERT(Int, ps.Likutis_PP - ps.KiekisRezervuotas) >0 or "
+ " CONVERT(Int, ps.Likutis_PP) >0)  and sand.Kodas in ('S','GA','GS') " ;
	  TreeMap seisud = new TreeMap();
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
        	   
             }
           }
           rs.close();
           st.close();
            
            
            
          String sql1 = "SELECT  CONVERT(varchar(20), md.SkaitaMedisId) AS kood, CONVERT(varchar(50), md.Pavad) AS nimetus, " +
       		" CONVERT(varchar(20),     CASE md2.SkaitaMedisId WHEN 1 THEN NULL ELSE md2.SkaitaMedisId END) AS ProdGroupParentAsId, " +
  	        "        CONVERT(varchar(15), md.Kodas) AS ProdGroupCode  " +
  	        "   FROM dbo.SkaitaMedis_List AS mdl INNER JOIN  " +
  	        "             dbo.SkaitaMedis AS md ON mdl.SkaitaMedis_ListId = md.SkaitaMedis_ListId  " +
  		"				  INNER JOIN   dbo.SkaitaMedis AS md2 ON md.TevasId = md2.SkaitaMedisId " +
           "     WHERE     (mdl.SysVardas = 'PREKES_STD') AND (md.SkaitaMedisId > 1) ";
  	      

  		     st = con.createStatement();
  		     rs = st.executeQuery(sql1);

  		    while (rs.next()) {
  		    	String kood = rs.getString("kood");
  		  String kood2 = rs.getString("ProdGroupParentAsId");
  		  	
  		  		 aa.put( kood.trim(),kood2);	
  		  	
  		  	}
  		    rs.close();
  		    st.close();
           
           
	 }catch(Exception r){
		 
	 }
	
	
	TreeMap inde = new TreeMap();
	 
    String cmd = "SELECT  Pr.PrekesID as tid, Pr.Kodas AS kood, Pr.Pavad AS nimetus, CONVERT(Money, Pr.PK_PardKaina) AS hind, CASE WHEN pr.mvid = pr.ipokavimomvid OR " +
 "                    ISNULL(pr.ipokavimomvid, 0) < 2 OR  " +
  "                   pr.MVId NOT IN (2, 62733) THEN mv.Kodas ELSE ipokmv.Kodas END AS unit , pl.VntDez  AS kastis, " + 
  "    CONVERT(Money, NULL) AS CostPrice, CONVERT(varchar(20), Pr.KAPKodas) AS ean,  " +
  "                   CONVERT(varchar(20),  pr.PrekiuGrupesID) AS grupp,TevasID as gr2, md.SkaitaMedisId AS grupp2, CONVERT(char(1), 'Y') AS Valid	" +
" FROM         dbo.Prekes AS Pr INNER JOIN dbo.PL_Prekes pl on pl.prekesid=pr.prekesID LEFT OUTER JOIN " +
 "                    dbo.MVGSantykiai AS mvs ON Pr.MVId = mvs.MVId1 AND Pr.IpokavimoMVId = mvs.MVId2 AND Pr.MVGId = mvs.MVGId INNER JOIN " +
 "                    dbo.Mokesciai AS mok ON Pr.MokesciaiId = mok.MokesciaiId INNER JOIN " +
 "                    dbo.PrekiuTipai AS pt ON Pr.PrekiuTipaiId = pt.PrekiuTipaiId INNER JOIN " + 
 "                    dbo.MV ON Pr.MVId = dbo.MV.MVid INNER JOIN " +
 "                    dbo.MV AS IpokMV ON Pr.IpokavimoMVId = IpokMV.MVid LEFT OUTER JOIN " +
 "                    dbo.SkaitaMedis_Prekes AS mdp ON Pr.PrekesID = mdp.PrekesID LEFT OUTER JOIN " +
 "                    dbo.SkaitaMedis AS md ON mdp.SkaitaMedisId = md.SkaitaMedisId " +
 "      WHERE     (Pr.PrekesID > 1) AND (pt.ArSandeliuojama = 1) and Pr.Nenaudoti <> ( 1 ) and md.SkaitaMedis_ListId IN (2)" ;

    //Prekiugrupes.Pavad
  String cmd2 = "SELECT Skaitamedis.Kodas  AS grouptext,  Skaitamedis_lapai.Indeksas AS jrk, Prekes.Kodas AS kood " +
    " FROM  {oj{oj{oj  dbo.SkaitaMedis Skaitamedis    LEFT OUTER JOIN dbo.SkaitaMedis_Lapai Skaitamedis_lapai    ON  Skaitamedis.SkaitaMedisId = Skaitamedis_lapai.SkaitaMedisId} " +
    " LEFT OUTER JOIN dbo.Prekes Prekes  ON  Skaitamedis_lapai.LapaiId = Prekes.PrekesID} "+
    " LEFT OUTER JOIN dbo.PrekiuGrupes Prekiugrupes ON  Prekes.PrekiuGrupesId = Prekiugrupes.PrekiuGrupesID} " +
    " WHERE  Skaitamedis_lapai.Indeksas <> ( '' ) ORDER BY Prekiugrupes.Pavad, Skaitamedis.Kodas,Skaitamedis_lapai.Indeksas";
    
    
      if (isDebug()) {
          System.out.println(cmd);
      }

      try {
        Connection con = getNewConnection();
        Statement st = con.createStatement();
          HashMap prodHash = new HashMap();
          
          ResultSet	      rs = st.executeQuery(cmd2);
          
          while (rs.next()) {
        	  String ti = rs.getString("grouptext").trim();
         	    String tid = rs.getString("jrk").trim();
         	    if(tid.length() == 1){
         	    	tid = "00" + tid;
         	    }
         	   if(tid.length() == 2){
        	    	tid = "0" + tid;
        	    }
                String kood = rs.getString("kood").trim();
                inde.put(kood , ti + "" + tid);
          }
          rs.close();
     	  
          	      rs = st.executeQuery(cmd);
             
             while (rs.next()) {
        	  boolean goes = true;
        	  String tid = rs.getString("tid");
              String kood = rs.getString("kood").trim();
            
              String nimetus =  rs.getString("nimetus");
               
              Product prod = new Product(kood);
               goes = false;
             try{
           	  double price = Double.parseDouble((String)hinnad.get(tid));
                 if(price == 0){
              	   price = 1;
                 }
                 prod.setPrice(price);
                 goes = true;
               //  System.out.println("hind " + prod.getPrice());  
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
       
              String grupp = rs.getString("gr2");
              if(grupp != null && grupp.trim().length()>0){
              	 prod.setGroup(grupp.trim());
              }
              
              String grupp2 = rs.getString("grupp2");
              if(grupp2 != null && grupp2.trim().length()>0){
            	  if(aa.get(grupp.trim()) != null){
            		  grupp = aa.get(grupp.trim())+"";
            	  }
            	  if(aa.get(grupp.trim()) != null){
            		  grupp = aa.get(grupp.trim())+"";
            	  }
              	 prod.setGroupSecond(grupp.trim() + "" +grupp2.trim());
              }

         
              if(nimetus != null){
                 prod.setName(Converter.enCodeEst(nimetus));
                 
                  if(inde.get(prod.getID()) != null){
                	  String aaa = inde.get(prod.getID()) + "";
                     // prod.setNameSorting(aaa + nimetus);
                   //   System.out.println("Sorting " + aaa + nimetus);
                  }
             
              }
              
              if(seisud.get(prod.getID()) != null){
            	  prod.setPartii(seisud.get(prod.getID()) +"");
            	//  prod.setStock(Double.parseDouble(seisud.get(prod.getID()) +""));  
              }
             
              prod.setVat("21");
          
              
              
              if(goes){
            	  prcodes.put(tid, kood);
         		   products.put(prod.getName(), prod);
                  prodHash.put(prod.getID(), prod);  
              }
             
          }
          rs.close();
          st.close();
          con.close();
      } catch (Exception e) {
          getParent().showError(e);
      }
      System.out.println("Arvol Tooted l�petan ");

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
     " pr.Kodas), Price = CONVERT(Money, pk.ppKainaBePVM / pk.Kiekis, 4), CostPrice = CONVERT(Money, NULL), kogus = CONVERT(Int, pk.Kiekis),  " +
    "  ODDiscount = CONVERT(real, 0), CurrencyCode = CONVERT(varchar(2), op.Valiuta), VAT = CONVERT(tinyInt, ISNULL(mok.TarifasProc1, 0) + ISNULL(mok.TarifasProc2, " + 
    "  0) + ISNULL(mok.TarifasProc3, 0)), OrderDetailAsID = CONVERT(varchar(20), ISNULL(ok.KilmeInfo, 'S' + CONVERT(varchar(15), pk.PardavimuKunoId))),  " +
    "  op.OperacijosId , op.Data as kuupaev " +
" FROM         Operacijos AS op INNER JOIN " +
" Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
" Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
"                     PardavimuKunai AS pk ON op.OperacijosId = pk.OperacijosId INNER JOIN " +
"                     OperacijuKuneliai AS ok ON pk.PardavimuKunoId = ok.OperacijosKunoId INNER JOIN " +
"                    Prekes AS pr ON pk.PrekesId = pr.PrekesId INNER JOIN " +
"                   Mokesciai AS mok ON pk.MokesciaiId = mok.MokesciaiId " +
"WHERE     0 = 1 AND op.Kilme = 35 and op.Data> getdate()-120 " +
"UNION ALL " +
"SELECT  part.Kodas as klkood ,    OrderAsID = CONVERT(varchar(20), op.OperacijosId), DeliveredOrderAsID = CONVERT(varchar(20), " + 
"                     (CASE WHEN op.OperacijosTipoId = 50 THEN op.OperacijosId ELSE NULL END)), ProductAsId = CONVERT(Int, pr.PrekesId), toode = CONVERT(varchar(35),  " +
"                     pr.Kodas), Price = CONVERT(Money, pk.ppKainaBePVM / pk.Kiekis, 4), CostPrice = CONVERT(Money, xSav.Savikaina), kogus = CONVERT(Int, pk.Kiekis),  " +
"                     ODDiscount = CONVERT(real, NULL), CurrencyCode = CONVERT(varchar(2), op.Valiuta), VAT = CONVERT(tinyInt, ISNULL(mok.TarifasProc1, 0)  " +
"                     + ISNULL(mok.TarifasProc2, 0) + ISNULL(mok.TarifasProc3, 0)), OrderDetailAsID = CONVERT(varchar(20), ISNULL(ok.KilmeInfo, 'S' + CONVERT(varchar(15),  " +
"                     pk.PardavimuKunoId))), op.OperacijosId , op.Data as kuupaev " +
"FROM         PardavimuKunai AS pk INNER JOIN " +
"                      Operacijos AS op ON pk.OperacijosId = op.OperacijosId INNER JOIN " +
"					   Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
" Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
"                      OperacijuKuneliai AS ok ON pk.PardavimuKunoId = ok.OperacijosKunoId INNER JOIN " +
"                      Prekes AS pr ON pk.PrekesId = pr.PrekesId INNER JOIN " +
"                      Mokesciai AS mok ON pk.MokesciaiId = mok.MokesciaiId LEFT OUTER JOIN " +
"                      xPardavimuKunai_Savikaina AS xSav ON pk.PardavimuKunoId = xsav.OperacijosKunoId " +
"WHERE     op.OperacijosTipoId = 50 and op.Data > getdate()-120";



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
          
          String sql = "SELECT  part.Kodas as saaja ,   toode = CONVERT(varchar(35),   pr.Kodas), " +
					" sum(case when op.Data > getdate()-30 and pk.Kiekis > 0 then pk.Kiekis else 0 end) as valjakogus , " +
					" sum(case when op.Data > getdate()-30 and pk.Kiekis < 0 then pk.Kiekis else 0 end) as tagkogus " +
					"	 FROM         PardavimuKunai AS pk INNER JOIN " +
                    "  Operacijos AS op ON pk.OperacijosId = op.OperacijosId INNER JOIN " +
					"   Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
" Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
                    "  OperacijuKuneliai AS ok ON pk.PardavimuKunoId = ok.OperacijosKunoId INNER JOIN " +
                    "  Prekes AS pr ON pk.PrekesId = pr.PrekesId INNER JOIN " +
                    "  Mokesciai AS mok ON pk.MokesciaiId = mok.MokesciaiId LEFT OUTER JOIN " +
                    "  xPardavimuKunai_Savikaina AS xSav ON pk.PardavimuKunoId = xsav.OperacijosKunoId " +
" WHERE     op.OperacijosTipoId = 50 and op.Data > getdate()-50 GROUP BY part.Kodas,pr.Kodas";
          
          PreparedStatement pst3 = con.prepareStatement(sql);
          ResultSet rs1 = pst3.executeQuery();
          while (rs1.next()) {
            String klient = rs1.getString("saaja").trim();
            String toode = rs1.getString("toode").trim();
            double pkogus = rs1.getDouble("valjakogus");
            double nkogus = rs1.getDouble("tagkogus");
           // System.out.println(toode+"pkogus"+pkogus);

            Customer c = (Customer) kliendid.get(klient);
            if (c != null) {
              //System.out.println("klient"+c.getName());
              ArrayList al = (ArrayList) c.getProducts().get(toode);
              if (al != null) {
                CustProd cp = (CustProd) al.get(1);
                if (cp != null) {
                  cp.setMovedStockPluss(pkogus);
                  if(nkogus > 0){
                	  cp.setMovedStockMinus(nkogus);
                  }
                  custProds.add(cp);
                }
              }
            }
          }
          rs1.close();
          pst3.close();
          con.close();
          
          
          
          
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
      String sql = "SELECT clid AS klient,docsum AS summa,linecount AS ridu,text AS tekst," +
                   "sentlong AS kuupaev FROM dayreport WHERE sentdate>(DATE()-30) ORDER BY clid";
      Connection c = DriverManager.getConnection(url2);
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(sql);


      String sql2 = "SELECT SL01001 AS kood, SL01040 AS aastamyyk,SL01041 AS kuumyyk,SY24003 AS agendinimi, SL01006 AS kontaktisik " +
                    ",SL01011 AS telefon, SL01024 AS maksetin,SL01036 AS ale" +
                    " FROM SL010100 LEFT JOIN SY240100 ON SY24002=SL01035 ORDER BY " +
                    "SL01002";
      connectionUp();
      Statement st2 = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs2 = st2.executeQuery(sql2);

      StringBuffer sb = new StringBuffer();
      String sep = ";";
      while(rs.next()){
        String cliID = rs.getString("klient").trim();
        sb.append(cliID + sep);
        sb.append(rs.getString("kuupaev").trim() + sep);
        sb.append(rs.getDouble("summa") + sep);
        sb.append(rs.getInt("ridu") + sep);
        String tekst = ensureLen(rs.getString("tekst")," ");
        tekst = tekst.replace('\'', ' ').replace('\"', ' ').replace(';', ' ');
        if (tekst == null) tekst = " ";

        sb.append(tekst + sep);

        boolean has = scrollTo(cliID, rs2);
        if (has){
         sb.append(ensureLen(rs2.getString("aastamyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("kuumyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
         sb.append(ensureLen(rs2.getString("kontaktisik"), " ") + sep);
         String tel =  rs2.getString("telefon") ;
         tel.replace('\'', ' ').replace('\"', ' ').replace(';',' ');
         sb.append(ensureLen(tel, " ") + sep);
        // System.out.println(ensureLen(tel, " "));
         sb.append(ensureLen(rs2.getString("maksetin"), " ") + sep);
         sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
        }

        sb.append("\n");
      }

      ManagerData md = new ManagerData();
      md.setClientVisitInfo(sb.toString());

      sql = "SELECT OR20003 AS klient,OR20019 AS agent,OR20017 AS agentname FROM OR200100 " +
          "WHERE OR20015>? GROUP BY OR20003,OR20019,OR20017 ORDER BY OR20003";

      PreparedStatement ps = con.prepareStatement(sql);
      ps.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
      rs = ps.executeQuery();
      StringBuffer bf2 = new StringBuffer();
      while(rs.next()){
        bf2.append(rs.getString("klient").trim() + sep);
        bf2.append(rs.getString("agent").trim() + sep);
        String agentName = rs.getString("agentname");
         if (agentName != null) agentName = agentName.trim();
        if (agentName == null) agentName = " ";
        bf2.append(agentName + "\n");
      }
      rs.close();
      ps.close();
      con.close();

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
     

    public static final String DRIVER_VERSION = "SkaiteArvol";
    private String             url = "jdbc:odbc:Arvol";
    private String             url2 = "jdbc:odbc:report";
    private Connection	       con = null;
    private Vector grouplist = new Vector();
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance(); 
    private TreeMap  dealers = new TreeMap();
    

	
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

