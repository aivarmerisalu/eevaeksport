/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.routines.EAN13CheckDigit;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class RivileBalmerkDBDriver extends AbstractDBDriver implements  BulkUpdater ,Discounter, PictureMaker{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public RivileBalmerkDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
        emailProps = new Properties();
        try{
        	emailProps.load(new FileInputStream(parent.getProps().getProperty("emailfile")));
        }catch (Exception e){
        }
        try {
            Class.forName(databaseDriverName).newInstance();

        } catch (Exception e) {
            parent.showError(e);
        }
        user =  getParent().getProps().getProperty("rest.user");
        pass = getParent().getProps().getProperty("rest.pass");
        rcompanyx = getParent().getProps().getProperty("rest.companyx");
     
         }
      
      String user =  null;
      String pass = null;
      String rcompanyx = null;

    
    
    private void makePictureCat(){


    	  String cmd = " SELECT N17_KODAS_PS AS toode,ltrim(N17_PAV)  AS nimetus,dbo.papn(L11_LAUKAS , 8, 'c')  AS tykke,dbo.papn(L11_LAUKAS , 9, 'c')  AS alusel, 0 AS myygihind," +
    		   		"N17_KODAS_PS AS ean, "+
    		   " 0 AS kogus,n17_kodas_ls_5 AS grupp, N17_KODAS_GS AS grupp2 ,N17_MEN_PAV AS minkogus ,N17_KODAS_LS_4 AS status , N17_MAX_NUOL as maks," +
    		   " case when N17_DATA_NAUJA>getdate() AND N17_AR_NAUJA=1 then 1 else 0 end AS uus " +
    		   " FROM n17_prod a  LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps left join L11_PAP1 b on b.L11_KODAS_NR = 'PSN17' and N17_KODAS_PS = L11_KODAS1 where  N17_POZ_DATE !=1" +
    		   " " ;


String pictsa = parent.getProps().getProperty("picta.dir");
String picts = parent.getProps().getProperty("pict.dir");

File d1  = new File(pictsa);
File[] dd1 = d1.listFiles();


for(int i = 0;dd1.length>i;i++){
	File fil = dd1[i];
	String name = fil.getName();
//	System.out.println(name);
	if(name.contains("(") && name.contains(")") && name.toLowerCase().endsWith("jpg")) {
		String ean = name.substring(name.lastIndexOf("(")+1,name.lastIndexOf(")"));
		
		if(ean.length() > 4) {
			 File fff =  new File (picts + ean + ".jpg");
			 if(!fff.exists()) {
				  try {
						Converter.copyFile(fil,fff);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  
			 }
	        	
		}
	}
	
}



         	String pictspda = parent.getProps().getProperty("pictpda.dir");


     	   //Kataloogi sisude kustutamine
     	        	 File res2 = new File (pictspda);
     	            String[] ff2 = res2.list();
     	        	 for(int i = 0;ff2.length>i;i++){
     	            	//(new File ( pictspda + ff2[i])).delete(); 
     	            }
         	
         	
	   	try {
	   		connectionUp();
	   		Statement stmt = con.createStatement();
	   	    ResultSet	      rs = stmt.executeQuery(cmd);


	        StringBuffer buf = new StringBuffer();
	        StringBuffer buf0 = new StringBuffer();
	        int a = 0;
	       
	        int c = 0;
	        
	        // leiame kataloogid ja peame meeles 
	        File d  = new File(picts);
	        
	        while (rs.next()) {
	   	    	a++;
	   	    	c++;
	   	     String toode = rs.getString("toode").trim();  
	         toode = toode.replace('/', '_');
	         toode = toode.replace('/', '_');
	        
	         String ean = rs.getString("ean");
             if(ean != null && ean.trim().length()>0){
           	  try{
           		  EAN13CheckDigit ee = new EAN13CheckDigit();
           		  String ean2 = ean.trim() + ee.calculate(ean.trim());
                    ean = ean2;
           	  }catch(Exception y){
          	  }
             }
      
        	 
          System.out.println("Piltide kopeerimine ");	
        
         	  //System.out.println("gg " + gg[0] + " " + gg.length);
                   	
             	File in = new File ( picts + ean + ".jpg"); 
             	if(in.isFile()){            	
             	File inpda = new File ( pictspda + toode + ".jpg" ); 
             	
             	  if(!inpda.exists()) {
             		  Converter.shrinkAndCopyFile(in,inpda);
             	  }
             
             	}
             
             
	   	    	if(c == 100){
	   	    		  c = 0;
	   	    		//  System.out.println("Piltide leidmine "  + a);	
	   	   		}
	   	           
	                 File f = new File(pictspda + toode+".jpg");
	                 String len = "0";
	                 if(f.exists()){
	                   len =	f.length() + "";
	                   buf.append(toode+".jpg;"  + len + "\n");
	           	     } 
	       
	                 
	                 }
	   	    rs.close();
	   	    stmt.close();
	   	    con.close();
	   	
	   
	   	    
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
	        

	             
	             File fff =  new File (pictspda +  "list.txt");
	       	  Converter.copyFile(f,fff);  	
	     	
	   	} catch (Exception e) {
	   	    getParent().showError(e);
	   	}
	   
	}



    public String getAssortmentStr(Agent agent){
    	return null;
    }
            public String getDealerCustomerCSV(String agent) {
		return null;
	}
               
            
            public String getSaveDebtInfo() {
      		  // Siin teeme Cashi ära.
            	
            	this.getCustomers(null);
      			  File dir = new File(getParent().getProps().getProperty("cash.read"));
      			  //  System.out.println("vvv "+ driver.getImportFile());
      			    File[] files = dir.listFiles();
      			boolean hasdata = false;
      			  
      			 StringBuffer sb = new StringBuffer();
      			  sb.append("CUSTCODE,DELIVERYCODE,CUSTNAME,SUMEUR,CHEQUENR,CHEQUEDATE,MANAGER,COMMENT\n");
      			  
      			    this.getCustomers(null);
      			    HashSet loetud = new HashSet();
      			    for (int i = 0; i < files.length ; i++){
      			      File file = files[i];
      			      if (file.isFile()){
      			    	  try{
      			    		
      			    	  FileReader fr = new FileReader(file);
      						BufferedReader br = new BufferedReader(fr);
      						String line = br.readLine();
      					        line = br.readLine().replaceAll(";;", "; ;");
      					        try{
      					        	  line = line +  br.readLine().replaceAll(";;", "; ;");
      									  	
      					        }catch(Exception e){
      					        	
      					        }
      					      StringTokenizer st = new StringTokenizer(line, ";");
      							if (st.hasMoreElements()){
      								//CUS_REALID	SUMA	KVID	USRREALID	ANSPOSTED	ST	VAL COMM
      								//10002654	300.0	abc=123	1046	25.09.2015	VO	EUR  COMMENT
      		      String cuids = st.nextToken();
      		      String cuname = ("" + custNAMES.get(cuids)).replaceAll(",", ".");
      		      String aadr = cuids.substring(cuids.lastIndexOf("-")+1);
      		      cuids = cuids.substring(0,cuids.lastIndexOf("-"));
      		      String payer = cuids;
      		      double summa = Double.parseDouble(st.nextToken());
      		      String docid = st.nextToken().replaceAll(",", ".");
      		      String user = st.nextToken();
      		      String date = st.nextToken();
      		      String sts = st.nextToken();
      		      String curr = st.nextToken();
      		      String comment = " ";
      		      try{
      		    	  comment = st.nextToken().replaceAll(",", ".");
      		      }catch(Exception s){
      		    	  
      		      }
      		       
      		      date = date.replace('.', '-').replace('.', '-');//substring(8)+ "-"+date.substring(4, 6)+"-"+date.substring(0,1);
      		   //   System.out.println("date " + date);
      		      
      		   hasdata= true;
      		   sb.append(cuids + "," + aadr + "," + cuname + "," + summa + "," + docid + "," + date + "," + user + "," + comment + "\n") ;
      		      
      		     // siia faili moodustamine ja siis maili peale saatmine.
      		 
      		      
      							}
      					
      							
      						br.close();
      						fr.close();
      						File dest = new File(getParent().getProps().getProperty("cash.read") + "done/" + file.getName());
      						if(dest.exists()){
      							dest.delete();
      						}
      						file.renameTo(dest);
      						  
      			    	  }catch(Exception e){
      			    		  e.printStackTrace();
      			    	  }
      			    	  }
      			      
      			    }
      			    
      			    String timest = (Calendar.getInstance().getTimeInMillis()+"").substring(6);
      		if(hasdata)
      			    Converter.saveFile(getParent().getProps().getProperty("cash.output")+ "cash-"+ Converter.getDDpMMpYYYYpString(null) + "-" + timest +".csv", sb.toString());
      			    
      			    
      			    System.exit(0);
      			  return null;
      		   }

   
            
        public Vector getFixedAssortmentData() {
          return null;
        }

	public String getCustProdDiscounts(Agent agent) {
		return null;
	}

	public Vector getPriceMatrixData() {
		Vector lines = new Vector();
HashSet olemas = new HashSet();
		Statement st = null;
		try {
			connectionUp();
			st = con.createStatement();


			String sql = " SELECT N51_KODAS_KT AS grupp,N52_KODAS AS toode ,N52_KAINA as price " +
	         " FROM N51_SUTH p INNER JOIN N52_SUTD r ON N51_KODAS_KT=N52_KODAS_KT where N52_POZ_KAINA =1 and N52_KAINA != 0 and N52_KODAS not like 'MED%'" +
	                  "  and n51_end_date > getdate()+1 and n51_busena!=3  order by N51_KODAS_KT, N52_KODAS";

			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {

				String clGroup = rs.getString("grupp");
				String prodID = rs.getString("toode");
			
				double price = rs.getDouble("price");
				MatrixLine mline = new MatrixLine();

				
				if (mline.getCustID() == null && clGroup != null
						&& clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
					//mline.setStartDate(Converter.getYYYYpMMpDDpString(new Date()));
				}

				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}
			
				if (price != 0) {
					mline.setPrice(price);
				}
				lines.add(mline);
			}

			rs.close();
			st.close();
			

			System.out.println("Lines0 " + lines.size());
			 sql = " SELECT N31_KODAS_NS AS grupp ,N31_NUOL_PROC as ale  " +
	                 "   FROM N31_NUOD p   order by N31_KODAS_NS";

			
				st = con.createStatement();
				rs = st.executeQuery(sql);

				while (rs.next()) {

					String gr = rs.getString("grupp");
					String clGroup = gr.substring(0,3).trim();
					String alegr = Converter.cleanLitLatEst(gr.substring(3).trim());
					double ale = rs.getDouble("ale");
					MatrixLine mline = new MatrixLine();
					
					if (clGroup != null
							&& clGroup.trim().length() > 0) {
						mline.setCliGroup(clGroup.trim());
						mline.setDiscGroup(alegr);
						if (ale > 0) {
							mline.setDiscount(ale);
							///System.out.println("allahindlus " + ale);
							lines.add(mline);
						}
					}				
				}
				
				rs.close();
				st.close();
				

		
			//	System.out.println("Lines1 " + lines.size());
				

				 sql = " SELECT n51_kodas_ks as custid, n51_kodas_ks as clgroup, n52_kodas as prodid, n52_nuolaida AS discount, n52_kaina as price ,n51_beg_date as algus, n51_end_date as lopp "
				 		+ ", (case when N51_PRIORITETAS < 10 then 1 else 0 end ) as kamp " + 
				 		"  ,DATEDIFF(day,n51_beg_date, n51_end_date) as kamp2 FROM  N08_KLIJ " +
		 " join N51_SUTH on n08_kodas_ks=n51_kodas_ks " +
		 " join N52_SUTD on n51_kodas_kt=n52_kodas_kt " +
		" where n08_kain_abc=1  and n51_busena!=3 and n51_tipas=2 and (n51_poz_date = 1 and(n51_poz_date = 1 and getdate() between n51_beg_date and n51_end_date)) and n52_kodas_us2!='            ' ";

				
					connectionUp();
					st = con.createStatement();
					 rs = st.executeQuery(sql);

					while (rs.next()) {

						String custID = "";//rs.getString("custid");
						String clGroup = rs.getString("clgroup");
						String prodID = rs.getString("prodid");
						//String discGroup = rs.getString("alegroup");
						double discount = rs.getDouble("discount");
						double price = rs.getDouble("price");
						 String fromdate = rs.getString("algus");
                         String todate = rs.getString("lopp");
                         int kamp = rs.getInt("kamp");
                         int kamp2 = rs.getInt("kamp2");
                         
						
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
						if(discount > 0 ) {
							
							if(price > 0 ) {
								mline.setPrice(price * (100 -  discount)/100);
							}else {
								mline.setDiscount(discount);	
							}
						}else if (price != 0) {
							mline.setPrice(price);
						}
						
						if(kamp == 1 && kamp2 < 100) {
							 mline.setStartDate(fromdate.substring(0,10));
	                          mline.setEndDate(todate.substring(0,10));
						}
						 
                     if(!olemas.contains(clGroup+"-"+prodID)) {
                    		lines.add(mline);
                     }
					
						olemas.add(clGroup+"-"+prodID);
						//System.out.println(discGroup);
					}

					rs.close();
					st.close();
					con.close();
				
				//	System.out.println("Lines2 " + lines.size());
		
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return lines;
	}

	 public String getProductSortingData(){
	   	return null;
	   }

		public Vector getLastThreeData() {
			// TODO Auto-generated method stub
			return null;
		}
	

		 public Vector getDebt(String agentCode) throws SQLException{
			 return this.debtV;
		 }
		
		 private Vector debtV = new Vector();
		 private Vector d30 = new Vector();
		 private Vector d15 = new Vector();
		 
    public Vector getDebt() throws SQLException{
       debtV = new Vector();
     String sql = "SELECT I44_KODAS_KS AS klient,I44_DOK_NR AS number ,max(I44_DATA_MOK) AS maksepaev , " +
         " sum(i44_suma_db-i44_suma_cr) AS tasumata ,min(I44_DATA_DOK) AS kuupaev , max(datediff(day,I44_DATA_MOK,getdate())) as yle FROM i44_skol,n08_klij WHERE " +
         " i44_skol.i44_kodas_ks=n08_klij.n08_kodas_ks and " +
         " exists(select i44_kodas_ks,i44_dok_nr,sum(i44_suma_db-i44_suma_cr) as suma from i44_skol i44 where i44.i44_kodas_ks=i44_skol.i44_kodas_ks " +
         " and i44.i44_dok_nr=i44_skol.i44_dok_nr and i44_tipas<>11 and i44.i44_data_dok <= getdate() group by i44.i44_kodas_ks,i44.i44_dok_nr " +
        " having sum(i44.i44_suma_db-i44.i44_suma_cr)<>0) group by I44_DOK_NR,I44_KODAS_KS order by I44_KODAS_KS,i44_dok_nr ";

      connectionUp();
      PreparedStatement st = con.prepareStatement(sql);
      //st.setString(1, agentCode);
      ResultSet rs = st.executeQuery();
      while(rs.next()){
       
        String kl = rs.getString("klient").trim();
        if(!kl.equals("140")) { // maximat pole vaja 
       	 Debt debt = new Debt();
         debt.setCustID(kl);
         String number = rs.getString("number").trim();
         debt.setPayerID(debt.getCustID());
         debt.setDoknr(number);
         debt.setDuedate(rs.getDate("maksepaev"));
         debt.setSum(rs.getDouble("tasumata"));
         debt.setDate(rs.getDate("kuupaev"));
         int yle = rs.getInt("yle");
         debt.setDue(yle+"");
         if(yle > 30 && !d30.contains(kl) && debt.getSum() > 0) {// muutsin 30 peale
        	 d30.add(kl);
        	 if(kl.equals("757")) {
        		 System.out.println("võlgu " + kl + " " + number);
        	 }
         }
         
         if(yle > 15 && !d15.contains(kl) && debt.getSum() > 0) {// muutsin 30 peale
        	 d15.add(kl);
        	 if(kl.equals("757")) {
        		 System.out.println("võlgu " + kl + " " + number);
        	 }
         }
        	 
         debtV.addElement(debt);
        }
      }
     
      st.close();
      return debtV;
    }


    /**
	 *
	 */
    public void connectionUp(){
      connectionUp(this.url);
    }

    

   HashMap stocks6 = null;

    public String getSaveStockInfo(){
    	stocks = new HashMap();
    	stocks6 = new HashMap();
  	  String  cmd = "  SELECT  I17_KODAS_PS AS toode, sum(I17_kiekis - I17_atiduota) AS STOCK from i17_vpro inner join n17_prod on  I17_KODAS_PS = N17_KODAS_PS " +
" where I17_KODAS_IS in ('SAND1') AND ((I17_kiekis - I17_atiduota) >0 OR (I17_P_PAR_K)>0) and n17_kodas_ls_1<>''  and  N17_POZ_DATE =0 " +
	 "   GROUP BY I17_KODAS_PS  having sum(I17_kiekis - I17_atiduota ) >0   ORDER BY I17_KODAS_PS" ;
//,'SAND6','CER3','CER4'

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
            	 stocks.put(kood, seis+ "");
                 products.append(kood +  ";");
                 products.append(" ;");
                 products.append(getParent().getNF().format(seis) + "; "  +  "\n"); 
               }
             }
             rs.close();
             
             
             cmd = "  SELECT  I17_KODAS_PS AS toode, sum(I17_kiekis - I17_atiduota) AS STOCK from i17_vpro inner join n17_prod on  I17_KODAS_PS = N17_KODAS_PS " +
            		 " where I17_KODAS_IS in ('SAND6') AND ((I17_kiekis - I17_atiduota) >0 OR (I17_P_PAR_K)>0) and n17_kodas_ls_1<>''  and  N17_POZ_DATE =0 " +
            		 	 "   GROUP BY I17_KODAS_PS  having sum(I17_kiekis - I17_atiduota ) >0   ORDER BY I17_KODAS_PS" ;
            		 //,'SAND6','CER3','CER4'
             
             rs = st.executeQuery(cmd);

             while (rs.next()) {

               String kood = rs.getString("toode").trim();
               double seis = rs.getDouble("stock");
               if(seis > 0){
              	 stocks6.put(kood, seis+ "");
                 }
               }
               rs.close();
               st.close();
               this.connectionDown();
         } catch (Exception e) {
             getParent().showError(e);
         }
         return products.toString();
     }



     



        public String getDistribution() {
          return null;
        }


    private void connectionUp(String url){
      try {
        if (con != null){
          con.close();
        }

           con = DriverManager.getConnection(url,"dba","sql");
      } catch (Exception e) {
            getParent().showError(e);
      }

    }

    
    
    public TreeMap getProdGroups() throws SQLException {

      connectionUp(url2);

      TreeMap tm = new TreeMap();

            String cmd = " select distinct n17_kodas_ls_5 as kood ,n35_pav as nimi from \r\n" + 
            		"                        n17_prod,n35_lgst where n17_kodas_ls_5 = n35_kodas_ls";
       try {
        stmt = con.prepareStatement(cmd);
        ResultSet rs = stmt.executeQuery();
      // setTreeGroup(true);
        while (rs.next()) {
          String liik = rs.getString("kood").trim();
          String nimetus =  Converter.enCodeEst(rs.getString("nimi").trim());
         
          if (liik != "") {
            tm.put(liik,nimetus);
          }
        }
      }
      catch (Exception e) {
        getParent().showError(e);
      }
      connectionDown();
      return tm;
    }


    public TreeMap getProdGroupsSecond() throws SQLException {
        return null;
      }

    /**
     *
     */
    public void connectionDown(){
        try {
        	if(con != null){

                con.close();
                con = null;	
        	}
        } catch (Exception e) {
            getParent().showError(e);
        }
    }
    
    private HashMap stocks = new HashMap();
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
        connectionUp(url2);
        String  cmd = "SELECT N15_KODAS_MS AS kood ,N15_PAV AS nimi,N15_TEVAS AS objekt  FROM  n15_men ORDER by N15_PAV";
        TreeMap agents = new TreeMap();
        
        if("true".equals(getParent().getProps().getProperty("makepict"))){
			   makePictureCat(); 
		   }
        
        Properties props = EksportFrame.loadProps(parent.getProps().getProperty("emailfile"));
        StringBuffer sb2 = new StringBuffer();
        try {
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("kood").trim());
              String name = rs.getString("nimi").trim();
              String ob = rs.getString("objekt");
            //  if(ob != null && ob.trim().length()>0){
              	
              /*	if(ob.trim().equals("SIAULIAI")){
              		ob = "03";
              	}else if(ob.trim().equals("KAUNAS")){
              		ob = "02";
              	}if(ob.trim().equals("VILNIUS")){
              		ob = "01";
              	}*/
            	  ob = "01"; 
            	  if(props.containsKey(agent.getID())){
              	    sb2.append(agent.getID() + ";" + name + "\n");  
                }
                agent.setObject(ob.trim());
             // }
              agent.setName(name);
              agents.put(name, agent);
              agents1.put(agent.getID(), agent);
              if(sb2.length() > 2){
                	try{
                		 Converter.saveFileInUTF8( "agent.txt", sb2);
                	}catch (Exception e){
                		
                	}                
                 }
            }
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        return agents;
    }

    
    private TreeMap agents1 = new TreeMap();
    
    public TreeMap getCustomers(Agent agent){
    	try {
    		getDebt();
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	
    	 try{
      	   Converter.getManagersReplacements(agents1, rcompanyx, user , pass);
      	        	
      }catch(Exception e){
      	e.printStackTrace();
      }
    	
        connectionUp(url2);
        String cmd = "SELECT  ltrim(N08_KODAS_KS) AS kood1, (case when (N33_kodas_ak) is null then '1' else ltrim((N33_kodas_ak)) end) AS kood, " +
        		" N08_PAV as nimetus, N33_kodas_ak AS nimetus2, N08_CREDIT_LIM as cred, " +
				" N33_ADRESAS AS aadress, ltrim(N08_NUOL_GR)   AS ale , " +
				" ' ' AS visit ,N08_KODAS_KS AS payer , N08_KODAS_LS_8 as konsig , " +
				" (CASE WHEN LEN(LTRIM(N33_KODAS_MS))=0 or N33_KODAS_MS  IS NULL THEN  N08_KODAS_MS_P ELSE N33_KODAS_MS  END) AS agent ,  " +
				" (case when N08_KODAS_TS_P in ('REAL','SEKAN','ISANKSTINIS','NER') then '0' else ltrim(N08_KODAS_TS_P) end) as paymterm , N33_TEL as phone , N33_SHIP_FOB  as deld " +
				"  FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS where N08_PAV is not null order by N08_PAV ";
        log(cmd);
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        HashMap volad = new HashMap();
        HashMap used = new HashMap();
        HashMap salesres = new HashMap();

        try {

        	
            String salessql = "\r\n" + 
            		"SELECT ltrim(I06_KODAS_KS)  AS klient,(case when (I06_kodas_ak) is null then '1' else ltrim((I06_kodas_ak)) end) as kood ,\r\n" + 
            		"( ltrim( case when month(I06_DOK_DATA) <10 then  '0' + ltrim(str(month(I06_DOK_DATA))) else \r\n" + 
            		"            		ltrim(str(month(I06_DOK_DATA))) end) ) as kuu, \r\n" + 
            		"            		        		sum(I07_KIEKIS*(I07_SUMA/case when I07_KIEKIS =0 then 1 else I07_KIEKIS/I07_FRAKCIJA  end )) as summa FROM i07_pard p with(nolock) \r\n" + 
            		"            						  INNER JOIN i06_parh with(nolock) ON p.I07_KODAS_PO=I06_KODAS_PO \r\n" + 
            		"            						 left JOIN N33_KBAN with(nolock) ON N33_KODAS_KS = I06_KODAS_KS AND I06_ADR2=N33_ADRESAS \r\n" + 
            		"            						 WHERE I06_DOK_DATA > getdate()-200 \r\n" + 
            		"            						 GROUP BY I06_KODAS_KS,(YEAR(I06_DOK_DATA)),(case when (I06_kodas_ak) is null then '1' else ltrim((I06_kodas_ak)) end)\r\n" + 
            		"									 ,(month(I06_DOK_DATA)) order by I06_KODAS_KS,(case when (I06_kodas_ak) is null then '1' else ltrim((I06_kodas_ak)) end)\r\n" + 
            		"									 ,YEAR(I06_DOK_DATA),month(I06_DOK_DATA)";
            	
        

            stmt = con.prepareStatement(salessql);
            ResultSet rs1 = stmt.executeQuery();
            while(rs1.next()){
            	String klient = rs1.getString("klient").trim();
            	String kood = rs1.getString("kood").trim();
            	klient = klient + "-" + kood;
            	
            	String kuu = rs1.getString("kuu");
            	int summa = (int)rs1.getDouble("summa");
              if(salesres.get(klient) == null){
            	  salesres.put(klient,"   " + kuu + " : " + summa + " " );
              }else{
            	  String asi = (String)salesres.get(klient);     	  
            	  salesres.put(klient,"   " + kuu + " : " + summa +" |" + asi);
              }
            }
           rs1.close();
           stmt.close();
        
        	//System.out.println(salesres.toString());
        	
        	
        	
            String sql =
                "SELECT I44_KODAS_KS AS klient, sum(i44_suma_db-i44_suma_cr) AS summa  FROM i44_skol,n08_klij WHERE " +
               " i44_skol.i44_kodas_ks=n08_klij.n08_kodas_ks and " +
               " exists(select i44_kodas_ks,i44_dok_nr,sum(i44_suma_db-i44_suma_cr) as suma from i44_skol i44 where i44.i44_kodas_ks=i44_skol.i44_kodas_ks " +
               " and i44.i44_dok_nr=i44_skol.i44_dok_nr and i44_tipas<>11 and i44.i44_data_mok <= getdate() group by i44.i44_kodas_ks,i44.i44_dok_nr "+
                 " having sum(i44.i44_suma_db-i44.i44_suma_cr)<>0) group by I44_KODAS_KS order by I44_KODAS_KS ";

            stmt = con.prepareStatement(sql);
             //stmt.setInt(1, Integer.parseInt(agent.getID()));
             //stmt.setDate(2, getStartingDate());
               rs1 = stmt.executeQuery();

            while (rs1.next()) {
              String id = rs1.getString("klient").trim();
              double debt = Double.parseDouble(rs1.getString("summa").trim());
                volad.put(id, debt);
            }
            rs1.close();
            stmt.close();
        	


            String sqlu =
                "SELECT I44_KODAS_KS AS klient, sum(i44_suma_db-i44_suma_cr) AS summa  FROM i44_skol,n08_klij WHERE " +
               " i44_skol.i44_kodas_ks=n08_klij.n08_kodas_ks and " +
               " exists(select i44_kodas_ks,i44_dok_nr,sum(i44_suma_db-i44_suma_cr) as suma from i44_skol i44 where i44.i44_kodas_ks=i44_skol.i44_kodas_ks " +
               " and i44.i44_dok_nr=i44_skol.i44_dok_nr and i44_tipas<>11 and i44.i44_data_mok <= getdate() group by i44.i44_kodas_ks,i44.i44_dok_nr "+
                 " having sum(i44.i44_suma_db-i44.i44_suma_cr)<>0) group by I44_KODAS_KS order by I44_KODAS_KS ";

            stmt = con.prepareStatement(sqlu);
             //stmt.setInt(1, Integer.parseInt(agent.getID()));
             //stmt.setDate(2, getStartingDate());
             	      rs1 = stmt.executeQuery();

            while (rs1.next()) {
              String id = rs1.getString("klient").trim();
              double debt = Double.parseDouble(rs1.getString("summa").trim());
                used.put(id, debt);
            }
            rs1.close();
        	

        	
            stmt = con.prepareStatement(cmd);
            //stmt.setInt(1, Integer.parseInt(agent.getID()));
            //stmt.setDate(2, getStartingDate());
            ResultSet	      rs = stmt.executeQuery();

            
            

            while (rs.next()) {
              String accountnum = rs.getString("kood1").trim();
              String maks = accountnum;
              String acc = rs.getString("kood");
              boolean goes = true;
              if (acc != null && acc.trim().length()>2){
              	accountnum = accountnum + "-" + acc.trim();
              }else {
            	  goes = false;
              }
              String name = rs.getString("nimetus").trim() ;
            		 String nim2 = rs.getString("nimetus2");
              if(nim2 != null) {
            	  name = name + " " + nim2.trim();
              }
            		  
              custNAMES.put(accountnum,name);
            	name = name + " "  + accountnum;
            
            Customer cust = new Customer(accountnum);
            //  System.out.println("Name " + name);
              if(volad.get(maks) != null){
            	  cust.setDebt(Double.parseDouble("" +volad.get(maks)));
              }
              
              if(used.get(maks) != null){
            	  cust.setUsedCredit(Double.parseDouble("" +used.get(maks)));
              }
            
              double cred = rs.getDouble("cred");
              if(cred>0){
            	  cust.setCredit(cred+"");
              }
              
              
             
              if(salesres.get(accountnum) != null){
              	cust.setInfo(Converter.enCodeEst("CHART sales :|Month & year|Sales |" )+ salesres.get(accountnum) );
              }
              
              String aadr = rs.getString("aadress");
              if(aadr != null && aadr.trim().length()>0){
                cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim()),"Lithuania"));
                name = name + " " + cust.getAddress();
              }
              cust.setName(Converter.enCodeEst(name).replaceAll("  ", " ").replaceAll("  ", " "));

              String discStr = rs.getString("ale");
              if (discStr != null && discStr.trim().length() >0 ){
            		     cust.setGroup(discStr.trim());
              }
              
              String visit = rs.getString("visit");
              if (visit != null && visit.trim().length()>0){
                cust.setVisit(visit.trim());
              }
              
              
              String payer =rs.getString("payer");
            
              if(payer != null && payer.trim().length()> 0){
                cust.setPayer(payer.trim());
               
                if(volad.get(payer.trim()) != null){
              	  cust.setDebt(Double.parseDouble("" +volad.get(payer.trim())));
                }
                
                if(used.get(payer.trim()) != null){
              	  cust.setUsedCredit(Double.parseDouble("" +used.get(payer.trim())));
                }

              }
              
              if(maks.equals("757")) {
         		 System.out.println("võlgu2 757 " + maks + " " + d30.contains(maks) +" " + d30.contains(cust.getID()));
         	 }
              
              if(d30.contains(cust.getID()) || d30.contains(maks)) {
            	  cust.setSalesBlockT(true);
              }
             //siia vaja siis panna tingimus 
              if( d15.contains(cust.getID()) || d15.contains(maks)) {
            	//  cust.setSalesBlockT(true);
              }
              
              String konsig = rs.getString("konsig");
              if(cust.isSalesBlockT() && konsig != null && konsig.trim().equals("ATSISK_PO_RE")){
            	  cust.setSalesBlockT(false);
            	 // System.out.println("Blokk maha " + cust.getID());
              }
                 


             String ag = rs.getString("agent").trim();
              cust.addAgent(new Agent(ag));
             
              
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
              
             
              cust.setMainAgent(ag);
              try{
            	  cust.setPaymTerm(rs.getString("paymterm").trim());
                    
              }catch(Exception e){
            	  
              }
              
              String phone = rs.getString("phone");
              if(phone != null && phone.trim().length() > 0){
            	  cust.setPersonalData("Tel: " + phone.trim());
              }
              
              String deld = rs.getString("deld");
              
              if(deld != null && deld.trim().length() > 0){
            	  cust.setCity("Pristat.: " + deld.trim());
              }
                
              if(goes) {
                  customers.put(name, cust);
                  custMap.put(cust.getPayer(), cust);
                	  
              }
       
            }
            rs.close();

            stmt.close();
            con.close();
          }
          catch (Exception e) {
            getParent().showError(e);
          }

          return customers;
    }

    private HashMap custNAMES = new HashMap();
    
    public TreeMap getProducts(Agent agent){
    	this.getSaveStockInfo();
    //	System.out.println("stocks suurus " + stocks.size() + " " + stocks6.size());
  
   String cmd = " SELECT N17_KODAS_PS AS toode,ltrim(N17_PAV)  AS nimetus,dbo.papn(L11_LAUKAS , 8, 'c')  AS tykke,dbo.papn(L11_LAUKAS , 9, 'c')  AS alusel, 0 AS myygihind,\r\n" + 
   		"   		N17_KODAS_PS AS ean, \r\n" + 
   		"    0 AS kogus,n17_kodas_ls_5 AS grupp, N17_KODAS_GS AS grupp2, n17_kodas_ls_3 AS gruppabi , N17_KODAS_ES as gruppabi2 ,N17_MEN_PAV AS minkogus ,N17_KODAS_LS_4 AS status ,  N35_PAV_K3 as maks,\r\n" + 
   		"    case when N17_DATA_NAUJA>getdate() AND N17_AR_NAUJA=1 then 1 else 0 end AS uus , ltrim(N17_KODAS_LS_8) as sellout \r\n" + 
   		"    FROM n17_prod a  LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps left join L11_PAP1 b on b.L11_KODAS_NR = 'PSN17' and N17_KODAS_PS = L11_KODAS1\r\n" + 
   		"   left join L02_FILD on L02_kodas_fil = N17_KODAS_PS left join N35_LGST on N35_KODAS_LS= N17_KODAS_LS_5   where  N17_POZ_DATE !=1 and (L02_KODAS_FS is null or L02_KODAS_FS!='NP_O') and N17_MEN_PAV !='IG' and len(ltrim(N17_PAV_K1))  >0\r\n" + 
   		"   ";
  
  
            System.out.println(cmd);
        TreeMap products = new TreeMap();

        try {
        	

			  
        //	 select I33_KODAS_PS AS toode, max(I33_KAINA) AS hind from I33_PKAI 
        	//    		 where I33_KODAS_iS = 'SAND1'  and rtrim(ltrim(I33_KODAS_PS))  = '70000004769' - 11.30 GROUP BY I33_KODAS_PS order by I33_KODAS_PS				  
        	
        	
    String    sql = "select I33_KODAS_PS AS toode, max(I33_KAINA) AS hind from I33_PKAI "
    		+ " where I33_KODAS_iS = 'SAND1'  GROUP BY I33_KODAS_PS order by I33_KODAS_PS " ;
           // System.out.println("hind");
          if (isDebug()) System.out.println(sql);
          connectionUp(url2);
          TreeMap tooted = new TreeMap();
          Statement    stm = con.createStatement();
            ResultSet  rst = stm.executeQuery(sql);

               while (rst.next()) {
                 String toode = rst.getString("toode").trim();
                 double hind = rst.getDouble("hind");
                	 Toode tt = new Toode(toode);                
                     tt.setKastis(hind);
                     tooted.put(toode,tt);	
             }
             rst.close();
             stm.close();
             

             
                 sql = " select n19_kodas_gs as nogr from N19_PGR where N19_pav_k3 = 'IG' " ;
                   connectionUp(url2);
                   TreeMap nogroups = new TreeMap();
                       stm = con.createStatement();
                       rst = stm.executeQuery(sql);

                        while (rst.next()) {
                          String nogrups = rst.getString("nogr").trim();
                          nogroups.put(nogrups,nogrups);	
                      }
                      rst.close();
                      stm.close();
             
        	
        	
             connectionUp(url2);
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            
            
            while (rs.next()) {
              String kood = rs.getString("toode").trim();
              Product prod = new Product(kood);
              prod.setUnit("VNT");
              String nimetus =  Converter.enCodeEst(rs.getString("nimetus").trim());
              
              nimetus =  (nimetus.replaceAll("bottle", "bot.").replaceAll("Reserve", "Res.").replaceAll("Reserva", "Res.")
               		.replaceAll("Premium", "Prem.")
               		.replaceAll("Chateau", "Cha.").replaceAll("Stolichnaya", "Stoli.").replaceAll("Carmenere", "Carm.").replaceAll("Primitivo", "Prim.")
               		.replaceAll("Riserva", "Ris.").replaceAll("Classic", "Cla.").replaceAll("Collection", "Col.")
               		.replaceAll("Chablis", "Cha.").replaceAll("Tempranillo", "Temp.")
               		.replaceAll("Chardonnay", "Char.").replaceAll("Cabernet", "Cab.").replaceAll("Sauvignon", "Sauv.").replaceAll("Rouge", "Rou.").replaceAll("Blanc", "Bla.")
               		.replaceAll(", Tamsusis alus", " ")
               		.replaceAll(", Pusiau sausas raudonasis vynas su SGN", " ")
               		.replaceAll(", Sausas rausvasis vynas su SKVN", " ")
               		.replaceAll(", Sausas baltasis vynas su SKVN", " ")
               		.replaceAll(", Sauldus baltasis vynas su SKVN", " ")
               		.replaceAll(", Pusiau saldus baltasis vynas su SKVN", " ")
               		.replaceAll(", Pusiau saldus raudonasis vynas su SG", " ")
               		.replaceAll(", Sausas raudonasis vynas", " ")
               		.replaceAll(", Sausas baltasis vynas", " ")
               		.replaceAll(", Saldus r11148xx11120xxinis putojantis vynas", " ")
               		.replaceAll(", 11119xxviesusis alus", " ")
               		.replaceAll(", Spiritinis g11130xxrimas", " ")
               	
               		.replaceAll(", Kvietinis alus", " ")
               		.replaceAll(", Saldus nealkoholinis putojantis vynas", " ")
               		.replaceAll(", Saldus raudonasis nealkoholinis vynas", " ")
               		.replaceAll(", Saldus rausvasis vaisi11146xx vynas", " ")
               		.replaceAll(", Saldus baltais vaisi11146xx vynas", " ")
               		.replaceAll(", Pusiau sausas baltasis nealkoholinis vynas", " ")
               		
               		.replaceAll(", 11119xxviesusis nealkoholinis alus", " ")
               		.replaceAll(", Sausas rausvasis vynas", " ")
               		.replaceAll(", Sausas putojantis vynas", " ")
               		.replaceAll(", Pusiau sausas baltasis vynas", " ")
               		.replaceAll(", Pusiau sausas raudonasis vynas", " ")
               		.replaceAll(", Nealkoholinis gazuotas sul11128xxi11146xx g11130xxrimas", " ")
               		
               		.replaceAll(", Armanjakas", " ")
               		.replaceAll(", Brendis", " ")
               		.replaceAll(", Kalvadosas", " ")
               		.replaceAll(", Viskis", " ")
               		.replaceAll(", Konjakas", " ")
               		.replaceAll(", Sidras", " ")
               		.replaceAll(", Džinas", " ")
               		.replaceAll(" likeris", " ")
               		.replaceAll(" Likeris", " ")
               		.replaceAll(", Romas", " ")
               		.replaceAll(", Degtinė", " ")
               		.replaceAll(" su SGN", " ")
               		
               		
               		
               		.replaceAll(", Pusiau sausas putojantis vynas", " ").trim()
            		  );
              
              
              prod.setName(nimetus);
              try{
              prod.setInCases(rs.getDouble("tykke"));
            } catch (Exception e){}
              
              try{
                  prod.setOnPallet(rs.getDouble("alusel"));
                } catch (Exception e){}
            
            if(tooted.get(kood) != null){
            	Toode tt = (Toode)tooted.get(kood);
            	  prod.setPrice(tt.getKastis());
            }
             // String price = rs.getString("iii");
            if(stocks6.get(prod.getID()) != null){
          	  prod.setPartii("Sandelis 6: " + (String)stocks6.get(prod.getID())); 
          	prod.setName(prod.getName() + "S6@" +((int)Double.parseDouble((String)stocks6.get(prod.getID()))) );
            }
              String ean = rs.getString("ean");
              if(ean != null && ean.trim().length()>0){
            	  try{
            		  EAN13CheckDigit ee = new EAN13CheckDigit();
            		  String ean2 = ean.trim() + ee.calculate(ean.trim());
                      prod.setEAN(ean2);	
            		  prod.setName(prod.getName() + " " + ean2.substring(ean2.length()-4));
            	  }catch(Exception y){
            		  
            	  }
            
              }
       
              if(stocks.get(prod.getID()) != null){
            	  prod.setStock(Double.parseDouble((String)stocks.get(prod.getID()))); 
              }else {
            	  prod.setStock(0);
              }
              
              boolean goes= true;
            
              
            String grupp = rs.getString("grupp");
             if (grupp != null && grupp.trim().length()>0){
               prod.setGroup(grupp.trim());
               if(nogroups.get(grupp.trim()) != null) {
            	   goes = false;
               }
               prod.setDiscgrupp(grupp.trim());
             }
             String grupp2 = rs.getString("grupp2");
                    if (grupp2 != null && grupp2.trim().length()>0){
                    	if(nogroups.get(grupp2.trim()) != null) {
                     	   goes = false;
                        }
                    }
        
              String gruppabi = rs.getString("gruppabi");
                    if (gruppabi != null && gruppabi.trim().length()>0){
                    	if(gruppabi.trim().equals("GLASSES")) {
                     	   goes = true;
                           prod.setName("x " + prod.getName());
                        }
                    }
                    
                    gruppabi = rs.getString("gruppabi2");
                 
              
               double max = rs.getDouble("maks");
               if(max > 0){
               	if(max < 1){
               		max = 2;
               	}
               	int uus = rs.getInt("uus");
               	
               	if(uus > 0){
               		//prod.setName("t " + nimetus);
               		//prod.setStatusTop("1");
               	}
               	prod.setMaxDiscount(max);
               }
               
               if(prod.getID().contains("GLASSES")) {
            	   goes = true;
               }
           
               String sellout = rs.getString("sellout");
               if(sellout!= null && (sellout.trim().toLowerCase().startsWith("stop") || sellout.trim().equals("Active")) ) {
            	 if(sellout.trim().toLowerCase().startsWith("stop")){
            		   prod.setStatusSellout("1");
            	   }
            	 
            	   if(stocks.get(prod.getID()) == null  && stocks6.get(prod.getID()) == null) {
            		   goes = false;
            	   }
               }
            
               if (gruppabi != null && gruppabi.trim().length()>0){
               	System.out.println("gruppabi 2 " +  gruppabi + " " + prod.getID());
               	if(gruppabi.trim().equals("MS")) {
                	   goes = true;
                      prod.setName("x " + prod.getName());
                   }
               }
               
               if(prod.getID().contains("MED")) {
            	   goes = true;
               }
               
               prod.setVat("21");
               if(goes) {
            	   products.put(nimetus, prod);
               }
              

                // }
            }
            rs.close();
            stmt.close();
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
        connectionUp(url2);
        
        String cmd =" SELECT ltrim(I06_KODAS_KS)  AS klient,(case when (N33_kodas_ak) is null then '1' else ltrim((N33_kodas_ak)) end) as kood," +
        		"I07_KODAS AS toode,I07_KIEKIS AS kogus, " +
				"I06_DOK_DATA AS kuupaev  ,max(I07_SUMA/case when I07_KIEKIS =0 then 1 else I07_KIEKIS/I07_FRAKCIJA  end ) as lastprice FROM i07_pard p" +
				"  INNER JOIN i06_parh ON p.I07_KODAS_PO=I06_KODAS_PO "+
				" left JOIN N33_KBAN ON N33_KODAS_KS = I06_KODAS_KS AND I06_ADR2=N33_ADRESAS " +
				" WHERE I06_DOK_DATA > getdate()-180 "+
				" GROUP BY I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ltrim((N33_kodas_ak)) end),I07_KODAS,I06_DOK_DATA,I07_KIEKIS";
    
        if (isDebug()) {
           System.out.println(cmd);
       }

       Vector custProds = new Vector();
       HashMap kliendid = new HashMap();


       try {
          stmt = con.prepareStatement(cmd);
          
           ResultSet	      rs = stmt.executeQuery();

           while (rs.next()) {
             String kliendikood = rs.getString("klient").trim();
             String kood = rs.getString("kood");
             if (kood != null && kood.trim().length()> 0){
             	kliendikood = kliendikood +  "-" + kood.trim();
             }
             
             String tootekood = rs.getString("toode").trim();
             double kogus = Double.parseDouble(rs.getString("kogus").trim());
             String kuup = rs.getString("kuupaev").substring(5,10).trim();
             Customer cust = (Customer) kliendid.get(kliendikood);
             double lastprice = rs.getDouble("lastprice");
            
             if (cust == null) {
               cust = new Customer(kliendikood);
             }

             Product prod = new Product(tootekood);
             cust.addProduct(prod,
                             new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                          kuup, lastprice, 0,0," "),false);
             kliendid.put(kliendikood, cust);
             custProds.add(new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                          kuup, lastprice, 0,0," "));
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
      StringBuffer sb = new StringBuffer();
   try {
       File dir = new File(getParent().getProps().getProperty("pop3.dir"));
       if(dir.exists() && dir.isDirectory()){
    	   String [] files = dir.list();  
    	   if(files.length>0){
    		   for (int i = 0;i<files.length;i++){      		  
        		   if (files[i].toLowerCase().indexOf(agent.getID().toLowerCase())!= -1 && files[i].toLowerCase().endsWith("log")){
        			 //  System.out.println(files[i]);  
        			   File f = new File(getParent().getProps().getProperty("pop3.dir") + (files[i].substring(0,files[i].indexOf(".")) + ".LOG").toUpperCase());
        			//   System.out.println(f.getAbsolutePath());  
        			   if(f.exists()){
        				   FileReader fr = new FileReader(f.getAbsoluteFile());
        				  
        				   BufferedReader reader = new BufferedReader(fr);
        			        String line = " ";
        			        
        			      while( ((line = reader.readLine())!= null )){
        			    	 // System.out.println(line); 
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
                       // System.out.println(agent.getID() + sb.toString());
           if(sb.toString().trim().length()>0){
        	try{
        		Email mail = new Email(null);
        		mail.setSMTP(getParent().getProps().getProperty("smtphost"));
        		mail.setFilesTo("apskaita@ceryx.lt");
        		mail.setFrom(getParent().getProps().getProperty("from"));
        		mail.setSubjectFilesTo("Problems From Rivile Impormodule");
        		mail.setBody("\n" + sb.toString());
        		mail.setSendNotice(false);
        		mail.send();
        	}catch(Exception e){
        		
        	}
        	   
            return sb.toString();
           }else {
           	return null;
           }
    }

    
    private Properties emailProps = null;

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
      connectionUp(url2);
        double sales = 0;
        String cmd = "SELECT SUM(summa)  AS salary " +
                     "FROM osmy/my_tell te  WHERE te.osmy = ? " +
                     "AND month(regaeg)=month(date()) AND year(regaeg)=year(date())";
        log(cmd);

        try {
            stmt = con.prepareStatement(cmd);
            stmt.setInt(1, Integer.parseInt(agentCode));
            ResultSet	      rs = stmt.executeQuery();

            if(rs.next()) {
              sales = rs.getDouble("salary");
            }
            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }

        setSales(sales);
        return 0;
    }




    public void setBulkSales(Map agents){
    	
    	 connectionUp(url2);
    	 
       String cmd = " SELECT  I06_KODAS_MS AS agent , SUM(case when I06_OP_TIP = '52' then -I06_SUMA else 0 end) + " +
                     "SUM(case when I06_OP_TIP = '51' then I06_SUMA else 0 end)  AS pluss  FROM i06_parh WHERE " +
                     "month(I06_OP_DATA)=month(getdate()) AND year(I06_OP_DATA)=year(getdate()) and I06_PERKELTA='2'" +
                     " and I06_R_DATE <= getdate() " +
                     "GROUP BY  I06_KODAS_MS";
       
       cmd= "SELECT n08_kodas_ms_p AS agent , SUM(case when I06_OP_TIP = '52' then -I06_SUMA else 0 end) + \r\n" + 
       		"SUM(case when I06_OP_TIP = '51' then I06_SUMA else 0 end)  AS pluss\r\n" + 
       		"FROM i06_parh inner join n08_klij on i06_kodas_ks=n08_kodas_ks\r\n" + 
       		"WHERE month(I06_OP_DATA)=month(getdate()) AND year(I06_OP_DATA)=year(getdate()) and I06_PERKELTA='2'\r\n" + 
       		"and I06_R_DATE <= getdate() \r\n" + 
       		"GROUP BY n08_kodas_ms_p\r\n" + 
       		"";

       log(cmd);

         try {
        	 
        	 
           stmt = con.prepareStatement(cmd);

           String cmd2 =
               "SELECT I44_KODAS_MS AS agent, sum(i44_suma_db-i44_suma_cr) AS summa  FROM i44_skol,n08_klij WHERE " +
              " i44_skol.i44_kodas_ks=n08_klij.n08_kodas_ks and " +
              " exists(select i44_kodas_ks,i44_dok_nr,sum(i44_suma_db-i44_suma_cr) as suma from i44_skol i44 where i44.i44_kodas_ks=i44_skol.i44_kodas_ks " +
              " and i44.i44_dok_nr=i44_skol.i44_dok_nr and i44_tipas<>11 and i44.i44_data_mok<=getdate() group by i44.i44_kodas_ks,i44.i44_dok_nr "+
                " having sum(i44.i44_suma_db-i44.i44_suma_cr)<>0) group by I44_KODAS_MS order by I44_KODAS_MS ";

        	 
           String cmd3 =
               "SELECT I44_KODAS_MS AS agent, sum(i44_suma_db-i44_suma_cr) AS summa  FROM i44_skol,n08_klij WHERE " +
              " i44_skol.i44_kodas_ks=n08_klij.n08_kodas_ks and " +
              " exists(select i44_kodas_ks,i44_dok_nr,sum(i44_suma_db-i44_suma_cr) as suma from i44_skol i44 where i44.i44_kodas_ks=i44_skol.i44_kodas_ks " +
              " and i44.i44_dok_nr=i44_skol.i44_dok_nr and i44_tipas<>11 and i44.i44_data_dok<=getdate() group by i44.i44_kodas_ks,i44.i44_dok_nr "+
                " having sum(i44.i44_suma_db-i44.i44_suma_cr)<>0) group by I44_KODAS_MS order by I44_KODAS_MS ";

        	 

      

       HashMap voladkoik = new HashMap ();
       HashMap voladyle = new HashMap ();
       stmt = con.prepareStatement(cmd2);  
     ResultSet	      rs = stmt.executeQuery();
     while(rs.next()) {
    	 String agent = rs.getString("agent");
    	 if(agent != null && agent.trim().length() > 0){
    		 int volg = rs.getInt("summa");
    		 voladyle.put(agent.trim(), volg);
    	 }
     }
     rs.close();
           
          
     stmt = con.prepareStatement(cmd3);  
     	      rs = stmt.executeQuery();
     while(rs.next()) {
    	 String agent = rs.getString("agent");
    	 if(agent != null && agent.trim().length() > 0){
    		 int volg = rs.getInt("summa");
    		 voladkoik.put(agent.trim(), volg);
    	 }
     }
     rs.close();
     
     stmt = con.prepareStatement(cmd); 
          rs = stmt.executeQuery();
             while(rs.next()) {
               String agentID = rs.getString("agent").trim();
               Agent agent = (Agent)agents.get(agentID);
               if (agent != null){
                 agent.setMonthSales((int)rs.getDouble("pluss"));
                 
                 int yldvolg = 0;
                 int overvolg = 0;
                 if(voladyle.get(agentID)!= null){
                	 overvolg = Integer.parseInt(""+voladyle.get(agentID));
                 }
                 
                 if(voladkoik.get(agentID)!= null){
                	 yldvolg = Integer.parseInt(""+voladkoik.get(agentID));
                 }
                 
                 if(overvolg != 0){
                //	 agent.setStart("Pradelstos skolos : " +overvolg + " €.|" ); 
                  }          

                 if(yldvolg != 0){
                	// agent.setStart(agent.getStart() + "Visa skola : " +yldvolg + " €.|" );
                  }
                 
                 
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

    public static final String DRIVER_VERSION = "RivileBalmerk";
    private String             url = "jdbc:odbc:Rivile";
    private String	       url2 = "jdbc:odbc:Rivile";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;

	@Override
	public void PictureMaker() {
		
     		  makePictureCat();
     		  System.exit(0);
   
	}
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

