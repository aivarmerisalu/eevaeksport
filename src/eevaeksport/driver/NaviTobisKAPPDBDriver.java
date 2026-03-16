package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.Date;
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

import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>MOFF Prekegetproducts
 * 
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class NaviTobisKAPPDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public NaviTobisKAPPDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    parent.setAbiKood(true);
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	try {
	    Class.forName(databaseDriverName).newInstance();
	} catch (Exception e) {
	    parent.showError(e);
	}
  }

   public String getPilotDayData(Agent agent){
     return null;
   }
   public String getSaveDebtInfo() {
	 
		
   	return null;
   }

  


   public Vector getPriceMatrixData(){

    return new Vector();
      }

  
   private Connection getNewConnection2() throws SQLException{
    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsna.user"), getParent().getProps().getProperty("dsna.pass"));
  }


	public Vector getLastThreeData() {
		
		return null;
	}

	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){}

	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	 
	     }


	 public String getProductSortingData(){
	   	return null;
	   }


   public ManagerData getManagerData() throws SQLException{

    return null;

  }


   
   public Vector getFixedAssortmentData() {
           Vector lines = new Vector();
           return lines;
   }



   public DealerProduct DealerProductsList() {
     return null;
   }

   
   private void  saveDebtFile(String debtstring) {
	 
     }

   public void makeDebtFile(){
			  
	   
		          
		      }


       public String getSaveStockInfo(){
    			  
    	   String cmd = " SELECT [Item No_] as kood       ,[Quantity] as kogus  ,[Location code] " +
"  FROM [_MERISOFT$Tobis_Stock]";
   
    	  
    		   	  

    		          if (isDebug()) System.out.println(cmd);
    		          StringBuffer products = new StringBuffer();
    		          try {


    		            	 Connection con = getNewConnection2();
    		        	        Statement stmt = con.createStatement();
    		        	        ResultSet rs = stmt.executeQuery(cmd);

    		            while (rs.next()) {

    		              String kood = rs.getString("kood").trim();
    		              double seis = rs.getDouble("kogus");
    		              if(seis > 0){
    		                  products.append(kood +  ";");
    		                  products.append(" ;");
    		                  products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
    		              }
    		              }
    		              rs.close();
    		              stmt.close();
    		              con.close();
    		          } catch (Exception e) {
    		              getParent().showError(e);
    		          }
    		          
    		          makeDebtFile();
    		          
    		          return products.toString();
    		      }



   public Vector getDebt(String agentCode){
        return new Vector();
  }

   public void connectionDown(){
   }

   public void connectionUp(){
   }

           public String getDealerCustomerCSV(String agent){
            return null;
            }

           public String  getCustProdDiscounts(Agent agent){
            return null;
            }

   public TreeMap getProdGroups() throws SQLException {
     TreeMap   tm = new TreeMap();  
      return tm;
   }



   public TreeMap getProdGroupsSecond() throws SQLException {
    TreeMap   tm = new TreeMap();
        return tm;
  }



   public TreeMap getAgents(){


       String cmd = "  select * from [dbo].[_MERISOFT$Tobis_Cust_list] ";
    
       
	    if (isDebug()) System.out.println(cmd);
	    TreeMap agents = new TreeMap();
	    try {
	        Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	          Agent agent = new Agent(rs.getString("code").trim());
	          agent.setName(rs.getString("name").trim());
	          agent.setObject(rs.getString("cust_loc_code").trim());
	          agents.put(agent.getName()+agent.getID(), agent);
	         }

	         rs.close();
            stmt.close();
	        con.close();
	       
	    } catch (Exception e){
	        getParent().showError(e);
	      }


     return agents;
   }

       public String getDistribution() {
         return null;
       }
       


       public TreeMap getCustomers(Agent agent){

    	     TreeMap tm = new TreeMap();
    
    	   String sql =  " select * from [dbo].[_MERISOFT$Tobis_Cust_list] " ;

    	   System.out.println(sql);
    	   try {
    	        Connection con = getNewConnection2();
    	        Statement stmt = con.createStatement();
    	        ResultSet rs = stmt.executeQuery(sql);
    	        
    	        while(rs.next()){
    	           Customer customer = new Customer( rs.getString("code").trim());
    	           customer.setName(rs.getString("name").trim());
	               customer.addAgent(new Agent(customer.getID()));
    	            tm.put(customer.getID() + customer.getName(), customer);
    	          
    	           
    	         }

    	        rs.close();
    	        stmt.close();
    	        con.close();

    	     } catch (Exception e){
    	       e.printStackTrace();
    	     }
  
    	     return tm;
    	   }

  
   
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();

    
     try {
           
    	 String cmd = "select item_code as kood , Description as name, [No_ 2] as ean,[Unit Price] as hind,[Base unit of Measure] as unit from [dbo].[_MERISOFT$Tobis_Cust_Items]";
      
    	 System.out.println(cmd);
     	 con = getNewConnection2();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("kood").trim();
            Product prod = new Product(id);
      	   prod.setName(Converter.enCodeEst(rs.getString("name").trim()));
         	
         	  String ean = (rs.getString("ean"));
              if (ean != null && ean.trim().length()>0){
                prod.setEAN(ean);
              }
              prod.setPrice(rs.getDouble("hind"));
              prod.setUnit(rs.getString("unit").replaceAll("²", "x2"));
                	
                       tm.put(prod.getName(), prod);  
                      
                   }
          
      
        rs.close();
        stmt.close();
        con.close();
                 
        
     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }



   public static String deCode(String res ){
	  	return res;
	  }
	 


   public String getAssortmentStr(Agent agent){
   return null;
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     return vec;
   }




   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "NaviTobisKAPP";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }


   public double getSalary(String agentCode){
     return 0;
   }


   private String subjectFilesTo = null;

  
   
   private String url2 = "jdbc:odbc:NAV40";
   private Connection con;
   private NumberFormat NF = NumberFormat.getInstance(Locale.ENGLISH);
  
 

   public NumberFormat getNF() {
	return NF;
}

   
public void setNF(NumberFormat nf) {
	NF = nf;
}


  public void setBulkSales(Map agents) {

  }
}
