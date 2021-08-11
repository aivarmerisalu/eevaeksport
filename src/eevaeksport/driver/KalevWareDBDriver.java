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
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.InventoryLine;
import eevaeksport.Product;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class KalevWareDBDriver extends AbstractDBDriver implements BulkUpdater {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public KalevWareDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
	// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	if("true".equals(parent.getProps().getProperty("laodriver"))){
		 this.tavaline = false;
	}
   //dbo.mj_warelines
	//dbo.mj_warelevel
	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	}
    }
    

    public String getSaveStockInfo(){
    	
   	
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
           con = getNewCon();
           stmt = con.createStatement();
	} catch (Exception e) {
	    getParent().showError(e);
	}
      }
    }
    
	private Connection getNewCon() throws SQLException {
		return DriverManager.getConnection(url, "mobilejava", "mobilejava");
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
         /* StringBuffer sb = new StringBuffer();
            String sql = " SELECT yksus,nimetus,osakond as pin FROM yksus WHERE klassif=2  and osakond!='' " ; //where dimension = 40
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

           */       

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
     

	TreeMap customers = new TreeMap();

	Customer tagastus = new Customer("ladu");
	tagastus.setName("ladu");
	tagastus.addAgent(new Agent("LADU"));
	customers.put("ladu", tagastus);
	
	return customers;
    }
    
    
    
    public TreeMap getProducts(Agent agent) {
	//String ext = parent.getProps().getProperty("groups");
	
    	
    	String cmd = "SELECT  SC01016,RTRIM(t.SC01001) AS kood,(RTRIM(SC01002)+' ' +rtrim(SC01003)) + ' ' +RTRIM(t.SC01001) AS nimetus ,SC01015 AS tykke,'' AS stock, " + 
			" 1 as hind,SC01115  AS grupp, SC01132 AS ean  ,  " +
			" 'kst.' as unit  ,  SC01141 as kaal FROM SC010800 t  " +
      " where SC01128 in ('ACT','SUSP','ERITEL','KAMP') and left(ltrim(SC01001),1) not in ('D','N','P','T','R','V') " +
      " ORDER BY LEFT(RTRIM(SC01037),1),t.SC01001  ";
	

	String cmd2 = "select prodid as toode,amount as kogus , location as location, partii as partii, warehouse as ladu ,partii as ssid from mj_warelevel	 " +
" union all " +
" select a.SC03001 as toode,c.SC33005/b.SC01140 as kogus,IsNull(d.LocationID,'') as location, c.SC33015 as partii,SC03002 as ladu ,SC33011 as ssid" +//SC33011 on asi 
" from SC030800 a left join SC010800 b ON a.SC03001=b.SC01001 " +
" left join SC330800 c ON a.SC03001=c.SC33001 and a.SC03002=c.SC33002 and c.SC33012=0  " +
" Left Join VKLS_LocationID_08 d ON c.SC33004=d.BinID And d.WhID=a.SC03002  " +
" Where  a.SC03002 like '23' and c.SC33005<>0 and LocationID is not null";

	String cmd3 = " select WhID as ladu,LocationID as location from VKLS_LocationID_08 where WhID in ('23','03','31') ";

	String sql = "select SC03001 as toode,SC33011 as code from  SC030800 a left join SC010800 b ON a.SC03001=b.SC01001 " +
   " left join SC330800 c ON a.SC03001=c.SC33001 and a.SC03002=c.SC33002 and c.SC33012=0  " +
   " where SC33011 != '' and a.SC03003<>0 order by SC03001";
	
	if (isDebug())
		System.out.println(cmd);
	if (isDebug())
		System.out.println(cmd2);
	TreeMap products = new TreeMap();
	try {

		StringBuffer buf = new StringBuffer();
		StringBuffer bu = new StringBuffer();
		Connection con = getNewCon();
		Statement st = con.createStatement();
		ResultSet rs1 = st.executeQuery(cmd2);
		while (rs1.next()) {

			String id = rs1.getString("toode");
			if (id != null) {
				id = id.trim();
				double kogus = rs1.getDouble("kogus");
				kogus = kogus * 100;
				int kkk = (int)kogus;
				double fff = Double.parseDouble(kkk + "") /100;
				String location = rs1.getString("location");
				String partiid = rs1.getString("partii").substring(0,10);
				String ladu = rs1.getString("ladu");
				String ssid =  rs1.getString("ssid");
			//	String blokk = rs1.getString("blokk");
				if (partiid != null) {
					// partii=
					// EksportPanel.getDateMMpDDpAAAAStr(partiid.getTime(),
					// 0);
				}
				if (location == null || location.length() == 0) {
					location = " ";
				}

				if (ssid == null || ssid.length() == 0) {
					ssid = " ";
				}
				if (ladu == null || ladu.length() == 0) {
					ladu = " ";
				}
				
					 String blokk = " ";
				
				buf.append(id + ";" + fff + ";" + location + ";"
						+ partiid + ";" + ladu + ";" + blokk + ";" + ssid +"\n");

			}
		}
		st.close();
		con.close();
		con = getNewCon();
		st = con.createStatement();
		// Statement stmt = con.createStatement();
		ResultSet rs = st.executeQuery(sql);
		HashMap barcHash = new HashMap();

		
		System.out.println("barcodestart");
		while (rs.next()) {
			String toode = rs.getString("toode").trim();
			String code = rs.getString("code").trim();
			if(barcHash.get(toode)!= null){
				Vector vv = (Vector)barcHash.get(toode);
				vv.add(code);
				barcHash.put(toode,vv);
			}else{
				Vector vv = new Vector();
				vv.add(code);
				barcHash.put(toode, vv);
			}
		}

		
		st.close();
		con.close();
		con = getNewCon();
		st = con.createStatement();
		// Statement stmt = con.createStatement();
		 rs = st.executeQuery(cmd);
		HashMap prodHash = new HashMap();

		Hashtable dateOut = new Hashtable();
		System.out.println("prodstart");
		while (rs.next()) {
			String kood = rs.getString("kood").trim();
			Product prod = new Product(kood);
			String nimetus = rs.getString("nimetus").trim();
			
			/*double kastis = rs.getDouble("tykke");
			if (kastis > 0) {
				prod.setInCases(kastis);
			}*/
			prod.setPrice(rs.getDouble("hind"));
			/*double onpal = rs.getDouble("onpallet");
			if (onpal > 0) {
				prod.setOnPallet(onpal);
			}*/
			String ean = rs.getString("ean");
			if (ean != null && ean.trim().length() > 0) {
				if (prodHash.get(prod.getID()) != null) {
					System.out.println("Lisame teist EAAN-i");
					prod = (Product) prodHash.get(prod.getID());
					prod.setEanCase(ean);
					System.out.println("Lisame teist EAAN " + prod.getEAN()
							+ " " + prod.getEanCase());
				} else {
					prod.setEAN(ean);

				}
			}

			String unit = rs.getString("unit");
			if (unit != null && unit.trim().length() > 0) {
				prod.setUnit(unit);
			}
   
			
			if(barcHash.get(prod.getID()) != null){
				Enumeration en = ((Vector)barcHash.get(prod.getID())).elements();
				
				 while (en.hasMoreElements()){				 
					prod.addBarcode((String)en.nextElement());
				}	
			}
			
			
			if (prod.getEAN().trim().length() == 0) {
				dateOut.put(kood.trim(), kood.trim() + "|" + nimetus.trim()
						+ "|" + prod.getStock());
				bu.append(kood.trim() + ";" + kood.trim() + "|"
						+ deCode(nimetus.trim()) + "|" + prod.getStock()
						+ "\n");
			} else {
				dateOut.put(
						prod.getEAN(),
						kood.trim() + "|" + nimetus.trim() + "|"
								+ prod.getStock());
				bu.append(prod.getEAN() + ";" + kood.trim() + "|"
						+ deCode(nimetus.trim()) + "|" + prod.getStock()
						+ "\n");
				if (prod.getEanCase().trim().length() > 0) {
					dateOut.put(prod.getEanCase(),
							kood.trim() + "|" + deCode(nimetus.trim())
									+ "|" + prod.getStock());
					bu.append(prod.getEanCase() + ";" + kood.trim() + "|"
							+ deCode(nimetus.trim()) + "|"
							+ prod.getStock() + "\n");
				}
			}

			prod.setName(nimetus);

			products.put(nimetus, prod);
			prodHash.put(prod.getID(), prod);

		}
		rs.close();
		st.close();
		con.close();

		try {
//			saveObject(dateOut, "tootedRev.has");
			if (bu.length() > 2) {
				File f = new File("tootedRev.txt");
				if (f.exists())
					f.delete();
				try {
					PrintWriter os = new PrintWriter(new FileWriter(
							"tootedRev.txt", true));
					os.write(bu.toString());
					os.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(0);
		}

		if (buf.length() > 2) {
			File f = new File("stocklocations.txt");
			if (f.exists())
				f.delete();
			try {
				PrintWriter os = new PrintWriter(new FileWriter(
						"stocklocations.txt", true));
				os.write(buf.toString());
				os.close();
			} catch (IOException ex) {
				ex.printStackTrace();
				System.exit(0);
			}
		}

		StringBuffer buf2 = new StringBuffer();
		con = this.getNewCon();
		st = con.createStatement();
		ResultSet rs4 = st.executeQuery(cmd3);
		while (rs4.next()) {

			String ladu = rs4.getString("ladu");
			String location = rs4.getString("location");

			if (location == null || location.length() == 0) {
				location = " ";
			}

			if (ladu == null || ladu.length() == 0) {
				ladu = " ";
			}

			buf2.append(ladu + ";" + location + "\n");

		}
		try {
			if (con != null) {
				con.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (buf2.length() > 2) {
			File f = new File("warestatic.txt");
			if (f.exists())
				f.delete();
			try {
				PrintWriter os = new PrintWriter(new FileWriter(
						"warestatic.txt", true));
				os.write(buf2.toString());
				os.close();
			} catch (IOException ex) {
				ex.printStackTrace();
				System.exit(0);
			}
		}

	} catch (Exception e) {
		getParent().showError(e);
		System.exit(0);
	}

	System.out.println("proddone");
	return products;

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
	  			
	  			
	  		
	  			 	
	  			 	//System.out.println(objekt + "'" + location+ "'" + toLocation + "'" + kogus + "'" +  prodid + "'" + 
	  					//ean + "'" + prName+ "'" + wrk + "'" + wrkname+ "'" + entered.toString() +
	  					//"'" +warehouse + "'" +toWarehouse + "-" + partii );
	  		 
	  			// Siin tuleb lisada kolm varianti inentuur ja rida ja p�is.
	  			 
	  			if(lineType.equals("INVENTORY") && inventuur){
	  				//inventuuri rida
	  		        try{
			   			 saveObject(tehtud, "loetudid.vec"); 
						 }catch(Exception e){
							 e.printStackTrace();
 			        }
 			
 			
	  	    	}
	  					
			    
	  			
	  			if(lineType.equals("MOVE")){
	  		  String outLoc =     "08-20-VVVVVV";
	  			  String inLoc =  "08-20-SSSSSS"; 
	  			  String defLoc = "08-20-DDDDDD";
	  			  if(getParent().getProps().getProperty("ware.inloc") != null){
	  				inLoc = getParent().getProps().getProperty("ware.inloc");
	  			  }
	  			if(getParent().getProps().getProperty("ware.outloc") != null){
	  				outLoc = getParent().getProps().getProperty("ware.outloc");
	  			  }
	  			if(getParent().getProps().getProperty("ware.defloc") != null){
	  				defLoc = getParent().getProps().getProperty("ware.defloc");
	  			  }
	  			
	  			//1. lisame rea teabelisse 
	  			
	  			
	  			String inssql  = "INSERT INTO mj_warelines  (custid,location,tolocation,prodid,partii,prodName,workerid,amount,worker,linetype,linedate,comment,warehouse,towarehouse) values('" 
	  				+ objekt + "','" + location + "'" + ",'" + toLocation + "','" + prodid + "','" + partii + "','" + prName + "','" + wrk + "'," +
	  							 kogus+ " ,'" + wrkname + "','MOVE',?,'"+ ean +"','23','23')";
	  			System.out.println(inssql);
	  		
	  			
	  			PreparedStatement st = con.prepareStatement(inssql);
	  			st.setTimestamp(1, new java.sql.Timestamp(lin.getEnterTime().getTime()));
	  			st.executeUpdate();
	  			   
	  			   
	  				//System.out.println(objekt + "'" + location+ "'" + toLocation + "'" + kogus + "'" +  prodid + "'" + 
		  				//	ean + "'" + prName+ "'" + wrk + "'" + wrkname+ "'" + entered.toString() +
		  				//	"'" +warehouse + "'" +toWarehouse + "-" + partii );	
	  			   
	  			   // 1.laoseisu muutmine 
	  			        //  a.vaatame kas l�pppesas on kaup sees   muudame olemasoleva aluse kogust (alus l�ks v�lja )
	  			   //
	  		   
	  			 if (outLoc.equals(toLocation)){
	  				   //kui on v�lja kanne siis kontrollime toote olemasolu pesas koos kogusega ja
	  				   //kui on 0 siis maha kui j��k on erinev 0 st siis update 
	  		  				   
	  				 inssql  = "SELECT amount - " + kogus+ " AS kogus,prodid from dbo.mj_warelevel where location = '" + toLocation + "' and prodid ='" + prodid + "'";
	  				
	  				 System.out.println("v�lja kanne " + inssql);
	  				 Statement sst = con.createStatement();
	  				 ResultSet rs = sst.executeQuery(inssql);
	  				 if(rs.next()){
		  					double am = rs.getDouble("kogus");
		  					if(am == 0){
		  						//kustutame pesast maha 
		  						String sqll  = "DELETE from  dbo.mj_warelevel where location = '" + location + "'" +
	  					 		" and prodid= '" + rs.getString("prodid") + "'";
	  					        Statement ssl = con.createStatement();
	  					        ssl.executeUpdate(sqll);
	  					      System.out.println(sqll);
		  					}else if(am > 0){
                        // muudame toote kogust pesas
		  						String sqll  = "UPDATE dbo.mj_warelevel set lastdate = getdate(), amount  = " + am+ " where location = '" + location+ "'" +
	  					 		" and prodid= '" + rs.getString("prodid") + "'";
	  					        Statement ssl = con.createStatement();
	  					        ssl.executeUpdate(sqll);
	  					      System.out.println(sqll);
		  					}else if(am < 0){
                        // kui j��b negatiivseks siis paneme vigadesse koos kommentaariga
		  						String sqll  = "UPDATE dbo.mj_warelevel set location='" + defLoc + "' ,lastdate = getdate() " +
	  					 		" , warehouse = 'Kogus l�ks Miinusesse " + location + "', amount  = " + am + " where location = '" + location+ "'" +
	  					 		" and prodid= '" + rs.getString("prodid") + "'";
	  					        Statement ssl = con.createStatement();
	  					        ssl.executeUpdate(sqll);
	  					      System.out.println(sqll);
		  					}
		  				 }
		  				 
	  			   }else if(!outLoc.equals(toLocation) && !inLoc.equals(toLocation) &&
		  					 !outLoc.equals(location) && !inLoc.equals(location)){
		  				//kui on  sisemine liigutamine
	  				   System.out.println("Sisemine liigutamine");
	  				   //Kustutame maha vanas asukohas toote 
	  					String sqll  = "DELETE from  dbo.mj_warelevel where location = '" + location + "'" +
					 		" and prodid= '"  + prodid + "'";
					        Statement ssl = con.createStatement();
					        ssl.executeUpdate(sqll);
					        ssl.close();
					        
					   //Ja lisame uude kohta
	  	
					        
					        inssql  = "SELECT amount,prodid from dbo.mj_warelevel where location = '" + toLocation + "'";
			  				System.out.println("Pesasse panek " + inssql);
			  				 Statement sst = con.createStatement();
			  				 ResultSet rs = sst.executeQuery(inssql); 
			  				 if(rs.next()){
			  					//kaup on sees ja peame ta �ra liigutama defekti pessa
			  					  sqll  = "UPDATE dbo.mj_warelevel set location = '" + defLoc+ "',lastdate = getdate() " +
			  					 		" , warehouse = '" + location + " -> " +toLocation + "' where location = '" + toLocation+ "'" +
			  					 		" and prodid= '" + rs.getString("prodid") + "'";
			  					Statement ss3 = con.createStatement();
			  					ss3.executeUpdate(sqll);
			  				   System.out.println(sqll);
			  					ss3.close();
			  				 }
			  				 
			  				 //lisame rea baasi uue lisaga 
			  				 inssql  = "INSERT INTO dbo.mj_warelevel (location,amount,prodid,partii,lastdate,comment,warehouse,prodName) values(" +
			  				 		"'" + toLocation + "'," + kogus+ ",'" + prodid + "','" + partii + "',getdate(),'"+ ean +"','','"+ prName +"')";
				  				System.out.println("Pesasse panek " + inssql);
				  				 sst = con.createStatement();
				  			     sst.executeUpdate(inssql); 
			  				 
			  	
					        
					        
					        
					        
		  			   }else if(!outLoc.equals(toLocation) && !inLoc.equals(toLocation)){
	  	            // kaup tuleb sisse 
	  				   inssql  = "SELECT amount,prodid from dbo.mj_warelevel where location = '" + toLocation + "'";
	  				System.out.println("Pesasse panek " + inssql);
	  				 Statement sst = con.createStatement();
	  				 ResultSet rs = sst.executeQuery(inssql); 
	  				 if(rs.next()){
	  					//kaup on sees ja peame ta �ra liigutama defekti pessa
	  					 String sqll  = "UPDATE dbo.mj_warelevel set location = '" + defLoc+ "',lastdate = getdate() " +
	  					 		" , warehouse = '" + location + " -> " +toLocation + "' where location = '" + toLocation+ "'" +
	  					 		" and prodid= '" + rs.getString("prodid") + "'";
	  					Statement ssl = con.createStatement();
	  					ssl.executeUpdate(sqll);
	  				 }
	  				 
	  				 //lisame rea baasi uue lisaga 
	  				 inssql  = "INSERT INTO dbo.mj_warelevel (location,amount,prodid,partii,lastdate,comment,warehouse,prodName) values(" +
	  				 		"'" + toLocation + "'," + kogus+ ",'" + prodid + "','" + partii + "',getdate(),'"+ ean +"','','"+ prName +"')";
		  				System.out.println("Pesasse panek " + inssql);
		  				 sst = con.createStatement();
		  			     sst.executeUpdate(inssql); 
	  				 
	  				 
	  				 
	  			   }
	  			
					
	  	
	  			   
	  			tehtud.add(lineid);
	  		     try{
		   			 saveObject(tehtud, "loetudid.vec"); 
					 }catch(Exception e){
						 e.printStackTrace();
			        }
	  			}
	  			 System.out.println("Line type  " +  lineType);
	  			
	  			if(lineType.equals("HEADERDONE")){
	  				//P�is tehtud rida
	  				
	  			}
	  			

	  			if(lineType.equals("LINECHANGE")){
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
    					
    					e.printStackTrace();
    				} catch (ClassNotFoundException e) {
    					e.printStackTrace();
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
		
	
	
	  private void changeOrderContent(InventoryLine line) throws SQLException{
		  
		  //	1.otsime doknri ylesse 
		  
		  	
		  	String sqldok = "Select dokumnr from dok where tasudok='" + line.getCustId() + "' and dok.doktyyp='T'";
		  	
		  	int kanne = 0;
		  	
		  	Statement sm = con.createStatement();
		  	ResultSet rr = sm.executeQuery(sqldok);
		  	if(rr.next()){
		  		kanne = rr.getInt("dokumnr");
		  	}
		  	rr.close();
		  	sm.close();
		  	
		  	
		  	
		  	//2. K�ime l�bi read ja vaatame kas seal dokumendil on selline rida  
		  	
						
						
						String sqlotsi = "select toode from rid where dokumnr="+ kanne + " and doktyyp='T' and toode='" + line.getProdId()+"'";
						System.out.println("sqlotsi" + sqlotsi);
						Statement so = con.createStatement();
						ResultSet sorr = so.executeQuery(sqlotsi);
						if(sorr.next()){
							System.out.println("Toode olemas UPDATE ");
							String sql2 = "UPDATE rid set kogus=" + line.getAmount() + " where dokumnr = "+ kanne + " and doktyyp ='T' and toode='" + line.getProdId()+"'";
							Statement aa = con.createStatement();
							aa.executeUpdate(sql2);
							aa.close();

							
						} else {
							System.out.println("Toote lisamine INSERT");
							
							String sql = "INSERT INTO rid VALUES('T',?, ?, ?, ?, ?,'','','',0,'','','','',date()," +
						    "0,0,0,0,'','','','','','','',0,0,'','','',0,0,.F.,.F.,0,'',.F.,0,.F.,'',0,'',0,0)";
										PreparedStatement st = con.prepareStatement(sql);	
							st.setInt(1, kanne);
							String toode = line.getProdId();
							if (toode == null){
							toode = " ";
							}
							
							st.setString(2, line.getProdName());
							
							st.setDouble(3, 0); //hind 
							
							
							st.setDouble(4, line.getAmount());
							st.setString(5, toode);
							st.executeUpdate();
							st.close();
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
    public Vector getCustomerProductsList(String agentCode) {
    	
    	Vector custProds = new Vector();

    	/*        connectionUp();
	
	String cmd =
	    " SELECT dok.tasudok as saaja ,rid.toode,dok.kuupaev,rid.kogus,rid.hind FROM dok INNER " +
	    " JOIN rid ON dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode WHERE " +
	    " len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 2  " +
	    "   AND  dok.doktyyp = 'T'  AND rid.doktyyp= 'T'  and tekst1 not like '%Arve%' and tekst1 not like '%ljastus%' " +
	    "   and kuupaev > date()-30 ORDER BY dok.tasudok,dok.kuupaev";

	if (isDebug()) {
	    System.out.println(cmd);
	}


	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String kliendikood = rs.getString("saaja").trim();
		String tootekood = rs.getString("toode").trim();
		 String kuupaev = rs.getString("kuupaev");
        double kogus = Double.parseDouble(rs.getString("kogus").trim());
       
        double hind = 0;
        if(!tavaline){
            hind = Double.parseDouble(rs.getString("hind").trim());              	
        }

                if (kliendikood.length() > 1) {
                  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,kuupaev.substring(5,10),0,0,hind," ");
                  custProds.add(cp);
                }
	    }

            rs.close();

	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	*/return custProds;
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
    public static final String DRIVER_VERSION = "KalevWare";
	private String url = "jdbc:odbc:scalaDB";
    private Connection	       con = null;
    private Vector topelt = new Vector();
    private boolean move = false;
    private Statement	       stmt;
    private String	       subjectFilesTo = null;
    private boolean tavaline = true;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

