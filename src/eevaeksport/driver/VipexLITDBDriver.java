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
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.InventoryLine;
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

public class VipexLITDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater {
  public VipexLITDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      stmt = con.createStatement();
      dataarea = parent.getProps().getProperty("dataareaid");
    } catch (Exception e){
       parent.showError(e);
    }
  }

  /**
   *
   */
  public void connectionUp(){
  }

  
  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  private long getAndUpdateRecID(int tab) throws SQLException{
	    long nextRecID = 0;
	    String sql = "SELECT NEXTVAL FROM SYSTEMSEQUENCES WHERE  NAME='SEQNO'AND dataareaid='" + dataarea + "'"; //AND tabid = " + tab;
	    log.info("getAndUpdateRecID(): " + sql);
	      Statement st = con.createStatement();
	      ResultSet rs = st.executeQuery(sql);
	      while(rs.next()){
	        //System.out.println("number:" + rs.getString("NEXTREC"));
	        nextRecID = rs.getLong("NEXTVAL");
	        log.info("NEXTRECID = " + nextRecID);
	        sql = "UPDATE SYSTEMSEQUENCES SET NEXTVAL=" + (nextRecID+1) +
	        " WHERE   NAME='SEQNO' and dataareaid='" + dataarea + "'";// tabid = " + tab;
	        log.info("getAndUpdateRecID(): " + sql);
	        Statement st2 = con.createStatement();
	        int i = st2.executeUpdate(sql);
	        st2.close();
	        log.info("UPDATED ROWS = " + i);
	      }
	      rs.close();
	      st.close();

	    return nextRecID;
	  }

  
  
  private void insertInventory(Vector vec){
  	if(vec != null){
  		
		Vector tehtud = null;
		File file = new File ("loetudid.vec");
		if(file.exists()){
		
           try {
           ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
				tehtud = (Vector)ois.readObject();
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}else{
			tehtud = new Vector();
		}
  		
  	
  		Enumeration enums = vec.elements();
  		while(enums.hasMoreElements()){
  			InventoryLine lin = (InventoryLine)enums.nextElement();
  			
  			String objekt = lin.getCustId();
  			if(objekt == null){
  				objekt = "";
  			}
  			String location = lin.getLocation();
  			if(location == null){
  				location = "";
  			}
  			String toLocation = lin.getToLocation();
  			if(toLocation == null){
  				toLocation = "";
  			}
  			String comment = lin.getComment();
  			if(comment == null){
  				comment = "";
  			}
  			String warehouse = lin.getWarehouse();
  			if(warehouse == null){
  				warehouse = "";
  			}
  			String toWarehouse = lin.getToWarehouse();
  			if(toWarehouse == null){
  				toWarehouse = "";
  			}
  			double kogus  = lin.getAmount();
  			String ean = lin.getEan();
  			if(ean == null){
  				ean = "";
  			}
  			String prName = lin.getProdName();
  			if(prName == null){
  				prName = "";
  			}
  			String wrk = lin.getWorker();
  			if(wrk == null){
  				wrk = "";
  			}
  			String wrkname = lin.getWorkername();
  			if(wrkname == null){
  				wrkname = "";
  			}
  			String prodid = lin.getProdId();
  			if(prodid == null){
  				prodid = "";
  			}
  			String partii = lin.getPartii();
  			if(partii == null){
  				partii = "";
  			}
  			String lineType = lin.getLineType();
  			if(lineType == null){
  				lineType = "";
  			}
  			String lineid = ean + "" + lin.getEnterTime().getTime();
  			java.sql.Date entered = new java.sql.Date(lin.getEnterTime().getTime());
  			int trouble = 0;
  			if(lin.isTrouble() && !tehtud.contains(lineid)){
  				trouble = 1;
  				
  				
  	  			System.out.println(objekt + "'" + location+ "'" + toLocation + "'" + kogus + "'" +  prodid + "'" + 
  	  					ean + "'" + prName+ "'" + wrk + "'" + wrkname+ "'" + entered.toString() + "'" +warehouse + "'" +toWarehouse + "-" + partii );
  	  		 
  	  			try{
  	  			Email mail = new Email(null);
   	  		   
  	  	  		mail.setSMTP(parent.getProps().getProperty("smtphost"));
  	  	  		mail.setFilesTo(parent.getProps().getProperty("troubleemail"));
  	  	  		mail.setFrom(parent.getProps().getProperty("from"));
  	  	  		mail.setSubjectFilesTo("Kriitilised Probleemid laost toode " + prName);
  	  	  		
  	  	  		mail.setBody("\nLaoseisu KriitilineProbleemiraport\n\n" +
  	  	  				  "\nLadu: " + warehouse +
  	  	  				"\nAsukoht kust : " + location +
  	  	  				"\nAsukoht kuhu : " + toLocation +
  	  	  				"\nToode  :    " + prodid +
  	  	  			    "\nTootenimi : " + prName +
  	  	  			    "\nTootekogus : " + kogus +
  	  	  			    "\nToote partii : " + partii +
  	  	  				"\nEAN : " + ean +
  	  	  				"\nT��taja : " + wrkname +
  	  	  				"\nFikseerimiseaeg : " +  entered.toString() );
  	  	  		mail.setSendNotice(false);
  	  	  		mail.send();
  	  				
  	  			}catch(Exception e){
  	  			e.printStackTrace();	
  	  			}
  	  				
  			}
  			
  			Calendar cal = Calendar.getInstance();
  			cal.setTime(lin.getEnterTime());
  			String tund = cal.get(Calendar.HOUR) + "";
  			if (tund.length() == 1){
  				tund = "0" + tund;
  			}
  			String minut = cal.get(Calendar.MINUTE) +"";
				if (minut.length() == 1){
					minut = "0" + minut;
  			}
				
				
				
				if(!tehtud.contains(lineid)){
				
  			 try{
  			  
  			 	
  			 	con = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  			
  			
  			System.out.println(objekt + "'" + location+ "'" + toLocation + "'" + kogus + "'" +  prodid + "'" + 
  					ean + "'" + prName+ "'" + wrk + "'" + wrkname+ "'" + entered.toString() + "'" +warehouse + "'" +toWarehouse + "-" + partii );
  		 
  			 
  				 String sql3 = " insert into VPInventDocImpModule ( ITEMID,ITEMNAME,QTY,FROMINVENTLOCATIONID,FROMWMSLOCATIONID, " +
         " FROMINVENTBATCHID,TOINVENTLOCATIONID ,TOWMSLOCATIONID ,TOINVENTBATCHID , TYPE, JOURNALTRANSDATE , EMPLID, " +
         "  IMPORTED,MODIFIEDDATE ,MODIFIEDTIME ,MODIFIEDBY ,CREATEDDATE ,CREATEDTIME ,CREATEDBY ,DATAAREAID ,RECVERSION  ,RECID,TROUBLE ,TROUBLE_DATE ) values( " +
         "	'" + prodid + "','" + prName.replaceAll(",", " ") + "',?,'" + warehouse.trim() + "','" + location.trim() + "',' " + partii 
             +"','" + toWarehouse.trim() + "','" + toLocation.trim() + "','" + partii + "', " +
         "	'" + lineType + "',getdate(),'" + wrk + "',0,?,1000,'"+ wrk+ "',getdate(),1000,'" + wrk + "','" + dataarea + "',50007," + getAndUpdateRecID(50007) + "," + trouble +",getdate()-10000)";
      PreparedStatement st = con.prepareStatement(sql3);
      System.out.println(sql3 );
     
        st.setDouble(1, kogus);
        st.setTimestamp(2, new Timestamp(entered.getTime()));
      //  st.setTime(3, new Time(entered.getTime()));
        st.executeUpdate();
        
		 String sql4 = " insert into VPInventDocImpModule ( ITEMID,ITEMNAME,QTY,FROMINVENTLOCATIONID,FROMWMSLOCATIONID, " +
         " FROMINVENTBATCHID,TOINVENTLOCATIONID ,TOWMSLOCATIONID ,TOINVENTBATCHID , TYPE, JOURNALTRANSDATE , EMPLID, " +
         "  IMPORTED,MODIFIEDDATE ,MODIFIEDTIME ,MODIFIEDBY ,CREATEDDATE ,CREATEDTIME ,CREATEDBY ,DATAAREAID ,RECVERSION  ,RECID,TROUBLE ,TROUBLE_DATE ) values( " +
         "	'" + prodid + "','" + prName.replaceAll(",", " ") + "',?,'" + warehouse.trim() + "','" + location.trim() + "',' " + partii 
             +"','" + toWarehouse.trim() + "','" + toLocation.trim() + "','" + partii + "', " +
         "	'" + lineType + "',getdate(),'" + wrk + "',0,?,1000,'"+ wrk+ "',getdate(),1000,'" + wrk + "','ts1',50007," + getAndUpdateRecID(50007) + "," + trouble +",getdate()-10000)";
      PreparedStatement st2 = con.prepareStatement(sql4);
      System.out.println(sql4 );

        st2.setDouble(1, kogus);
        st2.setTimestamp(2, new Timestamp(entered.getTime()));
      //  st.setTime(3, new Time(entered.getTime()));
        st2.executeUpdate();
        
        
        
  			 
            tehtud.add(lineid);
			 try{

   			 saveObject(tehtud, "loetudid.vec"); 
			 }catch(Exception e){
				 e.printStackTrace();
			 }
   		      	
   		      //  con.close();
   		        
  			 }catch (Exception e){
  			 	e.printStackTrace();
  			 }
  		
  			
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
           insertInventory(vec);
           File done = new File("import/done/" + file.getName());
           if(done.exists()){
           	done.delete();
           }
          file.renameTo(done);
           file.delete();
          
        }    
      }	
      }
      getSaveStockInfo();
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
    	
    	   
    	   	  
           String cmd2 = " SELECT summ.itemid AS toode , AVAILPHYSICAL AS kogus , wmslocationid as location  , inventbatchid as partii ,dim.inventlocationid as ladu  FROM INVENTSUM summ  " +
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
          StringBuffer sb = new StringBuffer();
          

          String sql = " select emplid as kood , name as nimi, altnum as parool from empltable where dimension = 40" ;
        	    if (isDebug()) System.out.println(sql);
     
        	    try {
        	      ResultSet rs = stmt.executeQuery(sql);
        	        while(rs.next()){
        	           agent = new Agent(rs.getString("kood").trim());
        	          String name = rs.getString("nimi").trim();
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
          
    return agents;
  }

  public TreeMap getCustomers(Agent agent){
  TreeMap customers = new TreeMap();
        String accountnum = "Siire";
      
        Customer cust = new Customer(accountnum);
          cust.setName("Siire");
          cust.addAgent(new Agent("LADU"));
          customers.put("Siire", cust);
      
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

    String cmd =" SELECT   inv.itemid AS kood,inv.itemname AS nimetus, factor AS tykke, '' AS stock, " +
   "  price AS hind,inv.itemgroupid AS grupp, '' AS onpallet , " +
   " itembarcode AS ean , left(taxitemgroupid,2) AS vat ,unitid AS unit " +
	"  FROM inventtable inv " +
   " LEFT JOIN inventtablemodule mod ON mod.itemid = inv.itemid " +
   " LEFT JOIN inventitemgroup gr ON inv.itemgroupid= gr.itemgroupid  " +
	"  LEFT JOIN inventitembarcode bc ON bc.itemid=inv.itemid  " +
	"  LEFT JOIN unitconvert uc ON uc.itemid=inv.itemid   " +
  " WHERE   bc.dataareaid='" + dataarea + "' and inv.dataareaid='" + dataarea + "' AND gr.dataareaid='" + dataarea + "' AND mod.dataareaid='" + dataarea + "'  AND moduletype=2 " +
    " AND len(inv.itemname)>0  AND left(inv.itemid,1)!='2' AND blocked = 0 "  ;


    //unitconvert
             String cmd2 = " SELECT summ.itemid AS toode , POSTEDQTY AS kogus , wmslocationid as location  , inventbatchid as partii ,dim.inventlocationid as ladu  FROM INVENTSUM summ  " +
     "  INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid  " +
     "   INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid  " +
     "   WHERE summ.DATAAREAID='" + dataarea + "'and dim.DATAAREAID='" + dataarea + "' and inv.DATAAREAID='" + dataarea + "' AND CLOSED = 0 AND  moduletype=0  " +
     "   AND  rtrim(dim.inventlocationid)  IN (SELECT inventlocationid FROM INVENTDIM group by inventlocationid ) and AVAILPHYSICAL > 0  " +
     "     ORDER BY summ.itemid";

             
             String cmd3 = "select inventlocationid as ladu,wmslocationid as location from wmslocation where dataareaid= '" + dataarea + "'";

    if (isDebug()) System.out.println(cmd);
       if (isDebug()) System.out.println(cmd2);
    TreeMap products = new TreeMap();
    Vector laosolu = new Vector();
    try {

        StringBuffer buf = new StringBuffer();
        StringBuffer bu = new StringBuffer();
        ResultSet rs1 = stmt.executeQuery(cmd2);
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
    	
    	
    	
    	
    	
    	
    	
    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();

      Hashtable dateOut = new Hashtable();
      System.out.println("prodstart");
      while(rs.next()){
        String kood = rs.getString("kood").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimetus").trim();

        prod.setPrice(rs.getDouble("hind"));

        String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
        	if(prodHash.get(prod.getID()) != null){
        		System.out.println("Lisame teist EAAN-i");
        		prod = (Product) prodHash.get(prod.getID());
        		prod.setEanCase(ean);
        		System.out.println("Lisame teist EAAN " + prod.getEAN() + " " + prod.getEanCase());
        	}else{
        		 prod.setEAN(ean);
        	}
      }

        String unit = rs.getString("unit");
               if(unit!=null && unit.trim().length()>0){
                prod.setUnit(unit);
             }

               if(prod.getEAN().trim().length()==0){
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
if(laosolu.contains(prod.getID())){
    products.put(nimetus, prod);
    prodHash.put(prod.getID(), prod);
}

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

                            
                            if(buf.length() > 2){
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
                             }   
                            
                            
                            
                            
                            
                            StringBuffer buf2 = new StringBuffer();
                            ResultSet rs4 = stmt.executeQuery(cmd3);
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
                                                    
                                                  }
                                                  
                                                  if(buf2.length() > 2){
                                                      File f = new File(getDir() + "warestatic.txt");
                                                      if (f.exists())f.delete();
                                                        try {
                                                  		PrintWriter os = new PrintWriter(new FileWriter(
                                                  				getDir() + "warestatic.txt", true));
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
    Vector custProds = new Vector();
    //if (true) return custProds ;
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
  private String url = "jdbc:odbc:BMSDSN";
  private Connection con;
  private String dataarea = null;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
