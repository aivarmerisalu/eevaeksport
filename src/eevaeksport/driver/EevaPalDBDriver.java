/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
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
public class EevaPalDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public EevaPalDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	if("true".equals(parent.getProps().getProperty("laodriver"))){
		 this.tavaline = false;
	}
   
	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	}	
	
	user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  
    public String getSaveDebtInfo() {
       	return null;
       }
       

    public String getSaveStockInfo(){
     return null;
      }
    public Vector getFixedAssortmentData() {
    	return lines;
    }
    
    public Vector getFixedAssortmentData1() {
       TreeMap sorting = new TreeMap();
       this.getProducts(null);
       lines = new Vector();
     
        Vector vv = new Vector();
   	 this.connectionUp();
        try {
        	TreeMap sort = new TreeMap();
       	  String cmd2 = "  select tasudok AS grupp  ,ry.nimetus as toode from dok d inner join rid r on r.dokumnr=d.dokumnr  inner join toode t on t.toode=r.toode " +
                 " LEFT JOIN artryhm ry ON ry.kood=t.ryhm " +
			"	where d.doktyyp ='H' and r.doktyyp ='H'  and  len(trim(t.toode))>2 AND t.grupp='L'  order by   t.pesakood, t.nimetus ";
       	  
       	  
       	 Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd2);
	        
	        while(rs.next()){
	        	String clGroup = Converter.cleanLitLatEst(rs.getString("grupp").trim());
	        	String prodID = rs.getString("toode");
	        	if(prodID != null){
	        		prodID = prodID.trim();
	        	if(sort.get(clGroup) == null){
	        		vv = new Vector();
	        		vv.addElement(prodID);
	        		sort.put(clGroup, vv);	
	        	}else{
	        		vv = (Vector)sort.get(clGroup);
	        		vv.addElement(prodID);
	        		sort.put(clGroup, vv);	
	        	}
	        	
	        	
	        	}
	        	}
	        rs.close();
	        stmt.close();
	        
	       // System.out.println("sort size " + sort.size());
       	TreeMap tooted = new TreeMap();
       	 stmt = con.createStatement();
       	 String cmd = "  select t.toode ,ry.nimetus  as nimi from  toode t inner JOIN artryhm ry ON ry.kood=t.ryhm where t.aktiivne=.T. " +
       	 		" AND t.grupp='L' and ry.nimetus is not null order by   t.pesakood, t.nimetus ";   	  
        	System.out.println(cmd);
       	         rs = stmt.executeQuery(cmd);

       	        while(rs.next()){
       	        	
       	        	String tt =  rs.getString("toode").trim();
       	        	String gr =  rs.getString("nimi").trim();
       	        	if(tooted.get(gr) == null){
    	        		vv = new Vector();
    	        		vv.addElement(tt);
    	        		tooted.put(gr, vv);	
    	        	}else{
    	        		vv = (Vector)tooted.get(gr);
    	        		vv.addElement(tt);
    	        		tooted.put(gr, vv);	
    	        	}
    	        	
        		  }	
       	    // System.out.println("sort size " + sort.size());
       	     System.out.println("tooted size " + tooted.size());
      
       	  Iterator iter = sort.keySet().iterator();
       	     
       	  while (iter.hasNext()){
       		  String grupp = (String) iter.next();     		  
       		  Vector prods = (Vector)sort.get(grupp);
       		 
       		  if(prods != null){
       			  
       			Enumeration en = prods.elements();
       			while (en.hasMoreElements()){
       				String ean = (String)en.nextElement();
       		if(tooted.get(ean) != null){
       			Vector vvv = (Vector)tooted.get(ean);
       			Enumeration enn = vvv.elements();
       			while (enn.hasMoreElements()){
       			
       				
       		   String custID = " ";
               String clGroup = " ";
               String prodID = (String)enn.nextElement();
               String prodGroup = " ";
               MatrixLine mline = new MatrixLine();

               clGroup = Converter.cleanLitLatEst(grupp);
               
   

               if (custID != null && custID.trim().length() > 0) {
                 mline.setCustID(custID.trim());
               }
               if (clGroup != null
                   && clGroup.trim().length() > 0) {
                 mline.setCliGroup(clGroup.trim());
               }

               if (prodID != null && prodID.trim().length() > 0) {
                 mline.setProdID(prodID.trim());
               }
               if (mline.getProdID() == null && prodGroup != null
                   && prodGroup.trim().length() > 0) {
                 mline.setProdGroup(prodGroup);
               }

            //   System.out.println(clGroup + prodssort.get(prodID) + prodID);
               
               sorting.put(clGroup + prodssort.get(prodID),mline);

       			}
       		}
       	 }
     	}
      }
	        
      	      stmt.close();
                      } catch (Exception e) {
                          getParent().showError(e);
                      }
                      
                      
                    Iterator itera =   sorting.values().iterator();
                    while (itera.hasNext()){
                    	lines.add(itera.next());
                    	
                    }
                    System.out.println(" Hinna Maatrix tehtud " + lines.size());
                    
                      
        return lines;
  }

    

        	   public String getAssortmentStr(Agent agent){
        	    StringBuffer sb = new StringBuffer();
        	     String cmd = " select r.toode, d.klient AS type  from dok d inner join rid r on r.dokumnr=d.dokumnr " +
                      " Where d.doktyyp='H' and r.doktyyp='H' order by  d.klient ,r.toode ";
        	    try {
        	     	// Connection con = getNewConnection2();
        	     	System.out.println(cmd);
        		        Statement stmt = con.createStatement();
        		        ResultSet rs = stmt.executeQuery(cmd);
        		        while(rs.next()){
        	        String id = rs.getString("toode").trim();
        	        String type = rs.getString("type").trim();				
        					sb.append(" ;" + type + "; ;" + id + "\n");
        				}

        		        rs.close();
        		       // con.close();
        	       } catch (Exception e){
        	      e.printStackTrace();
        	    }
        	    System.out.println("Assortiment lõpp " + sb.length());
        	    return sb.toString();
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
	    System.exit(0);
	}
      }
    }

        public String getDistribution() {
          return null;
        }
        public TreeMap getProdGroupsSecond() throws SQLException {
            TreeMap   tm = new TreeMap();
            String    sql = "SELECT yksus AS liik ,rtrim(yksus) + ' ' + nimetus AS nimi  " +
            		"FROM yksus y WHERE klassif=5 order by nimetus";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
              String liik = rs.getString("liik").trim();
              String nimetus = rs.getString("nimi").trim();
              if (liik != ""){
              tm.put(liik,Converter.enCodeEst(nimetus));
              }
            }
            rs.close();
            st.close();


            return tm;
        }

    public TreeMap getProdGroups() throws SQLException {
        TreeMap   tm = new TreeMap();
        String    sql = "SELECT yksus AS liik ,rtrim(yksus) + ' ' + nimetus AS nimi  " +
        		"FROM yksus y WHERE klassif=4 order by nimetus";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String liik = rs.getString("liik").trim();
          String nimetus = rs.getString("nimi").trim();
          if (liik != ""){
          tm.put(liik,Converter.enCodeEst(nimetus));
          }
        }
        rs.close();
        st.close();


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
        
    //	getFixedAssortmentData1();
    	connectionUp();
    	Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
        
	String  cmd = "SELECT yksus,nimetus FROM yksus WHERE klassif=2";
	TreeMap agents = new TreeMap();

	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String nimi = rs.getString("nimetus").trim();
              agent.setName(nimi);
              agent.setLanguage("EST");
              agent.setCountry("et");
              agent.setPassWord("0000");
String email = " ";
              
              if(propse.get(agent.getID()) != null){
            	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
              }
              
              
              if(email.length() > 3){
            	  agent.setEmail(email);
              }
              if(email.trim().length() > 1){
            	  agent.setActive(true);
                  agent1.put(agent.getID(), agent);
          		  agents.put(nimi, agent);
              }
              
              
        
	    }
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
        
        Agent aa = new Agent("HOUSE");
        aa.setName("HOUSE");
        agents.put("HOUSE", aa);
        
        
 
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

    private TreeMap agent1 = new TreeMap();
    
    public TreeMap getCustomers(Agent agent) {
        connectionUp();
        agent1 = new TreeMap();
        
        try{
      	   Converter.getManagersReplacements(agent1, rcompany, user, pass); 
      		
      }catch(Exception e){
      	e.printStackTrace();
      }
        if(lines.size() == 0){
        	 getFixedAssortmentData1();
        }
      
        
        if( tavaline){
           	

            System.out.println("piltideg alustan ");
           	
        	String  cmd2 = "SELECT t.toode, t.pesakood ,ry.nimetus , t.seisund FROM toode t LEFT JOIN artryhm ry ON ry.kood=t.ryhm  WHERE  " +
                "  len(trim(t.toode))>2 AND t.grupp='L' AND trim(t.seisund) !='A' AND  t.pesakood !='' AND aktiivne=.T. AND kassakeeld=.F. " +
                " GROUP BY t.toode ORDER BY t.pesakood,t.nimetus " ;
        	
        	
        	String picts = parent.getProps().getProperty("pict.dir");
      	    String pictspda = parent.getProps().getProperty("pictpda.dir");
      	    String results = parent.getProps().getProperty("price.dir");
      	    String results2 = parent.getProps().getProperty("price.dir2");
      	    String results3 = parent.getProps().getProperty("price.dir3");
      	    String results4 = parent.getProps().getProperty("price.dir4");
      	    String results5 = parent.getProps().getProperty("price.dir5");
      	    String results6 = parent.getProps().getProperty("price.dir6");
      	  

        	try {
        	    ResultSet	      rs = stmt.executeQuery(cmd2);

             TreeMap valitud = new TreeMap();
             TreeMap eanid = new TreeMap();
             TreeMap seisundid = new TreeMap();
             StringBuffer buf = new StringBuffer();
             StringBuffer buf0 = new StringBuffer();
             int a = 0;
             int b = 0;
        	    while (rs.next()) {
        	    	a++;
                      String pesakood = (rs.getString("pesakood").trim());
                      String toode = rs.getString("toode").trim();
                      String ean = rs.getString("nimetus");
                     
                      if(ean != null){
                          eanid.put(toode, ean.trim());  
                      }
                      String seisund = rs.getString("seisund");
                      if(seisund !=null && seisund.trim().length() > 0 ){
                    	  seisundid.put(toode, seisund);
                      }else{
                    	  seisund = "";
                      }
                      valitud.put(toode,pesakood);
                    
                      if(!seisund.equals("P")){
                    	
                      File f = new File(picts + toode+".jpg");
                      String len = "0";
                      if(f.exists()){
                      len =	f.length() + "";
                      }
                      //System.out.println(toode+".jpg;"  + len);
                      if(len.equals("0")){
                      	//System.out.println(toode+".jpg;"  + len);
                      	buf0.append(toode+".jpg\n");
                      }else{
                      	buf.append(toode+".jpg;"  + len + "\n");
                	    	b++;
                      }
                      
                      }
                      }
        	    connectionDown();
        	  //  System.out.println("Valitud " + valitud.size());
                
        	    //oleasoleva kataloogi kustutamine
        	    
                    File f = new File("list.txt");
                    if (f.exists())f.delete();
                    
                      try {
                		PrintWriter os = new PrintWriter(new FileWriter(
                				  "list.txt", true));
                		os.write(buf.toString());
                		os.close();
                	
                	  }catch (Exception ex) {
                		ex.printStackTrace();
                	  }
                //	  System.out.println("buf 0 " + a  + " " +  b + " "+ buf0.toString());
                	  File f0 = new File("list0.txt");
                	
                	  	
                         if (f0.exists())f0.delete();
                      
                        try {
                  		PrintWriter os1 = new PrintWriter(new FileWriter(
                  				  "list0.txt", true));
                  		os1.write(buf0.toString());
                  		os1.close();
                  		//System.out.println( buf.toString());
                  	  }catch (Exception ex) {
                  		ex.printStackTrace();
                  	  }

        //Kataloogi sisude kustutamine
                  	  
               File res = new File (results);
                 String[] ff = res.list();
             	 for(int i = 0;ff.length>i;i++){
                 //	System.out.println("fff " + ff[i]);
                 	(new File ( results + ff[i])).delete(); 
              
                 }
             	 
               File res3 = new File (results2);
                   String[] ff3 = res3.list();
               	 for(int i = 0;ff3.length>i;i++){
               	 	File hh = new File ( results2 + ff3[i]);
               	 	if(hh.isFile()){

                        hh.delete(); 	
               	 	}
                
                   }         	 
            	 
           	   File res4 = new File (results3);
                 String[] ff4 = res4.list();
             	 for(int i = 0;ff4.length>i;i++){
             	 	File hh = new File ( results3 + ff4[i]);
             	 	if(hh.isFile()){
                      hh.delete(); 	
             	 	}
                 }
            	   File res5 = new File (results4);
                   String[] ff5 = res5.list();
               	 for(int i = 0;ff5.length>i;i++){
               	 	File hh = new File ( results4 + ff5[i]);
               	 	if(hh.isFile()){
                        hh.delete(); 	
               	 	}else if(hh.isDirectory()){
               	 	String[] ffd = hh.list();
                  	 for(int o = 0;ffd.length>o;o++){
                  		File hhs = new File (  hh.getAbsolutePath() + ffd[o]);
                   	 	if(hhs.isFile()){
                   	 	hhs.delete();  
                   	 	}
                            
                  	 }
                  	 hh.delete();
               	 	}
                   }
             	
               	 
                 File res7 = new File (results6);
                 String[] ff7 = res7.list();
             	 for(int i = 0;ff7.length>i;i++){
             	 	File hh = new File ( results6 + ff7[i]);
             	 	if(hh.isFile()){
                      hh.delete(); 	
             	 	}else if(hh.isDirectory()){
             	 	String[] ffd = hh.list();
                	 for(int o = 0;ffd.length>o;o++){
                		File hhs = new File (  hh.getAbsolutePath() + ffd[o]);
                 	 	if(hhs.isFile()){
                 	 	hhs.delete();  
                 	 	}
                          
                	 }
                	 hh.delete();
             	 	}
                 }
               	 
               	 
                 File res6 = new File (results5);
                 String[] ff6 = res6.list();
             	 for(int i = 0;ff6.length>i;i++){
             	 	File hh = new File ( results5 + ff6[i]);
             	 	if(hh.isFile()){
                      hh.delete(); 	
             	 	}else if(hh.isDirectory()){
             	 	String[] ffd = hh.list();
                	 for(int o = 0;ffd.length>o;o++){
                		File hhs = new File (  hh.getAbsolutePath() + ffd[o]);
                 	 	if(hhs.isFile()){
                 	 	hhs.delete();  
                 	 	}
                          
                	 }
                	 hh.delete();
             	 	}
                 }
           	
               	 
               	 
               	 
               	 
             	 File res2 = new File (pictspda);
                 String[] ff2 = res2.list();
             	 for(int i = 0;ff2.length>i;i++){
                 //	System.out.println("fff " + ff2[i]);
                 	(new File ( pictspda + ff2[i])).delete(); 
              
                 }
//             	 Lõppes kustutamine 
             	 
             	 
               //Failide kopeerimine  
                 String[] gg = new File(picts).list();
              	  //System.out.println("gg " + gg[0] + " " + gg.length);
                 
                  for(int i = 0;gg.length>i;i++){
                  	
                  	File in = new File ( picts + gg[i]); 
                  	if(in.isFile()){
                  	
                  	File inpda = new File ( pictspda + gg[i]); 
                  	String innimi = gg[i];
                  //	System.out.println("in nimi " + innimi);
                  	if(innimi.indexOf(".") != -1){
                  		innimi = innimi.substring(0,innimi.indexOf("."));	
                  	}
                    
                    String outnimi = (String)valitud.get(innimi);
                    String outkampaania = (String)seisundid.get(innimi);
                    String outnimiean = (String)eanid.get(innimi);
                    
                    if(outkampaania != null){
                    	File fdir = new File(results5+ "\\"   );
                    	if (!fdir.exists())fdir.mkdir();
                    	File out2 = new File(results5+ "\\"  +outnimi +".jpg");
                    	copyFile(in,out2);
                    }else{
                    
                    	if(outnimiean != null){
                        	File fdir = new File(results4+ "\\"  + outnimiean );
                        	if (!fdir.exists())fdir.mkdir();
                        	File out2 = new File(results4+ "\\"  + outnimiean +"\\" +innimi +".jpg");
                        	copyFile(in,out2);
                        }
                        
                        
                        if(outnimi != null){
                        	File out = new File(results+ outnimi + ".jpg");
                        	
                        	//System.out.println("out nimi " + outnimi);
                        	if(outnimi.startsWith("08")
                        			|| outnimi.startsWith("11") 
                        			|| outnimi.startsWith("31.06")  || outnimi.startsWith("31.11")                     			
                        			 || outnimi.startsWith("31.05") || outnimi.startsWith("31.12")
                        			 || outnimi.startsWith("31.12") || outnimi.startsWith("31.14")
                        			 || outnimi.startsWith("31.15") || outnimi.startsWith("31.17")
                        			 || outnimi.startsWith("31.16")|| outnimi.startsWith("31.18")
                        			 || outnimi.startsWith("31.20") || outnimi.startsWith("31.19")
        							|| outnimi.startsWith("21.05") || outnimi.startsWith("21.06")
        							 || outnimi.startsWith("31.07") || outnimi.startsWith("31.08")
        							 || outnimi.startsWith("31.09") || outnimi.startsWith("31.10")
        							 	 || outnimi.startsWith("73.01") || outnimi.startsWith("73.02")	//sisse 2010 dtes						 	 
        							 	 || outnimi.startsWith("73.03") || outnimi.startsWith("73.04") //sisse 2010 dtes
        							 	// || outnimi.startsWith("74.01") || outnimi.startsWith("74.02") //sisse 2010 dtes
        							 	 // || outnimi.startsWith("74.03") || outnimi.startsWith("74.04") //sisse 2010 dtes
        							 	  || outnimi.startsWith("21.07") || outnimi.startsWith("21.08") //sisse 2010 dtes
        							 	  || outnimi.startsWith("21.09") || outnimi.startsWith("21.10") //sisse 2010 dtes
        							 	  || outnimi.startsWith("73.05") || outnimi.startsWith("73.06") //sisse 2010 dtes
        							 	  || outnimi.startsWith("73.07") || outnimi.startsWith("73.08") //sisse 2010 dtes
        							 	  || outnimi.startsWith("73.09") || outnimi.startsWith("73.10") //sisse 2010 dtes
        							 	  || outnimi.startsWith("31.21") 
        							 	 
        							){
                        		copyFile(in,new File(results2 + outnimi + ".jpg"));
                        	} else if(outnimi.startsWith("07.01")
                        			|| outnimi.startsWith("07.02") || outnimi.startsWith("07.03")
                        			|| outnimi.startsWith("07.04") || outnimi.startsWith("07.05")
                        			|| outnimi.startsWith("07.06") || outnimi.startsWith("07.07")
                        			|| outnimi.startsWith("07.08") || outnimi.startsWith("07.09")
                        			|| outnimi.startsWith("07.10") || outnimi.startsWith("07.11")
                        			|| outnimi.startsWith("07.12") || outnimi.startsWith("07.13")
                        			|| outnimi.startsWith("07.14")
                        			|| outnimi.startsWith("07.15")
        							 ){
                        		copyFile(in,new File(results6 + outnimi + ".jpg"));
                        	}else{
                        		copyFile(in,out);
                        		copyFile(in,new File(results3 + innimi + ".jpg"));	
                        	}
                        	copyFile(in,inpda);
                        }
                    	
                    }
                    
                    	
                  	}
                  }
                  File fff =  new File (pictspda +  "list.txt");
            	  copyFile(f,fff);  	
            	  File fff0 =  new File (pictspda +  "list0.txt");
            	  copyFile(f0,fff0);  	
            
                 
            
        	} catch (Exception e) {
        	    getParent().showError(e);
        	}
        } 

        System.out.println("piltideg läbi ");
        
        HashMap myygid = new HashMap(); 
        
        try{
       String cmd3 =   "SELECT dok.saaja AS saaja ,SUM(iif( YEAR(dok.kuupaev)=YEAR(date()) AND MONTH(dok.kuupaev)=MONTH(date()),rid.hind*rid.kogus,0)) AS cursales , " +
        	"SUM(iif( YEAR(dok.kuupaev)=YEAR(date()-(DAY(date())+1)) AND MONTH(dok.kuupaev)=MONTH(date()-(DAY(date())+1)),rid.hind*rid.kogus,0)) AS lastsales , " +
        	"SUM(iif( YEAR(dok.kuupaev)=YEAR(date()-365) AND MONTH(dok.kuupaev)=MONTH(date()),rid.hind*rid.kogus,0)) AS yearsales  " +
        	" , SUM(iif( YEAR(dok.kuupaev)=YEAR(date()) ,rid.hind*rid.kogus,0)) AS curYsales, " +
		"	  SUM(iif( YEAR(dok.kuupaev)= YEAR(date())-1 AND MONTH(dok.kuupaev)<=MONTH(date()) "
		+ " and  DAY(dok.kuupaev)<= DAY(date()),rid.hind*rid.kogus,0)) AS lastYsales  " +
		"	  FROM dok INNER JOIN rid ON " +
          "        dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode and rid.doktyyp=dok.doktyyp WHERE " +
          "         len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 2   AND dok.kuupaev > date()-750           " +
          "       AND dok.doktyyp = 'G' AND rid.doktyyp ='G' ORDER BY dok.saaja GROUP BY dok.saaja";
        	          	
        	          	            log(cmd3);
        	          	          connectionUp();
        	              stmt = con.createStatement();

        	        

        	        ResultSet      	      rs3 = stmt.executeQuery(cmd3);
        	              while(rs3.next()) {
        	                String saaja = rs3.getString("saaja");
        	                int curm = (int)rs3.getDouble("cursales");
        	                int lastm = (int)rs3.getDouble("lastsales");
        	                int yearm = (int)rs3.getDouble("yearsales");
        	                int yearC = (int)rs3.getDouble("curYsales");
        	                int yearL = (int)rs3.getDouble("lastYsales");
        	                
        	              String asi = null;
        	              if(curm != 0 ){
        	            	  asi = "Jooksev kuu : " + curm + "|"; 
        	              }
        	              
        	              if(lastm != 0 ){
        	            	 if(asi == null){
        	            	   asi = "Eelmine kuu : " + lastm + "|"; 
        	                 }else{
        	            	   asi = asi + "Eelmine kuu : " + lastm + "|"; 
        	                }
        	              } 
        	            	   if(yearm != 0 ){
              	            	 if(asi == null){
              	            	   asi = "Eelmine Aasta sama : " + yearm + "|"; 
              	                 }else{
              	            	   asi = asi + "Eelmine Aasta sama : " + yearm + "|"; 
              	                }}
        	            	   

        	            	   if(yearC != 0 ){
              	            	 if(asi == null){
              	            	   asi = "See Aasta kokku : " + yearC + "|"; 
              	                 }else{
              	            	   asi = asi + "See Aasta kokku : " + yearC + "|"; 
              	                }}
        	            	   if(yearL != 0 ){
                	            	 if(asi == null){
                	            	   asi = "Eelmine Aasta kokku : " + yearL + "|"; 
                	                 }else{
                	            	   asi = asi + "Eelmine Aasta kokku : " + yearL + "|"; 
                	                }}
          	            	   
        	            	   
        	                if(asi != null){
        	                	myygid.put(saaja,asi);      	
        	                }
        	              }
        	              rs3.close();
        	              stmt.close();
        
        
    	} catch (Exception e) {
    	    getParent().showError(e);
    	}
     
        System.out.println("eelmineaasta myyk läbi ");
        
        String cmd =
	    " SELECT k.kood, k.nimi, tanav,'' as linn , piirkond as regioon , postiindek as indeks ,trim(tanav)+ ' '+piirkond AS aadress,trim(kontaktisi) +' ' +telefon AS kontakt, " +
	    "k.maksetin AS makse,hinnaale as ale, SUM(iif(date()>d.tasukuup,d.tasumata,0)) AS tasumata,0 AS limit," +
	    " 0 as contrend, '' AS visit,k.maksja,k.objekt2 AS agent,k.objekt5 AS agent2,'' AS tagemail,riik as grupp,wwwpage as grupp2," +
	    " infomail as email FROM klient " +
	    " k LEFT JOIN dok d ON k.kood=d.saaja and  d.doktyyp='G' " +
		" WHERE  (k.objekt2 !='' or k.kood ='PALETTIEESTI') and k.kood != ''  GROUP BY k.kood, k.nimi order by k.kood ";

        connectionUp();

	log(cmd);

	TreeMap customers = new TreeMap();
	custCounter++;
	try {
		 stmt = con.createStatement();
		ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
            String kood = rs.getString("kood").trim();
		Customer cust = new Customer(kood);
		String nimi = rs.getString("nimi").trim();
                cust.setName(Converter.enCodeEst(nimi));
                

                String tanav = rs.getString("tanav");
                if (tanav != null && tanav.length() > 0){
                  cust.restStreet = tanav.trim();
                }
                cust.setCountry("Eesti");
                String regioon = rs.getString("regioon");
                if (regioon != null && regioon.length() > 0){
                	cust.restCity = ( regioon.trim()).trim();
                }

                String indeks = rs.getString("indeks");
                if (indeks != null && indeks.length() > 0){
                  cust.restZipCode = indeks.trim();
                }

                
                cust.setDebt(rs.getDouble("tasumata"));
                cust.setVisit(rs.getString("visit"));
                	cust.setAddress((Converter.adressLink(Converter.enCodeEst(parent.make(rs.getString("aadress"))),"Estonia")));
                	cust.setPersonalData(Converter.enCodeEst(parent.make(rs.getString("kontakt"))));
                	cust.setPaymTerm(parent.make(rs.getString("makse")));
                    cust.setDiscount(rs.getDouble("ale"));
                if(myygid.get(kood) != null){
                	cust.setInfo((String)myygid.get(kood));
                }
                
                
                String limit = rs.getString("limit");
                if(limit.trim().length()>0){
                 cust.setCredit(limit.trim());	
                }
             
                String over = rs.getString("contrend");
                if(over.trim().length()>0){
                 cust.setCredit(over);	
                }
                
                String payer = rs.getString("maksja");
                if(payer != null && payer.trim().length()>1){
                  cust.setPayer(payer.trim());
                  cust.setGroup(payer.trim());
                } else {cust.setPayer(kood);}
                
                String ag = rs.getString("agent");
                if (ag != null && ag.trim().length()>0){
                	cust.addAgent(new Agent(ag.trim()));
                	cust.addAgent(new Agent("MERLE"));
                	cust.addAgent(new Agent("HOUSE"));
                	cust.addAgent(new Agent("JORGER"));
                	if(ag.trim().equals("HEDI") || ag.trim().equals("KATRIN") || ag.trim().equals("JAANUS")){
                		cust.addAgent(new Agent("SVEN"));
                	}
                	
                	if(ag.trim().equals("KATLIN") || ag.trim().equals("MERJE") 
                			|| ag.trim().equals("HELI") || ag.trim().equals("HEDI")|| ag.trim().equals("KRISTI")
                			|| ag.trim().equals("MONIKA") || ag.trim().equals("KERSTI")){
                		cust.addAgent(new Agent("KATRIN"));
                	}
                	
                	if(ag.trim().equals("AIVAR") ||ag.trim().equals("MERILI") || ag.trim().equals("PIRET") || ag.trim().equals("VALLO")
                			|| ag.trim().equals("DIANA") || ag.trim().equals("IVIL") || 
                			ag.trim().equals("AIRI") || ag.trim().equals("SANNA") || ag.trim().equals("EVA")
                			){
                		cust.addAgent(new Agent("MERIKER"));
                	}
               
                }
                

                if (ag != null && agent1.get(ag.trim()) != null){
       				Agent aa = (Agent)agent1.get(ag.trim());
       			
       				Vector vec = aa.getDubbler();
       				if(vec!= null && vec.size() >0){
       					Enumeration en = vec.elements();
       					while(en.hasMoreElements()){
       						String age = (String) en.nextElement();
       						cust.addAgent(new Agent(age));
       					}
       				}
       			}
                
                if(cust.getID().equals("PALETTIEESTI")){
                	Enumeration enums = getParent().getAgentsHash().elements();
                    while (enums.hasMoreElements()) {
                      Agent a = (Agent) enums.nextElement();
                      cust.addAgent(a);
                    }
                }

                
                String ag2 = rs.getString("agent2");
                if (ag2 != null && ag2.trim().length()>0){
                	cust.addAgent(new Agent(ag2.trim()));          	
                	cust.addAgent(new Agent("MERLE"));
                	cust.addAgent(new Agent("HOUSE"));
                	cust.addAgent(new Agent("JORGER"));
                }
                String tagemail = rs.getString("tagemail");
                if (tagemail != null && tagemail.trim().length()>0){
                	cust.setReturnEmail(tagemail);          	
               
                }
                String grupp = rs.getString("grupp");
                String grupp2 = rs.getString("grupp2");
                
                if (grupp != null && grupp.trim().length()>0){
                	Vector vv = new Vector();
                	vv.add(Converter.cleanLitLatEst(grupp.trim()));
                	if(grupp2 != null && grupp2.trim().length()>0){
                		vv.add(Converter.cleanLitLatEst((grupp2.trim())));
                	}
                	cust.setFixSortCode(vv);          	
               
                }
                
                String email = rs.getString("email");
                if (email != null && email.trim().length()>0){
                	cust.setConfirmEmail(email.trim());          	
               
                	
                }
                
                customers.put(nimi, cust);
	    }
	} catch (Exception e) {
	//	this.getCustomers(null);
	//	if(custCounter == 2)
	 // System.exit(0);
		e.printStackTrace();
		getParent().showError(e);
	}

	
	   System.out.println("klientidega läbi ");
	     
	
	return customers;
    }

   private int  custCounter = 0;
    
   
   
	private HashSet refreshAllowed(File allowFile) {
		
		HashSet usedPr = new HashSet();
		try {
			
			BufferedReader in = new BufferedReader(new FileReader(allowFile));
			String line;
			HashSet usedCu = new HashSet();// mis klientidele on lubatud tooted
											// olemas
			
			while ((line = in.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, ":");
				while (st.hasMoreTokens()) {
					String prod = st.nextToken();
					usedPr.add(prod);
					}
				}
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return usedPr;
	}

   
   
    public TreeMap getProducts(Agent agent) {
        connectionUp();

        
        HashSet allowed = refreshAllowed(new File("allowed.txt"));
        TreeMap<String,Double> laos = new TreeMap<String,Double>();
        
        // Merle korraldus 7.09.2011 lülitada välja         
        String sql = "select toode,kogus as kog  from seisud   where osak='TALLINN'" ;
        
        String sql2 = "select r.toode, sum(r.kogus) as kog from dok d inner join rid r on r.dokumnr=d.dokumnr  where " +
		"  d.kuupaev > date()-30 and d.doktyyp='T' and taidetud =0 and tekst1 not like  ('%Arve%')" +
		" and tekst1 not like '%Sise%' and tekst1 not like '%TEHT%' and len(r.toode)>2 " +
		"  group by r.toode " ;
try{
PreparedStatement pstm = con.prepareStatement(sql);
ResultSet rs6 = pstm.executeQuery();

while(rs6.next()){
  laos.put(rs6.getString("toode").trim(),rs6.getDouble("kog"));
}

pstm = con.prepareStatement(sql2);
 rs6 = pstm.executeQuery();

while(rs6.next()){
	String key = rs6.getString("toode").trim();
	if(laos.get(key) != null){
		 laos.put(key,laos.get(key) - rs6.getDouble("kog"));
	}
 
}

}catch (Exception e){
}
System.out.println("laos " + laos.size());

	TreeMap products = new TreeMap();

	try {
   String add = "";
   if(!tavaline){
   	add = "";
   }
   
   String add2 = "t.pesakood + ' ' + t.nimetus";
   if(!tavaline){
   	add2 = "rtrim(t.toode) + ' ' + t.nimetus";
   }

          
           String cmd = "SELECT t.toode,t.pesakood , " +  add2 + " AS nimetus, asukoht AS lisa, t.tykke, mt.hind ,t.norm,ry.nimetus AS ean, " +
           	 "   t.yhik,t.objekt5 AS liik2 ,t.objekt4 AS liik ,kampaania as kestvus , pesakood FROM toode t LEFT JOIN artryhm ry ON ry.kood=t.ryhm " +
			"	left join hinnamtr mt ON mt.ryhm = t.ryhm AND mt.hinnak = 'HMAAT'  and prioriteet=90  WHERE  " +
            "     len(trim(t.toode))>2 AND t.grupp='L' AND trim(t.seisund) !='A' " + add + "  AND aktiivne=.T.  AND kassakeeld=.F. " +
            "    GROUP BY t.toode ORDER BY t.pesakood,t.nimetus ";

			if (isDebug()) {
			    System.out.println(cmd);
			}

           ResultSet  rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String pesa = rs.getString("pesakood");
		boolean goes = false;
		
		if(pesa != null && pesa.trim().length() > 0) {
			goes = true;
		}
		
		if(allowed.contains(kood)) {
			goes = true;
		}
		
		String name = rs.getString("nimetus").trim();
		String name2 = name;
		String lisa = rs.getString("lisa");
		int tykke = rs.getInt("tykke");
		
		if(lisa != null && lisa.trim().length() > 0){
			name = Converter.enCodeEst((name + " " + lisa).trim());
		}

		
          Product prod = new Product(kood);
		     
          if(tykke > 0){
  			prod.setInCases(tykke);
  		  }
                      
                          if(laos.get(kood) != null){
                        	  prod.setStock(laos.get(kood));
                          }
                          
               // prod.setInCases(rs.getDouble("tykke"));
               java.sql.Date dd = rs.getDate("kestvus");
            
               prod.setPrice(rs.getDouble("hind"));
                prod.setEAN(parent.make(rs.getString("ean")));
                prod.setUnit(parent.make(rs.getString("yhik")));
                prod.setGroup(parent.make(rs.getString("liik")));
                prod.setGroupSecond(parent.make(rs.getString("liik2")));
                String pesakood = rs.getString("pesakood");
                name =  name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
                prod.setName(Converter.enCodeEst(name));
               
                if(dd.getTime() >((new java.util.Date()).getTime())){
                    //	System.out.println("Kampaania " + prod.getName());
                    	//prod.setStatusDiscount("1");         	
                    name = "%%" + name;
                    prod.setStatusDiscount("1");
                   // prod.setName(Converter.enCodeEst(name));
                	prod.setGroup("0217");
                    }
                prod.setNameSorting(name);
                if(goes) {
                	  products.put(name, prod);
      		        prodssort.put( prod.getID(),name2);
      	        }
                
		      }
            rs.close();

 	} catch (Exception e) {
	    getParent().showError(e);
	}


	return products;
    }

    private HashMap prodssort = new HashMap();
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
	    "SELECT dok.saaja,rid.toode,toode.pesakood,dok.kuupaev,rid.kogus,rid.hind FROM dok INNER "
	    + "JOIN rid ON dok.dokumnr = rid.dokumnr and rid.doktyyp = dok.doktyyp INNER JOIN toode on rid.toode = toode.toode  WHERE "
	    + " len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 2 "
	    + "  AND dok.kuupaev >= date()-60 AND   dok.doktyyp in ('G','T')  AND rid.doktyyp in ('G','T') and rid.kogus != -1 " +
	     " ORDER BY dok.saaja, toode.pesakood, toode.nimetus,dok.kuupaev GROUP BY dok.saaja, rid.toode";

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
        if(hind == -1){
        	hind = 0;
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
        String sql = "SELECT klient,tasudok AS number,tasukuup AS maksepaev,tasumata, kuupaev ,tasukuup-date() as yle FROM " +
                     "dok WHERE   doktyyp='G' AND tasumata<>0 " +
                     "ORDER BY klient";


        con = DriverManager.getConnection(url);
        Statement stmt = con.createStatement();
        ResultSet	      rs = stmt.executeQuery(sql);
  	    while(rs.next()){
          Debt debt = new Debt();
          debt.setCustID(rs.getString("klient").trim());
          debt.setDoknr(rs.getString("number").trim());
          debt.setDuedate(rs.getDate("maksepaev"));

          debt.setSum(rs.getDouble("tasumata"));
          debt.setDate(rs.getDate("kuupaev"));
          debt.setDue(rs.getString("yle"));
          list.addElement(debt);
        }
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
	double  salary = 0;
        String  sql = "SELECT (SUM(o.doksumma)/1.20*(y.tasu/100) + y.palk) AS palk FROM omdok o INNER " +
                      "JOIN klient k ON k.kood=o.klient INNER JOIN yksus y ON y.yksus=k.objekt2 WHERE " +
                      "YEAR(o.kuupaev)=YEAR(date()) AND MONTH(o.kuupaev)=MONTH(date()) AND " +
                      "k.objekt2=?";

	if (isDebug()) {
	    System.out.println(sql);
	}

      try {
        PreparedStatement st = con.prepareStatement(sql);
        st.setString(1, agentCode);
        ResultSet rs = st.executeQuery();
        while(rs.next()){
          salary = rs.getDouble("palk");
        }
        rs.close();
        st.close();

        sql = "SELECT SUM(rid.hind*rid.kogus*toode.tasu/100) AS palk FROM dok INNER JOIN rid ON " +
              "dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode WHERE " +
              "len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 3  AND dok.pais3obj = ? AND " +
              "YEAR(dok.kuupaev)=YEAR(date()) AND MONTH(dok.kuupaev)=MONTH(date()) AND toode.tasu>0 " +
              "AND dok.doktyyp = 'G' AND rid.doktyyp ='G' AND rid.toode not like 'SU%' GROUP BY dok.raha";
        st = con.prepareStatement(sql);
        st.setString(1, agentCode);
        rs = st.executeQuery();
        while(rs.next()){
          salary += rs.getDouble("palk");
        }
        rs.close();
        st.close();



        sql = "SELECT sum(rid.kogus*(rid.hind-rid.kaubasumma))AS sales,sum(rid.kogus*(rid.hind))AS sales,sum(rid.kogus*(rid.kaubasumma))AS" +
        		" sal,plaan, dok.pais3obj  AS haldur FROM dok " +
              "INNER JOIN rid ON dok.doktyyp+STR(dok.dokumnr,7)=rid.doktyyp+STR(rid.dokumnr,7)  INNER JOIN yksus y ON y.yksus = dok.pais3obj " + 
              " WHERE  len(trim(dok.klient)) > 1  AND dok.pais3obj=? AND YEAR(dok.kuupaev)=YEAR(date()) " +
              " AND MONTH(dok.kuupaev)=MONTH(date()) AND y.klassif=3 AND (dok.doktyyp = 'G' OR dok.doktyyp = 'Y')" +
              " AND (rid.doktyyp ='G' or rid.doktyyp ='Y') " +
			  "  AND dok.yksus='HULGI' AND len(trim(rid.toode))>2 AND rid.toode not like 'SU%' GROUP BY haldur " ;
        st = con.prepareStatement(sql);
        	st.setString(1, agentCode);
        rs = st.executeQuery();
        double sales = 0;
        double plan = 0;
        while(rs.next()){
           sales = rs.getDouble("sales");
           plan = rs.getDouble("plaan");
        }
        setSales(sales);
        setBudget(plan);
        rs.close();
        st.close();


      } catch (SQLException e){
        getParent().showError(e);
      }

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

    public static boolean isDay(java.sql.Date lll , int add){
    	Calendar cc = Calendar.getInstance();
    	if(add != 0){
    		cc.add(Calendar.DAY_OF_YEAR, add);
    	}
         int year = lll.getYear()+ 1900;
         int mmm = lll.getMonth();
         int ddd = lll.getDate();
         
       //  System.out.println(year) + " " + mmm + " "+ ddd  + "   " + cc.get(Calendar.YEAR) + " " + cc.get(Calendar.MONTH)
         //+ " " + cc.get(Calendar.DAY_OF_MONTH));
    	if(cc.get(Calendar.YEAR) == year && 
    			cc.get(Calendar.MONTH) == mmm &&
    			cc.get(Calendar.DAY_OF_MONTH) == ddd ){
    		return true;
    	}else{
    		return false ;	
    	}   	
    }
    
    public void setBulkSales(Map agents){

    	
    	//teeme päringu palju on arveks tegematta ja nimekiri mis pole on arve või väljastus tehtud eile
    	
    
          try {
        	  
        	  
        	  
        	  String cmdx =   "SELECT dok.pais2obj AS agent ,SUM(rid.hind*rid.kogus) AS sales FROM dok INNER JOIN rid ON " +
                      "dok.dokumnr = rid.dokumnr and dok.doktyyp = rid.doktyyp  WHERE " +
                     " len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 2   AND " +
                     " dok.doktyyp='T' and dok.kuupaev > date()-20 and doksumma = 0 GROUP BY dok.pais2obj " +
					 " having SUM(rid.hind*rid.kogus) > 0 ";
                  	
                  	            log(cmdx);
                  	          connectionUp();
                      Statement stmt = con.createStatement();

                      ResultSet	      rs = stmt.executeQuery(cmdx);
                      while(rs.next()) {
                        String agentID = rs.getString("agent").trim();
                        Agent agent = (Agent)agents.get(agentID);
                        if (agent != null){
                        	agent.setStartText("Tellimusi ootel " + (int)rs.getDouble("sales") + " €");
                        }
                      }
                      rs.close();
                      stmt.close();

                      TreeMap list = new TreeMap();
                      
                      String cmdr =   " SELECT dok.pais2obj AS agent , k.nimi AS sales , max(dok.kuupaev) "
                      		+ "as ddd FROM dok INNER JOIN klient k ON  dok.saaja = k.kood  WHERE " +
                             " len(trim(dok.klient)) > 1    AND  dok.pais2obj != '' and " +
                             " dok.doktyyp in ('V','G') and dok.kuupaev > date()-4 group by k.nimi ";
                          	
                          	            log(cmdx);
                          	          connectionUp();
                              stmt = con.createStatement();
                              rs = stmt.executeQuery(cmdr);
                              Calendar cal = Calendar.getInstance();
                              boolean monday = false;
                              if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY){
                            	  monday = true;
                              }
                             
                              while(rs.next()) {
                                String agentID = rs.getString("agent").trim();
                                String nimi = rs.getString("sales").trim();
                                java.sql.Date ddd = rs.getDate("ddd");
                                Agent agent = (Agent)agents.get(agentID);
                                if (agent != null){
                                	//System.out.println("Data " + ddd.toLocaleString() + " " + list);
                                	if(monday && isDay(ddd,-3)){
                                		if(list.get(agentID) != null){
                                		  nimi = nimi + "|" + list.get(agentID);
                                		}
                                		list.put(agentID,nimi);
                                		
                                	}else if(isDay(ddd,-1)){
                                		if(list.get(agentID) != null){
                                  		  nimi = nimi + "|" + list.get(agentID);
                                  		}
                                  		list.put(agentID,nimi);
                                  		
                                    }
                                	}
                                	
                              }
                              rs.close();
                              stmt.close();
                     if(list.size() > 0){
                    	Iterator itera =  list.keySet().iterator();
                    	while (itera.hasNext()){
                    		String aaa = itera.next()+"";
                    		 Agent agent = (Agent)agents.get(aaa);
                             if (agent != null){
                            	 if(agent.getStartText() != null ){
                            	 	 agent.setStartText(agent.getStartText() + "|" + list.get(aaa)+"");
                                     	 
                            	 }else{
                            	 	 agent.setStartText(list.get(aaa)+"");
                                      		 
                            	 }
                             }
                    	}
                     
                    	
                         	
                     }
                      
                      
        	  
              String cmd =   "SELECT dok.pais2obj AS agent ,SUM(rid.hind*rid.kogus) AS sales FROM dok INNER JOIN rid ON " +
              "dok.dokumnr = rid.dokumnr and dok.doktyyp = rid.doktyyp  WHERE " +
              "len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 2   AND " +
              "YEAR(dok.kuupaev)=YEAR(date()) AND MONTH(dok.kuupaev)=MONTH(date())  " +
              "AND dok.doktyyp = 'G' AND rid.doktyyp ='G'  GROUP BY dok.pais2obj ";
          	
          	            log(cmd);
          	          connectionUp();
               stmt = con.createStatement();
               rs = stmt.executeQuery(cmd);

              while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                  agent.setMonthSales((int)rs.getDouble("sales"));
                }
              }
              rs.close();
              stmt.close();
             
              
              String cmd2 =   " SELECT dok.pais2obj AS agent ,SUM(rid.hind*rid.kogus) AS sales FROM dok INNER JOIN rid ON " +
             " dok.dokumnr = rid.dokumnr and dok.doktyyp = rid.doktyyp  WHERE " +
			 " YEAR(dok.kuupaev)=YEAR(date()) AND MONTH(dok.kuupaev)=MONTH(date()) and " +
             " ((len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 2   AND " +
             " dok.doktyyp = 'V' AND rid.doktyyp ='V'  and dok.klient ='RATAKIRJAEST') or " + 
			 " dok.doktyyp = 'U' AND rid.doktyyp ='U')  GROUP BY dok.pais2obj ";
          	
          	            log(cmd2);
              Statement stmt2 = con.createStatement();

              ResultSet	      rs2 = stmt2.executeQuery(cmd2);
              while(rs2.next()) {
                String agentID = rs2.getString("agent").trim();
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                  agent.setMonthSales((int)rs2.getDouble("sales"));
                //  System.out.println(agent.toString() + " " + agent.getMonthSales());
                }
              }
              rs2.close();
              stmt2.close();
              
              
              
              
               cmd2 =   "  select objekt2 AS agent ,eelsumma AS sales " +
			            "  from laoinfo where kuu = ltrim(str(year(date()))) + iif(len(ltrim(str(month(date()))))=1  " +
				        "  ,'0'+ltrim(str(month(date()))),ltrim(str(month(date())))) ";
            	
            	            log(cmd2);
            	          connectionUp();
               stmt2 = con.createStatement();

                  rs2 = stmt2.executeQuery(cmd2);
                while(rs2.next()) {
                  String agentID = rs2.getString("agent").trim();
                  Agent agent = (Agent)agents.get(agentID);
                  if (agent != null){
                    agent.setMonthBudget((int)rs2.getDouble("sales"));
                    
                  }
                }
                rs2.close();
                stmt2.close();
         
          } catch (Exception e) {
              getParent().showError(e);
              e.printStackTrace();
          }

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
    public static final String DRIVER_VERSION = "EevaPal";
    private String	       url = "jdbc:odbc:eeva";
    private Connection	       con = null;
    private Statement	       stmt;
    private  Vector lines = new Vector();
    private String	       subjectFilesTo = null;
    private boolean tavaline = true;
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
	public Vector getPriceMatrixData() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

