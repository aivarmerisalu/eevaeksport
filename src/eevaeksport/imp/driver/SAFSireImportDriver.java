package eevaeksport.imp.driver;

import java.io.File;
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

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;


public class SAFSireImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

  public SAFSireImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "import"
                                        , "sa", "paranka");
 
      con3 = DriverManager.getConnection(url + "import"
                                         , "sa", "paranka");
      con4 = DriverManager.getConnection(url + "saf"
            , "sa", "paranka");
      con5 = DriverManager.getConnection(url + "saf"
             , "sa", "paranka");

      con6 = DriverManager.getConnection(url + "import"
            , "sa", "paranka");
      con7 = DriverManager.getConnection(url + "import"
            , "sa", "paranka");
      con8 = DriverManager.getConnection(url + "saflit"
              , "sa", "paranka");


    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
  
  public String identifyCustomer(String id,String regnr)throws SQLException {
	    String custid = null;
	    
	    String cmd = "SELECT kl.id FROM grcompany kl WHERE U_import = '" + id + "'";   
	    PreparedStatement st2 = con4.prepareStatement(cmd);
	    ResultSet rs = st2.executeQuery();
	 
	 
	    while (rs.next()){
	    	custid =  rs.getString("id");
	     }
	    rs.close();
	    st2.close();
	    if(custid == null){
	    	   String cmd2 = "SELECT kl.code FROM grcompany kl WHERE  id ='" + id + "'";   
	    	    PreparedStatement st22 = con4.prepareStatement(cmd2);
	    	    ResultSet rs2 = st22.executeQuery();	 
	    	    while (rs2.next()){
	    	    //	custid =  rs2.getString("code");
	    	     }
	    }
	    System.out.println( custid + " " + cmd);  
	   	return custid;
	   }


  public int getSalesid() throws SQLException{
    String cmd = "SELECT MAX(tp_id)+1 AS id from tp ";   
    PreparedStatement st2 = con7.prepareStatement(cmd);
    ResultSet rs = st2.executeQuery();
    int result = 0 ;
    while (rs.next()){
    result =  rs.getInt("id");
     }
    this.idid = result;
    return result;
  }

  public String identifyProduct(String ean, String prodID) {
	  String sql2 = "Select id  as toode FROM grarticle pr " +
      "  where u_ean ='" + ean + "'";

	  String sql3 = "select t.id as toode from grbarcode bar inner join grarticle t on" +
	  		" t.id=bar.idgrarticle where bar.code ='" + ean + "' ";

String sql1 = "Select id AS toode FROM grarticle pr " + 
      " where pr.code='" + prodID + "'";

log.info("1_LINE_SQL: " + sql1);
log.info("2_LINE_SQL: " + sql2);
log.info("3_LINE_SQL: " + sql3);
String prod = null;

try{
	Statement st1 = con5.createStatement();
	ResultSet rs1 = st1.executeQuery(sql3);
	while (rs1.next()) {
		  prod = rs1.getString("toode").trim();
		}
		//System.out.println("prod0" + prod);
		//rs1.close();
		// st1.close();

		if(prod == null || prod.trim().length() == 0){
		  st1 = con5.createStatement();
		  rs1 = st1.executeQuery(sql2);

		  while (rs1.next()) {
		    prod = rs1.getString("toode").trim();
		  }
		}

		
		
		if(prod == null || prod.trim().length() == 0){
			  st1 = con5.createStatement();
			  rs1 = st1.executeQuery(sql1);

			  while (rs1.next()) {
			    prod = rs1.getString("toode").trim();
			  }
			}

		System.out.println("prod1" + prod);
		rs1.close();
		st1.close();
	
}catch(Exception e){
	
}

return prod;
}

  

  public void makeOrder(Order order) throws SQLException{
    setOrder(order);
    String salesid = getSalesid() + "";
    addInvoiceHeader(salesid);
    if(headerok){
    	   addInvoiceLines(salesid);
    	    String sql4 = "update tp set staatus=1 ";
    	    PreparedStatement st2 = con.prepareStatement(sql4);
    	  //  st2.setString(1, salesid.trim());
    	    st2.executeUpdate();
    }
 

  }



  public String getLitTooteID(String kood) throws SQLException{
	  String cmd = "Select id AS toode FROM grarticle pr " + 
      " where pr.code='" + kood + "'"; 
	  
	    PreparedStatement st2 = con8.prepareStatement(cmd);
	    ResultSet rs = st2.executeQuery();
	    String result = kood ;
	    while (rs.next()){
	     result =  rs.getString("toode");
	     }
	    rs.close();
	    st2.close();
	    return result;
	  }

  public String getTaxCode(String kood) throws SQLException{
	  String cmd = "SELECT taxofficecode as code FROM grcompany WHERE LTRIM(id)='" + kood + "'"; 
	  
	    PreparedStatement st2 = con5.prepareStatement(cmd);
	    ResultSet rs = st2.executeQuery();
	    String result = kood ;
	    while (rs.next()){
	     result =  rs.getString("code");
	     }
	    rs.close();
	    st2.close();
	    return result;
	  }
  public String getCompName(String kood) throws SQLException{
	  String cmd = "SELECT name as code FROM grcompany WHERE LTRIM(id)='" + kood + "'"; 
	  
	    PreparedStatement st2 = con5.prepareStatement(cmd);
	    ResultSet rs = st2.executeQuery();
	    String result = kood ;
	    while (rs.next()){
	     result =  rs.getString("code");
	     }
	    rs.close();
	    st2.close();
	    return result;
	  }

 

  private void addInvoiceLines(String salesid) throws SQLException{
	  StringBuffer sb = new StringBuffer();
	
	  String taxcode = getTaxCode(order.getCompany());
	  boolean tofile = false;
	  if(taxcode.indexOf("237153113") != -1 || taxcode.indexOf("40003053029") != -1 
			  ){  // rimi leedu ja l�ti oma ka leetu registri kood ainult toimib 
		  tofile = true;
	  }
	  
	  

	  String number = "";
	    if(order.getDokNr()!= null && order.getDokNr().length()>0){
	  	  number = order.getDokNr();
	    }

	    String tekst = "DokNum:" + number + " " + getCompName(order.getCompany()) + " tofile :" + tofile + " " + taxcode;
    Enumeration enums = order.getOrderLines().elements();
    int counter = 0;
    while (enums.hasMoreElements()){
      counter++;
      log.info("ORDER_LINE_COUNTER:" + counter);
      OrderLine orderLine = (OrderLine)enums.nextElement();
      String itemid = orderLine.getToode();
      double kogus = orderLine.getKogus();
      double ale = orderLine.getDiscount();
     
      double hind = 0;

          String sql = "SELECT pr.code AS kood,max(hi.price) AS hind " +
	       "  FROM grarticle pr LEFT JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id LEFT JOIN grpricelistrel hi ON  " +
	        " hi.idgrarticle=pr.id  LEFT JOIN grarticlecat arca ON arca.id = cat.idgrarticlecat " +
	        " LEFT JOIN grunit y ON y.id=pr.idgrunit  LEFT JOIN grbarcode bc ON bc.id=pr.id " +
	       " WHERE  articletype='G' and hi.price >0 AND pr.id='" + itemid + "'  group by pr.code ";
          
        log.info("SALES_LINE_SQL: " + sql);

        Statement st = con5.createStatement();
        ResultSet rs = st.executeQuery(sql);
      
        //eesti safi - bella collant nime alla - ja koopia l�ti saffi 
       
        while(rs.next()){
        	String kood = rs.getString("kood");
        	  if (ale != 0){
                  hind = rs.getDouble("hind")*(1-ale)*kogus;
                }
       
        	  
        	  if(tofile){
            	  String lit = getLitTooteID(kood);
                  sb.append( "1635;" + lit +  ";"  + kogus  + ";0; ;0;0;0; ;0;0; ;0; ;" + tekst + ";" + tekst + "\n");
          			System.out.println("toode faili panemisel" + kood +  " " + lit  );  
        	  }
        	  
              
          sql = "INSERT INTO ts (tp_id,kaubaID,kaubakood,kogus,hind)" +
            " VALUES( " +
              idid + "," +
              itemid + ",'" +
              kood + "'," +
              kogus + "," +
              hind +")";

              log.info("SALES_LINE_SQL: " + sql);
              PreparedStatement st2 = con3.prepareStatement(sql);
              st2.executeUpdate();
              setAmount(false);
              addNumLine();
              st2.close();
          }
       }
    
    if(sb.length() > 2){
    	String uniq = (Calendar.getInstance().getTimeInMillis() + "").substring(5);
        File f = new File(getDir() + "44." + uniq + ".txt");
        if (f.exists())f.delete();
          try {
    		PrintWriter os = new PrintWriter(new FileWriter(
    				getDir() + "44." + uniq + ".txt", true));
    		os.write(sb.toString());
    		os.close();
    	  }catch (IOException ex) {
    		ex.printStackTrace();
    	  }
     }
    }


private boolean headerok = false;

  private void addInvoiceHeader(String salesid) throws SQLException{
    String sql = "SELECT code FROM grcompany WHERE LTRIM(id)='" + order.getCompany() + "'";
    headerok = false;
      log.info("addInvoiceHeader: " + sql);
      Statement st = con4.createStatement();
      ResultSet rs = st.executeQuery(sql);
      String text = order.getInfo();
      if (text == null){
            text = "";
             }
      
      String number = "";
      if(order.getDokNr()!= null && order.getDokNr().length()>0){
    	  number = order.getDokNr();
      }

      while(rs.next()){
        String custCode = rs.getString("code").trim();
         String agent = order.getAgent();
         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);

         String agentid = " ";
         if (ag.getID().equals("20")) {
          
           agentid = "7";
         }
         else if (ag.getID().equals("21")) {
         
           agentid = "25";
         }
         else if (ag.getID().equals("49")) {
        
           agentid = "20";
         }
         else if (ag.getID().equals("23")) {
         
           agentid = " ";
         }
         else if (ag.getID().equals("24")) {
          
           agentid = "12";
         }
         else if (ag.getID().equals("25")) {
          
           agentid = "20";
         }



         int o = 1;
         if(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==6){
             o = 3;
         }
         
         Calendar cal = Calendar.getInstance();
         if(order.getShipCal() != null){
        	 cal = order.getShipCal();
         }

         if(text.length()>120){
        	 text = text.substring(0,120);
         }
         
           String asi =  text.replaceAll("'"," ");
          if(asi.equals(number)){
        	  asi = "";
          }
         
           System.out.println(text + " - " + number + " - " + asi);
   
   
         sql = "INSERT INTO tp (tellnr,kuupaev,tahtaeg,kliendiID,kliendikood" +
             "  ,sisestajaID,sisestaja,lisainfo) " +
             "VALUES ('" + number + "',getdate(),?" +
             "," + order.getCompany().replace('\'',' ') + ",?,'" + agentid.replace('\'',' ') + "','"  + ag.getName().replace('\'',' ') +
            // "','MJ')";
 			 "',?)";

    log.info("addInvoiceHeader2: " + sql);
        PreparedStatement stmt2 = con6.prepareStatement(sql);
        stmt2.setTimestamp(1, new java.sql.Timestamp(cal.getTimeInMillis()));
        stmt2.setString(2,custCode);
        stmt2.setString(3,asi);
        stmt2.executeUpdate();
        stmt2.close();
        headerok = true;
      //  addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
      }
      st.close();
      
  

  }

  private String getDir(){
	    String dir = parent.getProps().getProperty("output.dir");
	    if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	    return dir;
	  }


  public String getDriverVersion(){
    return "SAFSire";
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
  private Connection con6;
  private Connection con3;
  private Connection con4;
  private Connection con5;
  private Connection con7;
  private Connection con8;

  private ImportPanel parent;
  private Order order = null;
  private int idid = 0;
  private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
 
}
