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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
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
public class RivileJuaraDBDriver extends AbstractDBDriver implements  BulkUpdater ,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public RivileJuaraDBDriver(EksportPanel parent) {
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
    }

    
    
    private void makePictureCat(){



String cmd2 = " SELECT N17_KODAS_PS AS toode " +
" FROM n17_prod a    "  ;

String picts = parent.getProps().getProperty("pict.dir");
         	String pictspda = parent.getProps().getProperty("pictpda.dir");

	   	try {
	   		connectionUp();
	   		Statement stmt = con.createStatement();
	   	    ResultSet	      rs = stmt.executeQuery(cmd2);


	        StringBuffer buf = new StringBuffer();
	        StringBuffer buf0 = new StringBuffer();
	        int a = 0;
	        int b = 0;
	        int c = 0;
	        
	        // leiame kataloogid ja peame meeles 
	        File d  = new File(picts);
	        File[] dd = d.listFiles();
	        
	        while (rs.next()) {
	   	    	a++;
	   	    	c++;
	   	     String toode = rs.getString("toode").trim();  
	         toode = toode.replace('/', '_');
	         toode = toode.replace('/', '_');
	         String ean2 = "";
	        
           	  try{
           		  EAN13CheckDigit ee = new EAN13CheckDigit();
           		  ean2 = toode.trim() + ee.calculate(toode.trim());
                  
           	  }catch(Exception y){
           		  
           	  }
           
      
	         
	   	    	if(c == 100){
	   	    		  c = 0;
	   	    		  System.out.println("Piltide leidmine "  + a);	
	   	    		  
	   	    	}
	   	    	
	   	        //   toode = toode.substring(0,toode.length()-1);// l�ikame l�pust maha viimase numbri 
	              
	                 File f = new File(picts + ean2+".jpg");
	                 String len = "0";
	                 if(f.exists()){
	                   len =	f.length() + "";
	                 } 
	            
	                 if(toode.trim().indexOf("5012233096") != -1){
	       	   	   	  System.out.println("Piltide Asi "  + toode.trim() + " " + ean2 + " " + picts + ean2+".jpg" + " "
	       	   	   			  + f.exists() + " " + len);
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
//	        	 L�ppes kustutamine 
	        	 
	          System.out.println("Piltide kopeerimine ");	
	          //Failide kopeerimine  
	            String[] gg = new File(picts).list();
	         	  //System.out.println("gg " + gg[0] + " " + gg.length);
	            
	             for(int i = 0;gg.length>i;i++){          	
	             	File in = new File ( picts + gg[i]); 
	             	if(in.isFile()){            	
	             		//l�ikame maha yhe koha 
	             		String fname = (gg[i]);
	             		fname = fname.substring(0,fname.indexOf('.')-1) + ".jpg";
	             	File inpda = new File ( pictspda + fname ); 
	             	Converter.shrinkAndCopyFile(in,inpda);
	             	}
	             }
	             
	             
	             File fff =  new File (pictspda +  "list.txt");
	       	  Converter.copyFile(f,fff);  	
	       	  File fff0 =  new File (pictspda +  "list0.txt");
	       	Converter.copyFile(f0,fff0);  	
	   	} catch (Exception e) {
	   	   // getParent().showError(e);
	   	}
	   
	}
	 
	



    public String getAssortmentStr(Agent agent){
    	return null;
    }
            public String getDealerCustomerCSV(String agent) {
		return null;
	}
               
            
    public String getSaveDebtInfo() {
      		  makePictureCat();
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
		//this.getProducts(null);
		Vector lines = new Vector();
		
		
		//teeme gruppide sisu valmis 
	 	
    	HashMap gruppmap = new HashMap();
    	try {
    	
    	
  	  String  sql = " select L01_KODAS_FS as kood, L02_KODAS_FIL as toode from l02_fild inner join l01_filh on L01_KODAS_FS=L02_KODAS_FS "
  	  		+ " where L01_TIPAS=1  order by  L01_KODAS_FS,L02_KODAS_FIL" ;
 	   //('00000001','T0000002')
    //    System.out.println("teen GruppMap-i");
      if (isDebug()) System.out.println(sql);
      connectionUp(url);
         Statement stm = con.createStatement();
         ResultSet rst = stm.executeQuery(sql);

           while (rst.next()) {
             String kood = rst.getString("kood").trim();
              String toode  = rst.getString("toode").trim();
              ArrayList aa = new ArrayList();
              if(gruppmap.get(kood) != null) {
            	  aa = (ArrayList) gruppmap.get(kood) ;
              }
              aa.add(toode);
        	  gruppmap.put(kood, aa);
                         
         }
         rst.close();
         stm.close();
     	
		
	//	System.out.println("gruppmap " + gruppmap.size());
		

		 sql = "  SELECT N51_KODAS_KS AS grupp,N52_KODAS AS toode ,N52_NUOLAIDA as ale , N52_RUSIS as mis" + 
				"         FROM N51_SUTH p INNER JOIN N52_SUTD r ON N51_KODAS_KT=N52_KODAS_KT where  N52_NUOLAIDA != 0 \r\n" + 
				"                    order by N51_KODAS_KS,N52_KODAS";

		Statement st = null;
	
			connectionUp();
			st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {

				String clGroup = rs.getString("grupp");
				String prodID = rs.getString("toode");
				double ale = rs.getDouble("ale");
		        int mis = rs.getInt("mis");
		        if(mis != 7) {
		        	MatrixLine mline = new MatrixLine();
					if (clGroup != null && clGroup.trim().length() > 0) {
						mline.setCliGroup(clGroup.trim());
					}
					if (prodID != null && prodID.trim().length() > 0) {
							mline.setProdID(prodID.trim());
					}
				
					if (ale != 0) {
						mline.setDiscount(ale);;	
						lines.add(mline);
					}
		        } else {
		        	if(gruppmap.get(prodID) != null) {
		        		
		        		ArrayList aa = (ArrayList)gruppmap.get(prodID.trim());
		        	//	System.out.println("sees " + prodID + " " + aa.size());
		        		for(int i = 0; i<aa.size();i++) {
		        			String prod = (String)aa.get(i);
		        			MatrixLine mline = new MatrixLine();
							if (clGroup != null && clGroup.trim().length() > 0) {
								mline.setCliGroup(clGroup.trim());
							}
							
									mline.setProdID(prod);
							
						
							if (ale != 0) {
								mline.setDiscount(ale);;	
								lines.add(mline);
							}
		        		}
		        	}
		        	
		        }
				
			
			}

			rs.close();
			st.close();
			con.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	//	System.out.println("Matrix size " + lines.size());
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
      Vector list = new Vector();
     String sql = "SELECT I44_KODAS_KS AS klient,I44_DOK_NR AS number ,max(I44_DATA_MOK) AS maksepaev , " +
         " sum(i44_suma_db-i44_suma_cr) AS tasumata ,max(I44_DATA_DOK) AS kuupaev, max(datediff(day,I44_DATA_MOK,getdate())) AS yle FROM i44_skol,n08_klij WHERE " +
         " i44_skol.i44_kodas_ks=n08_klij.n08_kodas_ks and " +
         " exists(select i44_kodas_ks,i44_dok_nr,sum(i44_suma_db-i44_suma_cr) as suma from i44_skol i44 where i44.i44_kodas_ks=i44_skol.i44_kodas_ks " +
         " and i44.i44_dok_nr=i44_skol.i44_dok_nr and i44_tipas<>11 and i44.i44_data_dok <= getdate() group by i44.i44_kodas_ks,i44.i44_dok_nr " +
        " having sum(i44.i44_suma_db-i44.i44_suma_cr)<>0) group by I44_DOK_NR,I44_KODAS_KS order by I44_KODAS_KS,i44_dok_nr ";

      connectionUp();
      PreparedStatement st = con.prepareStatement(sql);
      //st.setString(1, agentCode);
      ResultSet rs = st.executeQuery();
      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        String number = rs.getString("number").trim();
        debt.setPayerID(debt.getCustID());
        debt.setDoknr(number);
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



    public String getSaveStockInfo(){
        
    	String  cmd = "  SELECT  I17_KODAS_PS AS toode, I17_KIEKIS-I17_ATIDUOTA AS STOCK , I17_KODAS_IS AS LADU  " + 
          		" from i17_vpro where I17_KODAS_IS in ('010000000049','010000000052','010000000058') and I17_KIEKIS > 0 "
          		+ " ORDER BY I17_KODAS_PS" ;


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



     



        public String getDistribution() {
          return null;
        }


    private void connectionUp(String url){
      try {
        if (con != null){
          con.close();
        }

           con = DriverManager.getConnection(url,"merisoft","@(}^V<#Xw5P?");
      } catch (Exception e) {
            getParent().showError(e);
      }

    }

    
    
    public TreeMap getProdGroups() throws SQLException {

      connectionUp(url2);

      TreeMap tm = new TreeMap();

            String cmd = "   SELECT distinct N17_KODAS_MS AS kood , (select N15_PAV from n15_men where N15_KODAS_MS =  N17_KODAS_MS) as nimi " + 
            		"    FROM n17_prod a  LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps where  N17_POZ_DATE !=1 " + 
            		"    and N17_PAV_K1 is not null  and len( ltrim(N17_KODAS_MS)) > 0 ";
       try {
        stmt = con.prepareStatement(cmd);
        ResultSet rs = stmt.executeQuery();
   
        while (rs.next()) {
          String liik = rs.getString("kood").trim();
          
          String nimetus =  Converter.enCodeEst(rs.getString("nimi"));
          
          if(nimetus == null) {
        	  nimetus = "aa No Name";
          }else {
        	  nimetus = nimetus.trim();
          }
         
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
       

        TreeMap tm = new TreeMap();

      /*        String cmd = " SELECT  N19_KODAS_GS AS kood ,N19_PAV  AS nimi  FROM  n19_pgr WHERE (length(trim(N19_KODAS_GS)))=4 ORDER BY N19_KODAS_GS";
         try {
          connectionUp(url2);
          stmt = con.prepareStatement(cmd);
          ResultSet rs = stmt.executeQuery();
        // setTreeGroup(true);
          while (rs.next()) {
            String liik = rs.getString("kood").trim();
            String nimetus = rs.getString("nimi").trim();
            if (liik != "") {
              tm.put(liik, Converter.enCodeEst(nimetus));
            }
          }
        }
        catch (Exception e) {
          getParent().showError(e);
        }
        connectionDown();*/
        return tm;
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
    
    private HashMap<String,Double> stocks = new HashMap<String,Double> ();
    private HashMap stocksabi = new HashMap();
    public DealerProduct getDealerProductsList() {
    	stocks = new HashMap<String,Double> ();
    	stocksabi = new HashMap();

        DealerProduct dealerProds = new DealerProduct();
        try {

          String  sql = "  SELECT  I17_KODAS_PS AS toode, I17_KIEKIS-I17_ATIDUOTA AS STOCK , I17_KODAS_IS AS LADU  " + 
          		" from i17_vpro where I17_KODAS_IS in ('010000000022','010000000052','010000000058', '010000000029','010000000057','010000000045','010000000047','010000000049'"
          		+ ",'SA0000000002', 'MD0000000003') and I17_KIEKIS > 0 "
          		+ " ORDER BY I17_KODAS_PS" ;

             if (isDebug()) System.out.println(sql);
             connectionUp(url2);
                Statement stm = con.createStatement();
                ResultSet rst = stm.executeQuery(sql);

                  while (rst.next()) {
                    String toode = rst.getString("toode").trim();
                    Double seis = Double.parseDouble(rst.getString("stock").replace(',','.'));
                    String ladu  =  rst.getString("ladu").trim();
                    dealerProds.add(ladu, toode,seis+""," "," " );
                  
                    	String lll = "*" + ladu.substring(9) + "*-" + seis+"|";
                    	if(stocksabi.get(toode) != null) {
                    		lll = lll + stocksabi.get(toode);
                    	}
                    	stocksabi.put(toode, lll);
                    	if(ladu.equals("010000000049") || ladu.equals("010000000052") || ladu.equals("010000000058")){
                    		if(stocks.get(toode) != null) {
                        		stocks.put(toode, (stocks.get(toode) +seis));
                        	}else {
                        		stocks.put(toode, seis);
                        	}
                        		
                    	}
                    	
                    
                  
                }
                rst.close();
                stm.close();

        } catch (Exception e) {
            getParent().showError(e);
        }

         return dealerProds;
        // return null;
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
    	
    //	this.getPriceMatrixData();
        connectionUp(url2);
        String  cmd = "SELECT N15_KODAS_MS AS kood ,N15_PAV AS nimi,N15_TEVAS AS objekt FROM "
        		+ " n15_men where N15_KODAS_MS like '000%' and len(rtrim(N15_KODAS_MS)) = 5 ORDER by N15_PAV";
        TreeMap agents = new TreeMap();
        
        
        HashMap asendajad = new HashMap();
        
    	try {
			 FileReader fr = new FileReader("vacation.txt");
	         BufferedReader reader = new BufferedReader(fr);
	       String line = null;
	 
	        while((line = reader.readLine()) != null ){
	           
	        	StringTokenizer st = new StringTokenizer(line, ";");
	//  System.out.println(line);
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
		 
		}catch(Exception d){
			
		}
        
        Properties props = EksportFrame.loadProps(parent.getProps().getProperty("emailfile"));
        StringBuffer sb2 = new StringBuffer();
        try {
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
            	String salesman = rs.getString("kood").trim(); 
              Agent agent = new Agent(salesman);
              String name = rs.getString("nimi").trim();
             String ob = null;
             
             if(asendajad.get(salesman) != null){
             	Vector vv = (Vector)asendajad.get(salesman);
             	Enumeration aa = vv.elements();
             	while(aa.hasMoreElements()){
             		agent.addDubbler((String)aa.nextElement());
             	}
             }
             
          //   00025- 1 manager Klaipedos profesionaia, storage number 45 Kristina Ramonaite,
           //  00027- 1 manager Panevezio profesionalai; storage number 29 Inga Kairiene;
            // 00024; 00026; 00028 -1 manager (Vilnius, Kaunas, Alytus); storage number 47 
              	
             if(agent.getID().trim().equals("00025")){
              		ob = "010000000045";
              	}
              	
             if(agent.getID().trim().equals("00024")){
              		ob = "010000000047";
              	}
              	
             if(agent.getID().trim().equals("00027")){
              		ob = "010000000057";
              	}
             
          
            
             if(ob != null) {
            	// agent.setObject(ob.trim());
                 agent.setName(name + " " + agent.getID());
                 agents.put(name, agent);
                 agents1.put(salesman, agent);
             }
            	
            }
            
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        return agents;
    }
    
    public TreeMap getAgents1() {
  		return agents1;
  	}
      private TreeMap agents1 = new TreeMap();

    public TreeMap getCustomers(Agent agent){
        connectionUp(url2);
        String cmd = "SELECT  ltrim(N08_KODAS_KS) AS kood1, (case when (N33_kodas_ak) is null then '1' else ltrim((N33_kodas_ak)) end) AS kood, \r\n" + 
        		"        	ltrim(N08_PAV) + ' ' AS nimetus , ltrim(N33_kodas_ak) as nimetus2,  \r\n" + 
        		"		 (case when N33_ADRESAS  !='' then N33_ADRESAS else ltrim(N08_ADR) end)  AS aadress,\r\n" + 
        		"		  (case when N08_NUOL_GR ='JURA' then '0' else ltrim(N08_NUOL_GR) end)  AS ale , \r\n" + 
        		"			' ' AS visit ,N08_KODAS_KS AS payer ,  \r\n" + 
        		"			(CASE WHEN len(TRIM(N33_SHIP_VIA))=0 or N33_SHIP_VIA IS NULL THEN  N08_KODAS_MS_P ELSE N33_SHIP_VIA END) AS agent ,  \r\n" + 
        		"			(case when N08_KODAS_TS_P in ('REAL','SEKAN','ISANKSTINIS','NER') then '0' else ltrim(N08_KODAS_TS_P) end) as paymterm ,\r\n" + 
        		"			 (case when N33_TEL  !='' then N33_TEL else ((case when N08_TEL  !='' then N08_TEL else (N08_MOB_TEL) end) ) end)  as phone \r\n" + 
        		"			, N08_E_MAIL as mail FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS  order by N08_PAV";
        log(cmd);
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        HashMap volad = new HashMap();

        try {

            String sql =
                "SELECT I44_KODAS_KS AS klient, sum(i44_suma_db-i44_suma_cr) AS summa  FROM i44_skol,n08_klij WHERE " +
               " i44_skol.i44_kodas_ks=n08_klij.n08_kodas_ks and " +
               " exists(select i44_kodas_ks,i44_dok_nr,sum(i44_suma_db-i44_suma_cr) as suma from i44_skol i44 where i44.i44_kodas_ks=i44_skol.i44_kodas_ks " +
               " and i44.i44_dok_nr=i44_skol.i44_dok_nr and i44_tipas<>11 and i44.i44_data_mok <= getdate() group by i44.i44_kodas_ks,i44.i44_dok_nr "+
                 " having sum(i44.i44_suma_db-i44.i44_suma_cr)<>0) group by I44_KODAS_KS order by I44_KODAS_KS ";

            stmt = con.prepareStatement(sql);
             //stmt.setInt(1, Integer.parseInt(agent.getID()));
             //stmt.setDate(2, getStartingDate());
             ResultSet	      rs1 = stmt.executeQuery();

            while (rs1.next()) {
              String id = rs1.getString("klient").trim();
              double debt = Double.parseDouble(rs1.getString("summa").trim());
                volad.put(id, debt);
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
              if (acc != null && acc.trim().length()>0){
              	accountnum = accountnum + "-" + acc.trim();
              }
              String name = rs.getString("nimetus").trim();
              String name2 = rs.getString("nimetus2");
              if(name2 != null) {
            	  name = name +" "+ name2.trim();
              }
              
              name = name + " " + accountnum;
              
              
              Customer cust = new Customer(accountnum);
           //   System.out.println("Name " + name);
              if(volad.get(maks) != null){
            	  cust.setDebt(Double.parseDouble("" +volad.get(maks)));
              }
              
            
              String aadr = rs.getString("aadress");
              if(aadr != null && aadr.trim().length()>0){
                cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim()),"Lithuania"));
                name = name + " " + cust.getAddress();
              }
              cust.setName(Converter.enCodeEst(name).replaceAll("  ", " ").replaceAll("  ", " "));

              String discStr = rs.getString("ale");
              if (discStr != null || discStr.trim().length() >0 ){
            	  try{
            		     cust.setDiscount(Double.parseDouble(discStr.trim()));
            	            		  
            	  }catch(Exception e){
            		  
            	  }
              }
              
              String visit = rs.getString("visit");
              if (visit != null && visit.trim().length()>0){
                cust.setVisit(visit.trim());
              }
              
              
              String payer =rs.getString("payer");
              if(payer != null && payer.trim().length()> 0){
                cust.setPayer(payer.trim());
              }
              
             if(volad.get(payer) != null){
            	 
             }
             
             cust.setGroup(maks);

             String ag = rs.getString("agent").trim();
              cust.addAgent(new Agent(ag));
              if(ag.equals("00026") || ag.equals("00028") ){
            	  cust.addAgent(new Agent("00024"));  
              }
             
              
          	Agent agg = ((Agent)this.getAgents1().get(ag));
				
				if(agg != null && agg.getDubbler().size()>0){
					//System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
					for (int r = 0 ; r < agg.getDubbler().size() ; r++){
						Agent dub = new Agent((String)agg.getDubbler().elementAt(r));
						cust.addAgent(dub);
					}
				}
			
             
             cust.setMainAgent(ag);
              try{
            	  cust.setPaymTerm(rs.getString("paymterm")+"");
                    
              }catch(Exception e){
            	  
              }
              
              String phone = rs.getString("phone");
              if(phone != null && phone.trim().length() > 0){
            	  cust.setPersonalData("Tel: " + phone.trim());
              }
              
              String mail = rs.getString("mail");
              if(mail != null && mail.trim().length() > 0){
            	  if(cust.getPersonalData() != null && cust.getPersonalData().length() > 0) {
            		  cust.setPersonalData(cust.getPersonalData() + " " + phone.trim());
            	  }else {
            		  cust.setPersonalData(mail.trim());
            	  }
            	 
              }
              
              customers.put(name, cust);
              custMap.put(cust.getPayer(), cust);
       
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

    TreeMap<String,Double> prices = new TreeMap<String,Double>();
    
    public TreeMap getProducts(Agent agent){
    	getDealerProductsList();
    	prices = new TreeMap<String,Double>();
    	System.out.println("stocks suurus " + stocks.size());
    	
    	String groupsok =  getParent().getProps().getProperty("groups.ok");
    	HashSet<String> okgr = new HashSet<String>();
    	if(groupsok != null) {
    		StringTokenizer st = new StringTokenizer(groupsok,";");
    		while(st.hasMoreTokens()) {
    			okgr.add(st.nextToken());
    		}
    	}
    	
  
   String cmd = " SELECT N17_KODAS_PS AS toode,trim(N17_PAV)  AS nimetus,N27_KIEKIS_PAK  AS tykke, 0 AS myygihind," +
   		"N17_KODAS_PS AS ean, "+
   " 0 AS kogus,N17_KODAS_MS AS grupp, N17_KODAS_GS AS grupp2 ,N17_MEN_PAV AS minkogus ,N17_KODAS_LS_4 AS status , N17_MAX_NUOL as maks," +
   " case when N17_DATA_NAUJA>getdate() AND N17_AR_NAUJA=1 then 1 else 0 end AS uus " +
   " FROM n17_prod a  LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps where  N17_POZ_DATE !=1" +
   " and N17_PAV is not null " ;
     
            System.out.println(cmd);
        TreeMap products = new TreeMap();

        try {
       
        	
    String    sql = " select I33_KODAS_PS AS toode, max(I33_KAINA) AS hind from I33_PKAI " + 
    		"    		 where I33_KODAS_iS = '010000000049'  GROUP BY I33_KODAS_PS having max(I33_KAINA) > 0 " ;
    
            System.out.println("hind");
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
             

        	
        	
             connectionUp(url2);
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            
            
            while (rs.next()) {
            	
            	boolean goes = true;
              String kood = rs.getString("toode").trim();
              Product prod = new Product(kood);
              String nimetus = rs.getString("nimetus").trim();
              prod.setName(Converter.enCodeEst(nimetus));
              try{
              prod.setInCases(rs.getDouble("tykke"));
            } catch (Exception e){}
            
            if(tooted.get(kood) != null){
            	Toode tt = (Toode)tooted.get(kood);
            	  prod.setPrice(tt.getKastis());
                  prices.put(kood, tt.getKastis());
            }
             // String price = rs.getString("iii");

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
            	  prod.setStock(stocks.get(prod.getID())); 
              }
              
              prod.setUnit("VNT");
              
              if(stocksabi.get(prod.getID()) != null){
            	  prod.setPartii((String)stocksabi.get(prod.getID())); 
              }
              
              
              String grupp = rs.getString("grupp");
             if (grupp != null && grupp.trim().length()>0){
               prod.setGroup(grupp.trim());
               if(!okgr.contains(grupp.trim())) {
            	   goes = false;
               }
             }
             String grupp2 = rs.getString("grupp2");
                    if (grupp2 != null && grupp2.trim().length()>0){
                    	if(grupp2.trim().length() > 4) {
                    		grupp2 = grupp2.substring(0,4);
                    	}
                     // prod.setGroupSecond(grupp2.trim());
                    }
           
               
               if(nimetus.startsWith("X ")) {
            	   prod.setStatusSellout("1");
            	   if(stocksabi.get(prod.getID()) == null && stocks.get(prod.getID()) == null){
            		   goes = false;
            	   }
               }
              
               double max = rs.getDouble("maks");
               if(max > 0){
               	if(max < 1){
               		max = 2;
               	}
               	int uus = rs.getInt("uus");
               	
               	if(uus > 0){
               		prod.setName("t " + nimetus);
               		//prod.setStatusTop("1");
               	}
               	//prod.setMaxDiscount(max);
               }
               prod.setVat("21");
               if(goes) {
            	   products.put(nimetus, prod);
               }
             

                // }
            }
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
				"I06_DOK_DATA AS kuupaev ,max(I07_SUMA/case when I07_KIEKIS =0 then 1 else I07_KIEKIS/I07_FRAKCIJA  end ) as lastprice FROM i07_pard p" +
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
        //  stmt.setDate(1, getStartingDate());
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
        	try{
        		Email mail = new Email(null);
        		mail.setSMTP(getParent().getProps().getProperty("smtphost"));
        		mail.setFilesTo("info@juara.lt");
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
        } catch (Exception e) {
            getParent().showError(e);
        }

        setSales(sales);
        return 0;
    }




    public void setBulkSales(Map agents){
    	
    	 connectionUp(url2);
    	 
       String cmd = " SELECT  I06_KODAS_MS AS agent , SUM(case when I06_OP_TIP = '52' then -I06_SUMA else 0 end) + " +
                     "SUM(case when I06_OP_TIP = '51' then I06_SUMA else 0 end)  AS pluss  FROM i06_parh  WHERE " +
                     "month(I06_OP_DATA)=month(getdate()) AND year(I06_OP_DATA)=year(getdate()) and I06_PERKELTA='2'" +
                     " and I06_R_DATE <= getdate() " +
                     "GROUP BY  I06_KODAS_MS";

       log(cmd);

         try {
        	 
        	 
    

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
               if(agentID.equals("00028") || agentID.equals("00026")) {
            	   agentID = "00024";
               }
               
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
                	// agent.setStart("Pradelstos skolos : " +overvolg + " eur|" ); 
                  }          

                 if(yldvolg != 0){
                	 //agent.setStart(agent.getStart() + "Visa skola : " +yldvolg + " eur.|" );
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

    public static final String DRIVER_VERSION = "RivileCeryx";
    private String             url = "jdbc:odbc:juara";
    private String	       url2 = "jdbc:odbc:juara";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

