package eevaeksport.imp.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeMap;
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

public class IceAxaImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public IceAxaImportDriver(ImportPanel parent) {
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
      //stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }

  public String identifyCustomer(String id,String regnr){
  	 String sql1 = "";
  	 if(regnr != null && regnr.trim().length()>0){
  	 	sql1 = "SELECT accountnum FROM custtable where ouraccountnum='" + id +   "' and regnum_w ='" + regnr + "' and dataareaid='bal'";
  	     	
  	 }else{
  	 	sql1 = "SELECT accountnum FROM custtable where ouraccountnum='" + id +   "'  and dataareaid='bal'";
  	    
  	 }
  	String sql3 = "SELECT accountnum FROM custtable where (ltrim(accountnum)= '" + id + "' or ouraccountnum='" + id + "') and dataareaid='bal'";
	String sql4 = "SELECT accountnum FROM custtable where accountnum= '" + id + "' and dataareaid='bal'";
	  
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

 
 if(acc == null){
	
 Statement st4 = con.createStatement();
 ResultSet rs4 = st4.executeQuery(sql4);

  while(rs4.next()){
    acc = rs4.getString("accountnum").trim();
 }
  System.out.println("3" +acc);
  System.out.println(sql4);
  
rs4.close();
st4.close();


 }

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
    
System.out.println("toote tuvastus " + prod + " " + prodID + " " + ean);
    return prod;
  }

  public void makeOrder(Order order) throws SQLException{
    //if (true) return;
    setOrder(order);


    String salesid = updateMyygNumberSequence();
   String salesidpilot = "";
    if(false){
      salesidpilot = "         " + order.getAgent() + "-" + order.getDokNr();
      if(order.getDealer()!= null && order.getDealer().equals("KH") )
        salesidpilot = "";
    }

    if (salesidpilot.length() > 0) {
     salesid = salesidpilot;
    }


   if(order.getAgent().equals("106") || order.getAgent().equals("107") || order.getAgent().equals("108")  || order.getAgent().equals("109")){
	   setOrder(consolidate(order));
	   
	   Vector lines = consolidate(order).getOrderLines();
		  Enumeration enums = lines.elements();
	
		  while  (enums.hasMoreElements()){
			  OrderLine lin = (OrderLine)enums.nextElement(); 
			  System.out.println(order.getAgent() + " ' " + order.getCompany() + " ' " + lin.getToode() + " ' '" +
					  lin.getDiscount() + " ' " + lin.getKogus());
		  }
			
   }
   
    // System.exit(0);
   if(salesid == null ){
	   salesid = "12" + (Calendar.getInstance().getTimeInMillis()+"").substring(7);
   }
    addInvoiceHeader(salesid);
    addInvoiceLines(salesid);
    String sql4 = "update mjsalestable set balinprocess=0 where ltrim(salesid) = ?";

    PreparedStatement st2 = con4.prepareStatement(sql4);
    st2.setString(1, salesid.trim());
    st2.executeUpdate();
    st2.close();

  }

 private Order consolidate(Order order){
     
	  Order ord23 = new Order(order.getCompany(),order.getAgentFirstName());
	     
	  ord23.setAgentCode(order.getAgentCode());
	  ord23.setDokNr(order.getDokNr());
	  ord23.setInfo(order.getInfo());
	  ord23.setTeostaja((order.getTeostaja()));
	  ord23.setChanged(order.isChanged());
	  ord23.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord23.setStatus(order.getStatus());
	  ord23.setShipTime(order.getShipTime());
	  ord23.setWarehouse(order.getWarehouse());
	  

	  Vector lines = order.getOrderLines();
	  Enumeration enums = lines.elements();
	  TreeMap norm = new TreeMap();
	  TreeMap aled = new TreeMap();
	  while  (enums.hasMoreElements()){
		  OrderLine lin = (OrderLine)enums.nextElement();
		     String itemid = lin.getToode();
	         double kogus  = lin.getKogus();
	         double disc   = lin.getDiscount();
	         String aaa = itemid+ "|" +disc;
	         if(disc > 0){
	        	 if(aled.get(aaa)!= null){
	        		 double kog = Double.parseDouble((String)aled.get(aaa)) + kogus;
	        		 aled.put(aaa,kog + "");
	        	 }else{
	        		 aled.put(aaa,kogus + ""); 
	        	 }	 
	         }else{
	        	 if(norm.get(itemid)!= null){
	        		 double kog = Double.parseDouble((String)norm.get(itemid)) + kogus;
	        		 norm.put(itemid,kog + "");
	        	 }else{
	        		 norm.put(itemid,kogus + ""); 
	        	 }
	         }
    
	         
	  }
	  
	  if(norm.size() > 0 ){
		Iterator itera =  norm.keySet().iterator();
		while(itera.hasNext()){
			String prodid = (String)itera.next();
			double kogus = Double.parseDouble((String)norm.get(prodid));
			
		ord23.add(prodid, kogus, 0.0, 0.0, "0", 0, "nimi", "tk", null, null,null,null,null);
		}
	  }
	  
	  if(aled.size() > 0){
			Iterator itera =  aled.keySet().iterator();
			while(itera.hasNext()){
				String asi = (String)itera.next();
				int ind = asi.indexOf("|");
				String prodid = asi.substring(0,ind);
				double ale = Double.parseDouble(asi.substring(ind+1));
				double kogus = Double.parseDouble((String)aled.get(asi));
				ord23.add(prodid, kogus, 0.0, ale, "0", 0, "nimi", "tk", null, null,null,null,null);
			} 
	  }
		
	  
	 return ord23;
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
      double kordaja = orderLine.getBonus();
   

      String sql = "SELECT 0 AS price,inv.ITEMNAME as name,inv.itemqtyinabox as kogusk FROM  inventtable inv " +
        " WHERE   inv.dataareaid='bal'  AND inv.itemid ='" + itemid + "'";
        log.info("SALES_LINE_SQL: " + sql);

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);


        while(rs.next()){

          double salesprice = rs.getDouble("price");
          double percent = 0;
          if(orderLine.getDiscount()>1 && order.getStatus() < 10){
        	  percent = orderLine.getDiscount();
          }
          String ladu = "";
          if(orderLine.getDiscount() == 1  &&  order.getStatus() < 10){
        	  ladu = "KS";
          }
          
        String agent = order.getAgent();
          if(agent.equals("106")){
        	  ladu = "L5";
          }
          if(agent.equals("108")){
        	  ladu = "L6";
          }
          if(agent.equals("109")){
        	  ladu = "LA";
          }
         
          
          String itemname = rs.getString("name").trim();
           kordaja = rs.getDouble("kogusk");
           
          if(kordaja > 0 && orderLine.getUnit() != null && orderLine.getUnit().equals("DPA") ){
        	  kogus = kordaja * kogus;
          }
 
          String  salesgroup = order.getAgent();
          
          boolean pakend = false ;
          boolean pakend2 = false ;
 if(itemid.equals("OQQ300") ){
	 // OQM001, OQM002, OQM003, OQM004, OQM005, OQM006, OQM007 ja OQM008
	 pakend = true;
 }
 if(itemid.equals("OQM001") || itemid.equals("OQM002") 
		 || itemid.equals("OQM003") || itemid.equals("OQM004")
		 || itemid.equals("OQM005") || itemid.equals("OQM006")
		 || itemid.equals("OQM007") || itemid.equals("OQM008")){
	 
	 pakend = true;
	 pakend2 = true;
 }
          
          String mjsalesid = salesid;
        
          Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
         
          if(ag == null){
          	ag = new Agent(agent);
          }
          boolean isPilot = ag.isIspilot();

           boolean pallet = false;

          if (!isPilot) {
          //  pallet = false;
          }
          else if (order.getDealer() != null && ("KH").equals(order.getDealer().trim())) {
            pallet = false;
          }
          else {
            pallet = false;
          }
          
          
          if (pakend) {
         String toode = "WTT017";
         if(pakend2){
        	 toode = "WTT018"; 
         }
        	  
        	int cou =  order.getOrderLines().size() + 1;
   
              sql = "  INSERT INTO mjsalesline(SALESID,LINENUM,ITEMID,SALESSTATUS, NAME, EXTERNALITEMID, TAXGROUP, QTYORDERED,SALESDELIVERNOW, " +
                  " REMAINSALESPHYSICAL, REMAINSALESFINANCIAL,COSTPRICE, SALESPRICE, CURRENCYCODE, LINEPERCENT, LINEDISC,LINEAMOUNT, CONFIRMEDDLV, " +
                  " RESERVATION, SALESUNIT,DIMENSION, DIMENSION2_, DIMENSION3_, PRICEUNIT, INVENTTRANSID,CUSTGROUP, CUSTACCOUNT, SALESQTY, " +
                  " SALESMARKUP, INVENTDELIVERNOW,MULTILNDISC, MULTILNPERCENT, SALESTYPE, BLOCKED, COMPLETE,REMAININVENTPHYSICAL, TAXITEMGROUP, TAXAUTOGENERATED," +
                  " UNDERDELIVERYPCT, OVERDELIVERYPCT, BARCODE, BARCODETYPE,INVENTREFTRANSID, INVENTREFTYPE, INVENTREFID, LINEHEADER, SCRAP,RETURNACTIONID, " +
                  " INVENTTRANSIDRETURN, INVENTDIMID,ESTIMATEGROSS, ESTIMATENET, CREATEDDATE, DATAAREAID, RECID, CUSTOMERLINENUM,INTERCOMPANYINVENTTRANSID," +
                  " ITEMGROUPNAME,ITEMGROUPID,ITEMBOMID,BALPALLETSQTY,BALBOXESQTY,LEDGERACCOUNT,MJSALESID) " +
                  " VALUES('" + mjsalesid.trim() + "'," + cou +
                  ",'" + toode + "',1,'Plastist �hekorrapakend','','E-kl',1.0,0,1.0,0,0.0,0,'EUR',0,0,0," +
                  "  GETDATE(),0,'','','','820751','1.000000000000','','100','" +
                  order.getCompany()  + "'," + kogus + ",'.000000000000'," +
                  " 0,0,0,3,'0',0,1.0,'20',1,'100.000000000000','100.000000000000','','','',0,'','',0,'','',''" +
                  " ,0,0, GETDATE(),'bal'," + getAndUpdateRecID() + ",0,'','Plastist �hekorrapakend','" +
                   "','',0,0,'','" + mjsalesid.trim() + "')";

           //   log.info("SALES_LINE_SQL: " + sql);

              System.out.println(" lisan pandi " + kogus );
              PreparedStatement st4 = con5.prepareStatement(sql);
              st4.executeUpdate();
              st4.close();

            }
          
          
          
          if (pallet && counter == 1) {


            sql = "  INSERT INTO mjsalesline(SALESID,LINENUM,ITEMID,SALESSTATUS, NAME, EXTERNALITEMID, TAXGROUP, QTYORDERED,SALESDELIVERNOW, " +
                " REMAINSALESPHYSICAL, REMAINSALESFINANCIAL,COSTPRICE, SALESPRICE, CURRENCYCODE, LINEPERCENT, LINEDISC,LINEAMOUNT, CONFIRMEDDLV, " +
                " RESERVATION, SALESUNIT,DIMENSION, DIMENSION2_, DIMENSION3_, PRICEUNIT, INVENTTRANSID,CUSTGROUP, CUSTACCOUNT, SALESQTY, " +
                " SALESMARKUP, INVENTDELIVERNOW,MULTILNDISC, MULTILNPERCENT, SALESTYPE, BLOCKED, COMPLETE,REMAININVENTPHYSICAL, TAXITEMGROUP, TAXAUTOGENERATED," +
                " UNDERDELIVERYPCT, OVERDELIVERYPCT, BARCODE, BARCODETYPE,INVENTREFTRANSID, INVENTREFTYPE, INVENTREFID, LINEHEADER, SCRAP,RETURNACTIONID, " +
                " INVENTTRANSIDRETURN, INVENTDIMID,ESTIMATEGROSS, ESTIMATENET, CREATEDDATE, DATAAREAID, RECID, CUSTOMERLINENUM,INTERCOMPANYINVENTTRANSID," +
                " ITEMGROUPNAME,ITEMGROUPID,ITEMBOMID,BALPALLETSQTY,BALBOXESQTY,LEDGERACCOUNT,MJSALESID) " +
                " VALUES('" + mjsalesid.trim() + "'," + counter +
                ",'XAA001',1,'Kaubaalus','','E-kl',1.0,0,1.0,0,0.0,0,'EUR',0,0,0," +
                "  GETDATE(),0,'','','','820751','1.000000000000','','100','" +
                order.getCompany()  + "',1.0,'.000000000000'," +
                " 0,0,0,3,'0',0,1.0,'20',1,'100.000000000000','100.000000000000','','','',0,'','',0,'','',''" +
                " ,0,0, GETDATE(),'bal'," + getAndUpdateRecID() + ",0,'','Kaubaalus','" +
                 "','',0,0,'','" + mjsalesid.trim() + "')";

            counter++;
         //   log.info("SALES_LINE_SQL: " + sql);

            PreparedStatement st4 = con5.prepareStatement(sql);
            st4.executeUpdate();
            st4.close();

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
               "EUR'," +//CURRENCYCODE,
              percent + "," +//LINEPERCENT,
              0 + "," +// LINEDISC,
              getPrice(kogus, percent, salesprice) + "," +//LINEAMOUNT,
              " GETDATE()," +// CONFIRMEDDLV,
              0 + ",'" +//RESERVATION,
           //    tootegrupp + "','" +// SALESGROUP,
              "','" +// SALESUNIT,
               "','" +//DIMENSION,
               salesgroup + "','" +//DIMENSION2_,
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
              + "'" +mjsalesid.trim() + "','" + ladu +
              "')";//

              log.info("SALES_LINE_SQL: " + sql);
              System.out.println("grupp "  + itemname);

              PreparedStatement st2 = con2.prepareStatement(sql);
              st2.setString(1, itemname);
              st2.executeUpdate();
              addNumLine();
              setAmount(false);
              st2.close();
          }
    }

  }

  private void addInvoiceHeader(String salesid) throws SQLException{
    String sql = "SELECT INVENTLOCATION,NAME,ACCOUNTNUM,INVOICEACCOUNT,DIMENSION2_,ADDRESS,URL,PAYMTERMID,CASHDISC,TAXGROUP,LINEDISC,CUSTGROUP,DIMENSION,DIMENSION3_,PRICEGROUP," +
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
        String salesgroup = rs.getString("DIMENSION2_").trim();
        String address = rs.getString("ADDRESS");
        if (salesgroup.equals("")){
          salesgroup = order.getAgent();
        }
         String agent = order.getAgent();
         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
         if(ag == null){
         	ag = new Agent(agent);
         }
       
         
      String profile = "TAVALINE";
      if(order.getStatus() > 2) {
    	  profile = "EDI";
      }
        log.info("INVOICEACCOUNT pikkus: " + invoiceAccount.length());
        if(invoiceAccount.length() == 0 ){
          invoiceAccount = accountnum;
        }
        

      sql = "INSERT INTO MJSALESTABLE (SALESID,SALESNAME,CUSTACCOUNT,INVOICEACCOUNT,DELIVERYDATE, " +
          "DELIVERYADDRESS,CREATEDDATE,DATAAREAID,RECID,SALESTABLETXT," +
          "BALPAYMID,MJSALESID,BALINPROCESS,SALESSTATUS,SALESTYPE,CURRENCYCODE,POSTINGPROFILE,LANGUAGEID,CUSTOMERREF)  " +
"VALUES ('" + salesid.trim() +//SALESID,
        "',?" +//SALESNAME,
        ",'" + accountnum +//CUSTACCOUNT,
        "','" + invoiceAccount +//INVOICEACCOUNT
        "', ? " +//,DELIVERYDATE
        ",?" +//DELIVERYADDRESS
        ",GETDATE()" +//CREATEDDATE,
        ",'bal'" +//DATAAREAID
        "," + recID +//
        ", ? "+//SALESTABLETXT,
        ",'" +// + balid +//BALPAYMID,
        "','" + mjsalesid.trim() +//MJPAYMID,
        "',1,1,3,'EUR',?,'et',?)";
        log.info("addInvoiceHeader2: " + sql);
        PreparedStatement stmt2 = con2.prepareStatement(sql);
        stmt2.setString(1, name);
   
     long mills = order.getShipCal().getTimeInMillis();
         
          
          System.out.println(order.getShipCal().getTime().toGMTString());
        stmt2.setTimestamp(2, new java.sql.Timestamp(mills));
        stmt2.setString(3, address);
        String info = order.getReference();
        System.out.println("info" + info);
        if (info == null){
        	info = "MobileJava";
        		}
        
        String comment = "";
        if(order.getInfo()!= null && order.getInfo().trim().length()>1){
        	comment = order.getInfo();
        	if(comment.length()>254){
        		comment = comment.substring(0,254);
        	}
        }
        	
        stmt2.setString(4, comment.replaceAll("\n", " "));
        stmt2.setString(5, profile);
        stmt2.setString(6, info);
      
        
        stmt2.executeUpdate();
        stmt2.close();
        addLog("lisatud arve päis -> " + order.getCompany() + "\n");
      }
      st9.close();


  }

  private long getAndUpdateRecID() throws SQLException{
    long nextRecID = 0;
    String sql = "SELECT NEXTVAL FROM SYSTEMSEQUENCES WHERE DATAAREAID='bal' AND NAME='SEQNO'";
    //log.info("getAndUpdateRecID(): " + sql);
      Statement st = con4.createStatement();
      ResultSet rs = st.executeQuery(sql);
      while(rs.next()){
        counter++;
       // log.info("COUNTER:" + counter);
        //System.out.println("number:" + rs.getString("NEXTREC"));
        nextRecID = Long.parseLong(rs.getString("NEXTVAL"));
        //log.info("NEXTRECID = " + nextRecID);
        sql = "UPDATE SYSTEMSEQUENCES SET NEXTVAL=" + (nextRecID+1) +
        " WHERE DATAAREAID='bal' AND NAME='SEQNO'";
       // log.info("getAndUpdateRecID(): " + sql);
        Statement st2 = con4.createStatement();
        st2.executeUpdate(sql);
        st2.close();
        //log.info("UPDATED ROWS = " + i);
      }
      rs.close();
      st.close();

    return nextRecID;
  }

  private String updateMyygNumberSequence() throws SQLException{
    return updateNumberSequence("müüg_45");
  }


  private String updateNumberSequence(String numberSequence) throws SQLException{
    String id = null;

      String sql = " SELECT NEXTREC, FORMAT FROM NUMBERSEQUENCETABLE WHERE NUMBERSEQUENCE='" + numberSequence + "' AND DATAAREAID='bal'";
    //  log.info("updateNumberSequence()2: " + sql);
      Statement st = con5.createStatement();
      ResultSet rs = st.executeQuery(sql);
      while(rs.next()){
        //System.out.println("number:" + rs.getString("NEXTREC"));
        id = mask(rs.getInt("NEXTREC"), rs.getString("FORMAT").trim());
    //  log.info("updateNumberSequence()3: " + id);
      }

      sql = "UPDATE NUMBERSEQUENCETABLE SET NEXTREC=( " +
      " SELECT NEXTREC FROM NUMBERSEQUENCETABLE WHERE NUMBERSEQUENCE='" + numberSequence + "' AND DATAAREAID='bal' " +
      " )+1 WHERE NUMBERSEQUENCE='" + numberSequence + "' AND DATAAREAID='bal' ";
     // log.info("updateNumberSequence(): " + sql);
     st.executeUpdate(sql);
     // log.info("updateNumberSequence(): " + i + " rows updated");

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
  private ImportPanel parent;
  private Order order = null;
  private boolean isAmount = false;
  private int counter = 0;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}