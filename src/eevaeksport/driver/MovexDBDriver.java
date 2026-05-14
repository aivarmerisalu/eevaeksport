package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalLineWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.DefectiveLineWrapperMeri;
import eevaeksport.report.EventInfo;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0f
 */

public class MovexDBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater,Discounter,EventInfo,PictureMaker{
  public MovexDBDriver(EksportPanel parent){
    super(parent);
    if(parent.getParentFrame().isDaySales()){
         this.subjectFilesTo = getParent().getString("senddatamsg");
    } else {this.subjectFilesTo = parent.getString("filemsg");}
    String databaseDriverName = "oracle.jdbc.driver.OracleDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
    //  con = DriverManager.getConnection("jdbc:oracle:thin:@10.10.10.56:1521:M3EEPROD", getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
     // con = DriverManager.getConnection("jdbc:oracle:thin:@10.10.100.102:1521:GM3DEV2", "ALC", "alc1807");
      con = DriverManager.getConnection("jdbc:oracle:thin:@10.10.100.97:1521:GM3REP1", "ALC", "alc1807");
       
      stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
	company = getParent().getProps().getProperty("companycode");
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  String company = null;

  
          public String getDealerCustomerCSV(String agent){
           return null;
           }
          
  public void connectionUp(){
  }

 
  public String getAssortmentStr(Agent agent){
		StringBuffer sb = new StringBuffer();
		
		String cmd = "Select cuno as klient,okcfc6 as klgrupp,lbl_id as abi , " +
				" lbl_nm as abinimi ,mmitno as toode,res as grupp , smcd as agent,  color, weight as bold " +
				" from alc.mj_assortment where res = 'c_MYYMATA'";
		
	
		String oldabi = null;
		String oldgr = null;
		try {
			
			Connection con = getNewConnection2();
			System.out.println(cmd);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String custid = Converter.cleanLitLatEst(rs.getString("klient"));
				if(custid == null || custid.trim().length() == 0){
					custid = " ";
				}else{
					custid = custid.trim();
				}
				
				
				String type = Converter.cleanLitLatEst(rs.getString("klgrupp").trim());
				if(custid.trim().length() > 0){//kui kliendi kood on olemas
					type = " ";
				}else{
					type = type.trim();
				}
				
				String abi = Converter.cleanLitLatEst(rs.getString("abi"));
				if(abi == null || abi.trim().length() == 0){
					abi = " ";
				}else{
					abi = abi.trim();
				}
				
				String id = rs.getString("toode");	
				String prgrupp = rs.getString("grupp");	
				if(prgrupp == null || prgrupp.trim().length() == 0){
					prgrupp = " ";
				}else{
					prgrupp = prgrupp.trim();
				}
				
				String ag = rs.getString("agent");	
				if(ag == null || ag.trim().length() == 0){
					ag = " ";
				}else{
					ag = ag.trim();
				}
				
				String color = rs.getString("color");
				if(color != null && color.trim().equals("roheline") ){
					color = "g";
				} else if(color != null && color.trim().equals("must") ){
					color = "l";
				} else if(color != null && color.trim().equals("punane") ){
					color = "r";
				} else if(color != null && color.trim().equals("sinine") ){
					color = "b";
				} else {
					color = " ";
				}
				
				if(color.trim().length()> 0){
					String ww = rs.getString("bold");
					if(ww != null && ww.trim().equals("1")){
						color = color.toUpperCase();
					}
						
				}
				
             if(abi.trim().length() > 0){
            	 if(oldabi == null){
            			sb.append(custid + ";" + Converter.cleanLitLatEst(type + "; ;" + abi + "; ;" + prgrupp + "; ;" + color + ";," + ag + ",")
        						+ "\n");
        		 }else if(!oldabi.equals(abi)){
                    sb.append(custid + ";" + Converter.cleanLitLatEst(type + "; ;" + abi + "; ;" + prgrupp + "; ;" + color + ";," + ag + ",")
     						+ "\n");
            	 }else if(oldabi.equals(abi) && !oldgr.equals(prgrupp)){
                     sb.append(custid + ";" + Converter.cleanLitLatEst(type + "; ;" + abi + "; ;" + prgrupp + "; ;" + color + ";," + ag + ",")
      						+ "\n");
             	 }
             }
		
			sb.append(custid + ";" + Converter.cleanLitLatEst(type + "; ;" + id + "; ;" + prgrupp + ";" + abi + ";" + color + ";," + ag + ",")
						+ "\n");
			oldabi = abi;
			oldgr = prgrupp;
			
			}
			
			

			rs.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
      }

  
  public String getAssortmentStrOld(Agent agent){
		StringBuffer sb = new StringBuffer();
		
		String cmd = "Select cuno as klient,okcfc6 as klgrupp,lbl_id as abi , " +
				" lbl_nm as abinimi ,mmitno as toode,res as grupp , smcd as agent,  color, weight as bold " +
				" from alc.mj_assortment_grps_tbl";
		String oldabi = null;
		String oldgr = null;
		try {
			
			Connection con = getNewConnection2();
			System.out.println(cmd);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String custid = Converter.cleanLitLatEst(rs.getString("klient"));
				if(custid == null || custid.trim().length() == 0){
					custid = " ";
				}else{
					custid = custid.trim();
				}
				
				
				String type = Converter.cleanLitLatEst(rs.getString("klgrupp").trim());
				if(custid.trim().length() > 0){//kui kliendi kood on olemas
					type = " ";
				}else{
					type = type.trim();
				}
				
				String abi = Converter.cleanLitLatEst(rs.getString("abi"));
				if(abi == null || abi.trim().length() == 0){
					abi = " ";
				}else{
					abi = abi.trim();
				}
				
				String id = rs.getString("toode");	
				String prgrupp = rs.getString("grupp");	
				if(prgrupp == null || prgrupp.trim().length() == 0){
					prgrupp = " ";
				}else{
					prgrupp = prgrupp.trim();
				}
				
				String ag = rs.getString("agent");	
				if(ag == null || ag.trim().length() == 0){
					ag = " ";
				}else{
					ag = ag.trim();
				}
				
				String color = rs.getString("color");
				if(color != null && color.trim().equals("roheline") ){
					color = "g";
				} else if(color != null && color.trim().equals("must") ){
					color = "l";
				} else if(color != null && color.trim().equals("punane") ){
					color = "r";
				} else if(color != null && color.trim().equals("sinine") ){
					color = "b";
				} else {
					color = " ";
				}
				
				if(color.trim().length()> 0){
					String ww = rs.getString("bold");
					if(ww != null && ww.trim().equals("1")){
						color = color.toUpperCase();
					}
						
				}
				
           if(abi.trim().length() > 0){
          	 if(oldabi == null){
          			sb.append(custid + ";" + Converter.cleanLitLatEst(type + "; ;" + abi + "; ;" + prgrupp + "; ;" + color + ";," + ag + ",")
      						+ "\n");
      		 }else if(!oldabi.equals(abi)){
                  sb.append(custid + ";" + Converter.cleanLitLatEst(type + "; ;" + abi + "; ;" + prgrupp + "; ;" + color + ";," + ag + ",")
   						+ "\n");
          	 }else if(oldabi.equals(abi) && !oldgr.equals(prgrupp)){
                   sb.append(custid + ";" + Converter.cleanLitLatEst(type + "; ;" + abi + "; ;" + prgrupp + "; ;" + color + ";," + ag + ",")
    						+ "\n");
           	 }
           }
		
			sb.append(custid + ";" + Converter.cleanLitLatEst(type + "; ;" + id + "; ;" + prgrupp + ";" + abi + ";" + color + ";," + ag + ",")
						+ "\n");
			oldabi = abi;
			oldgr = prgrupp;
			
			}
			
			

			rs.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
    }

  
  
	public Vector getLastThreeData() {
		return null;
	}

  public String getSaveDebtInfo() {

	  String  cmd = "SELECT * FROM alc.mj_blocked_payers";
		String cmd3  = "SELECT * FROM alc.mj_positive_overdue";
		String cmd4 = "SELECT * FROM alc.mj_negative_all_debt";

		if (isDebug()) System.out.println(cmd);
		TreeMap custs = new TreeMap();




		StringBuffer products = new StringBuffer();
		try {

		Connection con1 = getNewConnection();
		Statement st = con1.createStatement();
		ResultSet rs = st.executeQuery(cmd3);

		while (rs.next()) {

		String kood = rs.getString("klient").trim();
		double volg = rs.getDouble("debt");
		Customer custw = new Customer (kood);
		custw.setDebt(volg);
		if(volg>0){
			custs.put(kood,custw);
		}
		}

		//Siin lahutatakse maha negatiivsed arve saldod
		rs = st.executeQuery(cmd4);
		while (rs.next()) {

			String kood = rs.getString("klient").trim();
			double volg = rs.getDouble("debt");

			Customer custw = (Customer)custs.get(kood);
			if(custw != null){
			custw.setDebt(custw.getDebt() + volg);
			}

			}




		//siin pannakse peale PLOKKID

		rs = st.executeQuery(cmd);
		while (rs.next()) {
			String kood = rs.getString("klient").trim();
		    Customer cus =  (Customer)custs.get(kood);
		    if(cus != null){
		    	cus.setSalesBlock(true);
		    }else{
		    	Customer cusnew = new Customer(kood);
		    	cusnew.setSalesBlock(true);
		    	custs.put(kood,cusnew);
		    }
		 }

		rs.close();
		} catch (Exception e) {
		getParent().showError(e);
		}
		Iterator     codes = custs.values().iterator();
		while (codes.hasNext()){
		Customer cust = (Customer)codes.next();
		String bl = "0";
		if(cust.isSalesBlock()){
			bl = "1";
			}
		if(cust.getDebt() != -1 && cust.getDebt() != -2){
			if(cust.getDebt() != -3) {
				 products.append(cust.getID() + ";" + getParent().getNF().format(cust.getDebt()) + ";" + bl + "\n");
			}
			
		  }
		}

		return products.toString();

   }

  public String getSaveStockInfo(){

	  
    String  cmd = " SELECT * FROM alc.MJ_LAOSEIS " ;

      if (isDebug()) System.out.println(cmd);


      StringBuffer products = new StringBuffer();
      try {

        Connection con1 = getNewConnection();
        Statement st = con1.createStatement();
        ResultSet rs = st.executeQuery(cmd);

        while (rs.next()) {

          String kood = rs.getString("toode").trim();
        //  String dealer = rs.getString("LADU").trim();
              products.append(kood +  ";");
              products.append(" ;");
             // products.append(getParent().getNF().format(rs.getDouble("stock")) + ";" + dealer +  "\n");
              products.append(getParent().getNF().format(rs.getDouble("stock")) +   "; ; \n");
          }
          rs.close();
      } catch (Exception e) {
          getParent().showError(e);
      }
      makeTaskFile(parent.getProps().getProperty("stockrefdir"));
      return products.toString();
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
	 
	  String test = getParent().getProps().getProperty("testing");
		 if("true".equals(test)){
			  try {
				eventInfo(null);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	
    String cmd = "SELECT * FROM alc.mj_pda_owners";
    
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
  aHash = new HashMap();
    agents1 =  new TreeMap();
    
    String agentStr = getParent().getProps().getProperty("servicemen");

	if(agentStr != null){

	    StringTokenizer st = new StringTokenizer(agentStr, ";");
	       while(st.hasMoreTokens()){
	       	Agent agent = new Agent(st.nextToken());

			String agentAGENTS = getParent().getProps().getProperty(agent.getID());
     	    StringTokenizer stag = new StringTokenizer(agentAGENTS, ";");
		    agent.setName(stag.nextToken());
		

		       while(stag.hasMoreTokens()){
                agent.addDubbler(stag.nextToken());
		       }
				agents.put(agent.getName(), agent);
				agents1.put(agent.getID(), agent);
	       }
	}
    
    try {
    	//Connection con = DriverManager.getConnection("jdbc:oracle:thin:@10.10.100.102:1521:GM3DEV2", "ALC", "alc1807");
        Connection con = DriverManager.getConnection("jdbc:oracle:thin:@10.10.100.97:1521:GM3REP1", "ALC", "alc1807");
    	
    //	Connection con = DriverManager.getConnection("jdbc:oracle:thin:@10.10.10.56:1521:M3EEPROD", getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
        Statement stmt = con.createStatement();
     
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = rs.getString("NIMI").trim();
          agent.setName(name);
          agent.setIsPilot(true);
          String region = rs.getString("region");
          if(region == null){
        	  region = "";
          }
          /*
          try{
        	  if(!region.equals("")){
        		
        	  String cmd2 ="SELECT regioon, toode, ale FROM (SELECT dxobv1 AS regioon, dxobv2 AS toode, dxdisp AS ale FROM mvxjdta.ogdmtx " +                                                                                                            
" WHERE dxdisy='100' AND dxdipo='4' AND dxprex=' 1' AND dxcmpn!='*' AND dxdisp!='0' AND dxobv2 between '010101' AND '189999'" +
" AND dxfvdt IN (SELECT Max(dxfvdt) FROM mvxjdta.ogdmtx WHERE dxdisy='100' AND dxdipo='4' AND dxprex=' 1')" +
" UNION  SELECT d8obv1 AS regioon, d8obv2 AS toode, d8disp AS ale FROM mvxjdta.ogcmtx "+                                                             
" WHERE d8disy='100' AND d8dipo='4' AND d8prex=' 1' AND d8disp!='0' AND d8obv2 between '010101' AND '189999' AND " +
"   d8lvdt>='" + getDateStr() + "')  WHERE regioon = '" + region.trim() + "'" ;
        	  
        	 // System.out.print(cmd2);
        	  Statement s2 = con.createStatement();
        	  ResultSet rs2 = s2.executeQuery(cmd2); 
        	  while (rs2.next()){
        		  
        		  Toode tt = new Toode(rs2.getString("toode"));
        		  tt.setKogus(rs2.getDouble("ale"));
        		//  agent.addDiscountProds(tt);
        	  }
        	  rs2.close();
        	  s2.close();
        	  //System.out.println("Discprods " + agent.getID() + " " + agent.getDiscountProds());
        	  
        	  }
          }catch(Exception r){
        	  r.printStackTrace();
          }*/
          
          
          
          Iterator enums = agents1.keySet().iterator();
          while (enums.hasNext()) {
           Agent a =(Agent) agents1.get((String)enums.next());
         //  System.out.println( "Esimene " + a.toString());
          // System.out.println( "Teine " + agent.getID());
           if(a.getDubbler()!= null && a.getDubbler().size()>0){
        	   Enumeration en = a.getDubbler().elements();
        	   while(en.hasMoreElements()){
        		   String aa = (String)en.nextElement();
        		   if(aa.equals(agent.getID())){
            		   agent.addDubbler(a.getID());
            		  
        		   }
        	   }
           }
         }
          
      
          agents.put(name, agent);
          aHash.put(agent.getID(), agent);
        }
      

    rs.close();
    stmt.close();  
    con.close();

    } catch (Exception e){
        getParent().showError(e);
      }
    
	
  

    return agents;
  }

  private String getDir(){
	    String dir = getParent().getProps().getProperty("readin");
	    if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	    return dir;
	  }


      public String getDistribution() {

        StringBuffer sb = new StringBuffer();

        try {
          Connection con = getNewConnection();
          Statement st = con.createStatement();
          String cmd = "select mmitno as kood ,mmitds AS Nimetus , mmdim1 from mvxjdta.mitmas where MMCFI5 = 'Y'";

          if (isDebug())
          System.out.println(cmd);
          ResultSet rs = st.executeQuery(cmd);
         while(rs.next()){
           sb.append(rs.getString("kood") + ";" + rs.getString("nimetus") + "\n");
         }
         st.close();
         con.close();
       } catch (Exception e){}
      //   System.out.println(sb.toString());
        return sb.toString();
      }

 private void makeTaskFile(String dir){
	 try{
		 
		  String sql2 = "select lineid ,taskid,agentid,agentnam,clid, sentdate,senttime,reminder,photoname," +
		  		"status,mestext,headertext,res1 from alc.mj_task where sentdate > sysdate-360 order by clid,sentdate";
		  
	Connection con2 = this.getNewConnection2();
	    Statement stt = con2.createStatement();
	ResultSet rr = stt.executeQuery(sql2);
	StringBuffer sb = new StringBuffer ();
	sb.append("custid;kell;agentname;agentcode;header;message;answerid;messageid;reminder;fotoloc;done\n");
	while (rr.next()){
		String lineid =   rr.getString("lineid");
		String taskid =   rr.getString("taskid");
		String agentid =  rr.getString("agentid");
		String agentnam = rr.getString("agentnam");
		String clid =     rr.getString("clid");	
		String sentdate = rr.getString("sentdate");
		String senttime = rr.getString("senttime");
		Date reminder =   rr.getDate("reminder");
		String photoname = rr.getString("photoname");
		String status = rr.getString("status");
		String mestext =  rr.getString("mestext");
		String headertext = rr.getString("headertext");
		String donetime = rr.getString("res1");
     
		long lll = 0;
		boolean goes = true;
        if(donetime != null && donetime.trim().length() > 0){
        	
        	try{
        		lll = Long.parseLong(donetime.trim());
        		if(Calendar.getInstance().getTimeInMillis() - 518400000  > lll){
        			goes = false;
        		}
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        }
        
		if(clid == null || clid.trim().length() == 0){
			clid = " ";
		}else{
			clid = clid.trim();
		}
		
		if(lineid == null || lineid.trim().length() == 0){
			lineid = " ";
		}else{
			lineid = lineid.trim();
		}
		
		if(taskid == null || taskid.trim().length() == 0){
			taskid = " ";
		}else{
			taskid = taskid.trim();
		}
		
		if(senttime == null || senttime.trim().length() == 0){
			senttime = " ";
		}else{
			senttime = senttime.trim();
		}
		
		
		if(photoname == null || photoname.trim().length() == 0){
			photoname = " ";
		}else{
			photoname = photoname.trim();
		}
		
		if(mestext == null || mestext.trim().length() == 0){
			mestext = " ";
		}else{
			mestext = mestext.trim();
		}
		
		
		if(headertext == null || headertext.trim().length() == 0){
			headertext = " ";
		}else{
			headertext = headertext.trim();
		}
		
		String remi = "0";
		if(reminder != null){
			remi = (reminder.getTime() + 54000000) + "";
		}
		if(goes){
			sb.append(clid + ";" + sentdate.substring(0,16) + ";" + agentnam + ";" +agentid + ";" +
					mestext.replaceAll("\\r\\n|\\r|\\n", "|")	+ ";" + headertext.replaceAll("\\r\\n|\\r|\\n", "|") + ";" + lineid + ";" + taskid + ";" + remi + ";" + photoname+ ";"
					  + status + "\n");
		}else{
			//System.out.println("Ei l�he kaasa " + lineid );
		}
	
	}
	rr.close();
	stt.close();
	con2.close();


	  
	    File f = new File( "customertask.txt");
	    if (f.exists())f.delete();
	      
	    Converter.saveFileInUTF8("customertask.txt", sb);
		if( dir != null){
			 f = new File(dir + "customertask.txt");
			    if (f.exists())f.delete();
			 Converter.saveFileInUTF8(dir + "customertask.txt", sb);
		}
		  
	 }catch(Exception e){
		 e.printStackTrace();
	 }
 }

 HashMap sortimendid = null;
 
 
   private TreeMap getAnswers(){
	   TreeMap res = new TreeMap();
	   try{
		
	   Connection con = getNewConnection();
	      Statement st = con.createStatement();
	    
	      String sqlr = "SELECT clid ,objective,promotion FROM alc.mj_mootmine  order by clid, sort1,sort2";
	      
		     System.out.println(sqlr);
		  ResultSet rs = st.executeQuery(sqlr);
		  while(rs.next()){ 
			  String klient = rs.getString("clid").trim();
			  String eesm = rs.getString("objective").trim();
			  String tulem = rs.getString("promotion");
			  if(tulem == null){
				  tulem = "";
			  }else{
				  tulem = "(" + tulem + ") ";
			  }
			  
			  if(res.get(klient) != null){
				  res.put(klient,res.get(klient)+ "|" + tulem + eesm.replaceAll("\\r\\n|\\r|\\n", "|"));
			  }else{
				  res.put(klient,tulem + eesm.replaceAll("\\r\\n|\\r|\\n", "|"));
			  }
			  
			 }
		  rs.close();
		  st.close();
		  con.close();
		   
	   }catch(Exception r){
		   r.printStackTrace();
	   }
	   
	   return res ;
   }
 
  public TreeMap getCustomers(Agent agent) {
	  
	  
	  
	  TreeMap answInfo = getAnswers();
	  
	  sortimendid = new HashMap();
      
      File fff = new File(this.getExcelDir() + "/sortiment.xlsx" );
      TreeMap groups = new TreeMap(); 
  	try{
          FileInputStream ff = new FileInputStream(fff);
  		XSSFWorkbook workbook = new XSSFWorkbook((ff));
  		XSSFSheet sheet = workbook.getSheet("shops");
  		XSSFRow row = null;
  		
  		row = sheet.getRow(0);
  	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
  		
  		
  		
  		for(int a = 1 ; a < sheet.getLastRowNum()+1;a++){
  			try{
  			//	Loeme sisse rea
  				row = sheet.getRow(a);
  				if (row.getCell((short) 0) != null) {
  				


  	    			   String kood = null;
  	    			  if(row.getCell((short)0) != null){
	    	    			  	try{
	    	    			  	  kood = row.getCell((short)0).getStringCellValue();  
	    	    			  	 }catch (Exception e){
	    	    			   	  kood = ((int)row.getCell((short)0).getNumericCellValue())+"";  
	    	    	    		} 
	    	    			  //	System.out.println("kood0 " + kood );
	    	    			  	 if(kood.indexOf(".")!= -1){
	    	    			  		 int ii = kood.indexOf(".");
	    	    			  	 	 kood = (kood.substring(0,ii) + kood.substring(ii+1,kood.indexOf("E")));	
	    	    			  	 }
	    //
	    	//    				System.out.println("kood1 " + kood );
	    	    		    	
	    	    			  	 String sort = null;
	    	    			  	try{
	    	    			  		sort = row.getCell((short)1).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			  		sort = ((int)row.getCell((short)1).getNumericCellValue())+"";  
		    	    	    		} 
		    	    			   if(sort.indexOf(".")!= -1){
  	    	    			  		 int ii = sort.indexOf(".");
  	    	    			  	 	 sort = (sort.substring(0,ii) + sort.substring(ii+1,sort.indexOf("E")));	
  	    	    			  	 }
		    	    			   
	    	    			  	 groups.put(kood,sort);
	    	    			  	if(kood.length() == 9 ){
		    	    				   kood = kood + "0";
		    	    				   groups.put(kood,sort);
		    	    			   }
  	    			  
  	    				
  				}	   
  				}
 			}catch (Exception e){
  			//	System.out.println(row.getCell((short)a).getNumericCellValue());
  			}
  		}
  		
  	//	System.out.println(groups.toString());
  
  	 }catch (Exception e){
  		e.printStackTrace();
  	}

  	
	  
	  
	  
	  
	  
	  
	  
	  
	  
    TreeMap customers = new TreeMap();
    Vector hengapoed = new Vector ();
    HashMap lisapoed= new HashMap();
    Vector lkapoed = new Vector ();
     HashSet codes = new HashSet();
     
     
     File f = new File(this.getExcelDir() + "erialused.xlsx");
     TreeMap<String,String> erialused =  Converter.getErialused(f);
     System.out.println("TR " + erialused.toString());
 	
	  String inP = parent.getProps().getProperty("doc.origpath");
    String outP = parent.getProps().getProperty("doc.destination");
	
    try{
	  
  makeTaskFile(null);	
	  
      Connection con = getNewConnection();
      Statement st = con.createStatement();
    
      String   sqlr = " SELECT smcd as agent ,saaja FROM alc.mj_hengapoed ";
      
	     System.out.println(sqlr);
	  ResultSet rs = st.executeQuery(sqlr);
	  while(rs.next()){  
		  String agen = rs.getString("agent").trim();
		  String saaja = rs.getString("saaja").trim();
		  if(lisapoed.get(saaja)!= null){
			  Vector vv =  (Vector)lisapoed.get(saaja);
				 vv.addElement(agen);
				  lisapoed.put(saaja,vv);	  
		  }else{
			 Vector vv =  new Vector() ;
			 vv.addElement(agen);
			  lisapoed.put(saaja,vv);
		  }
     	
      }
	  
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
    
    try {
      Connection con = getNewConnection();
      Statement st = con.createStatement();
      
      
      String cmd6 = "SELECT mmitno as kood ,mmitds as nimi FROM mvxjdta.mitmas WHERE mmitno LIKE '26%' AND mmstat='20' "; 
       
       StringBuffer buf = new StringBuffer();
       
       Statement s2 = con.createStatement();
   	  ResultSet rs2 = s2.executeQuery(cmd6); 
   	  while (rs2.next()){
   		  buf.append("PV" + rs2.getString("kood")  + ";" + Converter.enCodeEst(rs2.getString("nimi")) +";12\n"  );
   	  }
       
       
       
         if(buf.length() > 2){
             f = new File(getDir() + "inventory.txt");
             if (f.exists())f.delete();
               try {
         		PrintWriter os = new PrintWriter(new FileWriter(
         				getDir() + "inventory.txt", true));
         		os.write(buf.toString());
         		os.close();
         	  }catch (IOException ex) {
         		ex.printStackTrace();
         	  }
          }
       
         
         buf = new StringBuffer();
         String cmd7 = "SELECT KOOD,TEHASET,VARA FROM alc.tmp_pv2013 " +
           " ORDER BY KOOD";
   	   rs2 = s2.executeQuery(cmd7); 
   	  while (rs2.next()){
   		  buf.append("PV" + rs2.getString("KOOD")  + ";" + rs2.getString("TEHASET")+ "|" + rs2.getString("VARA") +"\n"  );
   	  }
       
         if(buf.length() > 2){
              f = new File(getDir() + "custdetch.txt");
             if (f.exists())f.delete();
               try {
         		Converter.saveFileInUTF8(getDir() + "custdetch.txt", buf);
         	  }catch (Exception ex) {
         		ex.printStackTrace();
         	  }
          }
         
         buf = new StringBuffer();

         
         
         String cmd8 = "SELECT mmitno as kood,mmitds as nimi FROM mvxjdta.mitmas WHERE mmitno" +
         		" like '6_____' and mmcono=200  AND mmstat='20' ORDER BY mmitno ";
         
         cmd8 = "SELECT mmitno as kood ,mmitds as nimi FROM mvxjdta.mitmas WHERE mmitno like '26_____' and mmcono=100 and mmpdln='200' and mmstat=20  ORDER BY mmitno ";
   	   
         rs2 = s2.executeQuery(cmd8); 
   	  while (rs2.next()){
   		  String koo = rs2.getString("kood");
   		  if(koo != null && koo.trim().length() >0 ){
   			  buf.append( koo  + ";" + Converter.enCodeEst(rs2.getString("nimi")) +";1\n"  );
   		  }
   		
   	  }
      
   	  String cmd9 = " SELECT drrout as kood ,drtx40 as nimi FROM mvxjdta.droute WHERE drcono=100"
   			+ " and length(drrout)=4 and drrout between '2000' AND '2999' ORDER BY drrout ";

rs2 = s2.executeQuery(cmd9); 
while (rs2.next()){
 String koo = rs2.getString("kood");
 if(koo != null && koo.trim().length() >0 ){
	  buf.append( koo  + ";" + Converter.enCodeEst(rs2.getString("nimi")) +";2\n"  );
 }
}
   	  
       
         if(buf.length() > 2){
              f = new File(getDir() + "eventprods.txt");
             if (f.exists())f.delete();
               try {
         		Converter.saveFileInUTF8(getDir() + "eventprods.txt", buf);
         	       }catch (Exception ex) {
         		    ex.printStackTrace();
         	       }
          }
         
         
      
      
      
      
      
      
      

         String cmd2 = "SELECT * FROM alc.MJ_CUSTOMERS";

       System.out.println(cmd2);
      ResultSet rs = st.executeQuery(cmd2);
      HashMap custMap = new HashMap();
      
         while(rs.next()){
    	  
    	  String klgrupp = rs.getString("klgrupp");
    	  if (klgrupp != null ){
    		  klgrupp = Converter.cleanLitLatEst(klgrupp.trim());
    	  }else{
    		  klgrupp = "";
    	  }
          String accountnum = rs.getString("kood").trim();
          Customer cust = new Customer(accountnum);
        
          if(answInfo.get(accountnum) != null){
        	  	cust.addToCustDataVec(accountnum+ ";answinfo; ;Viimase hindamise tulemus |"   +  (answInfo.get(accountnum)+""));
        	      
          }
          
          cust.setShowfullpricelist(true);
          codes.add(accountnum);
          String name = rs.getString("nimi").trim();
          cust.setName(Converter.enCodeEst(name));
          String aadress1 = rs.getString("aadress1");
          if (aadress1 != null) {aadress1 = aadress1.trim();} else {aadress1 = " ";}
          
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress1 ),"Estonia")); // + " " + aadress2
          String visit = rs.getString("visit");
          if(visit != null){
            cust.setVisit(visit.trim());
          }
          String info = rs.getString("info");
          if (info != null){cust.setInfo(cust.getInfo() + Converter.enCodeEst(info.trim()));}
         
         // System.out.println(limiit);
          //if (limiit != null){cust.setCredit(info.trim());}
          String ag = rs.getString("agent").trim();
         if(hengapoed.contains(accountnum) 
        		/* &&   kommenttisin v�lja 
        		 (name.indexOf("RIMI") != -1 ||
        				 klgrupp.equals("ALKO") ||
        				 klgrupp.equals("HYPER") ||
        				 klgrupp.equals("SUPER") ||
        				 klgrupp.equals("SUURKAUP") ||
        				 klgrupp.equals("VAIKEKAUPA") ||
                         klgrupp.equals("VAIKEKAUPB")
        		 )*/
        		 ){
             cust.addAgent(new Agent("2154"));
         }
         
       //  System.out.println("grupp1 " + klgrupp.trim());
         if(klgrupp.trim().length() > 1 ){
        //	 System.out.println("grupp " + klgrupp.trim());
        	 cust.setGroup(klgrupp.trim());
        	 cust.setCity("Kliendi Grupp :" +klgrupp.trim() + "|" );
        	 if(!codes.contains("_"  + klgrupp.trim())){
        	 	  codes.add("_"  + klgrupp.trim());
        	 }
         }
         
         if(erialused.get(cust.getID()) != null) {
        	 if(cust.getCity() != null && cust.getCity().trim().length() > 0 ) {
        		 cust.setCity(cust.getCity() + "|Erialused : |" + erialused.get(cust.getID()));
        	 }else {
        		 cust.setCity("Erialused : |" + erialused.get(cust.getID()));
        	 }
         }
         
          if(ag!=null){

        	  if(ag.equals("LKA") || ag.equals("HEN") ){
        		//if(  lkapoed.contains(cust.getID()))
        		 	cust.addAgent(new Agent(ag));
        	    }else{
        		 	cust.addAgent(new Agent(ag));
        		}
        	  
         		Agent agg = ((Agent)this.aHash.get(ag.trim()));
				if(agg != null && agg.getDubbler().size()>0){
					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
						Agent aggx = new Agent((String)((Agent)this.aHash.get(ag.trim())).getDubbler().elementAt(i));
						 if(aggx.getID().equals("LKA") || aggx.getID().equals("HEN")){
							// if(lkapoed.contains(cust.getID())){
								 cust.addAgent(aggx);
							// }
							
			        	  }else{
			        		 	cust.addAgent(aggx);
			        	  }
					}
				}
          }
          
          
          
          String gro = (String) groups.get(cust.getID());
          if( gro != null && gro.trim().length() > 0){
          	cust.addFixSortCode(gro);
          	//System.out.println("Settin fixsorti " + gro);
          }
          
          
          
          cust.setPayer(rs.getString("payer").trim());
          cust.setCustType(cust.getPayer());
      
          
          String logist = rs.getString("ring") ;
          if (logist != null && logist.trim().length()>0) {
            if (logist.trim().length()<2){
            logist =  "00" + logist.trim();
            }else if (logist.trim().length()<3){
             logist =  "0" + logist.trim();
            }
            
          }
          String  blokk = rs.getString("blok2") ;

          if(blokk != null && !blokk.trim().equals("0")){
             cust.setSalesBlock(true);
          }
          
          String  ettem = rs.getString("blok3") ;

          if(ettem != null && ettem.trim().equals("100")){
          cust.setSalesBlockPrepay(true);
          }

          String allagents = rs.getString("toall");
          
            if(allagents.trim().equals("1")|| accountnum.equals("80010")){
            Enumeration enums = getParent().getAgentsHash().elements();
             while (enums.hasMoreElements()) {
              Agent a =(Agent) enums.nextElement();
              cust.addAgent(a);
            }
            }
            String jelp = rs.getString("asendus");
            if (jelp != null && jelp.trim().length()>0){
            	
            	if(jelp.equals("LKA") || jelp.equals("HEN")){
            		//if(lkapoed.contains(cust.getID())){
            			cust.addAgent(new Agent(jelp.trim()));	
            		//}
        		 	
        	  }else{
        		 	cust.addAgent(new Agent(jelp.trim()));
        		}
            }
            
            
           if( lisapoed.get(cust.getID()) != null){
        	   Vector vv = (Vector) lisapoed.get(cust.getID());
        	   for(int u = 0;u < vv.size(); u++){
        		   cust.addAgent(new Agent(vv.elementAt(u)+""));
        	   }
           }
            
            String phone = rs.getString("phone");
            if (phone != null && phone.trim().length()>0){
            	cust.setPersonalData("Tel:" + phone.trim());
            }
            
            
            String raadr = rs.getString("OPCUA1");
            if (raadr != null && raadr.trim().length()>0){
            	cust.restStreet = (raadr.trim());
            }
            
            String raadr2 = rs.getString("OPCUA2");
            if (raadr2 != null && raadr2.trim().length()>0){
            	cust.restCity = (raadr2.trim());
            }
            String raadr3 = rs.getString("OPCUA3");
            if (raadr3 != null && raadr3.trim().length()>0){
            	if(cust.restCity == null){
            		cust.restCity = (raadr3.trim());
            	}else{
            		cust.restCity = cust.restCity + " " + (raadr3.trim());
            	}
            }
            
            String rind = rs.getString("OPPONO");
            if (rind != null && rind.trim().length()>0){
            	cust.restZipCode = (rind.trim());
            }
            cust.setCountry("Eesti");
            
            String tarne = rs.getString("TARNE_INFO");
            if (tarne != null && tarne.trim().length()>0){
            	cust.addCity("Tarne ring: " + tarne.trim());
            }
            
  // cust.setDebt(rs.getDouble("debt"));
          customers.put(name, cust);
          custMap.put(cust.getID(), cust);
    }

      if(rcompany != null){
    	//  Converter.sendCustomerJson(customers, company, false, null, null);
	    }
     
      
    //  Converter.prepareDocCatalogue(inP,outP,codes,true);

    String   cmd3 ="SELECT * FROM alc.mj_overdue_sum_view";
     if (isDebug()) System.out.println(cmd3);
        ResultSet rs1 = st.executeQuery(cmd3);
         while (rs1.next()) {
           String id = rs1.getString("saaja").trim();
           double debt = Double.parseDouble(rs1.getString("SUMMA").trim());
           Customer cust = (Customer) custMap.get(id);
           if (cust != null && debt != -1 && debt != -2) {
             cust.setDebt(cust.getDebt() + debt);
             if(cust.getDebt() == -3) {
            	 cust.setDebt(0);
             }
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
	  return
			 //  DriverManager.getConnection("jdbc:oracle:thin:@10.10.100.102:1521:GM3DEV2", "ALC", "alc1807");
       DriverManager.getConnection("jdbc:oracle:thin:@10.10.100.97:1521:GM3REP1", "ALC", "alc1807");
       
  //  return DriverManager.getConnection("jdbc:oracle:thin:@10.10.10.56:1521:M3EEPROD", getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
  
  private Connection getNewConnection2() throws SQLException{
	    return DriverManager.getConnection(url, "ALC", "alc1807");
	  }
  
  
  public TreeMap getProdGroupsAlternative() {
	  TreeMap   tm = new TreeMap();
	
	    
	    return tm;

	  
	}
  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "select GRP as liik ,GRPJRK as nimetus  from alc.mj_prodgroupfile where nr = 1";
    
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
        tm.put(rs.getString("liik").trim(),
        		Converter.enCodeEst( rs.getString("nimetus").trim()));
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}

    public TreeMap getProdGroupsSecond() throws SQLException {

      Connection con = getNewConnection();
      TreeMap   tm = new TreeMap();
      String    sql = "select GRP as liik ,GRPJRK as nimetus  from alc.mj_prodgroupfile where nr = 2";
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);

      while (rs.next()) {
          tm.put(rs.getString("liik").trim(),
        		  Converter.enCodeEst(rs.getString("nimetus").trim()));
      }
      rs.close();
      st.close();
      con.close();

      return tm;
  }


 private Hashtable prices = null;

  public TreeMap getProducts(Agent agent){

	  prices = new Hashtable();
  	Hashtable stocks = new Hashtable();
  	
  	//Leiame Laoseisud
  	 try {


        String  sql = " SELECT * FROM alc.MJ_LAOSEIS " ;

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
             }
             rst.close();
             stm.close();
             con1.close();

     } catch (Exception e) {
         getParent().showError(e);
     }

	 try {
  StringBuffer pvcodes = new StringBuffer();

	        String  sql = " SELECT MLBANO FROM alc.mj_EQUIPMENT GROUP BY MLBANO  " ;

	          if (isDebug()) System.out.println(sql);
	          
	             Connection con1 = getNewConnection();
	             Statement stm = con1.createStatement();
	             ResultSet rst = stm.executeQuery(sql);

	               while (rst.next()) {
	                 String toode = rst.getString("MLBANO").trim();
	                 pvcodes.append(toode+"\n");
	               }
	               
	             rst.close();
	             stm.close();
	             con1.close();

	             Converter.saveFile("pvcodes.txt", pvcodes.toString());
	             
	     } catch (Exception e) {
	         getParent().showError(e);
	     }

  	 
    // Leiame soodused 
     
     Vector abikad = new Vector();
     
     try {


         String  sql = "Select distinct lbl_id  as toode,lbl_nm as nimetus ," +
         		" t.MMspe2 as grupp from alc.mj_assortment_grps_tbl  a INNER JOIN  " +
              " mvxjdta.MITMAS t on t.MMITNO= a.MMITNO where lbl_id is not null" ;

           if (isDebug()) System.out.println(sql);
              Connection con1 = getNewConnection();
              Statement stm = con1.createStatement();
              ResultSet rst = stm.executeQuery(sql);
        int p = 0;
                while (rst.next()) {
                	p++;
                	String hh = p+"";
                	if(hh.length() == 1){
                		hh ="00"+hh;
                	}
                	if(hh.length() == 2){
                		hh ="0"+hh;
                	}
                  String toode = rst.getString("toode").trim();
                  String nimetus = rst.getString("nimetus").trim();
                  String grupp = rst.getString("grupp").trim();
                  
                  Product prod = new Product(toode);
                  prod.setName( nimetus +" (" + hh+ ")" );
                  if(grupp != null && grupp.trim().length() > 0){
                	  prod.setGroup(grupp.trim());
                  }
                  abikad.add(prod);
              //    System.out.println(toode+"  kampaanias");
              }
              rst.close();
              stm.close();
              con1.close();

      } catch (Exception e) {
          getParent().showError(e);
      }

    // (case when substr(t.MMITNO,1,1)='1' then substr(t.MMITNO,2,6) else t.MMITNO end)
    String    cmd = "SELECT * FROM alc.mj_item_master WHERE cucl='NON-HORECA'";
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {
      Connection con = getNewConnection();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(cmd);
   int counter = 1;
      while(rs.next()){

      	String kood = rs.getString("kood").trim();
        Product prod = new Product(kood);
        String abi = rs.getString("abi").trim();
        String nimetus = abi + " " + rs.getString("nimetus").trim();
    
        prod.setName(Converter.enCodeEst(nimetus).replace(':', '-'));
        
        prod.setInCases( (rs.getDouble("tykke")));
        prod.setPrice( (rs.getDouble("hind")));
        prod.setVat("24");
      
        prices.put(kood, prod.getPrice() +"");
        
        

        String grupp = rs.getString("grupp") ;
         if (grupp != null && grupp.trim().length()>0) {
           prod.setGroup(grupp.trim());
         }

         prod.setInUnit(rs.getDouble("inunit"));
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
        unit = unit.trim();
        prod.setUnit(unit);
        prod.setGroupSecond(unit);
        if(unit.equals("1/4E") || unit.equals("1/2E") ){
         prod.setDiffColor(true);
         }

        }

        String id = rs.getString("id");
       // System.out.println("*" + id.trim() + "*");
            if (id.trim().equals("05") || id.trim().equals("06") ){
            prod.setSentExtra("SE");
            } else if (id.trim().equals("09")) {
              prod.setSentExtra("TA");
            }
        prod.setPalSpace(rs.getDouble("palletspace"));
        if(id.equals("05") || id.equals("06")){
        	id= "z" + id;
        }else{
        	id= "a" + id;
        }
        
        String aaa = counter + "";
        if(aaa.trim().length() == 2){
        	aaa = "0" + aaa ;
        }else if (aaa.trim().length() == 1){
        	aaa =  "00" + aaa;
        }
        
        nimetus= aaa + nimetus;
        
        if(stocks.get(kood) != null){
        	prod.setStock(Double.parseDouble((String)stocks.get(kood)));
        }

        double TAARA_VAT  = rs.getDouble("TAARA_VAT");
        double DEPO_WO_VAT = rs.getDouble("DEPO_WO_VAT");
        if(TAARA_VAT > 0) {
        prod.TAARA_VAT = TAARA_VAT;
        }
        if(DEPO_WO_VAT > 0) {
            prod.DEPO_WO_VAT = DEPO_WO_VAT;
            }
            
        
        prod.setNameSorting(nimetus);
        prod.setSorting(aaa);
        products.put( nimetus, prod);
        counter++;
        //}
      }
      rs.close();
      st.close();
      con.close();

    } catch (Exception e){
      getParent().showError(e);
    }
    
    
    if(abikad.size() > 0){
    	for(int o = 0;abikad.size()>o;o++){
    		Product pr = (Product)abikad.elementAt(o);
    		pr.setLabel(true);
    	    products.put( pr.getName() + pr.getID(), pr);
    	}
    }
   
    return products;
  }

  public Vector getCustomerProductsList(String agentCode){
	  
   String cmd = "SELECT * FROM alc.ALECOQ_4M_SALES";

      System.out.println(cmd);


    Vector custProds = new Vector();
    HashMap kliendid = new HashMap();


    try {
    	
    	
    	
        String 
        cmdf = "select b.MPPOPN as ltoode ,MPITNO as toode FROM " + 
        		"mvxjdta.MITPOP b inner join MVXJDTA.MITMAS g  on  MMITNO=MPITNO  where  b.mpalwt='1' " + 
        		"AND b.MPCONO=100 AND SubStr(b.MPREMK,1,6)='TELEMA'  and  MMBUAR='231' AND mmpdln='200'";

TreeMap tkoodid = new TreeMap();
Connection con = getNewConnection();
Statement st = con.createStatement();
ResultSet	   rs = st.executeQuery(cmdf);
  
while (rs.next()) {
String lkood = rs.getString("ltoode").trim();
String tkood = rs.getString("toode").trim();
tkoodid.put(lkood, tkood);
}
rs.close();
st.close();
con.close();
	  
    	
    	
    	
    	
       con = getNewConnection();
        st = con.createStatement();
       rs = st.executeQuery(cmd);
       while (rs.next()) {
        String tootekood = rs.getString("toode").trim();
        String kliendikood = rs.getString("saaja").trim();
        double kogus = rs.getDouble("kogus");
         String kuup = rs.getString("kuup");
         String lnimi = rs.getString("lnimi");
                   if(lnimi != null && !lnimi.trim().equals("") && tkoodid.get(lnimi.trim()) != null){
	             	  tootekood  = (String)tkoodid.get(lnimi.trim());
	                }
	    CustProd cp = new CustProd(kliendikood, tootekood, kogus, 0, 0,kuup,0,0,0," ");
        custProds.add(cp);
       }
      rs.close();
      st.close();
      con.close();

    }
    catch (Exception e) {
      getParent().showError(e);
    }

    return custProds;

  }


  public void setBulkSales(Map agents){

    String cmd =  "SELECT UCSMCD AS agent,SUM(UCSAAM) AS summacur, SUM(UCVOL3) AS summavol FROM mvxjdta.osbstd d WHERE " +
       " substr(UCIVDT,0,6) = '" + getYearMonth() + "' AND UCITTY='10' and UCDIVI='200' and UCFACI='200'  GROUP BY d.UCSMCD";

      if (isDebug()) System.out.println(cmd);
          try {
            Connection con = getNewConnection();
              Statement stmt = con.createStatement();
              Calendar cal = Calendar.getInstance();
              cal.set(Calendar.DAY_OF_MONTH, 1);
              ResultSet	      rs = stmt.executeQuery(cmd);
              while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                if (agentID.length()>2){
                  if (agentID.substring(2, 3).equals("1") ||
                      agentID.substring(2, 3).equals("2")
                      || agentID.substring(2, 3).equals("3")) {
                    agentID = agentID.substring(0, 2);
                  }
                }
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                  agent.setMonthSales((int)rs.getDouble("summacur"));
                  agent.setMonthSalesVol((int)rs.getDouble("summavol"));
                // System.out.println("agent" + agent.getName() + " " + agent.getMonthSales()+ " " + agent.getMonthSalesVol());
                }
              }
              rs.close();
              stmt.close();
              con.close();
          } catch (Exception e) {
              getParent().showError(e);
          }


  }



  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


  public DealerProduct getDealerProductsList() {

  	/*

       DealerProduct dealerProds = new DealerProduct();
       try {


          String  sql = " SELECT  MBITNO AS toode,MBSTQT-MBREQT AS STOCK , MBWHLO AS LADU " +
		"from mvxjdta.MITBAL l WHERE MBWHLO='100' OR MBWHLO='205' OR MBWHLO='215' ORDER BY MBITNO" ;

            if (isDebug()) System.out.println(sql);
               Connection con1 = getNewConnection();
               Statement stm = con1.createStatement();
               ResultSet rst = stm.executeQuery(sql);

                 while (rst.next()) {
                   String toode = rst.getString("toode").trim();
                   int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                   String ladu  =  rst.getString("ladu").trim();
                   dealerProds.add(ladu, toode,seis+""," "," " );
                   //System.out.println(ladu+" "+toode+" "+seis+"");
               }
               rst.close();
               stm.close();
               con1.close();

       } catch (Exception e) {
           getParent().showError(e);
       }

        return dealerProds;*/
        return null;
      }



      public Vector getDebt(String agentCode) throws SQLException{
       Vector list = new Vector();
       String sql = "SELECT trim(ESPYNO) AS maksja,ESCINO AS arve, ESDUDT AS kuupaev,ESCUAM AS SUMMA," +
         " round(SYSDATE -to_date(to_char(ESDUDT),'YYYYMMDD')) AS paevi ,ESDUDT As tahtaeg " +
         "FROM mvxjdta.FSLEDG Where ESRECO=0 AND ESCONO=100 AND ESDIVI='200' ORDER BY ESPYNO   " ;

     if (isDebug()) System.out.println(sql);
       Connection con = getNewConnection();
       stmt = con.createStatement();
       ResultSet rs = stmt.executeQuery(sql);
       Calendar cal = Calendar.getInstance();
       Date now = new Date(cal.getTimeInMillis());

       while(rs.next()){
         Debt debt = new Debt();
         debt.setCustID(rs.getString("maksja").trim());
         debt.setDoknr(rs.getString("arve").trim());
         Date duedate = getDate(rs.getString("kuupaev").trim());
         debt.setDuedate(duedate);

         debt.setSum(rs.getDouble("summa"));
         debt.setDue(rs.getString("paevi"));
         if(duedate.getTime()<now.getTime()){
           list.addElement(debt); // pane juhul kui  maksep�ev +1 on k�es kasuta tahtaeg
           // siis tuleb kokku liita YMT summad ja addida nee vastavale CUSTOMERI.Payerile
         }
       }
       rs.close();
       stmt.close();
       con.close();
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
      return "Movex";
    }

    public String getSubjectFilesTo() {
      return this.subjectFilesTo;
    }


    public String getDaySalesData(Agent agent) {
      String sql = "SELECT t.MMspe2 AS grupp,SUM(UCSAAM) AS summa " +
         "FROM mvxjdta.OSBSTD d ,mvxjdta.MITMAS t WHERE t.MMITNO=d.UCITNO " +
         "AND substr(UCIVDT,0,6) = '" + getYearMonth() + "' " +
         "AND UCSMCD='" +agent.getID() + "' AND UCITTY = '01' AND M9FACI=200 GROUP BY t.MMspe2 ORDER BY t.MMspe2 ";
         System.out.println(sql);
         StringBuffer sb = new StringBuffer();

         sb.append("\n " + getParent().getString("string5") + "\n\t" + getParent().getString("string6") +  "\n\n");
         sb.append(getParent().getString("string4") +  "\n");
           NumberFormat nn = getNF();
           nn.setGroupingUsed(false);
           try {
             Connection con = getNewConnection();
             stmt = con.createStatement();
             ResultSet rs2 = stmt.executeQuery(sql);
             while (rs2.next()) {
               sb.append(" " + rs2.getString("grupp") + "\n                 " +
                         nn.format(rs2.getDouble("summa")) +
                         " EUR\n-------------------------------------\n");
             }
             con.close();
             sb.append("\n\n" + getParent().getString ("string3") + "\n\n");
             sb.append(getParent().getString("string2") + "\n\n");
              } catch (Exception E){}

      return sb.toString();
    }



    public String getPilotDayData(Agent agent){

    	
    	String  sss = "SELECT * FROM alc.mj_made_orders WHERE "
    			+ "oksmcd='" + agent.getID()+ "' AND oaordt=" + this.getDateStr() + " order by nr";
    	
    	
      String   sql = " SELECT t.MMspe2 AS grupp,SUM(UCSAAM) AS summa " + 
      		"          FROM mvxjdta.OSBSTD d ,mvxjdta.MITMAS t WHERE t.MMITNO=d.UCITNO " + 
      		"          AND substr(UCIVDT,0,6) = '" + getYearMonth() + "' " + 
      		"          AND UCSMCD='" +agent.getID() + "' AND UCITTY = '10'  GROUP BY t.MMspe2 ORDER BY t.MMspe2" + 
      		"";
  
    

      System.out.println(sql);
      StringBuffer sb = new StringBuffer();
       try {
         sb.append("\n " + getParent().getString("todaysbills") + "\n\n");

         Connection con4 = getNewConnection();
         stmt = con4.createStatement();
         ResultSet rs5 = stmt.executeQuery(sss);
         while (rs5.next()) {
           sb.append(rs5.getString("nr") + " " + rs5.getString("nimi") + "\n (stat1 "  + rs5.getString("stat1") + ") (stat2 " + rs5.getString("stat2") +  ")\n\n");
         }
         rs5.close();
         con4.close();


      }
        catch (Exception e) {
          getParent().showError(e);
        }


        return sb.toString();
    }

    public void insertDefective(DefectiveWrapper wr){}

    public void insertDefectiveMeri(eevaeksport.report.DefectiveWrapperMeri wr){
        String sql = "INSERT INTO alc.MJ_QUALITY VALUES (?,?,?,?,?,?,?,?,?,?,sysdate,?,' ',' ',0,0)";
        try {
          Connection con = getNewConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, wr.getDefective().getAgentID());
            ps.setString(2, wr.getDefective().getAgentName());

            for (Iterator iter = wr.getDefective().getLines().iterator(); iter.hasNext(); ) {
              DefectiveLineWrapperMeri lw = new DefectiveLineWrapperMeri(iter.next());
              ps.setString(3, lw.getLine().getCustID());
              ps.setString(4, lw.getLine().getCustName());
              ps.setString(5, lw.getLine().getProdID());
              ps.setString(6, lw.getLine().getProdName());

              double kogus = 0;
              try {
                kogus = Double.parseDouble(lw.getLine().getAmount());
              } catch (NumberFormatException nfe){};
              ps.setDouble(7, kogus);
              String bb = lw.getLine().getBestBefore();
              System.out.println(kogus+"bb '"+lw.getLine().getBestBefore()+"'"+bb+"'");
              ps.setString(8, bb.trim());
              ps.setString(9, lw.getLine().getDefectText().substring(0,3).trim());
              ps.setString(10, lw.getLine().getProdType().trim() );
              ps.setString(11, lw.getLine().getDefectText().trim());

              ps.executeUpdate();
            }

            con.commit();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
      }



    public void insertDistribution(DistEvalListWrapper wr){}


    
    public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		File f = new File(this.getExcelDir() + "/sortiment.xlsx");
		
		try {
			
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			for(int r = 0 ;r < workbook.getNumberOfSheets();r++){
				XSSFSheet sheet = workbook.getSheetAt(r);
				if(!"Shops".equals(sheet.getSheetName())){
					
			
			XSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(0);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 3; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			 System.out.println(groups.toString());

			for (int i = 1; i < sheet.getLastRowNum()+1; i++) {
				row = sheet.getRow(i);
				String prodID = "";

				if (row.getCell((short) 1) != null) {

					try {
						prodID = (int) (row.getCell((short) 1)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID = (row.getCell((short) 1).getStringCellValue());
					}
					
                       if(!prodID.equals("0")){
                    	 //  System.out.println("prodId " + prodID);
					// Siin l�heme reasisse rokkima ja vaatama kas se toode
					// l�heb kaasa
					for (int a = 3; a < row.getLastCellNum(); a++) {

						String group = (String) groups.get(a + "");
						if (group != null && group.trim().length() > 0) {

							String custID = " ";
							String clGroup = " ";
							String prodGroup = " ";

							String go = null;
							clGroup = group;

							MatrixLine mline = new MatrixLine();
							// Vaatame kas selles veerus on sellel tootel pandud
							// mingi t�his
							if (row.getCell((short) a) != null) {

								try {
									go = (int) (row.getCell((short) a)
											.getNumericCellValue()) + "";
								} catch (Exception e) {
									go = (row.getCell((short) a)
											.getStringCellValue());
								}

							}

							if (go != null && go.equals("1")) {

								// System.out.println( a + " " + prodID + " " +
								 //clGroup + " " + go);

								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(clGroup.trim());
									
								}

								if (prodID != null
										&& prodID.trim().length() > 0) {
									mline.setProdID(prodID.trim());
								}
								if (mline.getProdID() == null
										&& prodGroup != null
										&& prodGroup.trim().length() > 0) {
									mline.setProdGroup(prodGroup);
								}
					
								result.add(mline);
							}
						}
					}
					   
                       }
				}
			}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		 System.out.println("fix ass " + result.size());

		return result;

	}




    private String setZero(int i){
     	String x = i +"";
     	if (x.length() == 1){
     		x = "0" + x;
     	}
     	return x;
     }
    private Date getDate(String kuup) {

      Calendar cal = Calendar.getInstance();
      int YEAR = Integer.parseInt(kuup.substring(0, 4));
      int MONTH = Integer.parseInt(kuup.substring(4, 6));
      int DAY = Integer.parseInt(kuup.substring(6, 8));
      cal.set(YEAR, MONTH, DAY, 1, 2, 3);
      Date now = new Date(cal.getTimeInMillis());
      return now;
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

    
    public String getDateStr(){
        Calendar cal = Calendar.getInstance();
        String month = (cal.get(Calendar.MONTH)+1) +"";
        String day = (cal.get(Calendar.DAY_OF_MONTH)) +"";
             if(month.length() == 1){month = "0" + month;}
             if(day.length() == 1){day = "0" + day;}
             return cal.get(Calendar.YEAR) + "" + month + day;
         }
    
    public String get30AgoStr(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -25);
        String month = (cal.get(Calendar.MONTH)+1) +"";
        String day = (cal.get(Calendar.DAY_OF_MONTH)) +"";
             if(month.length() == 1){month = "0" + month;}
             if(day.length() == 1){day = "0" + day;}
             return cal.get(Calendar.YEAR) + "" + month + day;
         }

        private String getYearMonth() {
          Calendar cal = Calendar.getInstance();
          String month = (cal.get(Calendar.MONTH)+1) +"";
          if(month.length() == 1){month = "0" + month;}
          return cal.get(Calendar.YEAR) + "" + month;
        }

   	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){}


   	 
 	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
       

         String kuhu = "supervisorsh";
         
     	 boolean fixing = false;
     	  try {	
          	
         
          
               for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
            	
            	   eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
            	   
            	   
            	   
            	   
            	   boolean inventuur = false;
            	   
            	   String res = lw.getLine().getResStr1();
            	              	   
            	              	   if(res!= null  && res.startsWith("INVSCAN")){
            	                 	     inventuur = true;
            	                      }
            	              	   //kui on inventuur siis see 
            	              	   if(inventuur) {

            	   					Calendar cal = Calendar.getInstance();
            	   					try {
            	   						cal.setTimeInMillis(Long.parseLong(lw.getLine().getResStr3()));
            	   					} catch (Exception e) {
            	   					
            	   					}

            	              		   String cuid = lw.getLine().getCustID();
            	              		   String agid = wr.getInfo().getAgentID();
            	              		   String invent = lw.getLine().getAction1Eval() +""; 
            	              		   String tehas = lw.getLine().getResStr1();
            	              		   String comm = lw.getLine().getComment();
            	              		   String itemno = lw.getLine().getResStr2();
            	              		  String serial = lw.getLine().getResStr2();
            	              		  String foundCode = lw.getLine().getProdName();
            	              		  
            	              		  int uuspv = 1;
            	              		  if(lw.getLine().isAction()) {
            	              			  uuspv = 0;
            	              		  }
            	              		if (foundCode.indexOf("Inventory") != -1) {
            	              			foundCode = serial;
            	              		}
            	              		  
            	              		String sb =  wr.getInfo().getAgentID() + ";" + serial + " ;"
           	           					 + lw.getLine().getCustID()+  ";uuspv "  + uuspv + " brand"+ lw.getLine().isBrand()
           	           					 + " group"+ lw.getLine().isGroup()
           	           					 + " action"+ lw.getLine().isAction()
           	           					+ " part"+ lw.getLine().isPartner();//long,latt
            	              		
            	              	  if(!lw.getLine().isPartner()) {
        	              			  serial = "";
        	              		  }
            	   					String cc = lw.getLine().getCustComment();
            						if (cc == null) {
            							cc = " ";
            						}
            						
            						if(cc.indexOf("||") != -1) {
            							itemno = cc.substring(0,cc.indexOf("||"));
            							cc = cc.substring(cc.indexOf("||")+2);
            						}
            						
            						String cd = lw.getLine().getComment();
            						if (cd == null) {
            							cd = " ";
            						}
            	              		   double amount = 1;
            	              		
            	           				
            	                      		   System.out.println("inventuur olemas " + sb);
            	                      		 String sql = "INSERT into alc.MJ_INVENTUUR values ("
            	                                     + "?, "   //   datetime
            	                                     + "?, "   //   agentid	C(50),
            	                                     + "?,"   //   clientid	C(50),
            	                                     + "?,"   //   itno	C(200),
            	                                     + "?,"   //   serial	C(200),
            	                                     + "?,"   //   PVnum	C(200),
            	                                     + "?,"   //   qty	C(200),
            	                                      + "?,"   //   comm	C(200),
            	                                       + "?,"   //   uuspv	C(200),
            	                                     + "?,?)";   //   skannitud
            	                      		 

            	                          	 Connection c =  getNewConnection();
            	                      		PreparedStatement ps = c.prepareStatement(sql);
            	                            
            	                            ps.setTimestamp(1, new java.sql.Timestamp(cal.getTimeInMillis()));
            	                            ps.setString(2, agid);
            	                            ps.setString(3, cuid);
            	                            ps.setString(4,  itemno);
            	                            ps.setString(5, foundCode );
            	                            ps.setString(6, cc);
            	                            ps.setInt(7, 1);
            	                            ps.setString(8, comm); 
            	                            ps.setInt(9, uuspv);
            	                            ps.setString(10, serial);
            	                            ps.setString(11, "");
            	                            ps.executeUpdate();	 
            	                   
            	                    ps.close();
            	                    c.close();
            
            	     } else {
            	   
            	   

                      	 Connection c =  getNewConnection();
            	   
            		kuhu = "supervisorsh";
                	 fixing = false;
                   
                  
                   if(lw.getLine().getResStr1().equals("INVPROBLEM")){
                  			fixing = true;
                  			kuhu = "tehnik"; 
                  		}
                  		
            	   String sql = "INSERT into alc.mj_" + kuhu +" values ("
                   + "?, "   //   visorid	C(50),
                   + "?,"   //   visorname	C(200),
                   + "?, "   //   agentid	C(50),
                   + "?,"   //   agentname	C(200),
                   + "?,"   //   clientid	C(50),
                   + "?,"   //   clientname	C(200),
                   + "'',"   //   clienttype	C(200),
                   + "'',"   //   clientarea	C(200),
                   + "?,"   //	prodid	C(50),
                   + "?,"   //   prodname	C(200),
                   + "?,"   // isbrand	N,
                   + "?,"   // cattotal	N,
                   + "?,"   // cattotal	N,
                   + "sysdate ,"   //	collecteddate	D,
                   + " '' ,"   //	collectedTime	D,
                   + "?,"   // amoun	N,
                   + "?,"   //normalpr	N,
                   + "?,"   // custcomme	C(200),
                   + "?,"   // comment	C(200),
                   + "?,"   // resstr1	C(200),
                   + "?,"   // resstr2	C(200),
                   + "?,"   // resint1	N,
         	      + "?)" ; //	resint3	N 
                    
                    
                    
               
                
            	   String res2 = lw.getLine().getResStr2();
            	   
            	   if(res2!= null && res2.length() >50){
               	 // res2 = res2.substring(0,49);
                 }else if (res2 == null){
               	  res2 = "";
                 }
            
            	   System.out.println("RES 2 " + res2);
                   
                 
                 
               		PreparedStatement ps = c.prepareStatement(sql);
                    Connection c2 =  getNewConnection();
                    ps.setString(1, wr.getInfo().getAgentID());
               		
                    String comm = Converter.deCode( lw.getLine().getComment());
                    if(comm != null && comm.length()>500){
                    comm = comm.substring(0,493);
                    }else if (comm == null) {
                    	comm = "";
                    }
                    
                    String telef = Converter.deCode( lw.getLine().getProdID());
                    if(telef != null && telef.length()>50){
                    	telef = telef.substring(0,49);
                    }
                    
                    
                    
              	String sql2 = "INSERT into alc.mj_tehnik (AGENT,AGNIMI,KLIENT,KLIENDINIMI,KLIENTTEL,KUUPAEV,PVTYYP" +
      			",TEHASETAHIS,PVNUMBER,INFOSISU,STATUS) values " +
      			"('" + Converter.deCode(wr.getInfo().getAgentID()) + 
      			"','" + Converter.deCode(wr.getInfo().getAgentName()) +  "'," +
      					"'" + lw.getLine().getCustID() +  "'," +
      							"'" + Converter.deCode(lw.getLine().getCustName().replaceAll("'", " "))+  "'" +
      					",'" + telef +  "'" +
      							",sysdate,'" 
      					+ Converter.deCode( lw.getLine().getProdName().replaceAll("'", " "))
      					+  "','" + res2 + "','" +Converter.deCode( lw.getLine().getCustComment().replaceAll("'", " ")) +  "'," +
      							"'" +comm.replaceAll("'", " ") +  "','')";
               Statement ps2 = c2.createStatement();
                 
               
               //  System.out.println(wr.getInfo().getAgentID() +"'");
                 ps.setString(2, Converter.deCode(wr.getInfo().getAgentName()));
                   
               //System.out.println(wr.getInfo().getAgentName() +"'");
                 ps.setString(3, wr.getInfo().getAgentID());
               //  System.out.println(wr.getInfo().getAgentID() +"'");
                 ps.setString(4, Converter.deCode(wr.getInfo().getAgentName()));
                // System.out.println(wr.getInfo().getAgentName() +"'");
                 ps.setString(5, lw.getLine().getCustID());
                
                // System.out.println(lw.getLine().getCustID() +"'");
                    ps.setString(6,Converter.deCode( lw.getLine().getCustName()));
               
                // System.out.println(lw.getLine().getCustName() +"'");
                 ps.setString(7, lw.getLine().getProdID());
                // System.out.println(lw.getLine().getProdID() +"'");
                 
                 String name =  Converter.deCode(lw.getLine().getProdName());
                 if(name == null){
              	   name = "tyhi";
                 }
                 ps.setString(8,name);
                 System.out.println(name +"'");
                 double action = 0;
                 if(lw.getLine().isAction()){
              	   action = 1;
                 }
                 ps.setDouble(9, action);//isBrand

                 ps.setInt(10, lw.getLine().getPercentageFromTotalCategory());
                // System.out.println();
                 ps.setInt(11, lw.getLine().getPercentageFromColdCategory());
                 //System.out.println(lw.getLine().getPercentageFromColdCategory() +"'");
                 ps.setDouble(12, lw.getLine().getAmount());   
                 System.out.println("Amount = " + lw.getLine().getAmount() +"'");
                 ps.setDouble(13, lw.getLine().getNormalPrice());
                 //System.out.println("price" +  lw.getLine().getNormalPrice() +"'");
                 ps.setString(14, Converter.deCode(lw.getLine().getCustComment()).replaceAll("'", " "));
                 //System.out.println(lw.getLine().getCustComment() +"'");
              
                 
                 ps.setString(15, Converter.deCode(comm).replaceAll("'", " "));
               
                 String res1 = lw.getLine().getResStr1();
                if(res1 != null && res1.length() > 50){
              	
                }else if(res1 == null){
              	  res1 = "";
                }
               
                    ps.setString(16, res1);
                     ps.setString(17, res2);
               
                 ps.setInt(18, lw.getLine().getResInt1());
                 ps.setInt(19, lw.getLine().getResInt2());
                 
                 System.out.println("lopp '" + lw.getLine().getResInt2() +"'");
                 if(fixing){
                	 System.out.println(sql2);
                     
                	    ps2.executeUpdate(sql2);
                 }else{
                	    ps.executeUpdate();	 
                 }
                  ps2.close();
                  ps.close();
                  c2.close();

                  c.close();
            	     }
               
               }
           } catch (SQLException e) {
               e.printStackTrace();
           }
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

    private String url = "jdbc:oracle:thin:@10.10.100.97:1521:GM3REP1";
   // con = DriverManager.getConnection("jdbc:oracle:thin:@10.10.100.102:1521:GM3DEV2", "ALC", "alc1807");
    //con = DriverManager.getConnection("jdbc:oracle:thin:@10.10.100.97:1521:GM3REP1", "ALC", "alc1807");
     
    private Connection con;
  private Statement stmt;
  private String subjectFilesTo = null;
  private TreeMap agents1 = new TreeMap();
  private NumberFormat NF = NumberFormat.getInstance();
  private TreeMap  dealers = new TreeMap();
  HashMap aHash = new HashMap();
  
  
  public Vector getPriceMatrixData(){
		
	  	 String    sql = " select '' AS custid,pyno AS clgroup,itno AS prodid,'' AS prodgroup, " +
	      "   max((Price * ((100-discount)/100))+ excise) AS price ,0 as disc, '' as paev,'' as startday, max(discount) as info  " +
	      "  ,  max(DISCOUNT_QTY) as extra from alc.discounts_view t where  camp = '0' and cuno is null group by t.pyno,t.itno order by pyno,itno " ;
	

  Vector result = new Vector();
    try {
    	
 	   Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {

        String custID = "";//Converter.enCodeEst(rs.getString("custid").trim());
        String clGroup = rs.getString("clgroup");
        String prodID = rs.getString("prodid").trim();
        
        String prodGroup = rs.getString("prodgroup");
       
        double price = rs.getDouble("price");
        double disc = rs.getDouble("disc");

        String date = rs.getString("paev");
        String sdate = rs.getString("startday");

        double info = rs.getDouble("info");  
        String extra = rs.getString("extra");
        MatrixLine mline = new MatrixLine();

        if (custID != null && custID.length() > 0) {
          mline.setCustID(custID);
        }
      
        
        if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
          mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
        }

      
        if(date != null && !date.startsWith("1753")){
       	 mline.setEndDate(date.substring(0,10));
        }
        if(sdate != null  && !sdate.startsWith("1753")){
         	 mline.setStartDate(sdate.substring(0,10));
         	 if(mline.getEndDate() == null){
         		 mline.setStartDate(null);
         	 }
          }
        if (prodID != null && prodID.length() > 0) {
          mline.setProdID(prodID);
        }
        if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
          mline.setProdGroup(prodGroup.trim());
          mline.setDiscGroup(prodGroup.trim());
        }

        if (price != 0) {
          mline.setPrice(price);

        }
        if (disc != 0) {
            mline.setDiscount(disc);

          }
      //  mline.setProdLevel("pr"); // paneme long listi 
        if( mline.getPrice() > 0 || mline.getDiscount() > 0){
        
        	String inff = "";
      	  if(extra != null && extra.trim().length() > 0){
      			 inff = " " + extra.trim();
      		 }
      	  
         	 if(info > 0){
         			mline.setLineinfo( parent.getNF().format(info) + "%" + inff );
                 	} else if(inff.trim().length() > 0){
                  mline.setLineinfo( inff );
                 }
         	 result.add(mline);    
        }
        
      }
      rs.close();
      
      //kampaaniate kysimine
      
      sql = "  select '' AS custid,pyno AS clgroup,itno AS prodid,'' AS prodgroup,  " +
	     "  max((Price * ((100-discount)/100)) + excise) AS price ,0 as disc, todt as paev,fromdt as startday"
	     + " ,max(discount) as info , DISCOUNT_QTY as extra , max(CAMPTYPE) as type " +
	     "   from alc.discounts_view t where  camp != '0'   and cuno is null group by t.pyno,t.itno,fromdt,todt,DISCOUNT_QTY order by pyno,itno " ;

       rs = stmt.executeQuery(sql);
      while (rs.next()) {

        String custID = "";// Converter.enCodeEst(rs.getString("custid").trim());
        String clGroup = rs.getString("clgroup");
        String prodID = rs.getString("prodid").trim();
        
        String prodGroup = rs.getString("prodgroup");
       
        double price = rs.getDouble("price");
        double disc = rs.getDouble("disc");
        String date = rs.getString("paev");
        String sdate = rs.getString("startday");
        double info = rs.getDouble("info");
        String extra = rs.getString("extra");
        String type = rs.getString("type");
 
        boolean rohe = true ;
        if(type == null || type.trim().length() == 0){
        //	if(!type.equals("CAMP")) {
        		rohe = false;
        	//}
        
        }
        boolean goes = false;
        if(prices.get(prodID) != null){
        	goes = true;
        	double pr = Double.parseDouble((String)prices.get(prodID));
        	if(pr == price){
        		goes = false;
        	}
        }
        MatrixLine mline = new MatrixLine();

        if (custID != null && custID.length() > 0) {
          mline.setCustID(custID);
        }
      
        
        if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
          mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
        }

      if(rohe){
    	
        if(date != null && !date.startsWith("1753")){
       	 mline.setEndDate(date);
        }
        if(sdate != null  && !sdate.startsWith("1753")){
         	 mline.setStartDate(sdate);
         	 if(mline.getEndDate() == null){
         		 mline.setStartDate(null);
         	 }
          }
        
      }
        
        if (prodID != null && prodID.length() > 0) {
          mline.setProdID(prodID);
        }
        if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
          mline.setProdGroup(prodGroup.trim());
          mline.setDiscGroup(prodGroup.trim());
        }

        if (price != 0) {
          mline.setPrice(price);
        }
        
        if (disc != 0) {
            mline.setDiscount(disc);
          }
        
        if( goes && mline.getPrice() > 0 || mline.getDiscount() > 0){
        	  String inff = "";
        	  if(extra != null && extra.trim().length() > 0){
        			 inff = " " + extra.trim();
        		 }
        	  
           	 if(info > 0){
           		 
           			mline.setLineinfo( parent.getNF().format(info) + "%" + inff );
           		 //   mline.setProdLevel("pr");
                   
           	  } else if(inff.trim().length() > 0){
                    mline.setLineinfo( inff );
              }
       
            result.add(mline);    
        }
      }
      
      
     
      
      // kliendip�histe hindade kysimine mis on erinevad maksja omadest 
      sql = " select cuno AS custid,'' AS clgroup,itno AS prodid,'' AS prodgroup,  " +
	     "  max((Price * ((100-discount)/100)) + excise) AS price ,0 as disc, todt as paev,fromdt as startday ,max(discount) as info " +
	     " , DISCOUNT_QTY as extra , camp from alc.discounts_view t where  cuno is not null " +
	     " group by t.cuno,t.itno,fromdt,todt,DISCOUNT_QTY ,camp order by cuno,itno" ;

    rs = stmt.executeQuery(sql);
   while (rs.next()) {

     String custID = rs.getString("custid").trim();// Converter.enCodeEst(rs.getString("custid").trim());
     String prodID = rs.getString("prodid").trim();   
     double price = rs.getDouble("price");
     String date = rs.getString("paev");
     String sdate = rs.getString("startday");
    
     double info = rs.getDouble("info");
     String extra = rs.getString("extra");
       
    boolean goes = true;
 
     MatrixLine mline = new MatrixLine();

     if (custID != null && custID.length() > 0) {
       mline.setCustID(custID);
     }
      
     if (prodID != null && prodID.length() > 0) {
       mline.setProdID(prodID);
     }
   
     if(date != null && !date.equals("0")){
       	 mline.setEndDate(date);
        }
        if(sdate != null  && !sdate.equals("0")){
         	 mline.setStartDate(sdate);
         	 if(mline.getEndDate() == null){
         		 mline.setStartDate(null);
         	 }
          }else{
        	    mline.setProdLevel("pr");
          }
       
     if (price != 0) {
       mline.setPrice(price);
     }
     
     if( goes && mline.getPrice() > 0 ){
    	  String inff = "";
    	  if(extra != null && extra.trim().length() > 0){
    			 inff = " " + extra.trim();
    		 }
    	  
       	 if(info > 0){
       			mline.setLineinfo( parent.getNF().format(info) + "%" + inff );
               	} else if(inff.trim().length() > 0){
                mline.setLineinfo( inff );
               }
    	 result.add(mline);    
     }
   }
      
      
      stmt.close();
      con.close();
    }catch(Exception e){
   	 e.printStackTrace();
    }
	
return result;
 }


public String getProductSortingData() {
	// TODO Auto-generated method stub
	return null;
}

private String getDateSt(String lon){
	//YYYYMMDDHH24MISS

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Long.parseLong(lon));
        
     
        StringBuffer sb = new StringBuffer();
	    sb.append(cal.get(Calendar.YEAR) + "");
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month + "");

	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day);
	    String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
	    if (hour.length() == 1){
	      sb.append("0");
	    }
	    sb.append(hour);
	    
	    String minute = String.valueOf(cal.get(Calendar.MINUTE));
	    if (minute.length() == 1){
	      sb.append("0");
	    }
	    sb.append(minute);
	    
	    String second = String.valueOf(cal.get(Calendar.SECOND));
	    if (second.length() == 1){
	      sb.append("0");
	    }
	    sb.append(second);
	    

	    return sb.toString();
	
}

private String getRing(String custid, String moodus){
	String res = "213";
	System.out.println("Custid " + custid + " moodus:" + moodus);
	
	if(moodus.equals("Ring")){
		
		
		String sql = "  SELECT  CASE WHEN do.dorout IS NULL THEN '2100' ELSE do.dorout END AS ring FROM mvxjdta.OCUSMA " +
		" left join mvxjdta.DRODPR do on okcuno=do.doobv1  and do.docono=100 and do.doedes='210' " +
        " WHERE okcuno ='" + custid + "'";
		try{
				 Connection c =  getNewConnection();
				 Statement stmt = c.createStatement();
					ResultSet rs = stmt.executeQuery(sql );
					if (rs.next()) {
						res =  rs.getString("ring");
					}
					rs.close();
					stmt.close();
					c.close();
			       } catch (SQLException e) {
			           e.printStackTrace();
			       }	
			       
	}else if(moodus.equals("Ise") ){
		
		String sql = "   SELECT  case   when doobv1 between '2100' and '2199'  then 2113 " + 
                    " when dorout between '2200' and '2299'  then 2223 " +
                     "  when dorout between '2300' and '2399'  then 2304  " +
                     " when dorout between '2400' and '2499'  then 2405  " +
                     " when dorout is NULL  then 2113 else 2223 end " +
                           " AS ring , OKCUCL as asi  FROM mvxjdta.OCUSMA " +
		" left join mvxjdta.DRODPR do on okcuno=do.doobv1  and do.docono=100 and do.doedes='210' " +
        " where okcuno ='" + custid + "'";	
	
		/*[16:34:28] Risto Puusepp:  when dorout between \'100\' and \'199\'  then 113
  when dorout between \'200\' and \'299\'  then 223
  when dorout between \'300\' and \'399\'  then 304
  when dorout between \'400\' and \'499\'  then 405
  when dorout is NULL  then 113*/
  try{
		 Connection c =  getNewConnection();
		 Statement stmt = c.createStatement();
			
		 ResultSet rs = stmt.executeQuery(sql );
			
			if (rs.next()) {
			res =  rs.getString("ring").trim();
			}
			
			rs.close();
			stmt.close();
			c.close();
	       } catch (SQLException e) {
	           e.printStackTrace();
	       }
	}else if(moodus.equals("Eri")){
		res = "2220";
	}

	

	System.out.println("Ring Tulemus  " + res);
	return res ;
}

public void eventInfo(Vector lines) throws SQLException {
	// TODO Auto-generated method stub
	
	 String test = getParent().getProps().getProperty("testing");
	 if("true".equals(test)){
		 lines = new Vector();
		 lines.add("1392642368489;RL;Raul Logina;07167;T�RVA T� KONSUM;k�lmiku vahetus;katrin riiet;  raul logina;Tartu;1;0;Ring; ;1392674408490;1392674408490;0;600014;K�lmik C10G 2 ust A.LeCoq;1.0; vana ja katkine k�lmik tagasi .uus asemele.; ");
		 lines.add("[Description: reklaam]");
	 }
		 
	 
	 System.out.println("Event info " + lines.size() + lines.toString());
	for(int i = 0; i < lines.size();i++){
		String line = (String)lines.elementAt(i);
		if(line.length() > 50){
			
		line = Converter.deCode(line);
		
	StringTokenizer st = new StringTokenizer(line,";");
	String id = "";
	String agent = "";
	String agentname = "";
	String ynimi = "";
	String knimi = "";
	String ladu = "";
	String inventar = "";
	String transa = "";
	String moodus = "";
	String algus = "";
	String lopp = "";
	String lineno = "";
	String prodid = "";
	String prodname = "";
	String prodkogus = "";
	String prodcomment = "";
	String custid = "";
	String custnimi = "";
	String tarne = "";
	String yid = "";
	String ring = "";
	
	String ericoord = "";
	String erivedu = "0";
	
	
		 id = st.nextToken();
		 yid = getDateSt(id);
		 agent =  st.nextToken();
		 agentname =  st.nextToken();
		 custid =  st.nextToken();
		 if(custid.length() == 4 ){
			 custid = "0" + custid;
		 }
		 custnimi =  st.nextToken();
		
		 ynimi =  st.nextToken().replace('+', ' ').replace('\'', ' ');
		 knimi =  st.nextToken().replace('+', ' ').replace('\'', ' ');
		 ladu =  st.nextToken();
		 inventar =  st.nextToken();
		 transa =  st.nextToken();
		 moodus =  st.nextToken();
		 tarne =  st.nextToken();
		 algus =  st.nextToken();
		 lopp =  st.nextToken();
		 lineno =  st.nextToken();
		 prodid =  st.nextToken();
		 prodname =  st.nextToken().replace('+', ' ').replace('\'', ' ');
		 prodkogus =  st.nextToken();
		 prodcomment =  st.nextToken().replace('+', ' ').replace('\'', ' ');	
    	 ring =  st.nextToken();
    	 if(st.hasMoreTokens()) {
    			tarne = st.nextToken();
    			erivedu = "1";
    	 }
    	 if(st.hasMoreTokens()) {
		    ericoord = st.nextToken();
 	       }
	

	
	if(!moodus.equals("EriRing")){
		ring = getRing(custid,moodus);
	}
	

	 String sql = "INSERT into alc.horeca_yritused2 (ME_ID,ME_NAME, ENTER_DATE, STAATUS,CUNO,CUNM,ADDR,YR_NAME,SUPPLIER,BEGIN_DATE,END_DATE" +
	 		",RESP_ID,RESP_NAME,TRANS1,TRANS2,CONTACT,GROUPID,GROUPNAME,QTY,COMM,EVENT_ID,LINE_ID,KAUBAGA,DELIVERED,RING, ERIVEDU, COORDS) values ("
          + "'" + agent + "', "   //   
          + "'" + agentname + "',"   // 
          + " sysdate, "   //   
          + "'10',"   // 
          + "'" + custid + "'," 
          + "?," //   
          + "'" + tarne + "'," 
          + "'" + ynimi + "'," 
          + "'" + ladu + "'," 
          + "?,?," 
          + "'" + agent + "'," //resp_id
          + "'" + agentname + "'," //rsp_name
          + "'" + moodus + "'," 
          + "" + transa + "," 
          + "?," 
          + "'" + prodid + "'," 
          + "'" + prodname + "'," 
          + "" + prodkogus + ","
          + "'" + prodcomment + "'," 
          + "'" + yid + "'," 
          + "'" + lineno + "'," 
          + "" + inventar + ",'Ei','" + ring + "',"+erivedu+",'" + ericoord + "')" ; 
  try{
	 Connection c =  getNewConnection();
           PreparedStatement ps = c.prepareStatement(sql);
           System.out.println(sql);
           Timestamp alg = new java.sql.Timestamp(Long.parseLong(algus));
           if(alg.getHours() == 12){
        	   alg.setHours(0);
        	   alg.setMinutes(0);
        	   alg.setSeconds(0);
        	   alg.setNanos(0);
           }
           Timestamp lop = new java.sql.Timestamp(Long.parseLong(lopp));
           if(lop.getHours() == 12){
        	   lop.setHours(0);
        	   lop.setMinutes(0);
        	   lop.setSeconds(0);
        	   lop.setNanos(0);
           }
           
           
           ps.setString(1, custnimi);
            ps.setTimestamp(2, alg);
            ps.setTimestamp(3, lop);
            ps.setString(4, knimi);
                      ps.executeUpdate();
           

           ps.close();
       } catch (SQLException e) {
           e.printStackTrace();
       }  }
}}

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


private void makeNormpict(String pictspda , String catal){
			
		String pictssou = catal ;
		System.out.println("cat name "  + pictssou );
		 File res2 = new File (pictssou);
		    String[] ff2 = res2.list();
			 for(int i = 0; ff2.length > i ; i++){
				 
				File sou = new File ( pictssou + ff2[i]); 
				//System.out.println();
				if(ff2[i].indexOf("_") != -1 ){
					String toode = ff2[i].substring(0,ff2[i].indexOf("_"));
					if(prodean.get(toode) != null){
						Vector vv = (Vector)prodean.get(toode);
						for(int w =0;w<vv.size();w++){
							File out = new File ( pictspda + vv.elementAt(w) + ".jpg");	
							try {
								copyFile(sou,out);
							} catch (Exception u) {
								u.printStackTrace();
					    	}	
						}
							
					}
			   }
		   }
	    }



Hashtable prodean = new Hashtable(); 

@Override
public void PictureMaker() {

	     
    String cmd = "SELECT * FROM alc.mj_item_master WHERE cucl='NON-HORECA'";

  		     	String picts = parent.getProps().getProperty("pict.dir");
		     	String pictspda = parent.getProps().getProperty("pictpda.dir");
		 	    String pictssource = parent.getProps().getProperty("pictsource.dir");

		   	try {
  		 	   Connection con = getNewConnection();
		   		Statement stmt = con.createStatement();
		   	    ResultSet	      rs = stmt.executeQuery(cmd);

		   	  while (rs.next()) {
		   		  String kood = rs.getString("kood").trim();
		   		 String ean = rs.getString("ean");
		   		 if(ean != null && ean.trim().length() >0){
		   			 if(prodean.get(ean.trim()) != null){
		   				 Vector vv = (Vector)prodean.get(ean.trim());
		   				 vv.add(kood);
		   				prodean.put(ean.trim(),vv);
		   			 }else{
		   				 Vector vv = new Vector();
		   				 vv.add(kood);
		   				prodean.put(ean.trim(),vv); 
		   			 }
		   		 }
		   		  
		   	  }
		   	  
		   	  System.out.println("prodean suurus " + prodean.size());
		        StringBuffer buf = new StringBuffer();
		        StringBuffer buf0 = new StringBuffer();
		        int a = 0;
		        int b = 0;
		        int c = 0;
		        
		        // leiame kataloogid ja peame meeles 
		    
		        
		        
		        this.makeNormpict( pictssource,picts);
		        
		        rs = stmt.executeQuery(cmd);
		   	    while (rs.next()) {
		   	    	a++;
		   	    	c++;
		   	    	
		   	     String toode = rs.getString("kood").trim();  
		         toode = toode.replace('/', '_');
		         toode = toode.replace('/', '_');
		   	    	if(c == 10){
		   	    		  c = 0;
		   	    		  System.out.println("Piltide leidmine "  + a);	
		   	    		  
		   	    	}
		   	     
		              
		                 File f = new File(pictssource + toode+".jpg");
		                 String len = "0";
		                 if(f.exists()){
		                   len =	f.length() + "";
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
		   	//  System.out.println("Kataloogi kustutamine "  + a);	    
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
//		        	 L�ppes kustutamine 
		        	 
		          System.out.println("Piltide kopeerimine ");	
		          //Failide kopeerimine  
		            String[] gg = new File(pictssource).list();
		         	  System.out.println("gg " + gg[0] + " " + gg.length);
		            
		             for(int i = 0;gg.length>i;i++){          	
		             	File in = new File ( pictssource + gg[i]); 
		             	if(in.isFile()){            	
		             	File inpda = new File ( pictspda + gg[i]); 
		                   	copyFile(in,inpda);
		             	}
		             }
		             
		           
		             
		             File fff =  new File (pictspda +  "list.txt");
		       	  copyFile(f,fff);  	
		       	  File fff0 =  new File (pictspda +  "list0.txt");
		       	  copyFile(f0,fff0);  	
		   	} catch (Exception e) {
		   	    getParent().showError(e);
		   	}
		   System.exit(0);
		}


public  Vector getCustomerProductsDistList() {
	
	String cmd =   "select client_id as saaja , product_id as toode from  alc.MJ_DISTRIBUTION_MARKS ";

	    
	    if (isDebug())
	      System.out.println(cmd);

	    Vector custProds = new Vector();
	   
	    try {
	      Connection con = getNewConnection();
	      Statement st = con.createStatement();
	      ResultSet rs = st.executeQuery(cmd);
	      int co = 0;
	       while (rs.next()) {
	    	   String kliendikood = rs.getString("saaja").trim();
	           String tootekood = rs.getString("toode").trim();
	           if(co < 100)
	      // System.out.println( kliendikood + " " + tootekood);
	           co++;
	      CustProd cp = new CustProd(kliendikood, tootekood, 0, 0, 0," ",0,0,0," ");
	      custProds.add(cp);
            }
	      rs.close();
	      st.close();
	      con.close();
	      System.out.println("custProdDist suurus " + custProds.size());
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	      getParent().showError(e);
	    }
       
	    return custProds;

}
	

}
