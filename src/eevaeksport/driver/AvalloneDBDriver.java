/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.InventoryLine;
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
public class AvalloneDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public AvalloneDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");

        // String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

        try {
            Class.forName(databaseDriverName).newInstance();

        } catch (Exception e) {
            parent.showError(e);
        }
        
        user =  getParent().getProps().getProperty("rest.user");
        pass = getParent().getProps().getProperty("rest.pass");
        rcompany = getParent().getProps().getProperty("rest.companyx");
         }
      String user =  null;
      String pass = null;
      String rcompany = null;

      private Hashtable custCodes = null;
      
      public Vector getPriceMatrixData()  {
          Vector lines = new Vector();
          String    sql = " SELECT mt.klient AS custid,mt.klliik AS clgroup,toode AS prodid,mt.artliik AS prodgroup,mt.hinnak AS discount,mt.hind " + 
          		"           AS price ,lopp-algus as paevi, algus , lopp  FROM hinnamtr mt inner join hkpais p on p.hinnak = mt.hinnak where " + 
          		"           algus <= date() and ( date()<=lopp or year(lopp) = 0 ) order by mt.klient,mt.klliik,mt.toode ";
            Statement st = null;
            try {
          	  
              connectionUp();
              st = con.createStatement();
              ResultSet rs = st.executeQuery(sql);

              while (rs.next()) {

                String custID = rs.getString("custid");
                String clGroup = rs.getString("clgroup");
                String prodID = rs.getString("prodid");
                String prodGroup = rs.getString("prodgroup");
                String discount = rs.getString("discount");
                double price = rs.getDouble("price");
                int paevi = rs.getInt("paevi");
                String algus = rs.getString("algus");
                String lopp = rs.getString("lopp");
                
                MatrixLine mline = new MatrixLine();
       
             if(paevi < 90) {

                if (lopp != null) {
  					mline.setEndDate(lopp.substring(0, 10));
  				}
  				if (algus != null) {
  					mline.setStartDate(algus.substring(0, 10));
  				}
  				
  			  } 

                if (custID != null && custID.trim().length() > 0) {
                  mline.setCustID(custID.trim());
                }
                if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                  mline.setCliGroup(clGroup.trim());
                }

                if (prodID != null && prodID.trim().length() > 0) {
                  mline.setProdID(prodID.trim());
                }
                if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                  mline.setProdGroup(prodGroup.trim());
                  price = 0;
                }
                if(discount!= null && !discount.trim().equals("HI") && !discount.trim().equals("K4") && !discount.trim().equals("K5")) {
                	try {
                	   	   mline.setDiscount(Double.parseDouble(discount.trim()));
               	            		
                	}catch(Exception e) {
                		e.printStackTrace();
                	}
                }
               
                if (price != 0) {
                  mline.setPrice(price);
                }
                if(price>0 || (!discount.equals("HI") && !discount.equals("K4") && !discount.equals("K5")))
               lines.add(mline);
              }

              rs.close();
              st.close();
              this.connectionDown();
            }
            catch (SQLException ex) {
            }
            
                      return lines;
        }
      
      
      
    public String getSaveDebtInfo() {
    	// System.out.println("xxxxxyyyy");
    	        File dir = new File("import");
    	        File block = new File("block.txt");
    	        if(block.exists()){
    	        	System.exit(0);
    	        }
    	        try {
					saveObject("",block.getName());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    	        
    	        File[] files = dir.listFiles();
    	        for (int i = 0; i < files.length ; i++){
    	          File file = files[i];
    	  //        System.out.println("xxxxxyyyy");
    	          if (file.isFile()){
    	            Vector vec = null;
    	            try {
    	            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
    	           
    				
    					vec = (Vector)ois.readObject();
    					ois.close();
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				} catch (ClassNotFoundException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    		//		System.out.println("xxxxx");
    	             insertStock(vec);
    	             File done = new File("import/done/" + file.getName());
    	             if(done.exists()){
    	             	done.delete();
    	             }
    	            file.renameTo(done);
    	             file.delete();
    	            
    	          }
	         
    	        }
    	        
    	        if(block.exists()){
    	        	block.delete();
    	        }
    	        System.exit(0);
    	    	
    	    	return null;
    	       }

    
    
    private void insertStock(Vector vec){
    	if(vec != null){
    		
    		
    		int doknr = 0;
    		
    		try{
    			String sql1 = "select dok.dokumnr from dok inner join rid on dok.dokumnr=rid.dokumnr " +
              " where dok.doktyyp='K' and rid.doktyyp='K' and dok.kinnitatud = 0 and dok.inventuur=1 and dok.kuupaev > date() -5 " +
              "and trim(dok.tekst1)= ''";
    			 con = DriverManager.getConnection(url);
    			 System.out.println(sql1);
        		Statement st = con.createStatement();
        		ResultSet rs = st.executeQuery(sql1);
        		if (rs.next()){
        			doknr = (rs.getInt("dokumnr"));
        		}
        		rs.close();
        		st.close();
        		con.close();
        			
    		}catch (Exception e){
    			e.printStackTrace();
    		}
    		
    		System.out.println("Dok nr  : " + doknr);
    		
    		
    		
    		if (doknr >0){
    		
    		
    		Enumeration enume = vec.elements();
    		while(enume.hasMoreElements()){
    			InventoryLine lin = (InventoryLine)enume.nextElement();
    			String objekt = lin.getCustId();
    			String location = lin.getLocation();
    			double kogus  = lin.getAmount();
    			String ean = lin.getEan();
    			String prName = lin.getProdName();
    			String wrk = lin.getWorker();
    			String wrkname = lin.getWorkername();
    			String prodid = lin.getProdId();
    			
    			java.sql.Date entered = new java.sql.Date(lin.getEnterTime().getTime());
    	
				
    			String sqq = "select kogus from rid where dokumnr= " + doknr +" and toode ='" + prodid + "' and doktyyp ='K' ";
    			 try{
    			  
    			 	
    			 	con = DriverManager.getConnection(url);
    			
    			Statement stq = con.createStatement();
    			ResultSet rsq = stq.executeQuery(sqq);
    			double kogusq = 0;
    			if(rsq.next()){
    				kogusq = rsq.getDouble("kogus");
    			}
    			
    			
    			System.out.println(objekt + " " + location + " " + kogus + " " +  prodid + " " + 
    					ean + " " + prName+ " " + wrk + " " + wrkname+ " " + entered.toString() + " " + kogusq);
    			
    			 String sql = " UPDATE RID SET  rtaitkogus= ? WHERE dokumnr =" + doknr + " and doktyyp ='K' and toode ='" + prodid + "'";
    		
    		
    				  con = DriverManager.getConnection(url);
     		        PreparedStatement stpr = con.prepareStatement(sql);
     		        stpr.setDouble(1,kogus + kogusq);
     		      try{
     		        stpr.executeUpdate();  
    			 }catch (Exception e){
    			 	this.log(e.toString());
    			 	log(objekt + " " + location + " " + kogus + " " +  prodid + " " + 
        					ean + " " + prName+ " " + wrk + " " + wrkname+ " " + entered.toString() + " " + kogusq);
    			 	
    			 }
     		      	System.out.println(sql  + "Rida updatetud");
     		      
     		        stpr.close();
     		        con.close();
     		        
    			 }catch (Exception e){
    			 	e.printStackTrace();
    			 }
    		
    			
    		}
    		
    		String sss = "update dok set tekst1='Sisselugemine tehtud' where doktyyp='K' and dokumnr=?"  ;
    		try {
				con = DriverManager.getConnection(url);
			
		        PreparedStatement stpr = con.prepareStatement(sss);
		        stpr.setInt(1,doknr);
		      	stpr.executeUpdate();  
		      	System.out.println(sss  + "Tekst muudetud");
		      
		        stpr.close();
		        con.close();
		        
    		} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		
   		  }
    	}
    }

    
    
    
           
    public String getSaveStockInfo(){
 
    	 
    	 connectionUp();
   		String cmd =  "  select  toode.aktsiis,dok.dokumnr , rid.toode  from dok inner join klient on dok.klient = klient.kood inner join rid on rid.dokumnr = dok.dokumnr inner join toode on rid.toode = toode.toode\r\n" + 
   				" where kuupaev > date()-120 and dok.doktyyp='G' and (otsekorral != '' or dok.klient like 'ARAXES%')  and dok.yksus = '1' and rid.toode != 'KM' " ;
   	Vector doks = new Vector();
   		log(cmd);
   		
   		try {

   			Statement stmt = con.createStatement();		
   			ResultSet	      rs = stmt.executeQuery(cmd);
   			Connection cc = DriverManager.getConnection(url);
   			while(rs.next()) {
   				String sss = "update rid set kulum = " + rs.getDouble("aktsiis")+ " where dokumnr = " + rs.getString("dokumnr") +
   						" and toode ='" + rs.getString("toode").trim() + "'";
   				Statement stm = cc.createStatement();
   				stm.executeUpdate(sss);
   			    stm.close();
   			System.out.println("Tehtud " + sss);
   			}
   			rs.close();
   			cc.close();
   			stmt.close();
   			con.close();
   		}catch (Exception e) {
   			
   		}
   			
    	 
   			
   		 
    	 return null ;
    	
    	
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
          		String cmd =  " SELECT sum(rid.kogus*(rid.hind))AS sales, " +
          		 " dok.pais2obj  AS agent FROM dok " +
                 " INNER JOIN rid ON dok.doktyyp+STR(dok.dokumnr,7)=rid.doktyyp+STR(rid.dokumnr,7)  INNER JOIN yksus y ON y.yksus = dok.pais2obj " +
                 "  WHERE  len(trim(dok.klient)) > 1  AND YEAR(dok.kuupaev)=YEAR(date()) " +
                 "  AND MONTH(dok.kuupaev)=MONTH(date()) AND y.klassif=2 AND (dok.doktyyp = 'G' OR dok.doktyyp = 'Y')" +
                 "  AND (rid.doktyyp ='G' or rid.doktyyp ='Y') " +
          		 "  AND dok.yksus in ('1','3') AND len(trim(rid.toode))>2  GROUP BY  dok.pais2obj" ;
          	
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
        agent1 = new TreeMap();
        String  cmd = "SELECT yksus,nimetus,asendus FROM yksus WHERE klassif=2";
        TreeMap agents = new TreeMap();

	    Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	
        try {
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              agent.setManager(true);
              String name = rs.getString("nimetus").trim();
              String asendus = rs.getString("asendus");
              agent.setLanguage(asendus);
              agent.setName(name);
              
              agent.setIsPilot(true);
              agent.setLanguage("EST");
              agent.setCountry("et");
              agent.setPassWord("0000");
              String email = " ";
              
              if(propse.get(agent.getID()) != null){
            	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
              
            	  if(email.length() > 3){
                	  agent.setEmail(email);
                	
                  }
                  if(email.length() > 0){
                	  agent.setActive(true);
                	  agent1.put(agent.getID(), agent);
                	  agents.put(name, agent);
                  }
              }
                 
             
               
             
            }
            
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        Agent aa = new Agent("HOUSE");
        aa.setName("HOUSE");
        agents.put("HOUSE", aa);
        return agents;
    }

    private TreeMap agent1 = new TreeMap();
    
    public TreeMap getCustomers(Agent agent) {
        connectionUp();
    	
        custCodes = new Hashtable();
        try{
     	   Converter.getManagersReplacements(agent1, rcompany, user, pass); 
     		
     }catch(Exception e){
     	e.printStackTrace();
     }
        // String cmd = "SELECT k.kood, k.nimi FROM (klient k INNER JOIN omrid ON k.kood = omrid.klient INNER JOIN rid ON rid.dokumnr = omrid.tasudjrk) WHERE omrid.rid2obj='" + agentCode + "' AND (len(rid.toode) > 2) GROUP BY k.kood";
        String cmd =
            "SELECT k.kood, k.nimi,TRIM(k.tanav) + ' ' + trim(k.piirkond) AS aadress,' ' AS ale,k.riik AS visit,email ,k.objekt2 " +
          "  AS agent,k.maksja , telefon , piirkond, tanav, postiindek as indeks, kontaktisi as kontakt, "
          + "email,k.objekt7 as chain, k.liik as category,keeld as blokk FROM klient k where (k.objekt2 !='' or k.kood like '%UUSKLIENT%') and k.mitteakt=.F. ORDER BY k.nimi " ;

        log(cmd);

        TreeMap customers = new TreeMap();
        HashMap custMap2 = new HashMap();
        try {
            ResultSet	      rs = stmt.executeQuery(cmd);
           
            while (rs.next()) {
              Customer cust = new Customer(rs.getString("kood").trim());
            String nimi = rs.getString("nimi").trim();
            cust.setName(enCode(nimi));

            String visit = rs.getString("visit");
            if (visit != null){
              cust.setVisit(visit.trim());
            }

            String aadress = rs.getString("aadress") ;
            if (aadress != null){
            	String aadr = aadress.trim();
            	aadr = "." + aadr + ",Estonia.";
              cust.setAddress(enCode(aadr));
            }
            cust.setDiscount(rs.getDouble("ale"));
            String email = rs.getString("email");
            if (email != null && email.trim().length()>0){
//            cust.setConfirmEmail(email.trim());

            }
            
            String ag = rs.getString("agent");
            if(ag != null && ag.trim().length() >0){
            	
            cust.addAgent(new Agent (ag.trim()));
           	 Agent aa = (Agent)getParent().getAgentsHash().get(ag);
           	 if(aa != null && aa.getLanguage() != null){
           		 cust.addAgent(new Agent (aa.getLanguage().trim()));
           	 }
           	 
            	
            	Agent aa1 = (Agent)agent1.get(ag.trim());
            	if(aa1 != null) {
            	
    			Vector vec = aa1.getDubbler();
				if(vec!= null && vec.size() >0){
					Enumeration en = vec.elements();
					while(en.hasMoreElements()){
						String age = (String) en.nextElement();
						cust.addAgent(new Agent(age));
					}
				}
            }
            	
            	
        	}
        
        			
        			

        	if (cust.getID().equals("UUSKLIENT")) {
                Enumeration enume = getParent().getAgentsHash().elements();
                while (enume.hasMoreElements()) {
                  Agent a = (Agent) enume.nextElement();
                  cust.addAgent(a);
                }
              }
            
            String payer = rs.getString("maksja");
            if(payer != null && payer.trim().length() >0){
            	cust.setPayer(payer.trim());
            }
            String telefon = rs.getString("telefon");
            if(telefon != null && telefon.trim().length() >0){
            	cust.setPersonalData(enCode("Tel:" + telefon.trim().replaceAll(";", ":")));
            }
            
            String piirkond = rs.getString("piirkond");
            if(piirkond != null && piirkond.trim().length() >0){
            	cust.restCity = piirkond.trim();
            }
            String tanav = rs.getString("tanav");
            if(tanav != null && tanav.trim().length() >0){
            	cust.restStreet = tanav.trim();
            }
            
            String indeks = rs.getString("indeks");
            if(indeks != null && indeks.trim().length() >0){
            	cust.restZipCode = indeks.trim();
            }
            
            String kontakt = rs.getString("kontakt");
            if(kontakt != null && kontakt.trim().length() >0){
            	if(cust.getPersonalData() != null && cust.getPersonalData().length() > 0){
            		cust.setPersonalData(enCode(kontakt.trim().replaceAll(";", ":") + "|"+ cust.getPersonalData()   ));
            	}else{
            		cust.setPersonalData(enCode(kontakt.trim().replaceAll(";", ":") ));	
            	}
            	
            }
            
            
            String chain = rs.getString("chain");
            if(chain != null && chain.trim().length() >0){
            	cust.setChain(chain.trim());
            }
            
            String category = rs.getString("category");
            if(category != null && category.trim().length() >0){
            	cust.category = category.trim();
            	cust.setGroup(category.trim());
            }
            
            int keeld = rs.getInt("blokk");
            if(keeld == 1) {
            	cust.setSalesBlockT(true);
            }
            
                customers.put(nimi, cust);
                custMap2.put(cust.getID(), cust);
            }


            String sql = "SELECT d.klient AS klient, SUM(d.tasumata) AS summa  from dok d  WHERE  " +
              "  date()>d.tasukuup and d.doktyyp='G' AND d.tasumata <> 0 AND len(klient) > 0 GROUP BY d.klient ORDER BY d.klient";
           ResultSet   rst =  stmt.executeQuery(sql);
            while(rst.next()){
              String id = rst.getString("klient").trim();
              double debt = Double.parseDouble(rst.getString("summa").trim());
       
            Iterator iter = custMap2.values().iterator(); 
            //System.out.println("Olemas2 "  + " " + id + " " + debt);
            while (iter.hasNext()){
            	Customer cc = (Customer)iter.next();
            	// 
            	if(cc.getPayer().equals(id) || cc.getID().equals(id)){
            		cc.setDebt(debt);       		
            		//System.out.println("Olemas222 " + cc.getID() + " " + cc.getPayer() + " " + id + " " + debt);
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

    
    private String enCode(String res ){
	  	 

	  	res =res.replaceAll("�","11111xx");
	  	res =res.replaceAll("�","11112xx");
		res =res.replaceAll("�","11114xx");
		res =res.replaceAll("�","11113xx");

		res =res.replaceAll("�","11115xx");
		res =res.replaceAll("�","11116xx");
		res =res.replaceAll("�","11117xx");
		res =res.replaceAll("�","11118xx");
		
		res =res.replaceAll("�","11119xx");
		res =res.replaceAll("�","11120xx");
		res =res.replaceAll("�","11121xx");
		res =res.replaceAll("�","11122xx");
	
	  	return res;
	  }
    

        public String getDistribution() {
          return null;
        }

        
        
        

    public TreeMap getProducts(Agent agent) {
        connectionUp();
    Hashtable dateOut = new Hashtable();

	TreeMap products = new TreeMap();
	TreeMap sto1 = new TreeMap();
	TreeMap sto3 = new TreeMap();
	try {
   
		
        String sql3 = " select toode , kogus as kokku from seisud  " +
        "   where  osak='1' order by toode  " ;
				
				try{
				PreparedStatement pstm = con.prepareStatement(sql3);
				ResultSet rs6 = pstm.executeQuery();
				
				while(rs6.next()){
				sto1.put(rs6.getString("toode").trim(), rs6.getDouble("kokku"));
				}
				rs6.close();
				pstm.close();
				}catch (Exception e){
				e.printStackTrace();
				}   
				
				
				
				String sql4 = " select toode , kogus as kokku from seisud  " +
				        "   where  osak='3' order by toode  " ; 
				
				try{
				PreparedStatement pstm1 = con.prepareStatement(sql4);
				ResultSet rs7 = pstm1.executeQuery();
				
				while(rs7.next()){
				sto3.put(rs7.getString("toode").trim(), "VIA:" + rs7.getDouble("kokku") + "|");
				}
				rs7.close();
				pstm1.close();
				}catch (Exception e){
				e.printStackTrace();
				}   


		
		
		
                  
        // String cmd = "SELECT toode, nimetus, yhik, myygihind FROM toode WHERE (len(trim(toode)) > 3) AND (trim(toode.grupp) = 'L')";
         String  cmd = "SELECT toode, rusnimetus ,nimetus , kastis as tykke, myygihind, yhik,liik AS grupp,ribakood AS ean,markused,maxale,ended,tasusumma as link , toptoode as top , uuskuni as uus FROM toode WHERE " +
         " (myygikood = 'K' OR  myygikood = '5%' ) AND len(trim(toode)) >2 AND (keeld2=.F. )  ORDER BY toode";
       if (isDebug()) {
           	System.out.println(cmd);
           	}


          stmt = con.createStatement();
                      ResultSet rs = stmt.executeQuery(cmd);

                      while (rs.next()) {
                          String kood = rs.getString("toode").trim();
                          
                          
                      	String rusnimetus =rs.getString("rusnimetus");
                        String nimetus =rs.getString("nimetus");
                        Product prod = new Product(kood);
                        if(rusnimetus != null && rusnimetus.trim().length()>0){
                        	nimetus = rusnimetus.trim();
                        }
                        
                        if(kood.startsWith("RR")){
                        	nimetus = "ZZZ " + nimetus;
                        }
                        prod.setName(enCode(nimetus.trim()));
                                             
        
                          prod.setPrice(rs.getDouble("myygihind"));
                          String grupp = enCode(rs.getString("grupp"));

                          if(grupp != null && grupp.trim().length()>0){
                          prod.setGroup(grupp.trim());}
                          if(kood.startsWith("RR")){
                        	  prod.setGroup("RRR");
                          }
                          prod.setInCases(rs.getDouble("tykke"));
                          String ean = rs.getString("ean");
                          if (ean != null && ean.trim().length()>0){
                            prod.setEAN(ean.trim());
                          }
       
                          
                          
                          
                          double maxale = rs.getDouble("maxale");
                          if (maxale >0){
                              prod.setMaxDiscount(maxale);
                          }else{
                        	//  prod.setMaxDiscount(25);
                          }
       
                          int lopp = rs.getInt("ended");
                          if (lopp == 1) {
                        	  prod.setStatusSellout("1");
                          }
                          
        				String add ="";
        			
          			if(sto3.get(prod.getID()) != null){
          				add = sto3.get(prod.getID()) + "";
          			}
          				
          			if(sto1.get(prod.getID()) != null){
          				prod.setStock(Double.parseDouble(sto1.get(prod.getID())+""));
          			}
          				
          			 String link = enCode(rs.getString("link"));

                     if(link != null && link.trim().length()>10){
                    	 prod.weblink = link.trim();
                     }
          			
                     int top = rs.getInt("top");
                     
                     if(top == 1) {
                    	 prod.setStatusTop("1");
                     }
                     
                     Date dd = rs.getDate("uus");
          			
                     if(dd != null ) {
                    	 if(dd.getTime() > Calendar.getInstance().getTimeInMillis()) {
                    		 prod.setStatusNew("1");
                    	 }
                     }
          				
          				prod.setName(prod.getName() + add);
          				prod.setPartii(prod.getPartii() + add);
          				nimetus = prod.getName();
          				
          				          
                     
                          
                          products.put(nimetus, prod);

                      }
                      

        } catch (Exception e) {
            getParent().showError(e);
        }
        System.out.println("prod done");
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
     String    sql = "SELECT t.liik AS liik , l.artlnimi AS nimi FROM toode t INNER JOIN  " +
                 " ../andmed/artliik l ON l.liik=t.liik GROUP BY t.liik";
     
     ResultSet rs = stmt.executeQuery(sql);

     while (rs.next()) {
       String liik=enCode(rs.getString("liik").trim());
       String nimetus=enCode(rs.getString("nimi").trim());
       if (liik != ""){
       tm.put(liik,nimetus);
       }
     }
     rs.close();

     tm.put("RRR","Reklaam kaubad");
     
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
	String cmd =
	    "SELECT dok.saaja,rid.toode,dok.kuupaev,rid.kogus, rid.hind FROM dok INNER \r\n" + 
	    "	   JOIN rid ON dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode WHERE \r\n" + 
	    "	     len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 2 \r\n" + 
	    "	      AND dok.kuupaev >= date() -180 \r\n" + 
	    "	     AND  dok.doktyyp = 'G' AND rid.doktyyp= 'G' and rid.toode not in ('001','002','003','004') GROUP BY dok.saaja, rid.toode ORDER BY dok.saaja, toode.toode,dok.kuupaev";

	if (isDebug()) {
	    System.out.println(cmd);
	}

	Vector custProds = new Vector();

	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String kliendikood = rs.getString("saaja").trim();
		String tootekood = rs.getString("toode").trim();
		 String kuupaev = rs.getString("kuupaev");
        double kogus = Double.parseDouble(rs.getString("kogus").trim());
        double hind = Double.parseDouble(rs.getString("hind").trim());
     

                if (kliendikood.length() > 1) {
                  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,kuupaev.substring(5,10),hind,0,0," ");
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
      
      String sql = "SELECT saaja,tasudok AS number,tasukuup AS maksepaev,tasumata, kuupaev,(date()-tasukuup) AS yle FROM " +
                   " dok WHERE  doktyyp='G' AND tasumata<>0  " + // tasukuup<date()+10 AND
                   " ORDER BY saaja ";

      connectionUp();
      ResultSet rs = stmt.executeQuery(sql);
      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("saaja").trim());
        debt.setDoknr(rs.getString("number").trim());
        debt.setDuedate(rs.getDate("maksepaev"));

        debt.setSum(rs.getDouble("tasumata"));
        debt.setDate(rs.getDate("kuupaev"));
        debt.setDue(rs.getString("yle"));
        list.addElement(debt);
      }
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
    public String getPilotDayData(Agent agent){
      return null;
    }


    public String getNonMobileOrders(Agent agent){
      connectionUp();

     String sql = " SELECT tasudok AS arve,kuupaev,k.nimi,doksumma AS summa FROM dok d INNER JOIN klient k ON k.kood=d.klient " +
               " WHERE kalktoode!='MobileJava' AND kuupaev > date()-3 AND pais2obj=?";


      StringBuffer sb = new StringBuffer();

        try {
            PreparedStatement st = con.prepareStatement(sql);
            st.setString(1, agent.getID());
            String sep= System.getProperty("line.separator");
            ResultSet	      rs = st.executeQuery();

            sb.append("ARVE    Nimi  \t\t\tKuup�ev\t\tSumma" + sep);
            sb.append("------------------------------------------------------" + sep);
            while(rs.next()){
              sb.append(rs.getString("arve").trim() + "\t");
              sb.append(rs.getString("nimi").trim() + "\t");
              sb.append(rs.getString("kuupaev").trim() + "\t");
              sb.append(rs.getString("summa").trim() + sep);




            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        connectionDown();
        return sb.toString();
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

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "Avallone";
    private String	       url = "jdbc:odbc:eeva";
    private Connection	       con = null;
    private Statement	       stmt;
    private String	       subjectFilesTo = null;



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
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/
