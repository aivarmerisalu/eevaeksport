package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.InventoryLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;

/**
 * Title: Eeva eksport Description: Copyright: Copyright (c) 2002 Company:
 * 
 * @author Tarmo Tubro
 * @version 1.0
 */

public class LeiburWareDBDriver extends AbstractDBDriver implements Manager, Promo,
		BulkUpdater, Discounter {
	public LeiburWareDBDriver(EksportPanel parent) {
		super(parent);
		if (parent.getParentFrame().isDaySales()) {
			this.subjectFilesTo = getParent().getString("senddatamsg");
		} else {
			this.subjectFilesTo = parent.getString("filemsg");
		}
		String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
		try {
			Class.forName(databaseDriverName).newInstance();
			con2 = DriverManager.getConnection(url2);
			stmt2 = con2.createStatement();
		} catch (Exception e) {
			parent.showError(e);
		}
	}

	public String getDealerCustomerCSV(String agent) {
		return null;
	}

	public Vector getLastThreeData() {
		Vector result = new Vector();
		return result;
	}

	/**
   *
   */
	public void connectionUp() {
	}

	public String getAssortmentStr(Agent agent) {
		return null;
	}

	public void insertSuperVisorSQL(SupervisorInfoWrapper wr) {
		
	}

	public String getSaveDebtInfo() {
		return null;
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
					} catch (EOFException e) {
						tehtud = new Vector();
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
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
		  			double kogusPic  = lin.getAmountByBooks();
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
					
		  			if(lineType.equals("INVENTORY") && inventuur){
		  					 }	  	    				
		  	
		  			if(lineType.equals("MOVE")){
		  				//laoliikumine rida
		  			}
		  			 System.out.println("Line type  " +  lineType);
		  			
		  			if(lineType.equals("HEADERDONE")){
		  				//P�is tehtud rida
		  			
		  		      }
				     
		  			if(lineType.equals("CHECKPOINT")){
		  				//P�is tehtud rida
		  			
		  		      }
		  			
		  			if(lineType.equals("FLOWLINE")){
		  				//Flow komplekteerimise rida
		  				try{
		  					
		  					//create table  FlowLine 

		  					/*CREATE TABLE dbo.FlowLine
		  						(
		  						Delivery varchar(50) NOT NULL,
		  						CreatedDate datetime NOT NULL,
		  						Customer varchar(50) NOT NULL,
		  						Product varchar(20) NOT NULL,
		  						AmountPicked numeric (10,2),
		  						PackID varchar (10) NOT NULL,
		  						Route varchar (12) NOT NULL,
		  						WorkerName varchar (100))
		  					*/
		  					String sql = "INSERT INTO FLOWLINE (Delivery,CreatedDate,Customer," +
			  				" Product,AmountPicked,PackID,Route,WorkerName,AmountOnOrder) values (?,?,?,?,?,?,?,?,?)";
			  				
		  					Connection con = getNewConnection();
			  				PreparedStatement st = con.prepareStatement(sql);
			  			
			  				st.setString(1, objekt);//delivery
			  				st.setTimestamp(2, new java.sql.Timestamp(entered.getTime()));
			  				st.setString(3, location);//Customer
			  				st.setString(4, prodid);//Product
			  				st.setDouble(5, kogusPic);
			  				st.setString(6, comment);//PackID
			  				st.setString(7, toLocation);//Route
			  				st.setString(8, wrkname);
			  				st.setDouble(9, kogus);
			  				
			  				
			  				st.executeUpdate();
			  				st.close();
			  				con.close();
			  				
		  				}catch(Exception a){
		  					a.printStackTrace();
		  				}
		  				
		  			
		  		      }
						}
		  	    			 tehtud.add(lineid);
		  	    	
		  					
				            try{
					   			 saveObject(tehtud, "loetudid.vec"); 
								 }catch(Exception e){
									 e.printStackTrace();
								 }
        		  		  }  		
		    		 }
		  		}
		  	
		  

		  
	 public static void saveObject(Object obj, String file) throws IOException {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
			oos.close();
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
    				insertInventory(vec ,true);
    					  File done = new File("import/done/" + file.getName());
    	    	             if(done.exists()){
    	    	             	done.delete();
    	    	             }
    	    	             file.renameTo(done);
    	    	             file.delete();
    	    	         	            
    	           }    
    	        }	
    	        }
    	        System.out.println(topelt);
    	        System.exit(0);
   
     return null;
      }

private Vector topelt = new Vector();

	public String getCustProdDiscounts(Agent agent) {
		return null;
	}

	/**
   *
   */
	public void connectionDown() {
	}

	public TreeMap getAgents() {

		TreeMap agents = new TreeMap();
		aHash = new HashMap();
		  StringBuffer sb = new StringBuffer();
		try {
    
			
			Connection con = getNewConnection();
			Statement st = con.createStatement();
String sql = "select isikukood as kood ,eesnimi + ' ' + perekonnanimi as nimi from Eksped_Persoonid ";
			ResultSet rs = st.executeQuery(sql);
			while (rs.next()) {
				String kood = rs.getString("kood").trim();
				kood = kood.substring(0,kood.length()-2);
				Agent agent = new Agent(kood);
				String name = rs.getString("nimi").trim();
				agent.setName(name);
				agents.put(name, agent);
				aHash.put(agent.getID(), agent);
				 sb.append(agent.getID() + ";" + name+ ";" + agent.getID().substring(7) + "\n") ;
			
				}

				
			Agent ladu = new Agent("LADU");
			ladu.setName("LADU");
			agents.put(ladu.getName(), ladu);
			aHash.put("LADU", ladu);
			
			rs.close();
			st.close();
			con.close();

		} catch (Exception e) {
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
         	 
         	  }
          }
		
		return agents;
	}

	public String getDistribution() {
		return null;
	}

	public TreeMap getCustomers(Agent agent) {
		TreeMap customers = new TreeMap();

			
		try {
			Connection con = getNewConnection();
			Statement st = con.createStatement();
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_YEAR, 1);
            String tana = getDateStr(cal);
            
            File f = new File ("usedata.txt");
        	if(f.exists()){
        		try{
        			FileReader fr = new FileReader("usedata.txt");
        	        BufferedReader reader = new BufferedReader(fr);
        	        String line = " ";
        	  
        	      while( ((line = reader.readLine())!= null   && line.trim().length() > 0 )){
        	    	  tana = line;  
        	    	  System.out.println("usedata date :" + tana);
        	      }
        		}catch (Exception dd){
        			dd.printStackTrace();
        		}
        	
        	}
        		
            
            if(parent.requestData != null){
            	tana = parent.requestData;
            	 System.out.println("MAKEDATA date :" + tana);
            }
			String cmd2 = "Komplekteerimisp�is '"  + tana + "'";

			if (isDebug())
				System.out.println(cmd2);
			ResultSet rs = st.executeQuery(cmd2);
			HashMap custMap = new HashMap();
            Vector custVec = new Vector();
			while (rs.next()) {
				
				String accountnum = rs.getString("Delivery").trim();
				Customer cust = new Customer(accountnum);
            String kood = rs.getString("Customer").trim();
            
				String name = rs.getString("CustomerName").trim() + " " + accountnum;
				cust.setName(name + " " + kood + " " + tana);
				String route = rs.getString("Route");
				cust.setGroup(route.trim());
				cust.setCity(tana);
				
				String aadress1 = rs.getString("DeliveryAddress1");
				if (aadress1 != null) {
					aadress1 = aadress1.trim();
				} else {
					aadress1 = " ";
				}
				String aadress2 = rs.getString("DeliveryAddress2");
				if (aadress2 != null) {
					aadress2 = aadress2.trim();
				} else {
					aadress2 = " ";
				}
				String aadress3 = rs.getString("DeliveryAddress3");
				if (aadress3 != null) {
					aadress3 = aadress3.trim();
				} else {
					aadress3 = " ";
				}
				String aadress4 = rs.getString("DeliveryAddress4");
				if (aadress4 != null) {
					aadress4 = aadress4.trim();
				} else {
					aadress4 = " ";
				}
				cust.setAddress(aadress1 + " " + aadress2 + " " + aadress3 + " " + aadress4 );
			    cust.addAgent(new Agent ("LADU"));
				customers.put(name, cust);
				custMap.put(cust.getID(), cust);
				custVec.add(cust.getID());
			}

			
 Enumeration enut = custVec.elements();
 
 custProds = new Vector();
 
 HashMap kogused = new HashMap();
   while (enut.hasMoreElements()){
	   String cmd = "komplekteerimisread   " + (String)enut.nextElement();

	if (isDebug())
		System.out.println(cmd);
	
	try {
		 con = getNewConnection();
		 st = con.createStatement();
		 rs = st.executeQuery(cmd);
		int cc = 0;
		
		while (rs.next()) {
			cc++;
			String kliendikood = rs.getString("Delivery").trim();
			String tootekood = rs.getString("ItemCode").trim();		
			double kogus = rs.getDouble("Quantity");
			String kuup = rs.getString("OrderLineNumber");
			CustProd cp = new CustProd(kliendikood, tootekood, kogus,
					0, 0, kuup, 0, 0, 0, " ");
			custProds.add(cp);
			
			if(kogused.get(kliendikood) == null){
				kogused.put(kliendikood, kogus + "");
			}else{
				double kog = new Double((String)kogused.get(kliendikood)).doubleValue();
				kogused.put(kliendikood, (kogus + kog) + "");
			}

		}
		rs.close();
		st.close();
		con.close();


		
	} catch (Exception e) {
		getParent().showError(e);
	}


   }
			
	Iterator  ite = kogused.keySet().iterator();
	while(ite.hasNext()){
		String kood = (String)ite.next();
		double vaar = new Double((String)kogused.get(kood)).doubleValue();
		int hhh = (int)(vaar /80);
		if(custMap.get(kood) != null){
			Customer ccc = (Customer)custMap.get(kood);
			ccc.setName(ccc.getName() + " <" + hhh + ">");
		}		
	}

		} catch (SQLException e) {
			getParent().showError(e);
		}
		return customers;
	}

	private Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(url, getParent().getProps()
				.getProperty("dsn.user"),
				getParent().getProps().getProperty("dsn.pass"));
	}

	public TreeMap getProdGroups() throws SQLException {
		TreeMap tm = new TreeMap();
		return tm;
	}

	public TreeMap getProdGroupsSecond() throws SQLException {
		TreeMap tm = new TreeMap();
		return tm;
	}

	public TreeMap getProducts(Agent agent) {


		String cmd = "select * from tooted_baas";

		if (isDebug())
			System.out.println(cmd);
		TreeMap products = new TreeMap();
		try {
			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);

			while (rs.next()) {

				String kood = rs.getString("kood").trim();
				Product prod = new Product(kood);
				String nimetus = rs.getString("nimetus").trim();
				prod.setInCases(rs.getDouble("tykke"));
				prod.setName(nimetus );
				String ean = rs.getString("ean");
				if (ean != null && ean.trim().length() > 7) {
					prod.setEAN(ean.trim());
				}
				String unit = rs.getString("unit");
				if (unit != null && unit.trim().length() > 0) {
					unit = unit.trim();
					prod.setUnit(unit);

				}
				products.put(prod.getName(), prod);
				// }
			}
			rs.close();
			st.close();
			con.close();

		} catch (Exception e) {
			getParent().showError(e);
		}

		return products;
	}

	public Vector getCustomerProductsList(String agentCode) {
		return custProds;

	}

	public void setBulkSales(Map agents) {

	}

	public double getSalary(String agentCode) {
		setSales(0);
		return 0;
	}

	public DealerProduct getDealerProductsList() {
		return null;
	}

	public Vector getDebt(String agentCode) throws SQLException {
		Vector list = new Vector();
				return list;
	}

	public ManagerData getManagerData() throws SQLException {
		ManagerData md = new ManagerData();
		return md;
	}

	public double getFoodSales(String agentCode) throws SQLException {
		return 0;
	}

	public double getAlcoSales(String agentCode) throws SQLException {
		return 0;
	}

	public double getPromoSales(String agentCode, int nr) throws SQLException {
		return 0;
	}

	public String getStartingTime() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH,
				-Integer.parseInt(getParent().getProps().getProperty("months")));
		int month = cal.get(Calendar.MONTH) + 1;
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		String sqlDate = cal.get(Calendar.YEAR) + month + day;
		return sqlDate;
	}

	public String getDriverVersion() {
		return "LeiburWare";
	}

	public String getSubjectFilesTo() {
		return this.subjectFilesTo;
	}

	public String getDaySalesData(Agent agent) {
		return null;
	}

	public String getPilotDayData(Agent agent) {

		return null;
	}

	public void insertDefective(DefectiveWrapper wr) {
		
	}

	public void insertDistribution(DistEvalListWrapper wr) {
	
	}

	public Vector getFixedAssortmentData() {
		return null;
	}



	public String getStartingDateStr() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH,
				-(Integer.parseInt(parent.getParentFrame().getProps()
						.getProperty("months")) + 1));
		String month = (cal.get(Calendar.MONTH) + 1) + "";
		String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (day.length() == 1) {
			day = "0" + day;
		}
		return cal.get(Calendar.YEAR) + "" + month + day;
	}

	public String getDateStr() {
		Calendar cal = Calendar.getInstance();
		String month = (cal.get(Calendar.MONTH) + 1) + "";
		String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (day.length() == 1) {
			day = "0" + day;
		}
		return cal.get(Calendar.YEAR) + "" + month + day;
	}

	public String getDateStr(Calendar cal) {
           if(cal == null){
        	   cal= Calendar.getInstance();
           }
		String month = (cal.get(Calendar.MONTH) + 1) + "";
		String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (day.length() == 1) {
			day = "0" + day;
		}
		return cal.get(Calendar.YEAR) + "" + month + day;
	}

	public String get30AgoStr() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -30);
		String month = (cal.get(Calendar.MONTH) + 1) + "";
		String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (day.length() == 1) {
			day = "0" + day;
		}
		return cal.get(Calendar.YEAR) + "" + month + day;
	}

	

	public TreeMap getDealers() {
		return dealers;
	}

	public void setDealers(TreeMap aDealers) {
		dealers = aDealers;
	}

	public NumberFormat getNF() {
		return NF;
	}

	public void setNF(NumberFormat aNF) {
		NF = aNF;
	}

	// private String url = "jdbc:oracle:thin:@10.10.10.55:1521:MVXEE001";
	private String url = "jdbc:odbc:mj_leibur";
	private String url2 = "jdbc:odbc:laduac";
	private Connection con2;
	private Vector custProds = null;

	private Statement stmt2;
	private String subjectFilesTo = null;
	private NumberFormat NF = NumberFormat.getInstance();
	private TreeMap dealers = new TreeMap();
	HashMap aHash = new HashMap();

	public Vector getPriceMatrixData() {

		return null;
	}

	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}
}
