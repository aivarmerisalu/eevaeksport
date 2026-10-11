
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
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
public class SolcraftFlowerZaleDBDriver extends AbstractDBDriver implements Discounter, Manager,Promo, BulkUpdater{

 
    public SolcraftFlowerZaleDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
        try {
            Class.forName(databaseDriverName).newInstance();
        
        } catch (Exception e) {
            parent.showError(e);
        }
    }
    
    private Connection getNewCon() throws SQLException{
    	try{
    		 return DriverManager.getConnection(url, "SYSDBA", "masterkey");		
    	}catch(SQLException e){
    		 return DriverManager.getConnection(url, "SYSDBA", "masterkey");
    	}
     
    }
    
    private Connection getNewCon2() throws SQLException{
    	try{
    		 return DriverManager.getConnection(url2, "SYSDBA", "masterkey");		
    	}catch(SQLException e){
    		 return DriverManager.getConnection(url2, "SYSDBA", "masterkey");
    	}
     
    }

    public String getSaveDebtInfo() {
    	
    	 makePictureCat();
    	 System.exit(0);
       	return null;
       }
    
    private void makePictureCat(){
	     
        String cmd = "SELECT pr.artikuls AS toode  FROM preces  " +
     	"		  pr INNER JOIN grupas g ON g.grupa=pr.grupa   " +
          "        where (pr.act='+' and pr.nosauk2 >'') OR  (g.grupa='50' and pr.nosauk2 >'') or g.grupa='041' " +
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
            return lines;
    }
        
        
   
        public Vector getPriceMatrixData() {
        	 Vector lines = new Vector();
        	 String sql = "   SELECT  1 AS clgroup, pr.artikuls   AS prodid,   cena1 AS price  FROM  preces  pr  where cena1 > 0 " +
			" union  SELECT  2 AS clgroup, pr.artikuls   AS prodid,   cena2 AS price  FROM  preces  pr  where cena2 > 0 " +
			" union  SELECT  3 AS clgroup, pr.artikuls   AS prodid,   cena3 AS price  FROM  preces  pr  where cena3 > 0 " +
			" union  SELECT  4 AS clgroup, pr.artikuls   AS prodid,   cena4 AS price  FROM  preces  pr  where cena4 > 0 " +
			" union  SELECT  5 AS clgroup, pr.artikuls   AS prodid,   cena5 AS price  FROM  preces  pr  where cena5 > 0 " +
			" union  SELECT  6 AS clgroup, pr.artikuls   AS prodid,   cena6 AS price  FROM  preces  pr  where cena6 > 0 " +
			" union  SELECT  7 AS clgroup, pr.artikuls   AS prodid,   cena7 AS price  FROM  preces  pr  where cena7 > 0 " +
			" union  SELECT  8 AS clgroup, pr.artikuls   AS prodid,   cena8 AS price  FROM  preces  pr  where cena8 > 0 " +
			" union SELECT  9 AS clgroup, pr.artikuls   AS prodid,   cena9 AS price  FROM  preces  pr  where cena9 > 0 " +
			" union  SELECT  10 AS clgroup, pr.artikuls   AS prodid,   cena10 AS price  FROM  preces  pr  where cena10 > 0 " +
			" union  SELECT  11 AS clgroup, pr.artikuls   AS prodid,   cena11 AS price  FROM  preces  pr  where cena11 > 0 " +
    		" union  SELECT  12 AS clgroup, pr.artikuls   AS prodid,   cena12 AS price  FROM  preces  pr  where cena12 > 0";
               /*
                try {

              	Statement stmt = this.getNewCon().createStatement();
              	        ResultSet rs = stmt.executeQuery(sql);

                        while (rs.next()) {

                                String clGroup = rs.getString("clgroup");
                                String prodID = rs.getString("prodid");
                                double price = rs.getDouble("price");
                               MatrixLine mline = new MatrixLine();

                                if (mline.getCustID() == null && clGroup != null
                                                && clGroup.trim().length() > 0) {
                                        mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
                                }

                                if (prodID != null && prodID.trim().length() > 0 ) {
                                        mline.setProdID(prodID.trim());
                                }

                                if (price != 0) {
                                        mline.setPrice(price);
                                }

                               if(prodlist.contains(prodID)){
                            	   lines.add(mline);
                               }
                        }
                        
                        stmt.close();
                        System.out.println("Teen Gruppidele Matixit " +lines.size());
               
                } catch (SQLException ex) {
                ex.printStackTrace();
                }*/
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


 /*   public TreeMap getProdGroupsSecond() throws SQLException {
    	 System.out.println("tootegrupidSEC alustan ");
      Connection con = getNewCon();
    TreeMap   tm = new TreeMap();
    String    sql = "SELECT palm_descr AS nimetus2 from grupas ORDER by nosauk  " ;


    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
    	String nimetus2 =  rs.getString("nimetus2");
    	if(nimetus2 != null && nimetus2.indexOf(" ") != -1 ){
    		nimetus2 = nimetus2.trim();
    		String aaaa = nimetus2.substring(0,nimetus2.indexOf(" "));
    		System.out.println(aaaa + "-" + nimetus2 );
    		if(aaaa.indexOf(".") != -1){	
        		aaaa = aaaa.substring(0,aaaa.indexOf(".")) +  aaaa.substring(aaaa.indexOf(".")+1);
           		 tm.put(aaaa,  nimetus2.trim());
           	}		
    		}
    	
    	}
    	
       
    
    rs.close();
    st.close();
    con.close();
    
    System.out.println("tootegrupidSEC ending ");
    getLogProdGroupsSecond(tm);
    return tm;
    }
*/
    
    public TreeMap getProdGroups() throws SQLException {
      Connection con = getNewCon();
      System.out.println("tootegrupid alustan ");
      //setTreeGroup(true);
    TreeMap   tm = new TreeMap();
    String    sql = " SELECT grupa as asi, nosauk AS nimetus2  from grupas  where  grupa like '02%'  " ;


    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
    	String asi =  rs.getString("asi").trim();
    	String nimetus2 =  rs.getString("nimetus2");
    	if(nimetus2 != null && asi.length() > 0){
    	 	 tm.put(asi, Converter.enCodeEst(nimetus2.trim()));
    	   }else if (nimetus2 == null){
    			 tm.put(asi, Converter.enCodeEst(asi));
    	   }
       	    }
  
    rs.close();
    st.close();
    con.close();
    System.out.println("tootegrupid ending " );
     return tm;
    }


        public String getDistribution() {
          return null;
        }



    public TreeMap getAgents() {
      String  cmd = "select num AS kood , nosauk || ' ' ||num as nimi  from kontr  " +
      	"	where nosauk != ''  ORDER BY num";
        TreeMap agents = new TreeMap();
        
        try {
        	
			Properties props = new Properties();
			try {
				props = EksportFrame.loadProps("emails.txt");

			} catch (Exception e) {
				e.printStackTrace();
			}

        	
          Connection con = getNewCon();
            Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);
            System.out.println("Agents Starting ");
            while (rs.next()) {
              Agent agent = new Agent(rs.getString("kood").trim());
              String nimi = rs.getString("nimi").trim();
              agent.setName(nimi);
              agent.setIsPilot(true);
              agent.setLanguage("LAT");
              agent.setCountry("lv");
              agent.setPassWord("0000");
              agent.setActive(true);
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
        agents.put("HOUSE", hh);
        System.out.println("Agents Ready ");
        return agents;
    }



    private String getDir(){
      String dir = getParent().getProps().getProperty("readin");
      if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
      return dir;
    }



    public TreeMap getCustomers(Agent agent){

      StringBuffer buf = new StringBuffer();

   
        
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        HashMap custMap2 = new HashMap();
        
        
        try {
        	
            Connection con = getNewCon2();

            Statement stmt = con.createStatement();
              	
try{
     
          
          String sql3 = " SELECT  PKONTS , KL_KODS, SALDO, CREDIT, C_BLOCK, DEBT, AL_DEBT, D_BLOCK, R_CODE FROM  F_CUST_CREDITLIST ";
          System.out.println(sql3);
             ResultSet rst = stmt.executeQuery(sql3);
             while(rst.next()){
             	String custom = rst.getString("PKONTS");
             	Customer cc = new Customer(custom.trim());
             	
             	
             	  double saldo = rst.getDouble("SALDO");
                if(saldo != 0){
                	cc.setUsedCredit(saldo);
                }
                double credit = rst.getDouble("CREDIT");
                if(credit != 0){
                	cc.setCredit(credit+"");
                }
             		boolean cblock = false;
             	String C_BLOCK = rst.getString("C_BLOCK");
             	 if(C_BLOCK.equals("+")){
             		 //siis yletamisel ei luba myya
             		cblock = true;
             	 }
             	 
                 double debt = rst.getDouble("DEBT");
                 if(debt != 0){
                 	cc.setDebt(debt);
                 }
     double adebt = rst.getDouble("AL_DEBT");
             	boolean dblock = false;
                	String D_BLOCK = rst.getString("D_BLOCK");
                	 if(D_BLOCK.equals("+")){
                		 //siis kuup�evade yletamisel ei luba myya
                		dblock = true;
                	 }
                	 int status = rst.getInt("R_CODE");
                	 if(status == 1){
                		 cc.setSalesBlock(true);
                	 }
                	if(status == 2){
               		 cc.setSalesBlock(true);
               	 }
                	
                	
                	if(dblock && debt > adebt){
                		 cc.setSalesBlock(true);
                	}
                	
                	if(cblock && saldo-adebt > credit){
               		 cc.setSalesBlock(true);
               	}
                	
                	custMap.put(cc.getID(), cc);
             }
             rst.close();
             stmt.close();
              con.close();
    
} catch (Exception ee) {
    getParent().showError(ee);
}
         
String cmd = " Select a.nosauk AS nimi , KODS AS payer, pkonts  AS kood, ka.id as id ,ka.kl_fizadrese  AS aadress,0 AS discount,'' AS visit, " + 
	"	persona  || ' ' || pers_talr  AS persinfo ,'' as info,ka.agenta_id AS agent ,a.kons_dienas AS makse	 " +
	"	 , a.ATLAIDE as ale , a.CENA as hinnak from  adreses a  left join klientu_adreses ka on a.kods=ka.kl_kods " +
	"	  where a.nosauk is not null and  (ka.agenta_id) != ''";


     log(cmd);
              con = getNewCon();
              stmt = con.createStatement();
          
          ResultSet	      rs = stmt.executeQuery(cmd);
          
            while (rs.next()) {
          
            	String nimi = rs.getString("nimi").trim();
            	nimi = nimi.replaceAll("FINANCE", "Fin.");
            	nimi = nimi.replaceAll("FINNANCE", "Fin.");
            	nimi = nimi.replaceAll("LIMITED", "Lim.");
            	nimi = nimi.replaceAll("SIDEPARK", "Sidep.");
            	nimi = Converter.firstToUpperCase(nimi);
                if(!nimi.endsWith("#")){
                	String payer = rs.getString("payer");
                	String kood = rs.getString("kood");
                	String id = rs.getString("id");
                	if(kood != null){
                	
                Customer cust = new Customer(kood.trim()+ "_" + id);
                         cust.setPayer(kood.trim());
                String aadress = rs.getString("aadress");
                if(aadress != null && aadress.trim().length()>0){
                    cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress.trim()),"Latvia"));
                    nimi = nimi + " " +aadress.trim();
                }
               // System.out.println("nimi2 " + nimi);
                cust.setName(Converter.enCodeEst(nimi));

                String ale = rs.getString("discount");
                if (ale != null && ale.trim().length()>0){
                  cust.setDiscount(Double.parseDouble(ale.trim()));
                }

                String visit=rs.getString("visit");
                if (visit!=null && visit.trim().length()>0){
              //    cust.setVisit(visit.trim().toUpperCase());
                }
                
          String info = rs.getString("persinfo");
                if(info != null && info.trim().length()>0){
                	cust.setPersonalData(Converter.enCodeEst(info.trim()).replaceAll("\\r\\n|\\r|\\n", " "));
                }
                info = rs.getString("info");
                if(info != null && info.trim().length()>0){
                	cust.setCity(Converter.enCodeEst(info.trim()));
                }

                String agentc = rs.getString("agent");

              if(agentc != null){
            	  
            	  Enumeration enums = getParent().getAgentsHash().elements();
					while (enums.hasMoreElements()) {
						Agent a = (Agent) enums.nextElement();
						cust.addAgent(a);
					}
            		cust.addAgent(new Agent(agentc.trim()));
            
              }
                
                                


      /*   String payer = rs.getString("payer");
         if (payer != null && payer.trim().length()>0){
            cust.setPayer(payer.trim());
         }*/

         String makset = rs.getString("makse");
         if (makset != null && makset.trim().length()>0){
         	if(makset.trim().equals("0")){
         		makset = "-1";
         	}
            cust.setPaymTerm(makset.trim());
         }
     

         double aleke = rs.getDouble("ale");
         if(aleke > 0 ){
        	 cust.setDiscount(aleke);
         }
         
         
         String alegr = rs.getString("hinnak");
         if(alegr != null && alegr.trim().length()>0){
         	cust.setGroup(Converter.cleanLitLatEst(alegr.trim()));
         }

         
         if(custMap.get(cust.getPayer()) != null){
	 Customer cc = (Customer)custMap.get(cust.getPayer());
        	 cust.setDebt(cc.getDebt());
        	 cust.setUsedCredit(cc.getUsedCredit());
        	 cust.setCredit(cc.getCredit());
        	 cust.setSalesBlock(cc.getSalesBlock());; 
         }
         
         
         if(custMap.get(cust.getID()) != null){
        	 Customer cc = (Customer)custMap.get(cust.getID());
                	 cust.setDebt(cc.getDebt());
                	 cust.setUsedCredit(cc.getUsedCredit());
                	 cust.setCredit(cc.getCredit());
                	 cust.setSalesBlock(cc.getSalesBlock());; 
                 }
            customers.put(cust.getName(), cust);
             
              custMap2.put(cust.getID(), cust);
          	
                	}
              }
            }

            rs.close();
            stmt.close();
               con.close();
               
        } catch (Exception e) {
            getParent().showError(e);
        }
        System.out.println("kliendid l�petan ");
        return customers;
    }


    
    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      System.out.println("debt alustan ");
      String sql = " SELECT PKONTS, DOK_NR, DOK_DATE, DUE_DATE, AMOUNT, OVER_DUE FROM  F_CUST_BILLLIST ";
      Connection con = getNewCon2();
      Statement stmt = con.createStatement();

      ResultSet rs = stmt.executeQuery(sql);

      while(rs.next()){
        Debt debt = new Debt();
        String klient = rs.getString("PKONTS");
        if(klient != null){
        	   debt.setCustID(klient.trim());
               debt.setDoknr(rs.getString("DOK_NR").trim());
               debt.setDate(rs.getDate("DOK_DATE"));
               Date dd = rs.getDate("DUE_DATE");
               if (dd == null){
            	   dd = new Date();
               }
               debt.setDuedate(dd);
           

               debt.setSum(Double.parseDouble(rs.getString("AMOUNT").trim()));
               String aa = rs.getString("OVER_DUE");
               if(aa == null ||aa.trim().length() == 0){
            	   aa = "0";
               }else{
            	   if(aa.startsWith("-")){
            		   aa = aa.substring(1);
            	   }else{
            		  aa = "-" +aa;
            	   }
               }
               debt.setDue(aa);
               if(debt.getSum() > 0.05 ||  -0.05 > debt.getSum()  )
               list.addElement(debt); 	
        }
     
      }
       rs.close();
       stmt.close();
       con.close();
       System.out.println("debt l�petan  " + list.size());
     
       return list;
    }

    public Vector getCustomerProductsList2() {
        // return new Vector();
       	Calendar cal = Calendar.getInstance();
       	cal.add(Calendar.MONTH, -1);
   String date = Converter.getYYYYmMMmDDmString(cal.getTime());
       	String sql1 = " select a.pkonts  AS klient, adrese_id  as id , pr.artikuls AS toode,daudzums AS kogus, datums AS kuupaev, " +
         	"	iep_cena as lastprice ,dok.numurs as lastinv from dokumenti dok inner join dok_spec rid on rid.dok_kods=dok.kods " +
   		"   INNER JOIN adreses a ON a.kods=dok.san_kods INNER JOIN preces pr ON pr.kods=rid.preces_kods where " +
   		"   datums > '" + date  + "'  AND funk=2 and daudzums > 0  GROUP BY pr.artikuls,a.pkonts, adrese_id,daudzums,datums,iep_cena "
   		+ ",dok.numurs  " +
   		"   ORDER BY a.pkonts,pr.artikuls,datums,daudzums " ;
           if (isDebug()) {
               System.out.println(sql1);
           }
           Vector custProds = new Vector();

           try {
             Connection con = getNewCon();
             PreparedStatement stmt = con.prepareStatement(sql1);
             ResultSet	      rs = stmt.executeQuery();

             HashMap kliendid = new HashMap();
             while (rs.next()) {
               String kliendikood = rs.getString("klient").trim();
               String id =rs.getString("id");
               if(id != null && id.trim().length() > 0){
               	kliendikood = kliendikood + "_" +id.trim();
               }
               String tootekood = rs.getString("toode").trim();
               double kogus = rs.getDouble("kogus");
               double lprice = rs.getDouble("lastprice");
               String dokum = rs.getString("lastinv").trim();
               Customer cust = (Customer)kliendid.get(kliendikood);
               
               if (cust == null){
                 cust = new Customer(kliendikood);
               }
               
               if(!sold.contains(tootekood)){
               	sold.add(tootekood);
               }
               
              Product prod = new Product(tootekood);
                 CustProd cp = new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                              rs.getString("kuupaev").substring(5,10), lprice, 0, 0, " ");
                 cp.setAbiKood(dokum.trim());
              	if(!tootekood.startsWith("PR")){
              	    cust.addProduct(prod,cp,false);
          	}
             
                 kliendid.put(kliendikood, cust);
                 custProds.add(cp);
             }

             rs.close();
             stmt.close();
             con.close();
           } catch (Exception e) {
             getParent().showError(e);
           }
           connectionDown();
         
           return custProds;
       }

   
    HashSet prodlist = new HashSet();
    
    public TreeMap getProducts(Agent agent){
    	  prlist = getCustomerProductsList2();
      String cmd = "SELECT pr.artikuls AS toode,pr.nosauk AS nimi,pr.cena1 AS myygihind, 21 AS VAT,pr.mervien AS unit,pr.minpas AS " +
      		" tykke ,pr.grupa AS grupp , pr.artikuls AS ean, '' as phid " +
			" FROM  PRECES pr  WHERE  (pr.grupa like '02%' or pr.grupa like '010%') and pr.nosauk is not null ";
      
      System.out.println(" tooted alustan ");

        if (isDebug()) {
            System.out.println(cmd);
        }

        TreeMap products = new TreeMap();
        try {
        	 Connection con = null;
        	try{
        		con = getNewCon();
                		
        	}catch (Exception r){
        		con = getNewCon();
                
        	}
        	
        	TreeMap<String, Double> seisud = new TreeMap<String ,Double>();
        	
          Statement st = con.createStatement();
            
          String  sql2 = " select t.artikuls as toode, sum(s.atlikums) as stock " +
 	 	"	from fwm_atlikumi s INNER JOIN preces t on s.preces_kods=t.kods where  grupa like '02%' or grupa like '010' group by t.artikuls " ;

          ResultSet rs = st.executeQuery(sql2);
          while(rs.next()){
            String id = rs.getString("toode").trim();
            
            double stock = rs.getDouble("stock");
          	if(stock > 0){
          		seisud.put(id, stock);
            }
          
          }
          rs.close();
          
          
              rs = st.executeQuery(cmd);
            HashMap prodHash = new HashMap();
            while (rs.next()) {
                String kood = rs.getString("toode").trim();
                if(kood.length() >2){
                boolean goes = false;
                String nimetus =   rs.getString("nimi");
                if(nimetus == null){
                	nimetus = kood;
                }
                	nimetus  = nimetus.trim().replaceAll("\n", " ")  + " " + kood;
                Product prod = new Product(kood);
                prod.setPrice(rs.getDouble("myygihind"));
                prod.setVat(rs.getDouble("VAT") + "");
                String unit = rs.getString("unit");
                if(unit != null && unit.trim().length()>0){
                	prod.setUnit(unit.trim());
                }
  
  if (seisud.get(kood)!= null){
	  goes = true ;
	  prod.setStock(seisud.get(kood));
  }
  
  if(sold.contains(kood)){
	  
	  goes = true;
  }
                prod.setInCases(rs.getInt("tykke"));
                prod.setOptAmountInUnits(prod.getInCases());
             //   prod.setStock(-1);
                String grupp = rs.getString("grupp");
                if(grupp != null && grupp.trim().length()>0){
                	prod.setGroup(grupp.trim());
                }
                	  
                prod.setName(Converter.enCodeEst(nimetus));
                String phid  = rs.getString("phid");
                
                if(phid != null && phid.trim().length()>0){
                	 //  prod.setPhotoid("phurl:http://img20.match-online.nl/FotoID/" + phid + ".jpg");
                }
                
              
                String ean = rs.getString("ean");
                if(ean != null && ean.trim().length()>0){
                	prod.setEAN(ean.trim());
                }
               
                if(goes){
                	products.put(nimetus, prod);
                    prodHash.put(prod.getID(), prod);
                    prodlist.add(prod.getID());
                }
                	 
            }
            	
            }
        	
            rs.close();
            st.close();
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
    
    private Vector prlist = new Vector();
    private HashSet sold = new HashSet();
    
    public Vector getCustomerProductsList(String agentCode) {
    	return new Vector();
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
                         	+" LVL "	+  "\n\n");
                         }
                         
                         rs3.close();
                         
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
    	Calendar cal3 = Calendar.getInstance();
    	//cal3.add(Calendar.MONTH, -2);
        String date = Converter.getYYYYmMMmDDmString(cal3.getTime()).substring(0, 7);
int year = cal3.get(Calendar.YEAR);
int month = cal3.get(Calendar.MONTH);

      String cmd = " SELECT kontr AS agent, sum (summa7) AS sales  FROM  dokumenti " +
" WHERE   datums like '" + date + "%' AND funk=2 and kontr !='' " +
" and apst='+' and pasut='-' and rezerv='-' AND kontr is not null GROUP BY kontr " +
" UNION ALL SELECT kontr AS agent,- sum (summa7) AS sales " +
" FROM  dokumenti  WHERE  datums like '" + date + "%'  AND funk=7 and kontr !='' and apst='+' and pasut='-' and rezerv='-' AND " +
"  kontr is not null GROUP BY kontr  ";
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
   StringBuffer products = new StringBuffer();
    	

   	 String  cmd = " select t.artikuls as toode, sum(s.atlikums) as stock  " +
 	 		"  from fwm_atlikumi s INNER JOIN PRECES_WEB_VIEW t on s.preces_kods=t.kods  "
 	 		+ " group by t.artikuls having  sum(s.atlikums)>0 " ;


	       if (isDebug()) System.out.println(cmd);
	       log(cmd);
	       try {

	          Connection con = getNewCon();
	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmd);

	         while (rs.next()) {

	           String kood = rs.getString("toode").trim();
	         //  String dealer = rs.getString("LADU").trim();
	           double seis = rs.getDouble("stock");
	           if(seis > 0 ){
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


    public ManagerData getManagerData() throws SQLException{
      return null;
    }


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

    public static final String DRIVER_VERSION = "SolcraftFlower";
    private String url = "jdbc:odbc:solcraft32";
    private String url2 = "jdbc:odbc:finance";
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

