/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.EksportFrame;
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
 * @author Tarmo Tubro
 * @version 1.0
 */
public class MoxLihaDBDriver extends AbstractDBDriver implements Manager,Promo, BulkUpdater,Discounter{



    public MoxLihaDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
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
      

    public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		File f = new File(this.getExcelDir() + "sortimendid.xls");
		try {
			
        	
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheetAt(0);
			HSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(3);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 2; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

		//	 System.out.println("groups " + groups.toString());

			for (int i = 5; i <= sheet.getLastRowNum(); i++) {
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
					for (int a = 2; a < row.getLastCellNum(); a++) {

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

							if (go != null && ( go.toLowerCase().equals("x") || go.length() > 3)) {

							// System.out.println( a + " " + prodID + " " +
								// clGroup + " " + go);

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
								
								if(go.length()> 3){
									mline.setLineinfo("Partner:" + go);
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

		// System.exit(0);

		return result;

	}
    private String notNull(String res){
    	if(res == null){
    		return "";
    	}else{
    		return res;
    	}
    }

  
    public Vector getPriceMatrixData() {
      Vector lines = new Vector();
      String uus =  "select  if(k.param2 not in ('MAKSJA'),k.code,' ') " +
 " as custid, if(k.param2  in ('MAKSJA'),k.id,' ') AS clgroup,t.code as prodid , ' ' AS prodgroup, " +
      "		         discount as discount, 0  AS price ,date_begin as algus , date_end as lopp , " +
        " if(disc.code in (1,10,6),1,0) as asi from front_discount disc " +
		"		 INNER JOIN front_product t ON t.id=disc.product_id  " +
		"			 INNER JOIN front_business k on k.id = disc.business_id " +
          "           where  date(sysdate()) between date_begin and date_end  ";

      try {
  	
      	Statement stmt = this.getNewCon().createStatement();
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
        				String asi =  rs.getString("asi").trim();
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
                      if(asi.equals("1"))  
                       lines.add(mline);
                }

        } catch (SQLException ex) {

          ex.printStackTrace();
        }


        return lines;
	//return null;
}



   public String getSaveDebtInfo() {
   	return null;
   }

   public String getProductSortingData(){
   	return null;
   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
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

        public String getSaveStockInfo(){
        	
        	System.exit(0);
        return null;
        }

		
        public String getEmail(String agentCode) {
            Properties props = EksportFrame.loadProps("emails.txt");
          //  System.out.println(props);
            String     email = props.getProperty(agentCode.trim());
            //System.out.println(email + agentCode);
            if (email == null) {
                email = "";
            }

            return email;
        }			
            	
        public String parseFirstEmail(String emails) {
            String		email = "";
            StringTokenizer st =
                new StringTokenizer(emails,";");

            if (st.hasMoreTokens()) {
                email = st.nextToken();
            }

            return email;
        }
    /**
     *
     */
    public void connectionUp(){
    }

    private Connection getNewCon() throws SQLException{
        return DriverManager.getConnection(url, "root", "er0te");
    }
  

    /**
     *
     */
    public void connectionDown(){
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */

    public TreeMap getProdGroups() throws SQLException {
        Connection con = getNewCon();
      TreeMap   tm = new TreeMap();
      String    sql = "select id as liik , name as nimetus from front_productgroup " ;

      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);

      while (rs.next()) {
        String liik = rs.getString("liik").trim();
        String nimi = liik + " " + rs.getString("nimetus").trim();
          tm.put(liik,
                 Converter.enCodeEst(nimi));
      }
      rs.close();
      st.close();
      con.close();
      return tm;
      }


    public TreeMap getProdGroupsSecond() throws SQLException {

        return null;
        }

        public String getDistribution() {
          return null;
        }

    public TreeMap getAgents() {
      String  cmd = "select code as agent , name as nimetus from front_person ";
     // getFixedAssortmentData();
      TreeMap agents = new TreeMap();

      
	    Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
      
        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);




            while (rs.next()) {
              Agent agent = new Agent(rs.getString("agent").trim());
           
              	String nimi = rs.getString("nimetus").trim();
              	agent.setName(nimi);

              	String dubbler = "";//rs.getString("dubbler");
              if(dubbler != null && dubbler.trim().length()>0){
              	StringTokenizer st = new StringTokenizer(dubbler,",");
              	while(st.hasMoreElements()){
              		String ag = st.nextToken().trim();
              		//System.out.println(ag);
              	
              		if(!agent.getID().equals(ag)){
                  		agent.addDubbler(ag);
                  		
                  	//	System.out.println("dubbler added " + agent.getID() + " dub. " + ag);
              		}
              	}
              }
              
              
              if(agent.getName() != null){
            	  agent.setLanguage("EST");
                  agent.setCountry("ee");
                  agent.setPassWord("0000");
                  String email = " ";
                  
                  if(propse.get(agent.getID()) != null){
                	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
                  }
                  if(email.length() > 3){
                	  agent.setEmail(email);
                  }
                  if(email.length() > 0){
                	  agent.setActive(true);
                  }
              	agents.put(agent.getName(), agent);
                aHash.put(agent.getID(), agent);

              }
                }


            rs.close();
            stmt.close();
            con.close();

        } catch (Exception e) {
            getParent().showError(e);
        }

        Agent aa = new Agent("HOUSE");
        aa.setName("Kontor");
        agents.put(aa.getName(), aa);
        aHash.put(aa.getID(), aa);
       
	       return agents;
	    }


  
    
	    public TreeMap getCustomers(Agent agent){
	      String cmd = "SELECT k.code AS kood, k.name AS nimi,comment as aadress, f.duedays as mt,f.credit_limit as lim, '' AS isik, " +
	      		" k.phone  As per,  k.email as email ,datafield4 as visit, " +
	            "        p.code as agent,param1 as firmatyyp,param3 as kauplusekett, days_of_order as tarne ,m.company_id as maksja, k.param6 as tyyp2 " +
			"	from front_business k  left join front_company f  on f.business_id = k.id " +
			" inner join front_person p on p.id=k.person_id " +
		    "	left join front_subunit m on m.business_id=k.id "; //where k.status ='Aktiivne'
	      log(cmd);
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        ketid = new HashMap();
        sortimendid = new HashMap();
        this.custprodlist = this.getCustomerProductsList2(null);
        
        File fff = new File(this.getExcelDir() + "sortimendid.xls" );
        TreeMap groups = new TreeMap(); 
    	try{
            FileInputStream ff = new FileInputStream(fff);
    		HSSFWorkbook workbook = new HSSFWorkbook((ff));
    		HSSFSheet sheet = workbook.getSheetAt(1);
    		HSSFRow row = null;
    		
    		row = sheet.getRow(0);
    	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
    		
    		System.out.println("rownum " + row.getLastCellNum());
    		
    		
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
	    	    			  	 groups.put(kood,sort);
    	    			  
    	    				
    				}	   
    				}
   			    }catch (Exception e){
    			//	System.out.println(row.getCell((short)a).getNumericCellValue());
    			}
    		}
    		
    		
    
    	 }catch (Exception e){
    		//e.printStackTrace();
    	}
        
        
    	 
        
    //	 System.out.println("groups to string " + groups.toString());
        
 	
		
        try {
          Connection con = getNewCon();

          Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);
            int counter = 0;
            while (rs.next()) {
              counter++;

                Customer cust = new Customer(rs.getString("kood").trim());
                String nimi = rs.getString("nimi").trim();
                if(nimi.startsWith("AS ") || nimi.startsWith("O� ") ){
                	nimi = nimi.substring(3,nimi.length());
                }
                cust.setName(Converter.enCodeEst(nimi));
                String add = rs.getString("aadress");
                if (add != null){
                  cust.setAddress(Converter.adressLink(Converter.enCodeEst(add.trim()),"Estonia"));
                  cust.restStreet = add.trim();
                  cust.setCountry("Eesti");
                  
                } else cust.setAddress(" ");
 

                String mt = rs.getString("mt");
                if (mt != null && mt.trim().length()>0){
                    cust.setPaymTerm(mt.trim());
                        }

                String isik = rs.getString("isik");
                if (isik != null && isik.trim().length()>0){
                    cust.setPersonalData(isik.trim());
                        }

                String per = rs.getString("per");
                if (per != null && per.trim().length()>0){
                    cust.setPersonalData((cust.getPersonalData() + " " + per.trim()).trim());
                        }

                String visit =  rs.getString("visit");
                    if (visit != null && visit.trim().length() != 0){
                cust.setVisit(visit.trim());
                    } else {
                    cust.setVisit(" ");
                  }

                    String ag = rs.getString("agent");
                    
                    String tyyp = rs.getString("firmatyyp");
                    if(tyyp != null  && tyyp.trim().length() > 0){
                    
                    if(tyyp.trim().equals("HORECA")){
                    	if(ag!= null && ag.trim().length()>0){
                    		if(ag.trim().length()<3){
                    		//	ag = ag + "  ";
                    		}
                    		//cust.setName(ag.substring(0,3) + " " + cust.getName());	
                    	}              	
                      }                
                    }
                    
                    
                    
                    
                  
				if(ag!= null && ag.trim().length()>0){
				   cust.addAgent(new Agent(ag.trim()));
					 Agent aa = (Agent)aHash.get(ag.trim());
		                if (aa != null){
		                	if(aa.getDubbler()!= null && aa.getDubbler().size()>0){
		                		for(int i = 0;i<aa.getDubbler().size();i++){
		                			cust.addAgent(new Agent((String)aa.getDubbler().elementAt(i)));	
		                		}
		                	}
		                }
				}
               
               
                
    
        
                String kett = rs.getString("kauplusekett");
                if(kett != null  && kett.trim().length() > 2 ){
                	cust.setGroup(kett.trim());
                	if(ketid.get(kett.trim()) != null){
                		Vector vv = (Vector)ketid.get(kett);
                		vv.add(cust.getID());
                		ketid.put(kett.trim(), vv);
                	}else{
                		Vector vv = new Vector();
                		vv.add(cust.getID());
                		ketid.put(kett.trim(), vv);
                	}
                	cust.setChain(kett.trim());
                }
                String grp = (String)groups.get(cust.getID());
                if(grp != null){
                //	System.out.println("Siin on grupp olemas "  + grp + " " + cust.getID() );
                	Vector vv = new Vector();
                	StringTokenizer st = new StringTokenizer(grp,"|");
                	while (st.hasMoreElements()){
                		String gg = st.nextToken();
                		vv.add(gg);
                		sortimendid.put(cust.getID(), gg);
                	}
                	cust.setFixSortCode(vv);
                	
                	
                }
                
                String tarne = rs.getString("tarne");
                if(tarne != null  && tarne.trim().length() > 0 ){
                	cust.setCity("tarnep�ev:  " + tarne.trim() );
                	
                }
                String maksja = rs.getString("maksja");
                if(maksja != null  && maksja.trim().length() > 0 ){
                	cust.setPayer(maksja.trim());
                	cust.setGroup(maksja.trim());
                }
                String tyyp2 = rs.getString("tyyp2");
                if(tyyp2 != null  && !tyyp2.trim().equals("HORECA")
                		 && !tyyp2.trim().equals("RETAIL")
                		 && !tyyp2.trim().equals("INDUSTRY")
                		 && !tyyp2.trim().equals("HORECA")){
                	 cust.addAgent(new Agent("HOUSE"));
                	
                }
                if(tyyp2 != null ){
                	cust.category = tyyp2.trim();
                }
                
                customers.put(cust.getName(), cust);
                custMap.put(cust.getPayer(), cust);

            }
            rs.close();

            String sql = "SELECT max(Kuup�ev) as dateke,max([Tasumise kuup�ev]) as ma,  a.[Maksja/Saaja] AS klient,a.number,(a.kuup�ev+a.[Viivise valem]) " + 
                 "  AS kuupaev,a.[MD Summa] AS summa,SUM(tas.[Tasutud Summa])AS tasutud " +
                 "  FROM TVMDKP a LEFT JOIN TVMDKK tas on tas.[Keha pvoti]=a.[Keha voti]  WHERE " +
                 "  a.[MD summa]<>0 AND a.kuup�ev>date()-300 AND (a.kuup�ev+a.[Viivise valem])< date() " +
                 "  GROUP BY a.number,a.[Maksja/Saaja],a.[MD Summa],(a.kuup�ev+a.[Viivise valem]) " +
                 "   having 1< (a.[MD Summa] -SUM(tas.[Tasutud Summa])) or max(str([Keha pvoti])) is null " +
                 "  ORDER BY a.[Maksja/Saaja] ";


         /*   rs = stmt.executeQuery(sql);
            while(rs.next()){
            	 java.sql.Date ddd= rs.getDate("dateke");
            	 
              String id = rs.getString("klient").trim();
              double debt = Double.parseDouble(rs.getString("summa").trim());
              double paid = 0;//
              try {
                paid = Double.parseDouble(rs.getString("tasutud").trim());             
              }
              catch (Exception e) {}
              Customer cust = (Customer) custMap.get(id);
              if (cust != null &&   (debt-paid  < 1 || -1 < debt-paid) && sendDebt(cust.getPayer())) {
               
                cust.setDebt(cust.getDebt() + debt - paid);
              }
            }
            rs.close();*/
            stmt.close();
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }

        return customers;
    }
 



    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      return list;
    }

    
    
    public TreeMap getProducts(Agent agent){    	   
    	    TreeMap products = new TreeMap();
		    prodHash = new HashMap();
		 
    	    try{
    	    	String cmd = "select t.code as toode , t.name as nimi,productunit as yhik ,t.barcode as EAN,t.keeping as maxdays, h.saleprice as hind, " +
" t.group_id as grupp, t.producttype as type, h.date_begin, h.date_end from front_product t inner join " +
" ( select * from front_saleprice sp join ( Select tid, min(pikkus) as pikkus from ( select t.id as tid, t.code as toode,  " +
" h.date_begin, h.date_end, h.date_end-h.date_begin as pikkus, h.id from front_product t inner join front_saleprice h on " +
"  h.product_id=t.id and h.pricing_id = 1 and h.date_begin < sysdate())as tmp group by toode) " +
" as vahetab on tid=sp.product_id  where sp.date_end-sp.date_begin=pikkus " +
" ) as h on h.product_id=t.id and h.pricing_id = 1 and h.date_begin < sysdate()  where status = 1";
    	    	  
    	    	  Connection con = getNewCon();
    	          Statement st = con.createStatement();
    	            ResultSet	      rs = st.executeQuery(cmd);
    	        
    	            while (rs.next()) {
    	            	 String toode = (rs.getString("toode").trim());
  	            	     String name =  (rs.getString("nimi"));	            	
    	            	 String unit = (rs.getString("yhik"));
    	            	 String ean = (rs.getString("ean"));
    	            	 double hind =  rs.getDouble("hind");
    	            	 String maxdays = (rs.getString("maxdays"));
    	            	 String grupp2 = rs.getString("grupp");
    	            	 double type = rs.getDouble("type");
    	            	 Product prod = new Product(toode);
    	            
    	            	 if(grupp2 != null){
       	            	  prod.setGroup(grupp2.trim());    	            		 
    	            	 }
    	            	 

 			        	 if(maxdays != null && maxdays.trim().length()>0 && !maxdays.trim().equals("0") ){
     			        	prod.setPartii("MaxDays " + maxdays.trim());
     			        }
 			        	 String add = "";
 			        	 if(usedprods.contains(toode)){
 			        		 add = " AKT";
 			        	 }
 			       	 String tyyp = "";
 			    	String namee =  name.trim() + add;
   			        	if(type > 0 ){
   			        		prod.setType((int)type + "");
   			        		tyyp = " T";
   			        	}
   			        
     			        prod.setName( Converter.enCodeEst(name.trim() + add));

     			        prod.setPrice(hind);

     			        if(unit != null && unit.trim().length()>0){
     			        	prod.setUnit(unit.trim());
        			        }

     			        if(ean != null && ean.trim().length()>0){
     			        	prod.setEAN(ean.trim());
        			        }

     			       products.put(namee, prod);
	                   prodHash.put(prod.getID(), prod);
 			        	  
    	            }

    	} catch(Exception e) {
    	e.printStackTrace();
    	}
    	System.out.println("tooted suurus " + products.size());
        return products;
    }



    public DealerProduct getDealerProductsList() {
    	return null;
        }

    public Vector getCustomerProductsList(String agentCode) {
    	return custprodlist;
    }
    public Vector getCustomerProductsList2(String agentCode) {

      String cmd = "select b_pro.code as toode,k.code as klient ,sr.qty AS kogus,fs.docdate as lastdate " +
" ,(case when fs.docdate+b_pro.keeping > date(sysdate()) then 0 else 1 end ) as yle " +
" from front_sale fs " +
" INNER JOIN front_salerow as sr on sr.sale_id=fs.id " +
" Inner Join front_product as b_pro On sr.product_id = b_pro.id " +
" INNER JOIN front_business k  on k.id = fs.business_id__re where docdate > (curdate() - interval 30 day) " +
" GROUP BY b_pro.code,k.code,sr.qty,fs.docdate,b_pro.keeping " +
" ORDER BY b_pro.code, k.code,fs.docdate";

        Vector vec = new Vector();
        HashMap kliendid = new HashMap();
        try {
       System.out.println(cmd);
      //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"
       Connection con = getNewCon();
       Statement stmt = con.createStatement();
       ResultSet rs = stmt.executeQuery(cmd);
       while(rs.next()){

                String custID = rs.getString("klient").trim();

                    String prodID = rs.getString("toode").trim();;
               
                    if(!usedprods.contains(prodID)){
                    	usedprods.addElement(prodID);
                    }
                    
                    double amount = 0;
                    String lastdate = " ";
                    amount = rs.getDouble("kogus");
                    lastdate = rs.getString("lastdate").trim().substring(5,10);
                    int yle = rs.getInt("yle");
                       Customer cust = (Customer)kliendid.get(custID);
                       if (cust == null){
                         cust = new Customer(custID);
                       }
                       Product prod = new Product(prodID);
                       CustProd cp = new CustProd(custID, prodID,amount , 0, 0,lastdate,0,0,0," ");
                       cp.setRedline(yle+"");

                    cust.addProduct(prod,cp,false);
                    kliendid.put(custID, cust);
                    vec.addElement(cp);

              }
            rs.close();
            con.close();


              int counter = 0;
              int cou = 0;
             String oldklient = null;
             String olddate = " ";
             String oldtoode = null;

             String cmd2 = " SELECT b_pro.code as toode,f_bu_re.code as klient,sr.qty AS kogus,fs.docdate as lastdate  "+
" FROM front_sale fs "+
            	 " inner join front_salerow as sr on (fs.id=sr.sale_id) "+
" inner join front_business as f_bu_re on ( fs.business_id__re=f_bu_re.id) "+
" Inner Join front_product as b_pro On sr.product_id = b_pro.id "+
" where docdate>curdate() - interval 30 day "+
" ORDER BY b_pro.code, f_bu_re.code,fs.docdate      desc";


             System.out.println(cmd2);
   		   //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"
   		    Connection con2 = getNewCon();
   		    Statement stmt2 = con2.createStatement();
   		    ResultSet rs2 = stmt2.executeQuery(cmd2);


     while (rs2.next()){
    	 String toode = rs2.getString("toode").trim();
          String klient = rs2.getString("klient").trim();
                 
                  String kogus = "0";
                  String date = " ";
                     kogus = (rs2.getDouble("kogus")+0.99)+"";
                     kogus = kogus.replace('.',',');
   				  if(kogus.indexOf(",") != -1){
   				  	kogus=kogus.substring(0,kogus.indexOf(","));
   				  }
                      date = rs2.getString("lastdate").trim().substring(0,10);

                  if(cou == 0){oldklient = klient;olddate = date;oldtoode=toode;}
                  if(!oldklient.equals(klient)){
                                counter = 0;
                                oldklient = klient;
                                olddate = date;
                  }
                  if(!toode.equals(oldtoode)){
                  	counter = 0;
                  }
                  Customer c = (Customer)kliendid.get(klient);
                  if (c != null ){

                    ArrayList al = (ArrayList)c.getProducts().get(toode);
                    if (al != null){
                      CustProd cp = (CustProd)al.get(1);
                      if (cp != null){

                   
                        if (counter == 0){cp.setS1(kogus );}
                        if (counter == 1){cp.setS2(kogus );}
                        if (counter == 2){cp.setS3(kogus );}
                        if (counter == 3){cp.setS4(kogus );}
                        if (counter == 4){cp.setS5(kogus );}
                        if(!olddate.equals(date)){counter++;}
                        vec.add(cp);
                      }
                    }
                    oldklient = klient;
                    olddate = date;
                    oldtoode = toode;
                  }
                  cou++;
                }


     rs2.close();
     con2.close();

        } catch (Exception e){
          e.printStackTrace();
        }
        return vec;
      }


    public String getPilotDayData(Agent agent){
      return null;
    }


    public String getNonMobileOrders(Agent agent){
      return null;
    }

    
    public void setBulkSales(Map agents){

      String cmd = " SELECT p.code AS agent , SUM(sumwotax) AS summa  from front_saleorder d INNER JOIN front_person p on p.id = d.agent_id " +
	"	WHERE month(docdate)= month(date(sysdate())) " +
     "  AND year(docdate)= year(sysdate())  GROUP BY  p.code ORDER BY  p.code " ;


      log(cmd);

        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            ResultSet	      rs = stmt.executeQuery(cmd);
            while(rs.next()) {
              String agentID = rs.getString("agent").trim();
              Agent agent = (Agent)agents.get(agentID);
              if (agent != null){
                agent.setMonthSales((int)rs.getDouble("summa"));
              //  System.out.println("bulksales " + agentID + agent.getMonthSales());
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
     * @param agentCode
     *
     * @return
     *
     * @see
     */
    public double getSalary(String agentCode) {
            return 0;
    }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
    }


    public ManagerData getManagerData() throws SQLException{
          return null;
    }


        public String getDriverVersion() {
        return DRIVER_VERSION;
    }
    
    
    
private String getText(String xx,Vector reason,boolean text){
	Enumeration enums = reason.elements();
	while(enums.hasMoreElements()){
		String yy =  (String)enums.nextElement();
		if(text){
		  if(xx.indexOf(yy)!=-1){
			return xx.substring(0,yy.length());
		  }	
		}else{
			 if(xx.indexOf(yy)!=-1 && xx.length()>yy.length()){
				return xx.substring(yy.length(), xx.length());
			  }		
		}
	}
	return "";
}
    
    public String getSubjectFilesTo() {
        return this.subjectFilesTo;
    }

    public void log(String s) {
        getParent().log(s);
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


       public TreeMap getDealers() {
           return dealers;
       }

       public void setDealers(TreeMap aDealers) {
           dealers = aDealers;
    }
  
       public static final String DRIVER_VERSION = "MoxLiha";
    private String url = "jdbc:odbc:moxuus";
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
    private  HashMap prodHash = null;
    private TreeMap	       dealers = new TreeMap();
    private Vector custprodlist = new Vector();
    private Vector usedprods = new Vector();
    private HashMap ketid = null;
    private HashMap sortimendid = null;
    private     HashMap aHash = new HashMap();


	@Override
	public String getStartingTime() {
		// TODO Auto-generated method stub
		return null;
	}

}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

