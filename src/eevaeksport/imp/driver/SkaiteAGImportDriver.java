/*
4 * Created on 13.09.2007
 */
package eevaeksport.imp.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.CallableStatement;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;


 public class SkaiteAGImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SkaiteEugImportDriver.class.getName());

   public SkaiteAGImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
   
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
         con = this.getNewConnection();
    }
     catch (Exception e) {
       parent.showError(e);
     }
     
     loadSignatures();
   }

   
	private Vector signatures = new Vector();

	private void loadSignatures(){
		
try{		
		File f = new File("signatures.vec");
		Calendar ss = Calendar.getInstance();
		
		if (f.exists() && f.lastModified() < ss.getTimeInMillis() - 3600000) {// kui on yle tunni vana siis maha 
			f.delete();
			signatures = new Vector();
		} else {
			signatures = (Vector) readObject("signatures.vec");
		}

		
	} catch (Exception e) {
		signatures = new Vector();
	}

	}
   
	 public static Object readObject(String s) throws IOException,
     ClassNotFoundException, IOException {
   Object obj = null;
   File f = new File(s);
   if (f.exists()){
     ObjectInputStream ois = new ObjectInputStream(new FileInputStream(s));
     obj = ois.readObject();
     ois.close();
   }
   return obj;
 }



   public String identifyCustomer(String id,String regnr)throws SQLException {
    String custid = null;    
    

    String cmd = "select kodas as kood from Partneriai where kodas ='" + id + "'" ; 
   
  try{

	    PreparedStatement st2 = con.prepareStatement(cmd);
	    ResultSet rs = st2.executeQuery();
	    System.out.println(cmd);
	    while (rs.next()){
	    	custid =  rs.getString("kood");
	     }
	    rs.close();
	    st2.close();
	    
	    
	    System.out.println("custid" +custid + " " +  id.trim().indexOf(" "));
	    
  }catch(Exception ee){
	  ee.printStackTrace();
  }


 	return custid;
   }
   
   
   public String identifyWare(String kood) throws SQLException {
	  String sql = "select warehouseno from asVwCustomer where rtrim(customercode) =  '"+ kood +"'";
	  String res = "EQ";
	/*  Connection  con = this.getNewConnection();
	  PreparedStatement st2 = con.prepareStatement(sql);
	    ResultSet rs = st2.executeQuery();
	 
	    while (rs.next()){
	    	res =  rs.getString("warehouseno").trim();
	     }
	    rs.close();
	    st2.close();
	    con.close();*/
	    if(res.equals("VL00")){
	    	res = "EQ";
	    }
	    
	  return res;
   }
   
   public int getInCases(String kood) throws SQLException {
		  String sql = "select UnitsInPack as kogus from asVwProduct where productcode = '"+ kood +"'";
		 int res = 1;
		// Connection  con = this.getNewConnection();
		  PreparedStatement st2 = con.prepareStatement(sql);
		    ResultSet rs = st2.executeQuery();
 		 
		    while (rs.next()){
		    	res =  rs.getInt("kogus");
		     }
		
		    rs.close();
		    st2.close();
		//    con.close();
		  return res;
	   }
   
   public String identifyInnerCust(Order ord) throws SQLException {
		  String sql = "SELECT CONVERT( varchar(20), ivr_reg.IvNormatyvuReiksmesId ) as kood FROM Partneriai AS part " +
 "  INNER JOIN PL_Partneriai               AS pl      ON part.PartneriaiId = pl.PartneriaiId " +
 "    INNER JOIN IvNormatyvuReiksmes     AS ivr_reg ON pl.RegionasId = ivr_reg.IvNormatyvuReiksmesId " +
 " where part.kodas='"+ ord +"'";
		  String res = "154";
		//  Connection  con = this.getNewConnection();
		  PreparedStatement st2 = con.prepareStatement(sql);
		    ResultSet rs = st2.executeQuery();
		 
		    while (rs.next()){
		    	res =  rs.getString("kood").trim();
		     }
		 
		    rs.close();
		    st2.close();
		 //   con.close();
		  return res;
	   }
   
   
   public String identifyRegion(String kood) throws SQLException {
		  String sql = "SELECT CONVERT( varchar(20), ivr_reg.IvNormatyvuReiksmesId ) as kood FROM Partneriai AS part " +
    "  INNER JOIN PL_Partneriai               AS pl      ON part.PartneriaiId = pl.PartneriaiId " +
    "    INNER JOIN IvNormatyvuReiksmes     AS ivr_reg ON pl.RegionasId = ivr_reg.IvNormatyvuReiksmesId " +
    " where part.kodas='"+ kood +"'";
		  String res = "154";
		//  Connection  con = this.getNewConnection();
		  PreparedStatement st2 = con.prepareStatement(sql);
		    ResultSet rs = st2.executeQuery();
		 
		    while (rs.next()){
		    	res =  rs.getString("kood").trim();
		     }
		 
		    rs.close();
		    st2.close();
		  //  con.close();
		  return res;
	   }
   

   
   public String identifyCurrency(String kood) throws SQLException {
		  String sql = "SELECT valkod as kood FROM Partneriai AS part " +
 " where part.kodas='"+ kood +"'";
		  String res = "Lt";
		 // Connection  con = this.getNewConnection();
		  PreparedStatement st2 = con.prepareStatement(sql);
		    ResultSet rs = st2.executeQuery();
		 
		    while (rs.next()){
		    	res =  rs.getString("kood").trim();
		     }
		 
		    rs.close();
		    st2.close();
		   // con.close();
		  return res;
	   }
   
   private  boolean  get48(String kood) throws SQLException {
		  String sql = "SELECT atstumas as kood  FROM Partneriai AS part INNER JOIN PL_Partneriai AS pl "
		  		+ " ON part.PartneriaiId = pl.PartneriaiId " +
               " where part.kodas='"+ kood +"'";
		  boolean res = false;
		 // Connection  con = this.getNewConnection();
		  PreparedStatement st2 = con.prepareStatement(sql);
		    ResultSet rs = st2.executeQuery();
		 
		    while (rs.next()){
		    	String aha =  rs.getString("kood");
		    	if (aha != null && aha.trim().length() > 0 && aha.trim().equals("48")){
		    		return true;
		    	}
		     }
		 
		    rs.close();
		    st2.close();
		   // con.close();
		  return res;
	   }
 
   
   public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  

   public String identifyProduct(String ean, String prodID) throws SQLException{

	  if( ean!=null){
		     if(ean.equals("59918257")){
		    	 ean = "7613032190842";
		     }
			   
		     if(ean.equals("5998710967612")){
		    	 ean = "7613032194680";
		     }
	 
		     if(ean.equals("5998710967193")){
		    	 ean = "5998710967070";
		     }		  
	  }

  	String sql2 = "SELECT kodas AS toode FROM Prekes " +
        " where KAPKodas like '%" + ean + "%' and kodas not like '%*%'";

  	
    String sql1 = "SELECT kodas AS toode FROM Prekes " +
    " where kodas = '"  + prodID + "'";
   // log.info("1_LINE_SQL: " + sql1);
   // log.info("2_LINE_SQL: " + sql2);

	String sql3 = "SELECT kodas AS toode FROM Prekes " +
    " where KAPKodas = '" + prodID + "' and kodas not like '%*%'";

	
    
    String prod = null;
  //  Connection  con = this.getNewConnection();
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql2);
    
    if(ean != null){
   
    int counter = 0;
    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
      counter++;
    }

    if(counter > 1){
    	return null;
    }
	
    }
    
    counter = 0;
    if(prodID != null && (prod == null || prod.trim().length() == 0)){
      st1 = con.createStatement();
      rs1 = st1.executeQuery(sql1);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
        counter++;
      }
      if(counter > 1){
      	return null;
      }
      }
      
    counter = 0;
    if(prodID != null && (prod == null || prod.trim().length() == 0)){
      st1 = con.createStatement();
      rs1 = st1.executeQuery(sql3);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
        counter++;
      }
      if(counter > 1){
      	return null;
      }
      }
    System.out.println("prod1" + prod);
    rs1.close();
    st1.close();
  // con.close();
    return prod;
   }

   public void makeOrder(Order order) throws SQLException{
	   
	   if (signatures == null){
    	   signatures = new Vector();
       }
     setOrder(order);
    // System.out.println("agent sisenemisel "+ order.getAgent());
     String refr = order.getDokNr();
    String add  =  parent.getProps().getProperty("add.ordernr");
    if(add == null){
    	add = "";
    }
    
     
   
     String doknr = add + order.getDokNr()+Converter.deCode(order.getCompany());
     if(order.getDokNr() == null){
    	 doknr =  order.getInfo()+Converter.deCode(order.getCompany());
    	  if( order.getInfo() == null){
    	    	doknr =( Calendar.getInstance().getTimeInMillis()+"" ).substring(5)+Converter.deCode(order.getCompany());
    	    }
    	 refr = "";
     }
     
     String aiai = parent.getProps().getProperty("testing");
     if(aiai != null && aiai.equals("true")){
    	 
    	   if(order.getStatus() == 2 || order.getStatus() == 4){
    	    	System.out.println("Re Pricing Document");
    	    	repricemode = true;
    	    	//tyyp = "123";
    	    }
    	  Enumeration enumr = order.getOrderLines().elements();	     
    	     while (enumr.hasMoreElements()){
    	    	this.addNumLine(); 
    	    	OrderLine orderLine = (OrderLine)enumr.nextElement(); 	 
    	    	 String unit = orderLine.getUnit();
    	         String itemid = orderLine.getToode();
    	         double kogus = orderLine.getKogus();
    	         String ware = "EQ";
    	    	   if(orderLine.getDealer() != null && orderLine.getDealer().trim().length() > 0){
    	    		   ware = orderLine.getDealer();
    	    	   }
    	    	System.out.println(order.getCompany() + " " + itemid + " " + orderLine.getKogus() + " " + kogus + " '' " + ware);
    	     }
    	       
     }else{
    	 
    	  String signature = order.getSignature();
		  if(signatures.contains(signature)){
			  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
			    		  
		  }else{
		 	  String salesid = addInvoiceHeader(doknr,refr,order.getInfo()) + "";
	    	     addInvoiceLines(salesid, doknr);
	    	     finishOrder(salesid,doknr);	 
	   		  signatures.add(signature);
			  this.trouble = null;
			  
		  }
    	 
		   try {
				saveObject(signatures, "signatures.vec");
			} catch (IOException e) {
				e.printStackTrace();
			}
		  
     }
     
   
   }

   public static void saveObject(Object obj, String file) throws IOException {
 		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
 		oos.writeObject(obj);
 		oos.close();
 	}
 	
   
   private String trouble = null;
   public String getTrouble() {
		// TODO Auto-generated method stub
		return trouble;
	}

   private void finishOrder(String salesid,String doknr) throws SQLException{
	   
	 	
       String   // sql = "uspu_IE_PardavimaiRun  NULL," + salesid ;
     sql = " uspu_IE_ClearNeimportuoti   NULL,2," + salesid ;
            log.info("SALES_ORDER_FINISH_SQL: " + sql);
           // System.out.println("SALES_ORDER_FINISH_SQL: " + sql);
        	String region = identifyRegion(Converter.deCode(order.getCompany()));
            
            CallableStatement st2 = con.prepareCall(sql);
            st2.executeUpdate(); 
            log.info("Finishing Order " + salesid + " success" );        
            st2.close();
        }
      

   private void addInvoiceLines(String salesid,String originalnr) throws SQLException{

     Enumeration enumr = order.getOrderLines().elements();
     int counter = 0;
     
 	
     
     
     while (enumr.hasMoreElements()){
       counter++;
     //  log.info("ORDER_LINE_COUNTER:" + counter);
       OrderLine orderLine = (OrderLine)enumr.nextElement();
       String itemid = orderLine.getToode();      
       double kogus = orderLine.getKogus();
       double ale = orderLine.getDiscount();  
       
       String name = orderLine.getTooteNimi();
       String unit = orderLine.getUnit();
       String hindkm = "null";
       String asi = "1";
       String lopphind = "null";
       String nullale = "null";
       
       String ware = "EQ";
	   if(order.getWarehouse() != null && order.getWarehouse().trim().length() > 0){
		   ware = order.getWarehouse();
		   if(ware.equals("00:00")){
			   ware = "EQ";
		   }
	   }
       
       
       if(this.repricemode){
    	   hindkm = "" + (orderLine.getPrice()*((100-orderLine.getDiscount())/100) * 1.21 ); 
    	  // System.out.println("repricemode " + repricemode + " " + hindkm);
    	   if(kogus < 0){
    		   kogus = kogus *-1;   
    	   }
    	   
    	   asi = "0";
    	   nullale = "0.00";
    	   lopphind = (orderLine.getPrice()*((100-orderLine.getDiscount())/100)) + "";
       }

       
       
       if(itemid != null && itemid.trim().length()>0){    	
    	   name = name.replaceAll("'", "");
    	   name = name.replaceAll("\"", "");
    	  
    	   
    	   
    	   
    	   String    sql = "declare @OpId INT \n"+
           " select @OpId = NULL \n" + 
           " exec uspi_IE_PardavimuKunai  @OpId Output ," + salesid + ",' '," +
		         "'" + itemid + "',null,null,''," + kogus + "," +
		           lopphind + ",null,null,'" + name + "',null,null,null,null," + ware + "," + nullale + "," + ale +
			             " ,null,null,null,null,null,null,null,null," + hindkm + "," +
			 " null,null,null,null," + asi + ",null,null,null,null,null,null,1 \n" + //39
			 " select @OpId " ;
  

               System.out.println("SALES_LINE_SQL: " + sql);
           	String region = identifyRegion(Converter.deCode(order.getCompany()));
               
               CallableStatement st2 = con.prepareCall(sql);
              if(kogus !=0){
            	  try{
            		  st2.executeQuery();  
                      setAmount(false);
                      addNumLine();   
            	  }catch (Exception rr){
            		  con = getCon(region);
                       st2 = con.prepareCall(sql);
                       st2.executeQuery();  
                       setAmount(false);
                       addNumLine();  
            	  }
                
              }else{
            	  trouble = ("PRODUCT CANNOT BE RESOLVED ABC format : \n" 
 	     				 + "\t" + name +" Code:" + itemid + " amount: " + orderLine.getKogus() + " Unit: " + unit +  "\n\n");
              }
           
           
               st2.close();
           }  
     }
     
   }



   private Connection getCon(String id ){
	   return getNewConnection();
   }

	private Connection getNewConnection() {
		try {
			String asi = "eugesta";
			
			 if( parent.getProps().getProperty("odbc") != null){
				 asi = parent.getProps().getProperty("odbc");
			 }
			return DriverManager.getConnection(url + asi
			        ,  "M_Partner_1", "mpsk2002");
		} catch (SQLException e) {
			
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}
	
   
   private boolean repricemode = false;
   
   private int addInvoiceHeader(String doknr ,String refer,String info) throws SQLException{
	   repricemode = false;
   	String agent = order.getAgent();
   	
   	int result = 0;
       String text = order.getInfo();
       if (text == null){
             text = "";
              }
  Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
       
    String ladu = "";
    	ladu = "EQ";   	
    	if(order.getWarehouse() != null && order.getWarehouse().trim().length() > 0){
    		ladu =  order.getWarehouse().trim();    		
    	}
  
   		System.out.println("ladu ja agent ;" + ladu + " " + agent + " " + order.getAgent());
    order.setAgentCode(agent);
	
    String currency = identifyCurrency(Converter.deCode(order.getCompany()));
    boolean cust48 = get48(Converter.deCode(order.getCompany()));
    
    String tyyp = "53";
    if(order.getStatus() == 1){
    	System.out.println("Return Document");
    	tyyp = "50";
    }
    if(ladu.equals("00:00")){
    	ladu = "VL00";
    }
    
    if(order.getStatus() == 2  || order.getStatus() == 4 ){
    	System.out.println("Re Pricing Document");
    	repricemode = true;
    	tyyp = "123";
    	ladu = "EQ";	
    	/*·         Параметр @piIE_PardavimuGalvaOUTID (index = 1) уникалный номер документа
·         Параметр @piPerkodavimuTipaiId (index = 2) должен бить 110350
·         Параметр @piOperacijosTipoId (index = 3) должен бить 123
·         Параметр @piData (index = 4) дата документа
·         Параметр @piPartnerioKodas (index = 5) код клиента
·         Параметр @piMokesciuMetodika (index = 6) должен бить 11
·         Параметр @piValiutosKodas (index = 8) должен бить LT
·         Параметр @piSandelioKodas (index = 9) должен бить EQ
·         Параметр @ piVykdytojoKodas (index = 10) код оператора
·         Параметр @ piModulioId (index = 21) должен бить 35
·         Параметр @ piFakturaNr (index = 22) должен бить X
*/
    }  //kommentisin välja et jama poleks 
    
    Calendar ccc = Calendar.getInstance();
    ccc.setTime(order.getShipCal().getTime());
    
   // kui on klienidl 48 h vedu siis paneme yhe päeva juurde 
   
    if(cust48){
    	if(ccc.get(Calendar.DAY_OF_WEEK)== Calendar.FRIDAY){
    		ccc.add(Calendar.DAY_OF_MONTH, 3);
    	}else{
      		ccc.add(Calendar.DAY_OF_MONTH, 1);
    	}
    }
    
    doknr= agent + "-" + (Calendar.getInstance().getTimeInMillis() + "").substring(5);
    String sql = "declare @OpId INT \n"+
    "select @OpId = NULL \n" +
    "exec uspi_IE_PardavimuGalva @OpId Output,'" + doknr + "',110350," + tyyp + ",'" + getDate(ccc) + "'," +
		
		"'" + Converter.deCode(order.getCompany()) + "',11,null,'" + currency + "'," +
		"'" + ladu + "','" + agent + "',null,null,null,null,null,null,null,null,null,null,160,'X'," +
		"null,null,null,null,'" + text.replaceAll("'", " ") + "',null,'"+ refer.replaceAll("'", " ") + "',null,null,null," +
		"null,null,null,null,null,null,null,null,null,null," +
		"null,null,null,null,null,null,null,null,null,null,null,null,null,null,1,1	\n" + //  2 null juures
		"select @OpId" ;
 

       log.info("addInvoiceHeader2: " + sql);
       System.out.println(sql);
      // System.exit(0);
       
     CallableStatement  msProcedure = con.prepareCall( sql );
     
	 
         System.out.println("number: " + doknr);
         System.out.println("kuupäev: " + getDate(ccc));
         System.out.println("firma: " + Converter.deCode( order.getCompany()));
         System.out.println("ladu: " + ladu);
         System.out.println("agent: " + agent + " " +  order.getAgent());
         System.out.println("text: " + text);
       //  System.exit(0);
         //stmt2.setTimestamp(2, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
      ResultSet rs3 = msProcedure.executeQuery();
      if(rs3.next()){
      	result = rs3.getInt(1);
      }
       rs3.close();
       msProcedure.close();
       
       addLog("Added Order Header -> " + order.getCompany() + " given:" + result + " original;" + doknr +"\n");
         System.out.println("Added Order Header -> " + order.getCompany() + " given:" + result + " original:" + doknr );

       return result;
       
   }


   public String getDriverVersion(){
     return "SkaiteAG";
   }

   private String getDate(Calendar cal){
   String yy = cal.get(Calendar.YEAR)+"";
   String mm = (cal.get(Calendar.MONTH)+1)+"";
   if(mm.length()==1){
   	mm = "0"+ mm ;
   }
   String dd = (cal.get(Calendar.DAY_OF_MONTH)) +"";
   if(dd.length()==0){
   	dd = "0" + dd;
   }
   return yy+"-"+mm+"-"+dd;
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



   private ImportPanel parent;
   private Order order = null;
   private boolean isAmount = false;
   private int counter = 0;
  private  Connection  con = null;

 }

