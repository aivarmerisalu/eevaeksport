package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Dealer;
import eevaeksport.DealerProduct;
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
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class AxaLitAgraDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
  public AxaLitAgraDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }

  
  public TreeMap getDealers() {
	   TreeMap	dealers = new TreeMap();
	   
	   String sql5 = "  select  INVENTLOCATIONID as kood ,name as nimi from Mj_getInventLocation where " +
	    " dataareaid ='110'  and inventsiteid = 2 ";
  try{
	  
      Connection con = getNewConnection();
      
      Statement stm = con.createStatement();
	  
	  ResultSet rs = stm.executeQuery(sql5);
	  
      while(rs.next()){
      	String kood = Converter.enCodeEst(rs.getString("kood").trim());
      	String name = Converter.enCodeEst(rs.getString("nimi").trim());     
        Dealer d = new Dealer(kood);
        d.setName(kood); //name
        dealers.put(kood, d);
      }
       rs.close();
       stm.close();
       con.close();
       
   }catch (Exception rr){
	rr.printStackTrace();
     }
		return dealers;
	}
  
  
  private String getVisitDay(String input){
	  String res = "";
	  
	  for (int i = 0 ; i < input.length(); i++ ){
		char a = input.charAt(i);
		if(a == 'A'){
			res = res + "E";
		}
		if(a == 'B'){
			res = res + "T";
		}
		if(a == 'C'){
			res = res + "K";
		}
		if(a == 'D'){
			res = res + "N";
		}
		if(a == 'E'){
			res = res + "R";
		}
		if(a == 'F'){
			res = res + "L";
		}
		if(a == 'G'){
			res = res + "P";
		}
		if(a == '1'){
			res = res + "2";
		}if(a == '2'){
			res = res + "1";
		}if(a == '3'){
			res = res + "4";
		}if(a == '4'){
			res = res + "8";
		}if(a == '5'){
			res = res + "16";
		}if(a == '6'){
			res = res + "32";
		}
	  }
	  
	  if(res.length() == 0){
		  res = " ";
	  }
	  return res ;
  }

	

	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	     String sql = "INSERT into supervisor values ("
	       + "?, "   //   agent	C(50),
	       + "?,"   //   agentname	C(200),
	       + "?,"   //   clientid	C(50),
	       + "?,"   //   clientname	C(200),
	       + "?,"   //	prodid	C(50),
	       + "?,"   //   prodname	C(200),
	       + "getdate() ,"   //	sentdate	D,
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
	        Connection con = getNewConnection(url2);
	           PreparedStatement ps = con.prepareStatement(sql);

	           for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
	        	   eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
	             ps.setString(1, wr.getInfo().getAgentID());
	             ps.setString(2, wr.getInfo().getAgentName());
	             ps.setString(3, lw.getLine().getCustID());
	             ps.setString(4, lw.getLine().getCustName());
	             ps.setString(5, lw.getLine().getProdID());
	             ps.setString(6, lw.getLine().getProdName());
	             ps.setDouble(7, lw.getLine().getNormalPrice());
	             ps.setDouble(8, lw.getLine().getAmount());
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
	             ps.setInt(33, lw.getLine().getResInt1());
	             ps.setInt(34, lw.getLine().getResInt2());
	             ps.setInt(35, 111); //111 on eesti andmed
	             ps.executeUpdate();
	           }

	           ps.close();
	           con.close();
	       } catch (SQLException e) {
	           e.printStackTrace();
	       }
	     }

  /**
   *
   */
  public void connectionUp(){
  }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

  public String getSaveDebtInfo() {
   	return null;
   }

  private String getDir() {
		String dir = getParent().getProps().getProperty("readin");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}

	public Vector getFixedAssortmentData() {
		Vector result = new Vector();

		return result;

	}


  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
         String sql = " select prodid,custgr,price ,algus ,lopp from MJ_getPrice";

          try {


        	Statement stmt = this.getNewConnection().createStatement();
                  ResultSet rs = stmt.executeQuery(sql);

                  while (rs.next()) {
                	  String prodID = rs.getString("prodid");
                          String custGR = rs.getString("custgr");
                          double price = rs.getDouble("price");
                      
                          String algus = rs.getString("algus");
                          String lopp = rs.getString("lopp");
                          MatrixLine mline = new MatrixLine();
                          
                    
                          if (custGR != null && custGR.trim().length() > 0) {
                                  mline.setCliGroup(custGR.trim());
                          }
                       
                          if (prodID != null && prodID.trim().length() > 0) {
                                  mline.setProdID(prodID.trim());
                          }
                     

                          if (price != 0) {
                                  mline.setPrice(price);
                          }
                          
                          if (algus != null && algus.trim().length() > 0) {
                              mline.setStartDate(algus.trim().substring(0,10));
                      }
                          if (lopp != null && lopp.trim().length() > 0) {
                              mline.setEndDate(lopp.trim().substring(0,10));
                      } 
                          lines.add(mline);
                  }
                  rs.close();
                  
          } catch (SQLException ex) {
          ex.printStackTrace();
          }
          return lines;
  }

  public String getProductSortingData(){
   	return null;
   }

  public Vector getDebt(String agentCode) throws SQLException{
     return this.debt;
   }


  public String getAssortmentStr(Agent agent){
  	return null;
  }
  
  private HashMap kogused = null;
  private HashMap kogusedStr = null;

 public String getSaveStockInfo(){
	  
kogused = new HashMap();
kogusedStr = new HashMap();
	  
	  String  cmd = " select  ITEMID as toode ,sum(AVEILPHYSICAL) as kogus ,inv.INVENTLOCATIONID as ladu ,l.inventsiteid as res from MJ_getInvensSum inv " +
                    " inner join Mj_getInventLocation l on inv.INVENTLOCATIONID = l.INVENTLOCATIONID where"
                    + " inv.dataareaid ='110'   " +
	  		        "  group by inv.INVENTLOCATIONID,ITEMID,l.inventsiteid  having sum(AVEILPHYSICAL) != 0 ";
  

       if (isDebug()) System.out.println(cmd);
       StringBuffer products = new StringBuffer();
       try {

         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery(cmd);

         while (rs.next()) {

           String kood = rs.getString("toode").trim();
           double seis = rs.getDouble("kogus");
           String ladu = rs.getString("ladu").trim();
           int res = rs.getInt("res");
           
           if(seis > 0){
        	   if(res == 2){
        		   products.append(kood +  ";");
                   products.append(" ;");
                   products.append(getParent().getNF().format(seis) + ";" + ladu  +  "\n"); // + dealer +  "\n");
                   if(kogused.get(kood) != null){
                	   double vahe = Double.parseDouble((String) kogused.get(kood));
                	   kogused.put(kood, (vahe+ seis) + "");
                   }else{
                	   kogused.put(kood, seis + ""); 
                   }
                  
        	   }else{

                   if(kogusedStr.get(kood) != null){
                	   String vahe = (String) kogusedStr.get(kood);
                	   kogusedStr.put(kood, vahe  + "|  " + ladu + ": " + (int)seis  );
                   }else{
                	   kogusedStr.put(kood, "Likutis: " + ladu + ": " + (int)seis  );
                   }   
        	   }
              
               
             
           }
           }
           rs.close();
       } catch (Exception e) {
           getParent().showError(e);
       }
    
       return products.toString();
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
  public void connectionDown(){
  }

  
  public DealerProduct getDealerProductsList() {

	   
      DealerProduct dealerProds = new DealerProduct();
      try {
    	  String  cmd = " select  ITEMID as kood ,sum(AVEILPHYSICAL) as kogus ,inv.INVENTLOCATIONID as ladu from MJ_getInvensSum inv " +
          " inner join Mj_getInventLocation l on inv.INVENTLOCATIONID = l.INVENTLOCATIONID where inv.dataareaid ='110' " +
	        " and l.inventsiteid = 2 group by inv.INVENTLOCATIONID,ITEMID having sum(AVEILPHYSICAL) != 0 ";
           if (isDebug()) System.out.println(cmd);
          Connection con = getNewConnection();
         
              Statement stm = con.createStatement();
              ResultSet rst = stm.executeQuery(cmd);

                while (rst.next()) {
                  String toode = rst.getString("kood").trim();
                  int seis = (int)Double.parseDouble(rst.getString("kogus").replace(',','.'));
                  String ladu  =  rst.getString("ladu").trim();
                  if(seis != 0){
                	  dealerProds.add(ladu, toode,seis+""," "," " );  
                  }
              }
              rst.close();
              stm.close();
              con.close();

      } catch (Exception e) {
          getParent().showError(e);
      }
         System.out.println("DealerProds size " + dealerProds.getDealers().size());
       return dealerProds;
     }

  
  
  
  
  public TreeMap getAgents(){
    String cmd = "select groupid as agent , Name as nimi ,Balace as volg , Ovedue as overvolg, " +
    		" Purch_Grain as grain, Contract_Grain as contractGrain  from MJ_getManagers where dataareaid = '110'";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    
    
    try {
    
    	

    	 FileReader fr = new FileReader("vacation.txt");
         BufferedReader reader = new BufferedReader(fr);
       String line = null;
      HashMap asendajad = new HashMap();
      
        while((line = reader.readLine()) != null ){
           
        	StringTokenizer st = new StringTokenizer(line, ";");
//  System.out.println(line);
          if (st.hasMoreTokens()) {
            String ag1 =	st.nextToken();
            String man = st.nextToken().trim();
            Vector vv = new Vector();
            if(asendajad.get(ag1) != null){
           	 vv = (Vector)asendajad.get(ag1);
           	 vv.add(man);
           	 asendajad.put(ag1, vv);
            }else{
           	 vv.add(man);
           	 asendajad.put(ag1, vv); 
             }
            }
          }
    	
    			
    	ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = rs.getString("nimi").trim() + " " + agent.getID();
          int yldvolg = rs.getInt("volg"); 
          int overvolg = rs.getInt("overvolg");
          int grain  = rs.getInt("grain");  
          int contractgrain  = rs.getInt("contractgrain"); 
         
          
          if(overvolg != 0){
        	 agent.setStart("Pradelstos skolos : " +overvolg + " €.|" ); 
          }   else {
        	  agent.setStart("Pradelstos skolos : 0 €.|" ); 
          }

         if(yldvolg != 0){
        	 agent.setStart(agent.getStart() + "Nepradelstos skolos : " +yldvolg + " €.|" );
          } else {
        	  agent.setStart(agent.getStart() + "Nepradelstos skolos : 0 €.|" );
          }
         
         if(grain != 0){
        	 agent.setStart(agent.getStart() + Converter.enCodeEst("Supirkta grûdø einami metai : ") +grain + " t.|" );
          } else {
        	  agent.setStart(agent.getStart() + Converter.enCodeEst("Supirkta grûdø einami metai : 0 t.|" ));
          }
        
         if(contractgrain != 0){
        	 agent.setStart(agent.getStart() + Converter.enCodeEst("Sukontraktuota grûdø einami metai : ") +contractgrain + " t.|" );
          } else {
        	  agent.setStart(agent.getStart() + Converter.enCodeEst("Sukontraktuota grûdø einami metai : 0 t.|" ));
          }
          agent.setName(name);
          agent.setIsPilot(true);
          
          
          if(asendajad.get(agent.getID()) != null){
          	Vector vv = (Vector)asendajad.get(agent.getID());
          	Enumeration aa = vv.elements();
          	while(aa.hasMoreElements()){
          		agent.addDubbler((String)aa.nextElement());
          	}
          }

		 agents1.put(agent.getID(), agent);
          agents.put(name, agent);
        }
        rs.close();
    } catch (Exception e){
        getParent().showError(e);
      }
  /*  Agent aa = new Agent ("HOUSE1");
    aa.setName("HOUSE1");
    agents.put("HOUSE1", aa);
    */
    
    return agents;
  }
  
  
  public TreeMap getCustomers(Agent agent){

 String cmd = " SELECT  cus.name,nimelisa,(cus.accountnum)+'_'+ltrim(str(aadr.addrrecid)) as accountnum,payer,   cus.linedisc , " + 
  " aadr.address, visit , agent , paymtermid as term,  block , payerblock ,   info, email , pricegroup as grupp , minpricegroup as groupmin  ,credit , pay.asi as used  ,ove.asi as debt , " +
  " Sales_Last_Year as lastsales , Sales_This_Year as thissales, Contract_Grain_Tons as grcon , Delivered_Grain_Tons as grdeltons ,Delivered_Grain_Amount as grdelamount, " +
  " contractnum_col as leping,relvendbalance_col as hankevolg , RiskGroup " +
" FROM MJ_GetAddress aadr inner join  MJ_getCustomers cus  on  cus.accountnum = aadr.accountnum and aadr.dataareaid= cus.dataareaid " + 
"  LEFT JOIN MJ_getPayerBalance pay on pay.accountnum = cus.accountnum and pay.dataareaid =cus.dataareaid " +
"  LEFT JOIN MJ_getPayerOverdue ove on ove.accountnum = cus.accountnum and pay.dataareaid =cus.dataareaid where cus.dataareaid ='110'";
 
 
 
           String sql = " SELECT  klient,numbe,maksepaev, tasumata, kuupaev , yle FROM MJ_getDebtList co where dataareaid ='110'  ";

           String sql3 = " SELECT purposeid as kood ,name as nimi  FROM MJ_getShipMode co where dataareaid ='110' ";
           
           String sql4 = "  select (ROW_NUMBER() OVER (ORDER BY paymtermid))+1 AS Row ,paymtermid as kood  , " +
           		         " description  as nimi from MJ_getPaymTerm where dataareaid ='ltb'  order by description  ";
           
        
//seda on vaja tuunida .



  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    
    try {
    	
    	Statement stmt = this.getNewConnection().createStatement();
    	
    	ResultSet  rs = stmt.executeQuery(sql3);
    	StringBuffer sb1 = new StringBuffer();
        while(rs.next()){
        	String kood = Converter.enCodeEst(rs.getString("kood").trim());
        	String name = Converter.enCodeEst(rs.getString("nimi").trim());
        	sb1.append(kood + ";" + name + "\n");
        }
        rs.close();
        try{
        
       	  Writer out = new BufferedWriter(new OutputStreamWriter(
  			    new FileOutputStream("shipmode.txt"), "UTF-8"));
  			try {
  			    out.write(sb1.toString());
  			} finally {
  			    out.close();
  			}
        }catch(Exception r){
        	
        }
    	HashMap tasud = new HashMap(); 
        rs = stmt.executeQuery(sql4);
    	sb1 = new StringBuffer();
        while(rs.next()){
        	String row = Converter.enCodeEst(rs.getString("row").trim());
        	String kood = Converter.enCodeEst(rs.getString("kood").trim());
        	String name = Converter.enCodeEst(rs.getString("nimi").trim());
        	sb1.append(row + ";" + name + "\n");
        	tasud.put(kood, row);
        }
        rs.close();
        try{
        
       	  Writer out = new BufferedWriter(new OutputStreamWriter(
  			    new FileOutputStream("paymterm.txt"), "UTF-8"));
  			try {
  			    out.write(sb1.toString());
  			} finally {
  			    out.close();
  			}
        }catch(Exception r){
        	
        }
      
       
        
    
    	
       rs = stmt.executeQuery(cmd);
      while(rs.next()){
        String name = rs.getString("name").trim();
      
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);
//SELECT  cus.name,nimelisa,cus.accountnum,payer,   cus.linedisc , 
 //      cus.address, visit , agent , term,  block , payerblock ,   info ,  grupp ,credit
   //     FROM MJ_getCustomers cus 
      //  name = name + " " + accountnum;
      
        String payer = rs.getString("payer");
        if(payer != null && payer.trim().length() > 0){
        	cust.setPayer(payer.trim());
        }else{
        	cust.setPayer(accountnum);
        }
     
     
        String aleke = rs.getString("linedisc");
        if(aleke != null && aleke.trim().length()>0){
            aleke = aleke.trim();
           // cust.setGroup(aleke);
        } else {
        	aleke = "000" ;
        }

        String aadr = rs.getString("address");
        if (aadr != null) {
        	
        	aadr = "." + aadr + ",Lithuania.";
          cust.setAddress(Converter.enCodeEst(aadr.trim().replace('\n',',')));
          name = name + " " + aadr.trim().replace('\n',',') ;
        }
        
        cust.setName( Converter.enCodeEst(name));
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
        String agen = rs.getString("agent");
        if (agen != null) {
          cust.addAgent(new Agent(agen.trim()));   
          cust.addAgent(new Agent("ADPARD_01"));
          if (agen != null ){
 				Agent agg = ((Agent)this.getAgents1().get(agen));
				
 				if(agg != null && agg.getDubbler().size()>0){
 					//System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
 					for (int r = 0 ; r < agg.getDubbler().size() ; r++){
 						Agent dub = new Agent((String)agg.getDubbler().elementAt(r));
 						cust.addAgent(dub);
 					}
 				}
 			}
          
        }
        
     
        String term = rs.getString("term");
        if(term != null && term.trim().length() >0){
        	try{
        	  	cust.setPaymTerm((String)tasud.get(term.trim()));		
        	}catch(Exception e){   
        		e.printStackTrace();
        	}
      	}
        
        int block = rs.getInt("block");
        if (block == 1 || block == 2) {
          cust.setSalesBlock(true);
        }
        
        int payerblock = rs.getInt("payerblock");
        if (payerblock == 1) {
          cust.setSalesBlock(true);
        }

        
        String info = rs.getString("info");
        if (info != null && info.trim().length()>0) {
          cust.setPersonalData(info.trim());
        }
        
        String mail = rs.getString("email");
        if (mail != null && mail.trim().length() > 4) {
        	if(cust.getPersonalData() != null &&cust.getPersonalData().trim().length() >0 ){
        		 cust.setPersonalData(cust.getPersonalData() + " | " + mail.trim());	
        	}else{
        		 cust.setPersonalData( mail.trim());
        	}
         
        }
        
        String grupp = rs.getString("grupp");
        if (grupp != null && grupp.trim().length()>0) {
          cust.setGroup(grupp.trim());
        }
        
      
        
        
       double  credit = rs.getDouble("credit");
       if(credit != 0){
    	   cust.setCredit(((int)credit)+"");
       }
         
       String usedStr = rs.getString("used");
       double yldvolg = 0;
       if (usedStr != null){
        // cust.setUsedCredit((int)Double.parseDouble(usedStr));
         yldvolg = Double.parseDouble(usedStr);
       }

       String debtStr = rs.getString("debt");
       if (debtStr != null){
         cust.setDebt(Double.parseDouble(debtStr));
       }
       
       
     int lastsales = rs.getInt("lastsales");
      
       int thissales = rs.getInt("thissales");
       int grainContr = rs.getInt("grcon");
       int grainTons = rs.getInt("grdeltons");
       int grainCurr = rs.getInt("grdelamount");
       String contract = rs.getString("leping");
       int hankevolg = rs.getInt("hankevolg");
       double kliendivolg = yldvolg;
       String risk  = rs.getString("RiskGroup");
    
       
       
        if(risk != null &&contract.trim().length() > 0 ){
        	cust.setCity( Converter.enCodeEst(" Rizikos grupė : ") + risk + "|");
        }
        
        if(kliendivolg != 0){
        	cust.setCity(cust.getCity() + Converter.enCodeEst("Kliento skola Litagrai : '") + kliendivolg + " € |");
        }
        
        if(hankevolg != 0){
        	cust.setCity(cust.getCity() + Converter.enCodeEst("Litagros skola klientui : '") + hankevolg + " € |");
        }
        

        if(thissales != 0){
        	//cust.setCity(cust.getCity() + Converter.enCodeEst("Pardavimai einami metai : '") + thissales + " € |");
        }
        
        
        if(lastsales != 0){
        	//cust.setCity(cust.getCity() + Converter.enCodeEst("Pardavimai praeiti metai : '") + lastsales + " € |");
        }
       
        
       if(grainContr != 0){
        	cust.setCity(cust.getCity() + "Sukontraktuota grūdų einami metai( t ) : '" + grainContr + "|");
        }
        
        if(grainTons != 0){
        	cust.setCity(cust.getCity() + Converter.enCodeEst("Supirkta grūdų einami metai( t ) : '") + grainTons + "|");
        }
        
        if(grainCurr != 0){
        	cust.setCity(cust.getCity() + Converter.enCodeEst("Supirkta grūdų einami metai( € ) : '") + grainCurr + "|");
        }
        
        
        if(contract != null &&contract.trim().length() > 0 ){
           	cust.setCity(cust.getCity() + Converter.enCodeEst("Sutarties numeris : '") + contract + "|");
        }
         
         cust.setCity(cust.getCity().replace('.', ',').replace('.', ',').replace('.', ',').replace('.', ',').replace('.', ','));
                    
                        customers.put(name, cust);
                        custMap.put(cust.getID(),cust);	
                  }
      

      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim());
                        String number = rs.getString("numbe").trim();
                        debt.setDoknr(number);
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(rs.getDouble("tasumata"));
                        debt.setDate(rs.getDate("kuupaev"));
                        debt.setDue(rs.getString("yle"));
                        this.debt.addElement(debt);
                      }


    } catch (Exception e){
      getParent().showError(e);
    }
    return customers;
  }



  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

  private Connection getNewConnection(String url) throws SQLException{
	    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	  }
      public String getDistribution() {
        return null;
      }


  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = " SELECT distinct productcode as liik,productname as nimi FROM MJ_getProducts where dataareaid='110'" +
    		"  ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik,  Converter.enCodeEst(rs.getString("nimi").trim()));
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}


  public TreeMap getProdGroupsSecond() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "   SELECT distinct groupcode as liik,groupname as nimi FROM MJ_getProducts where dataareaid='110' ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
      String nimi = rs.getString("nimi").trim();
      if(nimi.length()> 20 ){
    	  nimi = Converter.firstToUpperCase(nimi);
      }
        tm.put(liik, Converter.enCodeEst(nimi));
    }
    rs.close();
    st.close();
    con.close();

    return tm;
  }
  public TreeMap getAgents1() {
		return agents1;
	}

  private TreeMap agents1 = new TreeMap();

  public TreeMap getProducts(Agent agent){

    String cmd = " SELECT inv.itemid, inv.nimetus,inv.kastis,inv.alusel,inv.price,inv.ean ,inv.unit,inv.vat,inv.groupcode,inv.productcode " +
    		" , inv.ManufName ,prices as prstring FROM MJ_getProducts inv LEFT JOIN MJ_getStdPrices pr on pr.prodid = inv.itemid  " +
    		" where price > 0 and pr.dataareaid='110' and inv.dataareaid='110' ";
    
    getSaveStockInfo();
  
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = kood.substring(0,2) + " " +rs.getString("nimetus").trim() + " " + kood.substring(3) ;
     nimetus =  Converter.enCodeEst(nimetus);
 
  /*   if(kogused.get(kood) != null){
    	prod.setStock(Double.parseDouble((String)kogused.get(kood)));
      }else{
    	prod.setStock(0);
     }*/
    
  
        prod.setInCases(rs.getDouble("kastis"));
        prod.setPrice(rs.getDouble("price"));

        String ean = rs.getString("ean");
        String add = "";
        if(ean != null && ean.trim().length()>0){
        prod.setEAN(ean);
      }
     

      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit.trim());
       add = " " +unit.trim() + " " + add;
    }
      
      prod.setName(nimetus + add);
      
      String vat = rs.getString("vat");
      if(vat!=null && vat.trim().length()>0){
       prod.setVat("21");
    }
 
      
     String grupp2 = rs.getString("groupcode");
      if(grupp2 != null && grupp2.trim().length()>0){
             prod.setGroupSecond(grupp2);
          }
            
      grupp2 = rs.getString("productcode");     
      if(grupp2 != null && grupp2.trim().length()>0){
             prod.setGroup(grupp2);
          }
      
      String tootja = rs.getString("ManufName");
      
      if(tootja != null && tootja.trim().length()>0){
          prod.setPartii( "Gamintojas : " + Converter.enCodeEst(tootja) + "|");
       }
      
String prstring =  rs.getString("prstring");
      
      if(prstring != null && prstring.trim().length()>0){
        	prod.setPartii(prod.getPartii() +"Kaina : "+ (String)prstring.trim() +"|" );     	
        }  
      
      if(kogusedStr.get(kood) != null){
      	prod.setPartii(prod.getPartii() +(String)kogusedStr.get(kood) );     	
      }    

        products.put(nimetus, prod);
        prodHash.put(prod.getID(), prod);

      }
      rs.close();
      
    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }




  public Vector getCustomerProductsList(String agentCode){
	    Vector custProds = new Vector();
	    //if (true) return custProds ;
	 int counter = 1;
	   String cmd = "SELECT customer as orderaccount, itemid , qty, lastdate ,addrrecid "+ 
	        " FROM MJ_getCustomerProductList  where dataareaid = '110' order by 1,5,2,4 " ;


	    if (isDebug()) System.out.println(cmd);

	    try {

	    	Statement stmt =  this.getNewConnection().createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	      counter++;
	      while(rs.next()){
	        String kliendikood = rs.getString("orderaccount").trim();
	        String tootekood = rs.getString("itemid").trim();
	        double kogus = rs.getDouble("qty");
	  
	        String lastdate = rs.getString("lastdate").substring(0,10);
	        String addrrecid = rs.getString("addrrecid");
	        if(addrrecid == null){
	        	addrrecid = "";
	        }else{
	        	addrrecid = "_" + addrrecid.trim();
	        }
	                 lastdate = lastdate.substring(5,10);
	                 double hind =0;// rs.getDouble("hind");
	     	     //   String bill = rs.getString("dok");          
	        CustProd cp = new CustProd(kliendikood  + addrrecid , tootekood, kogus ,0 ,0,lastdate,hind,0,0," ");
	       // cp.setAbiKood(bill);

	        //cp.setRedline(rs.getString("date"));
	        custProds.add(cp);
	      }
	    } catch (Exception e){
	      getParent().showError(e);
	      if(counter< 4){
	      	try{
	        	 ResultSet rs = stmt.executeQuery(cmd);
	             counter++;
	             while(rs.next()){
	               String kliendikood = rs.getString("orderaccount").trim();
	               String tootekood = rs.getString("itemid").trim();
	               String lastdate = rs.getString("lastdate").substring(0,10);
	               String addrrecid = rs.getString("addrrecid");
	   	        if(addrrecid == null){
	   	        	addrrecid = "";
	   	        }else{
	   	        	addrrecid = "_" + addrrecid.trim();
	   	        }
	   	          
	                        lastdate = lastdate.substring(5,10);
	               CustProd cp = new CustProd(kliendikood  + addrrecid , tootekood, 0 ,0 ,0,lastdate,0,0,0," ");

	               cp.setRedline(rs.getString("date"));
	               custProds.add(cp);
	             }
	      	}catch (Exception ee){
ee.printStackTrace();
	      	}

	      }
	    }
	    return custProds;
	  }





  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


    public void setBulkSales(Map agents){
  this.connectionUp();
    
      
      
      String cmd =" SELECT agent,summa ,sales_last_year as last  FROM MJ_getAgentSales co where dataareaid = '110' ";

         if (isDebug()) System.out.println(cmd);
            try {
                Statement stmt =  this.getNewConnection().createStatement();
                ResultSet	      rs = stmt.executeQuery(cmd);
                while(rs.next()) {
                  String agentID = rs.getString("agent").trim();
                  Agent agent = (Agent)agents.get(agentID);
                  int see = (int)rs.getDouble("summa");
                  int last = (int)rs.getDouble("last");
                  if (agent != null){
                 //   agent.setMonthSales((int)rs.getDouble("summa"));
                  //  agent.setStart("Pardavimai einami metai " + see + " €" +"|"
                    //		+ "Pardavimai praeiti metai  : " + last + " €" +"|" + agent.getStart());
                  }
                  
                }
                rs.close();
                con.close();
            } catch (Exception e) {
                getParent().showError(e);
             }
          }

    public double getFoodSales(String agentCode) throws SQLException{
      return 0;
    }

    public double getAlcoSales(String agentCode) throws SQLException{
      return 0;
    }

    public ManagerData getManagerData() throws SQLException{
      
      return null;
    }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
    }


  public String getStartingTime(){
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, - Integer.parseInt(getParent().getProps().getProperty("months")));
    int month = cal.get(Calendar.MONTH) + 1;
    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    String sqlDate = cal.get(Calendar.YEAR) + "-" + month + "-" + day;
    return sqlDate;
  }

  public String getDriverVersion(){
    return "AxaLitArga";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent){



	       String sql2 =  "SELECT sal.orderid AS arve, sal.orderdate AS kuupaev,cus.name AS klient, count(rid.itemid) AS summa  " +
	    " FROM perinttelematable sal INNER JOIN custtable cus ON sal.CUSTACCOUNT=cus.accountnum  " +
	    " INNER JOIN perinttelemaline rid ON sal.orderid=rid.orderid  WHERE sal.dataareaid='dun' AND cus.dataareaid='dun'  AND rid.dataareaid='dun' AND cus.salesrep ='" + agent.getID() + "' AND " +
	    " sal.ORDERDATE > getdate()-0.8  and referenceno = '' GROUP BY sal.orderid, sal.orderdate , cus.name ORDER BY sal.orderdate";

	    StringBuffer sb = new StringBuffer();
	    sb.append("\n\n\n Vormistatud tellimused:\n\n");

	      try {
	                  ResultSet	      rs2 = stmt.executeQuery(sql2);
	                  while (rs2.next()) {
	                  sb.append("(" + rs2.getString("arve").trim() + ") [" + rs2.getString("kuupaev").substring(0,10) + "] - " + rs2.getString("klient") + "  Ridu: " + rs2.getDouble("summa") + "\n");
	                  }

	        //stmt = con.prepareStatement(sql);
	        //stmt.sets(1, agent.getObject());
	          //stmt.setString(2, agent.getObject());
	                  /* 	    String sql =" SELECT  (SELECT SUM(PHYSICALINVENT)FROM INVENTSUM summ INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid WHERE summ.DATAAREAID='bal' and dim.DATAAREAID='bal'" +
	"AND CLOSED = 0 AND dim.inventlocationid ='" + agent.getObject() + "' " +
	"AND summ.itemid=inv.itemid GROUP BY summ.itemid) AS ladu,SUM(sal.qty) AS kogus,avg(sal.lineamountmst/sal.qty) AS myygihind, " +
	"inv.itemname AS nimetus, inv.itemqtyinabox AS tykke " +
	"FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid  WHERE " +
	"inv.dataareaid='bal' AND sal.dimension='" + agent.getID() + "' AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate()) AND DAY(sal.invoicedate) = DAY(getdate()) " +
	"GROUP BY inv.itemname ,inv.itemqtyinabox,inv.itemid ORDER BY inv.itemname ";
   ResultSet	      rs = stmt.executeQuery(sql);
	          ResultSetMetaData md = rs.getMetaData();
	          sb.append("Nimi  \t\tLadu\t\tMüüdud\t\tSumma\n");
	          sb.append("---------------------------------------------------\n");

	          double totalKokku = 0d;

	          while (rs.next()) {
	            int ladu = rs.getInt("ladu");
	            int kogus = rs.getInt("kogus");
	            double hind = round(rs.getDouble("myygihind"), 2);
	            double kokku = round(kogus*hind, 2);
	            totalKokku = round(totalKokku + kokku, 2);

	            sb.append(rs.getString("nimetus").trim() + " " + rs.getInt("tykke")+ "\n");

	            sb.append("\t\t\t\t" + ladu + "\t\t" + kogus + "\t\t" +
	              kokku + "\n\n");
	           }

	          sb.append("---------------------------------------------------\n");
	          sb.append("\tPäeva müük kokku: " + totalKokku + " EUR");

*/
	                  } catch (Exception e) {
	                      getParent().showError(e);
	                  }


	      return sb.toString();
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
  private String url = "jdbc:odbc:AXA";
  private String url2 = "jdbc:odbc:report";
  private Connection con;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
