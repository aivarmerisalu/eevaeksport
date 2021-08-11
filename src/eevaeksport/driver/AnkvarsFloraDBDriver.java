
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
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import eevaeksport.Agent;
import eevaeksport.Contract;
import eevaeksport.ContractLine;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
//import ee.mobilejava.wrapper.SuperVisorLineWrapper;
//import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
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
public class AnkvarsFloraDBDriver extends AbstractDBDriver implements Discounter, Manager,Promo, BulkUpdater,PictureMaker{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public AnkvarsFloraDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
    }
    
   
    
    public String getSaveDebtInfo() {
    	
    	return null;
       }

    private TreeMap eans  = null; 
    
    private void makePictureCat(){
	     
    	if(eans == null) {
    		 this.getProducts(null);
    	}
   
       	
       	String picts = parent.getProps().getProperty("pict.dir");
        String pictspda = parent.getProps().getProperty("pictpda.dir");
     	  

       	try {
     	
            StringBuffer buf = new StringBuffer();
            
            File d  = new File(picts);
            for(String fname : d.list() ) {
            
            	 File in = new File ( picts + fname); 
            	 String ean = fname.substring(0,fname.indexOf(" "));
            	 
            		if(in.isFile() && eans.get(ean) != null){    
                     	 
                      	File inpda = new File ( pictspda + eans.get(ean)+ ".jpg"); 
                      	 if(!inpda.exists()){
                      		Converter.shrinkAndCopyFile(in,inpda);
                     	  }
                      
                      	
                        String len = "0";
                        if(inpda.exists()){
                          len =	inpda.length() + "";
                          } 
                        buf.append(eans.get(ean)+".jpg;"  + len + "\n");
                        }
            }
         
                   
            
          
          	
        
       	    //oleasoleva kataloogi kustutamine
       	    
                   File f = new File("list.txt");
                   if (f.exists())f.delete();
                   
                   try {
               		PrintWriter os = new PrintWriter(new FileWriter(
               				  "list.txt", true));
               		os.write(buf.toString());
               		os.close();
               	   File fff =  new File (pictspda +  "list.txt");
         	       copyFile(f,fff);  
               	
               	  }catch (Exception ex) {
               		ex.printStackTrace();
               	  }
           	  
       	} catch (Exception e) {
       	    getParent().showError(e);
       	}
       
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

    
	
    public Vector getLastThreeData() {
		return null;
	}
    
	public String getAssortmentStr(Agent agent){
   
    	return null;
      }

    public String getProductSortingData(){
       	return null;
       }

        public String getDealerCustomerCSV(String agent){
            return null;
            }
        public Vector getFixedAssortmentData(){
            return new Vector();
            }
    
        
        public Vector getPriceMatrixData()  {
            Vector lines = new Vector();
            String sss = "SELECT r.ISKLIENT AS custid, '' AS clgroup, r.ISPRECE AS prodid, '' AS prodgroup,'' AS discount, r.CENA_1 as price,"
            		+ " r.DAT_LIDZ-r.DAT_NO as paevi, r.DAT_NO as algus , r.DAT_LIDZ as lopp" + 
            	" FROM O_SPECCENAS r where r.DAT_NO < current_date-1 and r.DAT_LIDZ > current_date+1 " ;
            	
            
              Statement st = null;
              try {
            	  
                Connection con  =  getNewCon();
                st = con.createStatement();
                ResultSet rs = st.executeQuery(sss);

                while (rs.next()) {

                  String custID = rs.getString("custid");
                  String clGroup = rs.getString("clgroup");
                  String prodID = rs.getString("prodid");
                  String prodGroup = rs.getString("prodgroup");
                  String discount = rs.getString("discount");
                  double price = rs.getDouble("price");
                  int paevi = rs.getInt("paevi");
                  String algus = rs.getString("algus");
                  String lopp = rs.getString("lopp");
                  
                  MatrixLine mline = new MatrixLine();
         
               if(paevi < 90) {

                  if (lopp != null) {
    					mline.setEndDate(lopp.substring(0, 10));
    				}
    				if (algus != null) {
    					mline.setStartDate(algus.substring(0, 10));
    				}
    			  } 

                  if (custID != null && custID.trim().length() > 0) {
                    mline.setCustID(custID.trim());
                  }
                  
                  if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                    mline.setCliGroup(clGroup.trim());
                  }

                  if (prodID != null && prodID.trim().length() > 0) {
                    mline.setProdID(prodID.trim());
                  }
                  
                  if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                    mline.setProdGroup(prodGroup.trim());
                    price = 0;
                  }
                  
                  if(discount!= null && !discount.trim().equals("HI") && !discount.trim().equals("K4") && !discount.trim().equals("K5")) {
                  	try {
                  	   	//  mline.setDiscount(Double.parseDouble(discount.trim()));
                 	            		
                  	 } catch(Exception e) {
                  		e.printStackTrace();
                  	 }
                  }
                 
                  if (price != 0) {
                    mline.setPrice(price);
                  }
                  if(price > 0)
                 lines.add(mline);
                }

                rs.close();
                st.close();
                con.close();
              }
              catch (SQLException ex) {
            	  ex.printStackTrace();
              }
              
              return lines;
          }
        

                 public String  getCustProdDiscounts(Agent agent){
                         return null;
                         }


    /**
     *
     */
    public void connectionUp(){
    }

    private Connection getNewCon() throws SQLException{
      return  DriverManager.getConnection("jdbc:firebirdsql://192.168.1.122:3050/C:/ANKRAVS/DB/PRECES.GDB", "SYSDBA", "masterkey");
    }
   

    /**
     *
     */
    public void connectionDown(){
    }
    public void connectionDown2(){
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
     setTreeGroup(true);
    TreeMap   tm = new TreeMap();
    
    String    sql = "select isn as liik , GNOS as nimetus  from N_PRGRUP ";
 

    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {

    	String liik =rs.getString("liik").trim();
    	String nimi = rs.getString("nimetus").trim();
    	tm.put(liik,Converter.enCodeEst(nimi));

    }
    rs.close();
    st.close();
    con.close();

    return tm;
    }

    public TreeMap getProdGroupsSecond() throws SQLException {
    	    return null;      
    	    }


    public String getDistribution() {
          return null;
        }



    public TreeMap getAgents() {
      String  cmd = "select ISN as kood, GNOS as nimi from N_AGENTS";
        TreeMap agents = new TreeMap();

        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("kood").trim());

              String nimi = rs.getString("nimi").trim();
              agent.setName(nimi);
              agent.setIsPilot(true);
              agents.put(nimi, agent);
            }
            rs.close();
            stmt.close();
            con.close();

        } catch (Exception e) {
            getParent().showError(e);
        }
        return agents;
    }







    public TreeMap getCustomers(Agent agent){

      String cmd = "SELECT a.gnos as maknimi,b.GNOS AS nimi,a.ISN AS kood ,b.isn as kood2,b.adrese  AS aadress ,a.adrese  AS aadress2 ,0 AS discount,'' AS visit,b.TELEFONS1 AS info, \r\n" + 
      		"      		            a.ISKLAGENTS AS agent,b.ISAGENTS as agent2 , a.KONSIGN AS makse , a.cenugrupa AS grupp ,'' AS klale FROM N_KL_STRV b INNER JOIN  n_klient  a on b.isklient=a.isn where \r\n" + 
      		"      					 b.ISAGENTS > 1 and a.GNOS is not null \r\n" + 
      		"      					 ORDER BY a.GNOS";

        log(cmd);

        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        HashMap custMap2 = new HashMap();

        try {
          Connection con = getNewCon();


          Statement stmt = con.createStatement();
          
          TreeMap<String, Double> debts = new TreeMap<String, Double>();

          String sql3 = " select a.isklient  AS klient  , SUM(KOPSUMMA - (APMAKS_SUM +ATGPRECSUM)) as summa \r\n" + 
          		"      		from O_PAVOUT a where KOPSUMMA - (APMAKS_SUM +ATGPRECSUM)\r\n" + 
          		"      		 not between -0.01 and 0.01 and APM_LIDZ < current_timestamp\r\n" + 
          		"		   GROUP BY a.isklient ORDER BY a.isklient";
           System.out.println(sql3);
              ResultSet rst = stmt.executeQuery(sql3);
              while(rst.next()){
              	String custom = rst.getString("klient");
           	if (custom != null){
              

                  double debt = Double.parseDouble(rst.getString("summa").trim());
                  debts.put(custom, debt);
                  
              	}
              }
              rst.close();
          
          
    
            ResultSet	      rs = stmt.executeQuery(cmd);
           
            while (rs.next()) {
            
                String nimi = rs.getString("maknimi").trim();
                 nimi = nimi + " " + rs.getString("nimi").trim();
                 
                String custc = rs.getString("kood");
                if(custc != null){
                	 String custb = rs.getString("kood2");
                Customer cust = new Customer(custc.trim()+"-"+ custb.trim());
                cust.setPayer(custc);
                String aadress = rs.getString("aadress");
              
               if(aadress != null && aadress.trim().length()>0){
                    cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress.trim()),"Latvia"));
                    nimi = nimi + " " +aadress.trim();
                }
               // System.out.println("nimi2 " + nimi);
                cust.setName(Converter.enCodeEst(nimi));
                if(debts.get(custc) != null) {
                	cust.setDebt(debts.get(custc));
                }
             

                String visit=rs.getString("visit");
                if (visit!=null && visit.trim().length()>0){
                  cust.setVisit(visit.trim().toUpperCase());
                }

                String info = rs.getString("info");
                if(info != null && info.trim().length()>0){
                	cust.setPersonalData(Converter.enCodeEst(info.trim()));
                }

                String agentc = rs.getString("agent2");
                if(agentc != null && agentc.trim().length()>0){
                    cust.addAgent(new Agent(agentc.trim()));
                }
                



         String makset = rs.getString("makse");
         if (makset != null && makset.trim().length()>0){
         	if(makset.trim().equals("0")){
         		makset = "-1";
         	}
            cust.setPaymTerm(makset.trim());
         }

         String grupp = rs.getString("grupp");
          if(grupp != null && grupp.trim().length()>0){
            cust.setGroup(grupp.trim());
            }
              customers.put(cust.getName(), cust);
              custMap.put(cust.getPayer(), cust);
              custMap2.put(cust.getID(), cust);
                }
            }

            rs.close();
           

               con.close();


        } catch (Exception e) {
            getParent().showError(e);
        }

        return customers;
    }

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "select isklient as payer,isklstrv as klient, NR_PAVADZ as numbe , APM_LIDZ as kuupaev ,\r\n" + 
      		"KOPSUMMA - (APMAKS_SUM +ATGPRECSUM) as summa , current_timestamp - APM_LIDZ  as yle ,DATUMS as kuup ,KOPSUMMA as origsum\r\n" + 
      		" from O_PAVOUT d where KOPSUMMA - (APMAKS_SUM +ATGPRECSUM) not between -0.01 and 0.01 and datums > current_date-300";

      Connection con = getNewCon();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);

      while(rs.next()){
    	  Debt debt = new Debt();
    	  String payer = rs.getString("payer").trim();
    	  String kl =  rs.getString("klient");
    	  if(kl == null) {
    		  kl = "0";
    	  }
    	  String klient = payer + "-" + kl.trim();
	        debt.setCustID(klient);
	        debt.setPayerID(payer);

          String dok = rs.getString("numbe");
  	        if(dok != null){
  	        	 dok = dok.trim();
  	    	        debt.setDoknr(dok);   	    	        
  	    	        debt.setDuedate(rs.getDate("kuupaev"));
  	    	        debt.setSum(rs.getDouble("summa"));       
  	    	        debt.setDue(((int)rs.getDouble("yle"))+""); 
  	    	        debt.setDate(rs.getDate("kuup"));
  	    	        double origsum = rs.getDouble("origsum");
  	    	        if(origsum != debt.getSum()){
  	    	        	 debt.setTara(origsum);
  	    	        }
  	    	        list.addElement(debt);
	        }
        }
       rs.close();
       st.close();
       con.close();
       
       return list;
    }
    
    
    public TreeMap loadPrLists() {
		
		 TreeMap res = new TreeMap();
		 File dir = new File("excelprlist");
		 File[] files = dir.listFiles();
		    
		    for (int i = 0; i < files.length ; i++){
		      File file = files[i];
		      if (file.isFile()){
		
  	 try{
		 
		       FileInputStream	 ff = new FileInputStream(file);
				XSSFWorkbook workbook = new XSSFWorkbook(ff); 
				int sheets = workbook.getNumberOfSheets();
				
				for(int u =0; u < sheets; u++) {
				XSSFSheet sheet = workbook.getSheetAt(u); 
				System.out.println("hinnakirja lugemine " + u +" "+  file.getName());
				
				ff.close();	
				
				if(sheet != null){
					XSSFRow row = sheet.getRow(0);
							
					for(int ii = 1 ; ii < sheet.getLastRowNum()+1;ii++){
						 row = sheet.getRow(ii);
						 DataFormatter formatter = new DataFormatter();
							String ean = formatter.formatCellValue(row.getCell(0));
							String tnimi = formatter.formatCellValue(row.getCell(1));
							if( 0 < ean.length()) {
								res.put(ean, tnimi);
							}
						
					}
				 }
				 System.out.println("sorts size " + res.size() );
				}
				 }catch(Exception e) {
					 e.printStackTrace();
				 }	 
		      }}
		    System.out.println("sorts size " + res.toString() );
	return res;
}

    public TreeMap getProducts(Agent agent){

      String cmd = " SELECT pr.isn AS toode,pr.gnos AS nimi,isprgrupa as grnimi,pr.PARDCENA1 AS myygihind,\r\n" + 
      		"      		21 AS VAT,pr.MERVIENIBA AS unit, pr.SKKASTE AS tykke ,0 AS stock, '' AS uusgrupp,isprgrupa AS grupp \r\n" + 
      		"            , pr.artikuls AS ean  FROM N_KLPREC  pr  where CHAR_LENGTH(pr.artikuls)  > 8";

        if (isDebug()) {
            System.out.println(cmd);
        }

        eans = new TreeMap();
        TreeMap prlist = loadPrLists();
        
        TreeMap products = new TreeMap();
        try {
          Connection con = getNewCon();
          Statement st = con.createStatement();
            ResultSet	      rs = st.executeQuery(cmd);
            HashMap prodHash = new HashMap();
            while (rs.next()) {
                String kood = rs.getString("toode").trim();

                String nimetus = Converter.enCodeEst(rs.getString("nimi").trim()) ;
                Product prod = new Product(kood);
                prod.setPrice(rs.getDouble("myygihind")/1.21);
             
                //prod.setPrice(prod.getPrice()/(1+vt/100));
                String unit = rs.getString("unit");
                if(unit != null && unit.trim().length()>0){
                	prod.setUnit(unit.trim());

                }

               prod.setInCases((int)rs.getDouble("tykke"));

                prod.setStock(rs.getDouble("stock"));
                
                String grupp = rs.getString("grupp");
                if(grupp != null && grupp.trim().length()>0){
                	prod.setGroup(grupp.trim());

                               }

              boolean goes = false;

                String ean = rs.getString("ean");
                if(ean != null && ean.trim().length()>0){
                	prod.setEAN(ean.trim());
                	if(prlist.get(ean.trim()) != null) {
                		goes = true;
                		eans.put(ean, prod.getID());
                	}
                }
  
                prod.setStock(-1);
                prod.setVat("21");

             //   prod.setName(prod.getGroup()+ " " + kood + " " +  nimetus);
                prod.setName( nimetus);
                if(goes) {
                	 products.put(prod.getName(), prod);
                     prodHash.put(prod.getID(), prod);	
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
    public Vector getCustomerProductsList(String agentCode) {

      String sql1 = " select dok.isklient AS klient, dok.isklstrv AS klient2 , rid.isprece AS toode, daudzums AS kogus, \r\n" + 
      		"     rid.CENA as price, max(dok.datums) AS kuupaev  \r\n" + 
      		"   from O_PASPAV dok inner join O_PASPRC rid on rid.ispaspav=dok.isn  \r\n" + 
      		"    where   dok.datums > current_date-45\r\n" + 
      		"  GROUP BY rid.isprece,dok.isklient,dok.isklstrv,rid.daudzums,rid.CENA \r\n" + 
      		"  ORDER BY dok.isklient ,rid.isprece,max(dok.datums) desc ,rid.daudzums" ;
     
    	 	Vector custProds = new Vector();

    		 

    	        if (isDebug()) {
    	            System.out.println(sql1);
    	        }

    	        try {
    	          Connection con = this.getNewCon();
    	          PreparedStatement stmt = con.prepareStatement(sql1);
    	          ResultSet	      rs = stmt.executeQuery();

    	          HashMap kliendid = new HashMap();
    	          while (rs.next()) {
    	            String kliendikood = rs.getString("klient").trim();
    	            String kliendikood2 = rs.getString("klient2").trim();
    	            kliendikood = kliendikood + "-" + kliendikood2; 
    	          
    	         	   String tootekood = rs.getString("toode").trim();
    	         
    	                double kogus = rs.getDouble("kogus");
    	           	 double hind = rs.getDouble("Price");
    	                
    	          
    	                  CustProd cp = new CustProd(kliendikood, tootekood, kogus, 0, 0,
    	                                               rs.getString("kuupaev").substring(5,10), hind, 0, 0, " ");
    	          
    	          
    	                  custProds.add(cp);

    	            }

    	          rs.close();
    	          stmt.close();
    	          
    	          
    	        } catch (Exception e) {
    	          getParent().showError(e);
    	        }
    	        System.out.println("cp size " + custProds.size());
    	       	return custProds;
    	    }

    public String getPilotDayData(Agent agent){


      String sql2 =  "SELECT a.nosauk  || ' ' || a.adrese || ' ' || a.pilseta AS klient,numurs AS arve, d.created AS kuupaev," +
      		" summa9 AS summa , d.kontr " +
          " FROM dokumenti d INNER JOIN adreses a ON d.san_kods=a.kods WHERE  kontr = '" + agent.getID()+ "' AND " +
            " d.created > current_timestamp-1 AND d.tips='-'  ORDER BY d.created desc ";

      String sql3 =  "SELECT a.nosauk  || ' ' || a.adrese || ' ' || a.pilseta AS klient,numurs AS arve, d.created AS kuupaev," +
		" summa9 AS summa , d.kontr " +
    " FROM dokumenti d INNER JOIN adreses a ON d.nos_kods=a.kods WHERE  kontr = '" + agent.getID()+ "' AND " +
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
                      	+" LVL "	+  "\n\n");
                      }


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
      /*
      connectionUp(url);

      String sql = "SELECT RTRIM(SL01002) AS nimi, ST02009 AS number, OR20024 AS summa FROM ST020100 " +
                   "LEFT JOIN OR200100 ON  OR20001=ST02009 LEFT JOIN SL140100 k ON ST02001=SL14001 " +
                   "LEFT JOIN SL010100 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-3 AND ST02007=? " +
                  "AND LEN(RTRIM(OR20056))=0 GROUP BY ST02009,OR20024,SL01002,SL14003";

      StringBuffer sb = new StringBuffer();

        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, agent.getID());
            String sep= System.getProperty("line.separator");
            ResultSet	      rs = stmt.executeQuery();
            sb.append(getParent().getString("name") + "  \t\t" + getParent().getString("number") +
                      "\t" + getParent().getString("sum") + sep);
            sb.append("-------------------------------------------" + sep);
            while(rs.next()){
              sb.append(rs.getString("nimi").trim() + "\t");
              sb.append(rs.getString("number").trim() + "\t");
              String sum = getNF().format(Double.parseDouble(rs.getString("summa").trim())) ;
              sb.append(sum + sep);
            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        connectionDown();
        return sb.toString();*/
    }


    public void setBulkSales(Map agents) {
       
         String cmd = " select isagents as agent, sum(bezpvnsumma) as sales from O_PASPAV where EXTRACT (month from datums) =  EXTRACT (month from current_date)  and \r\n" + 
         		"		   EXTRACT (year from datums) =  EXTRACT (year from current_date)  Group by isagents ";
  
         try {
       
        	 Connection con = getNewCon();
             PreparedStatement stmt = con.prepareStatement(cmd);
             ResultSet rs = stmt.executeQuery();
           
                 while(rs.next()) {
       				String agentID = rs.getString("agent").trim();
       				Agent agent = (Agent)agents.get(agentID);
       				if (agent != null){
       					agent.setMonthSales((int)rs.getDouble("sales"));
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
      if (agentCode == null) return 0;
      String cmd = "SELECT SUM(OR20024) AS sales ,OR20019 AS agent FROM OR200100 WHERE MONTH(OR20015)=" +
                   "MONTH(GETDATE()) AND YEAR(OR20015)=YEAR(GETDATE()) GROUP BY OR20019";
        log(cmd);

        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            ResultSet	      rs = stmt.executeQuery(cmd);

            while(rs.next()) {
              setSales(rs.getDouble("sales"));
            }
            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }

        return 0;
    }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
      /*String sql = "SELECT ST02020 AS  kogus, SC01071 AS sisu FROM ST020100 INNER JOIN " +
                   "SC010100 ON ST02017=SC01001 WHERE ST02007=?" +
                   " AND YEAR(ST02015)=YEAR(GETDATE()) AND MONTH(ST02015)=MONTH(GETDATE()) AND SC01012=?";
      PreparedStatement pst = con.prepareStatement(sql);
      pst.setString(1, agentCode);
      //Calendar cal = Calendar.getInstance();
      //cal.set(Calendar.DAY_OF_MONTH, 1);
      //pst.setTimestamp(2, new Timestamp(cal.getTime().getTime()));
      pst.setString(2, String.valueOf(nr));
      ResultSet rs = pst.executeQuery();
      double total = 0;
      while(rs.next()){
        double kogus = rs.getDouble("kogus");
        String sisu = rs.getString("sisu");
        if (!sisu.equals("")){
          total += kogus*Double.parseDouble(sisu);
        }
      }
      rs.close();
      pst.close();
     // System.out.println("nr:" + nr + ", total:" + total);
      return total;*/
    }


    public String getSaveStockInfo(){

    	  String  cmd = " SELECT artikuls AS toode,sum(atlrez) AS stock from nolik_avg('LVL', " +
             " '01.01.1999', 0, '1', '0', '0', '', '', '') group by artikuls Order by artikuls" ;

	       if (isDebug()) System.out.println(cmd);
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
	               products.append(kood +  ";");
	               products.append(" ;");
	               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
	             }
	           }
	           rs.close();
	       } catch (Exception e) {
	           getParent().showError(e);
	       }
	       return products.toString();
	   }


/*	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
       String sql = "INSERT into supervisor values ("
         + "?, "   //   agent	C(50),
         + "?,"   //   agentname	C(200),
         + "?,"   //   clientid	C(50),
         + "?,"   //   clientname	C(200),
         + "?,"   //	prodid	C(50),
         + "?,"   //   prodname	C(200),
         + "current_timestamp ,"   //	sentdate	D,
         + " '' ,"   //	receivedTime	D,
         + "?,"   //normalpr	N,
         + "?,"   // amoun	N,
         + "?,"   // grouptotal	N,
         + "?,"   // cattotal	N,
         + "?,"   // pertotcat	I,
         + "?,"   // percolcat	I,
         + "?,"   // perpalcat	I,
         + "?,"   // pershecat	I,
         + "?,"   // perrescat	I,
         + "?,"   // isaction	L,
         + "?,"   // ispartner	L,
         + "?,"   // isbrand	L,
         + "?,"   // isgroup	L
         + "?,"   // actionaeva	N,
         + "?,"   // actionbeva N,
         + "?,"   // actiontota	N,
         + "?,"   // custeval	N,
         + "?,"   // custcomme	C(200),
         + "?,"   // comment	C(200),
         + "?,"   // custagent	C(200),
         + "?,"   // custagentname	C(200),
         + "?,"   // mustbe	I,
         + "?,"   // ideal	I,
         + "?,"   // resstr1	C(200),
         + "?,"   // resstr2	C(200),
         + "?,"   // resstr3	C(200),
         + "?,"   // resint1	I,
         + "?,"   // resint2	I,
	      + "?)" ; //	resint3	L

         try {
         	System.out.println("jam");
             PreparedStatement ps = ccc.prepareStatement(sql);

             for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
               SuperVisorLineWrapper lw = new SuperVisorLineWrapper(iter.next());
               ps.setString(1, wr.getInfo().getAgentID());
               ps.setString(2, wr.getInfo().getAgentName());
               ps.setString(3, lw.getLine().getCustID());
               ps.setString(4, lw.getLine().getCustName());
               ps.setString(5, lw.getLine().getProdID());
               ps.setString(6, lw.getLine().getProdName());
               ps.setDouble(7, lw.getLine().getNormalPrice()*100);
               ps.setDouble(8, lw.getLine().getAmount()*100);
               ps.setDouble(9, 0);
               ps.setDouble(10, 0);
               ps.setInt(11, lw.getLine().getPercentageFromTotalCategory());
               ps.setInt(12, lw.getLine().getPercentageFromColdCategory());
               ps.setInt(13, lw.getLine().getPercentageFromPallCategory());
               ps.setInt(14, lw.getLine().getPercentageFromShelfCategory());
               ps.setInt(15, lw.getLine().getPercentageFromRestCategory());
               ps.setBoolean(16, lw.getLine().isAction());
               ps.setBoolean(17, lw.getLine().isPartner());
               ps.setBoolean(18, lw.getLine().isBrand());
               ps.setBoolean(19, lw.getLine().isGroup());
               ps.setDouble(20, lw.getLine().getAction1Eval()*100);
               ps.setDouble(21, lw.getLine().getAction2Eval()*100);
               ps.setDouble(22, lw.getLine().getActionTotalEval()*100);
               ps.setDouble(23, lw.getLine().getCustEval()*100);
               String cc = lw.getLine().getCustComment();
               if (cc == null){
               	cc = " ";
               }
               ps.setString(24, cc);

               String cd = lw.getLine().getComment();
               if (cd == null){
               	cd = " ";
               }
               ps.setString(25, cd);

               String ce = lw.getLine().getCustAgentId();
               if (ce == null){
               	ce = " ";
               }

               ps.setString(26, ce);

               String cf = lw.getLine().getCustAgentName();
               if (cf == null){
               	cf = " ";
               }
               ps.setString(27, cf);
               ps.setInt(28, lw.getLine().getDealNr());
               ps.setInt(29, lw.getLine().getIdealNr());

               String cr1 = lw.getLine().getResStr1();
               if (cr1 == null){
               	cr1 = " ";
               }
               ps.setString(30, cr1);


               String cr2 = lw.getLine().getResStr2();
               if (cr2 == null){
               	cr2 = " ";
               }
               ps.setString(31, cr2);


               String cr3 = lw.getLine().getResStr3();
               if (cr3 == null){
               	cr3 = " ";
               }
               ps.setString(32, cr3);
               int ii = lw.getLine().getResInt1();
               int res = 0;
               if(ii == 0 ){
               	res = 1;
               }
               if(ii == 1 ){
               	res = 0;
               }
               if(ii == 2 ){
               	res = -1;
               }
               ps.setInt(33, res);
               ps.setInt(34, lw.getLine().getResInt2());
               ps.setInt(35, lw.getLine().getResInt3());
               ps.executeUpdate();
             }

             ps.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
       }

*/
    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,enddate AS kuupaev,docsum AS summa,linecount AS ridu,text AS tekst " +
                   " FROM dayreport WHERE enddate>(getdate()-30) ORDER BY clid,enddate desc ";
      Connection con = getNewCon();
      Connection con2 = getNewCon();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);


      String sql2 = "SELECT  c.kood, aasta AS aastamyyk,kuu AS kuumyyk,a.nimi AS agendinimi, " +
			" contact AS kontaktisik ,phone AS telefon,right( paymentcode,2) AS maksetin, discount AS ale FROM " +
			" customers c LEFT JOIN agents a ON a.kood=c.agent LEFT JOIN Vahetabel v ON v.klient=c.kood" +
			"     WHERE c.agent !='LUKUS' and c.kood in (select klient from dbo.custprodlist2 group by klient) ORDER BY c.nimi";

      Statement st2 = con2.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs2 = st2.executeQuery(sql2);

      StringBuffer sb = new StringBuffer();
      String sep = ";";
      while(rs.next()){
        String cliID = rs.getString("klient").trim();
        sb.append(cliID + sep);
        sb.append(rs.getString("kuupaev").substring(0,10) + sep);
        sb.append(rs.getDouble("summa") + sep);
        sb.append(rs.getInt("ridu") + sep);
        String tekst = rs.getString("tekst");
        if (tekst != null && tekst.trim().length()>0){
        	tekst = tekst.trim();
        	tekst = tekst.replace('\'', ' ').replace('\"', ' ');

        } else {
        	tekst = " ";
        }
        sb.append(tekst + sep);

        boolean has = scrollTo(cliID, rs2);
        if (has){
         sb.append(ensureLen(rs2.getString("aastamyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("kuumyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
         sb.append(ensureLen(rs2.getString("kontaktisik"), " ").replace(';',',') + sep);
         sb.append(ensureLen(rs2.getString("telefon"), " ").replace(';',',') + sep);
         String makse = rs2.getString("maksetin");
         if(makse!= null && makse.trim().length()>0){
         	if(makse.trim().equals("HE")){
         		makse = "1";
         	}

         }
         sb.append(ensureLen(makse, "0") + sep);
         sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
        }

        sb.append("\n");
      }

      ManagerData md = new ManagerData();
      md.setClientVisitInfo(sb.toString());

      sql = "SELECT c.kood AS klient,c.agent AS agent,a.nimi AS agentname FROM customers c INNER JOIN agents a ON a.kood=c.agent " +
     "  WHERE c.agent != 'LUKUS' and c.kood IN (select klient from dbo.custprodlist2 group by klient) GROUP BY c.kood,c.agent,a.nimi,c.nimi ORDER BY c.nimi";

      PreparedStatement ps = con.prepareStatement(sql);
    //  ps.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
      rs = ps.executeQuery();
      StringBuffer bf2 = new StringBuffer();
      while(rs.next()){
        bf2.append(rs.getString("klient").trim() + sep);

      	String agent = rs.getString("agent").trim();
        bf2.append(agent + sep);
        String agentName = rs.getString("agentname");
        if (agentName == null) agentName = " ";
        bf2.append(agentName + sep);
        String email = parent.parseFirstEmail(parent.getEmail(agent));
        if(email == null || email.trim().length()== 0 )email = " ";
        bf2.append(email + "\n");
      }

      md.setClientAgentInfo(bf2.toString());
      return md;
    }


    private boolean scrollTo(String id, ResultSet rs) throws SQLException{
      rs.beforeFirst();
      while(rs.next()){
        if (id.equals(rs.getString("kood").trim())){
          return true;
        }
      }
      return false;
    }

    private String ensureLen(String str, String replace){
      if (str == null) str = "";
      else str = str.trim();
      if (str.length() == 0){
        return replace;
      } else {
        return str;
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

    public static final String DRIVER_VERSION = "AnkvarsFlora";
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();

	@Override
	public void PictureMaker() {
		 makePictureCat();
			System.exit(0);
	}
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

