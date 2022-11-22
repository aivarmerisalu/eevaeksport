package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Dealer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class NaviLivikoESTDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager,PictureMaker,FixLoader {

  public NaviLivikoESTDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();
	} catch (Exception e) {
	    parent.showError(e);
	}
	
	
	    nodebt.add("S6807"); // maxima
	    nodebt.add("S7189"); // rkiosk
	    nodebt.add("S6021"); // Rimi
	    nodebt.add("S6708"); // selver
	    nodebt.add("S9915"); // cirkle
	    nodebt.add("S6029"); // harju ty
	    nodebt.add("S6641"); // prisma
	    nodebt.add("S6891"); // Comarket
	    user =  getParent().getProps().getProperty("rest.user");
	    pass = getParent().getProps().getProperty("rest.pass");
	    rcompany = getParent().getProps().getProperty("rest.companyx");
	     }
  
	  String user =  null;
	  String pass = null;
	  String rcompany = null;
  
  
   public String getPilotDayData(Agent agent){
     return null;
   }
   public String getSaveDebtInfo() {
   	return null;
   }

   
   
   @Override
  	public void FixLoader() {
  		this.getProducts(null);
  		TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
  		System.out.println("Contracts " + contracts.keySet().size() + " " + contracts.keySet().toString());
  		if(contracts.size() > 0) {
  			Converter.sendContractJson(contracts ,  rcompany,  user ,  pass);
  		}
  		
  		System.exit(0);
  	}

   public Vector getPriceMatrixData(){

	TreeMap actions = new TreeMap();
	TreeMap<String,Boolean> dates = new TreeMap<String,Boolean> ();
	TreeMap<String,Boolean> datesgreen = new TreeMap<String,Boolean> ();
	   Calendar now = Calendar.getInstance();
	   now.add(Calendar.DATE, -1);
	   
	   try {

		      String cmd = " select Campaign, name ,sellingstartday as ddd , sellingendday as ggg from CustomerCampaign" + 
		      		"  group by Campaign, name, sellingstartday, sellingendday " ;
										
	  	  System.out.println(cmd);
	  	     
	  	      Connection conv = getNewConnection2();
	  	       Statement stmt1 = conv.createStatement();
	  	       ResultSet rs1 = stmt1.executeQuery(cmd);
	  	       while(rs1.next()){
	  	          
	  	    	
	  	    	String camp = rs1.getString("Campaign");
	  	    	 String name = rs1.getString("name");
	  	    	 java.sql.Date dd = rs1.getDate("ddd");
	  	    	 java.sql.Date gg = rs1.getDate("ggg");
	  	    	 Calendar cc = Calendar.getInstance();
	  	    	 cc.setTime(gg);
	  	    	 cc.add(Calendar.DAY_OF_YEAR, -1);
	  	    	Calendar bb = Calendar.getInstance();
	  	    	 bb.setTime(dd);
	  	    	 bb.add(Calendar.DAY_OF_YEAR, -1);
	  	    	Date ggu = cc.getTime();
	  	    	Date ddu = bb.getTime();
	  	         boolean valid = false;
	  	       boolean green = false;
	  	         if(ddu.getTime() < now.getTimeInMillis() ) {
	  	        	 valid = true;
	  	         }
	  	         if( ggu.getTime() > now.getTimeInMillis() ) {
	  	        	 green = true;
	  	         }
	  	    	 
	  	          if(name != null && name.trim().length() > 0 && camp != null && camp.trim().length() > 0  ) {
	  	        	   actions.put(Converter.cleanLitLatEst(camp.trim()), name.trim());
	  	        	 dates.put(Converter.cleanLitLatEst(camp.trim()), valid);
	  	        	 datesgreen.put(Converter.cleanLitLatEst(camp.trim()), green);
	  	          }
	  	        
	  	         }

	  	       rs1.close();
	  	       stmt1.close();
	  	       conv.close();
	  	      } catch (Exception e){
	  	        e.printStackTrace();
	  	      }
	        
	   
	   

	  	 String  sql = " select * from PriceMatrixData where [minamount] = 0 order by salestype desc " ; 
	
  	 
	  	System.out.println("matrix  " + sql);
       Vector result = new Vector();
         try {
          
      	   Connection con = getNewConnection2();
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(sql);
           while (rs.next()) {

             String custID = Converter.enCodeEst(rs.getString("custid"));
             String clGroup = Converter.cleanLitLatEst(rs.getString("clgroup"));
             String prodID = rs.getString("prodid");
             String prodGroup = Converter.cleanLitLatEst(rs.getString("prodgroup"));
            
             double price = rs.getDouble("price");
             double disc = rs.getDouble("disc");
             Date date = rs.getDate("endday");
             Date sdate = rs.getDate("startday");
             int type = rs.getInt("salestype");
          boolean goes = true;
           //  int paevi = rs.getInt("paevi");
             
             MatrixLine mline = new MatrixLine();

             if (custID != null && custID.trim().length() > 0) {
               mline.setCustID(custID.trim());
             }
          
             
             if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
               mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
               if(dates.get(clGroup.trim()) != null ) {
            	   goes = dates.get(clGroup.trim());
            	   }
             }

             boolean green = false;
             if(datesgreen.get(Converter.cleanLitLatEst(clGroup.trim())) != null ) {
            	 green = datesgreen.get(Converter.cleanLitLatEst(clGroup.trim()));
          	   }
             
             if( type == 3 ){ // on kampaania
            	
            	 
            	 if(green) {
            		 
            	 if(sdate != null  && !sdate.toLocaleString().startsWith("1753")){
                	 mline.setStartDate(Converter.getDDsMMMsYYYYString(sdate));
                 }
            	 
            	
            		 if(date != null && !date.toLocaleString().startsWith("1753")){
                    	 mline.setEndDate(Converter.getDDsMMMsYYYYString(date));
                     }	 
            	 }else {
            		 mline.setProdLevel("pr");
            		 
            	 }
                 
                 
                 if(actions.get(Converter.cleanLitLatEst(clGroup.trim())) != null) {
                	 mline.setLineinfo(actions.get(Converter.cleanLitLatEst(clGroup.trim()))+"");
                 }
             }
             
             if( type == 1 && price  > 0) {
            	 if(date != null && !date.toLocaleString().startsWith("1753")){
                	 mline.setLineinfo(Converter.getDDsMMMsYYYYString(date));
                	 mline.setProdLevel("pr");
                 }
             }
             
            
             //pika hinna määramine
             
             if (prodID != null && prodID.trim().length() > 0) {
               mline.setProdID(prodID.trim());
             }
             if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
               mline.setProdGroup(Converter.cleanLitLatEst(prodGroup.trim()));
               mline.setDiscGroup(Converter.cleanLitLatEst(prodGroup.trim()));
             }

             if (price != 0) {
               mline.setPrice(price);

             }
             if (disc != 0) {
                 mline.setDiscount(disc);

               }
             if(mline.getPrice() > 0 || mline.getDiscount() > 0){
            	 
            	 
            	if(goes) {
            		  result.add(mline);    
            	   }
            	
             }
             
           }
           rs.close();
           stmt.close();
           con.close();
         }catch(Exception e){
        	 e.printStackTrace();
         } 
	System.out.println("matrix result " + result.size());
    return result;
      }

   private String turn(String aa) {
	   String res = "";
	   res = aa.substring(8, 10) + "-" + aa.substring(5, 7) + "-" + aa.substring(0, 4);
	   return res;
   }

   private Connection getNewConnection2() throws SQLException{
    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsna.user"), getParent().getProps().getProperty("dsna.pass"));
  }

   public Vector getLastThreeData() {

		Vector result = new Vector();
		try {
		

					String sq3 = "SELECT * FROM InvoiceLastFive order by kuupaev desc,arvenr ";
					
					Connection con3 = getNewConnection2();
					Statement st3 = con3.createStatement();
					ResultSet rs3 = st3.executeQuery(sq3);
					while (rs3.next()) {
						MatrixLine ml = new MatrixLine();
						ml.setCustID(rs3.getString("klkood").trim());
						ml.setStartDate(rs3.getString("kuupaev").substring(5,10));
						ml.setLineinfo(rs3.getString("arvenr").trim());
						double kordaja = 1;
						String liik  = rs3.getString("liik").trim();
						if(liik != null & liik.equals("kreedit")) {
							kordaja = -1;
						}
					 	ml.setProdID(rs3.getString("toode").trim());
						ml.setPrice(rs3.getDouble("kogus")* kordaja);
						ml.setDiscount(rs3.getDouble("price"));
						
						result.add(ml);
					}
					rs3.close();
					st3.close();
					con3.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}





	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
	  
	     }

	
	 public String getProductSortingData(){
	   	return null;
	   }

   public ManagerData getManagerData() throws SQLException{
     return null;
  }

   
   
   public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		
		try {
			
			
			String  cmd = " select * from CustomerFixedAssortments ";
  		
  		         if (isDebug()) System.out.println(cmd);
  		         StringBuffer products = new StringBuffer();
  		      
  		        	 Connection con = getNewConnection2();
  		           Statement st = con.createStatement();
  		           ResultSet rs = st.executeQuery(cmd);

  		           while (rs.next()) {

  		             String sortid = rs.getString("sortid").trim()+"x";
  		             String prodID = rs.getString("prodid").trim();
  		             int optio = rs.getInt("optionalitem");
  		             
  		   
						MatrixLine mline = new MatrixLine();
						

								if (sortid != null
										&& sortid.trim().length() > 1) {
									mline.setCliGroup(Converter.cleanLitLatEst(sortid.trim()).replaceAll(";", ":"));
								}

								if (prodID != null
										&& prodID.trim().length() > 0) {
									mline.setProdID(prodID.trim());
								}
								
								if(optio == 1) {
									mline.setLineinfo("CENTRAL"); // valikuline optional item
								}
								
								
								result.add(mline);
							}
						
					 rs.close();
					 st.close();
					con.close();
				
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.exit(0);

		System.out.println("fix size " + result.size());
		// System.exit(0);
		return result;

	
}
  


       public String getSaveStockInfo(){

 	  	  
    		  String  cmd = " select * from stock ";
    		  if(usedProds == null){
    			  this.getProducts(null);
    		  }
    		         if (isDebug()) System.out.println(cmd);
    		         StringBuffer products = new StringBuffer();
    		         try {
    		        	 Connection con = getNewConnection2();
    		           Statement st = con.createStatement();
    		           ResultSet rs = st.executeQuery(cmd);

    		           while (rs.next()) {

    		             String kood = rs.getString("kood").trim();
    		             double seis = rs.getDouble("kogus");
    		             String ladu = rs.getString("ladu").trim();
    		          
    		             
    		             if(seis > 0 && usedProds.contains(kood) ){
    		            	 if(ladu.equals("SMART-H")) {
    		                     products.append(kood +  ";");
        		                 products.append(" ;");
        		                 products.append(getParent().getNF().format(seis) + "; \n"); 
        		        	 }
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


   public Vector getDebt(String agentCode){
        return this.debtList;
  }

   public void connectionDown(){
   }

   public void connectionUp(){
   }

           public String getDealerCustomerCSV(String agent){
            return null;
            }
           
           
           public TreeMap getDealers() {
        	 
      	         return null;
        	    }
           
           public String  getCustProdDiscounts(Agent agent){
            return null;
            }

           
           public TreeMap getProdGroups() throws SQLException {
        	     TreeMap   tm = new TreeMap();
        	   //  setTreeGroup(true);
        	     String cmd = " select left(kood,3) as kood  ,max(nimi)as nimi from prodgroups group by left(kood,3)";
        	     	  try {
        	       	 Connection con = getNewConnection2();
        	       	System.out.println(cmd);
        	   	        Statement stmt = con.createStatement();
        	   	        ResultSet rs = stmt.executeQuery(cmd);
        	   	        while(rs.next()){
        	   	            String id = Converter.cleanLitLatEst(rs.getString("kood").trim().replaceAll(";", ":"));     
        	                String name = rs.getString("nimi").trim() ;
        	                
        	                if(name.indexOf(" ") != -1) {
        	                	name = name.substring(0,name.indexOf(" "));
        	                }
        	            
        	                tm.put(id, name.trim());  	             
        	   	        
        	   	     }
        	   	      rs.close();
        	   	      con.close();

        	        } catch (Exception e){
        	          e.printStackTrace();
        	        }
        	    
        	         return tm;
        	   }

           
           

         
           public TreeMap getProdGroupsSecond() throws SQLException {
        	     TreeMap   tm = new TreeMap();
          	   //  setTreeGroup(true);
          	     String cmd = " select * from countries ";
          	     	  try {
          	       	 Connection con = getNewConnection2();
          	       	System.out.println(cmd);
          	   	        Statement stmt = con.createStatement();
          	   	        ResultSet rs = stmt.executeQuery(cmd);
          	   	        while(rs.next()){
          	   	            String id = Converter.cleanLitLatEst(rs.getString("code").trim().replaceAll(";", ":"));     
          	                String name = rs.getString("name") ;
          	                 tm.put(id, name.trim());  	             
          	   	        }
          	   	      rs.close();
          	   	      con.close();

          	        } catch (Exception e){
          	          e.printStackTrace();
          	        }
          	    
          	         return tm;
             }


           
   private TreeMap teised = new TreeMap();
   
   public TreeMap getAgents(){
	   StringBuffer sb2 = new StringBuffer();
	 //  getLastThreeData();
	    String cmd = "select * from agents ";
        
	    if (isDebug()) System.out.println(cmd);
	    TreeMap agents = new TreeMap();
	    try {
	    	

	 	    	
	    	
	        Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	          Agent agent = new Agent(rs.getString("kood").trim());
	          String name = rs.getString("nimi").trim();
	          agent.setObject(rs.getString("objekt"));
	          agent.setName(name);

	          agent.setLanguage("EST");
	          agent.setCountry("ee");
	          agent.setPassWord("0000");
	          String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
	          if(email.length() > 1){
	        	  agent.setEmail(email);
	        	  agent.setActive(true);
	          }
	         
	          
	          
	  		
			    sb2.append(agent.getID() + ";" + name + "\n");  
	          agents.put(name, agent);
	          agents1.put(agent.getID(), agent);
	        }

	        Agent aa = new Agent("HOUSE1");
	        aa.setName("office1");
	        agents.put("office1", aa);
            rs.close();
            stmt.close();
	        con.close();            
	       
	    } catch (Exception e){
	        getParent().showError(e);
	       
	      }
	    
	    if(sb2.length() > 2){
	    	try{
	    		// Converter.saveFileInUTF8( "agent.txt", Converter.enCodeEst(sb2.toString()));
	    	}catch (Exception e){
	    	}
	      }

	    Agent aa = new Agent("MERC1");
        aa.setName("MERC1");
        agents.put("MERC1", aa);
        agents1.put(aa.getID(), aa);
        
        Agent aa2 = new Agent("MERC2");
        aa2.setName("MERC2");
        agents.put("MERC2", aa2);
        agents1.put(aa2.getID(), aa2);
        
        Agent aa3 = new Agent("MERC3");
        aa3.setName("MERC3");
        agents.put("MERC3", aa3);
        agents1.put(aa3.getID(), aa3);
        
        Agent aa4 = new Agent("MERC4");
        aa4.setName("MERC4");
        agents.put("MERC4", aa4);
        agents1.put(aa4.getID(), aa4);
	 
        Agent aa5 = new Agent("MERC5");
        aa5.setName("MERC5");
        agents.put("MERC5", aa5);
        agents1.put(aa5.getID(), aa5);
	  // this.setBulkSales(agents);
     return agents;
   }

       public String getDistribution() {
         return null;
       }
   
  private HashSet sorts = new HashSet();
     
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

  private TreeMap  getOpenOrders(){
		TreeMap res = new TreeMap();
	

		   String sql2 =  "select  tellimusenr AS id, max(tellimuseseeria) as tyyp, klkood AS kkood , "+ 
" tellimusenr as number ,kuupaev AS kuup  "+
"from OpenOrders group by tellimusenr,klkood ,kuupaev order by klkood,tellimusenr " ;
		   
		   
	
	 this.connectionUp();
	try {
	   nf.setMinimumFractionDigits(2);
	   Connection con = this.getNewConnection2();
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
		sb.append(turn(rs.getString("kuup").trim().substring(0,10)) + " " + text + " ");
		
    	 sb.append("|" );	
		
	  
	    String sql3 = "select kogus as amount,  'pdl' as unit  , t.nimi as name from  " +
				   " OpenOrders rid  inner join  products t on t.toode = rid.toode where   tellimusenr =  '" + id + "'";
	 //   System.out.println("sql 3 " + sql3 );
	    Connection cc = this.getNewConnection2();
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
		
		return res;
	}
	

  
  private boolean addDebt(String id) {
	  
	  if(id.equals("S6807") ) {
		  return false;
	  }
	  if(id.equals("S6708") ) {
		  return false;
	  }
	  if(id.equals("S6021") ) {
		  return false;
	  }
	  if(id.equals("S6641") ) {
		  return false;
	  }
	  if(id.equals("S6891") ) {
		  return false;
	  }
	  
	 /* S6807 maxima   
	  S6708 selver
	  S6021 rimi
	  S6641 prisma
	  S6891  abc*/
	  
	  return true;
  }
  
  
  Vector nodebt = new Vector();
  
       public TreeMap getCustomers(Agent agent){
    	   
    	 TreeMap webmerc1 =  Converter.getWebMercagents(rcompany, user, pass,false,"MERC1");
    	 TreeMap webmerc2 =  Converter.getWebMercagents(rcompany, user, pass,false,"MERC2");
    	 TreeMap webmerc3 =  Converter.getWebMercagents(rcompany, user, pass,false,"MERC3");
    	 TreeMap webmerc4 =  Converter.getWebMercagents(rcompany, user, pass,false,"MERC4");
    	 TreeMap webmerc5 =  Converter.getWebMercagents(rcompany, user, pass,false,"MERC5");
    	 Calendar now = Calendar.getInstance();
    	 long en = now.getTimeInMillis();
    	 now.add(Calendar.DAY_OF_MONTH, -32);
    	 long st = now.getTimeInMillis();
    	 HashSet contacted = new HashSet();
    	 JSONArray dayr =  Converter.getWebDayReport(st, en, rcompany, user, pass);
    	 
    	// System.out.println("dayr " + dayr);
    	 int lennx = dayr.length();
			for (int u = 0; u < lennx; u++) {
         	JSONObject gr = null;
			try {
				gr = dayr.getJSONObject(u);
				 String key = (String)gr.get("custid");
				
	           String agentname = null;
			    try {
				  agentname = (String)gr.get("agent");
				  agentname = agentname.replaceAll("\t", ",");
			     }catch (Exception e){
			     }
			    if(agentname != null && !agentname.contains("MERC")) {
			    	 contacted.add(key);
			    }
			   
			    
			    if(key.equals("S7774AZ")) {
			    	System.out.println("S7774AZ " + agentname + " " + gr.toString());
			    }
	         	
			} catch (JSONException e) {
			  e.printStackTrace();
			}
         
			}
    System.out.println("contacted "+ contacted.size()); 
      try{
       	   Converter.getManagersReplacements(agents1, rcompany, user, pass); 
       		
       }catch(Exception e){
       	e.printStackTrace();
       }
    	   
    	   TreeMap openOrders = this.getOpenOrders();

    	/// KLIENDI ANDMETE LEIDMINE
    	  	  if(debtList == null) {
    	   try {
     	      String cmd = "  select * from Receivables " ;
        	  System.out.println(cmd);
     	 
     	       this.debtList = new Vector();
     	      Connection conv = getNewConnection2();
     	       Statement stmt1 = conv.createStatement();
     	       ResultSet rs1 = stmt1.executeQuery(cmd);
     	       while(rs1.next()){
     	        Debt debt = new Debt();
     	      
     	        debt.setCustID(rs1.getString("klient"));
     	        debt.setPayerID(rs1.getString("maksja").trim());
     	        debt.setDoknr(rs1.getString("number"));
     	        debt.setDate(rs1.getDate("kuupaev"));
     	        debt.setSum(rs1.getDouble("summa"));
     	        debt.setDue(rs1.getInt("due") + "");
     	        debt.setDuedate(rs1.getDate("duedate"));
       	        
     	        
     	        if(addDebt(debt.getPayerID()) && !nodebt.contains(debt.getPayerID())) {
     	        	  this.debtList.add(debt);
     	        }
     	      
     	        
     	            }

     	       rs1.close();
     	       stmt1.close();
     	       conv.close();
     	      } catch (Exception e){
     	        e.printStackTrace();
     	      }
    	   
    	   }
    	   
	    
        TreeMap actions = new TreeMap();
        
        
        try {

	      String cmd = "  select * from CustomerCampaign order by sellingendday desc" ;
									
  	  System.out.println(cmd);
  	     
  	      Connection conv = getNewConnection2();
  	       Statement stmt1 = conv.createStatement();
  	       ResultSet rs1 = stmt1.executeQuery(cmd);
  	       while(rs1.next()){
  	          
  	    	 String klkood = rs1.getString("klkood");
  	    	String camp = rs1.getString("Campaign");
  	          
  	          if(klkood != null && klkood.trim().length() > 0 && camp != null && camp.trim().length() > 0  ) {
  	        	  if(actions.get(klkood.trim()) != null) {
  	        		  actions.put(klkood.trim(), actions.get(klkood.trim()) + "|" + Converter.cleanLitLatEst(camp));
  	        	  }else {
  	        		  actions.put(klkood.trim(), Converter.cleanLitLatEst(camp));
  	        	  }
  	          }
  	        
  	         }

  	       rs1.close();
  	       stmt1.close();
  	       conv.close();
  	      } catch (Exception e){
  	        e.printStackTrace();
  	      }
        
 TreeMap fixsort = new TreeMap();
        
        
        try {

	      String cmd = "  select * from CustomerAssortments " ;
									
  	  System.out.println(cmd);
  	       
  	      Connection conv = getNewConnection2();
  	       Statement stmt1 = conv.createStatement();
  	       ResultSet rs1 = stmt1.executeQuery(cmd);
  	       while(rs1.next()){
  	          
  	    	 String klkood = rs1.getString("custid");
  	    	String fixx = rs1.getString("sortid");
  	          
  	          if(klkood != null && klkood.trim().length() > 0 && fixx != null && fixx.trim().length() > 0  ) {
  	        	  fixx = Converter.cleanLitLatEst(fixx.trim().replaceAll(";", ":")+"x");
  	        	  if(fixsort.get(klkood.trim()) != null) {
  	        		fixsort.put(klkood.trim(), fixsort.get(klkood.trim()) + "|" + fixx);
  	        	  }else {
  	        		fixsort.put(klkood.trim(), fixx);
  	        	  }
  	          }
  	        
  	         }

  	       rs1.close();
  	       stmt1.close();
  	       conv.close();
  	      } catch (Exception e){
  	        e.printStackTrace();
  	      }
        
  	  
        String sql3 =  "select * from Balance where used != 0" ;

        TreeMap debtsover  = new TreeMap();
        TreeMap debtsall  = new TreeMap();
    try {
      Connection con3 = getNewConnection2();
      Statement stmt3 = con3.createStatement();
      ResultSet rs3 = stmt3.executeQuery(sql3);
      while(rs3.next()){
    	  String kl = rs3.getString("kood");
    	//  double sum = rs3.getDouble("koik");
         double yle = rs3.getDouble("yle");
         double used = rs3.getDouble("used");
         
        if(addDebt(kl)) {
         if(used !=0){
        	 debtsall.put(kl, used);
         }
         
         if(yle !=0){
        	 debtsover.put(kl, yle);
         }
        }
      }

rs3.close();
stmt3.close();
con3.close();
    } catch (Exception e){
         e.printStackTrace();
       }



	    
	    
	    
	    
    	     TreeMap tm = new TreeMap();
    	     this.customers = new ArrayList();
    	     HashSet payers = new HashSet();
    	     Connection con = null;
			try {
				con = getNewConnection2();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

    	     String sqlp =  "  select payer,count (payer) from Customers  group by payer " + 
    	     		" having count (payer) > 1   " ;
    	     try {
    	    	 
       	        Statement stmt = con.createStatement();
       	        ResultSet rs = stmt.executeQuery(sqlp);
       	        while(rs.next()){
       	        	payers.add(rs.getString("payer").trim());
       	        }
       	        rs.close();
       	        stmt.close();
       	       
       	     
    	     }catch(Exception l) {
    	    	 
    	     }
     	      

    	   String sql =  "  select * from Customers   " ;

    	   int minordsum = 0;
    	   
    	   if( parent.getProps().getProperty("minordsum") != null){
    		   try{
    			   minordsum = Integer.parseInt(parent.getProps().getProperty("minordsum"));
    		   }catch(Exception e){
    		   }
    	   }
    	   
    	   System.out.println(sql);
    	   try {
    	      
    	        Statement stmt = con.createStatement();
    	        ResultSet rs = stmt.executeQuery(sql);
    	        while(rs.next()){
    	         String name = rs.getString("nimi").trim();
    	   
    	         int namelen = name.length();
    	         name = name.replaceAll("\"", " ");
    	           String id = rs.getString("kood").trim();
    	           name = name + " " + id;
    	         
    	           Customer customer = new Customer(id);
    	           
    	           String payer = null;
    	           if (name.equals("")) name = "<" + id + ">";
    	             customer.setName(Converter.enCodeEst(name));
    	             
    	             payer = rs.getString("payer");
    	          if(payer != null && payer.trim().length()>0){
    	           	 payer = payer.trim();
    	             customer.setPayer(payer);
    	            // customer.setRegnr(payer);
    	             }
                   
    	          
    	           	if(minordsum > 0){
                	   customer.setMinsum(minordsum);
                   }
    	           	
                   
    	             String aadress = rs.getString("aadress");
    	           
    	             if( aadress != null && aadress.trim().length()>0){
    	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress.trim()),"Estonia"));
    	               name = name + " " + Converter.enCodeEst(aadress.trim());
    	              customer.setName(Converter.enCodeEst(name));
    	             }
    	             if(!contacted.contains(customer.getID())) {
    	            	 customer.diffBack2 = true;
    	            	 customer.needsToBeVisited = true;
    	             }
    	         
    	         	String ag = rs.getString("agent").trim();
    	             
    	             String sbb = rs.getString("blokk");
    	             if(sbb.equals("1")){
    	               customer.setSalesBlock(true);
    	             }
    	             if(sbb.equals("2")){
      	               customer.setSalesBlockT(true);
      	             }
    	             	 
    	             
    	         if(fixsort.get(customer.getID())!= null){
    	        	StringTokenizer st2 = new StringTokenizer(fixsort.get(customer.getID())+"" ,"|"  ) ;
    	        	while(st2.hasMoreTokens()) {
    	        		customer.addFixSortCode(st2.nextToken());
    	        	}
    	         }
    	      
    	         	
        	             
    	         	customer.addAgent(new Agent(ag));
    	             customer.setMainAgent(ag.trim());
    	             Agent aa1 = (Agent)agents1.get(ag.trim());
    	           	if(aa1 != null) {
    	           	
    	       		Vector vec = aa1.getDubbler();
    	       		if(vec!= null && vec.size() >0){
    	       			Enumeration en2 = vec.elements();
    	       			while(en2.hasMoreElements()){
    	       				String age = (String) en2.nextElement();
    	       				customer.addAgent(new Agent(age));
    	       			}
    	       		}
    	           }
    	             
    	             if(webmerc1.get(customer.getID()) != null) {
    	            		 customer.addAgent(new Agent("MERC1")); 
    	              }
    	             if(webmerc2.get(customer.getID()) != null) {
	            		 customer.addAgent(new Agent("MERC2")); 
	                  }
    	             if(webmerc3.get(customer.getID()) != null) {
	            		 customer.addAgent(new Agent("MERC3")); 
	                  }
    	             if(webmerc4.get(customer.getID()) != null) {
	            		 customer.addAgent(new Agent("MERC4")); 
	                  }
    	             
    	             if(webmerc5.get(customer.getID()) != null) {
	            		 customer.addAgent(new Agent("MERC5")); 
	                  }
    	             if (ag != null ){
	           				Agent agg = ((Agent)this.getAgents1().get(ag));
	         				
	           				if(agg != null && agg.getDubbler().size()>0){
	           					//System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
	           					for (int r = 0 ; r < agg.getDubbler().size() ; r++){
	           						Agent dub = new Agent((String)agg.getDubbler().elementAt(r));
	           						customer.addAgent(dub);
	           					}
	           				}
	           			}
    	             
    	             
    	             if (customer.getID().equals("99978")){// uus klient lisame kõik 
    	 			          Enumeration enums = getParent().getAgentsHash().elements();
    	 			          while (enums.hasMoreElements()) {
    	 			            Agent a = (Agent) enums.nextElement();
    	 			            customer.addAgent(a);
    	 			          }
    	 			}
                  
    	         	
    	         	
    	            String paym = rs.getString("payment");

    	           	String num = rs.getString("alekood");
    	           	if(num != null && num.trim().length()>0){
    	             customer.setDiscount(Double.parseDouble((num.trim())));
    	           	}
    	           	
    	           if(	!nodebt.contains(customer.getPayer())) {
    	        	   int kred = rs.getInt("krediit");
       	           	if(kred>0){
       	              customer.setCredit(kred + "");
       	           	}   
    	           }
    	           	
    	           	
    	
    	        	String visit = rs.getString("visit");
   	             if(visit != null && visit.trim().length()>0 && !visit.trim().toLowerCase().equals("x")){
   	                customer.setVisit(Converter.getVisitDay(visit.trim()));
   	             }

    	      
    	          
    	           if(paym != null && paym.trim().length()>0){
    	        	   paym = paym.replaceAll("P"," ");
    	               paym = paym.replaceAll("ETTEMAKS","-1").trim();
    	               paym = paym.replaceAll("EMAKS","-1").trim();
    	               try{
    	            		customer.setPaymTerm(Integer.parseInt(paym.trim())+ "");
    	               }catch(Exception e){
    	            	   //e.printStackTrace();
    	               }
    	        
    	             }
    	           if(	!nodebt.contains(customer.getPayer())) {
      	        	 
    	           if(debtsall.get(customer.getPayer()) != null){
    	        	   customer.setUsedCredit(Double.parseDouble(debtsall.get(customer.getPayer())+""));
    	           }
    	           
    	           if(debtsall.get(customer.getID()) != null){
    	        	   customer.setUsedCredit(Double.parseDouble(debtsall.get(customer.getID())+""));
    	           }
    	           
    	           if(debtsover.get(customer.getPayer()) != null){
    	        	   customer.setDebt(Double.parseDouble(debtsover.get(customer.getPayer())+""));
    	           }
    	           
    	           if(debtsover.get(customer.getID()) != null){
    	        	   customer.setDebt(Double.parseDouble(debtsover.get(customer.getID())+""));
    	           }
    	           }
    	           
    	           if(openOrders.get(customer.getID()) != null){
    	        	   customer.addToCustDataVec(customer.getID() + ";tarasaldo; ;Open Order |"   +  openOrders.get(customer.getID()));
    	           }
    	           
    	          //
    	           String cont = rs.getString("kontakt");
    	           
    	           
    	           String all = rs.getString("sendall");
    	           if("1".equals(all)){
    	        	   Enumeration enums = getParent().getAgentsHash().elements();
    	               while (enums.hasMoreElements()) {
    	                 Agent a = (Agent) enums.nextElement();
    	                 customer.addAgent(a);
    	               }
    	               customer.setMainAgent(" ");
    	           }

    	           
    	           String alegr = rs.getString("alegr");
    	           if(alegr != null && alegr.trim().length()>0){
    	        	customer.setGroup(Converter.cleanLitLatEst(alegr.trim()));
    	        	customer.setCustType(Converter.cleanLitLatEst(alegr.trim()));
    	           }
    	           
    	           
    	           String alegr2 = rs.getString("alegr2");
    	           if(alegr2 != null && alegr2.trim().length()>0){
    	             if(customer.getGroup().trim().length() >0){
    	        	 	customer.setGroup(customer.getGroup().trim() + "|" + Converter.cleanLitLatEst(alegr2.trim())); 	
    	        	  }else{
    	        	 	customer.setGroup(Converter.cleanLitLatEst(alegr2.trim())); 
    	        	  }
    	        	}
    	           
    	           
    	           if(actions.get(customer.getID()) != null) {
    	        	   if(customer.getGroup().trim().length() >0){
       	        	 	customer.setGroup(customer.getGroup().trim() + "|" + Converter.cleanLitLatEst((String)actions.get(customer.getID()))); 	
       	        	}else{
       	        	 	customer.setGroup(Converter.cleanLitLatEst((String)actions.get(customer.getID()))); 
       	        	}
    	           }
    	           

    	           String note = rs.getString("note");
    	           if(note != null && note.trim().length()>0){
    	        	customer.setCity(Converter.deCode(note.trim().replaceAll(";", ":")));
    	           }
    	           
    	        
    	           String email  = rs.getString("email");
    	           if(email != null && email.trim().length()>0){
    	        
    	    	       customer.setPersonalData (Converter.deCode(email.trim().replaceAll(";", ":")));   
    	        	  customer.setInfoEmail(email.trim().replaceAll(";", ",").replace('/',','));
    	           }
    	           
    	           
if(cont != null && cont.trim().length()>0){
    	        	   
    	        	   if(cont.contains(" 5")) {
    	        		   int a = cont.indexOf(" 5");
    	        		   cont = cont.substring(0,a) + "|Tel: " + cont.substring(a+2);
    	        	   }
    	        	   
    	        	   if(customer.getPersonalData() != null) {
    	        		   customer.setPersonalData(customer.getPersonalData() + "|" + cont.trim().replaceAll(";", ":"));   
    	        	   }else {
    	        		   customer.setPersonalData(cont.trim().replaceAll(";", ":"));     
    	        	   }
    	        	   
    	            
    	           }
    	           
    	           
    	           
    	           String chain = rs.getString("chain");
    	           if(chain != null && chain.trim().length()>0){
    	        	customer.setChain(Converter.deCode(chain.trim()));
    	           }
    	           
    	           String category = rs.getString("category");
    	           if(category != null && category.trim().length()>0){
    	        	customer.category = (Converter.deCode(category.trim()));
    	           }
    	           
    	            customer.setName(customer.getName() + "{" + namelen +"}");
    	           
    	            boolean goes = true;
    	            if(payers.contains(customer.getID())) {
    	            	//goes = false;  / hetkel väljas kuna tahtsid saad neid ja ise reguleerivad .
    	            }
    	           
    	            if(name.indexOf("X-TEGE") != -1) {
    	            	customer.noOrder = true;
    	            }
    	            
    	           if(goes) {
    	        	   tm.put(name, customer);
       	            this.customers.add(customer.getID());
       	           }
    	           
    	         }

    	        rs.close();
    	        stmt.close();
    	        con.close();



    	     } catch (Exception e){
    	       e.printStackTrace();
    	     }
    	   
    	   
Customer c1 = new Customer("MUUKON");
c1.setName("X-TEGEVUSED Kontor");
c1.noOrder = true;

Customer c2 = new Customer("MUULADU");
c2.setName("X-TEGEVUSED Ladu");
c2.noOrder = true;

Enumeration enums = getParent().getAgentsHash().elements();
while (enums.hasMoreElements()) {
  Agent a = (Agent) enums.nextElement();
  c1.addAgent(a);
}

 enums = getParent().getAgentsHash().elements();
while (enums.hasMoreElements()) {
  Agent a = (Agent) enums.nextElement();
  c2.addAgent(a);
}
tm.put("X-TEGEVUSED Kontor", c1);
   this.customers.add(c1.getID());
   tm.put("X-TEGEVUSED Ladu", c2);
   this.customers.add(c2.getID());
  		
    	   
    	     return tm;
    	   }

   public TreeMap getAgents1() {
	   
		return agents1;
	}
   

   private TreeMap agents1 = new TreeMap();
   private HashSet usedProds = null;
   private TreeMap eanprods = null;
   
   public TreeMap getProducts(Agent agent){
	   eanprods =  Converter.loadMore();
     TreeMap tm = new TreeMap();
      TreeMap  ladutr = new TreeMap();
      TreeMap  ladumuu = new TreeMap();
      HashSet laduhas = new HashSet();
       usedProds = new HashSet();
      HashMap prodHash = new HashMap();
      
      
     try {
   	  String cmd = "select * from Stock";

      	 Connection con = getNewConnection2();
  	        Statement stmt = con.createStatement();
  	        ResultSet rs = stmt.executeQuery(cmd);
  	        while(rs.next()){
  	              String	toode =rs.getString("kood").trim();
  	            double	seis = rs.getDouble("kogus");
  	          String ladu  =rs.getString("ladu").trim();
  	          
  	          if(ladu.equals("SMART-H")) {
  	           	ladutr.put(toode, seis + "");
  	          }
  	       
  	          if(!laduhas.contains(toode)){
    	          	laduhas.add(toode);
    	  	      }
  	  	      
  	          
  	        if(ladu.startsWith("SMART-A")){
  	          	ladumuu.put(toode, seis + "");
  	          }
  	          
  	        }

  	      rs.close();
  	      stmt.close();
  	      con.close();

  	    System.out.println(cmd);
   } catch (Exception e) {
       e.printStackTrace();
   }
 
     
    
     try {
    	 TreeMap grs = getProdGroups();
         TreeMap grs2 = getProdGroupsSecond();
      
     	String cmd = "select *  from Products"; // and t.[Unit Price] > 0
     	
      System.out.println(cmd);
      
      this.getProdGroups();
     	Connection con = getNewConnection2();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode").trim();
            Product prod = new Product(id);
         	String name = rs.getString("nimi").trim();
//         	double netto = rs.getDouble("netto");
         	String namesort = name;
         	int namelen = name.length();
         	name =   Converter.firstToUpperCase(name);
         
         	
         	
             double price = rs.getDouble("myygihind");
             if(price == 0){
            	 price = 1;
             }
                prod.setPrice(price);
                
                
                
              if(ladutr.get(id) != null ){
            	 // prod.setPartii("GAT-BABITE : " + ((int) new Double((String)ladutr.get(id)).doubleValue()));
            	  prod.setStock(new Double((String)ladutr.get(id)).doubleValue());
              }

              if(ladumuu.get(id) != null ){
            	  if(prod.getPartii() != null && prod.getPartii().length() > 1){
            		  prod.setPartii(prod.getPartii() +"|AKT. : " + ((int) new Double((String)ladumuu.get(id)).doubleValue()));
                        
            	  }else{
            		  prod.setPartii("AKT. : " + ((int) new Double((String)ladumuu.get(id)).doubleValue()));
                      		  
            	  }
            	//  prod.setStock(new Double((String)ladutr.get(id)).doubleValue());
              }
              
                String grupp = Converter.cleanLitLatEst(rs.getString("prodgroup"));
                 
              if(grupp != null && grupp.trim().length()>0 ){
                 prod.setGroup(grupp.trim().substring(0,3));  
                 if(grs.get(grupp.trim()) != null) {
               	  prod.setGroupname(grs.get(grupp.trim()) + "");
                }
               
              }
          
              String country = Converter.cleanLitLatEst(rs.getString("country"));
              
              if(country != null && country.trim().length()>0 ){
                 prod.setGroupSecond(country.trim());
                 if(grs2.get(country.trim()) != null) {
                	  prod.setGroupSecname(grs2.get(country.trim()) + "");
                 }
               
              }
              
            
                prod.setUnit(rs.getString("unit"));
                
              String ean = (rs.getString("ean"));
                 if (ean != null &&ean.trim().length()>3){
                   prod.setEAN(ean.trim());
                   eanprods.put(ean.trim(), prod.getID());
                   name = name + " " + ean.substring(ean.length()-4) + " ";
                 }
        

                 String status = (rs.getString("status"));
                 if (status != null && status.trim().length()>0){
                     if(status.equals("uudistoode")) {
                    	 prod.setStatusNew("1");
                     }
                     
                     
                     if(status.equals("lõpumüük")) {
                    	 prod.setStatusSellout("1");
                     }
                	
                 }
                 
                 
                 int top = rs.getInt("statustop");
                

                double	cases = rs.getDouble("kastis");
                	if(cases > 0){
                      prod.setInCases(cases);
                    }
                	
                	prod.setVat("20");
              
                   

                   String discgr = (rs.getString("discgroup"));
                   if (discgr != null && discgr.trim().length()>0){
                	   prod.setDiscgrupp(discgr.trim());
                   }

                   String liik = (rs.getString("liik"));
                   if (liik != null && liik.trim().length()>0){
                	   if(liik.trim().equals("IMPORT") || liik.trim().equals("LIVIKO")) {
                		   prod.setType("OMAD");
                	   }else {
                		   prod.setType("MUUD");
                	   }
                	  
                	  
                   }
                          
                   
                   int nautimus = rs.getInt("nautimus");
                   int gbox = rs.getInt("giftbox");
                   if(gbox == 1) {
                	   prod.setStatusGiftPack("1");
                   }
                   if(nautimus > 0 && top == 1) {
                	   prod.setStatusTop("12");
                   }else if(top == 1) {
                    	 prod.setStatusTop("1");
                     }else if(nautimus > 0) {
                    	 prod.setStatusTop("2");
                	   
                   }
                   
                 
                   
                   
                   
                   boolean goes = true;
                  
                   if(goes){
                	   usedProds.add(prod.getID());
                	   name = name + " {" + namelen + "}";
                	   prod.setName(Converter.enCodeEst(name));
                	   prod.setNameSorting(namesort);
                       tm.put(name, prod); 
                       prodHash.put(prod.getID(), prod);
                   }
              
                   
        }
  
      //  getAmountCampProdstring(prodHash);
        rs.close();
        
        
       
        
        cmd = " select clgroup as grupp ,prodid  as toode ,price as hind ,minamount as kogus,disc as perc from PriceMatrixData where [minamount] > 0  ";
       		 
       			
       		    StringBuffer buf = new StringBuffer();
       			try {
       				
       			
       			     stmt =  con.createStatement();
       			      rs = stmt.executeQuery(cmd);

       		           while(rs.next()){
       		        	        String  gruppx = rs.getString("grupp").trim();
       		                  	String  toodex = rs.getString("toode").trim();
       		                  	try{
       		                  		
       		                  	    double  hind  = rs.getDouble("hind");
       			                  	double  kogus = rs.getDouble("kogus");
       			                  	      buf.append(toodex+ "; ; ;" + NF.format(hind).replace(',', '.') + ";" + Converter.cleanLitLatEst(gruppx) + ";" + kogus + ";"  );
       			                          buf.append("\n");	
       			              		
       		                  	}catch(Exception e){
       		                  		
       		                  	}
       		                  }	
       		           rs.close();
       		           stmt.close();
       		           con.close();
       			}catch (Exception e ){
       				e.printStackTrace();
       			}
        
        
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
        
        
        stmt.close();
        con.close();
     } catch (Exception e){
       e.printStackTrace();
     }

     
    
     
     return tm;
   }

 
   
	public static void saveObject(Object obj, String file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(obj);
		oos.close();
	}
   
   public static String deCode(String res ){
  	return res;
	  }
	 


   public String getAssortmentStr(Agent agent){
    StringBuffer sb = new StringBuffer();
    return sb.toString();
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     HashMap kliendid = new HashMap();
     try {
    String cmd =  " select * from CustomerProductsList order by klient,toode,lastdate desc ";
    System.out.println(cmd);
   //   "klient","toode","kogus","viimane kuupï¿½ev","viimane kogus"
    Connection con = getNewConnection2();
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery(cmd);
    HashSet donehash = new HashSet();
    
    while(rs.next()){

             String custID = rs.getString("klient").trim();

                 String prodID = rs.getString("toode").trim();;

                 double amount = 0;
                 String lastdate = " ";
                 amount = rs.getDouble("kogus");
                 Date dd = new Date(rs.getDate("lastdate").getTime());
                 
               String date =  Converter.getDDsMMMString(dd);
                 
               //  lastdate = rs.getString().trim().substring(5,10);
                // lastdate = lastdate.substring(3,5)+ "-" + lastdate.substring(0,2);
                    Customer cust = (Customer)kliendid.get(custID);
                    if (cust == null){
                      cust = new Customer(custID);
                    }
                    
                   double lprice = rs.getDouble("lastprice");
                   String arve = rs.getString("docno");
               
                    
                    Product prod = new Product(prodID);
                    CustProd cp = new CustProd(custID, prodID,amount , 0, 0,date,lprice,0,0," ");
                     cp.setAbiKood(arve);
                     
                     if(!donehash.contains(custID +" "+ prodID)) {
                    	   cust.addProduct(prod,cp,false);
                           kliendid.put(custID, cust);
                           vec.addElement(cp); 
                           donehash.add(custID +" "+ prodID);
                     }
                  

           }
         rs.close();
         stmt.close();
         con.close();


           int counter = 0;
           int cou = 0;
          String oldklient = null;
          String olddate = " ";
          String oldtoode = null;

          String cmd2 =  "select * from CustomerProductsList order by klient, toode, lastdate desc";
          System.out.println(cmd2);
		   //   "klient","toode","kogus","viimane kuupï¿½ev","viimane kogus"
		    Connection con2 = getNewConnection2();
		    Statement stmt2 = con2.createStatement();
		    ResultSet rs2 = stmt2.executeQuery(cmd2);


  while (rs2.next()){
       String klient = rs2.getString("klient").trim();
               String toode = rs2.getString("toode").trim();
               String kogus = "0";
               String date = " ";
                  kogus = rs2.getDouble("kogus")+"";
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
                    if(!olddate.equals(date)){counter++;}
                     if (counter == 0){cp.setS1(kogus );}
                     if (counter == 1){cp.setS2(kogus );}
                     if (counter == 2){cp.setS3(kogus );}
                     if (counter == 3){cp.setS4(kogus );}
                     if (counter == 4){cp.setS5(kogus );}

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




   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "NaviLivikoEST";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }


   public double getSalary(String agentCode){
     return 0;
   }


   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private Vector debtList = null;
   private String url2 = "jdbc:odbc:navi";
   private NumberFormat NF = NumberFormat.getInstance(Locale.ENGLISH);
   
   public NumberFormat getNF() {
	return NF;
}
/**
 * @param nf The nF to set.
 */
public void setNF(NumberFormat nf) {
	NF = nf;
}

private TreeMap makePlans(){
	   TreeMap res = new TreeMap();
	  
	   try{
		
	   Calendar cal = Calendar.getInstance();
	   int month = (cal.get(Calendar.MONTH)+1);
	    int a = 0;
	   File f = new File (getExcelDir() + "budget.xlsx");
		  FileInputStream	 ff = new FileInputStream(f);
		XSSFWorkbook workbook = new XSSFWorkbook((ff));
		XSSFSheet sheet = workbook.getSheetAt(0);
	XSSFRow row = null; 
				ff.close();	
				if(sheet != null){
				
					
					for(int i = 0 ; i < sheet.getLastRowNum();i++){
						 row = sheet.getRow(i);
						if(row == null && a == 0){
							a = i;
						}else{
							a = sheet.getLastRowNum();
						}
					}

					for(int i = 1 ; i < a + 1;i++){
						 row = sheet.getRow(i);
						String agent = null;
						
						try {
							agent = (row.getCell((short) 0).getStringCellValue());
 					      } catch (Exception e) {
 						   e.printStackTrace();
 					      }
					//	System.out.println(agent);
						double plan = 0;
						try {
			    			plan = Double.parseDouble(row.getCell((short) month+1).getNumericCellValue()+ "");
						} catch (Exception e) {
	   					 e.printStackTrace();	
	   				  	 plan = Double.parseDouble(row.getCell((short) month+1).getStringCellValue());
						}
						
						if(agent != null){
						    res.put(agent.toUpperCase(),plan);
						}
 						
					}}
			   
	   }catch (Exception e){
		   e.printStackTrace();
	   }
		//System.out.println("jooksevplaan " + res.toString());
		
	   return res;
 }
 




public void setBulkSales(Map agents) {

	 
 	 try {
 	 	String cmd = " select * from BulkSales ";
 	  Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
                String agentID = rs.getString("agent").trim();
                double sales = rs.getDouble("summa");
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                  agent.setMonthSales((int)(sales));
                }
           }
	        rs.close();
	        con.close();
     } catch (Exception e){
       e.printStackTrace();
     }
}
 	 

 	 
 	public void setBulkSalesOld(Map agents) {

 		TreeMap plans = makePlans();
 		 int yy = Calendar.getInstance().get(Calendar.YEAR);
 	     int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
 	     int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
 		 
 	 	 try {
 	 	 	String cmd = " select * from BulkSales ";// and [Global Dimension 1 Code]='400'
 	 	  Connection con = getNewConnection2();
 		        Statement stmt = con.createStatement();
 		        ResultSet rs = stmt.executeQuery(cmd);
 		        while(rs.next()){
 	                String agentID = rs.getString("agent").trim();
 	                double sales = rs.getDouble("summa");
 	                Agent agent = (Agent)agents.get(agentID);
 	                if (agent != null){
 	                 // agent.setMonthSales((int)(sales));
 	                  agent.buffer = (int)(sales);

 	            	  double msales = agent.buffer;
 	        		  double psales = 0;
 	        		try{
 	        		  psales = Double.parseDouble(plans.get(agent.getID())+"");
 	        	  }catch(Exception e){
 	        		  e.printStackTrace();
 	        	  }
 	        	  double  tehtudsalperc = msales/psales;
 	        	  double plaanitavMyykSal = d2 * msales/dd;
 	        	  double  prognosalperc = plaanitavMyykSal/psales;
 	        	  
 	        	  if(tehtudsalperc > 10){
 	        		  tehtudsalperc = 0;  
 	        	  }
 	        	  if(prognosalperc > 10){
 	        		  prognosalperc = 0;  
 	        	  }
 	        	  
 	          String asi = "Sales: " + (int)msales + " €||";
 	          if(dd > 6){
 	        	  asi = asi + "Plan done/estimate :";
 	              asi = asi + "     " +(int)(tehtudsalperc*100) + "% / " + (int)(prognosalperc*100)  + " %||";  
 	          }
 	           asi = asi + "Sales plan: " + (int)psales + " €||";    
 	           agent.setStart( Converter.enCodeEst(asi ));
 	         //  System.out.println(Converter.enCodeEst(agentID + "' " +asi  + " "  + plans.get(agent.getID())) );
 	           
 	                  
 	                
 	                
 	                }
 	           }
 		        rs.close();
 		        con.close();
 	     } catch (Exception e){
 	       e.printStackTrace();
 	     }

 	 
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
//     	 Lõppes kustutamine 
		 
		 TreeMap prods = this.getProducts(null);

     StringBuffer buf = new StringBuffer();
     Iterator enume = prods.keySet().iterator();

     while (enume.hasNext()) {
	    	String asi = (String)enume.next();
	    	Product prod = (Product)prods.get(asi);
	    	String toode = prod.getEAN().trim();  
           if(toode.length() > 0){
            	
	   	  res2 = new File (pictssou);
          ff2 = res2.list();
         
     	 for(int i = 0;ff2.length>i;i++){
         	if( ff2[i].startsWith(toode)){
           // System.out.println("pilt olemas " + ff2[i]);
        	File in = new File ( pictssou + ff2[i]); 
          	if(in.isFile()){
          	File inpda = new File ( pictspda + prod.getID()+ ".jpg"); 
              	Converter.shrinkAndCopyFile(in,inpda,400);
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





@Override
public void PictureMaker() {
	makePictureCat();
	 System.exit(0);
	
}


}
