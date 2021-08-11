package eevaeksport.imp.driver;




import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class DaividaImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());
   private String pass = null;
   private String user = null;
   private String server = null;
    boolean testing = false;
    
    
    

    public static void saveProps(Properties props, String fileName){
  	    try {
  	      FileOutputStream out =
  	          new FileOutputStream(fileName);
  	      props.store(out, "");
  	      out.close();
  	    } catch (IOException e) {
  	     
  	    }
  	  }


    public static Properties loadProps(String fileName){
      Properties props = new Properties();
      try {
        FileInputStream in = new FileInputStream(fileName);
        props.load(in);
        in.close();
      } catch (IOException e){
         // showError(e);
        }
      return props;
    }
    private long getAndUpdateRecID() {
        long nextRecID = 0;
         Properties props = loadProps("numerator.properties"); 
           if(props == null){
        	   props = new Properties();
           }
         String asi = (String) props.get("nextval");
         if(asi == null){
        	 nextRecID = 1;
         }else{
        	 nextRecID = Long.parseLong(asi) + 1;
        	 
         }
         props.put("nextval", nextRecID  + "");
         saveProps(props,"numerator.properties");
        return nextRecID;
      }
  
  public DaividaImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    if("true".equals( parent.getProps().getProperty("testing"))){
    	testing = true;
    }
    
    getAgents();
  }
  
 
  
  
  private TreeMap names = null;
  private TreeMap custwares = null;

  private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	  }
  
  private TreeMap custNames(){
	  TreeMap res = new TreeMap();
	  String sql =  "  SELECT k.klientas  AS nimi,k.klient_id AS kood , sandelis as ware " +
		"  FROM klientai k  " ;

	  if(custwares == null){
		  custwares = new TreeMap();
	  }
	   System.out.println(sql);
	   try {
		   Connection con = getNewConnection();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(sql);
	        while(rs.next()){
	        	 String name = rs.getString("nimi").trim();
		           String id = rs.getString("kood").trim();
		           String ware = rs.getString("ware");
		           res.put(id, name);
		           
		           if(ware != null && ware.trim().length() > 0){
		        	   custwares.put(id, ware.trim());   
		           }
		         
		           
	        }
	        rs.close();
	        stmt.close();
	        con.close(); 
	   }catch(Exception e){
		  e.printStackTrace(); 
	   }
	      
	        
       System.out.println("customerNames Size " + res.size());
	  
	  return res ;
  }

  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  
 
  
  
  
  
  public String identifyCustomer(String id,String regnr)  {

    String acc = null;
    	
    	try{
    String sql2 = " select k.[No_] AS klient " +
      " FROM [Viï¿½iunai Baltic$Customer] k  where k.[No_] = '" + id.trim() + "'";
    System.out.println(sql2);

    
    Statement st2 = con.createStatement();
    ResultSet rs2 = st2.executeQuery(sql2);

    while (rs2.next()) {
      acc = rs2.getString("klient").trim();

    }
    rs2.close();
    st2.close();

  	}catch(Exception e){
  		e.printStackTrace();
  	}
  	
 	
     
 	if(acc == null){
  		acc = id;
  	}
    
    System.out.println("Klient: " + acc);
    return acc;

  }

  
  public String identifyProduct(String ean, String prodID) throws SQLException{
	
	  
    String sql1 = "SELECT [No_] AS toode  FROM [Viï¿½iunai Baltic$Item] LEFT JOIN [Viï¿½iunai Baltic$Item Unit of Measure]" +
    		" vat ON (vat.[Item No_]=[No_] and vat.[Code]='Dï¿½ï¿½.' ) " +
			"  WHERE [Brï¿½kï¿½ninis kodas] ='" + ean + "'  order by [Blocked] "; // and [Blocked]=0


    String sql2 = " SELECT [No_] AS toode FROM [Viï¿½iunai Baltic$Item] " +
			"  WHERE [No_] = '" + prodID + "' "; // and [Blocked]=0 

    String prod = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

  	System.out.println(sql1);
    if (rs1.next()) {
    	if(ean.trim().length()>0){
    	      prod = rs1.getString("toode").trim();	
    	}
    }
    System.out.println("peale esimest " + prod);
    
    if(prod == null || prod.trim().length() == 0  ){
    	System.out.println(sql2);
        rs1 = st1.executeQuery(sql2);
        while (rs1.next()) {

        	if(prodID != null && prodID.trim().length()>0){
                prod = rs1.getString("toode").trim();	
        	}
        }
        System.out.println("peale teist " + prod);
      }
 
    rs1.close();
    st1.close();
    
	   System.out.println("Tagastan toote:"  + prod + " prodID:" + prodID + " ean:" + ean);
    return prod;
  
}
  
  
  
public TreeMap getCustomers(){
    TreeMap tm = new TreeMap();
   
  	 String tulem = getStringFromFile("CustomerMeriEsb.xml");
   InputSource xml = new InputSource(new StringReader(tulem));	
		XPath xPath =  XPathFactory.newInstance().newXPath();
		 try{
		NodeList nodeList = (NodeList) xPath.compile("//Customer").evaluate(xml, XPathConstants.NODESET);

		for (int i = 0 ; i < nodeList.getLength() ; i++){
		
			Node o = nodeList.item(i);
    		NodeList ll = o.getChildNodes();
			String kood = null;
			String koodx = null;
			
	for (int r = 0 ; r < ll.getLength() ; r++){

			Node nn = ll.item(r);

	  if(nn.getNodeName().equals("cmAddr")){
		   try{  kood = (nn.getFirstChild().getNodeValue());
		   koodx = Converter.cleanLitLatEst(kood);
		   if(!kood.equals(koodx))
		   custs.put(koodx, kood);
		   }catch(Exception e){ }
           	   }
		   }
		}
		}catch(Exception r){
  		  r.printStackTrace();
  	    }
   
		return tm;
}

  
  
  public void makeOrder(Order order) throws SQLException{
      getAgents();
      //on olenmas sidus obkjektid et teha tellimuse faili
     counter = 0;
     makeOutput(order); 	    
    
  }
  
  
    private TreeMap agents = new TreeMap();
    private TreeMap custs = new TreeMap();
    
  
    private String getStringFromFile(String file){
  	  StringBuilder sb = new StringBuilder();
  	  try {
  		  
  		  BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(parent.getProps().getProperty("data.dir")
  				  +  file),"UTF8"));
  			    
  	      String line = br.readLine();

  	      while (line != null) {
  	          sb.append(line);
  	         // sb.append(System.lineSeparator());
  	          line = br.readLine();
  	      }
  	      br.close();

  	  } catch (Exception e) {
  		  e.printStackTrace();
  	  } finally {
  	   
  	  }
  	 return sb.toString();	
    }

    
    public TreeMap getAgents(){
    	
    	getCustomers();
        TreeMap tm = new TreeMap();
          agents = new TreeMap();
   		 String aaa = getStringFromFile("SalesPersonMeriEsb.xml");
          InputSource xml = new InputSource(new StringReader(aaa));	   		
   		XPath xPath =  XPathFactory.newInstance().newXPath();
   		 try{
   	     	NodeList nodeList = (NodeList) xPath.compile("//salesPerson").evaluate(xml, XPathConstants.NODESET);
   	   		for (int i = 0 ; i < nodeList.getLength() ; i++){
   			Node o = nodeList.item(i);
   			String kood = null;String koodx = null;
            NodeList ll = o.getChildNodes();
   				for (int r = 0 ; r < ll.getLength() ; r++){
   					Node nn = ll.item(r);
   					
   					  if(nn.getNodeName().equals("spAddr")){
   						   try{  
   							   kood = nn.getFirstChild().getNodeValue();
   							   koodx = Converter.cleanLitLatEst(kood);
   						   }catch(Exception e){ }
   					   }
		}
   				
   				if(!kood.equals(koodx)){
   					
   				   
   			 System.out.println(kood + " "+koodx );
   		       agents.put(koodx, kood);
   				}} 
    	 
   		 }catch(Exception e){
       		  e.printStackTrace();
       	  }
        
   		 System.out.println("Agendilist tehtud" + agents.toString());
        return tm;
      }

    
    

  public static  String escapeXML(String s){
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
  
  
  private void makeOutput(Order order){

	  StringBuffer sb = new StringBuffer();
	    
	      	        String add = getAndUpdateRecID() + "";
	      	        if(add.length() == 1){
	      	          add = "00000" + add;
	      	        }
	      	      if(add.length() == 2){
	      	          add = "0000" + add;
	      	        }
	      	    if(add.length() == 3){
	      	          add = "000" + add;
	      	        }
	    	
	      	  if(add.length() == 4){
      	          add = "00" + add;
      	        }
	    		        
	      	if(add.length() == 5){
    	          add = "0" + add;
    	        }
     String number = ""
     		+ "P0" + add ;
     String fileName = parent.getProps().getProperty("conv.dir") + number +  ".txt";
     try{
     
			String comm = "";
	 		if(order.getInfo() != null && order.getInfo().trim().length() >0){
	        comm = order.getInfo();
	 		}
	 		System.out.println("Comment " + comm);
	        

	        Calendar cal = Calendar.getInstance();
	        String month = (cal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	        String day = cal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;
			  String year = cal.get(Calendar.YEAR) + "";
			  if(year.length() == 2){
				  year = "20" + year;
			  }
	        String dateString = year +"-"+ month +"-"+ day;
                
	        sb.append(number + "\t"+ dateString );
	        //N0028502	2015-11-19	2015-11-20	KOP00807

	        if(order.getShipCal() != null){
	        	cal = order.getShipCal();
	        }else{
	        	cal.add(Calendar.DAY_OF_MONTH , 1);
	        }
	    
	         month = (cal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	        day = cal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;
			 year = cal.get(Calendar.YEAR) + "";
			  if(year.length() == 2){
				  year = "20" + year;
			  }
			  dateString = year +"-"+ month +"-"+ day;

			  String kl = order.getCompany();
			  
			  sb.append("\t"+ dateString + "\t" + order.getCompany()+ "\n\n");
			  
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
	          	if(line.getPartii() != null && line.getPartii().length() > 0){
	          		comm = line.getPartii();
	          	}
	        	  sb.append( number + "\t" +number + "\t" + toode+"\t" + line.getKogus() + "\t" + comm + "\n");
	        	  coun++;
	        	//  N0028502	N0028502	4201301	3.000
	          }
	          }



	 	
	      
	      System.out.println(fileName);
	     Converter.saveFileInUTF8(fileName, sb);
	     this.setNumAddedLines(coun);

		} catch (Exception e) {
			e.printStackTrace();
		}
	 }


   
   
   
  
   public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}
  


	   private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);


 
  public String getDriverVersion(){
    return "DaividaImport";
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


  
  private String url = "jdbc:odbc:mynowest";
  private Connection con;
  private int counter = 0;
  private ImportPanel parent;
  private boolean isAmount = false;
  private StringBuffer troubles= new StringBuffer();

  public String getTrouble() {
  	// TODO Auto-generated method stub
  	return troubles.toString();
  }
  
}
