
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
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
public class SolcraftJunDBDriver extends AbstractDBDriver implements Discounter, Manager,Promo, BulkUpdater{

 
    public SolcraftJunDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
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
      
        if(parent.getProps().getProperty("grupp8") != null){
    	  	firma8 = parent.getProps().getProperty("grupp8") ;
    	  }
      
        if(parent.getProps().getProperty("grupp9") != null){
    	  	firma9 = parent.getProps().getProperty("grupp9") ;
    	  }
      
  
        try {
            Class.forName(databaseDriverName).newInstance();
         ddd = DriverManager.getConnection("jdbc:odbc:finance", "SYSDBA", "master");
        try{
        	   bbb = DriverManager.getConnection("jdbc:odbc:finance", "SYSDBA", "master");	
        }catch(Exception ee){
        	
        }
      
         locProps = this.loadProps("groupreplace.properties");
        } catch (Exception e) {
            parent.showError(e);
        }
    	user =  getParent().getProps().getProperty("rest.user");
        pass = getParent().getProps().getProperty("rest.pass");
        rcompany = getParent().getProps().getProperty("rest.company");
         }
      
      String user =  null;
      String pass = null;
      String rcompany = null;
    
    HashSet ags = new HashSet();

    public String getSaveDebtInfo() {
    	/*TreeMap agents = this.getAgents();
    	
    	Iterator enume = agents.values().iterator();
    	while(enume.hasNext()){
    	
    		Agent aa = (Agent)enume.next();
    	
    		String sql = "update dayreport set clgroup ='" + aa.getLanguage() + "' where agent = '" +aa.getID() + "' and startdate > current_timestamp -10";
    		System.out.println(sql);
    		try{
    			Connection con = getNewCon();
      	        Statement stmt = con.createStatement();
      	        stmt.executeUpdate(sql);	
    		}catch(Exception e){
    		//e.printStackTrace();	
    		}
          }*/
    	
    	 makePictureCat();
    	 System.exit(0);
       	return null;
       }
    
    private void makePictureCat(){
	     
        String cmd = "SELECT pr.artikuls AS toode  FROM preces  " +
     	"		  pr INNER JOIN grupas g ON g.grupa=pr.grupa   " +
          "        where (pr.act='+' and pr.nosauk2 >'') OR  (g.grupa='50' and pr.nosauk2 >'') or g.grupa='041' " +
     		"	 order by pr.nosauk2";
           

       	
         	String picts = parent.getProps().getProperty("pict.dir");
     	    String pictspda = parent.getProps().getProperty("pictpda.dir");
     	 

       	try {
     	   Connection con = getNewCon();
    	
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

            Vector cats = new Vector();
            
            for(int i = 0; i < dd.length ; i++){
            	File t = dd[i];
            	if(t.isDirectory()){
            		cats.add(t.getName());
            	}
            }
            
            System.out.println("Piltide leidmine - katalooge sees " + cats.size() + " " + cats.toString());
       	    while (rs.next()) {
       	    	a++;
       	    	c++;
       	    	
       	     String toode = rs.getString("toode").trim();  
             toode = toode.replace('/', '_');
             toode = toode.replace('/', '_');
       	    	if(c == 100){
       	    		  c = 0;
       	    		  System.out.println("Piltide leidmine "  + a);	
       	    		  
       	    	}
       	     
                  
                     File f = new File(picts + toode+".jpg");
                     String len = "0";
                     if(f.exists()){
                       len =	f.length() + "";
                     } else if(cats.size() > 0 ){
                    	 for(int w = 0 ;w < cats.size();w++){
                    		f = new File(picts + cats.elementAt(w) + "/" + toode+".jpg");
                    		
                    		if(f.exists()){
                    			System.out.println("Kataloogis leitud " + picts + cats.elementAt(w) + "/" + toode+".jpg");	
                    		     len =	f.length() + "";	
                    		}                     
                    	 }
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
       	  System.out.println("Kataloogi kustutamine "  + a);	    
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

       //Kataloogi sisude kustutamine
                 	  
      
              	 
              	 
            	 File res2 = new File (pictspda);
                String[] ff2 = res2.list();
            	 for(int i = 0;ff2.length>i;i++){
                //	System.out.println("fff " + ff2[i]);
                	(new File ( pictspda + ff2[i])).delete(); 
             
                }
//            	 L�ppes kustutamine 
            	 
              System.out.println("Piltide kopeerimine ");	
              //Failide kopeerimine  
                String[] gg = new File(picts).list();
             	  //System.out.println("gg " + gg[0] + " " + gg.length);
                
                 for(int i = 0;gg.length>i;i++){          	
                 	File in = new File ( picts + gg[i]); 
                 	if(in.isFile()){            	
                 	File inpda = new File ( pictspda + toUpper(gg[i])); 
                       	copyFile(in,inpda);
                 	}
                 }
                 
                 //piltide kopeerimine alam kataloogist ..
                 
                 if(cats.size() >0){
                	 for(int h = 0;h < cats.size(); h++){
                		
                	   String[]  ff = new File(picts + cats.elementAt(h)+ "/").list();
                   	 
                      
                       for(int i = 0;i < ff.length  ;i++){          	
                       	File in = new File ( picts + cats.elementAt(h)+ "/" + ff[i]); 
                       	if(in.isFile()){   
                       		
                       	File inpda = new File ( pictspda + toUpper(ff[i])); 
                             	copyFile(in,inpda);
                       	 }
                       }
                       
                	 }
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
    	   
    	   asi = asi.substring(0,asi.length()-4).toUpperCase() + asi.substring(asi.lastIndexOf("."));
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


    
    
    public String getProductSortingData(){
       	return null;
       }
    
	public Properties loadProps(String fileName) {
		Properties props = new Properties();

		try {
			File file = new File(fileName);
			if (file.exists()) {
				FileInputStream in = new FileInputStream(file);
				props.load(in);
				in.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return props;
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




        public String getDealerCustomerCSV(String agent){
            return null;
            }

        
        public Vector getFixedAssortmentData() {
            Vector lines = new Vector();

            try {
           	  String cmd = "  select s.papild_nosauk2 as grupp,g.artikuls as toode " +
   " from atlaizhu_grupas s " +
   "      left join atlaizhu_grupas_saturs d on s.id=d.atlgrupas_id " +
   "      left join atlaides_saturs a on a.id=d.id " +
   "      left join preces g on a.kods=g.kods " +
   " where a.tips=3 and (s.papild_nosauk2='NUKO' or s.papild_nosauk2='BALTSTOR' or s.papild_nosauk2='MEGO'  or s.papild_nosauk2='OLAINFARM APTIEKA' " +
   " or s.papild_nosauk2='BETA' or s.papild_nosauk2='AIBE' or s.papild_nosauk2='ELVI' " +
" or s.papild_nosauk2='IEP GRUPA' or s.papild_nosauk2='VIRSI' or s.papild_nosauk2='DEPO' " +
" or s.papild_nosauk2='LATS'  or s.papild_nosauk2='SPIRITS' or s.papild_nosauk2='KESKO' or s.papild_nosauk2='RIMI'" +
" or s.papild_nosauk2='DROGAS'  or s.papild_nosauk2='NARVESEN'  or s.papild_nosauk2='KURSI' or s.papild_nosauk2='VIADA' or s.papild_nosauk2='MEDELENS' or s.papild_nosauk2='CIRCLE' " +
" or s.papild_nosauk2='MAXIMA' or s.papild_nosauk2='STOCKMANN'" +
"  or s.nosaukums='IEP GRUPA' or s.papild_nosauk2='PALMAS' or s.papild_nosauk2='EAST WEST TRANSIT' or s.papild_nosauk2='VIRSI'" + 
" or s.papild_nosauk2='CENUKLUBS' or s.papild_nosauk2='MANA APTIEKA' or s.papild_nosauk2='SENTOR FARM' or s.papild_nosauk2='ASTARTE'" +
" or s.papild_nosauk2='BENU APTIEKA' or s.papild_nosauk2='MANA APTIEKA' " +
" or s.papild_nosauk2='BBH INVESTMENTS' or s.papild_nosauk2='TN 2000' )" +
" order by g.PRIORITY,g.nosauk2 ";


  /* " where a.tips=3 and (s.papild_nosauk2='BALTSTOR' or s.papild_nosauk2='MEGO'  or s.papild_nosauk2='OLAINFARM APTIEKA' " +
   " or s.papild_nosauk2='ANTARIS' or s.papild_nosauk2='JELGAVFARM' or s.papild_nosauk2='SENTOR FARM'" +
   " or s.papild_nosauk2='BETA' or s.papild_nosauk2='AIBE' or s.papild_nosauk2='ELVI' " +
   " or s.papild_nosauk2='IEP GRUPA' or s.papild_nosauk2='VIRSI' or s.papild_nosauk2='JELGAVFARM' or s.papild_nosauk2='DEPO' " +
   " or s.papild_nosauk2='LATS'  or s.papild_nosauk2='PRISMA' or s.papild_nosauk2='KRAUTA' or s.papild_nosauk2='RIMI'" +
   " or s.papild_nosauk2='DROGAS'  or s.papild_nosauk2='NARVESEN' or s.papild_nosauk2='VIADA' or s.papild_nosauk2='STATOIL' or s.papild_nosauk2='CIRCLE' " +
   " or s.papild_nosauk2='MAXIMA' or s.papild_nosauk2='STOCKMANN'" +
   "  or s.nosaukums='IEP GRUPA' or s.papild_nosauk2='PALMAS' or s.papild_nosauk2='VIRSI'" + 
   " or s.papild_nosauk2='CENUKLUBS' or s.papild_nosauk2='MANA APTIEKA' or s.papild_nosauk2='DOMO'" +
   " or s.papild_nosauk2='BENU APTIEKA' or s.papild_nosauk2='MANA APTIEKA'" +
   " or s.papild_nosauk2='BBH INVESTMENTS' or s.papild_nosauk2='TN 2000' )" +
   " order by g.PRIORITY,g.nosauk2 ";*/
        
        
           	
           	System.out.println(cmd);
              	 Connection con = getNewCon();
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
                               mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
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
//                          getFixedAssortmentDataLog(lines);
            return lines;
    }
        
        
        public void getFixedAssortmentDataLog(Vector lines) {
         

            try {
           	  String cmd = "  select s.papild_nosauk2 as grupp,g.artikuls as toode " +
   " from atlaizhu_grupas s " +
   "      left join atlaizhu_grupas_saturs d on s.id=d.atlgrupas_id " +
   "      left join atlaides_saturs a on a.id=d.id " +
   "      left join preces g on a.kods=g.kods " +
   
   " where a.tips=3 and (s.papild_nosauk2='NUKO' or s.papild_nosauk2='BALTSTOR' or s.papild_nosauk2='MEGO'  or s.papild_nosauk2='OLAINFARM APTIEKA' " +
   " or s.papild_nosauk2='BETA' or s.papild_nosauk2='AIBE' or s.papild_nosauk2='ELVI' " +
" or s.papild_nosauk2='IEP GRUPA' or s.papild_nosauk2='VIRSI' or s.papild_nosauk2='DEPO' " +
" or s.papild_nosauk2='LATS'  or s.papild_nosauk2='SPIRITS' or s.papild_nosauk2='KESKO' or s.papild_nosauk2='RIMI'" +
" or s.papild_nosauk2='DROGAS'  or s.papild_nosauk2='NARVESEN'  or s.papild_nosauk2='KURSI' or s.papild_nosauk2='VIADA' or s.papild_nosauk2='MEDELENS' or s.papild_nosauk2='CIRCLE' " +
" or s.papild_nosauk2='MAXIMA' or s.papild_nosauk2='STOCKMANN'" +
"  or s.nosaukums='IEP GRUPA' or s.papild_nosauk2='PALMAS' or s.papild_nosauk2='VIRSI' or s.papild_nosauk2='EAST WEST TRANSIT' " + 
" or s.papild_nosauk2='CENUKLUBS' or s.papild_nosauk2='MANA APTIEKA' or s.papild_nosauk2='SENTOR FARM' or s.papild_nosauk2='ASTARTE' " +
" or s.papild_nosauk2='BENU APTIEKA' or s.papild_nosauk2='MANA APTIEKA'" +
" or s.papild_nosauk2='BBH INVESTMENTS' or s.papild_nosauk2='TN 2000' )" +
" order by g.nosauk2 ";


  /* " where a.tips=3 and (s.papild_nosauk2='BALTSTOR' or s.papild_nosauk2='MEGO' or s.papild_nosauk2='OLAINFARM APTIEKA' " +
   " or s.papild_nosauk2='BETA' " +
   " or s.papild_nosauk2='AIBE' or s.papild_nosauk2='ELVI' or s.papild_nosauk2='IEP GRUPA' " +
   " or s.papild_nosauk2='ANTARIS' or s.papild_nosauk2='DEPO' or s.papild_nosauk2='LATS' or s.papild_nosauk2='JELGAVFARM' " +
   " or s.papild_nosauk2='CENUKLUBS' or s.papild_nosauk2='RSKORPIONS'" +
   " or s.papild_nosauk2='BENU APTIEKA' or s.papild_nosauk2='MANA APTIEKA' or s.papild_nosauk2='DOMO'" +
 
   "or s.papild_nosauk2='PRISMA' or s.papild_nosauk2='KRAUTA' or s.papild_nosauk2='VIRSI' or s.papild_nosauk2='NK LUKOIL' or s.papild_nosauk2='STOCKMANN'" +
   " or s.papild_nosauk2='BBH INVESTMENTS'  or s.papild_nosauk2='ANTARES' or s.papild_nosauk2='PALMAS' or s.papild_nosauk2='VIRSI'  or s.papild_nosauk2='MAXIMA'  or s.papild_nosauk2='RIMI' " +
   " or s.papild_nosauk2='DROGAS'  or s.papild_nosauk2='NARVESEN' or s.papild_nosauk2='VIADA' or s.papild_nosauk2='STATOIL' or s.papild_nosauk2='CIRCLE' " +
   "  or s.nosaukums='IEP GRUPA'  or s.papild_nosauk2='TN 2000' or s.nosaukums='TN 2000'  " +
   ")" +
   " order by g.nosauk2 ";*/
           	  
           	  //ANTARIS/ANTARIS


           	System.out.println(cmd);
              	 Connection con = getNewLogCon();
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
                            	 if(locProps.getProperty(clGroup.trim()) != null){
                            		 clGroup = locProps.getProperty(clGroup.trim());
                            	 }
                               mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
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
          
    }
        
        
        
        public Vector getPriceMatrixData() {
            Vector lines = new Vector();
            String sql = "  SELECT ' ' AS custid, fn_rtrim(ale.nosaukums) || '' || ale.papild_nosauk2 AS clgroup,  " +
				 "   pr.artikuls   AS prodid, pr.artikuls  AS prodgroup,  " +
				"	 tt.atlaide AS discount,   tt.spec_cena AS price ,tips ,cast(tt.PALM_AKCIJAS_ATLAIDE as integer)  as ale,tt.NO_DATUMA1 as algus, " +
				" tt.NO_datuma2 as keskel ,tt.LIDZ_DATUMAM1 as lopp ,  PALM_AKCIJAS_ATLAIDE2 as lisaale ,"
				+ " SPEC_CENA2 as lisahind ,COMM as lisainff ,ACTION_MINIMUM_AMOUNT as minAm " +
				" FROM atlaizhu_grupas ale inner join atlaizhu_grupas_saturs abi on ale.id=abi.atlgrupas_id  " +
				 "    inner join atlaides_saturs tt on tt.id=abi.id  " +
				"	 inner join preces  pr on pr.kods= tt.kods  " +
				"     where   tt.spec_cena > 0 AND tt.no_datuma1<=current_timestamp and current_timestamp<=tt.lidz_datumam1 + 1  " +
				 "    order by  ale.nosaukums , ale.papild_nosauk2, pr.nosauk2";
           String sql2 = " SELECT pkonts AS custid, '' AS clgroup,  " +
				 "  pr.artikuls   AS prodid, " +
				 " pr.artikuls  AS prodgroup, " +
				 "	 tt.atlaide AS discount, " +
				 "     tt.spec_cena AS price ,tt.tips,cast(tt.PALM_AKCIJAS_ATLAIDE as integer)  as ale,"
				 + "tt.NO_datuma1 as algus,tt.NO_datuma2 as keskel ,tt.LIDZ_DATUMAM1 as lopp ,ACTION_MINIMUM_AMOUNT as minAm FROM " + 
				 "	atlaides_saturs tt " +
				"	 INNER JOIN klientu_atlaides aa on aa.id= tt.id " +
				"	 inner join preces  pr on pr.kods= tt.kods " +
				"	 INNEr JOIN adreses adr on adr.kods=aa.kl_kods " +
				 "    where   tt.spec_cena > 0 AND tt.no_datuma1<=current_timestamp and current_timestamp<=tt.lidz_datumam1 + 1 " +
				  "   order by pkonts,  pr.nosauk2";
            try {
             NumberFormat nf = NumberFormat.getInstance();
            	nf.setMaximumFractionDigits(2);
            	 NumberFormat nf2 = NumberFormat.getInstance();
             	nf2.setMaximumFractionDigits(0);


          	Statement stmt = this.getNewCon().createStatement();
          	Statement stmt2 = this.getNewCon().createStatement();
                    ResultSet rs = stmt.executeQuery(sql);

                    while (rs.next()) {

                      
                            String custID = rs.getString("custid");
                            String clGroup = rs.getString("clgroup");
                            String prodID = rs.getString("prodid");
                            String prodGroup = rs.getString("prodgroup");
                          //  double discount = rs.getDouble("discount");
                            double price = rs.getDouble("price");
                            String tips = rs.getString("tips").trim();
                            String ale = rs.getString("ale");
            				String sdate =  rs.getString("algus");
            				String kdate = rs.getString("keskel");
            				String tdate =  rs.getString("lopp");
                            String lisaale = rs.getString("lisaale");
                            String lisahind = rs.getString("lisahind");
                            String lisacomm = rs.getString("lisainff");
                            double minAm = rs.getDouble("minAm");
                            
                            
                            		

                            MatrixLine mline = new MatrixLine();
                            String add1= "";
                            if(minAm > 0 ){
                            	mline.setMinAm(minAm);
                            	add1 = "ma:"+(int)minAm + " ";
                            	 mline.setLineinfo( add1 );
                            }
                            
                            if(lisahind != null && lisahind.trim().length() >0 ){
                            lisahind = 	lisahind.trim();
                            try{
                            	 double dd = Double.parseDouble(lisahind);
                            	 mline.setLineinfo( add1 + nf.format(dd)  + " eur." );
                            }catch(Exception t){
                            	t.printStackTrace();
                            }
                            	
                            	
                            }
                           
                            if(lisaale != null && lisaale.trim().length() >0 && !lisaale.equals("0") ){
                            	try{
                            	//	System.out.println("asi " + lisaale + " " + ((int)((Double.parseDouble(lisaale)+0.499)))  );
                                	mline.setLineinfo(mline.getLineinfo() +  " " + ((int)((Double.parseDouble(lisaale)+ 0.499)))  + "%" );
                            	}catch(Exception r){
                            		r.printStackTrace();
                            	}
                            	
                            }
                            
                            if(lisacomm != null && lisacomm.trim().length() >0 ){
                            	mline.setLineinfo(mline.getLineinfo() +  " " + Converter.enCodeEst(lisacomm.trim()));
                            }
                           
                            
                            if (custID != null && custID.trim().length() > 0) {
                                    mline.setCustID(custID.trim());
                            }
                            if (mline.getCustID() == null && clGroup != null
                                            && clGroup.trim().length() > 0) {
                                    mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
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
                            String algus = "";
                            if(ale != null && ale.trim().length() != 0 ){
                            	algus =  ale + "% ";
                            }
                            
                            if(sdate != null && sdate.trim().length() != 0){
                            	algus = algus  + "[" +  sdate.substring(0,10) +  "] ";
                            }
                            
                            if(kdate != null && kdate.trim().length() != 0){
                            	algus = algus + kdate.substring(0,10);
                            }
                            
                            if(!algus.equals("")){
                            mline.setStartDate(algus);	
                            }
                        
            			
            				 if(tdate != null && tdate.trim().length() != 0){
                               mline.setEndDate(tdate.substring(0,10));
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
                          //  double discount = rs2.getDouble("discount");
                            double price = rs2.getDouble("price");
                            String tips = rs2.getString("tips").trim();
                            String ale = rs2.getString("ale");
            				String sdate =  rs2.getString("algus");
            				String kdate = rs2.getString("keskel");
            				String tdate =  rs2.getString("lopp");
            				 double minAm = rs2.getDouble("minAm");
                    		
                            MatrixLine mline = new MatrixLine();
                            String add1= "";
                            if(minAm > 0 ){
                            	mline.setMinAm(minAm);
                            	add1 = "ma:"+(int)minAm;
                            	 mline.setLineinfo(  add1 );
                            }
                            

                            if (custID != null && custID.trim().length() > 0) {
                                    mline.setCustID(custID.trim());
                            }
                            if (mline.getCustID() == null && clGroup != null
                                            && clGroup.trim().length() > 0) {
                                    mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
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
                            String algus = "";
                            if(ale != null && ale.trim().length() != 0){
                            	algus = ale + "% ";
                            }
                            
                            if(sdate != null && sdate.trim().length() != 0){
                            	algus = algus + " [" +sdate.substring(0,10) +  "] " ;
                            }
                            
                            if(kdate != null && kdate.trim().length() != 0){
                            	algus = algus + kdate.substring(0,10) + " " + add1;
                            }
                            if(!algus.equals("")){
                           
                            mline.setStartDate(algus);	
                            }
                        
            			
            				 if(tdate != null && tdate.trim().length() != 0){
                               mline.setEndDate(tdate.substring(0,10));
                             }
                            
                            lines.add(mline);
                    }

                    System.out.println("Teen kLientidele Matixit " + lines.size());

            } catch (SQLException ex) {
            ex.printStackTrace();
            }
//            getPriceMatrixDataLog(lines);
            return lines;
        	//return null;s
    }
        
        
        public TreeMap getCustomersLOG(){

        	
        	String kategory="";
        	 if(parent.getProps().getProperty("kategory") != null){
        		 kategory = "and kategory ='" + parent.getProps().getProperty("kategory") +"'";
        	 }
          String cmd = "SELECT a.nosauk AS nimi,a.pkonts AS kood,a.adrese || ' ' || a.pilseta AS aadress,a.atlaide AS discount,a.uni AS visit,a.pers_amats " +
          		" || ' ' || a.talrunis AS info, " +
                " agents AS agent,AGENTS1 , AGENTS2, TELEX as AGENTS3 ,'' AS payer , a.kons_dienas AS makse ,kategory AS grupp ,papild_nosauk || '' || papild_nosauk2 AS klale  FROM adreses a " +
    			"  WHERE  (a.tips!='A' " +
                " and a.nosauk not starting '#'  and a.nosauk not starting '*'  " + kategory +  ")  or a.pkonts = '0110000000' ORDER BY a.nosauk"; //uus klient ((or a.pkonts = '0100000'))

            log(cmd);
            
            TreeMap customers = new TreeMap();
            try {
              Connection con = getNewCon();

              Statement stmt = con.createStatement();
                ResultSet	      rs = stmt.executeQuery(cmd);
                int counter = 0;
                while (rs.next()) {
                  counter++;
         Customer cust = new Customer(rs.getString("kood").trim());

             String payer = rs.getString("payer");
             if (payer != null && payer.trim().length()>0){
                cust.setPayer(payer.trim());
             }

             String grupp = rs.getString("grupp");
             if (grupp != null && grupp.trim().length()>0){      
                cust.setCustType(grupp.trim());
             }

             String alegr = rs.getString("klale");
             if(alegr != null && alegr.trim().length()>0){
             	cust.setGroup(alegr.trim());
             }
                  customers.put(cust.getID(), cust);            
                }

                rs.close();
                stmt.close();
                con.close();
            } catch (Exception e) {
                getParent().showError(e);
            }

            return customers;
        }

        

        public void getPriceMatrixDataLog(Vector lines) {
         
            String sql = "  SELECT ' ' AS custid, ale.nosaukums || '' || ale.papild_nosauk2 AS clgroup,  " +
				 "   pr.artikuls   AS prodid, pr.artikuls  AS prodgroup,  " +
				"	 tt.atlaide AS discount,   tt.spec_cena AS price ,tips ,tt.NO_datuma1 as algus, " +
				" tt.NO_datuma2 as keskel ,tt.LIDZ_DATUMAM1 as lopp,cast(tt.PALM_AKCIJAS_ATLAIDE as integer)  as ahind FROM atlaizhu_grupas ale   " + //cast((1-tt.spec_cena/pr.cena5)*100 as int)
				"	 inner join atlaizhu_grupas_saturs abi on ale.id=abi.atlgrupas_id  " +
				 "    inner join atlaides_saturs tt on tt.id=abi.id  " +
				"	 inner join preces  pr on pr.kods= tt.kods  " +
				"     where   tt.spec_cena > 0 AND tt.no_datuma1<=current_timestamp and current_timestamp<=tt.lidz_datumam1  " +
				 "    order by  ale.nosaukums , ale.papild_nosauk2, pr.nosauk2";
            
           String sql2 = " SELECT pkonts AS custid, '' AS clgroup,  " +
				 "  pr.artikuls   AS prodid, " +
				 " pr.artikuls  AS prodgroup, " +
				 "	 tt.atlaide AS discount, " +
				 "     tt.spec_cena AS price ,tt.tips,tt.NO_datuma1 as algus,tt.NO_datuma2 as keskel " +
				 " ,tt.LIDZ_DATUMAM1 as lopp ,cast(tt.PALM_AKCIJAS_ATLAIDE as integer)  as ahind  FROM " + //cast((1-tt.spec_cena/pr.cena5)*100 as int)
				 "	atlaides_saturs tt " +
				 "	 INNER JOIN klientu_atlaides aa on aa.id= tt.id " +
				 "	 inner join preces  pr on pr.kods= tt.kods " +
				 "	 INNEr JOIN adreses adr on adr.kods=aa.kl_kods " +
				 "    where   tt.spec_cena > 0 AND tt.no_datuma1<=current_timestamp and current_timestamp<=tt.lidz_datumam1 " +
				 "   order by pkonts,  pr.nosauk2 ";
            try {


          	Statement stmt = this.getNewLogCon().createStatement();
          	Statement stmt2 = this.getNewLogCon().createStatement();
                    ResultSet rs = stmt.executeQuery(sql);

                    while (rs.next()) {

                      
                            String custID = rs.getString("custid");
                            String clGroup = rs.getString("clgroup");
                            String prodID = rs.getString("prodid");
                            String prodGroup = rs.getString("prodgroup");
                          //  double discount = rs.getDouble("discount");
                            double price = rs.getDouble("price");
                            String tips = rs.getString("tips").trim();
                            
            				String sdate =  rs.getString("algus");
            				String kdate = rs.getString("keskel");
            				String tdate =  rs.getString("lopp");
                            double ahind = rs.getDouble("ahind");

                            MatrixLine mline = new MatrixLine();

                            if (custID != null && custID.trim().length() > 0) {
                                    mline.setCustID(custID.trim());
                            }
                            if (mline.getCustID() == null && clGroup != null
                                            && clGroup.trim().length() > 0) {
                            	
                            	if(locProps.getProperty(clGroup.trim()) != null){
                            		clGroup =locProps.getProperty(clGroup.trim());
                            	}
                                    mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
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
                            
                            String algus = (int)ahind + "% ";
                            
                            
                            if(sdate != null && sdate.trim().length() != 0){
                            	algus = algus  + "[" +  sdate.substring(0,10) +  "] ";
                            }
                            
                            if(kdate != null && kdate.trim().length() != 0){
                            	algus = algus + kdate.substring(0,10);
                            }
                            
                            if(!algus.equals("")){
                            mline.setStartDate(algus);	
                            }
                        
            			
            				 if(tdate != null && tdate.trim().length() != 0){
                               mline.setEndDate(tdate.substring(0,10));
                             }
                            lines.add(mline);
                    }
                    
                    stmt.close();
                    System.out.println("Teen Gruppidele Matixit " +lines.size());
                    
                    
                    ResultSet rs2 = stmt2.executeQuery(sql2);

                    while (rs2.next()) {
                            String custID = rs2.getString("custid");
                            if(logMap.get(custID) != null){
                            	custID =(String) logMap.get(custID);
                            }
                            String clGroup = rs2.getString("clgroup");
                            String prodID = rs2.getString("prodid");
                            String prodGroup = rs2.getString("prodgroup");
                          //  double discount = rs2.getDouble("discount");
                            double price = rs2.getDouble("price");
                            String tips = rs2.getString("tips").trim();
                         
            				String sdate =  rs2.getString("algus");
            				String kdate = rs2.getString("keskel");
            				String tdate =  rs2.getString("lopp");
                            double ahind = rs2.getDouble("ahind");
                            
                            MatrixLine mline = new MatrixLine();

                            if (custID != null && custID.trim().length() > 0) {
                                    mline.setCustID(custID.trim());
                            }
                            if (mline.getCustID() == null && clGroup != null
                                            && clGroup.trim().length() > 0) {
                                    mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
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
                            String algus = (int)ahind + "% ";
                            
                            
                            if(sdate != null && sdate.trim().length() != 0){
                            	algus = algus + " [" +sdate.substring(0,10) +  "] ";
                            }
                            
                            if(kdate != null && kdate.trim().length() != 0){
                            	algus = algus + kdate.substring(0,10);
                            }
                            if(!algus.equals("")){
                           
                            mline.setStartDate(algus);	
                            }
                        
            			
            				 if(tdate != null && tdate.trim().length() != 0){
                               mline.setEndDate(tdate.substring(0,10));
                             }
                            
                            lines.add(mline);
                    }

                    System.out.println("Teen kLientidele Logsol Matixit " + lines.size());

            } catch (SQLException ex) {
            ex.printStackTrace();
            }
            
        	//return null;s
    }
                                                    public String  getCustProdDiscounts(Agent agent){
                                                     return null;
                                                     }

                                                	public Vector getLastThreeData() {
                                                		// TODO Auto-generated method stub
                                                		return null;
                                                	}
    /**
     *
     */
    public void connectionUp(){
    }

    private Connection getNewCon() throws SQLException{
      return DriverManager.getConnection(url, "SYSDBA", "master");
    }
    
    private Connection getNewLogCon() throws SQLException{
        return DriverManager.getConnection(urllog, "SYSDBA", "master");
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


 /*   public TreeMap getProdGroupsSecond() throws SQLException {
    	 System.out.println("tootegrupidSEC alustan ");
      Connection con = getNewCon();
    TreeMap   tm = new TreeMap();
    String    sql = "SELECT palm_descr AS nimetus2 from grupas ORDER by nosauk  " ;


    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
    	String nimetus2 =  rs.getString("nimetus2");
    	if(nimetus2 != null && nimetus2.indexOf(" ") != -1 ){
    		nimetus2 = nimetus2.trim();
    		String aaaa = nimetus2.substring(0,nimetus2.indexOf(" "));
    		System.out.println(aaaa + "-" + nimetus2 );
    		if(aaaa.indexOf(".") != -1){	
        		aaaa = aaaa.substring(0,aaaa.indexOf(".")) +  aaaa.substring(aaaa.indexOf(".")+1);
           		 tm.put(aaaa,  nimetus2.trim());
           	}		
    		}
    	
    	}
    	
       
    
    rs.close();
    st.close();
    con.close();
    
    System.out.println("tootegrupidSEC ending ");
    getLogProdGroupsSecond(tm);
    return tm;
    }
*/
    
    public TreeMap getProdGroups() throws SQLException {
      Connection con = getNewCon();
      System.out.println("tootegrupid alustan ");
      //setTreeGroup(true);
    TreeMap   tm = new TreeMap();
    String    sql = " SELECT palm_descr AS nimetus2 ,OTC as asi from grupas where palm_descr is not null ORDER by palm_descr  " ;


    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
    	
    	String nimetus2 =  rs.getString("nimetus2");
    	if(nimetus2 != null && nimetus2.indexOf(" ") != -1 ){
    		nimetus2 = nimetus2.trim();
    		String aaaa = nimetus2.substring(0,nimetus2.indexOf(" "));
    		 tm.put(aaaa,  Converter.enCodeEst(nimetus2.trim()));
    		if(!aaaa.startsWith("PR")){
    			String oct =  rs.getString("asi");
    			String lisa = "";
    			if(oct!=null && oct.trim().length() >0 && oct.trim().equals("OTC")){
    				aaaa =  "OTC|" + aaaa;
    				lisa = " OTC";
    			}
    			if(oct!=null && oct.trim().length() >0 && oct.trim().equals("ALC")){
    				aaaa =  "ALC|" + aaaa;
    				lisa = " ALC";
    			}
       		 tm.put(aaaa,  Converter.enCodeEst(nimetus2.trim() + lisa));
       	    }
    	}      
    }
    
    rs.close();
    st.close();
    con.close();
    System.out.println("tootegrupid ending " );
   try{
	  // getLogProdGroups(tm);
   }catch(Exception rr){   
   }
    return tm;
    }


        public String getDistribution() {
          return null;
        }



    public TreeMap getAgents() {
      String  cmd = "select num AS kood , nosauk || ' ' ||num as nimi ,nosauk as name from kontr  " +
      	"	where nosauk != ''  ORDER BY num";
        TreeMap agents = new TreeMap();
        
        try {
        	try{
        		getProdGroups();	
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        
          Connection con = getNewCon();
            Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);
            System.out.println("Agents Starting ");
            while (rs.next()) {
              Agent agent = new Agent(rs.getString("kood").trim());

              String nimi = rs.getString("nimi").trim();

              String nimi2 = rs.getString("name").trim();
              agent.setName(nimi);
              agent.setIsPilot(true);
              agent.setLanguage(nimi2);
              agents.put(nimi, agent);
              ags.add(agent.getID());
            }
            rs.close();
            stmt.close();
            con.close();

        } catch (Exception e) {
            getParent().showError(e);
        }
        Agent hh = new Agent("HOUSE");
        hh.setIsPilot(true);
        hh.setName("Office");
        Agent hh1 = new Agent("HOUSE1");
        hh1.setIsPilot(true);
        hh1.setName("Office");
        Agent hh2 = new Agent("HOUSE3");
        hh2.setIsPilot(true);
        hh2.setName("Office");
        agents.put("HOUSE", hh);
        agents.put("HOUSE1", hh1);
        agents.put("HOUSE3", hh2);
        System.out.println("Agents Ready ");
        return agents;
    }



    private String getDir(){
      String dir = getParent().getProps().getProperty("readin");
      if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
      return dir;
    }



    public TreeMap getCustomers(Agent agent){

      String cmd = "SELECT  a.nosauk AS nimi,a.pkonts AS kood,fn_rtrim(a.adrese) || ' ' || fn_rtrim(a.pilseta) AS aadress,a.atlaide AS discount,a.uni AS visit,a.pers_amats " +
      		" || ' ' || a.talrunis AS info, " +
            " agents AS agent,AGENTS1 , AGENTS2, TELEX as AGENTS3, APMEKLEJUMA_NOSACIJUMI as aginfo,'' AS payer , a.kons_dienas AS makse ,papild_nosauk AS grupp ,papild_nosauk || '' ||" +
            " papild_nosauk2 AS klale , palms as list2 , MIN_PASUT_SUMMA as minsum ,JGT_VS_LOGSOL as logsol, plu as eucerin , LICENCES_STAT as lic , FWPIEGDIENAS as vedu " +
            " , fn_rtrim(SHIFRS2) ||' '||fn_rtrim(a.KLTIPS) as klgrupp, "
            + "adrese as rstreet ,pilseta as rcity , kltips as rkategooria , kategory as rchain FROM adreses a  WHERE  (a.tips!='A' " +
            " and a.nosauk not starting '#'  and a.nosauk not starting '*' and agents is not null ) or a.pkonts = '0100000' ORDER BY a.nosauk";

      String cmd2 = "SELECT a.pkonts AS kood FROM adreses a  WHERE  (a.tips!='A' " + 
           "  and a.nosauk not starting '#'  and a.nosauk not starting '*' and agents is not null and palms!='') ORDER BY palms ";
      StringBuffer buf = new StringBuffer();
      
  	  Converter conv = new Converter();
	//  HashMap kyssad =   conv.getQuestionaire(this.getExcelDir());
      
      
     
        log(cmd);
        
        double minordsum = 0;
        if(getParent().getProps().getProperty("minordsum") != null){
        	try{
        		minordsum = new Double(getParent().getProps().getProperty("minordsum")).doubleValue();
        	}catch (Exception e){
        		e.printStackTrace();
        	}
        }
        System.out.println("kliendid alustan , minordsum " + minordsum);
        
        boolean  lookForOneTime = false;
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        HashMap custMap2 = new HashMap();
        Vector visordone = new Vector();
        
        if("true".equals(getParent().getProps().getProperty("lookForOneTime"))){
        	lookForOneTime = true;
        }
      
        
        
        
        
        Vector sup2 = new Vector();
        try {
          Connection con = getNewCon();

          Statement stmt = con.createStatement();
          Statement smt = ddd.createStatement();
          Statement smtl = bbb.createStatement();
          
          ResultSet	      rs5 = stmt.executeQuery(cmd2);
         
          while (rs5.next()) {
        	  String ooo = rs5.getString("kood").trim();
        		 // sup2.add(ooo);
        	  buf.append(ooo + System.getProperty("line.separator"));
          }
          File f = new File(getDir() + "asksup2.txt");
          if (f.exists())f.delete();
          if(buf.length() > 2){
              
                try {
          		PrintWriter os = new PrintWriter(new FileWriter(
          				getDir() + "asksup2.txt", true));
          		os.write(buf.toString());
          		os.close();
          	  }catch (IOException ex) {
          		ex.printStackTrace();
          	  }
           }
          
          
      	FileReader reader;
		try {
			reader = new FileReader(getDir() + "asksup2.txt" );
	
		BufferedReader br = new BufferedReader(reader);
		String line;
		while ( (line = br.readLine()) != null) {
			sup2.add(line);	
		}	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
          
          
          
          
          
          
     /*     String sql11 = "select custid from supervisor where " +
     
          		" extract(year from sentdate)=extract(year from current_timestamp) and prodid like 'rb%' group by custid";
          System.out.println(sql11);
          Connection ddd = DriverManager.getConnection(url2, "SYSDBA", "master");
          Statement str = ddd.createStatement();
          ResultSet rst1 = str.executeQuery(sql11);
          while(rst1.next()){
          	String custom = rst1.getString("custid").trim();
          	//visordone.add(custom);     	
          }
*/
          
          
          
          
          
          
            ResultSet	      rs = stmt.executeQuery(cmd);
            int counter = 0;
            while (rs.next()) {
              counter++;

              Customer cust = new Customer(rs.getString("kood").trim());
                
              String nimi = rs.getString("nimi").trim();
              cust.restName = nimi;

              if(!nimi.endsWith("#")){
                   // System.out.println("nimi1 " + nimi);
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
                 	nimi = nimi.replaceAll("BALTIC RESTAURANTS LATVIA","BalResLat");

                	nimi = nimi.replaceAll("DOUBLE COFFEE","DouCof");
                	nimi = nimi.replaceAll("AIZKRAUKLES SABIEDR�BA","AIZKRAUKLES SAB.");

            
                String aadress = rs.getString("aadress");
                if(aadress != null && aadress.trim().length()>0){
                    cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress.trim()),"Latvia"));
                    nimi = nimi + " " +aadress.trim()+ " " + cust.getID();
                }
               // System.out.println("nimi2 " + nimi);
                cust.setName(Converter.enCodeEst(nimi));

                String ale = rs.getString("discount");
                if (ale != null && ale.trim().length()>0){
                  cust.setDiscount(Double.parseDouble(ale.trim()));
                }

                String visit=rs.getString("visit");
                if (visit!=null && visit.trim().length()>0){
                  // cust.setVisit(visit.trim().toUpperCase());
                }
                
                if(minordsum != 0){
                	cust.setMinsum(minordsum);
                }

                String info = rs.getString("info");
                if(info != null && info.trim().length()>0){
                	cust.setPersonalData(Converter.enCodeEst(info.trim()));
                }

                String agentc = rs.getString("agent");
                String agente = rs.getString("agents1");
                String agentr = rs.getString("agents2");
                String agentt = rs.getString("agents3");
                
            
                String aginfo = rs.getString("aginfo");
                if(aginfo != null && aginfo.trim().length()>0){
                	
                	StringTokenizer st = new StringTokenizer(aginfo.trim(),";");
                	while(st.hasMoreTokens()){
                		String deal = st.nextToken();
                		StringTokenizer de = new StringTokenizer(deal.trim(),"|");
                		while (de.hasMoreTokens()){
                			/*
                			 * 1|0|01146;3|0|01141;

1| - it means days "1, 2, 3, 4, 5" (E, T, K, N, R)
0| - it means paired, nonpaired "2,1" (1/2)     OR     4, 8, 16, 32
01146;- its sales person code
*/ 
                			try{
                				String paev = de.nextToken();
                    			String nadal = de.nextToken();
                    			String age = de.nextToken();
                    			paev = paev.replaceAll("1", "E").replaceAll("2", "T").replaceAll("3", "K").replaceAll("4", "N").replaceAll("5", "R").replaceAll("6", "L").replaceAll("7", "P");
                    			if(nadal.equals("2")){
                    				nadal = "1";
                    			}else if (nadal.equals("1")){
                    				nadal = "2";
                    			}else if (nadal.equals("0")){
                    				nadal = "";
                    			}
                    			paev = paev + nadal;
                    			
                    			Agent aaa = new Agent(age);
                    			if(!paev.equals("0")) {
                    				aaa.setVisit(paev);
                    			}
                    			
                    			if(!age.contains("|") )
                    			cust.addAgent(aaa);
                    			if(age.contains("01127") )
                    		System.out.println(cust.getID() + " " + paev + " " + age  + " " + ags.contains(aaa));		
                			}catch (Exception ee){
                				//ee.printStackTrace();
                			//	System.out.println("Probleemne klient " + cust.getID() );		
                			}
                
                			
                		}
                	}
                	
                
                    if(aginfo.trim().indexOf("SH") == -1){
                        cust.addAgent(new Agent("HOUSE"));  
                        cust.addAgent(new Agent("HOUSE1")); 
                        cust.addAgent(new Agent("HOUSE3"));
                    } 
                }
                
                if(agente != null && agente.trim().length()>0 && !cust.hasAgent(agente.trim())){

                	if(ags.contains(agente.trim()))
                    cust.addAgent(new Agent(agente.trim()));
                    if(agente.trim().indexOf("SH") == -1){
                        cust.addAgent(new Agent("HOUSE"));  
                        cust.addAgent(new Agent("HOUSE1"));
                        cust.addAgent(new Agent("HOUSE3"));
                    }
                
                }
                if(agentc != null && agentc.trim().length()>0 && !cust.hasAgent(agentc.trim())){
                	if(ags.contains(agentc.trim()))
                    cust.addAgent(new Agent(agentc.trim()));
                    if(agentc.trim().indexOf("SH") == -1){
                        cust.addAgent(new Agent("HOUSE"));  
                        cust.addAgent(new Agent("HOUSE1")); 
                        cust.addAgent(new Agent("HOUSE3"));
                    } 
                }
                
                if(agentr != null && agentr.trim().length()>0 && !cust.hasAgent(agentr.trim())){
                	if(ags.contains(agentr.trim()))
                    cust.addAgent(new Agent(agentr.trim()));
                    if(agentr.trim().indexOf("SH") == -1){
                        cust.addAgent(new Agent("HOUSE"));  
                        cust.addAgent(new Agent("HOUSE1"));
                        cust.addAgent(new Agent("HOUSE3"));
                    }  
                 }            
              
                if(agentt != null && agentt.trim().length()>0 && !cust.hasAgent(agentt.trim())){
                	if(ags.contains(agentt.trim()))
                    cust.addAgent(new Agent(agentt.trim()));
                    if(agentt.trim().indexOf("SH") == -1){
                        cust.addAgent(new Agent("HOUSE"));  
                        cust.addAgent(new Agent("HOUSE1"));
                        cust.addAgent(new Agent("HOUSE3"));
                    } 
                }
                                
                if (cust.getID().equals("0100000")) {
                	agentc = "aia";
                	logMap.put("0110000000", "0100000");

                    Enumeration enums = getParent().getAgentsHash().elements();
                      while (enums.hasMoreElements()) {
                        Agent a = (Agent) enums.nextElement();
                        if(ags.contains(a.getID().trim()))
                        cust.addAgent(a);
                        
                        }
                      cust.addAgent(new Agent("HOUSE"));  
                      cust.addAgent(new Agent("HOUSE1"));
                      cust.addAgent(new Agent("HOUSE3"));
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
         	if(grupp.trim().equals("01")){
         		grupp = "A";
         	}
        	if(grupp.trim().equals("02")){
         		grupp = "B";
         	}
        	if(grupp.trim().equals("03") || grupp.trim().equals("04") ||
        			grupp.trim().equals("05")  || grupp.trim().equals("08")
					 || grupp.trim().equals("09")
					 || grupp.trim().equals("14")){
         		grupp = "C";
         	}
        	if(grupp.trim().equals("06")){
         		grupp = "D";
         	}
            cust.setCustType(Converter.cleanLitLatEst(grupp.trim()));
            Vector vv = new Vector();
            vv.add(grupp.trim());
            cust.setFixSortCode(vv);
         }

         String alegr = rs.getString("klale");
         if(alegr != null && alegr.trim().length()>0){
         	cust.setGroup(Converter.cleanLitLatEst(alegr.trim()));

         }
     	 // cust.setSupervisorFilled(true); //vaja  nagu oleks k�igi juures tehtud 

         
         double minsum = rs.getDouble("minsum");
          if(minsum > 0){
        	  cust.setMinsum(minsum);
          }
          if(minordsum == -1){
        	  cust.setMinsum(-1);
          }
          
          String logsol = rs.getString("logsol");
          if(logsol != null){
        	  logMap.put(logsol, cust.getID());
          }
          String teade = rs.getString("eucerin");
          if(teade != null && teade.trim().length() != 0){
        	  if(teade.equals("1")){
        		  cust.setInfo("Shell netirgot !||");
        		  cust.setCity("Shell netirgot !");
        	  }
          }
          
          String lic  = rs.getString("lic");
          if(lic != null && lic.trim().length() != 0){
        	  if(lic.equals("1")){
        		  cust.setLicence("OTC");
        		  //kllendile v�ib m��a
        		  
        		  // 2 ei v�i 
        		  // tyhi siis ka v�ib 
        	  }
        	  
        	  if(lic.equals("3")){
        		  cust.setLicence("ALC");
        	  }
        	  
          }
      
        	if(cust.getID().equals("0100000")){
        		  cust.setLicence("OTC");
        	}
        	
        	 String vedu  = rs.getString("vedu");
             if(vedu != null && vedu.trim().length() != 0){
            	 if(cust.getCity() != null && cust.getCity().trim().length() > 0){
            		 cust.setCity( cust.getCity() + "|Pieg�des diena - " +  vedu.replaceAll(";", "-")); 	 
            	 }else{
            		 cust.setCity("Pieg�des diena - " +  vedu.replaceAll(";", "-")); 	 
            	 }

                 cust.setCity(Converter.enCodeEst(cust.getCity()));
             }
          
             String klgrupp  = rs.getString("klgrupp");
             if(klgrupp != null && klgrupp.trim().length() != 0){
            	 
            	 cust.setBlockingDealer(klgrupp.trim());
            	
             }

             String rstreet  = rs.getString("rstreet");
             if(rstreet != null && rstreet.trim().length() != 0){
            	 cust.restStreet = rstreet.trim();
             }
             
             String rcity  = rs.getString("rcity");
             if(rcity != null && rcity.trim().length() != 0){
            	 cust.restCity = rcity.trim();
            	 cust.setCountry("Latvia");
             }
             
             String rkategooria  = rs.getString("rkategooria");
             if(rkategooria != null && rkategooria.trim().length() != 0){
            	 cust.category = rkategooria.trim() ;
             }
           
             String rchain  = rs.getString("rchain");
             if(rchain != null && rchain.trim().length() != 0){
            	 cust.setChain(rchain.trim());
             }
            
             
          
          if(cust.getID().toLowerCase().indexOf("pr")==-1 && agentc != null && agentc.toLowerCase().indexOf("pr")==-1){
              customers.put(cust.getName(), cust);
              custMap.put(cust.getPayer(), cust);
              custMap2.put(cust.getID(), cust);
        	  
               }
              }
            }

            rs.close();
            stmt.close();
            con.close(); 
            
            String sql3 = "Select RPACC AS klient,result_code AS staatus,rsaldo AS summa from m_sld_saldolist2 (cast('today' as date)) ";
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
        
        if(rcompany != null){
         	Converter.sendCustomerJson(customers, rcompany,true,user,pass,true);
         }
        
        
        System.out.println("kliendid l�petan ");
        return customers;
    }


    
    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      System.out.println("debt alustan ");
      String sql = "select pacc AS klient , fdoc as number,dat1 as kuupaev, sv as summa " +
            "  ,current_timestamp-dat1 AS yle  from dokument_v d  where sv is not null and acc1 starting '2310' and " +
       " current_timestamp  > dat1 and  dat1 > current_timestamp -180 order by pacc, datums, fdoc";

      Statement stmt = ddd.createStatement();

      ResultSet rs = stmt.executeQuery(sql);

      while(rs.next()){
        Debt debt = new Debt();
        String klient = rs.getString("klient");
        if(klient != null){
        	   debt.setCustID(klient.trim());
               debt.setDoknr(rs.getString("number").trim());

               debt.setDuedate(rs.getDate("kuupaev"));

               debt.setSum(Double.parseDouble(rs.getString("summa").trim()));
               debt.setDue(rs.getString("yle"));
               if(debt.getSum() > 0.05 ||  -0.05 > debt.getSum()  )
               list.addElement(debt); 	
        }
     
      }
       rs.close();
       stmt.close();
       System.out.println("debt l�petan  ");
       try{
      // getDebtLog(list);
    }catch(Exception rr){
 	   
    }
       return list;
    }

  
   
    
    
    public TreeMap getProducts(Agent agent){
    	

      String cmd = "SELECT pr.artikuls AS toode,pr.nosauk as nimi1,pr.nosauk2 AS nimi,pr.cena4 AS myygihind,( select first 1 tax_value from tax_values where tax_id = cast (g.nodoklis as int)  and  " +
   " current_timestamp >= tax_date_valid order by tax_date_valid desc) AS VAT,pr.mervien AS unit,pr.svars2 AS  " +
    "  		 tykke ,DALIJUMS AS minimum,OPTIMAL_AMOUNT as optimum , pr.mf_grupa AS uusgrupp,g.otc,g.grupa AS grupp,g.palm_descr AS grupp2  " +
     "        , pr.ecrkods AS ean ,PRIORITY AS sorting ,pr.palm_max_discount as maxdiscpr  ,g.palm_max_discount as maxdiscgr , pr.shrink as lisa FROM preces  " +
	"		  pr INNER JOIN grupas g ON g.grupa=pr.grupa   " +
     "        where (pr.act='+' and pr.nosauk2 >'') OR  (g.grupa='50' and pr.nosauk2 >'') or g.grupa='041' " +
		"	 order by pr.nosauk2";
      
      System.out.println(" tooted alustan ");

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
                String nimi1 =  rs.getString("nimi1");
                String nimetus =   rs.getString("nimi");
                if(nimetus == null || nimetus .trim().length() == 0 ){
                	nimetus = nimi1.trim().replaceAll("\n", " ") ;
                }else{
                	nimetus  = nimetus.trim().replaceAll("\n", " ") ;
                }
                Product prod = new Product(kood);
                prod.setType("JG");
                prod.setPrice(rs.getDouble("myygihind"));
                prod.setVat(rs.getDouble("VAT") + "");
                prod.setPrice(prod.getPrice());
                String unit = rs.getString("unit");
                if(unit != null && unit.trim().length()>0){
                	prod.setUnit(unit.trim());

                }

                prod.setInCases(rs.getInt("tykke"));
                
                double minim = rs.getDouble("minimum");
                if (minim > 1){
                    prod.setMinAmountInUnits(minim);
                }
                
                double optim = rs.getDouble("optimum");
                if (optim > 1){
                    prod.setOptAmountInUnits(optim);
                }
                prod.setStock(0);
               
                String gruppsec = rs.getString("uusgrupp");
                if(gruppsec != null && gruppsec.trim().length()>0){                	
               	 prod.setGroup(gruppsec.trim());
               }
               
                String otc = rs.getString("otc");
                
                
                String grupp = rs.getString("grupp");
                if(grupp != null && grupp.trim().length()>0){
                	 if(grupp.trim().equals("050")){
                	 	nimetus = "x " + nimetus;
                	 }
                    if(grupp.trim().length()< 4){
                    	grupp = grupp.trim() + "0";
                    }
                    prod.setSorting(grupp);
                }
                
                String grupp2 = rs.getString("grupp2");
                if(grupp2 != null && grupp2.trim().length()>0){
                	grupp2 = grupp2.trim();
					   if(grupp2.indexOf(" ") != -1){
						String gr1 = grupp2.substring(0,grupp2.indexOf(" "));
						
						if(1<gr1.trim().length()){
							if(otc!= null && otc.trim().length() > 0 && otc.trim().equals("OTC")){
								prod.setGroup("OTC|" + gr1.substring(0,2)) ; 	
		                	}else if(otc!= null && otc.trim().length() > 0 && otc.trim().equals("ALC")){
								prod.setGroup("ALC|" + gr1.substring(0,2)) ; 	
		                	}else{
		                		prod.setGroup(gr1.substring(0,2));
										
		                	}
						}
						
						if(2<gr1.trim().length()){
						//	String aaaa = gr1;
						//	prod.setGroupSecond(aaaa.substring(0,aaaa.indexOf(".")) +  aaaa.substring(aaaa.indexOf(".")+1));
						}
						if(otc!= null && otc.trim().length() > 0 && otc.trim().equals("OTC")){
							prod.setGroup("OTC|" + gr1);	
						}else if(otc!= null && otc.trim().length() > 0 && otc.trim().equals("ALC")){
							prod.setGroup("ALC|" + gr1);	
						}else{
							prod.setGroup(gr1);	
						}
						
					  }
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
               
                prod.setName(Converter.enCodeEst(nimetus));
                
                String ean = rs.getString("ean");
                if(ean != null && ean.trim().length()>0){
                	ean = ean.trim();
                	prod.setEAN(ean);
                	int len = ean.length();
                	if(len > 3){
                      	 prod.setName(Converter.enCodeEst(nimetus) + " " + ean.substring(len-4));
                           		
                	}
                }
                
                
                
                String sorting  = rs.getString("sorting");
                if(sorting != null && sorting.trim().length()>0){
                	prod.setNameSorting(sorting.trim()+ nimetus);
                }
                double maxdiscpr = rs.getDouble("maxdiscpr");
                double maxdiscgr = rs.getDouble("maxdiscgr");
                
                if(maxdiscgr != 0){
                	prod.setMaxDiscount(maxdiscgr);
                }
                if(maxdiscpr != 0){
                	prod.setMaxDiscount(maxdiscpr);
                }
           
                String lisa  = rs.getString("lisa");
                if(lisa != null && lisa.trim().length() != 0){
                	prod.setName(Converter.enCodeEst(prod.getName()) + " " + lisa.trim());              	
                }
                
            	if(!kood.startsWith("PR")){
            		 products.put(nimetus, prod);
                     prodHash.put(prod.getID(), prod);
            	}
               
            }
            rs.close();


        	  String  sql2 = " SELECT artikuls AS toode, atlrez AS stock from pdastocks " ;

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
try{
	// Logsol(products);	
}catch(Exception rr){
	   
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
    
    public void getCustomerProductsListLog(Vector custProds) {
        // return new Vector();
         String sql1 = " select a.pkonts AS klient , pr.artikuls AS toode, daudzums AS kogus, datums AS kuupaev  " +
   		  " from dokumenti dok inner join dok_spec rid on rid.dok_kods=dok.kods  " +
   		  " INNER JOIN adreses a ON a.kods=dok.san_kods " +
   		  " INNER JOIN preces pr ON pr.kods=rid.preces_kods where " +
   		  " datums > current_timestamp-120 AND funk=2 and daudzums > 0 " +
   		  " GROUP BY pr.artikuls,a.pkonts,daudzums,datums " +
   		  " ORDER BY a.pkonts,pr.artikuls,datums,daudzums " ;



           if (isDebug()) {
               System.out.println(sql1);

           }

           try {
             Connection con = getNewLogCon();
             PreparedStatement stmt = con.prepareStatement(sql1);

             //stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
             ResultSet	      rs = stmt.executeQuery();

             HashMap kliendid = new HashMap();
             while (rs.next()) {
               String kliendikood = rs.getString("klient").trim();
               if(this.logMap.get(kliendikood) != null){
            	   kliendikood = (String)logMap.get(kliendikood);
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
                           }

             rs.close();
             stmt.close();
             con.close();
           } catch (Exception e) {
             getParent().showError(e);
           }
          
       }
    
    public Vector getCustomerProductsList(String agentCode) {
     // return new Vector();
      String sql1 = " select a.pkonts AS klient , pr.artikuls AS toode, daudzums AS kogus, datums AS kuupaev  " +
		  " from dokumenti dok inner join dok_spec rid on rid.dok_kods=dok.kods  " +
		  " INNER JOIN adreses a ON a.kods=dok.san_kods " +
		  " INNER JOIN preces pr ON pr.kods=rid.preces_kods where " +
		  " datums > current_timestamp-120 AND funk=2 and daudzums > 0 " +
		  " GROUP BY pr.artikuls,a.pkonts,daudzums,datums " +
		  " ORDER BY a.pkonts,pr.artikuls,datums,daudzums " ;



        if (isDebug()) {
            System.out.println(sql1);

        }

        Vector custProds = new Vector();
        Vector custProds2 = new Vector();
      //  return custProds;

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
            CustProd cp2 = new CustProd(cust.getID()+"_1", prod.getID(), kogus, 0, 0,
                    rs.getString("kuupaev").substring(5,10), 0, 0, 0, " ");
              kliendid.put(kliendikood, cust);
              custProds.add(cp);
              custProds2.add(cp2);
          }

          rs.close();
          stmt.close();
          con.close();
        } catch (Exception e) {
          getParent().showError(e);
        }
        connectionDown();
        try{
        	//  getCustomerProductsListLog(custProds);
                	
        }catch(Exception ss){
        	
        }
      
  	  
		  try {
				Converter.saveObject(custProds, "custprod.vec");
			} catch (IOException e) {
			
				e.printStackTrace();
			}
        
        return custProds;
    }

    public String getPilotDayData(Agent agent){


      String sql2 =  "SELECT a.nosauk  || ' ' || a.adrese || ' ' || a.pilseta AS klient,numurs AS arve, d.created AS kuupaev," +
      		" summa9 AS summa , d.kontr " +
          " FROM dokumenti d INNER JOIN adreses a ON d.san_kods=a.kods WHERE  (kontr = '" + agent.getID()+ "' " +
          		"or kontr_jauns ='" +agent.getID() + "'  or  d.piezimes like '%" +agent.getID() + "%') AND " +
            " d.created > current_timestamp-1 AND d.tips='-'  ORDER BY d.created desc ";

      String sql3 =  "SELECT a.nosauk  || ' ' || a.adrese || ' ' || a.pilseta AS klient,numurs AS arve, d.created AS kuupaev," +
		" summa9 AS summa , d.kontr " +
    " FROM dokumenti d INNER JOIN adreses a ON d.nos_kods=a.kods WHERE (kontr = '" + agent.getID()+ "'" +
    		" or kontr_jauns ='" +agent.getID() + "' or  d.piezimes like '%" +agent.getID() + "%') AND " +
      " d.created > current_timestamp-1 AND d.tips='+'  ORDER BY d.created desc ";

      String sql4 =  "SELECT a.nosauk  || ' ' || a.adrese || ' ' || a.pilseta AS klient,numurs AS arve, d.created AS kuupaev," +
      		" summa9 AS summa , d.kontr " +
          " FROM dokumenti d INNER JOIN adreses a ON d.san_kods=a.kods WHERE ( kontr = 'FA" + agent.getID()+ "'" +
          		" or kontr_jauns ='FA" +agent.getID() + "' or  d.piezimes like '%" +agent.getID() + "%') AND " +
            " d.created > current_timestamp-1 AND d.tips='-'  ORDER BY d.created desc ";

      String sql5 =  "SELECT a.nosauk  || ' ' || a.adrese || ' ' || a.pilseta AS klient,numurs AS arve, d.created AS kuupaev," +
		" summa9 AS summa , d.kontr " +
    " FROM dokumenti d INNER JOIN adreses a ON d.nos_kods=a.kods WHERE   ( kontr = 'FA" + agent.getID()+ "' or " +
    		"kontr_jauns ='FA" +agent.getID() + "' or  d.piezimes like '%" +agent.getID() + "%') AND " +
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
                      	+" EUR "	+  "\n\n");
                      }
                       rs2.close();
                       


                     ResultSet rs3 = st.executeQuery(sql3);
                         while (rs3.next()) {
                         	if(second){
                         		sb.append(header2);
                         		second = false;
                         	}

                         sb.append(rs3.getString("klient")  +
                         		"\n    (" + rs3.getString("arve").trim() + ")[" + rs3.getString("kuupaev") + "] " +
                         		getParent().getNF().format(rs3.getDouble("summa"))
                         	+" EUR "	+  "\n\n");
                         }
                         rs3.close();

                      } catch (Exception e) {
                          getParent().showError(e);
                      }
                      
                      
                    
          
          
          
          
          
          String sqlunsold =  "select  klienta_nosauk as knimi , dok_number as doknr, dok_datums as kuup "
          		+ " , artikuls as toode , preces_nosauk  as tnimi , PASUT_DAUDZUMS-DAUDZUMS as kogus "+ 
" from ORDER_STATISTIC( current_timestamp-3, current_timestamp)  where PASUT_DAUDZUMS>DAUDZUMS and  agents ='" +agent.getID() + "'" ;

        	      
        	      
        	      System.out.println("siin");
        	       
        	         header = " Missing orders  :\n\n";
        	         first = true;
        	         second = true;
        	          try {
        	            Connection conl = this.getNewCon();
        	                   Statement st = conl.createStatement();
        	                  ResultSet rs2 = st.executeQuery(sqlunsold);
        	                      while (rs2.next()) {
        	                      	if(first){
        	                      		sb.append(header);
        	                      		first = false;
        	                      	}

        	                      sb.append(rs2.getString("knimi").trim()  +
        	                      		"\n    (" + rs2.getString("doknr").trim() + ")[" + rs2.getString("kuup").substring(0, 10)
        	                      		+ "] {" +  rs2.getDouble("kogus") + "} " + rs2.getString("toode").trim() + " " + 
        	                      		rs2.getString("tnimi") + "\n\n");
        	                      }
        	                       rs2.close();
          
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

    public void setBulkSalesLog(Map agents){
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
            Connection con = this.getNewLogCon();
              Statement stmt = con.createStatement();

              Calendar cal = Calendar.getInstance();
              cal.set(Calendar.DAY_OF_MONTH, 1);

              ResultSet	      rs = stmt.executeQuery(cmd);
              while(rs.next()) {
                String agentID = rs.getString("agent");
              try{
            	     agentID = agentID.substring(2);
                     Agent agent = (Agent)agents.get(agentID);
                     if (agent != null){
                     	int sales =(int)rs.getDouble("sales");
                     	
                      agent.setMonthSales( sales);
                       agent.setStart(agent.getStart() + " LS:" + sales);
                       System.out.println(agentID + " ls.." + agent.getMonthSales());
                     }  
              }catch(Exception e){
            	  
              }
           
              }
              rs.close();
              stmt.close();
              con.close();
          } catch (Exception e) {
              getParent().showError(e);
          }

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
            	  int sales = (int)rs.getDouble("sales");
                agent.setMonthSales(sales);
                agent.setStart("JG:" + agent.getMonthSales());
               // if(agentID.equals("01126"))
              //  System.out.println(agentID + " jg.." + agent.getMonthSales());
              }
            }
            rs.close();
            stmt.close();
            con.close();
            
           // setBulkSalesLog(agents);
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
//"
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

    }


    
    
    
    
    public void getLogSaveStockInfo(StringBuffer products){
    	
  	  
  	  String  cmd = "   SELECT artikuls AS toode, atlrez AS stock from pdastocks " ;

      	  
  	       if (isDebug()) System.out.println(cmd);
  	       log(cmd);
  	       try {

  	          Connection con = this.getNewLogCon();
  	         Statement st = con.createStatement();
  	         ResultSet rs = st.executeQuery(cmd);

  	         while (rs.next()) {

  	           String kood = rs.getString("toode").trim();
  	          
  	           double seis = rs.getDouble("stock");
  	           if(seis > 0){
  	               products.append(kood + "; ;" + getParent().getNF().format(seis)   +  "\n"); // + dealer +  "\n");
  	             }
  	           }
  	           rs.close();
  	           
  	       } catch (Exception e) {
  	           getParent().showError(e);
  	       }
  	   }


    

    public String getSaveStockInfo(){

    	  String  cmd = " SELECT artikuls AS toode, atlrez AS stock from pdastocks " ;

    
    	  
    	  

    	  /*SELECT artikuls AS toode,sum(atlrez) AS stock from nolik_avg('LVL',
'01.01.1999', 0, '010101', '0', '0', '', '', '') group by
artikuls Order by artikuls
*/
    	  
	       if (isDebug()) System.out.println(cmd);
	       log(cmd);
	       StringBuffer products = new StringBuffer();
	       try {

	          Connection con = getNewCon();
	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmd);

	         while (rs.next()) {

	           String kood = rs.getString("toode").trim();
	         //  String dealer = rs.getString("LADU").trim();
	           double seis = rs.getDouble("stock");
	           if(seis > 0){
	               products.append(kood +  "; ;");
	               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); 
	             }
	           }
	           rs.close();
	        } catch (Exception e) {
	           getParent().showError(e);
	       }
	       return products.toString();
	   }





	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
       String sql = "INSERT into supervisor values ("
         + "?, "   //   agent	C(50),
         + "?,"   //   agentname	C(200),
         + "?,"   //   clientid	C(50),
         + "?,"   //   clientname	C(200),
         + "?,"   //	prodid	C(50),
         + "?,"   //   prodname	C(200),
         + "current_timestamp ,"   //	sentdate	D,
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
         	Connection ddd = DriverManager.getConnection(url2, "SYSDBA", "master");
             PreparedStatement ps = ddd.prepareStatement(sql);


             String sql2 = "select custid from supervisor where extract(year from sentdate)=extract(year from current_timestamp) " +
             		"and  extract(month from sentdate)= extract(month from current_timestamp) and agentid = '" +  wr.getInfo().getAgentID() + "' group by custid";
             Connection ccc = DriverManager.getConnection(url2, "SYSDBA", "master");


             for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {

             	  Statement st3 = ccc.createStatement();
                  ResultSet rst =  st3.executeQuery(sql2);
                  int counter = 0;
                 while(rst.next()){
                 	counter ++;
                 }

                 //aas
               SuperVisorLineWrapper lw = new SuperVisorLineWrapper(iter.next());
               ps.setString(1, wr.getInfo().getAgentID());
               ps.setString(2,  Converter.deCode(wr.getInfo().getAgentName()));
               ps.setString(3, lw.getLine().getCustID());
               ps.setString(4, Converter.deCode(lw.getLine().getCustName()));
               ps.setString(5, lw.getLine().getProdID());
               ps.setString(6,  Converter.deCode(lw.getLine().getProdName()));
               ps.setDouble(7, lw.getLine().getNormalPrice()*100);
               ps.setDouble(8, lw.getLine().getAmount()*100);
               ps.setDouble(9, lw.getLine().getGroupTotal()*100);
               ps.setDouble(10, lw.getLine().getCategoryTotal()*100);
               ps.setInt(11, lw.getLine().getPercentageFromTotalCategory());
               ps.setInt(12, lw.getLine().getPercentageFromColdCategory());
               ps.setInt(13, lw.getLine().getPercentageFromPallCategory());
               ps.setInt(14, lw.getLine().getPercentageFromShelfCategory());
               //ps.setInt(15, lw.getLine().getPercentageFromRestCategory());
               ps.setInt(15, counter);
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
//ss
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
               int ii = lw.getLine().getResInt1();
               int res = 0;
               if(ii == 0 ){
               	res = 1;
               }
               if(ii == 1 ){
               	res = 0;
               }
               if(ii == 2 ){
               	res = -1;
               }
               ps.setInt(33, res);
               ps.setInt(34, lw.getLine().getResInt2());
               ps.setInt(35, lw.getLine().getResInt3());
               boolean isOK = true;


               String sql3 = "select custid from supervisor where extract(year from sentdate)=extract(year from current_timestamp) " +
        		"and  extract(month from sentdate)= extract(month from current_timestamp) and agentid = '" +  wr.getInfo().getAgentID() + "'" +
        				" AND  custid ='" + lw.getLine().getCustID()+ "' AND  prodid ='" + lw.getLine().getProdID()+ "'";
        Connection ccd = DriverManager.getConnection(url2, "SYSDBA", "master");



        	  Statement st4 = ccd.createStatement();
             ResultSet rst4 =  st4.executeQuery(sql3);

            while(rst4.next()){
           // isOK = false;  selle l�lisin v�lja sanda soovil
            }

               rst4.close();
               st4.close();
               ccd.close();
               System.out.println("isOK" + isOK + sql3);
               if(isOK){
               	ps.executeUpdate();

               }
                }

             ps.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
       }

	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	       String sql = "INSERT into supervisor values ("
	         + "?, "   //   agent	C(50),
	         + "?,"   //   agentname	C(200),
	         + "?,"   //   clientid	C(50),
	         + "?,"   //   clientname	C(200),
	         + "?,"   //	prodid	C(50),
	         + "?,"   //   prodname	C(200),
	         + "current_timestamp ,"   //	sentdate	D,
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
	         	Connection ddd = DriverManager.getConnection(url2, "SYSDBA", "master");
	             PreparedStatement ps = ddd.prepareStatement(sql);


	             String sql2 = "select custid from supervisor where extract(year from sentdate)=extract(year from current_timestamp) " +
	             		"and  extract(month from sentdate)= extract(month from current_timestamp) and agentid = '" +  wr.getInfo().getAgentID() + "' group by custid";
	             Connection ccc = DriverManager.getConnection(url2, "SYSDBA", "master");


	             for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {

	             	  Statement st3 = ccc.createStatement();
	                  ResultSet rst =  st3.executeQuery(sql2);
	                  int counter = 0;
	                 while(rst.next()){
	                 	counter ++;
	                 }

	                 eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
	               ps.setString(1, wr.getInfo().getAgentID());
	               ps.setString(2, Converter.deCode(wr.getInfo().getAgentName()));
	               ps.setString(3, lw.getLine().getCustID());
	               ps.setString(4, Converter.deCode(lw.getLine().getCustName()));
	               ps.setString(5, lw.getLine().getProdID());
	               ps.setString(6, Converter.deCode(lw.getLine().getProdName()));
	               ps.setDouble(7, lw.getLine().getNormalPrice()*100);
	               ps.setDouble(8, lw.getLine().getAmount()*100);
	               ps.setDouble(9, lw.getLine().getGroupTotal()*100);
	               ps.setDouble(10, lw.getLine().getCategoryTotal()*100);
	               ps.setInt(11, lw.getLine().getPercentageFromTotalCategory());
	               ps.setInt(12, lw.getLine().getPercentageFromColdCategory());
	               ps.setInt(13, lw.getLine().getPercentageFromPallCategory());
	               ps.setInt(14, lw.getLine().getPercentageFromShelfCategory());
	               //ps.setInt(15, lw.getLine().getPercentageFromRestCategory());
	               ps.setInt(15, counter);
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
	               ps.setString(24, Converter.deCode(cc));

	               String cd = lw.getLine().getComment();
	               if (cd == null){
	               	cd = " ";
	               }
	               ps.setString(25, Converter.deCode(cd));

	               String ce = lw.getLine().getCustAgentId();
	               if (ce == null){
	               	ce = " ";
	               }

	               ps.setString(26, ce);

	               String cf = lw.getLine().getCustAgentName();
	               if (cf == null){
	               	cf = " ";
	               }
	               ps.setString(27, Converter.deCode(cf));
	               ps.setInt(28, lw.getLine().getDealNr());
	               ps.setInt(29, lw.getLine().getIdealNr());

	               String cr1 = lw.getLine().getResStr1();
	               if (cr1 == null){
	               	cr1 = " ";
	               }
	               ps.setString(30, Converter.deCode(cr1));


	               String cr2 = lw.getLine().getResStr2();
	               if (cr2 == null){
	               	cr2 = " ";
	               }
	               ps.setString(31, Converter.deCode(cr2));


	               String cr3 = lw.getLine().getResStr3();
	               if (cr3 == null){
	               	cr3 = " ";
	               }
	               ps.setString(32, Converter.deCode(cr3));
	               int ii = lw.getLine().getResInt1();
	               int res = 0;
	               if(ii == 0 ){
	               	res = 1;
	               }
	               if(ii == 1 ){
	               	res = 0;
	               }
	               if(ii == 2 ){
	               	res = -1;
	               }
	               ps.setInt(33, res);
	               ps.setInt(34, lw.getLine().getResInt2());
	               ps.setInt(35, lw.getLine().getResInt3());
	               boolean isOK = true;


	               String sql3 = "select custid from supervisor where extract(year from sentdate)=extract(year from current_timestamp) " +
	        		"and  extract(month from sentdate)= extract(month from current_timestamp) and agentid = '" +  wr.getInfo().getAgentID() + "'" +
	        				" AND  custid ='" + lw.getLine().getCustID()+ "' AND  prodid ='" + lw.getLine().getProdID()+ "'";
	        Connection ccd = DriverManager.getConnection(url2, "SYSDBA", "master");



	        	  Statement st4 = ccd.createStatement();
	             ResultSet rst4 =  st4.executeQuery(sql3);

	            while(rst4.next()){
	           // isOK = false;  selle l�lisin v�lja sanda soovil
	            }

	               rst4.close();
	               st4.close();
	               ccd.close();
	               System.out.println("isOK" + isOK + sql3);
	               if(isOK){
	               	ps.executeUpdate();

	               }
	                }

	             ps.close();
	         } catch (SQLException e) {
	             e.printStackTrace();
	         }
	       }



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

    public static final String DRIVER_VERSION = "SolcraftJun";
    private String url = "jdbc:odbc:materiali";
    private String urllog = "jdbc:odbc:logsol";
    private Connection ddd ;
    private Connection bbb ;
    private String firma;
    private String firma2;
    private   HashMap logMap = new HashMap();
    private String firma3;
    private String firma4;
    private String firma5;
    private String firma6;
    private String firma7;
    private String firma8;
    private String firma9;
    private Properties locProps = null;
    private String             url2 =  "jdbc:odbc:materiali";
    //"jdbc:odbc:report";
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

