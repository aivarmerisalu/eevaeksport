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
import java.sql.Timestamp;
import java.util.Calendar;
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

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class EevaPalWareDBDriver extends AbstractDBDriver implements BulkUpdater {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public EevaPalWareDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
	// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	if("true".equals(parent.getProps().getProperty("laodriver"))){
		 this.tavaline = false;
	}
   
	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	}
    }
    

    public String getSaveStockInfo(){
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
    		
   		 TreeMap agents = new TreeMap();
            Agent agent = new Agent("LADU");
            agent.setName("LADU");
            agents.put("LADU", agent);   
          StringBuffer sb = new StringBuffer();
            String sql = " SELECT yksus,nimetus,objekt7 as pin FROM yksus WHERE klassif=2  and objekt5!='' " ; //where dimension = 40
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

     /*   String cmdvana =
	    " select dok.tasudok as kood ,kl.nimi,count(toode) as lines ,y.objekt5 as agent from dok inner join klient kl on kl.kood=dok.saaja " +
		" inner join rid on rid.dokumnr=dok.dokumnr and dok.doktyyp=rid.doktyyp " +
		" inner join yksus y on dok.kalktoode=y.yksus and y.klassif=2 " +
		" where kalktoode !='' and taidetud=0 and dok.doktyyp='T' group by dok.tasudok ";*/
        String cmd =
    	    " select dok.tasudok as kood ,kl.nimi,count(toode) as lines  from dok inner join klient kl on kl.kood=dok.saaja " +
		" inner join rid on rid.dokumnr=dok.dokumnr and dok.doktyyp=rid.doktyyp 	  " +
		" where dok.doktyyp='T' and rid.doktyyp= 'T' and tekst1 not like '%Arve%' and tekst1 not like '%ljastus%' " +
		" and kuupaev > date()-30 and toode !='KM' group by dok.tasudok";


	log(cmd);

	TreeMap customers = new TreeMap();

	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
            String kood = rs.getString("kood").trim();
		Customer cust = new Customer(kood);
		String nimi = kood + " " +rs.getString("nimi").trim() + "<" + rs.getString("lines") + ">" ;
                
                   cust.setName(nimi);
                   cust.addAgent(new Agent("LADU"));             
                customers.put(nimi, cust);
	    }
	      
	    
	} catch (Exception e) {
	    getParent().showError(e);
	}
	
	Customer tagastus = new Customer("AAATAGASTUS");
	tagastus.setName("AAA Tagastus");
	tagastus.addAgent(new Agent("LADU"));
	customers.put("AAA Tagastus", tagastus);
	
	
	
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
 
   
        String sql = " select toode from rid r  inner join dok d on r.dokumnr=d.dokumnr  " +
      "  and r.doktyyp=d.doktyyp  where kuupaev > date()-480   group by toode" ;
try{
Statement pstm = con.createStatement();
ResultSet rs6 = pstm.executeQuery(sql);

while(rs6.next()){

  prods.put(rs6.getString("toode").trim(), "toode");

}
}catch (Exception e){
e.printStackTrace();
}
	

 sql = " select toode ,finnimetus as abi from toode  where  finnimetus != ''" ;
try{
Statement pstm = con.createStatement();
ResultSet rs6 = pstm.executeQuery(sql);

while(rs6.next()){
barcodes.put(rs6.getString("toode").trim(),rs6.getString("abi").trim());
}
}catch (Exception e){
e.printStackTrace();
}

		System.out.println("prods size " +  prods.size());
		
		
   
   String add2 = "' ' +rtrim(t.pesakood) +  iif(aktiivne = .T.,'A','') +   '  '  +  rtrim(t.toode) + ' ' + t.nimetus";

          
           String cmd = "SELECT t.toode, " +  add2 + " AS nimetus,aktiivne, asukoht AS lisa, t.tykke, mt.hind ,t.norm,ry.nimetus AS ean, " +
           	 "   t.yhik,t.objekt5 AS liik2 ,t.objekt4 AS liik ,kampaania as kestvus FROM toode t LEFT JOIN artryhm ry ON ry.kood=t.ryhm " +
			"	left join hinnamtr mt ON mt.ryhm = t.ryhm AND mt.hinnak = 'HMAAT'  and prioriteet=99  WHERE  " +
            "     len(trim(t.toode))>2 AND t.grupp='L' AND trim(t.seisund) !='A'  and pesakood != '' and pesakood not like '%.  .%'  " + //  AND aktiivne=.T.
            "    GROUP BY t.toode ORDER BY t.pesakood,t.nimetus ";

			if (isDebug()) {
			    System.out.println(cmd);
			}

           ResultSet  rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String name = rs.getString("nimetus").trim();
		String lisa = rs.getString("lisa");
	
		if(lisa != null && lisa.trim().length() > 0){
			name = name + " " + lisa.trim();
		}

		
          Product prod = new Product(kood);
		        name =  name.substring(0,1) + (name.substring(1,name.length())).toLowerCase().trim();
                prod.setName(name);            
                prod.setPrice(rs.getDouble("hind"));
                prod.setEAN(parent.make(rs.getString("ean")));
                prod.setUnit(parent.make(rs.getString("yhik")));
              
                if(barcodes.get(kood) != null){
    		        prod.addBarcode((String)barcodes.get(kood));	
                }
                
                if(prods.get(kood) != null){
                    if(prod.getEAN().trim().length()==0){
                    	 dateOut.put(kood.trim(),kood.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock());
                    	 buf.append(kood.trim() + ";"+ kood.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock() + "\n");
                    }else{
                    	 dateOut.put(prod.getEAN(),kood.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock());
                    	 buf.append(prod.getEAN() + ";"+ kood.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock() + "\n");
                    	 if(prod.getEanCase().trim().length() >0){
                    		 dateOut.put(prod.getEanCase(),kood.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock());
                        	 buf.append(prod.getEanCase() + ";"+ kood.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock() + "\n");	 
                    	 }
                    }
                	
    		        products.put(name, prod);	
                }
	          }
            rs.close();

            
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
    		
            
 	} catch (Exception e) {
	    getParent().showError(e);
	}


	return products;
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
					
	  			 try{
	  			  
	  			 	
	  			 	con = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	  			
	  			
	  			System.out.println(objekt + "'" + location+ "'" + toLocation + "'" + kogus + "'" +  prodid + "'" + 
	  					ean + "'" + prName+ "'" + wrk + "'" + wrkname+ "'" + entered.toString() +
	  					"'" +warehouse + "'" +toWarehouse + "-" + partii );
	  		 
	  			// Siin tuleb lisada kolm varianti inentuur ja rida ja p�is.
	  			 
	  			if(lineType.equals("INVENTORY")){
	  				//inventuuri rida

	  				
	  				
	  				int doknr = 0;
	  	    		//1 dokumendi  numbri m��ramine
	  	    		
	  	    		try{
	  	    			String sql1 = "select dok.dokumnr from dok " +
	  	              " where dok.doktyyp='F'  and dok.kinnitatud = 0  and dok.kuupaev > date() -5 "; 
	  	               //+ " and trim(dok.tekst1)= ''";
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
	  	    		
	  					 lineid = ean + "" + lin.getEnterTime().getTime();
	  					
	  						 if (doknr >0){ // siin lylitatakse sisse inventuuri andmete sisselugemine
	  						//	 if (false){ 
	  	    			String sqq = "select rtaitkogus from rid where dokumnr= " + doknr +" and toode ='" + prodid + "' and doktyyp ='F' ";
	  	    			 try{
	  	    			  
	  	    			 	
	  	    			 	con = DriverManager.getConnection(url);
	  	    			boolean lisame = false;
	  	    			Statement stq = con.createStatement();
	  	    			ResultSet rsq = stq.executeQuery(sqq);
	  	    			double kogusq = 0;
	  	    			if(rsq.next()){
	  	    				kogusq = rsq.getDouble("rtaitkogus");
	  	    			}else{
	  	    				System.out.println("toodet pole dokumendis " + prodid);
	  	    				lisame = true;
	  	    			}
	  	    			
	  	    			
	  	    			
	  	    			 String sql = " UPDATE RID SET  rtaitkogus= ?  WHERE dokumnr =" + doknr + " and doktyyp ='F' and toode ='" + prodid + "'";
	  	    		
	  	    		
	  	    				  con = DriverManager.getConnection(url);
	  	     		        PreparedStatement stpr = con.prepareStatement(sql);
	  	     		        System.out.println("sisestatav Kogus :" + kogus + " - " + kogusq);
	  	     		        stpr.setDouble(1,kogus + kogusq);
	  	     		      // stpr.setString(2, location);
	  	     		      try{
	  	     		        stpr.executeUpdate();  
	  	     		       System.out.println(sql  + "Rida updatetud");
	  	      		      
	  	    			 }catch (Exception e){
	  	    			 	this.log(e.toString());
	  	    			 	System.out.println(sql  + "Rida EI T�IDETUD");
	  	       		      
	  	    			 }
	  	    			 
	  	    			 
	  	    			 if(lisame){
	  	    				 
	  	    				 String sql3 = " INSERT INTO rid VALUES('F'," + doknr + ",'" + prodid + "', 0,?, '" + prodid + "','','','',0,'pdl','','1','',date()," +
	  	    				 "0,0,0,0,'','','','','','',''," + kogus + ",0,'','','',0,0,.F.,.F.,0,'',.F.,0,.F.,'',0,'',0)";
	  	    				 //"INSERT INTO rid VALUES('T',?, ?, ?, ?, ?,'','','',0,'','','','',date()," +
	  	                     //"0,0,0,0,'','','','','','','',0,0,'','',            '',0,0,.F.,.F.,0,'',.F.,0,.F.,'',0,'',0)";
	  	    				 
	  	    				 PreparedStatement st = con.prepareStatement(sql3);
	  	        System.out.println(sql3 );
	  	       
	  	          st.setDouble(1, kogus);
	  	          st.executeUpdate();
	  	          System.out.println("lisame rea juurde  " + prodid );
	  	              st.close();
	  	    			 }
	  	    			 
	  	     		      	
	  	     		        stpr.close();
	  	     		     //   con.close();
	  	     		        
	  	    			 }catch (Exception e){
	  	    			 	e.printStackTrace();
	  	    			 }
	  	    				
	  						}
	  	    			 tehtud.add(lineid);
	  	    	}
	  				
	  				
	  				
	  	 String sql3 = " insert into INVENTORY (artikkel,kogus,ladu,ts, " +
	         " kasutaja,partii ,asukoht ) values( " +
	         "	'" + prodid + "',?,'" + warehouse.trim() + "',?,'"+ wrkname +"','" + partii+ "'" + location + "' )"  ;
	          PreparedStatement st = con.prepareStatement(sql3);
	          System.out.println(sql3 );
	     
	           st.setDouble(1, kogus);
	           st.setTimestamp(2, new Timestamp(entered.getTime()));
	           st.executeUpdate();
	              System.out.println("lisame rea juurde  " + prodid );
	            st.close();
	  			}
	  			
	  			
	  			
	  			if(lineType.equals("MOVE")){
	  				//laoliikumine rida
	  			}
	  			 System.out.println("Line type  " +  lineType);
	  			
	  			if(lineType.equals("HEADERDONE")){
	  				//P�is tehtud rida
	  				
	  				
	  				 String sql3 = " UPDATE dok  set kalktoode ='" + wrk + "' where tasudok='" + objekt + "' and dok.doktyyp='T'" ;
			          PreparedStatement st = con.prepareStatement(sql3);
			          System.out.println(sql3 );
			            st.executeUpdate();
			              System.out.println("lisame HEADERDONE rea juurde  "  );
			            st.close();
	  			}
	  			

	  			if(lineType.equals("LINECHANGE")){
	  			    this.changeOrderContent(lin);
	  			}
	  	 
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
    	
    	boolean ok = false;
		if (ok) {
    	String sq = "	select toode,dok.dokumnr,kogus  from dok inner join rid r on r.dokumnr=dok.dokumnr " + 
          "  where dok.doktyyp='F' and dok.dokumnr=3  and kogus =0  " ;
    	try{
    		con = DriverManager.getConnection(url);
			 System.out.println(sq);
   		Statement st = con.createStatement();
   		ResultSet rs = st.executeQuery(sq);
   		
   		while (rs.next()){
   			String sql2 = "delete from rid where doktyyp='F' and dokumnr = 3 and toode='" + rs.getString("toode").trim() + "'";	
   			System.out.println(sql2);
   		   Connection ccc = DriverManager.getConnection(url);
   			Statement stt = ccc.createStatement();
   		  // stt.executeUpdate(sql2);
   		   stt.close();
   		   ccc.close();
   		}
   		
   		
   		rs.close();
   		st.close();
   		con.close();
   		
    	}catch (Exception s){
    		s.printStackTrace();
    	}
    	System.exit(0);
    	}
    	
   
    	
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
        connectionUp();
	
	String cmd =
	    " SELECT dok.tasudok as saaja ,rid.toode,toode.pesakood,dok.kuupaev,rid.kogus,rid.hind FROM dok INNER " +
	    " JOIN rid ON dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode WHERE " +
	    " len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 2  " +
	    "   AND  dok.doktyyp = 'T'  AND rid.doktyyp= 'T'  and tekst1 not like '%Arve%' and tekst1 not like '%ljastus%' " +
	    "   and kuupaev > date()-30 ORDER BY dok.tasudok, toode.pesakood,dok.kuupaev";

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
    public static final String DRIVER_VERSION = "EevaPalWare";
    private String	       url = "jdbc:odbc:eeva";
    private Connection	       con = null;
    private Statement	       stmt;
    private String	       subjectFilesTo = null;
    private boolean tavaline = true;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

