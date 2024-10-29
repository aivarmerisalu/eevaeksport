
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class SolcraftShellDBDriver extends AbstractDBDriver implements Discounter, Manager,Promo, BulkUpdater{

 
    public SolcraftShellDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        // String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
        //String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
       
      
    
  
        try {
            Class.forName(databaseDriverName).newInstance();
         ddd = DriverManager.getConnection("jdbc:odbc:finance", "SYSDBA", "master");
        try{
        	   bbb = DriverManager.getConnection("jdbc:odbc:finance", "SYSDBA", "master");	
        }catch(Exception ee){
        	
        }
      
         locProps = this.loadProps("groupreplace.properties");
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
    	 makePictureCat();
    	 System.exit(0);
       	return null;
       }
    
    private void makePictureCat(){
	     
        String cmd = "SELECT pr.artikuls AS toode  FROM preces  " +
     	"		  pr INNER JOIN grupas g ON g.grupa=pr.grupa   " +
          "        where (pr.act='+' and  fn_rtrim(g.grupa) like '08%'" + 
     		"	 order by pr.nosauk2";
           

       	
         	String picts = parent.getProps().getProperty("pict.dir");
     	    String pictspda = parent.getProps().getProperty("pictpda.dir");
     	 

       	try {
     	   Connection con = getNewCon();
    	
       		Statement stmt = con.createStatement();
       	    ResultSet	      rs = stmt.executeQuery(cmd);


            StringBuffer buf = new StringBuffer();
            StringBuffer buf0 = new StringBuffer();
            int a = 0;
            int b = 0;
            int c = 0;
            
            // leiame kataloogid ja peame meeles 
            File d  = new File(picts);
            File[] dd = d.listFiles();

            Vector cats = new Vector();
            
            for(int i = 0; i < dd.length ; i++){
            	File t = dd[i];
            	if(t.isDirectory()){
            		cats.add(t.getName());
            	}
            }
            
            System.out.println("Piltide leidmine - katalooge sees " + cats.size() + " " + cats.toString());
       	    while (rs.next()) {
       	    	a++;
       	    	c++;
       	    	
       	     String toode = rs.getString("toode").trim();  
             toode = toode.replace('/', '_');
             toode = toode.replace('/', '_');
       	    	if(c == 100){
       	    		  c = 0;
       	    		  System.out.println("Piltide leidmine "  + a);	
       	    		  
       	    	}
       	     
                  
                     File f = new File(picts + toode+".jpg");
                     String len = "0";
                     if(f.exists()){
                       len =	f.length() + "";
                     } else if(cats.size() > 0 ){
                    	 for(int w = 0 ;w < cats.size();w++){
                    		f = new File(picts + cats.elementAt(w) + "/" + toode+".jpg");
                    		
                    		if(f.exists()){
                    			System.out.println("Kataloogis leitud " + picts + cats.elementAt(w) + "/" + toode+".jpg");	
                    		     len =	f.length() + "";	
                    		}                     
                    	 }
                     }
                     
                     if(len.equals("0")){
                     	//System.out.println(toode+".jpg;"  + len);
                     	buf0.append(toode+".jpg\n");
                     }else{
                     	buf.append(toode+".jpg;"  + len + "\n");
               	    	b++;
                     }
                     }
       	    rs.close();
       	    stmt.close();
       	    con.close();
       	
       	    //oleasoleva kataloogi kustutamine
       	  System.out.println("Kataloogi kustutamine "  + a);	    
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
                 	  
      
              	 
              	 
            	 File res2 = new File (pictspda);
                String[] ff2 = res2.list();
            	 for(int i = 0;ff2.length>i;i++){
                //	System.out.println("fff " + ff2[i]);
                	(new File ( pictspda + ff2[i])).delete(); 
             
                }
//            	 L�ppes kustutamine 
            	 
              System.out.println("Piltide kopeerimine ");	
              //Failide kopeerimine  
                String[] gg = new File(picts).list();
             	  //System.out.println("gg " + gg[0] + " " + gg.length);
                
                 for(int i = 0;gg.length>i;i++){          	
                 	File in = new File ( picts + gg[i]); 
                 	if(in.isFile()){            	
                 	File inpda = new File ( pictspda + toUpper(gg[i])); 
                       	copyFile(in,inpda);
                 	}
                 }
                 
                 //piltide kopeerimine alam kataloogist ..
                 
                 if(cats.size() >0){
                	 for(int h = 0;h < cats.size(); h++){
                		
                	   String[]  ff = new File(picts + cats.elementAt(h)+ "/").list();
                   	 
                      
                       for(int i = 0;i < ff.length  ;i++){          	
                       	File in = new File ( picts + cats.elementAt(h)+ "/" + ff[i]); 
                       	if(in.isFile()){   
                       		
                       	File inpda = new File ( pictspda + toUpper(ff[i])); 
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
     
       private String toUpper(String asi ){
    	   
    	   asi = asi.substring(0,asi.length()-4).toUpperCase() + asi.substring(asi.lastIndexOf("."));
    	   System.out.println(asi);
    	   return asi ;
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


    
    
    public String getProductSortingData(){
       	return null;
       }
    
	public Properties loadProps(String fileName) {
		Properties props = new Properties();

		try {
			File file = new File(fileName);
			if (file.exists()) {
				FileInputStream in = new FileInputStream(file);
				props.load(in);
				in.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return props;
	}

    public String getAssortmentStr(Agent agent){
   
    	return null;
      }




        public String getDealerCustomerCSV(String agent){
            return null;
            }

        
        public Vector getFixedAssortmentData() {
            Vector lines = new Vector();

            try {
           	  String cmd = "  select s.papild_nosauk2 as grupp,g.artikuls as toode " +
   " from atlaizhu_grupas s " +
   "      left join atlaizhu_grupas_saturs d on s.id=d.atlgrupas_id " +
   "      left join atlaides_saturs a on a.id=d.id " +
   "      left join preces g on a.kods=g.kods " +
   " where a.tips=3 and (s.papild_nosauk2='NUKO' or s.papild_nosauk2='BALTSTOR' or s.papild_nosauk2='MEGO'  or s.papild_nosauk2='OLAINFARM APTIEKA' " +
   " or s.papild_nosauk2='BETA' or s.papild_nosauk2='AIBE' or s.papild_nosauk2='ELVI' " +
" or s.papild_nosauk2='IEP GRUPA' or s.papild_nosauk2='VIRSI' or s.papild_nosauk2='DEPO' " +
" or s.papild_nosauk2='LATS'  or s.papild_nosauk2='SPIRITS' or s.papild_nosauk2='KESKO' or s.papild_nosauk2='RIMI'" +
" or s.papild_nosauk2='DROGAS'  or s.papild_nosauk2='NARVESEN'  or s.papild_nosauk2='KURSI' or s.papild_nosauk2='VIADA' or s.papild_nosauk2='MEDELENS' or s.papild_nosauk2='CIRCLE' " +
" or s.papild_nosauk2='MAXIMA' or s.papild_nosauk2='STOCKMANN'" +
"  or s.nosaukums='IEP GRUPA' or s.papild_nosauk2='PALMAS' or s.papild_nosauk2='EAST WEST TRANSIT' or s.papild_nosauk2='VIRSI'" + 
" or s.papild_nosauk2='CENUKLUBS' or s.papild_nosauk2='MANA APTIEKA' or s.papild_nosauk2='SENTOR FARM' or s.papild_nosauk2='ASTARTE'" +
" or s.papild_nosauk2='BENU APTIEKA' or s.papild_nosauk2='MANA APTIEKA' " +
" or s.papild_nosauk2='BBH INVESTMENTS' or s.papild_nosauk2='TN 2000' )" +
" order by g.PRIORITY,g.nosauk2 ";


 
           	
           	System.out.println(cmd);
              	 Connection con = getNewCon();
          	        Statement stmt = con.createStatement();
          	        ResultSet rs = stmt.executeQuery(cmd);
          	        while(rs.next()){
                             String custID = " ";
                             String clGroup = " ";
                             String prodID = " ";
                             String prodGroup = " ";
                             MatrixLine mline = new MatrixLine();

                             clGroup = rs.getString("grupp");
                             prodID = rs.getString("toode");

                             if (custID != null && custID.trim().length() > 0) {
                               mline.setCustID(custID.trim());
                             }
                             if (clGroup != null
                                 && clGroup.trim().length() > 0) {
                               mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
                             }

                             if (prodID != null && prodID.trim().length() > 0) {
                               mline.setProdID(prodID.trim());
                             }
                             if (mline.getProdID() == null && prodGroup != null
                                 && prodGroup.trim().length() > 0) {
                               mline.setProdGroup(prodGroup);
                             }

                             lines.add(mline);
                           }

          	       rs.close();
          	       con.close();
                          } catch (Exception e) {
                              getParent().showError(e);
                          }
//                          getFixedAssortmentDataLog(lines);
            return lines;
    }
        
                                                            public String  getCustProdDiscounts(Agent agent){
                                                     return null;
                                                     }

                                                	public Vector getLastThreeData() {
                                                		// TODO Auto-generated method stub
                                                		return null;
                                                	}
    /**
     *
     */
    public void connectionUp(){
    }

    private Connection getNewCon() throws SQLException{
      return DriverManager.getConnection(url, "SYSDBA", "master");
    }
    

    /**
     *
     */
    public void connectionDown(){
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
      Connection con = getNewCon();
      System.out.println("tootegrupid alustan ");
      //setTreeGroup(true);
    TreeMap   tm = new TreeMap();
    String    sql = " SELECT nosauk AS nimetus2 ,grupa from grupas " ;


    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
    	
    	String nimetus2 =  rs.getString("nimetus2");
    	if(nimetus2 != null) {
    		nimetus2 = nimetus2.trim().replaceAll("PLEOMAX", "PLE")
    				.replaceAll("Helix", "HEL")
    				.replaceAll("Rimula", "RIM")
    				.replaceAll("Industrial Engine Oils Gas Eng", "MYSELLA");
        	String kood =  rs.getString("grupa").trim();
        		
        	if(kood.startsWith("08") ) {
        
        		tm.put(kood,  Converter.enCodeEst(nimetus2.trim()));
        		 
        	}
    	}
    	    
    	
    }
    
    rs.close();
    st.close();
    con.close();
    System.out.println("tootegrupid ending " + tm.toString() );
   try{
	  // getLogProdGroups(tm);
   }catch(Exception rr){   
   }
    return tm;
    }


        public String getDistribution() {
          return null;
        }



    public TreeMap getAgents() {
      String  cmd = "select num AS kood , nosauk || ' ' ||num as nimi ,nosauk as name from kontr  \r\n" + 
      		"      		where nosauk != '' and num like 'SH%' ORDER BY num";
        TreeMap agents = new TreeMap();
        
        try {
        	try{
        		getProdGroups();	
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        
          Connection con = getNewCon();
            Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);
            System.out.println("Agents Starting ");
            while (rs.next()) {
              Agent agent = new Agent(rs.getString("kood").trim());

              String nimi = rs.getString("nimi").trim();

              String nimi2 = rs.getString("name").trim();
              agent.setName(nimi);
              agent.setIsPilot(true);
              agent.setLanguage(nimi2);
              agents.put(nimi, agent);
            }
            rs.close();
            stmt.close();
            con.close();

        } catch (Exception e) {
            getParent().showError(e);
        }
        Agent hh = new Agent("HOUSE");
        hh.setIsPilot(true);
        hh.setName("Office");
        Agent hh1 = new Agent("HOUSE1");
        hh1.setIsPilot(true);
        hh1.setName("Office");
        Agent hh2 = new Agent("HOUSE3");
        hh2.setIsPilot(true);
        hh2.setName("Office");
        agents.put("HOUSE", hh);
        agents.put("HOUSE1", hh1);
        agents.put("HOUSE3", hh2);
        System.out.println("Agents Ready ");
        return agents;
    }



    private String getDir(){
      String dir = getParent().getProps().getProperty("readin");
      if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
      return dir;
    }



    public TreeMap getCustomers(Agent agent){

      String cmd = "SELECT  a.nosauk AS nimi,a.pkonts AS kood,fn_rtrim(a.adrese) || ' ' || fn_rtrim(a.pilseta) AS aadress,a.atlaide AS discount,a.uni AS visit,a.pers_amats " +
      		" || ' ' || a.talrunis AS info, " +
            " agents AS agent,AGENTS1 , AGENTS2, TELEX as AGENTS3, APMEKLEJUMA_NOSACIJUMI as aginfo,'' AS payer , a.kons_dienas AS makse ,papild_nosauk AS grupp ,papild_nosauk || '' ||" +
            " papild_nosauk2 AS klale , palms as list2 , MIN_PASUT_SUMMA as minsum ,JGT_VS_LOGSOL as logsol, plu as eucerin , LICENCES_STAT as lic , FWPIEGDIENAS as vedu " +
            " , fn_rtrim(SHIFRS2) ||' '||fn_rtrim(a.KLTIPS) as klgrupp, "
            + "adrese as rstreet ,pilseta as rcity , kltips as rkategooria , kategory as rchain FROM adreses a  WHERE  (a.tips!='A' " +
            " and a.nosauk not starting '#'  and a.nosauk not starting '*' and agents is not null and papild_nosauk ='SHELL' ) or a.pkonts = '0100000' ORDER BY a.nosauk";

      String cmd2 = "SELECT a.pkonts AS kood FROM adreses a  WHERE  (a.tips!='A' " + 
           "  and a.nosauk not starting '#'  and a.nosauk not starting '*' and agents is not null and palms!='') ORDER BY palms ";
      StringBuffer buf = new StringBuffer();
      
  	  
      String salessql = " select a.pkonts AS klient , case when (extract( month from datums) <10) then  '0' || cast ((extract( month from datums)) as char) else   \r\n" + 
      		"     	      cast ((extract( month from datums)) as char) end as kuu, \r\n" + 
      		"			    sum(daudzums) AS summa\r\n" + 
      		"      		 from dokumenti dok inner join dok_spec rid on rid.dok_kods=dok.kods  \r\n" + 
      		"      		   INNER JOIN adreses a ON a.kods=dok.san_kods  INNER JOIN preces pr ON pr.kods=rid.preces_kods where  \r\n" + 
      		"  datums > current_timestamp-200  and daudzums > 0 and pr.mervien like 'lit%' and dok.papild_nosauk ='SHELL' and fn_atrim(pr.grupa) like '08%'\r\n" + 
      		"      	GROUP BY a.pkonts,case when (extract( month from datums) <10) then  '0' || cast ((extract( month from datums)) as char) else   \r\n" + 
      		"     	      cast ((extract( month from datums)) as char) end \r\n";
      	//	"  ORDER BY a.pkonts,case when (extract( month from datums) <10) then  '0' || cast ((extract( month from datums)) as char) else   \r\n" + 
      		//"     	      cast ((extract( month from datums)) as char) end";
      
      
     
        log(cmd);
        
        double minordsum = 0;
        if(getParent().getProps().getProperty("minordsum") != null){
        	try{
        		minordsum = new Double(getParent().getProps().getProperty("minordsum")).doubleValue();
        	}catch (Exception e){
        		e.printStackTrace();
        	}
        }
        System.out.println("kliendid alustan , minordsum " + minordsum);
        
        boolean  lookForOneTime = false;
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        HashMap custMap2 = new HashMap();
        Vector visordone = new Vector();
        
        if("true".equals(getParent().getProps().getProperty("lookForOneTime"))){
        	lookForOneTime = true;
        }
      
        HashMap salesres = new HashMap();
        
        
        
        Vector sup2 = new Vector();
        try {
        	
            Connection con = getNewCon();
        	
        	Statement stmt = null;// con.createStatement();
           /*  ResultSet rs1 = stmt.executeQuery(salessql);
             while(rs1.next()){
             	String klient = rs1.getString("klient").trim();
             	String kuu = rs1.getString("kuu");
             	int summa = (int)rs1.getDouble("summa");
               if(salesres.get(klient) == null){
             	  salesres.put(klient,"   " + kuu + " : " + summa + " " );
               }else{
             	  String asi = (String)salesres.get(klient);     	  
             	  salesres.put(klient,"   " + kuu + " : " + summa +" |" + asi);
               }
             }
            rs1.close();
            stmt.close();*/
         
         //	System.out.println(salesres.toString());
        	
        	
     

           stmt = con.createStatement();
          Statement smt = ddd.createStatement();
          
          ResultSet	      rs5 = stmt.executeQuery(cmd2);
         
          while (rs5.next()) {
        	  String ooo = rs5.getString("kood").trim();
        		 // sup2.add(ooo);
        	  buf.append(ooo + System.getProperty("line.separator"));
          }
          File f = new File(getDir() + "asksup2.txt");
          if (f.exists())f.delete();
          if(buf.length() > 2){
              
                try {
          		PrintWriter os = new PrintWriter(new FileWriter(
          				getDir() + "asksup2.txt", true));
          		os.write(buf.toString());
          		os.close();
          	  }catch (IOException ex) {
          		ex.printStackTrace();
          	  }
           }
          
          
      	FileReader reader;
		try {
			reader = new FileReader(getDir() + "asksup2.txt" );
	
		BufferedReader br = new BufferedReader(reader);
		String line;
		while ( (line = br.readLine()) != null) {
			sup2.add(line);	
		}	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
          
          
          
          
          
          
     /*     String sql11 = "select custid from supervisor where " +
     
          		" extract(year from sentdate)=extract(year from current_timestamp) and prodid like 'rb%' group by custid";
          System.out.println(sql11);
          Connection ddd = DriverManager.getConnection(url2, "SYSDBA", "master");
          Statement str = ddd.createStatement();
          ResultSet rst1 = str.executeQuery(sql11);
          while(rst1.next()){
          	String custom = rst1.getString("custid").trim();
          	//visordone.add(custom);     	
          }
*/
          
          
          
          
          
          
            ResultSet	      rs = stmt.executeQuery(cmd);
            int counter = 0;
            while (rs.next()) {
              counter++;

              Customer cust = new Customer(rs.getString("kood").trim());
                
              String nimi = rs.getString("nimi").trim();
              cust.restName = nimi;

              if(!nimi.endsWith("#")){
                   // System.out.println("nimi1 " + nimi);
                	nimi = nimi.replaceAll("Z/S","");
                	nimi = nimi.replaceAll(" SIA","");
                	nimi = nimi.replaceAll("AIZKRAUKLES","AIZ.");
                	nimi = nimi.replaceAll("NAFTA ","NA. ");

                String aadress = rs.getString("aadress");
                if(aadress != null && aadress.trim().length()>0){
                    cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress.trim()),"Latvia"));
                    nimi = nimi + " " +aadress.trim()+ " " + cust.getID();
                }
                cust.setName(Converter.enCodeEst(nimi));

                String ale = rs.getString("discount");
                if (ale != null && ale.trim().length()>0){
                  cust.setDiscount(Double.parseDouble(ale.trim()));
                }

                String visit=rs.getString("visit");
                if (visit!=null && visit.trim().length()>0){
              //    cust.setVisit(visit.trim().toUpperCase());
                }
                
                if(minordsum != 0){
                	cust.setMinsum(minordsum);
                }
                
                if(salesres.get(cust.getID()) != null){
                  	cust.setInfo(Converter.enCodeEst("CHART sales Lit. :|Month & year|Sales |" )+ salesres.get(cust.getID()) );
                  }

                String info = rs.getString("info");
                if(info != null && info.trim().length()>0){
                	cust.setPersonalData(Converter.enCodeEst(info.trim()));
                }

                String agentc = rs.getString("agent");
                String agente = rs.getString("agents1");
                String agentr = rs.getString("agents2");
                String agentt = rs.getString("agents3");
                
            
                String aginfo = rs.getString("aginfo");
                if(aginfo != null && aginfo.trim().length()>0){
                	
                	StringTokenizer st = new StringTokenizer(aginfo.trim(),";");
                	while(st.hasMoreTokens()){
                		String deal = st.nextToken();
                		StringTokenizer de = new StringTokenizer(deal.trim(),"|");
                		while (de.hasMoreTokens()){
                			/*
                			 * 1|0|01146;3|0|01141;

1| - it means days "1, 2, 3, 4, 5" (E, T, K, N, R)
0| - it means paired, nonpaired "2,1" (1/2)     OR     4, 8, 16, 32
01146;- its sales person code
*/ 
                			try{
                				String paev = de.nextToken();
                    			String nadal = de.nextToken();
                    			String age = de.nextToken();
                    			paev = paev.replaceAll("1", "E").replaceAll("2", "T").replaceAll("3", "K").replaceAll("4", "N").replaceAll("5", "R").replaceAll("6", "L").replaceAll("7", "P");
                    			if(nadal.equals("2")){
                    				nadal = "1";
                    			}else if (nadal.equals("1")){
                    				nadal = "2";
                    			}else if (nadal.equals("0")){
                    				nadal = "";
                    			}
                    			paev = paev + nadal;
                    			
                    			Agent aaa = new Agent(age);
                    			//aaa.setVisit(paev);
                    			if(!age.contains("|"))
                    			cust.addAgent(aaa);
                    	//		System.out.println(cust.getID() + " " + paev + " " + age );		
                			}catch (Exception ee){
                				//ee.printStackTrace();
                			//	System.out.println("Probleemne klient " + cust.getID() );		
                			}
                
                			
                		}
                	}
                    cust.addAgent(new Agent(aginfo.trim()));
                    if(aginfo.trim().indexOf("SH") == -1){
                        cust.addAgent(new Agent("HOUSE"));  
                        cust.addAgent(new Agent("HOUSE1")); 
                        cust.addAgent(new Agent("HOUSE3"));
                    } 
                }
                if(agente != null && agente.trim().length()>0 && !cust.hasAgent(agente.trim())){
                    cust.addAgent(new Agent(agente.trim()));
                    if(agente.trim().indexOf("SH") == -1){
                        cust.addAgent(new Agent("HOUSE"));  
                        cust.addAgent(new Agent("HOUSE1"));
                        cust.addAgent(new Agent("HOUSE3"));
                    }
                
                }
                if(agentc != null && agentc.trim().length()>0 && !cust.hasAgent(agentc.trim())){
                    cust.addAgent(new Agent(agentc.trim()));
                    if(agentc.trim().indexOf("SH") == -1){
                        cust.addAgent(new Agent("HOUSE"));  
                        cust.addAgent(new Agent("HOUSE1")); 
                        cust.addAgent(new Agent("HOUSE3"));
                    } 
                }
                
                if(agentr != null && agentr.trim().length()>0 && !cust.hasAgent(agentr.trim())){
                    cust.addAgent(new Agent(agentr.trim()));
                    if(agentr.trim().indexOf("SH") == -1){
                        cust.addAgent(new Agent("HOUSE"));  
                        cust.addAgent(new Agent("HOUSE1"));
                        cust.addAgent(new Agent("HOUSE3"));
                    }  
                 }            
              
                if(agentt != null && agentt.trim().length()>0 && !cust.hasAgent(agentt.trim())){
                    cust.addAgent(new Agent(agentt.trim()));
                    if(agentt.trim().indexOf("SH") == -1){
                        cust.addAgent(new Agent("HOUSE"));  
                        cust.addAgent(new Agent("HOUSE1"));
                        cust.addAgent(new Agent("HOUSE3"));
                    } 
                }
                
                cust.addAgent(new Agent("SH10"));
                                
                if (cust.getID().equals("0100000")) {
                	agentc = "aia";
                	logMap.put("0110000000", "0100000");

                    Enumeration enums = getParent().getAgentsHash().elements();
                      while (enums.hasMoreElements()) {
                        Agent a = (Agent) enums.nextElement();
                        cust.addAgent(a);
                        
                        }
                      cust.addAgent(new Agent("HOUSE"));  
                      cust.addAgent(new Agent("HOUSE1"));
                      cust.addAgent(new Agent("HOUSE3"));
                      }

         String payer = rs.getString("payer");
         if (payer != null && payer.trim().length()>0){
            cust.setPayer(payer.trim());
         }

         String makset = rs.getString("makse");
         if (makset != null && makset.trim().length()>0){
         	if(makset.trim().equals("0")){
         		makset = "-1";
         	}
            cust.setPaymTerm(makset.trim());
         }


         String grupp = rs.getString("grupp");
         if (grupp != null && grupp.trim().length()>0){
         	if(grupp.trim().equals("01")){
         		grupp = "A";
         	}
        	if(grupp.trim().equals("02")){
         		grupp = "B";
         	}
        	if(grupp.trim().equals("03") || grupp.trim().equals("04") ||
        			grupp.trim().equals("05")  || grupp.trim().equals("08")
					 || grupp.trim().equals("09")
					 || grupp.trim().equals("14")){
         		grupp = "C";
         	}
        	if(grupp.trim().equals("06")){
         		grupp = "D";
         	}
            cust.setCustType(Converter.cleanLitLatEst(grupp.trim()));
            Vector vv = new Vector();
            vv.add(grupp.trim());
            cust.setFixSortCode(vv);
         }

         String alegr = rs.getString("klale");
         if(alegr != null && alegr.trim().length()>0){
         	cust.setGroup(Converter.cleanLitLatEst(alegr.trim()));
         }

         
         double minsum = rs.getDouble("minsum");
          if(minsum > 0){
        	  cust.setMinsum(minsum);
          }
          if(minordsum == -1){
        	  cust.setMinsum(-1);
          }
          
          String logsol = rs.getString("logsol");
          if(logsol != null){
        	  logMap.put(logsol, cust.getID());
          }
          String teade = rs.getString("eucerin");
          if(teade != null && teade.trim().length() != 0){
        	  if(teade.equals("1")){
        		  cust.setInfo("Shell netirgot !||");
        		  cust.setCity("Shell netirgot !");
        	  }
          }
          
          String lic  = rs.getString("lic");
          if(lic != null && lic.trim().length() != 0){
        	  if(lic.equals("1")){
        		  cust.setLicence("OTC");
        		  //kllendile v�ib m��a
        		  
        		  // 2 ei v�i 
        		  // tyhi siis ka v�ib 
        	  }
        	  
        	  if(lic.equals("3")){
        		  cust.setLicence("ALC");
        		  //kllendile v�ib m��a
        		    
                  //kui on 2 v�i tyhi alkot ei tohi 1 v�i 3
        		
        	  }
        	  
          }
      
        	if(cust.getID().equals("0100000")){
        		  cust.setLicence("OTC");
        	}
        	
        	 String vedu  = rs.getString("vedu");
             if(vedu != null && vedu.trim().length() != 0){
            	 if(cust.getCity() != null && cust.getCity().trim().length() > 0){
            		// cust.setCity( cust.getCity() + "|Pieg�des diena - " +  vedu.replaceAll(";", "-")); 	 
            	 }else{
            		// cust.setCity("Pieg�des diena - " +  vedu.replaceAll(";", "-")); 	 
            	 }

                 cust.setCity(Converter.enCodeEst(cust.getCity()));
             }
          
             String klgrupp  = rs.getString("klgrupp");
             if(klgrupp != null && klgrupp.trim().length() != 0){
            	 
            	 cust.setBlockingDealer(klgrupp.trim());
            	
             }

             String rstreet  = rs.getString("rstreet");
             if(rstreet != null && rstreet.trim().length() != 0){
            	 cust.restStreet = rstreet.trim();
             }
             
             String rcity  = rs.getString("rcity");
             if(rcity != null && rcity.trim().length() != 0){
            	 cust.restCity = rcity.trim();
            	 cust.setCountry("Latvia");
             }
             
             String rkategooria  = rs.getString("rkategooria");
             if(rkategooria != null && rkategooria.trim().length() != 0){
            	 cust.category = rkategooria.trim() ;
             }
           
             String rchain  = rs.getString("rchain");
             if(rchain != null && rchain.trim().length() != 0){
            	 cust.setChain(rchain.trim());
             }
            
             
          
          if(cust.getID().toLowerCase().indexOf("pr")==-1 && agentc != null && agentc.toLowerCase().indexOf("pr")==-1){
              customers.put(cust.getName(), cust);
              custMap.put(cust.getPayer(), cust);
              custMap2.put(cust.getID(), cust);
        	  
               }
              }
            }

            rs.close();
            stmt.close();
            con.close(); 
            
            String sql3 = "select pacc AS klient , sum(sv) as summa   from dokument_v d  where sv is not null and acc1 starting '2310' and " + 
            		"       current_timestamp  > dat1 and  dat1 > current_timestamp -240  group by pacc order by pacc ";
            System.out.println(sql3);
               ResultSet rst = smt.executeQuery(sql3);
               while(rst.next()){
               	String custom = rst.getString("klient");
               	Customer cc = (Customer)custMap2.get(custom.trim());
               	if (cc != null){
               //	int status = rst.getInt("staatus");
               	 //if(status==2){
               	  //cc.setSalesBlock(true);
               	 //}
                   double debt = Double.parseDouble(rst.getString("summa").trim());
                   if(debt != 0){
                   	cc.setDebt(debt);

                   }
               	}

               }
               
                sql3 = "select pacc AS klient , sum(sv) as summa   from dokument_v d  where sv is not null and acc1 starting '2310'  " + 
               		"      group by pacc order by pacc ";
               System.out.println(sql3);
               rst = smt.executeQuery(sql3);
                  while(rst.next()){
                  	String custom = rst.getString("klient");
                  	Customer cc = (Customer)custMap2.get(custom.trim());
                  	if (cc != null){
                  //	int status = rst.getInt("staatus");
                  	 //if(status==2){
                  	  //cc.setSalesBlock(true);
                  	 //}
                      double debt = Double.parseDouble(rst.getString("summa").trim());
                      if(debt != 0){
                      	cc.setUsedCredit(debt);

                      }
                  	}

                  }
                  rst.close();
                  smt.close();

           
               
        } catch (Exception e) {
            getParent().showError(e);
        }
        
        
        System.out.println("kliendid l�petan ");
        return customers;
    }


    
    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      System.out.println("debt alustan ");
      String sql = "select pacc AS klient , fdoc as number,dat1 as kuupaev, sv as summa " +
            "  ,current_timestamp-dat1 AS yle ,d.dokdat as algp from dokument_v d  where sv is not null and acc1 starting '2310' and " +
       "  dat1 > current_timestamp -240 order by pacc, datums, fdoc";

      Statement stmt = ddd.createStatement();

      ResultSet rs = stmt.executeQuery(sql);

      while(rs.next()){
        Debt debt = new Debt();
        String klient = rs.getString("klient");
        if(klient != null){
        	   debt.setCustID(klient.trim());
               debt.setDoknr(rs.getString("number").trim());

               debt.setDuedate(rs.getDate("kuupaev"));

               debt.setSum(Double.parseDouble(rs.getString("summa").trim()));
               debt.setDue(rs.getString("yle"));
               debt.setDate(rs.getDate("algp"));
               if(debt.getSum() > 0.05 ||  -0.05 > debt.getSum()  )
               list.addElement(debt); 	
        }
     
      }
       rs.close();
       stmt.close();
       System.out.println("debt l�petan  ");
       try{
      // getDebtLog(list);
    }catch(Exception rr){
 	   
    }
       return list;
    }

  
   
    
    
    public TreeMap getProducts(Agent agent){
    	

      String cmd = "SELECT pr.artikuls AS toode,pr.nosauk as nimi1,pr.nosauk2 AS nimi,pr.cena4 AS myygihind,( select first 1 tax_value from tax_values where tax_id = cast (g.nodoklis as int)  and  " +
   " current_timestamp >= tax_date_valid order by tax_date_valid desc) AS VAT,pr.mervien AS unit,pr.svars2 AS  " +
    "  		 tykke ,DALIJUMS AS minimum,OPTIMAL_AMOUNT as optimum , pr.mf_grupa AS uusgrupp,g.otc,g.grupa AS grupp,g.palm_descr AS grupp2  " +
     "        , pr.ecrkods AS ean ,PRIORITY AS sorting ,pr.palm_max_discount as maxdiscpr  ,g.palm_max_discount as maxdiscgr"
     + " , pr.shrink as lisa , pr.svars as inunit FROM preces  " +
	"		  pr INNER JOIN grupas g ON g.grupa=pr.grupa   " +
     "        where pr.act='+'  and fn_atrim(pr.grupa) like '08%' " +
		"	 order by pr.nosauk2";
      
      System.out.println(" tooted alustan ");

        if (isDebug()) {
            System.out.println(cmd);
        }

        TreeMap products = new TreeMap();
        try {
          Connection con = getNewCon();
          Statement st = con.createStatement();
            ResultSet	      rs = st.executeQuery(cmd);
            HashMap prodHash = new HashMap();
            while (rs.next()) {
                String kood = rs.getString("toode").trim();
                String abi = kood;
                if(abi.indexOf(".")!= -1){
                	abi = abi.substring(0,abi.indexOf("."));
                }
                if (abi.length()== 3){
                	abi = "00" + abi;
                }
                if (abi.length()== 4){
                	abi =  abi + "0";
                }
                String nimi1 =  rs.getString("nimi1");
                String nimetus =   rs.getString("nimi");
                if(nimetus == null || nimetus .trim().length() == 0 ){
                	nimetus = nimi1.trim().replaceAll("\n", " ") ;
                }else{
                	nimetus  = nimetus.trim().replaceAll("\n", " ") ;
                }
                Product prod = new Product(kood);
               
                prod.setPrice(rs.getDouble("myygihind"));
                prod.setVat(rs.getDouble("VAT") + "");
                prod.setPrice(prod.getPrice());
                String unit = rs.getString("unit");
                if(unit != null && unit.trim().length()>0){
                	if(unit.trim().equals("litri")) {
                		unit = "GABX";
                	}
                	prod.setUnit(unit.trim());
                }

                prod.setInCases(rs.getInt("tykke"));
                
                double minim = rs.getDouble("minimum");
                if (minim > 1){
                   // prod.setMinAmountInUnits(minim);
                }
                
                double optim = rs.getDouble("optimum");
                if (optim > 1){
                  //  prod.setOptAmountInUnits(optim);
                }
              //  prod.setStock(0);
               
                
                
                String grupp = rs.getString("grupp");
                if(grupp != null && grupp.trim().length() > 0){
                	if(grupp.trim().length() > 4) {
                		//grupp = grupp.trim().substring( 0 , 4 );
                	}
                	prod.setGroup(grupp.trim());
                }
                
           
               
                prod.setName(Converter.enCodeEst(nimetus + " " + kood));
                
                String ean = rs.getString("ean");
                if(ean != null && ean.trim().length()>0){
                	ean = ean.trim();
                	prod.setEAN(ean);
                	int len = ean.length();
                	if(len > 3){
                      	// prod.setName(Converter.enCodeEst(nimetus) + " " + ean.substring(len-4));
                           		
                	}
                }
                
                
                
                String sorting  = rs.getString("sorting");
                if(sorting != null && sorting.trim().length()>0){
                	prod.setNameSorting(sorting.trim()+ nimetus);
                }
                double maxdiscpr = rs.getDouble("maxdiscpr");
                double maxdiscgr = rs.getDouble("maxdiscgr");
                
                if(maxdiscgr != 0){
                	
                	//prod.setMaxDiscount(maxdiscgr);
                }
                if(maxdiscpr != 0){
                	//prod.setMaxDiscount(maxdiscpr);
                }
           
                String lisa  = rs.getString("lisa");
                if(lisa != null && lisa.trim().length() != 0){
                	prod.setName(Converter.enCodeEst(prod.getName()) + " " + lisa.trim());              	
                }
                
                double inunit = rs.getDouble("inunit");
                if (inunit > 0 ) {
                	prod.setInUnit(inunit);
                }
                
            	if(!kood.startsWith("PR")){
            		 products.put(nimetus, prod);
                     prodHash.put(prod.getID(), prod);
            	}
               
            }
            rs.close();


        	  String  sql2 = " SELECT artikuls AS toode, atlrez AS stock from pdastocksshell " ;

            rs = st.executeQuery(sql2);
            while(rs.next()){
              String id = rs.getString("toode").trim();
              Product prod = (Product)prodHash.get(id);
              if (prod != null){
            	  double sto = rs.getDouble("stock");
            	  if(prod.getInUnit() > 1 && prod.getUnit().equals("GAB")) {
            		  sto = sto / prod.getInUnit();
            	  }
                prod.setStock(sto);
              }
            }
            rs.close();
            st.close();
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }
try{
	// Logsol(products);	
}catch(Exception rr){
	   
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
       
    public Vector getCustomerProductsList(String agentCode) {
     // return new Vector();
      String sql1 = " select a.pkonts AS klient , pr.artikuls AS toode, daudzums AS kogus, datums AS kuupaev , sum(case when \r\n" + 
      		"      		 datums > current_timestamp-30 then daudzums else 0 end) as days30  , max((rea_cena *(100-atlaides_proc)/100)) as lprice , max(numurs) as bill \r\n" + 
      		"      		   		   from dokumenti dok inner join dok_spec rid on rid.dok_kods=dok.kods  \r\n" + 
      		"      		   		   INNER JOIN adreses a ON a.kods=dok.san_kods \r\n" + 
      		"      		   		   INNER JOIN preces pr ON pr.kods=rid.preces_kods where \r\n" + 
      		"      		   		   datums > current_timestamp-360  and daudzums > 0 and dok.papild_nosauk ='SHELL' and fn_atrim(pr.grupa) like '08%' \r\n" + 
      		"      		   		   GROUP BY pr.artikuls,a.pkonts,daudzums,datums\r\n" + 
      		"      		   		   ORDER BY a.pkonts,pr.artikuls,datums,daudzums" ;



        if (isDebug()) {
            System.out.println(sql1);

        }

        Vector custProds = new Vector();
        Vector  custProds2 = new Vector();
      //  return custProds;

        try {
          Connection con = getNewCon();
          PreparedStatement stmt = con.prepareStatement(sql1);

          //stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
          ResultSet	      rs = stmt.executeQuery();

          HashMap kliendid = new HashMap();
          while (rs.next()) {
            String kliendikood = rs.getString("klient").trim();
            String tootekood = rs.getString("toode").trim();
            double kogus = rs.getDouble("kogus");
            
            double l30 = rs.getDouble("days30");
            double lprice = rs.getDouble("lprice");
            String bill = rs.getString("bill").trim();
            Customer cust = (Customer)kliendid.get(kliendikood);
            
            if (cust == null){
              cust = new Customer(kliendikood);
             }
            
            String kup = rs.getString("kuupaev").substring(5,10);
            kup =kup.substring(3,5) + "-"+  kup.substring(0,2);
            
              Product prod = new Product(tootekood);
              CustProd cp = new CustProd(cust.getID(), prod.getID(), kogus, l30, 0,
                                           kup, lprice, 0, 0, " ");
            cp.setAbiKood(bill);
            CustProd cp2 = new CustProd(cust.getID()+"_1", prod.getID(), kogus, l30, 0,
                    kup, lprice, 0, 0, " ");
cp2.setAbiKood(bill);
            if(!tootekood.startsWith("PR")){
           	    cust.addProduct(prod,cp,false);
          	  }
          
              kliendid.put(kliendikood, cust);
              custProds.add(cp);
              custProds2.add(cp2);
              
          }

          rs.close();
          stmt.close();
          con.close();
        } catch (Exception e) {
          getParent().showError(e);
        }
        connectionDown();
        try{
        	//  getCustomerProductsListLog(custProds);
                	
        }catch(Exception ss){
        	
        }
        
  	  try {
			Converter.saveObject(custProds2, "custprod.vec");
		} catch (IOException e) {
		
			e.printStackTrace();
		}
      
        return custProds;
    }

    public String getPilotDayData(Agent agent){


      String sql2 =  "SELECT a.nosauk  || ' ' || a.adrese || ' ' || a.pilseta AS klient,numurs AS arve, d.created AS kuupaev," +
      		" summa9 AS summa , d.kontr " +
          " FROM dokumenti d INNER JOIN adreses a ON d.san_kods=a.kods WHERE  (kontr = '" + agent.getID()+ "' " +
          		"or kontr_jauns ='" +agent.getID() + "'  or  d.piezimes like '%" +agent.getID() + "%') AND " +
            " d.created > current_timestamp-1 AND d.tips='-'  ORDER BY d.created desc ";

      String sql3 =  "SELECT a.nosauk  || ' ' || a.adrese || ' ' || a.pilseta AS klient,numurs AS arve, d.created AS kuupaev," +
		" summa9 AS summa , d.kontr " +
    " FROM dokumenti d INNER JOIN adreses a ON d.nos_kods=a.kods WHERE (kontr = '" + agent.getID()+ "'" +
    		" or kontr_jauns ='" +agent.getID() + "' or  d.piezimes like '%" +agent.getID() + "%') AND " +
      " d.created > current_timestamp-1 AND d.tips='+'  ORDER BY d.created desc ";

      String sql4 =  "SELECT a.nosauk  || ' ' || a.adrese || ' ' || a.pilseta AS klient,numurs AS arve, d.created AS kuupaev," +
      		" summa9 AS summa , d.kontr " +
          " FROM dokumenti d INNER JOIN adreses a ON d.san_kods=a.kods WHERE ( kontr = 'FA" + agent.getID()+ "'" +
          		" or kontr_jauns ='FA" +agent.getID() + "' or  d.piezimes like '%" +agent.getID() + "%') AND " +
            " d.created > current_timestamp-1 AND d.tips='-'  ORDER BY d.created desc ";

      String sql5 =  "SELECT a.nosauk  || ' ' || a.adrese || ' ' || a.pilseta AS klient,numurs AS arve, d.created AS kuupaev," +
		" summa9 AS summa , d.kontr " +
    " FROM dokumenti d INNER JOIN adreses a ON d.nos_kods=a.kods WHERE   ( kontr = 'FA" + agent.getID()+ "' or " +
    		"kontr_jauns ='FA" +agent.getID() + "' or  d.piezimes like '%" +agent.getID() + "%') AND " +
      " d.created > current_timestamp-1 AND d.tips='+'  ORDER BY d.created desc ";

      
      
      System.out.println("siin");
        StringBuffer sb = new StringBuffer();
        String header = " Sanemtie pasutijumi :\n\n";
        String header2 = "  Atgriezt�s pre�u pavadz�mes :\n\n";
        boolean first = true;
        boolean second = true;
          try {
            Connection con2 = getNewCon();
                   Statement st = con2.createStatement();
                  ResultSet rs2 = st.executeQuery(sql2);
                      while (rs2.next()) {
                      	if(first){
                      		sb.append(header);
                      		first = false;
                      	}

                      sb.append(rs2.getString("klient")  +
                      		"\n    (" + rs2.getString("arve").trim() + ")[" + rs2.getString("kuupaev") + "] " +
                      		getParent().getNF().format(rs2.getDouble("summa"))
                      	+" EUR "	+  "\n\n");
                      }
                       rs2.close();
                       


                     ResultSet rs3 = st.executeQuery(sql3);
                         while (rs3.next()) {
                         	if(second){
                         		sb.append(header2);
                         		second = false;
                         	}

                         sb.append(rs3.getString("klient")  +
                         		"\n    (" + rs3.getString("arve").trim() + ")[" + rs3.getString("kuupaev") + "] " +
                         		getParent().getNF().format(rs3.getDouble("summa"))
                         	+" EUR "	+  "\n\n");
                         }
                         rs3.close();

                      } catch (Exception e) {
                          getParent().showError(e);
                      }
                      
                      
                    
          
          
          
          
          
          String sqlunsold =  "select  klienta_nosauk as knimi , dok_number as doknr, dok_datums as kuup "
          		+ " , artikuls as toode , preces_nosauk  as tnimi , PASUT_DAUDZUMS-DAUDZUMS as kogus "+ 
" from ORDER_STATISTIC( current_timestamp-3, current_timestamp)  where PASUT_DAUDZUMS>DAUDZUMS and  agents ='" +agent.getID() + "'" ;

        	      
        	      
        	      System.out.println("siin");
        	       
        	         header = " Missing orders  :\n\n";
        	         first = true;
        	         second = true;
        	          try {
        	            Connection conl = this.getNewCon();
        	                   Statement st = conl.createStatement();
        	                  ResultSet rs2 = st.executeQuery(sqlunsold);
        	                      while (rs2.next()) {
        	                      	if(first){
        	                      		sb.append(header);
        	                      		first = false;
        	                      	}

        	                      sb.append(rs2.getString("knimi").trim()  +
        	                      		"\n    (" + rs2.getString("doknr").trim() + ")[" + rs2.getString("kuup").substring(0, 10)
        	                      		+ "] {" +  rs2.getDouble("kogus") + "} " + rs2.getString("toode").trim() + " " + 
        	                      		rs2.getString("tnimi") + "\n\n");
        	                      }
        	                       rs2.close();
          
        	          } catch (Exception e) {
                          getParent().showError(e);
                      }
                      
                      
                      
                      
                      
          System.out.println(agent.getID() + sb.toString());

         if(sb.toString().trim().length()>0){
          return sb.toString();
         }else {
         	return null;
         }

    }


    public String getNonMobileOrders(Agent agent){
      return null;
    }

    
    
    
    
    public void setBulkSales(Map agents){
      //System.out.println("bulkSales");
      String cmd = "SELECT kontr AS agent, sum (summa7) AS sales  FROM  " +
"dokumenti dok WHERE  extract(month from datums)= extract(month from current_timestamp) AND " +
"extract(year from datums)= extract(year from current_timestamp) AND funk=2 and kontr !='' " +
"and apst='+' and pasut='-' and rezerv='-' AND " +
"kontr is not null GROUP BY kontr " +
"UNION ALL SELECT kontr AS agent,- sum (summa7) AS sales " +
" FROM  dokumenti dok WHERE  extract(month from datums)= extract(month from current_timestamp)" +
"  AND extract(year from datums)= extract(year from current_timestamp) AND funk=7 and kontr !='' and apst='+' and pasut='-' and rezerv='-' AND " +
"kontr is not null GROUP BY kontr ";
        log(cmd);

        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            ResultSet	      rs = stmt.executeQuery(cmd);
            while(rs.next()) {
              String agentID = rs.getString("agent");
              Agent agent = (Agent)agents.get(agentID);
              if (agent != null){
            	  int sales = (int)rs.getDouble("sales");
                agent.setMonthSales(sales);
                agent.setStart(""+ agent.getMonthSales());
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
     * @param agentCode
     *
     * @return
     *
     * @see
     */
    public double getSalary(String agentCode) {
              return 0;
    }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
    }


    

    public String getSaveStockInfo(){

    	  String  cmd = " SELECT artikuls AS toode, atlrez AS stock from pdastocksshell " ;


	       if (isDebug()) System.out.println(cmd);
	       log(cmd);
	       StringBuffer products = new StringBuffer();
	       try {

	          Connection con = getNewCon();
	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmd);

	         while (rs.next()) {

	           String kood = rs.getString("toode").trim();
	         //  String dealer = rs.getString("LADU").trim();
	           double seis = rs.getDouble("stock");
	           if(seis > 0){
	               products.append(kood +  "; ;");
	               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); 
	             }
	           }
	           rs.close();
	        } catch (Exception e) {
	           getParent().showError(e);
	       }
	       return products.toString();
	   }





	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
       }

	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
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

    public static final String DRIVER_VERSION = "SolcraftShell";
    private String url = "jdbc:odbc:materiali";
    private Connection ddd ;
    private Connection bbb ;
    private   HashMap logMap = new HashMap();
    private Properties locProps = null;
    private String             url2 =  "jdbc:odbc:materiali";
    //"jdbc:odbc:report";
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();



	@Override
	public Vector getPriceMatrixData() {
		  Vector lines = new Vector();
          String sql = "  SELECT ' ' AS custid, ale.nosaukums || '' || ale.papild_nosauk2 AS clgroup,  " +
				 "   pr.artikuls   AS prodid, pr.artikuls  AS prodgroup,  " +
				"	 tt.atlaide AS discount,   tt.spec_cena AS price ,tips ,cast(tt.PALM_AKCIJAS_ATLAIDE as integer)  as ale,tt.NO_DATUMA1 as algus, " +
				" tt.NO_datuma2 as keskel ,tt.LIDZ_DATUMAM1 as lopp ,  PALM_AKCIJAS_ATLAIDE2 as lisaale ,"
				+ " SPEC_CENA2 as lisahind ,COMM as lisainff ,ACTION_MINIMUM_AMOUNT as minAm " +
				" FROM atlaizhu_grupas ale inner join atlaizhu_grupas_saturs abi on ale.id=abi.atlgrupas_id  " +
				 "    inner join atlaides_saturs tt on tt.id=abi.id  " +
				"	 inner join preces  pr on pr.kods= tt.kods  " +
				"     where   tt.spec_cena > 0 AND tt.no_datuma1<=current_timestamp and current_timestamp<=tt.lidz_datumam1 + 1  " +
				 "    order by  ale.nosaukums , ale.papild_nosauk2, pr.nosauk2";
         String sql2 = " SELECT pkonts AS custid, '' AS clgroup, pr.artikuls   AS prodid, \r\n" + 
         		"				  pr.artikuls  AS prodgroup, tt.atlaide AS discount,   tt.cena AS price ,tt.tips,cast(tt.PALM_AKCIJAS_ATLAIDE as integer)  as ale,\r\n" + 
         		"				 tt.NO_datuma1 as algus,tt.NO_datuma2 as keskel ,tt.LIDZ_DATUMAM1 as lopp ,ACTION_MINIMUM_AMOUNT as minAm FROM atlaides_saturs tt \r\n" + 
         		"					 INNER JOIN klientu_atlaides aa on aa.id= tt.id  inner join preces  pr on pr.kods= tt.kods \r\n" + 
         		"					 INNEr JOIN adreses adr on adr.kods=aa.kl_kods  where  fn_atrim(pr.grupa) like '08%' \r\n" + 
         		"				     order by pkonts,  pr.nosauk2";
          try {
           NumberFormat nf = NumberFormat.getInstance();
          	nf.setMaximumFractionDigits(2);
          	 NumberFormat nf2 = NumberFormat.getInstance();
           	nf2.setMaximumFractionDigits(0);


        	Statement stmt = this.getNewCon().createStatement();
        	Statement stmt2 = this.getNewCon().createStatement();
                  ResultSet rs = stmt.executeQuery(sql);

                  while (rs.next()) {

                    
                          String custID = rs.getString("custid");
                          String clGroup = rs.getString("clgroup");
                          String prodID = rs.getString("prodid");
                          String prodGroup = rs.getString("prodgroup");
                        //  double discount = rs.getDouble("discount");
                          double price = rs.getDouble("price");
                          String tips = rs.getString("tips").trim();
                          String ale = rs.getString("ale");
          				String sdate =  rs.getString("algus");
          				String kdate = rs.getString("keskel");
          				String tdate =  rs.getString("lopp");
                          String lisaale = rs.getString("lisaale");
                          String lisahind = rs.getString("lisahind");
                          String lisacomm = rs.getString("lisainff");
                          double minAm = rs.getDouble("minAm");
                          
                          
                          		

                          MatrixLine mline = new MatrixLine();
                          String add1= "";
                          if(minAm > 0 ){
                          	mline.setMinAm(minAm);
                          	add1 = "ma:"+(int)minAm + " ";
                          	 mline.setLineinfo( add1 );
                          }
                          
                          if(lisahind != null && lisahind.trim().length() >0 ){
                          lisahind = 	lisahind.trim();
                          try{
                          	 double dd = Double.parseDouble(lisahind);
                          	 mline.setLineinfo( add1 + nf.format(dd)  + " eur." );
                          }catch(Exception t){
                          	t.printStackTrace();
                          }
                          	
                          	
                          }
                         
                          if(lisaale != null && lisaale.trim().length() >0 && !lisaale.equals("0") ){
                          	try{
                          	//	System.out.println("asi " + lisaale + " " + ((int)((Double.parseDouble(lisaale)+0.499)))  );
                              	mline.setLineinfo(mline.getLineinfo() +  " " + ((int)((Double.parseDouble(lisaale)+ 0.499)))  + "%" );
                          	}catch(Exception r){
                          		r.printStackTrace();
                          	}
                          	
                          }
                          
                          if(lisacomm != null && lisacomm.trim().length() >0 ){
                          	mline.setLineinfo(mline.getLineinfo() +  " " + Converter.enCodeEst(lisacomm.trim()));
                          }
                         
                          
                          if (custID != null && custID.trim().length() > 0) {
                                  mline.setCustID(custID.trim());
                          }
                          if (mline.getCustID() == null && clGroup != null
                                          && clGroup.trim().length() > 0) {
                                  mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
                          }

                          if (prodID != null && prodID.trim().length() > 0 && tips.equals("3")) {
                                  mline.setProdID(prodID.trim());
                          }
                          if (mline.getProdID() == null && prodGroup != null
                                          && prodGroup.trim().length() > 0 && tips.equals("2")) {
                                  mline.setProdGroup(prodGroup);
                          }
                         // mline.setDiscount(discount);

                          if (price != 0) {
                                  mline.setPrice(price);
                          }
                          String algus = "";
                          if(ale != null && ale.trim().length() != 0 ){
                          	algus =  ale + "% ";
                          }
                          
                          if(sdate != null && sdate.trim().length() != 0){
                          	algus = algus  + "[" +  sdate.substring(0,10) +  "] ";
                          }
                          
                          if(kdate != null && kdate.trim().length() != 0){
                          	algus = algus + kdate.substring(0,10);
                          }
                          
                          if(!algus.equals("")){
                          mline.setStartDate(algus);	
                          }
                      
          			
          				 if(tdate != null && tdate.trim().length() != 0){
                             mline.setEndDate(tdate.substring(0,10));
                           }
                          lines.add(mline);
                  }
                  
                  stmt.close();
                  System.out.println("Teen Gruppidele Matixit " +lines.size());
                  
                  
                  ResultSet rs2 = stmt2.executeQuery(sql2);

                  while (rs2.next()) {
                          String custID = rs2.getString("custid");
                          String clGroup = rs2.getString("clgroup");
                          String prodID = rs2.getString("prodid");
                          String prodGroup = rs2.getString("prodgroup");
                        //  double discount = rs2.getDouble("discount");
                          double price = rs2.getDouble("price");
                          String tips = rs2.getString("tips").trim();
                          String ale = rs2.getString("ale");
          				String sdate =  rs2.getString("algus");
          				String kdate = rs2.getString("keskel");
          				String tdate =  rs2.getString("lopp");
          				 double minAm = rs2.getDouble("minAm");
                  		
                          MatrixLine mline = new MatrixLine();
                          String add1= "";
                          if(minAm > 0 ){
                          	mline.setMinAm(minAm);
                          	add1 = "ma:"+(int)minAm;
                          	 mline.setLineinfo(  add1 );
                          }
                          

                          if (custID != null && custID.trim().length() > 0) {
                                  mline.setCustID(custID.trim());
                          }
                          if (mline.getCustID() == null && clGroup != null
                                          && clGroup.trim().length() > 0) {
                                  mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
                          }

                          if (prodID != null && prodID.trim().length() > 0 && tips.equals("3")) {
                                  mline.setProdID(prodID.trim());
                          }
                          if (mline.getProdID() == null && prodGroup != null
                                          && prodGroup.trim().length() > 0 && tips.equals("2")) {
                                  mline.setProdGroup(prodGroup);
                          }
                         // mline.setDiscount(discount);

                          if (price != 0) {
                                  mline.setPrice(price);

                          }
                          String algus = "";
                          if(ale != null && ale.trim().length() != 0){
                          	algus = ale + "% ";
                          }
                          
                          if(sdate != null && sdate.trim().length() != 0){
                          	algus = algus + " [" +sdate.substring(0,10) +  "] " ;
                          }
                          
                          if(kdate != null && kdate.trim().length() != 0){
                          	algus = algus + kdate.substring(0,10) + " " + add1;
                          }
                          if(!algus.equals("")){
                         
                         // mline.setStartDate(algus);	
                          }
                      
          			
          				 if(tdate != null && tdate.trim().length() != 0){
                             mline.setEndDate(tdate.substring(0,10));
                           }
                          
                          lines.add(mline);
                  }

                  System.out.println("Teen kLientidele Matixit " + lines.size());

          } catch (SQLException ex) {
          ex.printStackTrace();
          }
//          getPriceMatrixDataLog(lines);
          return lines;
	
	}
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

