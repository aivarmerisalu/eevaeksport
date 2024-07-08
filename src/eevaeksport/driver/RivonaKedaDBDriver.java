/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.imageio.ImageIO;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.routines.EAN13CheckDigit;
import eevaeksport.util.Converter;
import imageUtil.ImageLoader;


/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class RivonaKedaDBDriver extends AbstractDBDriver implements  BulkUpdater ,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public RivonaKedaDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

        try {
            Class.forName(databaseDriverName).newInstance();

        } catch (Exception e) {
            parent.showError(e);
        }
        user =  getParent().getProps().getProperty("rest.user");
        pass = getParent().getProps().getProperty("rest.pass");
        rcompanyx = getParent().getProps().getProperty("rest.companyx");
        rcompany = getParent().getProps().getProperty("rest.company");
         }
      
      String user =  null;
      String pass = null;
      String rcompany = null;
      String rcompanyx = null;
      

 

    private void makeNormpict(String pictspda,String pictssou ){
  	  File res2 = new File (pictspda);
        String[] ff2 = res2.list();
      //  System.out.println("ff2 suur " + ff2.length);
    	 for(int i = 0; ff2.length > i ; i++){
    		if(ff2[i].indexOf("img") != -1 ){
    	  		try {
    	  			//System.out.println("min " + pictspda + ff2[i] + " | " + ff2[i].substring(0,ff2[i].indexOf("img")) + ".jpg");
    				shrinkAndCopyFile(pictspda + ff2[i],pictssou + ff2[i].substring(0,ff2[i].indexOf("img")) + ".jpg");
    			} catch (Exception e) {
    				e.printStackTrace();
    			}	
    		}
    		
    	 }
    }
    
    
  
    private void makePictureCat(){
	    
      	
      	    String picts = parent.getProps().getProperty("pict.dir");
    	    String pictspda = parent.getProps().getProperty("pictpda.dir");
    	    String pictssou  = parent.getProps().getProperty("pictpda.source");
    	 makeNormpict(picts,pictssou);
    	 //System.out.println("väljun");
    	// System.exit(0);

      	try {
      	TreeMap prods = this.getProducts(null);

           StringBuffer buf = new StringBuffer();
           StringBuffer buf0 = new StringBuffer();
           int a = 0;
           int b = 0;
           int c = 0;
           Iterator enume = prods.keySet().iterator();
           Vector govec = new Vector();
           System.out.println("Piltide leidmine ");
      	    while (enume.hasNext()) {
      	    	String asi = (String)enume.next();
      	    	Product prod = (Product)prods.get(asi);
      	    	a++;
      	    	c++;
      	    	if(c == 100){
      	    		
      	    		  c =0;
      	    		  System.out.println("Piltide leidmine "  + a);	
      	    	}
      	     
                    String toode = prod.getID().trim();  
                    toode = toode.replace('/', '_');
                    File f = new File(pictssou + toode+".jpg");
                    String len = "0";
                    if(f.exists()){
                      len =	f.length() + "";
                       govec.add(toode+".jpg");
                    }
                    if(len.equals("0")){
                    	//System.out.println(toode+".jpg;"  + len);
                    	buf0.append(toode+".jpg\n");
                    }else{
                    	buf.append(toode+".jpg;"  + len + "\n");
              	    	b++;
                    }
                    }
      	
      	
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
//           	 Lõppes kustutamine 
           	 
             System.out.println("Piltide kopeerimine ");	
             //Failide kopeerimine  
               String[] gg = new File(pictssou).list();
            	  //System.out.println("gg " + gg[0] + " " + gg.length);
               
                for(int i = 0;gg.length>i;i++){
                	
                	File in = new File ( pictssou + gg[i]); 
                	if(in.isFile()){
                	
                	File inpda = new File ( pictspda + gg[i]); 
                     if(govec.contains(gg[i])){
                    	 copyFile(in,inpda); 
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
    
    
    public void shrinkAndCopyFile (String in, String out) throws Exception {

        try {
        	BufferedImage inb = ImageIO.read(new File(in));
        	BufferedImage newImage = new BufferedImage(inb.getWidth(), inb.getHeight(), BufferedImage.TYPE_INT_RGB);
        	Graphics2D g = newImage.createGraphics();
        	g.drawImage(inb, 0, 0, null);
        	g.dispose();
        	 File outputfile = new File(out);
        	 ImageIO.write(newImage, "jpg", outputfile);
        	
          ImageLoader.fromFile(out).getResizedToWidth(300).writeToJPG(new File(out), 0.7f);//.soften(0.95f)
        }
        catch (Exception ioe) {
        	ioe.printStackTrace();
        }
          }
 
  	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
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


            public String getSaveStockInfo(){

            	 String  cmd = "  SELECT  I17_KODAS_PS AS toode, sum(I17_kiekis - I17_atiduota - i17_rezervas)/(case when N17_KODAS_US ='KG' or   N17_KODAS_US ='M' then 1000 else 1 end)\r\n" + 
            	 		"              	  		  AS STOCK from i17_vpro inner join n17_prod on  I17_KODAS_PS = N17_KODAS_PS \r\n" + 
            	 		"    where I17_KODAS_IS in ('KS000001','KS000005','KS000002','KS000003','KS000080') AND ((I17_kiekis - I17_atiduota - i17_rezervas) >0 OR (I17_P_PAR_K)>0) and n17_kodas_ls_1<>''  and  N17_POZ_DATE =0 \r\n" + 
            	 		"  			   GROUP BY I17_KODAS_PS,N17_KODAS_US  having sum(I17_kiekis - I17_atiduota - i17_rezervas) >0   ORDER BY I17_KODAS_PS			\r\n" + 
            	 		"									" ;

                   if (isDebug()) System.out.println(cmd);


                   StringBuffer products = new StringBuffer();
                   try {

                   connectionUp();
                     Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery(cmd);

                     while (rs.next()) {

                       String kood = rs.getString("toode").trim();
                       double seis = rs.getDouble("stock");
                       if(seis > 0){
                           products.append(kood +  ";");
                           products.append(" ;");
                           products.append(getParent().getNF().format(seis) + "; "  +  "\n"); 
                         }
                       }
                       rs.close();
                   } catch (Exception e) {
                       getParent().showError(e);
                   }
                   return products.toString();
               }



               
            public Vector getFixedAssortmentData() {
           	 Vector lines = new Vector();

               try {
               	  String cmd = "SELECT n51_kodas_ks as custid, N51_KODAS_KT as clgroup, n52_kodas as prodid, 0 AS discount, n52_kaina as price FROM  N08_KLIJ " +
"  join N51_SUTH on n08_kodas_ks=n51_kodas_ks  join N52_SUTD on n51_kodas_kt=n52_kodas_kt " +
" where (N51_KODAS_KT like 'K%' or N51_KODAS_KT like  'FIKTY%' ) and n51_tipas=2 and (n51_poz_date = 1 and(n51_poz_date = 1 and now(*)" +
" between n51_beg_date and n51_end_date))";
               	System.out.println(cmd);
                  	 connectionUp();
              	        Statement stmt = con.createStatement();
              	        ResultSet rs = stmt.executeQuery(cmd);
              	        while(rs.next()){
                                 String custID = rs.getString("custid");
                                 String clGroup = " ";
                                 String prodID = rs.getString("prodid");
                                 String prodGroup = " ";
                                 MatrixLine mline = new MatrixLine();

                                 if (custID != null && custID.trim().length() > 0) {
                                   mline.setCustID(custID.trim());
                                	 mline.setCliGroup(custID.trim());
                                 }
                                 if (clGroup != null && clGroup.trim().length() > 0) {
                                  // mline.setCliGroup(custID.trim());
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

            
            
	public String getCustProdDiscounts(Agent agent) {
		return null;
	}

	
	
	public Vector getPriceMatrixData() {
		Vector lines = new Vector();

		String sql = " SELECT n51_kodas_ks as custid, '' as clgroup, n52_kodas as prodid, 0 AS discount, n52_kaina as price FROM  N08_KLIJ " + 
 " join N51_SUTH on n08_kodas_ks=n51_kodas_ks  join N52_SUTD on n51_kodas_kt=n52_kodas_kt " +
" where (N51_KODAS_KT like 'K%' or N51_KODAS_KT like  'FIKTY%') and n51_tipas=2 and (n51_poz_date = 1 and(n51_poz_date = 1 and now(*)" +
 " between n51_beg_date and n51_end_date)) " ;
		Statement st = null;
		try {
			connectionUp();
			st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {

				//String custID = rs.getString("custid");
				String clGroup = rs.getString("custid");
				String prodID = rs.getString("prodid");
				//String discGroup = rs.getString("alegroup");
				double discount = rs.getDouble("discount");
				double price = rs.getDouble("price");
				MatrixLine mline = new MatrixLine();

				//if (custID != null && custID.trim().length() > 0) {
					//mline.setCustID(custID.trim());
			//	}
				if (mline.getCustID() == null && clGroup != null
						&& clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
				}

				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}
			//	if (mline.getProdID() == null && discGroup != null
				//		&& discGroup.trim().length() > 0) {
				//	mline.setDiscGroup(discGroup);
				//}
				mline.setDiscount(discount);

				if (price != 0) {
					mline.setPrice(price);
				}
				lines.add(mline);
				//System.out.println(discGroup);
			}

			rs.close();
			st.close();
			con.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		System.out.println("Price matrix Suurus "  + lines.size());
		return lines;
	}



	
	

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
     String 
     
     sql = "SELECT i06_kodas_ak as aadressnum,t03_kodas_ks AS klient,T03_DOK_NR AS number ,max(T03_DATA_MOK) AS maksepaev , " + 
            " sum(t03_suma_db-t03_suma_cr) AS tasumata ,max(t03_DATA_DOK) AS kuupaev, max(t03_DATA_MOK-getdate()) AS yle " + 
            " FROM t03_sdok  " +
            " left outer join i06_parh on (t03_dok_nr=i06_dok_nr)                where  t03_kodas_ks != '1077832'  " +
             "    group by i06_kodas_ls_3,i06_kodas_ak,t03_kodas_ks,t03_dok_nr " ;


     
     
     System.out.println("volanimekiri");
      connectionUp();
      PreparedStatement st = con.prepareStatement(sql);
      //st.setString(1, agentCode);
      ResultSet rs = st.executeQuery();
      while(rs.next()){
        Debt debt = new Debt();
        String acc = rs.getString("aadressnum");
        String accountnum = rs.getString("klient").trim();
        debt.setPayerID(accountnum);
        if (acc != null && acc.trim().length()>0){
        	accountnum = accountnum + "-" + acc.trim();
        }
        debt.setCustID(accountnum);
        String dok = rs.getString("number").trim();
        if(dok.length() >5){
        	//dok = dok.substring(3);
        }
        debt.setDoknr(dok);
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

    private TreeMap twoone = new TreeMap();
    private TreeMap teised = new TreeMap();

   
    public TreeMap getProdGroups() throws SQLException {
    	

        connectionUp(url2);

        TreeMap tm = new TreeMap();
             // N35_PAV ->N35_PAV_K3
              String cmd = "   SELECT n35_pav_k3 As esimene , N35_KODAS_LS as kood, N35_PAV as nimi FROM N35_LGST left join " +
              " n17_prod on  n17_kodas_ls_1 = N35_KODAS_LS where n35_tevas='MOBILINK' and  n17_kodas_ls_1<>'' and  N17_POZ_DATE = 0 " +
  			" group by N35_KODAS_LS , N35_PAV,n35_pav_k3";
         try {
          stmt = con.prepareStatement(cmd);
          ResultSet rs = stmt.executeQuery();
         setTreeGroup(true);
        // tm.put("xxxx", "Tester");
          while (rs.next()) {
          	String esimene =Converter.cleanLitLatEst(rs.getString("esimene"));
          	 String liik = Converter.cleanLitLatEst(rs.getString("kood").trim());
          	 String nimetus = rs.getString("nimi");
          	 boolean fir = false;
          	 if(esimene != null && esimene.trim().length()>0){
          		twoone.put(liik, esimene.trim()) ;
             		fir = true;
          	}
           
            String nimetus2 = "";
            if(nimetus.startsWith(" ")){
          	  if (fir){
          		  nimetus2 = "A " + nimetus.trim(); 
          		  nimetus = "A " + esimene.trim();
          		 
          	  }else{
          		  nimetus = "A " + nimetus.trim(); 
          	  }
          	 
            }else if(nimetus.startsWith(".")){
          	  if (fir){
          		  nimetus2 = "B " + nimetus.trim().substring(1);
          		  nimetus = "B " + esimene.trim();
          		
          	  }else{
          		  nimetus = "B " + nimetus.trim().substring(1);
          	  }
          	 
            }else{
          	  if (fir){
          		  nimetus2 = "C " + nimetus.trim();
          		  nimetus = "C " + esimene.trim();
          		
          	  }else{
          		  nimetus = "C " + nimetus.trim();	  
          	  }
          	 
            }
          		
            if (liik != "") {
          	  if (fir){
          		  tm.put(esimene.trim(), Converter.enCodeEst(nimetus.trim()));  
          		  String ess = esimene.trim();
          		  if(ess.length() > 3){
          			  ess = ess.substring(0,3);
          		  }
          		    teised.put(esimene.trim() + "|" +liik.trim(), Converter.enCodeEst(nimetus2.trim()) + " " + ess); 
          	  }else{
          		  tm.put((liik.trim()), Converter.enCodeEst(nimetus.trim()));  
          	  }
            }
          }
        }catch (Exception e) {
          getParent().showError(e);
        }
        System.out.println("tootegrupid1");
        connectionDown();
        TreeMap res = new TreeMap();
        TreeMap res2 = new TreeMap();
        Iterator enume = tm.keySet().iterator();
        int i = 1;
        while (enume.hasNext()){
      	  String aa = i + "";
      	  if(aa.length() == 1 ){
      		  aa = "00" + aa;
      	  }
      	  if(aa.length() == 2 ){
      		  aa = "0" + aa;
      	  }
      	  String kood = (String)enume.next();
      	  String nimi = (String)tm.get(kood);
      	  
      	  Iterator  enume2 = teised.keySet().iterator();
            while (enume2.hasNext()){
         	   String ko =(String) enume2.next();
         	   if(ko.startsWith(kood + "|")){
         		   res2.put(aa + ko.substring(ko.indexOf("|")+1), teised.get(ko));
         		 koodJaGrupp.put( ko.substring(ko.indexOf("|")+1),aa);
         	   }
            }
      	  
      	  koodJaGrupp.put( kood,aa);
      	  res.put(aa, nimi);
      	  i++;
        }  
      	  
      
        teised = res2;
        
      //  System.out.println("grupid " + res.toString());
        //System.out.println("grupid2 " + res2.toString());
        
        return res;
      }

    
    
    public TreeMap getProdGroupsold() throws SQLException {
    	

      connectionUp(url2);

      TreeMap tm = new TreeMap();

            String cmd = "   SELECT n35_pav_k1 As esimene , N35_KODAS_LS as kood, N35_PAV as nimi FROM N35_LGST left join " +
            " n17_prod on  n17_kodas_ls_1 = N35_KODAS_LS where n35_tevas='MOBILINK' and  n17_kodas_ls_1<>'' and  N17_POZ_DATE = 0 " +
			" group by N35_KODAS_LS , N35_PAV,n35_pav_k1";
       try {
        stmt = con.prepareStatement(cmd);
        ResultSet rs = stmt.executeQuery();
       setTreeGroup(true);
      // tm.put("xxxx", "Tester");
        while (rs.next()) {
        	String esimene =Converter.cleanLitLatEst(rs.getString("esimene"));
        	 String liik = Converter.cleanLitLatEst(rs.getString("kood").trim());
        	 String nimetus = rs.getString("nimi");
        	 boolean fir = false;
        	 if(esimene != null && esimene.trim().length()>0){
        		twoone.put(liik, esimene.trim()) ;
           		fir = true;
        	}
         
          String nimetus2 = "";
          if(nimetus.startsWith(" ")){
        	  if (fir){
        		  nimetus2 = "A " + nimetus.trim(); 
        		  nimetus = "A " + esimene.trim();
        		 
        	  }else{
        		  nimetus = "A " + nimetus.trim(); 
        	  }
        	 
          }else if(nimetus.startsWith(".")){
        	  if (fir){
        		  nimetus2 = "B " + nimetus.trim().substring(1);
        		  nimetus = "B " + esimene.trim();
        		
        	  }else{
        		  nimetus = "B " + nimetus.trim().substring(1);
        	  }
        	 
          }else{
        	  if (fir){
        		  nimetus2 = "C " + nimetus.trim();
        		  nimetus = "C " + esimene.trim();
        		
        	  }else{
        		  nimetus = "C " + nimetus.trim();	  
        	  }
        	 
          }
        		
          if (liik != "") {
        	  if (fir){
        		  tm.put(esimene.trim(), Converter.enCodeEst(nimetus.trim()));  
        		    teised.put(esimene.trim() + "|" +liik.trim(), Converter.enCodeEst(nimetus2.trim()) + " " + esimene.trim()); 
        	  }else{
        		  tm.put((liik.trim()), Converter.enCodeEst(nimetus.trim()));  
        	  }
           
          }
        }
      }
      catch (Exception e) {
        getParent().showError(e);
      }
      System.out.println("tootegrupid1");
      connectionDown();
      TreeMap res = new TreeMap();
      TreeMap res2 = new TreeMap();
      Iterator enume = tm.keySet().iterator();
      int i = 1;
      while (enume.hasNext()){
    	  String aa = i + "";
    	  if(aa.length() == 1 ){
    		  aa = "00" + aa;
    	  }
    	  if(aa.length() == 2 ){
    		  aa = "0" + aa;
    	  }
    	  String kood = (String)enume.next();
    	  String nimi = (String)tm.get(kood);
    	  
    	  Iterator  enume2 = teised.keySet().iterator();
          while (enume2.hasNext()){
       	   String ko =(String) enume2.next();
       	   if(ko.startsWith(kood + "|")){
       		   res2.put(aa + ko.substring(ko.indexOf("|")+1), teised.get(ko));
       		 koodJaGrupp.put( ko.substring(ko.indexOf("|")+1),aa);
       	   }
          }
    	  
    	  koodJaGrupp.put( kood,aa);
    	  res.put(aa, nimi);
    	  i++;
      }  
    	  
    
      teised = res2;
      
    //  System.out.println("grupid " + res.toString());
      //System.out.println("grupid2 " + res2.toString());
      
      return res;
    }

    private TreeMap koodJaGrupp = new TreeMap(); 
    public TreeMap getProdGroupsSecond() throws SQLException {
        connectionUp(url2);        
        return teised;
      }

    /**
     *
     */
    public void connectionDown(){
        try {
        	if(con != null){
                con.close();	
        	}
           con = null;
        } catch (Exception e) {
            getParent().showError(e);
        }
    }
    public DealerProduct getDealerProductsList() {
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
    public TreeMap getAgents() {
        TreeMap agents = new TreeMap();
        Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
        
    	   try {
    		   HashMap asendajad = new HashMap();
    		   try{ 
    		   FileReader fr = new FileReader("vacation.txt");
    	          BufferedReader reader = new BufferedReader(fr);
    	        String line = null;
    	     
    	    
    	    	    while((line = reader.readLine()) != null ){
        	         	StringTokenizer st = new StringTokenizer(line, ";");
        	           if (st.hasMoreTokens()) {
        	             String ag1 =	st.nextToken();
        	             String man = st.nextToken().trim();
        	             Vector vv = new Vector();
        	             if(asendajad.get(ag1) != null){
        	            	 vv = (Vector)asendajad.get(ag1);
        	            	 vv.add(man);
        	            	 asendajad.put(ag1, vv);
        	             }else{
        	            	 vv.add(man);
        	            	 asendajad.put(ag1, vv); 
        	              }
        	             }
        	           }  
    	    	    reader.close();
    	       }catch(Exception e){
    	    	 //  e.printStackTrace();
    	       }
    	     
    		   
      
		  connectionUp();
        String  cmd = "SELECT N35_KODAS_LS AS kood ,N35_PAV AS nimi from  N35_LGST " +
             "  WHERE N35_KODAS_LS like 'KAI%'  "+
			 "    ORDER by N35_PAV  ";
    

            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
            	String id = rs.getString("kood").trim();
              Agent agent = new Agent(id);
              String name = rs.getString("nimi").trim() + " " + id;
              if(asendajad != null && asendajad.get(id) != null){
              	Vector vv = (Vector)asendajad.get(id);
              	Enumeration aa = vv.elements();
              	while(aa.hasMoreElements()){
              		agent.addDubbler((String)aa.nextElement());
              	}
              	System.out.println("Asendajad " + agent.getDubbler().toString());
              }
              agent.setName(name);
              agent.setIsPilot(true);
              

              agent.setLanguage("LIT");
              agent.setCountry("lt");
              agent.setPassWord("0000");
String email = " ";
              
              if(propse.get(agent.getID()) != null){
            	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
              }
              
              
              if(email.length() > 3){
            	  agent.setEmail(email);
              }
              if(email.length() > 3){
            	  agent.setActive(true);
              }
              agents.put(name, agent);
              agents1.put(agent.getID(), agent);
            }
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        Agent aa= new Agent("HOUSE");
        aa.setIsPilot(true);
        aa.setName("HOUSE");
        agents.put("HOUSE", aa);
        Agent aai= new Agent("KAI");
        aai.setIsPilot(true);
        aai.setName("KAI");
        agents.put("KAI", aai);
    
        System.out.println("Agents size " + agents1.size() );
        
        try{
      	   Converter.getManagersReplacements(agents1, rcompanyx, user , pass);
      	        	System.out.println("Agents " + agents1 );
      }catch(Exception e){
      	e.printStackTrace();
      }
        
        return agents;
    
    }

    public TreeMap getAgents1() {
		return agents1;
	}
    private TreeMap agents1 = new TreeMap();
    
    public TreeMap getCustomers(Agent agent){
        connectionUp(url2);
        System.out.println("Kliendid");
        
        
        try{
     	   Converter.getManagersReplacements(agents1, rcompanyx, user , pass);
     	        	
     }catch(Exception e){
     	e.printStackTrace();
     }
        String cmd = " SELECT    ltrim(N08_KODAS_KS) AS kood1, (case when (N33_kodas_ak) is null then '1' else ltrim(str(N33_kodas_ak)) end) AS kood, " +    		
        		" rtrim(N08_PAV) + ' ' + rtrim(N33_kodas_ak) AS nimetus, " +
				" rtrim(N33_ADRESAS)  AS aadress, N33_KODAS_LS_3 AS visit ,N08_KODAS_KS AS payer ,N08_IM_KODAS as regnum, " +
				" (CASE WHEN LENGTH(LTRIM(N33_KODAS_LS_1))=0 or N33_KODAS_LS_1 IS NULL THEN  N08_KODAS_LS_1 ELSE  N33_KODAS_LS_1 END) AS agent0 " +
				" ,(CASE WHEN LENGTH(LTRIM(N33_KODAS_LS_4))=0 or N33_KODAS_LS_4 IS NULL THEN  N08_KODAS_LS_4 ELSE  N33_KODAS_LS_4 END) AS agent , " +
				" '' as paymterm , N08_PVM_KODAS as vatno , n08_credit_lim as credit " +
				" FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS  where " +  
				" ( (CASE WHEN LENGTH(LTRIM(N33_KODAS_LS_4))=0 or N33_KODAS_LS_4 IS NULL THEN  N08_KODAS_LS_4 ELSE  N33_KODAS_LS_4 END) like 'KAI%'" +
				" or  (CASE WHEN LENGTH(LTRIM(N33_KODAS_LS_1))=0 or N33_KODAS_LS_1 IS NULL THEN  N08_KODAS_LS_1 ELSE  N33_KODAS_LS_1 END) like 'KAI%') " ;
				
        		log(cmd);
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        HashMap overDebts = new HashMap();
        HashMap usedDebts = new HashMap();
        HashSet allo = new HashSet();
        try {
        	
        	
        	String sql33 = " SELECT n51_kodas_ks as custid, '' as clgroup, n52_kodas as prodid, 0 AS discount, n52_kaina as price FROM  N08_KLIJ " + 
        			 " join N51_SUTH on n08_kodas_ks=n51_kodas_ks  join N52_SUTD on n51_kodas_kt=n52_kodas_kt " +
        			" where (N51_KODAS_KT like 'K%' or N51_KODAS_KT like  'FIKTY%') and n51_tipas=2 and (n51_poz_date = 1 and(n51_poz_date = 1 and now(*)" +
        			 " between n51_beg_date and n51_end_date)) " ;
        					Statement st = null;
        					
        						connectionUp();
        						st = con.createStatement();
        						ResultSet rs = st.executeQuery(sql33);

        						while (rs.next()) {
        							String clGroup = rs.getString("custid").trim();
        							if(!allo.contains(clGroup))
        							allo.add(clGroup);
        						}

        						rs.close();
        							//String custID = rs.getString("custid");
        							
        						//System.out.println("allo " + allo.toString());
        	
        	 try {
        		
        	System.out.println("võlad ");
            String sql =
            " SELECT t03_kodas_ks as klient ,sum(t03_suma_db)-sum(t03_suma_cr)  as summa , " +
" sum(case when t03_data_mok < getdate()  then t03_suma_db else 0 end)-sum(case when t03_data_mok < getdate()  " +
" then t03_suma_cr else 0 end)  as summayle FROM t03_sdok group by t03_kodas_ks ";

            stmt = con.prepareStatement(sql);
             ResultSet	      rs1 = stmt.executeQuery();

            while (rs1.next()) {
              String id = rs1.getString("klient").trim();
              double used = Double.parseDouble(rs1.getString("summa").trim());
              double debt = Double.parseDouble(rs1.getString("summayle").trim());

              overDebts.put(id, debt +"");
              usedDebts.put(id, used + "");
             
            }
            rs1.close();
            
        	 }catch(Exception e){
        		 
        	 }
        	
        	
            stmt = con.prepareStatement(cmd);
            	      rs = stmt.executeQuery();


            	      
            	  
            	      while (rs.next()) {
           
              String accountnum = rs.getString("kood1").trim();
              String fix = accountnum;
              String acc = rs.getString("kood");
              if (acc != null && acc.trim().length()>0){
              	accountnum = accountnum + "-" + acc.trim();
              }
             
              String name = rs.getString("nimetus");
              if(name == null ) name = "";
              Customer cust = new Customer(accountnum);
              cust.setName(Converter.enCodeEst(name.trim()));
              String aadr = rs.getString("aadress");
              if(aadr != null && aadr.trim().length()>0){
                cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim()),"Lithuania"));
              }
              cust.setName(Converter.enCodeEst((name.trim() + " " + cust.getAddress()).trim()));

            
              
              String visit = rs.getString("visit");
              if (visit != null && visit.trim().length()>0){          	  
                cust.setVisit(Converter.getVisitDay(visit.trim()));
              }
              
              
              String payer =rs.getString("payer");
              if(payer != null && payer.trim().length()> 0){
                cust.setPayer(payer.trim());
              }
              if(overDebts.get(cust.getPayer()) != null){
            	  cust.setDebt(cust.getDebt() + Double.parseDouble((String)overDebts.get(cust.getPayer()) ));  
              }
              if(usedDebts.get(cust.getPayer()) != null){
            	//  cust.setUsedCredit(Double.parseDouble((String)usedDebts.get(cust.getPayer()) ));  
              }
             
              
              String regnum =rs.getString("regnum");
              String ag0 = rs.getString("agent0").trim();
         	  cust.addAgent(new Agent(ag0));

        	   if (ag0 != null ){
    				Agent agg = ((Agent)this.getAgents1().get(ag0));
  				
    				if(agg != null && agg.getDubbler().size()>0){
    				//	System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
    					for (int r = 0 ; r < agg.getDubbler().size() ; r++){
    						Agent dub = new Agent((String)agg.getDubbler().elementAt(r));
    						cust.addAgent(dub);
    					}
    				}
    			}
         String ag = rs.getString("agent").trim();
         	  cust.addAgent(new Agent(ag));
         	  
         	 if (ag != null) {
                 cust.setMainAgent( ag.trim());
                 if ( ag != null && agents1.get( ag.trim()) != null){
                              Agent aa = (Agent) agents1.get( ag.trim());
                        
                               Vector vec = aa.getDubbler();
                               if( vec!= null && vec.size() >0){
                                    Enumeration en = vec.elements();
                                    while( en.hasMoreElements()){
                                         String agw = (String) en.nextElement();
                                          cust.addAgent( new Agent( agw));
                                   }
                              }
                        }
                }
         	  
         	  
         	  
         	  
         	  if(ag.indexOf("KAI") != -1 ){
         		
              
  				Enumeration enume = getParent().getAgentsHash().elements();
  	          while (enume.hasMoreElements()) {
  	            Agent a = (Agent) enume.nextElement();
  	            //cust.addAgent(a);
  	          }
  	            //cust.addAgent(new Agent("KAI"));
         	  }
         
              try{
                 cust.setPaymTerm(rs.getInt("paymterm")+"");
                }catch(Exception e){
              }
                String vatno = parent.make(rs.getString("vatno"));
                cust.setVatnr(vatno);
                if(regnum != null && regnum.trim().length()> 0){
                    cust.setVatnr(regnum.trim());
                  }
                String credit = rs.getString("credit");
                if(credit != null && credit.trim().length()>0 && !credit.trim().equals("0")){
                	try{
                		int cre = Integer.parseInt(credit.trim());
                		if(cre > 0){
                		 	cust.setCredit(cre + "");
                       }
                   }catch(Exception o){
                		
                	}
                
                }
         
         
                
           /*     String grupp = rs.getString("grupp");
                if(grupp != null && grupp.trim().length()>0){
                	cust.setGroup(grupp.trim());
                }
                
                String city = rs.getString("city");
                if(city != null && city.trim().length()>0){
                //	cust.setCity(city.trim());
                }
                
                String custType = rs.getString("custType");
                if(custType != null && custType.trim().length()>0){
                	cust.setCustType(custType.trim());
                	//System.out.println(cust.getID() +" " + cust.getCustType());
                    
                }
             */   
               // System.out.println(cust.getID() +" '" + fix + "' " + allo.contains(fix));
              //  if(allo.contains(fix)){
                	cust.addFixSortCode(fix);
                    customers.put(name, cust);
                    custMap.put(cust.getID(), cust);	
                //}
                
            }

            stmt.close();
            con.close();
          }
          catch (Exception e) {
            getParent().showError(e);
          }

          return customers;
    }
    
      HashSet lubatud = new HashSet();
  
      
      public TreeMap getProducts(Agent agent){

    	     HashSet codes = new HashSet();
    	        TreeMap products = new TreeMap();
    	    	
    	    	  try {
    	         
    	    	   String  sql = "SELECT  I17_KODAS_PS AS toode, sum(I17_kiekis - I17_atiduota - i17_rezervas) AS STOCK"
    	    	   		+ ",avg(I17_P_PIR_K) as cost  from i17_vpro inner join n17_prod on  I17_KODAS_PS = N17_KODAS_PS " + 
    	 " where I17_KODAS_IS in ('KS000001','KS000005','KS000002','KS000003','KS000080') AND ((I17_kiekis - I17_atiduota - i17_rezervas) >0 OR (I17_P_PAR_K)>0) " +
    	 " and n17_kodas_ls_1<>''  and  N17_POZ_DATE =0 " +
    		"		    GROUP BY I17_KODAS_PS  having sum(I17_kiekis - I17_atiduota - i17_rezervas) >0   ORDER BY I17_KODAS_PS" ;
    	    	
    	           System.out.println("laoseisud");
    	         if (isDebug()) System.out.println(sql);
    	         connectionUp(url2);
    	         TreeMap tooted = new TreeMap();
    	            Statement stm = con.createStatement();
    	            ResultSet rst = stm.executeQuery(sql);

    	              while (rst.next()) {
    	                String toode = rst.getString("toode").trim();
    	                int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
    	                double cost = rst.getDouble("cost");
    	               	 Toode tt = new Toode(toode);
    	                    tt.setKogus(seis);   
    	                    if(cost > 0){
    	                    	tt.setDonecur(cost);
    	                    }
    	                    tooted.put(toode,tt);
    	                  
    	            }
    	            rst.close();
    	            stm.close();

    	            
    	            String sss = "SELECT kodas as prod , galioja_iki as valid_until, kiekis as quantity  FROM dbo.v5_SKU_ShelfLife where " +
    		               " galioja_iki -60 < getdate() and year(galioja_iki) != 1900";
    	            
    	            stm = con.createStatement();
    	             rst = stm.executeQuery(sss);
    	             TreeMap kriitil = new TreeMap();
    	              while (rst.next()) {
    	                String toode = rst.getString("prod").trim();
    	                String kuup = rst.getString("valid_until").trim().substring(0,10);
    	                int seis = (int)Double.parseDouble(rst.getString("quantity").replace(',','.'));
    	               	 Toode tt = new Toode(toode);
    	                    tt.setKogus(seis);  
    	                    tt.setLadu(kuup);
    	                    kriitil.put(toode,tt);	
    	            }
    	            rst.close();
    	            stm.close();
    	            
    	            
    	            
    	            String ggg = "	SELECT l11_kodas1 as toode, f_pap(L11_laukas,3,'c') as starta, f_pap(L11_laukas,4,'c') as dend  " +
    	            		" FROM dbo.L11_PAP1 where L11_kodas_nr = 'PSn17' and length(trim(f_pap(L11_laukas,3,'c')))> 9 and length(trim(f_pap(L11_laukas,4,'c'))) >9";
    	            
    	            stm = con.createStatement();
    	             rst = stm.executeQuery(ggg);
    	             HashSet soodus = new HashSet();
    	              while (rst.next()) {
    	                String toode = rst.getString("toode").trim();
    	                String algus = rst.getString("starta").trim();
    	                String lopp = rst.getString("dend").trim();
    	                
    	                //return ; 

    	                Date min = null;
    	                Date max = null;
    	                if(algus.length() > 8){
    	                	min = getDate(algus);
    	                }
    	                if(lopp.length() > 8){
    	                	max = getDate(lopp);
    	                }
    	               
    	                Date d = new Date();
    	                if(d.compareTo(min) >= 0 && d.compareTo(max) <= 0){
    	                	   soodus.add(toode);	
    	                	  // System.out.println("Sooduses " + toode);
    	                }
    	                 

    	            }
    	            rst.close();
    	            stm.close();

    	           // System.out.println("Soodused " + soodus.toString());
    	            
    	            String bc = "select N37_KODAS_PS as prod ,N37_BAR_KODAS as ean  from n37_pmat where N37_BAR_KODAS != N37_KODAS_PS " +
    	            		"and N37_BAR_KODAS!='000000000000' and '0'+ltrim(N37_KODAS_PS)!=N37_BAR_KODAS and '00'+ltrim(N37_KODAS_PS)!=N37_BAR_KODAS";
    	            
    	            stm = con.createStatement();
    	             rst = stm.executeQuery(bc);
    	             HashMap bcodes = new HashMap();
    	              while (rst.next()) {
    	                String toode = rst.getString("prod").trim();
    	                String bar = rst.getString("ean").trim();        
    	                bcodes.put(toode,bar);	
    	            }
    	            rst.close();
    	            stm.close();
    	            
    	  TreeMap prices = new TreeMap();
    	  
    	  String sql2 = "SELECT max(n32_g_date),n32_kodas_ps as toode,max(rtrim(n32_kodas_us)) as yhik ,n32_tipas,(n32_kaina1) as price , max(n32_kiekis2) as alekogus, "  +
    	  		" max(n32_kaina2) as alehind FROM dbo.N32_PABC " +
    	  		" where n32_g_date < now(*) and n32_tipas in('E','A')  group by  n32_kodas_ps,n32_tipas,n32_kaina1 order by n32_kodas_ps,max(n32_g_date) ";
    	  

    	if (isDebug()) System.out.println(sql2);
    	connectionUp(url2);
    	   Statement stm1 = con.createStatement();
    	   ResultSet rst1 = stm1.executeQuery(sql2);

    	     while (rst1.next()) {
    	         String toode = rst1.getString("toode").trim();
    	  
    	         String yhik = rst1.getString("yhik");
    	         double price = rst1.getDouble("price");
    	         double pallet =  rst1.getDouble("alekogus");
    	         double aleprice = rst1.getDouble("alehind");
    	        
    	        	 Toode tt = new Toode(toode);
    	             tt.setKastis(price/1.21);                  
    	             tt.setLadu(yhik);
    	             tt.setGr1(pallet + "");
    	             tt.setGr2(aleprice + "");
    	             if(prices.get("toode") == null)
    	             prices.put(toode,tt);
    	             
    	             if(toode.startsWith("599954089229")) {
              			 System.out.println("Laoseis 00dd '599954089229' " + " "+toode + " " + yhik);
              		 }
    	      }    
    	   
    	   rst1.close();
    	   stm1.close();
    	    	
    	    	

    		
    	   
    	        connectionUp(url2);
    	 
    	        this.getProdGroups();
    	       String cmd = "SELECT N17_KODAS_PS AS toode,trim(N17_PAV) AS nimetus,N27_KIEKIS_PAK  AS tykke,  " +
    	 "   n17_kodas_ls_1 AS grupp ,N17_MEN_PAV AS minkogus , " +
    	 "   N17_KODAS_LS_4 AS status , N17_MAX_NUOL as maxa ,( select top 1 N27_KIEKIS_PAK from N27_PAK a where left(a.N27_KODAS_US_PAK,7)='SANDPAL' and " +
    	 " a.N27_KODAS_PS = N17_KODAS_PS) as alusel,( select top 1 N27_KIEKIS_PAK from N27_PAK a where left(a.N27_KODAS_US_PAK,8)='SANDPICK' and " +
    	 " a.N27_KODAS_PS = N17_KODAS_PS)  as optimum ,a.N17_KODAS_KS_G as tarnija, k.N08_PAV as tarnijanimi " +
    	 "   FROM n17_prod a   left join N27_PAK on  trim(N17_KODAS_PS)=trim(N27_KODAS_PS) and left(N27_KODAS_US_PAK,7)='SANDPAK' " +
    	 "  left join n08_klij k on k.n08_kodas_ks= a.N17_KODAS_KS_G where  n17_kodas_ls_1<>'' and n17_kodas_ls_1 !='GAMYBA'  and  N17_POZ_DATE =0 and " +
		 "  (n17_kodas_ls_1 in ('PADAŽAI8','PADAŽAI9','MARGARINAS','ACTAS_EU','ACTAS_LT','ALIEJUS','ALIEJUS_ALYV','ALYVUOGĖS' " +
		 "  ,'MILTAI','PRIESKONIAI','PRYMAT') or " +
		 "   n17_kodas_ls_1  like ('FAS%') or n17_kodas_ls_1  like ('CUK%')or n17_kodas_ls_1  like ('MAIST_%') or"
		 + " n17_kodas_ls_1  like ('KON%') or n17_kodas_ls_1  like ('PAD_%')) and  n17_kodas_ls_1 != 'KONDITERIJA1' "
		 + "and n17_kodas_ls_1 != 'KONDITERIJA' " +
    	 "  order by N17_KODAS_PS ";   
    	   
    	 
    	       
    	   if (isDebug()) {
    	            System.out.println(cmd);
    	        }
    	        System.out.println("tooted");
    	   //     System.out.println("laos " + tooted.size() + " 599954089665 '" + tooted.get("599954089665")+"'");
    	      
    	        Vector done = new Vector(); 
    	        
    	        connectionUp(url2);
    	            stmt = con.prepareStatement(cmd);
    	            ResultSet	      rs = stmt.executeQuery();

    	            
    	            
    	            while (rs.next()) {
    	              String kood = rs.getString("toode").trim();
    	              Product prod = new Product(kood);
    	              prod.setStock(0);
    	              
    	              
    	              
    	              boolean goes = true;
    	           
    	               String nimetus = rs.getString("nimetus").trim();
    	               if(nimetus.startsWith("*") || nimetus.startsWith("#")){
    	             	  goes = false;
    	               }
    	              
    	              double kogus = 0;
    	              double pr = 0;
    	           
    	           
    	              Toode tt = (Toode) prices.get(kood);
    	              
    	              if(kood.startsWith("599954089229")) {
              			 System.out.println("Laoseis 00 '599954089229' " + " "+kood + " " + tt);
              		 }
    	              String add = "";
    	              if(tt != null){
    	              	kogus = tt.getKogus();
    	              	 if(kogus!= 0){
    	                  //	prod.setStock(kogus);
    	                  }
    	              	 
    	              	pr = tt.getKastis(); 
    	              	if(pr!= 0){
    	                  	prod.setPrice(pr);
    	                }
    	              	String yhik = tt.getLadu();

    	                if(yhik != null && yhik.trim().length()>0){
    	                	prod.setUnit(yhik.trim());
    	                }
    	                
    	           
    	                
    	                Toode ttt = (Toode) kriitil.get(kood);
    	                
    	                if(ttt != null){
    	                	
    	                	String kog  = ttt.getKogus() + "";
    	                	 if(prod.getUnit().equals("KG") || prod.getUnit().equals("M")){
    	                		 kog = ((int)(ttt.getKogus() /1000)) + "";
    	               	  }
    	                
    	                	 
    	                	
    	                	String kuup = ttt.getLadu();

    	                  if(kuup != null && kuup.trim().length()>0){
    	                  	prod.setPartii(kuup + " " + kog);
    	                  	prod.setDiffColor(true);
    	                  	}
    	              
    	                }
    	                
    	                // tt.setGr1(pallet + "");
    	              //  tt.setGr2(aleprice + "");
    	                 int pallet = 0;
    	                 try{
    	                	 if(prod.getUnit().equals("KG") || prod.getUnit().equals("M")){
    	                		 pallet = pallet /1000;
    	               	  }
    	                	 pallet = Integer.parseInt(tt.getGr1());
    	                 }catch(Exception r){
    	                	 
    	                 }
    	                 if(pallet > 1){
    	                	 prod.setOnPallet(pallet);
    	                 }
    	            
    	              	double palprice = 0;
    	                try{
    	                	palprice = Double.parseDouble(tt.getGr2());
    	                }catch(Exception r){
    	               	 
    	                }
    	                if(palprice > 0){
    	               	 prod.setPartii( " %k" + palprice + "");
    	                }
    	             	//prices.remove(kood);
    	              }
    	              
    	              
    	              
  	                Toode t = (Toode) tooted.get(kood);
  	                if(kood.startsWith("59995408966")) {
             			 System.out.println("Laoseis 00 '599954089665' " + " "+kood);
             		 }
  	                
  	                if(t != null){
  	                	kogus = t.getKogus();
  	                	
  	                	
  	                	 if(kogus!= 0){
  	                		 if(prod.getUnit().equals("KG") || prod.getUnit().equals("M") ){
  	                			 kogus = kogus /1000;
  	                		 }
  	                    
  	                		 if(kogus > 50000){
  	                			 kogus = 55555;
  	                		 }
  	                		 if(prod.getID().equals("599954089665")) {
  	                			 System.out.println("Laoseis '599954089665' " + kogus);
  	                		 }
  	                		 prod.setStock(kogus);
  	                    }
  	                	 
  	                	  double cost =  t.getDonecur();
 	                         if(cost >0){
 	                        	 double marg = (1-cost/prod.getPrice())*100 ;
 	                         	 if(marg >0){
 	                         	 	 prod.setMargin((int) marg + "");
 	                             }   
 	                         }
  	                }
  	                
    	              
    	            String lopp = prod.getID();
    	            if(lopp.length() > 4){
    	            	nimetus  = nimetus  + " " + lopp.substring(lopp.length() - 3 ) + " ";
    	            }
    	            String aleke = "";
    	            if(soodus.contains(prod.getID())){
    	            	
    	            	prod.setStatusDiscount("1");
    	            }
    	              prod.setName(Converter.enCodeEst(aleke + nimetus + add));
    	          
    	             
    	              
    	              try{
    	            	  double tykke = rs.getDouble("tykke");
    	            	  if(prod.getUnit().equals("KG") || prod.getUnit().equals("M")){
    	            		  tykke = tykke /1000;
    	            	  }
    	            	  
    	              prod.setInCases(tykke);
    	            } catch (Exception e){}
    	            if(prod.getPrice() > 0){
    	            }else{
    	            	prod.setPrice(1);     	
    	            }
    	   
    	              String grupp = Converter.cleanLitLatEst(rs.getString("grupp"));
    	             
    	             
    	            //  String grupp2 = rs.getString("grupp2");
    	                    
    	               String minam = rs.getString("minkogus");
    	               
    	              prod.setVat("21");
    	            	   try{
    	            		   double alusel =  rs.getDouble("alusel");
    	                       if(alusel >0){
    	                    	   if(prod.getUnit().equals("KG")|| prod.getUnit().equals("M")){
    	                    		   alusel = alusel /1000;
    	                     	  }
    	                    	prod.setOnPallet((int)alusel);   
    	                       }
    	                     }catch(Exception e){
    	                   	  
    	                     }
    	                     
    	                     try{
    	              		   double optimum =  rs.getDouble("optimum");
    	                         if(optimum >0){
    	                        	 if(prod.getUnit().equals("KG")|| prod.getUnit().equals("M")){
    	                        		 optimum = optimum /1000;
    	                       	  }
    	                      	prod.setOptAmountInUnits(optimum);   
    	                         }
    	                       }catch(Exception e){
    	                     	  
    	                       }  
    	                     
    	                     
    	                     String tarnija = rs.getString("tarnijanimi");
    	                     if(tarnija != null && tarnija.trim().length() > 0){
    	                    	 prod.setType(Converter.cleanLitLatEst(tarnija.trim()));
    	                     }
    	                     
    	                     
    	                     
    	                     
    	                if(grupp != null && grupp.trim().length()>0 ){
    	                	
    	                	if(twoone.get(grupp.trim()) != null){
    	                		String ad = "";
    	                		if(  koodJaGrupp.get(grupp.trim()) != null){
    	                    		ad = (String)koodJaGrupp.get(grupp.trim());
    	                    		 prod.setGroup(ad);
    	                    		 prod.setGroupSecond(ad + grupp.trim());
    	                    	}else{
    	                    		 prod.setGroup((String) twoone.get(grupp.trim()));
    	                    		 prod.setGroupSecond( grupp.trim());
    	                    	}
    	                	}else{
    	                		 prod.setGroup(grupp.trim());
    	                	}
    	                 }
    	                
    	                
    	              if(bcodes.get(prod.getID()) != null){
    	            	  prod.setEAN((String)bcodes.get(prod.getID()));
    	              }else{
    	            	  try{
    	                  	  EAN13CheckDigit ee = new EAN13CheckDigit();
    	                        prod.setEAN(prod.getID().trim() + ee.calculate(prod.getID().trim()));
    	                     
    	                    }catch(Exception e){
    	                  	  
    	                    }  
    	              }
    	                
    	           
    	               String idd = prod.getID();
    	               if(idd.length()>8){
    	               	idd = idd.substring(idd.length()-8,idd.length());
    	               }
    	               if(goes){
    	            	   lubatud.add(prod.getID());
    	            	   products.put(nimetus, prod);
    	                   done.addElement(prod.getID());
    	                   codes.add(prod.getID()) ;            
    	               }
    	               
    	            }     
    	        } catch (Exception e) {
    	            getParent().showError(e);
    	        }
    	    	  if(rcompany != null){
    	    			// Converter.sendProductJson(products, rcompany);
    	    		    }
    	    	  
    	    	  
    	    	String  cmd = " SELECT  n32_kodas_ps as toode ,n32_kaina2 as price2, " +
    	"n32_kiekis2 as quantity2,n32_kaina3 as price3,n32_kiekis3 as quantity3, " +
    	"n32_kaina4 as price4, n32_kiekis4 as quantity4 ,n32_g_date as algus FROM N32_PABC s where  N32_tipas = 'E' " +
    	"and n32_g_date =( select max (n32_g_date) FROM N32_PABC where n32_kodas_ps = s.n32_kodas_ps and N32_tipas = 'E') " +
    	"and n32_kiekis2 > 0 and n32_g_date  > getdate()-300 order by n32_kodas_ps , n32_g_date desc";
    	    				
    	    			    StringBuffer buf = new StringBuffer();
    	    				try {
    	    					
    	    				    Statement stmt =  con.createStatement();
    	    					ResultSet	      rs = stmt.executeQuery(cmd);

    	    			           while(rs.next()){
    	    			                  	String  toodex = rs.getString("toode").trim();
    	    			                  	if(codes.contains(toodex)){
    	    			                  	
    	    			                  	double  hind2 = rs.getDouble("price2")/1.21;
    	    			                  	double  kogus2 = rs.getDouble("quantity2");
    	    			                  	      buf.append(toodex+ "; ; ;" + hind2 + "; ;" + kogus2 + ";"  );
    	    			                          buf.append("\n");	
    	    			                      	double  hind3 = rs.getDouble("price3");
    	        			                  	double  kogus3 = rs.getDouble("quantity3");
    	        			                  	if(hind3 > 0 && kogus3 >0){
    	        			                  	  buf.append(toodex+ "; ; ;" + hind3/1.21 + "; ;" + kogus3 + ";"  );
    	    			                          buf.append("\n");	
    	        			                  	}
    	        			                  	
    	        			                  	double  hind4 = rs.getDouble("price4");
    	        			                  	double  kogus4 = rs.getDouble("quantity4");
    	        			                  	if(hind4 > 0 && kogus4 >0){
    	        			                  	  buf.append(toodex+ "; ; ;" + hind4/1.21 + "; ;" + kogus4 + ";"  );
    	    			                          buf.append("\n");	
    	        			                  	}
    	        			                	
    	    			                  	}
    	    			              }	
    	    			           rs.close();
    	    			           stmt.close();
    	    			           con.close();
    	    				}catch (Exception e ){
    	    					e.printStackTrace();
    	    				}
    	    				
    	    	  
    	    	  
    	    	  
    	    	  
    	    	  

    	    	    if(buf.length() > 2){
    	    	        File f = new File("prodactions.txt");
    	    	        if (f.exists())f.delete();
    	    	          try {
    	    	    		PrintWriter os = new PrintWriter(new FileWriter(
    	    	    				 "prodactions.txt", true));
    	    	    		os.write(buf.toString());
    	    	    		os.close();
    	    	    	  }catch (IOException ex) {
    	    	    		ex.printStackTrace();
    	    	    	  }
    	    	     }else{
    	    	        File f = new File("prodactions.txt");
    	    	        if (f.exists())f.delete();
    	    	          try {
    	    	    		PrintWriter os = new PrintWriter(new FileWriter(
    	    	    				 "prodactions.txt", true));
    	    	    		os.write("");
    	    	    		os.close();
    	    	    	  }catch (IOException ex) {
    	    	    		ex.printStackTrace();
    	    	    	  }
    	    	     }
    	    	  
    	    	  
    	    	  
    	        return products; 
    	    }
  

      private Date getDate(String value){
          Calendar cal = Calendar.getInstance();
          cal.set(Calendar.YEAR, Integer.parseInt(value.substring(0, 4)));
          cal.set(Calendar.MONTH, Integer.parseInt(value.substring(5, 7))-1);
          cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(value.substring(8, 10)));
          return new Date(cal.getTime().getTime());
        }
        
      
      
        public static String deCode(String res ){

      	return res;
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
        nfvat.setMaximumFractionDigits(2);
        nfvat.setMinimumFractionDigits(2);
        nfvat.setGroupingUsed(false);
        
        String cmd ="SELECT ltrim(I06_KODAS_KS)  AS klient,(case when (N33_kodas_ak) is null then '1' else ltrim(str(N33_kodas_ak)) end) as kood, " +
                 " I07_KODAS AS toode,sum(I07_KIEKIS/I07_FRAKCIJA)AS kogus,  " +
                  "       I06_DOK_DATA AS kuupaev ,max(I07_SUMA/case when I07_KIEKIS =0 then 1 else I07_KIEKIS/I07_FRAKCIJA  end ) as lastprice " +
                  " FROM i06_parh  force index (dok_data)" +
			    " inner join  n08_klij on  I06_KODAS_KS = N08_KODAS_KS " +
                 "       left JOIN N33_KBAN ON N33_KODAS_KS = I06_KODAS_KS AND I06_kodas_ak=N33_kodas_ak  " +
                 "   left outer join i07_pard force index (kodas) ON I07_KODAS_PO=I06_KODAS_PO " +
			"	left join n17_prod on I07_KODAS = N17_KODAS_PS " +
             "            WHERE I06_DOK_DATA > now(*) -120 and n17_kodas_ls_1<>'' and n17_kodas_ls_1 !='GAMYBA' and " +
				"		 ((CASE WHEN LENGTH(LTRIM(N33_KODAS_LS_1))=0 or N33_KODAS_LS_1 IS NULL THEN  N08_KODAS_LS_1 ELSE  N33_KODAS_LS_1 END) like 'KAI%'" +
				"		 or" +
				"		 (CASE WHEN LENGTH(LTRIM(N33_KODAS_LS_4))=0 or N33_KODAS_LS_4 IS NULL THEN  N08_KODAS_LS_4 ELSE  N33_KODAS_LS_4 END) like 'KAI%') " +
				"		  and (I07_KODAS) is not null" +
                 "       group by I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ltrim(str(N33_kodas_ak))end),I07_KODAS,I06_DOK_DATA  " +
				"	 order by I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ltrim(str(N33_kodas_ak))end),I07_KODAS,I06_DOK_DATA ";   
    	   
        if (isDebug()) {
           System.out.println(cmd);
       }

       Vector custProds = new Vector();
       HashMap kliendid = new HashMap();

Calendar cal = Calendar.getInstance();
cal.add(Calendar.MONTH, - 3);
       try {
          stmt = con.prepareStatement(cmd);
        //  stmt.setTimestamp(1, new Timestamp(cal.getTimeInMillis()));
           ResultSet	      rs = stmt.executeQuery();

           while (rs.next()) {
             String kliendikood = rs.getString("klient").trim();
             String kood = rs.getString("kood");
             if (kood != null && kood.trim().length()> 0){
             	kliendikood = kliendikood +  "-" + kood.trim();
             }
            
             String tootekood = rs.getString("toode");
             if(tootekood != null && lubatud.contains(tootekood.trim())){
            	// System.out.println("CP olemas" + tootekood  + " " + kliendikood);
            	 tootekood = tootekood.trim();
             
             double kogus = Double.parseDouble(rs.getString("kogus").trim());
             String kuup = rs.getString("kuupaev").substring(5,10).trim();
             double lastprice = rs.getDouble("lastprice")*1.21;
             if(lastprice >0 ){
            	 kuup = kuup + " (" + nfvat.format(lastprice) + "€.)"; 
             }
              
             Customer cust = (Customer) kliendid.get(kliendikood);

             if (cust == null) {
               cust = new Customer(kliendikood);
             }
        	
             Product prod = new Product(tootekood);
             cust.addProduct(prod,
                             new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                          kuup, 0, 0,0," "),false);
             kliendid.put(kliendikood, cust);
             custProds.add(new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                          kuup, 0, 0,0," "));
             }
           
           }
           rs.close();
           } catch (Exception e) {
           getParent().showError(e);
       }
       connectionDown();
       return custProds;
    }

    private NumberFormat nfvat = NumberFormat.getInstance(Locale.ENGLISH);
    
    public String getPilotDayData(Agent agent){

 	   	

        StringBuffer sb = new StringBuffer();
        try {
        	
        	
 	   	
       File dir = new File(getParent().getProps().getProperty("pop3.dir"));
      
       if(dir.exists() && dir.isDirectory()){
    	   String [] files = dir.list();  
    	   if(files.length>0){
    		   for (int i = 0;i<files.length;i++){
        		  
        		   if (files[i].toLowerCase().indexOf(agent.getID().toLowerCase())!= -1 && files[i].toLowerCase().endsWith("log")){
        			  // System.out.println(files[i]);  
        			   File f = new File(getParent().getProps().getProperty("pop3.dir") + (files[i].substring(0,files[i].indexOf(".")) + ".LOG").toUpperCase());
        			   //System.out.println(f.getAbsolutePath());  
        			   if(f.exists()){
        				   FileReader fr = new FileReader(f.getAbsoluteFile());
        				  
        				   BufferedReader reader = new BufferedReader(fr);
        			        String line = " ";
        			        
        			      while( ((line = reader.readLine())!= null )){
        			    	//  System.out.println(line); 
        			    	  if(line.trim().startsWith("Sufor")){
        			    		  sb.append(line.substring(line.indexOf("Klientas")+9) + "\n");
        			    	  }
        			    	  if(line.trim().startsWith("*Klaida")){
        			    		  sb.append("   " +line.substring(line.indexOf("Klaida")+8) + "\n");
        			    	  }
        			    	  
        			      }
        				   
        				   
        				   
        				   
        			   }
        			   
        		   }     		   
    		   }
    	   }
       }
        
                        
                        } catch (Exception e) {
                            getParent().showError(e);
                        }
                   //     System.out.println(agent.getID() + sb.toString());
           if(sb.toString().trim().length()>0){
        	  // return null;
            return sb.toString();
           }else {
           	return null;
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

    private String getCurEnd(int minus ){
    		    StringBuffer sb = new StringBuffer();
    		    Calendar cal= Calendar.getInstance();
    		    cal.add(Calendar.YEAR, -minus );
    		
    		    String year =  cal.get(Calendar.YEAR) + "";
    		    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    		    String month = String.valueOf(cal.get(Calendar.MONTH) + 1 );
    		    
    		    sb.append(year);
    		     if (month.length() == 1){
    		      sb.append("0");
    		    }
    		     sb.append(month);
    		  
    		    if (day.length() == 1){
    		     sb.append("0");
    		   }
    		     sb.append(day);
    		        
    		return sb.toString();

    }
private String getCurStart(int minus ){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    cal.add(Calendar.YEAR, -minus );
		    cal.set(Calendar.DAY_OF_MONTH, 1);
		
		    String year =   cal.get(Calendar.YEAR) + "";
		    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		    String month = String.valueOf(cal.get(Calendar.MONTH) + 1 );
		    
		    sb.append(year);
		     if (month.length() == 1){
		      sb.append("0");
		    }
		     sb.append(month);
		  
		    if (day.length() == 1){
		     sb.append("0");
		   }
		     sb.append(day);
		        
		return sb.toString();

}

    public void setBulkSales(Map agents){
    	 connectionUp(url2);
  
    	 
       String cmd = " SELECT i06_kodaS_ls_3 as agent, SUM(i07_pard.i07_suma*SIGN( 52-i06_parh.i06_op_tip)) AS suma_p," +
       		" SUM(i07_pard.i07_suma*SIGN( i06_parh.i06_op_tip-51)) AS suma_v, " +
" SUM(i07_pard.i07_suma*SIGN( 103-(i06_parh.i06_op_tip*2))) AS suma_all," +
"SUM((i07_pard.i07_suma*SIGN(103-(i06_parh.i06_op_tip*2)))+(i07_pard.i07_savikaina*SIGN(1+SIGN(1-i07_pard.i07_tipas)))) AS dohod,  " +
" SUM(i07_pard.i07_savikaina*SIGN(1+SIGN(1-i07_pard.i07_tipas))) AS savikaina," +
" SUM((CASE trim(i07_pard.i07_kodas_us)WHEN 'KG' THEN (i07_pard.i07_kiekis/i07_pard.i07_frakcija)*(n37_brutto/n37_frakcija)  " +
" ELSE (i07_pard.i07_kiekis/i07_pard.i07_frakcija)*(n37_brutto/n37_frakcija)/1000 END)*SIGN( 103-(i06_parh.i06_op_tip*2))) AS ves_kg,  " +
"  sum (i07_pard.i07_suma-i07_pard.i07_suma+1) AS EIL   " +
"  FROM i06_parh force INDEX (op_data),i07_pard force INDEX (kodas),n08_klij,n37_pmat,n17_prod  " +
"   where i06_parh.i06_kodas_po = i07_pard.i07_kodas_po AND n08_klij.n08_kodas_ks = i06_kodas_ks AND i06_kodas_ls_3<>'' and i07_kodas=n17_kodas_ps  " +
"   and  ((i07_kodas between '000000000000' and '999999999999') or (i07_kodas='NUOLAIDA' or i07_kodas='Z1' or i07_kodas='Z2'))  " +
"    and i07_pard.i07_kodas=n37_pmat.n37_kodas_ps  " +
"   and i07_pard.i07_kodas_us=n37_pmat.n37_kodas_us AND i06_parh.i06_op_data BETWEEN '" + getCurStart(0) +"' AND '" + getCurEnd(0) +"'  " +
"   AND ( I06_parh.i06_op_tip = 51 or I06_parh.i06_op_tip = 52)  " +
"   GROUP BY i06_kodaS_ls_3  " +
" order by i06_kodas_ls_3";
       
       String cmd2 = " SELECT i06_kodaS_ls_3 as agent, SUM(i07_pard.i07_suma*SIGN( 52-i06_parh.i06_op_tip)) AS suma_p," +
  		" SUM(i07_pard.i07_suma*SIGN( i06_parh.i06_op_tip-51)) AS suma_v, " +
" SUM(i07_pard.i07_suma*SIGN( 103-(i06_parh.i06_op_tip*2))) AS suma_all," +
"SUM((i07_pard.i07_suma*SIGN(103-(i06_parh.i06_op_tip*2)))+(i07_pard.i07_savikaina*SIGN(1+SIGN(1-i07_pard.i07_tipas)))) AS dohod,  " +
" SUM(i07_pard.i07_savikaina*SIGN(1+SIGN(1-i07_pard.i07_tipas))) AS savikaina," +
" SUM((CASE trim(i07_pard.i07_kodas_us)WHEN 'KG' THEN (i07_pard.i07_kiekis/i07_pard.i07_frakcija)*(n37_brutto/n37_frakcija)  " +
" ELSE (i07_pard.i07_kiekis/i07_pard.i07_frakcija)*(n37_brutto/n37_frakcija)/1000 END)*SIGN( 103-(i06_parh.i06_op_tip*2))) AS ves_kg,  " +
"  sum (i07_pard.i07_suma-i07_pard.i07_suma+1) AS EIL   " +
"  FROM i06_parh force INDEX (op_data),i07_pard force INDEX (kodas),n08_klij,n37_pmat,n17_prod  " +
"   where i06_parh.i06_kodas_po = i07_pard.i07_kodas_po AND n08_klij.n08_kodas_ks = i06_kodas_ks AND i06_kodas_ls_3<>'' and i07_kodas=n17_kodas_ps  " +
"   and  ((i07_kodas between '000000000000' and '999999999999') or (i07_kodas='NUOLAIDA' or i07_kodas='Z1' or i07_kodas='Z2'))  " +
"    and i07_pard.i07_kodas=n37_pmat.n37_kodas_ps  " +
"   and i07_pard.i07_kodas_us=n37_pmat.n37_kodas_us AND i06_parh.i06_op_data BETWEEN '" + getCurStart(1) +"' AND '" + getCurEnd(1) +"'  " +
"   AND ( I06_parh.i06_op_tip = 51 or I06_parh.i06_op_tip = 52)  " +
"   GROUP BY i06_kodaS_ls_3  " +
" order by i06_kodas_ls_3";
    
       log(cmd);
       System.out.println("myygid" + cmd);
       TreeMap rrr = new TreeMap();
         try {
           stmt = con.prepareStatement(cmd);

             ResultSet	      rs = stmt.executeQuery();
             while(rs.next()) {
               String agentID = rs.getString("agent").trim();
                  String asi = getCurStart(0).substring(0,6) + "| Pardavimas " + rs.getInt("suma_p") + "."; 
            	   asi = asi +                                  "| Pelnas       " + rs.getInt("dohod") + ".";
            	   asi = asi +                                  "| Kg.           " + rs.getInt("ves_kg");
            	  rrr.put(agentID, asi);
            	  Agent agent = (Agent)agents.get(agentID);
                  if (agent != null){
                	   agent.setStartText(asi);
                           	  
                  }
               
             }
             rs.close();
             
             System.out.println(rrr.size() + "suurus");
             stmt = con.prepareStatement(cmd2);  
   	      rs = stmt.executeQuery();
          while(rs.next()) {
            String agentID = rs.getString("agent").trim();
            Agent agent = (Agent)agents.get(agentID);
            if (agent != null){
            
            	String sta = "";
            	if(rrr.get(agentID) != null){
            		sta = rrr.get(agentID) + "||" ;
            	}
         	   String asi = sta + getCurStart(1).substring(0,6) + "| Pardavimas " + rs.getInt("suma_p") + "."; 
         	   asi = asi +                                  "| Pelnas       " + rs.getInt("dohod") + ".";
         	   asi = asi +                                  "| Kg.            " + rs.getInt("ves_kg");
         	   agent.setStartText(asi);
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

    
    public static final String DRIVER_VERSION = "RivonaKeda";
    private String         url = "jdbc:odbc:rivona00";
    private String	       url2 = "jdbc:odbc:rivona00";
   
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
	public double getSalary(String agentCode) {
		return 0;
	}





	  
	  public String getSaveDebtInfo() {
		  makePictureCat();
		  System.exit(0);
	   	return null;
	   }
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

