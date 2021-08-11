/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.InventoryLine;
import eevaeksport.MatrixLine;
import eevaeksport.Product;

/**
 * Title:        MobileJava
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class ElfaDBDriver extends AbstractDBDriver implements BulkUpdater{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public ElfaDBDriver(EksportPanel parent) {
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
	private String getDir() {
		String dir = getParent().getProps().getProperty("readin");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}
    

	  public static String getYYYYpMMpDDpString(Date dd){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    cal.setTime(dd);
		    
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}

			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}
			sb.append(day );
			
			sb.append("." + month  + ".");
			sb.append("" + cal.get(Calendar.YEAR));
			
		    return sb.toString();
		  }

	
	 public TreeMap getSort() {
			TreeMap result = new TreeMap();
			File f = new File(this.getDir() + "Listingud.xls");
			try {
				FileInputStream ff = new FileInputStream(f);
				HSSFWorkbook workbook = new HSSFWorkbook((ff));
				HSSFSheet sheet = workbook.getSheet("tarnekliendid");
				HSSFRow row = null;

				// System.out.println(groups.toString());

				for (int i = 3; i < sheet.getLastRowNum(); i++) {
					row = sheet.getRow(i);
					String custID = "";
					//System.out.println(row);
					if (row != null && row.getCell((short) 1) != null) {

						try {
							custID = (int) (row.getCell((short) 1)
									.getNumericCellValue()) + "";
						} catch (Exception e) {
							custID = (row.getCell((short) 1).getStringCellValue());
						}
						
	                       if(!custID.equals("0")){
	                    // Siin l�heme reasisse rokkima ja vaatama kas se toode
						// l�heb kaasa
	                    		try {
				     	    	  String text =  row.getCell((short) 0).getStringCellValue() ;
					    		  result.put(custID.trim(),text);  
					    		} catch (Exception e) {
	                    		 e.printStackTrace();
	    						}
						
	                    		
						
					}
				}}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return result;

		}
	
    public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		File f = new File(this.getDir() + "Listingud.xls");
		try {
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("listingud");
			HSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(5);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 5; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			for (int i = 6; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";

				if (row.getCell((short) 0) != null) {

					try {
						prodID = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID = (row.getCell((short) 0).getStringCellValue());
					}


                       if(!prodID.equals("0")){
                    		
                       		
      				                  	
					// Siin l�heme reasisse rokkima ja vaatama kas se toode
					// l�heb kaasa
					for (int a = 5; a < row.getLastCellNum(); a++) {

						String group = (String) groups.get(a + "");
						// System.out.println(prodID + " " + group);
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

							if (go != null && go.equals("X")) {

								 System.out.println( a + " " + prodID + " " +
								 clGroup + " " + go);

								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(clGroup.trim());
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
			e.printStackTrace();
		}

		 System.exit(0);

		return result;

	}

    public String getSaveStockInfo(){
    return null;
    }
    public String getSaveDebtInfo() {
    	// System.out.println("xxxxxyyyy");
    	        File dir = new File("import");
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
    	        System.exit(0);
    	    	
    	    	return null;
    	       }

    
    
    private void insertStock(Vector vec){
    	if(vec != null){
    		
    		
    		int doknr = 0;
    		//1 dokumensi  numbri m��ramine
    		
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
    		
    		
    		Enumeration enums = vec.elements();
    		while(enums.hasMoreElements()){
    			InventoryLine lin = (InventoryLine)enums.nextElement();
    			String objekt = lin.getCustId();
    			String location = lin.getLocation();
    			double kogus  = lin.getAmount();
    			String ean = lin.getEan();
    			String prName = lin.getProdName();
    			String wrk = lin.getWorker();
    			String wrkname = lin.getWorkername();
    			String prodid = lin.getProdId();
    			
    			java.sql.Date entered = new java.sql.Date(lin.getEnterTime().getTime());
    			String tund = lin.getEnterTime().getHours()+"";
    			if (tund.length() == 1){
    				tund = "0" + tund;
    			}
    			String minut = lin.getEnterTime().getMinutes()+"";
				if (minut.length() == 1){
					minut = "0" + minut;
    			}
				
				
    			
    			
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

    
    
    
    
    
    public String getAssortmentStr(Agent agent){
    	return null;
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
    	//getSort();
        connectionUp();
        String  cmd = "SELECT yksus,nimetus FROM yksus WHERE klassif=2";
        TreeMap agents = new TreeMap();

        try {
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              //agent.setObject(rs.getString("objekt").trim());
              String name = rs.getString("nimetus").trim();
              agent.setName(name);
                agents.put(name, agent);
            }
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
            "SELECT k.kood, k.nimi,TRIM(k.tanav) AS aadress,' ' AS ale,k.riik AS visit,email  FROM klient k  WHERE liik='EHITUS' ORDER BY k.nimi " ;

        log(cmd);

        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        try {
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Customer cust = new Customer(rs.getString("kood").trim());
            String nimi = rs.getString("nimi").trim();
            cust.setName(nimi);

            String visit = rs.getString("visit");
            if (visit != null){
              cust.setVisit(visit.trim());
            }

            String aadress = rs.getString("aadress") ;
            if (aadress != null){
              cust.setAddress(aadress.trim());
            }
            cust.setDiscount(rs.getDouble("ale"));
            String email = rs.getString("email");
            if (email != null && email.trim().length()>0){
          //  cust.setConfirmEmail(email.trim());

            }
            
            Agent a = new Agent("KAUPO");
            cust.addAgent(a);

                customers.put(nimi, cust);
                custMap.put(cust.getPayer(), cust);
            }


            String sql = "SELECT d.saaja AS klient, SUM(d.tasumata) AS summa FROM klient k LEFT JOIN dok d ON (k.kood=d.saaja OR k.kood = d.klient) WHERE " +
               " date()>d.tasukuup and d.doktyyp='G' AND d.tasumata <> 0 AND len(saaja) > 0 GROUP BY k.kood, k.nimi ORDER BY k.nimi";
           ResultSet   rst = stmt.executeQuery(cmd);
            rst = stmt.executeQuery(sql);
            while(rst.next()){
              String id = rst.getString("klient").trim();
              double debt = Double.parseDouble(rst.getString("summa").trim());
              Customer cust = (Customer)custMap.get(id);
              if (cust != null){
                cust.setDebt(debt);
              }
            }
            rst.close();
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
        Hashtable dateOut = new Hashtable();

try {
     // String cmd = "SELECT toode, nimetus, yhik, myygihind FROM toode WHERE (len(trim(toode)) > 3) AND (trim(toode.grupp) = 'L')";
     String  cmd =" select t.toode , rtrim(left(hankija,3)) + ' ' +nimetus  AS nimi, yhik, liik,myygihind,kogus,ribakood as ean " +
     " from toode t left join hetkesei h on t.toode=h.toode and osak='EFD' " +
       "       where len(trim(t.toode))>2 and grupp ='L'  " ;

           if (isDebug()) {
           	System.out.println(cmd);
           	}


          stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery(cmd);

                      while (rs.next()) {
                          String kood = rs.getString("toode").trim();
                          String nimetus =  rs.getString("nimi");
                          Product prod = new Product(kood);

                          prod.setName(nimetus.trim());
                          prod.setPrice(rs.getDouble("myygihind"));
                          String grupp = rs.getString("liik");
                          prod.setUnit(rs.getString("yhik"));

                          if(grupp != null && grupp.trim().length()>0){
                          prod.setGroup(grupp.trim());}
                        //  prod.setInCases(rs.getDouble("tykke"));
                          String ean = rs.getString("ean");
                          if (ean != null && ean.trim().length()>0){
                            prod.setEAN(ean.trim());
                          }
                          
                        
                          
                          prod.setStock(rs.getDouble("kogus"));
                          if(prod.getEAN().trim().length()==0){
                          	 dateOut.put(kood.trim(),kood.trim() + "|" + nimetus.trim() + "|" + prod.getStock());
                          }else{
                          	dateOut.put(prod.getEAN(),kood.trim() + "|" + nimetus.trim() + "|" + prod.getStock());
                          }
                          
                          if (nimetus.startsWith("ELF") || nimetus.startsWith("NOR")){
                            nimetus = kood +  " " + nimetus;  
                            prod.setName(nimetus);
                              products.put(nimetus, prod);  	
                          }
                        

                      }

        } catch (Exception e) {
            getParent().showError(e);
        }
    	try {
			saveObject(dateOut,"tootedRev.has");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
    
	public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
    
    public TreeMap getProdGroups() throws SQLException {
    	  Connection con = DriverManager.getConnection(url);
     TreeMap   tm = new TreeMap();
     String    sql = "select l.liik,artlnimi as nimi from ../andmed/artliik l inner join toode t on " +
     " t.liik=l.liik   where l.grupp='L' and hankija in ('ELFASWE','NORDICF') " +
     " group by l.liik";
     Statement st = con.createStatement();
     ResultSet rs = st.executeQuery(sql);

     while (rs.next()) {
       String liik=rs.getString("liik").trim();
       String nimetus=rs.getString("nimi").trim();
       if (liik != ""){
       tm.put(liik,nimetus);
       }
     }
     rs.close();
     st.close();


     return tm;
 }




    public Vector getCustomerProductsList(String agentCode) {
        connectionUp();
     
        String cmd =" SELECT dok.klient,rid.toode,rid.kogus,dok.kuupaev FROM dok INNER " +
       " JOIN rid ON dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode INNER JOIN klient k ON  dok.klient=k.kood WHERE " +
       " len(trim(rid.toode)) > 2  " +
       " AND k.liik='EHITUS' AND dok.kuupaev >=date()-180 " +
       " AND  dok.doktyyp = 'G'  AND rid.doktyyp= 'G' GROUP BY " +
         " dok.klient, rid.toode ORDER BY dok.klient, toode.nimetus ";

        if (isDebug()) {
            System.out.println(cmd);
        }

        Vector custProds = new Vector();


        try {
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              String kliendikood = rs.getString("klient").trim();
              String tootekood = rs.getString("toode").trim();
              double kogus = Double.parseDouble(rs.getString("kogus").trim());
              String kuup = rs.getString("kuupaev");
              System.out.println("eeee" + kliendikood + " " + tootekood);
             CustProd cp = new CustProd(kliendikood, tootekood, kogus, 0, 0,
             		kuup.substring(5,kuup.length()), 0, 0,0," ");
              custProds.add(cp);

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
      String sql = "SELECT klient,tasudok AS number,tasukuup AS maksepaev,tasumata, kuupaev,(date()-tasukuup) AS yle FROM " +
                   "dok WHERE pais2obj = ? AND tasukuup<date()+10 AND doktyyp='G' AND tasumata<>0 " +
                   "ORDER BY klient";

     Connection con = DriverManager.getConnection(url);
      PreparedStatement st = con.prepareStatement(sql);
      st.setString(1, agentCode);
      ResultSet rs = st.executeQuery();
      while(rs.next()){
        Debt debt = new Debt();
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
                "select sum(doksumma) as seekuu  from dok where (month(kuupaev))=(month(date())) and doktyyp='G' AND year(date())=year(kuupaev) AND pais2obj=?";
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

        /*	sql = "SELECT MAX(tasukatse) as tasukatse, vahe FROM tasu WHERE myyk <= "
                      + seekuu;
                if (debug) {
                    System.out.println(sql);
                }

                rs = stmt.executeQuery(sql);

                if (rs.next()) {
                    double tasukatse = rs.getDouble("tasukatse");
                    double vahe = rs.getDouble("vahe");

                    if (ajut) {
                        salary = tasukatse+ bonus;
                    } else {
                        salary = tasukatse + vahe;
                    }
                    salary = salary*koef/100;
                }

                rs.close();
      */
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


    public String getNonMobileOrders(Agent agent){
      connectionUp();

     String sql = " SELECT tasudok AS arve,kuupaev,k.nimi,doksumma AS summa FROM dok d INNER JOIN klient k ON k.kood=d.klient " +
               " WHERE kalktoode!='MobileJava' AND kuupaev > date()-3 AND pais2obj=?";


      StringBuffer sb = new StringBuffer();

        try {
        	  Connection con = DriverManager.getConnection(url);
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


    public void setBulkSales(Map agents){

        String cmd =   "SELECT dok.pais2obj AS agent ,SUM(rid.hind*rid.kogus) AS sales FROM dok INNER JOIN rid ON " +
        "dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode WHERE " +
        "len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 2   AND " +
        "YEAR(dok.kuupaev)=YEAR(date()) AND MONTH(dok.kuupaev)=MONTH(date())  " +
        "AND dok.doktyyp = 'G' AND rid.doktyyp ='G'  GROUP BY dok.pais2obj ";
    	
    	            log(cmd);
    	          connectionUp();
        try {
            Statement stmt = con.createStatement();

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            ResultSet	      rs = stmt.executeQuery(cmd);
            while(rs.next()) {
              String agentID = rs.getString("agent");
              Agent agent = (Agent)agents.get(agentID);
              if (agent != null){
                agent.setMonthSales((int)rs.getDouble("sales"));
                //System.out.println(agent.toString() + " " + agent.getMonthSales());
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
     * @param s
     *
     * @see
     */
    public void log(String s) {
        getParent().log(s);
    }

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "Elfa";
    private String	       url = "jdbc:odbc:eeva";
    private Connection	       con = null;
    private Statement	       stmt;
    private String	       subjectFilesTo = null;




}



