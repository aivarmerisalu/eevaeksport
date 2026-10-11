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
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
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
public class RivileAutoDBDriver extends AbstractDBDriver implements  BulkUpdater ,Discounter, PictureMaker{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public RivileAutoDBDriver(EksportPanel parent) {
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

    public String getAssortmentStr(Agent agent){
    	return null;
    }
            public String getDealerCustomerCSV(String agent) {
		return null;
	}

          
        	
            private void makePictureCatUus(){
            	    
              	
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
//                   	 Lõppes kustutamine 
            		 
            		 TreeMap prods = this.getProducts(null);

                   StringBuffer buf = new StringBuffer();
                   Iterator enume = prods.keySet().iterator();
              
                   while (enume.hasNext()) {
              	    	String asi = (String)enume.next();
              	    	Product prod = (Product)prods.get(asi);
              	    	String toode = prod.getEAN().trim();  
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
              
            	 System.exit(0);
            	 
            }
              


         
         
            
     public String getSaveDebtInfo() {
    	 
    	 HashMap volad = new HashMap();
     
    	 try {
     
             String sql =  "SELECT I44_KODAS_KS AS klient, sum(i44_suma_db-i44_suma_cr) AS summa  FROM i44_skol,n08_klij WHERE " +
                " i44_skol.i44_kodas_ks=n08_klij.n08_kodas_ks and " +
                " exists(select i44_kodas_ks,i44_dok_nr,sum(i44_suma_db-i44_suma_cr) as suma from i44_skol i44 where i44.i44_kodas_ks=i44_skol.i44_kodas_ks " +
                " and i44.i44_dok_nr=i44_skol.i44_dok_nr and i44_tipas<>11 and i44.i44_data_mok <= getdate() group by i44.i44_kodas_ks,i44.i44_dok_nr "+
                  " having sum(i44.i44_suma_db-i44.i44_suma_cr)<>0) group by I44_KODAS_KS order by I44_KODAS_KS ";
             con = DriverManager.getConnection(url,"dba","sql");
             Statement st = con.createStatement();
              ResultSet	rs1 = st.executeQuery(sql);

             while (rs1.next()) {
               String id = rs1.getString("klient").trim();
               double debt = Double.parseDouble(rs1.getString("summa").trim());
                 volad.put(id, debt);
             }
             rs1.close();
             st.close();
         	
     }catch(Exception e){
    	 e.printStackTrace();
     }
        		
       String  cmd3 = "SELECT  trim(N08_KODAS_KS) AS kood1, (case when (N33_kodas_ak) is null then '1' else trim((N33_kodas_ak)) end) AS kood, "
          	  		+ "  L02_kodas_fs as blokk " +
     				" FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS " +
     				" left join L02_FILD on L02_kodas_fil = N08_KODAS_KS  order by N08_PAV";
          	  
        		StringBuffer products = new StringBuffer();
        		try {

        			connectionUp();
        			Statement st = con.createStatement();
        			ResultSet rs = st.executeQuery(cmd3);

        			while (rs.next()) {
        				
        				 String accountnum = rs.getString("kood1").trim();
        				
        					 
        				
        				 double volg =  0;
        				 try{
        				  volg = Double.parseDouble(volad.get(accountnum)+ "");
        				 }catch(Exception r){
         					//r.printStackTrace(); 
         				 }
        	              String acc = rs.getString("kood");
        	              if (acc != null && acc.trim().length()>0){
        	              	accountnum = accountnum + "-" + acc.trim();
        	              }

        				String kood = accountnum;
        				
        				String blokk = rs.getString("blokk");
        				String bl = "0";
        				if(blokk!= null && blokk.equals("1")){
        					bl = "1";
        				}
        				if(blokk!= null && blokk.equals("2")){
        					bl = "2";
        				}
        				
        				
        				if(volg != 0 || !bl.equals("0"))
        					products.append(kood + ";"
        							+ getParent().getNF().format(volg) + ";" + bl
        							+ "\n");
        				
                          
        			}
        			
        			
        			rs.close();
        		} catch (Exception e) {
        			getParent().showError(e);
        		}
        		
        		this.connectionDown();
        		return products.toString();

        	}

               
        public Vector getFixedAssortmentData() {
          return null;
        }

	public String getCustProdDiscounts(Agent agent) {
		return null;
	}

	public Vector getPriceMatrixData() {
		Vector lines = new Vector();

		String sql = " SELECT N51_KODAS_KS AS grupp,N52_KODAS AS toode ,N52_KAINA as price " +
         " FROM N51_SUTH p INNER JOIN N52_SUTD r ON N51_KODAS_KT=N52_KODAS_KT where N52_POZ_KAINA =1 and N52_KAINA != 0 " +
                  "  order by N51_KODAS_KS,N52_KODAS";

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
         " sum(i44_suma_db-i44_suma_cr) AS tasumata ,max(I44_DATA_DOK) AS kuupaev, max(getdate()-I44_DATA_MOK) AS yle FROM i44_skol,n08_klij WHERE " +
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
      connectionUp(this.url);
    }



    public String getSaveStockInfo(){
        
  	  String  cmd = "  SELECT  I17_KODAS_PS AS toode, sum(I17_kiekis - I17_atiduota) AS STOCK from i17_vpro inner join n17_prod on  I17_KODAS_PS = N17_KODAS_PS " +
   " where I17_KODAS_IS in ('021') AND ((I17_kiekis - I17_atiduota) >0 OR (I17_P_PAR_K)>0) and n17_kodas_ls_1<>''  and  N17_POZ_DATE =0 " +
	 "   GROUP BY I17_KODAS_PS  having sum(I17_kiekis - I17_atiduota ) >0   ORDER BY I17_KODAS_PS" ;
  	   
  	 /* cmd = "  select toode ,sum(kog) as stock,ladu from ( " +
  				" SELECT I17_KODAS_PS AS toode, (I17_KIEKIS) AS kog , I17_KODAS_IS AS LADU " +
  	               " from i17_vpro " +
  				   "  where (I17_KIEKIS) != 0 ) as aa group by ladu,toode  ORDER BY ladu,toode ";*/

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

           con = DriverManager.getConnection(url,"dba","sql");
      } catch (Exception e) {
            getParent().showError(e);
      }

    }

    
    
    
    public TreeMap getProdGroups() throws SQLException {

      connectionUp(url2);

      TreeMap tm = new TreeMap();

            String cmd = "  select distinct n17_kodas_ls_2 as kood ,n35_pav as nimi from " +
                       " n17_prod,n35_lgst where n17_kodas_ls_2 = n35_kodas_ls ";
       try {
        stmt = con.prepareStatement(cmd);
        ResultSet rs = stmt.executeQuery();
      // setTreeGroup(true);
        while (rs.next()) {
          String liik = rs.getString("kood").trim().replace(';', ':');
          String nimetus =  Converter.cleanLitLatEst(rs.getString("nimi").trim());
         
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
        connectionUp(url2);

        TreeMap tm = new TreeMap();

              String cmd = " SELECT  N19_KODAS_GS AS kood ,N19_PAV  AS nimi   FROM  n19_pgr WHERE n19_lygis = 2  ORDER BY N19_KODAS_GS ";
         try {
          stmt = con.prepareStatement(cmd);
          ResultSet rs = stmt.executeQuery();
        // setTreeGroup(true);
          while (rs.next()) {
            String liik = rs.getString("kood").trim().replace(';', ':');
            String nimetus = rs.getString("nimi").trim();
            if (liik != "") {
              tm.put(liik, Converter.enCodeEst(nimetus));
            }
          }
        }
        catch (Exception e) {
          getParent().showError(e);
        }
        connectionDown();
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

    	
        DealerProduct dealerProds = new DealerProduct();
        try {

          String  sql = "  select toode ,sum(kog) as stock,ladu from ( " +
			" SELECT I17_KODAS_PS AS toode, (I17_KIEKIS) AS kog , I17_KODAS_IS AS LADU " +
               " from i17_vpro " +
			   "  where (I17_KIEKIS) != 0 ) as aa group by ladu,toode  ORDER BY ladu,toode ";
				 
				

             if (isDebug()) System.out.println(sql);
             connectionUp(url2);
                Statement stm = con.createStatement();
                ResultSet rst = stm.executeQuery(sql);

                  while (rst.next()) {
                    String toode = rst.getString("toode").trim();
                    int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                    String ladu  =  rst.getString("ladu").trim();
                   // String real  =  rst.getString("real");
                    if(ladu.equals("021")){
                    	stocks.put(toode, seis);
                    }
                    
                   
                    dealerProds.add(ladu, toode,seis+""," "," " );
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
        connectionUp(url2);
        String  cmd = "SELECT N15_KODAS_MS AS kood ,N15_PAV AS nimi,N15_PASTABOS AS objekt  FROM  n15_men ORDER by N15_PAV";
        TreeMap agents = new TreeMap();
        Properties props = EksportFrame.loadProps(parent.getProps().getProperty("emailfile"));
        StringBuffer sb2 = new StringBuffer();
        try {
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("kood").trim());
              String name = rs.getString("nimi").trim() + " " + agent.getID();
              String ob = rs.getString("objekt");
            //  if(ob != null && ob.trim().length()>0){
              	
              /*	if(ob.trim().equals("SIAULIAI")){
              		ob = "03";
              	}else if(ob.trim().equals("KAUNAS")){
              		ob = "02";
              	}if(ob.trim().equals("VILNIUS")){
              		ob = "01";
              	}*/
            	  
            	  if(props.containsKey(agent.getID())){
              	    sb2.append(agent.getID() + ";" + name + "\n");  
                }
                agent.setObject(ob.trim());
             // }
              agent.setName(name);
              agent.setIsPilot(true);
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

    public TreeMap getCustomers(Agent agent){
        connectionUp(url2);
        String cmd = "SELECT  trim(N08_KODAS_KS) AS kood1, (case when (N33_kodas_ak) is null then '1' else trim((N33_kodas_ak)) end) AS kood, " +
        		" trim(N08_PAV) + ' ' + trim(N33_kodas_ak)AS nimetus, " +
				" N33_ADRESAS AS aadress, (case when N08_NUOL_GR ='JURA' then '0' else trim(N08_NUOL_GR) end)  AS ale , " +
				" ' ' AS visit ,N08_KODAS_KS AS payer ,  " +
				" (CASE WHEN LENGTH(TRIM(N33_SHIP_VIA))=0 or N33_SHIP_VIA IS NULL THEN  N08_KODAS_MS_P ELSE N33_SHIP_VIA END) AS agent ,  " +
				" (case when N08_KODAS_TS_P in ('REAL','SEKAN','ISANKSTINIS','NER') then '0' else trim(N08_KODAS_TS_P) end) as paymterm , N33_TEL as phone ,L02_kodas_fs as blokk " +
				"  FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS " +
				" left join L02_FILD on L02_kodas_fil = N08_KODAS_KS  order by N08_PAV";
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
              String name = rs.getString("nimetus").trim() + " " + accountnum;
              Customer cust = new Customer(accountnum);
              if(volad.get(maks) != null){
            	  cust.setDebt(Double.parseDouble("" +volad.get(maks)));
              }
              
            
              String aadr = rs.getString("aadress");
              if(aadr != null && aadr.trim().length()>0){
                cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim()),"Lithuania"));
                name = name + " " + cust.getAddress();
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
              if(ag.equals("2001")){
            	  Enumeration enums = getParent().getAgentsHash().elements();
                  while (enums.hasMoreElements()) {
                    Agent a = (Agent) enums.nextElement();
                    cust.addAgent(a);
                    }
             
              }
             
              cust.setMainAgent(ag);
              cust.setPaymTerm(rs.getInt("paymterm")+"");
              
              String phone = rs.getString("phone");
              if(phone != null && phone.trim().length() > 0){
            	  cust.setPersonalData("Tel: " + phone.trim());
              }
              

              String blokk = rs.getString("blokk");
              if(blokk != null && blokk.trim().length() > 0 && blokk.trim().equals("NEPARD")){
            	  cust.setSalesBlockT(true);
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
    	  nf.setMinimumFractionDigits(2);
    	getDealerProductsList();
    	System.out.println("stocks suurus " + stocks.size());
  
   String cmd = " SELECT N17_KODAS_PS AS toode,trim(N17_PAV)  AS nimetus,N27_KIEKIS_PAK  AS tykke, 0 AS myygihind," +
   		"N37_BAR_KODAS AS ean, "+
   " 0 AS kogus,n17_kodas_ls_2 AS grupp, N17_KODAS_GS AS grupp2 ,N17_MEN_PAV AS minkogus ,N17_KODAS_LS_4 AS status , N17_MAX_NUOL as maks," +
   " case when N17_DATA_NAUJA>getdate() AND N17_AR_NAUJA=1 then 1 else 0 end AS uus " +
   " FROM n17_prod a  LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps " +
   " LEFT JOIN  n37_pmat ON n37_kodas_ps=n17_kodas_ps where  N17_POZ_DATE !=1 and n17_kodas_ls_2 !='' " +
   "  " ;
     
            System.out.println(cmd);
        TreeMap products = new TreeMap();

        try {
        	
        	
        	
    String    sql = "select I33_KODAS_PS AS toode, max(I33_KAINA) AS hind from I33_PKAI GROUP BY I33_KODAS_PS order by I33_KODAS_PS " ;
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
             
   sql = "  SELECT  I17_KODAS_PS AS toode  ,sum( I17_SUMA) as cost,sum(I17_kiekis) as kogus  from i17_vpro inner join n17_prod on  I17_KODAS_PS = N17_KODAS_PS  \r\n" + 
   		"   		    where  (I17_kiekis >0 OR (I17_P_PAR_K)>0) and n17_kodas_ls_1<>''  and  N17_POZ_DATE =0  \r\n" + 
   		"   			   GROUP BY I17_KODAS_PS  having sum(I17_kiekis  ) >0   ORDER BY I17_KODAS_PS";
        	
        	
   TreeMap tooted2 = new TreeMap();
   stm = con.createStatement();
     rst = stm.executeQuery(sql);

      while (rst.next()) {
        String toode = rst.getString("toode").trim();
        double ohind = rst.getDouble("cost");
        double kogus = rst.getDouble("kogus");
       	 Toode tt = new Toode(toode);                
            tt.setKastis(ohind);
            tt.setDonecur(kogus);
            tooted2.put(toode,tt);	
    }
    rst.close();
    stm.close();
   
   
             connectionUp(url2);
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            
            
            while (rs.next()) {
              String kood = rs.getString("toode").trim();
              Product prod = new Product(kood);
              String nimetus = rs.getString("nimetus").trim();
              prod.setName(Converter.enCodeEst(nimetus));
              prod.setNameSorting(Converter.cleanLitLatEst(nimetus));
              try{
              prod.setInCases(rs.getDouble("tykke"));
            } catch (Exception e){}
            
            if(tooted.get(kood) != null){
            	Toode tt = (Toode)tooted.get(kood);
            	  prod.setPrice(tt.getKastis()/1.21);
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
            	  prod.setPartii("Vil: " + stocks.get(prod.getID())); 
              }
              
              String grupp = rs.getString("grupp");
             if (grupp != null && grupp.trim().length()>0){
               prod.setGroup(grupp.trim().replace(';', ':'));
             }
             String grupp2 = rs.getString("grupp2");
                    if (grupp2 != null && grupp2.trim().length()>0){
                      prod.setGroupSecond(grupp2.trim().replace(';', ':'));
                    }
           
               double max = rs.getDouble("maks");
               if(max > 0){
               	if(max < 1){
               		max = 2;
               	}
               	int uus = rs.getInt("uus");
               	
               	if(uus > 0){
               		prod.setName("t " + nimetus);
               		}
               }
               prod.setVat("21");
               
               	
               
               Toode tt2 = (Toode) tooted2.get(prod.getID());
               
               if(tt2 != null) {
            	   
             Double oma =  tt2.getKastis() / tt2.getDonecur();
               
               
           
              	 prod.setMargin(nf.format((100-(oma/prod.getPrice()*100))) + "");
              	 if(prod.getPartii() != null && prod.getPartii().trim().length() > 0){
              		 prod.setPartii(prod.getPartii() + "|Cost: " + nf.format(oma) + " €"); 
              	 }else{
              		 prod.setPartii("Cost: " + nf.format(oma) + " €"); 
              	 }
               
               }
               
               
               products.put(nimetus, prod);

                // }
            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        return products;
    }

    
    private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
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
        
        String cmd =" SELECT trim(I06_KODAS_KS)  AS klient,(case when (N33_kodas_ak) is null then '1' else trim((N33_kodas_ak)) end) as kood," +
        		"I07_KODAS AS toode,I07_KIEKIS AS kogus, " +
				"I06_DOK_DATA AS kuupaev FROM i07_pard p" +
				"  INNER JOIN i06_parh ON p.I07_KODAS_PO=I06_KODAS_PO "+
				" left JOIN N33_KBAN ON N33_KODAS_KS = I06_KODAS_KS AND I06_ADR2=N33_ADRESAS " +
				" WHERE I06_DOK_DATA > ? "+
				" GROUP BY I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else trim((N33_kodas_ak)) end),I07_KODAS,I06_DOK_DATA,I07_KIEKIS";
    
        if (isDebug()) {
           System.out.println(cmd);
       }

       Vector custProds = new Vector();
       HashMap kliendid = new HashMap();


       try {
          stmt = con.prepareStatement(cmd);
          stmt.setDate(1, getStartingDate());
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
        		mail.setFilesTo("apskaita@autoarma.lt");
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
                	 agent.setStart("Pradelstos skolos : " +overvolg + " €.|" ); 
                  }          

                 if(yldvolg != 0){
                	 agent.setStart(agent.getStart() + "Visa skola : " +yldvolg + " €.|" );
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

    public static final String DRIVER_VERSION = "RivileAuto";
    private String             url = "jdbc:odbc:B2B";
    private String	       url2 = "jdbc:odbc:B2B";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
	
    @Override
	public void PictureMaker() {
		this.makePictureCatUus();
	}

}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

