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
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.EksportPanel;
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
public class InteraltusWareDBDriver extends AbstractDBDriver implements BulkUpdater {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public InteraltusWareDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	
   
	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	}
    }

    public String getSaveDebtInfo() {
    	// System.out.println("xxxxxyyyy");
    	        File dir = new File("import");
    	        File[] files = dir.listFiles();
    	        if(files != null){
    	        
    	        for (int i = 0; i < files.length ; i++){
    	          File file = files[i];
    	  //        System.out.println("xxxxxyyyy");
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
    		//		System.out.println("xxxxx");
    	             insertStock(vec);
    	             File done = new File("import/done/" + file.getName());
    	             if(done.exists()){
    	             	done.delete();
    	             }
    	            file.renameTo(done);
    	             file.delete();
    	            
    	          }    
    	        }	
    	        }
    	        System.exit(0);
    	    	
    	    	return null;
    	       }

    
    
    private void insertStock(Vector vec){
    	if(vec != null){
    		
    		
    		int doknr = 0;
    		
    		try{
    			String sql1 = "select dok.dokumnr from dok inner join rid on dok.dokumnr=rid.dokumnr " +
              " where dok.doktyyp='F' and rid.doktyyp='F' and dok.kinnitatud = 0  and dok.kuupaev > date() -5 " +
              "and trim(dok.tekst1)= ''";
    			 con = DriverManager.getConnection(url);
    			 System.out.println(sql1);
        		Statement st = con.createStatement();
        		ResultSet rs = st.executeQuery(sql1);
        		if (rs.next()){
        			doknr = (rs.getInt("dokumnr"));
        		}
        		rs.close();
        		st.close();
        		con.close();
        			
    		}catch (Exception e){
    			e.printStackTrace();
    		}
    		
    		System.out.println("Dok nr  : " + doknr);
    		
    		
    		
    		if (doknr >0){
    		
    		
    		Enumeration enums = vec.elements();
    		while(enums.hasMoreElements()){
    			InventoryLine lin = (InventoryLine)enums.nextElement();
    			String objekt = lin.getCustId();
    			String location = lin.getLocation();
    			double kogus  = lin.getAmount();
    			String ean = lin.getEan();
    			String prName = lin.getProdName();
    			String wrk = lin.getWorker();
    			String wrkname = lin.getWorkername();
    			String prodid = lin.getProdId();
    			
    			java.sql.Date entered = new java.sql.Date(lin.getEnterTime().getTime());
    			String tund = lin.getEnterTime().getHours()+"";
    			if (tund.length() == 1){
    				tund = "0" + tund;
    			}
    			String minut = lin.getEnterTime().getMinutes()+"";
				if (minut.length() == 1){
					minut = "0" + minut;
    			}
				
				
    			
    			
    			String sqq = "select kogus from rid where dokumnr= " + doknr +" and toode ='" + prodid + "' and doktyyp ='F' ";
    			 try{
    			  
    			 	
    			 	con = DriverManager.getConnection(url);
    			
    			Statement stq = con.createStatement();
    			ResultSet rsq = stq.executeQuery(sqq);
    			double kogusq = 0;
    			if(rsq.next()){
    				kogusq = rsq.getDouble("kogus");
    			}
    			
    			
    			System.out.println(objekt + " " + location + " " + kogus + " " +  prodid + " " + 
    					ean + " " + prName+ " " + wrk + " " + wrkname+ " " + entered.toString() + " " + kogusq);
    			
    			 String sql = " UPDATE RID SET  rtaitkogus= ? WHERE dokumnr =" + doknr + " and doktyyp ='F' and toode ='" + prodid + "'";
    		
    		
    				  con = DriverManager.getConnection(url);
     		        PreparedStatement stpr = con.prepareStatement(sql);
     		        stpr.setDouble(1,kogus + kogusq);
     		      try{
     		        stpr.executeUpdate();  
    			 }catch (Exception e){
    			 	this.log(e.toString());
    			 	log(objekt + " " + location + " " + kogus + " " +  prodid + " " + 
        					ean + " " + prName+ " " + wrk + " " + wrkname+ " " + entered.toString() + " " + kogusq);
    			 	
    			 }
     		      	System.out.println(sql  + "Rida updatetud");
     		      
     		        stpr.close();
     		        con.close();
     		        
    			 }catch (Exception e){
    			 	e.printStackTrace();
    			 }
    		
    			
    		}
    		
    		/*String sss = "update dok set tekst1='Sisselugemine tehtud' where doktyyp='F' and dokumnr=?"  ;
    		try {
				con = DriverManager.getConnection(url);
			
		        PreparedStatement stpr = con.prepareStatement(sss);
		        stpr.setInt(1,doknr);
		      //	stpr.executeUpdate();  
		      	System.out.println(sss  + "Tekst muudetud");
		      
		        stpr.close();
		        con.close();
		        
    		} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		*/
   		  }
    	}
    }

    
    

    
    
    

    
    public String getSaveStockInfo(){
    	
    	//SELLEGA PANEME TODETELE KASTI EAN-id
    	
    	// System.out.println("xxxxxyyyy");
        File dir = new File("import");
        File[] files = dir.listFiles();
        if(files != null){
        
        for (int i = 0; i < files.length ; i++){
          File file = files[i];
  //        System.out.println("xxxxxyyyy");
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
	//		System.out.println("xxxxx");
             insertProdData(vec);
             File done = new File("import/done/data/" + file.getName());
             if(done.exists()){
             	done.delete();
             }
            file.renameTo(done);
             file.delete();
            
          }    
        }	
        }
        System.exit(0);
    	
    return null;
    }
    
    
    
    private void insertProdData(Vector vec){
    	if(vec != null){
    		
       		
    		Enumeration enums = vec.elements();
    		while(enums.hasMoreElements()){
    			InventoryLine lin = (InventoryLine)enums.nextElement();
    			String objekt = lin.getCustId();
    			String location = lin.getLocation();
    			double kogus  = lin.getAmount();
    			String ean = lin.getEan();
    			String prName = lin.getProdName();
    			String wrk = lin.getWorker();
    			String wrkname = lin.getWorkername();
    			String prodid = lin.getProdId();
    			
    			java.sql.Date entered = new java.sql.Date(lin.getEnterTime().getTime());
    			String tund = lin.getEnterTime().getHours()+"";
    			if (tund.length() == 1){
    				tund = "0" + tund;
    			}
    			String minut = lin.getEnterTime().getMinutes()+"";
				if (minut.length() == 1){
					minut = "0" + minut;
    			}
				
    			
    			 try{
    			  
    			 	
    			 	con = DriverManager.getConnection(url);
    			
    			String kog = kogus + "";
    			if(kog.indexOf("E") != -1){
    				kog = kog.substring(0,kog.indexOf("E"));
    			}
    			
    			if(kog.indexOf(".") != -1){
    				kog = kog.substring(0,kog.indexOf(".")) + kog.substring(kog.indexOf(".")+1,kog.length());
    			}

    			
    			System.out.println(objekt + " " + location + " " + kogus + " " +  prodid + " " + 
    					ean + " " + prName+ " " + wrk + " " + wrkname+ " " + entered.toString() + " " + kog);
    			
    			 String sql = " UPDATE toode SET  ribakood3=? WHERE  toode ='" + prodid + "'";
    		
    		
    				  con = DriverManager.getConnection(url);
     		        PreparedStatement stpr = con.prepareStatement(sql);
     		        stpr.setString(1,kog);
     		      try{
     		        stpr.executeUpdate();  
    			 }catch (Exception e){
    			 	this.log(e.toString());
    			 	log(objekt + " " + location + " " + kogus + " " +  prodid + " " + 
        					ean + " " + prName+ " " + wrk + " " + wrkname+ " " + entered.toString() + " " + kog);
    			 	
    			 }
     		      	System.out.println(sql  + "Toode updatetud");
     		      
     		        stpr.close();
     		        con.close();
     		        
    			 }catch (Exception e){
    			 	e.printStackTrace();
    			 }
    		
    			
    		}
    		
   
   		  
    	}
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
           con = DriverManager.getConnection(url);
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
	//String  cmd = "SELECT objekt5,nimetus FROM yksus WHERE klassif=2 and ladu=.T. and objekt5!=''";
	String  cmd = "SELECT yksus,nimetus FROM yksus WHERE klassif=2";
	
	TreeMap agents = new TreeMap();
  StringBuffer sb = new StringBuffer();
	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String nimi = rs.getString("nimetus").trim();
              agent.setName(nimi);
              sb.append(agent.getID() + ";" + nimi+ "\n") ;
		agents.put(nimi, agent);
	    }
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();     
   
        
        Agent agent = new Agent("1");
        agent.setName("ladu");
        agents.put("ladu", agent);
        sb.append( "1;ladu\n") ;
        
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

    
    
    
    
    
    
    
    
    public TreeMap getProducts(Agent agent) {
        connectionUp();
        
        
        
        
        Hashtable dateOut = new Hashtable();
        StringBuffer buf = new StringBuffer();
    	TreeMap products = new TreeMap();
    	
       
        
        String sql = "SELECT kuupaev as alguspaev ,sum(kogus) AS kogus, toode ,partii FROM algsa WHERE " +
    "    YEAR(kuupaev)=YEAR(DATE()) and  MONTH(kuupaev)=MONTH(date())and osak='3' GROUP BY toode,partii ORDER BY kuupaev";
try {
PreparedStatement pstmt = con.prepareStatement(sql);

ResultSet rs = pstmt.executeQuery();
Hashtable tooted = new Hashtable();
Date kuupaev = null;
while(rs.next()){
kuupaev = rs.getDate("alguspaev");
String id = rs.getString("toode").trim();
Toode toode = new Toode(id);
toode.setAlguspaev(kuupaev);
toode.setKogus(rs.getDouble("kogus"));
toode.setLadu(rs.getString("partii").trim());
tooted.put(id, toode);
//System.out.println("1. " + toode);
}
rs.close();
pstmt.close();


sql = "select  sum(r.kogus) AS kogus,r.toode ,r.partii from toode t inner join rid r on t.toode=r.toode " +
    " inner join dok d on r.dokumnr=d.dokumnr where d.kuupaev >= ? " +
    " and d.doktyyp in('O','S','I') AND trim(sihtyksus)='3' group by r.toode,partii";

pstmt = con.prepareStatement(sql);
pstmt.setDate(1, kuupaev);
//pstmt.setString(2, agent.getObject());
rs = pstmt.executeQuery();
while(rs.next()){
String id = rs.getString("toode").trim();
String partii = rs.getString("partii").trim();
Toode toode = (Toode)tooted.get(id);
if (toode == null){
  toode = new Toode(id);
  toode.setLadu(partii);
  tooted.put(id, toode);
} else {
	
	if(toode.getLadu().equals(partii)){
		toode.addKogus(rs.getDouble("kogus"));	
	}
  //System.out.println("2. " + toode);
}
}
rs.close();
pstmt.close();

//System.out.println("1. tooted suurus: " + tooted.size());

sql = "select  sum(r.kogus) AS kogus,r.toode,r.partii from toode t inner join rid r on t.toode=r.toode" +
    " inner join dok d on r.dokumnr=d.dokumnr where d.kuupaev >= ? " +
    " and d.doktyyp in('G','V','Y','K') AND trim(yksus)='3' group by r.toode,r.partii";

pstmt = con.prepareStatement(sql);
pstmt.setDate(1, kuupaev);
rs = pstmt.executeQuery();
while(rs.next()){
String id = rs.getString("toode").trim();
String partii = rs.getString("partii").trim();
Toode toode = (Toode)tooted.get(id);
if (toode == null){
  toode = new Toode(id);
  toode.setLadu(partii);
  tooted.put(id, toode);
} 
if(toode.getLadu().equals(partii)){
	toode.addKogus(-rs.getDouble("kogus"));	
}
  
}
   




StringBuffer buf2 = new StringBuffer();        

Enumeration en2 = tooted.keys();
while(en2.hasMoreElements()){
    String id = (String)en2.nextElement();
    Toode tt = (Toode)tooted.get(id);
    String partii =  tt.getLadu() ;
    if(partii == null ){
    	partii = " ";
    }
    
    if(partii.trim().length() == 0){
    	partii = " ";
    }
    
    
	buf2.append(id + ";" + tt.getKogus() + "; ;" +partii+ ";" + "3" + "\n");        

}
    


if(buf2.length() > 2){
    File f = new File(getDir() + "stocklocations.txt");
    if (f.exists())f.delete();
      try {
		PrintWriter os = new PrintWriter(new FileWriter(
				getDir() + "stocklocations.txt", true));
		os.write(buf2.toString());
		os.close();
	  }catch (IOException ex) {
		ex.printStackTrace();
	  }
 }   


        
    
                  
        // String cmd = "SELECT toode, nimetus, yhik, myygihind FROM toode WHERE (len(trim(toode)) > 3) AND (trim(toode.grupp) = 'L')";
         String  cmd = "SELECT toode, nimetus , 1 as tykke, myygihind, yhik,liik AS grupp,ribakood AS ean FROM toode WHERE  " +
             "   len(trim(toode)) >2 and nimetus !='' ORDER BY nimetus";
           if (isDebug()) {
           	System.out.println(cmd);
           	}


          stmt = con.createStatement();
                       rs = stmt.executeQuery(cmd);

                      while (rs.next()) {
                          String kood = rs.getString("toode").trim();
                          
                        String nimetus =rs.getString("nimetus");
                        Product prod = new Product(kood);
                        
                        prod.setName(nimetus.trim());
                                             
                          String ean = rs.getString("ean");
                          if (ean != null && ean.trim().length()>0){
                            prod.setEAN(ean.trim());
                          
                          if(prod.getEAN().trim().length()==0){
                         	 dateOut.put(kood.trim(),kood.trim() + "|" + deCode (nimetus.trim()) + "|" + prod.getStock());
                         	 buf.append(kood.trim() + ";"+ kood.trim() + "|" + deCode (nimetus.trim()) + "|" + prod.getStock() + "\n");
                         }else{
                         	 dateOut.put(prod.getEAN(),kood.trim() + "|" + deCode (nimetus.trim()) + "|" + prod.getStock());
                         	 buf.append(prod.getEAN() + ";"+ kood.trim() + "|" + deCode (nimetus.trim()) + "|" + prod.getStock() + "\n");
                         }
                          
                          
                          products.put(deCode (nimetus.trim()), prod);

                      }
                      }
                      
                      try {
            			saveObject(dateOut,"tootedRev.has");
            			  if(buf.length() > 2){
            	                File f = new File(getDir() + "tootedRev.txt");
            	                if (f.exists())f.delete();
            	                  try {
            	            		PrintWriter os = new PrintWriter(new FileWriter(
            	            				getDir() + "tootedRev.txt", true));
            	            		os.write(buf.toString());
            	            		os.close();
            	            	  }catch (IOException ex) {
            	            		ex.printStackTrace();
            	            	  }
            	             }
            		} catch (IOException e1) {
            			// TODO Auto-generated catch block
            			e1.printStackTrace();
            		}

        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        return products;
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
    
    
    public static String deCode(String res ){

      	return res;
      }
     
    
    public TreeMap getCustomers(Agent agent) {
        connectionUp();

        String cmd =
	    "  select dok.dokumnr as kood ,kl.nimi,count(toode) as lines  from dok inner join klient kl on kl.kood=dok.klient " +
		" inner join rid on rid.dokumnr=dok.dokumnr and dok.doktyyp=rid.doktyyp  " +
		" where   dok.doktyyp='T'  and kinnitatud = 0 and kuupaev > date()-5 group by dok.dokumnr";


	log(cmd);

	TreeMap customers = new TreeMap();

	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
            String kood = rs.getString("kood").trim();
		Customer cust = new Customer(kood);
		String nimi = kood + " " +rs.getString("nimi").trim() + "<" + rs.getString("lines") + ">" ;
                
                   cust.setName(nimi);
               
                	cust.addAgent(new Agent("1"));          	
                
                customers.put(nimi, cust);
	    }
	} catch (Exception e) {
	    getParent().showError(e);
	}
	return customers;
    }

    
    public Vector getCustomerProductsList(String agentCode) {
        connectionUp();
	
	String cmd =
	    "SELECT dok.dokumnr as saaja ,rid.toode,dok.kuupaev,rid.kogus,rid.hind FROM dok INNER  " +
	    " JOIN rid ON dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode WHERE " +
	    " len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 2 AND " +
		"  dok.doktyyp='T'  and kinnitatud = 0 and kuupaev > date()-5 " +
	    "   AND rid.doktyyp= 'T'   " +
	    "  ORDER BY dok.tasudok, dok.kuupaev";

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
       
   

                if (kliendikood.length() > 1) {
                  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,kuupaev.substring(5,10),0,0,0," ");
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
        connectionUp();
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

    public static final String DRIVER_VERSION = "InteraltusWare";
    private String	       url = "jdbc:odbc:eeva";
    private Connection	       con = null;
    private Statement	       stmt;
    private String	       subjectFilesTo = null;
    
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

