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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.EksportPanel;
import eevaeksport.InventoryLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class MovexLaduDBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater{
  public MovexLaduDBDriver(EksportPanel parent){
    super(parent);
    if(parent.getParentFrame().isDaySales()){
         this.subjectFilesTo = getParent().getString("senddatamsg");
    } else {this.subjectFilesTo = parent.getString("filemsg");}
    String databaseDriverName = "oracle.jdbc.driver.OracleDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection("jdbc:oracle:thin:@10.10.10.56:1521:M3EEPROD", getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));;
      stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }

          public String getDealerCustomerCSV(String agent){
           return null;
           }

  /**
   *
   */
  public void connectionUp(){
  }

  public String getAssortmentStr(Agent agent){
   return null;
      }

  private void insertStock(Vector vec){
  	if(vec != null){
  		
  		
  		int doknr = 0;
  		//1 dokumensi  numbri m��ramine
  		
  		try{
  			String sql1 = "select dok.dokumnr from dok inner join rid on dok.dokumnr=rid.dokumnr " +
            " where dok.doktyyp='K' and rid.doktyyp='K' and dok.kinnitatud = 0 and dok.inventuur=1 and dok.kuupaev > date() -5 " +
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
				
				
  			
  			
  			String sqq = "select kogus from rid where dokumnr= " + doknr +" and toode ='" + prodid + "' and doktyyp ='K' ";
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
  			
  			 String sql = " UPDATE RID SET  rtaitkogus= ? WHERE dokumnr =" + doknr + " and doktyyp ='K' and toode ='" + prodid + "'";
  		
  		
  				  con = DriverManager.getConnection(url);
   		        PreparedStatement stpr = con.prepareStatement(sql);
   		        stpr.setDouble(1,kogus + kogusq);
   		      try{
   		        stpr.executeUpdate();  
  			 }catch (Exception e){
  			 	this.log(e.toString());
  			 	
  			 }
   		      	System.out.println(sql  + "Rida updatetud");
   		      
   		        stpr.close();
   		        con.close();
   		        
  			 }catch (Exception e){
  			 	e.printStackTrace();
  			 }
  		
  			
  		}
  		
  		String sss = "update dok set tekst1='Sisselugemine tehtud' where doktyyp='K' and dokumnr=?"  ;
  		try {
				con = DriverManager.getConnection(url);
			
		        PreparedStatement stpr = con.prepareStatement(sss);
		        stpr.setInt(1,doknr);
		      	stpr.executeUpdate();  
		      	System.out.println(sss  + "Tekst muudetud");
		      
		        stpr.close();
		        con.close();
		        
  		} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
  		
 		  }
  	}
  }


  public String getSaveDebtInfo() {
  	// System.out.println("xxxxxyyyy");
  	        File dir = new File("import");
  	        File block = new File("block.txt");
  	        if(block.exists()){
  	        	System.exit(0);
  	        }
  	        try {
					saveObject("",block.getName());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
  	        
  	        File[] files = dir.listFiles();
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
  	        
  	        if(block.exists()){
  	        	block.delete();
  	        }
  	        System.exit(0);
  	    	
  	    	return null;
  	       }


  public String getSaveStockInfo(){
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
    String cmd = "SELECT juusid AS id ,jutfno AS parool,SubStr(jutx40,9 ) AS nimi" +
    		" FROM mvxjdta.cmnusr WHERE juaupf='PICKERS' AND jutx40 LIKE 'KOMPL%'";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();

    StringBuffer sb = new StringBuffer();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          String  agent = (rs.getString("id").trim());
          String  parool = (rs.getString("parool").trim());
          String name = rs.getString("nimi").trim();
          sb.append(agent + ";" + name + ";" + parool + "\n");
       
        }
    rs.close();
    con.close();

    
    
    Agent agent = new Agent("LADU");
    agent.setName("ladu");
    agents.put("ladu", agent);
    
    
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
    
    
    
    } catch (Exception e){
        getParent().showError(e);
      }

    return agents;
  }



      public String getDistribution() {
      return null;
      }

      private String getDir(){
          String dir = getParent().getProps().getProperty("readin");
          if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
          return dir;
        }



  public TreeMap getCustomers(Agent agent){
    TreeMap customers = new TreeMap();

    try {
      Connection con = getNewConnection();
      Statement st = con.createStatement();

      String cmd2 = "  SELECT MWWHLO AS kood ,MWWHNM AS nimi FROM mvxjdta.MITWHL WHERE " +
		                        "    MWCONO='200' ORDER BY   MWWHNM ";


      if (isDebug()) System.out.println(cmd2);
      ResultSet rs = st.executeQuery(cmd2);
   
      StringBuffer sb = new StringBuffer();
      
      while(rs.next()){
          String kood = rs.getString("kood").trim();
          String nimi = rs.getString("nimi").trim();      
          sb.append(kood + ";" + nimi + "\n");
         
    }
      
      
      
      
      Customer cust = new Customer("alecoq");
      
		 Enumeration enums = getParent().getAgentsHash().elements();
      while (enums.hasMoreElements()) {
        Agent a = (Agent) enums.nextElement();
        cust.addAgent(a);
        }
      
     cust.setName("Alecoq");
     customers.put("Alecoq", cust);      
      
      
     
     if(sb.length() > 2){
         File f = new File(getDir() + "warehouse.txt");
         if (f.exists())f.delete();
           try {
     		PrintWriter os = new PrintWriter(new FileWriter(
     				getDir() + "warehouse.txt", true));
     		os.write(sb.toString());
     		os.close();
     	  }catch (IOException ex) {
     		ex.printStackTrace();
     	  }
      }

         st.close();
         con.close();


    } catch (SQLException e){
      getParent().showError(e);
    }
    return customers;
  }

  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection("jdbc:oracle:thin:@10.10.10.55:1521:MVXEE001", getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

  public TreeMap getProdGroups() throws SQLException {
    TreeMap   tm = new TreeMap();
    return tm;
}

    public TreeMap getProdGroupsSecond() throws SQLException {
      TreeMap   tm = new TreeMap();
      return tm;
  }




  public TreeMap getProducts(Agent agent){


      StringBuffer buf2 = new StringBuffer();  	
  	Hashtable stocks = new Hashtable();
  	
  	//Leiame Laoseisud
  	 try {


        String  sql = " SELECT  MBITNO AS toode,MBSTQT-MBREQT AS STOCK  from mvxjdta.MITBAL l WHERE MBWHLO='100' ORDER BY MBITNO" ;

          if (isDebug()) System.out.println(sql);
             Connection con1 = getNewConnection();
             Statement stm = con1.createStatement();
             ResultSet rst = stm.executeQuery(sql);

               while (rst.next()) {
                 String toode = rst.getString("toode").trim();
                 int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                // String ladu  =  rst.getString("ladu").trim();
                 stocks.put(toode,seis+"");
                 //System.out.println(ladu+" "+toode+" "+seis+"");
             
              
                 
                // buf2.append(toode + ";" + seis + ";" + location + ";" + partii + ";" + ladu + "\n");
               
               
               
               }
             rst.close();
             stm.close();
             con1.close();

     } catch (Exception e) {
         getParent().showError(e);
     }

     Hashtable dateOut = new Hashtable();

  	
    String cmd =  "SELECT t.MMITNO AS kood,  t.MMITDS AS nimetus,t.MMCFI2 AS tykke , t.MMSPE4  AS onpallet ,(case when a.MPALWT=2 then a.MPPOPN else '' end)" +
    		"  AS ean ,t.mmspe3 as unit  ,MMITTY " +
          " FROM mvxjdta.MITMAS t , mvxjdta.MITPOP a  WHERE t.MMITTY  in  ('01','06', '09') " +
         " AND  t.MMITNO=a.MPITNO   AND MPALWT=2   AND t.MMBUAR='111'  AND MMSTAT < '21' AND(t.MMCFI2 not in (1,4) OR t.MMITTY<>'01')  " +
        " ORDER BY mpitno,(case when a.MPALWT=2 then a.MPPOPN else '' end)desc ";


    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {
      Connection con = getNewConnection();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(cmd);
      StringBuffer buf = new StringBuffer();

      while(rs.next()){

      	String kood = rs.getString("kood").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimetus").trim();
        prod.setName(nimetus);

        prod.setInCases( (rs.getDouble("tykke")));
       

     

         String pal = rs.getString("onpallet");
                 if (pal != null && pal.trim().length()>0) {
                 double pallet = Double.parseDouble(pal);
                 prod.setOnPallet(pallet);
                 }

       String ean = rs.getString("ean");
       if(ean != null && ean.trim().length()>7){
         prod.setEAN(ean.trim());
       }
        String unit =rs.getString("unit");

        if (unit != null && unit.trim().length()>0){
        prod.setUnit(unit.trim());
        }
        
        if(stocks.get(kood) != null){
        	prod.setStock(Double.parseDouble((String)stocks.get(kood)));
        }
        products.put(nimetus, prod);
        

     
         
        
        if(prod.getEAN().trim().length()==0){
        	 dateOut.put(kood.trim(),kood.trim() + "|" + (nimetus.trim()) + "|" + prod.getStock());
        	 buf.append(kood.trim() + ";"+ kood.trim() + "|" + (nimetus.trim()) + "|" + prod.getStock() + "\n");
        }else{
        	 dateOut.put(prod.getEAN(),kood.trim() + "|" + (nimetus.trim()) + "|" + prod.getStock());
        	 buf.append(prod.getEAN() + ";"+ kood.trim() + "|" + (nimetus.trim()) + "|" + prod.getStock() + "\n");
        }
        
        
        
        
        //}
      }
      rs.close();
      st.close();
      con.close();

      
      
      
    	  
      String cmd3 = "SELECT  MLITNO as toode ,MLWHLO as ladu, MLWHSL as laokoht, MLBANO as partii, MLSTQT as " +
               " kogus FROM MVXJDTA.MITLOC where MLWHLO in ('100','200')  ORDER BY  MLITNO  ,MLWHLO";
      con = getNewConnection();
          st = con.createStatement();
           rs = st.executeQuery(cmd3);
        buf2 = new StringBuffer();

          while(rs.next()){

          	String id = rs.getString("toode").trim();
          	String location = rs.getString("laokoht").trim();          
          	 String partii = rs.getString("partii").trim();
          	 if(partii.equals("")){
          		 partii = " ";
          	 }
            String kogus = rs.getString("kogus").trim();
            
            buf2.append(id + ";" + kogus + ";" + location + ";" + partii + ";100\n");
          }
          rs.close();
          st.close();
          con.close();

      
      
/*SELECT MLWHSL as laokoht, MLITNO as toode, MLBANO as partii, MLSTQT as 
kogus , MLSTAS as partiistaatus
(mis see t�hendab ??status) FROM MITLOC


MLWHSL - konkreetses laos olev kindel laokoht.
MLITNO - toote ID
MLBANO - partii nr vabatekstina (MLPRDT on parim enne kuup�ev) 
MLSTQT - kogus, mis hetkel on laos olemas (sh igat t��pi reserveeringud)
 MLSTAS - normaalne staatus on "2", kui rida on staatuses "1" v�i "3", 
siis seda rida ei saa n�iteks m��a/tootmises/pakkimises kasutada ei saa
*/      
      

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
      
    } catch (Exception e){
      getParent().showError(e);
    }
    
    
    try {
		saveObject(dateOut,"tootedRev.has"); 
	  	 
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
    
    
   
    return products;
  }

  
	public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}

  
  
  public Vector getCustomerProductsList(String agentCode){
    return new Vector();

  }


  public void setBulkSales(Map agents){
  }



  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


  public DealerProduct getDealerProductsList() {
        return null;
      }



      public Vector getDebt(String agentCode) throws SQLException{
       return null;
    }

    public ManagerData getManagerData() throws SQLException{
       ManagerData md = new ManagerData();
       return md;
     }


    public double getFoodSales(String agentCode) throws SQLException{
      return 0;
    }

    public double getAlcoSales(String agentCode) throws SQLException{
      return 0;
    }
    public double getPromoSales(String agentCode, int nr) throws SQLException{
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
      return "MovexLadu";
    }

    public String getSubjectFilesTo() {
      return this.subjectFilesTo;
    }


    public String getDaySalesData(Agent agent) {
      return null;
    }



    public String getPilotDayData(Agent agent){
        return null;
    }






    public String getStartingDateStr(){
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH,
            -(Integer.parseInt(parent.getParentFrame().getProps().getProperty("months")) + 1));
    String month = (cal.get(Calendar.MONTH)+1) +"";
    String day = (cal.get(Calendar.DAY_OF_MONTH)) +"";
         if(month.length() == 1){month = "0" + month;}
         if(day.length() == 1){day = "0" + day;}
         return cal.get(Calendar.YEAR) + "" + month + day;
     }



        public void log(String s) {
            getParent().log(s);
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

    private String url = "jdbc:oracle:thin:@10.10.10.55:1521:MVXEE001";
  //private String url = "jdbc:odbc:MVXEE001";
  private Connection con;
  private Statement stmt;
  private String subjectFilesTo = null;
  private NumberFormat NF = NumberFormat.getInstance();
  private TreeMap  dealers = new TreeMap();
}
