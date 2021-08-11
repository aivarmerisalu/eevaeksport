package eevaeksport.imp.driver;


import java.io.File;
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
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class IceAxaLatImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public IceAxaLatImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
        , "bmssa", "bmssa_pwd");
      //con.setAutoCommit(false);
      //con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
      con2 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
        , "bmssa", "bmssa_pwd");
      con1 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
        , "bmssa", "bmssa_pwd");
      con5 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
        , "bmssa", "bmssa_pwd");
   
      con4 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
         , "bmssa", "bmssa_pwd");
      con6 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , "bmssa", "bmssa_pwd");
      //stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }

  
  public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  
  public String identifyCustomer(String id,String regnr){
  	 String sql1 = "";
  	 if(regnr != null && regnr.trim().length()>0){
  	 	sql1 = "SELECT accountnum FROM custtable where ouraccountnum='" + id +   "' and regnum_w ='" + regnr + "' and dataareaid='bal'";
  	     	
  	 }else{
  	 	sql1 = "SELECT accountnum FROM custtable where ouraccountnum='" + id +   "'  and dataareaid='bal'";
  	    
  	 }
  	String sql3 = "SELECT accountnum FROM custtable where (ltrim(accountnum)= '" + id + "' or ouraccountnum='" + id + "') and dataareaid='bal'";
     String acc = null;
try{
   Statement st1 = con1.createStatement();
   ResultSet rs1 = st1.executeQuery(sql1);

    while(rs1.next()){
      acc = rs1.getString("accountnum").trim();

   }
    System.out.println("1" +acc);
    System.out.println(sql1);
	
	
 rs1.close();
 st1.close();


   Statement st3 = con.createStatement();
   ResultSet rs3 = st3.executeQuery(sql3);

    while(rs3.next()){
      acc = rs3.getString("accountnum").trim();
   }
    System.out.println("2" +acc);
	
    
 rs3.close();
 st3.close();
}catch(Exception e){
	e.printStackTrace();
}


     return acc;
  }

  public String identifyProduct(String ean, String prodID){
    String sql1 = "SELECT itemid FROM inventtable where itemid='" + prodID + "' and dataareaid='bal'";
    String sql2 = "SELECT itemid FROM inventtable where namealias='" + ean + "' and dataareaid='bal'";

    String prod = null;
try{

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      prod = rs1.getString("itemid").trim();
    }

    if(prod == null){

      rs1 = st1.executeQuery(sql2);

      while (rs1.next()) {
        prod = rs1.getString("itemid").trim();
      }
    }
    rs1.close();
    st1.close();
	
    }catch(Exception e){
	e.printStackTrace();
    }
    
    return prod;
  }

  Vector cClients = null;
  
  public void makeOrder(Order order) throws SQLException{
    //if (true) return;
    setOrder(order);

	cClients = new Vector();
    String cashClient =  parent.getProps().getProperty("cash.clients");

    if(cashClient != null){
    
    	StringTokenizer st = new StringTokenizer(cashClient,",");
    	while(st.hasMoreTokens()){
    		cClients.add(st.nextElement());
    	}
    }

    String salesid = updateMyygNumberSequence();

    if(cClients.contains(order.getCompany())){
    	makeOutput(order);
    }else{
    	System.out.println("lisan Baasi  ");
    	   addInvoiceHeader(salesid );
    	    addInvoiceLines(salesid);	
    }
  
    String sql4 = "update mjsalestable set balinprocess=0 where ltrim(salesid) = ? ";

    PreparedStatement st2 = con2.prepareStatement(sql4);
    st2.setString(1, salesid.trim());
    boolean test = false ;
    
    
   if("true".equals(parent.getProps().getProperty("testing"))){
	   test = true;
   }
   
   if(!test){
	   st2.executeUpdate(); //v�ljas sest testin  
   }
 
   
  }

  private String getDateX(Calendar cal){
	  StringBuffer sb = new StringBuffer();
	  String year =   cal.get(Calendar.YEAR) + "";
	  String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	  String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	   if (day.length() == 1){
	    sb.append("0");
	  }
	   sb.append(day);
	   sb.append("-");
	  if (month.length() == 1){
	   sb.append("0");
	 }
	   sb.append(month );
	  sb.append( "-" + year);
	  return sb.toString();
	}

  
  private void makeOutput(Order order){
	    
	  StringBuffer sb = new StringBuffer();
	  String firma = order.getCompany();
      Calendar cal = Calendar.getInstance();
      cal.setTime(order.getShipCal().getTime());
 System.out.println("teen faili ");

      
      
	    
	      try {

   String separator  = ";";
	        int coun = 0;

	        Iterator iter = order.getOrderLines().iterator();
	        while (iter.hasNext()){
	          OrderLine line = (OrderLine)iter.next();
	          String toode  = line.getToode();
	 	     if(toode!= null){
	 	    	 toode = toode.trim();
	 	     }else{
	 	    	 toode = "";
	 	     }
	          if(line.getKogus()!= 0 && !toode.equals("")){
	        	 
	        	  sb.append( firma + separator
	                      + line.getToode() + separator
	                      + line.getKogus() );
	        	  sb.append(separator + "0");//doknr
	              sb.append(separator + " ");// dealer
	              sb.append(separator + "0");//tyhi
	              sb.append(separator + line.getDiscount());//ale
	              sb.append(separator + "" + cal.getTime().getTime()  );
	              sb.append(separator + "0");//tyhi
	              sb.append(separator + line.getPrice());//ale
	              sb.append(separator + "0"); //paymterm
	              sb.append(separator + order.getAgent());// agent
	              sb.append(separator + "0");//offer
	              sb.append(separator + line.getUnit());//unit
	              sb.append(separator + getDateNiceString(cal));
	              sb.append(separator + " "); //kommetaar
	              sb.append(separator + " "); //ladu
	              sb.append(separator + " ");//action kood
	              sb.append(separator   + " ");// action hind 

	              sb.append(System.getProperty("line.separator"));
	        	  coun++;
	          }
	          }

	        setNumAddedLines(coun); 
	     
	 	
	      String fileName = parent.getProps().getProperty("conv.dir") + order.getAgent() + "." + getDateX(Calendar.getInstance()) +  ".txt";
	   
	      System.out.println(fileName);
	        writeConsloStr(fileName,sb.toString());
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }

  
  private String getDateNiceString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    String year =   cal.get(Calendar.YEAR) + "";
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	     if (day.length() == 1){
	      sb.append("0");
	    }
	     sb.append(day);
	     sb.append(".");
	    if (month.length() == 1){
	     sb.append("0");
	   }
	     sb.append(month );
	    sb.append( "." + year);
	    return sb.toString();
	  }
	
  
	public void writeConsloStr(String path,String e) {
		
		try {
			
			File f = new File(path);
			if(!f.exists()){
				  try{
				      FileOutputStream fos = new FileOutputStream(path);
				        fos.write("".getBytes());
				        fos.close();
				
			} catch (Exception uu) {
				uu.printStackTrace();
			}
			}
			
			PrintWriter os = new PrintWriter(new FileWriter(
					path, true));
			os.write(e);
			os.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}

}

  private void addInvoiceLines(String salesid) throws SQLException{

    Enumeration enums = order.getOrderLines().elements();


    int counter = 0;
    while (enums.hasMoreElements()){
      counter++;
      log.info("ORDER_LINE_COUNTER:" + counter);
      OrderLine orderLine = (OrderLine)enums.nextElement();
      String itemid = orderLine.getToode();
      double kogus = orderLine.getKogus();

      String sql = "SELECT 0 AS price,inv.ITEMNAME FROM  inventtable inv " +
        " WHERE   inv.dataareaid='bal'  AND inv.itemid ='" + itemid + "'";
        log.info("SALES_LINE_SQL: " + sql);

        Statement st = con2.createStatement();
        ResultSet rs = st.executeQuery(sql);


        while(rs.next()){

          double salesprice = orderLine.getPrice();
          double percent = orderLine.getDiscount();
          String itemname = rs.getString("itemname").trim();
          String  salesgroup = order.getAgent();
          

          String mjsalesid = salesid;
          String agent = order.getAgent();
          
          String ladu = "KH";
          if(agent.equals("106")){
        	  ladu = "L5";
          }
          if(agent.equals("108")){
        	  ladu = "L6";
          }
          if(agent.equals("109")){
        	  ladu = "LA";
          }
          Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
         
          if(ag == null){
          	ag = new Agent(agent);
          }
          boolean isPilot = ag.isIspilot();
             if(!isPilot){
             	salesprice = 0;
             	percent = 0 ;
             }

          sql = "INSERT INTO mjsalesline(" +
            "SALESID," +
            "LINENUM," +
            "ITEMID," +
            "SALESSTATUS, NAME, EXTERNALITEMID, TAXGROUP, QTYORDERED," +
            "SALESDELIVERNOW, REMAINSALESPHYSICAL, REMAINSALESFINANCIAL," +
            "COSTPRICE, SALESPRICE, CURRENCYCODE, LINEPERCENT, LINEDISC," +
            "LINEAMOUNT, CONFIRMEDDLV, RESERVATION, SALESUNIT," +
            "DIMENSION, DIMENSION2_, DIMENSION3_, PRICEUNIT, INVENTTRANSID," +
            "CUSTGROUP, CUSTACCOUNT, SALESQTY, SALESMARKUP, INVENTDELIVERNOW," +
            "MULTILNDISC, MULTILNPERCENT, SALESTYPE, BLOCKED, COMPLETE," +
            "REMAININVENTPHYSICAL, TAXITEMGROUP, TAXAUTOGENERATED," +
            "UNDERDELIVERYPCT, OVERDELIVERYPCT, BARCODE, BARCODETYPE," +
            "INVENTREFTRANSID, INVENTREFTYPE, INVENTREFID, LINEHEADER, SCRAP," +
            "RETURNACTIONID, INVENTTRANSIDRETURN, INVENTDIMID," +
            "ESTIMATEGROSS, ESTIMATENET, CREATEDDATE, DATAAREAID, RECID, " +
            "CUSTOMERLINENUM,INTERCOMPANYINVENTTRANSID,ITEMGROUPNAME,ITEMGROUPID" +
            ",ITEMBOMID,BALPALLETSQTY,BALBOXESQTY,LEDGERACCOUNT,MJSALESID,INVENTLOCATIONID) VALUES('" +
              salesid.trim() + "'," +//
              counter + ",'" +//
              itemid + "'," +//
              1 + "," +//SALESSTATUS,
              "?," +//NAME,
              "'','" +//EXTERNALITEMID,
               "'," +//TAXGROUP,
              kogus + "," +// QTYORDERED
              0 + "," +//SALESDELIVERNOW,
              kogus + "," +//REMAINSALESPHYSICAL,
              0 + "," +// REMAINSALESFINANCIAL
              0.000 + "," +//COSTPRICE,
              salesprice + ",'" +// SALESPRICE,
               "LVL'," +//CURRENCYCODE,
              percent + "," +//LINEPERCENT,
              0 + "," +// LINEDISC,
              getPrice(kogus, percent, salesprice) + "," +//LINEAMOUNT,
              " GETDATE()," +// CONFIRMEDDLV,
              0 + ",'" +//RESERVATION,
           //    tootegrupp + "','" +// SALESGROUP,
              "','" +// SALESUNIT,
              salesgroup + "','" +//DIMENSION,
              "','" +//DIMENSION2_,
              "'," +//DIMENSION3_,
             "1.000000000000,'" + //PRICEUNIT,

                "','" + //INVENTTRANSID
                "','" +//CUSTGROUP

               order.getCompany()  + "'," +// CUSTACCOUNT
              kogus + "," +//SALESQTY
              "0,"+//SALESMARKUP
              0 + "," +//INVENTDELIVERNOW,
              0 + "," +//MULTILNDISC,
              0 + "," +//MULTILNPERCENT,
              3 + "," +// SALESTYPE,
               0 + "," +// BLOCKED, COMPLETE
              0 + "," +//COMPLETE
              kogus + ",'" +//REMAININVENTPHYSICAL,
                "20'," +//TAXITEMGROUP,
              1 + "," +//TAXAUTOGENERATED
              0 + "," +//UNDERDELIVERYPCT,
              0 + ",'" +//OVERDELIVERYPCT,
               "'," +// BARCODE,
              "''," +//rs.getString("barcodetype") + "'," +BARCODETYPE,
              "''," +//INVENTREFTRANSID,  ,
              0 + "," +//INVENTREFTYPE,
              "''," +//INVENTREFID
              "''," +// LINEHEADER,
              0 + "," +// SCRAP
              "''," +//RETURNACTIONID,
              "'','" +//INVENTTRANSIDRETURN,
               "'," +  // iteminvidINVENTDIMID
              // "'," +  // iteminvidINVENTDIMID
              0 + "," +//ESTIMATEGROSS,
              0 + "," +//ESTIMATENET,
              " GETDATE()," +//CREATEDDATE,
              "'bal'," +//DATAAREAID,
              getAndUpdateRecID()+/// RECID
              ",0," + //CUSTOMERLINENUM,
              "'','" + //INTERCOMPANYINVENTTRANSID
             "','" + //,ITEMGROUPNAME,
              "'," + //ITEMGROUPID
              "" + // DEL_CONFIGID,,
              "''," + //ITEMBOMID,
               "0,"  + //BALPALLETSQTY
                "0,"  +//BALBOXESQTY,L
              "''," //LEDGERACCOUNT
              + "'" +mjsalesid.trim() + "','" + ladu  + "')";//

              log.info("SALES_LINE_SQL: " + sql);
              System.out.println("grupp "  + itemname);

              PreparedStatement st2 = con6.prepareStatement(sql);
              st2.setString(1, itemname);

                st2.executeUpdate();

                addNumLine();

              setAmount(false);

              st2.close();


          }
    }

  }

  private void addInvoiceHeader(String salesid) throws SQLException{
    String sql = "SELECT INVENTLOCATION,NAME,ACCOUNTNUM,INVOICEACCOUNT,DIMENSION,ADDRESS,URL,PAYMTERMID,CASHDISC,TAXGROUP,LINEDISC,CUSTGROUP,DIMENSION2_,DIMENSION3_,PRICEGROUP," +
"MULTILINEDISC,ENDDISC,DLVTERM,DLVMODE,ZIPCODE,COUNTY,STATE,ONETIMECUSTOMER,EMAIL,FREIGHTZONE," +
"CURRENCY,BLOCKED,ACCOUNTSTATEMENT,CREDITMAX," +
"INCLTAX,NUMBERSEQUENCEGROUP,LANGUAGEID," +
"PRINTMODULETYPE,PAYMMODE,PAYMSPEC,DELIVERYROUTEID,VATNUM FROM CUSTTABLE WHERE LTRIM(ACCOUNTNUM)='" + order.getCompany() + "' AND DATAAREAID='bal'";

  log.info("addInvoiceHeader: " + sql);

    long recID = getAndUpdateRecID();
      Statement st9 = con1.createStatement();
      ResultSet rs = st9.executeQuery(sql);
      String mjsalesid = salesid ;
    

      while(rs.next()){
        String name = rs.getString("NAME");
        String accountnum = rs.getString("ACCOUNTNUM");
        String invoiceAccount = rs.getString("INVOICEACCOUNT").trim();
        String salesgroup = rs.getString("DIMENSION").trim();
        String address = rs.getString("ADDRESS");
        if (salesgroup.equals("")){
          //salesgroup = getSalesGroupByFirstName(order.getAgentFirstName());
          salesgroup = order.getAgent();
        }
         String agent = order.getAgent();
         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
         if(ag == null){
         	ag = new Agent(agent);
         }
         boolean isPilot = ag.isIspilot();
     
         System.out.println("agobj '"+ ag.getObject()+"'  orddealer'"+order.getDealer()+"'");

         

        log.info("INVOICEACCOUNT pikkus: " + invoiceAccount.length());
        if(invoiceAccount.length() == 0 ){
          invoiceAccount = accountnum;
        }
        

      sql = " INSERT INTO MJSALESTABLE (SALESID,SALESNAME,CUSTACCOUNT,INVOICEACCOUNT,DELIVERYDATE, " +
          " DELIVERYADDRESS,CREATEDDATE,DATAAREAID,RECID,SALESTABLETXT," +
          " BALPAYMID,MJSALESID,BALINPROCESS,SALESSTATUS,SALESTYPE,CURRENCYCODE,POSTINGPROFILE,LANGUAGEID)  " +
          " VALUES ('" + salesid.trim() +//SALESID,
        "',?" +//SALESNAME,
        ",'" + accountnum +//CUSTACCOUNT,
        "','" + invoiceAccount +//INVOICEACCOUNT
        "', ? " +//,DELIVERYDATE
        ",?" +//DELIVERYADDRESS
        ",GETDATE()" +//CREATEDDATE,
        ",'bal'" +//DATAAREAID
        "," + recID +//
        ", ? "+//SALESTABLETXT,
        ",'" + "" +//BALPAYMID,
        "','" + mjsalesid.trim() +//MJPAYMID,
        "',1,1,3,'LVL','TAVALINE','et')";
        log.info("addInvoiceHeader2: " + sql);
        PreparedStatement stmt2 = con2.prepareStatement(sql);
        stmt2.setString(1, name);
        java.util.Date dat = new java.util.Date();
        dat.setTime(order.getShipCal().getTimeInMillis());
        stmt2.setTimestamp(2, new java.sql.Timestamp(dat.getTime()));
        stmt2.setString(3, address);
        String info = order.getReference();
        System.out.println("info" + info);
        if (info == null){
        	info = "MobileJava";
        		}
        stmt2.setString(4, info);
        stmt2.executeUpdate();
        stmt2.close();
        addLog("lisatud arve päis -> " + order.getCompany() + "\n");
      }
      st9.close();


  }

  private long getAndUpdateRecID() throws SQLException{
    long nextRecID = 0;
    String sql = "SELECT NEXTVAL FROM SYSTEMSEQUENCES WHERE DATAAREAID='bal' AND NAME='SEQNO'";
    log.info("getAndUpdateRecID(): " + sql);
      Statement st = con4.createStatement();
      ResultSet rs = st.executeQuery(sql);
      while(rs.next()){
        counter++;
        log.info("COUNTER:" + counter);
        //System.out.println("number:" + rs.getString("NEXTREC"));
        nextRecID = Long.parseLong(rs.getString("NEXTVAL"));
        log.info("NEXTRECID = " + nextRecID);
        sql = "UPDATE SYSTEMSEQUENCES SET NEXTVAL=" + (nextRecID+1) +
        " WHERE DATAAREAID='bal' AND NAME='SEQNO'";
        log.info("getAndUpdateRecID(): " + sql);
        Statement st2 = con4.createStatement();
        int i = st2.executeUpdate(sql);
        st2.close();
        log.info("UPDATED ROWS = " + i);
      }
      rs.close();
      st.close();

    return nextRecID;
  }

  private String updateMyygNumberSequence() throws SQLException{
    return updateNumberSequence("m��g_32");
  }


  private String updateNumberSequence(String numberSequence) throws SQLException{
    String id = null;

      String sql = " SELECT NEXTREC, FORMAT FROM NUMBERSEQUENCETABLE WHERE NUMBERSEQUENCE='" + numberSequence + "' AND DATAAREAID='bal'";
      log.info("updateNumberSequence()2: " + sql);
      Statement st = con5.createStatement();
      ResultSet rs = st.executeQuery(sql);
      while(rs.next()){
        //System.out.println("number:" + rs.getString("NEXTREC"));
        id = mask(rs.getInt("NEXTREC"), rs.getString("FORMAT").trim());
      log.info("updateNumberSequence()3: " + id);
      }

      sql = "UPDATE NUMBERSEQUENCETABLE SET NEXTREC=( " +
      " SELECT NEXTREC FROM NUMBERSEQUENCETABLE WHERE NUMBERSEQUENCE='" + numberSequence + "' AND DATAAREAID='bal' " +
      " )+1 WHERE NUMBERSEQUENCE='" + numberSequence + "' AND DATAAREAID='bal' ";
      log.info("updateNumberSequence(): " + sql);
      int i = st.executeUpdate(sql);
      log.info("updateNumberSequence(): " + i + " rows updated");

      st.close();
    return id;
  }

  private String mask(int id, String recMask){
    String idStr = String.valueOf(id);
    int index = recMask.indexOf("#");
    int idlen = (""+id).length();
    String idLen = "";
    if (idlen == 4){
    idLen = "00";
    }
    if (idlen == 3){
idLen = "000";
}
    if (idlen == 2){
    idLen = "0000";
    }
    if (idlen == 5){
    idLen = "0";
    }

    if (index != -1){
      String prefix = recMask.substring(0, index);
      int suffixLength = recMask.length() - index;
      StringBuffer result = new StringBuffer();
      result.append(prefix);
      for (int i = 0; i < (suffixLength-idStr.length()); i++){
        result.append("0");
      }

      result.append(idStr);
      if (recMask.trim().startsWith("#######_055")){
       return "         "+ idLen + id + "_055"; // m��g_1
      } else {
         return "            " + result.toString(); // m��g_1
      }

    } else {
      return "             " + id ; // varu_6
    }
  }

  public static double getPrice(double kogus, double percent, double price){
    double value = kogus * (price-((price/100)*percent));
    value = (Math.round((value*100d)))/100d;
    return value;
  }

  public String getDriverVersion(){
    return "IceAxa";
  }

  private void setOrder(Order order){
    this.order = order;
  }

  private void addLog(String line){
    parent.addLog(line);
  }
  public boolean isAmount(){
          return isAmount;
  }

  /**
  *
  * @param aUseEAN - the new value for useEAN
  */
  public void setAmount(boolean aAmount){
           isAmount= aAmount;
  }

  private String url = "jdbc:odbc:";
  private Connection con;
   private Connection con1;
  private Connection con2;
  private Connection con4;
  private Connection con5;
  private Connection con6;
  private ImportPanel parent;
  private Order order = null;
  private boolean isAmount = false;
  private int counter = 0;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}