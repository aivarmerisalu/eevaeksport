package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.EksportPanel;
import eevaeksport.Product;
import eevaeksport.imp.driver.IceAxaImportDriver;
import eevaeksport.manager.ManagerData;


/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class ScalaEugEstWareDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater {
  public ScalaEugEstWareDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
    
}
   

  /**
   *
   */
  
  public static final String cleanLitLatEst(String ret){
	    if (ret != null){
	    
	        ret = ret.replaceAll("�","e");
	        ret = ret.replaceAll("�","I");
	        ret = ret.replaceAll("�","u");
	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","z");
	        ret = ret.replaceAll("�","s");
	        ret = ret.replaceAll("�","S");
	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","I");
	        ret = ret.replaceAll("�","u");
	        ret = ret.replaceAll("�","i");
	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","Z");
	        ret = ret.replaceAll("�","E");
	        ret = ret.replaceAll("�","U");
	        ret = ret.replaceAll("�","U");

	        ret = ret.replaceAll("�","Z");
	        ret = ret.replaceAll("�","A");
	        ret = ret.replaceAll("�","a");
	        ret = ret.replaceAll("�","C");

	        ret = ret.replaceAll("�","c");
	        ret = ret.replaceAll("�","E");
	        ret = ret.replaceAll("�","e");
	        ret = ret.replaceAll("�","G");
	        ret = ret.replaceAll("�","g");
	        ret = ret.replaceAll("�","I");
	        ret = ret.replaceAll("�","i");
	        ret = ret.replaceAll("�","K");
	        ret = ret.replaceAll("�","k");
	        ret = ret.replaceAll("�","L");
	        ret = ret.replaceAll("�","l");
	        ret = ret.replaceAll("�","N");
	        ret = ret.replaceAll("�","n");
	        
	        ret = ret.replaceAll("�","o");
	        ret = ret.replaceAll("�","a");
	        ret = ret.replaceAll("�","o");
	        ret = ret.replaceAll("�","u");
	        
	    	
	    }
	        
	        if(ret == null || ret.trim().length()==0){
	        return " ";
	        }else {
	        return ret;
	        }        
	      }
  
  public void connectionUp(){
  }

  
  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  
  private void insertInventory(HashMap has){
	  	if(has != null){
	  	
	  		Iterator iter = has.keySet().iterator();
	  		while(iter.hasNext()){
   String id = (String)iter.next();
   String value = (String)has.get(id);
	  	 try{
	  		 
	  			 	con = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));	  	
	  				  String sql3 = "update OR010100 set OR01185 = " + value + " where OR01001  = '" + id + "'"  ;
			          PreparedStatement st = con.prepareStatement(sql3);
			          System.out.println(sql3 );
			           log.info(  sql3 );			     
			           st.executeUpdate();
			           
			           sql3 = "update OR200100 set OR20185 = " + value + " where OR21001  = '" + id + "'"  ;
			           st = con.prepareStatement(sql3);
			           log.info(  sql3 );	
			           st.executeUpdate();
			           
			           st.close();	   		        
	  			 }catch (Exception e){
	  			 	e.printStackTrace();
	  			 }
	  		  }
	  	  }
	  }

	  
	
  
  
  public String getSaveDebtInfo() {
      File dir = new File("import");
      File[] files = dir.listFiles();
      if(files != null){
      
      for (int i = 0; i < files.length ; i++){
        File file = files[i];
        if (file.isFile()){
          HashMap vec = null;
          try {
          ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
         
			
				vec = (HashMap)ois.readObject();
				ois.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           insertInventory(vec);
           File done = new File("import/done/" + Calendar.getInstance().getTimeInMillis()+"-"+file.getName());
           if(done.exists()){
           	done.delete();
           }
          file.renameTo(done);
           file.delete();
          
        }    
      }	
      }
      //getSaveStockInfo();
      System.exit(0);
  	
  	return null;
     }


  public void log(String s) {
      getParent().log(s);
  }


   public Vector getFixedAssortmentData() {
     return null;
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
    	  

    	  Vector dones = new Vector();

    	  StringBuffer buf = new StringBuffer();
    	  
    	   String cmd = " select itemid from inventtrans where dataareaid='" + dataarea + "' and year(datephysical) = year(getdate()) and month(datephysical) = month(getdate()) " +
     " and day(datephysical) = day(getdate()) group by itemid " +
    " union " +
    " select itemid from inventjournaltrans where dataareaid='" + dataarea + "' and year(transdate) = year(getdate()) and month(transdate) = month(getdate()) " +
    " and day(transdate) = day(getdate()) group by itemid " +
   " order by itemid";
    	
    	   
    	   	  
           String cmd2 = " SELECT summ.itemid AS toode , AVAILPHYSICAL AS kogus , '' as location  , inventbatchid as partii ,dim.inventlocationid as ladu  FROM INVENTSUM summ  " +
   "  INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid  " +
   "   INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid  " +
   "   WHERE summ.DATAAREAID='" + dataarea + "'and dim.DATAAREAID='" + dataarea + "' and inv.DATAAREAID='" + dataarea + "' AND CLOSED = 0 AND  moduletype=0  " +
   "   AND  rtrim(dim.inventlocationid)  IN (SELECT inventlocationid FROM INVENTDIM group by inventlocationid ) and AVAILPHYSICAL > 0  " +
   "     ORDER BY summ.itemid";

    	   
    	   try {

    	   
    	   ResultSet rs2 = stmt.executeQuery(cmd);
           while(rs2.next()){
        	   String id = rs2.getString("itemid").trim();
        	   dones.addElement(id);
           }
             
             
    	  
 
          

    if (isDebug()) System.out.println(cmd2);
 
 

     ResultSet rs1 = stmt.executeQuery(cmd2);
                           while(rs1.next()){
                             
                             String id = rs1.getString("toode").trim();
                             if(dones.contains(id)){
                            	
                             double kogus = rs1.getDouble("kogus");
                             String location = rs1.getString("location");
                             String partii = rs1.getString("partii");
                             String ladu = rs1.getString("ladu");
                             if(partii == null || partii.length() == 0){
                              	partii= " ";  
                             }
                             if(location == null || location.length() == 0){
                           	    location= " ";  
                               }

                             if(ladu == null || ladu.length() == 0){
                           	  ladu =  " ";  
                               }
                             buf.append(id + ";" + kogus + ";" + location + ";" + partii + ";" + ladu + "\n");
                             
                             }
                           }
 
 	
 	

    	  
    	  
          if(buf.length() > 2){
              File f = new File(getDir() + "stocklocationsref.txt");
              if (f.exists())f.delete();
                try {
          		PrintWriter os = new PrintWriter(new FileWriter(
          				getDir() + "stocklocationsref.txt", true));
          		os.write(buf.toString());
          		os.close();
          	  }catch (IOException ex) {
          		ex.printStackTrace();
          	  }
           }
          
    	   }catch (Exception e){
    		   e.printStackTrace();
    	   }
    	  
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
  public void connectionDown(){
  }

  public TreeMap getAgents(){
 
    TreeMap agents = new TreeMap();
    
          Agent agent = new Agent("LADU");
          agent.setName("LADU");
          agents.put("LADU", agent);   
        
          
          /*  StringBuffer sb = new StringBuffer();

          String sql = "  select emplid as kood , name as nimi, altnum as parool  from empltable " +
          		" where dataareaid='sel '  and dimension in ('KAUN','SIAUL') order by name " ; //where dimension = 40
        	    if (isDebug()) System.out.println(sql);
     
        	    try {
        	      ResultSet rs = stmt.executeQuery(sql);
        	        while(rs.next()){
        	           agent = new Agent(rs.getString("kood").trim());
        	          String name = rs.getString("nimi").trim();
        	          if(name.length()>30){
        	        	  name = name.substring(0,30);
        	          }
        	          String parool = rs.getString("parool").trim();
        	          
        	          agent.setName(name);
        	          agent.setPassWord(parool);
        	          sb.append(agent.getID() + ";" + name+ ";" + parool + "\n") ;
        	          agents.put(name, agent);
        	        }
        	    } catch (Exception e){
        	        getParent().showError(e);
        	      }
        	   
          
                if(sb.length() > 2){
                    File f = new File(getDir() + "agent.txt");
                    if (f.exists())f.delete();
                      try {
                		PrintWriter os = new PrintWriter(new FileWriter(
                				getDir() + "agent.txt", true));
                		os.write(sb.toString());
                		os.close();
                	  }catch (IOException ex) {
                		ex.printStackTrace();
                	  }
                 }
          */
    return agents;
  }

  public TreeMap getCustomers(Agent agent) {
      connectionUp();

      String cmd =
	    "SELECT OR01001 AS kood,max(OR01021)  AS info, max(SL01002) AS nimi,count(OR01002) as lines,'LADU' as agent " +
	   "   ,max(OR01017) as ring ,max(SL01003 + ' '+ SL01004) as address  FROM   " +
        "    OR010100 inner join SL010100 on  SL01001 = OR01004 inner JOIN OR030100 ON OR03001 = OR01001 WHERE OR01015 > getdate()-5  group by OR01001 " + 
          " union all " +
      " SELECT OR20001 AS kood,max(OR20021)  AS info, max(SL01002) AS nimi,count(OR20002) as lines,'LADU' as agent " +
    	"   ,max(OR20017) as ring ,max(SL01003 + ' '+ SL01004) as address  FROM   " +
     "       OR200100 inner join SL010100 on  SL01001 = OR20004 inner JOIN OR210100 ON OR21001 = OR20001 WHERE OR20015 > getdate()-2 "+
		"	 group by OR20001  " ;// and OR20185 = ''

	log(cmd);

	TreeMap customers = new TreeMap();
  Vector salesids = new Vector();
	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
          String kood = rs.getString("kood").trim();
          salesids.add(kood);
		Customer cust = new Customer(kood);
		String info = rs.getString("info");
		String add = "";
		if(info == null || info.trim().length()== 0){
			add = "!!!!!";
		}
		String nimi = kood + " "+ add  + cleanLitLatEst(rs.getString("nimi").trim()) + "<" + rs.getString("lines") + ">" ;
              
                 cust.setName(nimi);
             
              String ag = rs.getString("agent");
              if (ag != null && ag.trim().length()>0){
              	cust.addAgent(new Agent(ag.trim()));          	
              }
              String ring = rs.getString("ring");
              if (ring != null && ring.trim().length()>0){
                	cust.setLogistics(ring.trim());          	
                }
              String aadr = rs.getString("address");
              if (aadr != null && aadr.trim().length()>0){
                	cust.setAddress(aadr.trim());          	
                }
              
              customers.put(nimi, cust);
	    }
	    
	   /* if(salesids.size()>0){
	    	for (int i = 0; i<salesids.size(); i++){
	    		String sql2 = "UPDATE SalesTable set SelSalesOrderStatus = 1 where deliverydate> getdate()-1  and dataareaid='sel' " +
    			" and salesid = '" + (String)salesids.get(i) + "'";
	    		stmt.executeUpdate(sql2);

	    	}    	
	    }
	    */
	    
	    
	    
	} catch (Exception e) {
	    getParent().showError(e);
	}
	return customers;
  }

  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }


      public String getDistribution() {
        return null;
      }


  public TreeMap getProdGroups() throws SQLException {
    return new TreeMap();
}


  public TreeMap getProdGroupsSecond() throws SQLException {
   return new TreeMap();
  }

  public TreeMap getProducts(Agent agent){

	  String cmd = " SELECT  RTRIM(SC01001) AS kood,SC01015 as sort, SC01037 + ' ' +RTRIM(SC01002) AS nimetus,SC01013 AS kastis,0 as alusel ,SC39004 AS hind ,SC03003-SC03004 AS stock,SC01060 AS ean ,SC01132 as ean2 " +
             "   FROM SC010100 left outer JOIN SC030100 on SC03001=SC01001 and SC03002='10' AND SC03003!=0 left outer JOIN SC390100 ON SC01001 = SC39001 and SC39002='00'  " +
             "   WHERE   LEN(LTRIM(SC01037))>0 and rtrim(ltrim(SC01016)) != 'S' and SC01015 != '' ORDER BY RTRIM(SC01037),SC01002";



    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
 
    try {

      //  StringBuffer buf = new StringBuffer();
        StringBuffer bu = new StringBuffer();
        /*ResultSet rs1 = stmt.executeQuery(cmd2);
                              while(rs1.next()){
                                
                                String id = rs1.getString("toode").trim();
                                double kogus = rs1.getDouble("kogus");
                                String location = rs1.getString("location");
                                String partii = rs1.getString("partii");
                                String ladu = rs1.getString("ladu");
                                if(partii == null || partii.length() == 0){
                              	partii= " ";  
                                }
                                if(location == null || location.length() == 0){
                              	  location= " ";  
                                  }

                                if(ladu == null || ladu.length() == 0){
                              	  ladu =  " ";  
                                  }
                                laosolu.addElement(id);
                                buf.append(id + ";" + kogus + ";" + location + ";" + partii + ";" + ladu + "\n");
                                
                              }
    	*/
    	
    	
    	
    	
    	
    	
    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();

      Hashtable dateOut = new Hashtable();
      System.out.println("prodstart");
      while(rs.next()){
        String kood = rs.getString("kood").trim();
        Product prod = new Product(kood);
        String nimetus = cleanLitLatEst(rs.getString("nimetus").trim());
        
        prod.setPrice(rs.getDouble("hind"));

        String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
        		 prod.setEAN(ean);
      }else {
    	  prod.setEAN(prod.getID());
      }

        String ean2 = rs.getString("ean2");
        if(ean2!=null && ean2.trim().length()>0){
        		 prod.setEanCase(ean2);
      }

               if(prod.getEAN()== null || prod.getEAN().trim().length()==0){
                	  dateOut.put(kood.trim(),kood.trim() + "|" + nimetus.trim() + "|" + prod.getStock());
                	  bu.append(kood.trim() + ";"+ kood.trim() + "|" + deCode (nimetus.trim()) + "|" + prod.getStock() + "\n");
                }else{
                	 dateOut.put(prod.getEAN(),kood.trim() + "|" + nimetus.trim() + "|" + prod.getStock());
                	 bu.append(prod.getEAN() + ";"+ kood.trim() + "|" + deCode (nimetus.trim()) + "|" + prod.getStock() + "\n");
                	 if(prod.getEanCase().trim().length() >0){
                 		 dateOut.put(prod.getEanCase(),kood.trim() + "|" + deCode (nimetus.trim()) + "|" + prod.getStock());
                     	 bu.append(prod.getEanCase() + ";"+ kood.trim() + "|" + deCode (nimetus.trim()) + "|" + prod.getStock() + "\n");	 
                 	 }
                }
               
               prod.setName(nimetus);
//if(laosolu.contains(prod.getID())){
    products.put(nimetus, prod);
    prodHash.put(prod.getID(), prod);
//}

      }
      rs.close();
      
      
      
      try {
			saveObject(dateOut,"tootedRev.has");
			  if(bu.length() > 2){
	                File f = new File(getDir() + "tootedRev.txt");
	                if (f.exists())f.delete();
	                  try {
	            		PrintWriter os = new PrintWriter(new FileWriter(
	            				getDir() + "tootedRev.txt", true));
	            		os.write(bu.toString());
	            		os.close();
	            	  }catch (IOException ex) {
	            		ex.printStackTrace();
	            	  }
	             }
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

                            
                           /* if(buf.length() > 2){
                                File f = new File(getDir() + "stocklocations.txt");
                                if (f.exists())f.delete();
                                  try {
                            		PrintWriter os = new PrintWriter(new FileWriter(
                            				getDir() + "stocklocations.txt", true));
                            		os.write(buf.toString());
                            		os.close();
                            	  }catch (IOException ex) {
                            		ex.printStackTrace();
                            	  }
                             }*/   
                            
                            
                            
                            
                            
                            StringBuffer buf2 = new StringBuffer();
                           /* ResultSet rs4 = stmt.executeQuery(cmd3);
                                                  while(rs4.next()){

                                                      String ladu = rs4.getString("ladu");
                                                    String location = rs4.getString("location");
                                                    
                                                    if(location == null || location.length() == 0){
                                                  	  location= " ";  
                                                      }

                                                    if(ladu == null || ladu.length() == 0){
                                                  	  ladu =  " ";  
                                                      }
                                                    
                                                    buf2.append(ladu + ";" +  location +  "\n");
                                                    
                                                  }*/
                                                  buf2.append("PROD-K;Kaunas\n");
                                                  buf2.append("PROD;Prodoct;\n");
                                                  if(buf2.length() > 2){
                                                      File f = new File(getDir() + "warehouse.txt");
                                                      if (f.exists())f.delete();
                                                        try {
                                                  		PrintWriter os = new PrintWriter(new FileWriter(
                                                  				getDir() + "warehouse.txt", true));
                                                  		os.write(buf2.toString());
                                                  		os.close();
                                                  	  }catch (IOException ex) {
                                                  		ex.printStackTrace();
                                                  	  }
                                                   }
                            
                            

    } catch (Exception e){
      getParent().showError(e);
    }

    System.out.println("proddone");
    return products;
  }

  public static String deCode(String res ){
	  	return res;
	  }
	 
  private String getDir(){
	    String dir = getParent().getProps().getProperty("readin");
	    if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	    return dir;
	  }

  
  public static void saveObject(Object obj, String file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
		oos.writeObject(obj);
		oos.close();
	}

  
  
  public Vector getCustomerProductsList(String agentCode){
	  connectionUp();
		
		String cmd =
		    " SELECT OR01001 AS saaja,OR03005 AS toode,OR01015 as kuupaev,OR03011 AS kogus , OR03002 as reaid " +
		    "FROM OR010100 inner join SL010100 on  SL01001 = OR01004 inner JOIN OR030100 ON OR03001 = OR01001 WHERE OR01015 > getdate()-5 " + 
		   "union all " +
		   " SELECT OR20001 AS saaja,OR21005 AS toode,OR20015 as kuupaev,OR21011 AS kogus , OR21002 as reaid " +
		  " FROM OR200100 inner join SL010100 on  SL01001 = OR20004 inner JOIN OR210100 ON OR21001 = OR20001 WHERE OR20015 > getdate()-3  " +
		  " order by 1,2";

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
	        double reaid = rs.getDouble("reaid");
	       
	    // System.out.println("2344 " + reaid);

	                if (kliendikood.length() > 1) {
	                  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,kuupaev.substring(5,10),0,0,reaid,"");
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



  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


    public void setBulkSales(Map agents){
        this.connectionUp();
  
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
    return "VipexLIT";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  	 public String getPilotDayData(Agent agent){

  	  
  	      return null;
  	  }






       public NumberFormat getNF(){
         return NF;
       }

 

       public void setNF(NumberFormat aNF){
         NF = aNF;
    }
  private String url = "jdbc:odbc:scalamobile";
  private Connection con;
  private String dataarea = null;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
