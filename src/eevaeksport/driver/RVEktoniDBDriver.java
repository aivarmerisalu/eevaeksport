/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class RVEktoniDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public RVEktoniDBDriver(EksportPanel parent) {
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

    private String enCode(String res ){
     	return res;
    	//return Converter.enCodeEst(res);
     }
    
    public String getProductSortingData(Vector done){
       	StringBuffer sb = new StringBuffer();
       	return sb.toString();
       }

    public void setBulkSales(Map agents){
       // if (true)return;
        String cmd = "SELECT sum(pr.reasum) AS sales , agent " +
                    "  FROM osmy/my_toode pr INNER JOIN osmy/my_tell te ON te.nr=pr.recnr " +
					"  INNER JOIN osmy/firma f on f.kood = te.saaja " +
					"   WHERE  month(regaeg)=month(date()) AND year(regaeg)=year(date())    "+            
					"   GROUP BY agent";
        log(cmd);
       this.connectionUp();
          try {
            Statement st = con.createStatement();
              ResultSet	      rs = st.executeQuery(cmd);
              while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                  agent.setMonthSales((int)rs.getDouble("sales"));
                }
              }
              rs.close();
              st.close();
              con.close();
          } catch (Exception e) {
              getParent().showError(e);
          }
          this.connectionDown();

      }
    
    public String getSaveDebtInfo() {
       	return null;
       }
       
    public String getSaveStockInfo(){
  	  connectionUp(url);
  	  String  cmd = "  SELECT a.siffer AS toode,  (kak+tulek-minek) AS kogus   "+
	  " FROM osmy/lm_mat a INNER JOIN  " +
     " osmy/lm_kla k ON a.grupp = k.klkood " + 
      " LEFT JOIN ladu/ll010000 ll ON ll.siffer=a.siffer " +
      "  WHERE   tkood <> 'F' and  a.mghind  != 0 ORDER by a.nimi" ;


	       if (isDebug()) System.out.println(cmd);
	       StringBuffer products = new StringBuffer();
	       try {

	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmd);

	         while (rs.next()) {

	           String kood = rs.getString("toode").trim();
	           double seis = rs.getDouble("kogus");
	           if(seis > 0){
	               products.append(kood +  ";");
	               products.append(" ;");
	               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
	             }
	           }
	           rs.close();
	       } catch (Exception e) {
	           getParent().showError(e);
	       }
	       return products.toString();
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

                                                    public Vector getDebt(String agentCode) throws SQLException{
                                                        Vector list = new Vector();
                                                        String sql = "SELECT d.firma as payer,d.saaja AS klient,aa.nr AS number ,aa.vkuup AS maksepaev , " + 
                                                           " sum(aa.summa-aa.makstud) AS tasumata ,kuup AS kuupaev, date()-vkuup AS yle from osmy/my_arve aa " +
                                                  		 "  INNER JOIN osmy/my_tell d ON d.arvenr=aa.nr  where  " +
                                                           " aa.summa-aa.makstud !=0 and aa.vkuup <= date()  "+
                                                           " and aa.liik not like '%S%'  group by aa.nr,d.saaja";

                                                        connectionUp();
                                                        PreparedStatement st = con.prepareStatement(sql);
                                                        //st.setString(1, agentCode);
                                                        ResultSet rs = st.executeQuery();
                                                        while(rs.next()){
                                                          Debt debt = new Debt();
                                                          debt.setPayerID(rs.getString("payer").trim());
                                                          debt.setCustID(rs.getString("klient").trim());
                                                          debt.setDoknr(rs.getString("number").trim());
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

     private Connection getNewConnection(){
    	 Connection res = null;
    	 try {
    		res =  DriverManager.getConnection(url); 
    	   } catch (Exception e) {
             getParent().showError(e);
           }
    	return res;
     }


        public String getDistribution() {
          return null;
        }


    public TreeMap getProdGroups() throws SQLException {
    	connectionUp(url);
		TreeMap   tm = new TreeMap();
        String    sql = "select k.klkood as liik, k.nimi " +
           " FROM osmy/lm_mat a INNER JOIN osmy/lm_kla k ON a.grupp = k.klkood group by k.klkood order by k.nimi ";
        Statement st  = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String liik=rs.getString("liik").trim();
          String nimetus= enCode(tapitaht(rs.getString("nimi").trim()));
          if (liik != ""){
          tm.put(liik,nimetus);
          }
        }
        rs.close();
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
    
    private String tapitaht(String name ){
    	String res = "";
    	char[] b = name.toCharArray();
        byte[] bytes = null;
        int a = 0;
		String bb = "";
		try {
			bytes = name.getBytes(Charset.forName("UTF-8"));
			
			for(int i = 0;i < bytes.length; i++){
		      	  byte c = bytes[i];
		      	  bb= bb + "." + c;
		      	  char h = b[i-a];
		      			  if(c == 63){
		      				  h = 'Ü';
		      			  }
		      			  if(c == -53){
		      				  i++;
		      				  a++;
		      				  c = bytes[i];
		      				 bb= bb + "." + c;
		      				  if(c == -121){
		      					 h = 'Ä';	  
		      				  }
		      			  }

		      			  if(c == -61){
		      				  i++;
		      				  a++;
		      				  c = bytes[i];
		      				 bb= bb + "." + c;
		      				  if(c == -91){
		      					 h = 'Õ';	  
		      				  }
		      			  }
		      			if(c == -59){
		      				  i++;
		      				  a++;
		      				  c = bytes[i];
		      				 bb= bb + "." + c;
		      				  if(c == -125){
		      					 h = 'C';	  
		      				  }
		      			  }
		      			  if(c == -62){
		      				  i++;
		      				  a++;
		      				  c = bytes[i];
		      				 bb= bb + "." + c;
		      				  if(c == -66){
		      					 h = 'S';	  
		      				  }
		      			  }
		      			  if(c == -60){
		      				  i++;
		      				  a++;
		      				  c = bytes[i];
		      				 bb= bb + "." + c;
		      				  if(c == -123){
		      					 h = 'O';	  
		      				  }
		      				if(c == -115){
		      					 h = 'K';	  
		      				  }
		      				  
		      			  }

		      			  if(c == -30){
		      				  i++;
		      				  i++;
		      				  a++;
		      				a++;
		      				  c = bytes[i];
		      				 bb= bb + "." + c;
		      				  if(c == -94){
		      					 h = 'Ö';	  
		      				  }
		      			  }
		      			  
		      			  
		      	   res = res + h; 
		      	}
		} catch (Exception e) {
			System.out.println(a + " " + name + " "  + b.length + " " + bytes.length + " " + bb + "\n" + res);
					
			e.printStackTrace();
		}
        
       
    	
    	return res;
    }
    
    public TreeMap getAgents() {
        connectionUp(url);
        
       // getPriceMatrixData();
        
        String  cmd = "SELECT kood AS yksus,trim(enimi)+' '+ trim(pnimi)" +
                       " as nimetus, '1' AS objekt FROM osmy/isik  ORDER BY enimi";
        TreeMap agents = new TreeMap();

        try {
          PreparedStatement stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              agent.setObject(rs.getString("objekt").trim());
              String name = rs.getString("nimetus").trim();
             name = enCode(tapitaht(name));
             
              System.out.println(name.toCharArray());
				String agentAGENTS = getParent().getProps().getProperty("dub." +agent.getID());
				if(agentAGENTS != null){
    	           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
			    while(stag.hasMoreTokens()){  	
                 agent.addDubbler(stag.nextToken());
                 System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
			    }

				}
              agent.setName(name);
              agent.setIsPilot(true);
              agents.put(name, agent);
              agents1.put(agent.getID(), agent);
            }
            rs.close();
            stmt.close();
            
            Agent aa = new Agent ("LADU");
            aa.setName("LADU");
            agents.put("LADU", aa);
            
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        return agents;
    }

    public TreeMap getCustomers(Agent agent){
        connectionUp(url);
        TreeMap customers = new TreeMap();
        TreeMap custHash = new TreeMap();
        
        TreeMap over = new TreeMap();
        TreeMap used = new TreeMap();
        	
        try {

        	 String aaa = " SELECT  tt.saaja ,  SUM(aa.summa-aa.makstud) AS tasumata FROM osmy/my_tell tt " +
             "  LEFT JOIN osmy/my_arve aa ON  trim(str(aa.nr))=trim(str(tt.arvenr)) and vkuup < date() AND aa.kuup> date()-365 " +
             "  GROUP BY tt.saaja";
             
         	  Statement st = con.createStatement();
             ResultSet  rs = st.executeQuery(aaa);
        

             while (rs.next()) {
             	  String kood = enCode(tapitaht(rs.getString("saaja").trim()));
             	  
                 	  double dd = rs.getDouble("tasumata");
                 	  if(dd!=0){
                 		 over.put(kood, dd);
                 	  }  
             }
             
             String aaa2 = " SELECT  tt.saaja ,  SUM(aa.summa-aa.makstud) AS tasumata FROM osmy/my_tell tt " +
             "  LEFT JOIN osmy/my_arve aa ON  trim(str(aa.nr))=trim(str(tt.arvenr))  AND aa.kuup> date()-365 " +
             "  GROUP BY tt.saaja";
             
         	   st = con.createStatement();
               rs = st.executeQuery(aaa2);
        

             while (rs.next()) {
             	  String kood = enCode(tapitaht(rs.getString("saaja").trim()));
             	  Customer cc = (Customer)custHash.get(kood);
             	  double dd = rs.getDouble("tasumata");
             		  if(dd != 0){
                 		used.put(kood, dd); 
                  }
             }
                         
             
        	
        	
        	String  ss = "select count (kood) as num, kood , min (alla) as ale FROM osmy/allahind a group by kood";
        
        Statement st2 = con.createStatement();
        ResultSet	      rs2 = st2.executeQuery(ss);
      TreeMap aled = new TreeMap();
      

        while (rs2.next()) {
            int num = rs2.getInt("num");
            String kood = enCode(tapitaht(rs2.getString("kood").trim()));
            String ale = rs2.getString("ale").trim();	
            if(num>4){
            	aled.put(kood, ale);
            }
        }
        
        String cmd = "SELECT  f.kood, f.nimi, '' AS tasumata, (trim(aadr1)+' '+trim(aadr2)) " +
        		" AS aadress, '0' AS ale , '' AS visit,krepiir as limit,   " +
	   " trim(f.juht)+ ' ' + trim(f.kontakt) + ' ' + trim(f.telefon) AS info , agent ,hgrupp as hinnak ,tt.firma AS payer"
	   + " FROM osmy/firma f 	LEFT JOIN osmy/my_tell tt ON f.kood=tt.saaja  " +
     "   WHERE   (f.agent)!=0  GROUP BY kood";
        
        log(cmd);

     

       
        	 st = con.createStatement();
         	      rs = st.executeQuery(cmd);
       

            while (rs.next()) {
              String name = enCode(tapitaht(rs.getString("nimi").trim()));
              String accountnum = enCode(tapitaht(rs.getString("kood").trim()));
              Customer cust = new Customer(accountnum);
              name = name+ " " + accountnum;
              cust.setName(name);
            
              cust.setAddress(enCode(tapitaht(rs.getString("aadress").trim())));

             
              String limit = rs.getString("limit");
              if (limit != null && limit.trim().length()>2){
                  cust.setCredit(limit.trim());
              }else{
            	  cust.setCredit("10000");
              }
              
              String info = rs.getString("info");
              if (info != null && info.trim().length()>0){
                cust.setPersonalData(info.trim().replaceAll("\\r\\n|\\r|\\n", " "));
              }
              String agen = rs.getString("agent");
           if(agen != null && agen.trim().length()>0){
            cust.addAgent(new Agent(agen.trim()));
 				Agent agg = ((Agent)this.getAgents1().get(agen.trim()));
 				
 				if(agg != null && agg.getDubbler().size()>0){
 					System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
 					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
 						cust.addAgent(new Agent((String)agg.getDubbler().elementAt(i)));
 						System.out.println(cust.getName() + cust.getAgents());
 					}
 				}
 			   cust.addAgent(new Agent("13"));//müügijuhile kõik kliendid
 	           cust.addAgent(new Agent("8"));
 				
           }
           
           if(cust.getID().equals("smarte")){
        	          Enumeration enums = getParent().getAgentsHash().elements();
        	          while (enums.hasMoreElements()) {
        	            Agent a = (Agent) enums.nextElement();
        	            cust.addAgent(a);
        	          }
        	}
           
           
           if(aled.get(accountnum)!= null){
        	   cust.setDiscount(new Double((String)aled.get(accountnum)).doubleValue());
           }
           
           if(over.get(accountnum) != null){
        	   cust.setDebt(Double.parseDouble(over.get(accountnum) + ""));
           }
           
           if(used.get(accountnum) != null){
        	   cust.setUsedCredit(Double.parseDouble(used.get(accountnum) + ""));
           }
        
           
           String hinnak = rs.getString("hinnak".trim());
           if (hinnak != null && !hinnak.equals("0")){
             cust.setCustType(hinnak);
           }
           
           
           String payer = rs.getString("payer".trim());
           if (payer != null && payer.trim().length() > 0){
             cust.setPayer(payer.trim());
           }
           
              customers.put(name , cust);
              custHash.put(accountnum, cust);
       
            }
            
           
            
            Customer cc = new Customer ("LADU");
            cc.setName("LADU");
            cc.addAgent(new Agent("LADU"));
            customers.put("LADU", cc);
            
        } catch (Exception e) {
            getParent().showError(e);
        }

        return customers;
    }

    public TreeMap getAgents1() {
		return agents1;
	}

    private TreeMap agents1 = new TreeMap();
    
	public  void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
    
    
    public TreeMap getProducts(Agent agent){
        connectionUp(url);
  
        
        Vector sellers = new Vector();
        
        // String cmd = "SELECT toode, nimetus, yhik, myygihind FROM toode WHERE (len(trim(toode)) > 3) AND (trim(toode.grupp) = 'L')";LEFT(k.nimi, 4) + ' ' +
        String cmd = "SELECT a.siffer AS toode,  a.nimi AS nimetus,iif(a.karp >0,a.karp,a.kast) AS tykke,a.kassak as ean,a.uhik as unit, " +
     "   a.mghind AS myygihind, (kak+tulek-minek) AS kogus  ,  a.grupp  ,str(a.grupp) + (a.allgrupp)  as grupp2 " +
	 " , oshind FROM osmy/lm_mat a LEFT JOIN  " +
     "   osmy/lm_kla k ON a.grupp = k.klkood  " +
     "   LEFT JOIN ladu/ll010000 ll ON ll.siffer=a.siffer " +
     "   WHERE  (  tkood <> 'F' and  a.mghind  != 0 )   ORDER by a.nimi";

        if (isDebug()) {
            System.out.println(cmd);
        }

        TreeMap products = new TreeMap();

        try {
        	
        	
        	
        	
        	
        	
        	   String  sql2 =  "SELECT pr.siffer AS toode FROM osmy/my_toode pr   " +
           " INNER JOIN osmy/my_tell te ON te.nr=pr.recnr INNER JOIN osmy/lm_mat a ON a.siffer =pr.siffer  WHERE  " +
           " regaeg > date()-200 and pr.siffer!=''  GROUP BY pr.siffer ";

          if (isDebug()) System.out.println(sql2);
       
             Statement stm1 = con.createStatement();
             ResultSet rst1 = stm1.executeQuery(sql2);

               while (rst1.next()) {
                 String toode = enCode(tapitaht(rst1.getString("toode").trim()));    
                     sellers.add(toode);	
                }    
        	
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
              String kood = enCode(tapitaht(rs.getString("toode").trim()));
              Product prod = new Product(kood);
              String nimetus = enCode(tapitaht(rs.getString("nimetus").trim()));
              prod.setName(nimetus);
              prod.setInCases(rs.getDouble("tykke"));
              
              String ean = rs.getString("ean");
              if(ean != null && ean.trim().length() >0){
              	prod.setEAN(ean.trim());
              }
              

              String unit = rs.getString("unit");
              if(unit != null && unit.trim().length() >0){
              	prod.setUnit(unit.trim());
              }
              
              prod.setPrice(rs.getDouble("myygihind"));
              String laoseis = rs.getString("kogus");
              if (laoseis != null){
                prod.setStock(Double.parseDouble(laoseis));
              }
           
              
              String grupp = rs.getString("grupp");
              if(grupp != null && grupp.trim().length() >0){
              	prod.setGroup(grupp.trim());
              	 prod.setDiscgrupp(grupp.trim());
              }
              prod.setVat("20");
              String grupp2 = rs.getString("grupp2");
              if(grupp2 != null && grupp2.trim().length() >0){
            	  //prod.setDiscgrupp(grupp2.trim());
              }
              
            
              
              
              
              
              if( prod.getStock() > 0){ //sellers.contains(kood) ||
              	
            	  /*   if(prod.getEAN().trim().length()==0){
                     	 dateOut.put(kood.trim(),kood.trim() + "|" + deCode (prod.getUnit() + " "+nimetus.trim()) + "|" + prod.getStock());
                     	 buf.append(kood.trim() + ";"+ kood.trim() + "|" + deCode (prod.getUnit() + " "+nimetus.trim()) + "|" + prod.getStock() + "\n");
                     }else{
                     	 dateOut.put(prod.getEAN(),kood.trim() + "|" + deCode (prod.getUnit() + " "+nimetus.trim()) + "|" + prod.getStock());
                     	 buf.append(prod.getEAN() + ";"+ kood.trim() + "|" + deCode (prod.getUnit() + " "+nimetus.trim()) + "|" + prod.getStock() + "\n");
                     	 if(prod.getEanCase().trim().length() >0){
                     		 dateOut.put(prod.getEanCase(),kood.trim() + "|" + deCode (prod.getUnit() + " " +nimetus.trim()) + "|" + prod.getStock());
                         	 buf.append(prod.getEanCase() + ";"+ kood.trim() + "|" + deCode (prod.getUnit() + " "+nimetus.trim()) + "|" + prod.getStock() + "\n");	 
                     	 }
                     }
                    */  
             prod.setSorting(prod.getID());             
              products.put(nimetus, prod);

              }

                // }
            }
          /*  try {
    			saveObject(dateOut,"tootedRev.has");
    			  if(buf.length() > 2){
    	                File f = new File( "tootedRev.txt");
    	                if (f.exists())f.delete();
    	                  try {
    	            		PrintWriter os = new PrintWriter(new FileWriter(
    	            				 "tootedRev.txt", true));
    	            		os.write(buf.toString());
    	            		os.close();
    	            	  }catch (IOException ex) {
    	            		ex.printStackTrace();
    	            	  }
    	             }
    		} catch (IOException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}*/
            
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
            connectionUp(url);
            String cmd =
                "SELECT te.saaja  AS klient,pr.siffer AS toode,pr.kogus AS kogus,te.regaeg AS kuupaev , reasum/pr.kogus as lastp FROM osmy/my_toode pr " +
                "INNER JOIN osmy/my_tell te ON te.nr=pr.recnr INNER JOIN osmy/lm_mat a ON a.siffer =pr.siffer  WHERE " +
                "regaeg > ? " +
                "GROUP BY te.saaja,pr.siffer ";
         
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
                 String kliendikood = enCode(tapitaht(rs.getString("klient").trim()));
                 String tootekood = enCode(tapitaht(rs.getString("toode").trim()));
                 double kogus = Double.parseDouble(rs.getString("kogus").trim());
                 String kuup = rs.getString("kuupaev");
                 double lastp = Double.parseDouble(rs.getString("lastp").trim());
                 Customer cust = (Customer) kliendid.get(kliendikood);

                 if (cust == null) {
                   cust = new Customer(kliendikood);
                 }

                 Product prod = new Product(tootekood);
                 
                 
                 cust.addProduct(prod,
                                 new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                              kuup.substring(5,kuup.length()), lastp, 0,0," "),false);
                 kliendid.put(kliendikood, cust);
                 custProds.add(new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                              kuup.substring(5,kuup.length()), lastp, 0,0," "));
             	
                 
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
  	 	   	
  	 	   	
  	 	    String cmd = "  SELECT sum(pr.reasum) AS sales , agent  " +
                     " FROM osmy/my_toode pr INNER JOIN osmy/my_tell te ON trim(str(te.nr))= trim(str(pr.recnr)) " +
					 " INNER JOIN osmy/firma f on f.kood = te.saaja " +
					 "  WHERE  month(regaeg)=month(date()) AND year(regaeg)=year(date()) and agent=" + agent.getID()+ "  GROUP BY agent";

  		       
  	 	 String sql2 =  " SELECT   d.saaja AS klient ,d.arvenr AS arve, regaeg AS kuupaev, k.nimi AS kliendinimi,d.summa AS arve_summa " +
      "   FROM osmy/my_tell d  INNER  JOIN osmy/firma k ON k.kood = d.saaja " +
      "   WHERE   d.regaeg >= date() and agent = " + agent.getID()+ " ORDER BY regaeg DESC " ;
  	        
  	   
  	        
  	        System.out.println("siin");
  	          String header = " Tänased tehingud :\n\n";
  	          String header1 = " Jooksva kuu müük : ";
  	          boolean first = true;
  	          nf.setMinimumFractionDigits(2);
  	            try {
  	            	
  	            	
  	            	
  	            	
  	            connectionUp(url);
  		              Statement stmt = con.createStatement();
  		              ResultSet	      rs = stmt.executeQuery(cmd);
  		              if(rs.next()) {
  		                sb.append(header1 + nf.format(rs.getDouble("sales"))+  " EUR\n\n");
  		                
  		              }
  		              rs.close();
  		              stmt.close();
  		              con.close();
  		         
  	            	
  	            	
  	            	
  	            	
  	            	
  		            Connection con3 = this.getNewConnection();
  	                     Statement st = con3.createStatement();
  	                    ResultSet rs2 = st.executeQuery(sql2);
  	                        while (rs2.next()) {
  	                        	if(first){
  	                        		sb.append(header);
  	                        		first = false;
  	                        	}
  	                        	String nimi = rs2.getString("kliendinimi").trim() ;
  	                        
  	                    
  	                      
  	                        sb.append(nimi  +
  	                        		"\n    (" + rs2.getString("arve").trim() + ")[" + rs2.getString("kuupaev").substring(0,10) + "] " + 
  	                        		getParent().getNF().format(rs2.getDouble("arve_summa"))  
  	                        	+" EUR-i "	+  "\n\n");
  	                        }

  	                        
  	                        
  	                        } catch (Exception e) {
  	                            getParent().showError(e);
  	                        }
  	                        System.out.println(agent.getID() + sb.toString());
  	           if(sb.toString().trim().length()>0){
  	           	
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
    public double getSalary(String agentCode) {
      connectionUp(url);
        double sales = 0;
        String cmd = "SELECT SUM(summa) AS salary " +
                     "FROM osmy/my_tell te  WHERE te.osmy=? " +
                     "AND month(regaeg)=month(date()) AND year(regaeg)=year(date())";
        log(cmd);

        try {
            stmt = con.prepareStatement(cmd);
            stmt.setInt(1, Integer.parseInt(agentCode));
            ResultSet	      rs = stmt.executeQuery();

            if(rs.next()) {
              sales = rs.getDouble("salary")/1.20;
            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        setSales(sales);
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
    public static final String DRIVER_VERSION = "RVEktoni";
    private String             url = "jdbc:odbc:Divinum";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
    private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

    
private TreeMap tooteHind = null;
	
	private void  getHinnad(Vector lines){
		
		
		String sql = "select siffer as toode , i1+i2+i3  as asi from osmy/hinnad where tunnus = 'M' and siffer not like '%*%' order by siffer";
		TreeMap res = new TreeMap();
		TreeMap vahe = new TreeMap();
	        try{
	            stmt = con.prepareStatement(sql);
	            ResultSet	      rs = stmt.executeQuery();
	       
	            while (rs.next()) {
	            	  String kood = rs.getString("toode").trim();
	                  String nimetus = rs.getString("asi").trim();
	                if(vahe.get(kood) != null){
	                	String aa = vahe.get(kood) + "";
	                	vahe.put(kood, aa +"|"+ nimetus);
	                }else{
	                	vahe.put(kood, nimetus);
	                }
	            }
	            
	            rs.close();
	            stmt.close();     	
	        }catch(Exception e){
	        	e.printStackTrace();
	        }
	   
     
	    Iterator itera =    vahe.keySet().iterator();
	    
	    while (itera.hasNext()){
	    	String toode = itera.next() + "";
	    	String nimetus = vahe.get(toode) + "";
	    	StringTokenizer st = new StringTokenizer(nimetus,"|");
	    	System.out.println("tood " + toode + " " + nimetus);
	    	int cou = 1;
            if (st.hasMoreTokens()){
          	  double aa = Double.parseDouble(st.nextToken());
          	  MatrixLine mline = new MatrixLine();
          		mline.setCliGroup(cou+ "");
				mline.setProdID(toode);
				if (aa != 0) {
					mline.setPrice(aa);
					//lines.add(mline);
				}
			  	cou++;
            }
        }
	    System.out.println("Lines size " + lines.size());
	}

	private void  getEriHinnad(Vector lines){

        String sql = "select siffer as toode , i1+i2+i3  as asi from osmy/hinnad where tunnus = 'T'   order by siffer";
      
        try{
            stmt = con.prepareStatement(sql);
            ResultSet	      rs = stmt.executeQuery();
       
            while (rs.next()) {
            	  String kood = rs.getString("toode").trim();
                  String nimetus = rs.getString("asi").trim();
                  StringTokenizer st = new StringTokenizer(nimetus,"|");
                  while (st.hasMoreTokens()){
                	  String aa = st.nextToken();
                	  if(aa.indexOf("000000") == -1 ){
                		  StringTokenizer ss = new StringTokenizer(aa,"Ų");
                		  while(ss.hasMoreTokens()){
                			  try{
                				  
                			  
                			  String saaja = ss.nextToken().trim();
                			  String kogus = ss.nextToken();
                			  String ale = ss.nextToken();
                			  String hind = ss.nextToken();
                			 
                			  MatrixLine mline = new MatrixLine();
                			  mline.setCustID(saaja);
                			  mline.setProdID(kood);
                			  try{
                				  mline.setPrice(Double.parseDouble(hind));
                			  }catch(Exception e ){
                			  }
                			
                			  try{
                				  double al = Double.parseDouble(ale);
                				  if(al > 0 ){
                					  mline.setPrice(Double.parseDouble(tooteHind.get(kood) + "") * (100-al)/100);
                				  }
                				//  mline.setDiscount(al);
                			  }catch(Exception e ){
                			  }
                			  
                			  
                			  if(mline.getPrice() > 0 || mline.getDiscount() > 0){
                				  lines.add(mline);
                				 // System.out.println("aa " + saaja + " " + kogus + " "+ ale+ " "+ hind);
                          		
                			  }
                			
                			  }catch(Exception e ){
                			  }
                			  
                		  }
                			  
                     
                	     }
                	    }
                	
            }
            rs.close();           
            stmt.close();     	
        }catch(Exception e){
        	e.printStackTrace();
        }
   
   
	    System.out.println("LinesEri size " + lines.size());
	}

    
    
    
    
	public Vector getPriceMatrixData() {
		Vector lines = new Vector();
		
         getHinnad(lines);
         getEriHinnad(lines);
         
         
         String sql = " SELECT '' AS custid,kood AS clgroup,'' AS prodid,str(grupp) AS alegroup,alla AS discount,0 " +
                      " AS price  FROM osmy/allahind  GROUP BY custid,grupp , allgrupp ORDER BY custid,grupp";

		Statement st = null;
		try {
			connectionUp();
			st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {

				String custID = rs.getString("custid");
				String clGroup = rs.getString("clgroup");
				String prodID = rs.getString("prodid");
				String discGroup = rs.getString("alegroup");
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
				if (mline.getProdID() == null && discGroup != null
						&& discGroup.trim().length() > 0) {
					mline.setProdGroup(discGroup);
				}
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


	  System.out.println("matrix lines " + lines.size());
	 return lines;
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
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

