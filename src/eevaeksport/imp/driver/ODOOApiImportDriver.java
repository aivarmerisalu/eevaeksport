
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import static java.util.Arrays.asList;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.Email;
import eevaeksport.Product;
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

public class ODOOApiImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public ODOOApiImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    try {
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    }  catch (Exception e) {
      parent.showError(e);
    }
    
    
    username = parent.getProps().getProperty("odoo.user");	
    company = parent.getProps().getProperty("odoo.dbname");
    password = parent.getProps().getProperty("odoo.pass");
    server = parent.getProps().getProperty("odoo.server");
		
    
  }
  private String username = null;
  private String company = null;
  private String password = null;
  private String server = null;
  private int uid = -1;

  

static int login(String url, String db, String login, String password) throws XmlRpcException, MalformedURLException {
	XmlRpcClient client = new XmlRpcClient();
	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	config.setEnabledForExtensions(true);

	config.setServerURL(new URL(url+"/xmlrpc/2/common"));
	client.setConfig(config);
	Object[] params = new Object[] {db,login,password}; // Ok & simple
	Object uid = client.execute("login", params);
	if (uid instanceof Integer) {
		return (int) uid;
	}else {
		System.out.println(uid.toString());
	}
		
	return -1;
}
  
  
  public String identifyCustomer(String id,String regnr){
	     return id;
	  }

	  public String identifyProduct(String ean, String prodID){
	    return prodID;
	  }
	   

  
  public void makeOrder(Order order) throws SQLException{
            counter = 0;
     		makeOutput(order);
      System.out.println("counter " + counter);
      setNumAddedLines(counter);
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
	  
  
  public TreeMap getProducts(Agent agent){
	     TreeMap tm = new TreeMap();
	   
	      TreeMap  ladutr = new TreeMap();
	      Object[] params = new Object[] {};
	      TreeMap names = new TreeMap();
	  try {    
	  	final XmlRpcClient models = new XmlRpcClient() {{
		    setConfig(new XmlRpcClientConfigImpl() {{
		        setServerURL(new URL(String.format("%s/xmlrpc/2/object", server)));
		    }});
		}};
		
		
		List<Object> res0 = java.util.Arrays.asList((Object[])models.execute("execute_kw", asList(
				    company, uid, password,
				    "product.product", "search_read",
				    asList(asList(asList("sale_ok", "=", true))), //(java.util.Arrays.asList("employee", "=", false))
				    new HashMap() {{
				        put("fields", java.util.Arrays.asList("name")); // 
				        put("limit", 6000);
				    }}
				)));
		
		 
		  for (Object json : res0){
			  HashMap row = (HashMap)json;  
			  names.put(row.get("id")+"", row.get("name")+"");
		  }

		  System.out.println("Res0 " + names.size());	
		 
	      
		List<Object> res = java.util.Arrays.asList((Object[])models.execute("execute_kw", asList(
				    company, uid, password,
				    "product.product", "search_read",
				    asList(java.util.Arrays.asList(
				    		asList("sale_ok", "=", true),
				    		asList("type", "=", "product"),
				    		asList("warranty", ">", 0),
				    		asList("active", "=", true))), //(java.util.Arrays.asList("employee", "=", false))
				    new HashMap() {{
				        put("fields", java.util.Arrays.asList( "price_extra","product_tmpl_id",
				       		 "type","packaging","description","warehouse_id",
				       		 "reception_count","code","rental","track_production","price","price_margin","qty_available",
				       		 "active","weight_net","default_code","weight" ,"name",
				       		 "produce_delay","list_price","sale_delay","property_stock_account_input",
				       		 "lst_price","delivery_count", "volume","lst_price_switch","ean13","categ_id","packaging_ids")); // 
				        put("limit", 3000);
				    }}
				)));
		 
		/* */
		
		
	     
	     for (Object json : res){

	    	 HashMap row = (HashMap)json;
	    	// System.out.println("Res " + row.toString());
	    	 String id = row.get("id")+"";
	    	 String code = row.get("default_code")+"";
	    	 if(!code.equals("false") ) {
	    		
	         Product prod = new Product(code);
	        
	         prod.setName(row.get("name")+"");
	       
	        String idname = null;
		         Object [] countryIdName = (Object []) row.get("product_tmpl_id");
		      //   System.out.println(countryIdName.toString() + " " + prod.getName());
		         if (countryIdName != null){
		          String country = countryIdName[1] + "";
		          idname = (country);
		           }
	         
	        // System.out.println(idname + " " + prod.getName());
	         if(idname != null && names.get(idname) != null) {
	        	 prod.setName(Converter.enCodeEst(names.get(idname)+" " + code));
	          }
	         
	       
	     String country = null;
	     String ean = row.get("ean13") +"";
	     if(!ean.equals("false")) {
	    	 prod.setEAN(ean);
	     }
	     
	     
	     
	     String asi = "packaging_ids";
	     
	     countryIdName = (Object []) row.get(asi);
	    // System.out.println(" " +countryIdName.getClass().getName() + " " + idname);
	      if (countryIdName != null && countryIdName.length > 0){
	    	  try {
	    		    country = countryIdName[0] + "";;
	    	        System.out.println(" " +country + " " + idname);
	    	  }catch(Exception e) {
	    		  e.printStackTrace();
	    	  }
	        
	        } 
	     try {
	    	 prod.setPrice(Double.parseDouble(row.get("lst_price_switch")+""));   
	     }catch(Exception e) {
	    	e.printStackTrace(); 
	     }
	     try {
	    	 prod.setStock(Double.parseDouble(row.get("qty_available")+""));   
	     }catch(Exception e) {
	    	e.printStackTrace(); 
	     }
	     
	     
	     tm.put(id, prod);
	     }
	    	 
    	 }
	}catch(Exception e){
	  	e.printStackTrace();	
	  	}
	System.out.println("tm.size() " + tm.size());
	     return tm;
	   }


  TreeMap tooted  = null ;
  
 private void makeOutput(Order order){
 	StringBuffer sb = new StringBuffer();
 	 nf.setGroupingUsed(false);
 	try {
		uid = login(server,company,username,password);
	
	if (uid >0) {
			System.out.println("Login Ok " + uid);
	  } else {
			System.out.println("Login Fail ");
	 }
	} catch (Exception e) {
		e.printStackTrace();
	}
 	if(tooted == null) {
 		tooted = getProducts(null);
 	}
 

 	try {
 	  int cou = 0;
		String ref = order.getTeostaja();
		if ( ref == null ){
			ref =  " ";
		}
		String com = order.getEmailBody();
		if ( com == null ){
			com =  " ";
		}		
		 String doknr = order.getDokNr();

         String ls = System.getProperty("line.separator");
         String fixagent = order.getAgent(); 
      
         String  custID = getCustCode(order.getCompany());
         
          Calendar  shipDate = order.getShipCal();
           Calendar cal = Calendar.getInstance();
          
           if (shipDate != null) {
                 cal = shipDate;
               }
         
           sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + ls);
           sb.append("<E-Document>" + ls);
      
           sb.append("<Header>" + ls);
           sb.append("<DateIssued>" +  Converter.escapeXML(getDateString(Calendar.getInstance())) + "</DateIssued>" + ls);
           sb.append("<Standard>HULGI</Standard>" + ls);
              sb.append("<SenderID>ELEIB</SenderID>" + ls);
              sb.append("<ReceiverID>ELEIB</ReceiverID>" + ls); 	
              sb.append("<Channel>MERISOFT</Channel>" + ls); 	
         

              
           sb.append("</Header>" + ls);
           sb.append("<Document>" + ls);
           
           String type = "order";
      
           sb.append("<DocumentType>" + type + "</DocumentType> " + ls);
  
     	 sb.append("<DocumentFunction>ORIGINAL</DocumentFunction> " + ls);
           
           sb.append("<DocumentParties>" + ls);
       //    String regnr = getRegKood(custID);
         	  sb.append("<BuyerParty>" + ls);
              sb.append( "<PartyCode>" +   Converter.escapeXML((custID)) + "</PartyCode>" + ls); 
          //    sb.append( "<RegNum>" +  escape(regnr) + "</RegNum>" + ls); 
            
              sb.append("</BuyerParty>" + ls);
           
              sb.append("<OrderParty>" + ls);
              sb.append( "<PartyCode>" +   Converter.escapeXML(custID) + "</PartyCode>" + ls);
            //  sb.append( "<RegNum>" +  escape(regnr) + "</RegNum>" + ls); 
              sb.append("</OrderParty>" + ls);
           
              sb.append("<DeliveryParty>" + ls);
              sb.append("<PartyCode>" +   Converter.escapeXML(custID) + "</PartyCode>" + ls); 
             // sb.append( "<RegNum>" +  escape(regnr) + "</RegNum>" + ls); 
         String agent = order.getAgent();
           if(fixagent != null){
         	  agent = fixagent;
           }
           if(agent.equals("mand")){
            //   agent = "m�nd";
          }
           
        //   sb.append("<UserId>" +  agent + "</UserId>" + ls);
           sb.append("</DeliveryParty>" + ls);
           
       
           sb.append("</DocumentParties>" + ls);
           
           sb.append("<DocumentInfo>" + ls);
           sb.append("<DocumentSubType>MERISOFT</DocumentSubType>" + ls);
              
           if(order.getStatus() > 5) {
        	   ref = doknr;
           }
           if(ref != null && ref.trim().length() > 0 ){
         	  sb.append("<RefInfo>" + ls);
         	   sb.append("<SourceDocument type=\"order\">" + ls);
               sb.append("    <SourceDocumentNum>" +  Converter.escapeXML(ref) + "</SourceDocumentNum>" + ls );
               sb.append("</SourceDocument>" + ls);
         	   sb.append("</RefInfo>" + ls); 
           }
           
       String    sss = (Calendar.getInstance().getTime().getTime()+ "").substring(5) ;
           sb.append("<DocumentNum>" +  Converter.escapeXML(agent + "-" + sss )+ "</DocumentNum>" + ls); 
           sb.append("<DateInfo>" + ls);
           sb.append("<OrderDate>" + Converter.escapeXML(getDateString(Calendar.getInstance())) + "</OrderDate>" + ls);
           sb.append("<DeliveryDateRequested>" +   Converter.escapeXML(getDateString(cal)) + "</DeliveryDateRequested>" + ls); //aeg taha panna  T10:00:00
           sb.append("</DateInfo>" + ls);
           sb.append("</DocumentInfo>" + ls);
           sb.append("   <DocumentItem>" + ls);

           
           Vector sortedLines = order.getOrderLines();
           for (int i = 0; i < sortedLines.size(); i++) {
               OrderLine line = (OrderLine)sortedLines.elementAt(i);
               double ale = line.getDiscount();     
               counter++;
               String ean = line.getEAN();
               
               if (ale == -1 ){
                   ale = 0 ;
                   }
               sb.append("      <ItemEntry>" + ls);
               sb.append("      <LineItemNum>"  + (i+1) + "</LineItemNum>" + ls);
             
               Product pr = null;
               String tcode = line.getToode();
               if(tooted.get(line.getToode()) != null) {
            	   pr = (Product)tooted.get(line.getToode());
            	   tcode = pr.getID();
               }
               
               sb.append("         <SellerItemCode>" + tcode + "</SellerItemCode>" + ls);
               if( ean==null || ean.length() < 6 ) {
              //    ean = getEan(line.getToode());
                 }
               String unit = line.getUnit() ;
  			  
    			if( pr != null) {
    				
    				unit = pr.getUnit();
    			}
               
              sb.append("         <BaseUnit>" + unit + "</BaseUnit>" + ls);
             	  sb.append("         <ItemUnitRecord>" + ls);
             	 
             			
                  sb.append("                <ItemUnit>" + unit+ "</ItemUnit>" + ls);
                  sb.append("         </ItemUnitRecord>" + ls);
                  
              // if(smarten)
                //  sb.append("          <ItemDescription>" + cleanLitLatEst(escape(getNimi(line.getToode()))) + "</ItemDescription>" + ls);               
                  sb.append("         <AmountOrdered>" +  Converter.escapeXML(getNF().format(line.getKogus())) + "</AmountOrdered>" + ls);
                
               if(line.getDiscount() > 0 && order.getStatus() < 3){
             	 // double discprice = (100d -line.getDiscount())*line.getPrice()/100d ;
               sb.append("         <ItemPrice>" +  (getNF().format(line.getPrice())) + "</ItemPrice>" + ls); 
               sb.append("         <ItemSum>" + (getNF().format(line.getPrice() * line.getKogus())) + "</ItemSum>" + ls);
               sb.append("         <Addition addCode=\"DSC\">" + ls);
               sb.append("              <AddRate>" + (getNF().format(line.getDiscount())) + "</AddRate>" + ls);
               sb.append("          </Addition>" + ls);
                
               }
              
           
     /*          sb.append("         <VAT>" + ls + "                <VATRate>" +  + "</VATRate>" + ls);
               sb.append("         </VAT>"+  ls);
               
               sb.append("         <ItemReserve>" + ls );
         
               sb.append("             <ItemReserveUnit>" + ls );
             
               
               sb.append("                <ItemUnit>" +  escape(yhik.trim()) + "</ItemUnit>" + ls );
               sb.append("                <AmountActual>" + getNF().format(line.getKogus()) + "</AmountActual>" + ls );
               sb.append("             </ItemReserveUnit>" + ls );
               sb.append("         </ItemReserve>" + ls );*/
               sb.append("      </ItemEntry>" + ls);
     
               
               
           }
           

           sb.append("   </DocumentItem>" + ls);
           
     	    String comm = order.getInfo();
             if(comm != null && comm.length()>100){
             	comm = comm.substring(0,100);
             }
             
         /*    if(comm != null && comm.trim().length() > 0 ){
             	  sb.append("   <AdditionalInfo>" +ls);
                   sb.append("       <Extension extensionId=\"remarks\">" + ls);
                   sb.append("       <InfoName>Remark1</InfoName>");
                   sb.append("            <InfoContent>" +  Converter.escapeXML(comm) + "</InfoContent>" + ls );
                   sb.append("       </Extension>");
                     //-<Extension extensionId="externalremarks"><InfoContent>K�esoleva arve aluseks olev n�ue on loovutatud AS-ile SEB Liising ja k�ik arvel toodud summad tuleb maksta AS-i SEB Liising arvelduskontole 10052045861006 SEB Pangas (kood 401). Kogu k�esoleva arvega seotud info tuleb teatavaks teha AS-ile SEB Liisingule.</InfoContent></Extension>
                   sb.append("   </AdditionalInfo>" + ls);	
             }*/
           
             
           sb.append("</Document>" + ls);
           sb.append("</E-Document>" + ls);
 
      counter = counter + cou;
    	//
      String fn = doknr;
      if(doknr.trim().length()== 0){
    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
      }
      String fileName =  Converter.escapeXML(parent.getProps().getProperty("conv.dir") + agent)  + "." + fn  +  ".xml";
      System.out.println(fileName);
      Writer out = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream(fileName), "UTF-8"));
			try {
			    out.write(sb.toString());
			} finally {
			    out.close();
			}

			  if(comm != null){
				  Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(order.getAgent());
		    	    Converter.sendCommentEmail( comm,null,order.getDokNr(),order.getCompany(), 
		    	    		" " +  currName  , " ",false,ag,parent,null);
		         }
			  currName = null;
			//System.exit(0);
	} catch (Exception e) {
		e.printStackTrace();
	}
 }
 
 

 
 
 private String getCustCode(String code){

	 
	   String tm = "noname";

		  	
	  	try{

	      	final XmlRpcClient models = new XmlRpcClient() {{
	      	        setConfig(new XmlRpcClientConfigImpl() {{
	      	        setServerURL(new URL(String.format("%s/xmlrpc/2/object", server)));
	      	    }});
	      	}};  	
	    
	  		
	  		//  this.getFielsnames("res.partner", models);
	      	List<Object> res  =	 asList((Object[])models.execute("execute_kw", asList(
	      			    company, uid, password,
	      			    "res.partner", "search_read",
	      			    asList(asList( asList("id", "=", code ), asList("is_company", "=", true), asList("customer", "=", true))), //(java.util.Arrays.asList("employee", "=", false))
	      			    new HashMap() {{
	      			        put("fields", java.util.Arrays.asList("street","customer","ref"
	      			        		  ,"name"));
	      			        put("limit", 5000);
	      			    }}
	      			)));
	      	
	      	  
	      for (Object json : res){
	      HashMap row = (HashMap)json; 
	          tm = row.get("ref") + "";
	          currName = row.get("name") + "";
	System.out.println("Cust " + tm + " " + code + " " + currName);          
	     }

	     } catch(Exception e){
	  	    e.printStackTrace();	
	  	 }
		     return tm;
		   }
	 String currName = null;

 
  
	public NumberFormat getNF() {
		return this.nf;
	}
 
  public String getDriverVersion(){
    return "ODOOApiImport";
  }

  
  public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  
  private NumberFormat calNF = NumberFormat.getInstance();

  
	
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
 
  private int counter = 0;
  private ImportPanel parent;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
