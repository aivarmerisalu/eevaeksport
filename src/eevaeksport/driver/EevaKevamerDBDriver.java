
package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
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
public class EevaKevamerDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,PictureMaker {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @seefsavest
     * 
     */
    public EevaKevamerDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");

        //Rotimaja9
        
        // String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

        try {
         //   Class.forName(databaseDriverName).newInstance();

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
			 loadPictures();
			makePictureCat();
			 System.exit(0);
			
		}
	  
		 private void loadPictures() {
			 	
			 
			 this.getProducts(null);
			 String picts = parent.getProps().getProperty("pict.dir");
			 if(prods.size() > 0) {
				 connectionUp();
			Iterator itera = 	 prods.iterator();
			while (itera.hasNext()) {
				String prodid = (String)itera.next();
			
	        	  String filename = picts + prodid.replace("/", "_") + ".jpg"; // Function for generating temporary filenames basing on GUID 
                  File fp = new File(filename);
                  if(!fp.exists()) {
                
	        	try {
	 	           
		        	String  cmd2 = " SELECT pi.pilt ,  pildityyp FROM firma34.artpilt pi where pi.toode='" + prodid + "'"  ;
	        		    Statement st = con.createStatement();
	         		    ResultSet	      rs = st.executeQuery(cmd2);
	        	        while (rs.next()) {
	                      byte[] bufbyte = rs.getBytes("pilt");
	                      String laiend = rs.getString("pildityyp").trim().toLowerCase();
	                      if(bufbyte != null){
	                    	  System.out.println(cmd2);
	                         	  FileOutputStream fos = new FileOutputStream(filename); 
	                        		    fos.write(bufbyte);
	                        		    fos.close();
	                          }
	                      }
	                      
	        	    rs.close();
	        	    st.close();
	        	  
	        	}catch(Exception e) {
	        		System.out.println(filename);
	        		e.printStackTrace();
	        	}
	      	      }
	           }
			 }
			 	connectionDown();
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
		             	//(new File ( pictspda + ff2[i])).delete(); 
		             }
//		         	 L�ppes kustutamine 
		  		 
		  		 TreeMap prods = this.getProducts(null);
		  		 
		         StringBuffer buf = new StringBuffer();
		         Iterator enume = prods.keySet().iterator();
		    
		         while (enume.hasNext()) {
		    	    	String asi = (String)enume.next();
		    	    	Product prod = (Product)prods.get(asi);
		    	    	String toode = prod.getID().trim().replace("/", "_");  
		               if(toode.length() > 0){
		                	
		    	   	  res2 = new File (pictssou);
		              ff2 = res2.list();
		             
		         	 for(int i = 0;ff2.length>i;i++){
		             	if( ff2[i].startsWith(toode)){
		               // System.out.println("pilt olemas " + ff2[i]);
		            	File in = new File ( pictssou + ff2[i]); 
		              	if(in.isFile()){
		              	File inpda = new File ( pictspda + toode+ ".jpg");
		              	if(!inpda.exists()){
		                  	Converter.shrinkAndCopyFile(in,inpda,400);
	              	     }
		              	 if(buf.toString().indexOf(toode+".jpg;"  + inpda.length()) == -1){
	                    		buf.append(toode+".jpg;"  + inpda.length() + "\n");
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
 	  
 String cmd3 = "SELECT k.maksja,d.klient AS klient, SUM(d.tasumata) AS debt  from firma34.dok d inner join firma34.klient k" +
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
       "   AS price , algus , lopp FROM firma34.hinnamtr mt inner join firma34.hkpais p on p.hinnak = mt.hinnak where " +
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
                " firma34.hetkesei s where s.osak ='1' ";
         if (isDebug()) {
             System.out.println(sql);
         }

         try {

        	 Connection con = DriverManager.getConnection(url,"odbcmerisa","pMerisiil49");
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
           con = DriverManager.getConnection(url,"odbcmerisa","pMerisiil49");
           stmt = con.createStatement();
        } catch (Exception e) {
            getParent().showError(e);
            System.exit(0);
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
        
       
        String  cmd = "SELECT yksus,nimetus FROM firma34.yksus2 ";
        TreeMap agents = new TreeMap();

        try {
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
            
              String name = rs.getString("nimetus");
              if(name != null) {
            	     agent.setName(name.trim());
                     agents.put(name.trim(), agent);  
                  //   System.out.println(name);
              }
         
            }
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        return agents;
    }
    
    
    private TreeMap  makePersonas() {
        connectionUp();
      StringBuffer sb2 = new StringBuffer();
      TreeMap res = new TreeMap();
      
        String  cmd = "SELECT ko.nimi, k.kood,ko.telefon,ko.email,ko.timestamp FROM firma34.kaardika ko\r\n" + 
        		"        		        		 inner join firma34.klient k on k.maksja=ko.klient\r\n" + 
        		"								 union\r\n" + 
        		"SELECT ko.nimi, k.kood,ko.telefon,ko.email,ko.timestamp FROM firma34.kaardika ko\r\n" + 
        		"        		        		 inner join firma34.klient k on k.kood=ko.klient ";
        TreeMap agents = new TreeMap();

        try {
            ResultSet	      rs = stmt.executeQuery(cmd);
            while (rs.next()) {
             
              String name =  parent.make(rs.getString("nimi"));
              String klient = parent.make(rs.getString("kood"));
              String phone = parent.make(rs.getString("telefon"));
              String mail =  parent.make(rs.getString("email"));
              String tst =  parent.make(rs.getString("timestamp"));
              if(name != null && name .trim().length() > 0 && klient.trim().length() >0 ) {
            	   sb2.append(klient + ";" + name + ";" + " ; ;" + phone  + ";" + mail + ";" + tst + "\n");
              }
              
              
              String aa = "";
              if(name.trim().length() > 1) {
            	  aa =  " |"  + name ;
              }
              if(phone.trim().length() > 1) {
            	  aa = aa  + " |" + phone ;
              }
              if(mail.trim().length() > 1) {
            	  aa = aa  + " |" + mail  ;
              }
             
              
              if(res.get(klient) != null) {
            	  aa = res.get(klient)+ "|" + aa  ;
              }
              res.put(klient, aa);
            }
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        if(sb2.length() > 2){
   		 Converter.saveFileInUTF8( "custcontacts.txt", sb2);
   	    }
        return res;
    }
    
    
    
    private TreeMap payers = new TreeMap();
    public TreeMap getCustomers(Agent agent) {
        
      String cmd =
            " SELECT k.kood,k.maksja, k.nimi,aadress,tanav , piirkond , maksetin, regnr,k.hinnaale AS ale, k.objekt2  as agent,k.klikaart as agent2 ,wwwpage as kkyl" +
            "  ,liik, kontaktisi,telefon,email" +
          "  FROM firma34.klient k where  k.objekt2 != '' or k.nimi like '%UUS KLI%' ORDER BY k.nimi";

        log(cmd);

     TreeMap cont = makePersonas();
        
       TreeMap customers = new TreeMap();
        HashMap custMap2 = new HashMap();
        try {
        	Connection   con = DriverManager.getConnection(url,"odbcmerisa","pMerisiil49");
        	
        	Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Customer cust = new Customer(rs.getString("kood").trim());
              String maks = rs.getString("maksja");
              if(maks != null && maks.trim().length() >0){
            	  cust.setPayer(maks.trim());
            	  Vector vec = (Vector)payers.get(cust.getID());
            	  if(vec == null) {
            		  vec = new Vector();
            	  }
            	  vec.add(cust.getID());
            	  payers.put(maks.trim(), vec);
              }
            String nimi = Converter.enCodeEst(rs.getString("nimi").trim());
            cust.setName(Converter.enCodeEst(nimi));
            
            String addr = rs.getString("aadress");
            if(addr != null){
            	addr = addr.replaceAll("\\r\\n|\\r|\\n", " ");
            	cust.setCity("Tarne:" + addr);
            }
            String tanav = rs.getString("tanav");
            String piirk = rs.getString("piirkond");
           
            if(tanav != null){
            	tanav = tanav.trim();
            	if(piirk != null){
            		tanav = tanav + " " + piirk.trim();
            	}
            	 cust.setAddress(Converter.enCodeEst(Converter.adressLink(tanav,"Estonia")));	
            }
        
            
            String ting = rs.getString("piirkond");
            if(ting != null){
            	try {
            		cust.setPaymTerm(Integer.parseInt(ting.trim()) +"");
            	}catch(Exception e) {
            		
            	}
            	if(ting.trim().equals("SR")) {
            		//cust.setPaymTerm("-2");
            		cust.setSalesBlockPrepay(true);
            	}
            }
            
            
            String abi2 = rs.getString("agent");
            if(abi2 != null && abi2.trim().length()>0){
            	cust.addAgent(new Agent(abi2.trim()));	
            }
            
            
            if(cust.getID().equals("2334")) {
            	cust.addAgent(new Agent("1"));
            	cust.addAgent(new Agent("9"));
            	cust.addAgent(new Agent("6"));
            	cust.addAgent(new Agent("4"));
            	cust.addAgent(new Agent("3"));
            	cust.addAgent(new Agent("7"));
            	cust.addAgent(new Agent("10"));
            }
            
            
            
            String liik = rs.getString("liik");
            if (liik != null && liik.trim().length()>0){
            	cust.setGroup(liik.trim());
            }
            
            String uuu = "";
            String isi = rs.getString("kontaktisi");
            String tel = rs.getString("telefon");
            String mail = rs.getString("email");
            
            if(isi != null){
            	uuu = uuu + isi.trim()+ "|";
            }
            if(tel != null){
            	uuu = uuu + "Tel:"  + tel.trim()+ "|";
            }
            if(mail != null){
            	uuu = uuu + mail.trim();
            }
            
            if(uuu.length() > 1) {
            	cust.setPersonalData(uuu);
            }
           
            if(cont.get(cust.getID()) != null) {
            	cust.setPersonalData(Converter.enCodeEst(cont.get(cust.getID())+""));
            }
           
                customers.put(nimi, cust);
                custMap2.put(cust.getID(), cust);
            }
            

            String sql = "SELECT d.klient AS klient, SUM(d.tasumata) AS summa  from firma34.dok d  WHERE  " +
              "  current_date >= d.tasukuup and d.doktyyp='G' AND d.tasumata <> 0 AND length(klient) > 0 GROUP BY d.klient ORDER BY d.klient";
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

		
		
		

    public TreeMap getProducts(Agent agent) {
        connectionUp();
        TreeMap products = new TreeMap();
        
        HashMap altuses = new HashMap();
        HashMap partiid = new HashMap();
        HashMap omahinnad = new HashMap();
        HashMap kriitiline = new HashMap();


//System.out.println("Kiilis " + kiilis.size());
       
        
 
 String sql4 = " select toode , sum(kogus) as kokku  from firma34.hetkesei " + 
 " where  osak='1' group  by toode   " ;

try{
	Connection con = DriverManager.getConnection(url,"odbcmerisa","pMerisiil49");
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
      

          String cmd = "SELECT toode, nimetus, tykke, myygihind, yhik  ,liik" +
          		",ribakood ,seisund , miinhind FROM firma34.toode WHERE " +
              "  length(trim(toode)) >2 and length(trim(nimetus)) >1 and grupp ='L' ORDER BY toode";
try{
	Connection con = DriverManager.getConnection(url,"odbcmerisa","pMerisiil49");

	stmt = con.createStatement();
                    ResultSet  rs = stmt.executeQuery(cmd);

                      while (rs.next()) {
                          String kood = rs.getString("toode").trim();
                        
                          double kogus = 0;
                          if (altuses.get(kood) != null){
                            kogus = Double.parseDouble(altuses.get(kood)+"");
                          } 
                          
                          Product prod = new Product(kood);
                          String nimetus = rs.getString("nimetus").trim() + " " +  kood;
                         // System.out.println("Kriitiline " + " " + kood );
                          
                          prod.setInCases(rs.getDouble("tykke"));
                         
                          prod.setName(Converter.enCodeEst(nimetus));
                          prod.setPrice(rs.getDouble("myygihind"));
                          String yhik = rs.getString("yhik"); 
                        
                          prod.setStock(kogus);
                          prod.setGroup(parent.make(rs.getString("liik")));
                          prod.setEAN(parent.make(rs.getString("ribakood")).replaceAll(";", "*"));
                          if(yhik != null){
                        	prod.setUnit(yhik.trim());  
                          } 
                          if(kriitiline.get(kood) != null){
                        	     //	prod.setStatusNew("1") ; 
                          	prod.setStatusGiftPack("1");
                          	prod.setDiffColor(true);
                            }
                         
                          prod.setVat("24");
                          
                         if(partiid.get(kood) != null){
                        	prod.setPartii(partiid.get(kood) + "") ; 
                          }
                          
                         if(omahinnad.get(kood) != null){
                        	 double oh = Double.parseDouble(omahinnad.get(kood)+"");
                        	 double kate = (1-oh/prod.getPrice())*100;
                         	if(kate>0){
                         		String kat = (kate + "");
                               // prod.setMargin(kat.substring(0,kat.indexOf(".")));
                         	}
                         	//prod.setMinPrice(oh*1.2);
                           }
                         boolean goes = true;
                        String seisund = rs.getString("seisund");
                         if(seisund != null && seisund.trim().equals("A")){
                            goes = false;
                           } 
                         
                         if(prod.getGroup().equals("TEENUS")) {
                        	 goes = false;
                         }
                         
                         
                         double minpr = rs.getDouble("miinhind");
                       if(minpr > 0) {
                    	   prod.setMinPrice(minpr);
                       }
                         
                         if(goes) {
                        	 products.put(nimetus, prod);
                        	 prods.add(prod.getID());
                         }

                      }
                      rs.close();
                      stmt.close();

                      
                                
        } catch (Exception e) {
        	e.printStackTrace();
            getParent().showError(e);
        }

System.out.println("tooted " + products.size());

        return products;
    }

    HashSet prods = new HashSet();
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
    	     String    sql = "select * from firma34.artliik where grupp = 'L' ";
    	     
    	     ResultSet rs = stmt.executeQuery(sql);

    	     while (rs.next()) {
    	       String liik = rs.getString("liik").trim();
    	       String nimetus = rs.getString("artlnimi").trim();
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
	    "SELECT dok.saaja ,rid.toode,dok.kuupaev,max(rid.kogus) as kogus,max(rid.hind)  as hind , max(tasudok) as dokk FROM firma34.dok INNER " +
	  " JOIN firma34.rid ON dok.dokumnr = rid.dokumnr INNER JOIN firma34.toode on rid.toode = toode.toode INNER JOIN firma34.klient k on k.kood= dok.saaja WHERE " +
	   "  length(trim(dok.klient)) > 0 AND length(trim(rid.toode)) > 2 " +
	   "  AND dok.kuupaev >= current_date-720 " +
	   "  AND  dok.doktyyp in  ('G')  GROUP BY dok.saaja, rid.toode , dok.kuupaev ORDER BY dok.saaja, rid.toode,dok.kuupaev ";
    
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
        String dok = rs.getString("dokk");
            if (kliendikood.length() > 0 && kogus != 0) {
                  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,kuupaev.substring(5,10),hind,0,0," ");
                if(dok != null)
                  cp.setAbiKood(dok.trim());
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
      String sql = "SELECT klient,saaja,(pais2obj) as age, tasudok AS number,tasukuup AS maksepaev,tasumata, kuupaev FROM " +
                   "firma34.dok WHERE  doktyyp='G' AND tasumata<>0 " +
                   "ORDER BY klient";

      connectionUp();
      PreparedStatement st = con.prepareStatement(sql);
     // st.setString(1, agentCode);
      ResultSet rs = st.executeQuery();
      while(rs.next()){
        Debt debt = new Debt();
        String klient = rs.getString("klient");
        String saaja = rs.getString("saaja");
        if(saaja != null){
        	 debt.setCustID(saaja.trim());
        	 debt.setPayerID(klient.trim());
        	 try {
        	
        	 String aa = rs.getString("age").trim() +"-"+ rs.getString("number").trim();
             debt.setDoknr(aa);
             java.sql.Date dd1 = rs.getDate("maksepaev");
             if(dd1 == null) {
            	 dd1 = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
             }
             debt.setDuedate(dd1);
             debt.setSum(rs.getDouble("tasumata"));
             java.sql.Date dd = rs.getDate("kuupaev");
             if(dd == null) {
            	 dd = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
             }
             debt.setDate(dd);
             list.addElement(debt);
        	 
        	 }catch(Exception k) {
        		 
        	 }
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
                "select sum(doksumma)/1.22 as seekuu  from firma34.dok where (month(kuupaev))=(month(current_date)) and doktyyp='G' AND year(current_date)=year(kuupaev) AND pais2obj=?";
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
                "select sum(doksumma)/1.22 as seekuu  from firma34.dok where (month(kuupaev))=(month(current_date)) and yksus='3' and doktyyp='G'"
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
                "select sum(doksumma)/1.22 as seekuu  from firma34.dok where (month(kuupaev))=(month(current_date)) and yksus='4' and doktyyp='G' AND year(current_date)=year(kuupaev) AND pais2obj=?";
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
    public static final String DRIVER_VERSION = "EevaKevamer";
 //   private String	       url = "jdbc:odbc:PostgreKEV";
  private String url = "jdbc:postgresql://erp.eetasoft.ee/sba";
    
    private Connection	       con = null;
    private Statement	       stmt;
    private String	       subjectFilesTo = null;
	
    private TreeMap getPlans() {
    	TreeMap res = new TreeMap();
    	
    	try{
    	 	   Calendar cal = Calendar.getInstance();
    	 	   String month = (cal.get(Calendar.MONTH)+1)+ "";
    	 	   if (month.length() == 1){
    	 		   month = "0" + month;
    	 	   }
    
    	 	   int jooksev = cal.get(Calendar.MONTH); 
    	 	   File f = new File (getExcelDir() + "plaan.xlsx");
    	 	   FileInputStream	 ff = new FileInputStream(f);
    	 	
    	 		XSSFWorkbook workbook = new XSSFWorkbook((ff));
    	 		XSSFSheet sheet = workbook.getSheetAt(0);
    	 		XSSFRow row = null; 
    	 				ff.close();	
    	 				if(sheet != null){
    	 					
    	 					for(int i = 4 ; i < 8 + 1;i++){
    	 						row = sheet.getRow(i);
    	 						String agent = null;
    	 						try {
    	 							agent = (row.getCell((short) 1).getStringCellValue());
    	    					      } catch (Exception e) {
    	    					    	  agent = (int)(row.getCell((short) 1).getNumericCellValue()) + "";	  
    	    					}
    	 						System.out.println("agent " + agent );
    	 						int kenn = 3;
    	 						double kent = (row.getCell((short) kenn + jooksev ).getNumericCellValue());
    	 						int certt = 16;
    	 						double cert = (row.getCell((short) certt + jooksev ).getNumericCellValue());
    	 						int alll = 29;
    	 						double all = (row.getCell((short) alll + jooksev ).getNumericCellValue());
    	 						
    	 						Toode tt = new Toode (agent);
    	 						tt.addKogus(kent);
    	 						tt.setDonecur(cert);
    	 						tt.setDonevol(all);
    	 					
    	 						res.put(agent, tt);
    	 						
    	 					}
    	 				}
          }catch(Exception e) {
    		e.printStackTrace();
    	  }
    	
   // System.out.println("res " + res.toString());	
    	return res;
    }
    
    
    
    @Override
	public void setBulkSales(Map agents) {
		
    TreeMap plan = getPlans();
    
    
		  connectionUp();
    		String cmd =  "  select sum(doksumma)/1.22 as sales , dok.pais2obj  AS agent   from firma34.dok where (month(kuupaev))=(month(current_date)) and doktyyp='G' AND year(current_date)=year(kuupaev) " +
			  "  GROUP BY dok.pais2obj" ;
    		
    		String cmd2 =  "  select sum(doksumma) as sales , dok.pais2obj  AS agent   from firma34.dok where "
    				+ " (month(kuupaev))=(month(current_date)) and doktyyp='T' AND year(current_date)=year(kuupaev) " +
    				  "  GROUP BY dok.pais2obj " ;
    	
    	
    		log(cmd);
    		
    		try {
    			
    			Statement stmt = con.createStatement();		
    		
    			
    			ResultSet	      rs = stmt.executeQuery(cmd);
    			while(rs.next()) {
    				String agentID = rs.getString("agent");
    				if(agentID != null){
    					Agent agent = (Agent)agents.get(agentID.trim());
        				if (agent != null){
        					agent.setStart("Vormistatud : " + (int)rs.getDouble("sales")+ "€");
        				}
    				}
    				
    			}
    			
    			 rs = stmt.executeQuery(cmd2);
     			while(rs.next()) {
     				String agentID = rs.getString("agent");
     				if(agentID != null){
     					Agent agent = (Agent)agents.get(agentID.trim());
         				if (agent != null){
         					
         					Toode tt = (Toode)plan.get(agentID.trim());
         	      			double all = 0;
         	      			
         	      			if(tt != null) {
         	      				all = tt.getDonevol();
         	      			}
         	      			
         	      			double sales = rs.getDouble("sales");
         	      			String uuu = (int)(all-sales)+"";
         	      			if((all-sales) < 0) {
         	      				 uuu = "+" + (int)(all-sales)*-1+"";
         	      			}
         	      			
         	      			
         					agent.setStart(agent.getStart()+"|Tellimused : " + (int)sales+ "€||EESMÄRK : " + (int)all  + "€|TÄITMATA : "+ uuu + "€|" );
         				
         				}
     				}
     				
     			}
    			
     			
        		String cmd3 =  "  select sum(kogus *hind *(100-ostuale)/100) as sales , dok.pais2obj  AS agent , liik  from firma34.dok inner join \r\n" + 
        				"			   firma34.rid ON dok.dokumnr = rid.dokumnr INNER JOIN firma34.toode on rid.toode = toode.toode where \r\n" + 
        				"    				 (month(kuupaev))=(month(current_date)) and doktyyp='T' AND year(current_date)=year(kuupaev) \r\n" + 
        				"					 and liik in ('CERTANIUM','KENT')\r\n" + 
        				"    				    GROUP BY dok.pais2obj,liik" ;
        			
     			 rs = stmt.executeQuery(cmd3);
     			 TreeMap sal = new TreeMap();
      			while(rs.next()) {
      				String agentID = rs.getString("agent");
      				String grupp = rs.getString("liik").trim();
      				double sale = rs.getDouble("sales");
      				if(agentID != null){
      					Agent agent = (Agent)agents.get(agentID.trim());
          				if (agent != null){
          					TreeMap sisu = new TreeMap();
          					if(sal.get(agentID.trim()) != null) {
          						sisu = (TreeMap)sal.get(agentID.trim());
          					}
          					sisu.put(grupp, sale);
          					sal.put(agentID.trim(), sisu);
          				}
      				}
      			}
     			
      			
      		Iterator itera = 	plan.keySet().iterator();
      		
      		while (itera.hasNext()) {
      			String ag = (String)itera.next();
      			
      			Agent agent = (Agent)agents.get(ag.trim());
  				if (agent != null){
  				
      			Toode tt = (Toode)plan.get(ag);
      			double kent = tt.getKogus();
      			double cert = tt.getDonecur();
      			double all = tt.getDonevol();
      			TreeMap sisu = (TreeMap)sal.get(ag);
      			
      			if(kent > 0) {
      				double ksal = 0;
      				if(sisu != null && sisu.get("KENT") != null) {
      					ksal = (Double) sisu.get("KENT");
      				  }
      				int done = (int)(kent - ksal);
      				String asi = "TÄITMATA : " + done ;
      				if(done < 0 ) {
      					asi = "ÜLE : " + -1*done ;
      				}
      				agent.setStart(agent.getStart()+"|KENT : " + (int)ksal + "€|" + asi + "€|" );
         		}
      			
      			if(cert > 0) {
      				double kcert = 0;
      				if(sisu != null && sisu.get("CERTANIUM") != null) {
      					kcert = (Double) sisu.get("CERTANIUM");
      					
      					int done = (int)(cert - kcert);
          				String asi = "TÄITMATA : " + done ;
          				if(done < 0 ) {
          					asi = "ÜLE : " + -1*done ;
          				}
      					agent.setStart(agent.getStart()+"|CERTANIUM : " + (int)kcert + "€|"+ asi + "€" );
      				}	
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
