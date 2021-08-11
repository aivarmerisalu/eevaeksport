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

public class VichiLATSQLImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public VichiLATSQLImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
        , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));
      //con.setAutoCommit(false);
      //con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
      con2 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
              , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));
      con5 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
        , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));
      con3 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
        , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));
      con5 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));
      //stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }

  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  public String identifyCustomer(String id,String regnr)  {

	    String acc = null;
	    	
	    
	    String sql3 = " select k.[No_] AS klient " +
	      " FROM [Vičiūnai-LAT$Customer] k  where k.[No_] = '" + id.trim() + "'";
	    String sql2 = " select [Customer No_] AS klient " +
	    " FROM [Vičiūnai-LAT$Customer Cross Reference]  where [Customer Cross]  = '" + id.trim() + "'";
	    String sql1 = "select [No_] as klient from [Vičiūnai-LAT$Customer] where [Import No_] = '" + id.trim() + "'";
	    String sql4 = "select [No_] as klient from [Vičiūnai-LAT$Customer] where [EDI ILN] = '" + id.trim() + "'";
	    System.out.println(sql2);

	    try{
	    Statement st2 = con.createStatement();
	    ResultSet rs2 = st2.executeQuery(sql2);
		
	    if (rs2.next()) {
	      acc = rs2.getString("klient").trim();
	    }
	    rs2.close();
	    st2.close();

	  	
	 	if(acc == null){
	 		st2 = con.createStatement();
	 	     rs2 = st2.executeQuery(sql4);

	 	    if (rs2.next()) {
	 	      acc = rs2.getString("klient").trim();
	 	    }
	 	    rs2.close();
	 	    st2.close();
	 		 		
	 	}
	  	

	 	if(acc == null){
	 		st2 = con.createStatement();
	 	     rs2 = st2.executeQuery(sql1);

	 	    if (rs2.next()) {
	 	      acc = rs2.getString("klient").trim();
	 	    }
	 	    rs2.close();
	 	    st2.close();
	 		 		
	 	}
	  	
	 	if(acc == null){
	 		st2 = con.createStatement();
	 	     rs2 = st2.executeQuery(sql3);

	 	    if (rs2.next()) {
	 	      acc = rs2.getString("klient").trim();
	 	    }
	 	    rs2.close();
	 	    st2.close();
	 		 		
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
	      " FROM [Vičiūnai-LAT$Customer]  where [No_]   = '" + customer + "'";
	    System.out.println(sql1);



	    Statement st1 = con3.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      agent = rs1.getString("agent").trim();

	    }    
	    rs1.close();
	    st1.close();
	  	
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	  	
	    return agent;

	  }


  
  public String identKett(String customer)  {

	    String agent = null;

	    	
	    	try{

	  	String sql1 = "select [Tinklas] AS agent " +
	      " FROM [Vičiūnai-LAT$Customer]  where [No_]   = '" + customer + "'";
	    System.out.println(sql1);



	    Statement st1 = con3.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      agent = rs1.getString("agent").trim();

	    }    
	    rs1.close();
	    st1.close();
	  	
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	  	
	    return agent;

	  }


  
  public String identifyProduct2(String ean, String prodID) throws SQLException{

    String sql1 = "SELECT [No_] AS toode  FROM [Vičiūnai ir Ko$Item] LEFT JOIN [Vičiūnai-LAT$Item Unit of Measure]" +
    		" vat ON (vat.[Item No_]=[No_] and vat.[Code]='DĖŽ.' ) " +
			"  WHERE [Brūkšninis kodas] ='" + ean + "' order by [Blocked] "; // and [Blocked]=0


    String sql2 = " SELECT [No_] AS toode FROM [Vičiūnai-LAT$Item] " +
			"  WHERE [No_] = '" + prodID + "' "; // and [Blocked]=0 

   
    String prod = null;

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
    rs1.close();
  


    System.out.println("prodFINAL " + prod + " " + ean + " " +prodID);

    return prod;
  }

  
  public String identifyProduct(String ean, String prodID) throws SQLException{
	  String tyyp = "";
	  if(prodID.indexOf("|") != -1){
		  tyyp = "and [Cross-Reference Type No_] ='" + prodID.substring( prodID.indexOf("|")+1) + "'";
		  prodID = prodID.substring(0, prodID.indexOf("|"));
	  }
	  
    String sql1 = "SELECT [No_] AS toode  FROM [Vičiūnai-LAT$Item] LEFT JOIN [Vičiūnai ir Ko$Item Unit of Measure]" +
    		" vat ON (vat.[Item No_]=[No_] and vat.[Code]='DĖŽ.' ) " +
			"  WHERE [Brūkšninis kodas] ='" + ean + "'   and [Blocked]=0 order by [Blocked] "; // and [Blocked]=0


    String sql2 = " SELECT [No_] AS toode FROM [Vičiūnai-LAT$Item] " +
			"  WHERE [No_] = '" + prodID + "' and [Blocked]=0 "; // and [Blocked]=0 

    
    String sql3 = "select [Item No_] as toode from [Vičiūnai-LAT$Item Cross Reference] where"
    		+ " [Cross-Reference No_]='" + prodID + "' and [Cross-Reference No_] !='' " + tyyp ;
    
    String sql5 = "select [Item No_] as toode from [Vičiūnai-LAT$Item Cross Reference] where"
    		+ " [Cross-Reference No_]='" + ean + "' and [Cross-Reference No_] !='' " + tyyp ;
 String id = null;
    if(prodID != null){
	    id = prodID.replace('.', ',');
   }
   

    String sql4 = "select [Item No_] as toode from [Vičiūnai-LAT$Item Cross Reference] where [Cross-Reference No_]='" + id + "'";
   
    String prod = null;

    Statement st1 = con3.createStatement();
    ResultSet rs1 = st1.executeQuery(sql3);

  	//System.out.println(sql3);
    if (rs1.next()) {
      prod = rs1.getString("toode").trim();
  	//System.out.println("prod1 " + prod );
    }
    
    if(prod == null){	
        rs1 = st1.executeQuery(sql5);
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
    rs1.close();
  


if(prod == null){
	 String eanabi = ean;
	    if(eanabi != null && eanabi.length()>7){
	    	eanabi = eanabi.substring(0,7);
	    }
	    
	    String sql6 = "SELECT [No_] AS toode  FROM [Vičiūnai-LAT$Item] LEFT JOIN [Vičiūnai-LAT$Item Unit of Measure]" +
		" vat ON (vat.[Item No_]=[No_] and vat.[Code]='IEP' ) " +
		"  WHERE left([Brūkšninis kodas],10) ='" + eanabi + "...' and [Blocked]=0 ";
	   // System.out.println("prod5 " + prod  + sql5);
    rs1 = st1.executeQuery(sql6);
    if (rs1.next()) {
      prod = rs1.getString("toode").trim();
    	//System.out.println("prod5 " + prod  + sql5);
    }
  }

System.out.println("prod" + prod +" "+ ean+ " " + prodID);

    st1.close();
   if(prod == null){
   	prod = prodID;
   }
   
    return prod;
  }

 
  public void makeOrder(Order order) throws SQLException{
System.out.println("Order Status and Spliting" + order.getStatus() + " ");
    if(order.getStatus() == 200){
    	  System.out.println("Order NOT SPLITED ");
    	String salesid =  (Calendar.getInstance().getTimeInMillis()+"") ;	    
        setOrder(order);
        addInvoiceLines(salesid);
    }else{
    	
    	 String salesid =  (Calendar.getInstance().getTimeInMillis()+"") ;	    
         setOrder(order);
         addInvoiceLines(salesid);
    	
    	
 	
   	}
  }

  
  private String getRimiProd(String prodid) {


	  	try{
	  	
	  	  
		  	String sql = 	"select [Item No_] as toode  from [Vičiūnai ir Ko$Item Cross Reference] where [Cross-Reference Type No_]='LAT' " +
			"  and [Cross-Reference No_] = '" + prodid + "'";
		  	
	  		String	sql1 = "  select [No_] as toode  from [Vičiūnai ir Ko$Item] where [No_] = '" + prodid + "' and [Blocked]=0"; 
			Statement st1 = con.createStatement();
			ResultSet rs1 = st1.executeQuery(sql);
			
			if (rs1.next()) {
			 return rs1.getString("toode").trim();
			}
	
	      rs1.close();
	      st1.close();
	    
	  	 st1 = con.createStatement();
		 rs1 = st1.executeQuery(sql1);
		
		if (rs1.next()) {
		  return  rs1.getString("toode").trim();
		}

	      rs1.close();
	      st1.close();
	  	}catch(Exception e){
	  		
	  	}

	  	return prodid ;
	  }

  
  private String getRimiCust() {
	  	String res = "";
	 	

	  	try{
	  	  String sql2 = " select [Customer No_] AS klient " +
	      " FROM [Vičiūnai ir Ko$Customer Cross Reference]  where [Customer Cross]  = '" + order.getILN().trim() + "'"; 
			
	  	System.out.println("Tere " + sql2 );
	  	Statement st1 = con.createStatement();
			System.out.println("Tere 2 " + sql2 );
			ResultSet rs1 = st1.executeQuery(sql2);
			
			if (rs1.next()) {
			res = rs1.getString("klient");
			}
	
	      rs1.close();
	      st1.close();
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}

	  	return res ;
	  }
  
  private boolean getRimi() {
	  	boolean res = false;
	 	

	  	try{
	  		 String	sql1 = "select k.[No_] AS klient  FROM [Vičiūnai-LAT$Customer] k " +
	  		 		" where k.[Tinklas]  like '%KESKO%' and k.[No_]= '" + order.getCompany() + "'"; 
			Statement st1 = con.createStatement();
			ResultSet rs1 = st1.executeQuery(sql1);
			 System.out.println(sql1);
			
			if (rs1.next()) {
			res = true;
			}
		
	      rs1.close();
	      st1.close();
	  	}catch(Exception e){
	  		
	  	}
  System.out.println("is Rimi " + res);
	  	return res ;
	  }

  

  private void addInvoiceLines(String salesid) throws SQLException{
	  
	    String cust = order.getCompany();
        if(cust == null && order.getCompName() != null){
      	  cust = order.getCompName();
        }
     boolean rimi = getRimi();
     String rimicust = order.getCompany();
     System.out.println("order Iln " + rimicust + " " + order.getILN());
     if(rimi && order.getILN() != null){
    	 rimicust = getRimiCust();
     }
     System.out.println("order Iln 2 " + rimicust + " " + order.getILN());
       
     int laks = 0;
     
   	String sql1 = "select [Order Splitting Level] as tulem  " +
     " FROM [Vičiūnai-LAT$Customer]  where [No_]   = '" + order.getCompany() + "'";
     Statement st = con5.createStatement();
     ResultSet rs = st.executeQuery(sql1);
   
     if(rs.next()){
   	  laks = rs.getInt("tulem");
     }
     rs.close();
     st.close();
     
	  
    Enumeration enumr = order.getOrderLines().elements();
    int counter = 0;
    while (enumr.hasMoreElements()){
      counter++;
     // log.info("ORDER_LINE_COUNTER:" + counter);
      OrderLine orderLine = (OrderLine)enumr.nextElement();
      String itemid = orderLine.getToode();
      double kogus = orderLine.getKogus();

      
      //String entry = "(select max([Entry No_])+1 as entry from  [Vi�i�nai ir Ko$U�sakymai i� L2B])";
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
      
      String sql0 = "SELECT ([Search Description]) AS nimi, [Unit Price] AS myygihind, [Grupės kodas] AS grupp,[Pogrupio kodas] AS grupp2 " +
		",[Base Unit of Measure] AS unit,[Brūkšninis kodas] AS ean,[Product Status] AS status ,[MaxDiscount] AS maxdiscount, " +
	"	  [Type] AS type, [Qty_ per Unit of Measure] AS kastis,  [Net Weight] AS kaal, [VAT Prod_ Posting Group] AS vat, [Description] AS vatdesc " +
	"		,[Blocked] as blokk , [Order separator] as separator FROM [Vičiūnai-LAT$Item] t LEFT JOIN [Vičiūnai-LAT$Item Unit of Measure] vat ON (vat.[Item No_]=[No_] and vat.[Code]='IEP' ) " +
	"		 LEFT JOIN [Vičiūnai-LAT$Prekių grupavimas] gr ON gr.[Kodas] = t.[Pogrupio kodas] WHERE [No_] =  '" + itemid + "' ";
   // log.info("SALES_LINE_SQL: " + sql0);

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
          
//[Vi�i�nai ir Ko$U�sakymai i� L2B]

          
      
         
          if(cust.length()>20){
        	//  cust = cust.substring(0,20);
          }
          
          String type = "";
          if(false && order.getStatus()== 98 ){
        	 
        	  if(order.getCompName() != null && (order.getCompName().equals("680") || order.getCompName().startsWith("R680"))){
        		  type = "3";
        		//  cust = "872";
        	  }
        	  if(order.getCompName() != null && (order.getCompName().equals("R520") || order.getCompName().equals("520") )){
        		  type = "4";
        		 
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
         
          
          String sql ="INSERT INTO [Vičiūnai-LAT$Užsakymai iš L2B]    ([Link2Business Order No_],[Order Line No_],[Entry No_],[Order date]" +
    		",[Customer No_], " +
					"[Salesperson Code],[Item No_],[Unit of measure],[Quantity], [Amount],[Unit Price],[Discount Pct],[Error Str], " +
					"[Navision Order No_], [Bin code],[Importo data ir laikas] ,[Order date time], "  +
				    "[Perduotas į užsakymus],[Atmestas],[Suma],[Ship-to Code],[Posting Date])  values " +
					"('" + doknr + "'," + counter + "000,0,'" + nextEntry + "','" + cust + "', " +
					" '" + agent + "','"+itemid+"','" + unit + "', "+kogus+" ,0,0,0,'', " +
					" '" + doknr + "',0,getdate(), " +
					" ?,'','',0,'" + type + "',?) " ;
         
          
            //  log.info("Blokk" +  blokk + " " + itemid +  "SALES_LINE_SQL: "  + sql);
              String aiai = parent.getProps().getProperty("testing");
              if(aiai != null && aiai.equals("true")){
            	  System.out.print("('" + doknr + "'," + counter + "000,0,'" + nextEntry + "','" + cust + "', " +
					" '" + agent + "','"+itemid+"','" + unit + "', "+kogus+" ,0,0,0,'', " +
					" '" + doknr + "',0,getdate(), " +
					" ?,'','',0,'" + type + "'getdate()) ");
            	  if(rimi){//leetu ka tellimus kui on Rimi oma
                      
                	  String sql2 ="INSERT INTO [Vičiūnai ir Ko$Užsakymai iš L2B]    ([Link2Business Order No_],[Order Line No_],[Entry No_],[Order date]" +
                		",[Customer No_], " +
              					"[Salesperson Code],[Item No_],[Unit of measure],[Quantity], [Amount],[Unit Price],[Discount Pct],[Error Str], " +
              					"[Navision Order No_], [Bin code],[Importo data ir laikas] ,[Order date time], "  +
              				    "[Perduotas į užsakymus],[Atmestas],[Suma],[Ship-to Code],[Posting Date])  values " +
              					"('" + doknr + "'," + counter + "000,0,'" + nextEntry + "','" + rimicust + "', " +
              					" '" + agent + "','"+getRimiProd(itemid)+"','" + unit + "', "+kogus+" ,0,0,0,'', " +
              					" '" + doknr + "',0,getdate(), " +
              					" ?,'','',0,'" + type + "',?) " ;
                	  
                System.out.println(sql2);           
              
            	  }  
                
            	  addNumLine(); 
              }else{
            	  Connection ccc = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
                          , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));
            	  PreparedStatement st2 = ccc.prepareStatement(sql);
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
                      if(rimi){//leetu ka tellimus kui on Rimi oma
                      
                    	  String sql2 ="INSERT INTO [Vičiūnai ir Ko$Užsakymai iš L2B]    ([Link2Business Order No_],[Order Line No_],[Entry No_],[Order date]" +
                    		",[Customer No_], " +
                  					"[Salesperson Code],[Item No_],[Unit of measure],[Quantity], [Amount],[Unit Price],[Discount Pct],[Error Str], " +
                  					"[Navision Order No_], [Bin code],[Importo data ir laikas] ,[Order date time], "  +
                  				    "[Perduotas į užsakymus],[Atmestas],[Suma],[Ship-to Code],[Posting Date])  values " +
                  					"('" + doknr + "'," + counter + "000,0,'" + nextEntry + "','" + rimicust + "', " +
                  					" '" + agent + "','"+getRimiProd(itemid)+"','" + unit + "', "+kogus+" ,0,0,0,'', " +
                  					" '" + doknr + "',0,getdate(), " +
                  					" ?,'','',0,'" + type + "',?) " ;
                    	  
                    	  st2 = con2.prepareStatement(sql2);
                         
                        	  st2.setTimestamp(1, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
                        	  st2.setTimestamp(2, new java.sql.Timestamp(cc.getTimeInMillis()));
                              st2.executeUpdate();
                             
                    	 
                      }
                  
                  }
                  st2.close();
                  ccc.close();
              }
        }
  }


  

  public String getDriverVersion(){
    return "VichiLATSQL";
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
  private Connection con;
  private Connection con2;
  private Connection con3;
  private Connection con5;
  private ImportPanel parent;
  private Order order = null;
  private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
  
}
