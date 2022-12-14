/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

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
public class RivileKurybaDBDriver extends AbstractDBDriver implements  BulkUpdater ,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public RivileKurybaDBDriver(EksportPanel parent) {
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
			
			
			 String sql2 = " select  N51_KODAS_KS AS grupp, N52_KODAS as tgrupp,N52_NUOLAIDA as ale from  N51_SUTH  " +
				 "  INNER JOIN N52_SUTD r ON N51_KODAS_KT=N52_KODAS_KT where  N52_POZ_NUOLAIDA = 1 order by N51_PAV ";
			 rs = st.executeQuery(sql2);
			
			while (rs.next()) {

				String clGroup = rs.getString("grupp");
				String prodGR = rs.getString("tgrupp");
			
				double ale = rs.getDouble("ale");
				MatrixLine mline = new MatrixLine();

				
				if (mline.getCustID() == null && clGroup != null
						&& clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
					mline.setStartDate(Converter.getYYYYpMMpDDpString(new Date()));
				}

				if (prodGR != null && prodGR.trim().length() > 0) {
					mline.setProdGroup(prodGR.trim());
				}
			
				if (ale != 0) {
					mline.setDiscount(ale);
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
         " sum(i44_suma_db-i44_suma_cr) AS tasumata ,max(DateDiff(dayofyear,I44_DATA_MOK,getdate())) AS yle FROM i44_skol,n08_klij WHERE " +
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
        debt.setDoknr(number);
        debt.setDuedate(rs.getDate("maksepaev"));
        debt.setPayerID(debt.getCustID());
        debt.setSum(rs.getDouble("tasumata"));
      //  debt.setDate(rs.getDate("kuupaev"));
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
        
  	  String  cmd = "SELECT  I17_KODAS_PS AS toode, I17_KIEKIS-I17_ATIDUOTA-I17_REZERVAS AS STOCK , I17_KODAS_IS AS LADU  " + 
            "   from i17_vpro where I17_KODAS_IS in ('02','18') and I17_KIEKIS > 0  ORDER BY I17_KODAS_PS" ;


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

           con = DriverManager.getConnection(url,"sa","sql");
      } catch (Exception e) {
            getParent().showError(e);
      }

    }

    
    
    public TreeMap getProdGroups() throws SQLException {

      connectionUp(url2);

      TreeMap tm = new TreeMap();

            String cmd = "  select distinct n17_kodas_ks as kood ,n17_kodas_ks as nimi from   n17_prod where len (n17_kodas_ks)>0";
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
        connectionUp(url2);

        TreeMap tm = new TreeMap();

              String cmd = " SELECT  N19_KODAS_GS AS kood ,N19_PAV  AS nimi  FROM  n19_pgr " +
              		" WHERE (len((N19_KODAS_GS)))=3 ORDER BY N19_KODAS_GS";
         try {
          stmt = con.prepareStatement(cmd);
          ResultSet rs = stmt.executeQuery();
        // setTreeGroup(true);
          while (rs.next()) {
            String liik = rs.getString("kood").trim();
            String nimetus = rs.getString("nimi").trim();
            if (liik != "") {
              tm.put(liik,liik +" "+ Converter.enCodeEst(nimetus));
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
    	stocks = new HashMap();

        DealerProduct dealerProds = new DealerProduct();
        try {

          String  sql = " SELECT  I17_KODAS_PS AS toode, I17_KIEKIS-I17_ATIDUOTA-I17_REZERVAS AS STOCK , I17_KODAS_IS AS LADU  " + 
            "   from i17_vpro where I17_KODAS_IS in ('02','18') and I17_KIEKIS > 0  ORDER BY I17_KODAS_PS" ;
//and n17_kodas_ks not like '%*'
             if (isDebug()) System.out.println(sql);
             connectionUp(url2);
                Statement stm = con.createStatement();
                ResultSet rst = stm.executeQuery(sql);

                  while (rst.next()) {
                    String toode = rst.getString("toode").trim();
                    int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                    String ladu  =  rst.getString("ladu").trim();
                    dealerProds.add(ladu, toode,seis+""," "," " );
                    if(stocks.get(toode) != null){
                    	try{
                    		double s2 = Double.parseDouble((String) stocks.get(toode));
                    		 stocks.put(toode, (seis + s2 )+ "");
                    	}catch(Exception e){
                    		
                    	}
                    	
                    }else{
                    	 stocks.put(toode, seis+ "");
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
        connectionUp(url2);
        String  cmd = "select distinct N08_ATSTOVAS as kood ,N08_ATSTOVAS  as nimi, '' as objekt from n08_klij where N08_ATSTOVAS like '%vad%'";
        TreeMap agents = new TreeMap();
        Properties props = EksportFrame.loadProps(parent.getProps().getProperty("emailfile"));
        StringBuffer sb2 = new StringBuffer();
        try {
        	
        	 Properties propse =  new Properties();
     	    try{
     	    	propse =  EksportFrame.loadProps("emails.txt");
     	  	  
     	    }catch(Exception e){
     	    	e.printStackTrace();
     	    }
        	
        	
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("kood").trim());
              String name = rs.getString("nimi").trim();
              
            	  if(props.containsKey(agent.getID())){
                	    sb2.append(agent.getID() + ";" + name + "\n");  
                  }
            	  
            	  agent.setLanguage("LIT");
                  agent.setCountry("lt");
                  agent.setPassWord("0000");
                  agent.setIsPilot(true);
                
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
            	  
            	  
              agent.setName(name);
              agents.put(name, agent);
            }
            
            if(sb2.length() > 2){
              	try{
              		 Converter.saveFileInUTF8( "agent.txt", sb2);
              	}catch (Exception e){
              		
              	}                
               }
            Agent ad = new Agent("HOUSE");
            ad.setName("HOUSE");
            agents.put("HOUSE", ad);
  
            
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        
        if(rcompany != null){
	    	Converter.sendAgentJson(agents, rcompany, false);
	    }
        return agents;
    }

    public TreeMap getCustomers(Agent agent){
        if(aktiivsed == null){
        	makeReports();
        }
    	
    	connectionUp(url2);
        
        String cmd = "SELECT  (N08_KODAS_KS) AS kood1, (case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end) AS kood,  " +
        		" (N08_PAV) as nimi, (N33_kodas_ak) AS nimetus,   N33_ADRESAS as tanav,'' as linn, '' as regioon, '' as indeks,  " +
				" N08_ADR AS aadress, (case when N08_NUOL_GR ='JURA' then '0' else rtrim(N08_NUOL_GR) end)  AS ale , " +
			"	' ' AS visit ,N08_KODAS_KS AS payer ,   " +
			"	 N08_ATSTOVAS AS agent ,   " +
			"	 (case when N08_KODAS_TS_P in ('REAL','SEKAN','ISANKSTINIS','NER') then '0' else (N08_KODAS_TS_P) end) as paymterm ,n08_mob_tel as phone ,N08_TEL as phone2 " + 
			"	  FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS  order by N08_PAV";
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
        	
            

            String salessql = " SELECT (I06_KODAS_KS)  AS klient,"
            		+ " (case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end) as kood,sum(I07_SUMA) AS summa , " + 
			 " str(datepart(YYYY,I06_DOK_DATA))+' '+str(datepart(mm,I06_DOK_DATA)) as kuu   FROM i07_pard p " +
				"  INNER JOIN i06_parh ON p.I07_KODAS_PO=I06_KODAS_PO  " +
				" left JOIN N33_KBAN ON N33_KODAS_KS = I06_KODAS_KS AND I06_ADR2=N33_ADRESAS " +
				" WHERE I06_DOK_DATA > getdate()-185 " +
				" GROUP BY I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end),str(datepart(YYYY,I06_DOK_DATA))+' '+str(datepart(mm,I06_DOK_DATA)) " +
              " ORDER BY I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end),str(datepart(YYYY,I06_DOK_DATA))+' '+str(datepart(mm,I06_DOK_DATA)) ";
            
            HashMap salesres = new HashMap();
            
        	Statement stmr ;
        	try{
        		 
        		 stmr = con.createStatement();
            	 rs1 = stmr.executeQuery(salessql);
                
            	 while(rs1.next()){
                	String klient = rs1.getString("klient").trim() + "-"+rs1.getString("kood").trim() ;
                	
                	int summa = (int)rs1.getDouble("summa");
                	
                	String aeg = rs1.getString("kuu").trim();
                	String kuu = aeg.substring(0, 4).trim() ;
                	if(kuu.length() == 1){
                		kuu = "0" + kuu;
                	}
                	String aasta = aeg.substring(4).trim();
                	if(aasta.length() == 1){
                		aasta = "0" + aasta;
                	}
                  if(salesres.get(klient) == null){
                	  salesres.put(klient,"   " + aasta + " : " + summa + " €" );
                  }else{
                	  String asi = (String)salesres.get(klient);     	  
                	  salesres.put(klient,"   " + aasta + " : " + summa +" €|" + asi);
                  }
                }
               rs1.close();
               stmr.close();
            }catch(Exception s){
        		s.printStackTrace();
        	}
        
        	  String sql3 = " SELECT N35_KODAS_LS as kood , N35_PAV as nimi FROM"
        	  		+ " N35_LGST where  LEFT(n35_tevas,2) = '01'  ";
              
        	  stmt = con.prepareStatement(sql3);
              ResultSet	  rs = stmt.executeQuery();

               StringBuffer sb1 = new StringBuffer();
              while (rs.next()) {
            	  String kood = Converter.enCodeEst(rs.getString("kood").trim());
              	String name = Converter.enCodeEst(rs.getString("nimi").trim());
              	sb1.append(kood + ";" + name + "\n");
              }
              rs.close();
            
              
              if(sb1.toString().length() > 0) {
                try{
                      Converter.saveFileInUTF8("shipmode.txt", sb1);
                	} catch (Exception r){
                  }
              }
        	
             TreeMap<String,Double> bonus = new TreeMap<String,Double>(); 
              
           String sql4 = "  select klsu_kodas ,  viso_sukaupta    from tmp_sukaupta ";
              
        	      stmt = con.prepareStatement(sql4);
              	  rs = stmt.executeQuery();

              while (rs.next()) {
            	   String kood = Converter.enCodeEst(rs.getString("klsu_kodas").trim());
              	   Double sum = rs.getDouble("viso_sukaupta");
              	   bonus.put(kood, sum);
              }
              rs.close();
              
              
              
              stmt = con.prepareStatement(cmd);
               rs = stmt.executeQuery();


            while (rs.next()) {
              String accountnum = rs.getString("kood1").trim();
              String kkood = accountnum;
              String maks = accountnum;
              String acc = rs.getString("kood");
              if (acc != null && acc.trim().length()>0){
              	accountnum = accountnum + "-" + acc.trim();
              }
              String name = rs.getString("nimi").trim() + " " + accountnum;
              Customer cust = new Customer(accountnum);
              if(volad.get(maks) != null){
            	  cust.setDebt(Double.parseDouble("" +volad.get(maks)));
              }
          
              String tanav = rs.getString("tanav");
              String add = "";
              if (tanav != null && tanav.length() > 0){
                cust.restStreet = tanav.trim();
                add = tanav.trim();
              }
              String linn = rs.getString("linn");
              if (linn != null && linn.length() > 0){
                cust.restCity = linn.trim();
              }
              String regioon = rs.getString("regioon");
              if (regioon != null && regioon.length() > 0){
              	cust.restCity = (cust.restCity + " " + regioon.trim()).trim();
              }

              String indeks = rs.getString("indeks");
              if (indeks != null && indeks.length() > 0){
                cust.restZipCode = indeks.trim();
              }

              cust.setCountry("Lithuania");

              if(salesres.get(accountnum) != null){
              	cust.setInfo("CHART Sales :|Year Month|Sales EUR|" + salesres.get(accountnum) );
              }
             
              cust.restName = name.trim();
             
              String aadr = rs.getString("aadress");
              if(aadr != null && aadr.trim().length()>0){
                cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim()),"Lithuania"));
                name = name + " " + cust.getAddress();
              }
              
              if(add.length() > 0) {
            	  name =  cust.restName + " " + add;
              }
              
            String adde = "";
              if(aktiivsed != null && !aktiivsed.contains(accountnum)){
            	  adde = "!!! ";
              }
             
              cust.setName(adde + Converter.enCodeEst(name));
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
               	  cust.setDebt(Double.parseDouble("" +volad.get(payer)));
                 }
         
                 String ag = rs.getString("agent").trim();
              cust.addAgent(new Agent(ag));
              cust.addAgent(new Agent("vadyba"));
              cust.addAgent(new Agent("vadyba9"));
              cust.addAgent(new Agent("HOUSE"));
              cust.setMainAgent(ag);
              String paym = rs.getString("paymterm");
              if(paym != null && paym.trim().length() >0){
            	  cust.setPaymTerm(paym.trim());  
              }
             
              String phone = rs.getString("phone");
              if(phone != null && phone.trim().length() > 0){
            	  cust.setPersonalData("TelGSM : " + phone.trim());
              }
              
              
              String phone2 = rs.getString("phone2");
              if(phone2 != null && phone2.trim().length() > 0){
            	  if(cust.getPersonalData() != null && cust.getPersonalData().trim().length() >0 ){
            		  cust.setPersonalData(cust.getPersonalData() + "|Tel: " + phone2.trim());
            	  }else{
            		  cust.setPersonalData("Tel: " + phone2.trim());
            	  }
            	 
              }
              
              if(bonus.get(kkood) != null) {
            	  cust.setCity("Bonus available: " + Double.parseDouble(bonus.get(kkood)+"")+"€");
            	  cust.bonusAvailable = Double.parseDouble(bonus.get(kkood)+"");
              }
              
             //System.out.println(cust.getID() +" " + cust.getAgents().toString());
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
        
        if(rcompany != null){
      //   	Converter.sendCustomerJson(customers, rcompany,false,user,pass);
         }

          return customers;
    }

    public TreeMap getProducts(Agent agent){
    	getDealerProductsList();
    	System.out.println("stocks suurus " + stocks.size());
  
   String cmd = " SELECT N17_KODAS_PS AS toode,(N17_PAV)  AS nimetus,N27_KIEKIS_PAK  AS tykke, 0 AS myygihind," +
   		"N17_KODAS_PS AS ean, "+
   " 0 AS kogus,n17_kodas_ks AS grupp, N17_KODAS_GS AS grupp2 ,N17_MEN_PAV AS minkogus ,N17_KODAS_LS_4 AS status , N17_MAX_NUOL as maks," +
   " case when N17_DATA_NAUJA>getdate() AND N17_AR_NAUJA=1 then 1 else 0 end AS uus " +
   " FROM n17_prod a  LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps where  N17_POZ_DATE !=1 and n17_kodas_ks not like '%*'" ;
 //  "	 WHERE N32_TIPAS='A' "+
  // "   AND N17_INTERNET = 1 AND N17_KODAS_PS NOT IN   (select L02_KODAS_FIL from L02_FILD where L02_KODAS_FS='01')";
     
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
             

        	
        	
             connectionUp(url2);
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            
            
            while (rs.next()) {
              String kood = rs.getString("toode").trim();
              Product prod = new Product(kood);
              String nimetus = rs.getString("nimetus").trim();
              prod.setName(Converter.enCodeEst(nimetus));
              try{
              prod.setInCases(rs.getDouble("tykke"));
            } catch (Exception e){}
            
            if(tooted.get(kood) != null){
            	Toode tt = (Toode)tooted.get(kood);
            	  prod.setPrice(tt.getKastis()/1.21);
            }

              String ean = rs.getString("ean");
              if(ean != null && ean.trim().length()>0){
            	  try{
              		  EAN13CheckDigit ee = new EAN13CheckDigit();
            		  String ean2 = ean.trim() + ee.calculate(ean.trim());
                      prod.setEAN(ean2);	
            		//  prod.setName(prod.getName() + " " + ean2.substring(ean2.length()-4));  
            	  }catch(Exception y){
            		  
            	  }
            
              }
              prod.setStock(0);
              if(stocks.get(prod.getID()) != null){
            	  
            	  prod.setStock(Double.parseDouble((String)stocks.get(prod.getID()))); 
              }
              
              String grupp = rs.getString("grupp");
             if (grupp != null && grupp.trim().length()>0){
               prod.setGroup(grupp.trim());
             }
             String grupp2 = rs.getString("grupp2");
                    if (grupp2 != null && grupp2.trim().length()>0){
                      prod.setGroupSecond(grupp2.trim());
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
               
               String stat = rs.getString("status");
               if (stat != null && stat.trim().length()>0){
               	if(stat.trim().equals("1")){
               		//prod.setStatusNew("1");
               	}
               	if(stat.trim().equals("2")){
               		//prod.setStatusDiscount("1");
               	}
               	if(stat.trim().equals("3")){
               	//	prod.setStatusSellout("1");
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
        connectionUp(url2);
        
        String cmd =" SELECT (I06_KODAS_KS)  AS klient,(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end) as kood," +
        		"I07_KODAS AS toode,I07_KIEKIS AS kogus, " +
				"I06_DOK_DATA AS kuupaev FROM i07_pard p" +
				"  INNER JOIN i06_parh ON p.I07_KODAS_PO=I06_KODAS_PO "+
				" left JOIN N33_KBAN ON N33_KODAS_KS = I06_KODAS_KS AND I06_ADR2=N33_ADRESAS " +
				" WHERE I06_DOK_DATA > ? "+
				" GROUP BY I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end),I07_KODAS,I06_DOK_DATA,I07_KIEKIS";
    
        if (isDebug()) {
           System.out.println(cmd);
       }

       Vector custProds = new Vector();
       HashMap kliendid = new HashMap();


       try {
          stmt = con.prepareStatement(cmd);
          stmt.setTimestamp(1, new java.sql.Timestamp(getStartingDate().getTime()));
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

    private TreeMap aruandeAgendid = null;
    private HashSet aktiivsed = null;
    private HashSet passiivsed = null;

  private void makeReports(){
	   connectionUp(url2);

	   /*Kuryba
Total amount of customers:                              600
Active customers in past 30 days :                 150
Active customers be for 30d. :                               140
Difference (we must show history in 60d.) :   +10  
Active customer yesterday :      35
Active customer past 7 days  :  20
*/
	      TreeMap tm = new TreeMap();

	      String cmd = "SELECT  N08_ATSTOVAS AS agent ,rtrim(N08_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end) AS kood, " +  
	        		" (N08_PAV) as nimi  FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS where N08_ATSTOVAS !='' order by N08_PAV"; 
	      
	      try {
	        stmt = con.prepareStatement(cmd);
	        ResultSet rs = stmt.executeQuery();
	     
	        while (rs.next()) {
	        	if(aruandeAgendid == null){
	        		aruandeAgendid = new TreeMap();
	        		aktiivsed = new HashSet();
	        		passiivsed = new HashSet();
	        	}
	        	
	          String agent = rs.getString("agent").trim();
	          String klient = rs.getString("kood").trim();
	          Agent aaa = new Agent(agent);
	          if(aruandeAgendid.get(agent)!= null){
		        	aaa = (Agent) aruandeAgendid.get(agent); 
		          }
	          if(aaa.klienteKokku == null){
	        		aaa.klienteKokku = new Vector();
		        }
	        
	          aaa.klienteKokku.add(klient);
	        
	          aruandeAgendid.put(agent,aaa);
	         }
	        rs.close();
	        stmt.close();
	      System.out.println("aktiivseid " + aktiivsed.size());
	        //leiame kes on ostnud 30 päeva jooksul 
	        
	        cmd = "SELECT  N08_ATSTOVAS AS agent ,rtrim(N08_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end) AS kood, " +  
	        	"	(N08_PAV) as nimi  FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS where N08_ATSTOVAS !='' and " +
				"	rtrim(N08_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end)  in " +
				"	(SELECT  rtrim(I06_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end) as kood	    FROM  i06_parh " +
				" left JOIN N33_KBAN ON N33_KODAS_KS = I06_KODAS_KS AND I06_ADR2=N33_ADRESAS " +
				" WHERE I06_DOK_DATA > getdate()-30 GROUP BY I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end)) order by N08_PAV ";
	        
	        stmt = con.prepareStatement(cmd);
	        rs = stmt.executeQuery();
		     
	        while (rs.next()) {
	        	
	        	
	          String agent = rs.getString("agent").trim();
	          String klient = rs.getString("kood").trim();
	          if(aruandeAgendid.get(agent)!= null){
	        	Agent aaa = (Agent) aruandeAgendid.get(agent); 
	        	if(aaa.klienteAkt30 == null){
	        		aaa.klienteAkt30 = new Vector();
		        }
	        	  aaa.klienteAkt30.add(klient);
		          aruandeAgendid.put(agent,aaa);   
		          aktiivsed.add(klient);
	          }
	         }
	      
	        rs.close();
	        stmt.close();
	      
	      
	        cmd = "SELECT  N08_ATSTOVAS AS agent ,rtrim(N08_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end) AS kood, " +  
		        	"	(N08_PAV) as nimi  FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS where N08_ATSTOVAS !='' and " +
					"	rtrim(N08_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end)  in " +
					"	(SELECT  rtrim(I06_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end) as kood	    FROM  i06_parh " +
					" left JOIN N33_KBAN ON N33_KODAS_KS = I06_KODAS_KS AND I06_ADR2=N33_ADRESAS " +
					" WHERE I06_DOK_DATA > getdate()-60 and I06_DOK_DATA < getdate()-30 GROUP BY I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end)) order by N08_PAV ";
		        
		        stmt = con.prepareStatement(cmd);
		        rs = stmt.executeQuery();
			     
		        while (rs.next()) {
		        	
		          String agent = rs.getString("agent").trim();
		          String klient = rs.getString("kood").trim();
		          if(aruandeAgendid.get(agent)!= null){
		        	Agent aaa = (Agent) aruandeAgendid.get(agent); 
		        	if(aaa.klienteAkt60 == null){
		        		aaa.klienteAkt60 = new Vector();
			        }
		        	aaa.klienteAkt60.add(klient);
			          aruandeAgendid.put(agent,aaa);   
		          }
		         }
		      
		        rs.close();
		        stmt.close();
		      
		        

		        cmd = "SELECT  N08_ATSTOVAS AS agent ,rtrim(N08_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end) AS kood, " +  
			        	"	(N08_PAV) as nimi  FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS where N08_ATSTOVAS !='' and " +
						"	rtrim(N08_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end)  in " +
						"	(SELECT  rtrim(I06_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end) as kood	    FROM  i06_parh " +
						" left JOIN N33_KBAN ON N33_KODAS_KS = I06_KODAS_KS AND I06_ADR2=N33_ADRESAS " +
						" WHERE MONTH(I06_DOK_DATA) = MONTH(getdate()-30) and I06_DOK_DATA > getdate()-60 GROUP BY I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end)) order by N08_PAV ";
			        
			        stmt = con.prepareStatement(cmd);
			        rs = stmt.executeQuery();
				     
			        while (rs.next()) {
			        	
			          String agent = rs.getString("agent").trim();
			          String klient = rs.getString("kood").trim();
			          if(aruandeAgendid.get(agent)!= null){
			        	Agent aaa = (Agent) aruandeAgendid.get(agent); 
			        	if(aaa.klienteEelm == null){
			        		aaa.klienteEelm = new Vector();
				        }
			        	aaa.klienteEelm.add(klient);
				          aruandeAgendid.put(agent,aaa);   
			          }
			         }
			      
			        rs.close();
			        stmt.close();
			      
			        
			        cmd = "SELECT  N08_ATSTOVAS AS agent ,rtrim(N08_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end) AS kood, " +  
				        	"	(N08_PAV) as nimi  FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS where N08_ATSTOVAS !='' and " +
							"	rtrim(N08_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end)  in " +
							"	(SELECT  rtrim(I06_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end) as kood	    FROM  i06_parh " +
							" left JOIN N33_KBAN ON N33_KODAS_KS = I06_KODAS_KS AND I06_ADR2=N33_ADRESAS " +
							" WHERE MONTH(I06_DOK_DATA) = MONTH(getdate()) and I06_DOK_DATA > getdate()-60 GROUP BY I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end)) order by N08_PAV ";
				        
				        stmt = con.prepareStatement(cmd);
				        rs = stmt.executeQuery();
					     
				        while (rs.next()) {
				        	
				          String agent = rs.getString("agent").trim();
				          String klient = rs.getString("kood").trim();
				          if(aruandeAgendid.get(agent)!= null){
				        	Agent aaa = (Agent) aruandeAgendid.get(agent); 
				        	if(aaa.klienteSee == null){
				        		aaa.klienteSee = new Vector();
					        }
				        	aaa.klienteSee.add(klient);
					          aruandeAgendid.put(agent,aaa);   
				          }
				         }
				      
				        rs.close();
				        stmt.close();
				     

		        cmd = "SELECT  N08_ATSTOVAS AS agent ,rtrim(N08_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end) AS kood, " +
			        	"(N08_PAV) as nimi  FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS where N08_ATSTOVAS !='' and " +
						"	rtrim(N08_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end)  in " +
						"	(SELECT  rtrim(I06_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end) as kood	    FROM  i06_parh " +
						" left JOIN N33_KBAN ON N33_KODAS_KS = I06_KODAS_KS AND I06_ADR2=N33_ADRESAS " +
						" WHERE I06_DOK_DATA > (case when DATENAME(dw,GETDATE()) = 'Monday' then getdate()-4 else getdate()-2 end ) " +
						 " and day(I06_DOK_DATA) = day(case when DATENAME(dw,GETDATE()) = 'Monday' then getdate()-3 else getdate()-1 end ) " +
						 " GROUP BY I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end)) order by N08_PAV  ";
			        
			        stmt = con.prepareStatement(cmd);
			        rs = stmt.executeQuery();
				     
			        while (rs.next()) {
			          String agent = rs.getString("agent").trim();
			          String klient = rs.getString("kood").trim();
			          if(aruandeAgendid.get(agent)!= null){
			        	Agent aaa = (Agent) aruandeAgendid.get(agent); 
			        	if(aaa.klienteEile == null){
			        		aaa.klienteEile = new Vector();
				        }
			        	  aaa.klienteEile.add(klient);
				          aruandeAgendid.put(agent,aaa);   
			          }
			         }
			      
			        rs.close();
			        stmt.close();
			      
	      
	      
			        cmd = "SELECT  N08_ATSTOVAS AS agent ,rtrim(N08_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end) AS kood, " +  
				        	"	(N08_PAV) as nimi  FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS where N08_ATSTOVAS !='' and " +
							"	rtrim(N08_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end)  in " +
							"	(SELECT  rtrim(I06_KODAS_KS)+'-'+rtrim(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end) as kood	    FROM  i06_parh " +
							" left JOIN N33_KBAN ON N33_KODAS_KS = I06_KODAS_KS AND I06_ADR2=N33_ADRESAS " +
							" WHERE I06_DOK_DATA > getdate()-8 GROUP BY I06_KODAS_KS,(case when (N33_kodas_ak) is null then '1' else ((N33_kodas_ak)) end)) order by N08_PAV ";
				        
				        stmt = con.prepareStatement(cmd);
				        rs = stmt.executeQuery();
					     
				        while (rs.next()) {
				        	
				          String agent = rs.getString("agent").trim();
				          String klient = rs.getString("kood").trim();
				          if(aruandeAgendid.get(agent)!= null){
				        	Agent aaa = (Agent) aruandeAgendid.get(agent);
				        	if(aaa.klienteViim7 == null){
				        		aaa.klienteViim7 = new Vector();
					        }
				        	  aaa.klienteViim7.add(klient);
					          aruandeAgendid.put(agent,aaa);   
				          }
				         }
				      
				        rs.close();
				        stmt.close();
				      
		      
		      
	      
	      
	      
	      
	      
	      } catch (Exception e) {
	        getParent().showError(e);
	      }
	      connectionDown();
    //  System.out.println("aruandeKleindid " + aruandeAgendid .toString());
  }

    public void setBulkSales(Map agents){
    	
    	 connectionUp(url2);
    	 
       String cmd = "  SELECT  I06_ATSTOVAS AS agent , SUM(case when I06_OP_TIP = '52' then -I06_SUMA else 0 end) + " +
                   " SUM(case when I06_OP_TIP = '51' then I06_SUMA else 0 end)  AS pluss  FROM i06_parh  WHERE " +
                   "  month(I06_OP_DATA)=month(getdate()) AND year(I06_OP_DATA)=year(getdate()) and I06_PERKELTA='2' " +
                   "   and I06_R_DATE <= getdate() and I06_ATSTOVAS like '%vad%' " +
                   "  GROUP BY  I06_ATSTOVAS";

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

          
           if(aktiivsed == null){
           	makeReports();
           }
       	
      

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

         
         //siia lisame info juurde iga agendi kohta 
         if(aruandeAgendid != null){
        	 String total = "";
        	Iterator itera =  aruandeAgendid.keySet().iterator();
        	while(itera.hasNext()){
        		String ag = itera.next()+"";
        		Agent aaa = (Agent)aruandeAgendid.get(ag);
        		if(agents.get(ag) != null){
        			Agent agent = (Agent)agents.get(ag);
        			total = total + agent.getName()+"\n";
        			int eile = 0;
        			if(aaa.klienteEile != null){
        				eile = aaa.klienteEile.size();
        			}
        			
        			int viim = 0;
        			if(aaa.klienteViim7 != null){
        				viim = aaa.klienteViim7.size();
        			}
        			
        			
        			int eelm = 0;
        			if(aaa.klienteEelm != null){
        				eelm = aaa.klienteEelm.size();
        			}
        			int see = 0;
        			if(aaa.klienteSee != null){
        				see = aaa.klienteSee.size();
        			}
        			
        			int a30 = 0;
        			if(aaa.klienteAkt30 != null){
        				a30 = aaa.klienteAkt30.size();
        			}
        			int a60 = 0;
        			if(aaa.klienteAkt60 != null){
        				a60 = aaa.klienteAkt60.size();
        			}
        			String asi = "Bendras klientu skaicius : " + aaa.klienteKokku.size() +"|" +
"Aktyvus klientai si men. : " + see +"|" +
"Aktyvus klientai praeita men: " + eelm +"|" +
"Aktyvus klientai per 30d.: " + a30 +"|" +
"Aktyvus klientai iki 30d.: " + a60 +"|" +
"Skirtumas : " + (a30 - a60) +"|" +
"Aktyvus klientai 1d.: " + eile +"|" +
"Aktyvus klientai 7d.: " + viim  +"|  savaites dienos vidurkis:" + ((int)(viim / 5)) ;
        			
        			total = total + asi.replace('|', '\n').replace('|', '\n').replace('|', '\n').replace('|', '\n') 
        					.replace('|', '\n') .replace('|', '\n').replace('|', '\n').replace('|', '\n') + "\n\n";
        			if(agent.getStartText() != null && agent.getStart().trim().length() > 0){
        				agent.setStartText(agent.getStartText() + "|" + asi);
        			}else{
        				agent.setStartText(asi);
        			}
        		}     
        	}
        //if(total.length() > 3){
        	//	 SimpleDateFormat  dfor = new SimpleDateFormat(" MMMM dd, yyyy", Locale.getDefault() );
        		//sendEmail( total,"Active Customers Report - " + dfor.format(new Date()),parent.getProps().getProperty("reportmail"));
        	//}
         }
       
         
     }

    
    
 /*   private void sendEmail(String body,String subjekt,String to ){
  		Email mail = new Email(null);
  		mail.setSMTP(parent.getProps().getProperty("smtphost"));
  		mail.setFrom(parent.getProps().getProperty("from"));
  		mail.setSubjectFilesTo(subjekt);
  		mail.setEncoding("UTF-8");
  		//mail.addAttach(f.getAbsolutePath());
  		mail.setSendNotice(false);
  		try {
  			mail.setBody(body);
  		} catch (MessagingException e1) {
  			// TODO Auto-generated catch block
  			e1.printStackTrace();
  		}
  		
  		
  		StringTokenizer st = new StringTokenizer(to,",");
  		while(st.hasMoreTokens()){
  			String mai = st.nextToken();
  			mail.setFilesTo(mai);
  			try {
  				mail.send();
  			} catch (AddressException e) {
  				e.printStackTrace();
  			} catch (UnsupportedEncodingException e) {
  				e.printStackTrace();
  			} catch (MessagingException e) {
  				e.printStackTrace();
  			}
  		}	
    }
    */
    

    
    
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

    public static final String DRIVER_VERSION = "RivileKuryba";
    private String             url = "jdbc:odbc:kuryba";
    private String	       url2 = "jdbc:odbc:kuryba";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

