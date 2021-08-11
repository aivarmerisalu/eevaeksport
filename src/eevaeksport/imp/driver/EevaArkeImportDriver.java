package eevaeksport.imp.driver;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
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
import java.util.Locale;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

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

public class EevaArkeImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public EevaArkeImportDriver(ImportPanel parent) {
   
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    this.parent = parent;
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url );
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	if(regnr==  null){
  	    sql1 = "SELECT kood FROM firma1.klient where   fil= '" + id + "'";	
  	}else{

  	     sql1 = "SELECT kood FROM firma1.klient where  (regnr)= '" + regnr + "' AND fil= '" + id + "'";	
  	}
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim();

    }
    
    if(acc != null){
    	return acc ;
    }
    sql1 = "SELECT kood FROM firma1.klient where   kood= '" + id + "'";	
    rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim();
    }
    
    
    
    rs1.close();
    st1.close();

   // System.out.println("Klient: " + acc);
    return acc;

  }

  public String identifyProduct(String ean, String prodID) throws SQLException{
    String sql1 = "SELECT toode FROM firma1.toode where ribakood='" + ean + "'";


    String sql2 = "SELECT toode FROM firma1.toode where toode='" + prodID + "'";

    String prod = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
//System.out.println("prod0" + prod);
    if(prod == null){

      rs1 = st1.executeQuery(sql2);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
      }
    }

    //System.out.println("prod1" + prod);
    rs1.close();
    st1.close();

    return prod;
  }

  
  public String getProductName(String prodID) throws SQLException{


	    String sql2 = "SELECT nimetus FROM firma1.toode where toode='" + prodID + "'";

	    String prod = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql2);

	    while (rs1.next()) {
	      prod = rs1.getString("nimetus").trim();
	    }

	    rs1.close();
	    st1.close();

	    return prod;
	  }


  public void makeOrder(Order order) throws SQLException{
    setOrder(order);
    counter = 0;
  //  int invoiceID = getNextID();
    //addInvoiceHeader(invoiceID);
    //addInvoiceLines(invoiceID);
    
    this.makeOutput(order);
    setNumAddedLines(counter);
  }

  private int getNextID() throws SQLException{
	  
	    String sql =  " select nextval('firma1.dok_dokumnr_seq') as seeralgus";

	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);
	    int intNr = -1;
	    while(rs.next()){
	      intNr = rs.getInt("seeralgus");
	    }
	    return intNr;
}

  
  
  



  private void addInvoiceHeader(int invID) throws SQLException{
	  String ladu = "1";

	  
	    
	  String agent = order.getAgent();
	  String ordernumber = null;
	    if(order.getStatus() > 10){
	    	ordernumber = order.getDokNr();
	    }
	  
	  String sql = "INSERT INTO firma1.dok( doktyyp, dokumnr, kuupaev, oper, klient, laonr, raha, tasudok, " +
           " knr, tasukuup, yksus, sihtyksus, pais2obj, saaja, krdokumnr, " +
           " eimuuda, kasutaja, username, kellaaeg, arvekonto, maksetin, exchrate, " +
          " ratefound, tekst1, viitenr, objrealt, arvenumber, pais3obj, pais4obj, " +
           " pais5obj, pais6obj, pais7obj, pais8obj, pais9obj, masin, tegmasin, " +
           " guid, doksumma, kinnitatud, tasumata, sularaha, kaardimaks, kalkliik, " +
           " kalktoode, inventuur, algus, lopp, taidetud, kaal, \"timestamp\", " +
           " vmnr, tellimus, volitaisik, postitatud, liikmesrii, tehingulii, " +
           " tarneklaus, statprots, deliverypa, kurss " +
          
           " ) VALUES (" +
           
           " 'T', ?, now(), 'LT1', ?, 1, 'EUR', '" + invID + "', " +
           " '1311', now(), '"+ladu+"', null, ?, ?, null, " +
           " false, 'EETASOFT','ADMIN', '23 59', '112', null, 1 ," +
           " now(), ?, '', false, null, null, null, " +
           " null, null, null, null, null, 'mobil', null, " +
           " '" + Calendar.getInstance().getTimeInMillis() + "', 0, false, 0, 0, 0, '', " +
           " null, false, now(), ?, false, 0, '20151027111600', " +
           " null, '" + ordernumber + "', null, now(), null, null, " +
           " null, null, null, 0 " +
           " )" ;
	  

   
	  System.out.println("oo "+sql);
    log.info("addInvoiceHeader2: " + sql);
    if(agent.equals("KATTLIN")) {
    	agent = "KÄTTLIN";
    }
    PreparedStatement stmt = con.prepareStatement(sql);
    stmt.setInt(1, invID);
    stmt.setString(2, order.getCompany());
    stmt.setString(3, agent);
    stmt.setString(4, order.getCompany());
    String info = order.getInfo();
    if(info == null){
    	info = " ";
    }
    stmt.setString(5, info);
    stmt.setTimestamp(6,new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
   // stmt.setString(7, ordernumber);

    stmt.executeUpdate();
    stmt.close();
  }


  
  
  
  

  
  private boolean addInvoiceLines(int invID) throws SQLException{
	    
	     boolean result = false;
	  StringBuffer sb = new StringBuffer(); 
	        
	    Enumeration enumr = order.getOrderLines().elements();
	    int count = 1;
	    while(enumr.hasMoreElements()){
	      OrderLine line = (OrderLine)enumr.nextElement();
	      
	      
	      String toode = line.getToode();
	      if (toode == null){
	      toode = " ";
	    }
	      double kastis = 1;
	      if(line.getUnit().equals("DPA")){
	    	  kastis = line.getBonus();
	      }
	      
	      if(order.getStatus() == 1){
	    	kastis = kastis * -1;  
	      }
	      
	     
	      
	      String sql = "INSERT INTO firma1.rid( " +
	              "id, reanr, dokumnr, nimetus, hind, kogus, toode, partii, myygikood, " +
	              "hinnak, kaubasumma, yhik, kulukonto, kuluobjekt, rid2obj, reakuupaev, " +
	              "kogpak, kulum, baasostu, ostuale, rid3obj, rid4obj, rid5obj, " +
	              "rid6obj, rid7obj, rid8obj, rid9obj, kaskogus, aktsiis, kulutoode, " +
	              "kulupartii, inpdoktyyp, inpdokumnr, rtaitkogus, fifoexpens, calculrow, " +
	              "laosumma, laoraha, variant, taitmata, iseteha, rtellimus, reakaal, " +
	              "paritoluri, tellreanr, statvaartu, statiskood) " +
	     " VALUES (nextval('firma1.rid_id_seq'),nextval('firma1.rid_reanr_seq'), ?,'" 
	              + line.getTooteNimi().replaceAll("'", " ") + "' , 0, " + line.getKogus()*kastis 
	     + ", '" + toode + "', null, null, " +
	       "       null, 0, null, null, null, null, now(), " +
	        "      0, 0, 0, " + line.getDiscount() + ", null, null, null, " +
	         "     null, null, null, null, 0, 0, null, " +
	         "     null, null, null, 0, false, false, " +
	          "    0, null, false, 0, false, null, " + line.getKogus()*kastis + ", " +
	           "   null, " + count+ ", null,   null)";
	      
	    
	    System.out.println(invID + " " + sql);

	      PreparedStatement st = con.prepareStatement(sql);
	      st.setInt(1, invID);
	      st.executeUpdate();
	      count++;
          st.close(); 
	      setNumAddedLines(getNumAddedLines() + 1);
	    }

return result;
}

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

  private void makeOutput(Order order){
  	StringBuffer sb = new StringBuffer();
  	 nf.setGroupingUsed(false);
  	 nf.setMinimumFractionDigits(4);

  	try {
  	  int cou = 0;
 		String ref = order.getReference();
 		if ( ref == null ){
 			ref =  " ";
 		}
 		String com = order.getEmailBody();
 		if ( com == null ){
 			com =  " ";
 		}		
 		String ware = order.getWarehouse();
 		System.out.println("ladu tellimusel " + ware);
 		if ( ware == null ){
 			ware = "10";
 		}
 		if(!ware.equals("92")){
 			ware = "10";
 		}
 		 String doknr = order.getDokNr();
          if(doknr.length()>6){
          	// doknr = doknr.substring(doknr.length()-6,doknr.length());
          }
                  

          String ls = System.getProperty("line.separator");
          String fixagent = order.getAgent(); 
          if(fixagent.equals("KATTLIN")){
            // fixagent = "KÄTTLIN";
          }
      
          String custID = order.getCompany();
          String ladu = null;
              Calendar  shipDate = order.getShipCal();
              
              String code = order.getWarehouse();        
              if (code != null){
                ladu = code;
              }
            
            if (ladu == null) {
              ladu = "";
            }
   
       

        
            Calendar cal = Calendar.getInstance();
           
            if (shipDate != null) {
                  cal = shipDate;
                }
          
            sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + ls);
            sb.append("<E-Document>" + ls);
       
            sb.append("<Header>" + ls);
            sb.append("<DateIssued>" + escape(getDateString(Calendar.getInstance())) + "</DateIssued>" + ls);
            sb.append("</Header>" + ls);
            sb.append("<Document>" + ls);
            sb.append("<DocumentType>order</DocumentType> " + ls);
            sb.append("<DocumentFunction>ORIGINAL</DocumentFunction> " + ls);
            sb.append("<DocumentParties>" + ls);
            sb.append("<DeliveryParty>" + ls);
            sb.append("<PartyCode>" +  escape(custID) + "</PartyCode>" + ls); 
            sb.append("</DeliveryParty>" + ls);
            sb.append("<OrderParty>" + ls);
            sb.append( "<PartyCode>" +  escape(custID) + "</PartyCode>" + ls);
            sb.append("</OrderParty>" + ls);
            sb.append("</DocumentParties>" + ls);
          
            
            sb.append("<DocumentInfo>" + ls);
             
       String sss = (Calendar.getInstance().getTime().getTime()+ "").substring(4) ;
            sb.append("<DocumentName>tellimus</DocumentName>" + ls); 
            sb.append("<DocumentNum>" + escape(fixagent + "-" + sss )+ "</DocumentNum>" + ls); 
            sb.append("<DateInfo>" + ls);
            sb.append("<OrderDate>" +escape(getDateString(Calendar.getInstance())) + "</OrderDate>" + ls);
            sb.append("<DeliveryDateRequested>" +  escape(getDateString(cal)) + "</DeliveryDateRequested>" + ls); 
            sb.append("</DateInfo>" + ls);
            sb.append("</DocumentInfo>" + ls);
       	  
            sb.append("   <DocumentItem>" + ls);
            Vector sortedLines = order.getOrderLines();
            for (int i = 0; i < sortedLines.size(); i++) {
                OrderLine line = (OrderLine)sortedLines.elementAt(i);
                double ale = line.getDiscount();     
                counter++;
                String ean = line.getEAN();
                String yhik = line.getUnit();
                if (ale == -1 ){
                    ale = 0 ;
                    }
                sb.append("      <ItemEntry>" + ls);
                sb.append("         <LineItemNum>"  + (i+1) + "</LineItemNum>" + ls);
                sb.append("         <SellerItemCode>" + line.getToode() + "</SellerItemCode>" + ls);
              	sb.append("         <ItemUnitRecord>" + ls);
                sb.append("                <ItemUnit>" + line.getUnit()+ "</ItemUnit>" + ls);
                sb.append("         </ItemUnitRecord>" + ls);
                sb.append("         <BaseUnit>" + line.getUnit()+ "</BaseUnit>" + ls);
                sb.append("         <AmountOrdered>" + escape(getNF().format(line.getKogus())) + "</AmountOrdered>" + ls);
                 
                if(line.getDiscount() > 0){
              	  double discprice = (100d -line.getDiscount())*line.getPrice()/100d ;
                sb.append("         <ItemPrice>" + escape(getNF().format(discprice)) + "</ItemPrice>" + ls); 
                }
               sb.append("          </ItemEntry>" + ls);
            }
      
            sb.append("   </DocumentItem>" + ls);
            
      	    String comm = order.getInfo();
              if(comm != null && comm.length()>100){
              	comm = comm.substring(0,100);
              }
              
              if(comm != null && comm.trim().length() > 0 ){
              	  sb.append("   <AdditionalInfo>" +ls);
                    sb.append("       <Extension extensionId=\"remarks\">" + ls);
                    sb.append("       <InfoName>Remark1</InfoName>");
                    sb.append("            <InfoContent>" + escape(comm) + "</InfoContent>" + ls );
                    sb.append("       </Extension>");
                      //-<Extension extensionId="externalremarks"><InfoContent>K�esoleva arve aluseks olev n�ue on loovutatud AS-ile SEB Liising ja k�ik arvel toodud summad tuleb maksta AS-i SEB Liising arvelduskontole 10052045861006 SEB Pangas (kood 401). Kogu k�esoleva arvega seotud info tuleb teatavaks teha AS-ile SEB Liisingule.</InfoContent></Extension>
                    sb.append("   </AdditionalInfo>" + ls);	
              }
            
              
            sb.append("</Document>" + ls);
            sb.append("</E-Document>" + ls);
  
       counter = counter + cou;
     	//
       String fn = doknr;
       if(doknr.trim().length()== 0){
     	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
       }
       String fileName = escape(parent.getProps().getProperty("conv.dir") + fixagent)  + "." + sss  +  ".xml";
       System.out.println(fileName);
       Writer out = new BufferedWriter(new OutputStreamWriter(
 			    new FileOutputStream(fileName), "UTF-8"));
 			try {
 			    out.write(sb.toString());
 			} finally {
 			    out.close();
 			}

 	} catch (Exception e) {
 		e.printStackTrace();
 	}
  }
  

  private int counter = 0;
  private String escape(String s){
	    if (s == null) return s;
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0 ; i < s.length() ; i++){
	      char c = s.charAt(i);
	      if (c == '&'){
	        sb.append("&amp;");
	      } else if (c == '<'){
	        sb.append("&lt;");
	      } else if (c == '>'){
	        sb.append("&gt;");
	      } else if (c == '\n'){
	        sb.append(" ");
	      } else {
	        sb.append(c);
	      }
	    }
	    return sb.toString();
	  }
  
  
  
 	public NumberFormat getNF() {
 		return this.nf;
 	}
 	
 	
  private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    sb.append(cal.get(Calendar.YEAR) + "-");
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month + "-");

	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day);

	    return sb.toString();
	  }

  public String getDriverVersion(){
    return "EevaArke";
  }

  private void setOrder(Order order){
    this.order = order;
  }

  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
	  
  private ImportPanel parent;

  private String url = "jdbc:odbc:PostgreSQL30";
  private Connection con;
  private Order order = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
