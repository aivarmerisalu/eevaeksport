/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import ee.mobilejava.wrapper.DistEvalLineWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Dealer;
import eevaeksport.DealerProduct;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.PlansBonuses;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.ClientBlock;
import eevaeksport.report.SalesSetter;
import eevaeksport.report.StockSetter;
import eevaeksport.util.Converter;


/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class VSDBDriver extends AbstractDBDriver implements Manager,BulkUpdater, ClientBlock, StockSetter,SalesSetter,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
	 public String getProductSortingData(){
	   	return null;
	   }
	
	
    public VSDBDriver(EksportPanel parent) {
      super(parent);
        if(parent.getProps().getProperty("language").equals("Russian")){
        this.nimetu = "rusnimetus";
        }

        

        if(parent.getProps().getProperty("planbonusmeasure") != null &&
           parent.getProps().getProperty("planbonusmeasure").equals("money") ){
        this.kordaja = "plaanhind";
        }

        this.subjectFilesTo = parent.getString("filemsg");
        if (parent.getProps().getProperty("dburl") != null &&
                         parent.getProps().getProperty("dburl").trim().length()>0 ){
                       this.url = parent.getProps().getProperty("dburl").trim();
                     }
        if (parent.getProps().getProperty("report.dburl") != null &&
                        parent.getProps().getProperty("report.dburl").trim().length()>0 ){
                       this.url2=parent.getProps().getProperty("report.dburl").trim();

                     }

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
    }


    String user =  null;
    String pass = null;
    String rcompanyx = null;
    
    
    private void updateClientStock()
    	    throws IOException, SQLException {
  

    	    System.out.println("Laoseis Update");

    	    
    		    HashMap hm = new HashMap();

    		    //laema faili alla 
    		    
    		    File ff = new File("stock");
    		    
    		    if(!ff.exists()) {
    		    	ff.mkdir();
    		    }
    		    
    		   Converter.downloadFile(parent.getProps().getProperty("stockurl"), "stock/stockref.txt");
    		 

    		    String	   fileName = "stock/stockref.txt";

    		    BufferedReader br =
    			new BufferedReader(new FileReader(fileName));
    		    String	   line = null;
    		    String	   dealerID = null;
    		    ArrayList      prods = new ArrayList();

    		    while ((line = br.readLine()) != null) {
    		    	 System.out.println("line " +  line);
    			StringTokenizer st = new StringTokenizer(line, ";");

    			if (st.hasMoreTokens() && line.trim().length()>0) {
    			    dealerID = "1";

    			    if (st.hasMoreTokens()) {
                                  String prodID = st.nextToken();
                               
    				Product p = new Product(prodID);
    				st.nextToken();
    				
    				if (st.hasMoreTokens()) {
    				    p.setStock(Double.parseDouble(st.nextToken()));
    				}

    				prods.add(p);
    				 if(dealerID.length() < 3 && dealerID.trim().length() >0 && prods.size()>1) {
    				       hm.put(dealerID, prods);
    				    }
    			    }
    			   
    			}
    		    }
                        System.out.println("Dealer " + dealerID);
    		    br.close();
    		   
    		    
    		    System.out.println("hm " + hm.toString());
    		    
    		    if (hm.size() > 0) {
    			  setStocks(hm);
    		    }
    		
        }


    
    public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		File f = new File(this.getExcelDir() + "Sortiment.xls");
		
		
		try {
			
			
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheetAt(0);
			HSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(1);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 3; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			 System.out.println(groups.toString());

			for (int i = 3; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";

				if (row.getCell((short) 0) != null) {

					try {
						
						prodID = (row.getCell((short) 0).getStringCellValue());
					} catch (Exception e) {
						prodID = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					}
					
                   if(!prodID.equals("")){
                	
					// Siin l�heme reasisse rokkima ja vaatama kas se toode
					// l�heb kaasa
					for (int a = 3; a < row.getLastCellNum(); a++) {

						String group = (String) groups.get(a + "");
						if (group != null && group.trim().length() > 0) {

							String custID = " ";
							String clGroup = " ";
							String prodGroup = " ";
							String go = null;
							clGroup = group;

							MatrixLine mline = new MatrixLine();
							// Vaatame kas selles veerus on sellel tootel pandud
							// mingi t�his
							if (row.getCell((short) a) != null) {

								try {
									go = (int) (row.getCell((short) a)
											.getNumericCellValue()) + "";
								} catch (Exception e) {
									go = (row.getCell((short) a)
											.getStringCellValue());
								}

							}

							if (go != null && go.trim().toLowerCase().equals("1")) {
                                  if(a > 3){
                                 	 System.out.println( a + " " + prodID + " " +
             								 clGroup + " " + go); 
                                  }
								 

								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
								}

								if (prodID != null
										&& prodID.trim().length() > 0) {
								     mline.setProdID(prodID.trim());		
								}
								
								if (mline.getProdID() == null
										&& prodGroup != null
										&& prodGroup.trim().length() > 0) {
									mline.setProdGroup(prodGroup);
								}
								  
								result.add(mline);
								
							}
						 }
					  }
				   }
				}
			 }
			
		} catch (Exception e) {
			//e.printStackTrace();
		}

		// System.exit(0);
   System.out.println("fixed size " + result.size());
		return result;

	}


    
    
    
        public String getSaveStockInfo(){
        	this.getProducts(null);
        	Converter.convertCoopSortimenttoMobitrekFormat(eans);
        return null;
        }
        public Vector getPriceMatrixData() {
           	return null;
           }
    	public Vector getLastThreeData() {
    		// TODO Auto-generated method stub
    		return null;
    	}
        public String getAssortmentStr(Agent agent){
        	return null;
        }

    /**
     *
     */
        public String getSaveDebtInfo() {
        	try {
				updateClientStock();
			} catch (IOException | SQLException e) {
				e.printStackTrace();
			}
           	return null;
           }
           
    public void connectionUp() {

	if (con == null) {
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
    public void connectionDown() {
	try {
	    stmt.close();
	    con.close();

	    con = null;
	} catch (Exception e) {
	    getParent().showError(e);
	}
    }


                                       public String getCustProdDiscounts(Agent agent){
                                        return null;
                                        }





             public String getDistribution() {
      connectionUp();
      StringBuffer sb = new StringBuffer();

      try {
        String cmd = "SELECT toode,left (artlnimi,3)+ ' ' + nimetus AS nimi FROM toode t INNER JOIN  ../andmed/artliik l on" +
        		" t.liik=l.liik  WHERE pangateen=.T. and l.grupp='L' ORDER BY artlnimi, nimetus";


        PreparedStatement st = con.prepareStatement(cmd);
        ResultSet rs = st.executeQuery();

                 while (rs.next()) {

          sb.append(rs.getString("toode").trim() + ";" + rs.getString("nimi").trim() + "\n");
        }
        st.close();

      } catch (Exception e) {}
      System.out.println(sb.toString());
      return sb.toString();

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

	/*try {
	    getPlansBonuses("2");
	} catch (SQLException e) {
	    e.printStackTrace();
	}*/

	// String  cmd = "SELECT yksus,nimetus,objekt FROM yksus WHERE klassif=2";
	String  cmd = "SELECT yksus,nimetus, 2 AS objekt, klassif ,agentcode ,tyyp FROM yksus order by nimetus";
	TreeMap agents = new TreeMap();

	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		int klassif = rs.getInt("klassif");
                String agentstr = rs.getString("agentcode").trim();
                String asendus = rs.getString("tyyp");

		if (klassif == 2) {
		    Agent agent = new Agent(rs.getString("yksus").trim());

		    agent.setObject(rs.getString("objekt").trim());

		    String name = rs.getString("nimetus").trim();

		    if(asendus != null && asendus.trim().length() > 0){
		    	StringTokenizer st = new StringTokenizer(asendus.trim(),",");
		    	while(st.hasMoreTokens()){
		    		agent.addDubbler(st.nextToken());
		    	}
		    }
		    
		    
		    agent.setLanguage("EST");
	          agent.setCountry("ee");
	          agent.setPassWord("0000");
	          String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
	          if(email.length() > 1){
	        	  agent.setEmail(email);
	        	  agent.setActive(true);
	          }
	         
		    agent.setName(name);
                    agent.setIsPilot(true);
		    agents.put(name, agent);
		    agents1.put(agent.getID(), agent);
		    
		} else if (klassif == 1 && agentstr.length() == 0) {
		    Dealer dealer = new Dealer(rs.getString("yksus").trim());

		    dealer.setName(Converter.enCodeEst(rs.getString("nimetus").trim()));
		    addDealer(dealer);
		}
	    }
	} catch (Exception e) {
	    getParent().showError(e);
	}

	connectionDown();

	return agents;
    }

    public Vector getDebt(String agentCode){
      return null;
    }

    public void blockClients(HashMap clients) throws SQLException{
      connectionUp();
      Iterator iter = clients.keySet().iterator();
      String dlr = (String)iter.next();
      if(dlr != null && dlr.trim().length() < 3){
      String sql = "UPDATE klient SET blokk" + dlr + "='',debtdate" + dlr + "='' ";
      Statement st = con.createStatement();
      st.executeUpdate(sql);
      st.close();

      sql = "UPDATE klient SET blokk" + dlr + "=?,debtdate" + dlr + "=? WHERE kood" + dlr + "=?";
      PreparedStatement pst = con.prepareStatement(sql);
      iter = clients.keySet().iterator();
      while(iter.hasNext()){
        String dealer = (String)iter.next();
        pst.setString(1, dealer);
        pst.setString(2, getStartingTime());
        ArrayList al = (ArrayList)clients.get(dealer);
        Iterator alIter = al.iterator();
        while(alIter.hasNext()){
          pst.setString(3, (String)alIter.next());
          pst.executeUpdate();
        }
      }
      
      pst.close();
      }
    }






    public void setSales(HashMap custprods) throws SQLException{
      connectionUp();
      Iterator iter = custprods.keySet().iterator();
      String dlr = (String)iter.next();
      String sql =" INSERT INTO rid VALUES('T',99999, '', ?, ?, ?,?,'K','',0,'',?,?,'', " +
           "?,MONTH(date()-20),YEAR(date()-20),0,0,? ,'','','',0,0,'','','',0,0,.F.,.F.,0,'',0)" ;

       String sql1 = "SELECT kood FROM KLIENT WHERE kood" + dlr + "= ? OR smartsaaja= ?";
       String sql2 = "SELECT toode FROM toode WHERE kood" + dlr + "= ?  OR ribakood= ?";

       PreparedStatement pst = con.prepareStatement(sql);
       iter = custprods.keySet().iterator();
       while (iter.hasNext()) {
         String dealer = (String) iter.next();
         ArrayList al = (ArrayList) custprods.get(dealer);

         for (int i = 0; i < al.size(); i++) {

           String owncustid = "MUUKLIENT";
           String ownprodid = "11111111";

           CustProd p = (CustProd) al.get(i);
           String custid = p.getCustID();
           String prodid = p.getProdID();
           Date daatum = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
           if(p.getReportmonth() != null){
           	daatum = p.getReportmonth();
           }

           PreparedStatement pst1 = con.prepareStatement(sql1);
           pst1.setString(1, custid);
           pst1.setString(2, custid);
           ResultSet rs = pst1.executeQuery();
           while (rs.next()) {
             owncustid = rs.getString("kood").trim();
           }

           PreparedStatement pst2 = con.prepareStatement(sql2);
           pst2.setString(1, prodid);
           pst2.setString(2, prodid);
           ResultSet rs2 = pst2.executeQuery();
           while (rs2.next()) {
             ownprodid = rs2.getString("toode").trim();
           }
           pst.setDouble(1, p.getLastPrice());
           pst.setDouble(2, p.getMovedStockPluss());
           pst.setString(3, ownprodid);
           pst.setString(4, prodid);
           pst.setString(5, custid);
           pst.setString(6, dealer);
           pst.setDate(7, daatum);
           pst.setString(8, owncustid);
           pst.executeUpdate();
         }
       }

       pst.close();
    }





    public void setStocks(HashMap products) throws SQLException{
      connectionUp();
      String startday = getStartingTime();
      Iterator iter = products.keySet().iterator();
      String dlr = (String)iter.next();
      System.out.println("Dealer '" +  dlr + "'");
      if(dlr != null && dlr.trim().length() < 3){
        String sql = "UPDATE toode SET stock" + dlr + "=0,stockdat" + dlr + "='' ";
        Statement st = con.createStatement();
        st.executeUpdate(sql);
        st.close();      	
      }

       String sql = "UPDATE toode SET stock" + dlr + "=?,stockdat" + dlr + "=? WHERE trim(kood" + dlr + ")=?";
      PreparedStatement pst = con.prepareStatement(sql);
      iter = products.keySet().iterator();
      while(iter.hasNext()){
        String dealer = (String)iter.next();
        
        if(dealer != null && dealer.trim().length() < 3){
        ArrayList al = (ArrayList)products.get(dealer);

        for (int i = 0 ; i < al.size() ; i++){
          Product p = (Product)al.get(i);
          pst.setDouble(1, p.getStock());
          pst.setString(2, startday);
          pst.setString(3, p.getID());
          pst.executeUpdate();
        }}
      }
      pst.close();
    }

    /**
     * Method declaration
     *
     *
     * @param agent
     *
     * @return
     *
     * @see
     */
    public TreeMap getAgents1() {
		return agents1;
	}
    
    private TreeMap agents1 = new TreeMap();
    
    
    
    
    private TreeMap getGrupid(){
	      File fff = new File(this.getExcelDir() + "Sortiment.xls" );
	      TreeMap groups = new TreeMap(); 
	  	try{
	          FileInputStream ff = new FileInputStream(fff);
	  		HSSFWorkbook workbook = new HSSFWorkbook((ff));
	  		HSSFSheet sheet = workbook.getSheetAt(1);
	  		HSSFRow row = null;
	  		
	  		row = sheet.getRow(0);
	  	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
	  		
	  		//System.out.println("rownum " + row.getLastCellNum());
	  		
	  		
	  		for(int a = 1 ; a < sheet.getLastRowNum();a++){
	  			try{
	  			//	Loeme sisse rea
	  				row = sheet.getRow(a);
	  				if (row.getCell((short) 0) != null) {
	  				


	  	    			   String kood = null;
	  	    			  if(row.getCell((short)0) != null){
		    	    			  	try{
		    	    			  	  kood = row.getCell((short)0).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			   	  kood = row.getCell((short)0).getNumericCellValue()+"";  
		    	    	    		} 
		    	    			  	 if(kood.indexOf(".")!= -1){
		    	    			  	 	 kood = (kood.substring(0,kood.indexOf(".")));	
		    	    			  	 }
		    	    			  	 String sort = null;
		    	    			  	try{
		    	    			  		sort = row.getCell((short)1).getStringCellValue();  
			    	    			  	 }catch (Exception e){
			    	    			  		sort = row.getCell((short)1).getNumericCellValue()+"";  
			    	    	    		} 
			    	    			  	 if(sort.indexOf(".")!= -1){
			    	    			  	 	 sort = (sort.substring(0,kood.indexOf(".")));	
			    	    			  	 }
			    	    			  	 sort = sort.replace(',', '|').replace(',', '|').replace(',', '|').replace(',', '|');
		    	    			  	 groups.put(kood,Converter.cleanLitLatEst(sort));
	  	    			  
	  	    				
	  				}	   
	  				}
	 			}catch (Exception e){
	  			//	System.out.println(row.getCell((short)a).getNumericCellValue());
	  			}
	  		}
	  		
	  		
	  
	  	 }catch (Exception e){
	  		e.printStackTrace();
	  	}
	  	 
	  	 System.out.println("grupid suurus " + groups.size() + " " + groups.toString() );
	      
	     return groups; 
	  	 
	  }


    
    
    public TreeMap getCustomers(Agent agent) {
    	
    	
    	 try{
       	   Converter.getManagersReplacements(getAgents1(), rcompanyx, user, pass); 
       		
       }catch(Exception e){
       	
       }
	connectionUp();
	 Converter cc = new Converter();
     HashMap kyssad =   cc.getQuestionaire(this.getExcelDir());
     TreeMap groups = this.getGrupid();
     
	String cmd =
	    "SELECT k.kood, k.nimi, 0 AS tasumata,visit,dealer, trim(tanav)+ ' ' +trim(riik)+ ' ' +trim(piirkond) AS Address, trim(telefon) as kontakt ,liik,info , " +
           "blokk1,blokk2,blokk3,blokk4,blokk5,  " +
           "blokk6,blokk7,blokk8,blokk9,blokk10,hinnak,k.email,k.turg,k.distributs AS kuniksdist,maksetin AS ting, objekt2 as ag, parool as ag2 ,"
           + " tanav,riik,piirkond FROM klient k "
	    + "LEFT JOIN yksus y ON y.yksus=k.dealer GROUP BY k.kood, k.nimi order by k.nimi";

       System.out.println(cmd);

	TreeMap customers = new TreeMap();

	try {
	    PreparedStatement st = con.prepareStatement(cmd);
	    ResultSet rs = st.executeQuery();

	    while (rs.next()) {
		Customer cust = new Customer(rs.getString("kood").trim());
		String   nimi = rs.getString("nimi").trim();

		cust.setName(Converter.enCodeEst(nimi));
		cust.setDebt(rs.getDouble("tasumata"));
		cust.setVisit(rs.getString("visit").trim());
                String aadr = rs.getString("address");
                if(aadr != null){
		       cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim()),"Estonia"));
                }
                
                String tel = rs.getString("kontakt");
                if(tel != null && tel.trim().length() > 0){
                	cust.setPersonalData(tel.trim());
                }
                String dealerID = rs.getString("dealer").trim();
                cust.setGroup(Converter.enCodeEst(rs.getString("liik").trim()));
                String info = rs.getString("info") ;
                if(info != null){
                  if(!info.trim().equals(""))
                cust.setInfo(Converter.enCodeEst(info.trim()));
                }

                if (!dealerID.equals("")) {
                  Dealer dealer = (Dealer) getDealers().get(dealerID);

                  if (dealer != null) {
                    cust.setDealer(dealer);
                  }
                }

                
                String grp = (String)groups.get(cust.getID());
                if(grp != null){
                	Vector vv = new Vector();
                	StringTokenizer ste = new StringTokenizer(grp,"|");
                	while (ste.hasMoreElements()){
                		String gg = ste.nextToken();
                		vv.add(Converter.cleanLitLatEst(gg));
                	}
                	cust.setFixSortCode(vv);
               	System.out.println(cust.getName() + " " + vv.toString());
                //	cust.setGroup(grp);
                }

                String block = " ";
                String block1 = rs.getString("blokk1");
                String block2 = rs.getString("blokk2");
                String block3 = rs.getString("blokk3");
                 String block4 = rs.getString("blokk4");
                  String block5 = rs.getString("blokk5");
                   String block6 = rs.getString("blokk6");
                    String block7 = rs.getString("blokk7");
                     String block8 = rs.getString("blokk8");
                      String block9 = rs.getString("blokk9");
                      String block10 = rs.getString("blokk10");

              if(block1 != null && block1.trim().length()>0){
              block = block1.trim() ;
              }
              if(block2 != null && block2.trim().length()>0){
             block = block + block2.trim()+ "|" ;
           }
           if (block3 != null && block3.trim().length()>0) {
             block = block + block3.trim() + "|";
           }
           if (block4 != null && block4.trim().length()>0) {
             block = block + block4.trim() + "|";
           }
           if (block5 != null && block5.trim().length()>0) {
             block = block + block5.trim() + "|";
           }
           if (block6 != null && block6.trim().length()>0) {
             block = block + block6.trim() + "|";
           }
           if (block7 != null && block7.trim().length()>0) {
             block = block + block7.trim() + "|";
           }
           if (block8 != null && block8.trim().length()>0) {
             block = block + block8.trim() + "|";
           }
           if (block9 != null && block9.trim().length()>0) {
             block = block + block9.trim() + "|";
           }
           if (block10 != null && block10.trim().length()>0) {
             block = block + block10.trim() ;
           }
            cust.setBlockingDealer(block.trim());
            String ale = rs.getString("hinnak");
            if (ale != null && !ale.trim().equals("")){
            cust.setFixDisc(Double.parseDouble(ale));
            }
             String email = rs.getString("email");
              if (email != null && !email.trim().equals("")){
                cust.setConfirmEmail(email.trim());
              }
              boolean showmarket = rs.getBoolean("turg");
                 if(showmarket){
                   if(agent != null ){
                     agent.setShowmarketinfoall(true);
                   }
                 cust.setShowmarketinfo(true);
                 }

            Date upto = rs.getDate("kuniksdist");
            String tanga = rs.getString("ting");
            if(tanga != null && tanga.trim().length()>0){
              if(tanga.trim().equals("KS")){
                tanga="-1";
              }
              cust.setPaymTerm(tanga.trim());
            }
            Calendar cal = Calendar.getInstance() ;
            cal.setTime(upto);
            Calendar cal2 = cal;
            cal = Calendar.getInstance();

            if( cal2.getTimeInMillis() > cal.getTimeInMillis()){
            cust.setDistUpTo("1");
            System.out.println("Distributsiooni" + cust.getName());
            }
            
            cc.addQuestiosToCustomer(cust,kyssad);
            
            String ag = rs.getString("ag");
            if(ag != null &&ag.trim().length() > 0){
                cust.addAgent(new Agent(ag.trim()));	
                cust.addAgent(new Agent("4"));	
                

       	     if (ag != null && ag.trim().length()>0){
       				Agent agg = ((Agent)this.getAgents1().get(ag.trim()));
       				
       				if(agg != null && agg.getDubbler().size()>0){
       					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
       					 cust.addAgent(new Agent((String)agg.getDubbler().elementAt(i)));
       				//	System.out.println( "lisan dubbleri " + cust.getName() + " " + ag + " "+  (String)agg.getDubbler().elementAt(i));
       					}
       				
       					
       				}
       			  if ( this.getAgents1().get(ag.trim()) != null){
						Agent aa = (Agent)getAgents1().get(ag.trim());
					
						Vector vec = aa.getDubbler();
						if(vec!= null && vec.size() >0){
							Enumeration en = vec.elements();
							while(en.hasMoreElements()){
								String agw = (String) en.nextElement();
								cust.addAgent(new Agent(agw));
							}
						}
					}
       	     
       	     }
       	     
       	     
       	     
            }
            
            
             ag = rs.getString("ag2");
            if(ag != null &&ag.trim().length() > 0){
                cust.addAgent(new Agent(ag.trim()));	
            
       	     if (ag != null && ag.trim().length()>0){
       				Agent agg = ((Agent)this.getAgents1().get(ag.trim()));
       				if(agg != null && agg.getDubbler().size()>0){
       					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
       					 cust.addAgent(new Agent((String)agg.getDubbler().elementAt(i)));
       				//	System.out.println( "lisan dubbleri " + cust.getName() + " " + ag + " "+  (String)agg.getDubbler().elementAt(i));
       					}
       				}
       			}
            }
           
            String tn = rs.getString("tanav");
            String riik = rs.getString("riik");
            String piirkond = rs.getString("piirkond");
            
           
            if(riik != null && riik.trim().length() >0 ) {
            	cust.setCountry(riik.trim());
            }
            if(piirkond!= null && piirkond.trim().length() >0 ) {
            	piirkond = piirkond.trim();
            }else {
            	piirkond = "";
            }
            if(tn != null && tn.trim().length() >0 ) {
            	cust.restStreet = tn.trim() + " " + piirkond;
            }
         
            
		customers.put(nimi, cust);
	    }

	} catch (Exception e) {
	    getParent().showError(e);
	}

	return customers;
    }

    /**
     * Method declaration
     *
     *
     * @param agent
     *
     * @return
     *
     * @see
     */
    
    private TreeMap eans = new TreeMap();
    
    public TreeMap getProducts(Agent agent) {
	connectionUp();

	TreeMap products = new TreeMap();



	try {

	   String cmd =
		"SELECT toode," + nimetu +",asukoht,kastis, myygihind, liik, plaanhind,minkogus,alusel,ribakood,ribakood3  FROM toode " +
               " WHERE len(trim(toode)) > 2 AND len(trim(liik))>0 AND len(trim("+nimetu+"))>0 AND eisaada = .F. ORDER BY toode ";

	    if (isDebug()) {
		System.out.println(cmd);
	    }

	    ResultSet rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		double  tykke = (rs.getDouble("kastis"));
		String  kood = rs.getString("toode").trim();

		String  nimetus = rs.getString(nimetu).trim();
		String asukoht = rs.getString("asukoht").trim();
		Product prod = new Product(kood);
		prod.setNameSorting(asukoht + nimetus);
		prod.setName(Converter.enCodeEst(nimetus));
		prod.setPrice(round(rs.getDouble("myygihind"), 2));
		prod.setInCases(tykke);
                prod.setGroup(rs.getString("liik").trim());
                prod.setMeasure(rs.getDouble("plaanhind"));
                prod.setMaxDiscount(rs.getDouble("minkogus"));
                String ean = rs.getString("ribakood");
                prod.setOnPallet(rs.getDouble("alusel"));
                if(ean != null && ean.trim().length()>7 ){
                prod.setEAN(ean.trim());
                eans.put(ean.trim(), kood);
                }
                String  status = rs.getString("ribakood3");

                if (status!= null && status.trim().length() > 0){
                    if (status.indexOf("F")!= -1){
                       prod.setMustBe("1");
                    }
                      if (status.indexOf("D")!= -1){
                        prod.setStatusDiscount("1");
                     }
                      if (status.indexOf("N")!= -1){
                       prod.setStatusNew("1");
                      }

                          if (status.indexOf("O")!= -1){
                             prod.setStatusSellout("1");
                          }
                }
                prod.setVat("22");
		products.put( nimetus, prod);

		// }
	    }
            rs.close();

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
	connectionUp();

	String cmd =
	    "SELECT dok.saaja,rid.toode, kogus, sum(iif(dok.kuupaev>date()-30,(kogus),0)) AS pkogus,dok.kuupaev AS lastdate FROM dok INNER "
	    + "JOIN rid ON dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode " +
           " INNER JOIN klient k ON dok.saaja=k.kood WHERE "
	    + " len(trim(dok.saaja)) > 0 AND len(trim(rid.toode)) > 2 ";
        cmd += "  AND dok.kuupaev > date()-180  ";	
        cmd += " AND k.objekt2 !='' AND  dok.doktyyp = 'G' GROUP BY dok.saaja, rid.toode ORDER BY dok.kuupaev DESC, dok.saaja,toode.asukoht, toode.nimetus";

	if (isDebug()) {
	    System.out.println(cmd);
	}

        String sql ="SELECT dok.saaja,rid.toode, sum(kogus) AS valjakogus FROM  dok " +
            "INNER JOIN  rid ON dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode INNER JOIN  klient k ON dok.saaja=k.kood " +
            " WHERE  len(trim(dok.saaja)) > 0 AND len(trim(rid.toode)) > 2 AND dok.kuupaev >date()-30 " +
       " AND dok.doktyyp = 'G' AND k.objekt2 !='' GROUP BY dok.saaja, rid.toode ORDER BY dok.kuupaev DESC, dok.saaja, toode.asukoht, toode.nimetus";

	Vector custProds = new Vector();
        HashMap kliendid = new HashMap();

	try {
          PreparedStatement pst = con.prepareStatement(cmd);
	    ResultSet	      rs = pst.executeQuery();

	    while (rs.next()) {
		String kliendikood = rs.getString("saaja").trim();
                Customer cust = (Customer) kliendid.get(kliendikood);
		String tootekood = rs.getString("toode").trim();
                double kogus = rs.getDouble("kogus");
                double plkogus = rs.getDouble("pkogus");
                String lastDate = rs.getString("lastdate").substring(5,10);
                CustProd cp = new CustProd(kliendikood, tootekood, kogus ,plkogus ,0,lastDate,0,0,0," ");
                custProds.add(cp);
                 kliendid.put(kliendikood, cust);
	    }
            rs.close();
            pst.close();

            PreparedStatement pst3 = con.prepareStatement(sql);
            ResultSet rs1 = pst3.executeQuery();
            while (rs1.next()) {
              String klient = rs1.getString("saaja").trim();
              String toode = rs1.getString("toode").trim();
              double pkogus = rs1.getDouble("valjakogus");
             // System.out.println(toode+"pkogus"+pkogus);

              Customer c = (Customer) kliendid.get(klient);
              if (c != null) {
                //System.out.println("klient"+c.getName());
                ArrayList al = (ArrayList) c.getProducts().get(toode);
                if (al != null) {
                  CustProd cp = (CustProd) al.get(1);
                  if (cp != null) {
                    cp.setMovedStockPluss(pkogus);
                    custProds.add(cp);
                  }
                }
              }
            }
            rs1.close();
	} catch (Exception e) {
	    getParent().showError(e);
	}

	connectionDown();

	return custProds;
    }

    
    
    public String getDealerCustomerCSV(String agent){
                 StringBuffer sb = new StringBuffer();
                 connectionUp();

                     String cmd = "SELECT yksus FROM yksus WHERE klassif=1";

                     if (isDebug()) {
                         System.out.println(cmd);
                     }


                     try {
                         ResultSet rs = stmt.executeQuery(cmd);

                         while (rs.next()) {
                             String       yksus = rs.getString("yksus").trim();
                             String    sql = "SELECT kood,blokk" + yksus + " AS plokk,kood" + yksus + " AS deacode ,debtdate" + yksus + " as daatum FROM klient WHERE kood" + yksus
                                             + "<>'' AND objekt2='" + agent + "' ORDER BY nimi";
                             Statement stm = con.createStatement();
                             ResultSet rst = stm.executeQuery(sql);

                             while (rst.next()) {
                               String klient = rst.getString("kood").trim();
                               String deacode = rst.getString("deacode");
                               String plokk = rst.getString("plokk");
                               String daatum = rst.getString("daatum");

                               if (deacode != null) {
                                 deacode = deacode.trim();
                                 if (deacode.length()==0){
                                  deacode = " ";
                                  }

                               }

                               if (plokk != null) {
                                 plokk = plokk.trim();
                                 if (!plokk.equals("1")) {
                                         plokk = "0";
                                       }
                               }

                               if (daatum != null) {
                                 daatum = daatum.trim();
                                 if (daatum.length()==0){
                                 daatum = " ";
                                 }
                               }



                               sb.append(yksus + ";" + klient + ";" + deacode + ";" + plokk + ";" +
                                         daatum + "\n");
                             }

                             rst.close();
                             stm.close();
                           }

                         rs.close();
                     } catch (Exception e) {
                         getParent().showError(e);
                     }
                     connectionDown();
                  return sb.toString();
                 }


    public DealerProduct getDealerProductsList() {
	connectionUp();

	String cmd = "SELECT yksus FROM yksus WHERE klassif=1";

	if (isDebug()) {
	    System.out.println(cmd);
	}

	if(dealerProds != null) {
		return dealerProds;
	}
	
	 dealerProds = new DealerProduct();

	try {
	    ResultSet rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String       yksus = rs.getString("yksus").trim();
                String        stock = "0";
		String    sql = "SELECT toode,stock" + yksus + " AS ladu,kood" + yksus + " AS deacode FROM toode WHERE kood" + yksus
				+ "<>'' ORDER BY nimetus";
		Statement stm = con.createStatement();
		ResultSet rst = stm.executeQuery(sql);

		while (rst.next()) {
                  String toode = rst.getString("toode").trim();
                  String ladu = rst.getString("ladu") ;
                  String deacode = rst.getString("deacode").trim();
                  if (!ladu.trim().equals("")){
                  stock = ladu ;
                  if (stock.equals("0")){
                    stock = " " ;
                  }
                  }
		    dealerProds.add(yksus, toode,stock," ",deacode);
		}

		rst.close();
		stm.close();
	    }

	    rs.close();
	} catch (Exception e) {
	    getParent().showError(e);
	}

	connectionDown();

	return dealerProds;
    }
    
    DealerProduct dealerProds = null;

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws SQLException
     *
     * @see
     */
    public TreeMap getProdGroups() throws SQLException {
	connectionUp();

	TreeMap   tm = new TreeMap();
	String    sql =
	    "SELECT liik,artlnimi AS nimetus FROM ../andmed/artliik WHERE grupp='L'";
	Statement st = con.createStatement();
	ResultSet rs = st.executeQuery(sql);

	while (rs.next()) {
	    tm.put(Converter.enCodeEst(rs.getString("liik").trim()),
	    		Converter.enCodeEst(rs.getString("nimetus").trim()));
	}

	rs.close();
	st.close();
	connectionDown();

	return tm;
    }

    /**
     * Method declaration
     *
     *
     * @param agent
     *
     * @return
     *
     * @throws SQLException
     *
     * @see
     */
    public PlansBonuses getPlansBonuses(String agent) throws SQLException {
	connectionUp();

	PlansBonuses pb = new PlansBonuses();
	String       sql =
	    "SELECT liik FROM ../andmed/artliik WHERE grupp='L' ";
	Statement    st = con.createStatement();
	ResultSet    rs = st.executeQuery(sql);
	String       sqlSelectList = "";
	int	     liik = -1;
	int	     counter = 0;

	while (rs.next()) {
	    counter++;
            liik = rs.getInt("liik");
            if(liik < 14) {
            	   pb.add(liik, 0, 0, 0, 0);
           	    if (!sqlSelectList.equals("")) {
           		sqlSelectList = sqlSelectList + ",";
           	    }   	
        	    sqlSelectList = sqlSelectList + "plaan" + liik + ",boonus" + liik;
            }
	}

        sql =
            "SELECT SUM(kogus*" + kordaja + ") AS plaandone,SUM(baasostu) AS bonusdone,"
            + sqlSelectList + ", a.liik FROM dok d "
            + "INNER JOIN  rid r ON r.dokumnr=d.dokumnr LEFT JOIN toode t "
            + "ON t.toode = r.toode INNER JOIN klient k ON k.kood=d.klient LEFT JOIN ../andmed/artliik a ON a.liik=t.liik LEFT JOIN yksus y "
            + "ON y.yksus=d.pais2obj  WHERE klassif=2 AND k.objekt2=? AND a.liik IN (SELECT liik FROM ../andmed/artliik WHERE grupp='L') AND "
            + "year(date())=year(kuupaev) AND month(date())=month(kuupaev) AND r.doktyyp='G' AND k.outbox=.F. AND d.doktyyp='G' "
            + "  AND d.yksus IN (SELECT yksus FROM yksus WHERE klassif=1 AND block=.F.) "
	    + "GROUP BY a.liik";

	PreparedStatement pst = con.prepareStatement(sql);

	pst.setString(1, agent);

	rs = pst.executeQuery();

	while (rs.next()) {
          int index = rs.getInt("liik");
          
          if(index < 14) {
        	  pb.add(index, rs.getDouble("plaan" + index),
       		       rs.getDouble("boonus" + index), rs.getDouble("plaandone"), rs.getDouble("bonusdone"));  
          }
        
	}

	//System.out.println(counter + ". " + sql);

	return pb;
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
	    String sql =
                "SELECT SUM(t.myygihind*r.kogus*(1-iif(hk.jrk!=0,hk.jrk,0)/100)) AS sales FROM rid r INNER JOIN dok d ON d.dokumnr=r.dokumnr " +
                "INNER JOIN toode t ON t.toode=r.toode INNER JOIN klient k ON k.kood=d.klient INNER JOIN yksus y ON y.yksus=d.yksus " +
                "LEFT JOIN hkpais hk ON hk.hinnak=k.hinnak  WHERE k.objekt2 =? " +
                "AND year(date())=year(kuupaev) AND len(trim(y.agentcode))=0 AND r.doktyyp='G' AND month(date())=month(kuupaev)" ;

	    if (isDebug()) {
		System.out.println(sql);
	    }

	    PreparedStatement pstmt = con.prepareStatement(sql);

	    pstmt.setString(1, agentCode);

	    ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
              setSales(rs.getDouble("sales"));
            }
String sql2 = "select evers from yksus where klassif=2 and yksus = ?";
pstmt = con.prepareStatement(sql2);
pstmt.setString(1, agentCode);

 rs = pstmt.executeQuery();
    while(rs.next()){
     try{
    	 setBudget(Double.parseDouble(rs.getString("evers")));
     }catch(Exception e){
    	 
     }
    }
	    rs.close();
	    pstmt.close();
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
     * @param pilot
     *
     * @return
     *
     * @see
     */

    public String getNonMobileOrders(Agent agent){
    	connectionUp();
        StringBuffer sb = new StringBuffer();
        sb.append(
              "Jooksva kuu plaani t�itmise aruanne\n\n KliendiLIIK       T�IDETUD\n");
        try {
         String sql =
          	"SELECT round(SUM(kogus*myygihind),2) AS plaandone ,a.nimetus FROM dok d  INNER JOIN  rid r ON r.dokumnr=d.dokumnr LEFT JOIN toode t " +
            "ON t.toode = r.toode INNER JOIN klient k ON k.kood=d.klient LEFT JOIN ../andmed/klliik a ON a.liik=k.liik LEFT JOIN yksus y " +
            "ON y.yksus=d.pais2obj  WHERE klassif=2 AND d.pais2obj= ?  AND d.doktyyp='G' AND a.liik IN (SELECT liik FROM ../andmed/klliik ) AND " +
            "year(date())=year(kuupaev) AND month(date())=month(kuupaev) AND d.yksus IN (select yksus from yksus where klassif=1 and block=.F.)AND k.outbox=.F. " +
            "GROUP BY a.liik";

          PreparedStatement pst = con.prepareStatement(sql);
           // System.out.println(agent.getID());
          pst.setString(1, agent.getID());

        ResultSet  rs = pst.executeQuery();
          while (rs.next()) {
            sb.append(rs.getString("nimetus") + "\n\t\t " +
                      rs.getDouble("plaandone") + "\n\n");
          }

        } catch (Exception e){}

       System.out.println(sb.toString());

        return sb.toString();


      }


    public String getPilotDayData(Agent agent){
    connectionUp();
    StringBuffer sb = new StringBuffer();
    sb.append(
          "Eelmise kuu plaani t�itmise aruanne\n\n LIIK     PLAAN    T�IDETUD\n");
    String sql =
        "SELECT liik FROM ../andmed/artliik WHERE grupp='L' ";
    try {
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);
      String sqlSelectList = "";
      int liik = -1;
      int counter = 0;

      while (rs.next()) {
        counter++;

        liik = rs.getInt("liik");
        //  pb.add(liik, 0, 0, 0, 0);
        if (!sqlSelectList.equals("")) {
          sqlSelectList = sqlSelectList + ",";
        }

        sqlSelectList = sqlSelectList + "plaan" + liik + ",boonus" + liik;
      }

      sql =
          "SELECT round(SUM(kogus*myygihind),2) AS plaandone,SUM(baasostu) AS bonusdone,"
          + sqlSelectList + ", a.liik ,a.artlnimi FROM dok d "
          + "INNER JOIN  rid r ON r.dokumnr=d.dokumnr LEFT JOIN toode t "
          + "ON t.toode = r.toode INNER JOIN klient k ON k.kood=d.klient LEFT JOIN ../andmed/artliik a ON a.liik=t.liik LEFT JOIN yksus y "
          + "ON y.yksus=d.pais2obj  WHERE klassif=2 AND d.pais2obj=?  AND d.doktyyp='G' AND a.liik IN (SELECT liik FROM ../andmed/artliik WHERE grupp='L') AND "
          +
          "year(date()-20)=year(kuupaev) AND month(date()-20)=month(kuupaev) AND d.yksus IN (select yksus from yksus where klassif=1 and block=.F.)AND k.outbox=.F. "
          + "GROUP BY a.liik";

      PreparedStatement pst = con.prepareStatement(sql);
       // System.out.println(agent.getID());
      pst.setString(1, agent.getID());

      rs = pst.executeQuery();


      while (rs.next()) {
       // System.out.println(sqlSelectList);
        int index = rs.getInt("liik");
        sb.append(rs.getString("artlnimi") + "\n\t " +
                  rs.getDouble("plaan" + index) + "   " +
                  rs.getDouble("plaandone") + "\n");
      }
    } catch (Exception e){}

   System.out.println(sb.toString());

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

    /**
     *
     * @return the TreeMap value of dealers.
     */
    public TreeMap getDealers() {
	return dealers;
    }


    /**
     *
     * @param aDealers - the new value for dealers
     */
    public void setDealers(TreeMap aDealers) {
	dealers = aDealers;
    }

    /**
     * Method declaration
     *
     *
     * @param dealer
     *
     * @see
     */
    private void addDealer(Dealer dealer) {
	getDealers().put(dealer.getID(), dealer);
    }

    public String getStartingTime(){
      Calendar cal = Calendar.getInstance();
      String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
      if (month.length() == 1) month = "0" + month;
      String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
      if (day.length() == 1) day = "0" + day;
      return day + month + cal.get(Calendar.YEAR);
    }

    public ManagerData getManagerData() throws SQLException{
      String sql = " SELECT clid AS klient,docsum AS summa,linecount AS ridu,text AS tekst,enddate AS kuupaev,y.nimetus AS dealer" +
       " FROM dayreport r INNER JOIN dok d ON d.klient=clid INNER JOIN yksus y ON y.yksus=d.yksus " +
                                   " WHERE enddate>(DATE()-30) AND d.kuupaev=enddate AND d.doktyyp='G'  AND y.klassif = 1 ORDER BY clid,enddate desc ";
      Connection c = DriverManager.getConnection(url2);
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(sql);

      String sql2 = "SELECT kood AS kood ,nimi AS nimi,maksetin AS maksetin,hinnaale AS ale," +
                   " y.nimetus AS agendinimi , kontaktisi AS kontaktisik , telefon AS telefon " +
                    "FROM klient k LEFT JOIN yksus y ON y.yksus=k.objekt2 WHERE klassif=2";
      connectionUp();
      Statement st2 = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs2 = st2.executeQuery(sql2);

      String sql3 = "SELECT SUM(kogus*myygihind) AS kuumyyk,d.klient FROM rid r INNER JOIN dok d ON d.dokumnr=r.dokumnr INNER JOIN toode t ON r.toode=t.toode " +
                    "WHERE MONTH(d.kuupaev)=MONTH(DATE()) AND YEAR(d.kuupaev)=YEAR(DATE()) AND d.doktyyp='G'  GROUP BY d.klient";
      Statement st3 = con.createStatement();
      ResultSet rs3 = st3.executeQuery(sql3);
      HashMap hm = new HashMap();
      while (rs3.next()){
        hm.put(rs3.getString("klient").trim(), new Double(rs3.getDouble("kuumyyk")));
      }
      rs3.close();
      st3.close();

      String sql4 = "SELECT SUM(kogus*myygihind) AS aastamyyk,d.klient FROM rid r INNER JOIN dok d ON d.dokumnr=r.dokumnr INNER JOIN toode t ON r.toode=t.toode " +
                    "WHERE YEAR(d.kuupaev)=YEAR(DATE()) AND d.doktyyp='G' GROUP BY d.klient";
      Statement st4 = con.createStatement();
      ResultSet rs4 = st4.executeQuery(sql4);
      HashMap hm2 = new HashMap();
      while (rs4.next()){
        hm2.put(rs4.getString("klient").trim(), new Double(rs4.getDouble("aastamyyk")));
      }
      rs4.close();
      st4.close();

      StringBuffer sb = new StringBuffer();
      String sep = ";";
      while(rs.next()){
        String cliID = rs.getString("klient").trim();
        sb.append(cliID + sep);
        sb.append(rs.getString("kuupaev").trim() + sep);
        sb.append(rs.getDouble("summa") + sep);
        sb.append(rs.getInt("ridu") + sep);
        String tekst = ensureLen(rs.getString("tekst")  ," ");
        tekst = tekst.replace('\'', ' ').replace('\"', ' ');

        sb.append(tekst + sep);

        boolean has = scrollTo(cliID, rs2);
        if (has){
          Double aastamyyk = (Double)hm2.get(cliID);
          double am = 0;
          if (aastamyyk != null) am = aastamyyk.doubleValue();
          sb.append(am + sep);

          Double kuumyyk = (Double)hm.get(cliID);
          double km = 0;
          if (kuumyyk != null) km = kuumyyk.doubleValue();

          sb.append(km + sep);
          sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
          sb.append(ensureLen(rs2.getString("kontaktisik"), " ") + sep);
          sb.append(ensureLen(rs2.getString("telefon"), " ") + sep);
          sb.append(ensureLen(rs2.getString("maksetin"), "0") + sep);
          sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
          sb.append("Dealer: " + rs.getString("dealer").trim() + sep);
        }

        sb.append("\n");
      }

            ManagerData md = new ManagerData();
            md.setClientVisitInfo(sb.toString());

            sql = "SELECT kood AS klient,objekt2 AS agent,y.nimetus AS agentname  from klient k inner join yksus y on y.yksus=objekt2 " +
				" where klassif =2  GROUP BY kood Order by kood";

    PreparedStatement ps = con.prepareStatement(sql);
   // ps.setDate(1, new Date(getStartingDate().getTime()));
    rs = ps.executeQuery();
    StringBuffer bf2 = new StringBuffer();
    while(rs.next()){
      bf2.append(rs.getString("klient").trim() + sep);
      String agent = rs.getString("agent").trim();
      bf2.append(agent + sep);
      String agentName = rs.getString("agentname");
      if (agentName != null) agentName = agentName.trim();
      if (agentName == null) agentName = " ";
      bf2.append(agentName + sep);
      String email = parent.parseFirstEmail(parent.getEmail(agent));
      if(email == null || email.trim().length()== 0 )email = " ";
      bf2.append(email + "\n");	
	
	
	
	
    }

    md.setClientAgentInfo(bf2.toString());



      return md;

    }


    public void insertDistribution(DistEvalListWrapper wr){
      
      }



    private String ensureLen(String str, String replace){
      if (str == null) str = "";
      else str = str.trim().replace('\'', ' ').replace('\"', ' ').replace(';',' ');
      if (str.length() == 0){
        return replace;
      } else {
        return str;
      }
    }

    private boolean scrollTo(String id, ResultSet rs) throws SQLException{
      rs.beforeFirst();
      while(rs.next()){
        if (id.equals(rs.getString("kood").trim())){
          return true;
        }
      }
      return false;
    }

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "VS";

    private String	       url = "jdbc:odbc:eeva";
    private String             url2 = "jdbc:odbc:report";
    private String             nimetu = "nimetus";
    private Connection	       con = null;
    private String             kordaja = "myygihind";
    private Statement	       stmt;
    private String	       subjectFilesTo = null;
    private TreeMap	       dealers = new TreeMap();




	@Override
	public void setBulkSales(Map agents) {
		// TODO Auto-generated method stub
		
	}
}



/*--- formatting done in "Sun Java Convention" style on 10-07-2002 ---*/

