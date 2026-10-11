package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
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
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class AxaBuraLatCallDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
  public AxaBuraLatCallDBDriver(EksportPanel parent){
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
    if(parent.getProps().getProperty("rate") != null){
   	 try {
   	    rate = Double.parseDouble(parent.getProps().getProperty("rate"));
   	    } catch (Exception e){
   	       parent.showError(e);
   	    }
   }
  }

  
 // private double  rate = 0.702804;
  private double  rate = 1;
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
	  makePictureCat();
	  System.exit(0);
   	return null;
   }


  public Vector getFixedAssortmentData() {
      Vector lines = new Vector();

      try {
     	  String cmd = "  select  assortgroupid as grupp,assortitemid as toode  from assorttable where dataareaid ='bb' " +
     	  		" and assortcondition =1 order by assortgroupid,assortitemid ";
   
//     	  select * from assortgroups_cust
  //   	  select * from assorttable


     	
     	System.out.println(cmd);
     	 Connection cc = getNewConnection();
    	        Statement stmt = cc.createStatement();
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
                         mline.setCliGroup(clGroup.trim());
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
    	       cc.close();
                    } catch (Exception e) {
                        getParent().showError(e);
                    }
                 
      return lines;
}

  public Vector getPriceMatrixData() {
      Vector lines = new Vector();
     String sql = "  select '' as custid ,PDT.AccountRelation as clgroup ,prodid = ITM.ItemID,price = Convert(Numeric(28, 12),SubString(Max(Convert(Char(1), " +
        " PDT.SPMPromotion) +Convert(VarChar(8), PDT.FromDate, 112) +Convert(VarChar, PDT.Amount)),10,28)) " +
  " from InventTable ITM inner join PriceDiscTable as PDT with (nolock) on PDT.DataAreaID = 'BB' and " +
" PDT.ItemRelation = ITM.ItemID where	PDT.Module = 1 and	PDT.AccountCode = 1 and PDT.ItemCode = 0 and " +
"	PDT.FromDate <= CONVERT(date, GETDATE()) and (PDT.ToDate = '19000101' or PDT.ToDate >= CONVERT(date, GETDATE())) " + //  and pdt.accountrelation !='_1'
"	and  PDT.CURRENCY = 'EUR' group by PDT.AccountRelation,	ITM.ItemID order by PDT.AccountRelation,prodid";

      try {

         Connection cc = getNewConnection();
    	Statement stmt = cc.createStatement();
              ResultSet rs = stmt.executeQuery(sql);

              while (rs.next()) {

                    //  String custID = rs.getString("custid");
                    //  String klient = rs.getString("klient");
                      String clGroup = rs.getString("clgroup");
                      String prodID = rs.getString("prodid");
                     // String prodGroup = rs.getString("prodgroup");
                     // double discount = rs.getDouble("discount");
                      double price = rs.getDouble("price");
                      MatrixLine mline = new MatrixLine();
                      
                     
                      /*if (klient != null && klient.trim().length() > 0) {
                          mline.setCustID(klient.trim());
                  }
                      if (custID != null && custID.trim().length() > 0) {
                              mline.setCustID(custID.trim());
                      }*/
                      if (mline.getCustID() == null && clGroup != null
                                      && clGroup.trim().length() > 0) {
                              mline.setCliGroup(clGroup.trim());
                      }

                      if (prodID != null && prodID.trim().length() > 0) {
                              mline.setProdID(prodID.trim());
                      }
                      /*if (mline.getProdID() == null && prodGroup != null
                                      && prodGroup.trim().length() > 0) {
                              mline.setProdGroup(prodGroup);
                      }*/
                      //mline.setDiscount(discount);

                      if (price != 0 && price > 0.01) {
                              mline.setPrice(price);
                      }
                      
                    
                     lines.add(mline);
              }
              rs.close();
              
              
              String sql2 = "  select  pd.accountrelation AS clgroup,  " +
			  " (case when pd.itemcode = 0 then pd.itemrelation else ''end) AS prodid, " + 
			  " (case when pd.itemcode = 1 then pd.itemrelation else ''end) AS prodgroup,(percent1) AS discount, " +
			  "   pd.fromdate as algus , pd.todate as lopp ,amount as price from  " +
			  "   pricedisctable pd where  pd.dataareaid = 'bb' and  pd.fromdate<=CONVERT(date, GETDATE()) and pd.todate>=CONVERT(date, GETDATE()) and pd.accountcode=1  " +				 
			"	 and  pd.accountrelation !='_1' and pd.spmpromotion = 1 "+
			 "  and  PD.CURRENCY = 'EUR'  order by pd.accountrelation , pd.itemrelation  " ;
              
              rs = stmt.executeQuery(sql2);

              while (rs.next()) {

                      String clGroup = rs.getString("clgroup");
                      String prodID = rs.getString("prodid");
                      String prodGroup = rs.getString("prodgroup");
                      
                      String fromdate = rs.getString("algus");
                      String todate = rs.getString("lopp");
                      double price = rs.getDouble("price");
                      MatrixLine mline = new MatrixLine();
                      
                  
                      if (clGroup != null && clGroup.trim().length() > 0) {
                              mline.setCliGroup(clGroup.trim());
                      }

                      if (prodID != null && prodID.trim().length() > 0) {
                              mline.setProdID(prodID.trim());
                      }
                      if (mline.getProdID() == null && prodGroup != null
                                      && prodGroup.trim().length() > 0) {
                              mline.setProdGroup(prodGroup);
                      }
                      mline.setPrice(price);
                      mline.setStartDate(fromdate.substring(0,10));
                      mline.setEndDate(todate.substring(0,10));
                 
                    
                      lines.add(mline);
              }
              rs.close();
              stmt.close();
              cc.close();
              
              

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

  public String getSaveStockInfo(){
	
	  String    cmd =  "SELECT  IST.ITEMID as toode, SUM(IST.AVAILPHYSICAL) as stock FROM INVENTSUM IST with (nolock) " +
	            "  INNER JOIN INVENTDIM ID with (nolock) ON IST.INVENTDIMID=ID.INVENTDIMID and ID.DATAAREAID=IST.DATAAREAID " +
	            "  INNER JOIN INVENTLOCATION ILT with (nolock) ON  ID.INVENTLOCATIONID = ILT.INVENTLOCATIONID and ID.DATAAREAID = ILT.DATAAREAID" +
	            "  INNER JOIN INVENTTABLE IT with (nolock) on  IT.ITEMID=IST.ITEMID and   IT.DATAAREAID=IST.DATAAREAID WHERE IST.DATAAREAID='bb'and IST.CLOSED = 0 AND " +
	      " ID.INVENTLOCATIONID in (select INVENTLOCATIONID from INVENTLOCATION where DATAAREAID = 'bb' and STOCKEXPORT = 1) and IT.BBINVENTSUNI9 ='NNY' GROUP BY  IST.itemid" +
	" HAVING SUM(IST.AVAILPHYSICAL) > 0 union all select itemid as toode, sum(qty) as stock from [Axdb_dt].dbo.[DTInventSum]" +
	" where TplPartnerId in (select DTPartner from [Axdb_dt].dbo.[DTPartnerTable] where StockExportDT = 1) and   qty != 0 GROUP BY itemid order by  toode ";

		 



	         if (isDebug()) System.out.println(cmd);
	         StringBuffer products = new StringBuffer();
	         try {

	           Statement st = con.createStatement();
	           ResultSet rs = st.executeQuery(cmd);

	           while (rs.next()) {

	             String kood = rs.getString("toode").trim();
	             double seis = rs.getDouble("stock");
	             if(seis > 0){
	                 products.append(kood +  ";");
	                 products.append(" ;");
	                 products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
	            //   kogused.put(kood, seis + "");
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
/*
  
      DealerProduct dealerProds = new DealerProduct();
      try {

        String  sql = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock ,dim.inventlocationid as ladu FROM INVENTSUM summ " +
               "  INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid " +
               "  INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid  "+
               "  WHERE summ.DATAAREAID='bb'and dim.DATAAREAID='bb' and inv.DATAAREAID='bb' AND CLOSED = 0 AND  moduletype=0 " +
               "    and dim.inventlocationid like'%PROD%' " +
               "  GROUP BY dim.inventlocationid,summ.itemid  ORDER BY dim.inventlocationid,summ.itemid ";

           if (isDebug()) System.out.println(sql);
         
              Statement stm = con.createStatement();
              ResultSet rst = stm.executeQuery(sql);

                while (rst.next()) {
                  String toode = rst.getString("toode").trim();
                  int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                  String ladu  =  rst.getString("ladu").trim();
                  dealerProds.add(ladu, toode,seis+""," "," " );
              }
              rst.close();
              stm.close();

      } catch (Exception e) {
          getParent().showError(e);
      }

       return dealerProds;*/
	  return null;
     }

  
  
  
  public TreeMap getAgents(){
	  TreeMap agents = new TreeMap(); 
	
	 
	  try {
		  // leiame agentside kaubagrupid 
		  String cmd0 = "select emp.emplid as kood , ITEMSUBGROUPID  as grupp from TraAgentItemGroupTable tra Inner join TraAgentsTable emp  " +
         " on emp.dataareaid= tra.dataareaid and ltrim(rtrim(tra.agent))= ltrim(rtrim(emp.agentsid)) and tra.DataAreaId = 'bb' ";
		      if (isDebug()) System.out.println(cmd0);
		     
		      TreeMap gr = new TreeMap();
		        ResultSet rs = stmt.executeQuery(cmd0);
		          while(rs.next()){
		            String agent = rs.getString("kood").trim();
		            String gru = rs.getString("grupp").trim();
		          
		            Vector ss = (Vector)gr.get(agent);
		            
		              if(ss != null){
		                  ss.add(gru);
		                  gr.put(agent, ss);
		              }else{
		            	  ss = new Vector();
		            	  ss.add(gru);
		            	  gr.put(agent, ss);
		              }
		            
		          } 
		  //leiame asendajad 
		          String cmd1 = " select (select emplid from TraAgentsTable  x where x.dataareaid = 'bb' and x.agentsid = a.agent) as agent , " +
				" (select emplid from TraAgentsTable  x where x.dataareaid = 'bb' and x.agentsid = a.agentsubstitute) as asendaja " +
				" from  TraAgentSubstituteTable a where a.dataareaid = 'bb' and getdate() between a.startdate and a.enddate ";
		 		      if (isDebug()) System.out.println(cmd0);
		 		     
		 		      TreeMap repl = new TreeMap();
		 		      rs = stmt.executeQuery(cmd1);
		 		          while(rs.next()){
		 		            String agent = rs.getString("agent").trim();
		 		            String gru = rs.getString("asendaja").trim();
		 		          
		 		            Vector ss = (Vector)repl.get(agent);		            
		 		              if(ss != null){
		 		                  ss.add(gru);
		 		                 repl.put(agent, ss);
		 		              }else{
		 		            	  ss = new Vector();
		 		            	  ss.add(gru);
		 		            	 repl.put(agent, ss);
		 		              }
		 		            
		 		          } 
		          System.out.println("Asendajad " + repl.toString());
	  
    String cmd = "select emp.emplid as kood ,del_name + ' ' + agentsid as nimi from  TraAgentsTable tra inner join empltable emp " + 
         " on emp.dataareaid= tra.dataareaid and ltrim(rtrim(tra.emplid))= ltrim(rtrim(emp.emplid)) where  " +
         " emp.dataareaid='bb' and  tra.dataareaid ='bb' ";
    if (isDebug()) System.out.println(cmd);
   
    
         rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("nimi").trim();
          agent.setName(name);
          agent.setIsPilot(true);
          
          // s�time gruppid kylge 
        if(gr.get(agent.getID())!= null){
        	Vector vv = (Vector)gr.get(agent.getID());
        	for (int i = 0; i<vv.size();i++){
        		agent.addProdGroupList((String)vv.elementAt(i));
        	}
        }
          // s�time asendajad kylge 
          if(repl.get(agent.getID()) != null){
        		Vector vv = (Vector)repl.get(agent.getID());
            	for (int i = 0; i<vv.size();i++){
            		agent.addDubbler((String)vv.elementAt(i));
            		 System.out.println("Agent ja dubbler " + agent.getID() + " " + agent.getDubbler().toString());
            	} 
          }
          
          agents.put(name, agent);
          agents1.put(agent.getID(), agent);
        }
    } catch (Exception e){
        getParent().showError(e);
      }
    Agent aa = new Agent ("HOUSE");
    aa.setName("HOUSE");
    agents.put("HOUSE", aa);
    
    Agent aa1 = new Agent ("HOUSE1");
    aa1.setName("HOUSE1");
    agents.put("HOUSE1", aa1);
    
    Agent aa2 = new Agent ("HOUSE2");
    aa2.setName("HOUSE2");
    agents.put("HOUSE2", aa2);
    
    Agent aa3 = new Agent ("HOUSE3");
    aa3.setName("HOUSE3");
    agents.put("HOUSE3", aa3);
    
    
    // System.out.println("Agendid " + agents.toString());
    
    return agents;
  }
  
  private TreeMap fixAsendused = null;
  
  private void makePictureCat(){
	     
      	String  cmd2 = " SELECT  inv.itemid as toode FROM inventtable inv WHERE  inv.dataareaid='bb'   and bbinventsuni9 ='NNY'" ;
      	
      	
      	String picts = parent.getProps().getProperty("pict.dir");
    	    String pictspda = parent.getProps().getProperty("pictpda.dir");
    	 

      	try {
      		Connection con = this.getNewConnection();
      		Statement stmt = con.createStatement();
      	    ResultSet	      rs = stmt.executeQuery(cmd2);

   
           StringBuffer buf = new StringBuffer();
           StringBuffer buf0 = new StringBuffer();
           int a = 0;
           int b = 0;
           int c = 0;
           System.out.println("Piltide leidmine ");
      	    while (rs.next()) {
      	    	a++;
      	    	c++;
      	    	if(c == 100){
      	    		  c =0;
      	    		  System.out.println("Piltide leidmine "  + a);	
      	    	}
      	     
                    String toode = rs.getString("toode").trim();               
                    File f = new File(picts +"B"+ toode+".jpg");
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
//           	 L�ppes kustutamine 
           	 
             System.out.println("Piltide kopeerimine ");	
             //Failide kopeerimine  
               String[] gg = new File(picts).list();
            	  //System.out.println("gg " + gg[0] + " " + gg.length);
               
                for(int i = 0;gg.length>i;i++){
                	
                	File in = new File ( picts + gg[i]); 
                	if(in.isFile()){
                	
                	File inpda = new File ( pictspda + gg[i].substring(1)); 
               
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


  public TreeMap getCustomers(Agent agent){
System.out.println("Alustan kliendid ");
StringBuffer sb2 = new StringBuffer();
String sep = ";";
//Teeme kysimuste failid valmis 

String volg = "select CTB.TRAADDRESSID, CTR.ACCOUNTNUM AS maksja, CTR.ORDERACCOUNT AS klient,  Sum(CTR.AmountMst - CTR.SettleAmountMst) AS tasumata " +
"from CustTrans as CTR with (nolock)  left join CustInvoiceJour as CIJ with (nolock) on   CIJ.DataAreaID = CTR.DataAreaID and " +
"  CIJ.InvoiceAccount = CTR.AccountNum and CIJ.LedgerVoucher = CTR.Voucher and CIJ.InvoiceDate = CTR.TransDate  " +
" inner join CustTable as CTB with (nolock) on    CTB.DataAreaID = CTR.DataAreaID and " +
"            CTB.AccountNum = IsNull(CIJ.OrderAccount, CTR.AccountNum)  " +
" where CTB.DataAreaID = 'BB' and CTB.Name not like '%sl[e�]gts%' and CTB.TraAddressID <> '' and CTR.AmountMst > 0 and  CTR.DueDate < GetDate() + 5 " +
" group by CTB.TRAADDRESSID,   CTR.ACCOUNTNUM,  CTR.ORDERACCOUNT,   CTR.POSTINGPROFILE " +
" having Sum(CTR.AmountMst - CTR.SettleAmountMst) > 0 and CTR.POSTINGPROFILE = 'STD'" ;
  

String kyssad = "SELECT pea.id as pea,kys.id as kys,QUESTION,QUESTION_TYPE as tyyp ,ANSWER_VARIANTS as nimekiri ,kys.MANDATORY as peab FROM [E-agent].[epasaule].[QUESTIONARY] pea inner join " +
"[E-agent].[epasaule].[QUESTIONS] kys on kys.QUESTIONARY_ID=pea.id";
TreeMap kysimused = new TreeMap();
try{

	

	  
	
    String sql2 = "select clid AS kood,sentdate as kuup,agentnam AS agent  , mestext " +
  		"AS mess from mj_crm where sentdate > getdate()-60 order by clid,sentdate";
 Connection con2 = this.getNewConnection2();
      Statement stt = con2.createStatement();
  ResultSet rr = stt.executeQuery(sql2);
  StringBuffer sb = new StringBuffer ();
  
  while (rr.next()){
  	sb.append(rr.getString("kood").trim() + ";" + rr.getString("kuup").trim() + " " + rr.getString("agent").trim()
  			+ "|" + rr.getString("mess").trim() + "\n");
  }
  rr.close();
  stt.close();
  con2.close();
  
  if(sb.length()==0){
	  sb.append("kood;meaasge|message\n");
  }
      File f = new File( "customercrm.txt");
      if (f.exists())f.delete();
        try {
  		PrintWriter os = new PrintWriter(new FileWriter(
  				"customercrm.txt", true));
  		os.write(sb.toString());
  		os.close();
  	  }catch (IOException ex) {
  		ex.printStackTrace();
  	  }
	
	
	

	
	
	
	Connection conh = this.getNewConnection2();
	Statement stmh = conh.createStatement();
	StringBuffer buf = new StringBuffer();
	StringBuffer buf2 = new StringBuffer();
    ResultSet rs1h = stmh.executeQuery(kyssad);
    while(rs1h.next()){
    	String kood = rs1h.getString("pea").trim();
    	String kys = rs1h.getString("kys").trim();
    	String nimi = rs1h.getString("QUESTION").trim();
    	String tyyp = rs1h.getString("tyyp").trim();
    	String nimekiri = rs1h.getString("nimekiri");
    	String peab = rs1h.getString("peab").trim();
    	
    	
    	if(nimekiri != null && nimekiri.trim().length()>0){
    		StringTokenizer st = new StringTokenizer(nimekiri.trim(),",");
    		while(st.hasMoreElements()){
    			buf2.append(kood+kys + ";" +st.nextToken().trim() + "\n");		
    		}   	
    	}
    	  buf.append(kood + ";" + kys + ";" +nimi + ";" +tyyp + ";" + peab + "\n"  );
    	  	       
    }
    
    if(buf2.length() > 2){
         f = new File("custdetch.txt");
        if (f.exists())f.delete();
          try {
    		PrintWriter os = new PrintWriter(new FileWriter(
    				   "custdetch.txt", true));
    		os.write(buf2.toString());
    		os.close();
    	  }catch (IOException ex) {
    		ex.printStackTrace();
    	  }
     }
   

                 if(buf.length() > 2){
                     f = new File("questions.txt");
                    if (f.exists())f.delete();
                      try {
                		PrintWriter os = new PrintWriter(new FileWriter(
                				   "questions.txt", true));
                		os.write(buf.toString());
                		os.close();
                	  }catch (IOException ex) {
                		ex.printStackTrace();
                	  }
                 }
    
    System.out.println("Kysimused " +kysimused.size() );
   rs1h.close();
   stmh.close();
   conh.close();


}catch(Exception dd){
	dd.printStackTrace();
}




//klientide osa algab


 String cmd = " SELECT (cus.deliveryfilter + cus.name + case when cus.bblagentcomment != '' then ' // ' + cus.bblagentcomment else '' end)   as name, cus.traaddressid as traid,'' as nimelisa,cus.namealias,cus.accountnum,cus.invoiceaccount AS payer, " +
 " (SELECT sum(amountmst) FROM custtransopen co WHERE co.duedate<getdate() and co.dataareaid = 'bb'  " +
 "  AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else " + 
 " cus.invoiceaccount end )) AS debt, cus.linedisc ,  " +
"     cus.address, '' AS visit ,  numofdays AS term, blocked AS block ,  " +
" (select (blocked) from custtable cc where  cc.dataareaid='bb' and cc.accountnum=cus.invoiceaccount) " + 
			 "   as payerblock ,phone + ' ' + telefax as info , pricegroup as grupp , " +
			 "(select (creditmax) from custtable cc where   cc.dataareaid='bb' and cc.accountnum=cus.invoiceaccount) as credit " +
			 ", bbcustgroupid as grupp3 , PAYMMODE as ette FROM custtable cus left outer JOIN paymterm " +
			 " pay on cus.paymtermid=pay.paymtermid and pay.dataareaid = cus.dataareaid and  pay.dataareaid='bb' " +
			"	 WHERE   cus.dataareaid='bb'  and traaddressid != '' and cus.traaddressid!= 'PA0000' " +
			"    order by cus.name " ;
 

 String ccc = "select invoiceaccount from custtable where dataareaid='bb' and invoiceaccount != ''  group by invoiceaccount";

 
 String cont = "select custaccount as klient,name  + (case when phone != '' then ' Tel. '+ phone else '' end )  " +
 " + (case when cellularphone != '' then ' Mob. '+ cellularphone else '' end ) " +
 "+ (case when telefax != '' then ' Fax. '+ telefax else '' end ) " +
 "+ (case when email != '' then '|  '+ email else '' end )" +
 "as asi1  from contactperson  where dataareaid='bb' and name != ''";
 
 String qqq = "select tra.emplid as agent , custaccount as klient , day , periodtype ,'(' +tra.emplid +')' + agentsid + ' ' + del_name as nam ,Tsmanager as tyyp from TraAgentCustTable cus inner join TraAgentsTable tra on cus.dataareaid= tra.dataareaid " +  
				" and cus.agent = tra.agentsid INNER JOIN EMPLTABLE e ON e.dataareaid = tra.dataareaid  and cus.dataareaid = tra.dataareaid and e.emplid=tra.emplid " +
				" where cus.dataareaid = 'bb' and tra.dataareaid = 'bb' and e.dataareaid = 'bb'";
 
 
           String ddd = "select (accountnum) as klient,max(assortgroupid) as asi1, min(assortgroupid) as asi2 from assortgroups_cust where   dataareaid = 'bb' " +
                     " group by accountnum having  count (assortgroupid ) >0 ";


  if (isDebug()) System.out.println(cmd);
  
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    TreeMap custAgents = new TreeMap();
    fixAsendused = new TreeMap()  ;
    TreeMap tempFix = new TreeMap();
    Vector payers =  new Vector();
    try {
      	Connection cong = this.getNewConnection();
    	Statement stmg = cong.createStatement();
    	
        ResultSet rs1g = stmg.executeQuery(ddd);
        while(rs1g.next()){
        	String kood = rs1g.getString("klient").trim();
        	String sort1 = rs1g.getString("asi1").trim();
        	String sort2 = rs1g.getString("asi2").trim();
        	
        	if(!sort1.equals(sort2)){
        		tempFix.put(kood, sort1+"|"+sort2);  
        	}else{
        		tempFix.put(kood, sort1);	
        	}
        	
        	       
        }
        System.out.println("Suurused" +tempFix.size() );
       rs1g.close();
       stmg.close();
       cong.close();
    	
    	
    	Connection conh = this.getNewConnection();
    	Statement stmh = conh.createStatement();
    	TreeMap contacts = new TreeMap();
        ResultSet rs1h = stmh.executeQuery(cont);
        while(rs1h.next()){
        	String id = rs1h.getString("klient").trim();
        	String contact = rs1h.getString("asi1").trim();
        	
        	
        	if(contacts.get(id) != null){
        		contacts.put(id, contacts.get(id)+"|"+contact);  
        	}else{
        		contacts.put(id, contact);  	
        	}
        	
        	       
        }
        System.out.println("Suurused" +tempFix.size() );
       rs1h.close();
       stmh.close();
       conh.close();
    	
  
       
    	Connection conr = this.getNewConnection();
    	Statement stmr = conr.createStatement();
    	
        ResultSet rs1 = stmr.executeQuery(ccc);
        while(rs1.next()){
        	
       payers.add(rs1.getString("invoiceaccount"));
          
        }
       rs1.close();
       stmr.close();
       conr.close();
    	
       Connection cone = this.getNewConnection();
       Vector kliendid = new Vector();
   	Statement stme = cone.createStatement();
   	TreeMap custagVisit = new TreeMap();
 	TreeMap custagName = new TreeMap();
 	TreeMap agNames = new TreeMap();
 	TreeMap agendiTyyp = new TreeMap();
       ResultSet rse = stme.executeQuery(qqq);
       while(rse.next()){
   	   
    		String age = rse.getString("agent").trim();
       	    String klient = rse.getString("klient").trim();
       	    String day = rse.getString("day");
       	    String period = rse.getString("periodtype");
       	    String name = rse.getString("nam");
       	    String tyyp = rse.getString("tyyp");
      
       	    if(tyyp != null && tyyp.trim().equals("1")){
       	 	 agendiTyyp.put(age, tyyp);
       	    }
       	  String old = (String)custagVisit.get(age + klient);
       	   String visit = this.getMJDay(day, period,old);
       	   custagVisit.put(age + klient, visit);
           custagName.put(age + klient, name);
          
       	 kliendid.add(klient);
       	 
       	 
       if(custAgents.get(klient) != null){
    	   Vector vv = (Vector)custAgents.get(klient) ;
    	   vv.add(age);
    	   custAgents.put(klient, vv);
       }else{
    	   Vector vv = new Vector() ;
    	   vv.add(age);
    	   custAgents.put(klient, vv);
       }
    	   
    	 
       }
      rse.close();
      stme.close();
      cone.close();
       
      
  	
  	
     	TreeMap debtover = new TreeMap();
      	
  	  	Connection congd = this.getNewConnection();
      	Statement stmgd = congd.createStatement();
      	
          ResultSet rs1gd = stmgd.executeQuery(volg);
          while(rs1gd.next()){
          	String kood = rs1gd.getString("maksja").trim();
          	double yle = rs1gd.getDouble("tasumata");
          	debtover.put(kood, yle + "");
          }
          System.out.println("Debtover" +debtover.size() );
         rs1gd.close();
         stmgd.close();
         congd.close();
      	
      	
  	

      
      
      System.out.println("agent klient tehtud ");
    	Statement stmt = this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      Statement stm = getNewConnection().createStatement();
      while(rs.next()){
        String name = rs.getString("name").trim();
      
        String traid = rs.getString("traid").trim();
        
        String panenim = name + traid;
     //   String alias = rs.getString("namealias");
     
        
        
        String accountnum = rs.getString("accountnum").trim();
        if(kliendid.contains(accountnum)){
        
        Customer cust = new Customer(accountnum);

     
        String payer = rs.getString("payer");
        if(payer == null || payer.trim().length()== 0){
        	cust.setPayer(cust.getID());
        }else{
        	cust.setPayer(payer.trim());
        }
        cust.setPayer(payer);
        String debtStr = rs.getString("debt");
        if (debtStr != null){
         // cust.setDebt(Double.parseDouble(debtStr));
        }
        
        
        if(debtover.get(cust.getPayer()) != null){
        	  cust.setDebt(Double.parseDouble((String) debtover.get(cust.getPayer())));
  	
        }
        if(debtover.get(cust.getID()) != null){
      	  cust.setDebt(Double.parseDouble((String) debtover.get(cust.getID())));
	
      }

        if(tempFix.get(cust.getID()) != null){
        	cust.addFixSortCode((String) tempFix.get(cust.getID()));
        }
        
        String aleke = rs.getString("linedisc");
        if(aleke != null && aleke.trim().length()>0){
            aleke = aleke.trim();
            cust.setGroup(aleke);
        } else {
        	aleke = "000" ;
        }

        String aadr = rs.getString("address");
        String aaf = "";
        if (aadr != null) {
          cust.setAddress( Converter.adressLink(Converter.enCodeEst(aadr.trim().replaceAll("\n"," ").replaceAll("\r", " ")),"Latvia"));
          aaf = aadr.trim().replaceAll("\n"," ").replaceAll("\r", " ");
        }
       
        cust.setName( traid + " " + Converter.enCodeEst(name) + ": " + Converter.enCodeEst(aaf));
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
   
        if(custAgents.get(cust.getID()) != null){
        	Vector vv = (Vector) custAgents.get(cust.getID());
        	for(int i = 0; i < vv.size();i++){
        		
        		Agent aa = new Agent((String) vv.elementAt(i));
        		String nam = (String)custagName.get(aa.getID() + cust.getID());
        		if(nam == null ||  nam.trim().length() == 0){
        			nam = " ";
        		}
        		sb2.append(cust.getID() + sep + aa.getID() +sep);
        		 sb2.append(aa.getID() + " " + nam + "\n");
        		
        		String visi = (String)custagVisit.get(aa.getID() + cust.getID());
        		if(visi != null && visi.trim().length() > 0){
        			aa.setVisit(visi);	
        		}
        		
        	
        		if(nam != null && nam.trim().length() > 0 && agendiTyyp.get(cust.getID()) == null ){
        			cust.setCity("Manager: " + Converter.enCodeEst(nam) + "|");
        		}
        		 cust.addAgent(aa);	
        		
        		 
        		 //lisame dublandid 
        		 
        				Agent agg = ((Agent)this.getAgents1().get(aa.getID()));
        			
        				if(agg != null && agg.getDubbler().size()>0){
        					
        					//System.out.println("lisame kliendile dublanti " + aa.getID() + " " +  agg.getDubbler() );
        					for (int r = 0 ; r < agg.getDubbler().size() ; r++){
        						cust.addAgent(new Agent((String)agg.getDubbler().elementAt(r)));
        					}
        				}
        		
        	}
        	
        }

        cust.addAgent(new Agent("HOUSE"));
        cust.addAgent(new Agent("HOUSE1"));
        cust.addAgent(new Agent("HOUSE2"));
        cust.addAgent(new Agent("HOUSE3"));
        cust.addAgent(new Agent("HOUSE4"));
        
        int block = rs.getInt("block");
        if (block == 1 ) {
          cust.setSalesBlock(true);
        }
        
        if (block == 2 ) {
            cust.setSalesBlockT(true);
          }
        
        if (block == 3 ) {
            cust.setInfo("Cash Only");
          }
        
        int payerblock = rs.getInt("payerblock");
        if (payerblock == 1) {
          cust.setSalesBlock(true);
        }
        
        if (payerblock == 2) {
            cust.setSalesBlockT(true);
          }
        if (payerblock == 3) {
        	  cust.setInfo("Cash Only");
          }
        
        String info = rs.getString("info");
        if (info != null && info.trim().length()>0) {
          cust.setPersonalData( Converter.enCodeEst(info.trim())); 
        }
        
        if(contacts.get(cust.getID()) != null){       	
        	 cust.setPersonalData( Converter.enCodeEst(cust.getPersonalData() + "|" + contacts.get(cust.getID())) );
        }
        
        String grupp = rs.getString("grupp");
        if (grupp != null && grupp.trim().length()>0) {
          cust.setGroup(grupp.trim());
        }
        
        
        double credit = rs.getDouble("credit");
        if(credit > 1){
        	cust.setCredit(credit + "");
        }
        String grupp3 = rs.getString("grupp3");
        if (grupp3 != null && grupp3.trim().length()>0) {      
          cust.setCity(cust.getCity() + "Cenu grupa: " + Converter.enCodeEst(grupp3.trim()));
        }
        
        String ette = rs.getString("ette");
        if (ette != null && ette.trim().equals("PrePayment")) {
        if(cust.getInfo() == null || cust.getInfo().trim().length() == 0){
      	  cust.setInfo("Apmaksas veids: prieksapmaksa");
        }else{
        	 cust.setInfo(cust.getInfo() + "\nApmaksas veids: prieksapmaksa");
        }
        }
        
        String cmd2 = "SELECT disc.percent1 AS discount FROM pricedisctable disc  WHERE disc.accountrelation='" + aleke + "' AND " +
               " disc.dataareaid='bb' AND len(disc.itemrelation)=0  AND fromdate<=CONVERT(date, GETDATE()) and todate >= CONVERT(date, GETDATE())";
             rs1 = stm.executeQuery(cmd2);
                     while(rs1.next()){
                       double ale = rs1.getDouble("discount");
                       cust.setDiscount(ale);
                     }
                    rs1.close();
                    boolean laks = true;
                    if((payer == null || payer.trim().length() == 0) && payers.contains(cust.getID())){
                    	//laks = false;
                    }
                    if(laks){
                        customers.put(panenim, cust);
                        custMap.put(cust.getID(),cust);	
                    }
        }
              
      }
System.out.println("Kliendi tehtud alustan v�la listiga ");


String sql = "select CTR.ACCOUNTNUM AS maksja,CTR.ORDERACCOUNT AS klient, CTR.TransDate AS kuupaev, CTR.Invoice AS number,   Sum(CTR.AmountMst - CTR.SettleAmountMst)  AS tasumata, CTR.DueDate AS maksepaev, datediff(day,CTR.DUEDATE,getdate()) AS yle " +
" from CustTrans as CTR with (nolock) left join CustInvoiceJour as CIJ with (nolock) on CIJ.DataAreaID = CTR.DataAreaID and CIJ.InvoiceAccount = CTR.AccountNum and CIJ.LedgerVoucher = CTR.Voucher and CIJ.InvoiceDate = CTR.TransDate " +
" inner join CustTable as CTB with (nolock)     on CTB.DataAreaID = CTR.DataAreaID and CTB.AccountNum = IsNull(CIJ.OrderAccount, CTR.AccountNum) " +
" where  CTB.DataAreaID = 'BB' and CTB.Name not like '%sl[e�]gts%' and CTB.TraAddressID <> '' and   CTR.AmountMst > 0 and CTR.DueDate < GetDate() + 5 " + 
" group by  CTR.ACCOUNTNUM,  CTR.ORDERACCOUNT, CTR.TransDate,  CTR.Invoice,  CTR.DueDate, CTR.POSTINGPROFILE " +
" having Sum(CTR.AmountMst - CTR.SettleAmountMst) > 0 and CTR.POSTINGPROFILE = 'STD'";

      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim());
                        debt.setDate(rs.getDate("kuupaev"));
                        String number = rs.getString("number").trim();
                        debt.setDoknr(number);
                        debt.setSum(rs.getDouble("tasumata"));
                        debt.setDuedate(rs.getDate("maksepaev"));
                      
                     
                        debt.setDue(rs.getString("yle"));
                        this.debt.addElement(debt);
                      }

                      rs.close();
                      
                      
                      if(sb2.length() > 2){
                          File f = new File( "clientagent.txt");
                          if (f.exists())f.delete();
                            try {
                      		PrintWriter os = new PrintWriter(new FileWriter(
                      				 "clientagent.txt", true));
                      		os.write(sb2.toString());
                      		os.close();
                      	  }catch (IOException ex) {
                      		ex.printStackTrace();
                      	  }
                       }
                      
                      
                      

    } catch (Exception e){
      getParent().showError(e);
    }
    System.out.println("V�la list tehtud  ");  
    return customers;
  }

  public TreeMap getAgents1() {
		return agents1;
	}

private TreeMap agents1 = new TreeMap();

  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
  private Connection getNewConnection2() throws SQLException{
	    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	  }


      public String getDistribution() {
        return null;
      }


  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "   select groupid as liik , name as nimi from BBLItemSubGroupTable where dataareaid = 'bb'  ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik, Converter.enCodeEst(rs.getString("nimi").trim()));
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}


  public TreeMap getProdGroupsSecond() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "  select trabrandid as liik , description as nimetus from TraBrandtable where dataareaid = 'bb' ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik, Converter.enCodeEst(rs.getString("nimetus").trim()));
    }
    rs.close();
    st.close();
    con.close();

    return tm;
  }

  private String getMJDay(String day, String period, String old){
	  String res = "";
	  
	  if(day.equals("0")){
		  res = " ";
	  }

	  
	  if(day.equals("1")){
		  res = "E";
	  }
	  if(day.equals("2")){
		  res = "T";
	  }

	  if(day.equals("3")){
		  res = "K";
	  }

	  if(day.equals("4")){
		  res = "N";
	  }

	  if(day.equals("5")){
		  res = "R";
	  }

	  if(day.equals("6")){
		  res = "L";
	  }

	  if(day.equals("7")){
		  res = "P";
	  }

	  if(day.equals("8")){
		  res = "ETKNR";
	  }
	  
	  if(period.equals("2")){
		  res = res + "2";
	  }

	  if(period.equals("3")){
		  res = res + "1";
	  }
	  
	  if(old != null){
		  res = old + res ;
	  }
	  
	  return res;
	  
  }
  public static final String addSpaces(String s, int totalLen){
	    int len = s.length();
	    StringBuffer sb = new StringBuffer(s);
	    for (int i=len; i < totalLen ; i++){
	      sb.append(' ');
	    }
	    if( len > totalLen){
	    	return s.substring(0,totalLen);
	    }
	    return sb.toString();
	  }
  
  public TreeMap getProducts(Agent agent){
	  
	  
  String  cmd2 =  "SELECT  IST.ITEMID as toode, SUM(IST.AVAILPHYSICAL) as stock FROM INVENTSUM IST with (nolock) " +
	            "  INNER JOIN INVENTDIM ID with (nolock) ON IST.INVENTDIMID=ID.INVENTDIMID and ID.DATAAREAID=IST.DATAAREAID " +
	            "  INNER JOIN INVENTLOCATION ILT with (nolock) ON  ID.INVENTLOCATIONID = ILT.INVENTLOCATIONID and ID.DATAAREAID = ILT.DATAAREAID" +
	            "  INNER JOIN INVENTTABLE IT with (nolock) on  IT.ITEMID=IST.ITEMID and   IT.DATAAREAID=IST.DATAAREAID WHERE IST.DATAAREAID='bb'and IST.CLOSED = 0 AND " +
	      " ID.INVENTLOCATIONID in (select INVENTLOCATIONID from INVENTLOCATION where DATAAREAID = 'bb' and STOCKEXPORT = 1) and IT.BBINVENTSUNI9 ='NNY' GROUP BY  IST.itemid" +
	" HAVING SUM(IST.AVAILPHYSICAL) > 0 union all select itemid as toode, sum(qty) as stock from [Axdb_dt].dbo.[DTInventSum]" +
	" where TplPartnerId in (select DTPartner from [Axdb_dt].dbo.[DTPartnerTable] where StockExportDT = 1) and   qty != 0 GROUP BY itemid order by  toode ";

		 
	  

  
    String cmd = " SELECT  inv.itemid, inv.namealias AS nimetus, tracrateqty AS kastis,standardpalletquantity AS alusel, " +
    "(select top 1 disc.amount from pricedisctable disc where inv.itemid=disc.itemrelation and inv.dataareaid=disc.dataareaid  and disc.accountrelation ='_1' " +
   " and (disc.todate>getdate() OR disc.todate=('1900-01-01 00:00:00.00') or disc.todate is null ) and  disc.dataareaid='bb') AS price , mod.price * " + rate + " as price2  " +
   " , itembarcode AS ean ,inv.bblitemsubgroupid AS grupp,inv.trabrandid AS grupp2,mod.unitid As unit, itemgroupid as vat " +
   " ,del_quantity as optimum ,del_lowestqty as minimum , abctype as abc , itemgroupid as tyyp FROM inventtable inv " + 
   " left join inventitembarcode bar ON bar.itemid=inv.itemid and bar.dataareaid=inv.dataareaid and useforprinting = 1 " +
   " left JOIN inventtablemodule mod ON mod.itemid = inv.itemid and inv.dataareaid=mod.dataareaid and mod.moduletype = 2 " +
   "    inner join InventItemSalesSetup iss on iss.itemid = inv.itemid and   iss.dataareaid = inv.dataareaid " +
   "  WHERE  inv.dataareaid = 'bb' and  mod.dataareaid = 'bb' and iss.stopped = 0 and   inv.bbinventsuni9 ='NNY' ";

	  
	  String  cmduus =  " SELECT TRJL.ItemId as toode, sum(TRJL.QtyInitial - TRJL.QtyUsed) as stock FROM TRARESGROUPTABLE TRGT " +
        "  inner join TraResJournalTable TRJT on  TRGT.GroupCode = TRJT.ResGroupCode and " +
         "       TRGT.DataAreaId = TRJT.DataAreaId left join TraResJournalLine TRJL on " +
          "      TRJT.ResGroupCode = TRJL.ResGroupCode and TRJT.JournalId = TRJL.JournalId and TRJT.DataAreaId = TRJL.DataAreaId " +
 " inner join InventTable ITB on TRJL.ItemId = ITB.ItemId and  TRJL.DataAreaId = ITB.DataAreaId " +
   "       inner join InventItemBarcode BAR on  ITB.ItemId = BAR.ItemId and  ITB.DataAreaId = BAR.DataAreaId and  BAR.USEFORPRINTING = 1 " +
  " WHERE  TRGT.DataAreaId = 'bb' GROUP BY TRJL.ItemId,  BAR.ItemBarcode having   sum(TRJL.QtyInitial - TRJL.QtyUsed) > 0";

	  

    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    TreeMap stocks = new TreeMap();
    TreeMap stocksNimi = new TreeMap();
    try {
        Connection ccc = this.getNewConnection();
    	Statement stmt =  ccc.createStatement();
        ResultSet rs = stmt.executeQuery(cmd2);
       
        while(rs.next()){
        	String toode= rs.getString("toode").trim();
        	double kogus = rs.getDouble("stock");
        	stocks.put(toode, kogus + "");
        }
        rs.close();
        stmt.close();
        ccc.close();
        
        ccc = this.getNewConnection();
   	 stmt =  ccc.createStatement();
       rs = stmt.executeQuery(cmduus);
      
       while(rs.next()){
       	String toode= rs.getString("toode").trim();
       	double kogus = rs.getDouble("stock");
       	stocksNimi.put(toode, ((int)kogus) + "");
       }
       rs.close();
       stmt.close();
       ccc.close();
        
        
        ccc = this.getNewConnection();
        stmt = ccc.createStatement();
        rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimetus").trim() + ": " + kood ;
        String panenim = nimetus;
   
        if(stocks.get(kood) != null){
        	prod.setStock(Double.parseDouble((String)stocks.get(kood)));
        }
        
        prod.setInCases(rs.getDouble("kastis"));
      //  prod.setOnPallet(rs.getDouble("alusel"));
        double pric = rs.getDouble("price");
        if(pric == 0){
        	pric = rs.getDouble("price2");
        }
        prod.setPrice(pric);

        String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
           prod.setEAN(ean.trim());
           if(ean.trim().length() < 13){
        	   ean = addSpaces(ean.trim(),13);
           }
          
         }else{
        	 ean = "             ";
         }
        if(ean.trim().length() == 12) {
        	ean = ean + " ";
        }
        ean = ean.substring(0,9) + "  " + ean.substring(9);
     String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
        prod.setGroup(grupp.trim());
     }

      String grupp2 = rs.getString("grupp2");
       if(grupp2!=null && grupp2.trim().length()>0){
        prod.setGroupSecond(grupp2.trim());
     }
       if(stocksNimi.get(kood)!= null){
          	nimetus = nimetus + " |s:" + stocksNimi.get(kood) +"|"; 
          }
      prod.setName(Converter.enCodeEst("          " + ean + "     "+  nimetus));

      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit);
    }
      
      String vat = rs.getString("vat");
      if(vat!=null && vat.trim().length()>0){
       prod.setVat(vat.trim().substring(1));
    }
      double optim = rs.getDouble("optimum");
       if(optim >0){
         prod.setOptAmountInUnits(optim);
       }
  
       double minamount = rs.getDouble("minimum");
       if(minamount >0){
         prod.setMinAmountInUnits(minamount);
       }
  
       
       String type = rs.getString("abc");
       if(type!=null && type.trim().length()>0){
    	   if(type.trim().equals("1")){
    		   prod.setType("A");
    	   }
    	   if(type.trim().equals("2")){
    		   prod.setType("B");
    	   }
    	   if(type.trim().equals("3")){
    		   prod.setType("C");
    	   }     
     }
       type = rs.getString("tyyp");
       if(type!=null && type.trim().length()>0){
    	      prod.setType(type.substring(0,1));   	   
     }
        products.put(panenim, prod);
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
    HashMap custs = new HashMap();
    //if (true) return custProds ;
 int counter = 1;
 String cmd = "SELECT jou.orderaccount as customer,tra.itemid ,tra.qty,jou.invoicedate AS lastdate ,jou.INVOICEID as arve,sal.traoriginalqty as orig" +
 " FROM salesline sal with(nolock) left join custinvoicetrans tra with(nolock) on sal.dataareaid=tra.dataareaid   " +
 	"   and sal.salesid = tra.salesid  and sal.itemid= tra.itemid " +
     "   INNER JOIN custinvoicejour jou with(nolock) ON  " +
    "   tra.invoiceid=jou.invoiceid and  tra.dataareaid= jou.dataareaid   " +
 	"   INNER JOIN inventTABLE cus with(nolock) ON cus.itemid=tra.itemid and  tra.dataareaid = cus.dataareaid  and jou.dataareaid = cus.dataareaid  " +
    "   WHERE  jou.invoicedate > getdate()-130 AND tra.dataareaid = 'bb'  AND jou.dataareaid  = 'bb' AND tra.dataareaid  = 'bb' and cus.dataareaid='bb'  "+   
     "   GROUP BY jou.orderaccount,tra.itemid ,cus.itemname,jou.invoicedate ,sal.traoriginalqty,tra.qty,jou.INVOICEID " +
 	"   ORDER BY jou.orderaccount,cus.itemname " ;
   if (isDebug()) System.out.println(cmd);

    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      counter++;
      while(rs.next()){
    	  
        String kliendikood = rs.getString("customer").trim();
        String tootekood = rs.getString("itemid").trim();
		        double kogus = rs.getDouble("qty");
		        String lastdate = rs.getString("lastdate").substring(0,10);
		        String lastdoc = rs.getString("arve").trim();
		        double orig =  0;
		        if(kogus > 0 ){
		        	orig = rs.getDouble("orig")-kogus;
		        }
		     
		        if(orig < 0){
		        	orig = 0;
		        }
		        lastdate = lastdate.substring(0,10);
		        custs.put(kliendikood + tootekood,lastdate); 
		        lastdate = lastdate.substring(5,10); 
		                 
		        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,0,orig,0," ");
		         cp.setAbiKood(lastdoc);
		         custProds.add(cp);
        
      }
      
      
    } catch (Exception e){
      getParent().showError(e);
      if(counter< 4){
      	try{
        	 ResultSet rs = stmt.executeQuery(cmd);
             counter++;
             while(rs.next()){
            	  String kliendikood = rs.getString("customer").trim();
                  String tootekood = rs.getString("itemid").trim();
          		        double kogus = rs.getDouble("qty");
          		        String lastdate = rs.getString("lastdate").substring(0,10);
          		        String lastdoc = rs.getString("arve").trim();
          		        lastdate = lastdate.substring(0,10);
          		        custs.put(kliendikood + tootekood,lastdate); 
          		        lastdate = lastdate.substring(5,10); 
          		                 
          		        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,0,0,0," ");
          		         cp.setAbiKood(lastdoc);
          		         custProds.add(cp);
             }
      	}catch (Exception ee){

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
    	    
         
         
         String cmd =" SELECT  cus.emplid as agent,round(SUM(lineamount),0) AS summa  " +
         "   FROM custinvoicetrans sal  INNER JOIN custinvoicejour jou ON " +
         "   sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid  INNER JOIN TraAgentsTable cus ON cus.agentsid = " +
         "jou.traagentsid and jou.dataareaid= cus.dataareaid  WHERE " +
   	  "	  sal.dataareaid='bb' AND cus.dataareaid='bb'  AND jou.dataareaid='bb' AND YEAR(sal.invoicedate) = YEAR(getdate()) " +
         "    AND MONTH(sal.invoicedate) = MONTH(getdate())     GROUP BY cus.emplid ";

            if (isDebug()) System.out.println(cmd);
               try {
                   Statement stmt =  this.getNewConnection().createStatement();
                   ResultSet	      rs = stmt.executeQuery(cmd);
                   while(rs.next()) {
                     String agentID = rs.getString("agent").trim();
                     Agent agent = (Agent)agents.get(agentID);
                     if (agent != null){
                       agent.setMonthSales((int)rs.getDouble("summa"));
                 
                      // System.out.println(agent.getMonthSales());
                     }
                   }
                   rs.close();
                   con.close();
               } catch (Exception e) {
                   getParent().showError(e);
                }
             
       
               
               String cmd2 =" SELECT cus.emplid as agent,jou.TraSalesTypeId as type , round(SUM(lineamount),0) AS summa  FROM custinvoicetrans sal  with (nolock) " +
   			" INNER JOIN custinvoicejour jou with (nolock) ON sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid  " +
   			" INNER JOIN TraAgentsTable cus with (nolock) ON 	cus.agentsid = jou.traagentsid and jou.dataareaid= cus.dataareaid " +  
   			" WHERE sal.dataareaid='bb' AND cus.dataareaid='bb' AND jou.dataareaid='bb' AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate())  " +
   			" GROUP BY jou.TraSalesTypeId, cus.emplid ";

                  if (isDebug()) System.out.println(cmd2);
                     try {
                         Statement stmt =  this.getNewConnection().createStatement();
                         ResultSet	      rs = stmt.executeQuery(cmd2);
                         while(rs.next()) {
                       	
                           String agentID = rs.getString("agent").trim();
                           Agent agent = (Agent)agents.get(agentID);
                           if (agent != null){
                           	  String type = rs.getString("type").trim();
                           	     int summa  = (int)rs.getDouble("summa");
                           	 
                            agent.setStart(agent.getStart() + getText(type) + " : " + summa + "ls|"  );
                            // System.out.println(agent.getMonthSales());
                           }
                         }
                         rs.close();
                         con.close();
                     } catch (Exception e) {
                         getParent().showError(e);
                      }
               
       
                      cmd2 =" SELECT cus.EmplId as agent , jou.TraSalesTypeId as type , round(SUM(lineamount),0) AS summa  " +
   			 " FROM SalesLine sal  with (nolock) INNER JOIN SalesTable jou with (nolock) ON " +
   			   "      	sal.SalesId=jou.SalesId and jou.dataareaid= sal.dataareaid   " +
   			" INNER JOIN TraAgentsTable cus with (nolock) ON cus.agentsid = jou.traagentsid and jou.dataareaid= cus.dataareaid  " +
   			" WHERE sal.dataareaid='bb' AND cus.dataareaid='bb' AND jou.dataareaid='bb' AND YEAR(jou.CreatedDateTime) = YEAR(getdate()) AND  " +
   			"	MONTH(jou.CreatedDateTime) = MONTH(getdate()) and jou.TraSalesTypeId in ( 'STD','A') GROUP BY 	jou.TraSalesTypeId,cus.emplid ORDER BY jou.TraSalesTypeId desc ";

                        if (isDebug()) System.out.println(cmd2);
                           try {
                               Statement stmt =  this.getNewConnection().createStatement();
                               ResultSet	      rs = stmt.executeQuery(cmd2);
                               while(rs.next()) {
                             	
                                 String agentID = rs.getString("agent").trim();
                                 Agent agent = (Agent)agents.get(agentID);
                                 if (agent != null){
                               	  String type = rs.getString("type").trim();
                            	     int summa  = (int)rs.getDouble("summa");
                            	 
                                  agent.setStart(agent.getStart() + getText2(type) + " : " + summa + "ls|"  );
                             // System.out.println(agent.getMonthSales());
                                 }
                               }
                               rs.close();
                               con.close();
                           } catch (Exception e) {
                               getParent().showError(e);
                            }
                     
       }
       
       private String getText2(String asi ){
       	if(asi.equals("A")){
       		return Converter.enCodeEst("Atgrie�ana (pas�t�jums)");
       	}
       	if(asi.equals("STD")){
       		return Converter.enCodeEst("P�rdo�ana (pas�t�jums) kop�");
       	}
       	return asi;
       }
       
       private String getText(String asi ){
       	if(asi.equals("A")){
       		return Converter.enCodeEst("Atgrie�ana");
       	}
       	if(asi.equals("STD")){
       		return Converter.enCodeEst("P�rdo�ana");
       	}
       	if(asi.equals("Kplus")){
       		return Converter.enCodeEst("Korekcija(+)");
       	}
       	if(asi.equals("K")){
       		return Converter.enCodeEst("Korekcija(-)");
       	}
       	return asi;
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
    return "AxaBuraLat";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent){



	       String sql2 =  "SELECT sal.orderid AS arve, sal.orderdate AS kuupaev,cus.name AS klient, count(rid.itemid) AS summa  " +
	    " FROM perinttelematable sal INNER JOIN custtable cus ON sal.CUSTACCOUNT=cus.accountnum  " +
	    " INNER JOIN perinttelemaline rid ON sal.orderid=rid.orderid and sal.dataareaid=rid.dataareaid  WHERE sal.dataareaid='bb' AND cus.dataareaid='bb'  AND rid.dataareaid='bb' AND cus.salesrep ='" + agent.getID() + "' AND " +
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
	          sb.append("Nimi  \t\tLadu\t\tM��dud\t\tSumma\n");
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
	          sb.append("\tP�eva m��k kokku: " + totalKokku + " EUR");

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
