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
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.InventoryLine;
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
public class OzolsEkoDBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater,Discounter{
	

	
	public OzolsEkoDBDriver(EksportPanel parent) {
		super(parent);
		this.subjectFilesTo = parent.getString("filemsg");
		// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
		String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
		
		try {
			Class.forName(databaseDriverName).newInstance();
			
		} catch (Exception e) {
			parent.showError(e);
		}
	
		 if(getParent().getProps().getProperty("dns.name") != null){
			 url = "jdbc:odbc:" + getParent().getProps().getProperty("dns.name") ;
			 url2 = "jdbc:odbc:" + getParent().getProps().getProperty("dns.name") ;
		 }
		
		
		 		
	    }

	
	public String getSaveDebtInfo() {
	   	return null;
	   }
	   
	public String getAssortmentStr(Agent agent){
		return null;
	}
	
	public String getCustProdDiscounts(Agent agent){
		return null;
	}
	
	public String getDealerCustomerCSV(String agent){
		return null;
	}
	
		
	  public void setBulkSales(Map agents) {
	       //if (agentCode == null) return 0;
	       connectionUp(url);
	         String cmd = "select rrd.cps_id_m as agent ,sum(netosumma) as sales  from rrd " +
	         		" INNER JOIN rrc on rrd.rrd_id=rrc.rrd_id where sct_id=20 group by rrd.cps_id_m";
	         
	         log(cmd);

	         try {
	             stmt = con.prepareStatement(cmd);
	             //stmt.setString(1, agentCode);

	             Calendar cal = Calendar.getInstance();
	             cal.set(Calendar.DAY_OF_MONTH, 1);

	             ResultSet	      rs = stmt.executeQuery();

	             while(rs.next()) {
	                String agentID = rs.getString("agent").trim();
	                 Agent agent = (Agent)agents.get(agentID);
	                 if (agent != null){
	                   agent.setMonthSales((int)rs.getDouble("sales"));
	                       }
	                    }

	             rs.close();
	             stmt.close();

	         } catch (Exception e) {
	             getParent().showError(e);
	         }

	     }
	
	/**
	 *
	 */
	public void connectionUp(){
		connectionUp(this.url);
	}
	
	
	public String getDistribution() {
	return null;
	}
	
	
	
	private void connectionUp(String url){
		try {
			if (con != null){
			//	con.close();
			}
			con = DriverManager.getConnection(url, getParent().getProps().getProperty("dns.user"),
					getParent().getProps().getProperty("dns.pass"));
		} catch (Exception e) {
			getParent().showError(e);
		}
		
	}
	
	/**
	 *
	 */
	public void connectionDown(){
		try {
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
	
	public TreeMap getProdGroups() throws SQLException {
		 connectionUp(url);
	    TreeMap   tm = new TreeMap();
	    String    sql = "select npg_id as liik,nos as nimi from npg " ;

	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	        tm.put(rs.getString("liik").trim(),
	        		Converter.enCodeEst(rs.getString("nimi").trim()));
	    }
	    rs.close();
	    st.close();
	    connectionDown();
	    return tm;
	}
	TreeMap agents = new TreeMap();
	
	
	public TreeMap getAgents() {
		connectionUp(url);
		String  cmd = "select cps_id as yksus , nos as nimi  , '' as ladu , ll1 as pasw from cps where s_stra = 1 ";
		 agents = new TreeMap();
		 StringBuffer sb2 = new StringBuffer();
		try {
			
			String cmd2 = "SELECT  cps.cps_id_m AS agent   From cps left join cpa on cpa.dok_id=cps.cps_id " +
  " where s_pirc = 1 or s_stra = 1  group by cps.cps_id_m ";
			
			stmt = con.prepareStatement(cmd2);
			ResultSet	      rs = stmt.executeQuery();
			Vector ags = new Vector();
			while (rs.next()) {
			  ags.add(rs.getString("agent").trim());	
			}
			rs.close();
			stmt.close();
			
			stmt = con.prepareStatement(cmd);
				      rs = stmt.executeQuery();
			
			while (rs.next()) {
				Agent agent = new Agent(rs.getString("yksus").trim());
				String nimi = rs.getString("nimi").trim();
				String ladu = rs.getString("ladu").trim();
				agent.setName(nimi +" " + ladu);
				agent.setIsPilot(true);
				String pass = rs.getString("pasw").trim();
				agent.setPassWord(pass);
				if(!ladu.equals("0")){
					agent.setObject(ladu);
				}
				if(!ags.contains(agent.getID())){
					agent.setSednAllClients(true);	
				}
				
              //  System.out.println(agent.getID() + " " + agent.isSednAllClients());
				 sb2.append(agent.getID() + ";" + nimi + " " + ladu + ";" + pass + "\n");
				agents.put(nimi, agent);
			}
		} catch (Exception e) {
			getParent().showError(e);
		}
		  
        if(sb2.length() > 2){
	    	try{
	    		 Converter.saveFileInUTF8( "agent.txt", Converter.enCodeEst(sb2.toString()));
	    	}catch (Exception e){
	    		
	    	}
	      
	     }
        
        String nimi = "LADU";
        Agent agent = new Agent(nimi);
        agents.put(nimi, agent);
        
		//   connectionDown();
		return agents;
	}
	
	

	 
	public TreeMap getCustomers(Agent agent){
		connectionUp(url);
		  Converter cc = new Converter();
		  
		 HashMap kyssad =   cc.getQuestionaire(this.getExcelDir());
		String cmd = "SELECT  rtrim(nos) AS nimi,ltrim(str(cps_id)) as kood0, ltrim(str(cpa_id)) AS kood,kods as code , rtrim(iela) + ' '+ rtrim( pils) + ' ' +  rtrim(pi) AS aadress,0 AS ale, " + 
	   "  '' AS visit , not_used  AS blokk,  cps_id AS payer,'' AS ware  , cpa.cps_id_m AS agent ,cps.cps_id_m as agent2 " +
	   "  ,kredits as limiit, 'pur' as type " +
	   "  From cps left join cpa on cpa.dok_id=cps.cps_id  where s_stra = 0";
		log(cmd);
		
		TreeMap customers = new TreeMap();
		HashMap custMap = new HashMap();
		HashMap comments = new HashMap();
		
		try {
			
			stmt = con.prepareStatement(cmd);
			ResultSet	rs = stmt.executeQuery();
			while (rs.next()) {
				String nimi = rs.getString("nimi");
				if(nimi != null){
				String kk0 = rs.getString("kood0").trim();
				String kk = rs.getString("kood");
				if(kk != null){
					kk = "-" + kk.trim();
				}
					
				Customer cust = new Customer(kk0);
				cust.setName(Converter.enCodeEst(nimi.replace(',',' ') + " " + rs.getString("code").trim()));
				String aadr = rs.getString("aadress");
				if(aadr != null){
					cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace(',',' ')),"Latvia"));
				}
				//String ale = rs.getString("ale");
				//if (ale != null && ale.trim().length() > 0){
					//ale.replace('%',' ');
				//	cust.setDiscount(Double.parseDouble(ale.trim()));
				//}
				//String dist = rs.getString("distribution") ;
				//cust.setVisit(rs.getString("visit").trim());
				
				
				cust.setShowmarketinfo(true);
				//(case when SL01075='0' AND SL01060='0' then 0 else 1 end) AS blokk
				String  blokk = rs.getString("blokk") ;
				if(blokk.trim().equals("1")){
					cust.setSalesBlockT(true);
				}
				
				 cc.addQuestiosToCustomer(cust,kyssad);
				String payer = rs.getString("payer");
				if(payer != null && payer.trim().length() > 0){
					cust.setPayer(payer.trim());
				}
				
				String ware =  rs.getString("ware");
				if(ware != null && ware.trim().length() > 0 && !ware.trim().equals("0")){			
					cust.setWare(ware.trim());
				}
				
				
				String ag = rs.getString("agent");
				if(ag != null && ag.trim().length() > 0){	
					cust.addAgent(new Agent (ag.trim()));
					cust.setMainAgent(ag.trim());
				}
				
				
				String ag2 = rs.getString("agent2");
				if(ag2 != null && ag2.trim().length() > 0){	
					cust.addAgent(new Agent (ag2.trim()));
					cust.setMainAgent(ag2.trim());
				}
				
				cust.addAgent(new Agent("LADU"));
				
				 if("true".equals(getParent().getProps().getProperty("all.customers"))){
	         			Enumeration enume = getParent().getAgentsHash().elements();
	         				while(enume.hasMoreElements()){
	         					Agent a = (Agent)enume.nextElement();
	         					cust.addAgent(a);
	         				}
	         		}else if(getParent().getProps().getProperty("all.customers") != null){
	         			String ags = getParent().getProps().getProperty("all.customers");
	         			StringTokenizer st = new StringTokenizer(ags ,",");
	         			while (st.hasMoreElements()){
	         				Agent aa = new Agent(st.nextToken());
	         				cust.addAgent(aa);
	         			}
	         		}else {
	         			Enumeration enume = getParent().getAgentsHash().elements();
	     				while(enume.hasMoreElements()){
	     					Agent a = (Agent)enume.nextElement();
	     					if(a.isSednAllClients()){
	     						cust.addAgent(a);
	     						
	     					}
	     				}
	         		}
				 
				/*String limiit = rs.getString("limiit");
	             if(limiit != null && limiit.trim().length()>0 && limiit.trim().length()<8){
	               	cust.setCredit(limiit.trim());              	
	             }*/
	                
	            
				if(comments.get(cust.getID()) != null){
					cust.setInfo(Converter.enCodeEst(((String)comments.get(cust.getID())).replaceAll("\n", " ")));
				}
				
			//	if(used.get(cust.getPayer()) != null){
				//	cust.setUsedCredit(used.get(cust.getPayer()));
			//	}
				
				
				
				Customer custo = (Customer)custMap.get(cust.getPayer());
						if(custo != null){
							cust.setDebt(custo.getDebt());
						}
						String tyyp = rs.getString("type");
						 if(tyyp.equals("pur")){
		                       cust.setCity("SUPPLIER");   
		                       cust.setName(Converter.enCodeEst(nimi));
		                   }
						
						
				customers.put(cust.getName(), cust);
				custMap.put(cust.getID(), cust);
				
				}
				}
			rs.close();
			stmt.close();
			
			
			
			 StringBuffer sb2 = new StringBuffer();
			
			 String cmd2 = "SELECT 'logtypes' as tyyp, nos as nimi , npg_id as id from npg WHERE show = 2 " +
			 " union " +
			 " SELECT 'logdef' as tyyp, nos as nimi ,ngp_id as id from ngp WHERE ord = 1  " +
			 " union " +
			 " SELECT 'protypes'+  CONVERT(varchar, npg_id) as tyyp, rtrim(nos)   as nimi ,ngp_id as id from ngp WHERE ord = 2 " +
			 " union " +
			 " SELECT 'housecode' as tyyp, ltrim(str(cnz_id)) +' '+ rtrim(nos) as nimi ,cnz_id as id from cnz";
				

						
						stmt = con.prepareStatement(cmd2);
				        rs = stmt.executeQuery();
						sb2.append("key;value\n");
						while (rs.next()) { 
							String tyyp = rs.getString("tyyp").trim();
							String nimi = rs.getString("nimi").trim();
							String id = rs.getString("id").trim();
						  sb2.append(tyyp + ";" + nimi + " " + id +  "\n");
						}
						rs.close();
						stmt.close();
						
			
			  
   if(sb2.length() > 2){
   	try{
   		 Converter.saveFileInUTF8( "custdetch.txt", sb2.toString());
   	}catch (Exception e){
   		
   	}
     
    }
			
			
			
			
		} catch (Exception e) {
			getParent().showError(e);
		}
		
		return customers;
	}
	
	
	  public static final String addSpaces(String s, int totalLen){
		    int len = s.length();
		    StringBuffer sb = new StringBuffer(s);
		    for (int i=len; i < totalLen ; i++){
		      sb.append(' ');
		    }
		    if( len > totalLen){
		    	return s.substring(0,totalLen);
		    }
		    return sb.toString();
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
	  
	  
	  private void getBatches(){
		  try{

				String cmd = "select nrs.nrs_id as id, nrs.Kods AS toode,nrs.nos AS nimetus, " +
				" nrs.svkods as ean, 'log' as yhik ,nrs.npg_id AS grupp   from nrs  inner join npg on npg.npg_id= nrs.npg_id " +
				" left join ctx on ctx.ctx_id=npg.ctx_id where nrs.sct_id=20";
		          
				  batches = new  HashMap();
				  kodas = new  HashMap();
				  wares  = new  HashMap();
				  batchKodas = new  HashMap();

					if (isDebug()) {
					    System.out.println(cmd);
					}
					  Statement pstm = con.createStatement();
					
					//System.out.println(seisud.size());
		       ResultSet  rs = pstm.executeQuery(cmd);

			    while (rs.next()) {
			    	String value = rs.getString("id").trim();
				String kood = rs.getString("toode").trim();
				String name = Converter.cleanLitLatEst(kood + " " +rs.getString("nimetus").trim());
				

		        Product prod = new Product(kood);
		               name =  name.substring(0,1) + (name.substring(1,name.length())).toLowerCase().trim();
		                prod.setName(name); 
		                String ean = parent.make(rs.getString("ean")); 
		       
		                if(!ean.startsWith("*")){
		                	prod.setEAN(ean);
		                }
		                
		                if(prod.getEAN().trim().length()!=0){
		                	batches.put(kood, ean);	
		                	if(batchKodas.get(ean)!= null){
		                		Vector kk = (Vector)batchKodas.get(ean);
		                		kk.add(kood);
		                		batchKodas.put(ean, kk);
		                	}else{
		                		Vector kk = new Vector();
		                		kk.add(kood);
		                		batchKodas.put(ean, kk);
		                	}
		                }
		                kodas.put(kood, value);
			    
		                
			    }          
		         
			    rs.close();
		            
  String ladu = "select kods as ladu , nns_id as loc from nns";
    rs = pstm.executeQuery(ladu);

  while (rs.next()) {
	  String value = rs.getString("ladu").trim();
	  String value2 = rs.getString("loc").trim();
      wares.put(value, value2);
  }
  	rs.close();
  	pstm.close();
		  }catch (Exception k){
			  
		  }
	  }
private HashMap batches = null;
private HashMap kodas = null;
private HashMap wares = null;
private HashMap batchKodas = null;
		  
	    public String getSaveStockInfo(){
	    	
	    	getBatches();
			
	    	        File dir = new File("import");
	    	        File[] files = dir.listFiles();
	    	        
	    	        
	    	        
	    	        
	             sortFilesDesc(files);
	     	        
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
	    				updateLogIsReady();
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

private boolean move = true;
private Vector topelt = new Vector();


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
  			if(objekt == null ){
  				objekt = "";
  			}else{
  				//lin.setCustId(objekt.substring(1));
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
  			
  		
  			
  			/*if(lin.isTrouble() && !tehtud.contains(lineid)){
  				
  				
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
  	  				
  			}*/
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
			
  		    try{
			   			 saveObject(tehtud, "loetudid.vec"); 
						 }catch(Exception e){
							 e.printStackTrace();
						 }
  			
						 
  			if(lineType.equals("MOVE")){
  				this.registrateMove(lin);
  				brakeBatch(lin);
  			    move = true;
  			}
  			 System.out.println("Line type  " +  lineType);
  			
  			if(lineType.equals("LOGREGISTRATOR")){	 
  				this.registrateLog(lin);
  			    brakeBatch(lin);
  				move = true;
  			}
  			
  			if(lineType.equals("BATCH")){	
  				this.registrateBatch(lin);
  				move = true;
  			}
  			
  			if(lineType.equals("PROSESS")){	
  			   this.registrateProcess(lin, false);
  			   brakeBatch(lin);
  			}
  			
  			if(lineType.equals("FINALIZE")){
  				if(lin.isTrouble()){
  					 this.registrateProcess(lin, true);
  	  				  brakeBatch(lin);
  				}
  			
  			}
  			
  	
  			if(move){
	            tehtud.add(lineid);	
	            try{
		   			saveObject(tehtud, "loetudid.vec"); 
					 }catch(Exception e){
						 e.printStackTrace();
					 }

  			}
    
  			 }catch (Exception e){
  			 	e.printStackTrace();
  			 }
  		
  			addToDB(lin);
				}
  		}
  	
  	}
  }



private void registrateBatch(InventoryLine lin ){
	 	
		
		try{
			
			
	/*		nrs_id           	AgentId              int,
	AgentName            varchar(100),
	ProductCode          varchar(30),
	BatchCode            varchar(30),
	DateCreated          datetime,
	CancelBatch          bit,
	IsOzolsUpd           bit    int*/
			
			 String cmd = "insert into dbo.mj_batches " +
			 		"( AgentId,AgentName , DateCreated ," +
			 		" BatchCode ,ProductCode ,CancelBatch) values " +
			        "( '" + lin.getWorker() + "','" + lin.getWorkername() + "',?,'" + lin.getProdId() + "','" + // siin asenda ID kui tekib 
			           lin.getEan() + "'," + toNum(lin.isTrouble())+ ")" ;
			
		
		
				   Connection cc = DriverManager.getConnection(url,"mobile","mobile");
					PreparedStatement smt = cc.prepareStatement(cmd);
					System.out.println(cmd);
					long time = Calendar.getInstance().getTimeInMillis();
					if(lin.getEnterTime() != null){
						time = lin.getEnterTime().getTime();
					}
					
					
					smt.setTimestamp(1, new java.sql.Timestamp(time));
					
					smt.executeUpdate();
					smt.close();
					cc.close();
					
				
		}catch(Exception e){
			e.printStackTrace();
		}
	
}


private void registrateMove(InventoryLine lin ){
 	
	//kui on Batch scannitud siis liigutame kogu Batchi sisu .Kui ei siis singel item 

	System.out.println("sees " + batchKodas.get(lin.getProdId())  + " " + lin.getProdId() );
	
	String houseID = "0";
	if(lin.getCustId() != null && lin.getCustId().length() > 0 ){
		houseID = lin.getCustId();
	}
	
	if(lin.getProdId().endsWith("xx")){
		lin.setProdId(lin.getProdId().substring(0,lin.getProdId().indexOf("xx")));
	}
	if(batchKodas.get(lin.getProdId()) != null){
  try{
		Vector vv = (Vector)batchKodas.get(lin.getProdId());
		for (int i = 0 ; i <vv.size();i++){
			String prod = (String)vv.elementAt(i);
			
		 String cmd = "insert into dbo.mj_stock_move " +
	 		"( AgentId,AgentName , DateCreated ," +
	 		"  ProductCode, ProductId ,WareTo,WareFrom,HouseId) values " +
	        "( '" + lin.getWorker() + "','" + lin.getWorkername() + "',?,'" + prod + "','" 
	        + kodas.get(prod) + "','" + wares.get(lin.getToWarehouse()) 
	        + "','" + wares.get(lin.getWarehouse()) + "'," + houseID + ")" ;

		   Connection cc = DriverManager.getConnection(url,"mobile","mobile");
			PreparedStatement smt = cc.prepareStatement(cmd);
			System.out.println(cmd);
			long time = Calendar.getInstance().getTimeInMillis();
			if(lin.getEnterTime() != null){
				time = lin.getEnterTime().getTime();
			}
			smt.setTimestamp(1, new java.sql.Timestamp(time));
			smt.executeUpdate();
			smt.close();
			cc.close();
		}
		
}catch(Exception e){
	e.printStackTrace();
}
	
		
		
	}else{
	
	try{
		
		 String cmd = "insert into dbo.mj_stock_move " +
		 		"( AgentId,AgentName , DateCreated ," +
		 		"  ProductCode, ProductId ,WareTo,WareFrom,HoudeId) values " +
		        "( '" + lin.getWorker() + "','" + lin.getWorkername() + "',?,'" + lin.getProdId() + "','" 
		        + kodas.get(lin.getProdId()) + "','" + wares.get(lin.getToWarehouse()) 
		           + "','" + wares.get(lin.getWarehouse()) + "'," +  houseID + ")" ;
	
			   Connection cc = DriverManager.getConnection(url,"mobile","mobile");
				PreparedStatement smt = cc.prepareStatement(cmd);
				System.out.println(cmd);
				long time = Calendar.getInstance().getTimeInMillis();
				if(lin.getEnterTime() != null){
					time = lin.getEnterTime().getTime();
				}
				smt.setTimestamp(1, new java.sql.Timestamp(time));
				smt.executeUpdate();
				smt.close();
				cc.close();
				
			
	}catch(Exception e){
		e.printStackTrace();
	}
	
	}
}




public static void saveObject(Object obj, String file) throws IOException {
	FileOutputStream fos = new FileOutputStream(file);
	ObjectOutputStream oos = new ObjectOutputStream(fos);
	oos.writeObject(obj);
	oos.close();
}	  



private void brakeBatch(InventoryLine lin){
	
	
	if(batches.get(lin.getProdId()) != null){
		
	System.out.println("HasBatch " + lin.getProdId());
	try{
		
				
				 String cmd = "insert into dbo.mj_batches " +
				 		"( AgentId,AgentName , DateCreated ," +
				 		" BatchCode ,ProductCode ,CancelBatch) values " +
				        "( '" + lin.getWorker() + "','" + lin.getWorkername() + "',?,'" +    batches.get(lin.getProdId()) + "','" + // siin asenda ID kui tekib 
				        lin.getProdId() + "',1)" ;
				
			
			
					   Connection cc = DriverManager.getConnection(url,"mobile","mobile");
						PreparedStatement smt = cc.prepareStatement(cmd);
						System.out.println(cmd);
						long time = Calendar.getInstance().getTimeInMillis();
						if(lin.getEnterTime() != null){
							time = lin.getEnterTime().getTime();
						}
						
						
						smt.setTimestamp(1, new java.sql.Timestamp(time));
						
						smt.executeUpdate();
						smt.close();
						cc.close();
						
					
			}catch(Exception e){
				e.printStackTrace();
			}
			
	}
}


private void registrateProcess(InventoryLine lin , boolean finalize){
	 	
		try{
	
			String defe = "0";
			if(lin.isTrouble()){
				defe = "1";
			}
			String fina = "0";
			if(finalize){
				fina = "1";
			}
			
			String procname  = lin.getLocation().replace(')', ' ').replace('(', ' ');
			String procid = procname.substring(procname.lastIndexOf(" ")+1);
			
			String houseID = "0";
			if(lin.getCustId() != null && lin.getCustId().length() > 0 ){
				houseID = lin.getCustId();
			}
			
			 String cmd = "insert into dbo.mj_process " +
			 		"( AgentId,AgentName , DateCreated,ProductID ,ProcessId,PrcessName ,defective," +
			 		" comment ,finalized,HouseId) values " +
			        "( '" + lin.getWorker() + "','" + lin.getWorkername() + "',?," + kodas.get(lin.getProdId()) + ","+ procid +",'" + // siin asenda ID kui tekib 
			        procname + "'," + defe + ",'" + lin.getComment() + "'," + fina+ "," + houseID + ")" ;
			
		
				   Connection cc = DriverManager.getConnection(url,"mobile","mobile");
					PreparedStatement smt = cc.prepareStatement(cmd);
					System.out.println(cmd);
					long time = Calendar.getInstance().getTimeInMillis();
					if(lin.getEnterTime() != null){
						time = lin.getEnterTime().getTime();
					}
					
					smt.setTimestamp(1, new java.sql.Timestamp(time));
					smt.executeUpdate();
					smt.close();
					cc.close();
					
				
		}catch(Exception e){
			e.printStackTrace();
		}
	
}

	  

private void registrateLog(InventoryLine lin ){
	 	
		
		try{
			
			System.out.println("Cust id " + lin.getCustId());
			
			String defid = lin.getProdName().replaceAll("]", " ");
			defid = defid.substring(defid.lastIndexOf(" ")+1);
			if(defid.trim().length() == 0){
				defid = "0";
			}
			String logid = lin.getLocation().replace(')', ' ').replace('(', ' ');
			logid = logid.substring(logid.lastIndexOf(" ")+1);
			
			 String cmd = "insert into dbo.mj_logg_registrator " +
			 		"( AgentId,AgentName , DateCreated , SupplierId, DocumentDate,ProductCode,LoggTypeId,LoggTypeName," +
			 		"LoggDefId,LoggDiam,LoggLen,Comment ,IsPhoto ,DocNo,IsReadyFor) values " +
			        "( '" + lin.getWorker() + "','" + lin.getWorkername() + "',?,'" + lin.getCustId() + 
			        "',?,'" + lin.getProdId() + "',"+ logid +"," + // siin asenda ID kui tekib 
			        		"'" + lin.getLocation().replace(')', ' ').replace('(', ' ') + "'," + defid + 
			        		"  ," + lin.getAmountByBooks() + " ," + lin.getAmount() +  
			          " ,'" + lin.getComment() + "'," + toNum(false)+ " ,'" + lin.getToLocation() + "',0)" ;
			
		
		
				   Connection cc = DriverManager.getConnection(url,"mobile","mobile");
					PreparedStatement smt = cc.prepareStatement(cmd);
					System.out.println(cmd);
					long time = Calendar.getInstance().getTimeInMillis();
					if(lin.getEnterTime() != null){
						time = lin.getEnterTime().getTime();
					}
					
					long kuup = 0; 
					try{
						kuup = Long.parseLong(lin.getPartii());
					}catch(Exception e){
						
					}
					
					smt.setTimestamp(1, new java.sql.Timestamp(time));
					smt.setTimestamp(2, new java.sql.Timestamp(kuup));
					 
					smt.executeUpdate();
					smt.close();
					cc.close();
					
				
		}catch(Exception e){
			e.printStackTrace();
		}
	
}





private void updateLogIsReady(){
 	
	
	try{
	
		String cmd = "update dbo.mj_logg_registrator set IsReadyFor = 1 ";
		
	
			   Connection cc = DriverManager.getConnection(url,"mobile","mobile");
				PreparedStatement smt = cc.prepareStatement(cmd);
				System.out.println(cmd);
				smt.executeUpdate();
				smt.close();
				cc.close();
				
			
	}catch(Exception e){
		e.printStackTrace();
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
			} else {
				lin.setLocation(location);
			}
			String toLocation = lin.getToLocation();
			if(toLocation == null){
				toLocation = "";
			}else {
				lin.setToLocation(toLocation);
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
				String eans = lin.getEan();
				if(eans.length() > 13){
					eans = eans.substring(0,13);
				}
				
				String prod =  lin.getProdId();
				if(prod.length() > 20){
					prod = prod.substring(0,20);
				}
				
				 String cmd = "insert into dbo.mj_inventory_lines " +
				 		"( worker,workername,prodId,location,toLocation,prodName,ean,custId,machine  ,partii ,warehouse  ,lineType ,comment, " +
				        " trouble,toWarehouse,amount,amountbybooks , enterTime ) values " +
				        "( '" + lin.getWorker() + "','" + lin.getWorkername() + "','" + prod + "'," +
				        		"'" + lin.getLocation().replace(')', ' ').replace('(', ' ')  + "','" 
				        		+lin.getLocation().replace(')', ' ').replace('(', ' ') + "','" 
				        		+ lin.getProdName().replaceAll("]", " ") + "'" +
				        		",'" +eans + "','" + lin.getCustId() + "','" + lin.getMachine() + "'" +
				        		"  ,'" + lin.getPartii() + "' ,'" + lin.getWarehouse() + "'  ,'" + lin.getLineType() + "'" +
				          " ,'" + lin.getComment() + "','" + toNum (lin.isTrouble()) + "','" + lin.getToWarehouse() + "',"
				          + lin.getAmount() + ",'" + lin.getAmountByBooks() + "' , getdate() )";
				
			
			
					   Connection cc = DriverManager.getConnection(url,"mobile","mobile");
						PreparedStatement smt = cc.prepareStatement(cmd);
						System.out.println(cmd);
						long time = Calendar.getInstance().getTimeInMillis();
						if(lin.getEnterTime() != null){
							//time = lin.getEnterTime().getTime();
						}
						
						//smt.setTimestamp(1, new java.sql.Timestamp(new Date().getTime()));
						smt.executeUpdate();
						smt.close();
						cc.close();
						
					
			}catch(Exception e){
				e.printStackTrace();
			}
	  }
	 
	  private int toNum(boolean bb){
	    	if(bb){
	    		return 1;
	    	}else{
	    		return 0;
	    	}
	    }
	    

	  
	
	public Vector getDebt(String agentCode) throws SQLException{
		Vector list = new Vector();
		String sql = " SET DATEFORMAT dmy " +
                      " exec atsk_n_setfltr @nSkfId = 540, @cTblV = 'rrd', @dDat_uz = '" + getDateStringYYYYmMMmDD(new Date())+ "'";
		
		connectionUp(url);
		System.out.println(sql);
		stmt = con.prepareStatement(sql);
	
		ResultSet rs = stmt.executeQuery();
		
		while(rs.next()){
			Debt debt = new Debt();
			debt.setDoknr(rs.getString("dok").trim().replace(';','.'));		
			debt.setDuedate(rs.getDate("dat"));
			debt.setSum(Double.parseDouble(rs.getString("doksumma").trim()));
			//debt.setDue(rs.getString("yle"));
			String kl = rs.getString("cps_id");
			debt.setCustID(kl.trim());
			list.addElement(debt);
		}
		rs.close();
		stmt.close();
		return list;
	}
	
	  
	   static public String getDateStringYYYYmMMmDD(Date dat){
		   Calendar cal = Calendar.getInstance();
		   cal.setTime(dat);
		    StringBuffer sb = new StringBuffer();
		    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		    if (day.length() == 1){
		      sb.append("0");
		    }
		    sb.append(day + ".");
		    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
		    if (month.length() == 1){
		      sb.append("0");
		    }
		    sb.append(month + ".");

		   
		    sb.append(cal.get(Calendar.YEAR) );
		  

		    return sb.toString();
		  }
		   
	
	
	  public DealerProduct getDealerProductsList() {


	       DealerProduct dealerProds = new DealerProduct();
	       try {


	         String  sql = " SET DATEFORMAT dmy " +
" exec atsk_n_setfltr @nSkfId = 771, @nRcl = 1, @lNotIncTrn = 0, @lNShowNotRest = 0, @lNShowBSumRest = 0, @lNShowBDzRest = 0, " +
"@lNotShNTurnov = 0, @cGrpCol = '', @cGrpTbl = '', @dDat_b = '" + getDateStringYYYYmMMmDD(new Date())+ "'";

	            if (isDebug()) System.out.println(sql);
	            connectionUp(url);
	               
	               Statement stm = con.createStatement();
	               ResultSet rst = stm.executeQuery(sql);

	                 while (rst.next()) {
	                	    int seis = (int)Double.parseDouble(rst.getString("batl_daudz").replace(',','.'));
	     	                	
	                	 String toode = rst.getString("nrs_id").trim();
	                	 String ladu  =  rst.getString("nns_id").trim();
	                  
	                   dealerProds.add(ladu, toode,seis+""," "," " );
	                   //System.out.println(ladu+" "+toode+" "+seis+"");
	               }
	               rst.close();
	               stm.close();
	               con.close();

	       } catch (Exception e) {
	           getParent().showError(e);
	       }

	        return dealerProds;
	       // return null;
	      }


	
	
	

	
    public TreeMap getProducts(Agent agent) {
        connectionUp();

        
        Hashtable dateOut = new Hashtable();
        StringBuffer buf = new StringBuffer();
	TreeMap products = new TreeMap();
	try {
 
		
        Statement pstm = con.createStatement();
	
		
        
	  String  cmd6 = " SET DATEFORMAT dmy " +
	    " exec atsk_n_setfltr @nSkfId = 771, @nRcl = 1, @lNotIncTrn = 0, @lNShowNotRest = 0, @lNShowBSumRest = 0, @lNShowBDzRest = 0, @lNotShNTurnov = 0, @cGrpCol = '', @cGrpTbl = '', " +
	    "@dDat_b = '" + getDateStringYYYYmMMmDD(new Date())+ "'";
		
	  StringBuffer buf4 = new StringBuffer();
	  Statement st = con.createStatement();
		ResultSet	      rs1 = st.executeQuery(cmd6);
		while (rs1.next()) {
			String ladu = rs1.getString("nnsKods").trim();
			String id = rs1.getString("nrsKods");
			if (id != null) {
				id = id.trim();
				double kogus = 1;
				String location = ladu + "000";
				String partiid = " ";
				String info =  " ";
				
			String blokk = " " ;//rs1.getString("blokk");
				if (partiid != null && partiid.length() > 6) {
					partiid = partiid.trim().substring(partiid.length()-6);
				}
				if (location == null || location.length() == 0) {
					location = " ";
				}
				//System.out.println("location " + location );

				if (ladu == null || ladu.length() == 0) {
					ladu = " ";
				}
				if (blokk == null || blokk.length() == 0) {
					blokk = " ";
				} else {
					blokk = blokk.trim();
					if(blokk.equals("B")){
						blokk = "1";
					}
				}
			
				buf4.append(id + ";" + kogus + ";" + location + ";"
						+ partiid + ";" + ladu + ";" + " ; ;" + info + "\n"); //blokk
               
			}
		}
		rs1.close();
		st.close();
		
		String cmd = "select  nrs.Kods AS toode,nrs.nos AS nimetus, " +
		" nrs.svkods as ean, 'log' as yhik ,nrs.npg_id AS grupp   from nrs  inner join npg on npg.npg_id= nrs.npg_id " +
		" left join ctx on ctx.ctx_id=npg.ctx_id where nrs.sct_id=20";
          
          

			if (isDebug()) {
			    System.out.println(cmd);
			}

			
			//System.out.println(seisud.size());
           ResultSet  rs = pstm.executeQuery(cmd);

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String name = Converter.cleanLitLatEst(kood + " " +rs.getString("nimetus").trim());
		

        Product prod = new Product(kood);
        prod.addBarcode(kood);
		prod.setStock(1);
		prod.setPrice(1);
		
		        name =  name.substring(0,1) + (name.substring(1,name.length())).toLowerCase().trim();
                prod.setName(name); 
                String ean = parent.make(rs.getString("ean")); 
                if(!ean.startsWith("*")){
                	prod.setEAN(ean);
                }
                prod.setUnit(parent.make(rs.getString("yhik")));
                prod.setGroup(parent.make(rs.getString("grupp")));
                
                
                if(prod.getEAN().trim().length()==0){
                    	 dateOut.put(kood.trim(),kood.trim() + "|" + Converter.deCode (name.trim()) + "|" + prod.getStock());
                    	 buf.append(kood.trim() + ";"+ kood.trim() + "|" + Converter.deCode (name.trim()) + "|" + prod.getStock() + "\n");
                    }else{
                    	 dateOut.put(prod.getEAN(),kood.trim() + "|" + Converter.deCode (name.trim()) + "|" + prod.getStock());
                    	 buf.append(prod.getEAN() + ";"+ kood.trim() + "|" + Converter.deCode (name.trim()) + "|" + prod.getStock() + "\n");
                    	 if(prod.getEanCase().trim().length() >0){
                    		 dateOut.put(prod.getEanCase(),kood.trim() + "|" + Converter.deCode (name.trim()) + "|" + prod.getStock());
                        	 buf.append(prod.getEanCase() + ";"+ kood.trim() + "|" + Converter.deCode (name.trim()) + "|" + prod.getStock() + "\n");	 
                    	 }
                    }
                	products.put(name, prod);	
                }
	          
            rs.close();
            
    this.getBatches();
            
            Iterator enumer = batchKodas.keySet().iterator();
            while (enumer.hasNext()){

                String kood = enumer.next() + "";
                Vector vv = (Vector)batchKodas.get(kood);
        		String name = "Batch from " + vv.size() + " logs " + kood ;
        		

                Product prod = new Product(kood+ "xx");
                prod.addBarcode(kood);
        		prod.setStock(1);
        		prod.setPrice(1);
        		prod.setName(name);
        		prod.setEAN(kood);

        		products.put(name, prod);	
            }
            

            
            	//saveObject(dateOut,"tootedRev.has");
    			  if(buf.length() > 2){
    	                File f = new File("tootedRev.txt");
    	                if (f.exists())f.delete();
    	                  try {
    	            		PrintWriter os = new PrintWriter(new FileWriter(
    	            				"tootedRev.txt", true));
    	            		os.write(buf.toString());
    	            		os.close();
    	            	  }catch (IOException ex) {
    	            		ex.printStackTrace();
    	            	  }
    	             }
    			  
    			
    				if (buf4.length() > 2) {
    					File f = new File("stocklocations.txt");
    					if (f.exists())
    						f.delete();
    					try {
    						PrintWriter os = new PrintWriter(new FileWriter(
    								"stocklocations.txt", true));
    						os.write(buf4.toString());
    						os.close();
    					} catch (IOException ex) {
    						ex.printStackTrace();
    						System.exit(0);
    					}
    				}
    			  
    		String cmd3 = "select kods as ladu , '000' as location from nns";
    			  StringBuffer buf2 = new StringBuffer();
    			 	ResultSet rs4 = pstm.executeQuery(cmd3);
    				while (rs4.next()) {
    					String ladu = rs4.getString("ladu").trim();
    					String location = rs4.getString("location");
    					if (location == null || location.length() == 0) {
    						location = " ";
    					}
    					if (ladu == null || ladu.length() == 0) {
    						ladu = " ";
    					}
    					buf2.append(ladu + ";" + ladu+location + "\n");
    				}
    			  rs4.close();
    			  
    			
    			  
    			  
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
	}


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
	public Vector getCustomerProductsList(String agentCode){
		connectionUp(url);
		String cmd =
			
			" select ltrim(str(rrd.cps_id)) + '-' + ltrim(str(cpa_id_sv)) as saaja ,nrs_id as toode ,daudz as kogus ,datums as kuup ,cena as hind  from rrd  " +
			" INNER JOIN rrc on rrd.rrd_id=rrc.rrd_id where sct_id=20 " +
            " GROUP BY datums, rrd.cps_id , cpa_id_sv,nrs_id,daudz,cena,rrc.nos  ORDER BY rrd.cps_id,rrc.nos" ;
		
		
		if (isDebug())
			System.out.println(cmd);
		
		Vector custProds = new Vector();
		HashMap kliendid = new HashMap();
		
		try {
			
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);
			while (rs.next()) {
				String kliendikood = rs.getString("saaja").trim();
				String tootekood = rs.getString("toode").trim();
				double kogus = rs.getDouble("kogus");
				String kuup = rs.getDate("kuup").toString().substring(5,10);
				double hind = rs.getDouble("hind");
				Customer cust = (Customer)kliendid.get(kliendikood);
				if (cust == null){ cust = new Customer(kliendikood);}
				CustProd cp = new CustProd(kliendikood, tootekood, kogus, 0, 0,kuup,hind,0,0," ");
				Product prod = new Product(tootekood);
				cust.addProduct(prod, new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,kuup,hind,0,0," "),false);
				custProds.add(cp);
				kliendid.put(kliendikood, cust);
				//}
			}
			rs.close();
			st.close();
			con.close();
			
		}
		catch (Exception e) {
			getParent().showError(e);
		}
		
		return custProds;
		
	}
	
	
	
	 public String getPilotDayData(Agent agent){
	
	    return null;

	  }

	
	public String getNonMobileOrders(Agent agent){
		
			return null;
	}
	
	public double getSalary(String agentCode) {
		return 0;
	}
	
	
	public double getPromoSales(String agentCode, int nr) throws SQLException{		
		return 0;
	}
	
	
	public ManagerData getManagerData() throws SQLException{
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
	public String getStartingTime() {
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
	
	private static double parseDouble(String s){
		if (s.length() > 0) return Double.parseDouble(s);
		else return 0;
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
	
	// private String url = "jdbc:DBF:///c://work/avallone/dbf";
	public static final String DRIVER_VERSION = "OzolsEko";
	private String             url = "jdbc:odbc:ozols";
	private String             url2 = "jdbc:odbc:ozols";
	private Connection	       con = null;

	private PreparedStatement  stmt = null;
	private String	       subjectFilesTo = null;
	private NumberFormat NF = NumberFormat.getInstance();


	@Override
	public Vector getPriceMatrixData() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Vector getFixedAssortmentData() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

