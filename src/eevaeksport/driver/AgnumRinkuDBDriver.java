/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.util.Converter;


/**
 * Title:        MobileJava
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class AgnumRinkuDBDriver extends AbstractDBDriver implements BulkUpdater, Discounter, PictureMaker {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public AgnumRinkuDBDriver(EksportPanel parent) {
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

    public String getSaveDebtInfo() {
    
    	    	return null;
    	       }

    
    
   
           
    public String getSaveStockInfo(){
    return null;
    }

    public String getAssortmentStr(Agent agent){
    	return null;
    }
    
            public String getDealerCustomerCSV(String agent){
             return null;
             }

             public String  getCustProdDiscounts(Agent agent){
                 return null;
                 }

                public void setBulkSales(Map agents){
              	  connectionUp();
          		String cmd =  " select id_ag as agent,sum(suma)as sales from dok where extract(month from data)= extract(month from current_timestamp)-1 " +
             "AND extract(year from data)= extract(year from current_timestamp) and doktip='4' " +
             " GROUP BY id_ag ORDER BY id_ag" ;
          	
          		log(cmd);
          		
          		try {
          			
          			Statement stmt = con.createStatement();		
          		
          			
          			ResultSet	      rs = stmt.executeQuery(cmd);
          			while(rs.next()) {
          				String agentID = rs.getString("agent").trim();
          				Agent agent = (Agent)agents.get(agentID);
          				if (agent != null){
          					agent.setMonthSales((int)rs.getDouble("sales"));
          				}
          			}
          			rs.close();
          			stmt.close();
          			con.close();
          		} catch (Exception e) {
          			getParent().showError(e);
          		}
          		connectionDown();
          		
          	}
           
                                                
    /**
     *
     */
    public void connectionUp(){
      if (con == null){
        try {
           con = DriverManager.getConnection(url);
           stmt = con.createStatement();
        } catch (Exception e) {
            getParent().showError(e);
        }
      }
    }

    /**
     *
     */
    public void connectionDown(){
        try {
           stmt.close();
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
    public TreeMap getAgents() {
        connectionUp();
        String  cmd = " SELECT id_agent as yksus, pavarde as ees,vardas as nimetus FROM drb d inner join subkl k on k.id_agent = id_drb \r\n" + 
        		"			  group by id_AGENT, pavarde, vardas ";
        TreeMap agents = new TreeMap();

        try {
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              //agent.setObject(rs.getString("objekt").trim());
              String ees = rs.getString("ees").trim();
              String name = ees + " " + rs.getString("nimetus").trim();
              agent.setName(name);
                agents.put(name, agent);
            }
            rs.close();
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        return agents;
    }

    public TreeMap getCustomers(Agent agent) {
        connectionUp();
        // String cmd = "SELECT k.kood, k.nimi FROM (klient k INNER JOIN omrid ON k.kood = omrid.klient INNER JOIN rid ON rid.dokumnr = omrid.tasudjrk) WHERE omrid.rid2obj='" + agentCode + "' AND (len(rid.toode) > 2) GROUP BY k.kood";
        String cmd =
            "SELECT  k.id_kl as klid,s.id_subkl as sid, k.kod as kood , k.pavad as nimi, s.kod as skood , s.adr as subnimi,k.adr AS aadress,' ' AS ale, ' ' AS visit ,s.id_agent   " + 
          "  AS agent,'' as maksja , k.tlf as telefon ,k.term_p as term , k.f1 as grupp   " +
		  " FROM kl k  left join subkl s on s.id_kl=k.id_kl where   k.deb='Y' and k.color!=255  and s.f2 != '' ORDER BY k.pavad ";

        log(cmd);

        TreeMap customers = new TreeMap();
        TreeMap custMap2 = new TreeMap();
        try {
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
            	
            	String klid = rs.getString("klid").trim();
            	String paym = klid;
            	String sid= rs.getString("sid");
            	if(sid != null){
            		klid = klid + "-" + sid;
            	}
              Customer cust = new Customer(klid);
            String name = rs.getString("nimi").trim();
            
            name = name.replaceAll("\"", " ");
            name = name.replaceAll("   ", " ");
            name = name.replaceAll("  ", " ");
  	      name = name.replace('*', ' ');
  	      name = name.replace('*', ' ');
  	      name = name.replace('*', ' ');
  	      name = name.replaceAll("  ", " ");
        
            cust.setPayer(paym);
            String subkod = rs.getString("skood");
            String subadr = rs.getString("subnimi");
            
            if(subkod != null){
            	name = (name + " " + subkod.trim()).trim();
            }
            if(subadr != null){
            	name = (name + " " + subadr.trim()).trim();
            }            
            
            cust.setName(name);
            
            String aadress = rs.getString("aadress") ;
            if (aadress != null){
              cust.setAddress(aadress.trim());
            }
            
            
            String visit = rs.getString("visit");
            if (visit != null){
              cust.setVisit(visit.trim());
            }
            Enumeration enums = getParent().getAgentsHash().elements();
            while (enums.hasMoreElements()) {
              Agent a = (Agent) enums.nextElement();
            //  cust.addAgent(a);
            }
          

    
            String ag = rs.getString("agent");
            if(ag != null && ag.trim().length() >0){
            	cust.addAgent(new Agent (ag.trim()));
            }
            
            String payer = rs.getString("maksja");
            if(payer != null && payer.trim().length() >0){
            	cust.setPayer(payer.trim());
            }
            String telefon = rs.getString("telefon");
            if(telefon != null && telefon.trim().length() >0){
            	cust.setPersonalData(telefon.trim().replaceAll(";", ":"));
            }
            
             telefon = rs.getString("term");
            if(telefon != null && telefon.trim().length() >0){
            	cust.setPaymTerm(telefon.trim());
            }
            
            String gr = rs.getString("grupp");
            if(gr != null && gr.trim().length() >0 && !gr.trim().equals("11")){//baashinnakirja ei pane
            	cust.setGroup(gr.trim());
            }
            
                customers.put(name, cust);
                custMap2.put(klid,cust);
            }

            
            String sql = " SELECT id_kl as klient,sum(skola -apmsum) as summa FROM  \r\n" + 
            		"                    dok WHERE (skola -apmsum > 0.1  or skola -apmsum < -0.1) and snd_kod = 'ps' and doknr like 'RAD%' and ((current_timestamp-data)-term) > 0\r\n" + 
            		"                     group by id_kl ORDER BY id_kl";
            
         ResultSet   rst =  stmt.executeQuery(sql);
          while(rst.next()){
            String id = rst.getString("klient").trim();
            double debt = Double.parseDouble(rst.getString("summa").trim());
     
          Iterator iter = custMap2.values().iterator(); 
        
          while (iter.hasNext()){
          	Customer cc = (Customer)iter.next();
          	// 
          	if(cc.getPayer().equals(id) || cc.getID().equals(id)){
          		cc.setDebt(debt);       		
          		
            }
          }
          }
          
           sql = " SELECT id_kl as klient,sum(skola -apmsum) as summa FROM  \r\n" + 
          		"                    dok WHERE (skola -apmsum > 0.1  or skola -apmsum < -0.1) and snd_kod = 'ps' and doknr like 'RAD%' \r\n" + 
          		"                     group by id_kl ORDER BY id_kl";
          
       rst =  stmt.executeQuery(sql);
        while(rst.next()){
          String id = rst.getString("klient").trim();
          double debt = Double.parseDouble(rst.getString("summa").trim());
   
        Iterator iter = custMap2.values().iterator(); 
     
        while (iter.hasNext()){
        	Customer cc = (Customer)iter.next();
        	// 
        	if(cc.getPayer().equals(id) || cc.getID().equals(id)){
        		cc.setUsedCredit(debt);       		
        		
        	  }
          }
        }
          
          
          
          rst.close();
            
            
            
            
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        return customers;
    }


        public String getDistribution() {
          return null;
        }
TreeMap photos = new TreeMap();

    public TreeMap getProducts(Agent agent) {
        connectionUp();

	TreeMap products = new TreeMap();

	try {
   
                  
       String  cmd = "  SELECT t.kod as toode , t.pavad as nimetus , ipak as tykke,KAINA1 as  myygihind, vnt as  yhik,g2.id_pgr AS grupp ,g.id_pgr  AS grupp2 ,bk.bkod   \r\n" + 
       		"       		      		       		      		AS ean, path as pilt ,t.pvm as vat , t.kiekis as laoseis,t.pogrupis, t.kateg, t.grupe   FROM prk t\r\n" + 
       		"       											 LEFT JOIN pgr g ON g.grupe=t.pogrupis and g.gr_type=2\r\n" + 
       		"       											 LEFT JOIN pgr g2 ON g2.grupe=t.posakis and g2.gr_type=3\r\n" + 
       		"       		       								inner join RR_PRKKN h ON h. ID_PRK = t.ID_PRK and h.ID_KN = 115 and kaina1 != 0\r\n" + 
       		"												LEFT JOIN prk_images i on i.id_prk=t.id_prk\r\n" + 
       		"       		       		                        left join BK bk on bk.id_prk=t.id_prk\r\n" + 
       		"       		       		       					where kod like '0000%' and ID_snd = 2948  and aktyvi ='Y' "; //and  g.grupe like '%pda%'
       if (isDebug()) {
           	System.out.println(cmd);
           	}

//color 8388608
       
          stmt = con.createStatement();
                      ResultSet rs = stmt.executeQuery(cmd);

                      while (rs.next()) {
                          String kood = rs.getString("toode").trim();
                          
                          Product prod = new Product(kood);
                          
                          
                        String nimetus =rs.getString("nimetus");
                          prod.setName(nimetus.trim());
                          prod.setPrice(rs.getDouble("myygihind"));
                          
                          String yhik = rs.getString("yhik");
                          if(yhik != null && yhik.trim().length()>0){
                          prod.setUnit(yhik.trim());}
                      
                          boolean goes = false;
                           String grupp = rs.getString("grupp");
                          if(grupp != null && grupp.trim().length()>0){
                          prod.setGroup(grupp.trim());
                          goes = true;
                          }
                          
                          
                          String grupp2 = rs.getString("grupp2");
                          if(grupp2 != null && grupp2.trim().length()>0){
                          prod.setGroupSecond(grupp2.trim());}

                          
                          prod.setInCases(rs.getDouble("tykke"));
                          String ean = rs.getString("ean");
                          if (ean != null && ean.trim().length()>0){
                            prod.setEAN(ean.trim());
                          }
                          String pilt = rs.getString("pilt");
                          if (pilt != null && pilt.trim().length()>0){
                        	  photos.put(kood,pilt);
                          }
                         
                           String vat = rs.getString("vat");
                           if (vat != null && vat.trim().length()>0){
                        	   prod.setVat(vat);
                           }
                           
                           double stock = rs.getDouble("laoseis");
                           if(stock>0){
                        	   prod.setStock(stock);
                           }else {
                        	   prod.setStock(0);
                           }
                         
                          
                          if(goes) {
                        	  products.put(nimetus, prod);  
                          }
                        

                      }
      rs.close();                      
                    

        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        return products;
    }
	public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}

    public TreeMap getProdGroups() throws SQLException {
   this.connectionUp();
     TreeMap   tm = new TreeMap();
     String    sql = "SELECT id_pgr AS liik , grupe AS nimi FROM pgr gr where gr_type = 3 and grupe != '' order by 1 desc";
     
     ResultSet rs = stmt.executeQuery(sql);

     while (rs.next()) {
       String liik=rs.getString("liik").trim();
       String nimetus=rs.getString("nimi").trim();
       if(nimetus.contains("RADLER")) {
    	   nimetus = "RIN. RADLER";
       }
     
       if (liik != ""){
       tm.put(liik,nimetus);
       }
     }
     rs.close();
     
     connectionDown();
     return tm;
 }


    public TreeMap getProdGroupsSecond() throws SQLException {
    	   this.connectionUp();
    	     TreeMap   tm = new TreeMap();
    	     String    sql = "SELECT id_pgr AS liik , grupe AS nimi  FROM pgr gr where gr_type = 2 and grupe != '' order by 1 desc";
    	     
    	     ResultSet rs = stmt.executeQuery(sql);

    	     while (rs.next()) {
    	       String liik=rs.getString("liik").trim();
    	       String nimetus=rs.getString("nimi").trim();
    	       if(nimetus.contains("RADLER")) {
    	    	   nimetus = "RIN. RADLER";
    	       }
    	       if (liik != ""){
    	       tm.put(liik,nimetus);
    	       }
    	     }
    	     rs.close();
    	     
    	     connectionDown();
    	     return tm;
    	 }

    public Vector getCustomerProductsList(String agentCode) {
        connectionUp();
	/*
	 * String cmd = "SELECT omrid.klient,rid.toode FROM omrid INNER JOIN rid ON omrid.tasudjrk = rid.dokumnr WHERE omrid.rid2obj='" +
	 * agentCode + "' AND (len(omrid.klient) > 1) AND (len(rid.toode) > 2) AND omrid.tasudtyyp='G' AND omrid.reakuupaev >= " +
	 * getStartingTime() + " GROUP BY omrid.klient, rid.toode ORDER BY omrid.klient, rid.nimetus";
	 */
	String cmd =	    "select sd.id_kl as saaja,sd.ID_SUBKL as abi,t.kod as toode,max(sd.data) as kuupaev,max(a.kiekis) as kogus , max(doknr) as lastdok , parkn as lastprice from spub2 a INNER JOIN SDOK sd on sd.id_sdok = a.id_sdok inner join prk t on t.id_prk=a.id_prk\r\n" + 
	    "	       where sd.data>current_timestamp-20  GROUP BY sd.id_kl,sd.ID_SUBKL, t.kod,parkn ORDER BY sd.id_kl,sd.ID_SUBKL , t.kod,parkn desc" ;
	
	if (isDebug()) {
	    System.out.println(cmd);
	}

	Vector custProds = new Vector();

	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String kliendikood = rs.getString("saaja").trim();
		String abikood = rs.getString("abi").trim();
		if(!abikood.equals("-1")){
			kliendikood= kliendikood + "-" + abikood;
		}
		String tootekood = rs.getString("toode").trim();
	     String kuupaev = rs.getString("kuupaev");
        double kogus = Double.parseDouble(rs.getString("kogus").trim());
   
        String lb = rs.getString("lastdok");
        double lp = Double.parseDouble(rs.getString("lastprice").trim());
                if (kliendikood.length() > 1) {
                  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,kuupaev.substring(5,10),lp,0,0," ");
                  cp.setAbiKood(lb);
                  custProds.add(cp);
                }
	    }

            rs.close();

	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return custProds;
    }


    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      
      String sql = " SELECT d.id_kl as payer , d.id_subkl as saaja  ,doknr AS number,d.data+ d.term AS maksepaev,d.skola -apmsum as tasumata, d.data as kuupaev ,((current_timestamp-d.data)-d.term)\r\n" + 
      		"      		      		AS yle, d.snd_kod  FROM  dok d  WHERE  (d.skola -apmsum > 0.1  or d.skola -apmsum < -0.1) and d.snd_kod = 'ps' and \r\n" + 
      		"						doknr like 'RAD%'  and ((current_timestamp-d.data)-d.term) < 200 ORDER BY d.id_kl ";

      connectionUp();
      ResultSet rs = stmt.executeQuery(sql);
      while(rs.next()){
    	  
        Debt debt = new Debt();
        debt.setPayerID(rs.getString("payer").trim());
        debt.setCustID(debt.getPayerID() + "-" + rs.getString("saaja").trim());
        debt.setDoknr(rs.getString("number").trim());
        debt.setDuedate(rs.getDate("maksepaev"));
        debt.setSum(rs.getDouble("tasumata"));
        debt.setDate(rs.getDate("kuupaev"));
        debt.setDue((Calendar.getInstance().getTimeInMillis()-debt.getDuedate().getTime()) /86400000 + "");
       
        list.addElement(debt);
      }
      rs.close();
      connectionDown();
      return list;
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

    public String getDriverVersion() {
        return DRIVER_VERSION;
    }

    public String getSubjectFilesTo() {
        return this.subjectFilesTo;
    }
    public String getPilotDayData(Agent agent){
      return null;
    }


    public String getNonMobileOrders(Agent agent){
      return null;
    }

    public void log(String s) {
        getParent().log(s);
    }

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "Rinku";
    private String	       url = "jdbc:odbc:rinku";
    private Connection	       con = null;
    private Statement	       stmt;
    private String	       subjectFilesTo = null;
	@Override

    public Vector getPriceMatrixData() {
      Vector lines = new Vector();
      String uus =  "select   '' as custid, hk.kod AS clgroup,bk.kod as prodid , ' ' AS prodgroup, \r\n" + 
      		"      		          0 as discount, kaina1  AS price ,'' as algus , '' as lopp \r\n" + 
      		"					   from RR_PRKKN h INNER JOIN RR_KN hk ON hk.id_kn=h.ID_KN LEFT JOIN  PRK bk on bk.id_prk=h.id_prk "
      		+ " and bk.ID_snd = 2948 where h.ID_KN != 115 and kaina1 !=0 and bk.kod != '' ";

      try {
    	  connectionUp();
      	Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(uus);

                while (rs.next()) {

                        String custID = rs.getString("custid");
                        String clGroup = rs.getString("clgroup");
                        String prodID = rs.getString("prodid");
                        String prodGroup = rs.getString("prodgroup");
                        double discount = rs.getDouble("discount");
                        double price = rs.getDouble("price");
                        String algus =  rs.getString("algus");
        				String lopp =  rs.getString("lopp");
        			//	String asi =  rs.getString("asi").trim();
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
                        if (mline.getProdID() == null && prodGroup != null
                                        && prodGroup.trim().length() > 0) {
                                mline.setProdGroup(prodGroup);
                        }
                        mline.setDiscount(discount);

                        if (price != 0) {
                                mline.setPrice(price);
                        }
                      
                        
                        if(algus != null && algus.trim().length() != 0){
                        mline.setStartDate(algus.substring(0,10));	
                        }                  
        			
        				 if(lopp != null && lopp.trim().length() != 0){
                           mline.setEndDate(lopp.substring(0,10));
                         }
                    //  if(asi.equals("1"))  
                       lines.add(mline);
                }
                rs.close();
                stmt.close();
                this.connectionDown();

        } catch (SQLException ex) {

          ex.printStackTrace();
        }


        return lines;
	//return null;
}


	@Override
	public Vector getFixedAssortmentData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStartingTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void PictureMaker() {
		 makePictureCat();
		  System.exit(0);
	}
	
	  private void makePictureCat(){
	  	    
	    	
	    	String pictssou = parent.getProps().getProperty("pict.dir");
	  	    String pictspda = parent.getProps().getProperty("pictpda.dir");
	  	   
	  	 try {
 	  
      File f = new File(pictspda +"list.txt");
      if (f.exists())f.delete();
      //Kataloogi sisude kustutamine
  	 	 File res2 = new File (pictspda);
	        
	  		 TreeMap prods = this.getProducts(null);
	  		 
	  		System.out.println(photos.toString() + " photos");
	         StringBuffer buf = new StringBuffer();
	         Iterator enume = prods.keySet().iterator();
	    
	         while (enume.hasNext()) {
	    	    	String asi = (String)enume.next();
	    	    	Product prod = (Product)prods.get(asi);
	    	    	String toode = prod.getID().trim();  
	    	    	String fname = (String)photos.get(toode); 
	    	    	
	               if(fname != null){
	                	
	    	   	  res2 = new File (pictssou + fname);
	    	   	System.out.println(" photos res 2 " + res2.exists() );
		        
	              
	              	if(res2.isFile()){
	              	File inpda = new File ( pictspda + prod.getID()+ ".jpg");
	              	if(!inpda.exists()){
	                  	Converter.shrinkAndCopyFile(res2,inpda,400);
        	  	          } 
	                if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
              		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
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
	     
	  }


}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/
