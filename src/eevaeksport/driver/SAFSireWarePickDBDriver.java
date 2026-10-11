
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
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
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.InventoryLine;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;


public class SAFSireWarePickDBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater,Discounter{
  public SAFSireWarePickDBDriver(EksportPanel parent){
    super(parent);
    if(parent.getParentFrame().isDaySales()){
         this.subjectFilesTo = getParent().getString("senddatamsg");
    } else {this.subjectFilesTo = parent.getString("filemsg");}
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      
      stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
     
    }
  }

  
  public String getProductSortingData(){
   	return null;
   }
  
      public String getDealerCustomerCSV(String agent){
       return null;
       }

      public Vector getFixedAssortmentData() {
        return null;
      }

      
	  public void connectionUp(){
	  }

	  public Vector getPriceMatrixData()  {
        Vector lines = new Vector();
        String    sql = "select '' AS custid, pl.idgrpricelist AS clgroup,pr.code  AS prodid,' ' AS prodgroup,pl.price AS price " +
            " FROM grpricelistrel pl INNER JOIN grarticle pr ON  pl.idgrarticle = pr.id  ORDER BY  pl.idgrpricelist,pr.name";
          Statement st = null;
          try {
            Connection con = getNewConnection();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

              String custID = rs.getString("custid").trim();
              String clGroup = rs.getString("clgroup");
              String prodID = rs.getString("prodid").trim();
              String prodGroup = rs.getString("prodgroup");
             // String sss = rs.getString("SC18009");
              String discount ="";
            /*  if(discount == null){
              	discount = sss.trim();
              }else{
              	discount = discount.trim().replaceAll("%"," ").trim();
              }
			   */
              double price = rs.getDouble("price");
              MatrixLine mline = new MatrixLine();

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
              }
              if(discount.trim().length()==0){
              	discount = "0";
              }
                mline.setDiscount(Double.parseDouble(discount));

              if (Double.parseDouble(discount) == 0 && price != 0) {
                mline.setPrice(price);

              }
             lines.add(mline);
            }

            rs.close();
            st.close();
            connectionDown();
          }
          catch (SQLException ex) {
            ex.printStackTrace();
          }
        return lines;
      }

	  
	  public String getSaveDebtInfo() {
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
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
		  			
		  			String tund = lin.getEnterTime().getHours()+"";
		  			if (tund.length() == 1){
		  				tund = "0" + tund;
		  			}
		  			String minut = lin.getEnterTime().getMinutes()+"";
						if (minut.length() == 1){
							minut = "0" + minut;
		  			}
						
						
						
						if(!tehtud.contains(lineid)){
						
		  			 try{
		  			  
		  			 	
		  			 	con = DriverManager.getConnection(url2, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
		  			
		  			
		  			System.out.println(objekt + "'" + location+ "'" + toLocation + "'" + kogus + "'" +  prodid + "'" + 
		  					ean + "'" + prName+ "'" + wrk + "'" + wrkname+ "'" + entered.toString() +
		  					"'" +warehouse + "'" +toWarehouse + "-" + partii );
		  		 
		  			// Siin tuleb lisada kolm varianti inentuur ja rida ja p�is.
		  			 
		  			if(lineType.equals("INVENTORY")){
		  				//inventuuri rida
		  				
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
		  			
		  			
		  			if(lineType.equals("HEADERDONE")){
		  				//P�is tehtud rida
		  				
		  				int kaste = 0;
		  				int aluseid = 0;
		  				if(kogus > 0){
		  					kaste = (int)kogus;
		  				}
		  				
		  				if((int) lin.getAmountByBooks() > 0){
		  					aluseid = (int) lin.getAmountByBooks();
		  				}
		  				
		  				 String sql3 = " insert into SDC (ts, " +
				         " doknr,wrkid,wrkname,staatus,kaste,aluseid) values( " +
				           "?,'" + objekt + "','"+ wrk +"','" + wrkname + "',1," +kaste+ "," + aluseid +")"  ;
				          PreparedStatement st = con.prepareStatement(sql3);
				          System.out.println(sql3 );
				     
				           st.setTimestamp(1, new Timestamp(entered.getTime()));
				           st.executeUpdate();
				              System.out.println("lisame HEADERDONE rea juurde  "  );
				            st.close();
		  			}
		  			

		  			if(lineType.equals("LINECHANGE")){
		  				//Rea muutus
		  				 String sql3 = " insert into SDCRow (doknr,ean,kauba_kood, " +
				         " kogus,SAFROWID) values('" + objekt+ "','" +
				            ean + "','" + prodid + "',? ,"+ toLocation +")"  ;
				          PreparedStatement st = con.prepareStatement(sql3);
				          System.out.println(sql3 );
				     
				           st.setDouble(1, kogus);
				           st.executeUpdate();
				              System.out.println("lisame Linechange rea juurde  "  );
				            st.close();
		  			}
		  	 
		            tehtud.add(lineid);
					 try{

		   			 saveObject(tehtud, "loetudid.vec"); 
					 }catch(Exception e){
						 e.printStackTrace();
					 }
		   		      	
		   		        con.close();
		   		        
		  			 }catch (Exception e){
		  			 	e.printStackTrace();
		  			 }
		  		
		  			
						}
		  		}
		  	
		  	}
		  }

		  
		  
		  
		public Vector getLastThreeData() {
			// TODO Auto-generated method stub
			return null;
		}

	  
	  public String getAssortmentStr(Agent agent){
	  	return null;
	  }



      public String getSaveStockInfo(){

      	  String  cmd = "SELECT pr.code AS toode,SUM(quantity) AS seis FROM dcurbalancestore ss  " +
    		 "INNER JOIN grarticle pr ON idgrarticle = pr.id WHERE ss.iddrstore=1 AND quantity >0 GROUP BY  pr.code " ;


		       if (isDebug()) System.out.println(cmd);
		       StringBuffer products = new StringBuffer();
		       try {
		       	Connection con = getNewConnection();
		         Statement st = con.createStatement();
		         ResultSet rs = st.executeQuery(cmd);

		         while (rs.next()) {

		           String kood = rs.getString("toode").trim();
		           double seis = rs.getDouble("seis");
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
      
       
 	  public String  getCustProdDiscounts(Agent agent){
	  return null;
	  }
	
 	   public String getPilotDayData(Agent agent){

 	  	
            return null;

    }

	  public void connectionDown(){
	  }

	  
    public TreeMap getAgents(){
  	
    String cmd = "SELECT er.code AS agent, er.firstname + ' ' + surname AS nimetus, er.code as abikood ,exportcode AS dubbler  " +
      "  FROM   eremployee er Order by er.code";
  StringBuffer sb = new StringBuffer();
    if (isDebug()) System.out.println(cmd);
  
    String dubbler = getParent().getProps().getProperty("dubbler");
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = rs.getString("nimetus").trim();
          sb.append(agent.getID() + ";" + name+ ";0000\n") ;
          if(dubbler != null && dubbler.trim().length()>1){
          	StringTokenizer st = new StringTokenizer(dubbler,";");
          	while(st.hasMoreElements()){
          			String dub = st.nextToken().trim();        	
              		agent.addDubbler(dub);
              		System.out.println("dubbler added " + agent.getID() + " dub. " + agent.getDubbler());     		
          	}
          }
          agent.setName(name);
          agent.setIsPilot(true);
         // agent.setObject("607");
          
          agents.put(name, agent);
          aHash.put(agent.getID(), agent);
        }
    rs.close();
    con.close();

    } catch (Exception e){
        getParent().showError(e);
      }
   Agent aa = new Agent("LADU");
    aa.setName("LADU");
    agents.put(aa.getName(), aa);
    
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

    return agents;
  }



      public String getDistribution() {
    return null;
      }




  public TreeMap getCustomers(Agent agent){
    TreeMap customers = new TreeMap();

    try {
      Connection con = getNewConnection();
      Statement st = con.createStatement();

      String cmd2 = "  select sourcedocno as kood , companyname as nimi, count(idgrarticle) as lines , d.id as docid   from sdConsignmD d " +
      	" inner join ddStoreDRow r on d.id=r.idgdocument where d.docdate > getdate()-20 and idsdinvoice is null and u_kaste < 1 " +
         "  group by sourcedocno,companyname ,d.id " +
         "  order by companyname";


      if (isDebug()) System.out.println(cmd2);
      ResultSet rs = st.executeQuery(cmd2);
      HashMap custMap = new HashMap();

      while(rs.next()){
          String kood = rs.getString("kood").trim();
          Customer cust = new Customer(kood);

  		String nimi = kood + " " +rs.getString("nimi").trim() + "<" + rs.getString("lines") + ">" ;
          cust.setName(nimi);
    
          cust.addAgent(new Agent("LADU"));
          cust.setContractOver(rs.getString("docid"));
               
          
          customers.put(nimi, cust);
          custMap.put(cust.getID(), cust);
    }

  

 
         st.close();
         con.close();


    } catch (SQLException e){
      getParent().showError(e);
    }
    return customers;
  }

  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
  

  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = " SELECT id AS liik , name AS nimetus FROM grarticlecat WHERE category='Peagrupp'  ORDER BY name ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
        tm.put(rs.getString("liik").trim(),
               rs.getString("nimetus").trim());
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}

    public TreeMap getProdGroupsSecond() throws SQLException {
        Connection con = getNewConnection();
        TreeMap   tm = new TreeMap();
        String    sql = " SELECT id AS liik , name AS nimetus FROM grarticlecat WHERE category='Alamgrupp'  ORDER BY name ";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            tm.put(rs.getString("liik").trim(),
                   rs.getString("nimetus").trim());
        }
        rs.close();
        st.close();
        con.close();

        return tm;
  }

    

    private String turn(String asi ){
    	String aa = "";
    	StringTokenizer st = new StringTokenizer(asi," ");
    	while (st.hasMoreTokens()){
    		aa = st.nextToken() + " " + aa;
    	}
    	
    	return aa.trim() ;
    }


  public TreeMap getProducts(Agent agent){
	  StringBuffer buf = new StringBuffer();
	  String cmd = "SELECT pr.code AS kood,pr.code,U_Asukoht as asuk, pr.name AS nimi,1 AS hind,(case when  (bc.code) is null then u_ean else bc.code end )  AS EAN , " + 
      "  pr.contentquantity AS tykke ,y.name AS yhik  " +
      "  from sdConsignmD d  " +
      "	inner join ddStoreDRow r on d.id=r.idgdocument  inner join  grarticle pr  ON pr.id=r.idgrarticle " +
      "  LEFT JOIN grunit y ON y.id=pr.idgrunit  LEFT JOIN grbarcode bc ON bc.idgrarticle=pr.id " +
      " WHERE  d.docdate > getdate()-20 and  idsdinvoice is null and u_kaste < 1 ORDER BY pr.name";
   
  
   
   

   if (isDebug()) System.out.println(cmd);
   TreeMap products = new TreeMap();
   try {
     
     Connection con = getNewConnection();
     Statement st = con.createStatement();
     ResultSet rs = st.executeQuery(cmd);

     while(rs.next()){
  
       String kood = rs.getString("kood").trim();
       String code = rs.getString("code").trim();
        Product prod = new Product(kood);
        String asuk = rs.getString("asuk").trim();
        String name = rs.getString("nimi").trim();
        
        name = name.replaceAll("Sukkp�ksid", "Sp.");
        name = name.replaceAll("Sokid", "Sk.");
        name = name.replaceAll("sokid", "sk.");
        name = name.replaceAll("Naiste", "N.");
        name = name.replaceAll("Laste", "La.");
        name = name.replaceAll("Meeste", "M.");
        name = name.replaceAll("madalad", "mad.");
        name = name.replaceAll("P�lvikud", "P�.");
        name = name.replaceAll("rinnahoidja", "rh.");
        name = name.replaceAll("bokserid", "bks.");
        name = name.replaceAll("korrigeeriv", "korr.");
        
        name = turn(name);
         
        String nimetus = asuk + " " + name + " " + code;
        double hind = rs.getDouble("hind");
        prod.setPrice(hind);

        String ean = rs.getString("ean");
        if (ean != null && ean.trim().length() > 7) {
          prod.setEAN(ean.trim());
          nimetus = nimetus + " " + ean.substring(ean.length()-4);
        }
    
        prod.setName(nimetus);
        
        prod.setInCases( (rs.getDouble("tykke")));
       String unit = rs.getString("yhik");

       if (unit != null && unit.trim().length() > 0) {
         unit = unit.trim();
         prod.setUnit(unit);
       }
              
       if(prod.getEAN().trim().length()==0){
      	 buf.append(kood.trim() + ";"+ kood.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock() + "\n");
      }else{
      	 buf.append(prod.getEAN() + ";"+ kood.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock() + "\n");
      	 if(prod.getEanCase().trim().length() >0){
      		
          	 buf.append(prod.getEanCase() + ";"+ kood.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock() + "\n");	 
      	 }
      }
       
    
       products.put(nimetus, prod);
       //}
     }
     rs.close();
     st.close();
     con.close();
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

   } catch (Exception e){
     getParent().showError(e);
   }

   return products;
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

  
  

  public Vector getCustomerProductsList(String agentCode){
  	

    String sql1 = 	"SELECT d.sourcedocno AS klient,t.code AS toode, r.quantity AS kogus ,d.docdate as kuupaev ,r.unitprice AS lastprice  ,r.id as reaid  " +
 "  FROM sdConsignmD d  INNER JOIN ddStoreDRow r ON d.id=r.idgdocument " +
  "  INNER JOIN grarticle t ON t.id=r.idgrarticle  " +
  "  WHERE d.docdate > getdate()-20 and idsdinvoice is null and u_kaste < 1 " +  
  "  GROUP BY sourcedocno,t.code,r.quantity,d.docdate,r.unitprice,r.id ORDER BY sourcedocno,t.code,d.docdate";

    
           if (isDebug()) {
                System.out.println(sql1);
            }

            Vector custProds = new Vector();

            try {
              Connection con = getNewConnection();
              PreparedStatement stmt = con.prepareStatement(sql1);

              //stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
              ResultSet	      rs = stmt.executeQuery();

              HashMap kliendid = new HashMap();
              while (rs.next()) {
                String kliendikood = rs.getString("klient").trim();
                String tootekood = rs.getString("toode").trim();
                double kogus = Double.parseDouble(rs.getString("kogus").trim());
                String lastdate = rs.getString("kuupaev").substring(0,10);
                 lastdate = lastdate.substring(5,10);
                double hind = Double.parseDouble(rs.getString("lastprice").trim());
                String reaid = rs.getString("reaid");
                Customer cust = (Customer)kliendid.get(kliendikood);
                if (cust == null){
                  cust = new Customer(kliendikood);
                }
                Product prod = new Product(tootekood);
                cust.addProduct(prod, new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,lastdate,hind,0,0,reaid),false);
                kliendid.put(kliendikood, cust);
                CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,0,0,hind,reaid);
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
       Vector list = new Vector();
       return list;
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
      return "SAFSireWarePick";
    }

    public String getSubjectFilesTo() {
      return this.subjectFilesTo;
    }


    public String getDaySalesData(Agent agent) {
      return null;
    }




    public void insertDefective(DefectiveWrapper wr){
    	
    }
     


    public void insertDistribution(DistEvalListWrapper wr){
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


  private String url = "jdbc:odbc:SAF";
  private String url2 = "jdbc:odbc:import";
  private Connection con;
  private TreeMap agents = new TreeMap();
  private     HashMap aHash = new HashMap();
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Statement stmt;
  private String subjectFilesTo = null;
  private NumberFormat NF = NumberFormat.getInstance();
  private TreeMap  dealers = new TreeMap();
}
