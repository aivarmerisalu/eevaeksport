package eevaeksport.imp.driver;

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

public class VichiLITSQLUUSImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public VichiLITSQLUUSImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      
     
    } catch (Exception e){
       parent.showError(e);
    }
  }

  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  public String identifyCustomer(String id,String regnr)  {

    String acc = null;

    	
    	try{
    String sql2 = " select [Customer No_] AS klient " +
      " FROM [Vičiūnai ir Ko$Customer Cross Reference]  where [Customer Cross]  = '" + id.trim() + "'";
    System.out.println(sql2);

   Connection con = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st2 = con.createStatement();
    ResultSet rs2 = st2.executeQuery(sql2);

    while (rs2.next()) {
      acc = rs2.getString("klient").trim();

    }
    rs2.close();
    st2.close();
    con.close();

    


  	if (acc == null){

  	String sql1 = "select [Customer No_] AS klient " +
      " FROM [Vičiūnai ir Ko$Customer Cross Reference]  where [Customer No_]  = '" + id + "'";
    System.out.println(sql1);
    Connection con3 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st1 = con3.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      acc = rs1.getString("klient").trim();

    }    
    rs1.close();
    st1.close();
    con3.close();
  	}
  	
  	
  	
  	
  	if (acc == null){

  	String sql1 = "select [No_] AS klient " +
      " FROM [Vičiūnai ir Ko$Customer]  where [EDI ILN]  = '" + id + "'";
    System.out.println(sql1);
    Connection con3 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st1 = con3.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      acc = rs1.getString("klient").trim();

    }    
    rs1.close();
    st1.close();
    con3.close();
  	}

  	if (acc == null && regnr != null && regnr.trim().length() > 0){

  	String sql1 = "select [No_] AS klient " +
      " FROM [Vičiūnai ir Ko$Customer]  where [EDI ILN]  = '" + regnr + "'";
    System.out.println(sql1);
    Connection con3 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st1 = con3.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      acc = rs1.getString("klient").trim();

    }    
    rs1.close();
    st1.close();
    con3.close();
  	}  	
  
    	}catch(Exception e){
  		e.printStackTrace();
  	}
  	
  	if(acc == null){
  		acc = id;
  	}
  	
    System.out.println("Klient: " + acc);
    return acc;

  }


  public String identifyAgent(String customer)  {

	    String agent = null;

	    	
	    	try{

	  	String sql1 = "select [Salesperson Code] AS agent " +
	      " FROM [Vičiūnai ir Ko$Customer]  where [No_]   = '" + customer + "'";
	    System.out.println(sql1);

	    Connection con3 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
	            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));


	    Statement st1 = con3.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      agent = rs1.getString("agent").trim();

	    }    
	    rs1.close();
	    st1.close();
	    con3.close();
	  	
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	  	
	    return agent;

	  }


  
  public String identKett(String customer)  {

	    String agent = null;

	    	
	    	try{

	  	String sql1 = "select [Tinklas] AS agent " +
	      " FROM [Vičiūnai ir Ko$Customer]  where [No_]   = '" + customer + "'";
	    System.out.println(sql1);

	    Connection con3 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
	            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

	    Statement st1 = con3.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      agent = rs1.getString("agent").trim();

	    }    
	    rs1.close();
	    st1.close();
	    con3.close();
	  	
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	  	
	    return agent;

	  }


  
  public String identifyProduct2(String ean, String prodID) throws SQLException{

    String sql1 = "SELECT [No_] AS toode  FROM [Vičiūnai ir Ko$Item] LEFT JOIN [Vičiūnai ir Ko$Item Unit of Measure]" +
    		" vat ON (vat.[Item No_]=[No_] and vat.[Code]='DĖŽ.' ) " +
			"  WHERE [Brūkšninis kodas] ='" + ean + "' order by [Blocked] "; // and [Blocked]=0


    String sql2 = " SELECT [No_] AS toode FROM [Vičiūnai ir Ko$Item] " +
			"  WHERE [No_] = '" + prodID + "' "; // and [Blocked]=0 

   
    String prod = null;
    Connection con = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql2);

  	System.out.println(sql2);
    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
  	  //System.out.println("prod1 " + prod );
    }
    
	System.out.println("prod2 " +sql1);
    if(prod == null){	
      rs1 = st1.executeQuery(sql1);
      if (rs1.next()) {
        prod = rs1.getString("toode").trim();
      //	System.out.println("prod2 " + prod  + sql1);
      }
    }
    st1.close();
    rs1.close();
   con.close();


    System.out.println("prodFINAL " + prod + " " + ean + " " +prodID);

    return prod;
  }

  
  public String identifyProduct(String ean, String prodID) throws SQLException{
	  String tyyp = "";
	  if(prodID.indexOf("|") != -1){
		  tyyp = "and [Cross-Reference Type No_] ='" + prodID.substring( prodID.indexOf("|")+1) + "'";
		  prodID = prodID.substring(0, prodID.indexOf("|"));
	  }
	  
    String sql1 = "SELECT [No_] AS toode  FROM [Vičiūnai ir Ko$Item] LEFT JOIN [Vičiūnai ir Ko$Item Unit of Measure]" +
    		" vat ON (vat.[Item No_]=[No_] and vat.[Code]='DĖŽ.' ) " +
			"  WHERE [Brūkšninis kodas] ='" + ean + "'  order by [Blocked] "; // and [Blocked]=0


    String sql2 = " SELECT [No_] AS toode FROM [Vičiūnai ir Ko$Item] " +
			"  WHERE [No_] = '" + prodID + "' "; // and [Blocked]=0 

    
    String sql3 = "select [Item No_] as toode from [Vičiūnai ir Ko$Item Cross Reference] "
    		+ "where [Cross-Reference No_]='" + prodID + "' and [Cross-Reference No_] !='' " + tyyp ;
    String sql6 = "select [Item No_] as toode from [Vičiūnai ir Ko$Item Cross Reference] "
    		+ "where [Cross-Reference No_]='" + ean + "' and [Cross-Reference No_] !='' " + tyyp ;
 String id = null;
    if(prodID != null){
	    id = prodID.replace('.', ',');
   }
   

    String sql4 = "select [Item No_] as toode from [Vičiūnai ir Ko$Item Cross Reference] where [Cross-Reference No_]='" + id + "'";
   
    String prod = null;
    Connection con3 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st1 = con3.createStatement();
    ResultSet rs1 = st1.executeQuery(sql3);

  	//System.out.println(sql3);
    if (rs1.next()) {
      prod = rs1.getString("toode").trim();
  	//System.out.println("prod1 " + prod );
    }
    
    if(prod == null){	
        rs1 = st1.executeQuery(sql6);
        if (rs1.next()) {
          prod = rs1.getString("toode").trim();
        	//System.out.println("prod2 " + prod  + sql1);
        }
      }
    
	//System.out.println("prod2 " +sql1);
    if(prod == null){	
      rs1 = st1.executeQuery(sql1);
      if (rs1.next()) {
        prod = rs1.getString("toode").trim();
      	//System.out.println("prod2 " + prod  + sql1);
      }
    }
	//System.out.println("prod2 " + prod  + sql1);
    if(prod == null ){
    	
        rs1 = st1.executeQuery(sql2);
        while (rs1.next()) {
          prod = rs1.getString("toode").trim();
        	System.out.println("prod3 " + prod  + sql3);
        }
      }
    //System.out.println("prod4 " + prod  + sql4);
    if(prod == null && prodID != null){
    	
        rs1 = st1.executeQuery(sql4);
        while (rs1.next()) {
          prod = rs1.getString("toode").trim();
        	//System.out.println("prod4 " + prod  + sql4);
        }
      }
   // st1.close();
    rs1.close();
   


if(prod == null){
	 String eanabi = ean;
	    if(eanabi != null && eanabi.length()>7){
	    	eanabi = eanabi.substring(0,7);
	    }
	    
	    String sql5 = "SELECT [No_] AS toode  FROM [Vičiūnai ir Ko$Item] LEFT JOIN [Vičiūnai ir Ko$Item Unit of Measure]" +
		" vat ON (vat.[Item No_]=[No_] and vat.[Code]='DĖŽ.' ) " +
		"  WHERE left([Brūkšninis kodas],10) ='" + eanabi + "...' and [Blocked]=0 ";
	   // System.out.println("prod5 " + prod  + sql5);
    rs1 = st1.executeQuery(sql5);
    if (rs1.next()) {
      prod = rs1.getString("toode").trim();
    	//System.out.println("prod5 " + prod  + sql5);
    }
  }

System.out.println("prod" + prod +" "+ ean+ " " + prodID);

    st1.close();
    con3.close();
   if(prod == null){
   	prod = prodID;
   }
   
    return prod;
  }


  public void makeOrder(Order order) throws SQLException{

    	String salesid =  (Calendar.getInstance().getTimeInMillis()+"") ;	    
        setOrder(order);
        addInvoiceLines(salesid);
  }


  private void addInvoiceLines(String salesid) throws SQLException{
	  
	    String cust = order.getCompany();
        if(cust == null && order.getCompName() != null){
      	  cust = order.getCompName();
        }

	  
	  
    Enumeration enumr = order.getOrderLines().elements();
    int counter = 0;
    int laks = 0;
    
  	String sql1 = "select [Order Splitting Level] as tulem  " +
    " FROM [Vičiūnai ir Ko$Customer]  where [No_]   = '" + order.getCompany() + "'";
 
  	  Connection con5 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
              , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));
    Statement st = con5.createStatement();
    ResultSet rs = st.executeQuery(sql1);
  
    if(rs.next()){
  	  laks = rs.getInt("tulem");
    }
    rs.close();
    st.close();
    
    
    System.out.println("laks " + laks);
    while (enumr.hasMoreElements()){
      counter++;
      log.info("ORDER_LINE_COUNTER:" + counter);
      OrderLine orderLine = (OrderLine)enumr.nextElement();
      String itemid = orderLine.getToode();
      double kogus = orderLine.getKogus();

      
      //String entry = "(select max([Entry No_])+1 as entry from  [Vièiûnai ir Ko$Uþsakymai ið L2B])";
      String entry = "(select getdate() as paev)";
      st = con5.createStatement();
       rs = st.executeQuery(entry);
      String nextEntry = "";
      if(rs.next()){
    	  nextEntry = rs.getString("paev").substring(0,19);
      }
      rs.close();
      st.close();
     // 
      
      String sql0 = "SELECT ([Search Description]) AS nimi, [Unit Price] AS myygihind, [Grupės kodas] AS grupp " +
		",[Base Unit of Measure] AS unit,  [Net Weight] AS kaal " +
	"		,[Blocked] as blokk , [Order separator] as separator  FROM [Vičiūnai ir Ko$Item] t LEFT JOIN " +
	" [Vičiūnai ir Ko$Item Unit of Measure] vat ON (vat.[Item No_]=[No_] and vat.[Code]='DĖŽ.' ) " +
	"		  LEFT JOIN [Vičiūnai ir Ko$Prekių grupavimas] gr ON gr.[Kodas] = t.[Pogrupio kodas] WHERE [No_] =  '" + itemid + "' ";
    log.info("SALES_LINE_SQL: " + sql0);
   
    st = con5.createStatement();
     rs = st.executeQuery(sql0);

    String unit = " ";
    int blokk = 0;
    String separator = "";
    while(rs.next()){
       unit = rs.getString("unit");
       blokk = rs.getInt("blokk");
       separator = rs.getString("separator");
      
       if(separator == null ||  separator.trim().length() == 0){
    	   separator = "";
       }else{
    	   separator = "." + separator.trim();
       }
      if (unit !=null && unit.trim().length()>0){
      	unit = unit.trim();
    
      }else {
      	unit = " ";
      }
    }
    rs.close();
    st.close();
    con5.close();
    
  if(orderLine.getUnit().trim().length()>0){
    unit = orderLine.getUnit();
  }
       
  if(unit != null && unit.equals("DPA") && orderLine.getBonus() > 0){ 
	   kogus = kogus * orderLine.getBonus();	
         }

          String agent = this.identifyAgent(order.getCompany());
         	if(agent == null){
               	agent = "";
               }
         
         	 System.out.println("agent '" + agent + "'");
          
//[Vièiûnai ir Ko$Uþsakymai ið L2B]

          
      
         
          if(cust.length()>20){
        	  cust = cust.substring(0,20);
          }
          
          String type = "";
          if(order.getStatus()== 98 ){
        	 
        	  if(order.getCompName() != null && (order.getCompName().equals("680") || order.getCompName().startsWith("R680"))){
        		  type = "3";
        		  cust = "872";
        	  }
        	  if(order.getCompName() != null && (order.getCompName().equals("R520") || order.getCompName().equals("520") )){
        		  type = "4";
        		  cust = "872";
        	  }
          }
          
          
    
          
       System.out.println("Status " + order.getStatus() + " " + order.getCompName());
        
          String doknr = order.getDokNr();
          if(laks > 0){
        	  String sep = separator;
        	  try{
        		  sep = separator.substring(0,laks+1);
        	  }catch(Exception e){
        		  
        	  }
        	  
        	  doknr = order.getDokNr() + sep;
          }
          
          if(doknr.length() > 20){
        	  doknr = doknr.substring(doknr.length()-20);
          }
          String sql ="INSERT INTO [Vičiūnai ir Ko$Užsakymai iš L2B]    ([Link2Business Order No_],[Order Line No_],[Entry No_],[Order date]" +
    		",[Customer No_], " +
					"[Salesperson Code],[Item No_],[Unit of measure],[Quantity], [Amount],[Unit Price],[Discount Pct],[Error Str], " +
					"[Navision Order No_], [Bin code],[Importo data ir laikas] ,[Order date time], "  +
				    "[Perduotas į užsakymus],[Atmestas],[Suma],[Ship-to Code],[Posting Date])  values " +
					"('" + doknr + "'," + counter + "000,0,'" + nextEntry + "','" + cust + "', " +
					" '" + agent + "','"+itemid+"','" + unit + "', "+kogus+" ,0,0,0,'', " +
					" '" + doknr + "',0,getdate(), " +
					" ?,'','',0,'" + type + "',?) " ;
          
              log.info("Blokk" +  blokk + " " + itemid +  "SALES_LINE_SQL: "  + sql);
              String aiai = parent.getProps().getProperty("testing");
              if(aiai != null && aiai.equals("true")){
            	  System.out.print( "laks" + laks +   "('" + doknr + "'," + counter + "000,0,'" + nextEntry + "','" + cust + "', " +
					" '" + agent + "','"+itemid+"','" + unit + "', "+kogus+" ,0,0,0,'', " +
					" '" + doknr + "',0,getdate(), " +
					" ?,'','',0,'" + type + "'getdate()) ");
              }else{
            	  Connection con3 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            	            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));
            	  PreparedStatement st2 = con3.prepareStatement(sql);
                  if(blokk == 0){
                	  st2.setTimestamp(1, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
                	  Calendar cc = order.getShipCal();
                	  cc.set(Calendar.HOUR_OF_DAY, 0);
                	  cc.set(Calendar.MINUTE, 0);
                	  cc.set(Calendar.SECOND, 0);
                	  cc.set(Calendar.MILLISECOND, 0);
                	  
                	  st2.setTimestamp(2, new java.sql.Timestamp(cc.getTimeInMillis()));
                      st2.executeUpdate();
                      addNumLine();  
                  }
                  st2.close();
                  con3.close();
              }
         }
  }




  

  public String getDriverVersion(){
    return "VichiLITSQL";
  }

  private void setOrder(Order order){
    this.order = order;
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

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
  
}
