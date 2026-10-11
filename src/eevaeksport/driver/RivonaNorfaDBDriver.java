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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
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
public class RivonaNorfaDBDriver extends AbstractDBDriver implements  BulkUpdater ,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public RivonaNorfaDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

        try {
            Class.forName(databaseDriverName).newInstance();

        } catch (Exception e) {
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
      private String company = null;
      

 
  
   
 
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
            
            	  String  cmd =  " select i17_kodas_is as ladu, i17_kodas_ps as toode , i17_kodas_us, sum((i17_kiekis-I17_ATIDUOTA-I17_REZERVAS)/i17_frakcija) as kogus " +
              " from i17_vpro left outer join n17_prod on i17_kodas_ps=n17_kodas_ps " +
           	  " where I17_KODAS_IS in ('VY000001') AND ((I17_KIEKIS) >0 OR (I17_P_PAR_K)>0) and n17_kodas_ls_3<>''  and  N17_POZ_DATE = 0 " +
            	  			 " and left(n17_kodas_gs,4) not between 'MA32' and 'MA35'  GROUP BY I17_KODAS_PS ,i17_kodas_is,i17_kodas_us  having sum(I17_KIEKIS) >0   ORDER BY I17_KODAS_PS" ;
            	   
               String  sqlAlco = "   SELECT  'AL-' + I17_KODAS_PS AS toode, sum(I17_KIEKIS) AS kogus from i17_vpro inner join n17_prod on  I17_KODAS_PS = N17_KODAS_PS " +
            	      	   " where I17_KODAS_IS in ('ND000001') AND ((I17_KIEKIS) >0 OR (I17_P_PAR_K)>0) and n17_kodas_ls_3<>''  and  N17_POZ_DATE = 0 " +
            	      	  			 " and left(n17_kodas_gs,4)between 'MA32' and 'MA35'  GROUP BY I17_KODAS_PS  having sum(I17_KIEKIS) >0   ORDER BY I17_KODAS_PS" ;
            	      	      	   

            	  
                   if (isDebug()) System.out.println(cmd);


                   StringBuffer products = new StringBuffer();
                   try {

                   connectionUp();
                     Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery(cmd);

                     while (rs.next()) {

                       String kood = rs.getString("toode").trim();
                       double seis = rs.getDouble("kogus");
                       if(seis > 0){
                           products.append(kood +  ";");
                           products.append(" ;");
                           products.append(getParent().getNF().format(seis) + "; "  +  "\n"); 
                         }
                       }
                       rs.close();
                       
                       connectionUp(urlAlco);
                        st = con.createStatement();
                        rs = st.executeQuery(sqlAlco);

                       while (rs.next()) {

                         String kood = rs.getString("toode").trim();
                         double seis = rs.getDouble("kogus");
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
               	  String cmd = " select L01_KODAS_FS as grupp,l02_kodas_fil as toode from L01_FILH h , L02_FILD d ,n17_prod p where " +
               	  		" h.l01_kodas_fs=d.l02_kodas_fs  and L01_TIPAS = 1 " +
   " and N17_KODAS_PS=l02_kodas_fil and h.l01_kodas_fs in (select n33_ship_via from " +
   " N33_KBAN group by n33_ship_via ) order by L01_KODAS_FS ,N17_PAV ";
               	System.out.println(cmd);
                  	 connectionUp();
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
            
	public String getCustProdDiscounts(Agent agent) {
		return null;
	}

	public Vector getPriceMatrixData() {
		Vector lines = new Vector();

		String sql = " SELECT n51_kodas_ks as custid, '' as clgroup, n52_kodas as prodid, 0 AS discount, n52_kaina as price FROM  N08_KLIJ " +
 " join N51_SUTH on n08_kodas_ks=n51_kodas_ks " +
 " join N52_SUTD on n51_kodas_kt=n52_kodas_kt " +
" where n08_kain_abc=1 and n51_tipas=2 and (n51_poz_date = 1 and(n51_poz_date = 1 and now(*) between n51_beg_date and n51_end_date)) and n52_kodas_us2!='            ' ";

		Statement st = null;
		try {
			connectionUp();
			st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {

				String custID = rs.getString("custid");
				String clGroup = rs.getString("clgroup");
				String prodID = rs.getString("prodid");
				//String discGroup = rs.getString("alegroup");
				double discount = rs.getDouble("discount");
				double price = rs.getDouble("price");
				MatrixLine mline = new MatrixLine();

				if (custID != null && custID.trim().length() > 0) {
					mline.setCustID(custID.trim());
				}
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
			
			connectionUp(urlAlco);
			 st = con.createStatement();
			 rs = st.executeQuery(sql);

			while (rs.next()) {

				String custID = rs.getString("custid");
				String clGroup = rs.getString("clgroup");
				String prodID = rs.getString("prodid");
				double discount = rs.getDouble("discount");
				double price = rs.getDouble("price");
				MatrixLine mline = new MatrixLine();

				if (custID != null && custID.trim().length() > 0) {
					mline.setCustID(custID.trim());
				}
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
     String sql = "SELECT t03_kodas_ks AS klient,T03_DOK_NR AS number ,max(T03_DATA_MOK) AS maksepaev , " +
         " sum(t03_suma_db-t03_suma_cr) AS tasumata ,max(t03_DATA_DOK) AS kuupaev, max(t03_DATA_MOK-getdate()) AS yle FROM t03_sdok " +
		 " group by t03_kodas_ks,t03_dok_nr  ";
     
     sql = "SELECT i06_kodas_ls_3 as agent,t03_kodas_ks AS klient,T03_DOK_NR AS number ,max(T03_DATA_MOK) AS maksepaev , " +
             " sum(t03_suma_db-t03_suma_cr) AS tasumata ,max(t03_DATA_DOK) AS kuupaev, max(t03_DATA_MOK-getdate()) AS yle  " +
             " FROM t03_sdok  " +
             " left outer join i06_parh on (t03_dok_nr=i06_dok_nr) " +
             "     group by agent,t03_kodas_ks,t03_dok_nr " ;
     
     System.out.println("volanimekiri");
      connectionUp();
      PreparedStatement st = con.prepareStatement(sql);
      //st.setString(1, agentCode);
      ResultSet rs = st.executeQuery();
      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        debt.setPayerID(debt.getCustID());
        String dok = rs.getString("number").trim();
        if(dok.length() >5){
        	dok = dok.substring(3);
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
           con = DriverManager.getConnection(url,getParent().getProps().getProperty("dsna.user"),
        		   getParent().getProps().getProperty("dsna.pass"));
      } catch (Exception e) {
            getParent().showError(e);
      }
    }

    private TreeMap twoone = new TreeMap();
    private TreeMap teised = new TreeMap();

   

    
    
    public TreeMap getProdGroupsOLD() throws SQLException {
    	

      connectionUp(url2);

      TreeMap tm = new TreeMap();

            String cmd = "   SELECT n35_pav_k1 As esimene , N35_KODAS_LS as kood, N35_PAV as nimi FROM N35_LGST left join " +
        "     n17_prod on  n17_kodas_ls_3 = N35_KODAS_LS where n35_tevas='MOBILINK' and  n17_kodas_ls_3<>'' and  N17_POZ_DATE = 0 " +
          "      group by N35_KODAS_LS , N35_PAV,n35_pav_k1 ";
       try {
        stmt = con.prepareStatement(cmd);
        ResultSet rs = stmt.executeQuery();
    
      // tm.put("xxxx", "Tester");
        while (rs.next()) {
        	String esimene =Converter.cleanLitLatEst(rs.getString("esimene"));
        	 String liik = Converter.cleanLitLatEst(rs.getString("kood").trim());
        	 String nimetus = rs.getString("nimi");
        	 boolean fir = false;
        	 if(esimene != null && esimene.trim().length()>0){
        		twoone.put(liik, esimene.trim()) ;
           		//fir = true;
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
        		    teised.put(esimene.trim() + "|" +liik.trim(), Converter.enCodeEst(nimetus2.trim())); 
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
      getProdGroupsAlco(tm);
      return tm;
    }

    
    
    public void getProdGroupsAlco(TreeMap tm) throws SQLException {
    	

        connectionUp(urlAlco);

        String cmd = "   select N35_PAV_K1 as kood, N35_PAV_K1 as nimi FROM N35_LGST left join " +
     "    n17_prod on  n17_kodas_ls_3 = N35_KODAS_LS where n35_tevas='MOBILINK' and  n17_kodas_ls_3<>'' and  N17_POZ_DATE = 0 and n35_pav_k1 != '' " +
     "      group by  N35_PAV,n35_pav_k1 order by n35_pav_k1";
  
        
        
  try {
    stmt = con.prepareStatement(cmd);
    ResultSet rs = stmt.executeQuery();
    int i = 300;
    while (rs.next()) {
    	 String liik = "AL" + Converter.cleanLitLatEst(rs.getString("kood").trim());
    	 String nimetus = rs.getString("nimi");
    	 
    	 if(nimetus.startsWith(" ")){
       	 	  nimetus = "A " + nimetus.trim(); 
       	 
         }else if(nimetus.startsWith(".")){
       	 	  nimetus = "B " + nimetus.trim().substring(1);
       	  
         }else{
       		  nimetus = "C " + nimetus.trim();	  
       	 }
    	 
    	 
    	 
    	 String aa = i + "";
    	  if(aa.length() == 1 ){
    		  aa = "00" + aa;
    	  }
    	  if(aa.length() == 2 ){
    		  aa = "0" + aa;
    	  }
     	  koodJaGrupp.put(liik,aa);
    	 
    		  tm.put(aa, Converter.enCodeEst(nimetus.trim()));
    		  i++;
    }
  }
  catch (Exception e) {
    getParent().showError(e);
  }
      }

    

 public void getProdGroupsAlcoSec(TreeMap tm) throws SQLException {
    	

        connectionUp(urlAlco);

        String cmd = "   select N17_KODAS_LS_3 as kood, N35_PAV as nimi ,N35_PAV_K1 as kood2  FROM N35_LGST left join " +
       "  n17_prod on  n17_kodas_ls_3 = N35_KODAS_LS where n35_tevas='MOBILINK' and  n17_kodas_ls_3<>'' and  N17_POZ_DATE = 0 " +
       "    group by N17_KODAS_LS_3 , N35_PAV,n35_pav_k1";
  
        
        
  try {
    stmt = con.prepareStatement(cmd);
    ResultSet rs = stmt.executeQuery();
    while (rs.next()) {
    	 String liik = Converter.cleanLitLatEst(rs.getString("kood").trim());
    	 String nimetus = rs.getString("nimi");
    	 String liik2 = Converter.cleanLitLatEst(rs.getString("kood2"));
       	 
       	if(liik2 != null && koodJaGrupp.get(liik2.trim()) != null){
     		  tm.put(koodJaGrupp.get(liik2.trim()) + Converter.cleanLitLatEst(liik), Converter.enCodeEst(nimetus.trim()));  
       	}
    }
  }
  catch (Exception e) {
    getParent().showError(e);
  }

        
        
      }

    

    

    public TreeMap getProdGroups() throws SQLException {
    	

        connectionUp(url2);
        setTreeGroup(true);
        TreeMap tm = new TreeMap();

              String cmd = "    select N35_PAV_K1 as kood, N35_PAV_K1 as nimi FROM N35_LGST left join "+
             "  n17_prod on  n17_kodas_ls_3 = N35_KODAS_LS where n35_tevas='MOBILINK' and  n17_kodas_ls_3<>'' and  N17_POZ_DATE = 0 and N35_PAV_K1 != '' " +
               "  group by N35_PAV_K1 order by N35_PAV_K1";
        
              
              
        try {
          stmt = con.prepareStatement(cmd);
          ResultSet rs = stmt.executeQuery();
          while (rs.next()) {
        	  
          	 String liik = Converter.cleanLitLatEst(rs.getString("kood").trim());
          	 String nimetus = rs.getString("nimi");
          	if(nimetus.startsWith(" ")){
         	 	  nimetus = "A " + nimetus.trim(); 
             }else if(nimetus.startsWith(".")){
         	 	  nimetus = "B " + nimetus.trim().substring(1);
             }else{
         		  nimetus = "C " + nimetus.trim();	  
         	}
          		  tm.put(Converter.cleanLitLatEst(liik), Converter.enCodeEst(nimetus.trim()));  
           }
         } catch (Exception e) {
          getParent().showError(e);
        }
        
        koodJaGrupp = new TreeMap();
        getProdGroupsAlco(tm);
        
        TreeMap res = new TreeMap();
        
        
        
        Iterator enume = tm.keySet().iterator();
        int i = 1;
        while (enume.hasNext()){
      	 
      	  String kood = (String)enume.next();
      	  String nimi = (String)tm.get(kood);
        	
      	 String aa = i + "";
     	  if(aa.length() == 1 ){
     		  aa = "00" + aa;
     	  }
     	  if(aa.length() == 2 ){
     		  aa = "0" + aa;
     	  }
      	  koodJaGrupp.put(kood,aa);
    	  res.put(aa, nimi);
    	  i++;
     
        }
      	  
      
        
        return res;
      }


    
    
    private TreeMap koodJaGrupp = new TreeMap(); 
    public TreeMap getProdGroupsSecond() throws SQLException {

        connectionUp(url2);

        TreeMap tm = new TreeMap();

              String cmd = "   select N17_KODAS_LS_3 as kood, N35_PAV as nimi,N35_PAV_K1 as kood2 FROM N35_LGST left join " +
             "  n17_prod on  n17_kodas_ls_3 = N35_KODAS_LS where n35_tevas='MOBILINK' and  n17_kodas_ls_3<>'' and  N17_POZ_DATE = 0 " +
             "    group by N17_KODAS_LS_3 , N35_PAV,n35_pav_k1";
        
              
              
        try {
          stmt = con.prepareStatement(cmd);
          ResultSet rs = stmt.executeQuery();
          while (rs.next()) {
          	 String liik = Converter.cleanLitLatEst(rs.getString("kood").trim());
          	 String nimetus = rs.getString("nimi");
          	 String liik2 = Converter.cleanLitLatEst(rs.getString("kood2"));
           	 
          	if(liik2 != null && koodJaGrupp.get(liik2.trim()) != null){
        		  tm.put(koodJaGrupp.get(liik2.trim()) + Converter.cleanLitLatEst(liik), Converter.enCodeEst(nimetus.trim()));  
          	}
          }
        }
        catch (Exception e) {
          getParent().showError(e);
        }
        getProdGroupsAlcoSec(tm);
        return tm;

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
        HashMap asendajad = new HashMap();
        Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
        
    	   try {
    
    		   
    		   File f = new File ("vacation.txt");
    		   if(f.exists()){
    			
    		   FileReader fr = new FileReader("vacation.txt");
    	          BufferedReader reader = new BufferedReader(fr);
    	        String line = null;
    	      
    	       
    	       try{
   	    	    while((line = reader.readLine()) != null ){
       	            
       	         	StringTokenizer st = new StringTokenizer(line, ";");
       	   System.out.println(line);
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
   	    	 System.out.println("Asendajad " + asendajad.toString());
   	       }catch(Exception e){
   	    	   e.printStackTrace();
   	       }
    	       
   	    
    		   }
      
		  connectionUp();
        String  cmd = " SELECT N15_KODAS_MS AS kood ,N15_PAV AS nimi FROM  n15_men " +
             "  WHERE N15_TEVAS= 'MOBIL_AGENT' ORDER by N15_PAV ";
    
    
     
           
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
            	String id = rs.getString("kood").trim();
              Agent agent = new Agent(id);
              String name = rs.getString("nimi").trim();
              if(asendajad.get(id) != null){
              	Vector vv = (Vector)asendajad.get(id);
              	Enumeration aa = vv.elements();
              	while(aa.hasMoreElements()){
              		agent.addDubbler((String)aa.nextElement());
              	}
              }
              System.out.println("Asendajad 2 " + agent.getDubbler() );
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
        
        
        return agents;
    }

    public TreeMap getAgents1() {
		return agents1;
	}
    private TreeMap agents1 = new TreeMap();
    
    public TreeMap getCustomers(Agent agent){
        connectionUp(url2);
        System.out.println("Kliendid");
        String cmd = "    SELECT ltrim(N08_KODAS_KS) AS kood1, (case when (N33_kodas_ak) is null  " +
               " then '1' else ltrim(str(N33_kodas_ak)) end) AS kood,      		" +
        		" rtrim(N08_PAV) + ' ' + rtrim(N33_kodas_ak) AS nimetus, " +
				" (case when (N33_ADRESAS) is null then N08_ADR else N33_ADRESAS end)  AS aadress, " +
				"   N33_SHIP_FOB AS visit ,N08_KODAS_KS AS payer ,N08_IM_KODAS as regnum, N08_KODAS_LS_7 as ag1, " +
				"   (CASE WHEN LENGTH(LTRIM(N33_KODAS_LS_1))=0 or N33_KODAS_LS_1 IS NULL THEN  N08_KODAS_LS_1 ELSE  N33_KODAS_LS_1 END)  AS agent ," +
				" '' as paymterm , N08_PVM_KODAS as vatno , n08_credit_lim as credit , n08_sutartis,(case when n08_sutartis =1 then N51_END_DATE-getdate() else 0 end) as leping " +
				"  FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS  " +
				"  left join  n51_suth on N08_KODAS_KS= N51_KODAS_KS  " ;
   
        log(cmd);
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        try {
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
           
              String accountnum = rs.getString("kood1").trim();
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
             //   cust.setVisit(visit.trim());
              }
              
              
              String payer =rs.getString("payer");
              if(payer != null && payer.trim().length()> 0){
                cust.setPayer(payer.trim());
              }
              
              String regnum =rs.getString("regnum");

              String ag2 = rs.getString("ag1").trim();
              if ( ag2 != null ){
            	 	ag2 = ag2.trim();
                    cust.addAgent(new Agent(ag2));
	        
Agent agg = ((Agent)this.getAgents1().get(ag2));
    				
      				if(agg != null && agg.getDubbler().size()>0){
      					System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
      					for (int r = 0 ; r < agg.getDubbler().size() ; r++){
      						Agent dub = new Agent((String)agg.getDubbler().elementAt(r));
      						cust.addAgent(dub);
      					}
      				}
      		
              }
             
              
String ag = rs.getString("agent");
            if ( ag != null ){
            	ag = ag.trim();
                cust.addAgent(new Agent(ag));
            
      				Agent agg = ((Agent)this.getAgents1().get(ag));
    				
      				if(agg != null && agg.getDubbler().size()>0){
      					System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
      					for (int r = 0 ; r < agg.getDubbler().size() ; r++){
      						Agent dub = new Agent((String)agg.getDubbler().elementAt(r));
      						cust.addAgent(dub);
      					}
      				}
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
                if(credit != null && credit.trim().length()>0){
                	cust.setCredit(credit.trim());
                }
                
                
/*                String lopp = rs.getString("lopp");
                int leping = rs.getInt("leping");
                if(leping < 31 && leping > 0){
                   	cust.setInfo("CONTRCT ENDING " + lopp.substring(0,10));
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
              customers.put(name, cust);
              custMap.put(cust.getID(), cust);
            }
System.out.println("v�lad ");
String sql =
    " SELECT t03_kodas_ks as klient ,sum(t03_suma_db)-sum(t03_suma_cr)  as summa , " +
" sum(case when t03_data_mok < getdate()  then t03_suma_db else 0 end)-sum(case when t03_data_mok < getdate()  " +
" then t03_suma_cr else 0 end)  as summayle FROM t03_sdok group by t03_kodas_ks ";

    stmt = con.prepareStatement(sql);
     //stmt.setInt(1, Integer.parseInt(agent.getID()));
     //stmt.setDate(2, getStartingDate());
     ResultSet	      rs1 = stmt.executeQuery();

    while (rs1.next()) {
      String id = rs1.getString("klient").trim();
      double used = Double.parseDouble(rs1.getString("summa").trim());
      double debt = Double.parseDouble(rs1.getString("summayle").trim());

   Iterator itera =    custMap.entrySet().iterator();
   while (itera.hasNext()){
	   try{
		   Customer cust = (Customer) itera.next();
           if (cust != null && cust.getPayer().equals(id)) {
             cust.setDebt(cust.getDebt() + debt );
             cust.setUsedCredit(used);
           }    
	   }catch(Exception e){
		   
	   }
	 
   }
     
    }
            rs1.close();
            stmt.close();
            con.close();
          }
          catch (Exception e) {
            getParent().showError(e);
          }

          return customers;
    }
    
    private Date getDate(String value){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(value.substring(0, 4)));
        cal.set(Calendar.MONTH, Integer.parseInt(value.substring(5, 7))-1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(value.substring(8, 10)));
        return new Date(cal.getTime().getTime());
      }
      
    
    private String getVisitDay(String input){
  	  String res = "";
  	  
  	  for (int i = 0 ; i < input.length(); i++ ){
  		char a = input.charAt(i);
  		if(a == 'A'){
  			res = res + "E";
  		}
  		if(a == 'B'){
  			res = res + "T";
  		}
  		if(a == 'C'){
  			res = res + "K";
  		}
  		if(a == 'D'){
  			res = res + "N";
  		}
  		if(a == 'E'){
  			res = res + "R";
  		}
  		if(a == 'F'){
  			res = res + "L";
  		}
  		if(a == 'G'){
  			res = res + "P";
  		}
  		if(a == '1'){
  			res = res + "2";
  		}if(a == '2'){
  			res = res + "1";
  		}if(a == '3'){
  			res = res + "4";
  		}if(a == '4'){
  			res = res + "8";
  		}if(a == '5'){
  			res = res + "16";
  		}if(a == '6'){
  			res = res + "32";
  		}
  	  }
  	  
  	  if(res.length() == 0){
  		  res = " ";
  	  }
  	  return res ;
    }

    
    public TreeMap getProducts(Agent agent){

  
        TreeMap products = new TreeMap();
    	
    	  try {
         
    	   String  sqlnon = "   SELECT  I17_KODAS_PS AS toode, sum(I17_KIEKIS) AS kogus from i17_vpro inner join n17_prod on  I17_KODAS_PS = N17_KODAS_PS " +
 " where I17_KODAS_IS in ('VY000001') AND ((I17_KIEKIS) >0 OR (I17_P_PAR_K)>0) and n17_kodas_ls_3<>''  and  N17_POZ_DATE = 0 " +
			 " and left(n17_kodas_gs,4) not between 'MA32' and 'MA35'  GROUP BY I17_KODAS_PS  having sum(I17_KIEKIS) >0   ORDER BY I17_KODAS_PS" ;
    	   
    	   String  sqlAlco = "   SELECT  'AL-' + I17_KODAS_PS AS toode, sum(I17_KIEKIS) AS kogus from i17_vpro inner join n17_prod on  I17_KODAS_PS = N17_KODAS_PS " +
    	   " where I17_KODAS_IS in ('ND000001') AND ((I17_KIEKIS) >0 OR (I17_P_PAR_K)>0) and n17_kodas_ls_3<>''  and  N17_POZ_DATE = 0 " +
    	  			 " and left(n17_kodas_gs,4) between 'MA32' and 'MA35'  GROUP BY I17_KODAS_PS  having sum(I17_KIEKIS) >0   ORDER BY I17_KODAS_PS" ;
    	      	   

           System.out.println("laoseisud");
         if (isDebug()) System.out.println(sqlnon);
         connectionUp(url2);
         TreeMap tooted = new TreeMap();
            Statement stm = con.createStatement();
            ResultSet rst = stm.executeQuery(sqlnon);

              while (rst.next()) {
                String toode = rst.getString("toode").trim();
                int seis = (int)Double.parseDouble(rst.getString("kogus").replace(',','.'));
               	 Toode tt = new Toode(toode);
                    tt.setKogus(seis);       
                    tooted.put(toode,tt);	
            }
            rst.close();
            stm.close();

            System.out.println("Laoseisud toit " + tooted.size());
            if (isDebug()) System.out.println(sqlAlco);
            connectionUp(urlAlco);
           
                stm = con.createStatement();
                rst = stm.executeQuery(sqlAlco);

                 while (rst.next()) {
                   String toode = rst.getString("toode").trim();
                   int seis = (int)Double.parseDouble(rst.getString("kogus").replace(',','.'));
                  	 Toode tt = new Toode(toode);
                       tt.setKogus(seis);       
                       tooted.put(toode,tt);	
               }
               rst.close();
               stm.close();
               System.out.println("Laoseisud toit + alco " + tooted.size());
          
        
            
            
            String ggg = "	SELECT l11_kodas1 as toode, pap(L11_laukas,3,'c') as starta, pap(L11_laukas,4,'c') as dend  " +
            		" FROM dbo.L11_PAP1 where L11_kodas_nr = 'PSn17' and length(trim(pap(L11_laukas,3,'c')))> 9 and length(trim(pap(L11_laukas,4,'c'))) >9";
            connectionUp(url);
            stm = con.createStatement();
             rst = stm.executeQuery(ggg);
             TreeMap soodus = new TreeMap();
              while (rst.next()) {
                String toode = rst.getString("toode").trim();
                String algus = rst.getString("starta").trim();
                String lopp = rst.getString("dend").trim();
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
                	   soodus.put(toode,toode);	
                	   System.out.println("Sooduses " + toode);
                }
            }
            rst.close();
            stm.close();
            
            
            String gggAlco = "	SELECT 'AL-' + l11_kodas1 as toode, pap(L11_laukas,3,'c') as starta, pap(L11_laukas,4,'c') as dend  " +
    		" FROM dbo.L11_PAP1 where L11_kodas_nr = 'PSn17' and length(trim(pap(L11_laukas,3,'c')))> 9 and length(trim(pap(L11_laukas,4,'c'))) >9";
    connectionUp(urlAlco);
    stm = con.createStatement();
     rst = stm.executeQuery(gggAlco);
    
      while (rst.next()) {
        String toode = rst.getString("toode").trim();
        String algus = rst.getString("starta").trim();
        String lopp = rst.getString("dend").trim();
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
        	   soodus.put(toode,toode);	
        	   System.out.println("Sooduses alco " + toode);
        }
    }
    rst.close();
    stm.close();
            

            
            
            String bc = " select N37_KODAS_PS as prod ,N37_BAR_KODAS as ean  from n37_pmat left outer join n17_prod on n17_kodas_ps=n37_kodas_ps " +
				"  where N37_BAR_KODAS != N37_KODAS_PS and left(n17_kodas_gs,4) not between 'MA32' and 'MA35' "+
            	"	and N37_BAR_KODAS!='000000000000' and '0'+ltrim(N37_KODAS_PS)!=N37_BAR_KODAS and '00'+ltrim(N37_KODAS_PS)!=N37_BAR_KODAS";
            connectionUp(url);
            stm = con.createStatement();
             rst = stm.executeQuery(bc);
             HashMap bcodes = new HashMap();
              while (rst.next()) {
                String toode = rst.getString("prod").trim();
                String bar = rst.getString("ean").trim(); 
                if(bar.length() > 5){
                	 bcodes.put(toode,bar);	
                }
            }
              
            rst.close();
            stm.close();
            
            
            String bcAlco = " select 'AL-' + N37_KODAS_PS as prod ,N37_BAR_KODAS as ean  from n37_pmat left outer join n17_prod on n17_kodas_ps=n37_kodas_ps " +
			"  where N37_BAR_KODAS != N37_KODAS_PS and left(n17_kodas_gs,4) between 'MA32' and 'MA35' "+
        	"	and N37_BAR_KODAS!='000000000000' and '0'+ltrim(N37_KODAS_PS)!=N37_BAR_KODAS and '00'+ltrim(N37_KODAS_PS)!=N37_BAR_KODAS";
       
      connectionUp(urlAlco);
      stm = con.createStatement();
      rst = stm.executeQuery(bcAlco);
     
      while (rst.next()) {
        String toode = rst.getString("prod").trim();
        String bar = rst.getString("ean").trim(); 
        if(bar.length() > 5){
        	 bcodes.put(toode,bar);	
        }
    }
      
    rst.close();
    stm.close();
            
            
            
  TreeMap prices = new TreeMap();
  
  String sql2 = "SELECT max(n32_g_date),n32_kodas_ps as toode,max(rtrim(n32_kodas_us)) as yhik ,n32_tipas,(n32_kaina1) as price , max(n32_kiekis2) as alekogus, " +
  		" max(n32_kaina2) as alehind FROM dbo.N32_PABC left outer join n17_prod on n17_kodas_ps=n32_kodas_ps " +
  		" where n32_g_date < now(*) and n32_tipas='B' and left(n17_kodas_gs,4) not between 'MA32' and 'MA35' " +
		"   group by  n32_kodas_ps,n32_tipas,n32_kaina1 order by n32_kodas_ps,max(n32_g_date)";
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
             prices.put(toode,tt);
      }    
   
   rst1.close();
   stm1.close();
    	
    	

   
   String sqlaAlco =  "SELECT max(n32_g_date),'AL-' + n32_kodas_ps as toode,max(rtrim(n32_kodas_us)) as yhik ,n32_tipas,(n32_kaina1) as price , max(n32_kiekis2) as alekogus, " +
		" max(n32_kaina2) as alehind FROM dbo.N32_PABC left outer join n17_prod on n17_kodas_ps=n32_kodas_ps " +
		" where n32_g_date < now(*) and n32_tipas='B' and left(n17_kodas_gs,4) between 'MA32' and 'MA35' " +
	"   group by  n32_kodas_ps,n32_tipas,n32_kaina1 order by n32_kodas_ps,max(n32_g_date)";

   if (isDebug()) System.out.println(sqlaAlco);
    connectionUp(urlAlco);
     stm1 = con.createStatement();
     rst1 = stm1.executeQuery(sqlaAlco);

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
              prices.put(toode,tt);
       }    
    
    rst1.close();
    stm1.close();
    
   
   
	
   
        connectionUp(url2);
 
        this.getProdGroups();
       String cmd = "SELECT N17_KODAS_PS AS toode,trim(N17_PAV) AS nimetus,N27_KIEKIS_PAK  AS tykke," +
 "  N35_PAV_K1 as grupp, n17_kodas_ls_3 AS grupp2 ,N17_MEN_PAV AS minkogus , " +
 "   N17_KODAS_LS_4 AS status , N17_MAX_NUOL as maxa ,0 as alusel " +
 "   FROM n17_prod a   left join N27_PAK on  trim(N17_KODAS_PS)=trim(N27_KODAS_PS) and left(N27_KODAS_US_PAK,8)='SANDPICK' " +
"	left join N35_LGST on  n17_kodas_ls_3 = N35_KODAS_LS " +
 "   where  n17_kodas_ls_3<>'' and n17_kodas_ls_4<>'01' and  N17_POZ_DATE =0 " +
 "   and left(n17_kodas_gs,4) not between 'MA32' and 'MA35' order by N17_KODAS_PS ";  
   
       String cmdalco = "SELECT 'AL-' +  N17_KODAS_PS AS toode,trim(N17_PAV) AS nimetus,N27_KIEKIS_PAK  AS tykke," + 
       "  N35_PAV_K1 as grupp, n17_kodas_ls_3 AS grupp2 ,N17_MEN_PAV AS minkogus , " +
       "   N17_KODAS_LS_4 AS status , N17_MAX_NUOL as maxa ,0 as alusel " +
       "   FROM n17_prod a   left join N27_PAK on  trim(N17_KODAS_PS)=trim(N27_KODAS_PS) and left(N27_KODAS_US_PAK,8)='SANDPICK' " +
      "	left join N35_LGST on  n17_kodas_ls_3 = N35_KODAS_LS " +
       "   where  n17_kodas_ls_3<>'' and n17_kodas_ls_4<>'01' and  N17_POZ_DATE =0 " +
       "   and left(n17_kodas_gs,4)  between 'MA32' and 'MA35' order by N17_KODAS_PS ";
       
       
   if (isDebug()) {
            System.out.println(cmd);
        }
        System.out.println("tooted");
        System.out.println("laos " + tooted.size());
      
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
              String add = "";
              if(tt != null){
              	pr = tt.getKastis(); 
              	if(pr!= 0){
                  	prod.setPrice(pr);
                }
              	String yhik = tt.getLadu();
             if(yhik != null && yhik.trim().length()>0){
                	prod.setUnit(yhik.trim());
                }
                Toode t = (Toode) tooted.get(kood);
                if(t != null){
                	kogus = t.getKogus();
                	 if(kogus!= 0){
                		 if(prod.getUnit().equals("KG")){
                			 kogus = kogus /1000;
                		 }
                    	prod.setStock(kogus);
                    }
                }
                int pallet = 0;
                 try{
                	 if(prod.getUnit().equals("KG")){
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
             	prices.remove(kood);
              }
            String lopp = prod.getID();
            if(lopp.length() > 4){
            	nimetus  = nimetus  + " " + lopp.substring(lopp.length() - 3 ) + " ";
            }
            String aleke = "";
            if(soodus.get(prod.getID()) != null){
            	aleke = "%% ";
            	prod.setStatusDiscount("1");
            }
              prod.setName(Converter.enCodeEst(aleke + nimetus + add));
              try{
            	  double tykke = rs.getDouble("tykke");
            	  if(prod.getUnit().equals("KG")){
            		  tykke = tykke /1000;
            	  }
              prod.setInCases(tykke);
            } catch (Exception e){}
            if(prod.getPrice() > 0){
            }else{
            	prod.setPrice(1);     	
            }
   
              String grupp = Converter.cleanLitLatEst(rs.getString("grupp"));
              String grupp2 = Converter.cleanLitLatEst(rs.getString("grupp2"));
              
              String minam = rs.getString("minkogus");
               prod.setVat("21");
            	   try{
            		   double alusel =  rs.getDouble("alusel");
                       if(alusel >0){
                    	   if(prod.getUnit().equals("KG")){
                    		   alusel = alusel /1000;
                     	  }
                    	prod.setOnPallet((int)alusel);   
                       }
                     }catch(Exception e){
                     }
                if(grupp != null && grupp.trim().length()>0 ){
                	if(koodJaGrupp.get(grupp.trim())!= null){
               		 prod.setGroup(koodJaGrupp.get(grupp.trim())+"");
               		 if(grupp2 != null && grupp2.trim().length()>0 ){
                  		  prod.setGroupSecond(koodJaGrupp.get(grupp.trim()) + "" + grupp2.trim());
                  	    }
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
            	   products.put(nimetus, prod);
                   done.addElement(prod.getID());
               }
            }  
             
             
             
           
             connectionUp(urlAlco);
             stmt = con.prepareStatement(cmdalco);
             	      rs = stmt.executeQuery();
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
               String add = "";
               if(tt != null){
               	pr = tt.getKastis(); 
               	if(pr!= 0){
                   	prod.setPrice(pr);
                 }
               	String yhik = tt.getLadu();
              if(yhik != null && yhik.trim().length()>0){
                 	prod.setUnit(yhik.trim());
                 }
                 Toode t = (Toode) tooted.get(kood);
                 if(t != null){
                 	kogus = t.getKogus();
                 	 if(kogus!= 0){
                 		 if(prod.getUnit().equals("KG")){
                 			 kogus = kogus /1000;
                 		 }
                     	prod.setStock(kogus);
                     }
                 }
                 int pallet = 0;
                  try{
                 	 if(prod.getUnit().equals("KG")){
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
              	prices.remove(kood);
               }
             String lopp = prod.getID();
             if(lopp.length() > 4){
             	nimetus  = nimetus  + " " + lopp.substring(lopp.length() - 3 ) + " ";
             }
             String aleke = "";
             if(soodus.get(prod.getID()) != null){
             	aleke = "%% ";
             	prod.setStatusDiscount("1");
             }
               prod.setName(Converter.enCodeEst("al " +aleke + nimetus + add));
               try{
             	  double tykke = rs.getDouble("tykke");
             	  if(prod.getUnit().equals("KG")){
             		  tykke = tykke /1000;
             	  }
               prod.setInCases(tykke);
             } catch (Exception e){}
             if(prod.getPrice() > 0){
             }else{
             	prod.setPrice(1);     	
             }
    
               String grupp = Converter.cleanLitLatEst(rs.getString("grupp"));
               String grupp2 = Converter.cleanLitLatEst(rs.getString("grupp2"));
               
               String minam = rs.getString("minkogus");
                prod.setVat("21");
             	   try{
             		   double alusel =  rs.getDouble("alusel");
                        if(alusel >0){
                     	   if(prod.getUnit().equals("KG")){
                     		   alusel = alusel /1000;
                      	  }
                     	prod.setOnPallet((int)alusel);   
                        }
                      }catch(Exception e){
                      }
                 if(grupp != null && grupp.trim().length()>0 ){
                	 if(koodJaGrupp.get(grupp.trim())!= null){
                   		 prod.setGroup(koodJaGrupp.get(grupp.trim())+"");
                   		 if(grupp2 != null && grupp2.trim().length()>0 ){
                      		  prod.setGroupSecond(koodJaGrupp.get(grupp.trim()) + "" + grupp2.trim());
                      	    }
                    	}
                 	}
                 
                 if(grupp2 != null && grupp2.trim().length()>0 ){
             		// prod.setGroupSecond(grupp2.trim());
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
             	   products.put("al" + nimetus, prod);
                    done.addElement(prod.getID());
                }
             }
             
        } catch (Exception e) {
            getParent().showError(e);
        }

        return products; 
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
                 "  I07_KODAS AS toode,sum(I07_KIEKIS/I07_FRAKCIJA)AS kogus,  " +
                  "       I06_DOK_DATA AS kuupaev ,max(I07_SUMA/case when I07_KIEKIS =0 then 1 else I07_KIEKIS/I07_FRAKCIJA  end ) as lastprice " +
                  " FROM i06_parh  force index (dok_data) " +
                  "      left JOIN N33_KBAN ON N33_KODAS_KS = I06_KODAS_KS AND I06_kodas_ak=N33_kodas_ak " +
                  "  left outer join i07_pard force index (kodas) ON I07_KODAS_PO=I06_KODAS_PO" +
                  " left outer join n17_prod on n17_kodas_ps=I07_KODAS  " +
                  "       WHERE i06_kodas_MS <>'' and I06_DOK_DATA > ? and left(n17_kodas_gs,4) not between 'MA32' and 'MA35'   " +
                  "      group by I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ltrim(str(N33_kodas_ak))end),I07_KODAS,I06_DOK_DATA " +
				"		 order by I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ltrim(str(N33_kodas_ak))end),I07_KODAS,I06_DOK_DATA  " ;

        
        String cmdalko ="SELECT ltrim(I06_KODAS_KS)  AS klient,(case when (N33_kodas_ak) is null then '1' else ltrim(str(N33_kodas_ak)) end) as kood, " +
        "  'AL-' +I07_KODAS AS toode,sum(I07_KIEKIS/I07_FRAKCIJA)AS kogus,  " +
         "       I06_DOK_DATA AS kuupaev ,max(I07_SUMA/case when I07_KIEKIS =0 then 1 else I07_KIEKIS/I07_FRAKCIJA  end ) as lastprice " +
         " FROM i06_parh  force index (dok_data) " +
         "      left JOIN N33_KBAN ON N33_KODAS_KS = I06_KODAS_KS AND I06_kodas_ak=N33_kodas_ak " +
         "  left outer join i07_pard force index (kodas) ON I07_KODAS_PO=I06_KODAS_PO" +
         " left outer join n17_prod on n17_kodas_ps=I07_KODAS  " +
         "       WHERE i06_kodas_MS <>'' and I06_DOK_DATA > ? and left(n17_kodas_gs,4)  between 'MA32' and 'MA35'   " +
         "      group by I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ltrim(str(N33_kodas_ak))end),I07_KODAS,I06_DOK_DATA " +
		"		 order by I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ltrim(str(N33_kodas_ak))end),I07_KODAS,I06_DOK_DATA  " ;

        if (isDebug()) {
           System.out.println(cmd);
       }

       Vector custProds = new Vector();
       HashMap kliendid = new HashMap();


       try {
          stmt = con.prepareStatement(cmd);
          stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
           ResultSet	      rs = stmt.executeQuery();

           while (rs.next()) {
             String kliendikood = rs.getString("klient").trim();
             String kood = rs.getString("kood");
             if (kood != null && kood.trim().length()> 0){
             	kliendikood = kliendikood +  "-" + kood.trim();
             }
            
             String tootekood = rs.getString("toode");
             if(tootekood != null){
            	 tootekood = tootekood.trim();
             
             double kogus = Double.parseDouble(rs.getString("kogus").trim());
             String kuup = rs.getString("kuupaev").substring(5,10).trim();
             double lastprice = rs.getDouble("lastprice")*1.21;
             if(lastprice >0 ){
            	 kuup = kuup + " (" + nfvat.format(lastprice) + "lt.)"; 
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

           connectionUp(urlAlco);
           stmt = con.prepareStatement(cmdalko);
           stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
            	      rs = stmt.executeQuery();

            while (rs.next()) {
              String kliendikood = rs.getString("klient").trim();
              String kood = rs.getString("kood");
              if (kood != null && kood.trim().length()> 0){
              	kliendikood = kliendikood +  "-" + kood.trim();
              }
             
              String tootekood = rs.getString("toode");
              if(tootekood != null){
             	 tootekood = tootekood.trim();
              
              double kogus = Double.parseDouble(rs.getString("kogus").trim());
              String kuup = rs.getString("kuupaev").substring(5,10).trim();
              double lastprice = rs.getDouble("lastprice")*1.21;
              if(lastprice >0 ){
             	 kuup = kuup + " (" + nfvat.format(lastprice) + "lt.)"; 
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
        			   System.out.println(files[i]);  
        			   File f = new File(getParent().getProps().getProperty("pop3.dir") + (files[i].substring(0,files[i].indexOf(".")) + ".LOG").toUpperCase());
        			   System.out.println(f.getAbsolutePath());  
        			   if(f.exists()){
        				   FileReader fr = new FileReader(f.getAbsoluteFile());
        				  
        				   BufferedReader reader = new BufferedReader(fr);
        			        String line = " ";
        			        
        			      while( ((line = reader.readLine())!= null )){
        			    	  System.out.println(line); 
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
                        System.out.println(agent.getID() + sb.toString());
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
                  String asi = getCurStart(0).substring(0,6) + "| Pardavimas " + rs.getInt("suma_p") + "Lt."; 
            	   asi = asi +                                  "| Pelnas       " + rs.getInt("dohod") + "Lt.";
            	   asi = asi +                                  "| Kg.           " + rs.getInt("ves_kg");
            	  rrr.put(agentID, asi);
               
             }
             rs.close();
             
             System.out.println(rrr.size() + "suurus");
             stmt = con.prepareStatement(cmd2);  
   	      rs = stmt.executeQuery();
          while(rs.next()) {
            String agentID = rs.getString("agent").trim();
            Agent agent = (Agent)agents.get(agentID);
            if (agent != null){
            //  agent.setMonthSales((int)rs.getDouble("pluss"));
         	   // buildima start texti siia koos vajalike andmetega /
            	
            	String sta = "";
            	if(rrr.get(agentID) != null){
            		sta = rrr.get(agentID) + "||" ;
            	}
         	   String asi = sta + getCurStart(1).substring(0,6) + "| Pardavimas " + rs.getInt("suma_p") + "Lt."; 
         	   asi = asi +                                  "| Pelnas       " + rs.getInt("dohod") + "Lt.";
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
    

    private void makeNormpict(String pictspda,String pictssou ){
  	  File res2 = new File (pictspda);
        String[] ff2 = res2.list();
      //  System.out.println("ff2 suur " + ff2.length);
    	 for(int i = 0; ff2.length > i ; i++){
    		if(ff2[i].indexOf("img") != -1 ){
    	  		try {
    	  			System.out.println("min " + pictspda + ff2[i] + " | " + ff2[i].substring(0,ff2[i].indexOf("img")) + ".jpg");
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
	 //System.out.println("v�ljun");
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
       Vector govecal = new Vector();
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
              
                if(toode.startsWith("AL-")){
                	 f = new File(pictssou + toode.substring(3)+".jpg");
                     if(f.exists()){
                         len =	f.length() + "";
                          govecal.add(toode.substring(3) +".jpg");
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
//       	 L�ppes kustutamine 
       	 
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
                 if(govecal.contains(gg[i])){
                	 copyFile(in,new File (pictspda + "AL-" + gg[i])); 
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

    public static final String DRIVER_VERSION = "RivonaNorfa";
    private String         url = "jdbc:odbc:norfa";
    private String	       url2 = "jdbc:odbc:norfa";
    private String	       urlAlco = "jdbc:odbc:norfa_nd";
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

