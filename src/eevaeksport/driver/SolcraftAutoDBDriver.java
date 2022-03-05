
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;

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
public class SolcraftAutoDBDriver extends AbstractDBDriver implements Discounter, Manager,Promo, BulkUpdater{

 
    public SolcraftAutoDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
        try {
            Class.forName(databaseDriverName).newInstance();
        
        } catch (Exception e) {
            parent.showError(e);
        }
        
        if("true".equals(parent.getProps().getProperty("lithuanian"))){
        	lithuanian = true;
        }
        
        
    }
    
    private boolean lithuanian = false;
    
    
    public HashMap getTobisData(){
		HashMap hash = new HashMap();
		
			try {
		
		
		String httpsURL = "https://www.tobis.lt/MeRiSoFt/stockref.txt";
		//https://www.tobis.lt/MeRiSoFt/stockref.txt

		final File  v  = new File("stocktobis.txt");
	
       	Converter.doTrustToCertificates();
           URL myurl = new URL(httpsURL);
           HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
           InputStream ins = con.getInputStream();
           InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
           BufferedReader in = new BufferedReader(isr);

           String inputLine;
           StringBuffer inputLineBuf = new StringBuffer();
           while ((inputLine = in.readLine()) != null){
           	inputLineBuf.append(inputLine);
          }

           in.close();
           
           if(inputLineBuf.length() > 2){
             
                 try {
               	  File f = new File ( "stocktobis.txt");
               	  if(f.exists())f.delete();
               	  
               	  
             	 Converter.saveFileInUTF8("stocktobis.txt", inputLineBuf.toString());
           	
           	  }catch (Exception ex) {
           		ex.printStackTrace();
           	  }
            }
		
			

				BufferedReader  inse = new BufferedReader(new FileReader(v));
				String	    line;
			
				while ((line = inse.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(line, ";");
					
					String prodID = "";
					double amount = 0;
					//System.out.println("id " + line);
					if (st.hasMoreTokens()) {
						prodID = st.nextToken();
					}
					st.nextToken();
				
					if (st.hasMoreTokens()) {
						amount = new Double(st.nextToken()).doubleValue();
					}
					hash.put(prodID,amount + "" );
						
				}
				inse.close();
               }catch(Exception e){
				e.printStackTrace();
			}
  	
		  return hash;
	  }


    public String getSaveDebtInfo() {
    	
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
            return lines;
    }
        
        
   
        public Vector getPriceMatrixData() {
        	 Vector lines = new Vector();
        	 String sql = "   SELECT  1 AS clgroup, pr.artikuls   AS prodid,   cena1 AS price  FROM  preces  pr  where cena1 > 0 " +
			" union  SELECT  2 AS clgroup, pr.artikuls   AS prodid,   cena2 AS price  FROM  preces  pr  where cena2 > 0 " +
			" union  SELECT  3 AS clgroup, pr.artikuls   AS prodid,   cena3 AS price  FROM  preces  pr  where cena3 > 0 " +
			" union  SELECT  4 AS clgroup, pr.artikuls   AS prodid,   cena4 AS price  FROM  preces  pr  where cena4 > 0 " +
			" union  SELECT  5 AS clgroup, pr.artikuls   AS prodid,   cena5 AS price  FROM  preces  pr  where cena5 > 0 " +
			" union  SELECT  6 AS clgroup, pr.artikuls   AS prodid,   cena6 AS price  FROM  preces  pr  where cena6 > 0 " +
			" union  SELECT  7 AS clgroup, pr.artikuls   AS prodid,   cena7 AS price  FROM  preces  pr  where cena7 > 0 " +
			" union  SELECT  8 AS clgroup, pr.artikuls   AS prodid,   cena8 AS price  FROM  preces  pr  where cena8 > 0 " +
			" union SELECT  9 AS clgroup, pr.artikuls   AS prodid,   cena9 AS price  FROM  preces  pr  where cena9 > 0 " +
			" union  SELECT  10 AS clgroup, pr.artikuls   AS prodid,   cena10 AS price  FROM  preces  pr  where cena10 > 0 " +
			" union  SELECT  11 AS clgroup, pr.artikuls   AS prodid,   cena11 AS price  FROM  preces  pr  where cena11 > 0 " +
    		" union  SELECT  12 AS clgroup, pr.artikuls   AS prodid,   cena12 AS price  FROM  preces  pr  where cena12 > 0";
               
                try {

              	Statement stmt = this.getNewCon().createStatement();
              	        ResultSet rs = stmt.executeQuery(sql);

                        while (rs.next()) {

                                String clGroup = rs.getString("clgroup");
                                String prodID = rs.getString("prodid");
                                double price = rs.getDouble("price");
                               MatrixLine mline = new MatrixLine();

                                if (mline.getCustID() == null && clGroup != null
                                                && clGroup.trim().length() > 0) {
                                        mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
                                }

                                if (prodID != null && prodID.trim().length() > 0 ) {
                                        mline.setProdID(prodID.trim());
                                }

                                if (price != 0) {
                                        mline.setPrice(price);
                                }

                                lines.add(mline);
                        }
                        
                        stmt.close();
                        System.out.println("Teen Gruppidele Matixit " +lines.size());
               
                } catch (SQLException ex) {
                ex.printStackTrace();
                }
                
                
                
                
               sql = "    SELECT ' ' AS custid, ale.nosaukums AS clgroup, " + 
    				  "  pr.artikuls   AS prodid, pr.artikuls  AS prodgroup, " + 
    				"	 tt.atlaide AS discount,   tt.cena AS price ,tips ,cast(tt.ATLAIDE as integer)  as ale,tt.NO_DATUMA as algus, " +
    				" tt.LIDZ_DATUMAM as lopp , '' as lisahind ,COMM as lisainff " +
    				" FROM atlaizhu_grupas ale inner join atlaizhu_grupas_saturs abi on ale.id=abi.atlgrupas_id  " +
    				"     inner join atlaides_saturs tt on tt.id=abi.id  " +
    			"		 inner join preces  pr on pr.kods= tt.kods  " +
    			"	     where   tt.no_datuma<=current_timestamp and current_timestamp<=tt.lidz_datumam + 1  " +
    			"	     order by  ale.nosaukums , pr.nosauk2";
               
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
                                double discount = rs.getDouble("discount");
                                double price = rs.getDouble("price");
                                String tips = rs.getString("tips").trim();
                                String sdate =  rs.getString("algus");
                				String tdate =  rs.getString("lopp");
                                String lisacomm = rs.getString("lisainff");
                                		

                                MatrixLine mline = new MatrixLine();
                                
                                 
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
                                }else{
                                	mline.setDiscount(discount);
                                }
                                String algus = "";
                                
                                if(sdate != null && sdate.trim().length() != 0){
                                	algus = sdate.substring(0,10);
                                }
                                
                               
                            
                			
                				 if(tdate != null && tdate.trim().length() != 0){
                                   mline.setEndDate(tdate.substring(0,10));
                                   if(!algus.equals("")){
                                       mline.setStartDate(algus);	
                                       }
                                 }
                                lines.add(mline);
                        }
                        
                        stmt.close();
                        System.out.println("Teen Gruppidele Matixit " +lines.size());
               
                        
                        

                        String sql2 = "  SELECT pkonts AS custid, '' AS clgroup,  pr.artikuls   AS prodid,  pr.artikuls  AS prodgroup,  tt.atlaide AS discount, " +
             				    " tt.cena AS price ,tt.tips,tt.NO_datuma as algus,tt.LIDZ_DATUMAM as lopp FROM atlaides_saturs tt " +
             					" INNER JOIN klientu_atlaides aa on aa.id= tt.id inner join preces  pr on pr.kods= tt.kods " +
             					" INNEr JOIN adreses adr on adr.kods=aa.kl_kods  where " + 
             				    "  ((tt.no_datuma<=current_timestamp and current_timestamp<=tt.lidz_datumam + 1 ) " +
								" or (tt.no_datuma<=current_timestamp and tt.lidz_datumam is null)) " +
             				    " order by pkonts,  pr.nosauk2 ";
                        
                        ResultSet rs2 = stmt2.executeQuery(sql2);

                        while (rs2.next()) {
                                String custID = rs2.getString("custid");
                                String clGroup = rs2.getString("clgroup");
                                String prodID = rs2.getString("prodid");
                                String prodGroup = rs2.getString("prodgroup");
                                double discount = rs2.getDouble("discount");
                                double price = rs2.getDouble("price");
                                String tips = rs2.getString("tips").trim();
                                
                				String sdate =  rs2.getString("algus");
                				String tdate =  rs2.getString("lopp");
                        		
                                MatrixLine mline = new MatrixLine();
                                String add1= "";
                                

                                if (custID != null && custID.trim().length() > 0) {
                                       // mline.setCustID(custID.trim());

                                        mline.setCliGroup(Converter.cleanLitLatEst(custID.trim()));
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
                                  }else{
                                	mline.setDiscount(discount);
                                 }
   
                               String algus = "";
                                if(sdate != null && sdate.trim().length() != 0){
                                	algus = sdate.substring(0,10) ;
                                }
                                
                               
                            
                			
                				 if(tdate != null && tdate.trim().length() != 0){
                                   mline.setEndDate(tdate.substring(0,10));
                                   if(!algus.equals("")){
                                       mline.setStartDate(algus);	
                                      }
                                 }
                                
                                lines.add(mline);
                        }

                        System.out.println("Teen kLientidele Matixit " + lines.size());

                } catch (SQLException ex) {
                ex.printStackTrace();
                }
                
                return lines;
            	          	        	
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
    	try{
    		 return DriverManager.getConnection(url, "SYSDBA", "mustangs");		
    	}catch(SQLException e){
    		 return DriverManager.getConnection(url, "SYSDBA", "mustangs");
    	}
     
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
    String    sql = " SELECT grupa as asi, nosauk AS nimetus2  from grupas   " ;


    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
    	String asi =  rs.getString("asi").trim();
    	String nimetus2 =  rs.getString("nimetus2");
    	if(nimetus2 != null && asi.length() > 0){
    	 	 tm.put(asi, Converter.enCodeEst(nimetus2.trim()));
    	   }else if (nimetus2 == null){
    			 tm.put(asi, Converter.enCodeEst(asi));
    	   }
       	    }
  
    rs.close();
    st.close();
    con.close();
    System.out.println("tootegrupid ending " );
     return tm;
    }


        public String getDistribution() {
          return null;
        }



    public TreeMap getAgents() {
      String  cmd = "select nosauk AS kood , nosauk || ' ' ||num as nimi  from kontr  " +
      	"	where nosauk != ''  ORDER BY num";
        TreeMap agents = new TreeMap();
       // this.getTobisData();
     //   getLitNames(null);
        try {
        	
          Connection con = getNewCon();
            Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);
            System.out.println("Agents Starting ");
            while (rs.next()) {
            	String kk = rs.getString("kood").trim();
            	if(kk.equals("Mārcis")) {
            		kk = "Marcis";
            	}
            	
              Agent agent = new Agent(kk);
              String nimi = rs.getString("nimi").trim();
              agent.setName(nimi);
              agent.setIsPilot(true);
              agents.put(nimi, agent);
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
        agents.put("HOUSE", hh);
        System.out.println("Agents Ready ");
        return agents;
    }



    private String getDir(){
      String dir = getParent().getProps().getProperty("readin");
      if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
      return dir;
    }



    public TreeMap getCustomers(Agent agent){

      String cmd = " Select adr_nosauk AS nimi , kl.KL_KODS AS payer,kl.pkonts ,kl.pkonts || '-' || kl.id AS kood,kl.adrese  AS aadress,0 AS discount,'' AS visit, " +
		" kl.per_vards  || ' ' || kl.per_talr || ' ' || kl.per_epasts AS persinfo ,kl.comm as info,ag.NOSAUK AS agent ,a.kons_dienas AS makse	 " +
		" , a.ATLAIDE as ale , a.CENA as hinnak from FWM_KLIENTA_ADRESES kl INNER JOIN KONTR ag ON ag.num = kl.kontr "
		+ " LEFT JOIN adreses a ON kl.KL_KODS=a.KODS where adr_nosauk is not null ";

      StringBuffer buf = new StringBuffer();


        log(cmd);
        
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        HashMap custMap2 = new HashMap();
        
        
        Vector sup2 = new Vector();
        try {
          Connection con = getNewCon();

          Statement stmt = con.createStatement();
         
          
          String sql3 = " SELECT * FROM  F_CUST_CREDITLIST ";
          System.out.println(sql3);
             ResultSet rst = stmt.executeQuery(sql3);
             while(rst.next()){
             	String custom = rst.getString("KL_KODS");
             	Customer cc = new Customer(custom.trim());
             	
             		
             	  double saldo = rst.getDouble("SALDO");
                if(saldo != 0){
                	cc.setUsedCredit(saldo);
                }
                double credit = rst.getDouble("CREDIT");
                if(credit != 0){
                	cc.setCredit(credit+"");
                }
             		boolean cblock = false;
             	String C_BLOCK = rst.getString("C_BLOCK");
             	 if(C_BLOCK.equals("+")){
             		 //siis yletamisel ei luba myya
             		cblock = true;
             	 }
             	 
                 double debt = rst.getDouble("DEBT");
                 if(debt != 0){
                 	cc.setDebt(debt);
                 }
     double adebt = rst.getDouble("AL_DEBT");
             	boolean dblock = false;
                	String D_BLOCK = rst.getString("D_BLOCK");
                	 if(D_BLOCK.equals("+")){
                		 //siis kuup�evade yletamisel ei luba myya
                		dblock = true;
                	 }
                	 int status = rst.getInt("R_CODE");
                	 if(status == 1){
                		 cc.setSalesBlock(true);
                	 }
                	if(status == 2){
               		 cc.setSalesBlock(true);
               	 }
                	
                	if(dblock && debt > adebt){
                		 cc.setSalesBlock(true);
                	}
                	
                	if(cblock && saldo-adebt > credit){
               		 cc.setSalesBlock(true);
               	}
                	
                	custMap.put(cc.getID(), cc);
             }
             rst.close();

          
          
          
          
          
          
          ResultSet	      rs = stmt.executeQuery(cmd);
          
            while (rs.next()) {
          
            	String nimi = rs.getString("nimi").trim();
            	nimi = nimi.replaceAll("FINANCE", "Fin.");
            	nimi = nimi.replaceAll("FINNANCE", "Fin.");
            	nimi = nimi.replaceAll("LIMITED", "Lim.");
            	nimi = nimi.replaceAll("SIDEPARK", "Sidep.");
            	nimi = Converter.firstToUpperCase(nimi);
                if(!nimi.endsWith("#")){
                	String payer = rs.getString("payer");

                	String konts = rs.getString("pkonts");
            
                	
                	String kood = rs.getString("kood");
                	if(kood != null){
                	
                Customer cust = new Customer(kood.trim());
                         cust.setPayer(payer);
                         if(konts != null){
                        	 cust.setGroup(Converter.cleanLitLatEst(konts.trim()));
                         }
                        
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
              //    cust.setVisit(visit.trim().toUpperCase());
                }
                
          String info = rs.getString("persinfo");
                if(info != null && info.trim().length()>0){
                	cust.setPersonalData(Converter.enCodeEst(info.trim()));
                }
                info = rs.getString("info");
                if(info != null && info.trim().length()>0){
                	cust.setCity(Converter.enCodeEst(info.trim()));
                }

                String agentc = rs.getString("agent");

              if(agentc != null){
            	  
            	  if(agentc.equals("Mārcis")) {
            		  agentc = "Marcis";
              	}
            		cust.addAgent(new Agent(agentc.trim()));
            		cust.addAgent(new Agent("KA"));
              }
                
                                
         String makset = rs.getString("makse");
         if (makset != null && makset.trim().length()>0){
         	if(makset.trim().equals("0")){
         		makset = "-1";
         	}
            cust.setPaymTerm(makset.trim());
         }
     

         double aleke = rs.getDouble("ale");
         if(aleke > 0 ){
        	 cust.setDiscount(aleke);
         }
         
         
         String alegr = rs.getString("hinnak");
         if(alegr != null && alegr.trim().length()>0){
        	 if(cust.getGroup() != null && cust.getGroup().trim().length() > 0){
        		 cust.setGroup(cust.getGroup() + "|" + Converter.cleanLitLatEst(alegr.trim()));
        	 }else{
        		 cust.setGroup( Converter.cleanLitLatEst(alegr.trim()));
        	 }
         }

         
        if(custMap.get(cust.getPayer()) != null){
	        Customer cc = (Customer)custMap.get(cust.getPayer());
        	 cust.setDebt(cc.getDebt());
        	 cust.setUsedCredit(cc.getUsedCredit());
        	 cust.setCredit(cc.getCredit());
        	 cust.setSalesBlock(cc.getSalesBlock());; 
          }
         
              customers.put(cust.getName(), cust);
              custMap2.put(cust.getID(), cust);
          	
                }
              }
            }

            rs.close();
            stmt.close();
               con.close();
               
        } catch (Exception e) {
            getParent().showError(e);
        }
        System.out.println("kliendid l�petan ");
        return customers;
    }


    
    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      System.out.println("debt alustan ");
      String sql = " SELECT * FROM  F_CUST_BILLLIST ";
      Connection con = getNewCon();
      Statement stmt = con.createStatement();

      ResultSet rs = stmt.executeQuery(sql);

      while(rs.next()){
        Debt debt = new Debt();
        String klient = rs.getString("KL_KODS");
        if(klient != null){
        	   debt.setCustID(klient.trim());
               debt.setDoknr(rs.getString("DOK_NR").trim());
               Date dd = rs.getDate("DUE_DATE");
               if (dd == null){
            	   dd = new Date();
               }
               debt.setDuedate(dd);
               debt.setDate(rs.getDate("DOK_DATE"));

               debt.setSum(Double.parseDouble(rs.getString("AMOUNT").trim()));
               String aa = rs.getString("OVER_DUE");
               if(aa == null ||aa.trim().length() == 0){
            	   aa = "0";
               }else{
            	   if(aa.startsWith("-")){
            		   aa = aa.substring(1);
            	   }else{
            		  aa = "-" +aa;
            	   }
               }
               debt.setDue(aa);
               if(debt.getSum() > 0.05 ||  -0.05 > debt.getSum()  )
               list.addElement(debt); 	
        }
     
      }
       rs.close();
       stmt.close();
       con.close();
       System.out.println("debt l�petan  " + list.size());
     
       return list;
    }

  
    private TreeMap getLitNames (TreeMap names){
    	
    	try{
    		
    		if (names  == null){
    			names = new TreeMap();
    		}
    		FileInputStream is1 = new FileInputStream("item-lt.txt");
    	
	
	
	BufferedReader br = new BufferedReader(new InputStreamReader(is1));
	//,"Cp1252"));
	String line;
	
	while ((line = br.readLine()) != null) {

		try{
		
			if(line.trim().length() > 0){
				StringTokenizer st = new StringTokenizer(line, "|");
				if (st.hasMoreTokens()) {
					names.put(st.nextToken(), st.nextToken());
				}	
			}
			
		}catch(Exception r ){
			r.printStackTrace();
		}
		
	}
}catch(Exception e){
    e.printStackTrace();		
    	}
    	System.out.println("names " + names.size() + " " + names.get("_3M00536") );
    	return names;
    }
   
    
    
    public TreeMap getProducts(Agent agent){

        TreeMap names = new TreeMap();
        if(lithuanian){
        	names = getLitNames(names);
        }
       // System.out.println("names " + names.toString());

      String cmd = "SELECT pr.artikuls AS toode,pr.nosauk AS nimi,pr.cena1 AS myygihind, 21 AS VAT,pr.mervien AS unit,pr.svars AS " + 
      		" tykke ,grupa AS grupp , pr.ecrkods AS ean  FROM preces pr WHERE act='+' ";
      
      System.out.println(" tooted alustan ");

        if (isDebug()) {
            System.out.println(cmd);
        }

        TreeMap products = new TreeMap();
        try {
        	 Connection con = null;
        	try{
        		con = getNewCon();
                		
        	}catch (Exception r){
        		con = getNewCon();
                
        	}
          Statement st = con.createStatement();
            ResultSet	      rs = st.executeQuery(cmd);
            HashMap prodHash = new HashMap();
            while (rs.next()) {
                String kood = rs.getString("toode").trim();
                if(kood.length() >2){
                
                String nimetus =   rs.getString("nimi");
                if(names.get(kood) != null ){
                	nimetus = (String)names.get(kood);
                }
                
                	nimetus  = nimetus.trim().replaceAll("\n", " ")  + " " + kood;
                Product prod = new Product(kood);
                prod.setPrice(rs.getDouble("myygihind"));
                prod.setVat(rs.getDouble("VAT") + "");
                String unit = rs.getString("unit");
                if(unit != null && unit.trim().length()>0){
                	prod.setUnit(unit.trim());
                }

                prod.setInCases(rs.getInt("tykke"));
                prod.setStock(-1);
                String grupp = rs.getString("grupp");
                if(grupp != null && grupp.trim().length()>0){
                	prod.setGroup(grupp.trim());
                }
                	  
                prod.setName(Converter.enCodeEst(nimetus));
                
                String ean = rs.getString("ean");
                if(ean != null && ean.trim().length()>0){
                	prod.setEAN(ean.trim());
                }
                
                	 products.put(nimetus, prod);
                     prodHash.put(prod.getID(), prod);
            	
            }
            	
            }
            rs.close();

            
  
            HashSet olemas = new HashSet();
            HashMap tobis =  getTobisData();
          Iterator itera =   tobis.keySet().iterator();
          while(itera.hasNext()){
        	  String key = (String)itera.next();
        	  double stock = Double.parseDouble((String)tobis.get(key));
        	  Product prod = (Product)prodHash.get(key);
              if (prod != null){
            	  olemas.add(key);
            	  prod.setStock(stock);
              }
              
        	  
          }
            

        	 String  sql2 = " select t.artikuls as toode, sum(s.atlikums) as stock "
        	 		+ "from fwm_atlikumi s INNER JOIN preces t on s.preces_kods=t.kods  group by t.artikuls" ;// where   adr_kods=2

            rs = st.executeQuery(sql2);
            while(rs.next()){
              String id = rs.getString("toode").trim();
              Product prod = (Product)prodHash.get(id);
              if (prod != null){
              double stock = rs.getDouble("stock");
            	  prod.setPartii("Riga :" + (int)stock);
                  prod.setName(prod.getName() + " " + "Riga:" + (int)stock);
                if(!olemas.contains(id)){
              	 // prod.setStock(stock);
                }
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
     // return new Vector();

    	String sql1 = " select a.pkonts || '-' || dok.adrese_id AS klient , pr.artikuls AS toode,"
    			+ " daudzums AS kogus, datums AS kuupaev,"
      		+ " rea_cena*((100-atlaides_proc)/100) as lastprice ,dok.numurs as lastinv "+
		   " from dokumenti dok inner join dok_spec rid on rid.dok_kods=dok.kods " +
		   "INNER JOIN adreses a ON a.kods=dok.san_kods " +
		   "INNER JOIN preces pr ON pr.kods=rid.preces_kods where " +
		   "datums > current_timestamp-350 AND funk=2 and daudzums > 0 " +
		   "GROUP BY pr.artikuls,a.pkonts|| '-' || dok.adrese_id,daudzums,datums,rea_cena,atlaides_proc ,dok.numurs " +
		   "ORDER BY a.pkonts|| '-' || dok.adrese_id,pr.artikuls,datums,daudzums " ;
        if (isDebug()) {
            System.out.println(sql1);
        }
        Vector custProds = new Vector();

        try {
          Connection con = getNewCon();
          PreparedStatement stmt = con.prepareStatement(sql1);
          ResultSet	      rs = stmt.executeQuery();

          HashMap kliendid = new HashMap();
          while (rs.next()) {
            String kliendikood = rs.getString("klient").trim();
            String tootekood = rs.getString("toode").trim();
            double kogus = rs.getDouble("kogus");
            double lprice = rs.getDouble("lastprice");
            String dokum = rs.getString("lastinv");
            Customer cust = (Customer)kliendid.get(kliendikood);
            if (cust == null){
              cust = new Customer(kliendikood);
            }
           Product prod = new Product(tootekood);
              CustProd cp = new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                           rs.getString("kuupaev").substring(5,10), lprice, 0, 0, " ");
              if(dokum !=  null){
            	   cp.setAbiKood(dokum.trim());
              }
              
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
                         	+" LVL "	+  "\n\n");
                         }
                         
                         rs3.close();
                         
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
    }

    
    public void setBulkSales(Map agents){
      //System.out.println("bulkSales");
      String cmd = " SELECT a.nosauk AS agent, sum (summa7) AS sales  FROM  " +
"dokumenti dok inner join kontr a on a.num=dok.kontr  WHERE  extract(month from dok.datums)= extract(month from current_timestamp) AND  " +
"extract(year from dok.datums)= extract(year from current_timestamp) AND funk=2 and kontr !='' " +
"and apst='+' and pasut='-' and rezerv='-' AND " +
"dok.kontr is not null GROUP BY a.nosauk " +
"UNION ALL SELECT a.nosauk AS agent,- sum (summa7) AS sales " +
" FROM  dokumenti dok inner join kontr a on a.num=dok.kontr WHERE  extract(month from dok.datums)= extract(month from current_timestamp) " +
"  AND extract(year from dok.datums)= extract(year from current_timestamp) AND funk=7 and kontr !='' and apst='+' and pasut='-' and rezerv='-' AND " +
"  dok.kontr is not null GROUP BY a.nosauk ";
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
        return 0;
    }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
    }


       

    public String getSaveStockInfo(){
    	
    	
    	HashSet olemas = new HashSet();

	       StringBuffer products = new StringBuffer();
    	 
        HashMap tobis =  getTobisData();
      Iterator itera =   tobis.keySet().iterator();
      while(itera.hasNext()){
    	  String key = (String)itera.next();
    	  double stock = Double.parseDouble((String)tobis.get(key));
    	  olemas.add(key);
    	    products.append(key +  "; ;");
            products.append(getParent().getNF().format(stock) + "; "  +  "\n"); 
         
    	  
      }

   	 String  cmd = " select t.artikuls as toode, sum(s.atlikums) as stock "
 	 		+ "from fwm_atlikumi s INNER JOIN preces t on s.preces_kods=t.kods  group by t.artikuls" ; //  where   adr_kods=9


	       if (isDebug()) System.out.println(cmd);
	       log(cmd);
	       try {

	          Connection con = getNewCon();
	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmd);

	         while (rs.next()) {

	           String kood = rs.getString("toode").trim();
	         //  String dealer = rs.getString("LADU").trim();
	           double seis = rs.getDouble("stock");
	           if(!olemas.contains(kood) && seis > 0 ){
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


    public ManagerData getManagerData() throws SQLException{
      return null;
    }


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

    public static final String DRIVER_VERSION = "SolcraftAuto";
    private String url = "jdbc:odbc:solcraft32";
    private Connection ddd ;
    private Connection bbb ;
    private   HashMap logMap = new HashMap();
    private Properties locProps = null;
    private String             url2 =  "jdbc:odbc:solcraft32";
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

