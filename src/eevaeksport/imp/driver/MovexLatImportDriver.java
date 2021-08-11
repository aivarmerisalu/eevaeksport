
/**
 * @author Aivar  Merisalu

 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;







import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.Product;
import eevaeksport.driver.Toode;
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

public class MovexLatImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public MovexLatImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "oracle.jdbc.driver.OracleDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection("jdbc:oracle:thin:@10.10.100.97:1521:GM3REP1", parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      tooted = this.getProducts(null);
   System.out.println("Products size " + tooted.size());
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  public HashMap getProducts(Agent agent){

	  
	  
	  String ex = parent.getProps().getProperty("products");
	      if (ex == null){
	      ex  = "";
	      }

	      String keg = parent.getProps().getProperty("kegs");
	          if (keg == null){
	          keg  = "";
	          }
	          
	          String deflist = parent.getProps().getProperty("deflist");
	          if (deflist == null){
	          	deflist  = "";
	          }
	      String cmd = "SELECT t.MMITNO AS kood, " +
	            " t.MMITDS AS nimetus,t.MMCFI2 AS tykke " + 
	  		 " ,MMSPE4 AS sort FROM mvxjdta.MITMAS t";



	      HashMap products = new HashMap();
	      try {
	        Statement st = con.createStatement();
	        ResultSet rs = st.executeQuery(cmd);

	        while(rs.next()){
	          String kood = rs.getString("kood").trim();
	          Product prod = new Product(kood);
	          String nimetus =  rs.getString("nimetus").trim();
	          prod.setName(nimetus);

	          String sort = rs.getString("sort");
	          if(sort != null && sort.trim().length() > 0){
	          	if(sort.trim().length()== 1){
	          		sort = "000" +sort.trim();
	          	}else if (sort.trim().length()== 2){
	          		sort = "00" +sort.trim();
	          	}else if (sort.trim().length()==3){
	          		sort = "0" +sort.trim();
	          	}
	          	prod.setSorting(sort.trim()+prod.getID());
	          }

	          products.put(prod.getID(), prod);
	        
	        }
	        rs.close();
	        st.close();
	      } catch (Exception e){
	        parent.showError(e);
	      }

	      return products;
	    }

  


  public  Vector sortProds(Vector inVec, HashMap tooted){
    Vector vec = new Vector();
    for (int i = 0 ; i < inVec.size() ; i++){
      vec.addElement(inVec.elementAt(i));
    }
    Vector sorted = new Vector(vec.size());
    while(vec.size() > 0){
      OrderLine smallest = null;
      Product smProd = null;
      for (Enumeration enumer = vec.elements(); enumer.hasMoreElements() ; ){
        OrderLine ol = (OrderLine)enumer.nextElement();
        Product prod = (Product)tooted.get(ol.getToode());
       
        if(prod == null){
        	 prod = new Product("9999");
       	  prod.setSorting("9999");
        }
       
        	
        if ( (smallest == null) || (smProd!= null && prod != null && prod.getSorting().compareTo(smProd.getSorting()) < 0 )) {
          smallest = ol;
          smProd = (Product)tooted.get(smallest.getToode());
          if(smProd == null){
        	  smProd = new Product("9999");
        	  smProd.setSorting("9999");
          }
        }
      
      }
      vec.removeElement(smallest);
      sorted.addElement(smallest);
      System.out.println(smallest.getToode());
    
      }

    for (int i = 0 ; i < sorted.size() ; i++){
       Product pp = (Product)tooted.get(((OrderLine)sorted.elementAt(i)).getToode());
       if(pp != null){
    	   System.out.println(pp.getSorting()+ " "  +pp.getName());
      }else{
    	   System.out.println("Tundmatu toode");
      }
       }
    return sorted;
  }
  
  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	
  	 System.out.println("Kliendi M��ramine: " );
  	
  	    sql1 = "select OKCUNO as kood  from mvxjdta.OCUSMA where OKCUSU='" + id 
  	    + "' and okcfc9 IN ('2050','2055','2009','2008')";	
	
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim() ;
    }
    
    rs1.close();
    st1.close();

    System.out.println("Klient: " + acc + " " + id);
    return acc;

  }
  
  public String identifyKett(String id)  {
	  	String sql1 = null;
	  	
	  	 System.out.println("Kliendi M��ramine: " );
	  	
	  	    sql1 = "select okcfc9 as kood  from mvxjdta.OCUSMA where OKCUNO='" + id  + "' ";	
		
	    System.out.println(sql1);
	    String acc = "";
try{
	  Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      acc = rs1.getString("kood").trim() ;
	    }
	    
	    rs1.close();
	    st1.close();
}catch(Exception e){
	e.printStackTrace();
}
	  

	    System.out.println("Klient: " + acc + " " + id);
	    return acc;

	  }

  
  

  private Vector splitOrder (Order order){
	  
	  //order.setOrderLines(sortProds(order.getOrderLines(),tooted));
	  
	  String kett =  identifyKett(order.getCompany());
	  boolean mego = false;
	  if(kett.equals("2009") || kett.equals("2008")){
		  mego = true;
	  }
	  
	  String ordnr = order.getDokNr();
	 	  if(order.getDokNr() == null){
	        	 ordnr = "IKI" + (Calendar.getInstance().getTimeInMillis()+ "").substring(5);
	         }
	  
	  Vector result = new Vector();
	  Order ord600 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ord700 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ord500 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ord400 = new Order(order.getCompany(),order.getAgentFirstName());
	     
	  ord600.setAgentCode(order.getAgentCode());
	  if(mego){
		  ord600.setDokNr(ordnr + "-1");
	  }else{
		  ord600.setDokNr(ordnr + "01");	
	  }
	  ord600.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord600.setWarehouse(order.getWarehouse());
	  
	  ord400.setAgentCode(order.getAgentCode());
	  if(mego){
		  ord400.setDokNr(ordnr + "-3");
	  }else{
		  ord400.setDokNr(ordnr + "03");	
	  }
	  ord400.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord400.setWarehouse(order.getWarehouse());

	  ord500.setAgentCode(order.getAgentCode());
	  ord500.setDokNr(ordnr);
	  ord500.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord500.setWarehouse(order.getWarehouse());

	  ord700.setAgentCode(order.getAgentCode());
	  if(mego){
		  ord700.setDokNr(ordnr + "-2");
	  }else{
		  ord700.setDokNr(ordnr + "02");	
	  }ord700.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord700.setWarehouse(order.getWarehouse());
	  
	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	  int linecount = 0;
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
		  
		     System.out.println("Splitting " + lin.getToode() + " " + lin.getTooteNimi());
		     try{
		    	 
		    
		     if(linecount < 14){
		     	
		     	ord500.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		     	System.out.println("500 " + lin.getToode());
		     }else if(linecount < 28){
		     
		     	ord600.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		     	System.out.println("600 " + lin.getToode());
		     }else if(linecount < 42){
		    	
			     ord700.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     		lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     System.out.println("700 " + lin.getToode());
		     }else {
		    	
			     ord400.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     		lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     System.out.println("400 " + lin.getToode());
		     }
		      linecount++;
		     }catch(Exception e){
		    	 e.printStackTrace();
		     }
		  
	  }
	  


	  if(ord500.getOrderLines() != null && ord500.getOrderLines().size()>0){
		  result.add(ord500);  
	  }
	  if(ord600.getOrderLines() != null && ord600.getOrderLines().size()>0){
		  result.add(ord600);  
	  }
	  if(ord700.getOrderLines() != null && ord700.getOrderLines().size()>0){
		  result.add(ord700);  
	  }
	  if(ord400.getOrderLines() != null && ord400.getOrderLines().size()>0){
		  result.add(ord400);  
	  }
	 System.out.println("Result =" +order.getDokNr()
			 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }
  
  
  private HashMap getPartnerCodesMego() {
		HashMap ean = new HashMap();
		File file = new File( "data" + File.separator + "artiklid.txt");
		//System.out.println("path" + path);
		if (file.exists()) {

			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = null;
				String token = null;
				while ((line = reader.readLine()) != null) {
					if (line.trim().length() != 0) {
						StringTokenizer st = new StringTokenizer(line, ";");
						String key = st.nextToken();
						token = st.nextToken();
						ean.put(key, token);
						if(st.hasMoreTokens()){
							token = st.nextToken();
							if(token.equals("1")){
							//	round = true;
							}
								
						}
						// System.out.println("key" + key + token);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return ean;
	}
	
	private HashMap getPartnerCodes() {
		HashMap res = new HashMap();
		File file = new File( "data"
				+ File.separator + "ikiartiklid.txt");
		//System.out.println("path" + path);
		if (file.exists()) {

			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = null;
				String token = null;
				while ((line = reader.readLine()) != null) {
					if (line.trim().length() != 0) {
						StringTokenizer st = new StringTokenizer(line, ";");
						String key = st.nextToken();
						
						token = st.nextToken();
						Toode prod = new Toode(token);
						
						token = st.nextToken();
						double kordaja = Double.parseDouble(token);
						prod.setKogus(kordaja);
						
						token = st.nextToken();
						double round = Double.parseDouble(token);
						prod.setKastis(round);
						
						token = st.nextToken();
						prod.setLadu(token);
						res.put(key, prod);
						
						// System.out.println("key" + key + token);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return res;
	}
  
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
   HashMap tooted = getPartnerCodes(); 
   String prod = null;
    if(tooted.get(ean) != null){
    	Toode tt = (Toode)tooted.get(ean);
    	prod = tt.getID();
    }
    return prod;
  }


  
  public void makeOrder(Order order) throws SQLException{
	  Vector splitted =  splitOrder(order);
	  Enumeration en = splitted.elements();

	   cou = 0;
	  while(en.hasMoreElements()){
		  Order ord = (Order)en.nextElement();	
          makeOutput(ord);  
	  }
	  setNumAddedLines(cou);
  }

  
 private void makeOutput(Order ord){
 	StringBuffer sb = new StringBuffer();
 	 nf.setGroupingUsed(false);
 	  
 
 	 
 	try {
 		 HashMap tooted = getPartnerCodes(); 
 		 HashMap tootedMego = getPartnerCodesMego(); 
 		 String kett =  identifyKett(ord.getCompany());
 		 
		String agent = ord.getAgent();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, 1);
		
		System.out.println("Tuvastatud agent :" + agent );
	
		String ref = ord.getReference();
		if ( ref == null ){
			ref =  " ";
		}
		String com = ord.getEmailBody();
		if ( com == null ){
			com =  " ";
		}		
		String ware = ord.getWarehouse();
		if ( ware == null ){
			ware = " ";
		}
		 String doknr = ord.getDokNr();
		 if(doknr == null){
			doknr = Calendar.getInstance().getTimeInMillis() +""; 
		 }
         if(doknr.length()>6){
         	 doknr = doknr.substring(doknr.length()-6,doknr.length());
         }
        

         String ls = System.getProperty("line.separator");
         
         sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + ls);
         sb.append("<Envelope xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:env=\"http://www.intentia.com/MBM_Envelope_1\" xmlns=\"http://www.intentia.com/schemas/mec\">" + ls);
         sb.append(" <Header>" + ls);
         sb.append("  <env:delivery>" + ls);
         sb.append("  <env:to>" + ls);
         sb.append("    <env:address>cesualus</env:address> " + ls);
         sb.append("  </env:to>" + ls);
         sb.append("  <env:from>" + ls);
         sb.append("    <env:address>MobileJava</env:address> " + ls);
         sb.append("  </env:from>" + ls);
         sb.append("  </env:delivery>" + ls);
         sb.append("  <env:manifest>" + ls);
         sb.append("    <env:reference uri=\"#ORDERS@1\">" + ls);
         sb.append("      <description /> " + ls);
         sb.append("    </env:reference>" + ls);
         sb.append("  </env:manifest>" + ls);
         sb.append(" </Header>" + ls);
         sb.append("<Body>" + ls);
         sb.append("<ORDERS>" + ls);
         sb.append("   <BGM>" + ls);
         sb.append("      <cmp01>" + ls);
         
         String ordnr = ord.getDokNr();
         if(ordnr == null){
        	 ordnr = (Calendar.getInstance().getTimeInMillis()+ "").substring(5);
         }
         
         sb.append("        <e01_1001>700</e01_1001>" + ls);
         sb.append("      </cmp01>" + ls);
         sb.append("     <e01_1004>" + ordnr + "</e01_1004>" + ls);
         sb.append("   </BGM>" + ls);
         sb.append("      <DTM>" + ls);
         sb.append("          <cmp01>" + ls);
         sb.append("            <e01_2005>2</e01_2005>" + ls);
         sb.append("            <e02_2380>" + escape(getDateString(cal)) + "</e02_2380>" + ls);
         sb.append("            <e03_2379>101</e03_2379> " + ls);
         sb.append("         </cmp01>" + ls);
         sb.append("     </DTM>" + ls);
         sb.append("   <g002>" + ls);
         sb.append("     <NAD>" + ls);
         sb.append("     <e01_3035>BY</e01_3035>" + ls);
         sb.append("      <cmp01>" + ls);
         sb.append("         <e01_3039>" + escape(ord.getCompany()) + "</e01_3039>");
         sb.append("      </cmp01>" + ls);
         sb.append("     </NAD>" + ls);
         sb.append("   </g002>" + ls);
         
         
         
    Vector lines = ord.getOrderLines();
      for (int i = 0; i < lines.size(); i++) {
    	   
      	OrderLine line = (OrderLine)lines.elementAt(i);
            double disc = line.getDiscount();
          
            Toode tt = null;
            nf.setMaximumFractionDigits(4);
            if(kett.equals("2009") || kett.equals("2008") ){
   			 if(tootedMego.get(line.getToode()) != null){
   				tt = new Toode(line.getToode());
   				tt.setKogus(Double.parseDouble((String)tootedMego.get(line.getToode())));
   				tt.setKastis(1);
   				 System.out.println("MEGO");
   			 }
    		 }else{
    			 if(tooted.get(line.getEAN()) != null){
           	    	 tt = (Toode)tooted.get(line.getEAN());   
           	    	 System.out.println("IKI");
           	        }
    		 }
            
       	
  
     
     sb.append("   <g025>" + ls);
     sb.append("      <LIN>" + ls);
     sb.append("         <e01_1082>1</e01_1082> " + ls);
     sb.append("          <cmp01>" + ls);
     sb.append("             <e01_7140>" + escape(line.getToode()) + "</e01_7140>" + ls);
     sb.append("             <e02_7143>SA</e02_7143> " + ls);
     sb.append("          </cmp01>" + ls);
     sb.append("      </LIN>" + ls);

  
     
     sb.append("       <QTY>" + ls);
     sb.append("          <cmp01>" + ls);
     sb.append("          <e01_6063>21</e01_6063>" + ls);
     double amount = line.getKogus();
     if(tt != null){
    	 amount = amount * tt.getKogus();
    	 
    	 if(tt.getKastis()> 0.5 ){
				System.out.println("kogus" + amount + " nimi " + line.getTooteNimi());
				amount = Math.round(amount + 0.49);
	          	System.out.println("kogusrr" + amount);		          
		}
		String out = amount + "";
		if(out.indexOf(".") != -1 && out.substring(out.indexOf("."),out.length()).length() >= out.indexOf(".")+3){
			out = out.substring(0,out.indexOf(".")+3);	
		}

		System.out.println("out " + out);
		amount = (Double.parseDouble(out));
    	 
     }
 //    System.out.println(line.getProduct()+ " " + amount + " " + prod.getSortCode());
     
     sb.append("            <e02_6060>" + amount + "</e02_6060> " + ls);
     sb.append("          </cmp01>" + ls);
     sb.append("      </QTY>" + ls);
     sb.append("      <DTM>" + ls);
     sb.append("          <cmp01>" + ls);
     sb.append("            <e01_2005>2</e01_2005>" + ls);
     sb.append("            <e02_2380>"  + escape(getDateString(cal))  +  "</e02_2380>" + ls);

     sb.append("            <e03_2379>101</e03_2379> " + ls);
     sb.append("         </cmp01>" + ls);
     sb.append("     </DTM>" + ls);
     sb.append("   </g025>" + ls);

        
       cou++;
      }
      
      
      
      sb.append("</ORDERS>" + ls);
      sb.append("</Body>" + ls);
      sb.append("</Envelope>" + ls);
      
      
      
      
      
      
    	
      String fn = ordnr;
      if(doknr.trim().length()== 0){
    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
      }
      String fileName = escape(parent.getProps().getProperty("conv.dir") + ord.getCompany() )  + "." + fn  +  ".xml";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sb.toString().getBytes());
        fos.close();

	} catch (Exception e) {
		e.printStackTrace();
	}
 }
  


 
  public String getDriverVersion(){
    return "MovexLatImport";
  }



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
  
  public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  
  private NumberFormat calNF = NumberFormat.getInstance();


  private String getDateString(Calendar cal){
    StringBuffer sb = new StringBuffer();
    String year =   cal.get(Calendar.YEAR) + "";
    sb.append(year.substring(2,4));
    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
    if (month.length() == 1){
      sb.append("0");
    }
    sb.append(month);

    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    if (day.length() == 1){
      sb.append("0");
    }
    sb.append(day);
    return sb.toString();
  }

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private   int cou = 0;
  private ImportPanel parent;
  private HashMap tooted = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
