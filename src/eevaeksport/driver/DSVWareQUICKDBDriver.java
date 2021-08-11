/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.InventoryLine;
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
public class DSVWareQUICKDBDriver extends AbstractDBDriver implements BulkUpdater {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public DSVWareQUICKDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
	// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	if(parent.getProps().getProperty("company") != null){
		company = parent.getProps().getProperty("company");
	}

   parent.setWare(true);
	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	}
    }
    private String company = "02";

    public String getSaveStockInfo(){
    	
   
		
    	        File dir = new File("import");
    	        File[] files = dir.listFiles();
    	        
    sortFilesDesc(files);
     	        
    	        if(files != null){
    	        	 makeBins();
    	        for (int i = 0; i < files.length ; i++){
    	          File file = files[i];
    	          if (file.isFile()){
    	            Vector vec = null;
    	            try {
    	            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
    	           
    				
    					vec = (Vector)ois.readObject();
    					ois.close();
    				} catch (IOException e) {
    					
    					e.printStackTrace();
    				} catch (ClassNotFoundException e) {
    					e.printStackTrace();
    				}
    				move = false;
    				insertInventory(vec ,true);
    				if(!move){
    					  File done = new File("import/done/" + file.getName());
    	    	             if(done.exists()){
    	    	             	done.delete();
    	    	             }
    	    	             file.renameTo(done);
    	    	             file.delete();
    	    	      }    	            
    	           }    
    	        }	
    	        }
    	        System.out.println(topelt);
    	        System.exit(0);
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

    /**
     *
     */
    public void connectionUp(){
      if (con == null){
	try {
           con = DriverManager.getConnection(url,"mobile","mobile");
           stmt = con.createStatement();
	} catch (Exception e) {
	    getParent().showError(e);
	}
      }
    }

        public String getDistribution() {
          return null;
        }
        public TreeMap getProdGroupsSecond() throws SQLException {
            TreeMap   tm = new TreeMap();
            return tm;
        }

    public TreeMap getProdGroups() throws SQLException {
        TreeMap   tm = new TreeMap();
        return tm;
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
        connectionUp();
    		
   		 TreeMap agents = new TreeMap();
            Agent agent = new Agent("LADU");
            agent.setName("LADU");
            agents.put("LADU", agent);   
          StringBuffer sb = new StringBuffer();
            String sql = " select MobileJavaID as yksus, Darbinieks as nimetus ,  MobileJavaPSW as pin from Darbinieki" + company + "00 " ; //where dimension = 40
          	    if (isDebug()) System.out.println(sql);
          
          	    try {
          	    	Statement stmt = con.createStatement();
          	      ResultSet rs = stmt.executeQuery(sql);
          	        while(rs.next()){
          	           agent = new Agent(rs.getString("yksus").trim());
          	          String name = rs.getString("nimetus").trim();
          	          if(name.length()>30){
          	        	  name = name.substring(0,30);
          	          }
          	          String parool = rs.getString("pin").trim();    	          
          	          agent.setName(name);
          	          agent.setPassWord(parool);
          	          sb.append(agent.getID() + ";" + name+ ";" + parool + "\n") ;
          	          agents.put(name, agent);
          	          }
          	        con.close();
          	    } catch (Exception e){
          	        getParent().showError(e);
          	      
          	      }
          	   
            
                  if(sb.length() > 2){
                      File f = new File("agent.txt");
                      if (f.exists())f.delete();
                        try {
                  		PrintWriter os = new PrintWriter(new FileWriter(
                  				"agent.txt", true));
                  		os.write(sb.toString());
                  		os.close();
                  	  }catch (IOException ex) {
                  		ex.printStackTrace();
                  	 System.exit(0);
                  	  }
                   }

                  

        connectionDown();     
   
	return agents;
    }

    public void copyFile(File in, File out) throws Exception {
        FileInputStream fis  = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buf = new byte[1024];
        int i = 0;
        while((i=fis.read(buf))!=-1) {
          fos.write(buf, 0, i);
          }
        fis.close();
        fos.close();
        }

    
    public TreeMap getCustomers(Agent agent) {
        connectionUp();
        String     cmd = 
		"  select 's' +dok.[Sales order] as kood ,max(ltrim(str(dok.Priority)) + dok.Customer) as nimi,count([Stock Code]) as lines , 'shop' as tyyp, max([Received Date]) as kuup , '' as aadr" + 
		" from mj_so_header_shop_" + company + " dok  " +
		" inner join mj_so_lines_shop_" + company + " rid on rid.[Sales order] = dok.[Sales order] where  (rid.[Qty]) > 0 " +
		"  group by 's' +dok.[Sales order]" ;

	log(cmd);

	TreeMap customers = new TreeMap();

	try {
	    ResultSet rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
            String kood = rs.getString("kood").trim();
		Customer cust = new Customer(kood);
		String nim = Converter.cleanLitLatEst(rs.getString("nimi").trim());
		if(nim.indexOf("<") != -1){
			nim = nim.substring(0,nim.indexOf("<"));
		}
		nim = nim.replaceAll("\"", "");
		int lines = rs.getInt("lines");
		String  tyyp = rs.getString("tyyp");
		String asi = "";
		
		   
		String nimi = kood + " " +nim + "<" + lines + ">" ;                	   
		String nimic = "<" + lines + "> " +nim  ; 
                   cust.addAgent(new Agent("LADU"));    
                   cust.setName(kood + " " + nim.substring(1) + "<" + lines + ">");
                   asi = nim.substring(0,1);
                   if(tyyp.equals("pur")){
                       cust.setCity("BUYIN");   
                       cust.setName(nimi);
                       asi = "";
                   }
                   if(tyyp.equals("checkpoint")){
                       cust.setCity("CHECKPOINT"); 
                       cust.setName(nimic);
                       asi = "";
                      // nimi = nimic;
                   }
            String kuup = rs.getString("kuup");
            if(kuup != null && kuup.trim().length() > 0){
            	cust.setContractOver(kuup);
            	if(kuup.length()>17){
            		cust.setName(cust.getName() + "      " + kuup.substring(11,16));
            	}
            	
            }
            
            String aadr = rs.getString("aadr");
            if(aadr != null && aadr.trim().length() > 0){
            	cust.setAddress(Converter.cleanLitLatEst(aadr.trim()));
            }       
            if(lines != 0){
            	  customers.put(asi + nimi, cust);
            }
              
	    }
	      rs.close();
	    
	} catch (Exception e) {
	    getParent().showError(e);
	}
		
	return customers;
    }

    public TreeMap getProducts(Agent agent) {
        connectionUp();

        
        Hashtable dateOut = new Hashtable();
        StringBuffer buf = new StringBuffer();

	TreeMap products = new TreeMap();
     HashMap prods = new HashMap();
     HashMap barcodes = new HashMap();
	try {
 
   
        String sql =   " select [Stock Code] as toode from mj_so_lines_shop_" + company  ;
		  	
        Statement pstm = con.createStatement();
        try{

ResultSet rs6 = pstm.executeQuery(sql);

while(rs6.next()){
String toode = rs6.getString("toode");
if(toode != null){
	 prods.put(toode.trim(), "toode");
}
}

String sqlbar = "select [Stock Code] as toode , [BarCode] as code from MJ_BarCodes_" + company + " " ;
rs6 = pstm.executeQuery(sqlbar);
while(rs6.next()){
	String toode = rs6.getString("toode");
	String code = rs6.getString("code");
	if(toode != null && code != null && code.trim().length() > 0 ){
		Vector vv = (Vector)barcodes.get(toode.trim());
		if(vv == null){
			vv = new Vector();
			vv.add(code.trim());
		}else{
			vv.add(code.trim());
		}
		 barcodes.put(toode.trim(), vv);
	 }
	}
	




rs6.close();
}catch (Exception e){
e.printStackTrace();
}
	TreeMap abikoodid = new TreeMap();

		System.out.println("prods size " +  prods.size());
		
		   String cmd = " SELECT  RTRIM(SC01001) AS toode,( RTRIM(SC01002) +rtrim(SC01003)) + ' ' + RTRIM(SC01001) AS nimetus, SC01060 as lisa, " + 
           		" SC01009 AS tykke, '' as norm,SC01132 AS ean, " +
			    "  SCUN003 as yhik   FROM SC010200 t LEFT JOIN SCUN0200 on SCUN002='LAT' and SCUN001=SC01133" +		
			   "  ORDER BY 1   ";

			if (isDebug()) {
			    System.out.println(cmd);
			}

			
			//System.out.println(seisud.size());
           ResultSet  rs = pstm.executeQuery(cmd);

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String name = Converter.cleanLitLatEst(kood + " " +rs.getString("nimetus").trim());
		String lisa = rs.getString("lisa");

        Product prod = new Product(kood);
        prod.addBarcode(kood);
		if(lisa != null && lisa.trim().length() > 0){
		prod.setOldName(lisa);
		 prod.addBarcode(lisa);
		}
       if(abikoodid.get(kood) != null){
    	   Vector vv = (Vector)abikoodid.get(kood);
    	   for(int u = 0; u< vv.size();u++){
    		   prod.addBarcode((String)vv.elementAt(u)); 
    	   }
       }
		
		        name =  name.substring(0,1) + (name.substring(1,name.length())).toLowerCase().trim();
                prod.setName(name);            
                prod.setEAN(parent.make(rs.getString("ean")));
                prod.setUnit(parent.make(rs.getString("yhik")));
                           
                if(barcodes.get(kood) != null){
                	Vector vv = (Vector)barcodes.get(kood);
                	for (int o = 0;o<vv.size();o++){
                		 prod.addBarcode((String)vv.elementAt(o));	
                	}
    		    }
                
                if(prods.get(kood) != null){
                    products.put(name, prod);	
                }
	          }
            rs.close();

 	} catch (Exception e) {
	    getParent().showError(e);
	}


	return products;
    }

    private int toNum(boolean bb){
    	if(bb){
    		return 1;
    	}else{
    		return 0;
    	}
    }
    

  private void addToDB(InventoryLine lin){
	  String objekt = lin.getCustId();
		if(objekt == null){
			objekt = "";
		}
		String location = lin.getLocation();
		if(location == null){
			location = "";
		}else if ( bincodes.get(location) != null){
			lin.setLocation((String)bincodes.get(location));
		}
		String toLocation = lin.getToLocation();
		if(toLocation == null){
			toLocation = "";
		}else if ( bincodes.get(toLocation) != null){
			lin.setToLocation((String)bincodes.get(toLocation));
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
		
		
		try{
			 String cmd = "insert into dbo.inventory_lines " +
			 		"( worker,workername,prodId,location,toLocation,prodName,ean,custId,machine  ,partii ,warehouse  ,lineType ,comment, " +
			        " trouble,toWarehouse,amount,amountbybooks , enterTime , company) values " +
			        "( '" + lin.getWorker() + "','" + lin.getWorkername() + "','" + lin.getProdId() + "'," +
			        		"'" + lin.getLocation() + "','" + lin.getToLocation() + "','" + lin.getProdName() + "'" +
			        		",'" + lin.getEan() + "','" + lin.getCustId() + "','" + lin.getMachine() + "'" +
			        		"  ,'" + lin.getPartii() + "' ,'" + lin.getWarehouse() + "'  ,'" + lin.getLineType() + "'" +
			          " ,'" + lin.getComment() + "','" + toNum (lin.isTrouble()) + "','" + lin.getToWarehouse() + "',"
			          + lin.getAmount() + ",'" + lin.getAmountByBooks() + "' , ? , '" + company + "')";
			
		
		
				   Connection cc = DriverManager.getConnection(url,"mobile","mobile");
					PreparedStatement smt = cc.prepareStatement(cmd);
					smt.setTimestamp(1, new java.sql.Timestamp(lin.getEnterTime().getTime() ));
					smt.executeUpdate();
					smt.close();
					cc.close();
					
					if(lin.getLineType().equals("INVENTORY")){
						  cmd = "insert into dbo.inventory_lines_inv " +
					 		"( worker,workername,prodId,location,toLocation,prodName,ean,custId,machine  ,partii ,warehouse  ,lineType ,comment, " +
					        " trouble,toWarehouse,amount,amountbybooks , enterTime , company) values " +
					        "( '" + lin.getWorker() + "','" + lin.getWorkername() + "','" + lin.getProdId() + "'," +
					        		"'" + lin.getLocation() + "','" + lin.getToLocation() + "','" + lin.getProdName() + "'" +
					        		",'" + lin.getEan() + "','" + lin.getCustId() + "','" + lin.getMachine() + "'" +
					        		"  ,'" + lin.getPartii() + "' ,'" + lin.getWarehouse() + "'  ,'" + lin.getLineType() + "'" +
					          " ,'" + lin.getComment() + "','" + toNum (lin.isTrouble()) + "','" + lin.getToWarehouse() + "',"
					          + lin.getAmount() + ",'" + lin.getAmountByBooks() + "' , ? , '" + company + "')";
					
				
				
						    cc = DriverManager.getConnection(url,"mobile","mobile");
							 smt = cc.prepareStatement(cmd);
							smt.setTimestamp(1, new java.sql.Timestamp(lin.getEnterTime().getTime() ));
							smt.executeUpdate();
							smt.close();
							cc.close();
							
					}
		}catch(Exception e){
			e.printStackTrace();
		}
  }
 
    

    private void insertInventory(Vector vec, boolean inventuur){
	  	if(vec != null){ 		
			Vector tehtud = null;
			
			Vector jooksev = new Vector();
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
	  			}else{
	  				lin.setCustId(objekt.substring(1));
	  			}
	  			
	  			String location = lin.getLocation();
	  			
	  			String toLocation = lin.getToLocation();
	  			
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
	  			if (jooksev.contains(prodid)){
	  				topelt.add(prodid + " " + kogus);
	  			  }
	  			jooksev.add(prodid);
	  			
	  		
	  			
	  			if(lin.isTrouble() && !tehtud.contains(lineid)){
	  				
	  				
	  	  			System.out.println(objekt + "'" + location+ "'" + toLocation + "'" + kogus + "'" +  prodid + "'" + 
	  	  					ean + "'" + prName+ "'" + wrk + "'" + wrkname+ "'" + entered.toString() + "'" +warehouse + "'" +toWarehouse + "-" + partii );
	  	  		 
	  	  			try{
	  	  			Email mail = new Email(null);
	   	  		   
	  	  	  		mail.setSMTP(parent.getProps().getProperty("smtphost"));
	  	  	  		mail.setFilesTo(parent.getProps().getProperty("troubleemail"));
	  	  	  		mail.setFrom(parent.getProps().getProperty("from"));
	  	  	  		mail.setSubjectFilesTo("Problems from warehouse " + prName);
	  	  	  		
	  	  	  		mail.setBody("\nProbel whit Stock level \n\n" +
	  	  	  				  "\nWarehouse: " + warehouse +
	  	  	  				"\nLocation From : " + location +
	  	  	  				"\nLocation To : " + toLocation +
	  	  	  				"\nProduct  :    " + prodid +
	  	  	  			    "\nProductname : " + prName +
	  	  	  			    "\nAmount : " + kogus +
	  	  	  			    "\nProduct Batch : " + partii +
	  	  	  				"\nEAN : " + ean +
	  	  	  				"\nWroker : " + wrkname +
	  	  	  				"\nTimestamp : " +  entered.toString() );
	  	  	  		mail.setSendNotice(false);
	  	  	  		mail.send();
	  	  				
	  	  			}catch(Exception e){
	  	  			e.printStackTrace();	
	  	  			}
	  	  				
	  			}
	  			Calendar cal = Calendar.getInstance();
	  			cal.setTimeInMillis(lin.getEnterTime().getTime());
	  			String tund = cal.get(Calendar.HOUR_OF_DAY)+"";
	  			if (tund.length() == 1){
	  				tund = "0" + tund;
	  			}
	  			String minut = cal.get(Calendar.MINUTE)+"";
					if (minut.length() == 1){
						minut = "0" + minut;
	  			}
					
				
					
					if(!tehtud.contains(lineid)){
					
						System.out.println(objekt + "'" + location+ "'" + toLocation + "'" + kogus + "'" +  prodid + "'" + 
			  					ean + "'" + prName+ "'" + wrk + "'" + wrkname+ "'" + entered.toString() +
			  					"'" +warehouse + "'" +toWarehouse + "-" + partii );	
						
	  			 try{
	     			 	con = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  		
	  			if(lineType.equals("INVENTORY") ){
	  	    			 tehtud.add(lineid);
	  	    			// makeInventoryMove(lin);
	  	    	}else{
	  	    		if(location == null){
		  				location = "";
		  			}else if ( bincodes.get(location) != null){
		  				lin.setLocation((String)bincodes.get(location));
		  			}	
	  	    	//muudame et oleks scalaga yhtne bin 
					 if(toLocation == null){
			  				toLocation = "";
			  			}else if ( bincodes.get(toLocation) != null){
			  				lin.setToLocation((String)bincodes.get(toLocation));
			  			}   
	  	    	}
	  		    try{
				   			 saveObject(tehtud, "loetudid.vec"); 
							 }catch(Exception e){
								 e.printStackTrace();
							 }
	  			
							 
	  			if(lineType.equals("MOVE")){
	  			    makeWareMove(lin);
	  			    move = true;
	  			}
	  			 System.out.println("Line type  " +  lineType);
	  			
	  			if(lineType.equals("HEADERDONE")){	 
	  				this.changeOrderHeader(lin);
	  				move = true;
	  			}
	  			
	  			if(lineType.equals("PURHEADERDONE")){	
	  				this.changePurchHeader(lin);
	  				move = true;
	  			}
	  			if(lineType.equals("CHECKLABEL")){	
	  				insertCheckLabel(lin);	  				
	  			}
	  			if(lineType.equals("CHECKLINE")){	
	  				insertCheckLabelDone(lin);  				
	  			}
	  			if(lineType.equals("PURLINECHANGE")){
	  			    this.changePurcOrderContent(lin);
	  			    move = true;
	  			}
	  	 
	  			if(lineType.equals("LINECHANGE")){
	  			    this.changeOrderContent(lin);
	  			    move = true;
	  			}
	  			if(move){
		            tehtud.add(lineid);	
		            try{
			   			 saveObject(tehtud, "loetudid.vec"); 
						 }catch(Exception e){
							 e.printStackTrace();
						 }

	  			}
					   		      	
	   		      //  con.close();
	   		        
	  			 }catch (Exception e){
	  			 	e.printStackTrace();
	  			 }
	  		
	  			addToDB(lin);
					}
	  		}
	  	
	  	}
	  }

	  
    HashMap bincodes = new HashMap();
    
    private void makeBins(){
    	
    	  connectionUp();
    		
    		String cmd =
    		    " select  BinNumber as bin, BinID as kood  from mj_bins_" + company + " "; 
    		    System.out.println(cmd);


    		try {
    		    ResultSet	      rs = stmt.executeQuery(cmd);	
    		    while (rs.next()){
    		    	bincodes.put(rs.getString("bin").trim(), rs.getString("kood").trim());
    		    }
    		}catch(Exception e){
    			
    		}
      System.out.println("Bins Size " + bincodes.size());
    	
    }
    
    
    public static void sortFilesDesc(File[] files) {
    	  Arrays.sort(files, new Comparator() {
    	    public int compare(Object o1, Object o2) {
    	      if (((File)o1).lastModified()<((File)o2).lastModified()) {
    	        return -1;
    	      } else if (((File) o1).lastModified() > ((File) o2).lastModified()) {
    	        return +1;
    	      } else {
    	        return 0;
    	      }
    	    }
    	  });
    	}
    
    public String getSaveDebtInfo() {
    	
        
    	     
    	        File dir = new File("import");
    	        File[] files = dir.listFiles();
    	        
    	        sortFilesDesc(files);
    	        
    	        if(files != null){
    	        	 makeBins();
    	        for (int i = 0; i < files.length ; i++){
    	          File file = files[i];
    	          if (file.isFile()){
    	            Vector vec = null;
    	            try {
    	            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
    	           
    				
    					vec = (Vector)ois.readObject();
    					ois.close();
    				} catch (Exception e) {				
    					e.printStackTrace();
    					 File done = new File("import/error/" + file.getName());
	    	             if(done.exists()){
	    	             	done.delete();
	    	             }
	    	            file.renameTo(done);
	    	             file.delete();
    				}
    				move = false;
    				insertInventory(vec ,false);
    				if(move){
    					  File done = new File("import/done/" + file.getName());
    	    	             if(done.exists()){
    	    	             	done.delete();
    	    	             }
    	    	            file.renameTo(done);
    	    	             file.delete();
    				}
    	           
    	            
    	          }    
    	        }	
    	        }
    	        System.out.println(topelt);
    	        System.exit(0);
    	    	
    	    	return null;
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

	public Vector getPriceMatrixData() {
		return null;
	}
	
	public Vector getFixedAssortmentData() {
		return null;

	}
		
	private String getToday(){
		
			StringBuffer sb = new StringBuffer();
		
			Calendar	cal = Calendar.getInstance();
			
			 
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}

			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}

			sb.append("" + cal.get(Calendar.YEAR));
			sb.append("-" + month);
			sb.append("-" + day);
			return sb.toString();
		}
	
	
	
	private void makeConfirmXML(String id ){
		 StringBuffer sb2 = new StringBuffer();
			String ls = System.getProperty("line.separator");
		   boolean first = true;
			
			 try {
				 String cmd = "select max([Supplier Code]) as supp , sum(ConfQty/[Unit Factor]) as kogus,sum(StockQty/[Unit Factor]),max([Stock Code]) as toode " +
				 		" ,  [Line Number] as lin  from mj_po_lines_02  " +
				 		" where [Purchase order] = '" + id + "' group by [Line Number] ";
					System.out.println(cmd);
				   Connection cc = DriverManager.getConnection(url,"mobile","mobile");
					PreparedStatement smt = cc.prepareStatement(cmd);
					ResultSet rs = smt.executeQuery();
					int counter = 1;
					  while (rs.next()) {
						  String supplier = rs.getString("supp");
						  double kogus = rs.getDouble("kogus");
						  String toode = rs.getString("toode");
						  String linenum = rs.getString("lin");
							if(first){
								   sb2.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + ls);
								      sb2.append("<msg:Msg xsi:schemaLocation=\"http://Epicor.com/Message/2.0 http://scshost/schemas/epicor/ScalaMessage.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:msg=\"http://Epicor.com/Message/2.0\">" + ls);			      
								      sb2.append("  <msg:Hdr>" + ls);
								      sb2.append("   <msg:Sender>" + ls);
								      sb2.append("     <msg:Name>Mobil</msg:Name>" + ls);
								      sb2.append("     <msg:Subname>Scala</msg:Subname>" + ls);
								      sb2.append("   </msg:Sender>" + ls);
								      sb2.append("    <msg:Ctrl>" + ls);
								      sb2.append("      <msg:MsgID>" + Calendar.getInstance().getTime().getTime() + "</msg:MsgID>" + ls);
								      sb2.append("    </msg:Ctrl>" + ls);
								      sb2.append("   </msg:Hdr>" + ls);
								      sb2.append("   <msg:Body>" + ls);
								      sb2.append("      <msg:Req msg-type=\"Purchase Order Invoice\" action=\"Process\">" + ls);
								      sb2.append("    <msg:Dta>" + ls);
								      sb2.append("    <dta:PurchaseInvoice xsi:schemaLocation=\"http://www.scala.net/PurchaseInvoice/1.1 http://scshost/schemas/Scala/1.1/PurchaseInvoice.xsd\" xmlns:msg=\"http://Epicor.com/InternalMessage/1.1\" xmlns:dta=\"http://www.scala.net/PurchaseInvoice/1.1\">" + ls);		      
								 
								      sb2.append("     <dta:InvHeader>" + ls);
								    		  sb2.append("    <dta:InvNum>" + Calendar.getInstance().getTimeInMillis() + "</dta:InvNum>" + ls);
								    		  sb2.append("    <dta:InvDate>" +getYYYYpMMpDDString(Calendar.getInstance().getTime()) + "</dta:InvDate>" + ls);
								    		  sb2.append("    <dta:BEDate>" +getYYYYpMMpDDString(Calendar.getInstance().getTime()) + "</dta:BEDate>" + ls);
								    		  sb2.append("    <dta:SuppCode>" + supplier + "</dta:SuppCode>" + ls);
								    		  sb2.append("    <dta:InvCurr>LVL</dta:InvCurr>" + ls);
								    		  sb2.append("    <dta:InvAmnt>0</dta:InvAmnt>" + ls);
								    		  sb2.append("    </dta:InvHeader>" + ls);
								    		  sb2.append("     <dta:InvLineList>" + ls);
								     first = false; 		
							}
					    	

							 sb2.append("     <dta:InvLine>" + ls);
							// sb2.append("    <dta:ShipmentNum>000033</dta:ShipmentNum>" + ls);
							 sb2.append("    <dta:OrdNum>" + id + "</dta:OrdNum>" + ls);
							 sb2.append("    <dta:OrdLine>" + linenum + "</dta:OrdLine>" + ls);
							 sb2.append("    <dta:StockCode>" + toode + "</dta:StockCode>" + ls);
							 sb2.append("    <dta:QtyInvoiced>" + kogus + "</dta:QtyInvoiced>" + ls);
							 sb2.append("    <dta:UnitPrice>0</dta:UnitPrice>" + ls);
							 sb2.append("    <dta:Amount>0</dta:Amount>" + ls);
							 sb2.append("    </dta:InvLine>" + ls);
							counter ++;
					    	
					    }
					smt.close();
					cc.close();
				
			
if(!first){
    sb2.append("    </dta:InvLineList>" + ls);
    sb2.append("    </dta:PurchaseInvoice>" + ls);
    sb2.append("  </msg:Dta>" + ls);
    sb2.append("</msg:Req>" + ls);
    sb2.append("</msg:Body>" + ls);
    sb2.append("</msg:Msg>" + ls);
}
		  	
 }catch (Exception e ){
				 
			 }
			  
		      
		      try{
		    	  if(sb2.length()>5){
		    		  String nr = Calendar.getInstance().getTimeInMillis() + "";
				    	String fileName3 = parent.getProps().getProperty("conv.dir4")  +  nr  +  ".xml";
			          	FileOutputStream fos = new FileOutputStream(fileName3);
				        fos.write(sb2.toString().getBytes());
				        fos.close();
		    	  }		    	      	
			    }catch(Exception e){
			    	e.printStackTrace();
			    }
			  
		
	}
	
	private String lineNum(int a){
		String res = a + "";
		while (res.length() < 7){
			res = "0" + res ;
		}
		return res ;
	}
	 private void  makeBatchUpdate(InventoryLine line ,String doknum){
		 connectionUp();
		 String partii = line.getPartii();
		 while(partii.startsWith("0")){
			 partii = partii.substring(1);
		 }
		
	 String cmd = "UPDATE mj_po_lines_" + company + " set status = 1, ConfQty= ConfQty + " + line.getAmount() + " where [Load Number]='" + partii + "'";
	
	 
	 try {
			System.out.println(cmd);
		   Connection cc = DriverManager.getConnection(url,"mobile","mobile");
		   	 
			PreparedStatement smt = cc.prepareStatement(cmd);	
			smt.executeUpdate();
			smt.close();
			cc.close();

	//vaja kontrollida ka k�ik read on t�idetud 100% kui jah siis kinni panna staatus 2 
	
			 String cmd2 = "select ConfQty,Delqty-ConfQty as vahe from mj_po_lines_" + company + " " +
			 		"where status < 2 and [Purchase order] = '" + doknum + "'";

				System.out.println(cmd2);
			ResultSet	      rs = stmt.executeQuery(cmd2);
  boolean korras = true ;
	    while (rs.next()) {
	    	double vahe = rs.getDouble("vahe");
	    	if(vahe != 0){
	    		korras = false;
	    	}
	    }
	
	    System.out.println("korras Boolean "+ korras);
		
	    if(korras){
	    	//makeConfirmXML(doknum);
	    	
	   	 String cmd3 = "UPDATE mj_po_lines_" + company + " set status = 2 where [Purchase order]='" + doknum + "'";
	   	 cc = DriverManager.getConnection(url,"mobile","mobile");
		 smt = cc.prepareStatement(cmd3);
		smt.executeUpdate();
		smt.close();
		cc.close();
	   	System.out.println(cmd3);
	    }
	    
		
	 } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		 
	 }
	  
	
	  
	 private void makeInventoryMove(InventoryLine line) throws SQLException{
		 
		  //	1.otsime loetud toote koguse  ylesse 
	
		  
		  String ddd  = "select  Qty as kogus , BinNumber as location, " +
	  		"[Batch Number] as partii, wh as ladu  from mj_bin_balance_" + company + " where [Stock Code]='" + line.getProdId().trim() + "' " +
	  				"and BinNumber ='" + line.getLocation() + "'";
			System.out.println(ddd);
		   
			String location = line.getLocation();
			if (location != null &&  bincodes.get(location.trim()) != null){	
  				location = (String)bincodes.get(line.getLocation());
  			}	
			
		//	System.out.println("loaction " + location + " " +  bincodes.get(location.trim()));
		   String doknum = line.getWorker() + "-" +  getTimeNiceString(line.getEnterTime());
			 ResultSet	      rs2 = stmt.executeQuery(ddd);
			 double scala = 0;
			 double vahe = 0;
			 String partii = "";
			 if (rs2.next()) {
				 scala = rs2.getDouble("kogus");
				  vahe = scala  - line.getAmount();	
				  partii = rs2.getString("partii");
			    }
			 
			 rs2.close();
		 
			System.out.println("Teeme " + vahe);
					 
			if(vahe > 0){
			//liigutame t�iega scala koguse minema .
					 
		  StringBuffer sb2 = new StringBuffer();
			String ls = System.getProperty("line.separator");
		  
	       sb2.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + ls);
		      sb2.append("<msg:Msg xsi:schemaLocation=\"http://Epicor.com/Message/2.0 http://scshost/schemas/epicor/ScalaMessage.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:msg=\"http://Epicor.com/Message/2.0\">" + ls);			      
		      sb2.append("  <msg:Hdr>" + ls);
		      sb2.append("   <msg:Sender>" + ls);
		      sb2.append("     <msg:Name>Mobil</msg:Name>" + ls);
		      sb2.append("     <msg:Subname>Scala</msg:Subname>" + ls);
		      sb2.append("   </msg:Sender>" + ls);
		      sb2.append("    <msg:Ctrl>" + ls);
		      sb2.append("      <msg:MsgID>" + Calendar.getInstance().getTime().getTime() + "</msg:MsgID>" + ls);
		      sb2.append("    </msg:Ctrl>" + ls);
		      sb2.append("   </msg:Hdr>" + ls);
		      sb2.append("   <msg:Body>" + ls);
		      sb2.append("      <msg:Req msg-type=\"Stock Transaction\" action=\"Process\">" + ls);
		      sb2.append("    <msg:Dta>" + ls);
		      sb2.append("    <dta:StockTransaction xsi:schemaLocation=\"http://www.scala.net/StockTransaction/1.1 http://scshost/schemas/Scala/1.1/StockTransaction.xsd\" xmlns:msg=\"http://Epicor.com/InternalMessage/1.1\" xmlns:dta=\"http://www.scala.net/StockTransaction/1.1\">" + ls);		      
		      sb2.append("       <dta:Movement>" + ls);
		      sb2.append("         <dta:StockCode>"  + line.getProdId() + "</dta:StockCode>"+ ls);
		      sb2.append("         <dta:WhCodeFrom>" + line.getWarehouse() + "</dta:WhCodeFrom>" + ls);
		      sb2.append("         <dta:WhCodeTo>94</dta:WhCodeTo>" + ls);
		      sb2.append("         <dta:BinCodeFrom>" + location + "</dta:BinCodeFrom>" + ls);
		      sb2.append("         <dta:BinCodeTo>Z00001</dta:BinCodeTo>" + ls);
		      sb2.append("         <dta:TransDate>" + getToday() + "</dta:TransDate>" + ls);
		      sb2.append("            <dta:BatchCode>"  + partii + "</dta:BatchCode>"+ ls);
		      sb2.append("         <dta:Qty>"  + scala + "</dta:Qty>"+ ls);
		     
		      sb2.append("         <dta:Ref>INV</dta:Ref>"+ ls);
		      sb2.append("         <dta:OrdNum>"  + doknum + "</dta:OrdNum>"+ ls);
		      
		      sb2.append("        </dta:Movement>" + ls);
			     
			      sb2.append("    </dta:StockTransaction>" + ls);
			      sb2.append("  </msg:Dta>" + ls);
			      sb2.append("</msg:Req>" + ls);
			      sb2.append("</msg:Body>" + ls);
			      sb2.append("</msg:Msg>" + ls);
			  	
			      
			      try{
			    	  String nr = Calendar.getInstance().getTimeInMillis() + "";
				    	String fileName3 = parent.getProps().getProperty("conv.dir4")  +  nr  +  ".xml";
			          	FileOutputStream fos = new FileOutputStream(fileName3);
				        fos.write(sb2.toString().getBytes());
				        fos.close();
			            	
				    }catch(Exception e){
				    	e.printStackTrace();
				    }
			}
			
			if(vahe < 0){
				//kui on yle siis paneme k�ik arvele samasse kohta 
				 
				  StringBuffer sb2 = new StringBuffer();
					String ls = System.getProperty("line.separator");
				  
			       sb2.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + ls);
				      sb2.append("<msg:Msg xsi:schemaLocation=\"http://Epicor.com/Message/2.0 http://scshost/schemas/epicor/ScalaMessage.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:msg=\"http://Epicor.com/Message/2.0\">" + ls);			      
				      sb2.append("  <msg:Hdr>" + ls);
				      sb2.append("   <msg:Sender>" + ls);
				      sb2.append("     <msg:Name>Mobil</msg:Name>" + ls);
				      sb2.append("     <msg:Subname>Scala</msg:Subname>" + ls);
				      sb2.append("   </msg:Sender>" + ls);
				      sb2.append("    <msg:Ctrl>" + ls);
				      sb2.append("      <msg:MsgID>" + Calendar.getInstance().getTime().getTime() + "</msg:MsgID>" + ls);
				      sb2.append("    </msg:Ctrl>" + ls);
				      sb2.append("   </msg:Hdr>" + ls);
				      sb2.append("   <msg:Body>" + ls);
				      sb2.append("      <msg:Req msg-type=\"Stock Transaction\" action=\"Process\">" + ls);
				      sb2.append("    <msg:Dta>" + ls);
				      sb2.append("    <dta:StockTransaction xsi:schemaLocation=\"http://www.scala.net/StockTransaction/1.1 http://scshost/schemas/Scala/1.1/StockTransaction.xsd\" xmlns:msg=\"http://Epicor.com/InternalMessage/1.1\" xmlns:dta=\"http://www.scala.net/StockTransaction/1.1\">" + ls);		      
				      sb2.append("       <dta:Taking>" + ls);
				      sb2.append("         <dta:StockCode>"  + line.getProdId() + "</dta:StockCode>"+ ls);
				      sb2.append("         <dta:WhCode>" + line.getWarehouse() + "</dta:WhCode>" + ls);
				      sb2.append("         <dta:BinCode>" + location + "</dta:BinCode>" + ls);
				      sb2.append("         <dta:TransDate>" + getToday() + "</dta:TransDate>" + ls);
				      sb2.append("            <dta:BatchCode>"  + partii + "</dta:BatchCode>"+ ls);
				      sb2.append("         <dta:Qty>"  + (-vahe) + "</dta:Qty>"+ ls);
				     
				      sb2.append("         <dta:Ref>INV</dta:Ref>"+ ls);
				      sb2.append("         <dta:OrdNum>"  + doknum + "</dta:OrdNum>"+ ls);
				      
				      sb2.append("        </dta:Taking>" + ls);
					     
					      sb2.append("    </dta:StockTransaction>" + ls);
					      sb2.append("  </msg:Dta>" + ls);
					      sb2.append("</msg:Req>" + ls);
					      sb2.append("</msg:Body>" + ls);
					      sb2.append("</msg:Msg>" + ls);
					  	
					      
					      try{
					    	  String nr = Calendar.getInstance().getTimeInMillis() + "";
						    	String fileName3 = parent.getProps().getProperty("conv.dir4")  +  nr  +  ".xml";
					          	FileOutputStream fos = new FileOutputStream(fileName3);
						        fos.write(sb2.toString().getBytes());
						        fos.close();
					            	
						    }catch(Exception e){
						    	e.printStackTrace();
						    }
					}
		 
		 
	 }
	 
	 private void makeWareMove(InventoryLine line) throws SQLException{
		  
		  //	1.otsime doknri ylesse 
		  
		  
		  // kaks eraldi tehingut on 
		  // 1 liigutamine sissetuleku alast kohale
		  // trigeerime 
		  // lao sisemine liigutamine
		  	
		  boolean sissePaika = false;
		  if(line.getLocation().equals("A00001")){
			  sissePaika = true ;
		  }
		  String partii = line.getPartii();
		  if(partii.length() > 6){
			  partii = partii.substring(partii.length()-6);
		  }
		  if(!sissePaika){
			  if(partii.length() == 6){
				  partii = "000000" + partii;  
			  }else if (partii.length() == 7){
				  partii = "00000" + partii;  
			  }else if (partii.length() == 8){
				  partii = "0000" + partii;  
			  }
		  }
		  
		  String ddd = " select [Purchase order]  as num from  mj_po_lines_" + company + " where  [Load Number]='" + partii + "'";
			 
			System.out.println(ddd);
		   Connection cc = DriverManager.getConnection(url,"mobile","mobile");
		   
		   
		   
		   String doknum = "";
				
			 ResultSet	      rs2 = stmt.executeQuery(ddd);
			 if (rs2.next()) {
			     doknum = rs2.getString("num"); 	
			    }
			 rs2.close();
		 
			
					 
					 
		  StringBuffer sb2 = new StringBuffer();
			String ls = System.getProperty("line.separator");
		  
	       sb2.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + ls);
		      sb2.append("<msg:Msg xsi:schemaLocation=\"http://Epicor.com/Message/2.0 http://scshost/schemas/epicor/ScalaMessage.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:msg=\"http://Epicor.com/Message/2.0\">" + ls);			      
		      sb2.append("  <msg:Hdr>" + ls);
		      sb2.append("   <msg:Sender>" + ls);
		      sb2.append("     <msg:Name>Mobil</msg:Name>" + ls);
		      sb2.append("     <msg:Subname>Scala</msg:Subname>" + ls);
		      sb2.append("   </msg:Sender>" + ls);
		      sb2.append("    <msg:Ctrl>" + ls);
		      sb2.append("      <msg:MsgID>" + Calendar.getInstance().getTime().getTime() + "</msg:MsgID>" + ls);
		      sb2.append("    </msg:Ctrl>" + ls);
		      sb2.append("   </msg:Hdr>" + ls);
		      sb2.append("   <msg:Body>" + ls);
		      sb2.append("      <msg:Req msg-type=\"Stock Transaction\" action=\"Process\">" + ls);
		      sb2.append("    <msg:Dta>" + ls);
		      sb2.append("    <dta:StockTransaction xsi:schemaLocation=\"http://www.scala.net/StockTransaction/1.1 http://scshost/schemas/Scala/1.1/StockTransaction.xsd\" xmlns:msg=\"http://Epicor.com/InternalMessage/1.1\" xmlns:dta=\"http://www.scala.net/StockTransaction/1.1\">" + ls);		      
		      sb2.append("       <dta:Movement>" + ls);
		      sb2.append("         <dta:StockCode>"  + line.getProdId() + "</dta:StockCode>"+ ls);
		      sb2.append("         <dta:WhCodeFrom>" + line.getWarehouse() + "</dta:WhCodeFrom>" + ls);
		      sb2.append("         <dta:WhCodeTo>" + line.getToWarehouse() + "</dta:WhCodeTo>" + ls);
		      sb2.append("         <dta:BinCodeFrom>" + line.getLocation() + "</dta:BinCodeFrom>" + ls);
		      sb2.append("         <dta:BinCodeTo>" + line.getToLocation() + "</dta:BinCodeTo>" + ls);
		      sb2.append("         <dta:TransDate>" + getToday() + "</dta:TransDate>" + ls);
		      sb2.append("            <dta:BatchCode>"  + partii + "</dta:BatchCode>"+ ls);
		      sb2.append("         <dta:Qty>"  + line.getAmount() + "</dta:Qty>"+ ls);
		     
		      sb2.append("         <dta:Ref>comment</dta:Ref>"+ ls);
		      sb2.append("         <dta:OrdNum>"  + doknum + "</dta:OrdNum>"+ ls);
		      
		      sb2.append("        </dta:Movement>" + ls);
			     
			      sb2.append("    </dta:StockTransaction>" + ls);
			      sb2.append("  </msg:Dta>" + ls);
			      sb2.append("</msg:Req>" + ls);
			      sb2.append("</msg:Body>" + ls);
			      sb2.append("</msg:Msg>" + ls);
		  	
		  	
		      
		      
		      try{
		    	  String nr = Calendar.getInstance().getTimeInMillis() + "";
			    	String fileName3 = parent.getProps().getProperty("conv.dir4")  +  nr  +  ".xml";
		          	FileOutputStream fos = new FileOutputStream(fileName3);
			        fos.write(sb2.toString().getBytes());
			        fos.close();
		            	
			    }catch(Exception e){
			    	e.printStackTrace();
			    }
			  
			    if(sissePaika){
					
					  makeBatchUpdate(line,doknum);
					  
				  }			
						
				
		  }
  private void changePurchHeader(InventoryLine line) throws SQLException{
		  
		  //	1.otsime doknri ylesse 
		  /*
		  	
		  StringBuffer sb2 = new StringBuffer();
			String ls = System.getProperty("line.separator");
		  
	       sb2.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + ls);
		      sb2.append("<msg:Msg xsi:schemaLocation=\"http://Epicor.com/Message/2.0 http://scshost/schemas/epicor/ScalaMessage.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:msg=\"http://Epicor.com/Message/2.0\">" + ls);			      
		      sb2.append("  <msg:Hdr>" + ls);
		      sb2.append("   <msg:Sender>" + ls);
		      sb2.append("     <msg:Name>Mobil</msg:Name>" + ls);
		      sb2.append("     <msg:Subname>Scala</msg:Subname>" + ls);
		      sb2.append("   </msg:Sender>" + ls);
		      sb2.append("    <msg:Ctrl>" + ls);
		      sb2.append("      <msg:MsgID>" + Calendar.getInstance().getTime().getTime() + "</msg:MsgID>" + ls);
		      sb2.append("    </msg:Ctrl>" + ls);
		      sb2.append("   </msg:Hdr>" + ls);
		      sb2.append("   <msg:Body>" + ls);
		      sb2.append("      <msg:Req msg-type=\"Goods Receive Note\" action=\"Process\">" + ls);
		      sb2.append("    <msg:Dta>" + ls);
		      sb2.append("    <dta:PurchageOrderDelivery xsi:schemaLocation=\"http://www.scala.net/SalesOrder/1.1 http://scshost/schemas/Scala/1.1/SalesOrder.xsd\" xmlns:msg=\"http://Epicor.com/InternalMessage/1.1\" xmlns:dta=\"http://www.scala.net/SalesOrder/1.1\">" + ls);		      
		      
		      
		      sb2.append("     <dta:OrderHeader>" + ls);
		      sb2.append("     <dta:OrdNum>"  + line.getCustId() + "</dta:OrdNum>"+ ls);
		      sb2.append("     </dta:OrderHeader>" + ls);
		      
		      sb2.append("        <dta:OrderLineList>" + ls);
		      sb2.append("         <dta:OrderLine>" + ls);
		      sb2.append("         <dta:LineNum>"  + line.getPartii() + "</dta:LineNum>"+ ls);
		      sb2.append("         <dta:StockCode>"  + line.getProdId() + "</dta:StockCode>"+ ls);
		     		      
		      sb2.append("             <dta:DelivLineList>"+ ls);
		      
		      sb2.append("              <dta:DelivLine>"+ ls);
		      sb2.append("               <QtyDeliv>"  + line.getAmount() + "</QtyDeliv>"+ ls);
		      sb2.append("               <BinCode>"  + line.getLocation() + "</BinCode>"+ ls);
		      sb2.append("               <SearchID>"  + line.getAmount() + "</SearchID>"+ ls);
		      sb2.append("               <BatchCode>"  + line.getAmount() + "</BatchCode>"+ ls);
		      sb2.append("               <SuppBatchCode>"  + line.getAmount() + "</SuppBatchCode>"+ ls);
		      
		      sb2.append("              </dta:DelivLine>"+ ls);
		      sb2.append("             </dta:DelivLineList>"+ ls);		     
		      sb2.append("         </dta:OrderLine>" + ls);
		      sb2.append("        </dta:OrderLineList>" + ls);
			     
			      sb2.append("    </dta:PurchageOrderDelivery>" + ls);
			      sb2.append("  </msg:Dta>" + ls);
			      sb2.append("</msg:Req>" + ls);
			      sb2.append("</msg:Body>" + ls);
			      sb2.append("</msg:Msg>" + ls);
		  	
		  	
		      
		      
		 /*     try{
		    	  String nr = Calendar.getInstance().getTimeInMillis() + "";
			    	String fileName3 = parent.getProps().getProperty("conv.dir4")  +  nr  +  ".xml";
		          	FileOutputStream fos = new FileOutputStream(fileName3);
			        fos.write(sb2.toString().getBytes());
			        fos.close();
		            	
			    }catch(Exception e){
			    	e.printStackTrace();
			    }
			  
			*/				
						
				
		  }
	
  private void changeOrderHeader(InventoryLine line) throws SQLException{
	  
	  //	1.otsime doknri ylesse 
	  
	  	
	  StringBuffer sb2 = new StringBuffer();
		String ls = System.getProperty("line.separator");
	  
       sb2.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + ls);
	      sb2.append("<msg:Msg xsi:schemaLocation=\"http://Epicor.com/Message/2.0 http://scshost/schemas/epicor/ScalaMessage.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:msg=\"http://Epicor.com/Message/2.0\">" + ls);			      
	      sb2.append("  <msg:Hdr>" + ls);
	      sb2.append("   <msg:Sender>" + ls);
	      sb2.append("     <msg:Name>Mobil</msg:Name>" + ls);
	      sb2.append("     <msg:Subname>Scala</msg:Subname>" + ls);
	      sb2.append("   </msg:Sender>" + ls);
	      sb2.append("    <msg:Ctrl>" + ls);
	      sb2.append("      <msg:MsgID>" + Calendar.getInstance().getTime().getTime() + "</msg:MsgID>" + ls);
	      sb2.append("    </msg:Ctrl>" + ls);
	      sb2.append("   </msg:Hdr>" + ls);
	      sb2.append("   <msg:Body>" + ls);
	      sb2.append("      <msg:Req msg-type=\"Print Request\" action=\"Process\">" + ls);
	      sb2.append("    <msg:Dta>" + ls);
	      sb2.append("    <dta:PrintRequest xsi:schemaLocation=\"http://www.scala.net/PrintRequest/1.1 http://scshost/schemas/Scala/1.1/PrintRequest.xsd\" xmlns:dta=\"http://www.scala.net/PrintRequest/1.1\">" + ls);		      
	      
	      sb2.append("    <dta:Invoice>" + ls);
	      sb2.append("    <dta:OutChannel>EDS</dta:OutChannel>" + ls);
	      sb2.append("    <dta:Comment>Invoices</dta:Comment>" + ls);
	      sb2.append("    <dta:minOrdNum>" + line.getCustId() + "</dta:minOrdNum>" + ls);
	 	  sb2.append("    <dta:maxOrdNum>" + line.getCustId() + "</dta:maxOrdNum>" + ls);
	    		 sb2.append("    </dta:Invoice>" + ls);
	    		
	    	  sb2.append("   </dta:PrintRequest>" + ls);
		      sb2.append("  </msg:Dta>" + ls);
		      sb2.append("</msg:Req>" + ls);
		      sb2.append("</msg:Body>" + ls);
		      sb2.append("</msg:Msg>" + ls);
	  		      
	      try{
	    	  String nr = Calendar.getInstance().getTimeInMillis() + "";
		    	String fileName3 = parent.getProps().getProperty("conv.dir4")  +  nr  +  ".xml";
	          	FileOutputStream fos = new FileOutputStream(fileName3);
		        fos.write(sb2.toString().getBytes());
		        fos.close();
	            	
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
	  }
	
	
	  private void changeOrderContent(InventoryLine line) throws SQLException{
		  
		  //	1.otsime doknri ylesse 
		  
		  	
		  StringBuffer sb2 = new StringBuffer();
			String ls = System.getProperty("line.separator");
		  
	       sb2.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + ls);
		      sb2.append("<msg:Msg xsi:schemaLocation=\"http://Epicor.com/Message/2.0 http://scshost/schemas/epicor/ScalaMessage.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:msg=\"http://Epicor.com/Message/2.0\">" + ls);			      
		      sb2.append("  <msg:Hdr>" + ls);
		      sb2.append("   <msg:Sender>" + ls);
		      sb2.append("     <msg:Name>Mobil</msg:Name>" + ls);
		      sb2.append("     <msg:Subname>Scala</msg:Subname>" + ls);
		      sb2.append("   </msg:Sender>" + ls);
		      sb2.append("    <msg:Ctrl>" + ls);
		      sb2.append("      <msg:MsgID>" + Calendar.getInstance().getTime().getTime() + "</msg:MsgID>" + ls);
		      sb2.append("    </msg:Ctrl>" + ls);
		      sb2.append("   </msg:Hdr>" + ls);
		      sb2.append("   <msg:Body>" + ls);
		      sb2.append("      <msg:Req msg-type=\"Sales Order Delivery\" action=\"Process\">" + ls);
		      sb2.append("    <msg:Dta>" + ls);
		      sb2.append("    <dta:SalesOrderDelivery xsi:schemaLocation=\"http://www.scala.net/SalesOrder/1.1 http://scshost/schemas/Scala/1.1/SalesOrderDelivery.xsd\" xmlns:msg=\"http://Epicor.com/InternalMessage/1.1\" xmlns:dta=\"http://www.scala.net/SalesOrderDelivery/1.1\">" + ls);		      
		      
		      
		      sb2.append("     <dta:OrderHeader>" + ls);
		      sb2.append("     <dta:OrdNum>"  + line.getCustId() + "</dta:OrdNum>"+ ls);
		      sb2.append("     </dta:OrderHeader>" + ls);
		      
		      sb2.append("        <dta:OrderLineList>" + ls);
		      sb2.append("         <dta:OrderLine>" + ls);
		      
		      String linenum1 = line.getWarehouse();
		      String linenum2 = line.getWarehouse();
		      if(linenum1.indexOf(",") != 1){
		    	  linenum1 = linenum1.substring(0,linenum1.indexOf(","));
		    	  linenum2 = linenum2.substring(linenum2.indexOf(",")+ 1);
		      }
		      
		      sb2.append("         <dta:LineNum>"  + linenum1 + "</dta:LineNum>"+ ls);
		      sb2.append("         <dta:StockCode>"  + line.getProdId() + "</dta:StockCode>"+ ls);
		     		      
		     sb2.append("             <dta:DelivLineList>"+ ls);		
		      sb2.append("              <dta:DelivLine>"+ ls);
		      
		      sb2.append("               <dta:QtyDeliv>"  + line.getAmount() + "</dta:QtyDeliv>"+ ls);
		      sb2.append("               <dta:DelivLineNum>"  + linenum2 + "</dta:DelivLineNum>"+ ls);
		      sb2.append("               <dta:BatchCode>"  + line.getLocation() + "</dta:BatchCode>"+ ls);
		      sb2.append("               <dta:DelivDateAct>"  + this.getToday() + "</dta:DelivDateAct>"+ ls);
		      
		      sb2.append("              </dta:DelivLine>"+ ls);
		      sb2.append("             </dta:DelivLineList>"+ ls);		     
		      sb2.append("         </dta:OrderLine>" + ls);
		      sb2.append("        </dta:OrderLineList>" + ls);
			     
			      sb2.append("    </dta:SalesOrderDelivery>" + ls);
			      sb2.append("  </msg:Dta>" + ls);
			      sb2.append("</msg:Req>" + ls);
			      sb2.append("</msg:Body>" + ls);
			      sb2.append("</msg:Msg>" + ls);
		  	
		  	
		      
		      
		      try{
		    	  String nr = Calendar.getInstance().getTimeInMillis() + "";
			    	String fileName3 = parent.getProps().getProperty("conv.dir4")  +  nr  +  ".xml";
		          	FileOutputStream fos = new FileOutputStream(fileName3);
			        fos.write(sb2.toString().getBytes());
			        fos.close();
		            	
			    }catch(Exception e){
			    	e.printStackTrace();
			    }
			  
							
						
				
		  }
	  
	  
	  public static String getTimeNiceString(Date dd){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    cal.setTime(dd);
		    
		    
		    
		    String hour =   cal.get(Calendar.HOUR_OF_DAY) + "";
		    
		    
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}

			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}

			
		    String min = String.valueOf(cal.get(Calendar.MINUTE));
		     if (hour.length() == 1){
		      sb.append("0");
		    }
		     sb.append(hour + ".");
		    
		    if (min.length() == 1){
		     sb.append("0");
		   }
		     sb.append(min);
		    
		    return sb.toString();
		  }
	  
	  public static String getYYYYpMMpDDpTimeNiceString(Date dd){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    cal.setTime(dd);
		    
		    
		    
		    String hour =   cal.get(Calendar.HOUR_OF_DAY) + "";
		    
		    
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}

			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}

			sb.append("" + cal.get(Calendar.YEAR));
			sb.append(month);
			sb.append(day);
		    
		    String min = String.valueOf(cal.get(Calendar.MINUTE));
		    String sec = String.valueOf(cal.get(Calendar.SECOND));
		    String msec = String.valueOf(cal.get(Calendar.MILLISECOND));
		     if (hour.length() == 1){
		      sb.append("0");
		    }
		     sb.append(hour);
		    
		    if (min.length() == 1){
		     sb.append("0");
		   }
		     sb.append(min);
		     if (sec.length() == 1){
		        sb.append("0");
		      }
		    sb.append(sec);
		    sb.append(msec);
		    
		    return sb.toString();
		  }
	  
	  public static String getYYYYpMMpDDString(Date dd){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    cal.setTime(dd);
		    
		    
		    
		    String hour =   cal.get(Calendar.HOUR_OF_DAY) + "";
		    
		    
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}

			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}

			sb.append("" + cal.get(Calendar.YEAR) + "-");
			sb.append(month + "-");
			sb.append(day);
		    
	
		    
		    return sb.toString();
		  }
	  
private double convQty(String prodid){
	double res = 1;
	
	String cmd = " select [Unit Factor] as res  from  mj_po_lines_02 where [Stock Code] = '" + prodid + "'";
	try{
		ResultSet	      rs2 = stmt.executeQuery(cmd);
		 if (rs2.next()) {
		     res = rs2.getDouble("res"); 	
		    }
		 rs2.close();	
	}catch(Exception e){
	e.printStackTrace();	
	}
	

	return res;
}

private void insertCheckLabelDone(InventoryLine lin){
	try{
		 String cmd = "update dbo.checkpoint_table set workerdone='" + lin.getWorker() + "',workernamedone='" + lin.getWorkername() + 
		 "',doneTime = getdate() where checkid='" + lin.getProdId() + "' ";
	
/*CREATE TABLE dbo.checkpoint_table
            (
            worker varchar(20) ,
            workername varchar(200) ,  
        checkid varchar(20) ,
        location  varchar(20) ,
        docnum varchar(20) ,
        pallets numeric(10,2),
        cases numeric(10,2),
        totallabels numeric(10,2),
        currentlabel numeric(10,2),
        workerdone varchar(20) ,
            workernamedone varchar(200) , 
        doneTime datetime ,
        trouble smallint ,
        enterTime datetime ,
        res1 numeric(10,2),
            res2 varchar(200)           )
*/
System.out.println(cmd);
		   Connection cc = DriverManager.getConnection(url,"mobile","mobile");
			PreparedStatement smt = cc.prepareStatement(cmd);
		//	smt.setTimestamp(1, new java.sql.Timestamp(lin.getEnterTime().getTime() ));
			smt.executeUpdate();
			cmd = "delete  from  dbo.checkpoint_table where entertime < getdate()-7";
			smt = cc.prepareStatement(cmd);
			smt.close();
			cc.close();
	}catch(Exception e){
	e.printStackTrace();	
	}
}	  
private void insertCheckLabel(InventoryLine lin){
	try{
		 String cmd = "insert into dbo.checkpoint_table " +
	 		"( worker,workername,checkid,location,docnum,pallets,cases,totallabels,currentlabel,enterTime,res2) values " +
	        "( '" + lin.getWorker() + "','" + lin.getWorkername() + "','" + lin.getProdId() + "'," +
	        		"'" + lin.getLocation() + "','" + lin.getCustId() + "'," + lin.getPartii() +
	        		"," + lin.getToLocation() + "," + lin.getAmount() + "," + lin.getAmountByBooks()+ ",getdate(),'" + lin.getProdName() + "' )";
	
		 
/*CREATE TABLE dbo.checkpoint_table
            (
            worker varchar(20) ,
            workername varchar(200) ,  
        checkid varchar(20) ,
        location  varchar(20) ,
        docnum varchar(20) ,
        pallets numeric(10,2),
        cases numeric(10,2),
        totallabels numeric(10,2),
        currentlabel numeric(10,2),
        workerdone varchar(20) ,
            workernamedone varchar(200) , 
        doneTime datetime ,
        trouble smallint ,
        enterTime datetime ,
        res1 numeric(10,2),
            res2 varchar(200)           )
*/
System.out.println(cmd);
		   Connection cc = DriverManager.getConnection(url,"mobile","mobile");
			PreparedStatement smt = cc.prepareStatement(cmd);
		//	smt.setTimestamp(1, new java.sql.Timestamp(lin.getEnterTime().getTime() ));
			smt.executeUpdate();
			smt.close();
			cc.close();
	}catch(Exception e){
	e.printStackTrace();	
	}
}

  private void changePurcOrderContent(InventoryLine line) throws SQLException{
		  
		  //	1.otsime doknri ylesse 
		  
		  	
		  StringBuffer sb2 = new StringBuffer();
			String ls = System.getProperty("line.separator");
		  
	       sb2.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + ls);
		      sb2.append("<msg:Msg xsi:schemaLocation=\"http://Epicor.com/Message/2.0 http://scshost/schemas/epicor/ScalaMessage.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:msg=\"http://Epicor.com/Message/2.0\">" + ls);			      
		      sb2.append("  <msg:Hdr>" + ls);
		      sb2.append("   <msg:Sender>" + ls);
		      sb2.append("     <msg:Name>Mobil</msg:Name>" + ls);
		      sb2.append("     <msg:Subname>Scala</msg:Subname>" + ls);
		      sb2.append("   </msg:Sender>" + ls);
		      sb2.append("    <msg:Ctrl>" + ls);
		      sb2.append("      <msg:MsgID>" + Calendar.getInstance().getTime().getTime() + "</msg:MsgID>" + ls);
		      sb2.append("    </msg:Ctrl>" + ls);
		      sb2.append("   </msg:Hdr>" + ls);
		      sb2.append("   <msg:Body>" + ls);
		      sb2.append("      <msg:Req msg-type=\"Goods Receive Note\" action=\"Process\">" + ls);
		      sb2.append("    <msg:Dta>" + ls);
		      sb2.append("    <dta:PurchaseOrderDelivery xsi:schemaLocation=\"http://www.scala.net/PurchaseOrderDelivery/1.1 http://scshost/schemas/Scala/1.1/PurchaseOrderDelivery.xsd\" xmlns:msg=\"http://Epicor.com/InternalMessage/1.1\" xmlns:dta=\"http://www.scala.net/PurchaseOrderDelivery/1.1\">" + ls);		      
		      
		      
		      sb2.append("     <dta:OrderHeader>" + ls);
		      sb2.append("     <dta:OrdNum>"  + line.getCustId() + "</dta:OrdNum>"+ ls);
		      sb2.append("     <dta:DelivNoteNum>"  + getYYYYpMMpDDpTimeNiceString(Calendar.getInstance().getTime()) + "</dta:DelivNoteNum>"+ ls);
		      sb2.append("     </dta:OrderHeader>" + ls);
		      
		      sb2.append("        <dta:OrderLineList>" + ls);
		      sb2.append("         <dta:OrderLine>" + ls);
		      sb2.append("         <dta:LineNum>"  + line.getWarehouse() + "</dta:LineNum>"+ ls);
		      sb2.append("         <dta:StockCode>"  + line.getProdId() + "</dta:StockCode>"+ ls);
		     		      
		      sb2.append("             <dta:DelivLineList>"+ ls);
		      
		      sb2.append("              <dta:DelivLine>"+ ls);
		      sb2.append("               <dta:QtyDeliv>"  + ( line.getAmount()/convQty( line.getProdId()) ) + "</dta:QtyDeliv>"+ ls);
		      sb2.append("               <dta:BinCode>A00001</dta:BinCode>"+ ls);//"  + line.getToLocation() + "
		      sb2.append("               <dta:SearchID>"  + line.getLocation() + "</dta:SearchID>"+ ls);
		      sb2.append("               <dta:BatchCode>"  + line.getLocation() + "</dta:BatchCode>"+ ls);
		      sb2.append("               <dta:SuppBatchCode>"  + line.getLocation() + "</dta:SuppBatchCode>"+ ls);	      
		      sb2.append("              </dta:DelivLine>"+ ls);
		      sb2.append("             </dta:DelivLineList>"+ ls);		     
		      sb2.append("         </dta:OrderLine>" + ls);
		      sb2.append("        </dta:OrderLineList>" + ls);
			     
			      sb2.append("    </dta:PurchaseOrderDelivery>" + ls);
			      sb2.append("  </msg:Dta>" + ls);
			      sb2.append("</msg:Req>" + ls);
			      sb2.append("</msg:Body>" + ls);
			      sb2.append("</msg:Msg>" + ls);
		  	
		      
		      try{
		    	  String nr = Calendar.getInstance().getTimeInMillis() + "";
			    	String fileName3 = parent.getProps().getProperty("conv.dir4")  +  nr  +  ".xml";
		          	FileOutputStream fos = new FileOutputStream(fileName3);
			        fos.write(sb2.toString().getBytes());
			        fos.close();
		            	
			    }catch(Exception e){
			    	e.printStackTrace();
			    }
			  				
		  }

	  
	  public String getNimi( String prodID) throws SQLException{

		    String sql2 = "SELECT nimetus FROM toode where toode='" + prodID + "'";

		    String prod = null;

		    Statement st1 = con.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql2);


		      rs1 = st1.executeQuery(sql2);

		      while (rs1.next()) {
		        prod = rs1.getString("nimetus").trim();
		      }
		    

		    //System.out.println("prod1" + prod);
		    rs1.close();
		    st1.close();

		    return prod;
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
	  " SELECT dok.[Sales order],dok.[Received Date]+'','s' + dok.[Sales order] as saaja ,[Stock Code] as toode, (rid.[Qty]) as kogus, " +
		" 0 as hind,ltrim(str(rid.[Qty]))+''+ltrim(str(rid.[Batch Number])) as info,rid.[Batch Number] as ostuale,[BinNumber] as bin  " +
		" , [Line Number] as linenum FROM mj_so_header_shop_" + company + " dok INNER  " +
	    " JOIN  mj_so_lines_shop_" + company + " rid ON  rid.[Sales order] = dok.[Sales order] where (rid.[Qty]) > 0 " +		
		"  ORDER BY 1,9,2 " ;
		
	if (isDebug()) {
	    System.out.println(cmd);
	}

	Vector custProds = new Vector();

	try {
		this.connectionUp();
		  Connection cc = DriverManager.getConnection(url,"mobile","mobile");
		Statement stmt = cc.createStatement();
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String kliendikood = rs.getString("saaja").trim();
		String tootekood = rs.getString("toode").trim();
        double kogus = Double.parseDouble(rs.getString("kogus").trim());  
        double    hind = Double.parseDouble(rs.getString("hind").trim());  
        String info = rs.getString("info").trim();
        double    ale = 0;
        try{
        	ale = Double.parseDouble(rs.getString("ostuale").trim()); 
        } catch ( Exception e){
        	
        }
       
       
   String bin = rs.getString("bin");
   if(bin != null){
	   bin = bin.trim();
	   if(bin.equals("null")){
		   bin = " ";
	   }
   }else{
	   bin = " ";
   }
   //System.out.println(bin);
   String linenum = rs.getString("linenum").trim();
                if (kliendikood.length() > 1) {
                	//Lastdate kaudu anname edasi reaunikaalse ID mille j�rgi siis yritame majandada tagasitulevat rida
                  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,info,0,ale,hind,bin);
                  cp.setAbiKood(linenum);
                  custProds.add(cp);
                }
	    }

            rs.close();
            stmt.close();
            cc.close();
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return custProds;
    }

    public Vector getDebt(String agentCode) throws SQLException{
        Vector list = new Vector();
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
       
	return 0;
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

    public void setBulkSales(Map agents){
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
    public static final String DRIVER_VERSION = "DSVWare";
    private String	       url = "jdbc:odbc:scala";
    private Connection	       con = null;
    private Vector topelt = new Vector();
    private boolean move = false;
    private Statement	       stmt;
    private String	       subjectFilesTo = null;

}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

