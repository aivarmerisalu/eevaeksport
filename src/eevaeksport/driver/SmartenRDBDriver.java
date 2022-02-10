 package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
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

public class SmartenRDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter, FixLoader {
  public SmartenRDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

      partnercode =  getParent().getProps().getProperty("partnercode");
     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      stmt = con.createStatement();
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
  private TreeMap eanprods = null;
  
  
  @Override
  public void FixLoader() {
  	
  eanprods =  Converter.loadMore();
  	  
  	  if(eanprods == null) {
  		  eanprods = new TreeMap();
  	  }
  	this.getProducts(null);
  	TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
  //	System.out.println("Contracts " + contracts.toString());
  	Converter.sendContractJson(contracts ,  rcompany,  user ,  pass);
  	System.exit(0);
  }
  
  
  /**
   *
   */
  public void connectionUp(){
  }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

  public String getSaveDebtInfo() {
   	return null;
   }

  
 private StringBuffer oorders = null;
 private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
 
  private TreeMap  getOpenOrders(){
		TreeMap res = new TreeMap();
		oorders = new StringBuffer();
		oorders.append(Calendar.getInstance().getTimeInMillis() + "\n");
		

		
		   String sql2 =  " select OrigTellNr AS id, '' as tyyp, KliendiKood AS kkood ,  OrigTellNr as number ,kuupaev AS kuup , 0 as summa  from vw_MJ_AvatudMyygid\r\n" + 
		   		" where tarnekuupaev > getdate()-1 and Origtellnr not like 'RU%' and Origtellnr !='' and partner ='"
				   + this.partnercode + "' group by OrigTellNr , KliendiKood, kuupaev" ;
		
	
	 this.connectionUp();
	try {
	   nf.setMinimumFractionDigits(2);
	   Connection con = this.getNewConnection();
	   PreparedStatement stmt2 = con.prepareStatement(sql2);
	   ResultSet  rs = stmt2.executeQuery();
	   StringBuffer sb = new StringBuffer();

	while(rs.next()){
		
		String id = rs.getString("id");
		String text = rs.getString("tyyp").trim();
		String nimi = rs.getString("kkood");
	    
		if (nimi == null){
	    nimi = " ";
	    } else {nimi = nimi.trim();}

		//DPP00488444 (‘Pabeigts’ – 01.06.2018; merisoft).
	    sb.append(rs.getString("number").trim() + "  ");
	    String kuup =(rs.getString("kuup").trim().substring(0,10)) ;
		sb.append((kuup) + " " + text + " ");
		
  	 sb.append("|" );	
		
  	 String summa = nf.format(rs.getDouble("summa"));
  	 
  	oorders.append(nimi + ";" + summa + ";" + kuup + "\n" );
	    String sql3 = "  select KogusMY as amount,  YhikMY as unit  , TooteNimetus as name from vw_MJ_AvatudMyygid " + 
	    		" where tarnekuupaev > getdate()-1 and partner ='AFRU' and OrigTellNr = '" + id + "'";
	 //   System.out.println("sql 3 " + sql3 );
	    Connection cc = this.getNewConnection();
	    PreparedStatement stmt3 = cc.prepareStatement(sql3);
	
		/*   CREATE TABLE [dbo].[OpenOrders](
	       [klkood] [nvarchar](20) NULL,
	       [kuupaev] [datetime] NULL, 
	       [tellimusenr] [nvarchar](50) NULL,
	       [toode] [nvarchar](50) NULL,
	       [kogus] [decimal](38, 20) NULL,
	       [price]  [decimal](38, 20) NULL)
*/

	       ResultSet  rs2 = stmt3.executeQuery();
	   
		    while(rs2.next()){
		    	sb.append("[" +nf.format(rs2.getDouble("amount"))+"] " 
		       + rs2.getString("unit").replaceAll("\\r\\n|\\r|\\n", " ").trim()+ " " + rs2.getString("name").toLowerCase().replaceAll("\\r\\n|\\r|\\n", " ").trim() + "|");
		    }
	   
		    rs2.close();
	        stmt3.close();
	        cc.close();
	        
	       if(res.get(nimi) != null){
	    	   res.put(nimi,res.get(nimi) +" | |" + sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
	   	     } else {
	    	   res.put(nimi, sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
	   	   }
	   	sb = new StringBuffer();
	       
	}
	
	
	rs.close();
	stmt2.close();
	con.close(); 	 
	
	} catch (Exception e) {
	  getParent().showError(e);
	}

  	connectionDown();
  	Converter.saveFile(parent.getProps().getProperty("savefilesdir")  + "customerorders.txt", oorders.toString());
		
		return res;
	}
	
  

	private String getDir() {
		String dir = "abifailid";
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}
	
	
	HashSet abicusts = null;
	
	private TreeMap getCustAgsExcel() {
		TreeMap asi = null;
		abicusts = new HashSet();
		
    File f = new File(this.getExcelDir() + "Partnerid.xls");
		
		try {
			
		
			// System.out.println("rownum exists" + f.exists() );
				
			
	 if(f.exists()){
		 
		 
			
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheetAt(0);
			HSSFRow row = null;
			
			
			
	
			for (int a = 0; a <= sheet.getLastRowNum(); a++) {
	           try {
	        	   row = sheet.getRow(a);
	        	   if(asi == null) {
	        		   asi = new TreeMap();
	        	   }
	        	  String custid = "";
	        	  try {
	        		  custid = (row.getCell((short) 0).getStringCellValue());
	        	  }catch(Exception e) {
	        		  custid = ((int)(row.getCell((short) 0).getNumericCellValue())+"");
	        		//  System.out.println("Jama koodiga " + custid + " " + a);
	        	  }
	        	  if(!abicusts.contains(custid))
	        	  abicusts.add(custid);
	        	  
	        	  String agent =  Converter.cleanLitLatEst(row.getCell((short) 13).getStringCellValue());
		        	StringTokenizer st = new StringTokenizer(agent,";");
		        	String uu = "";
		        	while (st.hasMoreTokens()){
		        		
		        		String aa = st.nextToken();
		        		if(aa.indexOf(":") != -1) {
		        			aa = aa.substring(0,aa.indexOf(":"));
		        			uu = uu + ":" + aa;
		        		}else {
		        			uu = uu + ":" + aa;
		        		}
		        		
		        		asi.put(custid, uu);
		        		asi.put("0" + custid, uu);
		        		
		        	}
		        	
		        	try {
		        		  String phone =  Converter.cleanLitLatEst(row.getCell((short) 5).getStringCellValue());
		        		  if(phone != null && phone.length() >0) {
		        			  asi.put(custid+"phone", phone);
		        			  asi.put("0"+custid+"phone", phone);
		        			//  System.out.println("PH "+ phone);
		        		  }
		        	}catch(Exception e) {
		        		
		        	}
		        	
		        	try {
		        		String email =  Converter.cleanLitLatEst(row.getCell((short) 7).getStringCellValue());
		        		  if(email != null && email.length() >0) {
		        			  asi.put(custid+"mail", email);
		        			  asi.put("0"+custid+"mail", email);
		        			  //System.out.println("EM "+ email);
		        		  }
		        	}catch(Exception e) {
		        		
		        	}
		        	try {
		        		 String name =  Converter.cleanLitLatEst(row.getCell((short) 17).getStringCellValue());
		        		  if(name != null && name.length() >0) {
		        			  asi.put(custid+"name", name);
		        			  asi.put("0"+custid+"name", name);
		        		  }
		        	}catch(Exception e) {
		        		
		        	} 
		        	  
		        	  
		        	  
		        	 
	        	  
				}catch(Exception k){
					k.printStackTrace();
				}
			}
	    }

		}catch(Exception e) {
		e.printStackTrace();	
		}		
		System.out.println("abicusts " + abicusts.size());
		return asi ;
	}
  
   public Vector getFixedAssortmentData() {
	   Vector result = new Vector();
		File f = new File(this.getDir() + "sortiment.xls");
		TreeMap groups = new TreeMap();
		try {
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			for (int d = 0 ; d < 4; d++){
			
			HSSFSheet sheet =  null ;
			try{
				sheet = workbook.getSheetAt(d);	
			}catch(Exception e ){
				
			}
			
			if(sheet != null){
				
			
			HSSFRow row = null;
		
			row = sheet.getRow(4);
			// 4 reas on kaupluste koodid 

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 6; a < row.getLastCellNum(); a++) {
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

				if (row.getCell((short) 1) != null) {

					try {
						prodID = (int) (row.getCell((short) 1)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID = (row.getCell((short) 1).getStringCellValue());
					}


                      if(!prodID.equals("0")){
                   		
                      		
     				                  	
					// Siin l�heme reasisse rokkima ja vaatama kas se toode
					// l�heb kaasa
					for (int a = 6; a < row.getLastCellNum(); a++) {

						String group = (String) groups.get(a + "");
						// System.out.println(prodID + " " + group);
						if (group != null && group.trim().length() > 0) {

							String custID = " ";
							String clGroup = " ";
							String prodGroup = " ";

							String go = null;
							custID = group.trim().substring(1);
							custID = custID.substring(0,custID.length()-1);
					    	//System.out.println(custID);

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

							if (go != null && go.toLowerCase().equals("x")) {

							//	 System.out.println( a + " " + prodID + " " + custID + " " +
							//	 clGroup + " " + go);

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
			}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null ;
		}

		return result;

   }

  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
         String sql = "SELECT  MaksjaKood as custid , Kliendikood as klient,  '' AS clgroup,  Tootekood AS prodid,  " +
			" ArtikliKlass AS prodgroup,PysiAlePct AS discount, Hind AS price ,KuupaevAlg as algus , KuupaevLopp as lopp  , month(KuupaevLopp-KuupaevAlg) as kamp"
			+ " FROM vw_MJ_hinnad where Partner='" + partnercode + "'";

          try {


        	Statement stmt = this.getNewConnection().createStatement();
                  ResultSet rs = stmt.executeQuery(sql);

                  while (rs.next()) {
                          String custID = rs.getString("custid");
                          String klient = rs.getString("klient");
                          String clGroup = rs.getString("clgroup");
                          String prodID = rs.getString("prodid");
                          String prodGroup = rs.getString("prodgroup");
                          double discount = rs.getDouble("discount");
                          double price = rs.getDouble("price");
                          String algus = rs.getString("algus").substring(0,10);
                          String lopp = rs.getString("lopp").substring(0,10);
                          int kuu = rs.getInt("kamp");
                          MatrixLine mline = new MatrixLine();
                          
                          boolean iscamp = false;
                          if(kuu <= 3 && !lopp.contains("1900")) {
                       	   iscamp = true;
                          }
                          
                         
                          if (klient != null && klient.trim().length() > 0) {
                              mline.setCustID(klient.trim());
                      }
                          if (custID != null && custID.trim().length() > 0) {
                                  mline.setCliGroup(custID.trim());
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
                          
                          
                          
                            	
                              if (algus  != null && algus.trim().length() > 0 && algus.indexOf("1900")== -1) {
                                     mline.setStartDate(algus.trim());
                             }

                             if (iscamp && lopp  != null && lopp.trim().length() > 0  && lopp.indexOf("1900")== -1) {
                                     mline.setEndDate(lopp.trim());
                             }
                             
                             if(kuu > 3 ){
                            	 mline.setProdLevel("pr");
                             }
                             
                             
                          
                        
                          lines.add(mline);
                  }
                  rs.close();

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
    	  
    	   getOpenOrders();
    		
    	  String  cmd = " SELECT TooteKood,KogusVaba FROM vw_MJ_Laosaldod where Partner='" + partnercode + "'" ;

	       if (isDebug()) System.out.println(cmd);
	       StringBuffer products = new StringBuffer();
	       try {
               Connection ccc =  this.getNewConnection();
	    	   Statement stmt = ccc.createStatement();
	           ResultSet rs = stmt.executeQuery(cmd);

	         while (rs.next()) {
	           String kood = rs.getString("TooteKood").trim();
	           double seis = rs.getDouble("KogusVaba");
	           if(seis > 0){
	               products.append(kood +  ";");
	               products.append(" ;");
	               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
	             }
	           }
	           rs.close();
	           stmt.close();
	           
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
    return null ;
     }

  
  TreeMap grupid = null;
  TreeMap eanGrupp = null;
  
  private TreeMap agent1 = new TreeMap();
  
  public TreeMap getAgents(){
    String cmd = "select Myygimees as kood , Nimi AS name  from vw_MJ_agendid  where Partner='" + partnercode + "' ";
    if (isDebug()) System.out.println(cmd);
 //   getFixedAssortmentData();
    StringBuffer sb2 = new StringBuffer();
    TreeMap agents = new TreeMap();
    eanGrupp = new TreeMap();
    grupid = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("name").trim();
          agent.setName(name);
          agent.setIsPilot(true);
          
          agent.setLanguage("EST");
          agent.setCountry("ee");
          agent.setPassWord("0000");
          String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
          if(email.length() > 1){
        	  agent.setEmail(email);
        	  agent.setActive(true);
          }
          agents.put(name, agent);
          agent1.put(agent.getID(), agent);
          
          sb2.append(agent.getID() + ";" + name + "\n");  
        }
    } catch (Exception e){
        getParent().showError(e);
      }
    Agent aa = new Agent ("HOUSE1");
    aa.setName("HOUSE1");
    agents.put("HOUSE1", aa);
    
    try {

    	FileReader fr = new FileReader(getDir() + "lisatooted.txt");
    	BufferedReader reader = new BufferedReader(fr);
    	String line = null;
    	String ean = null;
    	String grupp = null;
    	while ((line = reader.readLine()) != null) {
    		StringTokenizer st = new StringTokenizer(line, ";");

    		if (st.hasMoreTokens()) {
    			ean = st.nextToken().trim();
    		}
    		
    		if (st.hasMoreTokens()) {
    			grupp = st.nextToken().trim();
    		}
    		
    		try{
    			if(grupp.equals(" ")){
    				
    				eanGrupp.put(ean, "Muud");
    			}else{
    				eanGrupp.put(ean, grupp);
    				grupid.put(grupp, grupp);
    			}
    			
    			
    		}catch(Exception r){
    			
    		}
    		
    		
    	}
    	fr.close();
        
    	if(sb2.length() > 2){
        	try{
        		// Converter.saveFileInUTF8( "agent.txt", Converter.enCodeEst(sb2.toString()));
        	}catch (Exception e){
        		
        	}
          
         } 
    	
    }
    	catch (Exception e){
            getParent().showError(e);
          }
    return agents;
  }

  public Hashtable loadCustomerInfo() {

	    Hashtable result = null;
	    allcust = new Vector();
	    File f = new File("abifailid/" +  getParent().getProps().getProperty("custfilename"));
	   
	    if (f.exists()){
	      result = new Hashtable();
	      InputStreamReader is;
		try {
			is = new FileReader(f);
		
	      BufferedReader br = new BufferedReader(is);
	      String line = null;
	      while( ( line = br.readLine()) != null){
	      	String id = null;
	      	String name = null;
	      	String agent = null;
	      	String visit = null;
	        StringTokenizer st = new StringTokenizer(line, ";");
	        if (st.hasMoreTokens()){
	        	id = st.nextToken();
	        	Customer cc = new Customer(id);
	        	 if (st.hasMoreTokens()){
	               name = st.nextToken(); 
	               cc.setName(name);
	             }
	        	 
	       	 if (st.hasMoreTokens()){
		               agent = st.nextToken();
		               if(agent.trim().length() >0){
		            	   cc.setCity(agent.trim());
		            	   if(agent.trim().equals("ALL")){
		            		   allcust.add(id);   
		            	   }
		               }
		             
		               
		               
		        }	 
	    	 if (st.hasMoreTokens()){
		       	 st.nextToken();
	    		 
	    	 }

	       	 if (st.hasMoreTokens()){
		               visit = st.nextToken();
		               if(visit.trim().length() >0)
		               cc.setVisit(visit);
		        }
	        	 
	         result.put(id,cc);
	  	        
	      }
	    }
	      is.close();
	      br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    }
	    return result;
	  }
  
  HashMap custAgendid = null;
  
  public TreeMap getCustomers(Agent agent){
	  
	  try{
		  if(rcompanyx != null)
    	   Converter.getManagersReplacements(agent1, rcompanyx, user, pass); 
        } catch(Exception e){
        }
  	
	  TreeMap openOrders = this.getOpenOrders();
	  
	  String cmdo = "Select MaksjaKood AS payer ,  k.Staatus as blokk , cast(s.Saldo as int ) as credit FROM vw_MJ_kliendid k " +
              " LEFT JOIN vw_MJ_KliendiSaldo s on s.Kliendikood= k.Kliendikood or s.Kliendikood = MaksjaKood " ;

	  TreeMap maksjablokk = new TreeMap();
	   try {
  Connection con = this.getNewConnection();
	    	Statement stmt = con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmdo);
	  
	      while(rs.next()){
	    	  String abi = rs.getString("payer");
	    	  String block = rs.getString("blokk");
	          if (block != null && block.trim().equals("1")) {
	        	  maksjablokk.put(abi,block);
	          }
	      }
	      rs.close();
	      stmt.close();
	      con.close();
	   }catch(Exception e) {
		   e.printStackTrace();
	   }
	    	
	  
 String cmd = " SELECT (select Nimi from  vw_MJ_kliendid kk where kk.KliendiKood = k.MaksjaKood) as abi , Nimi + ' ' + Aadress as name,k.KliendiKood as accountnum,MaksjaKood AS payer ,   " +
               " Aadress  , Ring , Telefon  as info, k.Staatus as blokk , cast(s.Saldo as int ) as credit FROM vw_MJ_kliendid k " +
               " LEFT JOIN vw_MJ_KliendiSaldo s on s.Kliendikood= k.Kliendikood or s.Kliendikood = MaksjaKood " ;
 custAgendid = new HashMap();
 
	TreeMap custAgs = null;
	
	
	if ( this.partnercode.equals("AFRU") ||
			getParent().getProps().getProperty("partnercode").endsWith("VS")) { // royal unibrew
		custAgs = getCustAgsExcel();
		//System.out.println("custAgs " + custAgs.toString());
	}
 
  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
   
    Hashtable outside = loadCustomerInfo();
    
    try {
    	 Connection con = this.getNewConnection();
    	Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
  
      while(rs.next()){
    	  String abi = rs.getString("abi");
    	  
        String name = rs.getString("name").trim();
        if(abi != null && abi.trim().length() >0 && name.indexOf(abi.trim()) == -1){
        	name = abi + " " + name;
        }
        
        
        
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);

        name = name + " " + accountnum;
        cust.setName(Converter.enCodeEst(name));
        String payer = rs.getString("payer");
        if(payer == null || payer.trim().length()== 0){
        	cust.setPayer(cust.getID());
        }else{
        	cust.setPayer(payer.trim());
        }
        cust.setPayer(payer);
        
       

        String aadr = rs.getString("Aadress");
        if (aadr != null) {
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ')),"Estonia"));
         // System.out.println(aleke +aadr.replace('\n',' '));
        }
      
        
        
    
        
        
      String ring = rs.getString("ring");
      if (ring != null && ring.trim().length()>0) {
          cust.setCity("Veoring " + ring.trim());
        }else{
        	ring = " ";
        }
        
        String info = rs.getString("info");
        if (info != null && info.trim().length()>0) {
          cust.setPersonalData(info.trim());
        }
       
        String block = rs.getString("blokk");
        if (block != null && block.trim().equals("1")) {
          cust.setSalesBlock(true);
          cust.setDebt(1);
        }
        
        if(maksjablokk.get(cust.getPayer()) != null) {
        	cust.setSalesBlock(true);
        	cust.setDebt(1);
        }
        
        if(openOrders.get(cust.getID()) != null){
        //	System.out.println("lisan lahtist tellimust " + cust.getName());
      	   cust.addToCustDataVec(cust.getID() + ";tarasaldo; ;Open Order |"   +  openOrders.get(cust.getID()));
         }
        
        if(!cust.isSalesBlock()){
        	String  credit = rs.getString("credit");
            if (credit != null && !credit.trim().equals("0")) {
            //  cust.setCredit(credit + "");
            }else if (credit != null && credit.trim().equals("0")) {
            	//  cust.setCredit("5000");
            }	
        }
        if(allcust.contains(cust.getID())){
        	 Enumeration enums = getParent().getAgentsHash().elements();
             while (enums.hasMoreElements()) {
               Agent a = (Agent) enums.nextElement();
               cust.addAgent(a);
             }
        }
        
        cust.addAgent(new Agent("HOUSE1"));
        
        
        if ((getParent().getProps().getProperty("partnercode").endsWith("RU") || getParent().getProps().getProperty("partnercode").endsWith("VS"))
   			 && custAgs != null) {
   		// if(customer.getName().startsWith("P"))
   		 //System.out.println("acust " + customer.getID()+ " " + custAgs.get(customer.getID()));
   		 String infof = (String)custAgs.get(cust.getID()+"phone");
   		 String nam = (String)custAgs.get(cust.getID()+"name");
   		 String mail = (String)custAgs.get(cust.getID()+"mail");


   		 
   		 
   		 if (mail != null && mail.trim().length() > 0 ){
            	cust.setPersonalData(mail);
            	mail = " " + mail.trim();
            } else {
            	mail = "";
            }
            String pers  = nam;
            if (pers != null && pers.trim().length() > 0 ){
            		cust.setPersonalData(pers + mail);
            }
            
            if (info != null && info.trim().length()>2) {
            	if(info.trim().startsWith(",")){
            		info = info.trim().substring(1);
            	}
            	if(cust.getPersonalData() != null && cust.getPersonalData().trim().length() > 2) {
            	     cust.setPersonalData(cust.getPersonalData() +  "|Tel:" + info.trim().replaceAll(";", ",").replaceAll("\\r\\n|\\r|\\n", " "));
              }else {
            	     cust.setPersonalData( "Tel:" + info.trim().replaceAll(";", ",").replaceAll("\\r\\n|\\r|\\n", " "));
              }
             }
            
   		 
   		 
       	String ags = (String)custAgs.get(cust.getID());
       	if(ags != null) {
      // 	System.out.println("ags" + ags);
       		if(ags.startsWith(":")) {
       			ags = ags.substring(1);
       		}
       	
       			StringTokenizer st = new StringTokenizer(ags,":");
       			while(st.hasMoreElements()) {
       				String ss = st.nextToken();
       				cust.addAgent(new Agent(ss));	
       				if(cust.getAgents().size() == 1 ) {
							custAgendid.put(cust.getID(), ss);
						}
       				
       				   if (ss != null && agent1.get(ss.trim()) != null){
       						Agent aa = (Agent)agent1.get(ss.trim());
       					
       						Vector vec = aa.getDubbler();
       						if(vec!= null && vec.size() >0){
       							Enumeration en = vec.elements();
       							while(en.hasMoreElements()){
       								String ag = (String) en.nextElement();
       								cust.addAgent(new Agent(ag));
       							}
       						}
       					}
       			}
       		
       	}
       		
       	
        }
        
        boolean goes = true;
        
        if(abicusts != null) {
      	  if(!abicusts.contains(cust.getID())) {
      		  goes = false;
      	  }
        }
    
                if(outside == null){
                    cust.addAgent(new Agent(getParent().getProps().getProperty("officerep")));
                    if(getParent().getProps().getProperty("officerep2") != null){
                        cust.addAgent(new Agent(getParent().getProps().getProperty("officerep2")));
                    }
                    if(goes) {
                    customers.put(name, cust);
                    custMap.put(cust.getID(),cust);	
                    }
                
                } else if(outside.containsKey(cust.getID())){
                	Customer ccc = (Customer)outside.get(cust.getID());
                	
                	if(ccc.getCity() != null && ccc.getCity().trim().length() > 0){
                		cust.addAgent(new Agent(ccc.getCity()));
                		cust.setMainAgent(ccc.getCity());
                		  String agentAGENTS = getParent().getProps().getProperty(ccc.getCity());
                          if(agentAGENTS != null){
                  	         StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
                    	       while(stag.hasMoreTokens()){
                              cust.addAgent(new Agent(stag.nextToken()));
                  	       }
                          }
                	}
                	
                	 
                	
                	if(ccc.getVisit() != null && ccc.getVisit().trim().length() > 0){
                		cust.setVisit(ccc.getVisit());
                	}
                      cust.addAgent(new Agent(getParent().getProps().getProperty("officerep")));
                      if(getParent().getProps().getProperty("officerep2") != null){
                          cust.addAgent(new Agent(getParent().getProps().getProperty("officerep2")));
                      }
                      
                    
                      
                      if(goes) {
                    	  customers.put(name, cust);
                          custMap.put(cust.getID(),cust);  
                      }
                	 
            
                }
            }
      rs.close();
      stmt.close();
      con.close();

    } catch (Exception e){
      getParent().showError(e);
    }
    
    System.out.println("Cust suurus " + customers.size());
    return customers;
  }

  private void saveFile(String name , String res) throws IOException{
	
  }


  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }


      public String getDistribution() {
        return null;
      }


  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "    SELECT Artikliklass as liik,Artikliklassi_nimetus as nimi FROM vw_MJ_tootegrupid  where Partner = '" + partnercode + " '  ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik, Converter.enCodeEst(rs.getString("nimi").trim()).replaceAll("ROYAL UNIBREW", " ").trim());
    }
    rs.close();
    st.close();
    con.close();
    
    if(grupid != null){
    Iterator itera = 	grupid.keySet().iterator();
    while(itera.hasNext()){
    	String gr = (String) itera.next();
    	tm.put(gr, gr);
    }
    }
    tm.put("Muud", "Muud");

    return tm;
}


  public TreeMap getProdGroupsSecond() throws SQLException {

    //Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
  /*  String    sql = " SELECT talROUTETYPEID AS liik, talROUTETYPEID AS nimetus FROM inventtable WHERE dataareaid='tal' AND talROUTETYPEID!='' GROUP BY talROUTETYPEID";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik, rs.getString("nimetus").trim());
    }
    rs.close();
    st.close();
    con.close();
*/
    return tm;
  }

  public TreeMap getProducts(Agent agent){
	  StringBuffer buf = new StringBuffer();
    String cmd = "SELECT t.TooteKood as itemid, Nimetus, Kastis, Alusel, Hind AS price   " +
    "  , EAN ,Artikliklass AS grupp, Peaprojekt as grupp1 , YhikMyyk As unit, KM as vat ,MinKogusMyyk  as minimum , KordneKogusMyyk as optimum, " +
    "	KogusVaba as kogus FROM  vw_MJ_tooted t Left join  vw_MJ_Laosaldod s on s. Tootekood= t.TooteKood and t.Partner=s.Partner "+
    " where t.Partner = '" + partnercode + "'";
    
    
    String partiid = "select TooteKood as item,KogusVaba as amount ,Partii as lot from vw_MJ_laosaldodetail where partii != '' and partner ='" + partnercode + "'";
	StringBuffer csv = new StringBuffer();
 	 csv.append("ProductId;Name;Unit;Incase;OnPallet;Price;EAN;Group;VAT;Stock" + ls);


    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {


    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(partiid);
      HashMap partiiHash = new HashMap();
      while(rs.next()){
    	  String item = rs.getString("item").trim();
    	  int kogus = rs.getInt("amount");
    	  String lot = rs.getString("lot").trim();
    	//  buf.append(item+ ";" + lot + ";" + lot + " " + kogus + ";0" );
          //  buf.append("\n");	
    	  if(partiiHash.get(item)!= null){
    		  String olemas = (String) partiiHash.get(item);
    		  partiiHash.put(item, olemas + "," + lot + "|" + kogus);
    	  }else{
    		  partiiHash.put(item, lot + "|" + kogus);
    	  }
      }
    	
    	 stmt =  this.getNewConnection().createStatement();
          rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String add = "";
       String nii =  rs.getString("nimetus").trim();
        
        if(!partnercode.equals("AFRU")) {
        	add = "      ";
        }
      
        if(partiiHash.get(kood)!= null){
        	 prod.setPartii((String)partiiHash.get(kood));
        }
 
        prod.setInCases(rs.getDouble("kastis"));
        prod.setOnPallet(rs.getDouble("alusel"));
        prod.setPrice(rs.getDouble("price"));
  String lopp = "";
        String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
          prod.setEAN(ean);
          lopp = ean.substring(ean.length()-4);
          
      	 if(eanprods == null) {
    	     eanprods= new TreeMap();
    	   }
      	 
      	 if(eanprods.get(prod.getEAN()) == null) {
      	   eanprods.put(prod.getEAN(), prod.getID()); 
      	 }
          
        }
        
        String name = add + nii + " " + kood + " " + lopp ;
        
        
  	  name = name.replaceAll("alk.", " ");
	  name = name.replaceAll(" vol ", " ");
	  name = name.replaceAll("karb.-mata ", " ");
	  name = name.replaceAll("karb.-tud ", "karb. ");
	  name = name.replaceAll("maitseline ", " ");
	  name = name.replaceAll("%", "°");
	  name = name.replaceAll("karastusjook ", " ");
	  name = name.replaceAll("mahlajook ", " ");
	  name = name.replaceAll("naturaalne", "nat.");
	  name = name.replaceAll("A,C,E", "ACE");
	  name = name.replaceAll("A,D,E", "ADE");
	  name = name.replaceAll("vitamiinidega ", " ");
	  name = name.replaceAll("vitamiinidega", " ");
	  name = name.replaceAll("vitamiiniga ", " ");
	  name = name.replaceAll("karboniseer", "karb.");
	  name = name.replaceAll("Karboniseer", "karb.");
	  name = name.replaceAll("pdl ", " ");
	  name = name.replaceAll("Meistrite Gildi", "MGildi");
	  name = name.replaceAll("MEISTRITE GILDI", "MGildi");
	  name = name.replaceAll("Meistrite GIldi", "MGildi");
	  name = name.replaceAll("Mõisakeldri", "MKeldri");
	  name = name.replaceAll("MÕISAKELDRI", "MKeldri");
	  name = name.replaceAll("Koola karb.", " ");
	  name = name.replaceAll("koola karb.", " ");
	  name = name.replaceAll(".vesi ", " ");
	  name = name.replaceAll(" vesi ", " ");
	  name = name.replaceAll("Virsiku ", " ");
	  name = name.replaceAll("Roheline ", " ");
name = name.replaceAll("Poolkuiv ", " ");
name = name.replaceAll("õlu ", " ");
name = name.replaceAll("Active ", " ");
name = name.replaceAll("Sidruni ", " ");
name = name.replaceAll("sidrunimahlaga ", " ");
name = name.replaceAll("apelsinimahlaga ", " ");

	  name = name.replaceAll("Õlu ", " ");
	  name = name.replaceAll(" EE ", " ");
	  name = name.replaceAll("Jäätee ", " ");
	  name = name.replaceAll("jäätee ", " ");
	  name = name.replaceAll("ekstraktiga ", " ");
	  name = name.replaceAll("mahl ", " ");
	  name = name.replaceAll("vaba ", " ");
	  name = name.replaceAll("Pint ", " ");
	  name = name.replaceAll("Õlu ", " ");
	  name = name.replaceAll("   ", " ");
	  name = name.replaceAll("  ", " ");
	  name = name.replaceAll("  ", " ");

      String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
        prod.setGroup(grupp.trim());
        if(eanGrupp != null && this.eanGrupp.get(prod.getEAN()) != null ){
        	if( prod.getEAN().trim().length() >0){
        		 prod.setGroup((String)eanGrupp.get(prod.getEAN()));
        	}else{
        		 prod.setGroup("Muud");
        	}
        }
     }

      prod.setName(Converter.enCodeEst(name));

      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit);
        }
      
      String vat = rs.getString("vat");
      if(vat!=null && vat.trim().length()>0){
         prod.setVat("20");
       }
      
      String kogus = rs.getString("kogus");
      if(kogus!=null && kogus.trim().length()>0){
       prod.setStock(new Double(kogus).doubleValue());
        }else{
    	  prod.setStock(0);
        }
        products.put(name, prod);
        prodHash.put(prod.getID(), prod);
        
  

      }
      rs.close();
     // saveFile("Product_",csv.toString());
      
    } catch (Exception e){
      getParent().showError(e);
    }

    
    
  //  System.out.println(buf.toString());
	if(buf.length() > 2){
        File f = new File("prodactions.txt");
        if (f.exists())f.delete();
          try {
    		PrintWriter os = new PrintWriter(new FileWriter(
    				 "prodactions.txt", true));
    		os.write(buf.toString());
    		os.close();
    	  }catch (IOException ex) {
    		ex.printStackTrace();
    	  }
     }else{
        File f = new File("prodactions.txt");
        if (f.exists())f.delete();
          try {
    		PrintWriter os = new PrintWriter(new FileWriter(
    				 "prodactions.txt", true));
    		os.write("");
    		os.close();
    	  }catch (IOException ex) {
    		ex.printStackTrace();
    	  }
     }
	
	if( 20 > products.size() ) {
		System.exit(0);
	}
    return products;
  }


 private void makeInvoiceInfo(){
	 String cmd = "SELECT arveNr,ArveKuupaev ,KliendiKood,MaksjaKood,MMkood,Maksetahtaeg,Loppsumma,Netosumma,TooteKood, Ladu,KogusMY,Hind ,REaSumma,ReaLoppSuma " +
   "   FROM vw_MJ_kliendi_sortiment  Where Partner ='" + partnercode + "' and ArveKuupaev > getdate()-5  order by Arvenr" ;
	  
	 	StringBuffer csv = new StringBuffer();
	  	 csv.append("InvoiceNo;Invoicedate;CustomreCode;PayerCode;SalesPerson;PaymTerm;Totasum;Netosum;productCode;Warehouse;Amount;Price;LineSum;LineSumTotal" + ls);

   if (isDebug()) System.out.println(cmd);

    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
     
      while(rs.next()){
    	
            csv.append(rs.getString("arveNr") + ";" + rs.getString("ArveKuupaev") + ";" + rs.getString("KliendiKood") + ";" + 
            		rs.getString("MaksjaKood") + ";" + rs.getString("MMkood") + ";" + rs.getString("Maksetahtaeg") + ";" + 
            		rs.getString("Loppsumma") + ";" + rs.getString("Netosumma") + ";" + rs.getString("TooteKood") + ";" + 
            		rs.getString("Ladu") + ";" + rs.getString("KogusMY") + ";" + rs.getString("Hind") + ";" + 
            		rs.getString("REaSumma") + ";" + rs.getString("ReaLoppSuma")   + ls);
      }
      
      saveFile("Invoices_",csv.toString());
    } catch (Exception e){
        getParent().showError(e);
      }
    	  
    
 }

  public Vector getCustomerProductsList(String agentCode){
    Vector custProds = new Vector();
    HashMap custs = new HashMap();
    makeInvoiceInfo();
    //if (true) return custProds ;
 int counter = 1;
   String cmd = "SELECT KliendiKood as customer,TooteKood as itemid ,KogusMY as qty,ArveKuupaev  AS lastdate ,Hind as lastprice  " +
     "  FROM vw_MJ_kliendi_sortiment  Where Partner ='" + partnercode + "' order by KliendiKood,TooteKood,ArveKuupaev desc   " ;
  
   if (isDebug()) System.out.println(cmd);

    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      counter++;
      while(rs.next()){
   
    	  
        String kliendikood = rs.getString("customer").trim();

        String tootekood = rs.getString("itemid").trim();
        if(!custs.keySet().contains(kliendikood+ tootekood)){
		           			       
		        double kogus = rs.getDouble("qty");
		        String lastdate = rs.getString("lastdate").substring(0,10);
		        lastdate = lastdate.substring(0,10);
		        custs.put(kliendikood + tootekood,lastdate); 
		        lastdate = lastdate.substring(5,10); 
		        double lastprice = rs.getDouble("lastprice");
		        
		        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,lastprice,0,0," ");
		        custProds.add(cp);
        }
        
      }
 
      
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
    
      if(this.partnercode.equals("AFRU")) {
    	  TreeMap cus = getCustAgsExcel();
    	  if(cus == null) {
    		  System.exit(0);
    	  }
    	  String cmd =" select t.Kliendikood , sum(t.summ) as summa from \r\n" + 
    	  		"(select KliendiKood , (Netosumma) as summ from vw_MJ_Kliendi_Sortiment where partner ='AFRU'\r\n" + 
    	  		" and year(arvekuupaev) = year(getdate()) and month(arvekuupaev) = month(getdate()) group by  KliendiKood,Netosumma) as t group by t.KliendiKood";
System.out.println("Siin sees ");
          if (isDebug()) System.out.println(cmd);
             try {
            	 
            	 TreeMap<String,Double> myyk = new TreeMap<String,Double>();
                 Statement stmt =  this.getNewConnection().createStatement();
                 ResultSet	      rs = stmt.executeQuery(cmd);
                 while(rs.next()) {
                   String cuID = rs.getString("KliendiKood").trim();
                   Double summ = rs.getDouble("summa");
                   String agID = (String)cus.get(cuID);
                  // System.out.println("Siin sees1 " + agID);
                   if(agID != null) {
                	   
                	   agID = agID.substring(1);
                	   
                	   if(agID.contains(":")) {
                		   StringTokenizer st = new StringTokenizer(":",agID);
                           if(st.hasMoreTokens()) {
                         	   String agg = st.nextToken();
                         	  double ss = 0;
                         	   if(myyk.get(agg) != null) {
                             	   ss = myyk.get(agg) ;
                                }
                                myyk.put(agg, summ + ss);  
                           }
                	   } else {
                		   double ss = 0;
                		   if(myyk.get(agID) != null) {
                         	   ss = myyk.get(agID) ;
                            }
                            myyk.put(agID, summ + ss);   
                	   }
                     
                   }
                 }
                 rs.close();
                 con.close();
                 
                 
                 System.out.println("Siin sees2 " + myyk.toString() );
                Iterator itera = myyk.keySet().iterator();
                while(itera.hasNext()) {
                	String agentID = itera.next()+"";
                	Agent agent = (Agent)agents.get(agentID);
                //	 System.out.println(agent + " "  + agentID);
                    if (agent != null){
                      agent.setMonthSales((int) (myyk.get(agentID)).doubleValue());
                      //System.out.println(agent.getMonthSales());
                    }	
                }
                 
                 
                 
                 
                 
             } catch (Exception e) {
                 getParent().showError(e);
              }
    	  
      }else {
    	  
       
      String cmd =" select MyygiMees as agent ,MyykSumma as summa from vw_MJ_agendimyyk  ";

         if (isDebug()) System.out.println(cmd);
            try {
                Statement stmt =  this.getNewConnection().createStatement();
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
                con.close();
            } catch (Exception e) {
                getParent().showError(e);
             }
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
    return "SmartenR";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent){
     return null ;
  }





       public NumberFormat getNF(){
         return NF;
       }



       private String getDateYYYMMDD(long ll){
 		  Calendar cal = Calendar.getInstance();
 		  if(ll >0)
 		  cal.setTimeInMillis(ll);
 		    StringBuffer sb = new StringBuffer();
 		    String year =   cal.get(Calendar.YEAR) + "";
 		    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
 		    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
 		     if (day.length() == 1){
 		      day = "0" + day;
 		    }
 		    if (month.length() == 1){
 		     month = "0" + month;
 		   }
 		    sb.append(year + month + day);
 		    return sb.toString();
 		  }

     private String getOutCSVDir(){
         String dir = getParent().getProps().getProperty("readoutcsv");
         if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
         return dir;
       }



       /**
        *
        * @param aNF - the new value for NF
        */
       public void setNF(NumberFormat aNF){
         NF = aNF;
    }
  private String url = "jdbc:odbc:mobilejava";
  private Connection con;
  private  Vector allcust; 
  private   Vector debt = new Vector();
  private Statement stmt;
  private	String date = getDateYYYMMDD(0);
  private String ls = System.getProperty("line.separator");
  private String partnercode = null;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
