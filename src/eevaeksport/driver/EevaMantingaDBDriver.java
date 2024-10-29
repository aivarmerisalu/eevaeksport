/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import eevaeksport.Agent;
import eevaeksport.Contract;
import eevaeksport.ContractLine;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class EevaMantingaDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,PictureMaker {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @seefsavest
     * 
     */
    public EevaMantingaDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");

        //Rotimaja9
        
        // String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

        try {
            Class.forName(databaseDriverName).newInstance();

        } catch (Exception e) {
            parent.showError(e);
        }
        user =  getParent().getProps().getProperty("rest.user");
	    pass = getParent().getProps().getProperty("rest.pass");
	    rcompanyx = getParent().getProps().getProperty("rest.companyx");
	    rcompany = getParent().getProps().getProperty("rest.company");
          }
	  
	  String user =  null;
	  String rcompany =  null;
	  String pass = null;
	  String rcompanyx = null;

	  
	  
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
		             String[] ff2 = res2.list();
		         	 for(int i = 0;ff2.length>i;i++){
		             	(new File ( pictspda + ff2[i])).delete(); 
		             }
//		         	 L�ppes kustutamine 
		  		 
		  		 TreeMap prods = this.getProducts(null);

		         StringBuffer buf = new StringBuffer();
		         Iterator enume = prods.keySet().iterator();
		    
		         while (enume.hasNext()) {
		    	    	String asi = (String)enume.next();
		    	    	Product prod = (Product)prods.get(asi);
		    	    	String toode = prod.getID().trim();  
		               if(toode.length() > 0){
		                	
		    	   	  res2 = new File (pictssou);
		              ff2 = res2.list();
		             
		         	 for(int i = 0;ff2.length>i;i++){
		             	if( ff2[i].startsWith(toode)){
		               // System.out.println("pilt olemas " + ff2[i]);
		            	File in = new File ( pictssou + ff2[i]); 
		              	if(in.isFile()){
		              	File inpda = new File ( pictspda + prod.getID()+ ".jpg");
		              	if(!inpda.exists()){
		                  	Converter.shrinkAndCopyFile(in,inpda,400);
	              	    if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
		                    		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
		                    	}
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
 	  
 String cmd3 = "SELECT k.maksja,d.klient AS klient, SUM(d.tasumata) AS debt  from firma1.dok d inner join firma1.klient k" +
 		" on k.kood = d.klient  WHERE  " + 
  " current_date>d.tasukuup and d.doktyyp='G' AND d.tasumata <> 0 AND length(klient) > 0 GROUP BY d.klient,k.maksja ORDER BY d.klient";


 		TreeMap custs = new TreeMap();




 		StringBuffer products = new StringBuffer();
 		try {
        this.connectionUp();
 		Statement st = con.createStatement();
 		ResultSet rs = st.executeQuery(cmd3);

 		while (rs.next()) {
 			String maks = rs.getString("maksja");
 		String kood = rs.getString("klient");
 		double volg = rs.getDouble("debt");
 		Customer custw = null;
 		if(maks != null){
 			 custw = new Customer (maks.trim());
 			custw.setDebt(volg);
 			if(volg>0){
 				if(volg > 499){
 					//custw.setSalesBlock(true);
 				}
 				
 				custs.put(maks.trim(),custw);
 			}
 		}
 		if(kood != null){
 			 custw = new Customer (kood.trim());
 			custw.setDebt(volg);
 			if(volg>0){
 				if(volg > 499){
 					//custw.setSalesBlock(true);
 				}
 				
 				custs.put(kood.trim(),custw);
 			}
 		}
 	
 		
 		}

 		rs.close();
 		st.close();
 		con.close();
 		
 		} catch (Exception e) {
 		getParent().showError(e);
 		}
 		 
        this.getCustomers(null);

        
        Iterator     codes = custs.values().iterator();
 		while (codes.hasNext()){
 		Customer cust = (Customer)codes.next();
 		String bl = "0";
 		if(cust.isSalesBlock()){
 			bl = "1";
 			}
 		
 		 products.append(cust.getID() + ";" + getParent().getNF().format(cust.getDebt()) + ";" + bl + "\n");
 		}
        
 		return products.toString();

       }
    
    
    
    
    public Vector getPriceMatrixData()  {
        Vector lines = new Vector();
        String    sql = " SELECT mt.klient AS custid,mt.klliik AS clgroup,toode AS prodid,mt.artliik AS prodgroup,0 AS discount,mt.jaehind " + 
       "   AS price , algus , lopp FROM firma1.hinnamtr mt inner join firma1.hkpais p on p.hinnak = mt.hinnak where " +
       "  algus <= current_date and current_date<=lopp  and mt.klient != '-' order by mt.klient,mt.klliik,mt.toode";
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
              double discount = rs.getDouble("discount");
              double price = rs.getDouble("price");
              String algus = rs.getString("algus");
              String lopp = rs.getString("lopp");
              
              MatrixLine mline = new MatrixLine();

              if (lopp != null) {
					mline.setEndDate(lopp.substring(0, 10));
				}
				if (algus != null) {
					mline.setStartDate(algus.substring(0, 10));
				}
              if (custID != null && custID.trim().length() > 0) {
              //  mline.setCustID(custID.trim());
                mline.setCliGroup(custID.trim());
              }
              if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 1) {
              //  mline.setCliGroup(clGroup.trim());
              }

              if (prodID != null && prodID.trim().length() > 0) {
                mline.setProdID(prodID.trim());
              }
              if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                mline.setProdGroup(prodGroup.trim());
               // price = 0;
              }
               // mline.setDiscount(discount);

              if (price != 0) {
                mline.setPrice(price);
              }
              if(price>0 )
             lines.add(mline);
            }

            rs.close();
            st.close();
            this.connectionDown();
          }
          catch (SQLException ex) {
          }
          
          System.out.println("lines " + lines);
          
        return lines;
      }
    
    

    
       
    public String getAssortmentStr(Agent agent){
    	return null;
    }
    public String getSaveStockInfo(){
    	
    	  StringBuffer products = new StringBuffer();

         String sql = "select s.kogus, s.toode  from  " +
                " firma1.hetkesei s where s.osak ='NORD' ";
         if (isDebug()) {
             System.out.println(sql);
         }

         try {

        	 Connection con = DriverManager.getConnection(url,"postgres","Eetamant2");
        	 PreparedStatement pstm = con.prepareStatement(sql);
        	 

          ResultSet rs = pstm.executeQuery();
          
          while (rs.next()) {
        	  double seis = rs.getDouble("kogus");
        	  String  toode =  rs.getString("toode").trim();
        	  if(seis > 0){
        		  products.append(toode + ";");
           		  products.append(" ;");
           		  products.append(getParent().getNF().format(seis) + "; "
           				+ "\n");

                 // System.out.println("IA laoseisud " + seis + " " + toode);
        	  }
        	
          }
          rs.close();
          pstm.close();
           con.close();
            } catch (Exception e) {
                getParent().showError(e);
            }
            
         //  System.out.println("K�ik laoseisud tehtud " + products.toString());
            
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
    public void connectionUp(){
      if (con == null){
        try {
           con = DriverManager.getConnection(url,"postgres","Eetamant2");
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
       // getFixedAssortmentData();
        String  cmd = "SELECT yksus,nimetus FROM firma1.yksus2 ";
        TreeMap agents = new TreeMap();

        try {
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
            
              String name = rs.getString("nimetus");
              if(name != null) {
            	     agent.setName(name.trim());
                     agents.put(name.trim(), agent);  
              }
         
            }
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        return agents;
    }
private Hashtable custCodes = null;
    
    public TreeMap getCustomers(Agent agent) {
        
    	
    	
        custCodes = new Hashtable();
        // String cmd = "SELECT k.kood, k.nimi FROM (klient k INNER JOIN omrid ON k.kood = omrid.klient INNER JOIN rid ON rid.dokumnr = omrid.tasudjrk) WHERE omrid.rid2obj='" + agentCode + "' AND (len(rid.toode) > 2) GROUP BY k.kood";
        String cmd =
            " SELECT k.kood,k.maksja, k.nimi,aadress,k.hinnaale AS ale, k.objekt2  as agent,k.klikaart as agent2 ,wwwpage as kkyl" +
            "  ,liik " +
          "  FROM firma1.klient k where  k.objekt2 != '' ORDER BY k.nimi";

        log(cmd);

     
        
       TreeMap customers = new TreeMap();
        HashMap custMap2 = new HashMap();
        try {
        	Connection   con = DriverManager.getConnection(url,"postgres","Eetamant2");
        	Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Customer cust = new Customer(rs.getString("kood").trim());
              String maks = rs.getString("maksja");
              if(maks != null && maks.trim().length() >0){
            	  cust.setPayer(maks.trim());
              }
            String nimi = Converter.enCodeEst(rs.getString("nimi").trim());
            cust.setName(Converter.enCodeEst(nimi));
            
            String addr = rs.getString("aadress");
            if(addr != null){
            	addr = addr.replaceAll("\\r\\n|\\r|\\n", " ");
            	 cust.setAddress(Converter.enCodeEst(Converter.adressLink(addr.trim(),"Estonia")));	
            }
           
        
            
            String abi2 = rs.getString("agent");
            if(abi2 != null && abi2.trim().length()>0){
            	cust.addAgent(new Agent(abi2.trim()));	
            }
            
            
            
            
            String liik = rs.getString("liik");
            if (liik != null && liik.trim().length()>0){
            	cust.setGroup(liik.trim());
            }
           
                customers.put(nimi, cust);
                custMap2.put(cust.getID(), cust);
            }
            

            String sql = "SELECT d.klient AS klient, SUM(d.tasumata) AS summa  from firma1.dok d  WHERE  " +
              "  current_date>d.tasukuup and d.doktyyp='G' AND d.tasumata <> 0 AND length(klient) > 0 GROUP BY d.klient ORDER BY d.klient";
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
            		//cc.setDebt(debt);   
            		if(debt > 499){
            			//cc.setSalesBlock(true);
            		}
            	}
            }
            	  
              
            }
            rst.close();
            stmt.close();
            
            
        } catch (Exception e) {
            getParent().showError(e);
        }

   
        
        return customers;
    }




		public String getDistribution() {
          return null;
        }

		
		  private java.util.Date getCalXML(String ss){
			  Calendar cc = Calendar.getInstance();
			  	if(ss != null && ss.length() == 6){
			  		cc.set(Calendar.YEAR,2000 + Integer.parseInt(ss.substring(0,2)));
				  	cc.set(Calendar.MONTH,Integer.parseInt(ss.substring(2,4))-1);
				  	cc.set(Calendar.DATE,Integer.parseInt(ss.substring(4,6)));
				}
			  	return cc.getTime() ;
		  }
		

    public TreeMap getProducts(Agent agent) {
        connectionUp();
        TreeMap products = new TreeMap();
        HashMap kiilis = new HashMap();
        HashMap altuses = new HashMap();
        HashMap partiid = new HashMap();
        HashMap omahinnad = new HashMap();
        HashMap kriitiline = new HashMap();


//System.out.println("Kiilis " + kiilis.size());
       
        
 
 String sql4 = " select toode , sum(kogus) as kokku  from firma1.hetkesei " + 
 " where  osak='NORD' group  by toode   " ;

try{
	Connection con = DriverManager.getConnection(url,"postgres","Eetamant2");
PreparedStatement pstm = con.prepareStatement(sql4);
ResultSet rs6 = pstm.executeQuery();

while(rs6.next()){
altuses.put(rs6.getString("toode").trim(),rs6.getDouble("kokku") );
}
rs6.close();
pstm.close();
con.close();

}catch (Exception e){
	e.printStackTrace();
}
 




 
             //  System.out.println("Omahinnad " + omahinnad.toString());
      

          String cmd = "SELECT toode, nimetus, tykke, myygihind, yhik ,hankija " +
          		",ribakood   FROM firma1.toode WHERE " +
              "  length(trim(toode)) >3 and length(trim(ribakood)) > 3 ORDER BY toode";
try{
	Connection con = DriverManager.getConnection(url,"postgres","Eetamant2");

	stmt = con.createStatement();
                    ResultSet  rs = stmt.executeQuery(cmd);

                      while (rs.next()) {
                          String kood = rs.getString("toode").trim();
                        
                          double kogus = -1;
                          if (altuses.get(kood) != null){
                            kogus = Double.parseDouble(altuses.get(kood)+"");
                          } 
                          String nimetus = rs.getString("nimetus").trim() + " " +  kood;
                          String yhik = rs.getString("yhik"); 
                          
                          Product prod = new Product(kood);
                          prod.setName(Converter.enCodeEst(nimetus));
                          prod.setPrice(rs.getDouble("myygihind"));
                          prod.setInCases(rs.getDouble("tykke"));
                          prod.setStock(kogus);
                          prod.setGroup(parent.make(rs.getString("hankija")));
                          prod.setEAN(parent.make(rs.getString("ribakood")).replaceAll(";", "*"));
                          if(yhik != null){
                        	prod.setUnit(yhik.trim());  
                          } 
                          if(kriitiline.get(kood) != null){
                        	 // System.out.println("Kriitiline " + kriitiline.get(kood) + " " + kood );
                          //	prod.setStatusNew("1") ; 
                          	prod.setStatusGiftPack("1");
                          	prod.setDiffColor(true);
                            }
                         
                          
                          
                         if(partiid.get(kood) != null){
                        	prod.setPartii(partiid.get(kood) + "") ; 
                          }
                          
                         if(omahinnad.get(kood) != null){
                        	 double oh = Double.parseDouble(omahinnad.get(kood)+"");
                        	 double kate = (1-oh/prod.getPrice())*100;
                         	if(kate>0){
                         		String kat = (kate + "");
                                prod.setMargin(kat.substring(0,kat.indexOf(".")));
                         	}
                         	prod.setMinPrice(oh*1.2);
                           }
                         
                        
            
                         
                         
                          products.put(nimetus, prod);

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
    public TreeMap getProdGroups() throws SQLException {
    	   this.connectionUp();
    	     TreeMap   tm = new TreeMap();
    	     String    sql = "SELECT k.kood, k.nimi FROM firma1.toode t inner join firma1.klient k on k.kood =t.hankija WHERE " +
            " t.myygikood = 'K' AND length(trim(t.toode)) >3 group by k.nimi,k.kood ORDER BY k.kood  ";
    	     
    	     ResultSet rs = stmt.executeQuery(sql);

    	     while (rs.next()) {
    	       String liik=rs.getString("kood").trim();
    	       String nimetus= " " + Converter.enCodeEst(rs.getString("nimi").trim());
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
	String cmd =
	    "SELECT dok.saaja,rid.toode,dok.kuupaev,max(rid.kogus) as kogus,max(rid.hind)  as hind  FROM firma1.dok INNER " +
	  " JOIN firma1.rid ON dok.dokumnr = rid.dokumnr INNER JOIN firma1.toode on rid.toode = toode.toode INNER JOIN firma1.klient k on k.kood= dok.saaja WHERE " +
	   "  length(trim(dok.klient)) > 1 AND length(trim(rid.toode)) > 3 " +
	   "  AND dok.kuupaev >= current_date-130 " +
	   "  AND  dok.doktyyp = 'G'  GROUP BY dok.saaja, rid.toode , dok.kuupaev ORDER BY dok.saaja, rid.toode,dok.kuupaev ";
    
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
        double hind = rs.getDouble("hind");
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
      String sql = "SELECT klient,saaja,tasudok AS number,tasukuup AS maksepaev,tasumata, kuupaev FROM " +
                   "firma1.dok WHERE  tasukuup<current_date+5 AND doktyyp='G' AND tasumata<>0 " +
                   "ORDER BY klient";

      connectionUp();
      PreparedStatement st = con.prepareStatement(sql);
     // st.setString(1, agentCode);
      ResultSet rs = st.executeQuery();
      while(rs.next()){
        Debt debt = new Debt();
        String saaja = rs.getString("saaja");
        if(saaja != null){
        	 debt.setCustID(saaja.trim());
             debt.setDoknr(rs.getString("number").trim());
             debt.setDuedate(rs.getDate("maksepaev"));

             debt.setSum(rs.getDouble("tasumata"));
             debt.setDate(rs.getDate("kuupaev"));
             list.addElement(debt);
        }
       
      }
      st.close();
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
        connectionUp();
        double  salary = 0;

        try {

/*	String  sql = "SELECT ajut,tasu,plaan FROM yksus WHERE yksus = '" + agentCode
                      + "'";

        if (debug) {
            System.out.println(sql);
        }

            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                ajut = rs.getBoolean("ajut");
                koef = rs.getDouble("tasu");
                setBudget(rs.getDouble("plaan"));
            }

            rs.close();
*/
           /* sql = "select sum(doksumma) as eelmineaasta  from dok where (month(kuupaev))=(month(date())) and doktyyp='G' and year(date())-1=year(kuupaev)";
            //+ "< (select sum(doksumma) as seeaasta  from dok where (month(kuupaev))=(month(date())) and doktyyp='G' and year(date())=year(kuupaev))";
          if (debug) {
              System.out.println(sql);
          }

            double eelmineaasta = 0;
            double seeaasta = 0;

            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                eelmineaasta = rs.getDouble("eelmineaasta");
            }
            rs.close();


            sql = "select sum(doksumma) as seeaasta  from dok where (month(kuupaev))=(month(date())) and doktyyp='G' and year(date())=year(kuupaev)";
            if (debug) {
                System.out.println(sql);
            }

            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                seeaasta = rs.getDouble("seeaasta");
            }
            rs.close();

            if (eelmineaasta < seeaasta){
              bonus = 3000;
            }

*/	    // k�sioleva m��gimehe selle kuu m��gi leidmine
            String sql =
                "select sum(doksumma)/1.20 as seekuu  from firma1.dok where (month(kuupaev))=(month(current_date)) and doktyyp='G' AND year(current_date)=year(kuupaev) AND pais2obj=?";
            if (isDebug()) {
                System.out.println(sql);
            }

            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, agentCode);

            ResultSet rs = pstmt.executeQuery();

            double seekuu = 0;

            if (rs.next()) {
              seekuu = rs.getDouble("seekuu");
              setSales(seekuu);
            }
            rs.close();
            pstmt.close();
            String sql2 =
                "select sum(doksumma)/1.20 as seekuu  from firma1.dok where (month(kuupaev))=(month(current_date)) and yksus='3' and doktyyp='G'"
                + " AND year(current_date)=year(kuupaev) AND pais2obj=?";
            if (isDebug()) {
              System.out.println(sql2);
            }

            PreparedStatement pstmt2 = con.prepareStatement(sql2);
            pstmt2.setString(1, agentCode);

            ResultSet rs2 = pstmt2.executeQuery();

            double seekuuladu3 = 0;

            if (rs2.next()) {
              seekuuladu3 = rs2.getDouble("seekuu");
              setSales3(seekuuladu3);
            }
            rs2.close();
            pstmt2.close();

            String sql1 =
                "select sum(doksumma)/1.20 as seekuu  from firma1.dok where (month(kuupaev))=(month(current_date)) and yksus='4' and doktyyp='G' AND year(current_date)=year(kuupaev) AND pais2obj=?";
            if (isDebug()) {
              System.out.println(sql1);
            }

            PreparedStatement pstmt1 = con.prepareStatement(sql1);
            pstmt1.setString(1, agentCode);

            ResultSet rs1 = pstmt1.executeQuery();

            double seekuuladu2 = 0;

            if (rs1.next()) {
              seekuuladu2 = rs1.getDouble("seekuu");
              setSales4(seekuuladu2);
            }
            rs1.close();
            pstmt1.close();


        } catch (Exception e) {
            getParent().showError(e);
        }

        salary = Math.round(salary * 100d) / 100d;

        return salary;
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
    public static final String DRIVER_VERSION = "EevaMantinga";
    private String	       url = "jdbc:odbc:eeva";
    private Connection	       con = null;
    private Statement	       stmt;
    private String	       subjectFilesTo = null;
	@Override
	public void setBulkSales(Map agents) {
		  connectionUp();
    		String cmd =  "  select sum(doksumma)/1.22 as sales , dok.pais2obj  AS agent   from firma1.dok where (month(kuupaev))=(month(current_date)) and doktyyp='G' AND year(current_date)=year(kuupaev) " +
			  "  GROUP BY dok.pais2obj" ;
    	
    		log(cmd);
    		
    		try {
    			
    			Statement stmt = con.createStatement();		
    		
    			
    			ResultSet	      rs = stmt.executeQuery(cmd);
    			while(rs.next()) {
    				String agentID = rs.getString("agent");
    				if(agentID != null){
    					Agent agent = (Agent)agents.get(agentID.trim());
        				if (agent != null){
        					agent.setMonthSales((int)rs.getDouble("sales"));
        				}
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

	@Override
	public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		return result;
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
