/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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

public class RivileArktisDBDriver extends AbstractDBDriver implements  BulkUpdater ,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public RivileArktisDBDriver(EksportPanel parent) {
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
        
        odbc = parent.getProps().getProperty("odbc");
        
        user =  getParent().getProps().getProperty("rest.user");
        pass = getParent().getProps().getProperty("rest.pass");
        rcompanyx = getParent().getProps().getProperty("rest.companyx");
     
         }
      
      String user =  null;
      String pass = null;
      String rcompanyx = null;

    public String getAssortmentStr(Agent agent){
    	return null;
    }
            public String getDealerCustomerCSV(String agent) {
		return null;
	}
            public String getSaveDebtInfo() {
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

		
		String sql = "SELECT n32_tipas as grupp ,max(n32_g_date),n32_kodas_ps as toode ,(n32_kaina1) as price " +
  		" FROM dbo.N32_PABC " +
  		" where n32_g_date < getdate() group by  n32_kodas_ps,n32_tipas,n32_kaina1 order by n32_tipas ,n32_kodas_ps";
			

		Statement st = null;
		try {
			connectionUp();
			st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {

				String clGroup = rs.getString("grupp");
				String prodID = rs.getString("toode");
			
				double price = rs.getDouble("price");
				MatrixLine mline = new MatrixLine();

				
				if (mline.getCustID() == null && clGroup != null
						&& clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
					mline.setStartDate(Converter.getYYYYpMMpDDpString(new Date()));
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
			con.close();
		} catch (SQLException ex) {
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
        debt.setPayerID(debt.getCustID());
        String number = rs.getString("number").trim();
       
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
      connectionUp(this.url + odbc);
    }



    public String getSaveStockInfo(){
    	
    	String ladu = "('CRB','CER')";
        
  	  String  cmd = "  SELECT  I17_KODAS_PS AS toode, sum(I17_kiekis - I17_atiduota) AS STOCK from i17_vpro inner join n17_prod on  I17_KODAS_PS = N17_KODAS_PS " +
" where I17_KODAS_IS in " + ladu + " AND ((I17_kiekis - I17_atiduota) >0 OR (I17_P_PAR_K)>0) and n17_kodas_ls_1<>''  and  N17_POZ_DATE =0 " +
	 "   GROUP BY I17_KODAS_PS  having sum(I17_kiekis - I17_atiduota ) >0   ORDER BY I17_KODAS_PS" ;


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

           con = DriverManager.getConnection(url,"sa","Rivile_2023");
      } catch (Exception e) {
            getParent().showError(e);
      }

    }

    
    
    public TreeMap getProdGroups() throws SQLException {

      connectionUp(url2+odbc);

      TreeMap tm = new TreeMap();

      String cmd = " SELECT  N19_KODAS_GS AS kood ,N19_PAV  AS nimi  FROM  n19_pgr ORDER BY N19_KODAS_GS";
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
        TreeMap tm = new TreeMap();
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
    
    private HashMap stocks = new HashMap();
    public DealerProduct getDealerProductsList() {
    	stocks = new HashMap();

        DealerProduct dealerProds = new DealerProduct();
        try {

        	
        	String ladu = "('1')";
        	if(odbc.equals("rivilefrost")) {
        		ladu = "('01')";
        	}
        	
          String  sql = " SELECT  I17_KODAS_PS AS toode, sum(I17_KIEKIS)-sum(I17_ATIDUOTA) AS STOCK   " + //,'02','03'
            "   from i17_vpro where I17_KODAS_IS in " + ladu + " and I17_KIEKIS > 0  group by I17_KODAS_PS ORDER BY I17_KODAS_PS" ;

             if (isDebug()) System.out.println(sql);
             connectionUp(url2+odbc);
                Statement stm = con.createStatement();
                ResultSet rst = stm.executeQuery(sql);

                  while (rst.next()) {
                    String toode = rst.getString("toode").trim();
                    int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                    stocks.put(toode, seis+ "");
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
        connectionUp(url2+odbc);
        String  cmd = "SELECT N15_KODAS_MS AS kood ,N15_PAV AS nimi,N15_TEVAS AS objekt  FROM  n15_men ORDER by N15_PAV";
        TreeMap agents = new TreeMap();
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
              agents1.put(agent.getID(), agent);
              agents.put(name, agent);

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
    	
    	

   	 try{
   		 if(rcompanyx != null)
     	   Converter.getManagersReplacements(agents1, rcompanyx, user , pass);
     	        	
     }catch(Exception e){
     	e.printStackTrace();
     }
   	 
        connectionUp(url2+odbc);
        String cmd = "SELECT  rtrim(N08_KODAS_KS) AS kood1, (case when (N33_kodas_ak) is null then '1' " +
" else rtrim((N33_kodas_ak)) end) AS kood,  rtrim(N33_kodas_ak) AS nim , rtrim(N08_PAV) as nimetus , " +
"  N33_ADRESAS AS aadress , N08_ADR as aard2,  (case when N08_NUOL_GR ='JURA' then '0' else rtrim(N08_NUOL_GR) end)  AS ale ,  ' ' AS visit ,N08_KODAS_KS AS payer , " +
"  (CASE WHEN LEN(rTRIM(N33_SHIP_VIA))=0 or N33_SHIP_VIA IS NULL THEN  N08_KODAS_MS_P ELSE N33_SHIP_VIA END) AS agent , " +
"    N08_KODAS_TS_P as paymterm " +
" , N33_TEL as phone ,N08_KAINYNAS as hinnad FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS  order by N08_PAV";
        System.out.println(cmd);
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
              
            
              
              String name = rs.getString("nimetus").trim()  + accountnum;
              Customer cust = new Customer(accountnum);
              if(volad.get(maks) != null){
            	  cust.setDebt(Double.parseDouble("" +volad.get(maks)));
              }
              
            
              String aadr = rs.getString("aadress");
              if(aadr != null && aadr.trim().length()>0){
                cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim()),"Lithuania"));
                name = name + " " + cust.getAddress();
              }else{
            	aadr = rs.getString("aard2");
                  if(aadr != null && aadr.trim().length()>0){
                    cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim()),"Lithuania"));
                    name = name + " " + cust.getAddress();
                  } 
              }
            
              
              cust.setName(Converter.enCodeEst(name));

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

             String ag = rs.getString("agent").trim();
              cust.addAgent(new Agent(ag));
              if(ag.equals("2561") || ag.equals("2562") || ag.equals("2563")){
            	  cust.addAgent(new Agent("255"));
                  cust.addAgent(new Agent("2565"));  
              }
             
              cust.setMainAgent(ag);
              
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
              
              cust.setPaymTerm(rs.getString("paymterm"));
              
              String phone = rs.getString("phone");
              if(phone != null && phone.trim().length() > 0){
            	  cust.setPersonalData("Tel: " + phone.trim());
              }
              
              String hinnak = rs.getString("hinnad");
              if(hinnak != null && hinnak.trim().length() > 0){
            	  cust.setCustType(hinnak.trim());  
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

    public TreeMap getProducts(Agent agent){
    	getDealerProductsList();
    	System.out.println("stocks suurus " + stocks.size());
  
   String cmd = " SELECT N17_KODAS_PS AS toode,rtrim(N17_PAV)  AS nimetus,N27_KIEKIS_PAK  AS tykke, 0 AS myygihind, \r\n" + 
   		"           N37_BAR_KODAS AS ean, \r\n" + 
   		"   0 AS kogus,n17_kodas_ls_1 AS grupp, N17_KODAS_GS AS grupp2 ,N17_MEN_PAV AS minkogus ,N17_KODAS_LS_4 AS status , N17_MAX_NUOL as maks, \r\n" + 
   		"    case when N17_DATA_NAUJA>getdate() AND N17_AR_NAUJA=1 then 1 else 0 end AS uus ,N17_KODAS_US as yhik , N17_INTERNET as stat \r\n" + 
   		"    FROM n17_prod a  LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps left join n37_pmat on  N37_KODAS_PS= N17_KODAS_PS where  N17_POZ_DATE !=1 " ;
     
            System.out.println(cmd);
        TreeMap products = new TreeMap();

        try {
        	
        	
        	
    String    sql = "select I33_KODAS_PS AS toode, (I33_KAINA) AS hind from I33_PKAI where I33_KAINA != 0 order by I33_KODAS_PS" ;
            System.out.println("hind");
          if (isDebug()) System.out.println(sql);
          connectionUp(url2+odbc);
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
             

        	
        	
             connectionUp(url2+odbc);
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            
            
            while (rs.next()) {
              String kood = rs.getString("toode").trim();
              Product prod = new Product(kood);
              String nimetus = rs.getString("nimetus").trim();
              prod.setName(Converter.enCodeEst(nimetus).replace('~', '-'));
              try{
              prod.setInCases(rs.getDouble("tykke"));
            } catch (Exception e){}
            
            if(tooted.get(kood) != null){
            	Toode tt = (Toode)tooted.get(kood);
            	  prod.setPrice(tt.getKastis());
            }
             // String price = rs.getString("iii");

              String ean = rs.getString("ean");
              if(ean != null && ean.trim().length()>0){
            	  try{
            		  EAN13CheckDigit ee = new EAN13CheckDigit();
            		  String ean2 = ean.trim() + ee.calculate(ean.trim());
                      prod.setEAN(ean2);	
            		  prod.setName(prod.getName());
            	  }catch(Exception y){
            		  
            	  }
            
              }
       
             
              
            
             String grupp2 = rs.getString("grupp2");
                    if (grupp2 != null && grupp2.trim().length()>0){
                      prod.setGroup(grupp2.trim());
                    }
               String minam = rs.getString("minkogus");
            /*   double res = 0;
               if (minam != null && minam.trim().length()>0){
               	try{
               	if(minam.indexOf("/")!=-1){
               		res = Double.parseDouble(minam.trim().substring(0,minam.indexOf("/")));
               	}else if(minam.indexOf("*")!=-1){
               		res = Double.parseDouble(minam.trim().substring(0,minam.indexOf("*")));
               	}else{
               		res = Double.parseDouble(minam.trim());
               	}
               	}catch (Exception e){
               		e.printStackTrace();
               	}
               	prod.setInCases(res);
               }
              */ 
               
              
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
               int xx = 1;
               
               String yhik = rs.getString("yhik");
               if (yhik != null && yhik.trim().length()>0){
            	   if(yhik.trim().equals("KG")){
                       prod.setUnit("kg"); 
                       xx = 1000;
                       
            	   }else{
            		   prod.setUnit("vnt");
            	   }
               	}
               prod.setStock(0);
               
               
               String stat = rs.getString("stat");
               if (stat != null && stat.trim().length()>0){
               	if(stat.trim().equals("1")){
               		prod.setStatusTop("1");
               	}
               
               }
               
               if(stocks.get(prod.getID()) != null){
             	  prod.setStock(Double.parseDouble((String)stocks.get(prod.getID()))/xx); 
               }
               
               products.put(nimetus, prod);

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
        connectionUp(url2+odbc);
        
        String cmd ="  SELECT rtrim(I06_KODAS_KS)  AS klient,(case when (N33_kodas_ak) is null then '1' else rtrim((N33_kodas_ak)) end) as kood,\r\n" + 
        		"        		I07_KODAS AS toode,I07_KIEKIS AS kogus, \r\n" + 
        		"				I06_DOK_DATA AS kuupaev ,(I07_SUMA/case when I07_KIEKIS =0 then 1 else I07_KIEKIS/I07_FRAKCIJA  end ) as lastprice FROM i07_pard p\r\n" + 
        		"				  INNER JOIN i06_parh ON p.I07_KODAS_PO=I06_KODAS_PO\r\n" + 
        		"				 left JOIN N33_KBAN ON N33_KODAS_KS = I06_KODAS_KS AND left (N33_ADRESAS,40) = I06_ADR2 \r\n" + 
        		"				 WHERE I06_DOK_DATA > ? \r\n" + 
        		"				 GROUP BY I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else rtrim((N33_kodas_ak)) end),I07_KODAS,I06_DOK_DATA,I07_KIEKIS,\r\n" + 
        		"				 (I07_SUMA/case when I07_KIEKIS =0 then 1 else I07_KIEKIS/I07_FRAKCIJA  end )\r\n" + 
        		"				  order by \r\n" + 
        		"				 rtrim(I06_KODAS_KS) ,(case when (N33_kodas_ak) is null then '1' else rtrim((N33_kodas_ak)) end) ,\r\n" + 
        		"        		I07_KODAS,I06_DOK_DATA desc ";
        if (isDebug()) {
           System.out.println(cmd);
       }

       Vector custProds = new Vector();
       HashMap kliendid = new HashMap();

  HashSet done = new HashSet();
       try {
          stmt = con.prepareStatement(cmd);
          stmt.setTimestamp(1, getStartingStamp());
           ResultSet	      rs = stmt.executeQuery();

           while (rs.next()) {
             String kliendikood = rs.getString("klient").trim();
             String kood = rs.getString("kood");
             if (kood != null && kood.trim().length()> 0){
             	kliendikood = kliendikood +  "-" + kood.trim();
             }
             
             String tootekood = rs.getString("toode").trim();
             double kogus = Double.parseDouble(rs.getString("kogus").trim());
             if(kogus > 999){
            	 kogus = kogus /1000;
             }
             String kuup = rs.getString("kuupaev").substring(5,10).trim();
             Customer cust = (Customer) kliendid.get(kliendikood);
             double lastprice = rs.getDouble("lastprice");

             if (cust == null) {
               cust = new Customer(kliendikood);
             }

             Product prod = new Product(tootekood);
             if(!done.contains(kliendikood + tootekood)) {
            	  cust.addProduct(prod,
                          new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                       kuup, lastprice, 0,0," "),false);
              kliendid.put(kliendikood, cust);
              custProds.add(new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                       kuup, lastprice, 0,0," "));
              done.add(kliendikood + tootekood); 
             }
           
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
      connectionUp(url2+odbc);
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
    	
    	 connectionUp(url2+odbc);
    	 
       String cmd = " SELECT  I06_KODAS_MS AS agent , SUM(case when I06_OP_TIP = '52' then -I06_SUMA else 0 end) + " +
                     "SUM(case when I06_OP_TIP = '51' then I06_SUMA else 0 end)  AS pluss  FROM i06_parh  WHERE " +
                     "month(I06_OP_DATA)=month(getdate()) AND year(I06_OP_DATA)=year(getdate()) and I06_PERKELTA='2'" +
                     " and I06_R_DATE <= getdate() " +
                     "GROUP BY  I06_KODAS_MS";

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
                	 agent.setStart("Pradelstos skolos : " +overvolg + " lt.|" ); 
                  }          

                 if(yldvolg != 0){
                	 agent.setStart(agent.getStart() + "Visa skola : " +yldvolg + " lt.|" );
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

    
    private String odbc = null;
    public static final String DRIVER_VERSION = "RivileArktis";
    private String             url = "jdbc:odbc:";
    private String	       url2 = "jdbc:odbc:";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

