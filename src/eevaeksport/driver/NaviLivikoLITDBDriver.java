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

public class NaviLivikoLITDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager,PictureMaker {

  public NaviLivikoLITDBDriver(EksportPanel parent) {
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
	    rcompanyx = getParent().getProps().getProperty("rest.companyx");
	    nodebt.add("S6807"); // maxima
	    nodebt.add("S7189"); // rkiosk
	    nodebt.add("S6021"); // Rimi
	    nodebt.add("S6708"); // selver
	    nodebt.add("S9915"); // cirkle
	    nodebt.add("S6029"); // harju ty
	    nodebt.add("S6641"); // prisma
	    nodebt.add("S6891"); // Comarket
	  
  }

  Vector nodebt = new Vector();

  String user =  null;
  String pass = null;
  String rcompanyx = null;
  
  
   public String getPilotDayData(Agent agent){
     return null;
   }
   public String getSaveDebtInfo() {
   	return null;
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
	             Calendar dateC = Calendar.getInstance();
	             dateC.setTime(date);
	             Calendar sdateC = Calendar.getInstance();
	             sdateC.setTime(date);
	             
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
	                	 mline.setStartDate(Converter.getYYYY_MM_DDpString(sdateC));
	                 }
	            	 
	            	
	            		 if(date != null && !date.toLocaleString().startsWith("1753")){
	                    	 mline.setEndDate(Converter.getYYYY_MM_DDpString(dateC));
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

  		             String sortid = rs.getString("sortid").trim();
  		             String prodID = rs.getString("prodid").trim();
  		             
  		   
						MatrixLine mline = new MatrixLine();
						

								if (sortid != null
										&& sortid.trim().length() > 0) {
									mline.setCliGroup(sortid.trim());
								}

								if (prodID != null
										&& prodID.trim().length() > 0) {
									mline.setProdID(prodID.trim());
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
    		            	 if(ladu.equals("AKS")) {
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
        	     String cmd = " select kood as kood , (nimi)as nimi from prodgroups ";
        	     	  try {
        	       	 Connection con = getNewConnection2();
        	       	System.out.println(cmd);
        	   	        Statement stmt = con.createStatement();
        	   	        ResultSet rs = stmt.executeQuery(cmd);
        	   	        while(rs.next()){
        	   	            String id = Converter.cleanLitLatEst(rs.getString("kood").trim());     
        	                String name = rs.getString("nimi").trim() ;
        	                
        	                if(name.indexOf(" ") != -1) {
        	                //	name = name.substring(0,name.indexOf(" "));
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
          	   	            String id = Converter.cleanLitLatEst(rs.getString("code").trim());     
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

	          agent.setLanguage("LIT");
	          agent.setCountry("lt");
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
" tellimusenr as number ,kuupaev AS kuup , max(tellimusesumma) as summ "+
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
		sb.append("Sum: " + nf.format(rs.getDouble("summ")) + "€ ");
		
    	 sb.append("|" );	
		
	  
	    String sql3 = "select kogus as amount,  'pdl' as unit , price , t.nimi as name from  " +
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
		       + rs2.getString("unit").replaceAll("\\r\\n|\\r|\\n", " ").trim() 
		       + "(" + nf.format(rs2.getDouble("price")) + ") " 
		       + rs2.getString("name").toLowerCase().replaceAll("\\r\\n|\\r|\\n", " ").trim() + "|");
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
  
  
  
       public TreeMap getCustomers(Agent agent){
    	   
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
       	        
     	        
     	        if(addDebt(debt.getPayerID()) && !nodebt.contains(debt.getPayerID()) ) {
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

	      String cmd = "  select * from CustomerCampaign " ;
									
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
  	        		  actions.put(klkood.trim(),Converter.cleanLitLatEst( camp));
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
                       
        	        
        	           	
                       
        	             String aadress = rs.getString("aadress");
        	           
        	             if( aadress != null && aadress.trim().length()>0){
        	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress.trim()),""));
        	               name = name + " " + Converter.enCodeEst(aadress.trim());
        	              customer.setName(Converter.enCodeEst(name));
        	             }
        	   
    	         	String ag = rs.getString("agent").trim();
    	             
    	             String sbb = rs.getString("blokk");
    	             if(sbb.equals("1")){
    	               customer.setSalesBlock(true);
    	             }
    	             if(sbb.equals("2")){
      	               customer.setSalesBlockT(true);
      	             }

    	             if(sbb.equals("3")){
        	               customer.setSalesBlockPrepay(true);
        	             }

    	             
    	         if(fixsort.get(customer.getID())!= null){
    	        	StringTokenizer st = new StringTokenizer(fixsort.get(customer.getID())+"" ,"|"  ) ;
    	        	while(st.hasMoreTokens()) {
    	        		customer.addFixSortCode(st.nextToken());
    	        	}
    	         }
    	      
    	         	
        	             
    	         	customer.addAgent(new Agent(ag));
    	             customer.setMainAgent(ag.trim());
    	             
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
    	           
    	           
    	           
     	          minordsum = rs.getInt("minsum");
     	          
     	           	if(minordsum > 0){
                 	   customer.setMinsum(minordsum);
                    }
    	           
    	          //
    	           String cont = rs.getString("kontakt");
    	           if(cont != null && cont.trim().length()>0){
    	               customer.setPersonalData(cont.trim());   
    	           }
    	           
    	           String all = rs.getString("sendall");
    	           if("1".equals(all)){
    	        	   Enumeration enums = getParent().getAgentsHash().elements();
    	               while (enums.hasMoreElements()) {
    	                 Agent a = (Agent) enums.nextElement();
    	                 customer.addAgent(a);
    	               }
    	               customer.setMainAgent(" ");
    	           }

                 

    	        
    	       
    	           /*	      
    	           String delivery = rs.getString("delivery");
    	           if(delivery != null && delivery.trim().length()>0){
    	        	  delivery = delivery.replaceAll(";", ",").replaceAll("\\r\\n|\\r|\\n", " ").trim();
    	        	     if(customer.getCity()!= null && customer.getCity().trim().length() > 0 ){
        	        		   customer.setCity(customer.getCity() + "|DD:" + delivery); 
        	       	             }else{
        	        			   customer.setCity("DD:" + delivery );
        	       	         }
        	           }
    	        	

    	           String chain = rs.getString("chain");
    	           if(chain != null && chain.trim().length()>0){
    	           customer.setChain(chain.trim());   
    	           }
    	           
*/
    	           
    	           

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
    	        	   
    	        	customer.setCity(Converter.deCode(note.trim()));
    	        
    	           }
    	           

    	           String email  = rs.getString("email");
    	           if(email != null && email.trim().length()>0){
    	        	   
    	        	   if(customer.getPersonalData() != null) {

    	    	        	customer.setPersonalData (customer.getPersonalData() + "|" +Converter.deCode(email.trim()));
    	        	   }else {

    	    	        	customer.setPersonalData (Converter.deCode(email.trim()));   
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
    	   
    	   
    	  
  		
    	   
    	     return tm;
    	   }

   public TreeMap getAgents1() {
	   
		return agents1;
	}
   

   private TreeMap agents1 = new TreeMap();
   private HashSet usedProds = null;
   
   
   public TreeMap getProducts(Agent agent){
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
  	          
  	          if(ladu.equals("AKS")) {
  	           	ladutr.put(toode, seis + "");
  	          }
  	       
  	          if(!laduhas.contains(toode)){
    	          	laduhas.add(toode);
    	  	      }
  	  	      
  	          
  	        if(ladu.startsWith("LOBRS") && seis != 0){
  	          	ladumuu.put(toode, seis + "");
  	          }
  	          
  	        }

  	      rs.close();
  	      stmt.close();
  	      con.close();

  	    System.out.println("laod " + ladumuu.size() + " " + ladutr.size());
  	    
   } catch (Exception e) {
       e.printStackTrace();
   }
 
  
     try {
     	String cmd = "select * from Products"; // and t.[Unit Price] > 0
     	
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
        	name =   Converter.firstToUpperCase(name);
            name = name + " " + id ;
         	int namelen = name.length();
         	
             double price = rs.getDouble("myygihind");
             if(price == 0){
            	 price = 1;
             }
                prod.setPrice(price);
                
                
                
              if(ladutr.get(id) != null ){
            	 // prod.setPartii("GAT-BABITE : " + ((int) new Double((String)ladutr.get(id)).doubleValue()));
            	  prod.setStock(new Double((String)ladutr.get(id)).doubleValue());
              }

          
              
                String grupp = Converter.cleanLitLatEst(rs.getString("prodgroup"));
                 
              if(grupp != null && grupp.trim().length()>0 ){
                 prod.setGroup(grupp.trim());  
              }
          
              String country = Converter.cleanLitLatEst(rs.getString("country"));
              
              if(country != null && country.trim().length()>0 ){
                 prod.setGroupSecond(country.trim());  
              }
              
            
                prod.setUnit(rs.getString("unit"));
                
              String ean = (rs.getString("ean"));
                 if (ean != null &&ean.trim().length()>3){
                   prod.setEAN(ean.trim());
                   name = name + " " + ean.substring(ean.length()-4) ;
                 }
        
            
                 if(ladumuu.get(id) != null ){
               	  if(prod.getPartii() != null && prod.getPartii().length() > 1){
               		  prod.setPartii(prod.getPartii() +"|Br. : " + ((int) new Double((String)ladumuu.get(id)).doubleValue()));
                    }else{
               		  prod.setPartii("Br. : " + ((int) new Double((String)ladumuu.get(id)).doubleValue()));
                  }
               	     name = name + " @BR" + ((int) new Double((String)ladumuu.get(id)).doubleValue()) ; 
                 }
                 
               //  [status] [nvarchar](10) NULL, -- UUS VÄLI 'lõpumüük', 'uudistoode'
               //  [statustop] [int] NULL, -- UUS VÄLI 0-tava, 1-top toode

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
                 if(top == 1) {
                	 prod.setStatusTop("1");
                 }

                 double	maxdisc = rs.getDouble("maxdiscount");
             	if(maxdisc > -1){
             		if(maxdisc == 0 ) {
             			 prod.setMaxDiscount(-1);
                         
             		}else {
             			 prod.setMaxDiscount(maxdisc);
                         name = name  +  " MD-"  + (int)maxdisc + "%";
             		}
                  
                 }
                 
                double	cases = rs.getDouble("kastis");
                	if(cases > 0){
                      prod.setInCases(cases);
                    }
                	
                	prod.setVat("21");
              
                   

                   String discgr = (rs.getString("discgroup"));
                   if (discgr != null && discgr.trim().length()>0){
                	   prod.setDiscgrupp(discgr.trim());
                   }

                   String liik = (rs.getString("liik"));
                   if (liik != null && liik.trim().length()>0){
                	   prod.setType(liik.trim());
                   }
                     
                   
                   Date lattu = (rs.getDate("saabublattu"));
                   if (lattu != null ){
                	 if(lattu.getYear() > 100) {
                		 prod.setArrivesToStock(lattu.getTime());
                	 }
                	   
                   }
                   
                   String pant = (rs.getString("pant"));
                   if (pant != null && pant.trim().length()>0){
                	   prod.setPandPakendiKood(pant.trim());
                   }
                        
                          
                       
                
                   boolean goes = true;
                  
                   if(goes){
                	   usedProds.add(prod.getID());
                	   name = name + "{" + namelen + "}";
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
                 lastdate = rs.getString("lastdate").trim().substring(5,10);
                    Customer cust = (Customer)kliendid.get(custID);
                    if (cust == null){
                      cust = new Customer(custID);
                    }
                    
                   double lprice = rs.getDouble("lastprice");
                   String arve = rs.getString("docno");
               
                    
                    Product prod = new Product(prodID);
                    CustProd cp = new CustProd(custID, prodID,amount , 0, 0,lastdate,lprice,0,0," ");
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
     return "NaviLivikoLIT";
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
   private String url2 = "jdbc:odbc:navilit";
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
		System.out.println("jooksevplaan " + res.toString());
		
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
 	           System.out.println(Converter.enCodeEst(agentID + "' " +asi  + " "  + plans.get(agent.getID())) );
 	           
 	                  
 	                
 	                
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
