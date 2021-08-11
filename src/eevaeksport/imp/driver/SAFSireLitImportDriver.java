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


public class SAFSireLitImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

  public SAFSireLitImportDriver(ImportPanel parent)
  
  {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "importlit"
                                        , "sa", "paranka");
      con8 = DriverManager.getConnection(url + "saf"
              , "sa", "paranka");

      con3 = DriverManager.getConnection(url + "importlit"
                                         , "sa", "paranka");
      con4 = DriverManager.getConnection(url + "saflit"
            , "sa", "paranka");
      con5 = DriverManager.getConnection(url + "saflit"
               , "sa", "paranka");

      con6 = DriverManager.getConnection(url + "importlit"
            , "sa", "paranka");
      con7 = DriverManager.getConnection(url + "importlit"
            , "sa", "paranka");
    }
    catch (Exception e) {
      parent.showError(e);
    }
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
	    	   String cmd2 = "SELECT kl.id FROM grcompany kl WHERE  id ='" + id + "'";   
	    	    PreparedStatement st22 = con4.prepareStatement(cmd2);
	    	    ResultSet rs2 = st22.executeQuery();	 
	    	    while (rs2.next()){
	    	    	custid =  rs2.getString("id");
	    	     }
	    }
	    System.out.println( custid + " " + cmd);  
	   	return custid;
	   }

  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
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
    addInvoiceLines(salesid);
    String sql4 = "update tp set staatus=1 ";
    PreparedStatement st2 = con.prepareStatement(sql4);
   // st2.setString(1, salesid.trim());
    st2.executeUpdate();

  }






  private void addInvoiceLines(String salesid) throws SQLException{

      StringBuffer sb = new StringBuffer(); 
    Enumeration enums = order.getOrderLines().elements();
    int counter = 0;
    
    String number = "";
    if(order.getDokNr()!= null && order.getDokNr().length()>0){
  	  number = order.getDokNr();
    }
    String tekst = "KlKood Lit :" + order.getCompany() + " DokNum:" + number ;
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
          
      //  log.info("SALES_LINE_SQL: " + sql);
      String kood = "";
        Statement st = con5.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while(rs.next()){
        	kood = rs.getString("kood").trim();
            
            if (ale != 0){
            	hind = rs.getDouble("hind")*(1-ale)*kogus;
            }

        	//26;7557;50.0;0; ;0;0;0; ;0;0; ;0; ; 
          //  String est = getEstTooteID(kood);
        //	sb.append( "1429;" + est +  ";"  + kogus  + ";0; ;0;0;0; ;0;0; ;0; ;" + tekst + ";" + tekst + "\n");
        	//		System.out.println("toode" + kood +  " " + est  );
          sql = "INSERT INTO ts (tp_id,kaubaID,kaubakood,kogus,hind)" +
            " VALUES( " +
              idid + "," +
              itemid + ",'" +
              kood + "'," +
              kogus + "," +
              hind +")";

             // log.info("SALES_LINE_SQL: " + sql);
              PreparedStatement st2 = con3.prepareStatement(sql);
              st2.executeUpdate();
              setAmount(false);
              addNumLine();
              st2.close();
          }
       }

    if(sb.length() > 2){
    	String uniq = (Calendar.getInstance().getTimeInMillis() + "").substring(5);
        File f = new File(getDir() + "31." + uniq + ".txt");
        if (f.exists())f.delete();
          try {
    		PrintWriter os = new PrintWriter(new FileWriter(
    				getDir() + "31." + uniq + ".txt", true));
    		os.write(sb.toString());
    		os.close();
    	  }catch (IOException ex) {
    		ex.printStackTrace();
    	  }
     }
    }

  private String getDir(){
	    String dir = parent.getProps().getProperty("output.dir");
	    if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	    return dir;
	  }

  public String getEstTooteID(String kood) throws SQLException{
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


  private void addInvoiceHeader(String salesid) throws SQLException{
    String sql = "SELECT code FROM grcompany WHERE LTRIM(id)='" + order.getCompany() + "'";
 
      //log.info("addInvoiceHeader: " + sql);
      Statement st = con4.createStatement();
      ResultSet rs = st.executeQuery(sql);
      String text = order.getEmailBody();
      if (text == null){
            text = "";
             }
      if(order.getInfo() != null && order.getInfo().trim().length() > 0){
    	  text = order.getInfo();
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
         } else if (ag.getID().equals("21")) {
          agentid = "25";
         }



         if(text.length()>120){
        	 text = text.substring(0,120);
         }

         String asi = number ;
           if(!asi.equals(text)){
        		 asi = asi + text.replaceAll("'"," ");
           }
        
         sql = "INSERT INTO tp (tellnr,kuupaev,tahtaeg,kliendiID,kliendikood" +
             "  ,sisestajaID,sisestaja,lisainfo) " +
             "VALUES (' " + salesid + "', ?,getdate()" +
             "," + order.getCompany().replace('\'',' ') + ",?,'" + agentid.replace('\'',' ') + "','"  + ag.getName().replace('\'',' ') +
            // "','MJ')";
 			 "','"+ asi + "')";

   // log.info("addInvoiceHeader2: " + sql);
        PreparedStatement stmt2 = con6.prepareStatement(sql);
      Calendar cal = Calendar.getInstance();
      if(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==6){
          cal.add(Calendar.DAY_OF_YEAR, 3);
      }else{
    	  cal.add(Calendar.DAY_OF_YEAR, 1);
      }
      if(order.getShipCal() != null){
    	  cal = order.getShipCal();
      }
        stmt2.setString(2,custCode);
        stmt2.setTimestamp(1, new java.sql.Timestamp(cal.getTimeInMillis()) );
        stmt2.executeUpdate();
        stmt2.close();
        addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
      }
      st.close();

  }


  public String getDriverVersion(){
    return "SAFSireLit";
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
