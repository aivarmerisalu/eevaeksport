/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
public class EevaPalLatDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public EevaPalLatDBDriver(EksportPanel parent) {
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
    	      
             StringBuffer products = new StringBuffer();
             seisud = new TreeMap();
             
          String sql = "select s.toode , s.kogus  as asi from  " +
                 " firma1.hetkesei s  where s.osak ='RIGA'";
          if (isDebug()) {
              System.out.println(sql);
          }

          try {
       	   connectionUp();
           ResultSet rs = con.createStatement().executeQuery(sql);
           while (rs.next()) {
String toode = rs.getString("toode").trim();
String asi = rs.getString("asi").trim();
          seisud.put(toode, asi);
                     products.append(toode + "; ; ;");
                     products.append(asi);               
                     products.append("\n");
           }
           rs.close();
           con.close();
          this.connectionDown();
          
             } catch (Exception e) {
                 getParent().showError(e);
             }
             return products.toString();
         }

      


        	   public String getAssortmentStr(Agent agent){
        	    StringBuffer sb = new StringBuffer();
        	     String cmd = "select r.toode, d.klient AS type  from firma1.dok d inner join firma1.rid r on r.dokumnr=d.dokumnr " +
                     " Where d.doktyyp='H'";
        	    try {
        	    	connectionUp();
        	     	System.out.println(cmd);
        		        Statement stmt = con.createStatement();
        		        ResultSet rs = stmt.executeQuery(cmd);
        		        while(rs.next()){
        	        String id = rs.getString("toode").trim();
        	        String type = rs.getString("type").trim();				
        					sb.append(" ;" + type + "; ;" + id + "\n");
        				}

        		        rs.close();
        		        stmt.close();
        		       // con.close();
        	       } catch (Exception e){
        	      e.printStackTrace();
        	    }
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
	}
      }
    }

        public String getDistribution() {
          return null;
        }
        public TreeMap getProdGroupsSecond() throws SQLException {
            TreeMap   tm = new TreeMap();
            String    sql = "SELECT yksus AS liik ,rtrim(yksus)|| ' ' || lvlnimetus AS nimi  " + 
            		" FROM firma1.yksus5 y order by lvlnimetus";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
              String liik=rs.getString("liik");
              String nimetus=rs.getString("nimi");
              if(nimetus == null){
            	  nimetus = "xxx";
              }
              if (liik != null && liik.trim() != "" ){
               tm.put(liik.trim(),Converter.enCodeEst(nimetus));
              }
            }
            rs.close();
            st.close();


            return tm;
        }

    public TreeMap getProdGroups() throws SQLException {
        TreeMap   tm = new TreeMap();
        String    sql = "SELECT yksus AS liik ,rtrim(yksus) || ' ' || rtrim(lvlnimetus) AS nimi  " +
        	"	FROM firma1.yksus4 y order by lvlnimetus";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            String liik=rs.getString("liik");
            String nimetus=rs.getString("nimi");
            if(nimetus == null){
          	  nimetus = "xxx";
            }
            if (liik != null && liik.trim() != "" ){
            tm.put(liik.trim(),Converter.enCodeEst(nimetus));
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
        connectionUp();
	String  cmd = " SELECT yksus,nimetus FROM firma1.yksus2 ";
	TreeMap agents = new TreeMap();
	Properties propse =  new Properties();
    try{
    	propse =  EksportFrame.loadProps("emails.txt");
  	  
    }catch(Exception e){
    	e.printStackTrace();
    }
	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String nimi = rs.getString("nimetus").trim();
              agent.setName(nimi);
              agent.setLanguage("LAT");
              agent.setCountry("lv");
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
              }
              
		    agents.put(nimi, agent);
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

    
    public TreeMap getCustomers(Agent agent) {
    	
    	 getFixedAssortmentData1();
    	if(!"false".equals(parent.getProps().getProperty("makepict"))){
        if( tavaline){
           	
           	
            connectionUp();
        	String  cmd2 = "SELECT t.toode, (t.pesakood) ,(ry.nimetus),pi.pilt  FROM firma1.toode t LEFT JOIN firma1.artryhm ry ON ry.kood=t.ryhm " + 
      " LEFT JOIN firma1.artpilt pi ON pi.toode=t.toode WHERE  " +
          "          length(trim(t.toode))>2 AND t.grupp='L' AND t.seisund is null  AND  t.pesakood is not null AND aktiivne=true  " +
            "      GROUP BY t.toode,t.pesakood ,t.nimetus,ry.nimetus,pi.pilt ORDER BY t.pesakood,t.nimetus " ;
        	
        	
        	String picts = parent.getProps().getProperty("pict.dir");
      	    String pictspda = parent.getProps().getProperty("pictpda.dir");
      	    String results = parent.getProps().getProperty("price.dir");
      	    String results2 = parent.getProps().getProperty("price.dir2");
      	    String results3 = parent.getProps().getProperty("price.dir3");
      	   String results4 = parent.getProps().getProperty("price.dir4");
      	  

        	try {
        	    ResultSet	      rs = stmt.executeQuery(cmd2);

             TreeMap valitud = new TreeMap();
             TreeMap eanid = new TreeMap();
             StringBuffer buf = new StringBuffer();
             StringBuffer buf0 = new StringBuffer();
             int a = 0;
             int b = 0;
        	    while (rs.next()) {
        	    	a++;
                      String pesakood = (rs.getString("pesakood").trim());
                      String toode = rs.getString("toode").trim();
                      String ean = rs.getString("nimetus");
                      byte[] bufbyte = rs.getBytes("pilt");
                      
                      if(bufbyte != null){
                          String filename = picts + toode+".jpg"; // Function for generating temporary filenames basing on GUID 
                          File fp = new File(filename);
                          if(fp.exists()){
                        	  fp.delete();
                          }
                         // System.out.println(filename);
                          if(fp.createNewFile()) {
                        	  FileOutputStream fos = new FileOutputStream(fp); 
                              fos.write(bufbyte);
                              fos.close();     
                          }              	  
                      }


                      
                      if(ean != null){
                          eanid.put(toode, ean.trim());  
                      }
                      
                      valitud.put(toode,pesakood);
                      File f = new File(picts + toode+".jpg");
                      String len = "0";
                      if(f.exists()){
                      len =	f.length() + "";
                      }
                      //System.out.println( picts + toode+".jpg;"  + len);
                      if(len.equals("0")){
                      	//System.out.println(toode+".jpg;"  + len);
                      	buf0.append(toode+".jpg\n");
                      }else{
                      	buf.append(toode+".jpg;"  + len + "\n");
                	    	b++;
                      }
                      }
        	    connectionDown();
        	    ///System.out.println("Valitud " + valitud.size());
                
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
                	//  System.out.println("buf 0 " + a  + " " +  b + " "+ buf0.toString());
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
             	
             	 
             	 File res2 = new File (pictspda);
                 String[] ff2 = res2.list();
             	 for(int i = 0;ff2.length>i;i++){
                 //	System.out.println("fff " + ff2[i]);
                 	(new File ( pictspda + ff2[i])).delete(); 
              
                 }
//             	 L�ppes kustutamine 
             	 
             	 
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
                    String outnimiean = (String)eanid.get(innimi);
                    if(outnimiean != null){
                    	File fdir = new File(results4+ "\\"  + outnimiean );
                    	if (!fdir.exists())fdir.mkdir();
                    	File out2 = new File(results4+ "\\"  + outnimiean +"\\" +innimi +".jpg");
                    	copyFile(in,out2);
                    }
                    if(outnimi != null){
                    	File out = new File(results+ outnimi + ".jpg");
                    	
                    	//System.out.println("out nimi " + outnimi);
                    	if(outnimi.startsWith("08") || outnimi.startsWith("09") ||outnimi.startsWith("10") ||outnimi.startsWith("11")
                    			 || outnimi.startsWith("31.05") || outnimi.startsWith("31.06")
    							|| outnimi.startsWith("61.15") || outnimi.startsWith("61.14")
    							 || outnimi.startsWith("41.01")
    							|| outnimi.startsWith("21.05") || outnimi.startsWith("21.06")
    							 || outnimi.startsWith("31.07") || outnimi.startsWith("31.08")
    							 || outnimi.startsWith("31.09") || outnimi.startsWith("31.10")
    							){
                    		copyFile(in,new File(results2 + outnimi + ".jpg"));
                    		
                    		//System.out.println("out nimi " + outnimi);
                    	}else{
                    		copyFile(in,out);
                    		copyFile(in,new File(results3 + innimi + ".jpg"));	
                    	}
                    	Converter.shrinkAndCopyFile150(in,inpda);
                    }	
                  	}
                  }
                  File fff =  new File (pictspda +  "list.txt");
            	  copyFile(f,fff);  	
            	  File fff0 =  new File (pictspda +  "list0.txt");
            	  copyFile(f0,fff0);  	
            
            	  zipFolder();  
            
        	} catch (Exception e) {
        	    getParent().showError(e);
        	}
        } 
    	
    }
    	
    	
    	
    	
        connectionUp();

        String cmd =
	    "  SELECT k.kood, k.nimi , trim(tanav)  AS aadress, piirkond,trim(kontaktisi) || ' ' || telefon AS kontakt,  " +
	  "  k.maksetin AS makse,hinnaale as ale, (select sum(tasumata)  from firma1.dok a where a.klient=k.kood) AS tasumata,0 AS limit, " +
	  "   0 as contrend, '' AS visit,k.maksja,k.objekt2 AS agent,k.wwwpage AS agent2,tulemus AS tagemail ,riik as grupp , infomail as grupp2 FROM firma1.klient k " +
	 "	 WHERE  k.objekt2 is not null   order by  k.kood, k.nimi ";


	log(cmd);

	TreeMap customers = new TreeMap();

	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
            String kood = rs.getString("kood").trim();
		Customer cust = new Customer(kood);
		String nimi = rs.getString("nimi").trim();
                cust.setName(Converter.enCodeEst(nimi));
                
                

              

                cust.setCountry("Latvia");
              
                

                
                
                //cust.setDebt(rs.getDouble("tasumata"));
                cust.setVisit(rs.getString("visit"));
                String aadr = rs.getString("aadress");
                if(aadr != null){
                	cust.restStreet = aadr;
                }
                
                String piirk = rs.getString("piirkond");
                
                cust.setAddress(Converter.adressLink(Converter.enCodeEst((parent.make(aadr) +  " " + parent.make(piirk)).trim()),"Latvia"));
                	cust.setPersonalData(Converter.enCodeEst(parent.make(rs.getString("kontakt"))));
                	cust.setPaymTerm(parent.make(rs.getString("makse")));
                    cust.setDiscount(rs.getDouble("ale"));
                
                
                
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
                	cust.setMainAgent(ag.trim());
                	cust.addAgent(new Agent("MERLE"));
                	cust.addAgent(new Agent("GATIS"));
                	cust.addAgent(new Agent("OFISS"));
                }
                
                String ag2 = rs.getString("agent2");
                if (ag2 != null && ag2.trim().length()>0){
                	cust.addAgent(new Agent(ag2.trim()));          	
               
                }
                
                cust.addAgent(new Agent("HOUSE"));
                String tagemail = rs.getString("tagemail");
                if (tagemail != null && tagemail.trim().length()>0){
                	cust.setReturnEmail(tagemail);          	
               
                }
                
                String grupp = rs.getString("grupp");
                String grupp2 = rs.getString("grupp2");
                
                if (grupp != null && grupp.trim().length()>0){
                	Vector vv = new Vector();
                	vv.add(grupp.trim());
                	if(grupp2 != null && grupp2.trim().length()>0){
                		vv.add(grupp2.trim());
                	}
                	cust.setFixSortCode(vv);          	
               
                }
                
                 
                customers.put(nimi, cust);
	    }
	} catch (Exception e) {
	    getParent().showError(e);
	}
	
	 if(rcompany != null){
      //	Converter.sendCustomerJson(customers, rcompany,false, user ,  pass);
      }
	return customers;
    }

    
    private void zipFolder(){
    	try {
    		  String dirin = parent.getProps().getProperty("zipin.dir");
  		  String dirout = parent.getProps().getProperty("zipout.dir");	
        File outFolder = new File(dirout + "pictures.zip");

            ZipOutputStream zos = new 
                   ZipOutputStream(new FileOutputStream(outFolder)); 
            
            zipDir(dirin, zos);
            zos.close(); 
        } 
        catch(Exception e) 
        { 
            //handle exception 
        } 
        
        }
    
    
  
    private void zipDir(String dir2zip, ZipOutputStream zos) 
    { 
        try 
       { 
            //create a new File object based on the directory we 
           // have to zip 
        	File      zipDir = new File(dir2zip); 
            //get a listing of the directory content 
            String[] dirList = zipDir.list(); 
            byte[] readBuffer = new byte[2156]; 
            int bytesIn = 0; 
            //loop through dirList, and zip the files 
            for(int i=0; i<dirList.length; i++) 
            { 
                File f = new File(zipDir, dirList[i]); 
            if(f.isDirectory()) 
            { 
                    //if the File object is a directory, call this 
                    //function again to add its content recursively 
                String filePath = f.getPath(); 
                zipDir(filePath, zos); 
                    //loop again 
                continue; 
            } 
                //if we reached here, the File object f was not 
               // a directory 
                //create a FileInputStream on top of f 
                FileInputStream fis = new FileInputStream(f); 
                //create a new zip entry 
            ZipEntry anEntry = new ZipEntry(f.getPath()); 
                //place the zip entry in the ZipOutputStream object 
            zos.putNextEntry(anEntry); 
                //now write the content of the file to the ZipOutputStream 
                while((bytesIn = fis.read(readBuffer)) != -1) 
                { 
                    zos.write(readBuffer, 0, bytesIn); 
                } 
               //close the Stream 
               fis.close(); 
        } 
    } 
    catch(Exception e) 
    { 
        //handle exception 
    } 
    }
    
    private TreeMap seisud = null;
    
    public TreeMap getProducts(Agent agent) {
        connectionUp();


        this.getSaveStockInfo();
	TreeMap products = new TreeMap();

	try {

           String cmd = "SELECT t.toode, t.nimetus AS nimetus, asukoht AS lisa ,max( t.tykke) as kastis, max(t.myygihind) as hind ,max(t.norm) as norm,max(ry.nimetus) AS ean,  " +
           	"  max(t.yhik) as yhik,max(t.objekt5) AS liik2 ,max((t.objekt4)) AS liik ,max(kampaania) as kestvus , max(pesakood) as pesakood ,max(t.myygikood) as km FROM firma1.toode t LEFT JOIN firma1.artryhm ry ON ry.kood=t.ryhm  " + 
		"		left join firma1.hinnamtr mt ON mt.ryhm = t.ryhm AND mt.hinnak = '01'   WHERE   " +
         "        length(trim(t.toode))>2 AND t.grupp='L' AND t.seisund is null  AND  t.pesakood is not null  AND aktiivne=true  " + 
          "      GROUP BY t.toode,t.nimetus,asukoht,pesakood ORDER BY t.pesakood,t.nimetus";

			if (isDebug()) {
			    System.out.println(cmd);
			}
			Connection con = DriverManager.getConnection(url);
			Statement stmt = con.createStatement();
           ResultSet  rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String name = rs.getString("nimetus").trim();
		
		String lisa = rs.getString("lisa");
		double kastis = rs.getDouble("kastis");
		if(lisa != null && lisa.trim().length() > 0){
			//name = name + " " + lisa;
		}

		
          Product prod = new Product(kood);
		       	  
                    
          if(seisud.get(kood) != null){
        	  try{
        		  
  			   prod.setStock(Double.parseDouble(seisud.get(kood)+ ""));
        	  
        	  }catch(Exception e){
        		  e.printStackTrace();
        	  }
  		}                  
            if(kastis > 0){
            	prod.setInCases(kastis);
            }
               prod.setPrice(rs.getDouble("hind"));
                prod.setEAN(parent.make(rs.getString("ean")));
                prod.setUnit(parent.make(rs.getString("yhik")));
                prod.setGroup(parent.make(rs.getString("liik")));
                prod.setGroupSecond(parent.make(rs.getString("liik2")));
                String pesakood = rs.getString("pesakood");
                String name2 = pesakood + name + kood;
                String km = rs.getString("km");
                if(km != null && km.trim().equals("21")){
                	prod.setVat("21");
                }else{
                	prod.setVat("12");
                }
             /*   if(dd.getTime() >((new java.util.Date()).getTime())){
                    	System.out.println("Kampaania " + prod.getName());
                    	prod.setStatusDiscount("1");         	
                    	prod.setGroup("0217");
                    }*/
                
                String pesa = "";
                if(pesakood != null && pesakood.trim().length()>0){
                	//prod.setStatusDiscount("1");   
                //	prod.setGroup("0217");
                	pesa = pesakood.trim() + " "; 
                }
                name = "    " + pesa + name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
                prod.setName(Converter.enCodeEst(name));
            	
                
		        products.put(name, prod);
		        prodssort.put( prod.getID(),name2);
	          }
            rs.close();
            stmt.close();
            con.close();
            

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
    
    
    private HashMap prodssort = new HashMap();
    
    
    
    public Vector getCustomerProductsList(String agentCode) {
        connectionUp();
	
	String cmd =
	    "SELECT dok.klient,rid.toode,toode.pesakood,dok.kuupaev,max(rid.kogus) as kogus,max(rid.hind)  as hind FROM firma1.dok INNER " +
	    "JOIN firma1.rid ON dok.dokumnr = rid.dokumnr INNER JOIN firma1.toode on rid.toode = toode.toode WHERE " +
	    " length(trim(dok.klient)) > 1 AND length(trim(rid.toode)) > 2  " +
	    "  AND dok.kuupaev >= current_date -60 AND  dok.doktyyp = 'T'     " +
	    " GROUP BY dok.klient, rid.toode,toode.pesakood,dok.kuupaev  ORDER BY dok.klient, toode.pesakood,dok.kuupaev";

	if (isDebug()) {
	    System.out.println(cmd);
	}

	Vector custProds = new Vector();

	try {
		
		Connection con1 = DriverManager.getConnection(url);
       Statement stm = con1.createStatement();
	    ResultSet	      rs = stm.executeQuery(cmd);

	    while (rs.next()) {
		String kliendikood = rs.getString("klient").trim();
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
            stm.close();
            con1.close();

	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return custProds;
    }

    public Vector getDebt(String agentCode) throws SQLException{
        Vector list = new Vector();
        String sql = "SELECT klient,tasudok AS number,tasukuup AS maksepaev,tasumata, kuupaev ,tasukuup-current_date as yle FROM " +
                    " firma1.dok WHERE   doktyyp='G' AND tasumata<>0  ORDER BY klient";


                 //jee
        con = DriverManager.getConnection(url);
        Statement stmt = con.createStatement();
        ResultSet	      rs = stmt.executeQuery(sql);
  	    while(rs.next()){
          Debt debt = new Debt();
          debt.setCustID(rs.getString("klient").trim());
          debt.setDoknr(rs.getString("number").trim());
          Date dd = rs.getDate("maksepaev");
          if(dd != null){

              debt.setDuedate(dd);  
          }else{

              debt.setDuedate((new Date(Calendar.getInstance().getTimeInMillis())));
          }

          debt.setSum(rs.getDouble("tasumata"));
          debt.setDate(rs.getDate("kuupaev"));
          
          String due = rs.getString("yle");
          if(dd != null){
        	  debt.setDue(due);  
          }
         
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

    public void setBulkSales(Map agents){

          String cmd =   "SELECT dok.pais2obj AS agent ,SUM(rid.hind*rid.kogus) AS sales FROM firma1.dok INNER JOIN firma1.rid ON  " +
        "  dok.dokumnr = rid.dokumnr INNER JOIN firma1.toode on rid.toode = toode.toode WHERE  " +
        "  length(trim(dok.klient)) > 1 AND length(trim(rid.toode)) > 2   AND  " +
        "  date_part('YEAR',dok.kuupaev)=date_part('YEAR',current_date) AND date_part('MONTH',dok.kuupaev)=date_part('MONTH',current_date)  " + 
        "  AND dok.doktyyp = 'G'  GROUP BY dok.pais2obj ";
      	
      	            log(cmd);
      	          connectionUp();
          try {
              Statement stmt = con.createStatement();

              Calendar cal = Calendar.getInstance();
              cal.set(Calendar.DAY_OF_MONTH, 1);

              ResultSet	      rs = stmt.executeQuery(cmd);
              while(rs.next()) {
                String agentID = rs.getString("agent");
                if(agentID != null){ 	
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                  agent.setMonthSales((int)rs.getDouble("sales"));
                  //System.out.println(agent.toString() + " " + agent.getMonthSales());
                }
              }
              }
              rs.close();
              stmt.close();
              con.close();
          } catch (Exception e) {
              getParent().showError(e);
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
    public static final String DRIVER_VERSION = "EevaPalLat";
    private String	       url = "jdbc:odbc:eevalat";
    private Connection	       con = null;
    private Statement	       stmt;
    private String	       subjectFilesTo = null;
    private boolean tavaline = true;
    
    private  Vector lines = new Vector();
	
    
    public Vector getFixedAssortmentData1() {
        TreeMap sorting = new TreeMap();
        this.getProducts(null);
         Vector vv = new Vector();
    	 this.connectionUp();
         try {
         	TreeMap sort = new TreeMap();
        	  String cmd2 = " select tasudok AS grupp  ,ry.nimetus as toode from firma1.dok d inner join firma1.rid r " +
        	  		"on r.dokumnr=d.dokumnr  inner join firma1.toode t on t.toode=r.toode LEFT JOIN firma1.artryhm ry ON " +
        	  		" ry.kood=t.ryhm " +
 			"	where d.doktyyp ='H'   and  length(trim(t.toode))>2 AND t.grupp='L' and tasudok != '' and ry.nimetus != '' order by  tasudok, t.pesakood, t.nimetus ";
        	  
        	 Statement stmt = con.createStatement();
 	        ResultSet rs = stmt.executeQuery(cmd2);
 	        
 	        while(rs.next()){
 	        	String clGroup = rs.getString("grupp").trim();
 	        	String prodID = rs.getString("toode").trim();
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
 	        rs.close();
 	        stmt.close();
 	        
 	        System.out.println("sort size " + sort.size() + " " + sort.toString());
        	TreeMap tooted = new TreeMap();
        	 stmt = con.createStatement();
        	 
        	 String cmd = "  select t.toode , ry.nimetus  as nimi from  firma1.toode t inner JOIN firma1.artryhm ry ON ry.kood=t.ryhm where t.aktiivne=TRUE " +
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
        	        rs.close();
       
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

                clGroup = grupp;
                
    

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
                       System.out.println(" Hinna Maatrix tehtud " + lines.size());
                       
                     Iterator itera =   sorting.values().iterator();
                     while (itera.hasNext()){
                     	lines.add(itera.next());
                     }
                     
         return lines;
   }

    
    
    @Override
	public Vector getPriceMatrixData() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	 public Vector getFixedAssortmentData() {
    	return lines;
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

