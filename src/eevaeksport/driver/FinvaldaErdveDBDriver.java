package eevaeksport.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
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
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class FinvaldaErdveDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter,PictureMaker {
  public FinvaldaErdveDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
    
    } catch (Exception e){
       parent.showError(e);
    }
    
    
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
    rcompanyx = getParent().getProps().getProperty("rest.companyx");

  }

  
  

  String user =  null;
  String pass = null;
  String rcompany = null;
  String rcompanyx = null;
  /**
   *
   */
  public void connectionUp(){
  }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public void PictureMaker() {
		makePictureCat();
		 System.exit(0);
		
	}

	
	private Vector prodids = new Vector();
	
	  private void makePictureCat(){
	  	    
	    	
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
//	         	 Lõppes kustutamine 
	  		 
	  		 TreeMap prods = this.getProducts(null);

	         StringBuffer buf = new StringBuffer();
	         Iterator enume = prodids.iterator();
	     //    System.out.println("pilt olemas " + ff2.toString() + " "+ pictssou);
	         while (enume.hasNext()) {
	    	    	String toode = (String)enume.next();
	       	  if(toode.length() > 0){
	           	  res2 = new File (pictssou);
	              ff2 = res2.list();
	          //    System.out.println("pilt olemas " + ff2.toString() + " "+ pictssou);
	         	 for(int i = 0;ff2.length>i;i++){
	             //     System.out.println("pilt olemas " + ff2[i]);
	         	    
	         		 if( ff2[i].startsWith(toode)){
	            	File in = new File ( pictssou + ff2[i]); 
	              	if(in.isFile()){
	              	File inpda = new File ( pictspda + toode+ ".jpg"); 
	                  	Converter.shrinkAndCopyFile(in,inpda,400);
              	    if(buf.toString().indexOf(toode +".jpg;"  + inpda.length()) == -1){
	                    		buf.append(toode +".jpg;"  + inpda.length() + "\n");
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
	    
	  	 
	  	 
	  }

	
	
  public String getSaveDebtInfo() {
   	return null;
   }


   public Vector getFixedAssortmentData() {
     return null;
   }

   
    private Date getDate(String ss){
	  Calendar cc = Calendar.getInstance();  	
	  	cc.set(Calendar.YEAR,Integer.parseInt(ss.substring(0,4)));
	  	cc.set(Calendar.MONTH,Integer.parseInt(ss.substring(6,7))-1);
	  	cc.set(Calendar.DATE,Integer.parseInt(ss.substring(9,10)));
	  	return cc.getTime() ;
	  }
   
  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
    
          try {


        	Statement stmt = this.getNewConnection().createStatement();
                  

            String sql2 = "select '' as custid ,  klr_kod AS clgroup, " +
			  " pre_kod AS prodid, '' AS prodgroup,0 AS discount, par_k AS price ,  term_nuo_kain as algus , "
			  + " term_iki_kain as lopp  " +
			   "	FROM Nkrp1 pd  " ; //where  term_nuo_kain <= getdate() and term_iki_kain >= getdate()
            
            ResultSet rs = stmt.executeQuery(sql2);
Vector olemas = new Vector();
            while (rs.next()) {

                    String clGroup = rs.getString("clgroup");
                    String prodID = rs.getString("prodid");
                    String prodGroup = rs.getString("prodgroup");
                    double price = rs.getDouble("price");
                    String fromdate = rs.getString("algus");
                    String todate = rs.getString("lopp");
                    MatrixLine mline = new MatrixLine();
                    
                
                    if (clGroup != null && clGroup.trim().length() > 0) {
                            mline.setCliGroup(clGroup.trim());
                    }

                    if (prodID != null && prodID.trim().length() > 0) {
                            mline.setProdID(prodID.trim());
                    }
                    if (mline.getProdID() == null && prodGroup != null
                                    && prodGroup.trim().length() > 0) {
                            mline.setProdGroup(prodGroup);
                    }
                    mline.setPrice(price);
                    
                    
                    if(fromdate != null && fromdate.trim().length() > 0){
                   
                    	Date fr = getDate(fromdate);
                    Date to = getDate(todate);
                   
                    if(fr.before(new Date()) && to.after(new Date())){
                     	mline.setStartDate(fromdate.substring(0,10));
                        mline.setEndDate(todate.substring(0,10));	
                    }
                    }
                    if(price > 0){
                        olemas.add(clGroup.trim()+prodID.trim());
                        lines.add(mline);	
                    }
            }
            
                    	
                              
          } catch (SQLException ex) {
          ex.printStackTrace();
          }
          return lines;
  }

  public String getProductSortingData(){
   	return null;
   }

  public Vector getDebt(String agentCode) throws SQLException{
     return this.debt;
   }


  public String getAssortmentStr(Agent agent){
  	return null;
  }

      public String getSaveStockInfo(){

      
    
		
		String cmd = "select pre_kod as toode , sum(sp1_ska)  \r\n" + 
				"    	 as seis from preks1_pkksar   where  san_kod in (" + warelist + ") \r\n" + 
				"		 group by pre_kod having sum(sp1_ska) > 0 order by pre_kod " ;
		if (isDebug())
			System.out.println(cmd);
		StringBuffer products = new StringBuffer();
		try {
		Connection con =	this.getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);

			while (rs.next()) {

				String kood = rs.getString("toode").trim();
				double seis = rs.getDouble("seis");
				if (seis > 0) {
					products.append(kood + ";");
					products.append(" ;");
					products.append(getParent().getNF().format(seis) + "; "
							+ "\n");
				}
			}
			rs.close();
			st.close();
			con.close();
				
		} catch (Exception e) {
			getParent().showError(e);
		}
		return products.toString();
	}
      
      
      

              public String getDealerCustomerCSV(String agent){
               return null;
               }

                                                  public String  getCustProdDiscounts(Agent agent){
                                                   return null;
                                                   }


  /**
   *
   */
  public void connectionDown(){
  }

  
  public DealerProduct getDealerProductsList() {

  
      DealerProduct dealerProds = new DealerProduct();
     try {

    
        
        String warelist = "'CHI DIDMEN','CHI STUDI.','107','MP KLAIPĖD','TRISKELL K'";
       
      	  
      	String  warelist2 = "'LOMA','LASIO','IMAGE','ERAYBA','ONMACABIM','MP PROFESI','MILANOBRUS','TRISKELL','SKEYNDOR','CENTR.','BIOREPEEL'";

      	String sql = "select pre_kod as toode , sum(sp1_ska)  \r\n" + 
				"    	 as seis from preks1_pkksar   where  san_kod in (" + warelist + ") \r\n" + 
				"		 group by pre_kod having sum(sp1_ska) > 0 order by pre_kod " ;
     	String sql2 = "select pre_kod as toode , sum(sp1_ska)  \r\n" + 
				"    	 as seis from preks1_pkksar   where  san_kod in (" + warelist2 + ") \r\n" + 
				"		 group by pre_kod having sum(sp1_ska) > 0 order by pre_kod " ;
         
           if (isDebug()) System.out.println(sql);
          	Connection con = this.getNewConnection();
              Statement stm = con.createStatement();
              ResultSet rst = stm.executeQuery(sql);

                while (rst.next()) {
                  String toode = rst.getString("toode").trim();
                  int seis = (int)Double.parseDouble(rst.getString("seis").replace(',','.'));
                
                  dealerProds.add("KLA", toode,seis+""," "," " );
              }
                
                rst.close();
                rst = stm.executeQuery(sql2);

                while (rst.next()) {
                  String toode = rst.getString("toode").trim();
                  int seis = (int)Double.parseDouble(rst.getString("seis").replace(',','.'));
                  dealerProds.add("VIL", toode,seis+""," "," " );
              }
              rst.close();
              stm.close();
              con.close();

      } catch (Exception e) {
          getParent().showError(e);
      }

       return dealerProds;
     }

  
  
  
  public TreeMap getAgents(){
    String cmd = "select klg_kod as kood , klg_pav AS name   from klgrp ";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    agent1 = new TreeMap();
    
    try {

    	Connection con = this.getNewConnection();
    	Statement stmt = con.createStatement();
    	ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("name").trim();
          agent.setName(name);
          agent.setIsPilot(true);
          agent.setLanguage("LIT");
          agent.setCountry("lt");
          agent.setPassWord("0000");
          if(getParent().getProps().getProperty("KLA.list").contains(agent.getID())) {
        	 agent.setObject("KLA"); 
          }else {
        	  agent.setObject("VIL");
          }
          String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
          if(email.length() > 1){
        	  agent.setEmail(email);
        	  agent.setActive(true);
          }
         
          agent1.put(agent.getID(), agent);
          agents.put(name, agent);
           
         
        }
        rs.close();
        stmt.close();
        con.close();
        
    } catch (Exception e){
        getParent().showError(e);
      }
  
  
    return agents;
  }

  private TreeMap agent1 = new TreeMap();
  
  public TreeMap getCustomers(Agent agent){

	 // Converter.getManagersReplacements(agent1, rcompanyx, user , pass);
	  
 String cmd = " select kl.kli_kod as kood , adr.adr_kod as akood, kl.kli_pav as name ,adr.adr_pav1 as aadress,kli_adr as aadress1 , " + 
 		" adr_poz5 as adragent, gr.KLG_kod as agent, kli_tel as info, ats_ko1 as term,kli_sko as debt ,KLI_KRE as credit ,kl.klr_kod as hinnak " + 
 		" , ADR_PIR as esma, ADR_ANT  as teisi ,ADR_TRE as kolma , ADR_KET as nelja ,ADR_PEN as reede ,ADR_SES as laup, " + 
 		"	 ADR_SEK as pyhap,  ADR_POZ2  as category , ADR_POZ4 as cathelp from klient kl  left join adresas adr on kl.kli_kod = adr.kli_kod"
 		+ " left join klgr as gr on gr.KLI_KOD=kl.kli_kod " ;
 
 String ccc = "select kli_kod as payer from klient ";
 
           String sql = "SELECT  kli_kod AS klient, vaztar_nr AS number,apmok_data AS maksepaev, likusi_skola AS tasumata, " +
        "  dok_data AS kuupaev , datediff(day,apmok_data,getdate()) AS yle " +
		 " FROM view_PardSkolos   "; //where getdate()+5 > apmok_data



           String sqls = "SELECT  kli_kod AS klient,  sum(likusi_skola) AS tasumata " +
		"  FROM view_PardSkolos  where getdate()-1 > apmok_data group by kli_kod"; //

       //    tal_visitationdays ,talspartnercode,

          
           
  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    TreeMap debtMap = new TreeMap();
    Vector payers =  new Vector();
    try {

    	Connection con = this.getNewConnection();
    	Statement stmr = con.createStatement();
    	ResultSet rs1 = stmr.executeQuery(ccc);
        while(rs1.next()){
        	
       payers.add(rs1.getString("payer"));
          
        }
       rs1.close();
       stmr.close();
    	
      
    stmr = con.createStatement();
   	 rs1 = stmr.executeQuery(sqls);
       while(rs1.next()){
       	
    	   debtMap.put(rs1.getString("klient"),rs1.getString("tasumata"));
         
       }
      rs1.close();
      stmr.close();
       
       
       
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      
      while(rs.next()){
          String kood =  rs.getString("kood").trim();
          String akood = rs.getString("akood");
          if(akood == null) {
        	  akood = "00";
          }
          String accountnum = kood + "-" + akood.trim();
          Customer cust = new Customer(accountnum);
          cust.setPayer(kood);
          String name = rs.getString("name").trim();
          cust.restName = name;
          
          
          
          String aadr = rs.getString("aadress");
          String aad = accountnum;
          if (aadr != null) {
        	  if(aadr.indexOf("(") !=-1 && aadr.indexOf(")") !=-1){
        	  }
            cust.setAddress(aadr.trim().replace('\n',' '));
            aad = aadr.trim().replace('\n',' ');
            cust.restStreet = aad;
            cust.setCountry("Lithuania");
          }  else {
        	  aadr = rs.getString("aadress1");
              aad = accountnum;
             if (aadr != null) {
           	  if(aadr.indexOf("(") !=-1 && aadr.indexOf(")") !=-1){
           	  }
               cust.setAddress(aadr.trim().replace('\n',' '));
               aad = aadr.trim().replace('\n',' ');
               cust.restStreet = aad;
               cust.setCountry("Lithuania");
             }   
          }
        
              
        name = name + " " + aad;
        cust.setName(name);
        
        boolean add = false;
        String agen = rs.getString("adragent");
        if (agen != null) {
          cust.addAgent(new Agent(agen.trim()));
          add = true;
          
          if ( agen != null && agent1.get( agen.trim()) != null){
              Agent aa = (Agent) agent1.get( agen.trim());
               Vector vec = aa.getDubbler();
               if( vec!= null && vec.size() >0){
                    Enumeration en = vec.elements();
                    while( en.hasMoreElements()){
                         String ag = (String) en.nextElement();
                          cust.addAgent( new Agent( ag));
                   }
              }
        }

        }
        
        agen = rs.getString("agent");
        if (agen != null) {
          cust.addAgent(new Agent(agen.trim()));
          add = true;
        
          if ( agen != null && agent1.get( agen.trim()) != null){
              Agent aa = (Agent) agent1.get( agen.trim());
               Vector vec = aa.getDubbler();
               if( vec!= null && vec.size() >0){
                    Enumeration en = vec.elements();
                    while( en.hasMoreElements()){
                         String ag = (String) en.nextElement();
                          cust.addAgent( new Agent( ag));
                   }
              }
        }
        
        }
       
     
        

        String info = rs.getString("info");
        if (info != null && info.trim().length()>0) {
          cust.setPersonalData(" Tel: " + info.trim());
        }
        
        String term = rs.getString("term");
        if (term != null && term.trim().length()>2) {
          cust.setPaymTerm(term.substring(0, 2));
          }
        

        String debtStr = rs.getString("debt");
        if (debtStr != null){
          cust.setUsedCredit(Double.parseDouble(debtStr));
        }
        
        String creStr = rs.getString("credit");
        if (creStr != null){
        	try{
        		double dd = Double.parseDouble(creStr.trim());
        		if(dd > 0)
        	    cust.setCredit(dd + "");
        	}catch(Exception r){
        	}
        }
        
        
        String hinnak = rs.getString("hinnak");
        if (hinnak != null && hinnak.trim().length() > 0){
          cust.setGroup(hinnak.trim());
        }
        
        if(debtMap.get(kood) != null){
        	try{
        		double dd = Double.parseDouble(debtMap.get(kood)+"");
        		if(dd > 0.05 || dd < -0.05){
        			cust.setDebt(Double.parseDouble(debtMap.get(kood)+""));	
          	    }
        		    		
        	}catch(Exception r){
        		
        	}
        }
       
       
        
        
        // esma, ADR_ANT  as teisi ,ADR_TRE as kolma , ADR_KET as nelja ,ADR_PEN as reede ,ADR_SES as laup, " +
        //"	 ADR_SEK as pyhap
        int esm = rs.getInt("esma");
        int tei = rs.getInt("teisi");
        int kol = rs.getInt("kolma");
        int nel = rs.getInt("nelja");
        int ree = rs.getInt("reede");
        int lau = rs.getInt("laup");
        int pyh = rs.getInt("pyhap");
       
        String transa = "Vežimas :";
        boolean adde = false;
        String aa = "";
        String bb = "";
        boolean olemas = false;
        if(esm == 1){
        	aa = aa + "1," ;
        	adde  = true;
        
        	Calendar cd = Calendar.getInstance();
        if(cd.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY){
        	cd.add(Calendar.DAY_OF_YEAR, 7);
        	bb = bb + cd.getTimeInMillis();
        }else{
        	while(cd.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY){
        		cd.add(Calendar.DAY_OF_YEAR, 1);
        	}
        	bb = cd.getTimeInMillis() + "";
        }
        olemas = true;
        }
        
        if(tei == 1){
        	aa = aa + "2," ;
        	adde  = true;
        	
        	Calendar cd = Calendar.getInstance();
            if(cd.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY){
            	cd.add(Calendar.DAY_OF_YEAR, 7);
            	bb = bb + cd.getTimeInMillis();
            }else{
            	while(cd.get(Calendar.DAY_OF_WEEK) != Calendar.TUESDAY){
            		cd.add(Calendar.DAY_OF_YEAR, 1);
            	}
            	if(olemas){
            	  	bb = bb + "," +cd.getTimeInMillis() + "";
                }else{
            	 bb 	 = cd.getTimeInMillis() + "";
                }
            }
            olemas = true;
        	
        	
        }
        
        if(kol == 1){
        	aa = aa + "3," ;
        	adde  = true;
        	
        	Calendar cd = Calendar.getInstance();
            if(cd.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY){
            	cd.add(Calendar.DAY_OF_YEAR, 7);
            	bb = bb + cd.getTimeInMillis();
            }else{
            	//System.out.println("kolmap "  + cd.get(Calendar.DAY_OF_WEEK) + " "+ cd.getTime().toLocaleString());
            	while(cd.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY){
            		cd.add(Calendar.DAY_OF_YEAR, 1);
            		System.out.println("kolmap0 " +cd.get(Calendar.DAY_OF_WEEK) + " "+  cd.getTime().toLocaleString());
                    
            	}
            	
            	//System.out.println("kolmap2 " + cd.getTime().toLocaleString());
            	
            	if(olemas){
            	  	bb = bb + "," +cd.getTimeInMillis() + "";
                    
            	}else{
            	  	bb = cd.getTimeInMillis() + "";
                      		
            	}
            }
            olemas = true;
        	
        }
        
        if(nel == 1){
        	aa = aa + "4," ;
        	adde  = true;
        	Calendar cd = Calendar.getInstance();
            if(cd.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY){
            	cd.add(Calendar.DAY_OF_YEAR, 7);
            	bb = bb + cd.getTimeInMillis();
            }else{
            	while(cd.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY){
            		cd.add(Calendar.DAY_OF_YEAR, 1);
            	}
            	if(olemas){
            	  	bb = bb + "," +cd.getTimeInMillis() + "";
                }else{
            	  	bb = cd.getTimeInMillis() + "";
               }
            }
            olemas = true;
        	
        }
        
        if(ree == 1){
        	aa = aa + "5," ;
        	adde  = true;
        	Calendar cd = Calendar.getInstance();
            if(cd.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
            	cd.add(Calendar.DAY_OF_YEAR, 7);
            	bb = bb + cd.getTimeInMillis();
            }else{
            	//System.out.println("reede " + cd.getTime().toLocaleString());
                
            	
            	while(cd.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY){
            		cd.add(Calendar.DAY_OF_YEAR, 1);
            	}
            	
            	//System.out.println("reede0 " + cd.getTime().toLocaleString());
                
            	if(olemas){
            	  	bb = bb + "," +cd.getTimeInMillis() + "";
                    
            	}else{
            	  	bb = cd.getTimeInMillis() + "";
                      		
            	}
            }
            olemas = true;
        	
        }
        
        if(lau == 1){
        	aa = aa + "6," ;
        	adde  = true;
        	
        	Calendar cd = Calendar.getInstance();
            if(cd.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
            	cd.add(Calendar.DAY_OF_YEAR, 7);
            	bb = bb + cd.getTimeInMillis();
            }else{
            	while(cd.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY){
            		cd.add(Calendar.DAY_OF_YEAR, 1);
            	}
            	if(olemas){
            	  	bb = bb + "," +cd.getTimeInMillis() + "";
                    
            	}else{
            	  	bb = cd.getTimeInMillis() + "";
                      		
            	}
            }
            olemas = true;
        	
        }
        
        if(pyh == 1){
        	aa = aa + "7" ;
        	adde  = true;
        	
        	Calendar cd = Calendar.getInstance();
            if(cd.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            	cd.add(Calendar.DAY_OF_YEAR, 7);
            	bb = bb + cd.getTimeInMillis();
            }else{
            	while(cd.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY){
            		cd.add(Calendar.DAY_OF_YEAR, 1);
            	}
            	if(olemas){
            	  	bb = bb + "," +cd.getTimeInMillis() + "";
                    
            	}else{
            	  	bb = cd.getTimeInMillis() + "";
                      		
            	}
            }
            olemas = true;
        	
        }
        
        
        if(adde){
        	if(aa.endsWith(",")){
        		aa = aa.substring(0, aa.length()-1);
        	}
        	cust.setCity(transa + aa);
        }
        
        
        if(bb  != null && bb.length() > 0){
        	cust.setDelDays(bb);
        }
        
        
        

        String cate  = rs.getString("category");
        if (cate != null && cate.trim().length() > 0){
        	 String cate2  = rs.getString("cathelp");
        	 if (cate2 != null && cate2.trim().length() > 0){
        		 if(!cate.trim().equals(cate2.trim())){
        				cate = cate.trim() + "-" + cate2.trim(); 
        		        			 
        		 }
        	 }
          cust.category = cate.trim(); 
        }
        
                        customers.put(name, cust);
                        custMap.put(cust.getID(),cust);	
                    
      }
      
      Customer cc = new Customer("NEW CUSTOMER");
      cc.setName("NEW CUSTOMER");
      cc.setAddress("Somewhere in Lithuania");
      Enumeration enume = getParent().getAgentsHash().elements();
      while (enume.hasMoreElements()) {
        Agent a = (Agent) enume.nextElement();
        cc.addAgent(a);
      }
      customers.put("NEW CUSTOMER", cc);
      custMap.put(cc.getID(),cc);	
      

      Vector nosend = new Vector();
      nosend.add("MAXIMA");
      
      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        String kl = rs.getString("klient").trim();
                        debt.setCustID(kl);
                        debt.setPayerID(kl);
                        String number = rs.getString("number").trim();
                        debt.setDoknr(number);
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(rs.getDouble("tasumata"));
                        debt.setDue(rs.getString("yle"));
                        if(!nosend.contains(debt.getCustID())){
                        	  this.debt.addElement(debt);
                           
                        }
                      }

   rs.close();
   stmt.close();
   con.close();
   
    } catch (Exception e){
      getParent().showError(e);
    }
    
    if(rcompany != null){
			// Converter.sendCustomerJson(customers, rcompany, false,user,pass);
		    }
    
    return customers;
  }



  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, "dba", "sql");
  }


      public String getDistribution() {
        return null;
      }


  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "  select prr_kod as liik,prr_pav as nimetus from prrus where prr_vie = 0  ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = Converter.cleanLitLatEst(rs.getString("liik").trim());
        tm.put(liik, Converter.enCodeEst(rs.getString("nimetus").trim()));
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}


  public TreeMap getProdGroupsSecond() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    
    String    sql = " SELECT  pre_tek1 as liik, kli_pav  as nimetus FROM prekes p inner join klient_tiekejai s on "
    		+ " s.kli_kod= pre_tek1 where pre_rus = 'PREKĖ' group by pre_tek1,kli_pav";
    
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = Converter.cleanLitLatEst(rs.getString("liik").trim());
        tm.put(liik, Converter.enCodeEst(rs.getString("nimetus").trim()));
    }
    rs.close();
    st.close();
    con.close();
    return tm;

  }

  String warelist = "'CHI DIDMEN','CHI STUDI.','107','MP KLAIPĖD','TRISKELL K'";
  public TreeMap getProducts(Agent agent){
	  
	String  warelist2 = "'LOMA','LASIO','IMAGE','ERAYBA','ONMACABIM','MP PROFESI','MILANOBRUS','TRISKELL','SKEYNDOR','CENTR.','BIOREPEEL'";

    String cmd = "SELECT pre_kod as itemid, pre_pav AS nimetus, pre_min AS kastis,'' AS alusel, pre_par1/1.21 AS price  " +
", pre_bar AS ean ,pre_rus AS grupp, pre_tek1 as grupp2,mat_kod As unit, 21 as vat , " +
" pre_ska-pre_rez as seis ,pre_par6 AS mindisc,pre_rus  ,pre_uzs as opti , PRE_INF as topstatus FROM prekes where length(pre_rus) > 0 and  pre_par1 > 0";
    
	String ladu = " select pre_kod as toode , sum(sp1_ska)  \r\n" + 
			"    	 as seis from preks1_pkksar   where  san_kod in (" + warelist + ") \r\n" + 
			"		 group by pre_kod having sum(sp1_ska) > 0 order by pre_kod " ;
			
    
  
    
    String sqlPR = " select pre_kod AS toode, max(par_k) AS price  " +
    		" FROM Nkrp1 pd where par_val = 'EUR' group by pre_kod"; 

    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    HashMap seisud = new HashMap();
    HashMap prodHash = new HashMap();
    HashMap prices = new HashMap();
    
    try {
        Connection con = this.getNewConnection();
    	Statement stmt =  con.createStatement();
    	
    	ResultSet rs = stmt.executeQuery(ladu);
        while(rs.next()){
        	double seis = rs.getDouble("seis");
        	if(seis > 0){
        		   seisud.put(rs.getString("toode").trim(),seis);
        	}
        }
    	
      
    	
        
         rs = stmt.executeQuery(sqlPR);
        while(rs.next()){
        	String too = rs.getString("toode").trim();
        	double hh = rs.getDouble("price");
        prices.put(too ,hh);
        }
    	
        
        
     rs = stmt.executeQuery(cmd);
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimetus").trim()  ;
   
  
        prod.setInCases(rs.getDouble("kastis"));
        double price = rs.getDouble("price");
        prod.setPrice(price);

      String ean = rs.getString("ean");
      if(ean!=null && ean.trim().length()>0){
         prod.setEAN(ean);
        }

      String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
        prod.setGroup(Converter.cleanLitLatEst(grupp.trim()));
        }

       String grupp2 = rs.getString("grupp2");
       if(grupp2 !=null && grupp2.trim().length()>0){
          prod.setGroupSecond(Converter.cleanLitLatEst(grupp2.trim()));
        }

      prod.setName(nimetus);

      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit);
    }
      
      String vat = rs.getString("vat");
      if(vat!=null && vat.trim().length()>0){
       prod.setVat("21");
    }   
      
      if(seisud.get(prod.getID()) != null){
           try{
         	  prod.setStock(Double.parseDouble(seisud.get(prod.getID()) +""));
           }catch(Exception e){
           }
      }else{
    	  prod.setStock(0);
      }
      
     
      
      double mindisc = rs.getDouble("mindisc");
      if(mindisc >0 ){
    	//  prod.setMaxDiscount(mindisc);
      }
      

      double opti = rs.getDouble("opti");
      if(opti >0 ){
    	  prod.setOptAmountInUnits(opti);
      }


      String top = rs.getString("topstatus");
      if(top != null && top.trim().equals("PRIOR") ){
    	  prod.setStatusTop("1");
      }
      
        products.put(nimetus, prod);
        prodHash.put(prod.getID(), prod);
        prodids.add(prod.getID());
      }
      rs.close();
      stmt.close();
      con.close();
    } catch (Exception e){
      getParent().showError(e);
    }
    
    
    if(rcompany != null){
    	//Converter.sendProductJson(products, rcompany);
    }

    return products;
  }




  public Vector getCustomerProductsList(String agentCode){
    Vector custProds = new Vector();
    HashMap custs = new HashMap();
    //if (true) return custProds ;
   String cmd = " SELECT kli_kod +'-'+ case when adr_kod is null or adr_kod= '' then '00' else adr_kod end as customer,pre_kod as itemid ,pre_ska as qty,did_dat AS lastdate , \r\n" + 
   		"   		 max(did_pav) as info , pir_sumv as sss,pre_ska as lastprice FROM pard d inner join pard1 r on r.id_pard=d.id_pard where did_dat > getdate()-120  \r\n" + 
   		"     GROUP BY kli_kod +'-'+ case when adr_kod is null  or adr_kod= '' then '00' else adr_kod end ,pre_kod ,did_dat , pre_ska ,pir_sumv\r\n" + 
   		"     ORDER BY kli_kod +'-'+ case when adr_kod is null or adr_kod= '' then '00' else adr_kod end ,pre_kod  ,did_dat desc,pre_ska " ;
  
   if (isDebug()) System.out.println(cmd);

    try {
    	Connection con = getNewConnection();
    	Statement stmt =  con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      while(rs.next()){
    	  
        String kliendikood = rs.getString("customer").trim();
        String tootekood = rs.getString("itemid").trim();

        if(!custs.keySet().contains(kliendikood+ tootekood)){
		        double kogus = rs.getDouble("qty");
		        String lastdate = rs.getString("lastdate").substring(0,10);
		        String info = rs.getString("info");
		        if(info == null){
		        	info = " ";
		        }else{
		        	info = info.trim();
		        }
		        if(info.length()==0){
		        	info = " ";
		        }
		        double lasprice = rs.getDouble("sss");
		        double lastprice = rs.getDouble("lastprice");
			       if(lastprice != 0){
			    	   lastprice =   lasprice/lastprice;   
			       }
		        
		        lastdate = lastdate.substring(0,10);
		        custs.put(kliendikood + tootekood,lastdate); 
		        lastdate = lastdate.substring(5,10); 
		                 
		        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,lastprice,0,0," ");
		        cp.setAbiKood(info);
		        custProds.add(cp);
        }
        
      }
      rs.close();
      stmt.close();
      con.close();
          
    } catch (Exception e){
      getParent().showError(e);
         }
    return custProds;
  }




  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


    public void setBulkSales(Map agents){
  this.connectionUp();
    
      
      
      String cmd ="  SELECT adr_poz5 AS agent, round(SUM(pir_suml-pir_nl), 0) AS summa FROM pard sal " +
     " INNER JOIN PARD1 on pard1.id_pard=sal.id_pard " +
     " INNER JOIN klient k ON k.kli_kod = sal.kli_kod " +
     " INNER JOIN adresas adr ON k.kli_kod = adr.kli_kod AND sal.adr_kod = adr.adr_kod " +
 " WHERE YEAR(did_dat) = YEAR(getdate()) AND MONTH(did_dat) = MONTH(getdate()) AND adr_poz5 != '' " +
 " GROUP BY adr_poz5 ";

         if (isDebug()) System.out.println(cmd);
            try {
            	Connection con = this.getNewConnection();
                Statement stmt =  con.createStatement();
                ResultSet	      rs = stmt.executeQuery(cmd);
                while(rs.next()) {
                  String agentID = rs.getString("agent").trim();
                  Agent agent = (Agent)agents.get(agentID);
                  if (agent != null){
                    agent.setMonthSales((int)rs.getDouble("summa"));
                   // System.out.println(agent.getMonthSales());
                  }
                }

                rs.close();
                stmt.close();
                con.close();
            } catch (Exception e) {
                getParent().showError(e);
             }
          }

    public double getFoodSales(String agentCode) throws SQLException{
      return 0;
    }

    public double getAlcoSales(String agentCode) throws SQLException{
      return 0;
    }

    public ManagerData getManagerData() throws SQLException{
      
      return null;
    }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
    }


  public String getStartingTime(){
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, - Integer.parseInt(getParent().getProps().getProperty("months")));
    int month = cal.get(Calendar.MONTH) + 1;
    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    String sqlDate = cal.get(Calendar.YEAR) + "-" + month + "-" + day;
    return sqlDate;
  }

  public String getDriverVersion(){
    return "GlobusTrade";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent){



	       String sql2 =  "SELECT sal.orderid AS arve, sal.orderdate AS kuupaev,cus.name AS klient, count(rid.itemid) AS summa  " +
	    " FROM perinttelematable sal INNER JOIN custtable cus ON sal.CUSTACCOUNT=cus.accountnum  " +
	    " INNER JOIN perinttelemaline rid ON sal.orderid=rid.orderid and sal.dataareaid=rid.dataareaid  WHERE sal.dataareaid='lin' AND cus.dataareaid='lin'  AND rid.dataareaid='lin' AND cus.salesrep ='" + agent.getID() + "' AND " +
	    " sal.ORDERDATE > getdate()-0.8  and referenceno = '' GROUP BY sal.orderid, sal.orderdate , cus.name ORDER BY sal.orderdate";

	    StringBuffer sb = new StringBuffer();
	    sb.append("\n\n\n Vormistatud tellimused:\n\n");

	      try {
	              /*    ResultSet	      rs2 = stmt.executeQuery(sql2);
	                  while (rs2.next()) {
	                  sb.append("(" + rs2.getString("arve").trim() + ") [" + rs2.getString("kuupaev").substring(0,10) + "] - " + rs2.getString("klient") + "  Ridu: " + rs2.getDouble("summa") + "\n");
	                  }

	        //stmt = con.prepareStatement(sql);
	        //stmt.sets(1, agent.getObject());
	          //stmt.setString(2, agent.getObject());
	                  /* 	    String sql =" SELECT  (SELECT SUM(PHYSICALINVENT)FROM INVENTSUM summ INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid WHERE summ.DATAAREAID='bal' and dim.DATAAREAID='bal'" +
	"AND CLOSED = 0 AND dim.inventlocationid ='" + agent.getObject() + "' " +
	"AND summ.itemid=inv.itemid GROUP BY summ.itemid) AS ladu,SUM(sal.qty) AS kogus,avg(sal.lineamountmst/sal.qty) AS myygihind, " +
	"inv.itemname AS nimetus, inv.itemqtyinabox AS tykke " +
	"FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid  WHERE " +
	"inv.dataareaid='bal' AND sal.dimension='" + agent.getID() + "' AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate()) AND DAY(sal.invoicedate) = DAY(getdate()) " +
	"GROUP BY inv.itemname ,inv.itemqtyinabox,inv.itemid ORDER BY inv.itemname ";
   ResultSet	      rs = stmt.executeQuery(sql);
	          ResultSetMetaData md = rs.getMetaData();
	          sb.append("Nimi  \t\tLadu\t\tMüüdud\t\tSumma\n");
	          sb.append("---------------------------------------------------\n");

	          double totalKokku = 0d;

	          while (rs.next()) {
	            int ladu = rs.getInt("ladu");
	            int kogus = rs.getInt("kogus");
	            double hind = round(rs.getDouble("myygihind"), 2);
	            double kokku = round(kogus*hind, 2);
	            totalKokku = round(totalKokku + kokku, 2);

	            sb.append(rs.getString("nimetus").trim() + " " + rs.getInt("tykke")+ "\n");

	            sb.append("\t\t\t\t" + ladu + "\t\t" + kogus + "\t\t" +
	              kokku + "\n\n");
	           }

	          sb.append("---------------------------------------------------\n");
	          sb.append("\tPäeva müük kokku: " + totalKokku + " EUR");

*/
	                  } catch (Exception e) {
	                      getParent().showError(e);
	                  }


	      return sb.toString();
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
  private String url = "jdbc:odbc:finvalda";
  private   Vector debt = new Vector();
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
