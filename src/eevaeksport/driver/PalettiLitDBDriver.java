/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
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
public class PalettiLitDBDriver extends AbstractDBDriver implements  BulkUpdater ,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public PalettiLitDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
        emailProps = new Properties();
        try{
        	emailProps.load(new FileInputStream(parent.getProps().getProperty("emailfile")));
        }catch (Exception e){
        }
        try {
            Class.forName(databaseDriverName).newInstance();

        } catch (Exception e) {
            parent.showError(e);
        }
    
        nodebt = new Vector();
        String nd = getParent().getProps().getProperty("nodebt.list");
        if(nd != null){
     	   StringTokenizer st = new StringTokenizer(nd,",");
     	   while(st.hasMoreTokens()){
     		   nodebt.add(st.nextToken());
     	   }
        }
    
    }

    
    Vector nodebt = null;  
    
    HashSet picts = new HashSet();

    private HashSet picts(){
    	 HashSet picts = new HashSet();
    	 
    	   String pictspda = parent.getProps().getProperty("pictpda.dir");
    	   File res2 = new File (pictspda);
           String[] ff2 = res2.list();
    	   for(int i = 0;ff2.length>i;i++){
    		   picts.add(ff2[i].substring(0, ff2[i].indexOf(".")));
    	   }
    	   
            
    	   System.out.println("picts size " + picts.size());
    	   
    	 return picts;
    	
    }
    
    boolean makingPictures = false;
    
	  private void makePictureCat(){
		  pictures = true;
		  makingPictures = true;
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
//	         	 Lõppes kustutamine 
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
	             	if( ff2[i].equals(toode + ".jpg") ||
	             			ff2[i].equals(toode + ".JPG")){
	             		 
	                  
	               // System.out.println("pilt olemas " + ff2[i]);
	            	File in = new File ( pictssou + ff2[i]); 
	              	if(in.isFile()){
	              	File inpda = new File ( pictspda + toode+ ".jpg");

                    
	              	if(!inpda.exists()){
	                  	Converter.shrinkAndCopyFile150(in,inpda);
          	          }  
	              	 if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
                 		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
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



    public String getAssortmentStr(Agent agent){
    	return null;
    }
            public String getDealerCustomerCSV(String agent) {
		return null;
	}
               
            
    public String getSaveDebtInfo() {
      		  makePictureCat();
      		  System.exit(0);
      	   	return null;
    }
            
        public Vector getFixedAssortmentData() {
          return null;
        }

	public String getCustProdDiscounts(Agent agent) {
		return null;
	}

	public Vector getPriceMatrixData() {
		Vector lines = new Vector();

		String sql = " select  kontrah as grupp , vkodas as toode , kainaEU as price from kainoskontrah l";

		Statement st = null;
		try {
			connectionUp();
			st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {

				String clGroup = rs.getString("grupp");
				String prodID = rs.getString("toode");
				double price = rs.getDouble("price");
				MatrixLine mline = new MatrixLine();
				
				if ( clGroup != null && clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
					mline.setStartDate(Converter.getYYYYpMMpDDpString(new Date()));
				}

				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}
			
				if (price != 0) {
					mline.setPrice(price);
				}
				lines.add(mline);
			}

			rs.close();
			
			
					 sql = " select kontrah as grupp , grKodas as tgrupp , kaina as ale "
							+ "  from nuolgrkontrah where kaina != 0 " ;
	
				 rs = st.executeQuery(sql);

				while (rs.next()) {

					String clGroup = rs.getString("grupp");
					String prodGR = rs.getString("tgrupp");
					double ale = rs.getDouble("ale");
					MatrixLine mline = new MatrixLine();
					
					if (clGroup != null
							&& clGroup.trim().length() > 0) {
						mline.setCliGroup(clGroup.trim());
						mline.setStartDate(Converter.getYYYYpMMpDDpString(new Date()));
					}

					if (prodGR != null && prodGR.trim().length() > 0) {
						mline.setProdGroup(prodGR.trim());
					}
				
					if (ale != 0) {
						mline.setDiscount(ale);
					}
					
					lines.add(mline);
				}
			
			
			
			st.close();
			con.close();
		} catch (SQLException ex) {
		ex.printStackTrace();
		}
		return lines;
	}

	 public String getProductSortingData(){
	   	return null;
	   }

		public Vector getLastThreeData() {
			// TODO Auto-generated method stub
			return null;
		}
	

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
     String sql = " select  kontrahkod AS klient,moketkod as payer, Dokumnr AS number, "
     		+ "apmokdata AS maksepaev ,nepadsumaval AS tasumata, dokumdata AS kuupaev, "
     		+ "getdate() -apmokdata  AS yle from opzurant where objkod<>'0B0B' and nepadsumaval != 0" ;
      connectionUp();
      PreparedStatement st = con.prepareStatement(sql);
      //st.setString(1, agentCode);
      ResultSet rs = st.executeQuery();
      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        debt.setPayerID(rs.getString("payer").trim());
        String number = rs.getString("number").trim();
       
        debt.setDoknr(number);
        Date mm = rs.getDate("maksepaev");
        
        
        debt.setSum(rs.getDouble("tasumata"));
        debt.setDate(rs.getDate("kuupaev"));
        if(mm != null){
        	debt.setDuedate(mm);
        }else{
        	debt.setDuedate(debt.getDate());
        }
       
        String yle = rs.getString("yle");
        if(yle != null){
        	  debt.setDue(yle.trim());
        }
      
        boolean addDebt = true;
        if(nodebt.contains(debt.getPayerID())){
        	addDebt = false;
        }
        
        if(nodebt.contains(debt.getCustID())){
        	addDebt = false;
        }
        if(addDebt){
        	   list.addElement(debt);
        	        	
        }
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



    public String getSaveStockInfo(){
        
  	  String  cmd  = "select toode,stock from (select VERTYBESKODAS as toode,sum(kiekis)-isnull((select sum(kiekisapskvnt) from linum where\r\n" + 
  	  		"                       linum.periodono='XX' and linum.vertybeskodas=likoi.vertybeskodas and linum.objektokodas=likoi.objektokodas ),0)\r\n" + 
  	  		"               as stock  from likoi where  objektokodas='0110' and isnull(saugojimovieta,'')<>'' group by VERTYBESKODAS,objektokodas\r\n" + 
  	  		"                       having stock>0 order by VERTYBESKODAS) AA" + 
  	  		" UNION select kodas as toode,999 as stock from usrPrekiuRinkiniai";
  	

         if (isDebug()) System.out.println(cmd);

         stocks = new HashMap();
         StringBuffer products = new StringBuffer();
         try {

         connectionUp();
           Statement st = con.createStatement();
           ResultSet rs = st.executeQuery(cmd);

           while (rs.next()) {

             String kood = rs.getString("toode").trim();
             double seis = rs.getDouble("stock");
             if(seis > 0){
            	 stocks.put(kood, seis);
                 products.append(kood +  ";");
                 products.append(" ;");
                 products.append(getParent().getNF().format(seis) + "; "  +  "\n"); 
               }
             }
             rs.close();
         } catch (Exception e) {
             getParent().showError(e);
         }
         return products.toString();
     }



     



        public String getDistribution() {
          return null;
        }


    private void connectionUp(String url){
      try {
        if (con != null){
          con.close();
        }

           con = DriverManager.getConnection(url,"","");
      } catch (Exception e) {
            getParent().showError(e);
      }

    }

    private String ensure6 (String asi){
    	if(asi != null){
    		if(asi.length() > 6 ){
    			asi = asi.substring(0,6);
    		}else if (asi.length() == 0){
    			asi =  "000000";
    		}else if (asi.length() == 1){
    			asi = asi + "00000";
    		}else if (asi.length() == 2){
    			asi = asi + "0000";
    		}
    		else if (asi.length() == 3){
    			asi = asi + "000";
    		}
    		else if (asi.length() == 4){
    			asi = asi + "00";
    		}else if (asi.length() == 5){
    			asi = asi + "0";
    		}
    	}else{
    		asi =  "000000";	
    	}
    	
    	return asi ;
    }
    
    
    public TreeMap getProdGroups() throws SQLException {

      connectionUp(url2);
     this.setTreeGroup(true);
      TreeMap tm = new TreeMap();

            String cmd = " select KODAS AS kood,PAVADINIMAS  AS nimi  from gruko where  ParentKodas is null  order by kodas";
       try {
        stmt = con.prepareStatement(cmd);
        ResultSet rs = stmt.executeQuery();
      // setTreeGroup(true);
        while (rs.next()) {
          String liik = rs.getString("kood").trim();
          String nimetus = liik + " " +  Converter.enCodeEst(rs.getString("nimi").trim());
          if (liik != "") {
            tm.put(ensure6(liik),nimetus);
          }
        }
      }
      catch (Exception e) {
        getParent().showError(e);
      }	
       
       tm.put("000000","NOGROUP");
      connectionDown();
      return tm;
    }


    public TreeMap getProdGroupsSecond() throws SQLException {
    	  connectionUp(url2);

          TreeMap tm = new TreeMap();

                String cmd = " select ParentKodas as mother , KODAS AS kood,PAVADINIMAS  AS nimi  from gruko where  ParentKodas is not null  order by kodas";
           try {
            stmt = con.prepareStatement(cmd);
            ResultSet rs = stmt.executeQuery();
          // setTreeGroup(true);
            while (rs.next()) {
              
              String mother = this.ensure6(rs.getString("mother").trim()) + ".";
              String liik = rs.getString("kood").trim();
              String nimetus =  liik + " " +   Converter.enCodeEst(rs.getString("nimi").trim());
            
              if (liik != "") {
                tm.put(mother + liik,nimetus);
              }
            
            }
          }
          catch (Exception e) {
            getParent().showError(e);
          }	
           
           tm.put("000000","NOGROUP");
          connectionDown();
          return tm;
    	
      }

    /**
     *
     */
    public void connectionDown(){
        try {
        	if(con != null){

                con.close();
                con = null;	
        	}
        } catch (Exception e) {
            getParent().showError(e);
        }
    }
    
    private HashMap stocks = new HashMap();
    public DealerProduct getDealerProductsList() {
         return null;
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
        connectionUp(url2);
        String  cmd = "  select kodas as kood, pavadinimas as nimi  from regionai group by kodas,pavadinimas ";
        TreeMap agents = new TreeMap();
        
        if("true".equals(getParent().getProps().getProperty("makepict"))){
			   makePictureCat(); 
		   }
        
        StringBuffer sb2 = new StringBuffer();
        try {
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("kood").trim());
              String name = rs.getString("nimi").trim();
              	    sb2.append(agent.getID() + ";" + name + "\n");  
              agent.setName(name);
              agents.put(name, agent);

              if(sb2.length() > 2){
                	try{
                		 Converter.saveFileInUTF8( "agent.txt", sb2);
                	}catch (Exception e){
                		
                	}                
                 }
            }
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        return agents;
    }
    
    private String replaceBad(String token ){
    	StringBuilder s = new StringBuilder(token.length());

    	CharacterIterator it = new StringCharacterIterator(token);
    	for (char ch = it.first(); ch != CharacterIterator.DONE; ch = it.next()) {
    	    switch ((int)ch) {
    	        case 63:
    	            s.append("11120xx");
    	            break;
    	        case 731:
    	            s.append("11121xx");
    	            break;
    	        case 711:
    	            s.append("11122xx");
    	            break;
    	      
    	        default:
    	            s.append(ch);
    	            break;
    	    }
    	}

    	return  s.toString();
    }

    public TreeMap getCustomers(Agent agent){
        connectionUp(url2);
        String cmd = "SELECT  KODAS AS kood,Moka  as payer,  pavadinimas AS nimetus, Gatve AS aadress, miestas as city , dilerinuolproc  AS ale , " +
				" ' ' AS visit , regionas AS agent ,  ApmokejimoTerminas as paymterm ," +
				" MobilusTelefonas as phone, email , moka as chain FROM VISI.IMOKO where gavejas = 'T' and regionas is not null ";
      
        
        
        log(cmd);
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        HashMap volad = new HashMap();
        HashMap maksed = new HashMap();
        try {
        	

            String sql = "select  kontrahkod AS klient, sum(nepadsumaval) AS summa " +
	   " from opzurant where objkod<>'0B0B' and  getdate() -apmokdata >=0  GROUP BY kontrahkod having sum(nepadsumaval) != 0";
            stmt = con.prepareStatement(sql);
             ResultSet	      rs1 = stmt.executeQuery();

            while (rs1.next()) {
              String id = rs1.getString("klient").trim();
              double debt = Double.parseDouble(rs1.getString("summa").trim());
                volad.put(id, debt);
            }
            rs1.close();
        	
             sql = "select  moketkod AS klient, sum(nepadsumaval) AS summa " +
        "  from opzurant where objkod<>'0B0B' and  getdate() -apmokdata >=0  GROUP BY moketkod having sum(nepadsumaval) != 0";
            stmt = con.prepareStatement(sql);
           	      rs1 = stmt.executeQuery();

            while (rs1.next()) {
              String id = rs1.getString("klient").trim();
              double debt = Double.parseDouble(rs1.getString("summa").trim());
                volad.put(id, debt);
            }
            rs1.close();
            
            sql = "select e.kontrahkod AS klient, e.operdata as daat , sumcurrency as summa from opzureil e "
            		+ "key join opzurant o where o.zurnkod='PG' and e.operdata  > getdate()-60 order  by e.operdata desc ";
            
           stmt = con.prepareStatement(sql);
          	      rs1 = stmt.executeQuery();

           while (rs1.next()) {
             String id = rs1.getString("klient").trim();
             String daat = rs1.getString("daat").substring(0,10).trim();
             double debt = Double.parseDouble(rs1.getString("summa").trim());
             String text = "(" + getParent().getNF().format(debt) + " €) " + daat ;
            		 if(maksed.get(id) == null){
            	           maksed.put(id, text);
            	     }
              }
           
           rs1.close();
           
        	
            stmt = con.prepareStatement(cmd);
            //stmt.setInt(1, Integer.parseInt(agent.getID()));
            //stmt.setDate(2, getStartingDate());
            ResultSet	      rs = stmt.executeQuery();


            while (rs.next()) {
              String accountnum = rs.getString("kood").trim();
              String payer = rs.getString("payer");
      
              
              String name = Converter.enCodeEst(rs.getString("nimetus").trim() + " " + accountnum);
              
              Customer cust = new Customer(accountnum);
              if(payer != null && payer.trim().length() > 0){
            	  cust.setPayer(payer);
              }
             
              if(maksed.get(accountnum) != null){
            	  cust.setCity(Converter.enCodeEst("Pask. mokėjimas " ) + maksed.get(accountnum) );
              }
              
              if(maksed.get(payer) != null){
            	  cust.setCity(Converter.enCodeEst("Pask. mokėjimas " ) + maksed.get(payer) );
              }
              
              
              boolean addDebt = true;
              if(nodebt.contains(cust.getPayer())){
              	addDebt = false;
              }
              
              if(addDebt){
            	
              if(volad.get(accountnum) != null){
            	  cust.setDebt(Double.parseDouble("" +volad.get(accountnum)));
              }
              
              if(volad.get(payer) != null){
             	  cust.setDebt(Double.parseDouble("" +volad.get(payer)));
               }
            
              
              }
              String aadr = rs.getString("aadress");
              if(aadr != null && aadr.trim().length()>0){
            	  
            	  String city = rs.getString("city");
            	  if(city != null && city.trim().length()>0 && !aadr.contains(city.trim())){
            		  aadr = aadr + " " + city.trim();
            	  }
            	  
                cust.setAddress(Converter.adressLink(Converter.enCodeEst(replaceBad(aadr.trim())),"Lithuania"));
                name = name + " " + aadr.trim();
              }
              cust.setName(Converter.enCodeEst(replaceBad(name)));

              String discStr = rs.getString("ale");
              if (discStr != null && discStr.trim().length() >0 ){
            	  try{
            		     cust.setDiscount(Double.parseDouble(discStr.trim()));
            	            		  
            	  }catch(Exception e){
            		  
            	  }
              }
              
              String visit = rs.getString("visit");
              if (visit != null && visit.trim().length()>0){
                cust.setVisit(visit.trim());
              }
              
              
             
              
            

             String ag = rs.getString("agent").trim();
              cust.addAgent(new Agent(ag));
              cust.setPaymTerm(rs.getInt("paymterm")+"");
              
              String phone = rs.getString("phone");
              if(phone != null && phone.trim().length() > 0){
            	  cust.setPersonalData("Tel: " + phone.trim());
              }
              String email = rs.getString("email");
              if(email != null && email.trim().length() > 0){
            	  cust.setPersonalData((cust.getPersonalData() + " " + email.trim()).trim());
            	//  if(name.toLowerCase().contains("maxima")){
            		  cust.setConfirmEmail(email.trim());
            	  //}
              }
              

              String chain = rs.getString("chain");
              if(chain != null && chain.trim().length() > 0){
            	  cust.setChain(chain.trim());
            	  cust.setGroup(chain.trim());
              }
              customers.put(name, cust);
              custMap.put(cust.getPayer(), cust);
       
            }

            rs.close();
            stmt.close();
            con.close();
            
          } catch (Exception e) {
            getParent().showError(e);
          }

          return customers;
    }

    
    private boolean pictures = false;
    
    
    public TreeMap getProducts(Agent agent){
    	getSaveStockInfo();
    	System.out.println("stocks suurus " + stocks.size());
  
   String cmd = "select toode,nimetus,tykke,myygihind,ean,unit,vat,kogus,grupp,grupp2 from (" + 
   		"SELECT KODAS AS toode,PAVADINIMAS  AS nimetus,1  AS tykke, 0 AS myygihind, " + 
   		"   BARKODAS AS ean, APSKVNTKODAS as unit, PVMPROCENTAS as vat,  " + 
   		"   0 AS kogus,(select parentKodas from gruko g where g.kodas = VERTYBGRUPESKODAS) AS grupp, VERTYBGRUPESKODAS AS grupp2 " + 
   		"    FROM verko a  where SASKPREKODAS = '2040' and BARKODAS != '') BB " + 
   		"union select kodas as toode,(select top 1 pavadinimas  from \r\n" + 
   		"vw_PrekiuRinkiniai where Kodas_usr  = kodas) as nimetus,1 as tykke,0 as myygihind,(SELECT top 1 vv.barkodas as barcode " + 
   		"FROM \"VISI\".\"usrDetalizacija\" dt join verko vv on dt.kodasv=vv.kodas where barkodas is not null and dt.kodas_usr = xx.kodas ) as ean,"
   		+ "'VNT' as unit,21 as vat, "
   		+ " 0 as kogus,'Rinkiniai' as grupp,'Rinkiniai2' as  grupp2 " + 
   		"from usrprekiurinkiniai xx " ;
     
            System.out.println(cmd);
        TreeMap products = new TreeMap();

        try {
        	
        	
        	
    String sql = "select VERTYBESKODAS as kood , DYDIS as hind from vkainos  " ;
            System.out.println("hind");
          if (isDebug()) System.out.println(sql);
          connectionUp(url2);
          TreeMap tooted = new TreeMap();
          Statement    stm = con.createStatement();
            ResultSet  rst = stm.executeQuery(sql);

               while (rst.next()) {
                 String toode = rst.getString("kood").trim();
                 double hind = rst.getDouble("hind");
                	 Toode tt = new Toode(toode);                
                     tt.setKastis(hind);
                     tooted.put(toode,tt);	
             }
             rst.close();
             stm.close();
             this.connectionDown();
//System.out.println("hinnad " + tooted.size());
        	
        	
             connectionUp(url2);
            stmt = con.prepareStatement(cmd);
            ResultSet	      rs = stmt.executeQuery();

            
            HashSet picts = null;
            if(!this.makingPictures){
            	 picts = picts();
            }
            
            while (rs.next()) {
              String kood = rs.getString("toode").trim();
              Product prod = new Product(kood);
              String nimetus = rs.getString("nimetus").trim();
             prod.setOldName(nimetus);
              
            if(tooted.get(kood) != null){
            	Toode tt = (Toode)tooted.get(kood);
            	  prod.setPrice(tt.getKastis());
            }
             // String price = rs.getString("iii");
              String add = "";
              String ean = rs.getString("ean");
              if(ean != null && ean.trim().length()>0){
            	      prod.setEAN(ean.trim());	
            	      add = " " + ean.trim().substring(ean.trim().length() -3);
            	     
              }
       
              prod.setName(kood + add);
              prod.addBarcode(kood);
              
              String unit = rs.getString("unit");
              if (unit != null && unit.trim().length()>0){
                prod.setUnit(unit.trim());
                if(unit.contains("RINK")) {
                	prod.setUnit("VNT");
                }
              }
              double vat = rs.getDouble("vat");
              
              if(vat > 0){
            	  prod.setVat(vat + "");
              }
              
              String sort = "";
              if(stocks.get(prod.getID()) != null){
            	  prod.setStock((Double)stocks.get(prod.getID())); 
            	  sort = (100000 -(Double)stocks.get(prod.getID())) +"";
              }
              
              String grupp = rs.getString("grupp");
             if (grupp != null && grupp.trim().length()>0){
            	 prod.setGroup(this.ensure6(grupp.trim()));
             }else{
            	 prod.setGroup("000000");
             }
             String grupp2 = rs.getString("grupp2");
             if (grupp2 != null && grupp2.trim().length()>0){
               prod.setGroupSecond(prod.getGroup()+ "." + grupp2.trim());
             }
             
             prod.setVat("21");
             
             boolean goes = true;
             
             if(picts != null && !picts.contains(prod.getID()) && !pictures){
            	 goes = false;
            	 //System.out.println(prod.getID() + " 1 " + goes );
             }

            
             if(prod.getStock() < 0 && !pictures){
            	 goes = false;
            	// System.out.println(prod.getID() + " 2 " + goes );
             }
             

             
             if(goes){
            	 prod.setNameSorting(sort + " " + prod.getName() + " " + prod.getID());
                 products.put(prod.getID(), prod);
             }

            
            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        System.out.println("products " + products.size());
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
        connectionUp(url2);
        
        String cmd ="  SELECT kontrahentokodas  AS klient, vertybeskodas AS toode,"
        		+ "KIEKISAPSKVNT AS kogus, gavimodata AS kuupaev FROM linum l " +
				"  inner join dokum d on d.dokumentono=l.dokumentono " +
				" WHERE gavimodata > Getdate()-90 and SASKDEBETKODAS ='2410' " +
				" GROUP BY kontrahentokodas,vertybeskodas,KIEKISAPSKVNT,gavimodata " ;
        		
        if (isDebug()) {
           System.out.println(cmd);
       }

       Vector custProds = new Vector();
       HashMap kliendid = new HashMap();


     /*  try {
          stmt = con.prepareStatement(cmd);
         // stmt.setDate(1, getStartingDate());
           ResultSet	      rs = stmt.executeQuery();

           while (rs.next()) {
             String kliendikood = rs.getString("klient").trim();
             
             String tootekood = rs.getString("toode").trim();
             double kogus = Double.parseDouble(rs.getString("kogus").trim());
             String kuup = rs.getString("kuupaev").substring(5,10).trim();
             Customer cust = (Customer) kliendid.get(kliendikood);

             if (cust == null) {
               cust = new Customer(kliendikood);
             }

             Product prod = new Product(tootekood);
             cust.addProduct(prod,
                             new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                          kuup, 0, 0,0," "),false);
             kliendid.put(kliendikood, cust);
             custProds.add(new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                          kuup, 0, 0,0," "));
           }
           rs.close();
           } catch (Exception e) {
           getParent().showError(e);
       }
       connectionDown();*/
       return custProds;
    }


    public String getPilotDayData(Agent agent){
      StringBuffer sb = new StringBuffer();
   try {
       File dir = new File(getParent().getProps().getProperty("pop3.dir"));
       if(dir.exists() && dir.isDirectory()){
    	   String [] files = dir.list();  
    	   if(files.length>0){
    		   for (int i = 0;i<files.length;i++){      		  
        		   if (files[i].toLowerCase().indexOf(agent.getID().toLowerCase())!= -1 && files[i].toLowerCase().endsWith("log")){
        			   System.out.println(files[i]);  
        			   File f = new File(getParent().getProps().getProperty("pop3.dir") + (files[i].substring(0,files[i].indexOf(".")) + ".LOG").toUpperCase());
        			   System.out.println(f.getAbsolutePath());  
        			   if(f.exists()){
        				   FileReader fr = new FileReader(f.getAbsoluteFile());
        				  
        				   BufferedReader reader = new BufferedReader(fr);
        			        String line = " ";
        			        
        			      while( ((line = reader.readLine())!= null )){
        			    	  System.out.println(line); 
        			    	  if(line.trim().startsWith("Sufor")){
        			    		  sb.append(line.substring(line.indexOf("Klientas")+9) + "\n");
        			    	  }
        			    	  if(line.trim().startsWith("*Klaida")){
        			    		  sb.append("   " +line.substring(line.indexOf("Klaida")+8) + "\n");
        			    	  }
        			      }
        			   }
        		   }     		   
    		   }
    	   }
       }
        
                        } catch (Exception e) {
                            getParent().showError(e);
                        }
                        System.out.println(agent.getID() + sb.toString());
           if(sb.toString().trim().length()>0){
        	try{
        		Email mail = new Email(null);
        		mail.setSMTP(getParent().getProps().getProperty("smtphost"));
        		mail.setFilesTo("apsk");
        		mail.setFrom(getParent().getProps().getProperty("from"));
        		mail.setSubjectFilesTo("Problems From Rivile Impormodule");
        		mail.setBody("\n" + sb.toString());
        		mail.setSendNotice(false);
        		mail.send();
        	}catch(Exception e){
        		
        	}
        	   
            return sb.toString();
           }else {
           	return null;
           }
    }

    
    private Properties emailProps = null;

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




    public void setBulkSales(Map agents){
    	
    	 connectionUp(url2);
    	 
    	 String per = Converter.getYYYYMMString(0);
    	 
       String cmd = " select im.regionas as agent ,sum(doksumaval) as summa " +
 " from opzurant o join imoko im on o.kontrahkod=im.kodas " +
  " where o.objkod<>'0B0B' and  o.periodas='" + per + "' and  im.regionas is not null group by im.regionas";

       log(cmd);

         try {
        	 
        	 
           stmt = con.prepareStatement(cmd);

      

     ResultSet	      rs = stmt.executeQuery();
             while(rs.next()) {
               String agentID = rs.getString("agent").trim();
               Agent agent = (Agent)agents.get(agentID);
               if (agent != null){
                 agent.setMonthSales((int)rs.getDouble("summa"));
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

    public static final String DRIVER_VERSION = "PalettiLit";
    private String             url = "jdbc:odbc:PalettiSync32";
    private String	       url2 = "jdbc:odbc:PalettiSync32";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

